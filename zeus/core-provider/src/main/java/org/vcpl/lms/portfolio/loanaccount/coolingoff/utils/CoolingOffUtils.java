package org.vcpl.lms.portfolio.loanaccount.coolingoff.utils;

import org.vcpl.lms.portfolio.common.domain.DaysInYearType;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.vcpl.lms.portfolio.loanproduct.domain.CoolingOffInterestLogicApplicability;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class CoolingOffUtils {
    private static final BigDecimal divisor = BigDecimal.valueOf(100);
    private CoolingOffUtils() {
    }

    public static int getNumberOfDays(Loan loan, LocalDate transactionDate) {
        return Math.toIntExact(ChronoUnit.DAYS.between(loan.getDisbursementDate(), transactionDate));
    }

    public static BigDecimal calculateCoolingOffInterest(Loan loan, int days,LocalDate transactionDate) throws ArithmeticException {

        BigDecimal coolingOffInterest = BigDecimal.ZERO;
        LoanProduct loanProduct = loan.getLoanProduct();

        switch (CoolingOffInterestLogicApplicability.fromInt(loan.getLoanProduct().getProductCollectionConfig().getCoolingOffInterestLogicApplicability())) {
            case PNR ->{
                // calculating the coolingOff interest using PNR formula
                /*  pos *  interestRate * days / daysInYear  */

                double interestRate = loanProduct.getNominalInterestRatePerPeriod().doubleValue() / divisor.doubleValue();

                int daysInYear = DaysInYearType.fromInt(loan.getLoanProduct().getProductCollectionConfig().getCoolingOffDaysInYear()).isActual()
                        ? transactionDate.lengthOfYear() : loan.getLoanProduct().getProductCollectionConfig().getCoolingOffDaysInYear();

                coolingOffInterest = BigDecimal.valueOf((loan.getLoanSummary().getTotalPrincipalOutstanding().doubleValue() * interestRate  * days) /
                        (daysInYear)).setScale(loan.getLoanProduct().getProductCollectionConfig().getCoolingOffRoundingDecimals(),
                        loan.getLoanProduct().getProductCollectionConfig().getCoolingOffRoundingMode());
            }
            case MAX -> { // need to implement
            }
            case INVALID -> {
                final String errorMessage = "cooling off interest Calculation must be PNR or MAX ";
                throw new InvalidLoanTransactionTypeException("coolingOff", "cooling off interest Calculation must be PNR Or MAX", errorMessage);

            }

        }

        return coolingOffInterest;

    }
    public static Date LocalDateToDate(LocalDate localDate){
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static BigDecimal selfCoolingOffInterest(BigDecimal coolingOffInterest , LoanProduct loanProduct){
        if(coolingOffInterest.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(coolingOffInterest.doubleValue() * (loanProduct.getSelfPrincipalShare().doubleValue() / divisor.doubleValue()))
                .setScale(loanProduct.getProductCollectionConfig().getCoolingOffRoundingDecimals(),loanProduct.getProductCollectionConfig().getCoolingOffRoundingMode());
    }
    public static BigDecimal partnerCoolingOffInterest(BigDecimal coolingOffInterest , LoanProduct loanProduct){
        if(coolingOffInterest.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(coolingOffInterest.doubleValue() * (loanProduct.getPartnerPrincipalShare().doubleValue() / divisor.doubleValue()))
                .setScale(loanProduct.getProductCollectionConfig().getCoolingOffRoundingDecimals(),loanProduct.getProductCollectionConfig().getCoolingOffRoundingMode());
    }

    public static BigDecimal selfCoolingOffPrincipal(BigDecimal coolingOffPrincipal , LoanProduct loanProduct){
        if(coolingOffPrincipal.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(coolingOffPrincipal.doubleValue() * (loanProduct.getSelfPrincipalShare().doubleValue() / divisor.doubleValue()))
                .setScale(loanProduct.getProductCollectionConfig().getCoolingOffRoundingDecimals(),loanProduct.getProductCollectionConfig().getCoolingOffRoundingMode());
    }


    public static BigDecimal partnerCoolingOffPrincipal(BigDecimal coolingOffPrincipal , LoanProduct loanProduct){
        if(coolingOffPrincipal.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(coolingOffPrincipal.doubleValue() * (loanProduct.getPartnerPrincipalShare().doubleValue() / divisor.doubleValue()))
                .setScale(loanProduct.getProductCollectionConfig().getCoolingOffRoundingDecimals(),loanProduct.getProductCollectionConfig().getCoolingOffRoundingMode());
    }
}
