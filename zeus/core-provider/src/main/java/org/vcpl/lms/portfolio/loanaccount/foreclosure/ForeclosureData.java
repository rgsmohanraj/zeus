package org.vcpl.lms.portfolio.loanaccount.foreclosure;

import java.math.BigDecimal;

public record ForeclosureData(BigDecimal interestAmount, BigDecimal selfInterest, BigDecimal partnerInterest, BigDecimal FeeAmount) {
}
