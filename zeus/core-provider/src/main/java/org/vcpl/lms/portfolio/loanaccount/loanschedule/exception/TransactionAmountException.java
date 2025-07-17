package org.vcpl.lms.portfolio.loanaccount.loanschedule.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class TransactionAmountException extends AbstractPlatformDomainRuleException {

    public TransactionAmountException(final Object... defaultUserMessageArgs) {
        super("Transaction Amount can not be Zero and Advance Amount Also Not Avilable give Any Transaction Amount",
                "Transaction Amount can not be Zero and Advance Amount Also Not Avilable give Any Transaction Amount", defaultUserMessageArgs);
    }
}
