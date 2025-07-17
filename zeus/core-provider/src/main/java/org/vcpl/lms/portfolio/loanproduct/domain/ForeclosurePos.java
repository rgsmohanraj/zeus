package org.vcpl.lms.portfolio.loanproduct.domain;

public enum ForeclosurePos {
    RS_POS(1,"ForeclosurePos.rs pos"),
    REVISED_POS(2,"ForeclosurePos.revised pos"),
    INVALID(0,"invalid");

    private final Integer value;
    private final String code;

    ForeclosurePos(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public static Boolean isRsPos(ForeclosurePos foreclosurePos) {
        return foreclosurePos.equals(ForeclosurePos.RS_POS);}

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }


    public static ForeclosurePos fromInt(final Integer selectedMethod) {

        ForeclosurePos foreclosurePos = null;
        switch (selectedMethod) {
            case 1:
                foreclosurePos = ForeclosurePos.RS_POS;
                break;
            case 2:
                foreclosurePos = ForeclosurePos.REVISED_POS;
                break;
            default:
                foreclosurePos = ForeclosurePos.INVALID;
                break;
        }
        return foreclosurePos;
    }
}
