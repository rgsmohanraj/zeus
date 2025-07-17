package org.vcpl.lms.scheduledjobs.service;

import org.vcpl.lms.scheduledjobs.data.LoanAccrualData;

import java.util.List;
import java.util.Map;

public interface LoansAccrualSchedulerService {
    List<LoanAccrualData> retrieveActiveLoans();
    void process();
}
