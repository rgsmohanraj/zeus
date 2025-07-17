package org.vcpl.lms.portfolio.loanaccount.domain.xirr.Exception;

public class ZeroValuedDerivativeException extends OverflowException {
    ZeroValuedDerivativeException(NewtonRaphson.Calculation state) {
        super("Newton-Raphson failed due to zero-valued derivative.", state);
    }
}
