/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.loanaccount.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vcpl.lms.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.bulkimport.constants.LoanConstants;
import org.vcpl.lms.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.codes.service.CodeValueReadPlatformService;
import org.vcpl.lms.infrastructure.configuration.domain.ConfigurationDomainService;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.dataqueries.data.EntityTables;
import org.vcpl.lms.infrastructure.dataqueries.data.StatusEnum;
import org.vcpl.lms.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.vcpl.lms.infrastructure.jobs.annotation.CronTarget;
import org.vcpl.lms.infrastructure.jobs.exception.JobExecutionException;
import org.vcpl.lms.infrastructure.jobs.service.JobName;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.holiday.domain.Holiday;
import org.vcpl.lms.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrency;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.organisation.office.domain.Office;
import org.vcpl.lms.organisation.office.domain.OfficeRepository;
import org.vcpl.lms.organisation.office.service.OfficeReadPlatformServiceImpl;
import org.vcpl.lms.organisation.staff.domain.Staff;
import org.vcpl.lms.organisation.teller.data.CashierTransactionDataValidator;
import org.vcpl.lms.organisation.workingdays.domain.WorkingDays;
import org.vcpl.lms.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.BulkUploadConstants;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.RepaymentRequest;
import org.vcpl.lms.portfolio.loanaccount.data.*;
import org.vcpl.lms.portfolio.loanaccount.domain.XirrHistoryDetails;
import org.vcpl.lms.portfolio.loanaccount.domain.XirrHistoryDetailsRepository;
import org.vcpl.lms.portfolio.account.PortfolioAccountType;
import org.vcpl.lms.portfolio.account.data.AccountTransferDTO;
import org.vcpl.lms.portfolio.account.data.PortfolioAccountData;
import org.vcpl.lms.portfolio.account.domain.AccountAssociationType;
import org.vcpl.lms.portfolio.account.domain.AccountAssociations;
import org.vcpl.lms.portfolio.account.domain.AccountAssociationsRepository;
import org.vcpl.lms.portfolio.account.domain.AccountTransferDetailRepository;
import org.vcpl.lms.portfolio.account.domain.AccountTransferDetails;
import org.vcpl.lms.portfolio.account.domain.AccountTransferRecurrenceType;
import org.vcpl.lms.portfolio.account.domain.AccountTransferRepository;
import org.vcpl.lms.portfolio.account.domain.AccountTransferStandingInstruction;
import org.vcpl.lms.portfolio.account.domain.AccountTransferTransaction;
import org.vcpl.lms.portfolio.account.domain.AccountTransferType;
import org.vcpl.lms.portfolio.account.domain.StandingInstructionPriority;
import org.vcpl.lms.portfolio.account.domain.StandingInstructionStatus;
import org.vcpl.lms.portfolio.account.domain.StandingInstructionType;
import org.vcpl.lms.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.vcpl.lms.portfolio.account.service.AccountTransfersReadPlatformService;
import org.vcpl.lms.portfolio.account.service.AccountTransfersWritePlatformService;
import org.vcpl.lms.portfolio.accountdetails.domain.AccountType;
import org.vcpl.lms.portfolio.address.service.AddressReadPlatformServiceImpl;
import org.vcpl.lms.portfolio.calendar.domain.Calendar;
import org.vcpl.lms.portfolio.calendar.domain.CalendarEntityType;
import org.vcpl.lms.portfolio.calendar.domain.CalendarInstance;
import org.vcpl.lms.portfolio.calendar.domain.CalendarInstanceRepository;
import org.vcpl.lms.portfolio.calendar.domain.CalendarRepository;
import org.vcpl.lms.portfolio.calendar.domain.CalendarType;
import org.vcpl.lms.portfolio.calendar.exception.CalendarParameterUpdateNotSupportedException;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.charge.domain.ChargePaymentMode;
import org.vcpl.lms.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.vcpl.lms.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBeDeletedException;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBeDeletedException.LoanChargeCannotBeDeletedReason;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBePayedException;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBeUpdatedException;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBeUpdatedException.LoanChargeCannotBeUpdatedReason;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBeWaivedException;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBeWaivedException.LoanChargeCannotBeWaivedReason;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeNotFoundException;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeWaiveCannotBeReversedException;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeWaiveCannotBeReversedException.LoanChargeWaiveCannotUndoReason;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.client.domain.ClientRepositoryWrapper;
import org.vcpl.lms.portfolio.client.exception.ClientNotActiveException;
import org.vcpl.lms.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.vcpl.lms.portfolio.collateralmanagement.exception.LoanCollateralAmountNotSufficientException;
import org.vcpl.lms.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.vcpl.lms.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.vcpl.lms.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.vcpl.lms.portfolio.collectionsheet.command.SingleRepaymentCommand;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants.BusinessEntity;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants.BusinessEvents;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.common.service.BusinessEventNotifierService;
import org.vcpl.lms.portfolio.group.domain.Group;
import org.vcpl.lms.portfolio.group.exception.GroupNotActiveException;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.command.LoanUpdateCommand;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.domain.xirr.XirrService;
import org.vcpl.lms.portfolio.loanaccount.exception.*;
import org.vcpl.lms.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.ScheduledDateGenerator;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.vcpl.lms.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.vcpl.lms.portfolio.loanaccount.serialization.LoanApplicationCommandFromApiJsonHelper;
import org.vcpl.lms.portfolio.loanaccount.serialization.LoanEventApiJsonValidator;
import org.vcpl.lms.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeAmountFormulaCalculation;
import org.vcpl.lms.portfolio.loanproduct.data.LoanOverdueDTO;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesCharges;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesRepository;
import org.vcpl.lms.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.vcpl.lms.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.vcpl.lms.portfolio.note.domain.Note;
import org.vcpl.lms.portfolio.note.domain.NoteRepository;
import org.vcpl.lms.portfolio.partner.domain.Partner;
import org.vcpl.lms.portfolio.partner.domain.PartnerRepository;
import org.vcpl.lms.portfolio.partner.exception.PartnerNotFoundException;
import org.vcpl.lms.portfolio.paymentdetail.domain.PaymentDetail;
import org.vcpl.lms.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.vcpl.lms.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksAssembler;
import org.vcpl.lms.portfolio.savings.domain.SavingsAccount;
import org.vcpl.lms.portfolio.transfer.api.TransferApiConstants;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.useradministration.domain.AppUserRepository;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final LoanEventApiJsonValidator loanEventApiJsonValidator;
    private final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAccountDomainService loanAccountDomainService;
    private final NoteRepository noteRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAssembler loanAssembler;
    private final ChargeRepositoryWrapper chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final AccountTransferRepository accountTransferRepository;
    private final CalendarRepository calendarRepository;
    private final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository;
    private final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService;
    private final LoanApplicationCommandFromApiJsonHelper loanApplicationCommandFromApiJsonHelper;
    private final AccountAssociationsRepository accountAssociationRepository;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final GuarantorDomainService guarantorDomainService;
    private final LoanUtilService loanUtilService;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessingStrategy;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final CashierTransactionDataValidator cashierTransactionDataValidator;
    private final GLIMAccountInfoRepository glimRepository;
    private final LoanRepository loanRepository;
    private final RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler;
    private final PostDatedChecksRepository postDatedChecksRepository;
    private final LoanChargePaidByRepository loanChargePaidByRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final LoanChargeRepositoryWrapper loanChargeRepositoryWrapper;
    private final AddressReadPlatformServiceImpl addressReadPlatformService;
    private final PartnerRepository partnerRepository;
    private final XirrHistoryDetailsRepository xirrHistoryDetailsRepository;
    private final OfficeRepository officeRepository;
    private final AppUserRepository appUserRepository;
    private final LoanProductFeesChargesRepository loanProductFeesChargesRepository;
    private final OfficeReadPlatformServiceImpl officeReadPlatformServiceImpl;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanPenalForeclosureChargesRepository loanPenalForeclosureChargesRepository;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    private final CodeValueReadPlatformService codeValueReadPlatformService;

    private final LoanDpdHistoryRepository loanDpdHistoryRepository;

    private final LoanHistoryRepo loanHistoryRepo;

    private final GstService gstService;
    private final RefreshRepaymentSchedule refreshRepaymentSchedule;

    @Autowired
    public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
                                                     final LoanEventApiJsonValidator loanEventApiJsonValidator,
                                                     final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer, final LoanAssembler loanAssembler,
                                                     final LoanAccountDomainService loanAccountDomainService, final LoanTransactionRepository loanTransactionRepository,
                                                     final NoteRepository noteRepository, final ChargeRepositoryWrapper chargeRepository,
                                                     final LoanChargeRepository loanChargeRepository, final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
                                                     final JournalEntryWritePlatformService journalEntryWritePlatformService,
                                                     final CalendarInstanceRepository calendarInstanceRepository,
                                                     final PaymentDetailWritePlatformService paymentDetailWritePlatformService, final HolidayRepositoryWrapper holidayRepository,
                                                     final ConfigurationDomainService configurationDomainService, final WorkingDaysRepositoryWrapper workingDaysRepository,
                                                     final AccountTransfersWritePlatformService accountTransfersWritePlatformService,
                                                     final AccountTransfersReadPlatformService accountTransfersReadPlatformService,
                                                     final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
                                                     final LoanChargeReadPlatformService loanChargeReadPlatformService, final LoanReadPlatformService loanReadPlatformService,
                                                     final FromJsonHelper fromApiJsonHelper, final AccountTransferRepository accountTransferRepository,
                                                     final CalendarRepository calendarRepository,
                                                     final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository,
                                                     final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService,
                                                     final LoanApplicationCommandFromApiJsonHelper loanApplicationCommandFromApiJsonHelper,
                                                     final AccountAssociationsRepository accountAssociationRepository,
                                                     final AccountTransferDetailRepository accountTransferDetailRepository,
                                                     final BusinessEventNotifierService businessEventNotifierService, final GuarantorDomainService guarantorDomainService,
                                                     final LoanUtilService loanUtilService, final LoanSummaryWrapper loanSummaryWrapper,
                                                     final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService,
                                                     final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessingStrategy,
                                                     final CodeValueRepositoryWrapper codeValueRepository, final LoanRepositoryWrapper loanRepositoryWrapper,
                                                     final CashierTransactionDataValidator cashierTransactionDataValidator, final GLIMAccountInfoRepository glimRepository,
                                                     final LoanRepository loanRepository, final RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler,
                                                     final PostDatedChecksRepository postDatedChecksRepository, final LoanChargePaidByRepository loanChargePaidByRepository,
                                                     final ClientRepositoryWrapper clientRepositoryWrapper,
                                                     final LoanChargeRepositoryWrapper loanChargeRepositoryWrapper, final AddressReadPlatformServiceImpl addressReadPlatformService,
                                                     final PartnerRepository partnerRepository, final XirrHistoryDetailsRepository xirrHistoryDetailsRepository,
                                                     final OfficeRepository officeRepository, final AppUserRepository appUserRepository,
                                                     final LoanProductFeesChargesRepository loanProductFeesChargesRepository,
                                                     final OfficeReadPlatformServiceImpl officeReadPlatformServiceImpl, final LoanChargeAssembler loanChargeAssembler,
                                                     final LoanPenalForeclosureChargesRepository loanPenalForeclosureChargesRepository, final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                                                     final CodeValueReadPlatformService codeValueReadPlatformService, final LoanDpdHistoryRepository loanDpdHistoryRepository, LoanHistoryRepo loanHistoryRepo, GstService gstService, RefreshRepaymentSchedule refreshRepaymentSchedule) {

        this.context = context;
        this.loanEventApiJsonValidator = loanEventApiJsonValidator;
        this.loanAssembler = loanAssembler;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.loanAccountDomainService = loanAccountDomainService;
        this.loanTransactionRepository = loanTransactionRepository;
        this.noteRepository = noteRepository;
        this.chargeRepository = chargeRepository;
        this.loanChargeRepository = loanChargeRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.loanUpdateCommandFromApiJsonDeserializer = loanUpdateCommandFromApiJsonDeserializer;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.paymentDetailWritePlatformService = paymentDetailWritePlatformService;
        this.holidayRepository = holidayRepository;
        this.configurationDomainService = configurationDomainService;
        this.workingDaysRepository = workingDaysRepository;
        this.accountTransfersWritePlatformService = accountTransfersWritePlatformService;
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
        this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.loanReadPlatformService = loanReadPlatformService;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.accountTransferRepository = accountTransferRepository;
        this.calendarRepository = calendarRepository;
        this.repaymentScheduleInstallmentRepository = repaymentScheduleInstallmentRepository;
        this.loanScheduleHistoryWritePlatformService = loanScheduleHistoryWritePlatformService;
        this.loanApplicationCommandFromApiJsonHelper = loanApplicationCommandFromApiJsonHelper;
        this.accountAssociationRepository = accountAssociationRepository;
        this.accountTransferDetailRepository = accountTransferDetailRepository;
        this.businessEventNotifierService = businessEventNotifierService;
        this.guarantorDomainService = guarantorDomainService;
        this.loanUtilService = loanUtilService;
        this.loanSummaryWrapper = loanSummaryWrapper;
        this.transactionProcessingStrategy = transactionProcessingStrategy;
        this.entityDatatableChecksWritePlatformService = entityDatatableChecksWritePlatformService;
        this.codeValueRepository = codeValueRepository;
        this.cashierTransactionDataValidator = cashierTransactionDataValidator;
        this.loanRepository = loanRepository;
        this.glimRepository = glimRepository;
        this.repaymentWithPostDatedChecksAssembler = repaymentWithPostDatedChecksAssembler;
        this.postDatedChecksRepository = postDatedChecksRepository;
        this.loanChargePaidByRepository = loanChargePaidByRepository;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.loanChargeRepositoryWrapper = loanChargeRepositoryWrapper;
        this.addressReadPlatformService = addressReadPlatformService;
        this.partnerRepository = partnerRepository;
        this.xirrHistoryDetailsRepository = xirrHistoryDetailsRepository;
        this.appUserRepository = appUserRepository;
        this.officeRepository = officeRepository;
        this.loanProductFeesChargesRepository = loanProductFeesChargesRepository;
        this.officeReadPlatformServiceImpl = officeReadPlatformServiceImpl;
        this.loanChargeAssembler = loanChargeAssembler;
        this.loanPenalForeclosureChargesRepository = loanPenalForeclosureChargesRepository;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.loanDpdHistoryRepository = loanDpdHistoryRepository;
        this.loanHistoryRepo = loanHistoryRepo;
        this.gstService = gstService;
        this.refreshRepaymentSchedule = refreshRepaymentSchedule;
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    @Override
    @Transactional
    public void updateDaysPastDuesForPaidDues() {
        List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = this.repaymentScheduleInstallmentRepository
                .getAllByObligationsMetAndDaysPastDue();
        loanRepaymentScheduleInstallments.forEach(installment -> installment.setDaysPastDue(null));
        this.repaymentScheduleInstallmentRepository.saveAllAndFlush(loanRepaymentScheduleInstallments);
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseGLIMLoan(final Long loanId, final JsonCommand command) {
        final Long parentLoanId = loanId;
        GroupLoanIndividualMonitoringAccount parentLoan = glimRepository.findById(parentLoanId).get();
        List<Loan> childLoans = this.loanRepository.findByGlimId(loanId);
        CommandProcessingResult result = null;
        int count = 0;
        for (Loan loan : childLoans) {
            result = disburseLoan(loan.getId(), command, false);
            if (result.getLoanId() != null) {
                count++;
                // if all the child loans are approved, mark the parent loan as
                // approved
                if (count == parentLoan.getChildAccountsCount()) {
                    parentLoan.setLoanStatus(LoanStatus.ACTIVE.getValue());
                    glimRepository.save(parentLoan);
                }
            }
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseLoan(final Long loanId, final JsonCommand command, Boolean isAccountTransfer) {

        final AppUser currentUser = getAppUserIfPresent();
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        final Long partnerId = loan.getPartnerId();
        final BigDecimal selfPrincipalAmount = loan.getSelfPrincipaAmount();
        final Partner partner = this.partnerRepository.findById(partnerId).orElseThrow(() -> new PartnerNotFoundException(partnerId));
        final BigDecimal partnerBalanceLimit = partner.getBalanceLimit();
        this.loanEventApiJsonValidator.validateDisbursement(command.json(), isAccountTransfer, partnerBalanceLimit);
        final LocalDate disbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        loan.loanProduct().validateDisbursementDate(disbursementDate, loan.getLoanProduct());
        if (command.parameterExists("postDatedChecks")) {
            // validate with post dated checks for the disbursement
            this.loanEventApiJsonValidator.validateDisbursementWithPostDatedChecks(command.json(), loanId);
        }
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        if (loan.loanProduct().isDisallowExpectedDisbursements()) {
            // create artificial 'tranche/expected disbursal' as current disburse code expects it for multi-disbursal
            // products
            final Date artificialExpectedDate = Date
                    .from(loan.getExpectedDisbursedOnLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            LoanDisbursementDetails disbursementDetail = new LoanDisbursementDetails(artificialExpectedDate, null,
                    loan.getDisbursedAmount(), null);
            disbursementDetail.updateLoan(loan);
            loan.getDisbursementDetails().add(disbursementDetail);
        }

        // Get disbursedAmount
        final BigDecimal disbursedAmount = loan.getDisbursedAmount();
        final Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();

        BigDecimal reducedAmount = partnerBalanceLimit.subtract(selfPrincipalAmount);
        if (reducedAmount != null) {
            partner.UpdateBalanceLimit(reducedAmount);
        } else {
            reducedAmount = BigDecimal.valueOf(0);
            partner.UpdateBalanceLimit(reducedAmount);
        }
        // Partner.setBalanceLimit(reducedAmount);

        // Get relevant loan collateral modules
        if ((loanCollateralManagements != null && loanCollateralManagements.size() != 0)
                && AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {

            BigDecimal totalCollateral = BigDecimal.valueOf(0);

            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
                BigDecimal quantity = loanCollateralManagement.getQuantity();
                BigDecimal pctToBase = loanCollateralManagement.getClientCollateralManagement().getCollaterals().getPctToBase();
                BigDecimal basePrice = loanCollateralManagement.getClientCollateralManagement().getCollaterals().getBasePrice();
                totalCollateral = totalCollateral.add(quantity.multiply(basePrice).multiply(pctToBase).divide(BigDecimal.valueOf(100)));
            }

            // Validate the loan collateral value against the disbursedAmount
            if (disbursedAmount.compareTo(totalCollateral) > 0) {
                throw new LoanCollateralAmountNotSufficientException(disbursedAmount);
            }
        }

        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");

        final LocalDate disburementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");

        refreshRepaymentSchedule.updateLoan(loan, disburementDate);

        // validate ActualDisbursement Date Against Expected Disbursement Date
        LoanProduct loanProduct = loan.loanProduct();
        if (loanProduct.syncExpectedWithDisbursementDate()) {
            syncExpectedDateWithActualDisbursementDate(loan, actualDisbursementDate);
        }
        checkClientOrGroupActive(loan);

        final LocalDate nextPossibleRepaymentDate = loan.getNextPossibleRepaymentDateForRescheduling();
        final Date rescheduledRepaymentDate = command.dateValueOfParameterNamed("actualDisbursementDate");

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                StatusEnum.DISBURSE.getCode().longValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

        LocalDate recalculateFrom = null;
        if (!loan.isMultiDisburmentLoan()) {
            loan.setActualDisbursementDate(Date.from(actualDisbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        // validate actual disbursement date against meeting date
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        if (loan.isSyncDisbursementWithMeeting()) {
            this.loanEventApiJsonValidator.validateDisbursementDateWithMeetingDate(actualDisbursementDate, calendarInstance,
                    scheduleGeneratorDTO.isSkipRepaymentOnFirstDayofMonth(), scheduleGeneratorDTO.getNumberOfdays());
        }


        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_DISBURSAL,
                constructEntityMap(BusinessEntity.LOAN, loan));

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Map<String, Object> changes = new LinkedHashMap<>();

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        if (paymentDetail != null && paymentDetail.getPaymentType() != null && paymentDetail.getPaymentType().isCashPayment()) {
            BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
            this.cashierTransactionDataValidator.validateOnLoanDisbursal(currentUser, loan.getCurrencyCode(), transactionAmount);
        }
        final Boolean isPaymnetypeApplicableforDisbursementCharge = configurationDomainService
                .isPaymnetypeApplicableforDisbursementCharge();

        // Recalculate first repayment date based in actual disbursement date.
        updateLoanCounters(loan, actualDisbursementDate);
        Money amountBeforeAdjust = loan.getPrincpal();
        loan.validateAccountStatus(LoanEvent.LOAN_DISBURSED);
        boolean canDisburse = loan.canDisburse(actualDisbursementDate);
        ChangedTransactionDetail changedTransactionDetail = null;
        if (canDisburse) {

            // Get netDisbursalAmount from disbursal screen field.
            final BigDecimal netDisbursalAmount = command
                    .bigDecimalValueOfParameterNamed(LoanApiConstants.disbursementNetDisbursalAmountParameterName);
            if (netDisbursalAmount != null) {
                loan.setNetDisbursalAmount(netDisbursalAmount);
            }
            Money disburseAmount = loan.adjustDisburseAmount(command, actualDisbursementDate);
            Money amountToDisburse = disburseAmount.copy();
            BigDecimal selfDue = loan.getSelfPrincipaAmount();
            BigDecimal partnerDue = loan.getPartnerPrincipalAmount();
            boolean recalculateSchedule = amountBeforeAdjust.isNotEqualTo(loan.getPrincpal());
            final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

            if (loan.isTopup() && loan.getClientId() != null) {
                final Long loanIdToClose = loan.getTopupLoanDetails().getLoanIdToClose();
                final Loan loanToClose = this.loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose, loan.getClientId());
                if (loanToClose == null) {
                    throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.with.topup.is.not.active",
                            "Loan to be closed with this topup is not active.");
                }
                final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
                if (loan.getDisbursementDate().isBefore(lastUserTransactionOnLoanToClose)) {
                    throw new GeneralPlatformDomainRuleException(
                            "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                            "Disbursal date of this loan application " + loan.getDisbursementDate()
                                    + " should be after last transaction date of loan to be closed " + lastUserTransactionOnLoanToClose);
                }

                BigDecimal loanOutstanding = this.loanReadPlatformService
                        .retrieveLoanPrePaymentTemplate(LoanTransactionType.REPAYMENT, loanIdToClose, actualDisbursementDate).getAmount();
                final BigDecimal firstDisbursalAmount = loan.getFirstDisbursalAmount();
                if (loanOutstanding.compareTo(firstDisbursalAmount) > 0) {
                    throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                            "Topup loan amount should be greater than outstanding amount of loan to be closed.");
                }

                amountToDisburse = disburseAmount.minus(loanOutstanding);

                disburseLoanToLoan(loan, command, loanOutstanding);
            }

            if (isAccountTransfer) {
                disburseLoanToSavings(loan, command, amountToDisburse, paymentDetail);
                existingTransactionIds.addAll(loan.findExistingTransactionIds());
                existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
            } else {
                existingTransactionIds.addAll(loan.findExistingTransactionIds());
                existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
                AppUser transactionAppUser = currentUser.isSystemUser() ? loan.getApprovedBy() : currentUser;
                LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan.getOffice(), amountToDisburse, paymentDetail,
                        actualDisbursementDate, txnExternalId, DateUtils.getLocalDateTimeOfTenant(), transactionAppUser, selfDue, partnerDue);
                disbursementTransaction.setValueDate(Date.from(actualDisbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                disbursementTransaction.updateLoan(loan);
                loan.addLoanTransaction(disbursementTransaction);
            }
            regenerateScheduleOnDisbursement(command, loan, recalculateSchedule, scheduleGeneratorDTO, nextPossibleRepaymentDate,
                    rescheduledRepaymentDate);
            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);
            }
            if (isPaymnetypeApplicableforDisbursementCharge) {
                changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO, paymentDetail);
            } else {
                changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO, null);
            }
            /*servicer fee calculation*/

            if (loan.getLoanProduct().getServicerFeeConfig() != null) {
                ServicerFeeAmountFormulaCalculation.calculateServicerFeeForCharge(loan, loan.getLoanCharges(), amountToDisburse.getAmount());
            }

            //     List<LoanCharge> loanCharge = loan.getLoanCharges().stream().filter(Objects::nonNull).filter(loanCharges -> loanCharges.isProcessingFee(loanCharges.getCharge())).collect(Collectors.toList());

            //Xirr calculation
            MonetaryCurrency currency = loan.getCurrency();
            loan.updateXirrValue(XirrService.xirrCalculation(loan.getRepaymentScheduleInstallments(), loan.getPrincpal().getAmount(),
                    loan.getDisbursementDate(), loan.getCurrency(), loan.getLoanCharges(), loan.retreiveListOfTransactionsPostDisbursement(), loan));

            XirrHistoryDetails xirrHistoryDetails = new XirrHistoryDetails(loan, loan.getDisbursementDate(), LoanTransactionType.DISBURSEMENT.getValue(), loan.getXirrValue(), BigDecimal.ZERO);
            xirrHistoryDetailsRepository.saveAndFlush(xirrHistoryDetails);

            loan.adjustNetDisbursalAmount(amountToDisburse.getAmount());
        }
        loan.getLoanSummary().updateLoanArrearAgeing(loan);
        if (!changes.isEmpty()) {
            saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }

            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }

            // auto create standing instruction
            createStandingInstruction(loan);

            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        }

        final List<LoanCharge> loanCharges = loan.charges();
        final Map<Long, BigDecimal> disBuLoanCharges = new HashMap<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueAtDisbursement() && loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()
                    && loanCharge.isChargePending()) {
                disBuLoanCharges.put(loanCharge.getId(), loanCharge.amountOutstanding());
            }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        for (final Map.Entry<Long, BigDecimal> entrySet : disBuLoanCharges.entrySet()) {
            final PortfolioAccountData savingAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            final SavingsAccount fromSavingsAccount = null;
            final boolean isRegularTransaction = true;
            final boolean isExceptionForBalanceCheck = false;
            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(actualDisbursementDate, entrySet.getValue(),
                    PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN, savingAccountData.accountId(), loanId, "Loan Charge Payment",
                    locale, fmt, null, null, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue(), entrySet.getKey(), null,
                    AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount, isRegularTransaction,
                    isExceptionForBalanceCheck);
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        }

        updateRecurringCalendarDatesForInterestRecalculation(loan);
        this.loanAccountDomainService.recalculateAccruals(loan);
        /**
         * For value date added
         */
        loan.setValueDate(Date.from(loan.getDisbursementDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        // Post Dated Checks
        if (command.parameterExists("postDatedChecks")) {
            // get repayment with post dates checks to update
            Set<PostDatedChecks> postDatedChecks = this.repaymentWithPostDatedChecksAssembler.fromParsedJson(command.json(), loan);
            updatePostDatedChecks(postDatedChecks);
        }

        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_DISBURSAL,
                constructEntityMap(BusinessEntity.LOAN, loan));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private boolean equalState(String state) {

        if (state.equals(LoanConstants.TAMILNADU))
            return true;
        else if (state.equals(LoanConstants.MAHARASHTRA))
            return true;
        else
            return false;
    }

    private void updatePostDatedChecks(Set<PostDatedChecks> postDatedChecks) {
        this.postDatedChecksRepository.saveAll(postDatedChecks);
    }

    private void createAndSaveLoanScheduleArchive(final Loan loan, ScheduleGeneratorDTO scheduleGeneratorDTO) {
        LoanRescheduleRequest loanRescheduleRequest = null;
        LoanScheduleModel loanScheduleModel = loan.regenerateScheduleModel(scheduleGeneratorDTO);
        List<LoanRepaymentScheduleInstallment> installments = retrieveRepaymentScheduleFromModel(loanScheduleModel);
        this.loanScheduleHistoryWritePlatformService.createAndSaveLoanScheduleArchive(installments, loan, loanRescheduleRequest);
    }

    /**
     * create standing instruction for disbursed loan
     *
     * @param loan the disbursed loan
     **/
    private void createStandingInstruction(Loan loan) {

        if (loan.shouldCreateStandingInstructionAtDisbursement()) {
            AccountAssociations accountAssociations = this.accountAssociationRepository.findByLoanIdAndType(loan.getId(),
                    AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());

            if (accountAssociations != null) {

                SavingsAccount linkedSavingsAccount = accountAssociations.linkedSavingsAccount();

                // name is auto-generated
                final String name = "To loan " + loan.getAccountNumber() + " from savings " + linkedSavingsAccount.getAccountNumber();
                final Office fromOffice = loan.getOffice();
                final Client fromClient = loan.getClient();
                final Office toOffice = loan.getOffice();
                final Client toClient = loan.getClient();
                final Integer priority = StandingInstructionPriority.MEDIUM.getValue();
                final Integer transferType = AccountTransferType.LOAN_REPAYMENT.getValue();
                final Integer instructionType = StandingInstructionType.DUES.getValue();
                final Integer status = StandingInstructionStatus.ACTIVE.getValue();
                final Integer recurrenceType = AccountTransferRecurrenceType.AS_PER_DUES.getValue();
                final LocalDate validFrom = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

                AccountTransferDetails accountTransferDetails = AccountTransferDetails.savingsToLoanTransfer(fromOffice, fromClient,
                        linkedSavingsAccount, toOffice, toClient, loan, transferType);

                AccountTransferStandingInstruction accountTransferStandingInstruction = AccountTransferStandingInstruction.create(
                        accountTransferDetails, name, priority, instructionType, status, null, validFrom, null, recurrenceType, null, null,
                        null);
                accountTransferDetails.updateAccountTransferStandingInstruction(accountTransferStandingInstruction);

                this.accountTransferDetailRepository.save(accountTransferDetails);
            }
        }
    }

    private void updateRecurringCalendarDatesForInterestRecalculation(final Loan loan) {

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()
                && loan.loanInterestRecalculationDetails().getRestFrequencyType().isSameAsRepayment()) {
            final CalendarInstance calendarInstanceForInterestRecalculation = this.calendarInstanceRepository
                    .findByEntityIdAndEntityTypeIdAndCalendarTypeId(loan.loanInterestRecalculationDetailId(),
                            CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue(), CalendarType.COLLECTION.getValue());

            Calendar calendarForInterestRecalculation = calendarInstanceForInterestRecalculation.getCalendar();
            calendarForInterestRecalculation.updateStartAndEndDate(loan.getDisbursementDate(), loan.getMaturityDate());
            this.calendarRepository.save(calendarForInterestRecalculation);
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

    private void saveLoanWithDataIntegrityViolationChecks(final Loan loan) {
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

    /****
     * TODO Vishwas: Pair with Ashok and re-factor collection sheet code-base
     *
     * May of the changes made to disburseLoan aren't being made here, should refactor to reuse disburseLoan ASAP
     *****/
    @Transactional
    @Override
    public Map<String, Object> bulkLoanDisbursal(final JsonCommand command, final CollectionSheetBulkDisbursalCommand bulkDisbursalCommand,
                                                 Boolean isAccountTransfer) {
        final AppUser currentUser = getAppUserIfPresent();

        final SingleDisbursalCommand[] disbursalCommand = bulkDisbursalCommand.getDisburseTransactions();
        final Map<String, Object> changes = new LinkedHashMap<>();
        if (disbursalCommand == null) {
            return changes;
        }

        final LocalDate nextPossibleRepaymentDate = null;
        final Date rescheduledRepaymentDate = null;

        for (final SingleDisbursalCommand singleLoanDisbursalCommand : disbursalCommand) {
            final Loan loan = this.loanAssembler.assembleFrom(singleLoanDisbursalCommand.getLoanId());
            final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");

            // validate ActualDisbursement Date Against Expected Disbursement
            // Date
            LoanProduct loanProduct = loan.loanProduct();
            if (loanProduct.syncExpectedWithDisbursementDate()) {
                syncExpectedDateWithActualDisbursementDate(loan, actualDisbursementDate);
            }
            checkClientOrGroupActive(loan);
            this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_DISBURSAL,
                    constructEntityMap(BusinessEntity.LOAN, loan));

            final List<Long> existingTransactionIds = new ArrayList<>();
            final List<Long> existingReversedTransactionIds = new ArrayList<>();

            final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

            // Bulk disbursement should happen on meeting date (mostly from
            // collection sheet).
            // FIXME: AA - this should be first meeting date based on
            // disbursement date and next available meeting dates
            // assuming repayment schedule won't regenerate because expected
            // disbursement and actual disbursement happens on same date
            loan.validateAccountStatus(LoanEvent.LOAN_DISBURSED);
            updateLoanCounters(loan, actualDisbursementDate);
            boolean canDisburse = loan.canDisburse(actualDisbursementDate);
            ChangedTransactionDetail changedTransactionDetail = null;
            if (canDisburse) {
                Money amountBeforeAdjust = loan.getPrincpal();
                Money disburseAmount = loan.adjustDisburseAmount(command, actualDisbursementDate);
                BigDecimal selfDue = loan.getSelfPrincipaAmount();
                BigDecimal partnerDue = loan.getPartnerPrincipalAmount();
                boolean recalculateSchedule = amountBeforeAdjust.isNotEqualTo(loan.getPrincpal());
                final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");
                if (isAccountTransfer) {
                    disburseLoanToSavings(loan, command, disburseAmount, paymentDetail);
                    existingTransactionIds.addAll(loan.findExistingTransactionIds());
                    existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());

                } else {
                    existingTransactionIds.addAll(loan.findExistingTransactionIds());
                    existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
                    LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan.getOffice(), disburseAmount, paymentDetail,
                            actualDisbursementDate, txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser, selfDue, partnerDue);
                    disbursementTransaction.updateLoan(loan);
                    loan.addLoanTransaction(disbursementTransaction);
                }
                LocalDate recalculateFrom = null;
                final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
                regenerateScheduleOnDisbursement(command, loan, recalculateSchedule, scheduleGeneratorDTO, nextPossibleRepaymentDate,
                        rescheduledRepaymentDate);
                if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                    createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);
                }
                if (configurationDomainService.isPaymnetypeApplicableforDisbursementCharge()) {
                    changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO, paymentDetail);
                } else {
                    changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO, null);
                }
            }
            if (!changes.isEmpty()) {

                saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

                final String noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    final Note note = Note.loanNote(loan, noteText);
                    this.noteRepository.save(note);
                }
                if (changedTransactionDetail != null) {
                    for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings()
                            .entrySet()) {
                        this.loanTransactionRepository.save(mapEntry.getValue());
                        this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
                postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            }
            final List<LoanCharge> loanCharges = loan.charges();
            final Map<Long, BigDecimal> disBuLoanCharges = new HashMap<>();
            for (final LoanCharge loanCharge : loanCharges) {
                if (loanCharge.isDueAtDisbursement() && loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()
                        && loanCharge.isChargePending()) {
                    disBuLoanCharges.put(loanCharge.getId(), loanCharge.amountOutstanding());
                }
            }
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            for (final Map.Entry<Long, BigDecimal> entrySet : disBuLoanCharges.entrySet()) {
                final PortfolioAccountData savingAccountData = this.accountAssociationsReadPlatformService
                        .retriveLoanLinkedAssociation(loan.getId());
                final SavingsAccount fromSavingsAccount = null;
                final boolean isRegularTransaction = true;
                final boolean isExceptionForBalanceCheck = false;
                final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(actualDisbursementDate, entrySet.getValue(),
                        PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN, savingAccountData.accountId(), loan.getId(),
                        "Loan Charge Payment", locale, fmt, null, null, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue(),
                        entrySet.getKey(), null, AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null,
                        fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
            }
            updateRecurringCalendarDatesForInterestRecalculation(loan);
            this.loanAccountDomainService.recalculateAccruals(loan);
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_DISBURSAL,
                    constructEntityMap(BusinessEntity.LOAN, loan));
        }

        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult undoGLIMLoanDisbursal(final Long loanId, final JsonCommand command) {
        // GroupLoanIndividualMonitoringAccount
        // glimAccount=glimRepository.findOne(loanId);
        final Long parentLoanId = loanId;
        GroupLoanIndividualMonitoringAccount parentLoan = glimRepository.findById(parentLoanId).get();
        List<Loan> childLoans = this.loanRepository.findByGlimId(loanId);
        CommandProcessingResult result = null;
        int count = 0;
        for (Loan loan : childLoans) {
            result = undoLoanDisbursal(loan.getId(), command);
            if (result.getLoanId() != null) {
                count++;
                // if all the child loans are approved, mark the parent loan as
                // approved
                if (count == parentLoan.getChildAccountsCount()) {
                    parentLoan.setLoanStatus(LoanStatus.APPROVED.getValue());
                    glimRepository.save(parentLoan);
                }
            }
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult undoLoanDisbursal(final Long loanId, final JsonCommand command) {

        final AppUser currentUser = getAppUserIfPresent();
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_UNDO_DISBURSAL,
                constructEntityMap(BusinessEntity.LOAN, loan));
        removeLoanCycle(loan);
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        //
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final LocalDate recalculateFrom = null;
        loan.setActualDisbursementDate(null);
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        // Remove post dated checks if added.
        loan.removePostDatedChecks();

        final Map<String, Object> changes = loan.undoDisbursal(scheduleGeneratorDTO, existingTransactionIds, existingReversedTransactionIds,
                currentUser);

        if (!changes.isEmpty()) {
            if (loan.isTopup() && loan.getClientId() != null) {
                final Long loanIdToClose = loan.getTopupLoanDetails().getLoanIdToClose();
                final LocalDate expectedDisbursementDate = command
                        .localDateValueOfParameterNamed(LoanApiConstants.disbursementDateParameterName);
                BigDecimal loanOutstanding = this.loanReadPlatformService
                        .retrieveLoanPrePaymentTemplate(LoanTransactionType.REPAYMENT, loanIdToClose, expectedDisbursementDate).getAmount();
                BigDecimal netDisbursalAmount = loan.getApprovedPrincipal().subtract(loanOutstanding);
                loan.adjustNetDisbursalAmount(netDisbursalAmount);
            }
            saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            this.accountTransfersWritePlatformService.reverseAllTransactions(loanId, PortfolioAccountType.LOAN);
            String noteText = null;
            if (command.hasParameter("note")) {
                noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    final Note note = Note.loanNote(loan, noteText);
                    this.noteRepository.save(note);
                }
            }
            boolean isAccountTransfer = false;
            final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                    existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
            this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_UNDO_DISBURSAL,
                    constructEntityMap(BusinessEntity.LOAN, loan));
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult makeGLIMLoanRepayment(final Long loanId, final JsonCommand command) {

        final Long parentLoanId = loanId;

        glimRepository.findById(parentLoanId).get();

        JsonArray repayments = command.arrayOfParameterNamed("formDataArray");
        JsonCommand childCommand = null;
        CommandProcessingResult result = null;
        JsonObject jsonObject = null;

        Long[] childLoanId = new Long[repayments.size()];
        for (int i = 0; i < repayments.size(); i++) {
            jsonObject = repayments.get(i).getAsJsonObject();
            LOG.info("{}", jsonObject.toString());
            childLoanId[i] = jsonObject.get("loanId").getAsLong();
        }
        int j = 0;
        for (JsonElement element : repayments) {
            childCommand = JsonCommand.fromExistingCommand(command, element);
            result = makeLoanRepayment(LoanTransactionType.REPAYMENT, childLoanId[j++], childCommand, false);
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult makeLoanRepayment(final LoanTransactionType repaymentTransactionType, final Long loanId,
                                                     final JsonCommand command, final boolean isRecoveryRepayment) {


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
//        CodeValue modeOfPayment = null;
//        final Long modeOfPaymentId = command.longValueOfParameterNamed("modeOfPayment");
//        if (modeOfPaymentId != null) {
//            modeOfPayment = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection("ModeOfPayment",modeOfPaymentId);
//        }

        //  final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("amount");

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
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.outstanding.amount {}", errorMessage);
        }

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        final Boolean isHolidayValidationDone = false;
        final HolidayDetailDTO holidayDetailDto = null;
        boolean isAccountTransfer = false;
        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        LoanTransaction loanTransaction = this.loanAccountDomainService.makeRepayment(repaymentTransactionType, loan,
                commandProcessingResultBuilder, transactionDate, transactionAmount, paymentDetail, noteText, txnExternalId,
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

    @Transactional
    @Override
    public Map<String, Object> makeLoanBulkRepayment(final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand) {

        final SingleRepaymentCommand[] repaymentCommand = bulkRepaymentCommand.getLoanTransactions();
        final Map<String, Object> changes = new LinkedHashMap<>();
        final boolean isRecoveryRepayment = false;

        if (repaymentCommand == null) {
            return changes;
        }
        List<Long> transactionIds = new ArrayList<>();
        boolean isAccountTransfer = false;
        HolidayDetailDTO holidayDetailDTO = null;
        Boolean isHolidayValidationDone = false;
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        for (final SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            if (singleLoanRepaymentCommand != null) {
                Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(singleLoanRepaymentCommand.getLoanId());
                final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                        Date.from(singleLoanRepaymentCommand.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                final WorkingDays workingDays = this.workingDaysRepository.findOne();
                final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
                boolean isHolidayEnabled = false;
                isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
                holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                        allowTransactionsOnNonWorkingDay);
                loan.validateRepaymentDateIsOnHoliday(singleLoanRepaymentCommand.getTransactionDate(),
                        holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());
                loan.validateRepaymentDateIsOnNonWorkingDay(singleLoanRepaymentCommand.getTransactionDate(),
                        holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());
                isHolidayValidationDone = true;
                break;
            }

        }
        for (final SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            if (singleLoanRepaymentCommand != null) {
                final Loan loan = this.loanAssembler.assembleFrom(singleLoanRepaymentCommand.getLoanId());
                final PaymentDetail paymentDetail = singleLoanRepaymentCommand.getPaymentDetail();
                if (paymentDetail != null && paymentDetail.getId() == null) {
                    this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
                }
                BigDecimal selfDue = BigDecimal.ZERO;
                BigDecimal partnerDue = BigDecimal.ZERO;
                final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
                LoanTransaction loanTransaction = this.loanAccountDomainService.makeRepayment(LoanTransactionType.REPAYMENT, loan,
                        commandProcessingResultBuilder, bulkRepaymentCommand.getTransactionDate(),
                        singleLoanRepaymentCommand.getTransactionAmount(), paymentDetail, bulkRepaymentCommand.getNote(), null,
                        isRecoveryRepayment, isAccountTransfer, holidayDetailDTO, isHolidayValidationDone, selfDue, partnerDue, 0, null, null, null, null, null, loanHistoryRepo);
                transactionIds.add(loanTransaction.getId());
            }
        }
        changes.put("loanTransactions", transactionIds);
        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {

        AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        if (loan.status().isClosed() && loan.getLoanSubStatus() != null
                && loan.getLoanSubStatus().equals(LoanSubStatus.FORECLOSED.getValue())) {
            final String defaultUserMessage = "The loan cannot reopend as it is foreclosed.";
            throw new LoanForeclosureException("loan.cannot.be.reopened.as.it.is.foreclosured", defaultUserMessage, loanId);
        }
        checkClientOrGroupActive(loan);
        final LoanTransaction transactionToAdjust = this.loanTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(transactionId));
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_ADJUST_TRANSACTION,
                constructEntityMap(BusinessEntity.LOAN_ADJUSTED_TRANSACTION, transactionToAdjust));
        if (this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.LOAN)) {
            throw new PlatformServiceUnavailableException("error.msg.loan.transfer.transaction.update.not.allowed",
                    "Loan transaction:" + transactionId + " update not allowed as it involves in account transfer", transactionId);
        }
        if (loan.isClosedWrittenOff()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.written.off.update.not.allowed",
                    "Loan transaction:" + transactionId + " update not allowed as loan status is written off", transactionId);
        }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");
        final Date partnerTransferDate = command.dateValueOfParameterNamed("partnerTransferDate");
        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.stringValueOfParameterNamed("paymentTypeId"));

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createPaymentDetail(command, changes);
        LoanTransaction newTransactionDetail = LoanTransaction.repayment(loan.getOffice(), transactionAmountAsMoney, paymentDetail,
                transactionDate, txnExternalId, DateUtils.getLocalDateTimeOfTenant(), currentUser, null, null, BusinessEntity.LOAN_ADJUSTED_TRANSACTION.getValue(), null, null, partnerTransferDate, null);
        newTransactionDetail.setModifiedDate(DateUtils.getDateOfTenant());
        newTransactionDetail.setModifiedUser(currentUser);
        if (transactionToAdjust.isInterestWaiver()) {
            Money unrecognizedIncome = transactionAmountAsMoney.zero();
            Money interestComponent = transactionAmountAsMoney;
            if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                Money receivableInterest = loan.getReceivableInterest(transactionDate);
                if (transactionAmountAsMoney.isGreaterThan(receivableInterest)) {
                    interestComponent = receivableInterest;
                    unrecognizedIncome = transactionAmountAsMoney.minus(receivableInterest);
                }
            }
            newTransactionDetail = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney, transactionDate,
                    interestComponent, unrecognizedIncome, DateUtils.getLocalDateTimeOfTenant(), currentUser, null, null);
        }

        LocalDate recalculateFrom = null;

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = transactionToAdjust.getTransactionDate().isAfter(transactionDate) ? transactionDate
                    : transactionToAdjust.getTransactionDate();
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        final ChangedTransactionDetail changedTransactionDetail = loan.adjustExistingTransaction(newTransactionDetail,
                defaultLoanLifecycleStateMachine(), transactionToAdjust, existingTransactionIds, existingReversedTransactionIds,
                scheduleGeneratorDTO, currentUser);

        if (newTransactionDetail.isGreaterThanZero(loan.getPrincpal().getCurrency())) {
            if (paymentDetail != null) {
                this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
            }
            this.loanTransactionRepository.saveAndFlush(newTransactionDetail);
        }

        /***
         * TODO Vishwas Batch save is giving me a HibernateOptimisticLockingFailureException, looping and saving for the
         * time being, not a major issue for now as this loop is entered only in edge cases (when a adjustment is made
         * before the latest payment recorded against the loan)
         ***/
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                // update loan with references to the newly created transactions
                loan.addLoanTransaction(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            Note note = null;
            /**
             * If a new transaction is not created, associate note with the transaction to be adjusted
             **/
            if (newTransactionDetail.isGreaterThanZero(loan.getPrincpal().getCurrency())) {
                note = Note.loanTransactionNote(loan, newTransactionDetail, noteText);
            } else {
                note = Note.loanTransactionNote(loan, transactionToAdjust, noteText);
            }
            this.noteRepository.save(note);
        }

        Collection<Long> transactionIds = new ArrayList<>();
        List<LoanTransaction> transactions = loan.getLoanTransactions();
        for (LoanTransaction transaction : transactions) {
            if (transaction.isRefund() && transaction.isNotReversed()) {
                transactionIds.add(transaction.getId());
            }
        }

        if (!transactionIds.isEmpty()) {
            this.accountTransfersWritePlatformService.reverseTransfersWithFromAccountTransactions(transactionIds,
                    PortfolioAccountType.LOAN);
            loan.updateLoanSummarAndStatus();
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        this.loanAccountDomainService.recalculateAccruals(loan);
        Map<BusinessEntity, Object> entityMap = constructEntityMap(BusinessEntity.LOAN_ADJUSTED_TRANSACTION, transactionToAdjust);
        if (newTransactionDetail.isRepaymentType() && newTransactionDetail.isGreaterThanZero(loan.getPrincpal().getCurrency())) {
            entityMap.put(BusinessEntity.LOAN_TRANSACTION, newTransactionDetail);
        }
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_ADJUST_TRANSACTION, entityMap);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(transactionId)
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveInterestOnLoan(final Long loanId, final JsonCommand command) {

        AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        Money unrecognizedIncome = transactionAmountAsMoney.zero();
        Money interestComponent = transactionAmountAsMoney;
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Money receivableInterest = loan.getReceivableInterest(transactionDate);
            if (transactionAmountAsMoney.isGreaterThan(receivableInterest)) {
                interestComponent = receivableInterest;
                unrecognizedIncome = transactionAmountAsMoney.minus(receivableInterest);
            }
        }
        final LoanTransaction waiveInterestTransaction = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney,
                transactionDate, interestComponent, unrecognizedIncome, DateUtils.getLocalDateTimeOfTenant(), currentUser, null, null);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_WAIVE_INTEREST,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, waiveInterestTransaction));
        LocalDate recalculateFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = transactionDate;
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        final ChangedTransactionDetail changedTransactionDetail = loan.waiveInterest(waiveInterestTransaction,
                defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO,
                currentUser);

        this.loanTransactionRepository.saveAndFlush(waiveInterestTransaction);

        /***
         * TODO Vishwas Batch save is giving me a HibernateOptimisticLockingFailureException, looping and saving for the
         * time being, not a major issue for now as this loop is entered only in edge cases (when a waiver is made
         * before the latest payment recorded against the loan)
         ***/
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                // update loan with references to the newly created transactions
                loan.addLoanTransaction(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, waiveInterestTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_WAIVE_INTEREST,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, waiveInterestTransaction));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(waiveInterestTransaction.getId())
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult writeOff(final Long loanId, final JsonCommand command) {
        final AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        if (command.hasParameter("writeoffReasonId")) {
            Long writeoffReasonId = command.longValueOfParameterNamed("writeoffReasonId");
            CodeValue writeoffReason = this.codeValueRepository
                    .findOneByCodeNameAndIdWithNotFoundDetection(LoanApiConstants.WRITEOFFREASONS, writeoffReasonId);
            changes.put("writeoffReasonId", writeoffReasonId);
            loan.updateWriteOffReason(writeoffReason);
        }

        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_WRITTEN_OFF,
                constructEntityMap(BusinessEntity.LOAN, loan));
        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                StatusEnum.WRITE_OFF.getCode().longValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

        removeLoanCycle(loan);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        updateLoanCounters(loan, loan.getDisbursementDate());

        LocalDate recalculateFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = command.localDateValueOfParameterNamed("transactionDate");
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        final ChangedTransactionDetail changedTransactionDetail = loan.closeAsWrittenOff(command, defaultLoanLifecycleStateMachine(),
                changes, existingTransactionIds, existingReversedTransactionIds, currentUser, scheduleGeneratorDTO);
        LoanTransaction writeoff = changedTransactionDetail.getNewTransactionMappings().remove(0L);
        this.loanTransactionRepository.saveAndFlush(writeoff);
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            this.loanTransactionRepository.save(mapEntry.getValue());
            this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
        }
        saveLoanWithDataIntegrityViolationChecks(loan);
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, writeoff, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_WRITTEN_OFF,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, writeoff));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(writeoff.getId())
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult closeLoan(final Long loanId, final JsonCommand command) {

        AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_CLOSE,
                constructEntityMap(BusinessEntity.LOAN, loan));

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        updateLoanCounters(loan, loan.getDisbursementDate());

        LocalDate recalculateFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = command.localDateValueOfParameterNamed("transactionDate");
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        ChangedTransactionDetail changedTransactionDetail = loan.close(command, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO, currentUser);
        final LoanTransaction possibleClosingTransaction = changedTransactionDetail.getNewTransactionMappings().remove(0L);
        if (possibleClosingTransaction != null) {
            this.loanTransactionRepository.saveAndFlush(possibleClosingTransaction);
        }
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            this.loanTransactionRepository.save(mapEntry.getValue());
            this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
        }
        saveLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        if (possibleClosingTransaction != null) {
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        }
        this.loanAccountDomainService.recalculateAccruals(loan);

        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_CLOSE,
                constructEntityMap(BusinessEntity.LOAN, loan));

        // Update loan transaction on repayment.
        if (AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {
            Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
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

        // disable all active standing instructions linked to the loan
        this.loanAccountDomainService.disableStandingInstructionsLinkedToClosedLoan(loan);

        CommandProcessingResult result = null;
        if (possibleClosingTransaction != null) {

            result = new CommandProcessingResultBuilder().withCommandId(command.commandId())
                    .withEntityId(possibleClosingTransaction.getId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                    .withGroupId(loan.getGroupId()).withLoanId(loanId).with(changes).build();
        } else {
            result = new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                    .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                    .with(changes).build();
        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult closeAsRescheduled(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        removeLoanCycle(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_CLOSE_AS_RESCHEDULE,
                constructEntityMap(BusinessEntity.LOAN, loan));

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        loan.closeAsMarkedForReschedule(command, defaultLoanLifecycleStateMachine(), changes);

        saveLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_CLOSE_AS_RESCHEDULE,
                constructEntityMap(BusinessEntity.LOAN, loan));

        // disable all active standing instructions linked to the loan
        this.loanAccountDomainService.disableStandingInstructionsLinkedToClosedLoan(loan);

        // Update loan transaction on repayment.
        if (AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {
            Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
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

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private void validateAddingNewChargeAllowed(List<LoanDisbursementDetails> loanDisburseDetails) {
        boolean pendingDisbursementAvailable = false;
        for (LoanDisbursementDetails disbursementDetail : loanDisburseDetails) {
            if (disbursementDetail.actualDisbursementDate() == null) {
                pendingDisbursementAvailable = true;
                break;
            }
        }
        if (!pendingDisbursementAvailable) {
            throw new ChargeCannotBeUpdatedException("error.msg.charge.cannot.be.updated.no.pending.disbursements.in.loan",
                    "This charge cannot be added, No disbursement is pending");
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult addLoanCharge(final Long loanId, final JsonCommand command) {


        AppUser currentUser = null;
        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication auth = context.getAuthentication();
        if (auth != null) {
            currentUser = (AppUser) auth.getPrincipal();
        }

        this.loanEventApiJsonValidator.validateAddLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        JsonElement element = fromApiJsonHelper.parse(command.json());
        final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);

        final LocalDate localDate = command.localDateValueOfParameterNamed("dueDate");


        List<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
        final Long chargeDefinitionId = command.longValueOfParameterNamed("chargeId");
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        if (loan.isDisbursed() && chargeDefinition.isDisbursementCharge()) {
            // validates whether any pending disbursements are available to
            // apply this charge
            validateAddingNewChargeAllowed(loanDisburseDetails);
        }
        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        LoanProductFeesCharges loanProductFeesCharges = this.loanProductFeesChargesRepository.getByChargeAndLoanProduct(chargeDefinition, loan.getLoanProduct());

        LoanRepaymentScheduleInstallment installment = loan.fetchInstallmentByDate(localDate);

        /***
         * gst calculation at adhoc charge level
         ***/

        List<Charge> adhocCharge = new ArrayList<>();
        adhocCharge.add(chargeDefinition);

        final List<GstData> gstData = this.gstService.calculationOfGst(loan.getClientId(), adhocCharge, loan.getPrincpal().getAmount(), loan.getLoanProduct(), command.parsedJson(), null);

        boolean isAppliedOnBackDate = false;
        LoanCharge loanCharge = null;
        LocalDate recalculateFrom = loan.fetchInterestRecalculateFromDate();
        if (chargeDefinition.isPercentageOfDisbursementAmount()) {
            LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = null;
            for (LoanDisbursementDetails disbursementDetail : loanDisburseDetails) {
                if (disbursementDetail.actualDisbursementDate() == null) {
                    loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition, disbursementDetail.principal(), null, null, null,
                            disbursementDetail.expectedDisbursementDateAsLocalDate(), null, null, loanProductFeesCharges.getSelfShare(), loanProductFeesCharges.getPartnerShare(), gstData);
                    loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge, disbursementDetail);
                    loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                    this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_ADD_CHARGE,
                            constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));
                    validateAddLoanCharge(loan, chargeDefinition, loanCharge);
                    addCharge(loan, chargeDefinition, loanCharge);
                    isAppliedOnBackDate = true;
                    if (recalculateFrom.isAfter(disbursementDetail.expectedDisbursementDateAsLocalDate())) {
                        recalculateFrom = disbursementDetail.expectedDisbursementDateAsLocalDate();
                    }
                }
            }
            loan.addTrancheLoanCharge(chargeDefinition);
        } else {
            loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command, amount, loanProductFeesCharges.getSelfShare(), loanProductFeesCharges.getPartnerShare(), gstData, installment);
            this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_ADD_CHARGE,
                    constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));

            validateAddLoanCharge(loan, chargeDefinition, loanCharge);
            isAppliedOnBackDate = addCharge(loan, chargeDefinition, loanCharge);
            if (loanCharge.getDueLocalDate() == null || recalculateFrom.isAfter(loanCharge.getDueLocalDate())) {
                isAppliedOnBackDate = true;
                recalculateFrom = loanCharge.getDueLocalDate();
            }
        }

        boolean reprocessRequired = true;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            if (isAppliedOnBackDate && loan.isFeeCompoundingEnabledForInterestRecalculation()) {

                runScheduleRecalculation(loan, recalculateFrom);
                reprocessRequired = false;
            }
            updateOriginalSchedule(loan);
        }
        if (reprocessRequired) {
            ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    // update loan with references to the newly created
                    // transactions
                    loan.addLoanTransaction(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }
            /**
             * Modified for Adhoc Charges
             **/
            List<LoanCharge> loanCharges = new ArrayList<>();
            loanCharges.add(loanCharge);
            loan.updateGstForSelfAndPartnerInLoan(loanCharges, currentUser, true);
            saveLoanWithDataIntegrityViolationChecks(loan);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled() && isAppliedOnBackDate
                && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
            this.loanAccountDomainService.recalculateAccruals(loan);
        }
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_ADD_CHARGE,
                constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanCharge.getId())
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .build();
    }

    private void validateAddLoanCharge(final Loan loan, final Charge chargeDefinition, final LoanCharge loanCharge) {
        if (chargeDefinition.isOverdueInstallment()) {
            final String defaultUserMessage = "Installment charge cannot be added to the loan.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "overdue.charge", defaultUserMessage, null,
                    chargeDefinition.getName());
        } else if (loanCharge.getDueLocalDate() != null
                && loanCharge.getDueLocalDate().isBefore(loan.getLastUserTransactionForChargeCalc())) {
            final String defaultUserMessage = "charge with date before last transaction date can not be added to loan.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "date.is.before.last.transaction.date", defaultUserMessage, null,
                    chargeDefinition.getName());
        } else if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {

            if (loanCharge.isInstalmentFee() && loan.status().isActive()) {
                final String defaultUserMessage = "installment charge addition not allowed after disbursement";
                throw new LoanChargeCannotBeAddedException("loanCharge", "installment.charge", defaultUserMessage, null,
                        chargeDefinition.getName());
            }
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final List<LoanCharge> loanCharges = new ArrayList<>(1);
            loanCharges.add(loanCharge);
            this.loanApplicationCommandFromApiJsonHelper.validateLoanCharges(loanCharges, dataValidationErrors);
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

    }

    public void runScheduleRecalculation(final Loan loan, final LocalDate recalculateFrom) {
        AppUser currentUser = getAppUserIfPresent();
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            ScheduleGeneratorDTO generatorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
            ChangedTransactionDetail changedTransactionDetail = loan
                    .handleRegenerateRepaymentScheduleWithInterestRecalculation(generatorDTO, currentUser);
            saveLoanWithDataIntegrityViolationChecks(loan);
            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    // update loan with references to the newly created
                    // transactions
                    loan.addLoanTransaction(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }

        }
    }

    public void updateOriginalSchedule(Loan loan) {
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            final LocalDate recalculateFrom = null;
            ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
            createLoanScheduleArchive(loan, scheduleGeneratorDTO);
        }

    }

    private boolean addCharge(final Loan loan, final Charge chargeDefinition, final LoanCharge loanCharge) {

        AppUser currentUser = getAppUserIfPresent();
        if (!loan.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            final String errorMessage = "Charge and Loan must have the same currency.";
            throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
        }

        if (loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                    .retriveLoanLinkedAssociation(loan.getId());
            if (portfolioAccountData == null) {
                final String errorMessage = loanCharge.name() + "Charge  requires linked savings account for payment";
                throw new LinkedAccountRequiredException("loanCharge.add", errorMessage, loanCharge.name());
            }
        }

        loan.addLoanCharge(loanCharge, chargeDefinition);
        if (loanCharge.isAdhocChargeCharge()) {
            loanCharge.markAsFullyPaid();
        }

        this.loanChargeRepository.saveAndFlush(loanCharge);

        /**
         * we want to apply charge transactions only for those loans charges that are applied when a loan is active and
         * the loan product uses Upfront Accruals
         **/
        if (loan.status().isActive() && loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
            final LoanTransaction applyLoanChargeTransaction = loan.handleChargeAppliedTransaction(loanCharge, null, currentUser);
//            /** Addded for the loancharge Eventstoring
//             **/
//            applyLoanChargeTransaction.updateType(LoanTransactionType.ADHOC_CHARGE.getValue());
            this.loanTransactionRepository.saveAndFlush(applyLoanChargeTransaction);
        }
        boolean isAppliedOnBackDate = false;
        if (loanCharge.getDueLocalDate() == null || DateUtils.getLocalDateOfTenant().isAfter(loanCharge.getDueLocalDate())) {
            isAppliedOnBackDate = true;
        }
        return isAppliedOnBackDate;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateUpdateOfLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be edited only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.status().isSubmittedAndPendingApproval()) {
            throw new LoanChargeCannotBeUpdatedException(LoanChargeCannotBeUpdatedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE,
                    loanCharge.getId());
        }

        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_UPDATE_CHARGE,
                constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));

        final Map<String, Object> changes = loan.updateLoanCharge(loanCharge, command);

        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_UPDATE_CHARGE,
                constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult undoWaiveLoanCharge(final JsonCommand command) {

        LoanTransaction loanTransaction = this.loanTransactionRepository.findById(command.entityId())
                .orElseThrow(() -> new LoanTransactionNotFoundException(command.entityId()));

        if (!loanTransaction.getTypeOf().getCode().equals(LoanTransactionType.WAIVE_CHARGES.getCode())) {
            throw new InvalidLoanTransactionTypeException("Undo Waive Charge", "Waive an Installment Charge First",
                    "Transaction is not a waive charge type.");
        }

        Set<LoanChargePaidBy> loanChargePaidBySet = loanTransaction.getLoanChargesPaid();
        Integer installmentNumber = null;
        Long loanChargeId = null;
        final Long loanId = loanTransaction.getLoan().getId();

        for (LoanChargePaidBy loanChargePaidBy : loanChargePaidBySet) {
            installmentNumber = loanChargePaidBy.getInstallmentNumber();
            loanChargeId = loanChargePaidBy.getLoanCharge().getId();
            break;
        }

        AppUser currentUser = getAppUserIfPresent();
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.status().isActive()) {
            throw new LoanChargeWaiveCannotBeReversedException(LoanChargeWaiveCannotUndoReason.LOAN_INACTIVE, loanCharge.getId());
        }

        // Validate loan charge is not already paid
        if (loanCharge.isPaid()) {
            throw new LoanChargeWaiveCannotBeReversedException(LoanChargeWaiveCannotUndoReason.ALREADY_PAID, loanCharge.getId());
        }

        final Map<String, Object> changes = new LinkedHashMap<>(3);

        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_WAIVE_CHARGE_UNDO,
                constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));

        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;

            // final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
            if (installmentNumber != null) {

                // Get installment charge.
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);

                if (!loanTransaction.isNotReversed()) {
                    throw new LoanChargeWaiveCannotBeReversedException(LoanChargeWaiveCannotUndoReason.ALREADY_REVERSED,
                            loanTransaction.getId());
                }

                // Reverse waived transaction
                loanTransaction.setReversed();

                // Get installment amount waived.
                BigDecimal amountWaived = chargePerInstallment.getAmountWaived(loan.getCurrency()).getAmount();

                // Set manually adjusted value to `1`
                loanTransaction.setManuallyAdjustedOrReversed();

                // Save updated data
                this.loanTransactionRepository.saveAndFlush(loanTransaction);

                // Get installment outstanding amount
                BigDecimal amountOutstandingPerInstallment = chargePerInstallment.getAmountOutstanding();

                // Check whether the installment charge is not waived. If so throw new error
                if (!chargePerInstallment.isWaived() || amountWaived == null) {
                    throw new LoanChargeWaiveCannotBeReversedException(LoanChargeWaiveCannotUndoReason.NOT_WAIVED, loanChargeId);
                }

                // Get loan charge total amount waived
                BigDecimal totalAmountWaved = loanCharge.getAmountWaived(loan.getCurrency()).getAmount();

                // Get loan charge outstanding amount
                BigDecimal amountOutstanding = loanCharge.getAmountOutstanding(loan.getCurrency()).getAmount();

                // Add the amount waived to outstanding amount
                loanCharge.resetOutstandingAmount(amountOutstanding.add(amountWaived));

                // Subtract the amount waived from the existing amount waived.
                loanCharge.setAmountWaived(totalAmountWaved.subtract(amountWaived));

                // Add the amount waived to the outstanding amount of the installment
                chargePerInstallment.resetOutstandingAmount(amountOutstandingPerInstallment.add(amountWaived));

                // Set the amount waived value to ZERO
                chargePerInstallment.resetAmountWaived(BigDecimal.ZERO);

                // Reset waived flag
                chargePerInstallment.undoWaiveFlag();

                // Get the fee charges waived amount per installment
                BigDecimal feeChargesWaivedAmount = chargePerInstallment.getInstallment().getFeeChargesWaived(loan.getCurrency())
                        .getAmount();

                // Subtract the amount waived from the existing fee charges waived amount.
                chargePerInstallment.getInstallment().setFeeChargesWaived(feeChargesWaivedAmount.subtract(amountWaived));

                // Set the last modification date.
                chargePerInstallment.getInstallment().setLastModifiedDate(Instant.now());

                // Update loan charge.
                loanCharge.setInstallmentLoanCharge(chargePerInstallment, chargePerInstallment.getInstallment().getInstallmentNumber());

                if (loanCharge.getAmount(loan.getCurrency()).compareTo(loanCharge.getAmountOutstanding(loan.getCurrency())) == 0
                        && loanCharge.isWaived()) {
                    loanCharge.undoWaived();
                }

                this.loanChargeRepository.saveAndFlush(loanCharge);

                loan.updateLoanSummaryForUndoWaiveCharge(amountWaived);

                changes.put("amount", amountWaived);

            } else {
                throw new InstallmentNotFoundException(command.entityId());
            }
        }

        saveLoanWithDataIntegrityViolationChecks(loan);

        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_WAIVE_CHARGE_UNDO,
                constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));

        LoanTransaction loanTransactionData = this.loanTransactionRepository.getReferenceById(command.entityId());
        changes.put("principalPortion", loanTransactionData.getPrincipalPortion());
        changes.put("selfPrincipalPortion", loanTransactionData.getSelfPrincipalPortion());
        changes.put("partnerPrincipalPortion", loanTransactionData.getPartnerPrincipalPortion());

        changes.put("interestPortion", loanTransactionData.getInterestPortion(loan.getCurrency()));
        changes.put("feeChargesPortion", loanTransactionData.getFeeChargesPortion(loan.getCurrency()));
        changes.put("penaltyChargesPortion", loanTransactionData.getPenaltyChargesPortion(loan.getCurrency()));
        changes.put("outstandingLoanBalance", loanTransactionData.getOutstandingLoanBalance());
        changes.put("id", loanTransactionData.getId());
        changes.put("date", loanTransactionData.getTransactionDate());

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withLoanId(loanId) //
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        AppUser currentUser = getAppUserIfPresent();

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.loanEventApiJsonValidator.validateInstallmentChargeTransaction(command.json());
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.status().isActive()) {
            throw new LoanChargeCannotBeWaivedException(LoanChargeCannotBeWaivedReason.LOAN_INACTIVE, loanCharge.getId());
        }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBeWaivedException(LoanChargeCannotBeWaivedReason.ALREADY_WAIVED, loanCharge.getId());
        } else if (loanCharge.isPaid()) {
            throw new LoanChargeCannotBeWaivedException(LoanChargeCannotBeWaivedReason.ALREADY_PAID, loanCharge.getId());
        }
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_WAIVE_CHARGE,
                constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));
        Integer loanInstallmentNumber = null;
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            if (!StringUtils.isBlank(command.json())) {
                final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
                final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
                if (dueDate != null) {
                    chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
                } else if (installmentNumber != null) {
                    chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
                }
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_WAIVED, loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_PAID, loanCharge.getId());
            }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
        }

        final Map<String, Object> changes = new LinkedHashMap<>(3);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        Money accruedCharge = Money.zero(loan.getCurrency());
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Collection<LoanChargePaidByData> chargePaidByDatas = this.loanChargeReadPlatformService
                    .retriveLoanChargesPaidBy(loanCharge.getId(), LoanTransactionType.ACCRUAL, loanInstallmentNumber);
            for (LoanChargePaidByData chargePaidByData : chargePaidByDatas) {
                accruedCharge = accruedCharge.plus(chargePaidByData.getAmount());
            }
        }

        final LoanTransaction waiveTransaction = loan.waiveLoanCharge(loanCharge, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds, loanInstallmentNumber, scheduleGeneratorDTO, accruedCharge,
                currentUser);

        this.loanTransactionRepository.saveAndFlush(waiveTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_WAIVE_CHARGE,
                constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be deleted only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.status().isSubmittedAndPendingApproval()) {
            throw new LoanChargeCannotBeDeletedException(LoanChargeCannotBeDeletedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE,
                    loanCharge.getId());
        }
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_DELETE_CHARGE,
                constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));

        loan.removeLoanCharge(loanCharge);
        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_DELETE_CHARGE,
                constructEntityMap(BusinessEntity.LOAN_CHARGE, loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult payLoanCharge(final Long loanId, Long loanChargeId, final JsonCommand command,
                                                 final boolean isChargeIdIncludedInJson) {

        this.loanEventApiJsonValidator.validateChargePaymentTransaction(command.json(), isChargeIdIncludedInJson);
        if (isChargeIdIncludedInJson) {
            loanChargeId = command.longValueOfParameterNamed("chargeId");
        }
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.status().isActive()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.LOAN_INACTIVE, loanCharge.getId());
        }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_WAIVED, loanCharge.getId());
        } else if (loanCharge.isPaid()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_PAID, loanCharge.getId());
        }

        if (!loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.CHARGE_NOT_ACCOUNT_TRANSFER, loanCharge.getId());
        }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        Integer loanInstallmentNumber = null;
        BigDecimal amount = loanCharge.amountOutstanding();
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
            final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
            if (dueDate != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
            } else if (installmentNumber != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_WAIVED, loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_PAID, loanCharge.getId());
            }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
            amount = chargePerInstallment.getAmountOutstanding();
        }

        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
        if (portfolioAccountData == null) {
            final String errorMessage = "Charge with id:" + loanChargeId + " requires linked savings account for payment";
            throw new LinkedAccountRequiredException("loanCharge.pay", errorMessage, loanChargeId);
        }
        final SavingsAccount fromSavingsAccount = null;
        final boolean isRegularTransaction = true;
        final boolean isExceptionForBalanceCheck = false;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount, PortfolioAccountType.SAVINGS,
                PortfolioAccountType.LOAN, portfolioAccountData.accountId(), loanId, "Loan Charge Payment", locale, fmt, null, null,
                LoanTransactionType.CHARGE_PAYMENT.getValue(), loanChargeId, loanInstallmentNumber,
                AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount, isRegularTransaction,
                isExceptionForBalanceCheck);
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .withSavingsId(portfolioAccountData.accountId()).build();
    }

    public void disburseLoanToLoan(final Loan loan, final JsonCommand command, final BigDecimal amount) {

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount, PortfolioAccountType.LOAN,
                PortfolioAccountType.LOAN, loan.getId(), loan.getTopupLoanDetails().getLoanIdToClose(), "Loan Topup", locale, fmt,
                LoanTransactionType.DISBURSEMENT.getValue(), LoanTransactionType.REPAYMENT.getValue(), txnExternalId, loan, null);
        AccountTransferDetails accountTransferDetails = this.accountTransfersWritePlatformService.repayLoanWithTopup(accountTransferDTO);
        loan.getTopupLoanDetails().setAccountTransferDetails(accountTransferDetails.getId());
        loan.getTopupLoanDetails().setTopupAmount(amount);
    }

    public void disburseLoanToSavings(final Loan loan, final JsonCommand command, final Money amount, final PaymentDetail paymentDetail) {

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                .retriveLoanLinkedAssociation(loan.getId());
        if (portfolioAccountData == null) {
            final String errorMessage = "Disburse Loan with id:" + loan.getId() + " requires linked savings account for payment";
            throw new LinkedAccountRequiredException("loan.disburse.to.savings", errorMessage, loan.getId());
        }
        final SavingsAccount fromSavingsAccount = null;
        final boolean isExceptionForBalanceCheck = false;
        final boolean isRegularTransaction = true;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount.getAmount(), PortfolioAccountType.LOAN,
                PortfolioAccountType.SAVINGS, loan.getId(), portfolioAccountData.accountId(), "Loan Disbursement", locale, fmt,
                paymentDetail, LoanTransactionType.DISBURSEMENT.getValue(), null, null, null,
                AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, txnExternalId, loan, null, fromSavingsAccount,
                isRegularTransaction, isExceptionForBalanceCheck);
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);

    }

    @Override
    @CronTarget(jobName = JobName.TRANSFER_FEE_CHARGE_FOR_LOANS)
    public void transferFeeCharges() throws JobExecutionException {
        final Collection<LoanChargeData> chargeDatas = this.loanChargeReadPlatformService
                .retrieveLoanChargesForFeePayment(ChargePaymentMode.ACCOUNT_TRANSFER.getValue(), LoanStatus.ACTIVE.getValue());
        final boolean isRegularTransaction = true;
        List<Throwable> errors = new ArrayList<>();
        if (chargeDatas != null) {
            for (final LoanChargeData chargeData : chargeDatas) {
                if (chargeData.isInstallmentFee()) {
                    final Collection<LoanInstallmentChargeData> chargePerInstallments = this.loanChargeReadPlatformService
                            .retrieveInstallmentLoanCharges(chargeData.getId(), true);
                    PortfolioAccountData portfolioAccountData = null;
                    for (final LoanInstallmentChargeData installmentChargeData : chargePerInstallments) {
                        if (!installmentChargeData.getDueDate().isAfter(DateUtils.getLocalDateOfTenant())) {
                            if (portfolioAccountData == null) {
                                portfolioAccountData = this.accountAssociationsReadPlatformService
                                        .retriveLoanLinkedAssociation(chargeData.getLoanId());
                            }
                            final SavingsAccount fromSavingsAccount = null;
                            final boolean isExceptionForBalanceCheck = false;
                            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(
                                    LocalDate.now(DateUtils.getDateTimeZoneOfTenant()), installmentChargeData.getAmountOutstanding(),
                                    PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN, portfolioAccountData.accountId(),
                                    chargeData.getLoanId(), "Loan Charge Payment", null, null, null, null,
                                    LoanTransactionType.CHARGE_PAYMENT.getValue(), chargeData.getId(),
                                    installmentChargeData.getInstallmentNumber(), AccountTransferType.CHARGE_PAYMENT.getValue(), null, null,
                                    null, null, null, fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                            transferFeeCharge(accountTransferDTO, errors);
                        }
                    }
                } else if (chargeData.getDueDate() != null && !chargeData.getDueDate().isAfter(DateUtils.getLocalDateOfTenant())) {
                    final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                            .retriveLoanLinkedAssociation(chargeData.getLoanId());
                    final SavingsAccount fromSavingsAccount = null;
                    final boolean isExceptionForBalanceCheck = false;
                    final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()),
                            chargeData.getAmountOutstanding(), PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN,
                            portfolioAccountData.accountId(), chargeData.getLoanId(), "Loan Charge Payment", null, null, null, null,
                            LoanTransactionType.CHARGE_PAYMENT.getValue(), chargeData.getId(), null,
                            AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount,
                            isRegularTransaction, isExceptionForBalanceCheck);
                    transferFeeCharge(accountTransferDTO, errors);
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }

    private void transferFeeCharge(final AccountTransferDTO accountTransferDTO, List<Throwable> errors) {
        try {
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        } catch (RuntimeException e) {
            LOG.error("Exception while paying charge {} for loan id {}", accountTransferDTO.getChargeId(),
                    accountTransferDTO.getToAccountId(), e);
            errors.add(e);
        }
    }

    private LoanCharge retrieveLoanChargeBy(final Long loanId, final Long loanChargeId) {
        final LoanCharge loanCharge = this.loanChargeRepository.findById(loanChargeId)
                .orElseThrow(() -> new LoanChargeNotFoundException(loanChargeId));

        if (loanCharge.hasNotLoanIdentifiedBy(loanId)) {
            throw new LoanChargeNotFoundException(loanChargeId, loanId);
        }
        return loanCharge;
    }

    @Transactional
    @Override
    public LoanTransaction initiateLoanTransfer(final Loan loan, final LocalDate transferDate) {

        AppUser currentUser = getAppUserIfPresent();
        this.loanAssembler.setHelpers(loan);
        checkClientOrGroupActive(loan);
        validateTransactionsForTransfer(loan, transferDate);

        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_INITIATE_TRANSFER,
                constructEntityMap(BusinessEntity.LOAN, loan));

        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferTransaction = LoanTransaction.initiateTransfer(loan.getOffice(), loan, transferDate,
                DateUtils.getLocalDateTimeOfTenant(), currentUser);
        loan.addLoanTransaction(newTransferTransaction);
        loan.setLoanStatus(LoanStatus.TRANSFER_IN_PROGRESS.getValue());

        this.loanTransactionRepository.saveAndFlush(newTransferTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_INITIATE_TRANSFER,
                constructEntityMap(BusinessEntity.LOAN, loan));
        return newTransferTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction acceptLoanTransfer(final Loan loan, final LocalDate transferDate, final Office acceptedInOffice,
                                              final Staff loanOfficer) {
        AppUser currentUser = getAppUserIfPresent();
        this.loanAssembler.setHelpers(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_ACCEPT_TRANSFER,
                constructEntityMap(BusinessEntity.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.approveTransfer(acceptedInOffice, loan, transferDate,
                DateUtils.getLocalDateTimeOfTenant(), currentUser);
        loan.addLoanTransaction(newTransferAcceptanceTransaction);
        if (loan.getTotalOverpaid() != null) {
            loan.setLoanStatus(LoanStatus.OVERPAID.getValue());
        } else {
            loan.setLoanStatus(LoanStatus.ACTIVE.getValue());
        }
        if (loanOfficer != null) {
            loan.reassignLoanOfficer(loanOfficer, transferDate);
        }

        this.loanTransactionRepository.saveAndFlush(newTransferAcceptanceTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_ACCEPT_TRANSFER,
                constructEntityMap(BusinessEntity.LOAN, loan));

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction withdrawLoanTransfer(final Loan loan, final LocalDate transferDate) {
        AppUser currentUser = getAppUserIfPresent();
        this.loanAssembler.setHelpers(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_WITHDRAW_TRANSFER,
                constructEntityMap(BusinessEntity.LOAN, loan));

        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.withdrawTransfer(loan.getOffice(), loan, transferDate,
                DateUtils.getLocalDateTimeOfTenant(), currentUser);
        loan.addLoanTransaction(newTransferAcceptanceTransaction);
        loan.setLoanStatus(LoanStatus.ACTIVE.getValue());

        this.loanTransactionRepository.saveAndFlush(newTransferAcceptanceTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_WITHDRAW_TRANSFER,
                constructEntityMap(BusinessEntity.LOAN, loan));

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public void rejectLoanTransfer(final Loan loan) {
        this.loanAssembler.setHelpers(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_REJECT_TRANSFER,
                constructEntityMap(BusinessEntity.LOAN, loan));
        loan.setLoanStatus(LoanStatus.TRANSFER_ON_HOLD.getValue());
        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_REJECT_TRANSFER,
                constructEntityMap(BusinessEntity.LOAN, loan));
    }

    @Transactional
    @Override
    public CommandProcessingResult loanReassignment(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateUpdateOfLoanOfficer(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");

        final Staff fromLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);
        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_REASSIGN_OFFICER,
                constructEntityMap(BusinessEntity.LOAN, loan));
        if (!loan.hasLoanOfficer(fromLoanOfficer)) {
            throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId);
        }

        loan.reassignLoanOfficer(toLoanOfficer, dateOfLoanOfficerAssignment);

        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_REASSIGN_OFFICER,
                constructEntityMap(BusinessEntity.LOAN, loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult bulkLoanReassignment(final JsonCommand command) {

        this.loanEventApiJsonValidator.validateForBulkLoanReassignment(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");
        final String[] loanIds = command.arrayValueOfParameterNamed("loans");

        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Staff fromLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);

        for (final String loanIdString : loanIds) {
            final Long loanId = Long.valueOf(loanIdString);
            final Loan loan = this.loanAssembler.assembleFrom(loanId);
            this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_REASSIGN_OFFICER,
                    constructEntityMap(BusinessEntity.LOAN, loan));
            checkClientOrGroupActive(loan);

            if (!loan.hasLoanOfficer(fromLoanOfficer)) {
                throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId);
            }

            loan.reassignLoanOfficer(toLoanOfficer, dateOfLoanOfficerAssignment);
            saveLoanWithDataIntegrityViolationChecks(loan);
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_REASSIGN_OFFICER,
                    constructEntityMap(BusinessEntity.LOAN, loan));
        }
        this.loanRepositoryWrapper.flush();

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult removeLoanOfficer(final Long loanId, final JsonCommand command) {

        final LoanUpdateCommand loanUpdateCommand = this.loanUpdateCommandFromApiJsonDeserializer.commandFromApiJson(command.json());

        loanUpdateCommand.validate();

        final LocalDate dateOfLoanOfficerunAssigned = command.localDateValueOfParameterNamed("unassignedDate");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        if (loan.getLoanOfficer() == null) {
            throw new LoanOfficerUnassignmentException(loanId);
        }
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_REMOVE_OFFICER,
                constructEntityMap(BusinessEntity.LOAN, loan));

        loan.removeLoanOfficer(dateOfLoanOfficerunAssigned);

        saveLoanWithDataIntegrityViolationChecks(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_REMOVE_OFFICER,
                constructEntityMap(BusinessEntity.LOAN, loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
                                    final List<Long> existingReversedTransactionIds) {

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    @Transactional
    @Override
    public void applyMeetingDateChanges(final Calendar calendar, final Collection<CalendarInstance> loanCalendarInstances) {

        final Boolean reschedulebasedOnMeetingDates = null;
        final LocalDate presentMeetingDate = null;
        final LocalDate newMeetingDate = null;

        applyMeetingDateChanges(calendar, loanCalendarInstances, reschedulebasedOnMeetingDates, presentMeetingDate, newMeetingDate);

    }

    @Transactional
    @Override
    public void applyMeetingDateChanges(final Calendar calendar, final Collection<CalendarInstance> loanCalendarInstances,
                                        final Boolean reschedulebasedOnMeetingDates, final LocalDate presentMeetingDate, final LocalDate newMeetingDate) {

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final AppUser currentUser = getAppUserIfPresent();
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        final Collection<Integer> loanStatuses = new ArrayList<>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue()));
        final Collection<Integer> loanTypes = new ArrayList<>(Arrays.asList(AccountType.GROUP.getValue(), AccountType.JLG.getValue()));
        final Collection<Long> loanIds = new ArrayList<>(loanCalendarInstances.size());
        // loop through loanCalendarInstances to get loan ids
        for (final CalendarInstance calendarInstance : loanCalendarInstances) {
            loanIds.add(calendarInstance.getEntityId());
        }

        final List<Loan> loans = this.loanRepositoryWrapper.findByIdsAndLoanStatusAndLoanType(loanIds, loanStatuses, loanTypes);
        List<Holiday> holidays = null;
        final LocalDate recalculateFrom = null;
        // loop through each loan to reschedule the repayment dates
        for (final Loan loan : loans) {
            if (loan != null) {
                if (loan.getExpectedFirstRepaymentOnDate() != null && loan.getExpectedFirstRepaymentOnDate().equals(presentMeetingDate)) {
                    final String defaultUserMessage = "Meeting calendar date update is not supported since its a first repayment date";
                    throw new CalendarParameterUpdateNotSupportedException("meeting.for.first.repayment.date", defaultUserMessage,
                            loan.getExpectedFirstRepaymentOnDate(), presentMeetingDate);
                }

                Boolean isSkipRepaymentOnFirstMonth = false;
                Integer numberOfDays = 0;
                boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
                if (isSkipRepaymentOnFirstMonthEnabled) {
                    isSkipRepaymentOnFirstMonth = this.loanUtilService.isLoanRepaymentsSyncWithMeeting(loan.group(), calendar);
                    if (isSkipRepaymentOnFirstMonth) {
                        numberOfDays = configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue();
                    }
                }

                holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                        Date.from(loan.getDisbursementDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                    ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
                    loan.setHelpers(null, this.loanSummaryWrapper, this.transactionProcessingStrategy);
                    loan.recalculateScheduleFromLastTransaction(scheduleGeneratorDTO, existingTransactionIds,
                            existingReversedTransactionIds, currentUser);
                    createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);
                } else if (reschedulebasedOnMeetingDates != null && reschedulebasedOnMeetingDates) {
                    loan.updateLoanRepaymentScheduleDates(calendar.getStartDateLocalDate(), calendar.getRecurrence(), isHolidayEnabled,
                            holidays, workingDays, reschedulebasedOnMeetingDates, presentMeetingDate, newMeetingDate,
                            isSkipRepaymentOnFirstMonth, numberOfDays);
                } else {
                    loan.updateLoanRepaymentScheduleDates(calendar.getStartDateLocalDate(), calendar.getRecurrence(), isHolidayEnabled,
                            holidays, workingDays, isSkipRepaymentOnFirstMonth, numberOfDays);
                }

                saveLoanWithDataIntegrityViolationChecks(loan);
            }
        }
    }

    private void removeLoanCycle(final Loan loan) {
        final List<Loan> loansToUpdate;
        if (loan.isGroupLoan()) {
            if (loan.loanProduct().isIncludeInBorrowerCycle()) {
                loansToUpdate = this.loanRepositoryWrapper.getGroupLoansToUpdateLoanCounter(loan.getCurrentLoanCounter(), loan.getGroupId(),
                        AccountType.GROUP.getValue());
            } else {
                loansToUpdate = this.loanRepositoryWrapper.getGroupLoansToUpdateLoanProductCounter(loan.getLoanProductLoanCounter(),
                        loan.getGroupId(), AccountType.GROUP.getValue());
            }

        } else {
            if (loan.loanProduct().isIncludeInBorrowerCycle()) {
                loansToUpdate = this.loanRepositoryWrapper.getClientOrJLGLoansToUpdateLoanCounter(loan.getCurrentLoanCounter(),
                        loan.getClientId());
            } else {
                loansToUpdate = this.loanRepositoryWrapper.getClientLoansToUpdateLoanProductCounter(loan.getLoanProductLoanCounter(),
                        loan.getClientId());
            }

        }
        if (loansToUpdate != null) {
            updateLoanCycleCounter(loansToUpdate, loan);
        }
        loan.updateClientLoanCounter(null);
        loan.updateLoanProductLoanCounter(null);

    }

    private void updateLoanCounters(final Loan loan, final LocalDate actualDisbursementDate) {

        if (loan.isGroupLoan()) {
            final List<Loan> loansToUpdateForLoanCounter = this.loanRepositoryWrapper.getGroupLoansDisbursedAfter(
                    Date.from(actualDisbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), loan.getGroupId(),
                    AccountType.GROUP.getValue());
            final Integer newLoanCounter = getNewGroupLoanCounter(loan);
            final Integer newLoanProductCounter = getNewGroupLoanProductCounter(loan);
            updateLoanCounter(loan, loansToUpdateForLoanCounter, newLoanCounter, newLoanProductCounter);
        } else {
            final List<Loan> loansToUpdateForLoanCounter = this.loanRepositoryWrapper.getClientOrJLGLoansDisbursedAfter(
                    Date.from(actualDisbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), loan.getClientId());
            final Integer newLoanCounter = getNewClientOrJLGLoanCounter(loan);
            final Integer newLoanProductCounter = getNewClientOrJLGLoanProductCounter(loan);
            updateLoanCounter(loan, loansToUpdateForLoanCounter, newLoanCounter, newLoanProductCounter);
        }
    }

    private Integer getNewGroupLoanCounter(final Loan loan) {

        Integer maxClientLoanCounter = this.loanRepositoryWrapper.getMaxGroupLoanCounter(loan.getGroupId(), AccountType.GROUP.getValue());
        if (maxClientLoanCounter == null) {
            maxClientLoanCounter = 1;
        } else {
            maxClientLoanCounter = maxClientLoanCounter + 1;
        }
        return maxClientLoanCounter;
    }

    private Integer getNewGroupLoanProductCounter(final Loan loan) {

        Integer maxLoanProductLoanCounter = this.loanRepositoryWrapper.getMaxGroupLoanProductCounter(loan.loanProduct().getId(),
                loan.getGroupId(), AccountType.GROUP.getValue());
        if (maxLoanProductLoanCounter == null) {
            maxLoanProductLoanCounter = 1;
        } else {
            maxLoanProductLoanCounter = maxLoanProductLoanCounter + 1;
        }
        return maxLoanProductLoanCounter;
    }

    private void updateLoanCounter(final Loan loan, final List<Loan> loansToUpdateForLoanCounter, Integer newLoanCounter,
                                   Integer newLoanProductCounter) {

        final boolean includeInBorrowerCycle = loan.loanProduct().isIncludeInBorrowerCycle();
        for (final Loan loanToUpdate : loansToUpdateForLoanCounter) {
            // Update client loan counter if loan product includeInBorrowerCycle
            // is true
            if (loanToUpdate.loanProduct().isIncludeInBorrowerCycle()) {
                Integer currentLoanCounter = loanToUpdate.getCurrentLoanCounter() == null ? 1 : loanToUpdate.getCurrentLoanCounter();
                if (newLoanCounter > currentLoanCounter) {
                    newLoanCounter = currentLoanCounter;
                }
                loanToUpdate.updateClientLoanCounter(++currentLoanCounter);
            }

            if (loanToUpdate.loanProduct().getId().equals(loan.loanProduct().getId())) {
                Integer loanProductLoanCounter = loanToUpdate.getLoanProductLoanCounter();
                if (newLoanProductCounter > loanProductLoanCounter) {
                    newLoanProductCounter = loanProductLoanCounter;
                }
                loanToUpdate.updateLoanProductLoanCounter(++loanProductLoanCounter);
            }
        }

        if (includeInBorrowerCycle) {
            loan.updateClientLoanCounter(newLoanCounter);
        } else {
            loan.updateClientLoanCounter(null);
        }
        loan.updateLoanProductLoanCounter(newLoanProductCounter);
        this.loanRepositoryWrapper.save(loansToUpdateForLoanCounter);
    }

    private Integer getNewClientOrJLGLoanCounter(final Loan loan) {

        Integer maxClientLoanCounter = this.loanRepositoryWrapper.getMaxClientOrJLGLoanCounter(loan.getClientId());
        if (maxClientLoanCounter == null) {
            maxClientLoanCounter = 1;
        } else {
            maxClientLoanCounter = maxClientLoanCounter + 1;
        }
        return maxClientLoanCounter;
    }

    private Integer getNewClientOrJLGLoanProductCounter(final Loan loan) {

        Integer maxLoanProductLoanCounter = this.loanRepositoryWrapper.getMaxClientOrJLGLoanProductCounter(loan.loanProduct().getId(),
                loan.getClientId());
        if (maxLoanProductLoanCounter == null) {
            maxLoanProductLoanCounter = 1;
        } else {
            maxLoanProductLoanCounter = maxLoanProductLoanCounter + 1;
        }
        return maxLoanProductLoanCounter;
    }

    private void updateLoanCycleCounter(final List<Loan> loansToUpdate, final Loan loan) {

        final Integer currentLoancounter = loan.getCurrentLoanCounter();
        final Integer currentLoanProductCounter = loan.getLoanProductLoanCounter();

        for (final Loan loanToUpdate : loansToUpdate) {
            if (loan.loanProduct().isIncludeInBorrowerCycle()) {
                Integer runningLoancounter = loanToUpdate.getCurrentLoanCounter();
                if (runningLoancounter > currentLoancounter) {
                    loanToUpdate.updateClientLoanCounter(--runningLoancounter);
                }
            }
            if (loan.loanProduct().getId().equals(loanToUpdate.loanProduct().getId())) {
                Integer runningLoanProductCounter = loanToUpdate.getLoanProductLoanCounter();
                if (runningLoanProductCounter > currentLoanProductCounter) {
                    loanToUpdate.updateLoanProductLoanCounter(--runningLoanProductCounter);
                }
            }
        }
        this.loanRepositoryWrapper.save(loansToUpdate);
    }

    @Transactional
    @Override
    @CronTarget(jobName = JobName.APPLY_HOLIDAYS_TO_LOANS)
    public void applyHolidaysToLoans() {

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();

        if (!isHolidayEnabled) {
            return;
        }

        final Collection<Integer> loanStatuses = new ArrayList<>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue()));
        // Get all Holidays which are active and not processed
        final List<Holiday> holidays = this.holidayRepository.findUnprocessed();

        // Loop through all holidays
        for (final Holiday holiday : holidays) {
            // All offices to which holiday is applied
            final Set<Office> offices = holiday.getOffices();
            final Collection<Long> officeIds = new ArrayList<>(offices.size());
            for (final Office office : offices) {
                officeIds.add(office.getId());
            }

            // get all loans
            final List<Loan> loans = new ArrayList<>();
            // get all individual and jlg loans
            loans.addAll(this.loanRepositoryWrapper.findByClientOfficeIdsAndLoanStatus(officeIds, loanStatuses));
            // FIXME: AA optimize to get all client and group loans belongs to a
            // office id
            // get all group loans
            loans.addAll(this.loanRepositoryWrapper.findByGroupOfficeIdsAndLoanStatus(officeIds, loanStatuses));

            for (final Loan loan : loans) {
                // apply holiday
                loan.applyHolidayToRepaymentScheduleDates(holiday, this.loanUtilService);
            }
            this.loanRepositoryWrapper.save(loans);
            holiday.processed();
        }
        this.holidayRepository.save(holidays);
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
    @Transactional
    public void applyOverdueChargesForLoan(final Long loanId, Collection<OverdueLoanScheduleData> overdueLoanScheduleDatas) {

        Loan loan = null;
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        boolean runInterestRecalculation = false;
        LocalDate recalculateFrom = DateUtils.getLocalDateOfTenant();
        LocalDate lastChargeDate = null;
        for (final OverdueLoanScheduleData overdueInstallment : overdueLoanScheduleDatas) {

            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(overdueInstallment.toString());
            final JsonCommand command = JsonCommand.from(overdueInstallment.toString(), parsedCommand, this.fromApiJsonHelper, null, null,
                    null, null, null, loanId, null, null, null, null, null, null);
            LoanOverdueDTO overdueDTO = applyChargeToOverdueLoanInstallment(loanId, overdueInstallment.getChargeId(),
                    overdueInstallment.getPeriodNumber(), command, loan, existingTransactionIds, existingReversedTransactionIds,
                    overdueInstallment.getGraceOnArrearAgeing());
            loan = overdueDTO.getLoan();
            runInterestRecalculation = runInterestRecalculation || overdueDTO.isRunInterestRecalculation();
            if (recalculateFrom.isAfter(overdueDTO.getRecalculateFrom())) {
                recalculateFrom = overdueDTO.getRecalculateFrom();
            }
            if (lastChargeDate == null || overdueDTO.getLastChargeAppliedDate().isAfter(lastChargeDate)) {
                lastChargeDate = overdueDTO.getLastChargeAppliedDate();
            }
        }
        if (loan != null) {
            boolean reprocessRequired = true;
            LocalDate recalculatedTill = loan.fetchInterestRecalculateFromDate();
            if (recalculateFrom.isAfter(recalculatedTill)) {
                recalculateFrom = recalculatedTill;
            }

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                if (runInterestRecalculation && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                    runScheduleRecalculation(loan, recalculateFrom);
                    reprocessRequired = false;
                }
                updateOriginalSchedule(loan);
            }

//            if (reprocessRequired) {
//                addInstallmentIfPenaltyAppliedAfterLastDueDate(loan, lastChargeDate);
//                ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
//                if (changedTransactionDetail != null) {
//                    for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings()
//                            .entrySet()) {
//                        this.loanTransactionRepository.save(mapEntry.getValue());
//                        // update loan with references to the newly created
//                        // transactions
//                        loan.addLoanTransaction(mapEntry.getValue());
//                        this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
//                    }
//                }
//                saveLoanWithDataIntegrityViolationChecks(loan);
//            }

            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled() && runInterestRecalculation
                    && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                this.loanAccountDomainService.recalculateAccruals(loan);
            }
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_APPLY_OVERDUE_CHARGE,
                    constructEntityMap(BusinessEntity.LOAN, loan));

        }
    }

    private void addInstallmentIfPenaltyAppliedAfterLastDueDate(Loan loan, LocalDate lastChargeDate) {
        if (lastChargeDate != null) {
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            LoanRepaymentScheduleInstallment lastInstallment = loan.fetchRepaymentScheduleInstallment(installments.size());
            if (lastChargeDate.isAfter(lastInstallment.getDueDate())) {
                if (lastInstallment.isRecalculatedInterestComponent()) {
                    installments.remove(lastInstallment);
                    lastInstallment = loan.fetchRepaymentScheduleInstallment(installments.size());
                }
                boolean recalculatedInterestComponent = true;
                BigDecimal principal = BigDecimal.ZERO;
                BigDecimal interest = BigDecimal.ZERO;
                BigDecimal feeCharges = BigDecimal.ZERO;
                BigDecimal penaltyCharges = BigDecimal.ONE;
                BigDecimal selfPrincipal = BigDecimal.ZERO;
                BigDecimal partnerPrincipal = BigDecimal.ZERO;
                BigDecimal selfInterestCharged = BigDecimal.ZERO;
                BigDecimal partnerInterestCharged = BigDecimal.ZERO;
                BigDecimal selfDue = BigDecimal.ZERO;
                BigDecimal partnerDue = BigDecimal.ZERO;

                final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = null;
                LoanRepaymentScheduleInstallment newEntry = new LoanRepaymentScheduleInstallment(loan, installments.size() + 1,
                        lastInstallment.getDueDate(), lastChargeDate, principal, interest, feeCharges, penaltyCharges,
                        recalculatedInterestComponent, compoundingDetails, selfPrincipal, partnerPrincipal, selfInterestCharged, partnerInterestCharged, selfDue, partnerDue, null);
                loan.addLoanRepaymentScheduleInstallment(newEntry);
            }
        }
    }

    public LoanOverdueDTO applyChargeToOverdueLoanInstallment(final Long loanId, final Long loanChargeId, final Integer periodNumber,
                                                              final JsonCommand command, Loan loan, final List<Long> existingTransactionIds,
                                                              final List<Long> existingReversedTransactionIds, final Integer graceOnArrearsAgeing) {
        boolean runInterestRecalculation = false;
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(loanChargeId);

        Collection<Integer> frequencyNumbers = loanChargeReadPlatformService.retrieveOverdueInstallmentChargeFrequencyNumber(loanId,
                chargeDefinition.getId(), periodNumber);

        Integer feeFrequency = chargeDefinition.feeFrequency();
        final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        Map<Integer, LocalDate> scheduleDates = new HashMap<>();
        final Long penaltyWaitPeriodValue = this.configurationDomainService.retrievePenaltyWaitPeriod();
        final Long penaltyPostingWaitPeriodValue = this.configurationDomainService.retrieveGraceOnPenaltyPostingPeriod();
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        // Changes to support both overdue with and without grace period
        LocalDate startDate = null;

        if (graceOnArrearsAgeing != 0) {
            startDate = dueDate.plusDays(graceOnArrearsAgeing + 1);
            if (startDate.isBefore(DateUtils.getLocalDateOfTenant()) || startDate.equals(DateUtils.getLocalDateOfTenant())) {
                startDate = dueDate.plusDays(1);
            }
        } else {
            startDate = dueDate.plusDays(1);
        }

        Integer frequencyNunber = 1;
        if (feeFrequency == null) {
            scheduleDates.put(frequencyNunber++, startDate);
        } else {
            while (!startDate.isAfter(DateUtils.getLocalDateOfTenant())) {
                scheduleDates.put(frequencyNunber++, startDate.minusDays(1));
                LocalDate scheduleDate = scheduledDateGenerator.getRepaymentPeriodDate(PeriodFrequencyType.fromInt(feeFrequency),
                        chargeDefinition.feeInterval(), startDate);
                startDate = scheduleDate;
            }
        }

        for (Integer frequency : frequencyNumbers) {
            scheduleDates.remove(frequency);
        }

        LoanRepaymentScheduleInstallment installment = null;
        LocalDate lastChargeAppliedDate = dueDate;
        if (!scheduleDates.isEmpty()) {
            if (loan == null) {
                loan = this.loanAssembler.assembleFrom(loanId);
                checkClientOrGroupActive(loan);
                existingTransactionIds.addAll(loan.findExistingTransactionIds());
                existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
            }
            installment = loan.fetchRepaymentScheduleInstallment(periodNumber);
            lastChargeAppliedDate = installment.getDueDate();
        }
        LocalDate recalculateFrom = DateUtils.getLocalDateOfTenant();

        if (loan != null) {
            this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_APPLY_OVERDUE_CHARGE,
                    constructEntityMap(BusinessEntity.LOAN, loan));
            for (Map.Entry<Integer, LocalDate> entry : scheduleDates.entrySet()) {
                LoanProductFeesCharges loanProductFeesCharges = loanProductFeesChargesRepository.getByChargeAndLoanProduct(chargeDefinition, loan.getLoanProduct());
                List<LoanPenalForeclosureCharges> loanPenalForeclosureCharges = loanPenalForeclosureChargesRepository.findByLoanId(loan.getId());

                BigDecimal chargeAmount = BigDecimal.ZERO;
                BigDecimal selfShare = BigDecimal.ZERO;
                BigDecimal partnerShare = BigDecimal.ZERO;
                if (loanPenalForeclosureCharges != null) {
                    for (LoanPenalForeclosureCharges charges : loanPenalForeclosureCharges) {
                        if (charges.getCharge().isOverdueInstallment()) {
                            chargeAmount = charges.getAmountOrPercentage();
                            selfShare = charges.getSelfSharePercentage();
                            partnerShare = charges.getPartnerSharePercentage();
                        }
                    }
                }

                final LoanCharge loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command, entry.getValue(), chargeAmount, selfShare, partnerShare, periodNumber, new ArrayList<>());
                if (BigDecimal.ZERO.compareTo(loanCharge.amount()) == 0) {
                    continue;
                }
                LoanOverdueInstallmentCharge overdueInstallmentCharge = new LoanOverdueInstallmentCharge(loanCharge, installment,
                        entry.getKey());
                loanCharge.updateOverdueInstallmentCharge(overdueInstallmentCharge);
                boolean isAppliedOnBackDate = addCharge(loan, chargeDefinition, loanCharge);
               /*
               Removed DPD Calculation
               List<LoanCharge> loanCharges = loanChargeRepository.
                        getActiveLoanChargesByLoanIdAndInstallment(installment.getLoan().getId(), installment.getInstallmentNumber());
                installment.setDaysPastDue(loanCharges.size());*/
                runInterestRecalculation = runInterestRecalculation || isAppliedOnBackDate;
                if (entry.getValue().isBefore(recalculateFrom)) {
                    recalculateFrom = entry.getValue();
                }
                if (entry.getValue().isAfter(lastChargeAppliedDate)) {
                    lastChargeAppliedDate = entry.getValue();
                }
            }
        }

        return new LoanOverdueDTO(loan, runInterestRecalculation, recalculateFrom, lastChargeAppliedDate);
    }

    @Override
    public CommandProcessingResult undoWriteOff(Long loanId) {
        final AppUser currentUser = getAppUserIfPresent();

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        if (!loan.isClosedWrittenOff()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.status.not.written.off.update.not.allowed",
                    "Loan :" + loanId + " update not allowed as loan status is not written off", loanId);
        }
        LocalDate recalculateFrom = null;
        LoanTransaction writeOffTransaction = loan.findWriteOffTransaction();
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_UNDO_WRITTEN_OFF,
                constructEntityMap(BusinessEntity.LOAN_TRANSACTION, writeOffTransaction));

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        ChangedTransactionDetail changedTransactionDetail = loan.undoWrittenOff(existingTransactionIds, existingReversedTransactionIds,
                scheduleGeneratorDTO, currentUser);
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        if (writeOffTransaction != null) {
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_UNDO_WRITTEN_OFF,
                    constructEntityMap(BusinessEntity.LOAN_TRANSACTION, writeOffTransaction));
        }
        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private void validateMultiDisbursementData(final JsonCommand command, LocalDate expectedDisbursementDate,
                                               boolean isDisallowExpectedDisbursements) {
        final String json = command.json();
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        final JsonArray disbursementDataArray = command.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);

        if (isDisallowExpectedDisbursements) {
            if (disbursementDataArray != null) {
                final String errorMessage = "For this loan product, disbursement details are not allowed";
                throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        } else {
            if (disbursementDataArray == null || disbursementDataArray.size() == 0) {
                final String errorMessage = "For this loan product, disbursement details must be provided";
                throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        }

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("approvedLoanAmount", element);

        loanApplicationCommandFromApiJsonHelper.validateLoanMultiDisbursementDate(element, baseDataValidator, expectedDisbursementDate,
                principal);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateForAddAndDeleteTranche(final Loan loan) {

        BigDecimal totalDisbursedAmount = BigDecimal.ZERO;
        Collection<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
        for (LoanDisbursementDetails disbursementDetails : loanDisburseDetails) {
            if (disbursementDetails.actualDisbursementDate() != null) {
                totalDisbursedAmount = totalDisbursedAmount.add(disbursementDetails.principal());
            }
        }
        if (totalDisbursedAmount.compareTo(loan.getApprovedPrincipal()) == 0) {
            final String errorMessage = "loan.disbursement.cannot.be.a.edited";
            throw new LoanMultiDisbursementException(errorMessage);
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult addAndDeleteLoanDisburseDetails(Long loanId, JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        LocalDate expectedDisbursementDate = loan.getExpectedDisbursedOnLocalDate();
        if (!loan.loanProduct().isMultiDisburseLoan()) {
            final String errorMessage = "loan.product.does.not.support.multiple.disbursals";
            throw new LoanMultiDisbursementException(errorMessage);
        }
        if (loan.isSubmittedAndPendingApproval() || loan.isClosed() || loan.isClosedWrittenOff() || loan.status().isClosedObligationsMet()
                || loan.status().isOverpaid()) {
            final String errorMessage = "cannot.modify.tranches.if.loan.is.pendingapproval.closed.overpaid.writtenoff";
            throw new LoanMultiDisbursementException(errorMessage);
        }
        validateMultiDisbursementData(command, expectedDisbursementDate, loan.loanProduct().isDisallowExpectedDisbursements());

        this.validateForAddAndDeleteTranche(loan);

        loan.updateDisbursementDetails(command, actualChanges);

        if (loan.loanProduct().isDisallowExpectedDisbursements()) {
            if (!loan.getDisbursementDetails().isEmpty()) {
                final String errorMessage = "For this loan product, disbursement details are not allowed";
                throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        } else {
            if (loan.getDisbursementDetails().isEmpty()) {
                final String errorMessage = "For this loan product, disbursement details must be provided";
                throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        }

        if (loan.getDisbursementDetails().size() > loan.loanProduct().maxTrancheCount()) {
            final String errorMessage = "Number of tranche shouldn't be greter than " + loan.loanProduct().maxTrancheCount();
            throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                    loan.loanProduct().maxTrancheCount(), loan.getDisbursementDetails().size());
        }
        LoanDisbursementDetails updateDetails = null;
        return processLoanDisbursementDetail(loan, loanId, command, updateDetails);

    }

    private CommandProcessingResult processLoanDisbursementDetail(final Loan loan, Long loanId, JsonCommand command,
                                                                  LoanDisbursementDetails loanDisbursementDetails) {
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
        final Map<String, Object> changes = new LinkedHashMap<>();
        LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        ChangedTransactionDetail changedTransactionDetail = null;
        AppUser currentUser = getAppUserIfPresent();

        if (command.entityId() != null) {

            changedTransactionDetail = loan.updateDisbursementDateAndAmountForTranche(loanDisbursementDetails, command, changes,
                    scheduleGeneratorDTO, currentUser);
        } else {
            // BigDecimal setAmount = loan.getApprovedPrincipal();
            Collection<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
            BigDecimal setAmount = BigDecimal.ZERO;
            for (LoanDisbursementDetails details : loanDisburseDetails) {
                if (details.actualDisbursementDate() != null) {
                    setAmount = setAmount.add(details.principal());
                }
            }

            loan.repaymentScheduleDetail().setPrincipal(setAmount);

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                loan.regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO, currentUser);
            } else {
                loan.regenerateRepaymentSchedule(scheduleGeneratorDTO, currentUser);
                loan.processPostDisbursementTransactions();
            }
        }

        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (command.entityId() != null && changedTransactionDetail != null) {
            for (Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            createLoanScheduleArchive(loan, scheduleGeneratorDTO);
        }
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult updateDisbursementDateAndAmountForTranche(final Long loanId, final Long disbursementId,
                                                                             final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        LoanDisbursementDetails loanDisbursementDetails = loan.fetchLoanDisbursementsById(disbursementId);
        this.loanEventApiJsonValidator.validateUpdateDisbursementDateAndAmount(command.json(), loanDisbursementDetails);

        return processLoanDisbursementDetail(loan, loanId, command, loanDisbursementDetails);

    }

    public LoanTransaction disburseLoanAmountToSavings(final Long loanId, Long loanChargeId, final JsonCommand command,
                                                       final boolean isChargeIdIncludedInJson) {

        LoanTransaction transaction = null;

        this.loanEventApiJsonValidator.validateChargePaymentTransaction(command.json(), isChargeIdIncludedInJson);
        if (isChargeIdIncludedInJson) {
            loanChargeId = command.longValueOfParameterNamed("chargeId");
        }
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.status().isActive()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.LOAN_INACTIVE, loanCharge.getId());
        }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_WAIVED, loanCharge.getId());
        } else if (loanCharge.isPaid()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_PAID, loanCharge.getId());
        }

        if (!loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.CHARGE_NOT_ACCOUNT_TRANSFER, loanCharge.getId());
        }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        Integer loanInstallmentNumber = null;
        BigDecimal amount = loanCharge.amountOutstanding();
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
            final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
            if (dueDate != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
            } else if (installmentNumber != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_WAIVED, loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_PAID, loanCharge.getId());
            }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
            amount = chargePerInstallment.getAmountOutstanding();
        }

        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
        if (portfolioAccountData == null) {
            final String errorMessage = "Charge with id:" + loanChargeId + " requires linked savings account for payment";
            throw new LinkedAccountRequiredException("loanCharge.pay", errorMessage, loanChargeId);
        }
        final SavingsAccount fromSavingsAccount = null;
        final boolean isRegularTransaction = true;
        final boolean isExceptionForBalanceCheck = false;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount, PortfolioAccountType.SAVINGS,
                PortfolioAccountType.LOAN, portfolioAccountData.accountId(), loanId, "Loan Charge Payment", locale, fmt, null, null,
                LoanTransactionType.CHARGE_PAYMENT.getValue(), loanChargeId, loanInstallmentNumber,
                AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount, isRegularTransaction,
                isExceptionForBalanceCheck);
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);

        return transaction;
    }

    @Transactional
    @Override
    public void recalculateInterest(final long loanId) {
        Loan loan = this.loanAssembler.assembleFrom(loanId);
        LocalDate recalculateFrom = loan.fetchInterestRecalculateFromDate();
        AppUser currentUser = getAppUserIfPresent();
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_INTEREST_RECALCULATION,
                constructEntityMap(BusinessEntity.LOAN, loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        ScheduleGeneratorDTO generatorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        ChangedTransactionDetail changedTransactionDetail = loan.recalculateScheduleFromLastTransaction(generatorDTO,
                existingTransactionIds, existingReversedTransactionIds, currentUser);

        saveLoanWithDataIntegrityViolationChecks(loan);

        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                // update loan with references to the newly created
                // transactions
                loan.addLoanTransaction(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_INTEREST_RECALCULATION,
                constructEntityMap(BusinessEntity.LOAN, loan));
    }

    @Override
    public CommandProcessingResult recoverFromGuarantor(final Long loanId) {
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        this.guarantorDomainService.transaferFundsFromGuarantor(loan);
        return new CommandProcessingResultBuilder().withLoanId(loanId).build();
    }

    private void updateLoanTransaction(final Long loanTransactionId, final LoanTransaction newLoanTransaction) {
        final AccountTransferTransaction transferTransaction = this.accountTransferRepository.findByToLoanTransactionId(loanTransactionId);
        if (transferTransaction != null) {
            transferTransaction.updateToLoanTransaction(newLoanTransaction);
            this.accountTransferRepository.save(transferTransaction);
        }
    }

    private void createLoanScheduleArchive(final Loan loan, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);

    }

    private void regenerateScheduleOnDisbursement(final JsonCommand command, final Loan loan, final boolean recalculateSchedule,
                                                  final ScheduleGeneratorDTO scheduleGeneratorDTO, final LocalDate nextPossibleRepaymentDate,
                                                  final Date rescheduledRepaymentDate) {
        AppUser currentUser = getAppUserIfPresent();
        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        BigDecimal emiAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.emiAmountParameterName);
        loan.regenerateScheduleOnDisbursement(scheduleGeneratorDTO, recalculateSchedule, actualDisbursementDate, emiAmount, currentUser,
                nextPossibleRepaymentDate, rescheduledRepaymentDate);
    }

    private List<LoanRepaymentScheduleInstallment> retrieveRepaymentScheduleFromModel(LoanScheduleModel model) {
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        for (final LoanScheduleModelPeriod scheduledLoanInstallment : model.getPeriods()) {
            if (scheduledLoanInstallment.isRepaymentPeriod()) {
                final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(null,
                        scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                        scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                        scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                        scheduledLoanInstallment.penaltyChargesDue(), scheduledLoanInstallment.isRecalculatedInterestComponent(),
                        scheduledLoanInstallment.getLoanCompoundingDetails(), scheduledLoanInstallment.selfPrincipal(), scheduledLoanInstallment.partnerPrincipal(), scheduledLoanInstallment.selfInterestCharged(), scheduledLoanInstallment.partnerInterestCharged(), scheduledLoanInstallment.selfDue(), scheduledLoanInstallment.partnerDue(), null);
                installments.add(installment);
            }
        }
        return installments;
    }

    @Override
    public CommandProcessingResult creditBalanceRefund(Long loanId, JsonCommand command) {
        this.loanEventApiJsonValidator.validateNewRefundTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String noteText = command.stringValueOfParameterNamedAllowingNull("note");
        final String externalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        if (StringUtils.isNotBlank(externalId)) {
            changes.put("externalId", externalId);
        }

        final CommandProcessingResultBuilder commandProcessingResultBuilder = this.loanAccountDomainService.creditBalanceRefund(loanId,
                transactionDate, transactionAmount, noteText, externalId);

        return commandProcessingResultBuilder //
                .withCommandId(command.commandId()).with(changes) //
                .build();

    }

    @Override
    public void updateTransactionForXIRR(Collection<XIRRTransactionUpdateRecord> xirrTransactionUpdateRecords) {
        xirrTransactionUpdateRecords.stream().map(xirrTransactionUpdateRecord -> {
            Office office = officeRepository.getReferenceById(xirrTransactionUpdateRecord.clientId());
            Loan loan = loanRepository.getReferenceById(xirrTransactionUpdateRecord.loanId());
            AppUser appUser = appUserRepository.getReferenceById(2l);
            Date parsedDueDate = DateUtils.convertLocalDateToDate(xirrTransactionUpdateRecord.dueDate());
            return LoanTransaction.xirrCalculationForScheduler(office, loan, appUser, parsedDueDate);
        }).forEach(loanTransactionRepository::saveAndFlush);
    }

    @Override
    public void initiateXirrRecalculation(Collection<XIRRTransactionUpdateRecord> xirrTransactionUpdateRecords) {
        xirrTransactionUpdateRecords
                .stream()
                .map(record -> loanRepository.findById(record.loanId()).get())
                .forEach(loan -> XirrService.xirrCalculation(loan.getRepaymentScheduleInstallments(), loan.getPrincpal().getAmount(), loan.getDisbursementDate(),
                        loan.getCurrency(), loan.getLoanCharges(),
                        loan.retreiveListOfTransactionsPostDisbursement(), loan));
    }

    @Override
    public void calculateDaysPastDueForLoanInstallments(List<OverdueLoanInstallment> overdueLoanInstallments) {
        record DaysPastDueRecord(LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment,
                                 List<LoanDpdHistory> loanDpdHistories) {
        }
        ;

        Function<OverdueLoanInstallment, DaysPastDueRecord> installmentMapper = overdueLoanInstallment -> {
            LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = repaymentScheduleInstallmentRepository
                    .getLoanInstallmentByLoanIdAndInstallment(overdueLoanInstallment.loanId(), overdueLoanInstallment.installmentNumber());
            Integer dpd = Long.valueOf(ChronoUnit.DAYS.between(overdueLoanInstallment.duedate(), LocalDate.now())).intValue();
            loanRepaymentScheduleInstallment.setDaysPastDue(dpd);
            loanRepaymentScheduleInstallment.setDpdLastRunOn(DateUtils.getDateOfTenant());
            loanRepaymentScheduleInstallment.getLoan().setValueDate(DateUtils.getDateOfTenant());
            List<LoanDpdHistory> loanDpdHistories = getCurrentLoanDpdHistories(overdueLoanInstallment);
            // List<LoanDpdHistory> loanDpdHistories = getLoanDpdHistoriesForBackDated(overdueLoanInstallment);
            return new DaysPastDueRecord(loanRepaymentScheduleInstallment, loanDpdHistories);
        };

        LOG.info("[Scheduler - DPD] Found {} Overdue Loans ", overdueLoanInstallments.stream().map(OverdueLoanInstallment::loanId).distinct().count());
        overdueLoanInstallments.stream()
                .map(installmentMapper)
                .forEach(daysPastDueRecord -> {
                    LOG.info("[Scheduler - DPD] Processing - Loan Id: {} Installment: {}",
                            daysPastDueRecord.loanRepaymentScheduleInstallment().getLoan().getId(),
                            daysPastDueRecord.loanRepaymentScheduleInstallment().getInstallmentNumber());
                    repaymentScheduleInstallmentRepository.save(daysPastDueRecord.loanRepaymentScheduleInstallment());
                    daysPastDueRecord.loanDpdHistories().forEach(loanDpdHistoryRepository::save);
                });
    }

    private static List<LoanDpdHistory> getCurrentLoanDpdHistories(OverdueLoanInstallment overdueLoanInstallment) {
        List<LoanDpdHistory> loanDpdHistories = new ArrayList<>();
        if (Objects.isNull(overdueLoanInstallment.dpdLastRunOn())) {
            Integer latestDpd = Long.valueOf(ChronoUnit.DAYS.between(overdueLoanInstallment.duedate(), LocalDate.now())).intValue();
            LoanDpdHistory loanDpdHistory = new LoanDpdHistory(overdueLoanInstallment.loanId(),
                    overdueLoanInstallment.installmentNumber(), latestDpd, false,
                    // Sub 1 day to get dpdOnDate
                    Date.from(DateUtils.getLocalDateOfTenant().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Date.from(DateUtils.getLocalDateOfTenant().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            loanDpdHistories.add(loanDpdHistory);
        } else {
            LocalDate duePastDate = overdueLoanInstallment.dpdLastRunOn();
            Integer previousDpd = Long.valueOf(ChronoUnit.DAYS.between(overdueLoanInstallment.duedate(), overdueLoanInstallment.dpdLastRunOn())).intValue();
            while (duePastDate.isBefore(DateUtils.getLocalDateOfTenant())) {
                LoanDpdHistory loanDpdHistory = new LoanDpdHistory(overdueLoanInstallment.loanId(),
                        overdueLoanInstallment.installmentNumber(), ++previousDpd, false,
                        Date.from(duePastDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(DateUtils.getLocalDateOfTenant().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                loanDpdHistories.add(loanDpdHistory);
                duePastDate = duePastDate.plusDays(1);
            }
        }
        return loanDpdHistories;
    }

    private static List<LoanDpdHistory> getLoanDpdHistoriesForBackDated(OverdueLoanInstallment overdueLoanInstallment) {
        List<LoanDpdHistory> loanDpdHistories = new ArrayList<>();
        if (Objects.isNull(overdueLoanInstallment.dpdLastRunOn())) {
            LocalDate duePastDate = overdueLoanInstallment.duedate();
            int daysCount = 1;
            while (duePastDate.isBefore(DateUtils.getLocalDateOfTenant())) {
                LoanDpdHistory loanDpdHistory = new LoanDpdHistory(overdueLoanInstallment.loanId(),
                        overdueLoanInstallment.installmentNumber(), daysCount++, false,
                        Date.from(duePastDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(DateUtils.getLocalDateOfTenant().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                loanDpdHistories.add(loanDpdHistory);
                duePastDate = duePastDate.plusDays(1);
            }
        } else {
            LocalDate lastRunDate = overdueLoanInstallment.dpdLastRunOn().plusDays(1);
            Integer previousDpd = Long.valueOf(ChronoUnit.DAYS.between(overdueLoanInstallment.duedate(), overdueLoanInstallment.dpdLastRunOn())).intValue();
            while (lastRunDate.isBefore(DateUtils.getLocalDateOfTenant())) {
                LoanDpdHistory loanDpdHistory = new LoanDpdHistory(overdueLoanInstallment.loanId(),
                        overdueLoanInstallment.installmentNumber(), ++previousDpd, false,
                        Date.from(lastRunDate.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(DateUtils.getLocalDateOfTenant().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                loanDpdHistories.add(loanDpdHistory);
                lastRunDate = lastRunDate.plusDays(1);
            }
        }
        return loanDpdHistories;
    }


    @Override
    public void calculateAdvanceAmountOnDueDate(List<AppropriateAdvanceAmountOnDueDate> AppropriateAdvanceAmountOnDueDate) throws JobExecutionException {
        int success = 0;
        int failure = 0;
        for (AppropriateAdvanceAmountOnDueDate appropriateAdvanceAmountOnDueDate : AppropriateAdvanceAmountOnDueDate) {
            try {
                RepaymentRequest repaymentRequest = mapToRepaymentRequest(appropriateAdvanceAmountOnDueDate);
                LOG.info("[Scheduler - Advance Amount Appropriation on Due Date] Processing - Loan Id: {}", appropriateAdvanceAmountOnDueDate.loanId());
                final CommandWrapper commandRequest = new CommandWrapperBuilder()
                        .loanRepaymentTransaction(appropriateAdvanceAmountOnDueDate.loanId())
                        .withJson(new Gson().toJson(repaymentRequest))
                        .build();
                CommandProcessingResult commandProcessingResult = commandsSourceWritePlatformService
                        .logCommandSource(commandRequest);
                success++;
            } catch (Exception e) {
                LOG.error(" Failed {} Exception {}", appropriateAdvanceAmountOnDueDate.loanId(), e.getMessage());
                failure++;
            }
        }
        LOG.info("Advance Scheduler-Total Records Processed {} [Success: {}, Failure: {}]", AppropriateAdvanceAmountOnDueDate.size(), success, failure);
    }

    @Override
    public void updateMaxDPD(List<OverdueLoanInstallment> overdueLoanInstallments) {
        overdueLoanInstallments.stream().map(OverdueLoanInstallment::loanId).distinct().forEach(loanId -> {
            Loan loan = loanRepository.getReferenceById(loanId);
            Integer maxDpd = loan.getRepaymentScheduleInstallments().stream()
                    .map(LoanRepaymentScheduleInstallment::getDaysPastDue)
                    .filter(Objects::nonNull).max(Integer::compare)
                    .orElse(0);
            loan.setDaysPastDue(maxDpd);
            loan.setValueDate(DateUtils.getDateOfTenant());
            loanRepository.save(loan);
        });
    }

    private RepaymentRequest mapToRepaymentRequest(AppropriateAdvanceAmountOnDueDate appropriateAdvanceAmountOnDueDate) {

        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setInstallmentNumber(appropriateAdvanceAmountOnDueDate.installmentNumber());
        repaymentRequest.setTransactionAmount(BigDecimal.ZERO);
        String transactionDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        repaymentRequest.setTransactionDate(transactionDate);
        repaymentRequest.setDateFormat(BulkUploadConstants.DB_DATE_FORMAT);
        repaymentRequest.setLocale(BulkUploadConstants.LOCALE);
        repaymentRequest.setTriggeredBy(1);

        return repaymentRequest;
    }

    @Override
    @Transactional
    public CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command) {

        this.loanEventApiJsonValidator.validateNewRefundTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        // checkRefundDateIsAfterAtLeastOneRepayment(loanId, transactionDate);

        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        checkIfLoanIsPaidInAdvance(loanId, transactionAmount);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }

        final PaymentDetail paymentDetail = null;

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();

        this.loanAccountDomainService.makeRefundForActiveLoan(loanId, commandProcessingResultBuilder, transactionDate, transactionAmount,
                paymentDetail, noteText, null);

        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();

    }

    private void checkIfLoanIsPaidInAdvance(final Long loanId, final BigDecimal transactionAmount) {
        BigDecimal overpaid = this.loanReadPlatformService.retrieveTotalPaidInAdvance(loanId).getPaidInAdvance();

        if (overpaid == null || overpaid.compareTo(BigDecimal.ZERO) == 0 ? Boolean.TRUE
                : Boolean.FALSE || transactionAmount.floatValue() > overpaid.floatValue()) {
            if (overpaid == null) {
                overpaid = BigDecimal.ZERO;
            }
            throw new InvalidPaidInAdvanceAmountException(overpaid.toPlainString());
        }
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    private Map<BusinessEntity, Object> constructEntityMap(final BusinessEntity entityEvent, Object entity) {
        Map<BusinessEntity, Object> map = new HashMap<>(1);
        map.put(entityEvent, entity);
        return map;
    }

    @Override
    @Transactional
    public CommandProcessingResult undoLastLoanDisbursal(Long loanId, JsonCommand command) {
        final AppUser currentUser = getAppUserIfPresent();

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        final LocalDate recalculateFromDate = loan.getLastRepaymentDate();
        validateIsMultiDisbursalLoanAndDisbursedMoreThanOneTranche(loan);
        checkClientOrGroupActive(loan);
        this.businessEventNotifierService.notifyBusinessEventToBeExecuted(BusinessEvents.LOAN_UNDO_LASTDISBURSAL,
                constructEntityMap(BusinessEntity.LOAN, loan));

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFromDate);

        final Map<String, Object> changes = loan.undoLastDisbursal(scheduleGeneratorDTO, existingTransactionIds,
                existingReversedTransactionIds, currentUser, loan);
        if (!changes.isEmpty()) {
            saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            String noteText = null;
            if (command.hasParameter("note")) {
                noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    final Note note = Note.loanNote(loan, noteText);
                    this.noteRepository.save(note);
                }
            }
            boolean isAccountTransfer = false;
            final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                    existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
            this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BusinessEvents.LOAN_UNDO_LASTDISBURSAL,
                    constructEntityMap(BusinessEntity.LOAN, loan));
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult forecloseLoan(final Long loanId, final JsonCommand command) {
        final String json = command.json();
        final JsonElement element = fromApiJsonHelper.parse(json);
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.transactionDateParamName, element);
        if(transactionDate.isEqual(loan.getMaturityDate()) || transactionDate.isAfter(loan.getMaturityDate())){
            final String errorMessage = "Foreclosure is Not Allowed On and After Maturity date of Loan";
            throw new InvalidLoanTransactionTypeException("foreclosure", "foreclosure.is.Allowed.only.before.maturity.date.of.loan", errorMessage);
        }
        this.loanEventApiJsonValidator.validateLoanForeclosure(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", transactionDate);

        String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        final String receiptReferenceNumber = this.fromApiJsonHelper.extractStringNamed("receiptReferenceNumber", element);
        final String partnerTransferUtr = this.fromApiJsonHelper.extractStringNamed("partnerTransferUtr", element);
        final Date partnerTransferDate = command.dateValueOfParameterNamed("partnerTransferDate");
        CodeValue repaymentMode = null;
        final Long repaymentModeId = command.longValueOfParameterNamed(LoanApiConstants.repaymentModeIdParamName);
        if (repaymentModeId != null) {
            repaymentMode = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(LoanRepaymentConstants.REPAYMENT_MODE, repaymentModeId);
        }
        LoanRescheduleRequest loanRescheduleRequest = null;
        for (LoanDisbursementDetails loanDisbursementDetails : loan.getDisbursementDetails()) {
            if (!loanDisbursementDetails.expectedDisbursementDateAsLocalDate().isAfter(transactionDate)
                    && loanDisbursementDetails.actualDisbursementDate() == null) {
                final String defaultUserMessage = "The loan with undisbrsed tranche before foreclosure cannot be foreclosed.";
                throw new LoanForeclosureException("loan.with.undisbursed.tranche.before.foreclosure.cannot.be.foreclosured",
                        defaultUserMessage, transactionDate);
            }
        }
        /**
         * Adding value date
         */
        loan.setValueDate(Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        this.loanScheduleHistoryWritePlatformService.createAndSaveLoanScheduleArchive(loan.getRepaymentScheduleInstallments(), loan,
                loanRescheduleRequest);

        final Map<String, Object> modifications = this.loanAccountDomainService.foreCloseLoan(loan, transactionDate, noteText, receiptReferenceNumber, partnerTransferUtr, partnerTransferDate, repaymentMode);
        changes.putAll(modifications);

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        return commandProcessingResultBuilder.withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private void validateIsMultiDisbursalLoanAndDisbursedMoreThanOneTranche(Loan loan) {
        if (!loan.isMultiDisburmentLoan()) {
            final String errorMessage = "loan.product.does.not.support.multiple.disbursals.cannot.undo.last.disbursal";
            throw new LoanMultiDisbursementException(errorMessage);
        }
        Integer trancheDisbursedCount = 0;
        for (LoanDisbursementDetails disbursementDetails : loan.getDisbursementDetails()) {
            if (disbursementDetails.actualDisbursementDate() != null) {
                trancheDisbursedCount++;
            }
        }
        if (trancheDisbursedCount <= 1) {
            final String errorMessage = "tranches.should.be.disbursed.more.than.one.to.undo.last.disbursal";
            throw new LoanMultiDisbursementException(errorMessage);
        }

    }

    private void syncExpectedDateWithActualDisbursementDate(final Loan loan, LocalDate actualDisbursementDate) {
        if (!loan.getExpectedDisbursedOnLocalDate().equals(actualDisbursementDate)) {
            throw new DateMismatchException(actualDisbursementDate, loan.getExpectedDisbursedOnLocalDate());
        }

    }

    private void validateTransactionsForTransfer(final Loan loan, final LocalDate transferDate) {

        for (LoanTransaction transaction : loan.getLoanTransactions()) {
            if ((transaction.getTransactionDate().isEqual(transferDate)
                    && transaction.getCreatedDateTime().isEqual(transferDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()))
                    || transaction.getTransactionDate().isAfter(transferDate)) {
                throw new GeneralPlatformDomainRuleException(TransferApiConstants.transferClientLoanException,
                        TransferApiConstants.transferClientLoanExceptionMessage, transaction.getCreatedDateTime().toLocalDate(),
                        transferDate);
            }

        }

    }

}
