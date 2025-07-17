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
package org.vcpl.lms.mix.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
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
import org.vcpl.lms.infrastructure.core.serialization.ToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.mix.data.MixTaxonomyMappingData;
import org.vcpl.lms.mix.service.MixTaxonomyMappingReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/mixmapping")
@Component
@Scope("singleton")

@Tag(name = "Mix Mapping", description = "")
public class MixTaxonomyMappingApiResource {

    private final Set<String> responseDataParameters = new HashSet<>(Arrays.asList("identifier", "config"));

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<MixTaxonomyMappingData> toApiJsonSerializer;
    private final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public MixTaxonomyMappingApiResource(final PlatformSecurityContext context,
            final ToApiJsonSerializer<MixTaxonomyMappingData> toApiJsonSerializer,
            final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {

        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.readTaxonomyMappingService = readTaxonomyMappingService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTaxonomyMapping(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser();
        final MixTaxonomyMappingData mappingData = this.readTaxonomyMappingService.retrieveTaxonomyMapping();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, mappingData, this.responseDataParameters);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateTaxonomyMapping(final String jsonRequestBody) {
        // TODO support multiple configuration file loading
        final Long mappingId = (long) 1;
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTaxonomyMapping(mappingId).withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
