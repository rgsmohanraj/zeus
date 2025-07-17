package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.compare.ComparableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.client.domain.ClientRepositoryWrapper;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.BulkUploadConstants;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.*;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.mapper.BulkLoanUploadRequestMapper;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeTransactionRequest;
import org.vcpl.lms.portfolio.loanaccount.data.LoanTransactionData;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepository;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionType;
import org.vcpl.lms.portfolio.loanaccount.service.LoanReadPlatformService;
import org.vcpl.lms.portfolio.loanproduct.domain.Dedupe;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.service.DedupeServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BulkUploadProcessorServiceImpl implements BulkUploadProcessorService {
    private static final Logger LOG = LoggerFactory.getLogger(BulkUploadProcessorServiceImpl.class);
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final LoanRepository loanRepository;
    private final BulkLoanUploadRequestMapper bulkLoanUploadRequestMapper;
    private final DedupeServiceImpl dedupeServiceImpl;
    private final LoanReadPlatformService loanReadPlatformService;


    @Override
    @Transactional
    public void process(ClientLoanRecord clientLoanRecord, LoanProduct loanProduct) {
        createClient(clientLoanRecord,loanProduct);
        createLoan(clientLoanRecord, loanProduct);
        approveLoan(clientLoanRecord);
        if(!loanProduct.getIsBankDisbursementEnabled() && !loanProduct.getIsPennyDropEnabled()) {
            disburseLoan(clientLoanRecord);
        }
    }

    @Override
    public void createClient(final ClientLoanRecord clientLoanRecord, final LoanProduct loanProduct) {
        Client client = dedupeClient(clientLoanRecord, Dedupe.fromInt(loanProduct.getDedupeType()));
        if (!Objects.isNull(client)) {
            LOG.info("Client exist. Skipping client creation");
            List<String> infoRecords = new ArrayList<>();
            infoRecords.add("Client '" + client.getDisplayName() + "' already exist with given " + Dedupe.fromInt(loanProduct.getDedupeType()).toString() + ". Skipping client creation");
            clientLoanRecord.setInfoRecords(infoRecords);
            List<ApiParameterError> errors = new ArrayList<>();
            validateBeneficiaryAccountNo (client.getBeneficiaryAccountNumber(),clientLoanRecord,errors);
            validateIfscCode (client.getIfscCode(),clientLoanRecord,errors);
            clientLoanRecord.setClientId(client.getId());
            return;
        }
        ClientRequest clientRequestData = bulkLoanUploadRequestMapper.mapToClientRequest(clientLoanRecord);
        LOG.debug("- Client Creation Request {}", clientRequestData);
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .createClient()
                .withJson(new Gson().toJson(clientRequestData))
                .build();
        CommandProcessingResult clientCreationResult = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        LOG.debug("- Client Created {}",clientCreationResult.getClientId());
        clientLoanRecord.setClientId(clientCreationResult.getClientId());
    }

    private Client dedupeClient(ClientLoanRecord clientLoanRecord, Dedupe dedupeType) {
        switch (dedupeType) {
            case PAN:
                return dedupeServiceImpl.fetchUsingPanNumber(clientLoanRecord.getPan());
            case AADHAAR :
                return dedupeServiceImpl.fetchUsingAadhaar(clientLoanRecord.getAadhaar());
            default:
                return null;
        }
    }

    private void validateIfscCode(String ifscCode, ClientLoanRecord clientLoanRecord, List<ApiParameterError> errors) {
        if(!ifscCode.equals(clientLoanRecord.getIfsc())) {
            errors.add(ApiParameterError.generalError("validation.msg.client.ifsc.mismatch",
                    "Client Already Exist. Entered IFSC doesn't match with the existing IFSC in the system",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
    }

    private void validateBeneficiaryAccountNo(String beneficiaryAccountNumber,ClientLoanRecord clientLoanRecord, List<ApiParameterError> errors) {
        if(!beneficiaryAccountNumber
                .equals(clientLoanRecord.getBeneficiaryAccountNo())) {
            errors.add(ApiParameterError.generalError("validation.msg.client.account_no.mismatch",
                    "Client Already Exist. Entered Account Number doesn't match with the existing Account Number in the system",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
    }

    @Override
    public void createLoan(final ClientLoanRecord clientLoanRecord, final LoanProduct loanProduct) {
        LoanRequest loanRequest = bulkLoanUploadRequestMapper.mapToLoanRequest(clientLoanRecord, loanProduct);
        LOG.debug("- Loan Creation Request: {} ", loanRequest);
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .createLoanApplication()
                .withJson(new Gson().toJson(loanRequest))
                .build();
        CommandProcessingResult loanCreationResult = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        LOG.debug("- Loan Created {}",loanCreationResult.getLoanId());
        clientLoanRecord.setLoanAccountNo(loanRepository.getReferenceById(loanCreationResult.getLoanId()).getAccountNumber());
        clientLoanRecord.setLoanId(loanCreationResult.getLoanId());
    }

    @Override
    public void approveLoan(final ClientLoanRecord clientLoanRecord) {
        LoanApproveRequest loanApproveRequest = bulkLoanUploadRequestMapper.mapToLoanApproveRequest(clientLoanRecord);
        LOG.debug(" - Loan Creation Request: {} ", loanApproveRequest);
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .approveLoanApplication(clientLoanRecord.getLoanId())
                .withJson(new Gson().toJson(loanApproveRequest))
                .build();
        LOG.debug("- Approved Loan Id - {}",commandRequest.getLoanId());
        commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @Override
    public void disburseLoan(ClientLoanRecord clientLoanRecord) {
        LoanDisburseRequest loanDisburseRequest = bulkLoanUploadRequestMapper.mapToLoanDisburseRequest(clientLoanRecord);
        LOG.debug("- Disbursement Request - {}", loanDisburseRequest);
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .disburseLoanApplication(clientLoanRecord.getLoanId())
                .withJson(new Gson().toJson(loanDisburseRequest))
                .build();
        LOG.debug("- Disbursed Loan Id - {}",commandRequest.getLoanId());
        commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @Override
    @Transactional
    public void repayLoan(final RepaymentRecord repaymentRecord) {
        Loan loan = loanRepository.getByAccountNumber(repaymentRecord.getLoanAccountNo()).orElseThrow(() -> {
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.accountnumber.not.exist",
                    "Loan Account Number does not exist - " + repaymentRecord.getLoanAccountNo(),
                    null, null);
            return new PlatformApiDataValidationException(List.of(error));
        });

        if(!loanRepository.getByIdAndExternalId(loan.getId(),repaymentRecord.getExternalId()).isPresent()) {
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.externalId.mismatch",
                    "Given External Id is not linked to Loan Id: " + repaymentRecord.getLoanId(),
                    null, null);
            throw new PlatformApiDataValidationException(List.of(error));
        }
        RepaymentRequest repaymentRequest = bulkLoanUploadRequestMapper
                .mapToRepaymentRequest(repaymentRecord);
        repaymentRecord.setCollectionFlag(repaymentRecord.getCollectionFlag().toUpperCase());
        validateForCoolingOff(repaymentRecord);
        Double foreClosureAmount=foreClosureCalculation(loan.getId(),repaymentRecord.getTransactionDate(),repaymentRecord.getCollectionFlag());
        if(repaymentRecord.getCollectionFlag().equals("FORECLOSURE") && repaymentRecord.getTransactionAmount().doubleValue()!=foreClosureAmount) {
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.foreclosure.amount.not.matched",
                    "Transaction amount is mismatching with actual Foreclosure amount " +foreClosureAmount,
                    null, null);
            throw new PlatformApiDataValidationException(List.of(error));
        }

        LOG.debug("- Repayment Request - {}", repaymentRequest);
        CommandWrapper commandRequest = switch (repaymentRecord.getCollectionFlag()) {
            case "COOLING OFF" -> new CommandWrapperBuilder()
                    .loanCoolingOff(loan.getId())
                    .withJson(new Gson().toJson(repaymentRequest))
                    .build();
            case "COLLECTION" -> new CommandWrapperBuilder()
                    .loanRepaymentTransaction(loan.getId())
                    .withJson(new Gson().toJson(repaymentRequest))
                    .build();
            case "FORECLOSURE" -> new CommandWrapperBuilder()
                    .loanForeclosure(loan.getId())
                    .withJson(new Gson().toJson(repaymentRequest))
                    .build();
            default -> throw new PlatformApiDataValidationException(List.of(ApiParameterError.parameterError("error.msg.loan.collectionFlag.not.match",
                    "Collection flag should be COOLING OFF or COLLECTION or FORECLOSURE " ,
                    null, null)));
        };
        LOG.debug("- Collection Flag - {}", repaymentRecord.getCollectionFlag());
        CommandProcessingResult commandProcessingResult = commandsSourceWritePlatformService
                .logCommandSource(commandRequest);
        repaymentRecord.setClientId(commandProcessingResult.getClientId());
    }

    @Override
    public void repayCharge(final ChargeRepaymentRecord record,final Map<String, Charge> chargeMap) {
        Loan loan = loanRepository.getByAccountNumber(record.getLoanAccount()).orElseThrow(() -> {
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.accountnumber.not.exist",
                    "Loan Account Number does not exist - " + record.getLoanAccount(),
                    null, null);
            return new PlatformApiDataValidationException(List.of(error));
        });
        record.setLoanId(loan.getId());
        if(!loanRepository.getByIdAndExternalId(loan.getId(),record.getExternalId()).isPresent()) {
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.externalId.mismatch",
                    "Given External Id is not linked to Loan Id: " + record.getLoanId(),
                    null, null);
            throw new PlatformApiDataValidationException(List.of(error));
        }

        if(record.getCharges().isEmpty()) {
            record.getErrorRecords().add("Both Paid and Waived Off is empty");
            return;
        }

        if (!record.getCharges().entrySet().stream()
                .anyMatch(entry -> ComparableUtils.is(entry.getValue()).greaterThan(BigDecimal.ZERO))) {
            record.getErrorRecords().add("Paid and Waived Off column cannot be zero or less than zero");
            return;
        }

        ChargeRepaymentRequest chargeRepaymentRequest = bulkLoanUploadRequestMapper.mapToChargeTransactionRequest(record);

        record.getCharges().forEach((chargeName, amount) -> {
            if(chargeName.contains("Paid") && ComparableUtils.is(amount).greaterThan(BigDecimal.ZERO)) {
                Charge charge = chargeMap.get(chargeName.substring(0, chargeName.length()-4).trim());
                Long chargeId = charge.getId();
                chargeRepaymentRequest.setChargeId(chargeId);
                LOG.info("{}-{}:{}",chargeName, chargeId, amount);
                chargeRepaymentRequest.setAmount(amount);
                if(charge.isAdhocCharge()) {
                    processAdhocCharge(chargeRepaymentRequest);
                } else {
                    payCharge(chargeRepaymentRequest);
                }
            } else if(chargeName.contains("Waived Off") && ComparableUtils.is(amount).greaterThan(BigDecimal.ZERO)) {
                Charge charge = chargeMap.get(chargeName.substring(0, chargeName.length()-10).trim());
                Long chargeId = charge.getId();
                chargeRepaymentRequest.setChargeId(chargeId);
                chargeRepaymentRequest.setAmount(amount);
                LOG.info("{}-{}:{}",chargeName, chargeId, amount);
                if(charge.isAdhocCharge()) {
                    processAdhocCharge(chargeRepaymentRequest);
                } else {
                    waiveCharge(chargeRepaymentRequest);
                }
            }
        });
    }

    private void waiveCharge(ChargeRepaymentRequest chargeRepaymentRequest) {
        LOG.debug("Waive Loan Charge Request - {}", chargeRepaymentRequest);
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .waiveLoanCharge(chargeRepaymentRequest.getLoanId(), chargeRepaymentRequest.getChargeId())
                .withJson(new Gson().toJson(chargeRepaymentRequest))
                .build();
        commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private void payCharge(ChargeRepaymentRequest chargeRepaymentRequest) {
        LOG.debug("Pay Loan Charge Request - {}", chargeRepaymentRequest);
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .payLoanCharge(chargeRepaymentRequest.getLoanId(), chargeRepaymentRequest.getChargeId())
                .withJson(new Gson().toJson(chargeRepaymentRequest))
                .build();
        commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private void processAdhocCharge(ChargeRepaymentRequest chargeRepaymentRequest) {
        LOG.debug("-Pay/Waive Adhoc Loan Charge Request - {}", chargeRepaymentRequest);
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .createLoanCharge(chargeRepaymentRequest.getLoanId())
                .withJson(new Gson().toJson(chargeRepaymentRequest))
                .build();
        commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    public Double foreClosureCalculation(Long loanId, LocalDate transactionDate, String collectionFlagType){
        double foreCloseAmount=0d;
        if(collectionFlagType.equals("FORECLOSURE")) {
            LoanTransactionData transactionData = loanReadPlatformService.retrieveLoanForeclosureTemplate(LoanTransactionType.FORECLOSURE, loanId, transactionDate);
            foreCloseAmount =
                    transactionData.getPrincipalPortion().doubleValue() + transactionData.getInterestPortion().doubleValue() - transactionData.getAdvanceAmount().doubleValue()
                            + transactionData.getFeeChargesPortion().doubleValue() + transactionData.getPenaltyChargesPortion().doubleValue() + transactionData.getBounceCharge().doubleValue();
            LOG.info("Transaction amount is : {}" , foreCloseAmount);
        }
        return foreCloseAmount;
    }
    private void validateForCoolingOff(RepaymentRecord repaymentRecord){
        LoanProduct product=this.loanRepository.getByAccountNumber(repaymentRecord.getLoanAccountNo()).get().getLoanProduct();
        Boolean coolOffApplicability=product.getProductCollectionConfig().getCoolingOffApplicability();
        if(repaymentRecord.getCollectionFlag().equals("COOLING OFF") && repaymentRecord.getCoolingOffSpecific().get(BulkUploadConstants.COOLING_OF_DATE)==null) {
            String defaultMessage=Boolean.TRUE.equals(coolOffApplicability)? "Cooling off date is mandatory for collection flag Cooling Off " : " Cooling off is not enabled for this loan ";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.coolOffDate.not.applicable",
                    defaultMessage ,
                    null, null);
            throw new PlatformApiDataValidationException(List.of(error));
        }
    }

}
