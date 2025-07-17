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
package org.vcpl.lms.interoperation.service;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.interoperation.data.InteropAccountData;
import org.vcpl.lms.interoperation.data.InteropIdentifierAccountResponseData;
import org.vcpl.lms.interoperation.data.InteropIdentifiersResponseData;
import org.vcpl.lms.interoperation.data.InteropKycResponseData;
import org.vcpl.lms.interoperation.data.InteropQuoteResponseData;
import org.vcpl.lms.interoperation.data.InteropTransactionRequestResponseData;
import org.vcpl.lms.interoperation.data.InteropTransactionsData;
import org.vcpl.lms.interoperation.data.InteropTransferResponseData;
import org.vcpl.lms.interoperation.domain.InteropIdentifierType;

public interface InteropService {

    @NotNull
    InteropIdentifiersResponseData getAccountIdentifiers(@NotNull String accountId);

    @NotNull
    InteropAccountData getAccountDetails(@NotNull String accountId);

    @NotNull
    InteropTransactionsData getAccountTransactions(@NotNull String accountId, boolean debit, boolean credit, LocalDateTime transactionsFrom,
            LocalDateTime transactionsTo);

    @NotNull
    InteropIdentifierAccountResponseData getAccountByIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue,
            String subIdOrType);

    @NotNull
    InteropIdentifierAccountResponseData registerAccountIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue,
            String subIdOrType, @NotNull JsonCommand command);

    @NotNull
    InteropIdentifierAccountResponseData deleteAccountIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue,
            String subIdOrType);

    InteropTransactionRequestResponseData getTransactionRequest(@NotNull String transactionCode, @NotNull String requestCode);

    @NotNull
    InteropTransactionRequestResponseData createTransactionRequest(@NotNull JsonCommand command);

    InteropQuoteResponseData getQuote(@NotNull String transactionCode, @NotNull String quoteCode);

    @NotNull
    InteropQuoteResponseData createQuote(@NotNull JsonCommand command);

    InteropTransferResponseData getTransfer(@NotNull String transactionCode, @NotNull String transferCode);

    @NotNull
    InteropTransferResponseData prepareTransfer(@NotNull JsonCommand command);

    @NotNull
    InteropTransferResponseData commitTransfer(@NotNull JsonCommand command);

    @NotNull
    InteropTransferResponseData releaseTransfer(@NotNull JsonCommand command);

    @NotNull
    InteropKycResponseData getKyc(@NotNull String accountId);

    @NotNull
    String disburseLoan(@NotNull String accountId, String apiRequestBodyAsJson);
}
