package org.vcpl.lms.portfolio.loanaccount.bulkupload.mapper;

import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.client.domain.BeneficiaryDetails;
import org.vcpl.client.domain.FederalBankRequest;
import org.vcpl.client.domain.TransactionRequest;
import org.vcpl.client.enumeration.AccType;
import org.vcpl.client.enumeration.NotificationFlag;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.vcpl.lms.portfolio.client.data.ClientData;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ClientLoanRecord;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ClientRequest;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.service.BulkLoanUploadServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.data.LoanAccountData;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.service.LoanReadPlatformServiceImpl;
import org.vcpl.lms.portfolio.loanproduct.domain.TransactionTypePreference;

@Service
public class BulkLoanTransactionRequestBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(BulkLoanTransactionRequestBuilder.class);

    @Autowired  private CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired private LoanReadPlatformServiceImpl loanReadPlatformService;

    public TransactionRequest buildPennyDropTransactionRequest(final ClientLoanRecord clientLoanRecord) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setBankName(VPayTransactionConstants.Bank.FEDERAL);
        FederalBankRequest federalBankRequest = new FederalBankRequest();
        federalBankRequest.setExternalId(clientLoanRecord.getLoanId().toString());
        federalBankRequest.setAmount(String.valueOf(1));
        federalBankRequest.setAppCode(VPayTransactionConstants.ZEUS_APP_CODE);
        federalBankRequest.setPaymentType("imps");
        federalBankRequest.setRemarks(VPayTransactionConstants.TransactionEventType.PENNY_DROP); // Mandatory
        federalBankRequest.setSenderData("Initializing penny-drop");
        BeneficiaryDetails beneficiaryDetails = new BeneficiaryDetails();
        beneficiaryDetails.setName(clientLoanRecord.getBeneficiaryName()); // Mandatory
        beneficiaryDetails.setAccountNumber(String.valueOf(clientLoanRecord.getBeneficiaryAccountNo())); // Mandatory
        beneficiaryDetails.setNotificationFlag(NotificationFlag.BOTH.name()); // Mandatory
        beneficiaryDetails.setIfscCode(clientLoanRecord.getIfsc()); // Mandatory
        beneficiaryDetails.setMobileNumber(clientLoanRecord.getMobileNumber()); // Mandatory
        beneficiaryDetails.setEmailId(clientLoanRecord.getEmailAddress()); // Mandatory
        federalBankRequest.setBeneficiaryDetails(beneficiaryDetails);
        transactionRequest.setFederal(federalBankRequest);
        LOG.info("Penny Drop Request [Client: {} Loan: {}, Transaction Type: IMPS]",
                clientLoanRecord.getClientId(), clientLoanRecord.getLoanId());
        return transactionRequest;
    }


    public TransactionRequest buildPennyDropTransactionRequest(final Client client, Long loanId) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setBankName(VPayTransactionConstants.Bank.FEDERAL);
        FederalBankRequest federalBankRequest = new FederalBankRequest();
        federalBankRequest.setExternalId(String.valueOf(loanId));
        federalBankRequest.setAmount(String.valueOf(1));
        federalBankRequest.setAppCode(VPayTransactionConstants.ZEUS_APP_CODE);
        federalBankRequest.setPaymentType("imps");
        federalBankRequest.setRemarks(VPayTransactionConstants.TransactionEventType.PENNY_DROP); // Mandatory
        federalBankRequest.setSenderData("Initializing penny-drop");
        BeneficiaryDetails beneficiaryDetails = new BeneficiaryDetails();
        beneficiaryDetails.setName(client.getBeneficiaryName()); // Mandatory
        beneficiaryDetails.setAccountNumber(String.valueOf(client.getBeneficiaryAccountNumber())); // Mandatory
        beneficiaryDetails.setNotificationFlag(NotificationFlag.BOTH.name()); // Mandatory
        beneficiaryDetails.setIfscCode(client.getIfscCode()); // Mandatory
        beneficiaryDetails.setMobileNumber(client.getMobileNo()); // Mandatory
        beneficiaryDetails.setEmailId(client.getEmailAddress()); // Mandatory
        federalBankRequest.setBeneficiaryDetails(beneficiaryDetails);
        transactionRequest.setFederal(federalBankRequest);
        LOG.info("Penny Drop Request [Client: {} Loan: {}, Transaction Type: IMPS]",
                client.getId(), client.getId());
        return transactionRequest;
    }

    public TransactionRequest buildDistbursementTransactionRequest(ClientRequest clientRequest, Long loanId) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setBankName(VPayTransactionConstants.Bank.FEDERAL);
        FederalBankRequest federalBankRequest = new FederalBankRequest();
        federalBankRequest.setExternalId(String.valueOf(loanId));
        federalBankRequest.setAmount(String.valueOf(loanReadPlatformService.getNetDisbursementAmountByLoanId(loanId).setScale(2)));
        federalBankRequest.setAppCode(VPayTransactionConstants.ZEUS_APP_CODE);
        federalBankRequest.setRemarks(VPayTransactionConstants.TransactionEventType.DISBURSEMENT); // Mandatory
        federalBankRequest.setSenderData("Amount Disbursed");

        BeneficiaryDetails beneficiaryDetails = new BeneficiaryDetails();
        beneficiaryDetails.setName(clientRequest.getBeneficiaryName()); // Mandatory
        beneficiaryDetails.setAccountNumber(String.valueOf(clientRequest.getBeneficiaryAccountNumber())); // Mandatory
        beneficiaryDetails.setNotificationFlag(NotificationFlag.BOTH.name()); // Mandatory
        beneficiaryDetails.setIfscCode(clientRequest.getIfscCode()); // Mandatory
        beneficiaryDetails.setMobileNumber(clientRequest.getMobileNo()); // Mandatory
        beneficiaryDetails.setEmailId(clientRequest.getEmailAddress()); // Mandatory
        TransactionTypePreference transactionTypePreference = loanReadPlatformService.getTransactionTypePreferenceByLoanId(loanId);
        federalBankRequest.setPaymentType(transactionTypePreference.toString().toLowerCase());
        if (!transactionTypePreference.toString().equals("IMPS")) {
            beneficiaryDetails.setAccountType(getBankAccountType(codeValueReadPlatformService.getCodeValueById(Long
                    .valueOf(clientRequest.getAccountTypeId())))); // NOT NEEDED FOR IMPS
            beneficiaryDetails.setAddress(clientRequest.getAddress()); // Mandatory [RTGS/NEFT]
            federalBankRequest.setAlternativePayments("N");
        }
        federalBankRequest.setBeneficiaryDetails(beneficiaryDetails);
        transactionRequest.setFederal(federalBankRequest);
        return transactionRequest;
    }

    public TransactionRequest buildDistbursementTransactionRequest(Client clientRequest, Long loanId) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setBankName(VPayTransactionConstants.Bank.FEDERAL);
        FederalBankRequest federalBankRequest = new FederalBankRequest();
        federalBankRequest.setExternalId(String.valueOf(loanId));
        federalBankRequest.setAmount(String.valueOf(loanReadPlatformService.getNetDisbursementAmountByLoanId(loanId).setScale(2)));
        federalBankRequest.setAppCode(VPayTransactionConstants.ZEUS_APP_CODE);
        federalBankRequest.setRemarks(VPayTransactionConstants.TransactionEventType.DISBURSEMENT); // Mandatory
        federalBankRequest.setSenderData("Amount Disbursed");

        BeneficiaryDetails beneficiaryDetails = new BeneficiaryDetails();
        beneficiaryDetails.setName(clientRequest.getBeneficiaryName()); // Mandatory
        beneficiaryDetails.setAccountNumber(String.valueOf(clientRequest.getBeneficiaryAccountNumber())); // Mandatory
        beneficiaryDetails.setNotificationFlag(NotificationFlag.BOTH.name()); // Mandatory
        beneficiaryDetails.setIfscCode(clientRequest.getIfscCode()); // Mandatory
        beneficiaryDetails.setMobileNumber(clientRequest.getMobileNo()); // Mandatory
        beneficiaryDetails.setEmailId(clientRequest.getEmailAddress()); // Mandatory
        TransactionTypePreference transactionTypePreference = loanReadPlatformService.getTransactionTypePreferenceByLoanId(loanId);
        federalBankRequest.setPaymentType(transactionTypePreference.toString().toLowerCase());
        if (!transactionTypePreference.toString().equals("IMPS")) {
            StringBuilder addressBuilder = new StringBuilder(clientRequest.getCity()).append(" ").append(clientRequest.getState().getLabel());
            beneficiaryDetails.setAccountType(getBankAccountType(clientRequest.getAccountType().getLabel())); // NOT NEEDED FOR IMPS
            beneficiaryDetails.setAddress(addressBuilder.toString()); // MancodeValueReadPlatformServicedatory [RTGS/NEFT]
            federalBankRequest.setAlternativePayments("N");
        }
        federalBankRequest.setBeneficiaryDetails(beneficiaryDetails);
        transactionRequest.setFederal(federalBankRequest);
        LOG.info("Disbursement Request [Client: {} Loan: {}, Transaction Type: {}]",clientRequest.getId(), loanId, transactionTypePreference);
        return transactionRequest;
    }

    private String getBankAccountType(String bankAccountType) {
        switch (bankAccountType) {
            case "SBA - Savings Account" -> {
                return AccType.SBA.getBankClassPath();
            }
            case "CAA - Current Account" -> {
                return AccType.CAA.getBankClassPath();
            }
            case "CCA - Cash Credit Account" -> {
                return AccType.CCA.getBankClassPath();
            }
            // case "LAA - Loan Account" -> AccType.LAA.getBankClassPath();
            case "ODA - Over Draft Account" -> {
                return AccType.ODA.getBankClassPath();
            }
            case "OAB - Office Account Basic" -> {
                return AccType.OAB.getBankClassPath();
            }
            case "NRE - Non resident account" -> {
                return AccType.NRE.getBankClassPath();
            }
        }
        return null;
    }
}
