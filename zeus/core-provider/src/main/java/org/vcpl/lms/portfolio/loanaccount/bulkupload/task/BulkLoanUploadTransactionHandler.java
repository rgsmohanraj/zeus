package org.vcpl.lms.portfolio.loanaccount.bulkupload.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;

import org.slf4j.Logger;
import org.vcpl.client.domain.*;
import org.vcpl.client.gateway.VpayGateway;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.VPayCredentialManager;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil;
import org.vcpl.lms.infrastructure.security.service.TenantDetailsService;
import org.vcpl.lms.portfolio.client.service.ClientReadPlatformServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.BulkUploadConstants;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ClientRequest;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.LoanDisburseRequest;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.mapper.BulkLoanTransactionRequestBuilder;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.VpayTransactionReadWriteServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.data.VpayTransactionDetailsData;
import org.vcpl.lms.portfolio.loanaccount.service.LoanReadPlatformService;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRepository;
import org.vcpl.lms.portfolio.loanproduct.domain.TransactionTypePreference;
import org.vcpl.lms.useradministration.domain.AppUser;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class BulkLoanUploadTransactionHandler implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(BulkLoanUploadTransactionHandler.class);
    private LoanReadPlatformService loanReadPlatformService;
    private ClientReadPlatformServiceImpl clientReadPlatformService;
    private VpayTransactionReadWriteServiceImpl vpayTransactionReadWriteService;
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private BulkLoanTransactionRequestBuilder bulkLoanTransactionRequestBuilder;
    private TenantDetailsService tenantDetailsService;

    private LoanProductRepository loanProductRepository;

    private VPayCredentialManager vPayCredentialManager;
    private final AppUser user;

    public BulkLoanUploadTransactionHandler(final AppUser user, final LoanReadPlatformService loanReadPlatformService,
                                            final ClientReadPlatformServiceImpl clientReadPlatformService,
                                            final VpayTransactionReadWriteServiceImpl vpayTransactionReadWriteService,
                                            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                                            final BulkLoanTransactionRequestBuilder bulkLoanTransactionRequestBuilder,
                                            final TenantDetailsService tenantDetailsService, final LoanProductRepository loanProductRepository, final VPayCredentialManager vPayCredentialManager) {
        this.user = user;
        this.loanReadPlatformService = loanReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.vpayTransactionReadWriteService = vpayTransactionReadWriteService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.bulkLoanTransactionRequestBuilder = bulkLoanTransactionRequestBuilder;
        this.tenantDetailsService = tenantDetailsService;
        this.loanProductRepository = loanProductRepository;
        this.vPayCredentialManager = vPayCredentialManager;
    }
    @Override
    public void run() {
        GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
                authoritiesMapper.mapAuthorities(user.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        ThreadLocalContextUtil.setTenant(tenantDetailsService.loadTenantById("zeus-colending"));
        try {
            /**
             * Constructed below maps (pennyDropTransactionDetailsMap,disbursementTransactionDetailsMap) to reduce the
             * Lookup time for each vPayTransaction once the acknowledgement or transaction response is received
             */

            final Map<String, VpayTransactionDetailsData> pennyDropTransactionDetailsMap =
                    vpayTransactionReadWriteService
                            .getAllIncompleteTransactionForEventType(VPayTransactionConstants.TransactionEventType.PENNY_DROP);

            final Map<String, VpayTransactionDetailsData>  disbursementTransactionDetailsMap =
                    vpayTransactionReadWriteService
                            .getAllIncompleteTransactionForEventType(VPayTransactionConstants.TransactionEventType.DISBURSEMENT);
            if(!pennyDropTransactionDetailsMap.isEmpty() || !disbursementTransactionDetailsMap.isEmpty()) {
                LOG.info("Found [PennyDrop-{}, Disbursement-{}]",pennyDropTransactionDetailsMap.size(), disbursementTransactionDetailsMap.size());
            }

            if(!pennyDropTransactionDetailsMap.isEmpty()) {
                List<VpayTransactionDetailsData> pennyDropTransactions = initiatePennyDropTransactionEnquiry(pennyDropTransactionDetailsMap);
                LOG.debug("Disbursement Request: {}", pennyDropTransactions);
                if (Objects.nonNull(pennyDropTransactions)) {
                    initiateDisbursement(pennyDropTransactionDetailsMap, pennyDropTransactions);
                }
            }
            if(!disbursementTransactionDetailsMap.isEmpty()) {
                initiateDisbursementTransactionEnquiry(disbursementTransactionDetailsMap);
            }
        }catch (PlatformApiDataValidationException exception) {
            LOG.error("Exception : {}",exception.getMessage());
        } catch (Exception exception) {
            LOG.error("Exception : {}",exception.getMessage());
        }
    }

    private void initiateDisbursementTransactionEnquiry(Map<String, VpayTransactionDetailsData> disbursementTransactionDetailsMap) {
        TransactionTracker disbursementTransactionTrackerData = new TransactionTracker();
        List<Object> requests = new ArrayList<>(disbursementTransactionDetailsMap.keySet());
        disbursementTransactionTrackerData.setList(requests);
        TransactionResponse disbursementTransactionResponse = VpayGateway
                .apiCallForTransactionEnquiry(disbursementTransactionTrackerData,vPayCredentialManager.getVpayCredentialWithoutProductName());
        List<VpayTransactionDetailsData> disbursementTransactions = updateResponseToVpayTransaction(disbursementTransactionDetailsMap,
                disbursementTransactionResponse);
        updateDisbursementCompletion(disbursementTransactions);
    }

    private void initiateDisbursement(Map<String, VpayTransactionDetailsData> pennyDropTransactionDetailsMap,
                                      List<VpayTransactionDetailsData> pennyDropTransactions) throws JsonProcessingException {
        Map<Long,List<VpayTransactionDetailsData>> groupByProduct =
                pennyDropTransactions.stream().collect(Collectors.groupingBy(VpayTransactionDetailsData::getPartnerId));
        for (Map.Entry<Long,List<VpayTransactionDetailsData>> partnerLoanTransactions : groupByProduct.entrySet()) {
            List<TransactionRequest> clientBankDetails = buildRealTimeDisbursementRequest(partnerLoanTransactions.getValue());
            if(Objects.nonNull(clientBankDetails) && !clientBankDetails.isEmpty()) {
                TransactionTracker transactionTracker = new TransactionTracker();
                transactionTracker.setBankData(clientBankDetails);
                String disbursementBankAccount = loanProductRepository.getReferenceById(partnerLoanTransactions.getKey())
                        .getDisbursementBankAccount().getLabel();
                LOG.debug("TransactionResponse :  {} ", new ObjectMapper().writeValueAsString(transactionTracker));
                TransactionResponse disbursementAcknowledgements = VpayGateway.apiCallForDisbursement(transactionTracker,
                        vPayCredentialManager.getVpayCredentialWithProductName(disbursementBankAccount));
                LOG.debug("TransactionResponse :  {} ", new ObjectMapper().writeValueAsString(disbursementAcknowledgements));
                saveDisbursementAcknowledgement(pennyDropTransactionDetailsMap, disbursementAcknowledgements);
            }
        }
    }

    @NotNull
    private List<VpayTransactionDetailsData> initiatePennyDropTransactionEnquiry(Map<String, VpayTransactionDetailsData> pennyDropTransactionDetailsMap) throws JsonProcessingException {
        TransactionTracker pennyDropTransactionTrackerData = new TransactionTracker();
        List<Object> requests = new ArrayList<>(pennyDropTransactionDetailsMap.keySet());
        pennyDropTransactionTrackerData.setList(requests);
        // Transaction Enquiry For EventType: PENNY_DROP
        TransactionResponse transactionResponse = VpayGateway
                .apiCallForTransactionEnquiry(pennyDropTransactionTrackerData,vPayCredentialManager.getVpayCredentialWithoutProductName());
        LOG.debug("Transaction Enquiry Response : {}", new ObjectMapper().writeValueAsString(transactionResponse));
        return updateResponseToVpayTransaction(pennyDropTransactionDetailsMap,
                transactionResponse);
    }

    private void saveDisbursementAcknowledgement(Map<String, VpayTransactionDetailsData> pennyDropTransactionDetailsMap,
                                                 TransactionResponse disbursementAcknowledgements) {
        updateSuccessAcknowledgement(pennyDropTransactionDetailsMap, disbursementAcknowledgements);
        updateFailureAcknowledgement(pennyDropTransactionDetailsMap, disbursementAcknowledgements);
        updateVpayValidationFailure(disbursementAcknowledgements);
    }

    private void updateVpayValidationFailure(TransactionResponse disbursementAcknowledgements) {
        if(!Objects.isNull(disbursementAcknowledgements.getFailureList().getVpayValidationErrorResponse())) {
            disbursementAcknowledgements.getFailureList()
                    .getVpayValidationErrorResponse()
                    .stream()
                    .collect(Collectors.groupingBy(VpayValidationErrorResponse::getId))
                    .forEach((id, vpayValidationErrorResponses) -> {
                        VpayTransactionDetailsData vPayTransactionDetails = vpayTransactionReadWriteService
                                .getByLoanIdAndEventType(Long.valueOf(id), "PENNY_DROP");
                        TransactionTypePreference transactionTypePreference = loanReadPlatformService
                                .getTransactionTypePreferenceByLoanId(vPayTransactionDetails.getLoanId());
                        VpayTransactionDetailsData disbursementTransactionAck = new VpayTransactionDetailsData(0l,vPayTransactionDetails.getLoanId(),
                                vPayTransactionDetails.getClientId(), transactionTypePreference.toString(),
                                "DISBURSEMENT", null, null, "FAILURE", null, null, null,
                                user.getId(),null);
                        vpayValidationErrorResponses.forEach(vpayValidationErrorResponse -> disbursementTransactionAck
                                .setReason(vpayValidationErrorResponse.getReason()));
                        this.vpayTransactionReadWriteService.createTransactionAcknowledgement(disbursementTransactionAck);
                    });
        }
    }

    private void updateFailureAcknowledgement(Map<String, VpayTransactionDetailsData> pennyDropTransactionDetailsMap,
                                              TransactionResponse disbursementAcknowledgements) {
        if(!Objects.isNull(disbursementAcknowledgements.getFailureList()) && !Objects.isNull(disbursementAcknowledgements.getFailureList().getBankValidationErrorResponse())) {
            disbursementAcknowledgements.getFailureList()
                    .getBankValidationErrorResponse()
                    .getListOfAcknowledgement()
                    .forEach(acknowledgement -> {
                        VpayTransactionDetailsData vPayTransactionDetails = vpayTransactionReadWriteService
                                .getByLoanIdAndEventType(Long.valueOf(acknowledgement.getExternalId()), "PENNY_DROP");
                        TransactionTypePreference transactionTypePreference = loanReadPlatformService
                                .getTransactionTypePreferenceByLoanId(vPayTransactionDetails.getLoanId());
                        VpayTransactionDetailsData disbursementTransactionAck = new VpayTransactionDetailsData(0l, vPayTransactionDetails.getLoanId(),
                                vPayTransactionDetails.getClientId(), transactionTypePreference.toString(),
                                "DISBURSEMENT", null, null, "FAILURE", acknowledgement.getReason(),
                                null, new Date(), user.getId(),null);
                        LOG.debug("Disbursement Acknowledgement {} ", disbursementTransactionAck);
                        this.vpayTransactionReadWriteService.createTransactionAcknowledgement(disbursementTransactionAck);
                    });
            }
    }

    private void updateSuccessAcknowledgement(Map<String, VpayTransactionDetailsData> pennyDropTransactionDetailsMap,
                                              TransactionResponse disbursementAcknowledgements) {
        if(!Objects.isNull(disbursementAcknowledgements.getResponseList())) {
            disbursementAcknowledgements.getResponseList().getListOfAcknowledgement()
                    .forEach(acknowledgement -> {
                        VpayTransactionDetailsData vpayTransactionDetailsData = vpayTransactionReadWriteService
                                .getByLoanIdAndEventType(Long.valueOf(acknowledgement.getExternalId()),
                                        VPayTransactionConstants.TransactionEventType.PENNY_DROP);
                        /*boolean isDisbursementAlreadyTriggered = Objects.isNull(vpayTransactionReadWriteService
                                .getByLoanIdAndEventType(vpayTransactionDetailsData.getLoanId(),
                                        VPayTransactionConstants.TransactionEventType.DISBURSEMENT));
                        if (isDisbursementAlreadyTriggered) {*/
                            TransactionTypePreference transactionTypePreference = loanReadPlatformService
                                    .getTransactionTypePreferenceByLoanId(vpayTransactionDetailsData.getLoanId());
                            VpayTransactionDetailsData disbursementTransactionAck = VpayTransactionDetailsData.disbursementTransaction(
                                    vpayTransactionDetailsData.getLoanId(), vpayTransactionDetailsData.getClientId(),
                                    transactionTypePreference.toString(), acknowledgement.getReferenceId(),user.getId());
                            this.vpayTransactionReadWriteService.createTransactionAcknowledgement(disbursementTransactionAck);
                        /*}*/
                    });
        }
    }

    private List<TransactionRequest> buildRealTimeDisbursementRequest(List<VpayTransactionDetailsData> pennyDropTransactions) {
        return pennyDropTransactions.stream()
                // Ignoring Success Transaction and Transaction Created By Users Manually
                .filter(vPayTransactionDetails -> Objects.nonNull(vPayTransactionDetails.getAction())
                        && vPayTransactionDetails.getAction().equals("SUCCESS") && vPayTransactionDetails.getCreatedBy().equals(user.getId()))
                .map(vPayTransactionDetails -> {
                    ClientRequest clientRequest =  clientReadPlatformService.getImportClientRequestById(vPayTransactionDetails.getClientId());
                    LOG.info("Disbursement Request [Client: {} Loan: {}, Transaction Type: {}]",
                            vPayTransactionDetails.getClientId(), vPayTransactionDetails.getLoanId(), vPayTransactionDetails.getTransactionType());
                    return bulkLoanTransactionRequestBuilder.buildDistbursementTransactionRequest(clientRequest, vPayTransactionDetails.getLoanId());
                }).toList();
    }



    private List<VpayTransactionDetailsData> updateResponseToVpayTransaction(final Map<String, VpayTransactionDetailsData> vPayTransactionDetailsMap,
                                                                         final TransactionResponse pennyDropResponses) {
        updateTransactionSuccessResponse(vPayTransactionDetailsMap, pennyDropResponses);
        updateVpayTransactionError(vPayTransactionDetailsMap, pennyDropResponses);
        updateBankValidationError(vPayTransactionDetailsMap, pennyDropResponses);
        return vpayTransactionReadWriteService.updateTransactionResponse(vPayTransactionDetailsMap.values());
    }

    private void updateBankValidationError(Map<String, VpayTransactionDetailsData> vPayTransactionDetailsMap,
                                           TransactionResponse pennyDropResponses) {
        if(Objects.isNull(pennyDropResponses.getFailureList())
                || Objects.isNull(pennyDropResponses.getFailureList().getBankValidationErrorResponse())
                || Objects.isNull(pennyDropResponses.getFailureList().getBankValidationErrorResponse().getTransactionEnquiry())) {
            return;
        }
        pennyDropResponses.getFailureList()
                .getBankValidationErrorResponse()
                .getTransactionEnquiry()
                .forEach(federalResponse -> {
                    VpayTransactionDetailsData vpayTransactionDetailsData =
                            vPayTransactionDetailsMap.get(federalResponse.getReferenceId());
                    if (Objects.nonNull(vpayTransactionDetailsData)) {
                        vpayTransactionDetailsData.setAction(federalResponse.getResponseAction());
                        vpayTransactionDetailsData.setReason(federalResponse.getResponseReason());
                        if(Objects.nonNull(federalResponse.getTranAmount())) {
                            vpayTransactionDetailsData.setTransactionAmount(new BigDecimal(federalResponse.getTranAmount()));
                        }
                        vpayTransactionDetailsData.setTransactionDate(new Date());
                        this.vpayTransactionReadWriteService.updateTransactionResponse(vpayTransactionDetailsData);
                    }
                });
    }

    private void updateVpayTransactionError(Map<String, VpayTransactionDetailsData> vPayTransactionDetailsMap, TransactionResponse pennyDropResponses) {
        if (Objects.isNull(pennyDropResponses.getFailureList())
                || Objects.isNull(pennyDropResponses.getFailureList().getVpayValidationErrorResponse())){
            return;
        }
        pennyDropResponses.getFailureList()
                .getVpayValidationErrorResponse()
                .forEach(vpayValidationErrorResponse -> {
                    VpayTransactionDetailsData vpayTransactionDetailsData = vPayTransactionDetailsMap
                            .get(vpayValidationErrorResponse.getId());
                    vpayTransactionDetailsData.setAction("FAILURE");
                    vpayTransactionDetailsData.setReason(vpayValidationErrorResponse.getReason());
                    this.vpayTransactionReadWriteService.updateTransactionResponse(vpayTransactionDetailsData);
                });
    }

    private void updateTransactionSuccessResponse(Map<String, VpayTransactionDetailsData> vPayTransactionDetailsMap, TransactionResponse pennyDropResponses) {
        pennyDropResponses.getResponseList()
                .getTransactionEnquiry()
                .forEach(federalResponse -> {
                    VpayTransactionDetailsData vPayTransactionDetails = vPayTransactionDetailsMap
                            .get(federalResponse.getReferenceId());
                    vPayTransactionDetails.setTransactionAmount(new BigDecimal(federalResponse.getTranAmount()));
                    vPayTransactionDetails.setUtr(federalResponse.getUtr());
                    vPayTransactionDetails.setReason(federalResponse.getResponseReason());
                    vPayTransactionDetails.setAction(federalResponse.getResponseAction());
                    vPayTransactionDetails.setTransactionAmount(new BigDecimal(federalResponse.getTranAmount()));
                    vPayTransactionDetails.setTransactionDate(new Date());
                });
    }

    private void updateDisbursementCompletion(List<VpayTransactionDetailsData> disbursementTransactions) {
        disbursementTransactions.stream()
                .filter(vpayTransactionDetailsData -> vpayTransactionDetailsData.getAction().equals("SUCCESS"))
                .forEach(vPayTransactionDetails -> {
                    try {
                        LoanDisburseRequest loanDisburseRequest = new LoanDisburseRequest();
                        loanDisburseRequest.setTransactionAmount(loanReadPlatformService
                                .getPrincipleAmountForLoanId(vPayTransactionDetails.getLoanId()));
                        String date = loanReadPlatformService
                                .getExceptedDisbursementDateByLoanId(vPayTransactionDetails.getLoanId());
                        loanDisburseRequest.setActualDisbursementDate(date);
                        loanDisburseRequest.setPaymentTypeId(1);
                        loanDisburseRequest.setDateFormat(BulkUploadConstants.DB_DATE_FORMAT);
                        loanDisburseRequest.setLocale(BulkUploadConstants.LOCALE);
                        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                                .disburseLoanApplication(vPayTransactionDetails.getLoanId())
                                .withJson(new Gson().toJson(loanDisburseRequest))
                                .build();
                        LOG.info("Disbursed Loan Id - {}", commandRequest.getLoanId());
                        commandsSourceWritePlatformService.logCommandSourceWithSystemUser(commandRequest);
                    } catch (PlatformApiDataValidationException exception) {
                        LOG.error("Exception: {} ", exception.getErrors().stream().map(ApiParameterError::getDefaultUserMessage)
                                .collect(Collectors.joining(",")));
                    } catch (Exception ex) {
                        LOG.error("Exception : {}", ex.getMessage());
                    }
                });
    }
}
