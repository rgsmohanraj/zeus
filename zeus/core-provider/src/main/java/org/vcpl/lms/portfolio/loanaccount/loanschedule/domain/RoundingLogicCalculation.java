package org.vcpl.lms.portfolio.loanaccount.loanschedule.domain;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class RoundingLogicCalculation {
    private RoundingLogicCalculation() {
    }

     public static BigDecimal roundCalculation(Integer value, BigDecimal amount, Integer scaleValue, RoundingMode roundingMode){

        BigDecimal emiRoundedValue  = BigDecimal.ZERO;
         switch (value){
             // round the value based on the rounding mode and the precision
             case 1:
                 emiRoundedValue = amount.setScale(scaleValue,roundingMode);
                 break;
                 // round the value  to the next nearest 10  based on the decimal value
             case 10:
                 emiRoundedValue = amount.setScale(-1,roundingMode);
                 break;
                 // round the value in to the next nearest 100  based on the decimal value
             case 100:
                 emiRoundedValue = amount.setScale(-2,roundingMode);
         }

         return  emiRoundedValue;
     }


     public static Double decimalRoundingRegex (double value , Integer regexRound){
        DecimalFormat df;
         Double amount;
         switch (regexRound){
            case 1:
                df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.FLOOR);
                amount = Double.parseDouble(df.format(value));
                break;
            case 2:
                df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.FLOOR);
                amount = Double.parseDouble(df.format(value));
                break;
            case 3:
                df = new DecimalFormat("#.###");
                df.setRoundingMode(RoundingMode.FLOOR);
                amount = Double.parseDouble(df.format(value));
                break;
             case 4:
                 df = new DecimalFormat("#.####");
                 df.setRoundingMode(RoundingMode.FLOOR);
                 amount = Double.parseDouble(df.format(value));
                 break;
            default:
                amount=  Double.valueOf(value);
        }

        return amount;

    }
}
