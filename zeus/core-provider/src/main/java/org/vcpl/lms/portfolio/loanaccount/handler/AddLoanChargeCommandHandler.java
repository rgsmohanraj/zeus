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
package org.vcpl.lms.portfolio.loanaccount.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.vcpl.lms.commands.annotation.CommandType;
import org.vcpl.lms.commands.handler.NewCommandSourceHandler;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.exception.PlatformDataIntegrityException;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeTransactionRequest;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCharge;
import org.vcpl.lms.portfolio.loanaccount.serialization.ChargeCollectionApiJsonValidator;
import org.vcpl.lms.portfolio.loanaccount.service.LoanChargeCreationService;
import org.vcpl.lms.portfolio.loanaccount.service.LoanWritePlatformService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@CommandType(entity = "LOANCHARGE", action = "CREATE")
public class AddLoanChargeCommandHandler implements NewCommandSourceHandler {

    private final LoanWritePlatformService writePlatformService;
    private final ChargeCollectionApiJsonValidator validator;
    private final LoanChargeCreationService loanChargeCreationService;

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        try {
            ChargeTransactionRequest chargeTransactionRequest = validator.createCharge(command.json());
            chargeTransactionRequest.setLoanId(command.getLoanId());
            LoanCharge loanCharge = loanChargeCreationService.create(chargeTransactionRequest);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId())
                    .withLoanId(command.getLoanId())
                    .withProductId(loanCharge.getLoan().productId())
                    .build();
            // return this.writePlatformService.addLoanCharge(command.getLoanId(), command);
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.loan.charge.duplicate.externalId",
                    "Loan Charge with externalId `" + externalId + "` already exists", "externalId", externalId);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.loan.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("Error occured.", dve);
    }

}
