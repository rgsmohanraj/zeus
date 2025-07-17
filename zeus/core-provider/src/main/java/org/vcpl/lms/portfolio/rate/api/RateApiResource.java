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

package org.vcpl.lms.portfolio.rate.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import jakarta.ws.rs.Consumes;
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
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.rate.data.RateData;
import org.vcpl.lms.portfolio.rate.service.RateReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Bowpi GT Created by Jose on 19/07/2017.
 */

@Path("/rates")
@Component
@Scope("singleton")
@Tag(name = "Rate", description = "")
public class RateApiResource {

    private final Set<String> responseDataParameters = new HashSet<>(Arrays.asList("id", "name", "percentage", "productApply", "active"));
    private final String resourceNameForPermissions = "RATE";
    private final PlatformSecurityContext context;
    private final RateReadService readPlatformService;
    private final DefaultToApiJsonSerializer<RateData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public RateApiResource(final PlatformSecurityContext context, final RateReadService rateReadService,
            final DefaultToApiJsonSerializer<RateData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = rateReadService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("{rateId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveRate(@PathParam("rateId") Long rateId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final RateData rate = this.readPlatformService.retrieveOne(rateId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, rate, this.responseDataParameters);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createRate(final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createRate().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getAllRates(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        Collection<RateData> rates = this.readPlatformService.retrieveAllRates();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, rates, this.responseDataParameters);
    }

    @PUT
    @Path("{rateId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateRate(@PathParam("rateId") Long rateId, final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateRate(rateId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
