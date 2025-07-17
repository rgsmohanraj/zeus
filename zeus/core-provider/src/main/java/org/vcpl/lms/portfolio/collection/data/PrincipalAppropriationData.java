package org.vcpl.lms.portfolio.collection.data;

import org.vcpl.lms.organisation.monetary.domain.Money;

public record PrincipalAppropriationData(Money principal, Money selfPrincipal, Money partnerPrincipal) {
}
