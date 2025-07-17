package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@RequiredArgsConstructor
public class ChargeTransactionRequest {
    private Long loanId;
    private String loanAccountNo;
    private String externalId;
    private Long chargeId;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private Integer installment;
    // Collection Information
    private String receiptReferenceNumber;
    private String partnerTransferUtr;
    private LocalDate partnerTransferDate;
    private String repaymentMode;
    private String dateFormat;
    private String locale;
}
