package org.vcpl.lms.portfolio.loanaccount.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.useradministration.domain.AppUser;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_vpay_transaction_details")
public class VPayTransactionDetails extends AbstractPersistableCustom {
    @Column(name = "loan_id", nullable = false)
    private Long loanId;
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    @Column(name = "payment_type", nullable = false)
    private String transactionType;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @Column(name = "vpay_reference_id", nullable = false)
    private String vpayReferenceId;
    @Column(name = "utr", nullable = true)
    private String utr;
    @Column(name = "action", nullable = true)
    private String action;
    @Column(name = "reason", nullable = true)
    private String reason;
    @Column(name = "transaction_amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "transaction_datetime", nullable = false)
    private Date transactionDate;
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true)
    private AppUser appUser;
}
