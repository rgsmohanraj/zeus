package org.vcpl.lms.portfolio.loanaccount.bulkupload.template;

import lombok.Data;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxColumn;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxDynamicColumns;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxSheet;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.RequiredPolicy;

import java.time.LocalDate;
import java.util.List;

@Data
@XlsxSheet("Charge Collection")
public class XLSXChargeCollectionTemplate {
    @XlsxColumn(header = "S.NO", columnIndex = 0, type = CellDataType.NUMERIC)
    private int serialNo;
    @XlsxColumn(header = "Loan Account No", columnIndex = 1, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String loanAccount;
    @XlsxColumn(header = "External Id", columnIndex = 2, type = CellDataType.STRING, required = RequiredPolicy.MANDATORY)
    private String externalId;
    @XlsxColumn(header = "Installment", columnIndex = 3, type = CellDataType.NUMERIC)
    private Integer installmentNumber;
    @XlsxColumn(header = "Transaction Date", columnIndex = 4, type = CellDataType.LOCAL_DATE, required = RequiredPolicy.MANDATORY)
    private LocalDate transactionDate;
    @XlsxColumn(header = "Receipt Reference Number", columnIndex = 5, type = CellDataType.STRING)
    private String receiptReferenceNumber;
    @XlsxColumn(header = "Partner Transfer UTR", columnIndex = 6, type = CellDataType.STRING)
    private String partnerTransferUtr;
    @XlsxColumn(header = "Partner Transfer Date", columnIndex = 7, type = CellDataType.LOCAL_DATE)
    private LocalDate partnerTransferDate;
    @XlsxColumn(header= "Repayment Mode", columnIndex = 8, type = CellDataType.STRING,
            dropdownType = DropdownType.REPAYMENT_MODE)
    private String repaymentMode;
    @XlsxDynamicColumns(startIndex = 9, key = "charges")
    private List<?> charges;
}
