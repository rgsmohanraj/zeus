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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.springframework.stereotype.Component;

/**
 * A wrapper for dealing with side-effect free functionality related to a loans transactions and repayment schedule.
 */
@Component
public final class LoanSummaryWrapper {

    public BigDecimal calculateTotalPrincipalRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPrincipalCompleted().doubleValue() > 0 )
                .map(LoanRepaymentScheduleInstallment::getPrincipalCompleted)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateSelfTotalPrincipalRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfPrincipalCompleted()!= null )
                .map(LoanRepaymentScheduleInstallment::getSelfPrincipalCompleted)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculatePartnerTotalPrincipalRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerPrincipalCompleted() != null)
                .map(LoanRepaymentScheduleInstallment::getPartnerPrincipalCompleted)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalPrincipalWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return  repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPrincipalWrittenOff() != null)
                .map(LoanRepaymentScheduleInstallment::getPrincipalWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public Money calculateTotalPrincipalOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
                                                  final MonetaryCurrency currency, final LocalDate overdueAsOf) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            if (installment.isOverdueOn(overdueAsOf)) {
                total = total.plus(installment.getPrincipalOutstanding(currency));
            }
        }
        return total;
    }

    public BigDecimal calculateTotalInterestCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return  repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getInterestCharged().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getInterestCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }


    public BigDecimal calculateTotalSelfInterestCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfInterestCharged().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getSelfInterestCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalPartnerInterestCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerInterestCharged().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getPartnerInterestCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalInterestRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getInterestPaid().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getInterestPaid)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalSelfInterestRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfInterestPaid().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getSelfInterestPaid)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalPartnerInterestRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerInterestPaid().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getPartnerInterestPaid)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalSelfInterestWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return  repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfInterestWaived().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getSelfInterestWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalPartnerInterestWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerInterestWaived().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getPartnerInterestWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalInterestWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getInterestWaived().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getInterestWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalInterestWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getInterestWrittenOff().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getPartnerInterestWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public Money calculateTotalInterestOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
                                                 final MonetaryCurrency currency, final LocalDate overdueAsOf) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            if (installment.isOverdueOn(overdueAsOf)) {
                total = total.plus(installment.getInterestOutstanding(currency));
            }
        }
        return total;
    }

    public BigDecimal calculateTotalFeeChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getFeeChargesCharged().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getFeeChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalSelfFeeChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return  repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfFeeChargesCharged().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getSelfFeeChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalPartnerFeeChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerFeeChargesCharged().doubleValue() >0)
                .map(LoanRepaymentScheduleInstallment::getPartnerFeeChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }

    public Money calculateTotalFeeChargesRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
                                                final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getFeeChargesPaid(currency));
        }
        return total;
    }

    public BigDecimal calculateTotalFeeChargesWaived(List<LoanCharge> charges) {
        BigDecimal total =BigDecimal.ZERO;
        for (final LoanCharge charge : charges) {
            if (charge.isActive() && !charge.isPenaltyCharge() && !charge.isBounceCharge()) {
                total = total.add(charge.getAmountWaived());
            }
        }
        return total;
    }

    public BigDecimal calculateTotalFeeChargesWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getFeeChargesWrittenOff() != null)
                .map(LoanRepaymentScheduleInstallment::getFeeChargesWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }


    public BigDecimal calculateTotalSelfFeeChargesWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfFeeChargesWrittenOff() != null)
                .map(LoanRepaymentScheduleInstallment::getSelfFeeChargesWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalPartnerFeeChargesWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        return  repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerFeeChargesWrittenOff() != null)
                .map(LoanRepaymentScheduleInstallment::getPartnerFeeChargesWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public Money calculateTotalFeeChargesOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
                                                   final MonetaryCurrency currency, final LocalDate overdueAsOf) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            if (installment.isOverdueOn(overdueAsOf)) {
                total = total.plus(installment.getFeeChargesOutstanding(currency));
            }
        }
        return total;
    }

    public BigDecimal calculateTotalPenaltyChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPenaltyChargesCharged() != null)
                .map(LoanRepaymentScheduleInstallment::getPenaltyChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total.setScale(0, RoundingMode.HALF_UP);
    }
    public BigDecimal calculateTotalBounceChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getBounceChargesCharged() != null)
                .map(LoanRepaymentScheduleInstallment::getBounceChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total.setScale(0, RoundingMode.HALF_UP);
    }
    public BigDecimal calculateTotalPenaltyChargesRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPenaltyChargesPaid().doubleValue() > 0)
                .map(LoanRepaymentScheduleInstallment::getPenaltyChargesPaid)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }

    public BigDecimal calculateTotalPenaltyChargesWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPenaltyChargesWaived().doubleValue() > 0)
                .map(LoanRepaymentScheduleInstallment::getPenaltyChargesWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }

    public BigDecimal calculateTotalPenaltyChargesWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {


        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPenaltyChargesWrittenOff()!=null)
                .map(LoanRepaymentScheduleInstallment::getPenaltyChargesWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }

    public Money calculateTotalPenaltyChargesOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
                                                       final MonetaryCurrency currency, final LocalDate overdueAsOf) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            if (installment.isOverdueOn(overdueAsOf)) {
                total = total.plus(installment.getPenaltyChargesOutstanding(currency));
            }
        }
        return total;
    }

    public Money calculateTotalOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
                                         final MonetaryCurrency currency, final LocalDate overdueAsOf) {

        final Money principalOverdue = calculateTotalPrincipalOverdueOn(repaymentScheduleInstallments, currency, overdueAsOf);
        final Money interestOverdue = calculateTotalInterestOverdueOn(repaymentScheduleInstallments, currency, overdueAsOf);
        final Money feeChargesOverdue = calculateTotalFeeChargesOverdueOn(repaymentScheduleInstallments, currency, overdueAsOf);
        final Money penaltyChargesOverdue = calculateTotalPenaltyChargesOverdueOn(repaymentScheduleInstallments, currency, overdueAsOf);

        return principalOverdue.plus(interestOverdue).plus(feeChargesOverdue).plus(penaltyChargesOverdue);
    }

    public LocalDate determineOverdueSinceDateFrom(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
                                                   final MonetaryCurrency currency, final LocalDate from) {

        LocalDate overdueSince = null;
        final Money totalOverdue = calculateTotalOverdueOn(repaymentScheduleInstallments, currency, from);
        if (totalOverdue.isGreaterThanZero()) {
            for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
                if (installment.isOverdueOn(from)) {
                    if (overdueSince == null || overdueSince.isAfter(installment.getDueDate())) {
                        overdueSince = installment.getDueDate();
                    }
                }
            }
        }

        return overdueSince;
    }

    public BigDecimal calculateTotalChargesRepaidAtDisbursement(List<LoanCharge> charges) {
        BigDecimal total =BigDecimal.ZERO;
        if (charges == null) {
            return total;
        }
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isBounceCharge() &&!loanCharge.isPenaltyCharge() && loanCharge.getAmountPaid().doubleValue() > 0 &&
                    !loanCharge.getCharge().isForeclosureCharge() && loanCharge.isActive() && !loanCharge.isCoolingOffReversed()) {
                total = total.add(loanCharge.getAmountPaid());
            }
        }
        return total;

    }

    public BigDecimal calculateTotalGst(Collection<LoanCharge> loanCharges) {

        final  BigDecimal total = loanCharges.stream().filter(loanCharge -> loanCharge.getTotalGst().doubleValue() > 0 )
                .map(LoanCharge::getTotalGst).reduce(BigDecimal.ZERO,BigDecimal::add);

        return total;
    }

    public BigDecimal calculateSelfPenalShareAmountTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfPenaltyChargesCharged() != null)
                .map(LoanRepaymentScheduleInstallment::getSelfPenaltyChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total.setScale(0,RoundingMode.HALF_UP);
    }
    public BigDecimal calculateSelfBounceShareAmountTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfBounceChargesCharged() != null)
                .map(LoanRepaymentScheduleInstallment::getSelfBounceChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total.setScale(0,RoundingMode.HALF_UP);
    }

    public BigDecimal calculateSelfPenalChargesRepaidTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfPenaltyChargesPaid() != null)
                .map(LoanRepaymentScheduleInstallment::getSelfPenaltyChargesPaid)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculateSelfBounceChargesRepaidTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfBounceChargesPaid() != null)
                .map(LoanRepaymentScheduleInstallment::getSelfBounceChargesPaid)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculateSelfPenalWaivedChargesTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfPenaltyChargesWaived() != null)
                .map(LoanRepaymentScheduleInstallment::getSelfPenaltyChargesWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculateSelfBounceWaivedChargesTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfBounceChargesWaived() != null)
                .map(LoanRepaymentScheduleInstallment::getSelfBounceChargesWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }

    public BigDecimal calculateSelfPenalWrittenOffChargesTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, MonetaryCurrency currency) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfPenaltyChargesWrittenOff() != null)
                .map(LoanRepaymentScheduleInstallment::getSelfPenaltyChargesWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculateSelfBounceWrittenOffChargesTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, MonetaryCurrency currency) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfBounceChargesWrittenOff() != null)
                .map(LoanRepaymentScheduleInstallment::getSelfBounceChargesWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }

    public BigDecimal calculatePartnerPenalShareAmountTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerPenaltyChargesCharged().doubleValue() > 0)
                .map(LoanRepaymentScheduleInstallment::getPartnerPenaltyChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total.setScale(0,RoundingMode.HALF_UP);
    }
    public BigDecimal calculatePartnerBounceShareAmountTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerBounceChargesCharged().doubleValue() > 0)
                .map(LoanRepaymentScheduleInstallment::getPartnerBounceChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total.setScale(0,RoundingMode.HALF_UP);
    }

    public BigDecimal calculatePartnerPenalChargesRepaidTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerPenaltyChargesPaid() != null)
                .map(LoanRepaymentScheduleInstallment::getPartnerPenaltyChargesPaid)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculatePartnerBounceChargesRepaidTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerBounceChargesPaid() != null)
                .map(LoanRepaymentScheduleInstallment::getPartnerBounceChargesPaid)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }

    public BigDecimal calculatePartnerPenalWaivedChargesTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerPenaltyChargesWaived() != null)
                .map(LoanRepaymentScheduleInstallment::getPartnerPenaltyChargesWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculatePartnerBounceWaivedChargesTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerBounceChargesWaived() != null)
                .map(LoanRepaymentScheduleInstallment::getPartnerBounceChargesWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }

    public BigDecimal calculatePartnerPenalWrittenOffChargesTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerPenaltyChargesWrittenOff() != null)
                .map(LoanRepaymentScheduleInstallment::getPartnerPenaltyChargesWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculatePartnerBounceWrittenOffChargesTotal(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerBounceChargesWrittenOff() != null)
                .map(LoanRepaymentScheduleInstallment::getPartnerBounceChargesWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }

    public BigDecimal calculateTotalChargesRepaidAtForeclosure(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return   repaymentScheduleInstallments.stream().map(LoanRepaymentScheduleInstallment::getFeeChargesCharged).reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateAhocCharge(Collection<LoanCharge> loanCharges) {
        BigDecimal amount = BigDecimal.ZERO;

         for(LoanCharge loanCharge : loanCharges){
             if(loanCharge.isAdhocChargeCharge()){
                 amount =amount.add(loanCharge.getAmount());
             }
         }
     return amount;
    }

    public Money calculateAdhocChargesRepaid(Collection<LoanCharge> loanCharges, MonetaryCurrency currency) {

         Money amount = Money.zero(currency);
        for(LoanCharge loanCharge : loanCharges){
            if(loanCharge.isAdhocChargeCharge()){
                amount = loanCharge.getAmount(currency);
            }
        }
        return amount;
    }

    public BigDecimal calculateTotalChargeDisbursement(List<LoanCharge> charges) {

        BigDecimal total = BigDecimal.ZERO;

        for(LoanCharge loanCharge : charges){
            if(loanCharge.isDisbursementCharge() && loanCharge.getAmountPaid().doubleValue() > 0){
                total = total.add(loanCharge.getAmountPaid());
            }
        }
        return total;
    }

    public BigDecimal calculateTotalSelfChargesRepaidAtDisbursement(List<LoanCharge> charges) {

        BigDecimal total =BigDecimal.ZERO;
        if (charges == null) {
            return total;
        }
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isPenaltyCharge() && loanCharge.getSelfShareAmount()!=null  && !loanCharge.getCharge().isForeclosureCharge()) {
                total = total.add(loanCharge.getSelfShareAmount());
            }
        }
        return total;
    }

    public BigDecimal calculateTotalSelfChargeDisbursement(List<LoanCharge> charges) {

        BigDecimal total = BigDecimal.ZERO;

        for(LoanCharge loanCharge : charges){
            if(loanCharge.isDisbursementCharge() && loanCharge.getSelfShareAmount()!=null){
                total = total.add(loanCharge.getSelfShareAmount());
            }
        }
        return total;
    }

    public BigDecimal calculateTotalSelfChargesRepaidAtForeclosure(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        return repaymentScheduleInstallments.stream().map(LoanRepaymentScheduleInstallment::getSelfFeeChargesCharged).reduce(BigDecimal.ZERO,BigDecimal::add);

    }

    public BigDecimal calculateTotalPartnerChargesRepaidAtDisbursement(List<LoanCharge> charges) {

        BigDecimal total =BigDecimal.ZERO;
        if (charges == null) {
            return total;
        }
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isPenaltyCharge() && loanCharge.getPartnerShareAmount() !=null  && !loanCharge.getCharge().isForeclosureCharge()) {
                total = total.add(loanCharge.getPartnerShareAmount());
            }
        }
        return total;
    }

    public BigDecimal calculateTotalPartnerChargeDisbursement(List<LoanCharge> charges) {
        BigDecimal total = BigDecimal.ZERO;

        for(LoanCharge loanCharge : charges){
            if(loanCharge.isDisbursementCharge() && loanCharge.getPartnerShareAmount()!=null){
                total = total.add(loanCharge.getPartnerShareAmount());
            }
        }
        return total;
    }

    public BigDecimal calculateTotalPartnerChargesRepaidAtForeclosure(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {
        return repaymentScheduleInstallments.stream().map(LoanRepaymentScheduleInstallment::getPartnerPenaltyChargesCharged).reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateSelfAhocCharge(Collection<LoanCharge> loanCharges) {

        BigDecimal amount = BigDecimal.ZERO;

        for(LoanCharge loanCharge : loanCharges){
            if(loanCharge.isAdhocChargeCharge()){
                amount =amount.add(loanCharge.getSelfShareAmount());
            }
        }
        return amount;
    }

    public BigDecimal calculatePartnerAhocCharge(Collection<LoanCharge> loanCharges) {

        BigDecimal amount = BigDecimal.ZERO;

        for(LoanCharge loanCharge : loanCharges){
            if(loanCharge.isAdhocChargeCharge()){
                amount =amount.add(loanCharge.getPartnerShareAmount());
            }
        }
        return amount;
    }

    public BigDecimal calculateTotalPartnerGstDisbursement(List<LoanCharge> charges) {
      return   charges.stream().filter(loanCharge -> loanCharge.isDisbursementCharge() && loanCharge.getPartnerGst().doubleValue()>0 ).map(LoanCharge::getPartnerGst).reduce(BigDecimal.ZERO,BigDecimal::add);

    }

    public BigDecimal calculateTotalSelfGstDisbursement(List<LoanCharge> charges) {

        return  charges.stream().filter(loanCharge -> loanCharge.isDisbursementCharge() && loanCharge.getSelfGst().doubleValue()>0).map(LoanCharge::getSelfGst).reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public BigDecimal calculateTotalGstDisbursement(List<LoanCharge> charges) {
        BigDecimal total = BigDecimal.ZERO;

        for(LoanCharge loanCharge : charges){
            if(loanCharge.isDisbursementCharge()  && loanCharge.getTotalGst().doubleValue()>0
                    && (loanCharge.isActive() && !loanCharge.isCoolingOffReversed() )){
                total = total.add(loanCharge.getTotalGst());
            }
        }
        return total;
    }
    public BigDecimal calculateTotalBounceChargesRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getBounceChargesPaid().doubleValue() > 0)
                .map(LoanRepaymentScheduleInstallment::getBounceChargesPaid)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculateTotalBounceChargesWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getBounceChargesWaived().doubleValue() > 0)
                .map(LoanRepaymentScheduleInstallment::getBounceChargesWaived)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculateTotalBounceChargesWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {


        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getBounceChargesWrittenOff()!=null)
                .map(LoanRepaymentScheduleInstallment::getPenaltyChargesWrittenOff)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total;
    }
    public BigDecimal calculateBouncePenaltyChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

        final  BigDecimal total = repaymentScheduleInstallments.stream().filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getBounceChargesCharged() != null)
                .map(LoanRepaymentScheduleInstallment::getBounceChargesCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return total.setScale(0, RoundingMode.HALF_UP);
    }
}
