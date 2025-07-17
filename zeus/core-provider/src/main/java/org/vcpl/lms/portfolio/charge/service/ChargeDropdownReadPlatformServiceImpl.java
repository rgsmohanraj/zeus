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
package org.vcpl.lms.portfolio.charge.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.portfolio.charge.domain.*;
import org.springframework.stereotype.Service;
import org.vcpl.lms.portfolio.common.domain.DaysInYearType;

import static org.vcpl.lms.portfolio.charge.service.ChargeEnumerations.*;

@Service
public class ChargeDropdownReadPlatformServiceImpl implements ChargeDropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrieveCalculationTypes() {

        return Arrays.asList(chargeCalculationType(ChargeCalculationType.FLAT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT));
    }

    @Override
    public List<EnumOptionData> retrieveApplicableToTypes() {
        final List<EnumOptionData> chargeAppliesToTypes = new ArrayList<>();
        for (final ChargeAppliesTo chargeAppliesTo : ChargeAppliesTo.values()) {
            if (ChargeAppliesTo.INVALID.equals(chargeAppliesTo)) {
                continue;
            }
            chargeAppliesToTypes.add(chargeAppliesTo(chargeAppliesTo));
        }
        return chargeAppliesToTypes;
    }

    @Override
    public List<EnumOptionData> retrieveCollectionTimeTypes() {
        final List<EnumOptionData> chargeTimeTypes = new ArrayList<>();
        for (final ChargeTimeType chargeTimeType : ChargeTimeType.values()) {
            if (ChargeTimeType.INVALID.equals(chargeTimeType) || ChargeTimeType.SAVINGS_CLOSURE.equals(chargeTimeType)) {
                continue;
            }
            chargeTimeTypes.add(ChargeEnumerations.chargeTimeType(chargeTimeType));
        }
        return chargeTimeTypes;
    }

    @Override
    public List<EnumOptionData> retrivePaymentModes() {
        return Arrays.asList(chargePaymentMode(ChargePaymentMode.REGULAR));
    }

    @Override
    public List<EnumOptionData> retrieveLoanCalculationTypes() {
        return Arrays.asList(chargeCalculationType(ChargeCalculationType.FLAT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT));
    }

    @Override
    public List<EnumOptionData> retrieveLoanCollectionTimeTypes() {
        return Arrays.asList(chargeTimeType(ChargeTimeType.DISBURSEMENT), chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE),
                chargeTimeType(ChargeTimeType.INSTALMENT_FEE), chargeTimeType(ChargeTimeType.OVERDUE_INSTALLMENT),
                chargeTimeType(ChargeTimeType.TRANCHE_DISBURSEMENT),chargeTimeType(ChargeTimeType.FORECLOSURE_CHARGE),chargeTimeType(ChargeTimeType.ADHOC_CHARGE),
                chargeTimeType(ChargeTimeType.BOUNCE_CHARGE));
    }

    @Override
    public List<EnumOptionData> retrieveSavingsCalculationTypes() {
        return Arrays.asList(chargeCalculationType(ChargeCalculationType.FLAT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT));
    }

    @Override
    public List<EnumOptionData> retrieveSavingsCollectionTimeTypes() {
        return Arrays.asList(chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE), chargeTimeType(ChargeTimeType.SAVINGS_ACTIVATION),
                // chargeTimeType(ChargeTimeType.SAVINGS_CLOSURE),
                chargeTimeType(ChargeTimeType.WITHDRAWAL_FEE), chargeTimeType(ChargeTimeType.ANNUAL_FEE),
                chargeTimeType(ChargeTimeType.MONTHLY_FEE), chargeTimeType(ChargeTimeType.WEEKLY_FEE),
                chargeTimeType(ChargeTimeType.OVERDRAFT_FEE), chargeTimeType(ChargeTimeType.SAVINGS_NOACTIVITY_FEE));
    }

    @Override
    public List<EnumOptionData> retrieveClientCalculationTypes() {
        return Arrays.asList(chargeCalculationType(ChargeCalculationType.FLAT));
    }

    @Override
    public List<EnumOptionData> retrieveClientCollectionTimeTypes() {
        return Arrays.asList(chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE));
    }

    @Override
    public List<EnumOptionData> retrieveSharesCalculationTypes() {
        return Arrays.asList(chargeCalculationType(ChargeCalculationType.FLAT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT));
    }

    @Override
    public List<EnumOptionData> retrieveSharesCollectionTimeTypes() {
        return Arrays.asList(chargeTimeType(ChargeTimeType.SHAREACCOUNT_ACTIVATION), chargeTimeType(ChargeTimeType.SHARE_PURCHASE),
                chargeTimeType(ChargeTimeType.SHARE_REDEEM));
    }

    @Override
    public List<EnumOptionData> retrieveChargesType() {
        return Arrays.asList(chargeAppliesTo(ChargeAppliesTo.LOAN));
    }
    public List<EnumOptionData> retrieveGst() {
        return Arrays.asList(gstType(GstEnum.INCLUSIVE),gstType(GstEnum.EXCLUSIVE));

    }

    public List<EnumOptionData> retrieveGstSlabLimitApplyFor() {
        return Arrays.asList(gstSlabLimitApplyFor(GstSlabLimitApplyFor.LOAN_AMOUNT),gstSlabLimitApplyFor(GstSlabLimitApplyFor.CHARGE_PERCENTAGE),gstSlabLimitApplyFor(GstSlabLimitApplyFor.CHARGE_AMOUNT));

    }
    public List<EnumOptionData> retrieveGstSlabLimitOperator() {
        return Arrays.asList(gstSlabLimitOperator(GstSlabLimitOperator.GREATER_THAN),gstSlabLimitOperator(GstSlabLimitOperator.GREATER_THAN_EQUAL_TO),gstSlabLimitOperator(GstSlabLimitOperator.LESS_THAN),gstSlabLimitOperator(GstSlabLimitOperator.LESS_THAN_EQUAL_TO),gstSlabLimitOperator(GstSlabLimitOperator.EQUAL_TO));

    }

    @Override
    public List<EnumOptionData> retrievePenaltyInterstDaysInYearOptions() {
        return Arrays.asList(penalInterestDaysInYearOption(DaysInYearType.ACTUAL),
                penalInterestDaysInYearOption(DaysInYearType.DAYS_360),
                penalInterestDaysInYearOption(DaysInYearType.DAYS_365));
    }

}

