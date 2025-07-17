/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import jakarta.persistence.*;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.vcpl.lms.infrastructure.core.domain.AbstractAuditableSequenceCustom;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.foreclosure.ForeclosureUtils;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.vcpl.lms.portfolio.loanaccount.service.DpdBucketService;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.vcpl.lms.useradministration.domain.AppUser;

@Entity
@Setter
@Getter
@Table(name = "m_loan_repayment_schedule")
public final class LoanRepaymentScheduleInstallment extends AbstractAuditableSequenceCustom
        implements Comparable<LoanRepaymentScheduleInstallment> {

    /*
      Unique id generation type is Table, because to sequence order of insertion on one to Many relation'
      allocationSize is 1, beacause each time of insertion the records will be updated in sequence_generator table
     */
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,generator = "m_loan_repayment_schedule")
    @TableGenerator(table = "sequence_generator",allocationSize = 1,name = "m_loan_repayment_schedule")
    private Long id;

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", referencedColumnName = "id")
    private Loan loan;

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

    @Column(name = "principal_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalCompleted;

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
    @Column(name = "obligations_met_on_date")
    private Date obligationsMetOnDate;

    @Column(name = "recalculated_interest_component", nullable = false)
    private boolean recalculatedInterestComponent;

    @Column(name = "dpd", nullable = false)
    private Integer daysPastDue;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "loanRepaymentScheduleInstallment")
    private Set<LoanInterestRecalcualtionAdditionalDetails> loanCompoundingDetails = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "loanRepaymentScheduleInstallment")
    private Set<PostDatedChecks> postDatedChecks;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "installment")
    private Set<LoanInstallmentCharge> installmentCharges = new HashSet<>();

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
    @Column(name = "dpd_bucket", nullable = true)
    private String dpdBucket;
    @Column(name = "dpd_bucket_history", nullable = true)
    private String dpdBucketHistory;

    @Column(name = "bounce_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal bounceCharges;

    @Column(name = "bounce_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal bounceChargesPaid;

    @Column(name = "bounce_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal bounceChargesWrittenOff;

    @Column(name = "bounce_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal bounceChargesWaived;

    @Column(name = "self_bounce_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfBounceCharges;

    @Column(name = "self_bounce_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfBounceChargesPaid;

    @Column(name = "self_bounce_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfBounceChargesWrittenOff;

    @Column(name = "self_bounce_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfBounceChargesWaived;

    @Column(name = "partner_bounce_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerBounceCharges;

    @Column(name = "partner_bounce_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerBounceChargesPaid;

    @Column(name = "partner_bounce_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerBounceChargesWrittenOff;

    @Column(name = "partner_bounce_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerBounceChargesWaived;

    @Column(name = "interest_principal_appropriated_on_date", scale = 6, precision = 19, nullable = true)
    private Date interestPrincipalAppropriatedOnDate;

    @Column(name = "self_interest_principal_appropriated_on_date", scale = 6, precision = 19, nullable = true)
    private Date selfInterestPrincipalAppropriatedOnDate;

    @Column(name = "partner_interest_principal_appropriated_on_date", scale = 6, precision = 19, nullable = true)
    private Date partnerInterestPrincipalAppropriatedOnDate;

    LoanRepaymentScheduleInstallment() {
        this.installmentNumber = null;
        this.fromDate = null;
        this.dueDate = null;
        this.obligationsMet = false;
    }

    public LoanRepaymentScheduleInstallment(final Loan loan, final Integer installmentNumber, final LocalDate fromDate,
                                            final LocalDate dueDate, final BigDecimal principal, final BigDecimal interest, final BigDecimal feeCharges,
                                            final BigDecimal penaltyCharges, final boolean recalculatedInterestComponent,
                                            final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails, final BigDecimal rescheduleInterestPortion,
                                            final BigDecimal selfPrincipal, final BigDecimal partnerPrincipal,
                                            final BigDecimal selfInterestCharged, final BigDecimal partnerInterestCharged,
                                            final BigDecimal selfDue, final BigDecimal partnerDue, final Integer daysPastDue, final BigDecimal advanceAmount) {

        this.loan = loan;
        this.installmentNumber = installmentNumber;
        this.fromDate = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.dueDate = Date.from(dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.principal = defaultToNullIfZero(principal);
        this.interestCharged = defaultToNullIfZero(interest);
        this.due = ForeclosureUtils.total(principal, interest);
        this.feeChargesCharged = defaultToNullIfZero(feeCharges);
        this.penaltyCharges = defaultToNullIfZero(penaltyCharges);
        this.obligationsMet = false;
        this.recalculatedInterestComponent = recalculatedInterestComponent;
        this.selfPrincipal = selfPrincipal;
        this.partnerPrincipal = partnerPrincipal;
        this.selfInterestCharged = selfInterestCharged;
        this.partnerInterestCharged = partnerInterestCharged;
        this.selfDue = selfDue;
        this.partnerDue = partnerDue;
        this.daysPastDue = daysPastDue;
        this.dpdBucket = DpdBucketService.getDpdBucketAsString(this.daysPastDue);
        this.totalPaidInAdvance = advanceAmount;


        if (compoundingDetails != null) {
            compoundingDetails.forEach(cd -> cd.setLoanRepaymentScheduleInstallment(this));
        }
        this.loanCompoundingDetails = compoundingDetails;
        this.rescheduleInterestPortion = rescheduleInterestPortion;
    }

    public LoanRepaymentScheduleInstallment(final Loan loan, final Integer installmentNumber, final LocalDate fromDate,
                                            final LocalDate dueDate, final BigDecimal principal, final BigDecimal interest, final BigDecimal feeCharges,
                                            final BigDecimal penaltyCharges, final boolean recalculatedInterestComponent,
                                            final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails, final BigDecimal selfPrincipal,
                                            final BigDecimal partnerPrincipal, final BigDecimal selfInterestCharged, final BigDecimal partnerInterestCharged,
                                            final BigDecimal selfDue, final BigDecimal partnerDue, final Integer daysPastDue) {
        this.loan = loan;
        this.installmentNumber = installmentNumber;
        this.fromDate = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.due = principal.add(interest);
        this.dueDate = Date.from(dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.principal = defaultToNullIfZero(principal);
        this.interestCharged = defaultToNullIfZero(interest);
        this.feeChargesCharged = defaultToNullIfZero(feeCharges);
        this.penaltyCharges = defaultToNullIfZero(penaltyCharges);
        this.obligationsMet = false;
        this.recalculatedInterestComponent = recalculatedInterestComponent;
        this.selfPrincipal = selfPrincipal;
        this.partnerPrincipal = partnerPrincipal;
        this.selfInterestCharged = selfInterestCharged;
        this.partnerInterestCharged = partnerInterestCharged;
        this.selfDue = selfPrincipal.add(selfInterestCharged);
        this.partnerDue = partnerPrincipal.add(partnerInterestCharged);
        this.daysPastDue = daysPastDue;
        this.dpdBucket = DpdBucketService.getDpdBucketAsString(this.daysPastDue);

        if (compoundingDetails != null) {
            compoundingDetails.forEach(cd -> cd.setLoanRepaymentScheduleInstallment(this));
        }
        this.loanCompoundingDetails = compoundingDetails;
    }

    public LoanRepaymentScheduleInstallment(final Loan loan) {
        this.loan = loan;
        this.installmentNumber = null;
        this.fromDate = null;
        this.dueDate = null;
        this.obligationsMet = false;

    }

    public LoanRepaymentScheduleInstallment(Object o, int i, LocalDate installmentStartDate, LocalDate transactionDate, BigDecimal amount, BigDecimal amount1, BigDecimal amount2, BigDecimal amount3, boolean isInterestComponent, BigDecimal selfPrincipal, BigDecimal partnerPrincipal, BigDecimal selfInterestCharged, BigDecimal partnerInterestCharged, final BigDecimal selfDue, final BigDecimal partnerDue) {
        super();
    }

    private BigDecimal defaultToNullIfZero(final BigDecimal value) {
        BigDecimal result = value;
        if (BigDecimal.ZERO.compareTo(value) == 0) {
            result = null;
        }
        return result;
    }

    public Loan getLoan() {
        return this.loan;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }

    public LocalDate getFromDate() {
        LocalDate fromLocalDate = null;
        if (this.fromDate != null) {
            fromLocalDate = LocalDate.ofInstant(this.fromDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }

        return fromLocalDate;
    }

    public void setPostDatedChecksToNull() {
        this.postDatedChecks = null;
    }

    public Set<PostDatedChecks> getPostDatedCheck() {
        return this.postDatedChecks;
    }

    public LocalDate getDueDate() {
        return LocalDate.ofInstant(this.dueDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
    }

    public Money getPrincipal(final MonetaryCurrency currency) {
        return Money.of(currency, this.principal);
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public Money getSelfPrincipal(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPrincipal);
    }

    public Money getPartnerPrincipal(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPrincipal);
    }

    public Money getPrincipalCompleted(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalCompleted);
    }

    public BigDecimal getPrincipalCompleted() {
        return principalCompleted != null ? principalCompleted : BigDecimal.ZERO;
    }

    public BigDecimal getInterestPaid() {
        return interestPaid != null ? interestPaid : BigDecimal.ZERO;
    }

    public void updateLoanRepaymentSchedule(final BigDecimal amountWaived) {
        this.feeChargesWaived = this.feeChargesWaived.subtract(amountWaived);
    }

    public Money getPrincipalWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalWrittenOff);
    }

    public BigDecimal getPrincipalWrittenOff() {
        return principalWrittenOff;
    }

    public Money getPrincipalOutstanding(final MonetaryCurrency currency) {
        final Money principalAccountedFor = getPrincipalCompleted(currency).plus(getPrincipalWrittenOff(currency));
        return getPrincipal(currency).minus(principalAccountedFor);
    }

    public Money getInterestCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestCharged);
    }

    public BigDecimal getInterestCharged() {
        return this.interestCharged != null ? interestCharged : BigDecimal.ZERO;
    }

    public void setInterestCharged(BigDecimal interestCharged) {
        this.interestCharged = interestCharged;
    }

    public void setInterestCharges(Money amount) {
        this.interestCharged = amount.getAmount();

    }

    public Money getSelfInterestCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfInterestCharged);
    }

    public BigDecimal getSelfInterestCharged() {
        return this.selfInterestCharged != null ? selfInterestCharged : BigDecimal.ZERO;
    }

    public Money getPartnerInterestCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerInterestCharged);
    }

    public BigDecimal getPartnerInterestCharged() {
        return this.partnerInterestCharged != null ? partnerInterestCharged : BigDecimal.ZERO;
    }


    public Money getSelfDue(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfDue);
    }

    public Money getPartnerDue(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerDue);
    }


    public Money getInterestPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestPaid);
    }

    public Money getInterestWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestWaived);
    }

    public BigDecimal getInterestWaived() {
        return this.interestWaived != null ? interestWaived : BigDecimal.ZERO;
    }

    public Money getInterestWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestWrittenOff);
    }

    public BigDecimal getInterestWrittenOff() {
        return this.interestWrittenOff != null ? interestWrittenOff : BigDecimal.ZERO;
    }

    public Money getInterestOutstanding(final MonetaryCurrency currency) {
        final Money interestAccountedFor = getInterestPaid(currency).plus(getInterestWaived(currency))
                .plus(getInterestWrittenOff(currency));
        return getInterestCharged(currency).minus(interestAccountedFor);
    }

    public Money getInterestAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestAccrued);
    }

    public Money getFeeChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesCharged);
    }


    public BigDecimal getFeeChargesCharged() {
        return feeChargesCharged != null ? feeChargesCharged : BigDecimal.ZERO;
    }

    public Money getFeeChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesPaid);
    }

    public Money getFeeChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesWaived);
    }

    public Money getFeeChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesWrittenOff);
    }

    public BigDecimal getFeeChargesWrittenOff() {
        return this.feeChargesWrittenOff;
    }

    public Money getFeeChargesOutstanding(final MonetaryCurrency currency) {
        final Money feeChargesAccountedFor = getFeeChargesPaid(currency).plus(getFeeChargesWaived(currency))
                .plus(getFeeChargesWrittenOff(currency));
        return getFeeChargesCharged(currency).minus(feeChargesAccountedFor);
    }

    public Money getFeeAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeAccrued);
    }

    public Money getPenaltyChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyCharges);
    }

    public BigDecimal getPenaltyChargesCharged() {
        return this.penaltyCharges != null ? penaltyCharges : BigDecimal.ZERO;
    }

    public Money getPenaltyChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesPaid);
    }

    public BigDecimal getPenaltyChargesPaid() {
        return this.penaltyChargesPaid != null ? penaltyChargesPaid : BigDecimal.ZERO;
    }

    public Money getPenaltyChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesWaived);
    }

    public BigDecimal getPenaltyChargesWaived() {
        return this.penaltyChargesWaived != null ? penaltyChargesWaived : BigDecimal.ZERO;
    }

    public Money getPenaltyChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesWrittenOff);
    }


    public BigDecimal getPenaltyChargesWrittenOff() {
        return this.penaltyChargesWrittenOff;
    }

    public Money getPenaltyChargesOutstanding(final MonetaryCurrency currency) {
        final Money feeChargesAccountedFor = getPenaltyChargesPaid(currency).plus(getPenaltyChargesWaived(currency))
                .plus(getPenaltyChargesWrittenOff(currency));
        return getPenaltyChargesCharged(currency).minus(feeChargesAccountedFor);
    }

    public Money getPenaltyAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyAccrued);
    }

    public boolean isInterestDue(final MonetaryCurrency currency) {
        return getInterestOutstanding(currency).isGreaterThanZero();
    }

    public Money getTotalPrincipalAndInterest(final MonetaryCurrency currency) {
        return getPrincipal(currency).plus(getInterestCharged(currency));
    }

    public Money getTotalSelfPrincipalAndSelfInterestCharged(final MonetaryCurrency currency) {
        return getSelfPrincipal(currency).plus(getSelfInterestCharged(currency));
    }

    public Money getTotalPartnerPrincipalAndPartnerInterestCharged(final MonetaryCurrency currency) {
        return getPartnerPrincipal(currency).plus(getPartnerInterestCharged(currency));
    }

    public Money getTotalOutstanding(final MonetaryCurrency currency) {
        return getPrincipalOutstanding(currency).plus(getInterestOutstanding(currency)).plus(getFeeChargesOutstanding(currency))
                .plus(getPenaltyChargesOutstanding(currency).plus(getBounceChargesOutstanding(currency)));
    }

    public Money getSelfPrincipalCompleted(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPrincipalCompleted);
    }

    public BigDecimal getSelfPrincipalCompleted() {
        return selfPrincipalCompleted != null ? selfPrincipalCompleted : BigDecimal.ZERO;
    }

    public Money getSelfPrincipalWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPrincipalWrittenOff);
    }

    public Money getSelfInterestPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfInterestPaid);
    }

    public BigDecimal getSelfInterestPaid() {
        return this.selfInterestPaid != null ? selfInterestPaid : BigDecimal.ZERO;
    }

    public Money getSelfInterestWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfInterestWaived);
    }

    public BigDecimal getSelfInterestWaived() {
        return this.selfInterestWaived != null ? selfInterestWaived : BigDecimal.ZERO;
    }

    public Money getSelfInterestWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfInterestWrittenOff);
    }

    public Money getSelfInterestAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfInterestAccrued);
    }

    public BigDecimal getSelfRescheduleInterestPortion() {
        return selfRescheduleInterestPortion;
    }

    public Money getSelfFeeChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfFeeChargesCharged);
    }

    public BigDecimal getSelfFeeChargesCharged() {
        return this.selfFeeChargesCharged != null ? selfFeeChargesCharged : BigDecimal.ZERO;
    }

    public Money getSelfFeeChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfFeeChargesPaid);
    }

    public Money getSelfFeeChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfFeeChargesWrittenOff);
    }

    public BigDecimal getSelfFeeChargesWrittenOff() {
        return this.selfFeeChargesWrittenOff;
    }


    public Money getSelfFeeChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfFeeChargesWaived);
    }

    public Money getSelfFeeAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfFeeAccrued);
    }

    public Money getSelfPenaltyChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPenaltyCharges);
    }

    public BigDecimal getSelfPenaltyChargesCharged() {
        return this.selfPenaltyCharges;
    }

    public BigDecimal getSelfBounceChargesCharged() {
        return this.selfBounceCharges;
    }


    public Money getSelfPenaltyChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPenaltyChargesPaid);
    }

    public BigDecimal getSelfPenaltyChargesPaid() {
        return this.selfPenaltyChargesPaid;
    }


    public Money getSelfPenaltyChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPenaltyChargesWrittenOff);
    }

    public BigDecimal getSelfPenaltyChargesWrittenOff() {
        return this.selfPenaltyChargesWrittenOff;
    }

    public Money getSelfPenaltyChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPenaltyChargesWaived);
    }

    public BigDecimal getSelfPenaltyChargesWaived() {
        return this.selfPenaltyChargesWaived;
    }

    public Money getSelfPenaltyAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPenaltyAccrued);
    }

//    public Money getSelfTotalPaidInAdvance(final MonetaryCurrency currency) {
//        return Money.of(currency, this.selfTotalPaidInAdvance);
//    }

    public Money getAdvanceAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalPaidInAdvance);
    }

    public void setAdvanceAmount(final BigDecimal amount) {
        this.totalPaidInAdvance = getTotalPaidInAdvance().add(amount);
    }


    public Money getSelfTotalPaidLate(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfTotalPaidLate);
    }

    public Money getSelfPrincipalOutstanding(final MonetaryCurrency currency) {
        final Money selfPrincipalAccountedFor = getSelfPrincipalCompleted(currency).plus(getSelfPrincipalWrittenOff(currency));
        return getSelfPrincipal(currency).minus(selfPrincipalAccountedFor);
    }

    public Money getSelfInterestOutstanding(final MonetaryCurrency currency) {
        final Money selfInterestAccountedFor = getSelfInterestPaid(currency).plus(getSelfInterestWaived(currency))
                .plus(getSelfInterestWrittenOff(currency));
        return getSelfInterestCharged(currency).minus(selfInterestAccountedFor);
    }

    public Money getSelfAccruedInterestOutstanding(final MonetaryCurrency currency) {
        final Money selfInterestAccountedFor = getSelfInterestPaid(currency).plus(getSelfInterestWaived(currency))
                .plus(getSelfInterestWrittenOff(currency));
        return getSelfInterestAccrued(currency).minus(selfInterestAccountedFor);
    }

    public Money getSelfFeeChargesOutstanding(final MonetaryCurrency currency) {
        final Money selfFeeChargesAccountedFor = getSelfFeeChargesPaid(currency).plus(getSelfFeeChargesWaived(currency))
                .plus(getSelfFeeChargesWrittenOff(currency));
        return getSelfFeeChargesCharged(currency).minus(selfFeeChargesAccountedFor);
    }

    public Money getSelfPenaltyChargesOutstanding(final MonetaryCurrency currency) {
        final Money selfFeeChargesAccountedFor = getSelfPenaltyChargesPaid(currency).plus(getSelfPenaltyChargesWaived(currency))
                .plus(getSelfPenaltyChargesWrittenOff(currency));
        return getSelfPenaltyChargesCharged(currency).minus(selfFeeChargesAccountedFor);
    }

    public Money getPartnerPrincipalCompleted(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPrincipalCompleted);
    }

    public BigDecimal getPartnerPrincipalCompleted() {
        return partnerPrincipalCompleted != null ? partnerPrincipalCompleted : BigDecimal.ZERO;
    }


    public Money getPartnerPrincipalWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPrincipalWrittenOff);
    }

    public Money getPartnerInterestPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerInterestPaid);
    }

    public BigDecimal getPartnerInterestPaid() {
        return this.partnerInterestPaid != null ? partnerInterestPaid : BigDecimal.ZERO;
    }

    public Money getPartnerInterestWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerInterestWaived);
    }

    public BigDecimal getPartnerInterestWaived() {
        return this.partnerInterestWaived != null ? partnerInterestWaived : BigDecimal.ZERO;
    }

    public Money getPartnerInterestWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerInterestWrittenOff);
    }

    public Money getPartnerInterestAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerInterestAccrued);
    }

    public BigDecimal getPartnerRescheduleInterestPortion() {
        return partnerRescheduleInterestPortion;
    }

    public Money getPartnerFeeChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerFeeChargesCharged);
    }

    public BigDecimal getPartnerFeeChargesCharged() {
        return this.partnerFeeChargesCharged != null ? partnerFeeChargesCharged : BigDecimal.ZERO;
    }

    public Money getPartnerFeeChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerFeeChargesPaid);
    }

    public Money getPartnerFeeChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerFeeChargesWrittenOff);
    }

    public BigDecimal getPartnerFeeChargesWrittenOff() {
        return this.partnerFeeChargesWrittenOff;
    }

    public Money getPartnerFeeChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerFeeChargesWaived);
    }

    public Money getPartnerFeeAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerFeeAccrued);
    }

    public Money getPartnerPenaltyChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPenaltyCharges);
    }

    public BigDecimal getPartnerPenaltyChargesCharged() {
        return this.partnerPenaltyCharges != null ? partnerPenaltyCharges : BigDecimal.ZERO;
    }

    public BigDecimal getPartnerBounceChargesCharged() {
        return this.partnerBounceCharges != null ? partnerBounceCharges : BigDecimal.ZERO;
    }

    public Integer getDaysPastDue() {
        return daysPastDue;
    }

    public void setDaysPastDue(final Integer daysPastDue) {
        this.daysPastDue = daysPastDue;
        this.dpdBucket = DpdBucketService.getDpdBucketAsString(this.daysPastDue);
    }

    public String getDpdBucket() {
        return this.dpdBucket;
    }

    public Money getPartnerPenaltyChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPenaltyChargesPaid);
    }


    public BigDecimal getPartnerPenaltyChargesPaid() {
        return this.partnerPenaltyChargesPaid;
    }


    public Money getPartnerPenaltyChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPenaltyChargesWrittenOff);
    }

    public BigDecimal getPartnerPenaltyChargesWrittenOff() {
        return this.partnerPenaltyChargesWrittenOff;
    }

    public Money getPartnerPenaltyChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPenaltyChargesWaived);
    }

    public BigDecimal getPartnerPenaltyChargesWaived() {
        return this.partnerPenaltyChargesWaived;
    }

    public Money getPartnerPenaltyAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPenaltyAccrued);
    }

//    public Money getPartnerTotalPaidInAdvance(final MonetaryCurrency currency) {
//        return Money.of(currency, this.partnerTotalPaidInAdvance);
//    }

    public Money getPartnerTotalPaidLate(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerTotalPaidLate);
    }

    public Money getPartnerPrincipalOutstanding(final MonetaryCurrency currency) {
        final Money partnerPrincipalAccountedFor = getPartnerPrincipalCompleted(currency).plus(getPartnerPrincipalWrittenOff(currency));
        return getPartnerPrincipal(currency).minus(partnerPrincipalAccountedFor);
    }

    public Money getPartnerInterestOutstanding(final MonetaryCurrency currency) {
        final Money partnerInterestAccountedFor = getPartnerInterestPaid(currency).plus(getPartnerInterestWaived(currency))
                .plus(getPartnerInterestWrittenOff(currency));
        return getPartnerInterestCharged(currency).minus(partnerInterestAccountedFor);
    }

    public Money getPartnerAccruedInterestOutstanding(final MonetaryCurrency currency) {
        final Money partnerInterestAccountedFor = getPartnerInterestPaid(currency).plus(getPartnerInterestWaived(currency))
                .plus(getPartnerInterestWrittenOff(currency));
        return getPartnerInterestAccrued(currency).minus(partnerInterestAccountedFor);
    }

    public Money getPartnerFeeChargesOutstanding(final MonetaryCurrency currency) {
        final Money partnerFeeChargesAccountedFor = getPartnerFeeChargesPaid(currency).plus(getPartnerFeeChargesWaived(currency))
                .plus(getPartnerFeeChargesWrittenOff(currency));
        return getPartnerFeeChargesCharged(currency).minus(partnerFeeChargesAccountedFor);
    }

    public Money getPartnerPenaltyChargesOutstanding(final MonetaryCurrency currency) {
        final Money partnerFeeChargesAccountedFor = getPartnerPenaltyChargesPaid(currency).plus(getPartnerPenaltyChargesWaived(currency))
                .plus(getPartnerPenaltyChargesWrittenOff(currency));
        return getPartnerPenaltyChargesCharged(currency).minus(partnerFeeChargesAccountedFor);
    }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    public boolean isPartlyPaid() {
        return !this.obligationsMet && (this.interestPaid != null || this.feeChargesPaid != null || this.principalCompleted != null);
    }

    public boolean isObligationsMet() {
        return this.obligationsMet;
    }

    public boolean isNotFullyPaidOff() {
        return !this.obligationsMet;
    }

    public BigDecimal getAdvanceAmount() {

        return this.totalPaidInAdvance != null ? this.totalPaidInAdvance : BigDecimal.ZERO;
    }
    @Override
    public int compareTo(LoanRepaymentScheduleInstallment o) {
        return this.installmentNumber.compareTo(o.installmentNumber);
    }

    public boolean isPrincipalNotCompleted(final MonetaryCurrency currency) {
        return !isPrincipalCompleted(currency);
    }

    public boolean isPrincipalCompleted(final MonetaryCurrency currency) {
        return getPrincipalOutstanding(currency).isZero();
    }

    public BigDecimal getBounceChargesWrittenOff() {
        return bounceChargesWrittenOff;
    }

    public void setBounceChargesWrittenOff(BigDecimal bounceChargesWrittenOff) {
        this.bounceChargesWrittenOff = bounceChargesWrittenOff;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public BigDecimal getBounceCharges() {
        return Objects.isNull(bounceCharges) ? BigDecimal.ZERO : bounceCharges;
    }

    public void setBounceCharges(BigDecimal bounceCharges) {
        this.bounceCharges = bounceCharges;
    }

    public void setBounceChargesPaid(BigDecimal bounceChargesPaid) {
        this.bounceChargesPaid = bounceChargesPaid;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public BigDecimal getSelfBounceCharges() {
        return selfBounceCharges;
    }

    public void setSelfBounceCharges(BigDecimal selfBounceCharges) {
        this.selfBounceCharges = selfBounceCharges;
    }

    public BigDecimal getSelfBounceChargesPaid() {
        return selfBounceChargesPaid;
    }

    public void setSelfBounceChargesPaid(BigDecimal selfBounceChargesPaid) {
        this.selfBounceChargesPaid = selfBounceChargesPaid;
    }

    public BigDecimal getSelfBounceChargesWrittenOff() {
        return selfBounceChargesWrittenOff;
    }

    public void setSelfBounceChargesWrittenOff(BigDecimal selfBounceChargesWrittenOff) {
        this.selfBounceChargesWrittenOff = selfBounceChargesWrittenOff;
    }

    public BigDecimal getSelfBounceChargesWaived() {
        return selfBounceChargesWaived;
    }

    public void setSelfBounceChargesWaived(BigDecimal selfBounceChargesWaived) {
        this.selfBounceChargesWaived = selfBounceChargesWaived;
    }

    public BigDecimal getPartnerBounceCharges() {
        return partnerBounceCharges;
    }

    public void setPartnerBounceCharges(BigDecimal partnerBounceCharges) {
        this.partnerBounceCharges = partnerBounceCharges;
    }

    public BigDecimal getPartnerBounceChargesPaid() {
        return partnerBounceChargesPaid;
    }

    public void setPartnerBounceChargesPaid(BigDecimal partnerBounceChargesPaid) {
        this.partnerBounceChargesPaid = partnerBounceChargesPaid;
    }

    public BigDecimal getPartnerBounceChargesWrittenOff() {
        return partnerBounceChargesWrittenOff;
    }

    public void setPartnerBounceChargesWrittenOff(BigDecimal partnerBounceChargesWrittenOff) {
        this.partnerBounceChargesWrittenOff = partnerBounceChargesWrittenOff;
    }

    public BigDecimal getPartnerBounceChargesWaived() {
        return partnerBounceChargesWaived;
    }

    public void setPartnerBounceChargesWaived(BigDecimal partnerBounceChargesWaived) {
        this.partnerBounceChargesWaived = partnerBounceChargesWaived;
    }

    public void resetDerivedComponents() {
        this.principalCompleted = null;
        this.principalWrittenOff = null;
        this.interestPaid = null;
        this.interestWaived = null;
        this.interestWrittenOff = null;
        this.feeChargesPaid = null;
        this.feeChargesWaived = null;
        this.feeChargesWrittenOff = null;
        this.penaltyChargesPaid = null;
        this.penaltyChargesWaived = null;
        this.penaltyChargesWrittenOff = null;
        this.totalPaidInAdvance = null;
        this.totalPaidLate = null;

        this.selfPrincipalCompleted = null;
        this.selfPrincipalWrittenOff = null;
        this.selfInterestPaid = null;
        this.selfInterestWaived = null;
        this.selfInterestWrittenOff = null;
        this.selfFeeChargesPaid = null;
        this.selfFeeChargesWaived = null;
        this.selfFeeChargesWrittenOff = null;
        this.selfPenaltyChargesPaid = null;
        this.selfPenaltyChargesWaived = null;
        this.selfPenaltyChargesWrittenOff = null;
//        this.selfTotalPaidInAdvance = null;
        this.selfTotalPaidLate = null;

        this.partnerPrincipalCompleted = null;
        this.partnerPrincipalWrittenOff = null;
        this.partnerInterestPaid = null;
        this.partnerInterestWaived = null;
        this.partnerInterestWrittenOff = null;
        this.partnerFeeChargesPaid = null;
        this.partnerFeeChargesWaived = null;
        this.partnerFeeChargesWrittenOff = null;
        this.partnerPenaltyChargesPaid = null;
        this.partnerPenaltyChargesWaived = null;
        this.partnerPenaltyChargesWrittenOff = null;
        //this.partnerTotalPaidInAdvance = null;
        this.partnerTotalPaidLate = null;
        //  this.advanceAmount =null;

        this.obligationsMet = false;
        this.obligationsMetOnDate = null;
    }

    public void resetAccrualComponents() {
        this.interestAccrued = null;
        this.feeAccrued = null;
        this.penaltyAccrued = null;
    }

    public Money payPenaltyChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money penaltyPortionOfTransaction = Money.zero(currency);

        final Money penaltyChargesDue = getPenaltyChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(penaltyChargesDue)) {
            this.penaltyChargesPaid = getPenaltyChargesPaid(currency).plus(penaltyChargesDue).getAmount();
            penaltyPortionOfTransaction = penaltyPortionOfTransaction.plus(penaltyChargesDue);
        } else {
            this.penaltyChargesPaid = getPenaltyChargesPaid(currency).plus(transactionAmountRemaining).getAmount();
            penaltyPortionOfTransaction = penaltyPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.penaltyChargesPaid = defaultToNullIfZero(this.penaltyChargesPaid);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return penaltyPortionOfTransaction;
    }

    public Money payFeeChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money feePortionOfTransaction = Money.zero(currency);

        final Money feeChargesDue = getFeeChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(feeChargesDue)) {
            this.feeChargesPaid = getFeeChargesPaid(currency).plus(feeChargesDue).getAmount();
            feePortionOfTransaction = feePortionOfTransaction.plus(feeChargesDue);
        } else {
            this.feeChargesPaid = getFeeChargesPaid(currency).plus(transactionAmountRemaining).getAmount();
            feePortionOfTransaction = feePortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.feeChargesPaid = defaultToNullIfZero(this.feeChargesPaid);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, feePortionOfTransaction);

        return feePortionOfTransaction;
    }

    public Money payInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money interestPortionOfTransaction = Money.zero(currency);

        // trackAdvanceAndLateTotalsForRepaymentPeriod

        final Money interestDue = getInterestOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
            this.interestPaid = getInterestPaid(currency).plus(interestDue).getAmount();
            interestPortionOfTransaction = interestPortionOfTransaction.plus(interestDue);
        } else {
            this.interestPaid = getInterestPaid(currency).plus(transactionAmountRemaining).getAmount();
            interestPortionOfTransaction = interestPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.interestPaid = defaultToNullIfZero(this.interestPaid);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, interestPortionOfTransaction);

        return interestPortionOfTransaction;
    }

    public void paySelfInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money selfInterestPortionOfTransaction = Money.zero(currency);

        final Money selfInterestCharged = getSelfInterestOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(selfInterestCharged)) {
            this.selfInterestPaid = getSelfInterestPaid(currency).plus(selfInterestCharged).getAmount();
            selfInterestPortionOfTransaction = selfInterestPortionOfTransaction.plus(selfInterestCharged);
        } else {
            this.selfInterestPaid = getSelfInterestPaid(currency).plus(transactionAmountRemaining).getAmount();
            selfInterestPortionOfTransaction = selfInterestPortionOfTransaction.plus(transactionAmountRemaining);
        }
        this.selfInterestPaid = defaultToNullIfZero(this.selfInterestPaid);
        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        trackAdvanceAndLateSelfTotalsForRepaymentPeriod(transactionDate, currency, selfInterestPortionOfTransaction);
    }

    public void payPartnerInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money partnerInterestPortionOfTransaction = Money.zero(currency);

        final Money partnerInterestCharged = getPartnerInterestOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(partnerInterestCharged)) {
            this.partnerInterestPaid = getPartnerInterestPaid(currency).plus(partnerInterestCharged).getAmount();
            partnerInterestPortionOfTransaction = partnerInterestPortionOfTransaction.plus(partnerInterestCharged);
        } else {
            this.partnerInterestPaid = getPartnerInterestPaid(currency).plus(transactionAmountRemaining).getAmount();
            partnerInterestPortionOfTransaction = partnerInterestPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.partnerInterestPaid = defaultToNullIfZero(this.partnerInterestPaid);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        trackAdvanceAndLatePartnerTotalsForRepaymentPeriod(transactionDate, currency, partnerInterestPortionOfTransaction);

    }

    public Money payPrincipalComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money principalPortionOfTransaction = Money.zero(currency);

        final Money principalDue = getPrincipalOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(principalDue)) {
            this.principalCompleted = getPrincipalCompleted(currency).plus(principalDue).getAmount();
            principalPortionOfTransaction = principalPortionOfTransaction.plus(principalDue);
        } else {
            this.principalCompleted = getPrincipalCompleted(currency).plus(transactionAmountRemaining).getAmount();
            principalPortionOfTransaction = principalPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.principalCompleted = defaultToNullIfZero(this.principalCompleted);
        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, principalPortionOfTransaction);

        return principalPortionOfTransaction;
    }

    public void paySelfPrincipalComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money selfPrincipalPortionOfTransaction = Money.zero(currency);

        final Money selfPrincipal = getSelfPrincipal(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(selfPrincipal)) {
            this.selfPrincipalCompleted = getSelfPrincipalCompleted(currency).plus(selfPrincipal).getAmount();
            selfPrincipalPortionOfTransaction = selfPrincipalPortionOfTransaction.plus(selfPrincipal);
        } else {
            this.selfPrincipalCompleted = getSelfPrincipalCompleted(currency).plus(transactionAmountRemaining).getAmount();
            selfPrincipalPortionOfTransaction = selfPrincipalPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.selfPrincipalCompleted = defaultToNullIfZero(this.selfPrincipalCompleted);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        trackAdvanceAndLateSelfTotalsForRepaymentPeriod(transactionDate, currency, selfPrincipalPortionOfTransaction);

    }

    private void trackAdvanceAndLateSelfTotalsForRepaymentPeriod(LocalDate transactionDate, MonetaryCurrency currency, Money selfPrincipalPortionOfTransaction) {

        if (isLatePayment(transactionDate)) {
            this.selfTotalPaidLate = asMoney(this.selfTotalPaidLate, currency).plus(selfPrincipalPortionOfTransaction).getAmount();
        }
    }


    public void payPartnerPrincipalComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money partnerPrincipalPortionOfTransaction = Money.zero(currency);

        final Money partnerPrincipal = getPartnerPrincipal(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(partnerPrincipal)) {
            this.partnerPrincipalCompleted = getPartnerPrincipalCompleted(currency).plus(partnerPrincipal).getAmount();
            partnerPrincipalPortionOfTransaction = partnerPrincipalPortionOfTransaction.plus(partnerPrincipal);
        } else {
            this.partnerPrincipalCompleted = getPartnerPrincipalCompleted(currency).plus(transactionAmountRemaining).getAmount();
            partnerPrincipalPortionOfTransaction = partnerPrincipalPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.partnerPrincipalCompleted = defaultToNullIfZero(this.partnerPrincipalCompleted);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        trackAdvanceAndLatePartnerTotalsForRepaymentPeriod(transactionDate, currency, partnerPrincipalPortionOfTransaction);

    }

    private void trackAdvanceAndLatePartnerTotalsForRepaymentPeriod(LocalDate transactionDate, MonetaryCurrency currency, Money partnerPrincipalPortionOfTransaction) {
        if (isLatePayment(transactionDate)) {
            this.partnerTotalPaidLate = asMoney(this.partnerTotalPaidLate, currency).plus(partnerPrincipalPortionOfTransaction).getAmount();
        }

    }

    public Money waiveInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money waivedInterestPortionOfTransaction = Money.zero(currency);

        final Money interestDue = getInterestOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
            this.interestWaived = getInterestWaived(currency).plus(interestDue).getAmount();
            waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(interestDue);
        } else {
            this.interestWaived = getInterestWaived(currency).plus(transactionAmountRemaining).getAmount();
            waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.interestWaived = defaultToNullIfZero(this.interestWaived);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return waivedInterestPortionOfTransaction;
    }

    public Money waivePenaltyChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money waivedPenaltyChargesPortionOfTransaction = Money.zero(currency);

        final Money penanltiesDue = getPenaltyChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(penanltiesDue)) {
            this.penaltyChargesWaived = getPenaltyChargesWaived(currency).plus(penanltiesDue).getAmount();
            waivedPenaltyChargesPortionOfTransaction = waivedPenaltyChargesPortionOfTransaction.plus(penanltiesDue);
        } else {
            this.penaltyChargesWaived = getPenaltyChargesWaived(currency).plus(transactionAmountRemaining).getAmount();
            waivedPenaltyChargesPortionOfTransaction = waivedPenaltyChargesPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.penaltyChargesWaived = defaultToNullIfZero(this.penaltyChargesWaived);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return waivedPenaltyChargesPortionOfTransaction;
    }

    public Money waiveFeeChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money waivedFeeChargesPortionOfTransaction = Money.zero(currency);

        final Money feesDue = getFeeChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(feesDue)) {
            this.feeChargesWaived = getFeeChargesWaived(currency).plus(feesDue).getAmount();
            waivedFeeChargesPortionOfTransaction = waivedFeeChargesPortionOfTransaction.plus(feesDue);
        } else {
            this.feeChargesWaived = getFeeChargesWaived(currency).plus(transactionAmountRemaining).getAmount();
            waivedFeeChargesPortionOfTransaction = waivedFeeChargesPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.feeChargesWaived = defaultToNullIfZero(this.feeChargesWaived);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return waivedFeeChargesPortionOfTransaction;
    }

    public Money writeOffOutstandingPrincipal(final LocalDate transactionDate, final MonetaryCurrency currency) {

        final Money principalDue = getPrincipalOutstanding(currency);
        this.principalWrittenOff = defaultToNullIfZero(principalDue.getAmount());

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return principalDue;
    }

    public Money writeOffOutstandingInterest(final LocalDate transactionDate, final MonetaryCurrency currency) {

        final Money interestDue = getInterestOutstanding(currency);
        this.interestWrittenOff = defaultToNullIfZero(interestDue.getAmount());

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return interestDue;
    }

    public Money writeOffOutstandingFeeCharges(final LocalDate transactionDate, final MonetaryCurrency currency) {
        final Money feeChargesOutstanding = getFeeChargesOutstanding(currency);
        this.feeChargesWrittenOff = defaultToNullIfZero(feeChargesOutstanding.getAmount());

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return feeChargesOutstanding;
    }

    public Money writeOffOutstandingPenaltyCharges(final LocalDate transactionDate, final MonetaryCurrency currency) {
        final Money penaltyChargesOutstanding = getPenaltyChargesOutstanding(currency);
        this.penaltyChargesWrittenOff = defaultToNullIfZero(penaltyChargesOutstanding.getAmount());

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return penaltyChargesOutstanding;
    }

    public boolean isOverdueOn(final LocalDate date) {
        return getDueDate().isBefore(date);
    }

    public void updateChargePortion(final Money feeChargesDue, final Money feeChargesWaived, final Money feeChargesWrittenOff,
                                    final BigDecimal penaltyChargesDue, final Money penaltyChargesWaived, final Money penaltyChargesWrittenOff) {
        this.feeChargesCharged = defaultToNullIfZero(feeChargesDue.getAmount());
        this.feeChargesWaived = defaultToNullIfZero(feeChargesWaived.getAmount());
        this.feeChargesWrittenOff = defaultToNullIfZero(feeChargesWrittenOff.getAmount());
        this.penaltyCharges = penaltyChargesDue;
        this.penaltyChargesWaived = defaultToNullIfZero(penaltyChargesWaived.getAmount());
        this.penaltyChargesWrittenOff = defaultToNullIfZero(penaltyChargesWrittenOff.getAmount());
    }

    public void updateAccrualPortion(final Money interest, final Money feeCharges, final Money penalityCharges) {
        this.interestAccrued = defaultToNullIfZero(interest.getAmount());
        this.feeAccrued = defaultToNullIfZero(feeCharges.getAmount());
        this.penaltyAccrued = defaultToNullIfZero(penalityCharges.getAmount());
    }

    public void updateDerivedFields(final MonetaryCurrency currency, final LocalDate actualDisbursementDate) {
        if (!this.obligationsMet && getTotalOutstanding(currency).isZero()) {
            this.obligationsMet = true;
            this.obligationsMetOnDate = Date.from(actualDisbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
    }

    private void trackAdvanceAndLateTotalsForRepaymentPeriod(final LocalDate transactionDate, final MonetaryCurrency currency,
                                                             final Money amountPaidInRepaymentPeriod) {
//        if (isInAdvance(transactionDate)) {
//            this.totalPaidInAdvance = asMoney(this.totalPaidInAdvance, currency).plus(amountPaidInRepaymentPeriod).getAmount();
//        } else

        if (isLatePayment(transactionDate)) {
            this.totalPaidLate = asMoney(this.totalPaidLate, currency).plus(amountPaidInRepaymentPeriod).getAmount();
        }
    }

//    private void trackAdvanceAndLateSelfTotalsForRepaymentPeriod(final LocalDate transactionDate, final MonetaryCurrency currency,
//                                                             final Money amountPaidInRepaymentPeriod) {
//        if (isInAdvance(transactionDate)) {
//            this.selfTotalPaidInAdvance = asMoney(this.selfTotalPaidInAdvance, currency).plus(amountPaidInRepaymentPeriod).getAmount();
//        } else if (isLatePayment(transactionDate)) {
//            this.selfTotalPaidLate = asMoney(this.selfTotalPaidLate, currency).plus(amountPaidInRepaymentPeriod).getAmount();
//        }
//    }

//    private void trackAdvanceAndLatePartnerTotalsForRepaymentPeriod(final LocalDate transactionDate, final MonetaryCurrency currency,
//                                                             final Money amountPaidInRepaymentPeriod) {
//        if (isInAdvance(transactionDate)) {
//            this.partnerTotalPaidInAdvance = asMoney(this.partnerTotalPaidInAdvance, currency).plus(amountPaidInRepaymentPeriod).getAmount();
//        } else if (isLatePayment(transactionDate)) {
//            this.partnerTotalPaidLate = asMoney(this.partnerTotalPaidLate, currency).plus(amountPaidInRepaymentPeriod).getAmount();
//        }
//    }

    private Money asMoney(final BigDecimal decimal, final MonetaryCurrency currency) {
        return Money.of(currency, decimal);
    }

    private boolean isInAdvance(final LocalDate transactionDate) {
        return transactionDate.isBefore(getDueDate());
    }

    private boolean isLatePayment(final LocalDate transactionDate) {
        return transactionDate.isAfter(getDueDate());
    }

    public void checkIfRepaymentPeriodObligationsAreMet(final LocalDate transactionDate, final MonetaryCurrency currency) {
        this.obligationsMet = getTotalOutstanding(currency).isZero();
        if (this.obligationsMet) {
            this.obligationsMetOnDate = Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            this.obligationsMetOnDate = null;
        }

        this.interestPrincipalAppropriatedOnDate = getTotalInterestPrincipalOutstanding(currency).isZero() && Objects.isNull(interestPrincipalAppropriatedOnDate) ?
                Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : interestPrincipalAppropriatedOnDate;

        this.selfInterestPrincipalAppropriatedOnDate = getTotalSelfInterestPrincipalOutstanding(currency).isZero() && Objects.isNull(selfInterestPrincipalAppropriatedOnDate) ?
                Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : interestPrincipalAppropriatedOnDate;

        this.partnerInterestPrincipalAppropriatedOnDate = getTotalPartnerInterestPrincipalOutstanding(currency).isZero() && Objects.isNull(partnerInterestPrincipalAppropriatedOnDate) ?
                Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : interestPrincipalAppropriatedOnDate;

    }

    public void updateDueDate(final LocalDate newDueDate) {
        if (newDueDate != null) {
            this.dueDate = Date.from(newDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
    }

    public void updateFromDate(final LocalDate newFromDate) {
        if (newFromDate != null) {
            this.fromDate = Date.from(newFromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
    }

    public Money getTotalPaidInAdvance(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalPaidInAdvance);
    }

    public BigDecimal getTotalPaidInAdvance() {
        return this.totalPaidInAdvance != null ? totalPaidInAdvance : BigDecimal.ZERO;
    }

    public Money getTotalPaidLate(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalPaidLate);
    }

    public boolean isRecalculatedInterestComponent() {
        return this.recalculatedInterestComponent;
    }

    public void setRecalculatedInterestComponent(boolean recalculatedInterestComponent) {
        this.recalculatedInterestComponent = recalculatedInterestComponent;
    }

    public void updateInstallmentNumber(final Integer installmentNumber) {
        if (installmentNumber != null) {
            this.installmentNumber = installmentNumber;
        }
    }

    public void updateInterestCharged(final BigDecimal interestCharged) {
        this.interestCharged = interestCharged;
    }

    public void updateSelfInterestCharged(final BigDecimal selfInterestCharged) {
        this.selfInterestCharged = selfInterestCharged;
    }

    public void updatePartnerInterestCharged(final BigDecimal partnerInterestCharged) {
        this.partnerInterestCharged = partnerInterestCharged;
    }

//    public void updateSelfPrincipal(final BigDecimal selfPrincipal) {
//        this.selfPrincipal = selfPrincipal;
//    }
//
//    public void updatePartnerPrincipal(final BigDecimal partnerPrincipal) {
//        this.partnerPrincipal = partnerPrincipal;
//    }

    public void updateObligationMet(final Boolean obligationMet) {
        this.obligationsMet = obligationMet;
    }

    public void updateObligationMetOnDate(final LocalDate obligationsMetOnDate) {
        this.obligationsMetOnDate = (obligationsMetOnDate != null)
                ? Date.from(obligationsMetOnDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                : null;
    }

    public void updateInterestWrittenOff(final BigDecimal interestWrittenOff) {
        this.interestWrittenOff = interestWrittenOff;
    }

    public void updatePrincipal(final BigDecimal principal) {
        this.principal = principal;
    }

    public static Comparator<LoanRepaymentScheduleInstallment> installmentNumberComparator = new Comparator<LoanRepaymentScheduleInstallment>() {

        @Override
        public int compare(LoanRepaymentScheduleInstallment arg0, LoanRepaymentScheduleInstallment arg1) {

            return arg0.getInstallmentNumber().compareTo(arg1.getInstallmentNumber());
        }
    };


    public BigDecimal getTotalPaidLate() {
        return this.totalPaidLate;
    }

    public LocalDate getObligationsMetOnDate() {
        LocalDate obligationsMetOnDate = null;

        if (this.obligationsMetOnDate != null) {
            obligationsMetOnDate = LocalDate.ofInstant(this.obligationsMetOnDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }

        return obligationsMetOnDate;
    }

    /********** UNPAY COMPONENTS ****/

    public Money unpayPenaltyChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money penaltyPortionOfTransactionDeducted = Money.zero(currency);

        final Money penaltyChargesCompleted = getPenaltyChargesPaid(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(penaltyChargesCompleted)) {
            this.penaltyChargesPaid = Money.zero(currency).getAmount();
            penaltyPortionOfTransactionDeducted = penaltyChargesCompleted;
        } else {
            this.penaltyChargesPaid = penaltyChargesCompleted.minus(transactionAmountRemaining).getAmount();
            penaltyPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return penaltyPortionOfTransactionDeducted;
    }

    public Money unpayFeeChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money feePortionOfTransactionDeducted = Money.zero(currency);

        final Money feeChargesCompleted = getFeeChargesPaid(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(feeChargesCompleted)) {
            this.feeChargesPaid = Money.zero(currency).getAmount();
            feePortionOfTransactionDeducted = feeChargesCompleted;
        } else {
            this.feeChargesPaid = feeChargesCompleted.minus(transactionAmountRemaining).getAmount();
            feePortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        reduceAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, feePortionOfTransactionDeducted);

        return feePortionOfTransactionDeducted;
    }

    public Money unpayInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money interestPortionOfTransactionDeducted = Money.zero(currency);

        final Money interestCompleted = getInterestPaid(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestCompleted)) {
            this.interestPaid = Money.zero(currency).getAmount();
            interestPortionOfTransactionDeducted = interestCompleted;
        } else {
            this.interestPaid = interestCompleted.minus(transactionAmountRemaining).getAmount();
            interestPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        reduceAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, interestPortionOfTransactionDeducted);

        return interestPortionOfTransactionDeducted;
    }

    public Money unpaySelfInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money selfInterestPortionOfTransactionDeducted = Money.zero(currency);

        final Money selfInterestCompleted = getSelfInterestPaid(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(selfInterestCompleted)) {
            this.selfInterestPaid = Money.zero(currency).getAmount();
            selfInterestPortionOfTransactionDeducted = selfInterestCompleted;
        } else {
            this.selfInterestPaid = selfInterestCompleted.minus(transactionAmountRemaining).getAmount();
            selfInterestPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        //  reduceAdvanceAndLateSelfTotalsForRepaymentPeriod(transactionDate, currency, selfInterestPortionOfTransactionDeducted);

        return selfInterestPortionOfTransactionDeducted;
    }

    public Money unpayPartnerInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money partnerInterestPortionOfTransactionDeducted = Money.zero(currency);

        final Money partnerInterestCompleted = getPartnerInterestPaid(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(partnerInterestCompleted)) {
            this.partnerInterestPaid = Money.zero(currency).getAmount();
            partnerInterestPortionOfTransactionDeducted = partnerInterestCompleted;
        } else {
            this.partnerInterestPaid = partnerInterestCompleted.minus(transactionAmountRemaining).getAmount();
            partnerInterestPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        //reduceAdvanceAndLatePartnerTotalsForRepaymentPeriod(transactionDate, currency, partnerInterestPortionOfTransactionDeducted);

        return partnerInterestPortionOfTransactionDeducted;
    }

    public Money unpayPrincipalComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money principalPortionOfTransactionDeducted = Money.zero(currency);

        final Money principalCompleted = getPrincipalCompleted(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(principalCompleted)) {
            this.principalCompleted = Money.zero(currency).getAmount();
            principalPortionOfTransactionDeducted = principalCompleted;
        } else {
            this.principalCompleted = principalCompleted.minus(transactionAmountRemaining).getAmount();
            principalPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        reduceAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, principalPortionOfTransactionDeducted);

        return principalPortionOfTransactionDeducted;
    }

    public Money unpaySelfPrincipalComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money selfPrincipalPortionOfTransactionDeducted = Money.zero(currency);

        final Money selfPrincipalCompleted = getSelfPrincipalCompleted(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(selfPrincipalCompleted)) {
            this.selfPrincipalCompleted = Money.zero(currency).getAmount();
            selfPrincipalPortionOfTransactionDeducted = selfPrincipalCompleted;
        } else {
            this.selfPrincipalCompleted = selfPrincipalCompleted.minus(transactionAmountRemaining).getAmount();
            selfPrincipalPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        //   reduceAdvanceAndLateSelfTotalsForRepaymentPeriod(transactionDate, currency, selfPrincipalPortionOfTransactionDeducted);

        return selfPrincipalPortionOfTransactionDeducted;
    }

    public Money unpayPartnerPrincipalComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money partnerPrincipalPortionOfTransactionDeducted = Money.zero(currency);

        final Money partnerPrincipalCompleted = getPartnerPrincipalCompleted(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(partnerPrincipalCompleted)) {
            this.partnerPrincipalCompleted = Money.zero(currency).getAmount();
            partnerPrincipalPortionOfTransactionDeducted = partnerPrincipalCompleted;
        } else {
            this.partnerPrincipalCompleted = partnerPrincipalCompleted.minus(transactionAmountRemaining).getAmount();
            partnerPrincipalPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        //reduceAdvanceAndLatePartnerTotalsForRepaymentPeriod(transactionDate, currency, partnerPrincipalPortionOfTransactionDeducted);

        return partnerPrincipalPortionOfTransactionDeducted;
    }

    private void reduceAdvanceAndLateTotalsForRepaymentPeriod(final LocalDate transactionDate, final MonetaryCurrency currency,
                                                              final Money amountDeductedInRepaymentPeriod) {

//        if (isInAdvance(transactionDate)) {
//            Money mTotalPaidInAdvance = Money.of(currency, this.totalPaidInAdvance);
//
//            if (mTotalPaidInAdvance.isLessThan(amountDeductedInRepaymentPeriod)
//                    || mTotalPaidInAdvance.isEqualTo(amountDeductedInRepaymentPeriod)) {
//                this.totalPaidInAdvance = Money.zero(currency).getAmount();
//            } else {
//                this.totalPaidInAdvance = mTotalPaidInAdvance.minus(amountDeductedInRepaymentPeriod).getAmount();
//            }
//        } else

        if (isLatePayment(transactionDate)) {
            Money mTotalPaidLate = Money.of(currency, this.totalPaidLate);

            if (mTotalPaidLate.isLessThan(amountDeductedInRepaymentPeriod) || mTotalPaidLate.isEqualTo(amountDeductedInRepaymentPeriod)) {
                this.totalPaidLate = Money.zero(currency).getAmount();
            } else {
                this.totalPaidLate = mTotalPaidLate.minus(amountDeductedInRepaymentPeriod).getAmount();
            }
        }
    }

//    private void reduceAdvanceAndLateSelfTotalsForRepaymentPeriod(final LocalDate transactionDate, final MonetaryCurrency currency,
//                                                              final Money amountDeductedInRepaymentPeriod) {
//
//        if (isInAdvance(transactionDate)) {
//            Money mSelfTotalPaidInAdvance = Money.of(currency, this.selfTotalPaidInAdvance);
//
//            if (mSelfTotalPaidInAdvance.isLessThan(amountDeductedInRepaymentPeriod)
//                    || mSelfTotalPaidInAdvance.isEqualTo(amountDeductedInRepaymentPeriod)) {
//                this.selfTotalPaidInAdvance = Money.zero(currency).getAmount();
//            } else {
//                this.selfTotalPaidInAdvance = mSelfTotalPaidInAdvance.minus(amountDeductedInRepaymentPeriod).getAmount();
//            }
//        } else if (isLatePayment(transactionDate)) {
//            Money mSelfTotalPaidLate = Money.of(currency, this.selfTotalPaidLate);
//
//            if (mSelfTotalPaidLate.isLessThan(amountDeductedInRepaymentPeriod) || mSelfTotalPaidLate.isEqualTo(amountDeductedInRepaymentPeriod)) {
//                this.selfTotalPaidLate = Money.zero(currency).getAmount();
//            } else {
//                this.selfTotalPaidLate = mSelfTotalPaidLate.minus(amountDeductedInRepaymentPeriod).getAmount();
//            }
//        }
//    }

//    private void reduceAdvanceAndLatePartnerTotalsForRepaymentPeriod(final LocalDate transactionDate, final MonetaryCurrency currency,
//                                                              final Money amountDeductedInRepaymentPeriod) {
//
//        if (isInAdvance(transactionDate)) {
//            Money mPartnerTotalPaidInAdvance = Money.of(currency, this.partnerTotalPaidInAdvance);
//
//            if (mPartnerTotalPaidInAdvance.isLessThan(amountDeductedInRepaymentPeriod)
//                    || mPartnerTotalPaidInAdvance.isEqualTo(amountDeductedInRepaymentPeriod)) {
//                this.partnerTotalPaidInAdvance = Money.zero(currency).getAmount();
//            } else {
//                this.partnerTotalPaidInAdvance = mPartnerTotalPaidInAdvance.minus(amountDeductedInRepaymentPeriod).getAmount();
//            }
//        } else if (isLatePayment(transactionDate)) {
//            Money mPartnerTotalPaidLate = Money.of(currency, this.partnerTotalPaidLate);
//
//            if (mPartnerTotalPaidLate.isLessThan(amountDeductedInRepaymentPeriod) || mPartnerTotalPaidLate.isEqualTo(amountDeductedInRepaymentPeriod)) {
//                this.partnerTotalPaidLate = Money.zero(currency).getAmount();
//            } else {
//                this.partnerTotalPaidLate = mPartnerTotalPaidLate.minus(amountDeductedInRepaymentPeriod).getAmount();
//            }
//        }
//    }

    public Money getDue(MonetaryCurrency currency) {
        return getPrincipal(currency).plus(getInterestCharged(currency)).plus(getFeeChargesCharged(currency))
                .plus(getPenaltyChargesCharged(currency));
    }

    public Set<LoanInterestRecalcualtionAdditionalDetails> getLoanCompoundingDetails() {
        return this.loanCompoundingDetails;
    }

    public Money getAccruedInterestOutstanding(final MonetaryCurrency currency) {
        final Money interestAccountedFor = getInterestPaid(currency).plus(getInterestWaived(currency))
                .plus(getInterestWrittenOff(currency));
        return getInterestAccrued(currency).minus(interestAccountedFor);
    }

    public Money getTotalPaid(final MonetaryCurrency currency) {
        return getPenaltyChargesPaid(currency).plus(getFeeChargesPaid(currency)).plus(getInterestPaid(currency))
                .plus(getPrincipalCompleted(currency));
    }

    public BigDecimal getRescheduleInterestPortion() {
        return rescheduleInterestPortion;
    }

    public void setRescheduleInterestPortion(BigDecimal rescheduleInterestPortion) {
        this.rescheduleInterestPortion = rescheduleInterestPortion;
    }

    public void setFeeChargesWaived(final BigDecimal newFeeChargesCharged) {
        this.feeChargesWaived = newFeeChargesCharged;
    }

    public Set<LoanInstallmentCharge> getInstallmentCharges() {
        return installmentCharges;
    }

    public Money checkTheAdvancePayment(LocalDate transactionDate, Money transactionAmountRemaining) {
        return trackAdvanceTotalsForRepaymentPeriod(transactionDate, transactionAmountRemaining);
    }

    private Money trackAdvanceTotalsForRepaymentPeriod(LocalDate transactionDate, Money transactionAmountRemaining) {
        // MonetaryCurrency currency= transactionAmountRemaining.getCurrency();
        if (isInAdvance(transactionDate)) {
            this.totalPaidInAdvance = transactionAmountRemaining.getAmount();
            return transactionAmountRemaining.minus(totalPaidInAdvance);
        }
        return transactionAmountRemaining;
    }

    public Money currentDueDateOverPayment(LocalDate transactionDate, Money transactionAmountRemaining) {
        return trackAdvanceAmountForCurrentDueDate(transactionDate, transactionAmountRemaining);

    }

    private Money trackAdvanceAmountForCurrentDueDate(LocalDate transactionDate, Money transactionAmountRemaining) {

        if (isCurrentDueDate(transactionDate)) {
            this.totalPaidInAdvance = transactionAmountRemaining.getAmount();
        }
        return transactionAmountRemaining.minus(totalPaidInAdvance);
    }

    private boolean isCurrentDueDate(LocalDate transactionDate) {

        return transactionDate.equals(getDueDate());
    }

    public void updateAdvanceAmount(BigDecimal updatedAdvanceAmount) {
        this.totalPaidInAdvance = updatedAdvanceAmount;
    }

    public Boolean checkDueDateForProcess(LocalDate transactionDate) {

        return checkDueDateForProcessCheck(transactionDate);


    }

    private Boolean checkDueDateForProcessCheck(LocalDate transactionDate) {

        if (isInAdvance(transactionDate)) {
            return true;
        } else if (isEqual(transactionDate)) {
            return true;

        } else {
            return false;
        }
    }

    private boolean isEqual(LocalDate transactionDate) {

        return transactionDate.equals(getDueDate());
    }

    public void updateAdvanceAmountForCurrentDue(LocalDate transactionDate) {
        if (transactionDate.equals(getDueDate())) {
            this.totalPaidInAdvance = BigDecimal.ZERO;
        }
    }

    public Boolean isLastInstallment(final int numberOfRepayments, final int periodNumber) {
        return periodNumber == numberOfRepayments;
    }

    public void updatePenalChargeShare(BigDecimal selfPenalityChargeShare, BigDecimal partnerPenalityChargeShare) {
        this.selfPenaltyCharges = defaultToNullIfZero(selfPenalityChargeShare);
        this.partnerPenaltyCharges = defaultToNullIfZero(partnerPenalityChargeShare);
//        this.selfFeeChargesCharged =defaultToNullIfZero(selfFeeChargesDueForRepaymentPeriod.getAmount());
//        this.partnerFeeChargesCharged =defaultToNullIfZero(partnerFeeChargesDueForRepaymentPeriod.getAmount());
    }

    public void updatePenaltySplit(BigDecimal selfSharePenaltyAmount, BigDecimal partnerSharePenaltyAmount) {

        this.selfPenaltyChargesPaid = selfSharePenaltyAmount;
        this.partnerPenaltyChargesPaid = partnerSharePenaltyAmount;


    }

    public void updateForeClosureAmount(Money transactionAmountRemaining, BigDecimal selfShareForeClosureAmount, BigDecimal partnerShareForeClosureAmount) {

        this.feeChargesCharged = transactionAmountRemaining.getAmount();
        this.feeChargesPaid = transactionAmountRemaining.getAmount();
        this.selfFeeChargesCharged = selfShareForeClosureAmount;
        this.selfFeeChargesPaid = selfShareForeClosureAmount;
        this.partnerFeeChargesCharged = partnerShareForeClosureAmount;
        this.partnerFeeChargesPaid = partnerShareForeClosureAmount;

    }


    public void updateDpdNumberForValueDate(long i) {
        this.daysPastDue = 0;
        if (i != 0) {
            this.daysPastDue = Math.toIntExact(i);
            this.dpdBucket = DpdBucketService.getDpdBucketAsString(this.daysPastDue);
        }
    }


    public Money checkForAdvanceAmount(Money transactionAmountRemaining, LoanTransaction loanTransaction,
                                       LocalDate transactionDate, List<LoanTransaction> listOfLoanTransaction, LoanTransaction transaction) {

        /**
         * if the loan Transaction is Eligible For Advance then generating the advance loan Transaction and Based on the next installment condition
         */

        LoanRepaymentScheduleInstallment nextInstallment = loan.fetchRepaymentScheduleInstallment(this.getInstallmentNumber() + 1);
        if (Boolean.TRUE.equals(this.isLastInstallment(loan.getNumberOfRepayments(), this.getInstallmentNumber()) && transactionAmountRemaining.getAmount().doubleValue() > loan.getLoanSummary().getTotalOutstanding().doubleValue())) {
            final String errorMessage = "The transaction amount " + loan.getLoanSummary().getTotalOutstanding() + " cannot be greater than the total outstanding amount " + loanTransaction.getAmount();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.outstanding.amount {}", errorMessage);
        }
        if (!loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.LOAN_FORECLOSURE.getValue()) && LocalDate.now().isBefore(this.getDueDate())
                && (Objects.nonNull(nextInstallment) || this.isLastInstallment(loan.getNumberOfRepayments(), this.getInstallmentNumber()))) {

            LoanTransaction advanceLoanTransaction = new LoanTransaction(loan, loanTransaction.getOffice(), loanTransaction.getTypeOf().getValue(), loanTransaction.getDateOf(), transactionAmountRemaining.getAmount(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, loanTransaction.getOverPaymentPortion(), loanTransaction.isReversed(),
                    loanTransaction.getPaymentDetail(), loanTransaction.getExternalId(), loanTransaction.getCreatedDateTime(), loanTransaction.getAppUser(), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue(),

                    loanTransaction.getReceiptReferenceNumber(), loanTransaction.getPartnerTransferUtr(), null, loanTransaction.getRepaymentMode(), BigDecimal.ZERO, null);
            advanceLoanTransaction.setAdvanceAmountprocessed(Long.valueOf(0));
            advanceLoanTransaction.setValueDate(Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            CollectionReport collectionReport = new CollectionReport();
            collectionReport.setLoan(loan);
            collectionReport.setLoanTransaction(loanTransaction);
            collectionReport.setInstallmentNumber(this.getInstallmentNumber());
            collectionReport.setAmount(transactionAmountRemaining.getAmount());
            collectionReport.setDateOf(loanTransaction.getDateOf());
            collectionReport.setTypeOf(loanTransaction.getTypeOf().getValue());
            collectionReport.setCreatedDate(Date.from(loanTransaction.getCreatedDateTime().atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant()));
            collectionReport.setAdvanceAmount(transactionAmountRemaining.getAmount());
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(advanceLoanTransaction, this
                    , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO))
                    , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), defaultToNullIfZero(BigDecimal.ZERO),
                    defaultToNullIfZero(BigDecimal.ZERO), transactionAmountRemaining.getAmount());
            loanTransactionToRepaymentScheduleMapping.setAmount(transactionAmountRemaining.getAmount());
            advanceLoanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, advanceLoanTransaction);
            //  advanceLoanTransaction.setCollectionReport(collectionReport);
            this.totalPaidInAdvance = getTotalPaidInAdvance().add(transactionAmountRemaining.getAmount());
            transaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue());
            if (!loan.isNewTransaction(advanceLoanTransaction)) {
                listOfLoanTransaction.add(advanceLoanTransaction);
            }
            advanceLoanTransaction.setAdvanceAmountprocessed(Long.valueOf(0));



       /* LoanRepaymentScheduleInstallment nextInstallment =loan.fetchRepaymentScheduleInstallment(this.getInstallmentNumber() + 1);
        if( Boolean.TRUE.equals(this.isLastInstallment(loan.getNumberOfRepayments(), this.getInstallmentNumber()) && transactionAmountRemaining.getAmount().doubleValue() > this.getTotalOutstanding(loan.getCurrency()).getAmount().doubleValue()) ){
            final String errorMessage = "The transaction amount "+ loan.getLoanSummary().getTotalOutstanding() + " cannot be greater than the total outstanding amount " + loanTransaction.getAmount();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.outstanding.amount {}", errorMessage);
        }
        if (!loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.LOAN_FORECLOSURE.getValue())
                && (Objects.nonNull(nextInstallment) || this.isLastInstallment(loan.getNumberOfRepayments(),this.getInstallmentNumber()))
                && LocalDate.now().isBefore( Boolean.TRUE.equals(this.isLastInstallment(loan.getNumberOfRepayments(),this.getInstallmentNumber()))
                ? this.getDueDate():currentInstallment.getDueDate())) {
            *//*loanTransaction.setValueDate(loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate())
                    || loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate()) ? java.util.Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : java.util.Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));*//*
            loanTransaction.setValueDate(loanTransaction.getDateOf());
            CollectionReport collectionReport = new CollectionReport();
            collectionReport.setLoan(loan);
            collectionReport.setLoanTransaction(loanTransaction);
            collectionReport.setInstallmentNumber(this.getInstallmentNumber());
            collectionReport.setAmount(transactionAmountRemaining.getAmount());
            collectionReport.setDateOf(loanTransaction.getDateOf());
            collectionReport.setTypeOf(loanTransaction.getTypeOf().getValue());
            collectionReport.setCreatedDate(Date.from(loanTransaction.getCreatedDateTime().atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant()));
            collectionReport.setAdvanceAmount(transactionAmountRemaining.getAmount());
          //  loanTransaction.setCollectionReport(collectionReport);
            loanTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());
            currentInstallment.totalPaidInAdvance = getTotalPaidInAdvance().add(transactionAmountRemaining.getAmount());
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, this
                    , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO))
                    , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), defaultToNullIfZero(BigDecimal.ZERO),
                    defaultToNullIfZero(BigDecimal.ZERO), transactionAmountRemaining.getAmount());
            loanTransactionToRepaymentScheduleMapping.setAmount(transactionAmountRemaining.getAmount());
            if(loanTransaction.getAmount().doubleValue() > transactionAmountRemaining.getAmount().doubleValue()){
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping,loanTransaction);}
            else if(loanTransaction.getAmount().doubleValue() == transactionAmountRemaining.getAmount().doubleValue() && loanTransaction.getLoanTransactionToRepaymentScheduleMappings().isEmpty()){
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping,loanTransaction);
            }
            return Money.zero(this.loan.getCurrency());
        }*/


            //  loan.addLoanTransaction(advanceLoanTransaction);

            /*if(!loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.ADVANCE_AMOUNT.getValue())
                    && !loanTransaction.getPrincipalPortion(loan.getCurrency()).plus(loanTransaction.getInterestPortion(loan.getCurrency())).isZero()){
                loan.addLoanTransaction(advanceLoanTransaction);
            }else{
                if(loanTransaction.getAmount().doubleValue() == transactionAmountRemaining.getAmount().doubleValue()){
                loanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.ADVANCE_AMOUNT.getValue());
                LoanTransactionToRepaymentScheduleMapping transactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, this
                        , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO))
                        , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), defaultToNullIfZero(BigDecimal.ZERO),
                        defaultToNullIfZero(BigDecimal.ZERO), transactionAmountRemaining.getAmount());
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionToRepaymentScheduleMapping,loanTransaction);
                loanTransaction.setValueDate(this.dueDate);
                loanTransaction.setAmount(transactionAmountRemaining.getAmount());}
                else {
                    if(!loan.isNewTransaction(advanceLoanTransaction)){
                    loan.addLoanTransaction(advanceLoanTransaction);}
                }
            }*/
            return Money.zero(this.loan.getCurrency());
        }
        return transactionAmountRemaining;
    }

    public void setPrincipalOutstanding(BigDecimal principalOutstanding) {
        this.principalOutstanding = principalOutstanding;
    }

    public BigDecimal getPrincipalOutstanding() {
        return this.principalOutstanding;
    }

    public void setSelfPrincipalOutstanding(BigDecimal selfPrincipalOutstanding) {
        this.selfPrincipalOutstanding = selfPrincipalOutstanding;
    }

    public BigDecimal getSelfPrincipalOutstanding() {
        return this.selfPrincipalOutstanding != null ? selfPrincipalOutstanding : BigDecimal.ZERO;
    }

    public void setPartnerPrincipalOutstanding(BigDecimal partnerPrincipalOutstanding) {
        this.partnerPrincipalOutstanding = partnerPrincipalOutstanding;
    }

    public BigDecimal getPartnerPrincipalOutstanding() {
        return this.partnerPrincipalOutstanding != null ? partnerPrincipalOutstanding : BigDecimal.ZERO;
    }

    public void setDue(BigDecimal due) {
        this.due = due;
    }

    public BigDecimal getDue() {
        return this.due;
    }

    public void setDpdHistory(Integer dpdHistory) {
        this.dpdHistory = dpdHistory;
        this.dpdBucketHistory = DpdBucketService.getDpdBucketAsString(this.dpdHistory);
    }

    public Integer getDpdHistory() {
        return this.dpdHistory;
    }

    public String getDpdHistoryBucket() {
        return this.dpdBucketHistory;
    }

    public Money getInterestUptodate(MonetaryCurrency currency) {

        BigDecimal interestCharges = this.interestCharged != null ? this.interestCharged : BigDecimal.ZERO;
        BigDecimal interestPaid = this.interestPaid != null ? this.interestPaid : BigDecimal.ZERO;
        return Money.of(currency, interestCharges.subtract(interestPaid));

    }

    public Date getDpdLastRunOn() {
        return dpdLastRunOn;
    }

    public void setDpdLastRunOn(Date dpdLastRunOn) {
        this.dpdLastRunOn = dpdLastRunOn;
    }

    public void restComponenets() {
        this.totalPaidInAdvance = null;
    }

    public Money checkAdvanceForPostSchedular(Money transactionAmountRemaining, LoanTransaction loanTransaction, List<LoanTransaction> listOfLoanTransaction) {

        AppUser appUser = loan.getLoanTransactions().stream().filter(transaction -> !transaction.getAppUser().isSystemUser()).findAny().orElse(null).getAppUser();


        LoanRepaymentScheduleInstallment nextInstallment = loan.fetchRepaymentScheduleInstallment(this.getInstallmentNumber() + 1);
        if (transactionAmountRemaining.getAmount().doubleValue() > 0 && LocalDate.now().isBefore(nextInstallment.getDueDate()) && Objects.nonNull(nextInstallment)) {

            LoanTransaction advanceLoanTransaction = new LoanTransaction(loan, loanTransaction.getOffice(), loanTransaction.getTypeOf().getValue(), loanTransaction.getDateOf(), transactionAmountRemaining.getAmount(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, loanTransaction.getOverPaymentPortion(), loanTransaction.isReversed(),
                    loanTransaction.getPaymentDetail(), loanTransaction.getExternalId(), loanTransaction.getCreatedDateTime(), appUser, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue(),
                    loanTransaction.getReceiptReferenceNumber(), loanTransaction.getPartnerTransferUtr(), null, loanTransaction.getRepaymentMode(), BigDecimal.ZERO, loanTransaction.getId());

            advanceLoanTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());
            advanceLoanTransaction.setAmount(transactionAmountRemaining.getAmount());
            advanceLoanTransaction.setValueDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            nextInstallment.setAdvanceAmount(transactionAmountRemaining.getAmount());

            CollectionReport collectionReport = new CollectionReport();
            collectionReport.setLoan(loan);
            collectionReport.setLoanTransaction(advanceLoanTransaction);
            collectionReport.setInstallmentNumber(this.getInstallmentNumber());
            collectionReport.setAmount(transactionAmountRemaining.getAmount());
            collectionReport.setDateOf(advanceLoanTransaction.getDateOf());
            collectionReport.setTypeOf(advanceLoanTransaction.getTypeOf().getValue());
            collectionReport.setCreatedDate(Date.from(advanceLoanTransaction.getCreatedDateTime().atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant()));
            collectionReport.setAdvanceAmount(transactionAmountRemaining.getAmount());
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMappings = LoanTransactionToRepaymentScheduleMapping.createFrom(advanceLoanTransaction, nextInstallment,
                    Money.zero(advanceLoanTransaction.getLoan().getCurrency()), Money.zero(advanceLoanTransaction.getLoan().getCurrency()), Money.zero(advanceLoanTransaction.getLoan().getCurrency()),
                    Money.zero(advanceLoanTransaction.getLoan().getCurrency()), Money.zero(advanceLoanTransaction.getLoan().getCurrency()),
                    Money.zero(advanceLoanTransaction.getLoan().getCurrency()), Money.zero(advanceLoanTransaction.getLoan().getCurrency()), Money.zero(advanceLoanTransaction.getLoan().getCurrency()), BigDecimal.ZERO, BigDecimal.ZERO, transactionAmountRemaining.getAmount());
            loanTransactionToRepaymentScheduleMappings.setAmount(transactionAmountRemaining.getAmount());
            advanceLoanTransaction.getLoanTransactionToRepaymentScheduleMappings().add(loanTransactionToRepaymentScheduleMappings);
            listOfLoanTransaction.add(advanceLoanTransaction);
            return Money.zero(loan.getCurrency());
        }

        return transactionAmountRemaining;

    }

    public BigDecimal getBounceChargesCharged() {
        return this.bounceCharges != null ? bounceCharges : BigDecimal.ZERO;
    }

    public BigDecimal getBounceChargesPaid() {
        return this.bounceChargesPaid != null ? bounceChargesPaid : BigDecimal.ZERO;
    }

    public BigDecimal getBounceChargesWaived() {
        return this.bounceChargesWaived != null ? bounceChargesWaived : BigDecimal.ZERO;
    }

    public void setFeeChargesCharged(BigDecimal feeChargesCharged) {
        this.feeChargesCharged = feeChargesCharged;
    }

    public void reverseDaysPastDueOnInterestPrincipleCompletion(LocalDate transactionDate) {
        Money totalInterestPrincipleCharged = getPrincipal(this.loan.getCurrency()).add(getInterestCharged(this.loan.getCurrency()));
        Money totalInterestPrincipleRepaid = getPrincipalCompleted(this.loan.getCurrency()).add(getInterestPaid(this.loan.getCurrency()));
        if (totalInterestPrincipleCharged.minus(totalInterestPrincipleRepaid).isZero()) {
            if((transactionDate.equals(getDueDate()) || transactionDate.isAfter(getDueDate()))) {
                int days = Math.toIntExact(ChronoUnit.DAYS.between(getDueDate(), transactionDate));
                this.dpdHistory = days;
                this.daysPastDue = 0;
            } else if(transactionDate.isBefore(getDueDate())) {
                this.dpdHistory = 0;
                this.daysPastDue = 0;
            }
            this.dpdBucket = DpdBucketService.getDpdBucketAsString(this.daysPastDue);
            this.dpdBucketHistory = DpdBucketService.getDpdBucketAsString(this.dpdHistory);
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    public Money getBounceChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.bounceChargesPaid);
    }

    public Money getBounceChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.bounceChargesWaived);
    }

    public Money getBounceChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.bounceChargesWrittenOff);
    }

    public Money getBounceChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.bounceCharges);
    }

    public Money getBounceChargesOutstanding(final MonetaryCurrency currency) {
        final Money feeChargesAccountedFor = getBounceChargesPaid(currency).plus(getBounceChargesWaived(currency))
                .plus(getBounceChargesWrittenOff(currency));
        return getBounceChargesCharged(currency).minus(feeChargesAccountedFor);
    }

    public Money getTotalInterestPrincipalOutstanding(final MonetaryCurrency currency) {
        return getPrincipalOutstanding(currency).plus(getInterestOutstanding(currency));
    }

    public Money getTotalSelfInterestPrincipalOutstanding(final MonetaryCurrency currency) {
        return getPrincipalOutstanding(currency).plus(getInterestOutstanding(currency));
    }

    public Money getTotalPartnerInterestPrincipalOutstanding(final MonetaryCurrency currency) {
        return getPrincipalOutstanding(currency).plus(getInterestOutstanding(currency));
    }

}
