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
package org.vcpl.lms.portfolio.shareproducts.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.core.service.Page;
import org.vcpl.lms.infrastructure.core.service.SearchParameters;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.shareaccounts.data.ShareAccountDividendData;
import org.vcpl.lms.portfolio.shareaccounts.service.ShareAccountDividendReadPlatformService;
import org.vcpl.lms.portfolio.shareproducts.data.ShareProductDividendPayOutData;
import org.vcpl.lms.portfolio.shareproducts.service.ShareProductDividendReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/shareproduct/{productId}/dividend")
@Component
@Scope("singleton")

@Tag(name = "Self Dividend", description = "")
public class ShareDividendApiResource {

    private final DefaultToApiJsonSerializer<ShareProductDividendPayOutData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<ShareAccountDividendData> toApiAccountDetailJsonSerializer;
    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ShareAccountDividendReadPlatformService shareAccountDividendReadPlatformService;
    private final ShareProductDividendReadPlatformService shareProductDividendReadPlatformService;
    private final String resourceNameForPermissions = "DIVIDEND_SHAREPRODUCT";

    @Autowired
    public ShareDividendApiResource(final DefaultToApiJsonSerializer<ShareProductDividendPayOutData> toApiJsonSerializer,
            final PlatformSecurityContext platformSecurityContext,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<ShareAccountDividendData> toApiDividendsJsonSerializer,
            final ShareAccountDividendReadPlatformService shareAccountDividendReadPlatformService,
            final ShareProductDividendReadPlatformService shareProductDividendReadPlatformService) {
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.platformSecurityContext = platformSecurityContext;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiAccountDetailJsonSerializer = toApiDividendsJsonSerializer;
        this.shareAccountDividendReadPlatformService = shareAccountDividendReadPlatformService;
        this.shareProductDividendReadPlatformService = shareProductDividendReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@PathParam("productId") final Long productId, @QueryParam("offset") final Integer offset,
            @QueryParam("limit") final Integer limit, @QueryParam("orderBy") final String orderBy,
            @QueryParam("sortOrder") final String sortOrder, @QueryParam("status") final Integer status) {

        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);
        Page<ShareProductDividendPayOutData> dividendPayoutDetails = this.shareProductDividendReadPlatformService.retriveAll(productId,
                status, searchParameters);
        return this.toApiJsonSerializer.serialize(dividendPayoutDetails);
    }

    @GET
    @Path("{dividendId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveDividendDetails(@PathParam("dividendId") final Long dividendId, @QueryParam("offset") final Integer offset,
            @QueryParam("limit") final Integer limit, @QueryParam("orderBy") final String orderBy,
            @QueryParam("sortOrder") final String sortOrder, @QueryParam("accountNo") final String accountNo,
            @PathParam("productId") final Long productId) {

        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final SearchParameters searchParameters = SearchParameters.forPaginationAndAccountNumberSearch(offset, limit, orderBy, sortOrder,
                accountNo);
        Page<ShareAccountDividendData> dividendDetails = this.shareAccountDividendReadPlatformService.retriveAll(dividendId,
                searchParameters);
        return this.toApiAccountDetailJsonSerializer.serialize(dividendDetails);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createDividendDetail(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createShareProductDividendPayoutCommand(productId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    @PUT
    @Path("{dividendId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateDividendDetail(@PathParam("productId") final Long productId, @PathParam("dividendId") final Long dividendId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        if (is(commandParam, "approve")) {
            commandWrapper = new CommandWrapperBuilder().approveShareProductDividendPayoutCommand(productId, dividendId)
                    .withJson(apiRequestBodyAsJson).build();
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    @DELETE
    @Path("{dividendId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteDividendDetail(@PathParam("productId") final Long productId, @PathParam("dividendId") final Long dividendId) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().deleteShareProductDividendPayoutCommand(productId, dividendId)
                .build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
