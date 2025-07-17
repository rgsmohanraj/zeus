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
package org.apache.fineract.portfolio.loanproducttype.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproducttype.domain.LoanProductType;
import org.apache.fineract.portfolio.loanproducttype.domain.LoanProductTypeRepository;
import org.apache.fineract.portfolio.loanproducttype.exception.LoanTypeNotFoundException;
import org.apache.fineract.portfolio.loanproducttype.serialization.LoanTypeCommandFromApiJsonDeserializer;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.Map;
@Service
public class LoanProductTypeWritePlatformServiceJpaRepoImpl implements LoanProductTypeWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanProductTypeWritePlatformServiceJpaRepoImpl.class);

    private final PlatformSecurityContext context;
    private final LoanTypeCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final LoanProductTypeRepository loanProductTypeRepository;

    @Autowired
    public LoanProductTypeWritePlatformServiceJpaRepoImpl(final PlatformSecurityContext context,
                                                          final LoanTypeCommandFromApiJsonDeserializer fromApiJsonDeserializer, final LoanProductTypeRepository loanProductTypeRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanProductTypeRepository = loanProductTypeRepository;
    }

    @Transactional
    @Override
    @CacheEvict(value = "types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ty')")
    public CommandProcessingResult createType(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final LoanProductType loanProductType = LoanProductType.fromJson(command);

            this.loanProductTypeRepository.saveAndFlush(loanProductType);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanProductType.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleTypeDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleTypeDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ty')")
    public CommandProcessingResult updateType(final Long typeId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final LoanProductType loanProductType = this.loanProductTypeRepository.findById(typeId).orElseThrow(() -> new LoanTypeNotFoundException(typeId));

            final Map<String, Object> changes = loanProductType.update(command);
            if (!changes.isEmpty()) {
                this.loanProductTypeRepository.saveAndFlush(loanProductType);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanProductType.getId()).with(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleTypeDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleTypeDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleTypeDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("m_loan_product_type_external_id_key")) {
            final String description = command.stringValueOfParameterNamed("description");
            throw new PlatformDataIntegrityException("error.msg.loanproducttype.duplicate.description",
                    "A loanproducttype with description '" + description + "' already exists", "description", description);
        } else if (realCause.getMessage().contains("m_loan_product_type_external_id_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.loanproducttype.duplicate.name", "A loanproducttype with name '" + name + "' already exists",
                    "name", name);
        }

        LOG.error("Error occured.", dve);
        throw new PlatformDataIntegrityException("error.msg.loanproducttype.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

}
