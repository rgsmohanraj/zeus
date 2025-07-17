package org.vcpl.lms.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;

@Schema(description = "BulkUploadResponse")
public class BulkUploadResponseSwagger {
    private BulkUploadResponseSwagger() {}
    @Schema(example = "1")
    public Integer documentId;
    @Schema(example = "06 December 2023")
    public String importTime;
    @Schema(example = "06 December 2023")
    public String endTime;
    @Schema(example = "true")
    public Boolean completed;
    @Schema(example = "100")
    public Integer totalRecords;
    @Schema(example = "89")
    public Integer successCount;
    @Schema(example = "11")
    public Integer failureCount;
    public List<ImportDocumentDetailsSwagger> importDocumentDetailsList;

    @Schema(example = "")
    public String errorMessage;
    static final class ImportDocumentDetailsSwagger {
        @Schema(example = "1")
        public Long importId;
        @Schema(example = "EXTERNAL00034")
        public String externalId;
        @Schema(example = "11")
        public Long loanId;
        @Schema(example = "DERT11000001")
        public String loanAccountNo;
        @Schema(example = "06 December 2023")
        public Date date;
        @Schema(example = "true")
        public Boolean status;
        @Schema(example = "Failed due to invalid externalId")
        public String reason;
    }
}
