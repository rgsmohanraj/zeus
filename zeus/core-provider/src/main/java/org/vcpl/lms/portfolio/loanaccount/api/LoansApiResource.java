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
package org.vcpl.lms.portfolio.loanaccount.api;

import static org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations.interestType;
import static org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.vcpl.lms.accounting.common.AccountingDropdownReadPlatformService;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.bulkimport.data.GlobalEntityType;
import org.vcpl.lms.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.vcpl.lms.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.infrastructure.configuration.domain.ConfigurationDomainService;
import org.vcpl.lms.infrastructure.core.api.ApiParameterHelper;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.api.JsonQuery;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.core.data.PaginationParameters;
import org.vcpl.lms.infrastructure.core.data.UploadRequest;
import org.vcpl.lms.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.core.service.Page;
import org.vcpl.lms.infrastructure.core.service.SearchParameters;
import org.vcpl.lms.infrastructure.dataqueries.api.DataTableApiConstant;
import org.vcpl.lms.infrastructure.dataqueries.data.DatatableData;
import org.vcpl.lms.infrastructure.dataqueries.data.EntityTables;
import org.vcpl.lms.infrastructure.dataqueries.data.StatusEnum;
import org.vcpl.lms.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.staff.data.StaffData;
import org.vcpl.lms.portfolio.account.PortfolioAccountType;
import org.vcpl.lms.portfolio.account.data.PortfolioAccountDTO;
import org.vcpl.lms.portfolio.account.data.PortfolioAccountData;
import org.vcpl.lms.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.vcpl.lms.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.vcpl.lms.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.vcpl.lms.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.vcpl.lms.portfolio.calendar.data.CalendarData;
import org.vcpl.lms.portfolio.calendar.domain.CalendarEntityType;
import org.vcpl.lms.portfolio.calendar.service.CalendarReadPlatformService;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.charge.data.ColendingChargeData;
import org.vcpl.lms.portfolio.charge.domain.ChargeTimeType;
import org.vcpl.lms.portfolio.charge.service.ChargeReadPlatformService;
import org.vcpl.lms.portfolio.client.data.ClientData;
import org.vcpl.lms.portfolio.collateral.data.CollateralData;
import org.vcpl.lms.portfolio.collateral.service.CollateralReadPlatformService;
import org.vcpl.lms.portfolio.collateralmanagement.data.LoanCollateralResponseData;
import org.vcpl.lms.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformService;
import org.vcpl.lms.portfolio.floatingrates.data.InterestRatePeriodData;
import org.vcpl.lms.portfolio.fund.data.FundData;
import org.vcpl.lms.portfolio.fund.service.FundReadPlatformService;
import org.vcpl.lms.portfolio.group.data.GroupGeneralData;
import org.vcpl.lms.portfolio.group.service.GroupReadPlatformService;
import org.vcpl.lms.portfolio.loanaccount.data.*;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanTemplateTypeRequiredException;
import org.vcpl.lms.portfolio.loanaccount.exception.NotSupportedLoanTemplateTypeException;
import org.vcpl.lms.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.vcpl.lms.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryReadPlatformService;
import org.vcpl.lms.portfolio.loanaccount.service.*;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.ServicerFeeData;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeReadPlatformService;
import org.vcpl.lms.portfolio.loanproduct.LoanProductConstants;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductData;
import org.vcpl.lms.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.vcpl.lms.portfolio.loanproduct.domain.InterestMethod;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesData;
import org.vcpl.lms.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.vcpl.lms.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.vcpl.lms.portfolio.note.data.NoteData;
import org.vcpl.lms.portfolio.note.domain.NoteType;
import org.vcpl.lms.portfolio.note.service.NoteReadPlatformService;
import org.vcpl.lms.portfolio.partner.data.PartnerData;
import org.vcpl.lms.portfolio.partner.domain.Partner;
import org.vcpl.lms.portfolio.partner.domain.PartnerRepositoryWrapper;
import org.vcpl.lms.portfolio.partner.service.PartnerReadPlatformService;
import org.vcpl.lms.portfolio.rate.data.RateData;
import org.vcpl.lms.portfolio.rate.service.RateReadService;
import org.vcpl.lms.portfolio.savings.DepositAccountType;
import org.vcpl.lms.portfolio.savings.domain.SavingsAccountStatusType;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/loans")
@Component
@Scope("singleton")
@Tag(name = "Loans", description = "The API concept of loans models the loan application process and the loan contract/monitoring process.\n"
        + "\n" + "Field Descriptions\n" + "accountNo\n"
        + "The account no. associated with this loan. Is auto generated if not provided at loan application creation time.\n"
        + "externalId\n" + "A place to put an external reference for this loan e.g. The ID another system uses.\n"
        + "If provided, it must be unique.\n" + "fundId\n" + "Optional: For associating a loan with a given fund.\n" + "loanOfficerId\n"
        + "Optional: For associating a loan with a given staff member who is a loan officer.\n" + "loanPurposeId\n"
        + "Optional: For marking a loan with a given loan purpose option. Loan purposes are configurable and can be setup by system admin through code/code values screens.\n"
        + "principal\n" + "The loan amount to be disbursed to through loan.\n" + "loanTermFrequency\n" + "The length of loan term\n"
        + "Used like: loanTermFrequency loanTermFrequencyType\n" + "e.g. 12 Months\n" + "loanTermFrequencyType\n"
        + "The loan term period to use. Used like: loanTermFrequency loanTermFrequencyType\n"
        + "e.g. 12 Months Example Values: 0=Days, 1=Weeks, 2=Months, 3=Years\n" + "numberOfRepayments\n"
        + "Number of installments to repay.\n" + "Used like: numberOfRepayments Every repaymentEvery repaymentFrequencyType\n"
        + "e.g. 10 (repayments) Every 12 Weeks\n" + "repaymentEvery\n"
        + "Used like: numberOfRepayments Every repaymentEvery repaymentFrequencyType\n" + "e.g. 10 (repayments) Every 12 Weeks\n"
        + "repaymentFrequencyType\n" + "Used like: numberOfRepayments Every repaymentEvery repaymentFrequencyType\n"
        + "e.g. 10 (repayments) Every 12 Weeks \n" + "Example Values: 0=Days, 1=Weeks, 2=Months\n" + "interestRatePerPeriod\n"
        + "Interest Rate.\n" + "Used like: interestRatePerPeriod % interestRateFrequencyType - interestType\n"
        + "e.g. 12.0000% Per year - Declining Balance\n" + "interestRateFrequencyType\n"
        + "Used like: interestRatePerPeriod% interestRateFrequencyType - interestType\n" + "e.g. 12.0000% Per year - Declining Balance \n"
        + "Example Values: 2=Per month, 3=Per year\n" + "graceOnPrincipalPayment\n"
        + "Optional: Integer - represents the number of repayment periods that grace should apply to the principal component of a repayment period.\n"
        + "graceOnInterestPayment\n"
        + "Optional: Integer - represents the number of repayment periods that grace should apply to the interest component of a repayment period. Interest is still calculated but offset to later repayment periods.\n"
        + "graceOnInterestCharged\n" + "Optional: Integer - represents the number of repayment periods that should be interest-free.\n"
        + "graceOnArrearsAgeing\n"
        + "Optional: Integer - Used in Arrears calculation to only take into account loans that are more than graceOnArrearsAgeing days overdue.\n"
        + "interestChargedFromDate\n" + "Optional: Date - The date from with interest is to start being charged.\n"
        + "expectedDisbursementDate\n" + "The proposed disbursement date of the loan so a proposed repayment schedule can be provided.\n"
        + "submittedOnDate\n" + "The date the loan application was submitted by applicant.\n" + "linkAccountId\n"
        + "The Savings Account id for linking with loan account for payments.\n" + "amortizationType\n"
        + "Example Values: 0=Equal principle payments, 1=Equal installments\n" + "interestType\n"
        + "Used like: interestRatePerPeriod% interestRateFrequencyType - interestType\n" + "e.g. 12.0000% Per year - Declining Balance \n"
        + "Example Values: 0=Declining Balance, 1=Flat\n" + "interestCalculationPeriodType\n"
        + "Example Values: 0=Daily, 1=Same as repayment period\n" + "allowPartialPeriodInterestCalcualtion\n"
        + "This value will be supported along with interestCalculationPeriodType as Same as repayment period to calculate interest for partial periods. Example: Interest charged from is 5th of April , Principal is 10000 and interest is 1% per month then the interest will be (10000 * 1%)* (25/30) , it calculates for the month first then calculates exact periods between start date and end date(can be a decimal)\n"
        + "inArrearsTolerance\n" + "The amount that can be 'waived' at end of all loan payments because it is too small to worry about.\n"
        + "This is also the tolerance amount assessed when determining if a loan is in arrears.\n" + "transactionProcessingStrategyId\n"
        + "An enumeration that indicates the type of transaction processing strategy to be used. This relates to functionality that is also known as Payment Application Logic.\n"
        + "A number of out of the box approaches exist, some are custom to specific MFIs, some are more general and indicate the order in which payments are processed.\n"
        + "\n"
        + "Refer to the Payment Application Logic / Transaction Processing Strategy section in the appendix for more detailed overview of each available payment application logic provided out of the box.\n"
        + "\n" + "List of current approaches:\n" + "1 = Zeus style \n" + "2 = Heavensfamily (Custom MFI approach)\n"
        + "3 = Creocore (Custom MFI approach)\n" + "4 = RBI (India)\n" + "5 = Principal Interest Penalties Fees Order\n"
        + "6 = Interest Principal Penalties Fees Order\n" + "7 = Early Payment Strategy\n" + "loanType\n"
        + "To represent different type of loans.\n" + "At present there are three type of loans are supported. \n"
        + "Available loan types:\n" + "individual: Loan given to individual member\n" + "group: Loan given to group as a whole\n"
        + "jlg: Joint liability group loan given to members in a group on individual basis. JLG loan can be given to one or more members in a group.\n"
        + "recalculationRestFrequencyDate\n"
        + "Specifies rest frequency start date for interest recalculation. This date must be before or equal to disbursement date\n"
        + "recalculationCompoundingFrequencyDate\n"
        + "Specifies compounding frequency start date for interest recalculation. This date must be equal to disbursement date")
public class LoansApiResource {

    private final Set<String> loanDataParameters = new HashSet<>(Arrays.asList("id", "accountNo", "status", "externalId", "clientId",
            "group", "loanProductId", "loanProductName", "loanProductDescription", "isLoanProductLinkedToFloatingRate", "fundId",
            "fundName", "loanPurposeId", "loanPurposeName", "loanOfficerId", "loanOfficerName", "currency", "principal", "totalOverpaid",
            "inArrearsTolerance", "termFrequency", "termPeriodFrequencyType", "numberOfRepayments", "repaymentEvery",
            "interestRatePerPeriod", "annualInterestRate", "repaymentFrequencyType", "transactionProcessingStrategyId",
            "transactionProcessingStrategyName", "interestRateFrequencyType", "amortizationType", "interestType",
            "interestCalculationPeriodType", LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
            "expectedFirstRepaymentOnDate", "graceOnPrincipalPayment", "recurringMoratoriumOnPrincipalPeriods", "graceOnInterestPayment",
            "graceOnInterestCharged", "interestChargedFromDate", "timeline", "totalFeeChargesAtDisbursement", "summary",
            "repaymentSchedule", "transactions", "charges", "collateral", "guarantors", "meeting", "productOptions",
            "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions", "repaymentFrequencyTypeOptions",
            "repaymentFrequencyNthDayTypeOptions", "repaymentFrequencyDaysOfWeekTypeOptions", "termFrequencyTypeOptions",
            "interestRateFrequencyTypeOptions", "fundOptions", "repaymentStrategyOptions", "chargeOptions", "loanOfficerOptions",
            "loanPurposeOptions", "loanCollateralOptions", "chargeTemplate", "calendarOptions", "syncDisbursementWithMeeting",
            "loanCounter", "loanProductCounter", "notes", "accountLinkingOptions", "linkedAccount", "interestRateDifferential",
            "isFloatingInterestRate", "interestRatesPeriods", LoanApiConstants.canUseForTopup, LoanApiConstants.isTopup,
            LoanApiConstants.loanIdToClose, LoanApiConstants.topupAmount, LoanApiConstants.clientActiveLoanOptions,
            LoanApiConstants.datatables, LoanProductConstants.RATES_PARAM_NAME, "accruals"));

    private final Set<String> loanApprovalDataParameters = new HashSet<>(Arrays.asList("approvalDate", "approvalAmount"));
    final Set<String> glimAccountsDataParameters = new HashSet<>(Arrays.asList("glimId", "groupId", "clientId", "parentLoanAccountNo",
            "parentPrincipalAmount", "childLoanAccountNo", "childPrincipalAmount", "clientName"));
    private final String resourceNameForPermissions = "LOAN";

    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final CollateralReadPlatformService loanCollateralReadPlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanApprovalData> loanApprovalDataToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final FromJsonHelper fromJsonHelper;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final NoteReadPlatformService noteReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final RateReadService rateReadService;
    private final ConfigurationDomainService configurationDomainService;
    private final DefaultToApiJsonSerializer<GlimRepaymentTemplate> glimTemplateToApiJsonSerializer;
    private final GLIMAccountInfoReadPlatformService glimAccountInfoReadPlatformService;
    private final LoanCollateralManagementReadPlatformService loanCollateralManagementReadPlatformService;
    private final LoanReadPlatformServiceImpl loanReadPlatformServiceImp;
    private final PartnerRepositoryWrapper partnerRepositoryWrapper;
    private final DefaultToApiJsonSerializer<PartnerandProductData> partnerandProductDataDefaultToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanFilterData> loanFilterDataDefaultToApiJsonSerializer;

    private final DefaultToApiJsonSerializer<ImportDocumentData> bulkReportsFilterDefaultToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<VpayTransactionDetailsData> vpayTransactionDetailsDataDefaultToApiJsonSerializer;
    private final VPayIntegrationServiceImpl vPayIntegrationService;
    private final PartnerReadPlatformService partnerReadPlatformService;

    private  final LoanRepositoryWrapper loanRepositoryWrapper ;

    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;

    private final ServicerFeeReadPlatformService servicerFeeReadPlatformService;

    public LoansApiResource(final PlatformSecurityContext context, final LoanReadPlatformService loanReadPlatformService,
                            final LoanProductReadPlatformService loanProductReadPlatformService,
                            final LoanDropdownReadPlatformService dropdownReadPlatformService, final FundReadPlatformService fundReadPlatformService,
                            final ChargeReadPlatformService chargeReadPlatformService, final LoanChargeReadPlatformService loanChargeReadPlatformService,
                            final CollateralReadPlatformService loanCollateralReadPlatformService,
                            final LoanScheduleCalculationPlatformService calculationPlatformService,
                            final GuarantorReadPlatformService guarantorReadPlatformService,
                            final CodeValueReadPlatformService codeValueReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
                            final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer,
                            final DefaultToApiJsonSerializer<LoanApprovalData> loanApprovalDataToApiJsonSerializer,
                            final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer,
                            final ApiRequestParameterHelper apiRequestParameterHelper, final FromJsonHelper fromJsonHelper,
                            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                            final CalendarReadPlatformService calendarReadPlatformService, final NoteReadPlatformService noteReadPlatformService,
                            final PortfolioAccountReadPlatformService portfolioAccountReadPlatformServiceImpl,
                            final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
                            final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService,
                            final AccountDetailsReadPlatformService accountDetailsReadPlatformService,
                            final EntityDatatableChecksReadService entityDatatableChecksReadService,
                            final BulkImportWorkbookService bulkImportWorkbookService,
                            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService, final RateReadService rateReadService,
                            final ConfigurationDomainService configurationDomainService,
                            final DefaultToApiJsonSerializer<GlimRepaymentTemplate> glimTemplateToApiJsonSerializer,
                            final GLIMAccountInfoReadPlatformService glimAccountInfoReadPlatformService,
                            final LoanCollateralManagementReadPlatformService loanCollateralManagementReadPlatformService,
                            final LoanReadPlatformServiceImpl loanReadPlatformServiceImp, final PartnerRepositoryWrapper partnerRepositoryWrapper,
                            final DefaultToApiJsonSerializer<PartnerandProductData> partnerandProductDataDefaultToApiJsonSerializer,
                            DefaultToApiJsonSerializer<PartnerandProductData> partnerandProductDataDefaultToApiJsonSerializer1,
                            DefaultToApiJsonSerializer<LoanFilterData> loanFilterDataDefaultToApiJsonSerializer,
                            DefaultToApiJsonSerializer<ImportDocumentData> bulkReportsFilterDefaultToApiJsonSerializer, DefaultToApiJsonSerializer<VpayTransactionDetailsData> vpayTransactionDetailsDataDefaultToApiJsonSerializer,
                            final VPayIntegrationServiceImpl vPayIntegrationService, final PartnerReadPlatformService partnerReadPlatformService, final LoanRepositoryWrapper loanRepositoryWrapper, final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService, ServicerFeeReadPlatformService servicerFeeReadPlatformService) {
        this.context = context;
        this.loanReadPlatformService = loanReadPlatformService;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.loanCollateralReadPlatformService = loanCollateralReadPlatformService;
        this.calculationPlatformService = calculationPlatformService;
        this.guarantorReadPlatformService = guarantorReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.loanApprovalDataToApiJsonSerializer = loanApprovalDataToApiJsonSerializer;
        this.loanScheduleToApiJsonSerializer = loanScheduleToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.fromJsonHelper = fromJsonHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.noteReadPlatformService = noteReadPlatformService;
        this.portfolioAccountReadPlatformService = portfolioAccountReadPlatformServiceImpl;
        this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
        this.loanScheduleHistoryReadPlatformService = loanScheduleHistoryReadPlatformService;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.entityDatatableChecksReadService = entityDatatableChecksReadService;
        this.rateReadService = rateReadService;
        this.bulkImportWorkbookService = bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService = bulkImportWorkbookPopulatorService;
        this.configurationDomainService = configurationDomainService;
        this.glimTemplateToApiJsonSerializer = glimTemplateToApiJsonSerializer;
        this.glimAccountInfoReadPlatformService = glimAccountInfoReadPlatformService;
        this.loanCollateralManagementReadPlatformService = loanCollateralManagementReadPlatformService;
        this.loanReadPlatformServiceImp=loanReadPlatformServiceImp;
        this.partnerRepositoryWrapper = partnerRepositoryWrapper;
        this.partnerandProductDataDefaultToApiJsonSerializer = partnerandProductDataDefaultToApiJsonSerializer1;
        this.loanFilterDataDefaultToApiJsonSerializer = loanFilterDataDefaultToApiJsonSerializer;
        this.bulkReportsFilterDefaultToApiJsonSerializer = bulkReportsFilterDefaultToApiJsonSerializer;
        this.vpayTransactionDetailsDataDefaultToApiJsonSerializer = vpayTransactionDetailsDataDefaultToApiJsonSerializer;
        this.vPayIntegrationService = vPayIntegrationService;
        this.partnerReadPlatformService = partnerReadPlatformService;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
        this.servicerFeeReadPlatformService = servicerFeeReadPlatformService;
    }

    /*
     * This template API is used for loan approval, ideally this should be invoked on loan that are pending for
     * approval. But system does not validate the status of the loan, it returns the template irrespective of loan
     * status
     */

    @GET
    @Path("{loanId}/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveApprovalTemplate(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("templateType") @Parameter(description = "templateType") final String templateType,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LoanApprovalData loanApprovalTemplate = null;

        if (templateType == null) {
            final String errorMsg = "Loan template type must be provided";
            throw new LoanTemplateTypeRequiredException(errorMsg);
        } else if (templateType.equals("approval")) {
            loanApprovalTemplate = this.loanReadPlatformService.retrieveApprovalTemplate(loanId);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.loanApprovalDataToApiJsonSerializer.serialize(settings, loanApprovalTemplate, this.loanApprovalDataParameters);

    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Requests:\n" + "\n"
            + "loans/" +
            "" +
            "template?templateType=individual&clientId=1\n" + "\n" + "\n"
            + "loans/template?templateType=individual&clientId=1&productId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansTemplateResponse.class))) })
    public String template(@QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("groupId") @Parameter(description = "groupId") final Long groupId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("templateType") @Parameter(description = "templateType") final String templateType,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("false") @QueryParam("activeOnly") @Parameter(description = "activeOnly") final boolean onlyActive,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        // template
        final Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(onlyActive);

        // options
        Collection<StaffData> allowedLoanOfficers = null;
        Collection<CodeValueData> loanCollateralOptions = null;
        Collection<CalendarData> calendarOptions = null;
        LoanAccountData newLoanAccount = null;
        Long officeId = null;
        Collection<PortfolioAccountData> accountLinkingOptions = null;
        Partner partner = null;
        boolean isRatesEnabled = this.configurationDomainService.isSubRatesEnabled();

        if (productId != null) {
            newLoanAccount = this.loanReadPlatformService.retrieveLoanProductDetailsTemplate(productId, clientId, groupId);
        }

        if(productId != null){
            LoanProductData loanProduct =this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            partner = this.partnerRepositoryWrapper.findOneWithNotFoundDetection(loanProduct.getPartnerId());
        }

        if (templateType == null) {
            final String errorMsg = "Loan template type must be provided";
            throw new LoanTemplateTypeRequiredException(errorMsg);
        } else if (templateType.equals("collateral")) {
            loanCollateralOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
            newLoanAccount = LoanAccountData.collateralTemplate(loanCollateralOptions);
        } else {
            // for JLG loan both client and group details are required
            if (templateType.equals("individual") || templateType.equals("jlg")) {

                if (clientId == null) {
                    newLoanAccount = newLoanAccount == null ? LoanAccountData.emptyTemplate() : newLoanAccount;
                } else {
                    final LoanAccountData loanAccountClientDetails = this.loanReadPlatformService.retrieveClientDetailsTemplate(clientId);

                    officeId = loanAccountClientDetails.officeId();
                    newLoanAccount = newLoanAccount == null ? loanAccountClientDetails
                            : LoanAccountData.populateClientDefaults(newLoanAccount, loanAccountClientDetails);
                }

                // if it's JLG loan add group details
                if (templateType.equals("jlg")) {
                    final GroupGeneralData group = this.groupReadPlatformService.retrieveOne(groupId);
                    newLoanAccount = LoanAccountData.associateGroup(newLoanAccount, group);
                    calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                }

            } else if (templateType.equals("group")) {

                final LoanAccountData loanAccountGroupData = this.loanReadPlatformService.retrieveGroupDetailsTemplate(groupId);
                officeId = loanAccountGroupData.groupOfficeId();
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                newLoanAccount = newLoanAccount == null ? loanAccountGroupData
                        : LoanAccountData.populateGroupDefaults(newLoanAccount, loanAccountGroupData);
                accountLinkingOptions = getaccountLinkingOptions(newLoanAccount, clientId, groupId);

            } else if (templateType.equals("jlgbulk")) {
                // get group details along with members in that group
                final LoanAccountData loanAccountGroupData = this.loanReadPlatformService.retrieveGroupAndMembersDetailsTemplate(groupId);
                officeId = loanAccountGroupData.groupOfficeId();
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                newLoanAccount = newLoanAccount == null ? loanAccountGroupData
                        : LoanAccountData.populateGroupDefaults(newLoanAccount, loanAccountGroupData);
                if (productId != null) {
                    Map<Long, Integer> memberLoanCycle = new HashMap<>();
                    Collection<ClientData> members = loanAccountGroupData.groupData().clientMembers();
                    accountLinkingOptions = new ArrayList<>();
                    if (members != null) {
                        for (ClientData clientData : members) {
                            Integer loanCounter = this.loanReadPlatformService.retriveLoanCounter(clientData.id(), productId);
                            memberLoanCycle.put(clientData.id(), loanCounter);
                            accountLinkingOptions.addAll(getaccountLinkingOptions(newLoanAccount, clientData.id(), groupId));
                        }
                    }

                    newLoanAccount = LoanAccountData.associateMemberVariations(newLoanAccount, memberLoanCycle);
                }

            } else {
                final String errorMsg = "Loan template type '" + templateType + "' is not supported";
                throw new NotSupportedLoanTemplateTypeException(errorMsg, templateType);
            }

            allowedLoanOfficers = this.loanReadPlatformService.retrieveAllowedLoanOfficers(officeId, staffInSelectedOfficeOnly);

            if (clientId != null) {
                accountLinkingOptions = getaccountLinkingOptions(newLoanAccount, clientId, groupId);
            }

            // add product options, allowed loan officers and calendar options
            // (calendar options will be null in individual loan)
            newLoanAccount = LoanAccountData.associationsAndTemplate(newLoanAccount, productOptions, allowedLoanOfficers, calendarOptions,
                    accountLinkingOptions, isRatesEnabled);
        }
        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService
                .retrieveTemplates(StatusEnum.CREATE.getCode().longValue(), EntityTables.LOAN.getName(), productId);
        newLoanAccount.setDatatables(datatableTemplates);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, newLoanAccount, this.loanDataParameters);
    }

    private Collection<PortfolioAccountData> getaccountLinkingOptions(final LoanAccountData newLoanAccount, final Long clientId,
            final Long groupId) {
        final CurrencyData currencyData = newLoanAccount.currency();
        String currencyCode = null;
        if (currencyData != null) {
            currencyCode = currencyData.code();
        }
        final long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue() };
        final PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(), clientId,
                currencyCode, accountStatus, DepositAccountType.SAVINGS_DEPOSIT.getValue());
        if (groupId != null) {
            portfolioAccountDTO.setGroupId(groupId);
        }
        return this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
    }

    @GET
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan", description = "Note: template=true parameter doesn't apply to this resource."
            + "Example Requests:\n" + "\n" + "loans/1\n" + "\n" + "\n" + "loans/1?fields=id,principal,annualInterestRate\n" + "\n" + "\n"
            + "loans/1?associations=all\n" + "\n" + "loans/1?associations=all&exclude=guarantors\n" + "\n" + "\n"
            + "loans/1?fields=id,principal,annualInterestRate&associations=repaymentSchedule,transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansLoanIdResponse.class))) })
    public String retrieveLoan(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LoanAccountData loanBasicDetails = this.loanReadPlatformService.retrieveOne(loanId);
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        if (loanBasicDetails.isInterestRecalculationEnabled()) {
            Collection<CalendarData> interestRecalculationCalendarDatas = this.calendarReadPlatformService.retrieveCalendarsByEntity(
                    loanBasicDetails.getInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue(),
                    null);
            CalendarData calendarData = null;
            if (!CollectionUtils.isEmpty(interestRecalculationCalendarDatas)) {
                calendarData = interestRecalculationCalendarDatas.iterator().next();
            }
            Collection<CalendarData> interestRecalculationCompoundingCalendarDatas = this.calendarReadPlatformService
                    .retrieveCalendarsByEntity(loanBasicDetails.getInterestRecalculationDetailId(),
                            CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL.getValue(), null);
            CalendarData compoundingCalendarData = null;
            if (!CollectionUtils.isEmpty(interestRecalculationCompoundingCalendarDatas)) {
                compoundingCalendarData = interestRecalculationCompoundingCalendarDatas.iterator().next();
            }
            loanBasicDetails = LoanAccountData.withInterestRecalculationCalendarData(loanBasicDetails, calendarData,
                    compoundingCalendarData);
        }
        PartnerData partnerData = null;
        if(loanId !=null){
            LoanProductData loanProductData = this.loanProductReadPlatformService.retrieveLoanProduct(loanBasicDetails.getLoanProductId());
            partnerData = this.partnerReadPlatformService.retrievePartner(loanProductData.getPartnerId());
        }

        ServicerFeeData servicerFeeData = null;
        if(loan.getLoanProduct().getServicerFeeConfig()!=null) {
            servicerFeeData = this.servicerFeeReadPlatformService.retrieveServicerFeeConfigData(loan.productId());}

        if (loanBasicDetails.isMonthlyRepaymentFrequencyType()) {
            Collection<CalendarData> loanCalendarDatas = this.calendarReadPlatformService.retrieveCalendarsByEntity(loanId,
                    CalendarEntityType.LOANS.getValue(), null);
            CalendarData calendarData = null;
            if (!CollectionUtils.isEmpty(loanCalendarDatas)) {
                calendarData = loanCalendarDatas.iterator().next();
            }
            if (calendarData != null) {
                loanBasicDetails = LoanAccountData.withLoanCalendarData(loanBasicDetails, calendarData);
            }
        }


        final Collection<LoanChargeData>  disbursementChargeData=  loanChargeReadPlatformService.retrieveDisbursementSummaryCharge(loanId);
//        final BigDecimal netDisbursalAmount= loanBasicDetails.getNetDisbursalAmount();
//        final BigDecimal approvedAmount = loanBasicDetails.GetApprovedAmount();

        Collection<DisbursementSummary> disbursementSummary=loanChargeReadPlatformService.retrieveDisburesemntSummary(disbursementChargeData);

        BigDecimal total =BigDecimal.valueOf(0);
        for(DisbursementSummary disbursementSummaries :disbursementSummary ){
            total=total.add(disbursementSummaries.getTotal());
        }

        BankTranscationData bankTranscationData = loanReadPlatformService.retrieveBankTransaction(loanId);

        Collection<VpayTransactionDetailsData> vpayTransactionDetailsData=loanReadPlatformService.retrievePennyDrop(loanId);

        Collection<InterestRatePeriodData> interestRatesPeriods = this.loanReadPlatformService
                .retrieveLoanInterestRatePeriodData(loanBasicDetails);
        Collection<LoanTransactionData> loanRepayments = null;
        LoanScheduleData repaymentSchedule = null;
        Collection<LoanChargeData> charges = null;
        Collection<GuarantorData> guarantors = null;
        Collection<CollateralData> collateral = null;
        CalendarData meeting = null;
        Collection<NoteData> notes = null;
        PortfolioAccountData linkedAccount = null;
        Collection<DisbursementData> disbursementData = null;
        Collection<LoanTermVariationsData> emiAmountVariations = null;
        Collection<LoanCollateralResponseData> loanCollateralManagements = null;
        Collection<LoanCollateralManagementData> loanCollateralManagementData = new ArrayList<>();
        CollectionData collectionData = CollectionData.template();
        List<LoanAccrualData> loanAccruals = null;

        final Set<String> mandatoryResponseParameters = new HashSet<>();
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {

            if (associationParameters.contains(DataTableApiConstant.allAssociateParamName)) {
                associationParameters.addAll(Arrays.asList(DataTableApiConstant.repaymentScheduleAssociateParamName,
                        DataTableApiConstant.futureScheduleAssociateParamName, DataTableApiConstant.originalScheduleAssociateParamName,
                        DataTableApiConstant.transactionsAssociateParamName, DataTableApiConstant.chargesAssociateParamName,
                        DataTableApiConstant.guarantorsAssociateParamName, DataTableApiConstant.collateralAssociateParamName,
                        DataTableApiConstant.notesAssociateParamName, DataTableApiConstant.linkedAccountAssociateParamName,
                        DataTableApiConstant.multiDisburseDetailsAssociateParamName, DataTableApiConstant.collectionAssociateParamName,
                        DataTableApiConstant.monthEndAssociateParamName));
            }

            ApiParameterHelper.excludeAssociationsForResponseIfProvided(uriInfo.getQueryParameters(), associationParameters);

            if (associationParameters.contains(DataTableApiConstant.guarantorsAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.guarantorsAssociateParamName);
                guarantors = this.guarantorReadPlatformService.retrieveGuarantorsForLoan(loanId);
                if (CollectionUtils.isEmpty(guarantors)) {
                    guarantors = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.transactionsAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.transactionsAssociateParamName);
                final Collection<LoanTransactionData> currentLoanRepayments = this.loanReadPlatformService.retrieveLoanTransactions(loanId);
                if (!CollectionUtils.isEmpty(currentLoanRepayments)) {
                    loanRepayments = currentLoanRepayments;
                }
            }
            if (associationParameters.contains(DataTableApiConstant.monthEndAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.monthEndAssociateParamName);
                loanAccruals = this.loanReadPlatformService.retrieveLoanAccruals(loanId).stream()
                        .map(loanAccrual -> {
                            LoanAccrualData loanAccrualData = new LoanAccrualData();
                            loanAccrualData.setLoanId(loanAccrual.getLoanId());
                            loanAccrualData.setAccrualType(loanAccrual.getAccrualType());
                            loanAccrualData.setInstallment(loanAccrual.getInstallment());
                            loanAccrualData.setAccruedAmount(loanAccrual.getAccruedAmount());
                            loanAccrualData.setSelfAccruedAmount(loanAccrual.getSelfAccruedAmount());
                            loanAccrualData.setPartnerAccruedAmount(loanAccrual.getPartnerAccruedAmount());
                            loanAccrualData.setFromDate(loanAccrual.getFromDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            loanAccrualData.setToDate(loanAccrual.getToDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            loanAccrualData.setPostedDate(loanAccrual.getPostedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            loanAccrualData.setReversed(loanAccrual.isReversed());
                            return loanAccrualData;
                        }).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(loanAccruals)) {
                    loanAccruals = null;
                }
            }
            if (associationParameters.contains(DataTableApiConstant.multiDisburseDetailsAssociateParamName)
                    || associationParameters.contains(DataTableApiConstant.repaymentScheduleAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.multiDisburseDetailsAssociateParamName);
                disbursementData = this.loanReadPlatformService.retrieveLoanDisbursementDetails(loanId);
            }

            if (associationParameters.contains(DataTableApiConstant.emiAmountVariationsAssociateParamName)
                    || associationParameters.contains(DataTableApiConstant.repaymentScheduleAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.emiAmountVariationsAssociateParamName);
                emiAmountVariations = this.loanReadPlatformService.retrieveLoanTermVariations(loanId,
                        LoanTermVariationType.EMI_AMOUNT.getValue());
            }

            if (associationParameters.contains(DataTableApiConstant.repaymentScheduleAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.repaymentScheduleAssociateParamName);
                final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData = loanBasicDetails.repaymentScheduleRelatedData();
                repaymentSchedule = this.loanReadPlatformService.retrieveRepaymentSchedule(loanId, repaymentScheduleRelatedData,
                        disbursementData, loanBasicDetails.isInterestRecalculationEnabled(), loanBasicDetails.getTotalPaidFeeCharges());

                if (associationParameters.contains(DataTableApiConstant.futureScheduleAssociateParamName)
                        && loanBasicDetails.isInterestRecalculationEnabled()) {
                    mandatoryResponseParameters.add(DataTableApiConstant.futureScheduleAssociateParamName);
                    this.calculationPlatformService.updateFutureSchedule(repaymentSchedule, loanId);
                }

                if (associationParameters.contains(DataTableApiConstant.originalScheduleAssociateParamName)
                        && loanBasicDetails.isInterestRecalculationEnabled() && loanBasicDetails.isActive()) {
                    mandatoryResponseParameters.add(DataTableApiConstant.originalScheduleAssociateParamName);
                    LoanScheduleData loanScheduleData = this.loanScheduleHistoryReadPlatformService.retrieveRepaymentArchiveSchedule(loanId,
                            repaymentScheduleRelatedData, disbursementData);
                    loanBasicDetails = LoanAccountData.withOriginalSchedule(loanBasicDetails, loanScheduleData);
                }
            }

            if (associationParameters.contains(DataTableApiConstant.chargesAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.chargesAssociateParamName);
                charges = this.loanChargeReadPlatformService.retrieveLoanChargesForSpecificLoan(loanId);
                if (CollectionUtils.isEmpty(charges)) {
                    charges = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.collateralAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.collateralAssociateParamName);
                loanCollateralManagements = this.loanCollateralManagementReadPlatformService.getLoanCollateralResponseDataList(loanId);
                for (LoanCollateralResponseData loanCollateralManagement : loanCollateralManagements) {
                    loanCollateralManagementData.add(loanCollateralManagement.toCommand());
                }
                if (CollectionUtils.isEmpty(loanCollateralManagements)) {
                    loanCollateralManagements = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.meetingAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.meetingAssociateParamName);
                meeting = this.calendarReadPlatformService.retrieveLoanCalendar(loanId);
            }

            if (associationParameters.contains(DataTableApiConstant.notesAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.notesAssociateParamName);
                notes = this.noteReadPlatformService.retrieveNotesByResource(loanId, NoteType.LOAN.getValue());
                if (CollectionUtils.isEmpty(notes)) {
                    notes = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.linkedAccountAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.linkedAccountAssociateParamName);
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            }

            if (associationParameters.contains(DataTableApiConstant.collectionAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.collectionAssociateParamName);
                if (loanBasicDetails.isActive()) {
                    collectionData = this.loanReadPlatformService.retrieveLoanCollectionData(loanId);
                }
            }
        }

        Collection<LoanProductData> productOptions = null;
        LoanProductData product = null;
        Collection<EnumOptionData> loanTermFrequencyTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions = null;
        Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
        Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        Collection<EnumOptionData> amortizationTypeOptions = null;
        Collection<EnumOptionData> interestTypeOptions = null;
        Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        Collection<FundData> fundOptions = null;
        Collection<StaffData> allowedLoanOfficers = null;
        Collection<ChargeData> chargeOptions = null;
        Collection<ColendingChargeData> chargeOption=null;
        Collection<ColendingChargeData> foreClosureCharge=null;
        ChargeData chargeTemplate = null;
        Collection<CodeValueData> loanPurposeOptions = null;
        Collection<CodeValueData> loanCollateralOptions = null;
        Collection<CalendarData> calendarOptions = null;
        Collection<PortfolioAccountData> accountLinkingOptions = null;
        PaidInAdvanceData paidInAdvanceTemplate = null;
        Collection<LoanAccountSummaryData> clientActiveLoanOptions = null;
        Collection<ColendingChargeData> bounceCharge = null;

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
            product = this.loanProductReadPlatformService.retrieveLoanProduct(loanBasicDetails.loanProductId());
            loanBasicDetails.setProduct(product);
            loanTermFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
            repaymentFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
            repaymentFrequencyNthDayTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForNthDayOfMonth();
            repaymentFrequencyDayOfWeekTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForDaysOfWeek();
            interestRateFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();

            amortizationTypeOptions = this.dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
            if (product.isLinkedToFloatingInterestRates()) {
                interestTypeOptions = Arrays.asList(interestType(InterestMethod.DECLINING_BALANCE));
            } else {
                interestTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
            }
            interestCalculationPeriodTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();

            fundOptions = this.fundReadPlatformService.retrieveAllFunds();
            repaymentStrategyOptions = this.dropdownReadPlatformService.retreiveTransactionProcessingStrategies();
            final Long productId=product.getId();



            Collection<LoanProductFeesChargesData> colendingCharge=this.chargeReadPlatformService.retrieveColendingCharge(productId);
            chargeOption =this.loanReadPlatformServiceImp.retrieveColendingCharge(colendingCharge);

            if (product.getMultiDisburseLoan()) {
                chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId,
                        new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT });
            } else {
                chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId,
                        new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT, ChargeTimeType.TRANCHE_DISBURSEMENT });
            }
            chargeTemplate = this.loanChargeReadPlatformService.retrieveLoanChargeTemplate();

            allowedLoanOfficers = this.loanReadPlatformService.retrieveAllowedLoanOfficers(loanBasicDetails.officeId(),
                    staffInSelectedOfficeOnly);

            loanPurposeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanPurpose");
            loanCollateralOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
            final CurrencyData currencyData = loanBasicDetails.currency();
            String currencyCode = null;
            if (currencyData != null) {
                currencyCode = currencyData.code();
            }
            final long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue() };
            PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(),
                    loanBasicDetails.clientId(), currencyCode, accountStatus, DepositAccountType.SAVINGS_DEPOSIT.getValue());
            accountLinkingOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);

            if (!associationParameters.contains(DataTableApiConstant.linkedAccountAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.linkedAccountAssociateParamName);
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            }
            if (loanBasicDetails.groupId() != null) {
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(loanBasicDetails.groupId());
            }

            if (loanBasicDetails.product().canUseForTopup() && loanBasicDetails.clientId() != null) {
                clientActiveLoanOptions = this.accountDetailsReadPlatformService
                        .retrieveClientActiveLoanAccountSummary(loanBasicDetails.clientId());
            }

        }

        LoanProductData loanProductData= loanProductReadPlatformService.retrieveLoanProduct(loanBasicDetails.loanProductId());

        Collection<ChargeData> overdueCharges = this.chargeReadPlatformService.retrieveLoanProductCharges(loanBasicDetails.loanProductId(),
                ChargeTimeType.OVERDUE_INSTALLMENT);
        Collection<LoanProductFeesChargesData> OverDueAndForclosueCharge  = chargeReadPlatformService.retrieveOverdueCharge(loanBasicDetails.loanProductId());

        paidInAdvanceTemplate = this.loanReadPlatformService.retrieveTotalPaidInAdvance(loanId);

        // Get rates from Loan
        boolean isRatesEnabled = this.configurationDomainService.isSubRatesEnabled();
        List<RateData> rates = null;
        if (isRatesEnabled) {
            rates = this.rateReadService.retrieveLoanRates(loanId);
        }

        final LoanAccountData loanAccount = LoanAccountData.associationsAndTemplate(loanBasicDetails, repaymentSchedule, loanRepayments,
                charges, loanCollateralManagementData, guarantors, meeting, productOptions, loanTermFrequencyTypeOptions,
                repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, allowedLoanOfficers, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, notes, accountLinkingOptions, linkedAccount, disbursementData, emiAmountVariations,
                overdueCharges, paidInAdvanceTemplate, interestRatesPeriods, clientActiveLoanOptions, rates, isRatesEnabled,
                collectionData,disbursementSummary,total,loanProductData,foreClosureCharge, loanAccruals ,partnerData,bankTranscationData,OverDueAndForclosueCharge,servicerFeeData, bounceCharge);
                loanAccount.setVclHurdleRate(loanBasicDetails.getVclHurdleRate());

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        return this.toApiJsonSerializer.serialize(settings, loanAccount, this.loanDataParameters);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loans", description = "The list capability of loans can support pagination and sorting.\n"
            + "Example Requests:\n" + "\n" + "loans\n" + "\n" + "loans?fields=accountNo\n" + "\n" + "loans?offset=10&limit=50\n" + "\n"
            + "loans?orderBy=accountNo&sortOrder=DESC")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansResponse.class))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("sqlSearch") @Parameter(description = "sqlSearch") final String sqlSearch,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            // @QueryParam("underHierarchy") final String hierarchy,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final SearchParameters searchParameters = SearchParameters.forLoans(sqlSearch, externalId, offset, limit, orderBy, sortOrder,
                accountNo);

        final Page<LoanAccountData> loanBasicDetails = this.loanReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanBasicDetails, this.loanDataParameters);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Calculate loan repayment schedule | Submit a new Loan Application", description = "It calculates the loan repayment Schedule\n"
            + "Submits a new loan application\n"
            + "Mandatory Fields: clientId, productId, principal, loanTermFrequency, loanTermFrequencyType, loanType, numberOfRepayments, repaymentEvery, repaymentFrequencyType, interestRatePerPeriod, amortizationType, interestType, interestCalculationPeriodType, transactionProcessingStrategyId, expectedDisbursementDate, submittedOnDate,interestChargedFromDate, loanType\n"
            + "Optional Fields: graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, linkAccountId, allowPartialPeriodInterestCalcualtion, fixedEmiAmount, maxOutstandingLoanBalance, disbursementData, graceOnArrearsAgeing, createStandingInstructionAtDisbursement (requires linkedAccountId if set to true)\n"
            + "Additional Mandatory Fields if interest recalculation is enabled for product and Rest frequency not same as repayment period: recalculationRestFrequencyDate\n"
            + "Additional Mandatory Fields if interest recalculation with interest/fee compounding is enabled for product and compounding frequency not same as repayment period: recalculationCompoundingFrequencyDate\n"
            + "Additional Mandatory Field if Entity-Datatable Check is enabled for the entity of type loan: datatables")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansResponse.class))) })
    public String calculateLoanScheduleOrSubmitLoanApplication(
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        if (is(commandParam, "calculateLoanSchedule")) {

            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);

            final LoanScheduleModel loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query, true,null);

            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.loanScheduleToApiJsonSerializer.serialize(settings, loanSchedule.toData(), new HashSet<String>());
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanApplication().withJson(apiRequestBodyAsJson).build();

         final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Modify a loan application", description = "Loan application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdResponse.class))) })
    public String modifyLoanApplication(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanApplication(loanId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Loan Application", description = "Note: Only loans in \"Submitted and awaiting approval\" status can be deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.DeleteLoansLoanIdResponse.class))) })
    public String deleteLoanApplication(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanApplication(loanId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve Loan Application | Recover Loan Guarantee | Undo Loan Application Approval | Assign a Loan Officer | Unassign a Loan Officer | Reject Loan Application | Applicant Withdraws from Loan Application | Disburse Loan Disburse Loan To Savings Account | Undo Loan Disbursal", description = "Approve Loan Application:\n"
            + "Mandatory Fields: approvedOnDate\n" + "Optional Fields: approvedLoanAmount and expectedDisbursementDate\n"
            + "Approves the loan application\n\n" + "Recover Loan Guarantee:\n" + "Recovers the loan guarantee\n\n"
            + "Undo Loan Application Approval:\n" + "Undoes the Loan Application Approval\n\n" + "Assign a Loan Officer:\n"
            + "Allows you to assign Loan Officer for existing Loan.\n\n" + "Unassign a Loan Officer:\n"
            + "Allows you to unassign the Loan Officer.\n\n" + "Reject Loan Application:\n" + "Mandatory Fields: rejectedOnDate\n"
            + "Allows you to reject the loan application\n\n" + "Applicant Withdraws from Loan Application:\n"
            + "Mandatory Fields: withdrawnOnDate\n" + "Allows the applicant to withdraw the loan application\n\n" + "Disburse Loan:\n"
            + "Mandatory Fields: actualDisbursementDate\n" + "Optional Fields: transactionAmount and fixedEmiAmount\n"
            + "Disburses the Loan\n\n" + "Disburse Loan To Savings Account:\n" + "Mandatory Fields: actualDisbursementDate\n"
            + "Optional Fields: transactionAmount and fixedEmiAmount\n" + "Disburses the loan to Saving Account\n\n"
            + "Undo Loan Disbursal:\n" + "Undoes the Loan Disbursal\n" + "Showing request and response for Assign a Loan Officer")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdResponse.class))) })
    public String stateTransitions(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;

        if (is(commandParam, "reject")) {
            final CommandWrapper commandRequest = builder.rejectLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawnByApplicant")) {
            final CommandWrapper commandRequest = builder.withdrawLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            final CommandWrapper commandRequest = builder.approveLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "disburse")) {
            final CommandWrapper commandRequest = builder.disburseLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "disburseToSavings")) {
            final CommandWrapper commandRequest = builder.disburseLoanToSavingsApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoLoanApplicationApproval(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undodisbursal")) {
            final CommandWrapper commandRequest = builder.undoLoanApplicationDisbursal(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undolastdisbursal")) {
            final CommandWrapper commandRequest = builder.undoLastDisbursalLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (is(commandParam, "assignloanofficer")) {
            final CommandWrapper commandRequest = builder.assignLoanOfficer(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "unassignloanofficer")) {
            final CommandWrapper commandRequest = builder.unassignLoanOfficer(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "recoverGuarantees")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().recoverFromGuarantor(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getLoansTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("staffId") final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.LOANS.toString(), officeId, staffId, dateFormat);
    }

    @GET
    @Path("glimAccount/{glimId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getGlimRepaymentTemplate(@PathParam("glimId") final Long glimId, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        Collection<GlimRepaymentTemplate> glimRepaymentTemplate = this.glimAccountInfoReadPlatformService.findglimRepaymentTemplate(glimId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.glimTemplateToApiJsonSerializer.serialize(settings, glimRepaymentTemplate, this.glimAccountsDataParameters);
    }

    @POST
    @Path("glimAccount/{glimId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve GLIM Application | Undo GLIM Application Approval | Reject GLIM Application | Disburse Loan Disburse Loan To Savings Account | Undo Loan Disbursal", description = "Approve GLIM Application:\n"
            + "Mandatory Fields: approvedOnDate\n" + "Optional Fields: approvedLoanAmount and expectedDisbursementDate\n"
            + "Approves the GLIM application\n\n" + "Undo GLIM Application Approval:\n" + "Undoes the GLIM Application Approval\n\n"
            + "Reject GLIM Application:\n" + "Mandatory Fields: rejectedOnDate\n" + "Allows you to reject the GLIM application\n\n"
            + "Disburse Loan:\n" + "Mandatory Fields: actualDisbursementDate\n" + "Optional Fields: transactionAmount and fixedEmiAmount\n"
            + "Disburses the Loan\n\n" + "Disburse Loan To Savings Account:\n" + "Mandatory Fields: actualDisbursementDate\n"
            + "Optional Fields: transactionAmount and fixedEmiAmount\n" + "Disburses the loan to Saving Account\n\n"
            + "Undo Loan Disbursal:\n" + "Undoes the Loan Disbursal\n")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdResponse.class))) })
    public String glimStateTransitions(@PathParam("glimId") final Long glimId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;

        if (is(commandParam, "reject")) {
            final CommandWrapper commandRequest = builder.rejectGLIMApplication(glimId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            final CommandWrapper commandRequest = builder.approveGLIMLoanApplication(glimId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "disburse")) {
            final CommandWrapper commandRequest = builder.disburseGlimLoanApplication(glimId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "glimrepayment")) {
            final CommandWrapper commandRequest = builder.repaymentGlimLoanApplication(glimId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undodisbursal")) {
            final CommandWrapper commandRequest = builder.undoGLIMLoanDisbursal(glimId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoGLIMLoanApproval(glimId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("repayments/downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getLoanRepaymentTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.LOAN_TRANSACTIONS.toString(), officeId, null, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload Loan template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postLoanTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.LOANS.toString(), uploadedInputStream,
                fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }

    @POST
    @Path("repayments/uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload Loan repayments template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postLoanRepaymentTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.LOAN_TRANSACTIONS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }


    @GET
    @Path("partnerandproducttemplate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Details Template using partner and product", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Requests:\n" + "\n"
            + "loans/" +
            "" +
            "template?templateType=individual&clientId=1\n" + "\n" + "\n"
            + "loans/template?templateType=individual&clientId=1&productId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetProductPartnerResponse.class))) })
    public String templates(@QueryParam("partnerId") @Parameter(description = "partnerId") final Long partnerId,
                            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
                            @QueryParam("templateType") @Parameter(description = "templateType") final String templateType,
//                           @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
                            @DefaultValue("false") @QueryParam("activeOnly") @Parameter(description = "activeOnly") final boolean onlyActive,
                            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
//        final boolean onlyManualEntries;

//        final Collection<PartnerData> partneroptions = this.partnerReadPlatformService.retrieveAllPartners(onlyManualEntries, searchParameters);
//        final Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(onlyActive);
        List<PartnerandProductData> partnerandProductData = loanReadPlatformService.findAllProducts();

        final List<EnumOptionData> bulkReportDataEnums = this.dropdownReadPlatformService.bulkReportDataEnums();
        partnerandProductData.stream().forEach(partnerandProductDatas-> partnerandProductDatas.setEnumOptionData(bulkReportDataEnums));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.partnerandProductDataDefaultToApiJsonSerializer.serialize(settings, partnerandProductData, this.loanDataParameters);

    }


    @GET
    @Path("loansFilter")
    @Operation(summary = "List all loans by filter", description = "The list capability of loans can support pagination and sorting.\n"
            + "Example Requests:\n" + "\n" + "loans\n" + "\n" + "loans?fields=accountNo\n" + "\n" + "loans?offset=10&limit=50\n" + "\n"
            + "loans?orderBy=accountNo&sortOrder=DESC")
    @ApiResponses({

            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansFilterResponse.class))) })
    public String retrieveAll(@Context final UriInfo uriInfo,

                              @QueryParam("partnerId") @Parameter(description = "partnerId") final Long partnerId,
                              @QueryParam("productId") @Parameter(description = "productId") final Long productId,
                              @QueryParam("fromDate") @Parameter(description = "fromDate") final String fromDate,
                              @QueryParam("toDate") @Parameter(description = "toDate") final String toDate,
                              @QueryParam("paged") @Parameter(description = "paged") final Boolean paged,
                              @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
                              @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
                              @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
                              @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final PaginationParameters parameters = PaginationParameters.instance(paged, offset, limit, orderBy, sortOrder);
//        final SearchParameters searchParameters = SearchParameters.forLoan(partnerName,productId,fromDate,toDate);
        // final SQLBuilder loans = getAllLoans(partnerId,productId,fromDate,toDate);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());


        final List<LoanFilterData> loanFilterData = this.loanReadPlatformService.retrievePaginatedLoans(partnerId,productId,fromDate,toDate,
                parameters);
        return this.loanFilterDataDefaultToApiJsonSerializer.serialize(settings,loanFilterData,this.loanDataParameters);

    }

//    @GET
//    @Path("bulkReportsFilter")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansResponse.class))) })
//    public String retrieveBulkReports(@Context final UriInfo uriInfo,
//
//                  @QueryParam("fromDate")  final String fromDate,
//                  @QueryParam("toDate")  final String toDate,
//                  @QueryParam("type")  final String type)
//    {
//        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
//        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
//
//        final List<BulkReportsData> bulkReportsData = this.loanReadPlatformService.retriveBulkReportsData(fromDate,toDate,type);
//        return this.bulkReportsFilterDefaultToApiJsonSerializer.serialize(settings,bulkReportsData,this.loanDataParameters);
//
//    }

    @POST
    @Path("initiatepennydrop/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdResponse.class))) })
    public ResponseEntity<String> initiatePennyDrop(@PathParam("loanId") final Long loanId) throws JsonProcessingException {
        try {
            vPayIntegrationService.pennydrop(loanId);
            return ResponseEntity.ok("Initiated PennyDrop Successfully for loan ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @POST
    @Path("initiatedisbursement/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdResponse.class))) })
    public ResponseEntity<String> initiateDisbursement(@PathParam("loanId") final Long loanId) {
        try {
            vPayIntegrationService.disburse(loanId);
            return ResponseEntity.ok("Initiated Disbursement Successfully for loan ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
