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

import org.vcpl.lms.commands.annotation.CommandType;
import org.vcpl.lms.commands.handler.NewCommandSourceHandler;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeTransactionRequest;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransaction;
import org.vcpl.lms.portfolio.loanaccount.serialization.ChargeCollectionApiJsonValidator;
import org.vcpl.lms.portfolio.loanaccount.service.LoanChargePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "LOANCHARGE", action = "PAY")
public class PayLoanChargeCommandHandler implements NewCommandSourceHandler {

    private final ChargeCollectionApiJsonValidator validator;
    private final LoanChargePaymentService loanChargePaymentService;
    @Autowired
    public PayLoanChargeCommandHandler(final LoanChargePaymentService loanChargePaymentService,final ChargeCollectionApiJsonValidator validator) {
        this.loanChargePaymentService = loanChargePaymentService;
        this.validator = validator;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command){
        ChargeTransactionRequest chargeTransactionRequest = validator.chargeTransaction(command.json(), false);
        LoanTransaction transaction = this.loanChargePaymentService.pay(chargeTransactionRequest);
        return CommandProcessingResult.commandOnlyResult(transaction.getId());
    }
}
