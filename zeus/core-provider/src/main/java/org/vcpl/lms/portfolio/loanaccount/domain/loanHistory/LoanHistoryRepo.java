package org.vcpl.lms.portfolio.loanaccount.domain.loanHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LoanHistoryRepo extends JpaRepository<LoanHistory,Integer> , JpaSpecificationExecutor<LoanHistory> {
    List<LoanHistory> findByLoanId(Long loanId);

    @Query(value = " select * from m_loan_history where loan_id = ? order by rev desc limit 1 ",nativeQuery = true)
    LoanHistory getById(Long loanId);

}