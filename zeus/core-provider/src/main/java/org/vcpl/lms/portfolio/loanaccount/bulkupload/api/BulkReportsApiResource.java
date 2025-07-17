package org.vcpl.lms.portfolio.loanaccount.bulkupload.api;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.api.LoansApiResourceSwagger;
import org.vcpl.lms.portfolio.loanaccount.data.ImportDetailsResponseData;
import org.vcpl.lms.portfolio.loanaccount.data.ImportDocumentData;
import org.vcpl.lms.portfolio.loanaccount.service.LoanReadPlatformServiceImpl;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

@Path("/bulkReports")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BulkReportsApiResource {


    private final String resourceNameForPermissions = "LOAN";
    private final PlatformSecurityContext context;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final LoanReadPlatformServiceImpl loanReadPlatformServiceImp;
    private final DefaultToApiJsonSerializer<ImportDocumentData> bulkReportsFilterDefaultToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<ImportDetailsResponseData> bulkDownloadReasonDefaultToApiJsonSerializer;


    public BulkReportsApiResource(PlatformSecurityContext context, ApiRequestParameterHelper apiRequestParameterHelper,
                                  LoanReadPlatformServiceImpl loanReadPlatformServiceImp,
                                  DefaultToApiJsonSerializer<ImportDocumentData> bulkReportsFilterDefaultToApiJsonSerializer,
                                  DefaultToApiJsonSerializer<ImportDetailsResponseData> bulkDownloadReasonDefaultToApiJsonSerializer) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.loanReadPlatformServiceImp= loanReadPlatformServiceImp;
        this.bulkReportsFilterDefaultToApiJsonSerializer = bulkReportsFilterDefaultToApiJsonSerializer;
        this.bulkDownloadReasonDefaultToApiJsonSerializer = bulkDownloadReasonDefaultToApiJsonSerializer;
    }

    @GET
    @Path("bulkReportsFilter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansResponse.class))) })
    public String retrieveBulkReports(@Context final UriInfo uriInfo,

                      @QueryParam("fromDate")  final String fromDate,
                      @QueryParam("toDate")  final String toDate,
                      @QueryParam("type")  final String type)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final List<ImportDocumentData> importDocumentData = this.loanReadPlatformServiceImp.retriveBulkReportsData(fromDate,toDate,type);
        return this.bulkReportsFilterDefaultToApiJsonSerializer.serialize(settings, importDocumentData);

    }

}


