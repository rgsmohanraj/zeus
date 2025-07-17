package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.*;

import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.office.domain.OfficeRepository;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxColumn;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxDynamicColumns;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.annotations.XlsxSheet;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.XlsxColumnSchema;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.*;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.exception.XlsxReadWriteException;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.utils.CommonUtils;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesRepository;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRepository;
import org.vcpl.lms.portfolio.loanproduct.service.LoanProductReadPlatformService;

import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class XlsxTemplateServiceImpl implements  XlsxTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(XlsxTemplateServiceImpl.class);
    private XlsxDropdownService xlsxDropdownService;
    private XlsxValidationService xlsxValidationService;

    @Autowired private LoanProductFeesChargesRepository loanProductFeesChargesRepository;
    @Autowired private OfficeRepository officeRepository;
    @Autowired private CodeValueReadPlatformService codeValueReadPlatformService;
    @Autowired private LoanProductReadPlatformService loanProductReadPlatformService;

    @Autowired private LoanProductRepository loanProductRepository;
    public XlsxTemplateServiceImpl() {
    }
    @Override
    public Response generatePlainTemplate(Class<?> clazz,String fileName) {
        xlsxDropdownService = new XlsxDropdownServiceImpl(new HashMap<>(), officeRepository,
                codeValueReadPlatformService, loanProductReadPlatformService, loanProductFeesChargesRepository);
        Workbook wb = new XSSFWorkbook();
        generateHeader(clazz, wb, new HashMap<>());
        return buildResponse(wb,fileName);
    }

    @Override
    public List<String> generateHeaderList(Class<?> clazz) {
        xlsxDropdownService = new XlsxDropdownServiceImpl(new HashMap<>(), officeRepository,
                codeValueReadPlatformService, loanProductReadPlatformService, loanProductFeesChargesRepository);
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(XlsxColumn.class))
                .map(field -> field.getAnnotation(XlsxColumn.class))
                .map(xlsxColumn -> (xlsxColumn.required().equals(RequiredPolicy.MANDATORY) ? "*" : ""))
                .toList();
    }

    @Override
    public Response generateCustomTemplate(Class<?> clazz, final HashMap<String,Object> dropDownParametersMap,
                                           final String fileName, final Map<ColumnEvents,Map<String,Object>> dynamicColumnMap) {
        xlsxDropdownService = new XlsxDropdownServiceImpl(dropDownParametersMap, officeRepository,
                codeValueReadPlatformService, loanProductReadPlatformService, loanProductFeesChargesRepository);
        if(!dropDownParametersMap.isEmpty()) {
            xlsxValidationService = new XlsxValidationServiceImpl(loanProductRepository.getReferenceById((Long) dropDownParametersMap
                    .get("productId")));
        }
        Workbook wb = new XSSFWorkbook();
        generateHeader(clazz, wb, dynamicColumnMap);
        return buildResponse(wb,fileName);
    }

    @Override
    public XSSFSheet verifyExcel(Class<?> clazz, Map<ColumnEvents, Map<String, Object>> dynamicColumnMap,final XSSFWorkbook workbook) {
        XSSFSheet workSheet = verifySheetExist(clazz, workbook);
        validateHeaders(clazz, dynamicColumnMap, workSheet);
        validateEmptySheet(workSheet);
        return workSheet;
    }

    private static void validateEmptySheet(XSSFSheet workSheet) {
        if(workSheet.getPhysicalNumberOfRows() == 1) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Uploaded Excel Cannot be empty",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
    }

    private static void validateHeaders(Class<?> clazz, Map<ColumnEvents, Map<String, Object>> dynamicColumnMap, XSSFSheet workSheet) {
        List<String> invalidHeaders = new ArrayList<>();
        XSSFRow headerRow = workSheet.getRow(0);
        Map<String, Object> runtimeRequiredColumns = dynamicColumnMap.get(ColumnEvents.REQUIRED_COLUMN);
        Arrays.stream(clazz.getDeclaredFields())
                .forEach(field -> {
                    if(field.isAnnotationPresent(XlsxColumn.class)) {
                        XlsxColumn xlsxColumn = field.getAnnotation(XlsxColumn.class);
                        String requiredSymbol = "";
                        if((xlsxColumn.required().equals(RequiredPolicy.MANDATORY))
                                || (xlsxColumn.required().equals(RequiredPolicy.CONDITIONAL_MANDATE)
                                    && runtimeRequiredColumns.containsKey(xlsxColumn.header()))) {
                            requiredSymbol = "*";
                        }
                        String templateColumnHeader = xlsxColumn.header() + requiredSymbol;
                        String uploadColumnHeader = headerRow.getCell(xlsxColumn.columnIndex()).getStringCellValue();
                        if(!templateColumnHeader.equals(uploadColumnHeader)) {
                            invalidHeaders.add(uploadColumnHeader);
                        }
                    } else if(field.isAnnotationPresent(XlsxDynamicColumns.class)) {
                        XlsxDynamicColumns xlsxDynamicColumn = field.getAnnotation(XlsxDynamicColumns.class);
                        List<XlsxColumnSchema> xlsxColumnSchemas = (List<XlsxColumnSchema>) dynamicColumnMap
                                .get(ColumnEvents.DYNAMIC_COLUMNS)
                                .get(xlsxDynamicColumn.key());
                        int index = xlsxDynamicColumn.startIndex();
                        for (XlsxColumnSchema xlsxColumnSchema :xlsxColumnSchemas) {
                            String templateColumnHeader = xlsxColumnSchema.getHeader() + (xlsxColumnSchema.isRequired() ? "*" : "");
                            if(Objects.isNull(headerRow.getCell(index))) {
                                throw new XlsxReadWriteException("Invalid Template: Column - " + xlsxColumnSchema.getHeader() + " is missing.");
                            }
                            String uploadColumnHeader = headerRow.getCell(index++).getStringCellValue();
                            if(!templateColumnHeader.equals(uploadColumnHeader)) {
                                invalidHeaders.add(uploadColumnHeader);
                            }
                        }
                    }
                });
        if (!invalidHeaders.isEmpty()) {
            String invalidHeader = invalidHeaders.stream()
                    .collect(Collectors.joining(",")).toString();
            throw new XlsxReadWriteException("Invalid Template: Column Headers (" + invalidHeader + ") mismatched with actual template.");
        }
    }

    private static XSSFSheet verifySheetExist(Class<?> clazz, XSSFWorkbook workbook) {
        String sheetName = clazz.getAnnotation(XlsxSheet.class).value();
        XSSFSheet workSheet = workbook.getSheet(sheetName);
        if(Objects.isNull(workSheet)) throw new XlsxReadWriteException("Invalid Template. Please try uploading valid Template");
        return workSheet;
    }

    private void generateHeader(Class<?> clazz, Workbook wb, final Map<ColumnEvents,Map<String,Object>> parameters) {
        final XlsxSheet xlsxSheet = clazz.getAnnotation(XlsxSheet.class);
        Sheet sheet = wb.createSheet(xlsxSheet.value());
        final Row row = sheet.createRow(0);
        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(XlsxColumn.class))
                .map(field -> {
                    XlsxColumn xlsxColumn = field.getAnnotation(XlsxColumn.class);
                    boolean isRequired = isColumnMandatory(xlsxColumn, parameters.get(ColumnEvents.REQUIRED_COLUMN));
                    return new XlsxColumnSchema(xlsxColumn.columnIndex(),xlsxColumn.type(),isRequired,xlsxColumn.header(),
                            xlsxColumn.dropdownType(),xlsxColumn.formula(),xlsxColumn.validation());
                })
                .forEach(xlsxColumn -> generateXlsxColumn(wb, sheet, row, xlsxColumn));

        if(parameters.containsKey(ColumnEvents.DYNAMIC_COLUMNS)) {
            Map<String, Object> dynamicColumnMap = parameters.get(ColumnEvents.DYNAMIC_COLUMNS);
            Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(XlsxDynamicColumns.class))
                    .map(field -> field.getAnnotation(XlsxDynamicColumns.class))
                    .forEach(xlsxColumn ->
                            generateXlsxDyanmicColumn(wb, sheet, row, xlsxColumn,xlsxColumn.startIndex() ,dynamicColumnMap)
                    );
        }
    }

    private boolean isColumnMandatory(XlsxColumn xlsxColumn,Map<String,Object> parameters) {
        return switch (xlsxColumn.required()) {
            case MANDATORY -> true;
            case CONDITIONAL_MANDATE -> parameters.containsKey(xlsxColumn.header())
                    ? (boolean) parameters.get(xlsxColumn.header())
                    : false;
            default -> false;
        };
    }
    private void generateXlsxDyanmicColumn(final Workbook wb, final Sheet sheet, final Row row,
                                           final XlsxDynamicColumns xlsxDynamicColumns,int startIndex,
                                           final Map<String, Object> dynamicColumnMap) {
        List<XlsxColumnSchema> xlsxColumnSchemas = (List<XlsxColumnSchema>) dynamicColumnMap.get(xlsxDynamicColumns.key());
        for (XlsxColumnSchema xlsxColumnSchema :xlsxColumnSchemas) {
            xlsxColumnSchema.setColumnIndex(startIndex++);
            generateXlsxColumn(wb, sheet, row, xlsxColumnSchema);
        }
    }

    private void generateXlsxColumn(Workbook wb, Sheet sheet, Row row, XlsxColumnSchema xlsxColumnSchema) {
        XSSFFont xssfFont = (XSSFFont) wb.createFont();
        xssfFont.setBold(true);
        XSSFCellStyle cellStyle = (XSSFCellStyle) wb.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setLocked(false);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setFont(xssfFont);
        Cell cell = row.createCell(xlsxColumnSchema.getColumnIndex());
        /**
         * Setting Header
         */
        String cellHeader = xlsxColumnSchema.isRequired() ? xlsxColumnSchema.getHeader() + "*" : xlsxColumnSchema.getHeader();
        cell.setCellValue(cellHeader);

        CellStyle columnDataType = wb.createCellStyle();
        BuiltinFormats.getBuiltinFormat(getCellFormatBasedOnCellType(xlsxColumnSchema.getType()));
        sheet.setDefaultColumnStyle(xlsxColumnSchema.getColumnIndex(), columnDataType);

        /**
         * Setting dropdown
         */
        if (!xlsxColumnSchema.getDropdownType().equals(DropdownType.NONE)) {
            xlsxDropdownService.addDropDownToSheet(sheet, xlsxColumnSchema.getDropdownType(), xlsxColumnSchema.getColumnIndex());
        }
        if (!xlsxColumnSchema.getFormula().equals(StringUtils.EMPTY)) {
            xlsxDropdownService.addFormulaToColumn(sheet, xlsxColumnSchema.getFormula(), xlsxColumnSchema.getColumnIndex());
        }

        if (!xlsxColumnSchema.getValidation().equals(ColumnValidation.NONE)) {
            xlsxValidationService.addMinMaxValidation(sheet, xlsxColumnSchema.getColumnIndex(), xlsxColumnSchema.getValidation(),
                    xlsxColumnSchema.getType());
        }

        cell.setCellStyle(cellStyle);
        sheet.autoSizeColumn(xlsxColumnSchema.getColumnIndex());
    }

    private String getCellFormatBasedOnCellType(CellDataType type) {
        if(type.equals(CellDataType.NUMERIC)) {
            return "0";
        } else if (type.equals(CellDataType.STRING)) {
            return "TEXT";
        } else {
            return "General";
        }
    }


    @Override
    public List<?> readExcel(XSSFWorkbook workbook, Class template, Class pojo,
                             Map<ColumnEvents,Map<String,Object>> parameters) throws InstantiationException, IllegalAccessException {
        List<Object> objects = new ArrayList<>();
        XSSFSheet workSheet = verifyExcel(template,parameters,workbook);
        if(Objects.isNull(workSheet)) throw new XlsxReadWriteException("Invalid Template. Please try uploading valid Template");
        for (int index = 1; index < workSheet.getPhysicalNumberOfRows(); index++) {
            List<String> errors = new ArrayList<>();
            String headerName = null;
            XSSFRow row = workSheet.getRow(index);
            Object o = pojo.newInstance();
            List<String> requiredFieldMissing = new ArrayList<>();
            Long requiredCount = 0l;
            try {
                for (Field declaredField : template.getDeclaredFields()) {
                    if(declaredField.isAnnotationPresent(XlsxColumn.class)) {
                        XlsxColumn xlsxColumn = declaredField.getAnnotation(XlsxColumn.class);
                        XSSFCell xssfCell = row.getCell(xlsxColumn.columnIndex());
                        String name = declaredField.getName();
                        boolean skipEmptyCell = (Objects.isNull(xssfCell) && !isColumnMandatory(xlsxColumn,parameters.get(ColumnEvents.REQUIRED_COLUMN)));
                        headerName = xlsxColumn.header();
                        if(!skipEmptyCell) {
                            Object value = null;
                            requiredCount++;
                            try {
                                value = parseCellValue(xlsxColumn.type(), xssfCell);
                                if(isColumnMandatory(xlsxColumn,parameters.get(ColumnEvents.REQUIRED_COLUMN))
                                        && Objects.isNull(value)) {
                                    requiredFieldMissing.add(name);
                                    errors.add("Error in Column '" + xlsxColumn.header() + "' Reason: Column cannot be empty");
                                }
                                BeanUtils.copyProperty(o,name, value);
                            } catch (Exception ex) {
                                errors.add("Column: " + headerName +" Reason: "+ ex.getMessage());
                            }
                        }
                    }

                    if (parameters.containsKey(ColumnEvents.DYNAMIC_COLUMNS)) {
                       Map<String,Object> dynamicColumnSchemas = parameters.get(ColumnEvents.DYNAMIC_COLUMNS);
                        if(declaredField.isAnnotationPresent(XlsxDynamicColumns.class)) {
                            XlsxDynamicColumns xlsxDynamicColumns = declaredField.getAnnotation(XlsxDynamicColumns.class);
                            int startIndex = xlsxDynamicColumns.startIndex();
                            String key = xlsxDynamicColumns.key();
                            List<XlsxColumnSchema> xlsxColumnSchemas = (List<XlsxColumnSchema>) dynamicColumnSchemas.get(key);
                            String name = declaredField.getName();
                            Map<String, Object> valueMap = new HashMap<>();
                            for (XlsxColumnSchema xlsxColumnSchema: xlsxColumnSchemas) {
                                XSSFCell xssfCell = row.getCell(startIndex++);
                                boolean skipEmptyCell = (Objects.isNull(xssfCell) && !xlsxColumnSchema.isRequired());
                                headerName = xlsxColumnSchema.getHeader();
                                if(!skipEmptyCell) {
                                    Object value = null;
                                    try {
                                        value = parseCellValue(xlsxColumnSchema.getType(), xssfCell);
                                        if( xlsxColumnSchema.isRequired() && Objects.isNull(value)) {
                                            errors.add("Column '" + xlsxColumnSchema.getHeader() + "' cannot be empty");
                                        }
                                        if (value.equals(StringUtils.EMPTY)) requiredFieldMissing.add(name);
                                        valueMap.put(xlsxColumnSchema.getHeader(), value);
                                    } catch (Exception ex) {
                                        errors.add("Column: " + headerName +" Reason: "+ ex.getMessage());
                                    }
                                }
                            }
                            BeanUtils.copyProperty(o,name, valueMap);
                        }
                    }
                }
            } catch (Exception ex) {
                errors.add("Column: " + headerName +" Reason: "+ ex.getMessage());
            }
            try {
                BeanUtils.copyProperty(o,"errorRecords", errors);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            if(requiredCount != requiredFieldMissing.size()) objects.add(o);

        }
        return objects;
    }

    private static Object parseCellValue(CellDataType type, XSSFCell xssfCell) {
        if(Objects.isNull(xssfCell) || xssfCell.toString().length() == 0) {
            return null;
        }
        return switch (type) {
            case NUMERIC -> {
                try {
                    yield Double.valueOf(xssfCell.getNumericCellValue()).intValue();
                } catch (Exception exception) {
                    throw new XlsxReadWriteException("Value should be a numeric.");
                }

            }
            case STRING -> {
                if (xssfCell.getCellType().equals(CellType.NUMERIC)) {
                    yield String.valueOf(Double.valueOf(xssfCell.getNumericCellValue()).longValue());
                } else {
                    yield xssfCell.getStringCellValue();
                }
            }
            case LOCAL_DATE -> {
                try {
                    yield xssfCell.getLocalDateTimeCellValue().toLocalDate();
                } catch (Exception exception) {
                    throw new XlsxReadWriteException("Date fields should be in the 'dd-mm-yyyy' format or please check given date is valid or not");
                }
            }
            case DECIMAL -> {
                if(!NumberUtils.isCreatable(xssfCell.toString())) {
                    throw new XlsxReadWriteException(xssfCell.toString() + " is not a numeric or decimal value");
                }
                yield BigDecimal.valueOf(Double.valueOf(xssfCell.getRawValue()));
            }
            case BLANK -> StringUtils.EMPTY;
        };
    }
    private Response buildResponse(final Workbook workbook, final String entity) {
        String filename = entity + DateUtils.getLocalDateTimeOfTenant().toString() + ".xlsx";
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
        } catch (IOException e) {
            LOG.error("Problem occurred in buildResponse function", e);
        }

        final Response.ResponseBuilder response = Response.ok(baos.toByteArray());
        response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return response.build();
    }

    @Override
    //Based on the template you will get the headerName with header details
    public <T> Map<String, XlsxColumnSchema> getColumnDetails(Class<T> template) {
        Map<String, XlsxColumnSchema> columnDetails = new HashMap<>();
        for (Field declaredField : template.getDeclaredFields()) {
            XlsxColumnSchema xlsxColumnSchema = new XlsxColumnSchema();
            if (declaredField.isAnnotationPresent(XlsxColumn.class)) {
                XlsxColumn xlsxColumn = declaredField.getAnnotation(XlsxColumn.class);
                String headerName = xlsxColumn.header();
                columnDetails.put(declaredField.getName(), xlsxColumnSchema);
                xlsxColumnSchema.setHeader(headerName);
                xlsxColumnSchema.setRequired(xlsxColumn.required().equals(RequiredPolicy.MANDATORY)?Boolean.TRUE:Boolean.FALSE);
                xlsxColumnSchema.setColumnIndex(xlsxColumn.columnIndex());
            }
        }
        return columnDetails;
    }

    public void validateAndReturnErrorMessage(Map<String, XlsxColumnSchema> columnDetails, List<String> errorMessagePush, Object clientRecord) {
        for (Map.Entry<String, XlsxColumnSchema> columnDetail : columnDetails.entrySet()) {
            String headerKeyName = columnDetail.getKey();
            XlsxColumnSchema columnParameters = columnDetail.getValue();
            String headerLocaleName = columnParameters.getHeader();
            if (columnParameters.isRequired()) {
                Optional<Object> valueOfColumn = Optional.ofNullable(getDynamicRecordsFromObject(clientRecord, headerKeyName));
                if (valueOfColumn.isEmpty() || Objects.toString(valueOfColumn.get()).equals(StringUtils.EMPTY)) {
                    if (headerKeyName.equals("externalId")) {
                        setDynamicRecordsFromObject(clientRecord, "externalIdIsEmpty");
                    }
                    errorMessagePush.add("'"+headerLocaleName + "' is mandatory");
                }
            }
        }
    }
    private Object getDynamicRecordsFromObject(Object o, String k) {
        Object returnData = "EXCEPTION_SKIP";
        try{
            Field field = CommonUtils.getFieldValueFromObject(o,k);
            returnData = field.get(o);
        }catch (IllegalAccessException | NoSuchFieldException exception){
            LOG.error("Exception Occurred while getting dynamic Records".concat(exception.getMessage()));
        }
        return returnData;
    }

    private void setDynamicRecordsFromObject(Object o, String k) {
        Object returnData = "EXCEPTION_SKIP";
        try{
            Field field = CommonUtils.getFieldValueFromObject(o,k);
            field.setBoolean(o,Boolean.TRUE);
        }catch (IllegalAccessException | NoSuchFieldException exception){
            LOG.error("Exception Occurred while getting dynamic Records".concat(exception.getMessage()));
        }
    }
    public Map<String,Object> generateDynamicFieldsSpecificToCoolingOff(final Long productId){
        LoanProduct product=this.loanProductRepository.findById(productId).orElseThrow(()->new XlsxReadWriteException("No Product Found With Given Product Id"));
        Boolean coolOffApplicability=product.getProductCollectionConfig().getCoolingOffApplicability();
        List<XlsxColumnSchema> xlsxColumnSchemas=new ArrayList<>();
        LOG.debug("Cooling Off Applicability is : {}",coolOffApplicability);
        if(coolOffApplicability){
            xlsxColumnSchemas.add(new XlsxColumnSchema(0,CellDataType.LOCAL_DATE,false,"Cooling Off Date",DropdownType.NONE,StringUtils.EMPTY,ColumnValidation.NONE ));
           // xlsxColumnSchemas.add(new XlsxColumnSchema(0,CellDataType.DECIMAL,false,"InterestWaiver",DropdownType.NONE,StringUtils.EMPTY,ColumnValidation.NONE ));
        }
        Map<String,Object> dynamicColumnMap=new HashMap<>();
        dynamicColumnMap.put("cooling off specific",xlsxColumnSchemas);
        LOG.debug(" Dynamic Column Map is  : {}",dynamicColumnMap.get("cooling off specific"));
        return dynamicColumnMap;
    }
}
