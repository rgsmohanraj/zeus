package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnValidation;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XlsxColumnSchema {
    private int columnIndex;
    private CellDataType type;
    private boolean required;
    private String header;
    private DropdownType dropdownType;
    private String formula;
    private ColumnValidation validation;

}
