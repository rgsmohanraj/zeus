package org.vcpl.lms.portfolio.loanaccount.domain.xirr;

import org.vcpl.lms.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCharge;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class XirrService {
    /**
     * @Author Doni Sharmila
     * XIRR Logic has been implemented at Disbursement, repayment level
     */
    public static BigDecimal xirrCalculation(final List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallment, BigDecimal disbursalAmount,
                                             final LocalDate disbursementDate, final MonetaryCurrency currency, final Collection<LoanCharge> loanCharges,
                                             List<LoanTransaction> loanTransactions, Loan loan) {


        disbursalAmount = disbursalAmount.subtract(loanCharges.stream().filter(loanCharge -> Objects.nonNull(loanCharge) &&
                loanCharge.isProcessingFee(loanCharge.getCharge())).map(LoanCharge::amount).reduce(BigDecimal.ZERO,BigDecimal::add));

        double xirrCalc = 0;

        // creating the transaction with the negative disbursal Amount

        List<XirrTransaction> transaction = new ArrayList<>();
        transaction.add(new XirrTransaction(disbursalAmount.negate().doubleValue(),disbursementDate));

        loanTransactions.stream().
                filter(loanTransaction-> (loanTransaction.isNotReversed() && (!loanTransaction.isDisbursement()
                        || loanTransaction.isNonMonetaryTransaction())))
                .forEach(trans->{
                    Money dueAmount = trans.getPrincipalPortion(currency).add(trans.getInterestPortion(currency));
                    transaction.add(new XirrTransaction(dueAmount.getAmount().doubleValue(),trans.getTransactionDate()));
                });

       /* for(LoanRepaymentScheduleInstallment repaymentScheduleInstallment :loanRepaymentScheduleInstallment ){
            if(loan.getLoanStatus() == 300 && loanTransactions.isEmpty()){
                Money dueAmount =  repaymentScheduleInstallment.getPrincipal(currency).add(repaymentScheduleInstallment.getInterestCharged(currency));
                transaction.add(new XirrTransaction(dueAmount.getAmount().doubleValue(),repaymentScheduleInstallment.getDueDate()));
            }
            else {
                if(repaymentScheduleInstallment.getInterestPaid().add(repaymentScheduleInstallment.getPrincipalCompleted()).doubleValue() ==0){
                    Money dueAmount =  repaymentScheduleInstallment.getPrincipal(currency).add(repaymentScheduleInstallment.getInterestCharged(currency));
                    transaction.add(new XirrTransaction(dueAmount.getAmount().doubleValue(),repaymentScheduleInstallment.getDueDate()));}
            }
        }*/
        loanRepaymentScheduleInstallment.forEach(repaymentScheduleInstallment-> {
            if(loan.getLoanStatus() == 300 && loanTransactions.isEmpty()){
                Money dueAmount =  repaymentScheduleInstallment.getPrincipal(currency).add(repaymentScheduleInstallment.getInterestCharged(currency));
                transaction.add(new XirrTransaction(dueAmount.getAmount().doubleValue(),repaymentScheduleInstallment.getDueDate()));
            }else {
                if(repaymentScheduleInstallment.getInterestPaid().add(repaymentScheduleInstallment.getPrincipalCompleted()).doubleValue()==0){
                    Money dueAmount =  repaymentScheduleInstallment.getPrincipal(currency).add(repaymentScheduleInstallment.getInterestCharged(currency));
                    transaction.add(new XirrTransaction(dueAmount.getAmount().doubleValue(),repaymentScheduleInstallment.getDueDate()));}
            }
        });
        try{
            xirrCalc = Xirr.builder().withTransactions(transaction).xirr() * 100;}
        catch (Exception e){
            throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.xirr.has.processing .issue",
                    "Note: Processing Fee amount greater than half of the loan amount ");}
        return BigDecimal.valueOf(xirrCalc);
    }
}
