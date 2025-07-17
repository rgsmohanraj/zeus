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
package org.vcpl.lms.portfolio.loanproduct.domain;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.accounting.common.AccountingRuleType;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.common.domain.DaysInMonthType;
import org.vcpl.lms.portfolio.common.domain.DaysInYearType;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.floatingrates.data.FloatingRateDTO;
import org.vcpl.lms.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.vcpl.lms.portfolio.floatingrates.domain.FloatingRate;
import org.vcpl.lms.portfolio.fund.domain.Fund;
import org.vcpl.lms.portfolio.loanaccount.exception.BackDatedDisbursementNotAllowedException;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.domain.ServicerFeeConfig;
import org.vcpl.lms.portfolio.loanproduct.LoanProductConstants;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductFeeData;
import org.vcpl.lms.portfolio.loanproduct.exception.LoanProductGeneralRuleException;

import org.vcpl.lms.portfolio.partner.domain.Partner;
import org.vcpl.lms.portfolio.rate.domain.Rate;
import org.vcpl.lms.useradministration.domain.AppUser;

/**
 * Loan products allow for categorisation of an organisations loans into something meaningful to them.
 *
 * They provide a means of simplifying creation/maintenance of loans. They can also allow for product comparison to take
 * place when reporting.
 *
 * They allow for constraints to be added at product level.
 */
@Entity
@Setter
@Getter
@Table(name = "m_product_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unq_name"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "external_id_UNIQUE"),
        @UniqueConstraint(columnNames = { "short_name" }, name = "unq_short_name") })
public class LoanProduct extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "fund_id", nullable = true)
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "loan_transaction_strategy_id", nullable = true)
    private LoanTransactionProcessingStrategy transactionProcessingStrategy;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "short_name", nullable = false, unique = true)
    private String shortName;

    @Column(name = "loan_acc_no_preference", nullable = true)
    private String loanAccNoPreference;

    @Column(name = "description")
    private String description;

    @Column(name = "broken_interest_calculation_period")
    private String brokenInterestCalculationPeriod;
//
//    @Column(name = "repayment_strategy_for_npa")
//    private boolean repaymentStrategyForNpa;

    @Column(name = "repayment_strategy_for_npa_id")
    private String repaymentStrategyForNpaId;

    @Column(name = "loan_foreclosure_strategy")
    private String loanForeclosureStrategy;
//
//    @Column(name = "broken_interest_days_in_years")
//    private String brokenInterestDaysInYears;
//
//    @Column(name = "broken_interest_days_in_month")
//    private String brokenInterestDaysInMonth;

//    @Column(name = "broken_interest_strategy")
//    private String brokenInterestStrategy;

//    @Column(name = "coBorrower")
//    private boolean coBorrower;
//
//    @Column(name = "eod_balance")
//    private boolean eodBalance;
//
//    @Column(name = "secured_loan")
//    private boolean securedLoan;
//
//    @Column(name = "non_equated_installment")
//    private boolean nonEquatedInstallment;
//
//
//    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
//    private LoanProductAgeLimitsConfig ageLimitsConfig;
//
//    @Column(name = "allow_age_limits")
//    private boolean allowAgeLimits;

//    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
//    private LoanProductPrepaidLockConfig prepaidLoanConfig;
//
//    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
//    private LoanProductForeclosureLockConfig foreclosureLoanConfig;

//    @Column(name = "allow_prepaid_locking_period", nullable = false)
//    private boolean allowPrepaidLockingPeriod;
//
//    @Column(name = "allow_foreclosure_locking_period", nullable = false)
//    private boolean allowForeclosureLockingPeriod;

    @Column(name = "enable_colending_loan")
    private boolean enableColendingLoan;

    @Column(name = "select_accepted_dates")
    private boolean selectAcceptedDates;
//
//    @Column(name = "advance_emi")
//    private boolean advanceEMI;

//    @Column(name = "term_based_on_loancycle")
//    private boolean termBasedOnLoanCycle;
//
//    @Column(name = "is_net_off_applied")
//    private boolean isNetOffApplied;

    @Column(name = "allow_approval_over_amount_applied")
    private boolean allowApprovalOverAmountApplied;

    @Column(name = "use_days_in_month_for_loan_provisioning")
    private boolean useDaysInMonthForLoanProvisioning;

    @Column(name = "divide_by_thirty_for_partial_period")
    private boolean divideByThirtyForPartialPeriod;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_product_loan_charge", joinColumns = @JoinColumn(name = "product_loan_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private List<Charge> charges;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "m_product_loan_rate", joinColumns = @JoinColumn(name = "product_loan_id"), inverseJoinColumns = @JoinColumn(name = "rate_id"))
    private List<Rate> rates;

    @Embedded
    private LoanProductRelatedDetail loanProductRelatedDetail;

    @Embedded
    private LoanProductMinMaxConstraints loanProductMinMaxConstraints;

    @Column(name = "accounting_type")
    private Integer accountingRule;

    @Column(name = "include_in_borrower_cycle")
    private boolean includeInBorrowerCycle;

    @Column(name = "use_borrower_cycle")
    private boolean useBorrowerCycle;

    @Embedded
    private LoanProductTrancheDetails loanProducTrancheDetails;

    @Column(name = "start_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "close_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date closeDate;

    @Column(name = "external_id", length = 100, nullable = true, unique = true)
    private String externalId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductBorrowerCycleVariations> borrowerCycleVariations = new HashSet<>();

    @Column(name = "overdue_days_for_npa", nullable = true)
    private Integer overdueDaysForNPA;

    @Column(name = "min_days_between_disbursal_and_first_repayment", nullable = true)
    private Integer minimumDaysBetweenDisbursalAndFirstRepayment;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanProductInterestRecalculationDetails productInterestRecalculationDetails;

    @Column(name = "hold_guarantee_funds")
    private boolean holdGuaranteeFunds;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanProductGuaranteeDetails loanProductGuaranteeDetails;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true)
    private LoanProductConfigurableAttributes loanConfigurableAttributes;

    @Column(name = "principal_threshold_for_last_installment", scale = 2, precision = 5, nullable = false)
    private BigDecimal principalThresholdForLastInstallment;

    @Column(name = "account_moves_out_of_npa_only_on_arrears_completion")
    private boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;

    @Column(name = "can_define_fixed_emi_amount")
    private boolean canDefineInstallmentAmount;

    @Column(name = "instalment_amount_in_multiples_of")
    private Integer installmentAmountInMultiplesOf;

    @Column(name = "is_linked_to_floating_interest_rates")
    private boolean isLinkedToFloatingInterestRate;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanProductFloatingRates floatingRates;

    @Column(name = "allow_variabe_installments")
    private boolean allowVariabeInstallments;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanProductVariableInstallmentConfig variableInstallmentConfig;

    @Column(name = "sync_expected_with_disbursement_date")
    private boolean syncExpectedWithDisbursementDate;

    @Column(name = "can_use_for_topup")
    private boolean canUseForTopup = false;

    @Column(name = "fixed_principal_percentage_per_installment", scale = 2, precision = 5, nullable = true)
    private BigDecimal fixedPrincipalPercentagePerInstallment;

    @Column(name = "disallow_expected_disbursements")
    private boolean disallowExpectedDisbursements;

    @Column(name = "allow_approved_disbursed_amounts_over_applied")
    private boolean allowApprovedDisbursedAmountsOverApplied;

    @Column(name = "over_applied_calculation_type", nullable = true)
    private String overAppliedCalculationType;

    @Column(name = "over_applied_number", nullable = true)
    private Integer overAppliedNumber;

    @Column(name = "apply_prepaid_locking_period")
    private boolean applyPrepaidLockingPeriod;

    @Column(name = "prepay_locking_period")
    private Integer prepayLockingPeriod;

    @Column(name = "apply_foreclosure_locking_period")
    private boolean applyForeclosureLockingPeriod;

    @Column(name = "foreclosure_locking_period")
    private Integer foreclosureLockingPeriod;


    @Column(name = "over_amount_details")
    private boolean overAmountDetails;

    @Column(name = "by_percentage_split")
    private boolean byPercentageSplit;

    @Column(name = "self_principal_share")
    private Integer selfPrincipalShare;

    @Column(name = "self_fee_share")
    private Integer selfFeeShare;

    @Column(name = "self_penalty_share")
    private Integer selfPenaltyShare;

    @Column(name = "self_overpaid_shares")
    private Integer selfOverpaidShares;

    @Column(name = "self_interest_rate",  scale = 6, precision = 19, nullable = true)
    private BigDecimal selfInterestRate;

    @Column(name = "principal_share")
    private Integer principalShare;

    @Column(name = "fee_share")
    private Integer feeShare;

    @Column(name = "penalty_share")
    private Integer penaltyShare;

    @Column(name = "overpaid_share")
    private Integer overpaidShare;

    @Column(name = "interest_rate", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestRate;

    @Column(name = "partner_principal_share")
    private Integer partnerPrincipalShare;

    @Column(name = "partner_fee_share")
    private Integer partnerFeeShare;

    @Column(name = "partner_penalty_share")
    private Integer partnerPenaltyShare;

    @Column(name = "partner_overpaid_share")
    private Integer partnerOverpaidShare;

    @Column(name = "partner_interest_rate", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerInterestRate;

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(name = "enable_charge_wise_bifacation")
    private boolean enableChargeWiseBifacation;

    @Column(name = "enable_overdue")
    private boolean enableOverdue;

    @Column(name = "enable_fees_wise_bifacation")
    private boolean enableFeesWiseBifacation;

    @Column(name = "select_charge")
    private String selectCharge;
    //
//    @Column(name = "colending_charge")
//    private Integer colendingCharge;
//
//    @Column(name = "self_charge")
//    private Integer selfCharge;
//
//    @Column(name = "partner_charge")
//    private Integer partnerCharge;
//
    @Column(name = "accepted_date_type")
    private String acceptedDateType;

    @Column(name = "accepted_start_date")
    private Integer acceptedStartDate;

    @Column(name = "accepted_end_date")
    private Integer acceptedEndDate;

    @Column(name = "accepted_date")
    private Integer acceptedDate;

    @Column(name = "aum_slab_rate")
    private BigDecimal aumSlabRate;

    @Column(name = "gst_liability_by_vcpl")
    private BigDecimal gstLiabilityByVcpl;

    @Column(name = "gst_liability_by_partner")
    private BigDecimal gstLiabilityByPartner;

//    @Column(name = "vcpl_hurdle_rate",  scale = 6, precision = 19, nullable = true)
//    private BigDecimal vcplHurdleRate;

//    @Column(name = "vcpl_share_in_pf")
//    private BigDecimal vcplShareInPf;
//
//    @Column(name = "partner_share_in_pf")
//    private BigDecimal partnerShareInPf;
//
//    @Column(name = "vcpl_share_in_penal_interest")
//    private BigDecimal vcplShareInPenalInterest;
//
//    @Column(name = "partner_share_in_penal_interest")
//    private BigDecimal partnerShareInPenalInterest;

    @Column(name = "vcpl_share_in_broken_interest")
    private BigDecimal vcplShareInBrokenInterest;

    @Column(name = "partner_share_in_broken_interest")
    private BigDecimal partnerShareInBrokenInterest;

//    @Column(name = "vcpl_share_in_foreclosure_charges")
//    private BigDecimal vcplShareInForeclosureCharges;
//
//    @Column(name = "partner_share_in_foreclosure_charges")
//    private BigDecimal partnerShareInForeclosureCharges;
//
//    @Column(name = "vcpl_share_in_other_charges")
//    private BigDecimal vcplShareInOtherCharges;
//
//    @Column(name = "partner_share_in_other_charges")
//    private BigDecimal partnerShareInOtherCharges;

    @Column(name = "monitoring_trigger_par_30")
    private Integer monitoringTriggerPar30;

    @Column(name = "monitoring_trigger_par_90")
    private Integer monitoringTriggerPar90;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductAcceptedDates> productAcceptedDates = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_class_cv_id", nullable = true)
    private CodeValue assetClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_applicability_cv_id", nullable = true)
    private CodeValue insuranceApplicability;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fldg_logic_cv_id", nullable = true)
    private CodeValue fldgLogic;


//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "disbursement_mode_cv_id", nullable = true)
//    private CodeValue disbursementMode;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "collection_mode_cv_id", nullable = true)
//    private CodeValue collectionMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "framework_cv_id", nullable = true)
    private CodeValue frameWork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loantype_cv_id", nullable = true)
    private CodeValue loanType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_cv_id", nullable = true)
    private CodeValue loanProductClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_cv_id", nullable = true)
    private CodeValue loanProductType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductFeesCharges> loanProductFeesCharges = new HashSet<>();

    @Column(name = "disbursement_account_number",length = 50, nullable = true)
    private Long disbursementAccountNumber;

    @Column(name = "collection_account_number",length = 50, nullable = true)
    private Long collectionAccountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penal_invoice_cv_id", nullable = true)
    private CodeValue penalInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multiple_disbursement_cv_id", nullable = true)
    private CodeValue multipleDisbursement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tranche_clubbing_cv_id", nullable = true)
    private CodeValue trancheClubbing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repayment_schedule_update_allowed_cv_id", nullable = true)
    private CodeValue repaymentScheduleUpdateAllowed;

    @Column(name = "emi_rounding_mode", nullable = true)
    private String emiroundingMode;

    @Column(name = "penny_drop_enabled", nullable = true)
    private Boolean isPennyDropEnabled;

    @Column(name = "bank_disbursement_enabled", nullable = true)
    private Boolean isBankDisbursementEnabled;

    @Column(name = "createdon_date",nullable = true)
    private Date createdDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdon_userid", nullable = true)
    private AppUser createdUser;

    @Column(name = "modifiedon_date",nullable = true)
    private Date modifiedDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedon_userid", nullable = true)
    private AppUser modifiedUser;

    @Column(name = "servicer_fee_interest_config_enabled",nullable = true)
    private boolean servicerFeeInterestConfigEnabled;

    @Column(name = "servicer_fee_charges_config_enabled",nullable = true)
    private boolean servicerFeeChargesConfigEnabled;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private ServicerFeeConfig servicerFeeConfig;

    @Column(name = "dedupe_enabled", nullable = true)
    private Boolean dedupeEnabled;

    @Column(name = "dedupe_enum", nullable = true)
    private Integer dedupeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursement_bank_acc_cv_id", nullable = true)
    private CodeValue disbursementBankAccount;

    @Column(name = "emi_multiples_of",nullable = true)
    private Integer emiMultiples;

    @Column(name = "emi_decimal_regex",nullable = true)
    private Integer emiDecimalRegex;

    @Column(name = "interest_decimal",nullable = true)
    private Integer interestDecimal;

    @Column(name = "interest_rounding_mode",nullable = true)
    private String interestRoundingMode;

    @Column(name = "interest_decimal_regex",nullable = true)
    private Integer interestDecimalRegex;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private ProductCollectionConfig productCollectionConfig;

    @Column(name = "emi_days_in_month",nullable = true)
    private Integer emiDaysInMonth;

    @Column(name = "emi_days_in_year",nullable = true)
    private Integer emiDaysInYear;

    @Column(name = "emi_calc_logic",nullable = true)
    private Integer emiCalcLogic;

    @Column(name = "enable_backdated_disbursement", nullable = false)
    private Boolean enableBackDatedDisbursement;


    public Partner getPartner() {
        return partner;
    }

    public Set<LoanProductFeesCharges> getLoanProductFeesCharges() {
        return loanProductFeesCharges;
    }

    public static  LoanProduct assembleFromJson(final Fund fund, LoanTransactionProcessingStrategy loanTransactionProcessingStrategy,
                                        final List<Charge> productCharges, final JsonCommand command, final AprCalculator aprCalculator, FloatingRate floatingRate,
                                        final List<Rate> productRates, final Partner partner, final CodeValue assetClass, final CodeValue framework, final CodeValue loanType, final CodeValue insuranceApplicability, final CodeValue fldgLogic, final CodeValue penalInvoice,
                                        final CodeValue multipleDisbursement, final CodeValue trancheClubbing, final CodeValue repaymentScheduleUpdateAllowed,
                                        final CodeValue loanProductClass, final CodeValue loanProductType, List<LoanProductFeeData> loanProductChargeData, final CodeValue disbursementAccount) {


            final String name = command.stringValueOfParameterNamed("name");
            final String shortName = command.stringValueOfParameterNamed(LoanProductConstants.SHORT_NAME);
        final String loanAccNoPreference = command.stringValueOfParameterNamed(LoanProductConstants.LOAN_ACC_NO_PREFERENCE);
            final String brokenInterestCalculationPeriod = command.stringValueOfParameterNamed("brokenInterestCalculationPeriod");
            // final boolean repaymentStrategyForNpa=command.booleanObjectValueOfParameterNamed("repaymentStrategyForNpa");
            final String repaymentStrategyForNpaId = command.stringValueOfParameterNamed("repaymentStrategyForNpaId");
            final String loanForeclosureStrategy = command.stringValueOfParameterNamed("loanForeclosureStrategy");
//            final String brokenInterestDaysInYears = command.stringValueOfParameterNamed("brokenInterestDaysInYears");
//            final String brokenInterestDaysInMonth = command.stringValueOfParameterNamed("brokenInterestDaysInMonth");
  //          final String brokenInterestStrategy = command.stringValueOfParameterNamed("brokenInterestStrategy");

        final String description = command.stringValueOfParameterNamed("description");
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed("digitsAfterDecimal");
        final Integer inMultiplesOf = command.integerValueOfParameterNamed("inMultiplesOf");
//        final Boolean coBorrower = command.booleanObjectValueOfParameterNamed("coBorrower");
//        final Boolean eodBalance = command.booleanObjectValueOfParameterNamed("eodBalance");
//        final Boolean securedLoan = command.booleanObjectValueOfParameterNamed("securedLoan");
//        final Boolean nonEquatedInstallment = command.booleanObjectValueOfParameterNamed("nonEquatedInstallment");
//
//        final Boolean advanceEMI = command.booleanObjectValueOfParameterNamed("advanceEMI");
//        final Boolean termBasedOnLoanCycle = command.booleanObjectValueOfParameterNamed("termBasedOnLoanCycle");
//        final Boolean isNetOffApplied = command.booleanObjectValueOfParameterNamed("isNetOffApplied");
//        final Boolean allowApprovalOverAmountApplied = command.booleanObjectValueOfParameterNamed("allowApprovalOverAmountApplied");

        final Boolean  useDaysInMonthForLoanProvisioning= command.booleanObjectValueOfParameterNamed("useDaysInMonthForLoanProvisioning");
        final Boolean divideByThirtyForPartialPeriod = command.booleanObjectValueOfParameterNamed("divideByThirtyForPartialPeriod");


        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);
        final BigDecimal principal = command.bigDecimalValueOfParameterNamed("principal");
        final BigDecimal minPrincipal = command.bigDecimalValueOfParameterNamed("minPrincipal");
        final BigDecimal maxPrincipal = command.bigDecimalValueOfParameterNamed("maxPrincipal");

        final Boolean enableColendingLoan = command.booleanObjectValueOfParameterNamed("enableColendingLoan");
        final Boolean selectAcceptedDates = command.booleanObjectValueOfParameterNamed("selectAcceptedDates");

        final Boolean applyPrepaidLockingPeriod = command.booleanObjectValueOfParameterNamed("applyPrepaidLockingPeriod");
        final Integer prepayLockingPeriod = command.integerValueOfParameterNamed("prepayLockingPeriod");
        final Boolean applyForeclosureLockingPeriod = command.booleanObjectValueOfParameterNamed("applyForeclosureLockingPeriod");
        final Integer foreclosureLockingPeriod = command.integerValueOfParameterNamed("foreclosureLockingPeriod");
//        final Integer minAge=command.integerValueOfParameterNamed("minimumAge");
//        final Integer maxAge=command.integerValueOfParameterNamed("maximumAge");
//        final boolean overAmountDetails=command.booleanObjectValueOfParameterNamed("overAmountDetails");
        final boolean overAmountDetails=false;
        final boolean byPercentageSplit=command.booleanObjectValueOfParameterNamed("byPercentageSplit");
        final Integer selfPrincipalShare=command.integerValueOfParameterNamed("selfPrincipalShare");
        final Integer selfFeeShare=command.integerValueOfParameterNamed("selfFeeShare");
        final Integer selfPenaltyShare=command.integerValueOfParameterNamed("selfPenaltyShare");
        final Integer selfOverpaidShares=command.integerValueOfParameterNamed("selfOverpaidShares");
        final BigDecimal selfInterestRate=command.bigDecimalValueOfParameterNamed("selfInterestRate");
        final Integer principalShare=command.integerValueOfParameterNamed("principalShare");
        final Integer feeShare=command.integerValueOfParameterNamed("feeShare");
        final Integer penaltyShare=command.integerValueOfParameterNamed("penaltyShare");
        final Integer overpaidShare=command.integerValueOfParameterNamed("overpaidShare");
        final BigDecimal interestRate=command.bigDecimalValueOfParameterNamed("interestRate");
        final Integer partnerPrincipalShare=command.integerValueOfParameterNamed("partnerPrincipalShare");
        final Integer partnerFeeShare=command.integerValueOfParameterNamed("partnerFeeShare");
        final Integer partnerPenaltyShare=command.integerValueOfParameterNamed("partnerPenaltyShare");
        final Integer partnerOverpaidShare=command.integerValueOfParameterNamed("partnerOverpaidShare");
        final BigDecimal partnerInterestRate=command.bigDecimalValueOfParameterNamed("partnerInterestRate");
        final boolean enableFeesWiseBifacation=command.booleanObjectValueOfParameterNamed("enableFeesWiseBifacation");
        final boolean enableChargeWiseBifacation=command.booleanObjectValueOfParameterNamed("enableChargeWiseBifacation");
        final boolean enableOverdue = command.booleanObjectValueOfParameterNamed("enableOverDue");
        //To DO
        final String selectCharge=command.stringValueOfParameterNamed("selectCharge");
        //final Integer colendingCharge=command.integerValueOfParameterNamed("colendingCharge");
        //final Integer selfCharge=command.integerValueOfParameterNamed("selfCharge");
        //final Integer partnerCharge=command.integerValueOfParameterNamed("partnerCharge");
        final String acceptedDateType=command.stringValueOfParameterNamed("acceptedDateType");
        final Integer acceptedStartDate=command.integerValueOfParameterNamed("acceptedStartDate");
        final Integer acceptedEndDate=command.integerValueOfParameterNamed("acceptedEndDate");
        //ToDO
        final Integer acceptedDate=command.integerValueOfParameterNamed("acceptedDate");
        final BigDecimal aumSlabRate=command.bigDecimalValueOfParameterNamed("aumSlabRate");
        final BigDecimal gstLiabilityByVcpl=command.bigDecimalValueOfParameterNamed("gstLiabilityByVcpl");
        final BigDecimal gstLiabilityByPartner=command.bigDecimalValueOfParameterNamed("gstLiabilityByPartner");

//        final BigDecimal vcplShareInPf=command.bigDecimalValueOfParameterNamed("vcplShareInPf");
//        final BigDecimal partnerShareInPf=command.bigDecimalValueOfParameterNamed("partnerShareInPf");
//        final BigDecimal vcplShareInPenalInterest=command.bigDecimalValueOfParameterNamed("vcplShareInPenalInterest");
//        final BigDecimal partnerShareInPenalInterest=command.bigDecimalValueOfParameterNamed("partnerShareInPenalInterest");
        final BigDecimal vcplShareInBrokenInterest=command.bigDecimalValueOfParameterNamed("vcplShareInBrokenInterest");
        final BigDecimal partnerShareInBrokenInterest=command.bigDecimalValueOfParameterNamed("partnerShareInBrokenInterest");
      //  final BigDecimal vcplHurdleRate=command.bigDecimalValueOfParameterNamed("vcplHurdleRate");
        final Integer monitoringTriggerPar30=command.integerValueOfParameterNamed("monitoringTriggerPar30");
        final Integer monitoringTriggerPar90=command.integerValueOfParameterNamed("monitoringTriggerPar90");

        final InterestMethod interestMethod = InterestMethod.fromInt(command.integerValueOfParameterNamed("interestType"));
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                .fromInt(command.integerValueOfParameterNamed("interestCalculationPeriodType"));
        final boolean allowPartialPeriodInterestCalcualtion = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME);
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.integerValueOfParameterNamed("amortizationType"));
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType
                .fromInt(command.integerValueOfParameterNamed("repaymentFrequencyType"));
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.INVALID;
        BigDecimal interestRatePerPeriod = null;
        BigDecimal minInterestRatePerPeriod = null;
        BigDecimal maxInterestRatePerPeriod = null;
        BigDecimal annualInterestRate = null;
        BigDecimal interestRateDifferential = null;
        BigDecimal minDifferentialLendingRate = null;
        BigDecimal maxDifferentialLendingRate = null;
        BigDecimal defaultDifferentialLendingRate = null;
        Boolean isFloatingInterestRateCalculationAllowed = null;

        Integer minimumGapBetweenInstallments = null;
        Integer maximumGapBetweenInstallments = null;

//        Integer minimumValue = null;
//        Integer maximumValue = null;

        Integer minimumAge = null;
        Integer maximumAge = null;

//        Integer prepayLockingPeriod = null;
//        Integer foreclosureLockingPeriod = null;

//        Boolean byPercentageSplit=false;
//        Integer selfPrincipalShare=null;
//        Integer selfFeeShare=null;
//        Integer selfPenaltyShare=null;
//        Integer selfOverpaidShares=null;
//        Integer selfInterestRate=null;
//        Integer principalShare=null;
//        Integer feeShare=null;
//        Integer penaltyShare=null;
//        Integer overpaidShare=null;
//        Integer interestRate=null;
//        String selectPartner=null;
//        Boolean enableChargeWiseBifacation=false;
//        String selectCharge=null;
//        Integer colendingCharge=null;
//        Integer selfCharge=null;
//        Integer partnerCharge=null;
//
//        String acceptedDateType=null;
//        Integer acceptedStartDate=null;
//        Integer acceptedEndDate=null;
//        Integer acceptedDate=null;

        final Integer repaymentEvery = command.integerValueOfParameterNamed("repaymentEvery");
        final Integer numberOfRepayments = command.integerValueOfParameterNamed("numberOfRepayments");
        final Boolean isLinkedToFloatingInterestRates = command.booleanObjectValueOfParameterNamed("isLinkedToFloatingInterestRates");
        if (isLinkedToFloatingInterestRates != null && isLinkedToFloatingInterestRates) {
            interestRateDifferential = command.bigDecimalValueOfParameterNamed("interestRateDifferential");
            minDifferentialLendingRate = command.bigDecimalValueOfParameterNamed("minDifferentialLendingRate");
            maxDifferentialLendingRate = command.bigDecimalValueOfParameterNamed("maxDifferentialLendingRate");
            defaultDifferentialLendingRate = command.bigDecimalValueOfParameterNamed("defaultDifferentialLendingRate");
            isFloatingInterestRateCalculationAllowed = command
                    .booleanObjectValueOfParameterNamed("isFloatingInterestRateCalculationAllowed");
        } else {
            interestFrequencyType = PeriodFrequencyType.fromInt(command.integerValueOfParameterNamed("interestRateFrequencyType"));
            interestRatePerPeriod = command.bigDecimalValueOfParameterNamed("interestRatePerPeriod");
            minInterestRatePerPeriod = command.bigDecimalValueOfParameterNamed("minInterestRatePerPeriod");
            maxInterestRatePerPeriod = command.bigDecimalValueOfParameterNamed("maxInterestRatePerPeriod");
            annualInterestRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, numberOfRepayments,
                    repaymentEvery, repaymentFrequencyType);

        }

        final Boolean isVariableInstallmentsAllowed = command
                .booleanObjectValueOfParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName);
        if (isVariableInstallmentsAllowed != null && isVariableInstallmentsAllowed) {
            minimumGapBetweenInstallments = command.integerValueOfParameterNamed(LoanProductConstants.minimumGapBetweenInstallments);
            maximumGapBetweenInstallments = command.integerValueOfParameterNamed(LoanProductConstants.maximumGapBetweenInstallments);
        }



//        final Boolean allowAgeLimits = command
//                .booleanObjectValueOfParameterNamed(LoanProductConstants.allowAgeLimitsParamName);
//        if (allowAgeLimits != null && allowAgeLimits) {
//            minimumAge = command.integerValueOfParameterNamed(LoanProductConstants.minimumAge);
//            maximumAge = command.integerValueOfParameterNamed(LoanProductConstants.maximumAge);
//        }

//        final Boolean isPrepaidLockingPeriodAllowed = command
//                .booleanObjectValueOfParameterNamed(LoanProductConstants.allowPrepaidLockingPeriodParamName);
//        if (isPrepaidLockingPeriodAllowed != null && isPrepaidLockingPeriodAllowed) {
//            prepayLockingPeriod = command.integerValueOfParameterNamed(LoanProductConstants.prepayLockingPeriod);
//        }
//
//        final Boolean isForeclosureLockingPeriodAllowed = command
//                .booleanObjectValueOfParameterNamed(LoanProductConstants.allowForeclosureLockingPeriodParamName);
//        if (isForeclosureLockingPeriodAllowed != null && isForeclosureLockingPeriodAllowed) {
//            foreclosureLockingPeriod = command.integerValueOfParameterNamed(LoanProductConstants.foreclosureLockingPeriod);
//        }

        final Integer minNumberOfRepayments = command.integerValueOfParameterNamed("minNumberOfRepayments");
        final Integer maxNumberOfRepayments = command.integerValueOfParameterNamed("maxNumberOfRepayments");
        final BigDecimal inArrearsTolerance = command.bigDecimalValueOfParameterNamed("inArrearsTolerance");

        final BrokenStrategy brokenStrategy = BrokenStrategy.
                fromInt(command.integerValueOfParameterNamed("brokenInterestStrategy"));

        final DisbursementMode disbursementMode = DisbursementMode.
                disbursement(command.integerValueOfParameterNamed("disbursement"));

        final CollectionMode collectionMode = CollectionMode.
                collection(command.integerValueOfParameterNamed("collection"));
        final BrokenStrategyDaysInMonth brokenStrategyDaysInMonth=BrokenStrategyDaysInMonth.fromInt(command.integerValueOfParameterNamed("brokenInterestDaysInMonth"));

        final BrokenStrategyDayInYear brokenStrategyDayInYear=BrokenStrategyDayInYear.fromInt(command.integerValueOfParameterNamed("brokenInterestDaysInYears"));
        final TransactionTypePreference transactionTypePreference = TransactionTypePreference.
                transactionTypePreference(command.integerValueOfParameterNamed("transactionTypePreference"));



        // grace details
        final Integer graceOnPrincipalPayment = command.integerValueOfParameterNamed("graceOnPrincipalPayment");
        final Integer recurringMoratoriumOnPrincipalPeriods = command.integerValueOfParameterNamed("recurringMoratoriumOnPrincipalPeriods");
        final Integer graceOnInterestPayment = command.integerValueOfParameterNamed("graceOnInterestPayment");
        final Integer graceOnInterestCharged = command.integerValueOfParameterNamed("graceOnInterestCharged");
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = command
                .integerValueOfParameterNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT);

        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));
        final boolean includeInBorrowerCycle = command.booleanPrimitiveValueOfParameterNamed("includeInBorrowerCycle");

        final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
        final LocalDate closeDate = command.localDateValueOfParameterNamed("closeDate");
        final String externalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final boolean useBorrowerCycle = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME);
        final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations = new HashSet<>();

        if (useBorrowerCycle) {
            populateBorrowerCyclevariations(command, loanProductBorrowerCycleVariations);
        }

        final Set<LoanProductAcceptedDates> loanProductAcceptedDates = new HashSet<>();
        assembleAcceptedDates(command,loanProductAcceptedDates);

        final boolean multiDisburseLoan = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME);
        Integer maxTrancheCount = null;
        BigDecimal outstandingLoanBalance = null;
        if (multiDisburseLoan) {
            outstandingLoanBalance = command.bigDecimalValueOfParameterNamed(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME);
            maxTrancheCount = command.integerValueOfParameterNamed(LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME);
        }

        final Integer graceOnArrearsAgeing = command
                .integerValueOfParameterNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME);

        final Integer overdueDaysForNPA = command.integerValueOfParameterNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME);

        // Interest recalculation settings
        final boolean isInterestRecalculationEnabled = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);
        final DaysInMonthType daysInMonthType = DaysInMonthType
                .fromInt(command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME));

        final DaysInYearType daysInYearType = DaysInYearType
                .fromInt(command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME));

        LoanProductInterestRecalculationDetails interestRecalculationSettings = null;

        if (isInterestRecalculationEnabled) {
            interestRecalculationSettings = LoanProductInterestRecalculationDetails.createFrom(command);
        }

        final boolean holdGuarantorFunds = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.holdGuaranteeFundsParamName);
        LoanProductGuaranteeDetails loanProductGuaranteeDetails = null;
        if (holdGuarantorFunds) {
            loanProductGuaranteeDetails = LoanProductGuaranteeDetails.createFrom(command);
        }

        LoanProductConfigurableAttributes loanConfigurableAttributes = null;
        if (command.parameterExists(LoanProductConstants.allowAttributeOverridesParamName)) {
            loanConfigurableAttributes = LoanProductConfigurableAttributes.createFrom(command);
        } else {
            loanConfigurableAttributes = LoanProductConfigurableAttributes.populateDefaultsForConfigurableAttributes();
        }

        BigDecimal principalThresholdForLastInstallment = command
                .bigDecimalValueOfParameterNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName);

        if (principalThresholdForLastInstallment == null) {
            principalThresholdForLastInstallment = multiDisburseLoan
                    ? LoanProductConstants.DEFAULT_PRINCIPAL_THRESHOLD_FOR_MULTI_DISBURSE_LOAN
                    : LoanProductConstants.DEFAULT_PRINCIPAL_THRESHOLD_FOR_SINGLE_DISBURSE_LOAN;
        }
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME);
        final boolean canDefineEmiAmount = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.canDefineEmiAmountParamName);
        final Integer installmentAmountInMultiplesOf = command
                .integerValueOfParameterNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName);

        final boolean syncExpectedWithDisbursementDate = command.booleanPrimitiveValueOfParameterNamed("syncExpectedWithDisbursementDate");

        final boolean canUseForTopup = command.parameterExists(LoanProductConstants.CAN_USE_FOR_TOPUP)
                ? command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.CAN_USE_FOR_TOPUP)
                : false;

        final boolean isEqualAmortization = command.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM)
                ? command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM)
                : false;

        BigDecimal fixedPrincipalPercentagePerInstallment = command
                .bigDecimalValueOfParameterNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName);

        final boolean disallowExpectedDisbursements = command.parameterExists(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS)
                ? command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS)
                : false;

        final boolean allowApprovedDisbursedAmountsOverApplied = command
                .parameterExists(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED)
                ? command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED)
                : false;

        final String overAppliedCalculationType = command
                .stringValueOfParameterNamedAllowingNull(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE);

        final Integer overAppliedNumber = command.integerValueOfParameterNamed(LoanProductConstants.OVER_APPLIED_NUMBER);
        final Long disbursementAccountNumber = command.longValueOfParameterNamed("disbursementAccountNumber");
        final Long collectionAccountNumber = command.longValueOfParameterNamed("collectionAccountNumber");

        final String roundingMode = command.stringValueOfParameterNamed(LoanProductConstants.EMIROUNDINGMODE);

        final Boolean isPennyDropEnabled = command.booleanObjectValueOfParameterNamed("isPennyDropEnabled");
        final Boolean isBankDisbursementEnabled = command.booleanObjectValueOfParameterNamed("isBankDisbursementEnabled");

        final Boolean servicerFeeInterestConfigEnabled =  command.booleanObjectValueOfParameterNamed("servicerFeeInterestConfigEnabled");
        final Boolean servicerFeeChargesConfigEnabled = command.booleanObjectValueOfParameterNamed("servicerFeeChargesConfigEnabled");
        final boolean enableDedupe = command.booleanObjectValueOfParameterNamed("enableDedupe");
        final Integer dedupeType = command.integerValueOfParameterNamed("dedupeType");

        final MultiplesOf emiMultiplesOf =  MultiplesOf.fromInt(command.integerValueOfParameterNamed("emimultiples"));
        final Integer emiRegex = command.integerValueOfParameterNamed("emiDecimalRegex");
        final Integer interestRegex = command.integerValueOfParameterNamed(LoanProductConstants.INTERESTDECIMALREGEX);
        final Integer intererstDecimal = command.integerValueOfParameterNamed(LoanProductConstants.INTERESTDECIMAL);
        final String interestRoundingMode = command.stringValueOfParameterNamed(LoanProductConstants.INTERESTROUNDINGMODE);
        Boolean  enableEntryForAdvanceTransaction = true;
        final Integer advanceAppropriation = command.integerValueOfParameterNamed(LoanProductConstants.ADVANCEAPPROPRIATION);
        if(!AdvanceAppropriationOn.isOnDueDate(advanceAppropriation)){
            enableEntryForAdvanceTransaction = command.booleanObjectValueOfParameterNamed(LoanProductConstants.ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION);
        }
         Boolean  interestBenefitEnabled = command.booleanObjectValueOfParameterNamed(LoanProductConstants.INTEREST_BENEFIT_ENABLED);
        if (Objects.isNull(interestBenefitEnabled)) {
            interestBenefitEnabled = false;
        }
        final Integer foreclosureOnDueDateInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST);
        final Integer foreclosureOnDueDateCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE);
        final Integer foreclosureOtherThanDueDateInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST);
        final Integer foreclosureOtherThanDueDateCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE);
        final Integer foreclosureOneMonthOverdueInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST);
        final Integer foreclosureOneMonthOverdueCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE);
        final Integer foreclosureShortPaidInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST);
        final Integer foreclosureShortPaidInterestCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE);
        final Integer foreclosurePrincipalShortPaidInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST);
        final Integer foreclosurePrincipalShortPaidCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE);
        final Integer foreclosureTwoMonthsOverdueInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST);
        final Integer foreclosureTwoMonthsOverdueCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE);
        final Integer foreclosurePosAdvanceOnDueDate =  command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE);
        final Integer foreclosureAdvanceOnDueDateInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST);
        final Integer foreclosureAdvanceOnDueDateCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE);
        final Integer foreclosurePosAdvanceOtherThanDueDate =  command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE);
        final Integer foreclosureAdvanceAfterDueDateInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST);
        final Integer foreclosureAdvanceAfterDueDateCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE);
        final Integer foreclosureBackdatedShortPaidInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST);
        final Integer foreclosureBackdatedShortPaidInterestCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE);
        final Integer foreclosureBackdatedFullyPaidInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST);
        final Integer foreclosureBackdatedFullyPaidInterestCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE);
        final Integer foreclosureBackdatedShortPaidPrincipalInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST);
        final Integer foreclosureBackdatedShortPaidPrincipalCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE);
        final Integer foreclosureBackdatedFullyPaidEmiInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST);
        final Integer foreclosureBackdatedFullyPaidEmiCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE);
        final Integer foreclosureBackdatedAdvanceInterest = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST);
        final Integer foreclosureBackdatedAdvanceCharge = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE);
        final Integer foreclosureMethodType = command.integerValueOfParameterNamed(LoanProductConstants.FORECLOSURE_METHOD_TYPE);
		final Boolean coolingOffApplicability = command.booleanObjectValueOfParameterNamed(LoanProductConstants.COOLING_OFF_APPLICABILITY);
        final Integer coolingOffThresholdDays = command.integerValueOfParameterNamed(LoanProductConstants.COOLING_OFF_THRESHOLD_DAYS);
        final Integer coolingOffInterestAndChargeApplicability = command.integerValueOfParameterNamed(LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY);
        final Integer coolingOffInterestLogicApplicability = command.integerValueOfParameterNamed(LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY);
        final Integer coolingOffDaysInYear = command.integerValueOfParameterNamed(LoanProductConstants.COOLING_OFF_DAYS_IN_YEAR);
        final String coolingOffRoundingMode = command.stringValueOfParameterNamed(LoanProductConstants.COOLING_OFF_ROUNDING_MODE);
        final Integer coolingOffRoundingDecimals = command.integerValueOfParameterNamed(LoanProductConstants.COOLING_OFF_ROUNDING_DECIMALS);

        final Integer emiDaysInMonth = command.integerValueOfParameterNamed(LoanProductConstants.PMT_DAYS_IN_MONTH_TYPE);

        final Integer emiDaysInYear = command.integerValueOfParameterNamed(LoanProductConstants.PMT_DAYS_IN_YEAR_TYPE);

        final Integer emiCalcLogic = command.integerValueOfParameterNamed(LoanProductConstants.PMT_FORMULA_CALCULATION);

        final Integer advanceAppropriationAgainstOn = command.integerValueOfParameterNamed(LoanProductConstants.ADVANCE_APPROPRIATION_AGAINST_ON);

        final Boolean enableBackDatedDisbursement = command.booleanObjectValueOfParameterNamed(LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT);


        return new LoanProduct(fund,loanTransactionProcessingStrategy, name, shortName,loanAccNoPreference, description, currency, principal, minPrincipal,
                maxPrincipal, interestRatePerPeriod, minInterestRatePerPeriod, maxInterestRatePerPeriod, interestFrequencyType,
                annualInterestRate, interestMethod, interestCalculationPeriodMethod, allowPartialPeriodInterestCalcualtion, repaymentEvery,
                repaymentFrequencyType, numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, amortizationMethod,
                inArrearsTolerance, productCharges, accountingRuleType, includeInBorrowerCycle, startDate, closeDate, externalId,
                useBorrowerCycle, loanProductBorrowerCycleVariations, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationSettings, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuarantorFunds,
                loanProductGuaranteeDetails, principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion,
                canDefineEmiAmount, installmentAmountInMultiplesOf, loanConfigurableAttributes, isLinkedToFloatingInterestRates,
                floatingRate, interestRateDifferential, minDifferentialLendingRate, maxDifferentialLendingRate,
                defaultDifferentialLendingRate, isFloatingInterestRateCalculationAllowed,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, productRates, fixedPrincipalPercentagePerInstallment, disallowExpectedDisbursements,
                allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType, overAppliedNumber,
                isVariableInstallmentsAllowed,minimumGapBetweenInstallments,maximumGapBetweenInstallments,minimumAge,maximumAge,brokenInterestCalculationPeriod,repaymentStrategyForNpaId,
                loanForeclosureStrategy,brokenStrategyDayInYear, brokenStrategyDaysInMonth,
                useDaysInMonthForLoanProvisioning,divideByThirtyForPartialPeriod,enableColendingLoan, selectAcceptedDates,applyPrepaidLockingPeriod,prepayLockingPeriod,
                applyForeclosureLockingPeriod,foreclosureLockingPeriod,loanProductAcceptedDates,overAmountDetails,
                byPercentageSplit,selfPrincipalShare,selfFeeShare,selfPenaltyShare,selfOverpaidShares,
                selfInterestRate,principalShare,feeShare,penaltyShare,overpaidShare,interestRate,partner,enableFeesWiseBifacation,enableChargeWiseBifacation, enableOverdue,
                acceptedDateType,acceptedStartDate,acceptedEndDate,acceptedDate,selectCharge,
                partnerPrincipalShare,partnerFeeShare,partnerPenaltyShare,partnerOverpaidShare,partnerInterestRate,
                aumSlabRate,gstLiabilityByVcpl,gstLiabilityByPartner,vcplShareInBrokenInterest,
                partnerShareInBrokenInterest,monitoringTriggerPar30,monitoringTriggerPar90,assetClass,
                framework, loanType,insuranceApplicability,fldgLogic,loanProductClass,loanProductType,
                loanProductChargeData,brokenStrategy,disbursementMode,collectionMode,disbursementAccountNumber,
                collectionAccountNumber,penalInvoice,multipleDisbursement,trancheClubbing,repaymentScheduleUpdateAllowed,transactionTypePreference,roundingMode,
                isPennyDropEnabled,isBankDisbursementEnabled,servicerFeeInterestConfigEnabled,servicerFeeChargesConfigEnabled,
                enableDedupe, dedupeType,disbursementAccount,emiMultiplesOf.getValue(),intererstDecimal,interestRoundingMode,emiRegex,interestRegex,advanceAppropriation,enableEntryForAdvanceTransaction,interestBenefitEnabled,
                foreclosureOnDueDateInterest,foreclosureOnDueDateCharge,foreclosureOtherThanDueDateInterest,foreclosureOtherThanDueDateCharge,foreclosureOneMonthOverdueInterest,foreclosureOneMonthOverdueCharge,foreclosureShortPaidInterest
                ,foreclosureShortPaidInterestCharge,foreclosurePrincipalShortPaidInterest,foreclosurePrincipalShortPaidCharge,foreclosureTwoMonthsOverdueInterest,foreclosureTwoMonthsOverdueCharge,foreclosurePosAdvanceOnDueDate,foreclosureAdvanceOnDueDateInterest
                ,foreclosureAdvanceOnDueDateCharge,foreclosurePosAdvanceOtherThanDueDate,foreclosureAdvanceAfterDueDateInterest,foreclosureAdvanceAfterDueDateCharge,foreclosureBackdatedShortPaidInterest,foreclosureBackdatedShortPaidInterestCharge,foreclosureBackdatedFullyPaidInterest
                ,foreclosureBackdatedFullyPaidInterestCharge,foreclosureBackdatedShortPaidPrincipalInterest,foreclosureBackdatedShortPaidPrincipalCharge,foreclosureBackdatedFullyPaidEmiInterest,foreclosureBackdatedFullyPaidEmiCharge
                ,foreclosureBackdatedAdvanceInterest,foreclosureBackdatedAdvanceCharge,advanceAppropriationAgainstOn,emiDaysInMonth,emiDaysInYear,emiCalcLogic,enableBackDatedDisbursement,foreclosureMethodType,
				coolingOffApplicability,coolingOffThresholdDays,coolingOffInterestAndChargeApplicability,coolingOffInterestLogicApplicability,coolingOffDaysInYear,coolingOffRoundingMode,coolingOffRoundingDecimals);

    }


    public void updateLoanProductInRelatedClasses() {
        if (this.isInterestRecalculationEnabled()) {
            this.productInterestRecalculationDetails.updateProduct(this);
        }
        if (this.holdGuaranteeFunds) {
            this.loanProductGuaranteeDetails.updateProduct(this);
        }
    }



    private static void assembleAcceptedDates(final JsonCommand command,
                                              final Set<LoanProductAcceptedDates> loanProductAcceptedDates) {
        if (command.parameterExists(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME)) {
            final JsonArray selectDateArr = command.arrayOfParameterNamed(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME);
            if (selectDateArr != null && selectDateArr.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = selectDateArr.get(i).getAsJsonObject();
                    Integer acceptedDate = null;
                    if (jsonObject.has(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME).isJsonPrimitive()) {
                        acceptedDate = jsonObject.getAsJsonPrimitive(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME).getAsInt();
                    }

                    LoanProductAcceptedDates productAcceptedDates = new LoanProductAcceptedDates(acceptedDate);
                    loanProductAcceptedDates.add(productAcceptedDates);
                    i++;
                } while (i < selectDateArr.size());
            }
        }
    }

    /**
     * @param command
     * @param loanProductBorrowerCycleVariations
     */
    private static void populateBorrowerCyclevariations(final JsonCommand command,
                                                        final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assemblePrincipalVariations(command, loanProductBorrowerCycleVariations);

        assembleRepaymentVariations(command, loanProductBorrowerCycleVariations);

        assembleInterestRateVariations(command, loanProductBorrowerCycleVariations);
    }

    /**
     * @param command
     * @param loanProductBorrowerCycleVariations
     */
    private static void assembleInterestRateVariations(final JsonCommand command,
                                                       final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assembleVaritions(command, loanProductBorrowerCycleVariations, LoanProductParamType.INTERESTRATE.getValue(),
                LoanProductConstants.INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME);

    }

    /**
     * @param command
     * @param loanProductBorrowerCycleVariations
     */
    private static void assembleRepaymentVariations(final JsonCommand command,
                                                    final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assembleVaritions(command, loanProductBorrowerCycleVariations, LoanProductParamType.REPAYMENT.getValue(),
                LoanProductConstants.NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME);

    }

    /**
     * @param command
     * @param loanProductBorrowerCycleVariations
     */
    private static void assemblePrincipalVariations(final JsonCommand command,
                                                    final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assembleVaritions(command, loanProductBorrowerCycleVariations, LoanProductParamType.PRINCIPAL.getValue(),
                LoanProductConstants.PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME);
    }

    private static void assembleVaritions(final JsonCommand command,
                                          final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations, Integer paramType,
                                          String variationParameterName) {
        if (command.parameterExists(variationParameterName)) {
            final JsonArray variationArray = command.arrayOfParameterNamed(variationParameterName);
            if (variationArray != null && variationArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();
                    BigDecimal defaultValue = null;
                    BigDecimal minValue = null;
                    BigDecimal maxValue = null;
                    Integer cycleNumber = null;
                    Integer valueUsageCondition = null;
                    if (jsonObject.has(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).isJsonPrimitive()) {
                        defaultValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MIN_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsString())) {
                        minValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MAX_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsString())) {
                        maxValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).isJsonPrimitive()) {
                        cycleNumber = jsonObject.getAsJsonPrimitive(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME).isJsonPrimitive()) {
                        valueUsageCondition = jsonObject.getAsJsonPrimitive(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                                .getAsInt();
                    }
                    LoanProductBorrowerCycleVariations borrowerCycleVariations = new LoanProductBorrowerCycleVariations(cycleNumber,
                            paramType, valueUsageCondition, minValue, maxValue, defaultValue);
                    loanProductBorrowerCycleVariations.add(borrowerCycleVariations);
                    i++;
                } while (i < variationArray.size());
            }
        }
    }

    private Map<String, Object> updateBorrowerCycleVariations(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);
        List<Long> variationIds = fetchAllVariationIds();
        updateBorrowerCycleVaritions(command, LoanProductParamType.PRINCIPAL.getValue(),
                LoanProductConstants.PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, actualChanges, variationIds);
        updateBorrowerCycleVaritions(command, LoanProductParamType.INTERESTRATE.getValue(),
                LoanProductConstants.INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, actualChanges, variationIds);
        updateBorrowerCycleVaritions(command, LoanProductParamType.REPAYMENT.getValue(),
                LoanProductConstants.NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, actualChanges, variationIds);
        for (Long id : variationIds) {
            this.borrowerCycleVariations.remove(fetchLoanProductBorrowerCycleVariationById(id));
        }
        return actualChanges;
    }

    private List<Long> fetchAllVariationIds() {
        List<Long> list = new ArrayList<>();
        for (LoanProductBorrowerCycleVariations cycleVariation : this.borrowerCycleVariations) {
            list.add(cycleVariation.getId());
        }
        return list;
    }

    private void updateBorrowerCycleVaritions(final JsonCommand command, Integer paramType, String variationParameterName,
                                              final Map<String, Object> actualChanges, List<Long> variationIds) {
        if (command.parameterExists(variationParameterName)) {
            final JsonArray variationArray = command.arrayOfParameterNamed(variationParameterName);
            if (variationArray != null && variationArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();
                    BigDecimal defaultValue = null;
                    BigDecimal minValue = null;
                    BigDecimal maxValue = null;
                    Integer cycleNumber = null;
                    Integer valueUsageCondition = null;
                    Long id = null;
                    if (jsonObject.has(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).isJsonPrimitive()) {
                        defaultValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MIN_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsString())) {
                        minValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MAX_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsString())) {
                        maxValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).isJsonPrimitive()) {
                        cycleNumber = jsonObject.getAsJsonPrimitive(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME).isJsonPrimitive()) {
                        valueUsageCondition = jsonObject.getAsJsonPrimitive(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                                .getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME).isJsonPrimitive() && StringUtils
                            .isNotBlank(jsonObject.get(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME).getAsString())) {
                        id = jsonObject.getAsJsonPrimitive(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME).getAsLong();
                    }
                    LoanProductBorrowerCycleVariations borrowerCycleVariations = new LoanProductBorrowerCycleVariations(cycleNumber,
                            paramType, valueUsageCondition, minValue, maxValue, defaultValue);
                    if (id == null) {
                        borrowerCycleVariations.updateLoanProduct(this);
                        this.borrowerCycleVariations.add(borrowerCycleVariations);
                        actualChanges.put("borrowerCycleParamType", paramType);
                    } else {
                        variationIds.remove(id);
                        LoanProductBorrowerCycleVariations existingCycleVariation = fetchLoanProductBorrowerCycleVariationById(id);
                        if (!existingCycleVariation.equals(borrowerCycleVariations)) {
                            existingCycleVariation.copy(borrowerCycleVariations);
                            actualChanges.put("borrowerCycleId", id);
                        }
                    }
                    i++;
                } while (i < variationArray.size());
            }
        }
    }

    private void clearVariations(LoanProductParamType paramType, boolean clearAll) {
        if (clearAll) {
            this.borrowerCycleVariations.clear();
        } else {
            Set<LoanProductBorrowerCycleVariations> remove = new HashSet<>();
            for (LoanProductBorrowerCycleVariations borrowerCycleVariations : this.borrowerCycleVariations) {
                if (paramType.equals(borrowerCycleVariations.getParamType())) {
                    remove.add(borrowerCycleVariations);
                }
            }
            this.borrowerCycleVariations.removeAll(remove);
        }
    }

    public LoanProduct() {
        this.loanProductRelatedDetail = null;
        this.loanProductMinMaxConstraints = null;
    }



    public LoanProduct(final Fund fund, LoanTransactionProcessingStrategy transactionProcessingStrategy, final String name,
                       final String shortName,final String loanAccNoPreference, final String description, final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
                       final BigDecimal defaultMinPrincipal, final BigDecimal defaultMaxPrincipal,
                       final BigDecimal defaultNominalInterestRatePerPeriod, final BigDecimal defaultMinNominalInterestRatePerPeriod,
                       final BigDecimal defaultMaxNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
                       final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
                       final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean considerPartialPeriodInterest,
                       final Integer repayEvery, final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfInstallments,
                       final Integer defaultMinNumberOfInstallments, final Integer defaultMaxNumberOfInstallments,
                       final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods,
                       final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final AmortizationMethod amortizationMethod,
                       final BigDecimal inArrearsTolerance, final List<Charge> charges, final AccountingRuleType accountingRuleType,
                       final boolean includeInBorrowerCycle, final LocalDate startDate, final LocalDate closeDate, final String externalId,
                       final boolean useBorrowerCycle, final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations,
                       final boolean multiDisburseLoan, final Integer maxTrancheCount, final BigDecimal outstandingLoanBalance,
                       final Integer graceOnArrearsAgeing, final Integer overdueDaysForNPA, final DaysInMonthType daysInMonthType,
                       final DaysInYearType daysInYearType, final boolean isInterestRecalculationEnabled,
                       final LoanProductInterestRecalculationDetails productInterestRecalculationDetails,
                       final Integer minimumDaysBetweenDisbursalAndFirstRepayment, final boolean holdGuarantorFunds,
                       final LoanProductGuaranteeDetails loanProductGuaranteeDetails, final BigDecimal principalThresholdForLastInstallment,
                       final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion, final boolean canDefineEmiAmount,
                       final Integer installmentAmountInMultiplesOf, final LoanProductConfigurableAttributes loanProductConfigurableAttributes,
                       Boolean isLinkedToFloatingInterestRates, FloatingRate floatingRate, BigDecimal interestRateDifferential,
                       BigDecimal minDifferentialLendingRate, BigDecimal maxDifferentialLendingRate, BigDecimal defaultDifferentialLendingRate,
                       Boolean isFloatingInterestRateCalculationAllowed, final boolean syncExpectedWithDisbursementDate, final boolean canUseForTopup, final boolean isEqualAmortization,
                       final List<Rate> rates, final BigDecimal fixedPrincipalPercentagePerInstallment, final boolean disallowExpectedDisbursements,
                       final boolean allowApprovedDisbursedAmountsOverApplied, final String overAppliedCalculationType,
                       final Integer overAppliedNumber, final Boolean isVariableInstallmentsAllowed,
                       final Integer minimumGapBetweenInstallments, final Integer maximumGapBetweenInstallments, final Integer minimumAge, final Integer maximumAge,
                       final String brokenInterestCalculationPeriod,
                       final String repaymentStrategyForNpaId, final String loanForeclosureStrategy, final BrokenStrategyDayInYear brokenInterestDaysInYears, final BrokenStrategyDaysInMonth brokenInterestDaysInMonth,
                       final boolean useDaysInMonthForLoanProvisioning,
                       final boolean divideByThirtyForPartialPeriod,
                       final boolean enableColendingLoan, final boolean selectAcceptedDates, final boolean applyPrepaidLockingPeriod, final Integer prepayLockingPeriod, final boolean applyForeclosureLockingPeriod, final Integer foreclosureLockingPeriod,
                       final Set<LoanProductAcceptedDates> productAcceptedDates, final boolean overAmountDetails, final boolean byPercentageSplit, final Integer selfPrincipalShare, final Integer selfFeeShare, final Integer selfPenaltyShare, final Integer selfOverpaidShares, final BigDecimal selfInterestRate, final Integer principalShare, final Integer feeShare, final Integer penaltyShare,
                       final Integer overpaidShare, final BigDecimal interestRate, final Partner partner, final boolean enableFeesWiseBifacation,
                       final boolean enableChargeWiseBifacation, final boolean enableOverdue,
                       final String acceptedDateType, final Integer acceptedStartDate, final Integer acceptedEndDate, final Integer accepetedDate, final String selectCharge, final Integer partnerPrincipalShare, final Integer partnerFeeShare, final Integer partnerPenaltyShare, final Integer partnerOverpaidShare, final BigDecimal partnerInterestRate,
                       final BigDecimal aumSlabRate, final BigDecimal gstLiabilityByVcpl, final BigDecimal gstLiabilityByPartner,
                       final BigDecimal vcplShareInBrokenInterest, final BigDecimal partnerShareInBrokenInterest,
                       final Integer monitoringTriggerPar30,
                       final Integer monitoringTriggerPar90, final CodeValue assetClass, final CodeValue framework, final CodeValue loanType, final CodeValue insuranceApplicability, final CodeValue fldgLogic,
                       final CodeValue loanProductClass, final CodeValue loanProductType, List<LoanProductFeeData> loanProductChargeData, final BrokenStrategy brokenStrategy, final DisbursementMode disbursementMode, final CollectionMode collectionMode,
                       final Long disbursementAccountNumber, final Long collectionAccountNumber, final CodeValue penalInvoice,
                       final CodeValue multipleDisbursement, CodeValue trancheClubbing, final CodeValue repaymentScheduleUpdateAllowed, final TransactionTypePreference transactionTypePreference, final String emiRoundingMode,
                       final Boolean isPennyDropEnabled, final Boolean isBankDisbursementEnabled, final Boolean servicerFeeInterestConfigEnabled, final Boolean servicerFeeChargesConfigEnabled,
                       final boolean dedupeEnabled, final Integer dedupeType, final CodeValue disbursementAccount, Integer emiMultiples,
                       Integer intererstDecimalMode, String interestRoundingMode, final Integer emiDecimalRegex, final Integer interestRegex, Integer advanceAppropriation, boolean enableEntryForAdvanceTransaction, boolean interestBenefitEnabled,
                       final Integer foreclosureOnDueDateInterest, final Integer foreclosureOnDueDateCharge, final Integer foreclosureOtherThanDueDateInterest, final Integer foreclosureOtherThanDueDateCharge, final Integer foreclosureOneMonthOverdueInterest, final Integer foreclosureOneMonthOverdueCharge,
                       final Integer foreclosureShortPaidInterest, final Integer foreclosureShortPaidInterestCharge, final Integer foreclosurePrincipalShortPaidInterest, final Integer foreclosurePrincipalShortPaidCharge,
                       final Integer foreclosureTwoMonthsOverdueInterest, final Integer foreclosureTwoMonthsOverdueCharge, final Integer foreclosurePosAdvanceOnDueDate, final Integer foreclosureAdvanceOnDueDateInterest, final Integer foreclosureAdvanceOnDueDateCharge, final Integer foreclosurePosAdvanceOtherThanDueDate, final Integer foreclosureAdvanceAfterDueDateInterest,
                       final Integer foreclosureAdvanceAfterDueDateCharge, final Integer foreclosureBackdatedShortPaidInterest, final Integer foreclosureBackdatedShortPaidInterestCharge, final Integer foreclosureBackdatedFullyPaidInterest, final Integer foreclosureBackdatedFullyPaidInterestCharge,
                       final Integer foreclosureBackdatedShortPaidPrincipalInterest, final Integer foreclosureBackdatedShortPaidPrincipalCharge, final Integer foreclosureBackdatedFullyPaidEmiInterest, final Integer foreclosureBackdatedFullyPaidEmiCharge,
                       final Integer foreclosureBackdatedAdvanceInterest, final Integer foreclosureBackdatedAdvanceCharge, final Integer advanceAppropriationAgainstOn,
                       final Integer emiDaysInMonth, Integer emiDaysInYear, Integer emiCalcLogic, final Boolean enableBackDatedDisbursement, final Integer foreclosureMethodType,
					   final Boolean coolingOffApplicability,final Integer coolingOffThresholdDays, final Integer coolingOffInterestAndChargeApplicability,final Integer coolingOffInterestLogicApplicability,
					   final Integer coolingOffDaysInYear,final String coolingOffRoundingMode,final Integer coolingOffRoundingDecimals) {

        LoanProductFeesCharges loanProductFeesCharges=new LoanProductFeesCharges();
        Set<LoanProductFeesCharges> loanProductFees =loanProductFeesCharges.loanCharges(this,loanProductChargeData);

        this.loanProductFeesCharges=loanProductFees;
        this.assetClass=assetClass;
        this.frameWork=framework;
        this.loanType=loanType;
        this.insuranceApplicability=insuranceApplicability;
        this.fldgLogic=fldgLogic;

//        this.disbursementMode=disbursementMode;
//        this.collectionMode=collectionMode;
        this.loanProductClass=loanProductClass;
        this.loanProductType=loanProductType;
        //this.loanProductFeesCharges= new LoanProductFeesCharges(this,loanProductChargeData);


        this.fund = fund;
        this.transactionProcessingStrategy = transactionProcessingStrategy;
        this.name = name.trim();
        this.shortName = shortName.trim();
        this.loanAccNoPreference = loanAccNoPreference.trim();
        this.brokenInterestCalculationPeriod=brokenInterestCalculationPeriod;
//      this.repaymentStrategyForNpa=repaymentStrategyForNpa;
        this.repaymentStrategyForNpaId=repaymentStrategyForNpaId;
        this.loanForeclosureStrategy=loanForeclosureStrategy;
//        this.brokenInterestDaysInYears=brokenInterestDaysInYears;
//        this.brokenInterestDaysInMonth=brokenInterestDaysInMonth;
//        this.brokenInterestStrategy=brokenInterestStrategy;

        this.overAmountDetails=overAmountDetails;
        this.byPercentageSplit=byPercentageSplit;
        this.selfPrincipalShare=selfPrincipalShare;
        this.selfFeeShare=selfFeeShare;
        this.selfPenaltyShare=selfPenaltyShare;
        this.selfOverpaidShares=selfOverpaidShares;
        this.selfInterestRate=selfInterestRate;
        this.principalShare=principalShare;
        this.feeShare=feeShare;
        this.penaltyShare=penaltyShare;
        this.overpaidShare=overpaidShare;
        this.interestRate=interestRate;
        this.partnerPrincipalShare=partnerPrincipalShare;
        this.partnerFeeShare=partnerFeeShare;
        this.partnerPenaltyShare=partnerPenaltyShare;
        this.partnerOverpaidShare=partnerOverpaidShare;
        this.partnerInterestRate=partnerInterestRate;
        this.partner=partner;
        this.enableFeesWiseBifacation=enableFeesWiseBifacation;
        this.enableChargeWiseBifacation=enableChargeWiseBifacation;
        this.enableOverdue = enableOverdue;
        this.selectCharge=selectCharge;
      //  this.vcplHurdleRate=vcplHurdleRate;
//        this.colendingCharge=colendingCharge;
//        this.selfCharge=selfCharge;
//        this.partnerCharge=partnerCharge;
        this.acceptedDateType=acceptedDateType;
        this.acceptedStartDate=acceptedStartDate;
        this.acceptedEndDate=acceptedEndDate;
//        this.acceptedDate=acceptedDate;
        this.aumSlabRate=aumSlabRate;
        this.gstLiabilityByVcpl=gstLiabilityByVcpl;
        this.gstLiabilityByPartner=gstLiabilityByPartner;

//        this.vcplShareInPf=vcplShareInPf;
//        this.partnerShareInPf=partnerShareInPf;
//        this.vcplShareInPenalInterest=vcplShareInPenalInterest;
//        this.partnerShareInPenalInterest=partnerShareInPenalInterest;
        this.vcplShareInBrokenInterest=vcplShareInBrokenInterest;
        this.partnerShareInBrokenInterest=partnerShareInBrokenInterest;
//        this.vcplShareInForeclosureCharges=vcplShareInForeclosureCharges;
//        this.partnerShareInForeclosureCharges=partnerShareInForeclosureCharges;
//        this.vcplShareInOtherCharges=vcplShareInOtherCharges;
//        this.partnerShareInOtherCharges=partnerShareInOtherCharges;
        this.monitoringTriggerPar30=monitoringTriggerPar30;
        this.monitoringTriggerPar90=monitoringTriggerPar90;


        if (StringUtils.isNotBlank(description)) {
            this.description = description.trim();
        } else {
            this.description = null;
        }


        if (charges != null) {
            this.charges = charges;
        }


        this.isLinkedToFloatingInterestRate = isLinkedToFloatingInterestRates == null ? false : isLinkedToFloatingInterestRates;
        if (isLinkedToFloatingInterestRate) {
            this.floatingRates = new LoanProductFloatingRates(floatingRate, this, interestRateDifferential, minDifferentialLendingRate,
                    maxDifferentialLendingRate, defaultDifferentialLendingRate, isFloatingInterestRateCalculationAllowed);
        }

        this.allowVariabeInstallments = isVariableInstallmentsAllowed == null ? false : isVariableInstallmentsAllowed;

        if (allowVariabeInstallments) {
            this.variableInstallmentConfig = new LoanProductVariableInstallmentConfig(this, minimumGapBetweenInstallments,
                    maximumGapBetweenInstallments);
        }


//        this.allowAgeLimits = allowAgeLimits == null ? false : allowAgeLimits;
//
//        if (allowAgeLimits) {
//            this.ageLimitsConfig = new LoanProductAgeLimitsConfig(this, minimumAge,
//                    maximumAge);
//        }

//        this.allowPrepaidLockingPeriod = isPrepaidLockingPeriodAllowed == null ? false : isPrepaidLockingPeriodAllowed;
//
//        if (allowPrepaidLockingPeriod) {
//            this.prepaidLoanConfig = new LoanProductPrepaidLockConfig(this, prepayLockingPeriod
//                    );
//        }
//
//        this.allowForeclosureLockingPeriod = isForeclosureLockingPeriodAllowed == null ? false : isForeclosureLockingPeriodAllowed;
//
//        if (allowForeclosureLockingPeriod) {
//            this.foreclosureLoanConfig = new LoanProductForeclosureLockConfig(this, foreclosureLockingPeriod);
//        }

        this.loanProductRelatedDetail = new LoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod,
                interestPeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod,
                considerPartialPeriodInterest, repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, amortizationMethod,
                inArrearsTolerance, graceOnArrearsAgeing, daysInMonthType.getValue(), daysInYearType.getValue(),
                isInterestRecalculationEnabled, isEqualAmortization,brokenStrategy.getValue(),disbursementMode.getValue(),
                collectionMode.getValue(),brokenInterestDaysInYears.getValue(),brokenInterestDaysInMonth.getValue(),
                Objects.isNull(transactionTypePreference) ? null : transactionTypePreference.getValue());

        this.loanProductRelatedDetail.validateRepaymentPeriodWithGraceSettings();

        this.loanProductMinMaxConstraints = new LoanProductMinMaxConstraints(defaultMinPrincipal, defaultMaxPrincipal,
                defaultMinNominalInterestRatePerPeriod, defaultMaxNominalInterestRatePerPeriod, defaultMinNumberOfInstallments,
                defaultMaxNumberOfInstallments);

        if (accountingRuleType != null) {
            this.accountingRule = accountingRuleType.getValue();
        }
        this.includeInBorrowerCycle = includeInBorrowerCycle;
//        this.coBorrower=coBorrower;
//        this.eodBalance=eodBalance;
//        this.securedLoan=securedLoan;
//        this.nonEquatedInstallment=nonEquatedInstallment;
//
//        this.advanceEMI=advanceEMI;
//        this.termBasedOnLoanCycle=termBasedOnLoanCycle;
//        this.isNetOffApplied=isNetOffApplied;
        this.allowApprovalOverAmountApplied=allowApprovalOverAmountApplied;

        this.useDaysInMonthForLoanProvisioning=useDaysInMonthForLoanProvisioning;
        this.divideByThirtyForPartialPeriod=divideByThirtyForPartialPeriod;



        this.useBorrowerCycle = useBorrowerCycle;

        if (startDate != null) {
            this.startDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (closeDate != null) {
            this.closeDate = Date.from(closeDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        this.externalId = externalId;
        this.borrowerCycleVariations = loanProductBorrowerCycleVariations;
        for (LoanProductBorrowerCycleVariations borrowerCycleVariations : this.borrowerCycleVariations) {
            borrowerCycleVariations.updateLoanProduct(this);
        }
        if (loanProductConfigurableAttributes != null) {
            this.loanConfigurableAttributes = loanProductConfigurableAttributes;
            loanConfigurableAttributes.updateLoanProduct(this);
        }

        this.loanProducTrancheDetails = new LoanProductTrancheDetails(multiDisburseLoan, maxTrancheCount, outstandingLoanBalance);
        this.overdueDaysForNPA = overdueDaysForNPA;
        this.productInterestRecalculationDetails = productInterestRecalculationDetails;
        this.minimumDaysBetweenDisbursalAndFirstRepayment = minimumDaysBetweenDisbursalAndFirstRepayment;
        this.holdGuaranteeFunds = holdGuarantorFunds;
        this.loanProductGuaranteeDetails = loanProductGuaranteeDetails;
        this.principalThresholdForLastInstallment = principalThresholdForLastInstallment;
        this.accountMovesOutOfNPAOnlyOnArrearsCompletion = accountMovesOutOfNPAOnlyOnArrearsCompletion;
        this.canDefineInstallmentAmount = canDefineEmiAmount;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.syncExpectedWithDisbursementDate = syncExpectedWithDisbursementDate;
//        this.coBorrower=coBorrower;
//        this.eodBalance=eodBalance;
//        this.securedLoan=securedLoan;
//        this.nonEquatedInstallment=nonEquatedInstallment;
//
//        this.advanceEMI=advanceEMI;
//        this.termBasedOnLoanCycle=termBasedOnLoanCycle;
//        this.isNetOffApplied=isNetOffApplied;
        this.allowApprovalOverAmountApplied=allowApprovalOverAmountApplied;

        this.useDaysInMonthForLoanProvisioning=useDaysInMonthForLoanProvisioning;
        this.divideByThirtyForPartialPeriod=divideByThirtyForPartialPeriod;

        this.canUseForTopup = canUseForTopup;
        this.fixedPrincipalPercentagePerInstallment = fixedPrincipalPercentagePerInstallment;

        this.disallowExpectedDisbursements = disallowExpectedDisbursements;
        this.allowApprovedDisbursedAmountsOverApplied = allowApprovedDisbursedAmountsOverApplied;
        this.overAppliedCalculationType = overAppliedCalculationType;
        this.overAppliedNumber = overAppliedNumber;

        this.enableColendingLoan=enableColendingLoan;

        this.selectAcceptedDates=selectAcceptedDates;

        this.applyPrepaidLockingPeriod=applyPrepaidLockingPeriod;
        this.prepayLockingPeriod=prepayLockingPeriod;

        this.applyForeclosureLockingPeriod=applyPrepaidLockingPeriod;
        this.foreclosureLockingPeriod=prepayLockingPeriod;
        this.productAcceptedDates=productAcceptedDates;
        this.disbursementAccountNumber = disbursementAccountNumber;
        this.collectionAccountNumber = collectionAccountNumber;
        this.penalInvoice = penalInvoice;
        this.multipleDisbursement = multipleDisbursement;
        this.trancheClubbing = trancheClubbing;
        this.repaymentScheduleUpdateAllowed = repaymentScheduleUpdateAllowed;
        this.emiroundingMode = emiRoundingMode;
        this.isPennyDropEnabled = isPennyDropEnabled;
        this.isBankDisbursementEnabled = isBankDisbursementEnabled;
        this.servicerFeeInterestConfigEnabled = servicerFeeInterestConfigEnabled;
        this.servicerFeeChargesConfigEnabled = servicerFeeChargesConfigEnabled;
        this.dedupeEnabled = dedupeEnabled;
        this.dedupeType = dedupeType;
        this.disbursementBankAccount = disbursementAccount;
        this.emiMultiples = emiMultiples;
        this.interestDecimal = intererstDecimalMode;
        this.interestRoundingMode = interestRoundingMode;
        this.emiDecimalRegex = emiDecimalRegex;
        this.interestDecimalRegex = interestRegex;
        ProductCollectionConfig productCollectionConfig =  new ProductCollectionConfig();
        productCollectionConfig.setAdvanceAppropriationOn(advanceAppropriation);
        productCollectionConfig.setLoanProduct(this);
        productCollectionConfig.setAdvanceEntryEnabled(enableEntryForAdvanceTransaction);
        productCollectionConfig.setInterestBenefitEnabled(interestBenefitEnabled);
        productCollectionConfig.setForeclosureOnDueDateInterest(foreclosureOnDueDateInterest);
        productCollectionConfig.setForeclosureOnDueDateCharge(foreclosureOnDueDateCharge);
        productCollectionConfig.setForeclosureOtherThanDueDateInterest(foreclosureOtherThanDueDateInterest);
        productCollectionConfig.setForeclosureOtherThanDueDateCharge(foreclosureOtherThanDueDateCharge);
        productCollectionConfig.setForeclosureOneMonthOverdueInterest(foreclosureOneMonthOverdueInterest);
        productCollectionConfig.setForeclosureOneMonthOverdueCharge(foreclosureOneMonthOverdueCharge);
        productCollectionConfig.setForeclosureShortPaidInterest(foreclosureShortPaidInterest);
        productCollectionConfig.setForeclosureShortPaidInterestCharge(foreclosureShortPaidInterestCharge);
        productCollectionConfig.setForeclosurePrincipalShortPaidInterest(foreclosurePrincipalShortPaidInterest);
        productCollectionConfig.setForeclosurePrincipalShortPaidCharge(foreclosurePrincipalShortPaidCharge);
        productCollectionConfig.setForeclosureTwoMonthsOverdueInterest(foreclosureTwoMonthsOverdueInterest);
        productCollectionConfig.setForeclosureTwoMonthsOverdueCharge(foreclosureTwoMonthsOverdueCharge);
        productCollectionConfig.setForeclosurePosAdvanceOnDueDate(foreclosurePosAdvanceOnDueDate);
        productCollectionConfig.setForeclosureAdvanceOnDueDateInterest(foreclosureAdvanceOnDueDateInterest);
        productCollectionConfig.setForeclosureAdvanceOnDueDateCharge(foreclosureAdvanceOnDueDateCharge);
        productCollectionConfig.setForeclosurePosAdvanceOtherThanDueDate(foreclosurePosAdvanceOtherThanDueDate);
        productCollectionConfig.setForeclosureAdvanceAfterDueDateInterest(foreclosureAdvanceAfterDueDateInterest);
        productCollectionConfig.setForeclosureAdvanceOnDueDateCharge(foreclosureAdvanceOnDueDateCharge);
        productCollectionConfig.setForeclosureBackdatedShortPaidInterest(foreclosureBackdatedShortPaidInterest);
        productCollectionConfig.setForeclosureBackdatedShortPaidInterestCharge(foreclosureBackdatedShortPaidInterestCharge);
        productCollectionConfig.setForeclosureBackdatedFullyPaidInterest(foreclosureBackdatedFullyPaidInterest);
        productCollectionConfig.setForeclosureBackdatedFullyPaidInterestCharge(foreclosureBackdatedFullyPaidInterestCharge);
        productCollectionConfig.setForeclosureBackdatedShortPaidPrincipalInterest(foreclosureBackdatedShortPaidPrincipalInterest);
        productCollectionConfig.setForeclosureBackdatedShortPaidPrincipalCharge(foreclosureBackdatedShortPaidPrincipalCharge);
        productCollectionConfig.setForeclosureBackdatedFullyPaidEmiInterest(foreclosureBackdatedFullyPaidEmiInterest);
        productCollectionConfig.setForeclosureBackdatedFullyPaidEmiCharge(foreclosureBackdatedFullyPaidEmiCharge);
        productCollectionConfig.setForeclosureBackdatedAdvanceInterest(foreclosureBackdatedAdvanceInterest);
        productCollectionConfig.setForeclosureBackdatedAdvanceCharge(foreclosureBackdatedAdvanceCharge);
        productCollectionConfig.setAdvanceAppropriationAgainstOn(advanceAppropriationAgainstOn);
        productCollectionConfig.setForeclosureAdvanceAfterDueDateCharge(foreclosureAdvanceAfterDueDateCharge);
        productCollectionConfig.setForeclosureMethodType(foreclosureMethodType);
		productCollectionConfig.setCoolingOffApplicability(coolingOffApplicability);
        productCollectionConfig.setCoolingOffThresholdDays(coolingOffThresholdDays);
        productCollectionConfig.setCoolingOffInterestAndChargeApplicability(coolingOffInterestAndChargeApplicability);
        productCollectionConfig.setCoolingOffInterestLogicApplicability(coolingOffInterestLogicApplicability);
        productCollectionConfig.setCoolingOffDaysInYear(coolingOffDaysInYear);
        productCollectionConfig.setCoolingOffRoundingMode(coolingOffRoundingMode);
        productCollectionConfig.setCoolingOffRoundingDecimals(coolingOffRoundingDecimals);
        this.productCollectionConfig = productCollectionConfig;

        this.emiDaysInYear =emiDaysInYear;
        this.emiDaysInMonth = emiDaysInMonth;
        this.emiCalcLogic = emiCalcLogic;
        this.enableBackDatedDisbursement = enableBackDatedDisbursement;


        if (rates != null) {
            this.rates = rates;
        }
        validateLoanProductPreSave();
    }

    public void validateLoanProductPreSave() {

        if (this.disallowExpectedDisbursements) {
            if (!this.isMultiDisburseLoan()) {
                throw new LoanProductGeneralRuleException("allowMultipleDisbursals.not.set.disallowExpectedDisbursements.cant.be.set",
                        "Allow Multiple Disbursals Not Set - Disallow Expected Disbursals Can't Be Set");
            }
        }

        if (this.allowApprovedDisbursedAmountsOverApplied) {
            if (!this.disallowExpectedDisbursements) {
                throw new LoanProductGeneralRuleException(
                        "disallowExpectedDisbursements.not.set.allowApprovedDisbursedAmountsOverApplied.cant.be.set",
                        "Disallow Expected Disbursals Not Set - Allow Approved / Disbursed Amounts Over Applied Can't Be Set");
            }
        }

        if (this.overAppliedCalculationType == null || this.overAppliedCalculationType.isEmpty()) {
            if (this.allowApprovedDisbursedAmountsOverApplied) {
                throw new LoanProductGeneralRuleException(
                        "allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedCalculationType.is.mandatory",
                        "Allow Approved / Disbursed Amounts Over Applied is Set - Over Applied Calculation Type is Mandatory");
            }

        } else {
            if (!this.allowApprovedDisbursedAmountsOverApplied) {
                throw new LoanProductGeneralRuleException(
                        "allowApprovedDisbursedAmountsOverApplied.is.not.set.overAppliedCalculationType.cant.be.entered",
                        "Allow Approved / Disbursed Amounts Over Applied is Not Set - Over Applied Calculation Type Can't Be Entered");
            }

            List<String> overAppliedCalculationTypeAllowedValues = Arrays.asList("percentage", "flat");
            if (!overAppliedCalculationTypeAllowedValues.contains(this.overAppliedCalculationType)) {
                throw new LoanProductGeneralRuleException("overAppliedCalculationType.must.be.percentage.or.flat",
                        "Over Applied Calculation Type Must Be 'percentage' or 'flat'");
            }
        }

        if (this.overAppliedNumber != null) {
            if (!this.allowApprovedDisbursedAmountsOverApplied) {
                throw new LoanProductGeneralRuleException(
                        "allowApprovedDisbursedAmountsOverApplied.is.not.set.overAppliedNumber.cant.be.entered",
                        "Allow Approved / Disbursed Amounts Over Applied is Not Set - Over Applied Number Can't Be Entered");
            }
        } else {
            if (this.allowApprovedDisbursedAmountsOverApplied) {
                throw new LoanProductGeneralRuleException("allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedNumber.is.mandatory",
                        "Allow Approved / Disbursed Amounts Over Applied is Set - Over Applied Number is Mandatory");
            }
        }

    }

    public MonetaryCurrency getCurrency() {
        return this.loanProductRelatedDetail.getCurrency();
    }

    public void update(final Fund fund) {
        this.fund = fund;
    }

    public void update(final Partner partner) {
        this.partner = partner;
    }

    public RoundingMode getRoundingMode(){
        return RoundingMode.valueOf(emiroundingMode);
    }

    public void update(final LoanTransactionProcessingStrategy strategy) {
        this.transactionProcessingStrategy = strategy;
    }

    public LoanTransactionProcessingStrategy getRepaymentStrategy() {
        return this.transactionProcessingStrategy;
    }

    public boolean hasCurrencyCodeOf(final String currencyCode) {
        return this.loanProductRelatedDetail.hasCurrencyCodeOf(currencyCode);
    }

    public boolean update(final List<Charge> newProductCharges) {
        if (newProductCharges == null) {
            return false;
        }

        boolean updated = false;
        if (this.charges != null) {
            final Set<Charge> currentSetOfCharges = new HashSet<>(this.charges);
            final Set<Charge> newSetOfCharges = new HashSet<>(newProductCharges);

            if (!currentSetOfCharges.equals(newSetOfCharges)) {
                updated = true;
                this.charges = newProductCharges;
            }
        } else {
            updated = true;
            this.charges = newProductCharges;
        }
        return updated;
    }

    public boolean updateRates(final List<Rate> newProductRates) {
        if (newProductRates == null) {
            return false;
        }

        boolean updated = false;
        if (this.rates != null) {
            final Set<Rate> currentSetOfCharges = new HashSet<>(this.rates);
            final Set<Rate> newSetOfCharges = new HashSet<>(newProductRates);

            if (!currentSetOfCharges.equals(newSetOfCharges)) {
                updated = true;
                this.rates = newProductRates;
            }
        } else {
            updated = true;
            this.rates = newProductRates;
        }
        return updated;
    }

    public Integer getAccountingType() {
        return this.accountingRule;
    }

    public List<Charge> getLoanProductCharges() {
        return this.charges;
    }

    public void update(final LoanProductConfigurableAttributes loanConfigurableAttributes) {
        this.loanConfigurableAttributes = loanConfigurableAttributes;
    }

    public LoanProductConfigurableAttributes getLoanProductConfigurableAttributes() {
        return this.loanConfigurableAttributes;
    }

    public Map<String, Object> update(final JsonCommand command, final AprCalculator aprCalculator, FloatingRate floatingRate,List<LoanProductFeeData> loanProductChargeData) {

        final Map<String, Object> actualChanges = this.loanProductRelatedDetail.update(command, aprCalculator);
        actualChanges.putAll(loanProductMinMaxConstraints().update(command));

        final String isLinkedToFloatingInterestRates = "isLinkedToFloatingInterestRates";
        if (command.isChangeInBooleanParameterNamed(isLinkedToFloatingInterestRates, this.isLinkedToFloatingInterestRate)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isLinkedToFloatingInterestRates);
            actualChanges.put(isLinkedToFloatingInterestRates, newValue);
            this.isLinkedToFloatingInterestRate = newValue;
        }
        if (Objects.nonNull(loanProductChargeData)){
            List<LoanProductFeeData> existingLoanProductFeeCharges = new ArrayList<>();
            for(LoanProductFeesCharges existingCharge:this.loanProductFeesCharges){
            for(LoanProductFeeData updatedCharge : loanProductChargeData){
                if(existingCharge.getCharge().equals(updatedCharge.getCharge())){
                        existingCharge.setSelfShare(updatedCharge.getSelfShare());
                        existingCharge.setPartnerShare(updatedCharge.getPartnerShare());
                    existingLoanProductFeeCharges.add(updatedCharge);
                        break;
                }
            }
            }
            loanProductChargeData.removeAll(existingLoanProductFeeCharges);
            LoanProductFeesCharges loanProductFeesCharges=new LoanProductFeesCharges();
            Set<LoanProductFeesCharges> newLoanProductFeeCharges = loanProductFeesCharges.loanCharges(this,loanProductChargeData);
            if(Objects.nonNull(newLoanProductFeeCharges)) {
                this.loanProductFeesCharges.addAll(newLoanProductFeeCharges);
            }
        }

        if (this.isLinkedToFloatingInterestRate) {
            actualChanges.putAll(loanProductFloatingRates().update(command, floatingRate));
            this.loanProductRelatedDetail.updateForFloatingInterestRates();
            this.loanProductMinMaxConstraints.updateForFloatingInterestRates();
        } else {
            this.floatingRates = null;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName,
                this.allowVariabeInstallments)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName);
            actualChanges.put(LoanProductConstants.allowVariableInstallmentsParamName, newValue);
            this.allowVariabeInstallments = newValue;
        }

        if (this.allowVariabeInstallments) {
            actualChanges.putAll(loanProductVariableInstallmentConfig().update(command));
        } else {
            this.variableInstallmentConfig = null;
        }


        if (command.isChangeInLongParameterNamed(LoanProductConstants.ASSETCLASS, assetClassId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.ASSETCLASS);
            actualChanges.put(LoanProductConstants.ASSETCLASS, newValue);
        }
        if (command.isChangeInLongParameterNamed(LoanProductConstants.LOANPRODUCTCLASS, loanProductClassId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.LOANPRODUCTCLASS);
            actualChanges.put(LoanProductConstants.LOANPRODUCTCLASS, newValue);
        }

        if (command.isChangeInLongParameterNamed(LoanProductConstants.LOANPRODUCTTYPE, loanProductTypeId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.LOANPRODUCTTYPE);
            actualChanges.put(LoanProductConstants.LOANPRODUCTTYPE, newValue);
        }

        if (command.isChangeInLongParameterNamed(LoanProductConstants.INSURANCEAPPLICABILITY, insuranceApplicabilityId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.INSURANCEAPPLICABILITY);
            actualChanges.put(LoanProductConstants.INSURANCEAPPLICABILITY, newValue);
        }

        if (command.isChangeInLongParameterNamed(LoanProductConstants.FLDGLOGIC, fldgLogicId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.FLDGLOGIC);
            actualChanges.put(LoanProductConstants.FLDGLOGIC, newValue);
        }


//        if (command.isChangeInLongParameterNamed(LoanProductConstants.DISBURSEMENT, disbursementId())) {
//            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.DISBURSEMENT);
//            actualChanges.put(LoanProductConstants.DISBURSEMENT, newValue);
//        }
//        if (command.isChangeInLongParameterNamed(LoanProductConstants.COLLECTION, collectionId())) {
//            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.COLLECTION);
//            actualChanges.put(LoanProductConstants.COLLECTION, newValue);
//        }
        if (command.isChangeInLongParameterNamed(LoanProductConstants.FRAMEWORK, frameWorkId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.FRAMEWORK);
            actualChanges.put(LoanProductConstants.FRAMEWORK, newValue);
        }
        if (command.isChangeInLongParameterNamed(LoanProductConstants.LOANTYPE, loanTypeId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.LOANTYPE);
            actualChanges.put(LoanProductConstants.LOANTYPE, newValue);
        }
//
//        if (this.allowAgeLimits) {
//            actualChanges.putAll(loanProductAgeLimitsConfig().update(command));
//        } else {
//            this.ageLimitsConfig = null;
//        }

//        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.allowPrepaidLockingPeriodParamName,
//                this.allowPrepaidLockingPeriod)) {
//            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowPrepaidLockingPeriodParamName);
//            actualChanges.put(LoanProductConstants.allowPrepaidLockingPeriodParamName, newValue);
//            this.allowPrepaidLockingPeriod = newValue;
//        }
//
//        if (this.allowPrepaidLockingPeriod) {
//            actualChanges.putAll(loanProductPrepaidLockConfig().update(command));
//        } else {
//            this.prepaidLoanConfig = null;
//        }
//
//        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.allowForeclosureLockingPeriodParamName,
//                this.allowForeclosureLockingPeriod)) {
//            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowForeclosureLockingPeriodParamName);
//            actualChanges.put(LoanProductConstants.allowForeclosureLockingPeriodParamName, newValue);
//            this.allowForeclosureLockingPeriod = newValue;
//        }
//
//        if (this.allowForeclosureLockingPeriod) {
//            actualChanges.putAll(loanProductForeclosureLockConfig().update(command));
//        } else {
//            this.ageLimitsConfig = null;
//        }


        final String accountingTypeParamName = "accountingRule";
        if (command.isChangeInIntegerParameterNamed(accountingTypeParamName, this.accountingRule)) {
            final Integer newValue = command.integerValueOfParameterNamed(accountingTypeParamName);
            actualChanges.put(accountingTypeParamName, newValue);
            this.accountingRule = newValue;
        }

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        if(command.isChangeInStringParameterNamed(LoanProductConstants.EMIROUNDINGMODE,this.emiroundingMode)){
            final String newValue = command.stringValueOfParameterNamed(LoanProductConstants.EMIROUNDINGMODE);
            actualChanges.put(LoanProductConstants.EMIROUNDINGMODE, newValue);
            this.emiroundingMode = newValue;
        }

        final String shortNameParamName = LoanProductConstants.SHORT_NAME;
        if (command.isChangeInStringParameterNamed(shortNameParamName, this.shortName)) {
            final String newValue = command.stringValueOfParameterNamed(shortNameParamName);
            actualChanges.put(shortNameParamName, newValue);
            this.shortName = newValue;
        }

        final String loanAccNoPreferenceParamName = LoanProductConstants.LOAN_ACC_NO_PREFERENCE;
        if (command.isChangeInStringParameterNamed(loanAccNoPreferenceParamName, this.loanAccNoPreference)) {
            final String newValue = command.stringValueOfParameterNamed(loanAccNoPreferenceParamName);
            actualChanges.put(loanAccNoPreferenceParamName, newValue);
            this.loanAccNoPreference = newValue;
        }

        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        final String brokenInterestCalculationPeriodParamName = "brokenInterestCalculationPeriod";
        if (command.isChangeInStringParameterNamed(brokenInterestCalculationPeriodParamName, this.brokenInterestCalculationPeriod)) {
            final String newValue = command.stringValueOfParameterNamed(brokenInterestCalculationPeriodParamName);
            actualChanges.put(brokenInterestCalculationPeriodParamName, newValue);
            this.brokenInterestCalculationPeriod = newValue;
        }

//        final String repaymentStrategyForNpaIdParamName = "repaymentStrategyForNpaId";
//        if (command.isChangeInStringParameterNamed(repaymentStrategyForNpaIdParamName, this.repaymentStrategyForNpaId)) {
//            final String newValue = command.stringValueOfParameterNamed(repaymentStrategyForNpaIdParamName);
//            actualChanges.put(repaymentStrategyForNpaIdParamName, newValue);
//            this.repaymentStrategyForNpaId = newValue;
//        }

        final String loanForeclosureStrategyParamName = "loanForeclosureStrategy";
        if (command.isChangeInStringParameterNamed(loanForeclosureStrategyParamName, this.loanForeclosureStrategy)) {
            final String newValue = command.stringValueOfParameterNamed(loanForeclosureStrategyParamName);
            actualChanges.put(loanForeclosureStrategyParamName, newValue);
            this.loanForeclosureStrategy = newValue;
        }

//        final String brokenInterestDaysInYearsParamName = "brokenInterestDaysInYears";
//        if (command.isChangeInStringParameterNamed(brokenInterestDaysInYearsParamName, this.brokenInterestDaysInYears) {
//            final String newValue = command.stringValueOfParameterNamed(brokenInterestDaysInYearsParamName);
//            actualChanges.put(brokenInterestDaysInYearsParamName, newValue);
//            this.brokenInterestDaysInYears = Integer.valueOf(newValue);
//        }
//
//        final String brokenInterestDaysInMonthParamName = "brokenInterestDaysInMonth";
//        if (command.isChangeInStringParameterNamed(brokenInterestDaysInMonthParamName, String.valueOf(this.brokenInterestDaysInMonth))) {
//            final String newValue = command.stringValueOfParameterNamed(brokenInterestDaysInMonthParamName);
//            actualChanges.put(brokenInterestDaysInMonthParamName, newValue);
//            this.brokenInterestDaysInMonth = Integer.valueOf(newValue);
//        }

//        final String brokenIntrestStrategyParamName = "brokenInterestStrategy";
//        if (command.isChangeInStringParameterNamed(brokenIntrestStrategyParamName, this.brokenInterestStrategy)) {
//            final String newValue = command.stringValueOfParameterNamed(brokenIntrestStrategyParamName);
//            actualChanges.put(brokenIntrestStrategyParamName, newValue);
//            this.brokenInterestStrategy = newValue;
//        }
//        final String brokenInterestDaysInYearsParamName ="brokenInterestDaysInYears";
//        if (command.isChangeInStringParameterNamed(brokenIntrestStrategyParamName,this.brokenInterestDaysInYears)) {
//            final String newValue = command.stringValueOfParameterNamed(brokenInterestDaysInYearsParamName);
//            actualChanges.put(brokenInterestDaysInYearsParamName, newValue);
//            this.brokenInterestDaysInYears = newValue;
//        }
//
//
//        final String brokenInterestDaysInMonthParamName ="brokenInterestDaysInMonth";
//        if (command.isChangeInStringParameterNamed(brokenIntrestStrategyParamName,this.brokenInterestDaysInMonth)) {
//            final String newValue = command.stringValueOfParameterNamed(brokenInterestDaysInMonthParamName);
//            actualChanges.put(brokenInterestDaysInMonthParamName, newValue);
//            this.brokenInterestDaysInMonth = newValue;
//        }




        Long existingFundId = null;
        if (this.fund != null) {
            existingFundId = this.fund.getId();
        }
        final String fundIdParamName = "fundId";
        if (command.isChangeInLongParameterNamed(fundIdParamName, existingFundId)) {
            final Long newValue = command.longValueOfParameterNamed(fundIdParamName);
            actualChanges.put(fundIdParamName, newValue);
        }

        Long existingLoanProductClassId = null;
        if (this.loanProductClass != null) {
            existingLoanProductClassId = this.loanProductClass.getId();
        }
        final String loanProductClassParamName = "loanProductClassId";
        if (command.isChangeInLongParameterNamed(loanProductClassParamName, existingLoanProductClassId)) {
            final Long newValue = command.longValueOfParameterNamed(loanProductClassParamName);
            actualChanges.put(loanProductClassParamName, newValue);
        }

//        Long existingClassId = null;
//        if (this.loanProductClass != null) {
//            existingClassId = this.loanProductClass.getId();
//        }
//        final String classIdParamName = "classId";
//        if (command.isChangeInLongParameterNamed(classIdParamName, existingClassId)) {
//            final Long newValue = command.longValueOfParameterNamed(classIdParamName);
//            actualChanges.put(classIdParamName, newValue);
//        }

//        Long existingTypeId = null;
//        if (this.loanProductType != null) {
//            existingTypeId = this.loanProductType.getId();
//        }
//        final String typeIdParamName = "typeId";
//        if (command.isChangeInLongParameterNamed(typeIdParamName, existingTypeId)) {
//            final Long newValue = command.longValueOfParameterNamed(typeIdParamName);
//            actualChanges.put(typeIdParamName, newValue);
//        }

        Long existingPartnerId = null;
        if (this.partner != null) {
            existingPartnerId = this.partner.getId();
        }
        final String partnerIdParamName = "partnerId";
        if (command.isChangeInLongParameterNamed(partnerIdParamName, existingPartnerId)) {
            final Long newValue = command.longValueOfParameterNamed(partnerIdParamName);
            actualChanges.put(partnerIdParamName, newValue);
        }

        Long existingStrategyId = null;
        if (this.transactionProcessingStrategy != null) {
            existingStrategyId = this.transactionProcessingStrategy.getId();
        }
        final String transactionProcessingStrategyParamName = "transactionProcessingStrategyId";
        if (command.isChangeInLongParameterNamed(transactionProcessingStrategyParamName, existingStrategyId)) {
            final Long newValue = command.longValueOfParameterNamed(transactionProcessingStrategyParamName);
            actualChanges.put(transactionProcessingStrategyParamName, newValue);
        }

        final String chargesParamName = "charges";
        if (command.hasParameter(chargesParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(chargesParamName);
            if (jsonArray != null) {
                actualChanges.put(chargesParamName, command.jsonFragment(chargesParamName));
            }
        }

        final String includeInBorrowerCycleParamName = "includeInBorrowerCycle";
        if (command.isChangeInBooleanParameterNamed(includeInBorrowerCycleParamName, this.includeInBorrowerCycle)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(includeInBorrowerCycleParamName);
            actualChanges.put(includeInBorrowerCycleParamName, newValue);
            this.includeInBorrowerCycle = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, this.useBorrowerCycle)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, newValue);
            this.useBorrowerCycle = newValue;
        }

        if (this.useBorrowerCycle) {
            actualChanges.putAll(updateBorrowerCycleVariations(command));
        } else {
            clearVariations(null, true);
        }
        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String localeParamName = "locale";
        final String dateFormatParamName = "dateFormat";

        final String startDateParamName = "startDate";
        if (command.isChangeInLocalDateParameterNamed(startDateParamName, getStartDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(startDateParamName);
            actualChanges.put(startDateParamName, valueAsInput);
            actualChanges.put(dateFormatParamName, dateFormatAsInput);
            actualChanges.put(localeParamName, localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
            if (newValue != null) {
                this.startDate = Date.from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } else {
                this.startDate = null;
            }
        }

        final String closeDateParamName = "closeDate";
        if (command.isChangeInLocalDateParameterNamed(closeDateParamName, getCloseDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(closeDateParamName);
            actualChanges.put(closeDateParamName, valueAsInput);
            actualChanges.put(dateFormatParamName, dateFormatAsInput);
            actualChanges.put(localeParamName, localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(closeDateParamName);
            if (newValue != null) {
                this.closeDate = Date.from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } else {
                this.closeDate = null;
            }
        }

        final String externalIdTypeParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdTypeParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdTypeParamName);
            actualChanges.put(accountingTypeParamName, newValue);
            this.externalId = newValue;
        }
        loanProducTrancheDetails.update(command, actualChanges, localeAsInput);

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, this.overdueDaysForNPA)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, newValue);
            actualChanges.put("locale", localeAsInput);
            this.overdueDaysForNPA = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT,
                this.minimumDaysBetweenDisbursalAndFirstRepayment)) {
            final Integer newValue = command
                    .integerValueOfParameterNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT);
            actualChanges.put(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minimumDaysBetweenDisbursalAndFirstRepayment = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("syncExpectedWithDisbursementDate", this.syncExpectedWithDisbursementDate)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("syncExpectedWithDisbursementDate");
            actualChanges.put("syncExpectedWithDisbursementDate", newValue);
            this.syncExpectedWithDisbursementDate = newValue;
        }
//
//        if (command.isChangeInBooleanParameterNamed("coBorrower", this.coBorrower)) {
//            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("coBorrower");
//            actualChanges.put("coBorrower", newValue);
//            this.coBorrower = newValue;
//        }
//
//        if (command.isChangeInBooleanParameterNamed("eodBalance", this.eodBalance)) {
//            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("eodBalance");
//            actualChanges.put("eodBalance", newValue);
//            this.eodBalance = newValue;
//        }
//
//        if (command.isChangeInBooleanParameterNamed("securedLoan", this.securedLoan)) {
//            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("securedLoan");
//            actualChanges.put("securedLoan", newValue);
//            this.securedLoan = newValue;
//        }
//
//        if (command.isChangeInBooleanParameterNamed("nonEquatedInstallment", this.nonEquatedInstallment)) {
//            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("nonEquatedInstallment");
//            actualChanges.put("nonEquatedInstallment", newValue);
//            this.nonEquatedInstallment = newValue;
//        }
//
//
//        if (command.isChangeInBooleanParameterNamed("advanceEMI", this.advanceEMI)) {
//            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("advanceEMI");
//            actualChanges.put("advanceEMI", newValue);
//            this.advanceEMI = newValue;
//        }
//
//        if (command.isChangeInBooleanParameterNamed("termBasedOnLoanCycle", this.termBasedOnLoanCycle)) {
//            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("termBasedOnLoanCycle");
//            actualChanges.put("termBasedOnLoanCycle", newValue);
//            this.termBasedOnLoanCycle = newValue;
//        }
//
//        if (command.isChangeInBooleanParameterNamed("isNetOffApplied", this.isNetOffApplied)) {
//            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("isNetOffApplied");
//            actualChanges.put("isNetOffApplied", newValue);
//            this.isNetOffApplied = newValue;
//        }

        if (command.isChangeInBooleanParameterNamed("allowApprovalOverAmountApplied", this.allowApprovalOverAmountApplied)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("allowApprovalOverAmountApplied");
            actualChanges.put("allowApprovalOverAmountApplied", newValue);
            this.allowApprovalOverAmountApplied = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("useDaysInMonthForLoanProvisioning", this.useDaysInMonthForLoanProvisioning)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("useDaysInMonthForLoanProvisioning");
            actualChanges.put("useDaysInMonthForLoanProvisioning", newValue);
            this.useDaysInMonthForLoanProvisioning = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("divideByThirtyForPartialPeriod", this.divideByThirtyForPartialPeriod)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("divideByThirtyForPartialPeriod");
            actualChanges.put("divideByThirtyForPartialPeriod", newValue);
            this.divideByThirtyForPartialPeriod = newValue;
        }


        if (command.isChangeInBooleanParameterNamed("enableColendingLoan", this.enableColendingLoan)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("enableColendingLoan");
            actualChanges.put("enableColendingLoan", newValue);
            this.enableColendingLoan = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("byPercentageSplit", this.byPercentageSplit)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("byPercentageSplit");
            actualChanges.put("byPercentageSplit", newValue);
            this.byPercentageSplit = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("principalShare", this.principalShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("principalShare");
            actualChanges.put("principalShare", newValue);
            this.principalShare = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("feeShare", this.feeShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("feeShare");
            actualChanges.put("feeShare", newValue);
            this.feeShare = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("penaltyShare", this.penaltyShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("penaltyShare");
            actualChanges.put("penaltyShare", newValue);
            this.penaltyShare = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("overpaidShare", this.overpaidShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("overpaidShare");
            actualChanges.put("overpaidShare", newValue);
            this.overpaidShare = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed("interestRate", this.interestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("interestRate");
            actualChanges.put("interestRate", newValue);
            this.interestRate = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("selfPrincipalShare", this.selfPrincipalShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("selfPrincipalShare");
            actualChanges.put("selfPrincipalShare", newValue);
            this.selfPrincipalShare = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("selfFeeShare", this.selfFeeShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("selfFeeShare");
            actualChanges.put("selfFeeShare", newValue);
            this.selfFeeShare = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("selfPenaltyShare", this.selfPenaltyShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("selfPenaltyShare");
            actualChanges.put("selfPenaltyShare", newValue);
            this.selfPenaltyShare = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("selfOverpaidShares", this.selfOverpaidShares)) {
            final Integer newValue = command.integerValueOfParameterNamed("selfOverpaidShares");
            actualChanges.put("selfOverpaidShares", newValue);
            this.selfOverpaidShares = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed("selfInterestRate", this.selfInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("selfInterestRate");
            actualChanges.put("selfInterestRate", newValue);
            this.selfInterestRate = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("partnerPrincipalShare", this.partnerPrincipalShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("partnerPrincipalShare");
            actualChanges.put("partnerPrincipalShare", newValue);
            this.partnerPrincipalShare = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("partnerFeeShare", this.partnerFeeShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("partnerFeeShare");
            actualChanges.put("partnerFeeShare", newValue);
            this.partnerFeeShare = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("partnerPenaltyShare", this.partnerPenaltyShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("partnerPenaltyShare");
            actualChanges.put("partnerPenaltyShare", newValue);
            this.partnerPenaltyShare = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("partnerOverpaidShare", this.partnerOverpaidShare)) {
            final Integer newValue = command.integerValueOfParameterNamed("partnerOverpaidShare");
            actualChanges.put("partnerOverpaidShare", newValue);
            this.partnerOverpaidShare = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed("partnerInterestRate", this.partnerInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("partnerInterestRate");
            actualChanges.put("partnerInterestRate", newValue);
            this.partnerInterestRate = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("enableFeesWiseBifacation", this.enableFeesWiseBifacation)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("enableFeesWiseBifacation");
            actualChanges.put("enableFeesWiseBifacation", newValue);
            this.enableFeesWiseBifacation = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("enableChargeWiseBifacation", this.enableChargeWiseBifacation)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("enableChargeWiseBifacation");
            actualChanges.put("enableChargeWiseBifacation", newValue);
            this.enableChargeWiseBifacation = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("enableOverDue", this.enableOverdue)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("enableOverDue");
            actualChanges.put("enableOverDue", newValue);
            this.enableOverdue = newValue;
        }

        if (command.isChangeInStringParameterNamed("selectCharge", this.selectCharge)) {
            final String newValue = command.stringValueOfParameterNamed("selectCharge");
            actualChanges.put("selectCharge", newValue);
            this.selectCharge = newValue;
        }


        if (command.isChangeInBooleanParameterNamed("selectAcceptedDates", this.selectAcceptedDates)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("selectAcceptedDates");
            actualChanges.put("selectAcceptedDates", newValue);
            this.selectAcceptedDates = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("applyPrepaidLockingPeriod", this.applyPrepaidLockingPeriod)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("applyPrepaidLockingPeriod");
            actualChanges.put("applyPrepaidLockingPeriod", newValue);
            this.applyPrepaidLockingPeriod = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("prepayLockingPeriod", this.prepayLockingPeriod)) {
            final Integer newValue = command.integerValueOfParameterNamed("prepayLockingPeriod");
            actualChanges.put("prepayLockingPeriod", newValue);
            this.prepayLockingPeriod = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("applyForeclosureLockingPeriod", this.applyForeclosureLockingPeriod)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("applyForeclosureLockingPeriod");
            actualChanges.put("applyForeclosureLockingPeriod", newValue);
            this.applyForeclosureLockingPeriod = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("foreclosureLockingPeriod", this.foreclosureLockingPeriod)) {
            final Integer newValue = command.integerValueOfParameterNamed("foreclosureLockingPeriod");
            actualChanges.put("foreclosureLockingPeriod", newValue);
            this.foreclosureLockingPeriod = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed("aumSlabRate", this.aumSlabRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("aumSlabRate");
            actualChanges.put("aumSlabRate", newValue);
            this.aumSlabRate = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed("gstLiabilityByVcpl", this.gstLiabilityByVcpl)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("gstLiabilityByVcpl");
            actualChanges.put("gstLiabilityByVcpl", newValue);
            this.gstLiabilityByVcpl = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed("gstLiabilityByPartner", this.gstLiabilityByPartner)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("gstLiabilityByPartner");
            actualChanges.put("gstLiabilityByPartner", newValue);
            this.gstLiabilityByPartner = newValue;
        }

//        if (command.isChangeInBigDecimalParameterNamed("vcplHurdleRate", this.vcplHurdleRate)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("vcplHurdleRate");
//            actualChanges.put("vcplHurdleRate", newValue);
//            this.vcplHurdleRate = newValue;
//        }



//        if (command.isChangeInBigDecimalParameterNamed("vcplShareInPf", this.vcplShareInPf)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("vcplShareInPf");
//            actualChanges.put("vcplShareInPf", newValue);
//            this.vcplShareInPf = newValue;
//        }
//
//        if (command.isChangeInBigDecimalParameterNamed("partnerShareInPf", this.partnerShareInPf)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("partnerShareInPf");
//            actualChanges.put("partnerShareInPf", newValue);
//            this.partnerShareInPf = newValue;
//        }

//        if (command.isChangeInBigDecimalParameterNamed("vcplShareInPenalInterest", this.vcplShareInPenalInterest)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("vcplShareInPenalInterest");
//            actualChanges.put("vcplShareInPenalInterest", newValue);
//            this.vcplShareInPenalInterest = newValue;
//        }
//
//        if (command.isChangeInBigDecimalParameterNamed("partnerShareInPenalInterest", this.partnerShareInPenalInterest)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("partnerShareInPenalInterest");
//            actualChanges.put("partnerShareInPenalInterest", newValue);
//            this.partnerShareInPenalInterest = newValue;
//        }

        if (command.isChangeInBigDecimalParameterNamed("vcplShareInBrokenInterest", this.vcplShareInBrokenInterest)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("vcplShareInBrokenInterest");
            actualChanges.put("vcplShareInBrokenInterest", newValue);
            this.vcplShareInBrokenInterest = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed("partnerShareInBrokenInterest", this.partnerShareInBrokenInterest)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("partnerShareInBrokenInterest");
            actualChanges.put("partnerShareInBrokenInterest", newValue);
            this.partnerShareInBrokenInterest = newValue;
        }

//        if (command.isChangeInBigDecimalParameterNamed("vcplShareInForeclosureCharges", this.vcplShareInForeclosureCharges)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("vcplShareInForeclosureCharges");
//            actualChanges.put("vcplShareInForeclosureCharges", newValue);
//            this.vcplShareInForeclosureCharges = newValue;
//        }
//
//        if (command.isChangeInBigDecimalParameterNamed("partnerShareInForeclosureCharges", this.partnerShareInForeclosureCharges)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("partnerShareInForeclosureCharges");
//            actualChanges.put("partnerShareInForeclosureCharges", newValue);
//            this.partnerShareInForeclosureCharges = newValue;
//        }
//
//        if (command.isChangeInBigDecimalParameterNamed("vcplShareInOtherCharges", this.vcplShareInOtherCharges)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("vcplShareInOtherCharges");
//            actualChanges.put("vcplShareInOtherCharges", newValue);
//            this.vcplShareInOtherCharges = newValue;
//        }
//
//        if (command.isChangeInBigDecimalParameterNamed("partnerShareInOtherCharges", this.partnerShareInOtherCharges)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("partnerShareInOtherCharges");
//            actualChanges.put("partnerShareInOtherCharges", newValue);
//            this.partnerShareInOtherCharges = newValue;
//        }


        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.EMIDECIMALREGEX, this.emiDecimalRegex)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.EMIDECIMALREGEX);
            actualChanges.put(LoanProductConstants.EMIDECIMALREGEX, newValue);
            this.emiDecimalRegex = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.INTERESTDECIMALREGEX, this.interestDecimalRegex)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.INTERESTDECIMALREGEX);
            actualChanges.put(LoanProductConstants.INTERESTDECIMALREGEX, newValue);
            this.interestDecimalRegex = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.INTERESTDECIMAL, this.interestDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.INTERESTDECIMAL);
            actualChanges.put(LoanProductConstants.INTERESTDECIMAL, newValue);
            this.interestDecimal = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.EMIMULTIPLES, this.emiMultiples)) {
            final Integer newValue = command.integerValueOfParameterNamed("emimultiples");
            actualChanges.put("emimultiples", newValue);
            this.emiMultiples = newValue;
        }

        if (command.isChangeInStringParameterNamed("interestRoundingMode", this.interestRoundingMode)) {
            final String newValue = command.stringValueOfParameterNamed("interestRoundingMode");
            actualChanges.put("interestRoundingMode", newValue);
            this.interestRoundingMode = newValue;
        }


        if (command.isChangeInIntegerParameterNamed("monitoringTriggerPar30", this.monitoringTriggerPar30)) {
            final Integer newValue = command.integerValueOfParameterNamed("monitoringTriggerPar30");
            actualChanges.put("monitoringTriggerPar30", newValue);
            this.monitoringTriggerPar30 = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("monitoringTriggerPar90", this.monitoringTriggerPar90)) {
            final Integer newValue = command.integerValueOfParameterNamed("monitoringTriggerPar90");
            actualChanges.put("monitoringTriggerPar90", newValue);
            this.monitoringTriggerPar90 = newValue;
        }

        if (command.isChangeInLongParameterNamed("disbursementAccountNumber", this.disbursementAccountNumber)) {
            final Long newValue = command.longValueOfParameterNamed("disbursementAccountNumber");
            actualChanges.put("disbursementAccountNumber", newValue);
            this.disbursementAccountNumber = newValue;
        }

        if (command.isChangeInLongParameterNamed("collectionAccountNumber", this.collectionAccountNumber)) {
            final Long newValue = command.longValueOfParameterNamed("collectionAccountNumber");
            actualChanges.put("collectionAccountNumber", newValue);
            this.collectionAccountNumber = newValue;
        }

        if (command.isChangeInLongParameterNamed(LoanProductConstants.PENALINVOICE, penalInvoiceId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.PENALINVOICE);
            actualChanges.put(LoanProductConstants.PENALINVOICE, newValue);
        }

        if (command.isChangeInLongParameterNamed(LoanProductConstants.MULTIPLEDISBURSEMENT, multiDisbursementId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.MULTIPLEDISBURSEMENT);
            actualChanges.put(LoanProductConstants.MULTIPLEDISBURSEMENT, newValue);
        }

        if (command.isChangeInLongParameterNamed(LoanProductConstants.TRANCHECLUBBING, trancheClubbingId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.TRANCHECLUBBING);
            actualChanges.put(LoanProductConstants.TRANCHECLUBBING, newValue);
        }

        if (command.isChangeInLongParameterNamed(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED, repaymentScheduleUpdateAllowedId())) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED);
            actualChanges.put(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED, newValue);
        }
        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.PMT_FORMULA_CALCULATION, this.emiCalcLogic)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.PMT_FORMULA_CALCULATION);
            actualChanges.put(LoanProductConstants.PMT_FORMULA_CALCULATION, newValue);
            this.emiCalcLogic = newValue;
        }
        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.PMT_DAYS_IN_MONTH_TYPE, this.emiDaysInMonth)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.PMT_DAYS_IN_MONTH_TYPE);
            actualChanges.put(LoanProductConstants.PMT_DAYS_IN_MONTH_TYPE, newValue);
            this.emiDaysInMonth = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.PMT_DAYS_IN_YEAR_TYPE, this.emiDaysInYear)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.PMT_DAYS_IN_YEAR_TYPE);
            actualChanges.put(LoanProductConstants.PMT_DAYS_IN_YEAR_TYPE, newValue);
            this.emiDaysInYear = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT, this.enableBackDatedDisbursement)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT);
            actualChanges.put(LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT, newValue);
            this.enableBackDatedDisbursement = newValue;
        }
        /**
         * Update interest recalculation settings
         */
        final boolean isInterestRecalculationEnabledChanged = actualChanges
                .containsKey(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);

        if (isInterestRecalculationEnabledChanged) {
            if (this.isInterestRecalculationEnabled()) {
                this.productInterestRecalculationDetails = LoanProductInterestRecalculationDetails.createFrom(command);
                this.productInterestRecalculationDetails.updateProduct(this);
                actualChanges.put(LoanProductConstants.interestRecalculationCompoundingMethodParameterName,
                        command.integerValueOfParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName));
                actualChanges.put(LoanProductConstants.rescheduleStrategyMethodParameterName,
                        command.integerValueOfParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName));
            } else {
                this.productInterestRecalculationDetails = null;
            }
        }

        if (!isInterestRecalculationEnabledChanged && this.isInterestRecalculationEnabled()) {
            this.productInterestRecalculationDetails.update(command, actualChanges, localeAsInput);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.holdGuaranteeFundsParamName, this.holdGuaranteeFunds)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.holdGuaranteeFundsParamName);
            actualChanges.put(LoanProductConstants.holdGuaranteeFundsParamName, newValue);
            this.holdGuaranteeFunds = newValue;
        }

        final String configurableAttributesChanges = LoanProductConstants.allowAttributeOverridesParamName;
        if (command.hasParameter(configurableAttributesChanges)) {
            if (!command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                    .isJsonNull()) {
                actualChanges.put(configurableAttributesChanges, command.jsonFragment(configurableAttributesChanges));

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.amortizationTypeParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getAmortizationBoolean()) {
                    this.loanConfigurableAttributes.setAmortizationType(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.amortizationTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.interestTypeParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getInterestMethodBoolean()) {
                    this.loanConfigurableAttributes.setInterestType(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.interestTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.transactionProcessingStrategyIdParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getTransactionProcessingStrategyBoolean()) {
                    this.loanConfigurableAttributes.setTransactionProcessingStrategyId(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.transactionProcessingStrategyIdParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.interestCalculationPeriodTypeParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getInterestCalcPeriodBoolean()) {
                    this.loanConfigurableAttributes.setInterestCalculationPeriodType(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.interestCalculationPeriodTypeParamName).getAsBoolean());
                }

//                if (command.isChangeInBooleanParameterNamed("repaymentStrategyForNpa", this.repaymentStrategyForNpa)) {
//                    final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("repaymentStrategyForNpa");
//                    actualChanges.put("repaymentStrategyForNpa", newValue);
//                    this.repaymentStrategyForNpa = newValue;
//                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.repaymentStrategyForNpaParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getRepaymentStrategyForNpa()) {
                    this.loanConfigurableAttributes.setRepaymentStrategyForNpa(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.repaymentStrategyForNpaParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.inArrearsToleranceParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getArrearsToleranceBoolean()) {
                    this.loanConfigurableAttributes.setInArrearsTolerance(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.inArrearsToleranceParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.repaymentEveryParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getRepaymentEveryBoolean()) {
                    this.loanConfigurableAttributes.setRepaymentEvery(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.repaymentEveryParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getGraceOnPrincipalAndInterestPaymentBoolean()) {
                    this.loanConfigurableAttributes.setGraceOnPrincipalAndInterestPayment(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME)
                        .getAsBoolean() != this.loanConfigurableAttributes.getGraceOnArrearsAgingBoolean()) {
                    this.loanConfigurableAttributes.setGraceOnArrearsAgeing(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME).getAsBoolean());
                }
                final String servicerFeeInterestConfigEnabled = LoanProductConstants.SERVICER_FEE_INTEREST_CONFIG_ENABLED;
                if (command.isChangeInBooleanParameterNamed(servicerFeeInterestConfigEnabled, this.servicerFeeInterestConfigEnabled)) {
                    final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(servicerFeeInterestConfigEnabled);
                    this.servicerFeeInterestConfigEnabled = newValue;
        }
                final String servicerFeeChargesConfigEnabled = LoanProductConstants.SERVICER_FEE_CHARGES_CONFIG_ENABLED;
                if (command.isChangeInBooleanParameterNamed(servicerFeeChargesConfigEnabled, this.servicerFeeChargesConfigEnabled)) {
                    final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(servicerFeeChargesConfigEnabled);
                    this.servicerFeeChargesConfigEnabled = newValue;
                }
            } else {
                this.loanConfigurableAttributes = LoanProductConfigurableAttributes.populateDefaultsForConfigurableAttributes();
                this.loanConfigurableAttributes.updateLoanProduct(this);
            }
        }

        if (actualChanges.containsKey(LoanProductConstants.holdGuaranteeFundsParamName)) {
            if (this.holdGuaranteeFunds) {
                this.loanProductGuaranteeDetails = LoanProductGuaranteeDetails.createFrom(command);
                this.loanProductGuaranteeDetails.updateProduct(this);
                actualChanges.put(LoanProductConstants.mandatoryGuaranteeParamName,
                        this.loanProductGuaranteeDetails.getMandatoryGuarantee());
                actualChanges.put(LoanProductConstants.minimumGuaranteeFromGuarantorParamName,
                        this.loanProductGuaranteeDetails.getMinimumGuaranteeFromGuarantor());
                actualChanges.put(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName,
                        this.loanProductGuaranteeDetails.getMinimumGuaranteeFromOwnFunds());
            } else {
                this.loanProductGuaranteeDetails = null;
            }

        } else if (this.holdGuaranteeFunds) {
            this.loanProductGuaranteeDetails.update(command, actualChanges);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName,
                this.principalThresholdForLastInstallment)) {
            BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName);
            actualChanges.put(LoanProductConstants.principalThresholdForLastInstallmentParamName, newValue);
            this.principalThresholdForLastInstallment = newValue;
        }
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME,
                this.accountMovesOutOfNPAOnlyOnArrearsCompletion)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(
                    LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME);
            actualChanges.put(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME, newValue);
            this.accountMovesOutOfNPAOnlyOnArrearsCompletion = newValue;
        }
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.canDefineEmiAmountParamName, this.canDefineInstallmentAmount)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.canDefineEmiAmountParamName);
            actualChanges.put(LoanProductConstants.canDefineEmiAmountParamName, newValue);
            this.canDefineInstallmentAmount = newValue;
        }

        if (command.isChangeInIntegerParameterNamedWithNullCheck(LoanProductConstants.installmentAmountInMultiplesOfParamName,
                this.installmentAmountInMultiplesOf)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName);
            actualChanges.put(LoanProductConstants.installmentAmountInMultiplesOfParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.installmentAmountInMultiplesOf = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.CAN_USE_FOR_TOPUP, this.canUseForTopup)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.CAN_USE_FOR_TOPUP);
            actualChanges.put(LoanProductConstants.CAN_USE_FOR_TOPUP, newValue);
            this.canUseForTopup = newValue;
        }

        if (command.hasParameter(LoanProductConstants.RATES_PARAM_NAME)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(LoanProductConstants.RATES_PARAM_NAME);
            if (jsonArray != null) {
                actualChanges.put(LoanProductConstants.RATES_PARAM_NAME, command.jsonFragment(LoanProductConstants.RATES_PARAM_NAME));
            }
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName,
                this.fixedPrincipalPercentagePerInstallment)) {
            BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName);
            actualChanges.put(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName, newValue);
            this.fixedPrincipalPercentagePerInstallment = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS,
                this.disallowExpectedDisbursements)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS);
            actualChanges.put(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS, newValue);
            this.disallowExpectedDisbursements = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED,
                this.allowApprovedDisbursedAmountsOverApplied)) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED);
            actualChanges.put(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED, newValue);
            this.allowApprovedDisbursedAmountsOverApplied = newValue;
        }

        if (command.isChangeInStringParameterNamed(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE, this.overAppliedCalculationType)) {
            final String newValue = command.stringValueOfParameterNamed(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE);
            actualChanges.put(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE, newValue);
            this.overAppliedCalculationType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.OVER_APPLIED_NUMBER, this.overAppliedNumber)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.OVER_APPLIED_NUMBER);
            actualChanges.put(LoanProductConstants.OVER_APPLIED_NUMBER, newValue);
            actualChanges.put("locale", localeAsInput);
            this.overAppliedNumber = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("isPennyDropEnabled", this.isPennyDropEnabled)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("isPennyDropEnabled");
            actualChanges.put("isPennydropEnabled", newValue);
            this.isPennyDropEnabled = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("isBankDisbursementEnabled", this.isBankDisbursementEnabled)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("isBankDisbursementEnabled");
            actualChanges.put("isBankDisbursementEnabled", newValue);
            this.isBankDisbursementEnabled = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.DEDUPE_ENABLED, this.dedupeEnabled)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.DEDUPE_ENABLED);
            actualChanges.put(LoanProductConstants.DEDUPE_ENABLED, newValue);
            this.dedupeEnabled = newValue;
            if(!this.dedupeEnabled) this.dedupeType = null;
        }

        if (this.dedupeEnabled && command.isChangeInIntegerParameterNamed(LoanProductConstants.DEDUPE_TYPE, this.dedupeType)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DEDUPE_TYPE);
            actualChanges.put(LoanProductConstants.DEDUPE_TYPE, newValue);
            this.dedupeType = newValue;
        }

        if (command.isChangeInLongParameterNamed(LoanProductConstants.DISBURSEMENT_BANK_ACC_NAME, disbursementBankAccountId())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DISBURSEMENT_BANK_ACC_NAME);
            actualChanges.put(LoanProductConstants.DISBURSEMENT_BANK_ACC_NAME, newValue);
        }

        return actualChanges;
    }

    private Long loanTypeId() {

        Long loanTypeId = null;
        if (this.loanType != null) {
            loanTypeId = this.loanType.getId();
        }
        return loanTypeId;
    }

    private Long frameWorkId() {

        Long frameWorkId = null;
        if (this.frameWork != null) {
            frameWorkId = this.frameWork.getId();
        }
        return frameWorkId;
    }

//    private Long collectionId() {
//
//        Long collectionId = null;
//        if (this.collectionMode != null) {
//            collectionId = this.collectionMode.getId();
//        }
//        return collectionId;
//
//    }

//    private Long disbursementId() {
//
//        Long disbursementId = null;
//        if (this.disbursementMode != null) {
//            disbursementId = this.disbursementMode.getId();
//        }
//        return disbursementId;
//
//    }

    private Long fldgLogicId() {

        Long fldgLogicId = null;
        if (this.fldgLogic != null) {
            fldgLogicId = this.fldgLogic.getId();
        }
        return fldgLogicId;


    }





    private Long insuranceApplicabilityId() {

        Long insuranceApplicabilityId = null;
        if (this.insuranceApplicability != null) {
            insuranceApplicabilityId = this.insuranceApplicability.getId();
        }
        return insuranceApplicabilityId;

    }

    private Long loanProductTypeId() {

        Long loanProductTypeId = null;
        if (this.loanProductType != null) {
            loanProductTypeId = this.loanProductType.getId();
        }
        return loanProductTypeId;


    }

    private Long loanProductClassId() {

        Long loanProductClassId = null;
        if (this.loanProductClass != null) {
            loanProductClassId = this.loanProductClass.getId();
        }
        return loanProductClassId;
    }

    private Long assetClassId() {
            Long assetClassId = null;
            if (this.assetClass != null) {
                assetClassId = this.assetClass.getId();
            }
            return assetClassId;
        }


    private LoanProductFloatingRates loanProductFloatingRates() {
        this.floatingRates = this.floatingRates == null ? new LoanProductFloatingRates(null, this, null, null, null, null, false)
                : this.floatingRates;
        return this.floatingRates;
    }

    public LoanProductVariableInstallmentConfig loanProductVariableInstallmentConfig() {
        this.variableInstallmentConfig = this.variableInstallmentConfig == null ? new LoanProductVariableInstallmentConfig(this, null, null)
                : this.variableInstallmentConfig;
        return this.variableInstallmentConfig;
    }



//    public LoanProductAgeLimitsConfig loanProductAgeLimitsConfig() {
//        this.ageLimitsConfig = this.ageLimitsConfig == null ? new LoanProductAgeLimitsConfig(this, null, null)
//                : this.ageLimitsConfig;
//        return this.ageLimitsConfig;
//    }
//
//    public LoanProductPrepaidLockConfig loanProductPrepaidLockConfig() {
//        this.prepaidLoanConfig = this.prepaidLoanConfig == null ? new LoanProductPrepaidLockConfig(this,null)
//                : this.prepaidLoanConfig;
//        return this.prepaidLoanConfig;
//    }
//
//    public LoanProductForeclosureLockConfig loanProductForeclosureLockConfig() {
//        this.foreclosureLoanConfig = this.foreclosureLoanConfig == null ? new LoanProductForeclosureLockConfig(this,null)
//                : this.foreclosureLoanConfig;
//        return this.foreclosureLoanConfig;
//    }

    public boolean isAccountingDisabled() {
        return AccountingRuleType.NONE.getValue().equals(this.accountingRule);
    }

    public boolean isCashBasedAccountingEnabled() {
        return AccountingRuleType.CASH_BASED.getValue().equals(this.accountingRule);
    }

    public boolean isAccrualBasedAccountingEnabled() {
        return isUpfrontAccrualAccountingEnabled() || isPeriodicAccrualAccountingEnabled();
    }

    public boolean isUpfrontAccrualAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_UPFRONT.getValue().equals(this.accountingRule);
    }

    public boolean isPeriodicAccrualAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_PERIODIC.getValue().equals(this.accountingRule);
    }

    public Money getPrincipalAmount() {
        return this.loanProductRelatedDetail.getPrincipal();
    }

    public Money getMinPrincipalAmount() {
        return Money.of(this.loanProductRelatedDetail.getCurrency(), loanProductMinMaxConstraints().getMinPrincipal());
    }

    public Money getMaxPrincipalAmount() {
        return Money.of(this.loanProductRelatedDetail.getCurrency(), loanProductMinMaxConstraints().getMaxPrincipal());
    }

    public BigDecimal getNominalInterestRatePerPeriod() {
        return this.loanProductRelatedDetail.getNominalInterestRatePerPeriod();
    }

    public PeriodFrequencyType getInterestPeriodFrequencyType() {
        return this.loanProductRelatedDetail.getInterestPeriodFrequencyType();
    }

    public BigDecimal getMinNominalInterestRatePerPeriod() {
        return loanProductMinMaxConstraints().getMinNominalInterestRatePerPeriod();
    }

    public BigDecimal getMaxNominalInterestRatePerPeriod() {
        return loanProductMinMaxConstraints().getMaxNominalInterestRatePerPeriod();
    }

    public Integer getNumberOfRepayments() {
        return this.loanProductRelatedDetail.getNumberOfRepayments();
    }

    public Integer getMinNumberOfRepayments() {
        return loanProductMinMaxConstraints().getMinNumberOfRepayments();
    }

    public Integer getMaxNumberOfRepayments() {
        return loanProductMinMaxConstraints().getMaxNumberOfRepayments();
    }

    private Long penalInvoiceId() {

        Long penalInvoiceId = null;
        if (this.penalInvoice != null) {
            penalInvoiceId = this.penalInvoice.getId();
        }
        return penalInvoiceId;


    }

    private Long multiDisbursementId() {

        Long multiDisbursementId = null;
        if (this.multipleDisbursement != null) {
            multiDisbursementId = this.multipleDisbursement.getId();
        }
        return multiDisbursementId;
    }

    private Long trancheClubbingId() {

        Long trancheClubbingId = null;
        if (this.trancheClubbing != null) {
            trancheClubbingId = this.trancheClubbing.getId();
        }
        return trancheClubbingId;
    }

    private Long repaymentScheduleUpdateAllowedId() {

        Long repaymentScheduleUpdateAllowedId = null;
        if (this.repaymentScheduleUpdateAllowed != null) {
            repaymentScheduleUpdateAllowedId = this.repaymentScheduleUpdateAllowed.getId();
        }
        return repaymentScheduleUpdateAllowedId;


    }

    public LoanProductMinMaxConstraints loanProductMinMaxConstraints() {
        // If all min and max fields are null then loanProductMinMaxConstraints
        // initialising to null
        // Reset LoanProductMinMaxConstraints with null values.
        this.loanProductMinMaxConstraints = this.loanProductMinMaxConstraints == null
                ? new LoanProductMinMaxConstraints(null, null, null, null, null, null)
                : this.loanProductMinMaxConstraints;
        return this.loanProductMinMaxConstraints;
    }

    public boolean isIncludeInBorrowerCycle() {
        return this.includeInBorrowerCycle;
    }

    public LocalDate getStartDate() {
        LocalDate startLocalDate = null;
        if (this.startDate != null) {
            startLocalDate = LocalDate.ofInstant(this.startDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }
        return startLocalDate;
    }

    public LocalDate getCloseDate() {
        LocalDate closeLocalDate = null;
        if (this.closeDate != null) {
            closeLocalDate = LocalDate.ofInstant(this.closeDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }
        return closeLocalDate;
    }

    public String productName() {
        return this.name;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public boolean useBorrowerCycle() {
        return this.useBorrowerCycle;
    }

    public boolean isMultiDisburseLoan() {
        return this.loanProducTrancheDetails.isMultiDisburseLoan();
    }

    public BigDecimal outstandingLoanBalance() {
        return this.loanProducTrancheDetails.outstandingLoanBalance();
    }

    public Integer maxTrancheCount() {
        return this.loanProducTrancheDetails.maxTrancheCount();
    }

    public boolean isInterestRecalculationEnabled() {
        return this.loanProductRelatedDetail.isInterestRecalculationEnabled();
    }

    public Integer getMinimumDaysBetweenDisbursalAndFirstRepayment() {
        return this.minimumDaysBetweenDisbursalAndFirstRepayment == null ? 0 : this.minimumDaysBetweenDisbursalAndFirstRepayment;
    }

    public LoanProductBorrowerCycleVariations fetchLoanProductBorrowerCycleVariationById(Long id) {
        LoanProductBorrowerCycleVariations borrowerCycleVariation = null;
        for (LoanProductBorrowerCycleVariations cycleVariation : this.borrowerCycleVariations) {
            if (id.equals(cycleVariation.getId())) {
                borrowerCycleVariation = cycleVariation;
                break;
            }
        }
        return borrowerCycleVariation;
    }

    public boolean syncExpectedWithDisbursementDate() {
        return syncExpectedWithDisbursementDate;
    }
//    public boolean coBorrower() {
//        return coBorrower;
//    }
//    public boolean eodBalance() {
//        return eodBalance;
//    }
//    public boolean securedLoan() {
//        return securedLoan;
//    }
//    public boolean nonEquatedInstallment() {
//        return nonEquatedInstallment;
//    }
//
//    public boolean advanceEMI() {
//        return advanceEMI;
//    }
//    public boolean termBasedOnLoanCycle() {
//        return termBasedOnLoanCycle;
//    }
//    public boolean isNetOffApplied() {
//        return isNetOffApplied;
//    }
    public boolean allowApprovalOverAmountApplied() {
        return allowApprovalOverAmountApplied;
    }

    public boolean useDaysInMonthForLoanProvisioning() {
        return useDaysInMonthForLoanProvisioning;
    }

    public boolean divideByThirtyForPartialPeriod() {
        return divideByThirtyForPartialPeriod;
    }


    public boolean enableColendingLoan() {
        return enableColendingLoan;
    }

    public boolean selectAcceptedDates() {
        return selectAcceptedDates;
    }

    public boolean applyPrepaidLockingPeriod() {
        return applyPrepaidLockingPeriod;
    }
    public Integer prepayLockingPeriod() {
        return prepayLockingPeriod;
    }
    public boolean applyForeclosureLockingPeriod() {
        return applyForeclosureLockingPeriod;
    }
    public Integer foreclosureLockingPeriod() {
        return foreclosureLockingPeriod;
    }



    public void setSyncExpectedWithDisbursementDate(boolean syncExpectedWithDisbursementDate) {
        this.syncExpectedWithDisbursementDate = syncExpectedWithDisbursementDate;
    }

//
//
//    public void setCoBorrower(boolean coBorrower) {
//        this.coBorrower = coBorrower;
//    }
//
//    public void setEodBalance(boolean eodBalance) {
//        this.eodBalance = eodBalance;
//    }
//
//    public void setSecuredLoan(boolean securedLoan) {
//        this.securedLoan = securedLoan;
//    }
//
//    public void setNonEquatedInstallment(boolean nonEquatedInstallment) {
//        this.nonEquatedInstallment = nonEquatedInstallment;
//    }
//
//
//    public void setAdvanceEMI(boolean advanceEMI) {
//        this.advanceEMI = advanceEMI;
//    }
//
//    public void setTermBasedOnLoanCycle(boolean termBasedOnLoanCycle) {
//        this.termBasedOnLoanCycle = termBasedOnLoanCycle;
//    }
//
//    public void setIsNetOffApplied(boolean isNetOffApplied) {
//        this.isNetOffApplied = isNetOffApplied;
//    }

    public void setAllowApprovalOverAmountApplied(boolean allowApprovalOverAmountApplied) {
        this.allowApprovalOverAmountApplied = allowApprovalOverAmountApplied;
    }

    public void setUseDaysInMonthForLoanProvisioning(boolean useDaysInMonthForLoanProvisioning) {
        this.useDaysInMonthForLoanProvisioning = useDaysInMonthForLoanProvisioning;
    }

    public void setDivideByThirtyForPartialPeriod(boolean divideByThirtyForPartialPeriod) {
        this.divideByThirtyForPartialPeriod = divideByThirtyForPartialPeriod;
    }

    public Boolean getIsPennyDropEnabled() {
        return isPennyDropEnabled;
    }

    public void setPennyDropEnabled(Boolean isPennyDropEnabled) {
        isPennyDropEnabled = isPennyDropEnabled;
    }

    public Boolean getIsBankDisbursementEnabled() {
        return isBankDisbursementEnabled;
    }

    public void setIsBankDisbursementEnabled(Boolean isBankDisbursementEnabled) {
        isBankDisbursementEnabled = isBankDisbursementEnabled;
    }
    public Map<String, BigDecimal> fetchBorrowerCycleVariationsForCycleNumber(final Integer cycleNumber) {
        Map<String, BigDecimal> borrowerCycleVariations = new HashMap<>();
        borrowerCycleVariations.put(LoanProductConstants.PRINCIPAL, this.loanProductRelatedDetail.getPrincipal().getAmount());
        borrowerCycleVariations.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD,
                this.loanProductRelatedDetail.getNominalInterestRatePerPeriod());
        if (this.loanProductRelatedDetail.getNumberOfRepayments() != null) {
            borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                    BigDecimal.valueOf(this.loanProductRelatedDetail.getNumberOfRepayments()));
        }

        if (this.loanProductMinMaxConstraints != null) {
            borrowerCycleVariations.put(LoanProductConstants.MIN_PRINCIPAL, this.loanProductMinMaxConstraints.getMinPrincipal());
            borrowerCycleVariations.put(LoanProductConstants.MAX_PRINCIPAL, this.loanProductMinMaxConstraints.getMaxPrincipal());
            borrowerCycleVariations.put(LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD,
                    this.loanProductMinMaxConstraints.getMinNominalInterestRatePerPeriod());
            borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                    this.loanProductMinMaxConstraints.getMaxNominalInterestRatePerPeriod());

            if (this.loanProductMinMaxConstraints.getMinNumberOfRepayments() != null) {
                borrowerCycleVariations.put(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS,
                        BigDecimal.valueOf(this.loanProductMinMaxConstraints.getMinNumberOfRepayments()));
            }

            if (this.loanProductMinMaxConstraints.getMaxNumberOfRepayments() != null) {
                borrowerCycleVariations.put(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS,
                        BigDecimal.valueOf(this.loanProductMinMaxConstraints.getMaxNumberOfRepayments()));
            }
        }
        if (cycleNumber > 0) {
            Integer principalCycleUsed = 0;
            Integer interestCycleUsed = 0;
            Integer repaymentCycleUsed = 0;
            for (LoanProductBorrowerCycleVariations cycleVariation : this.borrowerCycleVariations) {
                if (cycleVariation.getBorrowerCycleNumber().equals(cycleNumber)
                        && cycleVariation.getValueConditionType().equals(LoanProductValueConditionType.EQUAL)) {
                    switch (cycleVariation.getParamType()) {
                        case PRINCIPAL:
                            borrowerCycleVariations.put(LoanProductConstants.PRINCIPAL, cycleVariation.getDefaultValue());
                            borrowerCycleVariations.put(LoanProductConstants.MIN_PRINCIPAL, cycleVariation.getMinValue());
                            borrowerCycleVariations.put(LoanProductConstants.MAX_PRINCIPAL, cycleVariation.getMaxValue());
                            principalCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            break;
                        case INTERESTRATE:
                            borrowerCycleVariations.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD, cycleVariation.getDefaultValue());
                            borrowerCycleVariations.put(LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD, cycleVariation.getMinValue());
                            borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD, cycleVariation.getMaxValue());
                            interestCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            break;
                        case REPAYMENT:
                            borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                                    cycleVariation.getDefaultValue());
                            borrowerCycleVariations.put(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS, cycleVariation.getMinValue());
                            borrowerCycleVariations.put(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS, cycleVariation.getMaxValue());
                            repaymentCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            break;
                        default:
                            break;
                    }
                } else if (cycleVariation.getBorrowerCycleNumber() < cycleNumber
                        && cycleVariation.getValueConditionType().equals(LoanProductValueConditionType.GREATERTHAN)) {
                    switch (cycleVariation.getParamType()) {
                        case PRINCIPAL:
                            if (principalCycleUsed < cycleVariation.getBorrowerCycleNumber()) {
                                borrowerCycleVariations.put(LoanProductConstants.PRINCIPAL, cycleVariation.getDefaultValue());
                                borrowerCycleVariations.put(LoanProductConstants.MIN_PRINCIPAL, cycleVariation.getMinValue());
                                borrowerCycleVariations.put(LoanProductConstants.MAX_PRINCIPAL, cycleVariation.getMaxValue());
                                principalCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            }
                            break;
                        case INTERESTRATE:
                            if (interestCycleUsed < cycleVariation.getBorrowerCycleNumber()) {
                                borrowerCycleVariations.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD,
                                        cycleVariation.getDefaultValue());
                                borrowerCycleVariations.put(LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD,
                                        cycleVariation.getMinValue());
                                borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                                        cycleVariation.getMaxValue());
                                interestCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            }
                            break;
                        case REPAYMENT:
                            if (repaymentCycleUsed < cycleVariation.getBorrowerCycleNumber()) {
                                borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                                        cycleVariation.getDefaultValue());
                                borrowerCycleVariations.put(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS, cycleVariation.getMinValue());
                                borrowerCycleVariations.put(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS, cycleVariation.getMaxValue());
                                repaymentCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return borrowerCycleVariations;
    }

    public DaysInMonthType fetchDaysInMonthType() {
        return this.loanProductRelatedDetail.fetchDaysInMonthType();
    }

    public DaysInYearType fetchDaysInYearType() {
        return this.loanProductRelatedDetail.fetchDaysInYearType();
    }

    public LoanProductInterestRecalculationDetails getProductInterestRecalculationDetails() {
        return this.productInterestRecalculationDetails;
    }

    public boolean isHoldGuaranteeFundsEnabled() {
        return this.holdGuaranteeFunds;
    }

    public LoanProductGuaranteeDetails getLoanProductGuaranteeDetails() {
        return this.loanProductGuaranteeDetails;
    }

    public String getShortName() {
        return this.shortName;
    }

    public String getLoanAccNoPreference() {
        return this.loanAccNoPreference;
    }

    public BigDecimal getPrincipalThresholdForLastInstallment() {
        return this.principalThresholdForLastInstallment;
    }

    public boolean isArrearsBasedOnOriginalSchedule() {
        boolean isBasedOnOriginalSchedule = false;
        if (getProductInterestRecalculationDetails() != null) {
            isBasedOnOriginalSchedule = getProductInterestRecalculationDetails().isArrearsBasedOnOriginalSchedule();
        }
        return isBasedOnOriginalSchedule;
    }

    public boolean canDefineInstallmentAmount() {
        return this.canDefineInstallmentAmount;
    }

    public Integer getInstallmentAmountInMultiplesOf() {
        return this.installmentAmountInMultiplesOf;
    }

    public LoanPreClosureInterestCalculationStrategy preCloseInterestCalculationStrategy() {
        LoanPreClosureInterestCalculationStrategy preCloseInterestCalculationStrategy = LoanPreClosureInterestCalculationStrategy.NONE;
        if (this.isInterestRecalculationEnabled()) {
            preCloseInterestCalculationStrategy = getProductInterestRecalculationDetails().preCloseInterestCalculationStrategy();
        }
        return preCloseInterestCalculationStrategy;
    }

    public LoanProductRelatedDetail getLoanProductRelatedDetail() {
        return loanProductRelatedDetail;
    }

    public boolean isLinkedToFloatingInterestRate() {
        return this.isLinkedToFloatingInterestRate;
    }

    public LoanProductFloatingRates getFloatingRates() {
        return this.floatingRates;
    }

    public Collection<FloatingRatePeriodData> fetchInterestRates(final FloatingRateDTO floatingRateDTO) {
        Collection<FloatingRatePeriodData> applicableRates = new ArrayList<>(1);
        if (isLinkedToFloatingInterestRate()) {
            applicableRates = getFloatingRates().fetchInterestRates(floatingRateDTO);
        }
        return applicableRates;
    }

    public boolean allowVariabeInstallments() {
        return this.allowVariabeInstallments;
    }

//    public boolean allowAgeLimits() {
//        return this.allowAgeLimits;
//    }

//    public boolean allowPrepaidLockingPeriod() {
//        return this.allowPrepaidLockingPeriod;
//    }
//
//    public boolean allowForeclosureLockingPeriod() {
//        return this.allowForeclosureLockingPeriod;
//    }

    public boolean canUseForTopup() {
        return this.canUseForTopup;
    }

    public boolean isEqualAmortization() {
        return loanProductRelatedDetail.isEqualAmortization();
    }

    public List<Rate> getRates() {
        return rates;
    }

    public void setRates(List<Rate> rates) {
        this.rates = rates;
    }

    public BigDecimal getFixedPrincipalPercentagePerInstallment() {
        return fixedPrincipalPercentagePerInstallment;
    }

    public boolean isDisallowExpectedDisbursements() {
        return disallowExpectedDisbursements;
    }

    public boolean isAllowApprovedDisbursedAmountsOverApplied() {
        return allowApprovedDisbursedAmountsOverApplied;
    }

    public String getOverAppliedCalculationType() {
        return overAppliedCalculationType;
    }

    public Integer getOverAppliedNumber() {
        return overAppliedNumber;
    }

    public void setDisallowExpectedDisbursements(boolean disallowExpectedDisbursements) {
        this.disallowExpectedDisbursements = disallowExpectedDisbursements;
    }

    public void setAllowApprovedDisbursedAmountsOverApplied(boolean allowApprovedDisbursedAmountsOverApplied) {
        this.allowApprovedDisbursedAmountsOverApplied = allowApprovedDisbursedAmountsOverApplied;
    }

    public void setOverAppliedCalculationType(String overAppliedCalculationType) {
        this.overAppliedCalculationType = overAppliedCalculationType;
    }

    public void setOverAppliedNumber(Integer overAppliedNumber) {
        this.overAppliedNumber = overAppliedNumber;
    }

    public void setLoanProducTrancheDetails(LoanProductTrancheDetails loanProducTrancheDetails) {
        this.loanProducTrancheDetails = loanProducTrancheDetails;
    }

    public Integer getSelfPrincipalShare() {
        return selfPrincipalShare;
    }

    public Integer getSelfFeeShare() {
        return selfFeeShare;
    }

    public Integer getSelfPenaltyShare() {
        return selfPenaltyShare;
    }

    public Integer getSelfOverpaidShares() {
        return selfOverpaidShares;
    }

    public BigDecimal getSelfInterestRate() {
        return selfInterestRate;
    }

    public Integer getPrincipalShare() {
        return principalShare;
    }

    public Integer getFeeShare() {
        return feeShare;
    }

    public Integer getPenaltyShare() {
        return penaltyShare;
    }

    public Integer getOverpaidShare() {
        return overpaidShare;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public Integer getPartnerPrincipalShare() {
        return partnerPrincipalShare;
    }

    public Integer getPartnerFeeShare() {
        return partnerFeeShare;
    }

    public Integer getPartnerPenaltyShare() {
        return partnerPenaltyShare;
    }

    public Integer getPartnerOverpaidShare() {
        return partnerOverpaidShare;
    }

    public BigDecimal getPartnerInterestRate() {
        return partnerInterestRate;
    }


    public BigDecimal getAumSlabRate() {
        return aumSlabRate;
    }

    public BigDecimal getGstLiabilityByVcpl() {
        return gstLiabilityByVcpl;
    }

    public BigDecimal getGstLiabilityByPartner() {
        return gstLiabilityByPartner;
    }

    public Long getDisbursementAccountNumber() {
        return disbursementAccountNumber;
    }

    public Long getCollectionAccountNumber() {
        return collectionAccountNumber;
    }

//   public BigDecimal getVcplHurdleRate() {
//        return vcplHurdleRate;
//    }

//    public BigDecimal getVcplShareInPf() {
//        return vcplShareInPf;
//    }
//
//    public BigDecimal getPartnerShareInPf() {
//        return partnerShareInPf;
//    }
//
//    public BigDecimal getVcplShareInPenalInterest() {
//        return vcplShareInPenalInterest;
//    }
//
//    public BigDecimal getPartnerShareInPenalInterest() {
//        return partnerShareInPenalInterest;
//    }

    public BigDecimal getVcplShareInBrokenInterest() {
        return vcplShareInBrokenInterest;
    }

    public BigDecimal getPartnerShareInBrokenInterest() {
        return partnerShareInBrokenInterest;
    }

//    public BigDecimal getVcplShareInForeclosureCharges() {
//        return vcplShareInForeclosureCharges;
//    }
//
//    public BigDecimal getPartnerShareInForeclosureCharges() {
//        return partnerShareInForeclosureCharges;
//    }
//
//    public BigDecimal getVcplShareInOtherCharges() {
//        return vcplShareInOtherCharges;
//    }
//
//    public BigDecimal getPartnerShareInOtherCharges() {
//        return partnerShareInOtherCharges;
//    }

    public Integer getMonitoringTriggerPar30() {
        return monitoringTriggerPar30;
    }

    public Integer getMonitoringTriggerPar90() {
        return monitoringTriggerPar90;
    }


    public void updateAssetClass(CodeValue assetClass) {

        this.assetClass = assetClass;

    }

    public  void  updateLoanProductType(CodeValue loanProductTypee){

        this.loanProductType = loanProductTypee;
    }

    public  void  updateInsuranceApplicability(CodeValue insuranceApplicability){

        this.insuranceApplicability=insuranceApplicability;
    }

    public  void  updateFldgLogic(CodeValue fldgLogic){
        this.fldgLogic=fldgLogic;
    }

    public void updatePenalInvoice(CodeValue penalInvoice)
    {
        this.penalInvoice = penalInvoice;
    }

    public void updateMultipleDisbursement(CodeValue multipleDisbursement)
    {
        this.multipleDisbursement = multipleDisbursement;
    }

    public void updateTrancheClubbing(CodeValue trancheClubbing)
    {
        this.trancheClubbing = trancheClubbing;
    }

    public void updateRepaymentScheduleUpdateAllowed(CodeValue repaymentScheduleUpdateAllowed)
    {
        this.repaymentScheduleUpdateAllowed = repaymentScheduleUpdateAllowed;
    }


//    public void updateDisbursement(CodeValue disbursement) {
//
//        this.disbursementMode=disbursement;
//    }

//    public void updateCollection(CodeValue collection) {
//
//        this.collectionMode=collection;
//    }

    public void updateFrameWork(CodeValue frameWork) {
        this.frameWork=frameWork;
    }

    public void updateLoanType(CodeValue loanType) {

        this.loanType =loanType;
    }

    public void updateLoanProductClass(CodeValue loanProductClass) {

        this.loanProductClass=loanProductClass;
    }
    public String getName() {
        return this.name;
    }
    public void updateDisbursementBankAccount(CodeValue disbursementBankAccount) {
        this.disbursementBankAccount=(this.isPennyDropEnabled || this.isBankDisbursementEnabled)
                ? disbursementBankAccount : null;
    }

    private Long disbursementBankAccountId() {

        Long disbursementBankAccountId = null;
        if (this.disbursementBankAccount != null) {
            disbursementBankAccountId = this.disbursementBankAccount.getId();
        }
        return disbursementBankAccountId;
    }

    public RoundingMode getInterestRoundingMode(){
        return  RoundingMode.valueOf(interestRoundingMode);
    }

    public void validateDisbursementDate(LocalDate disbursementDate, LoanProduct loanProduct) {
        if (Boolean.FALSE.equals(loanProduct.getEnableBackDatedDisbursement()) && disbursementDate.isBefore(LocalDate.now())) {
            final String errorMessage = "BackDate Disbursement is Not Allowed For This Loan cannot be Disbursed ";
            throw new BackDatedDisbursementNotAllowedException("disbursement ", "BackDate Disbursement is Not Allowed For This LoanProduct", errorMessage);
        }
    }
}
