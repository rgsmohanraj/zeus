package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkApiResponse {
    private String loanAccountNo;
    private String externalId;
    private boolean status;
    private String reason;
}