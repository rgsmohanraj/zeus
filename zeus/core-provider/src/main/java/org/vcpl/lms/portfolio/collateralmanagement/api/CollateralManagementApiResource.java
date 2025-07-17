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
package org.vcpl.lms.portfolio.collateralmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.service.CurrencyReadPlatformService;
import org.vcpl.lms.portfolio.collateralmanagement.data.CollateralManagementData;
import org.vcpl.lms.portfolio.collateralmanagement.service.ClientCollateralManagementReadPlatformService;
import org.vcpl.lms.portfolio.collateralmanagement.service.CollateralManagementReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/collateral-management")
@Component
@Scope("singleton")
@Tag(name = "Collateral Management", description = "Collateral Management is for managing collateral operations")
public class CollateralManagementApiResource {

    private final DefaultToApiJsonSerializer<CollateralManagementData> apiJsonSerializerService;
    private final DefaultToApiJsonSerializer<CurrencyData> apiJsonSerializerServiceForCurrency;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final CollateralManagementReadPlatformService collateralManagementReadPlatformService;
    private final String collateralReadPermission = "COLLATERAL_PRODUCT";
    private final ClientCollateralManagementReadPlatformService clientCollateralManagementReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;

    @Autowired
    public CollateralManagementApiResource(final DefaultToApiJsonSerializer<CollateralManagementData> apiJsonSerializerService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, final PlatformSecurityContext context,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final CollateralManagementReadPlatformService collateralManagementReadPlatformService,
            final ClientCollateralManagementReadPlatformService clientCollateralManagementReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService,
            final DefaultToApiJsonSerializer<CurrencyData> apiJsonSerializerServiceForCurrency) {
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.context = context;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.collateralManagementReadPlatformService = collateralManagementReadPlatformService;
        this.clientCollateralManagementReadPlatformService = clientCollateralManagementReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.apiJsonSerializerServiceForCurrency = apiJsonSerializerServiceForCurrency;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new collateral", description = "Collateral Creation")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CollateralManagementApiResourceSwagger.PostCollateralManagementProductRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollateralManagementApiResourceSwagger.PostCollateralManagementProductResponse.class))) })
    public String createCollateral(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().createCollateral().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.apiJsonSerializerService.serialize(commandProcessingResult);
    }

    @GET
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Collateral", description = "Fetch Collateral")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollateralManagementApiResourceSwagger.GetCollateralManagementsResponse.class))) })
    public String getCollateral(@PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser()
                .validateHasReadPermission(CollateralManagementJsonInputParams.COLLATERAL_PRODUCT_READ_PERMISSION.getValue());

        final CollateralManagementData collateralManagementData = this.collateralManagementReadPlatformService
                .getCollateralProduct(collateralId);

        return this.apiJsonSerializerService.serialize(collateralManagementData);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get All Collaterals", description = "Fetch all Collateral Products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CollateralManagementApiResourceSwagger.GetCollateralManagementsResponse.class)))) })
    public String getAllCollaterals(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser()
                .validateHasReadPermission(CollateralManagementJsonInputParams.COLLATERAL_PRODUCT_READ_PERMISSION.getValue());
        Collection<CollateralManagementData> collateralManagementDataList = this.collateralManagementReadPlatformService
                .getAllCollateralProducts();
        return this.apiJsonSerializerService.serialize(collateralManagementDataList);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Collateral Template", description = "Get Collateral Template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CollateralManagementApiResourceSwagger.GetCollateralProductTemplate.class)))) })
    public String getCollateralTemplate(@Context final UriInfo uriInfo) {
        Collection<CurrencyData> currencyDataCollection = this.currencyReadPlatformService.retrieveAllPlatformCurrencies();
        return this.apiJsonSerializerServiceForCurrency.serialize(currencyDataCollection);
    }

    @PUT
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Collateral", description = "Update Collateral")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CollateralManagementApiResourceSwagger.PutCollateralProductRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollateralManagementApiResourceSwagger.PutCollateralProductResponse.class))) })
    public String updateCollateral(@PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @Parameter(hidden = true) final String jsonBody) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().updateCollateralProduct(collateralId).withJson(jsonBody).build();

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);

        return this.apiJsonSerializerService.serialize(commandProcessingResult);

    }

    @DELETE
    @Path("{collateralId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Collateral", description = "Delete Collateral")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollateralManagementApiResourceSwagger.DeleteCollateralProductResponse.class))) })
    public String deleteCollateral(@PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCollateralProduct(collateralId).build();

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(commandProcessingResult);
    }

}
