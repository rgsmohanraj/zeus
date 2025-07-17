package org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistory;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.vcpl.lms.portfolio.loanproduct.domain.AdvanceAppropriationOn;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.useradministration.domain.AppUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BiFunction;

public interface Collection {

    Logger LOG = LoggerFactory.getLogger(Collection.class);
    default Money appropriationLogicLogicCalculationForAdavnce(Money transactionAmountRemaining, Loan loan, LoanTransaction loanTransaction,
                                                               LoanRepaymentScheduleInstallment currentInstallment, Money principalPortion, Money interestPortion, Money feeChargesPortion,
                                                               Money penaltyChargesPortion, Money selfPrincipalPortion, Money selfInterestPortion, BigDecimal selfFeeChargesPortion, BigDecimal selfSharePenaltyAmount,
                                                               Money partnerPrincipalPortion, Money partnerInterestPortion, BigDecimal partnerFeeChargesPortion, BigDecimal partnerSharePenaltyAmount, LoanHistoryRepo loanHistoryRepo, LocalDate transactionDate,
                                                               List<LoanTransaction> listOfLoanTransaction) {


        AppUser appUser = loan.getLoanTransactions().stream()
                .filter(transaction -> transaction.getAppUser().isSystemUser())
                .findAny().orElse(null).getAppUser();


        Money selfDue = selfInterestPortion.plus(selfPrincipalPortion);
        Money partnerDue = partnerInterestPortion.plus(partnerPrincipalPortion);

        Money amount = interestPortion.plus(principalPortion);

        LoanTransaction advanceTransaction = LoanTransaction.getNewLoanTransaction(loanTransaction, amount.getAmount(), principalPortion.getAmount(),
                interestPortion.getAmount(), feeChargesPortion.getAmount(), penaltyChargesPortion.getAmount(),
                selfPrincipalPortion.getAmount(), partnerPrincipalPortion.getAmount(), selfInterestPortion.getAmount(), partnerInterestPortion.getAmount(),
                selfDue.getAmount(), partnerDue.getAmount(), BusinessEventNotificationConstants.BusinessEvents.PROCESSEDADVANCE.getValue());

        advanceTransaction.setAppUser(appUser);

        advanceTransaction.setValueDate(loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate()) || loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate()) ?
                Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        advanceTransaction.setParentId(loanTransaction.getParentId());
        advanceTransaction.setAdvanceAmountprocessed(Long.valueOf(1));

        if (!loan.isNewTransaction(advanceTransaction)) {
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(advanceTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion, selfPrincipalPortion,
                    partnerPrincipalPortion, selfInterestPortion, partnerInterestPortion, selfPrincipalPortion.add(selfInterestPortion.getAmount()).getAmount(), partnerPrincipalPortion.add(partnerInterestPortion).getAmount(), BigDecimal.ZERO);
            advanceTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, advanceTransaction);
            loanTransactionToRepaymentScheduleMapping.setAmount(BigDecimal.ZERO);
            this.updateLoanHistory(advanceTransaction, loanHistoryRepo, currentInstallment, loan, transactionDate);

            List<CollectionReport> reports = new ArrayList<>();
            loanTransaction.setAdvanceAmountprocessed(Long.valueOf(1));
            loanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.PROCESSEDADVANCE.getValue());
            //reports.add(collectionReport);

            // post schedular Run if transaction Amount is remaning the creating the new transaction
            transactionAmountRemaining = currentInstallment.checkAdvanceForPostSchedular(transactionAmountRemaining, advanceTransaction, listOfLoanTransaction);
            advanceTransaction.setCollectionReport(reports);
            listOfLoanTransaction.add(advanceTransaction);

        }
        return transactionAmountRemaining;
    }

    default LoanTransaction beforeAndSameDateRepayment(Loan loan, LoanTransaction loanTransaction,
                                                       LoanRepaymentScheduleInstallment currentInstallment, Money principalPortion, Money interestPortion, Money feeChargesPortion,
                                                       Money penaltyChargesPortion, Money selfPrincipalPortion, Money selfInterestPortion, BigDecimal selfFeeChargesPortion, BigDecimal selfSharePenaltyAmount,
                                                       Money partnerPrincipalPortion, Money partnerInterestPortion, BigDecimal partnerFeeChargesPortion, BigDecimal partnerSharePenaltyAmount,
                                                       LoanHistoryRepo loanHistoryRepo, LocalDate transactionDate, List<LoanTransaction> listOfLoantransaction, LoanTransaction advanceLoanTransaction) {

        BigDecimal amount = BigDecimal.ZERO;

        Money selfDue = selfPrincipalPortion.add(selfInterestPortion).add(selfFeeChargesPortion);
        Money partnerDue = partnerPrincipalPortion.add(partnerInterestPortion).add(partnerFeeChargesPortion);

        LoanTransaction newTransaction = LoanTransaction.getNewLoanTransaction(loanTransaction, amount, principalPortion.getAmount(),
                interestPortion.getAmount(), feeChargesPortion.getAmount(), penaltyChargesPortion.getAmount(), selfPrincipalPortion.getAmount(), partnerPrincipalPortion.getAmount(),
                selfInterestPortion.getAmount(), partnerInterestPortion.getAmount(), selfDue.getAmount(), partnerDue.getAmount(), BusinessEventNotificationConstants.BusinessEvents.LOAN_MAKE_REPAYMENT.toString());

        newTransaction.setAmount(transactionDate.isEqual(currentInstallment.getDueDate())
                && loanTransaction.getAmount().doubleValue() > newTransaction.getInterestPortion().add(newTransaction.getPrincipalPortion()).doubleValue() ? BigDecimal.ZERO: transactionDate.isEqual(currentInstallment.getDueDate())
                                && loanTransaction.getAmount().doubleValue() ==  newTransaction.getInterestPortion().add(newTransaction.getPrincipalPortion()).doubleValue()?
                newTransaction.getInterestPortion().add(newTransaction.getPrincipalPortion()) : BigDecimal.ZERO);

        newTransaction.setValueDate(loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate())|| loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate()) ?
                Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        /* CollectionReport collectionReport = new CollectionReport();
         *//* collectionReport.mappingLoanTransactionToReport(collectionReport,loanTransaction,currentInstallment,
         interestPortion,principalPortion,feeChargesPortion,penaltyChargesPortion,selfPrincipalPortion,
        selfInterestPortion,selfFeeChargesPortion,selfSharePenaltyAmount,partnerPrincipalPortion,partnerInterest,partnerFeeChargesPortion,partnerSharePenaltyAmount);*//*
       // collectionReports.add(collectionReport);*/
        if (Objects.nonNull(newTransaction.getLoanTransactionToRepaymentScheduleMappings())) {
            this.updateLoanHistory(newTransaction, loanHistoryRepo, currentInstallment, currentInstallment.getLoan(), transactionDate);
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(newTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion, selfPrincipalPortion,
                    partnerPrincipalPortion, selfInterestPortion, partnerInterestPortion, selfPrincipalPortion.add(selfInterestPortion).getAmount(), partnerPrincipalPortion.add(partnerInterestPortion).getAmount(), BigDecimal.ZERO);
            newTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, newTransaction);
        }
        advanceLoanTransaction.setAmount(loanTransaction.getAmount());
        advanceLoanTransaction.setValueDate(loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate()) || loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate()) ?
                Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        advanceLoanTransaction.setAdvanceAmountprocessed(transactionDate.isEqual(currentInstallment.getDueDate()) ? null : Long.valueOf(1));
        advanceLoanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.ADVANCE_AMOUNT.getValue());
        advanceLoanTransaction.setAdvanceAmount(advanceLoanTransaction.getAmount());
        return newTransaction;
    }

    default void AfterDueDateRepayment(LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment, Money principalPortion, Money interestPortion, Money feeChargesPortion,
                                       Money penaltyChargesPortion, Money selfPrincipalPortion, Money selfInterestPortion, BigDecimal selfFeeChargesPortion, BigDecimal selfSharePenaltyAmount,
                                       Money partnerPrincipalPortion, Money partnerInterestPortion, BigDecimal partnerFeeChargesPortion,
                                       BigDecimal partnerSharePenaltyAmount, LoanHistoryRepo loanHistoryRepo, LocalDate transactionDate, MonetaryCurrency currency, LoanTransaction lateLoantransaction) {

        lateLoantransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,
                selfPrincipalPortion, partnerPrincipalPortion, selfInterestPortion, partnerInterestPortion,
                selfSharePenaltyAmount, partnerSharePenaltyAmount, selfFeeChargesPortion, partnerFeeChargesPortion);

        lateLoantransaction.setAmount(lateLoantransaction.getPrincipalPortion(currency).add(lateLoantransaction.getInterestPortion(currency)).getAmount());

        lateLoantransaction.setValueDate(loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate()) || loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate()) ?
                Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));


        this.updateLoanHistory(lateLoantransaction, loanHistoryRepo, currentInstallment, loanTransaction.getLoan(), transactionDate);


        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(lateLoantransaction, currentInstallment,
                principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion, selfPrincipalPortion,
                partnerPrincipalPortion, selfInterestPortion, partnerInterestPortion, selfPrincipalPortion.add(selfInterestPortion).getAmount(), partnerPrincipalPortion.add(partnerInterestPortion).getAmount(), BigDecimal.ZERO);

        lateLoantransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, lateLoantransaction);

/*
        //overDueLoanTransaction.setAmount(loanTransaction.getAmount());
        *//*LoanTransaction newLoanTransaction = new LoanTransaction(loan, loanTransaction.getOffice(), loanTransaction.getTypeOf().getValue(),loanTransaction.getDateOf(),interestPortion.getAmount().add(principalPortion.getAmount()),
                                principalPortion.getAmount(), interestPortion.getAmount(),feeChargesPortion.getAmount(), penaltyChargesPortion.getAmount(),  loanTransaction.getOverPaymentPortion(), loanTransaction.isReversed(),
                                loanTransaction.getPaymentDetail(),loanTransaction.getExternalId(), loanTransaction.getCreatedDateTime(),loanTransaction.getAppUser(), selfPrincipalPortion.add(selfInterest).add(selfFeeChargesPortion).getAmount(),
                                partnerPrincipalPortion.getAmount(), selfInterestPortion.getAmount(), partnerInterestPortion.getAmount(), selfPrincipalPortion.add(selfInterestPortion).add(selfFeeChargesPortion).getAmount(),
                                (partnerPrincipal.add(partnerInterest).add(partnerFeeChargesPortion)).getAmount(), BusinessEventNotificationConstants.BusinessEvents.LOAN_MAKE_REPAYMENT.toString(),
                                loanTransaction.getReceiptReferenceNumber(),loanTransaction.getPartnerTransferUtr(),null,loanTransaction.getRepaymentMode(),BigDecimal.ZERO,null);*//*
        CollectionReport collectionReport = new CollectionReport();
         collectionReport.mappingLoanTransactionToReport(collectionReport,newLoanTransaction,currentInstallment,
                          interestPortion,principalPortion,feeChargesPortion,penaltyChargesPortion,selfPrincipalPortion,
                                selfInterestPortion,selfFeeChargesPortion,selfSharePenaltyAmount,partnerPrincipalPortion,partnerInterest,partnerFeeChargesPortion,partnerSharePenaltyAmount);
        //  collectionReports.add(collectionReport);
        //    transactionAmountRemaining = currentInstallment.checkForAdvanceAmount(transactionAmountRemaining,newLoanTransaction,loanHistoryRepo,transactionDate,nextInstallment);

        // loan.getLoanTransactions().add(loanTransaction1);
        //  newLoanTransaction.setAdvanceAmount(loanTransaction.getAdvanceAmount().doubleValue()>0?loanTransaction.getAdvanceAmount():newLoanTransaction.getAdvanceAmount());
                            if((!loan.isNewTransaction(newLoanTransaction) || Objects.nonNull(loanTransaction.getId()) && loanTransaction.getAmount(currency).isEqualTo(newLoanTransaction.getAmount(currency)))){
                            loan.addLoanTransaction(newLoanTransaction);
                            if(loanTransaction.getInterestPortion(currency).plus(loanTransaction.getPrincipalPortion(currency)).isZero()){
                                loan.updateLoanTransaction(loanTransaction);
                            }
                            if(Objects.nonNull(newLoanTransaction.getLoanTransactionToRepaymentScheduleMappings())){
                                 AppropriationLogic.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, loanTransaction.getLoan(), transactionDate);
                                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(newLoanTransaction, currentInstallment,
                                        principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,selfPrincipalPortion,
                                        partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfPrincipalPortion.add(selfInterestPortion).getAmount(),partnerPrincipalPortion.add(partnerInterestPortion).getAmount(),BigDecimal.ZERO);
                                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping,newLoanTransaction);
                            }
                        }
                        else {
                            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion,
                                    selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,
                                    selfSharePenaltyAmount,partnerSharePenaltyAmount,selfFeeChargesPortion,partnerFeeChargesPortion);
             }*/
    }

    default void updateLoanHistory(LoanTransaction loanTransaction, LoanHistoryRepo historyRepo, LoanRepaymentScheduleInstallment currentInstallment, Loan loan, LocalDate transactionDate) {
        loanTransaction.getLoan().setEvent("collection");

        Boolean isLastIstallment = currentInstallment.isLastInstallment(loanTransaction.getLoan().getNumberOfRepayments(), currentInstallment.getInstallmentNumber());
        Boolean installmetisFullyPaid = CollectionUtills.isInstallmentIsFullYPaid(currentInstallment, loan.getCurrency());
        LoanHistory loanHistory = new LoanHistory();

        List<LoanHistory> loanHistories = historyRepo.findByLoanId(loanTransaction.getLoan().getId());

        loanHistory.setRev(loanHistories.stream().map(LoanHistory::getRev).reduce(0, Integer::max) + 1);
        BeanUtils.copyProperties(loanTransaction.getLoan(), loanHistory);
        loanHistory.setLoanId(loanTransaction.getLoan().getId());
        loanHistory.setPartnerId(loanTransaction.getLoan().getPartnerId());
        loanHistory.setClient(loanTransaction.getLoan().getClientId());
        loanHistory.setLoanProduct(loanTransaction.getLoan().getLoanProduct().getId());
        loanHistory.setInterestChargedFromDate(Date.from(loanTransaction.getLoan().getInterestChargedFromDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));


        loanHistory.setLoanStatus(isLastIstallment && installmetisFullyPaid &&
                loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.LOAN_MAKE_REPAYMENT.toString()) ? 600
                : currentInstallment.isLastInstallment(loan.getRepaymentScheduleInstallments().size(), currentInstallment.getInstallmentNumber()) && installmetisFullyPaid
                && loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.LOAN_FORECLOSURE.toString()) ? 710
                : loanTransaction.getLoan().getLoanStatus());


        if (loanHistory.getLoanStatus() == 710) {
            loanHistory.setClosedOnDate(java.sql.Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            loanHistory.setActualMaturityDate(java.sql.Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else if (loanHistory.getLoanStatus() == 600) {
            LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loan.getRepaymentScheduleInstallments().get(loan.getRepaymentScheduleInstallments().size() - 1);
            LocalDate lastInstallmentDueDate = loanRepaymentScheduleInstallment.getDueDate();
            if (loanRepaymentScheduleInstallment.getObligationsMetOnDate().isBefore(lastInstallmentDueDate)) {
                loanHistory.setClosedOnDate(java.util.Date.from(lastInstallmentDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                loanHistory.setActualMaturityDate(java.util.Date.from(lastInstallmentDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } else {
                loanHistory.setClosedOnDate(java.util.Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                loanHistory.setActualMaturityDate(java.util.Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
        } else {
            loanHistory.setClosedOnDate(null);
        }


        loanHistory.setValueDate(loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate())
                || loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate()) ? java.util.Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
                : java.util.Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        LoanHistorySummary loanHistorySummary = new LoanHistorySummary(loan);
        BeanUtils.copyProperties(loan.getSummary(), loanHistorySummary);

        loanHistorySummary.updateLoanArrearAgeing(loanTransaction.getLoan());

        loanHistorySummary.updateSummaryForLoanHistory(loan.getCurrency(), loanTransaction.getLoan().getPrincpal(),
                loanTransaction.getLoan().getRepaymentScheduleInstallments(), loanTransaction.getLoan().getLoanSummaryWrapper(), loanTransaction.getLoan().isDisbursed(), loanTransaction.getLoan().charges(),
                loanTransaction.getLoan().getSelfPrincipaAmount(), loanTransaction.getLoan().getPartnerPrincipalAmount());

        loanHistory.setSummary(loanHistorySummary);

        if ((Boolean.TRUE.equals(currentInstallment.isLastInstallment(loan.getRepaymentScheduleInstallments().size(), currentInstallment.getInstallmentNumber()))) || !loanHistories.stream().anyMatch(history -> history.getSummary().getTotalPrincipalOutstanding().doubleValue() == loanHistory.getSummary().getTotalPrincipalOutstanding().doubleValue()
                && history.getSummary().getTotalInterestOutstanding().doubleValue() == loanHistory.getSummary().getTotalInterestOutstanding().doubleValue())) {
            historyRepo.saveAndFlush(loanHistory);
        }
    }


    default Money verticalPrincipalAppropriation(MonetaryCurrency currency, Money transactionAmountRemaining,
                                                 LoanProduct loanProduct, LocalDate transactionDate,
                                                 List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments, List<LoanTransaction> transactions,
                                                 Loan loan, List<LoanTransaction> listOfLoanTransaction, LoanTransaction advanceLoanTransaction, LoanHistoryRepo loanHistoryRepo,
                                                 LoanTransaction lateLoantransaction, LoanTransaction currentTransaction) {

        for (LoanRepaymentScheduleInstallment installment : loanRepaymentScheduleInstallments) {

            if (installment.isPrincipalNotCompleted(currency) && transactionAmountRemaining.isGreaterThanZero()) {

                final Money principalPortion = installment.payPrincipalComponent(transactionDate, transactionAmountRemaining);

                transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
                final Money selfPrincipalPortion = CollectionUtills.calculateSelfPrincipalPortion(principalPortion, loanProduct);
                final Money partnerPrincipalPortion = CollectionUtills.calculatePartnerPrincipalPortion(principalPortion, selfPrincipalPortion);

                installment.paySelfPrincipalComponent(transactionDate,selfPrincipalPortion);
                LoanTransaction loanTransaction = getLoanTransactionFromMappingBasedOnInstallment(transactions, installment);

                 installment.paySelfPrincipalComponent(transactionDate,partnerPrincipalPortion);
                LOG.debug("Vertical Principal Paid For Installment Number {}",installment.getInstallmentNumber());
                if(LocalDate.now().isBefore(installment.getDueDate())){
                    LOG.info("ADVANCE LOAN TRANSACTION FOR FOUND FOR LOANID {}",loan.getId() );
                    LoanTransaction transaction = LoanTransaction.copyTransactionProperties(currentTransaction);
                    loan.updateLoanTransaction(transaction);
                }
                if (Objects.nonNull(loanTransaction)) {
                    loanTransaction.setPrincipalPortion(principalPortion.getAmount());
                    loanTransaction.setSelfPrincipalPortion(selfPrincipalPortion.getAmount());
                    loanTransaction.setPartnerPrincipalPortion(partnerPrincipalPortion.getAmount());
                    loanTransaction.getLoanTransactionToRepaymentScheduleMappings().clear();

                    BigDecimal selfDue = CollectionUtills.addAmount(loanTransaction.getSelfInterestPortion(), selfPrincipalPortion.getAmount());
                    BigDecimal partnerDue = CollectionUtills.addAmount(loanTransaction.getPartnerInterestPortion(), partnerPrincipalPortion.getAmount());
                    loanTransaction.setAmount(CollectionUtills.addAmount(loanTransaction.getInterestPortion(),principalPortion.getAmount()));

                    LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, installment, principalPortion, loanTransaction.getInterestPortion(currency), loanTransaction.getFeeChargesPortion(currency),
                            loanTransaction.getPenaltyChargesPortion(currency), selfPrincipalPortion, partnerPrincipalPortion, loanTransaction.getSelfInterestPortion(currency), loanTransaction.getPartnerInterestPortion(currency), selfDue, partnerDue, BigDecimal.ZERO);
                    loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);

                    if (loanTransaction.getTransactionDate().isBefore(installment.getDueDate())
                            || loanTransaction.getTransactionDate().isEqual(installment.getDueDate())) {
                        LoanTransaction newLoanTransaction = this.beforeAndSameDateRepayment(loan, loanTransaction, installment, principalPortion, loanTransaction.getInterestPortion(currency), loanTransaction.getFeeChargesPortion(currency),
                                loanTransaction.getFeeChargesPortion(currency), selfPrincipalPortion, loanTransaction.getSelfInterestPortion(currency), BigDecimal.ZERO, BigDecimal.ZERO,
                                partnerPrincipalPortion, loanTransaction.getPartnerInterestPortion(currency), BigDecimal.ZERO, BigDecimal.ZERO, loanHistoryRepo,
                                transactionDate, listOfLoanTransaction, advanceLoanTransaction);
                        loan.addLoanTransaction(newLoanTransaction);
                        transactions.remove(loanTransaction);

                    } else {
                        this.AfterDueDateRepayment(loanTransaction, installment, principalPortion, loanTransaction.getInterestPortion(currency), loanTransaction.getFeeChargesPortion(currency),
                                loanTransaction.getPenaltyChargesPortion(currency), selfPrincipalPortion, loanTransaction.getSelfInterestPortion(currency), BigDecimal.ZERO, BigDecimal.ZERO,
                                partnerPrincipalPortion, loanTransaction.getPartnerInterestPortion(currency), BigDecimal.ZERO, BigDecimal.ZERO, loanHistoryRepo,
                                transactionDate, currency, lateLoantransaction);
                        transactions.remove(loanTransaction);
                    }
                }else {
                    Money amount = principalPortion;
                    Money interestPortion = Money.zero(currency);
                    Money feeChargesPortion = Money.zero(currency);
                    Money penaltyChargesPortion = Money.zero(currency);
                    Money selfInterestPortion = Money.zero(currency);
                    BigDecimal partnerInterestPortion = BigDecimal.ZERO;
                    BigDecimal selfDue = selfPrincipalPortion.getAmount();
                    BigDecimal partnerDue = partnerPrincipalPortion.getAmount();

                    LoanTransaction newLoanTranaction = LoanTransaction.getNewLoanTransaction(currentTransaction, amount.getAmount(), principalPortion.getAmount(),
                            interestPortion.getAmount(), feeChargesPortion.getAmount(), penaltyChargesPortion.getAmount(), selfPrincipalPortion.getAmount(), partnerPrincipalPortion.getAmount(),
                            selfInterestPortion.getAmount(), partnerInterestPortion, selfDue, partnerDue, null);

                    if (currentTransaction.getTransactionDate().isBefore(installment.getDueDate())
                            || currentTransaction.getTransactionDate().isEqual(installment.getDueDate())) {
                        LoanTransaction newLoanTransaction = this.beforeAndSameDateRepayment(loan, newLoanTranaction, installment, principalPortion, newLoanTranaction.getInterestPortion(currency), newLoanTranaction.getFeeChargesPortion(currency),
                                newLoanTranaction.getFeeChargesPortion(currency), selfPrincipalPortion, newLoanTranaction.getSelfInterestPortion(currency), BigDecimal.ZERO, BigDecimal.ZERO,
                                partnerPrincipalPortion, newLoanTranaction.getPartnerInterestPortion(currency), BigDecimal.ZERO, BigDecimal.ZERO, loanHistoryRepo,
                                transactionDate, listOfLoanTransaction, advanceLoanTransaction);
                        loan.addLoanTransaction(newLoanTransaction);
                    } else {
                        this.AfterDueDateRepayment(currentTransaction, installment, principalPortion, interestPortion, feeChargesPortion,
                                penaltyChargesPortion, selfPrincipalPortion, selfInterestPortion, BigDecimal.ZERO, BigDecimal.ZERO,
                                partnerPrincipalPortion, partnerPrincipalPortion, BigDecimal.ZERO, BigDecimal.ZERO, loanHistoryRepo,
                                transactionDate, currency, lateLoantransaction);
                    }
                }
            }
        }


        return transactionAmountRemaining;

    }


    default Money checkForAdvanceAmount(Money transactionAmountRemaining, LoanTransaction loanTransaction, LocalDate transactionDate,
                                        List<LoanTransaction> listOfLoanTransaction, Loan loan, LoanRepaymentScheduleInstallment currentInstallemt, MonetaryCurrency currency,LoanProduct loanProduct) {

        /**
         * if the loan Transaction is Eligible For Advance then generating the advance loan Transaction and Based on the next installment condition
         */

        LOG.info("ADVACE AMOUNT IS PARKED FOR LOAN{}", loan.getId());

        LoanRepaymentScheduleInstallment nextInstallment = loan.fetchRepaymentScheduleInstallment(currentInstallemt.getInstallmentNumber() + 1);
        if (Boolean.TRUE.equals(currentInstallemt.isLastInstallment(loan.getNumberOfRepayments(), currentInstallemt.getInstallmentNumber())
                && transactionAmountRemaining.getAmount().doubleValue() > currentInstallemt.getTotalOutstanding(loan.getCurrency()).getAmount().doubleValue())
                && AdvanceAppropriationOn.isOnDueDate(loanProduct.getProductCollectionConfig().getAdvanceAppropriationOn())) {
            final String errorMessage = "The transaction amount " + loan.getLoanSummary().getTotalOutstanding() + " cannot be greater than the total outstanding amount " + loanTransaction.getAmount();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.outstanding.amount {}", errorMessage);
        }
        if (!loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.LOAN_FORECLOSURE.getValue()) && LocalDate.now().isBefore(currentInstallemt.getDueDate())
                && (Objects.nonNull(nextInstallment) || currentInstallemt.isLastInstallment(loan.getNumberOfRepayments(), currentInstallemt.getInstallmentNumber()))
                && AdvanceAppropriationOn.isOnDueDate(loanProduct.getProductCollectionConfig().getAdvanceAppropriationOn())) {


            LoanTransaction advanceLoanTransaction = LoanTransaction.getNewLoanTransaction(loanTransaction, transactionAmountRemaining.getAmount(), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue());

            advanceLoanTransaction.setAdvanceAmountprocessed(Long.valueOf(0));
            advanceLoanTransaction.setValueDate(Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(advanceLoanTransaction, currentInstallemt
                    , Money.zero(currency), Money.zero(currency), Money.zero(currency), Money.zero(currency), Money.zero(currency),
                    Money.zero(currency), Money.zero(currency), Money.zero(currency), Money.zero(currency).getAmount(), Money.zero(currency).getAmount(), transactionAmountRemaining.getAmount());

            loanTransactionToRepaymentScheduleMapping.setAmount(transactionAmountRemaining.getAmount());
            advanceLoanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, advanceLoanTransaction);
            currentInstallemt.setTotalPaidInAdvance(currentInstallemt.getTotalPaidInAdvance().add(transactionAmountRemaining.getAmount()));
            if (!loan.isNewTransaction(advanceLoanTransaction)) {
                listOfLoanTransaction.add(advanceLoanTransaction);
            }
            advanceLoanTransaction.setAdvanceAmountprocessed(Long.valueOf(0));



       /* LoanRepaymentScheduleInstallment nextInstallment =loan.fetchRepaymentScheduleInstallment(this.getInstallmentNumber() + 1);
        if( Boolean.TRUE.equals(this.isLastInstallment(loan.getNumberOfRepayments(), this.getInstallmentNumber()) && transactionAmountRemaining.getAmount().doubleValue() > this.getTotalOutstanding(loan.getCurrency()).getAmount().doubleValue()) ){
            final String errorMessage = "The transaction amount "+ loan.getLoanSummary().getTotalOutstanding() + " cannot be greater than the total outstanding amount " + loanTransaction.getAmount();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.outstanding.amount {}", errorMessage);
        }
        if (!loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.LOAN_FORECLOSURE.getValue())
                && (Objects.nonNull(nextInstallment) || this.isLastInstallment(loan.getNumberOfRepayments(),this.getInstallmentNumber()))
                && LocalDate.now().isBefore( Boolean.TRUE.equals(this.isLastInstallment(loan.getNumberOfRepayments(),this.getInstallmentNumber()))
                ? this.getDueDate():currentInstallment.getDueDate())) {
            *//*loanTransaction.setValueDate(loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate())
                    || loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate()) ? java.util.Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : java.util.Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));*//*
            loanTransaction.setValueDate(loanTransaction.getDateOf());
            CollectionReport collectionReport = new CollectionReport();
            collectionReport.setLoan(loan);
            collectionReport.setLoanTransaction(loanTransaction);
            collectionReport.setInstallmentNumber(this.getInstallmentNumber());
            collectionReport.setAmount(transactionAmountRemaining.getAmount());
            collectionReport.setDateOf(loanTransaction.getDateOf());
            collectionReport.setTypeOf(loanTransaction.getTypeOf().getValue());
            collectionReport.setCreatedDate(Date.from(loanTransaction.getCreatedDateTime().atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant()));
            collectionReport.setAdvanceAmount(transactionAmountRemaining.getAmount());
          //  loanTransaction.setCollectionReport(collectionReport);
            loanTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());
            currentInstallment.totalPaidInAdvance = getTotalPaidInAdvance().add(transactionAmountRemaining.getAmount());
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, this
                    , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO))
                    , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), defaultToNullIfZero(BigDecimal.ZERO),
                    defaultToNullIfZero(BigDecimal.ZERO), transactionAmountRemaining.getAmount());
            loanTransactionToRepaymentScheduleMapping.setAmount(transactionAmountRemaining.getAmount());
            if(loanTransaction.getAmount().doubleValue() > transactionAmountRemaining.getAmount().doubleValue()){
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping,loanTransaction);}
            else if(loanTransaction.getAmount().doubleValue() == transactionAmountRemaining.getAmount().doubleValue() && loanTransaction.getLoanTransactionToRepaymentScheduleMappings().isEmpty()){
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping,loanTransaction);
            }
            return Money.zero(this.loan.getCurrency());
        }*/


            //  loan.addLoanTransaction(advanceLoanTransaction);

            /*if(!loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.ADVANCE_AMOUNT.getValue())
                    && !loanTransaction.getPrincipalPortion(loan.getCurrency()).plus(loanTransaction.getInterestPortion(loan.getCurrency())).isZero()){
                loan.addLoanTransaction(advanceLoanTransaction);
            }else{
                if(loanTransaction.getAmount().doubleValue() == transactionAmountRemaining.getAmount().doubleValue()){
                loanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.ADVANCE_AMOUNT.getValue());
                LoanTransactionToRepaymentScheduleMapping transactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, this
                        , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO))
                        , Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), Money.of(loan.getCurrency(), defaultToNullIfZero(BigDecimal.ZERO)), defaultToNullIfZero(BigDecimal.ZERO),
                        defaultToNullIfZero(BigDecimal.ZERO), transactionAmountRemaining.getAmount());
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionToRepaymentScheduleMapping,loanTransaction);
                loanTransaction.setValueDate(this.dueDate);
                loanTransaction.setAmount(transactionAmountRemaining.getAmount());}
                else {
                    if(!loan.isNewTransaction(advanceLoanTransaction)){
                    loan.addLoanTransaction(advanceLoanTransaction);}
                }
            }*/
            return Money.zero(currentInstallemt.getLoan().getCurrency());
        }
        return transactionAmountRemaining;
    }
    default LoanTransaction getLoanTransactionFromMappingBasedOnInstallment(List<LoanTransaction> transactions, LoanRepaymentScheduleInstallment installment){
      return getLoanTransaction.apply(transactions,installment);
    }
    BiFunction<List<LoanTransaction>, LoanRepaymentScheduleInstallment,LoanTransaction> getLoanTransaction =(loanTransaction, installment) -> {
        LoanTransaction transaction= null;
        for (LoanTransaction transactions : loanTransaction){
            Boolean isPresent = transactions.getLoanTransactionToRepaymentScheduleMappings()
                    .stream()
                    .anyMatch(mapping -> mapping.getLoanRepaymentScheduleInstallment()
                            .getId().equals(installment.getId()));
            if(Boolean.TRUE.equals(isPresent)){
                transaction = transactions;
                break;
            }
        }
        return transaction;
    };
    default  Money verticalInterestAppropriation(List<LoanRepaymentScheduleInstallment> installments,MonetaryCurrency currency,
                                                 Money transactionAmountRemaining,LoanTransaction loanTransaction,LoanProduct loanProduct,
                                                 LocalDate transactionDate,List<LoanTransaction> transactions,
                                                 LoanRepaymentScheduleInstallment currentInstallmentBasedOnTransactionDate,Loan loan,List<LoanTransaction> listOfLoanTransaction,LoanHistoryRepo historyRepo){
        for (final LoanRepaymentScheduleInstallment installment : installments) {

            if ((installment.isInterestDue(currency) || installment.getFeeChargesOutstanding(currency).isGreaterThanZero()
                    || installment.getPenaltyChargesOutstanding(currency).isGreaterThanZero())
                    && (installment.isOverdueOn(loanTransaction.getTransactionDate()) || installment.getDueDate().isEqual(loanTransaction.getTransactionDate())
                    ||  installment.getInstallmentNumber().equals(currentInstallmentBasedOnTransactionDate.getInstallmentNumber()))
                    && transactionAmountRemaining.isGreaterThanZero()) {

                this.checkForAdvanceAmount(transactionAmountRemaining,  loanTransaction,  transactionDate,
                        listOfLoanTransaction,  loan,  installment,  currency, loanProduct);

                LOG.debug("Vertical Interest Paid For Installment Number {}",installment.getInstallmentNumber());

                final Money interestPortion = installment.payInterestComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
                final Money selfInterestPortion =  CollectionUtills.calculateSelfInterest(interestPortion,loanProduct);
                installment.paySelfInterestComponent(transactionDate,selfInterestPortion);
                final Money partnerInterestPortion = CollectionUtills.calculatePartnerInterest(interestPortion,selfInterestPortion);
                installment.payPartnerInterestComponent(transactionDate,partnerInterestPortion);


                Money principalPortion = Money.zero(currency);
                Money selfPrincipalPortion =Money.zero(currency);
                Money partnerPrincipalPortion = Money.zero(currency);
                Money feeChargesPortion = Money.zero(currency);
                Money penaltyChargesPortion = Money.zero(currency);

                BigDecimal selfDue = CollectionUtills.addAmount(selfInterestPortion.getAmount(),selfPrincipalPortion.getAmount());
                BigDecimal partnerDue = CollectionUtills.addAmount(partnerInterestPortion.getAmount(),partnerPrincipalPortion.getAmount());

                LoanTransaction transaction = LoanTransaction.getNewLoanTransaction(loanTransaction,interestPortion.add(principalPortion).getAmount(),
                        principalPortion.getAmount(),interestPortion.getAmount(),feeChargesPortion.getAmount(),penaltyChargesPortion.getAmount(),selfPrincipalPortion.getAmount(),partnerPrincipalPortion.getAmount(),selfInterestPortion.getAmount(),
                        partnerInterestPortion.getAmount(),selfPrincipalPortion.add(selfInterestPortion).getAmount(),partnerPrincipalPortion.add(partnerInterestPortion).getAmount(),BusinessEventNotificationConstants.BusinessEvents.LOAN_MAKE_REPAYMENT.getValue());

                transaction.setValueDate(loanTransaction.getTransactionDate().isEqual(installment.getDueDate()) || loanTransaction.getTransactionDate().isBefore(installment.getDueDate()) ?
                        Date.from(installment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                        Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));

                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,installment,principalPortion,interestPortion,feeChargesPortion,
                        penaltyChargesPortion,selfPrincipalPortion,partnerPrincipalPortion,selfInterestPortion,partnerInterestPortion,selfDue,partnerDue,BigDecimal.ZERO);
                transaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping,transaction);
                this.updateLoanHistory(transaction,  historyRepo,  installment,  loan,  transactionDate);
                transactions.add(transaction);
            }
        }

        return transactionAmountRemaining;
    }

}