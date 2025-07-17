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
package org.vcpl.lms.portfolio.rate.service;

import static org.vcpl.lms.portfolio.rate.api.RateApiConstants.approveUserIdParamName;

import java.util.Map;
import jakarta.persistence.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.exception.PlatformDataIntegrityException;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.rate.domain.Rate;
import org.vcpl.lms.portfolio.rate.domain.RateRepository;
import org.vcpl.lms.portfolio.rate.exception.RateNotFoundException;
import org.vcpl.lms.portfolio.rate.serialization.RateDefinitionCommandFromApiJsonDeserializer;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.vcpl.lms.useradministration.domain.AppUserRepository;
import org.vcpl.lms.useradministration.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bowpi GT Created by Jose on 19/07/2017.
 */
@Service
public class RateWriteServiceImpl implements RateWriteService {

    private static final Logger LOG = LoggerFactory.getLogger(RateWriteServiceImpl.class);

    private final RateRepository rateRepository;
    private final AppUserRepository appUserRepository;
    private final PlatformSecurityContext context;
    private final RateDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public RateWriteServiceImpl(RateRepository rateRepository, AppUserRepository appUserRepository,
            final RateDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer, PlatformSecurityContext context) {
        this.rateRepository = rateRepository;
        this.appUserRepository = appUserRepository;
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Override
    public CommandProcessingResult createRate(JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Long approveUserId = command.longValueOfParameterNamed(approveUserIdParamName);
            AppUser approveUser = null;
            if (approveUserId != null) {
                approveUser = this.appUserRepository.findById(approveUserId).orElseThrow(() -> new UserNotFoundException(approveUserId));
            }
            final Rate rate = Rate.fromJson(command, approveUser);

            this.rateRepository.saveAndFlush(rate);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(rate.getId()).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleRateDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleRateDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateRate(final Long rateId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            final Rate rateToUpdate = this.rateRepository.findById(rateId).orElseThrow(() -> new RateNotFoundException(rateId));

            final Map<String, Object> changes = rateToUpdate.update(command);

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            if (changes.containsKey(approveUserIdParamName)) {
                final Long newApproveUserId = (Long) changes.get(approveUserIdParamName);
                AppUser newApproveUser = null;
                if (newApproveUserId != null) {
                    newApproveUser = this.appUserRepository.findById(newApproveUserId)
                            .orElseThrow(() -> new UserNotFoundException(newApproveUserId));
                }
                rateToUpdate.setApproveUser(newApproveUser);
            }
            if (!changes.isEmpty()) {
                this.rateRepository.saveAndFlush(rateToUpdate);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(rateId) //
                    .with(changes) //
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleRateDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return new CommandProcessingResult((long) -1);
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleRateDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleRateDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("rate_name_org")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.fund.duplicate.externalId",
                    "A rate with name '" + name + "' already exists", "name", name);
        }

        LOG.error("Error due to Exception", dve);
        throw new PlatformDataIntegrityException("error.msg.fund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
