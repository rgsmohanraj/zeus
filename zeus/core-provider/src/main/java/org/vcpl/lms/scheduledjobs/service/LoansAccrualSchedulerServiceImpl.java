package org.vcpl.lms.scheduledjobs.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.jobs.annotation.CronTarget;
import org.vcpl.lms.infrastructure.jobs.service.JobName;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.scheduledjobs.data.InstallmentAccrualData;
import org.vcpl.lms.scheduledjobs.data.LoanAccrualData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LoansAccrualSchedulerServiceImpl implements LoansAccrualSchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(LoansAccrualSchedulerServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final LoanAccrualRepository loanAccrualRepository;
    private final LoanRepository loanRepository;
    @Override
    public List<LoanAccrualData> retrieveActiveLoans() {

        String query = """
                SELECT loan.id AS loanId,
                    loan.accrued_till AS accruedTill,
                    ls.installment AS installment,
                    ls.fromdate AS fromDate,
                    ls.duedate AS dueDate,
                    coalesce(ls.interest_amount,0) AS clientInterest,
                    coalesce(ls.self_interest_amount,0) AS selfInterest,
                    coalesce(ls.partner_interest_amount,0) AS partnerInterest,
                    (SELECT coalesce(sum(accrual_interest_derived),0) FROM m_loan_repayment_schedule WHERE loan_id = loan.id) as cumulativeClientInterestAccrued,
                    (SELECT coalesce(sum(self_accrual_interest_derived),0) FROM m_loan_repayment_schedule WHERE loan_id = loan.id) as cumulativeSelfInterestAccrued,
                    (SELECT coalesce(sum(partner_accrual_interest_derived),0) FROM m_loan_repayment_schedule WHERE loan_id = loan.id) as cumulativePartnerInterestAccrued,
                    coalesce(loan.interest_repaid_derived,0) as clientInterestRepaid,
                    coalesce(loan.self_interest_repaid_derived,0) as selfInterestRepaid,
                    coalesce(loan.partner_interest_repaid_derived,0) as partnerInterestRepaid,
                    coalesce(ls.accrual_interest_derived,0) AS clientInterestAccrued,
                    coalesce(ls.self_accrual_interest_derived,0) AS selfInterestAccrued,
                    coalesce(ls.partner_accrual_interest_derived,0) AS partnerInterestAccrued,
                    coalesce((SELECT sum(coalesce(interest_amount,0) - coalesce(interest_completed_derived,0)) FROM m_loan_repayment_schedule
                        WHERE loan_id = loan.id AND duedate <= ls.fromdate),0) as clientInterestAccruedButNotReceived,
                    coalesce((SELECT sum(coalesce(self_interest_amount,0) - coalesce(self_interest_completed_derived,0)) FROM m_loan_repayment_schedule
                        WHERE loan_id = loan.id AND duedate <= ls.fromdate),0) as selfInterestAccruedButNotReceived,
                    coalesce((SELECT sum(coalesce(partner_interest_amount,0) - coalesce(partner_interest_completed_derived,0)) FROM m_loan_repayment_schedule
                        WHERE loan_id = loan.id AND duedate <= ls.fromdate),0) as partnerInterestAccruedButNotReceived
                FROM m_loan_repayment_schedule ls  LEFT JOIN m_loan loan on loan.id=ls.loan_id
                WHERE (ls.interest_amount <> COALESCE(ls.accrual_interest_derived, 0)
                    AND loan.loan_status_id= 300 AND (loan.closedon_date <= current_date() OR loan.closedon_date IS NULL) 
                    AND loan.is_npa = false
                    AND (ls.duedate <= current_date() OR (ls.duedate > current_date() and ls.fromdate < current_date()))) 
                ORDER BY loan.id,ls.duedate
                """;

        return jdbcTemplate.query(query, accrualLoanDataExtractor);
    }

    ResultSetExtractor<List<LoanAccrualData>> accrualLoanDataExtractor = (rs -> {
        Map<Long, LoanAccrualData> loanInstallmentAccruals = new HashMap<>();
        while (rs.next()) {
            Long loanId = rs.getLong("loanId");
            // Grouping installments by loan while reading from db
            if(!loanInstallmentAccruals.containsKey(loanId)){
                LoanAccrualData accrualLoanData = new LoanAccrualData();
                accrualLoanData.setLoanId(loanId);
                accrualLoanData.setCumulativeClientInterestAccrued(rs.getBigDecimal("cumulativeClientInterestAccrued"));
                accrualLoanData.setCumulativeSelfInterestAccrued(rs.getBigDecimal("cumulativeSelfInterestAccrued"));
                accrualLoanData.setCumulativePartnerInterestAccrued(rs.getBigDecimal("cumulativePartnerInterestAccrued"));
                accrualLoanData.setInstallments(new ArrayList<>());
                loanInstallmentAccruals.put(loanId,accrualLoanData);
            }

            InstallmentAccrualData installmentAccrualData = new InstallmentAccrualData();
            if(Objects.nonNull(rs.getDate("accruedTill"))) {
                installmentAccrualData.setAccruedTill(DateUtils.convertDateToLocalDate(rs.getDate("accruedTill")));
            }

            installmentAccrualData.setInstallment(rs.getInt("installment"));
            installmentAccrualData.setDueDate(DateUtils.convertDateToLocalDate(rs.getDate("dueDate")));
            installmentAccrualData.setFromDate(DateUtils.convertDateToLocalDate(rs.getDate("fromDate")));

            installmentAccrualData.setClientInterest(rs.getBigDecimal("clientInterest"));
            installmentAccrualData.setSelfInterest(rs.getBigDecimal("selfInterest"));
            installmentAccrualData.setPartnerInterest(rs.getBigDecimal("partnerInterest"));

            installmentAccrualData.setClientInterestDueReceived(rs.getBigDecimal("clientInterestRepaid"));
            installmentAccrualData.setSelfInterestDueReceived(rs.getBigDecimal("selfInterestRepaid"));
            installmentAccrualData.setPartnerInterestDueReceived(rs.getBigDecimal("partnerInterestRepaid"));

            installmentAccrualData.setClientInterestAccrued(rs.getBigDecimal("clientInterestAccrued"));
            installmentAccrualData.setSelfInterestAccrued(rs.getBigDecimal("selfInterestAccrued"));
            installmentAccrualData.setPartnerInterestAccrued(rs.getBigDecimal("partnerInterestAccrued"));

            installmentAccrualData.setClientInterestAccruedButNotReceived(rs.getBigDecimal("clientInterestAccruedButNotReceived"));
            installmentAccrualData.setSelfInterestAccruedButNotReceived(rs.getBigDecimal("selfInterestAccruedButNotReceived"));
            installmentAccrualData.setPartnerInterestAccruedButNotReceived(rs.getBigDecimal("partnerInterestAccruedButNotReceived"));

            loanInstallmentAccruals.get(loanId).getInstallments().add(installmentAccrualData);
        }
        return loanInstallmentAccruals.values().stream().toList();
    });

    @Override
    @CronTarget(jobName = JobName.ADD_PERIODIC_ACCRUAL_ENTRIES)
    public void process() {
        List<LoanAccrualData> accrualLoans = retrieveActiveLoans();

        LOG.info("Total Loans for accrual {}", accrualLoans.size());

        accrualLoans.forEach(loanAccrualData -> {
            LocalDateTime accrualProcessingStartDate = LocalDateTime.now();
            Loan loan = loanRepository.getReferenceById(loanAccrualData.getLoanId());

            final BigDecimal[] cumulativeClientInterestAccrued = {loanAccrualData.getCumulativeClientInterestAccrued()};
            final BigDecimal[] cumulativeSelfInterestAccrued = {loanAccrualData.getCumulativeSelfInterestAccrued()};
            final BigDecimal[] cumulativePartnerInterestAccrued = {loanAccrualData.getCumulativePartnerInterestAccrued()};

            loanAccrualData.getInstallments().forEach(accrualLoanData -> {

                accrueInstallment(loan, accrualLoanData, cumulativeClientInterestAccrued, cumulativeSelfInterestAccrued, cumulativePartnerInterestAccrued);
            });
            loan.setAccruedTill(new Date());
            loanRepository.save(loan);
            LocalDateTime accrualProcessingEndDate = LocalDateTime.now();
            LOG.info("Accrued Loan - {} -> Time Taken : {} ", loanAccrualData.getLoanId(), ChronoUnit.SECONDS.between(accrualProcessingStartDate,
                    accrualProcessingEndDate));
        });

    }

    private void accrueInstallment(Loan loan, InstallmentAccrualData accrualLoanData, BigDecimal[] cumulativeClientInterestAccrued,
                                   BigDecimal[] cumulativeSelfInterestAccrued, BigDecimal[] cumulativePartnerInterestAccrued) {
        LOG.info("Accruing Loan - {} Installment {} ", loan.getId(), accrualLoanData.getInstallment());
        LocalDate from = Objects.requireNonNullElse(accrualLoanData.getAccruedTill(), accrualLoanData.getFromDate());
        LocalDate end = accrualLoanData.getDueDate().isAfter(LocalDate.now()) ? LocalDate.now() : accrualLoanData.getDueDate();

        BigDecimal clientInterestAmount = accrualLoanData.getClientInterest().subtract(accrualLoanData.getClientInterestAccrued());
        BigDecimal selfInterestAmount = accrualLoanData.getSelfInterest().subtract(accrualLoanData.getSelfInterestAccrued());
        BigDecimal partnerInterestAmount = accrualLoanData.getPartnerInterest().subtract(accrualLoanData.getPartnerInterestAccrued());

        BigDecimal clientInterestAccruedButNotDue = accrualLoanData.getClientInterestAccrued();
        BigDecimal selfInterestAccruedButNotDue = accrualLoanData.getSelfInterestAccrued();
        BigDecimal partnerInterestAccruedButNotDue = accrualLoanData.getPartnerInterestAccrued();
        while (from.isBefore(end)) {
            BigDecimal betweenDays = BigDecimal.valueOf(ChronoUnit.DAYS.between(from, accrualLoanData.getDueDate()));

            BigDecimal clientShare = clientInterestAmount.divide(betweenDays, 2, RoundingMode.HALF_UP);
            BigDecimal selfShare = selfInterestAmount.divide(betweenDays, 2, RoundingMode.HALF_UP);
            BigDecimal partnerShare = partnerInterestAmount.divide(betweenDays, 2, RoundingMode.HALF_UP);

            cumulativeClientInterestAccrued[0] = cumulativeClientInterestAccrued[0].add(clientShare);
            cumulativeSelfInterestAccrued[0] = cumulativeSelfInterestAccrued[0].add(selfShare);
            cumulativePartnerInterestAccrued[0] = cumulativePartnerInterestAccrued[0].add(partnerShare);

            clientInterestAccruedButNotDue = clientInterestAccruedButNotDue.add(clientShare);
            selfInterestAccruedButNotDue = selfInterestAccruedButNotDue.add(selfShare);
            partnerInterestAccruedButNotDue = partnerInterestAccruedButNotDue.add(partnerShare);

            // Auto Corrects daily accrual
            clientInterestAmount = clientInterestAmount.subtract(clientShare).setScale(2, RoundingMode.HALF_EVEN);
            selfInterestAmount = selfInterestAmount.subtract(selfShare).setScale(2, RoundingMode.HALF_EVEN);
            partnerInterestAmount = partnerInterestAmount.subtract(partnerShare).setScale(2, RoundingMode.HALF_EVEN);

            LoanAccrual loanAccrual = new LoanAccrual(loan.getId(), accrualLoanData.getInstallment(), LoanAccrualType.DAILY.getValue(),
                    DateUtils.convertLocalDateToDate(from), DateUtils.convertLocalDateToDate(from),
                    clientShare, cumulativeClientInterestAccrued[0], accrualLoanData.getClientInterestDueReceived(),
                    clientInterestAccruedButNotDue, accrualLoanData.getClientInterestAccruedButNotReceived().add(clientInterestAccruedButNotDue),
                    selfShare, cumulativeSelfInterestAccrued[0], accrualLoanData.getSelfInterestDueReceived(),
                    selfInterestAccruedButNotDue, accrualLoanData.getSelfInterestAccruedButNotReceived().add(selfInterestAccruedButNotDue),
                    partnerShare, cumulativePartnerInterestAccrued[0], accrualLoanData.getPartnerInterestDueReceived(),
                    partnerInterestAccruedButNotDue, accrualLoanData.getPartnerInterestAccruedButNotReceived().add(partnerInterestAccruedButNotDue),
                    Boolean.FALSE, new Date(System.currentTimeMillis()));

            loanAccrualRepository.save(loanAccrual);
            from = from.plusDays(1);
        }
        // Making it Effectively Final
        BigDecimal finalClientInterestAccruedButNotDue = clientInterestAccruedButNotDue;
        BigDecimal finalSelfInterestAccruedButNotDue = selfInterestAccruedButNotDue;
        BigDecimal finalPartnerInterestAccruedButNotDue = partnerInterestAccruedButNotDue;

        loan.getRepaymentScheduleInstallments().stream().filter(installment -> installment.getInstallmentNumber().equals(accrualLoanData.getInstallment()))
                .findAny().ifPresent(installment -> {
                    installment.setInterestAccrued(finalClientInterestAccruedButNotDue);
                    installment.setSelfInterestAccrued(finalSelfInterestAccruedButNotDue);
                    installment.setPartnerInterestAccrued(finalPartnerInterestAccruedButNotDue);
                });
    }
}
