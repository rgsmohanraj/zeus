package org.vcpl.lms.portfolio.loanproduct.domain;

import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum Dedupe {
    PAN(1, "Dedupe.pan"),
    AADHAAR(2,"Dedupe.aadhaar"),

    INVALID(3,"invalid");

    private final Integer value;
    private final String code;

    Dedupe(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }


    public static Dedupe fromInt(final Integer selectedMethod) {
        if(Objects.isNull(selectedMethod)) return Dedupe.INVALID;
        Dedupe dedupe = null;
        switch (selectedMethod) {
            case 1:
                dedupe = Dedupe.PAN;
                break;
            case 2:
                dedupe = Dedupe.AADHAAR;
                break;
            default:
                dedupe = Dedupe.INVALID;
                break;
        }
        return dedupe;
    }

    public static List<EnumOptionData> all() {
        return Arrays.asList(LoanEnumerations.dedupeEnum(Dedupe.PAN),
                LoanEnumerations.dedupeEnum(Dedupe.AADHAAR));
    }


}
