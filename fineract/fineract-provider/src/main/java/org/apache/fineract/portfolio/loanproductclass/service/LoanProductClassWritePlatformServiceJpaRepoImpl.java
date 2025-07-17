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
package org.apache.fineract.portfolio.loanproductclass.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproductclass.domain.LoanProductClass;
import org.apache.fineract.portfolio.loanproductclass.domain.LoanProductClassRepository;
import org.apache.fineract.portfolio.loanproductclass.exception.LoanClassNotFoundException;
import org.apache.fineract.portfolio.loanproductclass.serialization.LoanClassCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.Map;
@Service
public class LoanProductClassWritePlatformServiceJpaRepoImpl implements LoanProductClassWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanProductClassWritePlatformServiceJpaRepoImpl.class);

    private final PlatformSecurityContext context;
    private final LoanClassCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final LoanProductClassRepository loanProductClassRepository;

    @Autowired
    public LoanProductClassWritePlatformServiceJpaRepoImpl(final PlatformSecurityContext context,
                                                     final LoanClassCommandFromApiJsonDeserializer fromApiJsonDeserializer, final LoanProductClassRepository loanProductClassRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanProductClassRepository = loanProductClassRepository;
    }

    @Transactional
    @Override
    @CacheEvict(value = "classes", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('cl')")
    public CommandProcessingResult createClass(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final LoanProductClass loanProductClass = LoanProductClass.fromJson(command);

            this.loanProductClassRepository.saveAndFlush(loanProductClass);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanProductClass.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleClassDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleClassDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "classes", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('cl')")
    public CommandProcessingResult updateClass(final Long classId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final LoanProductClass loanProductClass = this.loanProductClassRepository.findById(classId).orElseThrow(() -> new LoanClassNotFoundException(classId));

            final Map<String, Object> changes = loanProductClass.update(command);
            if (!changes.isEmpty()) {
                this.loanProductClassRepository.saveAndFlush(loanProductClass);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanProductClass.getId()).with(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleClassDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleClassDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }


    private void handleClassDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("m_loan_product_class_external_id_key")) {
            final String description = command.stringValueOfParameterNamed("description");
            throw new PlatformDataIntegrityException("error.msg.loanproductclass.duplicate.description",
                    "A loanproductclass with description '" + description + "' already exists", "description", description);
        } else if (realCause.getMessage().contains("m_loan_product_class_external_id_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.loanproductclass.duplicate.name", "A loanproductclass with name '" + name + "' already exists",
                    "name", name);
        }

        LOG.error("Error occured.", dve);
        throw new PlatformDataIntegrityException("error.msg.loanproductclass.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
