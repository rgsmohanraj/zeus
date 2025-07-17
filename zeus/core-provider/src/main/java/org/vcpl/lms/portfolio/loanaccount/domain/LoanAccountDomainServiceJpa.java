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
package org.vcpl.lms.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vcpl.lms.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.configuration.domain.ConfigurationDomainService;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.vcpl.lms.infrastructure.core.exception.MultiException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.holiday.domain.Holiday;
import org.vcpl.lms.organisation.holiday.domain.HolidayRepository;
import org.vcpl.lms.organisation.holiday.domain.HolidayStatusType;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrency;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.organisation.office.service.OfficeReadPlatformServiceImpl;
import org.vcpl.lms.organisation.workingdays.domain.WorkingDays;
import org.vcpl.lms.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.vcpl.lms.portfolio.account.domain.AccountTransferRepository;
import org.vcpl.lms.portfolio.account.domain.AccountTransferStandingInstruction;
import org.vcpl.lms.portfolio.account.domain.AccountTransferTransaction;
import org.vcpl.lms.portfolio.account.domain.StandingInstructionRepository;
import org.vcpl.lms.portfolio.account.domain.StandingInstructionStatus;
import org.vcpl.lms.portfolio.accountdetails.domain.AccountType;
import org.vcpl.lms.portfolio.charge.domain.*;
import org.vcpl.lms.portfolio.charge.service.ChargeReadPlatformService;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.client.domain.ClientRepositoryWrapper;
import org.vcpl.lms.portfolio.client.exception.ClientNotActiveException;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants.BusinessEntity;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants.BusinessEvents;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.common.service.BusinessEventNotifierService;
import org.vcpl.lms.portfolio.group.domain.Group;
import org.vcpl.lms.portfolio.group.exception.GroupNotActiveException;
import org.vcpl.lms.portfolio.loanaccount.foreclosure.*;
import org.vcpl.lms.portfolio.loanaccount.data.HolidayDetailDTO;
import org.vcpl.lms.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.vcpl.lms.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.exception.InstallemtException;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistoryRepository;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.exception.TransactionAmountException;
import org.vcpl.lms.portfolio.loanaccount.service.*;
import org.vcpl.lms.portfolio.loanproduct.domain.ProductCollectionConfig;
import org.vcpl.lms.portfolio.note.domain.Note;
import org.vcpl.lms.portfolio.note.domain.NoteRepository;
import org.vcpl.lms.portfolio.partner.domain.Partner;
import org.vcpl.lms.portfolio.partner.domain.PartnerRepository;
import org.vcpl.lms.portfolio.partner.exception.PartnerNotFoundException;
import org.vcpl.lms.portfolio.paymentdetail.domain.PaymentDetail;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.data.PostDatedChecksStatus;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanAccountDomainServiceJpa extends ForeclosureService implements LoanAccountDomainService {


    private final LoanAssembler loanAccountAssembler;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanTransactionRepository loanTransactionRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepository holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;

    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final NoteRepository noteRepository;
    private final AccountTransferRepository accountTransferRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository;
    private final LoanAccrualPlatformService loanAccrualPlatformService;
    private final PlatformSecurityContext context;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanUtilService loanUtilService;
    private final StandingInstructionRepository standingInstructionRepository;
    private final PostDatedChecksRepository postDatedChecksRepository;
    private final LoanCollateralManagementRepository loanCollateralManagementRepository;
    private final PartnerRepository partnerRepository;
    private final XirrHistoryDetailsRepository xirrHistoryDetailsRepository;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final ChargeRepositoryWrapper chargeRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final OfficeReadPlatformServiceImpl officeReadPlatformServiceImpl;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanChargeRepository loanChargeRepository;
    private final LoanPenalForeclosureChargesRepository loanPenalForeclosureChargesRepository;
    private final LoanAccrualRepository loanAccrualRepository;
    private final LoanDpdHistoryRepository loanDpdHistoryRepository;

    private  final LoanHistoryRepo loanHistoryRepo;
    private final GstService gstServiceimpl;

    private final LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository;

    private final LoanChargePaymentService loanChargePaymentService;
    private final LoanAccrualWritePlatformService loanAccrualWritePlatformService;
    private static final Logger LOG = LoggerFactory.getLogger(LoanAccountDomainServiceJpa.class);

    @Autowired
    public LoanAccountDomainServiceJpa(final LoanAssembler loanAccountAssembler, final LoanRepositoryWrapper loanRepositoryWrapper,
                                       final LoanTransactionRepository loanTransactionRepository, final NoteRepository noteRepository,
                                       final ConfigurationDomainService configurationDomainService, final HolidayRepository holidayRepository,
                                       final WorkingDaysRepositoryWrapper workingDaysRepository,
                                       final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper,
                                       final JournalEntryWritePlatformService journalEntryWritePlatformService,
                                       final AccountTransferRepository accountTransferRepository,
                                       final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
                                       final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository,
                                       final LoanAccrualPlatformService loanAccrualPlatformService, final PlatformSecurityContext context,
                                       final BusinessEventNotifierService businessEventNotifierService, final LoanUtilService loanUtilService,
                                       final StandingInstructionRepository standingInstructionRepository, final PostDatedChecksRepository postDatedChecksRepository,
                                       final LoanCollateralManagementRepository loanCollateralManagementRepository, final PartnerRepository partnerRepository,
                                       final XirrHistoryDetailsRepository xirrHistoryDetailsRepository, final ChargeReadPlatformService chargeReadPlatformService,
                                       final ChargeRepositoryWrapper chargeRepositoryWrapper, final ClientRepositoryWrapper clientRepositoryWrapper,
                                       final OfficeReadPlatformServiceImpl officeReadPlatformServiceImpl, final LoanChargeAssembler loanChargeAssembler,
                                       final LoanChargeRepository loanChargeRepository, final LoanPenalForeclosureChargesRepository loanPenalForeclosureChargesRepository,
                                       final LoanAccrualRepository loanAccrualRepository, final LoanDpdHistoryRepository loanDpdHistoryRepository, LoanHistoryRepo loanHistoryRepo,
                                       GstService gstServiceimpl, LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository, final LoanChargePaymentService loanChargePaymentService,
                                       final LoanAccrualWritePlatformService loanAccrualWritePlatformService) {
        super(loanChargePaymentService);
        this.loanChargePaymentService = loanChargePaymentService;
        this.loanAccountAssembler = loanAccountAssembler;
        this.loanTransactionRepository = loanTransactionRepository;
        this.noteRepository = noteRepository;
        this.configurationDomainService = configurationDomainService;
        this.holidayRepository = holidayRepository;
        this.workingDaysRepository = workingDaysRepository;
        this.applicationCurrencyRepositoryWrapper = applicationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.accountTransferRepository = accountTransferRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.repaymentScheduleInstallmentRepository = repaymentScheduleInstallmentRepository;
        this.loanAccrualPlatformService = loanAccrualPlatformService;
        this.context = context;
        this.businessEventNotifierService = businessEventNotifierService;
        this.loanUtilService = loanUtilService;
        this.standingInstructionRepository = standingInstructionRepository;
        this.postDatedChecksRepository = postDatedChecksRepository;
        this.loanCollateralManagementRepository = loanCollateralManagementRepository;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.partnerRepository = partnerRepository;
        this.xirrHistoryDetailsRepository = xirrHistoryDetailsRepository;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.chargeRepositoryWrapper = chargeRepositoryWrapper;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.officeReadPlatformServiceImpl = officeReadPlatformServiceImpl;
        this.loanChargeAssembler = loanChargeAssembler;
        this.loanChargeRepository = loanChargeRepository;
        this.loanPenalForeclosureChargesRepository = loanPenalForeclosureChargesRepository;
        this.loanAccrualRepository = loanAccrualRepository;
        this.loanDpdHistoryRepository = loanDpdHistoryRepository;
        this.loanHistoryRepo = loanHistoryRepo;
        this.gstServiceimpl = gstServiceimpl;
        this.loanRepaymentScheduleHistoryRepository = loanRepaymentScheduleHistoryRepository;
        this.loanAccrualWritePlatformService = loanAccrualWritePlatformService;
    }

    @Transactional
    @Override
    public LoanTransaction makeRepayment(final LoanTransactionType repaymentTransactionType, final Loan loan,
                                         final CommandProcessingResultBuilder builderResult, final LocalDate transactionDate, final BigDecimal transactionAmount,
                                         final PaymentDetail paymentDetail, final String noteText, final String txnExternalId, final boolean isRecoveryRepayment,
                                         boolean isAccountTransfer, HolidayDetailDTO holidayDetailDto, Boolean isHolidayValidationDone, final BigDecimal selfDue, final BigDecimal partnerDue, final Integer installmentNumber,final String receiptReferenceNumber, final String partnerTransferUtr,final Date partnerTransferDate,
                                         CodeValue repaymentMode,Integer triggeredBy,LoanHistoryRepo loanHistoryRepo) {
        return makeRepayment(repaymentTransactionType, loan, builderResult, transactionDate, transactionAmount, paymentDetail, noteText,
                txnExternalId, isRecoveryRepayment, isAccountTransfer, holidayDetailDto, isHolidayValidationDone, false, selfDue, partnerDue,
                installmentNumber,receiptReferenceNumber,partnerTransferUtr,partnerTransferDate,repaymentMode,triggeredBy,loanHistoryRepo);
    }

    @Transactional
    @Override
    public void updateLoanCollateralTransaction(Set<LoanCollateralManagement> loanCollateralManagementSet) {
        this.loanCollateralManagementRepository.saveAll(loanCollateralManagementSet);
    }

    @Transactional
    @Override
    public void updateLoanCollateralStatus(Set<LoanCollateralManagement> loanCollateralManagementSet, boolean isReleased) {
        for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagementSet) {
            loanCollateralManagement.setIsReleased(isReleased);
        }
        this.loanCollateralManagementRepository.saveAll(loanCollateralManagementSet);
    }

    @Transactional
    @Override
    public LoanTransaction makeRepayment(final LoanTransactionType repaymentTransactionType, final Loan loan,
                                         final CommandProcessingResultBuilder builderResult, final LocalDate transactionDate, final BigDecimal transactionAmount,
                                         final PaymentDetail paymentDetail, final String noteText, final String txnExternalId, final boolean isRecoveryRepayment,
                                         boolean isAccountTransfer, HolidayDetailDTO holidayDetailDto, Boolean isHolidayValidationDone,
                                         final boolean isLoanToLoanTransfer, final BigDecimal selfDue, final BigDecimal partnerDue, Integer installmentNumber,
                                         final String receiptReferenceNumber,final String partnerTransferUtr,final Date partnerTransferDate,
                                         CodeValue repaymentMode,Integer triggeredBy,final LoanHistoryRepo loanHistoryRepo) {
        AppUser currentUser = getAppUserIfPresent();
        checkClientOrGroupActive(loan);
        final Long partnerId = loan.getPartnerId();
        final Partner partner = this.partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException(partnerId));

        //final BigDecimal avilableLimit=partner_Id.getAvilableLimit();

        BusinessEvents repaymentTypeEvent = getRepaymentTypeBusinessEvent(repaymentTransactionType, isRecoveryRepayment);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(repaymentTypeEvent,
                constructEntityMap(BusinessEntity.LOAN, loan));

        /**
         *Installment validation added for bulk Loan upload
         */

        validateInstallmentNumber(installmentNumber, loan);
        final BigDecimal advanceAmount = retriveAdvanceAmountFromRepaymentSchedule(loan);

        // TODO: Is it required to validate transaction date with meeting dates
        // if repayments is synced with meeting?
        /*
         * if(loan.isSyncDisbursementWithMeeting()){ // validate actual disbursement date against meeting date
         * CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByLoanId
         * (loan.getId(), CalendarEntityType.LOANS.getValue()); this.loanEventApiJsonValidator
         * .validateRepaymentDateWithMeetingDate(transactionDate, calendarInstance); }
         */


        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money repaymentAmount = Money.of(loan.getCurrency(), transactionAmount);
        final Money selfRepaymentAmount = Money.of(loan.getCurrency(), selfDue);
        final Money partnerRepaymentAmount = Money.of(loan.getCurrency(), partnerDue);
        final MonetaryCurrency currency = repaymentAmount.getCurrency();


        if (repaymentAmount.isEqualTo(Money.zero(currency)) && advanceAmount.compareTo(BigDecimal.ZERO) == 0) {

            throw new TransactionAmountException("Transaction Amount can not be Zero and Advance Amount Also Not Avilable give Any Transaction Amount");
        }

        LoanTransaction newRepaymentTransaction = null;
        final LocalDateTime currentDateTime = DateUtils.getLocalDateTimeOfTenant();
        if (isRecoveryRepayment) {
            newRepaymentTransaction = LoanTransaction.recoveryRepayment(loan.getOffice(), repaymentAmount, paymentDetail, transactionDate,
                    txnExternalId, currentDateTime, currentUser, selfRepaymentAmount, partnerRepaymentAmount);
        } else {
            newRepaymentTransaction = LoanTransaction.repaymentType(repaymentTransactionType, loan.getOffice(), repaymentAmount,
                    paymentDetail, transactionDate, txnExternalId, currentDateTime, currentUser, selfRepaymentAmount, partnerRepaymentAmount, repaymentTypeEvent.toString(),receiptReferenceNumber,partnerTransferUtr,partnerTransferDate,repaymentMode);
        }
       /* if (newRepaymentTransaction.getTransactionDate().isBefore(LocalDate.now())) {
            loan.reversalForBackdatedCaseWithOverDueCharge(newRepaymentTransaction.getTransactionDate(), installmentNumber);
            // loan.updateDpdForBackDatedCase(newRepaymentTransaction.getTransactionDate(),installmentNumber);
        }*/

        LocalDate recalculateFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = transactionDate;
        }
        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom,
                holidayDetailDto);



        final ChangedTransactionDetail changedTransactionDetail = loan.makeRepayment(newRepaymentTransaction,
                defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds, isRecoveryRepayment,
                scheduleGeneratorDTO, currentUser, isHolidayValidationDone,loanHistoryRepo);

        if(triggeredBy != null){
            newRepaymentTransaction.setAppUser(currentUser);}

        // final MonetaryCurrency currency = loan.getCurrency();
        XirrHistoryDetails xirrHistoryDetails = new XirrHistoryDetails(loan, newRepaymentTransaction.getTransactionDate(), LoanTransactionType.REPAYMENT.getValue(), loan.getXirrValue(), newRepaymentTransaction.getAmount(currency).getAmount());
        loan.getXirrHistoryDetails().add(xirrHistoryDetails);
        // xirrHistoryDetailsRepository.saveAndFlush(xirrHistoryDetails);
        /**
         *Loan Arrear Ageing column updation for makerepayment,foreclosure,adhoc charge
         */
        loan.getLoanSummary().updateLoanArrearAgeing(loan);

//        Money afterRepaymentAdvanceAmount = Money.of(loan.getCurrency(),this.retriveAdvanceAmountFromRepaymentSchedule(loan));
//        LoanTransaction advanceAmountTransaction = null;
//        if(afterRepaymentAdvanceAmount.getAmount().doubleValue() > 0){
//            advanceAmountTransaction = LoanTransaction.repaymentType(repaymentTransactionType, loan.getOffice(), afterRepaymentAdvanceAmount,
//                    paymentDetail, transactionDate, txnExternalId, currentDateTime, currentUser, Money.zero(loan.getCurrency()), Money.zero(loan.getCurrency()),BusinessEvents.ADVANCE_AMOUNT.getValue(), receiptReferenceNumber,partnerTransferUtr,partnerTransferDate,repaymentMode);
//            advanceAmountTransaction.setLoan(loan);
//
//            saveLoanTransactionWithDataIntegrityViolationChecks(advanceAmountTransaction);
//        }
        loan.setValueDate(Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
       // loan.getLoanTransactions().add(newRepaymentTransaction);
        // saveLoanTransactionWithDataIntegrityViolationChecks(newRepaymentTransaction);

//        if(backdatedOverDueCharges!=null){
//            loanChargeRepository.saveAndFlush(backdatedOverDueCharges);
//        }

        /***
         * TODO Vishwas Batch save is giving me a HibernateOptimisticLockingFailureException, looping and saving for the
         * time being, not a major issue for now as this loop is entered only in edge cases (when a payment is made
         * before the latest payment recorded against the loan)
         ***/

        // Reassigning max dpd
        Integer maxDpd =  loan.getRepaymentScheduleInstallments().stream()
                .map(LoanRepaymentScheduleInstallment::getDaysPastDue)
                .filter(Objects::nonNull).max(Integer::compare)
                .orElse(0);
        loan.setDaysPastDue(maxDpd);
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        boolean isEmiFullyPaid = loan.getRepaymentScheduleInstallments().stream().anyMatch(loanRepaymentScheduleInstallment ->
                loanRepaymentScheduleInstallment.getInstallmentNumber().equals(installmentNumber) && loanRepaymentScheduleInstallment.isObligationsMet());
        if(isEmiFullyPaid) {
            loanDpdHistoryRepository.getByLoanIdAndInstallmentAndDpdOnDateGreaterThanEqual(loan.getId(),installmentNumber,
                    DateUtils.convertLocalDateToDate(transactionDate)).ifPresent(loanDpdHistories ->
                    loanDpdHistories.stream()
                            .peek(loanDpdHistory -> loanDpdHistory.setIsReversed(true))
                            .forEach(loanDpdHistoryRepository::save));
        }

        if (changedTransactionDetail != null) {
            for (Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                saveLoanTransactionWithDataIntegrityViolationChecks(mapEntry.getValue());
                // update loan with references to the newly created transactions
                loan.addLoanTransaction(mapEntry.getValue());
                updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRepaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, isLoanToLoanTransfer);

        recalculateAccruals(loan);

        this.businessEventNotifierService.notifyBusinessEventWasExecuted(repaymentTypeEvent,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, newRepaymentTransaction));

        // disable all active standing orders linked to this loan if status
        // changes to closed
        loan.setEvent("collection");
        disableStandingInstructionsLinkedToClosedLoan(loan);

        builderResult.withEntityId(newRepaymentTransaction.getId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withGroupId(loan.getGroupId());

        if (AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {
            // Mark Post Dated Check as paid.
            final Set<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappings = newRepaymentTransaction
                    .getLoanTransactionToRepaymentScheduleMappings();

            // getting original principal Amount from the Emi

            if (loanTransactionToRepaymentScheduleMappings != null) {
                for (LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping : loanTransactionToRepaymentScheduleMappings) {
                    LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loanTransactionToRepaymentScheduleMapping
                            .getLoanRepaymentScheduleInstallment();
                    if (loanRepaymentScheduleInstallment != null) {
                        final boolean isPaid = loanRepaymentScheduleInstallment.isNotFullyPaidOff();
                        PostDatedChecks postDatedChecks = this.postDatedChecksRepository
                                .getPendingPostDatedCheck(loanRepaymentScheduleInstallment);

                        if (postDatedChecks != null) {
                            if (!isPaid) {
                                postDatedChecks.setStatus(PostDatedChecksStatus.POST_DATED_CHECKS_PAID);
                            } else {
                                postDatedChecks.setStatus(PostDatedChecksStatus.POST_DATED_CHECKS_PENDING);
                            }
                            this.postDatedChecksRepository.saveAndFlush(postDatedChecks);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        BigDecimal principalPortion = newRepaymentTransaction.getPrincipalPortion();
        BigDecimal increaredAvailableLimit = BigDecimal.valueOf(0);
        final BigDecimal availableLimit = partner.getBalanceLimit();
        if (principalPortion != null) {
            increaredAvailableLimit = availableLimit.add(principalPortion);
        } else {
            principalPortion = BigDecimal.valueOf(0);
            increaredAvailableLimit = availableLimit.add(principalPortion);
        }
        partner.setBalanceLimit(increaredAvailableLimit);

        return newRepaymentTransaction;
    }

    private void validateInstallmentNumber(Integer installmentNumber, Loan loan) {

            final LoanRepaymentScheduleInstallment LatestNotPaidInstallment = loan.getLatestNotPaidInstallment();
            if(installmentNumber > 1) {
                final LoanRepaymentScheduleInstallment previousInstallment = loan.fetchRepaymentScheduleInstallment(installmentNumber -1);

            if ( previousInstallment!= null && (previousInstallment.isNotFullyPaidOff() || previousInstallment.isPartlyPaid())) {
                throw new InstallemtException("error.msg.repayment.loan.duplicate.installment",
                        "Installment Number " + LatestNotPaidInstallment.getInstallmentNumber()  + " is not fully paid. Unable to Process this Record ", previousInstallment.getInstallmentNumber());}
//            } else if (previousInstallment!= null && previousInstallment.isPartlyPaid()) {
//                throw new InstallemtException("error.msg.repayment.loan.duplicate.installment",
//                        "Installment Number "+ LatestNotPaidInstallment.getInstallmentNumber()  + "is not fully paid. Unable to Process this Record ", previousInstallment.getInstallmentNumber());
//            }
            }
        final LoanRepaymentScheduleInstallment currentInstallment =loan.fetchRepaymentScheduleInstallment(installmentNumber);
             if (!currentInstallment.isNotFullyPaidOff()) {
                if (currentInstallment.getInstallmentNumber() == installmentNumber) {
                    throw new InstallemtException("error.msg.repayment.loan.duplicate.installment",
                            "Loan Repayment with InstallmetNumber  " + installmentNumber + "  Already Paid. Unable To process This Record ",  installmentNumber);
                }
            }
    }

    private BigDecimal retriveAdvanceAmountFromRepaymentSchedule(Loan loan) {

        BigDecimal advanceAmount =BigDecimal.ZERO;
        final  MonetaryCurrency currency=loan.getCurrency();

        List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments =loan.getRepaymentScheduleInstallments();
        for(LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : loanRepaymentScheduleInstallments)
        {
            advanceAmount=advanceAmount.add(loanRepaymentScheduleInstallment.getAdvanceAmount(currency).getAmount());}

        if(advanceAmount != null ){

            return advanceAmount;
        }
        else{
            return BigDecimal.ZERO;
        }

    }

    private BusinessEvents getRepaymentTypeBusinessEvent(LoanTransactionType repaymentTransactionType, boolean isRecoveryRepayment) {
        BusinessEvents repaymentTypeEvent = null;
        if (repaymentTransactionType.isRepayment()) {
            repaymentTypeEvent = BusinessEvents.LOAN_MAKE_REPAYMENT;
        } else if (repaymentTransactionType.isMerchantIssuedRefund()) {
            repaymentTypeEvent = BusinessEvents.LOAN_MERCHANT_ISSUED_REFUND;
        } else if (repaymentTransactionType.isPayoutRefund()) {
            repaymentTypeEvent = BusinessEvents.LOAN_PAYOUT_REFUND;
        } else if (repaymentTransactionType.isGoodwillCredit()) {
            repaymentTypeEvent = BusinessEvents.LOAN_GOODWILL_CREDIT;
        } else if (isRecoveryRepayment) {
            repaymentTypeEvent = BusinessEvents.LOAN_RECOVERY_PAYMENT;
        }
        return repaymentTypeEvent;
    }

    private void saveLoanTransactionWithDataIntegrityViolationChecks(LoanTransaction newRepaymentTransaction) {
        try {
            this.loanTransactionRepository.saveAndFlush(newRepaymentTransaction);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").value(newRepaymentTransaction.getExternalId())
                        .failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
        }
    }

    private void saveAndFlushLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            this.loanRepositoryWrapper.saveAndFlush(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
        }
    }

    @Override
    public void saveLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            this.loanRepositoryWrapper.save(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
        }
    }

    @Override
    @Transactional
    public LoanTransaction makeChargePayment(final Loan loan, final Long chargeId, final LocalDate transactionDate,
            final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final String noteText, final String txnExternalId,
            final Integer transactionType, Integer installmentNumber) {
        AppUser currentUser = getAppUserIfPresent();
        boolean isAccountTransfer = true;
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_CHARGE_PAYMENT,
                constructEntityMap(BusinessEntity.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money paymentAmout = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransactionType loanTransactionType = LoanTransactionType.fromInt(transactionType);

        final LoanTransaction newPaymentTransaction = LoanTransaction.loanPayment(null, loan.getOffice(), paymentAmout, paymentDetail,
                transactionDate, txnExternalId, loanTransactionType, DateUtils.getLocalDateTimeOfTenant(), currentUser,null,null);

        if (loanTransactionType.isRepaymentAtDisbursement()) {
            loan.handlePayDisbursementTransaction(chargeId, newPaymentTransaction, existingTransactionIds, existingReversedTransactionIds);
        } else {
            final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
            final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                    Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), HolidayStatusType.ACTIVE.getValue());
            final WorkingDays workingDays = this.workingDaysRepository.findOne();
            final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
            final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                    allowTransactionsOnNonWorkingDay);

            loan.makeChargePayment(chargeId, defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds,
                    holidayDetailDTO, newPaymentTransaction, installmentNumber);
        }
        saveLoanTransactionWithDataIntegrityViolationChecks(newPaymentTransaction);
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newPaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_CHARGE_PAYMENT,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, newPaymentTransaction));
        return newPaymentTransaction;
    }

    private void postJournalEntries(final Loan loanAccount, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isAccountTransfer) {
        postJournalEntries(loanAccount, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, false);
    }

    private void postJournalEntries(final Loan loanAccount, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isAccountTransfer, boolean isLoanToLoanTransfer) {

        final MonetaryCurrency currency = loanAccount.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepositoryWrapper.findOneWithNotFoundDetection(currency);

        final Map<String, Object> accountingBridgeData = loanAccount.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        accountingBridgeData.put("isLoanToLoanTransfer", isLoanToLoanTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    private void checkClientOrGroupActive(final Loan loan) {
        final Client client = loan.client();
        if (client != null) {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        final Group group = loan.group();
        if (group != null) {
            if (group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }
        }
    }

    @Override
    public LoanTransaction makeRefund(final Long accountId, final CommandProcessingResultBuilder builderResult,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final String noteText,
            final String txnExternalId) {
        AppUser currentUser = getAppUserIfPresent();
        boolean isAccountTransfer = true;
        final Loan loan = this.loanAccountAssembler.assembleFrom(accountId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_REFUND,
                constructEntityMap(BusinessEntity.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money refundAmount = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransaction newRefundTransaction = LoanTransaction.refund(loan.getOffice(), refundAmount, paymentDetail, transactionDate,
                txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser,null,null);
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loan.makeRefund(newRefundTransaction, defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds,
                allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

        saveLoanTransactionWithDataIntegrityViolationChecks(newRefundTransaction);
        this.loanRepositoryWrapper.saveAndFlush(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_REFUND,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, newRefundTransaction));
        builderResult.withEntityId(newRefundTransaction.getId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withGroupId(loan.getGroupId());

        return newRefundTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction makeDisburseTransaction(final Long loanId, final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PaymentDetail paymentDetail, final String noteText, final String txnExternalId) {
        return makeDisburseTransaction(loanId, transactionDate, transactionAmount, paymentDetail, noteText, txnExternalId, false);
    }

    @Transactional
    @Override
    public LoanTransaction makeDisburseTransaction(final Long loanId, final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PaymentDetail paymentDetail, final String noteText, final String txnExternalId, final boolean isLoanToLoanTransfer) {
        AppUser currentUser = getAppUserIfPresent();
        final Loan loan = this.loanAccountAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        boolean isAccountTransfer = true;
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        final Money amount = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan.getOffice(), amount, paymentDetail, transactionDate,
                txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser,null,null);

        // Subtract Previous loan outstanding balance from netDisbursalAmount
        loan.deductFromNetDisbursalAmount(transactionAmount);

        disbursementTransaction.updateLoan(loan);
        loan.addLoanTransaction(disbursementTransaction);
        saveLoanTransactionWithDataIntegrityViolationChecks(disbursementTransaction);
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, disbursementTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, isLoanToLoanTransfer);
        return disbursementTransaction;
    }

    @Override
    public void reverseTransfer(final LoanTransaction loanTransaction) {
        loanTransaction.reverse();
        saveLoanTransactionWithDataIntegrityViolationChecks(loanTransaction);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.vcpl.lms.portfolio.loanaccount.domain.LoanAccountDomainService
     * #recalculateAccruals(org.vcpl.lms.portfolio.loanaccount.domain. Loan)
     */
    @Override
    public void recalculateAccruals(Loan loan) {
        boolean isInterestCalcualtionHappened = loan.repaymentScheduleDetail().isInterestRecalculationEnabled();
        recalculateAccruals(loan, isInterestCalcualtionHappened);
    }

    @Override
    public void recalculateAccruals(Loan loan, boolean isInterestCalcualtionHappened) {
        LocalDate accruedTill = loan.getAccruedTill();
        if (!loan.isPeriodicAccrualAccountingEnabledOnLoanProduct() || !isInterestCalcualtionHappened || accruedTill == null || loan.isNpa()
                || !loan.status().isActive()) {
            return;
        }

        boolean isOrganisationDateEnabled = this.configurationDomainService.isOrganisationstartDateEnabled();
        Date organisationStartDate = new Date();
        if (isOrganisationDateEnabled) {
            organisationStartDate = this.configurationDomainService.retrieveOrganisationStartDate();
        }
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        Long loanId = loan.getId();
        Long officeId = loan.getOfficeId();
        LocalDate accrualStartDate = null;
        PeriodFrequencyType repaymentFrequency = loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType();
        Integer repayEvery = loan.repaymentScheduleDetail().getRepayEvery();
        LocalDate interestCalculatedFrom = loan.getInterestChargedFromDate();
        Long loanProductId = loan.productId();
        MonetaryCurrency currency = loan.getCurrency();
        ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        CurrencyData currencyData = applicationCurrency.toData();
        List<LoanCharge> loanCharges = loan.charges();

        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.getDueDate().isAfter(loan.getMaturityDate())) {
                accruedTill = DateUtils.getLocalDateOfTenant();
            }
            if (!isOrganisationDateEnabled || LocalDate.ofInstant(organisationStartDate.toInstant(), DateUtils.getDateTimeZoneOfTenant())
                    .isBefore(installment.getDueDate())) {
                generateLoanScheduleAccrualData(accruedTill, loanScheduleAccrualDatas, loanId, officeId, accrualStartDate,
                        repaymentFrequency, repayEvery, interestCalculatedFrom, loanProductId, currency, currencyData, loanCharges,
                        installment);
            }
        }

        if (!loanScheduleAccrualDatas.isEmpty()) {
            try {
                this.loanAccrualPlatformService.addPeriodicAccruals(accruedTill, loanScheduleAccrualDatas);
            } catch (MultiException e) {
                String globalisationMessageCode = "error.msg.accrual.exception";
                throw new GeneralPlatformDomainRuleException(globalisationMessageCode, e.getMessage(), e);
            }
        }

    }

    private void generateLoanScheduleAccrualData(final LocalDate accruedTill,
            final Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas, final Long loanId, Long officeId,
            final LocalDate accrualStartDate, final PeriodFrequencyType repaymentFrequency, final Integer repayEvery,
            final LocalDate interestCalculatedFrom, final Long loanProductId, final MonetaryCurrency currency,
            final CurrencyData currencyData, final List<LoanCharge> loanCharges, final LoanRepaymentScheduleInstallment installment) {

        if (!accruedTill.isBefore(installment.getDueDate())
                || (accruedTill.isAfter(installment.getFromDate()) && !accruedTill.isAfter(installment.getDueDate()))) {
            BigDecimal dueDateFeeIncome = BigDecimal.ZERO;
            BigDecimal dueDatePenaltyIncome = BigDecimal.ZERO;
            LocalDate chargesTillDate = installment.getDueDate();
            if (!accruedTill.isAfter(installment.getDueDate())) {
                chargesTillDate = accruedTill;
            }

            for (final LoanCharge loanCharge : loanCharges) {
                if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(installment.getFromDate(), chargesTillDate)) {
                    if (loanCharge.isFeeCharge()) {
                        dueDateFeeIncome = dueDateFeeIncome.add(loanCharge.amount());
                    } else if (loanCharge.isPenaltyCharge()) {
                        dueDatePenaltyIncome = dueDatePenaltyIncome.add(loanCharge.amount());
                    }
                }
            }
            LoanScheduleAccrualData accrualData = new LoanScheduleAccrualData(loanId, officeId, installment.getInstallmentNumber(),
                    accrualStartDate, repaymentFrequency, repayEvery, installment.getDueDate(), installment.getFromDate(),
                    installment.getId(), loanProductId, installment.getInterestCharged(currency).getAmount(),
                    installment.getFeeChargesCharged(currency).getAmount(), installment.getPenaltyChargesCharged(currency).getAmount(),
                    installment.getInterestAccrued(currency).getAmount(), installment.getFeeAccrued(currency).getAmount(),
                    installment.getPenaltyAccrued(currency).getAmount(), currencyData, interestCalculatedFrom,
                    installment.getInterestWaived(currency).getAmount());
            loanScheduleAccrualDatas.add(accrualData);

        }
    }

    private void updateLoanTransaction(final Long loanTransactionId, final LoanTransaction newLoanTransaction) {
        final AccountTransferTransaction transferTransaction = this.accountTransferRepository.findByToLoanTransactionId(loanTransactionId);
        if (transferTransaction != null) {
            transferTransaction.updateToLoanTransaction(newLoanTransaction);
            this.accountTransferRepository.save(transferTransaction);
        }
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    @Override
    public CommandProcessingResultBuilder creditBalanceRefund(Long loanId, LocalDate transactionDate, BigDecimal transactionAmount,
            String noteText, String externalId) {
        final Loan loan = this.loanAccountAssembler.assembleFrom(loanId);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_CREDIT_BALANCE_REFUND,
                constructEntityMap(BusinessEntity.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        AppUser currentUser = getAppUserIfPresent();

        final Money refundAmount = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransaction newCreditBalanceRefundTransaction = LoanTransaction.creditBalanceRefund(loan, loan.getOffice(), refundAmount,
                transactionDate, externalId, DateUtils.getLocalDateTimeOfTenant(), currentUser,null,null);

        loan.creditBalanceRefund(newCreditBalanceRefundTransaction, defaultLoanLifecycleStateMachine(), existingTransactionIds,
                existingReversedTransactionIds);

        this.loanTransactionRepository.saveAndFlush(newCreditBalanceRefundTransaction);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newCreditBalanceRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_CREDIT_BALANCE_REFUND,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, newCreditBalanceRefundTransaction));

        return new CommandProcessingResultBuilder().withEntityId(newCreditBalanceRefundTransaction.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId());
    }

    @Override
    public LoanTransaction makeRefundForActiveLoan(Long accountId, CommandProcessingResultBuilder builderResult, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, String noteText, String txnExternalId) {
        final Loan loan = this.loanAccountAssembler.assembleFrom(accountId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_REFUND,
                constructEntityMap(BusinessEntity.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        AppUser currentUser = getAppUserIfPresent();

        final Money refundAmount = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransaction newRefundTransaction = LoanTransaction.refundForActiveLoan(loan.getOffice(), refundAmount, paymentDetail,
                transactionDate, txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser,null,null);
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loan.makeRefundForActiveLoan(newRefundTransaction, defaultLoanLifecycleStateMachine(), existingTransactionIds,
                existingReversedTransactionIds, allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

        this.loanTransactionRepository.saveAndFlush(newRefundTransaction);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_REFUND,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, newRefundTransaction));

        builderResult.withEntityId(newRefundTransaction.getId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withGroupId(loan.getGroupId());

        return newRefundTransaction;
    }

    @Override
    public Map<String, Object> foreCloseLoan(final Loan loan, final LocalDate foreClosureDate, final String noteText,String receiptReferenceNumber, String partnerTransferUtr,
                                             Date partnerTransferDate, CodeValue repaymentMode) {
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_FORECLOSURE,
                constructEntityMap(BusinessEntity.LOAN, loan));

        AppUser currentUser = null;
        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication auth = context.getAuthentication();
        if (auth != null) {
            currentUser = (AppUser) auth.getPrincipal();
        }
        MonetaryCurrency currency = loan.getCurrency();
        LocalDateTime createdDate = DateUtils.getLocalDateTimeOfTenant();
        final Map<String, Object> changes = new LinkedHashMap<>();
        List<LoanTransaction> newTransactions = new ArrayList<>();

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
        final ScheduleGeneratorDTO scheduleGeneratorDTO = null;
        AppUser appUser = getAppUserIfPresent();
        /**
         * @Author Doni Sharmila Added for the Foreclosure short and over due scenario based on that loan transaction generation
         */

        // Finding the list of installment before the foreclosure date  and find the latest installment
        LoanRepaymentScheduleInstallment currentInstallment = ForeclosureUtils.getTheForeclosureInstallment(loan,foreClosureDate);
        ProductCollectionConfig productCollectionConfig = loan.getLoanProduct().getProductCollectionConfig();
        LoanRepaymentScheduleInstallment foreCloseDetail = null;
        ForeclosureEnum foreclosureEnum = checkForeclosureScenario(currentInstallment, foreClosureDate, currency, loan,productCollectionConfig);

        LOG.info("LOAN {} INITIATE FOR FORECLOSURE ON {} ",loan.getId(),foreclosureEnum.toString());

        foreCloseDetail = foreclosureBasedOnScenario(foreclosureEnum, foreClosureDate, currentInstallment, loan.getCurrency(), loan, gstServiceimpl, currentUser,loanAccrualRepository ,Boolean.FALSE);
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                && (loan.getAccruedTill() == null || !foreClosureDate.isEqual(loan.getAccruedTill()))) {
            loan.reverseAccrualsAfter(foreClosureDate);
            Money[] accruedReceivables = loan.getReceivableIncome(foreClosureDate);
            Money interestPortion = foreCloseDetail.getInterestCharged(currency).minus(accruedReceivables[0]);
            Money selfInterestPortion = foreCloseDetail.getSelfInterestCharged(currency).minus(accruedReceivables[0]);
            Money partnerInterestPortion = foreCloseDetail.getPartnerInterestCharged(currency).minus(accruedReceivables[0]);
            Money feePortion = foreCloseDetail.getFeeChargesCharged(currency).minus(accruedReceivables[1]);
            Money penaltyPortion = foreCloseDetail.getPenaltyChargesCharged(currency).minus(accruedReceivables[2]);
            Money total = interestPortion.plus(feePortion).plus(penaltyPortion);
            if (total.isGreaterThanZero()) {
                LoanTransaction accrualTransaction = LoanTransaction.accrueTransaction(loan, loan.getOffice(), foreClosureDate,
                        total.getAmount(), interestPortion.getAmount(), feePortion.getAmount(), penaltyPortion.getAmount(), appUser,selfInterestPortion.getAmount(),partnerInterestPortion.getAmount(),null,null);
                LocalDate fromDate = loan.getDisbursementDate();
                if (loan.getAccruedTill() != null) {
                    fromDate = loan.getAccruedTill();
                }
                createdDate = createdDate.plusSeconds(1);
                newTransactions.add(accrualTransaction);
                loan.addLoanTransaction(accrualTransaction);
                Set<LoanChargePaidBy> accrualCharges = accrualTransaction.getLoanChargesPaid();
                for (LoanCharge loanCharge : loan.charges()) {
                    if (loanCharge.isActive() && !loanCharge.isPaid()
                            && (loanCharge.isDueForCollectionFromAndUpToAndIncluding(fromDate, foreClosureDate)
                                    || loanCharge.isInstalmentFee())) {
                        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrualTransaction, loanCharge,
                                loanCharge.getAmountOutstanding(currency).getAmount(), null);
                        accrualCharges.add(loanChargePaidBy);
                    }
                }
            }
        }
        ForeclosureTemplateData foreclosureTemplateData = this.retrieveForeclosureTemplateData(foreCloseDetail,currency);

        /* Modifying the foreclosure interest based on the scenario */
        loan.updateInstallmentsPostDate(foreClosureDate,foreCloseDetail,foreclosureEnum,this,currentInstallment,productCollectionConfig);

        LoanTransaction payment = null;

        if (foreclosureTemplateData.payPrincipal().plus(foreclosureTemplateData.interestPayable()).plus(foreclosureTemplateData.feePayable()).plus(foreclosureTemplateData.penaltyPayable()).isGreaterThanZero()) {
            final PaymentDetail paymentDetail = null;
            String externalId = null;
            final LocalDateTime currentDateTime = DateUtils.getLocalDateTimeOfTenant();
            payment = LoanTransaction.repayment(loan.getOffice(), foreclosureTemplateData.payPrincipal().plus(foreclosureTemplateData.interestPayable()).minus(foreclosureTemplateData.advanceAmount()),
                    paymentDetail, foreClosureDate, externalId, currentDateTime, appUser, foreclosureTemplateData.paySelfPrincipal().plus(foreclosureTemplateData.paySelfInterest()).getAmount(), foreclosureTemplateData.payPartnerPrincipal().plus(foreclosureTemplateData.payPartnerInterest()).getAmount(), BusinessEvents.LOAN_FORECLOSURE.toString(), null, null, null, null);
            final BigDecimal bounceCharge = ForeclosureUtils.getBounceCharge(loan,foreCloseDetail.getInstallmentNumber()-1);
            createdDate = createdDate.plusSeconds(1);
            payment.updateAmount(foreCloseDetail.getFeeChargesCharged());
            payment.updateSelfAndPartnerAmount(foreCloseDetail.getFeeChargesCharged(),bounceCharge);
            payment.updateCreatedDate(Date.from(createdDate.atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant()));
            payment.updateLoan(loan);
            payment.setReceiptReferenceNumber(receiptReferenceNumber);
            payment.setPartnerTransferUtr(partnerTransferUtr);
            payment.setPartnerTransferDate(partnerTransferDate);
            payment.setRepaymentMode(repaymentMode);
            payment.setTypeOf(11);
            newTransactions.add(payment);
        }

        BigDecimal principalOutstanding =  BigDecimal.ZERO;
        BigDecimal selfPrincipalOutstanding = BigDecimal.ZERO;
        BigDecimal interestOutstanding = BigDecimal.ZERO;
        if(  foreCloseDetail.getInstallmentNumber()>1 && Objects.nonNull(loan.getRepaymentScheduleInstallments().get(loan.getRepaymentScheduleInstallments().size()-2))){
            LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loan.getRepaymentScheduleInstallments().get(loan.getRepaymentScheduleInstallments().size()-2);
             principalOutstanding =  loanRepaymentScheduleInstallment.getPrincipalOutstanding();
             selfPrincipalOutstanding = loanRepaymentScheduleInstallment.getSelfPrincipalOutstanding();
             interestOutstanding =  loanRepaymentScheduleInstallment.getInterestOutstanding(currency).getAmount();

        }

       /* *//*servicer Fee For Foreclosure*//*
        if(loan.getLoanProduct().getServicerFeeConfig() != null) {
            LoanWritePlatformServiceJpaRepositoryImpl.calculateServicerFeeForCharge(loan,loan.getLoanCharges());
        }*/

        List<Long> transactionIds = new ArrayList<>();

        /**
         * Existing Loan Foreclosure Was Commented And Written A Seprate Logic by @Author Doni Sharmila
         */

        /*final ChangedTransactionDetail changedTransactionDetail = loan.handleForeClosureTransactions(payment,
                defaultLoanLifecycleStateMachine(), scheduleGeneratorDTO, appUser,loanHistoryRepo);*/


        foreclosureAppropriation(loan.getRepaymentScheduleInstallments(), loan, payment, foreClosureDate, loanHistoryRepo, loan.getLoanProduct());

        XirrHistoryDetails xirrHistoryDetails = new XirrHistoryDetails(loan,payment.getTransactionDate(),LoanTransactionType.FORECLOSURE.getValue(), loan.getXirrValue(),
                payment.getAmount(currency).getAmount());
        loan.getXirrHistoryDetails().add(xirrHistoryDetails);

        loan.setLoanStatus((LoanStatus.fromInt(LoanStatus.FORECLOSED.getValue())).getValue());

        Date closingDate = Date.from(foreClosureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        loanAccrualWritePlatformService.reverseOnForeclosure(loan, closingDate);
        loan.getLoanSummary().resetLoanArrearAgeing();
        
        if(loan.isClosed()){
            loan.setClosedBy(currentUser);
        }

        updateRepaymentSchedulePostForeclosure(loan, loanRepaymentScheduleHistoryRepository, currentInstallment,
                foreClosureDate,principalOutstanding, interestOutstanding, selfPrincipalOutstanding, foreclosureEnum);

        loan.LoanSummaryDerivedFields();
        for (LoanTransaction newTransaction : newTransactions) {
            newTransaction.updateType(LoanTransactionType.FORECLOSURE.getValue());
            saveLoanTransactionWithDataIntegrityViolationChecks(newTransaction);
            transactionIds.add(newTransaction.getId());
        }
        changes.put("transactions", transactionIds);
        changes.put("eventAmount", foreclosureTemplateData.payPartnerPrincipal().getAmount().negate());

        // Reassigning max dpd
        Integer maxDpd =  loan.getRepaymentScheduleInstallments().stream()
                .map(LoanRepaymentScheduleInstallment::getDaysPastDue)
                .filter(Objects::nonNull).max(Integer::compare)
                .orElse(0);
        loan.setDaysPastDue(maxDpd);

        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        LOG.info("LOAN FORECLOSED {}" ,loan.getId());


        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_FORECLOSURE,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, payment));
        return changes;

    }
    /*  private BigDecimal percentageof(BigDecimal percentage, BigDecimal amount) {

            return percentage.multiply(amount).divide(new BigDecimal(100));
        }*/

    private Map<BusinessEntity, Object> constructEntityMap(final BusinessEntity entityEvent, Object entity) {
        Map<BusinessEntity, Object> map = new HashMap<>(1);
        map.put(entityEvent, entity);
        return map;
    }

    @Override
    @Transactional
    public void disableStandingInstructionsLinkedToClosedLoan(Loan loan) {
        if ((loan != null) && (loan.status() != null) && loan.status().isClosed()) {
            final Integer standingInstructionStatus = StandingInstructionStatus.ACTIVE.getValue();
            final Collection<AccountTransferStandingInstruction> accountTransferStandingInstructions = this.standingInstructionRepository
                    .findByLoanAccountAndStatus(loan, standingInstructionStatus);

            if (!accountTransferStandingInstructions.isEmpty()) {
                for (AccountTransferStandingInstruction accountTransferStandingInstruction : accountTransferStandingInstructions) {
                    accountTransferStandingInstruction.updateStatus(StandingInstructionStatus.DISABLED.getValue());
                    this.standingInstructionRepository.save(accountTransferStandingInstruction);
                }
            }
        }
    }

}
