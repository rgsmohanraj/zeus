package org.vcpl.lms.portfolio.charge.data;


import org.jetbrains.annotations.NotNull;
import org.vcpl.lms.accounting.glaccount.data.GLAccountData;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.portfolio.paymenttype.data.PaymentTypeData;
import org.vcpl.lms.portfolio.tax.data.TaxGroupData;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ColendingChargeData implements Comparable<ColendingChargeData>, Serializable {

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
    private final BigDecimal selfShare;
    private final BigDecimal partnerShare;

    public static ColendingChargeData template(final Long id,final String name, final boolean active,
                                               final boolean penalty,final boolean freeWithdrawal,final Integer freeWithdrawalChargeFrequency,final Integer restartFrequency,
                                               final Integer restartFrequencyEnum,final boolean isPaymentType,final PaymentTypeData paymentTypeOptions,final CurrencyData currency,
                                               final BigDecimal amount,final EnumOptionData chargeTimeType,final EnumOptionData chargeAppliesTo,final EnumOptionData chargeCalculationType,
                                               final EnumOptionData chargePaymentMode,final MonthDay feeOnMonthDay,final Integer feeInterval,final BigDecimal minCap,final BigDecimal maxCap,
                                               final EnumOptionData feeFrequency,final GLAccountData incomeOrLiabilityAccount,final TaxGroupData taxGroup,final Collection<CurrencyData> currencyOptions,
                                               final List<EnumOptionData> chargeCalculationTypeOptions,final List<EnumOptionData> chargeAppliesToOptions,final List<EnumOptionData> chargeTimeTypeOptions,
                                               final List<EnumOptionData> chargePaymetModeOptions,final List<EnumOptionData> loanChargeCalculationTypeOptions,final List<EnumOptionData> loanChargeTimeTypeOptions,
                                               final List<EnumOptionData> savingsChargeCalculationTypeOptions,final List<EnumOptionData> savingsChargeTimeTypeOptions,final List<EnumOptionData> clientChargeCalculationTypeOptions,
                                               final List<EnumOptionData> clientChargeTimeTypeOptions,final List<EnumOptionData> shareChargeCalculationTypeOptions,final List<EnumOptionData> shareChargeTimeTypeOptions,
                                               final List<EnumOptionData> feeFrequencyOptions,final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions,final Collection<TaxGroupData> taxGroupOptions,
                                               final String accountMappingForChargeConfig,final List<GLAccountData> expenseAccountOptions,final List<GLAccountData> assetAccountOptions,final List<CodeValueData> typeOption,final BigDecimal selfShare, final BigDecimal partnerShare
) {
        return new ColendingChargeData(id,name,active,penalty,freeWithdrawal,freeWithdrawalChargeFrequency,restartFrequency,restartFrequencyEnum,isPaymentType,paymentTypeOptions,currency, amount,chargeTimeType,chargeAppliesTo,chargeCalculationType, chargePaymentMode,feeOnMonthDay,feeInterval,minCap,maxCap,
                feeFrequency,incomeOrLiabilityAccount,taxGroup,currencyOptions,chargeCalculationTypeOptions,chargeAppliesToOptions,chargeTimeTypeOptions,
       chargePaymetModeOptions,loanChargeCalculationTypeOptions,loanChargeTimeTypeOptions,savingsChargeCalculationTypeOptions,savingsChargeTimeTypeOptions,clientChargeCalculationTypeOptions,
        clientChargeTimeTypeOptions,shareChargeCalculationTypeOptions,shareChargeTimeTypeOptions, feeFrequencyOptions,incomeOrLiabilityAccountOptions,taxGroupOptions,accountMappingForChargeConfig,
                expenseAccountOptions, assetAccountOptions, typeOption,selfShare,partnerShare);
    }

    public ColendingChargeData(final Long id, final String name, final boolean active,
                               final boolean penalty, final boolean freeWithdrawal, final Integer freeWithdrawalChargeFrequency, final Integer restartFrequency,
                               final Integer restartFrequencyEnum, final boolean isPaymentType, final PaymentTypeData paymentTypeOptions, final CurrencyData currency,
                               final BigDecimal amount, final EnumOptionData chargeTimeType, final EnumOptionData chargeAppliesTo, final EnumOptionData chargeCalculationType,
                               final EnumOptionData chargePaymentMode, final MonthDay feeOnMonthDay, final Integer feeInterval, final BigDecimal minCap, final BigDecimal maxCap,
                               final EnumOptionData feeFrequency, final GLAccountData incomeOrLiabilityAccount, final TaxGroupData taxGroup, final Collection<CurrencyData> currencyOptions,
                               final List<EnumOptionData> chargeCalculationTypeOptions, final List<EnumOptionData> chargeAppliesToOptions, final List<EnumOptionData> chargeTimeTypeOptions,
                               final List<EnumOptionData> chargePaymetModeOptions, final List<EnumOptionData> loanChargeCalculationTypeOptions, final List<EnumOptionData> loanChargeTimeTypeOptions,
                               final List<EnumOptionData> savingsChargeCalculationTypeOptions, final List<EnumOptionData> savingsChargeTimeTypeOptions, final List<EnumOptionData> clientChargeCalculationTypeOptions,
                               final List<EnumOptionData> clientChargeTimeTypeOptions, final List<EnumOptionData> shareChargeCalculationTypeOptions, final List<EnumOptionData> shareChargeTimeTypeOptions,
                               final List<EnumOptionData> feeFrequencyOptions, final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions, final Collection<TaxGroupData> taxGroupOptions,
                               final String accountMappingForChargeConfig, final List<GLAccountData> expenseAccountOptions, final List<GLAccountData> assetAccountOptions,
                               final List<CodeValueData> typeOption, final BigDecimal selfShare ,final BigDecimal partnerShare) {
        this.id=id;
        this.name=name;
        this.active=active;
        this.penalty=penalty;
        this.freeWithdrawal=freeWithdrawal;
        this.freeWithdrawalChargeFrequency=freeWithdrawalChargeFrequency;
        this.restartFrequency=restartFrequency;
        this.restartFrequencyEnum=restartFrequencyEnum;
        this.isPaymentType=isPaymentType;
        this.paymentTypeOptions=paymentTypeOptions;
        this.currency=currency;
        this.amount=amount;
        this.chargeTimeType=chargeTimeType;
        this.chargeAppliesTo=chargeAppliesTo;
        this.chargeCalculationType=chargeCalculationType;
        this.chargePaymentMode=chargePaymentMode;
        this.feeOnMonthDay=feeOnMonthDay;
        this.feeInterval=feeInterval;
        this.minCap=minCap;
        this.maxCap=maxCap;
        this.feeFrequency=feeFrequency;
        this.incomeOrLiabilityAccount=incomeOrLiabilityAccount;
        this.taxGroup=taxGroup;
        this.currencyOptions=currencyOptions;
        this.chargeCalculationTypeOptions=chargeCalculationTypeOptions;
        this.chargeAppliesToOptions=chargeAppliesToOptions;
        this.chargeTimeTypeOptions=chargeTimeTypeOptions;
        this.chargePaymetModeOptions=chargePaymetModeOptions;
        this.loanChargeCalculationTypeOptions=loanChargeCalculationTypeOptions;
        this.loanChargeTimeTypeOptions=loanChargeTimeTypeOptions;
        this.savingsChargeCalculationTypeOptions=savingsChargeCalculationTypeOptions;
        this.savingsChargeTimeTypeOptions=savingsChargeTimeTypeOptions;
        this.clientChargeCalculationTypeOptions=clientChargeCalculationTypeOptions;
        this.clientChargeTimeTypeOptions=clientChargeTimeTypeOptions;
        this.shareChargeCalculationTypeOptions=shareChargeCalculationTypeOptions;
        this.shareChargeTimeTypeOptions=shareChargeTimeTypeOptions;
        this.feeFrequencyOptions=feeFrequencyOptions;
        this.incomeOrLiabilityAccountOptions=incomeOrLiabilityAccountOptions;
        this.taxGroupOptions=taxGroupOptions;
        this.accountMappingForChargeConfig=accountMappingForChargeConfig;
        this.expenseAccountOptions=expenseAccountOptions;
        this.assetAccountOptions=assetAccountOptions;
        this.typeOption=typeOption;
        this.selfShare=selfShare;
        this.partnerShare=partnerShare;

    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPenalty() {
        return penalty;
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

    public CurrencyData getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public EnumOptionData getChargeTimeType() {
        return chargeTimeType;
    }

    public EnumOptionData getChargeAppliesTo() {
        return chargeAppliesTo;
    }

    public EnumOptionData getChargeCalculationType() {
        return chargeCalculationType;
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

    public BigDecimal getSelfShare() {
        return selfShare;
    }

    public BigDecimal getPartnerShare() {
        return partnerShare;
    }

    @Override
    public int compareTo(@NotNull ColendingChargeData obj) {
        if(obj == null){
            return -1;
        }
        return obj.id.compareTo(this.id);
    }
}



