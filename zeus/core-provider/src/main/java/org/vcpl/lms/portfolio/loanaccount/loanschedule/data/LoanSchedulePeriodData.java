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
package org.vcpl.lms.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import lombok.Getter;
import org.vcpl.lms.infrastructure.core.service.DateUtils;

/**
 * Immutable data object that represents a period of a loan schedule.
 *
 */

@Getter
public final class LoanSchedulePeriodData {

    private final Integer period;
    private final LocalDate fromDate;
    private final LocalDate dueDate;
    private final LocalDate obligationsMetOnDate;
    private final Boolean complete;
    private final Integer daysInPeriod;
    private final BigDecimal principalDisbursed;
    private final BigDecimal principalOriginalDue;
    private final BigDecimal principalDue;
    private final BigDecimal principalPaid;
    private final BigDecimal principalWrittenOff;
    private final BigDecimal principalOutstanding;
    private final BigDecimal principalLoanBalanceOutstanding;
    private final BigDecimal selfPrincipalLoanBalanceOutstanding;
    private final BigDecimal partnerPrincipalLoanBalanceOutstanding;

    private final BigDecimal selfPrincipalAmount;
    private final BigDecimal partnerPrincipalAmount;

    @SuppressWarnings("unused")
    private final BigDecimal interestOriginalDue;
    private final BigDecimal interestDue;
    private final BigDecimal interestPaid;
    private final BigDecimal interestWaived;
    private final BigDecimal interestWrittenOff;
    private final BigDecimal interestOutstanding;
    private final BigDecimal feeChargesDue;
    private final BigDecimal feeChargesPaid;
    private final BigDecimal feeChargesWaived;
    private final BigDecimal feeChargesWrittenOff;
    private final BigDecimal feeChargesOutstanding;
    private final BigDecimal penaltyChargesDue;
    private final BigDecimal penaltyChargesPaid;
    private final BigDecimal penaltyChargesWaived;
    private final BigDecimal penaltyChargesWrittenOff;
    private final BigDecimal penaltyChargesOutstanding;
    private final BigDecimal selfPenaltyChargesOutstanding;
    private final BigDecimal partnerPenaltyChargesOutstanding;

    private final BigDecimal selfFeeChargesDue;
    private final BigDecimal selfFeeChargesPaid;
    private final BigDecimal selfFeeChargesWaived;
    private final BigDecimal selfFeeChargesWrittenOff;
    private final BigDecimal selfFeeChargesOutstanding;

    private final BigDecimal partnerFeeChargesDue;
    private final BigDecimal partnerFeeChargesPaid;
    private final BigDecimal partnerFeeChargesWaived;
    private final BigDecimal partnerFeeChargesWrittenOff;
    private final BigDecimal partnerFeeChargesOutstanding;

    @SuppressWarnings("unused")
    private final BigDecimal totalOriginalDueForPeriod;
    private final BigDecimal totalDueForPeriod;
    private final BigDecimal totalSelfDueForPeriod;
    private final BigDecimal totalPartnerDueForPeriod;
    private final BigDecimal totalPaidForPeriod;
    private final BigDecimal totalSelfPaidForPeriod;
    private final BigDecimal totalPartnerPaidForPeriod;
    private final BigDecimal totalPaidInAdvanceForPeriod;
    //private final BigDecimal totalSelfPaidInAdvanceForPeriod;
    //private final BigDecimal totalPartnerPaidInAdvanceForPeriod;
    private final BigDecimal totalPaidLateForPeriod;
    private final BigDecimal totalSelfPaidLateForPeriod;
    private final BigDecimal totalPartnerPaidLateForPeriod;

    private final BigDecimal totalWaivedForPeriod;
    private final BigDecimal totalWrittenOffForPeriod;
    private final BigDecimal totalOutstandingForPeriod;
    private final BigDecimal totalSelfOutstandingForPeriod;
    private final BigDecimal totalPartnerOutstandingForPeriod;
    private final BigDecimal totalOverdue;
    private final BigDecimal totalActualCostOfLoanForPeriod;
    private final BigDecimal totalInstallmentAmountForPeriod;

    private final BigDecimal selfPrincipal;
    private final BigDecimal partnerPrincipal;
    private final BigDecimal selfInterestCharged;
    private final BigDecimal partnerInterestCharged;

    private final Integer daysPastDue;
    private final BigDecimal selfPenaltyCharges;
    private final BigDecimal selfPenaltyChargesPaid;
    private final BigDecimal selfPenaltyChargesWrittenOff;
    private final BigDecimal selfPenaltyChargesWaived;
    private final BigDecimal partnerPenaltyCharges;
    private final BigDecimal partnerPenaltyChargesPaid;
    private final BigDecimal partnerPenaltyChargesWrittenOff;
    private final BigDecimal partnerPenaltyChargesWaived;


    private final BigDecimal selfPrincipalPaid;
    private final BigDecimal selfInterestPaid;
    private final BigDecimal partnerPrincipalPaid;
    private final BigDecimal partnerInterestPaid;
    private final String dpdBucket;

    public static LoanSchedulePeriodData disbursementOnlyPeriod(final LocalDate disbursementDate, final BigDecimal principalDisbursed,
                                                                final BigDecimal feeChargesDueAtTimeOfDisbursement, final boolean isDisbursed,
                                                                final BigDecimal selfPrincipalAmount,final BigDecimal PartnerPrincipalAmount,
                                                                final Integer daysPastDue,final BigDecimal selfFeeChargesDueAtTimeOfDisbursement,
                                                                final BigDecimal partnerFeeChargesDueAtTimeOfDisbursement) {
        final Integer periodNumber = null;
        final LocalDate from = null;
        return new LoanSchedulePeriodData(periodNumber, from, disbursementDate, principalDisbursed, feeChargesDueAtTimeOfDisbursement,
                isDisbursed,selfPrincipalAmount,PartnerPrincipalAmount, daysPastDue, null,null,
                null,null,null,null,null,
                null,selfFeeChargesDueAtTimeOfDisbursement,partnerFeeChargesDueAtTimeOfDisbursement,null);
    }

    public static LoanSchedulePeriodData repaymentOnlyPeriod(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
                                                             final BigDecimal principalDue, final BigDecimal principalOutstanding, final BigDecimal interestDueOnPrincipalOutstanding,
                                                             final BigDecimal feeChargesDueForPeriod, final BigDecimal penaltyChargesDueForPeriod, final BigDecimal totalDueForPeriod,
                                                             final BigDecimal totalInstallmentAmountForPeriod,final BigDecimal selfPrincipal,final BigDecimal partnerPrincipal,
                                                             final BigDecimal selfInterestCharged,final BigDecimal partnerInterestCharged,final BigDecimal totalSelfDueForPeriod,
                                                             final BigDecimal totalPartnerDueForPeriod, final Integer daysPastDue,final String dpdBucket) {

        return new LoanSchedulePeriodData(periodNumber, fromDate, dueDate, principalDue, principalOutstanding,
                interestDueOnPrincipalOutstanding, feeChargesDueForPeriod, penaltyChargesDueForPeriod, totalDueForPeriod,
                totalInstallmentAmountForPeriod,selfPrincipal,partnerPrincipal,selfInterestCharged,partnerInterestCharged,
                totalSelfDueForPeriod,totalPartnerDueForPeriod, daysPastDue, null,null,null,null,
                null,null,null,null,dpdBucket);
    }

    public static LoanSchedulePeriodData repaymentPeriodWithPayments(@SuppressWarnings("unused") final Long loanId,
                                                                     final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate, final LocalDate obligationsMetOnDate,
                                                                     final boolean complete, final BigDecimal principalOriginalDue, final BigDecimal principalPaid,
                                                                     final BigDecimal principalWrittenOff, final BigDecimal principalOutstanding, final BigDecimal outstandingPrincipalBalanceOfLoan,
                                                                     final BigDecimal interestDueOnPrincipalOutstanding, final BigDecimal interestPaid, final BigDecimal interestWaived,
                                                                     final BigDecimal interestWrittenOff, final BigDecimal interestOutstanding, final BigDecimal feeChargesDue,
                                                                     final BigDecimal feeChargesPaid, final BigDecimal feeChargesWaived, final BigDecimal feeChargesWrittenOff,
                                                                     final BigDecimal feeChargesOutstanding, final BigDecimal penaltyChargesDue, final BigDecimal penaltyChargesPaid,
                                                                     final BigDecimal penaltyChargesWaived, final BigDecimal penaltyChargesWrittenOff, final BigDecimal penaltyChargesOutstanding,
                                                                     final BigDecimal totalDueForPeriod, final BigDecimal totalPaid, final BigDecimal totalPaidInAdvanceForPeriod,
                                                                     final BigDecimal totalPaidLateForPeriod, final BigDecimal totalWaived, final BigDecimal totalWrittenOff,
                                                                     final BigDecimal totalOutstanding, final BigDecimal totalActualCostOfLoanForPeriod,
                                                                     final BigDecimal totalInstallmentAmountForPeriod,final BigDecimal selfPrincipal,final BigDecimal partnerPrincipal,final BigDecimal selfInterestCharged,final BigDecimal partnerInterestCharged,final BigDecimal totalSelfDueForPeriod,final BigDecimal totalPartnerDueForPeriod,
                                                                     final BigDecimal selfPrincipalLoanBalanceOutstanding,final BigDecimal partnerPrincipalLoanBalanceOutstanding,final BigDecimal totalSelfPaid,
                                                                     final BigDecimal totalPartnerPaid,
                                                                     final BigDecimal totalSelfPaidLateForPeriod,final BigDecimal totalPartnerPaidLateForPeriod,final BigDecimal totalSelfOutstanding,
                                                                     final BigDecimal totalPartnerOutstanding, final Integer daysPastDue, final BigDecimal selfPenaltyCharges, final BigDecimal selfPenaltyChargesPaid,
                                                                     final BigDecimal selfPenaltyChargesWrittenOff, final BigDecimal selfPenaltyChargesWaived, final BigDecimal partnerPenaltyCharges,
                                                                     final BigDecimal partnerPenaltyChargesPaid, final BigDecimal partnerPenaltyChargesWrittenOff,
                                                                     final BigDecimal partnerPenaltyChargesWaived,final BigDecimal selfFeeChargesOutstanding,final BigDecimal partnerFeeChargesOutstanding,
                                                                     final BigDecimal selfFeeChargesExpectedDue, final BigDecimal partnerFeeChargesExpectedDue,final BigDecimal selfFeeChargesPaid,
                                                                     final BigDecimal partnerFeeChargesPaid,final BigDecimal selfFeeChargesWaived, final BigDecimal partnerFeeChargesWaived,
                                                                     final BigDecimal selfFeeChargesWrittenOff,final BigDecimal partnerFeeChargesWrittenOff,final BigDecimal selfPenaltyChargesOutstanding,final BigDecimal partnerPenaltyChargesOutstanding, final String dpdBucket) {

        return new LoanSchedulePeriodData(periodNumber, fromDate, dueDate, obligationsMetOnDate, complete, principalOriginalDue,
                principalPaid, principalWrittenOff, principalOutstanding, outstandingPrincipalBalanceOfLoan,
                interestDueOnPrincipalOutstanding, interestPaid, interestWaived, interestWrittenOff, interestOutstanding, feeChargesDue,
                feeChargesPaid, feeChargesWaived, feeChargesWrittenOff, feeChargesOutstanding, penaltyChargesDue, penaltyChargesPaid,
                penaltyChargesWaived, penaltyChargesWrittenOff, penaltyChargesOutstanding, totalDueForPeriod, totalPaid,
                totalPaidInAdvanceForPeriod, totalPaidLateForPeriod, totalWaived, totalWrittenOff, totalOutstanding,
                totalActualCostOfLoanForPeriod, totalInstallmentAmountForPeriod,selfPrincipal,partnerPrincipal,selfInterestCharged,partnerInterestCharged,totalSelfDueForPeriod,totalPartnerDueForPeriod,selfPrincipalLoanBalanceOutstanding,partnerPrincipalLoanBalanceOutstanding,
				 totalSelfPaid,totalPartnerPaid,totalSelfPaidLateForPeriod,
                totalPartnerPaidLateForPeriod,totalSelfOutstanding,totalPartnerOutstanding,daysPastDue, selfPenaltyCharges,selfPenaltyChargesPaid,selfPenaltyChargesWrittenOff,
                selfPenaltyChargesWaived, partnerPenaltyCharges,partnerPenaltyChargesPaid,partnerPenaltyChargesWrittenOff,partnerPenaltyChargesWaived,selfFeeChargesOutstanding,
                partnerFeeChargesOutstanding,selfFeeChargesExpectedDue,partnerFeeChargesExpectedDue, selfFeeChargesPaid, partnerFeeChargesPaid, selfFeeChargesWaived,
                partnerFeeChargesWaived, selfFeeChargesWrittenOff, partnerFeeChargesWrittenOff,selfPenaltyChargesOutstanding,partnerPenaltyChargesOutstanding,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,dpdBucket);

    }

    public static LoanSchedulePeriodData withPaidDetail(final LoanSchedulePeriodData loanSchedulePeriodData, final boolean complete,
                                                        final BigDecimal principalPaid, final BigDecimal interestPaid, final BigDecimal feeChargesPaid,
                                                        final BigDecimal penaltyChargesPaid, final Integer daysPastDue,final BigDecimal selfPrincipalPaid ,final BigDecimal selfInterestPaid,
                                                        final BigDecimal partnerPrincipalPaid,final BigDecimal partnerInterestPaid) {

        return new LoanSchedulePeriodData(loanSchedulePeriodData.period, loanSchedulePeriodData.fromDate, loanSchedulePeriodData.dueDate,
                loanSchedulePeriodData.obligationsMetOnDate, complete, loanSchedulePeriodData.principalOriginalDue, principalPaid,
                loanSchedulePeriodData.principalWrittenOff, loanSchedulePeriodData.principalOutstanding,
                loanSchedulePeriodData.principalLoanBalanceOutstanding, loanSchedulePeriodData.interestDue, interestPaid,
                loanSchedulePeriodData.interestWaived, loanSchedulePeriodData.interestWrittenOff,
                loanSchedulePeriodData.interestOutstanding, loanSchedulePeriodData.feeChargesDue, feeChargesPaid,
                loanSchedulePeriodData.feeChargesWaived, loanSchedulePeriodData.feeChargesWrittenOff,
                loanSchedulePeriodData.feeChargesOutstanding, loanSchedulePeriodData.penaltyChargesDue, penaltyChargesPaid,
                loanSchedulePeriodData.penaltyChargesWaived, loanSchedulePeriodData.penaltyChargesWrittenOff,
                loanSchedulePeriodData.penaltyChargesOutstanding, loanSchedulePeriodData.totalDueForPeriod,
                loanSchedulePeriodData.totalPaidForPeriod, loanSchedulePeriodData.totalPaidInAdvanceForPeriod,
                loanSchedulePeriodData.totalPaidLateForPeriod, loanSchedulePeriodData.totalWaivedForPeriod,
                loanSchedulePeriodData.totalWrittenOffForPeriod, loanSchedulePeriodData.totalOutstandingForPeriod,
                loanSchedulePeriodData.totalActualCostOfLoanForPeriod, loanSchedulePeriodData.totalInstallmentAmountForPeriod,
                loanSchedulePeriodData.selfPrincipal,loanSchedulePeriodData.partnerPrincipal,loanSchedulePeriodData.selfInterestCharged,
                loanSchedulePeriodData.partnerInterestCharged,loanSchedulePeriodData.totalSelfDueForPeriod,loanSchedulePeriodData.totalPartnerDueForPeriod,
                loanSchedulePeriodData.selfPrincipalLoanBalanceOutstanding, loanSchedulePeriodData.partnerPrincipalLoanBalanceOutstanding,
				loanSchedulePeriodData.totalSelfPaidForPeriod,loanSchedulePeriodData.totalPartnerPaidForPeriod,
                loanSchedulePeriodData.totalSelfPaidLateForPeriod,loanSchedulePeriodData.totalPartnerPaidLateForPeriod,
                loanSchedulePeriodData.totalSelfOutstandingForPeriod,loanSchedulePeriodData.totalPartnerOutstandingForPeriod,
                daysPastDue, null,null,null,null, null,null,null,null,loanSchedulePeriodData.selfFeeChargesOutstanding,
                loanSchedulePeriodData.partnerFeeChargesOutstanding,loanSchedulePeriodData.selfFeeChargesDue,loanSchedulePeriodData.partnerFeeChargesDue,
                loanSchedulePeriodData.selfFeeChargesPaid,loanSchedulePeriodData.partnerFeeChargesPaid,loanSchedulePeriodData.selfFeeChargesWaived,
                loanSchedulePeriodData.partnerFeeChargesWaived,loanSchedulePeriodData.selfFeeChargesWrittenOff,loanSchedulePeriodData.partnerFeeChargesWrittenOff,
                loanSchedulePeriodData.selfPenaltyChargesOutstanding,loanSchedulePeriodData.partnerPenaltyChargesOutstanding,selfPrincipalPaid,selfInterestPaid,partnerPrincipalPaid,partnerInterestPaid, null);

    }

    /*
     * constructor used for creating period on loan schedule that is only a disbursement (typically first period)
     */
    private LoanSchedulePeriodData(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
                                   final BigDecimal principalDisbursed, final BigDecimal chargesDueAtTimeOfDisbursement, final boolean isDisbursed,
                                   final BigDecimal selfPrincipalAmount, final BigDecimal partnerPrincipalAmount, Integer daysPastDue,
                                   final BigDecimal selfPenaltyCharges, final BigDecimal selfPenaltyChargesPaid, final BigDecimal selfPenaltyChargesWrittenOff,
                                   final BigDecimal selfPenaltyChargesWaived, final BigDecimal partnerPenaltyCharges, final BigDecimal partnerPenaltyChargesPaid,
                                   final BigDecimal partnerPenaltyChargesWrittenOff,final BigDecimal partnerPenaltyChargesWaived, final BigDecimal selfChargesDueAtTimeOfDisbursement,
                                   final BigDecimal partnerChargesDueAtTimeOfDisbursement, final String dpdBucket) {
        this.period = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.daysPastDue = daysPastDue;
        this.obligationsMetOnDate = null;
        this.complete = null;
        if (fromDate != null) {
            this.daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(this.fromDate, this.dueDate));
        } else {
            this.daysInPeriod = null;
        }
        this.principalDisbursed = principalDisbursed;
        this.selfPrincipalAmount = selfPrincipalAmount;
        this.partnerPrincipalAmount=partnerPrincipalAmount;

        this.principalOriginalDue = null;
        this.principalDue = null;
        this.principalPaid = null;
        this.principalWrittenOff = null;
        this.principalOutstanding = null;
        this.principalLoanBalanceOutstanding = principalDisbursed;
        this.selfPrincipalLoanBalanceOutstanding = selfPrincipalAmount;
        this.partnerPrincipalLoanBalanceOutstanding = partnerPrincipalAmount;


        this.interestOriginalDue = null;
        this.interestDue = null;
        this.interestPaid = null;
        this.interestWaived = null;
        this.interestWrittenOff = null;
        this.interestOutstanding = null;

        this.selfPrincipal = null;
        this.partnerPrincipal = null;
        this.selfInterestCharged = null;
        this.partnerInterestCharged = null;

        this.feeChargesDue = chargesDueAtTimeOfDisbursement;
        if (isDisbursed) {
            this.feeChargesPaid = chargesDueAtTimeOfDisbursement;
            this.feeChargesWaived = null;
            this.feeChargesWrittenOff = null;
            this.feeChargesOutstanding = null;
        } else {
            this.feeChargesPaid = null;
            this.feeChargesWaived = null;
            this.feeChargesWrittenOff = null;
            this.feeChargesOutstanding = chargesDueAtTimeOfDisbursement;
        }

        this.selfFeeChargesDue = selfChargesDueAtTimeOfDisbursement;
        if (isDisbursed) {

            this.selfFeeChargesPaid = selfChargesDueAtTimeOfDisbursement;
            this.selfFeeChargesWaived = null;
            this.selfFeeChargesWrittenOff = null;
            this.selfFeeChargesOutstanding = null;
        }
        else{

            this.selfFeeChargesPaid =null ;
            this.selfFeeChargesWaived = null;
            this.selfFeeChargesWrittenOff = null;
            this.selfFeeChargesOutstanding = selfChargesDueAtTimeOfDisbursement;

        }

        this.partnerFeeChargesDue =partnerChargesDueAtTimeOfDisbursement;
        if (isDisbursed) {

            this.partnerFeeChargesPaid = partnerChargesDueAtTimeOfDisbursement;
            this.partnerFeeChargesWaived = null;
            this.partnerFeeChargesWrittenOff = null;
            this.partnerFeeChargesOutstanding = null;
        }else{

            this.partnerFeeChargesPaid = null;
            this.partnerFeeChargesWaived = null;
            this.partnerFeeChargesWrittenOff = null;
            this.partnerFeeChargesOutstanding = partnerChargesDueAtTimeOfDisbursement;

        }



        this.penaltyChargesDue = null;
        this.penaltyChargesPaid = null;
        this.penaltyChargesWaived = null;
        this.penaltyChargesWrittenOff = null;
        this.penaltyChargesOutstanding = null;
        this.selfPenaltyChargesOutstanding=null;
        this.partnerPenaltyChargesOutstanding = null;

        this.totalOriginalDueForPeriod = chargesDueAtTimeOfDisbursement;
        this.totalDueForPeriod = chargesDueAtTimeOfDisbursement;
        this.totalSelfDueForPeriod = selfChargesDueAtTimeOfDisbursement;
        this.totalPartnerDueForPeriod = partnerChargesDueAtTimeOfDisbursement;

        this.totalSelfPaidForPeriod = this.selfFeeChargesPaid;
        this.totalPartnerPaidForPeriod = this.partnerFeeChargesPaid;

        this.totalPaidForPeriod = this.feeChargesPaid;
        this.totalPaidInAdvanceForPeriod = null;
        //this.totalSelfPaidInAdvanceForPeriod = null;
        //this.totalPartnerPaidInAdvanceForPeriod = null;
        this.totalPaidLateForPeriod = null;
        this.totalSelfPaidLateForPeriod = null;
        this.totalPartnerPaidLateForPeriod = null;

        this.totalWaivedForPeriod = null;
        this.totalWrittenOffForPeriod = null;
        this.totalOutstandingForPeriod = this.feeChargesOutstanding;
        this.totalSelfOutstandingForPeriod = this.selfFeeChargesOutstanding;
        this.totalPartnerOutstandingForPeriod = this.partnerFeeChargesOutstanding;
        this.totalActualCostOfLoanForPeriod = this.feeChargesDue;
        this.totalInstallmentAmountForPeriod = null;
        if (dueDate.isBefore(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()))) {
            this.totalOverdue = this.totalOutstandingForPeriod;
        } else {
            this.totalOverdue = null;
        }
        this.selfPenaltyCharges = selfPenaltyCharges;
        this.selfPenaltyChargesPaid = selfPenaltyChargesPaid;
        this.selfPenaltyChargesWrittenOff = selfPenaltyChargesWrittenOff;
        this.selfPenaltyChargesWaived = selfPenaltyChargesWaived;
        this.partnerPenaltyCharges = partnerPenaltyCharges;
        this.partnerPenaltyChargesPaid = partnerPenaltyChargesPaid;
        this.partnerPenaltyChargesWrittenOff = partnerPenaltyChargesWrittenOff;
        this.partnerPenaltyChargesWaived = partnerPenaltyChargesWaived;

        this.selfPrincipalPaid = null;
        this.selfInterestPaid =null;
        this.partnerPrincipalPaid =null;
        this.partnerInterestPaid = null;
        this.dpdBucket = dpdBucket;
    }

    /*
     * used for repayment only period when creating an empty loan schedule for preview etc
     */
    private LoanSchedulePeriodData(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
                                   final BigDecimal principalOriginalDue, final BigDecimal principalOutstanding,
                                   final BigDecimal interestDueOnPrincipalOutstanding, final BigDecimal feeChargesDueForPeriod,
                                   final BigDecimal penaltyChargesDueForPeriod, final BigDecimal totalDueForPeriod,
                                   final BigDecimal totalInstallmentAmountForPeriod, final BigDecimal selfPrincipal,
                                   final BigDecimal partnerPrincipal, final BigDecimal selfInterestCharged, final BigDecimal partnerInterestCharged,
                                   final BigDecimal totalSelfDueForPeriod, final BigDecimal totalPartnerDueForPeriod, Integer daysPastDue,
                                   BigDecimal selfPenaltyCharges, BigDecimal selfPenaltyChargesPaid,
                                   BigDecimal selfPenaltyChargesWrittenOff, BigDecimal selfPenaltyChargesWaived,
                                   BigDecimal partnerPenaltyCharges, BigDecimal partnerPenaltyChargesPaid,
                                   BigDecimal partnerPenaltyChargesWrittenOff, BigDecimal partnerPenaltyChargesWaived,final String dpdBucket) {
        this.period = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.daysPastDue = daysPastDue;
        this.obligationsMetOnDate = null;
        this.complete = null;
        if (fromDate != null) {
            this.daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(this.fromDate, this.dueDate));
        } else {
            this.daysInPeriod = null;
        }
        this.principalDisbursed = null;
        this.selfPrincipalAmount = null;
        this.partnerPrincipalAmount = null;

        this.principalOriginalDue = principalOriginalDue;
        this.principalDue = principalOriginalDue;
        this.principalPaid = null;
        this.principalWrittenOff = null;
        this.principalOutstanding = principalOriginalDue;
        this.principalLoanBalanceOutstanding = principalOutstanding;
        this.selfPrincipalLoanBalanceOutstanding = selfPrincipalAmount;
        this.partnerPrincipalLoanBalanceOutstanding = partnerPrincipalAmount;



        this.interestOriginalDue = interestDueOnPrincipalOutstanding;
        this.interestDue = interestDueOnPrincipalOutstanding;
        this.interestPaid = null;
        this.interestWaived = null;
        this.interestWrittenOff = null;
        this.interestOutstanding = interestDueOnPrincipalOutstanding;

        this.feeChargesDue = feeChargesDueForPeriod;
        this.feeChargesPaid = null;
        this.feeChargesWaived = null;
        this.feeChargesWrittenOff = null;
        this.feeChargesOutstanding = null;

        this.selfFeeChargesDue = null;
        this.selfFeeChargesPaid = null;
        this.selfFeeChargesWaived = null;
        this.selfFeeChargesWrittenOff = null;
        this.selfFeeChargesOutstanding = null;


        this.partnerFeeChargesDue = null;
        this.partnerFeeChargesPaid = null;
        this.partnerFeeChargesWaived = null;
        this.partnerFeeChargesWrittenOff = null;
        this.partnerFeeChargesOutstanding = null;


        this.penaltyChargesDue = penaltyChargesDueForPeriod;
        this.penaltyChargesPaid = null;
        this.penaltyChargesWaived = null;
        this.penaltyChargesWrittenOff = null;
        this.penaltyChargesOutstanding = null;
        this.selfPenaltyChargesOutstanding = null;
        this.partnerPenaltyChargesOutstanding = null;
        this.totalOriginalDueForPeriod = totalDueForPeriod;
        this.totalDueForPeriod = totalDueForPeriod;
        this.totalSelfDueForPeriod = totalSelfDueForPeriod;
        this.totalPartnerDueForPeriod = totalPartnerDueForPeriod;
        this.totalPaidForPeriod = BigDecimal.ZERO;
        this.totalPaidInAdvanceForPeriod = null;
      //  this.totalSelfPaidInAdvanceForPeriod = null;
     //   this.totalPartnerPaidInAdvanceForPeriod = null;
        this.totalPaidLateForPeriod = null;
        this.totalSelfPaidLateForPeriod = null;
        this.totalPartnerPaidLateForPeriod = null;

        this.totalWaivedForPeriod = null;
        this.totalWrittenOffForPeriod = null;
        this.totalOutstandingForPeriod = totalDueForPeriod;
        this.totalSelfOutstandingForPeriod = totalSelfDueForPeriod;
        this.totalPartnerOutstandingForPeriod = totalPartnerDueForPeriod;
        this.totalActualCostOfLoanForPeriod = interestDueOnPrincipalOutstanding.add(feeChargesDueForPeriod);
        this.totalInstallmentAmountForPeriod = totalInstallmentAmountForPeriod;
        this.selfPrincipal = selfPrincipal;
        this.partnerPrincipal = partnerPrincipal;
        this.selfInterestCharged = selfInterestCharged;
        this.partnerInterestCharged = partnerInterestCharged;

        this.totalSelfPaidForPeriod = BigDecimal.ZERO;
        this.totalPartnerPaidForPeriod = BigDecimal.ZERO;




        if (dueDate.isBefore(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()))) {
            this.totalOverdue = this.totalOutstandingForPeriod;
        } else {
            this.totalOverdue = null;
        }

        this.selfPenaltyCharges = selfPenaltyCharges;
        this.selfPenaltyChargesPaid = selfPenaltyChargesPaid;
        this.selfPenaltyChargesWrittenOff = selfPenaltyChargesWrittenOff;
        this.selfPenaltyChargesWaived = selfPenaltyChargesWaived;
        this.partnerPenaltyCharges = partnerPenaltyCharges;
        this.partnerPenaltyChargesPaid = partnerPenaltyChargesPaid;
        this.partnerPenaltyChargesWrittenOff = partnerPenaltyChargesWrittenOff;
        this.partnerPenaltyChargesWaived = partnerPenaltyChargesWaived;

        this.selfPrincipalPaid = null;
        this.selfInterestPaid = null;
        this.partnerPrincipalPaid = null;
        this.partnerInterestPaid = null;
        this.dpdBucket = dpdBucket;
    }

    /*
     * Used for creating loan schedule periods with full information on expected principal, interest & charges along
     * with what portion of each is paid.
     */
    private LoanSchedulePeriodData(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
                                   final LocalDate obligationsMetOnDate, final boolean complete, final BigDecimal principalOriginalDue,
                                   final BigDecimal principalPaid, final BigDecimal principalWrittenOff, final BigDecimal principalOutstanding,
                                   final BigDecimal principalLoanBalanceOutstanding, final BigDecimal interestDueOnPrincipalOutstanding,
                                   final BigDecimal interestPaid, final BigDecimal interestWaived, final BigDecimal interestWrittenOff,
                                   final BigDecimal interestOutstanding, final BigDecimal feeChargesDue, final BigDecimal feeChargesPaid,
                                   final BigDecimal feeChargesWaived, final BigDecimal feeChargesWrittenOff, final BigDecimal feeChargesOutstanding,
                                   final BigDecimal penaltyChargesDue, final BigDecimal penaltyChargesPaid, final BigDecimal penaltyChargesWaived,
                                   final BigDecimal penaltyChargesWrittenOff, final BigDecimal penaltyChargesOutstanding, final BigDecimal totalDueForPeriod,
                                   final BigDecimal totalPaid, final BigDecimal totalPaidInAdvanceForPeriod, final BigDecimal totalPaidLateForPeriod,
                                   final BigDecimal totalWaived, final BigDecimal totalWrittenOff, final BigDecimal totalOutstanding,
                                   final BigDecimal totalActualCostOfLoanForPeriod, final BigDecimal totalInstallmentAmountForPeriod, final BigDecimal selfPrincipal, final BigDecimal partnerPrincipal, final BigDecimal selfInterestCharged, final BigDecimal partnerInterestCharged, final BigDecimal totalSelfDueForPeriod, final BigDecimal totalPartnerDueForPeriod,
                                   final BigDecimal selfPrincipalLoanBalanceOutstanding, final BigDecimal partnerPrincipalLoanBalanceOutstanding, final BigDecimal totalSelfPaid,
                                   final BigDecimal totalPartnerPaid,
                                   final BigDecimal totalSelfPaidLateForPeriod, final BigDecimal totalPartnerPaidLateForPeriod, final BigDecimal totalSelfOutstanding,
                                   final BigDecimal totalPartnerOutstanding, Integer daysPastDue, BigDecimal selfPenaltyCharges,
                                   BigDecimal selfPenaltyChargesPaid, BigDecimal selfPenaltyChargesWrittenOff, BigDecimal selfPenaltyChargesWaived,
                                   BigDecimal partnerPenaltyCharges, BigDecimal partnerPenaltyChargesPaid, BigDecimal partnerPenaltyChargesWrittenOff,BigDecimal partnerPenaltyChargesWaived,final BigDecimal selfFeeChargesOutstanding,final BigDecimal partnerFeeChargesOutstanding,
                                   final BigDecimal selfFeeChargesExpectedDue, final BigDecimal partnerFeeChargesExpectedDue,final BigDecimal selfFeeChargesPaid,
                                   final BigDecimal partnerFeeChargesPaid,final BigDecimal selfFeeChargesWaived, final BigDecimal partnerFeeChargesWaived,
                                   final BigDecimal selfFeeChargesWrittenOff,final BigDecimal partnerFeeChargesWrittenOff,final BigDecimal selfPenaltyChargesOutstanding,
                                   final BigDecimal partnerPenaltyChargesOutstanding,final BigDecimal selfPrincipalPaid,final BigDecimal selfInterestPaid,final BigDecimal partnerPrincipalPaid,
                                   final BigDecimal partnerInterestPaid, final String dpdBucket) {
        this.period = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.obligationsMetOnDate = obligationsMetOnDate;
        this.complete = complete;
        this.daysPastDue = daysPastDue;
        if (fromDate != null) {
            this.daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(this.fromDate, this.dueDate));
        } else {
            this.daysInPeriod = null;
        }
        this.principalDisbursed = null;
        this.selfPrincipalAmount = null;
        this.partnerPrincipalAmount = null;



        this.principalOriginalDue = principalOriginalDue;
        this.principalDue = principalOriginalDue;
        this.principalPaid = principalPaid;
        this.principalWrittenOff = principalWrittenOff;
        this.principalOutstanding = principalOutstanding;
        this.principalLoanBalanceOutstanding = principalLoanBalanceOutstanding;
        this.selfPrincipalLoanBalanceOutstanding = selfPrincipalLoanBalanceOutstanding;
        this.partnerPrincipalLoanBalanceOutstanding = partnerPrincipalLoanBalanceOutstanding;



        this.interestOriginalDue = interestDueOnPrincipalOutstanding;
        this.interestDue = interestDueOnPrincipalOutstanding;
        this.interestPaid = interestPaid;
        this.interestWaived = interestWaived;
        this.interestWrittenOff = interestWrittenOff;
        this.interestOutstanding = interestOutstanding;

        this.feeChargesDue = feeChargesDue;
        this.feeChargesPaid = feeChargesPaid;
        this.feeChargesWaived = feeChargesWaived;
        this.feeChargesWrittenOff = feeChargesWrittenOff;
        this.feeChargesOutstanding = feeChargesOutstanding;

        this.selfFeeChargesDue = selfFeeChargesExpectedDue;
        this.selfFeeChargesPaid = selfFeeChargesPaid;
        this.selfFeeChargesWaived = feeChargesWaived;
        this.selfFeeChargesWrittenOff = selfFeeChargesWrittenOff;
        this.selfFeeChargesOutstanding = selfFeeChargesOutstanding;


        this.partnerFeeChargesDue = partnerFeeChargesExpectedDue;
        this.partnerFeeChargesPaid = partnerFeeChargesPaid;
        this.partnerFeeChargesWaived = partnerFeeChargesWaived;
        this.partnerFeeChargesWrittenOff = partnerFeeChargesWrittenOff;
        this.partnerFeeChargesOutstanding = partnerFeeChargesOutstanding;


        this.penaltyChargesDue = penaltyChargesDue;
        this.penaltyChargesPaid = penaltyChargesPaid;
        this.penaltyChargesWaived = penaltyChargesWaived;
        this.penaltyChargesWrittenOff = penaltyChargesWrittenOff;
        this.penaltyChargesOutstanding = penaltyChargesOutstanding;
        this.selfPenaltyChargesOutstanding = selfPenaltyChargesOutstanding;
        this.partnerPenaltyChargesOutstanding = partnerPenaltyChargesOutstanding;

        this.totalOriginalDueForPeriod = totalDueForPeriod;
        this.totalDueForPeriod = totalDueForPeriod;
        this.totalSelfDueForPeriod = totalSelfDueForPeriod;
        this.totalPartnerDueForPeriod = totalPartnerDueForPeriod;
        this.totalPaidForPeriod = totalPaid;
        this.totalPaidInAdvanceForPeriod = totalPaidInAdvanceForPeriod;
       // this.totalSelfPaidInAdvanceForPeriod = totalSelfPaidInAdvanceForPeriod;
      //  this.totalPartnerPaidInAdvanceForPeriod = totalPartnerPaidInAdvanceForPeriod;
        this.totalPaidLateForPeriod = totalPaidLateForPeriod;
        this.totalSelfPaidLateForPeriod = totalSelfPaidLateForPeriod;
        this.totalPartnerPaidLateForPeriod = totalPartnerPaidLateForPeriod;

        this.totalWaivedForPeriod = totalWaived;
        this.totalWrittenOffForPeriod = totalWrittenOff;
        this.totalOutstandingForPeriod = totalOutstanding;
        this.totalSelfOutstandingForPeriod = totalSelfOutstanding;
        this.totalPartnerOutstandingForPeriod = totalPartnerOutstanding;
        this.totalActualCostOfLoanForPeriod = totalActualCostOfLoanForPeriod;
        this.totalInstallmentAmountForPeriod = totalInstallmentAmountForPeriod;
        this.selfPrincipal = selfPrincipal;
        this.partnerPrincipal = partnerPrincipal;
        this.selfInterestCharged = selfInterestCharged;
        this.partnerInterestCharged = partnerInterestCharged;


        this.totalSelfPaidForPeriod = totalSelfPaid;
        this.totalPartnerPaidForPeriod = totalPartnerPaid;



        if (dueDate.isBefore(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()))) {
            this.totalOverdue = this.totalOutstandingForPeriod;
        } else {
            this.totalOverdue = null;
        }
        this.selfPenaltyCharges = selfPenaltyCharges;
        this.selfPenaltyChargesPaid = selfPenaltyChargesPaid;
        this.selfPenaltyChargesWrittenOff = selfPenaltyChargesWrittenOff;
        this.selfPenaltyChargesWaived = selfPenaltyChargesWaived;
        this.partnerPenaltyCharges = partnerPenaltyCharges;
        this.partnerPenaltyChargesPaid = partnerPenaltyChargesPaid;
        this.partnerPenaltyChargesWrittenOff = partnerPenaltyChargesWrittenOff;
        this.partnerPenaltyChargesWaived = partnerPenaltyChargesWaived;

        this.selfPrincipalPaid =selfPrincipalPaid;
        this.selfInterestPaid = selfInterestPaid;
        this.partnerPrincipalPaid =partnerPrincipalPaid;
        this.partnerInterestPaid =partnerInterestPaid;
        this.dpdBucket = dpdBucket;
    }

    private BigDecimal defaultToZeroIfNull(final BigDecimal possibleNullValue) {
        BigDecimal value = BigDecimal.ZERO;
        if (possibleNullValue != null) {
            value = possibleNullValue;
        }
        return value;
    }

    public Integer periodNumber() {
        return this.period;
    }

    public LocalDate periodFromDate() {
        return this.fromDate;
    }

    public Integer daysPastDue() {
        return this.daysPastDue;
    }

    public LocalDate periodDueDate() {
        return this.dueDate;
    }

    public Integer daysInPeriod() {
        return this.daysInPeriod;
    }

    public BigDecimal principalDisbursed() {
        return defaultToZeroIfNull(this.principalDisbursed);
    }

    public BigDecimal selfPrincipalAmount() {
        return defaultToZeroIfNull(this.selfPrincipalAmount);
    }

    public BigDecimal partnerPrincipalAmount() {
        return defaultToZeroIfNull(this.partnerPrincipalAmount);
    }


    public BigDecimal principalDue() {
        return defaultToZeroIfNull(this.principalDue);
    }

    public BigDecimal principalPaid() {
        return defaultToZeroIfNull(this.principalPaid);
    }

    public BigDecimal principalWrittenOff() {
        return defaultToZeroIfNull(this.principalWrittenOff);
    }

    public BigDecimal principalOutstanding() {
        return defaultToZeroIfNull(this.principalOutstanding);
    }

    public BigDecimal interestDue() {
        return defaultToZeroIfNull(this.interestDue);
    }

    public BigDecimal interestPaid() {
        return defaultToZeroIfNull(this.interestPaid);
    }

    public BigDecimal interestWaived() {
        return defaultToZeroIfNull(this.interestWaived);
    }

    public BigDecimal interestWrittenOff() {
        return defaultToZeroIfNull(this.interestWrittenOff);
    }

    public BigDecimal interestOutstanding() {
        return defaultToZeroIfNull(this.interestOutstanding);
    }

    public BigDecimal feeChargesDue() {
        return defaultToZeroIfNull(this.feeChargesDue);
    }

    public BigDecimal selfFeeChargesDue() {
        return defaultToZeroIfNull(this.selfFeeChargesDue);
    }

    public BigDecimal partnerFeeChargesDue() {
        return defaultToZeroIfNull(this.partnerFeeChargesDue);
    }

    public BigDecimal feeChargesWaived() {
        return defaultToZeroIfNull(this.feeChargesWaived);
    }

    public BigDecimal feeChargesWrittenOff() {
        return defaultToZeroIfNull(this.feeChargesWrittenOff);
    }

    public BigDecimal feeChargesPaid() {
        return defaultToZeroIfNull(this.feeChargesPaid);
    }
    public BigDecimal selfFeeChargesPaid() {
        return defaultToZeroIfNull(this.selfFeeChargesPaid);
    }
    public BigDecimal partnerFeeChargesPaid() {
        return defaultToZeroIfNull(this.partnerFeeChargesPaid);
    }

    public BigDecimal feeChargesOutstanding() {
        return defaultToZeroIfNull(this.feeChargesOutstanding);
    }
    public BigDecimal selfFeeChargesOutstanding() {
        return defaultToZeroIfNull(this.selfFeeChargesOutstanding);
    }
    public BigDecimal partnerFeeChargesOutstanding() {
        return defaultToZeroIfNull(this.partnerFeeChargesOutstanding);
    }

    public BigDecimal penaltyChargesDue() {
        return defaultToZeroIfNull(this.penaltyChargesDue);
    }

    public BigDecimal penaltyChargesWaived() {
        return defaultToZeroIfNull(this.penaltyChargesWaived);
    }

    public BigDecimal penaltyChargesWrittenOff() {
        return defaultToZeroIfNull(this.penaltyChargesWrittenOff);
    }

    public BigDecimal penaltyChargesPaid() {
        return defaultToZeroIfNull(this.penaltyChargesPaid);
    }

    public BigDecimal penaltyChargesOutstanding() {
        return defaultToZeroIfNull(this.penaltyChargesOutstanding);
    }

    public BigDecimal totalOverdue() {
        return defaultToZeroIfNull(this.totalOverdue);
    }

    public BigDecimal principalLoanBalanceOutstanding() {
        return this.principalLoanBalanceOutstanding;
    }

    public BigDecimal selfPrincipalLoanBalanceOutstanding() {
        return this.selfPrincipalLoanBalanceOutstanding;
    }


    public BigDecimal partnerPrincipalLoanBalanceOutstanding() {
        return this.partnerPrincipalLoanBalanceOutstanding;
    }


    public Boolean getComplete() {
        return this.complete;
    }

    public BigDecimal selfPrincipal() {
        return defaultToZeroIfNull(this.selfPrincipal);
    }

    public BigDecimal partnerPrincipal() {
        return defaultToZeroIfNull(this.partnerPrincipal);
    }

    public BigDecimal selfInterestCharged() {
        return defaultToZeroIfNull(this.selfInterestCharged);
    }

    public BigDecimal partnerInterestCharged() {
        return defaultToZeroIfNull(this.partnerInterestCharged);
    }

    public BigDecimal totalSelfDueForPeriod() {
        return defaultToZeroIfNull(this.totalSelfDueForPeriod);
    }

    public BigDecimal totalPartnerDueForPeriod() {
        return defaultToZeroIfNull(this.totalPartnerDueForPeriod);
    }

    public String dpdBucket() {
        return this.dpdBucket;
    }


}
