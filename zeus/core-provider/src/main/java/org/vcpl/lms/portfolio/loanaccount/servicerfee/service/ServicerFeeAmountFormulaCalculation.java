package org.vcpl.lms.portfolio.loanaccount.servicerfee.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCharge;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.Enum.ServicerFeeChargesRatio;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.domain.ServicerFeeChargesConfig;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;

public class ServicerFeeAmountFormulaCalculation {


    private static final Logger LOG = LoggerFactory.getLogger(ServicerFeeAmountFormulaCalculation.class);
    public static BigDecimal divisor = BigDecimal.valueOf(100);

    /*ServicerFee Interest Calculation*/
    public static BigDecimal vclInterestAmt(BigDecimal selfInterestPortionDerived, BigDecimal vclHurdleRate, BigDecimal overAllInterestRate, Integer vclInterestDecimal, String vclInterestRound) throws ArithmeticException {
        return  BigDecimal.valueOf(selfInterestPortionDerived.doubleValue() * (vclHurdleRate.doubleValue() / (divisor.doubleValue())) / (overAllInterestRate.doubleValue() / divisor.doubleValue())).setScale(vclInterestDecimal, RoundingMode.valueOf(vclInterestRound));
    }

    public static BigDecimal sfInterestBaseAmount(BigDecimal selfInterestPortionDerived, BigDecimal vclInterestAmt) throws ArithmeticException {
        return selfInterestPortionDerived.subtract(vclInterestAmt);
    }
    public static BigDecimal sfInterestGstLossAmount(BigDecimal sfInterestBaseAmt,BigDecimal sfBaseAmtGstLoss,Integer vclInterestDecimal,String vclInterestRound) throws ArithmeticException {
        return BigDecimal.valueOf(sfInterestBaseAmt.doubleValue() * (1/((divisor.doubleValue() + sfBaseAmtGstLoss.doubleValue())/divisor.doubleValue()))).setScale(vclInterestDecimal,RoundingMode.valueOf(vclInterestRound));
    }
    public static BigDecimal sfInterestGstAmount(BigDecimal sfInterestGstLossAmount,BigDecimal sfGst, Integer sfGstDecimal, String sfGstRound) throws ArithmeticException {
        return sfInterestGstLossAmount.multiply(sfGst.divide(divisor)).setScale(sfGstDecimal, RoundingMode.valueOf(sfGstRound));
    }
    public static BigDecimal sfInterestInvoiceAmount(BigDecimal sfInterestGstLossAmount,BigDecimal sfInterestGstAmount,Integer servicerFeeDecimal, String servicerFeeRound) throws ArithmeticException {
        return BigDecimal.valueOf(sfInterestGstLossAmount.doubleValue() +sfInterestGstAmount .doubleValue()).setScale(servicerFeeDecimal, RoundingMode.valueOf(servicerFeeRound));
    }

    /*SFChargeCalculation*/

    public static BigDecimal sfSelfBaseAmt(BigDecimal sfChargeAmount, BigDecimal sfSelfShare, Integer sfChargeBaseAmountDecimal, String sfChargeBaseAmountRoundingmode) throws ArithmeticException {
        return  BigDecimal.valueOf(sfChargeAmount.doubleValue() * (sfSelfShare.doubleValue() / (divisor.doubleValue()))).setScale(sfChargeBaseAmountDecimal,RoundingMode.valueOf(sfChargeBaseAmountRoundingmode));
    }
    public static BigDecimal sfSelfBaseAmtDynamic(BigDecimal loanAmount, BigDecimal sfSelfShare, Integer sfChargeBaseAmountDecimal, String sfChargeBaseAmountRoundingmode) throws ArithmeticException {
        return  BigDecimal.valueOf(loanAmount.doubleValue() * (sfSelfShare.doubleValue() / (divisor.doubleValue()))).setScale(sfChargeBaseAmountDecimal,RoundingMode.valueOf(sfChargeBaseAmountRoundingmode));
    }
    public  static BigDecimal sfPartnerBaseAmt(BigDecimal sfChargeAmount,BigDecimal sfSelfbaseAmt) throws ArithmeticException {
        return BigDecimal.valueOf(sfChargeAmount.doubleValue() - sfSelfbaseAmt.doubleValue());
    }

    public static BigDecimal sfChargeGstLossAmount(BigDecimal sfPartnerBaseAmt,BigDecimal sfChargeAmtGstLoss,Integer sfChargeBaseAmountDecimal,String sfChargeBaseAmountRoundingmode) throws ArithmeticException {
        return BigDecimal.valueOf(sfPartnerBaseAmt.doubleValue() * (1/((divisor.doubleValue() + sfChargeAmtGstLoss.doubleValue())/divisor.doubleValue()))).setScale(sfChargeBaseAmountDecimal,RoundingMode.valueOf(sfChargeBaseAmountRoundingmode));
    }
    public static BigDecimal sfChargeGstAmount(BigDecimal sfChargeGstLossAmount,BigDecimal sfChargeGst,Integer sfChargeGstDecimal,String sfChargeGstRoundingmode) throws ArithmeticException {
        return sfChargeGstLossAmount.multiply(sfChargeGst.divide(divisor)).setScale(sfChargeGstDecimal,RoundingMode.valueOf(sfChargeGstRoundingmode));
    }
    public static BigDecimal sfChargeInvoiceAmount(BigDecimal sfChargeGstLossAmount,BigDecimal sfChargeGstAmount,Integer sfChargeDecimal,String sfChargeRound) throws ArithmeticException {
        return BigDecimal.valueOf(sfChargeGstLossAmount.doubleValue() +sfChargeGstAmount .doubleValue()).setScale(sfChargeDecimal,RoundingMode.valueOf(sfChargeRound));
    }

    public static void calculateServicerFeeForCharge(Loan loan, Collection<LoanCharge> loanCharges, BigDecimal amountToDisburse) {

        BiPredicate<LoanProduct, LoanCharge> isActive = (loanProduct, loanCharge) -> loanProduct.getServicerFeeConfig().getServicerFeeChargesConfig().stream()
                .anyMatch(loanProductCharge -> loanProductCharge.getCharge().equals(loanCharge.getCharge()));

        loanCharges.stream().filter(loanCharge -> isActive.test(loan.getLoanProduct(),loanCharge)).forEach(loanCharge -> {

            ServicerFeeChargesConfig servicerFeeChargesConfig = getServicerFeeChargeConfig(loanCharge,loan.getLoanProduct());
            if(Objects.nonNull(servicerFeeChargesConfig)){
                BigDecimal sfChargeGstLossAmount = BigDecimal.ZERO;
                BigDecimal sfChargeGstAmount = BigDecimal.ZERO;
                BigDecimal sfChargeInvoiceAmount = BigDecimal.ZERO;
                /* Servicer Fee Charges Calculation based on Split Ratio (Fixed/Dynamic)*/
                BigDecimal sfSelfbaseAmount = ServicerFeeChargesRatio.isFixedSplit(ServicerFeeChargesRatio.getServicerFeeChargesRatio(servicerFeeChargesConfig.getServicerFeeChargesRatio())) ? ServicerFeeAmountFormulaCalculation.sfSelfBaseAmt(loanCharge.getAmount(),servicerFeeChargesConfig.getSfSelfShareCharge(),servicerFeeChargesConfig.getSfChargeBaseAmountDecimal(),servicerFeeChargesConfig.getSfChargeBaseAmountRoundingmode()) :
                        ServicerFeeAmountFormulaCalculation.sfSelfBaseAmtDynamic(amountToDisburse,servicerFeeChargesConfig.getSfSelfShareCharge(),servicerFeeChargesConfig.getSfChargeBaseAmountDecimal(),servicerFeeChargesConfig.getSfChargeBaseAmountRoundingmode());
                BigDecimal sfPartnerBaseAmount = ServicerFeeAmountFormulaCalculation.sfPartnerBaseAmt(loanCharge.getAmount(),sfSelfbaseAmount);
                if (sfPartnerBaseAmount.doubleValue()>0) {
                     sfChargeGstLossAmount = ServicerFeeAmountFormulaCalculation.sfChargeGstLossAmount(sfPartnerBaseAmount, servicerFeeChargesConfig.getSfChargeAmtGstLoss(), servicerFeeChargesConfig.getSfChargeBaseAmountDecimal(), servicerFeeChargesConfig.getSfChargeBaseAmountRoundingmode());
                     sfChargeGstAmount = ServicerFeeAmountFormulaCalculation.sfChargeGstAmount(servicerFeeChargesConfig.isSfChargeAmtGstLossEnabled() ? sfChargeGstLossAmount : loanCharge.getAmount(), servicerFeeChargesConfig.getSfChargeGst(), servicerFeeChargesConfig.getSfChargeGstDecimal(), servicerFeeChargesConfig.getSfChargeGstRoundingmode());
                     sfChargeInvoiceAmount = ServicerFeeAmountFormulaCalculation.sfChargeInvoiceAmount(sfChargeGstLossAmount, sfChargeGstAmount, servicerFeeChargesConfig.getSfChargeDecimal(), servicerFeeChargesConfig.getSfChargeRound());
                }
                loanCharge.setSfSelfBaseAmount(sfSelfbaseAmount);
                loanCharge.setSfPartnerBaseAmount(sfPartnerBaseAmount.doubleValue()>0 ? sfPartnerBaseAmount : BigDecimal.ZERO);
                loanCharge.setSfChargeGstLossAmount(sfChargeGstLossAmount);
                loanCharge.setSfChargeGstAmount(sfChargeGstAmount);
                loanCharge.setSfChargeInvoiceAmount(sfChargeInvoiceAmount);
                loanCharge.setSfSelfShare(servicerFeeChargesConfig.getSfSelfShareCharge());
                loanCharge.setSfPartnerShare(servicerFeeChargesConfig.getSfPartnerShareCharge());
                loanCharge.setServicerFeeEnabled(loan.getLoanProduct().isServicerFeeInterestConfigEnabled());
            }
        });

    }

    private static ServicerFeeChargesConfig getServicerFeeChargeConfig(LoanCharge loanCharge,LoanProduct loanProduct){
        return loanProduct.getServicerFeeConfig().getServicerFeeChargesConfig()
                .stream()
                .filter(servicerFeeChargesConfig -> servicerFeeChargesConfig.getCharge().equals(loanCharge.getCharge())).findFirst().orElse(null);



    }
    private ServicerFeeAmountFormulaCalculation() {
    }
}
