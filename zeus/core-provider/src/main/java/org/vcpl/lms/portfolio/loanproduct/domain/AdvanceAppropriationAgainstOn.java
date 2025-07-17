package org.vcpl.lms.portfolio.loanproduct.domain;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum AdvanceAppropriationAgainstOn {

    PRINCIPAL(1,"AdvanceAppropriationAgainstOn.Principal"),
    INTEREST_PRINCIPAL(2,"AdvanceAppropriationAgainstOn.Interest_Principal"),
    INVALID(0,"AdvanceAppropriationAgainstOn.Invalid");

    private final Integer value;
    private final String code;
    AdvanceAppropriationAgainstOn(Integer value,String code){
        this.value =value;
        this.code=code;
    }


    public static  AdvanceAppropriationAgainstOn getInt(Integer advanceAppropraiation){

        AdvanceAppropriationAgainstOn advanceAppropriationAgainstOn = null;
        if (Objects.isNull(advanceAppropraiation)) {
            return INVALID;
        }
        switch (advanceAppropraiation) {
            case 1 -> advanceAppropriationAgainstOn = AdvanceAppropriationAgainstOn.PRINCIPAL;
            case 2 -> advanceAppropriationAgainstOn = AdvanceAppropriationAgainstOn.INTEREST_PRINCIPAL;
            case 0 -> advanceAppropriationAgainstOn = AdvanceAppropriationAgainstOn.INVALID;
        }
        return advanceAppropriationAgainstOn;
    }

    public static Boolean interestAndPrincipal(AdvanceAppropriationAgainstOn advanceAppropriationAgainstOn) {
        return advanceAppropriationAgainstOn.getCode().equals(AdvanceAppropriationAgainstOn.INTEREST_PRINCIPAL.getCode());
    }
}
