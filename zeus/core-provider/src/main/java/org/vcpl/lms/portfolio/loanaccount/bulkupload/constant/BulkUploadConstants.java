package org.vcpl.lms.portfolio.loanaccount.bulkupload.constant;

public class BulkUploadConstants {
        public final static String DB_DATE_FORMAT = "yyyy-MM-dd";
        public final static String LOCALE = "en";
        public static final String INTEREST_WAIVER="InterestWaiver";
        public static final String COOLING_OF_DATE="Cooling Off Date";

    public final static class XlsxErrorBoxTitle {
        public final static String OFFICE_TITLE = "Invalid Office";
        public final static String CHARGE_TITLE = "Invalid Charge";
        public final static String GENDER_TITLE = "Invalid Gender";
        public final static String STATE_TITLE = "Invalid State";
        public final static String CITY_TITLE = "Invalid City";
        public final static String REPAYMENT_MODE_TITLE = "Invalid Repayment Mode";
        public final static String TRANSACTION_TYPE_PREFERENCE_TITLE = "Invalid Transaction Type Preference";
        public final static String DUE_DAYS_TITLE = "Invalid Due Day";
        public final static String APPLICANT_TYPE_TITLE = "Invalid Applicant Type";
        public final static String ACCOUNT_TYPE_TITLE = "Invalid Account Type";
        public final static String INTEREST_RATE_TITLE = "Invalid Interest Rate";
        public final static String PRINCIPLE_AMOUNT_TITLE = "Invalid Principle Amount";
        public final static String LOAN_TERM_TITLE = "Invalid Loan Term";
        public final static String ASSET_CLASS_TITLE = "Invalid Account Type";
        public static final String COLLECTION_FLAG_TITLE = "Invalid Collection Flag Mode";
    }

    public final static class XlsxErrorBoxMessage {
        public final static String OFFICE_MESSAGE = "Please choose only values from 'Entity' dropdown";
        public final static String CHARGE_MESSAGE = "Please choose only values from 'Charge' dropdown";
        public final static String GENDER_MESSAGE = "Please choose only values from 'Gender' dropdown";
        public final static String STATE_MESSAGE = "Please choose only values from 'State' dropdown";
        public final static String CITY_MESSAGE = "Please choose only values from 'City' dropdown";
        public final static String REPAYMENT_MODE_MESSAGE = "Please choose only values from 'Repayment Mode' dropdown";
        public final static String TRANSACTION_TYPE_PREFERENCE_MESSAGE = "Please choose only values from 'Transaction Type Preference' dropdown";
        public final static String DUE_DAYS_MESSAGE = "Please choose only values from 'Due Day' dropdown";
        public final static String APPLICANT_TYPE_MESSAGE = "Please choose only values from 'Applicant Type' dropdown";
        public final static String ACCOUNT_TYPE_MESSAGE = "Please choose only values from 'Applicant Type' dropdown";
        public final static String MIN_MAX_MESSAGE = "Please choose only values between :min and :max";
        public final static String ASSET_CLASS_MESSAGE = "Please choose only values from 'Asset Class' dropdown";
        public static final  String COLLECTION_FLAG_MESSAGE="Please choose only values from 'Collection Flag' dropdown";
    }
}
