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
import java.util.Collection;
import java.util.List;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.BulkReportDataEnum;
import org.vcpl.lms.portfolio.loanproduct.data.TransactionProcessingStrategyData;

public interface LoanDropdownReadPlatformService {

    List<EnumOptionData> retrieveLoanAmortizationTypeOptions();

    List<EnumOptionData> retrieveLoanInterestTypeOptions();

    List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions();

    List<EnumOptionData> retrieveLoanTermFrequencyTypeOptions();

    List<EnumOptionData> retrieveRepaymentFrequencyTypeOptions();

    List<EnumOptionData> retrieveRepaymentFrequencyOptionsForNthDayOfMonth();

    List<EnumOptionData> retrieveRepaymentFrequencyOptionsForDaysOfWeek();

    List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions();

    Collection<TransactionProcessingStrategyData> retreiveTransactionProcessingStrategies();

    List<EnumOptionData> retrieveLoanCycleValueConditionTypeOptions();

    List<EnumOptionData> retrieveInterestRecalculationCompoundingTypeOptions();

    List<EnumOptionData> retrieveInterestRecalculationNthDayTypeOptions();

    List<EnumOptionData> retrieveInterestRecalculationDayOfWeekTypeOptions();

    List<EnumOptionData> retrieveRescheduleStrategyTypeOptions();

    List<EnumOptionData> retrieveInterestRecalculationFrequencyTypeOptions();

    List<EnumOptionData> retrivePreCloseInterestCalculationStrategyOptions();

    List<EnumOptionData> retriveBrokenStrategy();

    List<EnumOptionData> retriveDisbursementMode();

    List<EnumOptionData> retriveCollection();

    List<EnumOptionData> retriveBrokenStrategyDaysInYear();

    List<EnumOptionData> retrieveBrokenStrategyDaysInMonth();

    List<EnumOptionData> retrieveTransactionTypePreference();

    List<RoundingMode> retrieveRoundingMode();

//    List<EnumOptionData> retriveGst();

    List<EnumOptionData> bulkReportDataEnums();

    List<EnumOptionData> retrieveMultiples();

    List<EnumOptionData> retrieveAdvanceAppropriation();
    List<EnumOptionData> retrieveForeclosurePosCalculation();
    List<EnumOptionData> retrieveServicerFeeChargesRatio();
	 List<EnumOptionData> retrieveAdvanceAppropriationAgainstOn();

    List<EnumOptionData> retrieveEmiCalcusEnum();
    List<EnumOptionData> retrieveForeclosureMethodTypes();
	List<EnumOptionData> retrieveCoolingOffInterestAndChargeApplicability();
    List<EnumOptionData> retrieveCoolingOffInterestLogicApplicability();
}
