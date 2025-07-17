package org.vcpl.lms.portfolio.loanaccount.domain.loanHistory;


import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@Component
public class LoanListener {

    @PreUpdate
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateModifiedDate(final Loan loan){
        loan.setModifiedDate(new Date(System.currentTimeMillis()));
    }

    @PostPersist
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void loanCreated(final Loan loan) {
        if(Objects.isNull(loan.getEvent())){
            LoanHistory loanHistory = new LoanHistory();
            BeanUtils.copyProperties(loan, loanHistory);
            loanHistory = mapLoanToLoanHistory(loan, loanHistory);
            perform(loanHistory);}}

    @PostUpdate
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void loanUpdated(final Loan loan) {
        if(Objects.isNull(loan.getEvent())){
        LoanHistory loanHistory = new LoanHistory();
        BeanUtils.copyProperties(loan,loanHistory);
        loanHistory= mapLoanToLoanHistory(loan, loanHistory);
        perform(loanHistory);
        }
    }

    public LoanHistory mapLoanToLoanHistory(Loan loan, LoanHistory loanHistory) {
        loanHistory.setRev(loan.getVersion());
        loanHistory.setLoanId(loan.getId());
        loanHistory.setPartnerId(loan.getPartnerId());
        loanHistory.setClient(loan.getClientId());
        loanHistory.setLoanProduct(loan.getLoanProduct().getId());
        loanHistory.setInterestChargedFromDate(Date.from(loan.getInterestChargedFromDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        loanHistory.updateLoanHistorySummary(loan);
        return loanHistory;
    }

    private void perform(LoanHistory loanHistory) {
        Object entityManager = BeanUtil.getBean("entityManagerFactory");
        if(entityManager instanceof EntityManagerFactory)
        {
            EntityManagerFactory nativeEntityManagerFactory = (EntityManagerFactory) entityManager;
            EntityManager entityManagerObj = nativeEntityManagerFactory.createEntityManager();
            entityManagerObj.getTransaction().begin();
            entityManagerObj.persist(loanHistory);
            entityManagerObj.getTransaction().commit();
            entityManagerObj.close();
        }
    }
}
