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
 * Adhikar/RBI style {@link LoanRepaymentScheduleTransactionProcessor}.
 *

 *
 * Per RBI regulations, all interest must be paid (both current and overdue) before principal is paid.
 *
 * For example on a loan with two installments due (one current and one overdue) of 220 each (200 principal + 20
 * interest):
 *
 * Partial Payment of 40 20 Payment to interest on Installment #1 (200 principal remaining) 20 Payment to interest on
 * Installment #2 (200 principal remaining)
 */
public class RBILoanRepaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    /**
     * For creocore, early is defined as any date before the installment due date
     */
    @SuppressWarnings("unused")
    @Override
    protected boolean isTransactionInAdvanceOfInstallment(final int currentInstallmentIndex,
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate, final Money transactionAmount,final Money transactionSelfAmount,final Money transactionPartnerAmount) {

        final LoanRepaymentScheduleInstallment currentInstallment = installments.get(currentInstallmentIndex);

        return transactionDate.isBefore(currentInstallment.getDueDate());
    }

    /**
     * For early/'in advance' repayments, pays off principal component only.
     */
    @SuppressWarnings("unused")
    @Override
    protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
                                                                         final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
                                                                         final LocalDate transactionDate, final Money paymentInAdvance,
                                                                         List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Money selfPaymentInAdvance,
                                                                         Money partnerPaymentInAdvance, List<CollectionReport> collectionReports, LoanHistoryRepo loanHistoryRepo,
                                                                         List<LoanTransaction> listOfLoanTransaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLoanTransction) {

        return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, paymentInAdvance,
                transactionMappings,selfPaymentInAdvance,partnerPaymentInAdvance,collectionReports,loanHistoryRepo,listOfLoanTransaction,lateLoantransaction,advanceLoanTransction);
    }

    /**
     * For late repayments, pay off in the same way as on-time payments, interest first then principal.
     */
    @Override
    protected Money handleTransactionThatIsALateRepaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
            final Money transactionAmountUnprocessed, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,Money transactionSelfAmountUnprocessed,Money transactionPartnerAmountUnprocessed,
                                                                       List<CollectionReport> collectionReports,LoanHistoryRepo loanHistoryRepo,List<LoanTransaction> listOfLoanTransaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLoanTransaction) {

        // pay of overdue and current interest due given transaction date
        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
        Money transactionSelfAmountRemaining = transactionSelfAmountUnprocessed;
        Money transactionPartnerAmountRemaining = transactionPartnerAmountUnprocessed;

        Money interestWaivedPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money selfFeeChargesPortion = Money.zero(currency);
        Money partnerFeeChargesPortion = Money.zero(currency);

        final Money principalPortion = Money.zero(currency);
        final Money selfPrincipalPortion = Money.zero(currency);
        final Money partnerPrincipalPortion = Money.zero(currency);

        final Money interestPortion = Money.zero(currency);
        final Money selfInterestPortion = Money.zero(currency);
        final Money partnerInterestPortion = Money.zero(currency);

        Money penaltyChargesPortion = Money.zero(currency);

        Money selfSharePenalAmount = Money.zero(currency);
        Money partnerSharePenalAmount = Money.zero(currency);

        if (loanTransaction.isInterestWaiver()) {
            interestWaivedPortion = currentInstallment.waiveInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestWaivedPortion);


            
			 loanTransaction.updateComponents(principalPortion, interestWaivedPortion, feeChargesPortion,
                     penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,
                     partnerInterestPortion,selfSharePenalAmount.getAmount(),partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
            if (interestWaivedPortion.isGreaterThanZero()) {
                transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                        principalPortion, interestWaivedPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null, BigDecimal.ZERO));
            }
        } else if (loanTransaction.isChargePayment()) {


            if (loanTransaction.isPenaltyPayment()) {
                penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
            } else {
                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
            }
            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,
                    selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,
                    selfSharePenalAmount.getAmount(),partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
            if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
                transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                        principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null,BigDecimal.ZERO));
            }
        } else {

            final LoanRepaymentScheduleInstallment currentInstallmentBasedOnTransactionDate = nearestInstallment(
                    loanTransaction.getTransactionDate(), installments);

            for (final LoanRepaymentScheduleInstallment installment : installments) {
                if ((installment.isInterestDue(currency) || installment.getFeeChargesOutstanding(currency).isGreaterThanZero()
                        || installment.getPenaltyChargesOutstanding(currency).isGreaterThanZero())
                        && (installment.isOverdueOn(loanTransaction.getTransactionDate()) || installment.getInstallmentNumber()
                                .equals(currentInstallmentBasedOnTransactionDate.getInstallmentNumber()))) {
                    penaltyChargesPortion = installment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

                    feeChargesPortion = installment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

                   installment.payInterestComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

                    installment.paySelfInterestComponent(transactionDate, transactionSelfAmountRemaining);

                   installment.payPartnerInterestComponent(transactionDate, transactionPartnerAmountRemaining);

                   loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion,
                            penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,
                            partnerInterestPortion,selfSharePenalAmount.getAmount(),partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());

                    if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
                        transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, installment,
                                principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null,BigDecimal.ZERO));
                    }
                }
            }

            // With whatever is remaining, pay off principal components of
            // installments
            for (final LoanRepaymentScheduleInstallment installment : installments) {
                if (installment.isPrincipalNotCompleted(currency) && transactionAmountRemaining.isGreaterThanZero()) {
                      installment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

                    installment.paySelfPrincipalComponent(transactionDate, transactionSelfAmountRemaining);

                    installment.payPartnerPrincipalComponent(transactionDate, transactionPartnerAmountRemaining);

                    loanTransaction.updateComponents(principalPortion, interestPortion, Money.zero(currency), Money.zero(currency),
                            selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfSharePenalAmount.getAmount(),
                            partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
                    boolean isMappingUpdated = false;
                    for (LoanTransactionToRepaymentScheduleMapping repaymentScheduleMapping : transactionMappings) {
                        if (repaymentScheduleMapping.getLoanRepaymentScheduleInstallment().getDueDate().equals(installment.getDueDate())) {
                            repaymentScheduleMapping.updateComponents(principalPortion, principalPortion.zero(), selfPrincipalPortion,
                                    partnerPrincipalPortion);
                            isMappingUpdated = true;
                            break;
                        }
                    }
                    if (!isMappingUpdated && principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion)
                            .isGreaterThanZero()) {
                        transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, installment,
                                principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null,BigDecimal.ZERO));
                    }
                }
            }
        }

        return transactionAmountRemaining;
    }

    private LoanRepaymentScheduleInstallment nearestInstallment(final LocalDate transactionDate,
            final List<LoanRepaymentScheduleInstallment> installments) {

        LoanRepaymentScheduleInstallment nearest = installments.get(0);
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.getDueDate().isBefore(transactionDate) || installment.getDueDate().isEqual(transactionDate)) {
                nearest = installment;
            } else if (installment.getDueDate().isAfter(transactionDate)) {
                break;
            }
        }
        return nearest;
    }

    /**
     * For normal on-time repayments, pays off interest first, then principal.
     */
    @Override
    protected Money handleTransactionThatIsOnTimePaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
                                                                      final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
                                                                      final List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, final Money transactionSelfAmountUnprocessed,
                                                                      final Money transactionPartnerAmountUnprocessed, final List<CollectionReport> collectionReports,LoanHistoryRepo loanHistoryRepo,List<LoanTransaction> listOfLoanTransaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLoanTransaction) {

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
        } else if (loanTransaction.isChargePayment()) {
            if (loanTransaction.isPenaltyPayment()) {
                penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
            } else {
                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
            }
        } else {

            penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

            feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

            interestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            currentInstallment.paySelfInterestComponent(transactionDate, transactionSelfAmountRemaining);

            currentInstallment.payPartnerInterestComponent(transactionDate, transactionPartnerAmountRemaining);

            principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

           currentInstallment.paySelfPrincipalComponent(transactionDate, transactionSelfAmountRemaining);

            currentInstallment.payPartnerPrincipalComponent(transactionDate, transactionPartnerAmountRemaining);
        }

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,
                selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfSharePenalAmount.getAmount(),
                partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null,BigDecimal.ZERO));
        }
        return transactionAmountRemaining;
    }

    @SuppressWarnings("unused")
    @Override
    protected void onLoanOverpayment(final LoanTransaction loanTransaction, final Money loanOverPaymentAmount) {
        // dont do anything for with loan over-payment
    }

    @Override
    public boolean isInterestFirstRepaymentScheduleTransactionProcessor() {
        return true;
    }

    @Override
    protected Money handleRefundTransactionPaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
            final List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        // final MonetaryCurrency currency =
        // transactionAmountUnprocessed.getCurrency();
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

        Money selfPenalShareAmount = Money.zero(transactionAmountRemaining.getCurrency());
        Money partnerSharePenalAmount = Money.zero(transactionAmountRemaining.getCurrency());

        if (transactionAmountRemaining.isGreaterThanZero()) {
            principalPortion = currentInstallment.unpayPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

            selfPrincipalPortion = currentInstallment.unpaySelfPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(selfPrincipalPortion);

            partnerPrincipalPortion = currentInstallment.unpayPartnerPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(partnerPrincipalPortion);
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
            feeChargesPortion = currentInstallment.unpayFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            penaltyChargesPortion = currentInstallment.unpayPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
        }

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,
                partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfPenalShareAmount.getAmount(),
                partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null,BigDecimal.ZERO));
        }
        return transactionAmountRemaining;
    }
}
