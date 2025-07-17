package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ChargeRepaymentRecord;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ClientLoanRecord;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.RepaymentRecord;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.util.Map;

public interface BulkUploadProcessorService {
    void process(ClientLoanRecord clientLoanRecord, LoanProduct loanProduct);
    void createClient(final ClientLoanRecord clientLoanRecord, final LoanProduct loanProduct);
    void createLoan(final ClientLoanRecord clientLoanRecord,final LoanProduct loanProduct) throws Exception;
    void approveLoan(final ClientLoanRecord clientLoanRecord);
    void disburseLoan(final ClientLoanRecord clientLoanRecord);
    void repayLoan(final RepaymentRecord repaymentRecord);
    void repayCharge(ChargeRepaymentRecord record,final Map<String, Charge> chargeMap);
}
