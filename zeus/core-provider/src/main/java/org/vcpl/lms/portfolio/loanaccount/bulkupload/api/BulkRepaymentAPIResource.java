package org.vcpl.lms.portfolio.loanaccount.bulkupload.api;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.api.BulkOperationResourceSwagger;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkApiResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.RepaymentRecord;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.BulkRepaymentService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Repayment Bulk API development
 *
 * @author  Yuva Prasanth K
 * @version 1.0
 * @since   2024-02-05
 */
@Path("/repaymentapi")
@Component
@RequiredArgsConstructor
public class BulkRepaymentAPIResource {
    private final BulkRepaymentService bulkRepaymentService;
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkRepaymentAPIResource.class);
    private final PlatformSecurityContext context;

    @POST
    @Path("upload")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = BulkOperationResourceSwagger.PostBulkRepaymentOperationRequest.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BulkOperationResourceSwagger.PostBulkOperationResponse.class))))})
    public ResponseEntity<Object> loanRepayment(@RequestBody List<RepaymentRecord> repaymentRecord) {
        this.context.authenticatedUser().validateHasPermissionTo("BULK_COLLECTION");
        List<BulkApiResponse> bulkApiResponses = new ArrayList<>();
        try {
            bulkApiResponses = bulkRepaymentService.initiateRepayment(repaymentRecord);
        }catch (PlatformApiDataValidationException exception) {
            LOGGER.error("Exception: {} ".concat(exception.getErrors().stream().map(ApiParameterError::getDefaultUserMessage)
                    .collect(Collectors.joining(","))));
            return ResponseEntity.badRequest().body(exception.getErrors().stream().map(ApiParameterError::getDefaultUserMessage)
                    .collect(Collectors.joining(". ")));
        } catch (Exception exception) {
            LOGGER.error("Exception: {} ".concat(exception.getMessage()));
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
        return ResponseEntity.ok(bulkApiResponses);
    }
}
