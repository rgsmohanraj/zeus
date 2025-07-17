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
package org.vcpl.lms.portfolio.loanproduct;

import java.math.BigDecimal;

public interface LoanProductConstants {

    String USE_BORROWER_CYCLE_PARAMETER_NAME = "useBorrowerCycle";

    String PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME = "principalVariationsForBorrowerCycle";
    String INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME = "interestRateVariationsForBorrowerCycle";
    String NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME = "numberOfRepaymentVariationsForBorrowerCycle";


    String DEFAULT_VALUE_PARAMETER_NAME = "defaultValue";
    String MIN_VALUE_PARAMETER_NAME = "minValue";
    String MAX_VALUE_PARAMETER_NAME = "maxValue";
    String VALUE_CONDITION_TYPE_PARAM_NAME = "valueConditionType";
    String BORROWER_CYCLE_NUMBER_PARAM_NAME = "borrowerCycleNumber";
    String BORROWER_CYCLE_ID_PARAMETER_NAME = "id";

    String PRINCIPAL_PER_CYCLE_PARAMETER_NAME = "principalPerCycle";
    String MIN_PRINCIPAL_PER_CYCLE_PARAMETER_NAME = "minPrincipalPerCycle";
    String MAX_PRINCIPAL_PER_CYCLE_PARAMETER_NAME = "maxPrincipalPerCycle";
    String PRINCIPAL_VALUE_USAGE_CONDITION_PARAM_NAME = "principalValueUsageCondition";
    String PRINCIPAL_CYCLE_NUMBERS_PARAM_NAME = "principalCycleNumbers";

    String NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME = "numberOfRepaymentsPerCycle";
    String MIN_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME = "minNumberOfRepaymentsPerCycle";
    String MAX_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME = "maxNumberOfRepaymentsPerCycle";
    String REPAYMENT_VALUE_USAGE_CONDITION_PARAM_NAME = "repaymentValueUsageCondition";
    String REPAYMENT_CYCLE_NUMBER_PARAM_NAME = "repaymentCycleNumber";

    String INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME = "interestRatePerPeriodPerCycle";
    String MIN_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME = "minInterestRatePerPeriodPerCycle";
    String MAX_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME = "maxInterestRatePerPeriodPerCycle";
    String INTEREST_RATE_VALUE_USAGE_CONDITION_PARAM_NAME = "interestRateValueUsageCondition";
    String INTEREST_RATE_CYCLE_NUMBER_PARAM_NAME = "interestRateCycleNumber";

    String PRINCIPAL = "principal";
    String MIN_PRINCIPAL = "minPrincipal";
    String MAX_PRINCIPAL = "maxPrincipalValue";

    String INTEREST_RATE_PER_PERIOD = "interestRatePerPeriod";
    String MIN_INTEREST_RATE_PER_PERIOD = "minInterestRatePerPeriod";
    String MAX_INTEREST_RATE_PER_PERIOD = "maxInterestRatePerPeriod";

    String NUMBER_OF_REPAYMENTS = "numberOfRepayments";
    String MIN_NUMBER_OF_REPAYMENTS = "minNumberOfRepayments";
    String MAX_NUMBER_OF_REPAYMENTS = "maxNumberOfRepayments";

    String VALUE_CONDITION_END_WITH_ERROR = "condition.type.must.end.with.greterthan";
    String VALUE_CONDITION_START_WITH_ERROR = "condition.type.must.start.with.equal";
    String SHORT_NAME = "shortName";

    String LOAN_ACC_NO_PREFERENCE = "loanAccNoPreference";

    String MULTI_DISBURSE_LOAN_PARAMETER_NAME = "multiDisburseLoan";
    String MAX_TRANCHE_COUNT_PARAMETER_NAME = "maxTrancheCount";
    String COLENDING_CHARGE_PARAMETER_NAME = "colendingCharge";
    String SELF_CHARGE_PARAMETER_NAME = "selfCharge";
    String PARTNER_CHARGE_PARAMETER_NAME = "partnerCharge";
    String ACCEPTED_DATE_PARAMETER_NAME = "acceptedDate";
    String OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME = "outstandingLoanBalance";

    String GRACE_ON_ARREARS_AGEING_PARAMETER_NAME = "graceOnArrearsAgeing";
    String OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME = "overdueDaysForNPA";
    String MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT = "minimumDaysBetweenDisbursalAndFirstRepayment";
    String ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME = "accountMovesOutOfNPAOnlyOnArrearsCompletion";

    // Interest recalculation related
    String IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME = "isInterestRecalculationEnabled";
    String DAYS_IN_YEAR_TYPE_PARAMETER_NAME = "daysInYearType";
    String DAYS_IN_MONTH_TYPE_PARAMETER_NAME = "daysInMonthType";
    String interestRecalculationCompoundingMethodParameterName = "interestRecalculationCompoundingMethod";
    String rescheduleStrategyMethodParameterName = "rescheduleStrategyMethod";
    String recalculationRestFrequencyTypeParameterName = "recalculationRestFrequencyType";
    String recalculationRestFrequencyIntervalParameterName = "recalculationRestFrequencyInterval";
    String recalculationRestFrequencyWeekdayParamName = "recalculationRestFrequencyDayOfWeekType";
    String recalculationRestFrequencyNthDayParamName = "recalculationRestFrequencyNthDayType";
    String recalculationRestFrequencyOnDayParamName = "recalculationRestFrequencyOnDayType";
    String isArrearsBasedOnOriginalScheduleParamName = "isArrearsBasedOnOriginalSchedule";
    String preClosureInterestCalculationStrategyParamName = "preClosureInterestCalculationStrategy";
    String recalculationCompoundingFrequencyTypeParameterName = "recalculationCompoundingFrequencyType";
    String recalculationCompoundingFrequencyIntervalParameterName = "recalculationCompoundingFrequencyInterval";
    String recalculationCompoundingFrequencyWeekdayParamName = "recalculationCompoundingFrequencyDayOfWeekType";
    String recalculationCompoundingFrequencyNthDayParamName = "recalculationCompoundingFrequencyNthDayType";
    String recalculationCompoundingFrequencyOnDayParamName = "recalculationCompoundingFrequencyOnDayType";
    String isCompoundingToBePostedAsTransactionParamName = "isCompoundingToBePostedAsTransaction";

    // Guarantee related
    String holdGuaranteeFundsParamName = "holdGuaranteeFunds";
    String mandatoryGuaranteeParamName = "mandatoryGuarantee";
    String minimumGuaranteeFromOwnFundsParamName = "minimumGuaranteeFromOwnFunds";
    String minimumGuaranteeFromGuarantorParamName = "minimumGuaranteeFromGuarantor";

    String principalThresholdForLastInstallmentParamName = "principalThresholdForLastInstallment";
    BigDecimal DEFAULT_PRINCIPAL_THRESHOLD_FOR_MULTI_DISBURSE_LOAN = BigDecimal.valueOf(50);
    BigDecimal DEFAULT_PRINCIPAL_THRESHOLD_FOR_SINGLE_DISBURSE_LOAN = BigDecimal.valueOf(0);
    // Fixed installment configuration related
    String canDefineEmiAmountParamName = "canDefineInstallmentAmount";
    String fixedPrincipalPercentagePerInstallmentParamName = "fixedPrincipalPercentagePerInstallment";

    // Loan Configurable Attributes
    String allowAttributeOverridesParamName = "allowAttributeOverrides";
    String amortizationTypeParamName = "amortizationType";
    String interestTypeParamName = "interestType";
    String transactionProcessingStrategyIdParamName = "transactionProcessingStrategyId";
    String interestCalculationPeriodTypeParamName = "interestCalculationPeriodType";
    String repaymentStrategyForNpaParamName = "repaymentStrategyForNpa";
    String inArrearsToleranceParamName = "inArrearsTolerance";
    String repaymentEveryParamName = "repaymentEvery";
    String graceOnPrincipalAndInterestPaymentParamName = "graceOnPrincipalAndInterestPayment";
    String allowCompoundingOnEodParamName = "allowCompoundingOnEod";
    String fldgLogicId = "fldgLogicId";
    String penalInvoiceId = "penalInvoiceId";
    String multipleDisbursementId = "multipleDisbursementId";
    String trancheClubbingId = "trancheClubbingId";
    String repaymentScheduleUpdateAllowedId = "repaymentScheduleUpdateAllowedId";
    String insuranceApplicabilityId = "insuranceApplicabilityId";
    String insuranceApplicability = "insuranceApplicability";
    String disbursementId = "disbursementId";
    String collectionId = "collectionId";
    String monitoringTriggerPar30 = "monitoringTriggerPar30";
    String monitoringTriggerPar90 = "monitoringTriggerPar90";

    // Variable Installments Settings
    String allowVariableInstallmentsParamName = "allowVariableInstallments";
    String minimumGapBetweenInstallments = "minimumGap";
    String maximumGapBetweenInstallments = "maximumGap";


    // Age Limits Installments Settings
    String allowAgeLimitsParamName = "allowAgeLimits";
    String minimumAge = "minimumAge";
    String maximumAge = "maximumAge";

//    //prepayLockInstallments Settings
//    String allowPrepaidLockingPeriodParamName = "allowPrepaidLockingPeriod";
//    String prepayLockingPeriod = "prepayLockingPeriod";
//
//    String allowForeclosureLockingPeriodParamName = "allowForeclosureLockingPeriod";
//    String foreclosureLockingPeriod = "foreclosureLockingPeriod";


    String ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME = "allowPartialPeriodInterestCalcualtion";

    String CAN_USE_FOR_TOPUP = "canUseForTopup";

    String IS_EQUAL_AMORTIZATION_PARAM = "isEqualAmortization";

    String RATES_PARAM_NAME = "rates";

    // Multiple disbursement related
    String installmentAmountInMultiplesOfParamName = "installmentAmountInMultiplesOf";
    String DISALLOW_EXPECTED_DISBURSEMENTS = "disallowExpectedDisbursements";
    String ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED = "allowApprovedDisbursedAmountsOverApplied";
    String OVER_APPLIED_CALCULATION_TYPE = "overAppliedCalculationType";
    String OVER_APPLIED_NUMBER = "overAppliedNumber";

    String ASSETCLASSID = "assetClassId";
    String ASSETCLASS = "assetClass";

    String INSURANCEAPPLICABILITYID = "insuranceApplicabilityId";
    String INSURANCEAPPLICABILITY = "insuranceApplicability";
    String FLDGLOGICID = "fldgLogicId";
    String PENALINVOICEID = "penalInvoiceId";
    String MULTIPLEDISBURSEMENTID = "multipleDisbursementId";
    String TRANCHECLUBBINGID = "trancheClubbingId";
    String REPAYMENTSCHEDULEUPDATEALLOWEDID = "repaymentScheduleUpdateAllowedId";
    String FLDGLOGIC = "fldgLogic";
    String PENALINVOICE = "penalInvoice";
    String MULTIPLEDISBURSEMENT = "multipleDisbursement";
    String TRANCHECLUBBING = "trancheClubbing";
    String REPAYMENTSCHEDULEUPDATEALLOWED = "repaymentScheduleUpdateAllowed";
    String DISBURSEMENTMODE = "disbursementMode";
    String DISBURSEMENT = "disbursement";
    String COLLECTIONID = "collectionId";
    String COLLECTION = "collection";
    String FRAMEWORKID = "frameWorkId";
    String FRAMEWORK = "frameWork";
    String LOANTYPEID = "loanTypeId";
    String LOANTYPE = "loanType";
    String LOANPRODUCTCLASS = "loanProductClass";
    String LOANPRODUCTCLASSID = "loanProductClassId";
    String LOANPRODUCTTYPE = "loanProductType";
    String LOANPRODUCTTYPEID = "loanProductTypeId";
    String BROKENINTERESTSTRATEGY = "brokenInterestStrategy";
    String BROKENINTERESTDAYSINYEARS = "brokenInterestDaysInYears";
    String TRANSACTIONTYPEPREFERENCE = "transactionTypePreference";
    String EMIROUNDINGMODE = "emiRoundingMode";

    String ISPENNYDROPENABLED = "isPennyDropEnabled";

    String ISBANKDISBURSEMENTENABLED = "isBankDisbursementEnabled";
    String SERVICER_FEE_INTEREST_CONFIG_ENABLED = "servicerFeeInterestConfigEnabled";
    String SERVICER_FEE_CHARGES_CONFIG_ENABLED = "servicerFeeChargesConfigEnabled";
    String DEDUPE_ENABLED = "enableDedupe";
    String DEDUPE_TYPE = "dedupeType";
    String DISBURSEMENT_BANK_ACC_NAME = "disbursementBankAccountName";
    String DISBURSEMENT_BANK_ACCOUNT_NAME = "DISBURSEMENTBANKACCOUNTNAME";
    String EMIMULTIPLES = "emimultiples";
    String INTERESTDECIMAL = "interestDecimal";
    String INTERESTROUNDINGMODE = "interestRoundingMode";
    String INTERESTDECIMALREGEX = "interestDecimalRegex";
    String EMIDECIMALREGEX = "emiDecimalRegex";
    String ADVANCEAPPROPRIATION = "advanceAppropriation";
    String ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION = "enableEntryForAdvanceTransaction";
    String FORECLOSURE_ON_DUE_DATE_INTEREST = "foreclosureOnDueDateInterest";
    String FORECLOSURE_ON_DUE_DATE_CHARGE = "foreclosureOnDueDateCharge";
    String FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST = "foreclosureOtherThanDueDateInterest";
    String FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE = "foreclosureOtherThanDueDateCharge";
    String FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST = "foreclosureOneMonthOverdueInterest";
    String FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE = "foreclosureOneMonthOverdueCharge";
    String FORECLOSURE_SHORT_PAID_INTEREST = "foreclosureShortPaidInterest";
    String FORECLOSURE_SHORT_PAID_INTEREST_CHARGE = "foreclosureShortPaidInterestCharge";
    String FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST = "foreclosurePrincipalShortPaidInterest";
    String FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE = "foreclosurePrincipalShortPaidCharge";
    String FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST = "foreclosureTwoMonthsOverdueInterest";
    String FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE = "foreclosureTwoMonthsOverdueCharge";
    String FORECLOSURE_POS_ADVANCE_ON_DUE_DATE = "foreclosurePosAdvanceOnDueDate";
    String FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST = "foreclosureAdvanceOnDueDateInterest";
    String FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE = "foreclosureAdvanceOnDueDateCharge";
    String FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE = "foreclosurePosAdvanceOtherThanDueDate";
    String FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST = "foreclosureAdvanceAfterDueDateInterest";
    String FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE = "foreclosureAdvanceAfterDueDateCharge";
    String FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST = "foreclosureBackdatedShortPaidInterest";
    String FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE = "foreclosureBackdatedShortPaidInterestCharge";
    String FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST = "foreclosureBackdatedFullyPaidInterest";
    String FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE = "foreclosureBackdatedFullyPaidInterestCharge";
    String FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST = "foreclosureBackdatedShortPaidPrincipalInterest";
    String FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE = "foreclosureBackdatedShortPaidPrincipalCharge";
    String FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST = "foreclosureBackdatedFullyPaidEmiInterest";
    String FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE = "foreclosureBackdatedFullyPaidEmiCharge";
    String FORECLOSURE_BACKDATED_ADVANCE_INTEREST = "foreclosureBackdatedAdvanceInterest";
    String FORECLOSURE_BACKDATED_ADVANCE_CHARGE = "foreclosureBackdatedAdvanceCharge";
    String INTEREST_BENEFIT_ENABLED = "interestBenefitEnabled";
	String ADVANCE_APPROPRIATION_AGAINST_ON = "advanceAppropriationAgainstOn";
    String PMT_DAYS_IN_MONTH_TYPE =  "pmtDaysInMonthType";
    String PMT_DAYS_IN_YEAR_TYPE ="pmtDaysInYearType";
    String PMT_FORMULA_CALCULATION = "pmtFormulaCalculation";
    String LOAN_PRODUCT_RESOURCE = "loanproduct";
    String LOAN_PRODUCT_NAME = "name";
    String LOAN_PRODUCT_DESCRIPTION = "description";
    String ENABLE_COLENDING_LOAN = "enableColendingLoan";
    String BY_PERCENTAGE_SPLIT  = "byPercentageSplit";
    String PARTNER_ID = "partnerId";
    String PRINCIPAL_SHARE = "principalShare";
    String SELF_PRINCIPAL_SHARE = "selfPrincipalShare";
    String PARTNER_PRINCIPAL_SHARE = "partnerPrincipalShare";
    String INTEREST_RATE = "interestRate";
    String SELF_INTEREST_RATE_SHARE = "selfInterestRate";
    String PARTNER_INTEREST_RATE_SHARE = "partnerInterestRate";
    String ENABLE_FEES_WISE_BIFACATION = "enableFeesWiseBifacation";
    String FEE_SHARE = "feeShare";
    String SELF_FEE_SHARE = "selfFeeShare";
    String PARTNER_FEE_SHARE = "partnerFeeShare";
    String ENABLE_CHARGE_WISE_BIFACATION = "enableChargeWiseBifacation";
    String ENABLE_OVERDUE = "enableOverDue";
    String CURRENCY_CODE = "currencyCode";
    String EMI_DECIMAL = "digitsAfterDecimal";
    String IN_MULTIPLES_OF = "inMultiplesOf";
    String REPAYMENT_FREQUENCY_TYPE = "repaymentFrequencyType";
    String BROKEN_PERIOD_INTEREST = "brokenPeriodInterest";
    String PENALTY_SHARE = "penaltyShare";
    String SELF_PENALTY_SHARE = "selfPenaltyShare";
    String PARTNER_PENALTY_SHARE = "partnerPenaltyShare";
    String INTEREST_RATE_FREQUENCY_TYPE = "interestRateFrequencyType";

    String ENABLE_BACKDATED_DISBURSEMENT =  "enableBackDatedDisbursement";
    String FORECLOSURE_METHOD_TYPE= "foreClosureMethodType";
	String COOLING_OFF_APPLICABILITY = "coolingOffApplicability";
    String COOLING_OFF_THRESHOLD_DAYS = "coolingOffThresholdDays";
    String COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY = "coolingOffInterestAndChargeApplicability";
    String COOLING_OFF_INTEREST_LOGIC_APPLICABILITY = "coolingOffInterestLogicApplicability";
    String COOLING_OFF_DAYS_IN_YEAR = "coolingOffDaysInYear";
    String COOLING_OFF_ROUNDING_MODE = "coolingOffRoundingMode";
    String COOLING_OFF_ROUNDING_DECIMALS = "coolingOffRoundingDecimals";
}
