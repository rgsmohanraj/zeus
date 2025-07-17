package org.vcpl.lms.portfolio.loanaccount.servicerfee.data;

import org.vcpl.lms.portfolio.charge.domain.Charge;

import java.math.BigDecimal;

public record ServicerFeeChargeData(Charge charge, BigDecimal sfSelfShareCharge, BigDecimal sfPartnerShareCharge,
                                    Boolean sfChargeAmtGstLossEnabled,BigDecimal sfChargeAmtGstLoss, Boolean isActive,Integer servicerFeeChargesRatio) {
}
