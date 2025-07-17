package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetails;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponse {
    private Integer documentId;
    private String importTime;
    private String endTime;
    private Boolean completed;
    private Integer entityType;
    private Integer totalRecords;
    private Integer successCount;
    private Integer failureCount;
    List<ImportDocumentDetails> importDocumentDetailsList;
    private String errorMessage;

    public BulkUploadResponse(Integer documentId, String importTime, String endTime, Boolean completed, Integer entityType, Integer totalRecords, Integer successCount, Integer failureCount, List<ImportDocumentDetails> importDocumentDetailsList) {
        this.documentId = documentId;
        this.importTime = importTime;
        this.endTime = endTime;
        this.completed = completed;
        this.entityType = entityType;
        this.totalRecords = totalRecords;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.importDocumentDetailsList = importDocumentDetailsList;
    }
}
