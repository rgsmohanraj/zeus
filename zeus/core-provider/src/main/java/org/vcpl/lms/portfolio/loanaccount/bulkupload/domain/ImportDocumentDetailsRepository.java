package org.vcpl.lms.portfolio.loanaccount.bulkupload.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportDocumentDetailsRepository extends JpaRepository<ImportDocumentDetails, Long>  {
    List<ImportDocumentDetails> getByImportId(long importId);
}
