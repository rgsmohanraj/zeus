package org.vcpl.lms.scheduledjobs.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.jobs.annotation.CronTarget;
import org.vcpl.lms.infrastructure.jobs.service.JobName;
import org.vcpl.lms.portfolio.loanaccount.service.VPayIntegrationService;
@Service
@RequiredArgsConstructor
public class VpayTransactionEnquirySchedulerServiceImpl implements VpayTransactionEnquirySchedulerService{

    private static final Logger LOG = LoggerFactory.getLogger(VpayTransactionEnquirySchedulerServiceImpl.class);
    private final VPayIntegrationService vPayIntegrationService;

    @CronTarget(jobName = JobName.VPAY_TRANSACTION_ENQUIRY)
    @Override
    public void vpayTransactionEnquiry(){
        vPayIntegrationService.pingVpay();
    }
}
