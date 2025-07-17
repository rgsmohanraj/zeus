package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.vcpl.client.domain.*;
import org.vcpl.client.enumeration.PaymentType;
import org.vcpl.client.gateway.VpayGateway;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.VPayCredentialManager;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkApiResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ClientLoanRecord;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.XlsxColumnSchema;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.mapper.BulkLoanTransactionRequestBuilder;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.template.XLXSClientLoanTemplate;
import org.vcpl.lms.portfolio.loanaccount.domain.VPayTransactionDetails;
import org.vcpl.lms.portfolio.loanaccount.domain.VPayTransactionDetailsRepository;
import org.vcpl.lms.portfolio.loanproduct.domain.Dedupe;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesRepository;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRepository;
import org.vcpl.lms.useradministration.domain.AppUser;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants.Action.FAILURE;
import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants.Action.IN_PROGRESS;
import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants.TransactionEventType.PENNY_DROP;
/**
 * Loan Bulk API development service implementation
 *
 * @author  Yuva Prasanth K
 * @version 1.0
 * @since   2024-02-05
 */
@Service
@RequiredArgsConstructor
public class BulkLoanServiceImpl implements BulkLoanService {
    private static final Logger LOG = LoggerFactory.getLogger(BulkLoanServiceImpl.class);
    private final LoanProductRepository loanProductRepository;
    private final XlsxTemplateService xlsxTemplateService;
    private final PlatformSecurityContext context;
    private final BulkUploadProcessorService bulkLoanProcessorService;
    private final BulkLoanTransactionRequestBuilder bulkLoanTransactionRequestBuilder;
    private final VPayTransactionDetailsRepository vPayTransactionDetailsRepository;
    private final LoanProductFeesChargesRepository loanProductFeesChargesRepository;
    private final VPayCredentialManager vPayCredentialManager;
    private AppUser systemAppUser;
    private Map<String, Object> reteriveMandatoryColumnsMap(Long productId) {
        Dedupe dedupe = Dedupe.fromInt(this.loanProductRepository.getReferenceById(productId).getDedupeType());
        Map<String, Object> mandatoryColumns = new HashMap<>();
        switch (dedupe) {
            case PAN -> mandatoryColumns.put("PAN", Boolean.TRUE);
            case AADHAAR -> mandatoryColumns.put("Aadhaar", Boolean.TRUE);
        }
        return mandatoryColumns;
    }

    private void initiatePennyDropTransaction(List<ClientLoanRecord> clientLoanRecords, LoanProduct loanProduct) throws JsonProcessingException {
        List<TransactionRequest> clientBankDetails = buildPennyDropTransactionRequest(clientLoanRecords);
        if (!clientBankDetails.isEmpty()) {
            TransactionTracker pennyDisburseTracker = new TransactionTracker();
            pennyDisburseTracker.setBankData(clientBankDetails);
            LOG.debug("Penny Drop Request {} ", new ObjectMapper().writeValueAsString(pennyDisburseTracker));
            String disbursementBankAccount = loanProduct.getDisbursementBankAccount().getLabel();
            TransactionResponse pennyDropAcknowledgements = VpayGateway.apiCallForDisbursement(pennyDisburseTracker, vPayCredentialManager.getVpayCredentialWithProductName(disbursementBankAccount));
            updateVpayTransaction(clientLoanRecords, pennyDropAcknowledgements);
        }
    }

    private void updateVpayTransaction(final List<ClientLoanRecord> clientLoanRecords,
                                                         final TransactionResponse pennyDropAcknowledgements) throws JsonProcessingException {
        if(Objects.isNull(pennyDropAcknowledgements)) {
            LOG.error("Transaction Response is empty for pennyDrop request");
            return;
        }
        LOG.debug("Response {}", new ObjectMapper().writeValueAsString(pennyDropAcknowledgements));
        List<ClientLoanRecord> records = clientLoanRecords.stream().filter(clientLoanRecord -> !clientLoanRecord.isErrorRecord()).toList();
        try{
            if(!Objects.isNull(pennyDropAcknowledgements.getResponseList())) {
                pennyDropAcknowledgements.getResponseList().getListOfAcknowledgement().forEach(acknowledgement -> records.stream().filter(clientLoanRecord -> clientLoanRecord.getLoanId()
                        .toString().equals(acknowledgement.getExternalId())).findAny().ifPresent(clientLoanRecord -> {
                    VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(clientLoanRecord.getLoanId(),
                            clientLoanRecord.getClientId(), PaymentType.IMPS.name(), PENNY_DROP,
                            acknowledgement.referenceId, null, null, IN_PROGRESS, BigDecimal.ONE, new Date(),
                            this.systemAppUser);
                    vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                }));
            }
            if(!Objects.isNull(pennyDropAcknowledgements.getFailureList().getBankValidationErrorResponse())) {
                pennyDropAcknowledgements.getFailureList()
                        .getBankValidationErrorResponse()
                        .getListOfAcknowledgement()
                        .forEach(acknowledgement -> records.stream().filter(clientLoanRecord -> clientLoanRecord.getLoanId()
                                .toString().equals(acknowledgement.getExternalId())).findAny().ifPresent(clientLoanRecord -> {
                            clientLoanRecord.getErrorRecords().add(acknowledgement.getReason());
                            clientLoanRecord.getImportDocumentDetails().setReason(acknowledgement.getReason());
                            VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(clientLoanRecord.getLoanId(),
                                    clientLoanRecord.getClientId(), PaymentType.IMPS.name(), PENNY_DROP,
                                    null, null, FAILURE, acknowledgement.getReason(), BigDecimal.ZERO, new Date(),
                                    this.systemAppUser);
                            vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                        }));
            }
            if(!Objects.isNull(pennyDropAcknowledgements.getFailureList().getVpayValidationErrorResponse())) {
                pennyDropAcknowledgements.getFailureList()
                        .getVpayValidationErrorResponse()
                        .stream()
                        .collect(Collectors.groupingBy(VpayValidationErrorResponse::getId))
                        .forEach((id, vpayValidationErrorResponses) -> records.stream().filter(clientLoanRecord -> clientLoanRecord.getLoanId()
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
                        }));
            }
        } catch (Exception exception) {
            LOG.error("Failed while processing penny drop transaction response Error: {} ", exception.getMessage());
        }
    }
    private void validateDuplicateExternalId(List<ClientLoanRecord> clientLoanRecords) {
        clientLoanRecords.stream().filter(data->Optional.ofNullable(data.getExternalId()).isPresent())
                .collect(Collectors.groupingBy(ClientLoanRecord::getExternalId))
                .entrySet().stream().filter(stringListEntry -> stringListEntry.getValue().size() > 1)
                .flatMap(stringListEntry -> stringListEntry.getValue().stream())
                .forEach(clientLoanRecord -> clientLoanRecord.getErrorRecords().add("Duplicate ExternalId: " + clientLoanRecord.getExternalId()));
    }
    private static void apiValidateEmptyRecords(List<ClientLoanRecord> clientLoanRecords) {
        if(clientLoanRecords.isEmpty()) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Records are empty to create new loan",
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
    private List<TransactionRequest> buildPennyDropTransactionRequest(List<ClientLoanRecord> clientLoanRecords) {
        LOG.debug("Building PennyDrop Transaction Request");
        return clientLoanRecords.stream()
                .filter(clientLoanRecord -> !clientLoanRecord.isErrorRecord())
                .map(clientLoanRecord -> {
                    LOG.info("Penny Drop Request [Client: {} Loan: {}, Transaction Type: IMPS]",
                            clientLoanRecord.getClientId(), clientLoanRecord.getLoanId());
                    return bulkLoanTransactionRequestBuilder.buildPennyDropTransactionRequest(clientLoanRecord);
                })
                .toList();
    }

    public void updateAPIErrorAndInfoDetails(ClientLoanRecord clientLoanRecord) {
        boolean status = clientLoanRecord.getErrorRecords().isEmpty();
        String reason = null;
        if (status) {
            reason = Objects.isNull(clientLoanRecord.getInfoRecords()) ? StringUtils.EMPTY
                    : String.join(". ", clientLoanRecord.getInfoRecords());
        } else {
            reason = String.join(". ", clientLoanRecord.getErrorRecords());
        }
        BulkApiResponse bulkApiResponse = new BulkApiResponse(clientLoanRecord.getLoanAccountNo(), clientLoanRecord.getExternalId(), status, reason);
        clientLoanRecord.setBulkApiResponse(bulkApiResponse);
    }
    public List<BulkApiResponse> initiateLoanProcessing(List<ClientLoanRecord> clientLoanRecords, final Long productId) throws IOException {
        apiValidateEmptyRecords(clientLoanRecords);
        this.systemAppUser = context.authenticatedUser();
        apiParseXlsxClientLoanTemplates(clientLoanRecords, productId);
        LOG.info("Received Upload Request { ProductId: {}, Records: {} }", productId, clientLoanRecords.size());
        LoanProduct loanProduct = loanProductRepository.getReferenceById(productId);
        clientLoanRecords.forEach(clientLoanRecord -> process(clientLoanRecord, loanProduct));
        // Initiating penny drop if enabled at product level
        if (loanProduct.getIsPennyDropEnabled() && loanProduct.getIsBankDisbursementEnabled()) {
            initiatePennyDropTransaction(clientLoanRecords,loanProduct);
        }
        return apiUpdateImportDocumentDetailsResponse(clientLoanRecords);
    }
    private List<BulkApiResponse> apiUpdateImportDocumentDetailsResponse(List<ClientLoanRecord> clientLoanRecords) {
        return clientLoanRecords.stream()
                .map(clientLoanRecord -> {
                    BulkApiResponse bulkApiResponse = clientLoanRecord.getBulkApiResponse();
                    bulkApiResponse.setStatus(!clientLoanRecord.isErrorRecord());
                    return bulkApiResponse;
                }).toList();
    }

    public void apiParseXlsxClientLoanTemplates(List<ClientLoanRecord> clientLoanRecords, final Long productId) {
        validateProductId(productId);
        List<String> getDynamicColumns = getDynamicColumns(productId);
        Map<String,Boolean> checkDyanmicColumnsAsMap = new HashMap<>();
        Map<String, Object> dynamicRequiredFields = reteriveMandatoryColumnsMap(productId);
        Map<String, XlsxColumnSchema> columnDetails = xlsxTemplateService.getColumnDetails(XLXSClientLoanTemplate.class);
        dynamicRequiredFieldsUpdate(columnDetails, dynamicRequiredFields);
        addDynamicColums(columnDetails, getDynamicColumns,checkDyanmicColumnsAsMap);
        validateRequiredItems(clientLoanRecords, columnDetails,checkDyanmicColumnsAsMap);
        validateExternalIdIsEmptyOrNull(clientLoanRecords);
        validateDuplicateExternalId(clientLoanRecords);
    }
    private void validateExternalIdIsEmptyOrNull(List<ClientLoanRecord> clientLoanRecords) {
        long[] indexOfData = new long[1];
        String serialNumbers = clientLoanRecords.stream().peek(data-> indexOfData[0]++)
                .filter(ClientLoanRecord::isExternalIdIsEmpty).map(ClientLoanRecord::getSerialNo).map(predicate->Optional.ofNullable(predicate).orElse(indexOfData[0]))
                .map(String::valueOf).collect(Collectors.joining(","));
        if (StringUtils.isNotEmpty(serialNumbers)) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Failed to process because external Id in: " + serialNumbers + " are missing",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
    }
    private void addDynamicColums(Map<String, XlsxColumnSchema> columnDetails, List<String> getDynamicColumns,Map<String,Boolean> checkDyanmicColumnsAsMap) {
        int[] counter = {columnDetails.size()};
        getDynamicColumns.forEach(action -> {
            checkDyanmicColumnsAsMap.put(action,Boolean.TRUE);
            XlsxColumnSchema xlsxColumnSchema = new XlsxColumnSchema();
            columnDetails.put(action, xlsxColumnSchema);
            xlsxColumnSchema.setRequired(Boolean.FALSE);
            xlsxColumnSchema.setHeader(action);
            xlsxColumnSchema.setColumnIndex(counter[0]++);
        });
    }
    private List<String> getDynamicColumns(Long productId) {
        return this.loanProductFeesChargesRepository.getChargesByLoanProductId(productId);
    }
    private void dynamicRequiredFieldsUpdate(Map<String, XlsxColumnSchema> columnDetails,Map<String,Object> dynamicRequiredFields){
        for(Map.Entry<String,Object> dynamicRequiredField: dynamicRequiredFields.entrySet()){
            XlsxColumnSchema columnParameter= columnDetails.get(dynamicRequiredField.getKey().toLowerCase());
            columnParameter.setRequired(Boolean.TRUE);
        }
    }

    private void validateRequiredItems(List<ClientLoanRecord> clientLoanRecords, Map<String, XlsxColumnSchema> columnDetails,Map<String,Boolean> checkDyanmicColumnsAsMap) {
        clientLoanRecords.forEach(clientRecord -> {
            List<String> errorMessagePush = new ArrayList<>();
            clientRecord.setErrorRecords(errorMessagePush);
            xlsxTemplateService.validateAndReturnErrorMessage(columnDetails,errorMessagePush,clientRecord);
            checkChargeNameIsValid(clientRecord,checkDyanmicColumnsAsMap,errorMessagePush);

        });
    }

    private void process(ClientLoanRecord clientLoanRecord, LoanProduct loanProduct) {
        if (!clientLoanRecord.isErrorRecord()) {
            try {
                LOG.debug("Processing - {}", clientLoanRecord);
                bulkLoanProcessorService.process(clientLoanRecord, loanProduct);
            } catch (PlatformApiDataValidationException exception) {
                LOG.error("[RollingBackTransaction] Upload Failed - External Id: {}", clientLoanRecord.getExternalId() + " Exception : " + exception.getMessage());
                exception.getErrors().stream()
                        .map(ApiParameterError::getDeveloperMessage).forEach(clientLoanRecord.getErrorRecords()::add);
            } catch (Exception exception) {
                LOG.error("[RollingBackTransaction] Upload Failed - External Id: {}", clientLoanRecord.getExternalId() + " Exception : " + exception.getMessage());
                clientLoanRecord.getErrorRecords().add(exception.getMessage());
            }
        }
        updateAPIErrorAndInfoDetails(clientLoanRecord);
    }

    private void checkChargeNameIsValid(ClientLoanRecord clientLoanRecord, Map<String, Boolean> checkDyanmicColumnsAsMap, List<String> errorMessage) {
        Map<String, BigDecimal> getCharges = clientLoanRecord.getCharges();
        if(Objects.isNull(getCharges)) return;
        getCharges.keySet().forEach(data -> {
            if (Objects.isNull(checkDyanmicColumnsAsMap.get(data))) {
                errorMessage.add(data.concat(" is not a valid charge name for this product"));
            }
        });
    }
}
