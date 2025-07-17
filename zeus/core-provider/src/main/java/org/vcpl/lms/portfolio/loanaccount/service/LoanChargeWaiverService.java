package org.vcpl.lms.portfolio.loanaccount.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.compare.ComparableUtils.is;

@Service
@RequiredArgsConstructor
public class LoanChargeWaiverService implements LoanChargeActions.Waiver {

    private static final Logger LOG = LoggerFactory.getLogger(LoanChargeWaiverService.class);

    private final LoanRepository loanRepository;
    private final PlatformSecurityContext context;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final CodeValueRepository codeValueRepository;
    private final ChargeRepository chargeRepository;

    @Override
    public LoanTransaction waive(final ChargeTransactionRequest chargeTransactionRequest) {
        Loan loan = loanRepository.getByAccountNumber(chargeTransactionRequest.getLoanAccountNo())
                .orElseThrow(() -> {
                    final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.accountnumber.not.exist",
                            "Loan Account Number does not exist - " + chargeTransactionRequest.getLoanAccountNo(),
                            null, null);
                    return new PlatformApiDataValidationException(List.of(error));
                });
        Charge charge=chargeRepository.getReferenceById(chargeTransactionRequest.getChargeId());
        Predicate<LoanCharge> loanChargePredicate = deriveLoanChargePredicate(chargeTransactionRequest);
        final BigDecimal[] waiveAmount = {chargeTransactionRequest.getAmount()};
        evalReceivedAmountWithOutstanding(loan, chargeTransactionRequest, loanChargePredicate,charge);

        LoanTransaction loanWaiverTransaction = new LoanTransaction();
        loanWaiverTransaction.setDateOf(DateUtils.convertLocalDateToDate(chargeTransactionRequest.getTransactionDate()));

        LoanTransaction chargeWaiverTransaction = waiveCharges(loan, loanChargePredicate, waiveAmount, loanWaiverTransaction);


        if (Objects.nonNull(chargeTransactionRequest.getPartnerTransferDate()))
            chargeWaiverTransaction.setPartnerTransferDate(DateUtils.convertLocalDateToDate(chargeTransactionRequest.getPartnerTransferDate()));

        if (Objects.nonNull(chargeTransactionRequest.getPartnerTransferUtr()))
            chargeWaiverTransaction.setPartnerTransferUtr(chargeTransactionRequest.getPartnerTransferUtr());

        if (Objects.nonNull(chargeTransactionRequest.getReceiptReferenceNumber()))
            chargeWaiverTransaction.setReceiptReferenceNumber(chargeTransactionRequest.getReceiptReferenceNumber());

        if (Objects.nonNull(chargeTransactionRequest.getRepaymentMode())) {
            CodeValue repaymentCodeValue = codeValueRepository.findByCodeNameAndLabel(LoanApiConstants.REPAYMENTMODE,
                    chargeTransactionRequest.getRepaymentMode());
            chargeWaiverTransaction.setRepaymentMode(repaymentCodeValue);
        }
        loan.getLoanTransactions().add(chargeWaiverTransaction);
        summarizeRepaymentScheduleForChargesWaived(loan, chargeWaiverTransaction);
        // Summarizing the outstanding at loan level
        loan.getSummary().updateSummary(loan.getCurrency(), loan.getPrincpal(), loan.getRepaymentScheduleInstallments(), loanSummaryWrapper, true,
                loan.charges(), loan.getSelfPrincipaAmount(), loan.getPartnerPrincipalAmount());

        loan.doPostLoanTransactionChecks(chargeTransactionRequest.getTransactionDate(),new DefaultLoanLifecycleStateMachine(List.of(LoanStatus.values())));
        loanRepository.save(loan);
        return chargeWaiverTransaction;
    }

    @Override
    public LoanTransaction waive(ChargeTransactionRequest chargeTransactionRequest, LoanTransaction existingTransaction) {
        Loan loan = existingTransaction.getLoan();
        Charge charge=chargeRepository.getReferenceById(chargeTransactionRequest.getChargeId());
        Predicate<LoanCharge> loanChargePredicate = deriveLoanChargePredicate(chargeTransactionRequest);
        final BigDecimal[] receivedPayment = {chargeTransactionRequest.getAmount()};
        evalReceivedAmountWithOutstanding(existingTransaction.getLoan(), chargeTransactionRequest, loanChargePredicate,charge);
        waiveCharges(loan, loanChargePredicate, receivedPayment, existingTransaction);
        // loan.getLoanTransactions().add(chargePaymentTransaction);
        summarizeRepaymentScheduleForChargesWaived(loan, existingTransaction);
        // Summarizing the outstanding at loan level
        loan.getSummary().updateSummary(loan.getCurrency(), loan.getPrincpal(), loan.getRepaymentScheduleInstallments(), loanSummaryWrapper, true,
                loan.charges(), loan.getSelfPrincipaAmount(), loan.getPartnerPrincipalAmount());
        return existingTransaction;
    }

    private Predicate<LoanCharge> deriveLoanChargePredicate(ChargeTransactionRequest chargeTransactionRequest) {
        return Objects.nonNull(chargeTransactionRequest.getInstallment())
                ? loanCharge -> (loanCharge.getInstallmentNumber().equals(chargeTransactionRequest.getInstallment())
                && loanCharge.getCharge().getId().equals(chargeTransactionRequest.getChargeId())
                && loanCharge.isActive() && !loanCharge.isPaid() && !loanCharge.isWaived())
                : loanCharge -> (loanCharge.getCharge().getId().equals(chargeTransactionRequest.getChargeId())
                && loanCharge.isActive() && !loanCharge.isPaid() && !loanCharge.isWaived());
    }

    private void evalReceivedAmountWithOutstanding(final Loan loan, final ChargeTransactionRequest chargeTransactionRequest,
                                                   Predicate<LoanCharge> loanChargePredicate,Charge charge) {

//        BigDecimal totalOutstanding = loan.getLoanCharges().stream()
//                .filter(loanChargePredicate)
//                .map(loanCharge -> loanCharge.getAmountOutstanding(loan.getCurrency()).getAmount())
//                .reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal totalOutstanding= (charge.isBounceCharge())?loan.getSummary().getTotalBounceChargesOutstanding():loan.getSummary().getTotalPenaltyChargesOutstanding();

        if(is(chargeTransactionRequest.getAmount()).greaterThan(totalOutstanding)) {
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.excess.charge.waiver",
                    "Received amount cannot be greater than total outstanding",
                    null, null);
            throw new PlatformApiDataValidationException(List.of(error));
        }
    }

    private LoanTransaction waiveCharges(final Loan loan, final Predicate<LoanCharge> loanChargePredicate,
                                      final BigDecimal[] waiveAmount, final LoanTransaction chargeWaiverTransaction) {
        chargeWaiverTransaction.setOutstandingLoanBalance(loan.getSummary().getTotalPrincipalOutstanding());
        chargeWaiverTransaction.setSelfOutstandingLoanBalance(loan.getSummary().getTotalSelfPrincipalOutstanding());
        Map<Integer,List<LoanCharge>> loanChargesToBePaid = loan.getLoanCharges().stream()
                .filter(loanChargePredicate)
                .sorted(Comparator.comparingInt(LoanCharge::getInstallmentNumber).thenComparing(LoanCharge::getCreatedDate))
                .collect(Collectors.groupingBy(LoanCharge::getInstallmentNumber));

        for (Map.Entry<Integer,List<LoanCharge>> installmentLoanCharges: loanChargesToBePaid.entrySet()) {
            LoanRepaymentScheduleInstallment installment = loan.fetchRepaymentScheduleInstallment(installmentLoanCharges.getKey());
            if ((installment.isPrincipalNotCompleted(loan.getCurrency()) || installment.isInterestDue(loan.getCurrency())) && !(is(waiveAmount[0]).equalTo(BigDecimal.ZERO))) {
                throw new RuntimeException("Installment:" + installmentLoanCharges.getKey() + " is not paid fully");
            }

            // Exits loop if the received payment is ZERO
            if (is(waiveAmount[0]).equalTo(BigDecimal.ZERO)) break;

            loan.getLoanTransactions().stream()
                    .filter(LoanTransaction::isRepayment)
                    .flatMap(loanTransaction -> loanTransaction.getLoanTransactionToRepaymentScheduleMappings().stream())
                    .filter(rsMapping -> rsMapping.getInstallment().getInstallmentNumber().equals(installmentLoanCharges.getKey()))
                    .max(Comparator.comparing(LoanTransactionToRepaymentScheduleMapping::getId))
                    .ifPresent(latestRsMapping -> {
                        if (chargeWaiverTransaction.getTransactionDate().isBefore(latestRsMapping.getLoanTransaction().getTransactionDate())) {
                            throw new RuntimeException("Charge Transaction Date cannot be before " + latestRsMapping.getInstallment().getInstallmentNumber()+ " - Installment Paid Date");
                        }
                    });
            installmentLoanCharges.getValue().forEach(loanCharge -> {
                if (!chargeWaiverTransaction.getTypeOf().isForeClosure() && !chargeWaiverTransaction.getTypeOf().isRepaymentType()){
                    if(loanCharge.isBounceCharge()) {
                        chargeWaiverTransaction.setEvent("BOUNCE_CHARGE_REPAYMENT");
                    } else if (loanCharge.isPenaltyCharge()) {
                        chargeWaiverTransaction.setEvent("PENALTY_CHARGE_REPAYMENT");
                    }
                }

                BigDecimal outstanding = loanCharge.getAmountOutstanding(loan.getCurrency()).getAmount();
                BigDecimal gstOutstanding = Objects.requireNonNullElse(loanCharge.getGstOutstandingDerived(),BigDecimal.ZERO);
                if(is(waiveAmount[0]).greaterThanOrEqualTo(outstanding.add(gstOutstanding))) {
                    chargeWaiverTransaction.getLoanChargesPaid().add(new LoanChargePaidBy(chargeWaiverTransaction,loanCharge,
                            outstanding,loanCharge.getInstallmentNumber()));
                    // Charge Amount
                    loanCharge.setAmountOutstanding(BigDecimal.ZERO);

                    loanCharge.setGstOutstandingDerived(BigDecimal.ZERO);
                    loanCharge.setSelfGstOutstanding(BigDecimal.ZERO);
                    loanCharge.setPartnerGstOutstanding(BigDecimal.ZERO);
                    loanCharge.setGstWaivedOffDerived(gstOutstanding);


                    loanCharge.setWaived(true);
                    Optional.ofNullable(loanCharge.getAmountWaived())
                            .ifPresentOrElse(amountAlreadyPaid -> loanCharge.setAmountWaived(amountAlreadyPaid.add(outstanding)),
                                    () -> loanCharge.setAmountWaived(outstanding));
                    chargeWaiverTransaction.setAmount(Objects.requireNonNullElse(chargeWaiverTransaction.getAmount(),BigDecimal.ZERO)
                            .add(outstanding.add(gstOutstanding)));
                    updateChargePortionBasedOnType(chargeWaiverTransaction,loanCharge);
                    updateSelfPortionOnFullWaiver(loanCharge, chargeWaiverTransaction);
                    updatePartnerPortionOnFullWaived(loanCharge, chargeWaiverTransaction);

                    waiveAmount[0] = waiveAmount[0].subtract(outstanding.add(gstOutstanding));
                } else if(is(waiveAmount[0]).betweenExclusive(BigDecimal.ZERO,outstanding)) {
                    chargeWaiverTransaction.getLoanChargesPaid().add(new LoanChargePaidBy(chargeWaiverTransaction,loanCharge,
                            waiveAmount[0],loanCharge.getInstallmentNumber()));
                    BigDecimal actualAmount=waiveAmount[0];
                    if(loanCharge.isGstEnabled() || true) {
                        if (is(waiveAmount[0]).equalTo(gstOutstanding)) {
                            loanCharge.setGstOutstandingDerived(BigDecimal.ZERO);
                            loanCharge.setGstWaivedOffDerived(Objects.nonNull(loanCharge.getGstWaivedOffDerived()) ? loanCharge.getGstWaivedOffDerived().add(waiveAmount[0]) : waiveAmount[0]);
                            loanCharge.setSelfGstOutstanding(BigDecimal.ZERO);
                            loanCharge.setSelfGstWaivedOff(Objects.nonNull(loanCharge.getSelfGstWaivedOff()) ? loanCharge.getSelfGstWaivedOff().add(waiveAmount[0]) : waiveAmount[0]);
                            waiveAmount[0] = waiveAmount[0].subtract(gstOutstanding).abs();

                        } else if (is(waiveAmount[0]).greaterThan(gstOutstanding) && !is(gstOutstanding).equalTo(BigDecimal.ZERO)) {
                            loanCharge.setGstOutstandingDerived(BigDecimal.ZERO);
                            loanCharge.setGstWaivedOffDerived(Objects.nonNull(loanCharge.getGstWaivedOffDerived()) ? loanCharge.getGstWaivedOffDerived().add(gstOutstanding) : gstOutstanding);
                            loanCharge.setSelfGstOutstanding(BigDecimal.ZERO);
                            loanCharge.setSelfGstWaivedOff(Objects.nonNull(loanCharge.getSelfGstWaivedOff()) ? loanCharge.getSelfGstWaivedOff().add(gstOutstanding) : gstOutstanding);

                            waiveAmount[0] = waiveAmount[0].subtract(gstOutstanding).abs();
                        } else if (is(waiveAmount[0]).lessThan(gstOutstanding) && !is(gstOutstanding).equalTo(BigDecimal.ZERO)) {
                            loanCharge.setGstOutstandingDerived(gstOutstanding.subtract(waiveAmount[0]));
                            loanCharge.setGstWaivedOffDerived(Objects.nonNull(loanCharge.getGstWaivedOffDerived()) ? loanCharge.getGstWaivedOffDerived().add(waiveAmount[0]) : waiveAmount[0]);
                            loanCharge.setSelfGstOutstanding(gstOutstanding.subtract(waiveAmount[0]));
                            loanCharge.setSelfGstWaivedOff(Objects.nonNull(loanCharge.getSelfGstWaivedOff()) ? loanCharge.getSelfGstWaivedOff().add(waiveAmount[0]) : waiveAmount[0]);

                            waiveAmount[0] = BigDecimal.ZERO;
                        }
                    }
                    BigDecimal shortPaidAmount = outstanding.subtract(waiveAmount[0]);
                    loanCharge.setAmountOutstanding(shortPaidAmount);
                    loanCharge.setAmountWaived(waiveAmount[0]);
                    chargeWaiverTransaction.setAmount(Objects.requireNonNullElse(chargeWaiverTransaction.getAmount(),BigDecimal.ZERO)
                            .add(actualAmount));
                    updateChargePortionBasedOnType(chargeWaiverTransaction,loanCharge);
                    waiveAmount[0] = updateSelfPortionOnShortWaiver(loanCharge, chargeWaiverTransaction, waiveAmount[0]);
                    waiveAmount[0] = updatePartnerPortionOnShortWaiver(loanCharge, chargeWaiverTransaction, waiveAmount[0]);
                    waiveAmount[0] = waiveAmount[0].subtract(outstanding);
                }
            });
        }
        if ( !chargeWaiverTransaction.getTypeOf().isForeClosure() && !chargeWaiverTransaction.getTypeOf().isRepaymentType()) {
            chargeWaiverTransaction.updateChargeWaiver(loan, context.authenticatedUser(), chargeWaiverTransaction.getTransactionDate());
        }
        return chargeWaiverTransaction;
    }

    private void updateChargePortionBasedOnType(LoanTransaction chargeWaiverTransaction, LoanCharge loanCharge) {
        if (loanCharge.isPenaltyCharge()) {
            chargeWaiverTransaction.setPenaltyChargesPortion(chargeWaiverTransaction.getAmount());
            return;
        } else if (loanCharge.getCharge().isBounceCharge()) {
            chargeWaiverTransaction.setBounceChargesPortion(chargeWaiverTransaction.getAmount());
            return;
        }
        chargeWaiverTransaction.setFeeChargesPortion(chargeWaiverTransaction.getAmount());
    }

    private void updateSelfPortionOnFullWaiver(LoanCharge loanCharge, LoanTransaction chargeWaiverTransaction) {
        BigDecimal selfWaivedAmount = Objects.isNull(loanCharge.getSelfAmountWaived())
                ? loanCharge.getSelfAmountOutstanding().add(loanCharge.getSelfGst())
                : loanCharge.getSelfAmountWaived().add(loanCharge.getSelfAmountOutstanding().add(loanCharge.getSelfGst()));
        updateSelfTransactionPortion(chargeWaiverTransaction,loanCharge.getSelfAmountOutstanding().add(loanCharge.getSelfGst()),loanCharge);
        LOG.info("Self Waived Amount {}",selfWaivedAmount);
        loanCharge.setSelfAmountWaived(selfWaivedAmount);
        loanCharge.setSelfAmountOutstanding(BigDecimal.ZERO);
    }

    private void updateSelfTransactionPortion(LoanTransaction chargeWaiverTransaction, BigDecimal selfAmountWaived, LoanCharge loanCharge) {
        if(loanCharge.isPenaltyCharge()) {
            updateSelfPenalWaiverTransaction(chargeWaiverTransaction, selfAmountWaived);
        } else if (loanCharge.getCharge().isBounceCharge()) {
            updateSelfBounceWaiverTransaction(chargeWaiverTransaction, selfAmountWaived);
        } else {
            updateSelfFeeChargeWaiverTransaction(chargeWaiverTransaction, selfAmountWaived);
        }
    }

    private void updateSelfPenalWaiverTransaction(LoanTransaction chargeWaiverTransaction, BigDecimal selfAmountOutstanding) {
        chargeWaiverTransaction.setSelfPenaltyChargesPortion(Objects
                .requireNonNullElse(chargeWaiverTransaction.getSelfPenaltyChargesPortion(),BigDecimal.ZERO)
                .add(selfAmountOutstanding));
        chargeWaiverTransaction.setSelfDue(chargeWaiverTransaction.getSelfPenaltyChargesPortion());
    }
    private void updateSelfBounceWaiverTransaction(LoanTransaction chargeWaiverTransaction, BigDecimal selfAmountOutstanding) {
        chargeWaiverTransaction.setSelfBounceChargesPortion(Objects
                .requireNonNullElse(chargeWaiverTransaction.getSelfBounceChargesPortion(),BigDecimal.ZERO)
                .add(selfAmountOutstanding));
        chargeWaiverTransaction.setSelfDue(chargeWaiverTransaction.getSelfBounceChargesPortion());
    }
    private void updateSelfFeeChargeWaiverTransaction(LoanTransaction chargeWaiverTransaction, BigDecimal selfAmountOutstanding) {
        chargeWaiverTransaction.setSelfFeeChargesPortion(Objects
                .requireNonNullElse(chargeWaiverTransaction.getSelfFeeChargesPortion(),BigDecimal.ZERO)
                .add(selfAmountOutstanding));
        chargeWaiverTransaction.setSelfDue(chargeWaiverTransaction.getSelfFeeChargesPortion());
    }

    private void updatePartnerPortionOnFullWaived(LoanCharge loanCharge, LoanTransaction chargeWaiverTransaction) {
        BigDecimal partnerWaivedAmount = Objects.isNull(loanCharge.getPartnerAmountWaived())
                ? loanCharge.getPartnerAmountOutstanding()
                : loanCharge.getPartnerAmountWaived().add(loanCharge.getPartnerAmountOutstanding());
        updatePartnerTransactionPortion(chargeWaiverTransaction, loanCharge.getPartnerAmountOutstanding(), loanCharge);
        loanCharge.setPartnerAmountWaived(partnerWaivedAmount);
        loanCharge.setPartnerAmountOutstanding(BigDecimal.ZERO);
    }

    private void updatePartnerTransactionPortion(LoanTransaction chargeWaiverTransaction, BigDecimal partnerAmountWaived,LoanCharge loanCharge) {
        if(loanCharge.isPenaltyCharge()) {
            updatePartnerPenalWaiverTransaction(chargeWaiverTransaction,partnerAmountWaived);
        } else if (loanCharge.getCharge().isBounceCharge()) {
            updatePartnerBounceWaiverTransaction(chargeWaiverTransaction,partnerAmountWaived);
        } else {
            updatePartnerFeeChargeWaiverTransaction(chargeWaiverTransaction,partnerAmountWaived);
        }
    }

    private void updatePartnerPenalWaiverTransaction(LoanTransaction chargeWaiverTransaction, BigDecimal partnerAmountOutstanding) {
        chargeWaiverTransaction.setPartnerPenaltyChargesPortion(Objects
                .requireNonNullElse(chargeWaiverTransaction.getPartnerPenaltyChargesPortion(),BigDecimal.ZERO)
                .add(partnerAmountOutstanding));
        chargeWaiverTransaction.setPartnerDue(chargeWaiverTransaction.getPartnerPenaltyChargesPortion());
    }
    private void updatePartnerBounceWaiverTransaction(LoanTransaction chargeWaiverTransaction, BigDecimal partnerAmountOutstanding) {
        chargeWaiverTransaction.setPartnerBounceChargesPortion(Objects
                .requireNonNullElse(chargeWaiverTransaction.getPartnerBounceChargesPortion(),BigDecimal.ZERO)
                .add(partnerAmountOutstanding));
        chargeWaiverTransaction.setPartnerDue(chargeWaiverTransaction.getPartnerBounceChargesPortion());
    }

    private void updatePartnerFeeChargeWaiverTransaction(LoanTransaction chargePaymentTransaction, BigDecimal partnerAmountOutstanding) {
        chargePaymentTransaction.setPartnerFeeChargesPortion(Objects
                .requireNonNullElse(chargePaymentTransaction.getPartnerFeeChargesPortion(),BigDecimal.ZERO)
                .add(partnerAmountOutstanding));
        chargePaymentTransaction.setPartnerDue(chargePaymentTransaction.getPartnerFeeChargesPortion());
    }

    private BigDecimal updateSelfPortionOnShortWaiver(LoanCharge loanCharge, LoanTransaction chargeWaiverTransaction, BigDecimal receivedWaiver) {
        if (is(receivedWaiver).greaterThanOrEqualTo(loanCharge.getSelfAmountOutstanding())) {
            loanCharge.setSelfAmountWaived(loanCharge.getSelfAmountOutstanding());
            updateSelfTransactionPortion(chargeWaiverTransaction,loanCharge.getSelfAmountOutstanding(),loanCharge);
            receivedWaiver = receivedWaiver.subtract(loanCharge.getSelfAmountOutstanding());
            loanCharge.setSelfAmountOutstanding(BigDecimal.ZERO);
        } else {
            loanCharge.setSelfAmountWaived(receivedWaiver);
            updateSelfTransactionPortion(chargeWaiverTransaction,receivedWaiver,loanCharge);
            loanCharge.setSelfAmountOutstanding(loanCharge.getSelfAmountOutstanding().subtract(receivedWaiver));
            receivedWaiver = BigDecimal.ZERO;
        }
        return receivedWaiver;
    }

    private BigDecimal updatePartnerPortionOnShortWaiver(LoanCharge loanCharge, LoanTransaction chargeWaiverTransaction, BigDecimal receivedWaiver) {
        if (is(receivedWaiver).greaterThanOrEqualTo(loanCharge.getPartnerAmountOutstanding())) {
            loanCharge.setPartnerAmountWaived(loanCharge.getPartnerAmountOutstanding());
            updatePartnerTransactionPortion(chargeWaiverTransaction,loanCharge.getPartnerAmountOutstanding(),loanCharge);
            receivedWaiver = receivedWaiver.subtract(loanCharge.getPartnerAmountOutstanding());
            loanCharge.setPartnerAmountOutstanding(BigDecimal.ZERO);
        } else {
            loanCharge.setPartnerAmountWaived(receivedWaiver);
            updatePartnerTransactionPortion(chargeWaiverTransaction,receivedWaiver,loanCharge);
            loanCharge.setPartnerAmountOutstanding(loanCharge.getPartnerAmountOutstanding().subtract(receivedWaiver));
            receivedWaiver = BigDecimal.ZERO;
        }
        return receivedWaiver;
    }

    private void summarizeRepaymentScheduleForChargesWaived(Loan loan, LoanTransaction transaction) {
        // Defined LoanChargeAccumulator Record - For Reducing/Accumulating Penal and Charge Waiver
        record LoanChargeAccumulator(BigDecimal penalAmountWaived,BigDecimal feeChargeAmountWaived,BigDecimal bounceChargeAmountWaived){}


        Function<LoanCharge,LoanChargeAccumulator> loanChargeLoanChargeAccumulator = loanCharge -> loanCharge.isPenaltyCharge()
                ? new LoanChargeAccumulator(loanCharge.getAmountWaived(), BigDecimal.ZERO,BigDecimal.ZERO)
                : (loanCharge.getCharge().isBounceCharge())? new LoanChargeAccumulator(BigDecimal.ZERO, BigDecimal.ZERO,loanCharge.getAmountWaived().add(Objects.nonNull(loanCharge.getGstWaivedOffDerived())?loanCharge.getGstWaivedOffDerived():BigDecimal.ZERO)): new LoanChargeAccumulator(BigDecimal.ZERO, loanCharge.getAmountWaived(),BigDecimal.ZERO);


        BinaryOperator<LoanChargeAccumulator> chargesWaivedAmountAccumulator = (lca1, lca2) ->
                new LoanChargeAccumulator(lca1.penalAmountWaived().add(lca2.penalAmountWaived()),
                        lca1.feeChargeAmountWaived().add(lca2.feeChargeAmountWaived()),lca1.bounceChargeAmountWaived().add(lca2.bounceChargeAmountWaived()));

        Map<Integer,LoanChargeAccumulator> chargeTransactionMap = loan.getCharges().stream()
                .collect(Collectors.groupingBy(LoanCharge::getInstallmentNumber,
                        // Down Stream - Mapping LoanCharge to LoanChargeAccumulator
                        Collectors.mapping(loanChargeLoanChargeAccumulator,
                                // Down Stream - Reducing/Accumulating LoanChargeAccumulator
                                Collectors.reducing(new LoanChargeAccumulator(BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO), chargesWaivedAmountAccumulator))));

        Map<Integer,LoanChargeAccumulator> chargeWaivedMap = transaction.getLoanChargesPaid().stream()
                .collect(Collectors.groupingBy(LoanChargePaidBy::getInstallmentNumber,
                        // Down Stream - Mapping LoanChargePaidBy to LoanChargeAccumulator
                        Collectors.mapping(loanChargePaidBy -> loanChargePaidBy.getLoanCharge().isPenaltyCharge()
                                        ? new LoanChargeAccumulator(loanChargePaidBy.getAmount(), BigDecimal.ZERO,BigDecimal.ZERO)
                                        :(loanChargePaidBy.getLoanCharge().getCharge().isBounceCharge())? new LoanChargeAccumulator(BigDecimal.ZERO,BigDecimal.ZERO, loanChargePaidBy.getAmount()) : new LoanChargeAccumulator(BigDecimal.ZERO, loanChargePaidBy.getAmount(),BigDecimal.ZERO),
                                // Down Stream - Reducing/Accumulating LoanChargeAccumulator
                                Collectors.reducing(new LoanChargeAccumulator(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO), chargesWaivedAmountAccumulator))));

        LOG.info("Map {} ", chargeTransactionMap);
        loan.getRepaymentScheduleInstallments().forEach(loanRepaymentScheduleInstallment -> {
            if (chargeTransactionMap.containsKey(loanRepaymentScheduleInstallment.getInstallmentNumber())) {
                LoanChargeAccumulator accumulatedAmount = chargeTransactionMap.get(loanRepaymentScheduleInstallment.getInstallmentNumber());
                loanRepaymentScheduleInstallment.setPenaltyChargesWaived(accumulatedAmount.penalAmountWaived());
                loanRepaymentScheduleInstallment.setFeeChargesWaived(accumulatedAmount.feeChargeAmountWaived());
                loanRepaymentScheduleInstallment.setBounceChargesWaived(accumulatedAmount.bounceChargeAmountWaived());
                if (!loanRepaymentScheduleInstallment.isObligationsMet()) {
                    loanRepaymentScheduleInstallment.checkIfRepaymentPeriodObligationsAreMet(DateUtils.convertDateToLocalDate(transaction.getValueDate()),
                            loanRepaymentScheduleInstallment.getLoan().getCurrency());
                }
            }

            if (chargeWaivedMap.containsKey(loanRepaymentScheduleInstallment.getInstallmentNumber())) {
                LoanChargeAccumulator accumulatedAmount = chargeWaivedMap.get(loanRepaymentScheduleInstallment.getInstallmentNumber());
                LoanTransactionToRepaymentScheduleMapping repaymentScheduleMapping = createChargesWaiverTransactionMapping(transaction,loanRepaymentScheduleInstallment,
                        accumulatedAmount.feeChargeAmountWaived(), accumulatedAmount.penalAmountWaived());
                transaction.getLoanTransactionToRepaymentScheduleMappings().add(repaymentScheduleMapping);
            }
        });
    }

    private LoanTransactionToRepaymentScheduleMapping createChargesWaiverTransactionMapping(final LoanTransaction loanTransaction, final LoanRepaymentScheduleInstallment installment,
                                                                                             final BigDecimal feeChargesAmount,final BigDecimal penalAmount){
        MonetaryCurrency currency = loanTransaction.getLoan().getCurrency();
        return LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, installment, Money.zero(currency),Money.zero(currency),
                Money.of(currency, feeChargesAmount), Money.of(currency, penalAmount),Money.zero(currency),Money.zero(currency),Money.zero(currency),
                Money.zero(currency),BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);
    }
}
