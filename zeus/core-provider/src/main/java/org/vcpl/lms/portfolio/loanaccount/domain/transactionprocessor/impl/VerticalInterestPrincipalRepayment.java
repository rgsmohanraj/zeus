package org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VerticalInterestPrincipalRepayment extends AbstractLoanRepaymentScheduleTransactionProcessor implements Collection {
    private static final Logger LOG = LoggerFactory.getLogger(VerticalInterestPrincipalRepayment.class);
    @Override
    protected Money handleTransactionThatIsALateRepaymentOfInstallment(LoanRepaymentScheduleInstallment currentInstallment, List<LoanRepaymentScheduleInstallment> installments, LoanTransaction loanTransaction,
                                                                       Money transactionAmountUnprocessed, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Money transactionSelfAmountUnprocessed,
                                                                       Money transactionPartnerAmountUnprocessed, List<CollectionReport> collectionReports, LoanHistoryRepo loanHistoryRepo, List<LoanTransaction> listOfLoanTransaction,
                                                                       LoanTransaction lateLoantransaction, LoanTransaction advanceLoanTransaction) {


        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        LoanProduct loanProduct = currentInstallment.getLoan().getLoanProduct();
        final Loan loan = loanTransaction.getLoan();

        LOG.info("LOAN {} INITIATED FOR VERTICAL APPROPRIATION ",loan.getId());

        final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
        List<LoanTransaction> transactions = new ArrayList<>();

        final LoanRepaymentScheduleInstallment currentInstallmentBasedOnTransactionDate = nearestInstallment(
                loanTransaction.getTransactionDate(), installments);

        transactionAmountRemaining = this.verticalInterestAppropriation(installments,currency,transactionAmountRemaining,loanTransaction,loanProduct,
               transactionDate,transactions,currentInstallmentBasedOnTransactionDate,loan,listOfLoanTransaction,loanHistoryRepo);

        transactionAmountRemaining =  this.verticalPrincipalAppropriation(currency,transactionAmountRemaining,loanProduct,
                transactionDate,installments,transactions,loan,listOfLoanTransaction,advanceLoanTransaction,
                loanHistoryRepo,lateLoantransaction,loanTransaction);

        if(!loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue())){
            advanceLoanTransaction.setAmount(loanTransaction.getAmount());
            loan.updateLoanTransaction(loanTransaction);}

        loan.getLoanTransactions().addAll(transactions);

        return transactionAmountRemaining;
    }


    public LoanRepaymentScheduleInstallment nearestInstallment(LocalDate transactionDate, List<LoanRepaymentScheduleInstallment> installments) {
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
    @Override
    protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(LoanRepaymentScheduleInstallment currentInstallment, List<LoanRepaymentScheduleInstallment> installments, LoanTransaction loanTransaction, LocalDate transactionDate,
                                                                         Money paymentInAdvance, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Money selfPaymentInAdvance, Money partnerPaymentInAdvance, List<CollectionReport> collectionReports,
                                                                         LoanHistoryRepo loanHistoryRepo, List<LoanTransaction> listOfTransaction, LoanTransaction lateLoanTransaction, LoanTransaction advanceLoanTransaction) {


        return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, paymentInAdvance,
                transactionMappings,selfPaymentInAdvance,partnerPaymentInAdvance,collectionReports,loanHistoryRepo,
                listOfTransaction,lateLoanTransaction,advanceLoanTransaction);
    }

    @Override
    protected Money handleTransactionThatIsOnTimePaymentOfInstallment(LoanRepaymentScheduleInstallment currentInstallment, LoanTransaction loanTransaction, Money transactionAmountUnprocessed,
                                                                      List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Money transactionSelfAmountUnprocessed, Money transactionPartnerAmountUnprocessed,
                                                                      List<CollectionReport> collectionReports, LoanHistoryRepo loanHistoryRepo, List<LoanTransaction> listOfLoanTransaction, LoanTransaction lateLoantransaction,
                                                                      LoanTransaction advanceLoanTransaction) {

        return handleTransactionThatIsALateRepaymentOfInstallment(currentInstallment, loanTransaction.getLoan().getRepaymentScheduleInstallments(), loanTransaction,
                 transactionAmountUnprocessed, transactionMappings,transactionSelfAmountUnprocessed, transactionPartnerAmountUnprocessed,collectionReports, loanHistoryRepo,  listOfLoanTransaction,
                lateLoantransaction, advanceLoanTransaction);
    }


    @Override
    protected Money handleRefundTransactionPaymentOfInstallment(LoanRepaymentScheduleInstallment currentInstallment, LoanTransaction loanTransaction,
                                                                Money transactionAmountUnprocessed, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {
        return null;
    }
}
