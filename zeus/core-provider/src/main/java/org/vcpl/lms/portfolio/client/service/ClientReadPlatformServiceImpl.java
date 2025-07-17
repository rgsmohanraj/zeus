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
package org.vcpl.lms.portfolio.client.service;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;


import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.vcpl.lms.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.core.domain.JdbcSupport;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.core.service.Page;
import org.vcpl.lms.infrastructure.core.service.PaginationHelper;
import org.vcpl.lms.infrastructure.core.service.SearchParameters;
import org.vcpl.lms.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.vcpl.lms.infrastructure.dataqueries.data.DatatableData;
import org.vcpl.lms.infrastructure.dataqueries.data.EntityTables;
import org.vcpl.lms.infrastructure.dataqueries.data.StatusEnum;
import org.vcpl.lms.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.infrastructure.security.utils.ColumnValidator;
import org.vcpl.lms.organisation.office.data.OfficeData;
import org.vcpl.lms.organisation.office.service.OfficeReadPlatformService;
import org.vcpl.lms.organisation.staff.data.StaffData;
import org.vcpl.lms.organisation.staff.service.StaffReadPlatformService;
import org.vcpl.lms.portfolio.address.data.AddressData;
import org.vcpl.lms.portfolio.address.service.AddressReadPlatformService;
import org.vcpl.lms.portfolio.client.api.ClientApiConstants;
import org.vcpl.lms.portfolio.client.data.ClientCollateralManagementData;
import org.vcpl.lms.portfolio.client.data.ClientData;
import org.vcpl.lms.portfolio.client.data.ClientFamilyMembersData;
import org.vcpl.lms.portfolio.client.data.ClientNonPersonData;
import org.vcpl.lms.portfolio.client.data.ClientTimelineData;
import org.vcpl.lms.portfolio.client.domain.ClientEnumerations;
import org.vcpl.lms.portfolio.client.domain.ClientStatus;
import org.vcpl.lms.portfolio.client.domain.LegalForm;
import org.vcpl.lms.portfolio.client.exception.ClientNotFoundException;
import org.vcpl.lms.portfolio.client.utils.AESEncryptionUtils;
import org.vcpl.lms.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.vcpl.lms.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.vcpl.lms.portfolio.group.data.GroupGeneralData;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ClientRequest;
import org.vcpl.lms.portfolio.savings.data.SavingsProductData;
import org.vcpl.lms.portfolio.savings.service.SavingsProductReadPlatformService;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@Service
@RequiredArgsConstructor
public class ClientReadPlatformServiceImpl implements ClientReadPlatformService {
    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    // data mappers
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ClientMapper clientMapper = new ClientMapper();
    private final ClientLookupMapper lookupMapper = new ClientLookupMapper();
    private final ClientMembersOfGroupMapper membersOfGroupMapper = new ClientMembersOfGroupMapper();
    private final ParentGroupsMapper clientGroupsMapper = new ParentGroupsMapper();

    private final AddressReadPlatformService addressReadPlatformService;
    private final ClientFamilyMembersReadPlatformService clientFamilyMembersReadPlatformService;
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final ColumnValidator columnValidator;
    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;

    @Override
    public ClientData retrieveTemplate(final Long officeId, final boolean staffInSelectedOfficeOnly) {
        this.context.authenticatedUser();

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);
        AddressData addressList = null;

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        final Collection<SavingsProductData> savingsProductDatas = this.savingsProductReadPlatformService.retrieveAllForLookupByType(null);

        final GlobalConfigurationPropertyData configuration = this.configurationReadPlatformService
                .retrieveGlobalConfiguration("Enable-Address");

        final Boolean isAddressEnabled = configuration.isEnabled();
        if (isAddressEnabled) {
            addressList = this.addressReadPlatformService.retrieveTemplate();
        }

        final ClientFamilyMembersData familyMemberOptions = this.clientFamilyMembersReadPlatformService.retrieveTemplate();

        Collection<StaffData> staffOptions = null;

        final boolean loanOfficersOnly = false;
        if (staffInSelectedOfficeOnly) {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
        } else {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(defaultOfficeId,
                    loanOfficersOnly);
        }
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }
        final List<CodeValueData> genderOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.GENDER));

        final List<CodeValueData> stateOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.STATE));

//        final List<CodeValueData> repaymentModeOptions = new ArrayList<>(
//                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.REPAYMENTMODE));

        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_TYPE));

        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CLASSIFICATION));

        final List<CodeValueData> clientNonPersonConstitutionOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION));

        final List<CodeValueData> clientNonPersonMainBusinessLineOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE));

        final List<EnumOptionData> clientLegalFormOptions = ClientEnumerations.legalForm(LegalForm.values());

        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService
                .retrieveTemplates(StatusEnum.CREATE.getCode().longValue(), EntityTables.CLIENT.getName(), null);

        final List<CodeValueData> accountTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.ACCOUNTTYPE));


        return ClientData.template(defaultOfficeId, LocalDate.now(DateUtils.getDateTimeZoneOfTenant()), offices, staffOptions, null,
                genderOptions,stateOptions,savingsProductDatas, clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions,
                clientNonPersonMainBusinessLineOptions, clientLegalFormOptions, familyMemberOptions,
                new ArrayList<AddressData>(Arrays.asList(addressList)), isAddressEnabled, datatableTemplates,accountTypeOptions);
    }

    @Override
    // @Transactional(readOnly=true)
    public Page<ClientData> retrieveAll(final SearchParameters searchParameters) {

        if (searchParameters != null && searchParameters.getStatus() != null
                && ClientStatus.fromString(searchParameters.getStatus()) == ClientStatus.INVALID) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final String defaultUserMessage = "The Status value '" + searchParameters.getStatus() + "' is not supported.";
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.status.value.is.not.supported",
                    defaultUserMessage, "status", searchParameters.getStatus());
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        final String userOfficeHierarchy = this.context.officeHierarchy();
        final String underHierarchySearchString = userOfficeHierarchy + "%";
        final String appUserID = String.valueOf(context.authenticatedUser().getId());

        // if (searchParameters.isScopedByOfficeHierarchy()) {
        // this.context.validateAccessRights(searchParameters.getHierarchy());
        // underHierarchySearchString = searchParameters.getHierarchy() + "%";
        // }
        List<Object> paramList = new ArrayList<>(Arrays.asList(underHierarchySearchString, underHierarchySearchString));
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(this.clientMapper.schema());
        sqlBuilder.append(" where (o.hierarchy like ? or transferToOffice.hierarchy like ?) ");

        if (searchParameters != null) {
            if (searchParameters.isSelfUser()) {
                sqlBuilder.append(
                        " and c.id in (select umap.client_id from m_selfservice_user_client_mapping as umap where umap.appuser_id = ? ) ");
                paramList.add(appUserID);
            }

            final String extraCriteria = buildSqlStringFromClientCriteria(this.clientMapper.schema(), searchParameters, paramList);

            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" and (").append(extraCriteria).append(")");
            }

            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            }

            if (searchParameters.isLimited()) {
                sqlBuilder.append(" ");
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
                } else {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
                }
            }
        }
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.clientMapper);
    }

    private String buildSqlStringFromClientCriteria(String schemaSql, final SearchParameters searchParameters, List<Object> paramList) {

        String sqlSearch = searchParameters.getSqlSearch();
        final Long officeId = searchParameters.getOfficeId();
        final String externalId = searchParameters.getExternalId();
        final String displayName = searchParameters.getName();
        final String firstname = searchParameters.getFirstname();
        final String lastname = searchParameters.getLastname();
        final String status = searchParameters.getStatus();

        String extraCriteria = "";
        if (sqlSearch != null) {
            sqlSearch = sqlSearch.replaceAll(" display_name ", " c.display_name ");
            sqlSearch = sqlSearch.replaceAll("display_name ", "c.display_name ");
            extraCriteria = " and (" + sqlSearch + ")";
            this.columnValidator.validateSqlInjection(schemaSql, sqlSearch);
        }

        if (officeId != null) {
            extraCriteria += " and c.office_id = ? ";
            paramList.add(officeId);
        }

        if (externalId != null) {
            paramList.add(externalId);
            extraCriteria += " and c.external_id like ? ";
        }

        if (displayName != null) {
            // extraCriteria += " and concatcoalesce(c.firstname, ''),
            // if(c.firstname > '',' ', '') , coalesce(c.lastname, '')) like "
            paramList.add("%" + displayName + "%");
            extraCriteria += " and c.display_name like ? ";
        }

        if (status != null) {
            ClientStatus clientStatus = ClientStatus.fromString(status);
            extraCriteria += " and c.status_enum = " + clientStatus.getValue().toString() + " ";
        }

        if (firstname != null) {
            paramList.add(firstname);
            extraCriteria += " and c.firstname like ? ";
        }

        if (lastname != null) {
            paramList.add(lastname);
            extraCriteria += " and c.lastname like ? ";
        }

        if (searchParameters.isScopedByOfficeHierarchy()) {
            paramList.add(searchParameters.getHierarchy() + "%");
            extraCriteria += " and o.hierarchy like ? ";
        }

        if (searchParameters.isOrphansOnly()) {
            extraCriteria += " and c.id NOT IN (select client_id from m_group_client) ";
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Override
    public ClientData retrieveOne(final Long clientId) {
        try {
            final String hierarchy = this.context.officeHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            final String sql = "select " + this.clientMapper.schema()
                    + " where ( o.hierarchy like ? or transferToOffice.hierarchy like ?) and c.id = ?";
            final ClientData clientData = this.jdbcTemplate.queryForObject(sql, this.clientMapper, // NOSONAR
                    new Object[] { hierarchySearchString, hierarchySearchString, clientId });

            // Get client collaterals
            final Collection<ClientCollateralManagement> clientCollateralManagements = this.clientCollateralManagementRepositoryWrapper
                    .getCollateralsPerClient(clientId);
            final Set<ClientCollateralManagementData> clientCollateralManagementDataSet = new HashSet<>();

            // Map to client collateral data class
            for (ClientCollateralManagement clientCollateralManagement : clientCollateralManagements) {
                BigDecimal total = clientCollateralManagement.getTotal();
                BigDecimal totalCollateral = clientCollateralManagement.getTotalCollateral(total);
                clientCollateralManagementDataSet
                        .add(ClientCollateralManagementData.setCollateralValues(clientCollateralManagement, total, totalCollateral));
            }

            final String clientGroupsSql = "select " + this.clientGroupsMapper.parentGroupsSchema();

            final Collection<GroupGeneralData> parentGroups = this.jdbcTemplate.query(clientGroupsSql, this.clientGroupsMapper, // NOSONAR
                    new Object[] { clientId });

            return ClientData.setParentGroups(clientData, parentGroups, clientCollateralManagementDataSet);

        } catch (final EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId, e);
        }
    }

    @Override
    public List<ClientData> retrieveAllForLookup() {
        String sql = "select " + this.lookupMapper.schema();
        return this.jdbcTemplate.query(sql, this.lookupMapper); // NOSONAR
    }

    @Override
    public Collection<ClientData> retrieveAllForLookupByOfficeId(final Long officeId) {

        final String sql = "select " + this.lookupMapper.schema() + " where c.office_id = ? and c.status_enum != ?";

        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] { officeId, ClientStatus.CLOSED.getValue() }); // NOSONAR
    }

    @Override
    public Collection<ClientData> retrieveClientMembersOfGroup(final Long groupId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.membersOfGroupMapper.schema() + " where o.hierarchy like ? and pgc.group_id = ?";

        return this.jdbcTemplate.query(sql, this.membersOfGroupMapper, new Object[] { hierarchySearchString, groupId }); // NOSONAR
    }

    @Override
    public Collection<ClientData> retrieveActiveClientMembersOfGroup(final Long groupId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.membersOfGroupMapper.schema()
                + " where o.hierarchy like ? and pgc.group_id = ? and c.status_enum = ? ";

        return this.jdbcTemplate.query(sql, this.membersOfGroupMapper, // NOSONAR
                new Object[] { hierarchySearchString, groupId, ClientStatus.ACTIVE.getValue() });
    }

    private static final class ClientMembersOfGroupMapper implements RowMapper<ClientData> {

        private final String schema;

        ClientMembersOfGroupMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);

            sqlBuilder.append(
                    "c.id as id, c.account_no as accountNo, c.external_id as externalId, c.status_enum as statusEnum,c.sub_status as subStatus, ");
            sqlBuilder.append(
                    "cvSubStatus.code_value as subStatusValue,cvSubStatus.code_description as subStatusDesc,c.office_id as officeId, o.name as officeName, ");
            sqlBuilder.append("c.transfer_to_office_id as transferToOfficeId, transferToOffice.name as transferToOfficeName, ");
            sqlBuilder.append("c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, ");
            sqlBuilder.append("c.fullname as fullname, c.display_name as displayName, ");
            sqlBuilder.append("c.mobile_no as mobileNo, ");
            sqlBuilder.append("c.age as age, ");
            sqlBuilder.append("c.beneficiary_name as beneficiaryName, ");
            sqlBuilder.append("c.city as city, ");
            sqlBuilder.append("c.beneficiary_account_number as beneficiaryAccountNumber, ");
            sqlBuilder.append("c.ifsc_code as ifscCode, ");
            sqlBuilder.append("c.micr_code as micrCode, ");
            sqlBuilder.append("c.swift_code as swiftCode, ");
            sqlBuilder.append("c.branch as branch, ");
            sqlBuilder.append("c.voter_id as voterId, ");
            sqlBuilder.append("c.passport_number as passportNumber, ");
            sqlBuilder.append("c.driving_license as drivingLicense, ");
            sqlBuilder.append("c.address as address,");
            sqlBuilder.append("c.pincode as pincode,");
            sqlBuilder.append("c.ration_card_number as rationCardNumber,");
            sqlBuilder.append("c.is_staff as isStaff, ");
            sqlBuilder.append("c.email_address as emailAddress, ");
            sqlBuilder.append("c.date_of_birth as dateOfBirth, ");
            sqlBuilder.append("c.gender_cv_id as genderId, ");
            sqlBuilder.append("cv.code_value as genderValue, ");
//            sqlBuilder.append("c.repaymentMode_cv_id as repaymentModeId, ");
          //  sqlBuilder.append("cvr.code_value as repaymentModeValue, ");
            sqlBuilder.append("c.client_type_cv_id as clienttypeId, ");
            sqlBuilder.append("cvclienttype.code_value as clienttypeValue, ");
            sqlBuilder.append("c.client_classification_cv_id as classificationId, ");
            sqlBuilder.append("cvclassification.code_value as classificationValue, ");
            sqlBuilder.append("c.legal_form_enum as legalFormEnum, ");
            sqlBuilder.append("c.activation_date as activationDate, c.image_id as imageId, ");
            sqlBuilder.append("c.staff_id as staffId, s.display_name as staffName,");
            sqlBuilder.append("c.default_savings_product as savingsProductId, sp.name as savingsProductName, ");
            sqlBuilder.append("c.default_savings_account as savingsAccountId, ");

            sqlBuilder.append("c.submittedon_date as submittedOnDate, ");
            sqlBuilder.append("sbu.username as submittedByUsername, ");
            sqlBuilder.append("sbu.firstname as submittedByFirstname, ");
            sqlBuilder.append("sbu.lastname as submittedByLastname, ");

            sqlBuilder.append("c.closedon_date as closedOnDate, ");
            sqlBuilder.append("clu.username as closedByUsername, ");
            sqlBuilder.append("clu.firstname as closedByFirstname, ");
            sqlBuilder.append("clu.lastname as closedByLastname, ");

            sqlBuilder.append("acu.username as activatedByUsername, ");
            sqlBuilder.append("acu.firstname as activatedByFirstname, ");
            sqlBuilder.append("acu.lastname as activatedByLastname, ");

            sqlBuilder.append("cnp.constitution_cv_id as constitutionId, ");
            sqlBuilder.append("cvConstitution.code_value as constitutionValue, ");
            sqlBuilder.append("cnp.incorp_no as incorpNo, ");
            sqlBuilder.append("cnp.incorp_validity_till as incorpValidityTill, ");
            sqlBuilder.append("cnp.main_business_line_cv_id as mainBusinessLineId, ");
            sqlBuilder.append("cvMainBusinessLine.code_value as mainBusinessLineValue, ");
            sqlBuilder.append("cnp.remarks as remarks ");
            sqlBuilder.append("c.pan as pan, ");
            sqlBuilder.append("c.aadhaar as aadhaar, ");
            sqlBuilder.append("c.account_type_cv_id as accountTypeId, ");
            sqlBuilder.append("cv.code_value as accountTypeValue, ");

            sqlBuilder.append("from m_client c ");
            sqlBuilder.append("join m_office o on o.id = c.office_id ");
            sqlBuilder.append("left join m_client_non_person cnp on cnp.client_id = c.id ");
            sqlBuilder.append("join m_group_client pgc on pgc.client_id = c.id ");
            sqlBuilder.append("left join m_staff s on s.id = c.staff_id ");
            sqlBuilder.append("left join m_savings_product sp on sp.id = c.default_savings_product ");
            sqlBuilder.append("left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");


            sqlBuilder.append("left join m_appuser sbu on sbu.id = c.submittedon_userid ");
            sqlBuilder.append("left join m_appuser acu on acu.id = c.activatedon_userid ");
            sqlBuilder.append("left join m_appuser clu on clu.id = c.closedon_userid ");
            sqlBuilder.append("left join m_code_value cv on cv.id = c.gender_cv_id ");
            sqlBuilder.append("left join m_code_value cvclienttype on cvclienttype.id = c.client_type_cv_id ");
            sqlBuilder.append("left join m_code_value cvclassification on cvclassification.id = c.client_classification_cv_id ");
            sqlBuilder.append("left join m_code_value cvSubStatus on cvSubStatus.id = c.sub_status ");
            sqlBuilder.append("left join m_code_value cvConstitution on cvConstitution.id = cnp.constitution_cv_id ");
            sqlBuilder.append("left join m_code_value cvMainBusinessLine on cvMainBusinessLine.id = cnp.main_business_line_cv_id ");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final Long subStatusId = JdbcSupport.getLong(rs, "subStatus");
            final String subStatusValue = rs.getString("subStatusValue");
            final String subStatusDesc = rs.getString("subStatusDesc");
            final boolean isActive = false;
            final CodeValueData subStatus = CodeValueData.instance(subStatusId, subStatusValue, subStatusDesc, isActive);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");

            final Long transferToOfficeId = JdbcSupport.getLong(rs, "transferToOfficeId");
            final String transferToOfficeName = rs.getString("transferToOfficeName");

            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String beneficiaryName = rs.getString("beneficiaryName");
            final String city = rs.getString("city");
            final String beneficiaryAccountNumber = rs.getString("beneficiaryAccountNumber");
            final String ifscCode = rs.getString("ifscCode");
            final Long micrCode = JdbcSupport.getLong(rs,"micrCode");
            final String swiftCode = rs.getString("swiftCode");
            final String branch = rs.getString("branch");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
            final Integer age = JdbcSupport.getInteger(rs,"age");
            final boolean isStaff = rs.getBoolean("isStaff");
            final String emailAddress = rs.getString("emailAddress");
            final LocalDate dateOfBirth = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            final Long genderId = JdbcSupport.getLong(rs, "genderId");
            final String genderValue = rs.getString("genderValue");
            final CodeValueData gender = CodeValueData.instance(genderId, genderValue);

            final Long stateId = JdbcSupport.getLong(rs, "stateId");
            final String stateValue = rs.getString("stateValue");
            final CodeValueData state = CodeValueData.instance(stateId, stateValue);

           // final Long repaymentModeId = JdbcSupport.getLong(rs, "repaymentModeId");
           // final String repaymentModeValue = rs.getString("repaymentModeValue");
           // final CodeValueData repaymentMode = CodeValueData.instance(repaymentModeId, repaymentModeValue);

            final Long clienttypeId = JdbcSupport.getLong(rs, "clienttypeId");
            final String clienttypeValue = rs.getString("clienttypeValue");
            final CodeValueData clienttype = CodeValueData.instance(clienttypeId, clienttypeValue);

            final Long classificationId = JdbcSupport.getLong(rs, "classificationId");
            final String classificationValue = rs.getString("classificationValue");
            final CodeValueData classification = CodeValueData.instance(classificationId, classificationValue);

            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final Long imageId = JdbcSupport.getLong(rs, "imageId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");

            final Long savingsProductId = JdbcSupport.getLong(rs, "savingsProductId");
            final String savingsProductName = rs.getString("savingsProductName");

            final Long savingsAccountId = JdbcSupport.getLong(rs, "savingsAccountId");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");
            final String voterId = rs.getString("voterId");
            final String passportNumber = rs.getString("passportNumber");
            final String drivingLicense = rs.getString("drivingLicense");
            final String address = rs.getString("address");
            final Long pincode = JdbcSupport.getLong(rs,"pincode");
            final String rationCardNumber = rs.getString("rationCardNumber");

            final Integer legalFormEnum = JdbcSupport.getInteger(rs, "legalFormEnum");
            EnumOptionData legalForm = null;
            if (legalFormEnum != null) {
                legalForm = ClientEnumerations.legalForm(legalFormEnum);
            }

            final Long constitutionId = JdbcSupport.getLong(rs, "constitutionId");
            final String constitutionValue = rs.getString("constitutionValue");
            final CodeValueData constitution = CodeValueData.instance(constitutionId, constitutionValue);
            final String incorpNo = rs.getString("incorpNo");
            final LocalDate incorpValidityTill = JdbcSupport.getLocalDate(rs, "incorpValidityTill");
            final Long mainBusinessLineId = JdbcSupport.getLong(rs, "mainBusinessLineId");
            final String mainBusinessLineValue = rs.getString("mainBusinessLineValue");
            final CodeValueData mainBusinessLine = CodeValueData.instance(mainBusinessLineId, mainBusinessLineValue);
            final String remarks = rs.getString("remarks");

            final ClientNonPersonData clientNonPerson = new ClientNonPersonData(constitution, incorpNo, incorpValidityTill,
                    mainBusinessLine, remarks);

            final ClientTimelineData timeline = new ClientTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname,
                    submittedByLastname, activationDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate,
                    closedByUsername, closedByFirstname, closedByLastname);
            final String pan = rs.getString("pan");
            final String aadhaar = maskAadhaar(rs.getString("aadhaar"));

            final Long accountTypeId = JdbcSupport.getLong(rs, "accountTypeId");
            final String accountTypeValue = rs.getString("accountTypeValue");
            final CodeValueData accountType = CodeValueData.instance(accountTypeId, accountTypeValue);


            return ClientData.instance(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id,
                    firstname, middlename, lastname,beneficiaryName,city,beneficiaryAccountNumber,ifscCode,micrCode,swiftCode,branch, fullname, displayName, externalId, mobileNo,age, emailAddress, dateOfBirth, gender,state,
                    activationDate, imageId, staffId, staffName, timeline, savingsProductId, savingsProductName, savingsAccountId,
                    clienttype, classification, legalForm, clientNonPerson, isStaff,pan,aadhaar,accountType,voterId,passportNumber,drivingLicense,address,pincode,rationCardNumber);

        }
    }

    @Override
    public Collection<ClientData> retrieveActiveClientMembersOfCenter(final Long centerId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.membersOfGroupMapper.schema()
                + " left join m_group g on pgc.group_id=g.id where o.hierarchy like ? and g.parent_id = ? and c.status_enum = ? group by c.id";

        return this.jdbcTemplate.query(sql, this.membersOfGroupMapper, // NOSONAR
                new Object[] { hierarchySearchString, centerId, ClientStatus.ACTIVE.getValue() });
    }

    private static final class ClientMapper implements RowMapper<ClientData> {

        private final String schema;

        ClientMapper() {
            final StringBuilder builder = new StringBuilder(400);

            builder.append(
                    "c.id as id, c.account_no as accountNo, c.external_id as externalId, c.status_enum as statusEnum,c.sub_status as subStatus, ");
            builder.append(
                    "cvSubStatus.code_value as subStatusValue,cvSubStatus.code_description as subStatusDesc,c.office_id as officeId, o.name as officeName, ");
            builder.append("c.transfer_to_office_id as transferToOfficeId, transferToOffice.name as transferToOfficeName, ");
            builder.append("c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, ");
            builder.append("c.fullname as fullname, c.display_name as displayName, ");
            builder.append("c.mobile_no as mobileNo, ");
            builder.append("c.age as age,");
            builder.append("c.beneficiary_name as beneficiaryName, ");
            builder.append("c.city as city, ");
            builder.append("c.beneficiary_account_number as beneficiaryAccountNumber, ");
            builder.append("c.ifsc_code as ifscCode, ");
            builder.append("c.micr_code as micrCode, ");
            builder.append("c.swift_code as swiftCode, ");
            builder.append("c.branch as branch, ");
            builder.append("c.is_staff as isStaff, ");
            builder.append("c.email_address as emailAddress, ");
            builder.append("c.date_of_birth as dateOfBirth, ");
            builder.append("c.gender_cv_id as genderId, ");
            builder.append("cv.code_value as genderValue, ");
            builder.append("c.state_cv_id as stateId, ");
            builder.append("cvs.code_value as stateValue, ");
            builder.append("c.voter_id as voterId, ");
            builder.append("c.passport_number as passportNumber, ");
            builder.append("c.driving_license as drivingLicense, ");
            builder.append("c.address as address, ");
            builder.append("c.pincode as pincode, ");
            builder.append("c.ration_card_number as rationCardNumber,");
//            builder.append("c.repaymentMode_cv_id as repaymentModeId, ");
           // builder.append("cvr.code_value as repaymentModeValue, ");

            builder.append("c.client_type_cv_id as clienttypeId, ");
            builder.append("cvclienttype.code_value as clienttypeValue, ");
            builder.append("c.client_classification_cv_id as classificationId, ");
            builder.append("cvclassification.code_value as classificationValue, ");
            builder.append("c.legal_form_enum as legalFormEnum, ");

            builder.append("c.submittedon_date as submittedOnDate, ");
            builder.append("sbu.username as submittedByUsername, ");
            builder.append("sbu.firstname as submittedByFirstname, ");
            builder.append("sbu.lastname as submittedByLastname, ");

            builder.append("c.closedon_date as closedOnDate, ");
            builder.append("clu.username as closedByUsername, ");
            builder.append("clu.firstname as closedByFirstname, ");
            builder.append("clu.lastname as closedByLastname, ");

            // builder.append("c.submittedon as submittedOnDate, ");
            builder.append("acu.username as activatedByUsername, ");
            builder.append("acu.firstname as activatedByFirstname, ");
            builder.append("acu.lastname as activatedByLastname, ");

            builder.append("cnp.constitution_cv_id as constitutionId, ");
            builder.append("cvConstitution.code_value as constitutionValue, ");
            builder.append("cnp.incorp_no as incorpNo, ");
            builder.append("cnp.incorp_validity_till as incorpValidityTill, ");
            builder.append("cnp.main_business_line_cv_id as mainBusinessLineId, ");
            builder.append("cvMainBusinessLine.code_value as mainBusinessLineValue, ");
            builder.append("cnp.remarks as remarks, ");

            builder.append("c.activation_date as activationDate, c.image_id as imageId, ");
            builder.append("c.staff_id as staffId, s.display_name as staffName, ");
            builder.append("c.default_savings_product as savingsProductId, sp.name as savingsProductName, ");
            builder.append("c.default_savings_account as savingsAccountId, ");
            builder.append("c.pan as pan,");
            builder.append("c.aadhaar as aadhaar,");
            builder.append("c.account_type_cv_id as accountTypeId, ");
            builder.append("cva.code_value as accountTypeValue ");
            builder.append("from m_client c ");
            builder.append("join m_office o on o.id = c.office_id ");
            builder.append("left join m_client_non_person cnp on cnp.client_id = c.id ");
            builder.append("left join m_staff s on s.id = c.staff_id ");
            builder.append("left join m_savings_product sp on sp.id = c.default_savings_product ");
            builder.append("left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
            builder.append("left join m_appuser sbu on sbu.id = c.submittedon_userid ");
            builder.append("left join m_appuser acu on acu.id = c.activatedon_userid ");
            builder.append("left join m_appuser clu on clu.id = c.closedon_userid ");
            builder.append("left join m_code_value cv on cv.id = c.gender_cv_id ");
            builder.append("left join m_code_value cvs on cvs.id = c.state_cv_id ");
            //  builder.append("left join m_code_value cvr on cvr.id = c.repaymentMode_cv_id ");
            builder.append("left join m_code_value cva on cva.id = c.account_type_cv_id ");
            builder.append("left join m_code_value cvclienttype on cvclienttype.id = c.client_type_cv_id ");
            builder.append("left join m_code_value cvclassification on cvclassification.id = c.client_classification_cv_id ");
            builder.append("left join m_code_value cvSubStatus on cvSubStatus.id = c.sub_status ");
            builder.append("left join m_code_value cvConstitution on cvConstitution.id = cnp.constitution_cv_id ");
            builder.append("left join m_code_value cvMainBusinessLine on cvMainBusinessLine.id = cnp.main_business_line_cv_id ");


            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final Long subStatusId = JdbcSupport.getLong(rs, "subStatus");
            final String subStatusValue = rs.getString("subStatusValue");
            final String subStatusDesc = rs.getString("subStatusDesc");
            final boolean isActive = false;
            final CodeValueData subStatus = CodeValueData.instance(subStatusId, subStatusValue, subStatusDesc, isActive);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");

            final Long transferToOfficeId = JdbcSupport.getLong(rs, "transferToOfficeId");
            final String transferToOfficeName = rs.getString("transferToOfficeName");

            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String beneficiaryName = rs.getString("beneficiaryName");
            final String city = rs.getString("city");
            final String beneficiaryAccountNumber = rs.getString("beneficiaryAccountNumber");
            final String ifscCode = rs.getString("ifscCode");
            final Long micrCode = JdbcSupport.getLong(rs,"micrCode");
            final String swiftCode = rs.getString("swiftCode");
            final String branch = rs.getString("branch");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
            final Integer age = JdbcSupport.getInteger(rs,"age");
            final boolean isStaff = rs.getBoolean("isStaff");
            final String emailAddress = rs.getString("emailAddress");
            final LocalDate dateOfBirth = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            final Long genderId = JdbcSupport.getLong(rs, "genderId");
            final String genderValue = rs.getString("genderValue");
            final CodeValueData gender = CodeValueData.instance(genderId, genderValue);

            final Long stateId = JdbcSupport.getLong(rs, "stateId");
            final String stateValue = rs.getString("stateValue");
            final CodeValueData state = CodeValueData.instance(stateId, stateValue);


//            final Long repaymentModeId = JdbcSupport.getLong(rs, "repaymentModeId");
//            final String repaymentModeValue = rs.getString("repaymentModeValue");
//            final CodeValueData repaymentMode = CodeValueData.instance(repaymentModeId, repaymentModeValue);

            final Long clienttypeId = JdbcSupport.getLong(rs, "clienttypeId");
            final String clienttypeValue = rs.getString("clienttypeValue");
            final CodeValueData clienttype = CodeValueData.instance(clienttypeId, clienttypeValue);

            final Long classificationId = JdbcSupport.getLong(rs, "classificationId");
            final String classificationValue = rs.getString("classificationValue");
            final CodeValueData classification = CodeValueData.instance(classificationId, classificationValue);

            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final Long imageId = JdbcSupport.getLong(rs, "imageId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");

            final Long savingsProductId = JdbcSupport.getLong(rs, "savingsProductId");
            final String savingsProductName = rs.getString("savingsProductName");
            final Long savingsAccountId = JdbcSupport.getLong(rs, "savingsAccountId");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");
            final String voterId = rs.getString("voterId");
            final String passportNumber = rs.getString("passportNumber");
            final String drivingLicense = rs.getString("drivingLicense");
            final String address = rs.getString("address");
            final Long pincode =  JdbcSupport.getLong(rs,"pincode");
            final String rationCardNumber = rs.getString("rationCardNumber");

            final Integer legalFormEnum = JdbcSupport.getInteger(rs, "legalFormEnum");
            EnumOptionData legalForm = null;
            if (legalFormEnum != null) {
                legalForm = ClientEnumerations.legalForm(legalFormEnum);
            }

            final Long constitutionId = JdbcSupport.getLong(rs, "constitutionId");
            final String constitutionValue = rs.getString("constitutionValue");
            final CodeValueData constitution = CodeValueData.instance(constitutionId, constitutionValue);
            final String incorpNo = rs.getString("incorpNo");
            final LocalDate incorpValidityTill = JdbcSupport.getLocalDate(rs, "incorpValidityTill");
            final Long mainBusinessLineId = JdbcSupport.getLong(rs, "mainBusinessLineId");
            final String mainBusinessLineValue = rs.getString("mainBusinessLineValue");
            final CodeValueData mainBusinessLine = CodeValueData.instance(mainBusinessLineId, mainBusinessLineValue);
            final String remarks = rs.getString("remarks");

            final ClientNonPersonData clientNonPerson = new ClientNonPersonData(constitution, incorpNo, incorpValidityTill,
                    mainBusinessLine, remarks);

            final ClientTimelineData timeline = new ClientTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname,
                    submittedByLastname, activationDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate,
                    closedByUsername, closedByFirstname, closedByLastname);
            final String pan = rs.getString("pan");
            final String aadhaar = maskAadhaar(rs.getString("aadhaar"));
            final Long accountTypeId = JdbcSupport.getLong(rs, "accountTypeId");
            final String accountTypeValue = rs.getString("accountTypeValue");
            final CodeValueData accountType = CodeValueData.instance(accountTypeId, accountTypeValue);


            return ClientData.instance(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id,
                    firstname, middlename, lastname,beneficiaryName,city,beneficiaryAccountNumber,ifscCode,micrCode,swiftCode,branch, fullname, displayName, externalId, mobileNo,age, emailAddress, dateOfBirth, gender,state,
                    activationDate, imageId, staffId, staffName, timeline, savingsProductId, savingsProductName, savingsAccountId,
                    clienttype, classification, legalForm, clientNonPerson, isStaff,pan,aadhaar,accountType,voterId,passportNumber,drivingLicense,address,pincode,rationCardNumber);

        }
    }

    private static final class ParentGroupsMapper implements RowMapper<GroupGeneralData> {

        public String parentGroupsSchema() {
            return "gp.id As groupId , gp.account_no as accountNo, gp.display_name As groupName from m_client cl JOIN m_group_client gc ON cl.id = gc.client_id "
                    + "JOIN m_group gp ON gp.id = gc.group_id WHERE cl.id  = ?";
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final String accountNo = rs.getString("accountNo");

            return GroupGeneralData.lookup(groupId, accountNo, groupName);
        }
    }

    private static final class ClientLookupMapper implements RowMapper<ClientData> {

        private final String schema;

        ClientLookupMapper() {
            final StringBuilder builder = new StringBuilder(900);

            builder.append(" c.id as id, c.display_name as displayName,c.status_enum as status,c.mobile_no as mobileno,c.account_no as accountNo,c.external_id as externalId, ");
            builder.append(" c.office_id as officeId, o.name as officeName ");
            builder.append(" from m_client c ");
            builder.append(" join m_office o on o.id = c.office_id ");
            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final Integer statusCode = rs.getInt("status");
            final String mobileNo = rs.getString("mobileno");
            final EnumOptionData status = ClientEnumerations.status(statusCode);
            final String accountNo = rs.getString("accountNo");

            return ClientData.lookup(id, displayName, officeId, officeName,mobileNo, status,accountNo);
        }
    }

    @Override
    public ClientData retrieveClientByIdentifier(final Long identifierTypeId, final String identifierKey)
    {
        try
        {
            final ClientIdentifierMapper mapper = new ClientIdentifierMapper();

            final String sql = "select " + mapper.clientLookupByIdentifierSchema();

            return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { identifierTypeId, identifierKey }); // NOSONAR
        }
        catch (final EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    private static final class ClientIdentifierMapper implements RowMapper<ClientData> {

        public String clientLookupByIdentifierSchema() {
            return "c.id as id, c.account_no as accountNo, c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, "
                    + "c.fullname as fullname, c.display_name as displayName," + "c.office_id as officeId, o.name as officeName "
                    + " from m_client c, m_office o, m_client_identifier ci " + "where o.id = c.office_id and c.id=ci.client_id "
                    + "and ci.document_type_id= ? and ci.document_key like ?";
        }

        @Override
        public ClientData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");

            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");

            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.clientIdentifier(id, accountNo, firstname, middlename, lastname, fullname, displayName, officeId, officeName);
        }
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public ClientData retrieveAllNarrations(final String clientNarrations) {
        final List<CodeValueData> narrations = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(clientNarrations));
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        final Collection<CodeValueData> clientNonPersonConstitutionOptions = null;
        final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions = null;
        final List<EnumOptionData> clientLegalFormOptions = null;

        return ClientData.template(null, null, null, null, narrations, null, null,null,null,null,  null,null,
                  null, null, null, null, null,null);
    }

    @Override
    public Date retrieveClientTransferProposalDate(Long clientId) {
        validateClient(clientId);
        final String sql = "SELECT cl.proposed_transfer_date FROM m_client cl WHERE cl.id =? ";
        try {
            return this.jdbcTemplate.queryForObject(sql, Date.class, clientId);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void validateClient(Long clientId) {
        try {
            final String sql = "SELECT cl.id FROM m_client cl WHERE cl.id =? ";
            this.jdbcTemplate.queryForObject(sql, Long.class, clientId);
        } catch (final EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId, e);
        }
    }



    @Override
    public Collection<Long> retrieveUserClients(Long aUserID) {
        String sql = "SELECT  m.client_id FROM m_selfservice_user_client_mapping m INNER JOIN m_client c ON c.id = m.client_id WHERE m.appuser_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, new Object[] { aUserID });
    }

    @Override
    public ClientRequest getImportClientRequestById(Long clientId) {
        final RowMapper<ClientRequest> clientRequestRowMapper = (final ResultSet rs, final int rownum) -> {
            ClientRequest clientRequest = new ClientRequest();
            clientRequest.setBeneficiaryName(rs.getString("beneficiaryName"));
            clientRequest.setCity(rs.getString("city"));
            clientRequest.setBeneficiaryAccountNumber(rs.getString("beneficiaryAccountNumber"));
            clientRequest.setIfscCode(rs.getString("ifscCode"));
            clientRequest.setMicrCode(rs.getString("micrCode"));
            clientRequest.setSwiftCode(rs.getString("swiftCode"));
            clientRequest.setBranch(rs.getString("branch"));
            clientRequest.setEmailAddress(rs.getString("emailAddress"));
            clientRequest.setMobileNo(rs.getString("mobileNumber"));
            clientRequest.setAccountTypeId(rs.getInt("accountType"));
            clientRequest.setAddress(rs.getString("city") + " " + rs.getString("state"));



            return clientRequest;
        };
        String getClientById = """
                SELECT c.beneficiary_name AS beneficiaryName, c.city AS city,
                c.beneficiary_account_number AS beneficiaryAccountNumber, c.ifsc_code AS ifscCode, c.micr_code AS micrCode, 
                c.swift_code AS swiftCode, c.branch AS branch, c.email_address AS emailAddress, c.mobile_no AS mobileNumber, 
                c.account_type_cv_id AS accountType, c.address AS address,c.pincode as pincode, mcv.code_value as state 
                FROM  m_client c inner join m_code_value mcv on state_cv_id = mcv.id WHERE c.id =  ? """;


        return this.jdbcTemplate.queryForObject(getClientById,clientRequestRowMapper,new Object[] {clientId});
    }

    public static String maskAadhaar(String encryptedAadhaar) {
        if(Objects.isNull(encryptedAadhaar)) {
            return null;
        }
        final String originalAadhaar = AESEncryptionUtils.decryptWithKey(encryptedAadhaar);
        return new StringBuilder(ClientApiConstants.maskAadhaarPrefix).append(originalAadhaar
                .substring(originalAadhaar.length()-4,originalAadhaar.length())).toString();
    }

    @Override
    public Map<Long,String> retrieveEncryptedAadhaar() {
        return jdbcTemplate.query("SELECT cl.id,cl.aadhaar FROM m_client cl", new ResultSetExtractor<Map<Long,String>>(){
            final Map<Long,String> clientMap = new HashMap<>();
            public Map<Long,String> extractData(ResultSet rs) throws SQLException,
                    DataAccessException {
                while(rs.next()) {
                    Long id=rs.getLong("id");
                    String aadhaar=rs.getString("aadhaar");
                    clientMap.put(id,aadhaar);
                }
                return clientMap;
            }
        });
    }


}
