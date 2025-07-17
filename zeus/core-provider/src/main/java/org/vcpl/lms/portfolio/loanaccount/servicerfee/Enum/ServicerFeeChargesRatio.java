package org.vcpl.lms.portfolio.loanaccount.servicerfee.Enum;

import lombok.Getter;

@Getter
public enum ServicerFeeChargesRatio {
    FIXED_SPLIT(1,"fixed_split"),

    DYNAMIC_SPLIT(2,"dynamic_split"),
    INVALID(0,"invalid");

    private  Integer value;
    private  String code;

    ServicerFeeChargesRatio(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public  static ServicerFeeChargesRatio getServicerFeeChargesRatio(Integer value){

        ServicerFeeChargesRatio servicerFeeChargesRatio =null;
        switch (value){

            case 1 -> servicerFeeChargesRatio =  ServicerFeeChargesRatio.FIXED_SPLIT;
            case 2 -> servicerFeeChargesRatio =  ServicerFeeChargesRatio.DYNAMIC_SPLIT;
            default -> servicerFeeChargesRatio = ServicerFeeChargesRatio.INVALID;
        }
        return servicerFeeChargesRatio;
    }

    public static boolean isFixedSplit(ServicerFeeChargesRatio servicerFeeChargesRatio) {
        return servicerFeeChargesRatio.getCode().equals(ServicerFeeChargesRatio.FIXED_SPLIT.getCode());
    }
}
