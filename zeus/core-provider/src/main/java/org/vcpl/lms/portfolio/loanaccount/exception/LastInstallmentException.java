package org.vcpl.lms.portfolio.loanaccount.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LastInstallmentException extends AbstractPlatformDomainRuleException {

    public LastInstallmentException(final Object... defaultUserMessageArgs) {
        super("Repayment Amount is should not more than EMI Amount in Last Repayment",
                "Repayment Amount is should not more than EMI Amount in Last Repayment", defaultUserMessageArgs);

    }



}
