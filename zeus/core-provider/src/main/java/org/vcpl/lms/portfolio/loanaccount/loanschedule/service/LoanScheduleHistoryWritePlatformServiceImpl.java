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
package org.vcpl.lms.portfolio.loanaccount.loanschedule.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistory;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistoryRepository;
import org.vcpl.lms.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.useradministration.domain.AppUserRepository;

@Service
@Transactional
public class LoanScheduleHistoryWritePlatformServiceImpl implements LoanScheduleHistoryWritePlatformService {

    private final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService;
    private final LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository;

    @Autowired
    public LoanScheduleHistoryWritePlatformServiceImpl(final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService,
                                                       final LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository) {
        this.loanScheduleHistoryReadPlatformService = loanScheduleHistoryReadPlatformService;
        this.loanRepaymentScheduleHistoryRepository = loanRepaymentScheduleHistoryRepository;

    }

    @Override
    public List<LoanRepaymentScheduleHistory> createLoanScheduleArchive(
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Loan loan, LoanRescheduleRequest loanRescheduleRequest) {
        Integer version = this.loanScheduleHistoryReadPlatformService.fetchCurrentVersionNumber(loan.getId()) + 1;
        final MonetaryCurrency currency = loan.getCurrency();
        final List<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistoryList = new ArrayList<>();

        for (LoanRepaymentScheduleInstallment repaymentScheduleInstallment : repaymentScheduleInstallments) {

            Date fromDate = null;
            Date dueDate = null;

            if (repaymentScheduleInstallment.getFromDate() != null) {
                fromDate = Date.from(repaymentScheduleInstallment.getFromDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }

            if (repaymentScheduleInstallment.getDueDate() != null) {
                dueDate = Date.from(repaymentScheduleInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            /*final Integer installmentNumber = repaymentScheduleInstallment.getInstallmentNumber();
            final BigDecimal principal = repaymentScheduleInstallment.getPrincipal(currency).getAmount();
            final BigDecimal selfPrincipal = repaymentScheduleInstallment.getSelfPrincipal(currency).getAmount();
            final BigDecimal partnerPrincipal = repaymentScheduleInstallment.getPartnerPrincipal(currency).getAmount();
            final BigDecimal interestCharged = repaymentScheduleInstallment.getInterestCharged(currency).getAmount();
            final BigDecimal selfInterestCharged = repaymentScheduleInstallment.getSelfInterestCharged(currency).getAmount();
            final BigDecimal partnerInterestCharged = repaymentScheduleInstallment.getPartnerInterestCharged(currency).getAmount();
            final BigDecimal selfDue = repaymentScheduleInstallment.getSelfDue(currency).getAmount();
            final BigDecimal partnerDue = repaymentScheduleInstallment.getPartnerDue(currency).getAmount();
            final BigDecimal feeChargesCharged = repaymentScheduleInstallment.getFeeChargesCharged(currency).getAmount();
            final BigDecimal penaltyCharges = repaymentScheduleInstallment.getPenaltyChargesCharged(currency).getAmount();
            final Integer dpd = repaymentScheduleInstallment.getDaysPastDue();
            final Integer dpdHistory = repaymentScheduleInstallment.getDpdHistory();*/

            Date createdOnDate = null;
            if (repaymentScheduleInstallment.getCreatedDate().isPresent()) {
                createdOnDate = Date.from(repaymentScheduleInstallment.getCreatedDate().get());
            }

            final AppUser createdByUser = repaymentScheduleInstallment.getCreatedBy().orElse(null);
            final AppUser lastModifiedByUser = repaymentScheduleInstallment.getLastModifiedBy().orElse(null);

            Date lastModifiedOnDate = null;

            if (repaymentScheduleInstallment.getLastModifiedDate().isPresent()) {
                lastModifiedOnDate = Date.from(repaymentScheduleInstallment.getLastModifiedDate().get());
            }
            Date obligationsMetOnDate = null;
            if (repaymentScheduleInstallment.getObligationsMetOnDate() != null) {
                obligationsMetOnDate = Date.from(repaymentScheduleInstallment.getObligationsMetOnDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            LoanRepaymentScheduleHistory loanRepaymentScheduleHistory = new LoanRepaymentScheduleHistory();
            BeanUtils.copyProperties(repaymentScheduleInstallment,loanRepaymentScheduleHistory);


            loanRepaymentScheduleHistory.setFromDate(fromDate);
            loanRepaymentScheduleHistory.setDueDate(dueDate);
            loanRepaymentScheduleHistory.setVersion(version);
            loanRepaymentScheduleHistory.setCreatedOnDate(createdOnDate);
            loanRepaymentScheduleHistory.setCreatedByUser(createdByUser);
            loanRepaymentScheduleHistory.setLastModifiedByUser(lastModifiedByUser);
            loanRepaymentScheduleHistory.setLastModifiedOnDate(lastModifiedOnDate);
            loanRepaymentScheduleHistory.setObligationsMetOnDate(obligationsMetOnDate);
            loanRepaymentScheduleHistory.setSelfDue(repaymentScheduleInstallment.getSelfDue(currency).getAmount());
            loanRepaymentScheduleHistory.setPartnerDue(repaymentScheduleInstallment.getPartnerDue(currency).getAmount());
            loanRepaymentScheduleHistory.setSelfPrincipal(repaymentScheduleInstallment.getSelfPrincipal(currency).getAmount());
            loanRepaymentScheduleHistory.setPartnerPrincipal(repaymentScheduleInstallment.getPartnerPrincipal(currency).getAmount());
            loanRepaymentScheduleHistory.setPartnerPrincipalCompleted(repaymentScheduleInstallment.getPartnerPrincipalCompleted());
            loanRepaymentScheduleHistory.setDpdBucket(repaymentScheduleInstallment.getDpdBucket());
            loanRepaymentScheduleHistory.setDpdBucketHistory(repaymentScheduleInstallment.getDpdHistoryBucket());
            loanRepaymentScheduleHistory.setInterestPrincipalAppropriatedOnDate(repaymentScheduleInstallment.getInterestPrincipalAppropriatedOnDate());
            loanRepaymentScheduleHistory.setSelfInterestPrincipalAppropriatedOnDate(repaymentScheduleInstallment.getSelfInterestPrincipalAppropriatedOnDate());
            loanRepaymentScheduleHistory.setPartnerInterestPrincipalAppropriatedOnDate(repaymentScheduleInstallment.getPartnerInterestPrincipalAppropriatedOnDate());

            /*LoanRepaymentScheduleHistory.instance(loan, loanRescheduleRequest,
            installmentNumber, fromDate, dueDate, principal, interestCharged, feeChargesCharged, penaltyCharges, createdOnDate,
            createdByUser, lastModifiedByUser, lastModifiedOnDate, version,selfPrincipal,partnerPrincipal,selfInterestCharged,partnerInterestCharged,selfDue,
            partnerDue, obligationsMetOnDate, dpd, dpdHistory);*/

            loanRepaymentScheduleHistoryList.add(loanRepaymentScheduleHistory);
        }
        return loanRepaymentScheduleHistoryList;
    }

    @Override
    public void createAndSaveLoanScheduleArchive(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Loan loan,
            LoanRescheduleRequest loanRescheduleRequest) {
        List<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistoryList = createLoanScheduleArchive(repaymentScheduleInstallments, loan,
                loanRescheduleRequest);
        this.loanRepaymentScheduleHistoryRepository.saveAll(loanRepaymentScheduleHistoryList);

    }

}
