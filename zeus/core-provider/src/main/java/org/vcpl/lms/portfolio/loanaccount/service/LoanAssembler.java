/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.loanaccount.service;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.configuration.domain.ConfigurationDomainService;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.organisation.holiday.domain.Holiday;
import org.vcpl.lms.organisation.holiday.domain.HolidayRepository;
import org.vcpl.lms.organisation.holiday.domain.HolidayStatusType;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.organisation.office.service.OfficeReadPlatformServiceImpl;
import org.vcpl.lms.organisation.staff.domain.Staff;
import org.vcpl.lms.organisation.staff.domain.StaffRepository;
import org.vcpl.lms.organisation.staff.exception.StaffNotFoundException;
import org.vcpl.lms.organisation.staff.exception.StaffRoleException;
import org.vcpl.lms.organisation.workingdays.domain.WorkingDays;
import org.vcpl.lms.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.vcpl.lms.portfolio.accountdetails.domain.AccountType;
import org.vcpl.lms.portfolio.accountdetails.service.AccountEnumerations;
import org.vcpl.lms.portfolio.address.service.AddressReadPlatformService;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.charge.domain.ChargeRepository;
import org.vcpl.lms.portfolio.charge.service.ChargeReadPlatformService;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.client.domain.ClientRepositoryWrapper;
import org.vcpl.lms.portfolio.client.exception.ClientNotActiveException;
import org.vcpl.lms.portfolio.collateralmanagement.domain.CollateralManagementDomain;
import org.vcpl.lms.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import org.vcpl.lms.portfolio.fund.domain.Fund;
import org.vcpl.lms.portfolio.fund.domain.FundRepository;
import org.vcpl.lms.portfolio.fund.exception.FundNotFoundException;
import org.vcpl.lms.portfolio.group.domain.Group;
import org.vcpl.lms.portfolio.group.domain.GroupRepository;
import org.vcpl.lms.portfolio.group.exception.ClientNotInGroupException;
import org.vcpl.lms.portfolio.group.exception.GroupNotActiveException;
import org.vcpl.lms.portfolio.group.exception.GroupNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.data.GstData;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.exception.ExceedingTrancheCountException;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidAmountOfCollaterals;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.exception.MultiDisbursementDataNotAllowedException;
import org.vcpl.lms.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.vcpl.lms.portfolio.loanproduct.domain.*;
import org.vcpl.lms.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.vcpl.lms.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.vcpl.lms.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.vcpl.lms.portfolio.rate.domain.Rate;
import org.vcpl.lms.portfolio.rate.service.RateAssembler;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanProductRepository loanProductRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final FundRepository fundRepository;
    private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
    private final StaffRepository staffRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanCollateralAssembler collateralAssembler;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final HolidayRepository holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanUtilService loanUtilService;
    private final RateAssembler rateAssembler;

    private final LoanProductFeesChargesRepositoryWrapper loanProductFeesChargesRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final AddressReadPlatformService addressReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final OfficeReadPlatformServiceImpl officeReadPlatformServiceImpl;
    private final LoanProductFeesChargesRepository loanProductFeesChargesRepository;
	private final GstService gstServiceimpl;


    @Autowired
    public LoanAssembler(final FromJsonHelper fromApiJsonHelper, final LoanRepositoryWrapper loanRepository,
                         final LoanProductRepository loanProductRepository, final ClientRepositoryWrapper clientRepository,
                         final GroupRepository groupRepository, final FundRepository fundRepository,
                         final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
                         final StaffRepository staffRepository, final CodeValueRepositoryWrapper codeValueRepository,
                         final LoanScheduleAssembler loanScheduleAssembler, final LoanChargeAssembler loanChargeAssembler,
                         final LoanCollateralAssembler collateralAssembler, final LoanSummaryWrapper loanSummaryWrapper,
                         final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
                         final HolidayRepository holidayRepository, final ConfigurationDomainService configurationDomainService,
                         final WorkingDaysRepositoryWrapper workingDaysRepository, final LoanUtilService loanUtilService,
                         RateAssembler rateAssembler, final LoanProductFeesChargesRepositoryWrapper loanProductFeesChargesRepositoryWrapper,
                         final ClientRepositoryWrapper clientRepositoryWrapper, final AddressReadPlatformService addressReadPlatformService,
                         final ChargeReadPlatformService chargeReadPlatformService, final OfficeReadPlatformServiceImpl officeReadPlatformServiceImpl, final LoanProductFeesChargesRepository loanProductFeesChargesRepository,
                         GstServiceImpl gstServiceimpl, ChargeRepository chargeRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.loanRepository = loanRepository;
        this.loanProductRepository = loanProductRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.fundRepository = fundRepository;
        this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
        this.staffRepository = staffRepository;
        this.codeValueRepository = codeValueRepository;
        this.loanScheduleAssembler = loanScheduleAssembler;
        this.loanChargeAssembler = loanChargeAssembler;
        this.collateralAssembler = collateralAssembler;
        this.loanSummaryWrapper = loanSummaryWrapper;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.holidayRepository = holidayRepository;
        this.configurationDomainService = configurationDomainService;
        this.workingDaysRepository = workingDaysRepository;
        this.loanUtilService = loanUtilService;
        this.rateAssembler = rateAssembler;
        this.loanProductFeesChargesRepositoryWrapper =loanProductFeesChargesRepositoryWrapper;
        this.clientRepositoryWrapper=clientRepositoryWrapper;
        this.addressReadPlatformService=addressReadPlatformService;
        this.chargeReadPlatformService=chargeReadPlatformService;
        this.officeReadPlatformServiceImpl=officeReadPlatformServiceImpl;
        this.loanProductFeesChargesRepository =loanProductFeesChargesRepository;
        this.gstServiceimpl = gstServiceimpl;
    }

    public Loan assembleFrom(final Long accountId) {
        final Loan loanAccount = this.loanRepository.findOneWithNotFoundDetection(accountId, true);
        loanAccount.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);

        return loanAccount;
    }

    public void setHelpers(final Loan loanAccount) {
        loanAccount.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);
    }

    public Loan assembleFrom(final JsonCommand command, final AppUser currentUser, final Long partnerId) {
        final JsonElement element = command.parsedJson();

        final Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = this.fromApiJsonHelper.extractLongNamed("groupId", element);

        return assembleApplication(element, clientId, groupId, currentUser,partnerId);
    }

    private Loan assembleApplication(final JsonElement element, final Long clientId, final Long groupId, final AppUser currentUser,final Long partnerId) {

        final String accountNo = this.fromApiJsonHelper.extractStringNamed("accountNo", element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed("productId", element);
        final Long fundId = this.fromApiJsonHelper.extractLongNamed("fundId", element);
        final Long loanOfficerId = this.fromApiJsonHelper.extractLongNamed("loanOfficerId", element);
        final Long transactionProcessingStrategyId = this.fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element);
        final Long loanPurposeId = this.fromApiJsonHelper.extractLongNamed("loanPurposeId", element);
        final Boolean syncDisbursementWithMeeting = this.fromApiJsonHelper.extractBooleanNamed("syncDisbursementWithMeeting", element);
        final Boolean createStandingInstructionAtDisbursement = this.fromApiJsonHelper
                .extractBooleanNamed("createStandingInstructionAtDisbursement", element);

        final LoanProduct loanProduct = this.loanProductRepository.findById(productId)
                .orElseThrow(() -> new LoanProductNotFoundException(productId));
        final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName, element);
        loanProduct.validateDisbursementDate(expectedDisbursementDate,loanProduct);
        final BigDecimal amount = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.disbursementPrincipalParameterName, element);
        final Fund fund = findFundByIdIfProvided(fundId);
        final Staff loanOfficer = findLoanOfficerByIdIfProvided(loanOfficerId);
        final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(
                transactionProcessingStrategyId);
        CodeValue loanPurpose = null;
        if (loanPurposeId != null) {
            loanPurpose = this.codeValueRepository.findOneWithNotFoundDetection(loanPurposeId);
        }
        List<LoanDisbursementDetails> disbursementDetails = new ArrayList<>();
        BigDecimal fixedEmiAmount = null;
        if (loanProduct.isMultiDisburseLoan() || loanProduct.canDefineInstallmentAmount()) {
            fixedEmiAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.emiAmountParameterName, element);
        }
        BigDecimal maxOutstandingLoanBalance = null;
        if (loanProduct.isMultiDisburseLoan()) {
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
            maxOutstandingLoanBalance = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.maxOutstandingBalanceParameterName,
                    element, locale);

            disbursementDetails = this.loanUtilService.fetchDisbursementData(element.getAsJsonObject());
            if (loanProduct.isDisallowExpectedDisbursements()) {
                if (!disbursementDetails.isEmpty()) {
                    final String errorMessage = "For this loan product, disbursement details are not allowed";
                    throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }
            } else {
                if (disbursementDetails.isEmpty()) {
                    final String errorMessage = "For this loan product, disbursement details must be provided";
                    throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }
            }

            if (disbursementDetails.size() > loanProduct.maxTrancheCount()) {
                final String errorMessage = "Number of tranche shouldn't be greter than " + loanProduct.maxTrancheCount();
                throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                        loanProduct.maxTrancheCount(), disbursementDetails.size());
            }
        }

        final String loanTypeParameterName = "loanType";
        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);
        final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeStr);
        Set<LoanCollateralManagement> collateral = new HashSet<>();

        if (!StringUtils.isBlank(loanTypeStr)) {
            final AccountType loanAccountType = AccountType.fromName(loanTypeStr);

            if (loanAccountType.isIndividualAccount()) {
                collateral = this.collateralAssembler.fromParsedJson(element);

                if (collateral.size() > 0) {
                    BigDecimal totalValue = BigDecimal.ZERO;
                    for (LoanCollateralManagement collateralManagement : collateral) {
                        final CollateralManagementDomain collateralManagementDomain = collateralManagement.getClientCollateralManagement()
                                .getCollaterals();
                        BigDecimal totalCollateral = collateralManagement.getQuantity().multiply(collateralManagementDomain.getBasePrice())
                                .multiply(collateralManagementDomain.getPctToBase()).divide(BigDecimal.valueOf(100));
                        totalValue = totalValue.add(totalCollateral);
                    }

                    if (amount.compareTo(totalValue) > 0) {
                        throw new InvalidAmountOfCollaterals(totalValue);
                    }
                }
            }
        }

        final List<Charge> charge =this.loanChargeAssembler.retrieveCharge(element);

        // Added  calculating gst To the given charge At the time of Loan Creation
       // chargeRepository.getReferenceById()
        List<GstData> gstData = this.gstServiceimpl.calculationOfGst(clientId,charge,amount,loanProduct,element,null);
        final List<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element, disbursementDetails,gstData);
        for (final LoanCharge loanCharge : loanCharges) {
            if (!loanProduct.hasCurrencyCodeOf(loanCharge.currencyCode())) {
                final String errorMessage = "Charge and Loan must have the same currency.";
                throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
            }
            if (loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
                final Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed("linkAccountId", element);
                if (savingsAccountId == null) {
                    final String errorMessage = "one of the charges requires linked savings account for payment";
                    throw new LinkedAccountRequiredException("loanCharge", errorMessage);
                }
            }
        }

        BigDecimal fixedPrincipalPercentagePerInstallment = fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, element);

        Loan loanApplication = null;
        Client client = null;
        Group group = null;

        // Here we add Rates to LoanApplication
        final List<Rate> rates = this.rateAssembler.fromParsedJson(element);

        final LoanProductRelatedDetail loanProductRelatedDetail = this.loanScheduleAssembler.assembleLoanProductRelatedDetail(element);
        final Integer brokenStrategy = loanProductRelatedDetail.getBrokenStrategy();
        final String brokenStrategyType= String.valueOf(BrokenStrategy.fromInt(brokenStrategy));


        final BigDecimal interestRateDifferential = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRateDifferentialParameterName, element);
        final Boolean isFloatingInterestRate = this.fromApiJsonHelper
                .extractBooleanNamed(LoanApiConstants.isFloatingInterestRateParameterName, element);



        if(!loanProduct.enableColendingLoan()) {

            final BigDecimal selfPrincipalAmount=BigDecimal.ZERO;
            final BigDecimal partnerPrincipalAmount=BigDecimal.ZERO;

            if (clientId != null) {
                client = this.clientRepository.findOneWithNotFoundDetection(clientId);
                if (client.isNotActive()) {
                    throw new ClientNotActiveException(clientId);
                }
            }

        if (groupId != null) {
            group = this.groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
            if (group.isNotActive()) {
                throw new GroupNotActiveException(groupId);
            }
        }

        if (client != null && group != null) {

            if (!group.hasClientAsMember(client)) {
                throw new ClientNotInGroupException(clientId, groupId);
            }

            loanApplication = Loan.newIndividualLoanApplicationFromGroup(accountNo, client, group, loanType.getId().intValue(), loanProduct,
                    fund, loanOfficer, loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, null,
                    syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance,
                    createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                    fixedPrincipalPercentagePerInstallment,partnerId, selfPrincipalAmount, partnerPrincipalAmount,brokenStrategyType);

        } else if (group != null) {
            loanApplication = Loan.newGroupLoanApplication(accountNo, group, loanType.getId().intValue(), loanProduct, fund, loanOfficer,
                    loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, null,
                    syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance,
                    createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                    fixedPrincipalPercentagePerInstallment,partnerId, selfPrincipalAmount, partnerPrincipalAmount,brokenStrategyType);

        } else if (client != null) {

            loanApplication = Loan.newIndividualLoanApplication(accountNo, client, loanType.getId().intValue(), loanProduct, fund,
                    loanOfficer, loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, collateral,
                    fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement,
                    isFloatingInterestRate, interestRateDifferential, rates, fixedPrincipalPercentagePerInstallment,partnerId, selfPrincipalAmount, partnerPrincipalAmount,brokenStrategyType);

            }
        }else if(loanProduct.enableColendingLoan()) {

            final Integer selfPrincipalShare = loanProduct.getSelfPrincipalShare();
            final Integer partnerPrincipalShare = loanProduct.getPartnerPrincipalShare();
            final BigDecimal divisor = BigDecimal.valueOf(Double.parseDouble("100.0"));


            // final Money principle=originalPrinciple.add(amount);

            final BigDecimal selfPrincipalAmount=amount.multiply(BigDecimal.valueOf(selfPrincipalShare)).divide(divisor,RoundingMode.CEILING);
            //final Money selfPrincipalAmount1=calculatePrinciple(principle,selfPrincipalShare);

            final BigDecimal partnerPrincipalAmount=amount.multiply(BigDecimal.valueOf(partnerPrincipalShare)).divide(divisor,RoundingMode.CEILING);

            if (clientId != null) {
                client = this.clientRepository.findOneWithNotFoundDetection(clientId);
                if (client.isNotActive()) {
                    throw new ClientNotActiveException(clientId);
                }
            }

            if (groupId != null) {
                group = this.groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
                if (group.isNotActive()) {
                    throw new GroupNotActiveException(groupId);
                }
            }


            if (client != null && group != null) {

                if (!group.hasClientAsMember(client)) {
                    throw new ClientNotInGroupException(clientId, groupId);
                }

                loanApplication = Loan.newIndividualLoanApplicationFromGroup(accountNo, client, group, loanType.getId().intValue(), loanProduct,
                        fund, loanOfficer, loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, null,
                        syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance,
                        createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                        fixedPrincipalPercentagePerInstallment,partnerId, selfPrincipalAmount,partnerPrincipalAmount,brokenStrategyType);

            } else if (group != null) {
                loanApplication = Loan.newGroupLoanApplication(accountNo, group, loanType.getId().intValue(), loanProduct, fund, loanOfficer,
                        loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, null,
                        syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance,
                        createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                        fixedPrincipalPercentagePerInstallment,partnerId, selfPrincipalAmount,  partnerPrincipalAmount,brokenStrategyType);

            } else if (client != null) {

                loanApplication = Loan.newIndividualLoanApplication(accountNo, client, loanType.getId().intValue(), loanProduct, fund,
                        loanOfficer, loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, collateral,
                        fixedEmiAmount, disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement,
                        isFloatingInterestRate, interestRateDifferential, rates, fixedPrincipalPercentagePerInstallment,partnerId,
                        selfPrincipalAmount, partnerPrincipalAmount,brokenStrategyType);

            }
        }


        final Set<LoanPenalForeclosureCharges> loanPenalForeclosureCharges =this.loanChargeAssembler.retrievePenaltyAndForeclosureCharge(loanApplication,loanProduct,element);


        if(!loanPenalForeclosureCharges.isEmpty()){
            loanApplication.setLoanPenalForeclosueCharges(loanPenalForeclosureCharges);;
        }

        final String externalId = this.fromApiJsonHelper.extractStringNamed("externalId", element);
        final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);

        if (loanApplication == null) {
            throw new IllegalStateException("No loan application exists for either a client or group (or both).");
        }
        loanApplication.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);

        if (loanProduct.isMultiDisburseLoan()) {
            for (final LoanDisbursementDetails loanDisbursementDetails : loanApplication.getDisbursementDetails()) {
                loanDisbursementDetails.updateLoan(loanApplication);
            }
        }
        if(Objects.nonNull(loanCharges)){
        loanApplication.updateGstForSelfAndPartnerInLoan(loanCharges,currentUser,true);}

        final LoanApplicationTerms loanApplicationTerms = this.loanScheduleAssembler.assembleLoanTerms(element);
        loanApplication.interestRateSplitCalculation(loanProduct,loanApplicationTerms);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loanApplication.getOfficeId(),
                Date.from(loanApplicationTerms.getExpectedDisbursementDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final LoanScheduleModel loanScheduleModel = this.loanScheduleAssembler.assembleLoanScheduleFrom(loanApplicationTerms,
                isHolidayEnabled, holidays, workingDays, element, disbursementDetails,gstData);
        loanApplication.loanApplicationSubmittal(currentUser, loanScheduleModel, loanApplicationTerms, defaultLoanLifecycleStateMachine(),
                submittedOnDate, externalId, allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay,loanApplication);

        return loanApplication;
    }

    private LoanPenalForeclosureCharges getLoanPenalForeclosureCharges(LoanProductFeesCharges loanProductFeesCharges, Loan loanApplication,LoanProduct loanProduct) {
        Charge charge=loanProductFeesCharges.getCharge();
        return new LoanPenalForeclosureCharges(loanApplication,loanProduct,charge,charge.getAmount(),loanProductFeesCharges.getSelfShare(),loanProductFeesCharges.getPartnerShare());

    }


    private Money calculatePrinciple(Money principle, Integer selfPrincipalShare) {

        final BigDecimal divisor = BigDecimal.valueOf(Double.parseDouble("100.0"));

        return principle.multipliedBy(selfPrincipalShare).dividedBy(divisor,RoundingMode.CEILING);
    }


    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    public CodeValue findCodeValueByIdIfProvided(final Long codeValueId) {
        CodeValue codeValue = null;
        if (codeValueId != null) {
            codeValue = this.codeValueRepository.findOneWithNotFoundDetection(codeValueId);
        }
        return codeValue;
    }

    public Fund findFundByIdIfProvided(final Long fundId) {
        Fund fund = null;
        if (fundId != null) {
            fund = this.fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId));
        }
        return fund;
    }

    public Staff findLoanOfficerByIdIfProvided(final Long loanOfficerId) {
        Staff staff = null;
        if (loanOfficerId != null) {
            staff = this.staffRepository.findById(loanOfficerId).orElseThrow(() -> new StaffNotFoundException(loanOfficerId));
            if (staff.isNotLoanOfficer()) {
                throw new StaffRoleException(loanOfficerId, StaffRoleException.StaffRole.LOAN_OFFICER);
            }
        }
        return staff;
    }

    public LoanTransactionProcessingStrategy findStrategyByIdIfProvided(final Long transactionProcessingStrategyId) {
        LoanTransactionProcessingStrategy strategy = null;
        if (transactionProcessingStrategyId != null) {
            strategy = this.loanTransactionProcessingStrategyRepository.findById(transactionProcessingStrategyId)
                    .orElseThrow(() -> new LoanTransactionProcessingStrategyNotFoundException(transactionProcessingStrategyId));
        }
        return strategy;
    }

    public void validateExpectedDisbursementForHolidayAndNonWorkingDay(final Loan loanApplication) {

        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loanApplication.getOfficeId(),
                Date.from(loanApplication.getExpectedDisbursedOnLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loanApplication.validateExpectedDisbursementForHolidayAndNonWorkingDay(workingDays, allowTransactionsOnHoliday, holidays,
                allowTransactionsOnNonWorkingDay);
    }
}
