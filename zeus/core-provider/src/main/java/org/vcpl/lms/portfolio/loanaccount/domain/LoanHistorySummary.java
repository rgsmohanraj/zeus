package org.vcpl.lms.portfolio.loanaccount.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanHistorySummary {

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




    public void updateSummaryForLoanHistory(MonetaryCurrency currency, Money principal, List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
                                            LoanSummaryWrapper summaryWrapper, boolean disbursed, List<LoanCharge> charges, BigDecimal selfPrincipalAmount,
                                            BigDecimal partnerPrincipalAmount) {

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
        BigDecimal totalGstForForeclosure = charges.stream()
                .filter(loanCharge -> loanCharge.getCharge().isForeclosureCharge()).map(LoanCharge::getTotalGst).reduce(BigDecimal.ZERO,BigDecimal::add);
        this.totalFeeChargesRepaid = totalFeeChargesRepaidAtDisbursement.add(totalFeeChargesRepaidAtForeClosure).add(totalGstAtDisbursement).add(totalGstForForeclosure);

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
        this.totalPenaltyChargesWaived = summaryWrapper.calculateTotalPenaltyChargesWaived(repaymentScheduleInstallments);
        this.totalPenaltyChargesWrittenOff = summaryWrapper.calculateTotalPenaltyChargesWrittenOff(repaymentScheduleInstallments);
        this.totalPenaltyChargesOutstanding = totalPenaltyChargesCharged.subtract(this.totalPenaltyChargesRepaid)
                .subtract(this.totalPenaltyChargesWaived).subtract(this.totalPenaltyChargesWrittenOff);
        /**
        //Total Bounce Charge Logic
         **/
        final BigDecimal totalBounceChargesCharged = summaryWrapper.calculateTotalBounceChargesCharged(repaymentScheduleInstallments);
        this.totalBounceChargesCharged = totalBounceChargesCharged;
        this.totalBounceChargesRepaid = summaryWrapper.calculateTotalBounceChargesRepaid(repaymentScheduleInstallments);
        this.totalBounceChargesWaived = summaryWrapper.calculateTotalBounceChargesWaived(repaymentScheduleInstallments);
        this.totalBounceChargesWrittenOff = summaryWrapper.calculateTotalBounceChargesWrittenOff(repaymentScheduleInstallments);
        this.totalBounceChargesOutstanding = totalBounceChargesCharged.subtract(this.totalBounceChargesRepaid)
                .subtract(this.totalBounceChargesWaived).subtract(this.totalBounceChargesWrittenOff);

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

        BigDecimal partnerPenalChargedDerived = summaryWrapper.calculatePartnerPenalShareAmountTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesChargedDerived = partnerPenalChargedDerived;
        this.partnerPenaltyChargesRepaidDerived = summaryWrapper.calculatePartnerPenalChargesRepaidTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesWaivedDerived = summaryWrapper.calculatePartnerPenalWaivedChargesTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesWrittenDerived = summaryWrapper.calculatePartnerPenalWrittenOffChargesTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesOutstandingDerived = partnerPenalChargedDerived.subtract(this.partnerPenaltyChargesRepaidDerived)
                .subtract(this.partnerPenaltyChargesWaivedDerived).subtract(this.partnerPenaltyChargesWrittenDerived);


        /**
         * Bounce Split Logics
         */
        BigDecimal selfBounceChargedDerived = summaryWrapper.calculateSelfBounceShareAmountTotal(repaymentScheduleInstallments);
        this.selfBounceChargesChargedDerived = selfBounceChargedDerived;
        this.selfBounceChargesRepaidDerived = summaryWrapper.calculateSelfBounceChargesRepaidTotal(repaymentScheduleInstallments);
        this.selfBounceChargesWaivedDerived = summaryWrapper.calculateSelfBounceWaivedChargesTotal(repaymentScheduleInstallments);
        this.selfBounceChargesWrittenDerived = summaryWrapper.calculateSelfBounceWrittenOffChargesTotal(repaymentScheduleInstallments, currency);
        this.selfBounceChargesOutstandingDerived = selfBounceChargedDerived.subtract(Objects.nonNull(this.selfBounceChargesRepaidDerived)?this.selfBounceChargesRepaidDerived:BigDecimal.ZERO)
                .subtract(Objects.nonNull(this.selfBounceChargesWaivedDerived)?this.selfBounceChargesWaivedDerived:BigDecimal.ZERO).subtract(Objects.nonNull(this.selfBounceChargesWrittenDerived)?this.selfBounceChargesWrittenDerived:BigDecimal.ZERO);

        BigDecimal partnerBounceChargedDerived = summaryWrapper.calculatePartnerBounceShareAmountTotal(repaymentScheduleInstallments);
        this.partnerBounceChargesChargedDerived = partnerBounceChargedDerived;
        this.partnerBounceChargesRepaidDerived = summaryWrapper.calculatePartnerBounceChargesRepaidTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesWaivedDerived = summaryWrapper.calculatePartnerBounceWaivedChargesTotal(repaymentScheduleInstallments);
        this.partnerPenaltyChargesWrittenDerived = summaryWrapper.calculatePartnerBounceWrittenOffChargesTotal(repaymentScheduleInstallments);
        this.partnerBounceChargesOutstandingDerived = partnerBounceChargedDerived.subtract(Objects.nonNull(this.partnerBounceChargesRepaidDerived)?this.partnerBounceChargesRepaidDerived:BigDecimal.ZERO)
                .subtract(Objects.nonNull(this.partnerBounceChargesWaivedDerived)?this.partnerBounceChargesWaivedDerived:BigDecimal.ZERO).subtract(Objects.nonNull(this.partnerBounceChargesWrittenDerived)?this.partnerBounceChargesWrittenDerived:BigDecimal.ZERO);


//        final Money totalExpectedRepayment = Money.of(currency, this.totalPrincipalDisbursed).plus(this.totalInterestCharged)
//                .plus(this.totalFeeChargesCharged).plus(this.totalPenaltyChargesCharged);
        final BigDecimal totalExpectedRepayment = this.totalPrincipalDisbursed.add (this.totalInterestCharged)
                .add(this.totalFeeChargesCharged).add(this.totalPenaltyChargesCharged);
        this.totalExpectedRepayment = totalExpectedRepayment;

//        final Money totalRepayment = Money.of(currency, this.totalPrincipalRepaid).plus(this.totalInterestRepaid)
//                .plus(this.totalFeeChargesRepaid).plus(this.totalPenaltyChargesRepaid);
//        this.totalRepayment = totalRepayment.getAmount();

        final BigDecimal totalRepayment =  this.totalPrincipalRepaid.add(this.totalInterestRepaid)
                .add(this.totalFeeChargesRepaid).add(this.totalPenaltyChargesRepaid);
        this.totalRepayment = totalRepayment;

        final BigDecimal totalSelfRepayment = this.totalSelfPrincipalRepaid.add(this.totalSelfInterestRepaid)
                .add(this.totalSelfFeeChargesRepaid).add(this.selfPenaltyChargesRepaidDerived);
        this.totalSelfRepayment = totalSelfRepayment;


        final BigDecimal totalPartnerRepayment = this.totalPartnerPrincipalRepaid.add(this.totalPartnerInterestRepaid)
                .add(this.totalPartnerFeeChargesRepaid).add(this.partnerPenaltyChargesRepaidDerived);
        this.totalPartnerRepayment = totalPartnerRepayment;

        final BigDecimal totalExpectedCostOfLoan =this.totalInterestCharged.add(this.totalFeeChargesCharged)
                .add(this.totalPenaltyChargesCharged);
        this.totalExpectedCostOfLoan = totalExpectedCostOfLoan;

        final BigDecimal totalCostOfLoan =  this.totalInterestRepaid.add(this.totalFeeChargesRepaid)
                .add(this.totalPenaltyChargesRepaid);
        this.totalCostOfLoan = totalCostOfLoan;

        final BigDecimal totalWaived = this.totalInterestWaived.add(this.totalFeeChargesWaived)
                .add(this.totalPenaltyChargesWaived);
        this.totalWaived = totalWaived;

        final BigDecimal totalWrittenOff = this.totalPrincipalWrittenOff.add(this.totalInterestWrittenOff)
                .add(this.totalFeeChargesWrittenOff).add(this.totalPenaltyChargesWrittenOff);
        this.totalWrittenOff = totalWrittenOff;

        final BigDecimal totalOutstanding = this.totalPrincipalOutstanding.add(this.totalInterestOutstanding)
                .add(this.totalFeeChargesOutstanding).add(this.totalPenaltyChargesOutstanding);
        this.totalOutstanding = totalOutstanding;

        final BigDecimal totalSelfOutstanding =  this.totalSelfPrincipalOutstanding.add(this.totalSelfInterestOutstanding)
                .add(this.totalSelfFeeChargesOutstanding).add(this.selfPenaltyChargesOutstandingDerived);
        this.totalSelfOutstanding = totalSelfOutstanding;

        final BigDecimal totalPartnerOutstanding = this.totalPartnerPrincipalOutstanding.add(this.totalPartnerInterestOutstanding)
                .add(this.totalPartnerFeeChargesOutstanding).add(this.partnerPenaltyChargesOutstandingDerived);
        this.totalPartnerOutstanding = totalPartnerOutstanding;
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

    private void resetLoanArrearAgeing() {
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

      //  BeanUtils.copyProperties(loan.getLoanSummary(),this);
       /* this.interestOverdueDerived =  loan.getSummary().getInterestOverdueDerived();
        this.totalPrincipalDisbursed = loan.getSummary().getTotalPrincipalDisbursed();
        this.totalPrincipalRepaid = loan.getSummary().getTotalPrincipalRepaid();
        this.totalSelfPrincipalRepaid= loan.getSummary().getTotalSelfPrincipalRepaid();
        this.totalPartnerPrincipalRepaid = loan.getSummary().getTotalPartnerPrincipalRepaid();
        this.totalPrincipalWrittenOff= loan.getSummary().getTotalPrincipalWrittenOff();
        this.totalPrincipalWrittenOff = loan.getSummary().getTotalSelfPrincipalWrittenOff();
        this. totalPartnerPrincipalWrittenOff = loan.getSummary().getTotalPartnerPrincipalWrittenOff();
        this.totalPrincipalOutstanding = loan.getSummary().getTotalPrincipalOutstanding();
        this.totalSelfPrincipalOutstanding = loan.getSummary().getTotalSelfPrincipalOutstanding();
        this.totalPartnerPrincipalOutstanding =  loan.getSummary().getTotalPartnerPrincipalOutstanding());
        (loan.getSummary().getTotalInterestCharged());
        (loan.getSummary().getTotalSelfInterestCharged());
        (loan.getSummary().getTotalPartnerInterestCharged());
        (loan.getSummary().getTotalInterestRepaid());
        (loan.getSummary().getTotalSelfInterestRepaid());
        (loan.getSummary().getTotalPartnerInterestRepaid());
        (loan.getSummary().getTotalInterestWaived());
        (loan.getSummary().getTotalPartnerInterestWaived());
        (loan.getSummary().getTotalSelfInterestWaived());
        (loan.getSummary().getTotalInterestWrittenOff());
        (loan.getSummary().getTotalInterestOutstanding());
        (loan.getSummary().getTotalSelfInterestOutstanding());
       (loan.getSummary().getTotalPartnerInterestOutstanding());
        (loan.getSummary().getTotalFeeChargesCharged());
       (loan.getSummary().getTotalSelfFeeChargesCharged());
        (loan.getSummary().getTotalPartnerFeeChargesCharged());
        (loan.getSummary().getTotalFeeChargesDueAtDisbursement());
        (loan.getSummary().getTotalSelfFeeChargesDueAtDisbursement());
        (loan.getSummary().getTotalPartnerFeeChargesDueAtDisbursement());
        (loan.getSummary().getTotalFeeChargesRepaid());
       (loan.getSummary().getTotalSelfFeeChargesRepaid());
        (loan.getSummary().getTotalPartnerFeeChargesRepaid());
       (loan.getSummary().getTotalFeeChargesWaived());
      (loan.getSummary().getTotalSelfFeeChargesWaived());
        (loan.getSummary().getTotalPartnerFeeChargesWaived());
       (loan.getSummary().getTotalFeeChargesWrittenOff());
       (loan.getSummary().getTotalSelfFeeChargesWrittenOff());
      (loan.getSummary().getTotalPartnerFeeChargesWrittenOff());
       loan.getSummary().getTotalFeeChargesOutstanding());
       (loan.getSummary().getTotalSelfFeeChargesOutstanding());
      (loan.getSummary().getTotalPartnerFeeChargesOutstanding());
        (loan.getSummary().getTotalPenaltyChargesCharged());
        (loan.getSummary().getTotalPenaltyChargesRepaid());
        (loan.getSummary().getTotalPenaltyChargesWaived());
        (loan.getSummary().getTotalPenaltyChargesWrittenOff());
        (loan.getSummary().getTotalPenaltyChargesOutstanding());
        (loan.getSummary().getTotalExpectedRepayment());
        (loan.getSummary().getTotalRepayment());
        (loan.getSummary().getTotalSelfRepayment());
        (loan.getSummary().getTotalPartnerRepayment());
        loan.getSummary().getTotalExpectedCostOfLoan());
        (loan.getSummary().getTotalCostOfLoan());
        (loan.getSummary().getTotalWaived());
        loanHistorySummary.setTotalWrittenOff(loan.getSummary().getInterestOverdueDerived());
        loanHistorySummary.setTotalOutstanding(loan.getSummary().getTotalOutstanding());
        loanHistorySummary.setTotalSelfOutstanding(loan.getSummary().getTotalSelfOutstanding());
        loanHistorySummary.setTotalPartnerOutstanding(loan.getSummary().getTotalPartnerOutstanding());
        loanHistorySummary.setSelfPenaltyChargesChargedDerived(loan.getSummary().getSelfPenaltyChargesChargedDerived());
        loanHistorySummary.setSelfPenaltyChargesOutstandingDerived(loan.getSummary().getSelfPenaltyChargesOutstandingDerived());
        loanHistorySummary.setSelfPenaltyChargesRepaidDerived(loan.getSummary().getSelfPenaltyChargesRepaidDerived());
        loanHistorySummary.setSelfPenaltyChargesWaivedDerived(loan.getSummary().getSelfPenaltyChargesWaivedDerived());
        loanHistorySummary.setSelfPenaltyChargesWrittenDerived(loan.getSummary().getSelfPenaltyChargesWrittenDerived());
        loanHistorySummary.setPartnerPenaltyChargesChargedDerived(loan.getSummary().getPartnerPenaltyChargesChargedDerived());
        loanHistorySummary.setPartnerPenaltyChargesOutstandingDerived(loan.getSummary().getPartnerPenaltyChargesOutstandingDerived());
        loanHistorySummary.setPartnerPenaltyChargesRepaidDerived(loan.getSummary().getPartnerPenaltyChargesRepaidDerived());
        loanHistorySummary.setPartnerPenaltyChargesWaivedDerived(loan.getSummary().getPartnerPenaltyChargesWaivedDerived());
        loanHistorySummary.setPartnerPenaltyChargesWrittenDerived(loan.getSummary().getPartnerPenaltyChargesWrittenDerived());
        loanHistorySummary.setTotalGstDerived(loan.getSummary().getTotalGstDerived());
        loanHistorySummary.setTotalGstPaid(loan.getSummary().getTotalGstPaid());
        loanHistorySummary.setTotalGstOutstanding(loan.getSummary().getTotalGstOutstanding());
        loanHistorySummary.setTotalSelfGstDerived(loan.getSummary().getTotalSelfGstDerived());
        loanHistorySummary.setTotalSelfGstPaid(loan.getSummary().getTotalSelfGstPaid());
        loanHistorySummary.setTotalSelfGstOutstanding(loan.getSummary().getTotalSelfGstOutstanding());
        loanHistorySummary.setTotalPartnerGstDerived(loan.getSummary().getTotalPartnerGstDerived());
        loanHistorySummary.setTotalPartnerGstPaid(loan.getSummary().getTotalPartnerGstPaid());
        loanHistorySummary.setTotalPartnerGstOutstanding(loan.getSummary().getTotalPartnerGstOutstanding());
        loanHistorySummary.setTotalGstDueAtDisbursement(loan.getSummary().getTotalGstDueAtDisbursement());
        loanHistorySummary.setTotalSelfGstDueAtDisbursement(loan.getSummary().getTotalSelfGstDueAtDisbursement());
        loanHistorySummary.setTotalPartnerGstDueAtDisbursement(loan.getSummary().getTotalPartnerGstDueAtDisbursement());
        loanHistorySummary.setPrincipalOverdueDerived(loan.getSummary().getPrincipalOverdueDerived());
        loanHistorySummary.setSelfPrincipalOverdueDerived(loan.getSummary().getSelfPrincipalOverdueDerived());
        loanHistorySummary.setPartnerPrincipalOverdueDerived(loan.getSummary().getPartnerPrincipalOverdueDerived());
        loanHistorySummary.setInterestOverdueDerived(loan.getSummary().getInterestOverdueDerived());
        loanHistorySummary.setSelfInterestOverdueDerived(loan.getSummary().getSelfInterestOverdueDerived());
        loanHistorySummary.setPartnerInterestOverdueDerived(loan.getSummary().getPartnerInterestOverdueDerived());
        loanHistorySummary.setTotalOverDueDerived(loan.getSummary().getTotalOverDueDerived());
        loanHistorySummary.setSelfTotalOverDueDerived(loan.getSummary().getSelfTotalOverDueDerived());
        loanHistorySummary.setPartnerTotalOverDueDerived(loan.getSummary().getPartnerTotalOverDueDerived());
        loanHistorySummary.setOverdueDinceDateDerived(loan.getSummary().getOverdueDinceDateDerived());*/




        /*loanHistorySummary.setInterestOverdueDerived(loan.getSummary().getInterestOverdueDerived());
        loanHistorySummary.setTotalPrincipalDisbursed(loan.getSummary().getTotalPrincipalDisbursed());
        loanHistorySummary.setTotalPrincipalRepaid(loan.getSummary().getTotalPrincipalRepaid());
        loanHistorySummary.setTotalSelfPrincipalRepaid(loan.getSummary().getTotalSelfPrincipalRepaid());
        loanHistorySummary.setTotalPartnerPrincipalRepaid(loan.getSummary().getTotalPartnerPrincipalRepaid());
        loanHistorySummary.setTotalPrincipalWrittenOff(loan.getSummary().getTotalPrincipalWrittenOff());
        loanHistorySummary.setTotalSelfPrincipalWrittenOff(loan.getSummary().getTotalSelfPrincipalWrittenOff());
        loanHistorySummary.setTotalPartnerPrincipalWrittenOff(loan.getSummary().getTotalPartnerPrincipalWrittenOff());
        loanHistorySummary.setTotalPrincipalOutstanding(loan.getSummary().getTotalPrincipalOutstanding());
        loanHistorySummary.setTotalSelfPrincipalOutstanding(loan.getSummary().getTotalSelfPrincipalOutstanding());
        loanHistorySummary.setTotalPartnerPrincipalOutstanding(loan.getSummary().getTotalPartnerPrincipalOutstanding());
        loanHistorySummary.setTotalInterestCharged(loan.getSummary().getTotalInterestCharged());
        loanHistorySummary.setTotalSelfInterestCharged(loan.getSummary().getTotalSelfInterestCharged());
        loanHistorySummary.setTotalPartnerInterestCharged(loan.getSummary().getTotalPartnerInterestCharged());
        loanHistorySummary.setTotalInterestRepaid(loan.getSummary().getTotalInterestRepaid());
        loanHistorySummary.setTotalSelfInterestRepaid(loan.getSummary().getTotalSelfInterestRepaid());
        loanHistorySummary.setTotalPartnerInterestRepaid(loan.getSummary().getTotalPartnerInterestRepaid());
        loanHistorySummary.setTotalInterestWaived(loan.getSummary().getTotalInterestWaived());
        loanHistorySummary.setTotalPartnerInterestWaived(loan.getSummary().getTotalPartnerInterestWaived());
        loanHistorySummary.setTotalSelfInterestWaived(loan.getSummary().getTotalSelfInterestWaived());
        loanHistorySummary.setTotalInterestWrittenOff(loan.getSummary().getTotalInterestWrittenOff());
        loanHistorySummary.setTotalInterestOutstanding(loan.getSummary().getTotalInterestOutstanding());
        loanHistorySummary.setTotalSelfInterestOutstanding(loan.getSummary().getTotalSelfInterestOutstanding());
        loanHistorySummary.setTotalPartnerInterestOutstanding(loan.getSummary().getTotalPartnerInterestOutstanding());
        loanHistorySummary.setTotalFeeChargesCharged(loan.getSummary().getTotalFeeChargesCharged());
        loanHistorySummary.setTotalSelfFeeChargesCharged(loan.getSummary().getTotalSelfFeeChargesCharged());
        loanHistorySummary.setTotalPartnerFeeChargesCharged(loan.getSummary().getTotalPartnerFeeChargesCharged());
        loanHistorySummary.setTotalFeeChargesDueAtDisbursement(loan.getSummary().getTotalFeeChargesDueAtDisbursement());
        loanHistorySummary.setTotalSelfFeeChargesDueAtDisbursement(loan.getSummary().getTotalSelfFeeChargesDueAtDisbursement());
        loanHistorySummary.setTotalPartnerFeeChargesDueAtDisbursement(loan.getSummary().getTotalPartnerFeeChargesDueAtDisbursement());
        loanHistorySummary.setTotalFeeChargesRepaid(loan.getSummary().getTotalFeeChargesRepaid());
        loanHistorySummary.setTotalSelfFeeChargesRepaid(loan.getSummary().getTotalSelfFeeChargesRepaid());
        loanHistorySummary.setTotalPartnerFeeChargesRepaid(loan.getSummary().getTotalPartnerFeeChargesRepaid());
        loanHistorySummary.setTotalFeeChargesWaived(loan.getSummary().getTotalFeeChargesWaived());
        loanHistorySummary.setTotalSelfFeeChargesWaived(loan.getSummary().getTotalSelfFeeChargesWaived());
        loanHistorySummary.setTotalPartnerFeeChargesWaived(loan.getSummary().getTotalPartnerFeeChargesWaived());
        loanHistorySummary.setTotalFeeChargesWrittenOff(loan.getSummary().getTotalFeeChargesWrittenOff());
        loanHistorySummary.setTotalSelfFeeChargesWrittenOff(loan.getSummary().getTotalSelfFeeChargesWrittenOff());
        loanHistorySummary.setTotalPartnerFeeChargesWrittenOff(loan.getSummary().getTotalPartnerFeeChargesWrittenOff());
        loanHistorySummary.setTotalFeeChargesOutstanding(loan.getSummary().getTotalFeeChargesOutstanding());
        loanHistorySummary.setTotalSelfFeeChargesOutstanding(loan.getSummary().getTotalSelfFeeChargesOutstanding());
        loanHistorySummary.setTotalPartnerFeeChargesOutstanding(loan.getSummary().getTotalPartnerFeeChargesOutstanding());
        loanHistorySummary.setTotalPenaltyChargesCharged(loan.getSummary().getTotalPenaltyChargesCharged());
        loanHistorySummary.setTotalPenaltyChargesRepaid(loan.getSummary().getTotalPenaltyChargesRepaid());
        loanHistorySummary.setTotalPenaltyChargesWaived(loan.getSummary().getTotalPenaltyChargesWaived());
        loanHistorySummary.setTotalPenaltyChargesWrittenOff(loan.getSummary().getTotalPenaltyChargesWrittenOff());
        loanHistorySummary.setTotalPenaltyChargesOutstanding(loan.getSummary().getTotalPenaltyChargesOutstanding());
        loanHistorySummary.setTotalExpectedRepayment(loan.getSummary().getTotalExpectedRepayment());
        loanHistorySummary.setTotalRepayment(loan.getSummary().getTotalRepayment());
        loanHistorySummary.setTotalSelfRepayment(loan.getSummary().getTotalSelfRepayment());
        loanHistorySummary.setTotalPartnerRepayment(loan.getSummary().getTotalPartnerRepayment());
        loanHistorySummary.setTotalExpectedCostOfLoan(loan.getSummary().getTotalExpectedCostOfLoan());
        loanHistorySummary.setTotalCostOfLoan(loan.getSummary().getTotalCostOfLoan());
        loanHistorySummary.setTotalWaived(loan.getSummary().getTotalWaived());
        loanHistorySummary.setTotalWrittenOff(loan.getSummary().getInterestOverdueDerived());
        loanHistorySummary.setTotalOutstanding(loan.getSummary().getTotalOutstanding());
        loanHistorySummary.setTotalSelfOutstanding(loan.getSummary().getTotalSelfOutstanding());
        loanHistorySummary.setTotalPartnerOutstanding(loan.getSummary().getTotalPartnerOutstanding());
        loanHistorySummary.setSelfPenaltyChargesChargedDerived(loan.getSummary().getSelfPenaltyChargesChargedDerived());
        loanHistorySummary.setSelfPenaltyChargesOutstandingDerived(loan.getSummary().getSelfPenaltyChargesOutstandingDerived());
        loanHistorySummary.setSelfPenaltyChargesRepaidDerived(loan.getSummary().getSelfPenaltyChargesRepaidDerived());
        loanHistorySummary.setSelfPenaltyChargesWaivedDerived(loan.getSummary().getSelfPenaltyChargesWaivedDerived());
        loanHistorySummary.setSelfPenaltyChargesWrittenDerived(loan.getSummary().getSelfPenaltyChargesWrittenDerived());
        loanHistorySummary.setPartnerPenaltyChargesChargedDerived(loan.getSummary().getPartnerPenaltyChargesChargedDerived());
        loanHistorySummary.setPartnerPenaltyChargesOutstandingDerived(loan.getSummary().getPartnerPenaltyChargesOutstandingDerived());
        loanHistorySummary.setPartnerPenaltyChargesRepaidDerived(loan.getSummary().getPartnerPenaltyChargesRepaidDerived());
        loanHistorySummary.setPartnerPenaltyChargesWaivedDerived(loan.getSummary().getPartnerPenaltyChargesWaivedDerived());
        loanHistorySummary.setPartnerPenaltyChargesWrittenDerived(loan.getSummary().getPartnerPenaltyChargesWrittenDerived());
        loanHistorySummary.setTotalGstDerived(loan.getSummary().getTotalGstDerived());
        loanHistorySummary.setTotalGstPaid(loan.getSummary().getTotalGstPaid());
        loanHistorySummary.setTotalGstOutstanding(loan.getSummary().getTotalGstOutstanding());
        loanHistorySummary.setTotalSelfGstDerived(loan.getSummary().getTotalSelfGstDerived());
        loanHistorySummary.setTotalSelfGstPaid(loan.getSummary().getTotalSelfGstPaid());
        loanHistorySummary.setTotalSelfGstOutstanding(loan.getSummary().getTotalSelfGstOutstanding());
        loanHistorySummary.setTotalPartnerGstDerived(loan.getSummary().getTotalPartnerGstDerived());
        loanHistorySummary.setTotalPartnerGstPaid(loan.getSummary().getTotalPartnerGstPaid());
        loanHistorySummary.setTotalPartnerGstOutstanding(loan.getSummary().getTotalPartnerGstOutstanding());
        loanHistorySummary.setTotalGstDueAtDisbursement(loan.getSummary().getTotalGstDueAtDisbursement());
        loanHistorySummary.setTotalSelfGstDueAtDisbursement(loan.getSummary().getTotalSelfGstDueAtDisbursement());
        loanHistorySummary.setTotalPartnerGstDueAtDisbursement(loan.getSummary().getTotalPartnerGstDueAtDisbursement());
        loanHistorySummary.setPrincipalOverdueDerived(loan.getSummary().getPrincipalOverdueDerived());
        loanHistorySummary.setSelfPrincipalOverdueDerived(loan.getSummary().getSelfPrincipalOverdueDerived());
        loanHistorySummary.setPartnerPrincipalOverdueDerived(loan.getSummary().getPartnerPrincipalOverdueDerived());
        loanHistorySummary.setInterestOverdueDerived(loan.getSummary().getInterestOverdueDerived());
        loanHistorySummary.setSelfInterestOverdueDerived(loan.getSummary().getSelfInterestOverdueDerived());
        loanHistorySummary.setPartnerInterestOverdueDerived(loan.getSummary().getPartnerInterestOverdueDerived());
        loanHistorySummary.setTotalOverDueDerived(loan.getSummary().getTotalOverDueDerived());
        loanHistorySummary.setSelfTotalOverDueDerived(loan.getSummary().getSelfTotalOverDueDerived());
        loanHistorySummary.setPartnerTotalOverDueDerived(loan.getSummary().getPartnerTotalOverDueDerived());
        loanHistorySummary.setOverdueDinceDateDerived(loan.getSummary().getOverdueDinceDateDerived());*/

//        return loanHistorySummary;


    public LoanHistorySummary(Loan loan) {

        this.totalPrincipalDisbursed = loan.getSummary().getTotalPrincipalDisbursed();
        this.totalPrincipalRepaid = loan.getSummary().getTotalPrincipalRepaid();
        this.totalSelfPrincipalRepaid = loan.getSummary().getTotalSelfPrincipalRepaid();
        this.totalPartnerPrincipalRepaid = loan.getSummary().getTotalPartnerPrincipalRepaid();
        this.totalPrincipalWrittenOff = loan.getSummary().getTotalPrincipalWrittenOff();
        this.totalSelfPrincipalWrittenOff = loan.getSummary().getTotalSelfPrincipalWrittenOff();
        this.totalPartnerPrincipalWrittenOff = loan.getSummary().getTotalPartnerPrincipalWrittenOff();
        this.totalPrincipalOutstanding = loan.getSummary().getTotalPrincipalOutstanding();
        this.totalSelfPrincipalOutstanding = loan.getSummary().getTotalSelfPrincipalOutstanding();
        this.totalPartnerPrincipalOutstanding = loan.getSummary().getTotalPartnerPrincipalOutstanding();
        this.totalInterestCharged =loan.getSummary().getTotalInterestCharged();
        this.totalSelfInterestCharged = loan.getSummary().getTotalSelfInterestCharged();
        this.totalPartnerInterestCharged = loan.getSummary().getTotalPartnerInterestCharged();
        this.totalInterestRepaid = loan.getSummary().getTotalInterestRepaid();
        this.totalSelfInterestRepaid = loan.getSummary().getTotalSelfInterestRepaid();
        this.totalPartnerInterestRepaid = loan.getSummary().getTotalPartnerInterestRepaid();
        this.totalInterestWaived = loan.getSummary().getTotalInterestWaived();
        this.totalPartnerInterestWaived = loan.getSummary().getTotalPartnerInterestWaived();
        this.totalSelfInterestWaived = loan.getSummary().getTotalSelfInterestWaived();
        this.totalInterestWrittenOff = loan.getSummary().getTotalInterestWrittenOff();
        this.totalInterestOutstanding = loan.getSummary().getTotalInterestOutstanding();
        this.totalSelfInterestOutstanding = loan.getSummary().getTotalSelfInterestOutstanding();
        this.totalPartnerInterestOutstanding = loan.getSummary().getTotalPartnerInterestOutstanding();
        this.totalFeeChargesCharged = loan.getSummary().getTotalFeeChargesCharged();
        this.totalSelfFeeChargesCharged = loan.getSummary().getTotalSelfFeeChargesCharged();
        this.totalPartnerFeeChargesCharged = loan.getSummary().getTotalPartnerFeeChargesCharged();
        this.totalFeeChargesDueAtDisbursement = loan.getSummary().getTotalFeeChargesDueAtDisbursement();
        this.totalSelfFeeChargesDueAtDisbursement = loan.getSummary().getTotalSelfFeeChargesDueAtDisbursement();
        this.totalPartnerFeeChargesDueAtDisbursement = loan.getSummary().getTotalPartnerFeeChargesDueAtDisbursement();
        this.totalFeeChargesRepaid = loan.getSummary().getTotalFeeChargesRepaid();
        this.totalSelfFeeChargesRepaid = loan.getSummary().getTotalSelfFeeChargesRepaid();
        this.totalPartnerFeeChargesRepaid = loan.getSummary().getTotalPartnerFeeChargesRepaid();
        this.totalFeeChargesWaived = loan.getSummary().getTotalFeeChargesWaived();
        this.totalSelfFeeChargesWaived = loan.getSummary().getTotalSelfFeeChargesWaived();
        this.totalPartnerFeeChargesWaived = loan.getSummary().getTotalPartnerFeeChargesWaived();
        this.totalFeeChargesWrittenOff = loan.getSummary().getTotalFeeChargesWrittenOff();
        this.totalSelfFeeChargesWrittenOff = loan.getSummary().getTotalSelfFeeChargesWrittenOff();
        this.totalPartnerFeeChargesWrittenOff = loan.getSummary().getTotalPartnerFeeChargesWrittenOff();
        this.totalFeeChargesOutstanding = loan.getSummary().getTotalFeeChargesOutstanding();
        this.totalSelfFeeChargesOutstanding = loan.getSummary().getTotalSelfFeeChargesOutstanding();
        this.totalPartnerFeeChargesOutstanding = loan.getSummary().getTotalPartnerFeeChargesOutstanding();
        this.totalPenaltyChargesCharged = loan.getSummary().getTotalPenaltyChargesCharged();
        this.totalPenaltyChargesRepaid = loan.getSummary().getTotalPenaltyChargesRepaid();
        this.totalPenaltyChargesWaived = loan.getSummary().getTotalPenaltyChargesWaived();
        this.totalPenaltyChargesWrittenOff = loan.getSummary().getTotalPenaltyChargesWrittenOff();
        this.totalPenaltyChargesOutstanding = loan.getSummary().getTotalPenaltyChargesOutstanding();

        this.totalBounceChargesCharged = loan.getSummary().getTotalBounceChargesCharged();
        this.totalBounceChargesRepaid = loan.getSummary().getTotalBounceChargesRepaid();
        this.totalBounceChargesWaived = loan.getSummary().getTotalBounceChargesWaived();
        this.totalBounceChargesWrittenOff = loan.getSummary().getTotalBounceChargesWrittenOff();
        this.totalBounceChargesOutstanding = loan.getSummary().getTotalBounceChargesOutstanding();

        this.totalExpectedRepayment = loan.getSummary().getTotalExpectedRepayment();
        this.totalRepayment = loan.getSummary().getTotalRepayment();
        this.totalSelfRepayment = loan.getSummary().getTotalSelfRepayment();
        this.totalPartnerRepayment = loan.getSummary().getTotalPartnerRepayment();
        this.totalExpectedCostOfLoan = loan.getSummary().getTotalExpectedCostOfLoan();
        this.totalCostOfLoan = loan.getSummary().getTotalCostOfLoan();
        this.totalWaived = loan.getSummary().getTotalWaived();
        this.totalWrittenOff = loan.getSummary().getTotalWrittenOff();
        this.totalOutstanding = loan.getSummary().getTotalOutstanding();
        this.totalSelfOutstanding = loan.getSummary().getTotalSelfOutstanding();
        this.totalPartnerOutstanding = loan.getSummary().getTotalPartnerOutstanding();
        this.selfPenaltyChargesChargedDerived = loan.getSummary().getSelfPenaltyChargesChargedDerived();
        this.selfPenaltyChargesOutstandingDerived = loan.getSummary().getSelfPenaltyChargesOutstandingDerived();
        this.selfPenaltyChargesRepaidDerived = loan.getSummary().getSelfPenaltyChargesRepaidDerived();
        this.selfPenaltyChargesWaivedDerived = loan.getSummary().getSelfPenaltyChargesWaivedDerived();
        this.selfPenaltyChargesWrittenDerived = loan.getSummary().getSelfPenaltyChargesWrittenDerived();
        this.partnerPenaltyChargesChargedDerived = loan.getSummary().getPartnerPenaltyChargesChargedDerived();
        this.partnerPenaltyChargesOutstandingDerived = loan.getSummary().getPartnerPenaltyChargesOutstandingDerived();
        this.partnerPenaltyChargesRepaidDerived = loan.getSummary().getPartnerPenaltyChargesRepaidDerived();
        this.partnerPenaltyChargesWaivedDerived = loan.getSummary().getPartnerPenaltyChargesWaivedDerived();
        this.partnerPenaltyChargesWrittenDerived = loan.getSummary().getPartnerPenaltyChargesWrittenDerived();

        this.selfBounceChargesChargedDerived = loan.getSummary().getSelfBounceChargesChargedDerived();
        this.selfBounceChargesOutstandingDerived = loan.getSummary().getSelfBounceChargesOutstandingDerived();
        this.selfBounceChargesRepaidDerived = loan.getSummary().getSelfBounceChargesRepaidDerived();
        this.selfBounceChargesWaivedDerived = loan.getSummary().getSelfBounceChargesWaivedDerived();
        this.selfBounceChargesWrittenDerived = loan.getSummary().getSelfBounceChargesWrittenDerived();
        this.partnerBounceChargesChargedDerived = loan.getSummary().getPartnerBounceChargesChargedDerived();
        this.partnerBounceChargesOutstandingDerived = loan.getSummary().getPartnerBounceChargesOutstandingDerived();
        this.partnerBounceChargesRepaidDerived = loan.getSummary().getPartnerBounceChargesRepaidDerived();
        this.partnerBounceChargesWaivedDerived = loan.getSummary().getPartnerBounceChargesWaivedDerived();
        this.partnerBounceChargesWrittenDerived = loan.getSummary().getPartnerBounceChargesWrittenDerived();


        this.totalGstDerived = loan.getSummary().getTotalGstDerived();
        this.totalGstPaid = loan.getSummary().getTotalGstPaid();
        this.totalGstOutstanding = loan.getSummary().getTotalGstOutstanding();
        this.totalSelfGstDerived = loan.getSummary().getTotalSelfGstDerived();
        this.totalSelfGstPaid = loan.getSummary().getTotalSelfGstPaid();
        this.totalSelfGstOutstanding = loan.getSummary().getTotalSelfGstOutstanding();
        this.totalPartnerGstDerived = loan.getSummary().getTotalPartnerGstDerived();
        this.totalPartnerGstPaid = loan.getSummary().getTotalPartnerGstPaid();
        this.totalPartnerGstOutstanding = loan.getSummary().getTotalPartnerGstOutstanding();
        this.totalGstDueAtDisbursement = loan.getSummary().getTotalGstDueAtDisbursement();
        this.totalSelfGstDueAtDisbursement = loan.getSummary().getTotalSelfGstDueAtDisbursement();
        this.totalPartnerGstDueAtDisbursement = loan.getSummary().getTotalPartnerGstDueAtDisbursement();
        this.principalOverdueDerived = loan.getSummary().getPrincipalOverdueDerived();
        this.selfPrincipalOverdueDerived = loan.getSummary().getSelfPrincipalOverdueDerived();
        this.partnerPrincipalOverdueDerived = loan.getSummary().getPartnerPrincipalOverdueDerived();
        this.interestOverdueDerived = loan.getSummary().getInterestOverdueDerived();
        this.selfInterestOverdueDerived = loan.getSummary().getSelfInterestOverdueDerived();
        this.partnerInterestOverdueDerived = loan.getSummary().getPartnerInterestOverdueDerived();
        this.totalOverDueDerived = loan.getSummary().getTotalOverDueDerived();
        this.selfTotalOverDueDerived = loan.getSummary().getSelfTotalOverDueDerived();
        this.partnerTotalOverDueDerived = loan.getSummary().getPartnerTotalOverDueDerived();
        this.overdueDinceDateDerived = loan.getSummary().getOverdueDinceDateDerived();

         /*loanHistorySummary.setInterestOverdueDerived(loan.getSummary().getInterestOverdueDerived());
        loanHistorySummary.setTotalPrincipalDisbursed(loan.getSummary().getTotalPrincipalDisbursed());
        loanHistorySummary.setTotalPrincipalRepaid(loan.getSummary().getTotalPrincipalRepaid());
        loanHistorySummary.setTotalSelfPrincipalRepaid(loan.getSummary().getTotalSelfPrincipalRepaid());
        loanHistorySummary.setTotalPartnerPrincipalRepaid(loan.getSummary().getTotalPartnerPrincipalRepaid());
        loanHistorySummary.setTotalPrincipalWrittenOff(loan.getSummary().getTotalPrincipalWrittenOff());
        loanHistorySummary.setTotalSelfPrincipalWrittenOff(loan.getSummary().getTotalSelfPrincipalWrittenOff());
        loanHistorySummary.setTotalPartnerPrincipalWrittenOff(loan.getSummary().getTotalPartnerPrincipalWrittenOff());
        loanHistorySummary.setTotalPrincipalOutstanding(loan.getSummary().getTotalPrincipalOutstanding());
        loanHistorySummary.setTotalSelfPrincipalOutstanding(loan.getSummary().getTotalSelfPrincipalOutstanding());
        loanHistorySummary.setTotalPartnerPrincipalOutstanding(loan.getSummary().getTotalPartnerPrincipalOutstanding());
        loanHistorySummary.setTotalInterestCharged(loan.getSummary().getTotalInterestCharged());
        loanHistorySummary.setTotalSelfInterestCharged(loan.getSummary().getTotalSelfInterestCharged());
        loanHistorySummary.setTotalPartnerInterestCharged(loan.getSummary().getTotalPartnerInterestCharged());
        loanHistorySummary.setTotalInterestRepaid(loan.getSummary().getTotalInterestRepaid());
        loanHistorySummary.setTotalSelfInterestRepaid(loan.getSummary().getTotalSelfInterestRepaid());
        loanHistorySummary.setTotalPartnerInterestRepaid(loan.getSummary().getTotalPartnerInterestRepaid());
        loanHistorySummary.setTotalInterestWaived(loan.getSummary().getTotalInterestWaived());
        loanHistorySummary.setTotalPartnerInterestWaived(loan.getSummary().getTotalPartnerInterestWaived());
        loanHistorySummary.setTotalSelfInterestWaived(loan.getSummary().getTotalSelfInterestWaived());
        loanHistorySummary.setTotalInterestWrittenOff(loan.getSummary().getTotalInterestWrittenOff());
        loanHistorySummary.setTotalInterestOutstanding(loan.getSummary().getTotalInterestOutstanding());
        loanHistorySummary.setTotalSelfInterestOutstanding(loan.getSummary().getTotalSelfInterestOutstanding());
        loanHistorySummary.setTotalPartnerInterestOutstanding(loan.getSummary().getTotalPartnerInterestOutstanding());
        loanHistorySummary.setTotalFeeChargesCharged(loan.getSummary().getTotalFeeChargesCharged());
        loanHistorySummary.setTotalSelfFeeChargesCharged(loan.getSummary().getTotalSelfFeeChargesCharged());
        loanHistorySummary.setTotalPartnerFeeChargesCharged(loan.getSummary().getTotalPartnerFeeChargesCharged());
        loanHistorySummary.setTotalFeeChargesDueAtDisbursement(loan.getSummary().getTotalFeeChargesDueAtDisbursement());
        loanHistorySummary.setTotalSelfFeeChargesDueAtDisbursement(loan.getSummary().getTotalSelfFeeChargesDueAtDisbursement());
        loanHistorySummary.setTotalPartnerFeeChargesDueAtDisbursement(loan.getSummary().getTotalPartnerFeeChargesDueAtDisbursement());
        loanHistorySummary.setTotalFeeChargesRepaid(loan.getSummary().getTotalFeeChargesRepaid());
        loanHistorySummary.setTotalSelfFeeChargesRepaid(loan.getSummary().getTotalSelfFeeChargesRepaid());
        loanHistorySummary.setTotalPartnerFeeChargesRepaid(loan.getSummary().getTotalPartnerFeeChargesRepaid());
        loanHistorySummary.setTotalFeeChargesWaived(loan.getSummary().getTotalFeeChargesWaived());
        loanHistorySummary.setTotalSelfFeeChargesWaived(loan.getSummary().getTotalSelfFeeChargesWaived());
        loanHistorySummary.setTotalPartnerFeeChargesWaived(loan.getSummary().getTotalPartnerFeeChargesWaived());
        loanHistorySummary.setTotalFeeChargesWrittenOff(loan.getSummary().getTotalFeeChargesWrittenOff());
        loanHistorySummary.setTotalSelfFeeChargesWrittenOff(loan.getSummary().getTotalSelfFeeChargesWrittenOff());
        loanHistorySummary.setTotalPartnerFeeChargesWrittenOff(loan.getSummary().getTotalPartnerFeeChargesWrittenOff());
        loanHistorySummary.setTotalFeeChargesOutstanding(loan.getSummary().getTotalFeeChargesOutstanding());
        loanHistorySummary.setTotalSelfFeeChargesOutstanding(loan.getSummary().getTotalSelfFeeChargesOutstanding());
        loanHistorySummary.setTotalPartnerFeeChargesOutstanding(loan.getSummary().getTotalPartnerFeeChargesOutstanding());
        loanHistorySummary.setTotalPenaltyChargesCharged(loan.getSummary().getTotalPenaltyChargesCharged());
        loanHistorySummary.setTotalPenaltyChargesRepaid(loan.getSummary().getTotalPenaltyChargesRepaid());
        loanHistorySummary.setTotalPenaltyChargesWaived(loan.getSummary().getTotalPenaltyChargesWaived());
        loanHistorySummary.setTotalPenaltyChargesWrittenOff(loan.getSummary().getTotalPenaltyChargesWrittenOff());
        loanHistorySummary.setTotalPenaltyChargesOutstanding(loan.getSummary().getTotalPenaltyChargesOutstanding());
        loanHistorySummary.setTotalExpectedRepayment(loan.getSummary().getTotalExpectedRepayment());
        loanHistorySummary.setTotalRepayment(loan.getSummary().getTotalRepayment());
        loanHistorySummary.setTotalSelfRepayment(loan.getSummary().getTotalSelfRepayment());
        loanHistorySummary.setTotalPartnerRepayment(loan.getSummary().getTotalPartnerRepayment());
        loanHistorySummary.setTotalExpectedCostOfLoan(loan.getSummary().getTotalExpectedCostOfLoan());
        loanHistorySummary.setTotalCostOfLoan(loan.getSummary().getTotalCostOfLoan());
        loanHistorySummary.setTotalWaived(loan.getSummary().getTotalWaived());
        loanHistorySummary.setTotalWrittenOff(loan.getSummary().getInterestOverdueDerived());
        loanHistorySummary.setTotalOutstanding(loan.getSummary().getTotalOutstanding());
        loanHistorySummary.setTotalSelfOutstanding(loan.getSummary().getTotalSelfOutstanding());
        loanHistorySummary.setTotalPartnerOutstanding(loan.getSummary().getTotalPartnerOutstanding());
        loanHistorySummary.setSelfPenaltyChargesChargedDerived(loan.getSummary().getSelfPenaltyChargesChargedDerived());
        loanHistorySummary.setSelfPenaltyChargesOutstandingDerived(loan.getSummary().getSelfPenaltyChargesOutstandingDerived());
        loanHistorySummary.setSelfPenaltyChargesRepaidDerived(loan.getSummary().getSelfPenaltyChargesRepaidDerived());
        loanHistorySummary.setSelfPenaltyChargesWaivedDerived(loan.getSummary().getSelfPenaltyChargesWaivedDerived());
        loanHistorySummary.setSelfPenaltyChargesWrittenDerived(loan.getSummary().getSelfPenaltyChargesWrittenDerived());
        loanHistorySummary.setPartnerPenaltyChargesChargedDerived(loan.getSummary().getPartnerPenaltyChargesChargedDerived());
        loanHistorySummary.setPartnerPenaltyChargesOutstandingDerived(loan.getSummary().getPartnerPenaltyChargesOutstandingDerived());
        loanHistorySummary.setPartnerPenaltyChargesRepaidDerived(loan.getSummary().getPartnerPenaltyChargesRepaidDerived());
        loanHistorySummary.setPartnerPenaltyChargesWaivedDerived(loan.getSummary().getPartnerPenaltyChargesWaivedDerived());
        loanHistorySummary.setPartnerPenaltyChargesWrittenDerived(loan.getSummary().getPartnerPenaltyChargesWrittenDerived());
        loanHistorySummary.setTotalGstDerived(loan.getSummary().getTotalGstDerived());
        loanHistorySummary.setTotalGstPaid(loan.getSummary().getTotalGstPaid());
        loanHistorySummary.setTotalGstOutstanding(loan.getSummary().getTotalGstOutstanding());
        loanHistorySummary.setTotalSelfGstDerived(loan.getSummary().getTotalSelfGstDerived());
        loanHistorySummary.setTotalSelfGstPaid(loan.getSummary().getTotalSelfGstPaid());
        loanHistorySummary.setTotalSelfGstOutstanding(loan.getSummary().getTotalSelfGstOutstanding());
        loanHistorySummary.setTotalPartnerGstDerived(loan.getSummary().getTotalPartnerGstDerived());
        loanHistorySummary.setTotalPartnerGstPaid(loan.getSummary().getTotalPartnerGstPaid());
        loanHistorySummary.setTotalPartnerGstOutstanding(loan.getSummary().getTotalPartnerGstOutstanding());
        loanHistorySummary.setTotalGstDueAtDisbursement(loan.getSummary().getTotalGstDueAtDisbursement());
        loanHistorySummary.setTotalSelfGstDueAtDisbursement(loan.getSummary().getTotalSelfGstDueAtDisbursement());
        loanHistorySummary.setTotalPartnerGstDueAtDisbursement(loan.getSummary().getTotalPartnerGstDueAtDisbursement());
        loanHistorySummary.setPrincipalOverdueDerived(loan.getSummary().getPrincipalOverdueDerived());
        loanHistorySummary.setSelfPrincipalOverdueDerived(loan.getSummary().getSelfPrincipalOverdueDerived());
        loanHistorySummary.setPartnerPrincipalOverdueDerived(loan.getSummary().getPartnerPrincipalOverdueDerived());
        loanHistorySummary.setInterestOverdueDerived(loan.getSummary().getInterestOverdueDerived());
        loanHistorySummary.setSelfInterestOverdueDerived(loan.getSummary().getSelfInterestOverdueDerived());
        loanHistorySummary.setPartnerInterestOverdueDerived(loan.getSummary().getPartnerInterestOverdueDerived());
        loanHistorySummary.setTotalOverDueDerived(loan.getSummary().getTotalOverDueDerived());
        loanHistorySummary.setSelfTotalOverDueDerived(loan.getSummary().getSelfTotalOverDueDerived());
        loanHistorySummary.setPartnerTotalOverDueDerived(loan.getSummary().getPartnerTotalOverDueDerived());
        loanHistorySummary.setOverdueDinceDateDerived(loan.getSummary().getOverdueDinceDateDerived());*/
    }

    public static LoanHistorySummary updateLoanHistorySummaryDetails(Loan loan) {
        return new LoanHistorySummary(loan);
    }
}
