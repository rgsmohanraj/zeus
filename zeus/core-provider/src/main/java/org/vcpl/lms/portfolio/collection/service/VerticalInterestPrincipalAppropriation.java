package org.vcpl.lms.portfolio.collection.service;

import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.collection.utills.Collectionutills;
import org.vcpl.lms.portfolio.collection.data.InterestAppropriationData;
import org.vcpl.lms.portfolio.collection.data.PrincipalAppropriationData;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransaction;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.service.LoanAccrualWritePlatformService;
import org.vcpl.lms.portfolio.loanaccount.service.LoanChargeActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;


public final class VerticalInterestPrincipalAppropriation implements CollectionAppropriation {

    @Override
    public Money handleTransaction(LoanRepaymentScheduleInstallment currentInstallment, LoanTransaction currentTransaction, Money transactionAmount,
                                   LocalDate transactionDate, Loan loan, LoanHistoryRepo loanHistoryRepo,
                                   LoanAccrualWritePlatformService loanAccrualWritePlatformService) {

        List<LoanTransactionToRepaymentScheduleMapping> mappings = new ArrayList<>();

        Money transactionAmountRemaining = transactionAmount;

        // Checking The Advance Scenario of The Transaction
        transactionAmountRemaining = checkForAdvanceAmount(transactionAmountRemaining, currentTransaction, loan, currentInstallment);

        InterestAppropriationData interestData = verticalInterestAppropriation(loan.getRepaymentScheduleInstallments(), transactionAmountRemaining,
                currentTransaction, transactionDate, loan, mappings, loanAccrualWritePlatformService);
        LOG.info("Vertical Interest Appropriation Completed");
        transactionAmountRemaining = transactionAmountRemaining.minus(Objects.isNull(interestData) ? Money.zero(loan.getCurrency()) : currentTransaction.getInterestPortion(loan.getCurrency()));

        transactionAmountRemaining = verticalPrincipalAppropriation(transactionAmountRemaining, transactionDate, loan,
                loanHistoryRepo, currentTransaction, interestData, mappings);
        LOG.info("Vertical Principal Appropriation Completed");

        transactionAmountRemaining = advanceAppropriationOnInterestAndPrincipal(transactionDate, transactionAmountRemaining, currentTransaction, loan, loanHistoryRepo,loanAccrualWritePlatformService);


        return transactionAmountRemaining;
    }

    private InterestAppropriationData verticalInterestAppropriation(List<LoanRepaymentScheduleInstallment> installments, Money transactionAmountRemaining,
                                                                    LoanTransaction loanTransaction, LocalDate transactionDate,
                                                                    Loan loan, List<LoanTransactionToRepaymentScheduleMapping> mappings,
                                                                    LoanAccrualWritePlatformService loanAccrualWritePlatformService) {

        Money unprocessedTransactionAmount = transactionAmountRemaining;

        MonetaryCurrency currency = loan.getCurrency();
        InterestAppropriationData interestAppropriationData = null;
        for (LoanRepaymentScheduleInstallment installment : installments) {
            if ((installment.isInterestDue(currency) && !loanTransaction.getTransactionDate().isBefore(installment.getDueDate())
                    && transactionAmountRemaining.isGreaterThanZero() && !installment.isObligationsMet())) {
                interestAppropriationData = CollectionAppropriation.appropriateInterestOnRepayment(installment, transactionDate,
                        unprocessedTransactionAmount, loanAccrualWritePlatformService);
                loanTransaction.updateInterestPortion(interestAppropriationData.interest(), interestAppropriationData.selfInterest(), interestAppropriationData.partnerInterest());
                unprocessedTransactionAmount = unprocessedTransactionAmount.minus(interestAppropriationData.interest());

                LoanChargeActions.reverseChargesOnRepayment(installment,transactionDate);
                installment.reverseDaysPastDueOnInterestPrincipleCompletion(transactionDate);

                mappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, installment, null, interestAppropriationData, Money.zero(currency),
                        Money.zero(currency), BigDecimal.ZERO));

            }
            if (installment.getPrincipalOutstanding(currency).isZero()) {
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(mappings);
            }
        }
        return interestAppropriationData;

    }

    @Override
    public LoanTransaction appropriationOnBeforeDueDate(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                                        LoanHistoryRepo loanHistoryRepo, PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData,
                                                        Money transactionAmountRemaining) {

        MonetaryCurrency currency = loan.getCurrency();
        Money principalPortion = Objects.nonNull(principalAppropriationData) ? principalAppropriationData.principal() : Money.zero(currency);
        Money selfPrincipalPortion = Objects.nonNull(principalAppropriationData) ? principalAppropriationData.selfPrincipal() : Money.zero(currency);
        Money partnerPrincipalPortion = Objects.nonNull(principalAppropriationData) ? principalAppropriationData.partnerPrincipal() : Money.zero(currency);

        Money interestPortion = Objects.nonNull(interestAppropriationData) ? interestAppropriationData.interest() : Money.zero(currency);
        Money selfInterestPortion = Objects.nonNull(interestAppropriationData) ? interestAppropriationData.selfInterest() : Money.zero(currency);
        Money partnerInterestPortion = Objects.nonNull(interestAppropriationData) ? interestAppropriationData.partnerInterest() : Money.zero(currency);

        Money feeChargesPortion = loanTransaction.getFeeChargesPortion(currency);
        Money penaltyChargesPortion = loanTransaction.getPenaltyChargesPortion(currency);
        LocalDate transactionDate = loanTransaction.getTransactionDate();

        BigDecimal amount = BigDecimal.ZERO;
        Money selfDue = loanTransaction.getSelfPrincipalPortion(currency).add(loanTransaction.getSelfInterestPortion(currency)).add(loanTransaction.getFeeChargesPortion(currency));
        Money partnerDue = loanTransaction.getPartnerPrincipalPortion(currency).add(loanTransaction.getPartnerInterestPortion(currency)).add(loanTransaction.getFeeChargesPortion(currency));

        LoanTransaction newTransaction = LoanTransaction.getNewLoanTransaction(loanTransaction, amount, principalPortion.getAmount(),
                interestPortion.getAmount(), feeChargesPortion.getAmount(), penaltyChargesPortion.getAmount(), selfPrincipalPortion.getAmount(), partnerPrincipalPortion.getAmount(),
                selfInterestPortion.getAmount(), partnerInterestPortion.getAmount(), selfDue.getAmount(), partnerDue.getAmount(), BusinessEventNotificationConstants.BusinessEvents.LOAN_MAKE_REPAYMENT.toString());

        newTransaction.setSelfDue(BigDecimal.ZERO);
        newTransaction.setPartnerDue(BigDecimal.ZERO);

        CollectionAppropriation.updateLoanHistory(newTransaction, loanHistoryRepo, currentInstallment, currentInstallment.getLoan(), transactionDate);

        loanTransaction.setValueDate(Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        loanTransaction.setAdvanceAmount(loanTransaction.getAmount());
        return newTransaction;
    }

    @Override
    public void appropriationOnDueDate(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                       LoanHistoryRepo loanHistoryRepo, PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData, Money transactionAmountRemaining) {

        MonetaryCurrency currency = loan.getCurrency();
        LocalDate transactionDate = loanTransaction.getTransactionDate();
        // passing Null  to Update LoanTransaction already Interest Portion Updated
        loanTransaction.updateLoanTransaction(principalAppropriationData, null, currency);
        CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, currentInstallment.getLoan(), transactionDate);
        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment, principalAppropriationData,
                interestAppropriationData, loanTransaction.getFeeChargesPortion(currency), loanTransaction.getPenaltyChargesPortion(currency), BigDecimal.ZERO);
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);

    }

    @Override
    public void appropriationOnAfterDueDate(LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                            LoanHistoryRepo loanHistoryRepo, LocalDate transactionDate, PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData, Money transactionAmountRemaining) {

        MonetaryCurrency currency = currentInstallment.getLoan().getCurrency();
        loanTransaction.setValueDate(Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        // passing Null  to Update LoanTransaction already Interest Portion Updated
        loanTransaction.updateLoanTransaction(principalAppropriationData, null, currency);
        CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, loanTransaction.getLoan(), transactionDate);


    }

    @Override
    public Money appropriationOnAdvanceAmount(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                              LoanHistoryRepo loanHistoryRepo, Money transactionAmountRemaining, PrincipalAppropriationData principalAppropriationData,
                                              InterestAppropriationData interestAppropriationData) {
        // No  Need Imple mentation
        return null;
    }

    private Money verticalPrincipalAppropriation(Money transactionAmountRemaining, LocalDate transactionDate,
                                                 Loan loan, LoanHistoryRepo loanHistoryRepo, LoanTransaction currentTransaction,
                                                 InterestAppropriationData interestAppropriationData, List<LoanTransactionToRepaymentScheduleMapping> mappings) {

        MonetaryCurrency currency = loan.getCurrency();
        Money transactionAmountUnprocessed = transactionAmountRemaining;

        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            if (installment.isPrincipalNotCompleted(currency) && !installment.getDueDate().isAfter(transactionDate)
                    && !installment.isObligationsMet()) {

                PrincipalAppropriationData principalAppropriationData = CollectionAppropriation.appropriatePrincipalOnRepayment(installment, transactionDate, transactionAmountUnprocessed);
                transactionAmountUnprocessed = transactionAmountUnprocessed.minus(principalAppropriationData.principal());
                LOG.debug("Vertical Principal Paid For Installment Number {}", installment.getInstallmentNumber());
                Money due = Collectionutills.getDue(principalAppropriationData, interestAppropriationData, currency);

                LoanChargeActions.reverseChargesOnRepayment(installment,transactionDate);
                installment.reverseDaysPastDueOnInterestPrincipleCompletion(transactionDate);

                if (transactionDate.isBefore(installment.getDueDate()) && due.isGreaterThanZero()) {
                    LoanTransaction beforeDueDateTransaction = appropriationOnBeforeDueDate(loan, currentTransaction, installment, loanHistoryRepo,
                            principalAppropriationData, interestAppropriationData, transactionAmountRemaining);
                    updateLoanTransactionMapping(mappings, installment, principalAppropriationData, beforeDueDateTransaction);
                    loan.addLoanTransaction(beforeDueDateTransaction);
                    LOG.debug("Appropriation Completed Before Due Date for Installment  {} ", installment.getInstallmentNumber());

                } else if (transactionDate.isEqual(installment.getDueDate()) && due.isGreaterThanZero()) {
                    appropriationOnDueDate(loan, currentTransaction, installment, loanHistoryRepo, principalAppropriationData, interestAppropriationData,transactionAmountRemaining);
                    currentTransaction.setValueDate(Date.from(currentTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    LOG.debug("Appropriation Completed On Due Date for Installment  {} ", installment.getInstallmentNumber());

                } else if (transactionDate.isAfter(installment.getDueDate()) && due.isGreaterThanZero()) {
                    appropriationOnAfterDueDate(currentTransaction, installment, loanHistoryRepo, transactionDate, principalAppropriationData, interestAppropriationData,transactionAmountRemaining);
                    updateLoanTransactionMapping(mappings, installment, principalAppropriationData, currentTransaction);
                    LOG.debug("Appropriation Completed After Due Date for Installment  {} ", installment.getInstallmentNumber());
                }

            }
        }
        return transactionAmountUnprocessed;
    }

    private void updateLoanTransactionMapping(List<LoanTransactionToRepaymentScheduleMapping> mappings, LoanRepaymentScheduleInstallment installment,
                                              PrincipalAppropriationData principalAppropriationData, LoanTransaction loanTransaction) {

        if(mappings.isEmpty() && principalAppropriationData.principal().isGreaterThanZero()){
            LoanTransactionToRepaymentScheduleMapping transaction =  LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,installment,principalAppropriationData,null
                    ,loanTransaction.getFeeChargesPortion(loanTransaction.getLoan().getCurrency()),loanTransaction.getPenaltyChargesPortion(loanTransaction.getLoan().getCurrency()),BigDecimal.ZERO);
            mappings.add(transaction);
        }
        Set<LoanTransactionToRepaymentScheduleMapping>  loanTransactionToRepaymentScheduleMappings = new HashSet<>();
        LoanTransactionToRepaymentScheduleMapping transactionMapping =  mappings.stream().filter(mapping->mapping.getLoanRepaymentScheduleInstallment().getInstallmentNumber().equals(installment.getInstallmentNumber()))
                .findFirst().orElse(null);
       if(Objects.nonNull(transactionMapping)){
       transactionMapping.setLoanTransaction(null);
        transactionMapping.setLoanTransaction(loanTransaction);
        transactionMapping.updateComponents(principalAppropriationData.principal(),
                principalAppropriationData.selfPrincipal(), principalAppropriationData.partnerPrincipal());}
       else {
           LoanTransactionToRepaymentScheduleMapping transaction =  LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,installment,principalAppropriationData,null
                   ,loanTransaction.getFeeChargesPortion(loanTransaction.getLoan().getCurrency()),loanTransaction.getPenaltyChargesPortion(loanTransaction.getLoan().getCurrency()),BigDecimal.ZERO);
           loanTransactionToRepaymentScheduleMappings.add(transaction);
       }


        /*mappings.forEach(transactionMapping -> {
            if (transactionMapping.getLoanRepaymentScheduleInstallment().getInstallmentNumber().equals(installment.getInstallmentNumber())) {
                System.out.println("first Condition");
                transactionMapping.setLoanTransaction(null);
                transactionMapping.setLoanTransaction(loanTransaction);
                transactionMapping.updateComponents(principalAppropriationData.principal(),
                        principalAppropriationData.selfPrincipal(), principalAppropriationData.partnerPrincipal());
            }else {
                System.out.println("second Condition");
                LoanTransactionToRepaymentScheduleMapping transaction =  LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,installment,principalAppropriationData,null
                        ,loanTransaction.getFeeChargesPortion(loanTransaction.getLoan().getCurrency()),loanTransaction.getPenaltyChargesPortion(loanTransaction.getLoan().getCurrency()),BigDecimal.ZERO);
                loanTransactionToRepaymentScheduleMappings.add(transaction);
            }
        });*/
        mappings.addAll(loanTransactionToRepaymentScheduleMappings);
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(mappings);
    }
}


