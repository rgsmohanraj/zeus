package org.vcpl.lms.portfolio.loanaccount.servicerfee.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "m_servicer_fee_calculation")
public class ServicerFeeAmountCalculation extends AbstractPersistableCustom {

    @OneToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

   /* @OneToOne
    @JoinColumn(name = "loan_transaction_id", nullable = false)
    private LoanTransaction loanTransaction;*/

    @OneToOne
    @JoinColumn(name = "loan_transaction_rs_mapping_id", nullable = false)
    private LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping;

    @Column(name = "vcl_interest_amt_hurdle_rate",nullable = true)
    private BigDecimal vclInterestAmtHurdleRate;

    @Column(name = "sf_interest_base_amount",nullable = true)
    private BigDecimal sfInterestBaseAmount;

    @Column(name = "sf_interest_gst_loss_amount",nullable = true)
    private BigDecimal sfInterestGstLossAmount;

    @Column(name = "sf_interest_gst_amount",nullable = true)
    private BigDecimal sfInterestGstAmount;

    @Column(name = "sf_interest_invoice_amount",nullable = true)
    private BigDecimal sfInterestInvoiceAmount;

    @Column(name = "vcl_penal_amt_hurdle_rate",nullable = true)
    private BigDecimal vclPenalAmtHurdleRate;

    @Column(name = "sf_penal_base_amount",nullable = true)
    private BigDecimal sfPenalBaseAmount;

    @Column(name = "sf_penal_gst_loss_amount",nullable = true)
    private BigDecimal sfPenalGstLossAmount;

    @Column(name = "sf_penal_gst_amount",nullable = true)
    private BigDecimal sfPenalGstAmount;

    @Column(name = "sf_penal_invoice_amount",nullable = true)
    private BigDecimal sfPenalInvoiceAmount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdon_date", nullable = true)
    private Date createdDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private Date dateOf;

    /*public ServicerFeeAmountCalculation(Loan loan, LoanTransaction loanTransaction, BigDecimal vcplInterestAmtHurdleRate, BigDecimal sfInterestBaseAmount, BigDecimal sfInterestGstLossAmount, BigDecimal sfInterestGstAmount, BigDecimal sfInterestInvoiceAmount, BigDecimal vcplPenalAmtHurdleRate, BigDecimal sfPenalBaseAmount, BigDecimal sfPenalGstLossAmount, BigDecimal sfPenalGstAmount, BigDecimal sfPenalInvoiceAmount) {
        this.loan = loan;
        this.loanTransaction = loanTransaction;
        this.vcplInterestAmtHurdleRate = vcplInterestAmtHurdleRate;
        this.sfInterestBaseAmount = sfInterestBaseAmount;
        this.sfInterestGstLossAmount = sfInterestGstLossAmount;
        this.sfInterestGstAmount = sfInterestGstAmount;
        this.sfInterestInvoiceAmount = sfInterestInvoiceAmount;
        this.vcplPenalAmtHurdleRate = vcplPenalAmtHurdleRate;
        this.sfPenalBaseAmount = sfPenalBaseAmount;
        this.sfPenalGstLossAmount = sfPenalGstLossAmount;
        this.sfPenalGstAmount = sfPenalGstAmount;
        this.sfPenalInvoiceAmount = sfPenalInvoiceAmount;
    }*/

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    /*public void setLoanTransaction(LoanTransaction loanTransaction) {
        this.loanTransaction = loanTransaction;
    }*/

    public void setLoanTransactionToRepaymentScheduleMapping(LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping) {
        this.loanTransactionToRepaymentScheduleMapping = loanTransactionToRepaymentScheduleMapping;
    }

    public void setVclInterestAmtHurdleRate(BigDecimal vclInterestAmtHurdleRate) {
        this.vclInterestAmtHurdleRate = vclInterestAmtHurdleRate;
    }

    public void setSfInterestBaseAmount(BigDecimal sfInterestBaseAmount) {
        this.sfInterestBaseAmount = sfInterestBaseAmount;
    }

    public void setSfInterestGstLossAmount(BigDecimal sfInterestGstLossAmount) {
        this.sfInterestGstLossAmount = sfInterestGstLossAmount;
    }

    public void setSfInterestGstAmount(BigDecimal sfInterestGstAmount) {
        this.sfInterestGstAmount = sfInterestGstAmount;
    }

    public void setSfInterestInvoiceAmount(BigDecimal sfInterestInvoiceAmount) {
        this.sfInterestInvoiceAmount = sfInterestInvoiceAmount;
    }

    public void setVclPenalAmtHurdleRate(BigDecimal vclPenalAmtHurdleRate) {
        this.vclPenalAmtHurdleRate = vclPenalAmtHurdleRate;
    }

    public void setSfPenalBaseAmount(BigDecimal sfPenalBaseAmount) {
        this.sfPenalBaseAmount = sfPenalBaseAmount;
    }

    public void setSfPenalGstLossAmount(BigDecimal sfPenalGstLossAmount) {
        this.sfPenalGstLossAmount = sfPenalGstLossAmount;
    }

    public void setSfPenalGstAmount(BigDecimal sfPenalGstAmount) {
        this.sfPenalGstAmount = sfPenalGstAmount;
    }

    public void setSfPenalInvoiceAmount(BigDecimal sfPenalInvoiceAmount) {
        this.sfPenalInvoiceAmount = sfPenalInvoiceAmount;
    }
}
