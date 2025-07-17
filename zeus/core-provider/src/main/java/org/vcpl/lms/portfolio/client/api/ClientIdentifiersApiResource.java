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
package org.vcpl.lms.portfolio.client.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.client.data.ClientData;
import org.vcpl.lms.portfolio.client.data.ClientIdentifierData;
import org.vcpl.lms.portfolio.client.exception.DuplicateClientIdentifierException;
import org.vcpl.lms.portfolio.client.service.ClientIdentifierReadPlatformService;
import org.vcpl.lms.portfolio.client.service.ClientReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/{clientId}/identifiers")
@Component
@Scope("singleton")
@Tag(name = "Client Identifier", description = "Client Identifiers refer to documents that are used to uniquely identify a customer\n"
        + "Ex: Drivers License, Passport, Ration card etc ")
public class ClientIdentifiersApiResource {

    private static final Set<String> CLIENT_IDENTIFIER_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "clientId", "documentType", "documentKey", "description", "allowedDocumentTypes"));

    private final String resourceNameForPermissions = "CLIENTIDENTIFIER";

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ClientIdentifierReadPlatformService clientIdentifierReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientIdentifierData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientIdentifiersApiResource(final PlatformSecurityContext context, final ClientReadPlatformService readPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final DefaultToApiJsonSerializer<ClientIdentifierData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ClientIdentifierReadPlatformService clientIdentifierReadPlatformService) {
        this.context = context;
        this.clientReadPlatformService = readPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.clientIdentifierReadPlatformService = clientIdentifierReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all Identifiers for a Client", description = "Example Requests:\n" + "clients/1/identifiers\n" + "\n" + "\n"
            + "clients/1/identifiers?fields=documentKey,documentType,description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientIdentifiersApiResourceSwagger.GetClientsClientIdIdentifiersResponse.class)))) })
    public String retrieveAllClientIdentifiers(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ClientIdentifierData> clientIdentifiers = this.clientIdentifierReadPlatformService
                .retrieveClientIdentifiers(clientId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientIdentifiers, CLIENT_IDENTIFIER_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Client Identifier Details Template", description = "This is a convenience resource useful for building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + " Field Defaults\n" + " Allowed description Lists\n" + "\n\nExample Request:\n" + "clients/1/identifiers/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientIdentifiersApiResourceSwagger.GetClientsClientIdIdentifiersTemplateResponse.class))) })
    public String newClientIdentifierDetails(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService.retrieveCodeValuesByCode("Customer Identifier");
        final ClientIdentifierData clientIdentifierData = ClientIdentifierData.template(codeValues);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientIdentifierData, CLIENT_IDENTIFIER_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Identifier for a Client", description = "Mandatory Fields\n" + "documentKey, documentTypeId ")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientIdentifiersApiResourceSwagger.PostClientsClientIdIdentifiersRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientIdentifiersApiResourceSwagger.PostClientsClientIdIdentifiersResponse.class))) })
    public String createClientIdentifier(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        try {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().createClientIdentifier(clientId)
                    .withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            return this.toApiJsonSerializer.serialize(result);
        } catch (final DuplicateClientIdentifierException e) {
            DuplicateClientIdentifierException rethrowas = e;
            if (e.getDocumentTypeId() != null) {
                // need to fetch client info
                final ClientData clientInfo = this.clientReadPlatformService.retrieveClientByIdentifier(e.getDocumentTypeId(),
                        e.getIdentifierKey());
                rethrowas = new DuplicateClientIdentifierException(clientInfo.displayName(), clientInfo.officeName(), e.getIdentifierType(),
                        e.getIdentifierKey());
            }
            throw rethrowas;
        }
    }

    @GET
    @Path("{identifierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Identifier", description = "Example Requests:\n" + "clients/1/identifier/2\n" + "\n" + "\n"
            + "clients/1/identifier/2?template=true\n" + "\n" + "clients/1/identifiers/2?fields=documentKey,documentType,description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientIdentifiersApiResourceSwagger.GetClientsClientIdIdentifiersResponse.class))) })
    public String retrieveClientIdentifiers(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("identifierId") @Parameter(description = "identifierId") final Long clientIdentifierId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ClientIdentifierData clientIdentifierData = this.clientIdentifierReadPlatformService.retrieveClientIdentifier(clientId,
                clientIdentifierId);
        if (settings.isTemplate()) {
            final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService.retrieveCodeValuesByCode("Customer Identifier");
            clientIdentifierData = ClientIdentifierData.template(clientIdentifierData, codeValues);
        }

        return this.toApiJsonSerializer.serialize(settings, clientIdentifierData, CLIENT_IDENTIFIER_DATA_PARAMETERS);
    }

    @PUT
    @Path("{identifierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Client Identifier", description = "Updates a Client Identifier")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientIdentifiersApiResourceSwagger.PutClientsClientIdIdentifiersIdentifierIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientIdentifiersApiResourceSwagger.PutClientsClientIdIdentifiersIdentifierIdResponse.class))) })
    public String updateClientIdentifer(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("identifierId") @Parameter(description = "identifierId") final Long clientIdentifierId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        try {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().updateClientIdentifier(clientId, clientIdentifierId)
                    .withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            return this.toApiJsonSerializer.serialize(result);
        } catch (final DuplicateClientIdentifierException e) {
            DuplicateClientIdentifierException reThrowAs = e;
            if (e.getDocumentTypeId() != null) {
                final ClientData clientInfo = this.clientReadPlatformService.retrieveClientByIdentifier(e.getDocumentTypeId(),
                        e.getIdentifierKey());
                reThrowAs = new DuplicateClientIdentifierException(clientInfo.displayName(), clientInfo.officeName(), e.getIdentifierType(),
                        e.getIdentifierKey());
            }
            throw reThrowAs;
        }
    }

    @DELETE
    @Path("{identifierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Client Identifier", description = "Deletes a Client Identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientIdentifiersApiResourceSwagger.DeleteClientsClientIdIdentifiersIdentifierIdResponse.class))) })
    public String deleteClientIdentifier(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("identifierId") @Parameter(description = "identifierId") final Long clientIdentifierId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClientIdentifier(clientId, clientIdentifierId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
