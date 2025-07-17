package org.vcpl.lms.portfolio.loanaccount.coolingoff.validator;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.InvalidJsonException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.portfolio.loanaccount.coolingoff.constants.CoolingOffConstants;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
public class CoolingOffDataValidator {
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList(CoolingOffConstants.COOLING_OFF_DATE,
            CoolingOffConstants.COLLECTION_FLAG, CoolingOffConstants.INTEREST_WAIVER, CoolingOffConstants.DATE_FORMAT, CoolingOffConstants.EXTERNAL_ID,
            CoolingOffConstants.INSTALLMENT_NUMBER, CoolingOffConstants.LOCALE, CoolingOffConstants.NOTE, CoolingOffConstants.PARTNER_TRANSFER_DATE,
            CoolingOffConstants.RECEIPT_REFERENCE_NUMBER, CoolingOffConstants.TRANSACTION_DATE, CoolingOffConstants.TRANSACTION_AMOUNT,
            CoolingOffConstants.PARTNER_TRANSFER_UTR, CoolingOffConstants.REPAYMENT_MODE,"triggeredBy"));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CoolingOffDataValidator(FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(CoolingOffConstants.COOLING_OFF_RESOURCE);

        final JsonElement element = this.fromApiJsonHelper.parse(json);


        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().zeroOrPositiveAmount();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        final String receiptReferenceNumber = this.fromApiJsonHelper.extractStringNamed("receiptReferenceNumber", element);
        baseDataValidator.reset().parameter("receiptReferenceNumber").value(receiptReferenceNumber).notExceedingLengthOf(40);

        final LocalDate coolingOffDate = fromApiJsonHelper.extractLocalDateNamed(CoolingOffConstants.COOLING_OFF_DATE, element);
        baseDataValidator.reset().parameter(CoolingOffConstants.COOLING_OFF_DATE).value(coolingOffDate).ignoreIfNull();

        final String coolingOffFlag = this.fromApiJsonHelper.extractStringNamed(CoolingOffConstants.COLLECTION_FLAG, element);
        baseDataValidator.reset().parameter(CoolingOffConstants.COLLECTION_FLAG).value(coolingOffFlag).ignoreIfNull();

        final BigDecimal interestWavier = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(CoolingOffConstants.INTEREST_WAIVER, element);
        baseDataValidator.reset().parameter(CoolingOffConstants.INTEREST_WAIVER).value(interestWavier).ignoreIfNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(List<ApiParameterError> dataValidationErrors) {

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
