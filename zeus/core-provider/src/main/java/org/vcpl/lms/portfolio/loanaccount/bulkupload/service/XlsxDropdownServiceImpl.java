package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.organisation.office.domain.Office;
import org.vcpl.lms.organisation.office.domain.OfficeRepository;
import org.vcpl.lms.portfolio.client.api.ClientApiConstants;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.client.domain.ClientRepositoryWrapper;
import org.vcpl.lms.portfolio.client.domain.LegalForm;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.LoanDisburseRequest;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.mapper.BulkLoanTransactionRequestBuilder;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepository;
import org.vcpl.lms.portfolio.loanaccount.domain.VPayTransactionDetails;
import org.vcpl.lms.portfolio.loanaccount.domain.VPayTransactionDetailsRepository;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesRepository;
import org.vcpl.lms.portfolio.loanproduct.service.LoanProductReadPlatformService;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class XlsxDropdownServiceImpl implements XlsxDropdownService {

    private static final Logger LOG = LoggerFactory.getLogger(XlsxDropdownServiceImpl.class);
    private HashMap<String,Object> dropDownParametersMap = null;
    private final OfficeRepository officeRepository;

    private final LoanProductFeesChargesRepository loanProductFeesChargesRepository;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    private final LoanProductReadPlatformService loanProductReadPlatformService;

    XlsxDropdownServiceImpl(final HashMap<String,Object> dropDownParametersMap, final OfficeRepository officeRepository,
                            final CodeValueReadPlatformService codeValueReadPlatformService,
                            final LoanProductReadPlatformService loanProductReadPlatformService,
                            final LoanProductFeesChargesRepository loanProductFeesChargesRepository) {
        this.dropDownParametersMap = dropDownParametersMap;
        this.officeRepository = officeRepository;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.loanProductFeesChargesRepository = loanProductFeesChargesRepository;
    }
    @Override
    public void addDropDownToSheet(Sheet sheet, DropdownType type, Integer columnIndex) {
        String[] data = loadDropdownOfType(type);
        if(data.length != 0) {
            DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
            CellRangeAddressList addressList = new CellRangeAddressList(1, 100000, columnIndex, columnIndex);
            DataValidationConstraint constraint = createConstraintBasedOnDataSize(data,sheet,columnIndex,validationHelper, type.toString());
            DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.createErrorBox(type.errorBoxTitle(), type.errorBoxMessage());
            dataValidation.setShowErrorBox(true);
            dataValidation.setShowPromptBox(true);
            sheet.addValidationData(dataValidation);
            LOG.info("Added Dropdown - {} to Sheet - {}", new Object[]{type.name(),sheet.getSheetName()});
        }
    }

    @Override
    public void addFormulaToColumn(Sheet sheet, String formula, Integer columnIndex) {
        DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
        CellRangeAddressList addressList = new CellRangeAddressList(1, 100000, columnIndex, columnIndex);
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(formula);
        DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
        dataValidation.setSuppressDropDownArrow(true);
        dataValidation.createErrorBox("Invalid External Id", "Please choose External Id from dropdown");
        dataValidation.setShowErrorBox(true);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);
    }

    public void addMinMaxValidationToColumn(Sheet sheet, Integer min, Integer max, Integer columnIndex) {
        DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
        CellRangeAddressList addressList = new CellRangeAddressList(1, 100000, columnIndex, columnIndex);
        DataValidationConstraint constraint = validationHelper.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN,
                String.valueOf(min),String.valueOf(max));
        DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
        dataValidation.setSuppressDropDownArrow(true);
        dataValidation.createErrorBox("Invalid External Id", "Please choose External Id from dropdown");
        dataValidation.setShowErrorBox(true);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);
    }

    private String[] loadDropdownOfType(DropdownType dropdownType) {

        return switch (dropdownType) {
            case CHARGE -> {
                Long productId = (Long) this.dropDownParametersMap.get("productId");
                LOG.info("Product Id {} ",productId );
                yield this.loanProductFeesChargesRepository.getChargesByLoanProductId(productId).toArray(String[]::new);
            }
            case OFFICE -> this.officeRepository.findAll().stream().map(Office::getName).toArray(String[]::new);
            case GENDER -> this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.GENDER)
                    .stream().map(CodeValueData::getName).toArray(String[]::new);
            case STATE -> this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.STATE)
                    .stream().map(CodeValueData::getName).toArray(String[]::new);
//            case CITY -> this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CITY)
//                    .stream().map(CodeValueData::getName).toArray(String[]::new);
            case REPAYMENT_MODE -> this.codeValueReadPlatformService.retrieveCodeValuesByCode("RepaymentMode")
                        .stream().map(CodeValueData::getName).toArray(String[]::new);
//            case TRANSACTION_TYPE_PREFERENCE -> this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.TRANSACTIONTYPEPREFERENCE)
//                    .stream().map(CodeValueData::getName).toArray(String[]::new);
            case DUE_DAYS -> IntStream.rangeClosed(1, 31).boxed().map(String::valueOf).toArray(String[]::new);
            case APPLICANT_TYPE -> new String[] {LegalForm.PERSON.toString(),LegalForm.ENTITY.toString()};
            case ACCOUNT_TYPE -> this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.ACCOUNTTYPE)
                    .stream().map(CodeValueData::getName).toArray(String[]::new);
            case ASSET_CLASS -> this.codeValueReadPlatformService.retrieveCodeValuesByCode("AssetClass")
                    .stream().map(CodeValueData::getName).toArray(String[]::new);
            case COLLECTION_FLAG -> this.codeValueReadPlatformService.retrieveCodeValuesByCode("CollectionFlag")
                    .stream().map(CodeValueData::getName).toArray(String[]::new);
            default -> new String[0];
        };
    }

    private DataValidationConstraint createConstraintBasedOnDataSize(String[] data, Sheet sheet, Integer columnIndex,
                                                                     DataValidationHelper validationHelper, String dropDownName) {
        if(Arrays.stream(data).collect(Collectors.joining(",")).length() < 250)
           return validationHelper.createExplicitListConstraint(data);

        Workbook workbook = sheet.getWorkbook();
        Sheet hidden = workbook.createSheet(dropDownName);
        for (int i = 0, length= data.length; i < length; i++) {
            String name = data[i];
            Row row = hidden.createRow(i);
            Cell cell = row.createCell(columnIndex);
            cell.setCellValue(name);
        }
        Name namedCell = workbook.createName();
        namedCell.setNameName(dropDownName);
        CellAddress cellAddress =new CellAddress(1, columnIndex);
        String address = cellAddress.toString();
        StringBuilder columnAddress = new StringBuilder();
        for (int i=0; i<address.length(); i++) {
            if(!Character.isAlphabetic(address.charAt(i))) break;
            columnAddress.append(address.charAt(i));
        }
        StringBuilder referenceFormulaBuilder = new StringBuilder(dropDownName).append("!").append("$").append(columnAddress)
                .append("$").append(1).append(":").append("$").append(columnAddress).append("$").append(data.length);
        namedCell.setRefersToFormula(referenceFormulaBuilder.toString());
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(dropDownName);
        workbook.setSheetHidden(workbook.getSheetIndex(dropDownName), true);
        return constraint;
    }
}
