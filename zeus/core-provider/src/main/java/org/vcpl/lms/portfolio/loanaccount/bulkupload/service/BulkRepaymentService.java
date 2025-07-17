package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkApiResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.RepaymentRecord;

import java.io.IOException;
import java.util.List;
/**
 * Repayment Bulk API development service
 *
 * @author  Yuva Prasanth K
 * @version 1.0
 * @since   2024-02-05
 */
public interface BulkRepaymentService {
    List<BulkApiResponse> initiateRepayment(final List<RepaymentRecord> repaymentRecord)
            throws IOException, InstantiationException, IllegalAccessException;
}
