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
package org.vcpl.lms.portfolio.client.data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import liquibase.pro.packaged.S;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.dataqueries.data.DatatableData;
import org.vcpl.lms.organisation.office.data.OfficeData;
import org.vcpl.lms.organisation.staff.data.StaffData;
import org.vcpl.lms.portfolio.address.data.AddressData;
import org.vcpl.lms.portfolio.client.domain.ClientEnumerations;
import org.vcpl.lms.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.vcpl.lms.portfolio.group.data.GroupGeneralData;
import org.vcpl.lms.portfolio.savings.data.SavingsAccountData;
import org.vcpl.lms.portfolio.savings.data.SavingsProductData;

/**
 * Immutable data object representing client data.
 */
@SuppressWarnings("unused")
public final class ClientData implements Comparable<ClientData>, Serializable {

    private final Long id;
    private final String accountNo;
    private final String externalId;

    private final EnumOptionData status;
    private final CodeValueData subStatus;

    private final Boolean active;
    private final LocalDate activationDate;

    private final String firstname;
    private final String middlename;
    private final String lastname;
    private final String beneficiaryName;
    private final String city;
    private final String beneficiaryAccountNumber;
    private final String ifscCode;
    private final Long micrCode;
    private final String swiftCode;
    private final String branch;
    private final String fullname;
    private final String displayName;
    private final String mobileNo;
    private final Integer age;
    private final String emailAddress;
    private final LocalDate dateOfBirth;
    private final CodeValueData gender;
    private final CodeValueData state;

    //private  final CodeValueData repaymentMode;
//    private final CodeValueData transactionTypePreference;
    private final CodeValueData clientType;
    private final CodeValueData clientClassification;
    private final Boolean isStaff;
    private final String voterId;
    private final String passportNumber;
    private final String drivingLicense;
    private final Long officeId;
    private final String officeName;
    private final Long transferToOfficeId;
    private final String transferToOfficeName;

    private final Long imageId;
    private final Boolean imagePresent;
    private final Long staffId;
    private final String staffName;
    private final ClientTimelineData timeline;

    private final Long savingsProductId;
    private final String savingsProductName;

    private final Long savingsAccountId;
    private final EnumOptionData legalForm;
    private final Set<ClientCollateralManagementData> clientCollateralManagements;

    // associations
    private final Collection<GroupGeneralData> groups;

    // template
    private final Collection<OfficeData> officeOptions;
    private final Collection<StaffData> staffOptions;
    private final Collection<CodeValueData> narrations;
    private final Collection<SavingsProductData> savingProductOptions;
    private final Collection<SavingsAccountData> savingAccountOptions;
    private final Collection<CodeValueData> genderOptions;
    private final Collection<CodeValueData> stateOptions;
   // private final Collection<CodeValueData> repaymentModeOptions;
//    private final Collection<CodeValueData> transactionTypePreferenceOptions;

    private final Collection<CodeValueData> clientTypeOptions;
    private final Collection<CodeValueData> clientClassificationOptions;
    private final Collection<CodeValueData> clientNonPersonConstitutionOptions;
    private final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions;
    private final List<EnumOptionData> clientLegalFormOptions;
    private final ClientFamilyMembersData familyMemberOptions;

    private final ClientNonPersonData clientNonPersonDetails;

    private final Collection<AddressData> addressList;

    private final Boolean isAddressEnabled;

    private final List<DatatableData> datatables;

    // import fields
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;
    private Long clientTypeId;
    private Long genderId;
    private Long stateId;

  //  private Long repaymentModeId;
//    private Long transactionTypePreferenceId;
    private Long clientClassificationId;
    private Long legalFormId;
    private LocalDate submittedOnDate;
    private final String pan;
    private final String aadhaar;
    private Long accountTypeId;
    private  final CodeValueData accountType;
    private final Collection<CodeValueData> accountTypeOptions;
    private final String address;

    private final Long pincode;

    private final String rationCardNumber;

    public static ClientData importClientEntityInstance(Long legalFormId, Integer rowIndex, String fullname, Long officeId,
                                                        Long clientTypeId, Long clientClassificationId, Long staffId, Boolean active, LocalDate activationDate,
                                                        LocalDate submittedOnDate, String externalId, LocalDate dateOfBirth, String mobileNo,
                                                        ClientNonPersonData clientNonPersonDetails, Collection<AddressData> addressList, String locale, String dateFormat) {
        return new ClientData(legalFormId, rowIndex,fullname, null, null,null,null,null,null,null,null, null, null,submittedOnDate,activationDate, active, externalId,
                officeId, staffId, mobileNo,null, dateOfBirth, clientTypeId, null,null, clientClassificationId, null,addressList, clientNonPersonDetails,locale,dateFormat,null,null,null,null,null,null,null,null,null);
    }

    public static ClientData createClientForInterestPosting(final Long id, final Long officeId) {
        return new ClientData(id, officeId);
    }

    private ClientData(final Long clientId, final Long officeId) {
        this.rowIndex = null;
        this.dateFormat = null;
        this.locale = null;
        this.firstname = null;
        this.lastname = null;
        this.beneficiaryName = null;
        this.city = null;
        this.beneficiaryAccountNumber = null;
        this.ifscCode = null;
        this.micrCode = null;
        this.swiftCode = null;
        this.branch = null;
        this.middlename = null;
        this.fullname = null;
        this.activationDate = null;
        this.submittedOnDate = null;
        this.active = null;
        this.externalId = null;
        this.officeId = officeId;
        this.staffId = null;
        this.legalFormId = null;
        this.mobileNo = null;
        this.age = null;
        this.dateOfBirth = null;
        this.clientTypeId = null;
        this.genderId = null;
        this.stateId = null;
     //   this.repaymentModeId = null;
//        this.transactionTypePreferenceId = null;
        this.clientClassificationId = null;
        this.isStaff = false;
        this.addressList = null;
        this.accountNo = null;
        this.status = null;
        this.subStatus = null;
        this.displayName = null;
        this.gender = null;
        this.state = null;
      //  this.repaymentMode = null;
//        this.transactionTypePreference = null;
        this.clientType = null;
        this.clientClassification = null;
        this.officeName = null;
        this.transferToOfficeId = null;
        this.transferToOfficeName = null;
        this.imageId = null;
        this.imagePresent = null;
        this.staffName = null;
        this.timeline = null;
        this.savingsProductId = null;
        this.savingsProductName = null;
        this.savingsAccountId = null;
        this.legalForm = null;
        this.groups = null;
        this.officeOptions = null;
        this.staffOptions = null;
        this.narrations = null;
        this.savingProductOptions = null;
        this.savingAccountOptions = null;
        this.genderOptions = null;
        this.stateOptions = null;
     //   this.repaymentModeOptions = null;
//        this.transactionTypePreferenceOptions = null;
        this.clientTypeOptions = null;
        this.clientClassificationOptions = null;
        this.clientNonPersonConstitutionOptions = null;
        this.clientNonPersonMainBusinessLineOptions = null;
        this.clientLegalFormOptions = null;
        this.clientNonPersonDetails = null;
        this.isAddressEnabled = null;
        this.datatables = null;
        this.familyMemberOptions = null;
        this.emailAddress = null;
        this.clientCollateralManagements = null;
        this.id = clientId;
        this.pan = null;
        this.aadhaar = null;
        this.accountTypeId = null;
        this.accountType = null;
        this.accountTypeOptions = null;
        this.voterId = null;
        this.passportNumber = null;
        this.drivingLicense = null;
        this.address = null;
        this.pincode = null;
        this.rationCardNumber = null;
    }

    public static ClientData importClientPersonInstance(Long legalFormId, Integer rowIndex, String firstname, String lastname,
            String beneficiaryName,String city,String beneficiaryAccountNumber,String ifscCode,Long micrCode,String swiftCode,String branch,
            String middlename, LocalDate submittedOn, LocalDate activationDate, Boolean active, String externalId, Long officeId,
            Long staffId, String mobileNo,Integer age, LocalDate dob, Long clientTypeId, Long genderId,Long stateId, Long clientClassificationId, Boolean isStaff,
            Collection<AddressData> addressList, String locale, String dateFormat,String pan,String aadhaar,Long accountTypeId,String voterId,String passportNumber,String drivingLicense,String address,Long pincode,String rationCardNumber) {

        return new ClientData(legalFormId, rowIndex, null, firstname, lastname,beneficiaryName,city,beneficiaryAccountNumber,ifscCode,micrCode,swiftCode,branch, middlename, submittedOn, activationDate, active, externalId,
                officeId, staffId, mobileNo, age,dob, clientTypeId, genderId,stateId, clientClassificationId, isStaff, addressList, null, locale,
                dateFormat,pan,aadhaar,accountTypeId,voterId,passportNumber,drivingLicense,address,pincode,rationCardNumber);
    }

    public static ClientData emptyInstance(Long clientId) {
        return lookup(clientId, null, null, null);
    }

    private ClientData(Long legalFormId, Integer rowIndex, String fullname, String firstname, String lastname,String beneficiaryName,String city,String beneficiaryAccountNumber,String ifscCode,Long micrCode,String swiftCode,String branch, String middlename,
            LocalDate submittedOn, LocalDate activationDate, Boolean active, String externalId, Long officeId, Long staffId,
            String mobileNo,Integer age, LocalDate dob, Long clientTypeId, Long genderId,Long stateId, Long clientClassificationId, Boolean isStaff,
            Collection<AddressData> addressList, ClientNonPersonData clientNonPersonDetails, String locale, String dateFormat,String pan,String aadhaar,Long accountTypeId,String voterId,String passportNumber,String drivingLicense,String address,Long pincode,String rationCardNumber) {
        this.rowIndex = rowIndex;
        this.dateFormat = dateFormat;
        this.locale = locale;
        this.firstname = firstname;
        this.lastname = lastname;
        this.beneficiaryName = beneficiaryName;
        this.city = city;
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.ifscCode = ifscCode;
        this.micrCode = micrCode;
        this.swiftCode = swiftCode;
        this.branch = branch;
        this.middlename = middlename;
        this.fullname = fullname;
        this.activationDate = activationDate;
        this.submittedOnDate = submittedOn;
        this.active = active;
        this.externalId = externalId;
        this.officeId = officeId;
        this.staffId = staffId;
        this.legalFormId = legalFormId;
        this.mobileNo = mobileNo;
        this.age = age;
        this.dateOfBirth = dob;
        this.clientTypeId = clientTypeId;
        this.genderId = genderId;
        this.stateId = stateId;
       // this.repaymentModeId = repaymentModeId;
//        this.transactionTypePreferenceId =transactionTypePreferenceId;
        this.clientClassificationId = clientClassificationId;
        this.isStaff = isStaff;
        this.addressList = addressList;
        this.id = null;
        this.accountNo = null;
        this.status = null;
        this.subStatus = null;
        this.displayName = null;
        this.gender = null;
        this.state = null;
     //   this.repaymentMode = null;
//        this.transactionTypePreference = null;
        this.clientType = null;
        this.clientClassification = null;
        this.officeName = null;
        this.transferToOfficeId = null;
        this.transferToOfficeName = null;
        this.imageId = null;
        this.imagePresent = null;
        this.staffName = null;
        this.timeline = null;
        this.savingsProductId = null;
        this.savingsProductName = null;
        this.savingsAccountId = null;
        this.legalForm = null;
        this.groups = null;
        this.officeOptions = null;
        this.staffOptions = null;
        this.narrations = null;
        this.savingProductOptions = null;
        this.savingAccountOptions = null;
        this.genderOptions = null;
        this.stateOptions = null;
      //  this.repaymentModeOptions =null;
//        this.transactionTypePreferenceOptions =null;
        this.clientTypeOptions = null;
        this.clientClassificationOptions = null;
        this.clientNonPersonConstitutionOptions = null;
        this.clientNonPersonMainBusinessLineOptions = null;
        this.clientLegalFormOptions = null;
        this.clientNonPersonDetails = null;
        this.isAddressEnabled = null;
        this.datatables = null;
        this.familyMemberOptions = null;
        this.emailAddress = null;
        this.clientCollateralManagements = null;
        this.pan = pan;
        this.aadhaar = aadhaar;
        this.accountTypeId = accountTypeId;
        this.accountType = null;
        this.accountTypeOptions = null;
        this.voterId = voterId;
        this.passportNumber = passportNumber;
        this.drivingLicense = drivingLicense;
        this.address = address;
        this.pincode = pincode;
        this.rationCardNumber = rationCardNumber;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }

    public Long getId() {
        return id;
    }

    public String getOfficeName() {
        return officeName;
    }

    public static ClientData template(final Long officeId, final LocalDate joinedDate, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions, final Collection<CodeValueData> narrations,
            final Collection<CodeValueData> genderOptions,final Collection<CodeValueData> stateOptions, final Collection<SavingsProductData> savingProductOptions,
            final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> clientClassificationOptions,
            final Collection<CodeValueData> clientNonPersonConstitutionOptions,
            final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions, final List<EnumOptionData> clientLegalFormOptions,
            final ClientFamilyMembersData familyMemberOptions, final Collection<AddressData> addressList, final Boolean isAddressEnabled,
            final List<DatatableData> datatables,final Collection<CodeValueData> accountTypeOptions) {
        final String accountNo = null;
        final EnumOptionData status = null;
        final CodeValueData subStatus = null;
        final String officeName = null;
        final Long transferToOfficeId = null;
        final String transferToOfficeName = null;
        final Long id = null;
        final String firstname = null;
        final String middlename = null;
        final String lastname = null;
        final String beneficiaryName = null;
        final String city =null;
        final String beneficiaryAccountNumber = null;
        final String ifscCode = null;
        final Long micrCode = null;
        final String swiftCode = null;
        final String branch = null;
        final String fullname = null;
        final String displayName = null;
        final String externalId = null;
        final String mobileNo = null;
        final Integer age = null;
        final String emailAddress = null;
        final LocalDate dateOfBirth = null;
        final CodeValueData gender = null;
        final CodeValueData state = null;
       // final CodeValueData repaymentMode = null;
//        final CodeValueData transactionTypePreference = null;
        final Long imageId = null;
        final Long staffId = null;
        final String staffName = null;
        final Collection<GroupGeneralData> groups = null;
        final ClientTimelineData timeline = null;
        final Long savingsProductId = null;
        final String savingsProductName = null;
        final Long savingsAccountId = null;
        final Collection<SavingsAccountData> savingAccountOptions = null;
        final CodeValueData clientType = null;
        final CodeValueData clientClassification = null;
        final EnumOptionData legalForm = null;
        final Boolean isStaff = false;
        final ClientNonPersonData clientNonPersonDetails = null;
        final Set<ClientCollateralManagementData> clientCollateralManagements = null;
        final String pan = null;
        final String aadhaar = null;
        final CodeValueData accountType = null;
        final String voterId = null;
        final String passportNumber = null;
        final String drivingLicense = null;
        final String address = null;
        final Long pincode = null;
        final String rationCardNumber = null;


        return new ClientData(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id, firstname,
                middlename, lastname,beneficiaryName,city,beneficiaryAccountNumber,ifscCode,micrCode,swiftCode,branch, fullname, displayName, externalId, mobileNo,age, emailAddress, dateOfBirth, gender,state, joinedDate, imageId,
                staffId, staffName, officeOptions, groups, staffOptions, narrations, genderOptions,stateOptions, timeline, savingProductOptions,
                savingsProductId, savingsProductName, savingsAccountId, savingAccountOptions, clientType, clientClassification,
                clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions, clientNonPersonMainBusinessLineOptions,
                clientNonPersonDetails, clientLegalFormOptions, familyMemberOptions, legalForm, addressList, isAddressEnabled, datatables,
                isStaff, clientCollateralManagements,pan,aadhaar,accountType,accountTypeOptions,voterId,passportNumber,drivingLicense,address,pincode,rationCardNumber);

    }

    public static ClientData templateOnTop(final ClientData clientData, final ClientData templateData) {
        final Set<ClientCollateralManagementData> clientCollateralManagements = null;
        return new ClientData(clientData.accountNo, clientData.status, clientData.subStatus, clientData.officeId, clientData.officeName,
                clientData.transferToOfficeId, clientData.transferToOfficeName, clientData.id, clientData.firstname, clientData.middlename,
                clientData.lastname,clientData.beneficiaryName,clientData.city,clientData.beneficiaryAccountNumber,clientData.ifscCode,clientData.micrCode,clientData.swiftCode,clientData.branch, clientData.fullname, clientData.displayName, clientData.externalId, clientData.mobileNo,clientData.age,
                clientData.emailAddress, clientData.dateOfBirth, clientData.gender,clientData.state,clientData.activationDate, clientData.imageId,
                clientData.staffId, clientData.staffName, templateData.officeOptions, clientData.groups, templateData.staffOptions,
                templateData.narrations, templateData.genderOptions,templateData.stateOptions, clientData.timeline, templateData.savingProductOptions,
                clientData.savingsProductId, clientData.savingsProductName, clientData.savingsAccountId, clientData.savingAccountOptions,
                clientData.clientType, clientData.clientClassification, templateData.clientTypeOptions,
                templateData.clientClassificationOptions, templateData.clientNonPersonConstitutionOptions,
                templateData.clientNonPersonMainBusinessLineOptions, clientData.clientNonPersonDetails, templateData.clientLegalFormOptions,
                templateData.familyMemberOptions, clientData.legalForm, clientData.addressList, clientData.isAddressEnabled, null,
                clientData.isStaff, clientCollateralManagements,clientData.pan,clientData.aadhaar,clientData.accountType,templateData.accountTypeOptions,clientData.voterId,clientData.passportNumber,clientData.drivingLicense,clientData.address,clientData.pincode,clientData.rationCardNumber);

    }

    public static ClientData templateWithSavingAccountOptions(final ClientData clientData,
            final Collection<SavingsAccountData> savingAccountOptions) {
        final Set<ClientCollateralManagementData> clientCollateralManagements = null;
        return new ClientData(clientData.accountNo, clientData.status, clientData.subStatus, clientData.officeId, clientData.officeName,
                clientData.transferToOfficeId, clientData.transferToOfficeName, clientData.id, clientData.firstname, clientData.middlename,
                clientData.lastname,clientData.beneficiaryName,clientData.city,clientData.beneficiaryAccountNumber,clientData.ifscCode,clientData.micrCode,clientData.swiftCode,clientData.branch, clientData.fullname, clientData.displayName, clientData.externalId, clientData.mobileNo,clientData.age,
                clientData.emailAddress, clientData.dateOfBirth, clientData.gender,clientData.state, clientData.activationDate, clientData.imageId,
                clientData.staffId, clientData.staffName, clientData.officeOptions, clientData.groups, clientData.staffOptions,
                clientData.narrations, clientData.genderOptions,clientData.stateOptions,clientData.timeline, clientData.savingProductOptions,
                clientData.savingsProductId, clientData.savingsProductName, clientData.savingsAccountId, savingAccountOptions,
                clientData.clientType, clientData.clientClassification, clientData.clientTypeOptions,
                clientData.clientClassificationOptions, clientData.clientNonPersonConstitutionOptions,
                clientData.clientNonPersonMainBusinessLineOptions, clientData.clientNonPersonDetails, clientData.clientLegalFormOptions,
                clientData.familyMemberOptions, clientData.legalForm, clientData.addressList, clientData.isAddressEnabled, null,
                clientData.isStaff, clientCollateralManagements,clientData.pan,clientData.aadhaar,clientData.accountType,clientData.accountTypeOptions,clientData.voterId,clientData.passportNumber,clientData.drivingLicense,clientData.address,clientData.pincode,clientData.rationCardNumber);

    }

    public static ClientData setParentGroups(final ClientData clientData, final Collection<GroupGeneralData> parentGroups,
            final Set<ClientCollateralManagementData> clientCollateralManagements) {
        return new ClientData(clientData.accountNo, clientData.status, clientData.subStatus, clientData.officeId, clientData.officeName,
                clientData.transferToOfficeId, clientData.transferToOfficeName, clientData.id, clientData.firstname, clientData.middlename,
                clientData.lastname,clientData.beneficiaryName,clientData.city,clientData.beneficiaryAccountNumber,clientData.ifscCode,clientData.micrCode,clientData.swiftCode,clientData.branch, clientData.fullname, clientData.displayName, clientData.externalId, clientData.mobileNo,clientData.age,
                clientData.emailAddress, clientData.dateOfBirth, clientData.gender,clientData.state,clientData.activationDate, clientData.imageId,
                clientData.staffId, clientData.staffName, clientData.officeOptions, parentGroups, clientData.staffOptions,null, null,null,
                clientData.timeline, clientData.savingProductOptions, clientData.savingsProductId, clientData.savingsProductName,
                clientData.savingsAccountId, clientData.savingAccountOptions, clientData.clientType, clientData.clientClassification,
                clientData.clientTypeOptions, clientData.clientClassificationOptions, clientData.clientNonPersonConstitutionOptions,
                clientData.clientNonPersonMainBusinessLineOptions, clientData.clientNonPersonDetails, clientData.clientLegalFormOptions,
                clientData.familyMemberOptions, clientData.legalForm, clientData.addressList, clientData.isAddressEnabled, null,
                clientData.isStaff, clientCollateralManagements,clientData.pan,clientData.aadhaar,clientData.accountType,null,clientData.voterId,clientData.passportNumber,clientData.drivingLicense,clientData.address,clientData.pincode,clientData.rationCardNumber);

    }

    public static ClientData clientIdentifier(final Long id, final String accountNo, final String firstname, final String middlename,
            final String lastname, final String fullname, final String displayName, final Long officeId, final String officeName) {
        final String beneficiaryName = null;
        final String city = null;
        final String beneficiaryAccountNumber = null;
        final String ifscCode = null;
        final Long micrCode = null;
        final String swiftCode = null;
        final String branch = null;
        final Long transferToOfficeId = null;
        final String transferToOfficeName = null;
        final String externalId = null;
        final String mobileNo = null;
        final Integer age = null;
        final String emailAddress = null;
        final LocalDate dateOfBirth = null;
        final CodeValueData gender = null;
        final CodeValueData state = null;
       // final CodeValueData repaymentMode = null;
//        final CodeValueData transactionTypePreference = null;
        final LocalDate activationDate = null;
        final Long imageId = null;
        final Long staffId = null;
        final String staffName = null;
        final Collection<OfficeData> allowedOffices = null;
        final Collection<GroupGeneralData> groups = null;
        final Collection<StaffData> staffOptions = null;
        final Collection<CodeValueData> closureReasons = null;
        final Collection<CodeValueData> genderOptions = null;
        final Collection<CodeValueData> stateOptions = null;
     //   final Collection<CodeValueData> repaymentModeOptions = null;
//        final Collection<CodeValueData> transactionTypePreferenceOptions = null;
        final ClientTimelineData timeline = null;
        final Collection<SavingsProductData> savingProductOptions = null;
        final Long savingsProductId = null;
        final String savingsProductName = null;
        final Long savingsAccountId = null;
        final Collection<SavingsAccountData> savingAccountOptions = null;
        final CodeValueData clientType = null;
        final CodeValueData clientClassification = null;
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        final Collection<CodeValueData> clientNonPersonConstitutionOptions = null;
        final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions = null;
        final List<EnumOptionData> clientLegalFormOptions = null;
        final ClientFamilyMembersData familyMemberOptions = null;
        final EnumOptionData status = null;
        final CodeValueData subStatus = null;
        final EnumOptionData legalForm = null;
        final Boolean isStaff = false;
        final ClientNonPersonData clientNonPerson = null;
        final Set<ClientCollateralManagementData> clientCollateralManagements = null;
        final String pan = null;
        final String aadhaar = null;
        final CodeValueData accountType = null;
        final Collection<CodeValueData> accountTypeOptions = null;
        final String voterId = null;
        final String passportNumber = null;
        final String drivingLicense = null;
        final String address = null;
        final Long pincode = null;
        final String rationCardNumber = null;
        return new ClientData(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id, firstname,
                middlename, lastname,beneficiaryName,city,beneficiaryAccountNumber,ifscCode,micrCode,swiftCode,branch, fullname, displayName, externalId, mobileNo,age, emailAddress, dateOfBirth, gender,state,activationDate,
                imageId, staffId, staffName, allowedOffices, groups, staffOptions, closureReasons, genderOptions,stateOptions,timeline,
                savingProductOptions, savingsProductId, savingsProductName, savingsAccountId, savingAccountOptions, clientType,
                clientClassification, clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions,
                clientNonPersonMainBusinessLineOptions, clientNonPerson, clientLegalFormOptions, familyMemberOptions, legalForm, null, null,
                null, isStaff, clientCollateralManagements,pan,aadhaar,accountType,accountTypeOptions,voterId,passportNumber,drivingLicense,address,pincode,rationCardNumber);
    }

    public static ClientData lookup(final Long id, final String displayName, final Long officeId, final String officeName) {
        final String accountNo = null;
        final EnumOptionData status = null;
        final CodeValueData subStatus = null;
        final Long transferToOfficeId = null;
        final String transferToOfficeName = null;
        final String firstname = null;
        final String middlename = null;
        final String lastname = null;
        final String beneficiaryName = null;
        final String city = null;
        final String beneficiaryAccountNumber = null;
        final String ifscCode = null;
        final Long micrCode = null;
        final String swiftCode = null;
        final String branch = null;
        final String fullname = null;
        final String externalId = null;
        final String mobileNo = null;
        final Integer age = null;
        final String emailAddress = null;
        final LocalDate dateOfBirth = null;
        final CodeValueData gender = null;
        final CodeValueData state = null;
       // final CodeValueData repaymentMode = null;
//        final CodeValueData transactionTypePreference = null;
        final LocalDate activationDate = null;
        final Long imageId = null;
        final Long staffId = null;
        final String staffName = null;
        final Collection<OfficeData> allowedOffices = null;
        final Collection<GroupGeneralData> groups = null;
        final Collection<StaffData> staffOptions = null;
        final Collection<CodeValueData> closureReasons = null;
        final Collection<CodeValueData> genderOptions = null;
        final Collection<CodeValueData> stateOptions = null;
      //  final Collection<CodeValueData> repaymentModeOptions = null;
//        final Collection<CodeValueData> transactionTypePreferenceOptions = null;
        final ClientTimelineData timeline = null;
        final Collection<SavingsProductData> savingProductOptions = null;
        final Long savingsProductId = null;
        final String savingsProductName = null;
        final Long savingsAccountId = null;
        final Collection<SavingsAccountData> savingAccountOptions = null;
        final CodeValueData clientType = null;
        final CodeValueData clientClassification = null;
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        final Collection<CodeValueData> clientNonPersonConstitutionOptions = null;
        final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions = null;
        final List<EnumOptionData> clientLegalFormOptions = null;
        final ClientFamilyMembersData familyMemberOptions = null;
        final EnumOptionData legalForm = null;
        final Boolean isStaff = false;
        final ClientNonPersonData clientNonPerson = null;
        final Set<ClientCollateralManagementData> clientCollateralManagements = null;
        final String pan = null;
        final String aadhaar = null;
        final CodeValueData accountType = null;
        final Collection<CodeValueData> accountTypeOptions = null;
        final String voterId = null;
        final String passportNumber = null;
        final String drivingLicense = null;
        final String address = null;
        final Long pincode = null;
        final String rationCardNumber = null;
        return new ClientData(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id, firstname,
                middlename, lastname,beneficiaryName,city,beneficiaryAccountNumber,ifscCode,micrCode,swiftCode,branch, fullname, displayName, externalId, mobileNo,age, emailAddress, dateOfBirth, gender,state, activationDate,
                imageId, staffId, staffName, allowedOffices, groups, staffOptions, closureReasons, genderOptions,stateOptions,timeline,
                savingProductOptions, savingsProductId, savingsProductName, savingsAccountId, savingAccountOptions, clientType,
                clientClassification, clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions,
                clientNonPersonMainBusinessLineOptions, clientNonPerson, clientLegalFormOptions, familyMemberOptions, legalForm, null, null,
                null, isStaff, clientCollateralManagements,pan,aadhaar,accountType,accountTypeOptions,voterId,passportNumber,drivingLicense,address,pincode,rationCardNumber);

    }

    public static ClientData instance(final Long id, final String displayName) {
        final Long officeId = null;
        final String officeName = null;
        return lookup(id, displayName, officeId, officeName);
    }

    public static ClientData instance(final String accountNo, final EnumOptionData status, final CodeValueData subStatus,
                                      final Long officeId, final String officeName, final Long transferToOfficeId, final String transferToOfficeName, final Long id,
                                      final String firstname, final String middlename, final String lastname, final String beneficiaryName, final String city, final String beneficiaryAccountNumber, final String ifscCode, final Long micrCode, final String swiftCode, final String branch, final String fullname, final String displayName,
                                      final String externalId, final String mobileNo, final Integer age, final String emailAddress, final LocalDate dateOfBirth,
                                      final CodeValueData gender, final CodeValueData state,final LocalDate activationDate, final Long imageId, final Long staffId, final String staffName,
                                      final ClientTimelineData timeline, final Long savingsProductId, final String savingsProductName, final Long savingsAccountId,
                                      final CodeValueData clientType, final CodeValueData clientClassification, final EnumOptionData legalForm,
                                      final ClientNonPersonData clientNonPerson, final Boolean isStaff, final String pan, final String aadhaar, final CodeValueData accountType,final String voterId,
                                      final String passportNumber,final String drivingLicense, final String address,final Long pincode,final String rationCardNumber) {

        final Collection<OfficeData> allowedOffices = null;
        final Collection<GroupGeneralData> groups = null;
        final Collection<StaffData> staffOptions = null;
        final Collection<CodeValueData> closureReasons = null;
        final Collection<CodeValueData> genderOptions = null;
        final Collection<CodeValueData> stateOptions = null;
    //    final Collection <CodeValueData> repaymentModeOptions =null;
//        final Collection <CodeValueData> transactionTypePreferenceOptions =null;
        final Collection<SavingsProductData> savingProductOptions = null;
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        final Collection<CodeValueData> clientNonPersonConstitutionOptions = null;
        final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions = null;
        final List<EnumOptionData> clientLegalFormOptions = null;
        final ClientFamilyMembersData familyMemberOptions = null;
        final Collection<ClientCollateralManagement> clientCollateralManagements = null;
        final Collection<CodeValueData> accountTypeOptions = null;
        return new ClientData(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id, firstname, middlename,lastname,beneficiaryName,city,beneficiaryAccountNumber,ifscCode,micrCode,swiftCode,branch, fullname, displayName, externalId, mobileNo,age, emailAddress, dateOfBirth, gender,state,activationDate,
                imageId, staffId, staffName, allowedOffices, groups, staffOptions, closureReasons, genderOptions,stateOptions,timeline,
                savingProductOptions, savingsProductId, savingsProductName, savingsAccountId, null, clientType, clientClassification,
                clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions, clientNonPersonMainBusinessLineOptions,
                clientNonPerson, clientLegalFormOptions, familyMemberOptions, legalForm, null, null, null, isStaff, null,pan,aadhaar,accountType,accountTypeOptions,voterId,passportNumber,drivingLicense,address,pincode,rationCardNumber);

    }

    private ClientData(final String accountNo, final EnumOptionData status, final CodeValueData subStatus, final Long officeId,
            final String officeName, final Long transferToOfficeId, final String transferToOfficeName, final Long id,
            final String firstname, final String middlename, final String lastname,final String beneficiaryName,final String city,final String beneficiaryAccountNumber,final String ifscCode,final Long micrCode,final String swiftCode,final String branch, final String fullname, final String displayName,
            final String externalId, final String mobileNo,final Integer age, final String emailAddress, final LocalDate dateOfBirth,
            final CodeValueData gender,final CodeValueData state,final LocalDate activationDate, final Long imageId, final Long staffId, final String staffName,
            final Collection<OfficeData> allowedOffices, final Collection<GroupGeneralData> groups,
            final Collection<StaffData> staffOptions, final Collection<CodeValueData> narrations,
            final Collection<CodeValueData> genderOptions,final Collection<CodeValueData> stateOptions,final ClientTimelineData timeline,
            final Collection<SavingsProductData> savingProductOptions, final Long savingsProductId, final String savingsProductName,
            final Long savingsAccountId, final Collection<SavingsAccountData> savingAccountOptions, final CodeValueData clientType,
            final CodeValueData clientClassification, final Collection<CodeValueData> clientTypeOptions,
            final Collection<CodeValueData> clientClassificationOptions, final Collection<CodeValueData> clientNonPersonConstitutionOptions,
            final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions, final ClientNonPersonData clientNonPerson,
            final List<EnumOptionData> clientLegalFormOptions, final ClientFamilyMembersData familyMemberOptions,
            final EnumOptionData legalForm, final Collection<AddressData> addressList, final Boolean isAddressEnabled,
            final List<DatatableData> datatables, final Boolean isStaff,
            final Set<ClientCollateralManagementData> clientCollateralManagements,final String pan,final String aadhaar,final CodeValueData accountType ,final Collection<CodeValueData> accountTypeOptions,final String voterId,final String passportNumber,final String drivingLicense,
                       final String address,final Long pincode,final String rationCardNumber) {
        this.accountNo = accountNo;
        this.status = status;
        if (status != null) {
            this.active = status.getId().equals(300L);
        } else {
            this.active = null;
        }
        this.subStatus = subStatus;
        this.officeId = officeId;
        this.officeName = officeName;
        this.transferToOfficeId = transferToOfficeId;
        this.transferToOfficeName = transferToOfficeName;
        this.id = id;
        this.firstname = StringUtils.defaultIfEmpty(firstname, null);
        this.middlename = StringUtils.defaultIfEmpty(middlename, null);
        this.lastname = StringUtils.defaultIfEmpty(lastname, null);
        this.beneficiaryName = StringUtils.defaultIfEmpty(beneficiaryName, null);
        this.city = StringUtils.defaultIfEmpty(city,null);
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.ifscCode = StringUtils.defaultIfEmpty(ifscCode, null);
        this.micrCode = micrCode;
        this.swiftCode = StringUtils.defaultIfEmpty(swiftCode, null);
        this.branch = StringUtils.defaultIfEmpty(branch, null);
        this.fullname = StringUtils.defaultIfEmpty(fullname, null);
        this.displayName = StringUtils.defaultIfEmpty(displayName, null);
        this.externalId = StringUtils.defaultIfEmpty(externalId, null);
        this.mobileNo = StringUtils.defaultIfEmpty(mobileNo, null);
        this.age = age;
        this.emailAddress = StringUtils.defaultIfEmpty(emailAddress, null);
        this.activationDate = activationDate;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.state = state;
       // this.repaymentMode = repaymentMode;
//        this.transactionTypePreference = transactionTypePreference;
        this.clientClassification = clientClassification;
        this.clientType = clientType;
        this.imageId = imageId;
        this.voterId = StringUtils.defaultIfEmpty(voterId, null);
        this.passportNumber = StringUtils.defaultIfEmpty(passportNumber, null);
        this.drivingLicense = StringUtils.defaultIfEmpty(drivingLicense, null);
        if (imageId != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }
        this.staffId = staffId;
        this.staffName = staffName;

        // associations
        this.groups = groups;

        // template
        this.officeOptions = allowedOffices;
        this.staffOptions = staffOptions;
        this.narrations = narrations;

        this.genderOptions = genderOptions;
        this.stateOptions = stateOptions;
      //  this.repaymentModeOptions = repaymentModeOptions;
//        this.transactionTypePreferenceOptions = transactionTypePreferenceOptions;
        this.clientClassificationOptions = clientClassificationOptions;
        this.clientTypeOptions = clientTypeOptions;

        this.clientNonPersonConstitutionOptions = clientNonPersonConstitutionOptions;
        this.clientNonPersonMainBusinessLineOptions = clientNonPersonMainBusinessLineOptions;
        this.clientLegalFormOptions = clientLegalFormOptions;
        this.familyMemberOptions = familyMemberOptions;

        this.timeline = timeline;
        this.savingProductOptions = savingProductOptions;
        this.savingsProductId = savingsProductId;
        this.savingsProductName = savingsProductName;
        this.savingsAccountId = savingsAccountId;
        this.savingAccountOptions = savingAccountOptions;
        this.legalForm = legalForm;
        this.isStaff = isStaff;
        this.clientNonPersonDetails = clientNonPerson;

        this.addressList = addressList;
        this.isAddressEnabled = isAddressEnabled;
        this.datatables = datatables;
        this.clientCollateralManagements = clientCollateralManagements;
        this.pan = pan;
        this.aadhaar  = aadhaar;
        this.accountType = accountType;
        this.accountTypeOptions = accountTypeOptions;
        this.address = address;
        this.pincode = pincode;
        this.rationCardNumber = StringUtils.defaultIfEmpty(rationCardNumber, null);

    }

    public Long id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public String accountNo() {
        return this.accountNo;
    }

    public Long officeId() {
        return this.officeId;
    }

    public String officeName() {
        return this.officeName;
    }

    public Long getImageId() {
        return this.imageId;
    }

    public Boolean getImagePresent() {
        return this.imagePresent;
    }

    public ClientTimelineData getTimeline() {
        return this.timeline;
    }

    @Override
    public int compareTo(final ClientData obj) {
        if (obj == null) {
            return -1;
        }
        return new CompareToBuilder() //
                .append(this.id, obj.id) //
                .append(this.displayName, obj.displayName) //
                .append(this.mobileNo, obj.mobileNo) //
                .append(this.emailAddress, obj.emailAddress) //
                .toComparison();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ClientData rhs = (ClientData) obj;
        return new EqualsBuilder() //
                .append(this.id, rhs.id) //
                .append(this.displayName, rhs.displayName) //
                .append(this.mobileNo, rhs.mobileNo) //
                .append(this.emailAddress, rhs.emailAddress) //
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37) //
                .append(this.id) //
                .append(this.displayName) //
                .toHashCode();
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }


    public LocalDate getActivationDate() {
        return this.activationDate;
    }

    public Boolean getIsAddressEnabled() {
        return this.isAddressEnabled;
    }


//    public CodeValueData getTransactionTypePreference() {
//        return transactionTypePreference;
//    }

    public static ClientData lookup(final Long id, final String displayName, final Long officeId, final String officeName, final String mobileNo,final EnumOptionData status,final String accountNo) {
//        final String accountNo = null;
        final CodeValueData subStatus = null;
        final Long transferToOfficeId = null;
        final String transferToOfficeName = null;
        final String firstname = null;
        final String middlename = null;
        final String lastname = null;
        final String beneficiaryName = null;
        final String city = null;
        final String beneficiaryAccountNumber = null;
        final String ifscCode = null;
        final Long micrCode = null;
        final String swiftCode = null;
        final String branch = null;
        final String fullname = null;
        final String externalId = null;
//        final String mobileNo = null;
        final Integer age = null;
        final String emailAddress = null;
        final LocalDate dateOfBirth = null;
        final CodeValueData gender = null;
        final CodeValueData state = null;
        // final CodeValueData repaymentMode = null;
//        final CodeValueData transactionTypePreference = null;
        final LocalDate activationDate = null;
        final Long imageId = null;
        final Long staffId = null;
        final String staffName = null;
        final Collection<OfficeData> allowedOffices = null;
        final Collection<GroupGeneralData> groups = null;
        final Collection<StaffData> staffOptions = null;
        final Collection<CodeValueData> closureReasons = null;
        final Collection<CodeValueData> genderOptions = null;
        final Collection<CodeValueData> stateOptions = null;
        //  final Collection<CodeValueData> repaymentModeOptions = null;
//        final Collection<CodeValueData> transactionTypePreferenceOptions = null;
        final ClientTimelineData timeline = null;
        final Collection<SavingsProductData> savingProductOptions = null;
        final Long savingsProductId = null;
        final String savingsProductName = null;
        final Long savingsAccountId = null;
        final Collection<SavingsAccountData> savingAccountOptions = null;
        final CodeValueData clientType = null;
        final CodeValueData clientClassification = null;
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        final Collection<CodeValueData> clientNonPersonConstitutionOptions = null;
        final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions = null;
        final List<EnumOptionData> clientLegalFormOptions = null;
        final ClientFamilyMembersData familyMemberOptions = null;
        final EnumOptionData legalForm = null;
        final Boolean isStaff = false;
        final ClientNonPersonData clientNonPerson = null;
        final Set<ClientCollateralManagementData> clientCollateralManagements = null;
        final String pan = null;
        final String aadhaar = null;
        final CodeValueData accountType = null;
        final Collection<CodeValueData> accountTypeOptions = null;
        final String voterId = null;
        final String passportNumber = null;
        final String drivingLicense = null;
        final String address = null;
        final Long pincode = null;
        final String rationCardNumber = null;
        return new ClientData(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id, firstname,
                middlename, lastname,beneficiaryName,city,beneficiaryAccountNumber,ifscCode,micrCode,swiftCode,branch, fullname, displayName, externalId, mobileNo,age, emailAddress, dateOfBirth, gender,state, activationDate,
                imageId, staffId, staffName, allowedOffices, groups, staffOptions, closureReasons, genderOptions,stateOptions,timeline,
                savingProductOptions, savingsProductId, savingsProductName, savingsAccountId, savingAccountOptions, clientType,
                clientClassification, clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions,
                clientNonPersonMainBusinessLineOptions, clientNonPerson, clientLegalFormOptions, familyMemberOptions, legalForm, null, null,
                null, isStaff, clientCollateralManagements,pan,aadhaar,accountType,accountTypeOptions,voterId,passportNumber,drivingLicense,address,pincode,rationCardNumber);

    }
}
