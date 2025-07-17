package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.vcpl.client.domain.*;
import org.vcpl.client.enumeration.PaymentType;
import org.vcpl.client.gateway.VpayGateway;
import org.vcpl.lms.infrastructure.bulkimport.domain.ImportDocument;
import org.vcpl.lms.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.VPayCredentialManager;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.documentmanagement.api.FileUploadValidator;
import org.vcpl.lms.infrastructure.documentmanagement.domain.Document;
import org.vcpl.lms.infrastructure.documentmanagement.domain.DocumentRepository;
import org.vcpl.lms.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl;
import org.vcpl.lms.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.*;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetails;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetailsRepository;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.CellDataType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnEvents;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.ColumnValidation;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.DropdownType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.mapper.BulkLoanTransactionRequestBuilder;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.template.XLXSClientLoanTemplate;
import org.vcpl.lms.portfolio.loanaccount.domain.VPayTransactionDetails;
import org.vcpl.lms.portfolio.loanaccount.domain.VPayTransactionDetailsRepository;
import org.vcpl.lms.portfolio.loanaccount.service.VPayIntegrationService;
import org.vcpl.lms.portfolio.loanproduct.domain.*;
import org.vcpl.lms.useradministration.domain.AppUser;

import jakarta.ws.rs.core.Response;

import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants.TransactionEventType.*;
import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants.Action.*;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BulkLoanUploadServiceImpl implements  BulkLoanUploadService{
    private static final Logger LOG = LoggerFactory.getLogger(BulkLoanUploadServiceImpl.class);
    @Autowired private LoanProductRepository loanProductRepository;
    @Autowired private ImportDocumentDetailsRepository importDocumentDetailsRepository;
    @Autowired private VPayIntegrationService vPayIntegrationService;
    @Autowired private FileUploadValidator fileUploadValidator;
    @Autowired private DocumentWritePlatformServiceJpaRepositoryImpl documentWritePlatformService;
    @Autowired private XlsxTemplateService xlsxTemplateService;
    @Autowired private PlatformSecurityContext context;
    @Autowired private ImportDocumentRepository importDocumentRepository;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private BulkUploadProcessorService bulkLoanProcessorService;
    @Autowired private BulkLoanTransactionRequestBuilder bulkLoanTransactionRequestBuilder;
    @Autowired private VPayTransactionDetailsRepository vPayTransactionDetailsRepository;
    @Autowired private LoanProductFeesChargesRepository loanProductFeesChargesRepository;
    @Autowired private DocumentRepository documentRepository;

    @Autowired private VPayCredentialManager vPayCredentialManager;
    private AppUser systemAppUser;
    @Autowired private BulkUploadDocumentManagerService bulkUploadDocumentManagerServiceImpl;
    @Override
    public Response buildBulkLoansUploadTemplate(Long partnerId, Long productId) {
        HashMap<String,Object> dropDownParametersMap = new HashMap<>();
        dropDownParametersMap.put("partnerId", partnerId);
        dropDownParametersMap.put("productId", productId);
        // loanProductFeesChargesRepository.getChargesByLoanProductId(productId);
        Map<String,Object> dynamicColumns = constructDynamicColumnsMap(productId);
        Map<String,Object> mandatoryColumnsMap = reteriveMandatoryColumnsMap(productId);
        Map<ColumnEvents,Map<String,Object>> parameters = new HashMap<>();
        parameters.put(ColumnEvents.DYNAMIC_COLUMNS, dynamicColumns);
        parameters.put(ColumnEvents.REQUIRED_COLUMN, mandatoryColumnsMap);
        return xlsxTemplateService.generateCustomTemplate(XLXSClientLoanTemplate.class, dropDownParametersMap, "bulk_upload_template",
                parameters);
    }

    @Override
    public BulkUploadResponse initiateLoanProcessing(final MultipartFileUploadRequest multipartFileUploadRequest, final Long productId,
                                                     AppUser systemAppUser) throws Exception {
        validateProductId(productId);
        LoanProduct loanProduct = loanProductRepository.getReferenceById(productId);
        if(loanProduct.getIsPennyDropEnabled() || loanProduct.getIsBankDisbursementEnabled()) {
            LOG.info("Loan Product is enabled with Penny Drop or Bank Disbursement, trying to connect Vpay");
            validateZeusVPayConnection();
        }

        LocalDateTime startDateTime = LocalDateTime.now();
        this.systemAppUser = systemAppUser;

        // Copying the input stream as we need to validate and read the input file so the stream doesn't close after first read
        ByteArrayOutputStream byteArrayOutputStream = copy(multipartFileUploadRequest.inputStream());
        // Parsing the input stream to java object and process all loans
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        // Validating and Uploading document to S3
        Document document = bulkUploadDocumentManagerServiceImpl.save(multipartFileUploadRequest.fileSize(), multipartFileUploadRequest.fileDetails(),
                multipartFileUploadRequest.bodyPart(), multipartFileUploadRequest.name(), multipartFileUploadRequest.description(),
                byteArrayOutputStream, DocumentManagementEntity.BULK_CLIENT_LOANS_CREATION);
        List<ClientLoanRecord> clientLoanRecords = parseXlsxClientLoanTemplates(workbook, productId, document);
        LOG.info("Received Upload Request { ProductId: {}, Records: {} }", productId, clientLoanRecords.size());

        clientLoanRecords.stream().forEach(clientLoanRecord -> process(clientLoanRecord, loanProduct));
        // Initiating penny drop if enabled at product level
        if (loanProduct.getIsPennyDropEnabled() && loanProduct.getIsBankDisbursementEnabled()) {
            clientLoanRecords = initiatePennyDropTransaction(clientLoanRecords,loanProduct);
        }
        return updateImportDocumentDetailsResponse(document, clientLoanRecords,startDateTime);
    }

    /**
     * Wrapping the Exception thrown by VPay Client to show customized error message
     * @throws Exception
     */
    void validateZeusVPayConnection() throws Exception {
        try {
            vPayIntegrationService.pingVpay();
        } catch (Exception ex) {
            LOG.error("Unable to establish the connection between Zeus & VPay. {}", ex.getMessage());
            throw new Exception("Unable to establish the connection between Zeus & VPay. Please get in touch with admin team");
        }

    }

    private Map<String, Object> reteriveMandatoryColumnsMap(Long productId) {
        Dedupe dedupe = Dedupe.fromInt(this.loanProductRepository.getReferenceById(productId).getDedupeType());
        Map<String, Object> mandatoryColumns = new HashMap<>();
        switch (dedupe) {
            case PAN -> mandatoryColumns.put("PAN", Boolean.TRUE);
            case AADHAAR -> mandatoryColumns.put("Aadhaar", Boolean.TRUE);
        }
        return mandatoryColumns;
    }

    private List<ClientLoanRecord> initiatePennyDropTransaction(List<ClientLoanRecord> clientLoanRecords, LoanProduct loanProduct) throws JsonProcessingException {
        List<TransactionRequest> clientBankDetails = buildPennyDropTransactionRequest(clientLoanRecords);
        if (!clientBankDetails.isEmpty()) {
            TransactionTracker pennyDisburseTracker = new TransactionTracker();
            pennyDisburseTracker.setBankData(clientBankDetails);
            LOG.debug("Penny Drop Request {} ", new ObjectMapper().writeValueAsString(pennyDisburseTracker));
            String disbursementBankAccount = loanProduct.getDisbursementBankAccount().getLabel();
            TransactionResponse pennyDropAcknowledgements = VpayGateway.apiCallForDisbursement(pennyDisburseTracker, vPayCredentialManager.getVpayCredentialWithProductName(disbursementBankAccount));
            clientLoanRecords = updateVpayTransaction(clientLoanRecords, pennyDropAcknowledgements);
        }
        return clientLoanRecords;
    }

    private void validateDuplicateExternalId(List<ClientLoanRecord> clientLoanRecords, Document document) {
        clientLoanRecords.stream()
                .collect(Collectors.groupingBy(ClientLoanRecord::getExternalId))
                .entrySet().stream().filter(stringListEntry -> stringListEntry.getValue().size() > 1)
                .flatMap(stringListEntry -> stringListEntry.getValue().stream())
                .forEach(clientLoanRecord -> {
                    clientLoanRecord.getErrorRecords().add("Duplicate ExternalId: " + clientLoanRecord.getExternalId());
                });
    }

    private void validateMandatoryFields(List<ClientLoanRecord> clientLoanRecords, Document document) {
        List<ClientLoanRecord> mandateMissingRecords = clientLoanRecords.stream()
                .filter(clientLoanRecord -> clientLoanRecord.getErrorRecords()
                        .stream().collect(Collectors.joining(".")).contains("External Id")).collect(Collectors.toList());
        if(!mandateMissingRecords.isEmpty()) {
            List<ApiParameterError> errors = new ArrayList<>();
            String serialNumbers =
                    mandateMissingRecords.stream()
                            .map(ClientLoanRecord::getSerialNo)
                            .map(String::valueOf).collect(Collectors.joining(",")).toString();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Failed to process the upload Request as external Id in Rows: " + serialNumbers + " are missing",
                    ""));
            documentRepository.delete(document);
            throw new PlatformApiDataValidationException(errors);
        }
    }

    private static void validateEmptyRecords(List<ClientLoanRecord> clientLoanRecords) {
        if(clientLoanRecords.isEmpty()) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Uploaded Excel Cannot be empty",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
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

    private Map<String,Object> constructDynamicColumnsMap(Long productId) {
        List<LoanProductFeesCharges> loanProductFeesCharges=  this.loanProductFeesChargesRepository.getListChargesByLoanProductId(productId,false);
        List<String>charges=new ArrayList<>();
   for(LoanProductFeesCharges loanProductFeesCharge: loanProductFeesCharges)
   {
       charges.add(loanProductFeesCharge.getCharge().getName());
   }
        List<XlsxColumnSchema> xlsxColumnSchemas = charges
                .stream()
                .map(s -> new XlsxColumnSchema(0, CellDataType.DECIMAL, false, s, DropdownType.NONE, StringUtils.EMPTY, ColumnValidation.NONE))
                .collect(Collectors.toList());
//        List<XlsxColumnSchema> xlsxColumnSchemas = this.loanProductFeesChargesRepository.getChargesByLoanProductId(productId)
//                .stream()
//                .map(s -> new XlsxColumnSchema(0, CellDataType.DECIMAL, false, s, DropdownType.NONE, StringUtils.EMPTY, ColumnValidation.NONE))
//                .collect(Collectors.toList());

        Map<String, Object> dynamicColumnMap = new HashMap<>();
        dynamicColumnMap.put("charges", xlsxColumnSchemas);
        return  dynamicColumnMap;
    }
    private List<ClientLoanRecord> updateVpayTransaction(final List<ClientLoanRecord> clientLoanRecords,
                                       final TransactionResponse pennyDropAcknowledgements) throws JsonProcessingException {
        if(Objects.isNull(pennyDropAcknowledgements)) {
            LOG.error("Transaction Response is empty for pennyDrop request");
            return clientLoanRecords;
        }
        LOG.debug("Response {}", new ObjectMapper().writeValueAsString(pennyDropAcknowledgements));
        List<ClientLoanRecord> records = clientLoanRecords.stream().filter(clientLoanRecord -> !clientLoanRecord.isErrorRecord()).toList();
        try{
            if(!Objects.isNull(pennyDropAcknowledgements.getResponseList())) {
                pennyDropAcknowledgements.getResponseList().getListOfAcknowledgement().forEach(acknowledgement -> {
                    records.stream().filter(clientLoanRecord -> clientLoanRecord.getLoanId()
                            .toString().equals(acknowledgement.getExternalId())).findAny().ifPresent(clientLoanRecord -> {
                        VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(clientLoanRecord.getLoanId(),
                                clientLoanRecord.getClientId(), PaymentType.IMPS.name(), PENNY_DROP,
                                acknowledgement.referenceId, null, null, IN_PROGRESS, BigDecimal.ONE, new Date(),
                                this.systemAppUser);
                        vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                    });
                });
            }
            if(!Objects.isNull(pennyDropAcknowledgements.getFailureList().getBankValidationErrorResponse())) {
                pennyDropAcknowledgements.getFailureList()
                        .getBankValidationErrorResponse()
                        .getListOfAcknowledgement()
                        .forEach(acknowledgement -> {
                            records.stream().filter(clientLoanRecord -> clientLoanRecord.getLoanId()
                                    .toString().equals(acknowledgement.getExternalId())).findAny().ifPresent(clientLoanRecord -> {
                                clientLoanRecord.getErrorRecords().add(acknowledgement.getReason());
                                clientLoanRecord.getImportDocumentDetails().setReason(acknowledgement.getReason());
                                VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(clientLoanRecord.getLoanId(),
                                        clientLoanRecord.getClientId(), PaymentType.IMPS.name(), PENNY_DROP,
                                        null, null, FAILURE, acknowledgement.getReason(), BigDecimal.ZERO, new Date(),
                                        this.systemAppUser);
                                vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                            });
                        });
            }
            if(!Objects.isNull(pennyDropAcknowledgements.getFailureList().getVpayValidationErrorResponse())) {
                pennyDropAcknowledgements.getFailureList()
                        .getVpayValidationErrorResponse()
                        .stream()
                        .collect(Collectors.groupingBy(VpayValidationErrorResponse::getId))
                        .forEach((id, vpayValidationErrorResponses) -> {
                            records.stream().filter(clientLoanRecord -> clientLoanRecord.getLoanId()
                                    .toString().equals(id)).findAny().ifPresent(clientLoanRecord -> {
                                vpayValidationErrorResponses.forEach(vpayValidationErrorResponse -> {
                                    clientLoanRecord.getErrorRecords().add(vpayValidationErrorResponse.getReason());
                                    clientLoanRecord.getImportDocumentDetails().setReason(vpayValidationErrorResponse.getReason());
                                });
                                VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(clientLoanRecord.getLoanId(),
                                        clientLoanRecord.getClientId(), PaymentType.IMPS.name(), PENNY_DROP,
                                        null, null, FAILURE,
                                        vpayValidationErrorResponses.stream().map(VpayValidationErrorResponse::getReason).collect(Collectors.joining(". ")),
                                        BigDecimal.ZERO, new Date(), this.systemAppUser);
                                vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                            });
                        });
            }
        } catch (Exception exception) {
            LOG.error("Failed while processing penny drop transaction response Error: {} ", exception.getMessage());
        }
        return clientLoanRecords;
    }

    private List<TransactionRequest> buildPennyDropTransactionRequest(List<ClientLoanRecord> clientLoanRecords) {
        LOG.debug("Building PennyDrop Transaction Request");
        return clientLoanRecords.stream()
                .filter(clientLoanRecord -> !clientLoanRecord.isErrorRecord())
                .map(bulkLoanTransactionRequestBuilder::buildPennyDropTransactionRequest)
                .toList();
    }

    public void updateImportDocumentDetails(ClientLoanRecord clientLoanRecord) {
        boolean status = clientLoanRecord.getErrorRecords().isEmpty();
        String reason = status ?
                Objects.isNull(clientLoanRecord.getInfoRecords()) ? StringUtils.EMPTY
                        : clientLoanRecord.getInfoRecords()
                            .stream()
                            .collect(Collectors.joining(". "))
                : clientLoanRecord.getErrorRecords()
                    .stream()
                    .collect(Collectors.joining(". "));
        // SET importId - 0 temporarily the exact importId will be updated in the same object in updateImportDocumentDetailsResponse method
        ImportDocumentDetails importDocumentDetails = new ImportDocumentDetails(0L, clientLoanRecord.getExternalId(),
                clientLoanRecord.getLoanId(),clientLoanRecord.getLoanAccountNo(), new Date(),status,reason);
        clientLoanRecord.setImportDocumentDetails(importDocumentDetails);
    }

    private BulkUploadResponse updateImportDocumentDetailsResponse(Document document, List<ClientLoanRecord> clientLoanRecords, LocalDateTime startDateTime)
    {
        Integer totalRecordCount = clientLoanRecords.size();
        Integer failedRecordCount = Math.toIntExact(clientLoanRecords.stream().filter(ClientLoanRecord::isErrorRecord).count());
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
    public List<ClientLoanRecord> parseXlsxClientLoanTemplates(XSSFWorkbook workbook,final Long productId,
                                                              final Document document) throws InstantiationException,
            IllegalAccessException {
        Map<String,Object> dynamicColumns = constructDynamicColumnsMap(productId);
        Map<String,Object> mandatoryColumnsMap = reteriveMandatoryColumnsMap(productId);
        Map<ColumnEvents,Map<String,Object>> parameters = new HashMap<>();
        parameters.put(ColumnEvents.DYNAMIC_COLUMNS, dynamicColumns);
        parameters.put(ColumnEvents.REQUIRED_COLUMN, mandatoryColumnsMap);
        List<ClientLoanRecord> clientLoanRecords =  (List<ClientLoanRecord>) xlsxTemplateService
                .readExcel(workbook, XLXSClientLoanTemplate.class, ClientLoanRecord.class, parameters);
        validateEmptyRecords(clientLoanRecords);
        validateMandatoryFields(clientLoanRecords, document);
        validateDuplicateExternalId(clientLoanRecords, document);
        return clientLoanRecords;
    }
    private void process(ClientLoanRecord clientLoanRecord, LoanProduct loanProduct) {
        if (!clientLoanRecord.isErrorRecord()) {
            try {
                LOG.debug("Processing - {}", clientLoanRecord);
                ClientLoanRecord clientLoanRecordData=applyDefaultCharge(clientLoanRecord,loanProduct);
                bulkLoanProcessorService.process(clientLoanRecordData, loanProduct);
            } catch (PlatformApiDataValidationException exception) {
                LOG.error("[RollingBackTransaction] Upload Failed - External Id: {}", clientLoanRecord.getExternalId() + " Exception : " + exception.getMessage());
                exception.getErrors().stream()
                        .map(ApiParameterError::getDeveloperMessage).forEach(clientLoanRecord.getErrorRecords()::add);
            } catch (Exception exception) {
                LOG.error("[RollingBackTransaction] Upload Failed - External Id: {}", clientLoanRecord.getExternalId() + " Exception : " + exception.getMessage());
                clientLoanRecord.getErrorRecords().add(exception.getMessage());
            }
        }
        updateImportDocumentDetails(clientLoanRecord);
    }
    private ClientLoanRecord applyDefaultCharge(ClientLoanRecord clientLoanRecord,LoanProduct loanProduct)
    {
        List<LoanProductFeesCharges> loanProductFeesCharges=loanProductFeesChargesRepository.getListChargesByLoanProductId(loanProduct.getId(),true);
        Map<String,BigDecimal> map=clientLoanRecord.getCharges();
        for(LoanProductFeesCharges loanProductFeesChargesData:loanProductFeesCharges)
            map.put(loanProductFeesChargesData.getCharge().getName(),loanProductFeesChargesData.getCharge().getAmount());
        clientLoanRecord.setCharges(map);
        return clientLoanRecord;
    }
}
