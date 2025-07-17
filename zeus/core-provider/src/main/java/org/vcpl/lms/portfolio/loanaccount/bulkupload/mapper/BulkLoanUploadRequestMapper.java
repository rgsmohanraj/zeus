package org.vcpl.lms.portfolio.loanaccount.bulkupload.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepository;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.organisation.office.domain.Office;
import org.vcpl.lms.organisation.office.domain.OfficeRepository;
import org.vcpl.lms.portfolio.client.api.ClientApiConstants;
import org.vcpl.lms.portfolio.client.domain.LegalForm;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.BulkUploadConstants;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.*;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesCharges;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRelatedDetail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BulkLoanUploadRequestMapper {
    @Autowired
    private CodeValueRepository codeValueRepository;
    Map<String,Office> officeDropDownCache = null;
    Map<String,CodeValue> codeValueDropDownCache = null;
    @Autowired
    private OfficeRepository officeRepository;
    public ClientRequest mapToClientRequest(final ClientLoanRecord clientLoanRecord) {
        storingDropDownValueToCatch();
        ClientRequest clientRequestData = new ClientRequest();
        Office officeRepositoryValue = officeDropDownCache.get(clientLoanRecord.getEntity());
        checkingNullAndThrowException(officeRepositoryValue, ClientApiConstants.clientEntityName);
        clientRequestData.setOfficeId(officeRepositoryValue.getId().intValue());
        clientRequestData.setLegalFormId(LegalForm.valueOf(clientLoanRecord.getApplicantType()).getValue());
        clientRequestData.setFirstname(clientLoanRecord.getFirstName());
        clientRequestData.setLastname(clientLoanRecord.getLastName());
        if(Objects.nonNull(clientLoanRecord.getMiddleName()))
            clientRequestData.setMiddlename(clientLoanRecord.getMiddleName());
        clientRequestData.setAge(clientLoanRecord.getAge());
        if (Objects.nonNull(clientLoanRecord.getGender())) {
            CodeValue genderCodeValue = codeValueDropDownCache.get(clientLoanRecord.getGender());
            checkingNullAndThrowException(genderCodeValue, ClientApiConstants.GENDER);
            clientRequestData.setGenderId(genderCodeValue.getId().intValue());
        }
        clientRequestData.setDateOfBirth(clientLoanRecord.getDob().format(DateTimeFormatter.ISO_LOCAL_DATE));
        clientRequestData.setActive(Boolean.TRUE);
        clientRequestData.setPan(clientLoanRecord.getPan());
        clientRequestData.setAadhaar(clientLoanRecord.getAadhaar());
        clientRequestData.setAddress(clientLoanRecord.getAddress());
        clientRequestData.setMobileNo(clientLoanRecord.getMobileNumber());
        if (Objects.nonNull(clientLoanRecord.getEmailAddress()))
            clientRequestData.setEmailAddress(clientLoanRecord.getEmailAddress());
        if (Objects.nonNull(clientLoanRecord.getCity())) {
            clientRequestData.setCity(clientLoanRecord.getCity());
        }
        CodeValue stateCodeValue = codeValueDropDownCache.get(clientLoanRecord.getState());
        checkingNullAndThrowException(stateCodeValue, ClientApiConstants.STATE);
        clientRequestData.setStateId(stateCodeValue.getId().intValue());
        clientRequestData.setActivationDate(clientLoanRecord.getActivationDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        clientRequestData.setSubmittedOnDate(clientLoanRecord.getSubmittedOn().format(DateTimeFormatter.ISO_LOCAL_DATE));
        /**
         * Bank Information Mapping
         */
        clientRequestData.setBeneficiaryAccountNumber(clientLoanRecord.getBeneficiaryAccountNo());
        clientRequestData.setBeneficiaryName(clientLoanRecord.getBeneficiaryName());
        clientRequestData.setIfscCode(clientLoanRecord.getIfsc());
        clientRequestData.setMicrCode(clientLoanRecord.getMicrCode());
        clientRequestData.setSwiftCode(clientLoanRecord.getSwiftCode());
        clientRequestData.setBranch(clientLoanRecord.getBranch());
        clientRequestData.setVoterId(clientLoanRecord.getVoterId());
        clientRequestData.setPassportNumber(clientLoanRecord.getPassportNumber());
        clientRequestData.setDrivingLicense(clientLoanRecord.getDrivingLicense());
        clientRequestData.setPincode(clientLoanRecord.getPincode());
        //clientRequestData.setRationCardNumber(clientLoanRecord.getRationCardNumber());
//        if (Objects.nonNull(clientLoanRecord.getRepaymentMode())) {
//            CodeValue repaymentCodeValue = codeValueRepository.findByCodeNameAndLabel(ClientApiConstants.REPAYMENTMODE,
//                    clientLoanRecord.getRepaymentMode());
//            clientRequestData.setRepaymentModeId(repaymentCodeValue.getId().intValue());
//        }
        CodeValue accountTypeCodeValue = codeValueDropDownCache.get(clientLoanRecord.getAccountType());
        checkingNullAndThrowException(accountTypeCodeValue, ClientApiConstants.ACCOUNTTYPE);
        clientRequestData.setAccountTypeId(accountTypeCodeValue.getId().intValue());
        clientRequestData.setDateFormat(BulkUploadConstants.DB_DATE_FORMAT);
        clientRequestData.setLocale(BulkUploadConstants.LOCALE);
        return clientRequestData;
    }

    private void storingDropDownValueToCatch() {
        if (!Objects.isNull(codeValueDropDownCache) && !Objects.isNull(officeDropDownCache))
            return;
        List<Office> officeList =
                officeRepository.findAll();
        officeDropDownCache = officeList.stream().collect(Collectors.toMap(Office::getName, data -> data));
        List<CodeValue> codeValueList =
                codeValueRepository.findAll();
        codeValueDropDownCache = codeValueList.stream().collect(Collectors.toMap(CodeValue::getLabel, data -> data));
    }
    public void resetTheCatchToDefaultValue(){
        officeDropDownCache = null;
        codeValueDropDownCache = null;
    }
    private void checkingNullAndThrowException(Object value, String notMatchedName){
        if (Optional.ofNullable(value).isEmpty()) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("",
                    "'" + notMatchedName + "' value is not a valid",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
    }
    public LoanRequest mapToLoanRequest(final ClientLoanRecord clientLoanRecord,final LoanProduct loanProduct) {
        LoanProductRelatedDetail loanProductRelatedDetail = loanProduct.getLoanProductRelatedDetail();
        Set<LoanProductFeesCharges> charges = loanProduct.getLoanProductFeesCharges();
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setClientId(clientLoanRecord.getClientId().intValue());
        loanRequest.setExternalId(clientLoanRecord.getExternalId());
        loanRequest.setProductId(loanProduct.getId().intValue());
        loanRequest.setSubmittedOnDate(clientLoanRecord.getLoanSubmittedOn().format(DateTimeFormatter.ISO_LOCAL_DATE));
        loanRequest.setExpectedDisbursementDate(clientLoanRecord.getDisbursementDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        BigDecimal principle = clientLoanRecord.getPrinciple();
        if(principle.remainder(BigDecimal.ONE).doubleValue() > 0) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.loan.principle.amount.cannot.be.in.decimal",
                    "Loan Amount cannot be in decimal",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
        loanRequest.setPrincipal(clientLoanRecord.getPrinciple());
        loanRequest.setLoanTermFrequency(clientLoanRecord.getLoanTerm());
        loanRequest.setLoanTermFrequencyType(PeriodFrequencyType.MONTHS.getValue());
        loanRequest.setNumberOfRepayments(clientLoanRecord.getLoanTerm());
        if(Objects.nonNull(clientLoanRecord.getFirstRepaymentOn())) {
            loanRequest.setRepaymentsStartingFromDate(clientLoanRecord
                    .getFirstRepaymentOn().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        loanRequest.setRepaymentEvery(loanProductRelatedDetail.getRepayEvery());
        loanRequest.setRepaymentFrequencyType(loanProductRelatedDetail.getRepaymentPeriodFrequencyType().getValue());
        loanRequest.setRepaymentFrequencyNthDayType(clientLoanRecord.getDueDay());
        loanRequest.setInterestChargedFromDate(clientLoanRecord.getDisbursementDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE));
        loanRequest.setInterestRatePerPeriod(clientLoanRecord.getInterestRate().doubleValue());
        loanRequest.setInterestType(loanProductRelatedDetail.getInterestMethod().getValue());
        loanRequest.setAmortizationType(loanProductRelatedDetail.getAmortizationMethod().getValue());
        loanRequest.setTransactionProcessingStrategyId(loanProduct.getRepaymentStrategy().toData().id().intValue());
        loanRequest.setInterestCalculationPeriodType(loanProductRelatedDetail.getInterestCalculationPeriodMethod().getValue());
        loanRequest.setLoanType("individual");
        loanRequest.setDateFormat(BulkUploadConstants.DB_DATE_FORMAT);
        loanRequest.setLocale(BulkUploadConstants.LOCALE);

        List<LoanCharge> loanCharges = new ArrayList<>();
        List<LoanCharge> overdueCharges = new ArrayList<>();
        List<LoanCharge> foreclosureCharges = new ArrayList<>();
        List<LoanCharge> bounceCharge = new ArrayList<>();
        clientLoanRecord.getCharges().forEach((name,value) -> {
                    if (!value.equals(BigDecimal.valueOf(0.0))) {
                        charges.stream().filter(charge -> charge.getCharge().getName().equals(name))
                                .findAny()
                                .ifPresent(charge -> {
                                    if (charge.getCharge().getChargeTimeType() == 9) {
                                        // Adding Overdue charge
                                        overdueCharges.add(new LoanCharge(charge.getCharge().getId().intValue(), value,
                                                charge.getSelfShare(),
                                                charge.getPartnerShare()));
                                    } else if (charge.getCharge().getChargeTimeType() == 17) {
                                        // Adding Foreclosure charge
                                        foreclosureCharges.add(new LoanCharge(charge.getCharge().getId().intValue(), value,
                                                charge.getSelfShare(),
                                                charge.getPartnerShare()));
                                    } else if (charge.getCharge().getChargeTimeType() == 19) {
                                        //Adding Bounce Charge
                                        bounceCharge.add(new LoanCharge(charge.getCharge().getId().intValue(), value,
                                                charge.getSelfShare(),
                                                charge.getPartnerShare()));
                                    } else if (charge.getCharge().getChargeTimeType() != 18) {
                                        // Adding charges other than adhoc
                                        loanCharges.add(new LoanCharge(charge.getCharge().getId().intValue(), value,
                                                charge.getSelfShare(),
                                                charge.getPartnerShare()));
                                    }
                                });
                    }
                }
            );
        loanRequest.setOverdueCharges(overdueCharges);
        loanRequest.setForeclosureCharges(foreclosureCharges);
        loanRequest.setCharges(loanCharges);
        loanRequest.setBounceCharge(bounceCharge);
        return loanRequest;
    }

    public LoanApproveRequest mapToLoanApproveRequest(final ClientLoanRecord clientLoanRecord) {
        LoanApproveRequest loanApproveRequest = new LoanApproveRequest();
        loanApproveRequest.setApprovedLoanAmount(clientLoanRecord.getPrinciple());
        loanApproveRequest.setApprovedOnDate(clientLoanRecord.getDisbursementDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE));
        loanApproveRequest.setExpectedDisbursementDate(clientLoanRecord.getDisbursementDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE));
        loanApproveRequest.setDateFormat(BulkUploadConstants.DB_DATE_FORMAT);
        loanApproveRequest.setLocale(BulkUploadConstants.LOCALE);
        return loanApproveRequest;
    }

    public LoanDisburseRequest mapToLoanDisburseRequest(final ClientLoanRecord clientLoanRecord){
        LoanDisburseRequest loanDisburseRequest = new LoanDisburseRequest();
        loanDisburseRequest.setActualDisbursementDate(clientLoanRecord.getDisbursementDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE));
        loanDisburseRequest.setTransactionAmount(clientLoanRecord.getPrinciple());
        loanDisburseRequest.setPaymentTypeId(1);
        loanDisburseRequest.setDateFormat(BulkUploadConstants.DB_DATE_FORMAT);
        loanDisburseRequest.setLocale(BulkUploadConstants.LOCALE);
        return loanDisburseRequest;
    }

    public RepaymentRequest mapToRepaymentRequest(final RepaymentRecord repaymentRecord){
        storingDropDownValueToCatch();
        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setInstallmentNumber(repaymentRecord.getInstallment()!=null ? repaymentRecord.getInstallment():0);
        repaymentRequest.setTransactionAmount(repaymentRecord.getTransactionAmount());
        repaymentRequest.setTransactionDate(repaymentRecord.getTransactionDate().
                format(DateTimeFormatter.ISO_LOCAL_DATE));
        repaymentRequest.setDateFormat(BulkUploadConstants.DB_DATE_FORMAT);
        repaymentRequest.setLocale(BulkUploadConstants.LOCALE);
        if (Objects.nonNull(repaymentRecord.getReceiptReferenceNumber())) {
            repaymentRequest.setReceiptReferenceNumber(repaymentRecord.getReceiptReferenceNumber());
        }
        if (Objects.nonNull(repaymentRecord.getPartnerTransferUTR())) {
            repaymentRequest.setPartnerTransferUtr(repaymentRecord.getPartnerTransferUTR());
        }
        if (Objects.nonNull(repaymentRecord.getCoolingOffSpecific().get(BulkUploadConstants.INTEREST_WAIVER))) {
            repaymentRequest.setInterestWaiver((BigDecimal) repaymentRecord.getCoolingOffSpecific().get(BulkUploadConstants.INTEREST_WAIVER));
        }
        if (Objects.nonNull(repaymentRecord.getCoolingOffSpecific().get(BulkUploadConstants.COOLING_OF_DATE))) {
            repaymentRequest.setCoolingOffDate(repaymentRecord.getCoolingOffSpecific().get(BulkUploadConstants.COOLING_OF_DATE).toString());
        }
        if (Objects.nonNull(repaymentRecord.getPartnerTransferDate())) {
            repaymentRequest.setPartnerTransferDate(repaymentRecord.getPartnerTransferDate().
                    format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (Objects.nonNull(repaymentRecord.getRepaymentMode())) {
            CodeValue repaymentCodeValue = codeValueDropDownCache.get(repaymentRecord.getRepaymentMode());
            checkingNullAndThrowException(repaymentCodeValue, ClientApiConstants.REPAYMENTMODE);
            repaymentRequest.setRepaymentMode(repaymentCodeValue.getId().intValue());
        }
        if (Objects.nonNull(repaymentRecord.getCollectionFlag())) {
            repaymentRequest.setCollectionFlag(repaymentRecord.getCollectionFlag());
        }
        return repaymentRequest;
    }

    public ChargeRepaymentRequest mapToChargeTransactionRequest(ChargeRepaymentRecord chargeRepaymentRecord) {
        ChargeRepaymentRequest chargeRepaymentRequest = new ChargeRepaymentRequest();
        chargeRepaymentRequest.setLoanId(chargeRepaymentRecord.getLoanId());
        chargeRepaymentRequest.setLoanAccountNo(chargeRepaymentRecord.getLoanAccount());
        chargeRepaymentRequest.setTransactionDate(chargeRepaymentRecord.getTransactionDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        chargeRepaymentRequest.setExternalId(chargeRepaymentRecord.getExternalId());
        /*if(Objects.nonNull(chargeRepaymentRecord.getInstallmentNumber())){
            chargeRepaymentRequest.setInstallment(chargeRepaymentRecord.getInstallmentNumber());
        }*/
        if(Objects.nonNull(chargeRepaymentRecord.getReceiptReferenceNumber())){
            chargeRepaymentRequest.setReceiptReferenceNumber(chargeRepaymentRecord.getReceiptReferenceNumber());
        }
        if(Objects.nonNull(chargeRepaymentRecord.getRepaymentMode())) {
            chargeRepaymentRequest.setRepaymentMode(chargeRepaymentRecord.getRepaymentMode());
        }
        if(Objects.nonNull(chargeRepaymentRecord.getPartnerTransferUtr())) {
            chargeRepaymentRequest.setPartnerTransferUtr(chargeRepaymentRecord.getPartnerTransferUtr());
        }
        if(Objects.nonNull(chargeRepaymentRecord.getPartnerTransferDate())) {
            chargeRepaymentRequest.setPartnerTransferDate(chargeRepaymentRecord.getPartnerTransferDate()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        chargeRepaymentRequest.setDateFormat(BulkUploadConstants.DB_DATE_FORMAT);
        chargeRepaymentRequest.setLocale(BulkUploadConstants.LOCALE);
        return chargeRepaymentRequest;
    }

}
