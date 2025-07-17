package org.vcpl.lms.portfolio.loanproduct.service;

import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.loanproduct.domain.Dedupe;

public interface DedupeService {
    Client fetchUsingAadhaar(final String aadhaar);
    Client fetchUsingPanNumber(final String pan);
}
