package org.vcpl.lms.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LoanDpdHistoryRepository extends JpaRepository<LoanDpdHistory, Long>, JpaSpecificationExecutor<Loan> {
    @Query(value = "SELECT * FROM m_loan_dpd_history WHERE loan_id = ? AND installment = ? " +
            "order by dpdon_date desc limit 1", nativeQuery = true)
    LoanDpdHistory getLastCalculatedDateByLoanIdAndInstallment(final Long loanId, final Integer installment);

    Optional<List<LoanDpdHistory>> getByLoanIdAndInstallmentAndDpdOnDateGreaterThanEqual(Long loanId, Integer installment, Date transactionDate);
}
