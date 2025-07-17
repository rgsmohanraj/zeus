package org.vcpl.lms.portfolio.loanproduct.domain;

public enum ForeclosureMethodTypes {
    PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING(1,"ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING"),
    PRINCIPAL_OUTSTANDING_INTEREST_DUE(2,"ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_DUE"),
    PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED(3,"ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED"),
    INVALID(0,"invalid");

    private final Integer value;
    private final String code;

    ForeclosureMethodTypes(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public static Boolean isPrincipalInterestOutstanding(ForeclosureMethodTypes foreclosurePos) {
        return foreclosurePos.equals(ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING);
    }

    public static Boolean isPrincipalOutstandingInterestDue(ForeclosureMethodTypes foreclosurePos) {
        return foreclosurePos.equals(ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_DUE);
    }

    public static Boolean isPrincipalOutstandingInterestAccrued(ForeclosureMethodTypes foreclosurePos) {
        return foreclosurePos.equals(ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED);
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }


    public static ForeclosureMethodTypes fromInt(final Integer selectedMethod) {

        ForeclosureMethodTypes foreclosureMethodType = null;
        switch (selectedMethod) {
            case 1:
                foreclosureMethodType = ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING;
                break;
            case 2:
                foreclosureMethodType = ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_DUE;
                break;
            case 3:
                foreclosureMethodType = ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED;
                break;
            default:
                foreclosureMethodType = ForeclosureMethodTypes.INVALID;
                break;
        }
        return foreclosureMethodType;
    }
}
