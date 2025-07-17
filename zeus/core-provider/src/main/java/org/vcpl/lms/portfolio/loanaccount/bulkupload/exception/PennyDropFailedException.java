package org.vcpl.lms.portfolio.loanaccount.bulkupload.exception;

public class PennyDropFailedException extends RuntimeException {
    public PennyDropFailedException(String defaultUserMessage) {
        super(defaultUserMessage);
    }
}
