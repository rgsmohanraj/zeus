package org.vcpl.lms.portfolio.loanaccount.bulkupload.template;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxColumn;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxDynamicColumns;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxSheet;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnValidation;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.RequiredPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@XlsxSheet("ClientLoan")
public class XLXSClientLoanTemplate {
    @XlsxColumn(header= "S.NO", columnIndex = 0, type = CellDataType.NUMERIC, required = RequiredPolicy.MANDATORY)
    private Integer serialNo;
    @XlsxColumn(header= "External Id", columnIndex = 1, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String externalId;
    @XlsxColumn(header= "Entity", columnIndex = 2, type = CellDataType.STRING, dropdownType = DropdownType.OFFICE,
            required = RequiredPolicy.MANDATORY)
    private String entity;
    @XlsxColumn(header= "Applicant Type", columnIndex = 3, type = CellDataType.STRING,
            dropdownType = DropdownType.APPLICANT_TYPE,required = RequiredPolicy.MANDATORY)
    private String applicantType;
    @XlsxColumn(header= "Asset Class", columnIndex = 4, type = CellDataType.STRING, dropdownType = DropdownType.ASSET_CLASS)
    private String assetClass;
    @XlsxColumn(header= "First Name", columnIndex = 5, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String firstName;
    @XlsxColumn(header= "Middle Name",columnIndex = 6, type = CellDataType.STRING)
    private String middleName;
    @XlsxColumn(header= "Last Name",columnIndex = 7, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String lastName;
    @XlsxColumn(header= "Age", columnIndex = 8, type = CellDataType.NUMERIC, required = RequiredPolicy.MANDATORY)
    private Integer age;
    @XlsxColumn(header= "Gender", columnIndex = 9, type = CellDataType.STRING,
            dropdownType = DropdownType.GENDER, required = RequiredPolicy.MANDATORY)
    private String gender;
    @XlsxColumn(header= "DOB" ,columnIndex = 10, type = CellDataType.LOCAL_DATE, required = RequiredPolicy.MANDATORY)
    private LocalDate dob;
    @XlsxColumn(header= "PAN", columnIndex = 11, type = CellDataType.STRING, required = RequiredPolicy.CONDITIONAL_MANDATE)
    private String pan;
    @XlsxColumn(header= "Aadhaar", columnIndex = 12, type = CellDataType.STRING, required = RequiredPolicy.CONDITIONAL_MANDATE)
    private String aadhaar;
    @XlsxColumn(header= "Voter ID", columnIndex = 13, type = CellDataType.STRING)
    private String voterId;
    @XlsxColumn(header= "Passport Number", columnIndex = 14, type = CellDataType.STRING)
    private String passportNumber;
    @XlsxColumn(header= "Driving License", columnIndex = 15, type = CellDataType.STRING)
    private String drivingLicense;
    @XlsxColumn(header= "Address" ,columnIndex = 16, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String address;
    @XlsxColumn(header= "Pincode" ,columnIndex = 17, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private Long pincode;
    @XlsxColumn(header= "Mobile Number", columnIndex = 18, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String mobileNumber;
    @XlsxColumn(header= "Email Address", columnIndex = 19, type = CellDataType.STRING)
    private String emailAddress;
    @XlsxColumn(header= "City", columnIndex = 20, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String city;
    @XlsxColumn(header= "State", columnIndex = 21, type = CellDataType.STRING, dropdownType = DropdownType.STATE,
            required = RequiredPolicy.MANDATORY)
    private String state;
    @XlsxColumn(header= "Submitted On", columnIndex = 22, type = CellDataType.LOCAL_DATE, required = RequiredPolicy.MANDATORY)
    private LocalDate submittedOn;
    @XlsxColumn(header= "Activation Date", columnIndex = 23, type = CellDataType.LOCAL_DATE, required = RequiredPolicy.MANDATORY)
    private LocalDate activationDate;
    @XlsxColumn(header= "Beneficiary Name", columnIndex = 24, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String beneficiaryName;
    @XlsxColumn(header= "Beneficiary Account No", columnIndex = 25, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String beneficiaryAccountNo;
    @XlsxColumn(header= "Account Type",columnIndex = 26, type = CellDataType.STRING,
            dropdownType = DropdownType.ACCOUNT_TYPE, required = RequiredPolicy.MANDATORY)
    private String accountType;
    @XlsxColumn(header= "IFSC", columnIndex = 27, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String ifsc;
    @XlsxColumn(header= "MICR Code", columnIndex = 28, type = CellDataType.STRING)
    private String micrCode;
    @XlsxColumn(header= "Swift Code", columnIndex = 29, type = CellDataType.STRING)
    private String swiftCode;
    @XlsxColumn(header= "Branch", columnIndex = 30,type = CellDataType.STRING)
    private String branch;
//    @XlsxColumn(header= "Repayment Mode", columnIndex = 29, type = CellDataType.STRING,
//            dropdownType = DropdownType.REPAYMENT_MODE)
//    private String repaymentMode;
    @XlsxColumn(header= "Loan Submitted On",columnIndex = 31, type = CellDataType.LOCAL_DATE, required = RequiredPolicy.MANDATORY)
    private LocalDate loanSubmittedOn;
    @XlsxColumn(header= "Disbursement Date",columnIndex = 32, type = CellDataType.LOCAL_DATE, required = RequiredPolicy.MANDATORY)
    private LocalDate disbursementDate;
    @XlsxColumn(header= "Principal",columnIndex = 33, type = CellDataType.DECIMAL,
            validation = ColumnValidation.PRINCIPLE_AMOUNT, required = RequiredPolicy.MANDATORY)
    private BigDecimal principle;
    @XlsxColumn(header= "Loan Term",columnIndex = 34, type = CellDataType.NUMERIC,
            validation = ColumnValidation.LOAN_TERM, required = RequiredPolicy.MANDATORY)
    private Integer loanTerm;
    @XlsxColumn(header= "First Repayment On", columnIndex = 35, type = CellDataType.LOCAL_DATE, required = RequiredPolicy.MANDATORY)
    private LocalDate firstRepaymentOn;
    @XlsxColumn(header= "Due Day",columnIndex = 36, type = CellDataType.NUMERIC,
            dropdownType = DropdownType.DUE_DAYS, required = RequiredPolicy.MANDATORY)
    private Integer dueDay;

    @XlsxColumn(header= "Interest Rate",columnIndex = 37, type = CellDataType.DECIMAL,
            validation = ColumnValidation.INTEREST_RATE, required = RequiredPolicy.MANDATORY)
    private BigDecimal interestRate;

//    @XlsxColumn(header= "Ration Card", columnIndex = 38, type = CellDataType.STRING)
//    private String rationCardNumber;

    @XlsxDynamicColumns(startIndex = 38, key = "charges")
    private List<?> charges;
}
