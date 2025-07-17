
package org.vcpl.lms.portfolio.loanaccount.data;

import java.math.BigDecimal;

public class DisbursementSummary {


    private final String name;
    private final BigDecimal processingFeeAmount;
    private final BigDecimal selfprocessingFeeAmount;
    private final BigDecimal partnerprocessingFeeAmount;
    private final BigDecimal cgstAmount;
    private final BigDecimal sgstAmount;
    private final BigDecimal igstAmount;
    private final BigDecimal total;

    private final BigDecimal selfGst;

    private final BigDecimal partnerGst;

    private final BigDecimal totalGst;
    private BigDecimal overAllTotal;

    public String getName() {
        return name;
    }

    public BigDecimal getProcessingFeeAmount() {
        return processingFeeAmount;
    }

    public BigDecimal getSelfprocessingFeeAmount() {
        return selfprocessingFeeAmount;
    }

    public BigDecimal getPartnerprocessingFeeAmount() {
        return partnerprocessingFeeAmount;
    }

    public BigDecimal getCgstAmount() {
        return cgstAmount;
    }

    public BigDecimal getSgstAmount() {
        return sgstAmount;
    }

    public BigDecimal getIgstAmount() {
        return igstAmount;
    }

    public void setOverAllTotal(BigDecimal overAllTotal) {
        this.overAllTotal = overAllTotal;
    }

    public BigDecimal getOverAllTotal() {
        return overAllTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getPartnerGst() {
        return partnerGst;
    }

    public BigDecimal getSelfGst() {
        return selfGst;
    }

    public BigDecimal getTotalGst() {
        return totalGst;
    }

    public DisbursementSummary(String name, BigDecimal processnigFeeAmount, BigDecimal selfprocessingFeeAmount, BigDecimal partnerprocessingFeeAmount, BigDecimal cgstAmount, BigDecimal sgstAmount, BigDecimal igstAmount, BigDecimal total, BigDecimal overAllTotal,BigDecimal selfGst, BigDecimal partnerGst, BigDecimal totalGst) {
        this.name = name;
        this.processingFeeAmount = processnigFeeAmount;
        this.selfprocessingFeeAmount=selfprocessingFeeAmount;
        this.partnerprocessingFeeAmount=partnerprocessingFeeAmount;
        this.cgstAmount = cgstAmount;
        this.sgstAmount = sgstAmount;
        this.total=total;
        this.overAllTotal=overAllTotal;
        this.igstAmount=igstAmount;
        this.selfGst = selfGst;
        this.partnerGst=partnerGst;
        this.totalGst=totalGst;
    }

    public void localInstance(BigDecimal overAllTotal){
        this.overAllTotal = overAllTotal;

    }


    public void setOverAllGst(BigDecimal overAllTotal) {
        this.overAllTotal=overAllTotal;
    }
}
