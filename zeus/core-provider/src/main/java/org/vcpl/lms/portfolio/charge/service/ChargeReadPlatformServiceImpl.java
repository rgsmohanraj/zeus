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
package org.vcpl.lms.portfolio.charge.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.MonthDay;
import java.util.*;

import org.vcpl.lms.accounting.common.AccountingDropdownReadPlatformService;
import org.vcpl.lms.accounting.glaccount.data.GLAccountData;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.infrastructure.configuration.domain.ConfigurationDomainServiceJpa;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.core.domain.JdbcSupport;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.entityaccess.domain.FineractEntityType;
import org.vcpl.lms.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.service.CurrencyReadPlatformService;
import org.vcpl.lms.portfolio.charge.api.ChargesApiConstants;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.charge.data.ColendingChargeData;
import org.vcpl.lms.portfolio.charge.domain.ChargeAppliesTo;
import org.vcpl.lms.portfolio.charge.domain.ChargeTimeType;
import org.vcpl.lms.portfolio.charge.exception.ChargeNotFoundException;
import org.vcpl.lms.portfolio.common.service.CommonEnumerations;
import org.vcpl.lms.portfolio.common.service.DropdownReadPlatformService;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesData;
import org.vcpl.lms.portfolio.paymenttype.data.PaymentTypeData;
import org.vcpl.lms.portfolio.tax.data.TaxGroupData;
import org.vcpl.lms.portfolio.tax.service.TaxReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author vishwas
 *
 */
@Service
public class ChargeReadPlatformServiceImpl implements ChargeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService;
    private final DropdownReadPlatformService dropdownReadPlatformService;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final TaxReadPlatformService taxReadPlatformService;
    private final ConfigurationDomainServiceJpa configurationDomainServiceJpa;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public ChargeReadPlatformServiceImpl(final CurrencyReadPlatformService currencyReadPlatformService,
            final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, final JdbcTemplate jdbcTemplate,
            final DropdownReadPlatformService dropdownReadPlatformService, final FineractEntityAccessUtil fineractEntityAccessUtil,
            final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService,
            final TaxReadPlatformService taxReadPlatformService, final ConfigurationDomainServiceJpa configurationDomainServiceJpa,
            final NamedParameterJdbcTemplate namedParameterJdbcTemplate,final CodeValueReadPlatformService codeReadPlatformService) {
        this.chargeDropdownReadPlatformService = chargeDropdownReadPlatformService;
        this.jdbcTemplate = jdbcTemplate;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.fineractEntityAccessUtil = fineractEntityAccessUtil;
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.taxReadPlatformService = taxReadPlatformService;
        this.configurationDomainServiceJpa = configurationDomainServiceJpa;
        this.codeValueReadPlatformService=codeReadPlatformService;
    }

    @Override
    @Cacheable(value = "charges", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public Collection<ChargeData> retrieveAllCharges() {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false  ";

        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        sql += " group by c.id order by c.name ";

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveAllChargesForCurrency(String currencyCode) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false and c.currency_code= ? ";

        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { currencyCode }); // NOSONAR
    }

    @Override
    public ChargeData retrieveCharge(final Long chargeId) {
        try {
            final ChargeMapper rm = new ChargeMapper();

            String sql = "select " + rm.chargeSchema() + " where c.id = ? and c.is_deleted=false ";

            sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

            sql = sql + " ;";
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { chargeId }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new ChargeNotFoundException(chargeId, e);
        }
    }

    @Override
    public ChargeData retrieveNewChargeDetails() {

        final Boolean enableGstCharges=false;

        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<EnumOptionData> allowedChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService.retrieveCalculationTypes();
        //final List<EnumOptionData> allowedChargeAppliesToOptions = this.chargeDropdownReadPlatformService.retrieveApplicableToTypes();
        final List<EnumOptionData> allowedChargeAppliesToOptions = this.chargeDropdownReadPlatformService.retrieveChargesType();
        final List<EnumOptionData> allowedChargeTimeOptions = this.chargeDropdownReadPlatformService.retrieveCollectionTimeTypes();
        final List<EnumOptionData> chargePaymentOptions = this.chargeDropdownReadPlatformService.retrivePaymentModes();
        final List<EnumOptionData> loansChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveLoanCalculationTypes();
        final List<EnumOptionData> loansChargeTimeTypeOptions = this.chargeDropdownReadPlatformService.retrieveLoanCollectionTimeTypes();
        final List<EnumOptionData> savingsChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveSavingsCalculationTypes();
        final List<EnumOptionData> savingsChargeTimeTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveSavingsCollectionTimeTypes();
        final List<EnumOptionData> clientChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveClientCalculationTypes();
      //  final Boolean gst=this.chargeDropdownReadPlatformService.retrieveGst();
        final List<EnumOptionData> clientChargeTimeTypeOptions = this.chargeDropdownReadPlatformService.retrieveClientCollectionTimeTypes();
        final List<EnumOptionData> feeFrequencyOptions = this.dropdownReadPlatformService.retrievePeriodFrequencyTypeOptions();
        final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForCharges();
        final List<EnumOptionData> shareChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveSharesCalculationTypes();
        final List<EnumOptionData> shareChargeTimeTypeOptions = this.chargeDropdownReadPlatformService.retrieveSharesCollectionTimeTypes();
        final Collection<TaxGroupData> taxGroupOptions = this.taxReadPlatformService.retrieveTaxGroupsForLookUp();
        final String accountMappingForChargeConfig = this.configurationDomainServiceJpa.getAccountMappingForCharge();
        final List<GLAccountData> expenseAccountOptions = this.accountingDropdownReadPlatformService.retrieveExpenseAccountOptions();
        final List<GLAccountData> assetAccountOptions = this.accountingDropdownReadPlatformService.retrieveAssetAccountOptions();
        final List<CodeValueData> typeOption = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ChargesApiConstants.TYPE));
        final List<CodeValueData> feesOption = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ChargesApiConstants.FEESCHARGETYPE));
        final List<EnumOptionData> gstOption=this.chargeDropdownReadPlatformService.retrieveGst();
        final Boolean isGstSlabEnabled=false;
        final List<EnumOptionData> gstSlabLimitApplyForOption = this.chargeDropdownReadPlatformService.retrieveGstSlabLimitApplyFor();
        final List<EnumOptionData> gstSlabLimitOperatorOption = this.chargeDropdownReadPlatformService.retrieveGstSlabLimitOperator();
        final List<EnumOptionData> penaltyInterestDaysInYearOption = this.chargeDropdownReadPlatformService.retrievePenaltyInterstDaysInYearOptions();
        final List<RoundingMode> roundingModes = dropdownReadPlatformService.retrieveRoundingMode();

        return ChargeData.template(currencyOptions, allowedChargeCalculationTypeOptions, allowedChargeAppliesToOptions,
                allowedChargeTimeOptions, chargePaymentOptions, loansChargeCalculationTypeOptions, loansChargeTimeTypeOptions,
                savingsChargeCalculationTypeOptions, savingsChargeTimeTypeOptions, clientChargeCalculationTypeOptions,
                clientChargeTimeTypeOptions, feeFrequencyOptions, incomeOrLiabilityAccountOptions, taxGroupOptions,
                shareChargeCalculationTypeOptions, shareChargeTimeTypeOptions, accountMappingForChargeConfig, expenseAccountOptions,
                assetAccountOptions,typeOption,enableGstCharges,gstOption,feesOption, penaltyInterestDaysInYearOption,isGstSlabEnabled,gstSlabLimitApplyForOption,gstSlabLimitOperatorOption,roundingModes,false);
    }

    @Override
    public Collection<ChargeData> retrieveLoanProductCharges(final Long loanProductId) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.loanProductChargeSchema() + " where c.is_deleted=false and c.is_active=true and plc.product_loan_id=? ";

        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { loanProductId }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveLoanProductCharges(final Long loanProductId, final ChargeTimeType chargeTime) {

        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.loanProductChargeSchema()
                + " where c.is_deleted=false and c.is_active=true and plc.product_loan_id=? and c.charge_time_enum=? ;";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { loanProductId, chargeTime.getValue() }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicableFees() {
        final ChargeMapper rm = new ChargeMapper();
        Object[] params = new Object[] { "Charge",ChargeAppliesTo.LOAN.getValue() };

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false and c.is_active=true and c.is_penalty=false and c.charge_type = ? and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, params); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicableForeclosure() {

        final ChargeMapper rm = new ChargeMapper();
        Object[] params = new Object[] { 17, ChargeAppliesTo.LOAN.getValue() };

        String sql = "select " + rm.chargeSchema()
                + " where c.is_deleted=false and c.is_active=true  and c.is_penalty=false and c.charge_time_enum = ? and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, params); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveLoanAccountApplicableCharges(final Long loanId, ChargeTimeType[] excludeChargeTimes) {
        final ChargeMapper rm = new ChargeMapper();
        StringBuilder excludeClause = new StringBuilder("");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("loanId", loanId);
        paramMap.put("chargeAppliesTo", ChargeAppliesTo.LOAN.getValue());
        processChargeExclusionsForLoans(excludeChargeTimes, excludeClause);
        String sql = "select " + rm.chargeSchema() + " join m_loan la on la.currency_code = c.currency_code" + " where la.id=:loanId"
                + " and c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=:chargeAppliesTo" + excludeClause + " ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";
        return this.namedParameterJdbcTemplate.query(sql, paramMap, rm);
    }

    /**
     * @param excludeChargeTimes
     * @param excludeClause
     * @param
     * @return
     */
    private void processChargeExclusionsForLoans(ChargeTimeType[] excludeChargeTimes, StringBuilder excludeClause) {
        if (excludeChargeTimes != null && excludeChargeTimes.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < excludeChargeTimes.length; i++) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(excludeChargeTimes[i].getValue());
            }
            excludeClause = excludeClause.append(" and c.charge_time_enum not in(" + sb.toString() + ") ");
            excludeClause.append(" ");
        }
    }

    @Override
    public Collection<ChargeData> retrieveLoanProductApplicableCharges(final Long loanProductId, ChargeTimeType[] excludeChargeTimes) {
        final ChargeMapper rm = new ChargeMapper();
        StringBuilder excludeClause = new StringBuilder("");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("productId", loanProductId);
        paramMap.put("chargeAppliesTo", ChargeAppliesTo.LOAN.getValue());
        processChargeExclusionsForLoans(excludeChargeTimes, excludeClause);
        String sql = "select " + rm.chargeSchema() + " join m_product_loan lp on lp.currency_code = c.currency_code"
                + " where lp.id=:productId" + " and c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=:chargeAppliesTo"
                + excludeClause + " ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.namedParameterJdbcTemplate.query(sql, paramMap, rm);
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicablePenalties() {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema()
                + " where c.is_deleted=false and c.is_active=true and c.is_penalty=true and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";
        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.LOAN.getValue() }); // NOSONAR
    }

    private String addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled() {

        String sql = "";

        // Check if branch specific products are enabled. If yes, fetch only
        // charges mapped to current user's office
        String inClause = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.LOANPRODUCTCHARGE);
        if ((inClause != null) && !inClause.trim().isEmpty()) {
            sql += " and c.id in ( " + inClause + " ) ";
        }

        return sql;
    }

    private static final class ChargeMapper implements RowMapper<ChargeData> {

        public String chargeSchema() {
            return "c.id as id, c.is_default_loan_charge as isDefaultLoanCharge ,c.name as name, c.amount as amount,c.min_charge_amount as minChargeAmount,c.max_charge_amount as maxChargeAmount,c.charge_type as type,c.fees_type as feesChargeType, c.currency_code as currencyCode, "
                    + "c.charge_applies_to_enum as chargeAppliesTo, c.charge_time_enum as chargeTime, "
                    + "c.charge_payment_mode_enum as chargePaymentMode,c.type_cv_id as chargeTypeCode,c.fees_charge_type_cv_id as feesTypeCode, "
                    + "c.charge_calculation_enum as chargeCalculation, c.is_penalty as penalty,c.gst_enabled as enableGstCharges, c.gst_enum as gstenum,"
                    + "c.is_active as active, c.is_free_withdrawal as isFreeWithdrawal, c.free_withdrawal_charge_frequency as freeWithdrawalChargeFrequency, c.restart_frequency as restartFrequency, c.restart_frequency_enum as restartFrequencyEnum,"
                    + "oc.name as currencyName, oc.decimal_places as currencyDecimalPlaces, "
                    + "oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, "
                    + "oc.internationalized_name_code as currencyNameCode, c.fee_on_day as feeOnDay, c.fee_on_month as feeOnMonth, "
                    + "c.fee_interval as feeInterval, c.fee_frequency as feeFrequency,c.min_cap as minCap,c.max_cap as maxCap, "
                    + "c.charge_decimal as chargeDecimal,c.charge_rounding_mode as chargeRoundingMode,c.charge_decimal_regex as chargeDecimalRegex,c.gst_decimal as gstDecimal, "
                    + "c.gst_rounding_mode as gstRoundingMode,c.gst_decimal_regex as gstDecimalRegex,c.gst_slab_limit_value as gstSlabLimitValue,c.is_gst_slab_enabled as isGstSlabEnabled,c.gst_slab_limit_apply_for as gstSlabLimitApplyFor,c.gst_slab_limit_operator as gstSlabLimitOperator, "
                    + "c.income_or_liability_account_id as glAccountId , acc.name as glAccountName, acc.gl_code as glCode, "
                    + "tg.id as taxGroupId, c.is_payment_type as isPaymentType, pt.id as paymentTypeId, pt.value as paymentTypeName, tg.name as taxGroupName, "
                    + " cvf.code_value as chargeTypeValue,cvft.code_value as feesTypeValue, c.penalty_interest_days_in_year as  penaltyInterestDaysInYear"
                    + " from m_charge c " + "join m_organisation_currency oc on c.currency_code = oc.code "
                    + " LEFT JOIN acc_gl_account acc on acc.id = c.income_or_liability_account_id " + "LEFT JOIN m_code_value cvf on cvf.id= c.type_cv_id "
                    +" LEFT JOIN m_payment_type pt on pt.id = c.payment_type_id "
                    + "LEFT JOIN m_code_value cvft on cvf.id= c.fees_charge_type_cv_id  "+ " LEFT JOIN m_tax_group tg on tg.id = c.tax_group_id ";
        }

        public String loanProductChargeSchema() {
            return chargeSchema() + " join m_product_loan_charge plc on plc.charge_id = c.id ";
        }

        public String loanApplicationOverDueSchema() {
            return OverDueChargeSchema() + " join m_product_loan_charge plc on plc.charge_id = c.id";
        }

        private String OverDueChargeSchema() {

            return "c.id as id, c.name as name, c.amount as amount,c.min_charge_amount as minChargeAmount,c.max_charge_amount as maxChargeAmount,c.charge_type as type,c.fees_type as feesChargeType, c.currency_code as currencyCode, "
                    + "c.charge_applies_to_enum as chargeAppliesTo, c.charge_time_enum as chargeTime, "
                    + "c.charge_payment_mode_enum as chargePaymentMode,c.type_cv_id as chargeTypeCode,c.fees_charge_type_cv_id as feesTypeCode, "
                    + "c.charge_calculation_enum as chargeCalculation, c.is_penalty as penalty,c.gst_enabled as enableGstCharges, c.gst_enum as gstenum,"
                    + "c.is_active as active, c.is_free_withdrawal as isFreeWithdrawal, c.free_withdrawal_charge_frequency as freeWithdrawalChargeFrequency, c.restart_frequency as restartFrequency, c.restart_frequency_enum as restartFrequencyEnum,"
                    + "oc.name as currencyName, oc.decimal_places as currencyDecimalPlaces, "
                    + "oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, "
                    + "oc.internationalized_name_code as currencyNameCode, c.fee_on_day as feeOnDay, c.fee_on_month as feeOnMonth, "
                    + "c.fee_interval as feeInterval, c.fee_frequency as feeFrequency,c.min_cap as minCap,c.max_cap as maxCap, "
                    + "c.charge_decimal as chargeDecimal,c.charge_rounding_mode as chargeRoundingMode,c.charge_decimal_regex as chargeDecimalRegex,c.gst_decimal as gstDecimal, "
                    + "c.gst_rounding_mode as gstRoundingMode,c.gst_decimal_regex as gstDecimalRegex,c.gst_slab_limit_value as gstSlabLimitValue,c.is_gst_slab_enabled as isGstSlabEnabled,c.gst_slab_limit_apply_for as gstSlabLimitApplyFor,c.gst_slab_limit_operator as gstSlabLimitOperator, "
                    + "c.income_or_liability_account_id as glAccountId , acc.name as glAccountName, acc.gl_code as glCode,lpfc.self_share_percentage as selfShare ,lpfc.partner_share_percentage as partnerShare "
                    + "tg.id as taxGroupId, c.is_payment_type as isPaymentType, pt.id as paymentTypeId, pt.value as paymentTypeName, tg.name as taxGroupName, "
                    + " cvf.code_value as chargeTypeValue,cvft.code_value as feesTypeValue, c.penalty_interest_days_in_year as  penaltyInterestDaysInYear"
                    + " from m_charge c " + "join m_organisation_currency oc on c.currency_code = oc.code "
                    + " LEFT JOIN acc_gl_account acc on acc.id = c.income_or_liability_account_id " + "LEFT JOIN m_code_value cvf on cvf.id= c.type_cv_id "
                    + "LEFT JOIN m_code_value cvft on cvf.id= c.fees_charge_type_cv_id "+ "LEFT JOIN m_loan_penal_foreclosure_charge lpfc on lpfc.charge_id = c.id"
                    + " LEFT JOIN m_tax_group tg on tg.id = c.tax_group_id " + " LEFT JOIN m_payment_type pt on pt.id = c.payment_type_id ";

        }

        public String savingsProductChargeSchema() {
            return chargeSchema() + " join m_savings_product_charge spc on spc.charge_id = c.id";
        }

        public String shareProductChargeSchema() {
            return chargeSchema() + " join m_share_product_charge mspc on mspc.charge_id = c.id";
        }

        @Override
        public ChargeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
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
//            final int gst=rs.getInt("gst");
//            final EnumOptionData gstId= ChargeEnumerations.gst(gst);

            EnumOptionData gstEnumType = null;
            final Integer gstEnum=rs.getInt("gstenum");
            if (gstEnum != null) {
                gstEnumType = CommonEnumerations.gst(gstEnum);
            }
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

            final Integer chargeDecimal = rs.getInt("chargeDecimal");
            final String chargeRoundingMode = rs.getString("chargeRoundingMode");
            final Integer chargeDecimalRegex = rs.getInt("chargeDecimalRegex");
            final Integer gstDecimal = rs.getInt("gstDecimal");
            final String gstRoundingMode = rs.getString("gstRoundingMode");
            final Integer gstDecimalRegex = rs.getInt("gstDecimalRegex");
            final BigDecimal gstSlabLimitValue = rs.getBigDecimal("gstSlabLimitValue");
            final Boolean isDefaultLoanCharge=rs.getBoolean("isDefaultLoanCharge");
            return ChargeData.instance(id, name, amount, currency, chargeTimeType, chargeAppliesToType, chargeCalculationType,
                    chargePaymentMode, feeOnMonthDay, feeInterval, penalty, active, isFreeWithdrawal, freeWithdrawalChargeFrequency,
                    restartFrequency, restartFrequencyEnum, isPaymentType, paymentTypeData, minCap, maxCap, feeFrequencyType, glAccountData,
                    taxGroupData,enableGstCharges,gstEnumType,minChargeAmount,maxChargeAmount,type,typeSelected,feesChargeTypeSelected,feesChargeType, penaltyInterestDaysInYearSelected,
                    chargeDecimal,chargeRoundingMode,chargeDecimalRegex,gstDecimal,gstRoundingMode,gstDecimalRegex,gstSlabLimitValue,isGstSlabEnabled,gstSlabLimitApplyForType,gstSlabLimitOperatorType,isDefaultLoanCharge);
        }
    }
    private static final class ColendingLoanForeClosureBounceChargeMapper implements  RowMapper<ColendingChargeData>{

        public String ColendingLoanChargeSchema(){


            return " c.id as chargeid ,c.name as chargeName,c.is_active as status,c.is_penalty as penalty, c.is_free_withdrawal as feeWithdrawal,"
                    +" c.free_withdrawal_charge_frequency as frequency ,c.restart_frequency as restartfrequency,"
                    +"c.restart_frequency_enum as restartFrequencyEnum,c.is_payment_type as paymenyType,c.charge_payment_mode_enum as paymentModeEnum,"
                    +"c.charge_time_enum as chargeTimeTypeEnum ,c.charge_applies_to_enum as chargeAppliesToEnum, c.currency_code as currencyCode,"
                    +"c.charge_calculation_enum as chargeCalculationEnum,c.charge_payment_mode_enum as chargePaymentMode,"
                    +"c.amount as amount ,c.fee_on_day as feeOnDay,c.fee_interval as feeInterval,"
                    +"c.min_charge_amount as minChargeAmount,c.max_charge_amount as maxChargeAmount,c.fees_type as feeChargeType,c.charge_type as type,"
                    +"c.charge_decimal as chargeDecimal,c.charge_rounding_mode as chargeRoundingMode,c.charge_decimal_regex as chargeDecimalRegex,c.gst_decimal as gstDecimal,"
                    +"c.gst_rounding_mode as gstRoundingMode,c.gst_decimal_regex as gstDecimalRegex,c.gst_slab_limit_value as gstSlabLimitValue,c.is_gst_slab_enabled as isGstSlabEnabled,c.gst_slab_limit_apply_for as gstSlabLimitApplyFor,c.gst_slab_limit_operator as gstSlabLimitOperator, "
                    +"c.gst_enabled as gstEnabled,c.gst_enum as gstEnum,pc.self_share as selfSharepercenatge ,pc.partner_share as partnerSharePercentage, "
                    +"oc.name as currencyName, oc.decimal_places as currencyDecimalPlaces,oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, "
                    +"oc.internationalized_name_code as currencyNameCode "
                    +" from m_charge c "
                    +" join m_organisation_currency oc on c.currency_code = oc.code "
                    +" LEFT JOIN m_product_loan_fees_charges pc on pc.charge_id = c.id";
        }

        @Override
        public ColendingChargeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("chargeid");
            final String name=rs.getString("chargeName");
            final Boolean active =rs.getBoolean("status");
            final Boolean penalty=rs.getBoolean("penalty");
            final Boolean freeWithdrawal=rs.getBoolean("feeWithdrawal");
            final Integer freeWithdrawalFrequency=rs.getInt("frequency");
            final Integer restartfrequency =rs.getInt("restartfrequency");
            final Integer restartFrequencyEnum = rs.getInt("restartFrequencyEnum");
            final Boolean isPaymentType = rs.getBoolean("paymenyType");

            final BigDecimal amount = rs.getBigDecimal("amount");
            final int chargeAppliesTo = rs.getInt("chargeAppliesToEnum");
            final EnumOptionData chargeAppliesToEnum = ChargeEnumerations.chargeAppliesTo(chargeAppliesTo);
            final Integer chargeTime = rs.getInt("chargeTimeTypeEnum");
            final ChargeTimeType chargeTimes =ChargeTimeType.fromInt(chargeTime);
            final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTimes);

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);


            final BigDecimal selfShare = rs.getBigDecimal("selfSharepercenatge");
            final BigDecimal partnerSharePercentage = rs.getBigDecimal("partnerSharePercentage");

            return new ColendingChargeData(id,name,active,penalty,freeWithdrawal,freeWithdrawalFrequency,restartfrequency,restartFrequencyEnum,
                    isPaymentType,null,currency,amount,chargeTimeType,null,chargeTimeType,null,null,null,null,null,null,null,null,
                    null,null,null,null,null,null,null,null,null,null,
                    null,null,null,null,null,null,null,null,null,null,selfShare,partnerSharePercentage);
        }




    }

    private static final class ColendingLoanChargeMapper implements  RowMapper<ColendingChargeData>{

        public String ColendingLoanChargeSchema(){

            return " c.id as chargeid ,c.name as chargeName,c.is_active as status,c.is_penalty as penalty, c.is_free_withdrawal as feeWithdrawal,"
                    +" c.free_withdrawal_charge_frequency as frequency ,c.restart_frequency as restartfrequency,"
                    +"c.restart_frequency_enum as restartFrequencyEnum,c.is_payment_type as paymenyType,c.charge_payment_mode_enum as paymentModeEnum,"
                    +"c.charge_time_enum as chargeTimeTypeEnum ,c.charge_applies_to_enum as chargeAppliesToEnum, c.currency_code as currencyCode,"
                    +"c.charge_calculation_enum as chargeCalculationEnum,c.charge_payment_mode_enum as chargePaymentMode,c.min_cap as minCap, c.max_cap as maxCap,"
                    +"c.amount as amount ,c.fee_on_day as feeOnDay,c.fee_interval as feeInterval,"
                    +"c.min_charge_amount as minChargeAmount,c.max_charge_amount as maxChargeAmount,c.fees_type as feeChargeType,c.charge_type as type,"
                    +"c.charge_decimal as chargeDecimal,c.charge_rounding_mode as chargeRoundingMode,c.charge_decimal_regex as chargeDecimalRegex,c.gst_decimal as gstDecimal,"
                    +"c.gst_rounding_mode as gstRoundingMode,c.gst_decimal_regex as gstDecimalRegex,c.gst_slab_limit_value as gstSlabLimitValue,c.is_gst_slab_enabled as isGstSlabEnabled,c.gst_slab_limit_apply_for as gstSlabLimitApplyFor,c.gst_slab_limit_operator as gstSlabLimitOperator,"
                    +"c.gst_enabled as gstEnabled,c.gst_enum as gstEnum,pc.self_share as selfSharepercenatge ,pc.partner_share as partnerSharePercentage,"
                    + "oc.name as currencyName, oc.decimal_places as currencyDecimalPlaces,oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol,"
                    + " oc.internationalized_name_code as currencyNameCode, c.fee_on_day as feeOnDay "
                    +" from m_charge c "
                    + "join m_organisation_currency oc on c.currency_code = oc.code "
                    +" LEFT JOIN m_product_loan_fees_charges pc on pc.charge_id = c.id";

        }
        @Override
        public ColendingChargeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("chargeid");
            final String name=rs.getString("chargeName");
            final Boolean active =rs.getBoolean("status");
            final Boolean penalty=rs.getBoolean("penalty");
            final Boolean freeWithdrawal=rs.getBoolean("feeWithdrawal");
            final Integer freeWithdrawalFrequency=rs.getInt("frequency");
            final Integer restartfrequency =rs.getInt("restartfrequency");
            final Integer restartFrequencyEnum = rs.getInt("restartFrequencyEnum");
            final Boolean isPaymentType = rs.getBoolean("paymenyType");

            final BigDecimal amount = rs.getBigDecimal("amount");


            final int chargeAppliesTo = rs.getInt("chargeAppliesToEnum");
            final EnumOptionData chargeAppliesToEnum = ChargeEnumerations.chargeAppliesTo(chargeAppliesTo);

            final Integer feeOnDay = rs.getInt("feeOnDay");

            final Integer chargeTime = rs.getInt("chargeTimeTypeEnum");

            final ChargeTimeType chargeTimes =ChargeTimeType.fromInt(chargeTime);
            final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTimes);

            final int paymentMode = rs.getInt("chargePaymentMode");
            final EnumOptionData chargePaymentMode = ChargeEnumerations.chargePaymentMode(paymentMode);

            final int chargeCalculation = rs.getInt("chargeCalculationEnum");
            final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);


            final BigDecimal mincap = rs.getBigDecimal("minCap");
            final BigDecimal maxCap = rs.getBigDecimal("maxCap");
            //final Integer feeInterval = rs.getInt("feeInterval");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);




            final BigDecimal selfShare = rs.getBigDecimal("selfSharepercenatge");
            final BigDecimal partnerSharePercentage = rs.getBigDecimal("partnerSharePercentage");





            return new ColendingChargeData(id,name,active,penalty,freeWithdrawal,freeWithdrawalFrequency,restartfrequency,restartFrequencyEnum,
                    isPaymentType,null,currency,amount,chargeTimeType,null,chargeCalculationType,chargePaymentMode,null,null,mincap,maxCap,null,null,null,
                    null,null,null,null,null,null,null,null,null,null,
                    null,null,null,null,null,null,null,null,null,null,selfShare,partnerSharePercentage);
        }




    }

    private static final class  ColendingChargeMapper implements RowMapper<LoanProductFeesChargesData> {

        public String colendinghargeSchema() {

            return " plc.id as colendingChargeId,plc.product_loan_id as loanProductId,plc.charge_id as chargeId,plc.self_share as selfShare, " +
                    " plc.partner_share as partnerShare,plc.charge_type as type, mc.is_penalty as isPenality, " +
                    " mc.name as chargeName from m_product_loan_fees_charges plc LEFT join m_charge mc on mc.id = plc.charge_id ";

        }

        @Override
        public LoanProductFeesChargesData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Integer id = rs.getInt("colendingChargeId");
            final Integer loanProductId=rs.getInt("loanProductId");
            final Integer chargeId=rs.getInt("chargeId");
            final BigDecimal selfShare=rs.getBigDecimal("selfShare");
            final BigDecimal partnerShare=rs.getBigDecimal("partnerShare");
            final String type=rs.getString("type");
            final String chargeName=rs.getString("chargeName");
            final boolean isPenal = rs.getBoolean("isPenality");
            return isPenal
                    ? new LoanProductFeesChargesData(id,null,null,loanProductId,chargeId,null,null,null,null,type,chargeName,selfShare,partnerShare)
                    : new LoanProductFeesChargesData(id,null,null,loanProductId,chargeId,selfShare,partnerShare,null,null,type,chargeName,null,null);
        }
    }



    @Override
    public Collection<ChargeData> retrieveSavingsProductApplicableCharges(final boolean feeChargesOnly) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=? ";
        if (feeChargesOnly) {
            sql = "select " + rm.chargeSchema()
                    + " where c.is_deleted=false and c.is_active=true and c.is_penalty=false and c.charge_applies_to_enum=? ";
        }
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.SAVINGS.getValue() }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveSavingsApplicablePenalties() {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema()
                + " where c.is_deleted=false and c.is_active=true and c.is_penalty=true and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";
        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.SAVINGS.getValue() }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveSavingsProductCharges(final Long savingsProductId) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.savingsProductChargeSchema()
                + " where c.is_deleted=false and c.is_active=true and spc.savings_product_id=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { savingsProductId }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveShareProductCharges(final Long shareProductId) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.shareProductChargeSchema() + " where c.is_deleted=false and c.is_active=true and mspc.product_id=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { shareProductId }); // NOSONAR
    }

    @Override
    public Collection<LoanProductFeesChargesData> retrieveColendingCharge(Long loanProductId) {
        final ColendingChargeMapper   rm =new ColendingChargeMapper();

        String sql = "select " + rm.colendinghargeSchema() + "  where plc.product_loan_id=? and mc.charge_time_enum <> 9 ";
       sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { loanProductId });
    }

    @Override
    public Collection<LoanProductFeesChargesData> retrieveOverdueCharge(Long loanProductId) {
        final ColendingChargeMapper   rm =new ColendingChargeMapper();
        String sql = "select " + rm.colendinghargeSchema() + "  where plc.product_loan_id=? and mc.charge_time_enum = 9 ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        return this.jdbcTemplate.query(sql, rm, new Object[] { loanProductId });
    }

    @Override
    public  Collection<ColendingChargeData> retrieveColendingLoanForeclosureCharge(Long productId) {

        final ColendingLoanForeClosureBounceChargeMapper rm =new ColendingLoanForeClosureBounceChargeMapper();
        String sql = "select " + rm.ColendingLoanChargeSchema() + " where  pc.product_loan_id=? and c.is_penalty = false and c.is_active = true and c.charge_time_enum =? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { productId,17 });

    }

    @Override
    public Collection<ColendingChargeData> retrieveColendingOtherCharges(Long productId) {

        final ColendingLoanChargeMapper   rm =new ColendingLoanChargeMapper();

        String sql = "select " + rm.ColendingLoanChargeSchema() + " where  pc.product_loan_id=? and c.is_penalty = false and c.is_active = true and c.charge_time_enum < ? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { productId,17 });
    }

    @Override
    public Collection<ChargeData> retrieveSavingsAccountApplicableCharges(Long savingsAccountId) {

        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " join m_savings_account sa on sa.currency_code = c.currency_code"
                + " where c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=? " + " and sa.id = ?";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.SAVINGS.getValue(), savingsAccountId }); // NOSONAR

    }

    @Override
    public Collection<ChargeData> retrieveAllChargesApplicableToClients() {
        final ChargeMapper rm = new ChargeMapper();
        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.CLIENT.getValue() }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicableCharges() {

        final ChargeMapper rm = new ChargeMapper();
        Object[] params = new Object[] { "Fees",ChargeAppliesTo.LOAN.getValue() };

        String sql = "select " + rm.chargeSchema()
                + " where c.is_deleted=false and c.is_active=true  and c.is_penalty=false and c.charge_type = ? and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, params); // NOSONAR


    }

    @Override
    public Collection<ChargeData> retrieveSharesApplicableCharges() {
        final ChargeMapper rm = new ChargeMapper();
        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.SHARES.getValue() }); // NOSONAR
    }

    public  Collection<ColendingChargeData> retrieveColendingLoanBounceCharge(Long productId) {

        final ColendingLoanForeClosureBounceChargeMapper rm =new ColendingLoanForeClosureBounceChargeMapper();
        String sql = "select " + rm.ColendingLoanChargeSchema() + " where  pc.product_loan_id=? and c.is_penalty = false and c.is_active = true and c.charge_time_enum = 19 ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { productId });

    }
}
