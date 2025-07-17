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

package org.vcpl.lms.portfolio.self.shareaccounts.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.accounts.api.AccountsApiResource;
import org.vcpl.lms.portfolio.accounts.constants.ShareAccountApiConstants;
import org.vcpl.lms.portfolio.accounts.data.AccountData;
import org.vcpl.lms.portfolio.accounts.exceptions.ShareAccountNotFoundException;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.charge.service.ChargeReadPlatformService;
import org.vcpl.lms.portfolio.client.exception.ClientNotFoundException;
import org.vcpl.lms.portfolio.products.data.ProductData;
import org.vcpl.lms.portfolio.products.service.ProductReadPlatformService;
import org.vcpl.lms.portfolio.self.client.service.AppuserClientMapperReadService;
import org.vcpl.lms.portfolio.self.shareaccounts.data.SelfShareAccountsDataValidator;
import org.vcpl.lms.portfolio.self.shareaccounts.service.AppUserShareAccountsMapperReadPlatformService;
import org.vcpl.lms.portfolio.shareaccounts.data.ShareAccountData;
import org.vcpl.lms.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/shareaccounts")
@Component
@Scope("singleton")

@Tag(name = "Self Share Accounts", description = "")
public class SelfShareAccountsApiResource {

    private final PlatformSecurityContext context;
    private final AccountsApiResource accountsApiResource;
    private final ShareAccountReadPlatformService readPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    private final DefaultToApiJsonSerializer<AccountData> toApiJsonSerializer;
    private final AppuserClientMapperReadService appuserClientMapperReadService;
    private final SelfShareAccountsDataValidator selfShareAccountsDataValidator;
    private final ProductReadPlatformService productReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final AppUserShareAccountsMapperReadPlatformService appUserShareAccountsMapperReadPlatformService;

    @Autowired
    public SelfShareAccountsApiResource(final PlatformSecurityContext context, final AccountsApiResource accountsApiResource,
            final ShareAccountReadPlatformService readPlatformService, final DefaultToApiJsonSerializer<AccountData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final AppuserClientMapperReadService appuserClientMapperReadService,
            final SelfShareAccountsDataValidator selfShareAccountsDataValidator,
            final ProductReadPlatformService productReadPlatformService, final ChargeReadPlatformService chargeReadPlatformService,
            final AppUserShareAccountsMapperReadPlatformService appUserShareAccountsMapperReadPlatformService) {
        this.context = context;
        this.accountsApiResource = accountsApiResource;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.selfShareAccountsDataValidator = selfShareAccountsDataValidator;
        this.appuserClientMapperReadService = appuserClientMapperReadService;
        this.productReadPlatformService = productReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.appUserShareAccountsMapperReadPlatformService = appUserShareAccountsMapperReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Share Account Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "Field Defaults\n" + "\n" + "Allowed Value Lists\n" + "\n" + "\n" + "Arguments\n" + "\n" + "clientId:Integer mandatory\n"
            + "productId:Integer optionalIf entered, productId, productName and selectedProduct fields are returned.\n"
            + "Example Requests:\n" + "\n" + "self/shareaccounts/template?clientId=14\n" + "\n"
            + "self/shareaccounts/template?clientId=14&productId=3\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SelfShareAccountsApiResourceSwagger.GetShareAccountsClientIdProductIdResponse.class)))) })
    public String template(@QueryParam("clientId") @Parameter(name = "clientId") final Long clientId,
            @QueryParam("productId") @Parameter(name = "productId") final Long productId, @Context final UriInfo uriInfo) {

        validateAppuserClientsMapping(clientId);

        Collection<ProductData> productOptions = new ArrayList<ProductData>();
        if (productId != null) {
            final boolean includeTemplate = true;
            productOptions.add(productReadPlatformService.retrieveOne(productId, includeTemplate));
        } else {
            productOptions = productReadPlatformService.retrieveAllForLookup();
        }

        String clientName = null;

        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSharesApplicableCharges();
        final ShareAccountData accountData = new ShareAccountData(clientId, clientName, productOptions, chargeOptions);

        return this.toApiJsonSerializer.serialize(accountData);

    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Submit new share application", description = "Mandatory fields:\n" + "\n"
            + "clientId, productId, submittedDate, savingsAccountId, requestedShares, applicationDate\n" + "\n" + "\n" + "Optional Fields\n"
            + "\n" + "accountNo, externalId\n" + "\n" + "\n" + "Inherited from Product (if not provided)\n" + "\n"
            + "minimumActivePeriod, minimumActivePeriodFrequencyType, lockinPeriodFrequency, lockinPeriodFrequencyType.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SelfShareAccountsApiResourceSwagger.PostNewShareApplicationResponse.class)))) })
    public String createAccount(final String apiRequestBodyAsJson) {
        HashMap<String, Object> attr = this.selfShareAccountsDataValidator.validateShareAccountApplication(apiRequestBodyAsJson);
        final Long clientId = (Long) attr.get(ShareAccountApiConstants.clientid_paramname);
        validateAppuserClientsMapping(clientId);
        String accountType = ShareAccountApiConstants.shareEntityType;
        return this.accountsApiResource.createAccount(accountType, apiRequestBodyAsJson);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a share application/account", description = "\n" + "\n" + "\n" + "Example Requests:\n" + "\n"
            + "self/shareaccounts/12\n")
    // TODO actually write
    // SelfShareAccountsApiResourceSwagger.GetShareAccountResponse (it currently
    // does not exist)
    // @ApiResponses({@ApiResponse(responseCode = "200", description = "OK",
    // responseContainer
    // = "List", schema = @Schema(implementation =
    // SelfShareAccountsApiResourceSwagger.GetShareAccountResponse.class)})
    public String retrieveShareAccount(@PathParam("accountId") final Long accountId, @Context final UriInfo uriInfo) {
        validateAppuserShareAccountMapping(accountId);
        final boolean includeTemplate = false;
        AccountData accountData = this.readPlatformService.retrieveOne(accountId, includeTemplate);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, accountData, readPlatformService.getResponseDataParams());
    }

    private void validateAppuserShareAccountMapping(final Long accountId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isMapped = this.appUserShareAccountsMapperReadPlatformService.isShareAccountsMappedToUser(accountId, user.getId());
        if (!isMapped) {
            throw new ShareAccountNotFoundException(accountId);
        }
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appuserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }
}
