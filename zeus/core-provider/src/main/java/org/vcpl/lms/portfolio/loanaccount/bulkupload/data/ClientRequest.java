package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.Data;

@Data
public class ClientRequest {
    private int officeId;
    private int legalFormId;
    private boolean active;
    private String externalId;
    private Integer genderId;
    private int age;
    private int stateId;
    private String city;
    private String pan;
    private String aadhaar;
    private String address;
    private String mobileNo;
    private String emailAddress;
    private String dateOfBirth;
    private String activationDate;
    private String submittedOnDate;
    private String firstname;
    private String middlename;
    private String lastname;
    //private int repaymentModeId;
    private String beneficiaryName;
    private String beneficiaryAccountNumber;
    private String ifscCode;
    private String micrCode;
    private String swiftCode;
    private String branch;
    private int accountTypeId;
    private String dateFormat;
    private String locale;
    private String voterId;
    private String passportNumber;
    private String drivingLicense;
    private Long pincode;
  //  private String rationCardNumber;
}
