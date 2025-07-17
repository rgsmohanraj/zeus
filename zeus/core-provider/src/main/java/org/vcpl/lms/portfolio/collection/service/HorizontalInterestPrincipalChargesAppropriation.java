package org.vcpl.lms.portfolio.collection.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.useradministration.domain.AppUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

@Service
public final class HorizontalInterestPrincipalChargesAppropriation implements CollectionAppropriation {

    Logger LOG = LoggerFactory.getLogger(HorizontalInterestPrincipalChargesAppropriation.class);

    @Override
    public LoanTransaction appropriationOnBeforeDueDate(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                                        LoanHistoryRepo loanHistoryRepo, PrincipalAppropriationData principalAppropriationData,
                                                        InterestAppropriationData interestAppropriationData, Money transactionAmountRemaining) {

        MonetaryCurrency currency = loan.getCurrency();
        Money principalPortion = Objects.nonNull(principalAppropriationData) ?  principalAppropriationData.principal(): Money.zero(currency);
        Money selfPrincipalPortion = Objects.nonNull(principalAppropriationData) ?  principalAppropriationData.selfPrincipal(): Money.zero(currency);
        Money partnerPrincipalPortion =Objects.nonNull(principalAppropriationData) ?  principalAppropriationData.partnerPrincipal(): Money.zero(currency);

        Money interestPortion =Objects.nonNull(interestAppropriationData) ?  interestAppropriationData.interest(): Money.zero(currency);
        Money selfInterestPortion =Objects.nonNull(interestAppropriationData) ?  interestAppropriationData.selfInterest(): Money.zero(currency);
        Money partnerInterestPortion = Objects.nonNull(interestAppropriationData) ?  interestAppropriationData.partnerInterest(): Money.zero(currency);

        Money feeChargesPortion = loanTransaction.getFeeChargesPortion(currency);
        Money penaltyChargesPortion = loanTransaction.getPenaltyChargesPortion(currency);
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
        newTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());
        newTransaction.setParentId(Objects.nonNull(loanTransaction.getId()) ? loanTransaction.getId() : null);
        newTransaction.setValueDate(Collectionutills.getValueDate(loanTransaction, currentInstallment));

        if (Objects.nonNull(newTransaction.getLoanTransactionToRepaymentScheduleMappings())) {
            CollectionAppropriation.updateLoanHistory(newTransaction, loanHistoryRepo, currentInstallment, currentInstallment.getLoan(), transactionDate);
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment, principalAppropriationData,
                    interestAppropriationData, loanTransaction.getFeeChargesPortion(currency)
                    , loanTransaction.getPenaltyChargesPortion(currency), transactionAmountRemaining.getAmount());
            newTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, newTransaction);
        }
        LoanRepaymentScheduleInstallment nextInstallment = Collectionutills.getNextInstallment(currentInstallment);
        if (LocalDate.now().isBefore(nextInstallment.getDueDate()) ||
                currentInstallment.isLastInstallment(loan.getNumberOfRepayments(), currentInstallment.getInstallmentNumber())) {
            nextInstallment.setTotalPaidInAdvance(transactionAmountRemaining.getAmount());
            newTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue());
        }
        loanTransaction.setAdvanceAmount(loanTransaction.getAdvanceAmount().doubleValue() >0 ? loanTransaction.getAdvanceAmount():loanTransaction.getAmount());
        loanTransaction.setValueDate(Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return newTransaction;
    }

    @Override
    public void appropriationOnDueDate(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                       LoanHistoryRepo loanHistoryRepo, PrincipalAppropriationData principalAppropriationData,
                                       InterestAppropriationData interestAppropriationData, Money transactionAmountRemainif) {



        MonetaryCurrency currency = loan.getCurrency();
        LocalDate transactionDate = loanTransaction.getTransactionDate();
        loanTransaction.setValueDate(Collectionutills.getValueDate(loanTransaction,currentInstallment));
        loanTransaction.updateLoanTransaction(principalAppropriationData, interestAppropriationData,currency);
        if (Objects.nonNull(loanTransaction.getLoanTransactionToRepaymentScheduleMappings())) {
            CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, currentInstallment.getLoan(), transactionDate);
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,currentInstallment,principalAppropriationData,
                    interestAppropriationData,loanTransaction.getFeeChargesPortion(currency),loanTransaction.getPenaltyChargesPortion(currency),transactionAmountRemainif.getAmount());
            loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping,loanTransaction);
        }
    }

    @Override
    public void appropriationOnAfterDueDate(LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                            LoanHistoryRepo loanHistoryRepo, LocalDate transactionDate, PrincipalAppropriationData principalAppropriationData,
                                            InterestAppropriationData interestAppropriationData, Money transactionAmountRemaining) {


        MonetaryCurrency currency = currentInstallment.getLoan().getCurrency();
        CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, loanTransaction.getLoan(), transactionDate);
        loanTransaction.updateLoanTransaction(principalAppropriationData, interestAppropriationData,currency);
        loanTransaction.setValueDate(Collectionutills.getValueDate(loanTransaction,currentInstallment));

        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                principalAppropriationData, interestAppropriationData, loanTransaction.getFeeChargesPortion(currency), loanTransaction.getPenaltyChargesPortion(currency),BigDecimal.ZERO);
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);
    }

    @Override
    public Money appropriationOnAdvanceAmount(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                              LoanHistoryRepo loanHistoryRepo, Money transactionAmountRemaining,
                                              PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData) {

        MonetaryCurrency currency = loan.getCurrency();

        AppUser appUser = Objects.requireNonNull(loan.getLoanTransactions().stream()
                .filter(transaction -> transaction.getAppUser().isSystemUser())
                .findAny().orElse(null)).getAppUser();

        Money selfDue = interestAppropriationData.selfInterest().add(principalAppropriationData.selfPrincipal());
        Money partnerDue = interestAppropriationData.partnerInterest().plus(principalAppropriationData.partnerPrincipal());
        loanTransaction.updateLoanTransaction(principalAppropriationData, interestAppropriationData,currency);
        loanTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());
        loanTransaction.setAdvanceAmountprocessed(transactionAmountRemaining.isGreaterThanZero() ? 0L : 1L );
        loanTransaction.setAmount(BigDecimal.ZERO);
        loanTransaction.setValueDate(Collectionutills.getValueDate(loanTransaction, currentInstallment));
        loanTransaction.setAppUser(appUser);
        loanTransaction.setEvent(transactionAmountRemaining.isGreaterThanZero() ? BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue()
                :BusinessEventNotificationConstants.BusinessEvents.PROCESSEDADVANCE.getValue());
        currentInstallment.setTotalPaidInAdvance(BigDecimal.ZERO);
        CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, loan, loanTransaction.getTransactionDate());

        Collectionutills.getNextInstallment(currentInstallment).setTotalPaidInAdvance(transactionAmountRemaining.getAmount());

        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                principalAppropriationData.principal(), interestAppropriationData.interest(), loanTransaction.getFeeChargesPortion(currency), loanTransaction.getPenaltyChargesPortion(currency), principalAppropriationData.selfPrincipal(),
                principalAppropriationData.partnerPrincipal(), interestAppropriationData.selfInterest(), interestAppropriationData.partnerInterest(),
                selfDue.getAmount(), partnerDue.getAmount(), BigDecimal.ZERO);
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);

        loan.addLoanTransaction(loanTransaction);


        return Money.zero(currency);
    }

    @Override
    public Money handleTransaction(LoanRepaymentScheduleInstallment currentInstallment, LoanTransaction currentTransaction,
                                   Money transactionAmount, LocalDate transactionDate, Loan loan, LoanHistoryRepo loanHistoryRepo,
                                   LoanAccrualWritePlatformService loanAccrualWritePlatformService) {

        MonetaryCurrency currency = loan.getCurrency();

        Money transactionAmountRemaining = transactionAmount;
        LoanProduct loanProduct = loan.getLoanProduct();

        BigDecimal overDuePercentage = null;
        BigDecimal overDueChargeSelfShare = null;
        overDuePercentage = Collectionutills.getOverDuePercentage(loanProduct.getLoanProductCharges());
        overDueChargeSelfShare = Collectionutills.getOverDueSelfShare(loanProduct.getLoanProductFeesCharges());

        Money penaltyChargesPortion = Money.zero(currency);
        BigDecimal selfSharePenaltyAmount = BigDecimal.ZERO;
        BigDecimal partnerSharePenaltyAmount = BigDecimal.ZERO;


        // Checking The Advance Scenario of The Transaction

        transactionAmountRemaining = checkForAdvanceAmount(transactionAmountRemaining, currentTransaction, loan, currentInstallment);

        InterestAppropriationData interestData = CollectionAppropriation.appropriateInterestOnRepayment(currentInstallment, transactionDate,
                transactionAmountRemaining,loanAccrualWritePlatformService);

        transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, interestData.interest());

        LoanChargeActions.reverseChargesOnRepayment(currentInstallment,transactionDate);

        PrincipalAppropriationData principalData = CollectionAppropriation.appropriatePrincipalOnRepayment(currentInstallment, transactionDate, transactionAmountRemaining);
        transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, principalData.principal());

        currentInstallment.reverseDaysPastDueOnInterestPrincipleCompletion(transactionDate);

        penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
        transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

        if (Objects.nonNull(overDuePercentage)) {
            selfSharePenaltyAmount = Collectionutills.calculatePenalSelfShare(overDueChargeSelfShare, penaltyChargesPortion);
            partnerSharePenaltyAmount = Collectionutills.calculatePenalPartnerShare(penaltyChargesPortion, selfSharePenaltyAmount);
            currentInstallment.updatePenaltySplit(selfSharePenaltyAmount, partnerSharePenaltyAmount);
            currentTransaction.updatePenaltyCharge(penaltyChargesPortion, selfSharePenaltyAmount, partnerSharePenaltyAmount);
        }
        Money due = Collectionutills.getDue(principalData,interestData,currency);
        if (currentTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue())
                && due.isGreaterThan(Money.zero(currency))) {
            transactionAmountRemaining = appropriationOnAdvanceAmount(loan, currentTransaction, currentInstallment, loanHistoryRepo, transactionAmountRemaining, principalData, interestData);
            LOG.debug("Appropriation Completed Before Due Date for Installemnt  {} ", currentInstallment.getInstallmentNumber());

        } else if (transactionDate.isBefore(currentInstallment.getDueDate()) && due.isGreaterThanZero()) {
            LoanTransaction transaction = appropriationOnBeforeDueDate(loan, currentTransaction, currentInstallment, loanHistoryRepo, principalData
                    , interestData,transactionAmountRemaining);
            if (transaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.toString())) {
                transactionAmountRemaining =Money.zero(currency);}
            Collectionutills.updateParentId(currentTransaction, transaction);
            loan.addLoanTransaction(transaction);
            LOG.debug("Appropriation Completed Before Due Date for Installemnt  {} ", currentInstallment.getInstallmentNumber());
        }
        else if (transactionDate.isEqual(currentInstallment.getDueDate()) && due.isGreaterThanZero()) {
            appropriationOnDueDate(loan, currentTransaction, currentInstallment, loanHistoryRepo, principalData, interestData,transactionAmountRemaining);
            currentTransaction.setAdvanceAmount( transactionAmountRemaining.getAmount());
            LOG.debug("Appropriation Completed On Due Date for Installemnt  {} ", currentInstallment.getInstallmentNumber());
        }
        else if (transactionDate.isAfter(currentInstallment.getDueDate()) && due.isGreaterThanZero()) {
            appropriationOnAfterDueDate(currentTransaction, currentInstallment, loanHistoryRepo, transactionDate, principalData, interestData,transactionAmountRemaining);
            currentTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());
            LOG.debug("Appropriation Completed After Due Date for Installemnt  {} ", currentInstallment.getInstallmentNumber());
        }
        return transactionAmountRemaining;
    }
}
