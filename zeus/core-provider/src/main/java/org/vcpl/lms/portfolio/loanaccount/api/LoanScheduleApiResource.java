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
package org.vcpl.lms.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashSet;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/schedule")
@Component
@Scope("singleton")
@Tag(name = "Loan Rescheduling", description = "Loan Term Variations provides the ability to change due dates, amounts and number of instalments before loan approval.")
public class LoanScheduleApiResource {

    private final String resourceNameForPermissions = "LOAN";
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<LoanScheduleData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public LoanScheduleApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<LoanScheduleData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final LoanScheduleCalculationPlatformService calculationPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.calculationPlatformService = calculationPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Calculate loan repayment schedule based on Loan term variations | Updates loan repayment schedule based on Loan term variations | Updates loan repayment schedule by removing Loan term variations", description = "Calculate loan repayment schedule based on Loan term variations:\n\n"
            + "Mandatory Fields: exceptions,locale,dateFormat\n\n" + "Updates loan repayment schedule based on Loan term variations:\n\n"
            + "Mandatory Fields: exceptions,locale,dateFormat\n\n" + "Updates loan repayment schedule by removing Loan term variations:\n\n"
            + "It updates the loan repayment schedule by removing Loan term variations\n\n"
            + "Showing request/response for 'Updates loan repayment schedule by removing Loan term variations'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanScheduleApiResourceSwagger.PostLoansLoanIdScheduleRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanScheduleApiResourceSwagger.PostLoansLoanIdScheduleResponse.class))) })
    public String calculateLoanScheduleOrSubmitVariableSchedule(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        CommandWrapper commandRequest = null;
        if (is(commandParam, "calculateLoanSchedule")) {
            this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
            final LoanScheduleData loanSchedule = this.calculationPlatformService.generateLoanScheduleForVariableInstallmentRequest(loanId,
                    apiRequestBodyAsJson);

            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, loanSchedule, new HashSet<String>());
        } else if (is(commandParam, "addVariations")) {
            commandRequest = new CommandWrapperBuilder().createScheduleExceptions(loanId).withJson(apiRequestBodyAsJson).build();
        } else if (is(commandParam, "deleteVariations")) {
            commandRequest = new CommandWrapperBuilder().deleteScheduleExceptions(loanId).build();
        }

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
