package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.XlsxColumnSchema;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnEvents;

import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface XlsxTemplateService {

    public Response generatePlainTemplate(final Class<?> clazz,final String fileName);
    List<String> generateHeaderList(final Class<?> clazz);
    public Response generateCustomTemplate(Class<?> clazz, final HashMap<String,Object> dropDownParametersMap, final String fileName,
                                           final Map<ColumnEvents,Map<String,Object>> dynamicColumnMap);
    XSSFSheet verifyExcel(Class<?> clazz, final Map<ColumnEvents,Map<String,Object>> dynamicColumnMap, final XSSFWorkbook workbook);
    public List<?> readExcel(XSSFWorkbook workbook, Class clazz, Class pojo, Map<ColumnEvents,Map<String,Object>> dynamicColumnSchemas) throws InstantiationException, IllegalAccessException;
    //Based on the template you will get the headerName with header details
    public <T> Map<String, XlsxColumnSchema> getColumnDetails(Class<T> template);
    public void validateAndReturnErrorMessage(Map<String, XlsxColumnSchema> columnDetails, List<String> errorMessagePush, Object clientRecord);
    public Map<String,Object> generateDynamicFieldsSpecificToCoolingOff(final Long productId);

}
