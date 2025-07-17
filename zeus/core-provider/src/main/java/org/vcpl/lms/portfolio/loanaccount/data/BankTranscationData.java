package org.vcpl.lms.portfolio.loanaccount.data;

import java.util.ArrayList;
import java.util.List;

public class BankTranscationData {

    private List<VpayTransactionDetailsData> pennyDropTransaction ;
    private List<VpayTransactionDetailsData> disbursementTransaction ;

    public List<VpayTransactionDetailsData> getPennyDropTransaction() {
        return pennyDropTransaction;
    }

    public void setPennyDropTransaction(List<VpayTransactionDetailsData> pennyDropTransaction) {
        this.pennyDropTransaction = pennyDropTransaction;
    }

    public List<VpayTransactionDetailsData> getDisbursementTransaction() {
        return disbursementTransaction;
    }

    public void setDisbursementTransaction(List<VpayTransactionDetailsData> disbursementTransaction) {
        this.disbursementTransaction = disbursementTransaction;
    }

    public BankTranscationData(List<VpayTransactionDetailsData> pennyDropTransaction,
                               List<VpayTransactionDetailsData> disbursementTransaction) {
        this.pennyDropTransaction = pennyDropTransaction;
        this.disbursementTransaction = disbursementTransaction;
    }
}
