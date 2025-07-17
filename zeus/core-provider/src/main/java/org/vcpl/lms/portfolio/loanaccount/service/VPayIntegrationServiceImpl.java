package org.vcpl.lms.portfolio.loanaccount.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.client.domain.*;
import org.vcpl.client.gateway.VpayGateway;
import org.vcpl.lms.infrastructure.core.data.VPayCredentialManager;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.exception.DisbursementFailedException;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.exception.PennyDropFailedException;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.mapper.BulkLoanTransactionRequestBuilder;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.VpayTransactionReadWriteServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepository;
import org.vcpl.lms.portfolio.loanaccount.domain.VPayTransactionDetails;
import org.vcpl.lms.portfolio.loanaccount.domain.VPayTransactionDetailsRepository;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.TransactionTypePreference;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants.TransactionEventType.*;
import static org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants.Action.*;

@Service
public class VPayIntegrationServiceImpl implements VPayIntegrationService {
    private static final Logger LOG = LoggerFactory.getLogger(VPayIntegrationServiceImpl.class);
    @Autowired private VPayTransactionDetailsRepository vPayTransactionDetailsRepository;
    @Autowired private BulkLoanTransactionRequestBuilder bulkLoanTransactionRequestBuilder;
    @Autowired private VpayTransactionReadWriteServiceImpl vpayTransactionReadWriteService;
    @Autowired private LoanRepository loanRepository;
    @Autowired private PlatformSecurityContext context;
    @Autowired private LoanReadPlatformServiceImpl loanReadPlatformService;

    @Autowired private VPayCredentialManager vPayCredentialManager;
    @Override
    public void pennydrop(final Long loanId) throws JsonProcessingException {
        checkIsPennyDropOptionEnabled(loanId);
        Loan loan = loanRepository.getReferenceById(loanId);
        Client client = loan.client();
        checkIsPennyDropTransactionExist(loanId);
        TransactionRequest transactionRequest = bulkLoanTransactionRequestBuilder.buildPennyDropTransactionRequest(client, loanId);
        TransactionTracker pennyDisburseTracker = new TransactionTracker();
        List<TransactionRequest> transactionRequestList = new ArrayList<>();
        transactionRequestList.add(transactionRequest);
        pennyDisburseTracker.setBankData(transactionRequestList);
        String disbursementBankAccount = loan.getLoanProduct().getDisbursementBankAccount().getLabel();
        TransactionResponse transactionResponse = VpayGateway.apiCallForDisbursement(pennyDisburseTracker, vPayCredentialManager.getVpayCredentialWithProductName(disbursementBankAccount));
        if(Objects.isNull(transactionResponse)) {
            LOG.error("Transaction Response is empty for pennyDrop request for Loan Id: {} ", loanId);
            throw new PennyDropFailedException("Empty Transaction Response From VPay.");
        }
        LOG.debug("Response {}", new ObjectMapper().writeValueAsString(transactionResponse));
        updateSuccessFullPennyDropResponse(loanId, client, transactionResponse);
        updateBankFailureResponseForPennyDrop(loanId, client, transactionResponse);
        updateVpayValidationFailureResponseForPennyDrop(loanId, client, transactionResponse);
    }

    private void checkIsPennyDropOptionEnabled(Long loanId) {
        LoanProduct loanProduct = loanRepository.getReferenceById(loanId).getLoanProduct();
        if(!loanProduct.getIsPennyDropEnabled()) {
            LOG.error("Initiate Penny Drop Option is disabled for Loan Product : {} ", loanProduct.getName());
            throw new PennyDropFailedException("Initiate Penny Drop Option is disabled for Loan Product : " + loanProduct.getName());
        }
    }

    @Override
    public void disburse(final Long loanId) {
        checkIsBankDisbursementOptionEnabled(loanId);
        List<VPayTransactionDetails> pennyDropTransactionDetails = isPennyDropInitiatedForLoan(loanId);
        isDisbursementInitiatedForLoan(loanId);
        // updateExceptionForPennyDropTransaction(pennyDropTransactionDetails);
        Loan loan = loanRepository.getReferenceById(loanId);
        Client client = loan.client();
        TransactionRequest request = bulkLoanTransactionRequestBuilder.buildDistbursementTransactionRequest(client, loanId);
        List<TransactionRequest> transactionRequest = new ArrayList<>();
        transactionRequest.add(request);
        TransactionTracker transactionTracker = new TransactionTracker();
        transactionTracker.setBankData(transactionRequest);
        String disbursementBankAccount = loan.getLoanProduct().getDisbursementBankAccount().getLabel();
        TransactionResponse disbursementAcknowledgements = VpayGateway.apiCallForDisbursement(transactionTracker, vPayCredentialManager.getVpayCredentialWithProductName(disbursementBankAccount));
        updateSuccessfullDisbursementResponse(loanId, client, disbursementAcknowledgements);
        updateVpayValidationFailedForDisbursement(loanId, client, disbursementAcknowledgements);
        updateBankFailureResponseForDisbursement(loanId, client, disbursementAcknowledgements);
    }

    private void checkIsBankDisbursementOptionEnabled(Long loanId) {
        LoanProduct loanProduct = loanRepository.getReferenceById(loanId).getLoanProduct();
        if(!loanProduct.getIsBankDisbursementEnabled()) {
            LOG.error("Initiate Disbursement Option is disabled for Loan Product : {} ", loanProduct.getName());
            throw new PennyDropFailedException("Initiate Disbursement Option is disabled for Loan Product : " + loanProduct.getName());
        }
    }

    private void updateVpayValidationFailureResponseForPennyDrop(Long loanId, Client client, TransactionResponse transactionResponse) {
        if(!Objects.isNull(transactionResponse.getFailureList().getVpayValidationErrorResponse())) {
            transactionResponse.getFailureList()
                        .getVpayValidationErrorResponse()
                        .stream()
                        .collect(Collectors.groupingBy(VpayValidationErrorResponse::getId))
                        .forEach((id, vpayValidationErrorResponses) -> {
                                    VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(loanId,
                                            client.getId(), "IMPS", PENNY_DROP, null, null, FAILURE,
                                            vpayValidationErrorResponses.stream().map(VpayValidationErrorResponse::getReason).collect(Collectors.joining(". ")),
                                            BigDecimal.ZERO, new Date(), context.authenticatedUser());
                            vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                            throw new PennyDropFailedException(vPayTransactionDetails.getReason());
                        });
        }
    }

    private void updateBankFailureResponseForPennyDrop(Long loanId, Client client, TransactionResponse transactionResponse) {
        if(!Objects.isNull(transactionResponse.getFailureList().getBankValidationErrorResponse())) {
            transactionResponse.getFailureList()
                        .getBankValidationErrorResponse()
                        .getListOfAcknowledgement()
                        .forEach(acknowledgement -> {
                            VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(loanId,
                                    client.getId(), "IMPS", PENNY_DROP,
                                    null, null, "FAILURE", acknowledgement.getReason(), BigDecimal.ZERO, new Date(),
                                    context.authenticatedUser());
                            vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                            throw new PennyDropFailedException(acknowledgement.getReason());
                        });
        }
    }

    private void updateSuccessFullPennyDropResponse(Long loanId, Client client, TransactionResponse transactionResponse) {
        if(!Objects.isNull(transactionResponse.getResponseList())) {
            transactionResponse.getResponseList().getListOfAcknowledgement().forEach(acknowledgement -> {
                    VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(loanId,
                            client.getId(), "IMPS", PENNY_DROP,
                            acknowledgement.referenceId, null, IN_PROGRESS, null, BigDecimal.ONE, new Date(), context.authenticatedUser());
                    vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                });
        }
    }

    private void checkIsPennyDropTransactionExist(Long loanId) {
        List<VPayTransactionDetails> loanVpayTransactions = vPayTransactionDetailsRepository.getByLoanIdAndEventType(loanId,PENNY_DROP);
        for (VPayTransactionDetails vPayTransactionDetails : loanVpayTransactions) {
            if(vPayTransactionDetails.getAction().equals(SUCCESS)) {
                throw new PennyDropFailedException("Penny Drop is already completed successfully");
            } else if(vPayTransactionDetails.getAction().equals(SUSPECT)) {
                throw new PennyDropFailedException("Penny Drop is already initiated and it is under 'SUSPECT' state");
            } else if(vPayTransactionDetails.getAction().equals(IN_PROGRESS) || vPayTransactionDetails.getAction().equals(PROCESSED)) {
                throw new PennyDropFailedException("Penny Drop is already In Progress");
            }
        }
    }

    private void isDisbursementInitiatedForLoan(Long loanId) {
        List<VPayTransactionDetails> loanVpayTransactions = vPayTransactionDetailsRepository.getByLoanIdAndEventType(loanId,DISBURSEMENT);
        for (VPayTransactionDetails vPayTransactionDetails : loanVpayTransactions) {
            if(vPayTransactionDetails.getAction().equals(SUCCESS)) {
                throw new PennyDropFailedException("Disbursement is already completed successfully");
            } else if(vPayTransactionDetails.getAction().equals(SUSPECT)) {
                throw new PennyDropFailedException("Disbursement is already initiated and it is under 'SUSPECT' state");
            } else if(vPayTransactionDetails.getAction().equals(IN_PROGRESS) || vPayTransactionDetails.getAction().equals(PROCESSED)) {
                throw new PennyDropFailedException("Disbursement is already In Progress");
            }
        }
    }

    private void updateExceptionForPennyDropTransaction(List<VPayTransactionDetails> pennyDropTransactionDetails) {
        pennyDropTransactionDetails.stream()
                .filter(vPayTransactionDetails ->
                        vPayTransactionDetails.getAction().equals(FAILURE)
                                || vPayTransactionDetails.getAction().equals(IN_PROGRESS)
                                || vPayTransactionDetails.getAction().equals(SUSPECT))
                .forEach(vPayTransactionDetails -> {
                    vPayTransactionDetails.setAction("Exempted");
                    vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                });
    }

    @NotNull
    private List<VPayTransactionDetails> isPennyDropInitiatedForLoan(Long loanId) {
        List<VPayTransactionDetails> pennyDropTransactionDetails = vPayTransactionDetailsRepository
                .getByLoanIdAndEventType(loanId, PENNY_DROP);
        if (pennyDropTransactionDetails.isEmpty()) {
            throw new DisbursementFailedException("Please initiate Penny Drop before initiating Disbursement");
        }

        if(pennyDropTransactionDetails.stream()
                .anyMatch(vPayTransactionDetails -> vPayTransactionDetails.getAction().equals(IN_PROGRESS) || vPayTransactionDetails.getAction().equals(PROCESSED))){
            throw new DisbursementFailedException("Penny Drop is InProgress");
        }

        if(pennyDropTransactionDetails.stream()
                .anyMatch(vPayTransactionDetails -> vPayTransactionDetails.getAction().equals(SUSPECT))){
            throw new DisbursementFailedException("Penny Drop is In Suspect. Status will be updated soon.");
        }

        return pennyDropTransactionDetails;
    }

    private void updateBankFailureResponseForDisbursement(Long loanId, Client client, TransactionResponse disbursementAcknowledgements) {
        if(!Objects.isNull(disbursementAcknowledgements.getFailureList().getVpayValidationErrorResponse())) {
            disbursementAcknowledgements.getFailureList()
                    .getVpayValidationErrorResponse()
                    .stream()
                    .collect(Collectors.groupingBy(VpayValidationErrorResponse::getId))
                    .forEach((id, vpayValidationErrorResponses) -> {
                        TransactionTypePreference transactionTypePreference = loanReadPlatformService.getTransactionTypePreferenceByLoanId(loanId);
                        VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(loanId,
                                client.getId(), transactionTypePreference.toString(), DISBURSEMENT,
                                null, null, FAILURE, null, null, new Date(), context.authenticatedUser());
                        vpayValidationErrorResponses.forEach(vpayValidationErrorResponse ->
                            vPayTransactionDetails.setReason(vpayValidationErrorResponse.getReason())
                        );
                        this.vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                        throw new DisbursementFailedException(vPayTransactionDetails.getReason());
                    });
        }
    }

    private void updateVpayValidationFailedForDisbursement(Long loanId, Client client, TransactionResponse disbursementAcknowledgements) {
        if(!Objects.isNull(disbursementAcknowledgements.getFailureList()) && !Objects.isNull(disbursementAcknowledgements.getFailureList().getBankValidationErrorResponse())) {
            disbursementAcknowledgements.getFailureList()
                    .getBankValidationErrorResponse()
                    .getListOfAcknowledgement()
                    .forEach(acknowledgement -> {
                        TransactionTypePreference transactionTypePreference = loanReadPlatformService.getTransactionTypePreferenceByLoanId(loanId);
                        VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(loanId,
                                client.getId(), transactionTypePreference.toString(), DISBURSEMENT,
                                acknowledgement.getReferenceId(), null, FAILURE, acknowledgement.getReason(), null,
                                new Date(), context.authenticatedUser());
                        this.vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                         throw new DisbursementFailedException(acknowledgement.getReason());
                    });
        }
    }

    private void updateSuccessfullDisbursementResponse(Long loanId, Client client, TransactionResponse disbursementAcknowledgements) {
        if(!Objects.isNull(disbursementAcknowledgements.getResponseList())) {
            disbursementAcknowledgements.getResponseList().getListOfAcknowledgement()
                    .forEach(acknowledgement -> {
                        /*boolean isDisbursementAlreadyTriggered = Objects.isNull(vpayTransactionReadWriteService
                                .getByLoanIdAndEventType(loanId, VPayTransactionConstants.TransactionEventType.DISBURSEMENT));
                        if (isDisbursementAlreadyTriggered) {*/
                            TransactionTypePreference transactionTypePreference = loanReadPlatformService.getTransactionTypePreferenceByLoanId(loanId);
                            VPayTransactionDetails vPayTransactionDetails = new VPayTransactionDetails(loanId,
                                    client.getId(), transactionTypePreference.toString(), DISBURSEMENT,
                                    acknowledgement.getReferenceId(), null, IN_PROGRESS, null, null, new Date(),
                                    context.authenticatedUser());
                            vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                            this.vPayTransactionDetailsRepository.saveAndFlush(vPayTransactionDetails);
                        // }
                    });
        }
    }

    @Override
    public void pingVpay(){
        Optional<VPayTransactionDetails> lastTransaction = this.vPayTransactionDetailsRepository.findFirstByOrderByTransactionDate();
        if(lastTransaction.isEmpty()) {
            LOG.info("There is no transaction records.");
            return;
        }
        TransactionTracker pennyDropTransactionTrackerData = new TransactionTracker();
        if(Objects.isNull(lastTransaction.get().getVpayReferenceId())){
            LOG.info("There is no Reference Id for latest transaction enquiry.");
            return;
        }
        pennyDropTransactionTrackerData.setList(List.of(lastTransaction.get().getVpayReferenceId()));
        LOG.info("Scheduler started and calling Vpay for transaction enquiry");
        VpayGateway.apiCallForTransactionEnquiry(pennyDropTransactionTrackerData,vPayCredentialManager.getVpayCredentialWithoutProductName());
    }
}
