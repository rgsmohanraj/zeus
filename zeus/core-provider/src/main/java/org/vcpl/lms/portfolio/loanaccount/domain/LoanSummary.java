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
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import liquibase.pro.packaged.B;
import lombok.Getter;
import lombok.Setter;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;

/**
 * Encapsulates all the summary details of a {@link Loan}.
 *
 * {@link LoanSummary} fields are updated through a scheduled job. see -
 *
 */
@Embeddable
@Setter
@Getter
public final class LoanSummary {

    // derived totals fields
    @Column(name = "principal_disbursed_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalDisbursed;

    @Column(name = "principal_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalRepaid;

    @Column(name = "self_principal_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfPrincipalRepaid;

    @Column(name = "partner_principal_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerPrincipalRepaid;

    @Column(name = "principal_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalWrittenOff;

    @Column(name = "self_principal_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfPrincipalWrittenOff;

    @Column(name = "partner_principal_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerPrincipalWrittenOff;

    //outstandingFields
    @Column(name = "principal_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalOutstanding;

    @Column(name = "self_principal_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfPrincipalOutstanding;

    @Column(name = "partner_principal_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerPrincipalOutstanding;

    @Column(name = "interest_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestCharged;

    @Column(name = "self_interest_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfInterestCharged;

    @Column(name = "partner_interest_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerInterestCharged;

    @Column(name = "interest_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestRepaid;

    @Column(name = "self_interest_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfInterestRepaid;

    @Column(name = "partner_interest_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerInterestRepaid;

    @Column(name = "interest_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestWaived;

    @Column(name = "self_interest_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerInterestWaived;

    @Column(name = "partner_interest_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfInterestWaived;

    @Column(name = "interest_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestWrittenOff;

    @Column(name = "interest_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestOutstanding;

    @Column(name = "self_interest_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfInterestOutstanding;

    @Column(name = "partner_interest_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerInterestOutstanding;

    @Column(name = "fee_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesCharged;

    @Column(name = "self_fee_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfFeeChargesCharged;

    @Column(name = "partner_fee_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerFeeChargesCharged;

    @Column(name = "total_charges_due_at_disbursement_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesDueAtDisbursement;

    @Column(name = "total_self_charges_due_at_disbursement_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfFeeChargesDueAtDisbursement;

    @Column(name = "total_partner_charges_due_at_disbursement_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerFeeChargesDueAtDisbursement;


    @Column(name = "fee_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesRepaid;

    @Column(name = "self_fee_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfFeeChargesRepaid;
    @Column(name = "partner_fee_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerFeeChargesRepaid;

    @Column(name = "fee_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesWaived;

    @Column(name = "self_fee_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfFeeChargesWaived;

    @Column(name = "partner_fee_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerFeeChargesWaived;


    @Column(name = "fee_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesWrittenOff;

    @Column(name = "self_fee_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfFeeChargesWrittenOff;

    @Column(name = "partner_fee_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerFeeChargesWrittenOff;

    @Column(name = "fee_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesOutstanding;

    @Column(name = "self_fee_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfFeeChargesOutstanding;


    @Column(name = "partner_fee_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerFeeChargesOutstanding;

    @Column(name = "penalty_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesCharged;

    @Column(name = "penalty_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesRepaid;

    @Column(name = "penalty_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesWaived;

    @Column(name = "penalty_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesWrittenOff;

    @Column(name = "penalty_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesOutstanding;

    @Column(name = "total_expected_repayment_derived", scale = 6, precision = 19)
    private BigDecimal totalExpectedRepayment;

    @Column(name = "total_repayment_derived", scale = 6, precision = 19)
    private BigDecimal totalRepayment;

    @Column(name = "total_self_repayment_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfRepayment;

    @Column(name = "total_partner_repayment_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerRepayment;


    @Column(name = "total_expected_costofloan_derived", scale = 6, precision = 19)
    private BigDecimal totalExpectedCostOfLoan;

    @Column(name = "total_costofloan_derived", scale = 6, precision = 19)
    private BigDecimal totalCostOfLoan;

    @Column(name = "total_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalWaived;

    @Column(name = "total_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalWrittenOff;

    @Column(name = "total_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalOutstanding;

    @Column(name = "total_self_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfOutstanding;

    @Column(name = "total_partner_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerOutstanding;

    @Column(name = "self_penalty_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal selfPenaltyChargesChargedDerived;

    @Column(name = "self_penalty_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal selfPenaltyChargesOutstandingDerived;

    @Column(name = "self_penalty_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal selfPenaltyChargesRepaidDerived;

    @Column(name = "self_penalty_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal selfPenaltyChargesWaivedDerived;

    @Column(name = "self_penalty_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal selfPenaltyChargesWrittenDerived;

    @Column(name = "partner_penalty_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal partnerPenaltyChargesChargedDerived;

    @Column(name = "partner_penalty_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal partnerPenaltyChargesOutstandingDerived;

    @Column(name = "partner_penalty_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal partnerPenaltyChargesRepaidDerived;

    @Column(name = "partner_penalty_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal partnerPenaltyChargesWaivedDerived;

    @Column(name = "partner_penalty_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal partnerPenaltyChargesWrittenDerived;

    @Column(name = "total_gst_derived", scale = 6, precision = 19)
    private BigDecimal totalGstDerived;

    @Column(name = "total_gst_paid", scale = 6, precision = 19)
    private BigDecimal totalGstPaid;

    @Column(name = "total_gst_outstanding", scale = 6, precision = 19)
    private BigDecimal totalGstOutstanding;

    @Column(name = "total_self_gst_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfGstDerived;

    @Column(name = "total_self_gst_paid", scale = 6, precision = 19)
    private BigDecimal totalSelfGstPaid;

    @Column(name = "total_self_gst_outstanding", scale = 6, precision = 19)
    private BigDecimal totalSelfGstOutstanding;

    @Column(name = "total_partner_gst_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerGstDerived;

    @Column(name = "total_partner_gst_paid", scale = 6, precision = 19)
    private BigDecimal totalPartnerGstPaid;

    @Column(name = "total_partner_gst_outstanding", scale = 6, precision = 19)
    private BigDecimal totalPartnerGstOutstanding;



    @Column(name = "total_gst_due_at_disbursement_derived", scale = 6, precision = 19)
    private BigDecimal totalGstDueAtDisbursement;

    @Column(name = "total_self_gst_due_at_disbursement_derived", scale = 6, precision = 19)
    private BigDecimal totalSelfGstDueAtDisbursement;

    @Column(name = "total_partner_gst_due_at_disbursement_derived", scale = 6, precision = 19)
    private BigDecimal totalPartnerGstDueAtDisbursement;

    /**
     * Loan arrear ageing columns added
     */

    @Column(name = "principal_overdue_derived", scale = 6, precision = 19)
    private BigDecimal principalOverdueDerived;

    @Column(name = "self_principal_overdue_derived", scale = 6, precision = 19)
    private BigDecimal selfPrincipalOverdueDerived;

    @Column(name = "partner_principal_overdue_derived", scale = 6, precision = 19)
    private BigDecimal partnerPrincipalOverdueDerived;

    @Column(name = "interest_overdue_derived", scale = 6, precision = 19)
    private BigDecimal interestOverdueDerived;

    @Column(name = "self_interest_overdue_derived", scale = 6, precision = 19)
    private BigDecimal selfInterestOverdueDerived;

    @Column(name = "partner_interest_overdue_derived", scale = 6, precision = 19)
    private BigDecimal partnerInterestOverdueDerived;

    @Column(name = "total_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalOverDueDerived;

    @Column(name = "self_total_overdue_derived", scale = 6, precision = 19)
    private BigDecimal selfTotalOverDueDerived;

    @Column(name = "partner_total_overdue_derived", scale = 6, precision = 19)
    private BigDecimal partnerTotalOverDueDerived;

    @Column(name = "overdue_since_date_derived")
    private LocalDate overdueDinceDateDerived;

    @Column(name = "bounce_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalBounceChargesCharged;

    @Column(name = "bounce_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalBounceChargesRepaid;

    @Column(name = "bounce_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalBounceChargesWaived;

    @Column(name = "bounce_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalBounceChargesWrittenOff;

    @Column(name = "bounce_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalBounceChargesOutstanding;

    @Column(name = "self_bounce_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal selfBounceChargesChargedDerived;

    @Column(name = "self_bounce_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal selfBounceChargesOutstandingDerived;

    @Column(name = "self_bounce_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal selfBounceChargesRepaidDerived;

    @Column(name = "self_bounce_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal selfBounceChargesWaivedDerived;

    @Column(name = "self_bounce_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal selfBounceChargesWrittenDerived;

    @Column(name = "partner_bounce_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal partnerBounceChargesChargedDerived;

    @Column(name = "partner_bounce_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal partnerBounceChargesOutstandingDerived;

    @Column(name = "partner_bounce_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal partnerBounceChargesRepaidDerived;

    @Column(name = "partner_bounce_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal partnerBounceChargesWaivedDerived;

    @Column(name = "partner_bounce_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal partnerBounceChargesWrittenDerived;

    @Column(name = "cooling_off_reversed_charge_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal coolingOffReversedChargeAmount;


    public static LoanSummary create(final BigDecimal totalFeeChargesDueAtDisbursement,final BigDecimal feeChargesSelfDueAtDisbursement,final BigDecimal feeChargesPartnerDueAtDisbursement) {
        return new LoanSummary(totalFeeChargesDueAtDisbursement, feeChargesSelfDueAtDisbursement, feeChargesPartnerDueAtDisbursement);
    }


    LoanSummary() {
        //
    }

    private LoanSummary(final BigDecimal totalFeeChargesDueAtDisbursement,final BigDecimal feeChargesSelfDueAtDisbursement,final BigDecimal feeChargesPartnerDueAtDisbursement) {
        this.totalFeeChargesDueAtDisbursement = totalFeeChargesDueAtDisbursement;
        this.totalSelfFeeChargesDueAtDisbursement = feeChargesSelfDueAtDisbursement;
        this.totalPartnerFeeChargesDueAtDisbursement = feeChargesPartnerDueAtDisbursement;
    }


    public void updateGstAmout(BigDecimal totalGst, BigDecimal selfGst, BigDecimal partnerGst,Boolean isDisbursement) {
        if(totalGst!=null){
        this.totalGstDerived=totalGst;
            if(isDisbursement) {
                this.totalGstPaid = getTotalGstPaid();
            }
            this.totalGstOutstanding =getTotalGstDerived().subtract(this.totalGstPaid);}
            if(selfGst!=null){
            this.totalSelfGstDerived = getTotalSelfGstDerived().add(selfGst);
            if(isDisbursement) {
                this.totalSelfGstPaid = getTotalSelfGstPaid();
            }
            this.totalSelfGstOutstanding = getTotalSelfGstDerived().subtract(this.totalSelfGstPaid);}
            if(partnerGst!=null){
                this.totalPartnerGstDerived = getTotalPartnerGstDerived().add(partnerGst);
            if(isDisbursement) {
                this.totalPartnerGstPaid = getTotalPartnerGstPaid();
            }
        this.totalPartnerGstOutstanding = getTotalPartnerGstDerived().subtract(this.totalPartnerGstPaid);}
    }
    public void updateGstAmout(BigDecimal totalGst, BigDecimal selfGst, BigDecimal partnerGst) {

        if(totalGst!=null){
            this.totalGstDerived=getTotalGstDerived().add(totalGst);
            this.totalGstPaid = getTotalGstPaid();
            this.totalGstOutstanding =getTotalGstPaid().subtract(getTotalGstDerived());}
        if(selfGst!=null){
            this.totalSelfGstDerived = getTotalSelfGstDerived().add(selfGst);
            this.totalSelfGstPaid = getTotalSelfGstPaid();
            this.totalSelfGstOutstanding = getTotalSelfGstPaid().subtract(getTotalSelfGstDerived());}
        if(partnerGst!=null){
            this.totalPartnerGstDerived = getTotalPartnerGstDerived().add(partnerGst);
            this.totalPartnerGstPaid = getTotalPartnerGstPaid();
            this.totalPartnerGstOutstanding = getTotalPartnerGstPaid().subtract(getTotalPartnerGstDerived());}
    }

    public void updateTotalFeeChargesDueAtDisbursement(final BigDecimal totalFeeChargesDueAtDisbursement,final BigDecimal feeChargesSelfDueAtDisbursement,final BigDecimal feeChargesPartnerDueAtDisbursement) {
        this.totalFeeChargesDueAtDisbursement = totalFeeChargesDueAtDisbursement;
        this.totalSelfFeeChargesDueAtDisbursement = feeChargesSelfDueAtDisbursement;
        this.totalPartnerFeeChargesDueAtDisbursement = feeChargesPartnerDueAtDisbursement;
    }

    public void updateTotalGstDueAtDisbursement(final BigDecimal totalGstDueAtDisbursement, final BigDecimal totalSelfGstDueAtDisbursement, final BigDecimal totalPartnerGstDueAtDisbursement) {
        this.totalGstDueAtDisbursement = totalGstDueAtDisbursement!=null? totalGstDueAtDisbursement : BigDecimal.ZERO;
        this.totalSelfGstDueAtDisbursement = totalSelfGstDueAtDisbursement !=null ? totalSelfGstDueAtDisbursement : BigDecimal.ZERO ;
        this.totalPartnerGstDueAtDisbursement = totalPartnerGstDueAtDisbursement != null ? totalPartnerGstDueAtDisbursement : BigDecimal.ZERO;
    }

    public Money getTotalFeeChargesDueAtDisbursement(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalFeeChargesDueAtDisbursement);
    }

    public BigDecimal getTotalFeeChargesDueAtDisbursement() {
        return totalFeeChargesDueAtDisbursement;
    }

    public Money getTotalGstDueAtDisbursement(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalGstDueAtDisbursement);
    }

    public BigDecimal getTotalGstDueAtDisbursement() {
        return totalGstDueAtDisbursement != null ? totalGstDueAtDisbursement: BigDecimal.ZERO ;
    }

    public BigDecimal getTotalSelfGstDueAtDisbursement() {
        return totalFeeChargesDueAtDisbursement != null ? totalFeeChargesDueAtDisbursement: BigDecimal.ZERO ;
    }

    public BigDecimal getTotalPartnerGstDueAtDisbursement() {
        return totalPartnerGstDueAtDisbursement != null ? totalPartnerGstDueAtDisbursement: BigDecimal.ZERO ;
    }



    public Money getTotalSelfGstDueAtDisbursement(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalSelfGstDueAtDisbursement);
    }

    public Money getTotalPartnerGstDueAtDisbursement(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalPartnerGstDueAtDisbursement);
    }

    public Money getTotalOutstanding(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalOutstanding);
    }

    public Money getTotalSelfOutstanding(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalSelfOutstanding);
    }

    public Money getTotalPartnerOutstanding(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalPartnerOutstanding);
    }

    public void updateFeeChargeOutstanding(final BigDecimal totalFeeChargesOutstanding) {
        this.totalFeeChargesOutstanding = totalFeeChargesOutstanding;
    }

    public void updateFeeChargesWaived(final BigDecimal totalFeeChargesWaived) {
        this.totalFeeChargesWaived = totalFeeChargesWaived;
    }

    public void updateSelfFeeChargesWaived(final BigDecimal totalSelfFeeChargesWaived) {
        this.totalSelfFeeChargesWaived = totalSelfFeeChargesWaived;
    }

    public  BigDecimal getTotalGstDerived (){
        if(this.totalGstDerived == null){
            this.totalGstDerived = BigDecimal.ZERO;
        }
        return this.totalGstDerived;
    }

    public  BigDecimal getTotalGstPaid (){
        if(this.totalGstDerived == null){
            this.totalGstDerived = BigDecimal.ZERO;
        }
        return this.totalGstDerived;
    }

    public  BigDecimal getTotalGstOutstanding(){
        if(this.totalGstDerived == null){
            this.totalGstDerived = BigDecimal.ZERO;
        }
        return this.totalGstDerived;
    }
    public  BigDecimal getTotalSelfGstDerived (){
        if(this.totalSelfGstDerived == null){
            this.totalSelfGstDerived = BigDecimal.ZERO;
        }
        return this.totalSelfGstDerived;
    }

    public  BigDecimal getTotalSelfGstPaid (){
        if(this.totalSelfGstDerived == null){
            this.totalSelfGstDerived = BigDecimal.ZERO;
        }
        return this.totalSelfGstDerived;
    }

    public  BigDecimal getTotalSelfGstOutstanding(){
        if(this.totalSelfGstDerived == null){
            this.totalSelfGstDerived = BigDecimal.ZERO;
        }
        return this.totalSelfGstDerived;
    }


    public  BigDecimal getTotalPartnerGstDerived (){
        if(this.totalPartnerGstDerived == null){
            this.totalPartnerGstDerived = BigDecimal.ZERO;
        }
        return this.totalPartnerGstDerived;
    }

    public  BigDecimal getTotalPartnerGstPaid (){
        if(this.totalPartnerGstDerived == null){
            this.totalPartnerGstDerived = BigDecimal.ZERO;
        }
        return this.totalPartnerGstDerived;
    }

    public  BigDecimal getTotalPartnerGstOutstanding(){
        if(this.totalPartnerGstDerived == null){
            this.totalPartnerGstDerived = BigDecimal.ZERO;
        }
        return this.totalPartnerGstDerived;
    }

    public void updatePartnerFeeChargesWaived(final BigDecimal totalPartnerFeeChargesWaived) {
        this.totalPartnerFeeChargesWaived = totalPartnerFeeChargesWaived;
    }

    public boolean isRepaidInFull(final MonetaryCurrency currency) {
        return getTotalOutstanding(currency).isZero();
    }

    public BigDecimal getTotalInterestCharged() {
        return this.totalInterestCharged;
    }

    public BigDecimal getTotalSelfInterestCharged() {
        return this.totalSelfInterestCharged;
    }

    public BigDecimal getTotalPartnerInterestCharged() {
        return this.totalPartnerInterestCharged;
    }

    public BigDecimal getTotalPrincipalOutstanding() {
        return this.totalPrincipalOutstanding;
    }

    public BigDecimal getTotalSelfPrincipalOutstanding() {
        return totalSelfPrincipalOutstanding;
    }

    public BigDecimal getTotalPartnerPrincipalOutstanding() {
        return totalPartnerPrincipalOutstanding;
    }


    public BigDecimal getTotalInterestOutstanding() {
        return this.totalInterestOutstanding;
    }

    public BigDecimal getTotalSelfInterestOutstanding() {
        return this.totalSelfInterestOutstanding;
    }

    public BigDecimal getTotalPartnerInterestOutstanding() {
        return this.totalPartnerInterestOutstanding;
    }


    public BigDecimal getTotalFeeChargesOutstanding() {
        return this.totalFeeChargesOutstanding;
    }

    public BigDecimal getTotalPenaltyChargesOutstanding() {
        return this.totalPenaltyChargesOutstanding;
    }

    public BigDecimal getTotalOutstanding() {
        return this.totalOutstanding;
    }

    public BigDecimal getTotalSelfOutstanding() {
        return this.totalSelfOutstanding;
    }

    public BigDecimal getTotalPartnerOutstanding() {
        return this.totalPartnerOutstanding;
    }

    public void setTotalPrincipalDisbursed(BigDecimal totalPrincipalDisbursed) {
        this.totalPrincipalDisbursed = totalPrincipalDisbursed;
    }

    public BigDecimal getSelfBounceChargesChargedDerived() {
        return selfBounceChargesChargedDerived;
    }

    public void setSelfBounceChargesChargedDerived(BigDecimal selfBounceChargesChargedDerived) {
        this.selfBounceChargesChargedDerived = selfBounceChargesChargedDerived;
    }

    public BigDecimal getSelfBounceChargesOutstandingDerived() {
        return selfBounceChargesOutstandingDerived;
    }

    public void setSelfBounceChargesOutstandingDerived(BigDecimal selfBounceChargesOutstandingDerived) {
        this.selfBounceChargesOutstandingDerived = selfBounceChargesOutstandingDerived;
    }

    public BigDecimal getSelfBounceChargesRepaidDerived() {
        return selfBounceChargesRepaidDerived;
    }

    public void setSelfBounceChargesRepaidDerived(BigDecimal selfBounceChargesRepaidDerived) {
        this.selfBounceChargesRepaidDerived = selfBounceChargesRepaidDerived;
    }

    public BigDecimal getSelfBounceChargesWaivedDerived() {
        return selfBounceChargesWaivedDerived;
    }

    public void setSelfBounceChargesWaivedDerived(BigDecimal selfBounceChargesWaivedDerived) {
        this.selfBounceChargesWaivedDerived = selfBounceChargesWaivedDerived;
    }

    public BigDecimal getSelfBounceChargesWrittenDerived() {
        return selfBounceChargesWrittenDerived;
    }

    public void setSelfBounceChargesWrittenDerived(BigDecimal selfBounceChargesWrittenDerived) {
        this.selfBounceChargesWrittenDerived = selfBounceChargesWrittenDerived;
    }

    public BigDecimal getPartnerBounceChargesChargedDerived() {
        return partnerBounceChargesChargedDerived;
    }

    public void setPartnerBounceChargesChargedDerived(BigDecimal partnerBounceChargesChargedDerived) {
        this.partnerBounceChargesChargedDerived = partnerBounceChargesChargedDerived;
    }

    public BigDecimal getPartnerBounceChargesOutstandingDerived() {
        return partnerBounceChargesOutstandingDerived;
    }

    public void setPartnerBounceChargesOutstandingDerived(BigDecimal partnerBounceChargesOutstandingDerived) {
        this.partnerBounceChargesOutstandingDerived = partnerBounceChargesOutstandingDerived;
    }

    public BigDecimal getPartnerBounceChargesRepaidDerived() {
        return partnerBounceChargesRepaidDerived;
    }

    public void setPartnerBounceChargesRepaidDerived(BigDecimal partnerBounceChargesRepaidDerived) {
        this.partnerBounceChargesRepaidDerived = partnerBounceChargesRepaidDerived;
    }

    public BigDecimal getPartnerBounceChargesWaivedDerived() {
        return partnerBounceChargesWaivedDerived;
    }

    public void setPartnerBounceChargesWaivedDerived(BigDecimal partnerBounceChargesWaivedDerived) {
        this.partnerBounceChargesWaivedDerived = partnerBounceChargesWaivedDerived;
    }

    public BigDecimal getPartnerBounceChargesWrittenDerived() {
        return partnerBounceChargesWrittenDerived;
    }

    public void setPartnerBounceChargesWrittenDerived(BigDecimal partnerBounceChargesWrittenDerived) {
        this.partnerBounceChargesWrittenDerived = partnerBounceChargesWrittenDerived;
    }

    /**
     * All fields but <code>totalFeeChargesDueAtDisbursement</code> should be reset.
     */
    public void zeroFields() {
        this.totalPrincipalDisbursed = BigDecimal.ZERO;
        this.totalPrincipalRepaid = BigDecimal.ZERO;
        this.totalPrincipalWrittenOff = BigDecimal.ZERO;
        this.totalPrincipalOutstanding = BigDecimal.ZERO;
        this.totalSelfPrincipalOutstanding = BigDecimal.ZERO;
        this.totalPartnerPrincipalOutstanding = BigDecimal.ZERO;

        this.totalInterestCharged = BigDecimal.ZERO;
        this.totalSelfInterestCharged = BigDecimal.ZERO;
        this.totalPartnerInterestCharged = BigDecimal.ZERO;
        this.totalInterestRepaid = BigDecimal.ZERO;
        this.totalSelfInterestRepaid = BigDecimal.ZERO;
        this.totalPartnerInterestRepaid = BigDecimal.ZERO;

        this.totalInterestWaived = BigDecimal.ZERO;
        this.totalSelfInterestWaived = BigDecimal.ZERO;
        this.totalPartnerInterestWaived = BigDecimal.ZERO;
        this.totalInterestWrittenOff = BigDecimal.ZERO;
        this.totalInterestOutstanding = BigDecimal.ZERO;
        this.totalSelfInterestOutstanding = BigDecimal.ZERO;
        this.totalPartnerInterestOutstanding = BigDecimal.ZERO;

        this.totalFeeChargesCharged = BigDecimal.ZERO;
        this.totalSelfFeeChargesCharged = BigDecimal.ZERO;
        this.totalPartnerFeeChargesCharged = BigDecimal.ZERO;
        this.totalFeeChargesRepaid = BigDecimal.ZERO;
        this.totalSelfFeeChargesRepaid = BigDecimal.ZERO;
        this.totalPartnerFeeChargesRepaid = BigDecimal.ZERO;
        this.totalFeeChargesWaived = BigDecimal.ZERO;
        this.totalSelfFeeChargesWaived = BigDecimal.ZERO;
        this.totalPartnerFeeChargesWaived = BigDecimal.ZERO;
        this.totalFeeChargesWrittenOff = BigDecimal.ZERO;
        this.totalSelfFeeChargesWrittenOff = BigDecimal.ZERO;
        this.totalPartnerFeeChargesWrittenOff = BigDecimal.ZERO;
        this.totalFeeChargesOutstanding = BigDecimal.ZERO;
        this.totalSelfFeeChargesOutstanding = BigDecimal.ZERO;
        this.totalPartnerFeeChargesOutstanding = BigDecimal.ZERO;
        this.totalPenaltyChargesCharged = BigDecimal.ZERO;
        this.totalPenaltyChargesRepaid = BigDecimal.ZERO;
        this.totalPenaltyChargesWaived = BigDecimal.ZERO;
        this.totalPenaltyChargesWrittenOff = BigDecimal.ZERO;
        this.totalPenaltyChargesOutstanding = BigDecimal.ZERO;
        this.totalExpectedRepayment = BigDecimal.ZERO;
        this.totalRepayment = BigDecimal.ZERO;
        this.totalSelfRepayment = BigDecimal.ZERO;
        this.totalPartnerRepayment = BigDecimal.ZERO;
        this.totalExpectedCostOfLoan = BigDecimal.ZERO;
        this.totalCostOfLoan = BigDecimal.ZERO;
        this.totalWaived = BigDecimal.ZERO;
        this.totalWrittenOff = BigDecimal.ZERO;
        this.totalOutstanding = BigDecimal.ZERO;
        this.totalSelfPrincipalRepaid = BigDecimal.ZERO;
        this.totalPartnerPrincipalRepaid = BigDecimal.ZERO;
        this.totalSelfOutstanding = BigDecimal.ZERO;
        this.totalPartnerOutstanding = BigDecimal.ZERO;
        this.principalOverdueDerived = BigDecimal.ZERO;
        this.selfPrincipalOverdueDerived = BigDecimal.ZERO;
        this.partnerPrincipalOverdueDerived = BigDecimal.ZERO;
        this.interestOverdueDerived = BigDecimal.ZERO;
        this.selfInterestOverdueDerived = BigDecimal.ZERO;
        this.partnerInterestOverdueDerived = BigDecimal.ZERO;
        this.totalOverDueDerived = BigDecimal.ZERO;
        this.selfTotalOverDueDerived = BigDecimal.ZERO;
        this.partnerTotalOverDueDerived = BigDecimal.ZERO;
        this.overdueDinceDateDerived =null;



    }

    public void updateSummary(final MonetaryCurrency currency, final Money principal,
            final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, final LoanSummaryWrapper summaryWrapper,
            final Boolean disbursed, List<LoanCharge> charges, final BigDecimal selfPrincipalAmount,final BigDecimal partnerPrincipalAmount) {

        this.totalPrincipalDisbursed = principal.getAmount();
        this.totalPrincipalRepaid = summaryWrapper.calculateTotalPrincipalRepaid(repaymentScheduleInstallments);

        //total Gst

        final BigDecimal totalGst=summaryWrapper.calculateTotalGst(charges);

        //used to update the repayment table
        this.totalSelfPrincipalRepaid = summaryWrapper.calculateSelfTotalPrincipalRepaid(repaymentScheduleInstallments);
        this.totalPartnerPrincipalRepaid = summaryWrapper.calculatePartnerTotalPrincipalRepaid(repaymentScheduleInstallments);
        this.totalPrincipalWrittenOff = summaryWrapper.calculateTotalPrincipalWrittenOff(repaymentScheduleInstallments);

        this.totalPrincipalOutstanding = principal.getAmount().subtract(this.totalPrincipalRepaid).subtract(this.totalPrincipalWrittenOff);
        this.totalSelfPrincipalOutstanding = selfPrincipalAmount.subtract(this.totalSelfPrincipalRepaid).subtract(this.totalPrincipalWrittenOff);
        this.totalPartnerPrincipalOutstanding = partnerPrincipalAmount.subtract(this.totalPartnerPrincipalRepaid).subtract(this.totalPrincipalWrittenOff);

        final BigDecimal totalInterestCharged = summaryWrapper.calculateTotalInterestCharged(repaymentScheduleInstallments);
        final BigDecimal totalSelfInterestCharged = summaryWrapper.calculateTotalSelfInterestCharged(repaymentScheduleInstallments);
        final BigDecimal totalPartnerInterestCharged = summaryWrapper.calculateTotalPartnerInterestCharged(repaymentScheduleInstallments);
        this.totalInterestCharged = totalInterestCharged;
        this.totalSelfInterestCharged = totalSelfInterestCharged;
        this.totalPartnerInterestCharged = totalPartnerInterestCharged;
        this.totalInterestRepaid = summaryWrapper.calculateTotalInterestRepaid(repaymentScheduleInstallments);
        this.totalSelfInterestRepaid = summaryWrapper.calculateTotalSelfInterestRepaid(repaymentScheduleInstallments);
        this.totalPartnerInterestRepaid = summaryWrapper.calculateTotalPartnerInterestRepaid(repaymentScheduleInstallments);
        this.totalInterestWaived = summaryWrapper.calculateTotalInterestWaived(repaymentScheduleInstallments);
        this.totalSelfInterestWaived = summaryWrapper.calculateTotalSelfInterestWaived(repaymentScheduleInstallments);
        this.totalPartnerInterestWaived = summaryWrapper.calculateTotalPartnerInterestWaived(repaymentScheduleInstallments);

        this.totalInterestWrittenOff = summaryWrapper.calculateTotalInterestWrittenOff(repaymentScheduleInstallments);

        this.totalInterestOutstanding = totalInterestCharged.subtract(this.totalInterestRepaid).subtract(this.totalInterestWaived)
                .subtract(this.totalInterestWrittenOff);

        this.totalSelfInterestOutstanding = totalSelfInterestCharged.subtract(this.totalSelfInterestRepaid).subtract(this.totalSelfInterestWaived)
                .subtract(this.totalInterestWrittenOff);

        this.totalPartnerInterestOutstanding = totalPartnerInterestCharged.subtract(this.totalPartnerInterestRepaid).subtract(this.totalPartnerInterestWaived)
                .subtract(this.totalInterestWrittenOff);

        final BigDecimal totalAdhocCharges = summaryWrapper.calculateAhocCharge(charges);
        final BigDecimal totalSelfAdhocCharges = summaryWrapper.calculateSelfAhocCharge(charges);
        final BigDecimal totalPartnerAdhocCharges = summaryWrapper.calculatePartnerAhocCharge(charges);
        final BigDecimal totalGstAtDisbursement = summaryWrapper.calculateTotalGstDisbursement(charges);
        final BigDecimal totalSelfGstAtDisbursement = summaryWrapper.calculateTotalSelfGstDisbursement(charges);
        final BigDecimal totalPartnerGstAtDisbursement = summaryWrapper.calculateTotalPartnerGstDisbursement(charges);
        final BigDecimal totalFeeChargesCharged = summaryWrapper.calculateTotalFeeChargesCharged(repaymentScheduleInstallments)
                .add(this.totalFeeChargesDueAtDisbursement).add(totalAdhocCharges).add(totalGstAtDisbursement);
        this.totalFeeChargesCharged = totalFeeChargesCharged;

        final BigDecimal totalSelfFeeChargesCharged = summaryWrapper.calculateTotalSelfFeeChargesCharged(repaymentScheduleInstallments)
                .add(this.totalSelfFeeChargesDueAtDisbursement).add(totalSelfAdhocCharges).add(totalSelfGstAtDisbursement);
        this.totalSelfFeeChargesCharged = totalSelfFeeChargesCharged;

        final BigDecimal totalPartnerFeeChargesCharged = summaryWrapper.calculateTotalPartnerFeeChargesCharged(repaymentScheduleInstallments)
                .add(this.totalPartnerFeeChargesDueAtDisbursement).add(totalPartnerAdhocCharges).add(totalPartnerGstAtDisbursement);
        this.totalPartnerFeeChargesCharged = totalPartnerFeeChargesCharged;

        BigDecimal totalFeeChargesRepaidAtDisbursement = summaryWrapper.calculateTotalChargesRepaidAtDisbursement(charges);
        BigDecimal totalChargesAtDisbursement = summaryWrapper.calculateTotalChargeDisbursement(charges);
        BigDecimal totalFeeChargesRepaidAtForeClosure = summaryWrapper.calculateTotalChargesRepaidAtForeclosure(repaymentScheduleInstallments);
        this.totalFeeChargesRepaid = totalFeeChargesRepaidAtDisbursement.add(totalFeeChargesRepaidAtForeClosure).add(totalGstAtDisbursement);
        //this.totalFeeChargesRepaid=new BigDecimal(111);
        BigDecimal totalSelfFeeChargesRepaidAtDisbursement = summaryWrapper.calculateTotalSelfChargesRepaidAtDisbursement(charges);
        BigDecimal totalSelfChargesAtDisbursement = summaryWrapper.calculateTotalSelfChargeDisbursement(charges);
        BigDecimal totalSelfFeeChargesRepaidAtForeClosure = summaryWrapper.calculateTotalSelfChargesRepaidAtForeclosure(repaymentScheduleInstallments);
        this.totalSelfFeeChargesRepaid = totalSelfFeeChargesRepaidAtDisbursement.add(totalSelfFeeChargesRepaidAtForeClosure).add(totalSelfGstAtDisbursement);


        BigDecimal totalPartnerFeeChargesRepaidAtDisbursement = summaryWrapper.calculateTotalPartnerChargesRepaidAtDisbursement(charges);
        BigDecimal totalPartnerChargesAtDisbursement = summaryWrapper.calculateTotalPartnerChargeDisbursement(charges);
        BigDecimal totalPartnerFeeChargesRepaidAtForeClosure = summaryWrapper.calculateTotalPartnerChargesRepaidAtForeclosure(repaymentScheduleInstallments);
        this.totalPartnerFeeChargesRepaid = totalPartnerFeeChargesRepaidAtDisbursement.add(totalPartnerFeeChargesRepaidAtForeClosure).add(totalPartnerGstAtDisbursement);


        if (charges != null) {
            this.totalFeeChargesWaived = summaryWrapper.calculateTotalFeeChargesWaived(charges);
        } else {
            this.totalFeeChargesWaived = BigDecimal.ZERO;
        }

        this.totalFeeChargesWrittenOff = summaryWrapper.calculateTotalFeeChargesWrittenOff(repaymentScheduleInstallments);
        this.totalSelfFeeChargesWrittenOff = summaryWrapper.calculateTotalSelfFeeChargesWrittenOff(repaymentScheduleInstallments);
        this.totalPartnerFeeChargesWrittenOff = summaryWrapper.calculateTotalPartnerFeeChargesWrittenOff(repaymentScheduleInstallments);

        this.totalFeeChargesOutstanding = totalFeeChargesCharged.subtract(this.totalFeeChargesRepaid).subtract(this.totalFeeChargesWaived)
                .subtract(this.totalFeeChargesWrittenOff);

        this.totalSelfFeeChargesOutstanding = totalSelfFeeChargesCharged.subtract(this.totalSelfFeeChargesRepaid).subtract(this.totalSelfFeeChargesWaived)
                .subtract(this.totalSelfFeeChargesWrittenOff);

        this.totalPartnerFeeChargesOutstanding = totalPartnerFeeChargesCharged.subtract(this.totalPartnerFeeChargesRepaid).subtract(this.totalPartnerFeeChargesWaived)
                .subtract(this.totalPartnerFeeChargesWrittenOff);

        final BigDecimal totalPenaltyChargesCharged = summaryWrapper.calculateTotalPenaltyChargesCharged(repaymentScheduleInstallments);
        this.totalPenaltyChargesCharged = totalPenaltyChargesCharged;
        this.totalPenaltyChargesRepaid = summaryWrapper.calculateTotalPenaltyChargesRepaid(repaymentScheduleInstallments);

        final BigDecimal totalBounceChargesCharged = summaryWrapper.calculateTotalBounceChargesCharged(repaymentScheduleInstallments);
        this.totalBounceChargesCharged=totalBounceChargesCharged;
        this.totalBounceChargesRepaid=summaryWrapper.calculateTotalBounceChargesRepaid(repaymentScheduleInstallments);
        this.totalBounceChargesWaived = summaryWrapper.calculateTotalBounceChargesWaived(repaymentScheduleInstallments);
        this.totalBounceChargesWrittenOff = summaryWrapper.calculateTotalBounceChargesWrittenOff(repaymentScheduleInstallments);
        this.totalBounceChargesOutstanding = totalBounceChargesCharged.subtract(this.totalBounceChargesRepaid)
                .subtract(this.totalBounceChargesWaived).subtract(this.totalBounceChargesWrittenOff);


        this.totalPenaltyChargesWaived = summaryWrapper.calculateTotalPenaltyChargesWaived(repaymentScheduleInstallments);
        this.totalPenaltyChargesWrittenOff = summaryWrapper.calculateTotalPenaltyChargesWrittenOff(repaymentScheduleInstallments);
        this.totalPenaltyChargesOutstanding = totalPenaltyChargesCharged.subtract(this.totalPenaltyChargesRepaid)
                .subtract(this.totalPenaltyChargesWaived).subtract(this.totalPenaltyChargesWrittenOff);

        /**
         * Penal Split Logics
         */
        BigDecimal selfPenalChargedDerived = summaryWrapper.calculateSelfPenalShareAmountTotal(repaymentScheduleInstallments);
        this.selfPenaltyChargesChargedDerived = selfPenalChargedDerived;
        this.selfPenaltyChargesRepaidDerived = summaryWrapper.calculateSelfPenalChargesRepaidTotal(repaymentScheduleInstallments);
        this.selfPenaltyChargesWaivedDerived = summaryWrapper.calculateSelfPenalWaivedChargesTotal(repaymentScheduleInstallments);
        this.selfPenaltyChargesWrittenDerived = summaryWrapper.calculateSelfPenalWrittenOffChargesTotal(repaymentScheduleInstallments, currency);
        this.selfPenaltyChargesOutstandingDerived = selfPenalChargedDerived.subtract(this.selfPenaltyChargesRepaidDerived)
                .subtract(this.selfPenaltyChargesWaivedDerived).subtract(this.selfPenaltyChargesWrittenDerived);

        BigDecimal selfBounceChargedDerived = summaryWrapper.calculateSelfBounceShareAmountTotal(repaymentScheduleInstallments);
        this.selfBounceChargesChargedDerived = selfBounceChargedDerived;
        this.selfBounceChargesRepaidDerived = summaryWrapper.calculateSelfBounceChargesRepaidTotal(repaymentScheduleInstallments);
        this.selfBounceChargesWaivedDerived = summaryWrapper.calculateSelfBounceWaivedChargesTotal(repaymentScheduleInstallments);
        this.selfBounceChargesWrittenDerived = summaryWrapper.calculateSelfBounceWrittenOffChargesTotal(repaymentScheduleInstallments, currency);
        this.selfBounceChargesOutstandingDerived = selfBounceChargedDerived.subtract(this.selfBounceChargesRepaidDerived)
                .subtract(this.selfBounceChargesWaivedDerived).subtract(this.selfBounceChargesWrittenDerived);



        BigDecimal partnerPenalChargedDerived = summaryWrapper.calculatePartnerPenalShareAmountTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesChargedDerived = partnerPenalChargedDerived;
        this.partnerPenaltyChargesRepaidDerived = summaryWrapper.calculatePartnerPenalChargesRepaidTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesWaivedDerived = summaryWrapper.calculatePartnerPenalWaivedChargesTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesWrittenDerived = summaryWrapper.calculatePartnerPenalWrittenOffChargesTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesOutstandingDerived = partnerPenalChargedDerived.subtract(this.partnerPenaltyChargesRepaidDerived)
                .subtract(this.partnerPenaltyChargesWaivedDerived).subtract(this.partnerPenaltyChargesWrittenDerived);

        BigDecimal partnerBounceChargedDerived = summaryWrapper.calculatePartnerBounceShareAmountTotal(repaymentScheduleInstallments);
        this.partnerBounceChargesChargedDerived = partnerBounceChargedDerived;
        this.partnerBounceChargesRepaidDerived = summaryWrapper.calculatePartnerBounceChargesRepaidTotal(repaymentScheduleInstallments);
        this.partnerBounceChargesWaivedDerived = summaryWrapper.calculatePartnerBounceWaivedChargesTotal(repaymentScheduleInstallments);
        this.partnerBounceChargesWrittenDerived = summaryWrapper.calculatePartnerBounceWrittenOffChargesTotal(repaymentScheduleInstallments);
        this.partnerBounceChargesOutstandingDerived = partnerBounceChargedDerived.subtract(this.partnerBounceChargesRepaidDerived)
                .subtract(this.partnerBounceChargesWaivedDerived).subtract(this.partnerBounceChargesWrittenDerived);


//        final Money totalExpectedRepayment = Money.of(currency, this.totalPrincipalDisbursed).plus(this.totalInterestCharged)
//                .plus(this.totalFeeChargesCharged).plus(this.totalPenaltyChargesCharged);
        final BigDecimal totalExpectedRepayment = this.totalPrincipalDisbursed.add (this.totalInterestCharged)
                .add(this.totalFeeChargesCharged).add(this.totalPenaltyChargesCharged).add(this.totalBounceChargesCharged);
        this.totalExpectedRepayment = totalExpectedRepayment;


//        final Money totalRepayment = Money.of(currency, this.totalPrincipalRepaid).plus(this.totalInterestRepaid)
//                .plus(this.totalFeeChargesRepaid).plus(this.totalPenaltyChargesRepaid);
//        this.totalRepayment = totalRepayment.getAmount();

        final BigDecimal totalRepayment =  this.totalPrincipalRepaid.add(this.totalInterestRepaid)
                .add(this.totalFeeChargesRepaid).add(this.totalPenaltyChargesRepaid).add(this.totalBounceChargesRepaid);
        this.totalRepayment = totalRepayment;

        final BigDecimal totalSelfRepayment = this.totalSelfPrincipalRepaid.add(this.totalSelfInterestRepaid)
                .add(this.totalSelfFeeChargesRepaid).add(this.selfPenaltyChargesRepaidDerived).add(this.selfBounceChargesRepaidDerived);
        this.totalSelfRepayment = totalSelfRepayment;


        final BigDecimal totalPartnerRepayment = this.totalPartnerPrincipalRepaid.add(this.totalPartnerInterestRepaid)
                .add(this.totalPartnerFeeChargesRepaid).add(this.partnerPenaltyChargesRepaidDerived).add(this.partnerBounceChargesRepaidDerived);
        this.totalPartnerRepayment = totalPartnerRepayment;

        final BigDecimal totalExpectedCostOfLoan =this.totalInterestCharged.add(this.totalFeeChargesCharged)
                .add(this.totalPenaltyChargesCharged);
        this.totalExpectedCostOfLoan = totalExpectedCostOfLoan;

        final BigDecimal totalCostOfLoan =  this.totalInterestRepaid.add(this.totalFeeChargesRepaid)
                .add(this.totalPenaltyChargesRepaid);
        this.totalCostOfLoan = totalCostOfLoan;

        final BigDecimal totalWaived = this.totalInterestWaived.add(this.totalFeeChargesWaived)
                .add(this.totalPenaltyChargesWaived).add(Objects.requireNonNullElse(this.getTotalBounceChargesWaived(),BigDecimal.ZERO));
        this.totalWaived = totalWaived;

        final BigDecimal totalWrittenOff = this.totalPrincipalWrittenOff.add(this.totalInterestWrittenOff)
                .add(this.totalFeeChargesWrittenOff).add(this.totalPenaltyChargesWrittenOff);
        this.totalWrittenOff = totalWrittenOff;

        final BigDecimal totalOutstanding = this.totalPrincipalOutstanding.add(this.totalInterestOutstanding)
                .add(this.totalFeeChargesOutstanding).add(this.totalPenaltyChargesOutstanding).add(this.totalBounceChargesOutstanding);
        this.totalOutstanding = totalOutstanding;

        final BigDecimal totalSelfOutstanding =  this.totalSelfPrincipalOutstanding.add(this.totalSelfInterestOutstanding)
                .add(this.totalSelfFeeChargesOutstanding).add(this.selfPenaltyChargesOutstandingDerived);
        this.totalSelfOutstanding = totalSelfOutstanding;

        final BigDecimal totalPartnerOutstanding = this.totalPartnerPrincipalOutstanding.add(this.totalPartnerInterestOutstanding)
                .add(this.totalPartnerFeeChargesOutstanding).add(this.partnerPenaltyChargesOutstandingDerived);
        this.totalPartnerOutstanding = totalPartnerOutstanding;


    }

    public BigDecimal getTotalPrincipalDisbursed() {
        return this.totalPrincipalDisbursed;
    }

    public BigDecimal getTotalPrincipalRepaid() {
        return this.totalPrincipalRepaid;
    }

    public BigDecimal getTotalWrittenOff() {
        return this.totalWrittenOff;
    }

    /**
     * @return total interest repaid
     **/
    public BigDecimal getTotalInterestRepaid() {
        return this.totalInterestRepaid;
    }

    public BigDecimal getTotalSelfInterestRepaid() {
        return this.totalSelfInterestRepaid;
    }

    public BigDecimal getTotalPartnerInterestRepaid() {
        return this.totalPartnerInterestRepaid;
    }


    public BigDecimal getTotalFeeChargesCharged() {
        return this.totalFeeChargesCharged;
    }

    public BigDecimal getTotalSelfFeeChargesCharged() {
        return this.totalSelfFeeChargesCharged;
    }

    public BigDecimal getTotalPartnerFeeChargesCharged() {
        return this.totalPartnerFeeChargesCharged;
    }

    public BigDecimal getTotalPenaltyChargesCharged() {
        return this.totalPenaltyChargesCharged;
    }

    public BigDecimal getTotalPrincipalWrittenOff() {
        return this.totalPrincipalWrittenOff;
    }

    public BigDecimal getTotalInterestWaived() {
        return this.totalInterestWaived;
    }

    public BigDecimal getTotalSelfInterestWaived() {
        return totalSelfInterestWaived;
    }

    public BigDecimal getTotalPartnerInterestWaived() {
        return totalPartnerInterestWaived;
    }

    public BigDecimal getTotalFeeChargesRepaid() {
        return this.totalFeeChargesRepaid;
    }

    public BigDecimal getTotalFeeChargesWaived() {
        return this.totalFeeChargesWaived;
    }

    public BigDecimal getTotalSelfFeeChargesWaived() {
        return totalSelfFeeChargesWaived;
    }

    public BigDecimal getTotalPartnerFeeChargesWaived() {
        return totalPartnerFeeChargesWaived;
    }

    public BigDecimal getTotalPenaltyChargesRepaid() {
        return this.totalPenaltyChargesRepaid;
    }

    public BigDecimal getTotalPenaltyChargesWaived() {
        return this.totalPenaltyChargesWaived;
    }

    public BigDecimal getTotalExpectedRepayment() {
        return this.totalExpectedRepayment;
    }

    public void updateFeesPaidAndDerivedForAdhocCharge(List<LoanCharge> loanCharge) {
        for (LoanCharge adhocCharge : loanCharge) {
            if (adhocCharge.isAdhocChargeCharge()) {
                this.totalFeeChargesCharged = totalFeeChargesCharged.add(adhocCharge.getAmount());
                this.totalSelfFeeChargesCharged = totalSelfFeeChargesCharged.add(adhocCharge.getAmount());
                this.totalPartnerFeeChargesCharged = totalPartnerFeeChargesCharged.add(adhocCharge.getAmount());
                this.totalFeeChargesRepaid = totalFeeChargesRepaid.add(adhocCharge.getAmount());
            }
        }
    }

    public void updateLoanArrearAgeing(Loan loan) {
        resetLoanArrearAgeing();
        loan.getRepaymentScheduleInstallments().stream().
                filter(installment -> !installment.isObligationsMet() && installment.getDueDate().isBefore(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()))).
                forEach(installment -> {
                    BigDecimal principalOverdue =BigDecimal.ZERO;
                    BigDecimal interestOverdue =BigDecimal.ZERO;
                    BigDecimal feeOverdue =BigDecimal.ZERO;
                    BigDecimal penaltyOverdue =BigDecimal.ZERO;
                    BigDecimal selfPrincipalOverdue =BigDecimal.ZERO;
                    BigDecimal selfInterestOverdue =BigDecimal.ZERO;
                    BigDecimal partnerPrincipalOverdue =BigDecimal.ZERO;
                    BigDecimal partnerInterestOverdue =BigDecimal.ZERO;
                    BigDecimal selfFeeOverdue =BigDecimal.ZERO;
                    BigDecimal partnerFeeOverdue =BigDecimal.ZERO;
                    BigDecimal selfPenaltyOverdue =BigDecimal.ZERO;
                    BigDecimal partnerPenaltyOverdue =BigDecimal.ZERO;


                    principalOverdue = principalOverdue.add(installment.getPrincipalOutstanding(loan.getCurrency()).getAmount());
                    interestOverdue = interestOverdue.add(installment.getInterestOutstanding(loan.getCurrency()).getAmount());
                    feeOverdue = feeOverdue.add(installment.getFeeChargesOutstanding(loan.getCurrency()).getAmount());
                    selfFeeOverdue =selfFeeOverdue.add(installment.getSelfFeeChargesOutstanding(loan.getCurrency()).getAmount());
                    partnerFeeOverdue =partnerFeeOverdue.add(installment.getPartnerFeeChargesOutstanding(loan.getCurrency()).getAmount());
                    selfPenaltyOverdue =selfPenaltyOverdue.add(installment.getSelfPenaltyChargesOutstanding(loan.getCurrency()).getAmount());
                    partnerFeeOverdue =partnerFeeOverdue.add(installment.getPartnerPenaltyChargesOutstanding(loan.getCurrency()).getAmount());
                    penaltyOverdue = penaltyOverdue.add(installment.getPenaltyChargesOutstanding(loan.getCurrency()).getAmount());
                    selfPrincipalOverdue = selfPrincipalOverdue.add(installment.getSelfPrincipalOutstanding(loan.getCurrency()).getAmount());
                    selfInterestOverdue =selfInterestOverdue.add(installment.getSelfInterestOutstanding(loan.getCurrency()).getAmount());
                    partnerPrincipalOverdue =partnerPrincipalOverdue.add(installment.getPartnerPrincipalOutstanding(loan.getCurrency()).getAmount());
                    partnerInterestOverdue =partnerInterestOverdue.add(installment.getPartnerInterestOutstanding(loan.getCurrency()).getAmount());

                    LocalDate overDueSince = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

                    if (installment.isNotFullyPaidOff() && overDueSince.isAfter(installment.getDueDate())) {
                        overDueSince = installment.getDueDate();
                    }

                    BigDecimal totalOverDue = principalOverdue.add(interestOverdue).add(feeOverdue).add(penaltyOverdue);
                    BigDecimal selfTotalOverDue = selfPrincipalOverdue.add(selfInterestOverdue).add(selfFeeOverdue).add(selfPenaltyOverdue);
                    BigDecimal partnerTotalOverDue = partnerPrincipalOverdue.add(partnerInterestOverdue).add(partnerFeeOverdue).add(partnerPenaltyOverdue);
                    if (totalOverDue.compareTo(BigDecimal.ZERO) > 0) {
                        this.principalOverdueDerived = (getPrincipalOverdueDerived()!=null?getPrincipalOverdueDerived():BigDecimal.ZERO).add(principalOverdue);
                        this.interestOverdueDerived =(getInterestOverdueDerived()!=null?getInterestOverdueDerived():BigDecimal.ZERO).add(interestOverdue);
                        this.selfPrincipalOverdueDerived = (getSelfPrincipalOverdueDerived()!=null?getSelfPrincipalOverdueDerived():BigDecimal.ZERO).add(selfPrincipalOverdue);
                        this.selfInterestOverdueDerived = (getSelfInterestOverdueDerived()!=null?getSelfInterestOverdueDerived():BigDecimal.ZERO).add(selfInterestOverdue);
                        this.partnerPrincipalOverdueDerived = (getPartnerPrincipalOverdueDerived()!=null?getPartnerPrincipalOverdueDerived():BigDecimal.ZERO).add(partnerPrincipalOverdue);
                        this.partnerInterestOverdueDerived = (getPartnerInterestOverdueDerived()!=null?getPartnerInterestOverdueDerived():BigDecimal.ZERO).add(partnerInterestOverdue);
                        this.totalOverDueDerived = (getTotalOverDueDerived()!=null?getTotalOverDueDerived():BigDecimal.ZERO).add(totalOverDue);
                        this.selfTotalOverDueDerived = (getSelfTotalOverDueDerived()!=null?getSelfTotalOverDueDerived():BigDecimal.ZERO).add(selfTotalOverDue);
                        this.partnerTotalOverDueDerived = (getPartnerTotalOverDueDerived()!=null?getPartnerTotalOverDueDerived():BigDecimal.ZERO).add(partnerTotalOverDue);
                        this.overdueDinceDateDerived = overDueSince;
                    }});


    }

    public void resetLoanArrearAgeing() {
        this.principalOverdueDerived = BigDecimal.ZERO;
        this.selfPrincipalOverdueDerived = BigDecimal.ZERO;
        this.partnerPrincipalOverdueDerived = BigDecimal.ZERO;
        this.interestOverdueDerived = BigDecimal.ZERO;
        this.selfInterestOverdueDerived = BigDecimal.ZERO;
        this.partnerInterestOverdueDerived = BigDecimal.ZERO;
        this.totalOverDueDerived = BigDecimal.ZERO;
        this.selfTotalOverDueDerived = BigDecimal.ZERO;
        this.partnerTotalOverDueDerived = BigDecimal.ZERO;
        this.overdueDinceDateDerived = null;
    }

    public void setTotalOutstanding(BigDecimal totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }

    public void updateTotalOutstanding(BigDecimal foreClosureChargeAmount,MonetaryCurrency currency) {

        this.totalOutstanding = Money.of(currency, totalOutstanding.add(foreClosureChargeAmount)).getAmount();
    }
    public void updateGstAmoutForBounce(BigDecimal totalGst, BigDecimal selfGst, BigDecimal partnerGst) {
        if(totalGst!=null){
            this.totalGstDerived=getTotalGstDerived().add(totalGst);
           // this.totalGstPaid = getTotalGstPaid();
            this.totalGstOutstanding =this.totalGstPaid.subtract(getTotalGstDerived());}
        if(selfGst!=null){
            this.totalSelfGstDerived = getTotalSelfGstDerived().add(selfGst);
           // this.totalSelfGstPaid = getTotalSelfGstPaid();
            this.totalSelfGstOutstanding = this.totalSelfGstPaid.subtract(getTotalSelfGstDerived());}
        if(partnerGst!=null){
            this.totalPartnerGstDerived = getTotalPartnerGstDerived().add(partnerGst);
           // this.totalPartnerGstPaid = getTotalPartnerGstPaid();
            this.totalPartnerGstOutstanding = this.totalPartnerGstPaid.subtract(getTotalPartnerGstDerived());}
    }
    public boolean isFirstInstallmentRepaid() {
        return getTotalPrincipalRepaid().add(getTotalInterestRepaid()).doubleValue()>0 ;
    }

    public BigDecimal getCoolingOffReversedChargeAmount(){
        return Objects.isNull(this.coolingOffReversedChargeAmount)?BigDecimal.ZERO:this.coolingOffReversedChargeAmount;
    }

    public void setCoolingOffReversedChargeAmount(BigDecimal amount){
        this.coolingOffReversedChargeAmount = getCoolingOffReversedChargeAmount().add(amount);
    }

}
