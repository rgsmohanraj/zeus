package org.vcpl.lms.portfolio.loanaccount.bulkupload.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformException;

public class BulkLoansUploadException extends AbstractPlatformException {
    public BulkLoansUploadException(String globalisationMessageCode, String defaultUserMessage) {
        super(globalisationMessageCode, defaultUserMessage);
    }
}
