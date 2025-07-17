package org.vcpl.lms.portfolio.loanaccount.bulkupload.enums;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.util.StringUtil;

import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.BulkUploadConstants.XlsxErrorBoxTitle.*;
import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.BulkUploadConstants.XlsxErrorBoxMessage.*;

public enum DropdownType {
    CHARGE(CHARGE_TITLE,CHARGE_MESSAGE),
    OFFICE(OFFICE_TITLE,OFFICE_MESSAGE),
    GENDER(GENDER_TITLE,GENDER_MESSAGE),
    STATE(STATE_TITLE,STATE_MESSAGE),
    CITY(CITY_TITLE,CITY_MESSAGE),
    REPAYMENT_MODE(REPAYMENT_MODE_TITLE,REPAYMENT_MODE_MESSAGE),
    TRANSACTION_TYPE_PREFERENCE(TRANSACTION_TYPE_PREFERENCE_TITLE,TRANSACTION_TYPE_PREFERENCE_MESSAGE),
    DUE_DAYS(DUE_DAYS_TITLE,DUE_DAYS_MESSAGE),
    APPLICANT_TYPE(APPLICANT_TYPE_TITLE,APPLICANT_TYPE_MESSAGE),
    ACCOUNT_TYPE(ACCOUNT_TYPE_TITLE,ACCOUNT_TYPE_MESSAGE),
    COLLECTION_FLAG(COLLECTION_FLAG_TITLE,COLLECTION_FLAG_MESSAGE),
    ASSET_CLASS(ASSET_CLASS_TITLE,ASSET_CLASS_MESSAGE)
    ,NONE(StringUtils.EMPTY,StringUtils.EMPTY);

    final String errorBoxTitle;
    final String errorBoxMessage;
    DropdownType(String errorBoxTitle, String errorBoxMessage) {
        this.errorBoxTitle = errorBoxTitle;
        this.errorBoxMessage = errorBoxMessage;
    }

    public String errorBoxTitle() {
        return errorBoxTitle;
    }
    public String errorBoxMessage() {
        return errorBoxMessage;
    }
}
