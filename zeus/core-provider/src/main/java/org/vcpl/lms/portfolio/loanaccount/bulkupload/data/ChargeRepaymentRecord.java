package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeRepaymentRecord {
    private Integer serialNo;
    private String loanAccount;
    private String externalId;
    private Integer installmentNumber;
    private LocalDate transactionDate;
    private String receiptReferenceNumber;
    private String partnerTransferUtr;
    private LocalDate partnerTransferDate;
    private String repaymentMode;
    private Map<String, BigDecimal> charges;

    private ImportDocumentDetails importDocumentDetails;
    private Long loanId;
    private List<String> errorRecords;
    private List<String> infoRecords;
    public boolean isErrorRecord() {
        return !errorRecords.isEmpty();
    }
}
