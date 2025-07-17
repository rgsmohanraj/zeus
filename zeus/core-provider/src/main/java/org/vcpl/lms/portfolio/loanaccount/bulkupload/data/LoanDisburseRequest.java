package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDisburseRequest {
    private String actualDisbursementDate;
    private BigDecimal transactionAmount;
    private int paymentTypeId;
    private String note;
    private String dateFormat;
    private String locale;
}
