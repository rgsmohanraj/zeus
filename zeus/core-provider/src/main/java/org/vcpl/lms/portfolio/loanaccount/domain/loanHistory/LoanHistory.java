package org.vcpl.lms.portfolio.loanaccount.domain.loanHistory;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanproduct.domain.AmortizationMethod;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Component
@Table(name = "m_loan_history")
public class LoanHistory {


    @Id
    @Column(name = "id",nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "rev",nullable = false)
    private Integer rev;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "account_no", length = 25, nullable = false)
    private String accountNumber;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "client_id", nullable = true)
    private Long client;

    @Column(name = "product_id", nullable = false)
    private Long loanProduct;

    @Column(name = "partner_id", scale = 6, precision = 19)
    private Long partnerId;

    @Column(name = "maturedon_date")
    private Date actualMaturityDate;

    @Column(name = "disbursedon_date")
    private Date actualDisbursementDate;

    @Column(name = "loan_status_id", nullable = false)
    private Integer loanStatus;

    @Temporal(TemporalType.DATE)
    @Column(name = "interest_calculated_from_date")
    private Date interestChargedFromDate;

    @Column(name = "total_overpaid_derived", scale = 6, precision = 19)
    private BigDecimal totalOverpaid;

    @Embedded
    private LoanHistorySummary summary;

    @Transient
    private LoanSummaryWrapper loanSummaryWrapper;

    @Column(name = "principal_amount_proposed", scale = 6, precision = 19, nullable = false)
    private BigDecimal proposedPrincipal;

    @Column(name = "approved_principal", scale = 6, precision = 19, nullable = false)
    private BigDecimal approvedPrincipal;

    @Column(name = "net_disbursal_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal netDisbursalAmount;

    @Column(name = "max_outstanding_loan_balance", scale = 6, precision = 19, nullable = true)
    private BigDecimal maxOutstandingLoanBalance;

    @Column(name = "total_recovered_derived", scale = 6, precision = 19)
    private BigDecimal totalRecovered;

    @Column(name = "self_principal_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal selfPrincipaAmount;

    @Column(name = "partner_principal_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal partnerPrincipalAmount;

    @Column(name = "self_net_disbursal_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal netSelfDisbursalAmount;

    @Column(name = "partner_net_disbursal_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal netPartnerDisbursalAmount;

    @Column(name = "createdon_date",nullable = false)
    private  Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modifiedon_date",nullable = false)
    private Date modifiedDate;

    @Column(name = "value_date",nullable = true)
    private Date valueDate;

    @Column(name = "dpd",nullable = true)
    private Integer daysPastDue;

    @Temporal(TemporalType.DATE)
    @Column(name = "closedon_date")
    private Date closedOnDate;

    @Column(name = "vcl_hurdle_rate", scale = 6, precision = 19, nullable = true)
    private BigDecimal vclHurdleRate;

    public void updateLoanHistorySummary(Loan loan) {
        this.summary =LoanHistorySummary.updateLoanHistorySummaryDetails(loan);
    }
}
