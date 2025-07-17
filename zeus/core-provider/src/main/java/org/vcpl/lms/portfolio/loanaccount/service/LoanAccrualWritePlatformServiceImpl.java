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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import liquibase.pro.packaged.cl;
import org.apache.commons.lang3.compare.ComparableUtils;
import org.springframework.transaction.annotation.Propagation;
import org.vcpl.lms.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.vcpl.lms.organisation.monetary.domain.*;
import org.vcpl.lms.portfolio.collection.data.InterestAppropriationData;
import org.vcpl.lms.portfolio.loanaccount.data.*;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations;
import org.vcpl.lms.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanAccrualWritePlatformServiceImpl implements LoanAccrualWritePlatformService {

    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final AppUserRepositoryWrapper userRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final MonthlyAccruedLoanRepository monthlyAccruedLoanRepository;
    private final LoanAccrualRepository loanAccrualRepository;

    @Autowired
    public LoanAccrualWritePlatformServiceImpl(final JdbcTemplate jdbcTemplate, final LoanReadPlatformService loanReadPlatformService,
            final JournalEntryWritePlatformService journalEntryWritePlatformService,
            final LoanChargeReadPlatformService loanChargeReadPlatformService, final AppUserRepositoryWrapper userRepository,
            final LoanRepositoryWrapper loanRepositoryWrapper, final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            DatabaseSpecificSQLGenerator sqlGenerator,final MonthlyAccruedLoanRepository monthlyAccruedLoanRepository,
                                               final LoanAccrualRepository loanAccrualRepository) {
        this.loanReadPlatformService = loanReadPlatformService;
        this.sqlGenerator = sqlGenerator;
        this.jdbcTemplate = jdbcTemplate;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.userRepository = userRepository;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.monthlyAccruedLoanRepository = monthlyAccruedLoanRepository;
        this.loanAccrualRepository = loanAccrualRepository;
    }

    @Override
    @Transactional
    public void addAccrualAccounting(final Long loanId, final Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas)
            throws Exception {
        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService.retrieveLoanChargesForAccural(loanId);
        Collection<LoanSchedulePeriodData> loanWaiverScheduleData = new ArrayList<>(1);
        Collection<LoanTransactionData> loanWaiverTansactionData = new ArrayList<>(1);

        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            if (accrualData.getWaivedInterestIncome() != null && loanWaiverScheduleData.isEmpty()) {
                loanWaiverScheduleData = this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId());
                loanWaiverTansactionData = this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId());
            }
            updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), accrualData.getDueDateAsLocaldate());
            updateInterestIncome(accrualData, loanWaiverTansactionData, loanWaiverScheduleData, accrualData.getDueDateAsLocaldate());
            addAccrualAccounting(accrualData);
        }
    }

    @Override
    @Transactional
    public void addPeriodicAccruals(final LocalDate tilldate, Long loanId, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas)
            throws Exception {
        boolean firstTime = true;
        LocalDate accruredTill = null;
        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService.retrieveLoanChargesForAccural(loanId);
        Collection<LoanSchedulePeriodData> loanWaiverScheduleData = new ArrayList<>(1);
        Collection<LoanTransactionData> loanWaiverTansactionData = new ArrayList<>(1);
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            if (accrualData.getWaivedInterestIncome() != null && loanWaiverScheduleData.isEmpty()) {
                loanWaiverScheduleData = this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId());
                loanWaiverTansactionData = this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId());
            }

            if (accrualData.getDueDateAsLocaldate().isAfter(tilldate)) {
                if (accruredTill == null || firstTime) {
                    accruredTill = accrualData.getAccruedTill();
                    firstTime = false;
                }
                if (accruredTill == null || accruredTill.isBefore(tilldate)) {
                    updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), tilldate);
                    updateInterestIncome(accrualData, loanWaiverTansactionData, loanWaiverScheduleData, tilldate);
                    addAccrualTillSpecificDate(tilldate, accrualData);
                }
            } else {
                updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), accrualData.getDueDateAsLocaldate());
                updateInterestIncome(accrualData, loanWaiverTansactionData, loanWaiverScheduleData, tilldate);
                addAccrualAccounting(accrualData);
                accruredTill = accrualData.getDueDateAsLocaldate();
            }
        }
    }

    private void addAccrualTillSpecificDate(final LocalDate tilldate, final LoanScheduleAccrualData accrualData) throws Exception {
        LocalDate interestStartDate = accrualData.getFromDateAsLocaldate();
        if (accrualData.getInterestCalculatedFrom() != null
                && accrualData.getFromDateAsLocaldate().isBefore(accrualData.getInterestCalculatedFrom())) {
            if (accrualData.getInterestCalculatedFrom().isBefore(accrualData.getDueDateAsLocaldate())) {
                interestStartDate = accrualData.getInterestCalculatedFrom();
            } else {
                interestStartDate = accrualData.getDueDateAsLocaldate();
            }
        }

        int totalNumberOfDays = Math.toIntExact(ChronoUnit.DAYS.between(interestStartDate, accrualData.getDueDateAsLocaldate()));
        LocalDate startDate = accrualData.getFromDateAsLocaldate();
        if (accrualData.getInterestCalculatedFrom() != null && startDate.isBefore(accrualData.getInterestCalculatedFrom())) {
            if (accrualData.getInterestCalculatedFrom().isBefore(tilldate)) {
                startDate = accrualData.getInterestCalculatedFrom();
            } else {
                startDate = tilldate;
            }
        }
        int daysToBeAccrued = Math.toIntExact(ChronoUnit.DAYS.between(startDate, tilldate));
        double interestPerDay = accrualData.getAccruableIncome().doubleValue() / totalNumberOfDays;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interestportion = null;
        BigDecimal feeportion = accrualData.getDueDateFeeIncome();
        BigDecimal penaltyportion = accrualData.getDueDatePenaltyIncome();
        if (daysToBeAccrued >= totalNumberOfDays) {
            interestportion = accrualData.getAccruableIncome();
        } else {
            double iterest = interestPerDay * daysToBeAccrued;
            interestportion = BigDecimal.valueOf(iterest);
        }

        interestportion = interestportion.setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalAccInterest = accrualData.getAccruedInterestIncome();
        BigDecimal totalAccPenalty = accrualData.getAccruedPenaltyIncome();
        BigDecimal totalAccFee = accrualData.getAccruedFeeIncome();

        if (interestportion != null) {
            if (totalAccInterest == null) {
                totalAccInterest = BigDecimal.ZERO;
            }
            interestportion = interestportion.subtract(totalAccInterest);
            amount = amount.add(interestportion);
            totalAccInterest = totalAccInterest.add(interestportion);
            if (interestportion.compareTo(BigDecimal.ZERO) == 0) {
                interestportion = null;
            }
        }
        if (feeportion != null) {
            if (totalAccFee == null) {
                totalAccFee = BigDecimal.ZERO;
            }
            feeportion = feeportion.subtract(totalAccFee);
            amount = amount.add(feeportion);
            totalAccFee = totalAccFee.add(feeportion);
            if (feeportion.compareTo(BigDecimal.ZERO) == 0) {
                feeportion = null;
            }
        }

        if (penaltyportion != null) {
            if (totalAccPenalty == null) {
                totalAccPenalty = BigDecimal.ZERO;
            }
            penaltyportion = penaltyportion.subtract(totalAccPenalty);
            amount = amount.add(penaltyportion);
            totalAccPenalty = totalAccPenalty.add(penaltyportion);
            if (penaltyportion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyportion = null;
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            addInterestAccrualAccounting(accrualData, amount, interestportion, totalAccInterest, feeportion, totalAccFee, penaltyportion,
                    totalAccPenalty, tilldate);
        }
    }

    @Transactional
    public void addAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData) throws Exception {

        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interestportion = null;
        BigDecimal totalAccInterest = null;
        if (scheduleAccrualData.getAccruableIncome() != null) {
            interestportion = scheduleAccrualData.getAccruableIncome();
            totalAccInterest = interestportion;
            if (scheduleAccrualData.getAccruedInterestIncome() != null) {
                interestportion = interestportion.subtract(scheduleAccrualData.getAccruedInterestIncome());
            }
            amount = amount.add(interestportion);
            if (interestportion.compareTo(BigDecimal.ZERO) == 0) {
                interestportion = null;
            }
        }

        BigDecimal feeportion = null;
        BigDecimal totalAccFee = null;
        if (scheduleAccrualData.getDueDateFeeIncome() != null) {
            feeportion = scheduleAccrualData.getDueDateFeeIncome();
            totalAccFee = feeportion;
            if (scheduleAccrualData.getAccruedFeeIncome() != null) {
                feeportion = feeportion.subtract(scheduleAccrualData.getAccruedFeeIncome());
            }
            amount = amount.add(feeportion);
            if (feeportion.compareTo(BigDecimal.ZERO) == 0) {
                feeportion = null;
            }
        }

        BigDecimal penaltyportion = null;
        BigDecimal totalAccPenalty = null;
        if (scheduleAccrualData.getDueDatePenaltyIncome() != null) {
            penaltyportion = scheduleAccrualData.getDueDatePenaltyIncome();
            totalAccPenalty = penaltyportion;
            if (scheduleAccrualData.getAccruedPenaltyIncome() != null) {
                penaltyportion = penaltyportion.subtract(scheduleAccrualData.getAccruedPenaltyIncome());
            }
            amount = amount.add(penaltyportion);
            if (penaltyportion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyportion = null;
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            addInterestAccrualAccounting(scheduleAccrualData, amount, interestportion, totalAccInterest, feeportion, totalAccFee, penaltyportion,
                    totalAccPenalty, scheduleAccrualData.getDueDateAsLocaldate());
        }
    }

    private final Function<MonthlyAccrualLoanDTO,MonthEndAccrual> calculateAccruedAmountAndCreateEntity = (monthlyAccrualLoanDTO) -> {
        BigDecimal interestPercentage = monthlyAccrualLoanDTO.getAnnualNominalInterestRate()
                .divide(BigDecimal.valueOf(100));
        BigDecimal noOfDays = BigDecimal.valueOf(monthlyAccrualLoanDTO.getDays())
                .divide(BigDecimal.valueOf(monthlyAccrualLoanDTO.getDaysInYearEnum()), MathContext.DECIMAL32);

        BigDecimal clientShareAccrued = monthlyAccrualLoanDTO.getPrincipalOutstanding()
                .multiply(interestPercentage).multiply(noOfDays);

        BigDecimal selfInterestPercentage = monthlyAccrualLoanDTO.getSelfInterestShare().divide(BigDecimal.valueOf(100));
        BigDecimal selfInterstShare = interestPercentage.multiply(selfInterestPercentage);

        BigDecimal selfShareAccruedMultiplier = selfInterstShare.divide(interestPercentage);
        BigDecimal selfInterestAccrued = clientShareAccrued.multiply(selfShareAccruedMultiplier);
        BigDecimal partnerInterestAccrued = clientShareAccrued.subtract(selfInterestAccrued);

        Date fromDate = monthlyAccrualLoanDTO.isDueMonth()
                ? DateUtils.convertLocalDateToDate(monthlyAccrualLoanDTO.getDueDate())
                : monthlyAccrualLoanDTO.isDisbursementMonth()
                    ? DateUtils.convertLocalDateToDate(monthlyAccrualLoanDTO.getDisbursementDate())
                    : DateUtils.convertLocalDateToDate(LocalDate.parse(DateUtils.getCurrentMonthFirstDate()));

        //Date fromdate = DateUtils.convertLocalDateToDate(LocalDate.parse(DateUtils.getCurrentMonthFirstDate()));
        Date monthEndDate = DateUtils.convertLocalDateToDate(LocalDate.parse(DateUtils.getCurrentMonthLastDate()));


        return new MonthEndAccrual(monthlyAccrualLoanDTO.getId(), fromDate, monthEndDate,
                clientShareAccrued, selfInterestAccrued, partnerInterestAccrued, DateUtils.getDateOfTenant());
    };

    @Transactional
    public void updateMonthlyAccrualForCurrentMonthDisbursedLoans(List<MonthlyAccrualLoanDTO> currentMonthDisbursedLoans){
        Function<MonthlyAccrualLoanDTO,MonthlyAccrualLoanDTO> calculateDaysFromDisbursedDate = (monthlyAccrualLoanDTO) -> {
            monthlyAccrualLoanDTO.setDays(ChronoUnit.DAYS.between(monthlyAccrualLoanDTO.getDisbursementDate(), LocalDate.parse(DateUtils.getCurrentMonthLastDate())) + 1);
            return monthlyAccrualLoanDTO;
        };
        List<MonthEndAccrual> monthEndAccruals = currentMonthDisbursedLoans.stream()
                .map(calculateDaysFromDisbursedDate.andThen(calculateAccruedAmountAndCreateEntity))
                .collect(Collectors.toList());
        monthlyAccruedLoanRepository.saveAllAndFlush(monthEndAccruals);
    }

    public void updateMonthlyAccrualForPreviousMonthDisbursedLoans(List<MonthlyAccrualLoanDTO> previousMonthDisbursedLoans){
        final Function<MonthlyAccrualLoanDTO,MonthlyAccrualLoanDTO> calculateDaysForPreviousMonthDisbursedDate = (monthlyAccrualLoanDTO) -> {
            monthlyAccrualLoanDTO.setDays(Long.valueOf(LocalDate.parse(DateUtils.getCurrentMonthFirstDate()).lengthOfMonth()));
            return monthlyAccrualLoanDTO;
        };

        final Predicate<MonthlyAccrualLoanDTO> dueMonthPredicate = monthlyAccrualLoanDTO ->
                monthlyAccrualLoanDTO.getDisbursementDate().isBefore(LocalDate.parse(DateUtils.getCurrentMonthFirstDate()))
                        && monthlyAccrualLoanDTO.getDueDate().isAfter(LocalDate.parse(DateUtils.getCurrentMonthLastDate()));

        List<MonthEndAccrual> monthEndAccruals = previousMonthDisbursedLoans.stream()
                .filter(dueMonthPredicate)
                .map(calculateDaysForPreviousMonthDisbursedDate.andThen(calculateAccruedAmountAndCreateEntity))
                .collect(Collectors.toList());
        monthlyAccruedLoanRepository.saveAllAndFlush(monthEndAccruals);
    }

    public void updateMonthlyAccrualForCurrentMonthDueLoans(List<MonthlyAccrualLoanDTO> disbursedLoans) {
        Function<MonthlyAccrualLoanDTO,MonthlyAccrualLoanDTO> calculateDaysFromDueDate = (monthlyAccrualLoanDTO) -> {
            monthlyAccrualLoanDTO.setDays(ChronoUnit.DAYS.between(monthlyAccrualLoanDTO.getDueDate(), LocalDate.parse(DateUtils.getCurrentMonthLastDate())) + 1);
            return monthlyAccrualLoanDTO;
        };
        final Predicate<MonthlyAccrualLoanDTO> maturiedLoanPredicate = monthlyAccrualLoanDTO ->
                !monthlyAccrualLoanDTO.getMaturityDate().equals(monthlyAccrualLoanDTO.getDueDate());

        List<MonthEndAccrual> monthEndAccruals = disbursedLoans.stream()
                .filter(maturiedLoanPredicate)
                .map(calculateDaysFromDueDate.andThen(calculateAccruedAmountAndCreateEntity))
                .collect(Collectors.toList());
        monthlyAccruedLoanRepository.saveAllAndFlush(monthEndAccruals);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reverseOnRepayment(Long loanId, LoanRepaymentScheduleInstallment installment, LocalDate transactionDate,
                                   InterestAppropriationData interestData) {

        Date paidDate = transactionDate.isAfter(installment.getDueDate())
                ? DateUtils.convertLocalDateToDate(transactionDate)
                : DateUtils.convertLocalDateToDate(installment.getDueDate());

        List<LoanAccrual> loanAccruals = loanAccrualRepository.getByLoanIdAndReversedFalseAndFromDateGreaterThanEqual(loanId, paidDate);
        loanAccruals.stream()
                .filter(loanAccrual -> ComparableUtils.is(loanAccrual.getFromDateAsLocalDate()).greaterThanOrEqualTo(transactionDate))
                .forEach(loanAccrual -> {
                    loanAccrual.setInterestAccruedButNotReceived(loanAccrual.getInterestAccruedButNotReceived().subtract(interestData.interest().getAmount()));
                    loanAccrual.setSelfInterestAccruedButNotReceived(loanAccrual.getSelfInterestAccruedButNotReceived().subtract(interestData.selfInterest().getAmount()));
                    loanAccrual.setPartnerInterestAccruedButNotReceived(loanAccrual.getPartnerInterestAccruedButNotReceived().subtract(interestData.partnerInterest().getAmount()));

                    loanAccrual.setInterestDueReceived(loanAccrual.getInterestDueReceived().add(interestData.interest().getAmount()));
                    loanAccrual.setSelfInterestDueReceived(loanAccrual.getSelfInterestDueReceived().add(interestData.selfInterest().getAmount()));
                    loanAccrual.setPartnerInterestDueReceived(loanAccrual.getPartnerInterestDueReceived().add(interestData.partnerInterest().getAmount()));
                });
        loanAccrualRepository.saveAll(loanAccruals);
    }

    @Override
    public void reverseOnForeclosure(Loan loan, Date transactionDate) {

        loanAccrualRepository.reverseLoanAccruals(loan.getId(), transactionDate);

        Long count = loan.getRepaymentScheduleInstallments()
                .stream()
                .filter(installment -> installment.getObligationsMetOnDate().isEqual(DateUtils.convertDateToLocalDate(transactionDate)))
                .count();

        int totalInstallments = (int) loan.getRepaymentScheduleInstallments().stream().count();
        LoanRepaymentScheduleInstallment lastInstallment = loan.getRepaymentScheduleInstallments().get(totalInstallments - 1);

        BigDecimal interestAccruedButNotReceived = BigDecimal.ZERO;
        BigDecimal selfInterestAccruedButNotReceived = BigDecimal.ZERO;
        BigDecimal partnerInterestAccruedButNotReceived = BigDecimal.ZERO;

        BigDecimal cumulativeInterestAccrued = BigDecimal.ZERO;
        BigDecimal cumulativeSelfInterestAccrued = BigDecimal.ZERO;
        BigDecimal cumulativePartnerInterestAccrued = BigDecimal.ZERO;

        if (count > 0) {
            Date previousDate = DateUtils.convertLocalDateToDate(lastInstallment.getFromDate().minusDays(1));
            Optional<LoanAccrual> optionalLoanAccrual = loanAccrualRepository
                    .getLoanAccrualsByLoanIdAndInstallmentAndFromDate(loan.getId(),lastInstallment.getInstallmentNumber()-1,previousDate);
            if (optionalLoanAccrual.isPresent()) {
                interestAccruedButNotReceived = optionalLoanAccrual.get().getInterestAccruedButNotReceived();
                selfInterestAccruedButNotReceived = optionalLoanAccrual.get().getSelfInterestAccruedButNotReceived();
                partnerInterestAccruedButNotReceived = optionalLoanAccrual.get().getPartnerInterestAccruedButNotReceived();
                cumulativeInterestAccrued = optionalLoanAccrual.get().getCumulativeAccruedAmount();
                cumulativeSelfInterestAccrued = optionalLoanAccrual.get().getSelfCumulativeAccruedAmount();
                cumulativePartnerInterestAccrued = optionalLoanAccrual.get().getPartnerCumulativeAccruedAmount();
            }
        }

        Optional<List<LoanAccrual>> optionalLoanAccruals = loanAccrualRepository
                .getLoanAccrualsByLoanIdAndInstallment(loan.getId(), lastInstallment.getInstallmentNumber());

        if (optionalLoanAccruals.isPresent()) {
            List<LoanAccrual> loanAccruals = optionalLoanAccruals.get().stream()
                    .filter(loanAccrual -> loanAccrual.getInstallment().equals(lastInstallment.getInstallmentNumber())).toList();

        BigDecimal clientInterestAmount = lastInstallment.getInterestCharged();
        BigDecimal selfInterestAmount = lastInstallment.getSelfInterestCharged();
        BigDecimal partnerInterestAccruedButNotDue = lastInstallment.getPartnerInterestCharged();

        BigDecimal clientInterestAccruedButNotDue = BigDecimal.ZERO;
        BigDecimal selfInterestAccruedButNotDue = BigDecimal.ZERO;
        BigDecimal partnerInterestAmount = BigDecimal.ZERO;

        for (LoanAccrual loanAccrual :loanAccruals) {
            BigDecimal betweenDays = BigDecimal.valueOf(ChronoUnit.DAYS.between(DateUtils.convertDateToLocalDate(loanAccrual.getFromDate()),lastInstallment.getDueDate()));
            BigDecimal clientShare = clientInterestAmount.divide(betweenDays, 2, RoundingMode.HALF_UP);
            BigDecimal selfShare = selfInterestAmount.divide(betweenDays, 2, RoundingMode.HALF_UP);
            BigDecimal partnerShare = partnerInterestAmount.divide(betweenDays, 2, RoundingMode.HALF_UP);

            clientInterestAccruedButNotDue = clientInterestAccruedButNotDue.add(clientShare);
            selfInterestAccruedButNotDue = selfInterestAccruedButNotDue.add(selfShare);
            partnerInterestAccruedButNotDue = partnerInterestAccruedButNotDue.add(partnerShare);

            clientInterestAmount = clientInterestAmount.subtract(clientShare).setScale(2, RoundingMode.HALF_EVEN);
            selfInterestAmount = selfInterestAmount.subtract(selfShare).setScale(2, RoundingMode.HALF_EVEN);
            partnerInterestAmount = partnerInterestAmount.subtract(partnerShare).setScale(2, RoundingMode.HALF_EVEN);

            loanAccrual.setAccruedAmount(clientShare);
            loanAccrual.setSelfAccruedAmount(selfShare);
            loanAccrual.setPartnerAccruedAmount(partnerShare);

            loanAccrual.setInterestAccruedButNotDue(clientInterestAccruedButNotDue);
            loanAccrual.setSelfInterestAccruedButNotDue(selfInterestAccruedButNotDue);
            loanAccrual.setPartnerInterestAccruedButNotDue(partnerInterestAccruedButNotDue);

            loanAccrual.setInterestAccruedButNotReceived(interestAccruedButNotReceived.add(clientInterestAccruedButNotDue));
            loanAccrual.setSelfInterestAccruedButNotReceived(selfInterestAccruedButNotReceived.add(selfInterestAccruedButNotDue));
            loanAccrual.setPartnerInterestAccruedButNotReceived(partnerInterestAccruedButNotReceived.add(partnerInterestAccruedButNotDue));

            cumulativeInterestAccrued = cumulativeInterestAccrued.add(clientShare);
            cumulativeSelfInterestAccrued = cumulativeSelfInterestAccrued.add(selfShare);
            cumulativePartnerInterestAccrued = cumulativePartnerInterestAccrued.add(partnerShare);

            loanAccrual.setCumulativeAccruedAmount(cumulativeInterestAccrued);
            loanAccrual.setSelfCumulativeAccruedAmount(cumulativeSelfInterestAccrued);
            loanAccrual.setPartnerCumulativeAccruedAmount(cumulativePartnerInterestAccrued);

        }
        loanAccrualRepository.saveAll(loanAccruals);
        loanAccrualRepository.save(new LoanAccrual(lastInstallment.getLoan().getId(),lastInstallment.getInstallmentNumber(),LoanAccrualType.DAILY.getValue(),
                transactionDate,transactionDate,BigDecimal.ZERO, cumulativeInterestAccrued, cumulativeInterestAccrued,BigDecimal.ZERO,BigDecimal.ZERO,
                BigDecimal.ZERO, cumulativeSelfInterestAccrued, cumulativeSelfInterestAccrued, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, cumulativePartnerInterestAccrued, cumulativePartnerInterestAccrued, BigDecimal.ZERO, BigDecimal.ZERO, false, new Date()));
        }
    }

    private void addAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData, BigDecimal amount, BigDecimal interestportion,
            BigDecimal totalAccInterest, BigDecimal feeportion, BigDecimal totalAccFee, BigDecimal penaltyportion,
            BigDecimal totalAccPenalty, final LocalDate accruedTill) throws DataAccessException {
        String transactionSql = "INSERT INTO m_loan_transaction  (loan_id,office_id,is_reversed,transaction_type_enum,transaction_date,amount,interest_portion_derived,"
                + "fee_charges_portion_derived,penalty_charges_portion_derived, submitted_on_date) VALUES (?, ?, false, ?, ?, ?, ?, ?, ?, ?)";
        this.jdbcTemplate.update(transactionSql, scheduleAccrualData.getLoanId(), scheduleAccrualData.getOfficeId(),
                LoanTransactionType.ACCRUAL.getValue(), Date.from(accruedTill.atStartOfDay(ZoneId.systemDefault()).toInstant()), amount,
                interestportion, feeportion, penaltyportion, DateUtils.getDateOfTenant());
        @SuppressWarnings("deprecation")
        final Long transactonId = this.jdbcTemplate.queryForObject("SELECT " + sqlGenerator.lastInsertId(), Long.class); // NOSONAR

        Map<LoanChargeData, BigDecimal> applicableCharges = scheduleAccrualData.getApplicableCharges();
        String chargespaidSql = "INSERT INTO m_loan_charge_paid_by (loan_transaction_id, loan_charge_id, amount,installment_number) VALUES (?,?,?,?)";
        for (Map.Entry<LoanChargeData, BigDecimal> entry : applicableCharges.entrySet()) {
            LoanChargeData chargeData = entry.getKey();
            this.jdbcTemplate.update(chargespaidSql, transactonId, chargeData.getId(), entry.getValue(),
                    scheduleAccrualData.getInstallmentNumber());
        }

        Map<String, Object> transactionMap = toMapData(transactonId, amount, interestportion, feeportion, penaltyportion,
                scheduleAccrualData, accruedTill);

        String repaymetUpdatesql = "UPDATE m_loan_repayment_schedule SET accrual_interest_derived=?, accrual_fee_charges_derived=?, "
                + "accrual_penalty_charges_derived=? WHERE  id=?";
        this.jdbcTemplate.update(repaymetUpdatesql, totalAccInterest, totalAccFee, totalAccPenalty,
                scheduleAccrualData.getRepaymentScheduleId());

        String updateLoan = "UPDATE m_loan  SET accrued_till=?  WHERE  id=?";
        this.jdbcTemplate.update(updateLoan, Date.from(accruedTill.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                scheduleAccrualData.getLoanId());
        final Map<String, Object> accountingBridgeData = deriveAccountingBridgeData(scheduleAccrualData, transactionMap);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    private void addInterestAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData, BigDecimal amount, BigDecimal interestportion,
                                     BigDecimal totalAccInterest, BigDecimal feeportion, BigDecimal totalAccFee, BigDecimal penaltyportion,
                                     BigDecimal totalAccPenalty, final LocalDate accruedTill) throws DataAccessException {
        if (Objects.nonNull(scheduleAccrualData.getAccruedTill())
                && scheduleAccrualData.getAccruedTill().isAfter(scheduleAccrualData.getFromDateAsLocaldate())) {
            LoanAccrual loanAccrual = new LoanAccrual(scheduleAccrualData.getLoanId(), scheduleAccrualData.getInstallmentNumber(), LoanAccrualType.DAILY.getValue(),
                    Date.from(scheduleAccrualData.getAccruedTill().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Date.from(scheduleAccrualData.getAccruedTill().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    amount, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    amount, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO, Boolean.FALSE,
                    new Date(System.currentTimeMillis()));
            loanAccrualRepository.save(loanAccrual);
        } else {
            int daysBetween = Math.toIntExact(ChronoUnit.DAYS.between(scheduleAccrualData.getFromDateAsLocaldate(), accruedTill));
            BigDecimal interestAmount = amount;
            for (int i = 0; i < daysBetween; i++) {
                BigDecimal split = interestAmount.divide(BigDecimal.valueOf(daysBetween - i), 2, RoundingMode.HALF_UP);
                interestAmount = interestAmount.subtract(split).setScale(2, RoundingMode.HALF_EVEN);
                LoanAccrual loanAccrual = new LoanAccrual(scheduleAccrualData.getLoanId(), scheduleAccrualData.getInstallmentNumber(), LoanAccrualType.DAILY.getValue(),
                        Date.from( scheduleAccrualData.getFromDateAsLocaldate().plusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(scheduleAccrualData.getFromDateAsLocaldate().plusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        split, BigDecimal.ZERO,BigDecimal.ZERO,  BigDecimal.ZERO, BigDecimal.ZERO,
                        split, BigDecimal.ZERO,BigDecimal.ZERO,  BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO,  BigDecimal.ZERO, BigDecimal.ZERO,
                        Boolean.FALSE, new Date(System.currentTimeMillis()));
                loanAccrualRepository.save(loanAccrual);
            }
        }
        /*
         *  Commented below logic as we are storing the accrual in m_loan_accrual table
         *
         *  String transactionSql = "INSERT INTO m_loan_transaction  (loan_id,office_id,is_reversed,transaction_type_enum,transaction_date,amount,interest_portion_derived,"
         *       + "submitted_on_date) VALUES (?, ?, false, ?, ?, ?, ?, ?)";
         *   this.jdbcTemplate.update(transactionSql, scheduleAccrualData.getLoanId(), scheduleAccrualData.getOfficeId(),
         *       LoanTransactionType.ACCRUAL.getValue(), Date.from(accruedTill.atStartOfDay(ZoneId.systemDefault()).toInstant()), amount,
         *       interestportion, DateUtils.getDateOfTenant());
         * */
        @SuppressWarnings("deprecation")
        /*final Long transactonId = this.jdbcTemplate.queryForObject("SELECT " + sqlGenerator.lastInsertId(), Long.class); // NOSONAR
        Map<LoanChargeData, BigDecimal> applicableCharges = scheduleAccrualData.getApplicableCharges();
        String chargespaidSql = "INSERT INTO m_loan_charge_paid_by (loan_transaction_id, loan_charge_id, amount,installment_number) VALUES (?,?,?,?)";
        for (Map.Entry<LoanChargeData, BigDecimal> entry : applicableCharges.entrySet()) {
            LoanChargeData chargeData = entry.getKey();
            this.jdbcTemplate.update(chargespaidSql, transactonId, chargeData.getId(), entry.getValue(),
                    scheduleAccrualData.getInstallmentNumber());
        }

        Map<String, Object> transactionMap = toMapData(transactonId, amount, interestportion, feeportion, penaltyportion,
                scheduleAccrualData, accruedTill);*/
        String updateAccrualInLoanRepaymentQuery = "UPDATE m_loan_repayment_schedule SET accrual_interest_derived=? WHERE  id=?";
        this.jdbcTemplate.update(updateAccrualInLoanRepaymentQuery, totalAccInterest, scheduleAccrualData.getRepaymentScheduleId());
        String updateLoan = "UPDATE m_loan  SET accrued_till=?  WHERE  id=?";
        this.jdbcTemplate.update(updateLoan, Date.from(accruedTill.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                scheduleAccrualData.getLoanId());
        // final Map<String, Object> accountingBridgeData = deriveAccountingBridgeData(scheduleAccrualData, transactionMap);
        // this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }


    public Map<String, Object> deriveAccountingBridgeData(final LoanScheduleAccrualData loanScheduleAccrualData,
            final Map<String, Object> transactionMap) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("loanId", loanScheduleAccrualData.getLoanId());
        accountingBridgeData.put("loanProductId", loanScheduleAccrualData.getLoanProductId());
        accountingBridgeData.put("officeId", loanScheduleAccrualData.getOfficeId());
        accountingBridgeData.put("currency", loanScheduleAccrualData.getCurrencyData());
        accountingBridgeData.put("cashBasedAccountingEnabled", false);
        accountingBridgeData.put("upfrontAccrualBasedAccountingEnabled", false);
        accountingBridgeData.put("periodicAccrualBasedAccountingEnabled", true);
        accountingBridgeData.put("isAccountTransfer", false);

        final List<Map<String, Object>> newLoanTransactions = new ArrayList<>();
        newLoanTransactions.add(transactionMap);

        accountingBridgeData.put("newLoanTransactions", newLoanTransactions);
        return accountingBridgeData;
    }

    public Map<String, Object> toMapData(final Long id, final BigDecimal amount, final BigDecimal interestportion,
            final BigDecimal feeportion, final BigDecimal penaltyportion, final LoanScheduleAccrualData loanScheduleAccrualData,
            final LocalDate accruredTill) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<>();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.ACCRUAL);

        thisTransactionData.put("id", id);
        thisTransactionData.put("officeId", loanScheduleAccrualData.getOfficeId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", false);
        thisTransactionData.put("date", accruredTill);
        thisTransactionData.put("currency", loanScheduleAccrualData.getCurrencyData());
        thisTransactionData.put("amount", amount);
        thisTransactionData.put("principalPortion", null);
        thisTransactionData.put("interestPortion", interestportion);
        thisTransactionData.put("feeChargesPortion", feeportion);
        thisTransactionData.put("penaltyChargesPortion", penaltyportion);
        thisTransactionData.put("overPaymentPortion", null);

        Map<LoanChargeData, BigDecimal> applicableCharges = loanScheduleAccrualData.getApplicableCharges();
        if (applicableCharges != null && !applicableCharges.isEmpty()) {
            final List<Map<String, Object>> loanChargesPaidData = new ArrayList<>();
            for (Map.Entry<LoanChargeData, BigDecimal> entry : applicableCharges.entrySet()) {
                LoanChargeData chargeData = entry.getKey();
                final Map<String, Object> loanChargePaidData = new LinkedHashMap<>();
                loanChargePaidData.put("chargeId", chargeData.getChargeId());
                loanChargePaidData.put("isPenalty", chargeData.isPenalty());
                loanChargePaidData.put("loanChargeId", chargeData.getId());
                loanChargePaidData.put("amount", entry.getValue());

                loanChargesPaidData.add(loanChargePaidData);
            }
            thisTransactionData.put("loanChargesPaid", loanChargesPaidData);
        }

        return thisTransactionData;
    }

    private void updateCharges(final Collection<LoanChargeData> chargesData, final LoanScheduleAccrualData accrualData,
            final LocalDate startDate, final LocalDate endDate) {

        final Map<LoanChargeData, BigDecimal> applicableCharges = new HashMap<>();
        BigDecimal dueDateFeeIncome = BigDecimal.ZERO;
        BigDecimal dueDatePenaltyIncome = BigDecimal.ZERO;
        for (LoanChargeData loanCharge : chargesData) {
            BigDecimal chargeAmount = BigDecimal.ZERO;
            if (loanCharge.getDueDate() == null) {
                if (loanCharge.isInstallmentFee() && accrualData.getDueDateAsLocaldate().isEqual(endDate)) {
                    Collection<LoanInstallmentChargeData> installmentData = loanCharge.getInstallmentChargeData();
                    for (LoanInstallmentChargeData installmentChargeData : installmentData) {

                        if (installmentChargeData.getInstallmentNumber().equals(accrualData.getInstallmentNumber())) {
                            BigDecimal accruableForInstallment = installmentChargeData.getAmount();
                            if (installmentChargeData.getAmountUnrecognized() != null) {
                                accruableForInstallment = accruableForInstallment.subtract(installmentChargeData.getAmountUnrecognized());
                            }
                            chargeAmount = accruableForInstallment;
                            boolean canAddCharge = chargeAmount.compareTo(BigDecimal.ZERO) > 0;
                            if (canAddCharge && (installmentChargeData.getAmountAccrued() == null
                                    || chargeAmount.compareTo(installmentChargeData.getAmountAccrued()) != 0)) {
                                BigDecimal amountForAccrual = chargeAmount;
                                if (installmentChargeData.getAmountAccrued() != null) {
                                    amountForAccrual = chargeAmount.subtract(installmentChargeData.getAmountAccrued());
                                }
                                applicableCharges.put(loanCharge, amountForAccrual);
                                BigDecimal amountAccrued = chargeAmount;
                                if (loanCharge.getAmountAccrued() != null) {
                                    amountAccrued = amountAccrued.add(loanCharge.getAmountAccrued());
                                }
                                loanCharge.updateAmountAccrued(amountAccrued);
                            }
                            break;
                        }
                    }
                }
            } else if (loanCharge.getDueDate().isAfter(startDate) && !loanCharge.getDueDate().isAfter(endDate)) {
                chargeAmount = loanCharge.getAmount();
                if (loanCharge.getAmountUnrecognized() != null) {
                    chargeAmount = chargeAmount.subtract(loanCharge.getAmountUnrecognized());
                }
                boolean canAddCharge = chargeAmount.compareTo(BigDecimal.ZERO) > 0;
                if (canAddCharge && (loanCharge.getAmountAccrued() == null || chargeAmount.compareTo(loanCharge.getAmountAccrued()) != 0)) {
                    BigDecimal amountForAccrual = chargeAmount;
                    if (loanCharge.getAmountAccrued() != null) {
                        amountForAccrual = chargeAmount.subtract(loanCharge.getAmountAccrued());
                    }
                    applicableCharges.put(loanCharge, amountForAccrual);
                }
            }

            if (loanCharge.isPenalty()) {
                dueDatePenaltyIncome = dueDatePenaltyIncome.add(chargeAmount);
            } else {
                dueDateFeeIncome = dueDateFeeIncome.add(chargeAmount);
            }
        }

        if (dueDateFeeIncome.compareTo(BigDecimal.ZERO) == 0) {
            dueDateFeeIncome = null;
        }

        if (dueDatePenaltyIncome.compareTo(BigDecimal.ZERO) == 0) {
            dueDatePenaltyIncome = null;
        }

        accrualData.updateChargeDetails(applicableCharges, dueDateFeeIncome, dueDatePenaltyIncome);
    }

    private void updateInterestIncome(final LoanScheduleAccrualData accrualData,
            final Collection<LoanTransactionData> loanWaiverTansactions, final Collection<LoanSchedulePeriodData> loanSchedulePeriodDatas,
            final LocalDate tilldate) {

        BigDecimal interestIncome = BigDecimal.ZERO;
        if (accrualData.getInterestIncome() != null) {
            interestIncome = accrualData.getInterestIncome();
        }
        if (accrualData.getWaivedInterestIncome() != null) {
            BigDecimal recognized = BigDecimal.ZERO;
            BigDecimal unrecognized = BigDecimal.ZERO;
            BigDecimal remainingAmt = BigDecimal.ZERO;
            Collection<LoanTransactionData> loanTransactionDatas = new ArrayList<>();

            for (LoanTransactionData loanTransactionData : loanWaiverTansactions) {
                if (!loanTransactionData.dateOf().isAfter(accrualData.getFromDateAsLocaldate())
                        || (loanTransactionData.dateOf().isAfter(accrualData.getFromDateAsLocaldate())
                                && !loanTransactionData.dateOf().isAfter(accrualData.getDueDateAsLocaldate())
                                && !loanTransactionData.dateOf().isAfter(tilldate))) {
                    loanTransactionDatas.add(loanTransactionData);
                }
            }

            Iterator<LoanTransactionData> iterator = loanTransactionDatas.iterator();
            for (LoanSchedulePeriodData loanSchedulePeriodData : loanSchedulePeriodDatas) {
                if (recognized.compareTo(BigDecimal.ZERO) <= 0 && unrecognized.compareTo(BigDecimal.ZERO) <= 0 && iterator.hasNext()) {
                    LoanTransactionData loanTransactionData = iterator.next();
                    recognized = recognized.add(loanTransactionData.getInterestPortion());
                    unrecognized = unrecognized.add(loanTransactionData.getUnrecognizedIncomePortion());
                }
                if (loanSchedulePeriodData.periodDueDate().isBefore(accrualData.getDueDateAsLocaldate())) {
                    remainingAmt = remainingAmt.add(loanSchedulePeriodData.interestWaived());
                    if (recognized.compareTo(remainingAmt) > 0) {
                        recognized = recognized.subtract(remainingAmt);
                        remainingAmt = BigDecimal.ZERO;
                    } else {
                        remainingAmt = remainingAmt.subtract(recognized);
                        recognized = BigDecimal.ZERO;
                        if (unrecognized.compareTo(remainingAmt) >= 0) {
                            unrecognized = unrecognized.subtract(remainingAmt);
                            remainingAmt = BigDecimal.ZERO;
                        } else if (iterator.hasNext()) {
                            remainingAmt = remainingAmt.subtract(unrecognized);
                            unrecognized = BigDecimal.ZERO;
                        }
                    }

                }
            }

            BigDecimal interestWaived = accrualData.getWaivedInterestIncome();
            if (interestWaived.compareTo(recognized) > 0) {
                interestIncome = interestIncome.subtract(interestWaived.subtract(recognized));
            }
        }

        accrualData.updateAccruableIncome(interestIncome);
    }

    @Override
    @Transactional
    public void addIncomeAndAccrualTransactions(Long loanId) throws LoanNotFoundException {
        if (loanId != null) {
            Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            if (loan == null) {
                throw new LoanNotFoundException(loanId);
            }
            final List<Long> existingTransactionIds = new ArrayList<>();
            final List<Long> existingReversedTransactionIds = new ArrayList<>();
            existingTransactionIds.addAll(loan.findExistingTransactionIds());
            existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
            loan.processIncomeTransactions(this.userRepository.fetchSystemUser());
            this.loanRepositoryWrapper.saveAndFlush(loan);
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        }
    }

    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }
}
