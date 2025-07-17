package org.vcpl.lms.portfolio.loanproduct.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum CoolingOffInterestAndChargeApplicability {
    NO_INTEREST(1, "CoolingOffInterestAndChargeApplicability.no_interest"),
    ONLY_INTEREST(2, "CoolingOffInterestAndChargeApplicability.only_interest"),
    INTEREST_AND_CHARGES(3, "CoolingOffInterestAndChargeApplicability.interest_and_charges"),
    ONLY_CHARGES(4, "CoolingOffInterestAndChargeApplicability.only_charges"),

    INVALID(0, "invalid");

    private final Integer value;
    private final String code;

    CoolingOffInterestAndChargeApplicability(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }


    public static CoolingOffInterestAndChargeApplicability fromInt(final Integer selectedMethod) {

        if (Objects.isNull(selectedMethod)) {
            return CoolingOffInterestAndChargeApplicability.INVALID;
        }

        CoolingOffInterestAndChargeApplicability coolingOffInterestAndChargeApplicability = null;
        switch (selectedMethod) {
            case 1:
                coolingOffInterestAndChargeApplicability = CoolingOffInterestAndChargeApplicability.NO_INTEREST;
                break;
            case 2:
                coolingOffInterestAndChargeApplicability = CoolingOffInterestAndChargeApplicability.ONLY_INTEREST;
                break;
            case 3:
                coolingOffInterestAndChargeApplicability = CoolingOffInterestAndChargeApplicability.INTEREST_AND_CHARGES;
                break;
            case 4:
                coolingOffInterestAndChargeApplicability = CoolingOffInterestAndChargeApplicability.ONLY_CHARGES;
                break;
            default:
                coolingOffInterestAndChargeApplicability = CoolingOffInterestAndChargeApplicability.INVALID;
                break;
        }
        return coolingOffInterestAndChargeApplicability;
    }
}
