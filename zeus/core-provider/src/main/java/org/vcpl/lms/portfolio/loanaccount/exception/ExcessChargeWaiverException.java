package org.vcpl.lms.portfolio.loanaccount.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ExcessChargeWaiverException extends AbstractPlatformDomainRuleException {
    public ExcessChargeWaiverException() {
        super("error.msg.excess.charge.waiver", "Waiver amount cannot be greater than total outstanding");
    }
}
