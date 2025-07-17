package org.vcpl.lms.portfolio.loanaccount.bulkupload.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.api.BulkUploadResponseSwagger;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkUploadResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.MultipartFileUploadRequest;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.BulkChargeRepaymentServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.BulkUploadProcessorService;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.utils.XlsxFileUtils;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Path("/charges")
@Component
@RequiredArgsConstructor
public class BulkChargeRepaymentApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(BulkChargeRepaymentApiResource.class);
    private final PlatformSecurityContext context;
    private final BulkChargeRepaymentServiceImpl bulkChargeRepaymentService;

    @GET
    @Path("collection/template")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClientTemplate(@QueryParam("partnerId") final Long partnerId,
                                      @QueryParam("productId") final Long productId) {
        this.context.authenticatedUser().validateHasPermissionTo("BULK_LOAN_CREATION");
        return bulkChargeRepaymentService.buildBulkChargeRepaymentUploadTemplate(partnerId,productId);
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BulkUploadResponseSwagger.class))) })
    public ResponseEntity chargeCollection(
            @HeaderParam("Content-Length") @Parameter(description = "Content-Length") final Long fileSize,
            @QueryParam("productId") final Long productId,
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetails,
            @FormDataParam("file") final FormDataBodyPart bodyPart,
            @FormDataParam("name") final String name,
            @FormDataParam("description") final String description) {
        this.context.authenticatedUser().validateHasPermissionTo("BULK_LOAN_CREATION");
        BulkUploadResponse bulkLoansUploadResponse = new BulkUploadResponse();
        LocalDateTime starttime = LocalDateTime.now();
        try {
            XlsxFileUtils.checkIsSupportedFileType(fileDetails.getFileName());
            XlsxFileUtils.checkFileSize(fileSize);
            MultipartFileUploadRequest multipartFileUploadRequest = new MultipartFileUploadRequest(fileSize,productId, inputStream,
                    fileDetails, bodyPart, name, description);
            bulkLoansUploadResponse = bulkChargeRepaymentService.initiateChargeRepayment(multipartFileUploadRequest,productId);
        } catch (PlatformApiDataValidationException exception) {
            LOG.error("Exception {} ",exception.getErrors());
            return ResponseEntity.badRequest().body(exception.getErrors().stream().map(ApiParameterError::getDefaultUserMessage)
                    .collect(Collectors.joining(". ")));
        } catch (Exception exception) {
            LOG.error("Exception {} ",exception);
            return ResponseEntity.badRequest().body(exception.getMessage());        }
        LOG.info("Processing Time: {}", starttime.until(LocalDateTime.now(), ChronoUnit.SECONDS));
        return ResponseEntity.ok(bulkLoansUploadResponse);
    }
}
