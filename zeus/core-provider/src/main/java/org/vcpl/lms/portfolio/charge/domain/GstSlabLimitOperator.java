package org.vcpl.lms.portfolio.charge.domain;

public enum GstSlabLimitOperator {
    GREATER_THAN(1,"gstSlabLimitOperator.greater.than"),
    GREATER_THAN_EQUAL_TO(2,"gstSlabLimitOperator.greater.than.equal.to"),
    LESS_THAN(3,"gstSlabLimitOperator.less.than"),
    LESS_THAN_EQUAL_TO(4,"gstSlabLimitOperator.less.than.equal.to"),
    EQUAL_TO(5,"gstSlabLimitOperator.equal.to"),
    INVALID(6,"gstSlabLimitOperator.invalid");

    private final Integer value;
    private final String code;

    GstSlabLimitOperator(final Integer value, final String code){

        this.value=value;
        this.code=code;

    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static GstSlabLimitOperator fromInt(final Integer selectedValue) {

        GstSlabLimitOperator gstSlabLimitOperator = null;
        if(selectedValue!=null){
        switch (selectedValue) {
            case 1:
                gstSlabLimitOperator = GstSlabLimitOperator.GREATER_THAN;
                break;
            case 2:
                gstSlabLimitOperator = GstSlabLimitOperator.GREATER_THAN_EQUAL_TO;
                break;
            case 3:
                gstSlabLimitOperator = GstSlabLimitOperator.LESS_THAN;
                break;
            case 4:
                gstSlabLimitOperator = GstSlabLimitOperator.LESS_THAN_EQUAL_TO;
                break;
            case 5:
                gstSlabLimitOperator = GstSlabLimitOperator.EQUAL_TO;
                break;
            default:
                gstSlabLimitOperator= GstSlabLimitOperator.INVALID;
                break;
        }}
        return gstSlabLimitOperator;
    }
    public static Object[] validateGstSlabOperator(){
        return  new Object[]{
                GstSlabLimitOperator.GREATER_THAN.getValue(),
                GstSlabLimitOperator.GREATER_THAN_EQUAL_TO.getValue(),
                GstSlabLimitOperator.LESS_THAN.getValue(),
                GstSlabLimitOperator.LESS_THAN_EQUAL_TO.getValue(),
                GstSlabLimitOperator.EQUAL_TO.getValue()
        };
    }
}
