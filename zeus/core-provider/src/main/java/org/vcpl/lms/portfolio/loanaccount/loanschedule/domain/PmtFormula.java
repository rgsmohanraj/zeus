package org.vcpl.lms.portfolio.loanaccount.loanschedule.domain;


import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

final class PmtFormula {

     private PmtFormula(){

     }


    public static double pmtWithMonthlyInterest(final double interestRateFraction, final double numberOfPayments, final double principal,
                                                final double futureValue, final boolean type, LoanProduct loanProduct) {



         Integer value = loanProduct.getEmiDaysInMonth() / loanProduct.getEmiDaysInYear();


         double payment = 0;
        if (interestRateFraction == 0) {
            payment = -1 * (futureValue + principal) / numberOfPayments;
        } else {
            final double r1 = interestRateFraction + 1;
            payment = (futureValue + principal * Math.pow(r1, numberOfPayments)) * interestRateFraction * (value)
                    / ((type ? r1 : 1) * (1 - Math.pow(r1, numberOfPayments)));
        }
        return payment;
    }
    public static double pmtWithMonthlyYearlyInterest(final double interestRateFraction, final double numberOfPayments, final double principal,
                                                      final double futureValue, final boolean type) {
        double payment = 0;
        if (interestRateFraction == 0) {
            payment = -1 * (futureValue + principal) / numberOfPayments;
        } else {
            final double r1 = interestRateFraction + 1;
            payment = (futureValue + principal * Math.pow(r1, numberOfPayments)) * interestRateFraction
                    / ((type ? r1 : 1) * (1 - Math.pow(r1, numberOfPayments)));
        }
        return payment;
    }
}
