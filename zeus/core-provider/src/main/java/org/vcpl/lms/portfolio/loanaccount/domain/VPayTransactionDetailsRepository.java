package org.vcpl.lms.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VPayTransactionDetailsRepository extends JpaRepository<VPayTransactionDetails,Long>, JpaSpecificationExecutor<MonthEndAccrual> {

    public List<VPayTransactionDetails> getByLoanIdAndEventType(Long loanId, String eventType);

    public Optional<List<VPayTransactionDetails>> getByLoanId(Long loanId);

    @Query(value="SELECT vtd from VPayTransactionDetails vtd WHERE vtd.loanId = :loanId and vtd.eventType = :eventType " +
            " and (vtd.reason is null or vtd.reason not like '%reverse%') ORDER BY vtd.id")
    public List<VPayTransactionDetails> getLatestTransaction(@Param("loanId") Long loanId, @Param("eventType") String eventType);

    @Query(value = "select * from m_vpay_transaction_details where vpay_reference_id is not null order by transaction_datetime desc limit 1" , nativeQuery = true)
    Optional<VPayTransactionDetails> findFirstByOrderByTransactionDate();
}
