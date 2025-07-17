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
package org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.loanaccount.domain.CollectionReport;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransaction;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;

/**
 * Heavensfamily style {@link LoanRepaymentScheduleTransactionProcessor}.
 *
 * For standard transactions, pays off components in order of interest, then principal.
 *
 * If a transaction results in an advance payment or overpayment for a given installment, the over paid amount is pay
 * off on the principal component of subsequent installments.
 *
 * If the entire principal of an installment is paid in advance then the interest component is waived.
 */
@SuppressWarnings("unused")
public class HeavensFamilyLoanRepaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    /**
     * For late repayments, pay off in the same way as on-time payments, interest first then principal.
     */
    @Override
    protected Money handleTransactionThatIsALateRepaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
            final Money transactionAmountUnprocessed, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,Money transactionSelfAmountUnprocessed,
                                                                       Money transactionPartnerAmountUnprocessed,List<CollectionReport> collectionReports,
                                                                       LoanHistoryRepo loanHistoryRepo,List<LoanTransaction> overDueLoanTransaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLoanTransaction ) {

        return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, transactionAmountUnprocessed,
                transactionMappings,transactionSelfAmountUnprocessed,transactionPartnerAmountUnprocessed,collectionReports,loanHistoryRepo,overDueLoanTransaction,lateLoantransaction,advanceLoanTransaction );
    }

    @Override
    protected boolean isTransactionInAdvanceOfInstallment(final int currentInstallmentIndex,
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate, final Money transactionAmount,final Money transactionSelfAmount,final Money transactionPartnerAmount) {

        boolean isInAdvance = false;

        LocalDate lastInstallmentDueDate = null;
        int previousInstallmentIndex = 0;
        if (currentInstallmentIndex > 0) {
            previousInstallmentIndex = currentInstallmentIndex - 1;
        }

        final LoanRepaymentScheduleInstallment previousInstallment = installments.get(previousInstallmentIndex);
        lastInstallmentDueDate = previousInstallment.getDueDate();

        isInAdvance = !(transactionDate.isAfter(lastInstallmentDueDate) || transactionDate.isEqual(lastInstallmentDueDate));

        return isInAdvance;
    }

    /**
     * For early/'in advance' repayments, pays off principal component only.
     */
    @Override
    protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
                                                                         final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
                                                                         final LocalDate transactionDate, final Money paymentInAdvance,
                                                                         final List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Money selfPaymentInAdvance,
                                                                         Money partnerPaymentInAdvance, List<CollectionReport> collectionReports, LoanHistoryRepo loanHistoryRepo,
                                                                         List<LoanTransaction> listOfLoanTRansaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLoanTransction) {

        final MonetaryCurrency currency = paymentInAdvance.getCurrency();
        Money transactionAmountRemaining = paymentInAdvance;
        Money transactionSelfAmountRemaining = selfPaymentInAdvance;
        Money transactionPartnerAmountRemaining = partnerPaymentInAdvance;

        Money principalPortion = Money.zero(currency);
        Money selfPrincipalPortion = Money.zero(currency);
        Money partnerPrincipalPortion = Money.zero(currency);

        Money interestPortion = Money.zero(currency);
        Money selfInterestPortion = Money.zero(currency);
        Money partnerInterestPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money selfFeeChargesPortion = Money.zero(currency);
        Money partnerFeeChargesPortion = Money.zero(currency);
        Money penaltyChargesPortion = Money.zero(currency);

        Money selfSharePenalAmount = Money.zero(currency);
        Money partnerSharePenalAmount = Money.zero(currency);

        if (loanTransaction.isInterestWaiver()) {
            interestPortion = currentInstallment.waiveInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion,
                    penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,
                    partnerInterestPortion,selfSharePenalAmount.getAmount(),partnerSharePenalAmount.getAmount(),
                    selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        } else if (loanTransaction.isChargePayment()) {
            if (loanTransaction.isPenaltyPayment()) {
                penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
            } else {
                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
            }
        } else {

            if (currentInstallment.isPrincipalNotCompleted(currency)) {
                principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
               currentInstallment.paySelfPrincipalComponent(transactionDate, transactionSelfAmountRemaining);
               currentInstallment.payPartnerPrincipalComponent(transactionDate, transactionPartnerAmountRemaining);

                if (currentInstallment.isPrincipalCompleted(currency)) {
                    // FIXME - KW - if auto waiving interest need to create
                    // another transaction to handle this.
                    currentInstallment.waiveInterestComponent(transactionDate, currentInstallment.getInterestCharged(currency));
                }

                loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,
                        partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfSharePenalAmount.getAmount(),
                        partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());

                transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
                transactionSelfAmountRemaining = transactionSelfAmountRemaining.minus(selfPrincipalPortion);
                transactionPartnerAmountRemaining = transactionPartnerAmountRemaining.minus(partnerPrincipalPortion);


            }

            // 1. pay of principal with over payment.
            principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

             currentInstallment.paySelfPrincipalComponent(transactionDate, transactionSelfAmountRemaining);
            transactionSelfAmountRemaining = transactionSelfAmountRemaining.minus(selfPrincipalPortion);

            currentInstallment.payPartnerPrincipalComponent(transactionDate, transactionPartnerAmountRemaining);
            transactionPartnerAmountRemaining = transactionPartnerAmountRemaining.minus(partnerPrincipalPortion);

            interestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            currentInstallment.paySelfInterestComponent(transactionDate, transactionSelfAmountRemaining);

           currentInstallment.payPartnerInterestComponent(transactionDate, transactionPartnerAmountRemaining);

            feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

            penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
        }

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,
                partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfSharePenalAmount.getAmount(),
                partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null, BigDecimal.ZERO));
        }
        return transactionAmountRemaining;
    }

    /**
     * For normal on-time repayments, pays off interest first, then principal.
     */
    @Override
    protected Money handleTransactionThatIsOnTimePaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
                                                                      final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
                                                                      List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,
                                                                      final Money transactionSelfAmountUnprocessed, final Money transactionPartnerAmountUnprocessed,
                                                                      List<CollectionReport> collectionReports,LoanHistoryRepo loanHistoryRepo,List<LoanTransaction> overDueLoanTransaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLoanTransaction) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
        Money transactionSelfAmountRemaining = transactionSelfAmountUnprocessed;
        Money transactionPartnerAmountRemaining = transactionPartnerAmountUnprocessed;

        Money principalPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money selfPrincipalPortion = Money.zero(transactionSelfAmountRemaining.getCurrency());
        Money partnerPrincipalPortion = Money.zero(transactionPartnerAmountRemaining.getCurrency());

        Money interestPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money selfInterestPortion = Money.zero(transactionSelfAmountRemaining.getCurrency());
        Money partnerInterestPortion = Money.zero(transactionPartnerAmountRemaining.getCurrency());

        Money feeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money selfFeeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money partnerFeeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money penaltyChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());

        Money selfSharePenalAmount = Money.zero(transactionAmountRemaining.getCurrency());
        Money partnerSharePenalAmount = Money.zero(transactionAmountRemaining.getCurrency());

        if (loanTransaction.isChargesWaiver()) {
            penaltyChargesPortion = currentInstallment.waivePenaltyChargesComponent(transactionDate,
                    loanTransaction.getPenaltyChargesPortion(currency));
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

            feeChargesPortion = currentInstallment.waiveFeeChargesComponent(transactionDate,
                    loanTransaction.getFeeChargesPortion(currency));
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

        } else if (loanTransaction.isInterestWaiver()) {
            interestPortion = currentInstallment.waiveInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,
                    selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,
                    selfSharePenalAmount.getAmount(),partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        } else if (loanTransaction.isChargePayment()) {
            if (loanTransaction.isPenaltyPayment()) {
                penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
            } else {
                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
            }
        } else {
            // 1. pay of principal before interest.

            principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

            currentInstallment.paySelfPrincipalComponent(transactionDate, transactionSelfAmountRemaining);

            currentInstallment.payPartnerPrincipalComponent(transactionDate, transactionPartnerAmountRemaining);

            interestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            currentInstallment.paySelfInterestComponent(transactionDate, transactionSelfAmountRemaining);

           currentInstallment.payPartnerInterestComponent(transactionDate, transactionPartnerAmountRemaining);

            feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

            penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
        }

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,
                selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,
                selfSharePenalAmount.getAmount(),partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null,BigDecimal.ZERO));
        }
        return transactionAmountRemaining;
    }

    @Override
    protected void onLoanOverpayment(final LoanTransaction loanTransaction, final Money loanOverPaymentAmount) {}

    @Override
    protected Money handleRefundTransactionPaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
        Money principalPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money selfPrincipalPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money partnerPrincipalPortion = Money.zero(transactionAmountRemaining.getCurrency());

        Money interestPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money selfInterestPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money partnerInterestPortion = Money.zero(transactionAmountRemaining.getCurrency());

        Money feeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money selfFeeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money partnerFeeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());

        Money penaltyChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());

        Money selfSharePenalAmount = Money.zero(transactionAmountRemaining.getCurrency());
        Money partnerSharePenalAmount = Money.zero(transactionAmountRemaining.getCurrency());

        if (transactionAmountRemaining.isGreaterThanZero()) {
            penaltyChargesPortion = currentInstallment.unpayPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            feeChargesPortion = currentInstallment.unpayFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            interestPortion = currentInstallment.unpayInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
            selfInterestPortion = currentInstallment.unpaySelfInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(selfInterestPortion);
            partnerInterestPortion = currentInstallment.unpayPartnerInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(partnerInterestPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            principalPortion = currentInstallment.unpayPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
            selfPrincipalPortion = currentInstallment.unpaySelfPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(selfPrincipalPortion);
            principalPortion = currentInstallment.unpayPartnerPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
        }

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,
                partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfSharePenalAmount.getAmount(),
                partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null,BigDecimal.ZERO));
        }
        return transactionAmountRemaining;
    }
}
