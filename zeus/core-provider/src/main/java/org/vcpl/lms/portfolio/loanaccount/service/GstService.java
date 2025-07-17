package org.vcpl.lms.portfolio.loanaccount.service;

import com.google.gson.JsonElement;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanaccount.data.GstData;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCharge;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanPenalForeclosureCharges;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface GstService {
    List<GstData> calculationOfGst(Long clientId, List<Charge> charges, BigDecimal principalAmount, LoanProduct loanProduct , JsonElement element, Set<LoanPenalForeclosureCharges> loanPenalForeclosureCharges);
    public  GstData calculateGstPostDisbursementCharges(LoanCharge loanCharge);
}
