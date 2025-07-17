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
package org.vcpl.lms.portfolio.self.security.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.vcpl.lms.infrastructure.security.api.UserDetailsApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/userdetails")
@Component
@ConditionalOnProperty("lms.security.oauth.enabled")
@Scope("singleton")

@Tag(name = "Self User Details", description = "")
public class SelfUserDetailsApiResource {

    private final UserDetailsApiResource userDetailsApiResource;

    @Autowired
    public SelfUserDetailsApiResource(final UserDetailsApiResource userDetailsApiResource) {
        this.userDetailsApiResource = userDetailsApiResource;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Fetch authenticated user details", description = "Checks the Authentication and returns the set roles and permissions allowed\n\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfUserDetailsApiResourceSwagger.GetSelfUserDetailsResponse.class))) })
    public String fetchAuthenticatedUserData() {
        return this.userDetailsApiResource.fetchAuthenticatedUserData();
    }
}
