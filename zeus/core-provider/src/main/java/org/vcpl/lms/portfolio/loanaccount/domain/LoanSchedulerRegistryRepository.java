package org.vcpl.lms.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanSchedulerRegistryRepository extends JpaRepository<LoanSchedulerRegistry,Long> {
    Optional<LoanSchedulerRegistry> getByLoanIdAndInstallment(Long loanId, Integer installment);
}