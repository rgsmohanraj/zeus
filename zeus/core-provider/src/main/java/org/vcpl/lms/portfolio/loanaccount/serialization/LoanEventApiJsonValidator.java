/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.loanaccount.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.InvalidJsonException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.exception.PlatformDataIntegrityException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.portfolio.calendar.domain.Calendar;
import org.vcpl.lms.portfolio.calendar.domain.CalendarInstance;
import org.vcpl.lms.portfolio.calendar.exception.NotValidRecurringDateException;
import org.vcpl.lms.portfolio.calendar.service.CalendarUtils;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepository;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanRepaymentScheduleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vcpl.lms.useradministration.domain.AppUser;

@Component
public final class LoanEventApiJsonValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanApplicationCommandFromApiJsonHelper fromApiJsonDeserializer;
    private final LoanRepository loanRepository;

    @Autowired
    public LoanEventApiJsonValidator(final FromJsonHelper fromApiJsonHelper,
            final LoanApplicationCommandFromApiJsonHelper fromApiJsonDeserializer, final LoanRepository loanRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanRepository = loanRepository;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateDisbursement(final String json, boolean isAccountTransfer,final BigDecimal PartnerBalanceLimit) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        Set<String> disbursementParameters = null;

        if (isAccountTransfer) {
            disbursementParameters = new HashSet<>(Arrays.asList("actualDisbursementDate", "externalId", "note", "locale", "dateFormat",
                    LoanApiConstants.principalDisbursedParameterName, LoanApiConstants.emiAmountParameterName,
                    LoanApiConstants.disbursementNetDisbursalAmountParameterName));
        } else {
            disbursementParameters = new HashSet<>(Arrays.asList("actualDisbursementDate", "externalId", "note", "locale", "dateFormat",
                    "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber", "adjustRepaymentDate",
                    LoanApiConstants.principalDisbursedParameterName, LoanApiConstants.emiAmountParameterName,
                    LoanApiConstants.postDatedChecks, LoanApiConstants.disbursementNetDisbursalAmountParameterName));
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.disbursement");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate actualDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed("actualDisbursementDate", element);
        baseDataValidator.reset().parameter("actualDisbursementDate").value(actualDisbursementDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        final BigDecimal principal = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.principalDisbursedParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.principalDisbursedParameterName).value(principal).ignoreIfNull()
                .positiveAmount().isLessThanLoanAmount(PartnerBalanceLimit,principal);


        final BigDecimal netDisbursalAmount = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.disbursementNetDisbursalAmountParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.disbursementNetDisbursalAmountParameterName).value(netDisbursalAmount)
                .ignoreIfNull().positiveAmount();

        final BigDecimal emiAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.emiAmountParameterName,
                element);
        baseDataValidator.reset().parameter(LoanApiConstants.emiAmountParameterName).value(emiAmount).ignoreIfNull().positiveAmount()
                .notGreaterThanMax(principal);

        validatePaymentDetails(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateDisbursementWithPostDatedChecks(final String json, final Long loanId) {
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.disbursement");
        final Loan loan = this.loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
        final List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallment = loan.getRepaymentScheduleInstallments();

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(jsonObject);
        if (jsonObject.has("postDatedChecks") && jsonObject.get("postDatedChecks").isJsonArray()) {
            JsonArray postDatedChecks = jsonObject.get("postDatedChecks").getAsJsonArray();
            for (int i = 0; i < postDatedChecks.size(); i++) {
                final JsonObject postDatedCheck = postDatedChecks.get(i).getAsJsonObject();

                final String name = this.fromApiJsonHelper.extractStringNamed("name", postDatedCheck);
                baseDataValidator.reset().parameter("name").value(name).notNull();

                final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed("amount", postDatedCheck, locale);
                baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

                final Long accountNo = this.fromApiJsonHelper.extractLongNamed("accountNo", postDatedCheck);
                baseDataValidator.reset().parameter("accountNo").value(accountNo).notNull().positiveAmount();

                final Long checkNo = this.fromApiJsonHelper.extractLongNamed("checkNo", postDatedCheck);
                baseDataValidator.reset().parameter("checkNo").value(checkNo).notNull().positiveAmount();

                final Integer installmentId = this.fromApiJsonHelper.extractIntegerNamed("installmentId", postDatedCheck, locale);
                final List<LoanRepaymentScheduleInstallment> installmentList = loanRepaymentScheduleInstallment.stream().filter(
                        repayment -> repayment.getInstallmentNumber().equals(installmentId) && repayment.getLoan().getId().equals(loanId))
                        .collect(Collectors.toList());
                if (installmentList.size() > 1) {
                    throw new PlatformDataIntegrityException("error.repayment.redundancy", "Multiple installment data found",
                            "postDatedChecks");
                } else if (installmentList.size() == 0) {
                    throw new LoanRepaymentScheduleNotFoundException(installmentId);
                }

            }

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors);
            }
        }
    }

    public void validateDisbursementDateWithMeetingDate(final LocalDate actualDisbursementDate, final CalendarInstance calendarInstance,
            Boolean isSkipRepaymentOnFirstMonth, Integer numberOfDays) {
        if (null != calendarInstance) {
            final Calendar calendar = calendarInstance.getCalendar();
            if (!calendar.isValidRecurringDate(actualDisbursementDate, isSkipRepaymentOnFirstMonth, numberOfDays)) {
                // Disbursement date should fall on a meeting date
                final String errorMessage = "Expected disbursement date '" + actualDisbursementDate.toString()
                        + "' does not fall on a meeting date.";
                throw new NotValidRecurringDateException("loan.actual.disbursement.date", errorMessage, actualDisbursementDate.toString(),
                        calendar.getTitle());
            }
        }
    }

    public void validateTransaction(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> transactionParameters = new HashSet<>(Arrays.asList("transactionDate", "transactionAmount", "externalId", "note",
                "locale", "dateFormat", "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber","principal","interest","selfPrincipal","partnerPrincipal","selfInterestCharged","partnerInterestCharged","selfDue","partnerDue"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().zeroOrPositiveAmount();

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        baseDataValidator.reset().parameter("principal").value(principal).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal selfPrincipal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("selfPrincipal", element);
        baseDataValidator.reset().parameter("selfPrincipal").value(selfPrincipal).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal partnerPrincipal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerPrincipal", element);
        baseDataValidator.reset().parameter("partnerPrincipal").value(partnerPrincipal).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal interest = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interest", element);
        baseDataValidator.reset().parameter("interest").value(interest).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal selfInterestCharged = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("selfInterestCharged", element);
        baseDataValidator.reset().parameter("selfInterestCharged").value(selfInterestCharged).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal partnerInterestCharged = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerInterestCharged", element);
        baseDataValidator.reset().parameter("partnerInterestCharged").value(partnerInterestCharged).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal selfDue = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("selfDue", element);
        baseDataValidator.reset().parameter("selfDue").value(selfDue).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal partnerDue = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerDue", element);
        baseDataValidator.reset().parameter("partnerDue").value(partnerDue).ignoreIfNull().zeroOrPositiveAmount();


        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateNewRepaymentTransaction(final String json, AppUser appUser) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> transactionParameters = new HashSet<>(
                Arrays.asList("transactionDate", "transactionAmount", "externalId", "note", "locale", "dateFormat", "paymentTypeId",
                        "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber", "loanId","principal","interest","selfPrincipal",
                        "partnerPrincipal","selfInterestCharged","partnerInterestCharged","selfDue","partnerDue","installmentNumber","receiptReferenceNumber"
                        ,"partnerTransferUtr","partnerTransferDate","repaymentModeId","repaymentMode","triggeredBy","interestWaiver","coolingOffDate","collectionFlag"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().zeroOrPositiveAmount();
        if(transactionAmount.doubleValue()==0  && !appUser.isSystemUser()){
            String errorMessage = "Transaction Amount Should be greater than 0";
            throw new InvalidLoanStateTransitionException("transaction", "amount.should.be.greater.than 0  {}",errorMessage);
        }

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        baseDataValidator.reset().parameter("principal").value(principal).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal selfPrincipal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("selfPrincipal", element);
        baseDataValidator.reset().parameter("selfPrincipal").value(selfPrincipal).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal partnerPrincipal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerPrincipal", element);
        baseDataValidator.reset().parameter("partnerPrincipal").value(partnerPrincipal).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal interest = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interest", element);
        baseDataValidator.reset().parameter("interest").value(interest).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal selfInterestCharged = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("selfInterestCharged", element);
        baseDataValidator.reset().parameter("selfInterestCharged").value(selfInterestCharged).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal partnerInterestCharged = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerInterestCharged", element);
        baseDataValidator.reset().parameter("partnerInterestCharged").value(partnerInterestCharged).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal selfDue = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("selfDue", element);
        baseDataValidator.reset().parameter("selfDue").value(selfDue).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal partnerDue = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerDue", element);
        baseDataValidator.reset().parameter("partnerDue").value(partnerDue).ignoreIfNull().zeroOrPositiveAmount();

//        final int triggerd = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("triggeredBy", element);
//        baseDataValidator.reset().parameter("triggeredBy").value(triggerd).ignoreIfNull().zeroOrPositiveAmount();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);


        final String receiptReferenceNumber = this.fromApiJsonHelper.extractStringNamed("receiptReferenceNumber", element);
        baseDataValidator.reset().parameter("receiptReferenceNumber").value(receiptReferenceNumber).notExceedingLengthOf(40);

//        final String partnerTransferUtr = this.fromApiJsonHelper.extractStringNamed("partnerTransferUtr", element);
//        baseDataValidator.reset().parameter("partnerTransferUtr").value(partnerTransferUtr).ignoreIfNull().notExceedingLengthOf(40);
//
//        final LocalDate partnerTransferDate = this.fromApiJsonHelper.extractLocalDateNamed("partnerTransferDate", element);
//        baseDataValidator.reset().parameter("partnerTransferDate").value(partnerTransferDate).ignoreIfNull();

        if (this.fromApiJsonHelper.parameterExists(LoanRepaymentConstants.repaymentModeIdParamName, element)) {
            final Integer repaymentModeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanRepaymentConstants.repaymentModeIdParamName, element);
            baseDataValidator.reset().parameter(LoanRepaymentConstants.repaymentModeIdParamName).value(repaymentModeId).ignoreIfNull();
        }

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateRepaymentDateWithMeetingDate(final LocalDate repaymentDate, final CalendarInstance calendarInstance) {
        if (null != calendarInstance) {
            final Calendar calendar = calendarInstance.getCalendar();
            if (calendar != null && repaymentDate != null) {
                // Disbursement date should fall on a meeting date
                if (!CalendarUtils.isValidRedurringDate(calendar.getRecurrence(), calendar.getStartDateLocalDate(), repaymentDate)) {
                    final String errorMessage = "Transaction date '" + repaymentDate.toString() + "' does not fall on a meeting date.";
                    throw new NotValidRecurringDateException("loan.transaction.date", errorMessage, repaymentDate.toString(),
                            calendar.getTitle());
                }

            }
        }
    }

    private void validatePaymentDetails(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        // Validate all string payment detail fields for max length
        final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("paymentTypeId", element);
        baseDataValidator.reset().parameter("paymentTypeId").value(paymentTypeId).ignoreIfNull().integerGreaterThanZero();
        final Set<String> paymentDetailParameters = new HashSet<>(
                Arrays.asList("accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber"));
        for (final String paymentDetailParameterName : paymentDetailParameters) {
            final String paymentDetailParameterValue = this.fromApiJsonHelper.extractStringNamed(paymentDetailParameterName, element);
            baseDataValidator.reset().parameter(paymentDetailParameterName).value(paymentDetailParameterValue).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }
    }

    public void validateTransactionWithNoAmount(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(
                Arrays.asList("transactionDate", "note", "locale", "dateFormat", "writeoffReasonId"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateAddLoanCharge(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(
                Arrays.asList("chargeId", "amount", "dueDate", "locale", "dateFormat", "externalId"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanCharge");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", element);
        baseDataValidator.reset().parameter("chargeId").value(chargeId).notNull().integerGreaterThanZero();

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

        if (this.fromApiJsonHelper.parameterExists("dueDate", element)) {
            final LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed("dueDate", element);
            baseDataValidator.reset().parameter("dueDate").value(dueDate).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists("externalId", element)) {
            final LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed("externalId", element);
            baseDataValidator.reset().parameter("externalId").value(dueDate).notBlank();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }


    public void validateUpdateOfLoanCharge(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList("amount", "dueDate", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanCharge");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

        if (this.fromApiJsonHelper.parameterExists("dueDate", element)) {
            this.fromApiJsonHelper.extractLocalDateNamed("dueDate", element);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUpdateOfLoanOfficer(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(
                Arrays.asList("assignmentDate", "fromLoanOfficerId", "toLoanOfficerId", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanOfficer");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long toLoanOfficerId = this.fromApiJsonHelper.extractLongNamed("toLoanOfficerId", element);
        baseDataValidator.reset().parameter("toLoanOfficerId").value(toLoanOfficerId).notNull().integerGreaterThanZero();

        final String assignmentDateStr = this.fromApiJsonHelper.extractStringNamed("assignmentDate", element);
        baseDataValidator.reset().parameter("assignmentDate").value(assignmentDateStr).notBlank();

        if (!StringUtils.isBlank(assignmentDateStr)) {
            final LocalDate assignmentDate = this.fromApiJsonHelper.extractLocalDateNamed("assignmentDate", element);
            baseDataValidator.reset().parameter("assignmentDate").value(assignmentDate).notNull();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForBulkLoanReassignment(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> supportedParameters = new HashSet<>(
                Arrays.asList("assignmentDate", "fromLoanOfficerId", "toLoanOfficerId", "loans", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanOfficer");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate assignmentDate = this.fromApiJsonHelper.extractLocalDateNamed("assignmentDate", element);
        baseDataValidator.reset().parameter("assignmentDate").value(assignmentDate).notNull();
        final Long fromLoanOfficerId = this.fromApiJsonHelper.extractLongNamed("fromLoanOfficerId", element);
        baseDataValidator.reset().parameter("fromLoanOfficerId").value(fromLoanOfficerId).notNull().longGreaterThanZero();
        final Long toLoanOfficerId = this.fromApiJsonHelper.extractLongNamed("toLoanOfficerId", element);
        baseDataValidator.reset().parameter("toLoanOfficerId").value(toLoanOfficerId).notNull().longGreaterThanZero();
        final String[] loans = this.fromApiJsonHelper.extractArrayNamed("loans", element);
        baseDataValidator.reset().parameter("loans").value(loans).arrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChargePaymentTransaction(final String json, final boolean isChargeIdIncluded) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        Set<String> transactionParameters = null;
        if (isChargeIdIncluded) {
            transactionParameters = new HashSet<>(
                    Arrays.asList("transactionDate", "locale", "dateFormat", "chargeId", "dueDate", "installmentNumber"));
        } else {
            transactionParameters = new HashSet<>(Arrays.asList("transactionDate", "locale", "dateFormat", "dueDate", "installmentNumber"));
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("loan.charge.payment.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        if (isChargeIdIncluded) {
            final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", element);
            baseDataValidator.reset().parameter("chargeId").value(chargeId).notNull().integerGreaterThanZero();
        }
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();
        final Integer installmentNumber = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("installmentNumber", element);
        baseDataValidator.reset().parameter("installmentNumber").value(installmentNumber).ignoreIfNull().integerGreaterThanZero();


        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateInstallmentChargeTransaction(final String json) {

        if (StringUtils.isBlank(json)) {
            return;
        }
        Set<String> transactionParameters = new HashSet<>(Arrays.asList("dueDate", "locale", "dateFormat", "installmentNumber"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("loan.charge.waive.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Integer installmentNumber = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("installmentNumber", element);
        baseDataValidator.reset().parameter("installmentNumber").value(installmentNumber).ignoreIfNull().integerGreaterThanZero();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateUpdateDisbursementDateAndAmount(final String json, LoanDisbursementDetails loanDisbursementDetails) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(
                Arrays.asList("locale", "dateFormat", LoanApiConstants.disbursementDataParameterName,
                        LoanApiConstants.approvedLoanAmountParameterName, LoanApiConstants.updatedDisbursementDateParameterName,
                        LoanApiConstants.updatedDisbursementPrincipalParameterName, LoanApiConstants.disbursementDateParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.update.disbursement");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate actualDisbursementDate = this.fromApiJsonHelper
                .extractLocalDateNamed(LoanApiConstants.disbursementDateParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.disbursementDateParameterName).value(actualDisbursementDate).notNull();

        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        final BigDecimal principal = this.fromApiJsonHelper
                .extractBigDecimalNamed(LoanApiConstants.updatedDisbursementPrincipalParameterName, element, locale);
        baseDataValidator.reset().parameter(LoanApiConstants.disbursementPrincipalParameterName).value(principal).notNull();

        final BigDecimal approvedPrincipal = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.approvedLoanAmountParameterName,
                element, locale);
        if (loanDisbursementDetails.actualDisbursementDate() != null) {
            baseDataValidator.reset().parameter(LoanApiConstants.disbursementDateParameterName)
                    .failWithCode(LoanApiConstants.ALREADY_DISBURSED);
        }

        fromApiJsonDeserializer.validateLoanMultiDisbursementDate(element, baseDataValidator, actualDisbursementDate, approvedPrincipal);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateNewRefundTransaction(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> transactionParameters = new HashSet<>(Arrays.asList("transactionDate", "transactionAmount", "externalId", "note",
                "locale", "dateFormat", "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber","principal","interest","selfPrincipal","partnerPrincipal","selfInterestCharged","partnerInterestCharged","selfDue","partnerDue"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, transactionParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().positiveAmount();

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        baseDataValidator.reset().parameter("principal").value(principal).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal selfPrincipal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("selfPrincipal", element);
        baseDataValidator.reset().parameter("selfPrincipal").value(selfPrincipal).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal partnerPrincipal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerPrincipal", element);
        baseDataValidator.reset().parameter("partnerPrincipal").value(partnerPrincipal).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal interest = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interest", element);
        baseDataValidator.reset().parameter("interest").value(interest).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal selfInterestCharged = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("selfInterestCharged", element);
        baseDataValidator.reset().parameter("selfInterestCharged").value(selfInterestCharged).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal partnerInterestCharged = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerInterestCharged", element);
        baseDataValidator.reset().parameter("partnerInterestCharged").value(partnerInterestCharged).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal selfDue = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("selfDue", element);
        baseDataValidator.reset().parameter("selfDue").value(selfDue).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal partnerDue = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerDue", element);
        baseDataValidator.reset().parameter("partnerDue").value(partnerDue).ignoreIfNull().zeroOrPositiveAmount();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        final String externalId = this.fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).notExceedingLengthOf(100);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateLoanForeclosure(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> foreclosureParameters = new HashSet<>(Arrays.asList("transactionDate", "note", "locale", "dateFormat","interestAccruedAfterDeath","dueDate",
                "receiptReferenceNumber","partnerTransferUtr","partnerTransferDate","repaymentMode","interestWaiver","coolingOffDate","installmentNumber","transactionAmount","triggeredBy","collectionFlag"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, foreclosureParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        validatePaymentDetails(baseDataValidator, element);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}
