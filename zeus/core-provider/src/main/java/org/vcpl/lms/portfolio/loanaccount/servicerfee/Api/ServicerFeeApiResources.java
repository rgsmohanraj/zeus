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
package org.vcpl.lms.portfolio.loanaccount.servicerfee.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.RetrieveServicerFeeChargeData;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.ServicerFeeData;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeReadPlatformService;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeReadPlatformServiceimp;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Objects;

@Path("/loans/servicerfee")
@Component
@Scope("singleton")
public class ServicerFeeApiResources {


    private final String resourceNameForPermissions = "SERVICERFEE";
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<ServicerFeeData> toApiJsonSerializer;
    private final PlatformSecurityContext context;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ServicerFeeReadPlatformService servicerFeeReadPlatformService;

    private final LoanProductRepository loanProductRepository;

    @Autowired
    public ServicerFeeApiResources(PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, DefaultToApiJsonSerializer<ServicerFeeData> toApiJsonSerializer, PlatformSecurityContext context, ApiRequestParameterHelper apiRequestParameterHelper, ServicerFeeReadPlatformServiceimp servicerFeeReadPlatformServiceimp, LoanProductRepository loanProductRepository) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.servicerFeeReadPlatformService = servicerFeeReadPlatformServiceimp;
        this.loanProductRepository = loanProductRepository;
    }



    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ServicerFeeApiResourceSwagger.PostServicerFeeRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ServicerFeeApiResourceSwagger.PostServicerFeeResponse.class))) })
    public String loanReassignment(final String apiRequestBodyAsJson) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createServicerFeeConfig().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{servicerFeeConfigId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ServicerFeeApiResourceSwagger.PutServicerFeeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ServicerFeeApiResourceSwagger.PutServicerFeeIdResponse.class))) })
    public String updateServicerFeeConfig(@Parameter(hidden = true) final String apiRequestBodyAsJson, @PathParam("servicerFeeConfigId") Long servicerFeeConfigId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateServicerFeeConfig(servicerFeeConfigId).withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }


    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a servicerFeeConfigId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServicerFeeApiResourceSwagger.GetServicerFeeResponse.class)))) })
    public String retrieveServicerFeeConfig(@PathParam("productId")final Long productId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        LoanProduct loanProduct = loanProductRepository.getReferenceById(productId);
        ServicerFeeData servicerFeeData =new ServicerFeeData();
        if(Objects.nonNull(loanProduct.getServicerFeeConfig())) {
             servicerFeeData = this.servicerFeeReadPlatformService.retrieveServicerFeeConfigData(productId);
            List<RetrieveServicerFeeChargeData> servicerFeeChargeData = this.servicerFeeReadPlatformService.retrieveServicerFee(servicerFeeData.getId());
            if (!servicerFeeChargeData.isEmpty()) {
                servicerFeeData.setServicerFeeChargeData(servicerFeeChargeData);
            }
        }
        return this.toApiJsonSerializer.serialize(settings, servicerFeeData);

    }
}
