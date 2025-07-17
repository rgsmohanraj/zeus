package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.vcpl.lms.infrastructure.core.exception.UnsupportedParameterException;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.bulkimport.domain.ImportDocument;
import org.vcpl.lms.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.documentmanagement.api.FileUploadValidator;
import org.vcpl.lms.infrastructure.documentmanagement.domain.Document;
import org.vcpl.lms.infrastructure.documentmanagement.domain.DocumentRepository;
import org.vcpl.lms.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl;
import org.vcpl.lms.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkUploadResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.MultipartFileUploadRequest;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.RepaymentRecord;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetails;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetailsRepository;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.template.XLSXRepaymentTemplate;

import jakarta.ws.rs.core.Response;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BulkRepaymentUploadServiceImpl implements BulkRepaymentUploadService {
    private static final Logger LOG = LoggerFactory.getLogger(BulkRepaymentUploadServiceImpl.class);
    @Autowired private XlsxTemplateService xlsxTemplateService;
    @Autowired private BulkUploadProcessorService bulkLoanProcessorService;
    @Autowired private FileUploadValidator fileUploadValidator;
    @Autowired private DocumentWritePlatformServiceJpaRepositoryImpl documentWritePlatformService;
    @Autowired private PlatformSecurityContext context;
    @Autowired private ImportDocumentRepository importDocumentRepository;
    @Autowired private ImportDocumentDetailsRepository importDocumentDetailsRepository;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private BulkUploadDocumentManagerService bulkUploadDocumentManagerServiceImpl;

    @Override
    public Response buildBulkRepaymentTemplate(Long productId) {
        Map<String,Object> dynamicColumns= xlsxTemplateService.generateDynamicFieldsSpecificToCoolingOff(productId);
        Map<ColumnEvents,Map<String,Object>> parameters = new HashMap<>();
        parameters.put(ColumnEvents.DYNAMIC_COLUMNS, dynamicColumns);
        parameters.put(ColumnEvents.REQUIRED_COLUMN, new HashMap<>());
        LOG.debug("Dynamic Columns generated are : {}" ,parameters);
        return xlsxTemplateService.generateCustomTemplate(XLSXRepaymentTemplate.class,new HashMap<>(),"bulkImport",parameters);
    }
    @Override
    public BulkUploadResponse initiateRepayment(MultipartFileUploadRequest multipartFileUploadRequest,Long productId) throws IOException, InstantiationException, IllegalAccessException {
        LocalDateTime startDateTime = LocalDateTime.now();
        // Copying the input stream as we need to validate and read the input file so the stream doesn't close after first read
        ByteArrayOutputStream byteArrayOutputStream = copy(multipartFileUploadRequest.inputStream());
        // Parsing the input stream to java object and process all loans
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        // Validating and Uploading document to S3
        Document document = bulkUploadDocumentManagerServiceImpl.save(multipartFileUploadRequest.fileSize(), multipartFileUploadRequest.fileDetails(),
                multipartFileUploadRequest.bodyPart(), multipartFileUploadRequest.name(), multipartFileUploadRequest.description(),
                byteArrayOutputStream, DocumentManagementEntity.BULK_LOANS_REPAYMENT);
        List<RepaymentRecord> repaymentRecords = parseXlsxTemplates(workbook,document,productId);
        for (RepaymentRecord repaymentRecord: repaymentRecords) {
            if (!repaymentRecord.isErrorRecord()) {
                try{
                    bulkLoanProcessorService.repayLoan(repaymentRecord);
                }  catch (PlatformApiDataValidationException exception) {
                    LOG.error("Upload Failed - External Id: {}", repaymentRecord.getExternalId() + " Exception : " + String.join(",", exception.getErrors().stream()
                            .map(ApiParameterError::getDefaultUserMessage).toList()));
                    exception.getErrors().stream()
                            .map(ApiParameterError::getDeveloperMessage).forEach(repaymentRecord.getErrorRecords()::add);
                } catch (Exception exception) {
                    LOG.error("Upload Failed - External Id: {}", repaymentRecord.getExternalId() + " Exception : " + exception.getMessage());
                    repaymentRecord.getErrorRecords().add(exception.getMessage());
                }
            }
            updateImportDocumentDetails(repaymentRecord);
        }
        return updateImportDocumentDetailsResponse(document,repaymentRecords,startDateTime);
    }

    public void updateImportDocumentDetails(RepaymentRecord repaymentRecord) {
        boolean status = repaymentRecord.getErrorRecords().isEmpty();
        String reason = status ? StringUtils.EMPTY : repaymentRecord.getErrorRecords()
                .stream()
                .collect(Collectors.joining(". "));
        ImportDocumentDetails importDocumentDetails = new ImportDocumentDetails(12L, repaymentRecord.getExternalId(),
                repaymentRecord.getLoanId(),repaymentRecord.getLoanAccountNo(), new Date(),status,reason);
        repaymentRecord.setImportDocumentDetails(importDocumentDetails);
    }
    @Override
    public List<RepaymentRecord> parseXlsxTemplates(final XSSFWorkbook workbook,final Document document,final Long productId) throws InstantiationException, IllegalAccessException {
        Map<String,Object> dynamicColumns = xlsxTemplateService.generateDynamicFieldsSpecificToCoolingOff(productId);
        Map<ColumnEvents,Map<String,Object>> parameters = new HashMap<>();
        parameters.put(ColumnEvents.DYNAMIC_COLUMNS, dynamicColumns);
        List<RepaymentRecord> repaymentRecords = (List<RepaymentRecord>) xlsxTemplateService.readExcel(workbook, XLSXRepaymentTemplate.class, RepaymentRecord.class, parameters);
        if(repaymentRecords.isEmpty()) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Uploaded Excel Cannot be empty",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
        validateMandatoryFields(repaymentRecords, document);
        return repaymentRecords;
    }

    private BulkUploadResponse updateImportDocumentDetailsResponse(Document document, List<RepaymentRecord> repaymentRecords, LocalDateTime startDateTime) {
        Integer totalRecordCount = repaymentRecords.size();
        Integer failedRecordCount = Math.toIntExact(repaymentRecords.stream().filter(RepaymentRecord::isErrorRecord).count());
        ImportDocument importDocument = ImportDocument.instance(document, startDateTime,
                DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity.LOANS.ordinal(),
                context.authenticatedUser(),repaymentRecords.size());
        importDocument.update(LocalDateTime.now(),(totalRecordCount - failedRecordCount), failedRecordCount);
        final ImportDocument importedDocument = importDocumentRepository.saveAndFlush(importDocument);
        List<ImportDocumentDetails> importDocumentDetailsList = repaymentRecords.stream()
                .map(clientLoanRecord -> {
                    ImportDocumentDetails importDocumentDetails = clientLoanRecord.getImportDocumentDetails();
                    importDocumentDetails.setImportId(importedDocument.getId());
                    return importDocumentDetails;
                }).toList();
        importDocumentDetailsRepository.saveAll(importDocumentDetailsList);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return new BulkUploadResponse(Math.toIntExact(importedDocument.getDocument().getId()),
                formatter.format(importedDocument.getImportTime()), formatter.format(importedDocument.getEndTime()),
                importedDocument.getCompleted(),DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity.LOANS.ordinal(),
                importedDocument.getTotalRecords(),importedDocument.getSuccessCount(),importedDocument.getFailureCount(),
                importDocumentDetailsList);
    }

    private void validateMandatoryFields(List<RepaymentRecord> clientLoanRecords, Document document) {
        List<RepaymentRecord> mandateMissingRecords = clientLoanRecords.stream()
                .filter(clientLoanRecord -> clientLoanRecord.getErrorRecords()
                        .stream().collect(Collectors.joining(".")).contains("External Id")).collect(Collectors.toList());
        if(!mandateMissingRecords.isEmpty()) {
            List<ApiParameterError> errors = new ArrayList<>();
            String serialNumbers =
                    mandateMissingRecords.stream()
                            .map(RepaymentRecord::getSerialNo)
                            .map(String::valueOf).collect(Collectors.joining(",")).toString();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Failed to process the upload Request as external Id in Rows: " + serialNumbers + " are missing",
                    ""));
            documentRepository.delete(document);
            throw new PlatformApiDataValidationException(errors);
        }
    }
}
