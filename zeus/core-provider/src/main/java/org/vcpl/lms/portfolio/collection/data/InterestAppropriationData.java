package org.vcpl.lms.portfolio.collection.data;

import org.vcpl.lms.organisation.monetary.domain.Money;
public record InterestAppropriationData(Money interest, Money selfInterest, Money partnerInterest) {


}
