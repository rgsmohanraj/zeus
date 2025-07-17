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
package org.vcpl.lms.portfolio.partner.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.bulkimport.data.GlobalEntityType;
import org.vcpl.lms.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.vcpl.lms.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.UploadRequest;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.core.service.SearchParameters;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.partner.data.PartnerData;
import org.vcpl.lms.portfolio.partner.service.PartnerReadPlatformService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/partners")
@Component
@Scope("singleton")
@Tag(name = "Partners", description = "Partners are used to model an MFIs structure. A hierarchical representation of partners is supported. There will always be at least one partner (which represents the MFI or an MFIs head partner). All subsequent partners added must have a parent partner.")
public class PartnersApiResource {

    /**
     * The set of parameters that are supported in response for {@link PartnerData}.
     */
    private final Set<String> responseDataParameters = new HashSet<>(Arrays.asList("id", "partnerName", "partnerCompanyRegistrationDate", "source", "panCard", "cinNumber",
            "address1", "address2", "city", "state", "pincode", "country", "constitution",
            "keyPersons", "industry", "sector", "subSector", "gstNumber", "gstRegistration",
            "partnerType", "beneficiaryName", "beneficiaryAccountNumber", "ifscCode",
            "micrCode", "swiftCode", "branch", "modelLimit", "approvedLimit", "pilotLimit",
            "partnerFloatLimit", "balanceLimit", "agreementStartDate", "agreementExpiryDate",
            "underlyingAssets", "security", "fldgCalculationOn"));

    private final String resourceNameForPermissions = "PARTNER";

    private final PlatformSecurityContext context;
    private final PartnerReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<PartnerData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;

    @Autowired
    public PartnersApiResource(final PlatformSecurityContext context, final PartnerReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<PartnerData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.bulkImportWorkbookService = bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService = bulkImportWorkbookPopulatorService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Partners", description = "Example Requests:\n" + "\n" + "partners\n" + "\n" + "\n"
            + "partners?fields=id,name,openingDate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PartnersApiResourceSwagger.GetPartnersResponse.class)))) })
    public String retrievePartners(@Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam("includeAllPartners") @Parameter(description = "includeAllPartners") final boolean onlyManualEntries,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final SearchParameters searchParameters = SearchParameters.forPartners(orderBy, sortOrder);

        final Collection<PartnerData> partners = this.readPlatformService.retrieveAllPartners(onlyManualEntries, searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, partners, this.responseDataParameters);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Partner Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "partners/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PartnersApiResourceSwagger.GetPartnersTemplateResponse.class))) })
    public String retrievePartnerTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        PartnerData partner = this.readPlatformService.retrieveNewPartnerTemplate();

//        final Collection<PartnerData> allowedParents = this.readPlatformService.retrieveAllPartnersForDropdown();
//        partner = PartnerData.appendedTemplate(partner);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, partner, this.responseDataParameters);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Partner", description = "Mandatory Fields\n" + "partnerName, approvedLimit")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = PartnersApiResourceSwagger.PostPartnersRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PartnersApiResourceSwagger.PostPartnersResponse.class))) })
    public String createPartner(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createPartner() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{partnerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Partner", description = "Example Requests:\n" + "\n" + "partners/1\n" + "\n" + "\n"
            + "partners/1?template=true\n" + "\n" + "\n" + "partners/1?fields=id,name,parentName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PartnersApiResourceSwagger.GetPartnersResponse.class))) })
    public String retreivePartner(@PathParam("partnerId") @Parameter(description = "partnerId") final Long partnerId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        PartnerData partner = this.readPlatformService.retrievePartner(partnerId);
        if (settings.isTemplate()) {

            final PartnerData templateData = this.readPlatformService.retrieveNewPartnerTemplate();

            partner = PartnerData.appendedTemplate(partner, templateData);
        }

        return this.toApiJsonSerializer.serialize(settings, partner, this.responseDataParameters);
    }

    @PUT
    @Path("{partnerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Partner", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = PartnersApiResourceSwagger.PutPartnersPartnerIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PartnersApiResourceSwagger.PutPartnersPartnerIdResponse.class))) })
    public String updatePartner(@PathParam("partnerId") @Parameter(description = "partnerId") final Long partnerId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updatePartner(partnerId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getPartnerTemplate(@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.PARTNERS.toString(), null, null, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload partner template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postPartnerTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.PARTNERS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}
