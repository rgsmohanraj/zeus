package org.vcpl.lms.portfolio.loanaccount.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.InvalidJsonException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeTransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ChargeCollectionApiJsonValidator {
    private final FromJsonHelper jsonHelper;
    private static class ChargeCollectionParameters {
        public static final String LOAN_ID = "loanId";
        public static final String EXTERNAL_ID = "externalId";
        public static final String LOAN_ACCOUNT_NO = "loanAccountNo";
        public static final String INSTALLMENT = "installment";
        public static final String CHARGE_NAME = "chargeName";
        public static final String AMOUNT = "amount";
        public static final String CHARGE_ID = "chargeId";
        public static final String TRANSACTION_DATE = "transactionDate";
        public static final String PARTNER_TRANSFER_DATE = "partnerTransferDate";
        public static final String RECEIPT_REFERENCE_NUMBER = "receiptReferenceNumber";
        public static final String PARTNER_TRANSFER_UTR = "partnerTransferUtr";
        public static final String REPAYMENT_MODE = "repaymentMode";
        public static final String LOCALE = "locale";
        public static final String DATE_FORMAT = "dateFormat";
        public static final String DUE_DATE = "dueDate";
    }

    private static final Set<String> CHARGE_TRANSACTION_PARAMS = Set.of(ChargeCollectionParameters.EXTERNAL_ID, ChargeCollectionParameters.LOAN_ID,
            ChargeCollectionParameters.LOAN_ACCOUNT_NO, ChargeCollectionParameters.INSTALLMENT, ChargeCollectionParameters.CHARGE_ID,
            ChargeCollectionParameters.AMOUNT, ChargeCollectionParameters.TRANSACTION_DATE, ChargeCollectionParameters.PARTNER_TRANSFER_DATE,
            ChargeCollectionParameters.PARTNER_TRANSFER_UTR, ChargeCollectionParameters.RECEIPT_REFERENCE_NUMBER, ChargeCollectionParameters.REPAYMENT_MODE,
            ChargeCollectionParameters.LOCALE, ChargeCollectionParameters.DATE_FORMAT);
    private static final Set<String> CREATION_PARAMS = Set.of(ChargeCollectionParameters.LOAN_ID,
            ChargeCollectionParameters.CHARGE_ID, ChargeCollectionParameters.AMOUNT,ChargeCollectionParameters.LOAN_ACCOUNT_NO,
            ChargeCollectionParameters.TRANSACTION_DATE, ChargeCollectionParameters.PARTNER_TRANSFER_DATE,
            ChargeCollectionParameters.PARTNER_TRANSFER_UTR, ChargeCollectionParameters.RECEIPT_REFERENCE_NUMBER, ChargeCollectionParameters.REPAYMENT_MODE,
            ChargeCollectionParameters.LOCALE, ChargeCollectionParameters.DATE_FORMAT);

    public ChargeTransactionRequest chargeTransaction(final String json, final boolean isWaiverTransaction) {
        if (StringUtils.isBlank(json)) throw new InvalidJsonException();

        this.jsonHelper.checkForUnsupportedParameters(new TypeToken<Map<String, Object>>() {}.getType(),
                json, CHARGE_TRANSACTION_PARAMS);
        final DataValidatorBuilder chargeRequestValidator = new DataValidatorBuilder(new ArrayList<>())
                .resource(isWaiverTransaction
                        ? "loan.charge.waiver.transaction"
                        : "loan.charge.payment.transaction");

        final JsonElement element = this.jsonHelper.parse(json);
        ChargeTransactionRequest chargeTransactionRequest = new ChargeTransactionRequest();
        validateAndSetLoanAccountNo(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetExternalId(chargeRequestValidator,element,chargeTransactionRequest);
        validateAndSetChargeId(chargeRequestValidator,element, chargeTransactionRequest);
        if( this.jsonHelper.parameterExists(ChargeCollectionParameters.INSTALLMENT,element) ) {
            validateAndSetInstallment(chargeRequestValidator,element,chargeTransactionRequest);
        }
        validateAndSetChargeAmount(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetTransactionDate(chargeRequestValidator, element, chargeTransactionRequest, true);
        validateAndSetPartnerTransferDate(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetPartnerTransferUtr(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetReceiptReferenceNumber(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetRepaymentMode(chargeRequestValidator, element, chargeTransactionRequest);
        throwOnValidationFailure(chargeRequestValidator);
        return chargeTransactionRequest;
    }


    public ChargeTransactionRequest createCharge(final String json) {

        this.jsonHelper.checkForUnsupportedParameters(new TypeToken<Map<String, Object>>() {}.getType(),
                json, CHARGE_TRANSACTION_PARAMS);
        final DataValidatorBuilder chargeRequestValidator = new DataValidatorBuilder(new ArrayList<>())
                .resource("loan.charge.creation");
        final JsonElement element = this.jsonHelper.parse(json);
        ChargeTransactionRequest chargeTransactionRequest = new ChargeTransactionRequest();
        validateAndSetLoanId(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetChargeId(chargeRequestValidator,element, chargeTransactionRequest);
        validateAndSetChargeAmount(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetTransactionDate(chargeRequestValidator, element, chargeTransactionRequest,false);
        validateAndSetPartnerTransferDate(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetPartnerTransferUtr(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetReceiptReferenceNumber(chargeRequestValidator, element, chargeTransactionRequest);
        validateAndSetRepaymentMode(chargeRequestValidator, element, chargeTransactionRequest);
        return chargeTransactionRequest;
    }

    private void throwOnValidationFailure(final DataValidatorBuilder chargeRequestValidator) {
        if (!chargeRequestValidator.getDataValidationErrors().isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    chargeRequestValidator.getDataValidationErrors());
        }
    }
    private void validateAndSetTransactionDate(DataValidatorBuilder chargeRequestValidator, JsonElement element,
                                         final ChargeTransactionRequest chargeTransactionRequest, final boolean doValidate) {
        LocalDate transactionDate = this.jsonHelper.extractLocalDateNamed(ChargeCollectionParameters.TRANSACTION_DATE, element);
        if(doValidate) {
            chargeRequestValidator.reset().parameter(ChargeCollectionParameters.TRANSACTION_DATE).value(transactionDate)
                    .notBlank();
        } else {
            chargeRequestValidator.reset().parameter(ChargeCollectionParameters.TRANSACTION_DATE).ignoreIfNull()
                    .value(transactionDate);
        }

        if(Objects.nonNull(transactionDate)) {
            chargeRequestValidator.parameter(ChargeCollectionParameters.TRANSACTION_DATE)
                    .value(transactionDate)
                    .validateDateGreaterThanCurrentDate();
            chargeTransactionRequest.setTransactionDate(transactionDate);
        }
    }

    private void validateAndSetPartnerTransferDate(DataValidatorBuilder chargeRequestValidator, JsonElement element,
                                               final ChargeTransactionRequest chargeTransactionRequest) {
        LocalDate partnerTransferDate = this.jsonHelper.extractLocalDateNamed(ChargeCollectionParameters.PARTNER_TRANSFER_DATE, element);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.PARTNER_TRANSFER_DATE).ignoreIfNull().value(partnerTransferDate);
        if(Objects.nonNull(partnerTransferDate)) {
            chargeTransactionRequest.setPartnerTransferDate(partnerTransferDate);
        }
    }

    private void validateAndSetChargeAmount(final DataValidatorBuilder chargeRequestValidator,final JsonElement element,
                                       final ChargeTransactionRequest chargeTransactionRequest) {
        final Locale locale = this.jsonHelper.extractLocaleParameter(element.getAsJsonObject());
        BigDecimal amount = this.jsonHelper.extractBigDecimalNamed(ChargeCollectionParameters.AMOUNT,element,locale);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.AMOUNT).value(amount)
                .notBlank().positiveAmount();
        chargeTransactionRequest.setAmount(amount);

    }

    private void validateAndSetInstallment(final DataValidatorBuilder chargeRequestValidator,final JsonElement element,
                                   final ChargeTransactionRequest chargeTransactionRequest) {
        final Locale locale = this.jsonHelper.extractLocaleParameter(element.getAsJsonObject());
        Integer installment = this.jsonHelper.extractIntegerNamed(ChargeCollectionParameters.INSTALLMENT,element,locale);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.INSTALLMENT).value(installment)
                .notBlank();
        chargeTransactionRequest.setInstallment(installment);
    }

    private void validateAndSetChargeId(final DataValidatorBuilder chargeRequestValidator,final JsonElement element,
                             final ChargeTransactionRequest chargeTransactionRequest) {
        Long chargeId = this.jsonHelper.extractLongNamed(ChargeCollectionParameters.CHARGE_ID,element);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.CHARGE_ID).value(chargeId)
                .notBlank();
        chargeTransactionRequest.setChargeId(chargeId);
    }

    private void validateAndSetLoanAccountNo(final DataValidatorBuilder chargeRequestValidator,final JsonElement element,
                                        final ChargeTransactionRequest chargeTransactionRequest) {
        String loanAccountNo = this.jsonHelper.extractStringNamed(ChargeCollectionParameters.LOAN_ACCOUNT_NO,element);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.LOAN_ACCOUNT_NO).value(loanAccountNo)
                .notBlank();
        chargeTransactionRequest.setLoanAccountNo(loanAccountNo);
    }
    private void validateAndSetLoanId(final DataValidatorBuilder chargeRequestValidator,final JsonElement element,
                                             final ChargeTransactionRequest chargeTransactionRequest) {
        Long loanId = this.jsonHelper.extractLongNamed(ChargeCollectionParameters.LOAN_ID,element);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.LOAN_ID).value(loanId)
                .notBlank();
        chargeTransactionRequest.setLoanId(loanId);
    }
    private void validateAndSetExternalId(final DataValidatorBuilder chargeRequestValidator,final JsonElement element,
                                 final ChargeTransactionRequest chargeTransactionRequest) {
        String externalId = this.jsonHelper.extractStringNamed(ChargeCollectionParameters.EXTERNAL_ID,element);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.EXTERNAL_ID).value(externalId)
                .notBlank();
        chargeTransactionRequest.setExternalId(externalId);
    }

    private void validateAndSetPartnerTransferUtr(final DataValidatorBuilder chargeRequestValidator,final JsonElement element,
                                        final ChargeTransactionRequest chargeTransactionRequest) {
        String partnerTransferUtr = this.jsonHelper.extractStringNamed(ChargeCollectionParameters.PARTNER_TRANSFER_UTR,element);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.PARTNER_TRANSFER_UTR)
                .ignoreIfNull().value(partnerTransferUtr).notExceedingLengthOf(40);
        if(Objects.nonNull(partnerTransferUtr)) {
            chargeTransactionRequest.setPartnerTransferUtr(partnerTransferUtr);
        }
    }
    private void validateAndSetReceiptReferenceNumber(final DataValidatorBuilder chargeRequestValidator,final JsonElement element,
                                                  final ChargeTransactionRequest chargeTransactionRequest) {
        String receiptReferenceNumber = this.jsonHelper.extractStringNamed(ChargeCollectionParameters.RECEIPT_REFERENCE_NUMBER,element);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.RECEIPT_REFERENCE_NUMBER)
                .ignoreIfNull().value(receiptReferenceNumber).notExceedingLengthOf(40);
        if(Objects.nonNull(receiptReferenceNumber)) {
            chargeTransactionRequest.setReceiptReferenceNumber(receiptReferenceNumber);
        }
    }

    private void validateAndSetRepaymentMode(final DataValidatorBuilder chargeRequestValidator,final JsonElement element,
                                                      final ChargeTransactionRequest chargeTransactionRequest) {
        String repaymentMode = this.jsonHelper.extractStringNamed(ChargeCollectionParameters.REPAYMENT_MODE,element);
        chargeRequestValidator.reset().parameter(ChargeCollectionParameters.REPAYMENT_MODE)
                .ignoreIfNull().value(repaymentMode);
        if(Objects.nonNull(repaymentMode)) {
            chargeTransactionRequest.setRepaymentMode(repaymentMode);
        }
    }
}
