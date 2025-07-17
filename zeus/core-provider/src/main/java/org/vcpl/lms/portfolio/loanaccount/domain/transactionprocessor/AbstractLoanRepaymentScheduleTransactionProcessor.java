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
package org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.data.LoanChargePaidDetail;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.impl.*;
import org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.impl.Collection;

/**
 * Abstract implementation of {@link LoanRepaymentScheduleTransactionProcessor} which is more convenient for concrete
 * implementations to extend.
 *
 * @see InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor
 *
 * @see HeavensFamilyLoanRepaymentScheduleTransactionProcessor
 * @see CreocoreLoanRepaymentScheduleTransactionProcessor
 */
public abstract class AbstractLoanRepaymentScheduleTransactionProcessor implements LoanRepaymentScheduleTransactionProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLoanRepaymentScheduleTransactionProcessor.class);

    /**
     * Provides support for passing all {@link LoanTransaction}'s so it will completely re-process the entire loan
     * schedule. This is required in cases where the {@link LoanTransaction} being processed is in the past and falls
     * before existing transactions or and adjustment is made to an existing in which case the entire loan schedule
     * needs to be re-processed.
     */

    @Override
    public ChangedTransactionDetail handleTransaction(final LocalDate disbursementDate,
                                                      final List<LoanTransaction> transactionsPostDisbursement, final MonetaryCurrency currency,
                                                      final List<LoanRepaymentScheduleInstallment> installments, List<LoanCharge> charges,
                                                      LoanHistoryRepo loanHistoryRepo) {
        if (charges != null) {
            for (final LoanCharge loanCharge : charges) {
                if (!loanCharge.isDueAtDisbursement() && !loanCharge.isAdhocChargeCharge()) {
                    loanCharge.resetPaidAmount(currency);
                }
            }
        }

        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            currentInstallment.restComponenets();
            currentInstallment.updateDerivedFields(currency, disbursementDate);
        }

        // re-process loan charges over repayment periods (picking up on waived
        // loan charges)
        final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(currency, disbursementDate, installments, charges);

        final ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        final List<LoanTransaction> transactionstoBeProcessed = new ArrayList<>();

        for (final LoanTransaction loanTransaction : transactionsPostDisbursement) {

            // Collection For Charge Payment
            if (loanTransaction.isChargePayment()) {
                List<LoanChargePaidDetail> chargePaidDetails = new ArrayList<>();
                final Set<LoanChargePaidBy> chargePaidBies = loanTransaction.getLoanChargesPaid();
                final List<LoanCharge> transferCharges = new ArrayList<>();
                for (final LoanChargePaidBy chargePaidBy : chargePaidBies) {
                    LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                    transferCharges.add(loanCharge);
                    if (loanCharge.isInstalmentFee()) {
                        chargePaidDetails.addAll(loanCharge.fetchRepaymentInstallment(currency));
                    }
                }
                LocalDate startDate = disbursementDate;
                for (final LoanRepaymentScheduleInstallment installment : installments) {
                    for (final LoanCharge loanCharge : transferCharges) {
                        if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(startDate, installment.getDueDate())) {
                            Money amountForProcess = loanCharge.getAmount(currency);
                            if (amountForProcess.isGreaterThan(loanTransaction.getAmount(currency))) {
                                amountForProcess = loanTransaction.getAmount(currency);
                            }
                            LoanChargePaidDetail chargePaidDetail = new LoanChargePaidDetail(amountForProcess, installment,
                                    loanCharge.isFeeCharge());
                            chargePaidDetails.add(chargePaidDetail);
                            break;
                        }
                    }
                    startDate = installment.getDueDate();
                }
                loanTransaction.resetDerivedComponents();
                Money unprocessed = loanTransaction.getAmount(currency);
                for (LoanChargePaidDetail chargePaidDetail : chargePaidDetails) {
                    final List<LoanRepaymentScheduleInstallment> processInstallments = new ArrayList<>(1);
                    processInstallments.add(chargePaidDetail.getInstallment());
                    Money processAmt = chargePaidDetail.getAmount();
                    if (processAmt.isGreaterThan(unprocessed)) {
                        processAmt = unprocessed;
                    }
                    unprocessed = handleTransactionAndCharges(loanTransaction, currency, processInstallments, transferCharges, processAmt,
                            chargePaidDetail.isFeeCharge(),loanHistoryRepo);
                    if (!unprocessed.isGreaterThanZero()) {
                        break;
                    }
                }

                if (unprocessed.isGreaterThanZero()) {
                    onLoanOverpayment(loanTransaction, unprocessed);
                    loanTransaction.updateOverPayments(unprocessed);
                }

            } else {
                transactionstoBeProcessed.add(loanTransaction);
            }
        }

        for (final LoanTransaction loanTransaction : transactionstoBeProcessed) {

            // Refund the Loan Collection

            if (!loanTransaction.getTypeOf().equals(LoanTransactionType.REFUND_FOR_ACTIVE_LOAN)) {
                final Comparator<LoanRepaymentScheduleInstallment> byDate = new Comparator<LoanRepaymentScheduleInstallment>() {

                    @Override
                    public int compare(LoanRepaymentScheduleInstallment ord1, LoanRepaymentScheduleInstallment ord2) {
                        return ord1.getDueDate().compareTo(ord2.getDueDate());
                    }
                };
                Collections.sort(installments, byDate);
            }
            // Loan Make Repayment collection

            if (loanTransaction.isRepaymentType() || loanTransaction.isInterestWaiver() || loanTransaction.isRecoveryRepayment()) {
                // pass through for new transactions
                if (loanTransaction.getId() == null || (loanTransaction.getInterestPortion(currency).plus(loanTransaction.getPartnerInterestPortion(currency)).isEqualTo(Money.zero(currency)))) {
                    handleTransaction(loanTransaction, currency, installments, charges,loanHistoryRepo);
                    loanTransaction.adjustInterestComponent(currency);
                } else {
                    /**
                     * For existing transactions, check if the re-payment breakup (principal, interest, fees, penalties)
                     * has changed.<br>
                     **/

                    // added the existing transaction values in the newLoanTransaction
                    Long advanceAmountProceed =  loanTransaction.getAdvanceAmountprocessed();
                    BigDecimal advance =  loanTransaction.getAdvanceAmount();
                    final LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);
                    newLoanTransaction.setAdvanceAmount(advance);
                    newLoanTransaction.setAdvanceAmountprocessed(advanceAmountProceed);


                    // Reset derived component of new loan transaction and
                    // re-process transaction
                    handleTransaction(newLoanTransaction, currency, installments, charges,loanHistoryRepo);
                    newLoanTransaction.adjustInterestComponent(currency);
                    /**
                     * Check if the transaction amounts have changed. If so, reverse the original transaction and update
                     * changedTransactionDetail accordingly
                     **/
                    if (LoanTransaction.transactionAmountsMatch(currency, loanTransaction, newLoanTransaction)) {
                        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(
                                newLoanTransaction.getLoanTransactionToRepaymentScheduleMappings());
                    } else {
                        loanTransaction.reverse();
                        loanTransaction.updateExternalId(null);
                        changedTransactionDetail.getNewTransactionMappings().put(loanTransaction.getId(), newLoanTransaction);
                    }
                }

            }

            // collection Written Off scenario

            else if (loanTransaction.isWriteOff()) {
                loanTransaction.resetDerivedComponents();
                handleWriteOff(loanTransaction, currency, installments);
            }


            // collection For Refund of Active Loans

            else if (loanTransaction.isRefundForActiveLoan()) {
                loanTransaction.resetDerivedComponents();

                handleRefund(loanTransaction, currency, installments, charges);
            }
        }
        return changedTransactionDetail;
    }

    /**
     * Provides support for processing the latest transaction (which should be latest transaction) against the loan
     * schedule.
     */
    @Override
    public void handleTransaction(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, final List<LoanCharge> charges,LoanHistoryRepo loanHistoryRepo) {

        final Money amountToProcess = null;
        final boolean isChargeAmount = false;
        handleTransaction(loanTransaction, currency, installments, charges, amountToProcess, isChargeAmount,loanHistoryRepo);

    }

    private void handleTransaction(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, final List<LoanCharge> charges, final Money chargeAmountToProcess,
            final boolean isFeeCharge,LoanHistoryRepo loanHistoryRepo) {

        Money transactionAmountUnprocessed = handleTransactionAndCharges(loanTransaction, currency, installments, charges,
                chargeAmountToProcess, isFeeCharge,loanHistoryRepo);

        if (transactionAmountUnprocessed.isGreaterThanZero()) {
            if (loanTransaction.isWaiver()) {
                loanTransaction.updateComponentsAndTotal(transactionAmountUnprocessed.zero(), transactionAmountUnprocessed.zero(),
                        transactionAmountUnprocessed.zero(), transactionAmountUnprocessed.zero(),transactionAmountUnprocessed.zero(),transactionAmountUnprocessed.zero(),
                        transactionAmountUnprocessed.zero(),transactionAmountUnprocessed.zero(),transactionAmountUnprocessed.zero(),transactionAmountUnprocessed.zero()
                        ,transactionAmountUnprocessed.zero().getAmount(),transactionAmountUnprocessed.zero().getAmount());
            } else {
                onLoanOverpayment(loanTransaction, transactionAmountUnprocessed);
                loanTransaction.updateOverPayments(transactionAmountUnprocessed);
            }
        }
    }

    private Money handleTransactionAndCharges(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, final List<LoanCharge> charges, final Money chargeAmountToProcess,
            final boolean isFeeCharge,LoanHistoryRepo loanHistoryRepo) {
        // to.
        if (loanTransaction.isRepaymentType() || loanTransaction.isInterestWaiver() || loanTransaction.isRecoveryRepayment()) {
            loanTransaction.resetDerivedComponents();
        }
        Money transactionAmountUnprocessed = processTransaction(loanTransaction, currency, installments, chargeAmountToProcess,loanHistoryRepo);

        final List<LoanCharge> loanFees = extractFeeCharges(charges);
        final List<LoanCharge> loanPenalties = extractPenaltyCharges(charges);
        Integer installmentNumber = null;
        if (loanTransaction.isChargePayment() && installments.size() == 1) {
            installmentNumber = installments.get(0).getInstallmentNumber();
        }

        if (loanTransaction.isNotWaiver() && !loanTransaction.isAccrual()) {
            Money feeCharges = loanTransaction.getFeeChargesPortion(currency);
            Money penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency);
            if (chargeAmountToProcess != null && feeCharges.isGreaterThan(chargeAmountToProcess)) {
                if (isFeeCharge) {
                    feeCharges = chargeAmountToProcess;
                } else {
                    penaltyCharges = chargeAmountToProcess;
                }
            }
            if (feeCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, feeCharges, loanFees, installmentNumber);
            }

            if (penaltyCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, penaltyCharges, loanPenalties, installmentNumber);
            }
        }
        return transactionAmountUnprocessed;
    }

    private Money processTransaction(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
                                     final List<LoanRepaymentScheduleInstallment> installments, Money amountToProcess, LoanHistoryRepo historyRepo) {
        int installmentIndex = 0;
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
        List<CollectionReport> collectionReports = new ArrayList<>();

        List<LoanTransaction> listOfTransaction = new ArrayList<>();
        LoanTransaction lateloanTransaction =  LoanTransaction.copyTransactionProperties(loanTransaction);

        LoanTransaction advanceLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);

        advanceLoanTransaction.setAmount(BigDecimal.ZERO);

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        Money transactionSelfAmountUnprocessed=loanTransaction.getSelfDue(currency);
        Money transactionPartnerAmountUnprocessed=loanTransaction.getPartnerDue(currency);
        Money  transactionAmountUnprocessed = loanTransaction.getAmount(currency);
        if (amountToProcess != null) {
            transactionAmountUnprocessed = amountToProcess;
        }
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            if (transactionAmountUnprocessed.isGreaterThanZero()) {
                if (currentInstallment.isNotFullyPaidOff()) {

                    // is this transaction early/late/on-time with respect to
                    // the
                    // current installment?
                    if (isTransactionInAdvanceOfInstallment(installmentIndex, installments, transactionDate,
                            transactionAmountUnprocessed,transactionSelfAmountUnprocessed,transactionPartnerAmountUnprocessed)){
                        transactionAmountUnprocessed = handleTransactionThatIsPaymentInAdvanceOfInstallment(currentInstallment,
                                installments, loanTransaction, transactionDate, transactionAmountUnprocessed, transactionMappings,transactionSelfAmountUnprocessed,
                                transactionPartnerAmountUnprocessed,collectionReports,historyRepo,listOfTransaction,lateloanTransaction,advanceLoanTransaction);

                    } else if (isTransactionALateRepaymentOnInstallment(installmentIndex, installments,
                            loanTransaction.getTransactionDate())) {
                        // does this result in a late payment of existing
                        // installment?
                        transactionAmountUnprocessed = handleTransactionThatIsALateRepaymentOfInstallment(currentInstallment, installments,
                                loanTransaction, transactionAmountUnprocessed, transactionMappings,transactionSelfAmountUnprocessed,
                                transactionPartnerAmountUnprocessed,collectionReports,historyRepo,listOfTransaction,lateloanTransaction,advanceLoanTransaction);
                        currentInstallment.updateAdvanceAmount(BigDecimal.ZERO);
                    } else {
                        // standard transaction
                        transactionAmountUnprocessed = handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment,
                                loanTransaction, transactionAmountUnprocessed, transactionMappings,transactionSelfAmountUnprocessed,
                                transactionPartnerAmountUnprocessed,collectionReports,historyRepo,listOfTransaction,lateloanTransaction,advanceLoanTransaction);
                        currentInstallment.updateAdvanceAmount(BigDecimal.ZERO);
                    }
                    LOG.info("LOAN {} paid for Installment {}  ",loanTransaction.getLoan().getId(),currentInstallment.getInstallmentNumber());
                }
            }
            installmentIndex++;
        }
        installments.stream().forEach(loanRepaymentScheduleInstallment -> {
            Integer currentDpd = Objects.isNull(loanRepaymentScheduleInstallment.getDaysPastDue())
                    ? 0 : loanRepaymentScheduleInstallment.getDaysPastDue();
            if (loanRepaymentScheduleInstallment.getTotalOutstanding(loanTransaction.getLoan().getCurrency()).isZero()
                    && !currentDpd.equals(0)) {
                if((loanRepaymentScheduleInstallment.getObligationsMetOnDate().equals(loanRepaymentScheduleInstallment.getDueDate())
                        || loanRepaymentScheduleInstallment.getObligationsMetOnDate().isAfter(loanRepaymentScheduleInstallment.getDueDate()))) {
                    int days = Math.toIntExact(ChronoUnit.DAYS.between(loanRepaymentScheduleInstallment.getDueDate(),
                            loanRepaymentScheduleInstallment.getObligationsMetOnDate()));
                    loanRepaymentScheduleInstallment.setDpdHistory(days);
                    loanRepaymentScheduleInstallment.setDaysPastDue(0);
                } else if(loanRepaymentScheduleInstallment.getObligationsMetOnDate().isBefore(loanRepaymentScheduleInstallment.getDueDate())) {
                    loanRepaymentScheduleInstallment.setDpdHistory(0);
                    loanRepaymentScheduleInstallment.setDaysPastDue(0);
                }
            }
        });



        /**
         * Creataing the list of loan Transaction For Bulk payment
         * For backdated cases based on the value date and Transaction Date and Stroring
         * Each Transaction
         */
        if(lateloanTransaction.getInterestPortion(currency).plus(lateloanTransaction.getPrincipalPortion(currency)).getAmount().doubleValue()>0){
            listOfTransaction.add(lateloanTransaction);
        }

        listOfTransaction.forEach(
                transaction->loanTransaction.getLoan().addLoanTransaction(transaction));


        /**
         * Adding the Advance LoanTransaction For Backdated cases at middle of installment and starting of the installment
         */
        advanceLoanTransaction.setValueDate(loanTransaction.getDateOf());

        if(((advanceLoanTransaction.getInterestPortion(currency).plus(advanceLoanTransaction.getPrincipalPortion(currency)).getAmount().doubleValue()) > 0 &&
                !advanceLoanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.PROCESSEDADVANCE.getValue()))
                || Objects.nonNull(advanceLoanTransaction.getAdvanceAmountprocessed())){
            loanTransaction.getLoan().getLoanTransactions().add(advanceLoanTransaction);
        }
        return transactionAmountUnprocessed;
    }
    private List<LoanCharge> extractFeeCharges(final List<LoanCharge> loanCharges) {
        final List<LoanCharge> feeCharges = new ArrayList<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge()) {
                feeCharges.add(loanCharge);
            }
        }
        return feeCharges;
    }

    private List<LoanCharge> extractPenaltyCharges(final List<LoanCharge> loanCharges) {
        final List<LoanCharge> penaltyCharges = new ArrayList<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                penaltyCharges.add(loanCharge);
            }
        }
        return penaltyCharges;
    }

    private void updateChargesPaidAmountBy(final LoanTransaction loanTransaction, final Money feeCharges, final List<LoanCharge> charges,
            final Integer installmentNumber) {

        Money amountRemaining = feeCharges;
        while (amountRemaining.isGreaterThanZero()) {
            final LoanCharge unpaidCharge = findEarliestUnpaidChargeFromUnOrderedSet(charges, feeCharges.getCurrency());
            Money feeAmount = feeCharges.zero();
            if (loanTransaction.isChargePayment()) {
                feeAmount = feeCharges;
            }
            if (unpaidCharge == null) {
                break;
                // All are trache charges
            }
            final Money amountPaidTowardsCharge = unpaidCharge.updatePaidAmountBy(amountRemaining, installmentNumber, feeAmount);
            if (!amountPaidTowardsCharge.isZero()) {
                Set<LoanChargePaidBy> chargesPaidBies = loanTransaction.getLoanChargesPaid();
                if (loanTransaction.isChargePayment()) {
                    for (final LoanChargePaidBy chargePaidBy : chargesPaidBies) {
                        LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                        if (loanCharge.getId().equals(unpaidCharge.getId())) {
                            chargePaidBy.setAmount(amountPaidTowardsCharge.getAmount());
                        }
                    }
                } else {
                    final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(loanTransaction, unpaidCharge,
                            amountPaidTowardsCharge.getAmount(), installmentNumber);
                    chargesPaidBies.add(loanChargePaidBy);
                }
                amountRemaining = amountRemaining.minus(amountPaidTowardsCharge);
            }
        }

    }

    private LoanCharge findEarliestUnpaidChargeFromUnOrderedSet(final List<LoanCharge> charges, final MonetaryCurrency currency) {
        LoanCharge earliestUnpaidCharge = null;
        LoanCharge installemntCharge = null;
        LoanInstallmentCharge chargePerInstallment = null;
        for (final LoanCharge loanCharge : charges) {
            if (loanCharge.getAmountOutstanding(currency).isGreaterThanZero() && !loanCharge.isDueAtDisbursement()) {
                if (loanCharge.isInstalmentFee()) {
                    LoanInstallmentCharge unpaidLoanChargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
                    if (chargePerInstallment == null || chargePerInstallment.getRepaymentInstallment().getDueDate()
                            .isAfter(unpaidLoanChargePerInstallment.getRepaymentInstallment().getDueDate())) {
                        installemntCharge = loanCharge;
                        chargePerInstallment = unpaidLoanChargePerInstallment;
                    }
                } else if (earliestUnpaidCharge == null || loanCharge.getDueLocalDate().isBefore(earliestUnpaidCharge.getDueLocalDate())) {
                    earliestUnpaidCharge = loanCharge;
                }
            }
        }
        if (earliestUnpaidCharge == null || (chargePerInstallment != null
                && earliestUnpaidCharge.getDueLocalDate().isAfter(chargePerInstallment.getRepaymentInstallment().getDueDate()))) {
            earliestUnpaidCharge = installemntCharge;
        }

        return earliestUnpaidCharge;
    }

    @Override
    public void handleWriteOff(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        Money principalPortion = Money.zero(currency);
        Money selfPrincipalPortion = Money.zero(currency);
        Money partnerPrincipalPortion = Money.zero(currency);

        Money interestPortion = Money.zero(currency);
        Money selfInterestPortion = Money.zero(currency);
        Money partnerInterestPortion = Money.zero(currency);

        Money feeChargesPortion = Money.zero(currency);
        Money penaltychargesPortion = Money.zero(currency);
        Money selfSharePenalAmount = Money.zero(currency);
        Money partnerSharePenalAmount = Money.zero(currency);
        Money selfFeeChargesPortion = Money.zero(currency);
        Money partnerFeeChargesPortion = Money.zero(currency);

        // determine how much is written off in total and breakdown for
        // principal, interest and charges
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {

            if (currentInstallment.isNotFullyPaidOff()) {
                principalPortion = principalPortion.plus(currentInstallment.writeOffOutstandingPrincipal(transactionDate, currency));
                selfPrincipalPortion = selfPrincipalPortion.plus(currentInstallment.writeOffOutstandingPrincipal(transactionDate, currency));
                partnerPrincipalPortion = partnerPrincipalPortion.plus(currentInstallment.writeOffOutstandingPrincipal(transactionDate, currency));

                interestPortion = interestPortion.plus(currentInstallment.writeOffOutstandingInterest(transactionDate, currency));
                selfInterestPortion = selfInterestPortion.plus(currentInstallment.writeOffOutstandingInterest(transactionDate, currency));
                partnerInterestPortion = partnerInterestPortion.plus(currentInstallment.writeOffOutstandingInterest(transactionDate, currency));

                feeChargesPortion = feeChargesPortion.plus(currentInstallment.writeOffOutstandingFeeCharges(transactionDate, currency));
                penaltychargesPortion = penaltychargesPortion
                        .plus(currentInstallment.writeOffOutstandingPenaltyCharges(transactionDate, currency));
            }
        }

        loanTransaction.updateComponentsAndTotal(principalPortion, interestPortion, feeChargesPortion, penaltychargesPortion,selfPrincipalPortion,
                partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfSharePenalAmount,partnerSharePenalAmount,selfFeeChargesPortion.getAmount(),partnerFeeChargesPortion.getAmount());
    }

    // abstract interface
    /**
     * This method is responsible for checking if the current transaction is 'an advance/early payment' based on the
     * details passed through.
     *
     * Default implementation simply processes transactions as 'Late' if the transaction date is after the installment
     * due date.
     */
    protected boolean isTransactionALateRepaymentOnInstallment(final int installmentIndex,
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate) {

        final LoanRepaymentScheduleInstallment currentInstallment = installments.get(installmentIndex);

        return transactionDate.isAfter(currentInstallment.getDueDate());
    }

    /**
     * For late repayments, how should components of installment be paid off
     *
     * @param transactionMappings
     *            TODO
     */
    protected abstract Money handleTransactionThatIsALateRepaymentOfInstallment(LoanRepaymentScheduleInstallment currentInstallment,
            List<LoanRepaymentScheduleInstallment> installments, LoanTransaction loanTransaction, Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,Money transactionSelfAmountUnprocessed,Money transactionPartnerAmountUnprocessed,List<CollectionReport> collectionReports,
                                                                                LoanHistoryRepo loanHistoryRepo,List<LoanTransaction> listOfLOanTransaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLoanTransaction);

    /**
     * This method is responsible for checking if the current transaction is 'an advance/early payment' based on the
     * details passed through.
     *
     * Default implementation is check transaction date is before installment due date.
     */
    protected boolean isTransactionInAdvanceOfInstallment(final int currentInstallmentIndex,
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate,
            @SuppressWarnings("unused") final Money transactionAmount,final Money transactionSelfAmount,final Money transactionPartnerAmount) {

        final LoanRepaymentScheduleInstallment currentInstallment = installments.get(currentInstallmentIndex);

        return transactionDate.isBefore(currentInstallment.getDueDate());
    }

    /**
     * For early/'in advance' repayments.
     *
     * @param transactionMappings
     *            TODO
     */
    protected abstract Money handleTransactionThatIsPaymentInAdvanceOfInstallment(LoanRepaymentScheduleInstallment currentInstallment,
            List<LoanRepaymentScheduleInstallment> installments, LoanTransaction loanTransaction, LocalDate transactionDate,
            Money paymentInAdvance, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,Money selfPaymentInAdvance,
                                                                                  Money partnerPaymentInAdvance,List<CollectionReport> collectionReports,
                                                                                  LoanHistoryRepo loanHistoryRepo,List<LoanTransaction> listOfTransaction,
                                                                                  LoanTransaction lateLoanTransaction,LoanTransaction advanceLoanTransaction);

    /**
     * For normal on-time repayments.
     *
     * @param transactionMappings
     *            TODO
     */
    protected abstract Money handleTransactionThatIsOnTimePaymentOfInstallment(LoanRepaymentScheduleInstallment currentInstallment,
            LoanTransaction loanTransaction, Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,final Money transactionSelfAmountUnprocessed,
                                                                               final Money transactionPartnerAmountUnprocessed,List<CollectionReport> collectionReports,LoanHistoryRepo loanHistoryRepo,
                                                                               List<LoanTransaction> overDueLoanTransaction,LoanTransaction lateLoantransaction,LoanTransaction advanceLoanTransaction);

    /**
     * Invoked when a transaction results in an over-payment of the full loan.
     *
     * transaction amount is greater than the total expected principal and interest of the loan.
     */
    @SuppressWarnings("unused")
    protected void onLoanOverpayment(final LoanTransaction loanTransaction, final Money loanOverPaymentAmount) {
        // empty implementation by default.
    }

    @Override
    public Money handleRepaymentSchedule(final List<LoanTransaction> transactionsPostDisbursement, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments,LoanHistoryRepo loanHistoryRepo) {
        Money unProcessed = Money.zero(currency);
        for (final LoanTransaction loanTransaction : transactionsPostDisbursement) {
            Money amountToProcess = null;
            if (loanTransaction.isRepaymentType() || loanTransaction.isInterestWaiver() || loanTransaction.isRecoveryRepayment()) {
                loanTransaction.resetDerivedComponents();
            }
            if (loanTransaction.isInterestWaiver()) {
                processTransaction(loanTransaction, currency, installments, amountToProcess,loanHistoryRepo);
            } else {
                unProcessed = processTransaction(loanTransaction, currency, installments, amountToProcess,loanHistoryRepo);
            }
        }
        return unProcessed;
    }

    @Override
    public boolean isInterestFirstRepaymentScheduleTransactionProcessor() {
        return false;
    }

    @Override
    public void handleRefund(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, final List<LoanCharge> charges) {
        // TODO Auto-generated method stub
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
        final Comparator<LoanRepaymentScheduleInstallment> byDate = new Comparator<LoanRepaymentScheduleInstallment>() {

            @Override
            public int compare(LoanRepaymentScheduleInstallment ord1, LoanRepaymentScheduleInstallment ord2) {
                return ord1.getDueDate().compareTo(ord2.getDueDate());
            }
        };
        Collections.sort(installments, Collections.reverseOrder(byDate));
        Money transactionAmountUnprocessed = loanTransaction.getAmount(currency);

        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            Money outstanding = currentInstallment.getTotalOutstanding(currency);
            Money due = currentInstallment.getDue(currency);

            if (outstanding.isLessThan(due)) {
                transactionAmountUnprocessed = handleRefundTransactionPaymentOfInstallment(currentInstallment, loanTransaction,
                        transactionAmountUnprocessed, transactionMappings);

            }

            if (transactionAmountUnprocessed.isZero()) {
                break;
            }

        }

        final List<LoanCharge> loanFees = extractFeeCharges(charges);
        final List<LoanCharge> loanPenalties = extractPenaltyCharges(charges);
        Integer installmentNumber = null;

        final Money feeCharges = loanTransaction.getFeeChargesPortion(currency);
        if (feeCharges.isGreaterThanZero()) {
            undoChargesPaidAmountBy(loanTransaction, feeCharges, loanFees, installmentNumber);
        }

        final Money penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency);
        if (penaltyCharges.isGreaterThanZero()) {
            undoChargesPaidAmountBy(loanTransaction, penaltyCharges, loanPenalties, installmentNumber);
        }
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);
    }

    /**
     * Invoked when a there is a refund of an active loan or undo of an active loan
     *
     * Undoes principal, interest, fees and charges of this transaction based on the repayment strategy
     *
     * @param transactionMappings
     *            TODO
     *
     */
    protected abstract Money handleRefundTransactionPaymentOfInstallment(LoanRepaymentScheduleInstallment currentInstallment,
            LoanTransaction loanTransaction, Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings);

    private void undoChargesPaidAmountBy(final LoanTransaction loanTransaction, final Money feeCharges, final List<LoanCharge> charges,
            final Integer installmentNumber) {

        Money amountRemaining = feeCharges;
        while (amountRemaining.isGreaterThanZero()) {
            final LoanCharge paidCharge = findLatestPaidChargeFromUnOrderedSet(charges, feeCharges.getCurrency());

            if (paidCharge != null) {
                Money feeAmount = feeCharges.zero();

                final Money amountDeductedTowardsCharge = paidCharge.undoPaidOrPartiallyAmountBy(amountRemaining, installmentNumber,
                        feeAmount);
                if (amountDeductedTowardsCharge.isGreaterThanZero()) {

                    final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(loanTransaction, paidCharge,
                            amountDeductedTowardsCharge.getAmount().multiply(new BigDecimal(-1)), null);
                    loanTransaction.getLoanChargesPaid().add(loanChargePaidBy);

                    amountRemaining = amountRemaining.minus(amountDeductedTowardsCharge);
                }
            }
        }

    }

    private LoanCharge findLatestPaidChargeFromUnOrderedSet(final List<LoanCharge> charges, MonetaryCurrency currency) {
        LoanCharge latestPaidCharge = null;
        LoanCharge installemntCharge = null;
        LoanInstallmentCharge chargePerInstallment = null;
        for (final LoanCharge loanCharge : charges) {
            boolean isPaidOrPartiallyPaid = loanCharge.isPaidOrPartiallyPaid(currency);
            if (isPaidOrPartiallyPaid && !loanCharge.isDueAtDisbursement()) {
                if (loanCharge.isInstalmentFee()) {
                    LoanInstallmentCharge paidLoanChargePerInstallment = loanCharge
                            .getLastPaidOrPartiallyPaidInstallmentLoanCharge(currency);
                    if (chargePerInstallment == null
                            || (paidLoanChargePerInstallment != null && chargePerInstallment.getRepaymentInstallment().getDueDate()
                                    .isBefore(paidLoanChargePerInstallment.getRepaymentInstallment().getDueDate()))) {
                        installemntCharge = loanCharge;
                        chargePerInstallment = paidLoanChargePerInstallment;
                    }
                } else if (latestPaidCharge == null || (loanCharge.isPaidOrPartiallyPaid(currency)
                        && loanCharge.getDueLocalDate().isAfter(latestPaidCharge.getDueLocalDate()))) {
                    latestPaidCharge = loanCharge;
                }
            }
        }
        if (latestPaidCharge == null || (chargePerInstallment != null
                && latestPaidCharge.getDueLocalDate().isAfter(chargePerInstallment.getRepaymentInstallment().getDueDate()))) {
            latestPaidCharge = installemntCharge;
        }

        return latestPaidCharge;
    }


    @Override
    public void processTransactionsFromDerivedFields(List<LoanTransaction> transactionsPostDisbursement, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, final List<LoanCharge> charges) {
        for (final LoanTransaction loanTransaction : transactionsPostDisbursement) {
            if (!loanTransaction.isAccrualTransaction()) {
                processTransactionFromDerivedFields(loanTransaction, currency, installments, charges);
            }
        }
    }

    private void processTransactionFromDerivedFields(final LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, final List<LoanCharge> charges) {
        Money principal = loanTransaction.getPrincipalPortion(currency);
        Money selfPrincipal = loanTransaction.getSelfPrincipalPortion(currency);
        Money partnerPrincipal = loanTransaction.getPartnerPrincipalPortion(currency);

        Money interest = loanTransaction.getInterestPortion(currency);
        Money selfInterestCharged = loanTransaction.getSelfInterestPortion(currency);
        Money partnerInterestCharged = loanTransaction.getPartnerInterestPortion(currency);
        if (loanTransaction.isInterestWaiver()) {
            interest = loanTransaction.getAmount(currency);
        }
        Money feeCharges = loanTransaction.getFeeChargesPortion(currency);
        Money penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency);
        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        if (principal.isGreaterThanZero() || interest.isGreaterThanZero() || feeCharges.isGreaterThanZero()
                || penaltyCharges.isGreaterThanZero()) {
            for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
                if (currentInstallment.isNotFullyPaidOff()) {
                    if (penaltyCharges.isGreaterThanZero()) {
                        Money penaltyChargesPortion = Money.zero(currency);
                        if (loanTransaction.isWaiver()) {
                            penaltyChargesPortion = currentInstallment.waivePenaltyChargesComponent(transactionDate, penaltyCharges);
                        } else {
                            penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, penaltyCharges);
                        }
                        penaltyCharges = penaltyCharges.minus(penaltyChargesPortion);
                    }

                    if (feeCharges.isGreaterThanZero()) {
                        Money feeChargesPortion = Money.zero(currency);
                        if (loanTransaction.isWaiver()) {
                            feeChargesPortion = currentInstallment.waiveFeeChargesComponent(transactionDate, feeCharges);
                        } else {
                            feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, feeCharges);
                        }
                        feeCharges = feeCharges.minus(feeChargesPortion);
                    }

                    if (interest.isGreaterThanZero()) {
                        Money interestPortion = Money.zero(currency);
                        if (loanTransaction.isWaiver()) {
                            interestPortion = currentInstallment.waiveInterestComponent(transactionDate, interest);
                        } else {
                            interestPortion = currentInstallment.payInterestComponent(transactionDate, interest);
                        }
                        interest = interest.minus(interestPortion);
                    }

                    if (selfInterestCharged.isGreaterThanZero()) {
                        Money selfInterestPortion = Money.zero(currency);
                        if (loanTransaction.isWaiver()) {
                            currentInstallment.waiveInterestComponent(transactionDate, selfInterestCharged);
                        } else {
                            currentInstallment.paySelfInterestComponent(transactionDate, selfInterestCharged);
                        }
                        selfInterestCharged = selfInterestCharged.minus(selfInterestPortion);
                    }

                    if (partnerInterestCharged.isGreaterThanZero()) {
                        Money partnerInterestPortion = Money.zero(currency);
                        if (loanTransaction.isWaiver()) {
                            partnerInterestPortion = currentInstallment.waiveInterestComponent(transactionDate, partnerInterestCharged);
                        } else {
                            currentInstallment.payPartnerInterestComponent(transactionDate, partnerInterestCharged);
                        }
                        partnerInterestCharged = partnerInterestCharged.minus(partnerInterestPortion);
                    }

                    if (principal.isGreaterThanZero()) {
                        Money principalPortion = currentInstallment.payPrincipalComponent(transactionDate, principal);
                        principal = principal.minus(principalPortion);
                    }

                   /* if (selfPrincipal.isGreaterThanZero()) {
                        Money selfPrincipalPortion= currentInstallment.paySelfPrincipalComponent(transactionDate, selfPrincipal);
                        selfPrincipal = selfPrincipal.minus(selfPrincipalPortion);
                    }*/

                    /*if (partnerPrincipal.isGreaterThanZero()) {
                        currentInstallment.payPartnerPrincipalComponent(transactionDate, partnerPrincipal);
                        partnerPrincipal = partnerPrincipal.minus(partnerPrincipalPortion);
                    }*/

                }
                if (!(principal.isGreaterThanZero() || interest.isGreaterThanZero() || feeCharges.isGreaterThanZero()
                        || penaltyCharges.isGreaterThanZero())) {
                    break;
                }
            }
        }

        final List<LoanCharge> loanFees = extractFeeCharges(charges);
        final List<LoanCharge> loanPenalties = extractPenaltyCharges(charges);
        Integer installmentNumber = null;
        if (loanTransaction.isChargePayment() && installments.size() == 1) {
            installmentNumber = installments.get(0).getInstallmentNumber();
        }

        if (loanTransaction.isNotWaiver()) {
            feeCharges = loanTransaction.getFeeChargesPortion(currency);
            penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency);
            if (feeCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, feeCharges, loanFees, installmentNumber);
            }

            if (penaltyCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, penaltyCharges, loanPenalties, installmentNumber);
            }
        }
    }

}
