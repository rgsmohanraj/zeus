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

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.portfolio.common.domain.DayOfWeekType;
import org.vcpl.lms.portfolio.common.domain.NthDayType;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.BulkReportDataEnum;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.PmtCalcEnum;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.Enum.ServicerFeeChargesRatio;
import org.vcpl.lms.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.vcpl.lms.portfolio.loanproduct.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations.*;
import static org.vcpl.lms.portfolio.loanproduct.service.LoanEnumerations.brokenStrategyDaysInMonth;

@Service
public class LoanDropdownReadPlatformServiceImpl implements LoanDropdownReadPlatformService {

    private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;

    @Autowired
    public LoanDropdownReadPlatformServiceImpl(
            final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository) {
        this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
    }

    @Override
    public List<EnumOptionData> retrieveLoanAmortizationTypeOptions() {

        final List<EnumOptionData> allowedAmortizationMethods = Arrays.asList(amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS),
                amortizationType(AmortizationMethod.EQUAL_PRINCIPAL));

        return allowedAmortizationMethods;
    }

    @Override
    public List<EnumOptionData> retrieveLoanInterestTypeOptions() {
        final List<EnumOptionData> allowedRepaymentScheduleCalculationMethods = Arrays.asList(interestType(InterestMethod.FLAT),
                interestType(InterestMethod.DECLINING_BALANCE));

        return allowedRepaymentScheduleCalculationMethods;
    }

    @Override
    public List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions() {

        final List<EnumOptionData> allowedOptions = Arrays.asList(interestCalculationPeriodType(InterestCalculationPeriodMethod.DAILY),
                interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD));

        return allowedOptions;
    }

    @Override
    public List<EnumOptionData> retrieveLoanTermFrequencyTypeOptions() {
        final List<EnumOptionData> loanTermFrequencyOptions = Arrays.asList(loanTermFrequencyType(PeriodFrequencyType.DAYS),
                loanTermFrequencyType(PeriodFrequencyType.WEEKS), loanTermFrequencyType(PeriodFrequencyType.MONTHS),
                loanTermFrequencyType(PeriodFrequencyType.YEARS));
        return loanTermFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentFrequencyTypeOptions() {

        final List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(repaymentFrequencyType(PeriodFrequencyType.DAYS),
                repaymentFrequencyType(PeriodFrequencyType.WEEKS), repaymentFrequencyType(PeriodFrequencyType.MONTHS));
        return repaymentFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentFrequencyOptionsForNthDayOfMonth() {
        final List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(repaymentFrequencyNthDayType(NthDayType.ONE),
                repaymentFrequencyNthDayType(NthDayType.TWO), repaymentFrequencyNthDayType(NthDayType.THREE),
                repaymentFrequencyNthDayType(NthDayType.FOUR), repaymentFrequencyNthDayType(NthDayType.FIVE),repaymentFrequencyNthDayType(NthDayType.SIX), repaymentFrequencyNthDayType(NthDayType.SEVEN),
                repaymentFrequencyNthDayType(NthDayType.EIGHT),repaymentFrequencyNthDayType(NthDayType.NINE), repaymentFrequencyNthDayType(NthDayType.TEN), repaymentFrequencyNthDayType(NthDayType.ELEVEN),
                repaymentFrequencyNthDayType(NthDayType.TWELVE),repaymentFrequencyNthDayType(NthDayType.THIRTEEN),repaymentFrequencyNthDayType(NthDayType.FOURTEEN), repaymentFrequencyNthDayType(NthDayType.FIFTEEN),
                repaymentFrequencyNthDayType(NthDayType.SIXTEEN),repaymentFrequencyNthDayType(NthDayType.SEVENTEEN),repaymentFrequencyNthDayType(NthDayType.EIGHTEEN), repaymentFrequencyNthDayType(NthDayType.NINETEEN),
                repaymentFrequencyNthDayType(NthDayType.TWENTY),repaymentFrequencyNthDayType(NthDayType.TWENTYONE),repaymentFrequencyNthDayType(NthDayType.TWENTYTWO), repaymentFrequencyNthDayType(NthDayType.TWENTYTHREE),
                repaymentFrequencyNthDayType(NthDayType.TWENTYFOUR),repaymentFrequencyNthDayType(NthDayType.TWENTYFIVE), repaymentFrequencyNthDayType(NthDayType.TWENTYSIX), repaymentFrequencyNthDayType(NthDayType.TWENTYSEVEN),
                repaymentFrequencyNthDayType(NthDayType.TWENTYEIGHT),repaymentFrequencyNthDayType(NthDayType.TWENTYNINE),repaymentFrequencyNthDayType(NthDayType.THIRTY), repaymentFrequencyNthDayType(NthDayType.THIRTYONE));
        return repaymentFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentFrequencyOptionsForDaysOfWeek() {

        final List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(repaymentFrequencyDayOfWeekType(DayOfWeekType.SUNDAY),
                repaymentFrequencyDayOfWeekType(DayOfWeekType.MONDAY), repaymentFrequencyDayOfWeekType(DayOfWeekType.TUESDAY),
                repaymentFrequencyDayOfWeekType(DayOfWeekType.WEDNESDAY), repaymentFrequencyDayOfWeekType(DayOfWeekType.THURSDAY),
                repaymentFrequencyDayOfWeekType(DayOfWeekType.FRIDAY), repaymentFrequencyDayOfWeekType(DayOfWeekType.SATURDAY));
        return repaymentFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions() {
        // support for monthly and annual percentage rate (MPR) and (APR)
        final List<EnumOptionData> interestRateFrequencyTypeOptions = Arrays.asList(interestRateFrequencyType(PeriodFrequencyType.MONTHS),
                interestRateFrequencyType(PeriodFrequencyType.YEARS), interestRateFrequencyType(PeriodFrequencyType.WHOLE_TERM));
        return interestRateFrequencyTypeOptions;
    }

    @Override
    public Collection<TransactionProcessingStrategyData> retreiveTransactionProcessingStrategies() {

        final Collection<TransactionProcessingStrategyData> strategyOptions = new ArrayList<>();

        final List<LoanTransactionProcessingStrategy> strategies = this.loanTransactionProcessingStrategyRepository.findAll();

        strategies.stream().filter(transactionProcessingStrategyData ->
                transactionProcessingStrategyData.getId() == 5
                        || transactionProcessingStrategyData.getId() ==6
                        || transactionProcessingStrategyData.getId() ==8
                        || transactionProcessingStrategyData.getId() ==9 ).forEach(loanTransactionProcessingStrategy -> {
            strategyOptions.add(loanTransactionProcessingStrategy.toData());});


       /* Sort sort = Sort.by("sortOrder");
        final List<LoanTransactionProcessingStrategy> strategies = this.loanTransactionProcessingStrategyRepository.findAll(sort);
        for (final LoanTransactionProcessingStrategy strategy : strategies) {
            strategyOptions.add(strategy.toData());
        }*/

        return strategyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveLoanCycleValueConditionTypeOptions() {

        final List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(
                loanCycleValueConditionType(LoanProductValueConditionType.EQUAL),
                loanCycleValueConditionType(LoanProductValueConditionType.GREATERTHAN));
        return repaymentFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationCompoundingTypeOptions() {

        final List<EnumOptionData> interestRecalculationCompoundingTypeOptions = Arrays.asList(
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.NONE),
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.FEE),
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.INTEREST),
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE));
        return interestRecalculationCompoundingTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationNthDayTypeOptions() {
        final List<EnumOptionData> interestRecalculationCompoundingNthDayTypeOptions = Arrays.asList(
                interestRecalculationCompoundingNthDayType(NthDayType.ONE), interestRecalculationCompoundingNthDayType(NthDayType.TWO),
                interestRecalculationCompoundingNthDayType(NthDayType.THREE), interestRecalculationCompoundingNthDayType(NthDayType.FOUR),
                interestRecalculationCompoundingNthDayType(NthDayType.LAST));
        return interestRecalculationCompoundingNthDayTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationDayOfWeekTypeOptions() {
        final List<EnumOptionData> interestRecalculationCompoundingNthDayTypeOptions = Arrays.asList(
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.SUNDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.MONDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.TUESDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.WEDNESDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.THURSDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.FRIDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.SATURDAY));
        return interestRecalculationCompoundingNthDayTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveRescheduleStrategyTypeOptions() {

        final List<EnumOptionData> rescheduleStrategyTypeOptions = Arrays.asList(
                rescheduleStrategyType(LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT),
                rescheduleStrategyType(LoanRescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS),
                rescheduleStrategyType(LoanRescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS));
        return rescheduleStrategyTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationFrequencyTypeOptions() {

        final List<EnumOptionData> interestRecalculationFrequencyTypeOptions = Arrays.asList(
                interestRecalculationFrequencyType(RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD),
                interestRecalculationFrequencyType(RecalculationFrequencyType.DAILY),
                interestRecalculationFrequencyType(RecalculationFrequencyType.WEEKLY),
                interestRecalculationFrequencyType(RecalculationFrequencyType.MONTHLY));
        return interestRecalculationFrequencyTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrivePreCloseInterestCalculationStrategyOptions() {

        final List<EnumOptionData> preCloseInterestCalculationStrategyOptions = Arrays.asList(
                preCloseInterestCalculationStrategy(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE),
                preCloseInterestCalculationStrategy(LoanPreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE));
        return preCloseInterestCalculationStrategyOptions;
    }

    @Override
    public List<EnumOptionData> retriveBrokenStrategy() {

        final List<EnumOptionData> brokenStrategy=Arrays.asList(brokenStrategy(BrokenStrategy.NOBROKEN),brokenStrategy(BrokenStrategy.DISBURSEMENT),
        brokenStrategy(BrokenStrategy.FIRSTREPAYMENT),brokenStrategy(BrokenStrategy.LASTREPAYMENT));


        return brokenStrategy;
    }


    @Override
    public List<EnumOptionData> retriveDisbursementMode() {

        final List<EnumOptionData> disbursementOptions =Arrays.asList(disbursementMode(DisbursementMode.DIRECT),
                disbursementMode(DisbursementMode.ESCROW),disbursementMode(DisbursementMode.REIMBURSEMENT));
        return disbursementOptions;
    }

    @Override
    public List<EnumOptionData> retriveCollection() {

        final List<EnumOptionData> collectionOptions =Arrays.asList(collection(CollectionMode.PARTNER),
                collection(CollectionMode.DIRECT),collection(CollectionMode.ESCROW),collection(CollectionMode.RAZORPAY));
        return collectionOptions;

    }

    @Override
    public List<EnumOptionData> retriveBrokenStrategyDaysInYear() {

        final List<EnumOptionData> BrokenStrategyDaysInYearOption=Arrays.asList((brokenStrategyDaysInYear(BrokenStrategyDayInYear.NOBROKENDAYS)),brokenStrategyDaysInYear(BrokenStrategyDayInYear.ACTUAL),brokenStrategyDaysInYear(BrokenStrategyDayInYear.DAYS_360),
                brokenStrategyDaysInYear(BrokenStrategyDayInYear.DAYS_365));
        return BrokenStrategyDaysInYearOption;



    }

    @Override
    public List<EnumOptionData> retrieveBrokenStrategyDaysInMonth() {
        final List<EnumOptionData> BrokenStrategyDaysInMonthOption=Arrays.asList(brokenStrategyDaysInMonth(BrokenStrategyDaysInMonth.ACTUAL),
                brokenStrategyDaysInMonth(BrokenStrategyDaysInMonth.DAYS_30),brokenStrategyDaysInMonth(BrokenStrategyDaysInMonth.DAYS_31));

        return BrokenStrategyDaysInMonthOption;
    }

    @Override
    public List<EnumOptionData> retrieveTransactionTypePreference() {
        final List<EnumOptionData> TransactionTypePreference=Arrays.asList(transactionTypePreference(org.vcpl.lms.portfolio.loanproduct.domain.TransactionTypePreference.IMPS),
                transactionTypePreference(org.vcpl.lms.portfolio.loanproduct.domain.TransactionTypePreference.RTGS),transactionTypePreference(org.vcpl.lms.portfolio.loanproduct.domain.TransactionTypePreference.NEFT));

        return TransactionTypePreference;
    }

    @Override
    public List<RoundingMode> retrieveRoundingMode() {

        //while adding or updating the Rounding modes, also change in the method isRoundingModesExist() of DataValidatorBuilder class
        final List<RoundingMode> roundingModes=Arrays.asList(RoundingMode.UP,RoundingMode.HALF_UP,RoundingMode.DOWN);

        return roundingModes;

    }

    @Override
    public List<EnumOptionData> bulkReportDataEnums() {

        final List<EnumOptionData> bulkReportEnumData =Arrays.asList(bulkReportEnum(BulkReportDataEnum.BULKCIENTLOANSCREATION),
                bulkReportEnum(BulkReportDataEnum.BULKLOANSREPAYMENT),bulkReportEnum(BulkReportDataEnum.BULKCHARGEREPAYMENT));

        return bulkReportEnumData;
    }

    @Override
    public List<EnumOptionData> retrieveMultiples(){
        return Arrays.asList(multiplesOf(MultiplesOf.ONE),
                multiplesOf(MultiplesOf.TEN),multiplesOf(MultiplesOf.HUNDRED));
    }

    @Override
    public List<EnumOptionData> retrieveAdvanceAppropriation() {
        return Arrays.asList(advanceAppropriationEnum(AdvanceAppropriationOn.RECEIPT_DATE),
                advanceAppropriationEnum(AdvanceAppropriationOn.ON_DUE_DATE));
    }
    @Override
    public List<EnumOptionData> retrieveForeclosurePosCalculation() {
        return Arrays.asList(foreclosurePosEnum(ForeclosurePos.RS_POS),
                foreclosurePosEnum(ForeclosurePos.REVISED_POS));
    }
    @Override
    public List<EnumOptionData> retrieveServicerFeeChargesRatio() {

        return Arrays.asList(retrieveServiceFeeSplitMethod(ServicerFeeChargesRatio.FIXED_SPLIT),
                retrieveServiceFeeSplitMethod(ServicerFeeChargesRatio.DYNAMIC_SPLIT));
    }
	
	@Override
    public List<EnumOptionData> retrieveAdvanceAppropriationAgainstOn() {

        return Arrays.asList(advanceAppropriationAgainstOn(AdvanceAppropriationAgainstOn.PRINCIPAL),
                advanceAppropriationAgainstOn(AdvanceAppropriationAgainstOn.INTEREST_PRINCIPAL));
    }
    @Override
    public List<EnumOptionData> retrieveEmiCalcusEnum() {
        return Arrays.asList(pmtCalcEnum(PmtCalcEnum.PMT_WITH_MONTHLY_INTEREST_RATE),
                pmtCalcEnum(PmtCalcEnum.PMT_WITH_YEARLY_INTEREST_RATE));
    }
    @Override
    public List<EnumOptionData> retrieveForeclosureMethodTypes() {
        return Arrays.asList(foreclosureMethodTypeEnums(ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING),
                foreclosureMethodTypeEnums(ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_DUE),
                foreclosureMethodTypeEnums(ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED));
    }
	@Override
  public List<EnumOptionData> retrieveCoolingOffInterestAndChargeApplicability(){
         return Arrays.asList(coolingOffInterestAndChargeApplicability(CoolingOffInterestAndChargeApplicability.NO_INTEREST),
		 coolingOffInterestAndChargeApplicability(CoolingOffInterestAndChargeApplicability.ONLY_INTEREST),
		 coolingOffInterestAndChargeApplicability(CoolingOffInterestAndChargeApplicability.INTEREST_AND_CHARGES),
		 coolingOffInterestAndChargeApplicability(CoolingOffInterestAndChargeApplicability.ONLY_CHARGES));
		  }

@Override
public List<EnumOptionData> retrieveCoolingOffInterestLogicApplicability(){
return Arrays.asList(coolingOffInterestLogicApplicability(CoolingOffInterestLogicApplicability.PNR),
coolingOffInterestLogicApplicability(CoolingOffInterestLogicApplicability.MAX));
}

}
