package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentRecord {
    private Long serialNo;
    private String loanAccountNo;
    private String externalId;
    private Integer installment;
    private BigDecimal transactionAmount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;
    private String receiptReferenceNumber;
    private String partnerTransferUTR;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate partnerTransferDate;
    private String repaymentMode;
    private Long clientId;
    private List<String> errorRecords;
    private ImportDocumentDetails importDocumentDetails;
    private BulkApiResponse bulkApiResponse;
    private Long loanId;
    private boolean externalIdIsEmpty;
    private Map<String,Object> coolingOffSpecific = new HashMap<>();
    private String collectionFlag;
    public boolean isErrorRecord() {
        return !errorRecords.isEmpty();
    }
}
