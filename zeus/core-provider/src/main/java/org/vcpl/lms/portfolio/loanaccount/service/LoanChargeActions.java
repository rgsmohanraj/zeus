package org.vcpl.lms.portfolio.loanaccount.service;

import org.jetbrains.annotations.NotNull;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.charge.domain.ChargeCalculationType;
import org.vcpl.lms.portfolio.charge.domain.ChargeTimeType;
import org.vcpl.lms.portfolio.common.domain.DaysInYearType;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeTransactionRequest;
import org.vcpl.lms.portfolio.loanaccount.data.GstData;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCharge;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransaction;
import org.vcpl.lms.useradministration.domain.AppUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface LoanChargeActions {

    interface Creation {
        LoanCharge create(final ChargeTransactionRequest chargeTransactionRequest);
    }

    interface Payment {
        LoanTransaction pay(final ChargeTransactionRequest chargeTransactionRequest);
        LoanTransaction pay(final ChargeTransactionRequest chargeTransactionRequest,final LoanTransaction existingTransaction);
    }

    interface Waiver {
        LoanTransaction waive(final ChargeTransactionRequest chargeTransactionRequest);
        LoanTransaction waive(final ChargeTransactionRequest chargeTransactionRequest,final LoanTransaction existingTransaction);
    }

    static LoanCharge createCharge(final Loan loan, final AppUser systemUser,
                                   final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment,
                                   final Charge charge, final LocalDate createOn,final BigDecimal selfShare,
                                   final BigDecimal partnerShare,final BigDecimal amount) {
        LoanCharge loanCharge = new LoanCharge();
        loanCharge.setInstallmentNumber(loanRepaymentScheduleInstallment.getInstallmentNumber());
        loanCharge.setLoan(loan);
        loanCharge.setCharge(charge);
        loanCharge.setAmountOrPercentage(amount);
        loanCharge.setFeesChargeTypes(charge.getFeesChargeType());

        updateChargeCalculations(loanRepaymentScheduleInstallment, charge,loanCharge,amount);
        loanCharge.setChargeTime(charge.getChargeTimeType());
        loanCharge.setChargeCalculation(charge.getChargeCalculation());
        loanCharge.setSelfSharePercentage(selfShare);
        loanCharge.setPartnerSharePercentage(partnerShare);
        loanCharge.updateSplitShareAmount(loanCharge.getAmount());
        loanCharge.setAmountOutstanding(loanCharge.getAmount());

        loanCharge.setMaxCap(charge.getMaxCap());
        loanCharge.setMinCap(charge.getMinCap());
        loanCharge.setActive(true);
        loanCharge.setPenaltyCharge(charge.isPenalty());

        // GST Information
        loanCharge.setGstEnabled(charge.enabelGst());
        loanCharge.setGst(charge.getGst());

        // Date Information
        loanCharge.setDueDate(DateUtils.convertLocalDateToDate(createOn.minusDays(1)));
        loanCharge.setCreatedDate(new Date());
        loanCharge.setCreatedUser(systemUser);
        loanCharge.setModifiedDate(new Date());
        loanCharge.setModifiedUser(systemUser);
        loanCharge.setChargePaymentMode(0);
        return loanCharge;
    }

    static void updateChargeCalculations(final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment,
                                          final Charge charge, final LoanCharge loanCharge,final BigDecimal amount) {
        switch (ChargeCalculationType.fromInt(charge.getChargeCalculation())) {
            case FLAT -> loanCharge.setAmount(amount);
            case PERCENT_OF_AMOUNT -> {
                BigDecimal calculatedOnAmount = loanRepaymentScheduleInstallment.getPrincipal();
                loanCharge.setPercentage(amount);
                loanCharge.setAmountOrPercentage(amount);
                loanCharge.setAmountPercentageAppliedTo(calculatedOnAmount);
                loanCharge.setAmount(calculatePercentageOn(charge, amount, calculatedOnAmount));
            }
            case PERCENT_OF_AMOUNT_AND_INTEREST -> {
                BigDecimal calculatedOnAmount = loanRepaymentScheduleInstallment.getPrincipal().add(loanRepaymentScheduleInstallment.getInterestCharged());
                loanCharge.setPercentage(amount);
                loanCharge.setAmountOrPercentage(amount);
                loanCharge.setAmountPercentageAppliedTo(calculatedOnAmount);
                loanCharge.setAmount(calculatePercentageOn(charge, amount, calculatedOnAmount));
            }
            case PERCENT_OF_INTEREST -> {
                BigDecimal calculatedOnAmount = loanRepaymentScheduleInstallment.getInterestCharged();
                loanCharge.setPercentage(amount);
                loanCharge.setAmountOrPercentage(amount);
                loanCharge.setAmountPercentageAppliedTo(calculatedOnAmount);
                loanCharge.setAmount(calculatePercentageOn(charge, amount, calculatedOnAmount));
            }
            case PERCENT_OF_DISBURSEMENT_AMOUNT -> {
                BigDecimal calculatedOnAmount = loanCharge.getLoan().getDisbursedAmount();
                loanCharge.setPercentage(amount);
                loanCharge.setAmountOrPercentage(amount);
                loanCharge.setAmountPercentageAppliedTo(calculatedOnAmount);
                loanCharge.setAmount(calculatePercentageOn(charge, amount, calculatedOnAmount));
            }
        }
    }

    @NotNull
    private static BigDecimal calculatePercentageOn(Charge charge, BigDecimal amount, BigDecimal calculatedOnAmount) {
        BigDecimal chargeAmount;
        if(charge.isPenalty()) {
            final DaysInYearType daysInYearsType = DaysInYearType.fromInt(charge.getPenaltyInterestDaysInYear());
            final Integer penaltyInterestDaysInYears = daysInYearsType.equals(DaysInYearType.ACTUAL)
                    ? LocalDate.now().lengthOfYear()
                    : daysInYearsType.getValue();
            chargeAmount = BigDecimal.valueOf(LoanCharge.percentageOf(amount, calculatedOnAmount).doubleValue() / penaltyInterestDaysInYears)
                    .setScale(charge.getChargeDecimal(), charge.getChargeRoundingMode());
        } else {
            chargeAmount = BigDecimal.valueOf(LoanCharge.percentageOf(amount, calculatedOnAmount).doubleValue())
                    .setScale(charge.getChargeDecimal(), charge.getChargeRoundingMode());
        }
        return chargeAmount;
    }

    static void reverseChargesOnRepayment(LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment,LocalDate obligationMeetOn) {
        Money totalInterestPrincipleCharged = loanRepaymentScheduleInstallment.getPrincipal(loanRepaymentScheduleInstallment.getLoan().getCurrency())
                .add(loanRepaymentScheduleInstallment.getInterestCharged(loanRepaymentScheduleInstallment.getLoan().getCurrency()));
        Money totalInterestPrincipleRepaid = loanRepaymentScheduleInstallment.getPrincipalCompleted(loanRepaymentScheduleInstallment.getLoan().getCurrency())
                .add(loanRepaymentScheduleInstallment.getInterestPaid(loanRepaymentScheduleInstallment.getLoan().getCurrency()));

        if (!totalInterestPrincipleCharged.minus(totalInterestPrincipleRepaid).isZero()
                || loanRepaymentScheduleInstallment.isObligationsMet()) {
            return;
        }

        record ChargeShare(BigDecimal clientShare,BigDecimal selfShare,BigDecimal partnerShare){}

        Predicate<LoanCharge> installmentPredicate = loanCharge -> !loanCharge.isActive()
                && Objects.nonNull(loanCharge.getInstallmentNumber())
                && loanCharge.getInstallmentNumber().equals(loanRepaymentScheduleInstallment.getInstallmentNumber())
                && (loanCharge.isPenaltyCharge() || loanCharge.isBounceCharge());

        Predicate<LoanCharge> installmentAndChargeCreatedOnPredicate = loanCharge -> loanCharge.isActive()
                && Objects.nonNull(loanCharge.getInstallmentNumber())
                && loanCharge.getInstallmentNumber().equals(loanRepaymentScheduleInstallment.getInstallmentNumber())
                && (loanCharge.isPenaltyCharge() || loanCharge.isBounceCharge())
                && (DateUtils.convertDateToLocalDate(loanCharge.getDueDate()).equals(obligationMeetOn)
                || DateUtils.convertDateToLocalDate(loanCharge.getDueDate()).isAfter(obligationMeetOn));

        BinaryOperator<ChargeShare> chargeAmountAccumulator = (lca1, lca2) ->
                new ChargeShare(lca1.clientShare().add(lca2.clientShare()),
                        lca1.selfShare().add(lca2.selfShare()),lca1.partnerShare().add(lca2.partnerShare()));

        loanRepaymentScheduleInstallment.getLoan().getCharges().stream()
                .filter(installmentAndChargeCreatedOnPredicate)
                .forEach(loanCharge -> loanCharge.setActive(false));

//        Map<ChargeTimeType,ChargeShare> chargeAmountMap = loanRepaymentScheduleInstallment.getLoan().getCharges().stream().filter(installmentPredicate)
//                .collect(Collectors.groupingBy(LoanCharge::getChargeTimeType,
//                        Collectors.mapping(loanCharge -> new ChargeShare(loanCharge.getAmount(),loanCharge.getSelfShareAmount(),loanCharge.getPartnerShareAmount()),
//                                Collectors.reducing(new ChargeShare(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO),chargeAmountAccumulator))));
        Map<ChargeTimeType,ChargeShare> chargeAmountMap = loanRepaymentScheduleInstallment.getLoan().getCharges().stream()
                .filter(installmentPredicate)
                .collect(Collectors.groupingBy(LoanCharge::getChargeTimeType,
                        Collectors.mapping(loanCharge -> new ChargeShare(loanCharge.getAmount().add(Objects.requireNonNullElse(loanCharge.getTotalGst(),BigDecimal.ZERO)),
                                        loanCharge.getSelfShareAmount().add(Objects.requireNonNullElse(loanCharge.getTotalSelfGst(),BigDecimal.ZERO)),
                                        loanCharge.getPartnerShareAmount().add(Objects.requireNonNullElse(loanCharge.getTotalPartnerGst(),BigDecimal.ZERO))),
                                Collectors.reducing(new ChargeShare(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO),chargeAmountAccumulator))));
        if(chargeAmountMap.containsKey(ChargeTimeType.OVERDUE_INSTALLMENT)) {
            ChargeShare penalChargeShare = chargeAmountMap.get(ChargeTimeType.OVERDUE_INSTALLMENT);
            loanRepaymentScheduleInstallment.setPenaltyCharges(loanRepaymentScheduleInstallment.getPenaltyChargesCharged().subtract(penalChargeShare.clientShare()));
            loanRepaymentScheduleInstallment.setSelfPenaltyCharges(loanRepaymentScheduleInstallment.getSelfPenaltyChargesCharged().subtract(penalChargeShare.selfShare()));
            loanRepaymentScheduleInstallment.setPartnerPenaltyCharges(loanRepaymentScheduleInstallment.getPartnerPenaltyChargesCharged().subtract(penalChargeShare.partnerShare()));
            loanRepaymentScheduleInstallment.checkIfRepaymentPeriodObligationsAreMet(obligationMeetOn,loanRepaymentScheduleInstallment.getLoan().getCurrency());
        }
        if(chargeAmountMap.containsKey(ChargeTimeType.BOUNCE_CHARGE)) {
            ChargeShare bounceChargeShare = chargeAmountMap.get(ChargeTimeType.BOUNCE_CHARGE);
            loanRepaymentScheduleInstallment.setBounceCharges(loanRepaymentScheduleInstallment.getBounceChargesCharged().subtract(bounceChargeShare.clientShare()));
            loanRepaymentScheduleInstallment.setSelfBounceCharges(loanRepaymentScheduleInstallment.getSelfBounceChargesCharged().subtract(bounceChargeShare.selfShare()));
            loanRepaymentScheduleInstallment.setPartnerBounceCharges(loanRepaymentScheduleInstallment.getPartnerBounceChargesCharged().subtract(bounceChargeShare.partnerShare()));
            loanRepaymentScheduleInstallment.checkIfRepaymentPeriodObligationsAreMet(obligationMeetOn,loanRepaymentScheduleInstallment.getLoan().getCurrency());
        }
    }

    /**
     * Should be used only on installment that is going to be deleted.
     * Removes the charges tagged to this installment and makes the penal & bounce charges zero.
     *
     * @param installment
     */
    static void reverseChargesOnForeclosure(final LoanRepaymentScheduleInstallment installment) {
        Predicate<LoanCharge> installmentPredicate = loanCharge -> loanCharge.isActive()
                && Objects.nonNull(loanCharge.getInstallmentNumber())
                && loanCharge.getInstallmentNumber().equals(installment.getInstallmentNumber())
                && (loanCharge.isPenaltyCharge() || loanCharge.isBounceCharge());
        installment.getLoan().getLoanCharges()
                .stream()
                .filter(installmentPredicate)
                .forEach(loanCharge -> loanCharge.setActive(false));
        installment.setPenaltyCharges(BigDecimal.ZERO);
        installment.setSelfPenaltyCharges(BigDecimal.ZERO);
        installment.setPartnerPenaltyCharges(BigDecimal.ZERO);
        installment.setBounceCharges(BigDecimal.ZERO);
        installment.setSelfBounceCharges(BigDecimal.ZERO);
        installment.setPartnerBounceCharges(BigDecimal.ZERO);
    }
}
