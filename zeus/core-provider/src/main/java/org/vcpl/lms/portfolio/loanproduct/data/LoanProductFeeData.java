package org.vcpl.lms.portfolio.loanproduct.data;

import org.vcpl.lms.portfolio.charge.domain.Charge;

import java.math.BigDecimal;

public class LoanProductFeeData {


    private final Charge charge;
    private final BigDecimal selfShare;
    private final BigDecimal partnerShare;
    private final String chargeType;

    public LoanProductFeeData(Charge charge,BigDecimal selfShare, BigDecimal partnerShare,String type) {
        this.charge=charge;
        this.selfShare = selfShare;
        this.partnerShare = partnerShare;
        this.chargeType=type;    }

    public Charge getCharge() {
        return charge;
    }

    public BigDecimal getSelfShare() {
        return selfShare;
    }

    public BigDecimal getPartnerShare() {
        return partnerShare;
    }

    public String getChargeType() {
        return chargeType;
    }
}
