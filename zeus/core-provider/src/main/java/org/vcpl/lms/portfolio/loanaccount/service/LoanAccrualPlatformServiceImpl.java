/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.loanaccount.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.vcpl.lms.infrastructure.core.exception.MultiException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.jobs.annotation.CronTarget;
import org.vcpl.lms.infrastructure.jobs.exception.JobExecutionException;
import org.vcpl.lms.infrastructure.jobs.service.JobName;
import org.vcpl.lms.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.lms.portfolio.loanaccount.data.MonthlyAccrualLoanDTO;
import org.vcpl.lms.portfolio.loanaccount.data.MonthlyAccrualLoanRecord;

@Service
public class LoanAccrualPlatformServiceImpl implements LoanAccrualPlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanAccrualPlatformServiceImpl.class);

    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanAccrualWritePlatformService loanAccrualWritePlatformService;

    @Autowired
    public LoanAccrualPlatformServiceImpl(final LoanReadPlatformService loanReadPlatformService,
            final LoanAccrualWritePlatformService loanAccrualWritePlatformService) {
        this.loanReadPlatformService = loanReadPlatformService;
        this.loanAccrualWritePlatformService = loanAccrualWritePlatformService;
    }

    @Override
    @CronTarget(jobName = JobName.ADD_ACCRUAL_ENTRIES)
    public void addAccrualAccounting() throws JobExecutionException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas = this.loanReadPlatformService.retriveScheduleAccrualData();
        Map<Long, Collection<LoanScheduleAccrualData>> loanDataMap = new HashMap<>();
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            LOG.info("[Scheduler - Add Accrual Transactions] Processing - Loan Id: {}",accrualData.getLoanId());
            if (loanDataMap.containsKey(accrualData.getLoanId())) {
                loanDataMap.get(accrualData.getLoanId()).add(accrualData);
            } else {
                Collection<LoanScheduleAccrualData> accrualDatas = new ArrayList<>();
                accrualDatas.add(accrualData);
                loanDataMap.put(accrualData.getLoanId(), accrualDatas);
            }
        }

        List<Throwable> errors = new ArrayList<>();
        for (Map.Entry<Long, Collection<LoanScheduleAccrualData>> mapEntry : loanDataMap.entrySet()) {
            try {
                LOG.info("[Scheduler - Add Periodic Accrual Transactions] Processing - Loan Id: {} ",mapEntry.getKey());
                this.loanAccrualWritePlatformService.addAccrualAccounting(mapEntry.getKey(), mapEntry.getValue());
            } catch (Exception e) {
                LOG.error("Failed to add accural transaction for loan {}", mapEntry.getKey(), e);
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }

    @Override
    // @CronTarget(jobName = JobName.ADD_PERIODIC_ACCRUAL_ENTRIES)
    public void addPeriodicAccruals() throws JobExecutionException {
        try {
                addPeriodicAccruals(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        } catch (MultiException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void addPeriodicAccruals(final LocalDate tilldate) throws JobExecutionException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas = this.loanReadPlatformService.retrivePeriodicAccrualData(tilldate);
        addPeriodicAccruals(tilldate, loanScheduleAccrualDatas);
    }

    @Override
    public void addPeriodicAccruals(final LocalDate tilldate, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas)
            throws JobExecutionException {
        Map<Long, Collection<LoanScheduleAccrualData>> loanDataMap = new HashMap<>();
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            if (loanDataMap.containsKey(accrualData.getLoanId())) {
                loanDataMap.get(accrualData.getLoanId()).add(accrualData);
            } else {
                Collection<LoanScheduleAccrualData> accrualDatas = new ArrayList<>();
                accrualDatas.add(accrualData);
                loanDataMap.put(accrualData.getLoanId(), accrualDatas);
            }
        }

        List<Throwable> errors = new ArrayList<>();
        for (Map.Entry<Long, Collection<LoanScheduleAccrualData>> mapEntry : loanDataMap.entrySet()) {
            try {
                this.loanAccrualWritePlatformService.addPeriodicAccruals(tilldate, mapEntry.getKey(), mapEntry.getValue());
            } catch (Exception e) {
                LOG.error("Failed to add accural transaction for loan {}", mapEntry.getKey(), e);
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }

    @Override
    @CronTarget(jobName = JobName.ADD_PERIODIC_ACCRUAL_ENTRIES_FOR_LOANS_WITH_INCOME_POSTED_AS_TRANSACTIONS)
    public void addPeriodicAccrualsForLoansWithIncomePostedAsTransactions() throws JobExecutionException {
        Collection<Long> loanIds = this.loanReadPlatformService.retrieveLoanIdsWithPendingIncomePostingTransactions();
        if (loanIds != null && loanIds.size() > 0) {
            List<Throwable> errors = new ArrayList<>();
            for (Long loanId : loanIds) {
                try {
                    this.loanAccrualWritePlatformService.addIncomeAndAccrualTransactions(loanId);
                } catch (Exception e) {
                    LOG.error("Failed to add income and accrual transaction for loan {}", loanId, e);
                    errors.add(e);
                }
            }
            if (!errors.isEmpty()) {
                throw new JobExecutionException(errors);
            }
        }
    }

    @Override
    @CronTarget(jobName = JobName.MONTHLY_ACCRUAL_TRANSACTION)
    public void addMonthlyAccruals() throws JobExecutionException {
        // if(LocalDate.now().equals(LocalDate.parse(DateUtils.getCurrentMonthLastDate()))) {
            LOG.info("Started Executing {}", JobName.MONTHLY_ACCRUAL_TRANSACTION);
            List<MonthlyAccrualLoanDTO> currentMonthDisbursedLoans = this.loanReadPlatformService.
                    retrieveLoansDisbursedBetweenDates(DateUtils.getCurrentMonthFirstDate(),DateUtils.getCurrentMonthLastDate());
            this.loanAccrualWritePlatformService.updateMonthlyAccrualForCurrentMonthDisbursedLoans(currentMonthDisbursedLoans);
            List<MonthlyAccrualLoanDTO> previousMonthDisbursedLoans = this.loanReadPlatformService.
                    retrieveLoansDisbursedBetweenDates(DateUtils.getPreviousMonthFirstDate(),DateUtils.getPreviousMonthLastDate());
            this.loanAccrualWritePlatformService.updateMonthlyAccrualForPreviousMonthDisbursedLoans(previousMonthDisbursedLoans);
            List<MonthlyAccrualLoanDTO> currentMonthDueLoans = this.loanReadPlatformService.retrieveLoansDueForCurrentMonth();
            this.loanAccrualWritePlatformService.updateMonthlyAccrualForCurrentMonthDueLoans(currentMonthDueLoans);
            LOG.info("Completed Executing {}", JobName.MONTHLY_ACCRUAL_TRANSACTION);
        // }
    }
}
