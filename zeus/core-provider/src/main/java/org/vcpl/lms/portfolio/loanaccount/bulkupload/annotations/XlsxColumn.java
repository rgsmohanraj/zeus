package org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations;

import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnValidation;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.RequiredPolicy;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XlsxColumn {
    int columnIndex();
    CellDataType type();
    String header();
    DropdownType dropdownType() default DropdownType.NONE;
    String formula() default StringUtils.EMPTY;
    ColumnValidation validation() default ColumnValidation.NONE;
    RequiredPolicy required() default RequiredPolicy.NON_MANDATORY;
}
