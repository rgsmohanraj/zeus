package org.vcpl.lms.portfolio.collection.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.collection.LoanRepaymentScheduleTransactionProcessorGeneric;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.service.LoanAccrualWritePlatformService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class CollectionService {
    private final LoanAccrualWritePlatformService loanAccrualWritePlatformService;
    Logger LOG = LoggerFactory.getLogger(CollectionService.class);

    public void appropriateInstallment(Loan loan, LocalDate transactionDate, List<LoanTransaction> loanTransaction, LoanHistoryRepo loanHistoryRepo) {
        /* // re-process loan charges over repayment periods (picking up on waived// loan charges)
        final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(currency, disbursementDate, loan.getRepaymentScheduleInstallments(), loan.getCharges())*/

        loanTransaction.stream().filter(transaction -> transaction.isRepaymentType() || transaction.isRecoveryRepayment())
                .forEach(transaction -> makeRepayment(transaction, loan, transactionDate, loanHistoryRepo));
    }

    private void makeRepayment(LoanTransaction transaction, Loan loan, LocalDate transactionDate, LoanHistoryRepo loanHistoryRepo) {

        MonetaryCurrency currency = loan.getCurrency();
        CollectionAppropriation repaymentStrategy = null;
        try {
             repaymentStrategy = LoanRepaymentScheduleTransactionProcessorGeneric.determineStrategy(loan.getTransactionProcessingStrategy());
        } catch (Exception e ) {
            LOG.error("Exception Occurs At Object Creation Of Strategy {}" ,e.getMessage());
        }
        if(Objects.isNull(repaymentStrategy)){
            LOG.error("Repayment Strategy is Null For Loan {}",loan.getId());
        }
        LOG.info("RePayment Strategy Selected For Loan {} is {}", loan.getId(), loan.getTransactionProcessingStrategy());
        Money transactionUnProcessed = transaction.getAmount(loan.getCurrency());
        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            installment.updateDerivedFields(currency, loan.getDisbursementDate());

            if (transactionUnProcessed.isGreaterThanZero() && !installment.isObligationsMet() && Objects.nonNull(repaymentStrategy)) {
                transactionUnProcessed = repaymentStrategy.handleTransaction(installment, transaction, transactionUnProcessed, transactionDate, loan, loanHistoryRepo,loanAccrualWritePlatformService);
                LOG.info("Loan {} paid for Installment {}  ", transaction.getLoan().getId(), installment.getInstallmentNumber());
            }
            LOG.debug("Appropriation Completed For Loan {}", loan.getId());
        }
    }

}
