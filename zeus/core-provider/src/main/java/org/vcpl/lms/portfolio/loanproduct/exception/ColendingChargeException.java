package org.vcpl.lms.portfolio.loanproduct.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ColendingChargeException extends AbstractPlatformDomainRuleException {

    public ColendingChargeException(final Object... defaultUserMessageArgs) {
        super("Enable charge is given required Atleast one charge type is requires",
                "Enable charge is given required Atleast one charge type is requires", defaultUserMessageArgs);
    }
}
