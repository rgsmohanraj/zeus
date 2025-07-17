package org.vcpl.lms.portfolio.loanaccount.domain;

public enum LoanAccrualType {

    DAILY(1,"loanAccrualType.daily"),
    MONTHLY(2,"loanAccrualType.monthly");
    private final Integer value;
    private final String code;

    LoanAccrualType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }
    public String getCode() {
        return this.code;
    }
}
