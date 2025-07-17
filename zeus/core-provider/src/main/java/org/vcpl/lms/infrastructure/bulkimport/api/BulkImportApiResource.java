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
package org.vcpl.lms.infrastructure.bulkimport.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Collection;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.vcpl.lms.infrastructure.bulkimport.data.GlobalEntityType;
import org.vcpl.lms.infrastructure.bulkimport.data.ImportData;
import org.vcpl.lms.infrastructure.bulkimport.exceptions.ImportTypeNotFoundException;
import org.vcpl.lms.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.documentmanagement.data.DocumentData;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/imports")
@Component
@Scope("singleton")
@Tag(name = "Bulk Import", description = "")
public class BulkImportApiResource {

    private final String resourceNameForPermissions = "IMPORT";

    private final PlatformSecurityContext context;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final DefaultToApiJsonSerializer<ImportData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public BulkImportApiResource(final PlatformSecurityContext context, final BulkImportWorkbookService bulkImportWorkbookService,
            final DefaultToApiJsonSerializer<ImportData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.context = context;
        this.bulkImportWorkbookService = bulkImportWorkbookService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveImportDocuments(@Context final UriInfo uriInfo, @QueryParam("entityType") final String entityType) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        Collection<ImportData> importData = new ArrayList<>();
        if (entityType.equals(GlobalEntityType.CLIENT.getCode())) {
            final Collection<ImportData> importForClientEntity = this.bulkImportWorkbookService.getImports(GlobalEntityType.CLIENTS_ENTTTY);
            final Collection<ImportData> importForClientPerson = this.bulkImportWorkbookService.getImports(GlobalEntityType.CLIENTS_PERSON);
            if (importForClientEntity != null) {
                importData.addAll(importForClientEntity);
            }
            if (importForClientPerson != null) {
                importData.addAll(importForClientPerson);
            }
        } else {
            final GlobalEntityType type = GlobalEntityType.fromCode(entityType);
            if (type == null) {
                throw new ImportTypeNotFoundException(entityType);
            }
            importData = this.bulkImportWorkbookService.getImports(type);
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, importData);
    }

    @GET
    @Path("getOutputTemplateLocation")
    public String retriveOutputTemplateLocation(@QueryParam("importDocumentId") final String importDocumentId) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final DocumentData documentData = this.bulkImportWorkbookService.getOutputTemplateLocation(importDocumentId);
        return this.toApiJsonSerializer.serialize(documentData.fileLocation());
    }

    @GET
    @Path("downloadOutputTemplate")
    @Produces("application/vnd.ms-excel")
    public Response getOutputTemplate(@QueryParam("importDocumentId") final String importDocumentId) {
        return bulkImportWorkbookService.getOutputTemplate(importDocumentId);
    }

}
