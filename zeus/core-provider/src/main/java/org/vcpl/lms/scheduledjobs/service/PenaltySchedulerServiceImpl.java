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
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.service.GstServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.service.LoanChargeActions;
import org.vcpl.lms.scheduledjobs.data.OverdueLoan;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.vcpl.lms.useradministration.domain.AppUserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@AllArgsConstructor
public class PenaltySchedulerServiceImpl implements PenaltySchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(PenaltySchedulerServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final LoanRepository loanRepository;
    private final AppUserRepository appUserRepository;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final LoanSchedulerRegistryRepository loanSchedulerRegistryRepository;

    private final ResultSetExtractor<List<OverdueLoan>> overdueLoansAndInstallmentMapper = (resultSet -> {
        Map<Long, OverdueLoan> loanInstallmentMap = new HashMap<>();
        while (resultSet.next()) {
            LocalDate lastRunLocalDate = null;
            Long loanId = resultSet.getLong("loan_id");
            Date lastRunDate = resultSet.getDate("penal_last_run_on");
            if(Objects.nonNull(lastRunDate)) {
                lastRunLocalDate = DateUtils.convertDateToLocalDate(lastRunDate);
            }

            Integer installment = resultSet.getInt("installment");
            if(Objects.isNull(lastRunLocalDate) || !lastRunLocalDate.equals(LocalDate.now())) {
                if (loanInstallmentMap.containsKey(loanId)) {
                    LoanSchedulerRegistry registry = new LoanSchedulerRegistry(loanId,installment,lastRunDate);
                    loanInstallmentMap.get(loanId)
                            .addOverdueInstallments(installment, registry);
                } else {
                    OverdueLoan overdueLoan = new OverdueLoan();
                    overdueLoan.setLoanId(loanId);
                    overdueLoan.setChargeId(resultSet.getLong("charge_id"));
                    overdueLoan.setAmount(resultSet.getBigDecimal("amount"));
                    overdueLoan.setSelfShare(resultSet.getBigDecimal("self_share"));
                    overdueLoan.setPartnerShare(resultSet.getBigDecimal("partner_share"));
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
    public List<OverdueLoan> retrieveOverdueLoans() {
        String query = """
            SELECT rs.loan_id, rs.installment, rs.duedate, rs.principal_amount, rs.interest_amount,
                mc.id as charge_id, lpfc.self_share_percentage as self_share,
                lpfc.partner_share_percentage as partner_share, lpfc.amount_or_percentage as amount,
                reg.id,reg.penal_last_run_on
            FROM m_loan l INNER JOIN m_loan_repayment_schedule rs ON l.id = rs.loan_id 
                INNER JOIN m_product_loan pl ON l.product_id = pl.id 
                INNER JOIN m_loan_penal_foreclosure_charge lpfc ON lpfc.loan_id = rs.loan_id 
                INNER JOIN m_charge mc ON mc.id = lpfc.charge_id
                LEFT JOIN m_loan_scheduler_registry reg on reg.loan_id = rs.loan_id and  reg.installment = rs.installment 
            WHERE mc.charge_applies_to_enum = 1 -- applied only on loan
                  AND l.loan_status_id = 300 -- only for active loans
                  AND ADDDATE(rs.duedate, l.grace_on_arrears_ageing-2)  < CURRENT_DATE()  -- calculating the due date including grace period
                  AND ((rs.principal_amount + rs.interest_amount) - (ifnull(rs.principal_completed_derived,0) + ifnull(rs.interest_completed_derived,0)) != 0) -- Unpaid / Partially paid
                  AND mc.charge_time_enum = 9 -- overdue installment
            ORDER BY rs.loan_id,installment
            """;
        return jdbcTemplate.query(query, overdueLoansAndInstallmentMapper);
    }

    @CronTarget(jobName = JobName.APPLY_CHARGE_TO_OVERDUE_LOAN_INSTALLMENT)
    @Override
    public void process() {
        LocalDateTime startDate = LocalDateTime.now();
        AppUser systemUser = appUserRepository.findAppUserByName("system");
        List<OverdueLoan> overdueLoans = retrieveOverdueLoans();
        LOG.info("Total Overdue Loans {}", overdueLoans.size());
        overdueLoans.stream().forEach(overdueLoan -> {
            LocalDateTime loanProcessingStartDate = LocalDateTime.now();
            LOG.info("Processing Loan Id: {} Installments: {} ", overdueLoan.getLoanId(), overdueLoan.getOverdueInstallments().size());
            Loan loan = loanRepository.getReferenceById(overdueLoan.getLoanId());

            applyPenalty(overdueLoan, loan, systemUser);
            LocalDateTime loanProcessingEndDate = LocalDateTime.now();
            LOG.info("Processing Loan -> Time Taken : {} ", ChronoUnit.SECONDS.between(loanProcessingStartDate, loanProcessingEndDate));
        });
        LocalDateTime endDate = LocalDateTime.now();
        LOG.info("Time Taken : {}", ChronoUnit.SECONDS.between(startDate, endDate));
    }

    @Override
    public void applyPenalty(final OverdueLoan overdueLoan, final Loan loan, final AppUser systemUser) {
        Charge penalCharge = loan.getLoanPenalForeclosueCharges().stream()
                .map(LoanPenalForeclosureCharges::getCharge)
                .filter(charge -> charge.getId().equals(overdueLoan.getChargeId()))
                .findAny()
                .get();

        loan.getRepaymentScheduleInstallments().stream()
                .filter(loanRepaymentScheduleInstallment -> overdueLoan.getOverdueInstallments()
                        .containsKey(loanRepaymentScheduleInstallment.getInstallmentNumber()))
                .forEach(loanRepaymentScheduleInstallment -> {
                    LoanSchedulerRegistry loanSchedulerRegistry = loanSchedulerRegistryRepository
                            .getByLoanIdAndInstallment(overdueLoan.getLoanId(),loanRepaymentScheduleInstallment.getInstallmentNumber())
                            .orElseGet(() -> new LoanSchedulerRegistry(overdueLoan.getLoanId(), loanRepaymentScheduleInstallment.getInstallmentNumber()));
                    Date lastRanDate = loanSchedulerRegistry.getPenalLastRunOn();
                    LocalDate nextRunDate = Objects.isNull(lastRanDate)
                            ? loanRepaymentScheduleInstallment.getDueDate().plusDays(1)
                            : nextRunDate(penalCharge, DateUtils.convertDateToLocalDate(lastRanDate));

                    while (Objects.nonNull(nextRunDate) && (nextRunDate.isBefore(LocalDate.now()) || nextRunDate.equals(LocalDate.now()))) {
                        LoanCharge loanCharge = LoanChargeActions.createCharge(loan, systemUser, loanRepaymentScheduleInstallment,
                                penalCharge, nextRunDate, overdueLoan.getSelfShare(), overdueLoan.getPartnerShare(), overdueLoan.getAmount());
                        loan.getCharges().add(loanCharge);
                        loanSchedulerRegistry.setPenalLastRunOn(DateUtils.convertLocalDateToDate(nextRunDate));
                        loanRepaymentScheduleInstallment
                                .setPenaltyCharges(loanRepaymentScheduleInstallment.getPenaltyChargesCharged().add(loanCharge.getAmount()));
                        loanRepaymentScheduleInstallment
                                .setSelfPenaltyCharges(Objects.requireNonNullElse(loanRepaymentScheduleInstallment.getSelfPenaltyChargesCharged(), BigDecimal.ZERO)
                                        .add(loanCharge.getSelfAmountOutstanding()));
                        loanRepaymentScheduleInstallment
                                .setPartnerPenaltyCharges(Objects.requireNonNullElse(loanRepaymentScheduleInstallment.getPartnerPenaltyChargesCharged(), BigDecimal.ZERO)
                                        .add(loanCharge.getPartnerAmountOutstanding()));
                        nextRunDate = nextRunDate(penalCharge, nextRunDate);
                    }
                    loanSchedulerRegistryRepository.save(loanSchedulerRegistry);
                });
        // Summarizing the outstanding at loan level
        loan.getSummary().updateSummary(loan.getCurrency(), loan.getPrincpal(), loan.getRepaymentScheduleInstallments(), loanSummaryWrapper, true,
                loan.charges(), loan.getSelfPrincipaAmount(), loan.getPartnerPrincipalAmount());
        loanRepository.save(loan);
    }

    private LocalDate nextRunDate(final Charge penalCharge, final LocalDate lastRunDate) {
        switch (PeriodFrequencyType.fromInt(penalCharge.getFeeFrequency())) {
            case DAYS -> {
                return lastRunDate.plusDays(penalCharge.feeInterval());
            }
            case WEEKS -> {
                return lastRunDate.plusWeeks(penalCharge.feeInterval());
            }
            case MONTHS -> {
                return lastRunDate.plusMonths(penalCharge.feeInterval());
            }
            case YEARS -> {
                return lastRunDate.plusYears(penalCharge.feeInterval());
            }
            default -> {
                return null;
            }
        }
    }
}
