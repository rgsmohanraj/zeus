package org.vcpl.lms.portfolio.loanproduct.domain;

public enum DisbursementMode {

    DIRECT(1, "DisbursementMode.direct"),
    ESCROW(2, "DisbursementMode.escrow"),
    REIMBURSEMENT(3, "DisbursementMode.Reimbursement"),
    INVALID(4,"DisbursementMode.Invalid");


    private final Integer value;
    private final String code;


    DisbursementMode(final Integer value, final String code) {
        this.value=value;
        this.code =code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static DisbursementMode disbursement(final Integer selectionMode) {

        DisbursementMode disbursementMode = null;
        switch (selectionMode) {
            case 1:
                disbursementMode = DisbursementMode.DIRECT;
                break;
            case 2:
                disbursementMode = DisbursementMode.ESCROW;
                break;
            case 3:
                disbursementMode = DisbursementMode.REIMBURSEMENT;
                break;
            default:
                disbursementMode = DisbursementMode.INVALID;
                break;


        }
        return disbursementMode;
    }
}
