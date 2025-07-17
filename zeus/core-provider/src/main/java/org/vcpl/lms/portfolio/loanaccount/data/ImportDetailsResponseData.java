package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportDetailsResponseData {
    private String externalId;
    private Long loanId;
    private String loanAccountNo;
    private String date;
    private String status;
    private String reason;
}
