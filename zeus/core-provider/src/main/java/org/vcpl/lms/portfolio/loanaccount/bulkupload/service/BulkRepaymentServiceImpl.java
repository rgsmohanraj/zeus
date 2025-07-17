package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.documentmanagement.api.FileUploadValidator;
import org.vcpl.lms.infrastructure.documentmanagement.domain.DocumentRepository;
import org.vcpl.lms.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.*;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.domain.ImportDocumentDetailsRepository;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.template.XLSXRepaymentTemplate;

import java.util.*;
import java.util.stream.Collectors;
/**
 * Repayment Bulk API development service implementation
 *
 * @author  Yuva Prasanth K
 * @version 1.0
 * @since   2024-02-05
 */
@Service
@RequiredArgsConstructor
public class BulkRepaymentServiceImpl implements BulkRepaymentService {
    private static final Logger LOG = LoggerFactory.getLogger(BulkRepaymentServiceImpl.class);
    private final XlsxTemplateService xlsxTemplateService;
    private final BulkUploadProcessorService bulkLoanProcessorService;
    private final FileUploadValidator fileUploadValidator;
    private final DocumentWritePlatformServiceJpaRepositoryImpl documentWritePlatformService;
    private final PlatformSecurityContext context;
    private final ImportDocumentRepository importDocumentRepository;
    private final ImportDocumentDetailsRepository importDocumentDetailsRepository;
    private final DocumentRepository documentRepository;
    private final BulkUploadDocumentManagerService bulkUploadDocumentManagerServiceImpl;
    @Override
    public List<BulkApiResponse> initiateRepayment(List<RepaymentRecord> repaymentRecords){
        apiValidateEmptyRecords(repaymentRecords);
        apiParseXlsxTemplates(repaymentRecords);
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
            updateAPIErrorAndInfoDetails(repaymentRecord);
        }
        return apiUpdateImportDocumentDetailsResponse(repaymentRecords);
    }
    public void updateAPIErrorAndInfoDetails(RepaymentRecord repaymentRecord) {
        boolean status = repaymentRecord.getErrorRecords().isEmpty();
        String reason = status ? StringUtils.EMPTY : String.join(". ", repaymentRecord.getErrorRecords());
        BulkApiResponse bulkApiResponse = new BulkApiResponse(repaymentRecord.getLoanAccountNo(), repaymentRecord.getExternalId(), status, reason);
        repaymentRecord.setBulkApiResponse(bulkApiResponse);
    }
    private static void apiValidateEmptyRecords(List<RepaymentRecord> repaymentRecords) {
        if(repaymentRecords.isEmpty()) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Records are empty to create repay the amount",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
    }

    private void apiParseXlsxTemplates(List<RepaymentRecord> repaymentRecords) {
        validateRequiredItems(repaymentRecords, xlsxTemplateService.getColumnDetails(XLSXRepaymentTemplate.class));
        validateExternalIdIsEmptyOrNull(repaymentRecords);
    }

    private void validateExternalIdIsEmptyOrNull(List<RepaymentRecord> clientRepaymentRecords) {
        long[] indexOfData = new long[1];
        String serialNumbers = clientRepaymentRecords.stream().peek(data-> indexOfData[0]++)
                .filter(RepaymentRecord::isExternalIdIsEmpty).map(RepaymentRecord::getSerialNo).map(predicate->Optional.ofNullable(predicate).orElse(indexOfData[0]))
                .map(String::valueOf).collect(Collectors.joining(","));
        if (StringUtils.isNotEmpty(serialNumbers)) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Failed to process because external Id in : " + serialNumbers + " are missing",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
    }

    private void validateRequiredItems(List<RepaymentRecord> clientLoanRecords, Map<String, XlsxColumnSchema> columnDetails) {
        clientLoanRecords.forEach(clientRecord -> {
            List<String> errorMessagePush = new ArrayList<>();
            clientRecord.setErrorRecords(errorMessagePush);
            xlsxTemplateService.validateAndReturnErrorMessage(columnDetails,errorMessagePush,clientRecord);
        });
    }
    private List<BulkApiResponse> apiUpdateImportDocumentDetailsResponse(List<RepaymentRecord> repaymentRecords) {
        return repaymentRecords.stream()
                .map(clientLoanRecord -> {
                    BulkApiResponse bulkApiResponse = clientLoanRecord.getBulkApiResponse();
                    bulkApiResponse.setStatus(!clientLoanRecord.isErrorRecord());
                    return bulkApiResponse;
                }).toList();
    }
}
