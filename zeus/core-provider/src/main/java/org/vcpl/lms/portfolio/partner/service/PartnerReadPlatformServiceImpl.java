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
package org.vcpl.lms.portfolio.partner.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.infrastructure.core.domain.JdbcSupport;
import org.vcpl.lms.infrastructure.core.service.SearchParameters;
import org.vcpl.lms.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.infrastructure.security.utils.ColumnValidator;
import org.vcpl.lms.organisation.monetary.service.CurrencyReadPlatformService;
import org.vcpl.lms.portfolio.partner.data.PartnerData;
import org.vcpl.lms.portfolio.partner.exception.PartnerNotFoundException;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PartnerReadPlatformServiceImpl implements PartnerReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final PlatformSecurityContext context;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final ColumnValidator columnValidator;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public PartnerReadPlatformServiceImpl(final PlatformSecurityContext context,
            final CurrencyReadPlatformService currencyReadPlatformService, final JdbcTemplate jdbcTemplate,
            final ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator, final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.context = context;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.columnValidator = columnValidator;
        this.jdbcTemplate = jdbcTemplate;
        this.sqlGenerator = sqlGenerator;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
    }

    private static final class PartnerMapper implements RowMapper<PartnerData> {

        public String partnerSchema() {

            return " o.id as id, o.partner_name as partnerName, o.partner_company_registration_date as partnerCompanyRegistrationDate, o.pancard as pancard, \n" +
                    " o.cin_number as cinNumber, o.address1 as address1, o.address2 as address2, o.pincode as pincode, o.key_persons as keyPersons, o.gst_number as gstNumber, \n" +
                    " o.beneficiary_name as beneficiaryName, o.beneficiary_account_number as beneficiaryAccountNumber, o.ifsc_code as ifscCode, o.micr_code as micrCode,o.city as city, \n" +
                    " o.swift_code as swiftCode, o.branch as branch, o.model_limit as modelLimit, o.approved_limit as approvedLimit, o.pilot_limit as pilotLimit, \n" +
                    " o.partner_float_limit as partnerFloatLimit, o.balance_limit as balanceLimit, o.agreement_start_date as agreementStartDate, o.agreement_expiry_date as agreementExpiryDate, \n" +
                    " o.source_cv_id as sourceId, scv.code_value as sourceValue, \n" +
                    " o.state_cv_id as stateId, stcv.code_value as stateValue, \n" +
                    " o.country_cv_id as countryId, cycv.code_value as countryValue, \n" +
                    " o.constitution_cv_id as constitutionId, cocv.code_value as constitutionValue, \n" +
                    " o.industry_cv_id as industryId, icv.code_value as industryValue, \n" +
                    " o.sector_cv_id as sectorId, sccv.code_value as sectorValue, \n" +
                    " o.sub_sector_cv_id as subSectorId, sscv.code_value as subSectorValue, \n" +
                    " o.gst_registration_cv_id as gstRegistrationId, gcv.code_value as gstRegistrationValue, \n" +
                    " o.partner_type_cv_id as partnerTypeId, pcv.code_value as partnerTypeValue, \n" +
                    " o.underlying_assets_cv_id as underlyingAssetsId, ucv.code_value as underlyingAssetsValue, \n" +
                    " o.security_cv_id as securityId, sycv.code_value as securityValue, \n" +
                    " o.fldg_calculation_on_cv_id as fldgCalculationOnId, fcv.code_value as fldgCalculationOnValue \n" +
                    " from m_partner o left join m_code_value scv ON o.source_cv_id = scv.id \n" +
//                    " left join m_code_value ccv ON o.city_cv_id = ccv.id \n" +
                    " left join m_code_value stcv ON o.state_cv_id = stcv.id \n" +
                    " left join m_code_value cycv ON o.country_cv_id = cycv.id \n" +
                    " left join m_code_value cocv ON o.constitution_cv_id = cocv.id \n" +
                    " left join m_code_value icv ON o.industry_cv_id = icv.id \n" +
                    " left join m_code_value sccv ON o.sector_cv_id = sccv.id \n" +
                    " left join m_code_value sscv ON o.sub_sector_cv_id = sscv.id \n" +
                    " left join m_code_value gcv ON o.gst_registration_cv_id = gcv.id \n" +
                    " left join m_code_value pcv ON o.partner_type_cv_id = pcv.id \n" +
                    " left join m_code_value ucv ON o.underlying_assets_cv_id = ucv.id \n" +
                    " left join m_code_value sycv ON o.security_cv_id = sycv.id \n" +
                    " left join m_code_value fcv ON o.fldg_calculation_on_cv_id = fcv.id ";
        }

        @Override
        public PartnerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String partnerName = rs.getString("partnerName");
            final LocalDate partnerCompanyRegistrationDate = JdbcSupport.getLocalDate(rs, "partnerCompanyRegistrationDate");
            final Long sourceId = JdbcSupport.getLong(rs, "sourceId");
            final String sourceValue = rs.getString("sourceValue");
            final CodeValueData source = CodeValueData.instance(sourceId, sourceValue);
            final String panCard = rs.getString("panCard");
            final String cinNumber = rs.getString("cinNumber");
            final String address1 = rs.getString("address1");
            final String address2 = rs.getString("address2");
            final Long stateId = JdbcSupport.getLong(rs, "stateId");
            final String stateValue = rs.getString("stateValue");
            final CodeValueData state = CodeValueData.instance(stateId, stateValue);
            final Long pincode = JdbcSupport.getLong(rs, "pincode");
            final Long countryId = JdbcSupport.getLong(rs, "countryId");
            final String countryValue = rs.getString("countryValue");
            final CodeValueData country = CodeValueData.instance(countryId, countryValue);
            final Long constitutionId = JdbcSupport.getLong(rs, "constitutionId");
            final String constitutionValue = rs.getString("constitutionValue");
            final CodeValueData constitution = CodeValueData.instance(constitutionId, constitutionValue);
            final String keyPersons = rs.getString("keyPersons");
            final Long industryId = JdbcSupport.getLong(rs, "industryId");
            final String industryValue = rs.getString("industryValue");
            final CodeValueData industry = CodeValueData.instance(industryId, industryValue);
            final Long sectorId = JdbcSupport.getLong(rs, "sectorId");
            final String sectorValue = rs.getString("sectorValue");
            final CodeValueData sector = CodeValueData.instance(sectorId, sectorValue);
            final Long subSectorId = JdbcSupport.getLong(rs, "subSectorId");
            final String subSectorValue = rs.getString("subSectorValue");
            final CodeValueData subSector = CodeValueData.instance(subSectorId, subSectorValue);
            final String gstNumber = rs.getString("gstNumber");
            final Long gstRegistrationId = JdbcSupport.getLong(rs, "gstRegistrationId");
            final String gstRegistrationValue = rs.getString("gstRegistrationValue");
            final CodeValueData gstRegistration = CodeValueData.instance(gstRegistrationId, gstRegistrationValue);
            final Long partnerTypeId = JdbcSupport.getLong(rs, "partnerTypeId");
            final String partnerTypeValue = rs.getString("partnerTypeValue");
            final CodeValueData partnerType = CodeValueData.instance(partnerTypeId, partnerTypeValue);
            final String beneficiaryName = rs.getString("beneficiaryName");
            final String beneficiaryAccountNumber = rs.getString("beneficiaryAccountNumber");
            final String ifscCode = rs.getString("ifscCode");
            final Long micrCode = JdbcSupport.getLong(rs, "micrCode");
            final String swiftCode = rs.getString("swiftCode");
            final String branch = rs.getString("branch");
            final BigDecimal modelLimit = rs.getBigDecimal("modelLimit");
            final BigDecimal approvedLimit = rs.getBigDecimal("approvedLimit");
            final BigDecimal pilotLimit = rs.getBigDecimal("pilotLimit");
            final BigDecimal partnerFloatLimit = rs.getBigDecimal("partnerFloatLimit");
            final BigDecimal balanceLimit = rs.getBigDecimal("balanceLimit");
            final LocalDate agreementStartDate = JdbcSupport.getLocalDate(rs,"agreementStartDate");
            final LocalDate agreementExpiryDate = JdbcSupport.getLocalDate(rs,"agreementExpiryDate");
            final Long underlyingAssetsId = JdbcSupport.getLong(rs, "underlyingAssetsId");
            final String underlyingAssetsValue = rs.getString("underlyingAssetsValue");
            final CodeValueData underlyingAssets = CodeValueData.instance(underlyingAssetsId, underlyingAssetsValue);
            final Long securityId = JdbcSupport.getLong(rs, "securityId");
            final String securityValue = rs.getString("securityValue");
            final CodeValueData security = CodeValueData.instance(securityId, securityValue);
            final Long fldgCalculationOnId = JdbcSupport.getLong(rs, "fldgCalculationOnId");
            final String fldgCalculationOnValue = rs.getString("fldgCalculationOnValue");
            final CodeValueData fldgCalculationOn = CodeValueData.instance(fldgCalculationOnId, fldgCalculationOnValue);
            final String city = rs.getString("city");
            final List<CodeValueData> sourceOptions = new ArrayList<>();
            final List<CodeValueData> stateOptions = new ArrayList<>();
            final List<CodeValueData> countryOptions = new ArrayList<>();
            final List<CodeValueData> constitutionOptions = new ArrayList<>();
            final List<CodeValueData> industryOptions = new ArrayList<>();
            final List<CodeValueData> sectorOptions = new ArrayList<>();
            final List<CodeValueData> subSectorOptions = new ArrayList<>();
            final List<CodeValueData> gstRegistrationOptions = new ArrayList<>();
            final List<CodeValueData> partnerTypeOptions = new ArrayList<>();
            final List<CodeValueData> underlyingAssetsOptions = new ArrayList<>();
            final List<CodeValueData> securityOptions = new ArrayList<>();
            final List<CodeValueData> fldgCalculationOnOptions = new ArrayList<>();

            return new PartnerData(id, partnerName, partnerCompanyRegistrationDate, source, panCard,cinNumber, address1, address2,  state, pincode, country,
                    constitution, keyPersons, industry, sector, subSector, gstNumber, gstRegistration, partnerType, beneficiaryName, beneficiaryAccountNumber,
                    ifscCode, micrCode, swiftCode, branch, modelLimit, approvedLimit, pilotLimit, partnerFloatLimit, balanceLimit, agreementStartDate,
                    agreementExpiryDate, underlyingAssets, security, fldgCalculationOn, city,sourceOptions, stateOptions,
                    countryOptions, constitutionOptions, industryOptions, sectorOptions, subSectorOptions, gstRegistrationOptions,
                    partnerTypeOptions, underlyingAssetsOptions, securityOptions, fldgCalculationOnOptions);
        }
    }

    private static final class PartnerDropdownMapper implements RowMapper<PartnerData> {

        public String schema() {
            return " o.id as id, o.partner_name as partnerName "
                    + " from m_partner o ";
        }

        @Override
        public PartnerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String partnerName = rs.getString("partnerName");

            final List<CodeValueData> sourceOptions = new ArrayList<>();
            final List<CodeValueData> cityOptions = new ArrayList<>();
            final List<CodeValueData> stateOptions = new ArrayList<>();
            final List<CodeValueData> countryOptions = new ArrayList<>();
            final List<CodeValueData> constitutionOptions = new ArrayList<>();
            final List<CodeValueData> industryOptions = new ArrayList<>();
            final List<CodeValueData> sectorOptions = new ArrayList<>();
            final List<CodeValueData> subSectorOptions = new ArrayList<>();
            final List<CodeValueData> gstRegistrationOptions = new ArrayList<>();
            final List<CodeValueData> partnerTypeOptions = new ArrayList<>();
            final List<CodeValueData> underlyingAssetsOptions = new ArrayList<>();
            final List<CodeValueData> securityOptions = new ArrayList<>();
            final List<CodeValueData> fldgCalculationOnOptions = new ArrayList<>();

            return PartnerData.dropdown(id, partnerName, sourceOptions, stateOptions, countryOptions, constitutionOptions,
                    industryOptions, sectorOptions, gstRegistrationOptions, subSectorOptions, partnerTypeOptions, underlyingAssetsOptions,
                    securityOptions, fldgCalculationOnOptions);
        }
    }

    @Override
    @Cacheable(value = "partners", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')")
    public Collection<PartnerData> retrieveAllPartners(final boolean includeAllPartners, final SearchParameters searchParameters) {
        final AppUser currentUser = this.context.authenticatedUser();
//        final String hierarchy = currentUser.getOffice().getHierarchy();
//        final String hierarchySearchString = includeAllPartners ? "." + "%" : hierarchy + "%";
        final PartnerMapper rm = new PartnerMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(rm.partnerSchema());
//        sqlBuilder.append(" where o.hierarchy like ? ");
        if (searchParameters != null) {
            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append("order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            } else {
                sqlBuilder.append("order by o.id");
            }
        }

        return this.jdbcTemplate.query(sqlBuilder.toString(), rm); // NOSONAR
    }

    @Override
    @Cacheable(value = "partnersForDropdown", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'ofd')")
    public Collection<PartnerData> retrieveAllPartnersForDropdown() {
        final AppUser currentUser = this.context.authenticatedUser();

        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final PartnerDropdownMapper rm = new PartnerDropdownMapper();
//        final String sql = "select " + rm.schema() + "where o.hierarchy like ? order by o.hierarchy";
        final String sql = "select " + rm.schema();

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    @Cacheable(value = "partnersById", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#partnerId)")
    public PartnerData retrievePartner(final Long partnerId) {

        try {
            this.context.authenticatedUser();

            final PartnerMapper rm = new PartnerMapper();
            final String sql = "select " + rm.partnerSchema() + " where o.id = ?";

            final PartnerData selectedPartner = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { partnerId }); // NOSONAR

            return selectedPartner;
        } catch (final EmptyResultDataAccessException e) {
            throw new PartnerNotFoundException(partnerId, e);
        }
    }

    @Override
    public PartnerData retrieveNewPartnerTemplate() {

        this.context.authenticatedUser();

        final List<CodeValueData> sourceOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("Source"));
//        final List<CodeValueData> cityOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("City"));
        final List<CodeValueData> stateOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("State"));
        final List<CodeValueData> countryOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("Country"));
        final List<CodeValueData> constitutionOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("Constitution"));
        final List<CodeValueData> industryOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("Industry"));
        final List<CodeValueData> sectorOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("Sector"));
        final List<CodeValueData> subSectorOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("SubSector"));
        final List<CodeValueData> gstRegistrationOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("GstRegistration"));
        final List<CodeValueData> partnerTypeOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("PartnerType"));
        final List<CodeValueData> underlyingAssetsOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("UnderlyingAssets"));
        final List<CodeValueData> securityOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("Security"));
        final List<CodeValueData> fldgCalculationOnOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("FldgCalculationOn"));

        return PartnerData.template(sourceOptions, stateOptions, countryOptions, constitutionOptions, industryOptions,
                sectorOptions, subSectorOptions, gstRegistrationOptions, partnerTypeOptions, underlyingAssetsOptions,
                securityOptions, fldgCalculationOnOptions);
    }

    public PlatformSecurityContext getContext() {
        return this.context;
    }
}
