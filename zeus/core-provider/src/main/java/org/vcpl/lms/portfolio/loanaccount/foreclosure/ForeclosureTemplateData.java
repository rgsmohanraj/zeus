package org.vcpl.lms.portfolio.loanaccount.foreclosure;

import org.vcpl.lms.organisation.monetary.domain.Money;

import java.math.BigDecimal;

public record ForeclosureTemplateData(Money interestPayable, Money feePayable, Money penaltyPayable,
                                      Money payPrincipal, Money paySelfPrincipal, Money payPartnerPrincipal, Money paySelfInterest, Money payPartnerInterest, BigDecimal advanceAmount) {
}
