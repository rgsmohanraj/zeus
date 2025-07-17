package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApproveRequest {
    private String approvedOnDate;
    private String expectedDisbursementDate;
    private BigDecimal approvedLoanAmount;
    private String note;
    private String dateFormat;
    private String locale;
}
