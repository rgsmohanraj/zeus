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
package org.vcpl.lms.portfolio.charge.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.MonthDay;
import java.util.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.vcpl.lms.accounting.glaccount.data.GLAccountData;
import org.vcpl.lms.accounting.glaccount.domain.GLAccount;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.portfolio.charge.api.ChargesApiConstants;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.charge.exception.ChargeDueAtDisbursementCannotBePenaltyException;
import org.vcpl.lms.portfolio.charge.exception.ChargeMustBePenaltyException;
import org.vcpl.lms.portfolio.charge.exception.ChargeParameterUpdateNotSupportedException;
import org.vcpl.lms.portfolio.charge.service.ChargeEnumerations;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.paymenttype.data.PaymentTypeData;
import org.vcpl.lms.portfolio.paymenttype.domain.PaymentType;
import org.vcpl.lms.portfolio.tax.data.TaxGroupData;
import org.vcpl.lms.portfolio.tax.domain.TaxGroup;
import org.vcpl.lms.useradministration.domain.AppUser;

@Entity
@Setter
@Getter
@ToString
@Table(name = "m_charge", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name") })
public class Charge extends AbstractPersistableCustom {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "charge_applies_to_enum", nullable = false)
    private Integer chargeAppliesTo;

    @Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTimeType;

    @Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;

    @Column(name = "charge_payment_mode_enum", nullable = true)
    private Integer chargePaymentMode;

    @Column(name = "fee_on_day", nullable = true)
    private Integer feeOnDay;

    @Column(name = "fee_interval", nullable = true)
    private Integer feeInterval;

    @Column(name = "fee_on_month", nullable = true)
    private Integer feeOnMonth;

    @Column(name = "is_penalty", nullable = false)
    private boolean penalty;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "min_cap", scale = 6, precision = 19, nullable = true)
    private BigDecimal minCap;

    @Column(name = "max_cap", scale = 6, precision = 19, nullable = true)
    private BigDecimal maxCap;

    @Column(name = "fee_frequency", nullable = true)
    private Integer feeFrequency;

    @Column(name = "is_free_withdrawal", nullable = false)
    private boolean enableFreeWithdrawal;

    @Column(name = "free_withdrawal_charge_frequency", nullable = true)
    private Integer freeWithdrawalFrequency;

    @Column(name = "restart_frequency", nullable = true)
    private Integer restartFrequency;

    @Column(name = "restart_frequency_enum", nullable = true)
    private Integer restartFrequencyEnum;

    @Column(name = "is_payment_type", nullable = false)
    private boolean enablePaymentType;

    @ManyToOne
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentType paymentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_or_liability_account_id")
    private GLAccount account;

    @ManyToOne
    @JoinColumn(name = "tax_group_id")
    private TaxGroup taxGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_cv_id", nullable = true)
    private CodeValue type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fees_charge_type_cv_id", nullable = true)
    private CodeValue feesChargeType;

    @Column(name = "min_charge_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal minChargeAmount;

    @Column(name = "max_charge_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal maxChargeAmount;

    @Column(name="gst_enabled",nullable = true)
    private Boolean enableGstCharges;

    @Column(name="gst_enum",nullable = true)
    private Integer gst;

    @Column(name = "charge_type", length = 3)
    private String types;

    @Column(name = "fees_type", length = 30)
    private String feesChargeTypes;

    @Column(name = "penalty_interest_days_in_year")
    private Integer penaltyInterestDaysInYear;

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

    @Column(name="charge_decimal",nullable = true)
    private Integer chargeDecimal;

    @Column(name="charge_rounding_mode",nullable = true)
    private String chargeRoundingMode;

    @Column(name="charge_decimal_regex",nullable = true)
    private Integer chargeDecimalRegex;

    @Column(name="gst_decimal",nullable = true)
    private Integer gstDecimal;

    @Column(name="gst_rounding_mode",nullable = true)
    private String gstRoundingMode;

    @Column(name="gst_decimal_regex",nullable = true)
    private Integer gstDecimalRegex;

    @Column(name="is_gst_slab_enabled",nullable = true)
    private Boolean isGstSlabEnabled;

    @Column(name="gst_slab_limit_apply_for",nullable = true)
    private Integer gstSlabLimitApplyFor;

    @Column(name="gst_slab_limit_operator",nullable = true)
    private Integer gstSlabLimitOperator;

    @Column(name="gst_slab_limit_value",nullable = true)
    private BigDecimal gstSlabLimitValue;

    @Column(name="is_default_loan_charge")
    private Boolean isDefaultLoanCharge;

    public String getTypes() {
        return types;
    }

    public String getFeesChargeTypes() {
        return feesChargeTypes;
    }

    public Integer getGst() {
        return gst;
    }

    public Integer getChargeDecimal() {
        return chargeDecimal;
    }

    public RoundingMode getChargeRoundingMode() {
        return RoundingMode.valueOf(chargeRoundingMode);
    }

    public Integer getChargeDecimalRegex() {
        return chargeDecimalRegex;
    }

    public Integer getGstDecimal() {
        return gstDecimal;
    }

    public RoundingMode getGstRoundingMode() {
        return RoundingMode.valueOf(gstRoundingMode);
    }

    public Integer getGstDecimalRegex() {
        return gstDecimalRegex;
    }

    public Boolean getGstSlabEnabled() {
        return isGstSlabEnabled;
    }

    public Integer getGstSlabLimitApplyFor() {
        return gstSlabLimitApplyFor;
    }

    public Integer getGstSlabLimitOperator() {
        return gstSlabLimitOperator;
    }

    public BigDecimal getGstSlabLimitValue() {
        return gstSlabLimitValue;
    }

    public BigDecimal getMinChargeAmount() {
        return minChargeAmount;
    }

    public BigDecimal getMaxChargeAmount() {
        return maxChargeAmount;
    }


    public static Charge fromJson(final JsonCommand command, final GLAccount account, final TaxGroup taxGroup,
            final PaymentType paymentType,final CodeValue type,final CodeValue feesChargeType) {


//        final GstEnum gst = GstEnum.fromInt(command.integerValueOfParameterNamed("gst"));



            final String name = command.stringValueOfParameterNamed("name");
            final BigDecimal amount = command.bigDecimalValueOfParameterNamed("amount");
            final BigDecimal minAmount = command.bigDecimalValueOfParameterNamed("minAmount");
            final BigDecimal maxAmount = command.bigDecimalValueOfParameterNamed("maxAmount");
            final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
            final Integer chargeDecimal = command.integerValueOfParameterNamed("chargeDecimal");
            final String chargeRoundingMode = command.stringValueOfParameterNamed("chargeRoundingMode");
            final Integer chargeDecimalRegex = command.integerValueOfParameterNamed("chargeDecimalRegex");
            final Boolean isGstSlabEnabled = command.booleanPrimitiveValueOfParameterNamed("isGstSlabEnabled");
            final boolean enableGstCharges = command.booleanPrimitiveValueOfParameterNamed("enableGstCharges");
            GstEnum gst=null;
            Integer gstDecimal = null;
            String gstRoundingMode = null;
            Integer gstDecimalRegex = null;
            BigDecimal gstSlabLimitValue = null;
            GstSlabLimitApplyFor gstSlabLimitApplyFor = null;
            GstSlabLimitOperator gstSlabLimitOperator = null;
            if(enableGstCharges ==true) {
                gst = GstEnum.fromInt(command.integerValueOfParameterNamed("gst"));
                gstDecimal = command.integerValueOfParameterNamed("gstDecimal");
                gstRoundingMode = command.stringValueOfParameterNamed("gstRoundingMode");
                gstDecimalRegex = command.integerValueOfParameterNamed("gstDecimalRegex");
                if(isGstSlabEnabled == true) {
                    gstSlabLimitApplyFor = GstSlabLimitApplyFor.fromInt(command.integerValueOfParameterNamed("gstSlabLimitApplyFor"));
                    gstSlabLimitOperator = GstSlabLimitOperator.fromInt(command.integerValueOfParameterNamed("gstSlabLimitOperator"));
                    gstSlabLimitValue = command.bigDecimalValueOfParameterNamed("gstSlabLimitValue");
                }
            }else{
                  gst=GstEnum.INVALID;

            }



            final ChargeAppliesTo chargeAppliesTo = ChargeAppliesTo.fromInt(command.integerValueOfParameterNamed("chargeAppliesTo"));
            final ChargeTimeType chargeTimeType = ChargeTimeType.fromInt(command.integerValueOfParameterNamed("chargeTimeType"));
            final ChargeCalculationType chargeCalculationType = ChargeCalculationType
                    .fromInt(command.integerValueOfParameterNamed("chargeCalculationType"));
            final Integer chargePaymentMode = command.integerValueOfParameterNamed("chargePaymentMode");

            final ChargePaymentMode paymentMode = chargePaymentMode == null ? null : ChargePaymentMode.fromInt(chargePaymentMode);

            final boolean penalty = command.booleanPrimitiveValueOfParameterNamed("penalty");
            final boolean active = command.booleanPrimitiveValueOfParameterNamed("active");
            final MonthDay feeOnMonthDay = command.extractMonthDayNamed("feeOnMonthDay");
            final Integer feeInterval = command.integerValueOfParameterNamed("feeInterval");
            final BigDecimal minCap = command.bigDecimalValueOfParameterNamed("minCap");
            final BigDecimal maxCap = command.bigDecimalValueOfParameterNamed("maxCap");
            final Integer feeFrequency = command.integerValueOfParameterNamed("feeFrequency");

            boolean enableFreeWithdrawalCharge = false;
            enableFreeWithdrawalCharge = command.booleanPrimitiveValueOfParameterNamed("enableFreeWithdrawalCharge");

            boolean enablePaymentType = false;
            enablePaymentType = command.booleanPrimitiveValueOfParameterNamed("enablePaymentType");

            Integer freeWithdrawalFrequency = null;
            Integer restartCountFrequency = null;
            PeriodFrequencyType countFrequencyType = null;

            if (enableFreeWithdrawalCharge) {
                freeWithdrawalFrequency = command.integerValueOfParameterNamed("freeWithdrawalFrequency");
                restartCountFrequency = command.integerValueOfParameterNamed("restartCountFrequency");

                countFrequencyType = PeriodFrequencyType.fromInt(command.integerValueOfParameterNamed("countFrequencyType"));
            }

            Integer penaltyInterestDaysInYear = command.integerValueOfParameterNamed(ChargesApiConstants.PENALTY_INTEGER_DAYS_IN_YEARS);
        final boolean isDefaultLoanCharge = command.booleanPrimitiveValueOfParameterNamed("isDefaultLoanCharge");
            return new Charge(name, amount, currencyCode, chargeAppliesTo, chargeTimeType, chargeCalculationType, penalty, active, paymentMode,
                    feeOnMonthDay, feeInterval, minCap, maxCap, feeFrequency, enableFreeWithdrawalCharge, freeWithdrawalFrequency,
                    restartCountFrequency, countFrequencyType, account, taxGroup, enablePaymentType, paymentType, type, enableGstCharges,
                    gst.getValue(),minAmount,maxAmount,feesChargeType, penaltyInterestDaysInYear,chargeDecimal,chargeRoundingMode,chargeDecimalRegex,
                    gstDecimal,gstRoundingMode,gstDecimalRegex,isGstSlabEnabled,Objects.nonNull(gstSlabLimitApplyFor)?gstSlabLimitApplyFor.getValue():Integer.valueOf(0),Objects.nonNull(gstSlabLimitOperator)?gstSlabLimitOperator.getValue():Integer.valueOf(0),Objects.nonNull(gstSlabLimitValue)?gstSlabLimitValue:BigDecimal.ZERO,isDefaultLoanCharge);


    }

    protected Charge() {}



    private Charge(final String name, final BigDecimal amount, final String currencyCode, final ChargeAppliesTo chargeAppliesTo,
                   final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculationType, final boolean penalty, final boolean active,
                   final ChargePaymentMode paymentMode, final MonthDay feeOnMonthDay, final Integer feeInterval, final BigDecimal minCap,
                   final BigDecimal maxCap, final Integer feeFrequency, final boolean enableFreeWithdrawalCharge,
                   final Integer freeWithdrawalFrequency, final Integer restartFrequency, final PeriodFrequencyType restartFrequencyEnum,
                   final GLAccount account, final TaxGroup taxGroup, final boolean enablePaymentType, final PaymentType paymentType,
                   final CodeValue type, final Boolean enableGstCharges, final Integer gst, final BigDecimal minChargeAmount,
                   final BigDecimal maxChargeAmount, final CodeValue feesChargeType, final Integer penaltyInterestDaysInYear,final Integer chargeDecimal,final String chargeRoundingMode,final Integer chargeDecimalRegex,
                   final Integer gstDecimal,final String gstRoundingMode,final Integer gstDecimalRegex,final Boolean isGstSlabEnabled,final Integer gstSlabLimitApplyFor,final Integer gstSlabLimitOperator,final BigDecimal gstSlabLimitValue,final Boolean isDefault) {
        this.type = type;
        this.feesChargeType = feesChargeType;
        this.name = name;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.chargeAppliesTo = chargeAppliesTo.getValue();
        this.chargeTimeType = chargeTime.getValue();
        this.chargeCalculation = chargeCalculationType.getValue();
        this.penalty = penalty;
        this.active = active;
        this.account = account;
        this.taxGroup = taxGroup;
        this.chargePaymentMode = paymentMode == null ? null : paymentMode.getValue();
        this.enableGstCharges=enableGstCharges;
        this.gst=gst;
        this.chargeDecimal = chargeDecimal;
        this.chargeRoundingMode = chargeRoundingMode;
        this.chargeDecimalRegex = chargeDecimalRegex;
        this.gstDecimal = gstDecimal;
        this.gstRoundingMode = gstRoundingMode;
        this.gstDecimalRegex = gstDecimalRegex;
        this.isGstSlabEnabled = isGstSlabEnabled;
        this.gstSlabLimitApplyFor = gstSlabLimitApplyFor;
        this.gstSlabLimitOperator = gstSlabLimitOperator;
        this.gstSlabLimitValue = gstSlabLimitValue;
        this.minChargeAmount=minChargeAmount;
        this.maxChargeAmount=maxChargeAmount;
        this.types=type.getLabel();
        if(feesChargeType != null){
            this.feesChargeTypes=feesChargeType.getLabel();

        }else{

            this.feesChargeTypes="";
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charges");

        if (isMonthlyFee() || isAnnualFee()) {
            this.feeOnMonth = feeOnMonthDay.getMonthValue();
            this.feeOnDay = feeOnMonthDay.getDayOfMonth();
        }
        this.feeInterval = feeInterval;
        this.feeFrequency = feeFrequency;

        if (isSavingsCharge()) {
            // TODO vishwas, this validation seems unnecessary as identical
            // validation is performed in the write service
            if (!isAllowedSavingsChargeTime()) {
                baseDataValidator.reset().parameter("chargeTimeType").value(this.chargeTimeType)
                        .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.time.for.savings");
            }
            // TODO vishwas, this validation seems unnecessary as identical
            // validation is performed in the writeservice
            if (!isAllowedSavingsChargeCalculationType()) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(this.chargeCalculation)
                        .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.calculation.type.for.savings");
            }

            if (!(ChargeTimeType.fromInt(getChargeTimeType()).isWithdrawalFee()
                    || ChargeTimeType.fromInt(getChargeTimeType()).isSavingsNoActivityFee())
                    && ChargeCalculationType.fromInt(getChargeCalculation()).isPercentageOfAmount()) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(this.chargeCalculation)
                        .failWithCodeNoParameterAddedToErrorCode(
                                "savings.charge.calculation.type.percentage.allowed.only.for.withdrawal.or.NoActivity");
            }

            if (enableFreeWithdrawalCharge) {
                this.enableFreeWithdrawal = true;
                this.freeWithdrawalFrequency = freeWithdrawalFrequency;
                this.restartFrequency = restartFrequency;
                this.restartFrequencyEnum = restartFrequencyEnum.getValue();
            }

            if (enablePaymentType) {
                if (paymentType != null) {

                    this.enablePaymentType = true;
                    this.paymentType = paymentType;
                }
            }

        } else if (isLoanCharge()) {

            if (penalty && (chargeTime.isTimeOfDisbursement() || chargeTime.isTrancheDisbursement())) {
                throw new ChargeDueAtDisbursementCannotBePenaltyException(name);
            }
            if (!penalty && chargeTime.isOverdueInstallment()) {
                throw new ChargeMustBePenaltyException(name);
            }
            // TODO vishwas, this validation seems unnecessary as identical
            // validation is performed in the write service
            if (!isAllowedLoanChargeTime()) {
                baseDataValidator.reset().parameter("chargeTimeType").value(this.chargeTimeType)
                        .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.time.for.loan");
            }
        }
        if(enableGstCharges)
        {
            this.enableGstCharges=enableGstCharges;
        }

        if (isPercentageOfApprovedAmount()) {
            this.minCap = minCap;
            this.maxCap = maxCap;
        }

        this.penaltyInterestDaysInYear = penaltyInterestDaysInYear;
        this.isDefaultLoanCharge=isDefault;

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public String getName() {
        return this.name;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public Integer getChargeTimeType() {
        return this.chargeTimeType;
    }

    public CodeValue getType() {
        return type;
    }

    public CodeValue getFeesChargeType() {
        return feesChargeType;
    }

    public Integer getChargeCalculation() {
        return this.chargeCalculation;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isPenalty() {
        return this.penalty;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public boolean isLoanCharge() {
        return ChargeAppliesTo.fromInt(this.chargeAppliesTo).isLoanCharge();
    }

    public boolean isAllowedLoanChargeTime() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isAllowedLoanChargeTime();
    }

    public boolean isAllowedClientChargeTime() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isAllowedClientChargeTime();
    }

    public boolean isSavingsCharge() {
        return ChargeAppliesTo.fromInt(this.chargeAppliesTo).isSavingsCharge();
    }

    public boolean isClientCharge() {
        return ChargeAppliesTo.fromInt(this.chargeAppliesTo).isClientCharge();
    }

    public boolean isAllowedSavingsChargeTime() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isAllowedSavingsChargeTime();
    }

    public boolean isAllowedSavingsChargeCalculationType() {
        return ChargeCalculationType.fromInt(this.chargeCalculation).isAllowedSavingsChargeCalculationType();
    }

    public boolean isAllowedClientChargeCalculationType() {
        return ChargeCalculationType.fromInt(this.chargeCalculation).isAllowedClientChargeCalculationType();
    }

    public boolean isPercentageOfApprovedAmount() {
        return ChargeCalculationType.fromInt(this.chargeCalculation).isPercentageOfAmount();
    }

    public boolean isPercentageOfDisbursementAmount() {
        return ChargeCalculationType.fromInt(this.chargeCalculation).isPercentageOfDisbursementAmount();
    }

    public BigDecimal getMinCap() {
        return this.minCap;
    }

    public BigDecimal getMaxCap() {
        return this.maxCap;
    }

    public boolean isEnableFreeWithdrawal() {
        return this.enableFreeWithdrawal;
    }

    public boolean isEnablePaymentType() {
        return this.enablePaymentType;
    }

    public Integer getFrequencyFreeWithdrawalCharge() {
        return this.freeWithdrawalFrequency;
    }

    public Integer getRestartFrequency() {
        return this.restartFrequency;
    }

    public Integer getRestartFrequencyEnum() {
        return this.restartFrequencyEnum;
    }

    public PaymentType getPaymentType() {
        return this.paymentType;
    }

    public Integer getChargeAppliesTo() {
        return chargeAppliesTo;
    }

    public Integer getFeeOnDay() {
        return feeOnDay;
    }



    public Integer getFeeOnMonth() {
        return feeOnMonth;
    }

    public Integer getFeeFrequency() {
        return feeFrequency;
    }

    public Integer getFreeWithdrawalFrequency() {
        return freeWithdrawalFrequency;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }
    public Boolean getEnablegstCharges() {
        return enableGstCharges;
    }

    public void setEnableGstCharges(Boolean enableGstCharges) {
        this.enableGstCharges = enableGstCharges;
    }


    private Long getPaymentTypeId() {
        Long paymentTypeId = null;
        if (this.paymentType != null) {
            paymentTypeId = this.paymentType.getId();
        }
        return paymentTypeId;
    }

    public Integer getPenaltyInterestDaysInYear() {
        return penaltyInterestDaysInYear;
    }

    public void setPenaltyInterestDaysInYear(Integer penaltyInterestDaysInYear) {
        this.penaltyInterestDaysInYear = penaltyInterestDaysInYear;
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String localeAsInput = command.locale();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charges");
        final String isDefaultParamName = "isDefaultLoanCharge";
        if(command.isChangeInBooleanParameterNamed(isDefaultParamName,this.isDefaultLoanCharge))
        {
            this.isDefaultLoanCharge=command.booleanObjectValueOfParameterNamed(isDefaultParamName);
            actualChanges.put(isDefaultParamName, this.isDefaultLoanCharge);
        }
        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }


        final String currencyCodeParamName = "currencyCode";
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, this.currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            this.currencyCode = newValue;
        }

        if (command.isChangeInLongParameterNamed(ChargesApiConstants.typeSelected, chargeTypeId())) {
            final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.typeSelected);
            actualChanges.put(ChargesApiConstants.typeSelected, newValue);
        }

        if (command.isChangeInLongParameterNamed(ChargesApiConstants.feesChargeTypeSelected, feesTypeId())) {
            final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.feesChargeTypeSelected);
            actualChanges.put(ChargesApiConstants.feesChargeTypeSelected, newValue);
        }

        final String amountParamName = "amount";
        if (command.isChangeInBigDecimalParameterNamed(amountParamName, this.amount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName);
            actualChanges.put(amountParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.amount = newValue;
        }


        final String minAmount = "minChargeAmount";
        if (command.isChangeInBigDecimalParameterNamed(minAmount, this.minChargeAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minAmount);
            actualChanges.put(minAmount, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minChargeAmount = newValue;
        }


        final String maxAmount = "maxChargeAmount";
        if (command.isChangeInBigDecimalParameterNamed(maxAmount, this.maxChargeAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxAmount);
            actualChanges.put(maxAmount, newValue);
            actualChanges.put("locale", localeAsInput);
            this.maxChargeAmount = newValue;
        }

        final String chargeTimeParamName = "chargeTimeType";
        if (command.isChangeInIntegerParameterNamed(chargeTimeParamName, this.chargeTimeType)) {
            final Integer newValue = command.integerValueOfParameterNamed(chargeTimeParamName);
            actualChanges.put(chargeTimeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.chargeTimeType = ChargeTimeType.fromInt(newValue).getValue();

            if (isSavingsCharge()) {
                if (!isAllowedSavingsChargeTime()) {
                    baseDataValidator.reset().parameter("chargeTimeType").value(this.chargeTimeType)
                            .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.time.for.savings");
                }
                // if charge time is changed to monthly then validate for
                // feeOnMonthDay and feeInterval
                if (isMonthlyFee()) {
                    final MonthDay monthDay = command.extractMonthDayNamed("feeOnMonthDay");
                    baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).notNull();

                    final Integer feeInterval = command.integerValueOfParameterNamed("feeInterval");
                    baseDataValidator.reset().parameter("feeInterval").value(feeInterval).notNull().inMinMaxRange(1, 12);
                }
            } else if (isLoanCharge()) {
                if (!isAllowedLoanChargeTime()) {
                    baseDataValidator.reset().parameter("chargeTimeType").value(this.chargeTimeType)
                            .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.time.for.loan");
                }
            } else if (isClientCharge()) {
                if (!isAllowedLoanChargeTime()) {
                    baseDataValidator.reset().parameter("chargeTimeType").value(this.chargeTimeType)
                            .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.time.for.client");
                }
            }
        }
        final String gstParamName  = "gstOption";
        if (command.isChangeInIntegerParameterNamed(gstParamName, this.gst)) {
            final Integer newValue = command.integerValueOfParameterNamed(gstParamName);
            actualChanges.put(gstParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.gst = newValue;
        }

        final String chargeDecimalParamName = "chargeDecimal";
        if (command.isChangeInIntegerParameterNamed(chargeDecimalParamName, this.chargeDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(chargeDecimalParamName);
            actualChanges.put(chargeDecimalParamName, newValue);
            this.chargeDecimal = newValue;
        }

        final String chargeRoundingModeParamName = "chargeRoundingMode";
        if (command.isChangeInStringParameterNamed(chargeRoundingModeParamName, this.chargeRoundingMode)) {
            final String newValue = command.stringValueOfParameterNamed(chargeRoundingModeParamName);
            actualChanges.put(chargeRoundingModeParamName, newValue);
            this.chargeRoundingMode = newValue;
        }

        final String chargeDecimalRegexParamName = "chargeDecimalRegex";
        if (command.isChangeInIntegerParameterNamed(chargeDecimalRegexParamName, this.chargeDecimalRegex)) {
            final Integer newValue = command.integerValueOfParameterNamed(chargeDecimalRegexParamName);
            actualChanges.put(chargeDecimalRegexParamName, newValue);
            this.chargeDecimalRegex = newValue;
        }

        final String gstDecimalParamName = "gstDecimal";
        if (command.isChangeInIntegerParameterNamed(gstDecimalParamName, this.gstDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(gstDecimalParamName);
            actualChanges.put(gstDecimalParamName, newValue);
            this.gstDecimal = newValue;
        }

        final String gstRoundingModeParamName = "gstRoundingMode";
        if (command.isChangeInStringParameterNamed(gstRoundingModeParamName, this.gstRoundingMode)) {
            final String newValue = command.stringValueOfParameterNamed(gstRoundingModeParamName);
            actualChanges.put(gstRoundingModeParamName, newValue);
            this.gstRoundingMode = newValue;
        }

        final String gstDecimalRegexParamName = "gstDecimalRegex";
        if (command.isChangeInIntegerParameterNamed(gstDecimalRegexParamName, this.gstDecimalRegex)) {
            final Integer newValue = command.integerValueOfParameterNamed(gstDecimalRegexParamName);
            actualChanges.put(gstDecimalRegexParamName, newValue);
            this.gstDecimalRegex = newValue;
        }

        final String gstSlabLimitApplyForParamName  = "gstSlabLimitApplyFor";
        if (command.isChangeInIntegerParameterNamed(gstSlabLimitApplyForParamName, this.gstSlabLimitApplyFor)) {
            final Integer newValue = command.integerValueOfParameterNamed(gstSlabLimitApplyForParamName);
            actualChanges.put(gstSlabLimitApplyForParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.gstSlabLimitApplyFor = newValue;
        }

        final String gstSlabLimitOperatorParamName  = "gstSlabLimitOperator";
        if (command.isChangeInIntegerParameterNamed(gstSlabLimitOperatorParamName, this.gstSlabLimitOperator)) {
            final Integer newValue = command.integerValueOfParameterNamed(gstSlabLimitOperatorParamName);
            actualChanges.put(gstSlabLimitOperatorParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.gstSlabLimitOperator = newValue;
        }

        final String gstSlabLimitValueParamName = "gstSlabLimitValue";
        if (command.isChangeInBigDecimalParameterNamed(gstSlabLimitValueParamName, this.gstSlabLimitValue)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(gstSlabLimitValueParamName);
            actualChanges.put(gstSlabLimitValueParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.gstSlabLimitValue = newValue;
        }

        final String isGstSlabEnabledParamName = "isGstSlabEnabled";
        if (command.isChangeInBooleanParameterNamed(isGstSlabEnabledParamName, this.isGstSlabEnabled)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(isGstSlabEnabledParamName);
            actualChanges.put(isGstSlabEnabledParamName, newValue);
            this.isGstSlabEnabled = newValue;
        }

        final String enableGstChargesParamName = "enableGstChargesSelected";
        if (command.isChangeInBooleanParameterNamed(enableGstChargesParamName, this.enableGstCharges)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(enableGstChargesParamName);
            actualChanges.put(enableGstChargesParamName, newValue);
            this.enableGstCharges = newValue;
        }

        final String freeWithdrawalFrequencyParamName = "freeWithdrawalFrequency";
        if (command.isChangeInIntegerParameterNamed(freeWithdrawalFrequencyParamName, this.freeWithdrawalFrequency)) {
            final Integer enableFreeWithdrawalChargeNewValue = command.integerValueOfParameterNamed(freeWithdrawalFrequencyParamName);
            actualChanges.put(freeWithdrawalFrequencyParamName, enableFreeWithdrawalChargeNewValue);
            this.freeWithdrawalFrequency = enableFreeWithdrawalChargeNewValue;
        }

        final String restartCountFrequencyParamName = "restartCountFrequency";
        if (command.isChangeInIntegerParameterNamed(restartCountFrequencyParamName, this.restartFrequency)) {
            final Integer restartCountFrequencyNewValue = command.integerValueOfParameterNamed(restartCountFrequencyParamName);
            actualChanges.put(restartCountFrequencyParamName, restartCountFrequencyNewValue);
            this.restartFrequency = restartCountFrequencyNewValue;
        }

        final String countFrequencyTypeParamName = "countFrequencyType";
        if (command.isChangeInIntegerParameterNamed(countFrequencyTypeParamName, this.restartFrequencyEnum)) {
            final Integer countFrequencyTypeNewValue = command.integerValueOfParameterNamed(countFrequencyTypeParamName);
            actualChanges.put(countFrequencyTypeParamName, countFrequencyTypeNewValue);
            this.restartFrequencyEnum = ChargeTimeType.fromInt(countFrequencyTypeNewValue).getValue();
        }

        final String enableFreeWithdrawalChargeParamName = "enableFreeWithdrawalCharge";
        if (command.isChangeInBooleanParameterNamed(enableFreeWithdrawalChargeParamName, this.enableFreeWithdrawal)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(enableFreeWithdrawalChargeParamName);
            actualChanges.put(enableFreeWithdrawalChargeParamName, newValue);
            this.enableFreeWithdrawal = newValue;

        }

        final String enablePaymentTypeParamName = "enablePaymentType";
        if (command.isChangeInBooleanParameterNamed(enablePaymentTypeParamName, this.enablePaymentType)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(enablePaymentTypeParamName);
            actualChanges.put(enablePaymentTypeParamName, newValue);
            this.enablePaymentType = newValue;
        }

        final String paymentTypeParamName = "paymentTypeId";
        if (command.isChangeInLongParameterNamed(paymentTypeParamName, getPaymentTypeId())) {
            final Long newValue = command.longValueOfParameterNamed(paymentTypeParamName);
            actualChanges.put(paymentTypeParamName, newValue);
        }

        final String chargeAppliesToParamName = "chargeAppliesTo";
        if (command.isChangeInIntegerParameterNamed(chargeAppliesToParamName, this.chargeAppliesTo)) {
            /*
             * final Integer newValue = command.integerValueOfParameterNamed(chargeAppliesToParamName);
             * actualChanges.put(chargeAppliesToParamName, newValue); actualChanges.put("locale", localeAsInput);
             * this.chargeAppliesTo = ChargeAppliesTo.fromInt(newValue).getValue();
             */

            // AA: Do not allow to change chargeAppliesTo.
            final String errorMessage = "Update of Charge applies to is not supported";
            throw new ChargeParameterUpdateNotSupportedException("charge.applies.to", errorMessage);
        }

        final String chargeCalculationParamName = "chargeCalculationType";
        if (command.isChangeInIntegerParameterNamed(chargeCalculationParamName, this.chargeCalculation)) {
            final Integer newValue = command.integerValueOfParameterNamed(chargeCalculationParamName);
            actualChanges.put(chargeCalculationParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.chargeCalculation = ChargeCalculationType.fromInt(newValue).getValue();

            if (isSavingsCharge()) {
                if (!isAllowedSavingsChargeCalculationType()) {
                    baseDataValidator.reset().parameter("chargeCalculationType").value(this.chargeCalculation)
                            .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.calculation.type.for.savings");
                }

                if (!(ChargeTimeType.fromInt(getChargeTimeType()).isWithdrawalFee()
                        || ChargeTimeType.fromInt(getChargeTimeType()).isSavingsNoActivityFee())
                        && ChargeCalculationType.fromInt(getChargeCalculation()).isPercentageOfAmount()) {
                    baseDataValidator.reset().parameter("chargeCalculationType").value(this.chargeCalculation)
                            .failWithCodeNoParameterAddedToErrorCode(
                                    "charge.calculation.type.percentage.allowed.only.for.withdrawal.or.noactivity");
                }
            } else if (isClientCharge()) {
                if (!isAllowedClientChargeCalculationType()) {
                    baseDataValidator.reset().parameter("chargeCalculationType").value(this.chargeCalculation)
                            .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.calculation.type.for.client");
                }
            }
        }

        // validate only for loan charge
        if (isLoanCharge()) {
            final String paymentModeParamName = "chargePaymentMode";
            if (command.isChangeInIntegerParameterNamed(paymentModeParamName, this.chargePaymentMode)) {
                final Integer newValue = command.integerValueOfParameterNamed(paymentModeParamName);
                actualChanges.put(paymentModeParamName, newValue);
                actualChanges.put("locale", localeAsInput);
                this.chargePaymentMode = ChargePaymentMode.fromInt(newValue).getValue();
            }
        }

        if (command.hasParameter("feeOnMonthDay")) {
            final MonthDay monthDay = command.extractMonthDayNamed("feeOnMonthDay");
            final String actualValueEntered = command.stringValueOfParameterNamed("feeOnMonthDay");
            final Integer dayOfMonthValue = monthDay.getDayOfMonth();
            if (!this.feeOnDay.equals(dayOfMonthValue)) {
                actualChanges.put("feeOnMonthDay", actualValueEntered);
                actualChanges.put("locale", localeAsInput);
                this.feeOnDay = dayOfMonthValue;
            }

            final Integer monthOfYear = monthDay.getMonthValue();
            if (!this.feeOnMonth.equals(monthOfYear)) {
                actualChanges.put("feeOnMonthDay", actualValueEntered);
                actualChanges.put("locale", localeAsInput);
                this.feeOnMonth = monthOfYear;
            }
        }

        final String feeInterval = "feeInterval";
        if (command.isChangeInIntegerParameterNamed(feeInterval, this.feeInterval)) {
            final Integer newValue = command.integerValueOfParameterNamed(feeInterval);
            actualChanges.put(feeInterval, newValue);
            actualChanges.put("locale", localeAsInput);
            this.feeInterval = newValue;
        }

        final String feeFrequency = "feeFrequency";
        if (command.isChangeInIntegerParameterNamed(feeFrequency, this.feeFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(feeFrequency);
            actualChanges.put(feeFrequency, newValue);
            actualChanges.put("locale", localeAsInput);
            this.feeFrequency = newValue;
        }

        if (this.feeFrequency != null) {
            baseDataValidator.reset().parameter("feeInterval").value(this.feeInterval).notNull();
        }

        final String penaltyParamName = "penalty";
        if (command.isChangeInBooleanParameterNamed(penaltyParamName, this.penalty)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(penaltyParamName);
            actualChanges.put(penaltyParamName, newValue);
            this.penalty = newValue;
        }

        final String activeParamName = "active";
        if (command.isChangeInBooleanParameterNamed(activeParamName, this.active)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(activeParamName);
            actualChanges.put(activeParamName, newValue);
            this.active = newValue;
        }

        // allow min and max cap to be only added to PERCENT_OF_AMOUNT for now
        if (isPercentageOfApprovedAmount()) {
            final String minCapParamName = "minCap";
            if (command.isChangeInBigDecimalParameterNamed(minCapParamName, this.minCap)) {
                final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minCapParamName);
                actualChanges.put(minCapParamName, newValue);
                actualChanges.put("locale", localeAsInput);
                this.minCap = newValue;
            }
            final String maxCapParamName = "maxCap";
            if (command.isChangeInBigDecimalParameterNamed(maxCapParamName, this.maxCap)) {
                final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxCapParamName);
                actualChanges.put(maxCapParamName, newValue);
                actualChanges.put("locale", localeAsInput);
                this.maxCap = newValue;
            }

        }

        if (this.penalty && ChargeTimeType.fromInt(this.chargeTimeType).isTimeOfDisbursement()) {
            throw new ChargeDueAtDisbursementCannotBePenaltyException(this.name);
        }
        if (!penalty && ChargeTimeType.fromInt(this.chargeTimeType).isOverdueInstallment()) {
            throw new ChargeMustBePenaltyException(name);
        }

        if (command.isChangeInLongParameterNamed(ChargesApiConstants.glAccountIdParamName, getIncomeAccountId())) {
            final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.glAccountIdParamName);
            actualChanges.put(ChargesApiConstants.glAccountIdParamName, newValue);
        }

        if (command.isChangeInLongParameterNamed(ChargesApiConstants.taxGroupIdParamName, getTaxGroupId())) {
            final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.taxGroupIdParamName);
            actualChanges.put(ChargesApiConstants.taxGroupIdParamName, newValue);
            if (taxGroup != null) {
                baseDataValidator.reset().parameter(ChargesApiConstants.taxGroupIdParamName).failWithCode("modification.not.supported");
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        return actualChanges;
    }

    private Long chargeTypeId() {
        Long chargeTypeId = null;
        if (this.type != null) {
            chargeTypeId = this.type.getId();
        }
        return chargeTypeId;
    }

    private Long feesTypeId() {

        Long feesTypeId = null;
        if (this.feesChargeType != null) {
            feesTypeId = this.feesChargeType.getId();
        }
        return feesTypeId;
    }

    /**
     * Delete is a <i>soft delete</i>. Updates flag on charge so it wont appear in query/report results.
     *
     * Any fields with unique constraints and prepended with id of record.
     */
    public void delete() {
        this.deleted = true;
        this.name = getId() + "_" + this.name;
    }

    public ChargeData toData() {
        final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(this.chargeTimeType);
        final EnumOptionData chargeAppliesTo = ChargeEnumerations.chargeAppliesTo(this.chargeAppliesTo);
        final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(this.chargeCalculation);
        final EnumOptionData chargePaymentmode = ChargeEnumerations.chargePaymentMode(this.chargePaymentMode);
        final EnumOptionData feeFrequencyType = ChargeEnumerations.chargePaymentMode(this.feeFrequency);
        final EnumOptionData penaltyInterestDaysInYear = ChargeEnumerations.chargePaymentMode(this.penaltyInterestDaysInYear);

        GLAccountData accountData = null;
        if (account != null) {
            accountData = new GLAccountData(account.getId(), account.getName(), account.getGlCode());
        }
        TaxGroupData taxGroupData = null;
        if (this.taxGroup != null) {
            taxGroupData = TaxGroupData.lookup(taxGroup.getId(), taxGroup.getName());
        }

        PaymentTypeData paymentTypeData = null;
        if (this.paymentType != null) {
            paymentTypeData = PaymentTypeData.instance(paymentType.getId(), paymentType.getPaymentName());
        }

        final CurrencyData currency = new CurrencyData(this.currencyCode, null, 0, 0, null, null);
        return ChargeData.instance(getId(), this.name, this.amount, currency, chargeTimeType, chargeAppliesTo, chargeCalculationType,
                chargePaymentmode, getFeeOnMonthDay(), this.feeInterval, this.penalty, this.active, this.enableFreeWithdrawal,
                this.freeWithdrawalFrequency, this.restartFrequency, this.restartFrequencyEnum, this.enablePaymentType, paymentTypeData,
                this.minCap, this.maxCap, feeFrequencyType, accountData, taxGroupData,null,null,null,null,null,null,null,null, penaltyInterestDaysInYear,
                null,null,null,null,null,null,null,null,null,null,this.isDefaultLoanCharge);
    }

    public Integer getChargePaymentMode() {
        return this.chargePaymentMode;
    }

    public Integer getFeeInterval() {
        return this.feeInterval;
    }

    public boolean isMonthlyFee() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isMonthlyFee();
    }

    public boolean isAnnualFee() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isAnnualFee();
    }

    public boolean isOverdueInstallment() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isOverdueInstallment();
    }

    public boolean isForeclosureCharge() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isForeclosureCharge();
    }

    public boolean isBounceCharge() { return ChargeTimeType.fromInt(this.chargeTimeType).isBounceCharge(); }

    public MonthDay getFeeOnMonthDay() {
        MonthDay feeOnMonthDay = null;
        if (this.feeOnDay != null && this.feeOnMonth != null) {
            feeOnMonthDay = MonthDay.now(DateUtils.getDateTimeZoneOfTenant()).withMonth(this.feeOnMonth).withDayOfMonth(this.feeOnDay);
        }
        return feeOnMonthDay;
    }

    public Integer feeInterval() {
        return this.feeInterval;
    }

    public Integer feeFrequency() {
        return this.feeFrequency;
    }

    public GLAccount getAccount() {
        return this.account;
    }

    public void setAccount(GLAccount account) {
        this.account = account;
    }

    public Long getIncomeAccountId() {
        Long incomeAccountId = null;
        if (this.account != null) {
            incomeAccountId = this.account.getId();
        }
        return incomeAccountId;
    }

    private Long getTaxGroupId() {
        Long taxGroupId = null;
        if (this.taxGroup != null) {
            taxGroupId = this.taxGroup.getId();
        }
        return taxGroupId;
    }

    public boolean isDisbursementCharge() {
        return ChargeTimeType.fromInt(this.chargeTimeType).equals(ChargeTimeType.DISBURSEMENT)
                || ChargeTimeType.fromInt(this.chargeTimeType).equals(ChargeTimeType.TRANCHE_DISBURSEMENT);
    }

    public TaxGroup getTaxGroup() {
        return this.taxGroup;
    }

    public void setTaxGroup(TaxGroup taxGroup) {
        this.taxGroup = taxGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Charge)) {
            return false;
        }
        Charge other = (Charge) o;
        return Objects.equals(name, other.name) && Objects.equals(amount, other.amount) && Objects.equals(currencyCode, other.currencyCode)
                && Objects.equals(chargeAppliesTo, other.chargeAppliesTo) && Objects.equals(chargeTimeType, other.chargeTimeType)
                && Objects.equals(chargeCalculation, other.chargeCalculation) && Objects.equals(chargePaymentMode, other.chargePaymentMode)
                && Objects.equals(feeOnDay, other.feeOnDay) && Objects.equals(feeInterval, other.feeInterval)
                && Objects.equals(feeOnMonth, other.feeOnMonth) && penalty == other.penalty && active == other.active
                && deleted == other.deleted && Objects.equals(minCap, other.minCap) && Objects.equals(maxCap, other.maxCap)
                && Objects.equals(feeFrequency, other.feeFrequency) && Objects.equals(account, other.account)
                && Objects.equals(taxGroup, other.taxGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount, currencyCode, chargeAppliesTo, chargeTimeType, chargeCalculation, chargePaymentMode, feeOnDay,
                feeInterval, feeOnMonth, penalty, active, deleted, minCap, maxCap, feeFrequency, account, taxGroup);
    }

    public boolean enabelGst() {
            return this.enableGstCharges;
        }

    public void updateChargeId(CodeValue codeValue) {
         this.type=codeValue;
         this.types=codeValue.getLabel();
    }

    public void updateFeesId(CodeValue codeValue) {
        this.feesChargeType=codeValue;
        this.feesChargeTypes=codeValue.getLabel();
    }

    public boolean isFlatAmount() {

        return ChargeCalculationType.fromInt(this.chargeCalculation).isFlat();

    }

    public boolean isPercentageOfAmount(Integer chargeCalculation) {
        return ChargeCalculationType.fromInt(chargeCalculation).isPercentageOfAmount();
    }


    public Boolean getDefault() {
        return isDefaultLoanCharge;
    }

    public void setDefault(Boolean aDefault) {
        isDefaultLoanCharge = aDefault;
    }

    public boolean isAdhocCharge() {
        return chargeTimeType.equals(18);
    }

}

