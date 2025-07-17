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
package org.vcpl.lms.portfolio.collateralmanagement.service;

import java.math.BigDecimal;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.vcpl.lms.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCollateralManagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanCollateralManagementWritePlatformServiceImpl implements LoanCollateralManagementWritePlatformService {

    private final LoanCollateralManagementRepository loanCollateralManagementRepository;
    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;

    @Autowired
    public LoanCollateralManagementWritePlatformServiceImpl(final LoanCollateralManagementRepository loanCollateralManagementRepository,
            final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper) {
        this.loanCollateralManagementRepository = loanCollateralManagementRepository;
        this.clientCollateralManagementRepositoryWrapper = clientCollateralManagementRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteLoanCollateral(JsonCommand command) {
        final Long id = command.entityId();
        final LoanCollateralManagement loanCollateralManagement = this.loanCollateralManagementRepository.findById(id).orElseThrow();
        ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();
        BigDecimal loanQuantity = loanCollateralManagement.getQuantity();
        BigDecimal clientQuantity = clientCollateralManagement.getQuantity();
        clientCollateralManagement.updateQuantity(clientQuantity.add(loanQuantity));
        this.clientCollateralManagementRepositoryWrapper.saveAndFlush(clientCollateralManagement);
        this.loanCollateralManagementRepository.deleteById(id);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(id).withLoanId(command.getLoanId())
                .build();
    }
}
