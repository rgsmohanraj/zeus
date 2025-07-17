package org.vcpl.lms.portfolio.loanproduct.domain;

import lombok.Getter;

@Getter
public enum AdvanceAppropriationOn {
    RECEIPT_DATE (1,"ReceiptDate"),
    ON_DUE_DATE(2,"OnDueDate"),
    INVALID(0,"Invalid");

    private final Integer value;
    private final String code;

    AdvanceAppropriationOn(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public  static AdvanceAppropriationOn fromInt(Integer value){

        AdvanceAppropriationOn advanceAppropriationOn = null;

        switch (value){
            case 0 -> advanceAppropriationOn = AdvanceAppropriationOn.INVALID;

            case 1 -> advanceAppropriationOn = AdvanceAppropriationOn.RECEIPT_DATE;

            case 2 -> advanceAppropriationOn =  AdvanceAppropriationOn.ON_DUE_DATE;
        }
        return advanceAppropriationOn;
    }

    public static boolean isOnDueDate(Integer advanceAppropriation) {
        return AdvanceAppropriationOn.ON_DUE_DATE.equals(fromInt(advanceAppropriation));
    }
}
