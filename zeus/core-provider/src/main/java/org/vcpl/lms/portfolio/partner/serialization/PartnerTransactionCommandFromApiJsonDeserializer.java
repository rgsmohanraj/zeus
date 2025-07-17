///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements. See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership. The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License. You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.vcpl.lms.organisation.partner.serialization;
//
//import com.google.gson.JsonElement;
//import com.google.gson.reflect.TypeToken;
//import java.lang.reflect.Type;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import org.apache.commons.lang3.StringUtils;
//import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
//import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
//import org.vcpl.lms.infrastructure.core.exception.InvalidJsonException;
//import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
//import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public final class PartnerTransactionCommandFromApiJsonDeserializer {
//
//    /**
//     * The parameters supported for this command.
//     */
//    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("fromPartnerId", "toPartnerId", "transactionDate",
//            "currencyCode", "transactionAmount", "description", "locale", "dateFormat"));
//
//    private final FromJsonHelper fromApiJsonHelper;
//
//    @Autowired
//    public PartnerTransactionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
//        this.fromApiJsonHelper = fromApiJsonHelper;
//    }
//
//    public void validatePartnerTransfer(final String json) {
//        if (StringUtils.isBlank(json)) {
//            throw new InvalidJsonException();
//        }
//
//        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
//        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);
//
//        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
//        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("partnerTransaction");
//
//        final JsonElement element = this.fromApiJsonHelper.parse(json);
//        final Long fromPartnerId = this.fromApiJsonHelper.extractLongNamed("fromPartnerId", element);
//        baseDataValidator.reset().parameter("fromPartnerId").value(fromPartnerId).ignoreIfNull().integerGreaterThanZero();
//
//        final Long toPartnerId = this.fromApiJsonHelper.extractLongNamed("toPartnerId", element);
//        baseDataValidator.reset().parameter("toPartnerId").value(toPartnerId).ignoreIfNull().integerGreaterThanZero();
//
//        if (fromPartnerId == null && toPartnerId == null) {
//            baseDataValidator.reset().parameter("toPartnerId").value(toPartnerId).notNull();
//        }
//
//        if (fromPartnerId != null && toPartnerId != null) {
//            baseDataValidator.reset().parameter("fromPartnerId").value(fromPartnerId).notSameAsParameter("toPartnerId", toPartnerId);
//        }
//
//        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
//        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();
//
//        final String currencyCode = this.fromApiJsonHelper.extractStringNamed("currencyCode", element);
//        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank().notExceedingLengthOf(3);
//
//        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
//        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().positiveAmount();
//
//        final String description = this.fromApiJsonHelper.extractStringNamed("description", element);
//        baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(100);
//
//        throwExceptionIfValidationWarningsExist(dataValidationErrors);
//    }
//
//    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
//        if (!dataValidationErrors.isEmpty()) {
//            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
//                    dataValidationErrors);
//        }
//    }
//}
