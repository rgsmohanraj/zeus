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
package org.vcpl.lms.portfolio.partner.data;

import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.domain.Code;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

/**
 * Immutable data object for partner data.
 */
public class PartnerData implements Serializable {

    private final Long id;
    private final String partnerName;
    private final LocalDate partnerCompanyRegistrationDate;
    private final CodeValueData source;
    private final String panCard;
    private final String cinNumber;
    private final String address1;
    private final String address2;
    private final CodeValueData state;
    private final Long pincode;
    private final CodeValueData country;
    private final CodeValueData constitution;
    private final String keyPersons;
    private final CodeValueData industry;
    private final CodeValueData sector;
    private final CodeValueData subSector;
    private final String gstNumber;
    private final CodeValueData gstRegistration;
    private final CodeValueData partnerType;
    private final String beneficiaryName;
    private final String beneficiaryAccountNumber;
    private final String ifscCode;
    private final Long micrCode;
    private final String swiftCode;
    private final String branch;
    private final BigDecimal modelLimit;
    private final BigDecimal approvedLimit;
    private final BigDecimal pilotLimit;
    private final BigDecimal partnerFloatLimit;
    private final BigDecimal balanceLimit;
    private final LocalDate agreementStartDate;
    private final LocalDate agreementExpiryDate;
    private final CodeValueData underlyingAssets;
    private final CodeValueData security;
    private final CodeValueData fldgCalculationOn;
    private final String city;

//    template

    private final Collection<CodeValueData> sourceOptions;
    private final Collection<CodeValueData> stateOptions;
    private final Collection<CodeValueData> countryOptions;
    private final Collection<CodeValueData> constitutionOptions;
    private final Collection<CodeValueData> industryOptions;
    private final Collection<CodeValueData> sectorOptions;
    private final Collection<CodeValueData> subSectorOptions;
    private final Collection<CodeValueData> gstRegistrationOptions;
    private final Collection<CodeValueData> partnerTypeOptions;
    private final Collection<CodeValueData> underlyingAssetsOptions;
    private final Collection<CodeValueData> securityOptions;
    private final Collection<CodeValueData> fldgCalculationOnOptions;



    // import fields
    private transient Integer rowIndex;
    private String locale;
    private String dateFormat;
//    private Long OfficeId;

    public static PartnerData importInstance(final String name, final Long parentId, final LocalDate openingDate, final String externalId) {
//        return new PartnerData(null, name, null, externalId, openingDate, null, parentId, null, null,null,null);
        return null;
    }

    public void setImportFields(final Integer rowIndex, final String locale, final String dateFormat) {
        this.rowIndex = rowIndex;
        this.locale = locale;
        this.dateFormat = dateFormat;
    }

    public static PartnerData testInstance(final Long id, final String name) {
//        return new PartnerData(id, name, null, null, null, null, null,null,null,null,null);
        return null;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public Long getId() {
        return id;
    }

    public static PartnerData dropdown(final Long id, final String partnerName, final Collection<CodeValueData> sourceOptions,
//                                       final Collection<CodeValueData> cityOptions,
                                       final Collection<CodeValueData> stateOptions,
                                       final Collection<CodeValueData> countryOptions, final Collection<CodeValueData> constitutionOptions,
                                       final Collection<CodeValueData> industryOptions, final Collection<CodeValueData> sectorOptions,
                                       final Collection<CodeValueData> subSectorOptions, final Collection<CodeValueData> gstRegistrationOptions,
                                       final Collection<CodeValueData> partnerTypeOptions, final Collection<CodeValueData> underlyingAssetsOptions,
                                       final Collection<CodeValueData> securityOptions, final Collection<CodeValueData> fldgCalculationOnOptions) {

        return new PartnerData(id, partnerName, null, null, null, null, null, null,null,null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, sourceOptions,
                 stateOptions, countryOptions, constitutionOptions, industryOptions, sectorOptions, subSectorOptions,
                gstRegistrationOptions, partnerTypeOptions, underlyingAssetsOptions, securityOptions, fldgCalculationOnOptions);
    }

    public static PartnerData template(final Collection<CodeValueData> sourceOptions,  final Collection<CodeValueData> stateOptions,
                                       final Collection<CodeValueData> countryOptions, final Collection<CodeValueData> constitutionOptions, final Collection<CodeValueData> industryOptions,
                                       final Collection<CodeValueData> sectorOptions, final Collection<CodeValueData> subSectorOptions,
                                       final Collection<CodeValueData> gstRegistrationOptions, final Collection<CodeValueData> partnerTypeOptions,
                                       final Collection<CodeValueData> underlyingAssetsOptions, final Collection<CodeValueData> securityOptions,
                                       final Collection<CodeValueData> fldgCalculationOnOptions) {

        final Long id = null;
        final String partnerName = null;
        final LocalDate partnerCompanyRegistrationDate = null;
        final CodeValueData source = null;
        final String panCard = null;
        final String cinNumber = null;
        final String address1 = null;
        final String address2 = null;
        final CodeValueData state = null;
        final Long pincode = null;
        final CodeValueData country = null;
        final CodeValueData constitution = null;
        final String keyPersons = null;
        final CodeValueData industry = null;
        final CodeValueData sector = null;
        final CodeValueData subSector = null;
        final String gstNumber = null;
        final CodeValueData gstRegistration = null;
        final CodeValueData partnerType = null;
        final String beneficiaryName = null;
        final String beneficiaryAccountNumber = null;
        final String ifscCode = null;
        final Long micrCode = null;
        final String swiftCode = null;
        final String branch = null;
        final BigDecimal modelLimit = null;
        final BigDecimal approvedLimit = null;
        final BigDecimal pilotLimit = null;
        final BigDecimal partnerFloatLimit = null;
        final BigDecimal balanceLimit = null;
        final LocalDate agreementStartDate = null;
        final LocalDate agreementExpiryDate = null;
        final CodeValueData underlyingAssets = null;
        final CodeValueData security = null;
        final CodeValueData fldgCalculationOn = null;
        final String city = null;

        return new PartnerData(id, partnerName, partnerCompanyRegistrationDate, source, panCard, cinNumber, address1,
                address2,  state, pincode, country, constitution, keyPersons, industry,
                sector, subSector, gstNumber, gstRegistration, partnerType, beneficiaryName, beneficiaryAccountNumber,
                ifscCode, micrCode, swiftCode, branch, modelLimit, approvedLimit, pilotLimit, partnerFloatLimit,
                balanceLimit, agreementStartDate, agreementExpiryDate, underlyingAssets, security, fldgCalculationOn,city, sourceOptions,
                 stateOptions, countryOptions, constitutionOptions, industryOptions, sectorOptions, subSectorOptions, gstRegistrationOptions,
                partnerTypeOptions, underlyingAssetsOptions, securityOptions, fldgCalculationOnOptions);
    }

    public static PartnerData appendedTemplate(final PartnerData partner, final PartnerData templateData) {
        return new PartnerData(partner.id, partner.partnerName, partner.partnerCompanyRegistrationDate, partner.source, partner.panCard,
                partner.cinNumber, partner.address1, partner.address2, partner.state, partner.pincode, partner.country,
                partner.constitution, partner.keyPersons, partner.industry, partner.sector, partner.subSector, partner.gstNumber,
                partner.gstRegistration, partner.partnerType, partner.beneficiaryName, partner.beneficiaryAccountNumber,
                partner.ifscCode, partner.micrCode, partner.swiftCode, partner.branch, partner.modelLimit, partner.approvedLimit,
                partner.pilotLimit, partner.partnerFloatLimit, partner.balanceLimit, partner.agreementStartDate,
                partner.agreementExpiryDate, partner.underlyingAssets, partner.security, partner.fldgCalculationOn,partner.city, templateData.sourceOptions,
                 templateData.stateOptions, templateData.countryOptions, templateData.constitutionOptions, templateData.industryOptions,
                templateData.sectorOptions, templateData.subSectorOptions, templateData.gstRegistrationOptions, templateData.partnerTypeOptions,
                templateData.underlyingAssetsOptions, templateData.securityOptions, templateData.fldgCalculationOnOptions);
    }

    public PartnerData(final Long id,  final String partnerName, final LocalDate partnerCompanyRegistrationDate, final CodeValueData source, final String panCard,
                       final String cinNumber, final String address1, final String address2, final CodeValueData state,
                       final Long pincode, final CodeValueData country, final CodeValueData constitution, final String keyPersons, final CodeValueData industry,
                       final CodeValueData sector, final CodeValueData subSector, final String gstNumber, final CodeValueData gstRegistration, final CodeValueData partnerType,
                       final String beneficiaryName, final String beneficiaryAccountNumber, final String ifscCode, final Long micrCode,
                       final String swiftCode, final String branch, final BigDecimal modelLimit, final BigDecimal approvedLimit,
                       final BigDecimal pilotLimit, final BigDecimal partnerFloatLimit, final BigDecimal balanceLimit,
                       final LocalDate agreementStartDate, final LocalDate agreementExpiryDate, final CodeValueData underlyingAssets,
                       final CodeValueData security, final CodeValueData fldgCalculationOn,final String city, final Collection<CodeValueData> sourceOptions,
                       final Collection<CodeValueData> stateOptions, final Collection<CodeValueData> countryOptions,
                       final Collection<CodeValueData> constitutionOptions, final Collection<CodeValueData> industryOptions,
                       final Collection<CodeValueData> sectorOptions, final Collection<CodeValueData> subSectorOptions,
                       final Collection<CodeValueData> gstRegistrationOptions, final Collection<CodeValueData> partnerTypeOptions,
                       final Collection<CodeValueData> underlyingAssetsOptions, final Collection<CodeValueData> securityOptions,
                       final Collection<CodeValueData> fldgCalculationOnOptions) {
        this.id = id;
        this.partnerName = partnerName;
        this.partnerCompanyRegistrationDate = partnerCompanyRegistrationDate;
        this.source = source;
        this.panCard = panCard;
        this.cinNumber = cinNumber;
        this.address1 = address1;
        this.address2 = address2;
        this.state = state;
        this.pincode = pincode;
        this.country = country;
        this.constitution = constitution;
        this.keyPersons = keyPersons;
        this.industry = industry;
        this.sector = sector;
        this.subSector = subSector;
        this.gstNumber = gstNumber;
        this.gstRegistration = gstRegistration;
        this.partnerType = partnerType;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.ifscCode = ifscCode;
        this.micrCode = micrCode;
        this.swiftCode = swiftCode;
        this.branch = branch;
        this.modelLimit = modelLimit;
        this.approvedLimit = approvedLimit;
        this.pilotLimit = pilotLimit;
        this.partnerFloatLimit = partnerFloatLimit;
        this.balanceLimit = balanceLimit;
        this.agreementStartDate = agreementStartDate;
        this.agreementExpiryDate = agreementExpiryDate;
        this.underlyingAssets = underlyingAssets;
        this.security = security;
        this.fldgCalculationOn = fldgCalculationOn;
        this.city = city;
        this.sourceOptions = sourceOptions;
        this.stateOptions = stateOptions;
        this.countryOptions = countryOptions;
        this.constitutionOptions = constitutionOptions;
        this.industryOptions = industryOptions;
        this.sectorOptions = sectorOptions;
        this.subSectorOptions = subSectorOptions;
        this.gstRegistrationOptions = gstRegistrationOptions;
        this.partnerTypeOptions = partnerTypeOptions;
        this.underlyingAssetsOptions = underlyingAssetsOptions;
        this.securityOptions = securityOptions;
        this.fldgCalculationOnOptions = fldgCalculationOnOptions;
    }

    public boolean hasIdentifyOf(final Long partnerId) {
        return this.id.equals(partnerId);
    }

    public String partnerName() {
        return this.partnerName;
    }
}
