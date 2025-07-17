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

package org.vcpl.lms.portfolio.self.spm.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.spm.api.SpmApiResource;
import org.vcpl.lms.spm.data.SurveyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Path("/self/surveys")
@Component
@Scope("singleton")

@Tag(name = "Self Spm", description = "")
public class SelfSpmApiResource {

    private final PlatformSecurityContext securityContext;
    private final SpmApiResource spmApiResource;

    @Autowired
    public SelfSpmApiResource(final PlatformSecurityContext securityContext, final SpmApiResource spmApiResource) {
        this.securityContext = securityContext;
        this.spmApiResource = spmApiResource;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public List<SurveyData> fetchAllSurveys() {
        securityContext.authenticatedUser();
        final Boolean isActive = true;
        return this.spmApiResource.fetchAllSurveys(isActive);
    }

}
