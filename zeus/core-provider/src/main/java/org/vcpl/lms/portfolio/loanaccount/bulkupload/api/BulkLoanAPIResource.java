package org.vcpl.lms.portfolio.loanaccount.bulkupload.api;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.portfolio.loanaccount.api.BulkOperationResourceSwagger;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkApiResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ClientLoanRecord;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.BulkLoanService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Loan Bulk API development
 *
 * @author  Yuva Prasanth K
 * @version 1.0
 * @since   2024-02-05
 */

@Path("/loanapi")
@Component
@RequiredArgsConstructor
public class BulkLoanAPIResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkLoanAPIResource.class);
    private final BulkLoanService bulkLoanService;

    @POST
    @Path("upload")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequestBody(required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = BulkOperationResourceSwagger.PostBulkLoanOperationRequest.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BulkOperationResourceSwagger.PostBulkOperationResponse.class))))})
    public ResponseEntity<Object> uploadLoadReassignment(@RequestBody List<ClientLoanRecord> clientLoadRecords, @QueryParam("productId") final Long productId) {
        List<BulkApiResponse> bulkLoansApiResponse;
        LocalDateTime startTime = LocalDateTime.now();
        LOGGER.info("Processing Time: {}", startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS));
        try {
            bulkLoansApiResponse = bulkLoanService.initiateLoanProcessing(clientLoadRecords, productId);
        } catch (PlatformApiDataValidationException exception) {
            LOGGER.error("Exception {} ", exception.getErrors());
            return ResponseEntity.badRequest().body(exception.getErrors().stream().map(ApiParameterError::getDefaultUserMessage)
                    .collect(Collectors.joining(". ")));
        } catch (Exception exception) {
            LOGGER.error("Exception {} ".concat(exception.getMessage()));
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
        LOGGER.info("Processing Time: {}", startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS));
        return ResponseEntity.ok(bulkLoansApiResponse);
    }
}
