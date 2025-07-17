package org.vcpl.lms.portfolio.loanproduct.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ColendingFeesException extends AbstractPlatformDomainRuleException {

    public ColendingFeesException(final Object... defaultUserMessageArgs) {
        super("Enable Fees is given required Atleast one Fees type is requires",
                "Enable Fees is given required Atleast one Fees type is requires", defaultUserMessageArgs);
    }
}
