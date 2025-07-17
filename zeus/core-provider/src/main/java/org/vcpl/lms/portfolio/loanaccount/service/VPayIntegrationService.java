package org.vcpl.lms.portfolio.loanaccount.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface VPayIntegrationService {
    public void pennydrop(final Long loanId) throws JsonProcessingException;
    public void disburse(final Long loanId);
    void pingVpay();
}
