package org.vcpl.lms.portfolio.loanproduct.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class EnableColendingLoanException extends AbstractPlatformDomainRuleException {

    public EnableColendingLoanException(final Object... defaultUserMessageArgs) {
        super("Enable Colending Loan is mandatory",
                "Enable Colending Loan is mandatory", defaultUserMessageArgs);
    }
}
