package org.vcpl.lms.portfolio.collection.utills;

import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.organisation.monetary.domain.MoneyHelper;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.collection.data.InterestAppropriationData;
import org.vcpl.lms.portfolio.collection.data.PrincipalAppropriationData;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanproduct.domain.AdvanceAppropriationOn;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesCharges;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;

public class Collectionutills {
    private Collectionutills() {
    }

    private static final BigDecimal divisor = BigDecimal.valueOf(100);

    public static Money calculateSelfInterest(Money interestPortion, LoanProduct loanProduct) {

        BigDecimal selfPrincipalShares = BigDecimal.valueOf(loanProduct.getSelfPrincipalShare()).divide(divisor);
        return interestPortion.multipliedBy(loanProduct.getSelfInterestRate().
                divide(loanProduct.getInterestRate())).multipliedBy(selfPrincipalShares);
    }

    public static Money calculatePartnerInterest(Money interestPortion, Money selfInterestPortion) {
        return interestPortion.minus(selfInterestPortion);
    }

    public static Money calculateSelfPrincipalPortion(Money principalPortion, LoanProduct loanProduct) {
        return principalPortion.multipliedBy(loanProduct.getSelfPrincipalShare()).dividedBy(divisor, RoundingMode.CEILING);
    }

    public static Money calculatePartnerPrincipalPortion(Money principalPortion, Money selfPrincipal) {

        return principalPortion.minus(selfPrincipal);
    }

    public static Boolean isInstallmentIsFullYPaid(LoanRepaymentScheduleInstallment currentInstallment,
                                                   MonetaryCurrency currency) {

        return currentInstallment.getTotalOutstanding(currency).isZero();
    }

    public static BigDecimal getOverDuePercentage(List<Charge> charge) {

        return charge
                .stream()
                .filter(Charge::isOverdueInstallment)
                .map(Charge::getAmount)
                .findFirst().orElse(null);
    }

    public static BigDecimal getOverDueSelfShare(Set<LoanProductFeesCharges> loanProductFeesCharges) {

        return loanProductFeesCharges
                .stream()
                .filter(loanProductFeesCharge -> loanProductFeesCharge.getCharge().isOverdueInstallment())
                .map(LoanProductFeesCharges::getSelfShare)
                .findFirst().orElse(null);
    }

    public static BigDecimal calculatePenalSelfShare(BigDecimal overDueChargeSelfShare, Money penaltyChargesPortion) {

        final MathContext mc = new MathContext(6, MoneyHelper.getRoundingMode());
        final BigDecimal numerator = overDueChargeSelfShare.divide(BigDecimal.valueOf(100));

        return penaltyChargesPortion.getAmount().multiply(numerator, mc);
    }

    public static BigDecimal calculatePenalPartnerShare(Money penaltyChargesPortion, BigDecimal selfSharePenaltyAmount) {
        return penaltyChargesPortion.getAmount().subtract(selfSharePenaltyAmount);
    }


    public static <T extends Number> BigDecimal subract(T one, T two) {
        return BigDecimal.valueOf(one.doubleValue() - two.doubleValue());
    }


    public static BigDecimal getAdvanceAmount(LoanRepaymentScheduleInstallment currentInstallment, LoanTransaction loanTransaction,
                                              LocalDate transactionDate, BigDecimal transactionAmount) {

        BigDecimal due = loanTransaction.getInterestPortion().add(loanTransaction.getPrincipalPortion());

        if (transactionDate.isEqual(currentInstallment.getDueDate()) &&
                (transactionAmount.doubleValue() == due.doubleValue())) {

            return transactionAmount;
        } else if (transactionDate.isEqual(currentInstallment.getDueDate()) &&
                (transactionAmount.doubleValue() > due.doubleValue())) {
            return transactionAmount.subtract(due);

        } else if (transactionDate.isBefore(currentInstallment.getDueDate())
                && (transactionAmount.doubleValue() > due.doubleValue())) {
            return transactionAmount.subtract(due);
        } else if (transactionDate.isBefore(currentInstallment.getDueDate())
                && transactionAmount.doubleValue() == due.doubleValue()) {
            return transactionAmount;
        }

        return transactionAmount;
    }


    public static Date getValueDate(LoanTransaction loanTransaction, LoanRepaymentScheduleInstallment currentInstallment) {

        return loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate())
                || loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate()) ?
                Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static void updateParentId(LoanTransaction oldTransaction, LoanTransaction newLoanTransaction) {
        Long parentid = Objects.nonNull(oldTransaction.getId()) ? oldTransaction.getId() : null;
        newLoanTransaction.setParentId(parentid);

    }

    public static List<LoanTransaction> getAdvanceLoanTransaction(Loan loan, LoanTransaction loanTransaction) {

        List<LoanTransaction> repaymentsOrWaivers = new ArrayList<>();

        Collectionutills.getAdavanceOverPaidAmout(loan, repaymentsOrWaivers, loanTransaction);
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        if (!repaymentsOrWaivers.isEmpty()) {
            Collections.sort(repaymentsOrWaivers, transactionComparator);
        }
        return repaymentsOrWaivers;

    }

    public static LoanRepaymentScheduleInstallment getNextInstallment(LoanRepaymentScheduleInstallment currentInstallment) {

        Loan loan = currentInstallment.getLoan();
        return Boolean.TRUE.equals(currentInstallment.isLastInstallment(loan.getNumberOfRepayments(), currentInstallment.getInstallmentNumber()))
                ? currentInstallment : loan.fetchRepaymentScheduleInstallment(currentInstallment.getInstallmentNumber() + 1);
    }


    public static void getAdavanceOverPaidAmout(Loan loan, List<LoanTransaction> repaymentsOrWaivers, LoanTransaction currentTransaction) {

        Predicate<LoanTransaction> checkAdvance = loanTransaction ->
                Objects.nonNull(loanTransaction.getEvent()) && loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue())
                        && loanTransaction.getAdvanceAmount().doubleValue() > 0 && currentTransaction.getAppUser().isSystemUser();

        loan.getLoanTransactions().stream().filter(checkAdvance)
                .map(loanTransaction -> {
                    LoanTransaction transaction = LoanTransaction.copyTransactionProperties(loanTransaction);
                    transaction.resetDerivedComponents();
                    loanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.PROCESSEDADVANCE.getValue());
                    loanTransaction.setAdvanceAmountprocessed(1L);
                    transaction.setAmount(loanTransaction.getAdvanceAmount());
                    transaction.setParentId(loanTransaction.getId());
                    return transaction;
                })
                .forEach(repaymentsOrWaivers::add);
    }


    public static Money checkNextInstallment(Money transactionAmountRemaining, LoanTransaction loanTransaction,
                                             LoanRepaymentScheduleInstallment currentInstallemt) {

        LoanRepaymentScheduleInstallment nextInstallment = Collectionutills.getNextInstallment(currentInstallemt);

        if ((LocalDate.now().isBefore(nextInstallment.getDueDate())
                || Boolean.TRUE.equals(currentInstallemt.isLastInstallment(currentInstallemt.getLoan().getNumberOfRepayments(), currentInstallemt.getInstallmentNumber())))
                && AdvanceAppropriationOn.isOnDueDate(currentInstallemt.getLoan().getLoanProduct().getProductCollectionConfig().getAdvanceAppropriationOn()) && transactionAmountRemaining.isGreaterThanZero()) {
            loanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue());
            loanTransaction.setAdvanceAmount(transactionAmountRemaining.getAmount());
            Collectionutills.getNextInstallment(currentInstallemt).setTotalPaidInAdvance(transactionAmountRemaining.getAmount());
            return Money.zero(currentInstallemt.getLoan().getCurrency());
        }
        return transactionAmountRemaining;
    }

    public static Money subractAmount(Money transactionAmountRemaining, Money principalPortion) {
        return transactionAmountRemaining.minus(principalPortion);
    }


    public static Money getDue(PrincipalAppropriationData principalData, InterestAppropriationData interestData, MonetaryCurrency currency) {

        return Objects.nonNull(principalData) ? principalData.principal().add(Objects.nonNull(interestData)?interestData.interest():Money.zero(currency)): Money.zero(currency);
    }
}

