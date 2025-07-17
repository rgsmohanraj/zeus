package org.vcpl.lms.portfolio.loanproduct.domain;

public enum CoolingOffInterestLogicApplicability {
    PNR(1,"CoolingOffInterestLogicApplicability.PNR"),
    MAX(2,"CoolingOffInterestLogicApplicability.MAX"),

    INVALID(0,"invalid");

    private final Integer value;
    private final String code;

    CoolingOffInterestLogicApplicability(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }


    public static CoolingOffInterestLogicApplicability fromInt(final Integer selectedMethod) {

        CoolingOffInterestLogicApplicability coolingOffInterestLogicApplicability = null;
        switch (selectedMethod) {
            case 1:
                coolingOffInterestLogicApplicability = CoolingOffInterestLogicApplicability.PNR;
                break;
            case 2:
                coolingOffInterestLogicApplicability = CoolingOffInterestLogicApplicability.MAX;
                break;
            default:
                coolingOffInterestLogicApplicability = CoolingOffInterestLogicApplicability.INVALID;
                break;
        }
        return coolingOffInterestLogicApplicability;
    }
}
