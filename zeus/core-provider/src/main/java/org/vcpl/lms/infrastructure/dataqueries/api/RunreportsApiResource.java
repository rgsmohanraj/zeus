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
package org.vcpl.lms.infrastructure.dataqueries.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Value;
import org.vcpl.lms.infrastructure.core.api.ApiParameterHelper;
import org.vcpl.lms.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.vcpl.lms.infrastructure.dataqueries.service.ReadReportingService;
import org.vcpl.lms.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.vcpl.lms.infrastructure.report.service.ReportingProcessService;
import org.vcpl.lms.infrastructure.security.exception.NoAuthorizationException;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.sql.SQLException;

@Path("/runreports")
@Component
@Scope("singleton")
@Tag(name = "Run Reports", description = "")
public class RunreportsApiResource {

    public static final String IS_SELF_SERVICE_USER_REPORT_PARAMETER = "isSelfServiceUserReport";

    private final PlatformSecurityContext context;
    private final ReadReportingService readExtraDataAndReportingService;
    private final ReportingProcessServiceProvider reportingProcessServiceProvider;
    @Value("${zeus.encryption.secretkey}")
    private String secretKey;


    @Autowired
    public RunreportsApiResource(final PlatformSecurityContext context, final ReadReportingService readExtraDataAndReportingService,
                                 final ReportingProcessServiceProvider reportingProcessServiceProvider) {
        this.context = context;
        this.readExtraDataAndReportingService = readExtraDataAndReportingService;
        this.reportingProcessServiceProvider = reportingProcessServiceProvider;
    }

    @GET
    @Path("{reportName}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON, "text/csv", "application/vnd.ms-excel", "application/pdf", "text/html"})
    @Operation(summary = "Running a Report",
            description = """     
                    This resource allows you to run and receive output from pre-defined LMS reports.
                    \n The default output is a JSON formatted Generic Resultset.The Generic Resultset contains Column Heading as well as Data information. 
                    \n  However, you can export to CSV format by simply adding  &exportCSV=true  to the end of your URL.
                    \nIf Pentaho reports have been pre-defined, they can also be run through this resource. Pentaho reports can return HTML, PDF or CSV formats.
                    \n The LMS reference application uses a JQuery plugin called stretchy reporting which, itself, uses this reports resource to provide a pretty flexible reporting User Interface (UI).
                    \nExample Requests:runreports/Client%20Listing?R_officeId=1 ,runreports/Client%20Listing?R_officeId=1&exportCSV=true ,
                    \nrunreports/OfficeIdSelectOne?R_officeId=1&parameterType=true,
                    \nrunreports/OfficeIdSelectOne?R_officeId=1&parameterType=true&exportCSV=true, 
                    \nrunreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=HTML&R_officeId=1
                    \nrunreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=XLS&R_officeId=1,
                    \nrunreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=CSV&R_officeId=1,
                    \nrunreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=PDF&R_officeId=1,
                    \nFor every report we have different Parameters for filtering the data.
                    \nListed below are the Reports and their respective parameters.
                    \nMultiple RS,Disbursement Report,Collection inflow Report,Collection Appropriation Report,Demand Vs Collection,Servicer Fee for these reports R_startDate,R_endDate and R_partnerId,
                    \nFor Pricipal Outstanding Report and DPD Report - R_asOn and R_partnerId
                    \nBureau Report - R_asOn,
                    \nGST Report-R_loanChargeCreatedStartDate and R_loanChargeCreatedEndDate,
                    \nDemand Report-R_partnerId and Pending Dues till Date,
                    \nStatement of Accounts - R_asOn and R_accountNo""")


    @Parameters({
            @Parameter(in = ParameterIn.QUERY, name = "R_partnerId", example = "1"),
            @Parameter(in = ParameterIn.QUERY, name = "R_startDate", example = "01 January 2022"),
            @Parameter(in = ParameterIn.QUERY, name = "R_endDate", example = "30 November 2023"),
            @Parameter(in = ParameterIn.QUERY, name = "dateFormat", example = "dd MMMM yyyy"),
            @Parameter(in = ParameterIn.QUERY, name = "locale", example = "en"),
            @Parameter(in = ParameterIn.QUERY, name = "R_accountNo", example = "CLNOCPL134454"),
            @Parameter(in = ParameterIn.QUERY, name = "R_asOn", example = "01 January 2022"),
            @Parameter(in = ParameterIn.QUERY, name = "R_loanChargeCreatedStartDate", example = "01 January 2022"),
            @Parameter(in = ParameterIn.QUERY, name = "R_loanChargeCreatedEndDate", example = "01 January 2022"),
            @Parameter(in = ParameterIn.QUERY, name = "R_dueDate", example = "01 January 2022"),
            @Parameter(in = ParameterIn.QUERY, name = "exportPDF", example = "true"),
            @Parameter(in = ParameterIn.QUERY, name = "exportCSV", example = "true"),
            @Parameter(in = ParameterIn.QUERY, name = "R_dueDate", example = "01 January 2022"),

    }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RunreportsApiResourceSwagger.RunReportsResponse.class)))})
    public Response runReport(@PathParam("reportName") @Parameter(description = "reportName") final String reportName,
                              @Context final UriInfo uriInfo,
                              @DefaultValue("false") @QueryParam(IS_SELF_SERVICE_USER_REPORT_PARAMETER) @Parameter(description = IS_SELF_SERVICE_USER_REPORT_PARAMETER) final boolean isSelfServiceUserReport) throws JRException, SQLException, IOException {

        MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
        queryParams.putAll(uriInfo.getQueryParameters());
        queryParams.putSingle("R_decryptKey",secretKey);
        final boolean parameterType = ApiParameterHelper.parameterType(queryParams);

        checkUserPermissionForReport(reportName, parameterType);

        // Pass through isSelfServiceUserReport so that ReportingProcessService implementations can use it
        queryParams.putSingle(IS_SELF_SERVICE_USER_REPORT_PARAMETER, Boolean.toString(isSelfServiceUserReport));

        String reportType = this.readExtraDataAndReportingService.getReportType(reportName, isSelfServiceUserReport, parameterType);
        ReportingProcessService reportingProcessService = this.reportingProcessServiceProvider.findReportingProcessService(reportType);
        if (reportingProcessService == null) {
            throw new PlatformServiceUnavailableException("err.msg.report.service.implementation.missing",
                    ReportingProcessServiceProvider.SERVICE_MISSING + reportType, reportType);
        }
        return reportingProcessService.processRequest(reportName, queryParams);
    }

    private void checkUserPermissionForReport(final String reportName, final boolean parameterType) {
        // Anyone can run a 'report' that is simply getting possible parameter
        // (dropdown listbox) values.
        if (!parameterType) {
            final AppUser currentUser = this.context.authenticatedUser();
            if (currentUser.hasNotPermissionForReport(reportName)) {
                throw new NoAuthorizationException("Not authorised to run report: " + reportName);
            }
        }
    }
}
