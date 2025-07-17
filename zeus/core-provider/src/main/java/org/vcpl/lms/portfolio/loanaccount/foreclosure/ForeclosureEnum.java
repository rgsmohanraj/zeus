package org.vcpl.lms.portfolio.loanaccount.foreclosure;

public enum ForeclosureEnum {
    FORECLOSURE_ON_BEFORE_DUEDATE,
    FORECLOSURE_ON_DUEDATE,
    FORCLOSURE_INTEREST_SHORT_PAID_ON_BEFORE_DUEDATE,
    FORCLOSURE_INTEREST_SHORT_PAID_ON_DUEDATE,
    FORCLOSURE_INTEREST_SHORT_PAID_AFTER_DUEDATE,
    FORECLOSURE_INTEREST_FULLY_PAID_BEFORE_DUE_DATE,
    FORECLOSURE_INTEREST_FULLY_PAID_ON_DUE_DATE,
    FORECLOSURE_INTEREST_FULLY_PAID_ON_AFTER_DUE_DATE,

    FORCLOSURE_PRINCIPAL_SHORT_PAID_BEFORE_DUEDATE,
    FORCLOSURE_PRINCIPAL_SHORT_PAID_ON_DUEDATE,
    FORCLOSURE_PRINCIPAL_SHORT_PAID_AFTER_DUEDATE,

    FORECLOSURE_EMI_FULLY_PAID_ON_BEFORE_DUE_DATE,
    FORECLOSURE_EMI_FULLY_PAID_ON_DUEDATE,
    FORECLOSURE_EMI_FULLY_PAID_ON_AFTER_DUEDATE,
    FORECLOSURE_1MONTH_OVERDUE,
    FORECLOSURE_2MONTH_OVERDUE,

    FORECLOSURE_ADVANCE_BEFORE_DUEDATE,
    FORECLOSURE_ADVANCE_ON_DUEDATE,
    FORECLOSURE_ADVANCE_AFTER_DUEDATE,
    FORECLOSURE_METHODS,
    INVALID;

    public boolean isAfterDueDate(ForeclosureEnum foreclosureEnum) {
        return foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_1MONTH_OVERDUE)
                || foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_2MONTH_OVERDUE)
                ||foreclosureEnum.equals(ForeclosureEnum.FORCLOSURE_INTEREST_SHORT_PAID_AFTER_DUEDATE)
                || foreclosureEnum.equals(FORCLOSURE_PRINCIPAL_SHORT_PAID_AFTER_DUEDATE);
    }
    public boolean notIn(ForeclosureEnum foreclosureEnum) {
        return !foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_EMI_FULLY_PAID_ON_BEFORE_DUE_DATE)
                && !foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_1MONTH_OVERDUE)
                && !foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_2MONTH_OVERDUE)
                && !foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_ADVANCE_BEFORE_DUEDATE);
    }

    public boolean isBackdateEmiFullPaid(ForeclosureEnum foreclosureEnum) {
        return ForeclosureEnum.FORECLOSURE_EMI_FULLY_PAID_ON_AFTER_DUEDATE.equals(foreclosureEnum);
    }

    public boolean isBackdateEmiFullPaidBeforeDueDate(ForeclosureEnum foreclosureEnum) {
        return ForeclosureEnum.FORECLOSURE_EMI_FULLY_PAID_ON_BEFORE_DUE_DATE.equals(foreclosureEnum)
                || ForeclosureEnum.FORECLOSURE_ADVANCE_BEFORE_DUEDATE.equals(foreclosureEnum);
    }
}
