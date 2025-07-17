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
package org.vcpl.lms.portfolio.loanaccount.service;

import static org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations.interestType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vcpl.lms.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.vcpl.lms.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.infrastructure.configuration.domain.ConfigurationDomainService;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.core.data.PaginationParameters;
import org.vcpl.lms.infrastructure.core.domain.JdbcSupport;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.core.service.Page;
import org.vcpl.lms.infrastructure.core.service.PaginationHelper;
import org.vcpl.lms.infrastructure.core.service.SearchParameters;
import org.vcpl.lms.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.infrastructure.security.utils.ColumnValidator;
import org.vcpl.lms.infrastructure.security.utils.SQLInjectionValidator;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrency;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.organisation.monetary.service.CurrencyReadPlatformService;
import org.vcpl.lms.organisation.staff.data.StaffData;
import org.vcpl.lms.organisation.staff.service.StaffReadPlatformService;
import org.vcpl.lms.portfolio.account.data.AccountTransferData;
import org.vcpl.lms.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.vcpl.lms.portfolio.accountdetails.domain.AccountType;
import org.vcpl.lms.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.vcpl.lms.portfolio.accountdetails.service.AccountEnumerations;
import org.vcpl.lms.portfolio.calendar.data.CalendarData;
import org.vcpl.lms.portfolio.calendar.domain.CalendarEntityType;
import org.vcpl.lms.portfolio.calendar.service.CalendarReadPlatformService;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.charge.data.ColendingChargeData;
import org.vcpl.lms.portfolio.charge.domain.*;
import org.vcpl.lms.portfolio.charge.service.ChargeDropdownReadPlatformService;
import org.vcpl.lms.portfolio.charge.service.ChargeEnumerations;
import org.vcpl.lms.portfolio.charge.service.ChargeReadPlatformService;
import org.vcpl.lms.portfolio.client.data.ClientData;
import org.vcpl.lms.portfolio.client.domain.ClientEnumerations;
import org.vcpl.lms.portfolio.client.service.ClientReadPlatformService;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.common.service.CommonEnumerations;
import org.vcpl.lms.portfolio.floatingrates.data.InterestRatePeriodData;
import org.vcpl.lms.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.vcpl.lms.portfolio.fund.data.FundData;
import org.vcpl.lms.portfolio.fund.service.FundReadPlatformService;
import org.vcpl.lms.portfolio.group.data.GroupGeneralData;
import org.vcpl.lms.portfolio.group.data.GroupRoleData;
import org.vcpl.lms.portfolio.group.service.GroupReadPlatformService;
import org.vcpl.lms.portfolio.loanaccount.foreclosure.Foreclosure;
import org.vcpl.lms.portfolio.loanaccount.foreclosure.ForeclosureEnum;
import org.vcpl.lms.portfolio.loanaccount.foreclosure.ForeclosureService;
import org.vcpl.lms.portfolio.loanaccount.foreclosure.ForeclosureUtils;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetails;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetailsRepository;
import org.vcpl.lms.portfolio.loanaccount.data.*;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductData;
import org.vcpl.lms.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.vcpl.lms.portfolio.loanproduct.domain.InterestMethod;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesData;
import org.vcpl.lms.portfolio.loanproduct.domain.ProductCollectionConfig;
import org.vcpl.lms.portfolio.loanproduct.domain.TransactionTypePreference;
import org.vcpl.lms.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations;
import org.vcpl.lms.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.vcpl.lms.portfolio.partner.domain.Partner;
import org.vcpl.lms.portfolio.paymentdetail.data.PaymentDetailData;
import org.vcpl.lms.portfolio.paymenttype.data.PaymentTypeData;
import org.vcpl.lms.portfolio.paymenttype.domain.PaymentType;
import org.vcpl.lms.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional(readOnly = true)
public class LoanReadPlatformServiceImpl extends ForeclosureService implements LoanReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final LoanDropdownReadPlatformService loanDropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final PaginationHelper paginationHelper;
    private final LoanMapper loaanLoanMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;
    private final LoanUtilService loanUtilService;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ChargeRepositoryWrapper chargeRepositoryWrapper;
    private final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final LoanPenalForeclosureChargesRepository loanPenalForeclosureChargesRepository;

    private final MonthlyAccruedLoanRepository monthlyAccruedLoanRepository;
    private final VPayTransactionDetailsRepository vPayTransactionDetailsRepository;
    private final ImportDocumentRepository importDocumentRepository;
    private final ImportDocumentDetailsRepository importDocumentDetailsRepository;
    private final LoanAccrualRepository loanAccrualRepository;
    private final GstService gstService;

    private final LoanChargePaymentService loanChargePaymentService;
    private final RowMapper<MonthlyAccrualLoanDTO> monthlyAccrualLoanRecordMapper = (final ResultSet rs, final int rownum) -> {
        MonthlyAccrualLoanDTO monthlyAccrualLoanDTO = new MonthlyAccrualLoanDTO();
        monthlyAccrualLoanDTO.setId(rs.getLong("id"));
        monthlyAccrualLoanDTO.setInstallment(rs.getInt("installment"));
        monthlyAccrualLoanDTO.setDisbursementDate(LocalDate.parse(rs.getString("disbursementDate")));
        monthlyAccrualLoanDTO.setDueDate(LocalDate.parse(rs.getString("dueDate")));
        monthlyAccrualLoanDTO.setPrincipalOutstanding(rs.getBigDecimal("principleOutStanding"));
        monthlyAccrualLoanDTO.setAnnualNominalInterestRate(rs.getBigDecimal("annualNominalInterestRate"));
        monthlyAccrualLoanDTO.setSelfInterestShare(rs.getBigDecimal("selfInterestShare"));
        monthlyAccrualLoanDTO.setPartnerInterestShare(rs.getBigDecimal("partnerInterestShare"));
        monthlyAccrualLoanDTO.setInterestCalculatedInPeriodEnum(rs.getInt("interestCalculatedInPeriod"));
        monthlyAccrualLoanDTO.setMaturityDate(LocalDate.parse(rs.getString("maturitydate")));
        Integer daysInYears = monthlyAccrualLoanDTO.getInterestCalculatedInPeriodEnum().equals(1)
                ? 360
                : rs.getInt("daysInYearEnum") == 1
                    ? LocalDate.now().lengthOfYear()
                    : rs.getInt("daysInYearEnum");
        monthlyAccrualLoanDTO.setDaysInYearEnum(Long.valueOf(daysInYears));
        monthlyAccrualLoanDTO.checkIsIntermediateMonth();
        return monthlyAccrualLoanDTO;
    };

    @Autowired
    public LoanReadPlatformServiceImpl(final PlatformSecurityContext context,
                                       final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
                                       final LoanProductReadPlatformService loanProductReadPlatformService, final ClientReadPlatformService clientReadPlatformService,
                                       final GroupReadPlatformService groupReadPlatformService, final LoanDropdownReadPlatformService loanDropdownReadPlatformService,
                                       final FundReadPlatformService fundReadPlatformService, final ChargeReadPlatformService chargeReadPlatformService,
                                       final CodeValueReadPlatformService codeValueReadPlatformService, final JdbcTemplate jdbcTemplate,
                                       final NamedParameterJdbcTemplate namedParameterJdbcTemplate, final CalendarReadPlatformService calendarReadPlatformService,
                                       final StaffReadPlatformService staffReadPlatformService, final PaymentTypeReadPlatformService paymentTypeReadPlatformService,
                                       final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
                                       final FloatingRatesReadPlatformService floatingRatesReadPlatformService, final LoanUtilService loanUtilService,
                                       final ConfigurationDomainService configurationDomainService,
                                       final AccountDetailsReadPlatformService accountDetailsReadPlatformService, final LoanRepositoryWrapper loanRepositoryWrapper,
                                       final ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator,
                                       PaginationHelper paginationHelper, final ChargeRepositoryWrapper chargeRepositoryWrapper,
                                       final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, final CurrencyReadPlatformService currencyReadPlatformService,
                                       final MonthlyAccruedLoanRepository monthlyAccruedLoanRepository,
                                       final VPayTransactionDetailsRepository vPayTransactionDetailsRepository, final LoanPenalForeclosureChargesRepository loanPenalForeclosureChargesRepository,
                                       final ImportDocumentRepository importDocumentRepository, final ImportDocumentDetailsRepository importDocumentDetailsRepository, final LoanAccrualRepository loanAccrualRepository, GstService gstService, LoanChargePaymentService loanChargePaymentService) {
        super(loanChargePaymentService);
        this.context = context;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.loanDropdownReadPlatformService = loanDropdownReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.floatingRatesReadPlatformService = floatingRatesReadPlatformService;
        this.loanUtilService = loanUtilService;
        this.configurationDomainService = configurationDomainService;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.columnValidator = columnValidator;
        this.loaanLoanMapper = new LoanMapper(sqlGenerator);
        this.sqlGenerator = sqlGenerator;
        this.paginationHelper = paginationHelper;
        this.chargeRepositoryWrapper=chargeRepositoryWrapper;
        this.chargeDropdownReadPlatformService =chargeDropdownReadPlatformService;
        this.currencyReadPlatformService =currencyReadPlatformService;
        this.monthlyAccruedLoanRepository = monthlyAccruedLoanRepository;
        this.vPayTransactionDetailsRepository = vPayTransactionDetailsRepository;
        this.loanPenalForeclosureChargesRepository = loanPenalForeclosureChargesRepository;
        this.importDocumentRepository = importDocumentRepository;
        this.importDocumentDetailsRepository = importDocumentDetailsRepository;
        this.loanAccrualRepository = loanAccrualRepository;
        this.gstService = gstService;
        this.loanChargePaymentService = loanChargePaymentService;
    }

    @Override
    public LoanAccountData retrieveOne(final Long loanId) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";


            final LoanMapper rm = new LoanMapper(sqlGenerator);


            final StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select ");
            sqlBuilder.append(rm.loanSchema());
            sqlBuilder.append(" join m_office o on (o.id = c.office_id or o.id = g.office_id) ");
            sqlBuilder.append(" left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
            sqlBuilder.append(" where l.id=? and ( o.hierarchy like ? or transferToOffice.hierarchy like ?)");

            return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), rm,
                    new Object[] { loanId, hierarchySearchString, hierarchySearchString });
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(loanId, e);
        }
    }



    @Override
    public LoanAccountData retrieveLoanByLoanAccount(String loanAccountNumber) {

        // final AppUser currentUser = this.context.authenticatedUser();
        this.context.authenticatedUser();
        final LoanMapper rm = new LoanMapper(sqlGenerator);

        final String sql = "select " + rm.loanSchema() + " where l.account_no=?";

        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanAccountNumber }); // NOSONAR

    }

    @Override
    public List<LoanAccountData> retrieveGLIMChildLoansByGLIMParentAccount(String parentloanAccountNumber) {
        this.context.authenticatedUser();
        final LoanMapper rm = new LoanMapper(sqlGenerator);

        final String sql = "select " + rm.loanSchema()
                + " left join glim_parent_child_mapping as glim on glim.glim_child_account_id=l.account_no "
                + "where glim.glim_parent_account_id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { parentloanAccountNumber }); // NOSONAR

    }

    @Override
    public LoanScheduleData retrieveRepaymentSchedule(final Long loanId,
                                                      final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData, Collection<DisbursementData> disbursementData,
                                                      boolean isInterestRecalculationEnabled, BigDecimal totalPaidFeeCharges) {

        try {
            this.context.authenticatedUser();

            final LoanScheduleResultSetExtractor fullResultsetExtractor = new LoanScheduleResultSetExtractor(
                    repaymentScheduleRelatedLoanData, disbursementData, isInterestRecalculationEnabled, totalPaidFeeCharges);
            final String sql = "select " + fullResultsetExtractor.schema() + " where l.id = ? order by ls.loan_id, ls.installment";

            return this.jdbcTemplate.query(sql, fullResultsetExtractor, new Object[] { loanId }); // NOSONAR

        } catch (final EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(loanId, e);
        }
    }

    @Override
    public LoanScheduleData retrieveRepaymentSchedule(Long loanId, RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData, Collection<DisbursementData> disbursementData, boolean isInterestRecalculationEnabled, BigDecimal totalPaidFeeCharges, BigDecimal selfPrincipal, BigDecimal partnerPrincipal, BigDecimal selfInterestCharged, BigDecimal partnerInterestCharged) {
        return null;
    }

    @Override
    public Collection<LoanTransactionData> retrieveLoanTransactions(final Long loanId) {
        try {
            this.context.authenticatedUser();

            final LoanTransactionsMapper rm = new LoanTransactionsMapper(sqlGenerator);

            // retrieve all loan transactions that are not invalid and have not
            // been 'contra'ed by another transaction
            // repayments at time of disbursement (e.g. charges)

            /***
             * TODO Vishwas: Remove references to "Contra" from the codebase
             ***/
            final String sql = "select " + rm.loanPaymentsSchema()
                    + " where tr.loan_id = ? and tr.transaction_type_enum not in (0, 3) and  (tr.is_reversed=false or tr.manually_adjusted_or_reversed = true or tr.transaction_type_enum = 27) order by tr.id, tr.createdon_date ASC, tr.value_date, tr.event ASC ";
            return this.jdbcTemplate.query(sql, rm, new Object[] { loanId }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Page<LoanAccountData> retrieveAll(final SearchParameters searchParameters) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(this.loaanLoanMapper.loanSchema());

        // TODO - for time being this will data scope list of loans returned to
        // only loans that have a client associated.
        // to support senario where loan has group_id only OR client_id will
        // probably require a UNION query
        // but that at present is an edge case
        sqlBuilder.append(" join m_office o on (o.id = c.office_id or o.id = g.office_id) ");
        sqlBuilder.append(" left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
        sqlBuilder.append(" where ( o.hierarchy like ? or transferToOffice.hierarchy like ?)");

        int arrayPos = 2;
        List<Object> extraCriterias = new ArrayList<>();
        extraCriterias.add(hierarchySearchString);
        extraCriterias.add(hierarchySearchString);

        if (searchParameters != null) {

            String sqlQueryCriteria = searchParameters.getSqlSearch();
            if (StringUtils.isNotBlank(sqlQueryCriteria)) {
                SQLInjectionValidator.validateSQLInput(sqlQueryCriteria);
                sqlQueryCriteria = sqlQueryCriteria.replace("accountNo", "l.account_no");
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), sqlQueryCriteria);
                sqlBuilder.append(" and (").append(sqlQueryCriteria).append(")");
            }

            if (StringUtils.isNotBlank(searchParameters.getExternalId())) {
                sqlBuilder.append(" and l.external_id = ?");
                extraCriterias.add(searchParameters.getExternalId());
                arrayPos = arrayPos + 1;
            }
            if (searchParameters.getOfficeId() != null) {
                sqlBuilder.append("and c.office_id =?");
                extraCriterias.add(searchParameters.getOfficeId());
                arrayPos = arrayPos + 1;
            }

            if (StringUtils.isNotBlank(searchParameters.getAccountNo())) {
                sqlBuilder.append(" and l.account_no = ?");
                extraCriterias.add(searchParameters.getAccountNo());
                arrayPos = arrayPos + 1;
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
        final Object[] objectArray = extraCriterias.toArray();
        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), finalObjectArray, this.loaanLoanMapper);
    }

    @Override
    public LoanAccountData retrieveTemplateWithClientAndProductDetails(final Long clientId, final Long productId) {

        this.context.authenticatedUser();

        final ClientData clientAccount = this.clientReadPlatformService.retrieveOne(clientId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();
        LoanAccountData loanTemplateDetails = LoanAccountData.clientDefaults(clientAccount.id(), clientAccount.accountNo(),
                clientAccount.displayName(), clientAccount.officeId(), expectedDisbursementDate);

        if (productId != null) {
            final LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            loanTemplateDetails = LoanAccountData.populateLoanProductDefaults(loanTemplateDetails, selectedProduct);
        }

        return loanTemplateDetails;
    }

    @Override
    public LoanAccountData retrieveTemplateWithGroupAndProductDetails(final Long groupId, final Long productId) {

        this.context.authenticatedUser();

        final GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();
        LoanAccountData loanDetails = LoanAccountData.groupDefaults(groupAccount, expectedDisbursementDate);

        if (productId != null) {
            final LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            loanDetails = LoanAccountData.populateLoanProductDefaults(loanDetails, selectedProduct);
        }

        return loanDetails;
    }

    @Override
    public LoanAccountData retrieveTemplateWithCompleteGroupAndProductDetails(final Long groupId, final Long productId) {

        this.context.authenticatedUser();

        GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        // get group associations
        final Collection<ClientData> membersOfGroup = this.clientReadPlatformService.retrieveClientMembersOfGroup(groupId);
        if (!CollectionUtils.isEmpty(membersOfGroup)) {
            final Collection<ClientData> activeClientMembers = null;
            final Collection<CalendarData> calendarsData = null;
            final CalendarData collectionMeetingCalendar = null;
            final Collection<GroupRoleData> groupRoles = null;
            groupAccount = GroupGeneralData.withAssocations(groupAccount, membersOfGroup, activeClientMembers, groupRoles, calendarsData,
                    collectionMeetingCalendar);
        }

        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();
        LoanAccountData loanDetails = LoanAccountData.groupDefaults(groupAccount, expectedDisbursementDate);

        if (productId != null) {
            final LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            loanDetails = LoanAccountData.populateLoanProductDefaults(loanDetails, selectedProduct);
        }

        return loanDetails;
    }

    @Override
    public LoanTransactionData retrieveLoanTransactionTemplate(final Long loanId) {

        this.context.authenticatedUser();

        RepaymentTransactionTemplateMapper mapper = new RepaymentTransactionTemplateMapper(sqlGenerator);
        String sql = "select " + mapper.schema();
        LoanTransactionData loanTransactionData = this.jdbcTemplate.queryForObject(sql, mapper, // NOSONAR
                new Object[] { LoanTransactionType.REPAYMENT.getValue(), loanId, loanId });
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final List<CodeValueData> repaymentModeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanRepaymentConstants.REPAYMENT_MODE));
        final CodeValueData repaymentMode = null;
        return LoanTransactionData.templateOnTop(loanTransactionData, paymentOptions,repaymentModeOptions,repaymentMode);
    }

    @Override
    public LoanTransactionData retrieveLoanPrePaymentTemplate(final LoanTransactionType repaymentTransactionType, final Long loanId,
                                                              LocalDate onDate) {

        this.context.authenticatedUser();
        this.loanUtilService.validateRepaymentTransactionType(repaymentTransactionType);

        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        loan.setHelpers(null, null, loanRepaymentScheduleTransactionProcessorFactory);

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate earliestUnpaidInstallmentDate = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        final LocalDate recalculateFrom = null;
        final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loan.fetchPrepaymentDetail(scheduleGeneratorDTO, onDate);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(repaymentTransactionType);
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final List<CodeValueData> repaymentModeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanRepaymentConstants.REPAYMENT_MODE));
        final BigDecimal outstandingLoanBalance = loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency).getAmount();
        final BigDecimal selfOutstandingLoanBalance = loanRepaymentScheduleInstallment.getSelfPrincipalOutstanding(currency).getAmount();
        final BigDecimal partnerOutstandingLoanBalance = loanRepaymentScheduleInstallment.getPartnerPrincipalOutstanding(currency).getAmount();
        final BigDecimal advanceAmount=loanRepaymentScheduleInstallment.getAdvanceAmount(currency).getAmount();
        final Integer installmentNumber =loanRepaymentScheduleInstallment.getInstallmentNumber();
        final CodeValue repaymentMode = null;

        final BigDecimal unrecognizedIncomePortion = null;
        BigDecimal adjustedChargeAmount = adjustPrepayInstallmentCharge(loan, onDate);
        return new LoanTransactionData(null, null, null, transactionType, null, currencyData, earliestUnpaidInstallmentDate,
                loanRepaymentScheduleInstallment.getTotalOutstanding(currency).getAmount().subtract(adjustedChargeAmount),
                loan.getNetDisbursalAmount(), loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency).getAmount(),
                loanRepaymentScheduleInstallment.getInterestOutstanding(currency).getAmount(),
                loanRepaymentScheduleInstallment.getFeeChargesOutstanding(currency).getAmount().subtract(adjustedChargeAmount),
                loanRepaymentScheduleInstallment.getPenaltyChargesOutstanding(currency).getAmount(), null, unrecognizedIncomePortion,
                paymentOptions, null, null, null, outstandingLoanBalance, false,null,null,null,null,null,null,
                selfOutstandingLoanBalance,partnerOutstandingLoanBalance,advanceAmount,null,
                loanRepaymentScheduleInstallment.getSelfFeeChargesOutstanding(currency).getAmount(),loanRepaymentScheduleInstallment.getPartnerFeeChargesOutstanding(currency).getAmount(),installmentNumber,repaymentModeOptions,null);
    }

    private BigDecimal adjustPrepayInstallmentCharge(Loan loan, final LocalDate onDate) {
        BigDecimal chargeAmount = BigDecimal.ZERO;
        /*
         * for(LoanCharge loanCharge: loan.charges()){ if(loanCharge.isInstalmentFee() &&
         * loanCharge.getCharge().getChargeCalculation()==ChargeCalculationType. FLAT.getValue()){ for
         * (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
         * if(onDate.isBefore(installment.getDueDate())){ LoanInstallmentCharge loanInstallmentCharge =
         * loanCharge.getInstallmentLoanCharge(installment.getInstallmentNumber( )); if(loanInstallmentCharge != null){
         * chargeAmount = chargeAmount.add(loanInstallmentCharge.getAmountOutstanding()); }
         *
         * break; } } } }
         */
        return chargeAmount;
    }

    @Override
    public LoanTransactionData retrieveWaiveInterestDetails(final Long loanId) {

        AppUser currentUser = this.context.authenticatedUser();

        // TODO - KW -OPTIMIZE - write simple sql query to fetch back overdue
        // interest that can be waived along with the date of repayment period
        // interest is overdue.
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final CurrencyData currencyData = applicationCurrency.toData();

        final LoanTransaction waiveOfInterest = loan.deriveDefaultInterestWaiverTransaction(DateUtils.getLocalDateTimeOfTenant(),
                currentUser);

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WAIVE_INTEREST);

        final BigDecimal amount = waiveOfInterest.getAmount(currency).getAmount();
        final BigDecimal outstandingLoanBalance = null;
        final BigDecimal selfOutstandingLoanBalance = null;
        final BigDecimal partnerOutstandingLoanBalance = null;
        final BigDecimal advanceAmount =BigDecimal.ZERO;
        final BigDecimal foreClosureAmount=BigDecimal.ZERO;
        final Integer installmentNumber = 0;

        final BigDecimal unrecognizedIncomePortion = null;
        return new LoanTransactionData(null, null, null, transactionType, null, currencyData, waiveOfInterest.getTransactionDate(), amount,
                loan.getNetDisbursalAmount(), null, null, null, null, null, null, null, null, outstandingLoanBalance,
                unrecognizedIncomePortion, false,null,null,null,null,null,null,selfOutstandingLoanBalance,partnerOutstandingLoanBalance,advanceAmount,foreClosureAmount,null,null,installmentNumber,null,null);

    }

    @Override
    public LoanTransactionData retrieveNewClosureDetails() {

        this.context.authenticatedUser();
        final BigDecimal outstandingLoanBalance = null;
        final BigDecimal selfOutstandingLoanBalance = null;
        final BigDecimal partnerOutstandingLoanBalance = null;

        final BigDecimal advanceAmount  = BigDecimal.ZERO;
        final BigDecimal foreClosureAmount  = BigDecimal.ZERO;
        final Integer installmentNumber = 0;

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WRITEOFF);
        final BigDecimal unrecognizedIncomePortion = null;
        return new LoanTransactionData(null, null, null, transactionType, null, null, DateUtils.getLocalDateOfTenant(), null, null, null,
                null, null, null, null, null, null, null, outstandingLoanBalance, unrecognizedIncomePortion, false,null,null,null,null,null,null,
                selfOutstandingLoanBalance,partnerOutstandingLoanBalance,advanceAmount,foreClosureAmount,null,null,installmentNumber,null,null);

    }

    @Override
    public LoanApprovalData retrieveApprovalTemplate(final Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        return new LoanApprovalData(loan.getProposedPrincipal(), DateUtils.getLocalDateOfTenant(), loan.getNetDisbursalAmount());
    }

    @Override
    public LoanTransactionData retrieveDisbursalTemplate(final Long loanId, boolean paymentDetailsRequired) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.DISBURSEMENT);
        Collection<PaymentTypeData> paymentOptions = null;
        Collection<CodeValueData> repaymentModeOptions = null;
        if (paymentDetailsRequired) {
            paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        }

        return LoanTransactionData.loanTransactionDataForDisbursalTemplate(transactionType,
                loan.getExpectedDisbursedOnLocalDateForTemplate(), loan.getDisburseAmountForTemplate(), loan.getNetDisbursalAmount(),
                paymentOptions, loan.retriveLastEmiAmount(), loan.getNextPossibleRepaymentDateForRescheduling(),repaymentModeOptions,null);

    }

    @Override
    public Integer retrieveNumberOfRepayments(final Long loanId) {
        this.context.authenticatedUser();
        return this.loanRepositoryWrapper.getNumberOfRepayments(loanId);
    }

    @Override
    public List<LoanRepaymentScheduleInstallmentData> getRepaymentDataResponse(final Long loanId) {
        this.context.authenticatedUser();
        final List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = this.loanRepositoryWrapper
                .getLoanRepaymentScheduleInstallments(loanId);
        List<LoanRepaymentScheduleInstallmentData> loanRepaymentScheduleInstallmentData = new ArrayList<>();

        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : loanRepaymentScheduleInstallments) {
            loanRepaymentScheduleInstallmentData.add(LoanRepaymentScheduleInstallmentData.instanceOf(
                    loanRepaymentScheduleInstallment.getId(), loanRepaymentScheduleInstallment.getInstallmentNumber(),
                    loanRepaymentScheduleInstallment.getDueDate(), loanRepaymentScheduleInstallment
                            .getTotalOutstanding(loanRepaymentScheduleInstallment.getLoan().getCurrency()).getAmount()));
        }
        return loanRepaymentScheduleInstallmentData;
    }

    @Override
    public LoanTransactionData retrieveLoanTransaction(final Long loanId, final Long transactionId) {
        this.context.authenticatedUser();
        try {
            final LoanTransactionsMapper rm = new LoanTransactionsMapper(sqlGenerator);
            final String sql = "select " + rm.loanPaymentsSchema() + " where l.id = ? and tr.id = ? ";
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanId, transactionId }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanTransactionNotFoundException(transactionId, e);
        }
    }


    private static final class LoanMapper implements RowMapper<LoanAccountData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        LoanMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String loanSchema() {
            return "l.id as id, l.account_no as accountNo, l.external_id as externalId, l.fund_id as fundId, f.name as fundName,"
                    + " l.loan_type_enum as loanType, l.loanpurpose_cv_id as loanPurposeId, cv.code_value as loanPurposeName,"
                    + " lp.id as loanProductId, lp.name as loanProductName, lp.description as loanProductDescription,lp.partner_id as partnerId,"
                    + " lp.is_linked_to_floating_interest_rates as isLoanProductLinkedToFloatingRate, "
                    + " lp.allow_variabe_installments as isvariableInstallmentsAllowed, lp.allow_age_limits as allowAgeLimits, "
                    + " lp.allow_multiple_disbursals as multiDisburseLoan,"
                    + " lp.can_define_fixed_emi_amount as canDefineInstallmentAmount,"
                    + " c.id as clientId, c.account_no as clientAccountNo, c.display_name as clientName, c.office_id as clientOfficeId,"
                    + " g.id as groupId, g.account_no as groupAccountNo, g.display_name as groupName,"
                    + " g.office_id as groupOfficeId, g.staff_id As groupStaffId , g.parent_id as groupParentId, (select mg.display_name from m_group mg where mg.id = g.parent_id) as centerName, "
                    + " g.hierarchy As groupHierarchy , g.level_id as groupLevel, g.external_id As groupExternalId, "
                    + " g.status_enum as statusEnum, g.activation_date as activationDate, "
                    + " l.submittedon_date as submittedOnDate, sbu.username as submittedByUsername, sbu.firstname as submittedByFirstname, sbu.lastname as submittedByLastname,"
                    + " l.rejectedon_date as rejectedOnDate, rbu.username as rejectedByUsername, rbu.firstname as rejectedByFirstname, rbu.lastname as rejectedByLastname,"
                    + " l.withdrawnon_date as withdrawnOnDate, wbu.username as withdrawnByUsername, wbu.firstname as withdrawnByFirstname, wbu.lastname as withdrawnByLastname,"
                    + " l.approvedon_date as approvedOnDate, abu.username as approvedByUsername, abu.firstname as approvedByFirstname, abu.lastname as approvedByLastname,"
                    + " l.expected_disbursedon_date as expectedDisbursementDate, l.disbursedon_date as actualDisbursementDate, dbu.username as disbursedByUsername, dbu.firstname as disbursedByFirstname, dbu.lastname as disbursedByLastname,"
                    + " l.closedon_date as closedOnDate, cbu.username as closedByUsername, cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname, l.writtenoffon_date as writtenOffOnDate, "
                    + " l.expected_firstrepaymenton_date as expectedFirstRepaymentOnDate, l.interest_calculated_from_date as interestChargedFromDate, l.expected_maturedon_date as expectedMaturityDate, "
                    + " l.principal_amount_proposed as proposedPrincipal, l.principal_amount as principal, l.approved_principal as approvedPrincipal, l.net_disbursal_amount as netDisbursalAmount,l.self_net_disbursal_amount as netSelfDisbursalAmount,l.partner_net_disbursal_amount as netPartnerDisbursalAmount, l.arrearstolerance_amount as inArrearsTolerance, l.number_of_repayments as numberOfRepayments, l.repay_every as repaymentEvery,"
                    + " l.grace_on_principal_periods as graceOnPrincipalPayment, l.recurring_moratorium_principal_periods as recurringMoratoriumOnPrincipalPeriods, l.grace_on_interest_periods as graceOnInterestPayment, l.grace_interest_free_periods as graceOnInterestCharged,l.grace_on_arrears_ageing as graceOnArrearsAgeing,"
                    + " l.nominal_interest_rate_per_period as interestRatePerPeriod, l.annual_nominal_interest_rate as annualInterestRate, "
                    + " l.repayment_period_frequency_enum as repaymentFrequencyType, l.interest_period_frequency_enum as interestRateFrequencyType, "
                    + " l.term_frequency as termFrequency, l.term_period_frequency_enum as termPeriodFrequencyType, l.repayment_strategy_for_npa as repaymentStrategyForNpa, "
                    + " l.amortization_method_enum as amortizationType, l.interest_method_enum as interestType, l.is_equal_amortization as isEqualAmortization, l.interest_calculated_in_period_enum as interestCalculationPeriodType,l.broken_interest_derived as brokenAtDisbursement,l.broken_interest_repaid as brokenAsDerived,"
                    + " l.fixed_principal_percentage_per_installment fixedPrincipalPercentagePerInstallment,l.total_gst_derived as totalGstDerived,l.total_gst_paid as totalGstPaid, "
                    + " l.allow_partial_period_interest_calcualtion as allowPartialPeriodInterestCalcualtion,"
                    + " l.loan_status_id as lifeCycleStatusId, l.loan_transaction_strategy_id as transactionStrategyId,l.xirr_value as xirrValue, "
                    + " lps.name as transactionStrategyName, "
                    + " l.currency_code as currencyCode, l.currency_digits as currencyDigits, l.currency_multiplesof as inMultiplesOf, rc."
                    + sqlGenerator.escape("name")
                    + " as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, "
                    + " l.loan_officer_id as loanOfficerId, s.display_name as loanOfficerName, "
                    + " l.principal_disbursed_derived as principalDisbursed," + " l.principal_repaid_derived as principalPaid, "+ " l.self_principal_repaid_derived as selfPrincipalPaid ," + " l.partner_principal_repaid_derived as partnerPrincipalPaid, "
                    + " l.principal_writtenoff_derived as principalWrittenOff,"+ " l.self_principal_writtenoff_derived as selfPrincipalWrittenOff,"+ " l.partner_principal_writtenoff_derived as partnerPrincipalWrittenOff,"
                    + " l.principal_outstanding_derived as principalOutstanding," + " l.self_principal_outstanding_derived as selfPrincipalOutstanding," + " l.partner_principal_outstanding_derived as partnerPrincipalOutstanding," + " l.interest_charged_derived as interestCharged,"
                    + " l.self_principal_amount as selfPrincipalAmount," + " l.partner_principal_amount as partnerPrincipalAmount,"
                    + " l.interest_repaid_derived as interestPaid," + " l.self_interest_repaid_derived as selfInterestPaid," + " l.partner_interest_repaid_derived as partnerInterestPaid," + " l.interest_waived_derived as interestWaived,l.self_interest_waived_derived as selfInterestWaived,l.partner_interest_waived_derived as partnerInterestWaived,"
                    + " l.interest_writtenoff_derived as interestWrittenOff," + " l.interest_outstanding_derived as interestOutstanding,"
                    + " l.fee_charges_charged_derived as feeChargesCharged," + " l.self_fee_charges_charged_derived as selfFeeChargesCharged," + " l.partner_fee_charges_charged_derived as partnerFeeChargesCharged,"
                    + " l.total_charges_due_at_disbursement_derived as feeChargesDueAtDisbursementCharged,l.total_self_charges_due_at_disbursement_derived as selfFeeChargesDueAtDisbursementCharged,l.total_partner_charges_due_at_disbursement_derived as partnerFeeChargesDueAtDisbursementCharged,"
                    + " l.fee_charges_repaid_derived as feeChargesPaid," + " l.fee_charges_waived_derived as feeChargesWaived,l.self_fee_charges_waived_derived as selfFeeChargesWaived,l.partner_fee_charges_waived_derived as partnerFeeChargesWaived,"
                    + " l.fee_charges_writtenoff_derived as feeChargesWrittenOff,"
                    + " l.fee_charges_outstanding_derived as feeChargesOutstanding,"
                    + " l.penalty_charges_charged_derived as penaltyChargesCharged,"
                    + " l.penalty_charges_repaid_derived as penaltyChargesPaid,"
                    + " l.penalty_charges_waived_derived as penaltyChargesWaived,"
                    + " l.penalty_charges_writtenoff_derived as penaltyChargesWrittenOff,"
                    + " l.penalty_charges_outstanding_derived as penaltyChargesOutstanding,"


                    + " l.bounce_charges_charged_derived as bounceChargesCharged,"
                    + " l.bounce_charges_repaid_derived as bounceChargesPaid,"
                    + " l.bounce_charges_waived_derived as bounceChargesWaived,"
                    + " l.bounce_charges_writtenoff_derived as bounceChargesWrittenOff,"
                    + " l.bounce_charges_outstanding_derived as bounceChargesOutstanding,"


                    + " l.total_expected_repayment_derived as totalExpectedRepayment," + " l.total_repayment_derived as totalRepayment,l.total_self_repayment_derived as totalSelfRepayment,l.total_partner_repayment_derived as totalPartnerRepayment,"
                    + " l.total_expected_costofloan_derived as totalExpectedCostOfLoan," + " l.total_costofloan_derived as totalCostOfLoan,"
                    + " l.total_waived_derived as totalWaived," + " l.total_writtenoff_derived as totalWrittenOff,"
                    + " l.writeoff_reason_cv_id as writeoffReasonId," + " codev.code_value as writeoffReason,"
                    + " l.total_outstanding_derived as totalOutstanding," + " l.total_overpaid_derived as totalOverpaid,"
                    + " l.fixed_emi_amount as fixedEmiAmount," + " l.max_outstanding_loan_balance as outstandingLoanBalance,"
                    + " l.loan_sub_status_id as loanSubStatusId," + " l.cooling_off_reversed_charge_amount as coolingOffReversedChargeAmount, " + " la.principal_overdue_derived as principalOverdue,"
                    + " la.interest_overdue_derived as interestOverdue," + " la.fee_charges_overdue_derived as feeChargesOverdue,"
                    + " la.penalty_charges_overdue_derived as penaltyChargesOverdue," + " la.total_overdue_derived as totalOverdue,"
                    + " la.overdue_since_date_derived as overdueSinceDate,"
                    + " l.sync_disbursement_with_meeting as syncDisbursementWithMeeting,"
                    + " l.loan_counter as loanCounter, l.loan_product_counter as loanProductCounter,"
                    + " l.is_npa as isNPA, l.days_in_month_enum as daysInMonth, l.days_in_year_enum as daysInYear, "
                    + " l.interest_recalculation_enabled as isInterestRecalculationEnabled, "
                    + " lir.id as lirId, lir.loan_id as loanId, lir.compound_type_enum as compoundType, lir.reschedule_strategy_enum as rescheduleStrategy, "
                    + " lir.rest_frequency_type_enum as restFrequencyEnum, lir.rest_frequency_interval as restFrequencyInterval, "
                    + " lir.rest_frequency_nth_day_enum as restFrequencyNthDayEnum, "
                    + " lir.rest_frequency_weekday_enum as restFrequencyWeekDayEnum, "
                    + " lir.rest_frequency_on_day as restFrequencyOnDay, "
                    + " lir.compounding_frequency_type_enum as compoundingFrequencyEnum, lir.compounding_frequency_interval as compoundingInterval, "
                    + " lir.compounding_frequency_nth_day_enum as compoundingFrequencyNthDayEnum, "
                    + " lir.compounding_frequency_weekday_enum as compoundingFrequencyWeekDayEnum, "
                    + " lir.compounding_frequency_on_day as compoundingFrequencyOnDay, "
                    + " lir.is_compounding_to_be_posted_as_transaction as isCompoundingToBePostedAsTransaction, "
                    + " lir.allow_compounding_on_eod as allowCompoundingOnEod, "
                    + " l.is_floating_interest_rate as isFloatingInterestRate, "
                    + " l.interest_rate_differential as interestRateDifferential, "
                    + " l.create_standing_instruction_at_disbursement as createStandingInstructionAtDisbursement, "
                    + " lpvi.minimum_gap as minimuminstallmentgap, lpvi.maximum_gap as maximuminstallmentgap, al.min_age as minimumAge, al.max_age as maximumAge, "
                    + " lp.can_use_for_topup as canUseForTopup, " + " l.is_topup as isTopup, " + " topup.closure_loan_id as closureLoanId, "
                    + " l.total_recovered_derived as totalRecovered" + ", topuploan.account_no as closureLoanAccountNo, "
                    + " topup.topup_amount as topupAmount ,l.vcl_hurdle_rate as vclHurdleRate " + " from m_loan l" //
                    + " join m_product_loan lp on lp.id = l.product_id" //
                    + " left join m_loan_recalculation_details lir on lir.loan_id = l.id " + " join m_currency rc on rc."
                    + sqlGenerator.escape("code") + " = l.currency_code" //
                    + " left join m_client c on c.id = l.client_id" //
                    + " left join m_group g on g.id = l.group_id" //
                    + " left join m_loan_arrears_aging la on la.loan_id = l.id" //
                    + " left join m_fund f on f.id = l.fund_id" //
                    + " left join m_staff s on s.id = l.loan_officer_id" //
                    + " left join m_appuser sbu on sbu.id = l.submittedon_userid"
                    + " left join m_appuser rbu on rbu.id = l.rejectedon_userid"
                    + " left join m_appuser wbu on wbu.id = l.withdrawnon_userid"
                    + " left join m_appuser abu on abu.id = l.approvedon_userid"
                    + " left join m_appuser dbu on dbu.id = l.disbursedon_userid" + " left join m_appuser cbu on cbu.id = l.closedon_userid"
                    + " left join m_code_value cv on cv.id = l.loanpurpose_cv_id"
                    + " left join m_code_value codev on codev.id = l.writeoff_reason_cv_id"
                    + " left join ref_loan_transaction_processing_strategy lps on lps.id = l.loan_transaction_strategy_id "
                    + " left join m_product_loan_variable_installment_config lpvi on lpvi.loan_product_id = l.product_id "
                    + " left join m_product_loan_age_limits_config al on al.product_loan_id = l.product_id "
                    + " left join m_loan_topup as topup on l.id = topup.loan_id"
                    + " left join m_loan as topuploan on topuploan.id = topup.closure_loan_id";

        }

        @Override
        public LoanAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");

            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientAccountNo = rs.getString("clientAccountNo");
            final Long clientOfficeId = JdbcSupport.getLong(rs, "clientOfficeId");
            final String clientName = rs.getString("clientName");

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final String groupAccountNo = rs.getString("groupAccountNo");
            final String groupExternalId = rs.getString("groupExternalId");
            final Long groupOfficeId = JdbcSupport.getLong(rs, "groupOfficeId");
            final Long groupStaffId = JdbcSupport.getLong(rs, "groupStaffId");
            final Long groupParentId = JdbcSupport.getLong(rs, "groupParentId");
            final String centerName = rs.getString("centerName");
            final String groupHierarchy = rs.getString("groupHierarchy");
            final String groupLevel = rs.getString("groupLevel");

            final Integer loanTypeId = JdbcSupport.getInteger(rs, "loanType");
            final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeId);

            final Long fundId = JdbcSupport.getLong(rs, "fundId");
            final String fundName = rs.getString("fundName");

            final Long loanOfficerId = JdbcSupport.getLong(rs, "loanOfficerId");
            final String loanOfficerName = rs.getString("loanOfficerName");

            final Long loanPurposeId = JdbcSupport.getLong(rs, "loanPurposeId");
            final String loanPurposeName = rs.getString("loanPurposeName");

            final Long loanProductId = JdbcSupport.getLong(rs, "loanProductId");
            final Long partnerId =JdbcSupport.getLong(rs,"partnerId");
            final String loanProductName = rs.getString("loanProductName");
            final String loanProductDescription = rs.getString("loanProductDescription");
            final boolean isLoanProductLinkedToFloatingRate = rs.getBoolean("isLoanProductLinkedToFloatingRate");
            final Boolean multiDisburseLoan = rs.getBoolean("multiDisburseLoan");
            final Boolean canDefineInstallmentAmount = rs.getBoolean("canDefineInstallmentAmount");
            final BigDecimal outstandingLoanBalance = rs.getBigDecimal("outstandingLoanBalance");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, "rejectedOnDate");
            final String rejectedByUsername = rs.getString("rejectedByUsername");
            final String rejectedByFirstname = rs.getString("rejectedByFirstname");
            final String rejectedByLastname = rs.getString("rejectedByLastname");

            final LocalDate withdrawnOnDate = JdbcSupport.getLocalDate(rs, "withdrawnOnDate");
            final String withdrawnByUsername = rs.getString("withdrawnByUsername");
            final String withdrawnByFirstname = rs.getString("withdrawnByFirstname");
            final String withdrawnByLastname = rs.getString("withdrawnByLastname");

            final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedOnDate");
            final String approvedByUsername = rs.getString("approvedByUsername");
            final String approvedByFirstname = rs.getString("approvedByFirstname");
            final String approvedByLastname = rs.getString("approvedByLastname");

            final LocalDate expectedDisbursementDate = JdbcSupport.getLocalDate(rs, "expectedDisbursementDate");
            final LocalDate actualDisbursementDate = JdbcSupport.getLocalDate(rs, "actualDisbursementDate");
            final String disbursedByUsername = rs.getString("disbursedByUsername");
            final String disbursedByFirstname = rs.getString("disbursedByFirstname");
            final String disbursedByLastname = rs.getString("disbursedByLastname");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate writtenOffOnDate = JdbcSupport.getLocalDate(rs, "writtenOffOnDate");
            final Long writeoffReasonId = JdbcSupport.getLong(rs, "writeoffReasonId");
            final String writeoffReason = rs.getString("writeoffReason");
            final LocalDate expectedMaturityDate = JdbcSupport.getLocalDate(rs, "expectedMaturityDate");

            final Boolean isvariableInstallmentsAllowed = rs.getBoolean("isvariableInstallmentsAllowed");
            final Integer minimumGap = rs.getInt("minimuminstallmentgap");
            final Integer maximumGap = rs.getInt("maximuminstallmentgap");

            final Boolean allowAgeLimits = rs.getBoolean("allowAgeLimits");
            final Integer minAge = rs.getInt("minimumAge");
            final Integer maxAge = rs.getInt("maximumAge");

            final LoanApplicationTimelineData timeline = new LoanApplicationTimelineData(submittedOnDate, submittedByUsername,
                    submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname,
                    withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname, withdrawnByLastname, approvedOnDate, approvedByUsername,
                    approvedByFirstname, approvedByLastname, expectedDisbursementDate, actualDisbursementDate, disbursedByUsername,
                    disbursedByFirstname, disbursedByLastname, closedOnDate, closedByUsername, closedByFirstname, closedByLastname,
                    expectedMaturityDate, writtenOffOnDate, closedByUsername, closedByFirstname, closedByLastname);

            final BigDecimal principal = rs.getBigDecimal("principal");
            final BigDecimal approvedPrincipal = rs.getBigDecimal("approvedPrincipal");
            final BigDecimal proposedPrincipal = rs.getBigDecimal("proposedPrincipal");
            final BigDecimal netDisbursalAmount = rs.getBigDecimal("netDisbursalAmount");
            final BigDecimal netSelfDisbursalAmount = rs.getBigDecimal("netSelfDisbursalAmount");
            final BigDecimal netPartnerDisbursalAmount = rs.getBigDecimal("netPartnerDisbursalAmount");
            final BigDecimal totalOverpaid = rs.getBigDecimal("totalOverpaid");
            final BigDecimal inArrearsTolerance = rs.getBigDecimal("inArrearsTolerance");

            final Integer numberOfRepayments = JdbcSupport.getInteger(rs, "numberOfRepayments");
            final Integer repaymentEvery = JdbcSupport.getInteger(rs, "repaymentEvery");
            final BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
            final BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");
            final BigDecimal interestRateDifferential = rs.getBigDecimal("interestRateDifferential");
            final boolean isFloatingInterestRate = rs.getBoolean("isFloatingInterestRate");

            final BigDecimal brokenInterestDeived=rs.getBigDecimal("brokenAtDisbursement");
            final BigDecimal brokenInterestPaid=rs.getBigDecimal("brokenAsDerived");

            final Integer graceOnPrincipalPayment = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnPrincipalPayment");
            final Integer recurringMoratoriumOnPrincipalPeriods = JdbcSupport.getIntegerDefaultToNullIfZero(rs,
                    "recurringMoratoriumOnPrincipalPeriods");
            final Integer graceOnInterestPayment = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnInterestPayment");
            final Integer graceOnInterestCharged = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnInterestCharged");
            final Integer graceOnArrearsAgeing = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnArrearsAgeing");

            final Integer termFrequency = JdbcSupport.getInteger(rs, "termFrequency");
            final Integer termPeriodFrequencyTypeInt = JdbcSupport.getInteger(rs, "termPeriodFrequencyType");
            final EnumOptionData termPeriodFrequencyType = LoanEnumerations.termFrequencyType(termPeriodFrequencyTypeInt);

            final int repaymentFrequencyTypeInt = JdbcSupport.getInteger(rs, "repaymentFrequencyType");
            final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(repaymentFrequencyTypeInt);

            final int interestRateFrequencyTypeInt = JdbcSupport.getInteger(rs, "interestRateFrequencyType");
            final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(interestRateFrequencyTypeInt);

            final Long transactionStrategyId = JdbcSupport.getLong(rs, "transactionStrategyId");
            final BigDecimal xirrValue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "xirrValue");
            final String transactionStrategyName = rs.getString("transactionStrategyName");

            final int amortizationTypeInt = JdbcSupport.getInteger(rs, "amortizationType");
            final int interestTypeInt = JdbcSupport.getInteger(rs, "interestType");
            final int interestCalculationPeriodTypeInt = JdbcSupport.getInteger(rs, "interestCalculationPeriodType");
            final boolean isEqualAmortization = rs.getBoolean("isEqualAmortization");
            final EnumOptionData amortizationType = LoanEnumerations.amortizationType(amortizationTypeInt);
            final BigDecimal fixedPrincipalPercentagePerInstallment = rs.getBigDecimal("fixedPrincipalPercentagePerInstallment");
            final EnumOptionData interestType = LoanEnumerations.interestType(interestTypeInt);
            final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                    .interestCalculationPeriodType(interestCalculationPeriodTypeInt);
            final Boolean allowPartialPeriodInterestCalcualtion = rs.getBoolean("allowPartialPeriodInterestCalcualtion");
            final BigDecimal selfPrincipalAmount = rs.getBigDecimal("selfPrincipalAmount");
            final BigDecimal partnerPrincipalAmount = rs.getBigDecimal("partnerPrincipalAmount");

            final Integer lifeCycleStatusId = JdbcSupport.getInteger(rs, "lifeCycleStatusId");
            final LoanStatusEnumData status = LoanEnumerations.status(lifeCycleStatusId);


            final Integer loanSubStatusId = JdbcSupport.getInteger(rs, "loanSubStatusId");
            EnumOptionData loanSubStatus = null;
            if (loanSubStatusId != null) {
                loanSubStatus = LoanSubStatus.loanSubStatus(loanSubStatusId);
            }

            // settings
            final LocalDate expectedFirstRepaymentOnDate = JdbcSupport.getLocalDate(rs, "expectedFirstRepaymentOnDate");
            final LocalDate interestChargedFromDate = JdbcSupport.getLocalDate(rs, "interestChargedFromDate");

            final Boolean syncDisbursementWithMeeting = rs.getBoolean("syncDisbursementWithMeeting");

            final BigDecimal feeChargesDueAtDisbursementCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                    "feeChargesDueAtDisbursementCharged");

            final BigDecimal selfFeeChargesDueAtDisbursementCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                    "selfFeeChargesDueAtDisbursementCharged");

            final BigDecimal partnerFeeChargesDueAtDisbursementCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                    "partnerFeeChargesDueAtDisbursementCharged");

            LoanSummaryData loanSummary = null;
            Boolean inArrears = false;
            if (status.id().intValue() >= 300) {

                // loan summary
                final BigDecimal principalDisbursed = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDisbursed");
                final BigDecimal principalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalPaid");

                final BigDecimal selfPrincipalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipalPaid");
                final BigDecimal partnerPrincipalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipalPaid");

                final BigDecimal principalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalWrittenOff");
                final BigDecimal principalOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalOutstanding");
                final BigDecimal principalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalOverdue");

                final BigDecimal interestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestCharged");
                final BigDecimal interestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestPaid");
                final BigDecimal selfInterestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestPaid");
                final BigDecimal partnerInterestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestPaid");

                final BigDecimal interestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWaived");
                final BigDecimal selfInterestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestWaived");
                final BigDecimal partnerInterestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestWaived");
                final BigDecimal interestWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWrittenOff");
                final BigDecimal interestOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestOutstanding");
                final BigDecimal interestOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestOverdue");

                 BigDecimal feeChargesCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesCharged");
                final BigDecimal selfFeeChargesCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfFeeChargesCharged");
                final BigDecimal partnerFeeChargesCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerFeeChargesCharged");
                 BigDecimal feeChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesPaid");
                final BigDecimal feeChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWaived");
                final BigDecimal selfFeeChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfFeeChargesWaived");
                final BigDecimal partnerFeeChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerFeeChargesWaived");
                final BigDecimal feeChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWrittenOff");
                final BigDecimal feeChargesOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesOutstanding");
                final BigDecimal feeChargesOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesOverdue");

                final BigDecimal totalGst = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,"totalGstDerived");
                //feeChargesCharged = BigDecimal.valueOf(feeChargesCharged.doubleValue()+totalGst.doubleValue());

                final BigDecimal totalGstPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,"totalGstPaid");
                //feeChargesPaid = BigDecimal.valueOf(feeChargesPaid.doubleValue() + totalGstPaid.doubleValue());

                final BigDecimal penaltyChargesCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesCharged");
                final BigDecimal penaltyChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesPaid");
                final BigDecimal penaltyChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWaived");
                final BigDecimal penaltyChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWrittenOff");
                final BigDecimal penaltyChargesOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesOutstanding");
                final BigDecimal penaltyChargesOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesOverdue");



                final BigDecimal bounceChargesCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "bounceChargesCharged");
                final BigDecimal bounceChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "bounceChargesPaid");
                final BigDecimal bounceChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "bounceChargesWaived");
                final BigDecimal bounceChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "bounceChargesWrittenOff");
                final BigDecimal bounceChargesOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "bounceChargesOutstanding");
                final BigDecimal bounceChargesOverdue =bounceChargesOutstanding!=null?bounceChargesOutstanding:BigDecimal.ZERO;



                BigDecimal totalExpectedRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalExpectedRepayment");
                totalExpectedRepayment = BigDecimal.valueOf(totalExpectedRepayment.doubleValue());
                BigDecimal totalRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalRepayment");
                totalRepayment = BigDecimal.valueOf(totalRepayment.doubleValue());
                final BigDecimal totalSelfRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalSelfRepayment");
                final BigDecimal totalPartnerRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalPartnerRepayment");
                final BigDecimal totalExpectedCostOfLoan = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalExpectedCostOfLoan");
                final BigDecimal totalCostOfLoan = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalCostOfLoan");
                final BigDecimal totalWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalWaived");
                final BigDecimal totalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalWrittenOff");
                final BigDecimal totalOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOutstanding");
                final BigDecimal totalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOverdue");
                final BigDecimal totalRecovered = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalRecovered");
                final BigDecimal coolingOffReversedChargeAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "coolingOffReversedChargeAmount");

                final LocalDate overdueSinceDate = JdbcSupport.getLocalDate(rs, "overdueSinceDate");
                if (overdueSinceDate != null) {
                    inArrears = true;
                }

                loanSummary = new LoanSummaryData(currencyData, principalDisbursed, principalPaid, principalWrittenOff,
                        principalOutstanding, principalOverdue, interestCharged, interestPaid, interestWaived, interestWrittenOff,
                        interestOutstanding, interestOverdue, feeChargesCharged, feeChargesDueAtDisbursementCharged, feeChargesPaid,
                        feeChargesWaived, feeChargesWrittenOff, feeChargesOutstanding, feeChargesOverdue, penaltyChargesCharged,
                        penaltyChargesPaid, penaltyChargesWaived, penaltyChargesWrittenOff, penaltyChargesOutstanding,
                        penaltyChargesOverdue, totalExpectedRepayment, totalRepayment, totalExpectedCostOfLoan, totalCostOfLoan,
                        totalWaived, totalWrittenOff, totalOutstanding, totalOverdue, overdueSinceDate, writeoffReasonId, writeoffReason,
                        totalRecovered,totalSelfRepayment,totalPartnerRepayment,selfPrincipalPaid,partnerPrincipalPaid,
                        selfInterestPaid,partnerInterestPaid,selfInterestWaived,partnerInterestWaived,selfFeeChargesWaived,partnerFeeChargesWaived,bounceChargesCharged,bounceChargesPaid,bounceChargesOutstanding,bounceChargesWaived,bounceChargesWrittenOff,bounceChargesOverdue,
                        coolingOffReversedChargeAmount);
            }

            GroupGeneralData groupData = null;
            if (groupId != null) {
                final Integer groupStatusEnum = JdbcSupport.getInteger(rs, "statusEnum");
                final EnumOptionData groupStatus = ClientEnumerations.status(groupStatusEnum);
                final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
                groupData = GroupGeneralData.instance(groupId, groupAccountNo, groupName, groupExternalId, groupStatus, activationDate,
                        groupOfficeId, null, groupParentId, centerName, groupStaffId, null, groupHierarchy, groupLevel, null);
            }

            final Integer loanCounter = JdbcSupport.getInteger(rs, "loanCounter");
            final Integer loanProductCounter = JdbcSupport.getInteger(rs, "loanProductCounter");
            final BigDecimal fixedEmiAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "fixedEmiAmount");
            final Boolean isNPA = rs.getBoolean("isNPA");

            final int daysInMonth = JdbcSupport.getInteger(rs, "daysInMonth");
            final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(daysInMonth);
            final int daysInYear = JdbcSupport.getInteger(rs, "daysInYear");
            final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(daysInYear);
            final boolean isInterestRecalculationEnabled = rs.getBoolean("isInterestRecalculationEnabled");
            final Boolean createStandingInstructionAtDisbursement = rs.getBoolean("createStandingInstructionAtDisbursement");

            LoanInterestRecalculationData interestRecalculationData = null;
            if (isInterestRecalculationEnabled) {

                final Long lprId = JdbcSupport.getLong(rs, "lirId");
                final Long productId = JdbcSupport.getLong(rs, "loanId");
                final int compoundTypeEnumValue = JdbcSupport.getInteger(rs, "compoundType");
                final EnumOptionData interestRecalculationCompoundingType = LoanEnumerations
                        .interestRecalculationCompoundingType(compoundTypeEnumValue);
                final int rescheduleStrategyEnumValue = JdbcSupport.getInteger(rs, "rescheduleStrategy");
                final EnumOptionData rescheduleStrategyType = LoanEnumerations.rescheduleStrategyType(rescheduleStrategyEnumValue);
                final CalendarData calendarData = null;
                final int restFrequencyEnumValue = JdbcSupport.getInteger(rs, "restFrequencyEnum");
                final EnumOptionData restFrequencyType = LoanEnumerations.interestRecalculationFrequencyType(restFrequencyEnumValue);
                final int restFrequencyInterval = JdbcSupport.getInteger(rs, "restFrequencyInterval");
                final Integer restFrequencyNthDayEnumValue = JdbcSupport.getInteger(rs, "restFrequencyNthDayEnum");
                EnumOptionData restFrequencyNthDayEnum = null;
                if (restFrequencyNthDayEnumValue != null) {
                    restFrequencyNthDayEnum = LoanEnumerations.interestRecalculationCompoundingNthDayType(restFrequencyNthDayEnumValue);
                }
                final Integer restFrequencyWeekDayEnumValue = JdbcSupport.getInteger(rs, "restFrequencyWeekDayEnum");
                EnumOptionData restFrequencyWeekDayEnum = null;
                if (restFrequencyWeekDayEnumValue != null) {
                    restFrequencyWeekDayEnum = LoanEnumerations
                            .interestRecalculationCompoundingDayOfWeekType(restFrequencyWeekDayEnumValue);
                }
                final Integer restFrequencyOnDay = JdbcSupport.getInteger(rs, "restFrequencyOnDay");
                final CalendarData compoundingCalendarData = null;
                final Integer compoundingFrequencyEnumValue = JdbcSupport.getInteger(rs, "compoundingFrequencyEnum");
                EnumOptionData compoundingFrequencyType = null;
                if (compoundingFrequencyEnumValue != null) {
                    compoundingFrequencyType = LoanEnumerations.interestRecalculationFrequencyType(compoundingFrequencyEnumValue);
                }
                final Integer compoundingInterval = JdbcSupport.getInteger(rs, "compoundingInterval");
                final Integer compoundingFrequencyNthDayEnumValue = JdbcSupport.getInteger(rs, "compoundingFrequencyNthDayEnum");
                EnumOptionData compoundingFrequencyNthDayEnum = null;
                if (compoundingFrequencyNthDayEnumValue != null) {
                    compoundingFrequencyNthDayEnum = LoanEnumerations
                            .interestRecalculationCompoundingNthDayType(compoundingFrequencyNthDayEnumValue);
                }
                final Integer compoundingFrequencyWeekDayEnumValue = JdbcSupport.getInteger(rs, "compoundingFrequencyWeekDayEnum");
                EnumOptionData compoundingFrequencyWeekDayEnum = null;
                if (compoundingFrequencyWeekDayEnumValue != null) {
                    compoundingFrequencyWeekDayEnum = LoanEnumerations
                            .interestRecalculationCompoundingDayOfWeekType(compoundingFrequencyWeekDayEnumValue);
                }
                final Integer compoundingFrequencyOnDay = JdbcSupport.getInteger(rs, "compoundingFrequencyOnDay");

                final Boolean isCompoundingToBePostedAsTransaction = rs.getBoolean("isCompoundingToBePostedAsTransaction");
                final Boolean allowCompoundingOnEod = rs.getBoolean("allowCompoundingOnEod");
                interestRecalculationData = new LoanInterestRecalculationData(lprId, productId, interestRecalculationCompoundingType,
                        rescheduleStrategyType, calendarData, restFrequencyType, restFrequencyInterval, restFrequencyNthDayEnum,
                        restFrequencyWeekDayEnum, restFrequencyOnDay, compoundingCalendarData, compoundingFrequencyType,
                        compoundingInterval, compoundingFrequencyNthDayEnum, compoundingFrequencyWeekDayEnum, compoundingFrequencyOnDay,
                        isCompoundingToBePostedAsTransaction, allowCompoundingOnEod);
            }

            final boolean canUseForTopup = rs.getBoolean("canUseForTopup");
            final boolean isTopup = rs.getBoolean("isTopup");
            final Long closureLoanId = rs.getLong("closureLoanId");
            final String closureLoanAccountNo = rs.getString("closureLoanAccountNo");
            final BigDecimal topupAmount = rs.getBigDecimal("topupAmount");

            final  BigDecimal vclHurdleRate = rs.getBigDecimal("vclHurdleRate");

           return  LoanAccountData.basicLoanDetails(id, accountNo, status, externalId, clientId, clientAccountNo, clientName,
                    clientOfficeId, groupData, loanType, loanProductId, loanProductName, loanProductDescription,
                    isLoanProductLinkedToFloatingRate, fundId, fundName, loanPurposeId, loanPurposeName, loanOfficerId, loanOfficerName,
                    currencyData, proposedPrincipal, principal, approvedPrincipal, netDisbursalAmount, totalOverpaid, inArrearsTolerance,
                    termFrequency, termPeriodFrequencyType, numberOfRepayments, repaymentEvery, repaymentFrequencyType, null, null,
                    transactionStrategyId, transactionStrategyName, amortizationType, interestRatePerPeriod, interestRateFrequencyType,
                    annualInterestRate, interestType, isFloatingInterestRate, interestRateDifferential, interestCalculationPeriodType,
                    allowPartialPeriodInterestCalcualtion, expectedFirstRepaymentOnDate, graceOnPrincipalPayment,
                    recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate,
                    timeline, loanSummary, feeChargesDueAtDisbursementCharged, syncDisbursementWithMeeting, loanCounter, loanProductCounter,
                    multiDisburseLoan, canDefineInstallmentAmount, fixedEmiAmount, outstandingLoanBalance, inArrears, graceOnArrearsAgeing,
                    isNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled, interestRecalculationData,
                    createStandingInstructionAtDisbursement, isvariableInstallmentsAllowed, minimumGap, maximumGap,allowAgeLimits, loanSubStatus,
                    canUseForTopup, isTopup, closureLoanId, closureLoanAccountNo, topupAmount, isEqualAmortization,
                    fixedPrincipalPercentagePerInstallment,brokenInterestDeived,brokenInterestPaid,selfPrincipalAmount,
                    partnerPrincipalAmount,netSelfDisbursalAmount,netPartnerDisbursalAmount,xirrValue,
                    selfFeeChargesDueAtDisbursementCharged,partnerFeeChargesDueAtDisbursementCharged,vclHurdleRate);
        }
    }

    private static final class MusoniOverdueLoanScheduleMapper implements RowMapper<OverdueLoanScheduleData> {

        public String schema() {
            return " ls.loan_id as loanId, ls.installment as period, ls.fromdate as fromDate, ls.duedate as dueDate, ls.obligations_met_on_date as obligationsMetOnDate, ls.completed_derived as complete,"
                    + " ls.principal_amount as principalDue,ls.self_principal_amount as selfPrincipalDue,ls.partner_principal_amount as partnerPrincipalDue,ls.principal_completed_derived as principalPaid, ls.self_principal_completed_derived as selfPrincipalPaid,ls.partner_principal_completed_derived as partnerPrincipalPaid, ls.principal_writtenoff_derived as principalWrittenOff, "
                    + " ls.interest_amount as interestDue,ls.self_interest_amount as selfInterestDue,ls.partner_interest_amount as partnerInterestDue, ls.interest_completed_derived as interestPaid,ls.self_interest_completed_derived as selfInterestPaid,ls.partner_interest_completed_derived as partnerInterestPaid, ls.interest_waived_derived as interestWaived,ls.self_interest_waived_derived as selfInterestWaived,ls.partner_interest_waived_derived as partnerInterestWaived, ls.interest_writtenoff_derived as interestWrittenOff, ls.self_interest_writtenoff_derived as selfInterestWrittenOff, ls.partner_interest_writtenoff_derived as partnerInterestWrittenOff, "
                    + " ls.fee_charges_amount as feeChargesDue,ls.self_fee_charges_amount as selfFeeChargesDue,ls.partner_fee_charges_amount as partnerFeeChargesDue, ls.fee_charges_completed_derived as feeChargesPaid, ls.fee_charges_waived_derived as feeChargesWaived,ls.self_fee_charges_waived_derived as selfFeeChargesWaived,ls.partner_fee_charges_waived_derived as partnerFeeChargesWaived, ls.fee_charges_writtenoff_derived as feeChargesWrittenOff, "
                    + " ls.penalty_charges_amount as penaltyChargesDue, ls.penalty_charges_completed_derived as penaltyChargesPaid, ls.penalty_charges_waived_derived as penaltyChargesWaived, ls.penalty_charges_writtenoff_derived as penaltyChargesWrittenOff, "
                    + " ls.total_paid_in_advance_derived as totalPaidInAdvanceForPeriod, ls.total_paid_late_derived as totalPaidLateForPeriod,ls.self_total_paid_late_derived as totalSelfPaidLateForPeriod,ls.partner_total_paid_late_derived as totalPartnerPaidLateForPeriod, "
                    + " ls.self_principal_amount as selfPrincipal, ls.partner_principal_amount as partnerPrincipal, ls.self_interest_amount as selfInterestCharged,ls.partner_interest_amount as partnerInterestCharged, "
                    + " ls.self_due as selfDue,ls.partner_due as partnerDue, "
                    + " mc.amount,mc.id as chargeId, ml.grace_on_arrears_ageing as graceOnArrearsAgeing " + " from m_loan_repayment_schedule ls "
                    + " inner join m_loan ml on ml.id = ls.loan_id "
                    + " join m_product_loan_fees_charges plc on plc.product_loan_id = ml.product_id "
                    + " join m_charge mc on mc.id = plc.charge_id ";

        }

        @Override
        public OverdueLoanScheduleData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long chargeId = rs.getLong("chargeId");
            final Long loanId = rs.getLong("loanId");
            final BigDecimal amount = rs.getBigDecimal("amount");
            final String dateFormat = "yyyy-MM-dd";
            final String dueDate = rs.getString("dueDate");
            final String locale = "en_GB";

            final BigDecimal principalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDue");
//            final BigDecimal selfPrincipalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipalDue");
//            final BigDecimal partnerPrincipalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipalDue");

            final BigDecimal selfPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
            final BigDecimal partnerPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");

            final BigDecimal principalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalPaid");
            final BigDecimal selfPrincipalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipalPaid");
            final BigDecimal partnerPrincipalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipalPaid");

            final BigDecimal principalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalWrittenOff");

            final BigDecimal principalOutstanding = principalDue.subtract(principalPaid).subtract(principalWrittenOff);
            final BigDecimal selfPrincipalOutstanding = selfPrincipal.subtract(selfPrincipalPaid).subtract(principalWrittenOff);
            final BigDecimal partnerPrincipalOutstanding = partnerPrincipal.subtract(partnerPrincipalPaid).subtract(principalWrittenOff);


            final BigDecimal interestExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestDue");
            final BigDecimal interestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestPaid");
            final BigDecimal selfInterestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestPaid");
            final BigDecimal partnerInterestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestPaid");

            final BigDecimal interestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWaived");
            final BigDecimal selfInterestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestWaived");
            final BigDecimal partnerInterestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestWaived");
            final BigDecimal interestWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWrittenOff");

            final BigDecimal interestActualDue = interestExpectedDue.subtract(interestWaived).subtract(interestWrittenOff);
            final BigDecimal interestOutstanding = interestActualDue.subtract(interestPaid);

            final Integer installmentNumber = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "period");
            final Integer graceOnArrearsAgeing = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnArrearsAgeing");
            final OverdueLoanScheduleData overdueLoanScheduleData = new OverdueLoanScheduleData(loanId, chargeId, dueDate, amount,
                    dateFormat, locale, principalOutstanding, interestOutstanding, installmentNumber,selfPrincipalOutstanding,
                    partnerPrincipalOutstanding, graceOnArrearsAgeing);
//            final BigDecimal selfPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
//            final BigDecimal partnerPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");
//            final BigDecimal selfInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
//            final BigDecimal partnerInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");

            return overdueLoanScheduleData;
        }
    }

    private static final class LoanScheduleResultSetExtractor implements ResultSetExtractor<LoanScheduleData> {

        private final CurrencyData currency;
        private final DisbursementData disbursement;
        private final BigDecimal totalFeeChargesDueAtDisbursement;
        private final Collection<DisbursementData> disbursementData;
        private LocalDate lastDueDate;
        private BigDecimal outstandingLoanPrincipalBalance;
        private BigDecimal selfOutstandingPrincipalBalance;
        private BigDecimal partnerOutstandingPrincipalBalance;

        private boolean excludePastUndisbursed;
        private final BigDecimal totalPaidFeeCharges;

        private final BigDecimal totalSelfFeeChargesDueAtDisbursement;
        private final BigDecimal totalPartnerFeeChargesDueAtDisbursement;



        LoanScheduleResultSetExtractor(final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData,
                                       Collection<DisbursementData> disbursementData, boolean isInterestRecalculationEnabled, BigDecimal totalPaidFeeCharges) {
            this.currency = repaymentScheduleRelatedLoanData.getCurrency();
            this.disbursement = repaymentScheduleRelatedLoanData.disbursementData();
            this.totalFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalFeeChargesAtDisbursement();
            this.lastDueDate = this.disbursement.disbursementDate();
            this.outstandingLoanPrincipalBalance = this.disbursement.amount();
            this.disbursementData = disbursementData;
            this.excludePastUndisbursed = isInterestRecalculationEnabled;
            this.totalPaidFeeCharges = totalPaidFeeCharges;

            this.selfOutstandingPrincipalBalance=this.disbursement.getSelfPrincipalAmount();
            this.partnerOutstandingPrincipalBalance=this.disbursement.getPartnerPrincipalAmount();
            this.totalSelfFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalSelfFeeChargesAtDisbursement();
            this.totalPartnerFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalPartnerFeeChargesAtDisbursement();


        }

        public String schema() {

            return " ls.loan_id as loanId, ls.installment as period, ls.fromdate as fromDate, ls.duedate as dueDate, ls.obligations_met_on_date as obligationsMetOnDate, ls.completed_derived as complete,"
                    + " ls.principal_amount as principalDue,ls.self_principal_amount as selfPrincipalDue,ls.partner_principal_amount as partnerPrincipalDue, ls.principal_completed_derived as principalPaid,ls.self_principal_completed_derived as selfPrincipalPaid,ls.partner_principal_completed_derived as partnerPrincipalPaid, ls.principal_writtenoff_derived as principalWrittenOff,ls.self_principal_writtenoff_derived as selfPrincipalWrittenOff,ls.partner_principal_writtenoff_derived as partnerprincipalWrittenOff ,"
                    + " ls.self_principal_amount as selfPrincipal, ls.partner_principal_amount as partnerPrincipal, ls.self_interest_amount as selfInterestCharged,ls.partner_interest_amount as partnerInterestCharged, "
                    + " ls.self_due as selfDue, ls.partner_due as partnerDue, "
                    + " ls.interest_amount as interestDue,ls.self_interest_amount as selfInterestDue,ls.partner_interest_amount as partnerInterestDue, ls.interest_completed_derived as interestPaid,ls.self_interest_completed_derived as selfInterestPaid,ls.partner_interest_completed_derived as partnerInterestPaid, ls.interest_waived_derived as interestWaived,ls.self_interest_waived_derived as selfInterestWaived,ls.partner_interest_waived_derived as partnerInterestWaived, ls.interest_writtenoff_derived as interestWrittenOff,ls.self_interest_writtenoff_derived as selfInterestWrittenOff, ls.partner_interest_writtenoff_derived as partnerInterestWrittenOff, "
                    + " ls.fee_charges_amount as feeChargesDue,ls.self_fee_charges_amount as selfFeeChargesDue,ls.partner_fee_charges_amount as partnerFeeChargesDue, ls.fee_charges_completed_derived as feeChargesPaid,ls.self_fee_charges_completed_derived as selfFeeChargesPaid,ls.partner_fee_charges_completed_derived as partnerFeeChargesPaid, ls.fee_charges_waived_derived as feeChargesWaived,ls.self_fee_charges_waived_derived as selfFeeChargesWaived,ls.partner_fee_charges_waived_derived as partnerFeeChargesWaived, ls.fee_charges_writtenoff_derived as feeChargesWrittenOff, "
                    + " ls.penalty_charges_amount as penaltyChargesDue,ls.self_penalty_charges_amount as selfPenaltyChargesDue,ls.partner_penalty_charges_amount as partnerPenaltyChargesDue, ls.penalty_charges_completed_derived as penaltyChargesPaid, ls.penalty_charges_waived_derived as penaltyChargesWaived, ls.penalty_charges_writtenoff_derived as penaltyChargesWrittenOff, "
                    + " ls.total_paid_in_advance_derived as totalPaidInAdvanceForPeriod, ls.total_paid_late_derived as totalPaidLateForPeriod,ls.self_total_paid_late_derived as totalSelfPaidLateForPeriod,ls.partner_total_paid_late_derived as totalPartnerPaidLateForPeriod, ls.dpd as dpd, "
                    + " ls.self_penalty_charges_amount as selfPenaltyChargesAmount, ls.self_penalty_charges_completed_derived as selfPenaltyChargesCompletedDerived, ls.self_penalty_charges_waived_derived as selfPenaltyChargesWaivedDerived, "
                    + " ls.self_penalty_charges_writtenoff_derived as selfPenaltyChargesWrittenoffDerived, ls.partner_penalty_charges_amount as partnerPenaltyChargesAmount, ls.partner_penalty_charges_completed_derived as partnerPenaltyChargesCompletedDerived, "
                    + " ls.partner_penalty_charges_waived_derived as partnerPenaltyChargesWaivedDerived, ls.partner_penalty_charges_writtenoff_derived as partnerPenaltyChargesWrittenoffDerived, ls.dpd_bucket as dpdBucket,l.loan_status_id as loanStatus"
                    + " from m_loan l left join m_loan_repayment_schedule ls on ls.loan_id = l.id ";
        }

        @Override
        public LoanScheduleData extractData(final ResultSet rs) throws SQLException, DataAccessException {
            BigDecimal waivedChargeAmount = BigDecimal.ZERO;
            for (DisbursementData disbursementDetail : disbursementData) {
                waivedChargeAmount = waivedChargeAmount.add(disbursementDetail.getWaivedChargeAmount());
            }
            final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursementOnlyPeriod(
                    this.disbursement.disbursementDate(), this.disbursement.amount(), this.totalFeeChargesDueAtDisbursement,
                    this.disbursement.isDisbursed(),this.disbursement.getSelfPrincipalAmount(),this.disbursement.getPartnerPrincipalAmount(),
                    null,this.totalSelfFeeChargesDueAtDisbursement,this.totalPartnerFeeChargesDueAtDisbursement);

            final Collection<LoanSchedulePeriodData> periods = new ArrayList<>();
            final MonetaryCurrency monCurrency = new MonetaryCurrency(this.currency.code(), this.currency.decimalPlaces(),
                    this.currency.currencyInMultiplesOf());
            BigDecimal totalPrincipalDisbursed = BigDecimal.ZERO;
            BigDecimal disbursementChargeAmount = this.totalFeeChargesDueAtDisbursement;
            BigDecimal selfDisbursementChargeAmount = this.totalFeeChargesDueAtDisbursement;
            BigDecimal partnerDisbursementChargeAmount = this.totalPartnerFeeChargesDueAtDisbursement;
            if (disbursementData == null || disbursementData.isEmpty()) {
                periods.add(disbursementPeriod);
                totalPrincipalDisbursed = Money.of(monCurrency, this.disbursement.amount()).getAmount();
            } else {
                if (!this.disbursement.isDisbursed()) {
                    excludePastUndisbursed = false;
                }
                for (DisbursementData data : disbursementData) {
                    if (data.getChargeAmount() != null) {
                        disbursementChargeAmount = disbursementChargeAmount.subtract(data.getChargeAmount());
                    }
                }
                this.outstandingLoanPrincipalBalance = BigDecimal.ZERO;
            }

            Money totalPrincipalExpected = Money.zero(monCurrency);
            Money totalSelfPrincipalExpected = Money.zero(monCurrency);
            Money totalPartnerPrincipalExpected = Money.zero(monCurrency);

            Money totalPrincipalPaid = Money.zero(monCurrency);
            Money totalSelfPrincipalPaid = Money.zero(monCurrency);
            Money totalPartnerPrincipalPaid = Money.zero(monCurrency);
            Money totalInterestCharged = Money.zero(monCurrency);
            Money totalSelfInterestCharged = Money.zero(monCurrency);
            Money totalPartnerInterestCharged = Money.zero(monCurrency);
            Money totalSelfDue = Money.zero(monCurrency);
            Money totalPartnerDue = Money.zero(monCurrency);

            BigDecimal totalFeeChargesCharged = BigDecimal.ZERO;
            BigDecimal totalSelfFeeChargesCharged = BigDecimal.ZERO;
            BigDecimal totalPartnerFeeChargesCharged = BigDecimal.ZERO;
            Money totalPenaltyChargesCharged = Money.zero(monCurrency);
            Money totalSelfPenaltyChargesCharged = Money.zero(monCurrency);
            Money totalPartnerPenaltyChargesCharged = Money.zero(monCurrency);
            Money totalWaived = Money.zero(monCurrency);
            Money totalWrittenOff = Money.zero(monCurrency);
            BigDecimal totalRepaymentExpected = BigDecimal.ZERO;
            BigDecimal totalSelfRepaymentExpected = BigDecimal.ZERO;
            BigDecimal totalPartnerRepaymentExpected = BigDecimal.ZERO;

            BigDecimal totalRepayment = BigDecimal.ZERO;
            BigDecimal totalSelfRepayment=BigDecimal.ZERO;
            BigDecimal totalPartnerRepayment=BigDecimal.ZERO;
            Money totalPaidInAdvance = Money.zero(monCurrency);
           // Money selfTotalPaidInAdvance = Money.zero(monCurrency);
           // Money partnerTotalPaidInAdvance = Money.zero(monCurrency);
            Money totalPaidLate = Money.zero(monCurrency);
            Money selfTotalPaidLate = Money.zero(monCurrency);
            Money partnerTotalPaidLate = Money.zero(monCurrency);

            BigDecimal totalOutstanding =BigDecimal.ZERO;
            BigDecimal totalSelfOutstanding = BigDecimal.ZERO;
            BigDecimal totalPartnerOutstanding = BigDecimal.ZERO;



            // update totals with details of fees charged during disbursement
           // totalFeeChargesCharged = totalFeeChargesCharged.plus(disbursementPeriod.feeChargesDue().subtract(waivedChargeAmount));
            totalFeeChargesCharged = BigDecimal.valueOf(totalFeeChargesCharged.doubleValue() + disbursementPeriod.feeChargesDue().doubleValue() - waivedChargeAmount.doubleValue()).setScale(2, RoundingMode.HALF_UP);

          //  totalSelfFeeChargesCharged = totalSelfFeeChargesCharged.plus(disbursementPeriod.selfFeeChargesDue().subtract(waivedChargeAmount));
            totalSelfFeeChargesCharged = BigDecimal.valueOf(totalSelfFeeChargesCharged.doubleValue() + disbursementPeriod.selfFeeChargesDue().doubleValue() -waivedChargeAmount.doubleValue()).setScale(2,RoundingMode.HALF_UP);
//            totalPartnerFeeChargesCharged = totalPartnerFeeChargesCharged.plus(disbursementPeriod.partnerFeeChargesDue().subtract(waivedChargeAmount));
            totalPartnerFeeChargesCharged =BigDecimal.valueOf(totalPartnerFeeChargesCharged.doubleValue() + disbursementPeriod.partnerFeeChargesDue().doubleValue() - waivedChargeAmount.doubleValue()).setScale(2,RoundingMode.HALF_UP);
            totalRepaymentExpected = totalRepaymentExpected.add(disbursementPeriod.feeChargesDue()).subtract(waivedChargeAmount);
            totalSelfRepaymentExpected = totalSelfRepaymentExpected.add(disbursementPeriod.selfFeeChargesDue()).subtract(waivedChargeAmount);
            totalPartnerRepaymentExpected = totalPartnerRepaymentExpected.add(disbursementPeriod.partnerFeeChargesDue()).subtract(waivedChargeAmount);

            totalRepayment = totalRepayment.add(disbursementPeriod.feeChargesPaid()).subtract(waivedChargeAmount);
            totalSelfRepayment=totalSelfRepayment.add(disbursementPeriod.selfFeeChargesPaid()).subtract(waivedChargeAmount);
            totalPartnerRepayment=totalPartnerRepayment.add(disbursementPeriod.partnerFeeChargesPaid()).subtract(waivedChargeAmount);
//            totalOutstanding = totalOutstanding.add(disbursementPeriod.feeChargesDue()).add(disbursementPeriod.feeChargesPaid());
//            totalSelfOutstanding = totalSelfOutstanding.add(disbursementPeriod.selfFeeChargesDue()).add(disbursementPeriod.selfFeeChargesPaid());
//            totalPartnerOutstanding = totalPartnerOutstanding.add(disbursementPeriod.partnerFeeChargesDue()).add(disbursementPeriod.partnerFeeChargesPaid());

            totalOutstanding = totalOutstanding.add(disbursementPeriod.feeChargesOutstanding());
            totalSelfOutstanding = totalSelfOutstanding.add(disbursementPeriod.selfFeeChargesOutstanding());
            totalPartnerOutstanding = totalPartnerOutstanding.add(disbursementPeriod.partnerFeeChargesOutstanding());

            Integer loanTermInDays = Integer.valueOf(0);
            while (rs.next()) {

                final Long loanId = rs.getLong("loanId");
                final Integer period = JdbcSupport.getInteger(rs, "period");
                LocalDate fromDate = JdbcSupport.getLocalDate(rs, "fromDate");
                final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueDate");
                final LocalDate obligationsMetOnDate = JdbcSupport.getLocalDate(rs, "obligationsMetOnDate");
                final boolean complete = rs.getBoolean("complete");
                if (disbursementData != null) {
                    BigDecimal principal = BigDecimal.ZERO;
                    for (final DisbursementData data : disbursementData) {
                        if (fromDate.equals(this.disbursement.disbursementDate()) && data.disbursementDate().equals(fromDate)) {
                            principal = principal.add(data.amount());
                            LoanSchedulePeriodData periodData = null;
                            if (data.getChargeAmount() == null) {
                                periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(data.disbursementDate(), data.amount(),
                                        disbursementChargeAmount, data.isDisbursed(),data.getSelfPrincipalAmount(),
                                        data.getPartnerPrincipalAmount(), null,selfDisbursementChargeAmount,partnerDisbursementChargeAmount);
                            } else {
                                periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(data.disbursementDate(), data.amount(),
                                        disbursementChargeAmount.add(data.getChargeAmount()).subtract(waivedChargeAmount),
                                        data.isDisbursed(),data.getSelfPrincipalAmount(),data.getPartnerPrincipalAmount(), null,selfDisbursementChargeAmount,partnerDisbursementChargeAmount);
                            }
                            if (periodData != null) {
                                periods.add(periodData);
                            }
                            this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.add(data.amount());
                        } else if (data.isDueForDisbursement(fromDate, dueDate)) {
                            if (!excludePastUndisbursed || (excludePastUndisbursed && (data.isDisbursed()
                                    || !data.disbursementDate().isBefore(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()))))) {
                                principal = principal.add(data.amount());
                                LoanSchedulePeriodData periodData = null;
                                if (data.getChargeAmount() == null) {
                                    periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(data.disbursementDate(), data.amount(),
                                            BigDecimal.ZERO, data.isDisbursed(),data.getSelfPrincipalAmount(),data.getPartnerPrincipalAmount(),
                                            null,BigDecimal.ZERO,BigDecimal.ZERO);
                                } else {
                                    periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(data.disbursementDate(), data.amount(),
                                            data.getChargeAmount(), data.isDisbursed(),data.getSelfPrincipalAmount(),data.getPartnerPrincipalAmount(),
                                            null,data.getSelfChargeAmount(),data.getPartnerChargeAmount());
                                }
                                if (periodData != null) {
                                    periods.add(periodData);
                                }
                                this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.add(data.amount());
                            }
                        }
                    }
                    totalPrincipalDisbursed = totalPrincipalDisbursed.add(principal);
                }

                Integer daysInPeriod = Integer.valueOf(0);
                if (fromDate != null) {
                    daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(fromDate, dueDate));
                    loanTermInDays = Integer.valueOf(loanTermInDays.intValue() + daysInPeriod.intValue());
                }

                final BigDecimal principalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDue");
                totalPrincipalExpected = totalPrincipalExpected.plus(principalDue);
//                final BigDecimal selfPrincipalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipalDue");
//                totalSelfPrincipalExpected = totalSelfPrincipalExpected.plus(selfPrincipalDue);
//                final BigDecimal partnerPrincipalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipalDue");
//                totalPartnerPrincipalExpected = totalPartnerPrincipalExpected.plus(partnerPrincipalDue);

                final BigDecimal selfPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
                totalSelfPrincipalExpected = totalSelfPrincipalExpected.plus(selfPrincipal);
                final BigDecimal partnerPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");
                totalPartnerPrincipalExpected = totalPartnerPrincipalExpected.plus(partnerPrincipal);
                final BigDecimal principalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalPaid");
                totalPrincipalPaid = totalPrincipalPaid.plus(principalPaid);

                final BigDecimal selfPrincipalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipalPaid");
                totalSelfPrincipalPaid = totalSelfPrincipalPaid.plus(selfPrincipalPaid);

                final BigDecimal partnerPrincipalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipalPaid");
                totalPartnerPrincipalPaid = totalPartnerPrincipalPaid.plus(partnerPrincipalPaid);

                /*Getting the Advance Amount at each period leve */

                // written off Details
                final BigDecimal principalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalWrittenOff");
                final BigDecimal selfPrincipalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipalWrittenOff");
                final BigDecimal partnerPrincipalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipalWrittenOff");

                BigDecimal principalOutstanding = principalDue.subtract(principalPaid).subtract(principalWrittenOff);
                BigDecimal selfPrincipalOutstanding = selfPrincipal.subtract(selfPrincipalPaid).subtract(selfPrincipalWrittenOff);
                BigDecimal partnerPrincipalOutstanding = partnerPrincipal.subtract(partnerPrincipalPaid).subtract(partnerPrincipalWrittenOff);

                final BigDecimal interestExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestDue");
                totalInterestCharged = totalInterestCharged.plus(interestExpectedDue);
                final BigDecimal selfInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
                totalSelfInterestCharged = totalSelfInterestCharged.plus(selfInterestCharged);
                final BigDecimal partnerInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");
                totalPartnerInterestCharged = totalPartnerInterestCharged.plus(partnerInterestCharged);
                final BigDecimal selfDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfDue");
                totalSelfDue = totalSelfDue.plus(selfDue);
                final BigDecimal partnerDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerDue");
                totalPartnerDue = totalPartnerDue.plus(partnerDue);
                final BigDecimal interestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestPaid");
                final BigDecimal selfInterestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestPaid");
                final BigDecimal partnerInterestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestPaid");

                final BigDecimal interestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWaived");
                final BigDecimal selfInterestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestWaived");
                final BigDecimal partnerInterestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestWaived");
                final BigDecimal interestWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWrittenOff");
                final BigDecimal selfInterestWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWrittenOff");
                final BigDecimal partnerInterestWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWrittenOff");
                final BigDecimal totalInstallmentAmount = totalPrincipalPaid.zero().plus(principalDue).plus(interestExpectedDue)
                        .getAmount();

                final BigDecimal interestActualDue = interestExpectedDue.subtract(interestWaived).subtract(interestWrittenOff);
                final BigDecimal selfInterestActualDue = selfInterestCharged.subtract(selfInterestWaived).subtract(selfInterestWrittenOff);
                final BigDecimal partnerInterestActualDue = partnerInterestCharged.subtract(partnerInterestWaived).subtract(partnerInterestWrittenOff);


                //Interest Outstanding in repaymentSchedule Screen
                final BigDecimal interestOutstanding = interestActualDue.subtract(interestPaid);
                final BigDecimal selfInterestOutstanding = selfInterestActualDue.subtract(selfInterestPaid);
                final BigDecimal partnerInterestOutstanding = partnerInterestActualDue.subtract(partnerInterestPaid);

                final BigDecimal feeChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesDue");
                totalFeeChargesCharged = totalFeeChargesCharged.add(feeChargesExpectedDue);
                final BigDecimal selfFeeChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfFeeChargesDue");
                totalSelfFeeChargesCharged = totalSelfFeeChargesCharged.add(selfFeeChargesExpectedDue);
                final BigDecimal partnerFeeChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerFeeChargesDue");
                totalPartnerFeeChargesCharged = totalPartnerFeeChargesCharged.add(partnerFeeChargesExpectedDue);

                final BigDecimal feeChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesPaid");
                final BigDecimal selfFeeChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfFeeChargesPaid");
                final BigDecimal partnerFeeChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerFeeChargesPaid");
                final BigDecimal feeChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWaived");
                final BigDecimal selfFeeChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfFeeChargesWaived");
                final BigDecimal partnerFeeChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerFeeChargesWaived");

                final BigDecimal feeChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWrittenOff");
                final BigDecimal selfFeeChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWrittenOff");
                final BigDecimal partnerFeeChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWrittenOff");

                final BigDecimal feeChargesActualDue = feeChargesExpectedDue.subtract(feeChargesWaived).subtract(feeChargesWrittenOff);
                final BigDecimal selfFeeChargesActualDue = selfFeeChargesExpectedDue.subtract(selfFeeChargesWaived).subtract(selfFeeChargesWrittenOff);
                final BigDecimal partnerFeeChargesActualDue = partnerFeeChargesExpectedDue.subtract(partnerFeeChargesWaived).subtract(partnerFeeChargesWrittenOff);

                //At the time of ReapaymentSchedule screen

                final BigDecimal feeChargesOutstanding = feeChargesActualDue.subtract(feeChargesPaid);
                final BigDecimal selfFeeChargesOutstanding = selfFeeChargesActualDue.subtract(selfFeeChargesPaid);
                final BigDecimal partnerFeeChargesOutstanding = partnerFeeChargesActualDue.subtract(partnerFeeChargesPaid);

                final BigDecimal penaltyChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesDue");
                totalPenaltyChargesCharged = totalPenaltyChargesCharged.plus(penaltyChargesExpectedDue);
                final BigDecimal selfPenaltyChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPenaltyChargesDue");
                totalSelfPenaltyChargesCharged = totalSelfPenaltyChargesCharged.plus(selfPenaltyChargesExpectedDue);
                final BigDecimal partnerPenaltyChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPenaltyChargesDue");
                totalPartnerPenaltyChargesCharged = totalPartnerPenaltyChargesCharged.plus(partnerPenaltyChargesExpectedDue);


                final BigDecimal penaltyChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesPaid");
                final BigDecimal penaltyChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWaived");
                final BigDecimal penaltyChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWrittenOff");

                final BigDecimal selfPenaltyChargesAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPenaltyChargesAmount");
                final BigDecimal selfPenaltyChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPenaltyChargesCompletedDerived");
                final BigDecimal selfPenaltyChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPenaltyChargesWaivedDerived");
                final BigDecimal selfPenaltyChargesWrittenoff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPenaltyChargesWrittenoffDerived");


                final BigDecimal partnerPenaltyChargesAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPenaltyChargesAmount");
                final BigDecimal partnerPenaltyChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPenaltyChargesCompletedDerived");
                final BigDecimal partnerPenaltyChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPenaltyChargesWaivedDerived");
                final BigDecimal partnerPenaltyChargesWrittenoff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPenaltyChargesWrittenoffDerived");


                final BigDecimal totalPaidInAdvanceForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                        "totalPaidInAdvanceForPeriod");

//                final BigDecimal totalSelfPaidInAdvanceForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
//                        "totalSelfPaidInAdvanceForPeriod");

               // final BigDecimal totalSelfPaidInAdvanceForPeriod = BigDecimal.ZERO;

//                final BigDecimal totalPartnerPaidInAdvanceForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
//                        "totalPartnerPaidInAdvanceForPeriod");

         //       final BigDecimal totalPartnerPaidInAdvanceForPeriod = BigDecimal.ZERO;
                final BigDecimal totalPaidLateForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalPaidLateForPeriod");
                final BigDecimal totalSelfPaidLateForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalSelfPaidLateForPeriod");
                final BigDecimal totalPartnerPaidLateForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalPartnerPaidLateForPeriod");
                final Integer daysPastDue = JdbcSupport.getInteger(rs, "dpd");
                final BigDecimal penaltyChargesActualDue = penaltyChargesExpectedDue.subtract(penaltyChargesWaived)
                        .subtract(penaltyChargesWrittenOff);
                final BigDecimal selfPenaltyChargesActualDue = selfPenaltyChargesExpectedDue.subtract(selfPenaltyChargesWaived)
                        .subtract(selfPenaltyChargesWrittenoff);
                final BigDecimal partnerPenaltyChargesActualDue = partnerPenaltyChargesExpectedDue.subtract(partnerPenaltyChargesWaived)
                        .subtract(partnerPenaltyChargesWrittenoff);

                final BigDecimal penaltyChargesOutstanding = penaltyChargesActualDue.subtract(penaltyChargesPaid);
                final BigDecimal selfPenaltyChargesOutstanding = selfPenaltyChargesActualDue.subtract(selfPenaltyChargesPaid);
                final BigDecimal partnerPenaltyChargesOutstanding = partnerPenaltyChargesActualDue.subtract(partnerPenaltyChargesPaid);


                final BigDecimal totalExpectedCostOfLoanForPeriod = interestExpectedDue.add(feeChargesExpectedDue);

                final BigDecimal totalSelfExpectedCostOfLoanForPeriod = selfInterestActualDue.add(selfFeeChargesActualDue);

                final BigDecimal totalPartnerExpectedCostOfLoanForPeriod = partnerInterestActualDue.add(partnerFeeChargesActualDue);

                final BigDecimal totalDueForPeriod = principalDue.add(totalExpectedCostOfLoanForPeriod);
                final BigDecimal totalSelfDueForPeriod = selfPrincipal.add(totalSelfExpectedCostOfLoanForPeriod);
                final BigDecimal totalPartnerDueForPeriod = partnerPrincipal.add(totalPartnerExpectedCostOfLoanForPeriod);
                final BigDecimal totalPaidForPeriod = principalPaid.add(interestPaid).add(feeChargesPaid).add(penaltyChargesPaid);
                final BigDecimal totalSelfPaidForPeriod=selfPrincipalPaid.add(selfInterestPaid).add(selfFeeChargesPaid).add(penaltyChargesPaid);
                final BigDecimal totalpartnerPaidForPeriod=partnerPrincipalPaid.add(partnerInterestPaid).add(partnerFeeChargesPaid).add(penaltyChargesPaid);
                final BigDecimal totalWaivedForPeriod = interestWaived.add(feeChargesWaived).add(penaltyChargesWaived);
                totalWaived = totalWaived.plus(totalWaivedForPeriod);
                final BigDecimal totalWrittenOffForPeriod = principalWrittenOff.add(interestWrittenOff).add(feeChargesWrittenOff)
                        .add(penaltyChargesWrittenOff);
                totalWrittenOff = totalWrittenOff.plus(totalWrittenOffForPeriod);
                final BigDecimal totalOutstandingForPeriod = principalOutstanding.add(interestOutstanding).add(feeChargesOutstanding)
                        .add(penaltyChargesOutstanding);
                final BigDecimal totalSelfOutstandingForPeriod = selfPrincipalOutstanding.add(selfInterestOutstanding).add(selfFeeChargesOutstanding)
                        .add(selfPenaltyChargesOutstanding);
                final BigDecimal totalpartnerOutstandingForPeriod = partnerPrincipalOutstanding.add(partnerInterestOutstanding).add(partnerFeeChargesOutstanding)
                        .add(partnerPenaltyChargesOutstanding);

                final BigDecimal totalActualCostOfLoanForPeriod = interestActualDue.add(feeChargesActualDue).add(penaltyChargesActualDue);
                final BigDecimal totalSelfActualCostOfLoanForPeriod =selfInterestActualDue.add(selfInterestActualDue).add(penaltyChargesActualDue);
                final BigDecimal totalPartnerfActualCostOfLoanForPeriod =partnerInterestActualDue.add(partnerInterestActualDue).add(penaltyChargesActualDue);
//                final BigDecimal selfPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
//                final BigDecimal partnerPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");
//                final BigDecimal selfInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
//                final BigDecimal partnerInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");
//                final BigDecimal selfDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfDue");
//                final BigDecimal partnerDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerDue");




                totalRepaymentExpected = totalRepaymentExpected.add(totalDueForPeriod);
                totalSelfRepaymentExpected = totalSelfRepaymentExpected.add(totalSelfDueForPeriod);
                totalPartnerRepaymentExpected = totalPartnerRepaymentExpected.add(totalPartnerDueForPeriod);
                totalRepayment = totalRepayment.add(totalPaidForPeriod);
                totalSelfRepayment=totalSelfRepayment.add(totalSelfPaidForPeriod);
                totalPartnerRepayment=totalPartnerRepayment.add(totalpartnerPaidForPeriod);
                totalPaidInAdvance = totalPaidInAdvance.plus(totalPaidInAdvanceForPeriod);
             //   selfTotalPaidInAdvance = selfTotalPaidInAdvance.plus(totalSelfPaidInAdvanceForPeriod);
             //   partnerTotalPaidInAdvance = partnerTotalPaidInAdvance.plus(totalPartnerPaidInAdvanceForPeriod);
                totalPaidLate = totalPaidLate.plus(totalPaidLateForPeriod);
                selfTotalPaidLate = selfTotalPaidLate.plus(totalSelfPaidLateForPeriod);
                partnerTotalPaidLate = partnerTotalPaidLate.plus(totalPartnerPaidLateForPeriod);

                totalOutstanding = totalOutstanding.add(totalOutstandingForPeriod);
                totalSelfOutstanding = totalSelfOutstanding.add(totalSelfOutstandingForPeriod);
                totalPartnerOutstanding = totalPartnerOutstanding.add(totalpartnerOutstandingForPeriod);

                if (fromDate == null) {
                    fromDate = this.lastDueDate;
                }
                BigDecimal outstandingPrincipalBalanceOfLoan = this.outstandingLoanPrincipalBalance.subtract(principalDue);
                BigDecimal outstandingSelfPrincipalBalanceOfLoan = this.selfOutstandingPrincipalBalance.subtract(selfPrincipal);
                BigDecimal outstandingPartnerPrincipalBalanceOfLoan = this.partnerOutstandingPrincipalBalance.subtract(partnerPrincipal);

                // update based on current period values
                this.lastDueDate = dueDate;
                this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.subtract(principalDue);
                this.selfOutstandingPrincipalBalance=this.selfOutstandingPrincipalBalance.subtract(selfPrincipal);
                this.partnerOutstandingPrincipalBalance=this.partnerOutstandingPrincipalBalance.subtract(partnerPrincipal);

//                final BigDecimal selfPenaltyChargesAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPenaltyChargesAmount");
//                final BigDecimal selfPenaltyChargesCompletedDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPenaltyChargesCompletedDerived");
//                final BigDecimal selfPenaltyChargesWaivedDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPenaltyChargesWaivedDerived");
//                final BigDecimal selfPenaltyChargesWrittenoffDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPenaltyChargesWrittenoffDerived");
//                final BigDecimal partnerPenaltyChargesAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPenaltyChargesAmount");
//                final BigDecimal partnerPenaltyChargesCompletedDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPenaltyChargesCompletedDerived");
//                final BigDecimal partnerPenaltyChargesWaivedDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPenaltyChargesWaivedDerived");
//                final BigDecimal partnerPenaltyChargesWrittenoffDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPenaltyChargesWrittenoffDerived");
                  final String dpdBucket = Objects.requireNonNullElse(rs.getString("dpdBucket"), StringUtils.EMPTY);

                final Integer loanStatus = rs.getInt( "loanStatus");

                // for cooling  off Handling the principal Outstanding and Making as Zero Last Installment only need to make Zero as of now not checked last installment
                if(LoanStatus.fromInt(loanStatus).isLoanCoolingOff()){
                    outstandingPrincipalBalanceOfLoan = BigDecimal.ZERO;
                    outstandingSelfPrincipalBalanceOfLoan = BigDecimal.ZERO;
                    outstandingPartnerPrincipalBalanceOfLoan = BigDecimal.ZERO;
                }


                final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.repaymentPeriodWithPayments(loanId, period, fromDate,
                        dueDate, obligationsMetOnDate, complete, principalDue, principalPaid, principalWrittenOff, principalOutstanding,
                        outstandingPrincipalBalanceOfLoan, interestExpectedDue, interestPaid, interestWaived, interestWrittenOff,
                        interestOutstanding, feeChargesExpectedDue, feeChargesPaid, feeChargesWaived, feeChargesWrittenOff,
                        feeChargesOutstanding, penaltyChargesExpectedDue, penaltyChargesPaid, penaltyChargesWaived,
                        penaltyChargesWrittenOff, penaltyChargesOutstanding, totalDueForPeriod, totalPaidForPeriod,
                        totalPaidInAdvanceForPeriod, totalPaidLateForPeriod, totalWaivedForPeriod, totalWrittenOffForPeriod,
                        totalOutstandingForPeriod, totalActualCostOfLoanForPeriod, totalInstallmentAmount,selfPrincipal,partnerPrincipal,selfInterestCharged,partnerInterestCharged,totalSelfDueForPeriod,totalPartnerDueForPeriod,outstandingSelfPrincipalBalanceOfLoan,outstandingPartnerPrincipalBalanceOfLoan,
						totalSelfPaidForPeriod,totalpartnerPaidForPeriod,totalSelfPaidLateForPeriod,totalPartnerPaidLateForPeriod,totalSelfOutstandingForPeriod,totalpartnerOutstandingForPeriod, daysPastDue,
                        selfPenaltyChargesAmount, selfPenaltyChargesPaid, selfPenaltyChargesWrittenoff, selfPenaltyChargesWaived,
                        partnerPenaltyChargesAmount, partnerPenaltyChargesPaid,
                        partnerPenaltyChargesWrittenoff, partnerPenaltyChargesWaived,
                        selfFeeChargesOutstanding,partnerFeeChargesOutstanding,selfFeeChargesExpectedDue,
                        partnerFeeChargesExpectedDue,selfFeeChargesPaid,partnerFeeChargesPaid,selfFeeChargesWaived,
                        partnerFeeChargesWaived,selfFeeChargesWrittenOff,partnerFeeChargesWrittenOff,selfPenaltyChargesOutstanding,
                        partnerPenaltyChargesOutstanding,dpdBucket);

                periods.add(periodData);
            }

            return new LoanScheduleData(this.currency, periods, loanTermInDays, totalPrincipalDisbursed, totalPrincipalExpected.getAmount(),
                    totalPrincipalPaid.getAmount(), totalInterestCharged.getAmount(), totalFeeChargesCharged,
                    totalPenaltyChargesCharged.getAmount(), totalWaived.getAmount(), totalWrittenOff.getAmount(),
                    totalRepaymentExpected, totalRepayment, totalPaidInAdvance.getAmount(),
                    totalPaidLate.getAmount(), totalOutstanding,totalSelfRepaymentExpected,totalPartnerRepaymentExpected,
                    totalSelfPrincipalExpected.getAmount(),totalPartnerPrincipalExpected.getAmount(),totalSelfInterestCharged.getAmount(),totalPartnerInterestCharged.getAmount(),
                    totalSelfDue.getAmount(),totalPartnerDue.getAmount(),totalSelfRepayment,totalPartnerRepayment,selfTotalPaidLate.getAmount(),partnerTotalPaidLate.getAmount(),
                    totalSelfOutstanding,totalPartnerOutstanding,totalSelfFeeChargesCharged,totalPartnerFeeChargesCharged);
        }

    }

    private static final class LoanTransactionsMapper implements RowMapper<LoanTransactionData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        LoanTransactionsMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String loanPaymentsSchema() {

            return " tr.id as id, tr.transaction_type_enum as transactionType, tr.transaction_date as " + sqlGenerator.escape("date")
                    + ", tr.amount as total, " + " tr.principal_portion_derived as principal,tr.self_principal_portion_derived as selfPrincipal,tr.partner_principal_portion_derived as partnerPrincipal, tr.interest_portion_derived as interest,tr.self_interest_portion_derived as selfInterestCharged,tr.partner_interest_portion_derived as partnerInterestCharged,tr.self_amount as selfDue,tr.partner_amount as partnerDue, "
                    + " tr.fee_charges_portion_derived as fees,tr.self_fee_charges_portion_derived as selfFee,tr.partner_fee_charges_portion_derived as partnerFee, tr.penalty_charges_portion_derived as penalties, "
                    + " tr.overpayment_portion_derived as overpayment, tr.outstanding_loan_balance_derived as outstandingLoanBalance,tr.self_outstanding_loan_balance_derived as selfOutstandingLoanBalance,tr.partner_outstanding_loan_balance_derived as partnerOutstandingLoanBalance,tr.receipt_reference_number as receiptReferenceNumber,tr.partner_transfer_utr as partnerTransferUtr,tr.partner_transfer_date as partnerTransferDate,tr.advance_amount as advanceAmount,tr.bounce_charges_portion_derived as bounceChargesPortionDerived,tr.self_bounce_charges_portion_derived as selfBounceChargesPortionDerived,tr.partner_bounce_charges_portion_derived as partnerBounceChargesPortionDerived, "
                    + " tr.unrecognized_income_portion as unrecognizedIncome," + " tr.submitted_on_date as submittedOnDate,tr.value_date as valueDate, "
                    + " tr.manually_adjusted_or_reversed as manuallyReversed,tr.repayment_mode_cv_id as repaymentModeId,cvrp.code_value as repaymentModeValue, "
                    + " pd.payment_type_id as paymentType,pd.account_number as accountNumber,pd.check_number as checkNumber, "
                    + " pd.receipt_number as receiptNumber, pd.bank_number as bankNumber,pd.routing_code as routingCode, l.net_disbursal_amount as netDisbursalAmount, "
                    + " l.currency_code as currencyCode, l.currency_digits as currencyDigits, l.currency_multiplesof as inMultiplesOf, rc."
                    + sqlGenerator.escape("name") + " as currencyName, "
                    + " rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, "
                    + " pt.value as paymentTypeName, tr.external_id as externalId, tr.office_id as officeId, office.name as officeName, "
                    + " fromtran.id as fromTransferId, fromtran.is_reversed as fromTransferReversed,"
                    + " fromtran.transaction_date as fromTransferDate, fromtran.amount as fromTransferAmount,"
                    + " fromtran.description as fromTransferDescription,"
                    + " totran.id as toTransferId, totran.is_reversed as toTransferReversed,"
                    + " totran.transaction_date as toTransferDate, totran.amount as toTransferAmount,l.loan_status_id as loanStatus,"
                    + " totran.description as toTransferDescription " + " from m_loan l join m_loan_transaction tr on tr.loan_id = l.id"
                    + " join m_currency rc on rc." + sqlGenerator.escape("code") + " = l.currency_code "
                    + " left JOIN m_payment_detail pd ON tr.payment_detail_id = pd.id"
                    + " left join m_payment_type pt on pd.payment_type_id = pt.id" + " left join m_office office on office.id=tr.office_id"
                    + " left join m_account_transfer_transaction fromtran on fromtran.from_loan_transaction_id = tr.id "
                    + " left join m_account_transfer_transaction totran on totran.to_loan_transaction_id = tr.id "
                    + " left join m_code_value cvrp on cvrp.id =tr.repayment_mode_cv_id ";
        }

        @Override
        public LoanTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final Long id = rs.getLong("id");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
            final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(transactionTypeInt);
            final boolean manuallyReversed = rs.getBoolean("manuallyReversed");

            PaymentDetailData paymentDetailData = null;

            if (transactionType.isPaymentOrReceipt()) {
                final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentType");
                if (paymentTypeId != null) {
                    final String typeName = rs.getString("paymentTypeName");
                    final PaymentTypeData paymentType = PaymentTypeData.instance(paymentTypeId, typeName);
                    final String accountNumber = rs.getString("accountNumber");
                    final String checkNumber = rs.getString("checkNumber");
                    final String routingCode = rs.getString("routingCode");
                    final String receiptNumber = rs.getString("receiptNumber");
                    final String bankNumber = rs.getString("bankNumber");
                    paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                            bankNumber);
                }
            }
            final LocalDate date = JdbcSupport.getLocalDate(rs, "date");
            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final BigDecimal totalAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "total");
            final BigDecimal principalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principal");
            final BigDecimal selfPrincipalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
            final BigDecimal partnerPrincipalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");
            final BigDecimal interestPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interest");
            final BigDecimal selfInterestPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
            final BigDecimal partnerInterestPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");
            final BigDecimal selfDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfDue");
            final BigDecimal partnerDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerDue");

            final BigDecimal feeChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fees");
            final BigDecimal selfFeeChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfFee");
            final BigDecimal partnerFeeChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerFee");
            final BigDecimal penaltyChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penalties");
            final BigDecimal overPaymentPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "overpayment");
            final BigDecimal unrecognizedIncomePortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "unrecognizedIncome");
             BigDecimal outstandingLoanBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "outstandingLoanBalance");
             BigDecimal selfOutstandingLoanBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfOutstandingLoanBalance");
             BigDecimal partnerOutstandingLoanBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerOutstandingLoanBalance");
            final BigDecimal advanceAmount= JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,"advanceAmount");
            final BigDecimal bounceChargesPortionDerived= JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,"bounceChargesPortionDerived");
            final BigDecimal selfBounceChargesPortionDerived= JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,"selfBounceChargesPortionDerived");
            final BigDecimal partnerBounceChargesPortionDerived= JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,"partnerBounceChargesPortionDerived");

            final BigDecimal foreClosureAmount=BigDecimal.ZERO;
            final Integer installmentNumber =0;
             final Long repaymentModeId = JdbcSupport.getLong(rs, "repaymentModeId");
             final String repaymentModeValue = rs.getString("repaymentModeValue");
             final CodeValueData repaymentMode = CodeValueData.instance(repaymentModeId, repaymentModeValue);
            final List<CodeValueData> repaymentModeOptions=new ArrayList<>();



            final String externalId = rs.getString("externalId");

            final BigDecimal netDisbursalAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "netDisbursalAmount");

            AccountTransferData transfer = null;
            final Long fromTransferId = JdbcSupport.getLong(rs, "fromTransferId");
            final Long toTransferId = JdbcSupport.getLong(rs, "toTransferId");
            if (fromTransferId != null) {
                final LocalDate fromTransferDate = JdbcSupport.getLocalDate(rs, "fromTransferDate");
                final BigDecimal fromTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fromTransferAmount");
                final boolean fromTransferReversed = rs.getBoolean("fromTransferReversed");
                final String fromTransferDescription = rs.getString("fromTransferDescription");

                transfer = AccountTransferData.transferBasicDetails(fromTransferId, currencyData, fromTransferAmount, fromTransferDate,
                        fromTransferDescription, fromTransferReversed);
            } else if (toTransferId != null) {
                final LocalDate toTransferDate = JdbcSupport.getLocalDate(rs, "toTransferDate");
                final BigDecimal toTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "toTransferAmount");
                final boolean toTransferReversed = rs.getBoolean("toTransferReversed");
                final String toTransferDescription = rs.getString("toTransferDescription");

                transfer = AccountTransferData.transferBasicDetails(toTransferId, currencyData, toTransferAmount, toTransferDate,
                        toTransferDescription, toTransferReversed);
            }

            LocalDate valueDate = JdbcSupport.getLocalDate(rs,"valueDate");

            final Integer loanStatus = rs.getInt("loanStatus");

            if(loanStatus.equals(LoanStatus.COOLING_OFF.getValue()) && transactionType.isCoolingOff()){
                outstandingLoanBalance = BigDecimal.ZERO;
                selfOutstandingLoanBalance =BigDecimal.ZERO;
                partnerOutstandingLoanBalance = BigDecimal.ZERO;

            }

            LoanTransactionData loanTransactionData =  new LoanTransactionData(id, officeId, officeName, transactionType, paymentDetailData, currencyData, date, totalAmount,
                    netDisbursalAmount, principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion, overPaymentPortion,
                    unrecognizedIncomePortion, externalId, transfer, null, outstandingLoanBalance, submittedOnDate,
                    manuallyReversed,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,
                    selfDue,partnerDue,selfOutstandingLoanBalance,partnerOutstandingLoanBalance,advanceAmount,foreClosureAmount,
                    selfFeeChargesPortion,partnerFeeChargesPortion,installmentNumber,repaymentModeOptions,repaymentMode);

            loanTransactionData.setValueDate(valueDate);
            loanTransactionData.setBounceChargesPortionDerived(bounceChargesPortionDerived);
            loanTransactionData.setSelfBounceChargesPortionDerived(selfBounceChargesPortionDerived);
            loanTransactionData.setPartnerBounceChargesPortionDerived(partnerBounceChargesPortionDerived);

            return  loanTransactionData;
        }
    }

    @Override
    public LoanAccountData retrieveLoanProductDetailsTemplate(final Long productId, final Long clientId, final Long groupId) {

        this.context.authenticatedUser();

        final LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
        final Collection<EnumOptionData> loanTermFrequencyTypeOptions = this.loanDropdownReadPlatformService
                .retrieveLoanTermFrequencyTypeOptions();
        final Collection<EnumOptionData> repaymentFrequencyTypeOptions = this.loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyTypeOptions();
        final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = this.loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyOptionsForNthDayOfMonth();
        final Collection<EnumOptionData> repaymentFrequencyDaysOfWeekTypeOptions = this.loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyOptionsForDaysOfWeek();
        final Collection<EnumOptionData> interestRateFrequencyTypeOptions = this.loanDropdownReadPlatformService
                .retrieveInterestRateFrequencyTypeOptions();
        final Collection<EnumOptionData> amortizationTypeOptions = this.loanDropdownReadPlatformService
                .retrieveLoanAmortizationTypeOptions();
        Collection<EnumOptionData> interestTypeOptions = null;
        if (loanProduct.isLinkedToFloatingInterestRates()) {
            interestTypeOptions = Arrays.asList(interestType(InterestMethod.DECLINING_BALANCE));
        } else {
            interestTypeOptions = this.loanDropdownReadPlatformService.retrieveLoanInterestTypeOptions();
        }
        final Collection<EnumOptionData> interestCalculationPeriodTypeOptions = this.loanDropdownReadPlatformService
                .retrieveLoanInterestRateCalculatedInPeriodOptions();
        final Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = this.loanDropdownReadPlatformService
                .retreiveTransactionProcessingStrategies();
        final Collection<CodeValueData> loanPurposeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanPurpose");
        final Collection<CodeValueData> loanCollateralOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode("LoanCollateral");
          Collection<ChargeData> chargeOptions =null;
          Collection<ColendingChargeData> chargeOption=this.chargeReadPlatformService.retrieveColendingOtherCharges(productId);
        Collection<ColendingChargeData> foreClosureCharge=this.chargeReadPlatformService.retrieveColendingLoanForeclosureCharge(productId);
        Collection<ColendingChargeData> bounceCharge = this.chargeReadPlatformService.retrieveColendingLoanBounceCharge(productId);
        final Partner partner = null;
    //  Collection<ColendingChargeData> chargeOption =retrieveColendingCharge(colendingCharge);
  //      Collection<ChargeData> chargeOptions = null;
//        if (loanProduct.getMultiDisburseLoan()) {
//            chargeOptions = this.chargeReadPlatformService.retrieveLoanProductApplicableCharges(productId,
//                    new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT });
//        } else {
//            chargeOptions = this.chargeReadPlatformService.retrieveLoanProductApplicableCharges(productId,
//                    new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT, ChargeTimeType.TRANCHE_DISBURSEMENT });
//
//   //        chargeOptions=this.chargeReadPlatformService.retrieveColendingCharge(productId);
//        }



        Integer loanCycleCounter = null;
        if (loanProduct.useBorrowerCycle()) {
            if (clientId == null) {
                loanCycleCounter = retriveLoanCounter(groupId, AccountType.GROUP.getValue(), loanProduct.getId());
            } else {
                loanCycleCounter = retriveLoanCounter(clientId, loanProduct.getId());
            }
        }

        Collection<LoanAccountSummaryData> activeLoanOptions = null;
        if (loanProduct.canUseForTopup() && clientId != null) {
            activeLoanOptions = this.accountDetailsReadPlatformService.retrieveClientActiveLoanAccountSummary(clientId);
        } else if (loanProduct.canUseForTopup() && groupId != null) {
            activeLoanOptions = this.accountDetailsReadPlatformService.retrieveGroupActiveLoanAccountSummary(groupId);
        }

        return LoanAccountData.loanProductWithTemplateDefaults(loanProduct, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions,
                repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDaysOfWeekTypeOptions, repaymentStrategyOptions,
                interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions, interestCalculationPeriodTypeOptions,
                fundOptions, chargeOptions, loanPurposeOptions, loanCollateralOptions, loanCycleCounter, activeLoanOptions,chargeOption,foreClosureCharge, bounceCharge);
    }

    public Collection<ColendingChargeData> retrieveColendingCharge(Collection<LoanProductFeesChargesData> colendingCharge) {

        final List<ColendingChargeData> colendingChargeData = new ArrayList<>();

        for(LoanProductFeesChargesData loanProductFeesChargesData:colendingCharge)
        {
            final Long id = Long.valueOf(loanProductFeesChargesData.getChargeId());
            final Charge charge =this.chargeRepositoryWrapper.findOneWithNotFoundDetection(id);
            final PaymentType paymentTypeData=charge.getPaymentType();
            final String currencyCode =charge.getCurrencyCode();
            final BigDecimal amount =charge.getAmount();
            final Integer feeOnMonthDay= charge.getFeeOnMonth();
            final BigDecimal selfShare=loanProductFeesChargesData.getSelfCharge();
            final BigDecimal partnerShare =loanProductFeesChargesData.getPartnerCharge();
            final ChargePaymentMode chargePayment= ChargePaymentMode.fromInt(charge.getChargePaymentMode());
            final EnumOptionData chargePaymentMode= ChargeEnumerations.chargePaymentMode(chargePayment);
            final ChargeCalculationType chargeCalculation=ChargeCalculationType.fromInt(charge.getChargeCalculation());
            final EnumOptionData chargeCalculationType=ChargeEnumerations.chargeCalculationType(chargeCalculation);
            final ChargeTimeType chargeTime =ChargeTimeType.fromInt(charge.getChargeTimeType());
            final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);
            final String currency =charge.getCurrencyCode();
            final CurrencyData currencyData= this.currencyReadPlatformService.retrieveCurrency(currency);
            final List<EnumOptionData> enumOptionData=this.chargeDropdownReadPlatformService.retrieveCalculationTypes();

            ColendingChargeData colendingCharges =new ColendingChargeData(charge.getId(),charge.getName(),charge.isActive(),charge.isPenalty(),charge.isEnableFreeWithdrawal(),charge.getFreeWithdrawalFrequency(),charge.getRestartFrequency(),charge.getRestartFrequencyEnum(),charge.isEnablePaymentType(),null,currencyData,
                    amount,chargeTimeType,null,chargeCalculationType,chargePaymentMode,charge.getFeeOnMonthDay(),charge.getFeeInterval(),charge.getMinCap(),charge.getMaxCap(),null,null,null,null,null,null,null,
                    null,null, null,null,null,null,null,null,null,null,null,
                    null,null,null,null,null,selfShare,partnerShare);

            colendingChargeData.add(colendingCharges);

        }
        return colendingChargeData;


    }

    @Override
    public Collection<XIRRTransactionUpdateRecord> getUnpaidDuesForXIRR() {
        final RowMapper<XIRRTransactionUpdateRecord> XIRRTransactionUpdateRecordMapper = (final ResultSet rs, final int rownum) ->
                new XIRRTransactionUpdateRecord(rs.getLong("id"),
                        rs.getLong("loan_id"),
                        LocalDate.parse(rs.getString("duedate")));

        final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("SELECT cl.id, ls.loan_id, ls.duedate")
                    .append(" FROM m_loan_repayment_schedule ls inner join  m_loan ml on ml.id = ls.loan_id")
                    .append(" join m_client cl on cl.id = ml.client_id")
                    .append(" WHERE ls.duedate = DATE_SUB(CURDATE(), INTERVAL 1 DAY )")
                    .append(" AND principal_completed_derived IS NULL")
                    .append(" AND interest_completed_derived IS NULL ");
            return this.jdbcTemplate.query(sqlBuilder.toString(), XIRRTransactionUpdateRecordMapper);
    }

    @Override
    public List<MonthlyAccrualLoanDTO> retrieveLoansDisbursedBetweenDates(String from, String to) {
        String loansDisbursedCurrentMonthQuery = """
                SELECT loan.id AS id,installment AS installment, loan.disbursedon_date AS disbursementDate,
                    fromdate AS fromDate, duedate AS dueDate, loan.principal_outstanding_derived AS principleOutStanding,
                    loan.annual_nominal_interest_rate AS annualNominalInterestRate, pl.self_interest_rate AS selfInterestShare,
                    pl.partner_interest_rate AS partnerInterestShare, pl.days_in_year_enum AS daysInYearEnum,
                    loan.interest_calculated_in_period_enum AS interestCalculatedInPeriod, loan.maturedon_date as maturitydate
                FROM m_loan_repayment_schedule AS ls INNER JOIN m_loan loan on loan.id = ls.loan_id
                    INNER JOIN m_product_loan pl on pl.id = loan.product_id
                WHERE (disbursedon_date BETWEEN ':from' AND ':to') AND installment = 1 AND loan_status_id = 300
                ORDER BY loan.id
                """;
        loansDisbursedCurrentMonthQuery = loansDisbursedCurrentMonthQuery.replace(":from",from).replace(":to", to);
        return this.jdbcTemplate.query(loansDisbursedCurrentMonthQuery, monthlyAccrualLoanRecordMapper);
    }


    @Override
    public List<MonthlyAccrualLoanDTO> retrieveLoansDueForCurrentMonth() {
        String loansDueForCurrentMonthQuery = """
                SELECT loan.id AS id,installment AS installment, loan.disbursedon_date AS disbursementDate,
                    fromdate AS fromDate, duedate AS dueDate, loan.principal_outstanding_derived AS principleOutStanding,
                    loan.annual_nominal_interest_rate AS annualNominalInterestRate, pl.self_interest_rate AS selfInterestShare,
                    pl.partner_interest_rate AS partnerInterestShare, pl.days_in_year_enum AS daysInYearEnum,
                    loan.interest_calculated_in_period_enum AS interestCalculatedInPeriod, loan.maturedon_date as maturitydate
                FROM m_loan_repayment_schedule AS ls INNER JOIN m_loan loan on loan.id = ls.loan_id
                    INNER JOIN m_product_loan pl on pl.id = loan.product_id
                WHERE (dueDate BETWEEN ':from' AND ':to') AND loan_status_id = 300
                ORDER BY loan.id
                """;
        loansDueForCurrentMonthQuery = loansDueForCurrentMonthQuery.replace(":from",DateUtils.getCurrentMonthFirstDate())
                .replace(":to", DateUtils.getCurrentMonthLastDate());
        return this.jdbcTemplate.query(loansDueForCurrentMonthQuery, monthlyAccrualLoanRecordMapper);
    }

    @Override
    public List<LoanAccrual> retrieveLoanAccruals(Long loanId) {
        return loanAccrualRepository.getByLoanIdAndReversedFalseOrderByFromDate(loanId);
    }

    @Override
    public BigDecimal getNetDisbursementAmountByLoanId(Long loanId) {
        String query = "SELECT net_disbursal_amount FROM m_loan WHERE id = ? ";
        return this.jdbcTemplate.queryForObject(query, BigDecimal.class, new Object[] {loanId});
    }

    @Override
    public String getExceptedDisbursementDateByLoanId(Long loanId) {
        String query = "SELECT expected_disbursedon_date FROM m_loan WHERE id = ? ";
        return this.jdbcTemplate.queryForObject(query, String.class, new Object[] { loanId });
    }

    @Override
    public BigDecimal getPrincipleAmountForLoanId(Long loanId) {
        String query = "SELECT principal_amount FROM m_loan WHERE id = ? ";
        return this.jdbcTemplate.queryForObject(query, BigDecimal.class, new Object[] { loanId });
    }

    @Override
    public List<VpayTransactionDetailsData> retrievePennyDrop(Long loanId) {
        List<VpayTransactionDetailsData> vpayTransactionDetailsDataList = new ArrayList<>();
        vPayTransactionDetailsRepository.getByLoanId(loanId).ifPresent(vPayTransactionDetailsArr -> {
            vPayTransactionDetailsArr.forEach(vPayTransactionDetails -> {
                VpayTransactionDetailsData vpayTransactionDetailsData = new VpayTransactionDetailsData();
                vpayTransactionDetailsData.setEventType(vPayTransactionDetails.getEventType());
                vpayTransactionDetailsData.setUtr(vPayTransactionDetails.getUtr());
                vpayTransactionDetailsData.setTransactionAmount(vPayTransactionDetails.getAmount());
                vpayTransactionDetailsData.setTransactionDate(vPayTransactionDetails.getTransactionDate());
                vpayTransactionDetailsData.setAction(vPayTransactionDetails.getAction());
                vpayTransactionDetailsData.setReason(vPayTransactionDetails.getReason());
                vpayTransactionDetailsDataList.add(vpayTransactionDetailsData);
            });
        });
        return vpayTransactionDetailsDataList;
    }

    @Override
    public List<PartnerandProductData> findAllProducts() {


            record PartnerProductRecord(long partnerId, String partnerName,long productId, String productName){}

            final RowMapper<PartnerProductRecord> partnerProductRecordRowMapper = (rs, row) -> {
                long partnerId =  rs.getLong("partnerId");
                String partnerName = rs.getString("partnerName");
                long productId =  rs.getLong("productId");
                String productName = rs.getString("productName");
                return new PartnerProductRecord(partnerId, partnerName,productId, productName);
            };

            String query = "Select partner_id as partnerId, mp.partner_name as partnerName, mpl.id AS productId, name as productName " +
                    "FROM m_product_loan as mpl inner join m_partner as mp on mpl.partner_id = mp.id";

            List<PartnerProductRecord> partnerProductRecordList = this.jdbcTemplate.query(query, partnerProductRecordRowMapper);
            Map<Long,List<PartnerProductRecord>> records =
                    partnerProductRecordList.stream()
                            .collect(Collectors.groupingBy(PartnerProductRecord::partnerId));

            List<PartnerandProductData> partnerProductDatas = new ArrayList<>();
            records.forEach((partnerId, partnerProductRecords) ->  {
                PartnerandProductData partnerProductData = new PartnerandProductData();
                partnerProductData.setPartnerId(partnerId);
                List<ProductInfoData> productInfoDataList = new ArrayList<>();
                partnerProductRecords.forEach(partnerProductRecord -> {
                    productInfoDataList.add(new ProductInfoData(partnerProductRecord.productId(),
                            partnerProductRecord.productName()));
                    partnerProductData.setPartnerName(partnerProductRecord.partnerName());

                });
                partnerProductData.setProductInfoDataList(productInfoDataList);
                partnerProductDatas.add(partnerProductData);
            });
            return  partnerProductDatas ;
        }




    @Override
    public List<LoanFilterData> retrievePaginatedLoans(Long partnerId, Long productId, String fromDate, String toDate,PaginationParameters parameters) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
//        final String hierarchySearchString = hierarchy + "%";

        //record LoanRecord(long accountNo , String name , long principal , long statusId, String disburseonDate){}

        final RowMapper <LoanFilterData> partnerProductRecordRowMapper = (rs, row) -> {
            long loanId = rs.getLong("loanId");
            AtomicReference<String> pennyAction = new AtomicReference<>("");
            AtomicReference<String> pennyFailureReason = new AtomicReference<>("");
            AtomicReference<String> pennydropUTR = new AtomicReference<>("");
            AtomicReference<String> disbursementAction = new AtomicReference<>("");
            AtomicReference<String> disbursementFailureReason = new AtomicReference<>("");
            AtomicReference<String> disbursementUTR = new AtomicReference<>("");

            this.vPayTransactionDetailsRepository.getByLoanId(loanId).ifPresent(vPayTransactionDetails -> {
                vPayTransactionDetails.stream().filter(vt -> !vt.getReason().contains("reverse"))
                        .forEach(vPayTransaction -> {
                    if (vPayTransaction.getEventType().equals(VPayTransactionConstants.TransactionEventType.PENNY_DROP)
                            && Objects.nonNull(vPayTransaction.getAction())) {
                        pennyAction.set(vPayTransaction.getAction());
                        if(Objects.nonNull(vPayTransaction.getReason()))
                            pennyFailureReason.set(vPayTransaction.getReason());
                        if(Objects.nonNull(vPayTransaction.getUtr()))
                            pennydropUTR.set(vPayTransaction.getUtr());
                    }

                    if (vPayTransaction.getEventType().equals(VPayTransactionConstants.TransactionEventType.DISBURSEMENT)
                            && Objects.nonNull(vPayTransaction.getAction())) {
                        disbursementAction.set(vPayTransaction.getAction());
                        if(Objects.nonNull(vPayTransaction.getReason()))
                            disbursementFailureReason.set(vPayTransaction.getReason());
                        if(Objects.nonNull(vPayTransaction.getUtr()))
                            disbursementUTR.set(vPayTransaction.getUtr());
                    }
                });
            });
             String accountNo = rs.getString("accountNo");
            String name = rs.getString("name");
            long principal = rs.getLong("principal");
            long statusId = rs.getLong("statusId");
            LoanStatus status =LoanStatus.fromInt(Math.toIntExact(Long.valueOf(statusId)));
            String disbursedate = rs.getString("disbursedate");
            String externalId = rs.getString("externalId");
            return new LoanFilterData(loanId, accountNo, externalId, name, principal, status, disbursedate, pennyAction.get(), pennydropUTR.get(),
                    pennyFailureReason.get(), disbursementAction.get(), disbursementUTR.get(), disbursementFailureReason.get());
        };
//        List<LoanFilterData> loanFilterData = new ArrayList<>();

        String sql="select l.id as loanId, l.account_no as accountNo, l.external_id as externalId, l.principal_amount as principal,l.expected_disbursedon_date as disburseDate," +
                " l.loan_status_id as statusId ,c.display_name as name from m_client as c inner join m_loan as l on l.client_id = c.id  " +
                " where l.partner_id = ? and l.product_id = ? and l.approvedon_date between ? and ? order by l.id asc";

        List<LoanFilterData> loansList = this.jdbcTemplate.query(sql, partnerProductRecordRowMapper,new Object[]{partnerId, productId, fromDate, toDate});

        return  loansList;

    }

    @Override
    public List<ImportDocumentData> retriveBulkReportsData(String fromDate, String toDate, String type) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();

        final RowMapper <ImportDocumentData> bulkReportsDataRowMapper = (rs, row) -> {
            Long id = rs.getLong("id");
            String name = rs.getString("name");
            String importTime = rs.getString("importTime");
            String endTime = rs.getString("endTime");
            Integer totalRecords = rs.getInt("totalRecords");
            Integer successRecords = rs.getInt("successRecords");
            Integer failureRecords = rs.getInt("failureRecords");
            return new ImportDocumentData(id,name, importTime, endTime, totalRecords, successRecords, failureRecords, null);
        };

        String sql="select m_import_document.id,m_document.file_name as name,m_document.parent_entity_type as Type," +
                "m_import_document.import_time as importTime,m_import_document.end_time as endTime,m_import_document.createdby_id ," +
                "m_import_document.total_records as totalRecords,m_import_document.success_count as successRecords,m_import_document.failure_count  as failureRecords " +
                "from m_document left JOIN m_import_document " +
                "on m_document.id=m_import_document.document_id where date_format(import_time, '%Y-%m-%d') between ? and ? " +
                "AND parent_entity_type= ? ";

        List<ImportDocumentData> importDocumentData = this.jdbcTemplate.query(sql, bulkReportsDataRowMapper, new Object[] {fromDate, toDate, type});

        importDocumentData.forEach(bulkReport -> {
            List<ImportDocumentDetails> importDocumentDetails = importDocumentDetailsRepository
                    .getByImportId(bulkReport.getId());
            List<ImportDetailsResponseData> importDetailsResponseDatas = new ArrayList<>();
            importDocumentDetails.forEach(importDocumentDetail -> {
                ImportDetailsResponseData importDetailsResponseData = new ImportDetailsResponseData();
                importDetailsResponseData.setExternalId(importDocumentDetail.getExternalId());
                importDetailsResponseData.setLoanId(importDocumentDetail.getLoanId());
                importDetailsResponseData.setLoanAccountNo(importDocumentDetail.getLoanAccountNo());
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                importDetailsResponseData.setDate(formatter.format(importDocumentDetail.getDate()));
                importDetailsResponseData.setStatus(importDocumentDetail.getStatus() ? "SUCCESS" : "FAILURE");
                importDetailsResponseData.setReason(importDocumentDetail.getReason());
                importDetailsResponseDatas.add(importDetailsResponseData);

            });
            bulkReport.setImportDetailsResponseData(importDetailsResponseDatas);
            bulkReport.getId();
        });
        return importDocumentData;
    }

    @Override
    public LoanAccountData retrieveClientDetailsTemplate(final Long clientId) {

        this.context.authenticatedUser();

        final ClientData clientAccount = this.clientReadPlatformService.retrieveOne(clientId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();

            return LoanAccountData.clientDefaults(clientAccount.id(), clientAccount.accountNo(), clientAccount.displayName(),
                clientAccount.officeId(), expectedDisbursementDate);
    }

    @Override
    public LoanAccountData retrieveGroupDetailsTemplate(final Long groupId) {
        this.context.authenticatedUser();
        final GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();
        return LoanAccountData.groupDefaults(groupAccount, expectedDisbursementDate);
    }

    @Override
    public LoanAccountData retrieveGroupAndMembersDetailsTemplate(final Long groupId) {
        GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();

        // get group associations
        final Collection<ClientData> membersOfGroup = this.clientReadPlatformService.retrieveActiveClientMembersOfGroup(groupId);
        if (!CollectionUtils.isEmpty(membersOfGroup)) {
            final Collection<ClientData> activeClientMembers = null;
            final Collection<CalendarData> calendarsData = null;
            final CalendarData collectionMeetingCalendar = null;
            final Collection<GroupRoleData> groupRoles = null;
            groupAccount = GroupGeneralData.withAssocations(groupAccount, membersOfGroup, activeClientMembers, groupRoles, calendarsData,
                    collectionMeetingCalendar);
        }

        return LoanAccountData.groupDefaults(groupAccount, expectedDisbursementDate);
    }

    @Override
    public Collection<CalendarData> retrieveCalendars(final Long groupId) {
        Collection<CalendarData> calendarsData = new ArrayList<>();
        calendarsData.addAll(
                this.calendarReadPlatformService.retrieveParentCalendarsByEntity(groupId, CalendarEntityType.GROUPS.getValue(), null));
        calendarsData
                .addAll(this.calendarReadPlatformService.retrieveCalendarsByEntity(groupId, CalendarEntityType.GROUPS.getValue(), null));
        calendarsData = this.calendarReadPlatformService.updateWithRecurringDates(calendarsData);
        return calendarsData;
    }

    @Override
    public Collection<StaffData> retrieveAllowedLoanOfficers(final Long selectedOfficeId, final boolean staffInSelectedOfficeOnly) {
        if (selectedOfficeId == null) {
            return null;
        }

        Collection<StaffData> allowedLoanOfficers = null;

        if (staffInSelectedOfficeOnly) {
            // only bring back loan officers in selected branch/office
            allowedLoanOfficers = this.staffReadPlatformService.retrieveAllLoanOfficersInOfficeById(selectedOfficeId);
        } else {
            // by default bring back all loan officers in selected
            // branch/office as well as loan officers in officer above
            // this office
            final boolean restrictToLoanOfficersOnly = true;
            allowedLoanOfficers = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(selectedOfficeId,
                    restrictToLoanOfficersOnly);
        }

        return allowedLoanOfficers;
    }

    @Override
    public Collection<OverdueLoanScheduleData> retrieveAllLoansWithOverdueInstallments(final Long penaltyWaitPeriod,
                                                                                       final Boolean backdatePenalties) {
        final MusoniOverdueLoanScheduleMapper rm = new MusoniOverdueLoanScheduleMapper();
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ").append(rm.schema())
                .append(" where " + sqlGenerator.subDate(sqlGenerator.currentDate(), "?", "day") + " > ls.duedate ")
                .append(" and ls.completed_derived <> true and mc.charge_applies_to_enum =1 ")
                .append(" and ls.recalculated_interest_component <> true ")
                .append(" and mc.charge_time_enum = 9 and ml.loan_status_id = 300 ");
        if (backdatePenalties) {
            return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] { penaltyWaitPeriod });
        }
        // Only apply for duedate = yesterday (so that we don't apply
        // penalties on the duedate itself)
        sqlBuilder.append(" and ls.duedate >= " + sqlGenerator.subDate(sqlGenerator.currentDate(), "(? + 1)", "day"));
        return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] { penaltyWaitPeriod, penaltyWaitPeriod });
    }

    @Override
    public List<OverdueLoanInstallment> retrieveLoansWithOverdueInstallmentsAndNoOverdueCharge() {
        RowMapper<OverdueLoanInstallment> overdueLoanInstallmentsRowMapper = (rs, rowNum) ->
             new OverdueLoanInstallment(rs.getLong("loanId"), rs.getInt("installment"),
                    rs.getInt("gracePeriod"), LocalDate.parse(rs.getString("dueDate")),
                     Objects.isNull(rs.getString("dpdLastRunOn")) ? null : LocalDate.parse(rs.getString("dpdLastRunOn")));
        final String query = """
                SELECT ml.id AS loanId, ls.installment AS installment, 
                ml.grace_on_arrears_ageing AS gracePeriod, ls.duedate AS dueDate,
                ls.dpd_tilldate as dpdLastRunOn
                FROM m_loan_repayment_schedule ls  INNER JOIN m_loan ml ON ml.id = ls.loan_id
                WHERE ((ls.principal_amount + ls.interest_amount) - (ifnull(ls.principal_completed_derived,0) + ifnull(ls.interest_completed_derived,0)) != 0)
                AND ls.recalculated_interest_component <> true
                AND ml.loan_status_id = 300
                AND curdate() > ls.duedate
                AND (ls.dpd_tilldate IS NULL OR ls.dpd_tilldate <> curdate())
                ORDER BY ml.id, ls.installment;
                """;
        return this.jdbcTemplate.query(query, overdueLoanInstallmentsRowMapper);
    }

    @Override
    public List<AppropriateAdvanceAmountOnDueDate> appropriateAdvanceAmountOnDueDate() {
        RowMapper<AppropriateAdvanceAmountOnDueDate> advanceAmountAppropriateRowMapper = (rs, rowNum) ->
                new AppropriateAdvanceAmountOnDueDate(rs.getLong("loanId"), rs.getInt("installment"),
                       rs.getBigDecimal("advanceAmount"), LocalDate.parse(rs.getString("dueDate")));
        final String query = """
                SELECT ml.id AS loanId, ls.installment AS installment,
                                ls.total_paid_in_advance_derived AS advanceAmount, ls.duedate AS dueDate
                                FROM m_loan_repayment_schedule ls   INNER JOIN m_loan ml ON ml.id = ls.loan_id
                                WHERE ls.completed_derived <> true
                                AND ls.recalculated_interest_component <> true
                                AND ml.loan_status_id = 300
                                AND curdate() = ls.duedate
                                AND ls.total_paid_in_advance_derived IS NOT NULL
                                ORDER BY ml.id, ls.installment;
                """;


        return this.jdbcTemplate.query(query, advanceAmountAppropriateRowMapper);

    }

    @Override
    public TransactionTypePreference getTransactionTypePreferenceByLoanId(Long loanId) {
        Integer transactionTypePreferenceEnum = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId)
                .getLoanProduct().getLoanProductRelatedDetail().getTransactionTypePreference();
        return TransactionTypePreference.transactionTypePreference(transactionTypePreferenceEnum);
    }

    @Override
    public BankTranscationData retrieveBankTransaction(Long loanId) {
        Function<VPayTransactionDetails,VpayTransactionDetailsData> entityToDataMapper = vPayTransactionDetails -> {
            VpayTransactionDetailsData vpayTransactionDetailsData = new VpayTransactionDetailsData();
            vpayTransactionDetailsData.setEventType(vPayTransactionDetails.getEventType());
            vpayTransactionDetailsData.setUtr(vPayTransactionDetails.getUtr());
            vpayTransactionDetailsData.setTransactionAmount(vPayTransactionDetails.getAmount());
            vpayTransactionDetailsData.setTransactionDate(vPayTransactionDetails.getTransactionDate());
            vpayTransactionDetailsData.setAction(vPayTransactionDetails.getAction());
            vpayTransactionDetailsData.setReason(vPayTransactionDetails.getReason());
            vpayTransactionDetailsData.setTransactionType(vPayTransactionDetails.getTransactionType());
            return vpayTransactionDetailsData;
        };
        List<VPayTransactionDetails> pennyDropTransactions = vPayTransactionDetailsRepository.getLatestTransaction(loanId,VPayTransactionConstants.TransactionEventType.PENNY_DROP);
        List<VpayTransactionDetailsData> pennyDropTransaction = null;
        if(!pennyDropTransactions.isEmpty()) {
            pennyDropTransaction = pennyDropTransactions.stream().map(entityToDataMapper).toList();
        }

        List<VPayTransactionDetails> disbursementTransactions = vPayTransactionDetailsRepository.getLatestTransaction(loanId,VPayTransactionConstants.TransactionEventType.DISBURSEMENT);
        List<VpayTransactionDetailsData> disbursementTransaction = null;
        if(!disbursementTransactions.isEmpty()) {
            disbursementTransaction = disbursementTransactions.stream().map(entityToDataMapper).toList();
        }
        return new BankTranscationData(pennyDropTransaction,disbursementTransaction);
    }


    @SuppressWarnings("deprecation")
    @Override
    public Integer retriveLoanCounter(final Long groupId, final Integer loanType, Long productId) {
        final String sql = "Select MAX(l.loan_product_counter) from m_loan l where l.group_id = ?  and l.loan_type_enum = ? and l.product_id=?";
        return this.jdbcTemplate.queryForObject(sql, new Object[] { groupId, loanType, productId }, Integer.class);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Integer retriveLoanCounter(final Long clientId, Long productId) {
        final String sql = "Select MAX(l.loan_product_counter) from m_loan l where l.client_id = ? and l.product_id=?";
        return this.jdbcTemplate.queryForObject(sql, new Object[] { clientId, productId }, Integer.class);
    }

    @Override
    public Collection<DisbursementData> retrieveLoanDisbursementDetails(final Long loanId) {
        final LoanDisbursementDetailMapper rm = new LoanDisbursementDetailMapper(sqlGenerator);
        final String sql = "select " + rm.schema()
                + " where dd.loan_id=? group by dd.id, lc.amount_waived_derived order by dd.expected_disburse_date";
        return this.jdbcTemplate.query(sql, rm, new Object[] { loanId }); // NOSONAR
    }

    private static final class LoanDisbursementDetailMapper implements RowMapper<DisbursementData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        LoanDisbursementDetailMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String schema() {
            return "dd.id as id,dd.expected_disburse_date as expectedDisbursementdate, dd.disbursedon_date as actualDisbursementdate,dd.principal as principal,dd.net_disbursal_amount as netDisbursalAmount,sum(lc.amount) chargeAmount,sum(lc.self_share_amount_derived) selfChargeAmount,sum(lc.partner_share_amount_derived) partnerChargeAmount, lc.amount_waived_derived waivedAmount, "
                    + sqlGenerator.groupConcat("lc.id") + " loanChargeId "
                    + "from m_loan l inner join m_loan_disbursement_detail dd on dd.loan_id = l.id left join m_loan_tranche_disbursement_charge tdc on tdc.disbursement_detail_id=dd.id "
                    + "left join m_loan_charge lc on  lc.id=tdc.loan_charge_id and lc.is_active=true";
        }

        @Override
        public DisbursementData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final LocalDate expectedDisbursementdate = JdbcSupport.getLocalDate(rs, "expectedDisbursementdate");
            final LocalDate actualDisbursementdate = JdbcSupport.getLocalDate(rs, "actualDisbursementdate");
            final BigDecimal principal = rs.getBigDecimal("principal");
            final String loanChargeId = rs.getString("loanChargeId");
            final BigDecimal netDisbursalAmount = rs.getBigDecimal("netDisbursalAmount");
            BigDecimal chargeAmount = rs.getBigDecimal("chargeAmount");
            BigDecimal selfChargeAmount = rs.getBigDecimal("selfChargeAmount");
            BigDecimal partnerChargeAmount = rs.getBigDecimal("partnerChargeAmount");
            final BigDecimal waivedAmount = rs.getBigDecimal("waivedAmount");
            final BigDecimal selfPrincipalAmount = rs.getBigDecimal("selfPrincipalAmount");
            final BigDecimal partnerPrincipalAmount = rs.getBigDecimal("partnerPrincipalAmount");


            if (chargeAmount != null && waivedAmount != null) {
                chargeAmount = chargeAmount.subtract(waivedAmount);
            }
            final DisbursementData disbursementData = new DisbursementData(id, expectedDisbursementdate, actualDisbursementdate, principal,
                    netDisbursalAmount, loanChargeId, chargeAmount, waivedAmount, selfPrincipalAmount, partnerPrincipalAmount,selfChargeAmount,partnerChargeAmount);
            return disbursementData;
        }

    }

    @Override
    public DisbursementData retrieveLoanDisbursementDetail(Long loanId, Long disbursementId) {
        final LoanDisbursementDetailMapper rm = new LoanDisbursementDetailMapper(sqlGenerator);
        final String sql = "select " + rm.schema() + " where dd.loan_id=? and dd.id=? group by dd.id, lc.amount_waived_derived";
        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanId, disbursementId }); // NOSONAR
    }

    @Override
    public Collection<LoanTermVariationsData> retrieveLoanTermVariations(Long loanId, Integer termType) {
        final LoanTermVariationsMapper rm = new LoanTermVariationsMapper();
        final String sql = "select " + rm.schema() + " where tv.loan_id=? and tv.term_type=?";
        return this.jdbcTemplate.query(sql, rm, new Object[] { loanId, termType }); // NOSONAR
    }

    private static final class LoanTermVariationsMapper implements RowMapper<LoanTermVariationsData> {

        public String schema() {
            return "tv.id as id,tv.applicable_date as variationApplicableFrom,tv.decimal_value as decimalValue, tv.date_value as dateValue, tv.is_specific_to_installment as isSpecificToInstallment "
                    + "from m_loan_term_variations tv";
        }

        @Override
        public LoanTermVariationsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final LocalDate variationApplicableFrom = JdbcSupport.getLocalDate(rs, "variationApplicableFrom");
            final BigDecimal decimalValue = rs.getBigDecimal("decimalValue");
            final LocalDate dateValue = JdbcSupport.getLocalDate(rs, "dateValue");
            final boolean isSpecificToInstallment = rs.getBoolean("isSpecificToInstallment");

            final LoanTermVariationsData loanTermVariationsData = new LoanTermVariationsData(id,
                    LoanEnumerations.loanvariationType(LoanTermVariationType.EMI_AMOUNT), variationApplicableFrom, decimalValue, dateValue,
                    isSpecificToInstallment);
            return loanTermVariationsData;
        }

    }

    @Override
    public Collection<LoanScheduleAccrualData> retriveScheduleAccrualData() {

        LoanScheduleAccrualMapper mapper = new LoanScheduleAccrualMapper();
        Date organisationStartDate = this.configurationDomainService.retrieveOrganisationStartDate();
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ").append(mapper.schema()).append(
                        " where (recaldet.is_compounding_to_be_posted_as_transaction is null or recaldet.is_compounding_to_be_posted_as_transaction = false) ")
                .append(" and (((ls.fee_charges_amount <> COALESCE(ls.accrual_fee_charges_derived, 0))")
                .append(" or ( ls.penalty_charges_amount <> COALESCE(ls.accrual_penalty_charges_derived, 0))")
                .append(" or ( ls.interest_amount <> COALESCE(ls.accrual_interest_derived, 0)))")
                .append(" and loan.loan_status_id=:active and loan.is_npa=false and ls.duedate <= "
                        + sqlGenerator.currentDate() + ") ");
                /*.append(" and loan.loan_status_id=:active and mpl.accounting_type=:type and loan.is_npa=false and ls.duedate <= "
                        + sqlGenerator.currentDate() + ") ");*/
        if (organisationStartDate != null) {
            sqlBuilder.append(" and ls.duedate > :organisationstartdate ");
        }
        sqlBuilder.append(" order by loan.id,ls.duedate ");
        Map<String, Object> paramMap = new HashMap<>(3);
        paramMap.put("active", LoanStatus.ACTIVE.getValue());
        // paramMap.put("type", AccountingRuleType.ACCRUAL_PERIODIC.getValue());
        paramMap.put("organisationstartdate",
                (organisationStartDate == null) ? formatter.format(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()))
                        : formatter.format(LocalDate.ofInstant(organisationStartDate.toInstant(), DateUtils.getDateTimeZoneOfTenant())));

        return this.namedParameterJdbcTemplate.query(sqlBuilder.toString(), paramMap, mapper);
    }

    @Override
    public Collection<LoanScheduleAccrualData> retrivePeriodicAccrualData(final LocalDate tillDate) {

        LoanSchedulePeriodicAccrualMapper mapper = new LoanSchedulePeriodicAccrualMapper();
        Date organisationStartDate = this.configurationDomainService.retrieveOrganisationStartDate();
        String formattedTillDate = formatter.format(tillDate);
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ").append(mapper.schema()).append(
                        " where  (recaldet.is_compounding_to_be_posted_as_transaction is null or recaldet.is_compounding_to_be_posted_as_transaction = false) ")
                .append(" and (((ls.fee_charges_amount <> COALESCE(ls.accrual_fee_charges_derived, 0))")
                .append(" or (ls.penalty_charges_amount <> COALESCE(ls.accrual_penalty_charges_derived, 0))")
                .append(" or (ls.interest_amount <> COALESCE(ls.accrual_interest_derived, 0)))")
                .append(" and loan.loan_status_id=:active and (loan.closedon_date <= '" + formattedTillDate
                        + "' or loan.closedon_date is null) ")
                /*.append(" and loan.loan_status_id=:active and mpl.accounting_type=:type and (loan.closedon_date <= '" + formattedTillDate
                        + "' or loan.closedon_date is null)")*/
                .append(" and loan.is_npa=false and (ls.duedate <= '" + formattedTillDate + "' or (ls.duedate > '" + formattedTillDate
                        + "' and ls.fromdate < '" + formattedTillDate + "'))) ");
        if (organisationStartDate != null) {
            String formattedOrganizationStartDate = formatter
                    .format(LocalDate.ofInstant(organisationStartDate.toInstant(), DateUtils.getDateTimeZoneOfTenant()));
            sqlBuilder.append(" and ls.duedate > '" + formattedOrganizationStartDate + "' ");
        }
        sqlBuilder.append(" order by loan.id,ls.duedate ");
        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put("active", LoanStatus.ACTIVE.getValue());
        // paramMap.put("type", AccountingRuleType.ACCRUAL_PERIODIC.getValue());
        return this.namedParameterJdbcTemplate.query(sqlBuilder.toString(), paramMap, mapper);
    }

    private static final class LoanSchedulePeriodicAccrualMapper implements RowMapper<LoanScheduleAccrualData> {

        public String schema() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("loan.id as loanId , (CASE WHEN loan.client_id is null THEN mg.office_id ELSE mc.office_id END) as officeId,")
                    .append("loan.accrued_till as accruedTill, loan.repayment_period_frequency_enum as frequencyEnum, ")
                    .append("loan.interest_calculated_from_date as interestCalculatedFrom, ").append("loan.repay_every as repayEvery,")
                    .append("ls.installment as installmentNumber, ")
                    .append("ls.duedate as duedate,ls.fromdate as fromdate ,ls.id as scheduleId,loan.product_id as productId,")
                    .append("ls.interest_amount as interest, ls.interest_waived_derived as interestWaived,ls.self_interest_waived_derived as selfInterestWaived,ls.partner_interest_waived_derived as partnerInterestWaived,")
                    .append("ls.penalty_charges_amount as penalty, ").append("ls.fee_charges_amount as charges, ")
                    .append("ls.accrual_interest_derived as accinterest,ls.accrual_fee_charges_derived as accfeecharege,ls.accrual_penalty_charges_derived as accpenalty,")
                    .append(" loan.currency_code as currencyCode,loan.currency_digits as currencyDigits,loan.currency_multiplesof as inMultiplesOf,")
                    .append("curr.display_symbol as currencyDisplaySymbol,curr.name as currencyName,curr.internationalized_name_code as currencyNameCode")
                    .append(" from m_loan_repayment_schedule ls ").append(" left join m_loan loan on loan.id=ls.loan_id ")
                    .append(" left join m_product_loan mpl on mpl.id = loan.product_id")
                    .append(" left join m_client mc on mc.id = loan.client_id ").append(" left join m_group mg on mg.id = loan.group_id")
                    .append(" left join m_currency curr on curr.code = loan.currency_code")
                    .append(" left join m_loan_recalculation_details as recaldet on loan.id = recaldet.loan_id ");
            return sqlBuilder.toString();
        }

        @Override
        public LoanScheduleAccrualData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            final Long loanId = rs.getLong("loanId");
            final Long officeId = rs.getLong("officeId");
            final LocalDate accruedTill = JdbcSupport.getLocalDate(rs, "accruedTill");
            final LocalDate interestCalculatedFrom = JdbcSupport.getLocalDate(rs, "interestCalculatedFrom");
            final Integer installmentNumber = JdbcSupport.getInteger(rs, "installmentNumber");

            final Integer frequencyEnum = JdbcSupport.getInteger(rs, "frequencyEnum");
            final Integer repayEvery = JdbcSupport.getInteger(rs, "repayEvery");
            final PeriodFrequencyType frequency = PeriodFrequencyType.fromInt(frequencyEnum);
            final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "duedate");
            final LocalDate fromDate = JdbcSupport.getLocalDate(rs, "fromdate");
            final Long repaymentScheduleId = rs.getLong("scheduleId");
            final Long loanProductId = rs.getLong("productId");
            final BigDecimal interestIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "interest");
            final BigDecimal feeIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "charges");
            final BigDecimal penaltyIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "penalty");
            final BigDecimal interestIncomeWaived = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "interestWaived");
            final BigDecimal selfInterestIncomeWaived = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "selfInterestWaived");
            final BigDecimal partnerInterestIncomeWaived = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "partnerInterestWaived");
            final BigDecimal accruedInterestIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "accinterest");
            final BigDecimal accruedFeeIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "accfeecharege");
            final BigDecimal accruedPenaltyIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "accpenalty");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return new LoanScheduleAccrualData(loanId, officeId, installmentNumber, accruedTill, frequency, repayEvery, dueDate, fromDate,
                    repaymentScheduleId, loanProductId, interestIncome, feeIncome, penaltyIncome, accruedInterestIncome, accruedFeeIncome,
                    accruedPenaltyIncome, currencyData, interestCalculatedFrom, interestIncomeWaived);
        }

    }

    private static final class LoanScheduleAccrualMapper implements RowMapper<LoanScheduleAccrualData> {

        public String schema() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("loan.id as loanId, (CASE WHEN loan.client_id is null THEN mg.office_id ELSE mc.office_id END) as officeId,")
                    .append("ls.duedate as duedate,ls.fromdate as fromdate,ls.id as scheduleId,loan.product_id as productId,")
                    .append("ls.installment as installmentNumber, ")
                    .append("ls.interest_amount as interest, ls.interest_waived_derived as interestWaived, ls.self_interest_waived_derived as selfInterestWaived, ls.partner_interest_waived_derived as partnerInterestWaived,")
                    .append("ls.penalty_charges_amount as penalty, ").append("ls.fee_charges_amount as charges, ")
                    .append("ls.accrual_interest_derived as accinterest,ls.accrual_fee_charges_derived as accfeecharege,ls.accrual_penalty_charges_derived as accpenalty,")
                    .append(" loan.currency_code as currencyCode,loan.currency_digits as currencyDigits,loan.currency_multiplesof as inMultiplesOf,")
                    .append("curr.display_symbol as currencyDisplaySymbol,curr.name as currencyName,curr.internationalized_name_code as currencyNameCode")
                    .append(" from m_loan_repayment_schedule ls ").append(" left join m_loan loan on loan.id=ls.loan_id ")
                    .append(" left join m_product_loan mpl on mpl.id = loan.product_id")
                    .append(" left join m_client mc on mc.id = loan.client_id ").append(" left join m_group mg on mg.id = loan.group_id")
                    .append(" left join m_currency curr on curr.code = loan.currency_code")
                    .append(" left join m_loan_recalculation_details as recaldet on loan.id = recaldet.loan_id ");
            return sqlBuilder.toString();
        }

        @Override
        public LoanScheduleAccrualData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            final Long loanId = rs.getLong("loanId");
            final Long officeId = rs.getLong("officeId");
            final Integer installmentNumber = JdbcSupport.getInteger(rs, "installmentNumber");
            final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "duedate");
            final LocalDate fromdate = JdbcSupport.getLocalDate(rs, "fromdate");
            final Long repaymentScheduleId = rs.getLong("scheduleId");
            final Long loanProductId = rs.getLong("productId");
            final BigDecimal interestIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "interest");
            final BigDecimal feeIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "charges");
            final BigDecimal penaltyIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "penalty");
            final BigDecimal interestIncomeWaived = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "interestWaived");
            final BigDecimal selfInterestIncomeWaived = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "selfInterestWaived");
            final BigDecimal partnerInterestIncomeWaived = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "partnerInterestWaived");

            final BigDecimal accruedInterestIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "accinterest");
            final BigDecimal accruedFeeIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "accfeecharege");
            final BigDecimal accruedPenaltyIncome = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "accpenalty");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);
            final LocalDate accruedTill = null;
            final PeriodFrequencyType frequency = null;
            final Integer repayEvery = null;
            final LocalDate interestCalculatedFrom = null;
            return new LoanScheduleAccrualData(loanId, officeId, installmentNumber, accruedTill, frequency, repayEvery, dueDate, fromdate,
                    repaymentScheduleId, loanProductId, interestIncome, feeIncome, penaltyIncome, accruedInterestIncome, accruedFeeIncome,
                    accruedPenaltyIncome, currencyData, interestCalculatedFrom, interestIncomeWaived);
        }
    }

    @Override
    public LoanTransactionData retrieveRecoveryPaymentTemplate(Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.RECOVERY_REPAYMENT);
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        BigDecimal outstandingLoanBalance = null;
        BigDecimal selfOutstandingLoanBalance = null;
        BigDecimal partnerOutstandingLoanBalance = null;
        final BigDecimal advanceAmount = BigDecimal.ZERO;
        final BigDecimal foreClosureAmount=BigDecimal.ZERO;
        final Integer installmentNumber =0;


        final BigDecimal unrecognizedIncomePortion = null;
        return new LoanTransactionData(null, null, null, transactionType, null, null, null, loan.getTotalWrittenOff(),
                loan.getNetDisbursalAmount(), null, null, null, null, null, unrecognizedIncomePortion, paymentOptions, null, null, null,
                outstandingLoanBalance, false,null,null,null,null,null,null,selfOutstandingLoanBalance,partnerOutstandingLoanBalance,
                advanceAmount,foreClosureAmount,null,null,installmentNumber,null,null);

    }

    @Override
    public LoanTransactionData retrieveLoanWriteoffTemplate(final Long loanId) {

        final LoanAccountData loan = this.retrieveOne(loanId);
        final BigDecimal outstandingLoanBalance = null;
        final BigDecimal selfOutstandingLoanBalance = null;
        final BigDecimal partnerOutstandingLoanBalance = null;
        final BigDecimal advanceAmount=BigDecimal.ZERO;
        final BigDecimal foreClosureAmount=BigDecimal.ZERO;
        final Integer installmentNumber=0;


        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WRITEOFF);
        final BigDecimal unrecognizedIncomePortion = null;
        final List<CodeValueData> writeOffReasonOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanApiConstants.WRITEOFFREASONS));
        LoanTransactionData loanTransactionData = new LoanTransactionData(null, null, null, transactionType, null, loan.currency(),
                DateUtils.getLocalDateOfTenant(), loan.getTotalOutstandingAmount(), loan.getNetDisbursalAmount(), null, null, null, null,
                null, null, null, null, outstandingLoanBalance, unrecognizedIncomePortion, false,null,null,null,null,
                null,null,selfOutstandingLoanBalance,partnerOutstandingLoanBalance,advanceAmount,foreClosureAmount,null,null,installmentNumber,null,null);
        loanTransactionData.setWriteOffReasonOptions(writeOffReasonOptions);
        return loanTransactionData;
    }

    @Override
    public Collection<Long> fetchLoansForInterestRecalculation() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ml.id FROM m_loan ml ");
        sqlBuilder.append(" INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        sqlBuilder.append(" LEFT JOIN m_loan_disbursement_detail dd on dd.loan_id=ml.id and dd.disbursedon_date is null ");
        // For Floating rate changes
        sqlBuilder.append(
                " left join m_product_loan_floating_rates pfr on ml.product_id = pfr.loan_product_id and ml.is_floating_interest_rate = true");
        sqlBuilder.append(" left join m_floating_rates fr on  pfr.floating_rates_id = fr.id");
        sqlBuilder.append(" left join m_floating_rates_periods frp on fr.id = frp.floating_rates_id ");
        sqlBuilder.append(" left join m_loan_reschedule_request lrr on lrr.loan_id = ml.id");
        // this is to identify the applicable rates when base rate is changed
        sqlBuilder.append(" left join  m_floating_rates bfr on  bfr.is_base_lending_rate = true");
        sqlBuilder.append(" left join  m_floating_rates_periods bfrp on  bfr.id = bfrp.floating_rates_id and bfrp.created_date >= ?");
        sqlBuilder.append(" WHERE ml.loan_status_id = ? ");
        sqlBuilder.append(" and ml.is_npa = false ");
        sqlBuilder.append(" and ((");
        sqlBuilder.append("ml.interest_recalculation_enabled = 1 ");
        sqlBuilder.append(" and (ml.interest_recalcualated_on is null or ml.interest_recalcualated_on <> ?)");
        sqlBuilder.append(" and ((");
        sqlBuilder.append(" mr.completed_derived is false ");
        sqlBuilder.append(" and mr.duedate < ? )");
        sqlBuilder.append(" or dd.expected_disburse_date < ? )) ");
        sqlBuilder.append(" or (");
        sqlBuilder.append(" fr.is_active = true and  frp.is_active = true");
        sqlBuilder.append(" and (frp.created_date >= ?  or ");
        sqlBuilder
                .append("(bfrp.id is not null and frp.is_differential_to_base_lending_rate = true and frp.from_date >= bfrp.from_date)) ");
        sqlBuilder.append("and lrr.loan_id is null");
        sqlBuilder.append(" ))");
        sqlBuilder.append(" group by ml.id");
        try {
            String currentdate = formatter.format(DateUtils.getLocalDateOfTenant());
            // will look only for yesterday modified rates
            String yesterday = formatter.format(DateUtils.getLocalDateOfTenant().minusDays(1));
            return this.jdbcTemplate.queryForList(sqlBuilder.toString(), Long.class,
                    new Object[] { yesterday, LoanStatus.ACTIVE.getValue(), currentdate, currentdate, currentdate, yesterday });
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Long> fetchLoansForInterestRecalculation(Integer pageSize, Long maxLoanIdInList, String officeHierarchy) {
        String currentdate = formatter.format(DateUtils.getLocalDateOfTenant());
        // will look only for yesterday modified rates
        String yesterday = formatter.format(DateUtils.getLocalDateOfTenant().minusDays(1));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ml.id FROM m_loan ml ");
        sqlBuilder.append(" left join m_client mc on mc.id = ml.client_id ");
        sqlBuilder.append(" left join m_office o on mc.office_id = o.id  ");
        sqlBuilder.append(" INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        sqlBuilder.append(" LEFT JOIN m_loan_disbursement_detail dd on dd.loan_id=ml.id and dd.disbursedon_date is null ");
        // For Floating rate changes
        sqlBuilder.append(
                " left join m_product_loan_floating_rates pfr on ml.product_id = pfr.loan_product_id and ml.is_floating_interest_rate = true");
        sqlBuilder.append(" left join m_floating_rates fr on  pfr.floating_rates_id = fr.id");
        sqlBuilder.append(" left join m_floating_rates_periods frp on fr.id = frp.floating_rates_id ");
        sqlBuilder.append(" left join m_loan_reschedule_request lrr on lrr.loan_id = ml.id");
        // this is to identify the applicable rates when base rate is changed
        sqlBuilder.append(" left join  m_floating_rates bfr on  bfr.is_base_lending_rate = true");
        sqlBuilder.append(" left join  m_floating_rates_periods bfrp on  bfr.id = bfrp.floating_rates_id and bfrp.created_date >= '"
                + yesterday + "'");
        sqlBuilder.append(" WHERE ml.loan_status_id = ? ");
        sqlBuilder.append(" and ml.is_npa = false ");
        sqlBuilder.append(" and ((");
        sqlBuilder.append("ml.interest_recalculation_enabled = true ");
        sqlBuilder.append(" and (ml.interest_recalcualated_on is null or ml.interest_recalcualated_on <> '" + currentdate + "')");
        sqlBuilder.append(" and ((");
        sqlBuilder.append(" mr.completed_derived is false ");
        sqlBuilder.append(" and mr.duedate < '" + currentdate + "' )");
        sqlBuilder.append(" or dd.expected_disburse_date < '" + currentdate + "' )) ");
        sqlBuilder.append(" or (");
        sqlBuilder.append(" fr.is_active = true and  frp.is_active = true");
        sqlBuilder.append(" and (frp.created_date >= '" + yesterday + "'  or ");
        sqlBuilder
                .append("(bfrp.id is not null and frp.is_differential_to_base_lending_rate = true and frp.from_date >= bfrp.from_date)) ");
        sqlBuilder.append("and lrr.loan_id is null");
        sqlBuilder.append(" ))");
        sqlBuilder.append(" and ml.id >= ?  and o.hierarchy like ? ");
        sqlBuilder.append(" group by ml.id ");
        sqlBuilder.append(" limit ? ");
        try {
            return Collections.synchronizedList(this.jdbcTemplate.queryForList(sqlBuilder.toString(), Long.class,
                    new Object[] { LoanStatus.ACTIVE.getValue(), maxLoanIdInList, officeHierarchy, pageSize }));
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Collection<LoanTransactionData> retrieveWaiverLoanTransactions(final Long loanId) {
        try {

            final LoanTransactionDerivedComponentMapper rm = new LoanTransactionDerivedComponentMapper(sqlGenerator);

            final String sql = "select " + rm.schema()
                    + " where tr.loan_id = ? and tr.transaction_type_enum = ? and tr.is_reversed=false order by tr.transaction_date ASC,id ";
            return this.jdbcTemplate.query(sql, rm, new Object[] { loanId, LoanTransactionType.WAIVE_INTEREST.getValue() }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public boolean isGuaranteeRequired(final Long loanId) {
        final String sql = "select pl.hold_guarantee_funds from m_loan ml inner join m_product_loan pl on pl.id = ml.product_id where ml.id=?";
        return this.jdbcTemplate.queryForObject(sql, Boolean.class, loanId);
    }

    private static final class LoanTransactionDerivedComponentMapper implements RowMapper<LoanTransactionData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        LoanTransactionDerivedComponentMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String schema() {

            return " tr.id as id, tr.transaction_type_enum as transactionType, tr.transaction_date as " + sqlGenerator.escape("date")
                    + ", tr.amount as total, " + " tr.principal_portion_derived as principal,tr.self_principal_portion_derived as selfPrincipal,tr.partner_principal_portion_derived as partnerPrincipal, tr.interest_portion_derived as interest,tr.self_interest_portion_derived as selfInterestCharged,tr.partner_interest_portion_derived as partnerInterestCharged,tr.self_amount as selfDue,tr.partner_amount as partnerDue, "
                    + " tr.fee_charges_portion_derived as fees,tr.self_fee_charges_portion_derived as selfFee,tr.partner_fee_charges_portion_derived as partnerFee, tr.penalty_charges_portion_derived as penalties, "
                    + " tr.overpayment_portion_derived as overpayment, tr.outstanding_loan_balance_derived as outstandingLoanBalance,tr.self_outstanding_loan_balance_derived as selfOutstandingLoanBalance,tr.partner_outstanding_loan_balance_derived as partnerOutstandingLoanBalance,tr.receipt_reference_number as receiptReferenceNumber,tr.partner_transfer_utr as partnerTransferUtr,tr.partner_transfer_date as partnerTransferDate,tr.repayment_mode_cv_id as repaymentModeId, "
                    + " tr.unrecognized_income_portion as unrecognizedIncome " + " from m_loan_transaction tr ";
        }

        @Override
        public LoanTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
            final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(transactionTypeInt);

            final LocalDate date = JdbcSupport.getLocalDate(rs, "date");
            final BigDecimal totalAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "total");
            final BigDecimal principalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principal");
            final BigDecimal selfPrincipalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
            final BigDecimal partnerPrincipalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");

            final BigDecimal interestPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interest");
            final BigDecimal selfInterestPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
            final BigDecimal partnerInterestPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");
            final BigDecimal selfDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfDue");
            final BigDecimal partnerDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerDue");


            final BigDecimal feeChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fees");
            final BigDecimal selfFeeChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfFee");
            final BigDecimal partnerFeeChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerFee");
            final BigDecimal penaltyChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penalties");
            final BigDecimal overPaymentPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "overpayment");
            final BigDecimal unrecognizedIncomePortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "unrecognizedIncome");
            final BigDecimal outstandingLoanBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "outstandingLoanBalance");
            final BigDecimal selfOutstandingLoanBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfOutstandingLoanBalance");
            final BigDecimal partnerOutstandingLoanBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerOutstandingLoanBalance");
            final BigDecimal advanceAmount=BigDecimal.ZERO;
            final BigDecimal foreClosureAmount=BigDecimal.ZERO;
            final Integer installmentNumber = 0;
             final Long repaymentModeId = JdbcSupport.getLong(rs, "repaymentModeId");
             final String repaymentModeValue = rs.getString("repaymentModeValue");
            final List<CodeValueData> repaymentModeOptions=new ArrayList<>();
             final CodeValueData repaymentMode = CodeValueData.instance(repaymentModeId, repaymentModeValue);
            return new LoanTransactionData(id, transactionType, date, totalAmount, null, principalPortion, interestPortion,
                    feeChargesPortion, penaltyChargesPortion, overPaymentPortion, unrecognizedIncomePortion, outstandingLoanBalance, false,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfDue,partnerDue,
                    selfOutstandingLoanBalance,partnerOutstandingLoanBalance,advanceAmount,foreClosureAmount,selfFeeChargesPortion,partnerFeeChargesPortion,installmentNumber,repaymentModeOptions,repaymentMode);
        }
    }

    @Override
    public Collection<LoanSchedulePeriodData> fetchWaiverInterestRepaymentData(final Long loanId) {
        try {

            final LoanRepaymentWaiverMapper rm = new LoanRepaymentWaiverMapper();

            final String sql = "select " + rm.getSchema()
                    + " where lrs.loan_id = ? and lrs.interest_waived_derived is not null order by lrs.installment ASC ";
            return this.jdbcTemplate.query(sql, rm, new Object[] { loanId }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }

    }

    private static final class LoanRepaymentWaiverMapper implements RowMapper<LoanSchedulePeriodData> {

        private final String sqlSchema;

        public String getSchema() {
            return this.sqlSchema;
        }

        LoanRepaymentWaiverMapper() {
            StringBuilder sb = new StringBuilder();
            sb.append("lrs.duedate as dueDate,lrs.interest_waived_derived interestWaived, lrs.installment as installment");
            sb.append(" from m_loan_repayment_schedule lrs ");
            sqlSchema = sb.toString();
        }

        @Override
        public LoanSchedulePeriodData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            final Integer period = JdbcSupport.getInteger(rs, "installment");
            final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueDate");
            final BigDecimal interestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWaived");
            final BigDecimal selfInterestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestWaived");
            final BigDecimal partnerInterestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestWaived");


            final LocalDate fromDate = null;
            final LocalDate obligationsMetOnDate = null;
            final Boolean complete = false;
            final BigDecimal principalOriginalDue = null;
            final BigDecimal principalPaid = null;
            final BigDecimal principalWrittenOff = null;
            final BigDecimal principalOutstanding = null;
            final BigDecimal interestPaid = null;
            final BigDecimal interestWrittenOff = null;
            final BigDecimal interestOutstanding = null;
            final BigDecimal feeChargesDue = null;
            final BigDecimal feeChargesPaid = null;
            final BigDecimal feeChargesWaived = null;
            final BigDecimal feeChargesWrittenOff = null;
            final BigDecimal feeChargesOutstanding = null;
            final BigDecimal penaltyChargesDue = null;
            final BigDecimal penaltyChargesPaid = null;
            final BigDecimal penaltyChargesWaived = null;
            final BigDecimal penaltyChargesWrittenOff = null;
            final BigDecimal penaltyChargesOutstanding = null;
            final BigDecimal selfPenaltyChargesOutstanding = null;
            final BigDecimal partnerPenaltyChargesOutstanding = null;

            final BigDecimal totalDueForPeriod = null;
            final BigDecimal totalSelfDueForPeriod = null;
            final BigDecimal totalPartnerDueForPeriod = null;
            final BigDecimal totalPaidInAdvanceForPeriod = null;
           // final BigDecimal totalSelfPaidInAdvanceForPeriod = null;
          //  final BigDecimal totalPartnerPaidInAdvanceForPeriod = null;
            final BigDecimal totalPaidLateForPeriod = null;
            final BigDecimal totalSelfPaidLateForPeriod = null;
            final BigDecimal totalPartnerPaidLateForPeriod = null;

            final BigDecimal totalActualCostOfLoanForPeriod = null;
            final BigDecimal outstandingPrincipalBalanceOfLoan = null;
            final BigDecimal interestDueOnPrincipalOutstanding = null;
            Long loanId = null;
            final BigDecimal totalWaived = null;
            final BigDecimal totalWrittenOff = null;
            final BigDecimal totalOutstanding = null;
            final BigDecimal totalPaid = null;
            final BigDecimal totalInstallmentAmount = null;
            final BigDecimal selfPrincipal = null;
            final BigDecimal partnerPrincipal = null;
            final BigDecimal selfInterestCharged = null;
            final BigDecimal partnerInterestCharged = null;

            final BigDecimal selfPrincipalLoanBalanceOutstanding = null;
            final BigDecimal partnerPrincipalLoanBalanceOutstanding = null;

            final BigDecimal totalSelfPaid = null;
            final BigDecimal totalPartnerPaid = null;
            final BigDecimal totalSelfOutstanding = null;
            final BigDecimal totalPartnerOutstanding = null;

             final BigDecimal selfFeeChargesOutstanding = null;
             final BigDecimal selfFeeChargesDue = null;
             final BigDecimal selfFeeChargesPaid= null;
             final BigDecimal selfFeeChargesWaived =null;
             final BigDecimal selfFeeChargesWrittenOff = null;


             final BigDecimal partnerFeeChargesOutstanding= null;
             final BigDecimal partnerFeeChargesDue= null;
             final BigDecimal partnerFeeChargesPaid=null;
             final BigDecimal partnerFeeChargesWaived=null;
             final BigDecimal partnerFeeChargesWrittenOff=null;

            return LoanSchedulePeriodData.repaymentPeriodWithPayments(loanId, period, fromDate, dueDate, obligationsMetOnDate, complete,
                    principalOriginalDue, principalPaid, principalWrittenOff, principalOutstanding, outstandingPrincipalBalanceOfLoan,
                    interestDueOnPrincipalOutstanding, interestPaid, interestWaived, interestWrittenOff, interestOutstanding, feeChargesDue,
                    feeChargesPaid, feeChargesWaived, feeChargesWrittenOff, feeChargesOutstanding, penaltyChargesDue, penaltyChargesPaid,
                    penaltyChargesWaived, penaltyChargesWrittenOff, penaltyChargesOutstanding, totalDueForPeriod, totalPaid,
                    totalPaidInAdvanceForPeriod, totalPaidLateForPeriod, totalWaived, totalWrittenOff, totalOutstanding,
                    totalActualCostOfLoanForPeriod, totalInstallmentAmount,selfPrincipal,partnerPrincipal,selfInterestCharged,partnerInterestCharged,totalSelfDueForPeriod,totalPartnerDueForPeriod,selfPrincipalLoanBalanceOutstanding,partnerPrincipalLoanBalanceOutstanding,
					totalSelfPaid, totalPartnerPaid,
                    totalSelfPaidLateForPeriod,totalPartnerPaidLateForPeriod,totalSelfOutstanding,totalPartnerOutstanding, null,
                    null, null, null, null, null,
                    null, null, null,selfFeeChargesOutstanding,partnerFeeChargesOutstanding,
                    selfFeeChargesDue,partnerFeeChargesDue,selfFeeChargesPaid,partnerFeeChargesPaid,selfFeeChargesWaived,partnerFeeChargesWaived,selfFeeChargesWrittenOff,partnerFeeChargesWrittenOff,selfPenaltyChargesOutstanding,partnerPenaltyChargesOutstanding,null);

        }
    }

    @Override
    public Date retrieveMinimumDateOfRepaymentTransaction(Long loanId) {
        // TODO Auto-generated method stub
        Date date = this.jdbcTemplate.queryForObject(
                "select min(transaction_date) from m_loan_transaction where loan_id=? and transaction_type_enum=2", Date.class, loanId);

        return date;
    }

    @Override
    public PaidInAdvanceData retrieveTotalPaidInAdvance(Long loanId) {
        try {
            final String sql = "  select (SUM(COALESCE(mr.principal_completed_derived, 0))"
                    + " + SUM(COALESCE(mr.interest_completed_derived, 0)) " + " + SUM(COALESCE(mr.fee_charges_completed_derived, 0)) "
                    + " + SUM(COALESCE(mr.penalty_charges_completed_derived, 0))) as total_in_advance_derived "
                    + " from m_loan ml INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id "
                    + " where ml.id=? and  mr.duedate >= " + sqlGenerator.currentDate() + " group by ml.id having "
                    + " (SUM(COALESCE(mr.principal_completed_derived, 0))  " + " + SUM(COALESCE(mr.interest_completed_derived, 0)) "
                    + " + SUM(COALESCE(mr.fee_charges_completed_derived, 0)) "
                    + "+  SUM(COALESCE(mr.penalty_charges_completed_derived, 0))) > 0";
            BigDecimal bigDecimal = this.jdbcTemplate.queryForObject(sql, BigDecimal.class, new Object[] { loanId }); // NOSONAR
            return new PaidInAdvanceData(bigDecimal);
        } catch (DataAccessException e) {
            return new PaidInAdvanceData(new BigDecimal(0));
        }
    }

    @Override
    public LoanTransactionData retrieveRefundByCashTemplate(Long loanId) {
        this.context.authenticatedUser();

        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        return retrieveRefundTemplate(loanId, LoanTransactionType.REFUND_FOR_ACTIVE_LOAN, paymentOptions, loan.getCurrency(),
                retrieveTotalPaidInAdvance(loan.getId()).getPaidInAdvance(), loan.getNetDisbursalAmount());
    }

    @Override
    public LoanTransactionData retrieveCreditBalanceRefundTemplate(Long loanId) {
        this.context.authenticatedUser();

        final Collection<PaymentTypeData> paymentOptions = null;
        final BigDecimal netDisbursal = null;
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        return retrieveRefundTemplate(loanId, LoanTransactionType.CREDIT_BALANCE_REFUND, paymentOptions, loan.getCurrency(),
                loan.getTotalOverpaid(), netDisbursal);

    }

    private LoanTransactionData retrieveRefundTemplate(Long loanId, LoanTransactionType loanTransactionType,
                                                       Collection<PaymentTypeData> paymentOptions, MonetaryCurrency currency, BigDecimal transactionAmount, BigDecimal netDisbursal) {

        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate currentDate = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

        final BigDecimal advanceAmount = BigDecimal.ZERO;
        final BigDecimal foreClosureAmount=BigDecimal.ZERO;


        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(loanTransactionType);
        return new LoanTransactionData(null, null, null, transactionType, null, currencyData, currentDate, transactionAmount, null,
                netDisbursal, null, null, null, null, null, null, null,
                null, null, false,null,null,null,null,null,null,null,null,advanceAmount,foreClosureAmount,null,null,null,null,null);
    }

    @Override
    public Collection<InterestRatePeriodData> retrieveLoanInterestRatePeriodData(LoanAccountData loanData) {
        this.context.authenticatedUser();

        if (loanData.isLoanProductLinkedToFloatingRate()) {
            final Collection<InterestRatePeriodData> intRatePeriodData = new ArrayList<>();
            final Collection<InterestRatePeriodData> intRates = this.floatingRatesReadPlatformService
                    .retrieveInterestRatePeriods(loanData.loanProductId());
            for (final InterestRatePeriodData rate : intRates) {
                if (rate.getFromDate()
                        .compareTo(Date.from(loanData.getDisbursementDate().atStartOfDay(ZoneId.systemDefault()).toInstant())) > 0
                        && loanData.isFloatingInterestRate()) {
                    updateInterestRatePeriodData(rate, loanData);
                    intRatePeriodData.add(rate);
                } else if (rate.getFromDate()
                        .compareTo(Date.from(loanData.getDisbursementDate().atStartOfDay(ZoneId.systemDefault()).toInstant())) <= 0) {
                    updateInterestRatePeriodData(rate, loanData);
                    intRatePeriodData.add(rate);
                    break;
                }
            }

            return intRatePeriodData;
        }
        return null;
    }

    private void updateInterestRatePeriodData(InterestRatePeriodData rate, LoanAccountData loan) {
        LoanProductData loanProductData = loanProductReadPlatformService.retrieveLoanProductFloatingDetails(loan.loanProductId());
        rate.setLoanProductDifferentialInterestRate(loanProductData.getInterestRateDifferential());
        rate.setLoanDifferentialInterestRate(loan.getInterestRateDifferential());

        BigDecimal effectiveInterestRate = BigDecimal.ZERO;
        effectiveInterestRate = effectiveInterestRate.add(rate.getLoanDifferentialInterestRate());
        effectiveInterestRate = effectiveInterestRate.add(rate.getLoanProductDifferentialInterestRate());
        effectiveInterestRate = effectiveInterestRate.add(rate.getInterestRate());
        if (rate.getBlrInterestRate() != null && rate.isDifferentialToBLR()) {
            effectiveInterestRate = effectiveInterestRate.add(rate.getBlrInterestRate());
        }
        rate.setEffectiveInterestRate(effectiveInterestRate);

        if (rate.getFromDate().compareTo(Date.from(loan.getDisbursementDate().atStartOfDay(ZoneId.systemDefault()).toInstant())) < 0) {
            rate.setFromDate(Date.from(loan.getDisbursementDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
    }

    @Override
    public Collection<Long> retrieveLoanIdsWithPendingIncomePostingTransactions() {
        String currentdate = formatter.format(DateUtils.getLocalDateOfTenant());
        StringBuilder sqlBuilder = new StringBuilder().append(" select distinct loan.id ").append(" from m_loan as loan ").append(
                        " inner join m_loan_recalculation_details as recdet on (recdet.loan_id = loan.id and recdet.is_compounding_to_be_posted_as_transaction is not null and recdet.is_compounding_to_be_posted_as_transaction = true) ")
                .append(" inner join m_loan_repayment_schedule as repsch on repsch.loan_id = loan.id ")
                .append(" inner join m_loan_interest_recalculation_additional_details as adddet on adddet.loan_repayment_schedule_id = repsch.id ")
                .append(" left join m_loan_transaction as trans on (trans.is_reversed <> true and trans.transaction_type_enum = 19 and trans.loan_id = loan.id and trans.transaction_date = adddet.effective_date) ")
                .append(" where loan.loan_status_id = 300 ").append(" and loan.is_npa = false ")
                .append(" and adddet.effective_date is not null ").append(" and trans.transaction_date is null ")
                .append(" and adddet.effective_date < '" + currentdate + "' ");
        try {
            return this.jdbcTemplate.queryForList(sqlBuilder.toString(), Long.class);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public LoanTransactionData retrieveLoanForeclosureTemplate(final LoanTransactionType loanTransactionType,final Long loanId, final LocalDate transactionDate) {
        this.context.authenticatedUser();

        AppUser currentUser = null;
        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication auth = context.getAuthentication();
        if (auth != null) {
            currentUser = (AppUser) auth.getPrincipal();
        }

        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        ProductCollectionConfig productCollectionConfig = loan.getLoanProduct().getProductCollectionConfig();
        loan.validateForForeclosure(transactionDate);
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate earliestUnpaidInstallmentDate = DateUtils.getLocalDateOfTenant();

       // final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loan.fetchLoanForeclosureDetail(transactionDate);


        LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment;

        // for template Short receipt and Overdue Scenario
        LoanRepaymentScheduleInstallment currentInstallment = ForeclosureUtils.getTheForeclosureInstallment(loan, transactionDate);

        ForeclosureEnum foreclosureEnum = checkForeclosureScenario(currentInstallment, transactionDate, currency, loan,productCollectionConfig);
        if (Objects.isNull(foreclosureEnum)){
            foreclosureEnum = ForeclosureEnum.INVALID;
        }
        loanRepaymentScheduleInstallment = foreclosureBasedOnScenario(foreclosureEnum, transactionDate, currentInstallment, loan.getCurrency(), loan, gstService, currentUser,loanAccrualRepository,Boolean.TRUE);
       /* List<LoanRepaymentScheduleInstallment> installments =loan.getRepaymentScheduleInstallments()
                .stream()
                .filter(installment -> (installment.getDueDate().isBefore(transactionDate)) || installment.getDueDate().isEqual(transactionDate))
                .toList();
        LoanRepaymentScheduleInstallment currentInstallment = installments.size() == 0 ?  loan.fetchRepaymentScheduleInstallment(1):  installments.get(installments.size()-1);
        if(currentInstallment.isObligationsMet()){
            loanRepaymentScheduleInstallment = loan.fetchLoanForeclosureDetail(transactionDate);
        }
        else {
            loanRepaymentScheduleInstallment =this.generateForeclosureInstallment(loan.getCurrency(),transactionDate,loan,currentInstallment,installments);
        }*/
       // final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loan.fetchLoanForeclosureDetailForTransactionScreen(transactionDate);
        BigDecimal unrecognizedIncomePortion = null;
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT);
        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final List<CodeValueData> repaymentModeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanRepaymentConstants.REPAYMENT_MODE));
        final BigDecimal outstandingLoanBalance = loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency).getAmount();
        final BigDecimal selfOutstandingLoanBalance = loanRepaymentScheduleInstallment.getSelfPrincipalOutstanding(currency).getAmount();
        final BigDecimal partnerOutstandingLoanBalance = loanRepaymentScheduleInstallment.getPartnerPrincipalOutstanding(currency).getAmount();
        final BigDecimal advanceAmount=loanRepaymentScheduleInstallment.getTotalPaidInAdvance();
        final Integer installmentNumber =loanRepaymentScheduleInstallment.getInstallmentNumber();

        final Boolean isReversed = false;

        final Money outStandingAmount = loanRepaymentScheduleInstallment.getTotalOutstanding(currency);
        final Long producutId = loan.productId();

        BigDecimal bounceCharge = ForeclosureUtils.getBounceCharge(loan,Objects.isNull(loanRepaymentScheduleInstallment.getInstallmentNumber())
                ? loan.getNumberOfRepayments() :  loanRepaymentScheduleInstallment.getInstallmentNumber());

        BigDecimal foreClosureCharge = loanRepaymentScheduleInstallment.getFeeChargesCharged();
        LoanTransactionData loanTransactionData = new LoanTransactionData(null, null, null, transactionType, null, currencyData, earliestUnpaidInstallmentDate,
                outStandingAmount.getAmount(), loan.getNetDisbursalAmount(),
                loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency).getAmount(),
                loanRepaymentScheduleInstallment.getInterestOutstanding(currency).getAmount(),
                loanRepaymentScheduleInstallment.getFeeChargesCharged(),
                loanRepaymentScheduleInstallment.getPenaltyChargesOutstanding(currency).getAmount(),
                null, unrecognizedIncomePortion, paymentTypeOptions, null, null,
                null, outstandingLoanBalance, isReversed,loanRepaymentScheduleInstallment.getSelfPrincipal(currency).getAmount(),
                loanRepaymentScheduleInstallment.getPartnerPrincipal(currency).getAmount(),
                loanRepaymentScheduleInstallment.getSelfInterestCharged(currency).getAmount(),
                loanRepaymentScheduleInstallment.getPartnerInterestCharged(currency).getAmount(),
                loanRepaymentScheduleInstallment.getSelfDue(currency).getAmount(),
                loanRepaymentScheduleInstallment.getPartnerDue(currency).getAmount(),selfOutstandingLoanBalance,
                partnerOutstandingLoanBalance,advanceAmount,foreClosureCharge,
                loanRepaymentScheduleInstallment.getSelfFeeChargesOutstanding(currency).getAmount(),
                loanRepaymentScheduleInstallment.getPartnerFeeChargesOutstanding(currency).getAmount(), installmentNumber, repaymentModeOptions, null);

        loanTransactionData.setBounceCharge(bounceCharge);

        return loanTransactionData;
    }

    private BigDecimal percentageof(BigDecimal percentage, BigDecimal amount) {
         return percentage.multiply(amount).divide(new BigDecimal(100));
    }

    private static final class CurrencyMapper implements RowMapper<CurrencyData> {

        @Override
        public CurrencyData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            return new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol, currencyNameCode);
        }

    }

    private static final class RepaymentTransactionTemplateMapper implements RowMapper<LoanTransactionData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;
        private CurrencyMapper currencyMapper = new CurrencyMapper();

        RepaymentTransactionTemplateMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String schema() {
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.append(
                    "(CASE WHEN max(tr.transaction_date)>ls.dueDate THEN max(tr.transaction_date) ELSE ls.dueDate END) as transactionDate, ");
            sqlBuilder.append(
                    "ls.principal_amount - coalesce(ls.principal_writtenoff_derived,0) - coalesce(ls.principal_completed_derived,0) as principalDue, ");
            sqlBuilder.append(
                    "ls.self_principal_amount - coalesce(ls.self_principal_writtenoff_derived,0) - coalesce(ls.self_principal_completed_derived,0) as selfPrincipalDue, ");
            sqlBuilder.append(
                    "ls.partner_principal_amount - coalesce(ls.partner_principal_writtenoff_derived,0) - coalesce(ls.partner_principal_completed_derived,0) as partnerPrincipalDue, ");
            sqlBuilder.append(
                    "ls.interest_amount - coalesce(ls.interest_completed_derived,0) - coalesce(ls.interest_waived_derived,0) - coalesce(ls.interest_writtenoff_derived,0) as interestDue, ");
            sqlBuilder.append(
                    "ls.self_principal_amount - coalesce(ls.self_principal_writtenoff_derived,0) - coalesce(ls.self_principal_completed_derived,0) as selfPrincipal, ");
            sqlBuilder.append(
                    "ls.partner_principal_amount - coalesce(ls.partner_principal_writtenoff_derived,0) - coalesce(ls.partner_principal_completed_derived,0) as partnerPrincipal, ");
            sqlBuilder.append(
                    "ls.self_interest_amount - coalesce(ls.self_interest_completed_derived,0) - coalesce(ls.self_interest_waived_derived,0) - coalesce(ls.self_interest_writtenoff_derived,0) as selfInterestCharged, ");
            sqlBuilder.append(
                    "ls.partner_interest_amount - coalesce(ls.partner_interest_completed_derived,0) - coalesce(ls.partner_interest_waived_derived,0) - coalesce(ls.partner_interest_writtenoff_derived,0) as partnerInterestCharged, ");
            sqlBuilder.append(
                    "ls.fee_charges_amount - coalesce(ls.fee_charges_completed_derived,0) - coalesce(ls.fee_charges_writtenoff_derived,0) - coalesce(ls.fee_charges_waived_derived,0) as feeDue, ");
            sqlBuilder.append(
                    "ls.penalty_charges_amount - coalesce(ls.penalty_charges_completed_derived,0) - coalesce(ls.penalty_charges_writtenoff_derived,0) - coalesce(ls.penalty_charges_waived_derived,0) as penaltyDue, ");
            sqlBuilder.append(
                    "l.currency_code as currencyCode, l.currency_digits as currencyDigits, l.currency_multiplesof as inMultiplesOf, l.net_disbursal_amount as netDisbursalAmount,l.broken_interest_derived as brokenInterestDerived,l.broken_interest_repaid as brokenInterestRepaid,  rc."
                            + sqlGenerator.escape("name") + " as currencyName, ");
            sqlBuilder.append("rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode,ls.total_paid_in_advance_derived as advanceAmount,ls.installment as installmentNumber,  ");
            sqlBuilder.append("tr.repayment_mode_cv_id as repaymentModeId, ");
            sqlBuilder.append("cvrp.code_value as repaymentModeValue ");
            sqlBuilder.append("FROM m_loan l ");
            sqlBuilder.append(
                    "LEFT JOIN m_loan_transaction tr ON tr.loan_id = l.id AND tr.transaction_type_enum = ? and tr.is_reversed = false ");
            sqlBuilder.append("join m_currency rc on rc." + sqlGenerator.escape("code") + " = l.currency_code ");
            sqlBuilder.append("left join m_code_value cvrp on cvrp.id = tr.repayment_mode_cv_id ");
            sqlBuilder.append("JOIN m_loan_repayment_schedule ls ON ls.loan_id = l.id AND ls.completed_derived = 0 ");
            sqlBuilder.append("join( ");
            sqlBuilder.append("(select min(ls.duedate) datedue,ls.loan_id from m_loan_repayment_schedule ls ");
            sqlBuilder.append("where ls.loan_id = ? and ls.completed_derived = 0) )asq ");
            sqlBuilder.append("on asq.loan_id = ls.loan_id and asq.datedue = ls.duedate ");
            sqlBuilder.append("WHERE l.id = ? ");
            sqlBuilder.append("GROUP BY ls.duedate, ");
            sqlBuilder.append("ls.principal_amount,ls.principal_completed_derived,ls.principal_writtenoff_derived, ");
            sqlBuilder
                    .append("ls.interest_amount,ls.interest_completed_derived,ls.interest_waived_derived,ls.interest_writtenoff_derived, ");
            sqlBuilder.append(
                    "ls.fee_charges_amount,ls.fee_charges_completed_derived, ls.fee_charges_writtenoff_derived, ls.fee_charges_waived_derived, ");
            sqlBuilder.append(
                    "ls.penalty_charges_amount, ls.penalty_charges_completed_derived, ls.penalty_charges_writtenoff_derived, ls.penalty_charges_waived_derived ");
            return sqlBuilder.toString();

        }

        @Override
        public LoanTransactionData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT);
            final CurrencyData currencyData = this.currencyMapper.mapRow(rs, rowNum);
            final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
            final BigDecimal principalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDue");
            final BigDecimal selfPrincipalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
            final BigDecimal partnerPrincipalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");

            final BigDecimal advanceAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "advanceAmount");
            final Integer installmentNumber = JdbcSupport.getInteger(rs, "installmentNumber");

            final BigDecimal interestDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestDue");
            final BigDecimal selfInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
            final BigDecimal partnerInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");

            final BigDecimal feeDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeDue");
            final BigDecimal penaltyDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyDue");
            final BigDecimal totalDue = principalPortion.add(interestDue).add(feeDue).add(penaltyDue);
            final BigDecimal selfDue = selfPrincipalPortion.add(selfInterestCharged).add(feeDue).add(penaltyDue);
            final BigDecimal partnerDue = partnerPrincipalPortion.add(partnerInterestCharged).add(feeDue).add(penaltyDue);
             final Long repaymentModeId = JdbcSupport.getLong(rs, "repaymentModeId");
             final String repaymentModeValue = rs.getString("repaymentModeValue");
             final CodeValueData repaymentMode = CodeValueData.instance(repaymentModeId, repaymentModeValue);
            final BigDecimal outstandingLoanBalance = null;
            final BigDecimal selfOutstandingLoanBalance = null;
            final BigDecimal partnerOutstandingLoanBalance = null;

            final BigDecimal unrecognizedIncomePortion = null;
            final BigDecimal overPaymentPortion = null;
            final BigDecimal netDisbursalAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "netDisbursalAmount");
            final Long id = null;
            final Long officeId = null;
            final String officeName = null;
            boolean manuallyReversed = false;
            final PaymentDetailData paymentDetailData = null;
            final String externalId = null;
            final AccountTransferData transfer = null;
            final BigDecimal fixedEmiAmount = null;
            final BigDecimal foreClosureAmount=BigDecimal.ZERO;
            final BigDecimal selfFee = BigDecimal.ZERO;
            final BigDecimal parnerFee = BigDecimal.ZERO;
            final List<CodeValueData> repaymentModeOptions=new ArrayList<>();
            //final BigDecimal advanceAmount=null;
//            final BigDecimal selfPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
//            final BigDecimal partnerPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");
//            final BigDecimal selfInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
//            final BigDecimal partnerInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");

            return new LoanTransactionData(id, officeId, officeName, transactionType, paymentDetailData, currencyData, date, totalDue,
                    netDisbursalAmount, principalPortion, interestDue, feeDue, penaltyDue, overPaymentPortion, externalId, transfer,
                    fixedEmiAmount, outstandingLoanBalance, unrecognizedIncomePortion, manuallyReversed,selfPrincipalPortion,partnerPrincipalPortion,selfInterestCharged,partnerInterestCharged,selfDue,partnerDue,
                    selfOutstandingLoanBalance,partnerOutstandingLoanBalance,advanceAmount,foreClosureAmount,selfFee,parnerFee,installmentNumber,repaymentModeOptions,repaymentMode);
        }

    }

    @Override
    public Long retrieveLoanIdByAccountNumber(String loanAccountNumber) {
        try {
            return this.jdbcTemplate.queryForObject("select l.id from m_loan l where l.account_no = ?", Long.class, loanAccountNumber);

        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public String retrieveAccountNumberByAccountId(Long accountId) {
        try {
            final String sql = "select loan.account_no from m_loan loan where loan.id = ?";
            return this.jdbcTemplate.queryForObject(sql, String.class, accountId);
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(accountId, e);
        }
    }

    @Override
    public Integer retrieveNumberOfActiveLoans() {
        final String sql = "select count(*) from m_loan";
        return this.jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public CollectionData retrieveLoanCollectionData(Long loanId) {
        final CollectionDataMapper mapper = new CollectionDataMapper(sqlGenerator);
        String sql = "select " + mapper.schema();
        CollectionData collectionData = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { loanId }); // NOSONAR
        return collectionData;
    }

    private static final class CollectionDataMapper implements RowMapper<CollectionData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        CollectionDataMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String schema() {
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.append(
                    "l.id as loanId, coalesce((l.approved_principal - l.principal_disbursed_derived), 0) as availableDisbursementAmount, ");
            sqlBuilder.append(sqlGenerator.dateDiff(sqlGenerator.currentDate(), "laa.overdue_since_date_derived") + " as pastDueDays, ");
            sqlBuilder.append(
                    "(select coalesce(min(lrs.duedate), null) as duedate from m_loan_repayment_schedule lrs where lrs.loan_id=l.id and lrs.completed_derived is false and lrs.duedate >= "
                            + sqlGenerator.currentDate() + ") as nextPaymentDueDate, ");
            sqlBuilder.append(sqlGenerator.dateDiff(sqlGenerator.currentDate(), "laa.overdue_since_date_derived") + " as delinquentDays, ");
            sqlBuilder.append(
                    sqlGenerator.currentDate() + " as delinquentDate, coalesce(laa.total_overdue_derived, 0) as delinquentAmount, ");
            sqlBuilder.append("lre.transactionDate as lastPaymentDate, coalesce(lre.amount, 0) as lastPaymentAmount ");
            sqlBuilder.append("from m_loan l left join m_loan_arrears_aging laa on laa.loan_id = l.id ");
            sqlBuilder.append(
                    "left join (select lt.loan_id, lt.transaction_date as transactionDate, lt.amount as amount from m_loan_transaction lt ");
            sqlBuilder.append(
                    "where lt.is_reversed = false and lt.transaction_type_enum=2 order by lt.transaction_date desc limit 1) lre on lre.loan_id = l.id ");
            sqlBuilder.append("where l.id=? ");
            return sqlBuilder.toString();
        }

        @Override
        public CollectionData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final LocalDate nextPaymentDueDate = JdbcSupport.getLocalDate(rs, "nextPaymentDueDate");
            final LocalDate delinquentDate = JdbcSupport.getLocalDate(rs, "delinquentDate");
            final LocalDate lastPaymentDate = JdbcSupport.getLocalDate(rs, "lastPaymentDate");
            final BigDecimal availableDisbursementAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "availableDisbursementAmount");
            final BigDecimal delinquentAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "delinquentAmount");
            final BigDecimal lastPaymentAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "lastPaymentAmount");
            final int pastDueDays = rs.getInt("pastDueDays");
            final int delinquentDays = rs.getInt("delinquentDays");

            return CollectionData.instance(availableDisbursementAmount, pastDueDays, nextPaymentDueDate, delinquentDays, delinquentDate,
                    delinquentAmount, lastPaymentDate, lastPaymentAmount);
        }
    }
}
