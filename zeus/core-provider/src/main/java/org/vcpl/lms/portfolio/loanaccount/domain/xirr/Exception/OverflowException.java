package org.vcpl.lms.portfolio.loanaccount.domain.xirr.Exception;

public class OverflowException extends ArithmeticException{
    private final NewtonRaphson.Calculation state;

    OverflowException(String message, NewtonRaphson.Calculation state) {
        super(message);
        this.state = state;
    }

    /**
     * @Author Doni Sharmila
     * Get the initial guess used by the algorithm.
     * @return the initial guess
     */
    public double getInitialGuess() {
        return state.getGuess();
    }

    /**
     * Get the number of iterations passed when encountering the overflow.
     * @return the number of iterations passed when encountering the overflow
     * condition
     */
    public long getIteration() {
        return state.getIteration();
    }

    /**
     * Get the candidate value when the overflow condition occurred.
     * @return the candidate value when the overflow condition occurred
     */
    public double getCandidate() {
        return state.getCandidate();
    }

    /**
     * Get the function value when the overflow condition occurred.
     * @return the function value when the overflow condition occurred
     */
    public double getValue() {
        return state.getValue();
    }

    /**
     * Get the derivative value when the overflow condition occurred.  A null
     * value indicates the derivative was not yet calculated.
     * @return the derivative value when the overflow condition occurred
     */
    public Double getDerivativeValue() {
        return state.getDerivativeValue();
    }

    @Override
    public String toString() {
        return super.toString() + ": " + state;
    }
}
