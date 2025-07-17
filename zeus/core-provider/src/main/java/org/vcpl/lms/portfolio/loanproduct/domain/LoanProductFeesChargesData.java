package org.vcpl.lms.portfolio.loanproduct.domain;


import java.io.Serializable;
import java.math.BigDecimal;

public final class LoanProductFeesChargesData implements Comparable<LoanProductFeesChargesData>, Serializable {

    private final Integer id;
    private final String name;
    private final Integer colendingFees;
    private final Integer colendingCharge;
    private final Integer loanProductId;
    private final Integer chargeId;
    private final BigDecimal selfCharge;
    private final BigDecimal partnerCharge;
    private final BigDecimal selfFees;
    private final BigDecimal partnerFees;
    private final String chargeType;

    private final BigDecimal selfOverDue;
    private final BigDecimal partnerOverDue;

    public LoanProductFeesChargesData(final Integer id,final Integer colendingFees,final Integer colendingCharge,final Integer loanProductId,
                                      final Integer chargeId,final BigDecimal selfCharge,final BigDecimal partnerCharge,final BigDecimal selfFees,
                                      final BigDecimal partnerFees, final String chargeType,final String chargeName, final BigDecimal selfOverDue,
                                      final BigDecimal partnerOverDue) {
        this.name = chargeName;
        this.id = id;
        this.colendingFees=colendingFees;
        this.colendingCharge=colendingCharge;
        this.loanProductId=loanProductId;
        this.chargeId=chargeId;
        this.selfCharge=selfCharge;
        this.partnerCharge=partnerCharge;
        this.selfFees=selfFees;
        this.partnerFees=partnerFees;
        this.chargeType=chargeType;
        this.selfOverDue = selfOverDue;
        this.partnerOverDue = partnerOverDue;

    }

    public Integer getId() {
        return id;
    }

    public Integer getLoanProductId() {
        return loanProductId;
    }

    public Integer getChargeId() {
        return chargeId;
    }

    public BigDecimal getSelfCharge() {
        return selfCharge;
    }

    public BigDecimal getPartnerCharge() {
        return partnerCharge;
    }

    public BigDecimal getSelfFees() {
        return selfFees;
    }

    public BigDecimal getPartnerFees() {
        return partnerFees;
    }

    public String getChargeType() {
        return chargeType;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(final LoanProductFeesChargesData obj) {
        if (obj == null) {
            return -1;
        }
        return obj.id.compareTo(this.id);

    }

}
