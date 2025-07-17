package org.vcpl.lms.portfolio.loanaccount.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class BackDatedDisbursementNotAllowedException extends AbstractPlatformDomainRuleException {

    public BackDatedDisbursementNotAllowedException(String globalisationMessageCode,
                                                    String defaultUserMessage, Object... defaultUserMessageArgs) {
        super(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }
}
