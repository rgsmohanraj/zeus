package org.vcpl.lms.portfolio.loanaccount.bulkupload.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_import_document_details")
public class ImportDocumentDetails extends AbstractPersistableCustom {
    @Column(name = "import_id", nullable = false)
    private Long importId;
    @Column(name = "external_id", nullable = false)
    private String externalId;
    @Column(name = "loan_id", nullable = false)
    private Long loanId;
    @Column(name = "loan_account_no", nullable = false)
    private String loanAccountNo;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date")
    private Date date;
    @Column(name = "status")
    private Boolean status;
    @Column(name = "reason")
    private String reason;
}
