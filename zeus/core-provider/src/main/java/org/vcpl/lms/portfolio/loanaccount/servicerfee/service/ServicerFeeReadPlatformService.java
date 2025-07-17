package org.vcpl.lms.portfolio.loanaccount.servicerfee.service;

import org.springframework.stereotype.Service;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.RetrieveServicerFeeChargeData;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.ServicerFeeData;

import java.util.List;

@Service
sealed public interface ServicerFeeReadPlatformService permits  ServicerFeeReadPlatformServiceimp {

    List<RetrieveServicerFeeChargeData> retrieveServicerFee(Long servicerFeeConfigId);
    ServicerFeeData retrieveServicerFeeConfigData( Long ProductId);
}
