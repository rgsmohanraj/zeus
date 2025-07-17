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
package org.vcpl.lms.infrastructure.core.data;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.quartz.CronExpression;
import org.springframework.util.ObjectUtils;
import org.vcpl.lms.portfolio.loanproduct.LoanProductConstants;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductFeeData;
import org.vcpl.lms.portfolio.loanproduct.domain.MultiplesOf;

import javax.xml.crypto.Data;

import static java.lang.Integer.sum;

public class DataValidatorBuilder {

    public static final String VALID_INPUT_SEPERATOR = "_";
    private final List<ApiParameterError> dataValidationErrors;
    private String resource;
    private String parameter;
    private String arrayPart;
    private Integer arrayIndex;
    private Object value;
    private boolean ignoreNullValue = false;

    /**
     * Default constructor used to start a new "validation chain".
     */
    public DataValidatorBuilder() {
        this(new ArrayList<>());
    }

    /**
     * Constructor used to "continue" an existing "validation chain".
     *
     * @param dataValidationErrors
     *            an existing list of {@link ApiParameterError} to add new validation errors to
     */

    public DataValidatorBuilder(final List<ApiParameterError> dataValidationErrors) {
        this.dataValidationErrors = dataValidationErrors;
    }

    public DataValidatorBuilder reset() {
        return new DataValidatorBuilder(this.dataValidationErrors).resource(this.resource);
    }

    public void merge(DataValidatorBuilder other) {
        dataValidationErrors.addAll(other.dataValidationErrors);
    }

    public boolean hasError() {
        return !dataValidationErrors.isEmpty();
    }

    public List<ApiParameterError> getDataValidationErrors() {
        return dataValidationErrors;
    }

    public DataValidatorBuilder resource(final String resource) {
        this.resource = resource;
        return this;
    }

    public DataValidatorBuilder parameter(final String parameter) {
        this.parameter = parameter;
        return this;
    }

    public DataValidatorBuilder parameterAtIndexArray(final String arrayPart, final Integer arrayIndex) {
        this.arrayPart = arrayPart;
        this.arrayIndex = arrayIndex;
        return this;
    }

    public DataValidatorBuilder value(final Object value) {
        this.value = value;
        return this;
    }

    public DataValidatorBuilder ignoreIfNull() {
        this.ignoreNullValue = true;
        return this;
    }

    public DataValidatorBuilder andNotBlank(final String linkedParameterName, final String linkedValue) {
        if (this.value == null && linkedValue == null && this.ignoreNullValue) {
            return this;
        }

        if (StringUtils.isBlank(linkedValue)) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(linkedParameterName).append(".cannot.be.empty.when.").append(this.parameter).append(".is.populated");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(linkedParameterName)
                    .append(" cannot be empty when ").append(this.parameter).append(" is populated.");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), linkedParameterName, linkedValue, this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public void failWithCode(final String errorCode, final Object... defaultUserMessageArgs) {
        final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                .append(this.parameter).append(".").append(errorCode);
        final StringBuilder defaultEnglishMessage = new StringBuilder("Failed data validation due to: ").append(errorCode).append(".");
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                this.parameter, this.value, defaultUserMessageArgs);
        this.dataValidationErrors.add(error);
    }

    public void failWithCodeNoParameterAddedToErrorCode(final String errorCode, final Object... defaultUserMessageArgs) {
        final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".").append(errorCode);
        final StringBuilder defaultEnglishMessage = new StringBuilder("Failed data validation due to: ").append(errorCode).append(".");
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                this.parameter, this.value, defaultUserMessageArgs);
        this.dataValidationErrors.add(error);
    }

    public DataValidatorBuilder equalToParameter(final String linkedParameterName, final Object linkedValue) {
        if (this.value == null && linkedValue == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && !this.value.equals(linkedValue)) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(linkedParameterName).append(".not.equal.to.").append(this.parameter);
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(linkedParameterName)
                    .append(" is not equal to ").append(this.parameter).append(".");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), linkedParameterName, linkedValue, this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder notSameAsParameter(final String linkedParameterName, final Object linkedValue) {
        if (this.value == null && linkedValue == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && this.value.equals(linkedValue)) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(linkedParameterName).append(".same.as.").append(this.parameter);
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(linkedParameterName)
                    .append(" is same as ").append(this.parameter).append(".");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), linkedParameterName, linkedValue, this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    /*** FIXME: Vishwas, why does this method have a parameter? Seems wrong ***/
    /*
     * This method is not meant for validation, if you have mandatory boolean param and if it has invalid value or value
     * not passed then call this method, this method is always used with input as false
     */
    public DataValidatorBuilder trueOrFalseRequired1(final boolean trueOfFalseFieldProvided) {
        if (!trueOfFalseFieldProvided && !this.ignoreNullValue) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".must.be.true.or.false");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                    .append(" must be set as true or false.");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder trueOrFalseRequired(final Object trueOfFalseField) {

        if (trueOfFalseField != null) {
            if (!trueOfFalseField.toString().equalsIgnoreCase("true") && !trueOfFalseField.toString().equalsIgnoreCase("false")) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".must.be.true.or.false");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be set as true or false.");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter);
                this.dataValidationErrors.add(error);
            }
        }

        return this;
    }

    public DataValidatorBuilder notNull() {
        if (this.value == null && !this.ignoreNullValue) {

            String realParameterName = this.parameter;
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter);
            if (this.arrayIndex != null && StringUtils.isNotBlank(this.arrayPart)) {
                validationErrorCode.append(".").append(this.arrayPart);
                realParameterName = new StringBuilder(this.parameter).append('[').append(this.arrayIndex).append("][")
                        .append(this.arrayPart).append(']').toString();
            }

            validationErrorCode.append(".cannot.be.null");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(realParameterName.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" is mandatory ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), realParameterName, this.arrayIndex);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder notBlank() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value == null || StringUtils.isBlank(this.value.toString())) {
            String realParameterName = this.parameter;
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter);
            if (this.arrayIndex != null && StringUtils.isNotBlank(this.arrayPart)) {
                validationErrorCode.append(".").append(this.arrayPart);
                realParameterName = new StringBuilder(this.parameter).append('[').append(this.arrayIndex).append("][")
                        .append(this.arrayPart).append(']').toString();
            }

            validationErrorCode.append(".cannot.be.null.or.blank");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(realParameterName.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" is mandatory ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), realParameterName, this.arrayIndex);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder notExceedingLengthOf(final Integer maxLength) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && this.value.toString().trim().length() > maxLength) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".exceeds.max.length");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" exceeds max length of ").append(maxLength).append(" ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, maxLength, this.value.toString());
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder equalTo(final Integer maxLength) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && this.value.toString().trim().length() != maxLength) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".exceeds.max.length");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" size should be  ").append(maxLength).append(" ").append("Characters ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, maxLength, this.value.toString());
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder inMinMaxRange(final Integer min, final Integer max) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer number = Integer.valueOf(this.value.toString());
            if (number < min || number > max) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.not.within.expected.range");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be between ").append(min).append(" and ").append(max).append(".");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, number, min, max);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder isOneOfTheseValues(final Object... values) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final List<Object> valuesList = Arrays.asList(values);
        final String valuesListStr = StringUtils.join(valuesList, ", ");

        if (this.value == null || !valuesList.contains(this.value)) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".is.not.one.of.expected.enumerations");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" must be one of [ ").append(valuesListStr).append(" ] ").append(".");

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value, values);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    public DataValidatorBuilder isOneOfEnumValues(Class<? extends Enum<?>> e) {
        final List<String> enumValuesList = Arrays.asList(Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new));
        return isOneOfTheseStringValues(enumValuesList);
    }

    public DataValidatorBuilder isOneOfTheseStringValues(final Object... values) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final List<Object> valuesList = Arrays.asList(values);
        final String valuesListStr = StringUtils.join(valuesList, ", ");

        if (this.value == null || !valuesList.contains(this.value.toString().toLowerCase())) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".is.not.one.of.expected.enumerations");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" must be one of [ ").append(valuesListStr).append(" ] ").append(".");

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value, values);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    public DataValidatorBuilder isOneOfTheseStringValues(final List<String> valuesList) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final String valuesListStr = StringUtils.join(valuesList, ", ");

        List<String> valuesListLowercase = valuesList.stream().map(String::toLowerCase).collect(Collectors.toList());

        if (this.value == null || !valuesListLowercase.contains(this.value.toString().toLowerCase())) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".is.not.one.of.expected.enumerations");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" must be one of [ ").append(valuesListStr).append(" ] ").append(".");

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value, valuesList);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    public DataValidatorBuilder isNotOneOfTheseValues(final Object... values) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final List<Object> valuesList = Arrays.asList(values);
            final String valuesListStr = StringUtils.join(valuesList, ", ");

            if (valuesList.contains(this.value)) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.one.of.unwanted.enumerations");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must not be any of [ ").append(valuesListStr).append(" ] ").append(".");

                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, this.value, values);

                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder positiveAmount() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final BigDecimal number = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (number.compareTo(BigDecimal.ZERO) <= 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.greater.than.zero");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be greater than 0");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder zeroOrPositiveAmount() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final BigDecimal number = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (number.compareTo(BigDecimal.ZERO) < 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.zero.or.greater");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be greater than or equal to 0.");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder integerZeroOrGreater() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer number = Integer.valueOf(this.value.toString());
            if (number < 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.zero.or.greater");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be zero or greater.");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder integerGreaterThanZero() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer number = Integer.valueOf(this.value.toString());
            if (number < 1) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.greater.than.zero");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be greater than 0");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder integerGreaterThanNumber(Integer number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer intValue = Integer.valueOf(this.value.toString());
            if (intValue < number + 1) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.greater.than.specified.number");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be greater than ").append(number);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, intValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder integerEqualToOrGreaterThanNumber(Integer number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer intValue = Integer.valueOf(this.value.toString());
            if (intValue < number) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.equal.to.or.greater.than.specified.number");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                        .append(" must be equal to or greater than").append(number);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, intValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder integerSameAsNumber(Integer number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer intValue = Integer.valueOf(this.value.toString());
            if (!intValue.equals(number)) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.equal.to.specified.number");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                        .append(" must be same as").append(number);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, intValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder integerInMultiplesOfNumber(Integer number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer intValue = Integer.valueOf(this.value.toString());
            if (intValue < number || intValue % number != 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.in.multiples.of.specified.number");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                        .append(" must be multiples of ").append(number);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, intValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder longGreaterThanZero() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Long number = Long.valueOf(this.value.toString());
            if (number < 1) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.greater.than.zero");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                        .append(" must be greater than 0");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder longZeroOrGreater() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Long number = Long.valueOf(this.value.toString());
            if (number < 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.equal.or.greater.than.zero");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                        .append(" must be equal or greater than 0.");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder longGreaterThanNumber(Long number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Long longValue = Long.valueOf(this.value.toString());
            if (longValue < number + 1) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.greater.than.specified.number");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                        .append(" must be greater than ").append(number);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, longValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder longGreaterThanNumber(String paramName, Long number, int index) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Long longValue = Long.valueOf(this.value.toString());
            if (longValue < number + 1) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".not.greater.than.specified.").append(paramName).append(".at Index.").append(index);
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                        .append(" must be greater than ").append(number);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, longValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder arrayNotEmpty() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final Object[] array = (Object[]) this.value;
        if (ObjectUtils.isEmpty(array)) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".cannot.be.empty");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                    .append(" cannot be empty. You must select at least one.");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder jsonArrayNotEmpty() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final JsonArray array = (JsonArray) this.value;
        if (this.value != null && !array.iterator().hasNext()) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".cannot.be.empty");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                    .append(" cannot be empty. You must select at least one.");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public void expectedArrayButIsNot() {
        final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                .append(this.parameter).append(".is.not.an.array");
        final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter).append(" is not an array.");
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                this.parameter);
        this.dataValidationErrors.add(error);
    }

    public DataValidatorBuilder anyOfNotNull(final Object... object) {
        boolean hasData = false;
        for (final Object obj : object) {
            if (obj != null) {
                hasData = true;
                break;
            }
        }

        if (!hasData) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource)
                    .append(".no.parameters.for.update");
            final StringBuilder defaultEnglishMessage = new StringBuilder("No parameters passed for update.");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), "id");
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder inValidValue(final String parameterValueCode, final Object invalidValue) {
        final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                .append(this.parameter).append(".invalid.").append(parameterValueCode);
        final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                .append(" has an invalid value.");
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                this.parameter, invalidValue);
        this.dataValidationErrors.add(error);
        return this;
    }

    public DataValidatorBuilder mustBeBlankWhenParameterProvided(final String parameterName, final Object parameterValue) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value == null && parameterValue != null) {
            return this;
        }

        if (this.value != null && StringUtils.isBlank(this.value.toString()) && parameterValue != null
                && StringUtils.isNotBlank(parameterValue.toString())) {
            return this;
        }

        final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                .append(this.parameter).append(".cannot.also.be.provided.when.").append(parameterName).append(".is.populated");
        final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                .append(" cannot also be provided when ").append(parameterName).append(" is populated ");
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                this.parameter, this.value, parameterName, parameterValue);
        this.dataValidationErrors.add(error);
        return this;
    }

    public DataValidatorBuilder mustBeBlankWhenParameterProvidedIs(final String parameterName, final Object parameterValue) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value == null && parameterValue != null) {
            return this;
        }

        if (this.value != null && StringUtils.isBlank(this.value.toString()) && parameterValue != null
                && StringUtils.isNotBlank(parameterValue.toString())) {
            return this;
        }

        final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                .append(this.parameter).append(".cannot.also.be.provided.when.").append(parameterName).append(".is.")
                .append(parameterValue);
        final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                .append(" cannot also be provided when ").append(parameterName).append(" is ").append(parameterValue);
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                this.parameter, this.value, parameterName, parameterValue);
        this.dataValidationErrors.add(error);
        return this;
    }

    public DataValidatorBuilder cantBeBlankWhenParameterProvidedIs(final String parameterName, final Object parameterValue) {
        if (this.value != null && StringUtils.isNotBlank(this.value.toString())) {
            return this;
        }

        final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                .append(this.parameter).append(".must.be.provided.when.").append(parameterName).append(".is.").append(parameterValue);
        final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                .append(" must be provided when ").append(parameterName).append(" is ").append(parameterValue);
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                this.parameter, this.value, parameterName, parameterValue);
        this.dataValidationErrors.add(error);
        return this;
    }

    public DataValidatorBuilder comapareMinimumAndMaximumAmounts(final BigDecimal minimumBalance, final BigDecimal maximumBalance) {
        if (minimumBalance != null && maximumBalance != null) {
            if (maximumBalance.compareTo(minimumBalance) < 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.not.within.expected.range");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(" minimum amount ")
                        .append(minimumBalance).append(" should less than maximum amount ").append(maximumBalance).append(".");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, minimumBalance, maximumBalance);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder inMinAndMaxAmountRange(final BigDecimal minimumAmount, final BigDecimal maximumAmount) {

        if (minimumAmount != null && maximumAmount != null && this.value != null) {
            final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (amount.compareTo(minimumAmount) < 0 || amount.compareTo(maximumAmount) > 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".amount.is.not.within.min.max.range");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" amount ")
                        //.append(amount)
                        .append(" must be between ").append(minimumAmount).append(" and ").append(maximumAmount)
                        .append(" .");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, amount, minimumAmount, maximumAmount);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder notLessThanMin(final BigDecimal min) {
        if (min != null && this.value != null) {
            final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (amount.compareTo(min) < 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.less.than.min");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase()).append(" value ")
                        .append(amount).append(" must not be less than minimum value ").append(min);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, amount, min);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder notGreaterThanMax(final BigDecimal max) {
        if (max != null && this.value != null) {
            final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (amount.compareTo(max) > 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.greater.than.max");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase()).append(" value ")
                        .append(amount).append(" must not be more than maximum value ").append(max);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, amount, max);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder comapareMinAndMaxOfTwoBigDecmimalNos(final BigDecimal min, final BigDecimal max) {
        if (min != null && max != null) {
            if (max.compareTo(min) < 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.not.within.expected.range");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(" min number ").append(min)
                        .append(" should less than max number ").append(max).append(".");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, min, max);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder isValidRecurringRule(final String recurringRule) {
        if (StringUtils.isNotBlank(recurringRule)) {
            try {
                final RRule rRule = new RRule(recurringRule);
                rRule.validate();
            } catch (final ValidationException e) {
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.recurring.rule",
                        "The Recurring Rule value: " + recurringRule + " is not valid.", this.parameter, recurringRule);
                this.dataValidationErrors.add(error);
                return this;
            } catch (final ParseException e) {
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg.recurring.rule.parsing.error",
                        "Error in pasring the Recurring Rule value: " + recurringRule + ".", this.parameter, recurringRule);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder notLessThanMin(final Integer min) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && min != null) {
            final Integer number = Integer.valueOf(this.value.toString());
            if (number < min) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.less.than.min");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be greater than minimum value ").append(min);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, number, min);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder notGreaterThanMax(final Integer max) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && max != null) {
            final Integer number = Integer.valueOf(this.value.toString());
            if (number > max) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.greater.than.max");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be less than maximum value ").append(max);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, number, max);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder matchesRegularExpression(final String expression) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && !this.value.toString().matches(expression)) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".does.not.match.regexp");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" must match the provided regular expression [ ").append(expression).append(" ] ").append(".");

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value, expression);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    public DataValidatorBuilder matchesRegularExpression(final String expression, final String Message) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && !this.value.toString().matches(expression)) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".does.not.match.regexp");
            final StringBuilder defaultEnglishMessage = new StringBuilder(Message);

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value, expression);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    private DataValidatorBuilder validateStringFor(final String validInputs) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        final Iterable<String> inputs = Splitter.onPattern(VALID_INPUT_SEPERATOR).split(validInputs);
        boolean validationErr = true;
        for (final String input : inputs) {
            if (input.equalsIgnoreCase(this.value.toString().trim())) {
                validationErr = false;
                break;
            }
        }
        if (validationErr) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".value.should.true.or.false");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                    .append(" value should true or false ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder validateForBooleanValue() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        return validateStringFor("TRUE" + VALID_INPUT_SEPERATOR + "FALSE");
    }

    public DataValidatorBuilder validatePhoneNumber() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        boolean validationErr = true;
        /*
         * supports numbers, parentheses(), hyphens and may contain + sign in the beginning and can contain whitespaces
         * in between and length allowed is 0-25 chars.
         */
        final String regex = "^\\+?[0-9. ()-]{0,25}$";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(this.value.toString());
        if (matcher.matches()) {
            validationErr = false;
        }
        if (validationErr) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".format.is.invalid");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.resource).append(this.parameter)
                    .append(" is in invalid format, should contain '-','+','()' and numbers only ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder validateCronExpression() {
        if (this.value != null && !CronExpression.isValidExpression(this.value.toString().trim())) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".invalid");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                    .append(" value is not a valid cron expression ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder validateDateAfter(final LocalDate date) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && date != null) {
            final LocalDate dateVal = (LocalDate) this.value;
            if (date.isAfter(dateVal)) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.less.than.date");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be greter than provided date").append(date).append(" ");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, dateVal, date);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder validateDateBefore(final LocalDate date) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && date != null) {
            final LocalDate dateVal = (LocalDate) this.value;
            if (date.isBefore(dateVal)) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.greater.than.date");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be less than provided date ").append(date).append(" ");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, dateVal, date);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder validateDateBeforeOrEqual(final LocalDate date) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && date != null) {
            final LocalDate dateVal = (LocalDate) this.value;
            if (dateVal.isAfter(date)) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.greater.than.date");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must be less than or equal to provided date").append(date).append(" ");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, dateVal, date);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder validateDateForEqual(final LocalDate date) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && date != null) {
            final LocalDate dateVal = (LocalDate) this.value;
            if (!dateVal.isEqual(date)) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.not.equal.to.date");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter)
                        .append(" must be equal to provided date").append(date).append(" ");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, dateVal, date);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder validateDateGreaterThanCurrentDate() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final LocalDate dateVal = (LocalDate) this.value;
            if (dateVal.isAfter(LocalDate.now())) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.greater.than.date");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ")
                        .append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                        .append(" must not be greater than current date ");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, dateVal);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /**
     * Throws Exception if validation errors.
     *
     * @throws PlatformApiDataValidationException
     *             unchecked exception (RuntimeException) thrown if there are any validation error
     */
    public void throwValidationErrors() throws PlatformApiDataValidationException {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public DataValidatorBuilder isLessThanLoanAmount(BigDecimal overAllLimit, BigDecimal principalAmount) {

        if (overAllLimit != null && this.value != null) {
            final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if ( amount.compareTo(overAllLimit) > 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".amount.is.not.within.the.range");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" amount ")
                        .append(amount).append(" must be less than ").append(overAllLimit).append(" ");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, amount, overAllLimit);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;




    }

    public DataValidatorBuilder loanProductClassIdExist(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer loanProductClassId) {

        CodeValue loanProductClas=null;
        final Long loanProductId=Long.valueOf(loanProductClassId);

        loanProductClas = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.LOANPRODUCTCLASS, Long.valueOf(loanProductClassId));
        final Long loanProductClass=loanProductClas.getId();
        if(loanProductId !=(loanProductClass))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(loanProductClassId).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;



    }


    public DataValidatorBuilder loanTypeIdExist(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer loanTypeId) {
        if (Objects.isNull(loanTypeId) && this.ignoreNullValue) {
            return this;
        }

        CodeValue loanTypeCodeVAlue=null;
        final Long loanTypeCv =Long.valueOf(loanTypeId);
        loanTypeCodeVAlue = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.LOANTYPE, Long.valueOf(loanTypeId));
        final Long loanTypeCvValue=loanTypeCodeVAlue.getId();
        if(loanTypeCv !=(loanTypeCvValue))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(loanTypeId).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;



    }


    public DataValidatorBuilder frameWorkIdExist(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer frameWorkId) {

        CodeValue frameWorkCodeValue=null;
        if(Objects.isNull(frameWorkId) && this.ignoreNullValue) {
            return this;
        }

        final Long frameWorkCvValue =Long.valueOf(frameWorkId);
        frameWorkCodeValue = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.FRAMEWORK, Long.valueOf(frameWorkId));
        final Long framwWorkId=frameWorkCodeValue.getId();
        if(frameWorkCvValue !=(framwWorkId)) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(frameWorkId).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;



    }




    public DataValidatorBuilder assetClassIdExist(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer assetClassId) {
        if(Objects.isNull(assetClassId) && this.ignoreNullValue) return this;

        CodeValue assetClassType=null;
        final Long assetClassTypes=Long.valueOf(assetClassId);

        assetClassType = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.ASSETCLASS, Long.valueOf(assetClassId));
        final Long loanProductClass=assetClassType.getId();
        if(!assetClassTypes.equals(loanProductClass)) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(assetClassId).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;



    }


    public DataValidatorBuilder insuranceApplicabilityIdExist(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer insuranceApplicabilityId) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        CodeValue insuranceApplicabilityCodeId=null;
        final Long insuranceApplicabilityTypes=Long.valueOf(insuranceApplicabilityId);

        insuranceApplicabilityCodeId = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.INSURANCEAPPLICABILITY, Long.valueOf(insuranceApplicabilityId));
        final Long insuranceApplicability=insuranceApplicabilityCodeId.getId();
        if(insuranceApplicabilityTypes !=(insuranceApplicability))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(insuranceApplicabilityId).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;



    }


    public DataValidatorBuilder fldgLogicIdExist(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer fldgLogicCode) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        CodeValue fldgLogicCodeValue=null;
        final Long fldgLogicCodeValues=Long.valueOf(fldgLogicCode);

        fldgLogicCodeValue = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.FLDGLOGIC, Long.valueOf(fldgLogicCode));
        final Long loanProductClass=fldgLogicCodeValue.getId();
        if(fldgLogicCodeValues !=(loanProductClass))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(fldgLogicCode).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;
        }

        return this;
    }

    public DataValidatorBuilder penalInvoiceExists(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer penalInvoiceCode) {
        if(Objects.isNull(penalInvoiceCode) && this.ignoreNullValue) return this;
        CodeValue penalInvoiceCodeValue=null;
        final Long penalInvoiceCodeValues=Long.valueOf(penalInvoiceCode);

        penalInvoiceCodeValue = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.PENALINVOICE, Long.valueOf(penalInvoiceCode));
        final Long loanProductClass=penalInvoiceCodeValue.getId();
        if(penalInvoiceCodeValues !=(loanProductClass))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(penalInvoiceCode).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;
    }

    public DataValidatorBuilder multipleDisbursementExists(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer multipleDisbursementCode) {
        if(Objects.isNull(multipleDisbursementCode) && this.ignoreNullValue) return this;
        CodeValue multipleDisbursementCodeValue=null;
        final Long multipleDisbursementCodeValues=Long.valueOf(multipleDisbursementCode);

        multipleDisbursementCodeValue = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.MULTIPLEDISBURSEMENT, Long.valueOf(multipleDisbursementCode));
        final Long loanProductClass=multipleDisbursementCodeValue.getId();
        if(multipleDisbursementCodeValues !=(loanProductClass))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(multipleDisbursementCode).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;
    }

    public DataValidatorBuilder trancheClubbingExists(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer trancheClubbingCode) {
        if(Objects.isNull(trancheClubbingCode) && this.ignoreNullValue) return this;
        CodeValue trancheClubbingCodeValue=null;
        final Long trancheClubbingCodeValues=Long.valueOf(trancheClubbingCode);

        trancheClubbingCodeValue = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.TRANCHECLUBBING, Long.valueOf(trancheClubbingCode));
        final Long loanProductClass=trancheClubbingCodeValue.getId();
        if(trancheClubbingCodeValues !=(loanProductClass))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(trancheClubbingCode).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;
    }

    public DataValidatorBuilder repaymentScheduleUpdateAllowedExists(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer repaymentScheduleUpdateAllowedCode) {
        if(Objects.isNull(repaymentScheduleUpdateAllowedCode) && this.ignoreNullValue) return this;
        CodeValue repaymentScheduleUpdateAllowedCodeValue=null;
        final Long repaymentScheduleUpdateAllowedCodeValues=Long.valueOf(repaymentScheduleUpdateAllowedCode);

        repaymentScheduleUpdateAllowedCodeValue = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.REPAYMENTSCHEDULEUPDATEALLOWED, Long.valueOf(repaymentScheduleUpdateAllowedCode));
        final Long loanProductClass=repaymentScheduleUpdateAllowedCodeValue.getId();
        if(repaymentScheduleUpdateAllowedCodeValues !=(loanProductClass))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(repaymentScheduleUpdateAllowedCode).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;
    }






    public DataValidatorBuilder disbursementIdExist(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer disbursementId) {
        if(Objects.isNull(disbursementId) && this.ignoreNullValue) return this;
        CodeValue disbursementCode=null;
        final Long disbursementCodeType=Long.valueOf(disbursementId);

        disbursementCode = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.DISBURSEMENTMODE, Long.valueOf(disbursementId));
        final Long loanProductClass=disbursementCode.getId();
        if(disbursementCodeType !=(loanProductClass))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(disbursementId).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;



    }

    public DataValidatorBuilder loanProductTypeIdExist(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer loanProductTypeId) {
        if(Objects.isNull(loanProductTypeId) && this.ignoreNullValue) return this;
        CodeValue loanProductType=null;
        final Long loanProductTypes=Long.valueOf(loanProductTypeId);

        loanProductType = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.LOANPRODUCTTYPE, Long.valueOf(loanProductTypeId));
        final Long loanProductClass=loanProductType.getId();
        if(loanProductTypes !=(loanProductClass))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(loanProductTypeId).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;



    }   public DataValidatorBuilder collectionIdExist(CodeValueRepositoryWrapper codeValueRepositoryWrapper, Integer collectionIdcode) {
        if(Objects.isNull(collectionIdcode) && this.ignoreNullValue) return this;
        CodeValue collectionCodeValue=null;
        final Long loanProductTypes=Long.valueOf(collectionIdcode);

        collectionCodeValue = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductConstants.COLLECTION, Long.valueOf(collectionIdcode));
        final Long collectionCvId=collectionCodeValue.getId();
        if(loanProductTypes !=(collectionCvId))
        {

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append("the given Value is not present");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(collectionIdcode).append(" must be present in the dataBase ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
            return this;

        }

        return this;



    }

    public DataValidatorBuilder notgreaterthan(BigDecimal minimumChargeAmount, BigDecimal maxChargeAmount,BigDecimal amount) {



        if (minimumChargeAmount != null  && maxChargeAmount!=null) {
            final BigDecimal totalAmount=minimumChargeAmount.add(maxChargeAmount);
           // final BigDecimal amounts = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (totalAmount.compareTo(minimumChargeAmount) < 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append("sum of amount");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                        .append(amount).append(" must not be equal to amount ").append(minimumChargeAmount).append(" ");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, amount, minimumChargeAmount);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

//    public void sumOfChargeAmount(DataValidatorBuilder baseDataValidator, BigDecimal minimumChargeAmount, BigDecimal maxChargeAmount, BigDecimal amount) {
//
//
//            final Integer total=100;
//
//
//            final BigDecimal totalInterest= sum(minimumChargeAmount,maxChargeAmount);
//            if(total != totalInterest){
//                baseDataValidator.reset().parameter(LoanProductConstants.minimumGapBetweenInstallments).failWithCode(
//                        "validation Error",
//                        "One of the charge is more than 100");}}

    private BigDecimal sum(BigDecimal minimumChargeAmount, BigDecimal maxChargeAmount) {

        return minimumChargeAmount.add(maxChargeAmount);
    }


    public DataValidatorBuilder notLessThanMinPercentage(BigDecimal minimumChargeAmount) {

            if (minimumChargeAmount != null && this.value != null) {
                final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
                if (amount.compareTo(minimumChargeAmount) < 0) {
                    final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                            .append(this.parameter).append(".is.less.than.min");
                    final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                            .append(amount).append(" must not be less than minimum value  ").append(minimumChargeAmount);
                    final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                            defaultEnglishMessage.toString(), this.parameter, amount, minimumChargeAmount);
                    this.dataValidationErrors.add(error);
                    return this;
                }
            }
            return this;
    }

    public DataValidatorBuilder inMinAndMaxAmountRangePercentage(BigDecimal minimumChargeAmount, BigDecimal maxChargeAmount) {

        if (minimumChargeAmount != null && maxChargeAmount != null && this.value != null) {
            final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (amount.compareTo(minimumChargeAmount) < 0 || amount.compareTo(maxChargeAmount) > 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".amount.is.not.within.min.max.range");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" amount ")
                        .append(amount).append(" must be between ").append(minimumChargeAmount).append(" and ").append(maxChargeAmount)
                        .append(" ");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, amount, minimumChargeAmount, maxChargeAmount);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder notGreaterThanMaxPercentage(BigDecimal maxChargeAmount) {

        if (maxChargeAmount != null && this.value != null) {
            final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (amount.compareTo(maxChargeAmount) > 0) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.greater.than.max");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                        .append(amount).append(" must not be more than maximum value ").append(maxChargeAmount);
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter, amount, maxChargeAmount);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public void isBefore(LocalDate expectedDisbursementDate, DataValidatorBuilder baseDataValidator,LocalDate interestChargedFromDate) {
        if(interestChargedFromDate.isBefore(expectedDisbursementDate) || interestChargedFromDate.isAfter(expectedDisbursementDate) ){
            baseDataValidator.reset().parameter("interestChargedFromDate").failWithCode(
                    "Interest Charge From Date Should Be same as Disbursement date",
                    "Interest Charge From Date Should Be same as Disbursement date");


        }
    }

    public DataValidatorBuilder isNumeric() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        if (this.value==null||!StringUtils.isNumeric(this.value.toString())) {
                final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                        .append(this.parameter).append(".is.not.a.numeric.value");
                final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase()).append(" value ")
                        .append(this.value).append(" must be a numeric value ");
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                        defaultEnglishMessage.toString(), this.parameter);
                this.dataValidationErrors.add(error);
            }
        return this;
    }

    public DataValidatorBuilder isAlphaNumeric() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value==null||!StringUtils.isAlphanumeric(this.value.toString())) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".is.not.a.alphanumeric.value");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The Parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase()).append(" value ")
                    .append(this.value).append(" must be a alphanumeric value ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder isAlpha() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        if (!StringUtils.isAlpha(this.value.toString())) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".is.not.a.alphabet.value");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(this.value).append(" must be a alphabet value ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder isNumericOrDecimal() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        if (!NumberUtils.isNumber(this.value.toString())) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".is.not.a.alphabet.value");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(this.value).append(" must be a alphabet value ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder matchRegex(String regexExpression, String errorMessage) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        if (!Pattern.matches(regexExpression, this.value.toString())) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".does.not.match.regex");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), errorMessage, null, null);
            this.dataValidationErrors.add(error);
        }
        return this;
    }


    public DataValidatorBuilder isEmailAddress() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        if (this.value==null||!Pattern.matches("^[A-Za-z0-9._%'-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,4}$", this.value.toString())) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(".does.not.match.regex");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The Parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase()).append(" value ")
                    .append(this.value).append(" is not matching wih required pattern ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value,null);
            this.dataValidationErrors.add(error);
        }
        return this;
    }
    public DataValidatorBuilder defaultValidation() {
        final StringBuilder validationErrorCode=new StringBuilder("validation.msg.").append(".").append(this.parameter).append("min max default should be same while checking isdefault option");
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), "Invalid Min,Max,Default Value", null, null);
        this.dataValidationErrors.add(error);
        return this;
    }
    public DataValidatorBuilder isAlphaWithSpace(){
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        if(this.value==null|| !Pattern.compile("^[A-Za-z ]+$").matcher(this.value.toString()).find()){
            final StringBuilder validationErrorCode= new StringBuilder("validation.msg.").append(".").append(this.parameter).append(".only characters are allowed");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" will accept only characters ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value,null);
            this.dataValidationErrors.add(error);
        }
        return this;
    }
    public DataValidatorBuilder isUpperCase(){
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        if(this.value==null||Pattern.compile("[a-z]").matcher(this.value.toString()).find()){
            final StringBuilder validationErrorCode= new StringBuilder("validation.msg.").append(".").append(this.parameter).append(".capitals only allowed");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The ").append(this.parameter).append(" value ")
                    .append(this.value).append(" capitals only allowed  ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value,null);
            this.dataValidationErrors.add(error);
        }
        return this;
    }
    public DataValidatorBuilder isAlphaNumericWithSpace(){
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        if(this.value==null||!Pattern.compile("^[A-Za-z0-9 ]+$").matcher(this.value.toString()).find()){
            final StringBuilder validationErrorCode= new StringBuilder("validation.msg").append(".").append(this.parameter).append(" no special characters allowed other than space");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append(" won't accept any special character ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value,null);
            this.dataValidationErrors.add(error);
        }
        return this;
    }
    public DataValidatorBuilder sizeOneOfThese(final Object... values){
        if(this.value==null && this.ignoreNullValue){
            return this;
        }
        List<Object> valuesListStr= Arrays.stream(values).toList();
        if(this.value==null || !valuesListStr.contains(this.value.toString().length())){
            String validationErrorCode = "validation.msg." + this.resource + "." +
                    this.parameter + ".is.not.one.of.expected.values";
            String defaultEnglishMessage = "The parameter " + this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase() +
                    " length must be either  " + valuesListStr.get(0) + " or "+ valuesListStr.get(1)  + " ";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode,
                    defaultEnglishMessage, this.parameter, this.value, values);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    /**
     * <p>
     *     This method checks the @param emiMultiplesOf is existing within list of available "Multiples of" for an loan product.
     * </p>
     * @param emiMultiplesOf
     * @return DataValidatorBuilder
     */
    public DataValidatorBuilder isEmiMultiplesOfExist(Integer emiMultiplesOf) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        List<Object> multiplesOfList= Arrays.asList(MultiplesOf.ONE.getValue(), MultiplesOf.TEN.getValue(), MultiplesOf.HUNDRED.getValue());

        Optional<MultiplesOf> optionalValue = Optional.ofNullable(MultiplesOf.fromInt(emiMultiplesOf));
        if(optionalValue.isEmpty()){
            final String valuesListStr = StringUtils.join(multiplesOfList, ", ");

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(" the given Value does not exist");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                    .append(" must be one of [ ").append(valuesListStr).append(" ] ").append(".");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    /**
     * <p>
     *     This method checks the @param roundingMode is existing within the list of available "Rounding Mode".
     * </p>
     * @param roundingMode
     * @return DataValidatorBuilder
     */
    public DataValidatorBuilder isRoundingModesExist(String roundingMode){
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final List<RoundingMode> roundingModeList=Arrays.asList(RoundingMode.UP,RoundingMode.HALF_UP,RoundingMode.DOWN);

        boolean isValidRoundingMode = roundingModeList.stream().anyMatch(list -> list.name().equals(roundingMode));
        if(!isValidRoundingMode){
            final String valuesListStr = StringUtils.join(roundingModeList, ", ");

            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter).append(" the given Value does not exist");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter)
                    .append(" must be one of [ ").append(valuesListStr).append(" ] ").append(".");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }


    public DataValidatorBuilder validateAadhaar(){
        if(this.value==null && ignoreNullValue){
            return this;
        }
        if(this.value==null || ! Pattern.compile("^(\\d{8}|[X|x]{8})\\d{4}$").matcher(this.value.toString()).find()){
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter +
                    ".not.matching.with.regex";
            String defaultEnglishMessage = "The parameter " +
                    "Aadhaar must contain 12 numeric digits or first 8 digits with X and last 4 digits numeric " + " ";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode,
                    defaultEnglishMessage, this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }
    public DataValidatorBuilder firstIndexSpecialCharacter(){
        if(this.value==null && this.ignoreNullValue){
            return  this;
        }
        if(this.value==null || !Pattern.compile("^[A-Za-z]$").matcher(this.value.toString().substring(0,1)).find()){
            String validationErrorCode="validation.msg." + this.resource +"." + this.parameter + ".not.matching.with.regex";
            String defaultEnglishMessage="The Parameter "+this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase()+" should not start with numeric or special characters ";
            final ApiParameterError error=ApiParameterError.parameterError(validationErrorCode,defaultEnglishMessage,this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }
    public DataValidatorBuilder isAlphaNumericWithSpecialCharacters(){
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        if(this.value==null||!Pattern.compile("^[A-Za-z0-9 .&-]+$").matcher(this.value.toString()).find()){
            final StringBuilder validationErrorCode= new StringBuilder("validation.msg").append(".").append(this.parameter).append(" no special characters allowed other than Hyphen(-), Dot(.), Ampersand(&) ");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(this.parameter.replaceAll("(?<!^)(?=[A-Z])", " ").toUpperCase())
                    .append("  no special characters allowed other than  Hyphen(-) , Dot(.) , Ampersand(&) ");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), this.parameter, this.value,null);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

}


