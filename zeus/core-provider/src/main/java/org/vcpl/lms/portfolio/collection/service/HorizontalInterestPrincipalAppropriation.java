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
import org.vcpl.lms.useradministration.domain.AppUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public non-sealed class HorizontalInterestPrincipalAppropriation implements CollectionAppropriation {


    @Override
    public Money handleTransaction(LoanRepaymentScheduleInstallment currentInstallment, LoanTransaction currentTransaction, Money transactionAmount,
                                   LocalDate transactionDate, Loan loan, LoanHistoryRepo loanHistoryRepo,
                                   LoanAccrualWritePlatformService loanAccrualWritePlatformService) {
        MonetaryCurrency currency = loan.getCurrency();

        Money transactionAmountRemaining = transactionAmount;
        // Checking The Advance Scenario of The Transaction
        transactionAmountRemaining = checkForAdvanceAmount(transactionAmountRemaining, currentTransaction, loan, currentInstallment);

        InterestAppropriationData interestData = CollectionAppropriation.appropriateInterestOnRepayment(currentInstallment, transactionDate,
                transactionAmountRemaining, loanAccrualWritePlatformService);

        transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, interestData.interest());

        PrincipalAppropriationData principalData = CollectionAppropriation.appropriatePrincipalOnRepayment(currentInstallment, transactionDate, transactionAmountRemaining);
        transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, principalData.principal());

        Money due = Collectionutills.getDue(principalData,interestData,currency);

        if (currentTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue())
                && due.isGreaterThan(Money.zero(currency))) {
            transactionAmountRemaining = appropriationOnAdvanceAmount(loan, currentTransaction, currentInstallment, loanHistoryRepo, transactionAmountRemaining, principalData, interestData);
            LOG.debug("Appropriation Completed Before Due Date for Installemnt  {} ", currentInstallment.getInstallmentNumber());
        } else if (transactionDate.isBefore(currentInstallment.getDueDate()) && due.isGreaterThanZero()) {
            LoanTransaction transaction = appropriationOnBeforeDueDate(loan, currentTransaction, currentInstallment, loanHistoryRepo, principalData, interestData,transactionAmountRemaining);
            Collectionutills.updateParentId(currentTransaction, transaction);
            loan.addLoanTransaction(transaction);
            LOG.debug("Appropriation Completed Before Due Date for Installemnt  {} ", currentInstallment.getInstallmentNumber());
        } else if (transactionDate.isEqual(currentInstallment.getDueDate()) && due.isGreaterThanZero()) {
            appropriationOnDueDate(loan, currentTransaction, currentInstallment, loanHistoryRepo, principalData, interestData,transactionAmountRemaining);
            currentTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());
            LOG.debug("Appropriation Completed On Due Date for Installemnt  {} ", currentInstallment.getInstallmentNumber());
        } else if (transactionDate.isAfter(currentInstallment.getDueDate()) && due.isGreaterThanZero()) {
            appropriationOnAfterDueDate(currentTransaction, currentInstallment, loanHistoryRepo, transactionDate, principalData, interestData,transactionAmountRemaining);
            currentTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());
            LOG.debug("Appropriation Completed After Due Date for Installemnt  {} ", currentInstallment.getInstallmentNumber());
        }
        return transactionAmountRemaining;
    }

    @Override
    public LoanTransaction appropriationOnBeforeDueDate(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                                        LoanHistoryRepo loanHistoryRepo, PrincipalAppropriationData principalAppropriationData,
                                                        InterestAppropriationData interestAppropriationData, Money transactionAmountRemainig) {
        MonetaryCurrency currency = loan.getCurrency();
        Money principalPortion = Objects.nonNull(principalAppropriationData) ?  principalAppropriationData.principal(): Money.zero(currency);
        Money selfPrincipalPortion = Objects.nonNull(principalAppropriationData) ?  principalAppropriationData.selfPrincipal(): Money.zero(currency);
        Money partnerPrincipalPortion =Objects.nonNull(principalAppropriationData) ?  principalAppropriationData.partnerPrincipal(): Money.zero(currency);

        Money interestPortion =Objects.nonNull(interestAppropriationData) ?  interestAppropriationData.interest(): Money.zero(currency);
        Money selfInterestPortion =Objects.nonNull(interestAppropriationData) ?  interestAppropriationData.selfInterest(): Money.zero(currency);
        Money partnerInterestPortion = Objects.nonNull(interestAppropriationData) ?  interestAppropriationData.partnerInterest(): Money.zero(currency);

        Money feeChargesPortion = Money.zero(currency);
        Money penaltyChargesPortion = Money.zero(currency);
        LocalDate transactionDate = loanTransaction.getTransactionDate();

        BigDecimal amount = BigDecimal.ZERO;
        Money selfDue = Money.zero(currency);
        Money partnerDue = Money.zero(currency);

        LoanTransaction newTransaction = LoanTransaction.getNewLoanTransaction(loanTransaction, amount, principalPortion.getAmount(),
                interestPortion.getAmount(), feeChargesPortion.getAmount(), penaltyChargesPortion.getAmount(), selfPrincipalPortion.getAmount(), partnerPrincipalPortion.getAmount(),
                selfInterestPortion.getAmount(), partnerInterestPortion.getAmount(), selfDue.getAmount(), partnerDue.getAmount(), BusinessEventNotificationConstants.BusinessEvents.LOAN_MAKE_REPAYMENT.toString());

        newTransaction.setAmount(BigDecimal.ZERO);
        newTransaction.setSelfDue(BigDecimal.ZERO);
        newTransaction.setPartnerDue(BigDecimal.ZERO);
        newTransaction.setAdvanceAmount(transactionAmountRemainig.getAmount());
        newTransaction.setParentId(Objects.nonNull(loanTransaction.getId()) ? loanTransaction.getId() : null);
        newTransaction.setValueDate(Collectionutills.getValueDate(loanTransaction, currentInstallment));

        newTransaction.setValueDate(Collectionutills.getValueDate(loanTransaction, currentInstallment));
        if (Objects.nonNull(newTransaction.getLoanTransactionToRepaymentScheduleMappings())) {
            CollectionAppropriation.updateLoanHistory(newTransaction, loanHistoryRepo, currentInstallment, currentInstallment.getLoan(), transactionDate);

            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment, principalAppropriationData,
                    interestAppropriationData, loanTransaction.getFeeChargesPortion(currency), loanTransaction.getPenaltyChargesPortion(currency), BigDecimal.ZERO);
            newTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, newTransaction);
        }
        loanTransaction.setValueDate(Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        loanTransaction.setAdvanceAmount(loanTransaction.getAdvanceAmount().doubleValue() >0 ? loanTransaction.getAdvanceAmount():loanTransaction.getAmount());
        loanTransaction.setSelfDue(loanTransaction.getAmount());
        return newTransaction;
    }

    @Override
    public void appropriationOnDueDate(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment, LoanHistoryRepo loanHistoryRepo,
                                       PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData, Money transactionAmountRemaining) {

        MonetaryCurrency currency = loan.getCurrency();
        LocalDate transactionDate = loanTransaction.getTransactionDate();
        loanTransaction.updateLoanTransaction(principalAppropriationData, interestAppropriationData,currency);

        loanTransaction.setValueDate(loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate())
                || loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate()) ?
                Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        if (Objects.nonNull(loanTransaction.getLoanTransactionToRepaymentScheduleMappings())) {
            CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, currentInstallment.getLoan(), transactionDate);

            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment, principalAppropriationData,
                    interestAppropriationData, loanTransaction.getFeeChargesPortion(currency), loanTransaction.getPenaltyChargesPortion(currency), BigDecimal.ZERO);
            loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);
        }
    }

    @Override
    public void appropriationOnAfterDueDate(LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                            LoanHistoryRepo loanHistoryRepo, LocalDate transactionDate, PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData, Money transactionAmountRemaining) {

        MonetaryCurrency currency = currentInstallment.getLoan().getCurrency();

        loanTransaction.setValueDate(Collectionutills.getValueDate(loanTransaction, currentInstallment));
        loanTransaction.setAmount(loanTransaction.getAmount());
        loanTransaction.updateLoanTransaction(principalAppropriationData, interestAppropriationData,currency);
        CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, loanTransaction.getLoan(), transactionDate);

        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment, principalAppropriationData, interestAppropriationData,
                loanTransaction.getFeeChargesPortion(currency), loanTransaction.getPenaltyChargesPortion(currency), BigDecimal.ZERO);

        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);
    }

    @Override
    public Money appropriationOnAdvanceAmount(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                              LoanHistoryRepo loanHistoryRepo, Money transactionAmountRemaining, PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData) {
        MonetaryCurrency currency = loan.getCurrency();

        AppUser appUser = Objects.requireNonNull(loan.getLoanTransactions().stream()
                .filter(transaction -> transaction.getAppUser().isSystemUser())
                .findAny().orElse(null)).getAppUser();
        loanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.PROCESSEDADVANCE.getValue());
        loanTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());

        loanTransaction.updateLoanTransaction(principalAppropriationData, interestAppropriationData,currency);

        LoanTransaction advanceTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);
        advanceTransaction.setAmount(BigDecimal.ZERO);
        advanceTransaction.setAppUser(appUser);
        advanceTransaction.setValueDate(Collectionutills.getValueDate(loanTransaction, currentInstallment));
        advanceTransaction.setEvent(transactionAmountRemaining.isGreaterThanZero() ? BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue()
                : BusinessEventNotificationConstants.BusinessEvents.PROCESSEDADVANCE.getValue());
        CollectionAppropriation.updateLoanHistory(advanceTransaction, loanHistoryRepo, currentInstallment, loan, loanTransaction.getTransactionDate());

        Collectionutills.getNextInstallment(currentInstallment).setTotalPaidInAdvance(transactionAmountRemaining.getAmount());
        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment, principalAppropriationData, interestAppropriationData,
                loanTransaction.getFeeChargesPortion(currency), loanTransaction.getPenaltyChargesPortion(currency), BigDecimal.ZERO);
        advanceTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);
        loan.addLoanTransaction(advanceTransaction);


        loanTransaction.resetDerivedComponents();


        return Money.zero(currency);
    }
}
