package org.vcpl.lms.portfolio.loanaccount.bulkupload.enums;

import org.apache.commons.lang3.StringUtils;

import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.BulkUploadConstants.XlsxErrorBoxTitle.*;
import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.BulkUploadConstants.XlsxErrorBoxMessage.*;

public enum ColumnValidation {
    INTEREST_RATE(INTEREST_RATE_TITLE,MIN_MAX_MESSAGE),
    PRINCIPLE_AMOUNT(PRINCIPLE_AMOUNT_TITLE,MIN_MAX_MESSAGE),
    LOAN_TERM(LOAN_TERM_TITLE,MIN_MAX_MESSAGE),
    NONE(StringUtils.EMPTY,StringUtils.EMPTY);

    final String errorBoxTitle;
    final String errorBoxMessage;
    ColumnValidation(String errorBoxTitle, String errorBoxMessage) {
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
