package org.vcpl.lms.portfolio.charge.domain;

public enum GstSlabLimitApplyFor {
    LOAN_AMOUNT(1,"gstSlabLimitApplyFor.loan.amount"),
    CHARGE_PERCENTAGE(2,"gstSlabLimitApplyFor.charge.percentage"),
    CHARGE_AMOUNT(3,"gstSlabLimitApplyFor.charge.amount"),
    INVALID(4,"gstSlabLimitApplyFor.invalid");

    private final Integer value;
    private final String code;

    GstSlabLimitApplyFor(final Integer value, final String code){

        this.value=value;
        this.code=code;

    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static GstSlabLimitApplyFor fromInt(final Integer selectedValue) {

        GstSlabLimitApplyFor gstSlabLimitApplyFor = null;
        if(selectedValue!=null){
        switch (selectedValue) {
            case 1:
                gstSlabLimitApplyFor = GstSlabLimitApplyFor.LOAN_AMOUNT;
                break;
            case 2:
                gstSlabLimitApplyFor = GstSlabLimitApplyFor.CHARGE_PERCENTAGE;
                break;
            case 3:
                gstSlabLimitApplyFor = GstSlabLimitApplyFor.CHARGE_AMOUNT;
                break;
            default:
                gstSlabLimitApplyFor= GstSlabLimitApplyFor.INVALID;
                break;
        }}
        return gstSlabLimitApplyFor;
    }
}
