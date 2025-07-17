package org.vcpl.lms.portfolio.loanaccount.loanschedule.domain;

import lombok.Getter;
import org.vcpl.lms.portfolio.common.domain.DaysInYearType;

@Getter
public enum PmtCalcEnum {


    PMT_WITH_MONTHLY_INTEREST_RATE(1, "emiCalcus.pmt_with_monthly_interest_rate"),
    PMT_WITH_YEARLY_INTEREST_RATE(2,"emiCalcus.pmt_with_yearly_interest_rate"),

    INVALID(0,"emiCalcus.pmt_with_invalid");

    private final Integer value;
    private final String code;

    PmtCalcEnum(Integer value, String code) {
        this.value= value;
        this.code = code;
    }


    public static PmtCalcEnum getInt(Integer emiCalcus){

        PmtCalcEnum emiCalcusEnum = null;

        switch (emiCalcus){

            case 0 -> emiCalcusEnum = PmtCalcEnum.INVALID;

            case 1 ->  emiCalcusEnum= PmtCalcEnum.PMT_WITH_MONTHLY_INTEREST_RATE;

            case 2 -> emiCalcusEnum = PmtCalcEnum.PMT_WITH_YEARLY_INTEREST_RATE;

        }
        return emiCalcusEnum;
    }

    public boolean isYearly() {
       return PmtCalcEnum.PMT_WITH_YEARLY_INTEREST_RATE.getValue().equals(this.value);
    }
}
