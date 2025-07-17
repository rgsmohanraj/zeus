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
package org.vcpl.lms.portfolio.loanproduct.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.vcpl.lms.accounting.common.AccountingEnumerations;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.infrastructure.core.domain.JdbcSupport;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.vcpl.lms.infrastructure.entityaccess.domain.FineractEntityType;
import org.vcpl.lms.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.charge.service.ChargeReadPlatformService;
import org.vcpl.lms.portfolio.common.service.CommonEnumerations;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.PmtCalcEnum;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductBorrowerCycleVariationData;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductData;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductGuaranteeData;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductInterestRecalculationData;
import org.vcpl.lms.portfolio.loanproduct.domain.*;
import org.vcpl.lms.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.vcpl.lms.portfolio.rate.data.RateData;
import org.vcpl.lms.portfolio.rate.service.RateReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class LoanProductReadPlatformServiceImpl implements LoanProductReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final RateReadService rateReadService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;


    @Autowired
    public LoanProductReadPlatformServiceImpl(final PlatformSecurityContext context,
                                              final ChargeReadPlatformService chargeReadPlatformService, final JdbcTemplate jdbcTemplate,
                                              final FineractEntityAccessUtil fineractEntityAccessUtil, final RateReadService rateReadService,
                                              DatabaseSpecificSQLGenerator sqlGenerator) {
        this.context = context;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.jdbcTemplate = jdbcTemplate;
        this.fineractEntityAccessUtil = fineractEntityAccessUtil;
        this.rateReadService = rateReadService;
        this.sqlGenerator = sqlGenerator;

    }

    @Override
    public LoanProductData retrieveLoanProduct(final Long loanProductId) {

        try {
            final Collection<ChargeData> charges = this.chargeReadPlatformService.retrieveLoanProductCharges(loanProductId);
            final Collection<LoanProductFeesChargesData> colendingCharge =this.chargeReadPlatformService.retrieveColendingCharge(loanProductId);
            final List<LoanProductFeesChargesData> colendingFees=colendingProductFees(colendingCharge);
            final Collection<LoanProductFeesChargesData>  colendingCharges=colendingProductCharge(colendingCharge);
            final Collection<LoanProductFeesChargesData> overDueCharge = this.chargeReadPlatformService.retrieveOverdueCharge(loanProductId);

//            for(LoanProductFeesChargesData charge :colendingProductCharge)
//            {
//               final String chargeType=charge.getChargeType();
//               if(chargeType.equals("charge")){
//
//                   colendingCharge.add(charge);}
//               else if(chargeType.equals("fees")){
//                   colendingFees.add(charge);}}

            final Collection<RateData> rates = this.rateReadService.retrieveProductLoanRates(loanProductId);
            final Collection<LoanProductBorrowerCycleVariationData> borrowerCycleVariationDatas = retrieveLoanProductBorrowerCycleVariations(
                    loanProductId);
            final LoanProductMapper rm = new LoanProductMapper(charges, borrowerCycleVariationDatas, rates,colendingFees,colendingCharges, overDueCharge);
            final String sql = "select " + rm.loanProductSchema() + " where lp.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanProductId }); // NOSONAR

        } catch (final EmptyResultDataAccessException e) {
            throw new LoanProductNotFoundException(loanProductId, e);
        }
    }

    private Collection<LoanProductFeesChargesData> colendingProductCharge(Collection<LoanProductFeesChargesData> colendingCharge) {
        List<LoanProductFeesChargesData> loanProductFeesChargesData =new ArrayList<LoanProductFeesChargesData>();
        for(LoanProductFeesChargesData list : colendingCharge)
        {
            final String chargeType=list.getChargeType();
            if(chargeType.equalsIgnoreCase("charge")){
                final Integer colendingCharges=list.getChargeId();
                final Integer loanProductId=  list.getLoanProductId();
                final Integer chargeId= list.getChargeId();
                final BigDecimal selfShare=list.getSelfCharge();
                final BigDecimal partnerShare=list.getPartnerCharge();
                final String chargeName = list.getName();
                loanProductFeesChargesData.add(new LoanProductFeesChargesData(null,null,colendingCharges,loanProductId,chargeId,selfShare,partnerShare,null,null,chargeType,chargeName, null, null));
            }

        }
        return loanProductFeesChargesData;
    }

    private List<LoanProductFeesChargesData> colendingProductFees(Collection<LoanProductFeesChargesData> colendingProductCharge) {



        List<LoanProductFeesChargesData> loanProductFeesChargesData =new ArrayList<LoanProductFeesChargesData>();
            for(LoanProductFeesChargesData list : colendingProductCharge)
            {
                final String chargeType=list.getChargeType();
                if(chargeType.equalsIgnoreCase("fees")){
                    final Integer colendingFees=list.getChargeId();
                    final Integer loanProductId=  list.getLoanProductId();
                    final Integer chargeId= list.getChargeId();
                    final BigDecimal selfShare=list.getSelfCharge();
                    final BigDecimal partnerShare=list.getPartnerCharge();
                    final String chargeName = list.getName();

                    final BigDecimal selfCharge=null;
                    final BigDecimal partnerCharge=null;

                    loanProductFeesChargesData.add(new LoanProductFeesChargesData(null,colendingFees,null,loanProductId,chargeId,selfCharge,partnerCharge,selfShare,partnerShare,chargeType,chargeName, null,null));
                }

               }
        return loanProductFeesChargesData;

    }

    @Override
    public Collection<LoanProductBorrowerCycleVariationData> retrieveLoanProductBorrowerCycleVariations(final Long loanProductId) {
        final LoanProductBorrowerCycleMapper rm = new LoanProductBorrowerCycleMapper();
        final String sql = "select " + rm.schema() + " where bc.loan_product_id=?  order by bc.borrower_cycle_number,bc.value_condition";
        return this.jdbcTemplate.query(sql, rm, new Object[] { loanProductId }); // NOSONAR
    }

    @Override
    public Collection<LoanProductData> retrieveAllLoanProducts() {

        this.context.authenticatedUser();

        final LoanProductMapper rm = new LoanProductMapper(null, null, null,null,null, null);

        String sql = "select " + rm.loanProductSchema();

        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.LOAN_PRODUCT);
        if (inClause != null && !inClause.trim().isEmpty()) {
            sql += " where lp.id in ( " + inClause + " ) ";
        }

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public Collection<LoanProductData> retrieveAllLoanProductsForLookup(String inClause) {

        this.context.authenticatedUser();

        final LoanProductLookupMapper rm = new LoanProductLookupMapper(sqlGenerator);

        String sql = "select " + rm.schema();

        if (inClause != null && !inClause.trim().isEmpty()) {
            sql += " where lp.id in (" + inClause + ") ";
            // Here no need to check injection as this is internal where clause
            // SQLInjectionValidator.validateSQLInput(inClause);
        }

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public Collection<LoanProductData> retrieveAllLoanProductsForLookup() {
        return retrieveAllLoanProductsForLookup(false);
    }

    @Override
    public Collection<LoanProductData> retrieveAllLoanProductsForLookup(final boolean activeOnly) {
        this.context.authenticatedUser();

        final LoanProductLookupMapper rm = new LoanProductLookupMapper(sqlGenerator);

        String sql = "select ";
        if (activeOnly) {
            sql += rm.activeOnlySchema();
        } else {
            sql += rm.schema();
        }

        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.LOAN_PRODUCT);
        if (inClause != null && !inClause.trim().isEmpty()) {
            if (activeOnly) {
                sql += " and id in ( " + inClause + " )";
            } else {
                sql += " where id in ( " + inClause + " ) ";
            }
        }

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public LoanProductData retrieveNewLoanProductDetails() {
        return LoanProductData.sensibleDefaultsForNewLoanProductCreation();
    }

    private static final class LoanProductMapper implements RowMapper<LoanProductData> {

        private final Collection<ChargeData> charges;

        private final Collection<LoanProductBorrowerCycleVariationData> borrowerCycleVariationDatas;

        private final Collection<RateData> rates;

        private final Collection<LoanProductFeesChargesData> colendingfees;
        private final Collection<LoanProductFeesChargesData> colendingCharge;
        private final Collection<LoanProductFeesChargesData> overDueCharges;


        LoanProductMapper(final Collection<ChargeData> charges,
                          final Collection<LoanProductBorrowerCycleVariationData> borrowerCycleVariationDatas,
                          final Collection<RateData> rates,final Collection<LoanProductFeesChargesData> colendingfees,
                          final Collection<LoanProductFeesChargesData> colendingCharge, final Collection<LoanProductFeesChargesData> overDueCharges) {
            this.charges = charges;
            this.borrowerCycleVariationDatas = borrowerCycleVariationDatas;
            this.rates = rates;
            this.colendingfees=colendingfees;
            this.colendingCharge=colendingCharge;
            this.overDueCharges=overDueCharges;
        }

        public String loanProductSchema() {
            return "lp.id as id, lp.fund_id as fundId, f.name as fundName, lp.loan_transaction_strategy_id as transactionStrategyId, ltps.name as transactionStrategyName, "
                    + "lp.name as name, lp.short_name as shortName,lp.loan_acc_no_preference as loanAccNoPreference, lp.description as description, "
                    + "lp.principal_amount as principal, lp.min_principal_amount as minPrincipal, lp.max_principal_amount as maxPrincipal, lp.currency_code as currencyCode, lp.currency_digits as currencyDigits, lp.currency_multiplesof as inMultiplesOf, "
                    + "lp.nominal_interest_rate_per_period as interestRatePerPeriod, lp.min_nominal_interest_rate_per_period as minInterestRatePerPeriod, lp.max_nominal_interest_rate_per_period as maxInterestRatePerPeriod, lp.interest_period_frequency_enum as interestRatePerPeriodFreq, "
                    + "lp.annual_nominal_interest_rate as annualInterestRate, lp.interest_method_enum as interestMethod, lp.interest_calculated_in_period_enum as interestCalculationInPeriodMethod,lp.allow_partial_period_interest_calcualtion as allowPartialPeriodInterestCalcualtion, "
                    + "lp.repay_every as repaidEvery, lp.repayment_period_frequency_enum as repaymentPeriodFrequency, lp.number_of_repayments as numberOfRepayments, lp.min_number_of_repayments as minNumberOfRepayments, lp.max_number_of_repayments as maxNumberOfRepayments, "
                    + "lp.grace_on_principal_periods as graceOnPrincipalPayment, lp.recurring_moratorium_principal_periods as recurringMoratoriumOnPrincipalPeriods, lp.grace_on_interest_periods as graceOnInterestPayment, lp.grace_interest_free_periods as graceOnInterestCharged, lp.grace_on_arrears_ageing as graceOnArrearsAgeing, lp.overdue_days_for_npa as overdueDaysForNPA, "
                    + "lp.min_days_between_disbursal_and_first_repayment As minimumDaysBetweenDisbursalAndFirstRepayment,lp.emi_rounding_mode as emiroundingMode,lp.emi_multiples_of as emiMultiplesOfSelected, "
                    + "lp.amortization_method_enum as amortizationMethod, lp.arrearstolerance_amount as tolerance, "
                    + "lp.accounting_type as accountingType, lp.include_in_borrower_cycle as includeInBorrowerCycle,lp.use_borrower_cycle as useBorrowerCycle, lp.start_date as startDate, lp.close_date as closeDate,  "
                    + "lp.allow_multiple_disbursals as multiDisburseLoan, lp.max_disbursals as maxTrancheCount, lp.max_outstanding_loan_balance as outstandingLoanBalance, "
                    + "lp.disallow_expected_disbursements as disallowExpectedDisbursements, lp.allow_approved_disbursed_amounts_over_applied as allowApprovedDisbursedAmountsOverApplied, lp.over_applied_calculation_type as overAppliedCalculationType, over_applied_number as overAppliedNumber,"
                    + "lp.repayment_strategy_for_npa_id as repaymentStrategyForNpaId, lp.repayment_strategy_for_npa as repaymentStrategyForNpa,lp.emi_decimal_regex as emiDecimalRegexSelected,lp.interest_decimal_regex as interestDecimalRegexSelected,lp.interest_Rounding_Mode as interestRoundingModeSelected,lp.interest_decimal as interestDecimalSelected, "
                    + "lp.days_in_month_enum as daysInMonth, lp.days_in_year_enum as daysInYear, lp.interest_recalculation_enabled as isInterestRecalculationEnabled, "
                    + "lp.can_define_fixed_emi_amount as canDefineInstallmentAmount, lp.instalment_amount_in_multiples_of as installmentAmountInMultiplesOf,lp.servicer_fee_interest_config_enabled as servicerFeeInterestConfigEnabled,lp.servicer_fee_charges_config_enabled as servicerFeeChargesConfigEnabled,lp.enable_backdated_disbursement as enableBackDatedDisbursement, "
                    + "lpr.pre_close_interest_calculation_strategy as preCloseInterestCalculationStrategy, "
                    + "lpr.id as lprId, lpr.product_id as productId, lpr.compound_type_enum as compoundType, lpr.reschedule_strategy_enum as rescheduleStrategy, "
                    + "lpr.rest_frequency_type_enum as restFrequencyEnum, lpr.rest_frequency_interval as restFrequencyInterval, "
                    + "lpr.rest_frequency_nth_day_enum as restFrequencyNthDayEnum, "
                    + "lpr.rest_frequency_weekday_enum as restFrequencyWeekDayEnum, " + "lpr.rest_frequency_on_day as restFrequencyOnDay, "
                    + "lpr.arrears_based_on_original_schedule as isArrearsBasedOnOriginalSchedule, "
                    + "lpr.compounding_frequency_type_enum as compoundingFrequencyTypeEnum, lpr.compounding_frequency_interval as compoundingInterval, "
                    + "lpr.compounding_frequency_nth_day_enum as compoundingFrequencyNthDayEnum, "
                    + "lpr.compounding_frequency_weekday_enum as compoundingFrequencyWeekDayEnum, "
                    + "lpr.compounding_frequency_on_day as compoundingFrequencyOnDay, "
                    + "lpr.is_compounding_to_be_posted_as_transaction as isCompoundingToBePostedAsTransaction, "
                    + "lpr.allow_compounding_on_eod as allowCompoundingOnEod, " + "lp.hold_guarantee_funds as holdGuaranteeFunds, "
                    + "lp.principal_threshold_for_last_installment as principalThresholdForLastInstallment, "
                    + "lp.fixed_principal_percentage_per_installment fixedPrincipalPercentagePerInstallment, "
                    + "lp.sync_expected_with_disbursement_date as syncExpectedWithDisbursementDate, "
                    + "lpg.id as lpgId, lpg.mandatory_guarantee as mandatoryGuarantee, "
                    + "lpg.minimum_guarantee_from_own_funds as minimumGuaranteeFromOwnFunds, lpg.minimum_guarantee_from_guarantor_funds as minimumGuaranteeFromGuarantor, "
                    + "lp.account_moves_out_of_npa_only_on_arrears_completion as accountMovesOutOfNPAOnlyOnArrearsCompletion, "
                    + "curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol, lp.external_id as externalId, "
                    + "lca.id as lcaId, lca.amortization_method_enum as amortizationBoolean, lca.interest_method_enum as interestMethodConfigBoolean, "
                    + "lca.loan_transaction_strategy_id as transactionProcessingStrategyBoolean,lca.interest_calculated_in_period_enum as interestCalcPeriodBoolean, lca.repayment_strategy_for_npa as repaymentStrategyForNpa, lca.arrearstolerance_amount as arrearsToleranceBoolean, "
                    + "lca.repay_every as repaymentFrequencyBoolean, lca.moratorium as graceOnPrincipalAndInterestBoolean, lca.grace_on_arrears_ageing as graceOnArrearsAgingBoolean, "
                    + "lp.is_linked_to_floating_interest_rates as isLinkedToFloatingInterestRates, "
                    + "lfr.floating_rates_id as floatingRateId, " + "fr.name as floatingRateName, "
                    + "lfr.interest_rate_differential as interestRateDifferential, "
                    + "lfr.min_differential_lending_rate as minDifferentialLendingRate, "
                    + "lfr.default_differential_lending_rate as defaultDifferentialLendingRate, "
                    + "lfr.max_differential_lending_rate as maxDifferentialLendingRate, "
                    + "lfr.is_floating_interest_rate_calculation_allowed as isFloatingInterestRateCalculationAllowed, "
                    + "lp.allow_variabe_installments as isVariableIntallmentsAllowed, " + "lvi.minimum_gap as minimumGap, "
                    + "lvi.maximum_gap as maximumGap, "
                    + "lp.can_use_for_topup as canUseForTopup, lp.is_equal_amortization as isEqualAmortization,"
                    + "lp.allow_age_limits as allowAgeLimits,lp.broken_interest_calculation_period as brokenInterestCalculationPeriod,"
                    + "lp.loan_foreclosure_strategy as loanForeclosureStrategy,lp.broken_interest_days_in_years as brokenInterestDaysInYears,"
                    + "lp.broken_interest_days_in_month as brokenInterestDaysInMonth,"
                    + "lp.allow_approval_over_amount_applied as allowApprovalOverAmountApplied,"
                    + "lp.over_amount_details as overAmountDetails,lp.enable_colending_loan as enableColendingLoan,lp.by_percentage_split as byPercentageSplit,lp.self_principal_share as selfPrincipalShare,lp.penny_drop_enabled as isPennyDropEnabled,lp.bank_disbursement_enabled as isBankDisbursementEnabled,"
                    + "lp.self_fee_share as selfFeeShare,lp.self_penalty_share as selfPenaltyShare,lp.self_overpaid_shares as selfOverpaidShares,"
                    + "lp.self_interest_rate as selfInterestRate,lp.principal_share as principalShare,lp.fee_share as feeShare,lp.penalty_share as penaltyShare,"
                    + "lp.overpaid_share as overpaidShare,lp.interest_rate as interestRate,lp.partner_principal_share as partnerPrincipalShare,lp.partner_fee_share as partnerFeeShare,lp.partner_penalty_share as partnerPenaltyShare,lp.partner_overpaid_share as partnerOverpaidShare,lp.partner_interest_rate as partnerInterestRate,lp.partner_id as partnerId,lp.loantype_cv_id as loantypeCvId,lp.framework_cv_id as frameWorkId,lp.enable_charge_wise_bifacation as enableChargeWiseBifacation,"
                    + "lp.enable_fees_wise_bifacation as enableFeesWiseBifacation, lp.enable_charge_wise_bifacation as enableChargeWiseBifacation, lp.enable_overdue as enableOverdue, "
                    + "lp.insurance_applicability_cv_id as insuranceApplicabilityId,lp.fldg_logic_cv_id as fldgLogicId,lp.penal_invoice_cv_id as penalInvoiceId,lp.multiple_disbursement_cv_id as multipleDisbursementId,lp.tranche_clubbing_cv_id as trancheClubbingId,lp.repayment_schedule_update_allowed_cv_id as repaymentScheduleUpdateAllowedId,lp.broken_Strategy_enum as brokenStrategy,lp.collection_enum as collectionModeId,lp.disbursement_enum as disbursementId,lp.transaction_type_preference_enum as transactionTypePreference,lp.asset_class_cv_id as assetClassId,lp.class_cv_id as loanProductClassId,lp.type_cv_id as loanProductTypeId,"
                    + "lp.select_charge as selectCharge,lp.select_accepted_dates as selectAcceptedDates,lp.accepted_date_type as acceptedDateType,lp.accepted_start_date as acceptedStartDate,lp.accepted_end_date as acceptedEndDate,lp.accepted_date as acceptedDate,lp.apply_prepaid_locking_period as applyPrepaidLockingPeriod,"
                    + "lp.prepay_locking_period as prepayLockingPeriod,lp.apply_foreclosure_locking_period as applyForeclosureLockingPeriod,"
                    + "lp.foreclosure_locking_period as foreclosureLockingPeriod,lp.use_days_in_month_for_loan_provisioning as useDaysInMonthForLoanProvisioning,"
                    + "lp.divide_by_thirty_for_partial_period as divideByThirtyForPartialPeriod,lp.aum_slab_rate as aumSlabRate,lp.gst_liability_by_vcpl as gstLiabilityByVcpl,lp.gst_liability_by_partner as gstLiabilityByPartner,lp.disbursement_account_number as disbursementAccountNumber,lp.collection_account_number  as collectionAccountNumber, al.min_age as minimumAge, al.max_age as maximumAge, "
                    + "lp.vcpl_share_in_broken_interest as vcplShareInBrokenInterest,lp.partner_share_in_broken_interest as partnerShareInBrokenInterest,lp.monitoring_trigger_par_30 as monitoringTriggerPar30,lp.monitoring_trigger_par_90 as monitoringTriggerPar90,cv.code_value as loanTypeValue, "
                    + "cv.code_value as loanTypeValue,cvf.code_value as frameWorkValue,cvi.code_value as insuranceApplicabilityValue,cvfl.code_value as fldgLogicValue,cvfg.code_value as penalInvoiceValue,cvfh.code_value as multiDisbursementValue,cvfj.code_value as trancheClubbingValue,cvfk.code_value as repaymentScheduleUpdateAllowedValue, "
                    + "cvac.code_value as assetClassValue,cvlc.code_value as loanProductClassValue,cvlt.code_value as loanProductTypeValue,dedupe_enabled as enableDedupe, dedupe_enum as dedupeBy,"
                    + " lp.disbursement_bank_acc_cv_id as disbursementBankAccountId, cvba.code_value as disbursementBankAccountValue,mpcc.advance_appropriation_on as advanceAppropriationSelected ,mpcc.advance_entry_enabled as advanceEntryEnabled,mpcc.interest_benefit_enabled as interestBenefitEnabled, "
                    + " mpcc.foreclosure_on_due_date_interest as foreclosureOnDueDateInterest, mpcc.foreclosure_on_due_date_charge as foreclosureOnDueDateCharge,mpcc.foreclosure_other_than_due_date_interest as foreclosureOtherThanDueDateInterest ,mpcc.foreclosure_other_than_due_date_charge as foreclosureOtherThanDueDateCharge, "
                    + " mpcc.foreclosure_one_month_overdue_interest as foreclosureOneMonthOverdueInterest, mpcc.foreclosure_one_month_overdue_charge as foreclosureOneMonthOverdueCharge,mpcc.foreclosure_short_paid_interest as foreclosureShortPaidInterest ,mpcc.foreclosure_short_paid_interest_charge as foreclosureShortPaidInterestCharge, "
                    + " mpcc.foreclosure_principal_short_paid_interest as foreclosurePrincipalShortPaidInterest, mpcc.foreclosure_principal_short_paid_charge as foreclosurePrincipalShortPaidCharge,mpcc.foreclosure_two_months_overdue_interest as foreclosureTwoMonthsOverdueInterest ,mpcc.foreclosure_two_months_overdue_charge as foreclosureTwoMonthsOverdueCharge,mpcc.foreclosure_pos_advance_on_due_date as foreclosurePosAdvanceOnDueDate, "
                    + " mpcc.foreclosure_advance_on_due_date_interest as foreclosureAdvanceOnDueDateInterest, mpcc.foreclosure_advance_on_due_date_charge as foreclosureAdvanceOnDueDateCharge,mpcc.foreclosure_pos_advance_other_than_due_date as foreclosurePosAdvanceOtherThanDueDate,mpcc.foreclosure_advance_after_due_date_interest as foreclosureAdvanceAfterDueDateInterest ,mpcc.foreclosure_advance_after_due_date_charge as foreclosureAdvanceAfterDueDateCharge,mpcc.foreclosure_pos_advance_other_than_due_date as foreclosureAdvancePosOtherThanDueDate, "
                    + " mpcc.foreclosure_backdated_short_paid_interest as foreclosureBackdatedShortPaidInterest, mpcc.foreclosure_backdated_short_paid_interest_charge as foreclosureBackdatedShortPaidInterestCharge,mpcc.foreclosure_backdated_fully_paid_interest as foreclosureBackdatedFullyPaidInterest ,mpcc.foreclosure_backdated_fully_paid_interest_charge as foreclosureBackdatedFullyPaidInterestCharge, "
                    + " mpcc.foreclosure_backdated_short_paid_principal_interest as foreclosureBackdatedShortPaidPrincipalInterest, mpcc.foreclosure_backdated_short_paid_principal_charge as foreclosureBackdatedShortPaidPrincipalCharge,mpcc.foreclosure_backdated_fully_paid_emi_interest as foreclosureBackdatedFullyPaidEmiInterest ,mpcc.foreclosure_backdated_fully_paid_emi_charge as foreclosureBackdatedFullyPaidEmiCharge, "
                    + " mpcc.foreclosure_backdated_advance_interest as foreclosureBackdatedAdvanceInterest, mpcc.foreclosure_backdated_advance_charge as foreclosureBackdatedAdvanceCharge,mpcc.advance_appropriation_against_on as advanceAppropriationAgainstOnSelected,lp.emi_days_in_month as emiDaysInMonthSelected,lp.emi_days_in_year as emiDaysInYearSelectd, lp.emi_calc_logic as emiCalcSelected, "
                    + " mpcc.foreclosure_method_type as foreclosureMethodType, mpcc.cooling_off_applicability as coolingOffApplicability,mpcc.cooling_off_threshold_days as coolingOffThresholdDays,mpcc.cooling_off_interest_and_charge_applicability as coolingOffInterestAndChargeApplicability,mpcc.cooling_off_interest_logic_applicability as coolingOffInterestLogicApplicability,mpcc.cooling_off_days_in_year as coolingOffDaysInYear,mpcc.cooling_off_rounding_mode as coolingOffRoundingMode,mpcc.cooling_off_rounding_decimals as coolingOffRoundingDecimals "
                    + " from m_product_loan lp " + " left join m_fund f on f.id = lp.fund_id "
                    + " left join m_product_loan_age_limits_config al on al.product_loan_id = lp.id "
                    +  "left join m_code_value cv on cv.id = lp.loantype_cv_id "
                    +  "left join m_code_value cvf on cvf.id = lp.framework_cv_id "
                    +  "left join m_code_value cvi on cvi.id = lp.insurance_applicability_cv_id "
                    +  "left join m_code_value cvfl on cvfl.id = lp.fldg_logic_cv_id "
                    +  "left join m_code_value cvfg on cvfg.id = lp.penal_invoice_cv_id "
                    +  "left join m_code_value cvfh on cvfh.id = lp.multiple_disbursement_cv_id "
                    +  "left join m_code_value cvfj on cvfj.id = lp.tranche_clubbing_cv_id "
                    +  "left join m_code_value cvfk on cvfk.id = lp.repayment_schedule_update_allowed_cv_id "
                    +  "left join m_code_value cvac on cvac.id = lp.asset_class_cv_id "
                    +  "left join m_code_value cvlc on cvlc.id = lp.class_cv_id "
                    +  "left join m_code_value cvlt on cvlt.id = lp.type_cv_id "
                    +  "left join m_code_value cvba on cvba.id = lp.disbursement_bank_acc_cv_id "
                    + " left join m_colending_accepted_dates cad on cad.product_loan_id = lp.id "
                    + " left join m_product_loan_recalculation_details lpr on lpr.product_id=lp.id "
                    + " left join m_product_loan_guarantee_details lpg on lpg.loan_product_id=lp.id "
                    + " left join ref_loan_transaction_processing_strategy ltps on ltps.id = lp.loan_transaction_strategy_id "
                    + " left join m_product_loan_configurable_attributes lca on lca.loan_product_id = lp.id "
                    + " left join m_product_loan_floating_rates as lfr on lfr.loan_product_id = lp.id "
                    + " left join m_floating_rates as fr on lfr.floating_rates_id = fr.id "
                    + " left join m_product_loan_variable_installment_config as lvi on lvi.loan_product_id = lp.id "
                    + " left join m_partner as p on p.id = lp.partner_id"
                    + " left join m_product_loan_collection_config as mpcc on mpcc.product_id = lp.id"
                    + " join m_currency curr on curr.code = lp.currency_code";

        }

        @Override
        public LoanProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");
            final String shortName = rs.getString("shortName");
            final String loanAccNoPreference = rs.getString("loanAccNoPreference");
            final String description = rs.getString("description");
            final Long fundId = JdbcSupport.getLong(rs, "fundId");

//            final Long classId = JdbcSupport.getLong(rs, "classId");
//            final Long typeId = JdbcSupport.getLong(rs, "typeId");
            final String fundName = rs.getString("fundName");
            final Long transactionStrategyId = JdbcSupport.getLong(rs, "transactionStrategyId");
            final String transactionStrategyName = rs.getString("transactionStrategyName");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                    currencyNameCode);

            final BigDecimal principal = rs.getBigDecimal("principal");
            final BigDecimal minPrincipal = rs.getBigDecimal("minPrincipal");
            final BigDecimal maxPrincipal = rs.getBigDecimal("maxPrincipal");
            final BigDecimal tolerance = rs.getBigDecimal("tolerance");

            final Integer numberOfRepayments = JdbcSupport.getInteger(rs, "numberOfRepayments");
            final Integer minNumberOfRepayments = JdbcSupport.getInteger(rs, "minNumberOfRepayments");
            final Integer maxNumberOfRepayments = JdbcSupport.getInteger(rs, "maxNumberOfRepayments");
            final Integer repaymentEvery = JdbcSupport.getInteger(rs, "repaidEvery");

            final Integer graceOnPrincipalPayment = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnPrincipalPayment");
            final Integer recurringMoratoriumOnPrincipalPeriods = JdbcSupport.getIntegerDefaultToNullIfZero(rs,
                    "recurringMoratoriumOnPrincipalPeriods");
            final Integer graceOnInterestPayment = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnInterestPayment");
            final Integer graceOnInterestCharged = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnInterestCharged");
            final Integer graceOnArrearsAgeing = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnArrearsAgeing");
            final Integer overdueDaysForNPA = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "overdueDaysForNPA");
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment = JdbcSupport.getInteger(rs,
                    "minimumDaysBetweenDisbursalAndFirstRepayment");

            final Integer accountingRuleId = JdbcSupport.getInteger(rs, "accountingType");
            final EnumOptionData accountingRuleType = AccountingEnumerations.accountingRuleType(accountingRuleId);

            final BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
            final BigDecimal minInterestRatePerPeriod = rs.getBigDecimal("minInterestRatePerPeriod");
            final BigDecimal maxInterestRatePerPeriod = rs.getBigDecimal("maxInterestRatePerPeriod");
            final BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");

            final boolean isLinkedToFloatingInterestRates = rs.getBoolean("isLinkedToFloatingInterestRates");
            final Integer floatingRateId = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "floatingRateId");
            final String floatingRateName = rs.getString("floatingRateName");
            final BigDecimal interestRateDifferential = rs.getBigDecimal("interestRateDifferential");
            final BigDecimal minDifferentialLendingRate = rs.getBigDecimal("minDifferentialLendingRate");
            final BigDecimal defaultDifferentialLendingRate = rs.getBigDecimal("defaultDifferentialLendingRate");
            final BigDecimal maxDifferentialLendingRate = rs.getBigDecimal("maxDifferentialLendingRate");
            final boolean isFloatingInterestRateCalculationAllowed = rs.getBoolean("isFloatingInterestRateCalculationAllowed");

            final boolean isVariableIntallmentsAllowed = rs.getBoolean("isVariableIntallmentsAllowed");
            final Integer minimumGap = rs.getInt("minimumGap");
            final Integer maximumGap = rs.getInt("maximumGap");

            final boolean allowAgeLimits = rs.getBoolean("allowAgeLimits");
            final Integer minimumAge = rs.getInt("minimumAge");
            final Integer maximumAge = rs.getInt("maximumAge");

//            final Boolean coBorrower = rs.getBoolean("coBorrower");
//            final Boolean eodBalance = rs.getBoolean("eodBalance");
//            final Boolean securedLoan = rs.getBoolean("securedLoan");
//            final Boolean nonEquatedInstallment = rs.getBoolean("nonEquatedInstallment");
//            final Boolean advanceEMI = rs.getBoolean("advanceEMI");
//            final Boolean termBasedOnLoanCycle = rs.getBoolean("termBasedOnLoanCycle");
//            final Boolean isNetOffApplied = rs.getBoolean("isNetOffApplied");
            final Boolean allowApprovalOverAmountApplied = rs.getBoolean("allowApprovalOverAmountApplied");
            final Boolean overAmountDetails = rs.getBoolean("overAmountDetails");

            final String brokenInterestCalculationPeriod = rs.getString("brokenInterestCalculationPeriod");
            final String repaymentStrategyForNpaId = rs.getString("repaymentStrategyForNpaId");
            final String loanForeclosureStrategy =rs.getString("loanForeclosureStrategy");
            final String brokenInterestDaysInYears = rs.getString("brokenInterestDaysInYears");
//            final String brokenInterestDaysInMonth = rs.getString("brokenInterestDaysInMonth");
//            final String brokenInterestStrategy = rs.getString("brokenInterestStrategy");

            final boolean enableColendingLoan=rs.getBoolean("enableColendingLoan");
            final boolean byPercentageSplit=rs.getBoolean("byPercentageSplit");
            final Integer selfPrincipalShare =rs.getInt("selfPrincipalShare");
            final Integer selfFeeShare =rs.getInt("selfFeeShare");
            final Integer selfPenaltyShare =rs.getInt("selfPenaltyShare");
            final Integer selfOverpaidShares =rs.getInt("selfOverpaidShares");
            final BigDecimal selfInterestRate =rs.getBigDecimal("selfInterestRate");
            final Integer principalShare =rs.getInt("principalShare");
            final Integer feeShare =rs.getInt("feeShare");
            final Integer penaltyShare =rs.getInt("penaltyShare");
            final Integer overpaidShare =rs.getInt("overpaidShare");
            final BigDecimal interestRate =rs.getBigDecimal("interestRate");
            final Integer partnerPrincipalShare =rs.getInt("partnerPrincipalShare");
            final Integer partnerFeeShare =rs.getInt("partnerFeeShare");
            final Integer partnerPenaltyShare =rs.getInt("partnerPenaltyShare");
            final Integer partnerOverpaidShare =rs.getInt("partnerOverpaidShare");
            final BigDecimal partnerInterestRate =rs.getBigDecimal("partnerInterestRate");
            final Long partnerId =JdbcSupport.getLong(rs,"partnerId");
            final Long loantypeCvId=JdbcSupport.getLong(rs,"loantypeCvId");
            final Long frameWorkId=JdbcSupport.getLong(rs,"frameWorkId");
            final Long insuranceApplicabilityId=JdbcSupport.getLong(rs,"insuranceApplicabilityId");
            final Long fldgLogicId=JdbcSupport.getLong(rs,"fldgLogicId");
           // final BigDecimal vcplHurdleRate =rs.getBigDecimal("vcplHurdleRate");

//            final Long disbursementId=JdbcSupport.getLong(rs,"disbursementId");
//            final Long collectionId=JdbcSupport.getLong(rs,"collectionId");
            final Long assetClassId=JdbcSupport.getLong(rs,"assetClassId");


            final Integer brokenStrategy=JdbcSupport.getInteger(rs,"brokenStrategy");
            final EnumOptionData brokenStrategyId = CommonEnumerations.brokenStrategy(brokenStrategy);


            final Integer collectionId=JdbcSupport.getInteger(rs,"collectionModeId");
            final EnumOptionData collectionvalue = CommonEnumerations.collectionMode(collectionId);


            final Integer disbursement=JdbcSupport.getInteger(rs,"disbursementId");
            final EnumOptionData disbursementvalue = CommonEnumerations.disbursementMode(disbursement);


            final Integer brokenInterestDaysInMonths=JdbcSupport.getInteger(rs,"brokenInterestDaysInMonth");
            final EnumOptionData brokenInterestDaysInMonthSelected=CommonEnumerations.brokenDaysInMonth(brokenInterestDaysInMonths);

            final Integer brokenInterestDaysInYear =JdbcSupport.getInteger(rs,"brokenInterestDaysInYears");
            final EnumOptionData brokenInterestDaysInYearsselected=CommonEnumerations.brokenDaysInYears(brokenInterestDaysInYear);

            final Integer transactionTypePreference =JdbcSupport.getInteger(rs,"transactionTypePreference");
            EnumOptionData transactionTypeValue = null;
            if(Objects.nonNull(transactionTypePreference)) {
                transactionTypeValue = CommonEnumerations.transactionTypePreference(transactionTypePreference);
            }


            final Long loanProductClassId=JdbcSupport.getLong(rs,"loanProductClassId");
            final Long loanProductTypeId=JdbcSupport.getLong(rs,"loanProductTypeId");

            final String loanTypeValue = rs.getString("loanTypeValue");
            final String frameWorkValue = rs.getString("frameWorkValue");
            final String insuranceApplicabilityValue = rs.getString("insuranceApplicabilityValue");
            final String fldgLogicValue = rs.getString("fldgLogicValue");

            final String assetClassValue = rs.getString("assetClassValue");
            final String loanProductClassValue = rs.getString("loanProductClassValue");
            final String loanProductTypeValue = rs.getString("loanProductTypeValue");

            final CodeValueData loanType = CodeValueData.instance(loantypeCvId, loanTypeValue);
            final CodeValueData frameWork = CodeValueData.instance(frameWorkId, frameWorkValue);
            final CodeValueData insuranceApplicability = CodeValueData.instance(insuranceApplicabilityId, insuranceApplicabilityValue);
            final CodeValueData fldgLogic = CodeValueData.instance(fldgLogicId, fldgLogicValue);

            final CodeValueData assetClass = CodeValueData.instance(assetClassId, assetClassValue);
            final CodeValueData loanProductClass = CodeValueData.instance(loanProductClassId, loanProductClassValue);
            final CodeValueData loanProductType = CodeValueData.instance(loanProductTypeId, loanProductTypeValue);
            final boolean enableFeesWiseBifacation =rs.getBoolean("enableFeesWiseBifacation");
            final boolean enableChargeWiseBifacation =rs.getBoolean("enableChargeWiseBifacation");
            final boolean enableOverdue = rs.getBoolean("enableOverdue");
            final String selectCharge =rs.getString("selectCharge");
            final Integer colendingCharge =null;
            final Integer selfCharge =null;
            final Integer partnerCharge =null;
            final boolean useDaysInMonthForLoanProvisioning=rs.getBoolean("useDaysInMonthForLoanProvisioning");
            final boolean divideByThirtyForPartialPeriod =rs.getBoolean("divideByThirtyForPartialPeriod");
            final BigDecimal aumSlabRate =rs.getBigDecimal("aumSlabRate");
            final BigDecimal gstLiabilityByVcpl =rs.getBigDecimal("gstLiabilityByVcpl");
            final BigDecimal gstLiabilityByPartner =rs.getBigDecimal("gstLiabilityByPartner");
            final BigDecimal vcplShareInBrokenInterest =rs.getBigDecimal("vcplShareInBrokenInterest");
            final BigDecimal partnerShareInBrokenInterest =rs.getBigDecimal("partnerShareInBrokenInterest");
            final Integer monitoringTriggerPar30 =rs.getInt("monitoringTriggerPar30");
            final Integer monitoringTriggerPar90 =rs.getInt("monitoringTriggerPar90");

            final Boolean selectAcceptedDates = rs.getBoolean("selectAcceptedDates");
            final String acceptedDateType=rs.getString("acceptedDateType");
            final Integer acceptedStartDate=rs.getInt("acceptedStartDate");
            final Integer acceptedEndDate=rs.getInt("acceptedEndDate");
            final Integer acceptedDate=rs.getInt("acceptedDate");

            final boolean applyPrepaidLockingPeriod=rs.getBoolean("applyPrepaidLockingPeriod");
            final Integer prepayLockingPeriod=rs.getInt("prepayLockingPeriod");

            final boolean applyForeclosureLockingPeriod=rs.getBoolean("applyForeclosureLockingPeriod");
            final Integer foreclosureLockingPeriod=rs.getInt("applyForeclosureLockingPeriod");

            final int repaymentFrequencyTypeId = JdbcSupport.getInteger(rs, "repaymentPeriodFrequency");
            final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(repaymentFrequencyTypeId);

            final int amortizationTypeId = JdbcSupport.getInteger(rs, "amortizationMethod");
            final EnumOptionData amortizationType = LoanEnumerations.amortizationType(amortizationTypeId);
            final boolean isEqualAmortization = rs.getBoolean("isEqualAmortization");

            final Integer interestRateFrequencyTypeId = JdbcSupport.getInteger(rs, "interestRatePerPeriodFreq");
            final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(interestRateFrequencyTypeId);

            final int interestTypeId = JdbcSupport.getInteger(rs, "interestMethod");
            final EnumOptionData interestType = LoanEnumerations.interestType(interestTypeId);

            final int interestCalculationPeriodTypeId = JdbcSupport.getInteger(rs, "interestCalculationInPeriodMethod");
            final Boolean allowPartialPeriodInterestCalcualtion = rs.getBoolean("allowPartialPeriodInterestCalcualtion");
            final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                    .interestCalculationPeriodType(interestCalculationPeriodTypeId);

            final boolean includeInBorrowerCycle = rs.getBoolean("includeInBorrowerCycle");
            final boolean useBorrowerCycle = rs.getBoolean("useBorrowerCycle");
            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
            final LocalDate closeDate = JdbcSupport.getLocalDate(rs, "closeDate");
            String status = "";
            if (closeDate != null && closeDate.isBefore(DateUtils.getLocalDateOfTenant())) {
                status = "loanProduct.inActive";
            } else {
                status = "loanProduct.active";
            }
            final String externalId = rs.getString("externalId");
            final Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle = new ArrayList<>();
            final Collection<LoanProductBorrowerCycleVariationData> interestRateVariationsForBorrowerCycle = new ArrayList<>();
            final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
            if (this.borrowerCycleVariationDatas != null) {
                for (final LoanProductBorrowerCycleVariationData borrowerCycleVariationData : this.borrowerCycleVariationDatas) {
                    final LoanProductParamType loanProductParamType = borrowerCycleVariationData.getParamType();
                    if (loanProductParamType.isParamTypePrincipal()) {
                        principalVariationsForBorrowerCycle.add(borrowerCycleVariationData);
                    } else if (loanProductParamType.isParamTypeInterestTate()) {
                        interestRateVariationsForBorrowerCycle.add(borrowerCycleVariationData);
                    } else if (loanProductParamType.isParamTypeRepayment()) {
                        numberOfRepaymentVariationsForBorrowerCycle.add(borrowerCycleVariationData);
                    }
                }
            }

            final Boolean multiDisburseLoan = rs.getBoolean("multiDisburseLoan");
            final Integer maxTrancheCount = rs.getInt("maxTrancheCount");
            final BigDecimal outstandingLoanBalance = rs.getBigDecimal("outstandingLoanBalance");
            final Boolean disallowExpectedDisbursements = rs.getBoolean("disallowExpectedDisbursements");
            final Boolean allowApprovedDisbursedAmountsOverApplied = rs.getBoolean("allowApprovedDisbursedAmountsOverApplied");
            final String overAppliedCalculationType = rs.getString("overAppliedCalculationType");
            final Integer overAppliedNumber = rs.getInt("overAppliedNumber");

            final int daysInMonth = JdbcSupport.getInteger(rs, "daysInMonth");
            final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(daysInMonth);
            final int daysInYear = JdbcSupport.getInteger(rs, "daysInYear");
            final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(daysInYear);
            final Integer installmentAmountInMultiplesOf = JdbcSupport.getInteger(rs, "installmentAmountInMultiplesOf");
            final boolean canDefineInstallmentAmount = rs.getBoolean("canDefineInstallmentAmount");
            final boolean isInterestRecalculationEnabled = rs.getBoolean("isInterestRecalculationEnabled");

            LoanProductInterestRecalculationData interestRecalculationData = null;
            if (isInterestRecalculationEnabled) {

                final Long lprId = JdbcSupport.getLong(rs, "lprId");
                final Long productId = JdbcSupport.getLong(rs, "productId");
                final int compoundTypeEnumValue = JdbcSupport.getInteger(rs, "compoundType");
                final EnumOptionData interestRecalculationCompoundingType = LoanEnumerations
                        .interestRecalculationCompoundingType(compoundTypeEnumValue);
                final int rescheduleStrategyEnumValue = JdbcSupport.getInteger(rs, "rescheduleStrategy");
                final EnumOptionData rescheduleStrategyType = LoanEnumerations.rescheduleStrategyType(rescheduleStrategyEnumValue);
                final int restFrequencyEnumValue = JdbcSupport.getInteger(rs, "restFrequencyEnum");
                final EnumOptionData restFrequencyType = LoanEnumerations.interestRecalculationFrequencyType(restFrequencyEnumValue);
                final int restFrequencyInterval = JdbcSupport.getInteger(rs, "restFrequencyInterval");
                final Integer restFrequencyNthDayEnumValue = JdbcSupport.getInteger(rs, "restFrequencyNthDayEnum");
                EnumOptionData restFrequencyNthDayEnum = null;
                if (restFrequencyNthDayEnumValue != null) {
                    restFrequencyNthDayEnum = LoanEnumerations.interestRecalculationCompoundingNthDayType(restFrequencyNthDayEnumValue);
                }
                final Integer restFrequencyWeekDayEnumValue = JdbcSupport.getInteger(rs, "restFrequencyWeekDayEnum");
                EnumOptionData restFrequencyWeekDayEnum = null;
                if (restFrequencyWeekDayEnumValue != null) {
                    restFrequencyWeekDayEnum = LoanEnumerations
                            .interestRecalculationCompoundingDayOfWeekType(restFrequencyWeekDayEnumValue);
                }
                final Integer restFrequencyOnDay = JdbcSupport.getInteger(rs, "restFrequencyOnDay");
                final Integer compoundingFrequencyEnumValue = JdbcSupport.getInteger(rs, "compoundingFrequencyTypeEnum");
                EnumOptionData compoundingFrequencyType = null;
                if (compoundingFrequencyEnumValue != null) {
                    compoundingFrequencyType = LoanEnumerations.interestRecalculationFrequencyType(compoundingFrequencyEnumValue);
                }
                final Integer compoundingInterval = JdbcSupport.getInteger(rs, "compoundingInterval");
                final Integer compoundingFrequencyNthDayEnumValue = JdbcSupport.getInteger(rs, "compoundingFrequencyNthDayEnum");
                EnumOptionData compoundingFrequencyNthDayEnum = null;
                if (compoundingFrequencyNthDayEnumValue != null) {
                    compoundingFrequencyNthDayEnum = LoanEnumerations
                            .interestRecalculationCompoundingNthDayType(compoundingFrequencyNthDayEnumValue);
                }
                final Integer compoundingFrequencyWeekDayEnumValue = JdbcSupport.getInteger(rs, "compoundingFrequencyWeekDayEnum");
                EnumOptionData compoundingFrequencyWeekDayEnum = null;
                if (compoundingFrequencyWeekDayEnumValue != null) {
                    compoundingFrequencyWeekDayEnum = LoanEnumerations
                            .interestRecalculationCompoundingDayOfWeekType(compoundingFrequencyWeekDayEnumValue);
                }
                final Integer compoundingFrequencyOnDay = JdbcSupport.getInteger(rs, "compoundingFrequencyOnDay");
                final boolean isArrearsBasedOnOriginalSchedule = rs.getBoolean("isArrearsBasedOnOriginalSchedule");
                final boolean isCompoundingToBePostedAsTransaction = rs.getBoolean("isCompoundingToBePostedAsTransaction");
                final int preCloseInterestCalculationStrategyEnumValue = JdbcSupport.getInteger(rs, "preCloseInterestCalculationStrategy");
                final EnumOptionData preCloseInterestCalculationStrategy = LoanEnumerations
                        .preCloseInterestCalculationStrategy(preCloseInterestCalculationStrategyEnumValue);
                final boolean allowCompoundingOnEod = rs.getBoolean("allowCompoundingOnEod");

                interestRecalculationData = new LoanProductInterestRecalculationData(lprId, productId, interestRecalculationCompoundingType,
                        rescheduleStrategyType, restFrequencyType, restFrequencyInterval, restFrequencyNthDayEnum, restFrequencyWeekDayEnum,
                        restFrequencyOnDay, compoundingFrequencyType, compoundingInterval, compoundingFrequencyNthDayEnum,
                        compoundingFrequencyWeekDayEnum, compoundingFrequencyOnDay, isArrearsBasedOnOriginalSchedule,
                        isCompoundingToBePostedAsTransaction, preCloseInterestCalculationStrategy, allowCompoundingOnEod);
            }

            final boolean amortization = rs.getBoolean("amortizationBoolean");
            final boolean interestMethod = rs.getBoolean("interestMethodConfigBoolean");
            final boolean transactionProcessingStrategy = rs.getBoolean("transactionProcessingStrategyBoolean");
            final boolean interestCalcPeriod = rs.getBoolean("interestCalcPeriodBoolean");
            final boolean repaymentStrategyForNpa = rs.getBoolean("repaymentStrategyForNpa");
            final boolean arrearsTolerance = rs.getBoolean("arrearsToleranceBoolean");
            final boolean repaymentFrequency = rs.getBoolean("repaymentFrequencyBoolean");
            final boolean graceOnPrincipalAndInterest = rs.getBoolean("graceOnPrincipalAndInterestBoolean");
            final boolean graceOnArrearsAging = rs.getBoolean("graceOnArrearsAgingBoolean");

            LoanProductConfigurableAttributes allowAttributeOverrides = null;

            allowAttributeOverrides = new LoanProductConfigurableAttributes(amortization, interestMethod, transactionProcessingStrategy,
                    interestCalcPeriod, repaymentStrategyForNpa,arrearsTolerance, repaymentFrequency, graceOnPrincipalAndInterest, graceOnArrearsAging);

            final boolean holdGuaranteeFunds = rs.getBoolean("holdGuaranteeFunds");
            LoanProductGuaranteeData loanProductGuaranteeData = null;
            if (holdGuaranteeFunds) {
                final Long lpgId = JdbcSupport.getLong(rs, "lpgId");
                final BigDecimal mandatoryGuarantee = rs.getBigDecimal("mandatoryGuarantee");
                final BigDecimal minimumGuaranteeFromOwnFunds = rs.getBigDecimal("minimumGuaranteeFromOwnFunds");
                final BigDecimal minimumGuaranteeFromGuarantor = rs.getBigDecimal("minimumGuaranteeFromGuarantor");
                loanProductGuaranteeData = LoanProductGuaranteeData.instance(lpgId, id, mandatoryGuarantee, minimumGuaranteeFromOwnFunds,
                        minimumGuaranteeFromGuarantor);
            }

            final BigDecimal principalThresholdForLastInstallment = rs.getBigDecimal("principalThresholdForLastInstallment");
            final BigDecimal fixedPrincipalPercentagePerInstallment = rs.getBigDecimal("fixedPrincipalPercentagePerInstallment");
            final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = rs.getBoolean("accountMovesOutOfNPAOnlyOnArrearsCompletion");
            final boolean syncExpectedWithDisbursementDate = rs.getBoolean("syncExpectedWithDisbursementDate");

            final boolean canUseForTopup = rs.getBoolean("canUseForTopup");
            final Collection<RateData> rateOptions = null;
            final boolean isRatesEnabled = false;
            final List<CodeValueData> frameWorkOptions=new ArrayList<>();
            final List<CodeValueData> insuranceApplicabilityOptions=new ArrayList<>();
            final List<CodeValueData> loanTypeOptions=new ArrayList<>();
            final List<CodeValueData> fldgLogicOptions=new ArrayList<>();


            final List<CodeValueData> disbursementOptions=new ArrayList<>();
            final List<CodeValueData> collectionOptions=new ArrayList<>();
            final List<CodeValueData> assetClassOptions=new ArrayList<>();
            final List<CodeValueData> loanProductClassOptions=new ArrayList<>();
            final List<CodeValueData> loanProductTypeOptions=new ArrayList<>();
            final Long disbursementAccountNumber =rs.getLong("disbursementAccountNumber");
            final Long collectionAccountNumber =rs.getLong("collectionAccountNumber");
            final String penalInvoiceValue =rs.getString("penalInvoiceValue");
            final String multiDisbursementValue = rs.getString("multiDisbursementValue");
            final String trancheClubbingValue = rs.getString("trancheClubbingValue");
            final String repaymentScheduleUpdateAllowedValue = rs.getString("repaymentScheduleUpdateAllowedValue");
            final Long penalInvoiceId = JdbcSupport.getLong(rs,"penalInvoiceId");
            final Long multipleDisbursementId = JdbcSupport.getLong(rs,"multipleDisbursementId");
            final Long trancheClubbingId = JdbcSupport.getLong(rs,"trancheClubbingId");
            final Long repaymentScheduleUpdateAllowedId = JdbcSupport.getLong(rs,"repaymentScheduleUpdateAllowedId");


            final CodeValueData penalInvoice = CodeValueData.instance(penalInvoiceId,penalInvoiceValue);
            final CodeValueData multiDisbursement = CodeValueData.instance(multipleDisbursementId,multiDisbursementValue);
            final CodeValueData trancheClubbing = CodeValueData.instance(trancheClubbingId,trancheClubbingValue);
            final CodeValueData repaymentScheduleUpdateAllowed = CodeValueData.instance(repaymentScheduleUpdateAllowedId,repaymentScheduleUpdateAllowedValue);
            final List<CodeValueData> penalInvoiceOptions = new ArrayList<>();
            final List<CodeValueData> multiDisbursementOptions = new ArrayList<>();
            final List<CodeValueData> trancheClubbingOptions = new ArrayList<>();
            final List<CodeValueData> repaymentScheduleUpdateAllowedOptions = new ArrayList<>();
            final String emiroundingModeSelected = rs.getString("emiroundingMode");
            final Boolean isPennyDropEnabled = rs.getBoolean("isPennyDropEnabled");
            final Boolean isBankDisbursementEnabled = rs.getBoolean("isBankDisbursementEnabled");
            final Boolean servicerFeeInterestConfigEnabled = rs.getBoolean("servicerFeeInterestConfigEnabled");
            final Boolean servicerFeeChargesConfigEnabled = rs.getBoolean("servicerFeeChargesConfigEnabled");
            final Boolean enableDedupe = rs.getBoolean("enableDedupe");
            final EnumOptionData selectedDedupe = LoanEnumerations.dedupeEnum(Dedupe.fromInt(rs.getInt("dedupeBy")));
            final List<EnumOptionData> dedupeEnumOptions = Dedupe.all();
            final Long disbursementBankAccountId = rs.getLong("disbursementBankAccountId");
            final String disbursementBankAccountValue = rs.getString("disbursementBankAccountValue");
            final List<CodeValueData> disbursementBankAccountNameOptions = new ArrayList<>();
            CodeValueData selectedDisbursementBankAccountName = null;
            if (disbursementBankAccountId != 0) {
                selectedDisbursementBankAccountName = CodeValueData.instance(disbursementBankAccountId, disbursementBankAccountValue);
            }

            final Integer emiDecimalRegexSelected= rs.getInt("emiDecimalRegexSelected");
            final Integer emiMultiplesOfSelected= rs.getInt("emiMultiplesOfSelected");


            final Integer interestDecimalRegexSelected = rs.getInt("interestDecimalRegexSelected");
            final String interestRoundingModeSelected = rs.getString("interestRoundingModeSelected");

            final Integer interestdecimalSelected = rs.getInt("interestDecimalSelected");

            Integer advanceAppropriation = rs.getInt("advanceAppropriationSelected");
            if(Objects.isNull(advanceAppropriation)){
                advanceAppropriation =0;
            }
            final EnumOptionData advanceAppropriationSelected = LoanEnumerations.advanceAppropriationEnum(AdvanceAppropriationOn.fromInt(advanceAppropriation));

            final Boolean interestBenefitEnabled = rs.getBoolean("interestBenefitEnabled");
            final Integer foreclosureOnDueDateInterest = rs.getInt("foreclosureOnDueDateInterest");
            final EnumOptionData foreclosureOnDueDateInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureOnDueDateInterest));
            final Integer foreclosureOnDueDateCharge = rs.getInt("foreclosureOnDueDateCharge");
            final EnumOptionData foreclosureOnDueDateChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureOnDueDateCharge));
            final Integer foreclosureOtherThanDueDateInterest = rs.getInt("foreclosureOtherThanDueDateInterest");
            final EnumOptionData foreclosureOtherThanDueDateInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureOtherThanDueDateInterest));
            final Integer foreclosureOtherThanDueDateCharge = rs.getInt("foreclosureOtherThanDueDateCharge");
            final EnumOptionData foreclosureOtherThanDueDateChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureOtherThanDueDateCharge));
            final Integer foreclosureOneMonthOverdueInterest = rs.getInt("foreclosureOneMonthOverdueInterest");
            final EnumOptionData foreclosureOneMonthOverdueInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureOneMonthOverdueInterest));
            final Integer foreclosureOneMonthOverdueCharge = rs.getInt("foreclosureOneMonthOverdueCharge");
            final EnumOptionData foreclosureOneMonthOverdueChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureOneMonthOverdueCharge));
            final Integer foreclosureShortPaidInterest = rs.getInt("foreclosureShortPaidInterest");
            final EnumOptionData foreclosureShortPaidInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureShortPaidInterest));
            final Integer foreclosureShortPaidInterestCharge = rs.getInt("foreclosureShortPaidInterestCharge");
            final EnumOptionData foreclosureShortPaidInterestChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureShortPaidInterestCharge));
            final Integer foreclosurePrincipalShortPaidInterest = rs.getInt("foreclosurePrincipalShortPaidInterest");
            final EnumOptionData foreclosurePrincipalShortPaidInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosurePrincipalShortPaidInterest));
            final Integer foreclosurePrincipalShortPaidCharge = rs.getInt("foreclosurePrincipalShortPaidCharge");
            final EnumOptionData foreclosurePrincipalShortPaidChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosurePrincipalShortPaidCharge));
            final Integer foreclosureTwoMonthsOverdueInterest = rs.getInt("foreclosureTwoMonthsOverdueInterest");
            final EnumOptionData foreclosureTwoMonthsOverdueInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureTwoMonthsOverdueInterest));
            final Integer foreclosureTwoMonthsOverdueCharge = rs.getInt("foreclosureTwoMonthsOverdueCharge");
            final EnumOptionData foreclosureTwoMonthsOverdueChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureTwoMonthsOverdueCharge));
            final Integer foreclosurePosAdvanceOnDueDate = rs.getInt("foreclosurePosAdvanceOnDueDate");
            final EnumOptionData foreclosurePosAdvanceOnDueDateSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosurePosAdvanceOnDueDate));
            final Integer foreclosureAdvanceOnDueDateInterest = rs.getInt("foreclosureAdvanceOnDueDateInterest");
            final EnumOptionData foreclosureAdvanceOnDueDateInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureAdvanceOnDueDateInterest));
            final Integer foreclosureAdvanceOnDueDateCharge = rs.getInt("foreclosureAdvanceOnDueDateCharge");
            final EnumOptionData foreclosureAdvanceOnDueDateChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureAdvanceOnDueDateCharge));
            final Integer foreclosurePosAdvanceOtherThanDueDate = rs.getInt("foreclosurePosAdvanceOtherThanDueDate");
            final EnumOptionData foreclosurePosAdvanceOtherThanDueDateSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosurePosAdvanceOtherThanDueDate));
            final Integer foreclosureAdvanceAfterDueDateInterest = rs.getInt("foreclosureAdvanceAfterDueDateInterest");
            final EnumOptionData foreclosureAdvanceAfterDueDateInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureAdvanceAfterDueDateInterest));
            final Integer foreclosureAdvanceAfterDueDateCharge = rs.getInt("foreclosureAdvanceAfterDueDateCharge");
            final EnumOptionData foreclosureAdvanceAfterDueDateChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureAdvanceAfterDueDateCharge));
            final Integer foreclosureBackdatedShortPaidInterest = rs.getInt("foreclosureBackdatedShortPaidInterest");
            final EnumOptionData foreclosureBackdatedShortPaidInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedShortPaidInterest));
            final Integer foreclosureBackdatedShortPaidInterestCharge = rs.getInt("foreclosureBackdatedShortPaidInterestCharge");
            final EnumOptionData foreclosureBackdatedShortPaidInterestChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedShortPaidInterestCharge));
            final Integer foreclosureBackdatedFullyPaidInterest = rs.getInt("foreclosureBackdatedFullyPaidInterest");
            final EnumOptionData foreclosureBackdatedFullyPaidInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedFullyPaidInterest));
            final Integer foreclosureBackdatedFullyPaidInterestCharge = rs.getInt("foreclosureBackdatedFullyPaidInterestCharge");
            final EnumOptionData foreclosureBackdatedFullyPaidInterestChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedFullyPaidInterestCharge));
            final Integer foreclosureBackdatedShortPaidPrincipalInterest = rs.getInt("foreclosureBackdatedShortPaidPrincipalInterest");
            final EnumOptionData foreclosureBackdatedShortPaidPrincipalInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedShortPaidPrincipalInterest));
            final Integer foreclosureBackdatedShortPaidPrincipalCharge = rs.getInt("foreclosureBackdatedShortPaidPrincipalCharge");
            final EnumOptionData foreclosureBackdatedShortPaidPrincipalChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedShortPaidPrincipalCharge));
            final Integer foreclosureBackdatedFullyPaidEmiInterest = rs.getInt("foreclosureBackdatedFullyPaidEmiInterest");
            final EnumOptionData foreclosureBackdatedFullyPaidEmiInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedFullyPaidEmiInterest));
            final Integer foreclosureBackdatedFullyPaidEmiCharge = rs.getInt("foreclosureBackdatedFullyPaidEmiCharge");
            final EnumOptionData foreclosureBackdatedFullyPaidEmiChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedFullyPaidEmiCharge));
            final Integer foreclosureBackdatedAdvanceInterest = rs.getInt("foreclosureBackdatedAdvanceInterest");
            final EnumOptionData foreclosureBackdatedAdvanceInterestSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedAdvanceInterest));
            final Integer foreclosureBackdatedAdvanceCharge = rs.getInt("foreclosureBackdatedAdvanceCharge");
            final EnumOptionData foreclosureBackdatedAdvanceChargeSelected =LoanEnumerations.foreclosurePosEnum(ForeclosurePos.fromInt(foreclosureBackdatedAdvanceCharge));
            final Boolean coolingOffApplicability = rs.getBoolean("coolingOffApplicability");
            final Integer coolingOffThresholdDays = rs.getInt("coolingOffThresholdDays");
            final Integer coolingOffInterestAndChargeApplicability = rs.getInt("coolingOffInterestAndChargeApplicability");
            final EnumOptionData coolingOffInterestAndChargeApplicabilitySelected =LoanEnumerations.coolingOffInterestAndChargeApplicability(CoolingOffInterestAndChargeApplicability.fromInt(coolingOffInterestAndChargeApplicability));
            final Integer coolingOffInterestLogicApplicability = rs.getInt("coolingOffInterestLogicApplicability");
            final EnumOptionData coolingOffInterestLogicApplicabilitySelected =LoanEnumerations.coolingOffInterestLogicApplicability(CoolingOffInterestLogicApplicability.fromInt(coolingOffInterestLogicApplicability));

            Integer coolingOffDaysInYear = rs.getInt("coolingOffDaysInYear");
            if (Objects.isNull(coolingOffDaysInYear)) {
                coolingOffDaysInYear = 0 ;
            }
            EnumOptionData coolingOffDaysInYearSelected =CommonEnumerations.daysInYearType(coolingOffDaysInYear);

            final String coolingOffRoundingModeSelected = rs.getString("coolingOffRoundingMode");
            final Integer coolingOffRoundingDecimals = rs.getInt("coolingOffRoundingDecimals");


            final Boolean advanceEntryEnabled =  rs.getBoolean("advanceEntryEnabled");

             Integer advanceAppropriationAgainstOn = rs.getInt("advanceAppropriationAgainstOnSelected");
            if(Objects.isNull(advanceAppropriationAgainstOn)){
                advanceAppropriationAgainstOn =0;
            }
            EnumOptionData advanceAppropriationAgainstOnSelected = LoanEnumerations.advanceAppropriationAgainstOn(AdvanceAppropriationAgainstOn.getInt(advanceAppropriationAgainstOn));


            Integer emiDaysInMonth = rs.getInt("emiDaysInMonthSelected");
            if (Objects.isNull(emiDaysInMonth)) {
                emiDaysInMonth = 0 ;
            }
              EnumOptionData emiDaysInMonthSelected =CommonEnumerations.daysInMonthType(emiDaysInMonth);

            Integer emiDaysInYear = rs.getInt("emiDaysInYearSelectd");
            if (Objects.isNull(emiDaysInYear)) {
                emiDaysInYear = 0 ;
            }
            final EnumOptionData emiDaysInYearSelected = CommonEnumerations.daysInYearType(emiDaysInYear);


            Integer emiCalc = rs.getInt("emiCalcSelected");
            if (Objects.isNull(emiCalc)) {
                emiCalc = 0 ;
            }
            final EnumOptionData emiCalcSelected = LoanEnumerations.pmtCalcEnum(PmtCalcEnum.getInt(emiCalc));

            final Boolean BackDatedDisbursement = rs.getBoolean("enableBackDatedDisbursement");
            final Integer foreclosureMethodType = rs.getInt("foreclosureMethodType");
            final EnumOptionData foreclosureMethodTypeEnum = LoanEnumerations.foreclosureMethodTypeEnums(ForeclosureMethodTypes.fromInt(foreclosureMethodType));
            

            return new LoanProductData(id, name, shortName,loanAccNoPreference, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                    numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                    minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType,
                    interestRateFrequencyType, amortizationType, interestType, interestCalculationPeriodType,
                    allowPartialPeriodInterestCalcualtion, fundId, fundName, transactionStrategyId, transactionStrategyName,
                    graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged,
                    this.charges, accountingRuleType, includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId,
                    principalVariationsForBorrowerCycle, interestRateVariationsForBorrowerCycle,
                    numberOfRepaymentVariationsForBorrowerCycle, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                    disallowExpectedDisbursements, allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType, overAppliedNumber,
                    graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                    interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, loanProductGuaranteeData,
                    principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                    installmentAmountInMultiplesOf, allowAttributeOverrides, isLinkedToFloatingInterestRates, floatingRateId,
                    floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                    maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableIntallmentsAllowed, minimumGap,
                    maximumGap, allowAgeLimits,minimumAge,maximumAge,syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, rateOptions, this.rates,
                    isRatesEnabled, fixedPrincipalPercentagePerInstallment,allowApprovalOverAmountApplied,overAmountDetails,
                    brokenInterestCalculationPeriod,repaymentStrategyForNpaId,loanForeclosureStrategy,brokenInterestDaysInYears,
                    enableColendingLoan,byPercentageSplit,selfPrincipalShare,selfFeeShare,selfPenaltyShare,
                    selfOverpaidShares,selfInterestRate, principalShare,feeShare,penaltyShare,overpaidShare,interestRate,partnerId,
                    enableFeesWiseBifacation, enableChargeWiseBifacation,enableOverdue,selectCharge,
                    colendingCharge,selfCharge,partnerCharge, selectAcceptedDates,acceptedDateType,acceptedStartDate,acceptedEndDate,acceptedDate,applyPrepaidLockingPeriod,prepayLockingPeriod,applyForeclosureLockingPeriod,foreclosureLockingPeriod,partnerPrincipalShare,partnerFeeShare,partnerPenaltyShare,partnerOverpaidShare,partnerInterestRate,
                    useDaysInMonthForLoanProvisioning,divideByThirtyForPartialPeriod,aumSlabRate,gstLiabilityByVcpl,gstLiabilityByPartner,vcplShareInBrokenInterest,
                    partnerShareInBrokenInterest,monitoringTriggerPar30,monitoringTriggerPar90,frameWorkOptions,insuranceApplicabilityOptions,loanTypeOptions,fldgLogicOptions,
                    disbursementOptions,collectionOptions,assetClassOptions,loanProductClassOptions,loanProductTypeOptions,loanType,frameWork,
                    insuranceApplicability,fldgLogic,assetClass,loanProductClass,loanProductType,brokenStrategyId,disbursementvalue,collectionvalue,this.colendingfees,this.colendingCharge,this.overDueCharges,
                    brokenInterestDaysInMonthSelected,brokenInterestDaysInYearsselected,disbursementAccountNumber,collectionAccountNumber,penalInvoiceOptions,multiDisbursementOptions,
                    trancheClubbingOptions,repaymentScheduleUpdateAllowedOptions,penalInvoice,multiDisbursement,trancheClubbing,repaymentScheduleUpdateAllowed,transactionTypeValue,emiroundingModeSelected,isPennyDropEnabled,
                    isBankDisbursementEnabled,servicerFeeInterestConfigEnabled,servicerFeeChargesConfigEnabled,enableDedupe,dedupeEnumOptions,selectedDedupe,disbursementBankAccountNameOptions,selectedDisbursementBankAccountName,
                    emiDecimalRegexSelected,interestdecimalSelected,interestRoundingModeSelected,interestDecimalRegexSelected,currencyDigits,
                    emiMultiplesOfSelected,advanceAppropriationSelected,advanceEntryEnabled,interestBenefitEnabled,foreclosureOnDueDateInterestSelected,foreclosureOnDueDateChargeSelected,foreclosureOtherThanDueDateInterestSelected,foreclosureOtherThanDueDateChargeSelected,foreclosureOneMonthOverdueInterestSelected,foreclosureOneMonthOverdueChargeSelected,foreclosureShortPaidInterestSelected
                    ,foreclosureShortPaidInterestChargeSelected,foreclosurePrincipalShortPaidInterestSelected,foreclosurePrincipalShortPaidChargeSelected,foreclosureTwoMonthsOverdueInterestSelected,foreclosureTwoMonthsOverdueChargeSelected,foreclosurePosAdvanceOnDueDateSelected,foreclosureAdvanceOnDueDateInterestSelected
                    ,foreclosureAdvanceOnDueDateChargeSelected,foreclosurePosAdvanceOtherThanDueDateSelected,foreclosureAdvanceAfterDueDateInterestSelected,foreclosureAdvanceAfterDueDateChargeSelected,foreclosureBackdatedShortPaidInterestSelected,foreclosureBackdatedShortPaidInterestChargeSelected,foreclosureBackdatedFullyPaidInterestSelected
                    ,foreclosureBackdatedFullyPaidInterestChargeSelected,foreclosureBackdatedShortPaidPrincipalInterestSelected,foreclosureBackdatedShortPaidPrincipalChargeSelected,foreclosureBackdatedFullyPaidEmiInterestSelected,foreclosureBackdatedFullyPaidEmiChargeSelected
                    ,foreclosureBackdatedAdvanceInterestSelected,foreclosureBackdatedAdvanceChargeSelected,advanceAppropriationAgainstOnSelected,emiDaysInMonthSelected,emiDaysInYearSelected,emiCalcSelected,BackDatedDisbursement,foreclosureMethodTypeEnum,
					coolingOffApplicability,coolingOffThresholdDays,coolingOffInterestAndChargeApplicabilitySelected,coolingOffInterestLogicApplicabilitySelected,coolingOffDaysInYearSelected,coolingOffRoundingModeSelected,coolingOffRoundingDecimals);
        }
    }

    private static final class LoanProductLookupMapper implements RowMapper<LoanProductData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        LoanProductLookupMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String schema() {
            return "lp.id as id, lp.name as name, lp.allow_multiple_disbursals as multiDisburseLoan from m_product_loan lp";
        }

        public String activeOnlySchema() {
            return schema() + " where (close_date is null or close_date >= " + sqlGenerator.currentDate() + ")";
        }

        public String productMixSchema() {
            return "lp.id as id, lp.name as name, lp.allow_multiple_disbursals as multiDisburseLoan FROM m_product_loan lp left join m_product_mix pm on pm.product_id=lp.id where lp.id not IN("
                    + "select lp.id from m_product_loan lp inner join m_product_mix pm on pm.product_id=lp.id)";
        }

        public String restrictedProductsSchema() {
            return "pm.restricted_product_id as id, rp.name as name, rp.allow_multiple_disbursals as multiDisburseLoan from m_product_mix pm join m_product_loan rp on rp.id = pm.restricted_product_id ";
        }

        public String derivedRestrictedProductsSchema() {
            return "pm.product_id as id, lp.name as name, lp.allow_multiple_disbursals as multiDisburseLoan from m_product_mix pm join m_product_loan lp on lp.id=pm.product_id";
        }

        @Override
        public LoanProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final Boolean multiDisburseLoan = rs.getBoolean("multiDisburseLoan");

            return LoanProductData.lookup(id, name, multiDisburseLoan);
        }
    }

    private static final class LoanProductBorrowerCycleMapper implements RowMapper<LoanProductBorrowerCycleVariationData> {

        public String schema() {
            return "bc.id as id,bc.borrower_cycle_number as cycleNumber,bc.value_condition as conditionType,bc.param_type as paramType,"
                    + "bc.default_value as defaultValue,bc.max_value as maxVal,bc.min_value as minVal "
                    + "from m_product_loan_variations_borrower_cycle bc";
        }

        @Override
        public LoanProductBorrowerCycleVariationData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long id = rs.getLong("id");
            final Integer cycleNumber = JdbcSupport.getInteger(rs, "cycleNumber");
            final Integer conditionType = JdbcSupport.getInteger(rs, "conditionType");
            final EnumOptionData conditionTypeData = LoanEnumerations.loanCycleValueConditionType(conditionType);
            final Integer paramType = JdbcSupport.getInteger(rs, "paramType");
            final EnumOptionData paramTypeData = LoanEnumerations.loanCycleParamType(paramType);
            final BigDecimal defaultValue = rs.getBigDecimal("defaultValue");
            final BigDecimal maxValue = rs.getBigDecimal("maxVal");
            final BigDecimal minValue = rs.getBigDecimal("minVal");

            final LoanProductBorrowerCycleVariationData borrowerCycleVariationData = new LoanProductBorrowerCycleVariationData(id,
                    cycleNumber, paramTypeData, conditionTypeData, defaultValue, minValue, maxValue);
            return borrowerCycleVariationData;
        }

    }

    @Override
    public Collection<LoanProductData> retrieveAllLoanProductsForCurrency(String currencyCode) {
        this.context.authenticatedUser();

        final LoanProductMapper rm = new LoanProductMapper(null, null, null,null,null, null);

        String sql = "select " + rm.loanProductSchema() + " where lp.currency_code= ? ";

        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.LOAN_PRODUCT);
        if (inClause != null && !inClause.trim().isEmpty()) {
            sql += " and id in (" + inClause + ") ";
        }

        return this.jdbcTemplate.query(sql, rm, new Object[] { currencyCode }); // NOSONAR
    }

    @Override
    public Collection<LoanProductData> retrieveAvailableLoanProductsForMix() {

        this.context.authenticatedUser();

        final LoanProductLookupMapper rm = new LoanProductLookupMapper(sqlGenerator);

        String sql = "Select " + rm.productMixSchema();

        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.LOAN_PRODUCT);
        if (inClause != null && !inClause.trim().isEmpty()) {
            sql += " and lp.id in ( " + inClause + " ) ";
        }

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public Collection<LoanProductData> retrieveRestrictedProductsForMix(final Long productId) {

        this.context.authenticatedUser();

        final LoanProductLookupMapper rm = new LoanProductLookupMapper(sqlGenerator);

        String sql = "Select " + rm.restrictedProductsSchema() + " where pm.product_id=? ";
        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause1 = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.LOAN_PRODUCT);
        if (inClause1 != null && !inClause1.trim().isEmpty()) {
            sql += " and rp.id in ( " + inClause1 + " ) ";
        }

        sql += " UNION Select " + rm.derivedRestrictedProductsSchema() + " where pm.restricted_product_id=?";

        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause2 = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.LOAN_PRODUCT);
        if (inClause2 != null && !inClause2.trim().isEmpty()) {
            sql += " and lp.id in ( " + inClause2 + " ) ";
        }

        return this.jdbcTemplate.query(sql, rm, new Object[] { productId, productId }); // NOSONAR
    }

    @Override
    public Collection<LoanProductData> retrieveAllowedProductsForMix(final Long productId) {

        this.context.authenticatedUser();

        final LoanProductLookupMapper rm = new LoanProductLookupMapper(sqlGenerator);

        String sql = "Select " + rm.schema() + " where ";

        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.LOAN_PRODUCT);
        if (inClause != null && !inClause.trim().isEmpty()) {
            sql += " lp.id in ( " + inClause + " ) and ";
        }

        sql += "lp.id not in (" + "Select pm.restricted_product_id from m_product_mix pm where pm.product_id=? " + "UNION "
                + "Select pm.product_id from m_product_mix pm where pm.restricted_product_id=?)";

        return this.jdbcTemplate.query(sql, rm, new Object[] { productId, productId }); // NOSONAR
    }

    @Override
    public LoanProductData retrieveLoanProductFloatingDetails(final Long loanProductId) {

        try {
            final LoanProductFloatingRateMapper rm = new LoanProductFloatingRateMapper();
            final String sql = "select " + rm.schema() + " where lp.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanProductId }); // NOSONAR

        } catch (final EmptyResultDataAccessException e) {
            throw new LoanProductNotFoundException(loanProductId, e);
        }
    }

    private static final class LoanProductFloatingRateMapper implements RowMapper<LoanProductData> {

        LoanProductFloatingRateMapper() {}

        public String schema() {
            return "lp.id as id,  lp.name as name," + "lp.is_linked_to_floating_interest_rates as isLinkedToFloatingInterestRates, "
                    + "lfr.floating_rates_id as floatingRateId, " + "fr.name as floatingRateName, "
                    + "lfr.interest_rate_differential as interestRateDifferential, "
                    + "lfr.min_differential_lending_rate as minDifferentialLendingRate, "
                    + "lfr.default_differential_lending_rate as defaultDifferentialLendingRate, "
                    + "lfr.max_differential_lending_rate as maxDifferentialLendingRate, "
                    + "lfr.is_floating_interest_rate_calculation_allowed as isFloatingInterestRateCalculationAllowed "
                    + " from m_product_loan lp " + " left join m_product_loan_floating_rates as lfr on lfr.loan_product_id = lp.id "
                    + " left join m_floating_rates as fr on lfr.floating_rates_id = fr.id ";
        }

        @Override
        public LoanProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");

            final boolean isLinkedToFloatingInterestRates = rs.getBoolean("isLinkedToFloatingInterestRates");
            final Integer floatingRateId = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "floatingRateId");
            final String floatingRateName = rs.getString("floatingRateName");
            final BigDecimal interestRateDifferential = rs.getBigDecimal("interestRateDifferential");
            final BigDecimal minDifferentialLendingRate = rs.getBigDecimal("minDifferentialLendingRate");
            final BigDecimal defaultDifferentialLendingRate = rs.getBigDecimal("defaultDifferentialLendingRate");
            final BigDecimal maxDifferentialLendingRate = rs.getBigDecimal("maxDifferentialLendingRate");
            final boolean isFloatingInterestRateCalculationAllowed = rs.getBoolean("isFloatingInterestRateCalculationAllowed");

            return LoanProductData.loanProductWithFloatingRates(id, name, isLinkedToFloatingInterestRates, floatingRateId, floatingRateName,
                    interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate, maxDifferentialLendingRate,
                    isFloatingInterestRateCalculationAllowed);
        }
    }

}
