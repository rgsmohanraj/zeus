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
package org.vcpl.lms.portfolio.charge.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.InvalidJsonException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.portfolio.charge.api.ChargesApiConstants;
import org.vcpl.lms.portfolio.charge.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import static java.lang.Integer.min;
import static java.lang.Integer.sum;

@Component
public final class ChargeDefinitionCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("name", "amount", "locale", "currencyCode",
            "currencyOptions", "chargeAppliesTo", "chargeTimeType", "chargeCalculationType", "chargeCalculationTypeOptions", "penalty",
            "active", "chargePaymentMode", "feeOnMonthDay", "feeInterval", "monthDayFormat", "minCap", "maxCap", "feeFrequency",
            "enableFreeWithdrawalCharge", "freeWithdrawalFrequency", "restartCountFrequency", "countFrequencyType", "paymentTypeId",
            "enablePaymentType", ChargesApiConstants.glAccountIdParamName, ChargesApiConstants.taxGroupIdParamName,"type","minAmount","maxAmount","minChargeAmount","maxChargeAmount","enableGstCharges","gst","enableGstChargesSelected","gstOption","typeSelected","feesChargeTypeSelected","feesChargeType", ChargesApiConstants.PENALTY_INTEGER_DAYS_IN_YEARS,
            "chargeDecimal","chargeRoundingMode","chargeDecimalRegex","gstDecimal","gstRoundingMode","gstDecimalRegex","gstSlabLimitValue","isGstSlabEnabled","gstSlabLimitApplyFor","gstSlabLimitOperator","isGstSlabEnabledSelected","gstSlabLimitApplyForOption","gstSlabLimitOperatorOption","isDefaultLoanCharge"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ChargeDefinitionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeAppliesTo", element);
        baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).notNull();
        if (chargeAppliesTo != null) {
            baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).isOneOfTheseValues(ChargeAppliesTo.validValues());
        }

        final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeCalculationType", element);
        baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType).notNull();

        final Integer feeChargeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("feesChargeType", element);
        baseDataValidator.reset().parameter("feesChargeType").value(feeChargeType).notNull();

        final Integer typeSelected = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("type", element);
        baseDataValidator.reset().parameter("typeSelected").value(typeSelected).notNull().isOneOfTheseValues(53,54);



//        final Integer gst = this.fromApiJsonHelper.extractIntegerNamed("gst", element,Locale.getDefault());
//        baseDataValidator.reset().parameter("gst").value(gst).notNull().integerZeroOrGreater();

        if (this.fromApiJsonHelper.parameterExists("enableFreeWithdrawalCharge", element)) {

            final Boolean enableFreeWithdrawalCharge = this.fromApiJsonHelper.extractBooleanNamed("enableFreeWithdrawalCharge", element);
            baseDataValidator.reset().parameter("enableFreeWithdrawalCharge").value(enableFreeWithdrawalCharge).notNull();

            if (enableFreeWithdrawalCharge) {

                final Integer freeWithdrawalFrequency = this.fromApiJsonHelper.extractIntegerNamed("freeWithdrawalFrequency", element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter("freeWithdrawalFrequency").value(freeWithdrawalFrequency).integerGreaterThanZero();

                final Integer restartCountFrequency = this.fromApiJsonHelper.extractIntegerNamed("restartCountFrequency", element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter("restartCountFrequency").value(restartCountFrequency).integerGreaterThanZero();

                final Integer countFrequencyType = this.fromApiJsonHelper.extractIntegerNamed("countFrequencyType", element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter("countFrequencyType").value(countFrequencyType);

            }
        }

        if (this.fromApiJsonHelper.parameterExists("enablePaymentType", element)) {

            final boolean enablePaymentType = this.fromApiJsonHelper.extractBooleanNamed("enablePaymentType", element);
            baseDataValidator.reset().parameter("enablePaymentType").value(enablePaymentType).notNull();

            if (enablePaymentType) {
                final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerNamed("paymentTypeId", element, Locale.getDefault());
                baseDataValidator.reset().parameter("paymentTypeId").value(paymentTypeId).integerGreaterThanZero();
            }
        }



        final ChargeAppliesTo appliesTo = ChargeAppliesTo.fromInt(chargeAppliesTo);
        if (appliesTo.isLoanCharge()) {
            // loan applicable validation
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validLoanValues());
            }
            final ChargeTimeType ctt = ChargeTimeType.fromInt(chargeTimeType);
            if(ctt.isOverdueInstallment()){

                final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed("feeInterval", element, Locale.getDefault());
                baseDataValidator.reset().parameter("feeInterval").value(feeInterval).integerGreaterThanZero().notNull();

                final Integer feeFrequency = this.fromApiJsonHelper.extractIntegerNamed("feeFrequency", element, Locale.getDefault());
                baseDataValidator.reset().parameter("feeFrequency").value(feeFrequency).inMinMaxRange(0, 3).notNull();

                final Integer penalityDaysInYear = this.fromApiJsonHelper.extractIntegerNamed("penaltyInterestDaysInYear", element, Locale.getDefault());
                baseDataValidator.reset().parameter("penaltyInterestDaysInYear").value(penalityDaysInYear).inMinMaxRange(0, 2).notNull();
            }

            final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargePaymentMode", element);
            baseDataValidator.reset().parameter("chargePaymentMode").value(chargePaymentMode).notNull()
                    .isOneOfTheseValues(ChargePaymentMode.validValues());
            if (chargePaymentMode != null) {
                baseDataValidator.reset().parameter("chargePaymentMode").value(chargePaymentMode)
                        .isOneOfTheseValues(ChargePaymentMode.validValues());
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForLoan());
            }

            if (chargeTimeType != null && chargeCalculationType != null) {
                performChargeTimeNCalculationTypeValidation(baseDataValidator, chargeTimeType, chargeCalculationType);
            }

        } else if (appliesTo.isSavingsCharge()) {
            // savings applicable validation
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validSavingsValues());
            }

            final ChargeTimeType ctt = ChargeTimeType.fromInt(chargeTimeType);

            if(ctt.isOverdueInstallment()){

                final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed("feeInterval", element, Locale.getDefault());
                baseDataValidator.reset().parameter("feeInterval").value(feeInterval).integerGreaterThanZero().notNull();

                final Integer feeFrequency = this.fromApiJsonHelper.extractIntegerNamed("feeFrequency", element, Locale.getDefault());
                baseDataValidator.reset().parameter("feeFrequency").value(feeFrequency).inMinMaxRange(0, 3).notNull();

                final Integer penalityDaysInYear = this.fromApiJsonHelper.extractIntegerNamed("penaltyInterestDaysInYear", element, Locale.getDefault());
                baseDataValidator.reset().parameter("penaltyInterestDaysInYear").value(penalityDaysInYear).inMinMaxRange(0, 2).notNull();
            }

            if (ctt.isWeeklyFee()) {
                final String monthDay = this.fromApiJsonHelper.extractStringNamed("feeOnMonthDay", element);
                baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).mustBeBlankWhenParameterProvidedIs("chargeTimeType",
                        chargeTimeType);
            }

            if (ctt.isMonthlyFee()) {
                final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed("feeOnMonthDay", element);
                baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).notNull();
                final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed("feeInterval", element, Locale.getDefault());
                baseDataValidator.reset().parameter("feeInterval").value(feeInterval).notNull().inMinMaxRange(1, 12);
            }

            if (ctt.isAnnualFee()) {
                final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed("feeOnMonthDay", element);
                baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).notNull();
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForSavings());
            }

        } else if (appliesTo.isClientCharge()) {
            // client applicable validation
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validClientValues());
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForClients());
            }

            // GL Account can be linked to clients
            if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.glAccountIdParamName, element)) {
                final Long glAccountId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.glAccountIdParamName, element);
                baseDataValidator.reset().parameter(ChargesApiConstants.glAccountIdParamName).value(glAccountId).notNull()
                        .longGreaterThanZero();
            }

        } else if (appliesTo.isSharesCharge()) {
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validShareValues());
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForShares());
            }

            if (chargeTimeType != null && chargeTimeType.equals(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue())) {
                if (chargeCalculationType != null) {
                    baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                            .isOneOfTheseValues(ChargeCalculationType.validValuesForShareAccountActivation());
                }
            }
        }

        final Boolean enableGstCharges=this.fromApiJsonHelper.extractBooleanNamed("enableGstCharges",element);
        baseDataValidator.reset().parameter("enableGstCharges").value(enableGstCharges).ignoreIfNull().isOneOfTheseValues(true,false);
        if(this.fromApiJsonHelper.parameterExists("enableGstCharges",element)){
            final Boolean enableGstChargesSelected = this.fromApiJsonHelper.extractBooleanNamed("enableGstCharges", element);
            if(enableGstChargesSelected){
                final Integer gstOption = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("gst", element);
                baseDataValidator.reset().parameter("gst").value(gstOption).notNull();

                final Integer gstDecimal = this.fromApiJsonHelper.extractIntegerNamed("gstDecimal", element,Locale.getDefault());
                baseDataValidator.reset().parameter("gstDecimal").value(gstDecimal).notNull().integerZeroOrGreater().isOneOfTheseValues(0,1,2);

                final String gstRoundingMode = this.fromApiJsonHelper.extractStringNamed("gstRoundingMode", element);
                baseDataValidator.reset().parameter("gstRoundingMode").value(gstRoundingMode).notNull();
            }
        }


        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank().notExceedingLengthOf(3).isOneOfTheseValues("INR","USD");

        final Integer chargeDecimal = this.fromApiJsonHelper.extractIntegerNamed("chargeDecimal", element,Locale.getDefault());
        baseDataValidator.reset().parameter("chargeDecimal").value(chargeDecimal).notNull().integerZeroOrGreater().isOneOfTheseValues(0,1,2);

        final String chargeRoundingMode = this.fromApiJsonHelper.extractStringNamed("chargeRoundingMode", element);
        baseDataValidator.reset().parameter("chargeRoundingMode").value(chargeRoundingMode).notNull();

        final Integer chargeDecimalRegex = this.fromApiJsonHelper.extractIntegerNamed("chargeDecimalRegex", element,Locale.getDefault());
        baseDataValidator.reset().parameter("chargeDecimalRegex").value(chargeDecimalRegex).ignoreIfNull().integerZeroOrGreater().isOneOfTheseValues(0,1,2);

        final Integer gstDecimalRegex = this.fromApiJsonHelper.extractIntegerNamed("gstDecimalRegex", element,Locale.getDefault());
        baseDataValidator.reset().parameter("gstDecimalRegex").value(gstDecimalRegex).ignoreIfNull().integerZeroOrGreater();

        final Boolean isGstSlabEnabled=this.fromApiJsonHelper.extractBooleanNamed("isGstSlabEnabled",element);
        baseDataValidator.reset().parameter("isGstSlabEnabled").value(isGstSlabEnabled).ignoreIfNull().isOneOfTheseValues(true,false);

        final Integer gstSlabLimitApplyFor = this.fromApiJsonHelper.extractIntegerNamed("gstSlabLimitApplyFor", element,Locale.getDefault());
        baseDataValidator.reset().parameter("gstSlabLimitApplyFor").value(gstSlabLimitApplyFor).ignoreIfNull().integerZeroOrGreater();

        final Integer gstSlabLimitOperator = this.fromApiJsonHelper.extractIntegerNamed("gstSlabLimitOperator", element,Locale.getDefault());
        baseDataValidator.reset().parameter("gstSlabLimitOperator").value(gstSlabLimitOperator).ignoreIfNull().integerZeroOrGreater().isOneOfTheseValues(GstSlabLimitOperator.validateGstSlabOperator());

        final BigDecimal gstSlabLimitValue = this.fromApiJsonHelper.extractBigDecimalNamed("gstSlabLimitValue", element,Locale.getDefault());
        baseDataValidator.reset().parameter("gstSlabLimitValue").value(gstSlabLimitValue).ignoreIfNull().zeroOrPositiveAmount();

//        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
//        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

//        final BigDecimal maxChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxAmount", element.getAsJsonObject());
//        baseDataValidator.reset().parameter("maxAmount").value(maxChargeAmount).notNull().positiveAmount();
//
//        final BigDecimal minimumChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minAmount", element.getAsJsonObject());
//        baseDataValidator.reset().parameter("minAmount").value(minimumChargeAmount).notNull().positiveAmount();



        final Boolean isDefaultLoanCharge=this.fromApiJsonHelper.extractBooleanNamed("isDefaultLoanCharge",element);
        if(isDefaultLoanCharge!=null && isDefaultLoanCharge)
        {
            final BigDecimal maxChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxAmount", element.getAsJsonObject());
            final BigDecimal minimumChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minAmount", element.getAsJsonObject());
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
            if(!(Objects.equals(maxChargeAmount, amount) && Objects.equals(maxChargeAmount, minimumChargeAmount)))
            {
                baseDataValidator.reset().parameter("Min,Max,Default").value(minimumChargeAmount).defaultValidation();
            }
        }
        baseDataValidator.reset().parameter("enableGstCharges").value(enableGstCharges).ignoreIfNull().isOneOfTheseValues(true,false);
    if(chargeCalculationType!=null){
        ChargeCalculationType chargeCalculationTypes=ChargeCalculationType.fromInt(chargeCalculationType);

        if(chargeCalculationTypes.isFlat()){



            final BigDecimal maxChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxAmount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxAmount").value(maxChargeAmount).notNull().positiveAmount();

            final BigDecimal minimumChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minAmount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minAmount").value(minimumChargeAmount).notNull().positiveAmount();

            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("amount").value(amount).ignoreIfNull().positiveAmount().inMinAndMaxAmountRange(minimumChargeAmount,maxChargeAmount);
        if (maxChargeAmount != null && maxChargeAmount.compareTo(BigDecimal.ZERO) >= 0) {


            if (minimumChargeAmount != null && minimumChargeAmount.compareTo(BigDecimal.ZERO) >= 0) {
                baseDataValidator.reset().parameter("maxAmount").value(maxChargeAmount).notLessThanMin(minimumChargeAmount);
                if (minimumChargeAmount.compareTo(maxChargeAmount) <= 0 && amount != null) {
                    baseDataValidator.reset().parameter("ChargeAmount").value(amount).inMinAndMaxAmountRange(minimumChargeAmount,
                            maxChargeAmount);
                }
            } else if (amount != null) {
                baseDataValidator.reset().parameter("ChargeAmount").value(amount).notGreaterThanMax(maxChargeAmount);
            }
        } else if (minimumChargeAmount != null && minimumChargeAmount.compareTo(BigDecimal.ZERO) >= 0 && amount != null) {
            baseDataValidator.reset().parameter("ChargeAmount").value(amount).notLessThanMin(minimumChargeAmount);
        }
        }
        else {

            final BigDecimal maxChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxAmount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxAmount").value(maxChargeAmount).ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(99));

            final BigDecimal minimumChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minAmount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minAmount").value(minimumChargeAmount).ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(99));


            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount().notGreaterThanMax(maxChargeAmount);

        }}



//            if (minimumChargeAmount != null && maxChargeAmount != null) {
//                notgreaterThan(minimumChargeAmount, maxChargeAmount, baseDataValidator,amount);
//            }

//            if(minimumChargeAmount.compareTo(maxChargeAmount) >=99 && amount.compareTo(minimumChargeAmount) >=99) {
//                if (minimumChargeAmount != null && minimumChargeAmount.compareTo(BigDecimal.ZERO) >= 0) {
//                    baseDataValidator.reset().parameter("maxAmount").value(maxChargeAmount).notLessThanMinPercentage(minimumChargeAmount);
//                    if (minimumChargeAmount.compareTo(maxChargeAmount) <= 0 && amount != null) {
//                        baseDataValidator.reset().parameter("ChargeAmount").value(amount).inMinAndMaxAmountRangePercentage(minimumChargeAmount,
//                                maxChargeAmount);
//                    }
//                } else if (amount != null) {
//                    baseDataValidator.reset().parameter("ChargeAmount").value(amount).notGreaterThanMaxPercentage(maxChargeAmount);
//                } else if (minimumChargeAmount != null && minimumChargeAmount.compareTo(BigDecimal.ZERO) >= 0 && amount != null) {
//                    baseDataValidator.reset().parameter("ChargeAmount").value(amount).notLessThanMinPercentage(minimumChargeAmount);
//                }
//            }
////            else{
//
//               validateCharge(baseDataValidator,maxChargeAmount,minimumChargeAmount,amount);
//            }



//            else {
//
//                chargeValidation(baseDataValidator,minimumChargeAmount,maxChargeAmount,amount);
//
//                final Integer total=100;
//
//
//                final BigDecimal totalInterest= sum(minimumChargeAmount,maxChargeAmount);
//                if(total != totalInterest){
//                    baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
//                            "validation Error",
//                            "One of the charge is more than 100");}}



//    private void notgreaterThan(BigDecimal minimumChargeAmount, BigDecimal maxChargeAmount, DataValidatorBuilder baseDataValidator) {
//
//        final Integer total=10;
//
//
//                final BigDecimal totalInterest=minimumChargeAmount.add(maxChargeAmount);
//                if(total != totalInterest){
//                    baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
//                            "validation Error",
//                            "One of the charge is more than 100");}}



//        ChargeCalculationType chargeCalculationTypes=ChargeCalculationType.fromInt(chargeCalculationType);
//        final String type=chargeCalculationTypes.getCode();
//        if(type== "PERCENT_OF_AMOUNT") {
//
//            if (minimumChargeAmount != null && minimumChargeAmount.compareTo(BigDecimal.ZERO) >= 0) {
//                baseDataValidator.reset().parameter("maxAmount").value(amount).notgreaterthan(minimumChargeAmount,maxChargeAmount,amount);
//
//            }
//        }


        if (this.fromApiJsonHelper.parameterExists("penalty", element)) {
            final Boolean penalty = this.fromApiJsonHelper.extractBooleanNamed("penalty", element);
            baseDataValidator.reset().parameter("penalty").value(penalty).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("active", element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed("active", element);
            baseDataValidator.reset().parameter("active").value(active).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("minCap", element)) {
            final BigDecimal minCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minCap").value(minCap).notNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists("maxCap", element)) {
            final BigDecimal maxCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxCap").value(maxCap).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.taxGroupIdParamName, element)) {
            final Long taxGroupId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.taxGroupIdParamName, element);
            baseDataValidator.reset().parameter(ChargesApiConstants.taxGroupIdParamName).value(taxGroupId).notNull().longGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

//    private void notgreaterThan(BigDecimal minimumChargeAmount, BigDecimal maxChargeAmount, DataValidatorBuilder baseDataValidator,BigDecimal amount) {
//
//
//                final BigDecimal totalInterest=minimumChargeAmount.add(maxChargeAmount);
//                if(amount != totalInterest){
//                    baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
//                            "validation Error",
//                            "sum of ");}}
//
//
//    }




    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists("name", element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
            baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);
        }
        if (this.fromApiJsonHelper.parameterExists("feesChargeTypeSelected", element)) {
            final Integer feeChargeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("feesChargeTypeSelected", element);
            baseDataValidator.reset().parameter("feesChargeTypeSelected").value(feeChargeType).notNull();
        }
        if (this.fromApiJsonHelper.parameterExists("typeSelected", element)) {
            final Integer typeSelected = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("typeSelected", element);
            baseDataValidator.reset().parameter("typeSelected").value(typeSelected).notNull().isOneOfTheseValues(53,54);
        }
        if(this.fromApiJsonHelper.parameterExists("enableGstChargesSelected",element)){
            final Boolean enableGstChargesSelected = this.fromApiJsonHelper.extractBooleanNamed("enableGstChargesSelected", element);
            if(Boolean.TRUE.equals(enableGstChargesSelected)) {
                final Integer gstOption = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("gstOption", element);
                baseDataValidator.reset().parameter("gstOption").value(gstOption).notNull();

                final Integer gstDecimal = this.fromApiJsonHelper.extractIntegerNamed("gstDecimal", element, Locale.getDefault());
                baseDataValidator.reset().parameter("gstDecimal").value(gstDecimal).notNull().integerZeroOrGreater().isOneOfTheseValues(0, 1, 2);

                final String gstRoundingMode = this.fromApiJsonHelper.extractStringNamed("gstRoundingMode", element);
                baseDataValidator.reset().parameter("gstRoundingMode").value(gstRoundingMode).notNull();
            }
        }

        if (this.fromApiJsonHelper.parameterExists("currencyCode", element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed("currencyCode", element);
            baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notNull().notBlank().notExceedingLengthOf(3).isOneOfTheseValues("INR","USD");
        }

        final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeCalculationType", element);
        baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType).notNull();
        final Boolean isDefaultLoanCharge=this.fromApiJsonHelper.extractBooleanNamed("isDefaultLoanCharge",element);
        if(isDefaultLoanCharge!=null && isDefaultLoanCharge)
        {
            final BigDecimal maxChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxChargeAmount", element.getAsJsonObject());
            final BigDecimal minimumChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minChargeAmount", element.getAsJsonObject());
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
            if(!(Objects.equals(maxChargeAmount, amount) && Objects.equals(maxChargeAmount, minimumChargeAmount)))
            {
                baseDataValidator.reset().parameter("Min,Max,Default").value(minimumChargeAmount).defaultValidation();
            }
        }
        if(chargeCalculationType!=null){
            ChargeCalculationType chargeCalculationTypes=ChargeCalculationType.fromInt(chargeCalculationType);
        if(chargeCalculationTypes.isFlat()){



            final BigDecimal maxChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxChargeAmount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxChargeAmount").value(maxChargeAmount).notNull().positiveAmount();

            final BigDecimal minimumChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minChargeAmount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minChargeAmount").value(minimumChargeAmount).notNull().positiveAmount();

            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount().inMinAndMaxAmountRange(minimumChargeAmount,maxChargeAmount);

            if (maxChargeAmount != null && maxChargeAmount.compareTo(BigDecimal.ZERO) >= 0) {


                if (minimumChargeAmount != null && minimumChargeAmount.compareTo(BigDecimal.ZERO) >= 0) {
                    baseDataValidator.reset().parameter("minChargeAmount").value(maxChargeAmount).notLessThanMin(minimumChargeAmount);
                    if (minimumChargeAmount.compareTo(maxChargeAmount) <= 0 && amount != null) {
                        baseDataValidator.reset().parameter("ChargeAmount").value(amount).inMinAndMaxAmountRange(minimumChargeAmount,
                                maxChargeAmount);
                    }
                } else if (amount != null) {
                    baseDataValidator.reset().parameter("ChargeAmount").value(amount).notGreaterThanMax(maxChargeAmount);
                }
            } else if (minimumChargeAmount != null && minimumChargeAmount.compareTo(BigDecimal.ZERO) >= 0 && amount != null) {
                baseDataValidator.reset().parameter("ChargeAmount").value(amount).notLessThanMin(minimumChargeAmount);
            }
        }
        else
        {

            final BigDecimal maxChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxChargeAmount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxChargeAmount").value(maxChargeAmount).ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(99));

            final BigDecimal minimumChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minChargeAmount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minChargeAmount").value(minimumChargeAmount).ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(99));


            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount().notGreaterThanMax(maxChargeAmount);

        }}


        if (this.fromApiJsonHelper.parameterExists("amount", element)) {
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();
        }


//            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
//            baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();
//
//            final BigDecimal minimumChargeAmount  = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minChargeAmount", element.getAsJsonObject());
//            baseDataValidator.reset().parameter("minChargeAmount").value(minimumChargeAmount).notNull().positiveAmount();
//
//
//            final BigDecimal maxChargeAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxChargeAmount", element.getAsJsonObject());
//            baseDataValidator.reset().parameter("maxChargeAmount").value(maxChargeAmount).notNull().positiveAmount();


//        if (maxChargeAmount != null && maxChargeAmount.compareTo(BigDecimal.ZERO) >= 0) {
//
//            if (minimumChargeAmount != null && minimumChargeAmount.compareTo(BigDecimal.ZERO) >= 0) {
//                baseDataValidator.reset().parameter("maxChargeAmount").value(maxChargeAmount).notLessThanMin(minimumChargeAmount);
//                if (minimumChargeAmount.compareTo(maxChargeAmount) <= 0 && amount != null) {
//                    baseDataValidator.reset().parameter("ChargeAmount").value(amount).inMinAndMaxAmountRange(minimumChargeAmount,
//                            maxChargeAmount);
//                }
//            } else if (amount != null) {
//                baseDataValidator.reset().parameter("ChargeAmount").value(amount).notGreaterThanMax(maxChargeAmount);
//            }
//        } else if (minimumChargeAmount != null && minimumChargeAmount.compareTo(BigDecimal.ZERO) >= 0 && amount != null) {
//            baseDataValidator.reset().parameter("ChargeAmount").value(amount).notLessThanMin(minimumChargeAmount);
//        }



        if (this.fromApiJsonHelper.parameterExists("minCap", element)) {
            final BigDecimal minCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minCap").value(minCap).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("maxCap", element)) {
            final BigDecimal maxCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxCap").value(maxCap).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("chargeAppliesTo", element)) {
            final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeAppliesTo", element);
            baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).notNull()
                    .isOneOfTheseValues(ChargeAppliesTo.validValues());
        }
       final String type=this.fromApiJsonHelper.extractStringNamed("type",element);
        baseDataValidator.reset().parameter("type").value(type).ignoreIfNull().isOneOfTheseValues("Fees","Charge");

        final Boolean enableGstCharges=this.fromApiJsonHelper.extractBooleanNamed("enableGstCharges",element);
        baseDataValidator.reset().parameter("enableGstCharges").value(enableGstCharges).ignoreIfNull().isOneOfTheseValues(true,false);

//        final Integer gst = this.fromApiJsonHelper.extractIntegerNamed("gst", element,Locale.getDefault());
//        baseDataValidator.reset().parameter("gst").value(gst).ignoreIfNull().integerZeroOrGreater();

        final Integer chargeDecimal = this.fromApiJsonHelper.extractIntegerNamed("chargeDecimal", element,Locale.getDefault());
        baseDataValidator.reset().parameter("chargeDecimal").value(chargeDecimal).notNull().integerZeroOrGreater().isOneOfTheseValues(0,1,2);

        final String chargeRoundingMode = this.fromApiJsonHelper.extractStringNamed("chargeRoundingMode", element);
        baseDataValidator.reset().parameter("chargeRoundingMode").value(chargeRoundingMode).notNull();

        final Integer chargeDecimalRegex = this.fromApiJsonHelper.extractIntegerNamed("chargeDecimalRegex", element,Locale.getDefault());
        baseDataValidator.reset().parameter("chargeDecimalRegex").value(chargeDecimalRegex).ignoreIfNull().integerZeroOrGreater();

        final Integer gstDecimalRegex = this.fromApiJsonHelper.extractIntegerNamed("gstDecimalRegex", element,Locale.getDefault());
        baseDataValidator.reset().parameter("gstDecimalRegex").value(gstDecimalRegex).ignoreIfNull().integerZeroOrGreater();

        final Boolean isGstSlabEnabled=this.fromApiJsonHelper.extractBooleanNamed("isGstSlabEnabled",element);
        baseDataValidator.reset().parameter("isGstSlabEnabled").value(isGstSlabEnabled).ignoreIfNull().isOneOfTheseValues(true,false);

        final Integer gstSlabLimitApplyFor = this.fromApiJsonHelper.extractIntegerNamed("gstSlabLimitApplyFor", element,Locale.getDefault());
        baseDataValidator.reset().parameter("gstSlabLimitApplyFor").value(gstSlabLimitApplyFor).ignoreIfNull().integerZeroOrGreater();

        final Integer gstSlabLimitOperator = this.fromApiJsonHelper.extractIntegerNamed("gstSlabLimitOperator", element,Locale.getDefault());
        baseDataValidator.reset().parameter("gstSlabLimitOperator").value(gstSlabLimitOperator).ignoreIfNull().integerZeroOrGreater().isOneOfTheseValues(GstSlabLimitOperator.validateGstSlabOperator());

        final BigDecimal gstSlabLimitValue = this.fromApiJsonHelper.extractBigDecimalNamed("gstSlabLimitValue", element,Locale.getDefault());
        baseDataValidator.reset().parameter("gstSlabLimitValue").value(gstSlabLimitValue).ignoreIfNull().zeroOrPositiveAmount();

        Boolean enableFreeWithdrawalCharge = false;
        if (this.fromApiJsonHelper.parameterExists("enableFreeWithdrawalCharge", element)) {
            enableFreeWithdrawalCharge = this.fromApiJsonHelper.extractBooleanNamed("enableFreeWithdrawalCharge", element);
            baseDataValidator.reset().parameter("enableFreeWithdrawalCharge").value(enableFreeWithdrawalCharge).notNull();

            if (enableFreeWithdrawalCharge) {

                final Integer freeWithdrawalFrequency = this.fromApiJsonHelper.extractIntegerNamed("freeWithdrawalFrequency", element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter("freeWithdrawalFrequency").value(freeWithdrawalFrequency).integerGreaterThanZero();

                final Integer restartCountFrequency = this.fromApiJsonHelper.extractIntegerNamed("restartCountFrequency", element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter("restartCountFrequency").value(restartCountFrequency).integerGreaterThanZero();

                final Integer countFrequencyType = this.fromApiJsonHelper.extractIntegerNamed("countFrequencyType", element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter("countFrequencyType").value(countFrequencyType);
            }

            Boolean enablePaymentType = false;
            if (this.fromApiJsonHelper.parameterExists("enablePaymentType", element)) {
                enablePaymentType = this.fromApiJsonHelper.extractBooleanNamed("enablePaymentType", element);
                baseDataValidator.reset().parameter("enablePaymentType").value(enablePaymentType).notNull();

                if (enablePaymentType) {
                    final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerNamed("paymentTypeId", element, Locale.getDefault());
                    baseDataValidator.reset().parameter("paymentTypeId").value(paymentTypeId).integerGreaterThanZero();
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists("chargeAppliesTo", element)) {
            final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeAppliesTo", element);
            baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).notNull()
                    .isOneOfTheseValues(ChargeAppliesTo.validValues());
        }

        if (this.fromApiJsonHelper.parameterExists("chargeAppliesTo", element)) {
            final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeAppliesTo", element);
            baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).notNull()
                    .isOneOfTheseValues(ChargeAppliesTo.validValues());
        }

        if (this.fromApiJsonHelper.parameterExists("chargeTimeType", element)) {

            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);

            final Collection<Object> validLoanValues = Arrays.asList(ChargeTimeType.validLoanValues());
            final Collection<Object> validSavingsValues = Arrays.asList(ChargeTimeType.validSavingsValues());
            final Collection<Object> validClientValues = Arrays.asList(ChargeTimeType.validClientValues());
            final Collection<Object> validShareValues = Arrays.asList(ChargeTimeType.validShareValues());
            final Collection<Object> allValidValues = new ArrayList<>(validLoanValues);
            allValidValues.addAll(validSavingsValues);
            allValidValues.addAll(validClientValues);
            allValidValues.addAll(validShareValues);
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull()
                    .isOneOfTheseValues(allValidValues.toArray(new Object[allValidValues.size()]));
        }

        if (this.fromApiJsonHelper.parameterExists("feeOnMonthDay", element)) {
            final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed("feeOnMonthDay", element);
            baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("feeInterval", element)) {
            final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed("feeInterval", element, Locale.getDefault());
            baseDataValidator.reset().parameter("feeInterval").value(feeInterval).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists("chargeCalculationType", element)) {
            final Integer chargeCalculation = this.fromApiJsonHelper.extractIntegerNamed("chargeCalculationType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculation).notNull().inMinMaxRange(1, 5);
        }

        if (this.fromApiJsonHelper.parameterExists("chargePaymentMode", element)) {
            final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerNamed("chargePaymentMode", element, Locale.getDefault());
            baseDataValidator.reset().parameter("chargePaymentMode").value(chargePaymentMode).notNull().inMinMaxRange(0, 1);
        }

        if (this.fromApiJsonHelper.parameterExists("penalty", element)) {
            final Boolean penalty = this.fromApiJsonHelper.extractBooleanNamed("penalty", element);
            baseDataValidator.reset().parameter("penalty").value(penalty).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("active", element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed("active", element);
            baseDataValidator.reset().parameter("active").value(active).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("minCap", element)) {
            final BigDecimal minCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minCap").value(minCap).notNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists("maxCap", element)) {
            final BigDecimal maxCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxCap").value(maxCap).notNull().positiveAmount();
        }
        final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);
        if (chargeTimeType != null) {
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType)
                    .isOneOfTheseValues(ChargeTimeType.validLoanValues());
        }
        final ChargeTimeType ctt = ChargeTimeType.fromInt(chargeTimeType);
        if(ctt.isOverdueInstallment()){

            final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed("feeInterval", element, Locale.getDefault());
            baseDataValidator.reset().parameter("feeInterval").value(feeInterval).integerGreaterThanZero().notNull();

            final Integer feeFrequency = this.fromApiJsonHelper.extractIntegerNamed("feeFrequency", element, Locale.getDefault());
            baseDataValidator.reset().parameter("feeFrequency").value(feeFrequency).inMinMaxRange(0, 3).notNull();

            final Integer penalityDaysInYear = this.fromApiJsonHelper.extractIntegerNamed("penaltyInterestDaysInYear", element, Locale.getDefault());
            baseDataValidator.reset().parameter("penaltyInterestDaysInYear").value(penalityDaysInYear).inMinMaxRange(0, 2).notNull();
        }


        if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.glAccountIdParamName, element)) {
            final Long glAccountId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.glAccountIdParamName, element);
            baseDataValidator.reset().parameter(ChargesApiConstants.glAccountIdParamName).value(glAccountId).notNull()
                    .longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.taxGroupIdParamName, element)) {
            final Long taxGroupId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.taxGroupIdParamName, element);
            baseDataValidator.reset().parameter(ChargesApiConstants.taxGroupIdParamName).value(taxGroupId).notNull().longGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChargeTimeNCalculationType(Integer chargeTimeType, Integer ChargeCalculationType) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");
        performChargeTimeNCalculationTypeValidation(baseDataValidator, chargeTimeType, ChargeCalculationType);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void performChargeTimeNCalculationTypeValidation(DataValidatorBuilder baseDataValidator, final Integer chargeTimeType,
            final Integer chargeCalculationType) {
        if (chargeTimeType.equals(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue())) {
            baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                    .isOneOfTheseValues(ChargeCalculationType.validValuesForShareAccountActivation());
        }

        if (chargeTimeType.equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue())) {
            baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                    .isOneOfTheseValues(ChargeCalculationType.validValuesForTrancheDisbursement());
        } else {
            baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                    .isNotOneOfTheseValues(ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue());
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
