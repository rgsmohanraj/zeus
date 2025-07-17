package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanCharge {
    public int chargeId;
    public BigDecimal amount;
    public BigDecimal selfShare;
    public BigDecimal partnerShare;
}
