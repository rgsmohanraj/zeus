package org.vcpl.lms.portfolio.loanproduct.domain;

import java.util.Objects;

public enum TransactionTypePreference {

    IMPS(1,"TransactionTypePreference.IMPS"),
    RTGS(2,"TransactionTypePreference.RTGS"),
    NEFT(3,"TransactionTypePreference.NEFT"),
    INVALID(4,"TransactionTypePreference.invalid");

    private final Integer value;
    private final String code;

    TransactionTypePreference(final Integer value, final String code){

        this.value=value;
        this.code=code;

    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static TransactionTypePreference transactionTypePreference(final Integer selectedValue) {
        TransactionTypePreference transactionTypePreference = null;
        if(Objects.isNull(selectedValue)) return null;
        switch (selectedValue) {
            case 1:
                transactionTypePreference = TransactionTypePreference.IMPS;
                break;
            case 2:
                transactionTypePreference = TransactionTypePreference.RTGS;
                break;
            case 3:
                transactionTypePreference = TransactionTypePreference.NEFT;
                break;
            default:
                transactionTypePreference= TransactionTypePreference.INVALID;
                break;
        }
        return transactionTypePreference;
    }
}
