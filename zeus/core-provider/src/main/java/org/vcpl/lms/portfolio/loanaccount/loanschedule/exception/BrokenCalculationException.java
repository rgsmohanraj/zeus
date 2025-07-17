package org.vcpl.lms.portfolio.loanaccount.loanschedule.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class BrokenCalculationException extends AbstractPlatformDomainRuleException {

    public BrokenCalculationException(final Object... defaultUserMessageArgs) {
        super("For calculating BrokenInterest Due Date should be more than 30 or select NoBroken In Loan Product",
                "For calculating BrokenInterest Due Date should be more than 30 or select NoBroken In Loan Product", defaultUserMessageArgs);
    }
}