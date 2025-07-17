package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnValidation;

public interface XlsxValidationService {
    public String[] getMinMaxValue(final ColumnValidation columnValidation);
    public void addMinMaxValidation(final Sheet sheet, final Integer columnIndex, final ColumnValidation columnValidation, final CellDataType type);

}
