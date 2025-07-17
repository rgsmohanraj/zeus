package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
@ToString
public class GstData {

    private final Long id;

    private final BigDecimal cgstAmount;
    private final BigDecimal sgstAmount;
    private final BigDecimal igstAmount;
    private final BigDecimal processingFee;
    private final BigDecimal updatedChargeAmount;
    private final BigDecimal totalGst;
    private final BigDecimal selfGst;
    private final BigDecimal partnerGst;

    public GstData(final Long id, final BigDecimal cgstAmount, final BigDecimal sgstAmount, final BigDecimal igstAmount,
                   final BigDecimal processingFee, final BigDecimal updatedChargeAmount,final BigDecimal totalGst,  final BigDecimal selfGst,final BigDecimal partnerGst) {

        this.id=id;
      this.cgstAmount=cgstAmount;
      this.sgstAmount=sgstAmount;
      this.igstAmount=igstAmount;
      this.processingFee=processingFee;
      this.updatedChargeAmount=updatedChargeAmount;
      this.totalGst=totalGst;
      this.selfGst =selfGst;
      this.partnerGst =partnerGst;
    }

    public Long getId() {
        return id;
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


    public BigDecimal getUpdatedChargeAmount() {
        return updatedChargeAmount;
    }

    public BigDecimal getTotalGst() {
        return totalGst;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public BigDecimal getSelfGst() {
        return selfGst;
    }

    public BigDecimal getPartnerGst() {
        return partnerGst;
    }
}
