package org.vcpl.lms.portfolio.loanaccount.bulkupload.exception;

public class DisbursementFailedException extends RuntimeException {
    public DisbursementFailedException(String defaultUserMessage) {
        super(defaultUserMessage);
    }
}
