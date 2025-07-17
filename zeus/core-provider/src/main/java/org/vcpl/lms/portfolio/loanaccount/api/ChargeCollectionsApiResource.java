package org.vcpl.lms.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vcpl.lms.portfolio.loanaccount.data.BulkApiResponse;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeCollectionRequest;
import org.vcpl.lms.portfolio.loanaccount.service.ChargeCollectionService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("charges/collection")
@Component
@Scope("singleton")
@Tag(name = "Charge Collection",
        description = "Module used to make bulk charge collection")
public class ChargeCollectionsApiResource {
    @Autowired
    private ChargeCollectionService chargeCollectionServiceImpl;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Charge Collections", description = "Charges are paid and waivedOff on bulk")
    @RequestBody(required = true, content =
                    @Content(schema = @Schema(implementation =
                            LoanChargesApiResourceSwagger.PostLoansLoanIdChargesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesResponse.class))) })
    public List<BulkApiResponse> collect(@RequestBody ChargeCollectionRequest request) {
        return chargeCollectionServiceImpl.process(request);
    }
}
