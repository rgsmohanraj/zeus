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
import java.util.Objects;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.collection.data.InterestAppropriationData;
import org.vcpl.lms.portfolio.collection.data.PrincipalAppropriationData;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.domain.ServicerFeeAmountCalculation;

@Entity
@Setter
@Getter
@Table(name = "m_loan_transaction_repayment_schedule_mapping")
public class LoanTransactionToRepaymentScheduleMapping extends AbstractPersistableCustom {

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "loan_transaction_id", nullable = false)
    private LoanTransaction loanTransaction;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "loan_repayment_schedule_id", nullable = false)
    private LoanRepaymentScheduleInstallment installment;

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

    @Column(name = "amount", scale = 6, precision = 19)
    private BigDecimal amount;

    @Column(name = "self_amount", scale = 6, precision = 19)
    private BigDecimal selfDue;

    @Column(name = "partner_amount", scale = 6, precision = 19)
    private BigDecimal partnerDue;

    @Column(name = "advance_amount", scale = 6, precision = 19)
    private BigDecimal advanceAmount;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "LoanTransactionToRepaymentScheduleMapping", optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private ServicerFeeAmountCalculation servicerFeeAmountCalculation;

    @Column(name = "bounce_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal bounceChargesPortion;

    public void setServicerFeeAmountCalculation(ServicerFeeAmountCalculation servicerFeeAmountCalculation) {
        this.servicerFeeAmountCalculation = servicerFeeAmountCalculation;
    }

    protected LoanTransactionToRepaymentScheduleMapping() {

    }

    private LoanTransactionToRepaymentScheduleMapping(final LoanTransaction loanTransaction,
            final LoanRepaymentScheduleInstallment installment, final BigDecimal principalPortion, final BigDecimal interestPortion,
            final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion, final BigDecimal amount,final BigDecimal selfPrincipalPortion,final BigDecimal partnerPrincipalPortion,
                                                      final BigDecimal selfInterestPortion,final BigDecimal partnerInterestPortion,final BigDecimal selfDue,final BigDecimal partnerDue,final BigDecimal totalPaidInAdvance,final BigDecimal bounceChargesPortion) {
        this.loanTransaction = loanTransaction;
        this.installment = installment;
        this.principalPortion = principalPortion;
        this.selfPrincipalPortion = selfPrincipalPortion;
        this.partnerPrincipalPortion = partnerPrincipalPortion;

        this.interestPortion = interestPortion;
        this.selfInterestPortion = selfInterestPortion;
        this.partnerInterestPortion = partnerInterestPortion;

        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.amount = amount;
        this.selfDue = selfDue;
        this.partnerDue = partnerDue;
        this.advanceAmount = totalPaidInAdvance;
        this.bounceChargesPortion=bounceChargesPortion;

    }
    private LoanTransactionToRepaymentScheduleMapping(final LoanTransaction loanTransaction,
                                                      final LoanRepaymentScheduleInstallment installment, final BigDecimal principalPortion, final BigDecimal interestPortion,
                                                      final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion, final BigDecimal amount,final BigDecimal selfPrincipalPortion,final BigDecimal partnerPrincipalPortion,
                                                      final BigDecimal selfInterestPortion,final BigDecimal partnerInterestPortion,final BigDecimal selfDue,final BigDecimal partnerDue,final BigDecimal totalPaidInAdvance) {
        this.loanTransaction = loanTransaction;
        this.installment = installment;
        this.principalPortion = principalPortion;
        this.selfPrincipalPortion = selfPrincipalPortion;
        this.partnerPrincipalPortion = partnerPrincipalPortion;

        this.interestPortion = interestPortion;
        this.selfInterestPortion = selfInterestPortion;
        this.partnerInterestPortion = partnerInterestPortion;

        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.amount = amount;
        this.selfDue = selfDue;
        this.partnerDue = partnerDue;
        this.advanceAmount = totalPaidInAdvance;

    }

    public static LoanTransactionToRepaymentScheduleMapping createFrom(final LoanTransaction loanTransaction,
            final LoanRepaymentScheduleInstallment installment, final Money principalPortion, final Money interestPortion,
            final Money feeChargesPortion, final Money penaltyChargesPortion,final Money selfPrincipalPortion,final Money partnerPrincipalPortion,
                                                                       final Money selfInterestPortion,final Money partnerInterestPortion,final BigDecimal selfDue,final BigDecimal partnerDue,final BigDecimal totalPaidInAdvance,final Money bounceChargesPortion) {
        return new LoanTransactionToRepaymentScheduleMapping(loanTransaction, installment, defaultToNullIfZero(principalPortion),
                defaultToNullIfZero(interestPortion), defaultToNullIfZero(feeChargesPortion), defaultToNullIfZero(penaltyChargesPortion),
                defaultToNullIfZero(principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).plus(totalPaidInAdvance)),defaultToNullIfZero(selfPrincipalPortion),defaultToNullIfZero(partnerPrincipalPortion),defaultToNullIfZero(selfInterestPortion),defaultToNullIfZero(partnerInterestPortion),selfDue,partnerDue,totalPaidInAdvance,defaultToNullIfZero(bounceChargesPortion));
    }
    public static LoanTransactionToRepaymentScheduleMapping createFrom(final LoanTransaction loanTransaction,
                                                                       final LoanRepaymentScheduleInstallment installment, final Money principalPortion, final Money interestPortion,
                                                                       final Money feeChargesPortion, final Money penaltyChargesPortion,final Money selfPrincipalPortion,final Money partnerPrincipalPortion,
                                                                       final Money selfInterestPortion,final Money partnerInterestPortion,final BigDecimal selfDue,final BigDecimal partnerDue,final BigDecimal totalPaidInAdvance) {
        return new LoanTransactionToRepaymentScheduleMapping(loanTransaction, installment, defaultToNullIfZero(principalPortion),
                defaultToNullIfZero(interestPortion), defaultToNullIfZero(feeChargesPortion), defaultToNullIfZero(penaltyChargesPortion),
                defaultToNullIfZero(principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).plus(totalPaidInAdvance)),defaultToNullIfZero(selfPrincipalPortion),defaultToNullIfZero(partnerPrincipalPortion),defaultToNullIfZero(selfInterestPortion),defaultToNullIfZero(partnerInterestPortion),selfDue,partnerDue,totalPaidInAdvance);
    }

    public static LoanTransactionToRepaymentScheduleMapping createFrom(final LoanTransaction loanTransaction,
                                                                       final LoanRepaymentScheduleInstallment installment, PrincipalAppropriationData principalAppropriationData,
                                                                       InterestAppropriationData interestAppropriationData, final Money feeChargesPortion, final Money penaltyChargesPortion,
                                                                       final BigDecimal totalPaidInAdvance) {


        MonetaryCurrency currency = loanTransaction.getLoan().getCurrency();
        Money interst = Objects.nonNull(interestAppropriationData)? interestAppropriationData.interest() : Money.zero(currency);
        Money selfInterst = Objects.nonNull(interestAppropriationData) ? interestAppropriationData.selfInterest() : Money.zero(currency);
        Money partnerInterst = Objects.nonNull(interestAppropriationData)? interestAppropriationData.partnerInterest() : Money.zero(currency);

        Money principal = Objects.nonNull(principalAppropriationData) ? principalAppropriationData.principal() : Money.zero(currency);
        Money selfPrincipal = Objects.nonNull(principalAppropriationData)? principalAppropriationData.selfPrincipal() : Money.zero(currency);
        Money partnerPrincipal = Objects.nonNull(principalAppropriationData) ? principalAppropriationData.partnerPrincipal() : Money.zero(currency);

        Money selfDue =selfInterst.plus(selfPrincipal);
        Money partnerDue = partnerInterst.plus(partnerInterst);
        BigDecimal due =  defaultToNullIfZero(principal.plus(interst).plus(feeChargesPortion).plus(penaltyChargesPortion).plus(totalPaidInAdvance));

        return new LoanTransactionToRepaymentScheduleMapping(loanTransaction, installment, defaultToNullIfZero(principal),
                defaultToNullIfZero(interst), defaultToNullIfZero(feeChargesPortion), defaultToNullIfZero(penaltyChargesPortion),due
                ,defaultToNullIfZero(partnerPrincipal),defaultToNullIfZero(selfPrincipal),
                defaultToNullIfZero(selfInterst),defaultToNullIfZero(partnerInterst),
                selfDue.getAmount(),partnerDue.getAmount(),totalPaidInAdvance);
    }

    private static BigDecimal defaultToNullIfZero(final Money value) {
        BigDecimal result = value.getAmount();
        if (value.isZero()) {
            result = null;
        }
        return result;
    }

    private BigDecimal defaultToZeroIfNull(final BigDecimal value) {
        BigDecimal result = value;
        if (value == null) {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    public LoanRepaymentScheduleInstallment getLoanRepaymentScheduleInstallment() {
        return this.installment;
    }

    public void updateComponents(final Money principal, final Money interest, final Money feeCharges, final Money penaltyCharges) {
        final MonetaryCurrency currency = principal.getCurrency();
        this.principalPortion = defaultToNullIfZero(getPrincipalPortion(currency).plus(principal));
        this.selfPrincipalPortion =defaultToNullIfZero(getSelfPrincipalPortion(currency).plus(selfPrincipalPortion));;
        this.partnerPrincipalPortion = defaultToNullIfZero(getPartnerPrincipalPortion(currency).plus(partnerPrincipalPortion));;

        this.interestPortion = defaultToNullIfZero(getInterestPortion(currency).plus(interest));
        this.selfInterestPortion=defaultToNullIfZero(getSelfInterestPortion(currency).plus(selfInterestPortion));
        this.partnerInterestPortion=defaultToNullIfZero(getPartnerInterestPortion(currency).plus(partnerInterestPortion));
        updateChargesComponents(feeCharges, penaltyCharges);
        updateAmount();
    }

    private void updateAmount() {
        this.amount = defaultToZeroIfNull(getPrincipalPortion()).add(defaultToZeroIfNull(getInterestPortion()))
                .add(defaultToZeroIfNull(getFeeChargesPortion())).add(defaultToZeroIfNull(getPenaltyChargesPortion())).add(defaultToZeroIfNull(getAdvanceAmount()));
        this.selfDue = defaultToZeroIfNull(getSelfPrincipalPortion()).add(defaultToZeroIfNull(getSelfInterestPortion()))
                .add(defaultToZeroIfNull(getFeeChargesPortion())).add(defaultToZeroIfNull(getPenaltyChargesPortion()));
        this.partnerDue = defaultToZeroIfNull(getPartnerPrincipalPortion()).add(defaultToZeroIfNull(getPartnerInterestPortion()))
                .add(defaultToZeroIfNull(getFeeChargesPortion())).add(defaultToZeroIfNull(getPenaltyChargesPortion()));
    }

    public void setComponents(final BigDecimal principal, final BigDecimal interest, final BigDecimal feeCharges,
            final BigDecimal penaltyCharges,final BigDecimal selfPrincipal,final BigDecimal partnerPrincipal,final BigDecimal selfInterestCharged,final BigDecimal partnerInterestCharged) {
        this.principalPortion = principal;
        this.selfPrincipalPortion = selfPrincipal;
        this.partnerPrincipalPortion = partnerPrincipal;

        this.interestPortion = interest;
        this.selfInterestPortion = selfInterestCharged;
        this.partnerInterestPortion = partnerInterestCharged;

        this.feeChargesPortion = feeCharges;
        this.penaltyChargesPortion = penaltyCharges;
        updateAmount();
    }

    private void updateChargesComponents(final Money feeCharges, final Money penaltyCharges) {
        final MonetaryCurrency currency = feeCharges.getCurrency();
        this.feeChargesPortion = defaultToNullIfZero(getFeeChargesPortion(currency).plus(feeCharges));
        this.penaltyChargesPortion = defaultToNullIfZero(getPenaltyChargesPortion(currency).plus(penaltyCharges));
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

    public Money getInterestPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestPortion);
    }

    public Money getSelfInterestPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.selfInterestPortion);
    }

    public Money getPartnerInterestPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.partnerInterestPortion);
    }

    public Money getFeeChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesPortion);
    }

    public Money getPenaltyChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesPortion);
    }

    public LoanTransaction getLoanTransaction() {
        return loanTransaction;
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

    public BigDecimal getInterestPortion() {
        return this.interestPortion;
    }

    public BigDecimal getSelfInterestPortion() {
        return this.selfInterestPortion;
    }

    public BigDecimal getPartnerInterestPortion() {
        return this.partnerInterestPortion;
    }

    public BigDecimal getSelfDue() {
        return this.selfDue;
    }

    public BigDecimal getPartnerDue() {
        return this.partnerDue;
    }

    public BigDecimal getFeeChargesPortion() {
        return this.feeChargesPortion;
    }

    public BigDecimal getPenaltyChargesPortion() {
        return this.penaltyChargesPortion;
    }

    public BigDecimal getAdvanceAmount(){
        return  this.advanceAmount;
    }
    public void setLoanTransaction(LoanTransaction loanTransaction) {
        this.loanTransaction = loanTransaction;
    }

    public void updateComponents(Money principal, Money selfPrincipal, Money partnerPrincipal) {

        this.principalPortion =  principal.getAmount();
        this.selfPrincipalPortion =  selfPrincipal.getAmount();
        this.partnerPrincipalPortion = partnerPrincipal.getAmount();
        updateAmount();


    }
}
