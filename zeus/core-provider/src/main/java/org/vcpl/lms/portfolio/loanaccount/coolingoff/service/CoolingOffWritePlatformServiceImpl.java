package org.vcpl.lms.portfolio.loanaccount.coolingoff.service;

import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.collection.data.InterestAppropriationData;
import org.vcpl.lms.portfolio.collection.data.PrincipalAppropriationData;
import org.vcpl.lms.portfolio.collection.service.CollectionAppropriation;
import org.vcpl.lms.portfolio.collection.utills.Collectionutills;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.coolingoff.constants.CoolingOffConstants;
import org.vcpl.lms.portfolio.loanaccount.coolingoff.utils.CoolingOffUtils;
import org.vcpl.lms.portfolio.loanaccount.coolingoff.validator.CoolingOffDataValidator;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.vcpl.lms.portfolio.loanaccount.foreclosure.ForeclosureUtils;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.vcpl.lms.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.vcpl.lms.portfolio.loanaccount.serialization.LoanEventApiJsonValidator;
import org.vcpl.lms.portfolio.loanaccount.service.LoanArrearsAgingServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeWritePlatformService;
import org.vcpl.lms.portfolio.loanproduct.domain.CoolingOffInterestAndChargeApplicability;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.paymentdetail.domain.PaymentDetail;
import org.vcpl.lms.useradministration.domain.AppUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CoolingOffWritePlatformServiceImpl implements CoolingOffWritePlatformService {
    private static final Logger LOG = LoggerFactory.getLogger(CoolingOffWritePlatformServiceImpl.class);


    private static final BigDecimal divisor = BigDecimal.valueOf(100);
    private final CoolingOffDataValidator coolingOffDataValidator;
    private final LoanEventApiJsonValidator loanEventApiJsonValidator;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final FromJsonHelper fromJsonHelper;
    private final LoanScheduleHistoryWritePlatformService loanRepaymentScheduleHistoryRepository;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final LoanHistoryRepo loanHistoryRepo;

    private final LoanArrearsAgingServiceImpl loanArrearsAgingService;

    public CoolingOffWritePlatformServiceImpl(CoolingOffDataValidator coolingOffDataValidator, LoanEventApiJsonValidator loanEventApiJsonValidator,
                                              PlatformSecurityContext context, LoanRepositoryWrapper loanRepositoryWrapper, FromJsonHelper fromJsonHelper,
                                              LoanScheduleHistoryWritePlatformService loanRepaymentScheduleHistoryRepository,
                                              CodeValueRepositoryWrapper codeValueRepositoryWrapper, LoanHistoryRepo loanHistoryRepo, LoanArrearsAgingServiceImpl loanArrearsAgingService) {

        this.coolingOffDataValidator = coolingOffDataValidator;
        this.loanEventApiJsonValidator = loanEventApiJsonValidator;
        this.context = context;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.fromJsonHelper = fromJsonHelper;
        this.loanRepaymentScheduleHistoryRepository = loanRepaymentScheduleHistoryRepository;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
        this.loanHistoryRepo = loanHistoryRepo;
        this.loanArrearsAgingService = loanArrearsAgingService;
    }

    @Override
    public CommandProcessingResult coolingOff(JsonCommand command) {

        AppUser appUser = getAppUserIfPresent();
        Long loanId = command.getLoanId();
        coolingOffDataValidator.validate(command.json());
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final JsonElement element = fromJsonHelper.parse(command.json());
        final LocalDate transactionDate = this.fromJsonHelper.extractLocalDateNamed(CoolingOffConstants.TRANSACTION_DATE, element);
        // Added threshold days validation based on cooling off date
        final LocalDate coolingOffDate = this.fromJsonHelper.extractLocalDateNamed(CoolingOffConstants.COOLING_OFF_DATE, element);

        checkLoanIsEligibleForCoolingOff(loan, coolingOffDate);
        LOG.info("CoolingOff is initiated for loan :{}", loan.getId());

        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");
        final String receiptReferenceNumber = command.stringValueOfParameterNamedAllowingNull("receiptReferenceNumber");
        final String partnerTransferUtr = command.stringValueOfParameterNamedAllowingNull("partnerTransferUtr");
        final Date partnerTransferDate = command.dateValueOfParameterNamed("partnerTransferDate");
        CodeValue repaymentMode = null;
        final Long repaymentModeId = command.longValueOfParameterNamed(LoanApiConstants.repaymentModeIdParamName);

        if (repaymentModeId != null) {
            repaymentMode = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanRepaymentConstants.REPAYMENT_MODE, repaymentModeId);
        }
        LoanRescheduleRequest loanRescheduleRequest = null;
        this.loanRepaymentScheduleHistoryRepository.createAndSaveLoanScheduleArchive(loan.getRepaymentScheduleInstallments(), loan, loanRescheduleRequest);

        // creating  a coolingOff Loan transaction

        final PaymentDetail paymentDetail = null;
        LoanTransaction coolingOffTransaction = new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.COOLING_OFF, paymentDetail,
                transactionAmount, transactionDate, txnExternalId, LocalDateTime.now(), appUser, BigDecimal.ZERO, BigDecimal.ZERO,
                BusinessEventNotificationConstants.BusinessEvents.COOLING_OFF.getValue(), receiptReferenceNumber, partnerTransferUtr, partnerTransferDate, repaymentMode);
        coolingOffTransaction.setValueDate(CoolingOffUtils.LocalDateToDate(coolingOffDate));

        coolingOff(loan, coolingOffDate, coolingOffTransaction);

        // reset the loan for back dated disbursement making the overDue columns as Zero
        loan.getLoanSummary().resetLoanArrearAgeing();

        // updating the Loan arrear table reversing the backdated Loan Entry
        loanArrearsAgingService.updateLoanArrearsAgeingDetails(loan);

        try {
            this.loanRepositoryWrapper.saveAndFlush(loan);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        LOG.info("Loan : {} Cooling of is Completed", loan.getId());
        return new CommandProcessingResultBuilder().
                withCommandId(command.commandId())
                .withEntityId(loan.getId())
                .build();
    }

    private void checkLoanIsEligibleForCoolingOff(Loan loan, LocalDate coolingOffDate) {

        int days = CoolingOffUtils.getNumberOfDays(loan, coolingOffDate);

        if (Boolean.TRUE.equals(!loan.getLoanProduct().getProductCollectionConfig().getCoolingOffApplicability())) {
            final String errorMessage = "cooling off is not enabled ";
            throw new InvalidLoanTransactionTypeException("Cooling off", "Cooling Off.is.Not.Allowed For This Loan ", errorMessage);
        }

        if (loan.getLoanProduct().getProductCollectionConfig().getCoolingOffThresholdDays() < days) {
            final String errorMessage = " Cooling off is Not Allowed For This Loan because threshold days is more ";
            throw new InvalidLoanTransactionTypeException("Cooling off", "Cooling Off.is.Not.Allowed For This Loan ", errorMessage);
        }
        if (loan.isClosed() || loan.isForeclosed() || loan.isLoanCoolingOff()) {
            final String errorMessage = "Cooling off is Not Allowed for already closed loans ";
            throw new InvalidLoanTransactionTypeException("Cooling off", "Cooling off is Not Allowed for already closed loans", errorMessage);
        }
        if (!loan.isDisbursed()) {
            final String errorMessage = "Loan is not disbursed so unable to do coolingOff";
            throw new InvalidLoanTransactionTypeException("Cooling Off", "Loan is not disbursed so unable to do coolingOff", errorMessage);
        }
        if (loan.getLoanSummary().isFirstInstallmentRepaid()) {
            final String errorMessage = "Loan is not allowed to do coolingOff after repayment";
            throw new InvalidLoanTransactionTypeException("Cooling Off", "Loan is not allowed to do coolingOff after repayment", errorMessage);
        }
    }
    private void coolingOff(Loan loan, LocalDate transactionDate, LoanTransaction loanTransaction) {

        // generating the installment based on the coolingOff scenarios
        LoanRepaymentScheduleInstallment repaymentScheduleInstallment = evaluateTheCoolingOffInterestAndCharges(loan, transactionDate);

        if (loanTransaction.getAmount().doubleValue()!=repaymentScheduleInstallment.getPrincipal().add(repaymentScheduleInstallment.getInterestCharged())
                .add(repaymentScheduleInstallment.getFeeChargesCharged()).doubleValue()) {
            final String errorMessage = " transaction Amount is incorrect it should be " + repaymentScheduleInstallment.getPrincipal().add(repaymentScheduleInstallment.getInterestCharged()).add(repaymentScheduleInstallment.getFeeChargesCharged());
            throw new InvalidLoanTransactionTypeException("coolingOff", "transaction Amount is incorrect", errorMessage);
        }
        loanTransaction.setAmount(repaymentScheduleInstallment.getPrincipal().add(repaymentScheduleInstallment.getInterestCharged())
                .add(repaymentScheduleInstallment.getFeeChargesCharged()));
        loan.updateCoolingOffInstallment(repaymentScheduleInstallment);
        repayLoan(loanTransaction, loanHistoryRepo);

    }

    private void repayLoan(LoanTransaction loanTransaction, LoanHistoryRepo loanHistoryRepo) {

        Loan loan = loanTransaction.getLoan();
        LocalDate transactionDate = loanTransaction.getTransactionDate();
        MonetaryCurrency currency = loanTransaction.getLoan().getCurrency();
        LoanProduct loanProduct = loanTransaction.getLoan().getLoanProduct();

        final LocalDate loanTransactionDate = loanTransaction.getTransactionDate();
        if (loanTransactionDate.isBefore(loan.getDisbursementDate())) {
            final String errorMessage = "The transaction date cannot be before the loan disbursement date: "
                    + loan.getApprovedOnDate().toString();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.before.disbursement.date",
                    errorMessage, loanTransactionDate, loan.getDisbursementDate());
        }

        if (loanTransactionDate.isAfter(DateUtils.getLocalDateOfTenant())) {
            final String errorMessage = "The transaction date cannot be in the future.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.a.future.date",
                    errorMessage, loanTransactionDate);
        }

        loanTransaction.getLoan().getRepaymentScheduleInstallments().forEach(currentInstallment -> {
            Money transactionAmountRemaining = loanTransaction.getAmount(currency);
            if (transactionAmountRemaining.isGreaterThanZero() && !currentInstallment.isObligationsMet()) {

                InterestAppropriationData interestData = CollectionAppropriation.appropriateInterest(currentInstallment, transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, interestData.interest());

                PrincipalAppropriationData principalData = CollectionAppropriation.appropriatePrincipal(currentInstallment, transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, principalData.principal());

                Money feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

                BigDecimal foreClosureSelfShare = ForeclosureUtils.retrieveForeclosureSelfShare(loanProduct.getLoanProductFeesCharges());
                BigDecimal selfFeeChargesPortion = ForeclosureUtils.retrieveSelfForeclosureFeeCharge(feeChargesPortion.getAmount(), foreClosureSelfShare, divisor);
                Money partnerFeeChargesPortion = ForeclosureUtils.retrievePartnerForeclosureFeeCharge(feeChargesPortion, selfFeeChargesPortion);
                currentInstallment.updateForeClosureAmount(feeChargesPortion, selfFeeChargesPortion, partnerFeeChargesPortion.getAmount());

                loanTransaction.updateLoanTransaction(principalData, interestData, loanTransaction.getLoan().getCurrency());
                loanTransaction.feechargesPortion(feeChargesPortion, selfFeeChargesPortion, partnerFeeChargesPortion);

                CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, currentInstallment.getLoan(), transactionDate);

                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,
                        currentInstallment, principalData, interestData, loanTransaction.getFeeChargesPortion(currency), loanTransaction.getPenaltyChargesPortion(currency), transactionAmountRemaining.getAmount());
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);
                currentInstallment.setDue(loanTransaction.getAmount());
                currentInstallment.setSelfDue(loanTransaction.getSelfDue());
                currentInstallment.setPartnerDue(loanTransaction.getPartnerDue());
                loan.addLoanTransaction(loanTransaction);

            }

        });

        // updating the Loan Cycle
        loan.setLoanStatus(720);

        /*calculationg servicer fee interest for cooling off interest*/
        ServicerFeeWritePlatformService.calculateServicerFee(loan.getLoanTransactions(), loan.getLoanProduct(), loan.getCurrency(), loan);
        updateLoanTransaction(loan);
        loan.LoanSummaryDerivedFields();

        updatingLoanPostCoolingOff(loan);
        loan.doPostLoanTransactionChecks(transactionDate, new DefaultLoanLifecycleStateMachine(List.of(LoanStatus.values())));


    }

    private void updatingLoanPostCoolingOff(Loan loan) {

        // making the total outstanding of a Loan to zero  after loan is coolingOff
        loan.getLoanSummary().setTotalOutstanding(BigDecimal.ZERO);
        loan.getLoanSummary().setTotalSelfOutstanding(BigDecimal.ZERO);
        loan.getLoanSummary().setTotalPartnerOutstanding(BigDecimal.ZERO);


        loan.getLoanSummary().setTotalPrincipalRepaid(loan.getLoanSummary().getTotalPrincipalRepaid().add(loan.getLoanSummary().getTotalFeeChargesDueAtDisbursement())
                .add(loan.getLoanSummary().getTotalGstPaid()));
        loan.getLoanSummary().setTotalPrincipalOutstanding(BigDecimal.ZERO);


        loan.getLoanSummary().setTotalSelfPrincipalRepaid(loan.getLoanSummary().getTotalSelfPrincipalRepaid().add(loan.getLoanSummary().getTotalSelfGstDueAtDisbursement())
                .add(loan.getLoanSummary().getTotalSelfGstPaid()));
        loan.getLoanSummary().setTotalSelfPrincipalOutstanding(BigDecimal.ZERO);


        loan.getLoanSummary().setTotalPrincipalOutstanding(BigDecimal.ZERO);
        loan.getLoanSummary().setTotalInterestOutstanding(BigDecimal.ZERO);
        loan.getLoanSummary().setTotalFeeChargesOutstanding(BigDecimal.ZERO);
        loan.getLoanSummary().setTotalOutstanding(BigDecimal.ZERO);

        loan.getLoanTransactions()
                .stream()
                .filter(transaction -> transaction.getTypeOf().isCoolingOff())
                .forEach(trans -> trans.setOutstandingLoanBalance(BigDecimal.ZERO));

        // CoolingOff the loan and setting the principal & principal Outstanding as Total Principal for coolingOff Installment
        if (loan.isLoanCoolingOff()) {
            loan.getRepaymentScheduleInstallments().forEach(installment -> {
                installment.setPrincipal(loan.getPrincpal().getAmount());
                installment.setSelfPrincipal(loan.getPrincpal().getAmount());
                installment.setPrincipalCompleted(loan.getPrincpal().getAmount());
                installment.setPrincipalOutstanding(BigDecimal.ZERO);
                installment.setSelfPrincipalOutstanding(BigDecimal.ZERO);
                installment.setSelfPrincipalCompleted(loan.getPrincpal().getAmount());
            });
        }
    }

    private void updateLoanTransaction(Loan loan) {
        loan.getLoanTransactions()
                .stream()
                .filter(loanTransaction -> loanTransaction.isDisbursement())
                .forEach(loanTransaction -> {

                    loanTransaction.setAmount(loan.getPrincpal().getAmount());
                    loanTransaction.setSelfDue(loan.getSelfPrincipaAmount());
                    loanTransaction.setPartnerDue(loan.getPartnerPrincipalAmount());

                    loanTransaction.setOutstandingLoanBalance(loan.getPrincpal().getAmount());
                    loanTransaction.setSelfOutstandingLoanBalance(loan.getSelfPrincipaAmount());
                    loanTransaction.setPartneroutstandingLoanBalance(loan.getPartnerPrincipalAmount());
                });
    }

    private LoanRepaymentScheduleInstallment evaluateTheCoolingOffInterestAndCharges(Loan loan, LocalDate transactionDate) {

        LoanRepaymentScheduleInstallment repaymentScheduleInstallment = new LoanRepaymentScheduleInstallment(loan);
        switch (CoolingOffInterestAndChargeApplicability.fromInt(loan.getLoanProduct()
                .getProductCollectionConfig().getCoolingOffInterestAndChargeApplicability())) {
            case INVALID -> {
                LOG.error("Cooling Off Interest and Charges Calculation Type Not Selected Correctly");
                break;
            }
            case NO_INTEREST -> {
                BigDecimal coolingOffInterest = BigDecimal.ZERO;
                updateCoolingOffInstallment(repaymentScheduleInstallment, coolingOffInterest);
            }

            case ONLY_INTEREST -> {

                int days = CoolingOffUtils.getNumberOfDays(loan, transactionDate);
                BigDecimal coolingOffInterest = CoolingOffUtils.calculateCoolingOffInterest(loan, days,transactionDate);
                updateCoolingOffInstallment(repaymentScheduleInstallment, coolingOffInterest);
            }
            case INTEREST_AND_CHARGES, ONLY_CHARGES -> {
                // Implementation required
            }
            default -> {
                final String errorMessage = "Cooling Off Interest and Charges Calculation Type in Not Supported select Valid Method";
                throw new InvalidLoanTransactionTypeException("Cooling off", "Cooling Off is Not Allowed", errorMessage);
            }
        }
        repaymentScheduleInstallment.setInstallmentNumber(1);
        repaymentScheduleInstallment.setFromDate(CoolingOffUtils.LocalDateToDate(loan.getDisbursementDate()));
        repaymentScheduleInstallment.setDueDate(CoolingOffUtils.LocalDateToDate(transactionDate));
        repaymentScheduleInstallment.setLoan(loan);

        loan.getLoanSummary().setInterestOverdueDerived(BigDecimal.ZERO);
        loan.getLoanSummary().setPrincipalOverdueDerived(BigDecimal.ZERO);
        loan.getLoanSummary().setTotalOverDueDerived(BigDecimal.ZERO);

        // reversing The LoanCharge and LoanTransaction
        reverseLoanChargeWhileCoolingOff(loan);
        reverseLoanTransactionWhileCoolingOff(loan);
        return repaymentScheduleInstallment;
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    private void reverseLoanChargeWhileCoolingOff(Loan loan) {
        List<LoanCharge> charges = new ArrayList<>();

        loan.getCharges().stream().filter(LoanCharge::isDisbursementCharge).forEach(loanCharge -> {

            loan.getLoanSummary().setCoolingOffReversedChargeAmount(loanCharge.getAmount().add(loanCharge.getTotalGst()));
            LoanCharge charge = new LoanCharge();
            BeanUtils.copyProperties(loanCharge, charge);
            charge.setAmountOutstanding(loanCharge.getAmountOutstanding(loan.getCurrency()).getAmount());
            charge.setChargeCalculation(loanCharge.getChargeCalculation().getValue());
            charge.setChargePaymentMode(loanCharge.getChargePaymentMode().getValue());

            charge.setSelfGstPaid(loanCharge.getSelfGstPaid());
            charge.setPartnerGstPaid(loanCharge.getPartnerGstPaid());
            charge.setSelfGstPercentage(loanCharge.getSelfGstPercentage());
            charge.setPartnerGstPercentage(loanCharge.getPartnerGstPercentage());
            charge.setAmountOrPercentage(loanCharge.getAmountOrPercentage());

            charge.setCreatedDate(loanCharge.getCreatedDate());
            charge.setCreatedUser(loanCharge.getCreatedUser());
            charge.setModifiedUser(getAppUserIfPresent());
            charge.setModifiedDate(CoolingOffUtils.LocalDateToDate(LocalDate.now()));

            //loanCharge.setCoolingOffReversed(true);
            charge.setId(null);
            charge.setCoolingOffReversed(true);
            charges.add(charge);
        });

        loan.getCharges().addAll(charges);
    }

    private void reverseLoanTransactionWhileCoolingOff(Loan loan) {
        List<LoanTransaction> loanTransactions = new ArrayList<>();
        loan.getLoanTransactions().stream()
                .filter(loanTransaction -> loanTransaction.getTypeOf().isRepaymentAtDisbursement())
                .map(loanTransaction -> {
                    LoanTransaction transaction = LoanTransaction.copyTransactionProperties(loanTransaction);
                    transaction.setTypeOf(LoanTransactionType.CHARGE_REVERSAL.getValue());
                    transaction.setReversed(true);
                    transaction.setValueDate(loanTransaction.getValueDate());
                    transaction.setEvent(LoanTransactionType.CHARGE_REVERSAL.toString());
                    transaction.setSelfFeeChargesPortion(loanTransaction.getSelfFeeChargesPortion());
                    transaction.setPartnerFeeChargesPortion(loanTransaction.getPartnerFeeChargesPortion());
                    return transaction;
                })
                .forEach(loanTransactions::add);
        loan.getLoanTransactions().addAll(loanTransactions);

    }

    private void updateCoolingOffInstallment(LoanRepaymentScheduleInstallment repaymentScheduleInstallment, BigDecimal coolingOffInterest) {

        Loan loan = repaymentScheduleInstallment.getLoan();

        BigDecimal totalFeeChargesDueAtDisbursement = loan.getLoanSummary().getTotalFeeChargesDueAtDisbursement();
        BigDecimal totalGstChargesDueAtDisbursement = loan.getLoanSummary().getTotalGstDerived();

        repaymentScheduleInstallment.setInterestCharged(coolingOffInterest);
        repaymentScheduleInstallment.setSelfInterestCharged(CoolingOffUtils.selfCoolingOffInterest(coolingOffInterest, loan.getLoanProduct()));
        repaymentScheduleInstallment.setPartnerInterestCharged(repaymentScheduleInstallment.getInterestCharged().subtract(repaymentScheduleInstallment.getSelfInterestCharged()));

        if (loan.getLoanProduct().getSelfPrincipalShare().doubleValue() == 100) {
            repaymentScheduleInstallment.setPrincipal(loan.getPrincpal().minus(totalFeeChargesDueAtDisbursement).minus(totalGstChargesDueAtDisbursement).getAmount());
            repaymentScheduleInstallment.setSelfPrincipal(repaymentScheduleInstallment.getPrincipal());
            repaymentScheduleInstallment.setPartnerPrincipal(BigDecimal.ZERO);
        } else {
            repaymentScheduleInstallment.setPrincipal(loan.getPrincpal().minus(totalFeeChargesDueAtDisbursement).getAmount());
            repaymentScheduleInstallment.setSelfPrincipal(CoolingOffUtils.selfCoolingOffPrincipal(loan.getPrincpal().getAmount(), loan.getLoanProduct()));
            repaymentScheduleInstallment.setPartnerPrincipal(repaymentScheduleInstallment.getPrincipal().subtract(repaymentScheduleInstallment.getSelfPrincipal(loan.getCurrency()).getAmount()));
        }

        repaymentScheduleInstallment.setPrincipalOutstanding(repaymentScheduleInstallment.getPrincipal());
    }


    @Override
    public BigDecimal getCoolingOffAmount(Long loanId, LocalDate coolingOffDate) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        // checking the CoolingOff For the loan is applicable or not
        checkLoanIsEligibleForCoolingOff(loan, coolingOffDate);
        LoanRepaymentScheduleInstallment repaymentScheduleInstallment = evaluateTheCoolingOffInterestAndCharges(loan, coolingOffDate);
        // returning CoolingOff Amount
        return repaymentScheduleInstallment.getPrincipal().add(repaymentScheduleInstallment.getInterestCharged())
                .add(repaymentScheduleInstallment.getFeeChargesCharged());
    }

}
