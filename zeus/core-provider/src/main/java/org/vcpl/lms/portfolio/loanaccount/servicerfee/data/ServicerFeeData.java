package org.vcpl.lms.portfolio.loanaccount.servicerfee.data;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ServicerFeeData {

    private  Long id;

    private Long loanProduct;

    private String vclInterestRound;

    private Integer vclInterestDecimal;

    private String servicerFeeRound;

    private Integer servicerFeeDecimal;

    private boolean sfBaseAmtGstLossEnabled;

    private BigDecimal sfBaseAmtGstLoss;


    private BigDecimal sfGst;

    private String sfGstRound;

    private Integer sfGstDecimal;

    private BigDecimal vclHurdleRate;

    private BigDecimal sfChargeGst;

    private String sfChargeRound;

    private  BigDecimal sfChargeDecimal;

    private  String sfChargeBaseAmountRoundingmode;

    private Integer sfChargeBaseAmountDecimal;

    private  String sfChargeGstRoundingmode;

    private Integer sfChargeGstDecimal;

    private List<RetrieveServicerFeeChargeData> servicerFeeChargeData;


    public ServicerFeeData(Long id, Long loanProduct,  String vclInterestRound, Integer vclInterestDecimal,
                           String servicerFeeRound, Integer servicerFeeDecimal, boolean sfBaseAmtGstLossEnabled,
                           BigDecimal sfBaseAmtGstLoss, BigDecimal sfGst, String sfGstRound, Integer sfGstDecimal,
                           BigDecimal vclHurdleRate,BigDecimal sfChargeGst,String sfChargeRound,BigDecimal sfChargeDecimal,String sfChargeBaseAmountRoundingmode,
                           Integer sfChargeBaseAmountDecimal,String sfChargeGstRoundingmode,Integer sfChargeGstDecimal) {
        this.id = id;
        this.loanProduct = loanProduct;
        this.vclInterestRound = vclInterestRound;
        this.vclInterestDecimal = vclInterestDecimal;
        this.servicerFeeRound = servicerFeeRound;
        this.servicerFeeDecimal = servicerFeeDecimal;
        this.sfBaseAmtGstLossEnabled = sfBaseAmtGstLossEnabled;
        this.sfBaseAmtGstLoss = sfBaseAmtGstLoss;
        this.sfGst = sfGst;
        this.sfGstRound = sfGstRound;
        this.sfGstDecimal = sfGstDecimal;
        this.vclHurdleRate = vclHurdleRate;
        this.sfChargeGst =sfChargeGst;
        this.sfChargeRound = sfChargeRound;
        this.sfChargeDecimal =sfChargeDecimal;
        this.sfChargeBaseAmountRoundingmode = sfChargeBaseAmountRoundingmode;
        this.sfChargeBaseAmountDecimal = sfChargeBaseAmountDecimal;
        this.sfChargeGstRoundingmode = sfChargeGstRoundingmode;
        this.sfChargeGstDecimal= sfChargeGstDecimal;
    }
}
