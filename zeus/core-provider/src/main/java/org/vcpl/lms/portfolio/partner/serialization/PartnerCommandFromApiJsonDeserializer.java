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
package org.vcpl.lms.portfolio.partner.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.InvalidJsonException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.core.service.DateUtils;

/**
 * Deserializer of JSON for partner API.
 */
@Component
public final class PartnerCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<>(
            Arrays.asList("partnerName" , "partnerCompanyRegistrationDate" , "source" , "panCard", "cinNumber" ,
                    "address1" , "address2" , "city" , "state", "pincode" , "country", "constitution" , "keyPersons" ,
                    "industry" , "sector" , "subSector" , "gstNumber" , "gstRegistration", "partnerType" ,
                    "beneficiaryName" , "beneficiaryAccountNumber" , "ifscCode" , "micrCode" , "swiftCode" , "branch",
                    "modelLimit" , "approvedLimit" , "pilotLimit" , "partnerFloatLimit" , "balanceLimit" , "agreementStartDate",
                    "agreementExpiryDate" , "underlyingAssets" , "security" , "fldgCalculationOn", "locale", "dateFormat"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public PartnerCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("partner");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String partnerName = this.fromApiJsonHelper.extractStringNamed("partnerName", element);
        baseDataValidator.reset().parameter("partnerName").value(partnerName).notNull().notBlank().notExceedingLengthOf(75).isAlphaNumericWithSpecialCharacters();

        if (this.fromApiJsonHelper.extractLocalDateNamed("partnerCompanyRegistrationDate", element) != null) {
            final LocalDate partnerCompanyRegistrationDate = this.fromApiJsonHelper.extractLocalDateNamed("partnerCompanyRegistrationDate", element);
            baseDataValidator.reset().parameter("partnerCompanyRegistrationDate").value(partnerCompanyRegistrationDate).notNull()
                    .validateDateBefore(DateUtils.getLocalDateOfTenant());
        }
        if (this.fromApiJsonHelper.extractStringNamed("panCard", element) != null) {
            final String panCard = this.fromApiJsonHelper.extractStringNamed("panCard", element);
            baseDataValidator.reset().parameter("panCard").value(panCard).equalTo(10).ignoreIfNull().isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractStringNamed("cinNumber", element) != null) {
            final String cinNumber = this.fromApiJsonHelper.extractStringNamed("cinNumber", element);
            baseDataValidator.reset().parameter("cinNumber").value(cinNumber).equalTo(21).ignoreIfNull().isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractStringNamed("address1", element) != null) {
            final String address1 = this.fromApiJsonHelper.extractStringNamed("address1", element);
            baseDataValidator.reset().parameter("address1").value(address1).ignoreIfNull().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("address2", element) != null) {
            final String address2 = this.fromApiJsonHelper.extractStringNamed("address2", element);
            baseDataValidator.reset().parameter("address2").value(address2).ignoreIfNull().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("city", element) != null) {
            final String city = this.fromApiJsonHelper.extractStringNamed("city", element);
            baseDataValidator.reset().parameter("city").value(city).isAlphaWithSpace().ignoreIfNull().notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.extractLongNamed("state", element) != null) {
            final long state = this.fromApiJsonHelper.extractLongNamed("state", element);
            baseDataValidator.reset().parameter("state").value(state).ignoreIfNull().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("pincode", element) != null) {
            final long pincode = this.fromApiJsonHelper.extractLongNamed("pincode", element);
            baseDataValidator.reset().parameter("pincode").value(pincode).ignoreIfNull().longGreaterThanZero().isNumeric().equalTo(6);
        }


        if(this.fromApiJsonHelper.extractStringNamed("beneficiaryName",element)!=null){
            final String beneficieryName=this.fromApiJsonHelper.extractStringNamed("beneficiaryName",element);
            baseDataValidator.reset().parameter("beneficiaryName").value(beneficieryName).ignoreIfNull().isAlphaNumericWithSpace();
        }


        if (this.fromApiJsonHelper.extractLongNamed("source", element) != null) {
            final long source = this.fromApiJsonHelper.extractLongNamed("source", element);
            baseDataValidator.reset().parameter("source").value(source).ignoreIfNull().longGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractLongNamed("country", element) != null) {
            final long country = this.fromApiJsonHelper.extractLongNamed("country", element);
            baseDataValidator.reset().parameter("country").value(country).ignoreIfNull().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("constitution", element) != null) {
            final long constitution = this.fromApiJsonHelper.extractLongNamed("constitution", element);
            baseDataValidator.reset().parameter("constitution").value(constitution).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("industry", element) != null) {
            final long industry = this.fromApiJsonHelper.extractLongNamed("industry", element);
            baseDataValidator.reset().parameter("industry").value(industry).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("sector", element) != null) {
            final long sector = this.fromApiJsonHelper.extractLongNamed("sector", element);
            baseDataValidator.reset().parameter("sector").value(sector).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("subSector", element) != null) {
            final long subSector = this.fromApiJsonHelper.extractLongNamed("subSector", element);
            baseDataValidator.reset().parameter("subSector").value(subSector).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractStringNamed("gstNumber", element) != null) {
            final String gstNumber = this.fromApiJsonHelper.extractStringNamed("gstNumber", element);
            baseDataValidator.reset().parameter("gstNumber").value(gstNumber).ignoreIfNull().equalTo(15).isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractLongNamed("gstRegistration", element) != null) {
            final long gstRegistration = this.fromApiJsonHelper.extractLongNamed("gstRegistration", element);
            baseDataValidator.reset().parameter("gstRegistration").value(gstRegistration).ignoreIfNull().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("partnerType", element) != null) {
            final long partnerType = this.fromApiJsonHelper.extractLongNamed("partnerType", element);
            baseDataValidator.reset().parameter("partnerType").value(partnerType).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractStringNamed("beneficiaryAccountNumber", element) != null) {
            final String beneficiaryAccountNumber = this.fromApiJsonHelper.extractStringNamed("beneficiaryAccountNumber", element);
            baseDataValidator.reset().parameter("beneficiaryAccountNumber").value(beneficiaryAccountNumber).ignoreIfNull().notExceedingLengthOf(40).isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractStringNamed("ifscCode", element) != null) {
            final String ifscCode = this.fromApiJsonHelper.extractStringNamed("ifscCode", element);
            baseDataValidator.reset().parameter("ifscCode").value(ifscCode).equalTo(11).ignoreIfNull().isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractLongNamed("micrCode", element) != null) {
            final long micrCode = this.fromApiJsonHelper.extractLongNamed("micrCode", element);
            baseDataValidator.reset().parameter("micrCode").value(micrCode).ignoreIfNull().longGreaterThanZero().isNumeric().equalTo(9);
        }

        if (this.fromApiJsonHelper.extractStringNamed("swiftCode", element) != null) {
            final String swiftCode = this.fromApiJsonHelper.extractStringNamed("swiftCode", element);
            baseDataValidator.reset().parameter("swiftCode").value(swiftCode).equalTo(11).ignoreIfNull().isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractStringNamed("branch", element) != null) {
            final String branch = this.fromApiJsonHelper.extractStringNamed("branch", element);
            baseDataValidator.reset().parameter("branch").value(branch).notExceedingLengthOf(50).ignoreIfNull().isAlphaWithSpace();
        }

        if (this.fromApiJsonHelper.extractStringNamed("modelLimit", element) != null) {
            final BigDecimal modelLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("modelLimit", element);
            baseDataValidator.reset().parameter("modelLimit").value(modelLimit).ignoreIfNull().positiveAmount();
        }

        final BigDecimal approvedLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("approvedLimit", element);
        baseDataValidator.reset().parameter("approvedLimit").value(approvedLimit).notNull().notBlank().positiveAmount().isNumeric();

        if (this.fromApiJsonHelper.extractStringNamed("pilotLimit", element) != null) {
            final BigDecimal pilotLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("pilotLimit", element);
            baseDataValidator.reset().parameter("pilotLimit").value(pilotLimit).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.extractStringNamed("partnerFloatLimit", element) != null) {
            final BigDecimal partnerFloatLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerFloatLimit", element);
            baseDataValidator.reset().parameter("partnerFloatLimit").value(partnerFloatLimit).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.extractStringNamed("balanceLimit", element) != null) {
            final BigDecimal balanceLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("balanceLimit", element);
            baseDataValidator.reset().parameter("balanceLimit").value(balanceLimit).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.extractLocalDateNamed("agreementStartDate", element) != null) {
            final LocalDate agreementStartDate = this.fromApiJsonHelper.extractLocalDateNamed("agreementStartDate", element);
            baseDataValidator.reset().parameter("agreementStartDate").value(agreementStartDate).value(agreementStartDate).notNull();
        }

        if (this.fromApiJsonHelper.extractLocalDateNamed("agreementExpiryDate", element) != null) {
            final LocalDate agreementExpiryDate = this.fromApiJsonHelper.extractLocalDateNamed("agreementExpiryDate", element);
            baseDataValidator.reset().parameter("agreementExpiryDate").value(agreementExpiryDate).value(agreementExpiryDate).notNull();
        }

        if (this.fromApiJsonHelper.extractLongNamed("underlyingAssets", element) != null) {
            final long underlyingAssets = this.fromApiJsonHelper.extractLongNamed("underlyingAssets", element);
            baseDataValidator.reset().parameter("underlyingAssets").value(underlyingAssets).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("security", element) != null) {
            final long security = this.fromApiJsonHelper.extractLongNamed("security", element);
            baseDataValidator.reset().parameter("security").value(security).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("fldgCalculationOn", element) != null) {
            final long fldgCalculationOn = this.fromApiJsonHelper.extractLongNamed("fldgCalculationOn", element);
            baseDataValidator.reset().parameter("fldgCalculationOn").value(fldgCalculationOn).notBlank().longGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("partner");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists("partnerName", element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed("partnerName", element);
            baseDataValidator.reset().parameter("partnerName").value(name).notBlank().notExceedingLengthOf(100).isAlphaNumericWithSpecialCharacters();
        }

        if (this.fromApiJsonHelper.extractLocalDateNamed("partnerCompanyRegistrationDate", element) != null) {
            final LocalDate partnerCompanyRegistrationDate = this.fromApiJsonHelper.extractLocalDateNamed("partnerCompanyRegistrationDate", element);
            baseDataValidator.reset().parameter("partnerCompanyRegistrationDate").value(partnerCompanyRegistrationDate).notNull()
                    .validateDateBefore(DateUtils.getLocalDateOfTenant());
        }

        if (this.fromApiJsonHelper.extractLongNamed("source", element) != null) {
            final long source = this.fromApiJsonHelper.extractLongNamed("source", element);
            baseDataValidator.reset().parameter("source").value(source).ignoreIfNull().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractStringNamed("panCard", element) != null) {
            final String panCard = this.fromApiJsonHelper.extractStringNamed("panCard", element);
            baseDataValidator.reset().parameter("panCard").value(panCard).equalTo(10).ignoreIfNull().isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractStringNamed("cinNumber", element) != null) {
            final String cinNumber = this.fromApiJsonHelper.extractStringNamed("cinNumber", element);
            baseDataValidator.reset().parameter("cinNumber").value(cinNumber).equalTo(21).ignoreIfNull().isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractStringNamed("address1", element) != null) {
            final String address1 = this.fromApiJsonHelper.extractStringNamed("address1", element);
            baseDataValidator.reset().parameter("address1").value(address1).ignoreIfNull().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("address2", element) != null) {
            final String address2 = this.fromApiJsonHelper.extractStringNamed("address2", element);
            baseDataValidator.reset().parameter("address2").value(address2).ignoreIfNull().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("city", element) != null) {
            final String city = this.fromApiJsonHelper.extractStringNamed("city", element);
            baseDataValidator.reset().parameter("city").value(city).ignoreIfNull().isAlphaWithSpace().notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.extractLongNamed("state", element) != null) {
            final long state = this.fromApiJsonHelper.extractLongNamed("state", element);
            baseDataValidator.reset().parameter("state").value(state).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("pincode", element) != null) {
            final long pincode = this.fromApiJsonHelper.extractLongNamed("pincode", element);
            baseDataValidator.reset().parameter("pincode").value(pincode).ignoreIfNull().longGreaterThanZero().isNumeric().equalTo(6);
        }

        if (this.fromApiJsonHelper.extractLongNamed("country", element) != null) {
            final long country = this.fromApiJsonHelper.extractLongNamed("country", element);
            baseDataValidator.reset().parameter("country").value(country).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("constitution", element) != null) {
            final long constitution = this.fromApiJsonHelper.extractLongNamed("constitution", element);
            baseDataValidator.reset().parameter("constitution").value(constitution).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("industry", element) != null) {
            final long industry = this.fromApiJsonHelper.extractLongNamed("industry", element);
            baseDataValidator.reset().parameter("industry").value(industry).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("sector", element) != null) {
            final long sector = this.fromApiJsonHelper.extractLongNamed("sector", element);
            baseDataValidator.reset().parameter("sector").value(sector).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("subsector", element) != null) {
            final long subsector = this.fromApiJsonHelper.extractLongNamed("subsector", element);
            baseDataValidator.reset().parameter("subsector").value(subsector).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractStringNamed("gstNumber", element) != null) {
            final String gstNumber = this.fromApiJsonHelper.extractStringNamed("gstNumber", element);
            baseDataValidator.reset().parameter("gstNumber").value(gstNumber).ignoreIfNull().equalTo(15).isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractLongNamed("gstRegistration", element) != null) {
            final long gstRegistration = this.fromApiJsonHelper.extractLongNamed("gstRegistration", element);
            baseDataValidator.reset().parameter("gstRegistration").value(gstRegistration).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("partnerType", element) != null) {
            final long partnerType = this.fromApiJsonHelper.extractLongNamed("partnerType", element);
            baseDataValidator.reset().parameter("partnerType").value(partnerType).notBlank().longGreaterThanZero();
        }
        if(this.fromApiJsonHelper.extractStringNamed("beneficiaryName",element)!=null) {
            final String beneficieryName = this.fromApiJsonHelper.extractStringNamed("beneficiaryName", element);
            baseDataValidator.reset().parameter("beneficiaryName").value(beneficieryName).ignoreIfNull().isAlphaNumericWithSpace();
        }

        if (this.fromApiJsonHelper.extractStringNamed("beneficiaryAccountNumber", element) != null) {
            final String beneficiaryAccountNumber = this.fromApiJsonHelper.extractStringNamed("beneficiaryAccountNumber", element);
            baseDataValidator.reset().parameter("beneficiaryAccountNumber").value(beneficiaryAccountNumber).ignoreIfNull().notExceedingLengthOf(40).isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractStringNamed("ifscCode", element) != null) {
            final String ifscCode = this.fromApiJsonHelper.extractStringNamed("ifscCode", element);
            baseDataValidator.reset().parameter("ifscCode").value(ifscCode).equalTo(11).ignoreIfNull().isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractLongNamed("micrCode", element) != null) {
            final long micrCode = this.fromApiJsonHelper.extractLongNamed("micrCode", element);
            baseDataValidator.reset().parameter("micrCode").value(micrCode).longGreaterThanZero().isNumeric().ignoreIfNull().equalTo(9);
        }

        if (this.fromApiJsonHelper.extractStringNamed("swiftCode", element) != null) {
            final String swiftCode = this.fromApiJsonHelper.extractStringNamed("swiftCode", element);
            baseDataValidator.reset().parameter("swiftCode").value(swiftCode).equalTo(11).ignoreIfNull().isAlphaNumeric();
        }

        if (this.fromApiJsonHelper.extractStringNamed("branch", element) != null) {
            final String branch = this.fromApiJsonHelper.extractStringNamed("branch", element);
            baseDataValidator.reset().parameter("branch").value(branch).notExceedingLengthOf(50).ignoreIfNull().isAlphaWithSpace();
        }

        if (this.fromApiJsonHelper.extractStringNamed("modelLimit", element) != null) {
            final BigDecimal modelLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("modelLimit", element);
            baseDataValidator.reset().parameter("modelLimit").ignoreIfNull().value(modelLimit).positiveAmount();
        }

            final BigDecimal approvedLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("approvedLimit", element);
            baseDataValidator.reset().parameter("approvedLimit").value(approvedLimit).notNull().notBlank().positiveAmount();

        if (this.fromApiJsonHelper.extractStringNamed("pilotLimit", element) != null) {
            final BigDecimal pilotLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("pilotLimit", element);
            baseDataValidator.reset().parameter("pilotLimit").value(pilotLimit).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.extractStringNamed("partnerFloatLimit", element) != null) {
            final BigDecimal partnerFloatLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("partnerFloatLimit", element);
            baseDataValidator.reset().parameter("partnerFloatLimit").value(partnerFloatLimit).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.extractStringNamed("balanceLimit", element) != null) {
            final BigDecimal balanceLimit = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("balanceLimit", element);
            baseDataValidator.reset().parameter("balanceLimit").value(balanceLimit).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.extractLocalDateNamed("agreementStartDate", element) != null) {
            final LocalDate agreementStartDate = this.fromApiJsonHelper.extractLocalDateNamed("agreementStartDate", element);
            baseDataValidator.reset().parameter("agreementStartDate").value(agreementStartDate).value(agreementStartDate).notNull();
        }

        if (this.fromApiJsonHelper.extractLocalDateNamed("agreementExpiryDate", element) != null) {
            final LocalDate agreementExpiryDate = this.fromApiJsonHelper.extractLocalDateNamed("agreementExpiryDate", element);
            baseDataValidator.reset().parameter("agreementExpiryDate").value(agreementExpiryDate).value(agreementExpiryDate).notNull();
        }

        if (this.fromApiJsonHelper.extractLongNamed("underlyingAssets", element) != null) {
            final long underlyingAssets = this.fromApiJsonHelper.extractLongNamed("underlyingAssets", element);
            baseDataValidator.reset().parameter("underlyingAssets").value(underlyingAssets).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("security", element) != null) {
            final long security = this.fromApiJsonHelper.extractLongNamed("security", element);
            baseDataValidator.reset().parameter("security").value(security).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed("fldgCalculationOn", element) != null) {
            final long fldgCalculationOn = this.fromApiJsonHelper.extractLongNamed("fldgCalculationOn", element);
            baseDataValidator.reset().parameter("fldgCalculationOn").value(fldgCalculationOn).notBlank().longGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}
