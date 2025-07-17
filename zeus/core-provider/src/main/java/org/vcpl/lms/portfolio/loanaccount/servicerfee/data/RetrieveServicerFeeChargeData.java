package org.vcpl.lms.portfolio.loanaccount.servicerfee.data;

import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.portfolio.charge.data.ChargeData;

import java.math.BigDecimal;

public record RetrieveServicerFeeChargeData(ChargeData charge, BigDecimal sfSelfShareCharge, BigDecimal sfPartnerShareCharge, Boolean sfChargeAmtGstLossEnabled, BigDecimal sfChargeAmtGstLoss, Boolean isActive, EnumOptionData servicerFeeChargesRatio) {
}
