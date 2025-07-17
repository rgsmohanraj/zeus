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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.lang3.ObjectUtils;
import org.vcpl.lms.infrastructure.core.domain.JdbcSupport;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.loanaccount.data.DisbursementData;
import org.vcpl.lms.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoanScheduleHistoryReadPlatformServiceImpl implements LoanScheduleHistoryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public LoanScheduleHistoryReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate, final PlatformSecurityContext context) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Integer fetchCurrentVersionNumber(Long loanId) {
        final String sql = "select MAX(lrs.version) from m_loan_repayment_schedule_history lrs where lrs.loan_id = ?";
        Integer max = this.jdbcTemplate.queryForObject(sql, new Object[] { loanId }, Integer.class);
        return ObjectUtils.defaultIfNull(max, 0);
    }

    @Override
    public LoanScheduleData retrieveRepaymentArchiveSchedule(final Long loanId,
                                                             final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData, Collection<DisbursementData> disbursementData) {

        try {
            this.context.authenticatedUser();
            Integer versionNumber = fetchCurrentVersionNumber(loanId);
            if (versionNumber == 0) {
                return null;
            }
            final LoanScheduleArchiveResultSetExtractor fullResultsetExtractor = new LoanScheduleArchiveResultSetExtractor(
                    repaymentScheduleRelatedLoanData, disbursementData);
            final String sql = "select " + fullResultsetExtractor.schema()
                    + " where ls.loan_id = ? and ls.version = ? order by ls.loan_id, ls.installment";

            return this.jdbcTemplate.query(sql, fullResultsetExtractor, new Object[] { loanId, versionNumber }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(loanId, e);
        }
    }

    private static final class LoanScheduleArchiveResultSetExtractor implements ResultSetExtractor<LoanScheduleData> {

        private final CurrencyData currency;
        private final DisbursementData disbursement;
        private final BigDecimal totalFeeChargesDueAtDisbursement;
        private final Collection<DisbursementData> disbursementData;
        private LocalDate lastDueDate;
        private BigDecimal outstandingLoanPrincipalBalance;
        private final BigDecimal totalSelfFeeChargesDueAtDisbursement;
        private final BigDecimal totalPartnerFeeChargesDueAtDisbursement;



        LoanScheduleArchiveResultSetExtractor(final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData,
                                              Collection<DisbursementData> disbursementData) {
            this.currency = repaymentScheduleRelatedLoanData.getCurrency();
            this.disbursement = repaymentScheduleRelatedLoanData.disbursementData();
            this.totalFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalFeeChargesAtDisbursement();
            this.totalSelfFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalSelfFeeChargesAtDisbursement();
            this.totalPartnerFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalPartnerFeeChargesAtDisbursement();
            this.lastDueDate = this.disbursement.disbursementDate();
            this.outstandingLoanPrincipalBalance = this.disbursement.amount();
            this.disbursementData = disbursementData;




        }

        public String schema() {
            StringBuilder stringBuilder = new StringBuilder(200);
            stringBuilder.append(" ls.installment as period, ls.fromdate as fromDate, ls.duedate as dueDate, ");
            stringBuilder.append(
                    "ls.principal_amount as principalDue, ls.interest_amount as interestDue, ls.fee_charges_amount as feeChargesDue, ls.penalty_charges_amount as penaltyChargesDue, ls.self_principal_amount as selfPrincipal, ls.partner_principal_amount as partnerPrincipal, "
                            +"ls.self_interest_amount as selfInterestCharged,  ls.partner_interest_amount as partnerInterestCharged, ls.self_due as selfDue, ls.partner_due as partnerDue ");
            stringBuilder.append(" from m_loan_repayment_schedule_history ls ");
            return stringBuilder.toString();
        }

        @Override
        public LoanScheduleData extractData(final ResultSet rs) throws SQLException, DataAccessException {

            final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursementOnlyPeriod(
                    this.disbursement.disbursementDate(), this.disbursement.amount(), this.totalFeeChargesDueAtDisbursement,
                    this.disbursement.isDisbursed(),this.disbursement.getSelfPrincipalAmount(),
                    this.disbursement.getPartnerPrincipalAmount(), null,this.totalSelfFeeChargesDueAtDisbursement,this.totalPartnerFeeChargesDueAtDisbursement);

            final Collection<LoanSchedulePeriodData> periods = new ArrayList<>();
            final MonetaryCurrency monCurrency = new MonetaryCurrency(this.currency.code(), this.currency.decimalPlaces(),
                    this.currency.currencyInMultiplesOf());
            BigDecimal totalPrincipalDisbursed = BigDecimal.ZERO;
            if (disbursementData == null || disbursementData.isEmpty()) {
                periods.add(disbursementPeriod);
                totalPrincipalDisbursed = Money.of(monCurrency, this.disbursement.amount()).getAmount();
            } else {
                this.outstandingLoanPrincipalBalance = BigDecimal.ZERO;
            }

            Money totalPrincipalExpected = Money.zero(monCurrency);
            Money totalSelfPrincipalExpected = Money.zero(monCurrency);
            Money totalPartnerPrincipalExpected = Money.zero(monCurrency);

            Money totalInterestCharged = Money.zero(monCurrency);
            Money totalSelfInterestCharged = Money.zero(monCurrency);
            Money totalPartnerInterestCharged = Money.zero(monCurrency);
            Money totalSelfDue = Money.zero(monCurrency);
            Money totalPartnerDue = Money.zero(monCurrency);

            Money totalFeeChargesCharged = Money.zero(monCurrency);
            Money totalPenaltyChargesCharged = Money.zero(monCurrency);
            Money totalRepaymentExpected = Money.zero(monCurrency);
            Money totalSelfRepaymentExpected = Money.zero(monCurrency);
            Money totalPartnerRepaymentExpected = Money.zero(monCurrency);
            Money totalSelfFeeChargesCharged = Money.zero(monCurrency);
            Money totalPartnerFeeChargesCharged = Money.zero(monCurrency);



            // update totals with details of fees charged during disbursement
            totalFeeChargesCharged = totalFeeChargesCharged.plus(disbursementPeriod.feeChargesDue());
            totalRepaymentExpected = totalRepaymentExpected.plus(disbursementPeriod.feeChargesDue());
            totalSelfRepaymentExpected = totalSelfRepaymentExpected.plus(disbursementPeriod.feeChargesDue());
            totalPartnerRepaymentExpected = totalPartnerRepaymentExpected.plus(disbursementPeriod.feeChargesDue());

            Integer loanTermInDays = Integer.valueOf(0);
            while (rs.next()) {
                final Integer period = JdbcSupport.getInteger(rs, "period");
                LocalDate fromDate = JdbcSupport.getLocalDate(rs, "fromDate");
                final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueDate");
                if (disbursementData != null) {
                    BigDecimal principal = BigDecimal.ZERO;
                    for (DisbursementData data : disbursementData) {
                        if (fromDate.equals(this.disbursement.disbursementDate()) && data.disbursementDate().equals(fromDate)) {
                            principal = principal.add(data.amount());
                            final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(data.disbursementDate(),
                                    data.amount(), this.totalFeeChargesDueAtDisbursement, data.isDisbursed(),this.disbursement.getSelfPrincipalAmount(),
                                    this.disbursement.getPartnerPrincipalAmount(), null,this.totalSelfFeeChargesDueAtDisbursement,this.totalPartnerFeeChargesDueAtDisbursement);
                            periods.add(periodData);
                            this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.add(data.amount());
                        } else if (data.isDueForDisbursement(fromDate, dueDate)
                                && this.outstandingLoanPrincipalBalance.compareTo(BigDecimal.ZERO) > 0) {
                            principal = principal.add(data.amount());
                            final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(data.disbursementDate(),
                                    data.amount(), BigDecimal.ZERO, data.isDisbursed(),this.disbursement.getSelfPrincipalAmount(),
                                    this.disbursement.getPartnerPrincipalAmount(), null,BigDecimal.ZERO,BigDecimal.ZERO);
                            periods.add(periodData);
                            this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.add(data.amount());
                        }
                    }
                    totalPrincipalDisbursed = totalPrincipalDisbursed.add(principal);
                }

                Integer daysInPeriod = Integer.valueOf(0);
                if (fromDate != null) {
                    daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(fromDate, dueDate));
                    loanTermInDays = Integer.valueOf(loanTermInDays.intValue() + daysInPeriod.intValue());
                }

                final BigDecimal principalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDue");
                totalPrincipalExpected = totalPrincipalExpected.plus(principalDue);

                final BigDecimal selfPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
                totalSelfPrincipalExpected = totalSelfPrincipalExpected.plus(selfPrincipal);

                final BigDecimal partnerPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");
                totalPartnerPrincipalExpected = totalPartnerPrincipalExpected.plus(partnerPrincipal);

                final BigDecimal interestExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestDue");
                totalInterestCharged = totalInterestCharged.plus(interestExpectedDue);

                final BigDecimal selfInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
                totalSelfInterestCharged = totalSelfInterestCharged.plus(selfInterestCharged);

                final BigDecimal partnerInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");
                totalPartnerInterestCharged = totalPartnerInterestCharged.plus(partnerInterestCharged);

                final BigDecimal selfDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfDue");
                totalSelfDue = totalSelfDue.plus(selfDue);

                final BigDecimal partnerDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerDue");
                totalPartnerDue = totalPartnerDue.plus(partnerDue);


                final BigDecimal totalInstallmentAmount = totalPrincipalExpected.zero().plus(principalDue).plus(interestExpectedDue)
                        .getAmount();

                final BigDecimal feeChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesDue");
                totalFeeChargesCharged = totalFeeChargesCharged.plus(feeChargesExpectedDue);

                final BigDecimal penaltyChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesDue");
                totalPenaltyChargesCharged = totalPenaltyChargesCharged.plus(penaltyChargesExpectedDue);

                final BigDecimal totalExpectedCostOfLoanForPeriod = interestExpectedDue.add(feeChargesExpectedDue)
                        .add(penaltyChargesExpectedDue);

                final BigDecimal totalDueForPeriod = principalDue.add(totalExpectedCostOfLoanForPeriod);


//                final BigDecimal selfPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
//                final BigDecimal partnerPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");
//                final BigDecimal selfInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
//                final BigDecimal partnerInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");
//                final BigDecimal selfDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfDue");
//                final BigDecimal partnerDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerDue");
                final BigDecimal totalSelfDueForPeriod = selfPrincipal.add(totalExpectedCostOfLoanForPeriod);
                final BigDecimal totalPartnerDueForPeriod = partnerPrincipal.add(totalExpectedCostOfLoanForPeriod);
                totalRepaymentExpected = totalRepaymentExpected.plus(totalDueForPeriod);
                totalSelfRepaymentExpected = totalSelfRepaymentExpected.plus(totalSelfDueForPeriod);
                totalPartnerRepaymentExpected = totalPartnerRepaymentExpected.plus(totalPartnerDueForPeriod);

                if (fromDate == null) {
                    fromDate = this.lastDueDate;
                }
                final BigDecimal outstandingPrincipalBalanceOfLoan = this.outstandingLoanPrincipalBalance.subtract(principalDue);

                // update based on current period values
                this.lastDueDate = dueDate;
                this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.subtract(principalDue);

                final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.repaymentOnlyPeriod(period, fromDate, dueDate,
                        principalDue, outstandingPrincipalBalanceOfLoan, interestExpectedDue, feeChargesExpectedDue,
                        penaltyChargesExpectedDue, totalDueForPeriod, totalInstallmentAmount,selfPrincipal,partnerPrincipal,selfInterestCharged,partnerInterestCharged,selfDue,partnerDue, null,null);

                periods.add(periodData);
            }

            return new LoanScheduleData(this.currency, periods, loanTermInDays, totalPrincipalDisbursed, totalPrincipalExpected.getAmount(),
                    totalInterestCharged.getAmount(), totalFeeChargesCharged.getAmount(), totalPenaltyChargesCharged.getAmount(),
                    totalRepaymentExpected.getAmount(),totalSelfRepaymentExpected.getAmount(),totalPartnerRepaymentExpected.getAmount(),
                    totalSelfPrincipalExpected.getAmount(),totalPartnerPrincipalExpected.getAmount(),totalSelfInterestCharged.getAmount(),totalPartnerInterestCharged.getAmount(),
                    totalSelfDue.getAmount(),totalPartnerDue.getAmount(),totalSelfRepaymentExpected.getAmount(),
                    totalPartnerRepaymentExpected.getAmount(),totalSelfFeeChargesCharged.getAmount(),totalPartnerFeeChargesCharged.getAmount());
        }

    }

}
