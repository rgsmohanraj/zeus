package org.vcpl.lms.portfolio.loanaccount.bulkupload.template;

import lombok.Data;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxColumn;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxColumnFormula;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxSheet;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;

import java.math.BigDecimal;
import java.util.List;

@Data
@XlsxSheet("Charges")
public class XLXSChargeTemplate {

    @XlsxColumn(header = "External Id", columnIndex = 0, type = CellDataType.STRING,
            formula = "'ClientLoan'!$B$2:$B$100000")
    private String externalId;
    @XlsxColumn(header = "Charge Name", columnIndex = 1, type = CellDataType.STRING,
            dropdownType = DropdownType.CHARGE)
    private String chargeName;
    @XlsxColumn(header = "Value", columnIndex = 2, type = CellDataType.DECIMAL)
    private BigDecimal value;
}
