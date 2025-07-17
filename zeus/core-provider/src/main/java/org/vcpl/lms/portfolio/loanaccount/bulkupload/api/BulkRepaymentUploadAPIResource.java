package org.vcpl.lms.portfolio.loanaccount.bulkupload.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.api.BulkUploadResponseSwagger;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkUploadResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.MultipartFileUploadRequest;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.BulkRepaymentUploadService;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.XlsxTemplateService;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.utils.XlsxFileUtils;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.stream.Collectors;

@Path("/repayment")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BulkRepaymentUploadAPIResource {
    private static final Logger LOG = LoggerFactory.getLogger(BulkRepaymentUploadAPIResource.class);
    @Autowired private XlsxTemplateService xlsxTemplateService;
    @Autowired private BulkRepaymentUploadService bulkRepaymentService;
    @Autowired private DefaultToApiJsonSerializer<BulkUploadResponse> toApiJsonSerializer;
    @Autowired private PlatformSecurityContext context;

    @GET
    @Path("uploadtemplate")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRepaymentTemplate(@QueryParam("productId") Long productId) {
        this.context.authenticatedUser().validateHasPermissionTo("BULK_COLLECTION");
        return bulkRepaymentService.buildBulkRepaymentTemplate(productId);
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BulkUploadResponseSwagger.class))) })
    public ResponseEntity<?> loanRepayment(
            @HeaderParam("Content-Length") @Parameter(description = "Content-Length") final Long fileSize,
            @FormDataParam("file") FormDataContentDisposition fileDetails,
            @FormDataParam("file") File file,
            @Parameter(hidden = true) @FormDataParam("file") final FormDataBodyPart bodyPart,
            @FormDataParam("name") final String name,
            @FormDataParam("description") final String description,
            @QueryParam("productId") Long productId
    ) throws FileNotFoundException {
        this.context.authenticatedUser().validateHasPermissionTo("BULK_COLLECTION");
        BulkUploadResponse bulkUploadResponse = new BulkUploadResponse();
        InputStream inputStream=new FileInputStream(file);
        try {
            XlsxFileUtils.checkIsSupportedFileType(fileDetails.getFileName());
            XlsxFileUtils.checkFileSize(fileSize);
            MultipartFileUploadRequest multipartFileUploadRequest = new MultipartFileUploadRequest(fileSize,productId, inputStream,
                    fileDetails, bodyPart, fileDetails.getFileName(), description);
            bulkUploadResponse = bulkRepaymentService.initiateRepayment(multipartFileUploadRequest,productId);
        }catch (PlatformApiDataValidationException exception) {
            LOG.error("Exception: {} ",exception.getErrors().stream().map(ApiParameterError::getDefaultUserMessage)
                    .collect(Collectors.joining(",")).toString());
            return ResponseEntity.badRequest().body(exception.getErrors().stream().map(ApiParameterError::getDefaultUserMessage)
                    .collect(Collectors.joining(". ")));
        } catch (Exception exception) {
            LOG.error("Exception: {} ",exception);
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
        return ResponseEntity.ok(bulkUploadResponse);
    }

}
