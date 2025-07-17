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
package org.vcpl.lms.portfolio.partner.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.infrastructure.core.service.DateUtils;

@Entity
@Table(name = "m_partner", uniqueConstraints = { @UniqueConstraint(columnNames = { "partner_name" }, name = "name_org"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "externalid_org") })
public class Partner extends AbstractPersistableCustom implements Serializable {


//    @ManyToOne
//    @JoinColumn(name =  "office_id", nullable = false)
//    private Office office;

    @Column(name = "partner_name", nullable = false, length = 75)
    private String partnerName;

    @Column(name = "partner_company_registration_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date partnerCompanyRegistrationDate;

    @ManyToOne
    @JoinColumn(name = "source_cv_id", nullable = true)
    private CodeValue source;

    @Column(name = "pancard", nullable = true, length = 10)
    private String panCard;

    @Column(name = "cin_number", nullable = true, length = 30)
    private String cinNumber;

    @Column(name = "address1", nullable = true, length = 100)
    private String address1;

    @Column(name = "address2", nullable = true, length = 100)
    private String address2;

    @Column(name = "city", nullable = true,length = 50)
    private String city;

    @ManyToOne
    @JoinColumn(name = "state_cv_id", nullable = true)
    private CodeValue state;

    @Column(name = "pincode", nullable = true, length = 6)
    private Long pincode;

    @ManyToOne
    @JoinColumn(name = "country_cv_id", nullable = true)
    private CodeValue country;

    @ManyToOne
    @JoinColumn(name = "constitution_cv_id", nullable = true)
    private CodeValue constitution;

    @Column(name = "key_persons", nullable = true, length = 30)
    private String keyPersons;

    @ManyToOne
    @JoinColumn(name = "industry_cv_id", nullable = true)
    private CodeValue industry;

    @ManyToOne
    @JoinColumn(name = "sector_cv_id", nullable = true)
    private CodeValue sector;

    @ManyToOne
    @JoinColumn(name = "sub_sector_cv_id", nullable = true)
    private CodeValue subSector;

    @Column(name = "gst_number", nullable = true, length = 15)
    private String gstNumber;

    @ManyToOne
    @JoinColumn(name = "gst_registration_cv_id", nullable = true)
    private CodeValue gstRegistration;

    @ManyToOne
    @JoinColumn(name = "partner_type_cv_id", nullable = true)
    private CodeValue partnerType;

    @Column(name = "beneficiary_name", nullable = true, length = 75)
    private String beneficiaryName;

    @Column(name = "beneficiary_account_number", nullable = true, length = 40)
    private String beneficiaryAccountNumber;

    @Column(name = "ifsc_code", nullable = true, length = 11)
    private String ifscCode;

    @Column(name = "micr_code", nullable = true, length = 9)
    private Long micrCode;

    @Column(name = "swift_code", nullable = true, length = 11)
    private String swiftCode;

    @Column(name = "branch", nullable = true, length = 50)
    private String branch;

    @Column(name = "model_limit", nullable = true, scale = 6, precision = 19)
    private BigDecimal modelLimit;

    @Column(name = "approved_limit", nullable = true, scale = 6, precision = 19)
    private BigDecimal approvedLimit;

    @Column(name = "pilot_limit", nullable = true, scale = 6, precision = 19)
    private BigDecimal pilotLimit;

    @Column(name = "partner_float_limit", nullable = true, scale = 6, precision = 19)
    private BigDecimal partnerFloatLimit;

    @Column(name = "balance_limit", nullable = true, scale = 6, precision = 19)
    private BigDecimal balanceLimit;

    @Column(name = "agreement_start_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date agreementStartDate;

    @Column(name = "agreement_expiry_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date agreementExpiryDate;

    @ManyToOne
    @JoinColumn(name = "underlying_assets_cv_id", nullable = true)
    private CodeValue underlyingAssets;

    @ManyToOne
    @JoinColumn(name = "security_cv_id", nullable = true)
    private CodeValue security;

    @ManyToOne
    @JoinColumn(name = "fldg_calculation_on_cv_id", nullable = true)
    private CodeValue fldgCalculationOn;

    public static Partner fromJson(final JsonCommand command, final CodeValue source,
                                    final CodeValue state, final CodeValue country,
                                   final CodeValue constitution, final CodeValue industry, final CodeValue sector,
                                   final CodeValue subSector, final CodeValue gstRegistration, final CodeValue partnerType,
                                   final CodeValue underlyingAssets, final CodeValue security, final CodeValue fldgCalculationOn) {

        final String partnerName = command.stringValueOfParameterNamed("partnerName");
        final LocalDate partnerCompanyRegistrationDate = command.localDateValueOfParameterNamed("partnerCompanyRegistrationDate");
        final String panCard = command.stringValueOfParameterNamed("panCard");
        final String cinNumber = command.stringValueOfParameterNamed("cinNumber");
        final String address1 = command.stringValueOfParameterNamed("address1");
        final String address2 = command.stringValueOfParameterNamed("address2");
        final Long pincode = command.longValueOfParameterNamed("pincode");
        final String keyPersons = command.stringValueOfParameterNamed("keyPersons");
        final String gstNumber = command.stringValueOfParameterNamed("gstNumber");
        final String beneficiaryName = command.stringValueOfParameterNamed("beneficiaryName");
        final String beneficiaryAccountNumber = command.stringValueOfParameterNamed("beneficiaryAccountNumber");
        final String ifscCode = command.stringValueOfParameterNamed("ifscCode");
        final Long micrCode = command.longValueOfParameterNamed("micrCode");
        final String swiftCode = command.stringValueOfParameterNamed("swiftCode");
        final String branch = command.stringValueOfParameterNamed("branch");
        final BigDecimal modelLimit = command.bigDecimalValueOfParameterNamed("modelLimit");
        final BigDecimal approvedLimit = command.bigDecimalValueOfParameterNamed("approvedLimit");
        final BigDecimal pilotLimit = command.bigDecimalValueOfParameterNamed("pilotLimit");
        final BigDecimal partnerFloatLimit = command.bigDecimalValueOfParameterNamed("partnerFloatLimit");
        final LocalDate agreementStartDate = command.localDateValueOfParameterNamed("agreementStartDate");
        final LocalDate agreementExpiryDate = command.localDateValueOfParameterNamed("agreementExpiryDate");
        final BigDecimal balanceLimitcalc = approvedLimit;
        final String city = command.stringValueOfParameterNamed("city");



        return new Partner(partnerName, partnerCompanyRegistrationDate, source, panCard,cinNumber, address1, address2, state, pincode, country,
                constitution, keyPersons, industry, sector, subSector, gstNumber, gstRegistration, partnerType, beneficiaryName,
                beneficiaryAccountNumber, ifscCode, micrCode, swiftCode, branch, modelLimit, approvedLimit, pilotLimit, partnerFloatLimit,
                balanceLimitcalc, agreementStartDate, agreementExpiryDate, underlyingAssets, security, fldgCalculationOn,city);
    }

    protected Partner() {

        this.partnerName = null;
        this.partnerCompanyRegistrationDate = null;
        this.source = null;
        this.panCard = null;
        this.cinNumber = null;
        this.address1 = null;
        this.address2 = null;
        this.state = null;
        this.pincode = null;
        this.country = null;
        this.constitution = null;
        this.keyPersons = null;
        this.industry = null;
        this.sector = null;
        this.subSector = null;
        this.gstNumber = null;
        this.gstRegistration = null;
        this.partnerType = null;
        this.beneficiaryName = null;
        this.beneficiaryAccountNumber = null;
        this.ifscCode = null;
        this.micrCode = null;
        this.swiftCode = null;
        this.branch = null;
        this.modelLimit = null;
        this.approvedLimit = null;
        this.pilotLimit = null;
        this.partnerFloatLimit = null;
        this.balanceLimit = null;
        this.agreementStartDate = null;
        this.agreementExpiryDate = null;
        this.underlyingAssets = null;
        this.security = null;
        this.fldgCalculationOn = null;
        this.city = null;


    }

    private Partner(final String partnerName, final LocalDate partnerCompanyRegistrationDate, final CodeValue source, final String panCard,
                    final String cinNumber, final String address1, final String address2, final CodeValue state,
                    final Long pincode, final CodeValue country, final CodeValue constitution, final String keyPersons,
                    final CodeValue industry, final CodeValue sector, final CodeValue subSector, final String gstNumber, final CodeValue gstRegistration,
                    final CodeValue partnerType, final String beneficiaryName, final String beneficiaryAccountNumber, final String ifscCode,
                    final Long micrCode, final String swiftCode, final String branch, final BigDecimal modelLimit,
                    final BigDecimal approvedLimit, final BigDecimal pilotLimit, final BigDecimal partnerFloatLimit,
                    final BigDecimal balanceLimit, final LocalDate agreementStartDate, final LocalDate agreementExpiryDate,
                    final CodeValue underlyingAssets, final CodeValue security, final CodeValue fldgCalculationOn,final String city) {
//        this.parent = parent;
//        this.openingDate = Date.from(openingDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        if (parent != null) {
//            this.parent.addChild(this);
//        }
//
//        if (StringUtils.isNotBlank(name)) {
//            this.name = name.trim();
//        } else {
//            this.name = null;
//        }
//        if (StringUtils.isNotBlank(externalId)) {
//            this.externalId = externalId.trim();
//        } else {
//            this.externalId = null;
//        }
//
//        this.limit = (limit == null ? BigDecimal.ZERO : limit);
//
//        this.expiryDate = Date.from(expiryDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        this.partnerName = partnerName;

        if (partnerCompanyRegistrationDate != null) {
            this.partnerCompanyRegistrationDate = Date.from(partnerCompanyRegistrationDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (source != null) {
            this.source = source;
        }

        if (StringUtils.isNotBlank(panCard)) {
            this.panCard = panCard.trim();
        } else {
            this.panCard = null;
        }

        if (StringUtils.isNotBlank(cinNumber)) {
            this.cinNumber = cinNumber.trim();
        } else {
            this.cinNumber = null;
        }

        if (StringUtils.isNotBlank(address1)) {
            this.address1 = address1.trim();
        } else {
            this.address1 = null;
        }

        if (StringUtils.isNotBlank(address2)) {
            this.address2 = address2.trim();
        } else {
            this.address2 = null;
        }



        if (state != null) {
            this.state = state;
        }

        this.pincode = pincode;

        if (country != null) {
            this.country = country;
        }

        if (constitution != null) {
            this.constitution = constitution;
        }

        if (StringUtils.isNotBlank(keyPersons)) {
            this.keyPersons = keyPersons.trim();
        } else {
            this.keyPersons = null;
        }

        if (industry != null) {
            this.industry = industry;
        }

        if (sector != null) {
            this.sector = sector;
        }

        if (subSector != null) {
            this.subSector = subSector;
        }

        if (StringUtils.isNotBlank(gstNumber)) {
            this.gstNumber = gstNumber.trim();
        } else {
            this.gstNumber = null;
        }

        if (gstRegistration != null) {
            this.gstRegistration = gstRegistration;
        }

        if (partnerType != null) {
            this.partnerType = partnerType;
        }

        if (StringUtils.isNotBlank(beneficiaryName)) {
            this.beneficiaryName = beneficiaryName.trim();
        } else {
            this.beneficiaryName = null;
        }

        this.beneficiaryAccountNumber = beneficiaryAccountNumber;


        if (StringUtils.isNotBlank(ifscCode)) {
            this.ifscCode = ifscCode.trim();
        } else {
            this.ifscCode = null;
        }

        if (StringUtils.isNotBlank(city)) {
            this.city = city.trim();
        } else {
            this.city = null;
        }

        this.micrCode = micrCode;

        if (StringUtils.isNotBlank(swiftCode)) {
            this.swiftCode = swiftCode.trim();
        } else {
            this.swiftCode = null;
        }

        if (StringUtils.isNotBlank(branch)) {
            this.branch = branch.trim();
        } else {
            this.branch = null;
        }

        this.modelLimit = modelLimit;
        this.approvedLimit = approvedLimit;
        this.pilotLimit = pilotLimit;
        this.partnerFloatLimit = partnerFloatLimit;
        this.balanceLimit = balanceLimit;

        if (agreementStartDate != null) {
            this.agreementStartDate = Date.from(agreementStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (agreementExpiryDate != null) {
            this.agreementExpiryDate = Date.from(agreementExpiryDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (underlyingAssets != null) {
            this.underlyingAssets = underlyingAssets;
        }

        if (security != null) {
            this.security = security;
        }

        if (fldgCalculationOn != null) {
            this.fldgCalculationOn = fldgCalculationOn;
        }

    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String partnerNameParamName = "partnerName";
        if (command.isChangeInStringParameterNamed(partnerNameParamName, this.partnerName)) {
            final String newValue = command.stringValueOfParameterNamed(partnerNameParamName);
            actualChanges.put(partnerNameParamName, newValue);
            this.partnerName = newValue;
        }

        final String partnerCompanyRegistrationDateParamName = "partnerCompanyRegistrationDate";
        if (command.isChangeInLocalDateParameterNamed(partnerCompanyRegistrationDateParamName, getPartnerCompanyRegistrationDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(partnerCompanyRegistrationDateParamName);
            actualChanges.put(partnerCompanyRegistrationDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(partnerCompanyRegistrationDateParamName);
            if (newValue != null) {
                this.partnerCompanyRegistrationDate = Date.from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } else {
                this.partnerCompanyRegistrationDate = null;
            }
        }

        final String sourceParamName = "source";
        if (command.isChangeInLongParameterNamed(sourceParamName, sourceId())) {
            final Long newValue = command.longValueOfParameterNamed(sourceParamName);
            actualChanges.put(sourceParamName, newValue);
        }

        final String panCardParamName = "panCard";
        if (command.isChangeInStringParameterNamed(panCardParamName, this.panCard)) {
            final String newValue = command.stringValueOfParameterNamed(panCardParamName);
            actualChanges.put(panCardParamName, newValue);
            this.panCard = newValue;
            this.panCard = StringUtils.defaultIfBlank(newValue, null);
        }

        final String cinNumberParamName = "cinNumber";
        if (command.isChangeInStringParameterNamed(cinNumberParamName, this.cinNumber)) {
            final String newValue = command.stringValueOfParameterNamed(cinNumberParamName);
            actualChanges.put(cinNumberParamName, newValue);
            this.cinNumber = newValue;
            this.cinNumber = StringUtils.defaultIfBlank(newValue, null);
        }

        final String address1ParamName = "address1";
        if (command.isChangeInStringParameterNamed(address1ParamName, this.address1)) {
            final String newValue = command.stringValueOfParameterNamed(address1ParamName);
            actualChanges.put(address1ParamName, newValue);
            this.address1 = newValue;
            this.address1 = StringUtils.defaultIfBlank(newValue, null);
        }

        final String address2ParamName = "address2";
        if (command.isChangeInStringParameterNamed(address2ParamName, this.address2)) {
            final String newValue = command.stringValueOfParameterNamed(address2ParamName);
            actualChanges.put(address2ParamName, newValue);
            this.address2 = newValue;
            this.address2 = StringUtils.defaultIfBlank(newValue, null);
        }



        final String stateParamName = "state";
        if (command.isChangeInLongParameterNamed(stateParamName, stateId())) {
            final Long newValue = command.longValueOfParameterNamed(stateParamName);
            actualChanges.put(stateParamName, newValue);
        }

        final String pincodeParamName = "pincode";
        if (command.isChangeInLongParameterNamed(pincodeParamName, this.pincode)) {
            final Long newValue = command.longValueOfParameterNamed(pincodeParamName);
            actualChanges.put(pincodeParamName, newValue);
            this.pincode = newValue;
        }

        final String countryParamName = "country";
        if (command.isChangeInLongParameterNamed(countryParamName, countryId())) {
            final Long newValue = command.longValueOfParameterNamed(countryParamName);
            actualChanges.put(countryParamName, newValue);
        }

        final String constitutionParamName = "constitution";
        if (command.isChangeInLongParameterNamed(constitutionParamName, constitutionId())) {
            final Long newValue = command.longValueOfParameterNamed(constitutionParamName);
            actualChanges.put(constitutionParamName, newValue);
        }

        final String keyPersonsParamName = "keyPersons";
        if (command.isChangeInStringParameterNamed(keyPersonsParamName, this.keyPersons)) {
            final String newValue = command.stringValueOfParameterNamed(keyPersonsParamName);
            actualChanges.put(keyPersonsParamName, newValue);
            this.keyPersons = newValue;
            this.keyPersons = StringUtils.defaultIfBlank(newValue, null);
        }

        final String industryParamName = "industry";
        if (command.isChangeInLongParameterNamed(industryParamName, industryId())) {
            final Long newValue = command.longValueOfParameterNamed(industryParamName);
            actualChanges.put(industryParamName, newValue);
        }

        final String sectorParamName = "sector";
        if (command.isChangeInLongParameterNamed(sectorParamName, sectorId())) {
            final Long newValue = command.longValueOfParameterNamed(sectorParamName);
            actualChanges.put(sectorParamName, newValue);
        }

        final String subSectorParamName = "subSector";
        if (command.isChangeInLongParameterNamed(subSectorParamName, subSectorId())) {
            final Long newValue = command.longValueOfParameterNamed(subSectorParamName);
            actualChanges.put(subSectorParamName, newValue);
        }

        final String gstNumberParamName = "gstNumber";
        if (command.isChangeInStringParameterNamed(gstNumberParamName, this.gstNumber)) {
            final String newValue = command.stringValueOfParameterNamed(gstNumberParamName);
            actualChanges.put(gstNumberParamName, newValue);
            this.gstNumber = newValue;
            this.gstNumber = StringUtils.defaultIfBlank(newValue, null);
        }

        final String gstRegistrationParamName = "gstRegistration";
        if (command.isChangeInLongParameterNamed(gstRegistrationParamName, gstRegistrationId())) {
            final Long newValue = command.longValueOfParameterNamed(gstRegistrationParamName);
            actualChanges.put(gstRegistrationParamName, newValue);
        }

        final String partnerTypeParamName = "partnerType";
        if (command.isChangeInLongParameterNamed(partnerTypeParamName, partnerTypeId())) {
            final Long newValue = command.longValueOfParameterNamed(partnerTypeParamName);
            actualChanges.put(partnerTypeParamName, newValue);
        }

        final String beneficiaryNameParamName = "beneficiaryName";
        if (command.isChangeInStringParameterNamed(beneficiaryNameParamName, this.beneficiaryName)) {
            final String newValue = command.stringValueOfParameterNamed(beneficiaryNameParamName);
            actualChanges.put(beneficiaryNameParamName, newValue);
            this.beneficiaryName = newValue;
            this.beneficiaryName = StringUtils.defaultIfBlank(newValue, null);
        }

        final String beneficiaryAccountNumberParamName = "beneficiaryAccountNumber";
        if (command.isChangeInStringParameterNamed(beneficiaryAccountNumberParamName, this.beneficiaryAccountNumber)) {
            final String newValue = command.stringValueOfParameterNamed(beneficiaryAccountNumberParamName);
            actualChanges.put(beneficiaryAccountNumberParamName, newValue);
            this.beneficiaryAccountNumber = newValue;
        }

        final String ifscCodeParamName = "ifscCode";
        if (command.isChangeInStringParameterNamed(ifscCodeParamName, this.ifscCode)) {
            final String newValue = command.stringValueOfParameterNamed(ifscCodeParamName);
            actualChanges.put(ifscCodeParamName, newValue);
            this.ifscCode = newValue;
            this.ifscCode = StringUtils.defaultIfBlank(newValue, null);
        }

        final String cityParamName = "city";
        if (command.isChangeInStringParameterNamed(cityParamName, this.city)) {
            final String newValue = command.stringValueOfParameterNamed(cityParamName);
            actualChanges.put(cityParamName, newValue);
            this.city = newValue;
            this.city = StringUtils.defaultIfBlank(newValue, null);
        }

        final String micrCodeParamName = "micrCode";
        if (command.isChangeInLongParameterNamed(micrCodeParamName, this.micrCode)) {
            final Long newValue = command.longValueOfParameterNamed(micrCodeParamName);
            actualChanges.put(micrCodeParamName, newValue);
            this.micrCode = newValue;
        }

        final String swiftCodeParamName = "swiftCode";
        if (command.isChangeInStringParameterNamed(swiftCodeParamName, this.swiftCode)) {
            final String newValue = command.stringValueOfParameterNamed(swiftCodeParamName);
            actualChanges.put(swiftCodeParamName, newValue);
            this.swiftCode = newValue;
            this.swiftCode = StringUtils.defaultIfBlank(newValue, null);
        }

        final String branchParamName = "branch";
        if (command.isChangeInStringParameterNamed(branchParamName, this.branch)) {
            final String newValue = command.stringValueOfParameterNamed(branchParamName);
            actualChanges.put(branchParamName, newValue);
            this.branch = newValue;
            this.branch = StringUtils.defaultIfBlank(newValue, null);
        }

        final String modelLimitParamName = "modelLimit";
        if (command.isChangeInBigDecimalParameterNamed(modelLimitParamName, this.modelLimit)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(modelLimitParamName);
            actualChanges.put(modelLimitParamName, newValue);
            this.modelLimit = newValue;
        }

        final String approvedLimitParamName = "approvedLimit";
        if (command.isChangeInBigDecimalParameterNamed(approvedLimitParamName, this.approvedLimit)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(approvedLimitParamName);
            actualChanges.put(approvedLimitParamName, newValue);
            this.approvedLimit = newValue;
        }

        final String pilotLimitParamName = "pilotLimit";
        if (command.isChangeInBigDecimalParameterNamed(pilotLimitParamName, this.pilotLimit)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(pilotLimitParamName);
            actualChanges.put(pilotLimitParamName, newValue);
            this.pilotLimit = newValue;
        }

        final String partnerFloatLimitParamName = "partnerFloatLimit";
        if (command.isChangeInBigDecimalParameterNamed(partnerFloatLimitParamName, this.partnerFloatLimit)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(partnerFloatLimitParamName);
            actualChanges.put(partnerFloatLimitParamName, newValue);
            this.partnerFloatLimit = newValue;
        }

//        final String balanceLimitParamName = "balanceLimit";
//        if (command.isChangeInBigDecimalParameterNamed(balanceLimitParamName, this.balanceLimit)) {
//            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(balanceLimitParamName);
//            actualChanges.put(balanceLimitParamName, newValue);
//            this.balanceLimit = newValue;
//        }

        final String agreementStartDateParamName = "agreementStartDate";
        if (command.isChangeInLocalDateParameterNamed(agreementStartDateParamName, getAgreementStartDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(agreementStartDateParamName);
            actualChanges.put(agreementStartDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(agreementStartDateParamName);
            if (newValue != null) {
                this.agreementStartDate = Date.from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } else {
                this.agreementStartDate = null;
            }
        }

        final String agreementExpiryDateParamName = "agreementExpiryDate";
        if (command.isChangeInLocalDateParameterNamed(agreementExpiryDateParamName, getAgreementExpiryDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(agreementExpiryDateParamName);
            actualChanges.put(agreementExpiryDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(agreementExpiryDateParamName);
            if (newValue != null) {
                this.agreementExpiryDate = Date.from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } else {
                this.agreementExpiryDate = null;
            }
        }

        final String underlyingAssetsParamName = "underlyingAssets";
        if (command.isChangeInLongParameterNamed(underlyingAssetsParamName, underlyingAssetsId())) {
            final Long newValue = command.longValueOfParameterNamed(underlyingAssetsParamName);
            actualChanges.put(underlyingAssetsParamName, newValue);
        }

        final String securityParamName = "security";
        if (command.isChangeInLongParameterNamed(securityParamName, securityId())) {
            final Long newValue = command.longValueOfParameterNamed(securityParamName);
            actualChanges.put(securityParamName, newValue);
        }

        final String fldgCalculationOnParamName = "fldgCalculationOn";
        if (command.isChangeInLongParameterNamed(fldgCalculationOnParamName, fldgCalculationOnId())) {
            final Long newValue = command.longValueOfParameterNamed(fldgCalculationOnParamName);
            actualChanges.put(fldgCalculationOnParamName, newValue);
        }

        return actualChanges;
    }

    public LocalDate getPartnerCompanyRegistrationDate() {
        LocalDate partnerCompanyRegistrationDate = null;
        if (this.partnerCompanyRegistrationDate != null) {
            partnerCompanyRegistrationDate = LocalDate.ofInstant(this.partnerCompanyRegistrationDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }
        return partnerCompanyRegistrationDate;
    }

    public LocalDate getAgreementStartDate() {
        LocalDate agreementStartDate = null;
        if (this.agreementStartDate != null) {
            agreementStartDate = LocalDate.ofInstant(this.agreementStartDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }
        return agreementStartDate;
    }

    public LocalDate getAgreementExpiryDate() {
        LocalDate agreementExpiryDate = null;
        if (this.agreementExpiryDate != null) {
            agreementExpiryDate = LocalDate.ofInstant(this.agreementExpiryDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }
        return agreementExpiryDate;
    }

    public boolean identifiedBy(final Long id) {
        return getId().equals(id);
    }

    public String getPartnerName() {
        return this.partnerName;
    }

    public BigDecimal getApprovedLimit() {
        return approvedLimit;
    }

    public BigDecimal getBalanceLimit() {
        return balanceLimit;
    }

    public void setBalanceLimit(BigDecimal balanceLimit) {
        this.balanceLimit = balanceLimit;
    }

    public boolean doesNotHaveAnPartnerInHierarchyWithId(final Long partnerId) {
        return !hasAnPartnerInHierarchyWithId(partnerId);
    }

    private boolean hasAnPartnerInHierarchyWithId(final Long partnerId) {

        boolean match = false;

        if (identifiedBy(partnerId)) {
            match = true;
        }

        return match;
    }

    private Long sourceId() {
        Long sourceId = null;
        if (this.source != null) {
            sourceId = this.source.getId();
        }
        return sourceId;
    }

    private Long stateId() {
        Long stateId = null;
        if (this.state != null) {
            stateId = this.state.getId();
        }
        return stateId;
    }

    private Long countryId() {
        Long countryId = null;
        if (this.country != null) {
            countryId = this.country.getId();
        }
        return countryId;
    }

    private Long constitutionId() {
        Long constitutionId = null;
        if (this.constitution != null) {
            constitutionId = this.constitution.getId();
        }
        return constitutionId;
    }

    private Long industryId() {
        Long industryId = null;
        if (this.industry != null) {
            industryId = this.industry.getId();
        }
        return industryId;
    }

    private Long sectorId() {
        Long sectorId = null;
        if (this.sector != null) {
            sectorId = this.sector.getId();
        }
        return sectorId;
    }

    private Long subSectorId() {
        Long subSectorId = null;
        if (this.subSector != null) {
            subSectorId = this.subSector.getId();
        }
        return subSectorId;
    }

    private Long gstRegistrationId() {
        Long gstRegistrationId = null;
        if (this.gstRegistration != null) {
            gstRegistrationId = this.gstRegistration.getId();
        }
        return gstRegistrationId;
    }

    private Long partnerTypeId() {
        Long partnerTypeId = null;
        if (this.partnerType != null) {
            partnerTypeId = this.partnerType.getId();
        }
        return partnerTypeId;
    }

    private Long underlyingAssetsId() {
        Long underlyingAssetsId = null;
        if (this.underlyingAssets != null) {
            underlyingAssetsId = this.underlyingAssets.getId();
        }
        return underlyingAssetsId;
    }

    private Long securityId() {
        Long securityId = null;
        if (this.security != null) {
            securityId = this.security.getId();
        }
        return securityId;
    }

    private Long fldgCalculationOnId() {
        Long fldgCalculationOnId = null;
        if (this.fldgCalculationOn != null) {
            fldgCalculationOnId = this.fldgCalculationOn.getId();
        }
        return fldgCalculationOnId;
    }

    public CodeValue source() { return this.source; }

    public void updateSource(CodeValue source) { this.source = source; }

    public CodeValue state() { return this.state; }

    public void updateState(CodeValue state) { this.state = state; }

    public CodeValue country() { return this.country; }

    public void updateCountry(CodeValue country) { this.country = country; }

    public CodeValue constitution() { return this.constitution; }

    public void updateConstitution(CodeValue constitution) { this.constitution = constitution; }

    public CodeValue industry() { return this.industry; }

    public void updateIndustry(CodeValue industry) { this.industry = industry; }

    public CodeValue sector() { return this.sector; }

    public void updateSector(CodeValue sector) { this.sector = sector; }

    public CodeValue subSector() { return this.subSector; }

    public void updateSubSector(CodeValue subSector) { this.subSector = subSector; }

    public CodeValue gstRegistration() { return this.gstRegistration; }

    public void updateGstRegistration(CodeValue gstRegistration) { this.gstRegistration = gstRegistration; }

    public CodeValue partnerType() { return this.partnerType; }

    public void updatePartnerType(CodeValue partnerType) { this.partnerType = partnerType; }

    public CodeValue underlyingAssets() { return this.underlyingAssets; }

    public void updateUnderlyingAssets(CodeValue underlyingAssets) { this.underlyingAssets = underlyingAssets; }

    public CodeValue security() { return this.security; }

    public void updateSecurity(CodeValue security) { this.security = security; }

    public CodeValue fldgCalculationOn() { return this.fldgCalculationOn; }

    public void updateFldgCalculationOn(CodeValue fldgCalculationOn) { this.fldgCalculationOn = fldgCalculationOn; }

    public void UpdateBalanceLimit(BigDecimal reducedAmount) {
        if(reducedAmount!=null){
            this.balanceLimit=reducedAmount;
        }
        else{
            this.balanceLimit=this.balanceLimit;
        }
    }
}