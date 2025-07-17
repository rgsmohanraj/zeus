package org.vcpl.lms.portfolio.loanproduct.domain;

import jakarta.persistence.*;
import liquibase.pro.packaged.B;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "m_product_loan_collection_config")
public class ProductCollectionConfig extends AbstractPersistableCustom {


    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "advance_appropriation_on", nullable = false)
    private Integer advanceAppropriationOn;

    @Column(name = "advance_entry_enabled",nullable = false)
    private boolean advanceEntryEnabled;

    @Column(name = "interest_benefit_enabled",nullable = true)
    private Boolean interestBenefitEnabled;

    @Column(name = "foreclosure_on_due_date_interest",nullable = true)
    private Integer foreclosureOnDueDateInterest;

    @Column(name = "foreclosure_on_due_date_charge", nullable = true)
    private Integer foreclosureOnDueDateCharge;

    @Column(name = "foreclosure_other_than_due_date_interest",nullable = true)
    private Integer foreclosureOtherThanDueDateInterest;

    @Column(name = "foreclosure_other_than_due_date_charge", nullable = true)
    private Integer foreclosureOtherThanDueDateCharge;

    @Column(name = "foreclosure_one_month_overdue_interest",nullable = true)
    private Integer foreclosureOneMonthOverdueInterest;

    @Column(name = "foreclosure_one_month_overdue_charge", nullable = true)
    private Integer foreclosureOneMonthOverdueCharge;

    @Column(name = "foreclosure_short_paid_interest",nullable = true)
    private Integer foreclosureShortPaidInterest;

    @Column(name = "foreclosure_short_paid_interest_charge", nullable = true)
    private Integer foreclosureShortPaidInterestCharge;

    @Column(name = "foreclosure_principal_short_paid_interest",nullable = true)
    private Integer foreclosurePrincipalShortPaidInterest;

    @Column(name = "foreclosure_principal_short_paid_charge", nullable = true)
    private Integer foreclosurePrincipalShortPaidCharge;

    @Column(name = "foreclosure_two_months_overdue_interest",nullable = true)
    private Integer foreclosureTwoMonthsOverdueInterest;

    @Column(name = "foreclosure_two_months_overdue_charge", nullable = true)
    private Integer foreclosureTwoMonthsOverdueCharge;

    @Column(name = "foreclosure_pos_advance_on_due_date",nullable = true)
    private Integer foreclosurePosAdvanceOnDueDate;

    @Column(name = "foreclosure_advance_on_due_date_interest",nullable = true)
    private Integer foreclosureAdvanceOnDueDateInterest;

    @Column(name = "foreclosure_advance_on_due_date_charge", nullable = true)
    private Integer foreclosureAdvanceOnDueDateCharge;

    @Column(name = "foreclosure_pos_advance_other_than_due_date",nullable = true)
    private Integer foreclosurePosAdvanceOtherThanDueDate;

    @Column(name = "foreclosure_advance_after_due_date_interest",nullable = true)
    private Integer foreclosureAdvanceAfterDueDateInterest;

    @Column(name = "foreclosure_advance_after_due_date_charge", nullable = true)
    private Integer foreclosureAdvanceAfterDueDateCharge;

    @Column(name = "foreclosure_backdated_short_paid_interest",nullable = true)
    private Integer foreclosureBackdatedShortPaidInterest;

    @Column(name = "foreclosure_backdated_short_paid_interest_charge", nullable = true)
    private Integer foreclosureBackdatedShortPaidInterestCharge;

    @Column(name = "foreclosure_backdated_fully_paid_interest",nullable = true)
    private Integer foreclosureBackdatedFullyPaidInterest;

    @Column(name = "foreclosure_backdated_fully_paid_interest_charge", nullable = true)
    private Integer foreclosureBackdatedFullyPaidInterestCharge;

    @Column(name = "foreclosure_backdated_short_paid_principal_interest",nullable = true)
    private Integer foreclosureBackdatedShortPaidPrincipalInterest;

    @Column(name = "foreclosure_backdated_short_paid_principal_charge", nullable = true)
    private Integer foreclosureBackdatedShortPaidPrincipalCharge;

    @Column(name = "foreclosure_backdated_fully_paid_emi_interest",nullable = true)
    private Integer foreclosureBackdatedFullyPaidEmiInterest;

    @Column(name = "foreclosure_backdated_fully_paid_emi_charge", nullable = true)
    private Integer foreclosureBackdatedFullyPaidEmiCharge;

    @Column(name = "foreclosure_backdated_advance_interest",nullable = true)
    private Integer foreclosureBackdatedAdvanceInterest;

    @Column(name = "foreclosure_backdated_advance_charge", nullable = true)
    private Integer foreclosureBackdatedAdvanceCharge;
	
	@Column(name = "advance_appropriation_against_on",nullable = true)
	private Integer advanceAppropriationAgainstOn;

    @Column(name = "foreclosure_method_type",nullable = true)
    private Integer foreclosureMethodType;
	@Column(name = "cooling_off_applicability", nullable = true)
    private Boolean coolingOffApplicability;

    @Column(name = "cooling_off_threshold_days", nullable = true)
    private Integer coolingOffThresholdDays;

    @Column(name = "cooling_off_interest_and_charge_applicability", nullable = true)
    private Integer coolingOffInterestAndChargeApplicability;

    @Column(name = "cooling_off_interest_logic_applicability", nullable = true)
    private Integer coolingOffInterestLogicApplicability;

    @Column(name = "cooling_off_days_in_year", nullable = true)
    private Integer coolingOffDaysInYear;

    @Column(name = "cooling_off_rounding_mode", nullable = true)
    private String coolingOffRoundingMode;

    @Column(name = "cooling_off_rounding_decimals", nullable = true)
    private Integer coolingOffRoundingDecimals;

    public  static  ProductCollectionConfig getInstance(){
        return new ProductCollectionConfig();
    }

    public boolean isAdvanceEntryEnabled(){
        return advanceEntryEnabled;
    }

    public Boolean isCoolingOffEnable(){
        return coolingOffApplicability;}

    public RoundingMode getCoolingOffRoundingMode(){
        return RoundingMode.valueOf(this.coolingOffRoundingMode);
    }
	


}
