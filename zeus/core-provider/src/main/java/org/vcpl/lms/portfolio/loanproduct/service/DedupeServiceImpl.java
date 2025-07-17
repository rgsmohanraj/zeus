package org.vcpl.lms.portfolio.loanproduct.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.client.domain.ClientRepositoryWrapper;

import java.util.Objects;

@Service
public class DedupeServiceImpl implements DedupeService {

    @Autowired
    private ClientRepositoryWrapper clientRepositoryWrapper;

    @Override
    public Client fetchUsingAadhaar(String aadhaar) {
        return clientRepositoryWrapper.getClientByAadhaar(aadhaar);
    }

    @Override
    public Client fetchUsingPanNumber(String pan) {
        return clientRepositoryWrapper.getClientByPan(pan);
    }
}
