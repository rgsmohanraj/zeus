package org.vcpl.lms.portfolio.loanaccount.api;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
final public class BulkOperationResourceSwagger {
    private BulkOperationResourceSwagger() {}
    @Schema(description = "Bulk Operation Resource")
    public static final class PostBulkLoanOperationRequest {
        @Schema(
                description = "Serial number is used of unique record identification, In the record if external id not mentioned then based on the serial number only,error message will be displayed",
                name = "serialNo",
                type = "Long",
                example = "1001")
        public Long serialNo;
        @Schema(
                description = "External Id is unique identification of each loan",
                name = "externalId",
                type = "String",
                example = "NOC28NOV005")
        public String externalId;
        @Schema(
                description = "On which company,client and loan is created",
                name = "entity",
                type = "String",
                example = "Vivriti Capital Limited")
        public String entity;
        @Schema(
                description = "Here we can select, the application type of the client, the client is person or from organization",
                name = "applicantType",
                type = "String",
                example = "PERSON")
        public String applicantType;
        @Schema(
                description = "First Name of the client",
                name = "firstName",
                type = "String",
                example = "Yuva")
        public String firstName;
        @Schema(
                description = "Middle Name of the client",
                name = "middleName",
                type = "String",
                example = "Prasanth")
        public String middleName;
        @Schema(
                description = "Last Name of the client",
                name = "lastName",
                type = "String",
                example = "Kanagaraj")
        public String lastName;
        @Schema(
                description = "client Age",
                name = "age",
                type = "Integer",
                example = "25")
        public Integer age;
        @Schema(
                description = "client Gender",
                name = "gender",
                type = "String",
                example = "Male")
        public String gender;
        @Schema(description = "Date of birth", format = "date", example = "[1999, 05, 03]")
        public LocalDate dob;
        @Schema(description = "client Pan number",
                name = "pan",type = "String",example = "ASIPY1903A")
        public String pan;
        @Schema(description = "client aadhaar number",
                name = "aadhaar",type = "String",example = "123456789102")
        public String aadhaar;
        @Schema(description = "client address",type = "String",
                name = "address",example = "Prestige Zackria Metropolitan, No.200/1-8, 2nd Floor, Block 1, Anna Salai, Chennai, Tamil Nadu 600002")
        public String address;
        @Schema(description = "client phone number",type = "String",
                name = "mobileNumber",example = "7708698809")
        public String mobileNumber;
        @Schema(description = "client email address",type = "String",
                name = "emailAddress",example = "abc@vivriti.com")
        public String emailAddress;
        @Schema(description = "client city detail",type = "String",
                name = "city",example = "Chennai")
        public String city;
        @Schema(description = "client state detail",type = "String",
                name = "state",example = "Tamil Nadu")
        public String state;
        @Schema(description = "Client detail submission date", format = "date", example = "[1999, 05, 03]")
        public LocalDate submittedOn;
        @Schema(description = "Client detail activate date",format = "date", example = "[1999, 05, 03]")
        public LocalDate activationDate;
        @Schema(name = "beneficiaryName",type = "String",description = "entity that you legally designate to receive the benefits from your financial products",example = "Prasanth K")
        public String beneficiaryName;
        @Schema(description = "Beneficiary Account number",type = "String",
                name = "beneficiaryAccountNo",example = "DABDSBSDB00012")
        public String beneficiaryAccountNo;
        @Schema(description = "IFSC code for Beneficiary Account",type = "String",
                name = "ifsc",example = "SBIN0018272")
        public String ifsc;
        @Schema(description = "Micro code for Beneficiary Account",type = "String",
                name = "micrCode",example = "700002021")
        public String micrCode;
        @Schema(description = "Swift code for Beneficiary Account",type = "String",
                name = "swiftCode",example = "AAAABBCC123")
        public String swiftCode;
        @Schema(description = "Branch name for Beneficiary Account",type = "String",
                name = "branch",example = "kottivakkam")
        public String branch;
        @Schema(description = "Amount Transaction type",type = "String",
                name = "transactionTypePreferred",example = "IMPS")
        public String transactionTypePreferred;
        @Schema(description = "Loan Submitted On", format = "date", example = "[1999, 05, 03]")
        public LocalDate loanSubmittedOn;
        @Schema(description = "Loan disbursement Date", format = "date", example = "[1999, 05, 03]")
        public LocalDate disbursementDate;
        @Schema(description = "Loan principle amount",type = "BigDecimal",
                name = "principle",example = "10000")
        public BigDecimal principle;
        @Schema(description = "Loan tenure",type = "Integer",
                name = "loanTerm",example = "12")
        public Integer loanTerm;
        @Schema(description = "First Collection Done on", format = "date", example = "[1999, 05, 03]")
        public LocalDate firstRepaymentOn;
        @Schema(description = "Loan repayment due date",type = "Integer",
                name = "dueDay",example = "5")
        public Integer dueDay;
        @Schema(description = "Loan Interest Rate",type = "BigDecimal",
                name = "interestRate",example = "15")
        public BigDecimal interestRate;
        @Schema(description = "Loan Account Type",type = "String",
                name = "accountType",example = "SBA - Savings Account")
        public String accountType;
        @Schema(description = "Client Voter Id Number",type = "String",
                name = "voterId",example = "ABCDE1234F")
        public String voterId;
        @Schema(description = "Client Passport Number",type = "String",
                name = "passportNumber",example = "ABCDE1234F")
        public String passportNumber;
        @Schema(description = "Client Driving license",type = "String",
                name = "drivingLicense",example = "ABC123456789")
        public String drivingLicense;
        @Schema(description = "Client Pincode",type = "Long",
                name = "pincode",example = "641665")
        public Long pincode;
    }
    @Schema(description = "Bulk Operation Resource Response")
    public static final class PostBulkOperationResponse {
        @Schema(description = "Loan Account Number",type = "String",
                name = "loanAccountNo",example = "DABDSBSDB00012")
        public String loanAccountNo;
        @Schema(description = "Unique External ID",type = "String",
                name = "externalId",example = "AOIDATASET")
        public String externalId;
        @Schema(description = "Unique External ID",type = "Boolean",
               name = "status",example = "true")
        public Boolean status;
        @Schema(description = "Information or Error message will be displayed",type = "String",
                name = "reason",example = "Information and Error message will be displayed")
        public String reason;
    }
    @Schema(description = "Bulk Operation Resource Request")
    public static final class PostBulkRepaymentOperationRequest {
        @Schema(description = "Serail Number",type = "Long",
                name = "serialNo",example = "0001")
        public Long serialNo;
        @Schema(description = "Loan Account Number",type = "String",
                name = "loanAccountNo",example = "AAAA2122000000002")
        public String loanAccountNo;
        @Schema(description = "External Identification unique",type = "Long",
                name = "externalId",example = "ABCDEFGH001")
        public String externalId;
        public Integer installment;
        @Schema(description = "Transaction amount",type = "BigDecimal",
                name = "transactionAmount",example = "67.35")
        public BigDecimal transactionAmount;
        @Schema(description = "Transaction Date",type = "LocalDate",
                name = "transactionDate",example = "[2021, 05, 03]")
        public LocalDate transactionDate;
        @Schema(description = "Reference account number",type = "String",
                name = "receiptReferenceNumber",example = "AAAA2122000000002")
        public String receiptReferenceNumber;
        @Schema(description = "Partner Trasfer UTR  ",type = "String",
                name = "partnerTransferUTR",example = "CITIN12345678999")
        public String partnerTransferUTR;
        @Schema(description = "Partner Transaction Date",type = "LocalDate",
                name = "transactionDate",example = "[2021, 05, 03]")
        public LocalDate partnerTransferDate;
        @Schema(description = "Amount Transaction type",type = "String",
                name = "repaymentMode",example = "IMPS")
        public String repaymentMode;
        @Schema(description = "Client Id",type = "Long",
                name = "clientId",example = "0")
        public Long clientId;
        @Schema(description = "Loan",type = "Long",
                name = "loanId",example = "0")
        public Long loanId;
    }
}
