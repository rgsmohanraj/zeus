/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.organisation.office.domain.Office;
import org.vcpl.lms.portfolio.account.data.AccountTransferData;
import org.vcpl.lms.portfolio.collection.data.InterestAppropriationData;
import org.vcpl.lms.portfolio.collection.data.PrincipalAppropriationData;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.data.LoanTransactionData;
import org.vcpl.lms.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations;
import org.vcpl.lms.portfolio.paymentdetail.data.PaymentDetailData;
import org.vcpl.lms.portfolio.paymentdetail.domain.PaymentDetail;
import org.vcpl.lms.useradministration.domain.AppUser;

/**
 * All monetary transactions against a loan are modelled through this entity. Disbursements, Repayments, Waivers,
 * Write-off etc
 */
@Entity
@Setter
@Getter
@Table(name = "m_loan_transaction", uniqueConstraints = { @UniqueConstraint(columnNames = { "external_id" }, name = "external_id_UNIQUE") })
@ToString
public class LoanTransaction extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(optional = true)
    @JoinColumn(name = "payment_detail_id", nullable = true)
    private PaymentDetail paymentDetail;

    @Column(name = "transaction_type_enum", nullable = false)
    private Integer typeOf;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private Date dateOf;

    @Temporal(TemporalType.DATE)
    @Column(name = "submitted_on_date", nullable = false)
    private Date submittedOnDate;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "self_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal selfDue;

    @Column(name = "partner_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal partnerDue;

    @Column(name = "principal_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalPortion;

    @Column(name = "self_principal_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPrincipalPortion;

    @Column(name = "partner_principal_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPrincipalPortion;

    @Column(name = "interest_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestPortion;

    @Column(name = "self_interest_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfInterestPortion;

    @Column(name = "partner_interest_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerInterestPortion;

    @Column(name = "fee_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesPortion;

    @Column(name = "penalty_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesPortion;

    @Column(name = "overpayment_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal overPaymentPortion;

    @Column(name = "unrecognized_income_portion", scale = 6, precision = 19, nullable = true)
    private BigDecimal unrecognizedIncomePortion;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Column(name = "external_id", length = 100, nullable = true, unique = true)
    private String externalId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdon_date", nullable = true)
    private Date createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdon_userid", nullable = true)
    private AppUser appUser;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanTransaction", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanChargePaidBy> loanChargesPaid = new HashSet<>();

    @Column(name = "outstanding_loan_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal outstandingLoanBalance;

    @Column(name = "self_outstanding_loan_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfOutstandingLoanBalance;

    @Column(name = "partner_outstanding_loan_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partneroutstandingLoanBalance;


    @Column(name = "manually_adjusted_or_reversed", nullable = false)
    private boolean manuallyAdjustedOrReversed;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "loanTransaction")
    private Set<LoanCollateralManagement> loanCollateralManagementSet = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "loanTransaction")
    private Set<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappings = new HashSet<>();

    @Column(name = "self_penalty_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPenaltyChargesPortion;

    @Column(name = "partner_penalty_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPenaltyChargesPortion;

    @Column(name = "event",  nullable = true)
    private String event;

    @Column(name = "self_fee_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfFeeChargesPortion;

    @Column(name = "partner_fee_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerFeeChargesPortion;

    @Column(name = "receipt_reference_number",  nullable = false)
    private String receiptReferenceNumber;

    @Column(name = "partner_transfer_utr",  nullable = true)
    private String partnerTransferUtr;

    @Temporal(TemporalType.DATE)
    @Column(name = "partner_transfer_date", nullable = true)
    private Date partnerTransferDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repayment_mode_cv_id", nullable = true)
    private CodeValue repaymentMode;

    @Column(name = "advance_amount", nullable = true)
    private BigDecimal advanceAmount;

    @Column(name = "modifiedon_date",nullable = true)
    private Date modifiedDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedon_userid", nullable = true)
    private AppUser modifiedUser;

  /*  @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanTransaction", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private ServicerFeeAmountCalculation servicerFeeAmountCalculation;*/

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanTransaction",orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CollectionReport> collectionReport = new ArrayList<>();

    @Column(name = "advance_amount_processed",nullable = true)
    private Long advanceAmountprocessed;

    @Column(name = "parent_id",nullable = true)
    private Long parentId;

    @Column(name = "value_date", nullable = false)
    private Date valueDate;

    @Column(name = "bounce_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal bounceChargesPortion;

    @Column(name = "self_bounce_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfBounceChargesPortion;

    @Column(name = "partner_bounce_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerBounceChargesPortion;


    public LoanTransaction() {
        /*
         * this.loan = null; this.dateOf = null; this.typeOf = null; this.submittedOnDate = DateUtils.getDateOfTenant();
         * this.createdDate = new Date(); this.appUser = null;
         */
    }

    public static LoanTransaction incomePosting(final Loan loan, final Office office, final Date dateOf, final BigDecimal amount,
                                                final BigDecimal interestPortion, final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion,
                                                final AppUser appUser,final BigDecimal selfInterestPortion,final BigDecimal partnerInterestPortion,
                                                final BigDecimal selfDue,final BigDecimal partnerDue,final Date partnerTransferDate,final CodeValue repaymentMode) {
        final Integer typeOf = LoanTransactionType.INCOME_POSTING.getValue();
        final BigDecimal principalPortion = BigDecimal.ZERO;
        final BigDecimal selfPrincipalPortion = BigDecimal.ZERO;
        final BigDecimal partnerPrincipalPortion = BigDecimal.ZERO;

        final BigDecimal overPaymentPortion = BigDecimal.ZERO;
        final boolean reversed = false;
        final PaymentDetail paymentDetail = null;
        final String externalId = null;
        final String receiptReferenceNumber = null;
        final String partnerTransferUtr = null;
        final LocalDateTime createdDate = DateUtils.getLocalDateTimeOfTenant();
        final BigDecimal advanceAmount = BigDecimal.ZERO;
        final Long parentId = null;
        return new LoanTransaction(loan, office, typeOf, dateOf, amount, principalPortion, interestPortion, feeChargesPortion,
                penaltyChargesPortion, overPaymentPortion, reversed, paymentDetail, externalId, createdDate,
                appUser,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfDue,partnerDue,LoanTransactionType.INCOME_POSTING.getCode(),receiptReferenceNumber,partnerTransferUtr,partnerTransferDate,repaymentMode,advanceAmount,parentId);
    }

    public static LoanTransaction disbursement(final Office office, final Money amount, final PaymentDetail paymentDetail,
                                               final LocalDate disbursementDate, final String externalId, final LocalDateTime createdDate,
                                               final AppUser appUser,final BigDecimal selfDue,final BigDecimal partnerDue) {
        return new LoanTransaction(null, office, LoanTransactionType.DISBURSEMENT, paymentDetail, amount.getAmount(), disbursementDate,
                externalId, createdDate, appUser,selfDue,partnerDue,LoanTransactionType.DISBURSEMENT.name(),null,null,null,null);
    }

    public static LoanTransaction repayment(final Office office, final Money amount, final PaymentDetail paymentDetail,
                                            final LocalDate paymentDate, final String externalId, final LocalDateTime createdDate,
                                            final AppUser appUser,final BigDecimal selfDue,final BigDecimal partnerDue,final String event,final String receiptReferenceNumber,final String partnerTransferUtr,final Date partnerTransferDate,final CodeValue repaymentMode) {
        return new LoanTransaction(null, office, LoanTransactionType.REPAYMENT, paymentDetail, amount.getAmount(), paymentDate, externalId,
                createdDate, appUser,selfDue,partnerDue,event,receiptReferenceNumber,partnerTransferUtr,partnerTransferDate,repaymentMode);
    }


    public static LoanTransaction repaymentType(final LoanTransactionType repaymentType, final Office office, final Money amount,
                                                final PaymentDetail paymentDetail, final LocalDate paymentDate, final String externalId, final LocalDateTime createdDate,
                                                final AppUser appUser,final Money selfDue,final Money partnerDue,final String event,final String receiptReferenceNumber,final String partnerTransferUtr,final Date partnerTransferDate,final CodeValue repaymentMode) {
        return new LoanTransaction(null, office, repaymentType, paymentDetail, amount.getAmount(), paymentDate, externalId, createdDate,
                appUser,selfDue.getAmount(),partnerDue.getAmount(),event,receiptReferenceNumber,partnerTransferUtr,partnerTransferDate,repaymentMode);
    }

    public void setLoanTransactionToRepaymentScheduleMappings(final Integer installmentId, final BigDecimal chargePerInstallment) {
        for (LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping : this.loanTransactionToRepaymentScheduleMappings) {
            final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loanTransactionToRepaymentScheduleMapping
                    .getLoanRepaymentScheduleInstallment();
            if (loanRepaymentScheduleInstallment.getInstallmentNumber().equals(installmentId)) {
                loanRepaymentScheduleInstallment.updateLoanRepaymentSchedule(chargePerInstallment);
                break;
            }
        }

    }

    public static LoanTransaction recoveryRepayment(final Office office, final Money amount, final PaymentDetail paymentDetail,
                                                    final LocalDate paymentDate, final String externalId, final LocalDateTime createdDate,
                                                    final AppUser appUser,final Money selfDue,final Money partnerDue) {
        final String event = null;
        return new LoanTransaction(null, office, LoanTransactionType.RECOVERY_REPAYMENT, paymentDetail, amount.getAmount(), paymentDate,
                externalId, createdDate, appUser,selfDue.getAmount(),partnerDue.getAmount(),event,null,null,null,null);
    }

    public static LoanTransaction loanPayment(final Loan loan, final Office office, final Money amount, final PaymentDetail paymentDetail,
                                              final LocalDate paymentDate, final String externalId, final LoanTransactionType transactionType,
                                              final LocalDateTime createdDate, final AppUser appUser,final BigDecimal selfDue,final BigDecimal partnerDue) {
        final String event = null;
        return new LoanTransaction(loan, office, transactionType, paymentDetail, amount.getAmount(), paymentDate, externalId, createdDate,
                appUser,selfDue,partnerDue,event,null,null,null,null);
    }

    public static LoanTransaction repaymentAtDisbursement(final Office office, final Money amount, final PaymentDetail paymentDetail,
                                                          final LocalDate paymentDate, final String externalId, final LocalDateTime createdDate,
                                                          final AppUser appUser,final BigDecimal selfDue,final BigDecimal partnerDue) {
        final String event = null;
        return new LoanTransaction(null, office, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT, paymentDetail, amount.getAmount(),
                paymentDate, externalId, createdDate, appUser,selfDue,partnerDue, event,null,null,null,null);
    }

    public static LoanTransaction waiver(final Office office, final Loan loan, final Money amount, final LocalDate waiveDate,
                                         final Money waived, final Money unrecognizedPortion, final LocalDateTime createdDate, final AppUser appUser,final Money selfDue,final Money partnerDue) {
        LoanTransaction loanTransaction = new LoanTransaction(loan, office, LoanTransactionType.WAIVE_INTEREST, amount.getAmount(),
                waiveDate, null, createdDate, appUser,selfDue.getAmount(),partnerDue.getAmount());
        loanTransaction.updateInterestComponent(waived, unrecognizedPortion);
        return loanTransaction;
    }

    public static LoanTransaction accrueInterest(final Office office, final Loan loan, final Money amount,
                                                 final LocalDate interestAppliedDate, final LocalDateTime createdDate,
                                                 final AppUser appUser,final BigDecimal selfDue,final BigDecimal partnerDue) {
        BigDecimal principalPortion = null;
        BigDecimal selfPrincipalPortion = null;
        BigDecimal partnerPrincipalPortion = null;

        BigDecimal feesPortion = null;
        BigDecimal penaltiesPortion = null;
        BigDecimal interestPortion = amount.getAmount();
        BigDecimal selfInterestPortion = selfDue;
        BigDecimal partnerInterestPortion = partnerDue;

        BigDecimal overPaymentPortion = null;
        boolean reversed = false;
        PaymentDetail paymentDetail = null;
        String externalId = null;
        BigDecimal advanceAmount = BigDecimal.ZERO;
        Long parentId = null;
        return new LoanTransaction(loan, office, LoanTransactionType.ACCRUAL.getValue(),
                Date.from(interestAppliedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), interestPortion, principalPortion,
                interestPortion, feesPortion, penaltiesPortion, overPaymentPortion, reversed, paymentDetail, externalId, createdDate,
                appUser,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfDue,partnerDue, LoanTransactionType.ACCRUAL.getCode(),null,null,null,null,advanceAmount,parentId);
    }

    public static LoanTransaction accrual(final Loan loan, final Office office, final Money amount, final Money interest,
                                          final Money feeCharges, final Money penaltyCharges, final LocalDate transactionDate,
                                          final BigDecimal selfInterestCharged,final BigDecimal partnerInterestCharged,
                                          final BigDecimal selfDue,final BigDecimal partnerDue) {
        final AppUser appUser = null;
        return accrueTransaction(loan, office, transactionDate, amount.getAmount(), interest.getAmount(), feeCharges.getAmount(),
                penaltyCharges.getAmount(), appUser,selfInterestCharged,partnerInterestCharged,selfDue,partnerDue);
    }

    public static LoanTransaction accrueTransaction(final Loan loan, final Office office, final LocalDate dateOf, final BigDecimal amount,
                                                    final BigDecimal interestPortion, final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion,
                                                    final AppUser appUser,final BigDecimal selfInterestPortion,final BigDecimal partnerInterestPortion,
                                                    final BigDecimal selfDue,final BigDecimal partnerDue) {
        BigDecimal principalPortion = null;
        BigDecimal selfPrincipalPortion = null;
        BigDecimal partnerPrincipalPortion = null;

        BigDecimal overPaymentPortion = null;
        boolean reversed = false;
        PaymentDetail paymentDetail = null;
        String externalId = null;
        LocalDateTime createdDate = DateUtils.getLocalDateTimeOfTenant();
        BigDecimal advanceAmount = BigDecimal.ZERO;
        Long parentId = null;

        return new LoanTransaction(loan, office, LoanTransactionType.ACCRUAL.getValue(),
                Date.from(dateOf.atStartOfDay(ZoneId.systemDefault()).toInstant()), amount, principalPortion, interestPortion,
                feeChargesPortion, penaltyChargesPortion, overPaymentPortion, reversed, paymentDetail, externalId, createdDate, appUser,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,
                selfDue,partnerDue,null,LoanTransactionType.ACCRUAL.getCode(),null,null,null,advanceAmount,parentId);
    }

    public static LoanTransaction initiateTransfer(final Office office, final Loan loan, final LocalDate transferDate,
                                                   final LocalDateTime createdDate, final AppUser appUser) {
        BigDecimal advanceAmount = BigDecimal.ZERO;

        return new LoanTransaction(loan, office, LoanTransactionType.INITIATE_TRANSFER.getValue(),
                Date.from(transferDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), loan.getSummary().getTotalOutstanding(),
                loan.getSummary().getTotalPrincipalOutstanding(), loan.getSummary().getTotalInterestOutstanding(),
                loan.getSummary().getTotalFeeChargesOutstanding(), loan.getSummary().getTotalPenaltyChargesOutstanding(), null, false, null,
                null, createdDate, appUser,loan.getSelfPrincipaAmount(),loan.getPartnerPrincipalAmount(),null,null,null,null,LoanTransactionType.INITIATE_TRANSFER.getCode(),null,null,null,null,advanceAmount,null);
    }

    public static LoanTransaction approveTransfer(final Office office, final Loan loan, final LocalDate transferDate,
                                                  final LocalDateTime createdDate, final AppUser appUser) {
        BigDecimal advanceAmount = BigDecimal.ZERO;

        return new LoanTransaction(loan, office, LoanTransactionType.APPROVE_TRANSFER.getValue(),
                Date.from(transferDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), loan.getSummary().getTotalOutstanding(),
                loan.getSummary().getTotalPrincipalOutstanding(), loan.getSummary().getTotalInterestOutstanding(),
                loan.getSummary().getTotalFeeChargesOutstanding(), loan.getSummary().getTotalPenaltyChargesOutstanding(), null, false, null,
                null, createdDate, appUser,loan.getSelfPrincipaAmount(),loan.getPartnerPrincipalAmount(),null,null,null,null,LoanTransactionType.APPROVE_TRANSFER.getCode(),null,null,null,null,advanceAmount,null);
    }

    public static LoanTransaction withdrawTransfer(final Office office, final Loan loan, final LocalDate transferDate,
                                                   final LocalDateTime createdDate, final AppUser appUser) {
        BigDecimal advanceAmount = BigDecimal.ZERO;

        return new LoanTransaction(loan, office, LoanTransactionType.WITHDRAW_TRANSFER.getValue(),
                Date.from(transferDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), loan.getSummary().getTotalOutstanding(),
                loan.getSummary().getTotalPrincipalOutstanding(), loan.getSummary().getTotalInterestOutstanding(),
                loan.getSummary().getTotalFeeChargesOutstanding(), loan.getSummary().getTotalPenaltyChargesOutstanding(), null, false, null,
                null, createdDate, appUser,loan.getSelfPrincipaAmount(),loan.getPartnerPrincipalAmount(),null,null,null,null, LoanTransactionType.WITHDRAW_TRANSFER.getCode(),null,null,null,null,advanceAmount,null);
    }

    public static LoanTransaction refund(final Office office, final Money amount, final PaymentDetail paymentDetail,
                                         final LocalDate paymentDate, final String externalId, final LocalDateTime createdDate,
                                         final AppUser appUser,final Money selfDue,final Money partnerDue) {
        final String event = null;
        return new LoanTransaction(null, office, LoanTransactionType.REFUND, paymentDetail, amount.getAmount(), paymentDate, externalId,
                createdDate, appUser,selfDue.getAmount(),partnerDue.getAmount(),event,null,null,null,null);
    }

    public static LoanTransaction copyTransactionProperties(final LoanTransaction loanTransaction) {

        return new LoanTransaction(loanTransaction.loan, loanTransaction.office, loanTransaction.typeOf, loanTransaction.dateOf,
                loanTransaction.amount, loanTransaction.principalPortion, loanTransaction.interestPortion,
                loanTransaction.feeChargesPortion, loanTransaction.penaltyChargesPortion, loanTransaction.overPaymentPortion,
                loanTransaction.reversed, loanTransaction.paymentDetail, loanTransaction.externalId,
                (loanTransaction.createdDate == null) ? LocalDateTime.now(DateUtils.getDateTimeZoneOfTenant())
                        : LocalDateTime.ofInstant(loanTransaction.createdDate.toInstant(), DateUtils.getDateTimeZoneOfTenant()),
                loanTransaction.appUser,loanTransaction.selfPrincipalPortion,loanTransaction.partnerPrincipalPortion,loanTransaction.selfInterestPortion,loanTransaction.partnerInterestPortion,
                loanTransaction.selfDue,loanTransaction.partnerDue,loanTransaction.event,loanTransaction.receiptReferenceNumber,loanTransaction.partnerTransferUtr,loanTransaction.partnerTransferDate,
                loanTransaction.repaymentMode,loanTransaction.advanceAmount,Objects.nonNull(loanTransaction.getId())? loanTransaction.getId() : null);
    }

    public static LoanTransaction accrueLoanCharge(final Loan loan, final Office office, final Money amount, final LocalDate applyDate,
                                                   final Money feeCharges, final Money penaltyCharges, final LocalDateTime createdDate,

                                                   final AppUser appUser,final BigDecimal selfFeeChargesPortion, final BigDecimal partnerFeeChargesPortion) {
        /**
         *for Accrual we seeting charge amount as a due Amount
         */
        String externalId = null;
        final LoanTransaction applyCharge = new LoanTransaction(loan, office, LoanTransactionType.ACCRUAL, amount.getAmount(), applyDate,
                externalId, createdDate, appUser,selfFeeChargesPortion,partnerFeeChargesPortion);
        applyCharge.updateChargesComponents(feeCharges, penaltyCharges,BigDecimal.ZERO,BigDecimal.ZERO,selfFeeChargesPortion,partnerFeeChargesPortion);
        return applyCharge;
    }

    public static LoanTransaction creditBalanceRefund(final Loan loan, final Office office, final Money amount, final LocalDate paymentDate,
                                                      final String externalId, final LocalDateTime createdDate, final AppUser appUser,final Money selfDue,final Money partnerDue) {
        final PaymentDetail paymentDetail = null;
        final String event = null;
        return new LoanTransaction(loan, office, LoanTransactionType.CREDIT_BALANCE_REFUND, paymentDetail, amount.getAmount(), paymentDate,
                externalId, createdDate, appUser,selfDue.getAmount(),partnerDue.getAmount(),event,null,null,null,null);
    }

    public static LoanTransaction refundForActiveLoan(final Office office, final Money amount, final PaymentDetail paymentDetail,
                                                      final LocalDate paymentDate, final String externalId, final LocalDateTime createdDate,
                                                      final AppUser appUser,final Money selfDue,final Money partnerDue) {
        final String event = null;
        return new LoanTransaction(null, office, LoanTransactionType.REFUND_FOR_ACTIVE_LOAN, paymentDetail, amount.getAmount(), paymentDate,
                externalId, createdDate, appUser,selfDue.getAmount(),partnerDue.getAmount(),event,null,null,null,null);
    }

    public static boolean transactionAmountsMatch(final MonetaryCurrency currency, final LoanTransaction loanTransaction,
                                                  final LoanTransaction newLoanTransaction) {
        if (loanTransaction.getAmount(currency).isEqualTo(newLoanTransaction.getAmount(currency))
                && loanTransaction.getPrincipalPortion(currency).isEqualTo(newLoanTransaction.getPrincipalPortion(currency))

                && loanTransaction.getInterestPortion(currency).isEqualTo(newLoanTransaction.getInterestPortion(currency))
                && loanTransaction.getFeeChargesPortion(currency).isEqualTo(newLoanTransaction.getFeeChargesPortion(currency))
                && loanTransaction.getPenaltyChargesPortion(currency).isEqualTo(newLoanTransaction.getPenaltyChargesPortion(currency))
                && loanTransaction.getOverPaymentPortion(currency).isEqualTo(newLoanTransaction.getOverPaymentPortion(currency))) {
            return true;
        }
        return false;
    }

    public LoanTransaction(final Loan loan, final Office office, final Integer typeOf, final Date dateOf, final BigDecimal amount,
                            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
                            final BigDecimal penaltyChargesPortion, final BigDecimal overPaymentPortion, final boolean reversed,
                            final PaymentDetail paymentDetail, final String externalId, final LocalDateTime createdDate, final AppUser appUser,final BigDecimal selfPrincipalPortion,final BigDecimal partnerPrincipalPortion,
                            final BigDecimal selfInterestPortion,final BigDecimal partnerInterestPortion,final BigDecimal selfDue,final BigDecimal partnerDue,final String event,
                            final String receiptReferenceNumber,final String partnerTransferUtr,final Date partnerTransferDate,final CodeValue repaymentMode,final BigDecimal advanceAmount ,final Long parentId) {

        this.loan = loan;
        this.typeOf = typeOf;
        this.dateOf = dateOf;
        this.amount = amount;
        this.selfDue = selfDue;
        this.partnerDue = partnerDue;

        this.principalPortion = principalPortion;
        this.selfPrincipalPortion = selfPrincipalPortion;
        this.partnerPrincipalPortion = partnerPrincipalPortion;

        this.interestPortion = interestPortion;
        this.selfInterestPortion = selfInterestPortion;
        this.partnerInterestPortion = partnerInterestPortion;

        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.overPaymentPortion = overPaymentPortion;
        this.reversed = reversed;
        this.paymentDetail = paymentDetail;
        this.office = office;
        this.externalId = externalId;
        this.submittedOnDate = DateUtils.getDateOfTenant();
        this.createdDate = Date.from(createdDate.atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant());
        this.appUser = appUser;
        this.event = event;
        this.receiptReferenceNumber = receiptReferenceNumber;
        this.partnerTransferUtr = partnerTransferUtr;
        this.partnerTransferDate = partnerTransferDate;
        this.repaymentMode = repaymentMode;
        this.advanceAmount = advanceAmount;
        this.parentId = parentId;

    }

    public static LoanTransaction waiveLoanCharge(final Loan loan, final Office office, final Money waived, final LocalDate waiveDate,
                                                  final Money feeChargesWaived, final Money penaltyChargesWaived, final Money unrecognizedCharge, final LocalDateTime createdDate,
                                                  final AppUser appUser) {
        final LoanTransaction waiver = new LoanTransaction(loan, office, LoanTransactionType.WAIVE_CHARGES, waived.getAmount(), waiveDate,
                null, createdDate, appUser,null,null);
        waiver.updateChargesComponents(feeChargesWaived, penaltyChargesWaived, unrecognizedCharge);

        return waiver;
    }

    public static LoanTransaction writeoff(final Loan loan, final Office office, final LocalDate writeOffDate, final String externalId,
                                           final LocalDateTime createdDate, final AppUser appUser) {
        return new LoanTransaction(loan, office, LoanTransactionType.WRITEOFF, null, writeOffDate, externalId, createdDate, appUser,null,null);
    }

    private LoanTransaction(final Loan loan, final Office office, final Date dateOf, final Integer typeOf,
                            final Date submittedOnDate, final Date createdDate,
                            final AppUser appUser) {
        this.loan = loan;
        this.office = office;
        this.dateOf = dateOf;
        this.reversed = Boolean.FALSE;
        this.typeOf = typeOf;
        this.amount = BigDecimal.ZERO;
        this.principalPortion = BigDecimal.ZERO;
        this.interestPortion = BigDecimal.ZERO;
        this.submittedOnDate = submittedOnDate;
        this.manuallyAdjustedOrReversed = Boolean.FALSE;
        this.createdDate = createdDate;
        this.appUser = appUser;
    }
    private LoanTransaction(final Loan loan, final Office office, final LoanTransactionType type, final BigDecimal amount,
                            final LocalDate date, final String externalId, final LocalDateTime createdDate,
                            final AppUser appUser,final BigDecimal selfDue,final BigDecimal partnerDue) {
        this.loan = loan;
        this.typeOf = type.getValue();
        this.amount = amount;
        this.selfDue = selfDue;
        this.partnerDue = partnerDue;

        this.dateOf = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.externalId = externalId;
        this.office = office;
        this.submittedOnDate = DateUtils.getDateOfTenant();
        this.createdDate = Date.from(createdDate.atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant());
        this.appUser = appUser;
    }

    public LoanTransaction(final Loan loan, final Office office, final LoanTransactionType type, final PaymentDetail paymentDetail,//call at the time of transaction creating
                            final BigDecimal amount, final LocalDate date, final String externalId, final LocalDateTime createdDate,
                            final AppUser appUser,final BigDecimal selfDue,final BigDecimal partnerDue,final String event,final String receiptReferenceNumber,final String partnerTransferUtr,final Date partnerTransferDate,final CodeValue repaymentMode) {
        this.loan = loan;
        this.typeOf = type.getValue();
        this.paymentDetail = paymentDetail;
        this.amount = amount;
        this.selfDue = selfDue;
        this.partnerDue = partnerDue;

        this.dateOf = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.externalId = externalId;
        this.office = office;
        this.submittedOnDate = DateUtils.getDateOfTenant();
        this.createdDate = Date.from(createdDate.atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant());
        this.appUser = appUser;
        this.event = event;
        this.receiptReferenceNumber = receiptReferenceNumber;
        this.partnerTransferUtr = partnerTransferUtr;
        this.partnerTransferDate = partnerTransferDate;
        this.repaymentMode = repaymentMode;
    }

    public void reverse() {
        this.reversed = true;
        this.loanTransactionToRepaymentScheduleMappings.clear();
    }

    public void resetDerivedComponents() {
        this.principalPortion = null;
        this.selfPrincipalPortion = null;
        this.partnerPrincipalPortion = null;

        this.interestPortion = null;
        this.selfInterestPortion = null;
        this.partnerInterestPortion = null;

        this.feeChargesPortion = null;
        this.penaltyChargesPortion = null;
        this.overPaymentPortion = null;
        this.outstandingLoanBalance = null;
        this.selfOutstandingLoanBalance=null;
        this.partneroutstandingLoanBalance=null;
    }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    /**
     * This updates the derived fields of a loan transaction for the principal, interest and interest waived portions.
     *
     * This accumulates the values passed to the already existent values for each of the portions.
     *
     * @param principal
     *            principal
     * @param interest
     *            interest
     * @param feeCharges
     *            feeCharges
     * @param penaltyCharges
     *            penaltyCharges
     */
    public void updateComponents(final Money principal, final Money interest, final Money feeCharges, final Money penaltyCharges, final Money selfPrincipal,
                                 final Money partnerPrincipal, final Money selfInterestCharged, final Money partnerInterestCharged,
                                 final BigDecimal selfSharePenaltyAmount, final BigDecimal partnerSharePenaltyAmount,
                                 final BigDecimal selfFeeChargesPortion,final BigDecimal partnerFeeChargesPortion) {
        final MonetaryCurrency currency = principal.getCurrency();

        this.principalPortion = defaultToNullIfZero(getPrincipalPortion(currency).plus(principal).getAmount());
        this.selfPrincipalPortion=defaultToNullIfZero(getSelfPrincipalPortion(currency).plus(selfPrincipal).getAmount());
        this.partnerPrincipalPortion=defaultToNullIfZero(getPartnerPrincipalPortion(currency).plus(partnerPrincipal).getAmount());

        this.interestPortion = defaultToNullIfZero(getInterestPortion(currency).plus(interest).getAmount());
        this.selfInterestPortion = defaultToNullIfZero(getSelfInterestPortion(currency).plus(selfInterestCharged).getAmount());
        this.partnerInterestPortion = defaultToNullIfZero(getPartnerInterestPortion(currency).plus(partnerInterestCharged).getAmount());
        this.selfDue = defaultToNullIfZero(getSelfPrincipalPortion(currency).plus(getSelfInterestPortion(currency)).getAmount()
                .add(getSelfPenaltyChargesPortion(currency).getAmount()).add(selfFeeChargesPortion));
        this.partnerDue = defaultToNullIfZero(getPartnerPrincipalPortion(currency).plus(getPartnerInterestPortion(currency)).getAmount()
                .add(getPartnerPenaltyChargesPortion(currency).getAmount()).add(partnerFeeChargesPortion));
        updateChargesComponents(feeCharges, penaltyCharges,selfSharePenaltyAmount,partnerSharePenaltyAmount,selfFeeChargesPortion,partnerFeeChargesPortion);
    }


    public static LoanTransaction xirrCalculationForScheduler(final Office office,final Loan loan,final AppUser appUser,final Date dueDate) {
        return new LoanTransaction(loan,office, dueDate, LoanTransactionType.SCHEDULER.getValue(),DateUtils.getDateOfTenant(),
                DateUtils.getDateOfTenant(), appUser);
    }
    public void updateChargesComponents(final Money feeCharges, final Money penaltyCharges,final BigDecimal selfSharePenaltyAmount,
                                        final BigDecimal partnerSharePenaltyAmount,final BigDecimal selfFeeChargesPortion,final BigDecimal partnerFeeChargesPortion) {
        final MonetaryCurrency currency = feeCharges.getCurrency();
        this.feeChargesPortion = defaultToNullIfZero(getFeeChargesPortion(currency).plus(feeCharges).getAmount());
        this.penaltyChargesPortion = defaultToNullIfZero(getPenaltyChargesPortion(currency).plus(penaltyCharges).getAmount());
        this.selfPenaltyChargesPortion = defaultToNullIfZero(getSelfPenaltyChargesPortion(currency).plus(selfSharePenaltyAmount).getAmount());
        this.partnerPenaltyChargesPortion = defaultToNullIfZero(getPartnerPenaltyChargesPortion(currency).plus(partnerSharePenaltyAmount).getAmount());
        this.selfFeeChargesPortion =defaultToNullIfZero(getSelfFeeChargesPortion(currency).plus(selfFeeChargesPortion).getAmount());
        this.partnerFeeChargesPortion = defaultToNullIfZero(getPartnerFeeChargesPortion(currency).plus(partnerFeeChargesPortion).getAmount());
    }

    private void updateChargesComponents(final Money feeCharges, final Money penaltyCharges, final Money unrecognizedCharges) {
        final MonetaryCurrency currency = feeCharges.getCurrency();
        this.feeChargesPortion = defaultToNullIfZero(getFeeChargesPortion(currency).plus(feeCharges).getAmount());
        this.penaltyChargesPortion = defaultToNullIfZero(getPenaltyChargesPortion(currency).plus(penaltyCharges).getAmount());

        this.unrecognizedIncomePortion = defaultToNullIfZero(getUnrecognizedIncomePortion(currency).plus(unrecognizedCharges).getAmount());
    }

    private void updateInterestComponent(final Money interest, final Money unrecognizedInterest) {
        final MonetaryCurrency currency = interest.getCurrency();
        this.interestPortion = defaultToNullIfZero(getInterestPortion(currency).plus(interest).getAmount());
        this.selfInterestPortion = defaultToNullIfZero(getSelfInterestPortion(currency).plus(selfInterestPortion).getAmount());
        this.partnerInterestPortion = defaultToNullIfZero(getPartnerInterestPortion(currency).plus(partnerInterestPortion).getAmount());

        this.unrecognizedIncomePortion = defaultToNullIfZero(getUnrecognizedIncomePortion(currency).plus(unrecognizedInterest).getAmount());
    }

    public void adjustInterestComponent(final MonetaryCurrency currency) {
        this.interestPortion = defaultToNullIfZero(getInterestPortion(currency).minus(getUnrecognizedIncomePortion(currency)).getAmount());
    }

    public void updateComponentsAndTotal(final Money principal, final Money interest, final Money feeCharges, final Money penaltyCharges,final Money selfPrincipal,final Money partnerPrincipal,
                                         final Money selfInterestCharged,final Money partnerInterestCharged,final Money selfPenalShareAmount,
                                         final Money partnerPenalShareAmount,final BigDecimal selfFeeChargesPortion ,final BigDecimal partnerFeeChargesPortion) {


        updateComponents(principal, interest, feeCharges, penaltyCharges,selfPrincipal,partnerPrincipal,selfInterestCharged,
                partnerInterestCharged,selfPenalShareAmount.getAmount(),partnerPenalShareAmount.getAmount(),selfFeeChargesPortion,partnerFeeChargesPortion);

        final MonetaryCurrency currency = principal.getCurrency();

        this.amount = getPrincipalPortion(currency).plus(getInterestPortion(currency)).plus(getFeeChargesPortion(currency))
                .plus(getPenaltyChargesPortion(currency)).getAmount();
    }

    public void updateOverPayments(final Money overPayment) {
        final MonetaryCurrency currency = overPayment.getCurrency();
        this.overPaymentPortion = defaultToNullIfZero(getOverPaymentPortion(currency).plus(overPayment).getAmount());
    }

    public Money getPrincipalPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalPortion);
    }

    public Money getSelfPrincipalPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPrincipalPortion);
    }

    public Money getPartnerPrincipalPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPrincipalPortion);
    }

    public BigDecimal getPrincipalPortion() {
        return this.principalPortion;
    }

    public BigDecimal getSelfPrincipalPortion() {
        return this.selfPrincipalPortion;
    }

    public BigDecimal getPartnerPrincipalPortion() {
        return this.partnerPrincipalPortion;
    }

    public Money getInterestPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestPortion);
    }

    public BigDecimal getInterestPortion() {
        return this.interestPortion;
    }

    public Money getSelfInterestPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfInterestPortion);
    }

    public Money getPartnerInterestPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerInterestPortion);
    }

    public Money getUnrecognizedIncomePortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.unrecognizedIncomePortion);
    }

    public Money getFeeChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesPortion);
    }

    public Money getPenaltyChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesPortion);
    }

    public Money getSelfPenaltyChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfPenaltyChargesPortion);
    }

    public Money getPartnerPenaltyChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerPenaltyChargesPortion);
    }

    public Money getOverPaymentPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.overPaymentPortion);
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public Money getSelfDue(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfDue);
    }

    public Money getPartnerDue(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerDue);
    }


    public LocalDate getTransactionDate() {
        return LocalDate.ofInstant(this.dateOf.toInstant(), DateUtils.getDateTimeZoneOfTenant());
    }

    public Date getDateOf() {
        return this.dateOf;
    }

    public LoanTransactionType getTypeOf() {
        return LoanTransactionType.fromInt(this.typeOf);
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public boolean isNotReversed() {
        return !isReversed();
    }

    public void setReversed() {
        this.reversed = true;
    }

    public void setManuallyAdjustedOrReversed() {
        this.manuallyAdjustedOrReversed = true;
    }

    public void setReceiptReferenceNumber(String receiptReferenceNumber) {
        this.receiptReferenceNumber = receiptReferenceNumber;
    }

    public void setPartnerTransferUtr(String partnerTransferUtr) {
        this.partnerTransferUtr = partnerTransferUtr;
    }

    public void setPartnerTransferDate(Date partnerTransferDate) {
        this.partnerTransferDate = partnerTransferDate;
    }

    public void setRepaymentMode(CodeValue repaymentMode) {
        this.repaymentMode = repaymentMode;
    }

    public boolean isRepaymentType() {
        return isRepayment() || isMerchantIssuedRefund() || isPayoutRefund() || isGoodwillCredit() || isForeclosure();
    }

    public boolean isForeclosure() {
            return LoanTransactionType.FORECLOSURE.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isRepayment() {
        return LoanTransactionType.REPAYMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isMerchantIssuedRefund() {
        return LoanTransactionType.MERCHANT_ISSUED_REFUND.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isPayoutRefund() {
        return LoanTransactionType.PAYOUT_REFUND.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isGoodwillCredit() {
        return LoanTransactionType.GOODWILL_CREDIT.equals(getTypeOf()) && isNotReversed();
    }

    public BigDecimal getAdvanceAmount() {
        return advanceAmount!=null?advanceAmount :BigDecimal.ZERO;
    }

    public Money getAdvanceAmount( MonetaryCurrency currency) {
        return Money.of(currency ,this.advanceAmount);
    }


    public void setAdvanceAmount(BigDecimal advanceAmount) {
        this.advanceAmount = advanceAmount;
    }

    public boolean isNotRepaymentType() {
        return !isRepaymentType();
    }

    public boolean isIncomePosting() {
        return LoanTransactionType.INCOME_POSTING.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isNotIncomePosting() {
        return !isIncomePosting();
    }

    public boolean isDisbursement() {
        return LoanTransactionType.DISBURSEMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isRepaymentAtDisbursement() {
        return LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isNotRecoveryRepayment() {
        return !isRecoveryRepayment();
    }

    public boolean isRecoveryRepayment() {
        return LoanTransactionType.RECOVERY_REPAYMENT.equals(getTypeOf()) && isNotReversed();
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public boolean isInterestWaiver() {
        return LoanTransactionType.WAIVE_INTEREST.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isChargesWaiver() {
        return LoanTransactionType.WAIVE_CHARGES.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isNotInterestWaiver() {
        return !isInterestWaiver();
    }

    public boolean isWaiver() {
        return isInterestWaiver() || isChargesWaiver();
    }

    public boolean isNotWaiver() {
        return !isInterestWaiver() && !isChargesWaiver();
    }

    public boolean isNotCreditBalanceRefund() {
        return !isCreditBalanceRefund();
    }

    public boolean isChargePayment() {
        return getTypeOf().isChargePayment() && isNotReversed();
    }

    public boolean isPenaltyPayment() {
        boolean isPenalty = false;
        if (isChargePayment()) {
            for (final LoanChargePaidBy chargePaidBy : this.loanChargesPaid) {
                isPenalty = chargePaidBy.getLoanCharge().isPenaltyCharge();
                break;
            }
        }
        return isPenalty;
    }

    public boolean isWriteOff() {
        return getTypeOf().isWriteOff() && isNotReversed();
    }

    public boolean isIdentifiedBy(final Long identifier) {
        return getId().equals(identifier);
    }

    public boolean isBelongingToLoanOf(final Loan check) {
        return this.loan.getId().equals(check.getId());
    }

    public boolean isNotBelongingToLoanOf(final Loan check) {
        return !isBelongingToLoanOf(check);
    }

    public boolean isNonZero() {
        return this.amount.subtract(BigDecimal.ZERO).doubleValue() > 0;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public boolean isGreaterThan(final Money monetaryAmount) {
        return getAmount(monetaryAmount.getCurrency()).isGreaterThan(monetaryAmount);
    }

    public boolean isGreaterThanZero(final MonetaryCurrency currency) {
        return getAmount(currency).isGreaterThanZero();
    }

    public boolean isGreaterThanZeroAndLessThanOrEqualTo(BigDecimal totalOverpaid) {
        return isNonZero() && this.amount.compareTo(totalOverpaid) <= 0;
    }

    public boolean isNotZero(final MonetaryCurrency currency) {
        return !getAmount(currency).isZero();
    }

    private BigDecimal defaultToNullIfZero(final BigDecimal value) {
        BigDecimal result = value;
        if (BigDecimal.ZERO.compareTo(value) == 0) {
            result = null;
        }
        return result;
    }

    public LoanTransactionData toData(final CurrencyData currencyData, final AccountTransferData transfer) {
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(this.typeOf);
        PaymentDetailData paymentDetailData = null;
        if (this.paymentDetail != null) {
            paymentDetailData = this.paymentDetail.toData();
        }
        return new LoanTransactionData(getId(), this.office.getId(), this.office.getName(), transactionType, paymentDetailData,
                currencyData, getTransactionDate(), this.amount, this.loan.getNetDisbursalAmount(), this.principalPortion,
                this.interestPortion, this.feeChargesPortion, this.penaltyChargesPortion, this.overPaymentPortion, this.externalId,
                transfer, null, outstandingLoanBalance, this.unrecognizedIncomePortion, this.manuallyAdjustedOrReversed,this.selfPrincipalPortion,this.partnerPrincipalPortion,this.selfInterestPortion,this.partnerInterestPortion,
                this.selfDue,this.partnerDue,selfOutstandingLoanBalance,partneroutstandingLoanBalance,null,null,this.selfFeeChargesPortion,this.partnerFeeChargesPortion,null,null,null);
    }

    public Map<String, Object> toMapData(final CurrencyData currencyData) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<>();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(this.typeOf);

        thisTransactionData.put("id", getId());
        thisTransactionData.put("officeId", this.office.getId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", Boolean.valueOf(isReversed()));
        thisTransactionData.put("date", getTransactionDate());
        thisTransactionData.put("currency", currencyData);
        thisTransactionData.put("amount", this.amount);
        thisTransactionData.put("selfDue", this.selfDue);
        thisTransactionData.put("partnerDue", this.partnerDue);

        thisTransactionData.put("netDisbursalAmount", this.loan.getNetDisbursalAmount());
        thisTransactionData.put("principalPortion", this.principalPortion);
        thisTransactionData.put("selfPrincipalPortion", this.selfPrincipalPortion);
        thisTransactionData.put("partnerPrincipalPortion", this.partnerPrincipalPortion);

        thisTransactionData.put("interestPortion", this.interestPortion);
        thisTransactionData.put("selfInterestPortion", this.selfInterestPortion);
        thisTransactionData.put("partnerInterestPortion", this.partnerInterestPortion);

        thisTransactionData.put("feeChargesPortion", this.feeChargesPortion);
        thisTransactionData.put("penaltyChargesPortion", this.penaltyChargesPortion);
        thisTransactionData.put("overPaymentPortion", this.overPaymentPortion);

        if (this.paymentDetail != null) {
            thisTransactionData.put("paymentTypeId", this.paymentDetail.getPaymentType().getId());
        }

        if (!this.loanChargesPaid.isEmpty()) {
            final List<Map<String, Object>> loanChargesPaidData = new ArrayList<>();
            for (final LoanChargePaidBy chargePaidBy : this.loanChargesPaid) {
                final Map<String, Object> loanChargePaidData = new LinkedHashMap<>();
                loanChargePaidData.put("chargeId", chargePaidBy.getLoanCharge().getCharge().getId());
                loanChargePaidData.put("isPenalty", chargePaidBy.getLoanCharge().isPenaltyCharge());
                loanChargePaidData.put("loanChargeId", chargePaidBy.getLoanCharge().getId());
                loanChargePaidData.put("amount", chargePaidBy.getAmount());

                loanChargesPaidData.add(loanChargePaidData);
            }
            thisTransactionData.put("loanChargesPaid", loanChargesPaidData);
        }

        return thisTransactionData;
    }

    public Loan getLoan() {
        return this.loan;
    }

    public Set<LoanChargePaidBy> getLoanChargesPaid() {
        return this.loanChargesPaid;
    }

    public void setLoanChargesPaid(final Set<LoanChargePaidBy> loanChargesPaid) {
        this.loanChargesPaid = loanChargesPaid;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public boolean isRefund() {
        return LoanTransactionType.REFUND.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isCreditBalanceRefund() {
        return LoanTransactionType.CREDIT_BALANCE_REFUND.equals(getTypeOf()) && isNotReversed();
    }

    public void updateExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public boolean isAccrual() {
        return LoanTransactionType.ACCRUAL.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isNonMonetaryTransaction() {
        return isNotReversed() && (LoanTransactionType.CONTRA.equals(getTypeOf())
                || LoanTransactionType.MARKED_FOR_RESCHEDULING.equals(getTypeOf())
                || LoanTransactionType.APPROVE_TRANSFER.equals(getTypeOf()) || LoanTransactionType.INITIATE_TRANSFER.equals(getTypeOf())
                || LoanTransactionType.REJECT_TRANSFER.equals(getTypeOf()) || LoanTransactionType.WITHDRAW_TRANSFER.equals(getTypeOf()));
    }

    public void updateOutstandingLoanBalance(BigDecimal outstandingLoanBalance,BigDecimal selfOutstanding,BigDecimal partnerOutstanding) {
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.selfOutstandingLoanBalance=selfOutstanding;
        this.partneroutstandingLoanBalance=partnerOutstanding;
    }

    public boolean isNotRefundForActiveLoan() {
        // TODO Auto-generated method stub
        return !isRefundForActiveLoan();
    }

    public boolean isRefundForActiveLoan() {
        return LoanTransactionType.REFUND_FOR_ACTIVE_LOAN.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isManuallyAdjustedOrReversed() {
        return this.manuallyAdjustedOrReversed;
    }

    public boolean isNotManuallyAdjustedOrReversed() {
        return !this.manuallyAdjustedOrReversed;
    }

    public void manuallyAdjustedOrReversed() {
        this.manuallyAdjustedOrReversed = true;
    }

    private LocalDate getCreatedDate() {
        return (this.createdDate == null) ? LocalDate.now(DateUtils.getDateTimeZoneOfTenant())
                : LocalDate.ofInstant(this.createdDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
    }

    public LocalDateTime getCreatedDateTime() {
        return (this.createdDate == null) ? LocalDateTime.now(DateUtils.getDateTimeZoneOfTenant())
                : LocalDateTime.ofInstant(this.createdDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
    }

    public boolean isLastTransaction(final LoanTransaction loanTransaction) {
        boolean isLatest = false;
        if (loanTransaction != null) {
            isLatest = this.getTransactionDate().isBefore(loanTransaction.getTransactionDate())
                    || (this.getTransactionDate().isEqual(loanTransaction.getTransactionDate())
                    && this.getCreatedDate().isBefore(loanTransaction.getCreatedDate()));
        }
        return isLatest;
    }

    public boolean isLatestTransaction(final LoanTransaction loanTransaction) {
        boolean isLatest = false;
        if (loanTransaction != null) {
            isLatest = this.getCreatedDate().isBefore(loanTransaction.getCreatedDate());
        }
        return isLatest;
    }

    public void updateLoanTransactionToRepaymentScheduleMappings(final Collection<LoanTransactionToRepaymentScheduleMapping> mappings) {
        Collection<LoanTransactionToRepaymentScheduleMapping> retainMappings = new ArrayList<>();
        for (LoanTransactionToRepaymentScheduleMapping updatedrepaymentScheduleMapping : mappings) {
            updateMapingDetail(retainMappings, updatedrepaymentScheduleMapping);
        }
    }

    public void updateLoanTransactionToRepaymentScheduleMappings(final LoanTransactionToRepaymentScheduleMapping mappings,LoanTransaction loanTransaction) {
        Collection<LoanTransactionToRepaymentScheduleMapping> retainMappings = new ArrayList<>();
            updateMapingDetails(retainMappings, mappings,loanTransaction);

    }

    private boolean updateMapingDetails(Collection<LoanTransactionToRepaymentScheduleMapping> retainMappings, LoanTransactionToRepaymentScheduleMapping updatedrepaymentScheduleMapping, LoanTransaction loanTransaction) {
        boolean isMappingUpdated = false;
        for (LoanTransactionToRepaymentScheduleMapping repaymentScheduleMapping : this.loanTransactionToRepaymentScheduleMappings) {
            if (updatedrepaymentScheduleMapping.getLoanRepaymentScheduleInstallment().getId() != null
                    && repaymentScheduleMapping.getLoanRepaymentScheduleInstallment().getDueDate()
                    .equals(updatedrepaymentScheduleMapping.getLoanRepaymentScheduleInstallment().getDueDate())) {
                repaymentScheduleMapping.setComponents(updatedrepaymentScheduleMapping.getPrincipalPortion(),
                        updatedrepaymentScheduleMapping.getInterestPortion(), updatedrepaymentScheduleMapping.getFeeChargesPortion(),
                        updatedrepaymentScheduleMapping.getPenaltyChargesPortion(),updatedrepaymentScheduleMapping.getSelfPrincipalPortion(),updatedrepaymentScheduleMapping.getPartnerPrincipalPortion(),
                        updatedrepaymentScheduleMapping.getSelfInterestPortion(),updatedrepaymentScheduleMapping.getPartnerInterestPortion());
                isMappingUpdated = true;
                retainMappings.add(repaymentScheduleMapping);
                break;
            }
        }
        if (!isMappingUpdated) {
            updatedrepaymentScheduleMapping.setLoanTransaction(loanTransaction);
            this.loanTransactionToRepaymentScheduleMappings.add(updatedrepaymentScheduleMapping);
            retainMappings.add(updatedrepaymentScheduleMapping);
        }
        return isMappingUpdated;
    }

    private boolean updateMapingDetail(final Collection<LoanTransactionToRepaymentScheduleMapping> retainMappings,
                                       final LoanTransactionToRepaymentScheduleMapping updatedrepaymentScheduleMapping) {
        boolean isMappingUpdated = false;
        for (LoanTransactionToRepaymentScheduleMapping repaymentScheduleMapping : this.loanTransactionToRepaymentScheduleMappings) {
            if (updatedrepaymentScheduleMapping.getLoanRepaymentScheduleInstallment().getId() != null
                    && repaymentScheduleMapping.getLoanRepaymentScheduleInstallment().getDueDate()
                    .equals(updatedrepaymentScheduleMapping.getLoanRepaymentScheduleInstallment().getDueDate())) {
                repaymentScheduleMapping.setComponents(updatedrepaymentScheduleMapping.getPrincipalPortion(),
                        updatedrepaymentScheduleMapping.getInterestPortion(), updatedrepaymentScheduleMapping.getFeeChargesPortion(),
                        updatedrepaymentScheduleMapping.getPenaltyChargesPortion(),updatedrepaymentScheduleMapping.getSelfPrincipalPortion(),updatedrepaymentScheduleMapping.getPartnerPrincipalPortion(),
                        updatedrepaymentScheduleMapping.getSelfInterestPortion(),updatedrepaymentScheduleMapping.getPartnerInterestPortion());
                isMappingUpdated = true;
                retainMappings.add(repaymentScheduleMapping);
                break;
            }
        }
        if (!isMappingUpdated) {
            updatedrepaymentScheduleMapping.setLoanTransaction(this);
            this.loanTransactionToRepaymentScheduleMappings.add(updatedrepaymentScheduleMapping);
            retainMappings.add(updatedrepaymentScheduleMapping);
        }
        return isMappingUpdated;
    }

    public Set<LoanTransactionToRepaymentScheduleMapping> getLoanTransactionToRepaymentScheduleMappings() {
        return this.loanTransactionToRepaymentScheduleMappings;
    }

    public Boolean isAllowTypeTransactionAtTheTimeOfLastUndo() {
        return isDisbursement() || isAccrual() || isRepaymentAtDisbursement();
    }

    public void updateCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isAccrualTransaction() {
        return isAccrual();
    }

    public BigDecimal getOutstandingLoanBalance() {
        return outstandingLoanBalance;
    }



    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }

    public boolean isPaymentTransaction() {
        return this.isNotReversed() && !(this.isDisbursement() || this.isAccrual() || this.isRepaymentAtDisbursement()
                || this.isNonMonetaryTransaction() || this.isIncomePosting());
    }

    public Set<LoanCollateralManagement> getLoanCollateralManagementSet() {
        return this.loanCollateralManagementSet;
    }


    public void updateLoanTransactionAmountForForeclosure( BigDecimal gstAtForeclosure) {
        if(gstAtForeclosure!= null){
        this.amount = amount.subtract(gstAtForeclosure);}
    }

    public void updateType(Integer value) {
        this.typeOf=value;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public void updateEvent(LoanTransaction loanTransaction) {
        if (loanTransaction.getPrincipalPortion() == null && loanTransaction.getInterestPortion() == null) {
            loanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue());
        }
    }

    public Money getTransactionAmount(MonetaryCurrency currency) {
        return getAmount(currency).minus(getAdvanceAmount());
    }

    public void setCollectionReport(List<CollectionReport> collectionReport) {
        this.collectionReport.addAll(collectionReport);
    }

    public void setCollectionReport(CollectionReport collectionReport) {
        this.collectionReport.add(collectionReport);
    }

    public void updateCollectionReport(List<CollectionReport> collectionReports) {
        for(CollectionReport collectionReport1 : collectionReports){
            updateCollectionReportTransaction(collectionReport1);
        }
    }

    private void updateCollectionReportTransaction(CollectionReport collectionReports) {
        boolean iscollectionReportemty = false;
        for(CollectionReport collectionReport1 : this.collectionReport){
            if(!collectionReport1.getDateOf().equals(collectionReports.getDateOf()) &&
                    collectionReport1.getPrincipalPortion().doubleValue() == (collectionReports.getPrincipalPortion().doubleValue()) &&
                    collectionReport1.getInterestPortion().doubleValue() == (collectionReports.getInterestPortion().doubleValue())){
                this.collectionReport.add(collectionReports);
                iscollectionReportemty = true;
            }
        }
        if(!iscollectionReportemty){
            this.collectionReport.add(collectionReports);
        }
    }

    public String getReceiptReferenceNumber() {
        return Objects.nonNull(this.receiptReferenceNumber) ? receiptReferenceNumber:null;
    }

    public String getPartnerTransferUtr() {
        return  Objects.nonNull(this.partnerTransferUtr) ? partnerTransferUtr:null;
    }

    public Date getPartnerTransferDate() {
        return Objects.nonNull(this.partnerTransferDate) ? partnerTransferDate:null;
    }

    public CodeValue getRepaymentMode() {
        return Objects.nonNull(this.repaymentMode) ? repaymentMode:null;
    }

    public boolean isNewTransaction(Loan loan,LoanTransaction loanTransaction) {

        return loan.getLoanTransactions().stream().anyMatch(transaction -> transaction.getAmount(loan.getCurrency()).equals(loanTransaction.getAmount(loan.getCurrency()))
                && transaction.getPrincipalPortion(loan.getCurrency()).equals(loanTransaction.getPrincipalPortion(loan.getCurrency()))
                && transaction.getInterestPortion(loan.getCurrency()).equals(loanTransaction.getInterestPortion(loan.getCurrency()))
                && transaction.getValueDate().equals(loanTransaction.getValueDate()));
    }

    public void updateAmount(BigDecimal value) {
        this.amount =  amount.add(value);
    }
    public void updateChargePayment(final Loan loan, final AppUser appUser) {
        this.loan = loan;
        this.office = loan.getOffice();
        this.appUser = appUser;
        //this.event = "LOAN_CHARGE_REPAYMENT";
        this.typeOf = LoanTransactionType.CHARGE_PAYMENT.getValue();
        this.valueDate = this.dateOf;
        this.createdDate = new Date();
        this.submittedOnDate = new Date();
    };

    public void updateChargeWaiver(final Loan loan, final AppUser appUser, final LocalDate transactionDate) {
        this.loan = loan;
        this.office = loan.getOffice();
        this.appUser = appUser;
        this.event = "LOAN_CHARGE_WAIVER";
        this.typeOf = LoanTransactionType.WAIVE_CHARGES.getValue();
        this.valueDate = DateUtils.convertLocalDateToDate(transactionDate);
        this.dateOf = DateUtils.convertLocalDateToDate(transactionDate);
        this.createdDate = new Date();
        this.submittedOnDate = new Date();
    }

    // TODO missing hashCode(), equals(Object obj), but probably OK as long as
    // this is never stored in a Collection.


    public static LoanTransaction getNewLoanTransaction(LoanTransaction loanTransaction,BigDecimal amount,BigDecimal principalPortion,
                                                        BigDecimal interestPortion,BigDecimal feeChargesPortion,BigDecimal penaltyChargesPortion,
                                                        BigDecimal selfPrincipalPortion,BigDecimal partnerPrincipalPortion,BigDecimal selfInterestPortion,BigDecimal partnerInterestPortion,
                                                        BigDecimal selfDue,BigDecimal partnerDue,String event) {

        return new LoanTransaction(loanTransaction.getLoan(),loanTransaction.getOffice(), loanTransaction.getTypeOf().getValue(),
                loanTransaction.getDateOf(),amount,principalPortion,interestPortion,feeChargesPortion,penaltyChargesPortion,
                loanTransaction.getOverPaymentPortion(),loanTransaction.isReversed(), loanTransaction.getPaymentDetail(),loanTransaction.getExternalId(),
                loanTransaction.getCreatedDateTime(),loanTransaction.getAppUser(),selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,
                selfDue,partnerDue,event,loanTransaction.getReceiptReferenceNumber(),loanTransaction.getPartnerTransferUtr(),loanTransaction.getPartnerTransferDate(),
                loanTransaction.getRepaymentMode(),BigDecimal.ZERO,null);
    }

    public void updateInterestPortion(Money interest, Money selfInterestPortion, Money partnerInterestPortion) {

        MonetaryCurrency currency =  interest.getCurrency();
        this.interestPortion = defaultToNullIfZero(getInterestPortion(currency).plus(interest).getAmount());
        this.selfInterestPortion = defaultToNullIfZero(getSelfInterestPortion(currency).plus(selfInterestPortion).getAmount());
        this.partnerInterestPortion = defaultToNullIfZero(getPartnerInterestPortion(currency).plus(partnerInterestPortion).getAmount());

    }

    public void feechargesPortion(Money feeAmount, BigDecimal selfFeeAmount, Money partnerFeeAmount) {

        MonetaryCurrency currency =  feeAmount.getCurrency();
        this.feeChargesPortion = defaultToNullIfZero(getFeeChargesPortion(currency).plus(feeAmount).getAmount());
        this.selfFeeChargesPortion=defaultToNullIfZero(getSelfFeeChargesPortion(currency).plus(selfFeeAmount).getAmount());
        this.partnerFeeChargesPortion=defaultToNullIfZero(getPartnerFeeChargesPortion(currency).plus(partnerFeeAmount).getAmount());
    }

    public void updatePenaltyCharge(Money penaltyChargesPortion, BigDecimal selfSharePenaltyAmount, BigDecimal partnerSharePenaltyAmount) {
        MonetaryCurrency currency =  penaltyChargesPortion.getCurrency();
        this.penaltyChargesPortion = defaultToNullIfZero(getPenaltyChargesPortion(currency).plus(penaltyChargesPortion).getAmount());
        this.selfPenaltyChargesPortion=defaultToNullIfZero(getSelfPenaltyChargesPortion(currency).plus(selfSharePenaltyAmount).getAmount());
        this.partnerPenaltyChargesPortion=defaultToNullIfZero(getPartnerPenaltyChargesPortion(currency).plus(partnerSharePenaltyAmount).getAmount());
    }

    public void updateLoanTransaction(PrincipalAppropriationData principalAppropriationData,
                                      InterestAppropriationData interestAppropriationData, MonetaryCurrency currency) {


        Money principal = Objects.nonNull(principalAppropriationData) ? principalAppropriationData.principal(): Money.zero(currency);
        Money selfPrincipal = Objects.nonNull(principalAppropriationData)? principalAppropriationData.selfPrincipal() : Money.zero(currency);;
        Money partnerPrincipal = Objects.nonNull(principalAppropriationData)? principalAppropriationData.partnerPrincipal(): Money.zero(currency);

        Money interest = Objects.nonNull(interestAppropriationData)? interestAppropriationData.interest(): Money.zero(currency);
        Money selfInterest =Objects.nonNull(interestAppropriationData)? interestAppropriationData.selfInterest(): Money.zero(currency);
        Money partnerInterest = Objects.nonNull(interestAppropriationData)? interestAppropriationData.partnerInterest(): Money.zero(currency);


        this.principalPortion = defaultToNullIfZero(getPrincipalPortion(currency).plus(principal).getAmount());
        this.selfPrincipalPortion=defaultToNullIfZero(getSelfPrincipalPortion(currency).plus(selfPrincipal).getAmount());
        this.partnerPrincipalPortion=defaultToNullIfZero(getPartnerPrincipalPortion(currency).plus(partnerPrincipal).getAmount());

        this.interestPortion = defaultToNullIfZero(getInterestPortion(currency).plus(interest).getAmount());
        this.selfInterestPortion = defaultToNullIfZero(getSelfInterestPortion(currency).plus(selfInterest).getAmount());
        this.partnerInterestPortion = defaultToNullIfZero(getPartnerInterestPortion(currency).plus(partnerInterest).getAmount());

        this.selfDue = defaultToNullIfZero(getSelfPrincipalPortion(currency).plus(getSelfInterestPortion(currency)).getAmount()
                .add(getSelfPenaltyChargesPortion(currency).getAmount()).add(getSelfFeeChargesPortion(currency).getAmount()).add(getSelfBounceChargesPortion(currency).getAmount()));

        this.partnerDue = defaultToNullIfZero(getPartnerPrincipalPortion(currency).plus(getPartnerInterestPortion(currency)).getAmount()
                .add(getPartnerPenaltyChargesPortion(currency).getAmount().add((getPartnerFeeChargesPortion(currency).getAmount())).add(getPartnerBounceChargesPortion(currency).getAmount())));
    }

    public void updateSelfAndPartnerAmount(BigDecimal feeChargesCharged, BigDecimal bounceCharge) {
        this.selfDue = getSelfDue().add(feeChargesCharged).add(bounceCharge);
    }

    public BigDecimal getSelfDue() {
        return Objects.isNull(selfDue)?BigDecimal.ZERO:selfDue;
    }
    public BigDecimal getPartnerDue() {
        return Objects.isNull(partnerDue)?BigDecimal.ZERO:partnerDue;
    }

    public Money getSelfFeeChargesPortion(final MonetaryCurrency currency){
        return Money.of(currency,this.selfFeeChargesPortion);
    }

    public Money getPartnerFeeChargesPortion(final MonetaryCurrency currency){
        return Money.of(currency,this.partnerFeeChargesPortion);
    }
    public Money getSelfBounceChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfBounceChargesPortion);
    }
    public Money getPartnerBounceChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerBounceChargesPortion);
    }


}
