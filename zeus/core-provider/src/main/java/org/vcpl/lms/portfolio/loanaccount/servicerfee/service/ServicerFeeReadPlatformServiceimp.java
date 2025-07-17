package org.vcpl.lms.portfolio.loanaccount.servicerfee.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.vcpl.lms.accounting.glaccount.data.GLAccountData;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.core.domain.JdbcSupport;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.charge.service.ChargeEnumerations;
import org.vcpl.lms.portfolio.common.service.CommonEnumerations;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.Enum.ServicerFeeChargesRatio;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.RetrieveServicerFeeChargeData;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.ServicerFeeData;
import org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations;
import org.vcpl.lms.portfolio.paymenttype.data.PaymentTypeData;
import org.vcpl.lms.portfolio.tax.data.TaxGroupData;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.MonthDay;
import java.util.List;

@Service
non-sealed public class ServicerFeeReadPlatformServiceimp implements ServicerFeeReadPlatformService {
    private final JdbcTemplate jdbcTemplate;

    public ServicerFeeReadPlatformServiceimp(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<RetrieveServicerFeeChargeData> retrieveServicerFee(Long productId) {
        ServicerFeeChargeMapper servicerFeeChargeMapper = new ServicerFeeChargeMapper();

        String sql = """
                select sfc.charge_id as ChargeId, sfc.sf_self_share_charge as sfSelfShareCharge , sfc.sf_partner_share_charge as sfPartnerShareCharge,
                sfc.sf_charge_amt_gst_loss_enabled as sfChargeAmtGstLossEnabled, sfc.sf_charge_amt_gst_loss as sfChargeAmtGstLoss ,sfc.is_active as isActive,c.id as id,
                c.name as name, c.amount as amount,  c.min_charge_amount as minChargeAmount,c.max_charge_amount as maxChargeAmount,  c.charge_type as type,
                c.fees_type as feesChargeType, c.currency_code as currencyCode, c.charge_applies_to_enum as chargeAppliesTo,c.charge_time_enum as chargeTime, c.charge_payment_mode_enum as chargePaymentMode,
                c.type_cv_id as chargeTypeCode, c.fees_charge_type_cv_id as feesTypeCode,c.charge_calculation_enum as chargeCalculation,c.is_penalty as penalty,
                c.gst_enabled as enableGstCharges,  c.gst_enum as gstenum,c.is_active as active, c.is_free_withdrawal as isFreeWithdrawal, c.free_withdrawal_charge_frequency as freeWithdrawalChargeFrequency,
                c.charge_decimal as chargeDecimal,c.charge_rounding_mode as chargeRoundingMode,c.charge_decimal_regex as chargeDecimalRegex,c.gst_decimal as gstDecimal,
                c.gst_rounding_mode as gstRoundingMode,c.gst_decimal_regex as gstDecimalRegex,c.gst_slab_limit_value as gstSlabLimitValue,c.is_gst_slab_enabled as isGstSlabEnabled,c.gst_slab_limit_apply_for as gstSlabLimitApplyFor,c.gst_slab_limit_operator as gstSlabLimitOperator,
                c.restart_frequency as restartFrequency, c.restart_frequency_enum as restartFrequencyEnum,oc.name as currencyName, oc.decimal_places as currencyDecimalPlaces,
                oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol,oc.internationalized_name_code as currencyNameCode, c.fee_on_day as feeOnDay, c.fee_on_month as feeOnMonth,
                c.fee_interval as feeInterval, c.fee_frequency as feeFrequency,c.min_cap as minCap,c.max_cap as maxCap,c.income_or_liability_account_id as glAccountId , acc.name as glAccountName, acc.gl_code as glCode,
                tg.id as taxGroupId, c.is_payment_type as isPaymentType, pt.id as paymentTypeId, pt.value as paymentTypeName, tg.name as taxGroupName,
                cvf.code_value as chargeTypeValue,cvft.code_value as feesTypeValue, c.penalty_interest_days_in_year as  penaltyInterestDaysInYear,sfc.servicer_fee_charges_ratio as servicerFeeChargesRatio
                FROM m_servicer_fee_charges_config sfc
                LEFT JOIN m_charge c on c.id = sfc.charge_id
                join m_organisation_currency oc on c.currency_code = oc.code
                LEFT JOIN acc_gl_account acc on acc.id = c.income_or_liability_account_id
                LEFT JOIN m_code_value cvf on cvf.id= c.type_cv_id
                LEFT JOIN m_payment_type pt on pt.id = c.payment_type_id
                LEFT JOIN m_code_value cvft on cvf.id= c.fees_charge_type_cv_id
                LEFT JOIN m_tax_group tg on tg.id = c.tax_group_id
                where servicer_fee_config_id = ? 
                """;
        return this.jdbcTemplate.query(sql,servicerFeeChargeMapper,new Object[]{productId});
    }

    private static final class   ServicerFeeChargeMapper implements RowMapper<RetrieveServicerFeeChargeData> {

        @Override
        public RetrieveServicerFeeChargeData mapRow(ResultSet rs, int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final BigDecimal amount = rs.getBigDecimal("amount");


            final BigDecimal minChargeAmount = rs.getBigDecimal("minChargeAmount");
            final BigDecimal maxChargeAmount = rs.getBigDecimal("maxChargeAmount");
            final String type = rs.getString("type");
            final String feesChargeType = rs.getString("feesChargeType");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final int chargeAppliesTo = rs.getInt("chargeAppliesTo");
            final EnumOptionData chargeAppliesToType = ChargeEnumerations.chargeAppliesTo(chargeAppliesTo);

            final int chargeTime = rs.getInt("chargeTime");
            final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);

            final int chargeCalculation = rs.getInt("chargeCalculation");
            final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);

            final Long chargeTypeId= JdbcSupport.getLong(rs,"chargeTypeCode");
            final String chargeTypeValue = rs.getString("chargeTypeValue");
            final CodeValueData typeSelected = CodeValueData.instance(chargeTypeId, chargeTypeValue);

            final Long feesTypeId= JdbcSupport.getLong(rs,"feesTypeCode");
            final String feesTypeValue = rs.getString("feesTypeValue");
            final CodeValueData feesChargeTypeSelected = CodeValueData.instance(feesTypeId, feesTypeValue);

            final int paymentMode = rs.getInt("chargePaymentMode");
            final EnumOptionData chargePaymentMode = ChargeEnumerations.chargePaymentMode(paymentMode);

            final boolean penalty = rs.getBoolean("penalty");
            final boolean active = rs.getBoolean("active");

            final boolean enableGstCharges=rs.getBoolean("enableGstCharges");
// final int gst=rs.getInt("gst");
// final EnumOptionData gstId= ChargeEnumerations.gst(gst);

            EnumOptionData gstEnumType = null;
            final Integer gstEnum=rs.getInt("gstenum");
            if (gstEnum != null) {
                gstEnumType = CommonEnumerations.gst(gstEnum);
            }
            final Integer chargeDecimal = rs.getInt("chargeDecimal");
            final String chargeRoundingMode = rs.getString("chargeRoundingMode");
            final Integer chargeDecimalRegex = rs.getInt("chargeDecimalRegex");
            final Integer gstDecimal = rs.getInt("gstDecimal");
            final String gstRoundingMode = rs.getString("gstRoundingMode");
            final Integer gstDecimalRegex = rs.getInt("gstDecimalRegex");
            final BigDecimal gstSlabLimitValue = rs.getBigDecimal("gstSlabLimitValue");
            final boolean isGstSlabEnabled=rs.getBoolean("isGstSlabEnabled");
            EnumOptionData gstSlabLimitApplyForType = null;
            EnumOptionData gstSlabLimitOperatorType = null;
            final Integer gstSlabLimitApplyFor=rs.getInt("gstSlabLimitApplyFor");
            final Integer gstSlabLimitOperator=rs.getInt("gstSlabLimitOperator");
            if (gstSlabLimitApplyFor != null) {
                gstSlabLimitApplyForType = CommonEnumerations.gstSlabLimitApplyFor(gstSlabLimitApplyFor);
            }
            if (gstSlabLimitOperator != null) {
                gstSlabLimitOperatorType = CommonEnumerations.gstSlabLimitOperator(gstSlabLimitOperator);
            }


            final Integer feeInterval = JdbcSupport.getInteger(rs, "feeInterval");
            EnumOptionData feeFrequencyType = null;
            final Integer feeFrequency = JdbcSupport.getInteger(rs, "feeFrequency");
            if (feeFrequency != null) {
                feeFrequencyType = CommonEnumerations.termFrequencyType(feeFrequency, "feeFrequency");
            }
            MonthDay feeOnMonthDay = null;
            final Integer feeOnMonth = JdbcSupport.getInteger(rs, "feeOnMonth");
            final Integer feeOnDay = JdbcSupport.getInteger(rs, "feeOnDay");
            if (feeOnDay != null && feeOnMonth != null) {
                feeOnMonthDay = MonthDay.now(DateUtils.getDateTimeZoneOfTenant()).withDayOfMonth(feeOnDay).withMonth(feeOnMonth);
            }
            final BigDecimal minCap = rs.getBigDecimal("minCap");
            final BigDecimal maxCap = rs.getBigDecimal("maxCap");

// extract GL Account
            final Long glAccountId = JdbcSupport.getLong(rs, "glAccountId");
            final String glAccountName = rs.getString("glAccountName");
            final String glCode = rs.getString("glCode");
            GLAccountData glAccountData = null;
            if (glAccountId != null) {
                glAccountData = new GLAccountData(glAccountId, glAccountName, glCode);
            }

            final Long taxGroupId = JdbcSupport.getLong(rs, "taxGroupId");
            final String taxGroupName = rs.getString("taxGroupName");
            TaxGroupData taxGroupData = null;
            if (taxGroupId != null) {
                taxGroupData = TaxGroupData.lookup(taxGroupId, taxGroupName);
            }

            final boolean isFreeWithdrawal = rs.getBoolean("isFreeWithdrawal");
            final int freeWithdrawalChargeFrequency = rs.getInt("freeWithdrawalChargeFrequency");
            final int restartFrequency = rs.getInt("restartFrequency");
            final int restartFrequencyEnum = rs.getInt("restartFrequencyEnum");

            final boolean isPaymentType = rs.getBoolean("isPaymentType");
            final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentTypeId");

            final String paymentTypeName = rs.getString("paymentTypeName");
            PaymentTypeData paymentTypeData = null;
            if (paymentTypeId != null) {
                paymentTypeData = PaymentTypeData.instance(paymentTypeId, paymentTypeName);
            }

            final Integer penaltyInterestDaysInYear = rs.getInt("penaltyInterestDaysInYear");
            EnumOptionData penaltyInterestDaysInYearSelected = CommonEnumerations.daysInYearType(penaltyInterestDaysInYear);

            ChargeData chargeData = ChargeData.instance(id, name, amount, currency, chargeTimeType, chargeAppliesToType, chargeCalculationType,
                    chargePaymentMode, feeOnMonthDay, feeInterval, penalty, active, isFreeWithdrawal, freeWithdrawalChargeFrequency,
                    restartFrequency, restartFrequencyEnum, isPaymentType, paymentTypeData, minCap, maxCap, feeFrequencyType, glAccountData,
                    taxGroupData,enableGstCharges,gstEnumType,minChargeAmount,maxChargeAmount,type,typeSelected,feesChargeTypeSelected,feesChargeType, penaltyInterestDaysInYearSelected,chargeDecimal,chargeRoundingMode,chargeDecimalRegex,gstDecimal,gstRoundingMode,gstDecimalRegex,gstSlabLimitValue,isGstSlabEnabled,gstSlabLimitApplyForType,gstSlabLimitOperatorType,false);

            BigDecimal sfSelfShareCharge = rs.getBigDecimal("sfSelfShareCharge");
            BigDecimal sfPartnerShareCharge = rs.getBigDecimal("sfPartnerShareCharge");
            boolean sfChargeAmtGstLossEnabled = rs.getBoolean("sfChargeAmtGstLossEnabled");
            BigDecimal sfChargeAmtGstLoss = rs.getBigDecimal("sfChargeAmtGstLoss");
            boolean isActive = rs.getBoolean("isActive");

            Integer servicerFeeChargesRatio = rs.getInt("servicerFeeChargesRatio");
            EnumOptionData servicerFeeChargesRatioSelected = LoanEnumerations.retrieveServiceFeeSplitMethod(ServicerFeeChargesRatio.getServicerFeeChargesRatio(servicerFeeChargesRatio));

            return new RetrieveServicerFeeChargeData(chargeData,sfSelfShareCharge,sfPartnerShareCharge,sfChargeAmtGstLossEnabled,sfChargeAmtGstLoss,isActive,servicerFeeChargesRatioSelected);
        }
    }

    private static  final class   ServicerFeeDataMapper implements RowMapper<ServicerFeeData> {

        @Override
        public ServicerFeeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            long productId = rs.getLong("productId");
            BigDecimal vclHurdleRate = rs.getBigDecimal("vclHurdleRate");
            String vclInterestRound = rs.getString("vclInterestRound");
            Integer vclInterestDecimal = rs.getInt("vclInterestDecimal");
            String servicerFeeRound = rs.getString("servicerFeeRound");
            Integer servicerFeeDecimal = rs.getInt("servicerFeeDecimal");
            boolean sfBaseAmtGstLossEnabled = rs.getBoolean("sfBaseAmtGstLossEnabled");
            BigDecimal sfBaseAmtGstLoss = rs.getBigDecimal("sfBAseAmtGst");
            BigDecimal sfGst = rs.getBigDecimal("sfGst");
            String sfGstRound = rs.getString("sfGstRound");
            Integer sfGstDecimal = rs.getInt("sfGstDecimal");
            BigDecimal sfChargegst = rs.getBigDecimal("sfChargegst");
            BigDecimal sfChargeDecimal = rs.getBigDecimal("sfChargeDecimal");
            String sfChargeRound = rs.getString("sfChargeRound");
            String sfChargeBaseAmountRoundingmode = rs.getString("sfChargeBaseAmountRoundingmode");
            Integer sfChargeBaseAmountDecimal = rs.getInt("sfChargeBaseAmountDecimal");
            String sfChargeGstRoundingmode = rs.getString("sfChargeGstRoundingmode");
            Integer sfChargeGstDecimal = rs.getInt("sfChargeGstDecimal");
            return new ServicerFeeData(id,productId,vclInterestRound,vclInterestDecimal,servicerFeeRound,
                    servicerFeeDecimal,sfBaseAmtGstLossEnabled,sfBaseAmtGstLoss,sfGst,sfGstRound,sfGstDecimal,vclHurdleRate,sfChargegst,sfChargeRound,sfChargeDecimal,sfChargeBaseAmountRoundingmode,
                    sfChargeBaseAmountDecimal, sfChargeGstRoundingmode,sfChargeGstDecimal);
        }

    }
    @Override
    public ServicerFeeData retrieveServicerFeeConfigData( Long ProductId) {

        ServicerFeeDataMapper servicerFeeChargeDataRowMapper = new ServicerFeeDataMapper();
        String sql= """
                 Select ms.id as id, ms.product_id as productId, ms.vcl_hurdle_rate as vclHurdleRate, ms.vcl_interest_round as vclInterestRound,
                ms.vcl_interest_decimal as vclInterestDecimal,  ms.servicer_fee_round as servicerFeeRound,  ms.servicer_fee_decimal as servicerFeeDecimal,
                ms.sf_base_amt_gst_loss_enabled as sfBaseAmtGstLossEnabled, ms.sf_base_amt_gst_loss as sfBAseAmtGst, ms.sf_gst as sfGst,ms.sf_gst_round as sfGstRound,
                ms.sf_gst_decimal as sfGstDecimal, msc.sf_charge_gst as sfChargegst, msc.sf_charge_round as sfChargeRound,
                msc.sf_charge_decimal as sfChargeDecimal,msc.sf_charge_base_amount_roundingmode as sfChargeBaseAmountRoundingmode,msc.sf_charge_base_amount_decimal as sfChargeBaseAmountDecimal,
                msc.sf_charge_gst_roundingmode as sfChargeGstRoundingmode,msc.sf_charge_gst_decimal as sfChargeGstDecimal
                from m_servicer_fee_config ms                       
                left join m_servicer_fee_charges_config msc on msc.servicer_fee_config_id = ms.id
                where ms.product_id =?
                group by ms.product_id
                """;

        return   this.jdbcTemplate.queryForObject(sql,servicerFeeChargeDataRowMapper,new Object[]{ProductId});



    }
}
