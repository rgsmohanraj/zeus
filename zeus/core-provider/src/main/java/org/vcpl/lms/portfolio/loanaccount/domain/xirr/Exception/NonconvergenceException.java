package org.vcpl.lms.portfolio.loanaccount.domain.xirr.Exception;

public class NonconvergenceException extends IllegalArgumentException{

    private final double initialGuess;
    private final long iterations;

    public NonconvergenceException(double guess, long iterations) {
        super("Newton-Raphson failed to converge within " + iterations
                + " iterations.");
        this.initialGuess = guess;
        this.iterations = iterations;
    }

    public double getInitialGuess() {
        return initialGuess;
    }

    /**
     * Get the number of iterations applied.
     * @return the number of iterations applied.
     */
    public long getIterations() {
        return iterations;
    }
}
