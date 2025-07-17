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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.VPayCredentialManager;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.infrastructure.security.service.TenantDetailsService;
import org.vcpl.lms.portfolio.client.service.ClientReadPlatformServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.api.BulkUploadResponseSwagger;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkUploadResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.MultipartFileUploadRequest;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.mapper.BulkLoanTransactionRequestBuilder;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.BulkLoanUploadService;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.VpayTransactionReadWriteServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.task.BulkLoanUploadTransactionHandler;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.utils.XlsxFileUtils;
import org.vcpl.lms.portfolio.loanaccount.service.LoanReadPlatformService;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRepository;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.vcpl.lms.useradministration.domain.AppUserRepositoryWrapper;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Path("/loans")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BulkLoanUploadApiResource {

    @Value("${zeus.vpay-transaction.enquiry.every}")
    private Integer retry;

    private AppUser systemAppUser;
    private static final Logger LOG = LoggerFactory.getLogger(BulkLoanUploadApiResource.class);
    @Autowired private DefaultToApiJsonSerializer<BulkUploadResponse> toApiJsonSerializer;
    @Autowired private BulkLoanUploadService bulkLoanUploadService;
    @Autowired private AppUserRepositoryWrapper appUserRepositoryWrapper;
    @Autowired private TenantDetailsService tenantDetailsService;
    @Autowired private LoanReadPlatformService loanReadPlatformService;
    @Autowired private ClientReadPlatformServiceImpl clientReadPlatformService;
    @Autowired private VpayTransactionReadWriteServiceImpl vpayTransactionReadWriteService;
    @Autowired private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Autowired private BulkLoanTransactionRequestBuilder bulkLoanTransactionRequestBuilder;
    @Autowired private PlatformSecurityContext context;
    @Autowired private LoanProductRepository loanProductRepository;
    @Autowired private VPayCredentialManager vPayCredentialManager;
    @PostConstruct
    public void initializeExecutorService() {
        systemAppUser = appUserRepositoryWrapper.fetchSystemUser();
        BulkLoanUploadTransactionHandler bulkLoanUploadTransactionHandler = new BulkLoanUploadTransactionHandler(systemAppUser,
                loanReadPlatformService, clientReadPlatformService, vpayTransactionReadWriteService, commandsSourceWritePlatformService,
                bulkLoanTransactionRequestBuilder, tenantDetailsService, loanProductRepository,vPayCredentialManager);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        int initialDelay = 30;
        LOG.info("Zeus -> VPay Transaction Enquiry Scheduled every {} seconds ", retry);
        scheduledExecutorService.scheduleAtFixedRate(bulkLoanUploadTransactionHandler, initialDelay, retry, TimeUnit.SECONDS);
    }

    @GET
    @Path("uploadtemplate")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClientTemplate(@QueryParam("partnerId") final Long partnerId,
                                      @QueryParam("productId") final Long productId) {
         this.context.authenticatedUser().validateHasPermissionTo("BULK_LOAN_CREATION");
         return bulkLoanUploadService.buildBulkLoansUploadTemplate(partnerId,productId);
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BulkUploadResponseSwagger.class))) })
    public ResponseEntity loanReassignment(
            @HeaderParam("Content-Length") @Parameter(description = "Content-Length") final Long fileSize,
            @QueryParam("productId") final Long productId,
            @FormDataParam("file") FormDataContentDisposition fileDetails,
            @FormDataParam("file") File file,
            @Parameter(hidden = true) @FormDataParam("file") final FormDataBodyPart bodyPart,
            @FormDataParam("name") final String name,
            @FormDataParam("description") final String description) throws FileNotFoundException {
        this.context.authenticatedUser().validateHasPermissionTo("BULK_LOAN_CREATION");
        InputStream inputStream=new FileInputStream(file);
        BulkUploadResponse bulkLoansUploadResponse = new BulkUploadResponse();
        LocalDateTime starttime = LocalDateTime.now();
        try {
            XlsxFileUtils.checkIsSupportedFileType(fileDetails.getFileName());
            XlsxFileUtils.checkFileSize(fileSize);
            MultipartFileUploadRequest multipartFileUploadRequest = new MultipartFileUploadRequest(fileSize,productId, inputStream,
                    fileDetails, bodyPart, fileDetails.getFileName(), description);
            bulkLoansUploadResponse = bulkLoanUploadService.initiateLoanProcessing(multipartFileUploadRequest,productId,systemAppUser);
        } catch (PlatformApiDataValidationException exception) {
            LOG.error("Exception {} ",exception.getErrors());
            return ResponseEntity.badRequest().body(exception.getErrors().stream().map(ApiParameterError::getDefaultUserMessage)
                    .collect(Collectors.joining(". ")));
        } catch (Exception exception) {
            LOG.error("Exception {} ",exception);
            return ResponseEntity.badRequest().body(exception.getMessage());        }
        LOG.info("Processing Time: {}", starttime.until(LocalDateTime.now(),ChronoUnit.SECONDS));
        return ResponseEntity.ok(bulkLoansUploadResponse);
    }





}
