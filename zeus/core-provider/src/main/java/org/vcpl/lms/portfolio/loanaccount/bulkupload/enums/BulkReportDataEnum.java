package org.vcpl.lms.portfolio.loanaccount.bulkupload.enums;

import lombok.Getter;

@Getter
public enum BulkReportDataEnum {
     BULKCIENTLOANSCREATION(1,"bulk_client_loans_creation"),
     BULKLOANSREPAYMENT (2,"bulk_loans_repayment"),

     BULKCHARGEREPAYMENT(3,"bulk_charge_repayment");






    private final Integer value;
    private final String code;

    BulkReportDataEnum(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

}

