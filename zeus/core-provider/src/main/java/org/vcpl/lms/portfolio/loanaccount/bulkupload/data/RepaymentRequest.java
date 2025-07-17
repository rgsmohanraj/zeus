package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentRequest {
    private int installmentNumber;
    private BigDecimal transactionAmount;
    private String transactionDate;
    private String dateFormat;
    private String locale;
    private String receiptReferenceNumber;
    private String partnerTransferUtr;
    private String partnerTransferDate;
    private Integer repaymentMode;
    private int triggeredBy;
    private String coolingOffDate;
    private BigDecimal interestWaiver;
    private String collectionFlag;
}
