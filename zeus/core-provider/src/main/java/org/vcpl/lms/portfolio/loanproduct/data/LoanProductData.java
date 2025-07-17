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
package org.vcpl.lms.portfolio.loanproduct.data;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.vcpl.lms.accounting.common.AccountingEnumerations;
import org.vcpl.lms.accounting.common.AccountingRuleType;
import org.vcpl.lms.accounting.glaccount.data.GLAccountData;
import org.vcpl.lms.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.vcpl.lms.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.office.data.OfficeData;
import org.vcpl.lms.portfolio.calendar.data.CalendarData;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.common.domain.DaysInMonthType;
import org.vcpl.lms.portfolio.common.domain.DaysInYearType;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.common.service.CommonEnumerations;
import org.vcpl.lms.portfolio.floatingrates.data.FloatingRateData;
import org.vcpl.lms.portfolio.fund.data.FundData;
import org.vcpl.lms.portfolio.loanaccount.data.LoanInterestRecalculationData;
import org.vcpl.lms.portfolio.loanproduct.domain.*;
import org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations;

import org.vcpl.lms.portfolio.partner.data.PartnerData;
import org.vcpl.lms.portfolio.paymenttype.data.PaymentTypeData;
import org.vcpl.lms.portfolio.rate.data.RateData;
import org.springframework.util.CollectionUtils;

/**
 * Immutable data object to represent loan products.
 */
public class LoanProductData implements Serializable {

    private final Long id;
    private final String name;
    private final String shortName;
    private final String loanAccNoPreference;
    private final String description;

    private final String brokenInterestCalculationPeriod;
    //private final boolean repaymentStrategyForNpa;
    private final String repaymentStrategyForNpaId;
    private final String loanForeclosureStrategy;
//    private final String brokenInterestDaysInYears;
//    private final String brokenInterestDaysInMonth;
    private final String brokenInterestStrategy;
    private final boolean useDaysInMonthForLoanProvisioning;
    private final boolean divideByThirtyForPartialPeriod;


    private final Long fundId;
    private final String fundName;
//    private final Long classId;
//    private final Long typeId;

//    private final boolean coBorrower;
//    private final boolean eodBalance;
//    private final boolean securedLoan;
//    private final boolean nonEquatedInstallment;
//    private final boolean advanceEMI;
//    private final boolean termBasedOnLoanCycle;
//    private final boolean isNetOffApplied;
    private final boolean allowApprovalOverAmountApplied;
    private final boolean overAmountDetails;

    private final boolean includeInBorrowerCycle;
    private final boolean useBorrowerCycle;
    private final LocalDate startDate;
    private final LocalDate closeDate;
    private final String status;
    private final String externalId;
    // terms
    private final CurrencyData currency;
    private final BigDecimal principal;
    private final BigDecimal minPrincipal;
    private final BigDecimal maxPrincipal;
    private final Integer numberOfRepayments;
    private final Integer minNumberOfRepayments;
    private final Integer maxNumberOfRepayments;
    private final Integer repaymentEvery;
    private final EnumOptionData repaymentFrequencyType;
    private final BigDecimal interestRatePerPeriod;
    private final BigDecimal minInterestRatePerPeriod;
    private final BigDecimal maxInterestRatePerPeriod;
    private final EnumOptionData interestRateFrequencyType;
    private final BigDecimal annualInterestRate;

    private final boolean isLinkedToFloatingInterestRates;
    private final Integer floatingRateId;
    private final String floatingRateName;
    private final BigDecimal interestRateDifferential;
    private final BigDecimal minDifferentialLendingRate;
    private final BigDecimal defaultDifferentialLendingRate;
    private final BigDecimal maxDifferentialLendingRate;
    private final boolean isFloatingInterestRateCalculationAllowed;

    // Variable Installments Settings
    private final boolean allowVariableInstallments;
    private final Integer minimumGap;
    private final Integer maximumGap;


//
//    // AgeCriteria Installments Settings
//    private final boolean allowAgeCriteria;
//    private final Integer minValue;
//    private final Integer maxValue;

    // AgeLimits Installments Settings
    private final boolean allowAgeLimits;
    private final Integer minimumAge;
    private final Integer maximumAge;



    private final boolean enableColendingLoan;
    private final boolean byPercentageSplit;
    private final Integer selfPrincipalShare;
    private final Integer selfFeeShare;
    private final Integer selfPenaltyShare;
    private final Integer selfOverpaidShares;
    private final BigDecimal selfInterestRate;
    private final Integer principalShare;
    private final Integer feeShare;
    private final Integer penaltyShare;
    private final Integer overpaidShare;
    private final BigDecimal interestRate;
    private final Integer partnerPrincipalShare;
    private final Integer partnerFeeShare;
    private final Integer partnerPenaltyShare;
    private final Integer partnerOverpaidShare;
    private final BigDecimal partnerInterestRate;
    private final Long partnerId;
    private final boolean enableFeesWiseBifacation;
    private final boolean enableChargeWiseBifacation;
    private final boolean enableOverDue;
    private final String selectCharge;
    private final Integer colendingCharge;
    private final Integer selfCharge;
    private final Integer partnerCharge;

    private final boolean selectAcceptedDates;
    private final String acceptedDateType;
    private final Integer acceptedStartDate;
    private final Integer acceptedEndDate;
    private final Integer acceptedDate;

    private final boolean applyPrepaidLockingPeriod;
    private final Integer prepayLockingPeriod;
    private final boolean applyForeclosureLockingPeriod;
    private final Integer foreclosureLockingPeriod;

    // settings
    private final EnumOptionData amortizationType;
    private final EnumOptionData interestType;
    private final EnumOptionData interestCalculationPeriodType;
    private final Boolean allowPartialPeriodInterestCalcualtion;
    private final BigDecimal inArrearsTolerance;
    private final Long transactionProcessingStrategyId;
    private final String transactionProcessingStrategyName;
    private final Integer graceOnPrincipalPayment;
    private final Integer recurringMoratoriumOnPrincipalPeriods;
    private final Integer graceOnInterestPayment;
    private final Integer graceOnInterestCharged;
    private final Integer graceOnArrearsAgeing;
    private final Integer overdueDaysForNPA;
    private final EnumOptionData daysInMonthType;
    private final EnumOptionData daysInYearType;
    private final boolean isInterestRecalculationEnabled;
    private final LoanProductInterestRecalculationData interestRecalculationData;
    private final Integer minimumDaysBetweenDisbursalAndFirstRepayment;
    private final boolean canDefineInstallmentAmount;
    private final Integer installmentAmountInMultiplesOf;
    private final BigDecimal aumSlabRate;
    private final BigDecimal gstLiabilityByVcpl;
    private final BigDecimal gstLiabilityByPartner;
    private final String emiRoundingModeSelected;


    private final EnumOptionData brokenStrategyId;
    private final EnumOptionData disbursementId;
    private final EnumOptionData collectionId;
    private final EnumOptionData brokenInterestDaysInMonthSelected;
    private final EnumOptionData brokenInterestDaysInYearSelected;
    private final EnumOptionData transactionTypePreference;

//    private final BigDecimal vcplShareInPf;
//    private final BigDecimal partnerShareInPf;
//    private final BigDecimal vcplShareInPenalInterest;
//    private final BigDecimal partnerShareInPenalInterest;
    private final BigDecimal vcplShareInBrokenInterest;
    private final BigDecimal partnerShareInBrokenInterest;

   // private final BigDecimal vcplHurdleRate;
//    private final BigDecimal vcplShareInForeclosureCharges;
//    private final BigDecimal partnerShareInForeclosureCharges;
//    private final BigDecimal vcplShareInOtherCharges;
//    private final BigDecimal partnerShareInOtherCharges;
    private final Integer monitoringTriggerPar30;
    private final Integer monitoringTriggerPar90;
    private final List<CodeValueData> frameWorkOptions;
    private final List<CodeValueData> insuranceApplicabilityOptions;
    private final List<CodeValueData> loanTypeOptions;
    private final List<CodeValueData> fldgLogicOptions;
    private final List<CodeValueData> assetClassOptions;
    private final List<CodeValueData> loanProductClassOptions;
    private final List<CodeValueData> loanProductTypeOptions;
    private final CodeValueData loanType;
    private final CodeValueData frameWork;
    private final CodeValueData insuranceApplicability;
    private final CodeValueData fldgLogic;
//    private final CodeValueData disbursement;
//    private final CodeValueData collection;
    private final CodeValueData assetClass;
    private final CodeValueData loanProductClass;
    private final CodeValueData loanProductType;


    // charges
    private final Collection<ChargeData> charges;

    //colendingCharge

    private final Collection<LoanProductFeesChargesData> colendingFees;
    private final Collection<LoanProductFeesChargesData> colendingCharges;
    private final Collection<LoanProductFeesChargesData> overDueCharges;

    private final Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle;
    private final Collection<LoanProductBorrowerCycleVariationData> interestRateVariationsForBorrowerCycle;
    private final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariationsForBorrowerCycle;
    // accounting
    private final EnumOptionData accountingRule;
    private final boolean canUseForTopup;

    private Map<String, Object> accountingMappings;
    private Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;
    private Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings;
    private Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings;

    // rates
    private final boolean isRatesEnabled;
    private final Collection<RateData> rates;

    // template related
    private final Collection<FundData> fundOptions;
    private final Collection<PartnerData> partnerData;
    @SuppressWarnings("unused")
    private final Collection<PaymentTypeData> paymentTypeOptions;
    @SuppressWarnings("unused")
    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> repaymentFrequencyTypeOptions;
    private final List<EnumOptionData> interestRateFrequencyTypeOptions;
    private final List<EnumOptionData> amortizationTypeOptions;
    private final List<EnumOptionData> interestTypeOptions;
    private final List<EnumOptionData> interestCalculationPeriodTypeOptions;
    private final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions;
    private final Collection<ChargeData> chargeOptions;
    private final Collection<RateData> rateOptions;
    @SuppressWarnings("unused")
    private final Collection<ChargeData> penaltyOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> accountingRuleOptions;
    @SuppressWarnings("unused")
    private final Map<String, List<GLAccountData>> accountingMappingOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> valueConditionTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> daysInMonthTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> daysInYearTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationCompoundingTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationNthDayTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationDayOfWeekTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> rescheduleStrategyTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> preClosureInterestCalculationStrategyOptions;

    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationFrequencyTypeOptions;
    @SuppressWarnings("unused")
    private final List<FloatingRateData> floatingRateOptions;

    private final List<EnumOptionData> brokenStrategy;
    private final List<EnumOptionData> brokenDaysInYearOptions;
    private final List<EnumOptionData> brokenDaysInMonthOptions;

    private final List<EnumOptionData> disbursementOptions;
    private final List<EnumOptionData> collectionOptions;
    private final Collection<ChargeData> feeOption;
    private final List<EnumOptionData> transactionTypeOptions;

    private final Boolean multiDisburseLoan;
    private final Integer maxTrancheCount;
    private final BigDecimal outstandingLoanBalance;
    private final Boolean disallowExpectedDisbursements;
    private final Boolean allowApprovedDisbursedAmountsOverApplied;
    private final String overAppliedCalculationType;
    private final Integer overAppliedNumber;

    private final BigDecimal principalThresholdForLastInstallment;

    private final Boolean holdGuaranteeFunds;
    private final LoanProductGuaranteeData productGuaranteeData;
    private final Boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;
    private LoanProductConfigurableAttributes allowAttributeOverrides;
    private final boolean syncExpectedWithDisbursementDate;
    private final boolean isEqualAmortization;
    private final BigDecimal fixedPrincipalPercentagePerInstallment;
    private final Long disbursementAccountNumber;
    private final Long collectionAccountNumber;
    private final CodeValueData penalInvoice;
    private final CodeValueData multipleDisbursement;
    private final CodeValueData trancheClubbing;
    private final CodeValueData repaymentScheduleUpdateAllowed;
    private final List<CodeValueData> penalInvoiceOptions;
    private final List<CodeValueData> multipleDisbursementOptions;
    private final List<CodeValueData> trancheClubbingOptions;
    private final List<CodeValueData> repaymentScheduleUpdateAllowedOptions;
    private final List<RoundingMode> roundingModes;
    private final Boolean isPennyDropEnabled;

    private final Boolean isBankDisbursementEnabled;

    private final Boolean servicerFeeInterestConfigEnabled;

    private final Boolean servicerFeeChargesConfigEnabled;
    private final boolean enableDedupe;
    private final List<EnumOptionData> dedupeOptions;
    private final EnumOptionData selectedDedupe;
    private final List<CodeValueData> disbursementBankAccountNameOptions;
    private final CodeValueData selectedDisbursementBankAccountName;
    final List<EnumOptionData> emimultiplesOf;
    final Integer emiDecimalRegexSelected;
    final Integer interestDecimalRegexSelected;
    final String interestRoundingModeSelected;
    final Integer interestDecimalSelected;
    private Integer emiDecimalSelected;
    private Integer emiMultiplesOfSelected;
    private  List<EnumOptionData> advanceAppropriations;
    private EnumOptionData advanceAppropriationSelected;
    private Boolean advanceEntryEnabledSelected;
    private Boolean interestBenefitEnabled;
    private List<EnumOptionData> foreclosurePosCalculation;
    private EnumOptionData foreclosureOnDueDateInterestSelected;
    private EnumOptionData foreclosureOnDueDateChargeSelected;
    private EnumOptionData foreclosureOtherThanDueDateInterestSelected;
    private EnumOptionData foreclosureOtherThanDueDateChargeSelected;
    private EnumOptionData foreclosureOneMonthOverdueInterestSelected;
    private EnumOptionData foreclosureOneMonthOverdueChargeSelected;
    private EnumOptionData foreclosureShortPaidInterestSelected;
    private EnumOptionData foreclosureShortPaidInterestChargeSelected;
    private EnumOptionData foreclosurePrincipalShortPaidInterestSelected;
    private EnumOptionData foreclosurePrincipalShortPaidChargeSelected;
    private EnumOptionData foreclosureTwoMonthsOverdueInterestSelected;
    private EnumOptionData foreclosureTwoMonthsOverdueChargeSelected;
    private EnumOptionData foreclosurePosAdvanceOnDueDateSelected;
    private EnumOptionData foreclosureAdvanceOnDueDateInterestSelected;
    private EnumOptionData foreclosureAdvanceOnDueDateChargeSelected;
    private  EnumOptionData foreclosurePosAdvanceOtherThanDueDateSelected;
    private EnumOptionData foreclosureAdvanceAfterDueDateInterestSelected;
    private EnumOptionData foreclosureAdvanceAfterDueDateChargeSelected;
    private EnumOptionData foreclosureBackdatedShortPaidInterestSelected;
    private EnumOptionData foreclosureBackdatedShortPaidInterestChargeSelected;
    private EnumOptionData foreclosureBackdatedFullyPaidInterestSelected;
    private EnumOptionData foreclosureBackdatedFullyPaidInterestChargeSelected;
    private EnumOptionData foreclosureBackdatedShortPaidPrincipalInterestSelected;
    private EnumOptionData foreclosureBackdatedShortPaidPrincipalChargeSelected;
    private EnumOptionData foreclosureBackdatedFullyPaidEmiInterestSelected;
    private EnumOptionData foreclosureBackdatedFullyPaidEmiChargeSelected;
    private EnumOptionData foreclosureBackdatedAdvanceInterestSelected;
    private EnumOptionData foreclosureBackdatedAdvanceChargeSelected;
    private List<EnumOptionData> servicerFeeChargesRatioOptions;
	private List<EnumOptionData> advanceAppropriationAgainstOn;
	private EnumOptionData advanceAppropriationAgainstOnSelected;

    private EnumOptionData emiDaysInMonthSelected;
    private EnumOptionData emiDaysInYearSelected;
    private EnumOptionData emiCalcSelected;

    private List<EnumOptionData> emiCalcEnum;
    private List<EnumOptionData>  coolingOffInterestAndChargeApplicability;
    private List<EnumOptionData>  coolingOffInterestLogicApplicability;
    private Boolean enableBackDatedDisbursementSelected;



    private List<EnumOptionData> foreclosureMethodType;

    private EnumOptionData foreclosureMethodTypeSelected;
	private Boolean coolingOffApplicability;
    private Integer coolingOffThresholdDays;
    private EnumOptionData coolingOffInterestAndChargeApplicabilitySelected;
    private EnumOptionData coolingOffInterestLogicApplicabilitySelected;
    private EnumOptionData  coolingOffDaysInYearSelected;
    private  String coolingOffRoundingModeSelected;
    private Integer coolingOffRoundingDecimals;

    /**
     * Used when returning lookup information about loan product for dropdowns.
     */
    public static LoanProductData lookup(final Long id, final String name, final Boolean multiDisburseLoan) {
        final String shortName = null;
        final String loanAccNoPreference = null;
        final String description = null;
        final String brokenInterestCalculationPeriod = null;
        //final boolean repaymentStrategyForNpa = false;
        final String repaymentStrategyForNpaId = null;
        final String loanForeclosureStrategy = null;
        final String brokenInterestDaysInYears = null;
        final String brokenInterestDaysInMonth = null;
        final String brokenInterestStrategy = null;
        final CurrencyData currency = null;
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final boolean isLinkedToFloatingInterestRates = false;
        final Integer floatingRateId = null;
        final String floatingRateName = null;
        final BigDecimal interestRateDifferential = null;
        final BigDecimal minDifferentialLendingRate = null;
        final BigDecimal defaultDifferentialLendingRate = null;
        final BigDecimal maxDifferentialLendingRate = null;
        final boolean isFloatingInterestRateCalculationAllowed = false;
        final boolean isVariableInstallmentsAllowed = false;
        final Integer minimumGap = null;
        final Integer maximumGap = null;

//        final boolean isAgeCriteriaAllowed = false;
//        final Integer minValue = null;
//        final Integer maxValue = null;

        final boolean allowAgeLimits = false;
        final Integer minimumAge = null;
        final Integer maximumAge = null;


        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData interestRateFrequencyType = null;
        final EnumOptionData amortizationType = null;
        final EnumOptionData interestType = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Boolean allowPartialPeriodInterestCalcualtion = null;
        final Long fundId = null;

        final EnumOptionData brokenStrategy = null;
        final EnumOptionData collectionOption = null;
        final EnumOptionData disbursementOption = null;

        final String fundName = null;

//        final Boolean coBorrower = false;
//        final Boolean eodBalance = false;
//        final Boolean securedLoan = false;
//        final Boolean nonEquatedInstallment = false;
        final Boolean useDaysInMonthForLoanProvisioning = false;
        final Boolean divideByThirtyForPartialPeriod = false;

//        final Boolean advanceEMI = false;
//        final Boolean termBasedOnLoanCycle = false;
//        final Boolean isNetOffApplied = false;
        final Boolean allowApprovalOverAmountApplied = false;
        final Boolean overAmountDetails = false;

        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;
        final Collection<ChargeData> charges = null;
        final Collection<LoanProductBorrowerCycleVariationData> principalVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations = new ArrayList<>(1);
        final EnumOptionData accountingType = null;
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;
        final Boolean disallowExpectedDisbursements = false;
        final Boolean allowApprovedDisbursedAmountsOverApplied = false;
        final String overAppliedCalculationType = null;
        final Integer overAppliedNumber = null;

        final LoanProductGuaranteeData productGuaranteeData = null;
        final Boolean holdGuaranteeFunds = false;
        final BigDecimal principalThresholdForLastInstallment = null;
        final BigDecimal fixedPrincipalPercentagePerInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = null;
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;
        final Collection<RateData> rateOptions = null;
        final Collection<RateData> rates = null;
        final boolean isRatesEnabled = false;

        final boolean enableColendingLoan=false;
        final boolean byPercentageSplit=false;
        final Integer selfPrincipalShare =null;
        final Integer selfFeeShare =null;
        final Integer selfPenaltyShare =null;
        final Integer selfOverpaidShares =null;
        final BigDecimal selfInterestRate =null;
        final Integer principalShare =null;
        final Integer feeShare =null;
        final Integer penaltyShare =null;
        final Integer overpaidShare =null;
        final BigDecimal interestRate =null;
        final Integer partnerPrincipalShare =null;
        final Integer partnerFeeShare =null;
        final Integer partnerPenaltyShare =null;
        final Integer partnerOverpaidShare =null;
        final BigDecimal partnerInterestRate =null;
        final Long partnerId =null;
        final boolean enableFeesWiseBifacation = false;
        final boolean enableChargeWiseBifacation =false;
        final boolean enableOverDue = false;
        final String selectCharge =null;
        final Integer colendingCharge =null;
        final Integer selfCharge =null;
        final Integer partnerCharge =null;

        final Boolean selectAcceptedDates = false;
        final String acceptedDateType=null;
        final Integer acceptedStartDate=null;
        final Integer acceptedEndDate=null;
        final Integer acceptedDate=null;

        final boolean applyPrepaidLockingPeriod=false;
        final Integer prepayLockingPeriod=null;

        final boolean applyForeclosureLockingPeriod=false;
        final Integer foreclosureLockingPeriod=null;
        final BigDecimal aumSlabRate =null;
        final BigDecimal gstLiabilityByVcpl =null;
        final BigDecimal gstLiabilityByPartner =null;
        final String roundingModeSelected = null;

//        final BigDecimal vcplShareInPf =null;
//        final BigDecimal partnerShareInPf =null;
//        final BigDecimal vcplShareInPenalInterest =null;
//        final BigDecimal partnerShareInPenalInterest =null;
        final BigDecimal vcplShareInBrokenInterest =null;
        final BigDecimal partnerShareInBrokenInterest =null;
      //  final BigDecimal vcplHurdleRate =null;
//        final BigDecimal vcplShareInForeclosureCharges =null;
//        final BigDecimal partnerShareInForeclosureCharges =null;
//        final BigDecimal vcplShareInOtherCharges =null;
//        final BigDecimal partnerShareInOtherCharges =null;
        final Integer monitoringTriggerPar30 =null;
        final Integer monitoringTriggerPar90 =null;
        final List<CodeValueData> frameWorkOptions=null;
        final List<CodeValueData> insuranceApplicabilityOptions=null;
        final List<CodeValueData> loanTypeOptions=null;
        final List<CodeValueData> fldgLogicOptions=null;
        final List<CodeValueData> disbursementOptions=null;
        final List<CodeValueData> collectionOptions=null;
        final List<CodeValueData> assetClassOptions=null;
        final List<CodeValueData> loanProductClassOptions=null;
        final List<CodeValueData> loanProductTypeOptions=null;

        final CodeValueData loanType=null;
        final CodeValueData frameWork=null;
        final CodeValueData insuranceApplicability=null;
        final CodeValueData fldgLogic=null;

//        final CodeValueData disbursement=null;
//        final CodeValueData collection=null;
        final CodeValueData assetClass=null;
        final CodeValueData loanProductClass=null;
        final CodeValueData loanProductType=null;

        final EnumOptionData brokenStrategyId = null;
        final EnumOptionData disbursement = null;
        final EnumOptionData collection = null;
        final Collection<LoanProductFeesChargesData> colendingFees = null;
        final Collection<LoanProductFeesChargesData> colendingCharges = null;
        final Collection<LoanProductFeesChargesData> overDueCharges = null;
        final EnumOptionData brokenInterestDayInMonthSelected = null;
        final EnumOptionData brokenInterestDayInYearSelected = null;
        final Long disbursementAccountNumber = null;
        final Long collectionAccountNumber = null;
        final List<CodeValueData> penalInvoiceOptions = null;
        final List<CodeValueData> multipleDisbursementOptions = null;
        final List<CodeValueData> trancheClubbingOptions = null;
        final List<CodeValueData> repaymentScheduleUpdateAllowedOptions = null;
        final CodeValueData penalInvoice = null;
        final CodeValueData multipleDisbursement = null;
        final CodeValueData trancheClubbing = null;
        final CodeValueData repaymentScheduleUpdateAllowed = null;
        final EnumOptionData transactionTypePreference = null;
        final Boolean isPennyDropEnabled = false;
        final Boolean isBankDisbursementEnabled= false;
        final Boolean servicerFeeInterestConfigEnabled = false;
        final Boolean servicerFeeChargesConfigEnabled =false;
        final boolean enableDedupe = false;
        final List<EnumOptionData> dedupeOptions = null;
        final EnumOptionData selectedDedupe = null;
        final List<CodeValueData> disbursementBankAccountNameOptions = null;
        final CodeValueData selectedDisbursementBankAccountName = null;

        final Integer emiDecimalPlacesRegexSelected = null;
        final Integer interestdecimalSelected = null;
        final String interestRoundingModeSelected = null;
        final Integer interestRegexSelected = null;
        final  Integer emiMultiplesOf = null;
        final Integer emiMultiplesOfSelected = null;
        final EnumOptionData advanceAppropriationSelected = null;
        final Boolean advanceEntryEnabledSelected =  null;
        final Boolean interestBenefitEnabled = null;
        final EnumOptionData foreclosureOnDueDateInterest = null;
        final EnumOptionData foreclosureOnDueDateCharge = null;
        final EnumOptionData foreclosureOtherThanDueDateInterest = null;
        final EnumOptionData foreclosureOtherThanDueDateCharge = null;
        final EnumOptionData foreclosureOneMonthOverdueInterest = null;
        final EnumOptionData foreclosureOneMonthOverdueCharge = null;
        final EnumOptionData foreclosureShortPaidInterest = null;
        final EnumOptionData foreclosureShortPaidInterestCharge = null;
        final EnumOptionData foreclosurePrincipalShortPaidInterest = null;
        final EnumOptionData foreclosurePrincipalShortPaidCharge = null;
        final EnumOptionData foreclosureTwoMonthsOverdueInterest = null;
        final EnumOptionData foreclosureTwoMonthsOverdueCharge = null;
        final EnumOptionData foreclosurePosAdvanceOnDueDate = null;
        final EnumOptionData foreclosureAdvanceOnDueDateInterest = null;
        final EnumOptionData foreclosureAdvanceOnDueDateCharge = null;
        final EnumOptionData foreclosurePosAdvanceOtherThanDueDate = null;
        final EnumOptionData foreclosureAdvanceAfterDueDateInterest = null;
        final EnumOptionData foreclosureAdvanceAfterDueDateCharge = null;
        final EnumOptionData foreclosureBackdatedShortPaidInterest = null;
        final EnumOptionData foreclosureBackdatedShortPaidInterestCharge = null;
        final EnumOptionData foreclosureBackdatedFullyPaidInterest = null;
        final EnumOptionData foreclosureBackdatedFullyPaidInterestCharge = null;
        final EnumOptionData foreclosureBackdatedShortPaidPrincipalInterest = null;
        final EnumOptionData foreclosureBackdatedShortPaidPrincipalCharge = null;
        final EnumOptionData foreclosureBackdatedFullyPaidEmiInterest = null;
        final EnumOptionData foreclosureBackdatedFullyPaidEmiCharge = null;
        final EnumOptionData foreclosureBackdatedAdvanceInterest = null;
        final EnumOptionData foreclosureBackdatedAdvanceCharge = null;
		final EnumOptionData advanceAppropriationAgainstOnSelected = null;

        EnumOptionData emiDaysInMonthSelected = null;
        EnumOptionData emiDaysInYearSelected = null;
        EnumOptionData emiCalcSelected= null;

        Boolean enableBackDatedDisbursementSelect = null;
        EnumOptionData foreclosureMethodTypesSelected = null;
		final EnumOptionData coolingOffInterestAndChargeApplicability = null;

        final Boolean coolingOffApplicability = null;
        final Integer coolingOffThresholdDays = null;
        final EnumOptionData coolingOffInterestAndChargeApplicabilitySelected =null;
        final EnumOptionData coolingOffInterestLogicApplicabilitySelected = null;
        final EnumOptionData  coolingOffDaysInYearSelected =null;
        final  String coolingOffRoundingModeSelected = null;
        final Integer coolingOffRoundingDecimals = null;
        return new LoanProductData(id, name, shortName,loanAccNoPreference, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, charges, accountingType,
                includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariations,
                interestRateVariations, numberOfRepaymentVariations, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                disallowExpectedDisbursements, allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType, overAppliedNumber,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                allowAgeLimits,minimumAge,maximumAge,

                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, rateOptions, rates, isRatesEnabled,
                fixedPrincipalPercentagePerInstallment,allowApprovalOverAmountApplied,overAmountDetails,
                brokenInterestCalculationPeriod,repaymentStrategyForNpaId,loanForeclosureStrategy,
                brokenInterestStrategy,enableColendingLoan,byPercentageSplit,selfPrincipalShare,selfFeeShare,selfPenaltyShare,
                selfOverpaidShares,selfInterestRate, principalShare,feeShare,penaltyShare,overpaidShare,interestRate,partnerId,enableFeesWiseBifacation,enableChargeWiseBifacation,enableOverDue,selectCharge,
                colendingCharge,selfCharge,partnerCharge, selectAcceptedDates,acceptedDateType,acceptedStartDate,acceptedEndDate,acceptedDate,applyPrepaidLockingPeriod,prepayLockingPeriod,applyForeclosureLockingPeriod,
                foreclosureLockingPeriod,partnerPrincipalShare,partnerFeeShare,partnerPenaltyShare,partnerOverpaidShare,partnerInterestRate,useDaysInMonthForLoanProvisioning,divideByThirtyForPartialPeriod,aumSlabRate,
                gstLiabilityByVcpl,gstLiabilityByPartner,vcplShareInBrokenInterest,partnerShareInBrokenInterest,
                monitoringTriggerPar30,monitoringTriggerPar90,frameWorkOptions,insuranceApplicabilityOptions,loanTypeOptions,fldgLogicOptions,disbursementOptions,collectionOptions,assetClassOptions,
                loanProductClassOptions,loanProductTypeOptions,loanType,frameWork,insuranceApplicability,fldgLogic,assetClass,
                loanProductClass,loanProductType,brokenStrategy,disbursement,collection,colendingFees,colendingCharges,overDueCharges,brokenInterestDayInMonthSelected,brokenInterestDayInYearSelected,
                disbursementAccountNumber,collectionAccountNumber,penalInvoiceOptions,multipleDisbursementOptions,trancheClubbingOptions,repaymentScheduleUpdateAllowedOptions,penalInvoice,multipleDisbursement,
                trancheClubbing,repaymentScheduleUpdateAllowed,transactionTypePreference,roundingModeSelected,isPennyDropEnabled,isBankDisbursementEnabled,servicerFeeInterestConfigEnabled,servicerFeeChargesConfigEnabled,
                enableDedupe,dedupeOptions,selectedDedupe,disbursementBankAccountNameOptions,selectedDisbursementBankAccountName,emiDecimalPlacesRegexSelected,interestdecimalSelected,
                interestRoundingModeSelected,interestRegexSelected,emiMultiplesOf,emiMultiplesOfSelected,advanceAppropriationSelected,advanceEntryEnabledSelected,interestBenefitEnabled,
                foreclosureOnDueDateInterest,foreclosureOnDueDateCharge,foreclosureOtherThanDueDateInterest,foreclosureOtherThanDueDateCharge,foreclosureOneMonthOverdueInterest,foreclosureOneMonthOverdueCharge,foreclosureShortPaidInterest
                ,foreclosureShortPaidInterestCharge,foreclosurePrincipalShortPaidInterest,foreclosurePrincipalShortPaidCharge,foreclosureTwoMonthsOverdueInterest,foreclosureTwoMonthsOverdueCharge,foreclosurePosAdvanceOnDueDate,foreclosureAdvanceOnDueDateInterest
                ,foreclosureAdvanceOnDueDateCharge,foreclosurePosAdvanceOtherThanDueDate,foreclosureAdvanceAfterDueDateInterest,foreclosureAdvanceAfterDueDateCharge,foreclosureBackdatedShortPaidInterest,foreclosureBackdatedShortPaidInterestCharge,foreclosureBackdatedFullyPaidInterest
                ,foreclosureBackdatedFullyPaidInterestCharge,foreclosureBackdatedShortPaidPrincipalInterest,foreclosureBackdatedShortPaidPrincipalCharge,foreclosureBackdatedFullyPaidEmiInterest,foreclosureBackdatedFullyPaidEmiCharge
                ,foreclosureBackdatedAdvanceInterest,foreclosureBackdatedAdvanceCharge,advanceAppropriationAgainstOnSelected,emiDaysInMonthSelected,emiDaysInYearSelected,emiCalcSelected,enableBackDatedDisbursementSelect, foreclosureMethodTypesSelected,
				coolingOffApplicability,coolingOffThresholdDays,coolingOffInterestAndChargeApplicabilitySelected,coolingOffInterestLogicApplicabilitySelected,
                coolingOffDaysInYearSelected,coolingOffRoundingModeSelected,coolingOffRoundingDecimals);
    }

    public static LoanProductData lookupWithCurrency(final Long id, final String name, final CurrencyData currency) {
        final String shortName = null;
        final String loanAccNoPreference = null;
        final String description = null;
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final boolean isLinkedToFloatingInterestRates = false;
        final Integer floatingRateId = null;
        final String floatingRateName = null;
        final BigDecimal interestRateDifferential = null;
        final BigDecimal minDifferentialLendingRate = null;
        final BigDecimal defaultDifferentialLendingRate = null;
        final BigDecimal maxDifferentialLendingRate = null;
        final boolean isFloatingInterestRateCalculationAllowed = false;
        final boolean isVariableInstallmentsAllowed = false;
        final Integer minimumGap = null;
        final Integer maximumGap = null;

        final String brokenInterestCalculationPeriod = null;
        // final boolean repaymentStrategyForNpa = false;
        final String repaymentStrategyForNpaId = null;
        final String loanForeclosureStrategy = null;
        final String brokenInterestDaysInYears = null;
        final String brokenInterestDaysInMonth = null;
        final String brokenInterestStrategy = null;


//        final boolean isAgeCriteriaAllowed = false;
//        final Integer minValue = null;
//        final Integer maxValue = null;

        final boolean allowAgeLimits = false;
        final Integer minimumAge = null;
        final Integer maximumAge = null;


        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData interestRateFrequencyType = null;
        final EnumOptionData amortizationType = null;
        final EnumOptionData interestType = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Boolean allowPartialPeriodInterestCalcualtion = false;
        final Long fundId = null;

//        final Long classId = null;
//        final Long typeId = null;

//        final Boolean coBorrower = false;
//        final Boolean eodBalance = false;
//        final Boolean securedLoan = false;
//        final Boolean nonEquatedInstallment = false;
//
//        final Boolean advanceEMI = false;
//        final Boolean termBasedOnLoanCycle = false;
//        final Boolean isNetOffApplied = false;
        final Boolean allowApprovalOverAmountApplied = false;
        final Boolean overAmountDetails = false;
        final Boolean useDaysInMonthForLoanProvisioning = false;
        final Boolean divideByThirtyForPartialPeriod = false;


        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;

        final Collection<ChargeData> charges = null;
        final EnumOptionData accountingType = null;
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;

        final Collection<LoanProductBorrowerCycleVariationData> principalVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations = new ArrayList<>(1);
        final Boolean multiDisburseLoan = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;
        final Boolean disallowExpectedDisbursements = false;
        final Boolean allowApprovedDisbursedAmountsOverApplied = false;
        final String overAppliedCalculationType = null;
        final Integer overAppliedNumber = null;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = null;
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final Boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final BigDecimal fixedPrincipalPercentagePerInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;
        final Collection<RateData> rateOptions = null;
        final Collection<RateData> rates = null;
        final boolean isRatesEnabled = false;

        final boolean enableColendingLoan=false;
        final boolean byPercentageSplit=false;
        final Integer selfPrincipalShare =null;
        final Integer selfFeeShare =null;
        final Integer selfPenaltyShare =null;
        final Integer selfOverpaidShares =null;
        final BigDecimal selfInterestRate =null;
        final Integer principalShare =null;
        final Integer feeShare =null;
        final Integer penaltyShare =null;
        final Integer overpaidShare =null;
        final BigDecimal interestRate =null;
        final Integer partnerPrincipalShare =null;
        final Integer partnerFeeShare =null;
        final Integer partnerPenaltyShare =null;
        final Integer partnerOverpaidShare =null;
        final BigDecimal partnerInterestRate =null;
        final Long partnerId =null;
        final boolean enableFeesWiseBifacation =false;
        final boolean enableChargeWiseBifacation =false;
        final boolean enableOverDue = false;
        final String selectCharge =null;
        final Integer colendingCharge =null;
        final Integer selfCharge =null;
        final Integer partnerCharge =null;
       // final BigDecimal vcplHurdleRate =null;

        final Boolean selectAcceptedDates = false;
        final String acceptedDateType=null;
        final Integer acceptedStartDate=null;
        final Integer acceptedEndDate=null;
        final Integer acceptedDate=null;

        final boolean applyPrepaidLockingPeriod=false;
        final Integer prepayLockingPeriod=null;

        final boolean applyForeclosureLockingPeriod=false;
        final Integer foreclosureLockingPeriod=null;
        final BigDecimal aumSlabRate =null;
        final BigDecimal gstLiabilityByVcpl=null;
        final BigDecimal gstLiabilityByPartner=null;
        final String roundingModeSelected = null;


//        final BigDecimal vcplShareInPf =null;
//        final BigDecimal partnerShareInPf =null;
//        final BigDecimal vcplShareInPenalInterest =null;
//        final BigDecimal partnerShareInPenalInterest =null;
        final BigDecimal vcplShareInBrokenInterest =null;
        final BigDecimal partnerShareInBrokenInterest =null;
//        final BigDecimal vcplShareInForeclosureCharges =null;
//        final BigDecimal partnerShareInForeclosureCharges =null;
//        final BigDecimal vcplShareInOtherCharges =null;
//        final BigDecimal partnerShareInOtherCharges =null;
        final Integer monitoringTriggerPar30 =null;
        final Integer monitoringTriggerPar90 =null;
        final List<CodeValueData> frameWorkOptions =null;
        final List<CodeValueData> insuranceApplicabilityOptions =null;
        final List<CodeValueData> loanTypeOptions =null;
        final List<CodeValueData> fldgLogicOptions =null;
        final List<CodeValueData> disbursementOptions =null;
        final List<CodeValueData> collectionsOptions =null;
        final List<CodeValueData> assetClassOptions =null;
        final List<CodeValueData> loanProductClassOptions =null;
        final List<CodeValueData> loanProductTypeOptions =null;

        final CodeValueData loanType=null;
        final CodeValueData frameWork=null;
        final CodeValueData insuranceApplicability=null;
        final CodeValueData fldgLogic=null;

//        final CodeValueData disbursement=null;
//        final CodeValueData collection=null;
        final CodeValueData assetClass=null;
        final CodeValueData loanProductClass=null;
        final CodeValueData loanProductType=null;

        final EnumOptionData brokenStrategyId = null;
        final EnumOptionData disbursement = null;
        final EnumOptionData collection = null;

        final Collection<LoanProductFeesChargesData> colendingFees = null;
        final Collection<LoanProductFeesChargesData> colendingCharges = null;
        final Collection<LoanProductFeesChargesData> overDueCharges = null;
        final EnumOptionData brokenInterestDayInMonthSelected = null;
        final EnumOptionData brokenInterestDayInYearSelected = null;
        final Long disbursementAccountNumber = null;
        final Long collectionAccountNumber = null;
        final List<CodeValueData> penalInvoiceOptions = null;
        final List<CodeValueData> multipleDisbursementOptions = null;
        final List<CodeValueData> trancheClubbingOptions = null;
        final List<CodeValueData> repaymentScheduleUpdateAllowedOptions = null;
        final CodeValueData penalInvoice = null;
        final CodeValueData multipleDisbursement = null;
        final CodeValueData trancheClubbing = null;
        final CodeValueData repaymentScheduleUpdateAllowed = null;
        final EnumOptionData transactionTypePreference = null;
        final Boolean isPennyDropEnabled = false;
        final Boolean isBankDisbursementEnabled= false;
        final Boolean servicerFeeInterestConfigEnabled= false;
        final Boolean servicerFeeChargesConfigEnabled= false;
        final boolean enableDedupe = false;
        final List<EnumOptionData> dedupeOptions = null;
        final EnumOptionData selectedDedupe = null;
        final List<CodeValueData> disbursementBankAccountNameOptions = null;
        final CodeValueData selectedDisbursementBankAccountName = null;
        final Integer emidecimalPlacesRegexSelected = null;
        final Integer interestdecimalSelected = null;
        final String interestRoundingModeSelected = null;
        final Integer interestRegexSelected = null;
        final  Integer emiMultiplesOf = null;
        final Integer emiMultiplesOfSelected = null;
        final  EnumOptionData advanceAppropriationSelected = null;
        final Boolean advanceEntryEnabledSelected = null;
        final Boolean interestBenefitEnabled = null;
        final EnumOptionData foreclosureOnDueDateInterest = null;
        final EnumOptionData foreclosureOnDueDateCharge = null;
        final EnumOptionData foreclosureOtherThanDueDateInterest = null;
        final EnumOptionData foreclosureOtherThanDueDateCharge = null;
        final EnumOptionData foreclosureOneMonthOverdueInterest = null;
        final EnumOptionData foreclosureOneMonthOverdueCharge = null;
        final EnumOptionData foreclosureShortPaidInterest = null;
        final EnumOptionData foreclosureShortPaidInterestCharge = null;
        final EnumOptionData foreclosurePrincipalShortPaidInterest = null;
        final EnumOptionData foreclosurePrincipalShortPaidCharge = null;
        final EnumOptionData foreclosureTwoMonthsOverdueInterest = null;
        final EnumOptionData foreclosureTwoMonthsOverdueCharge = null;
        final EnumOptionData foreclosurePosAdvanceOnDueDate = null;
        final EnumOptionData foreclosureAdvanceOnDueDateInterest = null;
        final EnumOptionData foreclosureAdvanceOnDueDateCharge = null;
        final EnumOptionData foreclosurePosAdvanceOtherThanDueDate = null;
        final EnumOptionData foreclosureAdvanceAfterDueDateInterest = null;
        final EnumOptionData foreclosureAdvanceAfterDueDateCharge = null;
        final EnumOptionData foreclosureBackdatedShortPaidInterest = null;
        final EnumOptionData foreclosureBackdatedShortPaidInterestCharge = null;
        final EnumOptionData foreclosureBackdatedFullyPaidInterest = null;
        final EnumOptionData foreclosureBackdatedFullyPaidInterestCharge = null;
        final EnumOptionData foreclosureBackdatedShortPaidPrincipalInterest = null;
        final EnumOptionData foreclosureBackdatedShortPaidPrincipalCharge = null;
        final EnumOptionData foreclosureBackdatedFullyPaidEmiInterest = null;
        final EnumOptionData foreclosureBackdatedFullyPaidEmiCharge = null;
        final EnumOptionData foreclosureBackdatedAdvanceInterest = null;
        final EnumOptionData foreclosureBackdatedAdvanceCharge = null;
		final EnumOptionData advanceAppropriationAgainstOnSelected = null;

        EnumOptionData emiDaysInMonthSelected = null;
        EnumOptionData emiDaysInYearSelected = null;
        EnumOptionData emiCalcSelected= null;
        Boolean enableBackDatedDisbursementSelect = null;
        EnumOptionData foreclosureMethodTypesSelected = null;
		final Boolean coolingOffApplicability = null;
        final Integer coolingOffThresholdDays = null;
        final EnumOptionData coolingOffInterestAndChargeApplicabilitySelected = null;
        final EnumOptionData coolingOffInterestLogicApplicabilitySelected = null;
        final EnumOptionData coolingOffDaysInYearSelected = null;
        final String coolingOffRoundingModeSelected = null;
        final Integer coolingOffRoundingDecimals = null;


        return new LoanProductData(id, name, shortName,loanAccNoPreference, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, charges, accountingType,
                includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariations,
                interestRateVariations, numberOfRepaymentVariations, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                disallowExpectedDisbursements, allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType, overAppliedNumber,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                allowAgeLimits,minimumAge,maximumAge,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, rateOptions, rates, isRatesEnabled,
                fixedPrincipalPercentagePerInstallment,allowApprovalOverAmountApplied,overAmountDetails,
                brokenInterestCalculationPeriod,repaymentStrategyForNpaId,loanForeclosureStrategy,
                brokenInterestStrategy,enableColendingLoan,byPercentageSplit,selfPrincipalShare,selfFeeShare,selfPenaltyShare,
                selfOverpaidShares,selfInterestRate, principalShare,feeShare,penaltyShare,overpaidShare,interestRate,partnerId,enableFeesWiseBifacation,enableChargeWiseBifacation,enableOverDue, selectCharge,
                colendingCharge,selfCharge,partnerCharge,selectAcceptedDates,acceptedDateType,acceptedStartDate,acceptedEndDate,acceptedDate,applyPrepaidLockingPeriod,prepayLockingPeriod,
                applyForeclosureLockingPeriod,foreclosureLockingPeriod,partnerPrincipalShare,partnerFeeShare,partnerPenaltyShare,partnerOverpaidShare,partnerInterestRate,useDaysInMonthForLoanProvisioning,divideByThirtyForPartialPeriod,
                aumSlabRate,gstLiabilityByVcpl,gstLiabilityByPartner,vcplShareInBrokenInterest,partnerShareInBrokenInterest,
                monitoringTriggerPar30,monitoringTriggerPar90,frameWorkOptions,insuranceApplicabilityOptions,loanTypeOptions,fldgLogicOptions,disbursementOptions,collectionsOptions,
                assetClassOptions,loanProductClassOptions,loanProductTypeOptions,loanType,frameWork,insuranceApplicability,fldgLogic,assetClass,loanProductClass,loanProductType, brokenStrategyId,disbursement,collection,colendingFees,colendingCharges,overDueCharges,brokenInterestDayInMonthSelected,brokenInterestDayInYearSelected
                ,disbursementAccountNumber,collectionAccountNumber,penalInvoiceOptions,multipleDisbursementOptions,trancheClubbingOptions,repaymentScheduleUpdateAllowedOptions,penalInvoice,multipleDisbursement,trancheClubbing,repaymentScheduleUpdateAllowed,transactionTypePreference,roundingModeSelected,isPennyDropEnabled,
                isBankDisbursementEnabled,servicerFeeInterestConfigEnabled,servicerFeeChargesConfigEnabled,enableDedupe,dedupeOptions,selectedDedupe,disbursementBankAccountNameOptions,
                selectedDisbursementBankAccountName,emidecimalPlacesRegexSelected,interestdecimalSelected,interestRoundingModeSelected,
                interestRegexSelected,emiMultiplesOf,emiMultiplesOfSelected,advanceAppropriationSelected,advanceEntryEnabledSelected,interestBenefitEnabled,
                foreclosureOnDueDateInterest,foreclosureOnDueDateCharge,foreclosureOtherThanDueDateInterest,foreclosureOtherThanDueDateCharge,foreclosureOneMonthOverdueInterest,foreclosureOneMonthOverdueCharge,foreclosureShortPaidInterest
                ,foreclosureShortPaidInterestCharge,foreclosurePrincipalShortPaidInterest,foreclosurePrincipalShortPaidCharge,foreclosureTwoMonthsOverdueInterest,foreclosureTwoMonthsOverdueCharge,foreclosurePosAdvanceOnDueDate,foreclosureAdvanceOnDueDateInterest
                ,foreclosureAdvanceOnDueDateCharge,foreclosurePosAdvanceOtherThanDueDate,foreclosureAdvanceAfterDueDateInterest,foreclosureAdvanceAfterDueDateCharge,foreclosureBackdatedShortPaidInterest,foreclosureBackdatedShortPaidInterestCharge,foreclosureBackdatedFullyPaidInterest
                ,foreclosureBackdatedFullyPaidInterestCharge,foreclosureBackdatedShortPaidPrincipalInterest,foreclosureBackdatedShortPaidPrincipalCharge,foreclosureBackdatedFullyPaidEmiInterest,foreclosureBackdatedFullyPaidEmiCharge
                ,foreclosureBackdatedAdvanceInterest,foreclosureBackdatedAdvanceCharge,advanceAppropriationAgainstOnSelected,emiDaysInMonthSelected,emiDaysInYearSelected,emiCalcSelected,enableBackDatedDisbursementSelect, foreclosureMethodTypesSelected,
				coolingOffApplicability,coolingOffThresholdDays,coolingOffInterestAndChargeApplicabilitySelected,
                coolingOffInterestLogicApplicabilitySelected,coolingOffDaysInYearSelected,coolingOffRoundingModeSelected,coolingOffRoundingDecimals);

    }

    public static LoanProductData sensibleDefaultsForNewLoanProductCreation() {
        final Long id = null;
        final String name = null;
        final String shortName = null;
        final String loanAccNoPreference = null;
        final String description = null;
        final CurrencyData currency = CurrencyData.blank();
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;

        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final boolean isLinkedToFloatingInterestRates = false;
        final Integer floatingRateId = null;
        final String floatingRateName = null;
        final BigDecimal interestRateDifferential = null;
        final BigDecimal minDifferentialLendingRate = null;
        final BigDecimal defaultDifferentialLendingRate = null;
        final BigDecimal maxDifferentialLendingRate = null;
        final boolean isFloatingInterestRateCalculationAllowed = false;
        final boolean isVariableInstallmentsAllowed = false;
        final Integer minimumGap = null;
        final Integer maximumGap = null;

        final String brokenInterestCalculationPeriod = null;
        //final boolean repaymentStrategyForNpa = false;
        final String repaymentStrategyForNpaId = null;
        final String loanForeclosureStrategy = null;
        final String brokenInterestDaysInYears = null;
        final String brokenInterestDaysInMonth = null;
        final String brokenInterestStrategy = null;
//
//        final boolean isAgeCriteriaAllowed = false;
//        final Integer minValue = null;
//        final Integer maxValue = null;

        final boolean allowAgeLimits = false;
        final Integer minimumAge = null;
        final Integer maximumAge = null;


        final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(PeriodFrequencyType.YEARS);
        final EnumOptionData amortizationType = LoanEnumerations.amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS);
        final EnumOptionData interestType = LoanEnumerations.interestType(InterestMethod.DECLINING_BALANCE);
        final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                .interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
        final Boolean allowPartialPeriodInterestCalcualtion = null;
        final Long fundId = null;
        final String fundName = null;
//        final Long classId = null;
//        final Long typeId = null;

//        final Boolean coBorrower = false;
//        final Boolean eodBalance = false;
//        final Boolean securedLoan = false;
//        final Boolean nonEquatedInstallment = false;
//
//        final Boolean advanceEMI = false;
//        final Boolean termBasedOnLoanCycle = false;
//        final Boolean isNetOffApplied = false;
        final Boolean allowApprovalOverAmountApplied = false;
        final Boolean overAmountDetails = false;
        final Boolean useDaysInMonthForLoanProvisioning = false;
        final Boolean divideByThirtyForPartialPeriod = false;

        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;

        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;

        final Collection<ChargeData> charges = null;
        final Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>(1);

        final EnumOptionData accountingType = AccountingEnumerations.accountingRuleType(AccountingRuleType.NONE);
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;
        final Boolean multiDisburseLoan = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;
        final Boolean disallowExpectedDisbursements = false;
        final Boolean allowApprovedDisbursedAmountsOverApplied = false;
        final String overAppliedCalculationType = null;
        final Integer overAppliedNumber = null;

        final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(DaysInMonthType.ACTUAL);
        final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(DaysInYearType.ACTUAL);
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = LoanProductInterestRecalculationData
                .sensibleDefaultsForNewLoanProductCreation();
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final Boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final BigDecimal fixedPrincipalPercentagePerInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;
        final Collection<RateData> rateOptions = null;
        final Collection<RateData> rates = null;
        final boolean isRatesEnabled = false;

        final boolean enableColendingLoan=false;
        final boolean byPercentageSplit=false;
        final Integer selfPrincipalShare =null;
        final Integer selfFeeShare =null;
        final Integer selfPenaltyShare =null;
        final Integer selfOverpaidShares =null;
        final BigDecimal selfInterestRate =null;
        final Integer principalShare =null;
        final Integer feeShare =null;
        final Integer penaltyShare =null;
        final Integer overpaidShare =null;
        final BigDecimal interestRate =null;
        final Integer partnerPrincipalShare =null;
        final Integer partnerFeeShare =null;
        final Integer partnerPenaltyShare =null;
        final Integer partnerOverpaidShare =null;
        final BigDecimal partnerInterestRate =null;
        final Long partnerId =null;
        final boolean enableFeesWiseBifacation =false;
        final boolean enableChargeWiseBifacation =false;
        final boolean enableOverDue = false;
        final String selectCharge =null;
        final Integer colendingCharge =null;
        final Integer selfCharge =null;
        final Integer partnerCharge =null;
    //    final BigDecimal vcplHurdleRate =null;

        final boolean selectAcceptedDates = false;
        final String acceptedDateType=null;
        final Integer acceptedStartDate=null;
        final Integer acceptedEndDate=null;
        final Integer acceptedDate=null;

        final boolean applyPrepaidLockingPeriod=false;
        final Integer prepayLockingPeriod=null;

        final boolean applyForeclosureLockingPeriod=false;
        final Integer foreclosureLockingPeriod=null;
        final BigDecimal aumSlabRate =null;
        final BigDecimal gstLiabilityByVcpl =null;
        final BigDecimal gstLiabilityByPartner =null;
        final String roundingModeSelected = null;


//        final BigDecimal vcplShareInPf =null;
//        final BigDecimal partnerShareInPf =null;
//        final BigDecimal vcplShareInPenalInterest =null;
//        final BigDecimal partnerShareInPenalInterest =null;
        final BigDecimal vcplShareInBrokenInterest =null;
        final BigDecimal partnerShareInBrokenInterest =null;
//        final BigDecimal vcplShareInForeclosureCharges =null;
//        final BigDecimal partnerShareInForeclosureCharges =null;
//        final BigDecimal vcplShareInOtherCharges =null;
//        final BigDecimal partnerShareInOtherCharges =null;
        final Integer monitoringTriggerPar30 =null;
        final Integer monitoringTriggerPar90 =null;
        final List<CodeValueData> frameWorkOptions=null;
        final List<CodeValueData> insuranceApplicabilityOptions=null;
        final List<CodeValueData> loanTypeOptions=null;
        final List<CodeValueData> fldgLogicOptions=null;

        final List<CodeValueData> disbursementOptions=null;
        final List<CodeValueData> collectionOptions=null;
        final List<CodeValueData> assetClassOptions=null;
        final List<CodeValueData> loanProductClassOptions=null;
        final List<CodeValueData> loanProductTypeOptions=null;
        final CodeValueData loanType=null;
        final CodeValueData frameWork=null;
        final CodeValueData insuranceApplicability=null;
        final CodeValueData fldgLogic=null;
        final CodeValueData assetClass=null;
        final CodeValueData loanProductClass=null;
        final CodeValueData loanProductType=null;
        final EnumOptionData brokenStrategy = null;
        final EnumOptionData disbursement = null;
        final EnumOptionData collection = null;
        final Collection<LoanProductFeesChargesData> colendingFees = null;
        final Collection<LoanProductFeesChargesData> colendingCharges = null;
        final Collection<LoanProductFeesChargesData> overDueCharges = null;
        final EnumOptionData brokenInterestDayInMonthSelected = null;
        final EnumOptionData brokenInterestDayInYearSelected = null;
        final Long disbursementAccountNumber = null;
        final Long collectionAccountNumber = null;
        final List<CodeValueData> penalInvoiceOptions = null;
        final List<CodeValueData> multipleDisbursementOptions = null;
        final List<CodeValueData> trancheClubbingOptions = null;
        final List<CodeValueData> repaymentScheduleUpdateAllowedOptions = null;
        final CodeValueData penalInvoice = null;
        final CodeValueData multipleDisbursement = null;
        final CodeValueData trancheClubbing = null;
        final CodeValueData repaymentScheduleUpdateAllowed = null;
        final EnumOptionData transactionTypePreference = null;
        final Boolean isPennyDropEnabled = false;
        final Boolean isBankDisbursementEnabled= false;
        final Boolean servicerFeeInterestConfigEnabled= false;
        final Boolean servicerFeeChargesConfigEnabled= false;
        final boolean enableDedupe = false;
        final List<EnumOptionData> dedupeOptions = Dedupe.all();
        final EnumOptionData selectedDedupe = null;
        final List<CodeValueData> disbursementBankAccountNameOptions = null;
        final CodeValueData selectedDisbursementBankAccountName = null;

        final Integer emidecimalPlacesRegexSelected = null;
        final Integer interestdecimalSelected = null;
        final String interestRoundingModeSelected = null;
        final Integer interestRegexSelected = null;
        final  Integer emiMultiplesOf = null;
        final Integer emiMultiplesOfSelected = null;
        final EnumOptionData advanceAppropriationSelected = null;
        final Boolean advanceEntryEnabledSelected = null;
        final Boolean interestBenefitEnabled = null;
        final EnumOptionData foreclosureOnDueDateInterest = null;
        final EnumOptionData foreclosureOnDueDateCharge = null;
        final EnumOptionData foreclosureOtherThanDueDateInterest = null;
        final EnumOptionData foreclosureOtherThanDueDateCharge = null;
        final EnumOptionData foreclosureOneMonthOverdueInterest = null;
        final EnumOptionData foreclosureOneMonthOverdueCharge = null;
        final EnumOptionData foreclosureShortPaidInterest = null;
        final EnumOptionData foreclosureShortPaidInterestCharge = null;
        final EnumOptionData foreclosurePrincipalShortPaidInterest = null;
        final EnumOptionData foreclosurePrincipalShortPaidCharge = null;
        final EnumOptionData foreclosureTwoMonthsOverdueInterest = null;
        final EnumOptionData foreclosureTwoMonthsOverdueCharge = null;
        final EnumOptionData foreclosurePosAdvanceOnDueDate = null;
        final EnumOptionData foreclosureAdvanceOnDueDateInterest = null;
        final EnumOptionData foreclosureAdvanceOnDueDateCharge = null;
        final EnumOptionData foreclosurePosAdvanceOtherThanDueDate = null;
        final EnumOptionData foreclosureAdvanceAfterDueDateInterest = null;
        final EnumOptionData foreclosureAdvanceAfterDueDateCharge = null;
        final EnumOptionData foreclosureBackdatedShortPaidInterest = null;
        final EnumOptionData foreclosureBackdatedShortPaidInterestCharge = null;
        final EnumOptionData foreclosureBackdatedFullyPaidInterest = null;
        final EnumOptionData foreclosureBackdatedFullyPaidInterestCharge = null;
        final EnumOptionData foreclosureBackdatedShortPaidPrincipalInterest = null;
        final EnumOptionData foreclosureBackdatedShortPaidPrincipalCharge = null;
        final EnumOptionData foreclosureBackdatedFullyPaidEmiInterest = null;
        final EnumOptionData foreclosureBackdatedFullyPaidEmiCharge = null;
        final EnumOptionData foreclosureBackdatedAdvanceInterest = null;
        final EnumOptionData foreclosureBackdatedAdvanceCharge = null;
		final EnumOptionData advanceAppropriationAgainstOnSelected = null;

        EnumOptionData emiDaysInMonthSelected = null;
        EnumOptionData emiDaysInYearSelected = null;
        EnumOptionData emiCalcSelected= null;
        Boolean enableBackDatedDisbursementSelect = null;
        EnumOptionData foreclosureMethodTypesSelected = null;
		final Boolean coolingOffApplicability = null;
        final Integer coolingOffThresholdDays = null;
        final EnumOptionData coolingOffInterestAndChargeApplicabilitySelected = null;
        final EnumOptionData coolingOffInterestLogicApplicabilitySelected = null;
        final EnumOptionData coolingOffDaysInYearSelected = null;
        final String coolingOffRoundingModeSelected = null;
        final Integer coolingOffRoundingDecimals = null;


        return new LoanProductData(id, name, shortName,loanAccNoPreference, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, charges, accountingType,
                includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariationsForBorrowerCycle,
                interestRateVariationsForBorrowerCycle, numberOfRepaymentVariationsForBorrowerCycle, multiDisburseLoan, maxTrancheCount,
                outstandingLoanBalance, disallowExpectedDisbursements, allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType,
                overAppliedNumber, graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                allowAgeLimits,minimumAge,maximumAge,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, rateOptions, rates, isRatesEnabled,
                fixedPrincipalPercentagePerInstallment,allowApprovalOverAmountApplied,overAmountDetails,
                brokenInterestCalculationPeriod,repaymentStrategyForNpaId,loanForeclosureStrategy,
                brokenInterestStrategy,enableColendingLoan,byPercentageSplit,selfPrincipalShare,selfFeeShare,selfPenaltyShare,
                selfOverpaidShares,selfInterestRate, principalShare,feeShare,penaltyShare,overpaidShare,interestRate,partnerId,enableFeesWiseBifacation,enableChargeWiseBifacation,enableOverDue,
                selectCharge,colendingCharge,selfCharge,partnerCharge,selectAcceptedDates,acceptedDateType,acceptedStartDate,acceptedEndDate,acceptedDate,
                applyPrepaidLockingPeriod,prepayLockingPeriod,applyForeclosureLockingPeriod,foreclosureLockingPeriod,partnerPrincipalShare,partnerFeeShare,partnerPenaltyShare,partnerOverpaidShare,partnerInterestRate,
                useDaysInMonthForLoanProvisioning,divideByThirtyForPartialPeriod,aumSlabRate,gstLiabilityByVcpl,gstLiabilityByPartner,vcplShareInBrokenInterest,partnerShareInBrokenInterest,
                monitoringTriggerPar30,monitoringTriggerPar90,frameWorkOptions,insuranceApplicabilityOptions,loanTypeOptions,fldgLogicOptions,disbursementOptions,collectionOptions,
                assetClassOptions,loanProductClassOptions,loanProductTypeOptions,loanType,frameWork,insuranceApplicability,fldgLogic,
                assetClass,loanProductClass,loanProductType,brokenStrategy,disbursement,collection,colendingFees,colendingCharges,overDueCharges,brokenInterestDayInMonthSelected,brokenInterestDayInYearSelected,
                disbursementAccountNumber,collectionAccountNumber,penalInvoiceOptions,multipleDisbursementOptions,trancheClubbingOptions,repaymentScheduleUpdateAllowedOptions,
                penalInvoice,multipleDisbursement,trancheClubbing,repaymentScheduleUpdateAllowed,transactionTypePreference,roundingModeSelected,isPennyDropEnabled,
                isBankDisbursementEnabled,servicerFeeInterestConfigEnabled,servicerFeeChargesConfigEnabled,enableDedupe,dedupeOptions,selectedDedupe,disbursementBankAccountNameOptions,
                selectedDisbursementBankAccountName,emidecimalPlacesRegexSelected,interestdecimalSelected,interestRoundingModeSelected,interestRegexSelected,emiMultiplesOf,emiMultiplesOfSelected,advanceAppropriationSelected,advanceEntryEnabledSelected,interestBenefitEnabled,
                foreclosureOnDueDateInterest,foreclosureOnDueDateCharge,foreclosureOtherThanDueDateInterest,foreclosureOtherThanDueDateCharge,foreclosureOneMonthOverdueInterest,foreclosureOneMonthOverdueCharge,foreclosureShortPaidInterest
                ,foreclosureShortPaidInterestCharge,foreclosurePrincipalShortPaidInterest,foreclosurePrincipalShortPaidCharge,foreclosureTwoMonthsOverdueInterest,foreclosureTwoMonthsOverdueCharge,foreclosurePosAdvanceOnDueDate,foreclosureAdvanceOnDueDateInterest
                ,foreclosureAdvanceOnDueDateCharge,foreclosurePosAdvanceOtherThanDueDate,foreclosureAdvanceAfterDueDateInterest,foreclosureAdvanceAfterDueDateCharge,foreclosureBackdatedShortPaidInterest,foreclosureBackdatedShortPaidInterestCharge,foreclosureBackdatedFullyPaidInterest
                ,foreclosureBackdatedFullyPaidInterestCharge,foreclosureBackdatedShortPaidPrincipalInterest,foreclosureBackdatedShortPaidPrincipalCharge,foreclosureBackdatedFullyPaidEmiInterest,foreclosureBackdatedFullyPaidEmiCharge
                ,foreclosureBackdatedAdvanceInterest,foreclosureBackdatedAdvanceCharge,advanceAppropriationAgainstOnSelected,emiDaysInMonthSelected,emiDaysInYearSelected,emiCalcSelected,enableBackDatedDisbursementSelect, foreclosureMethodTypesSelected,
				coolingOffApplicability,coolingOffThresholdDays,coolingOffInterestAndChargeApplicabilitySelected,coolingOffInterestLogicApplicabilitySelected,
                coolingOffDaysInYearSelected,coolingOffRoundingModeSelected,coolingOffRoundingDecimals);

    }

    public static LoanProductData loanProductWithFloatingRates(final Long id, final String name,
                                                               final boolean isLinkedToFloatingInterestRates, final Integer floatingRateId, final String floatingRateName,
                                                               final BigDecimal interestRateDifferential, final BigDecimal minDifferentialLendingRate,
                                                               final BigDecimal defaultDifferentialLendingRate, final BigDecimal maxDifferentialLendingRate,
                                                               final boolean isFloatingInterestRateCalculationAllowed) {
        final String shortName = null;
        final String loanAccNoPreference = null;
        final String description = null;
        final CurrencyData currency = CurrencyData.blank();
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;

        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final boolean isVariableInstallmentsAllowed = false;
        final Integer minimumGap = null;
        final Integer maximumGap = null;

        final String brokenInterestCalculationPeriod = null;
        //final boolean repaymentStrategyForNpa = false;
        final String repaymentStrategyForNpaId = null;
        final String loanForeclosureStrategy = null;
        final String brokenInterestDaysInYears = null;
        final String brokenInterestDaysInMonth = null;
        final String brokenInterestStrategy = null;
//
//        final boolean isAgeCriteriaAllowed = false;
//        final Integer minValue = null;
//        final Integer maxValue = null;

        final boolean allowAgeLimits = false;
        final Integer minimumAge = null;
        final Integer maximumAge = null;


        final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(PeriodFrequencyType.YEARS);
        final EnumOptionData amortizationType = LoanEnumerations.amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS);
        final EnumOptionData interestType = LoanEnumerations.interestType(InterestMethod.DECLINING_BALANCE);
        final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                .interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
        final Boolean allowPartialPeriodInterestCalcualtion = false;
        final Long fundId = null;
        final String fundName = null;
//        final Long classId = null;
//        final Long typeId = null;

//        final Boolean coBorrower = false;
//        final Boolean eodBalance = false;
//        final Boolean securedLoan = false;
//        final Boolean nonEquatedInstallment = false;
//
//        final Boolean advanceEMI = false;
//        final Boolean termBasedOnLoanCycle = false;
//        final Boolean isNetOffApplied = false;
        final Boolean allowApprovalOverAmountApplied = false;
        final Boolean overAmountDetails = false;
        final Boolean useDaysInMonthForLoanProvisioning = false;
        final Boolean divideByThirtyForPartialPeriod = false;


        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;

        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;

        final Collection<ChargeData> charges = null;
        final Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>(1);

        final EnumOptionData accountingType = AccountingEnumerations.accountingRuleType(AccountingRuleType.NONE);
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;
        final Boolean multiDisburseLoan = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;
        final Boolean disallowExpectedDisbursements = false;
        final Boolean allowApprovedDisbursedAmountsOverApplied = false;
        final String overAppliedCalculationType = null;
        final Integer overAppliedNumber = null;

        final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(DaysInMonthType.ACTUAL);
        final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(DaysInYearType.ACTUAL);
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = LoanProductInterestRecalculationData
                .sensibleDefaultsForNewLoanProductCreation();
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final Boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final BigDecimal fixedPrincipalPercentagePerInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;
        final Collection<RateData> rateOptions = null;
        final Collection<RateData> rates = null;
        final boolean isRatesEnabled = false;
        final boolean enableColendingLoan=false;
        final boolean byPercentageSplit=false;
        final Integer selfPrincipalShare =null;
        final Integer selfFeeShare =null;
        final Integer selfPenaltyShare =null;
        final Integer selfOverpaidShares =null;
        final BigDecimal selfInterestRate =null;
        final Integer principalShare =null;
        final Integer feeShare =null;
        final Integer penaltyShare =null;
        final Integer overpaidShare =null;
        final BigDecimal interestRate =null;
        final Integer partnerPrincipalShare =null;
        final Integer partnerFeeShare =null;
        final Integer partnerPenaltyShare =null;
        final Integer partnerOverpaidShare =null;
        final BigDecimal partnerInterestRate =null;
        final Long partnerId =null;
        final boolean enableFeesWiseBifacation=false;
        final boolean enableChargeWiseBifacation =false;
        final boolean enableOverDue = false;
        final String selectCharge =null;
        final Integer colendingCharge =null;
        final Integer selfCharge =null;
        final Integer partnerCharge =null;
       // final BigDecimal vcplHurdleRate =null;

        final Boolean selectAcceptedDates = false;
        final String acceptedDateType=null;
        final Integer acceptedStartDate=null;
        final Integer acceptedEndDate=null;
        final Integer acceptedDate=null;

        final boolean applyPrepaidLockingPeriod=false;
        final Integer prepayLockingPeriod=null;

        final boolean applyForeclosureLockingPeriod=false;
        final Integer foreclosureLockingPeriod=null;
        final BigDecimal aumSlabRate =null;
        final BigDecimal gstLiabilityByVcpl=null;
        final BigDecimal gstLiabilityByPartner=null;


//        final BigDecimal vcplShareInPf =null;
//        final BigDecimal partnerShareInPf =null;
//        final BigDecimal vcplShareInPenalInterest =null;
//        final BigDecimal partnerShareInPenalInterest =null;
        final BigDecimal vcplShareInBrokenInterest =null;
        final BigDecimal partnerShareInBrokenInterest =null;
//        final BigDecimal vcplShareInForeclosureCharges =null;
//        final BigDecimal partnerShareInForeclosureCharges =null;
//        final BigDecimal vcplShareInOtherCharges =null;
//        final BigDecimal partnerShareInOtherCharges =null;
        final Integer monitoringTriggerPar30 =null;
        final Integer monitoringTriggerPar90 =null;
        final List<CodeValueData> frameWorkOptions=null;
        final List<CodeValueData> insuranceApplicabilityOptions=null;
        final List<CodeValueData> loanTypeOptions=null;
        final List<CodeValueData> fldgLogicOptions=null;


        final List<CodeValueData> disbursementOptions=null;
        final List<CodeValueData> collectionOptions=null;
        final List<CodeValueData> assetClassOptions=null;
        final List<CodeValueData> loanProductClassOptions=null;
        final List<CodeValueData> loanProductTypeOptions=null;
        final CodeValueData loanType=null;
        final CodeValueData frameWork=null;
        final CodeValueData insuranceApplicability=null;
        final CodeValueData fldgLogic=null;
        final CodeValueData assetClass=null;
        final CodeValueData loanProductClass=null;
        final CodeValueData loanProductType=null;

        final EnumOptionData brokenStrategy = null;
        final EnumOptionData disbursement = null;
        final EnumOptionData collection = null;
        final Collection<LoanProductFeesChargesData> colendingFees = null;
        final Collection<LoanProductFeesChargesData> colendingCharges = null;
        final Collection<LoanProductFeesChargesData> overDueCharges = null;
        final EnumOptionData brokenInterestDayInMonthSelected = null;
        final EnumOptionData brokenInterestDayInYearSelected = null;
        final Long disbursementAccountNumber = null;
        final Long collectionAccountNumber = null;
        final List<CodeValueData> penalInvoiceOptions = null;
        final List<CodeValueData> multipleDisbursementOptions = null;
        final List<CodeValueData> trancheClubbingOptions = null;
        final List<CodeValueData> repaymentScheduleUpdateAllowedOptions = null;
        final CodeValueData penalInvoice = null;
        final CodeValueData multipleDisbursement = null;
        final CodeValueData trancheClubbing = null;
        final CodeValueData repaymentScheduleUpdateAllowed = null;
        final EnumOptionData transactionTypePreference = null;
        final String roundingModeSelected = null;
        final Boolean isPennyDropEnabled = false;
        final Boolean isBankDisbursementEnabled= false;
        final Boolean servicerFeeInterestConfigEnabled= false;
        final Boolean servicerFeeChargesConfigEnabled= false;
        final boolean enableDedupe = false;
        final List<EnumOptionData> dedupeOptions = null;
        final EnumOptionData selectedDedupe = null;
        final List<CodeValueData> disbursementBankAccountNameOptions = null;
        final CodeValueData selectedDisbursementBankAccountName = null;
        final Integer decimalPlacesRegexSelected = null;
        final Integer interestdecimalSelected = null;
        final String interestRoundingModeSelected = null;
        final Integer interestRegexSelected = null;
        final  Integer emiMultiplesOf = null;
        final Integer emiMultiplesOfSelected = null;
        final  EnumOptionData advanceAppropriationSelected = null;
        final Boolean advanceEntryEnabledSelected = null;
        final Boolean interestBenefitEnabled = null;
        final EnumOptionData foreclosureOnDueDateInterest = null;
        final EnumOptionData foreclosureOnDueDateCharge = null;
        final EnumOptionData foreclosureOtherThanDueDateInterest = null;
        final EnumOptionData foreclosureOtherThanDueDateCharge = null;
        final EnumOptionData foreclosureOneMonthOverdueInterest = null;
        final EnumOptionData foreclosureOneMonthOverdueCharge = null;
        final EnumOptionData foreclosureShortPaidInterest = null;
        final EnumOptionData foreclosureShortPaidInterestCharge = null;
        final EnumOptionData foreclosurePrincipalShortPaidInterest = null;
        final EnumOptionData foreclosurePrincipalShortPaidCharge = null;
        final EnumOptionData foreclosureTwoMonthsOverdueInterest = null;
        final EnumOptionData foreclosureTwoMonthsOverdueCharge = null;
        final EnumOptionData foreclosurePosAdvanceOnDueDate = null;
        final EnumOptionData foreclosureAdvanceOnDueDateInterest = null;
        final EnumOptionData foreclosureAdvanceOnDueDateCharge = null;
        final EnumOptionData foreclosurePosAdvanceOtherThanDueDate = null;
        final EnumOptionData foreclosureAdvanceAfterDueDateInterest = null;
        final EnumOptionData foreclosureAdvanceAfterDueDateCharge = null;
        final EnumOptionData foreclosureBackdatedShortPaidInterest = null;
        final EnumOptionData foreclosureBackdatedShortPaidInterestCharge = null;
        final EnumOptionData foreclosureBackdatedFullyPaidInterest = null;
        final EnumOptionData foreclosureBackdatedFullyPaidInterestCharge = null;
        final EnumOptionData foreclosureBackdatedShortPaidPrincipalInterest = null;
        final EnumOptionData foreclosureBackdatedShortPaidPrincipalCharge = null;
        final EnumOptionData foreclosureBackdatedFullyPaidEmiInterest = null;
        final EnumOptionData foreclosureBackdatedFullyPaidEmiCharge = null;
        final EnumOptionData foreclosureBackdatedAdvanceInterest = null;
        final EnumOptionData foreclosureBackdatedAdvanceCharge = null;
		 final EnumOptionData advanceAppropriationAgainstOnSelected = null;

         EnumOptionData emiDaysInMonthSelected = null;
         EnumOptionData emiDaysInYearSelected = null;
         EnumOptionData emiCalcSelected= null;
        Boolean enableBackDatedDisbursementSelect = null;
        EnumOptionData foreclosureMethodTypesSelected = null;
		final Boolean coolingOffApplicability = null;
        final Integer coolingOffThresholdDays = null;
        final EnumOptionData coolingOffInterestAndChargeApplicabilitySelected = null;
        final EnumOptionData coolingOffInterestLogicApplicabilitySelected = null;
        final EnumOptionData coolingOffDaysInYearSelected = null;
        final String coolingOffRoundingModeSelected = null;
        final Integer coolingOffRoundingDecimals = null;
        return new LoanProductData(id, name, shortName,loanAccNoPreference, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, charges, accountingType,
                includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariationsForBorrowerCycle,
                interestRateVariationsForBorrowerCycle, numberOfRepaymentVariationsForBorrowerCycle, multiDisburseLoan, maxTrancheCount,
                outstandingLoanBalance, disallowExpectedDisbursements, allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType,
                overAppliedNumber, graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                allowAgeLimits,minimumAge,maximumAge,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, rateOptions, rates, isRatesEnabled,
                fixedPrincipalPercentagePerInstallment,allowApprovalOverAmountApplied,overAmountDetails,
                brokenInterestCalculationPeriod,repaymentStrategyForNpaId,loanForeclosureStrategy,
                brokenInterestStrategy,enableColendingLoan,byPercentageSplit,selfPrincipalShare,selfFeeShare,selfPenaltyShare,
                selfOverpaidShares,selfInterestRate, principalShare,feeShare,penaltyShare,overpaidShare,interestRate,partnerId,enableFeesWiseBifacation,enableChargeWiseBifacation,enableOverDue,
                selectCharge,colendingCharge,selfCharge,partnerCharge,selectAcceptedDates,acceptedDateType,acceptedStartDate,acceptedEndDate,acceptedDate,applyPrepaidLockingPeriod,prepayLockingPeriod,
                applyForeclosureLockingPeriod,foreclosureLockingPeriod,partnerPrincipalShare,partnerFeeShare,partnerPenaltyShare,partnerOverpaidShare,partnerInterestRate,
                useDaysInMonthForLoanProvisioning,divideByThirtyForPartialPeriod,aumSlabRate,gstLiabilityByVcpl,gstLiabilityByPartner,vcplShareInBrokenInterest,partnerShareInBrokenInterest,
                monitoringTriggerPar30,monitoringTriggerPar90,frameWorkOptions,insuranceApplicabilityOptions,loanTypeOptions,fldgLogicOptions,disbursementOptions,collectionOptions,
                assetClassOptions,loanProductClassOptions,loanProductTypeOptions,loanType,frameWork,insuranceApplicability,fldgLogic,
                assetClass,loanProductClass,loanProductType,brokenStrategy,disbursement,collection,colendingFees,colendingCharges,overDueCharges,brokenInterestDayInMonthSelected,brokenInterestDayInYearSelected,
                disbursementAccountNumber,collectionAccountNumber,penalInvoiceOptions,multipleDisbursementOptions,trancheClubbingOptions,repaymentScheduleUpdateAllowedOptions,
                penalInvoice,multipleDisbursement,trancheClubbing,repaymentScheduleUpdateAllowed,transactionTypePreference,roundingModeSelected,isPennyDropEnabled,
                isBankDisbursementEnabled,servicerFeeInterestConfigEnabled,servicerFeeChargesConfigEnabled,enableDedupe,dedupeOptions,selectedDedupe,disbursementBankAccountNameOptions,
                selectedDisbursementBankAccountName,decimalPlacesRegexSelected,interestdecimalSelected,interestRoundingModeSelected,interestRegexSelected,emiMultiplesOf,emiMultiplesOfSelected,advanceAppropriationSelected,advanceEntryEnabledSelected,interestBenefitEnabled,
                foreclosureOnDueDateInterest,foreclosureOnDueDateCharge,foreclosureOtherThanDueDateInterest,foreclosureOtherThanDueDateCharge,foreclosureOneMonthOverdueInterest,foreclosureOneMonthOverdueCharge,foreclosureShortPaidInterest
                ,foreclosureShortPaidInterestCharge,foreclosurePrincipalShortPaidInterest,foreclosurePrincipalShortPaidCharge,foreclosureTwoMonthsOverdueInterest,foreclosureTwoMonthsOverdueCharge,foreclosurePosAdvanceOnDueDate,foreclosureAdvanceOnDueDateInterest
                ,foreclosureAdvanceOnDueDateCharge,foreclosurePosAdvanceOtherThanDueDate,foreclosureAdvanceAfterDueDateInterest,foreclosureAdvanceAfterDueDateCharge,foreclosureBackdatedShortPaidInterest,foreclosureBackdatedShortPaidInterestCharge,foreclosureBackdatedFullyPaidInterest
                ,foreclosureBackdatedFullyPaidInterestCharge,foreclosureBackdatedShortPaidPrincipalInterest,foreclosureBackdatedShortPaidPrincipalCharge,foreclosureBackdatedFullyPaidEmiInterest,foreclosureBackdatedFullyPaidEmiCharge
                ,foreclosureBackdatedAdvanceInterest,foreclosureBackdatedAdvanceCharge,advanceAppropriationAgainstOnSelected,emiDaysInMonthSelected,emiDaysInYearSelected,emiCalcSelected,enableBackDatedDisbursementSelect, foreclosureMethodTypesSelected,
				coolingOffApplicability,coolingOffThresholdDays,coolingOffInterestAndChargeApplicabilitySelected,coolingOffInterestLogicApplicabilitySelected,
                coolingOffDaysInYearSelected,coolingOffRoundingModeSelected,coolingOffRoundingDecimals);

    }

    public static LoanProductData withAccountingDetails(final LoanProductData productData, final Map<String, Object> accountingMappings,
                                                        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings,
                                                        final Collection<ChargeToGLAccountMapper> feeToGLAccountMappings,
                                                        final Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings) {
        productData.accountingMappings = accountingMappings;
        productData.paymentChannelToFundSourceMappings = paymentChannelToFundSourceMappings;
        productData.feeToIncomeAccountMappings = feeToGLAccountMappings;
        productData.penaltyToIncomeAccountMappings = penaltyToGLAccountMappings;
        return productData;
    }

    public LoanProductData(final Long id, final String name, final String shortName, final String loanAccNoPreference, final String description, final CurrencyData currency,
                           final BigDecimal principal, final BigDecimal minPrincipal, final BigDecimal maxPrincipal, final BigDecimal tolerance,
                           final Integer numberOfRepayments, final Integer minNumberOfRepayments, final Integer maxNumberOfRepayments,
                           final Integer repaymentEvery, final BigDecimal interestRatePerPeriod, final BigDecimal minInterestRatePerPeriod,
                           final BigDecimal maxInterestRatePerPeriod, final BigDecimal annualInterestRate, final EnumOptionData repaymentFrequencyType,
                           final EnumOptionData interestRateFrequencyType, final EnumOptionData amortizationType, final EnumOptionData interestType,
                           final EnumOptionData interestCalculationPeriodType, final Boolean allowPartialPeriodInterestCalcualtion, final Long fundId,
                           final String fundName, final Long transactionProcessingStrategyId, final String transactionProcessingStrategyName,
                           final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods,
                           final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final Collection<ChargeData> charges,
                           final EnumOptionData accountingType, final boolean includeInBorrowerCycle, boolean useBorrowerCycle, final LocalDate startDate,
                           final LocalDate closeDate, final String status, final String externalId,
                           Collection<LoanProductBorrowerCycleVariationData> principalVariations,
                           Collection<LoanProductBorrowerCycleVariationData> interestRateVariations,
                           Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations, Boolean multiDisburseLoan,
                           Integer maxTrancheCount, BigDecimal outstandingLoanBalance, final Boolean disallowExpectedDisbursements,
                           final Boolean allowApprovedDisbursedAmountsOverApplied, final String overAppliedCalculationType,
                           final Integer overAppliedNumber, final Integer graceOnArrearsAgeing, final Integer overdueDaysForNPA,
                           final EnumOptionData daysInMonthType, final EnumOptionData daysInYearType, final boolean isInterestRecalculationEnabled,
                           final LoanProductInterestRecalculationData interestRecalculationData,
                           final Integer minimumDaysBetweenDisbursalAndFirstRepayment, boolean holdGuaranteeFunds,
                           final LoanProductGuaranteeData loanProductGuaranteeData, final BigDecimal principalThresholdForLastInstallment,
                           final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion, boolean canDefineInstallmentAmount,
                           Integer installmentAmountInMultiplesOf, LoanProductConfigurableAttributes allowAttributeOverrides,
                           boolean isLinkedToFloatingInterestRates, Integer floatingRateId, String floatingRateName, BigDecimal interestRateDifferential,
                           BigDecimal minDifferentialLendingRate, BigDecimal defaultDifferentialLendingRate, BigDecimal maxDifferentialLendingRate,
                           boolean isFloatingInterestRateCalculationAllowed, final boolean isVariableInstallmentsAllowed,
                           final Integer minimumGapBetweenInstallments, final Integer maximumGapBetweenInstallments,
                           final boolean allowAgeLimits, final Integer minimumAge, final Integer maximumAge,
                           final boolean syncExpectedWithDisbursementDate, final boolean canUseForTopup, final boolean isEqualAmortization,
                           Collection<RateData> rateOptions, Collection<RateData> rates, final boolean isRatesEnabled,
                           final BigDecimal fixedPrincipalPercentagePerInstallment, final boolean allowApprovalOverAmountApplied,
                           final boolean overAmountDetails, final String brokenInterestCalculationPeriod, final String repaymentStrategyForNpaId, final String loanForeclosureStrategy,
                           final String brokenInterestStrategy, final boolean enableColendingLoan, final boolean byPercentageSplit, final Integer selfPrincipalShare, final Integer selfFeeShare,
                           final Integer selfPenaltyShare, final Integer selfOverpaidShares, final BigDecimal selfInterestRate, final Integer principalShare, final Integer feeShare, final Integer penaltyShare, final Integer overpaidShare, final BigDecimal interestRate,
                           final Long partnerId,
                           final boolean enableFeesWiseBifacation, final boolean enableChargeWiseBifacation, final boolean enableOverDue, final String selectCharge,
                           final Integer colendingCharge,
                           final Integer selfCharge, final Integer partnerCharge, final Boolean selectAcceptedDates, final String acceptedDateType, final Integer acceptedStartDate, final Integer acceptedEndDate, final Integer acceptedDate,
                           final boolean applyPrepaidLockingPeriod, final Integer prepayLockingPeriod, final boolean applyForeclosureLockingPeriod, final Integer foreclosureLockingPeriod, final Integer partnerPrincipalShare, final Integer partnerFeeShare, final Integer partnerPenaltyShare, final Integer partnerOverpaidShare, final BigDecimal partnerInterestRate,
                           final boolean useDaysInMonthForLoanProvisioning, final boolean divideByThirtyForPartialPeriod, final BigDecimal aumSlabRate, final BigDecimal gstLiabilityByVcpl, final BigDecimal gstLiabilityByPartner,
                           final BigDecimal vcplShareInBrokenInterest,
                           final BigDecimal partnerShareInBrokenInterest, final Integer monitoringTriggerPar30, final Integer monitoringTriggerPar90, final List<CodeValueData> frameWorkOptions, final List<CodeValueData> insuranceApplicabilityOptions, final List<CodeValueData> loanTypeOptions, final List<CodeValueData> fldgLogicOptions,
                           final List<CodeValueData> disbursementOptions, final List<CodeValueData> collectionOptions, final List<CodeValueData> assetClassOptions, final List<CodeValueData> loanProductClassOptions, final List<CodeValueData> loanProductTypeOptions, final CodeValueData loanType, final CodeValueData frameWork, final CodeValueData insuranceApplicability,
                           final CodeValueData fldgLogic, final CodeValueData assetClass,
                           final CodeValueData loanProductClass, final CodeValueData loanProductType, final EnumOptionData brokenStrategy ,
                           final EnumOptionData disbursement, final EnumOptionData collection, final Collection<LoanProductFeesChargesData> colendingFees,
                           final  Collection<LoanProductFeesChargesData> colendingCharges,
                           final Collection<LoanProductFeesChargesData> overDueCharges, final EnumOptionData brokenInterestDaysInMonthSelected, final EnumOptionData brokenInterestDaysInYearSelected,
                           final Long disbursementAccountNumber, final Long collectionAccountNumber, final List<CodeValueData> penalInvoiceOptions,
                           final List<CodeValueData> multipleDisbursementOptions, final List<CodeValueData> trancheClubbingOptions, final List<CodeValueData> repaymentScheduleUpdateAllowedOptions,
                           final CodeValueData penalInvoice, final CodeValueData multipleDisbursement, final CodeValueData trancheClubbing, final CodeValueData repaymentScheduleUpdateAllowed, final EnumOptionData transactionTypePreference, final String emiRoundingModeSelected,
                           final Boolean isPennyDropEnabled , final Boolean isBankDisbursementEnabled, final Boolean servicerFeeInterestConfigEnabled, final Boolean servicerFeeChargesConfigEnabled,
                           final boolean enableDedupe, final List<EnumOptionData> dedupeOptions, final EnumOptionData selectedDedupe, final List<CodeValueData> disbursementBankAccountNameOptions, final CodeValueData selectedDisbursementBankAccountName, final Integer emiDecimalRegexSelected, final Integer interestDecimalSelected,
                           final String interestRoundingModeSelected,final Integer interestDecimalRegexSelected, final Integer emiDecimalSelected,final Integer emiMultiplesOfSelected,final  EnumOptionData advanceAppropriationSelected,Boolean advanceEntryEnabledSelected,Boolean interestBenefitEnabled,
                           final EnumOptionData foreclosureOnDueDateInterestSelected,final EnumOptionData foreclosureOnDueDateChargeSelected,final EnumOptionData foreclosureOtherThanDueDateInterestSelected,final EnumOptionData foreclosureOtherThanDueDateChargeSelected,final EnumOptionData foreclosureOneMonthOverdueInterestSelected,final EnumOptionData foreclosureOneMonthOverdueChargeSelected,
                           final EnumOptionData foreclosureShortPaidInterestSelected,final EnumOptionData foreclosureShortPaidInterestChargeSelected,final EnumOptionData foreclosurePrincipalShortPaidInterestSelected,final EnumOptionData foreclosurePrincipalShortPaidChargeSelected,
                           final EnumOptionData foreclosureTwoMonthsOverdueInterestSelected,final EnumOptionData foreclosureTwoMonthsOverdueChargeSelected, final EnumOptionData foreclosurePosAdvanceOnDueDateSelected,final EnumOptionData foreclosureAdvanceOnDueDateInterestSelected,final EnumOptionData foreclosureAdvanceOnDueDateChargeSelected, final EnumOptionData foreclosurePosAdvanceOtherThanDueDateSelected ,final EnumOptionData foreclosureAdvanceAfterDueDateInterestSelected,
                           final EnumOptionData foreclosureAdvanceAfterDueDateChargeSelected,final EnumOptionData foreclosureBackdatedShortPaidInterestSelected,final EnumOptionData foreclosureBackdatedShortPaidInterestChargeSelected,final EnumOptionData foreclosureBackdatedFullyPaidInterestSelected,final EnumOptionData foreclosureBackdatedFullyPaidInterestChargeSelected,
                           final EnumOptionData foreclosureBackdatedShortPaidPrincipalInterestSelected,final EnumOptionData foreclosureBackdatedShortPaidPrincipalChargeSelected,final EnumOptionData foreclosureBackdatedFullyPaidEmiInterestSelected,final EnumOptionData foreclosureBackdatedFullyPaidEmiChargeSelected,
                           final EnumOptionData foreclosureBackdatedAdvanceInterestSelected,final EnumOptionData foreclosureBackdatedAdvanceChargeSelected,EnumOptionData advanceAppropriationAgainstOnSelected,
                           EnumOptionData emidaysInMonthSelected,EnumOptionData emidaysInYearTypeSelected,EnumOptionData emiCalcSelected,Boolean enableBackDatedDisbursementSelected, final EnumOptionData foreclosureMethodTypeSelected,
						   final Boolean coolingOffApplicability,final Integer coolingOffThresholdDays,final EnumOptionData coolingOffInterestAndChargeApplicabilitySelected,final EnumOptionData coolingOffInterestLogicApplicabilitySelected,
						   final EnumOptionData coolingOffDaysInYearSelected,final String coolingOffRoundingModeSelected,final Integer coolingOffRoundingDecimals){

        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.loanAccNoPreference = loanAccNoPreference;
        this.description = description;
        this.currency = currency;
        this.principal = principal;
        this.minPrincipal = minPrincipal;
        this.maxPrincipal = maxPrincipal;
        this.inArrearsTolerance = tolerance;
        this.numberOfRepayments = numberOfRepayments;
        this.minNumberOfRepayments = minNumberOfRepayments;
        this.maxNumberOfRepayments = maxNumberOfRepayments;
        this.graceOnPrincipalPayment = graceOnPrincipalPayment;
        this.recurringMoratoriumOnPrincipalPeriods = recurringMoratoriumOnPrincipalPeriods;
        this.graceOnInterestPayment = graceOnInterestPayment;
        this.graceOnInterestCharged = graceOnInterestCharged;
        this.repaymentEvery = repaymentEvery;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.minInterestRatePerPeriod = minInterestRatePerPeriod;
        this.maxInterestRatePerPeriod = maxInterestRatePerPeriod;
        this.annualInterestRate = annualInterestRate;
        this.isLinkedToFloatingInterestRates = isLinkedToFloatingInterestRates;
        this.floatingRateId = floatingRateId;
        this.floatingRateName = floatingRateName;
        this.interestRateDifferential = interestRateDifferential;
        this.minDifferentialLendingRate = minDifferentialLendingRate;
        this.defaultDifferentialLendingRate = defaultDifferentialLendingRate;
        this.maxDifferentialLendingRate = maxDifferentialLendingRate;
        this.isFloatingInterestRateCalculationAllowed = isFloatingInterestRateCalculationAllowed;
        this.allowVariableInstallments = isVariableInstallmentsAllowed;
        this.minimumGap = minimumGapBetweenInstallments;
        this.maximumGap = maximumGapBetweenInstallments;
        this.brokenInterestCalculationPeriod=brokenInterestCalculationPeriod;
//        this.repaymentStrategyForNpa=repaymentStrategyForNpa;
        this.repaymentStrategyForNpaId=repaymentStrategyForNpaId;
        this.loanForeclosureStrategy=loanForeclosureStrategy;
//        this.brokenInterestDaysInYears=brokenInterestDaysInYears;
//        this.brokenInterestDaysInMonth=brokenInterestDaysInMonth;
        this.brokenInterestStrategy=brokenInterestStrategy;
//
//        this.allowAgeCriteria = isAgeCriteriaAllowed;
//        this.minValue = minimumValue;
//        this.maxValue = maximumValue;

        this.allowAgeLimits = allowAgeLimits;
        this.minimumAge = minimumAge;
        this.maximumAge = maximumAge;


        this.repaymentFrequencyType = repaymentFrequencyType;
        this.interestRateFrequencyType = interestRateFrequencyType;
        this.amortizationType = amortizationType;
        this.interestType = interestType;
        this.interestCalculationPeriodType = interestCalculationPeriodType;
        this.allowPartialPeriodInterestCalcualtion = allowPartialPeriodInterestCalcualtion;
        this.fundId = fundId;
        this.fundName = fundName;
//        this.classId= classId;
//        this.typeId=typeId;
//        this.coBorrower=coBorrower;
//        this.eodBalance=eodBalance;
//        this.securedLoan=securedLoan;
//        this.nonEquatedInstallment=nonEquatedInstallment;
//
//        this.advanceEMI=advanceEMI;
//        this.termBasedOnLoanCycle=termBasedOnLoanCycle;
//        this.isNetOffApplied=isNetOffApplied;
        this.allowApprovalOverAmountApplied=allowApprovalOverAmountApplied;
        this.overAmountDetails=overAmountDetails;
        this.useDaysInMonthForLoanProvisioning=useDaysInMonthForLoanProvisioning;
        this.divideByThirtyForPartialPeriod=divideByThirtyForPartialPeriod;


        this.transactionProcessingStrategyId = transactionProcessingStrategyId;
        this.transactionProcessingStrategyName = transactionProcessingStrategyName;
        this.charges = charges;

        this.colendingFees=colendingFees;
        this.colendingCharges=colendingCharges;
        this.overDueCharges=overDueCharges;

        this.accountingRule = accountingType;
        this.includeInBorrowerCycle = includeInBorrowerCycle;
        this.useBorrowerCycle = useBorrowerCycle;
        this.startDate = startDate;
        this.closeDate = closeDate;
        this.status = status;
        this.externalId = externalId;
        this.minimumDaysBetweenDisbursalAndFirstRepayment = minimumDaysBetweenDisbursalAndFirstRepayment;
        this.rateOptions = rateOptions;
        this.rates = rates;
        this.isRatesEnabled = isRatesEnabled;

        this.enableColendingLoan=enableColendingLoan;
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
        this.partnerId=partnerId;
        this.enableFeesWiseBifacation=enableFeesWiseBifacation;
        this.enableChargeWiseBifacation=enableChargeWiseBifacation;
        this.enableOverDue =enableOverDue;
        this.selectCharge=selectCharge;
        this.colendingCharge=colendingCharge;
        this.selfCharge=selfCharge;
        this.partnerCharge=partnerCharge;
     //   this.vcplHurdleRate=vcplHurdleRate;

        this.selectAcceptedDates=selectAcceptedDates;
        this.acceptedDateType=acceptedDateType;
        this.acceptedStartDate=acceptedStartDate;
        this.acceptedEndDate=acceptedEndDate;
        this.acceptedDate=acceptedDate;

        this.applyPrepaidLockingPeriod=applyPrepaidLockingPeriod;
        this.prepayLockingPeriod=prepayLockingPeriod;

        this.applyForeclosureLockingPeriod=applyPrepaidLockingPeriod;
        this.foreclosureLockingPeriod=prepayLockingPeriod;


        this.chargeOptions = null;
        this.feeOption=null;
        this.penaltyOptions = null;
        this.paymentTypeOptions = null;
        this.currencyOptions = null;
        this.fundOptions = null;
//        this.classOptions=null;
//        this.typeOptions=null;
        this.partnerData=null;
        this.transactionProcessingStrategyOptions = null;
        this.amortizationTypeOptions = null;
        this.interestTypeOptions = null;
        this.interestCalculationPeriodTypeOptions = null;
        this.repaymentFrequencyTypeOptions = null;
        this.interestRateFrequencyTypeOptions = null;
        this.floatingRateOptions = null;

        this.accountingMappingOptions = null;
        this.accountingRuleOptions = null;
        this.accountingMappings = null;
        this.paymentChannelToFundSourceMappings = null;
        this.feeToIncomeAccountMappings = null;
        this.penaltyToIncomeAccountMappings = null;
        this.valueConditionTypeOptions = null;
        this.principalVariationsForBorrowerCycle = principalVariations;
        this.interestRateVariationsForBorrowerCycle = interestRateVariations;
        this.numberOfRepaymentVariationsForBorrowerCycle = numberOfRepaymentVariations;
        this.multiDisburseLoan = multiDisburseLoan;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.maxTrancheCount = maxTrancheCount;
        this.disallowExpectedDisbursements = disallowExpectedDisbursements;
        this.allowApprovedDisbursedAmountsOverApplied = allowApprovedDisbursedAmountsOverApplied;
        this.overAppliedCalculationType = overAppliedCalculationType;
        this.overAppliedNumber = overAppliedNumber;

        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
        this.overdueDaysForNPA = overdueDaysForNPA;
        this.daysInMonthType = daysInMonthType;
        this.daysInYearType = daysInYearType;
        this.isInterestRecalculationEnabled = isInterestRecalculationEnabled;
        this.interestRecalculationData = interestRecalculationData;
        this.holdGuaranteeFunds = holdGuaranteeFunds;
        this.productGuaranteeData = loanProductGuaranteeData;
        this.principalThresholdForLastInstallment = principalThresholdForLastInstallment;
        this.fixedPrincipalPercentagePerInstallment = fixedPrincipalPercentagePerInstallment;
        this.accountMovesOutOfNPAOnlyOnArrearsCompletion = accountMovesOutOfNPAOnlyOnArrearsCompletion;
        this.allowAttributeOverrides = allowAttributeOverrides;
        this.brokenStrategyId=brokenStrategy;
        this.disbursementId=disbursement;
        this.collectionId=collection;
        this.brokenInterestDaysInMonthSelected=brokenInterestDaysInMonthSelected;
        this.brokenInterestDaysInYearSelected=brokenInterestDaysInYearSelected;
        this.transactionTypePreference=transactionTypePreference;



        this.daysInMonthTypeOptions = null;
        this.daysInYearTypeOptions = null;
        this.interestRecalculationCompoundingTypeOptions = null;
        this.rescheduleStrategyTypeOptions = null;
        this.interestRecalculationFrequencyTypeOptions = null;
        this.interestRecalculationNthDayTypeOptions = null;
        this.interestRecalculationDayOfWeekTypeOptions = null;
        this.brokenStrategy=null;
        this.collectionOptions=null;
        this.disbursementOptions=null;
        this.brokenDaysInYearOptions=null;
        this.brokenDaysInMonthOptions=null;
        this.transactionTypeOptions=null;
        this.roundingModes = null;
        this.emimultiplesOf = null;



        this.canDefineInstallmentAmount = canDefineInstallmentAmount;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.preClosureInterestCalculationStrategyOptions = null;
        this.syncExpectedWithDisbursementDate = syncExpectedWithDisbursementDate;
        this.canUseForTopup = canUseForTopup;
        this.isEqualAmortization = isEqualAmortization;
        this.aumSlabRate=aumSlabRate;
        this.gstLiabilityByVcpl=gstLiabilityByVcpl;
        this.gstLiabilityByPartner=gstLiabilityByPartner;
        this.emiRoundingModeSelected = emiRoundingModeSelected;

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
        this.frameWorkOptions=null;
        this.insuranceApplicabilityOptions=null;
        this.loanTypeOptions=null;
        this.fldgLogicOptions=null;

        this.assetClassOptions=null;
        this.loanProductClassOptions=null;
        this.loanProductTypeOptions=null;
        this.loanType=loanType;
        this.frameWork=frameWork;
        this.insuranceApplicability=insuranceApplicability;
        this.fldgLogic=fldgLogic;

//        this.disbursement=disbursement;
//        this.collection=collection;
        this.assetClass=assetClass;
        this.loanProductClass=loanProductClass;
        this.loanProductType=loanProductType;
        this.disbursementAccountNumber = disbursementAccountNumber;
        this.collectionAccountNumber = collectionAccountNumber;
        this.penalInvoiceOptions = null;
        this.multipleDisbursementOptions = null;
        this.trancheClubbingOptions  = null;
        this.repaymentScheduleUpdateAllowedOptions = null;
        this.penalInvoice = penalInvoice;
        this.multipleDisbursement = multipleDisbursement;
        this.trancheClubbing = trancheClubbing;
        this.repaymentScheduleUpdateAllowed = repaymentScheduleUpdateAllowed;
        this.isPennyDropEnabled = isPennyDropEnabled;
        this.isBankDisbursementEnabled = isBankDisbursementEnabled;
        this.servicerFeeInterestConfigEnabled = servicerFeeInterestConfigEnabled;
        this.servicerFeeChargesConfigEnabled = servicerFeeChargesConfigEnabled;
        this.enableDedupe = enableDedupe;
        this.dedupeOptions = dedupeOptions;
        this.selectedDedupe = selectedDedupe;
        this.disbursementBankAccountNameOptions = disbursementBankAccountNameOptions;
        this.selectedDisbursementBankAccountName = selectedDisbursementBankAccountName;

        this.emiDecimalRegexSelected = emiDecimalRegexSelected;
        this.interestDecimalSelected = interestDecimalSelected;
        this.interestRoundingModeSelected = interestRoundingModeSelected;
        this.interestDecimalRegexSelected = interestDecimalRegexSelected;
        this.emiDecimalSelected = emiDecimalSelected;
        this.emiMultiplesOfSelected = emiMultiplesOfSelected;
        this.advanceAppropriationSelected = advanceAppropriationSelected;
        this.advanceEntryEnabledSelected = advanceEntryEnabledSelected;
        this.interestBenefitEnabled = interestBenefitEnabled;
        this.foreclosureOnDueDateInterestSelected = foreclosureOnDueDateInterestSelected;
        this.foreclosureOnDueDateChargeSelected = foreclosureOnDueDateChargeSelected;
        this.foreclosureOtherThanDueDateInterestSelected = foreclosureOtherThanDueDateInterestSelected;
        this.foreclosureOtherThanDueDateChargeSelected = foreclosureOtherThanDueDateChargeSelected;
        this.foreclosureOneMonthOverdueInterestSelected = foreclosureOneMonthOverdueInterestSelected;
        this.foreclosureOneMonthOverdueChargeSelected = foreclosureOneMonthOverdueChargeSelected;
        this.foreclosureShortPaidInterestSelected = foreclosureShortPaidInterestSelected;
        this.foreclosureShortPaidInterestChargeSelected = foreclosureShortPaidInterestChargeSelected;
        this.foreclosurePrincipalShortPaidInterestSelected = foreclosurePrincipalShortPaidInterestSelected;
        this.foreclosurePrincipalShortPaidChargeSelected = foreclosurePrincipalShortPaidChargeSelected;
        this.foreclosureTwoMonthsOverdueInterestSelected = foreclosureTwoMonthsOverdueInterestSelected;
        this.foreclosureTwoMonthsOverdueChargeSelected = foreclosureTwoMonthsOverdueChargeSelected;
        this.foreclosurePosAdvanceOnDueDateSelected = foreclosurePosAdvanceOnDueDateSelected;
        this.foreclosureAdvanceOnDueDateInterestSelected = foreclosureAdvanceOnDueDateInterestSelected;
        this.foreclosureAdvanceOnDueDateChargeSelected = foreclosureAdvanceOnDueDateChargeSelected;
        this.foreclosurePosAdvanceOtherThanDueDateSelected = foreclosurePosAdvanceOtherThanDueDateSelected;
        this.foreclosureAdvanceAfterDueDateInterestSelected = foreclosureAdvanceAfterDueDateInterestSelected;
        this.foreclosureAdvanceAfterDueDateChargeSelected = foreclosureAdvanceAfterDueDateChargeSelected;
        this.foreclosureBackdatedShortPaidInterestSelected = foreclosureBackdatedShortPaidInterestSelected;
        this.foreclosureBackdatedShortPaidInterestChargeSelected = foreclosureBackdatedShortPaidInterestChargeSelected;
        this.foreclosureBackdatedFullyPaidInterestSelected = foreclosureBackdatedFullyPaidInterestSelected;
        this.foreclosureBackdatedFullyPaidInterestChargeSelected = foreclosureBackdatedFullyPaidInterestChargeSelected;
        this.foreclosureBackdatedShortPaidPrincipalInterestSelected = foreclosureBackdatedShortPaidPrincipalInterestSelected;
        this.foreclosureBackdatedShortPaidPrincipalChargeSelected = foreclosureBackdatedShortPaidPrincipalChargeSelected;
        this.foreclosureBackdatedFullyPaidEmiInterestSelected = foreclosureBackdatedFullyPaidEmiInterestSelected;
        this.foreclosureBackdatedFullyPaidEmiChargeSelected = foreclosureBackdatedFullyPaidEmiChargeSelected;
        this.foreclosureBackdatedAdvanceInterestSelected = foreclosureBackdatedAdvanceInterestSelected;
        this.foreclosureBackdatedAdvanceChargeSelected = foreclosureBackdatedAdvanceChargeSelected;

        this.advanceAppropriationAgainstOnSelected = advanceAppropriationAgainstOnSelected;
        this.emiDaysInMonthSelected =emidaysInMonthSelected;
        this.emiDaysInYearSelected = emidaysInYearTypeSelected;
        this.emiCalcSelected = emiCalcSelected;
        this.enableBackDatedDisbursementSelected = enableBackDatedDisbursementSelected;
        this.foreclosureMethodTypeSelected = foreclosureMethodTypeSelected;
		this.coolingOffApplicability = coolingOffApplicability;
        this.coolingOffThresholdDays = coolingOffThresholdDays;
        this.coolingOffInterestAndChargeApplicabilitySelected = coolingOffInterestAndChargeApplicabilitySelected;
        this.coolingOffInterestLogicApplicabilitySelected = coolingOffInterestLogicApplicabilitySelected;
        this.coolingOffDaysInYearSelected = coolingOffDaysInYearSelected;
        this.coolingOffRoundingModeSelected = coolingOffRoundingModeSelected;
        this.coolingOffRoundingDecimals = coolingOffRoundingDecimals;
    }

    public LoanProductData(final LoanProductData productData, final Collection<ChargeData> chargeOptions,
                           final Collection<ChargeData> penaltyOptions, final Collection<PaymentTypeData> paymentTypeOptions,
                           final Collection<CurrencyData> currencyOptions, final List<EnumOptionData> amortizationTypeOptions,
                           final List<EnumOptionData> interestTypeOptions, final List<EnumOptionData> interestCalculationPeriodTypeOptions,
                           final List<EnumOptionData> repaymentFrequencyTypeOptions, final List<EnumOptionData> interestRateFrequencyTypeOptions,
                           final Collection<FundData> fundOptions, Collection<TransactionProcessingStrategyData> transactionStrategyOptions,
                           final Collection<RateData> rateOptions, final Map<String, List<GLAccountData>> accountingMappingOptions,
                           final List<EnumOptionData> accountingRuleOptions, final List<EnumOptionData> valueConditionTypeOptions,
                           final List<EnumOptionData> daysInMonthTypeOptions, final List<EnumOptionData> daysInYearTypeOptions,
                           final List<EnumOptionData> interestRecalculationCompoundingTypeOptions,
                           final List<EnumOptionData> rescheduleStrategyTypeOptions, final List<EnumOptionData> interestRecalculationFrequencyTypeOptions,
                           final List<EnumOptionData> preCloseInterestCalculationStrategyOptions, final List<FloatingRateData> floatingRateOptions,
                           final List<EnumOptionData> interestRecalculationNthDayTypeOptions,
                           final List<EnumOptionData> interestRecalculationDayOfWeekTypeOptions, final boolean isRatesEnabled,
                           final Collection<PartnerData> partnerData, final List<CodeValueData> frameWorkOptions, final List<CodeValueData> insuranceApplicabilityOptions,
                           final List<CodeValueData> loanTypeOptions, final List<CodeValueData> fldgLogicOptions,
                           final List<CodeValueData> assetClassOptions, final List<CodeValueData> loanProductClassOptions,
                           final List<CodeValueData> loanProductTypeOptions, final Collection<ChargeData> feeOption, final List<EnumOptionData> brokenStrategy,
                           final List<EnumOptionData> disbursementOptions, final List <EnumOptionData> collectionOptions, final List<EnumOptionData> brokenDaysInYearOptions, final List<EnumOptionData> brokeDaysInMonthOptions,
                           final List<CodeValueData> penalInvoiceOptions, final List<CodeValueData> multipleDisbursementOptions, final List<CodeValueData> trancheClubbingOptions,
                           final List<CodeValueData> repaymentScheduleUpdateAllowedOptions, final List<EnumOptionData> transactionTypeOptions, final List<RoundingMode> roundingModes,
                           final List<CodeValueData> disbursementBankAccNameOptions, final List<EnumOptionData> emimultiplesOf,List<EnumOptionData> advanceAppropriations,
                           List<EnumOptionData> foreclosurePosCalculation,List<EnumOptionData> servicerFeeChargesRatioOptions,List<EnumOptionData> advanceAppropriationAgainstOn,List<EnumOptionData> emiCalcusEnum,
                           final List<EnumOptionData> foreclosureMethodType,List<EnumOptionData> coolingOffInterestAndChargeApplicability,List<EnumOptionData> coolingOffInterestLogicApplicability) {
        this.id = productData.id;
        this.name = productData.name;
        this.shortName = productData.shortName;
        this.loanAccNoPreference = productData.loanAccNoPreference;
        this.description = productData.description;
        this.fundId = productData.fundId;
        this.fundName = productData.fundName;
//        this.classId = productData.classId;
//        this.typeId = productData.typeId;

//        this.coBorrower=productData.coBorrower;
//        this.eodBalance=productData.eodBalance;
//        this.securedLoan=productData.securedLoan;
//        this.nonEquatedInstallment=productData.nonEquatedInstallment;
//
//        this.advanceEMI=productData.advanceEMI;
//        this.termBasedOnLoanCycle=productData.termBasedOnLoanCycle;
//        this.isNetOffApplied=productData.isNetOffApplied;
        this.allowApprovalOverAmountApplied=productData.allowApprovalOverAmountApplied;
        this.overAmountDetails=productData.overAmountDetails;
        this.useDaysInMonthForLoanProvisioning=productData.useDaysInMonthForLoanProvisioning;
        this.divideByThirtyForPartialPeriod=productData.divideByThirtyForPartialPeriod;


        this.principal = productData.principal;
        this.minPrincipal = productData.minPrincipal;
        this.maxPrincipal = productData.maxPrincipal;
        this.inArrearsTolerance = productData.inArrearsTolerance;
        this.numberOfRepayments = productData.numberOfRepayments;
        this.minNumberOfRepayments = productData.minNumberOfRepayments;
        this.maxNumberOfRepayments = productData.maxNumberOfRepayments;
        this.repaymentEvery = productData.repaymentEvery;
        this.interestRatePerPeriod = productData.interestRatePerPeriod;
        this.minInterestRatePerPeriod = productData.minInterestRatePerPeriod;
        this.maxInterestRatePerPeriod = productData.maxInterestRatePerPeriod;
        this.annualInterestRate = productData.annualInterestRate;
        this.isLinkedToFloatingInterestRates = productData.isLinkedToFloatingInterestRates;
        this.floatingRateId = productData.floatingRateId;
        this.floatingRateName = productData.floatingRateName;
        this.interestRateDifferential = productData.interestRateDifferential;
        this.minDifferentialLendingRate = productData.minDifferentialLendingRate;
        this.defaultDifferentialLendingRate = productData.defaultDifferentialLendingRate;
        this.maxDifferentialLendingRate = productData.maxDifferentialLendingRate;
        this.isFloatingInterestRateCalculationAllowed = productData.isFloatingInterestRateCalculationAllowed;
        this.allowVariableInstallments = productData.allowVariableInstallments;
        this.minimumGap = productData.minimumGap;
        this.maximumGap = productData.maximumGap;
        this.allowAgeLimits = productData.allowAgeLimits;
        this.minimumAge = productData.minimumAge;
        this.maximumAge = productData.maximumAge;



        this.brokenInterestCalculationPeriod = productData.brokenInterestCalculationPeriod;
//       this.repaymentStrategyForNpa = productData.repaymentStrategyForNpa;
        this.repaymentStrategyForNpaId = productData.repaymentStrategyForNpaId;
        this.loanForeclosureStrategy = productData.loanForeclosureStrategy;
//        this.brokenInterestDaysInYears = productData.brokenInterestDaysInYears;
//        this.brokenInterestDaysInMonth = productData.brokenInterestDaysInMonth;
        this.brokenInterestStrategy = productData.brokenInterestStrategy;

        this.enableColendingLoan=productData.enableColendingLoan;
        this.byPercentageSplit=productData.byPercentageSplit;
        this.selfPrincipalShare=productData.selfPrincipalShare;
        this.selfFeeShare=productData.selfFeeShare;
        this.selfPenaltyShare=productData.selfPenaltyShare;
        this.selfOverpaidShares=productData.selfOverpaidShares;
        this.selfInterestRate=productData.selfInterestRate;
        this.principalShare=productData.principalShare;
        this.feeShare=productData.feeShare;
        this.penaltyShare=productData.penaltyShare;
        this.overpaidShare=productData.overpaidShare;
        this.interestRate=productData.interestRate;
        this.partnerPrincipalShare=productData.partnerPrincipalShare;
        this.partnerFeeShare=productData.partnerFeeShare;
        this.partnerPenaltyShare=productData.partnerPenaltyShare;
        this.partnerOverpaidShare=productData.partnerOverpaidShare;
        this.partnerInterestRate=productData.partnerInterestRate;
        this.partnerId=productData.partnerId;
        this.enableFeesWiseBifacation=productData.enableFeesWiseBifacation;
        this.enableChargeWiseBifacation=productData.enableChargeWiseBifacation;
        this.enableOverDue = productData.enableOverDue;
        this.selectCharge=productData.selectCharge;
        this.colendingCharge=productData.colendingCharge;
        this.selfCharge=productData.selfCharge;
        this.partnerCharge=productData.partnerCharge;
    //    this.vcplHurdleRate=productData.vcplHurdleRate;

        this.selectAcceptedDates=productData.selectAcceptedDates;

        this.applyPrepaidLockingPeriod=productData.applyPrepaidLockingPeriod;
        this.prepayLockingPeriod=productData.prepayLockingPeriod;
        this.applyForeclosureLockingPeriod=productData.applyForeclosureLockingPeriod;
        this.foreclosureLockingPeriod=productData.foreclosureLockingPeriod;
        this.aumSlabRate=productData.aumSlabRate;
        this.gstLiabilityByVcpl=productData.gstLiabilityByVcpl;
        this.gstLiabilityByPartner=productData.gstLiabilityByPartner;
        this.emiRoundingModeSelected = productData.emiRoundingModeSelected;

//        this.vcplShareInPf=productData.vcplShareInPf;
//        this.partnerShareInPf=productData.partnerShareInPf;
//        this.vcplShareInPenalInterest=productData.vcplShareInPenalInterest;
//        this.partnerShareInPenalInterest=productData.partnerShareInPenalInterest;
        this.vcplShareInBrokenInterest=productData.vcplShareInBrokenInterest;
        this.partnerShareInBrokenInterest=productData.partnerShareInBrokenInterest;
//        this.vcplShareInForeclosureCharges=productData.vcplShareInForeclosureCharges;
//        this.partnerShareInForeclosureCharges=productData.partnerShareInForeclosureCharges;
//        this.vcplShareInOtherCharges=productData.vcplShareInOtherCharges;
//        this.partnerShareInOtherCharges=productData.partnerShareInOtherCharges;
        this.monitoringTriggerPar30=productData.monitoringTriggerPar30;
        this.monitoringTriggerPar90=productData.monitoringTriggerPar90;

        this.repaymentFrequencyType = productData.repaymentFrequencyType;
        this.interestRateFrequencyType = productData.interestRateFrequencyType;
        this.amortizationType = productData.amortizationType;
        this.interestType = productData.interestType;
        this.interestCalculationPeriodType = productData.interestCalculationPeriodType;
        this.allowPartialPeriodInterestCalcualtion = productData.allowPartialPeriodInterestCalcualtion;
        this.startDate = productData.startDate;
        this.closeDate = productData.closeDate;
        this.status = productData.status;
        this.externalId = productData.externalId;

        this.charges = nullIfEmpty(productData.charges());
        this.colendingFees=returnNullIfEmpty(productData.colendingFees());
        this.colendingCharges=returnNullIfEmpty(productData.colendingCharges());
        this.principalVariationsForBorrowerCycle = productData.principalVariationsForBorrowerCycle;
        this.interestRateVariationsForBorrowerCycle = productData.interestRateVariationsForBorrowerCycle;
        this.numberOfRepaymentVariationsForBorrowerCycle = productData.numberOfRepaymentVariationsForBorrowerCycle;
        this.accountingRule = productData.accountingRule;
        this.accountingMappings = productData.accountingMappings;
        this.paymentChannelToFundSourceMappings = productData.paymentChannelToFundSourceMappings;
        this.feeToIncomeAccountMappings = productData.feeToIncomeAccountMappings;
        this.penaltyToIncomeAccountMappings = productData.penaltyToIncomeAccountMappings;
        this.overDueCharges =productData.overDueCharges;

        this.chargeOptions = chargeOptions;
        this.feeOption=feeOption;
        this.penaltyOptions = penaltyOptions;
        this.paymentTypeOptions = paymentTypeOptions;
        this.currencyOptions = currencyOptions;
        this.currency = productData.currency;
        this.fundOptions = fundOptions;
        this.partnerData=partnerData;
        this.frameWorkOptions=frameWorkOptions;
        this.insuranceApplicabilityOptions=insuranceApplicabilityOptions;
        this.loanTypeOptions=loanTypeOptions;
        this.fldgLogicOptions=fldgLogicOptions;
        this.assetClassOptions=assetClassOptions;
        this.loanProductClassOptions=loanProductClassOptions;
        this.loanProductTypeOptions=loanProductTypeOptions;
        this.loanType=productData.loanType;
        this.frameWork=productData.frameWork;
        this.insuranceApplicability=productData.insuranceApplicability;
        this.fldgLogic=productData.fldgLogic;

//        this.disbursement=productData.disbursement;
//        this.collection=productData.collection;
        this.assetClass=productData.assetClass;
        this.loanProductClass=productData.loanProductClass;
        this.loanProductType=productData.loanProductType;

        this.transactionProcessingStrategyOptions = transactionStrategyOptions;
        this.rateOptions = rateOptions;
        this.floatingRateOptions = floatingRateOptions;
        if (this.transactionProcessingStrategyOptions != null && this.transactionProcessingStrategyOptions.size() == 1) {
            final List<TransactionProcessingStrategyData> listOfOptions = new ArrayList<>(this.transactionProcessingStrategyOptions);

            this.transactionProcessingStrategyId = listOfOptions.get(0).id();
            this.transactionProcessingStrategyName = listOfOptions.get(0).name();
        } else {
            this.transactionProcessingStrategyId = productData.transactionProcessingStrategyId;
            this.transactionProcessingStrategyName = productData.transactionProcessingStrategyName;
        }

        this.graceOnPrincipalPayment = productData.graceOnPrincipalPayment;
        this.recurringMoratoriumOnPrincipalPeriods = productData.recurringMoratoriumOnPrincipalPeriods;
        this.graceOnInterestPayment = productData.graceOnInterestPayment;
        this.graceOnInterestCharged = productData.graceOnInterestCharged;
        this.includeInBorrowerCycle = productData.includeInBorrowerCycle;
        this.useBorrowerCycle = productData.useBorrowerCycle;
        this.multiDisburseLoan = productData.multiDisburseLoan;
        this.maxTrancheCount = productData.maxTrancheCount;
        this.outstandingLoanBalance = productData.outstandingLoanBalance;
        this.disallowExpectedDisbursements = productData.disallowExpectedDisbursements;
        this.allowApprovedDisbursedAmountsOverApplied = productData.allowApprovedDisbursedAmountsOverApplied;
        this.overAppliedCalculationType = productData.overAppliedCalculationType;
        this.overAppliedNumber = productData.overAppliedNumber;

        this.minimumDaysBetweenDisbursalAndFirstRepayment = productData.minimumDaysBetweenDisbursalAndFirstRepayment;

        this.amortizationTypeOptions = amortizationTypeOptions;
        this.interestTypeOptions = interestTypeOptions;
        this.interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions;
        this.interestRecalculationNthDayTypeOptions = interestRecalculationNthDayTypeOptions;
        this.interestRecalculationDayOfWeekTypeOptions = interestRecalculationDayOfWeekTypeOptions;
        this.repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions;
        this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;

        this.accountingMappingOptions = accountingMappingOptions;
        this.accountingRuleOptions = accountingRuleOptions;
        this.valueConditionTypeOptions = valueConditionTypeOptions;
        this.graceOnArrearsAgeing = productData.graceOnArrearsAgeing;
        this.overdueDaysForNPA = productData.overdueDaysForNPA;

        this.daysInMonthType = productData.daysInMonthType;
        this.brokenStrategyId=productData.brokenStrategyId;
        this.disbursementId=productData.disbursementId;
        this.collectionId=productData.collectionId;
        this.brokenInterestDaysInMonthSelected=productData.brokenInterestDaysInMonthSelected;
        this.brokenInterestDaysInYearSelected=productData.brokenInterestDaysInYearSelected;
        this.daysInYearType = productData.daysInYearType;
        this.isInterestRecalculationEnabled = productData.isInterestRecalculationEnabled;
        this.interestRecalculationData = productData.interestRecalculationData;
        this.holdGuaranteeFunds = productData.holdGuaranteeFunds;
        this.productGuaranteeData = productData.productGuaranteeData;
        this.principalThresholdForLastInstallment = productData.principalThresholdForLastInstallment;
        this.fixedPrincipalPercentagePerInstallment = productData.fixedPrincipalPercentagePerInstallment;
        this.accountMovesOutOfNPAOnlyOnArrearsCompletion = productData.accountMovesOutOfNPAOnlyOnArrearsCompletion;
        this.transactionTypePreference=productData.transactionTypePreference;
        this.daysInMonthTypeOptions = daysInMonthTypeOptions;
        this.daysInYearTypeOptions = daysInYearTypeOptions;
        this.interestRecalculationCompoundingTypeOptions = interestRecalculationCompoundingTypeOptions;
        this.rescheduleStrategyTypeOptions = rescheduleStrategyTypeOptions;
        this.allowAttributeOverrides = productData.allowAttributeOverrides;
        this.disbursementOptions=disbursementOptions;
        this.brokenStrategy=brokenStrategy;
        this.collectionOptions=collectionOptions;
        this.brokenDaysInYearOptions=brokenDaysInYearOptions;
        this.brokenDaysInMonthOptions=brokeDaysInMonthOptions;
        this.transactionTypeOptions=transactionTypeOptions;
        this.roundingModes =roundingModes;
        this.emimultiplesOf = emimultiplesOf;

        if (CollectionUtils.isEmpty(interestRecalculationFrequencyTypeOptions)) {
            this.interestRecalculationFrequencyTypeOptions = null;
        } else {
            this.interestRecalculationFrequencyTypeOptions = interestRecalculationFrequencyTypeOptions;
        }
        this.canDefineInstallmentAmount = productData.canDefineInstallmentAmount;
        this.installmentAmountInMultiplesOf = productData.installmentAmountInMultiplesOf;
        this.preClosureInterestCalculationStrategyOptions = preCloseInterestCalculationStrategyOptions;
        this.syncExpectedWithDisbursementDate = productData.syncExpectedWithDisbursementDate;
        this.canUseForTopup = productData.canUseForTopup;
        this.isEqualAmortization = productData.isEqualAmortization;
        this.rates = productData.rates;
        this.isRatesEnabled = isRatesEnabled;
        this.acceptedDateType = productData.acceptedDateType;
        this.acceptedStartDate=productData.acceptedStartDate;
        this.acceptedEndDate=productData.acceptedEndDate;
        this.acceptedDate=productData.acceptedDate;
        this.disbursementAccountNumber = productData.disbursementAccountNumber;
        this.collectionAccountNumber = productData.collectionAccountNumber;
        this.penalInvoiceOptions = penalInvoiceOptions;
        this.multipleDisbursementOptions = multipleDisbursementOptions;
        this.trancheClubbingOptions = trancheClubbingOptions;
        this.repaymentScheduleUpdateAllowedOptions = repaymentScheduleUpdateAllowedOptions;
        this.penalInvoice = productData.penalInvoice;
        this.multipleDisbursement = productData.multipleDisbursement;
        this.trancheClubbing = productData.trancheClubbing;
        this.repaymentScheduleUpdateAllowed = productData.repaymentScheduleUpdateAllowed;
        this.isPennyDropEnabled = productData.isPennyDropEnabled;
        this.isBankDisbursementEnabled = productData.isBankDisbursementEnabled;
        this.servicerFeeInterestConfigEnabled = productData.servicerFeeInterestConfigEnabled;
        this.servicerFeeChargesConfigEnabled = productData.servicerFeeChargesConfigEnabled;
        this.enableDedupe = productData.enableDedupe;
        this.dedupeOptions = productData.dedupeOptions;
        this.selectedDedupe = productData.selectedDedupe;
        this.selectedDisbursementBankAccountName = productData.selectedDisbursementBankAccountName;
        this.disbursementBankAccountNameOptions = disbursementBankAccNameOptions;
        this.emiDecimalRegexSelected = productData.emiDecimalRegexSelected;
        this.interestDecimalRegexSelected = productData.interestDecimalRegexSelected;
        this.interestRoundingModeSelected = productData.interestRoundingModeSelected;
        this. interestDecimalSelected = productData.interestDecimalSelected;
        this.emiDecimalSelected = productData.emiDecimalSelected;
        this.emiMultiplesOfSelected = productData.emiMultiplesOfSelected;
        this.advanceAppropriations = advanceAppropriations;
        this.advanceAppropriationSelected = productData.advanceAppropriationSelected;
        this.advanceEntryEnabledSelected = productData.advanceEntryEnabledSelected;
        this.servicerFeeChargesRatioOptions = servicerFeeChargesRatioOptions;
        this.interestBenefitEnabled = productData.interestBenefitEnabled;
        this.foreclosurePosCalculation = foreclosurePosCalculation;
        this.foreclosureOnDueDateInterestSelected = productData.foreclosureOnDueDateInterestSelected;
        this.foreclosureOnDueDateChargeSelected = productData.foreclosureOnDueDateChargeSelected;
        this.foreclosureOtherThanDueDateInterestSelected = productData.foreclosureOtherThanDueDateInterestSelected;
        this.foreclosureOtherThanDueDateChargeSelected = productData.foreclosureOtherThanDueDateChargeSelected;
        this.foreclosureOneMonthOverdueInterestSelected = productData.foreclosureOneMonthOverdueInterestSelected;
        this.foreclosureOneMonthOverdueChargeSelected = productData.foreclosureOneMonthOverdueChargeSelected;
        this.foreclosureShortPaidInterestSelected = productData.foreclosureShortPaidInterestSelected;
        this.foreclosureShortPaidInterestChargeSelected = productData.foreclosureShortPaidInterestChargeSelected;
        this.foreclosurePrincipalShortPaidInterestSelected = productData.foreclosurePrincipalShortPaidInterestSelected;
        this.foreclosurePrincipalShortPaidChargeSelected = productData.foreclosurePrincipalShortPaidChargeSelected;
        this.foreclosureTwoMonthsOverdueInterestSelected = productData.foreclosureTwoMonthsOverdueInterestSelected;
        this.foreclosureTwoMonthsOverdueChargeSelected = productData.foreclosureTwoMonthsOverdueChargeSelected;
        this.foreclosurePosAdvanceOnDueDateSelected = productData.foreclosurePosAdvanceOnDueDateSelected;
        this.foreclosureAdvanceOnDueDateInterestSelected = productData.foreclosureAdvanceOnDueDateInterestSelected;
        this.foreclosureAdvanceOnDueDateChargeSelected = productData.foreclosureAdvanceOnDueDateChargeSelected;
        this.foreclosurePosAdvanceOtherThanDueDateSelected = productData.foreclosurePosAdvanceOtherThanDueDateSelected;
        this.foreclosureAdvanceAfterDueDateInterestSelected = productData.foreclosureAdvanceAfterDueDateInterestSelected;
        this.foreclosureAdvanceAfterDueDateChargeSelected = productData.foreclosureAdvanceAfterDueDateChargeSelected;
        this.foreclosureBackdatedShortPaidInterestSelected = productData.foreclosureBackdatedShortPaidInterestSelected;
        this.foreclosureBackdatedShortPaidInterestChargeSelected = productData.foreclosureBackdatedShortPaidInterestChargeSelected;
        this.foreclosureBackdatedFullyPaidInterestSelected = productData.foreclosureBackdatedFullyPaidInterestSelected;
        this.foreclosureBackdatedFullyPaidInterestChargeSelected = productData.foreclosureBackdatedFullyPaidInterestChargeSelected;
        this.foreclosureBackdatedShortPaidPrincipalInterestSelected = productData.foreclosureBackdatedShortPaidPrincipalInterestSelected;
        this.foreclosureBackdatedShortPaidPrincipalChargeSelected = productData.foreclosureBackdatedShortPaidPrincipalChargeSelected;
        this.foreclosureBackdatedFullyPaidEmiInterestSelected = productData.foreclosureBackdatedFullyPaidEmiInterestSelected;
        this.foreclosureBackdatedFullyPaidEmiChargeSelected = productData.foreclosureBackdatedFullyPaidEmiChargeSelected;
        this.foreclosureBackdatedAdvanceInterestSelected = productData.foreclosureBackdatedAdvanceInterestSelected;
        this.foreclosureBackdatedAdvanceChargeSelected = productData.foreclosureBackdatedAdvanceChargeSelected;
		this.advanceAppropriationAgainstOn = advanceAppropriationAgainstOn;
        this.emiCalcEnum = emiCalcusEnum;
        this.emiCalcSelected =productData.emiCalcSelected;
        this.emiDaysInYearSelected = productData.emiDaysInYearSelected;
        this.emiDaysInMonthSelected = productData.emiDaysInMonthSelected;
        this.enableBackDatedDisbursementSelected = productData.enableBackDatedDisbursementSelected;
        this.advanceAppropriationAgainstOnSelected = productData.advanceAppropriationAgainstOnSelected;
        this.foreclosureMethodType = foreclosureMethodType;
        this.foreclosureMethodTypeSelected = productData.foreclosureMethodTypeSelected;
		this.coolingOffApplicability = productData.coolingOffApplicability;
        this.coolingOffThresholdDays = productData.coolingOffThresholdDays;
        this.coolingOffInterestAndChargeApplicability = coolingOffInterestAndChargeApplicability;
        this.coolingOffInterestAndChargeApplicabilitySelected = productData.coolingOffInterestAndChargeApplicabilitySelected;
        this.coolingOffInterestLogicApplicability = coolingOffInterestLogicApplicability;
        this.coolingOffInterestLogicApplicabilitySelected = productData.coolingOffInterestLogicApplicabilitySelected;
        this.coolingOffDaysInYearSelected = productData.coolingOffDaysInYearSelected;
        this.coolingOffRoundingModeSelected = productData.coolingOffRoundingModeSelected;
        this.coolingOffRoundingDecimals = productData.coolingOffRoundingDecimals;
    }

    private Collection<ChargeData> nullIfEmpty(final Collection<ChargeData> charges) {
        Collection<ChargeData> chargesLocal = charges;
        if (charges == null || charges.isEmpty()) {
            chargesLocal = null;
        }
        return chargesLocal;
    }

    private Collection<LoanProductFeesChargesData> returnNullIfEmpty(final Collection<LoanProductFeesChargesData> charges) {
        Collection<LoanProductFeesChargesData> chargesLocal = charges;
        if (charges == null || charges.isEmpty()) {
            chargesLocal = null;
        }
        return chargesLocal;
    }

    public Collection<ChargeData> charges() {
        Collection<ChargeData> chargesLocal = new ArrayList<>();
        if (this.charges != null) {
            chargesLocal = this.charges;
        }
        return chargesLocal;
    }
    public Collection<LoanProductFeesChargesData> colendingCharges() {
        Collection<LoanProductFeesChargesData> chargesLocal = new ArrayList<>();
        if (this.colendingCharges != null) {
            chargesLocal = this.colendingCharges;
        }
        return chargesLocal;
    }

    public Collection<LoanProductFeesChargesData> colendingFees() {
        Collection<LoanProductFeesChargesData> chargesLocal = new ArrayList<>();
        if (this.colendingFees != null) {
            chargesLocal = this.colendingFees;
        }
        return chargesLocal;
    }

    public EnumOptionData accountingRuleType() {
        return this.accountingRule;
    }

    public boolean hasAccountingEnabled() {
        return this.accountingRule.getId() > AccountingRuleType.NONE.getValue();
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Long getFundId() {
        return this.fundId;
    }

    public String getFundName() {
        return this.fundName;
    }

//    public Long getClassId() {
//        return this.classId;
//    }
//
//    public Long getTypeId() {
//        return this.typeId;
//    }

    public Long getTransactionProcessingStrategyId() {
        return this.transactionProcessingStrategyId;
    }

    public String getTransactionProcessingStrategyName() {
        return this.transactionProcessingStrategyName;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public BigDecimal getMinPrincipal() {
        return this.minPrincipal;
    }

    public BigDecimal getMaxPrincipal() {
        return this.maxPrincipal;
    }

    public BigDecimal getInArrearsTolerance() {
        return this.inArrearsTolerance;
    }

    public Integer getNumberOfRepayments() {
        return this.numberOfRepayments;
    }

    public Integer getRepaymentEvery() {
        return this.repaymentEvery;
    }

    public BigDecimal getInterestRatePerPeriod() {
        return this.interestRatePerPeriod;
    }

    public BigDecimal getAnnualInterestRate() {
        return this.annualInterestRate;
    }

    public EnumOptionData getRepaymentFrequencyType() {
        return this.repaymentFrequencyType;
    }

    public Integer getGraceOnPrincipalPayment() {
        return this.graceOnPrincipalPayment;
    }

    public Integer getRecurringMoratoriumOnPrincipalPeriods() {
        return this.recurringMoratoriumOnPrincipalPeriods;
    }

    public Integer getGraceOnInterestPayment() {
        return this.graceOnInterestPayment;
    }

    public Integer getGraceOnInterestCharged() {
        return this.graceOnInterestCharged;
    }

    public EnumOptionData getInterestRateFrequencyType() {
        return this.interestRateFrequencyType;
    }

    public EnumOptionData getAmortizationType() {
        return this.amortizationType;
    }

    public EnumOptionData getInterestType() {
        return this.interestType;
    }

    public EnumOptionData getInterestCalculationPeriodType() {
        return this.interestCalculationPeriodType;
    }

    public Collection<FundData> getFundOptions() {
        return this.fundOptions;
    }

    public Collection<PartnerData> getPartnerData() { return this.partnerData; }

    public List<EnumOptionData> getAmortizationTypeOptions() {
        return this.amortizationTypeOptions;
    }

    public List<EnumOptionData> getInterestTypeOptions() {
        return this.interestTypeOptions;
    }

    public List<EnumOptionData> getInterestCalculationPeriodTypeOptions() {
        return this.interestCalculationPeriodTypeOptions;
    }

    public List<EnumOptionData> getRepaymentFrequencyTypeOptions() {
        return this.repaymentFrequencyTypeOptions;
    }

    public List<EnumOptionData> getInterestRateFrequencyTypeOptions() {
        return this.interestRateFrequencyTypeOptions;
    }

    public Collection<ChargeData> getChargeOptions() {
        return this.chargeOptions;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof LoanProductData)) {
            return false;
        }
        final LoanProductData loanProductData = (LoanProductData) obj;
        return loanProductData.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean useBorrowerCycle() {
        return this.useBorrowerCycle;
    }

    public Collection<LoanProductBorrowerCycleVariationData> getPrincipalVariationsForBorrowerCycle() {
        return this.principalVariationsForBorrowerCycle;
    }

    public Collection<LoanProductBorrowerCycleVariationData> getInterestRateVariationsForBorrowerCycle() {
        return this.interestRateVariationsForBorrowerCycle;
    }

    public Collection<LoanProductBorrowerCycleVariationData> getNumberOfRepaymentVariationsForBorrowerCycle() {
        return this.numberOfRepaymentVariationsForBorrowerCycle;
    }

    public Boolean getMultiDisburseLoan() {
        return this.multiDisburseLoan;
    }

    public BigDecimal getOutstandingLoanBalance() {
        return this.outstandingLoanBalance;
    }

    public Integer getGraceOnArrearsAgeing() {
        return this.graceOnArrearsAgeing;
    }



    public boolean isInterestRecalculationEnabled() {
        return this.isInterestRecalculationEnabled;
    }

    public LoanProductInterestRecalculationData getInterestRecalculationData() {
        return this.interestRecalculationData;
    }

    public Collection<ChargeData> overdueFeeCharges() {
        Collection<ChargeData> overdueFeeCharges = new ArrayList<>();
        Collection<ChargeData> charges = charges();
        for (ChargeData chargeData : charges) {
            if (chargeData.isOverdueInstallmentCharge()) {
                overdueFeeCharges.add(chargeData);
            }
        }
        return overdueFeeCharges;
    }

    public LoanInterestRecalculationData toLoanInterestRecalculationData() {
        final Long id = null;
        final Long loanId = null;
        final CalendarData calendarData = null;
        final CalendarData compoundingCalendarData = null;
        return new LoanInterestRecalculationData(id, loanId, getInterestRecalculationCompoundingType(), getRescheduleStrategyType(),
                calendarData, getRecalculationRestFrequencyType(), getRecalculationRestFrequencyInterval(),
                getInterestRecalculationRestNthDayType(), getInterestRecalculationRestWeekDayType(),
                getInterestRecalculationRestOnDayType(), compoundingCalendarData, getRecalculationCompoundingFrequencyType(),
                getRecalculationCompoundingFrequencyInterval(), getInterestRecalculationCompoundingNthDayType(),
                getInterestRecalculationCompoundingWeekDayType(), getInterestRecalculationCompoundingOnDayType(),
                isCompoundingToBePostedAsTransaction(), allowCompoundingOnEod());
    }

    private EnumOptionData getRescheduleStrategyType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRescheduleStrategyType();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationCompoundingType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getInterestRecalculationCompoundingType();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationCompoundingNthDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyNthDay();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationCompoundingWeekDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyWeekday();
        }
        return null;
    }

    private Integer getInterestRecalculationCompoundingOnDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyOnDay();
        }
        return null;
    }

    private EnumOptionData getRecalculationRestFrequencyType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyType();
        }
        return null;
    }

    private Integer getRecalculationRestFrequencyInterval() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyInterval();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationRestNthDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyNthDay();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationRestWeekDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyWeekday();
        }
        return null;
    }

    private Integer getInterestRecalculationRestOnDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyOnDay();
        }
        return null;
    }

    private EnumOptionData getRecalculationCompoundingFrequencyType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyType();
        }
        return null;
    }

    private Integer getRecalculationCompoundingFrequencyInterval() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyInterval();
        }
        return null;
    }

    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    public Boolean isCompoundingToBePostedAsTransaction() {
        return isInterestRecalculationEnabled() ? this.interestRecalculationData.isCompoundingToBePostedAsTransaction() : null;
    }

    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    public Boolean allowCompoundingOnEod() {
        return isInterestRecalculationEnabled() ? this.interestRecalculationData.allowCompoundingOnEod() : null;
    }

    public boolean canDefineInstallmentAmount() {
        return this.canDefineInstallmentAmount;
    }

    public LoanProductConfigurableAttributes getloanProductConfigurableAttributes() {
        return this.allowAttributeOverrides;
    }

    public void setloanProductConfigurableAttributes(LoanProductConfigurableAttributes loanProductConfigurableAttributes) {
        this.allowAttributeOverrides = loanProductConfigurableAttributes;
    }

    public boolean isLinkedToFloatingInterestRates() {
        return this.isLinkedToFloatingInterestRates;
    }

    public BigDecimal getMinDifferentialLendingRate() {
        return this.minDifferentialLendingRate;
    }

    public BigDecimal getDefaultDifferentialLendingRate() {
        return this.defaultDifferentialLendingRate;
    }

    public BigDecimal getMaxDifferentialLendingRate() {
        return this.maxDifferentialLendingRate;
    }

    public boolean isFloatingInterestRateCalculationAllowed() {
        return this.isFloatingInterestRateCalculationAllowed;
    }

    public boolean isVariableInstallmentsAllowed() {
        return this.allowVariableInstallments;
    }

    public Integer getMinimumGapBetweenInstallments() {
        return this.minimumGap;
    }

    public Integer getMaximumGapBetweenInstallments() {
        return this.maximumGap;
    }
//
//    public boolean isAgeCriteriaAllowed() {
//        return this.allowAgeCriteria;
//    }
//
//    public Integer getMinimumValue() {
//        return this.minValue;
//    }
//
//    public Integer getMaximumValue() {
//        return this.maxValue;
//    }

    public boolean allowAgeLimits() {
        return this.allowAgeLimits;
    }

    public Integer getMinimumAge() {
        return this.minimumAge;
    }

    public Integer getMaximumAge() {
        return this.maximumAge;
    }



    public Boolean getAllowPartialPeriodInterestCalcualtion() {
        return this.allowPartialPeriodInterestCalcualtion;
    }

    public boolean syncExpectedWithDisbursementDate() {
        return syncExpectedWithDisbursementDate;
    }

    public boolean canUseForTopup() {
        return this.canUseForTopup;
    }

    public BigDecimal getInterestRateDifferential() {
        return this.interestRateDifferential;
    }

    public boolean isEqualAmortization() {
        return isEqualAmortization;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public Integer getMinNumberOfRepayments() {
        return minNumberOfRepayments;
    }

    public Integer getMaxNumberOfRepayments() {
        return maxNumberOfRepayments;
    }

    public BigDecimal getMinInterestRatePerPeriod() {
        return minInterestRatePerPeriod;
    }

    public BigDecimal getMaxInterestRatePerPeriod() {
        return maxInterestRatePerPeriod;
    }

    public BigDecimal getFixedPrincipalPercentagePerInstallment() {
        return fixedPrincipalPercentagePerInstallment;
    }

    public EnumOptionData getDaysInMonthType() {
        return daysInMonthType;
    }

    public EnumOptionData getDaysInYearType() {
        return daysInYearType;
    }

    public List<EnumOptionData> getDaysInMonthTypeOptions() {
        return daysInMonthTypeOptions;
    }

    public List<EnumOptionData> getDaysInYearTypeOptions() {
        return daysInYearTypeOptions;
    }

    public Boolean getIsPennydropEnabled(){return isPennyDropEnabled;}

    public Boolean getIsDisbursementEnabled(){return isBankDisbursementEnabled;}
}
