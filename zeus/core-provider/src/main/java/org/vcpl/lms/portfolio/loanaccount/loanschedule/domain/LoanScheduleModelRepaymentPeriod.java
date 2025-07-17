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
package org.vcpl.lms.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

/**
 * Domain representation of a Loan Schedule Repayment Period (not used for persistence)
 */
public final class LoanScheduleModelRepaymentPeriod implements LoanScheduleModelPeriod {

    private final int periodNumber;
    private final LocalDate fromDate;
    private final LocalDate dueDate;
    private Money principalDue;
    private final Money outstandingLoanBalance;
    private Money interestDue;
    private Money feeChargesDue;
    private Money penaltyChargesDue;
    private Money totalDue;
    private final boolean recalculatedInterestComponent;
    private final Set<LoanInterestRecalcualtionAdditionalDetails> loanCompoundingDetails = new HashSet<>();
    private boolean isEMIFixedSpecificToInstallment = false;
    BigDecimal rescheduleInterestPortion;
    private Money selfPrincipal;
    private Money partnerPrincipal;
    private Money selfInterestCharged;
    private Money partnerInterestCharged;
    private Money selfDue;
    private Money partnerDue;

    public int getPeriodNumber() {
        return periodNumber;
    }

    public static LoanScheduleModelRepaymentPeriod repayment(final int periodNumber, final LocalDate startDate,
                                                             final LocalDate scheduledDueDate, final Money principalDue, final Money outstandingLoanBalance, final Money interestDue,
                                                             final Money feeChargesDue, final Money penaltyChargesDue, final Money totalDue, boolean recalculatedInterestComponent, final Money selfPrincipal, final Money partnerPrincipal, final Money selfInterestCharged, final Money partnerInterestCharged, final Money selfDue, final Money partnerDue) {

        return new LoanScheduleModelRepaymentPeriod(periodNumber, startDate, scheduledDueDate, principalDue, outstandingLoanBalance,
                interestDue, feeChargesDue, penaltyChargesDue, totalDue, recalculatedInterestComponent,selfPrincipal,partnerPrincipal,selfInterestCharged,partnerInterestCharged,selfDue,partnerDue);
    }

    public LoanScheduleModelRepaymentPeriod(final int periodNumber, final LocalDate fromDate, final LocalDate dueDate,
                                            final Money principalDue, final Money outstandingLoanBalance, final Money interestDue, final Money feeChargesDue,
                                            final Money penaltyChargesDue, final Money totalDue, final boolean recalculatedInterestComponent,final Money selfPrincipal,final Money partnerPrincipal,final Money selfInterestCharged,final Money partnerInterestCharged,final Money selfDue,final Money partnerDue) {
        this.periodNumber = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.principalDue = principalDue;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.interestDue = interestDue;
        this.feeChargesDue = feeChargesDue;
        this.penaltyChargesDue = penaltyChargesDue;
        this.totalDue = totalDue;
        this.recalculatedInterestComponent = recalculatedInterestComponent;
        this.selfPrincipal=selfPrincipal;
        this.partnerPrincipal=partnerPrincipal;
        this.selfInterestCharged = selfInterestCharged;
        this.partnerInterestCharged = partnerInterestCharged;
        this.selfDue=selfDue;
        this.partnerDue=partnerDue;

    }

    @Override
    public LoanSchedulePeriodData toData() {
        return LoanSchedulePeriodData.repaymentOnlyPeriod(this.periodNumber, this.fromDate, this.dueDate, this.principalDue.getAmount(),
                this.outstandingLoanBalance.getAmount(), this.interestDue.getAmount(), this.feeChargesDue.getAmount(),
                this.penaltyChargesDue.getAmount(), this.totalDue.getAmount(), this.principalDue.plus(this.interestDue).getAmount(),
                this.selfPrincipal.getAmount(),this.partnerPrincipal.getAmount(),this.selfInterestCharged.getAmount(),
                this.partnerInterestCharged.getAmount(),this.selfDue.getAmount(),this.partnerDue.getAmount(), null,null);
    }

    @Override
    public boolean isRepaymentPeriod() {
        return true;
    }

    @Override
    public Integer periodNumber() {
        return this.periodNumber;
    }

    @Override
    public LocalDate periodFromDate() {
        return this.fromDate;
    }

    @Override
    public LocalDate periodDueDate() {
        return this.dueDate;
    }

    @Override
    public BigDecimal principalDue() {
        BigDecimal value = null;
        if (this.principalDue != null) {
            value = this.principalDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal selfPrincipal() {
        BigDecimal value = null;
        if (this.selfPrincipal != null) {
            value = this.selfPrincipal.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal partnerPrincipal() {
        BigDecimal value = null;
        if (this.partnerPrincipal != null) {
            value = this.partnerPrincipal.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal interestDue() {
        BigDecimal value = null;
        if (this.interestDue != null) {
            value = this.interestDue.getAmount();
        }

        return value;
    }

//    @Override
//    public BigDecimal selfPrincipal() {
//        BigDecimal value = null;
//        if (this.selfPrincipal != null) {
//            value = this.selfPrincipal.getAmount();
//        }
//
//        return value;
//    }


    @Override
    public BigDecimal selfInterestCharged() {
        BigDecimal value = null;
        if (this.selfInterestCharged != null) {
            value = this.selfInterestCharged.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal partnerInterestCharged() {
        BigDecimal value = null;
        if (this.partnerInterestCharged != null) {
            value = this.partnerInterestCharged.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal selfDue() {
        BigDecimal value = null;
        if (this.selfDue != null) {
            value = this.selfDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal partnerDue() {
        BigDecimal value = null;
        if (this.partnerDue != null) {
            value = this.partnerDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal feeChargesDue() {
        BigDecimal value = null;
        if (this.feeChargesDue != null) {
            value = this.feeChargesDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal penaltyChargesDue() {
        BigDecimal value = null;
        if (this.penaltyChargesDue != null) {
            value = this.penaltyChargesDue.getAmount();
        }

        return value;
    }

    @Override
    public void addLoanCharges(BigDecimal feeCharge, BigDecimal penaltyCharge) {
        this.feeChargesDue = this.feeChargesDue.plus(feeCharge);
        this.penaltyChargesDue = this.penaltyChargesDue.plus(penaltyCharge);
        this.totalDue = this.totalDue.plus(feeCharge).plus(penaltyCharge);
    }

    @Override
    public void addPrincipalAmount(final Money principalDue) {
        this.principalDue = this.principalDue.plus(principalDue);
        this.totalDue = this.totalDue.plus(principalDue);
    }

    @Override
    public void addSelfPrincipal(final Money selfPrincipal) {
        this.selfPrincipal = this.selfPrincipal.plus(selfPrincipal);
        this.totalDue = this.totalDue.plus(selfPrincipal);
    }

    @Override
    public void addPartnerPrincipal(final Money partnerPrincipal) {
        this.partnerPrincipal = this.partnerPrincipal.plus(partnerPrincipal);
        this.totalDue = this.totalDue.plus(partnerPrincipal);
    }

    @Override
    public boolean isRecalculatedInterestComponent() {
        return this.recalculatedInterestComponent;
    }

    @Override
    public void addInterestAmount(Money interestDue) {
        this.interestDue = this.interestDue.plus(interestDue);
        this.totalDue = this.totalDue.plus(interestDue);
    }

    @Override
    public void addSelfInterestCharged(Money selfInterestCharged) {
        this.selfInterestCharged = this.selfInterestCharged.plus(selfInterestCharged);
        this.totalDue = this.totalDue.plus(selfInterestCharged);
    }


    @Override
    public void addPartnerInterestCharged(Money partnerInterestCharged) {
        this.partnerInterestCharged = this.partnerInterestCharged.plus(partnerInterestCharged);
        this.totalDue = this.totalDue.plus(partnerInterestCharged);
    }

    @Override
    public Set<LoanInterestRecalcualtionAdditionalDetails> getLoanCompoundingDetails() {
        return this.loanCompoundingDetails;
    }

    @Override
    public boolean isEMIFixedSpecificToInstallment() {
        return this.isEMIFixedSpecificToInstallment;
    }

    @Override
    public void setEMIFixedSpecificToInstallmentTrue() {
        this.isEMIFixedSpecificToInstallment = true;
    }

    @Override
    public void setRescheduleInterestPortion(BigDecimal rescheduleInterestPortion) {
        this.rescheduleInterestPortion = rescheduleInterestPortion;
    }

    @Override
    public BigDecimal rescheduleInterestPortion() {
        return this.rescheduleInterestPortion;
    }
}
