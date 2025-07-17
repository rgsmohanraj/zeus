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
package org.vcpl.lms.portfolio.charge.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.vcpl.lms.accounting.glaccount.data.GLAccountData;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.portfolio.charge.domain.ChargeTimeType;
import org.vcpl.lms.portfolio.loanaccount.data.LoanChargeData;
import org.vcpl.lms.portfolio.paymenttype.data.PaymentTypeData;
import org.vcpl.lms.portfolio.savings.data.SavingsAccountChargeData;
import org.vcpl.lms.portfolio.shareaccounts.data.ShareAccountChargeData;
import org.vcpl.lms.portfolio.tax.data.TaxGroupData;

/**
 * Immutable data object for charge data.
 */
public final class ChargeData implements Comparable<ChargeData>, Serializable {

    private final Long id;
    private final String name;
    private final boolean active;
    private final boolean penalty;
    private final boolean freeWithdrawal;
    private final Integer freeWithdrawalChargeFrequency;
    private final Integer restartFrequency;
    private final Integer restartFrequencyEnum;
    private final boolean isPaymentType;
    private final PaymentTypeData paymentTypeOptions;
    private final CurrencyData currency;
    private final BigDecimal amount;
    private final EnumOptionData chargeTimeType;
    private final EnumOptionData chargeAppliesTo;
    private final EnumOptionData chargeCalculationType;
    private final EnumOptionData chargePaymentMode;
    private final MonthDay feeOnMonthDay;
    private final Integer feeInterval;
    private final BigDecimal minCap;
    private final BigDecimal maxCap;
    private final EnumOptionData feeFrequency;
    private final GLAccountData incomeOrLiabilityAccount;
    private final TaxGroupData taxGroup;

    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> chargeCalculationTypeOptions;//
    private final List<EnumOptionData> chargeAppliesToOptions;//
    private final List<EnumOptionData> chargeTimeTypeOptions;//
    private final List<EnumOptionData> chargePaymetModeOptions;//

    private final List<EnumOptionData> loanChargeCalculationTypeOptions;
    private final List<EnumOptionData> loanChargeTimeTypeOptions;
    private final List<EnumOptionData> savingsChargeCalculationTypeOptions;
    private final List<EnumOptionData> savingsChargeTimeTypeOptions;
    private final List<EnumOptionData> clientChargeCalculationTypeOptions;
    private final List<EnumOptionData> clientChargeTimeTypeOptions;
    private final List<EnumOptionData> shareChargeCalculationTypeOptions;
    private final List<EnumOptionData> shareChargeTimeTypeOptions;

    private final List<EnumOptionData> feeFrequencyOptions;

    private final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions;
    private final Collection<TaxGroupData> taxGroupOptions;

    private final String accountMappingForChargeConfig;
    private final List<GLAccountData> expenseAccountOptions;
    private final List<GLAccountData> assetAccountOptions;
    private final List<CodeValueData> typeOption;
    private final List<CodeValueData> feesOption;


    private final Boolean enableGstCharges;
    private final List<EnumOptionData> gstOption;
    private final Boolean enableGstChargesSelected;
    private final EnumOptionData gstSelected;
    private final BigDecimal minChargeAmount;
    private final BigDecimal maxChargeAmount;
    private final String type;
    private final String feesChargeType;
    private final CodeValueData typeSelected;
    private final CodeValueData feesChargeTypeSelected;
    private final List<EnumOptionData> daysInYearTypeOptions;
    private final EnumOptionData penaltyInterestDaysInYearSelected;
    private final Integer chargeDecimal;
    private final String chargeRoundingMode;
    private final Integer chargeDecimalRegex;
    private final Integer gstDecimal;
    private final String gstRoundingMode;
    private final Integer gstDecimalRegex;
    private final BigDecimal gstSlabLimitValue;
    private final Boolean isGstSlabEnabled;
    private final List<EnumOptionData> gstSlabLimitApplyForOption;
    private final List<EnumOptionData> gstSlabLimitOperatorOption;
    private final Boolean isGstSlabEnabledSelected;
    private final EnumOptionData gstSlabLimitApplyForSelected;
    private final EnumOptionData gstSlabLimitOperatorSelected;

    private final List<RoundingMode> roundingModes;

    private final Boolean isDefaultLoanCharge;
    public static ChargeData template(final Collection<CurrencyData> currencyOptions,
                                      final List<EnumOptionData> chargeCalculationTypeOptions, final List<EnumOptionData> chargeAppliesToOptions,
                                      final List<EnumOptionData> chargeTimeTypeOptions, final List<EnumOptionData> chargePaymentModeOptions,
                                      final List<EnumOptionData> loansChargeCalculationTypeOptions, final List<EnumOptionData> loansChargeTimeTypeOptions,
                                      final List<EnumOptionData> savingsChargeCalculationTypeOptions, final List<EnumOptionData> savingsChargeTimeTypeOptions,
                                      final List<EnumOptionData> clientChargeCalculationTypeOptions, final List<EnumOptionData> clientChargeTimeTypeOptions,
                                      final List<EnumOptionData> feeFrequencyOptions, final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions,
                                      final Collection<TaxGroupData> taxGroupOptions, final List<EnumOptionData> shareChargeCalculationTypeOptions,
                                      final List<EnumOptionData> shareChargeTimeTypeOptions, String accountMappingForChargeConfig,
                                      List<GLAccountData> expenseAccountOptions, List<GLAccountData> assetAccountOptions,
                                      final List<CodeValueData> typeOption, final Boolean enableGstCharges, final List<EnumOptionData> gstOption,
                                      final List<CodeValueData> feesOption, final List<EnumOptionData> daysInYearTypeOptions,
                                      final Boolean isGstSlabEnabled, final List<EnumOptionData> gstSlabLimitApplyForOption, final List<EnumOptionData> gstSlabLimitOperatorOption, final List<RoundingMode> roundingModes,final Boolean isDefaultLoanCharge) {
        final GLAccountData account = null;
        final TaxGroupData taxGroupData = null;
        final Boolean enableGstChargesSelected =false;
        final EnumOptionData gstSelected=null;
        final Boolean isGstSlabEnabledSelected =false;
        final EnumOptionData gstSlabLimitApplyForSelected=null;
        final EnumOptionData gstSlabLimitOperatorSelected=null;
        final BigDecimal minChargeAmount=null;
        final BigDecimal maxChargeAmount=null;
        final String type=null;
        final String feesChargeType=null;
        final CodeValueData typeSelected =null;
        final CodeValueData feesChargeTypeSelected =null;
        final BigDecimal selfShare = null;
        final BigDecimal partnerShare = null;
        final Integer chargeDecimal = null;
        final String chargeRoundingMode = null;
        final Integer chargeDecimalRegex = null;
        final Integer gstDecimal = null;
        final String gstRoundingMode = null;
        final Integer gstDecimalRegex = null;
        final BigDecimal gstSlabLimitValue = null;


        return new ChargeData(null, null, null, null, null, null, null, null, false, false, false, null, null, null, false, null,
                taxGroupData, currencyOptions, chargeCalculationTypeOptions, chargeAppliesToOptions, chargeTimeTypeOptions,
                chargePaymentModeOptions, loansChargeCalculationTypeOptions, loansChargeTimeTypeOptions,
                savingsChargeCalculationTypeOptions, savingsChargeTimeTypeOptions, clientChargeCalculationTypeOptions,
                clientChargeTimeTypeOptions, null, null, null, null, null, feeFrequencyOptions, account, incomeOrLiabilityAccountOptions,
                taxGroupOptions, shareChargeCalculationTypeOptions, shareChargeTimeTypeOptions, accountMappingForChargeConfig,
                expenseAccountOptions, assetAccountOptions,typeOption,enableGstCharges,gstOption,enableGstChargesSelected,gstSelected,minChargeAmount,maxChargeAmount,type,typeSelected,feesChargeTypeSelected,feesOption,feesChargeType, daysInYearTypeOptions, null,chargeDecimal,chargeRoundingMode,chargeDecimalRegex,gstDecimal,gstRoundingMode,gstDecimalRegex,gstSlabLimitValue,
                isGstSlabEnabled,gstSlabLimitApplyForOption,gstSlabLimitOperatorOption,isGstSlabEnabledSelected,gstSlabLimitApplyForSelected,gstSlabLimitOperatorSelected,roundingModes, isDefaultLoanCharge);
    }

    public static ChargeData withTemplate(final ChargeData charge, final ChargeData template) {
        return new ChargeData(charge.id, charge.name, charge.amount, charge.currency, charge.chargeTimeType, charge.chargeAppliesTo,
                charge.chargeCalculationType, charge.chargePaymentMode, charge.penalty, charge.active, charge.freeWithdrawal,
                charge.freeWithdrawalChargeFrequency, charge.restartFrequency, charge.restartFrequencyEnum, charge.isPaymentType,
                charge.paymentTypeOptions, charge.taxGroup, template.currencyOptions, template.chargeCalculationTypeOptions,
                template.chargeAppliesToOptions, template.chargeTimeTypeOptions, template.chargePaymetModeOptions,
                template.loanChargeCalculationTypeOptions, template.loanChargeTimeTypeOptions, template.savingsChargeCalculationTypeOptions,
                template.savingsChargeTimeTypeOptions, template.clientChargeCalculationTypeOptions, template.clientChargeTimeTypeOptions,
                charge.feeOnMonthDay, charge.feeInterval, charge.minCap, charge.maxCap, charge.feeFrequency, template.feeFrequencyOptions,
                charge.incomeOrLiabilityAccount, template.incomeOrLiabilityAccountOptions, template.taxGroupOptions,
                template.shareChargeCalculationTypeOptions, template.shareChargeTimeTypeOptions, template.accountMappingForChargeConfig,
                template.expenseAccountOptions, template.assetAccountOptions,template.typeOption,template.enableGstCharges,template.gstOption,
                charge.enableGstChargesSelected,charge.gstSelected,charge.minChargeAmount,charge.maxChargeAmount,charge.type,charge.typeSelected,charge.feesChargeTypeSelected,template.feesOption,charge.feesChargeType, template.daysInYearTypeOptions, charge.penaltyInterestDaysInYearSelected,
                charge.chargeDecimal,charge.chargeRoundingMode,charge.chargeDecimalRegex,charge.gstDecimal,charge.gstRoundingMode,charge.gstDecimalRegex,charge.gstSlabLimitValue,template.isGstSlabEnabled,template.gstSlabLimitApplyForOption,template.gstSlabLimitOperatorOption,
                charge.isGstSlabEnabledSelected,charge.gstSlabLimitApplyForSelected,charge.gstSlabLimitOperatorSelected,template.roundingModes, charge.isDefaultLoanCharge);
    }

    public static ChargeData instance(final Long id, final String name, final BigDecimal amount, final CurrencyData currency,
            final EnumOptionData chargeTimeType, final EnumOptionData chargeAppliesTo, final EnumOptionData chargeCalculationType,
            final EnumOptionData chargePaymentMode, final MonthDay feeOnMonthDay, final Integer feeInterval, final boolean penalty,
            final boolean active, final boolean freeWithdrawal, final Integer freeWithdrawalChargeFrequency, final Integer restartFrequency,
            final Integer restartFrequencyEnum, final boolean isPaymentType, final PaymentTypeData paymentTypeOptions,
            final BigDecimal minCap, final BigDecimal maxCap, final EnumOptionData feeFrequency, final GLAccountData accountData,
            TaxGroupData taxGroupData,final Boolean enableGstCharge,final EnumOptionData gst,final BigDecimal minChargeAmount,
            final BigDecimal maxChargeAmount,final String type,final CodeValueData typeSelected,final CodeValueData feesChargeTypeSelected,final String feesChargeType, final EnumOptionData penaltyInterestDaysInYearSelected,
            final Integer chargeDecimal,final String chargeRoundingMode,final Integer chargeDecimalRegex,final Integer gstDecimal,final String gstRoundingMode,final Integer gstDecimalRegex,final BigDecimal gstSlabLimitValue,
            final Boolean isGstSlabEnable,final EnumOptionData gstSlabLimitApplyFor,final EnumOptionData gstSlabLimitOperator,final Boolean isDefault) {

        final Collection<CurrencyData> currencyOptions = null;
        final List<EnumOptionData> chargeCalculationTypeOptions = null;
        final List<EnumOptionData> chargeAppliesToOptions = null;
        final List<EnumOptionData> chargeTimeTypeOptions = null;
        final List<EnumOptionData> chargePaymentModeOptions = null;
        final List<EnumOptionData> loansChargeCalculationTypeOptions = null;
        final List<EnumOptionData> loansChargeTimeTypeOptions = null;
        final List<EnumOptionData> savingsChargeCalculationTypeOptions = null;
        final List<EnumOptionData> savingsChargeTimeTypeOptions = null;
        final List<EnumOptionData> feeFrequencyOptions = null;
        final List<EnumOptionData> clientChargeCalculationTypeOptions = null;
        final List<EnumOptionData> clientChargeTimeTypeOptions = null;
        final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions = null;
        final List<EnumOptionData> shareChargeCalculationTypeOptions = null;
        final List<EnumOptionData> shareChargeTimeTypeOptions = null;
        final Collection<TaxGroupData> taxGroupOptions = null;
        final String accountMappingForChargeConfig = null;
        final List<GLAccountData> expenseAccountOptions = null;
        final List<GLAccountData> assetAccountOptions = null;
        final List<CodeValueData> typeOption=null;
        final List<CodeValueData> feesOption=null;

        final Boolean enableGstCharges=false;
        final List<EnumOptionData> gstOption=null;
        final Boolean isGstSlabEnabled = false;
        final List<EnumOptionData> gstSlabLimitApplyForOption = null;
        final List<EnumOptionData> gstSlabLimitOperatorOption = null;
        return new ChargeData(id, name, amount, currency, chargeTimeType, chargeAppliesTo, chargeCalculationType, chargePaymentMode,
                penalty, active, freeWithdrawal, freeWithdrawalChargeFrequency, restartFrequency, restartFrequencyEnum, isPaymentType,
                paymentTypeOptions, taxGroupData, currencyOptions, chargeCalculationTypeOptions, chargeAppliesToOptions,
                chargeTimeTypeOptions, chargePaymentModeOptions, loansChargeCalculationTypeOptions, loansChargeTimeTypeOptions,
                savingsChargeCalculationTypeOptions, savingsChargeTimeTypeOptions, clientChargeCalculationTypeOptions,
                clientChargeTimeTypeOptions, feeOnMonthDay, feeInterval, minCap, maxCap, feeFrequency, feeFrequencyOptions, accountData,
                incomeOrLiabilityAccountOptions, taxGroupOptions, shareChargeCalculationTypeOptions, shareChargeTimeTypeOptions,
                accountMappingForChargeConfig, expenseAccountOptions, assetAccountOptions,typeOption,enableGstCharges,gstOption,enableGstCharge,gst,minChargeAmount,maxChargeAmount,type,typeSelected,feesChargeTypeSelected,feesOption,feesChargeType, null, penaltyInterestDaysInYearSelected,
                chargeDecimal,chargeRoundingMode,chargeDecimalRegex,gstDecimal,gstRoundingMode,gstDecimalRegex,gstSlabLimitValue,isGstSlabEnabled,gstSlabLimitApplyForOption,gstSlabLimitOperatorOption,isGstSlabEnable,gstSlabLimitApplyFor,gstSlabLimitOperator,null, isDefault);
    }

    public static ChargeData lookup(final Long id, final String name, final boolean isPenalty) {
        final BigDecimal amount = null;
        final CurrencyData currency = null;
        final EnumOptionData chargeTimeType = null;
        final EnumOptionData chargeAppliesTo = null;
        final EnumOptionData chargeCalculationType = null;
        final EnumOptionData chargePaymentMode = null;
        final MonthDay feeOnMonthDay = null;
        final Integer feeInterval = null;
        final Boolean penalty = isPenalty;
        final Boolean active = false;
        final Boolean freeWithdrawal = false;
        final Integer freeWithdrawalChargeFrequency = null;
        final Integer restartFrequency = null;
        final Integer restartFrequencyEnum = null;
        final Boolean isPaymentType = false;
        final PaymentTypeData paymentTypeOptions = null;
        final BigDecimal minCap = null;
        final BigDecimal maxCap = null;
        final Collection<CurrencyData> currencyOptions = null;
        final List<EnumOptionData> chargeCalculationTypeOptions = null;
        final List<EnumOptionData> chargeAppliesToOptions = null;
        final List<EnumOptionData> chargeTimeTypeOptions = null;
        final List<EnumOptionData> chargePaymentModeOptions = null;
        final List<EnumOptionData> loansChargeCalculationTypeOptions = null;
        final List<EnumOptionData> loansChargeTimeTypeOptions = null;
        final List<EnumOptionData> savingsChargeCalculationTypeOptions = null;
        final List<EnumOptionData> savingsChargeTimeTypeOptions = null;
        final List<EnumOptionData> clientChargeCalculationTypeOptions = null;
        final List<EnumOptionData> clientChargeTimeTypeOptions = null;
        final EnumOptionData feeFrequency = null;
        final List<EnumOptionData> feeFrequencyOptions = null;
        final GLAccountData account = null;
        final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions = null;
        final List<EnumOptionData> shareChargeCalculationTypeOptions = null;
        final List<EnumOptionData> shareChargeTimeTypeOptions = null;
        final TaxGroupData taxGroupData = null;
        final Collection<TaxGroupData> taxGroupOptions = null;
        final String accountMappingForChargeConfig = null;
        final List<GLAccountData> expenseAccountOptions = null;
        final List<GLAccountData> assetAccountOptions = null;
        final List<CodeValueData> typeOption =null;
        final List<CodeValueData> feesOption =null;
        final Boolean enableGstCharges=false;
        final List<EnumOptionData> gstOption=null;
        final Boolean enableGstChargesSelected = false;
        final EnumOptionData gstSelected = null;
        final Boolean isGstSlabEnabledSelected = false;
        final EnumOptionData gstSlabLimitApplyForSelected = null;
        final EnumOptionData gstSlabLimitOperatorSelected = null;
        final BigDecimal minChargeAmount=null;
        final BigDecimal maxChargeAmount =null;
        final String type=null;
        final String feesChargeType=null;
        final CodeValueData typeSelected=null;
        final CodeValueData feesChargeTypeSelected=null;
        final BigDecimal selfShare = null;
        final BigDecimal partnerShare = null;
        final Integer chargeDecimal = null;
        final String chargeRoundingMode = null;
        final Integer chargeDecimalRegex = null;
        final Integer gstDecimal = null;
        final String gstRoundingMode = null;
        final Integer gstDecimalRegex = null;
        final BigDecimal gstSlabLimitValue = null;
        final Boolean isGstSlabEnabled=false;
        final List<EnumOptionData> gstSlabLimitApplyForOption=null;
        final List<EnumOptionData> gstSlabLimitOperatorOption=null;
        final Boolean isDefault = false;

        return new ChargeData(id, name, amount, currency, chargeTimeType, chargeAppliesTo, chargeCalculationType, chargePaymentMode,
                penalty, active, freeWithdrawal, freeWithdrawalChargeFrequency, restartFrequency, restartFrequencyEnum, isPaymentType,
                paymentTypeOptions, taxGroupData, currencyOptions, chargeCalculationTypeOptions, chargeAppliesToOptions,
                chargeTimeTypeOptions, chargePaymentModeOptions, loansChargeCalculationTypeOptions, loansChargeTimeTypeOptions,
                savingsChargeCalculationTypeOptions, savingsChargeTimeTypeOptions, clientChargeCalculationTypeOptions,
                clientChargeTimeTypeOptions, feeOnMonthDay, feeInterval, minCap, maxCap, feeFrequency, feeFrequencyOptions, account,
                incomeOrLiabilityAccountOptions, taxGroupOptions, shareChargeCalculationTypeOptions, shareChargeTimeTypeOptions,
                accountMappingForChargeConfig, expenseAccountOptions, assetAccountOptions,typeOption,enableGstCharges,gstOption,
                enableGstChargesSelected,gstSelected,minChargeAmount,maxChargeAmount,type,typeSelected,feesChargeTypeSelected,feesOption,feesChargeType, null, null,
                chargeDecimal,chargeRoundingMode,chargeDecimalRegex,gstDecimal,gstRoundingMode,gstDecimalRegex,gstSlabLimitValue,isGstSlabEnabled,gstSlabLimitApplyForOption,gstSlabLimitOperatorOption,
                isGstSlabEnabledSelected,gstSlabLimitApplyForSelected,gstSlabLimitOperatorSelected,null, isDefault);
    }

    public ChargeData(final Long id, final String name, final BigDecimal amount, final CurrencyData currency,
                      final EnumOptionData chargeTimeType, final EnumOptionData chargeAppliesTo, final EnumOptionData chargeCalculationType,
                      final EnumOptionData chargePaymentMode, final boolean penalty, final boolean active, final boolean freeWithdrawal,
                      final Integer freeWithdrawalChargeFrequency, final Integer restartFrequency, final Integer restartFrequencyEnum,
                      final boolean isPaymentType, final PaymentTypeData paymentTypeOptions, final TaxGroupData taxGroupData,
                      final Collection<CurrencyData> currencyOptions, final List<EnumOptionData> chargeCalculationTypeOptions,
                      final List<EnumOptionData> chargeAppliesToOptions, final List<EnumOptionData> chargeTimeTypeOptions,
                      final List<EnumOptionData> chargePaymentModeOptions, final List<EnumOptionData> loansChargeCalculationTypeOptions,
                      final List<EnumOptionData> loansChargeTimeTypeOptions, final List<EnumOptionData> savingsChargeCalculationTypeOptions,
                      final List<EnumOptionData> savingsChargeTimeTypeOptions, final List<EnumOptionData> clientChargeCalculationTypeOptions,
                      final List<EnumOptionData> clientChargeTimeTypeOptions, final MonthDay feeOnMonthDay, final Integer feeInterval,
                      final BigDecimal minCap, final BigDecimal maxCap, final EnumOptionData feeFrequency,
                      final List<EnumOptionData> feeFrequencyOptions, final GLAccountData account,
                      final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions, final Collection<TaxGroupData> taxGroupOptions,
                      final List<EnumOptionData> shareChargeCalculationTypeOptions, final List<EnumOptionData> shareChargeTimeTypeOptions,
                      final String accountMappingForChargeConfig, final List<GLAccountData> expenseAccountOptions,
                      final List<GLAccountData> assetAccountOptions, final List<CodeValueData> typeOption,
                      final Boolean enableGstCharges, final List<EnumOptionData> gstOption, final Boolean enableGstCharge,
                      final EnumOptionData gst, final BigDecimal minChargeAmount, final BigDecimal maxChargeAmount, final String type, final CodeValueData typeSelected, final CodeValueData feesChargeTypeSelected,
                      final List<CodeValueData> feesOption, final String feesChargeType, List<EnumOptionData> daysInYearTypeOptions, EnumOptionData penaltyInterestDaysInYearSelected,
                      final Integer chargeDecimal, final String chargeRoundingMode, final Integer chargeDecimalRegex, final Integer gstDecimal, final String gstRoundingMode, final Integer gstDecimalRegex, final BigDecimal gstSlabLimitValue,
                      final Boolean isGstSlabEnabled, final List<EnumOptionData> gstSlabLimitApplyForOption, final List<EnumOptionData> gstSlabLimitOperatorOption, final Boolean isGstSlabEnable, final EnumOptionData gstSlabLimitApplyFor, final EnumOptionData gstSlabLimitOperator,
                      final List<RoundingMode> roundingModes, Boolean isDefault) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.currency = currency;
        this.chargeTimeType = chargeTimeType;
        this.chargeAppliesTo = chargeAppliesTo;
        this.chargeCalculationType = chargeCalculationType;
        this.chargePaymentMode = chargePaymentMode;
        this.feeInterval = feeInterval;
        this.feeOnMonthDay = feeOnMonthDay;
        this.penalty = penalty;
        this.active = active;
        this.freeWithdrawal = freeWithdrawal;
        this.freeWithdrawalChargeFrequency = freeWithdrawalChargeFrequency;
        this.restartFrequency = restartFrequency;
        this.restartFrequencyEnum = restartFrequencyEnum;
        this.isPaymentType = isPaymentType;
        this.paymentTypeOptions = paymentTypeOptions;
        this.minCap = minCap;
        this.maxCap = maxCap;
        this.currencyOptions = currencyOptions;
        this.chargeCalculationTypeOptions = chargeCalculationTypeOptions;
        this.chargeAppliesToOptions = chargeAppliesToOptions;
        this.chargeTimeTypeOptions = chargeTimeTypeOptions;
        this.chargePaymetModeOptions = chargePaymentModeOptions;
        this.savingsChargeCalculationTypeOptions = savingsChargeCalculationTypeOptions;
        this.savingsChargeTimeTypeOptions = savingsChargeTimeTypeOptions;
        this.clientChargeCalculationTypeOptions = clientChargeCalculationTypeOptions;
        this.clientChargeTimeTypeOptions = clientChargeTimeTypeOptions;
        this.loanChargeCalculationTypeOptions = loansChargeCalculationTypeOptions;
        this.loanChargeTimeTypeOptions = loansChargeTimeTypeOptions;
        this.feeFrequency = feeFrequency;
        this.feeFrequencyOptions = feeFrequencyOptions;
        this.incomeOrLiabilityAccount = account;
        this.incomeOrLiabilityAccountOptions = incomeOrLiabilityAccountOptions;
        this.taxGroup = taxGroupData;
        this.taxGroupOptions = taxGroupOptions;
        this.shareChargeCalculationTypeOptions = shareChargeCalculationTypeOptions;
        this.shareChargeTimeTypeOptions = shareChargeTimeTypeOptions;
        this.accountMappingForChargeConfig = accountMappingForChargeConfig;
        this.assetAccountOptions = assetAccountOptions;
        this.expenseAccountOptions = expenseAccountOptions;
        this.typeOption=typeOption;
        this.feesOption=feesOption;
        this.enableGstCharges=enableGstCharges;
        this.gstOption=gstOption;
        this.enableGstChargesSelected=enableGstCharge;
        this.gstSelected=gst;
        this.isGstSlabEnabledSelected=isGstSlabEnable;
        this.gstSlabLimitApplyForSelected=gstSlabLimitApplyFor;
        this.gstSlabLimitOperatorSelected=gstSlabLimitOperator;
        this.minChargeAmount=minChargeAmount;
        this.maxChargeAmount=maxChargeAmount;
        this.type=type;
        this.feesChargeType=feesChargeType;
        this.typeSelected=typeSelected;
        this.feesChargeTypeSelected=feesChargeTypeSelected;
        this.daysInYearTypeOptions = daysInYearTypeOptions;
        this.penaltyInterestDaysInYearSelected = penaltyInterestDaysInYearSelected;
        this.chargeDecimal = chargeDecimal;
        this.chargeRoundingMode = chargeRoundingMode;
        this.chargeDecimalRegex = chargeDecimalRegex;
        this.gstDecimal = gstDecimal;
        this.gstRoundingMode = gstRoundingMode;
        this.gstDecimalRegex = gstDecimalRegex;
        this.gstSlabLimitValue = gstSlabLimitValue;
        this.isGstSlabEnabled=isGstSlabEnabled;
        this.gstSlabLimitApplyForOption=gstSlabLimitApplyForOption;
        this.gstSlabLimitOperatorOption=gstSlabLimitOperatorOption;
        this.roundingModes = roundingModes;
        this.isDefaultLoanCharge = isDefault;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ChargeData)) {
            return false;
        }
        final ChargeData chargeData = (ChargeData) obj;
        return this.id.equals(chargeData.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public int compareTo(final ChargeData obj) {
        if (obj == null) {
            return -1;
        }

        return obj.id.compareTo(this.id);
    }

    public LoanChargeData toLoanChargeData() {

        BigDecimal percentage = null;
        if (this.chargeCalculationType.getId() == 2) {
            percentage = this.amount;
        }

        return LoanChargeData.newLoanChargeDetails(this.id, this.name, this.currency, this.amount, percentage, this.chargeTimeType,
                this.chargeCalculationType, this.penalty, this.chargePaymentMode, this.minCap, this.maxCap);
    }

    public SavingsAccountChargeData toSavingsAccountChargeData() {

        final Long savingsChargeId = null;
        final Long savingsAccountId = null;
        final BigDecimal amountPaid = BigDecimal.ZERO;
        final BigDecimal amountWaived = BigDecimal.ZERO;
        final BigDecimal amountWrittenOff = BigDecimal.ZERO;
        final BigDecimal amountOutstanding = BigDecimal.ZERO;
        final BigDecimal percentage = BigDecimal.ZERO;
        final BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        final Collection<ChargeData> chargeOptions = null;
        final LocalDate dueAsOfDate = null;
        final Boolean isActive = null;
        final Boolean isFreeWithdrawal = null;
        final Integer freeWithdrawalChargeFrequency = null;
        final Integer restartFrequency = null;
        final Integer restartFrequencyEnum = null;

        final LocalDate inactivationDate = null;

        return SavingsAccountChargeData.instance(savingsChargeId, this.id, savingsAccountId, this.name, this.currency, this.amount,
                amountPaid, amountWaived, amountWrittenOff, amountOutstanding, this.chargeTimeType, dueAsOfDate, this.chargeCalculationType,
                percentage, amountPercentageAppliedTo, chargeOptions, this.penalty, this.feeOnMonthDay, this.feeInterval, isActive,
                isFreeWithdrawal, freeWithdrawalChargeFrequency, restartFrequency, restartFrequencyEnum, inactivationDate);
    }

    public ShareAccountChargeData toShareAccountChargeData() {

        final Long shareChargeId = null;
        final Long shareAccountId = null;
        final BigDecimal amountPaid = BigDecimal.ZERO;
        final BigDecimal amountWaived = BigDecimal.ZERO;
        final BigDecimal amountWrittenOff = BigDecimal.ZERO;
        final BigDecimal amountOutstanding = BigDecimal.ZERO;
        final BigDecimal percentage = BigDecimal.ZERO;
        final BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        final Collection<ChargeData> chargeOptions = null;
        final Boolean isActive = null;
        final BigDecimal chargeAmountOrPercentage = BigDecimal.ZERO;

        return new ShareAccountChargeData(shareChargeId, this.id, shareAccountId, this.name, this.currency, this.amount, amountPaid,
                amountWaived, amountWrittenOff, amountOutstanding, this.chargeTimeType, this.chargeCalculationType, percentage,
                amountPercentageAppliedTo, chargeOptions, isActive, chargeAmountOrPercentage);
    }

    public boolean isPenalty() {
        return this.penalty;
    }

    public boolean isOverdueInstallmentCharge() {
        boolean isOverdueInstallmentCharge = false;
        if (this.chargeTimeType != null) {
            isOverdueInstallmentCharge = ChargeTimeType.fromInt(this.chargeTimeType.getId().intValue()).isOverdueInstallment();
        }
        return isOverdueInstallmentCharge;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EnumOptionData getChargeCalculationType() {
        return chargeCalculationType;
    }

    public EnumOptionData getChargeTimeType() {
        return chargeTimeType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public CurrencyData getCurrency() {
        return currency;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFreeWithdrawal() {
        return freeWithdrawal;
    }

    public Integer getFreeWithdrawalChargeFrequency() {
        return freeWithdrawalChargeFrequency;
    }

    public Integer getRestartFrequency() {
        return restartFrequency;
    }

    public Integer getRestartFrequencyEnum() {
        return restartFrequencyEnum;
    }

    public boolean isPaymentType() {
        return isPaymentType;
    }

    public PaymentTypeData getPaymentTypeOptions() {
        return paymentTypeOptions;
    }

    public EnumOptionData getChargeAppliesTo() {
        return chargeAppliesTo;
    }

    public EnumOptionData getChargePaymentMode() {
        return chargePaymentMode;
    }

    public MonthDay getFeeOnMonthDay() {
        return feeOnMonthDay;
    }

    public Integer getFeeInterval() {
        return feeInterval;
    }

    public BigDecimal getMinCap() {
        return minCap;
    }

    public BigDecimal getMaxCap() {
        return maxCap;
    }

    public EnumOptionData getFeeFrequency() {
        return feeFrequency;
    }

    public GLAccountData getIncomeOrLiabilityAccount() {
        return incomeOrLiabilityAccount;
    }

    public TaxGroupData getTaxGroup() {
        return taxGroup;
    }

    public Collection<CurrencyData> getCurrencyOptions() {
        return currencyOptions;
    }

    public List<EnumOptionData> getChargeCalculationTypeOptions() {
        return chargeCalculationTypeOptions;
    }

    public List<EnumOptionData> getChargeAppliesToOptions() {
        return chargeAppliesToOptions;
    }

    public List<EnumOptionData> getChargeTimeTypeOptions() {
        return chargeTimeTypeOptions;
    }

    public List<EnumOptionData> getChargePaymetModeOptions() {
        return chargePaymetModeOptions;
    }

    public List<EnumOptionData> getLoanChargeCalculationTypeOptions() {
        return loanChargeCalculationTypeOptions;
    }

    public List<EnumOptionData> getLoanChargeTimeTypeOptions() {
        return loanChargeTimeTypeOptions;
    }

    public List<EnumOptionData> getSavingsChargeCalculationTypeOptions() {
        return savingsChargeCalculationTypeOptions;
    }

    public List<EnumOptionData> getSavingsChargeTimeTypeOptions() {
        return savingsChargeTimeTypeOptions;
    }

    public List<EnumOptionData> getClientChargeCalculationTypeOptions() {
        return clientChargeCalculationTypeOptions;
    }

    public List<EnumOptionData> getClientChargeTimeTypeOptions() {
        return clientChargeTimeTypeOptions;
    }

    public List<EnumOptionData> getShareChargeCalculationTypeOptions() {
        return shareChargeCalculationTypeOptions;
    }

    public List<EnumOptionData> getShareChargeTimeTypeOptions() {
        return shareChargeTimeTypeOptions;
    }

    public List<EnumOptionData> getFeeFrequencyOptions() {
        return feeFrequencyOptions;
    }

    public Map<String, List<GLAccountData>> getIncomeOrLiabilityAccountOptions() {
        return incomeOrLiabilityAccountOptions;
    }

    public Collection<TaxGroupData> getTaxGroupOptions() {
        return taxGroupOptions;
    }

    public String getAccountMappingForChargeConfig() {
        return accountMappingForChargeConfig;
    }

    public List<GLAccountData> getExpenseAccountOptions() {
        return expenseAccountOptions;
    }

    public List<GLAccountData> getAssetAccountOptions() {
        return assetAccountOptions;
    }

    public List<CodeValueData> getTypeOption() {
        return typeOption;
    }

    public List<CodeValueData> getFeesOption() {
        return feesOption;
    }

    public Boolean getEnableGstCharges() {
        return enableGstCharges;
    }

    public List<EnumOptionData> getGstOption() {
        return gstOption;
    }

    public Boolean getEnableGstChargesSelected() {
        return enableGstChargesSelected;
    }

    public EnumOptionData getGstSelected() {
        return gstSelected;
    }
    public Boolean getIsGstSlabEnabledSelected() {
        return isGstSlabEnabledSelected;
    }

    public EnumOptionData getGstSlabLimitApplyForSelected() {
        return gstSlabLimitApplyForSelected;
    }
    public EnumOptionData getGstSlabLimitOperatorSelected() {
        return gstSlabLimitOperatorSelected;
    }

    public BigDecimal getMinChargeAmount() {
        return minChargeAmount;
    }

    public BigDecimal getMaxChargeAmount() {
        return maxChargeAmount;
    }

    public String getType() {
        return type;
    }

    public String getFeesChargeType() {
        return feesChargeType;
    }

    public CodeValueData getTypeSelected() {
        return typeSelected;
    }

    public CodeValueData getFeesChargeTypeSelected() {
        return feesChargeTypeSelected;
    }

    public List<EnumOptionData> getDaysInYearTypeOptions() {
        return daysInYearTypeOptions;
    }

    public EnumOptionData getPenaltyInterestDaysInYearSelected() {
        return penaltyInterestDaysInYearSelected;
    }

    public Integer getChargeDecimal() {
        return chargeDecimal;
    }

    public String getChargeRoundingMode() {
        return chargeRoundingMode;
    }

    public Integer getChargeDecimalRegex() {
        return chargeDecimalRegex;
    }

    public Integer getGstDecimal() {
        return gstDecimal;
    }

    public String getGstRoundingMode() {
        return gstRoundingMode;
    }

    public Integer getGstDecimalRegex() {
        return gstDecimalRegex;
    }

    public BigDecimal getGstSlabLimitValue() {
        return gstSlabLimitValue;
    }
    public Boolean getIsGstSlabEnabled() {
        return isGstSlabEnabled;
    }

    public List<EnumOptionData> getGstSlabLimitApplyForOption() {
        return gstSlabLimitApplyForOption;
    }
    public List<EnumOptionData> getGstSlabLimitOperatorOption() {
        return gstSlabLimitOperatorOption;
    }
}
