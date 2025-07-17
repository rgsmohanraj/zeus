package org.vcpl.lms.portfolio.loanaccount.bulkupload.template;

import lombok.Data;
import org.vcpl.client.domain.BeneficiaryDetails;
import org.vcpl.client.enumeration.AccType;
import org.vcpl.client.enumeration.NotificationFlag;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxColumn;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxDynamicColumns;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxSheet;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.RequiredPolicy;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@XlsxSheet("Repayment")
public class XLSXRepaymentTemplate {
    @XlsxColumn(header = "S.NO", columnIndex = 0, type = CellDataType.NUMERIC,required = RequiredPolicy.MANDATORY)
    private int serialNo;
    @XlsxColumn(header = "Loan Account No", columnIndex = 1, type = CellDataType.STRING,required = RequiredPolicy.MANDATORY)
    private String loanAccountNo;
    @XlsxColumn(header = "External Id", columnIndex = 2, type = CellDataType.STRING,required = RequiredPolicy.MANDATORY)
    private String externalId;
    @XlsxColumn(header = "Installment", columnIndex = 3, type = CellDataType.NUMERIC)
    private Integer installment;
    @XlsxColumn(header = "Transaction Amount", columnIndex = 4, type = CellDataType.DECIMAL,required = RequiredPolicy.MANDATORY)
    private int transactionAmount;
    @XlsxColumn(header = "Transaction Date", columnIndex = 5, type = CellDataType.LOCAL_DATE,required = RequiredPolicy.MANDATORY)
    private LocalDate transactionDate;
    @XlsxColumn(header = "Receipt Reference Number", columnIndex = 6, type = CellDataType.STRING)
    private String receiptReferenceNumber;
    @XlsxColumn(header = "Partner Transfer UTR", columnIndex = 7, type = CellDataType.STRING)
    private String partnerTransferUTR;
    @XlsxColumn(header = "Partner Transfer Date", columnIndex = 8, type = CellDataType.LOCAL_DATE)
    private LocalDate partnerTransferDate;
    @XlsxColumn(header= "Repayment Mode", columnIndex = 9, type = CellDataType.STRING,
            dropdownType = DropdownType.REPAYMENT_MODE)
    private String repaymentMode;
    @XlsxColumn(header="Collection Flag", columnIndex = 10, type = CellDataType.STRING
            ,dropdownType = DropdownType.COLLECTION_FLAG
            ,required = RequiredPolicy.MANDATORY)
    private String collectionFlag;
    @XlsxDynamicColumns(startIndex = 11, key = "cooling off specific")
    private List<?> coolingOffSpecific;
}
