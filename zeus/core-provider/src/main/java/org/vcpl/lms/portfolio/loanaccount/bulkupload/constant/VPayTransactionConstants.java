package org.vcpl.lms.portfolio.loanaccount.bulkupload.constant;

public final class VPayTransactionConstants {
    private VPayTransactionConstants() {}
    public static final String ZEUS_APP_CODE = "Z";

    public final static class TransactionEventType {
        private TransactionEventType() {}
        public static final String PENNY_DROP = "PENNY_DROP";
        public static final String DISBURSEMENT = "DISBURSEMENT";
    }

    public final static class Action {
        private Action() {}

        public static final String SUCCESS = "SUCCESS";
        public static final String FAILURE = "FAILURE";
        public static final String IN_PROGRESS = "INPROGRESS";
        public static final String SUSPECT = "SUSPECT";
        public static final String PROCESSED = "PROCESSED";
    }

    public final static class Bank {
        private Bank() {}
        public static final String FEDERAL = "federal";
    }



}
