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

import java.util.Collection;
import java.util.Map;
import jakarta.persistence.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vcpl.lms.accounting.glaccount.domain.GLAccount;
import org.vcpl.lms.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.exception.PlatformDataIntegrityException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.vcpl.lms.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.charge.api.ChargesApiConstants;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.charge.domain.ChargeRepository;
import org.vcpl.lms.portfolio.charge.exception.ChargeCannotBeDeletedException;
import org.vcpl.lms.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.vcpl.lms.portfolio.charge.exception.ChargeNotFoundException;
import org.vcpl.lms.portfolio.charge.serialization.ChargeDefinitionCommandFromApiJsonDeserializer;
import org.vcpl.lms.portfolio.fund.domain.Fund;
import org.vcpl.lms.portfolio.loanproduct.LoanProductConstants;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRepository;
import org.vcpl.lms.portfolio.paymentdetail.PaymentDetailConstants;
import org.vcpl.lms.portfolio.paymenttype.domain.PaymentType;
import org.vcpl.lms.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.vcpl.lms.portfolio.tax.domain.TaxGroup;
import org.vcpl.lms.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.useradministration.domain.AppUser;

@Service
public class ChargeWritePlatformServiceJpaRepositoryImpl implements ChargeWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(ChargeWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final JdbcTemplate jdbcTemplate;
    private final ChargeRepository chargeRepository;
    private final LoanProductRepository loanProductRepository;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final GLAccountRepositoryWrapper glAccountRepository;
    private final TaxGroupRepositoryWrapper taxGroupRepository;
    private final PaymentTypeRepositoryWrapper paymentTyperepositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    @Autowired
    public ChargeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer, final ChargeRepository chargeRepository,
            final LoanProductRepository loanProductRepository, final JdbcTemplate jdbcTemplate,
            final FineractEntityAccessUtil fineractEntityAccessUtil, final GLAccountRepositoryWrapper glAccountRepository,
            final TaxGroupRepositoryWrapper taxGroupRepository, final PaymentTypeRepositoryWrapper paymentTyperepositoryWrapper,final CodeValueRepositoryWrapper codeValueRepositoryWrapper) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.jdbcTemplate = jdbcTemplate;
        this.chargeRepository = chargeRepository;
        this.loanProductRepository = loanProductRepository;
        this.fineractEntityAccessUtil = fineractEntityAccessUtil;
        this.glAccountRepository = glAccountRepository;
        this.taxGroupRepository = taxGroupRepository;
        this.paymentTyperepositoryWrapper = paymentTyperepositoryWrapper;
        this.codeValueRepositoryWrapper=codeValueRepositoryWrapper;
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult createCharge(final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            AppUser currentUser = null;
            final SecurityContext context = SecurityContextHolder.getContext();
            final Authentication auth = context.getAuthentication();
            if (auth != null) {
                currentUser = (AppUser) auth.getPrincipal();
            }
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            // Retrieve linked GLAccount for Client charges (if present)
            final Long glAccountId = command.longValueOfParameterNamed(ChargesApiConstants.glAccountIdParamName);

            GLAccount glAccount = null;
            if (glAccountId != null) {
                glAccount = this.glAccountRepository.findOneWithNotFoundDetection(glAccountId);
            }

            final Long taxGroupId = command.longValueOfParameterNamed(ChargesApiConstants.taxGroupIdParamName);
            TaxGroup taxGroup = null;
            if (taxGroupId != null) {
                taxGroup = this.taxGroupRepository.findOneWithNotFoundDetection(taxGroupId);
            }

            final boolean enablePaymentType = command.booleanPrimitiveValueOfParameterNamed("enablePaymentType");
            PaymentType paymentType = null;
            if (enablePaymentType) {
                final Long paymentTypeId = command.longValueOfParameterNamed(PaymentDetailConstants.paymentTypeParamName);
                if (paymentTypeId != null) {
                    paymentType = this.paymentTyperepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
                }
            }

            CodeValue type = null;
            //String chargeType=null;
            final Long typeId=command.longValueOfParameterNamed(ChargesApiConstants.TYPE);
            if (typeId != null) {
                type = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(ChargesApiConstants.TYPE,typeId);
                //chargeType=type.getLabel();

            }

            CodeValue feesChargeType = null;
            final Long feesId = command.longValueOfParameterNamed(ChargesApiConstants.FEESCHARGETYPE);
            if (feesId != null) {
                feesChargeType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(ChargesApiConstants.FEESCHARGETYPE,feesId);
            }


            final Charge charge = Charge.fromJson(command, glAccount, taxGroup, paymentType,type,feesChargeType);
            /**
             * Created and modified date added
             */
            charge.setCreatedDate(DateUtils.getDateOfTenant());
            charge.setCreatedUser(currentUser);
            this.chargeRepository.saveAndFlush(charge);

            // check if the office specific products are enabled. If yes, then
            // save this savings product against a specific office
            // i.e. this savings product is specific for this office.
            fineractEntityAccessUtil.checkConfigurationAndAddProductResrictionsForUserOffice(
                    FineractEntityAccessType.OFFICE_ACCESS_TO_CHARGES, charge.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(charge.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult updateCharge(final Long chargeId, final JsonCommand command) {

        try {
            AppUser currentUser = null;
            final SecurityContext context = SecurityContextHolder.getContext();
            final Authentication auth = context.getAuthentication();
            if (auth != null) {
                currentUser = (AppUser) auth.getPrincipal();
            }
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Charge chargeForUpdate = this.chargeRepository.findById(chargeId)
                    .orElseThrow(() -> new ChargeNotFoundException(chargeId));

            final Map<String, Object> changes = chargeForUpdate.update(command);

            if (changes.containsKey(ChargesApiConstants.typeSelected)) {

                final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.typeSelected);
                CodeValue typeSelected = null;
                if (newValue != null) {
                    typeSelected = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(ChargesApiConstants.TYPE, newValue);

                    chargeForUpdate.updateChargeId(typeSelected);
                }
            }

            if (changes.containsKey(ChargesApiConstants.feesChargeTypeSelected)) {

                final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.feesChargeTypeSelected);
                CodeValue feesChargeTypeSelected = null;
                if (newValue != null) {
                    feesChargeTypeSelected = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(ChargesApiConstants.FEESCHARGETYPE, newValue);
                    chargeForUpdate.updateFeesId(feesChargeTypeSelected);
                }
            }

            this.fromApiJsonDeserializer.validateChargeTimeNCalculationType(chargeForUpdate.getChargeTimeType(),
                    chargeForUpdate.getChargeCalculation());

            if (changes.containsKey("active")) {
                // IF the key exists then it has changed (otherwise it would
                // have been filtered), so check current state:
                if (!chargeForUpdate.isActive()) {
                    // TODO: Change this function to only check the mappings!!!
                    final Boolean isChargeExistWithLoans = isAnyLoanProductsAssociateWithThisCharge(chargeId);
                    final Boolean isChargeExistWithSavings = isAnySavingsProductsAssociateWithThisCharge(chargeId);

                    if (isChargeExistWithLoans || isChargeExistWithSavings) {
                        throw new ChargeCannotBeUpdatedException("error.msg.charge.cannot.be.updated.it.is.used.in.loan",
                                "This charge cannot be updated, it is used in loan");
                    }
                }
            } else if ((changes.containsKey("feeFrequency") || changes.containsKey("feeInterval")) && chargeForUpdate.isLoanCharge()) {
                final Boolean isChargeExistWithLoans = isAnyLoanProductsAssociateWithThisCharge(chargeId);
                if (isChargeExistWithLoans) {
                    throw new ChargeCannotBeUpdatedException("error.msg.charge.frequency.cannot.be.updated.it.is.used.in.loan",
                            "This charge frequency cannot be updated, it is used in loan");
                }
            }

            // Has account Id been changed ?
            if (changes.containsKey(ChargesApiConstants.glAccountIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.glAccountIdParamName);
                GLAccount newIncomeAccount = null;
                if (newValue != null) {
                    newIncomeAccount = this.glAccountRepository.findOneWithNotFoundDetection(newValue);
                }
                chargeForUpdate.setAccount(newIncomeAccount);
            }

            final String paymentTypeIdParamName = "paymentTypeId";
            if (changes.containsKey(paymentTypeIdParamName)) {

                final Integer paymentTypeIdNewValue = command.integerValueOfParameterNamed(paymentTypeIdParamName);

                PaymentType paymentType = null;
                if (paymentTypeIdNewValue != null) {
                    final Long paymentTypeId = paymentTypeIdNewValue.longValue();

                    paymentType = this.paymentTyperepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
                    chargeForUpdate.setPaymentType(paymentType);
                }

            }

            if (changes.containsKey(ChargesApiConstants.taxGroupIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.taxGroupIdParamName);
                TaxGroup taxGroup = null;
                if (newValue != null) {
                    taxGroup = this.taxGroupRepository.findOneWithNotFoundDetection(newValue);
                }
                chargeForUpdate.setTaxGroup(taxGroup);
            }
            if (changes.containsKey(ChargesApiConstants.PENALTY_INTEGER_DAYS_IN_YEARS)) {
                final Boolean isChargeExistWithLoans = isAnyLoanProductsAssociateWithThisCharge(chargeId);
                if (isChargeExistWithLoans) {
                    throw new ChargeCannotBeUpdatedException("error.msg.charge.frequency.cannot.be.updated.it.is.used.in.loan",
                            "Penality Interest days in years cannot be updated, it is used in loan");
                }
                chargeForUpdate.setPenaltyInterestDaysInYear(command.integerValueOfParameterNamed(ChargesApiConstants.PENALTY_INTEGER_DAYS_IN_YEARS));
            }

            /**
             *modified date and modified user added
             */
            chargeForUpdate.setModifiedDate(DateUtils.getDateOfTenant());
            chargeForUpdate.setModifiedUser(currentUser);

            if (!changes.isEmpty()) {
                this.chargeRepository.save(chargeForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(chargeId).with(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult deleteCharge(final Long chargeId) {
        AppUser currentUser = null;
        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication auth = context.getAuthentication();
        if (auth != null) {
            currentUser = (AppUser) auth.getPrincipal();
        }

        final Charge chargeForDelete = this.chargeRepository.findById(chargeId).orElseThrow(() -> new ChargeNotFoundException(chargeId));
        if (chargeForDelete.isDeleted()) {
            throw new ChargeNotFoundException(chargeId);
        }

        final Collection<LoanProduct> loanProducts = this.loanProductRepository.retrieveLoanProductsByChargeId(chargeId);
        final Boolean isChargeExistWithLoans = isAnyLoansAssociateWithThisCharge(chargeId);
        final Boolean isChargeExistWithSavings = isAnySavingsAssociateWithThisCharge(chargeId);

        // TODO: Change error messages around:
        if (!loanProducts.isEmpty() || isChargeExistWithLoans || isChargeExistWithSavings) {
            throw new ChargeCannotBeDeletedException("error.msg.charge.cannot.be.deleted.it.is.already.used.in.loan",
                    "This charge cannot be deleted, it is already used in loan");
        }

        chargeForDelete.delete();
        /**
         * modified date added for deleted charge
         */
        chargeForDelete.setModifiedDate(DateUtils.getDateOfTenant());
        chargeForDelete.setModifiedUser(currentUser);

        this.chargeRepository.save(chargeForDelete);

        return new CommandProcessingResultBuilder().withEntityId(chargeForDelete.getId()).build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.charge.duplicate.name", "Charge with name `" + name + "` already exists",
                    "name", name);
        }

        LOG.error("Error occured.", dve);
        throw new PlatformDataIntegrityException("error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    private boolean isAnyLoansAssociateWithThisCharge(final Long chargeId) {
        final String sql = "select (CASE WHEN exists (select 1 from m_loan_charge lc where lc.charge_id = ? and lc.is_active = true) THEN 'true' ELSE 'false' END)";
        final String isLoansUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return Boolean.valueOf(isLoansUsingCharge);
    }

    private boolean isAnySavingsAssociateWithThisCharge(final Long chargeId) {
        final String sql = "select (CASE WHEN exists (select 1 from m_savings_account_charge sc where sc.charge_id = ? and sc.is_active = true) THEN 'true' ELSE 'false' END)";
        final String isSavingsUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return Boolean.valueOf(isSavingsUsingCharge);
    }

    private boolean isAnyLoanProductsAssociateWithThisCharge(final Long chargeId) {
        final String sql = "select (CASE WHEN exists (select 1 from m_product_loan_charge lc where lc.charge_id = ?) THEN 'true' ELSE 'false' END)";
        final String isLoansUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return Boolean.valueOf(isLoansUsingCharge);
    }

    private boolean isAnySavingsProductsAssociateWithThisCharge(final Long chargeId) {
        final String sql = "select (CASE WHEN (exists (select 1 from m_savings_product_charge sc where sc.charge_id = ?)) = 1 THEN 'true' ELSE 'false' END)";
        final String isSavingsUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return Boolean.valueOf(isSavingsUsingCharge);
    }
}
