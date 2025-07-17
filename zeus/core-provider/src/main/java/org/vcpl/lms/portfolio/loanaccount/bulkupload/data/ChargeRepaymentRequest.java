package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class ChargeRepaymentRequest {
    private Long loanId;
    private String loanAccountNo;
    private String externalId;
    private Long chargeId;
    private BigDecimal amount;
    private String transactionDate;
    private Integer installment;
    // Collection Information
    private String receiptReferenceNumber;
    private String partnerTransferUtr;
    private String partnerTransferDate;
    private String repaymentMode;
    private String dateFormat;
    private String locale;
}
