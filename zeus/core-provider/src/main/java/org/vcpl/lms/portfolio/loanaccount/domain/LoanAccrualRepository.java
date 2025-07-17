package org.vcpl.lms.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LoanAccrualRepository extends JpaRepository<LoanAccrual, Long>, JpaSpecificationExecutor<LoanAccrual> {

    List<LoanAccrual> getByLoanIdAndReversedFalseOrderByFromDate(Long loanId);

    List<LoanAccrual> getByLoanIdAndReversedFalseAndFromDateGreaterThanEqual(Long loanId,Date transactionDate);

    @Modifying
    @Query("UPDATE LoanAccrual accrual SET accrual.reversed = true WHERE accrual.loanId = :loanId and accrual.fromDate >= :fromDate ")
    int reverseLoanAccruals(Long loanId, Date fromDate);

    @Query("SELECT accrual FROM LoanAccrual accrual WHERE accrual.loanId = :loanId AND accrual.installment = :installment AND accrual.reversed = false " +
            " ORDER BY accrual.fromDate")
    Optional<List<LoanAccrual>> getLoanAccrualsByLoanIdAndInstallment(Long loanId, Integer installment);

    @Query("SELECT accrual FROM LoanAccrual accrual WHERE accrual.loanId = :loanId AND accrual.installment = :installment " +
            " AND accrual.fromDate = :fromDate AND accrual.reversed = false ")
    Optional<LoanAccrual> getLoanAccrualsByLoanIdAndInstallmentAndFromDate(Long loanId, Integer installment,Date fromDate);

    @Query("SELECT accrual FROM LoanAccrual accrual where accrual.loanId = :loanId and accrual.fromDate = :fromDate")
    Optional<LoanAccrual> getLoanAccrualsByLoanIdAndFromDate(Long loanId, Date fromDate);

}