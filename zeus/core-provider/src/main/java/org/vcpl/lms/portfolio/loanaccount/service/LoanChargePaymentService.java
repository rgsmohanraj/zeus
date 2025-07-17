package org.vcpl.lms.portfolio.loanaccount.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepository;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.charge.domain.ChargeRepository;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeTransactionRequest;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeAmountFormulaCalculation;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.apache.commons.lang3.compare.ComparableUtils.is;

@Service
@RequiredArgsConstructor
public class LoanChargePaymentService implements LoanChargeActions.Payment {

    private static final Logger LOG = LoggerFactory.getLogger(LoanChargePaymentService.class);

    private final LoanRepository loanRepository;
    private final PlatformSecurityContext context;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final CodeValueRepository codeValueRepository;
    private final ChargeRepository chargeRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final JdbcTemplate jdbcTemplate;
    @Override
    public LoanTransaction pay(final ChargeTransactionRequest chargeTransactionRequest) {
        Loan loan = Objects.nonNull(chargeTransactionRequest.getLoanId())
                ? loanRepository.getReferenceById(chargeTransactionRequest.getLoanId())
                : loanRepository.getByAccountNumber(chargeTransactionRequest.getLoanAccountNo())
                    .orElseThrow(() -> {
                        final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.accountnumber.not.exist",
                            "Loan Account Number does not exist - " + chargeTransactionRequest.getLoanAccountNo(),
                            null, null);
                        return new PlatformApiDataValidationException(List.of(error));
                    });
        Charge charge=chargeRepository.getReferenceById(chargeTransactionRequest.getChargeId());
        Predicate<LoanCharge> loanChargePredicate = deriveLoanChargePredicate(chargeTransactionRequest);
        final BigDecimal[] receivedPayment = {chargeTransactionRequest.getAmount()};
        evalReceivedAmountWithOutstanding(loan, chargeTransactionRequest, loanChargePredicate,charge);

        LoanTransaction chargePaymentTransaction = new LoanTransaction();
        chargePaymentTransaction.setDateOf(DateUtils.convertLocalDateToDate(chargeTransactionRequest.getTransactionDate()));

        payCharges(loan, loanChargePredicate, receivedPayment, chargePaymentTransaction);
        if (Objects.nonNull(chargeTransactionRequest.getPartnerTransferDate()))
            chargePaymentTransaction.setPartnerTransferDate(DateUtils.convertLocalDateToDate(chargeTransactionRequest.getPartnerTransferDate()));

        if (Objects.nonNull(chargeTransactionRequest.getPartnerTransferUtr()))
            chargePaymentTransaction.setPartnerTransferUtr(chargeTransactionRequest.getPartnerTransferUtr());

        if (Objects.nonNull(chargeTransactionRequest.getReceiptReferenceNumber()))
            chargePaymentTransaction.setReceiptReferenceNumber(chargeTransactionRequest.getReceiptReferenceNumber());

        if (Objects.nonNull(chargeTransactionRequest.getRepaymentMode())) {
            CodeValue repaymentCodeValue = codeValueRepository.findByCodeNameAndLabel(LoanApiConstants.REPAYMENTMODE,
                    chargeTransactionRequest.getRepaymentMode());
            chargePaymentTransaction.setRepaymentMode(repaymentCodeValue);
        }
        loan.getLoanTransactions().add(chargePaymentTransaction);

        summarizeRepaymentScheduleForChargesRepaid(loan, chargePaymentTransaction);
        // Summarizing the outstanding at loan level
        loan.getSummary().updateSummary(loan.getCurrency(), loan.getPrincpal(), loan.getRepaymentScheduleInstallments(), loanSummaryWrapper, true,
                loan.charges(), loan.getSelfPrincipaAmount(), loan.getPartnerPrincipalAmount());

        loan.doPostLoanTransactionChecks(chargeTransactionRequest.getTransactionDate(),new DefaultLoanLifecycleStateMachine(List.of(LoanStatus.values())));
        loanRepository.save(loan);
        return chargePaymentTransaction;
    }

    /**
     * Method process loan charge payment with the given loan transaction instead of creating new loan transaction
     *
     * @param chargeTransactionRequest
     * @param existingTransaction
     * @return
     */
    @Override
    public LoanTransaction pay(ChargeTransactionRequest chargeTransactionRequest, LoanTransaction existingTransaction) {
        Loan loan = existingTransaction.getLoan();
        Charge charge=chargeRepository.getReferenceById(chargeTransactionRequest.getChargeId());
        Predicate<LoanCharge> loanChargePredicate = deriveLoanChargePredicate(chargeTransactionRequest);
        final BigDecimal[] receivedPayment = {chargeTransactionRequest.getAmount()};
        evalReceivedAmountWithOutstanding(existingTransaction.getLoan(), chargeTransactionRequest, loanChargePredicate,charge);
        payCharges(loan, loanChargePredicate, receivedPayment, existingTransaction);
        // loan.getLoanTransactions().add(chargePaymentTransaction);
        summarizeRepaymentScheduleForChargesRepaid(loan, existingTransaction);
        // Summarizing the outstanding at loan level
        loan.getSummary().updateSummary(loan.getCurrency(), loan.getPrincpal(), loan.getRepaymentScheduleInstallments(), loanSummaryWrapper, true,
                loan.charges(), loan.getSelfPrincipaAmount(), loan.getPartnerPrincipalAmount());
        return existingTransaction;
    }

    public Predicate<LoanCharge> deriveLoanChargePredicate(ChargeTransactionRequest chargeTransactionRequest) {
        return Objects.nonNull(chargeTransactionRequest.getInstallment())
                ? loanCharge -> (loanCharge.getInstallmentNumber().equals(chargeTransactionRequest.getInstallment())
                    && loanCharge.getCharge().getId().equals(chargeTransactionRequest.getChargeId())
                    && loanCharge.isActive() && !loanCharge.isPaid() && !loanCharge.isWaived())
                : loanCharge -> (loanCharge.getCharge().getId().equals(chargeTransactionRequest.getChargeId())
                && loanCharge.isActive() && !loanCharge.isPaid() && !loanCharge.isWaived());
    }

    public LoanTransaction payCharges(final Loan loan, final Predicate<LoanCharge> loanChargePredicate,
                                       final BigDecimal[] receivedPayment, final LoanTransaction chargePaymentTransaction) {

        if (!chargePaymentTransaction.getTypeOf().isForeClosure() && !chargePaymentTransaction.getTypeOf().isRepaymentType()){
            chargePaymentTransaction.setOutstandingLoanBalance(loan.getSummary().getTotalPrincipalOutstanding());
            chargePaymentTransaction.setSelfOutstandingLoanBalance(loan.getSummary().getTotalSelfPrincipalOutstanding());
        }

        Map<Integer,List<LoanCharge>> loanChargesToBePaid = loan.getLoanCharges().stream()
                .filter(loanChargePredicate)
                .sorted(Comparator.comparingInt(LoanCharge::getInstallmentNumber).thenComparing(LoanCharge::getCreatedDate))
                .collect(Collectors.groupingBy(LoanCharge::getInstallmentNumber));

        for (Map.Entry<Integer,List<LoanCharge>> installmentLoanCharges: loanChargesToBePaid.entrySet()) {
            LoanRepaymentScheduleInstallment installment = loan.fetchRepaymentScheduleInstallment(installmentLoanCharges.getKey());
            if ( (installment.isPrincipalNotCompleted(loan.getCurrency()) || installment.isInterestDue(loan.getCurrency()))
                    && is(receivedPayment[0]).greaterThan(BigDecimal.ZERO)) {
                throw new RuntimeException("Installment: "+installmentLoanCharges.getKey()+ " Not Paid Fully");
            }

            // Exits loop if the received payment is ZERO
            if (is(receivedPayment[0]).equalTo(BigDecimal.ZERO)) break;

            loan.getLoanTransactions().stream()
                    .filter(LoanTransaction::isRepayment)
                    .flatMap(loanTransaction -> loanTransaction.getLoanTransactionToRepaymentScheduleMappings().stream())
                            .filter(rsMapping -> rsMapping.getInstallment().getInstallmentNumber().equals(installmentLoanCharges.getKey()))
                    .max(Comparator.comparing(LoanTransactionToRepaymentScheduleMapping::getId))
                    .ifPresent(latestRsMapping -> {
                        if (chargePaymentTransaction.getTransactionDate().isBefore(latestRsMapping.getLoanTransaction().getTransactionDate())) {
                            throw new RuntimeException("Charge Transaction Date cannot be before " + latestRsMapping.getInstallment().getInstallmentNumber()+ " - Installment Paid Date");
                        }
                    });

            installmentLoanCharges.getValue().forEach(loanCharge -> {
                if (!chargePaymentTransaction.getTypeOf().isForeClosure() && !chargePaymentTransaction.getTypeOf().isRepaymentType()){
                    if(loanCharge.isBounceCharge()) {
                        chargePaymentTransaction.setEvent("BOUNCE_CHARGE_REPAYMENT");
                    } else if (loanCharge.isPenaltyCharge()) {
                        chargePaymentTransaction.setEvent("PENALTY_CHARGE_REPAYMENT");
                    }
                }

                BigDecimal outstanding = loanCharge.getAmountOutstanding(loan.getCurrency()).getAmount();
                BigDecimal gstOutStanding=Objects.nonNull(loanCharge.getGstOutstandingDerived())?loanCharge.getGstOutstandingDerived():BigDecimal.ZERO;
                BigDecimal totalGst=loanCharge.getTotalGst();
                if(is(receivedPayment[0]).greaterThanOrEqualTo(outstanding.add(totalGst))) {
                    chargePaymentTransaction.getLoanChargesPaid().add(new LoanChargePaidBy(chargePaymentTransaction,loanCharge,
                            outstanding.add(gstOutStanding),loanCharge.getInstallmentNumber()));
                    //Gst amount
                    loanCharge.setGstOutstandingDerived(BigDecimal.ZERO);
                    loanCharge.setGstPaidDerived(Objects.nonNull(loanCharge.getGstPaidDerived()) ? loanCharge.getGstPaidDerived().add(gstOutStanding) : gstOutStanding);
                    loanCharge.setSelfGstOutstanding(BigDecimal.ZERO);
                    loanCharge.setSelfGstPaid(loanCharge.getGstPaidDerived());
                    // Charge Amount
                    loanCharge.setAmountOutstanding(BigDecimal.ZERO);
                    loanCharge.setPaid(true);
                    Optional.ofNullable(loanCharge.getAmountPaid())
                            .ifPresentOrElse(amountAlreadyPaid -> loanCharge.setAmountPaid(amountAlreadyPaid.add(outstanding)),
                                    () -> loanCharge.setAmountPaid(outstanding));
                    chargePaymentTransaction.setAmount(Objects.requireNonNullElse(chargePaymentTransaction.getAmount(),BigDecimal.ZERO)
                            .add(outstanding.add(gstOutStanding)));

                    // updateChargePortionBasedOnType(chargePaymentTransaction,loanCharge.isPenaltyCharge(),loanCharge.getCharge().isBounceCharge());
                    updateChargePortionBasedOnType(chargePaymentTransaction,loanCharge);
                    updateSelfPortionOnFullPayment(loanCharge, chargePaymentTransaction);
                    updatePartnerPortionOnFullPayment(loanCharge, chargePaymentTransaction);
                    receivedPayment[0] = receivedPayment[0].subtract(outstanding.add(gstOutStanding));
                    /* servicer fee calculation for Bounce Charge */
                    if(loan.getLoanProduct().getServicerFeeConfig() != null){
                        ServicerFeeAmountFormulaCalculation.calculateServicerFeeForCharge(loan,loan.getLoanCharges(),loan.getPrincpal().getAmount());
                    }
                    LOG.info("Processing Full Payment - ChargeId: {} LoanChargeId: {}",loanCharge.getCharge().getId(),loanCharge.getId());
                } else if(is(receivedPayment[0]).betweenExclusive(BigDecimal.ZERO,outstanding.add(totalGst))) {
                    chargePaymentTransaction.getLoanChargesPaid().add(new LoanChargePaidBy(chargePaymentTransaction,loanCharge,
                            receivedPayment[0],loanCharge.getInstallmentNumber()));
                    BigDecimal actualAmount=receivedPayment[0];
                    if (is(receivedPayment[0]).equalTo(gstOutStanding)) {
                        loanCharge.setGstOutstandingDerived(BigDecimal.ZERO);
                        loanCharge.setGstPaidDerived(Objects.nonNull(loanCharge.getGstPaidDerived()) ? loanCharge.getGstPaidDerived().add(receivedPayment[0]) : receivedPayment[0]);
                        loanCharge.setSelfGstOutstanding(BigDecimal.ZERO);
                        loanCharge.setSelfGstPaid(loanCharge.getGstPaidDerived());
                        receivedPayment[0] = receivedPayment[0].subtract(gstOutStanding).abs();
                    } else if (is(receivedPayment[0]).greaterThan(gstOutStanding) && !is(gstOutStanding).equalTo(BigDecimal.ZERO)) {
                        loanCharge.setGstOutstandingDerived(BigDecimal.ZERO);
                        loanCharge.setGstPaidDerived(Objects.nonNull(loanCharge.getGstPaidDerived()) ? loanCharge.getGstPaidDerived().add(gstOutStanding) : gstOutStanding);
                        loanCharge.setSelfGstOutstanding(BigDecimal.ZERO);
                        loanCharge.setSelfGstPaid(loanCharge.getGstPaidDerived());
                        receivedPayment[0] = receivedPayment[0].subtract(gstOutStanding).abs();
                    } else if (is(receivedPayment[0]).lessThan(gstOutStanding) && !is(gstOutStanding).equalTo(BigDecimal.ZERO)) {
                        loanCharge.setGstOutstandingDerived(gstOutStanding.subtract(receivedPayment[0]));
                        loanCharge.setGstPaidDerived(Objects.nonNull(loanCharge.getGstPaidDerived()) ? loanCharge.getGstPaidDerived().add(receivedPayment[0]) : receivedPayment[0]);
                        loanCharge.setSelfGstOutstanding(gstOutStanding.subtract(receivedPayment[0]));
                        loanCharge.setSelfGstPaid(loanCharge.getGstPaidDerived());
                        receivedPayment[0] = BigDecimal.ZERO;
                    }


                    BigDecimal shortPaidAmount = outstanding.subtract(receivedPayment[0]);
                    loanCharge.setAmountOutstanding(shortPaidAmount);
                    loanCharge.setAmountPaid(Objects.nonNull(loanCharge.getAmountPaid())?loanCharge.getAmountPaid().add( receivedPayment[0]):receivedPayment[0]);
                    chargePaymentTransaction.setAmount(Objects.requireNonNullElse(chargePaymentTransaction.getAmount(),BigDecimal.ZERO)
                            .add(actualAmount));
                    updateChargePortionBasedOnType(chargePaymentTransaction,loanCharge);
                    receivedPayment[0] = updateSelfPortionOnShortPayment(loanCharge, chargePaymentTransaction, receivedPayment[0]);
                    receivedPayment[0] = updatePartnerPortionOnShortPayment(loanCharge, chargePaymentTransaction, receivedPayment[0]);
                    receivedPayment[0] = receivedPayment[0].subtract(outstanding);
                    LOG.info("Processing Short Payment - ChargeId: {} LoanChargeId: {}",loanCharge.getCharge().getId(),loanCharge.getId());
                }
            });
        }
        if ( !chargePaymentTransaction.getTypeOf().isForeClosure() && !chargePaymentTransaction.getTypeOf().isRepaymentType()){
            chargePaymentTransaction.updateChargePayment(loan,context.authenticatedUser());
        }

        return chargePaymentTransaction;
    }

    private void evalReceivedAmountWithOutstanding(final Loan loan, final ChargeTransactionRequest chargeTransactionRequest,
                                                             Predicate<LoanCharge> loanChargePredicate,Charge charge) {
       BigDecimal totalOutstanding= (charge.isBounceCharge())
               ? loan.getSummary().getTotalBounceChargesOutstanding()
               : loan.getSummary().getTotalPenaltyChargesOutstanding();

        if (is(chargeTransactionRequest.getAmount()).greaterThan(totalOutstanding)) {
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.excess.charge.payment",
                    "Received amount cannot be greater than total outstanding",
                    null, null);
            throw new PlatformApiDataValidationException(List.of(error));
        }
    }

    public void summarizeRepaymentScheduleForChargesRepaid(Loan loan, LoanTransaction transaction) {
        // Defined LoanChargeAccumulator Record - For Reducing/Accumulating Penal and Charge Paid
        record LoanChargeAccumulator(BigDecimal penalAmountRepaid,BigDecimal feeChargeAmountRepaid,BigDecimal bounceAmount){}

        BinaryOperator<LoanChargeAccumulator> chargesPaidAmountAccumulator = (lca1, lca2) ->
                new LoanChargeAccumulator(lca1.penalAmountRepaid().add(lca2.penalAmountRepaid()),
                        lca1.feeChargeAmountRepaid().add(lca2.feeChargeAmountRepaid()),
                        lca1.bounceAmount().add(lca2.bounceAmount()));
        Function<LoanCharge,LoanChargeAccumulator> loanChargeLoanChargeAccumulator = loanCharge -> loanCharge.isPenaltyCharge()
                ? new LoanChargeAccumulator(loanCharge.getAmountPaid(), BigDecimal.ZERO,BigDecimal.ZERO)
                : (loanCharge.getCharge().isBounceCharge())? new LoanChargeAccumulator(BigDecimal.ZERO, BigDecimal.ZERO,loanCharge.getAmountPaid().add(Objects.nonNull(loanCharge.getGstPaidDerived())?loanCharge.getGstPaidDerived():BigDecimal.ZERO)): new LoanChargeAccumulator(BigDecimal.ZERO, loanCharge.getAmountPaid(),BigDecimal.ZERO);
        Map<Integer,LoanChargeAccumulator> chargeTransactionMap = loan.getCharges().stream()
                .collect(Collectors.groupingBy(LoanCharge::getInstallmentNumber,
                        // Down Stream - Mapping LoanCharge to LoanChargeAccumulator
                        Collectors.mapping(loanChargeLoanChargeAccumulator,
                                // Down Stream - Reducing/Accumulating LoanChargeAccumulator
                                Collectors.reducing(new LoanChargeAccumulator(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO), chargesPaidAmountAccumulator))));
        Map<Integer,LoanChargeAccumulator> chargePaidByMap = transaction.getLoanChargesPaid().stream()
                .collect(Collectors.groupingBy(LoanChargePaidBy::getInstallmentNumber,
                        // Down Stream - Mapping LoanChargePaidBy to LoanChargeAccumulator
                        Collectors.mapping(loanChargePaidBy -> loanChargePaidBy.getLoanCharge().isPenaltyCharge()
                            ? new LoanChargeAccumulator(loanChargePaidBy.getAmount(), BigDecimal.ZERO,BigDecimal.ZERO)
                            : (loanChargePaidBy.getLoanCharge().getCharge().isBounceCharge())?new LoanChargeAccumulator(BigDecimal.ZERO, BigDecimal.ZERO,loanChargePaidBy.getAmount()) : new LoanChargeAccumulator(BigDecimal.ZERO, loanChargePaidBy.getAmount(),BigDecimal.ZERO),
                                // Down Stream - Reducing/Accumulating LoanChargeAccumulator
                                Collectors.reducing(new LoanChargeAccumulator(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO), chargesPaidAmountAccumulator))));
        LOG.info("Map {} ", chargeTransactionMap);
        loan.getRepaymentScheduleInstallments().forEach(loanRepaymentScheduleInstallment -> {
            if (chargeTransactionMap.containsKey(loanRepaymentScheduleInstallment.getInstallmentNumber())) {
                LoanChargeAccumulator accumulatedAmount = chargeTransactionMap.get(loanRepaymentScheduleInstallment.getInstallmentNumber());
                loanRepaymentScheduleInstallment.setPenaltyChargesPaid(accumulatedAmount.penalAmountRepaid());
              //  loanRepaymentScheduleInstallment.setFeeChargesPaid(accumulatedAmount.feeChargeAmountRepaid());
                loanRepaymentScheduleInstallment.setBounceChargesPaid(accumulatedAmount.bounceAmount());
                loanRepaymentScheduleInstallment.setSelfBounceChargesPaid(accumulatedAmount.bounceAmount);
                if (!loanRepaymentScheduleInstallment.isObligationsMet()) {
                    loanRepaymentScheduleInstallment.checkIfRepaymentPeriodObligationsAreMet(DateUtils.convertDateToLocalDate(transaction.getValueDate()),
                            loanRepaymentScheduleInstallment.getLoan().getCurrency());
                }
            }

            if (chargePaidByMap.containsKey(loanRepaymentScheduleInstallment.getInstallmentNumber())) {
                LoanChargeAccumulator accumulatedAmount = chargePaidByMap.get(loanRepaymentScheduleInstallment.getInstallmentNumber());
                LoanTransactionToRepaymentScheduleMapping repaymentScheduleMapping = createChargesPaymentTransactionMapping(transaction,loanRepaymentScheduleInstallment,
                        accumulatedAmount.feeChargeAmountRepaid(), accumulatedAmount.penalAmountRepaid(),accumulatedAmount.bounceAmount());
                transaction.getLoanTransactionToRepaymentScheduleMappings().add(repaymentScheduleMapping);
            }
        });
    }

    private void updateChargePortionBasedOnType(LoanTransaction chargePaymentTransaction,LoanCharge loanCharge) {
        if (loanCharge.isPenaltyCharge()) {
            BigDecimal penalPaid = Objects.requireNonNullElse(chargePaymentTransaction.getPenaltyChargesPortion(),BigDecimal.ZERO)
                    .add(loanCharge.getAmount().add(loanCharge.getTotalGst()));
            chargePaymentTransaction.setPenaltyChargesPortion(penalPaid);
             return;
        } else if(loanCharge.getCharge().isBounceCharge()) {
            BigDecimal bouncePaid = Objects.requireNonNullElse(chargePaymentTransaction.getBounceChargesPortion(),BigDecimal.ZERO)
                    .add(loanCharge.getAmount().add(loanCharge.getTotalGst()));
            chargePaymentTransaction.setBounceChargesPortion(bouncePaid);
            return;
        }
        BigDecimal feesChargesPaid = Objects.requireNonNullElse(chargePaymentTransaction.getFeeChargesPortion(),BigDecimal.ZERO)
                .add(loanCharge.getAmount().add(loanCharge.getTotalGst()));
        chargePaymentTransaction.setFeeChargesPortion(feesChargesPaid);
    }

    private BigDecimal updatePartnerPortionOnShortPayment(LoanCharge loanCharge, LoanTransaction chargePaymentTransaction, BigDecimal receivedPayment) {
        if (is(receivedPayment).greaterThanOrEqualTo(loanCharge.getPartnerAmountOutstanding())) {
            loanCharge.setPartnerShareAmountRepaid(loanCharge.getPartnerAmountOutstanding());
            updatePartnerTransactionPortion(chargePaymentTransaction,loanCharge.getPartnerAmountOutstanding(),loanCharge);
            receivedPayment = receivedPayment.subtract(loanCharge.getPartnerAmountOutstanding());
            loanCharge.setPartnerAmountOutstanding(BigDecimal.ZERO);
        } else {
            loanCharge.setPartnerShareAmountRepaid(Objects.nonNull(loanCharge.getPartnerShareAmountRepaid())? loanCharge.getPartnerShareAmountRepaid().add(receivedPayment):receivedPayment);
            updatePartnerTransactionPortion(chargePaymentTransaction,receivedPayment,loanCharge);
            loanCharge.setPartnerAmountOutstanding(loanCharge.getPartnerAmountOutstanding().subtract(receivedPayment));
            receivedPayment = BigDecimal.ZERO;
        }
        return receivedPayment;
    }
    private BigDecimal updateSelfPortionOnShortPayment(LoanCharge loanCharge, LoanTransaction chargePaymentTransaction, BigDecimal receivedPayment) {
        if (is(receivedPayment).greaterThanOrEqualTo(loanCharge.getSelfAmountOutstanding())) {
            loanCharge.setSelfShareAmountRepaid(loanCharge.getSelfAmountOutstanding());
            updateSelfTransactionPortion(chargePaymentTransaction,loanCharge.getSelfAmountOutstanding(),loanCharge);
            receivedPayment = receivedPayment.subtract(loanCharge.getSelfAmountOutstanding());
            loanCharge.setSelfAmountOutstanding(BigDecimal.ZERO);
        } else {
            loanCharge.setSelfShareAmountRepaid(Objects.nonNull(loanCharge.getSelfShareAmountRepaid())?loanCharge.getSelfShareAmountRepaid().add(receivedPayment):receivedPayment);
            updateSelfTransactionPortion(chargePaymentTransaction,receivedPayment,loanCharge);
            loanCharge.setSelfAmountOutstanding(loanCharge.getSelfAmountOutstanding().subtract(receivedPayment));
            receivedPayment = BigDecimal.ZERO;
        }
        return receivedPayment;
    }

    private void updatePartnerPortionOnFullPayment(LoanCharge loanCharge, LoanTransaction chargePaymentTransaction) {
        BigDecimal partnerPaidAmount = Objects.isNull(loanCharge.getPartnerShareAmountRepaid())
                ? loanCharge.getPartnerAmountOutstanding()
                : loanCharge.getPartnerShareAmountRepaid().add(loanCharge.getPartnerAmountOutstanding());
        updatePartnerTransactionPortion(chargePaymentTransaction, loanCharge.getPartnerAmountOutstanding(), loanCharge);
        loanCharge.setPartnerShareAmountRepaid(partnerPaidAmount);
        loanCharge.setPartnerAmountOutstanding(BigDecimal.ZERO);
    }

    private void updatePartnerTransactionPortion(LoanTransaction chargePaymentTransaction, BigDecimal partnerAmountPaid, LoanCharge loanCharge) {
        if(loanCharge.isPenaltyCharge()) {
            updatePartnerPenalPaymentTransaction(chargePaymentTransaction,partnerAmountPaid);
        } else if (loanCharge.getCharge().isBounceCharge()) {
            updatePartnerBouncePaymentTransaction(chargePaymentTransaction,partnerAmountPaid);
        } else {
            updatePartnerFeeChargePaymentTransaction(chargePaymentTransaction,partnerAmountPaid);
        }
    }

    private void updateSelfTransactionPortion(LoanTransaction chargePaymentTransaction, BigDecimal selfAmountPaid, LoanCharge loanCharge) {
        if(loanCharge.isPenaltyCharge()) {
            updateSelfPenalPaymentTransaction(chargePaymentTransaction, selfAmountPaid);
        }else if(loanCharge.getCharge().isBounceCharge())
        {
            updateSelfBouncePaymentTransaction(chargePaymentTransaction, selfAmountPaid);
        }
        else {
            updateSelfFeeChargePaymentTransaction(chargePaymentTransaction, selfAmountPaid);
        }
    }
    private void updatePartnerPenalPaymentTransaction(LoanTransaction chargePaymentTransaction, BigDecimal partnerAmountOutstanding) {
        chargePaymentTransaction.setPartnerPenaltyChargesPortion(Objects
                .requireNonNullElse(chargePaymentTransaction.getPartnerPenaltyChargesPortion(),BigDecimal.ZERO)
                .add(partnerAmountOutstanding));
        chargePaymentTransaction.setPartnerDue(chargePaymentTransaction.getPartnerPenaltyChargesPortion());
    }
    private void updatePartnerBouncePaymentTransaction(LoanTransaction chargePaymentTransaction, BigDecimal partnerAmountOutstanding) {
        chargePaymentTransaction.setPartnerBounceChargesPortion(Objects
                .requireNonNullElse(chargePaymentTransaction.getPartnerBounceChargesPortion(),BigDecimal.ZERO)
                .add(partnerAmountOutstanding));
        chargePaymentTransaction.setPartnerDue(chargePaymentTransaction.getPartnerBounceChargesPortion());
    }

    private void updatePartnerFeeChargePaymentTransaction(LoanTransaction chargePaymentTransaction, BigDecimal partnerAmountOutstanding) {
        chargePaymentTransaction.setPartnerFeeChargesPortion(Objects
                .requireNonNullElse(chargePaymentTransaction.getPartnerFeeChargesPortion(),BigDecimal.ZERO)
                .add(partnerAmountOutstanding));
        chargePaymentTransaction.setPartnerDue(chargePaymentTransaction.getPartnerFeeChargesPortion());
    }

    private void updateSelfPortionOnFullPayment(LoanCharge loanCharge, LoanTransaction chargePaymentTransaction) {

        BigDecimal selfPaidAmount = Objects.isNull(loanCharge.getSelfShareAmountRepaid())
                ? loanCharge.getSelfAmountOutstanding().add(loanCharge.getSelfGst())
                : loanCharge.getSelfShareAmountRepaid().add(loanCharge.getSelfAmountOutstanding().add(loanCharge.getSelfGst()));
        updateSelfTransactionPortion(chargePaymentTransaction,selfPaidAmount,loanCharge);
        LOG.info("Self Paid Amount {}",selfPaidAmount);
        loanCharge.setSelfShareAmountRepaid(selfPaidAmount);
        loanCharge.setSelfAmountOutstanding(BigDecimal.ZERO);
    }

    private void updateSelfPenalPaymentTransaction(LoanTransaction chargePaymentTransaction, BigDecimal selfAmountOutstanding) {
        chargePaymentTransaction.setSelfPenaltyChargesPortion(Objects
                .requireNonNullElse(chargePaymentTransaction.getSelfPenaltyChargesPortion(),BigDecimal.ZERO)
                .add(selfAmountOutstanding));
        chargePaymentTransaction.setSelfDue(chargePaymentTransaction.getSelfPenaltyChargesPortion());
    }
    private void updateSelfBouncePaymentTransaction(LoanTransaction chargePaymentTransaction, BigDecimal selfAmountOutstanding) {
        chargePaymentTransaction.setSelfBounceChargesPortion(Objects
                .requireNonNullElse(chargePaymentTransaction.getSelfBounceChargesPortion(),BigDecimal.ZERO)
                .add(selfAmountOutstanding));
        chargePaymentTransaction.setSelfDue(chargePaymentTransaction.getSelfBounceChargesPortion());
    }

    private void updateSelfFeeChargePaymentTransaction(LoanTransaction chargePaymentTransaction, BigDecimal selfAmountOutstanding) {
        chargePaymentTransaction.setSelfFeeChargesPortion(Objects
                .requireNonNullElse(chargePaymentTransaction.getSelfFeeChargesPortion(),BigDecimal.ZERO)
                .add(selfAmountOutstanding));
        chargePaymentTransaction.setSelfDue(chargePaymentTransaction.getSelfFeeChargesPortion());
    }

    private LoanTransactionToRepaymentScheduleMapping createChargesPaymentTransactionMapping(final LoanTransaction loanTransaction, final LoanRepaymentScheduleInstallment installment,
                                                                                           final BigDecimal feeChargesAmount,final BigDecimal penalAmount,final BigDecimal bounceAmount){
        MonetaryCurrency currency = loanTransaction.getLoan().getCurrency();
        return LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, installment, Money.zero(currency),Money.zero(currency),
                Money.of(currency, feeChargesAmount), Money.of(currency, penalAmount),Money.zero(currency),Money.zero(currency),Money.zero(currency),
                Money.zero(currency),BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,Money.of(currency, bounceAmount));
    }
}
