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
package org.vcpl.lms.portfolio.loanproduct.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.accounting.common.AccountingConstants.LoanProductAccountingParams;
import org.vcpl.lms.accounting.common.AccountingRuleType;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.InvalidJsonException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.portfolio.calendar.service.CalendarUtils;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanproduct.LoanProductConstants;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductFeeData;
import org.vcpl.lms.portfolio.loanproduct.domain.*;
import org.vcpl.lms.portfolio.loanproduct.exception.EnableColendingLoanException;
import org.vcpl.lms.portfolio.loanproduct.exception.EqualAmortizationUnsupportedFeatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vcpl.lms.portfolio.partner.domain.Partner;

import static java.lang.Integer.sum;
import static org.apache.el.lang.ELArithmetic.add;

@Component
public final class LoanProductDataValidator {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("locale", "dateFormat", LoanProductConstants.LOAN_PRODUCT_NAME, LoanProductConstants.LOAN_PRODUCT_DESCRIPTION, "fundId",
            LoanProductConstants.CURRENCY_CODE, LoanProductConstants.EMI_DECIMAL, LoanProductConstants.IN_MULTIPLES_OF, LoanProductConstants.PRINCIPAL, "minPrincipal", "maxPrincipal", LoanProductConstants.repaymentEveryParamName,
            LoanProductConstants.NUMBER_OF_REPAYMENTS, "minNumberOfRepayments", "maxNumberOfRepayments", "repaymentFrequencyType", "interestRatePerPeriod",
            "minInterestRatePerPeriod", "maxInterestRatePerPeriod", "interestRateFrequencyType", "amortizationType", "interestType",
            "interestCalculationPeriodType", LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
            "inArrearsTolerance", "transactionProcessingStrategyId", "graceOnPrincipalPayment", "recurringMoratoriumOnPrincipalPeriods",
            "graceOnInterestPayment", "graceOnInterestCharged", "charges", "accountingRule", "includeInBorrowerCycle", "startDate",
            "closeDate", "externalId", "isLinkedToFloatingInterestRates", "floatingRatesId", "interestRateDifferential",
            "minDifferentialLendingRate", "defaultDifferentialLendingRate", "maxDifferentialLendingRate",
            "isFloatingInterestRateCalculationAllowed", "syncExpectedWithDisbursementDate",
            "allowApprovalOverAmountApplied", "overAmountDetails","overAmountType",
            "overAmount","useDaysInMonthForLoanProvisioning","divideByThirtyForPartialPeriod","brokenInterestCalculationPeriod","repaymentStrategyForNpa","repaymentStrategyForNpaId",
            "loanForeclosureStrategy","brokenInterestDaysInYears","brokenInterestStrategy","enableColendingLoan","byPercentageSplit","selfPrincipalShare","selfFeeShare",
            "selfPenaltyShare","selfOverpaidShares","selfInterestRate","principalShare","feeShare","penaltyShare","overpaidShare",
            "interestRate","partnerId","enableChargeWiseBifacation","selectCharge","colendingCharge","selectOverdueCharges","selfCharge","partnerCharge","selectDateArr","selectAcceptedDates","acceptedDateType","acceptedStartDate","acceptedEndDate","acceptedDate",
            "applyPrepaidLockingPeriod","prepayLockingPeriod","applyForeclosureLockingPeriod","foreclosureLockingPeriod","minimumAge","maximumAge","partnerPrincipalShare","partnerFeeShare","partnerPenaltyShare","partnerOverpaidShare","partnerInterestRate","goodwillCreditAccountId",
            "aumSlabRate","gstLiabilityByVcpl","gstLiabilityByPartner","vcplShareInBrokenInterest","partnerShareInBrokenInterest","monitoringTriggerPar30","brokenPeriodInterest","loanType","frameWork","assetClass","monitoringTriggerPar90","insuranceApplicability",LoanProductConstants.DISBURSEMENT,"collection","fldgLogic","penalInvoice","multipleDisbursement","trancheClubbing","repaymentScheduleUpdateAllowed","collectionAccountNumber","disbursementAccountNumber","loanProductClass","loanProductType","selectFees","enableFeesWiseBifacation","gst","transactionTypePreference","isPennyDropEnabled", "isBankDisbursementEnabled",
            LoanProductAccountingParams.FEES_RECEIVABLE.getValue(), LoanProductAccountingParams.FUND_SOURCE.getValue(),
            LoanProductAccountingParams.INCOME_FROM_FEES.getValue(), LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(),
            LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(), LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(),
            LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), LoanProductAccountingParams.OVERPAYMENT.getValue(),
            LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(),
            LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(),
            LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
            LoanProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue(), LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(),
            LoanProductAccountingParams.PENALTY_INCOME_ACCOUNT_MAPPING.getValue(), LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME,
            LoanProductConstants.PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
            LoanProductConstants.INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
            LoanProductConstants.NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, LoanProductConstants.SHORT_NAME,
            LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME, LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME,
            LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME, LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME,
            LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME,
            LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME,
            LoanProductConstants.rescheduleStrategyMethodParameterName,
            LoanProductConstants.interestRecalculationCompoundingMethodParameterName,
            LoanProductConstants.recalculationRestFrequencyIntervalParameterName,
            LoanProductConstants.recalculationRestFrequencyTypeParameterName,
            LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName,
            LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName,
            LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName,
            LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT, LoanProductConstants.mandatoryGuaranteeParamName,
            LoanProductConstants.holdGuaranteeFundsParamName, LoanProductConstants.minimumGuaranteeFromGuarantorParamName,
            LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, LoanProductConstants.principalThresholdForLastInstallmentParamName,
            LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME,
            LoanProductConstants.canDefineEmiAmountParamName, LoanProductConstants.installmentAmountInMultiplesOfParamName,
            LoanProductConstants.preClosureInterestCalculationStrategyParamName, LoanProductConstants.allowAttributeOverridesParamName,
            LoanProductConstants.allowVariableInstallmentsParamName, LoanProductConstants.minimumGapBetweenInstallments,

            LoanProductConstants.maximumGapBetweenInstallments, LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName,
            LoanProductConstants.allowAgeLimitsParamName, LoanProductConstants.minimumAge,LoanProductConstants.maximumAge,

            LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName,
            LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName,
            LoanProductConstants.recalculationRestFrequencyWeekdayParamName, LoanProductConstants.recalculationRestFrequencyNthDayParamName,
            LoanProductConstants.recalculationRestFrequencyOnDayParamName,
            LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, LoanProductConstants.allowCompoundingOnEodParamName,
            LoanProductConstants.CAN_USE_FOR_TOPUP, LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, LoanProductConstants.RATES_PARAM_NAME,
            LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName, LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS,
            LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED, LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE,
            LoanProductConstants.OVER_APPLIED_NUMBER, LoanProductConstants.ENABLE_OVERDUE,"selectOverDue",LoanProductConstants.EMIROUNDINGMODE, LoanProductConstants.LOAN_ACC_NO_PREFERENCE,
            LoanProductConstants.SERVICER_FEE_INTEREST_CONFIG_ENABLED,LoanProductConstants.SERVICER_FEE_CHARGES_CONFIG_ENABLED,
            LoanProductConstants.DEDUPE_ENABLED, LoanProductConstants.DEDUPE_TYPE, LoanProductConstants.DISBURSEMENT_BANK_ACC_NAME,LoanProductConstants.EMIMULTIPLES,LoanProductConstants.INTERESTDECIMAL, LoanProductConstants.INTERESTROUNDINGMODE,
            LoanProductConstants.INTERESTDECIMALREGEX, LoanProductConstants.EMIDECIMALREGEX,LoanProductConstants.ADVANCEAPPROPRIATION,LoanProductConstants.ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION,LoanProductConstants.INTEREST_BENEFIT_ENABLED,
            LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST,LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE,LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST,LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE,
            LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST,LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE,LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST,LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE,
            LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST,LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE,LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST,LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE,
            LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE,LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST,LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE,LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE,LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST,LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE,
            LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST,LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE,LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST,LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE,
            LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST,LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE,LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST,LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE,
            LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST,LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE,LoanProductConstants.ADVANCE_APPROPRIATION_AGAINST_ON,LoanProductConstants.PMT_DAYS_IN_MONTH_TYPE,LoanProductConstants.PMT_DAYS_IN_YEAR_TYPE,LoanProductConstants.PMT_FORMULA_CALCULATION, LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT,
			LoanProductConstants.COOLING_OFF_APPLICABILITY,LoanProductConstants.COOLING_OFF_THRESHOLD_DAYS,LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY,LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY,
            LoanProductConstants.COOLING_OFF_DAYS_IN_YEAR,LoanProductConstants.COOLING_OFF_ROUNDING_MODE,LoanProductConstants.COOLING_OFF_ROUNDING_DECIMALS,LoanProductConstants.FORECLOSURE_METHOD_TYPE));


    private static final String[] supportedloanConfigurableAttributes = { LoanProductConstants.amortizationTypeParamName,
            LoanProductConstants.interestTypeParamName, LoanProductConstants.transactionProcessingStrategyIdParamName,
            LoanProductConstants.interestCalculationPeriodTypeParamName, LoanProductConstants.inArrearsToleranceParamName,
            LoanProductConstants.repaymentEveryParamName,  LoanProductConstants.repaymentStrategyForNpaParamName,
            LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName,
            LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME };

    private final FromJsonHelper fromApiJsonHelper;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    @Autowired
    public LoanProductDataValidator(final FromJsonHelper fromApiJsonHelper,final CodeValueRepositoryWrapper codeValueRepositoryWrapper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.codeValueRepositoryWrapper=codeValueRepositoryWrapper;
    }

    public void validateForCreate(final String json, List<LoanProductFeeData> loanProductChargeData) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(LoanProductConstants.LOAN_PRODUCT_RESOURCE);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_PRODUCT_NAME, element);
        baseDataValidator.reset().parameter(LoanProductConstants.LOAN_PRODUCT_NAME).value(name).notBlank().notExceedingLengthOf(100);

        final String shortName = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.SHORT_NAME, element);
        baseDataValidator.reset().parameter(LoanProductConstants.SHORT_NAME).value(shortName).notBlank().notExceedingLengthOf(10);

        final String loanAccNoPreference = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_ACC_NO_PREFERENCE, element);
        baseDataValidator.reset().parameter(LoanProductConstants.LOAN_ACC_NO_PREFERENCE).value(loanAccNoPreference).notBlank().notExceedingLengthOf(11);

        final String description = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_PRODUCT_DESCRIPTION, element);
        baseDataValidator.reset().parameter(LoanProductConstants.LOAN_PRODUCT_DESCRIPTION).value(description).notExceedingLengthOf(500);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOANPRODUCTCLASS, element)) {
            final Integer loanProductClassId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.LOANPRODUCTCLASS, element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOANPRODUCTCLASS).value(loanProductClassId).ignoreIfNull().integerGreaterThanZero().loanProductClassIdExist(this.codeValueRepositoryWrapper,loanProductClassId);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOANPRODUCTTYPE, element)) {
            final Integer loanProductTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.LOANPRODUCTTYPE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOANPRODUCTTYPE).value(loanProductTypeId).ignoreIfNull().integerGreaterThanZero().loanProductTypeIdExist(this.codeValueRepositoryWrapper,loanProductTypeId);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ASSETCLASS, element)) {
            final Integer assetClassId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.ASSETCLASS, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ASSETCLASS).value(assetClassId).notBlank().integerGreaterThanZero().ignoreIfNull()
                    .assetClassIdExist(this.codeValueRepositoryWrapper, assetClassId);
        }


        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FLDGLOGIC, element)) {
            final Integer fldgLogicID = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.FLDGLOGIC, element);
            baseDataValidator.reset().parameter(LoanProductConstants.FLDGLOGIC).value(fldgLogicID).notBlank().ignoreIfNull().integerGreaterThanZero().fldgLogicIdExist(this.codeValueRepositoryWrapper, fldgLogicID);
        }


        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FRAMEWORK, element)) {
            final Integer frameWorkId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.FRAMEWORK, element);
            baseDataValidator.reset().parameter(LoanProductConstants.FRAMEWORK).value(frameWorkId).ignoreIfNull().integerGreaterThanZero()
                    .frameWorkIdExist(this.codeValueRepositoryWrapper,frameWorkId);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOANTYPE, element)) {
            final Integer loanTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.LOANTYPE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOANTYPE).value(loanTypeId).ignoreIfNull().integerGreaterThanZero().loanTypeIdExist(this.codeValueRepositoryWrapper,loanTypeId);
        }

        if (this.fromApiJsonHelper.parameterExists("fundId", element)) {
            final Long fundId = this.fromApiJsonHelper.extractLongNamed("fundId", element);
            baseDataValidator.reset().parameter("fundId").value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists("loanType", element)) {
            final Long loanType = this.fromApiJsonHelper.extractLongNamed("loanType", element);
            baseDataValidator.reset().parameter("loanType").value(loanType).ignoreIfNull().integerGreaterThanZero();
        }

        Boolean isEqualAmortization = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
            baseDataValidator.reset().parameter(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM).value(isEqualAmortization).ignoreIfNull()
                    .validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT, element)) {
            final Long minimumDaysBetweenDisbursalAndFirstRepayment = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT, element);
            baseDataValidator.reset().parameter(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT)
                    .value(minimumDaysBetweenDisbursalAndFirstRepayment).ignoreIfNull().integerGreaterThanZero();
        }

        final Boolean includeInBorrowerCycle = this.fromApiJsonHelper.extractBooleanNamed("includeInBorrowerCycle", element);
        baseDataValidator.reset().parameter("includeInBorrowerCycle").value(includeInBorrowerCycle).ignoreIfNull()
                .validateForBooleanValue();

        final Boolean enableDedupe = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.DEDUPE_ENABLED, element);
        baseDataValidator.reset().parameter(LoanProductConstants.DEDUPE_ENABLED).value(enableDedupe).notBlank().ignoreIfNull().validateForBooleanValue();

        if(!baseDataValidator.hasError() && Boolean.valueOf(enableDedupe)){
            final Integer dedupeType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.DEDUPE_TYPE, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.DEDUPE_TYPE).value(dedupeType).notBlank().ignoreIfNull().inMinMaxRange(1,2);
        }

//        final Boolean coBorrower = this.fromApiJsonHelper.extractBooleanNamed("coBorrower", element);
//        baseDataValidator.reset().parameter("coBorrower").value(coBorrower).ignoreIfNull()
//                .validateForBooleanValue();

//        final Boolean eodBalance = this.fromApiJsonHelper.extractBooleanNamed("eodBalance", element);
//        baseDataValidator.reset().parameter("eodBalance").value(eodBalance).ignoreIfNull()
//                .validateForBooleanValue();

//        final Boolean securedLoan = this.fromApiJsonHelper.extractBooleanNamed("securedLoan", element);
//        baseDataValidator.reset().parameter("securedLoan").value(securedLoan).ignoreIfNull()
//                .validateForBooleanValue();
//
//        final Boolean nonEquatedInstallment = this.fromApiJsonHelper.extractBooleanNamed("nonEquatedInstallment", element);
//        baseDataValidator.reset().parameter("nonEquatedInstallment").value(nonEquatedInstallment).ignoreIfNull()
//                .validateForBooleanValue();
//
//
//        final Boolean ageLimits = this.fromApiJsonHelper.extractBooleanNamed("ageLimits", element);
//        baseDataValidator.reset().parameter("ageLimits").value(ageLimits).ignoreIfNull()
//                .validateForBooleanValue();
//
////        final Boolean prepayLockingperiod = this.fromApiJsonHelper.extractBooleanNamed("prepayLockingPeriod", element);
////        baseDataValidator.reset().parameter("prepayLockingperiod").value(prepayLockingperiod).ignoreIfNull()
////                .validateForBooleanValue();
//
//        final Boolean advanceEMI = this.fromApiJsonHelper.extractBooleanNamed("advanceEMI", element);
//        baseDataValidator.reset().parameter("advanceEMI").value(advanceEMI).ignoreIfNull()
//                .validateForBooleanValue();
//
//        final Boolean termBasedOnLoanCycle = this.fromApiJsonHelper.extractBooleanNamed("termBasedOnLoanCycle", element);
//        baseDataValidator.reset().parameter("termBasedOnLoanCycle").value(termBasedOnLoanCycle).ignoreIfNull()
//                .validateForBooleanValue();
//
//        final Boolean isNetOffApplied = this.fromApiJsonHelper.extractBooleanNamed("isNetOffApplied", element);
//        baseDataValidator.reset().parameter("isNetOffApplied").value(isNetOffApplied).ignoreIfNull()
//                .validateForBooleanValue();

        final Boolean allowApprovalOverAmountApplied = this.fromApiJsonHelper.extractBooleanNamed("allowApprovalOverAmountApplied", element);
        baseDataValidator.reset().parameter("allowApprovalOverAmountApplied").value(allowApprovalOverAmountApplied).ignoreIfNull()
                .validateForBooleanValue();

        final Boolean useDaysInMonthForLoanProvisioning = this.fromApiJsonHelper.extractBooleanNamed("useDaysInMonthForLoanProvisioning", element);
        baseDataValidator.reset().parameter("useDaysInMonthForLoanProvisioning").value(useDaysInMonthForLoanProvisioning).ignoreIfNull().validateForBooleanValue();

        final Boolean divideByThirtyForPartialPeriod = this.fromApiJsonHelper.extractBooleanNamed("divideByThirtyForPartialPeriod", element);
        baseDataValidator.reset().parameter("divideByThirtyForPartialPeriod").value(divideByThirtyForPartialPeriod).ignoreIfNull()
                .validateForBooleanValue();

        final Boolean repaymentStrategyForNpa = this.fromApiJsonHelper.extractBooleanNamed("repaymentStrategyForNpa", element);
        baseDataValidator.reset().parameter("repaymentStrategyForNpa").value(repaymentStrategyForNpa).ignoreIfNull()
                .validateForBooleanValue();

        final Boolean enableColendingLoan = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_COLENDING_LOAN, element);
        baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_COLENDING_LOAN).value(enableColendingLoan).notBlank().ignoreIfNull()
                .validateForBooleanValue();

        final Boolean byPercentageSplit = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.BY_PERCENTAGE_SPLIT, element);
        baseDataValidator.reset().parameter(LoanProductConstants.BY_PERCENTAGE_SPLIT).value(byPercentageSplit).notBlank().ignoreIfNull()
                .validateForBooleanValue();

        if(!baseDataValidator.hasError()){

            if(!enableColendingLoan.booleanValue() || !byPercentageSplit.booleanValue()){
                throw new EnableColendingLoanException("Enable Colending Loan and Percentage Split is mandatory");
            } else {

                final Long partnerId = this.fromApiJsonHelper.extractLongNamed(LoanProductConstants.PARTNER_ID, element);
                baseDataValidator.reset().parameter(LoanProductConstants.PARTNER_ID).value(partnerId).notBlank().ignoreIfNull().integerGreaterThanZero();

                //principal
                final Integer principalShare = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.PRINCIPAL_SHARE, element,Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.PRINCIPAL_SHARE).value(principalShare).notBlank().ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

                final Integer selfPrincipalShare = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.SELF_PRINCIPAL_SHARE, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.SELF_PRINCIPAL_SHARE).value(selfPrincipalShare).notBlank().ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

                final Integer partnerPrincipalShare = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.PARTNER_PRINCIPAL_SHARE, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.PARTNER_PRINCIPAL_SHARE).value(partnerPrincipalShare).notBlank().ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

                if (!baseDataValidator.hasError()) {
                    final Integer totalPrincipal = sum(selfPrincipalShare, partnerPrincipalShare);
                    validatePrincipalAmout(baseDataValidator, principalShare, totalPrincipal);
                }

                //interestRate
                final BigDecimal interestRate = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.INTEREST_RATE, element,Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.INTEREST_RATE).value(interestRate).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(100));

                final BigDecimal selfInterestRate = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.SELF_INTEREST_RATE_SHARE, element,Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.SELF_INTEREST_RATE_SHARE).value(selfInterestRate).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(100));

                final BigDecimal partnerInterestRate = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.PARTNER_INTEREST_RATE_SHARE, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.PARTNER_INTEREST_RATE_SHARE).value(partnerInterestRate).notBlank().ignoreIfNull().zeroOrPositiveAmount().notGreaterThanMax(BigDecimal.valueOf(100));

                if (!baseDataValidator.hasError()) {
                    final BigDecimal totalInterest = (BigDecimal) add(selfInterestRate, partnerInterestRate);
                    validateInterestlAmout(baseDataValidator, interestRate, totalInterest);
                }

                //kept not a mandatory fields
                //brokenPeriodInterest
                final BigDecimal brokenPeriodInterest = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.BROKEN_PERIOD_INTEREST, element,Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.BROKEN_PERIOD_INTEREST).value(brokenPeriodInterest).positiveAmount().notGreaterThanMax(BigDecimal.valueOf(100));

                final BigDecimal vcplShareInBrokenInterest = this.fromApiJsonHelper.extractBigDecimalNamed("vcplShareInBrokenInterest", element, Locale.getDefault());
                baseDataValidator.reset().parameter("vcplShareInBrokenInterest").value(vcplShareInBrokenInterest).positiveAmount().notGreaterThanMax(BigDecimal.valueOf(100));

                final BigDecimal partnerShareInBrokenInterest = this.fromApiJsonHelper.extractBigDecimalNamed("partnerShareInBrokenInterest", element, Locale.getDefault());
                baseDataValidator.reset().parameter("partnerShareInBrokenInterest").value(partnerShareInBrokenInterest).zeroOrPositiveAmount().notGreaterThanMax(BigDecimal.valueOf(100));

//                if (!baseDataValidator.hasError()) {
//                    final BigDecimal totalInterest = (BigDecimal) add(vcplShareInBrokenInterest, partnerShareInBrokenInterest);
//                    validateDecimalAmount(baseDataValidator, brokenPeriodInterest, totalInterest, BROKEN_PERIOD_INTEREST);
//                }

                //fee
                final Boolean enableFeesWiseBifacation = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_FEES_WISE_BIFACATION, element);
                baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_FEES_WISE_BIFACATION).value(enableFeesWiseBifacation).notBlank().ignoreIfNull()
                        .validateForBooleanValue();

                //charges
                final Boolean enableChargeWiseBifacation = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_CHARGE_WISE_BIFACATION, element);
                baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_CHARGE_WISE_BIFACATION).value(enableChargeWiseBifacation).notBlank().ignoreIfNull().validateForBooleanValue();

                //overdue
                final Boolean enableOverdue = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_OVERDUE, element);
                baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_OVERDUE).value(enableOverdue).notBlank().ignoreIfNull().validateForBooleanValue();
            }
        }

        //penaltyShare
        final Integer penaltyShare = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.PENALTY_SHARE, element,Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.PENALTY_SHARE).value(penaltyShare).ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

        final Integer selfPenaltyShare = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.SELF_PENALTY_SHARE, element,Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.SELF_PENALTY_SHARE).value(selfPenaltyShare).ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

        final Integer partnerPenaltyShare = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.PARTNER_PENALTY_SHARE, element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.PARTNER_PENALTY_SHARE).value(partnerPenaltyShare).ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

        //overPaid
        final Integer overpaidShare = this.fromApiJsonHelper.extractIntegerNamed("overpaidShare", element,Locale.getDefault());
        baseDataValidator.reset().parameter("overpaidShare").value(overpaidShare).ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

        final Integer selfOverpaidShares = this.fromApiJsonHelper.extractIntegerNamed("selfOverpaidShares", element,Locale.getDefault());
        baseDataValidator.reset().parameter("selfOverpaidShares").value(selfOverpaidShares).ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

        final Integer partnerOverpaidShare = this.fromApiJsonHelper.extractIntegerNamed("partnerOverpaidShare", element, Locale.getDefault());
        baseDataValidator.reset().parameter("partnerOverpaidShare").value(partnerOverpaidShare).ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

        //feeShares
        final Integer feeShare = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FEE_SHARE, element,Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.FEE_SHARE).value(feeShare).ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

        final Integer selfFeeShare = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.SELF_FEE_SHARE, element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.SELF_FEE_SHARE).value(selfFeeShare).ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

        final Integer partnerFeeShare = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.PARTNER_FEE_SHARE, element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.PARTNER_FEE_SHARE).value(partnerFeeShare).ignoreIfNull().integerZeroOrGreater().notGreaterThanMax(Integer.valueOf(100));

        final Boolean applyPrepaidLockingPeriod = this.fromApiJsonHelper.extractBooleanNamed("applyPrepaidLockingPeriod", element);
        baseDataValidator.reset().parameter("applyPrepaidLockingPeriod").value(applyPrepaidLockingPeriod).ignoreIfNull()
                .validateForBooleanValue();

        final Integer prepayLockingPeriod = this.fromApiJsonHelper.extractIntegerNamed("prepayLockingPeriod", element,Locale.getDefault());
        baseDataValidator.reset().parameter("prepayLockingPeriod").value(prepayLockingPeriod).ignoreIfNull().integerZeroOrGreater();


        final Boolean applyForeclosureLockingPeriod = this.fromApiJsonHelper.extractBooleanNamed("applyForeclosureLockingPeriod", element);
        baseDataValidator.reset().parameter("applyForeclosureLockingPeriod").value(applyForeclosureLockingPeriod).ignoreIfNull()
                .validateForBooleanValue();

        final Integer foreclosureLockingPeriod = this.fromApiJsonHelper.extractIntegerNamed("foreclosureLockingPeriod", element,Locale.getDefault());
        baseDataValidator.reset().parameter("foreclosureLockingPeriod").value(foreclosureLockingPeriod).ignoreIfNull().integerZeroOrGreater();

        final Boolean selectAcceptedDates = this.fromApiJsonHelper.extractBooleanNamed("selectAcceptedDates", element);
        baseDataValidator.reset().parameter("selectAcceptedDates").value(selectAcceptedDates).ignoreIfNull()
                .validateForBooleanValue();

        final String acceptedDateType = this.fromApiJsonHelper.extractStringNamed("acceptedDateType",
                element);
        baseDataValidator.reset().parameter("acceptedDateType").value(acceptedDateType).ignoreIfNull()
                .isOneOfTheseValues("Range","Select Date");

        final Integer acceptedStartDate = this.fromApiJsonHelper.extractIntegerNamed("acceptedStartDate", element, Locale.getDefault());
        baseDataValidator.reset().parameter("acceptedStartDate").value(acceptedStartDate).ignoreIfNull().integerZeroOrGreater();

        final Integer acceptedEndDate = this.fromApiJsonHelper.extractIntegerNamed("acceptedEndDate", element, Locale.getDefault());
        baseDataValidator.reset().parameter("acceptedEndDate").value(acceptedEndDate).ignoreIfNull().integerZeroOrGreater();

        final Integer acceptedDate = this.fromApiJsonHelper.extractIntegerNamed("acceptedDate", element, Locale.getDefault());
        baseDataValidator.reset().parameter("acceptedDate").value(acceptedDate).ignoreIfNull().integerZeroOrGreater();

        final BigDecimal aumSlabRate = this.fromApiJsonHelper.extractBigDecimalNamed("aumSlabRate", element, Locale.getDefault());
        baseDataValidator.reset().parameter("aumSlabRate").value(aumSlabRate).ignoreIfNull().positiveAmount();

        final BigDecimal gstLiabilityByVcpl = this.fromApiJsonHelper.extractBigDecimalNamed("gstLiabilityByVcpl", element, Locale.getDefault());
        baseDataValidator.reset().parameter("gstLiabilityByVcpl").value(gstLiabilityByVcpl).ignoreIfNull().zeroOrPositiveAmount().notGreaterThanMax(BigDecimal.valueOf(100));

        final BigDecimal gstLiabilityByPartner = this.fromApiJsonHelper.extractBigDecimalNamed("gstLiabilityByPartner", element, Locale.getDefault());
        baseDataValidator.reset().parameter("gstLiabilityByPartner").value(gstLiabilityByPartner).ignoreIfNull().zeroOrPositiveAmount().notGreaterThanMax(BigDecimal.valueOf(100));

//        validateGstLiablity(gstLiabilityByVcpl,gstLiabilityByPartner,baseDataValidator);


        final Integer monitoringTriggerPar30 = this.fromApiJsonHelper.extractIntegerNamed("monitoringTriggerPar30", element, Locale.getDefault());
        baseDataValidator.reset().parameter("monitoringTriggerPar30").value(monitoringTriggerPar30).ignoreIfNull().integerZeroOrGreater();

        final Integer monitoringTriggerPar90 = this.fromApiJsonHelper.extractIntegerNamed("monitoringTriggerPar90", element, Locale.getDefault());
        baseDataValidator.reset().parameter("monitoringTriggerPar90").value(monitoringTriggerPar90).ignoreIfNull().integerZeroOrGreater();

        // terms
        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.CURRENCY_CODE, element);
        baseDataValidator.reset().parameter(LoanProductConstants.CURRENCY_CODE).value(currencyCode).notBlank().notExceedingLengthOf(3);

        final Integer pmtFormulaCalculation = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.PMT_FORMULA_CALCULATION, element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.PMT_FORMULA_CALCULATION).value(pmtFormulaCalculation).notBlank().ignoreIfNull().isOneOfTheseValues(1,2);

        if(!baseDataValidator.hasError() && pmtFormulaCalculation.equals(1)){

            final Integer daysInYearType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.PMT_DAYS_IN_YEAR_TYPE,
                    element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.PMT_DAYS_IN_YEAR_TYPE).value(daysInYearType).notBlank().ignoreIfNull().isOneOfTheseValues(1, 360, 364, 365);

            final Integer daysInMonthType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.PMT_DAYS_IN_MONTH_TYPE,
                    element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.PMT_DAYS_IN_MONTH_TYPE).value(daysInMonthType).notBlank().ignoreIfNull().isOneOfTheseValues(1, 30, 31);

        }

        final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.EMI_DECIMAL, element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.EMI_DECIMAL).value(digitsAfterDecimal).notBlank().inMinMaxRange(0, 6);

        final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.IN_MULTIPLES_OF, element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.IN_MULTIPLES_OF).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();

        Integer emiDecimalRegex = null;
        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.EMIDECIMALREGEX, element)){
            emiDecimalRegex = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.EMIDECIMALREGEX, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.EMIDECIMALREGEX).value(emiDecimalRegex).notNull().ignoreIfNull().integerZeroOrGreater().isOneOfTheseValues(0,1,2,3,4);}

        Integer emiMultiples = null;
        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.EMIMULTIPLES, element)){
            emiMultiples = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.EMIMULTIPLES, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.EMIMULTIPLES).value(emiMultiples).notNull().ignoreIfNull().integerZeroOrGreater().isEmiMultiplesOfExist(emiMultiples);}

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.EMIROUNDINGMODE, element)){
            final String emiRoundingMode = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.EMIROUNDINGMODE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.EMIROUNDINGMODE).value(emiRoundingMode).notBlank().ignoreIfNull().isRoundingModesExist(emiRoundingMode);
        }

       /* if(Objects.nonNull(emiDecimalRegex) && Objects.nonNull(emiMultiples) && emiDecimalRegex > emiMultiples){
            baseDataValidator.reset().parameter(LoanProductConstants.EMIDECIMALREGEX).value(emiMultiples).integerEqualToOrGreaterThanNumber(emiDecimalRegex);
        }*/

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.INTERESTROUNDINGMODE, element)){
            final String interestRoundingMode = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.INTERESTROUNDINGMODE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.INTERESTROUNDINGMODE).value(interestRoundingMode).notBlank().ignoreIfNull().isRoundingModesExist(interestRoundingMode);}

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.INTERESTDECIMALREGEX, element)){
            final Integer interestDecimalRegex = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.INTERESTDECIMALREGEX, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.INTERESTDECIMALREGEX).value(interestDecimalRegex).notNull().integerZeroOrGreater().isOneOfTheseValues(0,1,2,3,4);}


        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.INTERESTDECIMAL, element)) {
            final Integer interestDecimal = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.INTERESTDECIMAL, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.INTERESTDECIMAL).value(interestDecimal).notNull().integerZeroOrGreater().inMinMaxRange(0, 6);
        }

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanProductConstants.PRINCIPAL, element);
        baseDataValidator.reset().parameter(LoanProductConstants.PRINCIPAL).value(principal).notBlank().positiveAmount();

        final String minPrincipalParameterName = "minPrincipal";
        BigDecimal minPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(minPrincipalParameterName, element)) {
            minPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minPrincipalParameterName, element);
            baseDataValidator.reset().parameter(minPrincipalParameterName).value(minPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        final String maxPrincipalParameterName = "maxPrincipal";
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(maxPrincipalParameterName, element)) {
            maxPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxPrincipalParameterName, element);
            baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        if (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) >= 0) {

            if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).notLessThanMin(minPrincipalAmount);
                if (minPrincipalAmount.compareTo(maxPrincipalAmount) <= 0 && principal != null) {
                    baseDataValidator.reset().parameter(LoanProductConstants.PRINCIPAL).value(principal).inMinAndMaxAmountRange(minPrincipalAmount,
                            maxPrincipalAmount);
                }
            } else if (principal != null) {
                baseDataValidator.reset().parameter(LoanProductConstants.PRINCIPAL).value(principal).notGreaterThanMax(maxPrincipalAmount);
            }
        } else if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) >= 0 && principal != null) {
            baseDataValidator.reset().parameter(LoanProductConstants.PRINCIPAL).value(principal).notLessThanMin(minPrincipalAmount);
        }

        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.NUMBER_OF_REPAYMENTS, element);
        baseDataValidator.reset().parameter(LoanProductConstants.NUMBER_OF_REPAYMENTS).value(numberOfRepayments).notBlank().ignoreIfNull().integerGreaterThanZero();

        final String minNumberOfRepaymentsParameterName = "minNumberOfRepayments";
        Integer minNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(minNumberOfRepaymentsParameterName, element)) {
            minNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(minNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(minNumberOfRepaymentsParameterName).value(minNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        final String maxNumberOfRepaymentsParameterName = "maxNumberOfRepayments";
        Integer maxNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(maxNumberOfRepaymentsParameterName, element)) {
            maxNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(maxNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        if (maxNumberOfRepayments != null && maxNumberOfRepayments.compareTo(0) > 0) {
            if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
                baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments)
                        .notLessThanMin(minNumberOfRepayments);
                if (minNumberOfRepayments.compareTo(maxNumberOfRepayments) <= 0) {
                    baseDataValidator.reset().parameter(LoanProductConstants.NUMBER_OF_REPAYMENTS).value(numberOfRepayments).inMinMaxRange(minNumberOfRepayments,
                            maxNumberOfRepayments);
                }
            } else {
                baseDataValidator.reset().parameter(LoanProductConstants.NUMBER_OF_REPAYMENTS).value(numberOfRepayments)
                        .notGreaterThanMax(maxNumberOfRepayments);
            }
        } else if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
            baseDataValidator.reset().parameter(LoanProductConstants.NUMBER_OF_REPAYMENTS).value(numberOfRepayments).notLessThanMin(minNumberOfRepayments);
        }

        final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.repaymentEveryParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.repaymentEveryParamName).value(repaymentEvery).notBlank().ignoreIfNull().integerGreaterThanZero();

        final Integer repaymentFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.REPAYMENT_FREQUENCY_TYPE, element,
                Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.REPAYMENT_FREQUENCY_TYPE).value(repaymentFrequencyType).notBlank().inMinMaxRange(0, 2);

        // settings
        final Integer amortizationType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.amortizationTypeParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.amortizationTypeParamName).value(amortizationType).notBlank().inMinMaxRange(0, 1);

        final Integer interestType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.interestTypeParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.interestTypeParamName).value(interestType).notBlank().inMinMaxRange(0, 1);

        final Integer interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.interestCalculationPeriodTypeParamName, element,
                Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.interestCalculationPeriodTypeParamName).value(interestCalculationPeriodType).notNull().inMinMaxRange(0,
                1);

        final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
        baseDataValidator.reset().parameter("inArrearsTolerance").value(inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();

        final Long transactionProcessingStrategyId = this.fromApiJsonHelper.extractLongNamed(LoanProductConstants.transactionProcessingStrategyIdParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.transactionProcessingStrategyIdParamName).value(transactionProcessingStrategyId).notBlank().ignoreIfNull().isOneOfTheseValues(5L, 6L, 8L, 9L);

        // grace validation
        final Integer graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
        baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(graceOnPrincipalPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
        baseDataValidator.reset().parameter("graceOnInterestPayment").value(graceOnInterestPayment).zeroOrPositiveAmount();

        final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
        baseDataValidator.reset().parameter("graceOnInterestCharged").value(graceOnInterestCharged).zeroOrPositiveAmount();

        final Integer graceOnArrearsAgeing = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element);
        baseDataValidator.reset().parameter(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME).value(graceOnArrearsAgeing)
                .integerZeroOrGreater();

        final Integer overdueDaysForNPA = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, element);
        baseDataValidator.reset().parameter(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME).value(overdueDaysForNPA)
                .integerZeroOrGreater();

        final Long disbursementAccountNumber = this.fromApiJsonHelper.extractLongNamed("disbursementAccountNumber", element);
        baseDataValidator.reset().parameter("disbursementAccountNumber").value(disbursementAccountNumber).ignoreIfNull().longZeroOrGreater();

        final Long collectionAccountNumber = this.fromApiJsonHelper.extractLongNamed("collectionAccountNumber", element);
        baseDataValidator.reset().parameter("collectionAccountNumber").value(collectionAccountNumber).ignoreIfNull().longZeroOrGreater();

//        final BigDecimal vcplHurdleRate = this.fromApiJsonHelper.extractBigDecimalNamed("vcplHurdleRate", element,Locale.getDefault());
//        baseDataValidator.reset().parameter("vcplHurdleRate").value(vcplHurdleRate).notNull().positiveAmount();


        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.PENALINVOICE,element))
        {
            final Integer penalInvoiceID = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.PENALINVOICE,element);
            baseDataValidator.reset().parameter(LoanProductConstants.PENALINVOICE).value(penalInvoiceID).ignoreIfNull().integerGreaterThanZero().penalInvoiceExists(this.codeValueRepositoryWrapper,penalInvoiceID);
        }

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.MULTIPLEDISBURSEMENT,element))
        {
            final Integer multiDisbursementId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.MULTIPLEDISBURSEMENT,element);
            baseDataValidator.reset().parameter(LoanProductConstants.MULTIPLEDISBURSEMENT).value(multiDisbursementId).ignoreIfNull().integerGreaterThanZero().multipleDisbursementExists(this.codeValueRepositoryWrapper,multiDisbursementId);
        }

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.TRANCHECLUBBING,element))
        {
            final Integer trancheClubbingId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.TRANCHECLUBBING,element);
            baseDataValidator.reset().parameter(LoanProductConstants.TRANCHECLUBBING).value(trancheClubbingId).ignoreIfNull().integerGreaterThanZero().trancheClubbingExists(this.codeValueRepositoryWrapper,trancheClubbingId);
        }

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED,element))
        {
            final Integer repaymentScheduleUpdateAllowedId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED,element);
            baseDataValidator.reset().parameter(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED).value(repaymentScheduleUpdateAllowedId).ignoreIfNull().integerGreaterThanZero().repaymentScheduleUpdateAllowedExists(this.codeValueRepositoryWrapper,repaymentScheduleUpdateAllowedId);
        }

        final Boolean servicerFeeInterestConfigEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.SERVICER_FEE_INTEREST_CONFIG_ENABLED, element);
        baseDataValidator.reset().parameter("servicerFeeInterestConfigEnabled").value(servicerFeeInterestConfigEnabled).notBlank().ignoreIfNull().validateForBooleanValue();

        final Boolean servicerFeeChargesConfigEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.SERVICER_FEE_CHARGES_CONFIG_ENABLED, element);
        baseDataValidator.reset().parameter("servicerFeeChargesConfigEnabled").value(servicerFeeChargesConfigEnabled).notBlank().ignoreIfNull().validateForBooleanValue();

        /**
         * { @link DaysInYearType }
         */
        final Integer daysInYearType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME,
                element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME).value(daysInYearType).notBlank().ignoreIfNull().isOneOfTheseValues(1, 360, 364, 365);

        /**
         * { @link DaysInMonthType }
         */
        final Integer daysInMonthType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME,
                element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME).value(daysInMonthType).notBlank().ignoreIfNull().isOneOfTheseValues(1, 30, 31);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME,
                element)) {
            Boolean npaChangeConfig = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME)
                    .value(npaChangeConfig).notNull().isOneOfTheseValues(true, false);
        }

        final Integer brokenInterestStrategy=this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.BROKENINTERESTSTRATEGY,element);
              baseDataValidator.reset().parameter(LoanProductConstants.BROKENINTERESTSTRATEGY).value(brokenInterestStrategy).notBlank().ignoreIfNull().inMinMaxRange(1,4);

        final Integer disbursement=this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.DISBURSEMENT,element);
        baseDataValidator.reset().parameter(LoanProductConstants.DISBURSEMENT).value(disbursement).notBlank().ignoreIfNull().inMinMaxRange(1,3);


        final Integer collection =this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.COLLECTION,element);
        baseDataValidator.reset().parameter(LoanProductConstants.COLLECTION).value(collection).notBlank().ignoreIfNull().inMinMaxRange(1,4);

        final Integer transactionTypePreference =this.fromApiJsonHelper.extractIntegerWithLocaleNamed("transactionTypePreference",element);
        baseDataValidator.reset().parameter("transactionTypePreference").value(transactionTypePreference).ignoreIfNull().inMinMaxRange(1,4);

        final String brokenInterestCalculationPeriod = this.fromApiJsonHelper.extractStringNamed("brokenInterestCalculationPeriod",
                element);
        baseDataValidator.reset().parameter("brokenInterestCalculationPeriod").value(brokenInterestCalculationPeriod).ignoreIfNull()
                .isOneOfTheseValues("Actual","Days");

        final String repaymentStrategyForNpaId = this.fromApiJsonHelper.extractStringNamed("repaymentStrategyForNpaId",
                element);
        baseDataValidator.reset().parameter("repaymentStrategyForNpaId").value(repaymentStrategyForNpaId).ignoreIfNull()
                .isOneOfTheseValues("Strategy1","Strategy2");

        final String loanForeclosureStrategy = this.fromApiJsonHelper.extractStringNamed("loanForeclosureStrategy",
                element);
        baseDataValidator.reset().parameter("loanForeclosureStrategy").value(loanForeclosureStrategy).ignoreIfNull()
                .isOneOfTheseValues("Loan Strategy1","Loan Strategy2");

//        final Integer brokenInterestDaysInMonth = this.fromApiJsonHelper.extractIntegerNamed("brokenInterestDaysInMonth",
//                element, Locale.getDefault());
//        baseDataValidator.reset().parameter("brokenInterestDaysInMonth").value(brokenInterestDaysInMonth).notNull()
//                .isOneOfTheseValues(0,1, 30,31);

        final Integer brokenInterestDaysInYears = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.BROKENINTERESTDAYSINYEARS,
                element, Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.BROKENINTERESTDAYSINYEARS).value(brokenInterestDaysInYears).notBlank().ignoreIfNull().isOneOfTheseValues( 1, 2, 365, 364);
//1
//        final String brokenInterestDaysInMonth = this.fromApiJsonHelper.extractStringNamed("brokenInterestDaysInMonth",
//                element);
//        baseDataValidator.reset().parameter("brokenInterestDaysInMonth").value(brokenInterestDaysInMonth).ignoreIfNull()
//                .isOneOfTheseValues("364","Actual");

//        final String brokenInterestDaysInYears = this.fromApiJsonHelper.extractStringNamed("brokenInterestDaysInYears",
//                element);
//        baseDataValidator.reset().parameter("brokenInterestDaysInYears").value(brokenInterestDaysInYears).ignoreIfNull()
//                .isOneOfTheseValues("365","Actual");
        if(!loanProductChargeData.isEmpty()){

        colendingChargeValidation(baseDataValidator,loanProductChargeData);
        duplicateChargeValidation(baseDataValidator,loanProductChargeData);
        }

//        final String brokenInterestStrategy = this.fromApiJsonHelper.extractStringNamed("brokenInterestStrategy",
//                element);
//        baseDataValidator.reset().parameter("brokenInterestStrategy").value(brokenInterestStrategy).ignoreIfNull()
//                .isOneOfTheseValues("Nobroken",LoanProductConstants.DISBURSEMENT,"FirstRepayment","LastRepayment");

        // Interest recalculation settings
        final Boolean isInterestRecalculationEnabled = this.fromApiJsonHelper
                .extractBooleanNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, element);
        baseDataValidator.reset().parameter(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME)
                .value(isInterestRecalculationEnabled).notNull().isOneOfTheseValues(true, false);

        if (isInterestRecalculationEnabled != null) {
            if (isInterestRecalculationEnabled) {
                if (isEqualAmortization) {
                    throw new EqualAmortizationUnsupportedFeatureException("interest.recalculation", "interest recalculation");
                }
                validateInterestRecalculationParams(element, baseDataValidator, null);
            }
        }

        // interest rates
        if (this.fromApiJsonHelper.parameterExists("isLinkedToFloatingInterestRates", element)
                && this.fromApiJsonHelper.extractBooleanNamed("isLinkedToFloatingInterestRates", element) == true) {
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate", "floating interest rate");
            }
            if (this.fromApiJsonHelper.parameterExists("interestRatePerPeriod", element)) {
                baseDataValidator.reset().parameter("interestRatePerPeriod").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "interestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("minInterestRatePerPeriod", element)) {
                baseDataValidator.reset().parameter("minInterestRatePerPeriod").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "minInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("maxInterestRatePerPeriod", element)) {
                baseDataValidator.reset().parameter("maxInterestRatePerPeriod").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "maxInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("interestRateFrequencyType", element)) {
                baseDataValidator.reset().parameter("interestRateFrequencyType").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "interestRateFrequencyType param is not supported when isLinkedToFloatingInterestRates is true");
            }
            if ((interestType == null || !interestType.equals(InterestMethod.DECLINING_BALANCE.getValue()))
                    || (isInterestRecalculationEnabled == null || isInterestRecalculationEnabled == false)) {
                baseDataValidator.reset().parameter("isLinkedToFloatingInterestRates").failWithCode(
                        "supported.only.for.declining.balance.interest.recalculation.enabled",
                        "Floating interest rates are supported only for declining balance and interest recalculation enabled loan products");
            }

            final Integer floatingRatesId = this.fromApiJsonHelper.extractIntegerNamed("floatingRatesId", element, Locale.getDefault());
            baseDataValidator.reset().parameter("floatingRatesId").value(floatingRatesId).notNull();

            final BigDecimal interestRateDifferential = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRateDifferential",
                    element);
            baseDataValidator.reset().parameter("interestRateDifferential").value(interestRateDifferential).notNull()
                    .zeroOrPositiveAmount();

            final String minDifferentialLendingRateParameterName = "minDifferentialLendingRate";
            BigDecimal minDifferentialLendingRate = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(minDifferentialLendingRateParameterName, element);
            baseDataValidator.reset().parameter(minDifferentialLendingRateParameterName).value(minDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String defaultDifferentialLendingRateParameterName = "defaultDifferentialLendingRate";
            BigDecimal defaultDifferentialLendingRate = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(defaultDifferentialLendingRateParameterName, element);
            baseDataValidator.reset().parameter(defaultDifferentialLendingRateParameterName).value(defaultDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String maxDifferentialLendingRateParameterName = "maxDifferentialLendingRate";
            BigDecimal maxDifferentialLendingRate = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(maxDifferentialLendingRateParameterName, element);
            baseDataValidator.reset().parameter(maxDifferentialLendingRateParameterName).value(maxDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                if (minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter("defaultDifferentialLendingRate").value(defaultDifferentialLendingRate)
                            .notLessThanMin(minDifferentialLendingRate);
                }
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                if (minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter("maxDifferentialLendingRate").value(maxDifferentialLendingRate)
                            .notLessThanMin(minDifferentialLendingRate);
                }
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter("maxDifferentialLendingRate").value(maxDifferentialLendingRate)
                            .notLessThanMin(defaultDifferentialLendingRate);
                }
            }

            final Boolean isFloatingInterestRateCalculationAllowed = this.fromApiJsonHelper
                    .extractBooleanNamed("isFloatingInterestRateCalculationAllowed", element);
            baseDataValidator.reset().parameter("isFloatingInterestRateCalculationAllowed").value(isFloatingInterestRateCalculationAllowed)
                    .notNull().isOneOfTheseValues(true, false);
        } else {
            if (this.fromApiJsonHelper.parameterExists("floatingRatesId", element)) {
                baseDataValidator.reset().parameter("floatingRatesId").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "floatingRatesId param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("interestRateDifferential", element)) {
                baseDataValidator.reset().parameter("interestRateDifferential").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "interestRateDifferential param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("minDifferentialLendingRate", element)) {
                baseDataValidator.reset().parameter("minDifferentialLendingRate").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "minDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("defaultDifferentialLendingRate", element)) {
                baseDataValidator.reset().parameter("defaultDifferentialLendingRate").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "defaultDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("maxDifferentialLendingRate", element)) {
                baseDataValidator.reset().parameter("maxDifferentialLendingRate").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "maxDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("isFloatingInterestRateCalculationAllowed", element)) {
                baseDataValidator.reset().parameter("isFloatingInterestRateCalculationAllowed").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "isFloatingInterestRateCalculationAllowed param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            final BigDecimal interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanProductConstants.INTEREST_RATE_PER_PERIOD,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.INTEREST_RATE_PER_PERIOD).value(interestRatePerPeriod).notNull().zeroOrPositiveAmount();

            final String minInterestRatePerPeriodParameterName = "minInterestRatePerPeriod";
            BigDecimal minInterestRatePerPeriod = null;
            if (this.fromApiJsonHelper.parameterExists(minInterestRatePerPeriodParameterName, element)) {
                minInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minInterestRatePerPeriodParameterName,
                        element);
                baseDataValidator.reset().parameter(minInterestRatePerPeriodParameterName).value(minInterestRatePerPeriod).ignoreIfNull()
                        .zeroOrPositiveAmount();
            }

            final String maxInterestRatePerPeriodParameterName = "maxInterestRatePerPeriod";
            BigDecimal maxInterestRatePerPeriod = null;
            if (this.fromApiJsonHelper.parameterExists(maxInterestRatePerPeriodParameterName, element)) {
                maxInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxInterestRatePerPeriodParameterName,
                        element);
                baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod).ignoreIfNull()
                        .zeroOrPositiveAmount();
            }

            if (maxInterestRatePerPeriod != null && maxInterestRatePerPeriod.compareTo(BigDecimal.ZERO) >= 0) {
                if (minInterestRatePerPeriod != null && minInterestRatePerPeriod.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod)
                            .notLessThanMin(minInterestRatePerPeriod);
                    if (minInterestRatePerPeriod.compareTo(maxInterestRatePerPeriod) <= 0) {
                        baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod)
                                .inMinAndMaxAmountRange(minInterestRatePerPeriod, maxInterestRatePerPeriod);
                    }
                } else {
                    baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod)
                            .notGreaterThanMax(maxInterestRatePerPeriod);
                }
            } else if (minInterestRatePerPeriod != null && minInterestRatePerPeriod.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod)
                        .notLessThanMin(minInterestRatePerPeriod);
            }

            final Integer interestRateFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.INTEREST_RATE_FREQUENCY_TYPE, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.INTEREST_RATE_FREQUENCY_TYPE).value(interestRateFrequencyType).notBlank().inMinMaxRange(2, 4);
        }

        // Guarantee Funds
        Boolean holdGuaranteeFunds = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.holdGuaranteeFundsParamName, element)) {
            holdGuaranteeFunds = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.holdGuaranteeFundsParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.holdGuaranteeFundsParamName).value(holdGuaranteeFunds).notNull()
                    .isOneOfTheseValues(true, false);
        }

        if (holdGuaranteeFunds != null) {
            if (holdGuaranteeFunds) {
                validateGuaranteeParams(element, baseDataValidator, null);
            }
        }

        BigDecimal principalThresholdForLastInstallment = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.principalThresholdForLastInstallmentParamName)
                .value(principalThresholdForLastInstallment).notLessThanMin(BigDecimal.ZERO).notGreaterThanMax(BigDecimal.valueOf(100));

        BigDecimal fixedPrincipalPercentagePerInstallment = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName)
                .value(fixedPrincipalPercentagePerInstallment).notLessThanMin(BigDecimal.ONE).notGreaterThanMax(BigDecimal.valueOf(100));

        if(!baseDataValidator.hasError()) {
            if (!amortizationType.equals(AmortizationMethod.EQUAL_PRINCIPAL.getValue()) && fixedPrincipalPercentagePerInstallment != null) {
                baseDataValidator.reset().parameter(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName).failWithCode(
                        "not.supported.principal.fixing.not.allowed.with.equal.installments",
                        "Principal fixing cannot be done with equal installment amortization");
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.canDefineEmiAmountParamName, element)) {
            final Boolean canDefineInstallmentAmount = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.canDefineEmiAmountParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.canDefineEmiAmountParamName).value(canDefineInstallmentAmount)
                    .isOneOfTheseValues(true, false);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.installmentAmountInMultiplesOfParamName, element)) {
            final Integer installmentAmountInMultiplesOf = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.installmentAmountInMultiplesOfParamName)
                    .value(installmentAmountInMultiplesOf).ignoreIfNull().integerGreaterThanZero();
        }

        final Boolean isPennyDropEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ISPENNYDROPENABLED, element);
        baseDataValidator.reset().parameter(LoanProductConstants.ISPENNYDROPENABLED).value(isPennyDropEnabled).ignoreIfNull()
                .validateForBooleanValue();

        final Boolean isBankDisbursementEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ISBANKDISBURSEMENTENABLED, element);
        baseDataValidator.reset().parameter(LoanProductConstants.ISBANKDISBURSEMENTENABLED).value(isBankDisbursementEnabled).ignoreIfNull()
                .validateForBooleanValue();

        // accounting related data validation
        final Integer accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).notBlank().inMinMaxRange(1, 4);

        final Integer advanceAppropriation = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.ADVANCEAPPROPRIATION,element,Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.ADVANCEAPPROPRIATION).value(advanceAppropriation).isOneOfTheseValues(1,2);
        if(!AdvanceAppropriationOn.isOnDueDate(advanceAppropriation)){
            final Boolean advanceEntryEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION,element);
            baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION).value(advanceEntryEnabled).notBlank();
        }

        final Boolean interestBenefitEnabled = this.fromApiJsonHelper.extractBooleanNamed("interestBenefitEnabled", element);
        baseDataValidator.reset().parameter("interestBenefitEnabled").value(interestBenefitEnabled).ignoreIfNull()
                .validateForBooleanValue().isOneOfTheseValues(true,false);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST, element)){
            Integer foreclosureOnDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST).value(foreclosureOnDueDateInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE, element)){
            Integer foreclosureOnDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE).value(foreclosureOnDueDateCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST, element)){
            Integer foreclosureOtherThanDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST).value(foreclosureOtherThanDueDateInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE, element)){
            Integer foreclosureOtherThanDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE).value(foreclosureOtherThanDueDateCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST, element)){
            Integer foreclosureOneMonthOverdueInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST).value(foreclosureOneMonthOverdueInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE, element)){
            Integer foreclosureOneMonthOverdueCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE).value(foreclosureOneMonthOverdueCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST, element)){
            Integer foreclosureShortPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST).value(foreclosureShortPaidInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE, element)){
            Integer foreclosureShortPaidInterestCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE).value(foreclosureShortPaidInterestCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST, element)){
            Integer foreclosurePrincipalShortPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST).value(foreclosurePrincipalShortPaidInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE, element)){
            Integer foreclosurePrincipalShortPaidCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE).value(foreclosurePrincipalShortPaidCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST, element)){
            Integer foreclosureTwoMonthsOverdueInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST).value(foreclosureTwoMonthsOverdueInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE, element)){
            Integer foreclosureTwoMonthsOverdueCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE).value(foreclosureTwoMonthsOverdueCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE, element)){
            Integer foreclosurePosAdvanceOnDueDate = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE).value(foreclosurePosAdvanceOnDueDate).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST, element)){
            Integer foreclosureAdvanceOnDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST).value(foreclosureAdvanceOnDueDateInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE, element)){
            Integer foreclosureAdvanceOnDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE).value(foreclosureAdvanceOnDueDateCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE, element)){
            Integer foreclosurePosAdvanceOtherThanDueDate = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE).value(foreclosurePosAdvanceOtherThanDueDate).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST, element)){
            Integer foreclosureAdvanceAfterDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST).value(foreclosureAdvanceAfterDueDateInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE, element)){
            Integer foreclosureAdvanceAfterDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE).value(foreclosureAdvanceAfterDueDateCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST, element)){
            Integer foreclosureBackdatedShortPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST).value(foreclosureBackdatedShortPaidInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE, element)){
            Integer foreclosureBackdatedShortPaidInterestCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE).value(foreclosureBackdatedShortPaidInterestCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST, element)){
            Integer foreclosureBackdatedFullyPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST).value(foreclosureBackdatedFullyPaidInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE, element)){
            Integer foreclosureBackdatedFullyPaidInterestCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE).value(foreclosureBackdatedFullyPaidInterestCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST, element)){
            Integer foreclosureBackdatedShortPaidPrincipalInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST).value(foreclosureBackdatedShortPaidPrincipalInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE, element)){
            Integer foreclosureBackdatedShortPaidPrincipalCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE).value(foreclosureBackdatedShortPaidPrincipalCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST, element)){
            Integer foreclosureBackdatedFullyPaidEmiInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST).value(foreclosureBackdatedFullyPaidEmiInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE, element)){
            Integer foreclosureBackdatedFullyPaidEmiCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE).value(foreclosureBackdatedFullyPaidEmiCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST, element)){
            Integer foreclosureBackdatedAdvanceInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST).value(foreclosureBackdatedAdvanceInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE, element)){
            Integer foreclosureBackdatedAdvanceCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE).value(foreclosureBackdatedAdvanceCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_METHOD_TYPE, element)){
            Integer foreclosureMethodType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_METHOD_TYPE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_METHOD_TYPE).value(foreclosureMethodType).ignoreIfNull().isOneOfTheseValues (1,2,3);
        }
        Boolean enableBackDatedDisbursement = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT, element);
        baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT).value(enableBackDatedDisbursement).ignoreIfNull().isOneOfTheseValues (true,false);

        Integer coolingOffThresholdDays = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_THRESHOLD_DAYS, element,Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.COOLING_OFF_THRESHOLD_DAYS).value(coolingOffThresholdDays).ignoreIfNull().inMinMaxRange (1,30);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.COOLING_OFF_APPLICABILITY, element)){
            Boolean coolingOffApplicability = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.COOLING_OFF_APPLICABILITY, element);
            baseDataValidator.reset().parameter(LoanProductConstants.COOLING_OFF_APPLICABILITY).value(coolingOffApplicability).isOneOfTheseValues (true,false);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY, element)){
            Integer coolingOffInterestAndChargeApplicability = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY).value(coolingOffInterestAndChargeApplicability).ignoreIfNull().isOneOfTheseValues (1,2,3,4);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY, element)){
            Integer coolingOffInterestLogicApplicability = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY).value(coolingOffInterestLogicApplicability).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.COOLING_OFF_ROUNDING_MODE, element)){
            final String coolingOffRoundingMode = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.COOLING_OFF_ROUNDING_MODE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.COOLING_OFF_ROUNDING_MODE).value(coolingOffRoundingMode).ignoreIfNull().isRoundingModesExist(coolingOffRoundingMode);
        }

        Integer coolingOffRoundingDecimals = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_ROUNDING_DECIMALS, element,Locale.getDefault());
        baseDataValidator.reset().parameter(LoanProductConstants.COOLING_OFF_ROUNDING_DECIMALS).value(coolingOffRoundingDecimals).ignoreIfNull().isOneOfTheseValues (0,2);

        if (isCashBasedAccounting(accountingRuleType) || isAccrualBasedAccounting(accountingRuleType)) {

            final Long fundAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.FUND_SOURCE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.FUND_SOURCE.getValue()).value(fundAccountId).notNull()
                    .integerGreaterThanZero();

            final Long loanPortfolioAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                    .notNull().integerGreaterThanZero();

            final Long transfersInSuspenseAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue())
                    .value(transfersInSuspenseAccountId).notNull().integerGreaterThanZero();

            final Long incomeFromInterestId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.INCOME_FROM_FEES.getValue(),
                    element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId).notNull()
                    .integerGreaterThanZero();

            final Long incomeFromPenaltyId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromRecoveryAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue())
                    .value(incomeFromRecoveryAccountId).notNull().integerGreaterThanZero();

            final Long writeOffAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                    .notNull().integerGreaterThanZero();

            final Long overpaymentAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.OVERPAYMENT.getValue(),
                    element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.OVERPAYMENT.getValue()).value(overpaymentAccountId).notNull()
                    .integerGreaterThanZero();

            validatePaymentChannelFundSourceMappings(baseDataValidator, element);
            validateChargeToIncomeAccountMappings(baseDataValidator, element);

        }

        if (isAccrualBasedAccounting(accountingRuleType)) {

            final Long receivableInterestAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue())
                    .value(receivableInterestAccountId).notNull().integerGreaterThanZero();

            final Long receivableFeeAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.FEES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                    .notNull().integerGreaterThanZero();

            final Long receivablePenaltyAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue())
                    .value(receivablePenaltyAccountId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, element)) {
            final Boolean useBorrowerCycle = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME).value(useBorrowerCycle)
                    .ignoreIfNull().validateForBooleanValue();
            if (useBorrowerCycle) {
                validateBorrowerCycleVariations(element, baseDataValidator);
            }
        }

        validateMultiDisburseLoanData(baseDataValidator, element);

        validateLoanConfigurableAttributes(baseDataValidator, element);

        validateVariableInstallmentSettings(baseDataValidator, element);

        validateAgeLimitsSettings(baseDataValidator, element);

//        validatePrepayLockingPeriodSettings(baseDataValidator, element);

        validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, null);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.CAN_USE_FOR_TOPUP, element)) {
            final Boolean canUseForTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.CAN_USE_FOR_TOPUP, element);
            baseDataValidator.reset().parameter(LoanProductConstants.CAN_USE_FOR_TOPUP).value(canUseForTopup).validateForBooleanValue();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void duplicateChargeValidation(DataValidatorBuilder baseDataValidator, List<LoanProductFeeData> loanProductChargeData) {

        List<Charge> list =loanProductChargeData.stream().map(LoanProductFeeData::getCharge).collect(Collectors.toList());

        Set<Charge> seen = new HashSet<>();
        Set<Charge> duplicates = list.stream().filter(id -> !seen.add(id)).collect(Collectors.toSet());

        if(!duplicates.isEmpty()){

            baseDataValidator.reset().parameter(LoanProductConstants.COLENDING_CHARGE_PARAMETER_NAME).failWithCode(
                    "duplicate charge exists check the charge and fees",
                    "duplicate charge exists check the charge and fees ");}
    }

    private void validateGstLiablity(BigDecimal gstLiabilityByVcpl, BigDecimal gstLiabilityByPartner,DataValidatorBuilder dataValidatorBuilder) {

        Integer value = Integer.valueOf(BigDecimal.valueOf(100).intValue());
        BigDecimal sum =gstLiabilityByVcpl.add(gstLiabilityByPartner);


        if(sum.intValue() != (value)){
            dataValidatorBuilder.reset().parameter("gstLiabilityByVcpl").failWithCode(
                    "sum of self and partner Gst Liability should be 100",
                    "sum of self and partner Gst Liability should be 100");

        }
    }

    private void colendingChargeValidation(final DataValidatorBuilder baseDataValidator,final List<LoanProductFeeData> loanProductChargeData) {

        for(LoanProductFeeData list :loanProductChargeData){
            final BigDecimal total=BigDecimal.valueOf(100);

            final BigDecimal partnerShare =list.getPartnerShare();
            final BigDecimal selfShare=  list.getSelfShare();
            final BigDecimal totalInterest= partnerShare.add(selfShare).setScale(0) ;
            if(!total.equals(totalInterest)){
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
                        "One of the charge is more than 100","charge.error");}}
        }

    private void validateInterestlAmout(DataValidatorBuilder baseDataValidator, BigDecimal interestRate, BigDecimal totalInterest) {

        final Integer interest=Integer.valueOf(interestRate.intValue());
        final Integer totalInterests=Integer.valueOf(totalInterest.intValue());

        if(interest != totalInterests){
            baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
                    "validation Error",
                    "Sum of self and Partner Interest  must be 100");}}

    private void validatePrincipalAmout(DataValidatorBuilder baseDataValidator, Integer principalShare, Integer toatlPrincipal) {

        if(principalShare != toatlPrincipal){
            baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
                    "validation Error",
                    "Sum of self and Partner Principal Amount must be 100");}}
    private void validateIntAmount(DataValidatorBuilder baseDataValidator, Integer fieldValue, Integer totalShare, String fieldName) {

        final Integer value=Integer.valueOf(fieldValue.intValue());
        final Integer totalSum=Integer.valueOf(totalShare.intValue());

        if( value != totalSum){
            baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
                    "validation Error",
                    "Sum of self and Partner " + fieldName + " must be 100");}
    }

    private void validateDecimalAmount(DataValidatorBuilder baseDataValidator, BigDecimal fieldValue, BigDecimal totalShare, String fieldName) {

        final BigDecimal value=BigDecimal.valueOf(fieldValue.intValue());
        final BigDecimal totalSum=BigDecimal.valueOf(totalShare.intValue());

        if( value != totalSum){
            baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
                    "validation Error",
                    "Sum of self and Partner " + fieldName + " must be 100");}
    }


    private void validateVariableInstallmentSettings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowVariableInstallmentsParamName, element)
                && this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.allowVariableInstallmentsParamName, element)) {

            boolean isEqualAmortization = false;
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
                isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
            }
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("variable.installment", "variable installment");
            }

            Long minimumGapBetweenInstallments = null;
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGapBetweenInstallments, element)) {
                minimumGapBetweenInstallments = this.fromApiJsonHelper.extractLongNamed(LoanProductConstants.minimumGapBetweenInstallments,
                        element);
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).value(minimumGapBetweenInstallments)
                        .notNull();
            } else {
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
                        "is.mandatory.when.allowVariableInstallments.is.true",
                        "minimumGap param is mandatory when allowVariableInstallments is true");
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.maximumGapBetweenInstallments, element)) {
                final Long maximumGapBetweenInstallments = this.fromApiJsonHelper
                        .extractLongNamed(LoanProductConstants.maximumGapBetweenInstallments, element);
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).value(maximumGapBetweenInstallments)
                        .notNull();
                baseDataValidator.reset().parameter(LoanProductConstants.maximumGapBetweenInstallments).value(maximumGapBetweenInstallments)
                        .notNull().longGreaterThanNumber(minimumGapBetweenInstallments);
            }

        } else {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGapBetweenInstallments, element)) {
                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
                        "not.supported.when.allowVariableInstallments.is.false",
                        "minimumGap param is not supported when allowVariableInstallments is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.maximumGapBetweenInstallments, element)) {
                baseDataValidator.reset().parameter(LoanProductConstants.maximumGapBetweenInstallments).failWithCode(
                        "not.supported.when.allowVariableInstallments.is.false",
                        "maximumGap param is not supported when allowVariableInstallments is not supplied or false");
            }

        }
    }

    private void validateAgeLimitsSettings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowAgeLimitsParamName, element)
                && this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.allowAgeLimitsParamName, element)) {


            Long minimumAge = null;
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumAge, element)) {
                minimumAge = this.fromApiJsonHelper.extractLongNamed(LoanProductConstants.minimumAge,
                        element);
                baseDataValidator.reset().parameter(LoanProductConstants.minimumAge).value(minimumAge)
                        .notNull();
            } else {
                baseDataValidator.reset().parameter(LoanProductConstants.minimumAge).failWithCode(
                        "is.mandatory.when.allowAgeLimits.is.true",
                        "minimumAge param is mandatory when allowAgeLimits is true");
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.maximumAge, element)) {
                final Long maximumAge = this.fromApiJsonHelper
                        .extractLongNamed(LoanProductConstants.maximumAge, element);
                baseDataValidator.reset().parameter(LoanProductConstants.maximumAge).value(maximumAge)
                        .notNull();

            }

        } else {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumAge, element)) {
                baseDataValidator.reset().parameter(LoanProductConstants.minimumAge).failWithCode(
                        "not.supported.when.allowAgeLimits.is.false",
                        "minimumAge param is not supported when allowAgeLimits is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.maximumAge, element)) {
                baseDataValidator.reset().parameter(LoanProductConstants.maximumAge).failWithCode(
                        "not.supported.when.allowAgeLimits.is.false",
                        "maxAge param is not supported when allowAgeLimits is not supplied or false");
            }

        }
    }

//    private void validatePrepayLockingPeriodSettings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
//        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowPrepaidLockingPeriodParamName, element)
//                && this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.allowPrepaidLockingPeriodParamName, element)) {
//
//
//            Long prepayLockingPeriod = null;
//            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.prepayLockingPeriod, element)) {
//                prepayLockingPeriod = this.fromApiJsonHelper.extractLongNamed(LoanProductConstants.prepayLockingPeriod,
//                        element);
//                baseDataValidator.reset().parameter(LoanProductConstants.prepayLockingPeriod).value(prepayLockingPeriod)
//                        .notNull();
//            } else {
//                baseDataValidator.reset().parameter(LoanProductConstants.prepayLockingPeriod).failWithCode(
//                        "is.mandatory.when.allowPrepaidLockingPeriod.is.true",
//                        "prepayLockPeriod param is mandatory when allowPrepaidLockingPeriod is true");
//            }
//
//            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.prepayLockingPeriod, element)) {
//                baseDataValidator.reset().parameter(LoanProductConstants.prepayLockingPeriod).failWithCode(
//                        "not.supported.when.allowPrepaidLockingPeriod.is.false",
//                        "prepayLockPeriod param is not supported when allowPrepaidLockingPeriod is not supplied or false");
//            }
//
//        }
//    }


    private void validateLoanConfigurableAttributes(final DataValidatorBuilder baseDataValidator, final JsonElement element) {

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowAttributeOverridesParamName, element)) {

            final JsonObject object = element.getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName);

            // Validate that parameter names are allowed
            Set<String> supportedConfigurableAttributes = new HashSet<>();
            Collections.addAll(supportedConfigurableAttributes, supportedloanConfigurableAttributes);
            this.fromApiJsonHelper.checkForUnsupportedNestedParameters(LoanProductConstants.allowAttributeOverridesParamName, object,
                    supportedConfigurableAttributes);

            Integer length = supportedloanConfigurableAttributes.length;

            for (int i = 0; i < length; i++) {
                /* Validate the attribute names */
                if (this.fromApiJsonHelper.parameterExists(supportedloanConfigurableAttributes[i], object)) {
                    Boolean loanConfigurationAttributeValue = this.fromApiJsonHelper
                            .extractBooleanNamed(supportedloanConfigurableAttributes[i], object);
                    /* Validate the boolean value */
                    baseDataValidator.reset().parameter(LoanProductConstants.allowAttributeOverridesParamName)
                            .value(loanConfigurationAttributeValue).notNull().validateForBooleanValue();
                }

            }
        }
    }

    private void validateMultiDisburseLoanData(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        Boolean multiDisburseLoan = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME, element)) {
            multiDisburseLoan = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME).value(multiDisburseLoan)
                    .ignoreIfNull().validateForBooleanValue();
        }

        boolean isEqualAmortization = false;
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
        }
        if (isEqualAmortization && multiDisburseLoan) {
            throw new EqualAmortizationUnsupportedFeatureException("tranche.disbursal", "tranche disbursal");
        }

        if (multiDisburseLoan) {
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME, element)) {
                final BigDecimal outstandingLoanBalance = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME, element);
                baseDataValidator.reset().parameter(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME)
                        .value(outstandingLoanBalance).ignoreIfNull().zeroOrPositiveAmount();
            }

            final Integer maxTrancheCount = this.fromApiJsonHelper
                    .extractIntegerNamed(LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME).value(maxTrancheCount).notNull()
                    .integerGreaterThanZero();

            final Integer interestType = this.fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
            baseDataValidator.reset().parameter("interestType").value(interestType).ignoreIfNull()
                    .integerSameAsNumber(InterestMethod.DECLINING_BALANCE.getValue());
        }

        final String overAppliedCalculationType = this.fromApiJsonHelper.extractStringNamed("overAppliedCalculationType", element);
        baseDataValidator.reset().parameter("overAppliedCalculationType").value(overAppliedCalculationType).notExceedingLengthOf(10);
    }

    private void validateInterestRecalculationParams(final JsonElement element, final DataValidatorBuilder baseDataValidator,
                                                     final LoanProduct loanProduct) {

        /**
         * { @link InterestRecalculationCompoundingMethod }
         */
        InterestRecalculationCompoundingMethod compoundingMethod = null;

        if (loanProduct == null || this.fromApiJsonHelper
                .parameterExists(LoanProductConstants.interestRecalculationCompoundingMethodParameterName, element)) {
            final Integer interestRecalculationCompoundingMethod = this.fromApiJsonHelper.extractIntegerNamed(
                    LoanProductConstants.interestRecalculationCompoundingMethodParameterName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.interestRecalculationCompoundingMethodParameterName)
                    .value(interestRecalculationCompoundingMethod).notNull().inMinMaxRange(0, 3);
            if (interestRecalculationCompoundingMethod != null) {
                compoundingMethod = InterestRecalculationCompoundingMethod.fromInt(interestRecalculationCompoundingMethod);
            }
        }

        if (compoundingMethod == null) {
            if (loanProduct == null) {
                compoundingMethod = InterestRecalculationCompoundingMethod.NONE;
            } else {
                compoundingMethod = InterestRecalculationCompoundingMethod
                        .fromInt(loanProduct.getProductInterestRecalculationDetails().getInterestRecalculationCompoundingMethod());
            }
        }

        /**
         * { @link LoanRescheduleStrategyMethod }
         */
        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.rescheduleStrategyMethodParameterName, element)) {
            final Integer rescheduleStrategyMethod = this.fromApiJsonHelper
                    .extractIntegerNamed(LoanProductConstants.rescheduleStrategyMethodParameterName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.rescheduleStrategyMethodParameterName).value(rescheduleStrategyMethod)
                    .notNull().inMinMaxRange(1, 3);
        }

        RecalculationFrequencyType frequencyType = null;

        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationRestFrequencyTypeParameterName, element)) {
            final Integer recalculationRestFrequencyType = this.fromApiJsonHelper
                    .extractIntegerNamed(LoanProductConstants.recalculationRestFrequencyTypeParameterName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.recalculationRestFrequencyTypeParameterName)
                    .value(recalculationRestFrequencyType).notNull().inMinMaxRange(1, 4);
            if (recalculationRestFrequencyType != null) {
                frequencyType = RecalculationFrequencyType.fromInt(recalculationRestFrequencyType);
            }
        }

        if (frequencyType == null) {
            if (loanProduct == null) {
                frequencyType = RecalculationFrequencyType.INVALID;
            } else {
                frequencyType = loanProduct.getProductInterestRecalculationDetails().getRestFrequencyType();
            }
        }

        if (!frequencyType.isSameAsRepayment()) {
            if (loanProduct == null || this.fromApiJsonHelper
                    .parameterExists(LoanProductConstants.recalculationRestFrequencyIntervalParameterName, element)) {
                final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(
                        LoanProductConstants.recalculationRestFrequencyIntervalParameterName, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.recalculationRestFrequencyIntervalParameterName)
                        .value(recurrenceInterval).notNull();
            }
            if (loanProduct == null
                    || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationRestFrequencyNthDayParamName, element)
                    || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationRestFrequencyWeekdayParamName, element)) {
                CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator,
                        LoanProductConstants.recalculationRestFrequencyNthDayParamName,
                        LoanProductConstants.recalculationRestFrequencyWeekdayParamName, element, this.fromApiJsonHelper);
            }
            if (loanProduct == null
                    || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationRestFrequencyOnDayParamName, element)) {
                final Integer recalculationRestFrequencyOnDay = this.fromApiJsonHelper
                        .extractIntegerNamed(LoanProductConstants.recalculationRestFrequencyOnDayParamName, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.recalculationRestFrequencyOnDayParamName)
                        .value(recalculationRestFrequencyOnDay).ignoreIfNull().inMinMaxRange(1, 28);
            }
        }

        if (compoundingMethod.isCompoundingEnabled()) {
            RecalculationFrequencyType compoundingfrequencyType = null;

            if (loanProduct == null || this.fromApiJsonHelper
                    .parameterExists(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName, element)) {
                final Integer recalculationCompoundingFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(
                        LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName, element, Locale.getDefault());
                baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName)
                        .value(recalculationCompoundingFrequencyType).notNull().inMinMaxRange(1, 4);
                if (recalculationCompoundingFrequencyType != null) {
                    compoundingfrequencyType = RecalculationFrequencyType.fromInt(recalculationCompoundingFrequencyType);
                    if (!compoundingfrequencyType.isSameAsRepayment()) {
                        PeriodFrequencyType repaymentFrequencyType = null;
                        if (this.fromApiJsonHelper.parameterExists("repaymentFrequencyType", element)) {
                            Integer repaymentFrequencyTypeVal = this.fromApiJsonHelper.extractIntegerNamed("repaymentFrequencyType",
                                    element, Locale.getDefault());
                            repaymentFrequencyType = PeriodFrequencyType.fromInt(repaymentFrequencyTypeVal);
                        } else if (loanProduct != null) {
                            repaymentFrequencyType = loanProduct.getLoanProductRelatedDetail().getRepaymentPeriodFrequencyType();
                        }
                        if (!compoundingfrequencyType.isSameFrequency(repaymentFrequencyType)) {
                            baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName)
                                    .value(recalculationCompoundingFrequencyType).failWithCode("must.be.same.as.repayment.frequency");
                        }
                    }
                }
            }

            if (compoundingfrequencyType == null) {
                if (loanProduct == null) {
                    compoundingfrequencyType = RecalculationFrequencyType.INVALID;
                } else {
                    compoundingfrequencyType = loanProduct.getProductInterestRecalculationDetails().getCompoundingFrequencyType();
                }
            }

            if (!compoundingfrequencyType.isSameAsRepayment()) {
                if (loanProduct == null || this.fromApiJsonHelper
                        .parameterExists(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName, element)) {
                    final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(
                            LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName, element, Locale.getDefault());
                    Integer repaymentEvery = null;
                    if (loanProduct == null) {
                        repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.repaymentEveryParamName, element);
                    } else {
                        repaymentEvery = loanProduct.getLoanProductRelatedDetail().getRepayEvery();
                    }

                    baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName)
                            .value(recurrenceInterval).notNull().integerInMultiplesOfNumber(repaymentEvery);
                }
                if (loanProduct == null
                        || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName,
                        element)
                        || this.fromApiJsonHelper.parameterExists(LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName,
                        element)) {
                    CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator,
                            LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName,
                            LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName, element, this.fromApiJsonHelper);
                }
                if (loanProduct == null || this.fromApiJsonHelper
                        .parameterExists(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName, element)) {
                    final Integer recalculationRestFrequencyOnDay = this.fromApiJsonHelper.extractIntegerNamed(
                            LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName, element, Locale.getDefault());
                    baseDataValidator.reset().parameter(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName)
                            .value(recalculationRestFrequencyOnDay).ignoreIfNull().inMinMaxRange(1, 28);
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName, element)) {
            final Boolean isArrearsBasedOnOriginalSchedule = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName)
                    .value(isArrearsBasedOnOriginalSchedule).notNull().isOneOfTheseValues(true, false);
        }
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, element)) {
            final Boolean isCompoundingToBePostedAsTransactions = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName)
                    .value(isCompoundingToBePostedAsTransactions).notNull().isOneOfTheseValues(true, false);
        }

        final Integer preCloseInterestCalculationStrategy = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.preClosureInterestCalculationStrategyParamName)
                .value(preCloseInterestCalculationStrategy).ignoreIfNull().inMinMaxRange(
                        LoanPreClosureInterestCalculationStrategy.getMinValue(), LoanPreClosureInterestCalculationStrategy.getMaxValue());
    }

    public void validateForUpdate(final String json, final LoanProduct loanProduct,final Partner partner,final  List<LoanProductFeeData> loanProductChargeData) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(LoanProductConstants.LOAN_PRODUCT_RESOURCE);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_PRODUCT_NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_PRODUCT_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOAN_PRODUCT_NAME).value(name).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.SHORT_NAME, element)) {
            final String shortName = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.SHORT_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.SHORT_NAME).value(shortName).notBlank().notExceedingLengthOf(10);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_ACC_NO_PREFERENCE, element)) {
            final String loanAccNoPreference = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_ACC_NO_PREFERENCE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOAN_ACC_NO_PREFERENCE).value(loanAccNoPreference).notBlank().notExceedingLengthOf(11);
        }

        if (this.fromApiJsonHelper.parameterExists("description", element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed("description", element);
            baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists("includeInBorrowerCycle", element)) {
            final Boolean includeInBorrowerCycle = this.fromApiJsonHelper.extractBooleanNamed("includeInBorrowerCycle", element);
            baseDataValidator.reset().parameter("includeInBorrowerCycle").value(includeInBorrowerCycle).ignoreIfNull()
                    .validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.CURRENCY_CODE, element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.CURRENCY_CODE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.CURRENCY_CODE).value(currencyCode).notBlank().notExceedingLengthOf(3);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.EMI_DECIMAL, element)) {
            final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.EMI_DECIMAL, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.EMI_DECIMAL).value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IN_MULTIPLES_OF, element)) {
            final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.IN_MULTIPLES_OF, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.IN_MULTIPLES_OF).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();
        }

        final String minPrincipalParameterName = "minPrincipal";
        BigDecimal minPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(minPrincipalParameterName, element)) {
            minPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minPrincipalParameterName, element);
            baseDataValidator.reset().parameter(minPrincipalParameterName).value(minPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        final String maxPrincipalParameterName = "maxPrincipal";
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(maxPrincipalParameterName, element)) {
            maxPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxPrincipalParameterName, element);
            baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.PRINCIPAL, element)) {
            final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanProductConstants.PRINCIPAL, element);
            baseDataValidator.reset().parameter(LoanProductConstants.PRINCIPAL).value(principal).positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("inArrearsTolerance", element)) {
            final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
            baseDataValidator.reset().parameter("inArrearsTolerance").value(inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();
        }

        final String minNumberOfRepaymentsParameterName = "minNumberOfRepayments";
        Integer minNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(minNumberOfRepaymentsParameterName, element)) {
            minNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(minNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(minNumberOfRepaymentsParameterName).value(minNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        final String maxNumberOfRepaymentsParameterName = "maxNumberOfRepayments";
        Integer maxNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(maxNumberOfRepaymentsParameterName, element)) {
            maxNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(maxNumberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments).ignoreIfNull()
                    .integerGreaterThanZero();
        }
        final BigDecimal selfInterestRate = this.fromApiJsonHelper.extractBigDecimalNamed("selfInterestRate", element,Locale.getDefault());
        baseDataValidator.reset().parameter("selfInterestRate").value(selfInterestRate).ignoreIfNull().positiveAmount();

        final Integer principalShare = this.fromApiJsonHelper.extractIntegerNamed("principalShare", element,Locale.getDefault());
        baseDataValidator.reset().parameter("principalShare").value(principalShare).ignoreIfNull().integerZeroOrGreater();

        final Integer selfPrincipalShare = this.fromApiJsonHelper.extractIntegerNamed("selfPrincipalShare", element, Locale.getDefault());
        baseDataValidator.reset().parameter("selfPrincipalShare").value(selfPrincipalShare).ignoreIfNull().integerZeroOrGreater();

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.NUMBER_OF_REPAYMENTS, element)) {
            final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.NUMBER_OF_REPAYMENTS, element);
            baseDataValidator.reset().parameter(LoanProductConstants.NUMBER_OF_REPAYMENTS).value(numberOfRepayments).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.repaymentEveryParamName, element)) {
            final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.repaymentEveryParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.repaymentEveryParamName).value(repaymentEvery).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists("repaymentFrequencyType", element)) {
            final Integer repaymentFrequencyType = this.fromApiJsonHelper.extractIntegerNamed("repaymentFrequencyType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("repaymentFrequencyType").value(repaymentFrequencyType).notNull().inMinMaxRange(0, 3);
        }

        if (this.fromApiJsonHelper.parameterExists("transactionProcessingStrategyId", element)) {
            final Long transactionProcessingStrategyId = this.fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId",
                    element);
            baseDataValidator.reset().parameter("transactionProcessingStrategyId").value(transactionProcessingStrategyId).notNull()
                    .integerGreaterThanZero();
        }

        final Integer partnerPrincipalShare = this.fromApiJsonHelper.extractIntegerNamed("partnerPrincipalShare", element, Locale.getDefault());
        baseDataValidator.reset().parameter("partnerPrincipalShare").value(partnerPrincipalShare).ignoreIfNull().integerZeroOrGreater();

        final BigDecimal partnerInterestRate = this.fromApiJsonHelper.extractBigDecimalNamed("partnerInterestRate", element, Locale.getDefault());
        baseDataValidator.reset().parameter("partnerInterestRate").value(partnerInterestRate).ignoreIfNull();

        final BigDecimal interestRate = this.fromApiJsonHelper.extractBigDecimalNamed("interestRate", element,Locale.getDefault());
        baseDataValidator.reset().parameter("interestRate").value(interestRate).ignoreIfNull().positiveAmount();

//        final BigDecimal vcplHurdleRate = this.fromApiJsonHelper.extractBigDecimalNamed("vcplHurdleRate", element,Locale.getDefault());
//        baseDataValidator.reset().parameter("vcplHurdleRate").value(vcplHurdleRate).notNull().positiveAmount();

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOANPRODUCTCLASS, element)) {
            final Integer loanProductClassId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.LOANPRODUCTCLASS, element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOANPRODUCTCLASS).value(loanProductClassId).ignoreIfNull().integerGreaterThanZero().loanProductClassIdExist(this.codeValueRepositoryWrapper,loanProductClassId);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOANPRODUCTTYPE, element)) {
            final Integer loanProductTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.LOANPRODUCTTYPE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOANPRODUCTTYPE).value(loanProductTypeId).ignoreIfNull().integerGreaterThanZero().loanProductTypeIdExist(this.codeValueRepositoryWrapper,loanProductTypeId);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ASSETCLASS, element)) {
            final Integer assetClassId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.ASSETCLASS, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ASSETCLASS).value(assetClassId).integerGreaterThanZero().ignoreIfNull().assetClassIdExist(this.codeValueRepositoryWrapper,assetClassId);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FRAMEWORK, element)) {
            final Integer frameWorkId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.FRAMEWORK, element);
            baseDataValidator.reset().parameter(LoanProductConstants.FRAMEWORK).value(frameWorkId).ignoreIfNull().integerGreaterThanZero().frameWorkIdExist(this.codeValueRepositoryWrapper,frameWorkId);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOANTYPE, element)) {
            final Integer loanTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.LOANTYPE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.LOANTYPE).value(loanTypeId).ignoreIfNull().integerGreaterThanZero().loanTypeIdExist(this.codeValueRepositoryWrapper,loanTypeId);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FLDGLOGIC, element)) {
            final Integer fldgLogicID = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.FLDGLOGIC, element);
            baseDataValidator.reset().parameter(LoanProductConstants.FLDGLOGIC).value(fldgLogicID).ignoreIfNull().integerGreaterThanZero().fldgLogicIdExist(this.codeValueRepositoryWrapper,fldgLogicID);
        }


                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.INSURANCEAPPLICABILITY, element)) {
            final Integer insuranceApplicabilityId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.INSURANCEAPPLICABILITY, element);
            baseDataValidator.reset().parameter(LoanProductConstants.INSURANCEAPPLICABILITY).value(insuranceApplicabilityId).ignoreIfNull().integerGreaterThanZero().insuranceApplicabilityIdExist(codeValueRepositoryWrapper,insuranceApplicabilityId);
        }
        if(!loanProductChargeData.isEmpty()){

            colendingChargeValidation(baseDataValidator,loanProductChargeData);
            duplicateChargeValidation(baseDataValidator,loanProductChargeData);
        }
        final Integer totalPrincipal= sum(selfPrincipalShare,partnerPrincipalShare);

        validatePrincipalAmout(baseDataValidator,principalShare,totalPrincipal );
        final BigDecimal totalInterest= (BigDecimal) add(selfInterestRate,partnerInterestRate);

        validateInterestlAmout(baseDataValidator,interestRate,totalInterest );

        final Integer brokenInterestStrategy=this.fromApiJsonHelper.extractIntegerWithLocaleNamed("brokenInterestStrategy",element);
        baseDataValidator.reset().parameter("brokenInterestStrategy").value(brokenInterestStrategy).ignoreIfNull().inMinMaxRange(1,5);

        final Integer disbursement=this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.DISBURSEMENT,element);
        baseDataValidator.reset().parameter(LoanProductConstants.DISBURSEMENT).value(disbursement).ignoreIfNull().inMinMaxRange(1,4);


        final Integer collection =this.fromApiJsonHelper.extractIntegerWithLocaleNamed("collection",element);
        baseDataValidator.reset().parameter("collection").value(collection).ignoreIfNull().inMinMaxRange(1,5);

        final Integer transactionTypePreference =this.fromApiJsonHelper.extractIntegerWithLocaleNamed("transactionTypePreference",element);
        baseDataValidator.reset().parameter("transactionTypePreference").value(transactionTypePreference).ignoreIfNull().inMinMaxRange(1,4);

        final BigDecimal aumSlabRate = this.fromApiJsonHelper.extractBigDecimalNamed("aumSlabRate", element, Locale.getDefault());
        baseDataValidator.reset().parameter("aumSlabRate").value(aumSlabRate).ignoreIfNull().positiveAmount();

        final BigDecimal gstLiabilityByVcpl = this.fromApiJsonHelper.extractBigDecimalNamed("gstLiabilityByVcpl", element, Locale.getDefault());
        baseDataValidator.reset().parameter("gstLiabilityByVcpl").value(gstLiabilityByVcpl).ignoreIfNull().positiveAmount();

        final BigDecimal gstLiabilityByPartner = this.fromApiJsonHelper.extractBigDecimalNamed("gstLiabilityByPartner", element, Locale.getDefault());
        baseDataValidator.reset().parameter("gstLiabilityByPartner").value(gstLiabilityByPartner).ignoreIfNull().zeroOrPositiveAmount();



        final BigDecimal vcplShareInBrokenInterest = this.fromApiJsonHelper.extractBigDecimalNamed("vcplShareInBrokenInterest", element, Locale.getDefault());
        baseDataValidator.reset().parameter("vcplShareInBrokenInterest").value(vcplShareInBrokenInterest).ignoreIfNull().positiveAmount();

        final BigDecimal partnerShareInBrokenInterest = this.fromApiJsonHelper.extractBigDecimalNamed("partnerShareInBrokenInterest", element, Locale.getDefault());
        baseDataValidator.reset().parameter("partnerShareInBrokenInterest").value(partnerShareInBrokenInterest).ignoreIfNull().zeroOrPositiveAmount();

        final Integer monitoringTriggerPar30 = this.fromApiJsonHelper.extractIntegerNamed("monitoringTriggerPar30", element, Locale.getDefault());
        baseDataValidator.reset().parameter("monitoringTriggerPar30").value(monitoringTriggerPar30).ignoreIfNull().integerZeroOrGreater();

        final Integer monitoringTriggerPar90 = this.fromApiJsonHelper.extractIntegerNamed("monitoringTriggerPar90", element, Locale.getDefault());
        baseDataValidator.reset().parameter("monitoringTriggerPar90").value(monitoringTriggerPar90).ignoreIfNull().integerZeroOrGreater();


        // grace validation
        if (this.fromApiJsonHelper.parameterExists("graceOnPrincipalPayment", element)) {
            final Integer graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment",
                    element);
            baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(graceOnPrincipalPayment).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("graceOnInterestPayment", element)) {
            final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
            baseDataValidator.reset().parameter("graceOnInterestPayment").value(graceOnInterestPayment).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("graceOnInterestCharged", element)) {
            final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
            baseDataValidator.reset().parameter("graceOnInterestCharged").value(graceOnInterestCharged).zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element)) {
            final Integer graceOnArrearsAgeing = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME).value(graceOnArrearsAgeing)
                    .integerZeroOrGreater();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, element)) {
            final Integer overdueDaysForNPA = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME).value(overdueDaysForNPA)
                    .integerZeroOrGreater();
        }

        Integer amortizationType = null;
        if (this.fromApiJsonHelper.parameterExists("amortizationType", element)) {
            amortizationType = this.fromApiJsonHelper.extractIntegerNamed("amortizationType", element, Locale.getDefault());
            baseDataValidator.reset().parameter("amortizationType").value(amortizationType).notNull().inMinMaxRange(0, 1);
        }

        if (this.fromApiJsonHelper.parameterExists("interestType", element)) {
            final Integer interestType = this.fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
            baseDataValidator.reset().parameter("interestType").value(interestType).notNull().inMinMaxRange(0, 1);
        }
        Integer interestCalculationPeriodType = loanProduct.getLoanProductRelatedDetail().getInterestCalculationPeriodMethod().getValue();
        if (this.fromApiJsonHelper.parameterExists("interestCalculationPeriodType", element)) {
            interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerNamed("interestCalculationPeriodType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("interestCalculationPeriodType").value(interestCalculationPeriodType).notNull()
                    .inMinMaxRange(0, 1);
        }

        if (this.fromApiJsonHelper.parameterExists("acceptedStartDate", element)) {
            final Integer acceptedStartDate = this.fromApiJsonHelper.extractIntegerNamed("acceptedStartDate", element, Locale.getDefault());
            baseDataValidator.reset().parameter("acceptedStartDate").value(acceptedStartDate).ignoreIfNull().integerZeroOrGreater();
        }

        if (this.fromApiJsonHelper.parameterExists("acceptedEndDate", element)) {
            final Integer acceptedEndDate = this.fromApiJsonHelper.extractIntegerNamed("acceptedEndDate", element, Locale.getDefault());
            baseDataValidator.reset().parameter("acceptedEndDate").value(acceptedEndDate).ignoreIfNull().integerZeroOrGreater();
        }

        if (this.fromApiJsonHelper.parameterExists("acceptedDate", element)) {
            final Integer acceptedDate = this.fromApiJsonHelper.extractIntegerNamed("acceptedDate", element, Locale.getDefault());
            baseDataValidator.reset().parameter("acceptedDate").value(acceptedDate).ignoreIfNull().integerZeroOrGreater();
        }

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.IN_MULTIPLES_OF, element)){
            final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.IN_MULTIPLES_OF, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.IN_MULTIPLES_OF).value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();}

        Integer emiDecimalRegex = null;
        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.EMIDECIMALREGEX, element)){
            emiDecimalRegex = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.EMIDECIMALREGEX, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.EMIDECIMALREGEX).value(emiDecimalRegex).notNull().integerZeroOrGreater().isOneOfTheseValues(0,1,2,3,4);}

        Integer decimalRounds = null;
        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.EMIMULTIPLES, element)){
            decimalRounds = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.EMIMULTIPLES, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.EMIMULTIPLES).value(decimalRounds).notNull().integerZeroOrGreater();}

/*        if(Objects.nonNull(emiDecimalRegex) && Objects.nonNull(decimalRounds) && emiDecimalRegex > decimalRounds){
            baseDataValidator.reset().parameter(LoanProductConstants.EMIDECIMALREGEX).value(decimalRounds).integerEqualToOrGreaterThanNumber(emiDecimalRegex);
        }*/

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.INTERESTROUNDINGMODE, element)){
            final String interestRoundingMode = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.INTERESTROUNDINGMODE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.INTERESTROUNDINGMODE).value(interestRoundingMode).notNull();}

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.INTERESTDECIMALREGEX, element)){
            final Integer interestDecimalRegex = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.INTERESTDECIMALREGEX, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.INTERESTDECIMALREGEX).value(interestDecimalRegex).notNull().integerZeroOrGreater().isOneOfTheseValues(0,1,2,3,4);}



        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.INTERESTDECIMAL, element)) {
            final Integer interestDecimal = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.INTERESTDECIMAL, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.INTERESTDECIMAL).value(interestDecimal).notNull().integerZeroOrGreater();
        }

        /**
         * { @link DaysInYearType }
         */
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, element)) {
            final Integer daysInYearType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME,
                    element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME).value(daysInYearType).notNull()
                    .isOneOfTheseValues(1, 360, 364, 365);
        }

        /**
         * { @link DaysInMonthType }
         */
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, element)) {
            final Integer daysInMonthType = this.fromApiJsonHelper
                    .extractIntegerNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME, element, Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME).value(daysInMonthType).notNull()
                    .isOneOfTheseValues(1, 30);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME,
                element)) {
            Boolean npaChangeConfig = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME)
                    .value(npaChangeConfig).notNull().isOneOfTheseValues(true, false);
        }

        boolean isEqualAmortization = loanProduct.isEqualAmortization();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, element);
            baseDataValidator.reset().parameter(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM).value(isEqualAmortization).ignoreIfNull()
                    .validateForBooleanValue();
        }


        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ISPENNYDROPENABLED, element)) {
           final Boolean isPennyDropEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ISPENNYDROPENABLED, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ISPENNYDROPENABLED).value(isPennyDropEnabled).ignoreIfNull()
                    .validateForBooleanValue();
        }


        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ISBANKDISBURSEMENTENABLED, element)) {
            final Boolean isBankDisbursementEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ISBANKDISBURSEMENTENABLED, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ISBANKDISBURSEMENTENABLED).value(isBankDisbursementEnabled).ignoreIfNull()
                    .validateForBooleanValue();
        }

        // Interest recalculation settings
        Boolean isInterestRecalculationEnabled = loanProduct.isInterestRecalculationEnabled();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, element)) {
            isInterestRecalculationEnabled = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME)
                    .value(isInterestRecalculationEnabled).notNull().isOneOfTheseValues(true, false);
        }


        if (isInterestRecalculationEnabled != null) {
            if (isInterestRecalculationEnabled) {
                if (isEqualAmortization) {
                    throw new EqualAmortizationUnsupportedFeatureException("interest.recalculation", "interest recalculation");
                }
                validateInterestRecalculationParams(element, baseDataValidator, loanProduct);
            }
        }

        // interest rates
        boolean isLinkedToFloatingInterestRates = loanProduct.isLinkedToFloatingInterestRate();
        if (this.fromApiJsonHelper.parameterExists("isLinkedToFloatingInterestRates", element)) {
            isLinkedToFloatingInterestRates = this.fromApiJsonHelper.extractBooleanNamed("isLinkedToFloatingInterestRates", element);
        }
        if (isLinkedToFloatingInterestRates) {
            if (isEqualAmortization) {
                throw new EqualAmortizationUnsupportedFeatureException("floating.interest.rate", "floating interest rate");
            }
            if (this.fromApiJsonHelper.parameterExists("interestRatePerPeriod", element)) {
                baseDataValidator.reset().parameter("interestRatePerPeriod").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "interestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("minInterestRatePerPeriod", element)) {
                baseDataValidator.reset().parameter("minInterestRatePerPeriod").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "minInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("maxInterestRatePerPeriod", element)) {
                baseDataValidator.reset().parameter("maxInterestRatePerPeriod").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "maxInterestRatePerPeriod param is not supported when isLinkedToFloatingInterestRates is true");
            }

            if (this.fromApiJsonHelper.parameterExists("interestRateFrequencyType", element)) {
                baseDataValidator.reset().parameter("interestRateFrequencyType").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.true",
                        "interestRateFrequencyType param is not supported when isLinkedToFloatingInterestRates is true");
            }

            Integer interestType = this.fromApiJsonHelper.parameterExists("interestType", element)
                    ? this.fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault())
                    : loanProduct.getLoanProductRelatedDetail().getInterestMethod().getValue();
            if ((interestType == null || !interestType.equals(InterestMethod.DECLINING_BALANCE.getValue()))
                    || (isInterestRecalculationEnabled == null || isInterestRecalculationEnabled == false)) {
                baseDataValidator.reset().parameter("isLinkedToFloatingInterestRates").failWithCode(
                        "supported.only.for.declining.balance.interest.recalculation.enabled",
                        "Floating interest rates are supported only for declining balance and interest recalculation enabled loan products");
            }

            Long floatingRatesId = loanProduct.getFloatingRates() == null ? null : loanProduct.getFloatingRates().getFloatingRate().getId();
            if (this.fromApiJsonHelper.parameterExists("floatingRatesId", element)) {
                floatingRatesId = this.fromApiJsonHelper.extractLongNamed("floatingRatesId", element);
            }
            baseDataValidator.reset().parameter("floatingRatesId").value(floatingRatesId).notNull();

            BigDecimal interestRateDifferential = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().getInterestRateDifferential();
            if (this.fromApiJsonHelper.parameterExists("interestRateDifferential", element)) {
                interestRateDifferential = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRateDifferential", element);
            }
            baseDataValidator.reset().parameter("interestRateDifferential").value(interestRateDifferential).notNull()
                    .zeroOrPositiveAmount();

            final String minDifferentialLendingRateParameterName = "minDifferentialLendingRate";
            BigDecimal minDifferentialLendingRate = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().getMinDifferentialLendingRate();
            if (this.fromApiJsonHelper.parameterExists(minDifferentialLendingRateParameterName, element)) {
                minDifferentialLendingRate = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(minDifferentialLendingRateParameterName, element);
            }
            baseDataValidator.reset().parameter(minDifferentialLendingRateParameterName).value(minDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String defaultDifferentialLendingRateParameterName = "defaultDifferentialLendingRate";
            BigDecimal defaultDifferentialLendingRate = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().getDefaultDifferentialLendingRate();
            if (this.fromApiJsonHelper.parameterExists(defaultDifferentialLendingRateParameterName, element)) {
                defaultDifferentialLendingRate = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(defaultDifferentialLendingRateParameterName, element);
            }
            baseDataValidator.reset().parameter(defaultDifferentialLendingRateParameterName).value(defaultDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            final String maxDifferentialLendingRateParameterName = "maxDifferentialLendingRate";
            BigDecimal maxDifferentialLendingRate = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().getMaxDifferentialLendingRate();
            if (this.fromApiJsonHelper.parameterExists(maxDifferentialLendingRateParameterName, element)) {
                maxDifferentialLendingRate = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(maxDifferentialLendingRateParameterName, element);
            }
            baseDataValidator.reset().parameter(maxDifferentialLendingRateParameterName).value(maxDifferentialLendingRate).notNull()
                    .zeroOrPositiveAmount();

            if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                if (minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter("defaultDifferentialLendingRate").value(defaultDifferentialLendingRate)
                            .notLessThanMin(minDifferentialLendingRate);
                }
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                if (minDifferentialLendingRate != null && minDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter("maxDifferentialLendingRate").value(maxDifferentialLendingRate)
                            .notLessThanMin(minDifferentialLendingRate);
                }
            }

            if (maxDifferentialLendingRate != null && maxDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                if (defaultDifferentialLendingRate != null && defaultDifferentialLendingRate.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter("maxDifferentialLendingRate").value(maxDifferentialLendingRate)
                            .notLessThanMin(defaultDifferentialLendingRate);
                }
            }

            Boolean isFloatingInterestRateCalculationAllowed = loanProduct.getFloatingRates() == null ? null
                    : loanProduct.getFloatingRates().isFloatingInterestRateCalculationAllowed();
            if (this.fromApiJsonHelper.parameterExists("isFloatingInterestRateCalculationAllowed", element)) {
                isFloatingInterestRateCalculationAllowed = this.fromApiJsonHelper
                        .extractBooleanNamed("isFloatingInterestRateCalculationAllowed", element);
            }
            baseDataValidator.reset().parameter("isFloatingInterestRateCalculationAllowed").value(isFloatingInterestRateCalculationAllowed)
                    .notNull().isOneOfTheseValues(true, false);
        } else {
            if (this.fromApiJsonHelper.parameterExists("floatingRatesId", element)) {
                baseDataValidator.reset().parameter("floatingRatesId").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "floatingRatesId param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("interestRateDifferential", element)) {
                baseDataValidator.reset().parameter("interestRateDifferential").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "interestRateDifferential param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("minDifferentialLendingRate", element)) {
                baseDataValidator.reset().parameter("minDifferentialLendingRate").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "minDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("defaultDifferentialLendingRate", element)) {
                baseDataValidator.reset().parameter("defaultDifferentialLendingRate").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "defaultDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("maxDifferentialLendingRate", element)) {
                baseDataValidator.reset().parameter("maxDifferentialLendingRate").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "maxDifferentialLendingRate param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            if (this.fromApiJsonHelper.parameterExists("isFloatingInterestRateCalculationAllowed", element)) {
                baseDataValidator.reset().parameter("isFloatingInterestRateCalculationAllowed").failWithCode(
                        "not.supported.when.isLinkedToFloatingInterestRates.is.false",
                        "isFloatingInterestRateCalculationAllowed param is not supported when isLinkedToFloatingInterestRates is not supplied or false");
            }

            final String minInterestRatePerPeriodParameterName = "minInterestRatePerPeriod";
            BigDecimal minInterestRatePerPeriod = loanProduct.getMinNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists(minInterestRatePerPeriodParameterName, element)) {
                minInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minInterestRatePerPeriodParameterName,
                        element);
            }
            baseDataValidator.reset().parameter(minInterestRatePerPeriodParameterName).value(minInterestRatePerPeriod).ignoreIfNull()
                    .zeroOrPositiveAmount();

            final String maxInterestRatePerPeriodParameterName = "maxInterestRatePerPeriod";
            BigDecimal maxInterestRatePerPeriod = loanProduct.getMaxNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists(maxInterestRatePerPeriodParameterName, element)) {
                maxInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxInterestRatePerPeriodParameterName,
                        element);
            }
            baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod).ignoreIfNull()
                    .zeroOrPositiveAmount();

            BigDecimal interestRatePerPeriod = loanProduct.getLoanProductRelatedDetail().getNominalInterestRatePerPeriod();
            if (this.fromApiJsonHelper.parameterExists("interestRatePerPeriod", element)) {
                interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
            }
            baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod).notNull().zeroOrPositiveAmount();

            Integer interestRateFrequencyType = loanProduct.getLoanProductRelatedDetail().getInterestPeriodFrequencyType().getValue();
            if (this.fromApiJsonHelper.parameterExists("interestRateFrequencyType", element)) {
                interestRateFrequencyType = this.fromApiJsonHelper.extractIntegerNamed("interestRateFrequencyType", element,
                        Locale.getDefault());
            }
            baseDataValidator.reset().parameter("interestRateFrequencyType").value(interestRateFrequencyType).notNull().inMinMaxRange(0, 4);
        }

        // Guarantee Funds
        Boolean holdGuaranteeFunds = loanProduct.isHoldGuaranteeFundsEnabled();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.holdGuaranteeFundsParamName, element)) {
            holdGuaranteeFunds = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.holdGuaranteeFundsParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.holdGuaranteeFundsParamName).value(holdGuaranteeFunds).notNull()
                    .isOneOfTheseValues(true, false);
        }

        if (holdGuaranteeFunds != null) {
            if (holdGuaranteeFunds) {
                validateGuaranteeParams(element, baseDataValidator, null);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.principalThresholdForLastInstallmentParamName, element)) {
            BigDecimal principalThresholdForLastInstallment = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.principalThresholdForLastInstallmentParamName)
                    .value(principalThresholdForLastInstallment).notNull().notLessThanMin(BigDecimal.ZERO)
                    .notGreaterThanMax(BigDecimal.valueOf(100));
        }

        BigDecimal fixedPrincipalPercentagePerInstallment = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName, element);
        baseDataValidator.reset().parameter(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName)
                .value(fixedPrincipalPercentagePerInstallment).notLessThanMin(BigDecimal.ONE).notGreaterThanMax(BigDecimal.valueOf(100));

        if (!AmortizationMethod.EQUAL_PRINCIPAL.getValue().equals(amortizationType) && fixedPrincipalPercentagePerInstallment != null) {
            baseDataValidator.reset().parameter(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName).failWithCode(
                    "not.supported.principal.fixing.not.allowed.with.equal.installments",
                    "Principal fixing cannot be done with equal installment amortization");
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.canDefineEmiAmountParamName, element)) {
            final Boolean canDefineInstallmentAmount = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.canDefineEmiAmountParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.canDefineEmiAmountParamName).value(canDefineInstallmentAmount)
                    .isOneOfTheseValues(true, false);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.installmentAmountInMultiplesOfParamName, element)) {
            final Integer installmentAmountInMultiplesOf = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName, element);
            baseDataValidator.reset().parameter(LoanProductConstants.installmentAmountInMultiplesOfParamName)
                    .value(installmentAmountInMultiplesOf).ignoreIfNull().integerGreaterThanZero();
        }

        final Long disbursementAccountNumber = this.fromApiJsonHelper.extractLongNamed("disbursementAccountNumber", element);
        baseDataValidator.reset().parameter("disbursementAccountNumber").value(disbursementAccountNumber).ignoreIfNull().longZeroOrGreater();

        final Long collectionAccountNumber = this.fromApiJsonHelper.extractLongNamed("collectionAccountNumber", element);
        baseDataValidator.reset().parameter("collectionAccountNumber").value(collectionAccountNumber).ignoreIfNull().longZeroOrGreater();

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.PENALINVOICE, element)) {
            final Integer penalInvoiceID = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.PENALINVOICE, element);
            baseDataValidator.reset().parameter(LoanProductConstants.PENALINVOICE).value(penalInvoiceID).ignoreIfNull().integerGreaterThanZero().penalInvoiceExists(this.codeValueRepositoryWrapper,penalInvoiceID);
        }

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.MULTIPLEDISBURSEMENT,element))
        {
            final Integer multiDisbursementId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.MULTIPLEDISBURSEMENT,element);
            baseDataValidator.reset().parameter(LoanProductConstants.MULTIPLEDISBURSEMENT).value(multiDisbursementId).ignoreIfNull().integerGreaterThanZero().multipleDisbursementExists(this.codeValueRepositoryWrapper,multiDisbursementId);
        }

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.TRANCHECLUBBING,element))
        {
            final Integer trancheClubbingId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.TRANCHECLUBBING,element);
            baseDataValidator.reset().parameter(LoanProductConstants.TRANCHECLUBBING).value(trancheClubbingId).ignoreIfNull().integerGreaterThanZero().trancheClubbingExists(this.codeValueRepositoryWrapper,trancheClubbingId);
        }

        if(this.fromApiJsonHelper.parameterExists(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED,element))
        {
            final Integer repaymentScheduleUpdateAllowedId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED,element);
            baseDataValidator.reset().parameter(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED).value(repaymentScheduleUpdateAllowedId).ignoreIfNull().integerGreaterThanZero().repaymentScheduleUpdateAllowedExists(this.codeValueRepositoryWrapper,repaymentScheduleUpdateAllowedId);
        }



        final Integer accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).ignoreIfNull().inMinMaxRange(1, 4);

        final Long fundAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.FUND_SOURCE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.FUND_SOURCE.getValue()).value(fundAccountId).ignoreIfNull()
                .integerGreaterThanZero();

        final Long loanPortfolioAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long transfersInSuspenseAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue()).value(transfersInSuspenseAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromInterestId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.INCOME_FROM_FEES.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId).ignoreIfNull()
                .integerGreaterThanZero();

        final Long incomeFromPenaltyId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromRecoveryAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue()).value(incomeFromRecoveryAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long writeOffAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long overpaymentAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.OVERPAYMENT.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.OVERPAYMENT.getValue()).value(overpaymentAccountId).ignoreIfNull()
                .integerGreaterThanZero();

        final Long receivableInterestAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue()).value(receivableInterestAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long receivableFeeAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.FEES_RECEIVABLE.getValue(),
                element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long receivablePenaltyAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), element);
        baseDataValidator.reset().parameter(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue()).value(receivablePenaltyAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.SERVICER_FEE_INTEREST_CONFIG_ENABLED, element)){
        Boolean servicerFeeInterestConfigEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.SERVICER_FEE_INTEREST_CONFIG_ENABLED, element);
            baseDataValidator.reset().parameter(LoanProductConstants.SERVICER_FEE_INTEREST_CONFIG_ENABLED).value(servicerFeeInterestConfigEnabled).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.SERVICER_FEE_CHARGES_CONFIG_ENABLED, element)){
            Boolean servicerFeeChargesConfigEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.SERVICER_FEE_CHARGES_CONFIG_ENABLED, element);
            baseDataValidator.reset().parameter(LoanProductConstants.SERVICER_FEE_CHARGES_CONFIG_ENABLED).value(servicerFeeChargesConfigEnabled).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ADVANCEAPPROPRIATION, element)){
            Integer advanceAppropriation = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.ADVANCEAPPROPRIATION, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.ADVANCEAPPROPRIATION).value(advanceAppropriation).isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION, element)){
            Boolean enableEntryForAdvanceTransaction = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION).value(enableEntryForAdvanceTransaction).notBlank().isOneOfTheseValues (true,false);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.INTEREST_BENEFIT_ENABLED, element)){
            Boolean interestBenefitEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.INTEREST_BENEFIT_ENABLED, element);
            baseDataValidator.reset().parameter(LoanProductConstants.INTEREST_BENEFIT_ENABLED).value(interestBenefitEnabled).isOneOfTheseValues (true,false);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST, element)){
            Integer foreclosureOnDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST).value(foreclosureOnDueDateInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE, element)){
            Integer foreclosureOnDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE).value(foreclosureOnDueDateCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST, element)){
            Integer foreclosureOtherThanDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST).value(foreclosureOtherThanDueDateInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE, element)){
            Integer foreclosureOtherThanDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE).value(foreclosureOtherThanDueDateCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST, element)){
            Integer foreclosureOneMonthOverdueInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST).value(foreclosureOneMonthOverdueInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE, element)){
            Integer foreclosureOneMonthOverdueCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE).value(foreclosureOneMonthOverdueCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST, element)){
            Integer foreclosureShortPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST).value(foreclosureShortPaidInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE, element)){
            Integer foreclosureShortPaidInterestCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE).value(foreclosureShortPaidInterestCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST, element)){
            Integer foreclosurePrincipalShortPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST).value(foreclosurePrincipalShortPaidInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE, element)){
            Integer foreclosurePrincipalShortPaidCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE).value(foreclosurePrincipalShortPaidCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST, element)){
            Integer foreclosureTwoMonthsOverdueInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST).value(foreclosureTwoMonthsOverdueInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE, element)){
            Integer foreclosureTwoMonthsOverdueCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE).value(foreclosureTwoMonthsOverdueCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE, element)){
            Integer foreclosurePosAdvanceOnDueDate = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE).value(foreclosurePosAdvanceOnDueDate).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST, element)){
            Integer foreclosureAdvanceOnDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST).value(foreclosureAdvanceOnDueDateInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE, element)){
            Integer foreclosureAdvanceOnDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE).value(foreclosureAdvanceOnDueDateCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE, element)){
            Integer foreclosurePosAdvanceOtherThanDueDate = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE).value(foreclosurePosAdvanceOtherThanDueDate).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST, element)){
            Integer foreclosureAdvanceAfterDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST).value(foreclosureAdvanceAfterDueDateInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE, element)){
            Integer foreclosureAdvanceAfterDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE).value(foreclosureAdvanceAfterDueDateCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST, element)){
            Integer foreclosureBackdatedShortPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST).value(foreclosureBackdatedShortPaidInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE, element)){
            Integer foreclosureBackdatedShortPaidInterestCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE).value(foreclosureBackdatedShortPaidInterestCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST, element)){
            Integer foreclosureBackdatedFullyPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST).value(foreclosureBackdatedFullyPaidInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE, element)){
            Integer foreclosureBackdatedFullyPaidInterestCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE).value(foreclosureBackdatedFullyPaidInterestCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST, element)){
            Integer foreclosureBackdatedShortPaidPrincipalInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST).value(foreclosureBackdatedShortPaidPrincipalInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE, element)){
            Integer foreclosureBackdatedShortPaidPrincipalCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE).value(foreclosureBackdatedShortPaidPrincipalCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST, element)){
            Integer foreclosureBackdatedFullyPaidEmiInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST).value(foreclosureBackdatedFullyPaidEmiInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE, element)){
            Integer foreclosureBackdatedFullyPaidEmiCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE).value(foreclosureBackdatedFullyPaidEmiCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST, element)){
            Integer foreclosureBackdatedAdvanceInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST).value(foreclosureBackdatedAdvanceInterest).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE, element)){
            Integer foreclosureBackdatedAdvanceCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE).value(foreclosureBackdatedAdvanceCharge).ignoreIfNull().isOneOfTheseValues (1,2);
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT, element)){
            Boolean enableBackDateDisbursement = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT, element);
            baseDataValidator.reset().parameter(LoanProductConstants.ENABLE_BACKDATED_DISBURSEMENT).value(enableBackDateDisbursement).ignoreIfNull().isOneOfTheseValues (true,false);
        }
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FORECLOSURE_METHOD_TYPE, element)){
            Integer foreclosureMethodType = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_METHOD_TYPE, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.FORECLOSURE_METHOD_TYPE).value(foreclosureMethodType).ignoreIfNull().isOneOfTheseValues (1,2,3);
        }
		
		if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY, element)){
            Integer coolingOffInterestAndChargeApplicability = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY).value(coolingOffInterestAndChargeApplicability).ignoreIfNull();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY, element)){
            Integer coolingOffInterestLogicApplicability = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY, element,Locale.getDefault());
            baseDataValidator.reset().parameter(LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY).value(coolingOffInterestLogicApplicability).ignoreIfNull();
        }
        validatePaymentChannelFundSourceMappings(baseDataValidator, element);
        validateChargeToIncomeAccountMappings(baseDataValidator, element);

        validateMinMaxConstraint(element, baseDataValidator, loanProduct);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, element)) {
            final Boolean useBorrowerCycle = this.fromApiJsonHelper
                    .extractBooleanNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, element);
            baseDataValidator.reset().parameter(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME).value(useBorrowerCycle)
                    .ignoreIfNull().validateForBooleanValue();
            if (useBorrowerCycle) {
                validateBorrowerCycleVariations(element, baseDataValidator);
            }
        }

        validateMultiDisburseLoanData(baseDataValidator, element);

        // validateLoanConfigurableAttributes(baseDataValidator,element);

        validateVariableInstallmentSettings(baseDataValidator, element);

        validateAgeLimitsSettings(baseDataValidator, element);

//        validatePrepayLockingPeriodSettings(baseDataValidator, element);



        validatePartialPeriodSupport(interestCalculationPeriodType, baseDataValidator, element, loanProduct);

        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.CAN_USE_FOR_TOPUP, element)) {
            final Boolean canUseForTopup = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.CAN_USE_FOR_TOPUP, element);
            baseDataValidator.reset().parameter(LoanProductConstants.CAN_USE_FOR_TOPUP).value(canUseForTopup).validateForBooleanValue();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }


    private void validateMinMaxConstraint(JsonElement element, DataValidatorBuilder baseDataValidator, LoanProduct loanProduct) {

        validatePrincipalMinMaxConstraint(element, loanProduct, baseDataValidator);

        validateNumberOfRepaymentsMinMaxConstraint(element, loanProduct, baseDataValidator);

        validateNominalInterestRatePerPeriodMinMaxConstraint(element, loanProduct, baseDataValidator);
    }

    /*
     * Validation for advanced accounting options
     */
    private void validatePaymentChannelFundSourceMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (this.fromApiJsonHelper.parameterExists(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element)) {
            final JsonArray paymentChannelMappingArray = this.fromApiJsonHelper
                    .extractJsonArrayNamed(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
            if (paymentChannelMappingArray != null && paymentChannelMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                    final Long paymentTypeId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.PAYMENT_TYPE.getValue(),
                            jsonObject);
                    final Long paymentSpecificFundAccountId = this.fromApiJsonHelper
                            .extractLongNamed(LoanProductAccountingParams.FUND_SOURCE.getValue(), jsonObject);

                    baseDataValidator.reset()
                            .parameter(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + "[" + i + "]."
                                    + LoanProductAccountingParams.PAYMENT_TYPE.getValue())
                            .value(paymentTypeId).notNull().integerGreaterThanZero();
                    baseDataValidator.reset()
                            .parameter(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + "[" + i + "]."
                                    + LoanProductAccountingParams.FUND_SOURCE.getValue())
                            .value(paymentSpecificFundAccountId).notNull().integerGreaterThanZero();
                    i++;
                } while (i < paymentChannelMappingArray.size());
            }
        }
    }

    private void validateChargeToIncomeAccountMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        // validate for both fee and penalty charges
        validateChargeToIncomeAccountMappings(baseDataValidator, element, true);
        validateChargeToIncomeAccountMappings(baseDataValidator, element, true);
    }

    private void validateChargeToIncomeAccountMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element,
                                                       final boolean isPenalty) {
        String parameterName;
        if (isPenalty) {
            parameterName = LoanProductAccountingParams.PENALTY_INCOME_ACCOUNT_MAPPING.getValue();
        } else {
            parameterName = LoanProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue();
        }

        if (this.fromApiJsonHelper.parameterExists(parameterName, element)) {
            final JsonArray chargeToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(parameterName, element);
            if (chargeToIncomeAccountMappingArray != null && chargeToIncomeAccountMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = chargeToIncomeAccountMappingArray.get(i).getAsJsonObject();
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.CHARGE_ID.getValue(),
                            jsonObject);
                    final Long incomeAccountId = this.fromApiJsonHelper
                            .extractLongNamed(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue(), jsonObject);
                    baseDataValidator.reset().parameter(parameterName + "[" + i + "]." + LoanProductAccountingParams.CHARGE_ID.getValue())
                            .value(chargeId).notNull().integerGreaterThanZero();
                    baseDataValidator.reset()
                            .parameter(parameterName + "[" + i + "]." + LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue())
                            .value(incomeAccountId).notNull().integerGreaterThanZero();
                    i++;
                } while (i < chargeToIncomeAccountMappingArray.size());
            }
        }
    }

    public void validateMinMaxConstraints(final JsonElement element, final DataValidatorBuilder baseDataValidator,
                                          final LoanProduct loanProduct,final Partner partner) {

        validatePrincipalMinMaxConstraint(element, loanProduct, baseDataValidator);

        validateNumberOfRepaymentsMinMaxConstraint(element, loanProduct, baseDataValidator);

        validateNominalInterestRatePerPeriodMinMaxConstraint(element, loanProduct, baseDataValidator);

        validatePrincipalAmountLimit(element,partner,baseDataValidator,loanProduct);
    }

    private void validatePrincipalAmountLimit(JsonElement element, Partner partner, DataValidatorBuilder baseDataValidator,LoanProduct loanProduct) {


        boolean principalUpdated = false;
        final String principalParameterName = LoanProductConstants.PRINCIPAL;
        BigDecimal principalAmount = null;
        BigDecimal overAllLimit=null;
        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            principalUpdated = true;
        } else {
            principalAmount = loanProduct.getPrincipalAmount().getAmount();
        }

        overAllLimit=partner.getBalanceLimit();

        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            baseDataValidator.reset().parameter(principalParameterName).value(principal).notNull().positiveAmount()
                    .isLessThanLoanAmount(overAllLimit,principalAmount);
        }


    }

    public void validateMinMaxConstraints(final JsonElement element, final DataValidatorBuilder baseDataValidator,
                                          final LoanProduct loanProduct, Integer cycleNumber,final Partner partner) {

        final Map<String, BigDecimal> minmaxValues = loanProduct.fetchBorrowerCycleVariationsForCycleNumber(cycleNumber);
        final String principalParameterName = LoanProductConstants.PRINCIPAL;
        BigDecimal principalAmount = null;
        BigDecimal minPrincipalAmount = null;
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            minPrincipalAmount = minmaxValues.get(LoanProductConstants.MIN_PRINCIPAL);
            maxPrincipalAmount = minmaxValues.get(LoanProductConstants.MAX_PRINCIPAL);
        }

        if ((minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) > 0)
                && (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) > 0)) {
            baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).inMinAndMaxAmountRange(minPrincipalAmount,
                    maxPrincipalAmount);
        } else {
            if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
                baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).notLessThanMin(minPrincipalAmount);
            } else if (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
                baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).notGreaterThanMax(maxPrincipalAmount);
            }
        }

        final String numberOfRepaymentsParameterName = LoanProductConstants.NUMBER_OF_REPAYMENTS;
        Integer maxNumberOfRepayments = null;
        Integer minNumberOfRepayments = null;
        Integer numberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(numberOfRepaymentsParameterName, element)) {
            numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
            if (minmaxValues.get(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS) != null) {
                minNumberOfRepayments = minmaxValues.get(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS).intValueExact();
            }
            if (minmaxValues.get(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS) != null) {
                maxNumberOfRepayments = minmaxValues.get(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS).intValueExact();
            }
        }

        if (maxNumberOfRepayments != null && maxNumberOfRepayments.compareTo(0) > 0) {
            if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
                baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                        .inMinMaxRange(minNumberOfRepayments, maxNumberOfRepayments);
            } else {
                baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                        .notGreaterThanMax(maxNumberOfRepayments);
            }
        } else if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
            baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                    .notLessThanMin(minNumberOfRepayments);
        }

        final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
        BigDecimal interestRatePerPeriod = null;
        BigDecimal minInterestRatePerPeriod = null;
        BigDecimal maxInterestRatePerPeriod = null;
        if (this.fromApiJsonHelper.parameterExists(interestRatePerPeriodParameterName, element)) {
            interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName, element);
            minInterestRatePerPeriod = minmaxValues.get(LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD);
            maxInterestRatePerPeriod = minmaxValues.get(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD);
        }
        if (maxInterestRatePerPeriod != null) {
            if (minInterestRatePerPeriod != null) {
                baseDataValidator.reset().parameter("interest Rate").value(interestRatePerPeriod)
                        .inMinAndMaxAmountRange(minInterestRatePerPeriod, maxInterestRatePerPeriod);
            } else {
                baseDataValidator.reset().parameter("interest Rate").value(interestRatePerPeriod)
                        .notGreaterThanMax(maxInterestRatePerPeriod);
            }
        } else if (minInterestRatePerPeriod != null) {
            baseDataValidator.reset().parameter("interest Rate").value(interestRatePerPeriod)
                    .notLessThanMin(minInterestRatePerPeriod);
        }

    }

    private void validatePrincipalMinMaxConstraint(final JsonElement element, final LoanProduct loanProduct,
                                                   final DataValidatorBuilder baseDataValidator) {

        boolean principalUpdated = false;
        boolean minPrincipalUpdated = false;
        boolean maxPrincipalUpdated = false;
        final String principalParameterName = LoanProductConstants.PRINCIPAL;
        BigDecimal principalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            principalUpdated = true;
        } else {
            principalAmount = loanProduct.getPrincipalAmount().getAmount();
        }

        final String minPrincipalParameterName = "minPrincipal";
        BigDecimal minPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(minPrincipalParameterName, element)) {
            minPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minPrincipalParameterName, element);
            minPrincipalUpdated = true;
        } else {
            minPrincipalAmount = loanProduct.getMinPrincipalAmount().getAmount();
        }

        final String maxPrincipalParameterName = "maxPrincipal";
        BigDecimal maxPrincipalAmount = null;
        if (this.fromApiJsonHelper.parameterExists(maxPrincipalParameterName, element)) {
            maxPrincipalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxPrincipalParameterName, element);
            maxPrincipalUpdated = true;
        } else {
            maxPrincipalAmount = loanProduct.getMaxPrincipalAmount().getAmount();
        }

        if (minPrincipalUpdated) {
            baseDataValidator.reset().parameter(minPrincipalParameterName).value(minPrincipalAmount).notGreaterThanMax(maxPrincipalAmount);
        }

        if (maxPrincipalUpdated) {
            baseDataValidator.reset().parameter(maxPrincipalParameterName).value(maxPrincipalAmount).notLessThanMin(minPrincipalAmount);
        }

        if ((principalUpdated || minPrincipalUpdated || maxPrincipalUpdated)) {

            if ((minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) > 0)
                    && (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) > 0)) {
                baseDataValidator.reset().parameter(principalParameterName).value(principalAmount)
                        .inMinAndMaxAmountRange(minPrincipalAmount, maxPrincipalAmount);
            } else {
                if (minPrincipalAmount != null && minPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    baseDataValidator.reset().parameter(principalParameterName).value(principalAmount).notLessThanMin(minPrincipalAmount);
                } else if (maxPrincipalAmount != null && maxPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    baseDataValidator.reset().parameter(principalParameterName).value(principalAmount)
                            .notGreaterThanMax(maxPrincipalAmount);
                }
            }
        }
    }

    private void validateNumberOfRepaymentsMinMaxConstraint(final JsonElement element, final LoanProduct loanProduct,
                                                            final DataValidatorBuilder baseDataValidator) {
        boolean numberOfRepaymentsUpdated = false;
        boolean minNumberOfRepaymentsUpdated = false;
        boolean maxNumberOfRepaymentsUpdated = false;

        final String numberOfRepaymentsParameterName = LoanProductConstants.NUMBER_OF_REPAYMENTS;
        Integer numberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(numberOfRepaymentsParameterName, element)) {
            numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
            numberOfRepaymentsUpdated = true;
        } else {
            numberOfRepayments = loanProduct.getNumberOfRepayments();
        }

        final String minNumberOfRepaymentsParameterName = "minNumberOfRepayments";
        Integer minNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(minNumberOfRepaymentsParameterName, element)) {
            minNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(minNumberOfRepaymentsParameterName, element);
            minNumberOfRepaymentsUpdated = true;
        } else {
            minNumberOfRepayments = loanProduct.getMinNumberOfRepayments();
        }

        final String maxNumberOfRepaymentsParameterName = "maxNumberOfRepayments";
        Integer maxNumberOfRepayments = null;
        if (this.fromApiJsonHelper.parameterExists(maxNumberOfRepaymentsParameterName, element)) {
            maxNumberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(maxNumberOfRepaymentsParameterName, element);
            maxNumberOfRepaymentsUpdated = true;
        } else {
            maxNumberOfRepayments = loanProduct.getMaxNumberOfRepayments();
        }

        if (minNumberOfRepaymentsUpdated) {
            baseDataValidator.reset().parameter(minNumberOfRepaymentsParameterName).value(minNumberOfRepayments).ignoreIfNull()
                    .notGreaterThanMax(maxNumberOfRepayments);
        }

        if (maxNumberOfRepaymentsUpdated) {
            baseDataValidator.reset().parameter(maxNumberOfRepaymentsParameterName).value(maxNumberOfRepayments)
                    .notLessThanMin(minNumberOfRepayments);
        }

        if (numberOfRepaymentsUpdated || minNumberOfRepaymentsUpdated || maxNumberOfRepaymentsUpdated) {
            if (maxNumberOfRepayments != null && maxNumberOfRepayments.compareTo(0) > 0) {
                if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
                    baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                            .inMinMaxRange(minNumberOfRepayments, maxNumberOfRepayments);
                } else {
                    baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                            .notGreaterThanMax(maxNumberOfRepayments);
                }
            } else if (minNumberOfRepayments != null && minNumberOfRepayments.compareTo(0) > 0) {
                baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments)
                        .notLessThanMin(minNumberOfRepayments);
            }
        }
    }

    private void validateNominalInterestRatePerPeriodMinMaxConstraint(final JsonElement element, final LoanProduct loanProduct,
                                                                      final DataValidatorBuilder baseDataValidator) {

        if ((this.fromApiJsonHelper.parameterExists("isLinkedToFloatingInterestRates", element)
                && this.fromApiJsonHelper.extractBooleanNamed("isLinkedToFloatingInterestRates", element) == true)
                || loanProduct.isLinkedToFloatingInterestRate()) {
            return;
        }
        boolean iRPUpdated = false;
        boolean minIRPUpdated = false;
        boolean maxIRPUpdated = false;
        final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
        BigDecimal interestRatePerPeriod = null;

        if (this.fromApiJsonHelper.parameterExists(interestRatePerPeriodParameterName, element)) {
            interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName, element);
            iRPUpdated = true;
        } else {
            interestRatePerPeriod = loanProduct.getNominalInterestRatePerPeriod();
        }

        final String minInterestRatePerPeriodParameterName = "minInterestRatePerPeriod";
        BigDecimal minInterestRatePerPeriod = null;
        if (this.fromApiJsonHelper.parameterExists(minInterestRatePerPeriodParameterName, element)) {
            minInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minInterestRatePerPeriodParameterName,
                    element);
            minIRPUpdated = true;
        } else {
            minInterestRatePerPeriod = loanProduct.getMinNominalInterestRatePerPeriod();
        }

        final String maxInterestRatePerPeriodParameterName = "maxInterestRatePerPeriod";
        BigDecimal maxInterestRatePerPeriod = null;
        if (this.fromApiJsonHelper.parameterExists(maxInterestRatePerPeriodParameterName, element)) {
            maxInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(maxInterestRatePerPeriodParameterName,
                    element);
            maxIRPUpdated = true;
        } else {
            maxInterestRatePerPeriod = loanProduct.getMaxNominalInterestRatePerPeriod();
        }

        if (minIRPUpdated) {
            baseDataValidator.reset().parameter(minInterestRatePerPeriodParameterName).value(minInterestRatePerPeriod).ignoreIfNull()
                    .notGreaterThanMax(maxInterestRatePerPeriod);
        }

        if (maxIRPUpdated) {
            baseDataValidator.reset().parameter(maxInterestRatePerPeriodParameterName).value(maxInterestRatePerPeriod).ignoreIfNull()
                    .notLessThanMin(minInterestRatePerPeriod);
        }

        if (iRPUpdated || minIRPUpdated || maxIRPUpdated) {
            if (maxInterestRatePerPeriod != null) {
                if (minInterestRatePerPeriod != null) {
                    baseDataValidator.reset().parameter("Interest Rate").value(interestRatePerPeriod)
                            .inMinAndMaxAmountRange(minInterestRatePerPeriod, maxInterestRatePerPeriod);
                } else {
                    baseDataValidator.reset().parameter("Interest Rate").value(interestRatePerPeriod)
                            .notGreaterThanMax(maxInterestRatePerPeriod);
                }
            } else if (minInterestRatePerPeriod != null) {
                baseDataValidator.reset().parameter("Interest Rate").value(interestRatePerPeriod)
                        .notLessThanMin(minInterestRatePerPeriod);
            }
        }
    }

    private boolean isCashBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.CASH_BASED.getValue().equals(accountingRuleType);
    }

    private boolean isAccrualBasedAccounting(final Integer accountingRuleType) {
        return isUpfrontAccrualAccounting(accountingRuleType) || isPeriodicAccounting(accountingRuleType);
    }

    private boolean isUpfrontAccrualAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_UPFRONT.getValue().equals(accountingRuleType);
    }

    private boolean isPeriodicAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_PERIODIC.getValue().equals(accountingRuleType);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private void validateBorrowerCycleVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        validateBorrowerCyclePrincipalVariations(element, baseDataValidator);
        validateBorrowerCycleRepaymentVariations(element, baseDataValidator);
        validateBorrowerCycleInterestVariations(element, baseDataValidator);
    }

    private void validateBorrowerCyclePrincipalVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {

        validateBorrowerCycleVariations(element, baseDataValidator,
                LoanProductConstants.PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.PRINCIPAL_PER_CYCLE_PARAMETER_NAME, LoanProductConstants.MIN_PRINCIPAL_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MIN_PRINCIPAL_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.PRINCIPAL_VALUE_USAGE_CONDITION_PARAM_NAME, LoanProductConstants.PRINCIPAL_CYCLE_NUMBERS_PARAM_NAME);
    }

    private void validateBorrowerCycleRepaymentVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        validateBorrowerCycleVariations(element, baseDataValidator,
                LoanProductConstants.NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.REPAYMENT_VALUE_USAGE_CONDITION_PARAM_NAME, LoanProductConstants.REPAYMENT_CYCLE_NUMBER_PARAM_NAME);
    }

    private void validateBorrowerCycleInterestVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        validateBorrowerCycleVariations(element, baseDataValidator,
                LoanProductConstants.INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME,
                LoanProductConstants.INTEREST_RATE_VALUE_USAGE_CONDITION_PARAM_NAME,
                LoanProductConstants.INTEREST_RATE_VALUE_USAGE_CONDITION_PARAM_NAME);
    }

    private void validateBorrowerCycleVariations(final JsonElement element, final DataValidatorBuilder baseDataValidator,
                                                 final String variationParameterName, final String defaultParameterName, final String minParameterName,
                                                 final String maxParameterName, final String valueUsageConditionParamName, final String cycleNumbersParamName) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        Integer lastCycleNumber = 0;
        LoanProductValueConditionType lastConditionType = LoanProductValueConditionType.EQUAL;
        if (this.fromApiJsonHelper.parameterExists(variationParameterName, element)) {
            final JsonArray variationArray = this.fromApiJsonHelper.extractJsonArrayNamed(variationParameterName, element);
            if (variationArray != null && variationArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();

                    BigDecimal defaultValue = this.fromApiJsonHelper
                            .extractBigDecimalNamed(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME, jsonObject, locale);
                    BigDecimal minValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.MIN_VALUE_PARAMETER_NAME,
                            jsonObject, locale);
                    BigDecimal maxValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanProductConstants.MAX_VALUE_PARAMETER_NAME,
                            jsonObject, locale);
                    Integer cycleNumber = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME,
                            jsonObject, locale);
                    Integer valueUsageCondition = this.fromApiJsonHelper
                            .extractIntegerNamed(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME, jsonObject, locale);

                    baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).notBlank();
                    if (minValue != null) {
                        baseDataValidator.reset().parameter(minParameterName).value(minValue).notGreaterThanMax(maxValue);
                    }

                    if (maxValue != null) {
                        baseDataValidator.reset().parameter(maxParameterName).value(maxValue).notLessThanMin(minValue);
                    }
                    if ((minValue != null && minValue.compareTo(BigDecimal.ZERO) > 0)
                            && (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) > 0)) {
                        baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).inMinAndMaxAmountRange(minValue,
                                maxValue);
                    } else {
                        if (minValue != null && minValue.compareTo(BigDecimal.ZERO) > 0) {
                            baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).notLessThanMin(minValue);
                        } else if (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) > 0) {
                            baseDataValidator.reset().parameter(defaultParameterName).value(defaultValue).notGreaterThanMax(maxValue);
                        }
                    }

                    LoanProductValueConditionType conditionType = LoanProductValueConditionType.INVALID;
                    if (valueUsageCondition != null) {
                        conditionType = LoanProductValueConditionType.fromInt(valueUsageCondition);
                    }
                    baseDataValidator.reset().parameter(valueUsageConditionParamName).value(valueUsageCondition).notNull().inMinMaxRange(
                            LoanProductValueConditionType.EQUAL.getValue(), LoanProductValueConditionType.GREATERTHAN.getValue());
                    if (lastConditionType.equals(LoanProductValueConditionType.EQUAL)
                            && conditionType.equals(LoanProductValueConditionType.GREATERTHAN)) {
                        if (lastCycleNumber == 0) {
                            baseDataValidator.reset().parameter(cycleNumbersParamName)
                                    .failWithCode(LoanProductConstants.VALUE_CONDITION_START_WITH_ERROR);
                            lastCycleNumber = 1;
                        }
                        baseDataValidator.reset().parameter(cycleNumbersParamName).value(cycleNumber).notNull()
                                .integerSameAsNumber(lastCycleNumber);
                    } else if (lastConditionType.equals(LoanProductValueConditionType.EQUAL)) {
                        baseDataValidator.reset().parameter(cycleNumbersParamName).value(cycleNumber).notNull()
                                .integerSameAsNumber(lastCycleNumber + 1);
                    } else if (lastConditionType.equals(LoanProductValueConditionType.GREATERTHAN)) {
                        baseDataValidator.reset().parameter(cycleNumbersParamName).value(cycleNumber).notNull()
                                .integerGreaterThanNumber(lastCycleNumber);
                    }
                    if (conditionType != null) {
                        lastConditionType = conditionType;
                    }
                    if (cycleNumber != null) {
                        lastCycleNumber = cycleNumber;
                    }
                    i++;
                } while (i < variationArray.size());
                if (!lastConditionType.equals(LoanProductValueConditionType.GREATERTHAN)) {
                    baseDataValidator.reset().parameter(cycleNumbersParamName)
                            .failWithCode(LoanProductConstants.VALUE_CONDITION_END_WITH_ERROR);
                }
            }

        }

    }

    private void validateGuaranteeParams(final JsonElement element, final DataValidatorBuilder baseDataValidator,
                                         final LoanProduct loanProduct) {
        BigDecimal mandatoryGuarantee = BigDecimal.ZERO;
        BigDecimal minimumGuaranteeFromOwnFunds = BigDecimal.ZERO;
        BigDecimal minimumGuaranteeFromGuarantor = BigDecimal.ZERO;
        if (loanProduct != null) {
            mandatoryGuarantee = loanProduct.getLoanProductGuaranteeDetails().getMandatoryGuarantee();
            minimumGuaranteeFromOwnFunds = loanProduct.getLoanProductGuaranteeDetails().getMinimumGuaranteeFromOwnFunds();
            minimumGuaranteeFromGuarantor = loanProduct.getLoanProductGuaranteeDetails().getMinimumGuaranteeFromGuarantor();
        }

        if (loanProduct == null || this.fromApiJsonHelper.parameterExists(LoanProductConstants.mandatoryGuaranteeParamName, element)) {
            mandatoryGuarantee = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanProductConstants.mandatoryGuaranteeParamName,
                    element);
            baseDataValidator.reset().parameter(LoanProductConstants.mandatoryGuaranteeParamName).value(mandatoryGuarantee).notNull();
            if (mandatoryGuarantee == null) {
                mandatoryGuarantee = BigDecimal.ZERO;
            }
        }

        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGuaranteeFromGuarantorParamName, element)) {
            minimumGuaranteeFromGuarantor = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanProductConstants.minimumGuaranteeFromGuarantorParamName, element);
            if (minimumGuaranteeFromGuarantor == null) {
                minimumGuaranteeFromGuarantor = BigDecimal.ZERO;
            }
        }

        if (loanProduct == null
                || this.fromApiJsonHelper.parameterExists(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, element)) {
            minimumGuaranteeFromOwnFunds = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, element);
            if (minimumGuaranteeFromOwnFunds == null) {
                minimumGuaranteeFromOwnFunds = BigDecimal.ZERO;
            }
        }

        if (mandatoryGuarantee.compareTo(minimumGuaranteeFromOwnFunds.add(minimumGuaranteeFromGuarantor)) < 0) {
            baseDataValidator.parameter(LoanProductConstants.mandatoryGuaranteeParamName)
                    .failWithCode("must.be.greter.than.sum.of.min.funds");
        }

    }

    private void validatePartialPeriodSupport(final Integer interestCalculationPeriodType, final DataValidatorBuilder baseDataValidator,
                                              final JsonElement element, final LoanProduct loanProduct) {
        if (interestCalculationPeriodType != null) {
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                    .fromInt(interestCalculationPeriodType);
            boolean considerPartialPeriodUpdates = interestCalculationPeriodMethod.isDaily();

            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
                    element)) {
                final Boolean considerPartialInterestEnabled = this.fromApiJsonHelper
                        .extractBooleanNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME, element);
                baseDataValidator.reset().parameter(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME)
                        .value(considerPartialInterestEnabled).notNull().isOneOfTheseValues(true, false);
                final boolean considerPartialPeriods = considerPartialInterestEnabled == null ? false : considerPartialInterestEnabled;
                if (interestCalculationPeriodMethod.isDaily()) {
                    if (considerPartialPeriods) {
                        baseDataValidator.reset().parameter(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME)
                                .failWithCode("not.supported.for.daily.calcualtions");
                    }
                } else {
                    considerPartialPeriodUpdates = considerPartialPeriods;
                }
            }

            if (!considerPartialPeriodUpdates) {
                Boolean isInterestRecalculationEnabled = null;
                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME,
                        element)) {
                    isInterestRecalculationEnabled = this.fromApiJsonHelper
                            .extractBooleanNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, element);
                } else if (loanProduct != null) {
                    isInterestRecalculationEnabled = loanProduct.isInterestRecalculationEnabled();
                }
                if (isInterestRecalculationEnabled != null && isInterestRecalculationEnabled) {
                    baseDataValidator.reset().parameter(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                Boolean multiDisburseLoan = null;
                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME, element)) {
                    multiDisburseLoan = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME,
                            element);
                } else if (loanProduct != null) {
                    isInterestRecalculationEnabled = loanProduct.isMultiDisburseLoan();
                }
                if (multiDisburseLoan != null && multiDisburseLoan) {
                    baseDataValidator.reset().parameter(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                Boolean variableInstallments = null;
                if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.allowVariableInstallmentsParamName, element)) {
                    variableInstallments = this.fromApiJsonHelper
                            .extractBooleanNamed(LoanProductConstants.allowVariableInstallmentsParamName, element);
                } else if (loanProduct != null) {
                    isInterestRecalculationEnabled = loanProduct.allowVariabeInstallments();
                }
                if (variableInstallments != null && variableInstallments) {
                    baseDataValidator.reset().parameter(LoanProductConstants.allowVariableInstallmentsParamName)
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }

                Boolean floatingInterestRates = null;
                if (this.fromApiJsonHelper.parameterExists("isLinkedToFloatingInterestRates", element)) {
                    floatingInterestRates = this.fromApiJsonHelper.extractBooleanNamed("isLinkedToFloatingInterestRates", element);
                } else if (loanProduct != null) {
                    isInterestRecalculationEnabled = loanProduct.isLinkedToFloatingInterestRate();
                }
                if (floatingInterestRates != null && floatingInterestRates) {
                    baseDataValidator.reset().parameter("isLinkedToFloatingInterestRates")
                            .failWithCode("not.supported.for.selected.interest.calcualtion.type");
                }
            }

        }
    }
}
