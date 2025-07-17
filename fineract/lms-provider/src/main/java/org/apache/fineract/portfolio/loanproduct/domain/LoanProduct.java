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
package org.apache.fineract.portfolio.loanproduct.domain;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException;
import org.apache.fineract.portfolio.loanproductclass.domain.LoanProductClass;
import org.apache.fineract.portfolio.loanproducttype.domain.LoanProductType;
import org.apache.fineract.portfolio.rate.domain.Rate;

/**
 * Loan products allow for categorisation of an organisations loans into something meaningful to them.
 *
 * They provide a means of simplifying creation/maintenance of loans. They can also allow for product comparison to take
 * place when reporting.
 *
 * They allow for constraints to be added at product level.
 */
@Entity
@Table(name = "m_product_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unq_name"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "external_id_UNIQUE"),
        @UniqueConstraint(columnNames = { "short_name" }, name = "unq_short_name") })
public class LoanProduct extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "fund_id", nullable = true)
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = true)
    private LoanProductClass loanProductClass;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = true)
    private LoanProductType loanProductType;

    @ManyToOne
    @JoinColumn(name = "loan_transaction_strategy_id", nullable = true)
    private LoanTransactionProcessingStrategy transactionProcessingStrategy;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "short_name", nullable = false, unique = true)
    private String shortName;

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

    @Column(name = "broken_interest_days_in_years")
    private String brokenInterestDaysInYears;

    @Column(name = "broken_interest_days_in_month")
    private String brokenInterestDaysInMonth;

    @Column(name = "broken_interest_strategy")
    private String brokenInterestStrategy;

    @Column(name = "coBorrower")
    private boolean coBorrower;

    @Column(name = "eod_balance")
    private boolean eodBalance;

    @Column(name = "secured_loan")
    private boolean securedLoan;

    @Column(name = "non_equated_installment")
    private boolean nonEquatedInstallment;


    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanProductAgeLimitsConfig ageLimitsConfig;

    @Column(name = "allow_age_limits")
    private boolean allowAgeLimits;

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

    @Column(name = "advance_emi")
    private boolean advanceEMI;

    @Column(name = "term_based_on_loancycle")
    private boolean termBasedOnLoanCycle;

    @Column(name = "is_net_off_applied")
    private boolean isNetOffApplied;

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

    @Column(name = "self_interest_rate")
    private Integer selfInterestRate;

    @Column(name = "principal_share")
    private Integer principalShare;

    @Column(name = "fee_share")
    private Integer feeShare;

    @Column(name = "penalty_share")
    private Integer penaltyShare;

    @Column(name = "overpaid_share")
    private Integer overpaidShare;

    @Column(name = "interest_rate")
    private Integer interestRate;

    @Column(name = "select_partner")
    private String selectPartner;

    @Column(name = "enable_charge_wise_bifacation")
    private boolean enableChargeWiseBifacation;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductCharges> productCharges = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductAcceptedDates> productAcceptedDates = new HashSet<>();

    public static LoanProduct assembleFromJson(final Fund fund, final LoanProductClass loanProductClass,final LoanProductType loanProductType, LoanTransactionProcessingStrategy loanTransactionProcessingStrategy,
                                               final List<Charge> productCharges, final JsonCommand command, final AprCalculator aprCalculator, FloatingRate floatingRate,
                                               final List<Rate> productRates) {

        final String name = command.stringValueOfParameterNamed("name");
        final String shortName = command.stringValueOfParameterNamed(LoanProductConstants.SHORT_NAME);
        final String brokenInterestCalculationPeriod=command.stringValueOfParameterNamed("brokenInterestCalculationPeriod");
        // final boolean repaymentStrategyForNpa=command.booleanObjectValueOfParameterNamed("repaymentStrategyForNpa");
        final String repaymentStrategyForNpaId=command.stringValueOfParameterNamed("repaymentStrategyForNpaId");
        final String loanForeclosureStrategy=command.stringValueOfParameterNamed("loanForeclosureStrategy");
        final String brokenInterestDaysInYears=command.stringValueOfParameterNamed("brokenInterestDaysInYears");
        final String brokenInterestDaysInMonth=command.stringValueOfParameterNamed("brokenInterestDaysInMonth");
        final String brokenInterestStrategy=command.stringValueOfParameterNamed("brokenInterestStrategy");

        final String description = command.stringValueOfParameterNamed("description");
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed("digitsAfterDecimal");
        final Integer inMultiplesOf = command.integerValueOfParameterNamed("inMultiplesOf");
        final Boolean coBorrower = command.booleanObjectValueOfParameterNamed("coBorrower");
        final Boolean eodBalance = command.booleanObjectValueOfParameterNamed("eodBalance");
        final Boolean securedLoan = command.booleanObjectValueOfParameterNamed("securedLoan");
        final Boolean nonEquatedInstallment = command.booleanObjectValueOfParameterNamed("nonEquatedInstallment");

        final Boolean advanceEMI = command.booleanObjectValueOfParameterNamed("advanceEMI");
        final Boolean termBasedOnLoanCycle = command.booleanObjectValueOfParameterNamed("termBasedOnLoanCycle");
        final Boolean isNetOffApplied = command.booleanObjectValueOfParameterNamed("isNetOffApplied");
        final Boolean allowApprovalOverAmountApplied = command.booleanObjectValueOfParameterNamed("allowApprovalOverAmountApplied");

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
        final Integer selfInterestRate=command.integerValueOfParameterNamed("selfInterestRate");
        final Integer principalShare=command.integerValueOfParameterNamed("principalShare");
        final Integer feeShare=command.integerValueOfParameterNamed("feeShare");
        final Integer penaltyShare=command.integerValueOfParameterNamed("penaltyShare");
        final Integer overpaidShare=command.integerValueOfParameterNamed("overpaidShare");
        final Integer interestRate=command.integerValueOfParameterNamed("interestRate");
        final String selectPartner=command.stringValueOfParameterNamed("selectPartner");
        final boolean enableChargeWiseBifacation=command.booleanObjectValueOfParameterNamed("enableChargeWiseBifacation");
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



        final Boolean allowAgeLimits = command
                .booleanObjectValueOfParameterNamed(LoanProductConstants.allowAgeLimitsParamName);
        if (allowAgeLimits != null && allowAgeLimits) {
            minimumAge = command.integerValueOfParameterNamed(LoanProductConstants.minimumAge);
            maximumAge = command.integerValueOfParameterNamed(LoanProductConstants.maximumAge);
        }

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

        final Set<LoanProductCharges> loanProductCharges = new HashSet<>();
        assembleColendingCharges(command,loanProductCharges);
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

        return new LoanProduct(fund,loanProductClass,loanProductType, loanTransactionProcessingStrategy, name, shortName, description, currency, principal, minPrincipal,
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
                allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType, overAppliedNumber,coBorrower,eodBalance,securedLoan,nonEquatedInstallment,advanceEMI,termBasedOnLoanCycle,isNetOffApplied,allowApprovalOverAmountApplied,
                isVariableInstallmentsAllowed,minimumGapBetweenInstallments,maximumGapBetweenInstallments,allowAgeLimits,minimumAge,maximumAge,brokenInterestCalculationPeriod,repaymentStrategyForNpaId,
                loanForeclosureStrategy,brokenInterestDaysInYears, brokenInterestDaysInMonth, brokenInterestStrategy,useDaysInMonthForLoanProvisioning,divideByThirtyForPartialPeriod,enableColendingLoan, selectAcceptedDates,applyPrepaidLockingPeriod,prepayLockingPeriod,
                applyForeclosureLockingPeriod,foreclosureLockingPeriod,loanProductCharges,loanProductAcceptedDates,overAmountDetails,byPercentageSplit,selfPrincipalShare,selfFeeShare,selfPenaltyShare,selfOverpaidShares,
                selfInterestRate,principalShare,feeShare,penaltyShare,overpaidShare,interestRate,selectPartner,enableChargeWiseBifacation,acceptedDateType,acceptedStartDate,acceptedEndDate,acceptedDate,selectCharge);

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

    private static void assembleColendingCharges(final JsonCommand command,
                                                 final Set<LoanProductCharges> loanProductCharges) {
        if (command.parameterExists("selectCharge")) {
            final JsonArray selectCharge = command.arrayOfParameterNamed("selectCharge");
            if (selectCharge != null && selectCharge.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = selectCharge.get(i).getAsJsonObject();
                    Integer colendingCharge = null;
                    Integer selfCharge = null;
                    Integer partnerCharge = null;
                    if (jsonObject.has(LoanProductConstants.COLENDING_CHARGE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.COLENDING_CHARGE_PARAMETER_NAME).isJsonPrimitive()) {
                        colendingCharge = jsonObject.getAsJsonPrimitive(LoanProductConstants.COLENDING_CHARGE_PARAMETER_NAME).getAsInt();
                    }

                    if (jsonObject.has(LoanProductConstants.SELF_CHARGE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.SELF_CHARGE_PARAMETER_NAME).isJsonPrimitive()) {
                        selfCharge = jsonObject.getAsJsonPrimitive(LoanProductConstants.SELF_CHARGE_PARAMETER_NAME).getAsInt();
                    }

                    if (jsonObject.has(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME).isJsonPrimitive()) {
                        partnerCharge = jsonObject.getAsJsonPrimitive(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME).getAsInt();
                    }

                    LoanProductCharges productCharges = new LoanProductCharges(colendingCharge,selfCharge,partnerCharge);
                    loanProductCharges.add(productCharges);
                    i++;
                } while (i < selectCharge.size());
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



    public LoanProduct(final Fund fund, final LoanProductClass loanProductClass,final LoanProductType loanProductType, LoanTransactionProcessingStrategy transactionProcessingStrategy, final String name,
                       final String shortName, final String description, final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
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
                       final Integer overAppliedNumber, final boolean coBorrower, final boolean eodBalance, final boolean securedLoan, final boolean nonEquatedInstallment,final boolean advanceEMI, final boolean termBasedOnLoanCycle,
                       final boolean isNetOffApplied, final boolean allowApprovalOverAmountApplied, final Boolean isVariableInstallmentsAllowed,
                       final Integer minimumGapBetweenInstallments, final Integer maximumGapBetweenInstallments,final Boolean allowAgeLimits,final Integer minimumAge,final Integer maximumAge,
                       final String brokenInterestCalculationPeriod,
                       final String repaymentStrategyForNpaId,final String loanForeclosureStrategy,final String brokenInterestDaysInYears,final String brokenInterestDaysInMonth,final String brokenInterestStrategy,
                       final boolean useDaysInMonthForLoanProvisioning,
                       final boolean divideByThirtyForPartialPeriod,
                       final boolean enableColendingLoan,final boolean selectAcceptedDates,final boolean applyPrepaidLockingPeriod,final Integer prepayLockingPeriod,final boolean applyForeclosureLockingPeriod,final Integer foreclosureLockingPeriod,
                       final Set<LoanProductCharges> productCharges,final Set<LoanProductAcceptedDates> productAcceptedDates,final boolean overAmountDetails,final boolean byPercentageSplit,final Integer selfPrincipalShare,final Integer selfFeeShare,final Integer selfPenaltyShare,final Integer selfOverpaidShares,final Integer selfInterestRate,final Integer principalShare,final Integer feeShare,final Integer penaltyShare,
                       final Integer overpaidShare,final Integer interestRate,final String selectPartner,final boolean enableChargeWiseBifacation,final String acceptedDateType,final Integer acceptedStartDate,final Integer acceptedEndDate,final Integer accepetedDate,final String selectCharge) {
        this.fund = fund;
        this.loanProductClass=loanProductClass;
        this.loanProductType = loanProductType;
        this.transactionProcessingStrategy = transactionProcessingStrategy;
        this.name = name.trim();
        this.shortName = shortName.trim();
        this.brokenInterestCalculationPeriod=brokenInterestCalculationPeriod;
//      this.repaymentStrategyForNpa=repaymentStrategyForNpa;
        this.repaymentStrategyForNpaId=repaymentStrategyForNpaId;
        this.loanForeclosureStrategy=loanForeclosureStrategy;
        this.brokenInterestDaysInYears=brokenInterestDaysInYears;
        this.brokenInterestDaysInMonth=brokenInterestDaysInMonth;
        this.brokenInterestStrategy=brokenInterestStrategy;

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
        this.selectPartner=selectPartner;
        this.enableChargeWiseBifacation=enableChargeWiseBifacation;
        this.selectCharge=selectCharge;
//        this.colendingCharge=colendingCharge;
//        this.selfCharge=selfCharge;
//        this.partnerCharge=partnerCharge;
        this.acceptedDateType=acceptedDateType;
        this.acceptedStartDate=acceptedStartDate;
        this.acceptedEndDate=acceptedEndDate;
//        this.acceptedDate=acceptedDate;


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


        this.allowAgeLimits = allowAgeLimits == null ? false : allowAgeLimits;

        if (allowAgeLimits) {
            this.ageLimitsConfig = new LoanProductAgeLimitsConfig(this, minimumAge,
                    maximumAge);
        }

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
                isInterestRecalculationEnabled, isEqualAmortization);

        this.loanProductRelatedDetail.validateRepaymentPeriodWithGraceSettings();

        this.loanProductMinMaxConstraints = new LoanProductMinMaxConstraints(defaultMinPrincipal, defaultMaxPrincipal,
                defaultMinNominalInterestRatePerPeriod, defaultMaxNominalInterestRatePerPeriod, defaultMinNumberOfInstallments,
                defaultMaxNumberOfInstallments);

        if (accountingRuleType != null) {
            this.accountingRule = accountingRuleType.getValue();
        }
        this.includeInBorrowerCycle = includeInBorrowerCycle;
        this.coBorrower=coBorrower;
        this.eodBalance=eodBalance;
        this.securedLoan=securedLoan;
        this.nonEquatedInstallment=nonEquatedInstallment;

        this.advanceEMI=advanceEMI;
        this.termBasedOnLoanCycle=termBasedOnLoanCycle;
        this.isNetOffApplied=isNetOffApplied;
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
        this.coBorrower=coBorrower;
        this.eodBalance=eodBalance;
        this.securedLoan=securedLoan;
        this.nonEquatedInstallment=nonEquatedInstallment;

        this.advanceEMI=advanceEMI;
        this.termBasedOnLoanCycle=termBasedOnLoanCycle;
        this.isNetOffApplied=isNetOffApplied;
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
        this.productCharges=productCharges;
        this.productAcceptedDates=productAcceptedDates;


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

    public void update(final LoanProductClass loanProductClass) {
        this.loanProductClass = loanProductClass;
    }

    public void update(final LoanProductType loanProductType) {
        this.loanProductType = loanProductType;
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

    public Map<String, Object> update(final JsonCommand command, final AprCalculator aprCalculator, FloatingRate floatingRate) {

        final Map<String, Object> actualChanges = this.loanProductRelatedDetail.update(command, aprCalculator);
        actualChanges.putAll(loanProductMinMaxConstraints().update(command));

        final String isLinkedToFloatingInterestRates = "isLinkedToFloatingInterestRates";
        if (command.isChangeInBooleanParameterNamed(isLinkedToFloatingInterestRates, this.isLinkedToFloatingInterestRate)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isLinkedToFloatingInterestRates);
            actualChanges.put(isLinkedToFloatingInterestRates, newValue);
            this.isLinkedToFloatingInterestRate = newValue;
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



        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.allowAgeLimitsParamName,
                this.allowAgeLimits)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowAgeLimitsParamName);
            actualChanges.put(LoanProductConstants.allowAgeLimitsParamName, newValue);
            this.allowAgeLimits = newValue;
        }

        if (this.allowAgeLimits) {
            actualChanges.putAll(loanProductAgeLimitsConfig().update(command));
        } else {
            this.ageLimitsConfig = null;
        }

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

        final String shortNameParamName = LoanProductConstants.SHORT_NAME;
        if (command.isChangeInStringParameterNamed(shortNameParamName, this.shortName)) {
            final String newValue = command.stringValueOfParameterNamed(shortNameParamName);
            actualChanges.put(shortNameParamName, newValue);
            this.shortName = newValue;
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

        final String brokenInterestDaysInYearsParamName = "brokenInterestDaysInYears";
        if (command.isChangeInStringParameterNamed(brokenInterestDaysInYearsParamName, this.brokenInterestDaysInYears)) {
            final String newValue = command.stringValueOfParameterNamed(brokenInterestDaysInYearsParamName);
            actualChanges.put(brokenInterestDaysInYearsParamName, newValue);
            this.brokenInterestDaysInYears = newValue;
        }

        final String brokenInterestDaysInMonthParamName = "brokenInterestDaysInMonth";
        if (command.isChangeInStringParameterNamed(brokenInterestDaysInMonthParamName, this.brokenInterestDaysInMonth)) {
            final String newValue = command.stringValueOfParameterNamed(brokenInterestDaysInMonthParamName);
            actualChanges.put(brokenInterestDaysInMonthParamName, newValue);
            this.brokenInterestDaysInMonth = newValue;
        }

        final String brokenIntrestStrategyParamName = "brokenInterestStrategy";
        if (command.isChangeInStringParameterNamed(brokenIntrestStrategyParamName, this.brokenInterestStrategy)) {
            final String newValue = command.stringValueOfParameterNamed(brokenIntrestStrategyParamName);
            actualChanges.put(brokenIntrestStrategyParamName, newValue);
            this.brokenInterestStrategy = newValue;
        }



        Long existingFundId = null;
        if (this.fund != null) {
            existingFundId = this.fund.getId();
        }
        final String fundIdParamName = "fundId";
        if (command.isChangeInLongParameterNamed(fundIdParamName, existingFundId)) {
            final Long newValue = command.longValueOfParameterNamed(fundIdParamName);
            actualChanges.put(fundIdParamName, newValue);
        }

        Long existingClassId = null;
        if (this.loanProductClass != null) {
            existingClassId = this.loanProductClass.getId();
        }
        final String classIdParamName = "classId";
        if (command.isChangeInLongParameterNamed(classIdParamName, existingClassId)) {
            final Long newValue = command.longValueOfParameterNamed(classIdParamName);
            actualChanges.put(classIdParamName, newValue);
        }

        Long existingTypeId = null;
        if (this.loanProductType != null) {
            existingTypeId = this.loanProductType.getId();
        }
        final String typeIdParamName = "typeId";
        if (command.isChangeInLongParameterNamed(typeIdParamName, existingTypeId)) {
            final Long newValue = command.longValueOfParameterNamed(typeIdParamName);
            actualChanges.put(typeIdParamName, newValue);
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

        if (command.isChangeInBooleanParameterNamed("coBorrower", this.coBorrower)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("coBorrower");
            actualChanges.put("coBorrower", newValue);
            this.coBorrower = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("eodBalance", this.eodBalance)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("eodBalance");
            actualChanges.put("eodBalance", newValue);
            this.eodBalance = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("securedLoan", this.securedLoan)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("securedLoan");
            actualChanges.put("securedLoan", newValue);
            this.securedLoan = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("nonEquatedInstallment", this.nonEquatedInstallment)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("nonEquatedInstallment");
            actualChanges.put("nonEquatedInstallment", newValue);
            this.nonEquatedInstallment = newValue;
        }


        if (command.isChangeInBooleanParameterNamed("advanceEMI", this.advanceEMI)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("advanceEMI");
            actualChanges.put("advanceEMI", newValue);
            this.advanceEMI = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("termBasedOnLoanCycle", this.termBasedOnLoanCycle)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("termBasedOnLoanCycle");
            actualChanges.put("termBasedOnLoanCycle", newValue);
            this.termBasedOnLoanCycle = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("isNetOffApplied", this.isNetOffApplied)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("isNetOffApplied");
            actualChanges.put("isNetOffApplied", newValue);
            this.isNetOffApplied = newValue;
        }

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

        return actualChanges;
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



    public LoanProductAgeLimitsConfig loanProductAgeLimitsConfig() {
        this.ageLimitsConfig = this.ageLimitsConfig == null ? new LoanProductAgeLimitsConfig(this, null, null)
                : this.ageLimitsConfig;
        return this.ageLimitsConfig;
    }
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
    public boolean coBorrower() {
        return coBorrower;
    }
    public boolean eodBalance() {
        return eodBalance;
    }
    public boolean securedLoan() {
        return securedLoan;
    }
    public boolean nonEquatedInstallment() {
        return nonEquatedInstallment;
    }

    public boolean advanceEMI() {
        return advanceEMI;
    }
    public boolean termBasedOnLoanCycle() {
        return termBasedOnLoanCycle;
    }
    public boolean isNetOffApplied() {
        return isNetOffApplied;
    }
    public boolean allowApprovalOverAmountApplied() {
        return allowApprovalOverAmountApplied;
    }

    public boolean useDaysInMonthForLoanProvisioning()
    {
        return useDaysInMonthForLoanProvisioning;
    }

    public boolean divideByThirtyForPartialPeriod()
    {
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



    public void setCoBorrower(boolean coBorrower) {
        this.coBorrower = coBorrower;
    }

    public void setEodBalance(boolean eodBalance) {
        this.eodBalance = eodBalance;
    }

    public void setSecuredLoan(boolean securedLoan) {
        this.securedLoan = securedLoan;
    }

    public void setNonEquatedInstallment(boolean nonEquatedInstallment) {
        this.nonEquatedInstallment = nonEquatedInstallment;
    }


    public void setAdvanceEMI(boolean advanceEMI) {
        this.advanceEMI = advanceEMI;
    }

    public void setTermBasedOnLoanCycle(boolean termBasedOnLoanCycle) {
        this.termBasedOnLoanCycle = termBasedOnLoanCycle;
    }

    public void setIsNetOffApplied(boolean isNetOffApplied) {
        this.isNetOffApplied = isNetOffApplied;
    }

    public void setAllowApprovalOverAmountApplied(boolean allowApprovalOverAmountApplied) {
        this.allowApprovalOverAmountApplied = allowApprovalOverAmountApplied;
    }

    public void setUseDaysInMonthForLoanProvisioning(boolean useDaysInMonthForLoanProvisioning) {
        this.useDaysInMonthForLoanProvisioning = useDaysInMonthForLoanProvisioning;
    }

    public void setDivideByThirtyForPartialPeriod(boolean divideByThirtyForPartialPeriod) {
        this.divideByThirtyForPartialPeriod = divideByThirtyForPartialPeriod;
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

    public boolean allowAgeLimits() {
        return this.allowAgeLimits;
    }

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

}
