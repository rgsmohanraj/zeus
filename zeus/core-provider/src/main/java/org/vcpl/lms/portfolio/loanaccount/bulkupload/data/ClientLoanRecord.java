package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetails;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientLoanRecord {
    private Long serialNo;
    private String externalId;
    private String entity;
    private String applicantType;
    private String firstName;
    private String middleName;
    private String lastName;
    private String assetClass;
    private Integer age;
    private String gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private String pan;
    private String aadhaar;
    private String address;
    private String mobileNumber;
    private String emailAddress;
    private String city;
    private String state;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate submittedOn;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate activationDate;
    private String beneficiaryName;
    private String beneficiaryAccountNo;
    private String ifsc;
    private String micrCode;
    private String swiftCode;
    private String branch;
    //private String repaymentMode;
    private String transactionTypePreferred;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate loanSubmittedOn;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate disbursementDate;
    private BigDecimal principle;
    private Integer loanTerm;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstRepaymentOn;
    private Integer dueDay;
    private BigDecimal interestRate;
    private String accountType;
    private Set<LoanChargeRecord> loanChargeRecords;
    private List<String> errorRecords = new ArrayList<>();
    private LoanProduct loanProduct;
    private ImportDocumentDetails importDocumentDetails;
    private Long loanId;
    private Long clientId;
    private Map<String,BigDecimal> charges = new HashMap<>();
    private String voterId;
    private String passportNumber;
    private String drivingLicense;
    private Long pincode;
    private String loanAccountNo;
    private boolean externalIdIsEmpty;
    private BulkApiResponse bulkApiResponse;
  //  private String rationCardNumber;

    private List<String> infoRecords;

    public boolean isErrorRecord() {
        return !errorRecords.isEmpty();
    }
}
