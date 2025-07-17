package org.vcpl.lms.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyAccruedLoanRepository extends JpaRepository<MonthEndAccrual, Long>, JpaSpecificationExecutor<MonthEndAccrual> {

    List<MonthEndAccrual> getByLoanId(Long loanId);
}
