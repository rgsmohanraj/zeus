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
package org.vcpl.lms.portfolio.savings.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.core.service.Page;
import org.vcpl.lms.infrastructure.core.service.SearchParameters;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.savings.SavingsApiConstants;
import org.vcpl.lms.portfolio.savings.data.DepositAccountOnHoldTransactionData;
import org.vcpl.lms.portfolio.savings.service.DepositAccountOnHoldTransactionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/savingsaccounts/{savingsId}/onholdtransactions")
@Component
@Scope("singleton")
@Tag(name = "Deposit Account On Hold Fund Transactions", description = "")
public class DepositAccountOnHoldFundTransactionsApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<DepositAccountOnHoldTransactionData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DepositAccountOnHoldTransactionReadPlatformService depositAccountOnHoldTransactionReadPlatformService;

    @Autowired
    public DepositAccountOnHoldFundTransactionsApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<DepositAccountOnHoldTransactionData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final DepositAccountOnHoldTransactionReadPlatformService depositAccountOnHoldTransactionReadPlatformService) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.depositAccountOnHoldTransactionReadPlatformService = depositAccountOnHoldTransactionReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@PathParam("savingsId") final Long savingsId, @QueryParam("guarantorFundingId") final Long guarantorFundingId,
            @Context final UriInfo uriInfo, @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("orderBy") final String orderBy, @QueryParam("sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);

        final Page<DepositAccountOnHoldTransactionData> transfers = this.depositAccountOnHoldTransactionReadPlatformService
                .retriveAll(savingsId, guarantorFundingId, searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, transfers,
                SavingsApiSetConstants.SAVINGS_ACCOUNT_ON_HOLD_RESPONSE_DATA_PARAMETERS);
    }

}
