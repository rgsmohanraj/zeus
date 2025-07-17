package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;

public interface XlsxDropdownService {
    void addDropDownToSheet(Sheet sheet,DropdownType type,Integer columnIndex);
    void addFormulaToColumn(Sheet sheet,String formula,Integer columnIndex);
}
