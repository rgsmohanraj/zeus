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
 * This {@link LoanRepaymentScheduleTransactionProcessor} defaults to having the payment order of Interest first, then
 * principal, penalties and fees.
 */
public class EarlyPaymentLoanRepaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    /**
     * For early/'in advance' repayments, pay off in the same way as on-time payments, interest first, principal,
     * penalties and charges.
     */
    @SuppressWarnings("unused")
    @Override
    protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
                                                                         final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
                                                                         final LocalDate transactionDate, final Money paymentInAdvance,
                                                                         final List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Money selfPaymentInAdvance, Money partnerPaymentInAdvance,
                                                                         List<CollectionReport> collectionReports, LoanHistoryRepo loanHistoryRepo,List<LoanTransaction> listOfLoanTransaction,LoanTransaction lateLoanTransaction,LoanTransaction advanceLoanTransction) {

        final MonetaryCurrency currency = paymentInAdvance.getCurrency();
        Money transactionAmountRemaining = paymentInAdvance;
        Money transactionSelfAmountRemaining = selfPaymentInAdvance;
        Money transactionPartnerAmountRemaining = partnerPaymentInAdvance;

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
            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,
                    partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfSharePenalAmount.getAmount(),
                    partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        } else {

            // Only allocate to principal:
            principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
			
			 currentInstallment.paySelfPrincipalComponent(transactionDate, transactionSelfAmountRemaining);

            currentInstallment.payPartnerPrincipalComponent(transactionDate, transactionPartnerAmountRemaining);

            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,
                    selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,
                    selfSharePenalAmount.getAmount(),partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        }
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null, BigDecimal.ZERO));
        }
        return transactionAmountRemaining;

    }

    /**
     * For late repayments, pay off in the same way as on-time payments, interest first then principal.
     */
    @SuppressWarnings("unused")
    @Override
    protected Money handleTransactionThatIsALateRepaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
                                                                       final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
                                                                       final Money transactionAmountUnprocessed, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,
                                                                       Money transactionSelfAmountUnprocessed, Money transactionPartnerAmountUnprocessed, final List<CollectionReport> collectionReports,
                                                                       LoanHistoryRepo loanHistoryRepo,List<LoanTransaction> overDueLoanTransaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLoanTransaction) {

        return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, transactionAmountUnprocessed,
                transactionMappings,transactionSelfAmountUnprocessed,transactionPartnerAmountUnprocessed,collectionReports,loanHistoryRepo,overDueLoanTransaction,lateLoantransaction,advanceLoanTransaction);
    }

    /**
     * For normal on-time repayments, pays off interest first, then principal.
     */
    @Override
    protected Money handleTransactionThatIsOnTimePaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
            final List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,final Money transactionSelfAmountUnprocessed,
                                                                      final Money transactionPartnerAmountUnprocessed,final List<CollectionReport> collectionReports,LoanHistoryRepo loanHistoryRepo,
                                                                      List<LoanTransaction> overDueLoanTransaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLOanTransaction) {

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
            // zero this type of transaction and ignore it for now.
            transactionAmountRemaining = Money.zero(currency);
        } else if (loanTransaction.isInterestWaiver()) {
            interestPortion = currentInstallment.waiveInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,
                    selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfSharePenalAmount.getAmount(),
                    partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
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
        } else {
            interestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            currentInstallment.paySelfInterestComponent(transactionDate, transactionSelfAmountRemaining);
            transactionSelfAmountRemaining = transactionSelfAmountRemaining.minus(selfInterestPortion);

            currentInstallment.payPartnerInterestComponent(transactionDate, transactionPartnerAmountRemaining);
            transactionPartnerAmountRemaining = transactionPartnerAmountRemaining.minus(partnerInterestPortion);

            principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

            currentInstallment.paySelfPrincipalComponent(transactionDate, transactionSelfAmountRemaining);

            currentInstallment.payPartnerPrincipalComponent(transactionDate, transactionPartnerAmountRemaining);

            penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

            feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,
                    partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfSharePenalAmount.getAmount(),
                    partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        }
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null,BigDecimal.ZERO));
        }
        return transactionAmountRemaining;
    }

    @Override
    protected Money handleRefundTransactionPaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {

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

        Money selfSharePenalAmount = Money.zero(transactionAmountRemaining.getCurrency());
        Money partnerSharePenalAmount = Money.zero(transactionAmountRemaining.getCurrency());

        if (transactionAmountRemaining.isGreaterThanZero()) {
            feeChargesPortion = currentInstallment.unpayFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            penaltyChargesPortion = currentInstallment.unpayPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            principalPortion = currentInstallment.unpayPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            selfPrincipalPortion = currentInstallment.unpaySelfPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(selfPrincipalPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
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

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,
                selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,
                selfSharePenalAmount.getAmount(),partnerSharePenalAmount.getAmount(),selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,null,null,BigDecimal.ZERO));
        }
        return transactionAmountRemaining;
    }

}
