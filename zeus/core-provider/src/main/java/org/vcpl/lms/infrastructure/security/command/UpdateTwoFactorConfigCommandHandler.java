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
package org.vcpl.lms.infrastructure.security.command;

import java.util.Map;
import org.vcpl.lms.commands.annotation.CommandType;
import org.vcpl.lms.commands.handler.NewCommandSourceHandler;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.security.data.TwoFactorConfigurationValidator;
import org.vcpl.lms.infrastructure.security.service.TwoFactorConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "TWOFACTOR_CONFIGURATION", action = "UPDATE")
@ConditionalOnProperty("lms.security.2fa.enabled")
public class UpdateTwoFactorConfigCommandHandler implements NewCommandSourceHandler {

    private final TwoFactorConfigurationService configurationService;
    private final TwoFactorConfigurationValidator dataValidator;

    @Autowired
    public UpdateTwoFactorConfigCommandHandler(TwoFactorConfigurationService configurationService,
            TwoFactorConfigurationValidator dataValidator) {
        this.configurationService = configurationService;
        this.dataValidator = dataValidator;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        this.dataValidator.validateForUpdate(command.json());
        final Map<String, Object> changes = configurationService.update(command);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).build();
    }
}
