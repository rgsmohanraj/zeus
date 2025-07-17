package org.vcpl.lms.portfolio.loanproduct.domain;


import lombok.Getter;

import java.util.Objects;

@Getter
public enum MultiplesOf {

    ONE (1,"decimal.one"),
    TEN(10,"decimal.ten"),
    HUNDRED(100,"decimal.hundred");


    private  Integer value;

    private String  code;
    MultiplesOf(Integer value, String code) {
        this.value = value;
        this.code  = code ;
    }

    public static MultiplesOf fromInt(Integer decimalPlacesHundreds) {
        if(Objects.isNull(decimalPlacesHundreds)){
            return  null;
        }
        MultiplesOf decimalRound = null;
        switch (decimalPlacesHundreds){
            case 1:
                decimalRound =  MultiplesOf.ONE;
                break;
            case 10:
                decimalRound = MultiplesOf.TEN;
                break;
            case 100:
                decimalRound = MultiplesOf.HUNDRED;
                break;
        }
        return decimalRound;
    }
}
