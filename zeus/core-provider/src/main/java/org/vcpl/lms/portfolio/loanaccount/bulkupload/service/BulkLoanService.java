package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkApiResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ClientLoanRecord;

import java.io.IOException;
import java.util.List;
/**
 * Loan Bulk API development service
 *
 * @author  Yuva Prasanth K
 * @version 1.0
 * @since   2024-02-05
 */
public interface BulkLoanService {
    public List<BulkApiResponse> initiateLoanProcessing(List<ClientLoanRecord> clientLoadRecords, final Long productId) throws IOException, InstantiationException, IllegalAccessException;
}
