package org.vcpl.lms.portfolio.loanaccount.foreclosure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.collection.data.InterestAppropriationData;
import org.vcpl.lms.portfolio.collection.data.PrincipalAppropriationData;
import org.vcpl.lms.portfolio.collection.service.CollectionAppropriation;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeTransactionRequest;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransaction;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.domain.xirr.XirrService;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.vcpl.lms.portfolio.loanaccount.service.LoanChargeActions;
import org.vcpl.lms.portfolio.loanaccount.service.LoanChargePaymentService;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeWritePlatformService;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForeclosureService implements Foreclosure {
    private static final String LOCALE = "en";
    private static final String DATE_FORMATE = "dd MMMM yyyy";
    private final LoanChargePaymentService loanChargePaymentService;

    @Autowired
    public ForeclosureService(LoanChargePaymentService loanChargePaymentService) {
        this.loanChargePaymentService = loanChargePaymentService;
    }

    private BigDecimal getDivisor() {
        return BigDecimal.valueOf(100);
    }

    @Override
    public void foreclosureAppropriation(List<LoanRepaymentScheduleInstallment> installments, Loan loan, LoanTransaction loanTransaction,
                                         LocalDate transactionDate, LoanHistoryRepo loanHistoryRepo, LoanProduct loanProduct) {

        // validating The Transaction Date Before Appropriation
        loan.validateForForeclosure(loanTransaction.getTransactionDate());

        if (loanTransaction.isRecoveryRepayment()
                && loanTransaction.getAmount(loan.getCurrency()).getAmount().compareTo(loan.getSummary().getTotalWrittenOff()) > 0) {
            final String errorMessage = "The transaction amount cannot greater than the remaining written off amount.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.written.off", errorMessage);
        }

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
        List<LoanTransaction> loanTransactions = new ArrayList<>();
        // Retrieving the Advance LoanTransaction
        ForeclosureUtils.getAdvanceAmountForTransaction(loan, loanTransactions);

        loanTransactions.add(loanTransaction);

        /* Adding LoanChargePaidBy for Foreclosure Charge */
        ForeclosureUtils.addLoanChargesPaidBy(loanTransaction, loan);

        LOG.debug("LOAN FORECLOSURE APPROPRIATION STARTED ");

        loanTransactions.forEach(transaction -> makeForeclosure(transaction, loan, installments, loanProduct, transactionDate, loanHistoryRepo));

        final List<LoanTransaction> transactionsPostDisbursement = loan.retreiveListOfTransactionsPostDisbursement();
        loanTransaction.setTypeOf(2);
        transactionsPostDisbursement.add(loanTransaction);// adding current Loan Transaction for Calculation
        // Calculating The Servicer Fee
        ServicerFeeWritePlatformService.calculateServicerFee(transactionsPostDisbursement, loanProduct, loan.getCurrency(), loan);
        // calculating the Xirr value
        loan.updateXirrValue(XirrService.xirrCalculation(loan.getRepaymentScheduleInstallments(), loan.getPrincpal().getAmount(), loan.getDisbursementDate(),
                loan.getCurrency(), loan.getLoanCharges(), transactionsPostDisbursement, loan));

        loan.LoanSummaryDerivedFields();
        loan.doPostLoanTransactionChecksAfterForeclosed(transactionDate, loan.getLoanLifecycleStateMachine());

        LOG.debug("LOAN FORECLOSED AND UPDATE IN THE LOAN AND TRANSACTION TABLE");

    }

    private void makeForeclosure(LoanTransaction loanTransaction, Loan loan, List<LoanRepaymentScheduleInstallment> installment,
                                 LoanProduct loanProduct, LocalDate transactionDate, LoanHistoryRepo loanHistoryRepo) {

        MonetaryCurrency currency = loan.getCurrency();
        Money transactionAmountRemaining = loanTransaction.getAmount(currency);

        Money feeChargesPortion = Money.zero(currency);
        BigDecimal selfFeeChargesPortion = BigDecimal.ZERO;
        Money partnerFeeChargesPortion = Money.zero(currency);

        Money penaltyChargesPortion = Money.zero(currency);

        for (LoanRepaymentScheduleInstallment currentInstallment : installment) {
            if (!currentInstallment.isObligationsMet() && transactionAmountRemaining.isGreaterThanZero()) {
                // Handling The Foreclosure Advance scenario For Previously Paid Adavance Amount
                // As of Now It will Appropriate Only PrincipalPortion of The Installment
                if (loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.toString())) {
                    LOG.info("ADVANCE LOAN TRANSACTION APPROPRIATION STARTED ");
                    transactionAmountRemaining = appropriationLogicForForeclosureWithAdvanceAmount(transactionAmountRemaining, loanTransaction, currentInstallment, transactionDate, loan,
                            feeChargesPortion, loanHistoryRepo, loanProduct, currency);
                }
                if (transactionAmountRemaining.isZero()) {
                    break;
                }

                    InterestAppropriationData interestAppropriationData = CollectionAppropriation.appropriateInterest(currentInstallment, transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(interestAppropriationData.interest());

                PrincipalAppropriationData principalAppropriationData = CollectionAppropriation.appropriatePrincipal(currentInstallment, transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(principalAppropriationData.principal());

                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

                currentInstallment.reverseDaysPastDueOnInterestPrincipleCompletion(transactionDate);

                // Appropriating The Foreclosure Charges

                BigDecimal foreClosureSelfShare = ForeclosureUtils.retrieveForeclosureSelfShare(loanProduct.getLoanProductFeesCharges());

                selfFeeChargesPortion = ForeclosureUtils.retrieveSelfForeclosureFeeCharge(feeChargesPortion.getAmount(), foreClosureSelfShare, getDivisor());

                partnerFeeChargesPortion = ForeclosureUtils.retrievePartnerForeclosureFeeCharge(feeChargesPortion, selfFeeChargesPortion);

                currentInstallment.updateForeClosureAmount(feeChargesPortion, selfFeeChargesPortion, partnerFeeChargesPortion.getAmount());

                partnerFeeChargesPortion = ForeclosureUtils.retrievePartnerForeclosureFeeCharge(feeChargesPortion, selfFeeChargesPortion);

                loanTransaction.feechargesPortion(feeChargesPortion, selfFeeChargesPortion, partnerFeeChargesPortion);

                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment, principalAppropriationData, interestAppropriationData, feeChargesPortion,
                        penaltyChargesPortion, BigDecimal.ZERO);
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);
                loanTransaction.setValueDate(loanTransaction.getDateOf());

                // calling Pay and reverse function at last installment of foreclosure
                if (currentInstallment.isLastInstallment(loan.getRepaymentScheduleInstallments().size(), currentInstallment.getInstallmentNumber())
                        && isBounceChargeIsApplicable(loan) && getRemainingBounceChargeAmountToPay(loan).doubleValue() > 0) {
                    payBounceCharge(loan, loanTransaction, transactionAmountRemaining, currentInstallment, transactionDate);
                }

                loanTransaction.updateLoanTransaction(principalAppropriationData, interestAppropriationData, currency);

                CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, loan, transactionDate);
                List<LoanTransaction> loanTransactions = new ArrayList<>();
                removingTheLoanTransactionForBackDateAdvance(transactionDate, loan, currentInstallment, loanTransactions);
                if (!loanTransactions.isEmpty()) {
                    loanTransactions.forEach(transaction -> loanTransaction.getLoan().getLoanTransactions().add(transaction));
                }
                if (Boolean.TRUE.equals(currentInstallment.isLastInstallment(loan.getNumberOfRepayments(), currentInstallment.getInstallmentNumber()) &&
                        loan.getLoanSummary().getTotalOutstanding().doubleValue() == 0) && transactionAmountRemaining.getAmount().doubleValue() > 0) {
                    final String errorMessage = "The transaction amount " + loan.getLoanSummary().getTotalOutstanding() + " cannot be greater than the total outstanding amount " + loanTransaction.getAmount();
                    throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.outstanding.amount {}", errorMessage);
                }
            }
        }
    }

    private void getChargeTransactionRequest(Loan loan, LoanTransaction loanTransaction, BigDecimal transactionAmountRemaining) {

        ChargeTransactionRequest chargeTransactionRequest = new ChargeTransactionRequest();

        chargeTransactionRequest.setLoanId(loan.getId());
        chargeTransactionRequest.setLoanAccountNo(loan.getAccountNumber());
        chargeTransactionRequest.setExternalId(loan.getExternalId());
        Long chargeid = ForeclosureUtils.getBounceCharge(loan);
        chargeTransactionRequest.setChargeId(chargeid);
        chargeTransactionRequest.setAmount(transactionAmountRemaining);
        chargeTransactionRequest.setTransactionDate(loanTransaction.getTransactionDate());
        chargeTransactionRequest.setInstallment(null);
        chargeTransactionRequest.setPartnerTransferDate(ForeclosureUtils.convert(loanTransaction.getPartnerTransferDate()));
        chargeTransactionRequest.setDateFormat(DATE_FORMATE);
        chargeTransactionRequest.setLocale(LOCALE);
        // calling pay Function to Pay Bounce Charge While Foreclosure
        loanChargePaymentService.pay(chargeTransactionRequest,loanTransaction);
    }


    private Money payBounceCharge(Loan loan, LoanTransaction loanTransaction, Money transactionAmountRemaining, LoanRepaymentScheduleInstallment currentInstallment, LocalDate transactionDate) {

        LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loan.getRepaymentScheduleInstallments()
                .get(loan.getLoanRepaymentScheduleInstallmentsSize() - 1);
        BigDecimal bounceChargeAmount = ForeclosureUtils.getBounceCharge(loan, loanRepaymentScheduleInstallment.getInstallmentNumber()-1);
        getChargeTransactionRequest(loan, loanTransaction, bounceChargeAmount);
        transactionAmountRemaining = transactionAmountRemaining.minus(bounceChargeAmount);
        LoanChargeActions.reverseChargesOnRepayment(currentInstallment, transactionDate);
        return transactionAmountRemaining;
    }

    private BigDecimal getRemainingBounceChargeAmountToPay(Loan loan) {

        return loan.getRepaymentScheduleInstallments()
                .stream()
                .filter(installment -> installment.getBounceCharges().doubleValue() > 0
                        && installment.getBounceChargesCharged().subtract(installment.getBounceChargesPaid()).doubleValue()>0)
                .map(LoanRepaymentScheduleInstallment::getBounceCharges)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private Boolean isBounceChargeIsApplicable(Loan loan) {

        return loan.getLoanProduct()
                .getLoanProductFeesCharges()
                .stream()
                .anyMatch(productFeesCharges -> productFeesCharges.getCharge().isBounceCharge());
    }
}
