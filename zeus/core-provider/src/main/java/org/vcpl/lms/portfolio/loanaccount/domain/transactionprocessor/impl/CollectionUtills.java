package org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.impl;

import liquibase.pro.packaged.B;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.organisation.monetary.domain.MoneyHelper;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCharge;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionType;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesCharges;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public final class CollectionUtills {


    private  CollectionUtills (){
    }


    private static   final BigDecimal divisor =  BigDecimal.valueOf(100);

    public static Money calculateSelfInterest(Money interestPortion, LoanProduct loanProduct){

        BigDecimal selfPrincipalShares= BigDecimal.valueOf(loanProduct.getSelfPrincipalShare()).divide(divisor);
        return  interestPortion.multipliedBy(loanProduct.getSelfInterestRate().
                divide(loanProduct.getInterestRate())).multipliedBy(selfPrincipalShares);
    }
    public static Money  calculatePartnerInterest(Money interestPortion ,Money selfInterestPortion){
        return interestPortion.minus(selfInterestPortion);
    }
    public static   Money calculateSelfPrincipalPortion(Money principalPortion,LoanProduct loanProduct){
        return  principalPortion.multipliedBy(loanProduct.getSelfPrincipalShare()).dividedBy(divisor, RoundingMode.CEILING);
    }
    public static Money calculatePartnerPrincipalPortion(Money principalPortion , Money selfPrincipal  ){

        return  principalPortion.minus(selfPrincipal);
    }

    public static BigDecimal foreclosureSelfShare(LoanProduct loanProduct){
        return loanProduct.getLoanProductFeesCharges()
                .stream()
                .filter(loanProductFeesCharges -> loanProductFeesCharges.getCharge().isForeclosureCharge())
                .map(LoanProductFeesCharges::getSelfShare)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public static BigDecimal retrieveForeclosureGst(Loan loan){

        return  loan.getLoanCharges()
                .stream()
                .filter(LoanCharge::isForeclosureCharge)
                .map(LoanCharge::getTotalGst)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public static Boolean isInstallmentIsFullYPaid(LoanRepaymentScheduleInstallment currentInstallment, MonetaryCurrency currency){

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
    public static BigDecimal calculatePenalSelfShare(BigDecimal overDueChargeSelfShare,Money penaltyChargesPortion ){

        final MathContext mc = new MathContext(6, MoneyHelper.getRoundingMode());
        final BigDecimal numerator = overDueChargeSelfShare.divide(BigDecimal.valueOf(100));
        final BigDecimal penalSelfShare = penaltyChargesPortion.getAmount().multiply(numerator, mc);
        return penalSelfShare;
    }

    public static BigDecimal calculatePenalPartnerShare(Money penaltyChargesPortion,BigDecimal selfSharePenaltyAmount) {

       return penaltyChargesPortion.getAmount().subtract(selfSharePenaltyAmount);
    }

    public static Money getForeclosureAmount(Money transactionAmountRemaining, BigDecimal gstAtForeclosurecharge) {

        return transactionAmountRemaining.minus(gstAtForeclosurecharge);

    }

    public static BigDecimal getSelfForeclosureAmount(Money feeChargesPortion,BigDecimal foreClosureSelfShare ) {

        return feeChargesPortion.multipliedBy(foreClosureSelfShare).dividedBy(divisor,RoundingMode.CEILING).getAmount();
    }

    public static BigDecimal getPartnerForeclosureAmount(Money feeChargesPortion, BigDecimal selfFeeChargesPortion) {
        return feeChargesPortion.minus(selfFeeChargesPortion).getAmount();
    }

    public static BigDecimal addAmount(BigDecimal amount1,BigDecimal amount2) throws  ArithmeticException{
        return amount1.add(amount2);
    }


    public static Money subractAmount(Money transactionAmountRemaining, Money interestPortion) {

        return transactionAmountRemaining.minus(interestPortion);
    }
}
