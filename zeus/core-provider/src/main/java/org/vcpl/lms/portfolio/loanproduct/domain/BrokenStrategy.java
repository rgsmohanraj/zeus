package org.vcpl.lms.portfolio.loanproduct.domain;



public enum BrokenStrategy {

    NOBROKEN(1,"BrokenStrategy.nobroken"),
    DISBURSEMENT(2,"BrokenStrategy.disbursement"),
    FIRSTREPAYMENT(3,"BrokenStrategy.firstrepayment"),
    LASTREPAYMENT(4,"BrokenStrategy.lastrepayment"),
    INVALID(5,"invaild" );


    private final Integer value;
    private final String code;

    BrokenStrategy(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }


    public static BrokenStrategy fromInt(final Integer selectedMethod) {

        BrokenStrategy brokenStrategy = null;
        switch (selectedMethod) {
            case 1:
                brokenStrategy = BrokenStrategy.NOBROKEN;
                break;
            case 2:
                brokenStrategy = BrokenStrategy.DISBURSEMENT;
                break;
            case 3:
                brokenStrategy = BrokenStrategy.FIRSTREPAYMENT;
                break;
            case 4:
                brokenStrategy = BrokenStrategy.LASTREPAYMENT;
                break;
            default:
                brokenStrategy = BrokenStrategy.INVALID;
                break;
        }
        return brokenStrategy;
    }

}
