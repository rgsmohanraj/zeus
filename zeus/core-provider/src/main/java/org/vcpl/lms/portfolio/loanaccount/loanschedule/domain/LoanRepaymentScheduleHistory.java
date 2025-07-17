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
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.vcpl.lms.useradministration.domain.AppUser;

@Entity
@NoArgsConstructor
@Setter
@Getter
@Table(name = "m_loan_repayment_schedule_history")
public class LoanRepaymentScheduleHistory extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @OneToOne(optional = true)
    @JoinColumn(name = "loan_reschedule_request_id")
    private LoanRescheduleRequest loanRescheduleRequest;

    @Column(name = "installment", nullable = false)
    private Integer installmentNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "fromdate", nullable = true)
    private Date fromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "duedate", nullable = false)
    private Date dueDate;

    @Column(name = "principal_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal principal;

    @Column(name = "principal_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalWrittenOff;

    @Column(name = "interest_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestCharged;

    @Column(name = "interest_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestPaid;

    @Column(name = "interest_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestWaived;

    @Column(name = "interest_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestWrittenOff;

    @Column(name = "accrual_interest_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestAccrued;

    @Column(name = "reschedule_interest_portion", scale = 6, precision = 19, nullable = true)
    private BigDecimal rescheduleInterestPortion;

    @Column(name = "fee_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesCharged;

    @Column(name = "fee_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesPaid;

    @Column(name = "fee_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesWrittenOff;

    @Column(name = "fee_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesWaived;

    @Column(name = "accrual_fee_charges_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeAccrued;

    @Column(name = "penalty_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyCharges;

    @Column(name = "penalty_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesPaid;

    @Column(name = "penalty_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesWrittenOff;

    @Column(name = "penalty_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesWaived;

    @Column(name = "accrual_penalty_charges_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyAccrued;

    @Column(name = "total_paid_in_advance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal totalPaidInAdvance;

    @Column(name = "total_paid_late_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal totalPaidLate;

    @Column(name = "completed_derived", nullable = false)
    private boolean obligationsMet;
    @Temporal(TemporalType.DATE)
    @Column(name = "created_date")
    private Date createdOnDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "obligations_met_on_date")
    private Date obligationsMetOnDate;

    @Column(name = "recalculated_interest_component", nullable = false)
    private boolean recalculatedInterestComponent;

    @Column(name = "dpd", nullable = false)
    private Integer daysPastDue;

    @Column(name = "self_principal_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPrincipal;

    @Column(name = "partner_principal_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPrincipal;

    @Column(name = "self_interest_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfInterestCharged;

    @Column(name = "partner_interest_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerInterestCharged;

    @Column(name = "self_due", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfDue;

    @Column(name = "partner_due", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerDue;

    @Column(name = "principal_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalCompleted;

    @Column(name = "self_principal_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPrincipalCompleted;

    @Column(name = "self_principal_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPrincipalWrittenOff;

    @Column(name = "self_interest_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfInterestPaid;

    @Column(name = "self_interest_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfInterestWaived;

    @Column(name = "self_interest_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfInterestWrittenOff;

    @Column(name = "self_accrual_interest_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfInterestAccrued;

    @Column(name = "self_reschedule_interest_portion", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfRescheduleInterestPortion;

    @Column(name = "self_fee_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfFeeChargesCharged;

    @Column(name = "self_fee_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfFeeChargesPaid;

    @Column(name = "self_fee_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfFeeChargesWrittenOff;

    @Column(name = "self_fee_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfFeeChargesWaived;

    @Column(name = "self_accrual_fee_charges_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfFeeAccrued;

    @Column(name = "self_penalty_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPenaltyCharges;

    @Column(name = "self_penalty_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPenaltyChargesPaid;

    @Column(name = "self_penalty_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPenaltyChargesWrittenOff;

    @Column(name = "self_penalty_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPenaltyChargesWaived;

    @Column(name = "self_accrual_penalty_charges_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPenaltyAccrued;

//    @Column(name = "self_total_paid_in_advance_derived", scale = 6, precision = 19, nullable = true)
//    private BigDecimal selfTotalPaidInAdvance;

    @Column(name = "self_total_paid_late_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfTotalPaidLate;

    @Column(name = "partner_principal_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPrincipalCompleted;

    @Column(name = "partner_principal_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPrincipalWrittenOff;

    @Column(name = "partner_interest_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerInterestPaid;

    @Column(name = "partner_interest_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerInterestWaived;

    @Column(name = "partner_interest_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerInterestWrittenOff;

    @Column(name = "partner_accrual_interest_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerInterestAccrued;

    @Column(name = "partner_reschedule_interest_portion", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerRescheduleInterestPortion;

    @Column(name = "partner_fee_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerFeeChargesCharged;

    @Column(name = "partner_fee_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerFeeChargesPaid;

    @Column(name = "partner_fee_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerFeeChargesWrittenOff;

    @Column(name = "partner_fee_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerFeeChargesWaived;

    @Column(name = "partner_accrual_fee_charges_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerFeeAccrued;

    @Column(name = "partner_penalty_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPenaltyCharges;

    @Column(name = "partner_penalty_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPenaltyChargesPaid;

    @Column(name = "partner_penalty_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPenaltyChargesWrittenOff;

    @Column(name = "partner_penalty_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPenaltyChargesWaived;

    @Column(name = "partner_accrual_penalty_charges_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPenaltyAccrued;

//    @Column(name = "partner_total_paid_in_advance_derived", scale = 6, precision = 19, nullable = true)
//    private BigDecimal partnerTotalPaidInAdvance;

    @Column(name = "partner_total_paid_late_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerTotalPaidLate;

    @Column(name = "principal_outstanding", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalOutstanding;

    @Column(name = "self_principal_outstanding", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPrincipalOutstanding;

    @Column(name = "partner_principal_outstanding", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPrincipalOutstanding;

    @Column(name = "due", scale = 6, precision = 19, nullable = true)
    private BigDecimal due;

    @Column(name = "dpd_history", nullable = true)
    private Integer dpdHistory;

    @Temporal(TemporalType.DATE)
    @Column(name = "dpd_tilldate", nullable = true)
    private Date dpdLastRunOn;

    @ManyToOne
    @JoinColumn(name = "createdby_id")
    private AppUser createdByUser;

    @ManyToOne
    @JoinColumn(name = "lastmodifiedby_id")
    private AppUser lastModifiedByUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "lastmodified_date")
    private Date lastModifiedOnDate;

    @Column(name = "version")
    private Integer version;

    @Column(name = "dpd_bucket", nullable = true)
    private String dpdBucket;

    @Column(name = "dpd_bucket_history", nullable = true)
    private String dpdBucketHistory;

    @Column(name = "interest_principal_appropriated_on_date", scale = 6, precision = 19, nullable = true)
    private Date interestPrincipalAppropriatedOnDate;

    @Column(name = "self_interest_principal_appropriated_on_date", scale = 6, precision = 19, nullable = true)
    private Date selfInterestPrincipalAppropriatedOnDate;

    @Column(name = "partner_interest_principal_appropriated_on_date", scale = 6, precision = 19, nullable = true)
    private Date partnerInterestPrincipalAppropriatedOnDate;
    /**
     * LoanRepaymentScheduleHistory constructor
     **/
//    protected LoanRepaymentScheduleHistory() {}

    /**
     * LoanRepaymentScheduleHistory constructor
     **/
    private LoanRepaymentScheduleHistory(final Loan loan, final LoanRescheduleRequest loanRescheduleRequest,
            final Integer installmentNumber, final Date fromDate, final Date dueDate, final BigDecimal principal,
            final BigDecimal interestCharged, final BigDecimal feeChargesCharged, final BigDecimal penaltyCharges, final Date createdOnDate,
            final AppUser createdByUser, final AppUser lastModifiedByUser, final Date lastModifiedOnDate, final Integer version,final BigDecimal selfPrincipal,final BigDecimal partnerPrincipal,
                                         final BigDecimal selfInterestCharged,final BigDecimal partnerInterestCharged,final BigDecimal selfDue,final BigDecimal partnerDue,
                                         final Date obligationsMetOnDate, final Integer daysPastDue, final Integer dpdHistory, final String dpdBucket,final String dpdHistoryBucket) {

        this.loan = loan;
        this.loanRescheduleRequest = loanRescheduleRequest;
        this.installmentNumber = installmentNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.principal = principal;
        this.interestCharged = interestCharged;
        this.feeChargesCharged = feeChargesCharged;
        this.penaltyCharges = penaltyCharges;
        this.createdOnDate = createdOnDate;
        this.createdByUser = createdByUser;
        this.lastModifiedByUser = lastModifiedByUser;
        this.lastModifiedOnDate = lastModifiedOnDate;
        this.version = version;
        this.selfPrincipal = selfPrincipal;
        this.partnerPrincipal = partnerPrincipal;
        this.selfInterestCharged = selfInterestCharged;
        this.partnerInterestCharged = partnerInterestCharged;
        this.selfDue = selfDue;
        this.partnerDue = partnerDue;
        this.obligationsMetOnDate = obligationsMetOnDate;
        this.daysPastDue = daysPastDue;
        this.dpdHistory = dpdHistory;
        this.dpdBucket = dpdBucket;
        this.dpdBucketHistory = dpdHistoryBucket;
    }

    /**
     * @return an instance of the LoanRepaymentScheduleHistory class
     **/
    public static LoanRepaymentScheduleHistory instance(final Loan loan, final LoanRescheduleRequest loanRescheduleRequest,
            final Integer installmentNumber, final Date fromDate, final Date dueDate, final BigDecimal principal,
            final BigDecimal interestCharged, final BigDecimal feeChargesCharged, final BigDecimal penaltyCharges, final Date createdOnDate,
            final AppUser createdByUser, final AppUser lastModifiedByUser, final Date lastModifiedOnDate, final Integer version, final BigDecimal selfPrincipal, final BigDecimal partnerPrincipal,final BigDecimal selfInterestCharged ,
                                                        final BigDecimal partnerInterestCharged, final BigDecimal selfDue,final BigDecimal partnerDue,
                                                        final Date obligationsMetOnDate, final Integer daysPastDue, final Integer dpdHistory,final String dpdBucket,
                                                        final String dpdHistoryBucket) {
        return new LoanRepaymentScheduleHistory(loan, loanRescheduleRequest, installmentNumber, fromDate, dueDate, principal,
                interestCharged, feeChargesCharged, penaltyCharges, createdOnDate, createdByUser, lastModifiedByUser, lastModifiedOnDate,
                version,selfPrincipal,partnerPrincipal,selfInterestCharged,partnerInterestCharged,selfDue,partnerDue, obligationsMetOnDate,
                daysPastDue, dpdHistory, dpdBucket, dpdHistoryBucket);

    }

}
