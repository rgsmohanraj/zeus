package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.*;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetails;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetailsRepository;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnEvents;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnValidation;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.template.XLSXChargeCollectionTemplate;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesCharges;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesRepository;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRepository;

import jakarta.ws.rs.core.Response;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulkChargeRepaymentServiceImpl implements BulkChargeRepaymentService {
    private static final Logger LOG = LoggerFactory.getLogger(BulkChargeRepaymentServiceImpl.class);
    private final LoanProductFeesChargesRepository loanProductFeesChargesRepository;
    private final LoanProductRepository loanProductRepository;
    private final XlsxTemplateService xlsxTemplateService;
    private final FileUploadValidator fileUploadValidator;
    private final DocumentWritePlatformServiceJpaRepositoryImpl documentWritePlatformService;
    private final DocumentRepository documentRepository;
    private final PlatformSecurityContext context;
    private final ImportDocumentRepository importDocumentRepository;
    private final ImportDocumentDetailsRepository importDocumentDetailsRepository;
    private final BulkUploadDocumentManagerService bulkUploadDocumentManagerServiceImpl;
    private final BulkUploadProcessorService bulkLoanProcessorService;
    @Override
    public Response buildBulkChargeRepaymentUploadTemplate(Long partnerId, Long productId) {
        HashMap<String,Object> dropDownParametersMap = new HashMap<>();
        dropDownParametersMap.put("partnerId", partnerId);
        dropDownParametersMap.put("productId", productId);
        Map<String,Object> dynamicColumns = constructDynamicColumnsMap(productId);
        Map<ColumnEvents,Map<String,Object>> parameters = new EnumMap<>(ColumnEvents.class);
        parameters.put(ColumnEvents.DYNAMIC_COLUMNS, dynamicColumns);
        return xlsxTemplateService.generateCustomTemplate(XLSXChargeCollectionTemplate.class, dropDownParametersMap, "bulk_charge_collection",
                parameters);
    }


    @Override
    public BulkUploadResponse initiateChargeRepayment(MultipartFileUploadRequest multipartFileUploadRequest, Long productId) throws IOException, InstantiationException, IllegalAccessException {
        validateProductId(productId);
        LocalDateTime startDateTime = LocalDateTime.now();
        // Copying the input stream as we need to validate and read the input file so the stream doesn't close after first read
        ByteArrayOutputStream byteArrayOutputStream = copy(multipartFileUploadRequest.inputStream());
        // Parsing the input stream to java object and process all loans
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        // Validating and Uploading document to S3
        Document document = bulkUploadDocumentManagerServiceImpl.save(multipartFileUploadRequest.fileSize(), multipartFileUploadRequest.fileDetails(),
                multipartFileUploadRequest.bodyPart(), multipartFileUploadRequest.name(), multipartFileUploadRequest.description(),
                byteArrayOutputStream, DocumentManagementEntity.BULK_CHARGE_REPAYMENT);

        List<ChargeRepaymentRecord> chargeCollectionRecords = parseXlsxClientLoanTemplates(workbook, productId, document);
        LOG.info("Received Charge Bulk Repayment Request { ProductId: {}, Records: {} }", productId, chargeCollectionRecords.size());
        Map<String,Charge> chargeMap = retrieveChargeIds(productId);
        chargeCollectionRecords.forEach(collectionRecord -> {
            if (!collectionRecord.isErrorRecord()) {
                try{
                    bulkLoanProcessorService.repayCharge(collectionRecord,chargeMap);
                }  catch (PlatformApiDataValidationException exception) {
                    LOG.error("Upload Failed - External Id: {}", collectionRecord.getExternalId() + " Exception : " + String.join(",", exception.getErrors().stream()
                            .map(ApiParameterError::getDefaultUserMessage).toList()));
                    exception.getErrors().stream()
                            .map(ApiParameterError::getDeveloperMessage).forEach(collectionRecord.getErrorRecords()::add);
                } catch (Exception exception) {
                    LOG.error("Upload Failed - External Id: {}", collectionRecord.getExternalId() + " Exception : " + exception.getMessage());
                    collectionRecord.getErrorRecords().add(exception.getMessage());
                }
            }
            updateImportDocumentDetails(collectionRecord);
        });
        return updateImportDocumentDetailsResponse(document, chargeCollectionRecords,startDateTime);
    }

    private Map<String, Charge> retrieveChargeIds(Long productId) {
        return loanProductRepository.getReferenceById(productId).getLoanProductFeesCharges()
                .stream().map(LoanProductFeesCharges::getCharge)
                .collect(Collectors.toMap(Charge::getName, charge -> charge));
    }

    public List<ChargeRepaymentRecord> parseXlsxClientLoanTemplates(XSSFWorkbook workbook, final Long productId, final Document document) throws InstantiationException,
            IllegalAccessException {
        Map<String,Object> dynamicColumns = constructDynamicColumnsMap(productId);
        Map<ColumnEvents,Map<String,Object>> parameters = new EnumMap<>(ColumnEvents.class);
        parameters.put(ColumnEvents.DYNAMIC_COLUMNS, dynamicColumns);
        List<ChargeRepaymentRecord> clientLoanRecords = (List<ChargeRepaymentRecord>) xlsxTemplateService
                .readExcel(workbook, XLSXChargeCollectionTemplate.class, ChargeRepaymentRecord.class, parameters);
        if(clientLoanRecords.isEmpty()) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Uploaded Excel Cannot be empty",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }

        validateMandatoryFields(clientLoanRecords, document);
        return clientLoanRecords;
    }

    private Map<String,Object> constructDynamicColumnsMap(Long productId) {
        List<XlsxColumnSchema> xlsxColumnSchemas = new ArrayList<>();
        this.loanProductFeesChargesRepository.getChargesForCollection(productId)
                .forEach(header -> {
                    xlsxColumnSchemas.add(new XlsxColumnSchema(0, CellDataType.DECIMAL, false, header + " Paid",
                            DropdownType.NONE, StringUtils.EMPTY, ColumnValidation.NONE));
                    xlsxColumnSchemas.add(new XlsxColumnSchema(0, CellDataType.DECIMAL, false, header + " Waived Off",
                            DropdownType.NONE, StringUtils.EMPTY, ColumnValidation.NONE));
                });
        Map<String, Object> dynamicColumnMap = new HashMap<>();
        dynamicColumnMap.put("charges", xlsxColumnSchemas);
        return  dynamicColumnMap;
    }

    private static void validateProductId(Long productId) {
        if(Objects.isNull(productId)) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.loan.upload.invalid.productId",
                    "Product cannot be empty",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
    }

    private void validateMandatoryFields(List<ChargeRepaymentRecord> clientLoanRecords, Document document) {
        List<ChargeRepaymentRecord> mandateMissingRecords = clientLoanRecords.stream()
                .filter(chargeRepaymentRecord -> chargeRepaymentRecord.getErrorRecords()
                        .stream().collect(Collectors.joining(".")).contains("External Id")).toList();
        if(!mandateMissingRecords.isEmpty()) {
            List<ApiParameterError> errors = new ArrayList<>();
            String serialNumbers =
                    mandateMissingRecords.stream()
                            .map(ChargeRepaymentRecord::getSerialNo)
                            .map(String::valueOf).collect(Collectors.joining(","));
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Failed to process the upload Request as external Id in Rows: " + serialNumbers + " are missing",
                    ""));
            documentRepository.delete(document);
            throw new PlatformApiDataValidationException(errors);
        }
    }

    private BulkUploadResponse updateImportDocumentDetailsResponse(Document document, List<ChargeRepaymentRecord> clientLoanRecords, LocalDateTime startDateTime) {
        Integer totalRecordCount = clientLoanRecords.size();
        Integer failedRecordCount = Math.toIntExact(clientLoanRecords.stream().filter(ChargeRepaymentRecord::isErrorRecord).count());
        ImportDocument importDocument = ImportDocument.instance(document, startDateTime,
                DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity.LOANS.ordinal(),
                context.authenticatedUser(),clientLoanRecords.size());
        importDocument.update(LocalDateTime.now(),(totalRecordCount - failedRecordCount), failedRecordCount);
        final ImportDocument importedDocument = importDocumentRepository.saveAndFlush(importDocument);
        List<ImportDocumentDetails> importDocumentDetailsList = clientLoanRecords.stream()
                .map(clientLoanRecord -> {
                    ImportDocumentDetails importDocumentDetails = clientLoanRecord.getImportDocumentDetails();
                    importDocumentDetails.setStatus(!clientLoanRecord.isErrorRecord());
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

    public void updateImportDocumentDetails(ChargeRepaymentRecord chargeRepaymentRecord) {
        boolean status = chargeRepaymentRecord.getErrorRecords().isEmpty();
        String reason = status ?
                Objects.isNull(chargeRepaymentRecord.getInfoRecords()) ? StringUtils.EMPTY
                        : chargeRepaymentRecord.getInfoRecords()
                        .stream()
                        .collect(Collectors.joining(". "))
                : chargeRepaymentRecord.getErrorRecords()
                .stream()
                .collect(Collectors.joining(". "));
        // SET importId - 0 temporarily the exact importId will be updated in the same object in updateImportDocumentDetailsResponse method
        ImportDocumentDetails importDocumentDetails = new ImportDocumentDetails(0L, chargeRepaymentRecord.getExternalId(),
                chargeRepaymentRecord.getLoanId(),chargeRepaymentRecord.getLoanAccount(), new Date(),status,reason);
        chargeRepaymentRecord.setImportDocumentDetails(importDocumentDetails);
    }
}
