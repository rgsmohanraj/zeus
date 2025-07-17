package org.vcpl.lms.portfolio.collection.service;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.vcpl.lms.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.configuration.domain.ConfigurationDomainService;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.vcpl.lms.infrastructure.core.exception.MultiException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrency;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.account.domain.*;
import org.vcpl.lms.portfolio.accountdetails.domain.AccountType;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.client.exception.ClientNotActiveException;
import org.vcpl.lms.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.vcpl.lms.portfolio.collection.utills.Collectionutills;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.common.service.BusinessEventNotifierService;
import org.vcpl.lms.portfolio.group.domain.Group;
import org.vcpl.lms.portfolio.group.exception.GroupNotActiveException;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.data.HolidayDetailDTO;
import org.vcpl.lms.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.vcpl.lms.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.domain.xirr.XirrService;
import org.vcpl.lms.portfolio.loanaccount.exception.InstallemtException;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.exception.TransactionAmountException;
import org.vcpl.lms.portfolio.loanaccount.serialization.LoanEventApiJsonValidator;
import org.vcpl.lms.portfolio.loanaccount.service.LoanAccrualPlatformService;
import org.vcpl.lms.portfolio.loanaccount.service.LoanAssembler;
import org.vcpl.lms.portfolio.loanaccount.service.LoanUtilService;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeWritePlatformService;
import org.vcpl.lms.portfolio.note.domain.Note;
import org.vcpl.lms.portfolio.note.domain.NoteRepository;
import org.vcpl.lms.portfolio.partner.domain.Partner;
import org.vcpl.lms.portfolio.partner.domain.PartnerRepository;
import org.vcpl.lms.portfolio.partner.exception.PartnerNotFoundException;
import org.vcpl.lms.portfolio.paymentdetail.domain.PaymentDetail;
import org.vcpl.lms.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.data.PostDatedChecksStatus;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.vcpl.lms.useradministration.domain.AppUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
@Service

public non-sealed class CollectionWritePlatformServiceImpl implements CollectionWritePlatformService {
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final PlatformSecurityContext context;
    private final LoanUtilService loanUtilService;
    private final LoanEventApiJsonValidator loanEventApiJsonValidator;
    private final LoanAssembler loanAssembler;
    private final LoanAccountDomainService loanAccountDomainService;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final LoanHistoryRepo loanHistoryRepo;
    private final PostDatedChecksRepository postDatedChecksRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final PartnerRepository partnerRepository;
    private final LoanDpdHistoryRepository loanDpdHistoryRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final NoteRepository noteRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanAccrualPlatformService loanAccrualPlatformService;
    private final AccountTransferRepository accountTransferRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final StandingInstructionRepository standingInstructionRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final CollectionService collectionservice;
    private final LoanAccrualRepository loanAccrualRepository;


    @Autowired
    public CollectionWritePlatformServiceImpl(CodeValueRepositoryWrapper codeValueRepository, PlatformSecurityContext context, LoanUtilService loanUtilService,
                                              LoanEventApiJsonValidator loanEventApiJsonValidator, LoanAssembler loanAssembler,
                                              LoanAccountDomainService loanAccountDomainService, PaymentDetailWritePlatformService paymentDetailWritePlatformService,
                                              LoanHistoryRepo loanHistoryRepo, PostDatedChecksRepository postDatedChecksRepository, BusinessEventNotifierService businessEventNotifierService, PartnerRepository partnerRepository, LoanDpdHistoryRepository loanDpdHistoryRepository, ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper, JournalEntryWritePlatformService journalEntryWritePlatformService, NoteRepository noteRepository, ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository, ConfigurationDomainService configurationDomainService, LoanAccrualPlatformService loanAccrualPlatformService, AccountTransferRepository accountTransferRepository, LoanTransactionRepository loanTransactionRepository, StandingInstructionRepository standingInstructionRepository, LoanRepositoryWrapper loanRepositoryWrapper, CollectionService collectionservice, LoanAccrualRepository loanAccrualRepository) {
        this.codeValueRepository = codeValueRepository;
        this.context = context;
        this.loanUtilService = loanUtilService;
        this.loanEventApiJsonValidator = loanEventApiJsonValidator;
        this.loanAssembler = loanAssembler;
        this.loanAccountDomainService = loanAccountDomainService;
        this.paymentDetailWritePlatformService = paymentDetailWritePlatformService;
        this.loanHistoryRepo = loanHistoryRepo;
        this.postDatedChecksRepository = postDatedChecksRepository;
        this.businessEventNotifierService = businessEventNotifierService;
        this.partnerRepository = partnerRepository;
        this.loanDpdHistoryRepository = loanDpdHistoryRepository;
        this.applicationCurrencyRepositoryWrapper = applicationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.noteRepository = noteRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.configurationDomainService = configurationDomainService;
        this.loanAccrualPlatformService = loanAccrualPlatformService;
        this.accountTransferRepository = accountTransferRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.standingInstructionRepository = standingInstructionRepository;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.collectionservice = collectionservice;
        this.loanAccrualRepository = loanAccrualRepository;
    }

    @Override
    @Transactional
    public CommandProcessingResult makeLoanRepayment(LoanTransactionType repaymentTransactionType, Long loanId, JsonCommand command,
                                                     boolean isRecoveryRepayment) {

        AppUser currentUser = getAppUserIfPresent();
        this.loanUtilService.validateRepaymentTransactionType(repaymentTransactionType);
        this.loanEventApiJsonValidator.validateNewRepaymentTransaction(command.json(), currentUser);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");
        Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
        final String receiptReferenceNumber = command.stringValueOfParameterNamedAllowingNull("receiptReferenceNumber");
        final String partnerTransferUtr = command.stringValueOfParameterNamedAllowingNull("partnerTransferUtr");
        final Date partnerTransferDate = command.dateValueOfParameterNamed("partnerTransferDate");
        final Integer triggeredBy = command.integerValueOfParameterNamed("triggeredBy");
        CodeValue repaymentMode = null;
        final Long repaymentModeId = command.longValueOfParameterNamed(LoanApiConstants.repaymentModeIdParamName);
        if (repaymentModeId != null) {
            repaymentMode = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(LoanRepaymentConstants.REPAYMENT_MODE, repaymentModeId);
        }

        final BigDecimal selfDue = command.bigDecimalValueOfParameterNamed("selfDue");
        final BigDecimal partnerDue = command.bigDecimalValueOfParameterNamed("partnerDue");

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.stringValueOfParameterNamed("paymentTypeId"));
        changes.put("amount", command.stringValueOfParameterNamed("amount"));
        changes.put("triggeredBy", command.stringValueOfParameterNamed("triggeredBy"));

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        installmentNumber = installmentNumber != 0 ? installmentNumber : loan.getRepaymentScheduleInstallments().stream().filter(loanRepayment -> !loanRepayment.isObligationsMet()).findFirst().get().getInstallmentNumber();
        LoanRepaymentScheduleInstallment installment = loan.fetchRepaymentScheduleInstallment(installmentNumber);
        if (Boolean.TRUE.equals(installment.isLastInstallment(loan.getNumberOfRepayments(), installment.getInstallmentNumber())) && transactionAmount.doubleValue() > installment.getTotalOutstanding(loan.getCurrency()).getAmount().doubleValue()) {
            final String errorMessage = "The transaction amount " + transactionAmount + " cannot greater than the total outstanding amount " + installment.getTotalOutstanding(loan.getCurrency());
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.outstanding.amount {}", errorMessage);}
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        final Boolean isHolidayValidationDone = false;
        final HolidayDetailDTO holidayDetailDto = null;
        boolean isAccountTransfer = false;
        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        LoanTransaction loanTransaction = makeRepayment(repaymentTransactionType, loan, commandProcessingResultBuilder, transactionDate, transactionAmount, paymentDetail, noteText, txnExternalId,
                isRecoveryRepayment, isAccountTransfer, holidayDetailDto, isHolidayValidationDone, selfDue, partnerDue, installmentNumber, receiptReferenceNumber,
                partnerTransferUtr, partnerTransferDate, repaymentMode, triggeredBy, loanHistoryRepo);

        // Update loan transaction on repayment.
        if (AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {
            Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
                loanCollateralManagement.setLoanTransactionData(loanTransaction);
                ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();

                if (loan.status().isClosed()) {
                    loanCollateralManagement.setIsReleased(true);
                    BigDecimal quantity = loanCollateralManagement.getQuantity();
                    clientCollateralManagement.updateQuantity(clientCollateralManagement.getQuantity().add(quantity));
                    loanCollateralManagement.setClientCollateralManagement(clientCollateralManagement);
                }
            }
            this.loanAccountDomainService.updateLoanCollateralTransaction(loanCollateralManagements);
        }
        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }



    public LoanTransaction makeRepayment(final LoanTransactionType repaymentTransactionType, final Loan loan,
                                         final CommandProcessingResultBuilder builderResult, final LocalDate transactionDate, final BigDecimal transactionAmount,
                                         final PaymentDetail paymentDetail, final String noteText, final String txnExternalId, final boolean isRecoveryRepayment,
                                         boolean isAccountTransfer, HolidayDetailDTO holidayDetailDto, Boolean isHolidayValidationDone, final BigDecimal selfDue, final BigDecimal partnerDue, final Integer installmentNumber, final String receiptReferenceNumber, final String partnerTransferUtr, final Date partnerTransferDate,
                                         CodeValue repaymentMode, Integer triggeredBy, LoanHistoryRepo loanHistoryRepo) {

        return makeRepayment(repaymentTransactionType, loan, builderResult, transactionDate, transactionAmount, paymentDetail, noteText,
                txnExternalId, isRecoveryRepayment, isAccountTransfer, holidayDetailDto, isHolidayValidationDone, false, selfDue, partnerDue,
                installmentNumber, receiptReferenceNumber, partnerTransferUtr, partnerTransferDate, repaymentMode, triggeredBy, loanHistoryRepo);
    }

    private LoanTransaction makeRepayment(LoanTransactionType repaymentTransactionType, Loan loan, CommandProcessingResultBuilder builderResult,
                                          LocalDate transactionDate, BigDecimal transactionAmount, PaymentDetail paymentDetail, String noteText, String txnExternalId,
                                          boolean isRecoveryRepayment, boolean isAccountTransfer, HolidayDetailDTO holidayDetailDto, Boolean isHolidayValidationDone, boolean isLoanToLoanTransfer, BigDecimal selfDue,
                                          BigDecimal partnerDue, Integer installmentNumber, String receiptReferenceNumber, String partnerTransferUtr, Date partnerTransferDate, CodeValue repaymentMode,
                                          Integer triggeredBy, LoanHistoryRepo loanHistoryRepo) {


        AppUser currentUser = getAppUserIfPresent();
        checkClientOrGroupActive(loan);
        final Long partnerId = loan.getPartnerId();
        final Partner partner = this.partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException(partnerId));

        BusinessEventNotificationConstants.BusinessEvents repaymentTypeEvent = getRepaymentTypeBusinessEvent(repaymentTransactionType, isRecoveryRepayment);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(repaymentTypeEvent, constructEntityMap(BusinessEventNotificationConstants.BusinessEntity.LOAN, loan));

        // Installment validation added for bulk Loan upload
        validateInstallmentNumber(installmentNumber, loan);
        final BigDecimal advanceAmount = retriveAdvanceAmountFromRepaymentSchedule(loan);

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
                    paymentDetail, transactionDate, txnExternalId, currentDateTime, currentUser, selfRepaymentAmount, partnerRepaymentAmount, repaymentTypeEvent.toString(), receiptReferenceNumber, partnerTransferUtr, partnerTransferDate, repaymentMode);
        }
        LocalDate recalculateFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = transactionDate;
        }
        final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom,
                holidayDetailDto);

        final ChangedTransactionDetail changedTransactionDetail = makeRepayment(newRepaymentTransaction,
                defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds, isRecoveryRepayment,
                scheduleGeneratorDTO, currentUser, isHolidayValidationDone, loanHistoryRepo, loan);

        if (triggeredBy != null) {
            newRepaymentTransaction.setAppUser(currentUser);
        }
        XirrHistoryDetails xirrHistoryDetails = new XirrHistoryDetails(loan, newRepaymentTransaction.getTransactionDate(), LoanTransactionType.REPAYMENT.getValue(), loan.getXirrValue(), newRepaymentTransaction.getAmount(currency).getAmount());
        loan.getXirrHistoryDetails().add(xirrHistoryDetails);

        // updating Loan overDue For Back Date Disbursement cases Ageing
        loan.getLoanSummary().updateLoanArrearAgeing(loan);

        loan.setValueDate(Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // Reassigning max dpd
        Integer maxDpd = loan.getRepaymentScheduleInstallments().stream()
                .map(LoanRepaymentScheduleInstallment::getDaysPastDue)
                .filter(Objects::nonNull).max(Integer::compare)
                .orElse(0);
        loan.setDaysPastDue(maxDpd);

        // Closure Entry for accrual when loan is closed
        if (loan.getLoanStatus().intValue() == 600) {
            int size = loan.getRepaymentScheduleInstallments().size();
            Optional<LoanRepaymentScheduleInstallment> installment = loan.getRepaymentScheduleInstallments()
                    .stream()
                    .sorted(Comparator.comparingInt(LoanRepaymentScheduleInstallment::getInstallmentNumber).reversed()).findFirst();

            if (installment.isPresent()) {
                // To support backdated cases
                Date calculatedOn = transactionDate.isAfter(installment.get().getDueDate())
                        ? DateUtils.convertLocalDateToDate(transactionDate)
                        : DateUtils.convertLocalDateToDate(installment.get().getDueDate());

                loanAccrualRepository.save(new LoanAccrual(loan.getId(),size-1,LoanAccrualType.DAILY.getValue(), calculatedOn, calculatedOn,
                        BigDecimal.ZERO,loan.getSummary().getTotalInterestRepaid(),loan.getSummary().getTotalInterestRepaid(),BigDecimal.ZERO,BigDecimal.ZERO,
                        BigDecimal.ZERO,loan.getSummary().getTotalSelfInterestRepaid(),loan.getSummary().getTotalSelfInterestRepaid(),BigDecimal.ZERO,BigDecimal.ZERO,
                        BigDecimal.ZERO,loan.getSummary().getTotalPartnerInterestRepaid(),loan.getSummary().getTotalPartnerInterestRepaid(),BigDecimal.ZERO,BigDecimal.ZERO, false, new Date()));
            }

        }
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        boolean isEmiFullyPaid = loan.getRepaymentScheduleInstallments().stream().anyMatch(loanRepaymentScheduleInstallment ->
                loanRepaymentScheduleInstallment.getInstallmentNumber().equals(installmentNumber) && loanRepaymentScheduleInstallment.isObligationsMet());
        if (isEmiFullyPaid) {
            loanDpdHistoryRepository.getByLoanIdAndInstallmentAndDpdOnDateGreaterThanEqual(loan.getId(), installmentNumber,
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
                constructEntityMap(BusinessEventNotificationConstants.BusinessEntity.LOAN_TRANSACTION, newRepaymentTransaction));

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
        if (installmentNumber > 1) {
            final LoanRepaymentScheduleInstallment previousInstallment = loan.fetchRepaymentScheduleInstallment(installmentNumber - 1);

            if (previousInstallment != null && (previousInstallment.isNotFullyPaidOff() || previousInstallment.isPartlyPaid())) {
                throw new InstallemtException("error.msg.repayment.loan.duplicate.installment",
                        "Installment Number " + LatestNotPaidInstallment.getInstallmentNumber() + " is not fully paid. Unable to Process this Record ", previousInstallment.getInstallmentNumber());
            }
//            } else if (previousInstallment!= null && previousInstallment.isPartlyPaid()) {
//                throw new InstallemtException("error.msg.repayment.loan.duplicate.installment",
//                        "Installment Number "+ LatestNotPaidInstallment.getInstallmentNumber()  + "is not fully paid. Unable to Process this Record ", previousInstallment.getInstallmentNumber());
//            }
        }
        final LoanRepaymentScheduleInstallment currentInstallment = loan.fetchRepaymentScheduleInstallment(installmentNumber);
        if (!currentInstallment.isNotFullyPaidOff()) {
            if (currentInstallment.getInstallmentNumber() == installmentNumber) {
                throw new InstallemtException("error.msg.repayment.loan.duplicate.installment",
                        "Loan Repayment with InstallmetNumber  " + installmentNumber + "  Already Paid. Unable To process This Record ", installmentNumber);
            }
        }
    }

    private BusinessEventNotificationConstants.BusinessEvents getRepaymentTypeBusinessEvent(LoanTransactionType repaymentTransactionType, boolean isRecoveryRepayment) {
        BusinessEventNotificationConstants.BusinessEvents repaymentTypeEvent = null;
        if (repaymentTransactionType.isRepayment()) {
            repaymentTypeEvent = BusinessEventNotificationConstants.BusinessEvents.LOAN_MAKE_REPAYMENT;
        } else if (repaymentTransactionType.isMerchantIssuedRefund()) {
            repaymentTypeEvent = BusinessEventNotificationConstants.BusinessEvents.LOAN_MERCHANT_ISSUED_REFUND;
        } else if (repaymentTransactionType.isPayoutRefund()) {
            repaymentTypeEvent = BusinessEventNotificationConstants.BusinessEvents.LOAN_PAYOUT_REFUND;
        } else if (repaymentTransactionType.isGoodwillCredit()) {
            repaymentTypeEvent = BusinessEventNotificationConstants.BusinessEvents.LOAN_GOODWILL_CREDIT;
        } else if (isRecoveryRepayment) {
            repaymentTypeEvent = BusinessEventNotificationConstants.BusinessEvents.LOAN_RECOVERY_PAYMENT;
        }
        return repaymentTypeEvent;
    }

    private void checkClientOrGroupActive(Loan loan) {
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

    private Map<BusinessEventNotificationConstants.BusinessEntity, Object> constructEntityMap(final BusinessEventNotificationConstants.BusinessEntity entityEvent, Object entity) {
        Map<BusinessEventNotificationConstants.BusinessEntity, Object> map = new HashMap<>(1);
        map.put(entityEvent, entity);
        return map;
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
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

    public void recalculateAccruals(Loan loan) {
        boolean isInterestCalcualtionHappened = loan.repaymentScheduleDetail().isInterestRecalculationEnabled();
        recalculateAccruals(loan, isInterestCalcualtionHappened);
    }

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

    private BigDecimal retriveAdvanceAmountFromRepaymentSchedule(Loan loan) {
        return loan.getRepaymentScheduleInstallments()
                .stream()
                .map(LoanRepaymentScheduleInstallment::getAdvanceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateLoanTransaction(final Long loanTransactionId, final LoanTransaction newLoanTransaction) {
        final AccountTransferTransaction transferTransaction = this.accountTransferRepository.findByToLoanTransactionId(loanTransactionId);
        if (transferTransaction != null) {
            transferTransaction.updateToLoanTransaction(newLoanTransaction);
            this.accountTransferRepository.save(transferTransaction);
        }
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

    private ChangedTransactionDetail makeRepayment(final LoanTransaction repaymentTransaction,
                                                   final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
                                                   final List<Long> existingReversedTransactionIds, boolean isRecoveryRepayment, final ScheduleGeneratorDTO scheduleGeneratorDTO,
                                                   final AppUser currentUser, Boolean isHolidayValidationDone, LoanHistoryRepo loanHistoryRepo, Loan loan) {
        HolidayDetailDTO holidayDetailDTO = null;
        LoanEvent event = null;
        if (isRecoveryRepayment) {
            event = LoanEvent.LOAN_RECOVERY_PAYMENT;
        } else {
            event = LoanEvent.LOAN_REPAYMENT_OR_WAIVER;
        }
        if (!isHolidayValidationDone) {
            holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
        }
        validateAccountStatus(event, loan);
        validateActivityNotBeforeClientOrGroupTransferDate(event, repaymentTransaction.getTransactionDate(),loan);
        validateActivityNotBeforeLastTransactionDate(event, repaymentTransaction.getTransactionDate(),loan);

        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());

        final ChangedTransactionDetail changedTransactionDetail = handleRepaymentOrRecoveryOrWaiverTransaction(repaymentTransaction,
                loanLifecycleStateMachine, null, scheduleGeneratorDTO, currentUser, loanHistoryRepo,loan);

        return changedTransactionDetail;
    }

    public void validateAccountStatus(final LoanEvent event, Loan loan) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        switch (event) {
            case LOAN_CREATED:
                break;
            case LOAN_APPROVED:
                if (!loan.isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan Account Approval is not allowed. Loan Account is not in submitted and pending approval state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.approve.account.is.not.submitted.and.pending.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_APPROVAL_UNDO:
                if (!loan.isApproved()) {
                    final String defaultUserMessage = "Loan Account Undo Approval is not allowed. Loan Account is not in approved state.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.undo.approval.account.is.not.approved",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_DISBURSED:
                if ((!(loan.isApproved() && loan.isNotDisbursed()) && !loan.getLoanProduct().isMultiDisburseLoan())
                        || (loan.getLoanProduct().isMultiDisburseLoan() && !loan.isAllTranchesNotDisbursed())) {
                    final String defaultUserMessage = "Loan Disbursal is not allowed. Loan Account is not in approved and not disbursed state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.disbursal.account.is.not.approve.not.disbursed.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_DISBURSAL_UNDO:
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Undo disbursal is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.undo.disbursal.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                if (loan.isOpen() && loan.isTopup()) {
                    final String defaultUserMessage = "Loan Undo disbursal is not allowed on Topup Loans";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.disbursal.not.allowed.on.topup.loan", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_REPAYMENT_OR_WAIVER:
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Repayment (or its types) or Waiver is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.repayment.or.waiver.account.is.not.active", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_REJECTED:
                if (!loan.isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan application cannot be rejected. Loan Account is not in Submitted and Pending approval state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.reject.account.is.not.submitted.pending.approval.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_WITHDRAWN:
                if (!loan.isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan application cannot be withdrawn. Loan Account is not in Submitted and Pending approval state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.withdrawn.account.is.not.submitted.pending.approval.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case WRITE_OFF_OUTSTANDING:
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Written off is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.writtenoff.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case WRITE_OFF_OUTSTANDING_UNDO:
                if (!loan.isClosedWrittenOff()) {
                    final String defaultUserMessage = "Loan Undo Written off is not allowed. Loan Account is not Written off.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.writtenoff.account.is.not.written.off", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case REPAID_IN_FULL:
                break;
            case LOAN_CHARGE_PAYMENT:
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Charge payment is not allowed. Loan Account is not Active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.charge.payment.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_CLOSED:
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Closing Loan Account is not allowed. Loan Account is not Active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.close.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_EDIT_MULTI_DISBURSE_DATE:
                if (loan.isClosed()) {
                    final String defaultUserMessage = "Edit disbursement is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.edit.disbursement.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_RECOVERY_PAYMENT:
                if (!loan.isClosedWrittenOff()) {
                    final String defaultUserMessage = "Recovery repayments may only be made on loans which are written off";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.account.is.not.written.off",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_REFUND:
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Refund is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.refund.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_DISBURSAL_UNDO_LAST:
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Undo last disbursal is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.last.disbursal.account.is.not.active", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_FORECLOSURE:
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan foreclosure is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.foreclosure.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            case LOAN_CREDIT_BALANCE_REFUND:
                if (!loan.status().isOverpaid()) {
                    final String defaultUserMessage = "Loan Credit Balance Refund is not allowed. Loan Account is not Overpaid.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.credit.balance.refund.account.is.not.overpaid", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                break;
            default:
                break;
        }

    }

    private void validateActivityNotBeforeClientOrGroupTransferDate(final LoanEvent event, final LocalDate activityDate,Loan loan) {
        if (loan.getClient() != null && loan.getClient().getOfficeJoiningLocalDate() != null) {
            final LocalDate clientOfficeJoiningDate = loan.getClient().getOfficeJoiningLocalDate();
            if (activityDate.isBefore(clientOfficeJoiningDate)) {
                String errorMessage = null;
                String action = null;
                String postfix = null;
                switch (event) {
                    case LOAN_CREATED:
                        errorMessage = "The date on which a loan is submitted cannot be earlier than client's transfer date to this office";
                        action = "submittal";
                        postfix = "cannot.be.before.client.transfer.date";
                        break;
                    case LOAN_APPROVED:
                        errorMessage = "The date on which a loan is approved cannot be earlier than client's transfer date to this office";
                        action = "approval";
                        postfix = "cannot.be.before.client.transfer.date";
                        break;
                    case LOAN_APPROVAL_UNDO:
                        errorMessage = "The date on which a loan is approved cannot be earlier than client's transfer date to this office";
                        action = "approval";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                        break;
                    case LOAN_DISBURSED:
                        errorMessage = "The date on which a loan is disbursed cannot be earlier than client's transfer date to this office";
                        action = "disbursal";
                        postfix = "cannot.be.before.client.transfer.date";
                        break;
                    case LOAN_DISBURSAL_UNDO:
                        errorMessage = "Cannot undo a disbursal done in another branch";
                        action = "disbursal";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                        break;
                    case LOAN_REPAYMENT_OR_WAIVER:
                        errorMessage = "The date on which a repayment or waiver is made cannot be earlier than client's transfer date to this office";
                        action = "repayment.or.waiver";
                        postfix = "cannot.be.made.before.client.transfer.date";
                        break;
                    case LOAN_REJECTED:
                        errorMessage = "The date on which a loan is rejected cannot be earlier than client's transfer date to this office";
                        action = "reject";
                        postfix = "cannot.be.before.client.transfer.date";
                        break;
                    case LOAN_WITHDRAWN:
                        errorMessage = "The date on which a loan is withdrawn cannot be earlier than client's transfer date to this office";
                        action = "withdraw";
                        postfix = "cannot.be.before.client.transfer.date";
                        break;
                    case WRITE_OFF_OUTSTANDING:
                        errorMessage = "The date on which a write off is made cannot be earlier than client's transfer date to this office";
                        action = "writeoff";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                        break;
                    case REPAID_IN_FULL:
                        errorMessage = "The date on which the loan is repaid in full cannot be earlier than client's transfer date to this office";
                        action = "close";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                        break;
                    case LOAN_CHARGE_PAYMENT:
                        errorMessage = "The date on which a charge payment is made cannot be earlier than client's transfer date to this office";
                        action = "charge.payment";
                        postfix = "cannot.be.made.before.client.transfer.date";
                        break;
                    case LOAN_REFUND:
                        errorMessage = "The date on which a refund is made cannot be earlier than client's transfer date to this office";
                        action = "refund";
                        postfix = "cannot.be.made.before.client.transfer.date";
                        break;
                    case LOAN_DISBURSAL_UNDO_LAST:
                        errorMessage = "Cannot undo a last disbursal in another branch";
                        action = "disbursal";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                        break;
                    default:
                        break;
                }
                throw new InvalidLoanStateTransitionException(action, postfix, errorMessage, clientOfficeJoiningDate);
            }
        }
    }

    private void validateActivityNotBeforeLastTransactionDate(final LoanEvent event, final LocalDate activityDate,Loan loan) {
        if (!(loan.repaymentScheduleDetail().isInterestRecalculationEnabled() || loan.loanProduct().isHoldGuaranteeFundsEnabled())) {
            return;
        }
        LocalDate lastTransactionDate = loan.getLastUserTransactionDate();
        if (lastTransactionDate.isAfter(activityDate)) {
            String errorMessage = null;
            String action = null;
            String postfix = null;
            switch (event) {
                case LOAN_REPAYMENT_OR_WAIVER:
                    errorMessage = "The date on which a repayment or waiver is made cannot be earlier than last transaction date";
                    action = "repayment.or.waiver";
                    postfix = "cannot.be.made.before.last.transaction.date";
                    break;
                case WRITE_OFF_OUTSTANDING:
                    errorMessage = "The date on which a write off is made cannot be earlier than last transaction date";
                    action = "writeoff";
                    postfix = "cannot.be.made.before.last.transaction.date";
                    break;
                case LOAN_CHARGE_PAYMENT:
                    errorMessage = "The date on which a charge payment is made cannot be earlier than last transaction date";
                    action = "charge.payment";
                    postfix = "cannot.be.made.before.last.transaction.date";
                    break;
                default:
                    break;
            }
            throw new InvalidLoanStateTransitionException(action, postfix, errorMessage, lastTransactionDate);
        }
    }


    private ChangedTransactionDetail handleRepaymentOrRecoveryOrWaiverTransaction(final LoanTransaction loanTransaction,
                                                                                  final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction adjustedTransaction,
                                                                                  final ScheduleGeneratorDTO scheduleGeneratorDTO, final AppUser currentUser, LoanHistoryRepo loanHistoryRepo,Loan loan) {
        ChangedTransactionDetail changedTransactionDetail = null;

        LoanStatus statusEnum = null;

        LocalDate recalculateFrom = loanTransaction.getTransactionDate();
        if (adjustedTransaction != null && adjustedTransaction.getTransactionDate().isBefore(recalculateFrom)) {
            recalculateFrom = adjustedTransaction.getTransactionDate();
        }
        statusEnum = loanTransaction.isRecoveryRepayment() ? loanLifecycleStateMachine.transition(LoanEvent.LOAN_RECOVERY_PAYMENT, LoanStatus.fromInt(loan.getLoanStatus()))
                : loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, LoanStatus.fromInt(loan.getLoanStatus()));


        if (loanTransaction.isRecoveryRepayment()
                && loanTransaction.getAmount(loan.getCurrency()).getAmount().compareTo(loan.getSummary().getTotalWrittenOff()) > 0) {
            final String errorMessage = "The transaction amount cannot greater than the remaining written off amount.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.written.off", errorMessage);
        }
        loan.setLoanStatus(statusEnum.getValue());

        loanTransaction.updateLoan(loan);

        if (!loanTransaction.getEvent().equals("loan_adjusted_transaction")) {
            loan.addLoanTransaction(loanTransaction);
        }

        if (loanTransaction.isNotRepaymentType() && loanTransaction.isNotWaiver() && loanTransaction.isNotRecoveryRepayment()) {
            final String errorMessage = "A transaction of type repayment or recovery repayment or waiver was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.repayment.or.waiver.or.recovery.transaction",
                    errorMessage);
        }

        final LocalDate loanTransactionDate = loanTransaction.getTransactionDate();
        if (loanTransactionDate.isBefore(loan.getDisbursementDate())) {
            final String errorMessage = "The transaction date cannot be before the loan disbursement date: "
                    + loan.getApprovedOnDate().toString();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.before.disbursement.date", errorMessage,
                    loanTransactionDate, loan.getDisbursementDate());
        }

        if (loanTransactionDate.isAfter(DateUtils.getLocalDateOfTenant())) {
            final String errorMessage = "The transaction date cannot be in the future.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.a.future.date", errorMessage, loanTransactionDate);
        }

        if (loanTransaction.isInterestWaiver()) {
            Money totalInterestOutstandingOnLoan = getTotalInterestOutstandingOnLoan(loan);
            if (adjustedTransaction != null) {
                totalInterestOutstandingOnLoan = totalInterestOutstandingOnLoan.plus(adjustedTransaction.getAmount(loan.getCurrency()));
            }
            if (loanTransaction.getAmount(loan.getCurrency()).isGreaterThan(totalInterestOutstandingOnLoan)) {
                final String errorMessage = "The amount of interest to waive cannot be greater than total interest outstanding on loan.";
                throw new InvalidLoanStateTransitionException("waive.interest", "amount.exceeds.total.outstanding.interest", errorMessage,
                        loanTransaction.getAmount(loan.getCurrency()), totalInterestOutstandingOnLoan.getAmount());
            }
        }

        if (loan.getLoanProduct().isMultiDisburseLoan() && adjustedTransaction == null) {
            BigDecimal totalDisbursed = loan.getDisbursedAmount();
            if (totalDisbursed.compareTo(loan.getLoanSummary().getTotalPrincipalRepaid()) < 0) {
                final String errorMessage = "The transaction cannot be done before the loan disbursement: "
                        + loan.getApprovedOnDate().toString();
                throw new InvalidLoanStateTransitionException("transaction", "cannot.be.done.before.disbursement", errorMessage);
            }
        }
        boolean reprocess = true;
        if (reprocess) {
            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO, currentUser,loan);
            }

            final List<LoanTransaction> pendingTransaction = Collectionutills.getAdvanceLoanTransaction(loan, loanTransaction);
            pendingTransaction.add(loanTransaction);

            collectionservice.appropriateInstallment(loan, loanTransactionDate, pendingTransaction, loanHistoryRepo);

        }

        loan.setEvent("collection");
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement =loan.retreiveListOfTransactionsPostDisbursement();

        if (loanTransaction.getAppUser().isSystemUser()) {
            loan.getLoanTransactions().remove(loanTransaction);
        }

        // calculating the Xirr value
        loan.updateXirrValue(XirrService.xirrCalculation(loan.getRepaymentScheduleInstallments(), loan.getPrincpal().getAmount(), loan.getDisbursementDate(),
                loan.getCurrency(), loan.getLoanCharges(), allNonContraTransactionsPostDisbursement, loan));

        loan.updateLoanSummaryDerivedFields();
        //calculating the ServicerFee from loanTransaction if the transaction already present it will not calculate it ignore the loan Transaction
        ServicerFeeWritePlatformService.calculateServicerFee(loan.getLoanTransactions(), loan.getLoanProduct(), loan.getCurrency(), loan);
        if (loanTransaction.isNotRecoveryRepayment()) {
            loan.doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
        }

        if (loan.getLoanProduct().isMultiDisburseLoan()) {
            BigDecimal totalDisbursed =loan.getDisbursedAmount();
            if (totalDisbursed.compareTo(loan.getLoanSummary().getTotalPrincipalRepaid()) < 0
                    && loan.repaymentScheduleDetail().getPrincipal().minus(totalDisbursed).isGreaterThanZero()) {
                final String errorMessage = "The transaction cannot be done before the loan disbursement: "
                        + loan.getApprovedOnDate().toString();
                throw new InvalidLoanStateTransitionException("transaction", "cannot.be.done.before.disbursement", errorMessage);
            }
        }

        if (changedTransactionDetail != null) {
            loan.getLoanTransactions().removeAll(changedTransactionDetail.getNewTransactionMappings().values());
        }
        return changedTransactionDetail;
    }

    private Money getTotalInterestOutstandingOnLoan(Loan loan) {
        Money cumulativeInterest = Money.zero(loan.getCurrency());

        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : installments) {
            cumulativeInterest = cumulativeInterest.plus(scheduledRepayment.getInterestOutstanding(loan.getCurrency()));
        }

        return cumulativeInterest;
    }

    public void regenerateRepaymentScheduleWithInterestRecalculation(final ScheduleGeneratorDTO generatorDTO, final AppUser currentUser,Loan loan) {

        LocalDate lastTransactionDate = loan.getLastUserTransactionDate();
        final LoanScheduleDTO loanSchedule =loan.getRecalculatedSchedule(generatorDTO);
        if (loanSchedule == null) {
            return;
        }
        loan.updateLoanSchedule(loanSchedule.getInstallments(), currentUser);
        loan.setInterestRecalculatedOn(DateUtils.getDateOfTenant());
        LocalDate lastRepaymentDate = loan.getLastRepaymentPeriodDueDate(true);
        List<LoanCharge> charges = loan.charges();
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isDueAtDisbursement()) {
               loan.updateOverdueScheduleInstallment(loanCharge);
                if (loanCharge.getDueLocalDate() == null || !lastRepaymentDate.isBefore(loanCharge.getDueLocalDate())) {
                    if ((loanCharge.isInstalmentFee() || !loanCharge.isWaived())
                            && (loanCharge.getDueLocalDate() == null || !lastTransactionDate.isAfter(loanCharge.getDueLocalDate()))) {
                        loan.recalculateLoanCharge(loanCharge, generatorDTO.getPenaltyWaitPeriod());
                        loanCharge.updateWaivedAmount(loan.getCurrency());
                    }
                } else {
                    loanCharge.setActive(false);
                }
            }
        }

       loan.processPostDisbursementTransactions();
        loan.processIncomeTransactions(currentUser);
    }
}
