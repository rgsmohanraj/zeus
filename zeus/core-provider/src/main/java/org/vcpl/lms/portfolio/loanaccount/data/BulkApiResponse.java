package org.vcpl.lms.portfolio.loanaccount.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkApiResponse {
    private String loanAccountNo;
    private String externalId;
    private String status;
    private String reason;
}
