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
package org.vcpl.lms.portfolio.loanproduct.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vcpl.lms.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.exception.PlatformDataIntegrityException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.vcpl.lms.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.service.CurrencyReadPlatformService;
import org.vcpl.lms.organisation.office.exception.OfficeNotFoundException;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants.BusinessEntity;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants.BusinessEvents;
import org.vcpl.lms.portfolio.common.service.BusinessEventNotifierService;
import org.vcpl.lms.portfolio.floatingrates.domain.FloatingRate;
import org.vcpl.lms.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.vcpl.lms.portfolio.fund.domain.Fund;
import org.vcpl.lms.portfolio.fund.domain.FundRepository;
import org.vcpl.lms.portfolio.fund.exception.FundNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanAccountNumberSequenceRepository;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanAccountNumberSequence;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.vcpl.lms.portfolio.loanproduct.LoanProductConstants;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductFeeData;
import org.vcpl.lms.portfolio.loanproduct.domain.*;
import org.vcpl.lms.portfolio.loanproduct.exception.*;
import org.vcpl.lms.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.vcpl.lms.portfolio.partner.domain.Partner;
import org.vcpl.lms.portfolio.partner.domain.PartnerRepository;
import org.vcpl.lms.portfolio.partner.exception.PartnerNotFoundException;
import org.vcpl.lms.portfolio.rate.domain.Rate;
import org.vcpl.lms.portfolio.rate.domain.RateRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.useradministration.domain.AppUser;

@Service
public class LoanProductWritePlatformServiceJpaRepositoryImpl implements LoanProductWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanProductWritePlatformServiceJpaRepositoryImpl.class);
    private static final String OVERDUECHARGE_OBJECT_KEY = "overDueCharge";
    private static final String SELFCHARGE_OBJECT_KEY = "selfOverDue";
    private static final String PARTNERCHARGE_OBJECT_KEY = "partnerOverDue";
    private final PlatformSecurityContext context;
    private final LoanProductDataValidator fromApiJsonDeserializer;
    private final LoanProductRepository loanProductRepository;
    private final AprCalculator aprCalculator;
    private final FundRepository fundRepository;
    private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
    private final ChargeRepositoryWrapper chargeRepository;
    private final RateRepositoryWrapper rateRepository;
    private final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final FloatingRateRepositoryWrapper floatingRateRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final PartnerRepository partnerRepository;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final FromJsonHelper fromApiJsonHelper;
    private final LoanProductFeesChargesRepository loanProductFeesChargesRepository;
    private final CurrencyReadPlatformService currencyReadPlatformService;


    private final LoanAccountNumberSequenceRepository loanAccountNumberSequenceRepository;

    @Autowired
    public LoanProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
                                                            final LoanProductDataValidator fromApiJsonDeserializer, final LoanProductRepository loanProductRepository,
                                                            final AprCalculator aprCalculator, final FundRepository fundRepository,
                                                            final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
                                                            final ChargeRepositoryWrapper chargeRepository, final RateRepositoryWrapper rateRepository,
                                                            final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService,
                                                            final FineractEntityAccessUtil fineractEntityAccessUtil, final FloatingRateRepositoryWrapper floatingRateRepository,
                                                            final LoanRepositoryWrapper loanRepositoryWrapper, final BusinessEventNotifierService businessEventNotifierService,
                                                            final PartnerRepository partnerRepository, final CodeValueRepositoryWrapper codeValueRepositoryWrapper, final FromJsonHelper fromApiJsonHelper, final LoanProductFeesChargesRepository loanProductFeesChargesRepository, CurrencyReadPlatformService currencyReadPlatformService,
                                                            final LoanAccountNumberSequenceRepository loanAccountNumberSequenceRepository) {

        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanProductRepository = loanProductRepository;
        this.aprCalculator = aprCalculator;
        this.fundRepository = fundRepository;
        this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
        this.chargeRepository = chargeRepository;
        this.rateRepository = rateRepository;
        this.accountMappingWritePlatformService = accountMappingWritePlatformService;
        this.fineractEntityAccessUtil = fineractEntityAccessUtil;
        this.floatingRateRepository = floatingRateRepository;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.businessEventNotifierService = businessEventNotifierService;
        this.partnerRepository = partnerRepository;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
        this.fromApiJsonHelper=fromApiJsonHelper;
        this.loanProductFeesChargesRepository =loanProductFeesChargesRepository;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.loanAccountNumberSequenceRepository = loanAccountNumberSequenceRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createLoanProduct(final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            AppUser currentUser = null;
            final SecurityContext context = SecurityContextHolder.getContext();
            final Authentication auth = context.getAuthentication();
            if (auth != null) {
                currentUser = (AppUser) auth.getPrincipal();
            }
            List<LoanProductFeeData> loanProductChargeData=assembleListOfColendingCharge(command);

            this.fromApiJsonDeserializer.validateForCreate(command.json(),loanProductChargeData);
            validateInputDates(command);

            final Fund fund = findFundByIdIfProvided(command.longValueOfParameterNamed("fundId"));

            final Partner partner = findPartnerByIdIfProvided(command.longValueOfParameterNamed("partnerId"));

            final Long transactionProcessingStrategyId = command.longValueOfParameterNamed("transactionProcessingStrategyId");
            final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(
                    transactionProcessingStrategyId);

            final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
            checkNewLoanProductCurrencyCodeExists(currencyCode);

            final List<Charge> charges = assembleListOfProductCharges(command, currencyCode);
            final List<Rate> rates = assembleListOfProductRates(command);

            final String name = command.stringValueOfParameterNamed(LoanProductConstants.LOAN_PRODUCT_NAME);
            final String shortName = command.stringValueOfParameterNamed(LoanProductConstants.SHORT_NAME);
            final String loanAccNoPreference = command.stringValueOfParameterNamed(LoanProductConstants.LOAN_ACC_NO_PREFERENCE);
            checkNewLoanProductIsUnique(name, shortName, loanAccNoPreference);

            FloatingRate floatingRate = null;
            if (command.parameterExists("floatingRatesId")) {
                floatingRate = this.floatingRateRepository
                        .findOneWithNotFoundDetection(command.longValueOfParameterNamed("floatingRatesId"));
            }
            CodeValue assetClass = null;
            final Long assetClassId = command.longValueOfParameterNamed(LoanProductConstants.ASSETCLASS);
            if (assetClassId != null) {
                assetClass = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.ASSETCLASS,assetClassId);
            }

            CodeValue insuranceApplicability = null;
            final Long insuranceApplicabilityId = command.longValueOfParameterNamed(LoanProductConstants.insuranceApplicability);
            if (insuranceApplicabilityId != null) {
                insuranceApplicability = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.INSURANCEAPPLICABILITY,insuranceApplicabilityId);
            }

            CodeValue fldgLogic = null;
            final Long fldgLogicId = command.longValueOfParameterNamed(LoanProductConstants.FLDGLOGIC);
            if (fldgLogicId != null) {
                fldgLogic = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.FLDGLOGIC,fldgLogicId);
            }

            CodeValue penalInvoice = null;
            final Long penalInvoiceId = command.longValueOfParameterNamed(LoanProductConstants.PENALINVOICE);
            if (penalInvoiceId != null) {
                penalInvoice = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.PENALINVOICE,penalInvoiceId);
            }

            CodeValue multipleDisbursement = null;
            final Long multipleDisbursementId = command.longValueOfParameterNamed(LoanProductConstants.MULTIPLEDISBURSEMENT);
            if (multipleDisbursementId != null) {
                multipleDisbursement = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.MULTIPLEDISBURSEMENT,multipleDisbursementId);
            }

            CodeValue trancheClubbing = null;
            final Long trancheClubbingId = command.longValueOfParameterNamed(LoanProductConstants.TRANCHECLUBBING);
            if (trancheClubbingId != null) {
                trancheClubbing = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.TRANCHECLUBBING,trancheClubbingId);
            }

            CodeValue repaymentScheduleUpdateAllowed = null;
            final Long repaymentScheduleUpdateAllowedId = command.longValueOfParameterNamed(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED);
            if (repaymentScheduleUpdateAllowedId != null) {
                repaymentScheduleUpdateAllowed = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED,repaymentScheduleUpdateAllowedId);
            }



//            CodeValue disbursementMode = null;
//            final Long disbursementId = command.longValueOfParameterNamed(LoanProductConstants.DISBURSEMENT);
//            if (disbursementId != null) {
//                disbursementMode = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.DISBURSEMENTMODE,disbursementId);
//            }

//            CodeValue collection = null;
//            final Long collectionId = command.longValueOfParameterNamed(LoanProductConstants.COLLECTION);
//            if (collectionId != null) {
//                collection = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.COLLECTION,collectionId);
//            }

            CodeValue frameWork = null;
            final Long frameWorkId = command.longValueOfParameterNamed(LoanProductConstants.FRAMEWORK);
            if (frameWorkId != null) {
                frameWork = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.FRAMEWORK,frameWorkId);
            }

            CodeValue loanType = null;
            final Long loanTypeId = command.longValueOfParameterNamed(LoanProductConstants.LOANTYPE);
            if (loanTypeId != null) {
                loanType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.LOANTYPE,loanTypeId);
            }

            CodeValue loanProductClass = null;
            final Long loanProductClassId = command.longValueOfParameterNamed(LoanProductConstants.LOANPRODUCTCLASS);
            if (loanProductClassId != null) {
                loanProductClass = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.LOANPRODUCTCLASS,loanProductClassId);
            }

            CodeValue loanProductType = null;
            final Long loanProductTypeId = command.longValueOfParameterNamed(LoanProductConstants.LOANPRODUCTTYPE);
            if (loanProductTypeId != null) {
                loanProductType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.LOANPRODUCTTYPE,loanProductTypeId);
            }

            CodeValue disbursementAccount = null;
            final Long disbursementAccountNameId = command.longValueOfParameterNamed("disbursementBankAccountName");
            if (disbursementAccountNameId != null) {
                disbursementAccount = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.DISBURSEMENT_BANK_ACCOUNT_NAME,disbursementAccountNameId);
            }


            final LoanProduct loanproduct = LoanProduct.assembleFromJson(fund, loanTransactionProcessingStrategy, charges, command,
                    this.aprCalculator, floatingRate, rates, partner,assetClass,frameWork,loanType,
                    insuranceApplicability,fldgLogic,penalInvoice,multipleDisbursement,trancheClubbing,repaymentScheduleUpdateAllowed,loanProductClass,loanProductType,
                    loanProductChargeData,disbursementAccount);
            loanproduct.updateLoanProductInRelatedClasses();
            loanproduct.setCreatedDate(DateUtils.getDateOfTenant());
            loanproduct.setCreatedUser(currentUser);

            this.loanProductRepository.saveAndFlush(loanproduct);

            LoanAccountNumberSequence loanAccountNumberSequence = new LoanAccountNumberSequence(loanproduct.getPartner().getId(),loanproduct.getId(),loanproduct.getLoanAccNoPreference(),loanproduct.getShortName(),0);
            this.loanAccountNumberSequenceRepository.saveAndFlush(loanAccountNumberSequence);
            // save accounting mappings
            this.accountMappingWritePlatformService.createLoanProductToGLAccountMapping(loanproduct.getId(), command);
            // check if the office specific products are enabled. If yes, then
            // save this savings product against a specific office
            // i.e. this savings product is specific for this office.
            fineractEntityAccessUtil.checkConfigurationAndAddProductResrictionsForUserOffice(
                    FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS, loanproduct.getId());

            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_PRODUCT_CREATE,
                    constructEntityMap(BusinessEntity.LOAN_PRODUCT, loanproduct));

            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_PRODUCT_CREATE,
                    constructEntityMap(BusinessEntity.LOAN_PRODUCT, loanproduct));

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanproduct.getId()) //
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }

    }

    private LoanTransactionProcessingStrategy findStrategyByIdIfProvided(final Long transactionProcessingStrategyId) {
        LoanTransactionProcessingStrategy strategy = null;
        if (transactionProcessingStrategyId != null) {
            return this.loanTransactionProcessingStrategyRepository.findById(transactionProcessingStrategyId)
                    .orElseThrow(() -> new LoanTransactionProcessingStrategyNotFoundException(transactionProcessingStrategyId));
        }
        return strategy;
    }

    private Fund findFundByIdIfProvided(final Long fundId) {
        Fund fund = null;
        if (fundId != null) {
            fund = this.fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId));
        }
        return fund;
    }

    private Partner findPartnerByIdIfProvided(final Long partnerId) {
        Partner partner = null;
        if (partnerId != null) {
            partner = this.partnerRepository.findById(partnerId).orElseThrow(() -> new PartnerNotFoundException(partnerId));
        }
        return partner;
    }

    /**
     * <p>
     *     This method checks the uniqueness of parameters namely, name, short name, loan account no preference of new loan product.
     *     If already exists, it will throws PlatformApiDataValidationException.
     * </p>
     * @param name
     * @param shortName
     * @param loanAccNoPreference
     * @throws PlatformApiDataValidationException
     */
    private void checkNewLoanProductIsUnique(String name, String shortName, String loanAccNoPreference) throws PlatformApiDataValidationException{

        List<ApiParameterError> parameterErrors =
                loanProductRepository.findByNameOrShortNameOrLoanAccNoPreference(name, shortName,loanAccNoPreference)
                        .map(loanProducts -> loanProducts.stream()
                                .flatMap(loanProduct -> Stream.of(
                                        checkForErrors(loanProduct.getName(), "name", name),
                                        checkForErrors(loanProduct.getShortName(), LoanProductConstants.SHORT_NAME, shortName),
                                        checkForErrors(loanProduct.getLoanAccNoPreference(), LoanProductConstants.LOAN_ACC_NO_PREFERENCE, loanAccNoPreference)
                                ))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());

        if(!parameterErrors.isEmpty()){
            throw new PlatformApiDataValidationException(parameterErrors);
        }
    }

    private ApiParameterError checkForErrors(String value, String fieldName, String newValue){
        if(Objects.equals(value, newValue)){
            return ApiParameterError.parameterErrorWithValue(
                    "error.msg.loan.product.duplicate." + fieldName,
                    "Loan product with " + (fieldName.equals("name") ? "name" : fieldName) + " `" + newValue + "` already exists",
                    (fieldName.equals("name") ? "name" : fieldName),
                    newValue, newValue);
        }
        return null;
    }
    /**
     * <p>
     *     This method checks the currency code of new loan product from platform allowed currencies.
     *     If it not exists, it will throws PlatformApiDataValidationException.
     * </p>
     * @param currencyCode
     * @throws PlatformApiDataValidationException
     */
    private void checkNewLoanProductCurrencyCodeExists(String currencyCode) throws PlatformApiDataValidationException{

        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final boolean isInvalidCurrencyCode = currencyOptions.stream().noneMatch(currencyData -> currencyData.getCode().equals(currencyCode));

        if(isInvalidCurrencyCode){
            throw new PlatformApiDataValidationException(Collections.singletonList(
                    ApiParameterError.parameterErrorWithValue("error.msg.product.loan.currencyCode",
                            "Loan product with currency code value `" + currencyCode + "` does not exists",
                            "currencyCode" , currencyCode, currencyCode )));

        }

    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanProduct(final Long loanProductId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            AppUser currentUser = null;
            final SecurityContext context = SecurityContextHolder.getContext();
            final Authentication auth = context.getAuthentication();
            if (auth != null) {
                currentUser = (AppUser) auth.getPrincipal();
            }

            final LoanProduct product = this.loanProductRepository.findById(loanProductId)
                    .orElseThrow(() -> new LoanProductNotFoundException(loanProductId));
            final String json =command.json();

            final JsonElement element = this.fromApiJsonHelper.parse(json);
            this.fromApiJsonHelper.parameterExists("partnerId", element);
            final Long partner_id= this.fromApiJsonHelper.extractLongNamed("partnerId", element);
            final Partner partner = this.partnerRepository.findById(partner_id)
                    .orElseThrow(() -> new OfficeNotFoundException(partner_id));

            List<LoanProductFeeData> loanProductChargeData = assembleListOfColendingChargeForUpdate(command,product);

            this.fromApiJsonDeserializer.validateForUpdate(command.json(), product,partner,loanProductChargeData);
            validateInputDates(command);

            if (anyChangeInCriticalFloatingRateLinkedParams(command, product)
                    && this.loanRepositoryWrapper.doNonClosedLoanAccountsExistForProduct(product.getId())) {
                throw new LoanProductCannotBeModifiedDueToNonClosedLoansException(product.getId());
            }

            FloatingRate floatingRate = null;
            if (command.parameterExists("floatingRatesId")) {
                floatingRate = this.floatingRateRepository
                        .findOneWithNotFoundDetection(command.longValueOfParameterNamed("floatingRatesId"));
            }

            final Map<String, Object> changes = product.update(command, this.aprCalculator, floatingRate,loanProductChargeData);


            if (changes.containsKey("fundId")) {
                final Long fundId = (Long) changes.get("fundId");
                final Fund fund = findFundByIdIfProvided(fundId);
                product.update(fund);
            }

            if (changes.containsKey(LoanProductConstants.ASSETCLASS)) {
                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.ASSETCLASS);
                CodeValue assetClass = null;
                if (newValue != null) {
                    assetClass = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.ASSETCLASS, newValue);
                }
                product.updateAssetClass(assetClass);
            }

            if (changes.containsKey(LoanProductConstants.LOANPRODUCTCLASS)) {
                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.LOANPRODUCTCLASS);
                CodeValue loanProductClass = null;
                if (newValue != null) {
                    loanProductClass = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.LOANPRODUCTCLASS, newValue);
                }
                product.updateLoanProductClass(loanProductClass);
            }

            if (changes.containsKey(LoanProductConstants.LOANPRODUCTTYPE)) {

                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.LOANPRODUCTTYPE);
                CodeValue loanProductType = null;
                if (newValue != null) {
                    loanProductType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.LOANPRODUCTTYPE, newValue);
                }
                product.updateLoanProductType(loanProductType);
            }

            if (changes.containsKey(LoanProductConstants.INSURANCEAPPLICABILITY)) {

                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.INSURANCEAPPLICABILITY);
                CodeValue insuranceApplicability = null;
                if (newValue != null) {
                    insuranceApplicability = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.INSURANCEAPPLICABILITY, newValue);
                }
                product.updateInsuranceApplicability(insuranceApplicability);
            }

            if (changes.containsKey(LoanProductConstants.FLDGLOGIC)) {

                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.FLDGLOGIC);
                CodeValue fldgLogic = null;
                if (newValue != null) {
                    fldgLogic = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.FLDGLOGIC, newValue);
                }
                product.updateFldgLogic(fldgLogic);
            }

            if (changes.containsKey(LoanProductConstants.PENALINVOICE)) {

                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.PENALINVOICE);
                CodeValue penalInvoice = null;
                if (newValue != null) {
                    penalInvoice = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.PENALINVOICE, newValue);
                }
                product.updatePenalInvoice(penalInvoice);
            }

            if (changes.containsKey(LoanProductConstants.MULTIPLEDISBURSEMENT)) {

                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.MULTIPLEDISBURSEMENT);
                CodeValue multipleDisbursement = null;
                if (newValue != null) {
                    multipleDisbursement = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.MULTIPLEDISBURSEMENT, newValue);
                }
                product.updateMultipleDisbursement(multipleDisbursement);
            }

            if (changes.containsKey(LoanProductConstants.TRANCHECLUBBING)) {

                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.TRANCHECLUBBING);
                CodeValue trancheClubbing = null;
                if (newValue != null) {
                    trancheClubbing = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.TRANCHECLUBBING, newValue);
                }
                product.updateTrancheClubbing(trancheClubbing);
            }

            if (changes.containsKey(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED)) {

                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED);
                CodeValue repaymentScheduleUpdateAllowed = null;
                if (newValue != null) {
                    repaymentScheduleUpdateAllowed = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED, newValue);
                }
                product.updateRepaymentScheduleUpdateAllowed(repaymentScheduleUpdateAllowed);
            }
            //            if (changes.containsKey(LoanProductConstants.DISBURSEMENT)) {
//
//                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.DISBURSEMENT);
//                CodeValue disbursement = null;
//                if (newValue != null) {
//                    disbursement = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.DISBURSEMENTMODE, newValue);
//                }
//                product.updateDisbursement(disbursement);
//            }
//            if (changes.containsKey(LoanProductConstants.COLLECTION)) {
//
//                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.COLLECTION);
//                CodeValue collection = null;
//                if (newValue != null) {
//                    collection = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.COLLECTION, newValue);
//                }
//                product.updateCollection(collection);
//            }

            if (changes.containsKey(LoanProductConstants.FRAMEWORK)) {

                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.FRAMEWORK);
                CodeValue frameWork = null;
                if (newValue != null) {
                    frameWork = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.FRAMEWORK, newValue);
                }
                product.updateFrameWork(frameWork);
            }

            if (changes.containsKey(LoanProductConstants.LOANTYPE)) {

                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.LOANTYPE);
                CodeValue loanType = null;
                if (newValue != null) {
                    loanType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.LOANTYPE, newValue);
                }
                product.updateLoanType(loanType);
            }


//            if (changes.containsKey("classId")) {
//                final Long classId = (Long) changes.get("classId");
//                final LoanProductClass loanProductClass = findClassByIdIfProvided(classId);
//                product.update(loanProductClass);
//            }
//
//            if (changes.containsKey("typeId")) {
//                final Long typeId = (Long) changes.get("typeId");
//                final LoanProductType loanProductType = findTypeByIdIfProvided(typeId);
//                product.update(loanProductType);
//            }

            if (changes.containsKey("partnerId")) {
                final Long partnerId = (Long) changes.get("partnerId");
                final Partner partnerUpdate = findPartnerByIdIfProvided(partnerId);
                product.update(partnerUpdate);
            }

            if (changes.containsKey("transactionProcessingStrategyId")) {
                final Long transactionProcessingStrategyId = (Long) changes.get("transactionProcessingStrategyId");
                final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(
                        transactionProcessingStrategyId);
                product.update(loanTransactionProcessingStrategy);
            }

            if (changes.containsKey("charges")) {
                final List<Charge> productCharges = assembleListOfProductCharges(command, product.getCurrency().getCode());
                final boolean updated = product.update(productCharges);
                if (!updated) {
                    changes.remove("charges");
                }
            }

            if (changes.containsKey(LoanProductConstants.DISBURSEMENT_BANK_ACC_NAME)) {
                final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.DISBURSEMENT_BANK_ACC_NAME);
                CodeValue disbursementBankAccount = null;
                if (newValue != null && newValue != 0) {
                    disbursementBankAccount = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.DISBURSEMENT_BANK_ACCOUNT_NAME, newValue);
                }
                product.updateDisbursementBankAccount(disbursementBankAccount);
            }

            ProductCollectionConfig productCollectionConfig =  Objects.nonNull(product.getProductCollectionConfig()) ? product.getProductCollectionConfig() : ProductCollectionConfig.getInstance();
            if(Objects.isNull(product.getProductCollectionConfig())){
                productCollectionConfig.setLoanProduct(product);
                product.setProductCollectionConfig(productCollectionConfig);
            }

            if (command.parameterExists(LoanProductConstants.ADVANCEAPPROPRIATION)) {
                final Integer advanceAppropriation = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.ADVANCEAPPROPRIATION, element, Locale.getDefault());
                productCollectionConfig.setAdvanceAppropriationOn(advanceAppropriation);
            }

            if (command.parameterExists(LoanProductConstants.ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION)) {
                final Boolean enableEntryForAdvanceTransaction = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_ENTRY_FOR_ADVANCE_TRANSACTION, element);
                productCollectionConfig.setAdvanceEntryEnabled(enableEntryForAdvanceTransaction);
            }

            if (command.parameterExists(LoanProductConstants.INTEREST_BENEFIT_ENABLED)) {
                final Boolean interestBenefitEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.INTEREST_BENEFIT_ENABLED, element);
                productCollectionConfig.setInterestBenefitEnabled(interestBenefitEnabled);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST)) {
                final Integer foreclosureOnDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureOnDueDateInterest(foreclosureOnDueDateInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE)) {
                final Integer foreclosureOnDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ON_DUE_DATE_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureOnDueDateCharge(foreclosureOnDueDateCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST)) {
                final Integer foreclosureOtherThanDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureOtherThanDueDateInterest(foreclosureOtherThanDueDateInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE)) {
                final Integer foreclosureOtherThanDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_OTHER_THAN_DUE_DATE_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureOtherThanDueDateCharge(foreclosureOtherThanDueDateCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST)) {
                final Integer foreclosureOneMonthOverdueInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureOneMonthOverdueInterest(foreclosureOneMonthOverdueInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE)) {
                final Integer foreclosureOneMonthOverdueCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ONE_MONTH_OVERDUE_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureOneMonthOverdueCharge(foreclosureOneMonthOverdueCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST)) {
                final Integer foreclosureShortPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureShortPaidInterest(foreclosureShortPaidInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE)) {
                final Integer foreclosureShortPaidInterestCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_SHORT_PAID_INTEREST_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureShortPaidInterestCharge(foreclosureShortPaidInterestCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST)) {
                final Integer foreclosurePrincipalShortPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosurePrincipalShortPaidInterest(foreclosurePrincipalShortPaidInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE)) {
                final Integer foreclosurePrincipalShortPaidCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_PRINCIPAL_SHORT_PAID_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosurePrincipalShortPaidCharge(foreclosurePrincipalShortPaidCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST)) {
                final Integer foreclosureTwoMonthsOverdueInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureTwoMonthsOverdueInterest(foreclosureTwoMonthsOverdueInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE)) {
                final Integer foreclosureTwoMonthsOverdueCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_TWO_MONTHS_OVERDUE_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureTwoMonthsOverdueCharge(foreclosureTwoMonthsOverdueCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE)) {
                final Integer foreclosurePosAdvanceOnDueDate = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_POS_ADVANCE_ON_DUE_DATE, element, Locale.getDefault());
                productCollectionConfig.setForeclosurePosAdvanceOnDueDate(foreclosurePosAdvanceOnDueDate);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST)) {
                final Integer foreclosureAdvanceOnDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureAdvanceOnDueDateInterest(foreclosureAdvanceOnDueDateInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE)) {
                final Integer foreclosureAdvanceOnDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_ON_DUE_DATE_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureAdvanceOnDueDateCharge(foreclosureAdvanceOnDueDateCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE)) {
                final Integer foreclosurePosAdvanceOtherThanDueDate = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_POS_ADVANCE_OTHER_THAN_DUE_DATE, element, Locale.getDefault());
                productCollectionConfig.setForeclosurePosAdvanceOtherThanDueDate(foreclosurePosAdvanceOtherThanDueDate);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST)) {
                final Integer foreclosureAdvanceAfterDueDateInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureAdvanceAfterDueDateInterest(foreclosureAdvanceAfterDueDateInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE)) {
                final Integer foreclosureAdvanceAfterDueDateCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_ADVANCE_AFTER_DUE_DATE_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureAdvanceAfterDueDateCharge(foreclosureAdvanceAfterDueDateCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST)) {
                final Integer foreclosureBackdatedShortPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedShortPaidInterest(foreclosureBackdatedShortPaidInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE)) {
                final Integer foreclosureBackdatedShortPaidInterestCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_INTEREST_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedShortPaidInterestCharge(foreclosureBackdatedShortPaidInterestCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST)) {
                final Integer foreclosureBackdatedFullyPaidInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedFullyPaidInterest(foreclosureBackdatedFullyPaidInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE)) {
                final Integer foreclosureBackdatedFullyPaidInterestCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_INTEREST_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedFullyPaidInterestCharge(foreclosureBackdatedFullyPaidInterestCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST)) {
                final Integer foreclosureBackdatedShortPaidPrincipalInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedShortPaidPrincipalInterest(foreclosureBackdatedShortPaidPrincipalInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE)) {
                final Integer foreclosureBackdatedShortPaidPrincipalCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_SHORT_PAID_PRINCIPAL_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedShortPaidPrincipalCharge(foreclosureBackdatedShortPaidPrincipalCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST)) {
                final Integer foreclosureBackdatedFullyPaidEmiInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedFullyPaidEmiInterest(foreclosureBackdatedFullyPaidEmiInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE)) {
                final Integer foreclosureBackdatedFullyPaidEmiCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_FULLY_PAID_EMI_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedFullyPaidEmiCharge(foreclosureBackdatedFullyPaidEmiCharge);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST)) {
                final Integer foreclosureBackdatedAdvanceInterest = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_INTEREST, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedAdvanceInterest(foreclosureBackdatedAdvanceInterest);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE)) {
                final Integer foreclosureBackdatedAdvanceCharge = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.FORECLOSURE_BACKDATED_ADVANCE_CHARGE, element, Locale.getDefault());
                productCollectionConfig.setForeclosureBackdatedAdvanceCharge(foreclosureBackdatedAdvanceCharge);
            }

            if (command.parameterExists(LoanProductConstants.ADVANCE_APPROPRIATION_AGAINST_ON)) {
                final Integer advanceAppropriationAgainstOn = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.ADVANCE_APPROPRIATION_AGAINST_ON, element);
                productCollectionConfig.setAdvanceAppropriationAgainstOn(advanceAppropriationAgainstOn);
            }

            if (command.parameterExists(LoanProductConstants.FORECLOSURE_METHOD_TYPE)) {
                final Integer foreclosureMethodTypes = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanProductConstants.FORECLOSURE_METHOD_TYPE, element);
                productCollectionConfig.setForeclosureMethodType(foreclosureMethodTypes);
            }else
                productCollectionConfig.setForeclosureMethodType(null);
				if (command.parameterExists(LoanProductConstants.COOLING_OFF_APPLICABILITY)) {
                final Boolean coolingOffApplicability = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.COOLING_OFF_APPLICABILITY, element);
                productCollectionConfig.setCoolingOffApplicability(coolingOffApplicability);
            }

            if (command.parameterExists(LoanProductConstants.COOLING_OFF_THRESHOLD_DAYS)) {
                final Integer coolingOffThresholdDays = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_THRESHOLD_DAYS, element,Locale.getDefault());
                productCollectionConfig.setCoolingOffThresholdDays(coolingOffThresholdDays);
            }

            if (command.parameterExists(LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY)) {
                final Integer coolingOffInterestAndChargeApplicability = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_INTEREST_AND_CHARGE_APPLICABILITY, element, Locale.getDefault());
                productCollectionConfig.setCoolingOffInterestAndChargeApplicability(coolingOffInterestAndChargeApplicability);
            }

            if (command.parameterExists(LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY)) {
                final Integer coolingOffInterestLogicApplicability = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_INTEREST_LOGIC_APPLICABILITY, element, Locale.getDefault());
                productCollectionConfig.setCoolingOffInterestLogicApplicability(coolingOffInterestLogicApplicability);
            }

            if (command.parameterExists(LoanProductConstants.COOLING_OFF_DAYS_IN_YEAR)) {
                final Integer coolingOffDaysInYear = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_DAYS_IN_YEAR, element, Locale.getDefault());
                productCollectionConfig.setCoolingOffDaysInYear(coolingOffDaysInYear);
            }

            if (command.parameterExists(LoanProductConstants.COOLING_OFF_ROUNDING_MODE)) {
                final String coolingOffRoundingMode = this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.COOLING_OFF_ROUNDING_MODE, element);
                productCollectionConfig.setCoolingOffRoundingMode(coolingOffRoundingMode);
            }

            if (command.parameterExists(LoanProductConstants.COOLING_OFF_ROUNDING_DECIMALS)) {
                final Integer coolingOffRoundingDecimals = this.fromApiJsonHelper.extractIntegerNamed(LoanProductConstants.COOLING_OFF_ROUNDING_DECIMALS, element,Locale.getDefault());
                productCollectionConfig.setCoolingOffRoundingDecimals(coolingOffRoundingDecimals);
            }
            /**
             *updating ModifiedUser And ModifiedDates
             */
            product.setModifiedDate(DateUtils.getDateOfTenant());
            product.setModifiedUser(currentUser);
            // accounting related changes
            final boolean accountingTypeChanged = changes.containsKey("accountingRule");
            final Map<String, Object> accountingMappingChanges = this.accountMappingWritePlatformService
                    .updateLoanProductToGLAccountMapping(product.getId(), command, accountingTypeChanged, product.getAccountingType());
            changes.putAll(accountingMappingChanges);

            if (changes.containsKey(LoanProductConstants.RATES_PARAM_NAME)) {
                final List<Rate> productRates = assembleListOfProductRates(command);
                final boolean updated = product.updateRates(productRates);
                if (!updated) {
                    changes.remove(LoanProductConstants.RATES_PARAM_NAME);
                }
            }

            if (!changes.isEmpty()) {
                product.validateLoanProductPreSave();
                this.loanProductRepository.saveAndFlush(product);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanProductId) //
                    .with(changes) //
                    .build();

        } catch (final DataIntegrityViolationException | JpaSystemException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }

    }

    private List<LoanProductFeeData> assembleListOfColendingChargeForUpdate(JsonCommand command, LoanProduct product) {

        List<LoanProductFeeData> loanProductFeesCharges = new ArrayList<LoanProductFeeData>();
        int processingFeeType = 0;
        int foreclosureType = 0;

        if (command.parameterExists("selectCharge")) {
            final JsonArray chargesArray = command.arrayOfParameterNamed("selectCharge");
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("colendingCharge")) {
                        final String chargeid = jsonObject.get("colendingCharge").getAsString();
                        if (chargeid.equalsIgnoreCase("")) {
                            throw new ColendingChargeException("Enable charge is given required Atleast one charge type is requires");
                        }
                        final Long id = jsonObject.get("colendingCharge").getAsLong();
                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                        if (charge.isForeclosureCharge()) {
                            foreclosureType++;
                        }
                        final CodeValue chargeType = charge.getType();
                        final String type = chargeType.getLabel();
                        final BigDecimal selfCharge = jsonObject.get("selfCharge").getAsBigDecimal();
                        final BigDecimal partnerCharges = jsonObject.get("partnerCharge").getAsBigDecimal();
                        loanProductFeesCharges.add(new LoanProductFeeData(charge, selfCharge, partnerCharges, type));
                    }
                }
                if (foreclosureType > 1) {
                    throw new LoanProductForeclosureException("Product Should have only one type of Foreclosure Charge");
                }
            }
        }
        if (command.parameterExists("selectFees")) {
            final JsonArray chargesArray = command.arrayOfParameterNamed("selectFees");
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("colendingFees")) {
                        final String feesId = jsonObject.get("colendingFees").getAsString();
                        if (feesId.equalsIgnoreCase("")) {
                            throw new ColendingFeesException("Enable Fees is given required Atleast one Fees type is requires");
                        } else {
                            final Long id = jsonObject.get("colendingFees").getAsLong();
                            final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                            String ids = charge.getFeesChargeTypes();
                            if ("Processing Fees".equals(ids)) {
                                processingFeeType++;
                            }
                            if (charge.isForeclosureCharge()) {
                                foreclosureType++;
                            }
                            final CodeValue chargeType = charge.getType();
                            final String type = chargeType.getLabel();
                            final BigDecimal selfFees = jsonObject.get("selfFees").getAsBigDecimal();
                            final BigDecimal partnerFees = jsonObject.get("partnerFees").getAsBigDecimal();
                            ;
                            loanProductFeesCharges.add(new LoanProductFeeData(charge, selfFees, partnerFees, type));
                        }
                    }
                }
                if (processingFeeType > 1) {
                    throw new LoanProductProcessingFeeException("Product Should have only one type of processing Fee");
                }
                if (foreclosureType > 1) {
                    throw new LoanProductForeclosureException("Product Should have only one type of Foreclosure Charge");
                }
            }
        }

        if (command.parameterExists("selectOverDue")) {
            final JsonArray overDueChargesArray = command.arrayOfParameterNamed("selectOverDue");
            for(JsonElement jsonElement: overDueChargesArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (!jsonObject.has(OVERDUECHARGE_OBJECT_KEY)) {
                    throw new ColendingFeesException("Overdue Charges Enable. Please provide one overdue charge");
                }
                final Long id = jsonObject.get(OVERDUECHARGE_OBJECT_KEY).getAsLong();
                final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                String type = charge.getType().getLabel();
                BigDecimal selfCharge = jsonObject.get(SELFCHARGE_OBJECT_KEY).getAsBigDecimal();
                BigDecimal partnerCharge = jsonObject.get(PARTNERCHARGE_OBJECT_KEY).getAsBigDecimal();
                loanProductFeesCharges.add(new LoanProductFeeData(charge, selfCharge, partnerCharge, type));
            }
        }

        return loanProductFeesCharges;
    }


    private boolean anyChangeInCriticalFloatingRateLinkedParams(JsonCommand command, LoanProduct product) {
        final boolean isChangeFromFloatingToFlatOrViceVersa = command.isChangeInBooleanParameterNamed("isLinkedToFloatingInterestRates",
                product.isLinkedToFloatingInterestRate());
        final boolean isChangeInCriticalFloatingRateParams = product.getFloatingRates() != null
                && (command.isChangeInLongParameterNamed("floatingRatesId", product.getFloatingRates().getFloatingRate().getId())
                || command.isChangeInBigDecimalParameterNamed("interestRateDifferential",
                product.getFloatingRates().getInterestRateDifferential()));
        return isChangeFromFloatingToFlatOrViceVersa || isChangeInCriticalFloatingRateParams;
    }
    private List<LoanProductFeeData> assembleListOfColendingFees(JsonCommand command) {
        List<LoanProductFeeData> loanProductFeesCharges = new ArrayList<LoanProductFeeData>();
        LoanProductFeesCharges Charge = new LoanProductFeesCharges();


        List<LoanProductFeeData> loanProductFeeData = null;
        if (command.parameterExists("selectFees")) {
            final JsonArray chargesArray = command.arrayOfParameterNamed("selectFees");
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("colendingFees")) {
                        final Long id = jsonObject.get("colendingFees").getAsLong();
                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                        final CodeValue chargetype=charge.getType();
                        final String type=chargetype.getLabel();

                        final BigDecimal selfFees = jsonObject.get("selfFees").getAsBigDecimal();
                        final BigDecimal partnerFees = jsonObject.get("partnerFees").getAsBigDecimal();;
                        loanProductFeesCharges.add(new LoanProductFeeData(charge,selfFees,partnerFees,type));



                    }
                }
            }
        }


        return loanProductFeesCharges;


    }
    private List<LoanProductFeeData> assembleListOfColendingCharge(JsonCommand command) {
        List<LoanProductFeeData> loanProductFeesCharges = new ArrayList<LoanProductFeeData>();
        int processingFeeType = 0;
        int foreclosureType = 0;

        if (command.parameterExists("selectCharge")) {
            final JsonArray chargesArray = command.arrayOfParameterNamed("selectCharge");
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("colendingCharge")) {
                        final String chargeid = jsonObject.get("colendingCharge").getAsString();
                        if(chargeid.equalsIgnoreCase("")){
                            throw new ColendingChargeException("Enable charge is given required Atleast one charge type is requires");
                        }
                        final Long id = jsonObject.get("colendingCharge").getAsLong();

                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                        if (charge.isForeclosureCharge()) {
                            foreclosureType++;
                        }
                        final CodeValue chargeType =charge.getType();
                        final String type=chargeType.getLabel();
                        final BigDecimal selfCharge = jsonObject.get("selfCharge").getAsBigDecimal();
                        final BigDecimal partnerCharges = jsonObject.get("partnerCharge").getAsBigDecimal();

                        loanProductFeesCharges.add(new LoanProductFeeData(charge,selfCharge,partnerCharges,type));
                    }
                }
                if(foreclosureType > 1){ throw new LoanProductForeclosureException("Product Should have only one type of Foreclosure Charge");}
            }
        }
        if (command.parameterExists("selectFees"))
        {
            final JsonArray chargesArray = command.arrayOfParameterNamed("selectFees");
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("colendingFees")) {
                        final String feesId = jsonObject.get("colendingFees").getAsString();
                        if (feesId.equalsIgnoreCase("")) {
                            throw new ColendingFeesException("Enable Fees is given required Atleast one Fees type is requires");
                        } else {
                            final Long id = jsonObject.get("colendingFees").getAsLong();
                            final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                            String ids = charge.getFeesChargeTypes();
                            if ("Processing Fees".equals(ids)) {
                                processingFeeType++;
                            }
                            if (charge.isForeclosureCharge()) {
                                foreclosureType++;
                            }
                            final CodeValue chargeType = charge.getType();
                            final String type = chargeType.getLabel();
                            final BigDecimal selfFees = jsonObject.get("selfFees").getAsBigDecimal();
                            final BigDecimal partnerFees = jsonObject.get("partnerFees").getAsBigDecimal();
                            ;
                            loanProductFeesCharges.add(new LoanProductFeeData(charge, selfFees, partnerFees, type));
                        }
                    }
                }
                if(processingFeeType > 1){    throw new LoanProductProcessingFeeException("Product Should have only one type of processing Fee");}
                if(foreclosureType > 1){ throw new LoanProductForeclosureException("Product Should have only one type of Foreclosure Charge");}
            }
        }

        if (command.parameterExists("selectOverDue")) {
            final JsonArray overDueChargesArray = command.arrayOfParameterNamed("selectOverDue");
            for(JsonElement jsonElement: overDueChargesArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (!jsonObject.has(OVERDUECHARGE_OBJECT_KEY)) {
                    throw new ColendingFeesException("Overdue Charges Enable. Please provide one overdue charge");
                }
                final Long id = jsonObject.get(OVERDUECHARGE_OBJECT_KEY).getAsLong();
                final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                String type = charge.getType().getLabel();
                BigDecimal selfCharge = jsonObject.get(SELFCHARGE_OBJECT_KEY).getAsBigDecimal();
                BigDecimal partnerCharge = jsonObject.get(PARTNERCHARGE_OBJECT_KEY).getAsBigDecimal();
                loanProductFeesCharges.add(new LoanProductFeeData(charge, selfCharge, partnerCharge, type));
            }
        }
        return loanProductFeesCharges;
    }

    private List<Charge> assembleListOfProductCharges(final JsonCommand command, final String currencyCode) {

        final List<Charge> charges = new ArrayList<>();

        String loanProductCurrencyCode = command.stringValueOfParameterNamed("currencyCode");


        if (loanProductCurrencyCode == null) {
            loanProductCurrencyCode = currencyCode;
        }

        if (command.parameterExists("charges")) {
            final JsonArray chargesArray = command.arrayOfParameterNamed("charges");
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("id")) {
                        final Long id = jsonObject.get("id").getAsLong();

                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);

                        if (!loanProductCurrencyCode.equals(charge.getCurrencyCode())) {
                            final String errorMessage = "Charge and Loan Product must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.loan.product", errorMessage);
                        }
                        charges.add(charge);
                    }
                }
            }

        }


        return charges;
    }

    private List<Rate> assembleListOfProductRates(final JsonCommand command) {

        final List<Rate> rates = new ArrayList<>();

        if (command.parameterExists("rates")) {
            final JsonArray ratesArray = command.arrayOfParameterNamed("rates");
            if (ratesArray != null) {
                List<Long> idList = new ArrayList<>();
                for (int i = 0; i < ratesArray.size(); i++) {

                    final JsonObject jsonObject = ratesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("id")) {
                        idList.add(jsonObject.get("id").getAsLong());
                    }
                }
                rates.addAll(this.rateRepository.findMultipleWithNotFoundDetection(idList));
            }
        }

        return rates;
    }
    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("'external_id'")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.externalId",
                    "Loan Product with externalId `" + externalId + "` already exists", "externalId", externalId, realCause);
        } else if (realCause.getMessage().contains("'unq_name'")) {

            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.name",
                    "Loan product with name `" + name + "` already exists", "name", name, realCause);
        } else if (realCause.getMessage().contains("short_name")) {

            final String shortName = command.stringValueOfParameterNamed("shortName");
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.short.name",
                    "Loan product with short name `" + shortName + "` already exists", "shortName", shortName, realCause);
        } else if (realCause.getMessage().contains("Duplicate entry")) {
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.entry",
                    "Loan product may only have one charge of each type.`", "charges", realCause);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.product.loan.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.", realCause);
    }

    private void validateInputDates(final JsonCommand command) {
        final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
        final LocalDate closeDate = command.localDateValueOfParameterNamed("closeDate");

        if (startDate != null && closeDate != null) {
            if (closeDate.isBefore(startDate)) {
                throw new LoanProductDateException(startDate.toString(), closeDate.toString());
            }
        }
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        LOG.error("Error occured.", dve);
    }

    private Map<BusinessEventNotificationConstants.BusinessEntity, Object> constructEntityMap(
            final BusinessEventNotificationConstants.BusinessEntity entityEvent, Object entity) {
        Map<BusinessEventNotificationConstants.BusinessEntity, Object> map = new HashMap<>(1);
        map.put(entityEvent, entity);
        return map;
    }
}
