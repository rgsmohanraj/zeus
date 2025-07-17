package org.vcpl.lms.portfolio.collection.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.collection.utills.Collectionutills;
import org.vcpl.lms.portfolio.collection.data.InterestAppropriationData;
import org.vcpl.lms.portfolio.collection.data.PrincipalAppropriationData;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistory;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.vcpl.lms.portfolio.loanaccount.service.LoanAccrualWritePlatformService;
import org.vcpl.lms.portfolio.loanaccount.service.LoanChargeActions;
import org.vcpl.lms.portfolio.loanproduct.domain.AdvanceAppropriationOn;
import org.vcpl.lms.portfolio.loanproduct.domain.AdvanceAppropriationAgainstOn;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public sealed interface CollectionAppropriation permits
        HorizontalInterestPrincipalAppropriation, HorizontalInterestPrincipalChargesAppropriation, VerticalInterestPrincipalAppropriation {


    Logger LOG = LoggerFactory.getLogger(CollectionAppropriation.class);


    Money handleTransaction(LoanRepaymentScheduleInstallment currentInstallment, LoanTransaction currentTransaction,

                            Money transactionAmount, LocalDate transactionDate, Loan loan, LoanHistoryRepo loanHistoryRepo,
                            LoanAccrualWritePlatformService loanAccrualWritePlatformService);

    LoanTransaction appropriationOnBeforeDueDate(Loan loan, LoanTransaction loanTransaction,
                                                 LoanRepaymentScheduleInstallment currentInstallment, LoanHistoryRepo loanHistoryRepo,
                                                 PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData, Money transactionAmountRemaining);

    void appropriationOnDueDate(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                LoanHistoryRepo loanHistoryRepo, PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData, Money transactionAmountRemaing);

    void appropriationOnAfterDueDate(LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                     LoanHistoryRepo loanHistoryRepo, LocalDate transactionDate, PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData, Money transa);

    Money appropriationOnAdvanceAmount(Loan loan, LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment,
                                       LoanHistoryRepo loanHistoryRepo, Money transactionAmountRemaining, PrincipalAppropriationData principalAppropriationData, InterestAppropriationData interestAppropriationData);



    static void updateLoanHistory(LoanTransaction loanTransaction, LoanHistoryRepo historyRepo, LoanRepaymentScheduleInstallment currentInstallment,
                                  Loan loan, LocalDate transactionDate) {

        LOG.info("Update Loan History Started For Loan {}", loan.getId());
        loanTransaction.getLoan().setEvent("collection");
        Boolean isLastInstallment = currentInstallment.isLastInstallment(loanTransaction.getLoan().getNumberOfRepayments(), currentInstallment.getInstallmentNumber());
        Boolean installmentIsFullyPaid = Collectionutills.isInstallmentIsFullYPaid(currentInstallment, loan.getCurrency());

        LoanHistory loanHistory = new LoanHistory();
        LoanHistory loanHistories = historyRepo.getById(loanTransaction.getLoan().getId());
        loanHistory.setRev(loanHistories.getRev() + 1);

        BeanUtils.copyProperties(loanTransaction.getLoan(), loanHistory);
        loanHistory.setLoanId(loanTransaction.getLoan().getId());
        loanHistory.setPartnerId(loanTransaction.getLoan().getPartnerId());
        loanHistory.setClient(loanTransaction.getLoan().getClientId());
        loanHistory.setLoanProduct(loanTransaction.getLoan().getLoanProduct().getId());
        loanHistory.setInterestChargedFromDate(Date.from(loanTransaction.getLoan().getInterestChargedFromDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // For updating loanStatus in loanHistory table
        if (isLastInstallment && installmentIsFullyPaid &&
                loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.LOAN_MAKE_REPAYMENT.toString())){
            loanHistory.setLoanStatus(600);
        } else if (currentInstallment.isLastInstallment(loan.getRepaymentScheduleInstallments().size(), currentInstallment.getInstallmentNumber()) && installmentIsFullyPaid
                && loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.LOAN_FORECLOSURE.toString())) {
            loanHistory.setLoanStatus(710);
        } else if (currentInstallment.isLastInstallment(loan.getRepaymentScheduleInstallments().size(), currentInstallment.getInstallmentNumber()) && installmentIsFullyPaid
                && loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.COOLING_OFF.getValue())) {
            loanHistory.setLoanStatus(720);
        } else  {
            loanHistory.setLoanStatus(loan.getLoanStatus());}


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
        if (loan.getTransactionProcessingStrategy().isVerticalStrategy()) {
            loanHistory.setValueDate(Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else {
            loanHistory.setValueDate(loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate())
                    || loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate()) ? Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        LoanHistorySummary loanHistorySummary = new LoanHistorySummary(loan);
        BeanUtils.copyProperties(loan.getSummary(), loanHistorySummary);
        loanHistorySummary.updateLoanArrearAgeing(loanTransaction.getLoan());
        loanHistorySummary.updateSummaryForLoanHistory(loan.getCurrency(), loanTransaction.getLoan().getPrincpal(),
                loanTransaction.getLoan().getRepaymentScheduleInstallments(), Objects.isNull(loan.getLoanSummaryWrapper()) ? new LoanSummaryWrapper():loan.getLoanSummaryWrapper(), loanTransaction.getLoan().isDisbursed(), loanTransaction.getLoan().charges(),
                loanTransaction.getLoan().getSelfPrincipaAmount(), loanTransaction.getLoan().getPartnerPrincipalAmount());
        loanHistory.setSummary(loanHistorySummary);
        if ((Boolean.TRUE.equals(currentInstallment.isLastInstallment(loan.getRepaymentScheduleInstallments().size(), currentInstallment.getInstallmentNumber())))
                || loanHistories.getSummary().getTotalPrincipalOutstanding().doubleValue() != loanHistory.getSummary().getTotalPrincipalOutstanding().doubleValue()
                || loanHistories.getSummary().getTotalInterestOutstanding().doubleValue() != loanHistory.getSummary().getTotalInterestOutstanding().doubleValue()) {
            historyRepo.saveAndFlush(loanHistory);

            LOG.info("Update Loan History Ended For Loan {}", loan.getId());

        }
    }


    default Money checkForAdvanceAmount(Money transactionAmountRemaining, LoanTransaction loanTransaction,
                                        Loan loan, LoanRepaymentScheduleInstallment currentInstallemt) throws ArithmeticException {

        MonetaryCurrency currency = loan.getCurrency();
        LoanProduct loanProduct = loan.getLoanProduct();
        LoanRepaymentScheduleInstallment nextInstallment = Collectionutills.getNextInstallment(currentInstallemt);
        if (Boolean.TRUE.equals(currentInstallemt.isLastInstallment(loan.getNumberOfRepayments(), currentInstallemt.getInstallmentNumber())
                && transactionAmountRemaining.getAmount().doubleValue() > currentInstallemt.getTotalOutstanding(loan.getCurrency()).getAmount().doubleValue())) {
            final String errorMessage = "The transaction amount " + loan.getLoanSummary().getTotalOutstanding() + " cannot be greater than the total outstanding amount " + loanTransaction.getAmount();
            throw new InvalidLoanTransactionTypeException("transaction", "cannot.be.greater.than.total.outstanding.amount {}", errorMessage);
        }

        // checking the LoanTransaction date if Future Date  or Not If It is Future Date Making The Transaction Amount as Zero
        if (LocalDate.now().isBefore(currentInstallemt.getDueDate()) && (Objects.nonNull(nextInstallment) || currentInstallemt.isLastInstallment(loan.getNumberOfRepayments(), currentInstallemt.getInstallmentNumber()))
                && AdvanceAppropriationOn.isOnDueDate(loanProduct.getProductCollectionConfig().getAdvanceAppropriationOn())) {
            loanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue());
            currentInstallemt.updateAdvanceAmount(currentInstallemt.getTotalPaidInAdvance(currency).getAmount().add(transactionAmountRemaining.getAmount()));
            loanTransaction.setAdvanceAmount(loanTransaction.getAdvanceAmount().doubleValue() == 0 ? transactionAmountRemaining.getAmount() : loanTransaction.getAdvanceAmount(currency).getAmount());
            loanTransaction.setValueDate(Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            loanTransaction.setAdvanceAmountprocessed(0L);
            if (Objects.nonNull(loanTransaction.getParentId())) {
                loan.getLoanTransactions().stream().filter(transaction -> Objects.nonNull(transaction.getId())
                                && transaction.getId().equals(loanTransaction.getParentId()))
                        .forEach(loanTransactions -> {
                            loanTransactions.setEvent((BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue()));
                            loanTransactions.setAdvanceAmountprocessed(0L);
                        });
            }
            loanTransaction.setParentId(null);
            if(loanTransaction.getAmount().doubleValue() == transactionAmountRemaining.getAmount().doubleValue()){
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping
                    .createFrom(loanTransaction,currentInstallemt,Money.zero(currency),Money.zero(currency),
            loanTransaction.getFeeChargesPortion(currency),loanTransaction.getPenaltyChargesPortion(currency),loanTransaction.getSelfPrincipalPortion(currency),loanTransaction.getPartnerPrincipalPortion(currency),
            loanTransaction.getSelfInterestPortion(currency),loanTransaction.getPartnerInterestPortion(currency),loanTransaction.getSelfDue(),loanTransaction.getPartnerDue(),transactionAmountRemaining.getAmount());
            loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping,loanTransaction);
            }
            LOG.info("Advance Amount Is Parked For Loan {}", loan.getId());
            return Money.zero(currency);
        }
        return transactionAmountRemaining;
    }


    static InterestAppropriationData appropriateInterestOnRepayment(LoanRepaymentScheduleInstallment installment, LocalDate transactionDate,
                                                         Money transactionAmountRemaining, LoanAccrualWritePlatformService loanAccrualWritePlatformService) {

        InterestAppropriationData interestAppropriationData = appropriateInterest(installment,transactionDate,transactionAmountRemaining);

        // Reversing Accrual On Value date short or full payment
        loanAccrualWritePlatformService.reverseOnRepayment(installment.getLoan().getId(),installment,transactionDate,interestAppropriationData);

        return interestAppropriationData;
    }

    static InterestAppropriationData appropriateInterest(LoanRepaymentScheduleInstallment installment, LocalDate transactionDate,
                                                         Money transactionAmountRemaining) {

        LoanProduct loanProduct = installment.getLoan().getLoanProduct();
        final Money interestPortion = installment.payInterestComponent(transactionDate, transactionAmountRemaining);

        final Money selfInterestPortion = Collectionutills.calculateSelfInterest(interestPortion, loanProduct);
        installment.paySelfInterestComponent(transactionDate, transactionAmountRemaining);

        final Money partnerInterestPortion = Collectionutills.calculatePartnerInterest(interestPortion, selfInterestPortion);
        installment.payPartnerInterestComponent(transactionDate, partnerInterestPortion);

        return new InterestAppropriationData(interestPortion, selfInterestPortion, partnerInterestPortion);
    }


    static PrincipalAppropriationData appropriatePrincipalOnRepayment(LoanRepaymentScheduleInstallment installment, LocalDate transactionDate,
                                                           Money transactionAmountRemaining) {
        return appropriatePrincipal(installment,transactionDate,transactionAmountRemaining);
    }

    static PrincipalAppropriationData appropriatePrincipal(LoanRepaymentScheduleInstallment installment, LocalDate transactionDate,
                                                           Money transactionAmountRemaining) {

        LoanProduct loanProduct = installment.getLoan().getLoanProduct();

        final Money principalPortion = installment.payPrincipalComponent(transactionDate, transactionAmountRemaining);

        final Money selfPrincipalPortion = Collectionutills.calculateSelfPrincipalPortion(principalPortion, loanProduct);
        installment.paySelfPrincipalComponent(transactionDate, selfPrincipalPortion);

        final Money partnerPrincipalPortion = Collectionutills.calculatePartnerPrincipalPortion(principalPortion, selfPrincipalPortion);
        installment.payPartnerPrincipalComponent(transactionDate, partnerPrincipalPortion);

        return new PrincipalAppropriationData(principalPortion, selfPrincipalPortion, partnerPrincipalPortion);

    }

    default Money advanceAppropriationOnInterestAndPrincipal(LocalDate transactionDate, Money transactionAmount, LoanTransaction loanTransaction,
                                                             Loan loan, LoanHistoryRepo loanHistoryRepo,LoanAccrualWritePlatformService loanAccrualWritePlatformService) {

        LOG.info("Same Date Advance Appropriation Started");
        MonetaryCurrency currency = loan.getCurrency();
        LoanProduct loanProduct = loan.getLoanProduct();
        Money transactionAmountRemaining = transactionAmount;
        InterestAppropriationData interestData = null;
        PrincipalAppropriationData principalData = null;
        LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);
        loanTransaction.setValueDate(Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        newLoanTransaction.resetDerivedComponents();

        for (LoanRepaymentScheduleInstallment currentInstallment : loan.getRepaymentScheduleInstallments()) {
            if (transactionAmountRemaining.isZero()) {
                break;
            }
            LoanRepaymentScheduleInstallment nextInstallment = Collectionutills.getNextInstallment(currentInstallment);
            if ((Objects.nonNull(nextInstallment)
                    || currentInstallment.isLastInstallment(loan.getNumberOfRepayments(), currentInstallment.getInstallmentNumber()))
                    && ! AdvanceAppropriationOn.isOnDueDate(loanProduct.getProductCollectionConfig().getAdvanceAppropriationOn())
                    && Boolean.TRUE.equals(AdvanceAppropriationAgainstOn.interestAndPrincipal(AdvanceAppropriationAgainstOn.getInt(loanProduct.getProductCollectionConfig()
                    .getAdvanceAppropriationAgainstOn()))) && transactionAmountRemaining.isGreaterThanZero()
                    && !currentInstallment.isObligationsMet()) {

                LOG.info("Advance Appropriation Against On Interest And Principal For Loan {}", loan.getId());

                interestData = appropriateInterestOnRepayment(currentInstallment, transactionDate, transactionAmountRemaining, loanAccrualWritePlatformService);

                transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, interestData.interest());

                principalData = appropriatePrincipalOnRepayment(currentInstallment, transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, principalData.principal());

            } else if (transactionDate.isBefore(currentInstallment.getDueDate()) && transactionAmountRemaining.isGreaterThanZero()
                    && Boolean.FALSE.equals(AdvanceAppropriationAgainstOn.interestAndPrincipal(AdvanceAppropriationAgainstOn.getInt(loanProduct.getProductCollectionConfig().getAdvanceAppropriationAgainstOn())))
                    && !currentInstallment.isObligationsMet()) {

                LOG.info("Advance Appropriation Against On Principal For Loan {}", loan.getId());
                principalData = appropriatePrincipalOnRepayment(currentInstallment, transactionDate, transactionAmountRemaining);

                transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, principalData.principal());

                // Allowing To Appropriate the Interest Amount For Last Repayment If Transaction Amount Is More Than Principal Amount
               /* if (Boolean.TRUE.equals(currentInstallment.isLastInstallment(loan.getNumberOfRepayments(), currentInstallment.getInstallmentNumber()))) {
                    interestData = Collectionappropriation.appropriateInterest(currentInstallment, transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = Collectionutills.subractAmount(transactionAmountRemaining, interestData.interest());
                }*/
            }

            LoanChargeActions.reverseChargesOnRepayment(currentInstallment,transactionDate);
            currentInstallment.reverseDaysPastDueOnInterestPrincipleCompletion(transactionDate);
            Money due = Collectionutills.getDue(principalData, interestData, currency);
            if (transactionDate.isBefore(currentInstallment.getDueDate()) && due.isGreaterThanZero()) {
                updateLoanTransactionForAdvance(currentInstallment,principalData, interestData, loanHistoryRepo,loanProduct.getProductCollectionConfig().isAdvanceEntryEnabled() ? newLoanTransaction :loanTransaction ,
                        transactionDate);
                newLoanTransaction.setAmount(BigDecimal.ZERO);
                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment, principalData,
                        null, loanTransaction.getFeeChargesPortion(currency), loanTransaction.getPenaltyChargesPortion(currency), BigDecimal.ZERO);
                newLoanTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, loanTransaction);
                LOG.debug("Appropriation Completed After Due Date for Installment  {} ", nextInstallment.getInstallmentNumber());

            }
        }
        if (newLoanTransaction.getInterestPortion(currency).plus(newLoanTransaction.getPrincipalPortion(currency)).isGreaterThanZero()) {
            loan.addLoanTransaction(newLoanTransaction);
        }
        return transactionAmountRemaining;
    }


    default void updateLoanTransactionForAdvance(LoanRepaymentScheduleInstallment currentInstallment,PrincipalAppropriationData principalAppropriationData,
                                                 InterestAppropriationData interestAppropriationData,LoanHistoryRepo loanHistoryRepo,LoanTransaction loanTransaction,LocalDate transactionDate ){

        MonetaryCurrency currency = currentInstallment.getLoan().getCurrency();
        loanTransaction.setValueDate(Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        // passing Null  to Update LoanTransaction already Interest Portion Updated
        loanTransaction.updateLoanTransaction(principalAppropriationData, interestAppropriationData, currency);
        CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, loanTransaction.getLoan(), transactionDate);
    }

}

