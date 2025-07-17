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
package org.vcpl.lms.portfolio.loanproduct.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.RoundingMode;
import java.util.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.vcpl.lms.accounting.common.AccountingDropdownReadPlatformService;
import org.vcpl.lms.accounting.glaccount.data.GLAccountData;
import org.vcpl.lms.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.vcpl.lms.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.vcpl.lms.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformServiceImpl;
import org.vcpl.lms.infrastructure.configuration.domain.ConfigurationDomainService;
import org.vcpl.lms.infrastructure.core.api.ApiParameterHelper;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.service.CurrencyReadPlatformService;
import org.vcpl.lms.organisation.office.data.OfficeData;
import org.vcpl.lms.organisation.office.service.OfficeReadPlatformService;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.charge.service.ChargeReadPlatformService;
import org.vcpl.lms.portfolio.common.service.DropdownReadPlatformService;
import org.vcpl.lms.portfolio.floatingrates.data.FloatingRateData;
import org.vcpl.lms.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.vcpl.lms.portfolio.fund.data.FundData;
import org.vcpl.lms.portfolio.fund.service.FundReadPlatformService;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanproduct.LoanProductConstants;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductData;
import org.vcpl.lms.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.vcpl.lms.portfolio.loanproduct.productmix.data.ProductMixData;
import org.vcpl.lms.portfolio.loanproduct.productmix.service.ProductMixReadPlatformService;
import org.vcpl.lms.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.vcpl.lms.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.vcpl.lms.portfolio.partner.data.PartnerData;
import org.vcpl.lms.portfolio.partner.service.PartnerReadPlatformService;
import org.vcpl.lms.portfolio.paymenttype.data.PaymentTypeData;
import org.vcpl.lms.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.vcpl.lms.portfolio.products.constants.ProductsApiConstants;
import org.vcpl.lms.portfolio.rate.data.RateData;
import org.vcpl.lms.portfolio.rate.service.RateReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loanproducts")
@Component
@Scope("singleton")
@Tag(name = "Loan Products", description = "A Loan product is a template that is used when creating a loan. Much of the template definition can be overridden during loan creation.")
public class LoanProductsApiResource {

    private final Set<String> loanProductDataParameters = new HashSet<>(Arrays.asList("id", "name", "shortName","loanAccNoPreference", "description", "fundId",
            "fundName", "includeInBorrowerCycle", "currency", "principal", "minPrincipal", "maxPrincipal", "numberOfRepayments",
            "minNumberOfRepayments", "maxNumberOfRepayments", "repaymentEvery", "repaymentFrequencyType", "graceOnPrincipalPayment",
            "recurringMoratoriumOnPrincipalPeriods", "graceOnInterestPayment", "graceOnInterestCharged", "interestRatePerPeriod",
            "minInterestRatePerPeriod", "maxInterestRatePerPeriod", "interestRateFrequencyType", "annualInterestRate", "amortizationType",
            "interestType", "interestCalculationPeriodType", LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
            "inArrearsTolerance", "transactionProcessingStrategyId", "transactionProcessingStrategyName", "charges", "accountingRule",
            "externalId", "accountingMappings", "paymentChannelToFundSourceMappings", "fundOptions","paymentTypeOptions",
            "currencyOptions", "repaymentFrequencyTypeOptions", "interestRateFrequencyTypeOptions", "amortizationTypeOptions",
            "interestTypeOptions", "interestCalculationPeriodTypeOptions", "transactionProcessingStrategyOptions", "chargeOptions",
            "accountingOptions", "accountingRuleOptions", "accountingMappingOptions", "floatingRateOptions",
            "isLinkedToFloatingInterestRates", "floatingRatesId", "interestRateDifferential", "minDifferentialLendingRate",
            "defaultDifferentialLendingRate", "maxDifferentialLendingRate", "isFloatingInterestRateCalculationAllowed",
            LoanProductConstants.CAN_USE_FOR_TOPUP, LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, LoanProductConstants.RATES_PARAM_NAME,
            LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, "classId", "typeId", "partnerId", "enableOverdue","isPennyDropEnabled","isBankDisbursementEnabled"));

    private final Set<String> productMixDataParameters = new HashSet<>(
            Arrays.asList("productId", "productName", "restrictedProducts", "allowedProducts", "productOptions"));

    private final String resourceNameForPermissions = "LOANPRODUCT";

    private final PlatformSecurityContext context;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final PartnerReadPlatformService partnerReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanProductData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final DefaultToApiJsonSerializer<ProductMixData> productMixDataApiJsonSerializer;
    private final ProductMixReadPlatformService productMixReadPlatformService;
    private final DropdownReadPlatformService commonDropdownReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final FloatingRatesReadPlatformService floatingRateReadPlatformService;
    private final RateReadService rateReadService;
    private final ConfigurationDomainService configurationDomainService;
    private final CodeValueReadPlatformServiceImpl codeValueReadPlatformService;

    @Autowired
    public LoanProductsApiResource(final PlatformSecurityContext context, final LoanProductReadPlatformService readPlatformService,
                                   final ChargeReadPlatformService chargeReadPlatformService, final CurrencyReadPlatformService currencyReadPlatformService,
                                   final FundReadPlatformService fundReadPlatformService, final LoanDropdownReadPlatformService dropdownReadPlatformService,
                                   final DefaultToApiJsonSerializer<LoanProductData> toApiJsonSerializer,
                                   final ApiRequestParameterHelper apiRequestParameterHelper,
                                   final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                                   final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService,
                                   final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService,
                                   final DefaultToApiJsonSerializer<ProductMixData> productMixDataApiJsonSerializer,
                                   final ProductMixReadPlatformService productMixReadPlatformService,
                                   final DropdownReadPlatformService commonDropdownReadPlatformService,
                                   PaymentTypeReadPlatformService paymentTypeReadPlatformService,
                                   final FloatingRatesReadPlatformService floatingRateReadPlatformService, final RateReadService rateReadService,
                                   final ConfigurationDomainService configurationDomainService, final PartnerReadPlatformService partnerReadPlatformService, final CodeValueReadPlatformServiceImpl codeValueReadPlatformService ) {
        this.context = context;
        this.loanProductReadPlatformService = readPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.accountMappingReadPlatformService = accountMappingReadPlatformService;
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
        this.productMixDataApiJsonSerializer = productMixDataApiJsonSerializer;
        this.productMixReadPlatformService = productMixReadPlatformService;
        this.commonDropdownReadPlatformService = commonDropdownReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
        this.floatingRateReadPlatformService = floatingRateReadPlatformService;
        this.rateReadService = rateReadService;
        this.configurationDomainService = configurationDomainService;
        this.partnerReadPlatformService=partnerReadPlatformService;
        this.codeValueReadPlatformService=codeValueReadPlatformService;

    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Loan Product", description = "Depending of the Accounting Rule (accountingRule) selected, additional fields with details of the appropriate Ledger Account identifiers would need to be passed in.\n"
            + "\n" + "Refer ZEUS Accounting Specs Draft for more details regarding the significance of the selected accounting rule\n\n"
            + "Mandatory Fields: name, shortName,loanAccNoPreference,currencyCode, assetClass, currency,emiRoundingMode,emiDecimalValues,emiInMultiplesOf,interestRoundingMode,interestDecimalValues,principal, numberOfRepayments, repaymentEvery, repaymentFrequencyType, interestRatePerPeriod, interestRateFrequencyType, amortizationType, interestType, interestCalculationPeriodType, daysInYearType,fldgLogic,brokenInterestStrategy,brokenInterestDaysInYears,disbursement,collection,gstLiabilityByVcl,gstLiabliltyByPartner, daysInMonthType\n\n"
            + "Optional Fields: brokenInterestCalculationPeriod,repaymentStrategyForNpaId,loanForeclosureStrategy,brokenInterestDaysInYears,brokenInterestDaysInMonth,brokenInterestStrategy,enableColendingLoan,byPercentageSplit,selfPrincipalShare,selfFeeShare," +
            "selfPenaltyShare,selfOverpaidShares,selfInterestRate,principalShare,feeShare,penaltyShare,overpaidShare," +
            "interestRate,partnerId,enableChargeWiseBifacation,selectCharge,colendingCharge,selfCharge,partnerCharge,selectAcceptedDates" +
            "inArrearsTolerance, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, graceOnArrearsAgeing, charges, paymentChannelToFundSourceMappings, feeToIncomeAccountMappings, penaltyToIncomeAccountMappings, includeInBorrowerCycle, " +
            "useBorrowerCycle,principalVariationsForBorrowerCycle, numberOfRepaymentVariationsForBorrowerCycle, interestRateVariationsForBorrowerCycle, multiDisburseLoan,maxTrancheCount, outstandingLoanBalance,overdueDaysForNPA,holdGuaranteeFunds, principalThresholdForLastInstalment, " +
            "accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount, installmentAmountInMultiplesOf, allowAttributeOverrides, allowPartialPeriodInterestCalcualtion,coBorrower,eodBalance,,securedLoan,nonEquatedInstallment,advanceEMI,termBasedOnLoanCycle,isNetOffApplied,allowApprovalOverAmountApplied,useDaysInMonthForLoanProvisioning," +
            "divideByThirtyForPartialPeriod,applyPrepayingLockingPeriod,prepayLockingPeriod,applyForeclosureLockingPeriod,foreclosureLockingPeriod\n\n"
            + "Additional Mandatory Fields for Cash(2) based accounting: fundSourceAccountId, loanPortfolioAccountId, interestOnLoanAccountId, incomeFromFeeAccountId, incomeFromPenaltyAccountId, writeOffAccountId, transfersInSuspenseAccountId, overpaymentLiabilityAccountId\n\n"
            + "Additional Mandatory Fields for periodic (3) and upfront (4)accrual accounting: fundSourceAccountId, loanPortfolioAccountId, interestOnLoanAccountId, incomeFromFeeAccountId, incomeFromPenaltyAccountId, writeOffAccountId, receivableInterestAccountId, receivableFeeAccountId, receivablePenaltyAccountId, transfersInSuspenseAccountId, overpaymentLiabilityAccountId\n\n"
            + "Additional Mandatory Fields if interest recalculation is enabled(true): interestRecalculationCompoundingMethod, rescheduleStrategyMethod, recalculationRestFrequencyType\n\n"
            + "Additional Optional Fields if interest recalculation is enabled(true): isArrearsBasedOnOriginalSchedule, preClosureInterestCalculationStrategy\n\n"
            + "Additional Optional Fields if interest recalculation is enabled(true) and recalculationRestFrequencyType is not same as repayment period: recalculationRestFrequencyInterval, recalculationRestFrequencyDate\n\n"
            + "Additional Optional Fields if interest recalculation is enabled(true) and interestRecalculationCompoundingMethod is enabled: recalculationCompoundingFrequencyType\n\n"
            + "Additional Optional Fields if interest recalculation is enabled(true) and interestRecalculationCompoundingMethod is enabled and recalculationCompoundingFrequencyType is not same as repayment period: recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyDate\n\n")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PostLoanProductsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PostLoanProductsResponse.class))) })
    public String createLoanProduct(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanProduct().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loan Products", description = "Lists Loan Products\n\n" + "Example Requests:\n" + "\n" + "loanproducts\n"
            + "\n" + "\n" + "loanproducts?fields=name,description,interestRateFrequencyType,amortizationType")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanProductsApiResourceSwagger.GetLoanProductsResponse.class)))) })
    public String retrieveAllLoanProducts(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("productMixes")) {
                this.context.authenticatedUser().validateHasReadPermission("PRODUCTMIX");
                final Collection<ProductMixData> productMixes = this.productMixReadPlatformService.retrieveAllProductMixes();
                return this.productMixDataApiJsonSerializer.serialize(settings, productMixes, this.productMixDataParameters);
            }
        }

        final Collection<LoanProductData> products = this.loanProductReadPlatformService.retrieveAllLoanProducts();

        return this.toApiJsonSerializer.serialize(settings, products, this.loanProductDataParameters);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Product Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "loanproducts/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.GetLoanProductsTemplateResponse.class))) })
    public String retrieveTemplate(@Context final UriInfo uriInfo,
            @QueryParam("isProductMixTemplate") @Parameter(description = "isProductMixTemplate") final boolean isProductMixTemplate) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (isProductMixTemplate) {
            this.context.authenticatedUser().validateHasReadPermission("PRODUCTMIX");

            final Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAvailableLoanProductsForMix();
            final ProductMixData productMixData = ProductMixData.template(productOptions);
            return this.productMixDataApiJsonSerializer.serialize(settings, productMixData, this.productMixDataParameters);
        }

        LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();
        loanProduct = handleTemplate(loanProduct);

        return this.toApiJsonSerializer.serialize(settings, loanProduct, this.loanProductDataParameters);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Product", description = "Retrieves a Loan Product\n\n" + "Example Requests:\n" + "\n"
            + "loanproducts/1\n" + "\n" + "\n" + "loanproducts/1?template=true\n" + "\n" + "\n"
            + "loanproducts/1?fields=name,description,numberOfRepayments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.GetLoanProductsProductIdResponse.class))) })
    public String retrieveLoanProductDetails(@PathParam("productId") @Parameter(description = "productId") final Long productId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);

        Map<String, Object> accountingMappings = null;
        Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;
        Collection<ChargeToGLAccountMapper> feeToGLAccountMappings = null;
        Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings = null;
        if (loanProduct.hasAccountingEnabled()) {
            accountingMappings = this.accountMappingReadPlatformService.fetchAccountMappingDetailsForLoanProduct(productId,
                    loanProduct.accountingRuleType().getId().intValue());
            paymentChannelToFundSourceMappings = this.accountMappingReadPlatformService
                    .fetchPaymentTypeToFundSourceMappingsForLoanProduct(productId);
            feeToGLAccountMappings = this.accountMappingReadPlatformService.fetchFeeToGLAccountMappingsForLoanProduct(productId);
            penaltyToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchPenaltyToIncomeAccountMappingsForLoanProduct(productId);
            loanProduct = LoanProductData.withAccountingDetails(loanProduct, accountingMappings, paymentChannelToFundSourceMappings,
                    feeToGLAccountMappings, penaltyToGLAccountMappings);
        }

        if (settings.isTemplate()) {
            loanProduct = handleTemplate(loanProduct);
        }
        return this.toApiJsonSerializer.serialize(settings, loanProduct, this.loanProductDataParameters);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Loan Product", description = "Updates a Loan Product")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PutLoanProductsProductIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PutLoanProductsProductIdResponse.class))) })
    public String updateLoanProduct(@PathParam("productId") @Parameter(description = "productId") final Long productId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanProduct(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private LoanProductData handleTemplate(final LoanProductData productData) {

        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableFees();
        if (chargeOptions.isEmpty()) {
            chargeOptions = null;
        }

        Collection<ChargeData> feeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges();
        if (feeOptions.isEmpty()) {
            feeOptions = null;
        }

        Collection<ChargeData> penaltyOptions = this.chargeReadPlatformService.retrieveLoanApplicablePenalties();
        if (penaltyOptions.isEmpty()) {
            penaltyOptions = null;
        }

        boolean isRatesEnabled = this.configurationDomainService.isSubRatesEnabled();
        Collection<RateData> rateOptions = this.rateReadService.retrieveLoanApplicableRates();
        if (rateOptions.isEmpty()) {
            rateOptions = null;
        }

        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<EnumOptionData> amortizationTypeOptions = this.dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
        final List<EnumOptionData> interestTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
        final List<EnumOptionData> interestCalculationPeriodTypeOptions = this.dropdownReadPlatformService
                .retrieveLoanInterestRateCalculatedInPeriodOptions();
        final List<EnumOptionData> repaymentFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
        final List<EnumOptionData> interestRateFrequencyTypeOptions = this.dropdownReadPlatformService
                .retrieveInterestRateFrequencyTypeOptions();
        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();

        Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        if (fundOptions.isEmpty()) {
            fundOptions = null;
        }

        Collection<PartnerData> partnerData = this.partnerReadPlatformService.retrieveAllPartnersForDropdown();
        if (partnerData.isEmpty()) {
            partnerData = null;
        }

        final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = this.dropdownReadPlatformService
                .retreiveTransactionProcessingStrategies();

        final Map<String, List<GLAccountData>> accountOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForLoanProducts();

        final List<EnumOptionData> accountingRuleTypeOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountingRuleTypeOptions();

        final List<EnumOptionData> loanCycleValueConditionTypeOptions = this.dropdownReadPlatformService
                .retrieveLoanCycleValueConditionTypeOptions();

        final List<EnumOptionData> daysInMonthTypeOptions = commonDropdownReadPlatformService.retrieveDaysInMonthTypeOptions();
        final List<EnumOptionData> daysInYearTypeOptions = commonDropdownReadPlatformService.retrieveDaysInYearTypeOptions();
        final List<EnumOptionData> interestRecalculationCompoundingTypeOptions = dropdownReadPlatformService
                .retrieveInterestRecalculationCompoundingTypeOptions();
        final List<EnumOptionData> rescheduleStrategyTypeOptions = dropdownReadPlatformService.retrieveRescheduleStrategyTypeOptions();
        final List<EnumOptionData> interestRecalculationFrequencyTypeOptions = dropdownReadPlatformService
                .retrieveInterestRecalculationFrequencyTypeOptions();
        final List<EnumOptionData> interestRecalculationNthDayTypeOptions = dropdownReadPlatformService
                .retrieveInterestRecalculationNthDayTypeOptions();
        final List<EnumOptionData> interestRecalculationDayOfWeekTypeOptions = dropdownReadPlatformService
                .retrieveInterestRecalculationDayOfWeekTypeOptions();
        final List<EnumOptionData> preCloseInterestCalculationStrategyOptions = dropdownReadPlatformService
                .retrivePreCloseInterestCalculationStrategyOptions();
        final List<FloatingRateData> floatingRateOptions = this.floatingRateReadPlatformService.retrieveLookupActive();

        final List<EnumOptionData> brokenStrategy = dropdownReadPlatformService.retriveBrokenStrategy();
        final List<EnumOptionData> disbursementOptions = dropdownReadPlatformService.retriveDisbursementMode();
        final List<EnumOptionData> collectionOptions = dropdownReadPlatformService.retriveCollection();
        final List<EnumOptionData> brokenStrategyDaysInYear=dropdownReadPlatformService.retriveBrokenStrategyDaysInYear();
        final List<EnumOptionData> brokenStrategyDaysInMonth =dropdownReadPlatformService.retrieveBrokenStrategyDaysInMonth();
        final List<EnumOptionData> transactionTypeOptions = dropdownReadPlatformService.retrieveTransactionTypePreference();
        final List<EnumOptionData> emiMultiplesOfOption  = dropdownReadPlatformService.retrieveMultiples();
        final List<CodeValueData> frameWorkOptions =new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.FRAMEWORK));
        final List<CodeValueData> insuranceApplicabilityOptions =new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.INSURANCEAPPLICABILITY));
        final List<CodeValueData> loanTypeOptions =new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.LOANTYPE));
        final List<CodeValueData> fldgLogicOptions =new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.FLDGLOGIC));
//        final List<CodeValueData> disbursementOptions =new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.DISBURSEMENTMODE));
//        final List<CodeValueData> collectionOptions =new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.COLLECTION));
        final List<CodeValueData> assetClassOptions =new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.ASSETCLASS));
            final List<CodeValueData> loanProductClassOptions =new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.LOANPRODUCTCLASS));
        final List<CodeValueData> loanProductTypeOptions =new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.LOANPRODUCTTYPE));
        final List<CodeValueData>penalInvoiceOptions =  new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.PENALINVOICE));
        final List<CodeValueData> multipleDisbursementOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.MULTIPLEDISBURSEMENT));
        final List<CodeValueData>trancheClubbingOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.TRANCHECLUBBING));
        final List<CodeValueData> repaymentScheduleUpdateAllowedOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.REPAYMENTSCHEDULEUPDATEALLOWED));
        final List<RoundingMode> roundingModes = dropdownReadPlatformService.retrieveRoundingMode();
        final List<CodeValueData> disbursementBankAccNameOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductsApiConstants.DISBURSEMENT_BANK_ACCOUNT_NAME));
        final List<EnumOptionData> advanceAppropriations = dropdownReadPlatformService.retrieveAdvanceAppropriation();
        final List<EnumOptionData> foreclosurePosCalculation = dropdownReadPlatformService.retrieveForeclosurePosCalculation();
        final List<EnumOptionData> servicerFeeChargesRatioOptions =  dropdownReadPlatformService.retrieveServicerFeeChargesRatio();
		final List<EnumOptionData> advanceAppropriationAgainstOn = dropdownReadPlatformService.retrieveAdvanceAppropriationAgainstOn();
        final List<EnumOptionData> emiCalcusEnum = dropdownReadPlatformService.retrieveEmiCalcusEnum();
        final List<EnumOptionData> foreclosureMethodTypes = dropdownReadPlatformService.retrieveForeclosureMethodTypes();
		final List<EnumOptionData> coolingOffInterestAndChargeApplicability =  dropdownReadPlatformService.retrieveCoolingOffInterestAndChargeApplicability();
        final List<EnumOptionData> coolingOffInterestLogicApplicability =  dropdownReadPlatformService.retrieveCoolingOffInterestLogicApplicability();
        return new LoanProductData(productData, chargeOptions, penaltyOptions, paymentTypeOptions, currencyOptions, amortizationTypeOptions,
                interestTypeOptions, interestCalculationPeriodTypeOptions, repaymentFrequencyTypeOptions, interestRateFrequencyTypeOptions,
                fundOptions, transactionProcessingStrategyOptions, rateOptions, accountOptions, accountingRuleTypeOptions,
                loanCycleValueConditionTypeOptions, daysInMonthTypeOptions, daysInYearTypeOptions,
                interestRecalculationCompoundingTypeOptions, rescheduleStrategyTypeOptions, interestRecalculationFrequencyTypeOptions,
                preCloseInterestCalculationStrategyOptions, floatingRateOptions, interestRecalculationNthDayTypeOptions,
                interestRecalculationDayOfWeekTypeOptions, isRatesEnabled, partnerData,frameWorkOptions,insuranceApplicabilityOptions,loanTypeOptions,fldgLogicOptions,
                assetClassOptions,loanProductClassOptions,loanProductTypeOptions,feeOptions
                ,brokenStrategy,disbursementOptions,collectionOptions,brokenStrategyDaysInYear,brokenStrategyDaysInMonth,penalInvoiceOptions,multipleDisbursementOptions,
                trancheClubbingOptions,repaymentScheduleUpdateAllowedOptions,transactionTypeOptions,roundingModes,disbursementBankAccNameOptions,emiMultiplesOfOption,advanceAppropriations,
                foreclosurePosCalculation,servicerFeeChargesRatioOptions,advanceAppropriationAgainstOn,emiCalcusEnum,foreclosureMethodTypes,coolingOffInterestAndChargeApplicability,coolingOffInterestLogicApplicability);
    }

}
