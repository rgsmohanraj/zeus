package org.vcpl.lms.portfolio.loanaccount.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;

import jakarta.persistence.*;
import org.vcpl.lms.infrastructure.core.service.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_loan_accrual")
public class LoanAccrual extends AbstractPersistableCustom {

    @Column(name = "loan_id")
    private Long loanId;

    @Column(name = "installment")
    private Integer installment;

    @Column(name = "accrual_type_enum", nullable = false)
    private Integer accrualType;

    @Temporal(TemporalType.DATE)
    @Column(name = "from_date")
    private Date fromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "to_date")
    private Date toDate;

    @Column(name = "accrued_amount")
    private BigDecimal accruedAmount;

    @Column(name = "cumulative_accrued_amount")
    private BigDecimal cumulativeAccruedAmount;

    @Column(name = "interest_due_received")
    private BigDecimal interestDueReceived;

    @Column(name =  "interest_accrued_but_not_due")
    private BigDecimal interestAccruedButNotDue;

    @Column(name =  "interest_accrued_but_not_received")
    private BigDecimal interestAccruedButNotReceived;

    @Column(name = "self_accrued_amount")
    private BigDecimal selfAccruedAmount;

    @Column(name = "self_cumulative_accrued_amount")
    private BigDecimal selfCumulativeAccruedAmount;

    @Column(name = "self_interest_due_received")
    private BigDecimal selfInterestDueReceived;

    @Column(name =  "self_interest_accrued_but_not_due")
    private BigDecimal selfInterestAccruedButNotDue;

    @Column(name =  "self_interest_accrued_but_not_received")
    private BigDecimal selfInterestAccruedButNotReceived;

    @Column(name = "partner_accrued_amount")
    private BigDecimal partnerAccruedAmount;

    @Column(name = "partner_cumulative_accrued_amount")
    private BigDecimal partnerCumulativeAccruedAmount;

    @Column(name = "partner_interest_due_received")
    private BigDecimal partnerInterestDueReceived;

    @Column(name =  "partner_interest_accrued_but_not_due")
    private BigDecimal partnerInterestAccruedButNotDue;

    @Column(name =  "partner_interest_accrued_but_not_received")
    private BigDecimal partnerInterestAccruedButNotReceived;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Temporal(TemporalType.DATE)
    @Column(name = "posted_date")
    private Date postedDate;

    public LocalDate getFromDateAsLocalDate() {
        return DateUtils.convertDateToLocalDate(fromDate);
    }
}
