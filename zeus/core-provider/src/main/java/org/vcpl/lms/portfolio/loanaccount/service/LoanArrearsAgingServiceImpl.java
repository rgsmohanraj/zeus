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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.vcpl.lms.infrastructure.core.domain.JdbcSupport;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil;
import org.vcpl.lms.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.vcpl.lms.infrastructure.jobs.annotation.CronTarget;
import org.vcpl.lms.infrastructure.jobs.service.JobName;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants.BusinessEntity;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants.BusinessEvents;
import org.vcpl.lms.portfolio.common.service.BusinessEventListener;
import org.vcpl.lms.portfolio.common.service.BusinessEventNotifierService;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanArrearsAgingServiceImpl implements LoanArrearsAgingService, BusinessEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(LoanArrearsAgingServiceImpl.class);
    private final BusinessEventNotifierService businessEventNotifierService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final JdbcTemplate jdbcTemplate;
    private final LoanRepository loanRepository;

    @Autowired
    public LoanArrearsAgingServiceImpl(final JdbcTemplate jdbcTemplate, final BusinessEventNotifierService businessEventNotifierService,
                                       DatabaseSpecificSQLGenerator sqlGenerator, LoanRepository loanRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.businessEventNotifierService = businessEventNotifierService;
        this.sqlGenerator = sqlGenerator;
        this.loanRepository = loanRepository;
    }

    @PostConstruct
    public void registerForNotification() {
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_REFUND, this);
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_ADJUST_TRANSACTION, this);
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_MAKE_REPAYMENT, this);
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_UNDO_WRITTEN_OFF, this);
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_WAIVE_INTEREST, this);
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_ADD_CHARGE, this);
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_WAIVE_CHARGE, this);
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_CHARGE_PAYMENT, this);
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_APPLY_OVERDUE_CHARGE, this);
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_DISBURSAL, new DisbursementEventListener());
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_FORECLOSURE, this);
    }

    @Transactional
    @Override
    @CronTarget(jobName = JobName.UPDATE_LOAN_ARREARS_AGEING)
    public void updateLoanArrearsAgeingDetails() {

        this.jdbcTemplate.execute("truncate table m_loan_arrears_aging");

        final StringBuilder updateSqlBuilder = new StringBuilder(900);
        final String principalOverdueCalculationSql = "SUM(COALESCE(mr.principal_amount, 0) - coalesce(mr.principal_completed_derived, 0) - coalesce(mr.principal_writtenoff_derived, 0))";
        final String interestOverdueCalculationSql = "SUM(COALESCE(mr.interest_amount, 0) - coalesce(mr.interest_writtenoff_derived, 0) - coalesce(mr.interest_waived_derived, 0) - "
                + "coalesce(mr.interest_completed_derived, 0))";
        final String feeChargesOverdueCalculationSql = "SUM(COALESCE(mr.fee_charges_amount, 0) - coalesce(mr.fee_charges_writtenoff_derived, 0) - "
                + "coalesce(mr.fee_charges_waived_derived, 0) - coalesce(mr.fee_charges_completed_derived, 0))";
        final String penaltyChargesOverdueCalculationSql = "SUM(COALESCE(mr.penalty_charges_amount, 0) - coalesce(mr.penalty_charges_writtenoff_derived, 0) - "
                + "coalesce(mr.penalty_charges_waived_derived, 0) - coalesce(mr.penalty_charges_completed_derived, 0))";

        final String selfPrincipalOverdueCalculationSql = """
        SUM(COALESCE(mr.self_principal_amount, 0) - coalesce(mr.self_principal_completed_derived, 0) -
         coalesce(mr.self_principal_writtenoff_derived, 0))""";

        final String selfInterestOverdueCalculationSql = """
         SUM(COALESCE(mr.self_interest_amount, 0) - coalesce(mr.self_interest_writtenoff_derived, 0) -
         coalesce(mr.self_interest_waived_derived, 0) - coalesce(mr.self_interest_completed_derived, 0))""";

        final String partnerPrincipalOverdueCalculationSql = """
        SUM(COALESCE(mr.partner_principal_amount, 0) - coalesce(mr.partner_principal_completed_derived, 0) - 
        coalesce(mr.partner_principal_writtenoff_derived, 0))""";

        final String partnerInterestOverdueCalculationSql = """
         SUM(COALESCE(mr.partner_interest_amount, 0) - coalesce(mr.partner_interest_writtenoff_derived, 0) -
         coalesce(mr.partner_interest_waived_derived, 0) - coalesce(mr.partner_interest_completed_derived, 0))""";

        updateSqlBuilder.append(
                """
        INSERT INTO m_loan_arrears_aging(loan_id,principal_overdue_derived,self_principal_overdue_derived,
        partner_principal_overdue_derived,interest_overdue_derived,self_interest_overdue_derived,partner_interest_overdue_derived,
        fee_charges_overdue_derived,penalty_charges_overdue_derived,total_overdue_derived,overdue_since_date_derived)""");
        updateSqlBuilder.append("select ml.id as loanId,");
        updateSqlBuilder.append(principalOverdueCalculationSql + " as principal_overdue_derived,");
        updateSqlBuilder.append(selfPrincipalOverdueCalculationSql + " as  self_principal_overdue_derived," );
        updateSqlBuilder.append(partnerPrincipalOverdueCalculationSql + "as partner_principal_overdue_derived,");
        updateSqlBuilder.append(interestOverdueCalculationSql + " as interest_overdue_derived,");
        updateSqlBuilder.append(selfInterestOverdueCalculationSql +" as self_interest_overdue_derived,");
        updateSqlBuilder.append(partnerInterestOverdueCalculationSql + "as partner_interest_overdue_derived,");
        updateSqlBuilder.append(feeChargesOverdueCalculationSql + " as fee_charges_overdue_derived,");
        updateSqlBuilder.append(penaltyChargesOverdueCalculationSql + " as penalty_charges_overdue_derived,");
        updateSqlBuilder.append(principalOverdueCalculationSql + "+" + interestOverdueCalculationSql + "+");
        updateSqlBuilder.append(feeChargesOverdueCalculationSql + "+" + penaltyChargesOverdueCalculationSql + " as total_overdue_derived,");
        updateSqlBuilder.append("MIN(mr.duedate) as overdue_since_date_derived ");
        updateSqlBuilder.append(" FROM m_loan ml ");
        updateSqlBuilder.append(" INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        updateSqlBuilder.append(" left join m_product_loan_recalculation_details prd on prd.product_id = ml.product_id ");
        updateSqlBuilder.append(" WHERE ml.loan_status_id = 300 "); // active
        updateSqlBuilder.append(" and mr.completed_derived is false ");
        updateSqlBuilder.append(" and mr.duedate < ")
                .append(sqlGenerator.subDate(sqlGenerator.currentDate(), "COALESCE(ml.grace_on_arrears_ageing, 0)", "day")).append(" ");
        updateSqlBuilder.append(" and (prd.arrears_based_on_original_schedule = false or prd.arrears_based_on_original_schedule is null) ");
        updateSqlBuilder.append(" GROUP BY ml.id");

        List<String> insertStatements = updateLoanArrearsAgeingDetailsWithOriginalSchedule();
        insertStatements.add(0, updateSqlBuilder.toString());
        final int[] results = this.jdbcTemplate.batchUpdate(insertStatements.toArray(new String[0]));
        int result = 0;
        for (int i : results) {
            result += i;
        }

        try {
            int val = 0;
            List<Loan> loans = loanRepository.findLoanByLoanStatus();
            for(Loan loan : loans){
                LOG.info(" [Scheduler - Update Loan Arrears Ageing] Processing - Loan Id: {} ",loan.getId());
                loan.getLoanSummary().updateLoanArrearAgeing(loan);
                loan.setValueDate(DateUtils.getDateOfTenant());
                val++;
            }
            LOG.info("{}: Records processed into loan by schedular : {}", ThreadLocalContextUtil.getTenant().getName(), val);
        }catch (Exception e){
            LOG.error(e.getMessage());
        }


        /*List<String> insertStatement = new ArrayList<>();
        insertStatement.add(0,arrearColumnInsertStatement.toString());
        final int[] loanresult = this.jdbcTemplate.batchUpdate(insertStatement.toArray(new String[0]));*/

        LOG.info("{}: Records affected by updateLoanArrearsAgeingDetails: {}", ThreadLocalContextUtil.getTenant().getName(), result);
    }

    @Override
    public void updateLoanArrearsAgeingDetailsWithOriginalSchedule(final Loan loan) {
        int count = this.jdbcTemplate.queryForObject("select count(mla.loan_id) from m_loan_arrears_aging mla where mla.loan_id =?",
                Integer.class, loan.getId());
        List<String> updateStatement = new ArrayList<>();
        OriginalScheduleExtractor originalScheduleExtractor = new OriginalScheduleExtractor(loan.getId().toString(), sqlGenerator);
        Map<Long, List<LoanSchedulePeriodData>> scheduleDate = this.jdbcTemplate.query(originalScheduleExtractor.schema,
                originalScheduleExtractor);
        if (scheduleDate.size() > 0) {
            List<Map<String, Object>> transactions = getLoanSummary(loan.getId(), loan.getLoanSummary());
            updateSchheduleWithPaidDetail(scheduleDate, transactions);
            createInsertStatements(updateStatement, scheduleDate, count == 0);
            if (updateStatement.size() == 1) {
                this.jdbcTemplate.update(updateStatement.get(0));
            } else {
                String deletestatement = "DELETE FROM m_loan_arrears_aging WHERE  loan_id=?";
                this.jdbcTemplate.update(deletestatement, new Object[] { loan.getId() }); // NOSONAR
            }
        }
    }
    @Override
    public void updateLoanArrearsAgeingDetails(final Loan loan) {
        int count = this.jdbcTemplate.queryForObject("select count(mla.loan_id) from m_loan_arrears_aging mla where mla.loan_id =?",
                Integer.class, loan.getId());
        String updateStatement = constructUpdateStatement(loan, count == 0);
        if (updateStatement == null) {
            String deletestatement = "DELETE FROM m_loan_arrears_aging WHERE  loan_id=?";
            this.jdbcTemplate.update(deletestatement, new Object[] { loan.getId() }); // NOSONAR
        } else {
            this.jdbcTemplate.update(updateStatement);
        }
    }

    private String constructUpdateStatement(final Loan loan, boolean isInsertStatement) {
        String updateSql = null;
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        BigDecimal principalOverdue = BigDecimal.ZERO;
        BigDecimal interestOverdue = BigDecimal.ZERO;
        BigDecimal feeOverdue = BigDecimal.ZERO;
        BigDecimal penaltyOverdue = BigDecimal.ZERO;

        BigDecimal selfPrincipalOverdue = BigDecimal.ZERO;
        BigDecimal selfInterestOverdue = BigDecimal.ZERO;
        BigDecimal partnerPrincipalOverdue = BigDecimal.ZERO;
        BigDecimal partnerInterestOverdue = BigDecimal.ZERO;

        LocalDate overDueSince = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.getDueDate().isBefore(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()))) {
                principalOverdue = principalOverdue.add(installment.getPrincipalOutstanding(loan.getCurrency()).getAmount());
                interestOverdue = interestOverdue.add(installment.getInterestOutstanding(loan.getCurrency()).getAmount());
                feeOverdue = feeOverdue.add(installment.getFeeChargesOutstanding(loan.getCurrency()).getAmount());
                penaltyOverdue = penaltyOverdue.add(installment.getPenaltyChargesOutstanding(loan.getCurrency()).getAmount());
                selfPrincipalOverdue = selfPrincipalOverdue.add(installment.getSelfPrincipalOutstanding(loan.getCurrency()).getAmount());
                selfInterestOverdue =selfInterestOverdue.add(installment.getSelfInterestOutstanding(loan.getCurrency()).getAmount());
                partnerPrincipalOverdue =partnerPrincipalOverdue.add(installment.getPartnerPrincipalOutstanding(loan.getCurrency()).getAmount());
                partnerInterestOverdue =partnerInterestOverdue.add(installment.getPartnerInterestOutstanding(loan.getCurrency()).getAmount());
                if (installment.isNotFullyPaidOff() && overDueSince.isAfter(installment.getDueDate())) {
                    overDueSince = installment.getDueDate();
                }
            }
        }

        BigDecimal totalOverDue = principalOverdue.add(interestOverdue).add(feeOverdue).add(penaltyOverdue);
        if (totalOverDue.compareTo(BigDecimal.ZERO) > 0) {
            if (isInsertStatement) {
                updateSql = constructInsertStatement(loan.getId(), principalOverdue, interestOverdue, feeOverdue, penaltyOverdue,
                        overDueSince,selfPrincipalOverdue,selfInterestOverdue,partnerPrincipalOverdue,partnerInterestOverdue);
            } else {
                updateSql = constructUpdateStatement(loan.getId(), principalOverdue, interestOverdue, feeOverdue, penaltyOverdue,
                        overDueSince ,selfPrincipalOverdue,selfInterestOverdue,partnerPrincipalOverdue,partnerInterestOverdue);
            }
        }
        return updateSql;
    }

    private List<String> updateLoanArrearsAgeingDetailsWithOriginalSchedule() {
        List<String> insertStatement = new ArrayList<>();

        final StringBuilder loanIdentifier = new StringBuilder();
        loanIdentifier.append("select ml.id as loanId FROM m_loan ml  ");
        loanIdentifier.append("INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        loanIdentifier.append(
                "inner join m_product_loan_recalculation_details prd on prd.product_id = ml.product_id and prd.arrears_based_on_original_schedule = true  ");
        loanIdentifier.append("WHERE ml.loan_status_id = 300  and mr.completed_derived is false  and mr.duedate < ")
                .append(sqlGenerator.subDate(sqlGenerator.currentDate(), "COALESCE(ml.grace_on_arrears_ageing, 0)", "day"))
                .append(" group by ml.id");
        List<Long> loanIds = this.jdbcTemplate.queryForList(loanIdentifier.toString(), Long.class);
        if (!loanIds.isEmpty()) {
            String loanIdsAsString = loanIds.toString();
            loanIdsAsString = loanIdsAsString.substring(1, loanIdsAsString.length() - 1);
            OriginalScheduleExtractor originalScheduleExtractor = new OriginalScheduleExtractor(loanIdsAsString, sqlGenerator);
            Map<Long, List<LoanSchedulePeriodData>> scheduleDate = this.jdbcTemplate.query(originalScheduleExtractor.schema,
                    originalScheduleExtractor);

            List<Map<String, Object>> loanSummary = getLoanSummary(loanIdsAsString);
            updateSchheduleWithPaidDetail(scheduleDate, loanSummary);
            createInsertStatements(insertStatement, scheduleDate, true);
        }

        return insertStatement;

    }

    private List<Map<String, Object>> getLoanSummary(final String loanIdsAsString) {
        final StringBuilder transactionsSql = new StringBuilder();
        transactionsSql.append("select ml.id as loanId, ");
        transactionsSql
                .append("ml.principal_repaid_derived as principalAmtPaid, ml.principal_writtenoff_derived as  principalAmtWrittenoff, ");
        transactionsSql.append(" ml.interest_repaid_derived as interestAmtPaid, ml.interest_waived_derived as interestAmtWaived, ");
        transactionsSql.append("ml.fee_charges_repaid_derived as feeAmtPaid, ml.fee_charges_waived_derived as feeAmtWaived, ");
        transactionsSql
                .append("ml.penalty_charges_repaid_derived as penaltyAmtPaid, ml.penalty_charges_waived_derived as penaltyAmtWaived ");
        transactionsSql.append("from m_loan ml ");
        transactionsSql.append("where ml.id IN (").append(loanIdsAsString).append(") order by ml.id");

        List<Map<String, Object>> loanSummary = this.jdbcTemplate.queryForList(transactionsSql.toString());
        return loanSummary;
    }

    private List<Map<String, Object>> getLoanSummary(final Long loanId, final LoanSummary loanSummary) {
        List<Map<String, Object>> transactionDetail = new ArrayList<>();
        Map<String, Object> transactionMap = new HashMap<>();

        transactionMap.put("loanId", loanId);
        transactionMap.put("principalAmtPaid", loanSummary.getTotalPrincipalRepaid());
        transactionMap.put("principalAmtWrittenoff", loanSummary.getTotalPrincipalWrittenOff());
        transactionMap.put("interestAmtPaid", loanSummary.getTotalInterestRepaid());
        transactionMap.put("interestAmtWaived", loanSummary.getTotalInterestWaived());
        transactionMap.put("feeAmtPaid", loanSummary.getTotalFeeChargesRepaid());
        transactionMap.put("feeAmtWaived", loanSummary.getTotalFeeChargesWaived());
        transactionMap.put("penaltyAmtPaid", loanSummary.getTotalPenaltyChargesRepaid());
        transactionMap.put("penaltyAmtWaived", loanSummary.getTotalPenaltyChargesWaived());
        transactionMap.put("selfPrincipalPaid", loanSummary.getTotalSelfPrincipalRepaid());
        transactionMap.put("selfPrincipalWrittenOff", loanSummary.getTotalSelfPrincipalWrittenOff());
        transactionMap.put("selfInterestPaid", loanSummary.getTotalSelfInterestRepaid());
        transactionMap.put("selfInterestWaived", loanSummary.getTotalSelfInterestWaived());
        transactionMap.put("partnerPrincipalPaid", loanSummary.getTotalPartnerPrincipalRepaid());
        transactionMap.put("partnerPrincipalWrittenOff", loanSummary.getTotalPartnerPrincipalWrittenOff());
        transactionMap.put("partnerInterestPaid", loanSummary.getTotalPartnerInterestRepaid());
        transactionMap.put("partnerInterestWaived", loanSummary.getTotalPartnerInterestWaived());
        transactionDetail.add(transactionMap);
        return transactionDetail;

    }

    private void createInsertStatements(List<String> insertStatement, Map<Long, List<LoanSchedulePeriodData>> scheduleDate,
                                        boolean isInsertStatement) {
        for (Map.Entry<Long, List<LoanSchedulePeriodData>> entry : scheduleDate.entrySet()) {
            final Long loanId = entry.getKey();
            BigDecimal principalOverdue = BigDecimal.ZERO;
            BigDecimal interestOverdue = BigDecimal.ZERO;
            BigDecimal feeOverdue = BigDecimal.ZERO;
            BigDecimal penaltyOverdue = BigDecimal.ZERO;

            BigDecimal selfPrincipalOverdue = BigDecimal.ZERO;
            BigDecimal selfInterestOverdue = BigDecimal.ZERO;
            BigDecimal partnerPrincipalOverdue = BigDecimal.ZERO;
            BigDecimal partnerInterestOverdue = BigDecimal.ZERO;
            LocalDate overDueSince = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

            for (LoanSchedulePeriodData loanSchedulePeriodData : entry.getValue()) {
                if (!loanSchedulePeriodData.getComplete()) {
                    principalOverdue = principalOverdue
                            .add(loanSchedulePeriodData.principalDue().subtract(loanSchedulePeriodData.principalPaid()));
                    selfPrincipalOverdue = selfPrincipalOverdue
                            .add(loanSchedulePeriodData.selfPrincipal().subtract(loanSchedulePeriodData.getSelfPrincipalPaid()));
                    partnerPrincipalOverdue = partnerPrincipalOverdue
                            .add(loanSchedulePeriodData.principalDue().subtract(loanSchedulePeriodData.getPartnerInterestPaid()));
                    interestOverdue = interestOverdue
                            .add(loanSchedulePeriodData.interestDue().subtract(loanSchedulePeriodData.interestPaid()));
                    selfInterestOverdue = selfInterestOverdue
                            .add(loanSchedulePeriodData.selfInterestCharged().subtract(loanSchedulePeriodData.getSelfInterestPaid()));
                    partnerInterestOverdue = partnerInterestOverdue
                            .add(loanSchedulePeriodData.partnerInterestCharged().subtract(loanSchedulePeriodData.getPartnerInterestPaid()));
                    feeOverdue = feeOverdue.add(loanSchedulePeriodData.feeChargesDue().subtract(loanSchedulePeriodData.feeChargesPaid()));
                    penaltyOverdue = penaltyOverdue
                            .add(loanSchedulePeriodData.penaltyChargesDue().subtract(loanSchedulePeriodData.penaltyChargesPaid()));
                    if (overDueSince.isAfter(loanSchedulePeriodData.periodDueDate()) && loanSchedulePeriodData.principalDue()
                            .subtract(loanSchedulePeriodData.principalPaid()).compareTo(BigDecimal.ZERO) > 0) {
                        overDueSince = loanSchedulePeriodData.periodDueDate();
                    }
                }
            }
            if (principalOverdue.compareTo(BigDecimal.ZERO) > 0) {
                String sqlStatement = null;
                if (isInsertStatement) {
                    sqlStatement = constructInsertStatement(loanId, principalOverdue, interestOverdue, feeOverdue, penaltyOverdue,
                            overDueSince,selfPrincipalOverdue,selfInterestOverdue,partnerPrincipalOverdue,partnerInterestOverdue);
                } else {
                    sqlStatement = constructUpdateStatement(loanId, principalOverdue, interestOverdue, feeOverdue, penaltyOverdue,
                            overDueSince,selfPrincipalOverdue,selfInterestOverdue,partnerPrincipalOverdue,partnerInterestOverdue);
                }
                insertStatement.add(sqlStatement);
            }

        }
    }

    private String constructInsertStatement(final Long loanId, BigDecimal principalOverdue, BigDecimal interestOverdue,
                                            BigDecimal feeOverdue, BigDecimal penaltyOverdue, LocalDate overDueSince,BigDecimal selfPrincipalOverdue,
                                            BigDecimal selfInterestOverdue,BigDecimal partnerPrincipalOverdue,BigDecimal partnerInterestOverdue) {
        final StringBuilder insertStatementBuilder = new StringBuilder(900);
        insertStatementBuilder.append("INSERT INTO m_loan_arrears_aging(loan_id,principal_overdue_derived,interest_overdue_derived,")
                .append("fee_charges_overdue_derived,penalty_charges_overdue_derived,total_overdue_derived,overdue_since_date_derived," +
                        "self_principal_overdue_derived,self_interest_overdue_derived,partner_principal_overdue_derived,partner_interest_overdue_derived) VALUES(");
        insertStatementBuilder.append(loanId).append(",");
        insertStatementBuilder.append(principalOverdue).append(",");
        insertStatementBuilder.append(interestOverdue).append(",");
        insertStatementBuilder.append(feeOverdue).append(",");
        insertStatementBuilder.append(penaltyOverdue).append(",");
        BigDecimal totalOverDue = principalOverdue.add(interestOverdue).add(feeOverdue).add(penaltyOverdue);
        insertStatementBuilder.append(totalOverDue).append(",'");
        insertStatementBuilder.append(this.formatter.format(overDueSince)).append(" ' ,");
        insertStatementBuilder.append(selfPrincipalOverdue).append(",");
        insertStatementBuilder.append(selfInterestOverdue).append(",");
        insertStatementBuilder.append(partnerPrincipalOverdue).append(",");
        insertStatementBuilder.append(partnerInterestOverdue).append(" )");


        return insertStatementBuilder.toString();
    }

    private String constructUpdateStatement(final Long loanId, BigDecimal principalOverdue, BigDecimal interestOverdue,
                                            BigDecimal feeOverdue, BigDecimal penaltyOverdue, LocalDate overDueSince,
                                            BigDecimal selfPrincipalOverdue,BigDecimal selfInterestOverdue,BigDecimal partnerPrincipalOverdue,BigDecimal partnerInterestOverdue) {
        final StringBuilder insertStatementBuilder = new StringBuilder(900);
        insertStatementBuilder.append("UPDATE m_loan_arrears_aging SET principal_overdue_derived=");
        insertStatementBuilder.append(principalOverdue).append(", interest_overdue_derived=");
        insertStatementBuilder.append(interestOverdue).append(", fee_charges_overdue_derived=");
        insertStatementBuilder.append(feeOverdue).append(", penalty_charges_overdue_derived=");
        insertStatementBuilder.append(penaltyOverdue).append(", total_overdue_derived=");
        BigDecimal totalOverDue = principalOverdue.add(interestOverdue).add(feeOverdue).add(penaltyOverdue);
        insertStatementBuilder.append(totalOverDue).append(",overdue_since_date_derived= ' ");
        insertStatementBuilder.append(this.formatter.format(overDueSince)).append(" ' ,self_principal_overdue_derived= ");
        insertStatementBuilder.append(selfPrincipalOverdue).append(",self_interest_overdue_derived= ");
        insertStatementBuilder.append(selfInterestOverdue).append(",partner_principal_overdue_derived= ");
        insertStatementBuilder.append(partnerPrincipalOverdue).append(",partner_interest_overdue_derived= ");
        insertStatementBuilder.append(partnerInterestOverdue);
        insertStatementBuilder.append(" WHERE  loan_id=").append(loanId);
        return insertStatementBuilder.toString();
    }

    private void updateSchheduleWithPaidDetail(Map<Long, List<LoanSchedulePeriodData>> scheduleDate,
                                               List<Map<String, Object>> loanSummary) {
        for (Map<String, Object> transactionMap : loanSummary) {
            String longValue = transactionMap.get("loanId").toString(); // From
            // JDBC
            // Template
            // API,
            // we
            // are
            // getting
            // BigInteger
            // but
            // in
            // other
            // call,
            // we
            // are
            // getting
            // Long
            Long loanId = Long.parseLong(longValue);
            BigDecimal principalAmtPaid = (BigDecimal) transactionMap.get("principalAmtPaid");
            BigDecimal principalAmtWrittenoff = (BigDecimal) transactionMap.get("principalAmtWrittenoff");
            BigDecimal interestAmtPaid = (BigDecimal) transactionMap.get("interestAmtPaid");
            BigDecimal interestAmtWaived = (BigDecimal) transactionMap.get("interestAmtWaived");
            BigDecimal feeAmtPaid = (BigDecimal) transactionMap.get("feeAmtPaid");
            BigDecimal feeAmtWaived = (BigDecimal) transactionMap.get("feeAmtWaived");
            BigDecimal penaltyAmtPaid = (BigDecimal) transactionMap.get("penaltyAmtPaid");
            BigDecimal penaltyAmtWaived = (BigDecimal) transactionMap.get("penaltyAmtWaived");


            BigDecimal selfPrincipalAmountPaid = (BigDecimal) transactionMap.get("selfPrincipalPaid");
            BigDecimal selfPrincipalAmountWrittenOff = (BigDecimal) transactionMap.get("selfPrincipalWrittenOff");
            BigDecimal selfInterestAmountPaid = (BigDecimal) transactionMap.get("selfInterestPaid");
            BigDecimal selfInterestAmountWaived = (BigDecimal) transactionMap.get("selfInterestWaived");
            BigDecimal partnerPrincipalAmountPaid = (BigDecimal) transactionMap.get("partnerPrincipalPaid");
            BigDecimal partnerPrincipalAmountWrittenOff = (BigDecimal) transactionMap.get("partnerPrincipalWrittenOff");
            BigDecimal partnerInterestAmountPaid = (BigDecimal) transactionMap.get("partnerInterestPaid");
            BigDecimal partnerInterestAmountWaived = (BigDecimal) transactionMap.get("partnerInterestWaived");



            BigDecimal principalAmt = principalAmtPaid.add(principalAmtWrittenoff);
            BigDecimal interestAmt = interestAmtPaid.add(interestAmtWaived);
            BigDecimal feeAmt = feeAmtPaid.add(feeAmtWaived);
            BigDecimal penaltyAmt = penaltyAmtPaid.add(penaltyAmtWaived);

            BigDecimal selfPrincipalAmount = selfPrincipalAmountPaid.add(selfPrincipalAmountWrittenOff);
            BigDecimal selfInterestAmount = selfInterestAmountPaid.add(selfInterestAmountWaived);
            BigDecimal partnerPrincipalAmount = partnerPrincipalAmountPaid.add(partnerPrincipalAmountWrittenOff);
            BigDecimal partnerInterestAmount = partnerInterestAmountPaid.add(partnerInterestAmountWaived);




            List<LoanSchedulePeriodData> loanSchedulePeriodDatas = scheduleDate.get(loanId);
            if (loanSchedulePeriodDatas != null) {
                List<LoanSchedulePeriodData> updatedPeriodData = new ArrayList<>(loanSchedulePeriodDatas.size());
                for (LoanSchedulePeriodData loanSchedulePeriodData : loanSchedulePeriodDatas) {
                    BigDecimal principalPaid = null;
                    BigDecimal interestPaid = null;
                    BigDecimal feeChargesPaid = null;
                    BigDecimal penaltyChargesPaid = null;

                    BigDecimal selfPrincipalPaid = null;
                    BigDecimal selfInterestPaid = null;
                    BigDecimal partnerPrincipalPaid = null;
                    BigDecimal partnerInterestPaid = null;


                    Boolean isComplete = true;
                    if (loanSchedulePeriodData.principalDue().compareTo(principalAmt) > 0) {
                        principalPaid = principalAmt;
                        selfPrincipalPaid =selfPrincipalAmountPaid;
                        partnerPrincipalPaid = selfPrincipalAmountPaid;
                        principalAmt = BigDecimal.ZERO;
                        selfPrincipalAmountPaid = BigDecimal.ZERO;
                        selfPrincipalAmountPaid = BigDecimal.ZERO;
                        isComplete = false;
                    } else {
                        principalPaid = loanSchedulePeriodData.principalDue();
                        selfPrincipalPaid = loanSchedulePeriodData.selfPrincipal();
                        partnerPrincipalPaid = loanSchedulePeriodData.partnerPrincipal();
                        principalAmt = principalAmt.subtract(loanSchedulePeriodData.principalDue());
                        selfPrincipalAmount = selfPrincipalAmount.subtract(loanSchedulePeriodData.selfPrincipal());
                        partnerPrincipalAmount = partnerPrincipalAmount.subtract(loanSchedulePeriodData.partnerPrincipal());
                    }

                    if (loanSchedulePeriodData.interestDue().compareTo(interestAmt) > 0) {
                        interestPaid = interestAmt;
                        selfInterestPaid = selfInterestAmount ;
                        partnerInterestPaid = partnerInterestAmount;
                        interestAmt = BigDecimal.ZERO;
                        interestAmt = BigDecimal.ZERO;
                        interestAmt = BigDecimal.ZERO;
                        isComplete = false;
                    } else {
                        interestPaid = loanSchedulePeriodData.interestDue();
                        selfInterestPaid = loanSchedulePeriodData.selfInterestCharged();
                        partnerInterestPaid =  loanSchedulePeriodData.partnerInterestCharged();
                        interestAmt = interestAmt.subtract(loanSchedulePeriodData.interestDue());
                        selfInterestAmount  = selfInterestAmount.subtract(loanSchedulePeriodData.selfInterestCharged());
                        partnerInterestAmount = partnerInterestAmount.subtract(loanSchedulePeriodData.partnerInterestCharged());
                    }
                    if (loanSchedulePeriodData.feeChargesDue().compareTo(feeAmt) > 0) {
                        feeChargesPaid = feeAmt;
                        feeAmt = BigDecimal.ZERO;
                        isComplete = false;
                    } else {
                        feeChargesPaid = loanSchedulePeriodData.feeChargesDue();
                        feeAmt = feeAmt.subtract(loanSchedulePeriodData.feeChargesDue());
                    }
                    if (loanSchedulePeriodData.penaltyChargesDue().compareTo(penaltyAmt) > 0) {
                        penaltyChargesPaid = penaltyAmt;
                        penaltyAmt = BigDecimal.ZERO;
                        isComplete = false;
                    } else {
                        penaltyChargesPaid = loanSchedulePeriodData.penaltyChargesDue();
                        penaltyAmt = penaltyAmt.subtract(loanSchedulePeriodData.penaltyChargesDue());
                    }

                    LoanSchedulePeriodData periodData = LoanSchedulePeriodData.withPaidDetail(loanSchedulePeriodData, isComplete,
                            principalPaid, interestPaid, feeChargesPaid, penaltyChargesPaid, null,selfPrincipalPaid ,selfInterestPaid,partnerPrincipalPaid,partnerInterestPaid);
                    updatedPeriodData.add(periodData);
                }
                loanSchedulePeriodDatas.clear();
                loanSchedulePeriodDatas.addAll(updatedPeriodData);
            }
        }
    }

    private static final class OriginalScheduleExtractor implements ResultSetExtractor<Map<Long, List<LoanSchedulePeriodData>>> {

        private final String schema;

        OriginalScheduleExtractor(final String loanIdsAsString, DatabaseSpecificSQLGenerator sqlGenerator) {
            final StringBuilder scheduleDetail = new StringBuilder();
            scheduleDetail.append("select ml.id as loanId, mr.duedate as dueDate, mr.principal_amount as principalAmount, ");
            scheduleDetail.append(
                    "mr.interest_amount as interestAmount, mr.fee_charges_amount as feeAmount, mr.penalty_charges_amount as penaltyAmount  ");
            scheduleDetail.append("from m_loan ml  INNER JOIN m_loan_repayment_schedule_history mr on mr.loan_id = ml.id ");
            scheduleDetail.append("where mr.duedate  < "
                    + sqlGenerator.subDate(sqlGenerator.currentDate(), "COALESCE(ml.grace_on_arrears_ageing, 0)", "day") + " and ");
            scheduleDetail.append("ml.id IN(").append(loanIdsAsString).append(") and  mr.version = (");
            scheduleDetail.append("select max(lrs.version) from m_loan_repayment_schedule_history lrs where mr.loan_id = lrs.loan_id");
            scheduleDetail.append(") order by ml.id,mr.duedate");
            this.schema = scheduleDetail.toString();
        }

        @Override
        public Map<Long, List<LoanSchedulePeriodData>> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, List<LoanSchedulePeriodData>> scheduleDate = new HashMap<>();

            while (rs.next()) {
                Long loanId = rs.getLong("loanId");

                List<LoanSchedulePeriodData> periodDatas = scheduleDate.get(loanId);
                if (periodDatas == null) {
                    periodDatas = new ArrayList<>();
                    scheduleDate.put(loanId, periodDatas);
                }

                periodDatas.add(fetchLoanSchedulePeriodData(rs));
            }

            return scheduleDate;
        }

        private LoanSchedulePeriodData fetchLoanSchedulePeriodData(ResultSet rs) throws SQLException {
            final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueDate");
            final BigDecimal principalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalAmount");
            final BigDecimal interestDueOnPrincipalOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestAmount");
            final BigDecimal totalInstallmentAmount = principalDue.add(interestDueOnPrincipalOutstanding);
            final BigDecimal feeChargesDueForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeAmount");
            final BigDecimal penaltyChargesDueForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyAmount");
            final Integer periodNumber = null;
            final LocalDate fromDate = null;
            final BigDecimal principalOutstanding = null;
            final BigDecimal totalDueForPeriod = null;
            final BigDecimal selfPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfPrincipal");
            final BigDecimal partnerPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerPrincipal");
            final BigDecimal selfInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfInterestCharged");
            final BigDecimal partnerInterestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerInterestCharged");
            final BigDecimal selfDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "selfDue");
            final BigDecimal partnerDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "partnerDue");

            return LoanSchedulePeriodData.repaymentOnlyPeriod(periodNumber, fromDate, dueDate, principalDue, principalOutstanding,
                    interestDueOnPrincipalOutstanding, feeChargesDueForPeriod, penaltyChargesDueForPeriod, totalDueForPeriod,
                    totalInstallmentAmount,selfPrincipal,partnerPrincipal,selfInterestCharged,partnerInterestCharged,selfDue,partnerDue, null,null);

        }
    }

    @SuppressWarnings("unused")
    @Override
    public void businessEventToBeExecuted(Map<BusinessEntity, Object> businessEventEntity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
        Loan loan = null;
        Object loanEntity = businessEventEntity.get(BusinessEntity.LOAN);
        Object loanTransactionEntity = businessEventEntity.get(BusinessEntity.LOAN_TRANSACTION);
        Object loanAdjustTransactionEntity = businessEventEntity.get(BusinessEntity.LOAN_ADJUSTED_TRANSACTION);
        Object loanChargeEntity = businessEventEntity.get(BusinessEntity.LOAN_CHARGE);
        if (loanEntity != null) {
            loan = (Loan) loanEntity;
        } else if (loanTransactionEntity != null) {
            LoanTransaction loanTransaction = (LoanTransaction) loanTransactionEntity;
            loan = loanTransaction.getLoan();
        } else if (loanAdjustTransactionEntity != null) {
            LoanTransaction loanTransaction = (LoanTransaction) loanAdjustTransactionEntity;
            loan = loanTransaction.getLoan();
        } else if (loanChargeEntity != null) {
            LoanCharge loanCharge = (LoanCharge) loanChargeEntity;
            loan = loanCharge.getLoan();
        }
        if (loan != null && loan.isOpen() && loan.repaymentScheduleDetail().isInterestRecalculationEnabled()
                && loan.loanProduct().isArrearsBasedOnOriginalSchedule()) {
            updateLoanArrearsAgeingDetailsWithOriginalSchedule(loan);
        } else {
            updateLoanArrearsAgeingDetails(loan);
        }
    }

    private class DisbursementEventListener implements BusinessEventListener {

        @SuppressWarnings("unused")
        @Override
        public void businessEventToBeExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            // TODO Auto-generated method stub

        }

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            Object loanEntity = businessEventEntity.get(BusinessEntity.LOAN);
            if (loanEntity != null) {
                Loan loan = (Loan) loanEntity;
                updateLoanArrearsAgeingDetails(loan);
            }

        }

    }
}
