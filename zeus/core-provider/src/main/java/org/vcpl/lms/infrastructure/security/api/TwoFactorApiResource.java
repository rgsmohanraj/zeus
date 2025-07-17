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
package org.vcpl.lms.infrastructure.security.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.core.serialization.ToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.data.AccessTokenData;
import org.vcpl.lms.infrastructure.security.data.OTPDeliveryMethod;
import org.vcpl.lms.infrastructure.security.data.OTPMetadata;
import org.vcpl.lms.infrastructure.security.data.OTPRequest;
import org.vcpl.lms.infrastructure.security.domain.TFAccessToken;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.infrastructure.security.service.TwoFactorService;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/twofactor")
@Component
@ConditionalOnProperty("lms.security.2fa.enabled")
@Scope("singleton")
@Tag(name = "Two Factor", description = "")
public class TwoFactorApiResource {

    private final ToApiJsonSerializer<OTPMetadata> otpRequestSerializer;
    private final ToApiJsonSerializer<OTPDeliveryMethod> otpDeliveryMethodSerializer;
    private final ToApiJsonSerializer<AccessTokenData> accessTokenSerializer;
    private final DefaultToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer;

    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final TwoFactorService twoFactorService;

    @Autowired
    public TwoFactorApiResource(ToApiJsonSerializer<OTPMetadata> otpRequestSerializer,
            ToApiJsonSerializer<OTPDeliveryMethod> otpDeliveryMethodSerializer, ToApiJsonSerializer<AccessTokenData> accessTokenSerializer,
            DefaultToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer, PlatformSecurityContext context,
            PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, TwoFactorService twoFactorService) {
        this.otpRequestSerializer = otpRequestSerializer;
        this.otpDeliveryMethodSerializer = otpDeliveryMethodSerializer;
        this.accessTokenSerializer = accessTokenSerializer;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.context = context;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.twoFactorService = twoFactorService;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String getOTPDeliveryMethods(@Context final UriInfo uriInfo) {
        AppUser user = context.authenticatedUser();

        List<OTPDeliveryMethod> otpDeliveryMethods = twoFactorService.getDeliveryMethodsForUser(user);
        return this.otpDeliveryMethodSerializer.serialize(otpDeliveryMethods);
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String requestToken(@QueryParam("deliveryMethod") final String deliveryMethod,
            @QueryParam("extendedToken") @DefaultValue("false") boolean extendedAccessToken, @Context final UriInfo uriInfo) {
        final AppUser user = context.authenticatedUser();

        final OTPRequest request = twoFactorService.createNewOTPToken(user, deliveryMethod, extendedAccessToken);
        return this.otpRequestSerializer.serialize(request.getMetadata());
    }

    @Path("validate")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String validate(@QueryParam("token") final String token) {
        final AppUser user = context.authenticatedUser();

        TFAccessToken accessToken = twoFactorService.createAccessTokenFromOTP(user, token);

        return accessTokenSerializer.serialize(accessToken.toTokenData());
    }

    @Path("invalidate")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateConfiguration(final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().invalidateTwoFactorAccessToken().withJson(apiRequestBodyAsJson)
                .build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
