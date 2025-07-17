package org.vcpl.lms.portfolio.loanaccount.domain;

import lombok.*;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanListener;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_loan_dpd_history")
public class LoanDpdHistory  extends AbstractPersistableCustom {

    @Column(name = "loan_id")
    private Long loanId;

    @Column(name = "installment")
    private Integer installment;

    @Column(name = "dpd")
    private Integer dpd;

    @Column(name = "is_reversed")
    private Boolean isReversed;

    @Temporal(TemporalType.DATE)
    @Column(name = "dpdon_date")
    private Date dpdOnDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdon_date")
    private Date createDate;
}
