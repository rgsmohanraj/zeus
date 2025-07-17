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
package org.vcpl.lms.notification.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.ToApiJsonSerializer;
import org.vcpl.lms.infrastructure.core.service.Page;
import org.vcpl.lms.infrastructure.core.service.SearchParameters;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.notification.data.NotificationData;
import org.vcpl.lms.notification.service.NotificationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/notifications")
@Component
@Scope("singleton")

@Tag(name = "Notification", description = "")
public class NotificationApiResource {

    private final PlatformSecurityContext context;
    private final NotificationReadPlatformService notificationReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ToApiJsonSerializer<NotificationData> toApiJsonSerializer;

    @Autowired
    public NotificationApiResource(PlatformSecurityContext context, NotificationReadPlatformService notificationReadPlatformService,
            ApiRequestParameterHelper apiRequestParameterHelper, ToApiJsonSerializer<NotificationData> toApiJsonSerializer) {
        this.context = context;
        this.notificationReadPlatformService = notificationReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getAllNotifications(@Context final UriInfo uriInfo, @QueryParam("orderBy") final String orderBy,
            @QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset,
            @QueryParam("sortOrder") final String sortOrder, @QueryParam("isRead") final boolean isRead) {

        this.context.authenticatedUser();
        final Page<NotificationData> notificationData;
        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);
        if (!isRead) {
            notificationData = this.notificationReadPlatformService.getAllUnreadNotifications(searchParameters);
        } else {
            notificationData = this.notificationReadPlatformService.getAllNotifications(searchParameters);
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, notificationData);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void update() {
        this.context.authenticatedUser();
        this.notificationReadPlatformService.updateNotificationReadStatus();
    }
}
