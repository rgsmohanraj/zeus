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
package org.vcpl.lms.portfolio.collateral.service;

import java.util.Map;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.exception.PlatformDataIntegrityException;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.collateral.api.CollateralApiConstants;
import org.vcpl.lms.portfolio.collateral.api.CollateralApiConstants.CollateralJSONinputParams;
import org.vcpl.lms.portfolio.collateral.command.CollateralCommand;
import org.vcpl.lms.portfolio.collateral.domain.LoanCollateral;
import org.vcpl.lms.portfolio.collateral.domain.LoanCollateralRepository;
import org.vcpl.lms.portfolio.collateral.exception.CollateralCannotBeCreatedException;
import org.vcpl.lms.portfolio.collateral.exception.CollateralCannotBeCreatedException.LoanCollateralCannotBeCreatedReason;
import org.vcpl.lms.portfolio.collateral.exception.CollateralCannotBeDeletedException;
import org.vcpl.lms.portfolio.collateral.exception.CollateralCannotBeDeletedException.LoanCollateralCannotBeDeletedReason;
import org.vcpl.lms.portfolio.collateral.exception.CollateralCannotBeUpdatedException;
import org.vcpl.lms.portfolio.collateral.exception.CollateralCannotBeUpdatedException.LoanCollateralCannotBeUpdatedReason;
import org.vcpl.lms.portfolio.collateral.exception.CollateralNotFoundException;
import org.vcpl.lms.portfolio.collateral.serialization.CollateralCommandFromApiJsonDeserializer;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollateralWritePlatformServiceJpaRepositoryImpl implements CollateralWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(CollateralWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanCollateralRepository collateralRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final CollateralCommandFromApiJsonDeserializer collateralCommandFromApiJsonDeserializer;

    @Autowired
    public CollateralWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final LoanRepositoryWrapper loanRepositoryWrapper, final LoanCollateralRepository collateralRepository,
            final CodeValueRepositoryWrapper codeValueRepository,
            final CollateralCommandFromApiJsonDeserializer collateralCommandFromApiJsonDeserializer) {
        this.context = context;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.collateralRepository = collateralRepository;
        this.codeValueRepository = codeValueRepository;
        this.collateralCommandFromApiJsonDeserializer = collateralCommandFromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult addCollateral(final Long loanId, final JsonCommand command) {

        this.context.authenticatedUser();
        final CollateralCommand collateralCommand = this.collateralCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        collateralCommand.validateForCreate();

        try {
            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            final CodeValue collateralType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                    CollateralApiConstants.COLLATERAL_CODE_NAME, collateralCommand.getCollateralTypeId());
            final LoanCollateral collateral = LoanCollateral.fromJson(loan, collateralType, command);

            /**
             * Collaterals may be added only when the loan associated with them are yet to be approved
             **/
            if (!loan.status().isSubmittedAndPendingApproval()) {
                throw new CollateralCannotBeCreatedException(
                        LoanCollateralCannotBeCreatedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loan.getId());
            }

            this.collateralRepository.saveAndFlush(collateral);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withLoanId(loan.getId())//
                    .withEntityId(collateral.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleCollateralDataIntegrityViolation(dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateCollateral(final Long loanId, final Long collateralId, final JsonCommand command) {

        this.context.authenticatedUser();
        final CollateralCommand collateralCommand = this.collateralCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        collateralCommand.validateForUpdate();

        final Long collateralTypeId = collateralCommand.getCollateralTypeId();
        try {
            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            CodeValue collateralType = null;

            final LoanCollateral collateralForUpdate = this.collateralRepository.findById(collateralId)
                    .orElseThrow(() -> new CollateralNotFoundException(loanId, collateralId));

            final Map<String, Object> changes = collateralForUpdate.update(command);

            if (changes.containsKey(CollateralJSONinputParams.COLLATERAL_TYPE_ID.getValue())) {

                collateralType = this.codeValueRepository
                        .findOneByCodeNameAndIdWithNotFoundDetection(CollateralApiConstants.COLLATERAL_CODE_NAME, collateralTypeId);
                collateralForUpdate.setCollateralType(collateralType);
            }

            /**
             * Collaterals may be updated only when the loan associated with them are yet to be approved
             **/
            if (!loan.status().isSubmittedAndPendingApproval()) {
                throw new CollateralCannotBeUpdatedException(
                        LoanCollateralCannotBeUpdatedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loan.getId());
            }

            if (!changes.isEmpty()) {
                this.collateralRepository.saveAndFlush(collateralForUpdate);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withLoanId(command.getLoanId())//
                    .withEntityId(collateralId) //
                    .with(changes) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleCollateralDataIntegrityViolation(dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteCollateral(final Long loanId, final Long collateralId, final Long commandId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanCollateral collateral = this.collateralRepository.findByLoanIdAndId(loanId, collateralId);
        if (collateral == null) {
            throw new CollateralNotFoundException(loanId, collateralId);
        }

        /**
         * Collaterals may be deleted only when the loan associated with them are yet to be approved
         **/
        if (!loan.status().isSubmittedAndPendingApproval()) {
            throw new CollateralCannotBeDeletedException(
                    LoanCollateralCannotBeDeletedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loanId, collateralId);
        }

        this.collateralRepository.delete(collateral);
        return new CommandProcessingResultBuilder().withCommandId(commandId).withLoanId(loanId).withEntityId(collateralId).build();
    }

    private void handleCollateralDataIntegrityViolation(final NonTransientDataAccessException dve) {
        LOG.error("Error occured.", dve);
        throw new PlatformDataIntegrityException("error.msg.collateral.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
