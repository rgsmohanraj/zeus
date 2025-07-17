package org.vcpl.lms.portfolio.loanaccount.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ExcessChargePaymentException extends AbstractPlatformDomainRuleException {
    public ExcessChargePaymentException() {
        super("error.msg.excess.charge.payment", "Received amount cannot be greater than total outstanding");
    }
}
