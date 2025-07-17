package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnValidation;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.util.Arrays;
import java.util.Objects;

public class XlsxValidationServiceImpl implements XlsxValidationService {

    private final LoanProduct loanProduct;

    public XlsxValidationServiceImpl(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }
    @Override
    public String[] getMinMaxValue(ColumnValidation columnValidation) {
        String[] minMaxValue = new String[2];
        switch (columnValidation) {
            case INTEREST_RATE -> {
                minMaxValue[0] = String.valueOf(loanProduct.getMinNominalInterestRatePerPeriod());
                minMaxValue[1] = String.valueOf(loanProduct.getMaxNominalInterestRatePerPeriod());
            }
            case LOAN_TERM -> {
                minMaxValue[0] = String.valueOf(loanProduct.getMinNumberOfRepayments());
                minMaxValue[1] = String.valueOf(loanProduct.getMaxNumberOfRepayments());
            }
            case PRINCIPLE_AMOUNT -> {
                minMaxValue[0] = String.valueOf(loanProduct.getMinPrincipalAmount().getAmount());
                minMaxValue[1] = String.valueOf(loanProduct.getMaxPrincipalAmount().getAmount());
            }
        }
        return minMaxValue;
    }

    @Override
    public void addMinMaxValidation(Sheet sheet, final Integer columnIndex, final ColumnValidation columnValidation, final CellDataType type) {
        String[] minMaxValue = this.getMinMaxValue(columnValidation);
        if(Objects.isNull(minMaxValue[0]) || Objects.isNull(minMaxValue[1])) {
            return;
        }
        DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
        CellRangeAddressList addressList = new CellRangeAddressList(1, 100000, columnIndex, columnIndex);
        DataValidationConstraint constraint = null;
        if(CellDataType.DECIMAL.equals(type)) {
            constraint = validationHelper.createDecimalConstraint(DataValidationConstraint.OperatorType.BETWEEN,
                        minMaxValue[0],minMaxValue[1]);
        } else {
            constraint =  validationHelper.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN,
                        minMaxValue[0],minMaxValue[1]);
        }
        DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
        dataValidation.setSuppressDropDownArrow(true);
        String message = columnValidation.errorBoxMessage()
                    .replace(":min", minMaxValue[0])
                    .replace(":max", minMaxValue[1]);
        dataValidation.createErrorBox(columnValidation.errorBoxTitle(), message);
        dataValidation.setShowErrorBox(true);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);
    }


}
