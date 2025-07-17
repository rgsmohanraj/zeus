package org.vcpl.lms.portfolio.loanaccount.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LoanPenalForeclosureChargesRepository extends JpaRepository<LoanPenalForeclosureCharges,Long>, JpaSpecificationExecutor<LoanCharge> {

    List<LoanPenalForeclosureCharges> findByLoanId(Long loanId);

}
