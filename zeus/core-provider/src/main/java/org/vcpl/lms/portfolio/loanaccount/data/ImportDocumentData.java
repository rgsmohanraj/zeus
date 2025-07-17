package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportDocumentData {
    private Long id;
    private String name;
    private String importTime;
    private String endTime;
    private Integer totalRecords;
    private Integer successCount;
    private Integer failureCount;
    private List<ImportDetailsResponseData> importDetailsResponseData;
}
