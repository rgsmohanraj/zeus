package org.vcpl.lms.portfolio.loanaccount.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_month_end_accrual")
public class MonthEndAccrual extends AbstractPersistableCustom {

    @Column(name = "loan_id")
    private Long loanId;

    @Temporal(TemporalType.DATE)
    @Column(name = "from_date")
    private Date fromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "month_end_date")
    private Date monthEndDate;

    @Column(name = "month_end_accrued_amount")
    private BigDecimal monthEndAccruedAmount;

    @Column(name = "self_month_end_accrued_amount")
    private BigDecimal selfMonthEndAccruedAmount;

    @Column(name = "partner_month_end_accrued_amount")
    private BigDecimal partnerMonthEndAccruedAmount;

    @Temporal(TemporalType.DATE)
    @Column(name = "entry_posted_date")
    private Date entryPostedDate;
}
