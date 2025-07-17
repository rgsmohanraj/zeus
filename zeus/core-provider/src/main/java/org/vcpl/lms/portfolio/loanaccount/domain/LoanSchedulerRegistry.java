package org.vcpl.lms.portfolio.loanaccount.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.ToString;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;

import java.util.Date;

@Entity
@Table(name = "m_loan_scheduler_registry")
@ToString
public class LoanSchedulerRegistry extends AbstractPersistableCustom {
    @Column(name = "loan_id",nullable = false)
    private Long loanId;
    @Column(name = "installment",nullable = false)
    private Integer installment;
    @Column(name = "penal_last_run_on",nullable = false)
    private Date penalLastRunOn;
    @Column(name = "bounce_last_run_on",nullable = false)
    private Date bounceLastRunOn;

    public LoanSchedulerRegistry() {}

    public LoanSchedulerRegistry(Long loanId, Integer installment) {
        this.loanId = loanId;
        this.installment = installment;
    }

    public LoanSchedulerRegistry(Long loanId, Integer installment, Date penalLastRunOn) {
        this.loanId = loanId;
        this.installment = installment;
        this.penalLastRunOn = penalLastRunOn;
    }



    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Integer getInstallment() {
        return installment;
    }

    public void setInstallment(Integer installment) {
        this.installment = installment;
    }

    public Date getPenalLastRunOn() {
        return penalLastRunOn;
    }

    public void setPenalLastRunOn(Date penalLastRunOn) {
        this.penalLastRunOn = penalLastRunOn;
    }

    public Date getBounceLastRunOn() {
        return bounceLastRunOn;
    }

    public void setBounceLastRunOn(Date bounceLastRunOn) {
        this.bounceLastRunOn = bounceLastRunOn;
    }
}
