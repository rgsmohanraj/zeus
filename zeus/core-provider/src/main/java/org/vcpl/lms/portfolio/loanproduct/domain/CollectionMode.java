package org.vcpl.lms.portfolio.loanproduct.domain;

public enum CollectionMode {

    PARTNER(1,"CollectionMode.partner"),
    DIRECT(2,"CollectionMode.direct"),
    ESCROW(3,"CollectionMode.escrow"),
    RAZORPAY(4,"CollectionMode.razorpay"),
    INVALID(5,"CollectionMode.invalid");


    private final Integer value;
    private final String code;

    CollectionMode(final Integer value, final String code){

        this.value=value;
        this.code=code;

    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static CollectionMode collection(final Integer selectedValue) {

        CollectionMode collection = null;
        switch (selectedValue) {
            case 1:
                collection = CollectionMode.PARTNER;
                break;
            case 2:
                collection = CollectionMode.DIRECT;
                break;
            case 3:
                collection = CollectionMode.ESCROW;
                break;
            case 4:
                collection = CollectionMode.RAZORPAY;
                break;
            default:
                collection= CollectionMode.INVALID;
                break;
        }
        return collection;
    }
}
