/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;

/**
 * A wrapper around loan schedule related data exposing needed behaviour by loan.
 */
public class LoanRepaymentScheduleProcessingWrapper {

    public void reprocess(final MonetaryCurrency currency, final LocalDate disbursementDate,
                          final List<LoanRepaymentScheduleInstallment> repaymentPeriods, final List<LoanCharge> loanCharges) {

        Money totalInterest = Money.zero(currency);
        Money totalPrincipal = Money.zero(currency);
        Money totalSelfPrincipal = Money.zero(currency);
        Money totalPartnerPrincipal = Money.zero(currency);
        Money totalSelfInterestCharged = Money.zero(currency);
        Money totalPartnerInterestCharged = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentPeriods) {
            totalInterest = totalInterest.plus(installment.getInterestCharged(currency));
            totalPrincipal = totalPrincipal.plus(installment.getPrincipal(currency));
            totalSelfPrincipal = totalSelfPrincipal.plus(installment.getSelfPrincipal(currency));
            totalPartnerPrincipal = totalPartnerPrincipal.plus(installment.getPartnerPrincipal(currency));
            totalSelfInterestCharged = totalSelfInterestCharged.plus(installment.getSelfInterestCharged(currency));
            totalPartnerInterestCharged = totalPartnerInterestCharged.plus(installment.getPartnerInterestCharged(currency));

        }
        LocalDate startDate = disbursementDate;
        for (final LoanRepaymentScheduleInstallment period : repaymentPeriods) {

//            for(final LoanCharge loanCharge :loanCharges) {

             //   final Integer periodNumbers =loanCharge.getOverdueInstallmentCharge().getInstallment().getInstallmentNumber();


                final Money feeChargesDueForRepaymentPeriod = cumulativeFeeChargesDueWithin(startDate, period.getDueDate(), loanCharges,
                        currency, period, totalPrincipal, totalInterest, !period.isRecalculatedInterestComponent(), totalSelfPrincipal, totalPartnerPrincipal, totalSelfInterestCharged, totalPartnerInterestCharged);
                final Money feeChargesWaivedForRepaymentPeriod = cumulativeFeeChargesWaivedWithin(startDate, period.getDueDate(), loanCharges,
                        currency, !period.isRecalculatedInterestComponent());
                final Money feeChargesWrittenOffForRepaymentPeriod = cumulativeFeeChargesWrittenOffWithin(startDate, period.getDueDate(),
                        loanCharges, currency, !period.isRecalculatedInterestComponent());

                final BigDecimal penaltyChargesDueForRepaymentPeriod = cumulativePenaltyChargesDueWithin(startDate, period.getDueDate(), loanCharges,
                        currency, period, totalPrincipal, totalInterest, !period.isRecalculatedInterestComponent(), totalSelfPrincipal, totalPartnerPrincipal, totalSelfInterestCharged, totalPartnerInterestCharged);
                final Money penaltyChargesWaivedForRepaymentPeriod = cumulativePenaltyChargesWaivedWithin(startDate, period.getDueDate(),
                        loanCharges, currency, !period.isRecalculatedInterestComponent());
                   final Money penaltyChargesWrittenOffForRepaymentPeriod = cumulativePenaltyChargesWrittenOffWithin(startDate,
                        period.getDueDate(), loanCharges, currency, !period.isRecalculatedInterestComponent());

                period.updateChargePortion(feeChargesDueForRepaymentPeriod, feeChargesWaivedForRepaymentPeriod,
                        feeChargesWrittenOffForRepaymentPeriod, penaltyChargesDueForRepaymentPeriod, penaltyChargesWaivedForRepaymentPeriod,
                        penaltyChargesWrittenOffForRepaymentPeriod);

            /**
             *self and partner fee share amount for each installment level
             *
             */
//            final Money selfFeeChargesDueForRepaymentPeriod = cumulativeSelfFeeChargesDueWithin(startDate, period.getDueDate(), loanCharges,currency,period);
//            final Money partnerFeeChargesDueForRepaymentPeriod = cumulativePartnerFeeChargesDueWithin(startDate, period.getDueDate(), loanCharges,currency,period);
            /**
             * self share and partner share calculation For each installment
                */
                final BigDecimal selfPenalityChargeShare = cumulativeSelfPenaltyChargeWithin(startDate, period, loanCharges, currency);
                final BigDecimal partnerPenalityChargeShare = cumulativePartnerPenaltyChargeWithin(startDate, period, loanCharges, currency);
                period.updatePenalChargeShare(selfPenalityChargeShare, partnerPenalityChargeShare);
                startDate = period.getDueDate();
            }
    }

    /*private Money cumulativePartnerFeeChargesDueWithin(LocalDate startDate, LocalDate dueDate, Set<LoanCharge> loanCharges, MonetaryCurrency currency, LoanRepaymentScheduleInstallment period) {

        Money cumulative = Money.zero(currency);
        for(LoanCharge loanCharge : loanCharges ){
            if(loanCharge.isActive() &&  loanCharge.getInstallmentNumber().equals(period.getInstallmentNumber())){

                cumulative = cumulative.plus(loanCharge.partnerAmount());
            }
        }
        return cumulative;
    }

    private Money cumulativeSelfFeeChargesDueWithin(LocalDate startDate, LocalDate dueDate, Set<LoanCharge> loanCharges, MonetaryCurrency currency,
                                                    LoanRepaymentScheduleInstallment period) {
         Money cumulative = Money.zero(currency);
        for(LoanCharge loanCharge : loanCharges ){
            if(loanCharge.isActive() &&  loanCharge.getInstallmentNumber().equals(period.getInstallmentNumber())){

                cumulative = cumulative.plus(loanCharge.selfAmount());
            }
        }
        return cumulative;
    }*/

    private Money cumulativeFeeChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final List<LoanCharge> loanCharges,
                                                final MonetaryCurrency monetaryCurrency, LoanRepaymentScheduleInstallment period, final Money totalPrincipal,
                                                final Money totalInterest, boolean isInstallmentChargeApplicable,final Money totalSelfPrincipal,final Money totalPartnerPrincipal,final Money totalSelfInterstCharged,final Money totalPartnerInterstCharged) {

        Money cumulative = Money.zero(monetaryCurrency);
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge() && !loanCharge.isDueAtDisbursement()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    if (loanCharge.getChargeCalculation().isPercentageBased()) {
                        BigDecimal amount = BigDecimal.ZERO;
                        if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                            amount = amount.add(period.getPrincipal(monetaryCurrency).getAmount())
                                    .add(period.getInterestCharged(monetaryCurrency).getAmount());
                        } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                            amount = amount.add(period.getInterestCharged(monetaryCurrency).getAmount());
                        } else {
                            amount = amount.add(period.getPrincipal(monetaryCurrency).getAmount());
//                            amount = amount.add(period.getSelfPrincipal(monetaryCurrency).getAmount());
//                            amount = amount.add(period.getPartnerPrincipal(monetaryCurrency).getAmount());

                        }
                        BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                        cumulative = cumulative.plus(loanChargeAmt);
                    } else {
                        cumulative = cumulative.plus(loanCharge.amountOrPercentage());
                    }
                } else if (loanCharge.isOverdueInstallmentCharge()
                        && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                        amount = amount.add(totalPrincipal.getAmount()).add(totalInterest.getAmount());


                    } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                        amount = amount.add(totalInterest.getAmount());
                    } else {
                        // If charge type is specified due date and loan is
                        // multi disburment loan.
                        // Then we need to get as of this loan charge due date
                        // how much amount disbursed.
                        if (loanCharge.getLoan() != null && loanCharge.isSpecifiedDueDate()
                                && loanCharge.getLoan().isMultiDisburmentLoan()) {
                            for (final LoanDisbursementDetails loanDisbursementDetails : loanCharge.getLoan().getDisbursementDetails()) {
                                if (!loanDisbursementDetails.expectedDisbursementDate().after(loanCharge.getDueDate())) {
                                    amount = amount.add(loanDisbursementDetails.principal());

                                }
                            }
                        } else {
                            amount = amount.add(totalPrincipal.getAmount());

                        }
                    }
                    BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                    cumulative = cumulative.plus(loanChargeAmt);
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.amount());
                }
            }
        }

        return cumulative;
    }

    private Money cumulativeFeeChargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd,
                                                   final List<LoanCharge> loanCharges, final MonetaryCurrency currency, boolean isInstallmentChargeApplicable) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge() && !loanCharge.isDueAtDisbursement()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWaived(currency));
                    }
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.getAmountWaived(currency));
                }
            }
        }

        return cumulative;
    }

    private Money cumulativeFeeChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd,
                                                       final List<LoanCharge> loanCharges, final MonetaryCurrency currency, boolean isInstallmentChargeApplicable) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge() && !loanCharge.isDueAtDisbursement()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWrittenOff(currency));
                    }
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.getAmountWrittenOff(currency));
                }
            }
        }

        return cumulative;
    }

    private BigDecimal cumulativePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
                                                    final List<LoanCharge> loanCharges, final MonetaryCurrency currency,
                                                    LoanRepaymentScheduleInstallment period,
                                                    final Money totalPrincipal, final Money totalInterest,
                                                         boolean isInstallmentChargeApplicable,final Money totalSelfPrincipal,
                                                         final Money totalPartnerPrincipal,final Money totalSelfInterstCharged,
                                                         final Money totalPartnerInterstCharged) {

        BigDecimal cumulative = BigDecimal.ZERO;
        RoundingMode productRoundingMode = period.getLoan().getLoanProduct().getRoundingMode();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                productRoundingMode = loanCharge.getLoan().getLoanProduct().getRoundingMode();
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    if (loanCharge.getChargeCalculation().isPercentageBased()) {
                        BigDecimal amount = BigDecimal.ZERO;
                        if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                            amount = amount.add(period.getPrincipal(currency).getAmount())
                                    .add(period.getInterestCharged(currency).getAmount());

                        } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                            amount = amount.add(period.getInterestCharged(currency).getAmount());
                        } else {
                            amount = amount.add(period.getPrincipal(currency).getAmount());

                        }
                        BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                        cumulative = BigDecimal.valueOf(cumulative.doubleValue() + loanChargeAmt.doubleValue()).setScale(2, RoundingMode.HALF_UP);
                    } else {
                        cumulative = BigDecimal.valueOf(cumulative.doubleValue() + loanCharge.amountOrPercentage().doubleValue()).setScale(2, RoundingMode.HALF_UP);
                    }
                } else if (loanCharge.isOverdueInstallmentCharge()
                        && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()
                        && loanCharge.getInstallmentNumber().equals(period.getInstallmentNumber())) {
                    cumulative = BigDecimal.valueOf(cumulative.doubleValue() + loanCharge.chargeAmount().doubleValue());
                }
//                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
//                        && loanCharge.getChargeCalculation().isPercentageBased()) {
//                    BigDecimal amount = BigDecimal.ZERO;
//                    if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
//                        amount = amount.add(totalPrincipal.getAmount()).add(totalInterest.getAmount());
//
//                    } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
//                        amount = amount.add(totalInterest.getAmount());
//                    } else {
//                        amount = amount.add(totalPrincipal.getAmount());
//
//                    }
//                    BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
//                    cumulative = cumulative.plus(loanChargeAmt);
//                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
//                    cumulative = cumulative.plus(loanCharge.amount());
//                }
            }
        }

        return cumulative.setScale(0, productRoundingMode);
    }

    private Money cumulativePenaltyChargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd,
                                                       final List<LoanCharge> loanCharges, final MonetaryCurrency currency, boolean isInstallmentChargeApplicable) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWaived(currency));
                    }
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.getAmountWaived(currency));
                }
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd,
                                                           final List<LoanCharge> loanCharges, final MonetaryCurrency currency, boolean isInstallmentChargeApplicable) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWrittenOff(currency));
                    }
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.getAmountWrittenOff(currency));
                }
            }
            }

        return cumulative;
    }

    private BigDecimal cumulativeSelfPenaltyChargeWithin(final LocalDate startDate,final LoanRepaymentScheduleInstallment period,
                                                    final List<LoanCharge> loanCharges, final MonetaryCurrency currency) {
       // Money cumulative = Money.zero(currency);
        BigDecimal amount = BigDecimal.ZERO;
        for(LoanCharge loanCharge: loanCharges) {
            if (loanCharge.isOverdueInstallmentCharge() && loanCharge.isActive()
                    && loanCharge.isDueForCollectionFromAndUpToAndIncluding(startDate, period.getDueDate())
                    && loanCharge.getChargeCalculation().isPercentageBased()
                    && loanCharge.getInstallmentNumber().equals(period.getInstallmentNumber())) {
              //  cumulative = cumulative.plus(loanCharge.getSelfShareAmount());
                amount = amount.add(loanCharge.getSelfShareAmount());
            }
        }
        return amount;

    }

    private BigDecimal cumulativePartnerPenaltyChargeWithin(final LocalDate startDate, final LoanRepaymentScheduleInstallment period, final List<LoanCharge> loanCharges, final MonetaryCurrency currency) {
        Money cumulative = Money.zero(currency);
        BigDecimal amount = BigDecimal.ZERO;
        for(LoanCharge loanCharge:loanCharges) {
            if (loanCharge.isOverdueInstallmentCharge()
                    && loanCharge.isDueForCollectionFromAndUpToAndIncluding(startDate, period.getDueDate())
                    && loanCharge.getChargeCalculation().isPercentageBased()
                    && loanCharge.getInstallmentNumber().equals(period.getInstallmentNumber())) {
              //  cumulative = cumulative.plus(loanCharge.getPartnerShareAmount());
                 amount = amount.add(loanCharge.getPartnerShareAmount());
            }
        }
        return amount;
    }
}
