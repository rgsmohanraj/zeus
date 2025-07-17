package org.vcpl.lms.portfolio.loanaccount.bulkupload.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformException;

public class XlsxReadWriteException extends RuntimeException {
    public XlsxReadWriteException(String defaultUserMessage) {
        super(defaultUserMessage);
    }
}
