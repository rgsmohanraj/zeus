package org.vcpl.lms.scheduledjobs.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.jobs.annotation.CronTarget;
import org.vcpl.lms.infrastructure.jobs.service.JobName;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanaccount.data.GstData;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.impl.Collection;
import org.vcpl.lms.portfolio.loanaccount.service.GstService;
import org.vcpl.lms.portfolio.loanaccount.service.LoanChargeActions;
import org.vcpl.lms.scheduledjobs.data.OverdueLoan;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.vcpl.lms.useradministration.domain.AppUserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@AllArgsConstructor
public class BounceChargeSchedulerServiceImpl implements BounceChargeSchedulerService,Collection {

    private static final Logger LOG = LoggerFactory.getLogger(BounceChargeSchedulerServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final LoanRepository loanRepository;
    private final AppUserRepository appUserRepository;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final LoanSchedulerRegistryRepository loanSchedulerRegistryRepository;
    private final GstService gstService;
    private final LoanHistoryRepo loanHistoryRepo;

    @Override
    public List<OverdueLoan> retrieveOverdueInstallmentsforBounceChargeCalc() {
        String query = """
                SELECT loanId, installment, dueDate, chargeId, selfShare, partnerShare, amount, regId, bounceLastRunOn, partnerId from
                       (select rs.loan_id as loanId, rs.installment as installment, rs.duedate as dueDate,
                       bc.charge_id as chargeId, bc.self_share_percentage as selfShare,
                       bc.partner_share_percentage as partnerShare, bc.amount_or_percentage as amount,
                       reg.id as regId, reg.bounce_last_run_on as bounceLastRunOn, lo.partner_id as partnerId from m_loan lo
                       left join m_loan_repayment_schedule rs on rs.loan_id = lo.id
                       left join m_loan_penal_foreclosure_charge bc on bc.loan_id = lo.id and bc.product_id = lo.product_id
                       left join m_charge ch on ch.id = bc.charge_id
                       LEFT JOIN m_loan_scheduler_registry reg on reg.loan_id = rs.loan_id and  reg.installment = rs.installment
                       where rs.duedate < current_date()
                       and lo.loan_status_id = 300
                       and rs.completed_derived <> true
                       and ch.charge_time_enum = 19
                       and ch.is_active = true) bci where (loanId, installment)
                       not in (select loan_id, installment from m_loan_charge where charge_time_enum =19)
                ORDER BY loanId, installment
                """;
        return jdbcTemplate.query(query, overdueLoansAndInstallmentMapper);
    }

    private final ResultSetExtractor<List<OverdueLoan>> overdueLoansAndInstallmentMapper = (resultSet -> {
        Map<Long, OverdueLoan> loanInstallmentMap = new HashMap<>();
        while (resultSet.next()) {
            LocalDate lastRunLocalDate = null;
            Long loanId = resultSet.getLong("loanId");
            Date lastRunDate = resultSet.getDate("bounceLastRunOn");
            if(Objects.nonNull(lastRunDate)) {
                lastRunLocalDate = DateUtils.convertDateToLocalDate(lastRunDate);
            }

            Integer installment = resultSet.getInt("installment");
            if(Objects.isNull(lastRunLocalDate) || !lastRunLocalDate.equals(LocalDate.now())) {
                if (loanInstallmentMap.containsKey(loanId)) {
                    // Date lastRunDate = resultSet.getDate("last_run_date");
                    LoanSchedulerRegistry registry = new LoanSchedulerRegistry(loanId,installment,lastRunDate);
                    loanInstallmentMap.get(loanId)
                            .addOverdueInstallments(installment, registry);
                } else {
                    OverdueLoan overdueLoan = new OverdueLoan();
                    overdueLoan.setLoanId(loanId);
                    overdueLoan.setChargeId(resultSet.getLong("chargeId"));
                    overdueLoan.setAmount(resultSet.getBigDecimal("amount"));
                    overdueLoan.setSelfShare(resultSet.getBigDecimal("selfShare"));
                    overdueLoan.setPartnerShare(resultSet.getBigDecimal("partnerShare"));
                    overdueLoan.setOverdueInstallments(new HashMap<>());
                    LoanSchedulerRegistry registry = new LoanSchedulerRegistry(loanId,installment,lastRunDate);
                    overdueLoan.addOverdueInstallments(installment, registry);
                    loanInstallmentMap.put(loanId, overdueLoan);
                }
            }
        }
        return loanInstallmentMap.values().stream().toList();
    });

    @Override
    @CronTarget(jobName = JobName.APPLY_BOUNCE_CHARGE_TO_OVERDUE_LOANS)
    public void process() {
        LocalDateTime startDate = LocalDateTime.now();
        AppUser systemUser = appUserRepository.findAppUserByName("system");
        List<OverdueLoan> overdueLoans = retrieveOverdueInstallmentsforBounceChargeCalc();
        LOG.info("Total Overdue Loans {}", overdueLoans.size());
        overdueLoans.stream().forEach(overdueLoan -> {
            LocalDateTime loanProcessingStartDate = LocalDateTime.now();
            LOG.info("Processing Loan Id: {} Installments: {} ", overdueLoan.getLoanId(), overdueLoan.getOverdueInstallments().size());
            Loan loan = loanRepository.getReferenceById(overdueLoan.getLoanId());
            applyBounceCharge(overdueLoan, loan, systemUser);
            LocalDateTime loanProcessingEndDate = LocalDateTime.now();
            LOG.info("Processing Loan -> Time Taken : {} ", ChronoUnit.SECONDS.between(loanProcessingStartDate, loanProcessingEndDate));
        });
        LocalDateTime endDate = LocalDateTime.now();
        LOG.info("Time Taken : {}", ChronoUnit.SECONDS.between(startDate, endDate));
    }

    @Override
    public void applyBounceCharge(final OverdueLoan bounceLoan, final Loan loan, final AppUser systemUser) {
        Charge bounceCharge = loan.getLoanPenalForeclosueCharges().stream()
                .map(LoanPenalForeclosureCharges::getCharge)
                .filter(charge -> charge.getId().equals(bounceLoan.getChargeId()))
                .findAny()
                .get();
        final BigDecimal[] totalGst = {BigDecimal.ZERO};
        final BigDecimal[] selfGst = {BigDecimal.ZERO};
        final BigDecimal[] partnerGst = {BigDecimal.ZERO};
        loan.getRepaymentScheduleInstallments().stream()
                .filter(loanRepaymentScheduleInstallment -> bounceLoan.getOverdueInstallments()
                        .containsKey(loanRepaymentScheduleInstallment.getInstallmentNumber()))
                .forEach(loanRepaymentScheduleInstallment -> {

                    LoanSchedulerRegistry loanSchedulerRegistry = loanSchedulerRegistryRepository
                            .getByLoanIdAndInstallment(bounceLoan.getLoanId(), loanRepaymentScheduleInstallment.getInstallmentNumber())
                            .orElseGet(() -> new LoanSchedulerRegistry(bounceLoan.getLoanId(), loanRepaymentScheduleInstallment.getInstallmentNumber()));
                    Date lastRanDate = loanSchedulerRegistry.getBounceLastRunOn();
                    LocalDate nextRunDate = Objects.isNull(lastRanDate)
                            ? loanRepaymentScheduleInstallment.getDueDate().plusDays(1)
                            : nextRunDate(bounceCharge, DateUtils.convertDateToLocalDate(lastRanDate));
                    while (Objects.nonNull(nextRunDate) && (nextRunDate.isBefore(LocalDate.now()) || nextRunDate.equals(LocalDate.now()))) {
                        LoanCharge loanCharge = LoanChargeActions.createCharge(loan, systemUser, loanRepaymentScheduleInstallment,
                                bounceCharge, nextRunDate, bounceLoan.getSelfShare(), bounceLoan.getPartnerShare(),bounceLoan.getAmount());
                        loan.getCharges().add(loanCharge);
                        loanSchedulerRegistry.setBounceLastRunOn(DateUtils.convertLocalDateToDate(nextRunDate));
                        loanRepaymentScheduleInstallment.setBounceCharges(loanRepaymentScheduleInstallment.getBounceChargesCharged().add(loanCharge.getAmount()));
                        loanRepaymentScheduleInstallment
                                .setSelfBounceCharges(Objects.requireNonNullElse(loanRepaymentScheduleInstallment.getSelfBounceChargesCharged(), BigDecimal.ZERO)
                                        .add(loanCharge.getSelfAmountOutstanding()));
                        loanRepaymentScheduleInstallment
                                .setPartnerBounceCharges(Objects.requireNonNullElse(loanRepaymentScheduleInstallment.getPartnerBounceChargesCharged(), BigDecimal.ZERO)
                                        .add(loanCharge.getPartnerAmountOutstanding()));
                        if(loanCharge.enabelGst()) {
                            updateGstLoanCharge(loanCharge);
                        }

                        totalGst[0] = totalGst[0].add(loanCharge.getTotalGst());
                        selfGst[0] = selfGst[0].add(loanCharge.getSelfGst());
                        partnerGst[0] = partnerGst[0].add(loanCharge.getPartnerGst());
                        loanCharge.setCreatedDate(DateUtils.getDateOfTenant());
                        loanCharge.setCreatedUser(systemUser);
                        nextRunDate = nextRunDate(bounceCharge, nextRunDate);
                    }
                        loan.updateGstForSelfAndPartnerInLoanForBounce(totalGst[0], selfGst[0], partnerGst[0], false);

                    loanSchedulerRegistryRepository.save(loanSchedulerRegistry);
                });


        loan.getSummary().updateSummary(loan.getCurrency(), loan.getPrincpal(), loan.getRepaymentScheduleInstallments(), loanSummaryWrapper, true,
                loan.charges(), loan.getSelfPrincipaAmount(), loan.getPartnerPrincipalAmount());
        loanRepository.save(loan);
    }
    private void updateGstLoanCharge(LoanCharge loanCharge) {
       GstData gstData = this.gstService.calculateGstPostDisbursementCharges(loanCharge);
        loanCharge.setIgstAmount(gstData.getIgstAmount());
        loanCharge.setTotalGst(gstData.getTotalGst());
        loanCharge.setCgstAmount(gstData.getCgstAmount());
        loanCharge.setSgstAmount(gstData.getSgstAmount());
        loanCharge.setSelfGst(gstData.getSelfGst());
        loanCharge.setPartnerGst(gstData.getPartnerGst());
        BigDecimal amount = loanCharge.getAmount();
        loanCharge.setAmount(amount.subtract(gstData.getTotalGst()));
        loanCharge.updateSplitShareAmount(loanCharge.getAmount());
        loanCharge.setAmountOutstanding(loanCharge.getAmount());
        loanCharge.setAmountOrPercentage(loanCharge.getAmount());
        loanCharge.setSelfGstOutstanding(loanCharge.getSelfGst());
        loanCharge.setPartnerGstOutstanding(loanCharge.getPartnerGst());
        loanCharge.setGstOutstandingDerived(gstData.getTotalGst());
    }

    private LocalDate nextRunDate(final Charge penalCharge, final LocalDate lastRunDate) {
            return null;
        }

}
