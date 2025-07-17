package org.vcpl.lms.portfolio.loanaccount.data;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeCollection {
    private String externalId;
    private String loanAccountNo;
    private Integer installment;
    private String transactionDate;
    private List<ChargeDetails> charges;
    private String receiptReferenceNumber;
    private String partnerTransferUtr;
    private String partnerTransferDate;
    private String repaymentMode;
}
