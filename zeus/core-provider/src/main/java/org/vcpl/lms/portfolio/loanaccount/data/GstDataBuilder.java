package org.vcpl.lms.portfolio.loanaccount.data;

import java.math.BigDecimal;

public class GstDataBuilder {
    private  Long id;
    private  BigDecimal cgstAmount;
    private  BigDecimal sgstAmount;
    private  BigDecimal igstAmount;
    private  BigDecimal processingFee;
    private  BigDecimal updatedChargeAmount;
    private  BigDecimal totalGst;
    private  BigDecimal selfGst;
    private  BigDecimal partnerGst;


    public GstDataBuilder setId(Long id) {
        this.id = id;
        return  this;
    }

    public GstDataBuilder setCgstAmount(BigDecimal cgstAmount) {
        this.cgstAmount = cgstAmount;
        return  this;
    }

    public GstDataBuilder setSgstAmount(BigDecimal sgstAmount) {
        this.sgstAmount = sgstAmount;
        return  this;
    }

    public GstDataBuilder setIgstAmount(BigDecimal igstAmount) {
        this.igstAmount = igstAmount;
        return  this;
    }

    public GstDataBuilder setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
        return  this;
    }

    public GstDataBuilder setSelfGst(BigDecimal selfGst) {
        this.selfGst = selfGst;
        return this;
    }

    public GstDataBuilder setPartnerGst(BigDecimal partnerGst) {
        this.partnerGst = partnerGst;
        return this;
    }

    public GstDataBuilder setUpdatedChargeAmount(BigDecimal updatedChargeAmount) {
        this.updatedChargeAmount = updatedChargeAmount;
        return  this;
    }

    public GstDataBuilder setTotalGst(BigDecimal totalGst) {
        this.totalGst = totalGst;
        return  this;
    }

    public GstData getGstData  (){
        return new GstData(id,cgstAmount,sgstAmount,igstAmount,processingFee,updatedChargeAmount,totalGst,selfGst,partnerGst);
    }
}
