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
package org.vcpl.lms.portfolio.common.service;

import java.util.ArrayList;
import java.util.List;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.portfolio.charge.domain.GstSlabLimitApplyFor;
import org.vcpl.lms.portfolio.charge.domain.GstSlabLimitOperator;
import org.vcpl.lms.portfolio.common.domain.ConditionType;
import org.vcpl.lms.portfolio.common.domain.DaysInMonthType;
import org.vcpl.lms.portfolio.common.domain.DaysInYearType;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.loanproduct.domain.*;
import org.vcpl.lms.portfolio.charge.domain.GstEnum;

public final class CommonEnumerations {

    private CommonEnumerations() {

    }

    public static final String DAYS_IN_MONTH_TYPE = "daysInMonthType";
    public static final String DAYS_IN_YEAR_TYPE = "daysInYearType";

    public static EnumOptionData commonEnumueration(final String typeName, final int id) {
        EnumOptionData enumData = null;
        if (typeName.equals(DAYS_IN_MONTH_TYPE)) {
            enumData = daysInMonthType(id);
        } else if (typeName.equals(DAYS_IN_YEAR_TYPE)) {
            enumData = daysInYearType(id);
        }
        return enumData;
    }

    public static EnumOptionData termFrequencyType(final int id, final String codePrefix) {
        return termFrequencyType(PeriodFrequencyType.fromInt(id), codePrefix);
    }

    public static EnumOptionData termFrequencyType(final PeriodFrequencyType type, final String codePrefix) {
        EnumOptionData optionData = null;
        switch (type) {
            case DAYS:
                optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.YEARS.getCode(), "Years");
            break;
            default:
                optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData conditionType(final int id, final String codePrefix) {
        return conditionType(ConditionType.fromInt(id), codePrefix);
    }

    public static EnumOptionData conditionType(final ConditionType type, final String codePrefix) {
        EnumOptionData optionData = null;
        switch (type) {
            case EQUAL:
                optionData = new EnumOptionData(ConditionType.EQUAL.getValue().longValue(), codePrefix + ConditionType.EQUAL.getCode(),
                        "equal");
            break;
            case NOT_EQUAL:
                optionData = new EnumOptionData(ConditionType.NOT_EQUAL.getValue().longValue(),
                        codePrefix + ConditionType.NOT_EQUAL.getCode(), "notEqual");
            break;
            case GRETERTHAN:
                optionData = new EnumOptionData(ConditionType.GRETERTHAN.getValue().longValue(),
                        codePrefix + ConditionType.GRETERTHAN.getCode(), "greterthan");
            break;
            case LESSTHAN:
                optionData = new EnumOptionData(ConditionType.LESSTHAN.getValue().longValue(),
                        codePrefix + ConditionType.LESSTHAN.getCode(), "lessthan");
            break;
            default:
                optionData = new EnumOptionData(ConditionType.INVALID.getValue().longValue(), ConditionType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> conditionType(final ConditionType[] conditionTypes, final String codePrefix) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final ConditionType conditionType : conditionTypes) {
            if (!conditionType.isInvalid()) {
                optionDatas.add(conditionType(conditionType, codePrefix));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData daysInMonthType(final int id) {
        return daysInMonthType(DaysInMonthType.fromInt(id));
    }

    public static EnumOptionData brokenStrategy(final int id) {
        return brokenStrategy(BrokenStrategy.fromInt(id));
    }

    public static EnumOptionData gst(final int id) {
        return gst(GstEnum.fromInt(id));
    }
    public static EnumOptionData gstSlabLimitApplyFor(final int id) {
        return gstSlabLimitApplyFor(GstSlabLimitApplyFor.fromInt(id));
    }
    public static EnumOptionData gstSlabLimitOperator(final int id) {
        return gstSlabLimitOperator(GstSlabLimitOperator.fromInt(id));
    }
    public static EnumOptionData collectionMode(final int id) {
        return collectionMode(CollectionMode.collection(id));
    }

    public static EnumOptionData disbursementMode(final int id)
    {
        return disbursementMode(DisbursementMode.disbursement(id));
    }

    public static EnumOptionData brokenDaysInMonth(final int id)
    {
        return brokenDaysInMonth(BrokenStrategyDaysInMonth.fromInt(id));
    }

    public static EnumOptionData brokenDaysInYears(final int id)
    {
        return brokenDaysInYears(BrokenStrategyDayInYear.fromInt(id));
    }

    public static EnumOptionData transactionTypePreference(final int id) {
        return transactionTypePreference(TransactionTypePreference.transactionTypePreference(id));
    }

    private static EnumOptionData brokenDaysInYears(BrokenStrategyDayInYear fromInt) {

        EnumOptionData optionData = null;
        switch (fromInt) {
            case INVALID:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.INVALID.getValue().longValue(),BrokenStrategyDayInYear.INVALID.getCode(), "invalid");
                break;
            case ACTUAL:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.ACTUAL.getValue().longValue(), BrokenStrategyDaysInMonth.ACTUAL.getCode(),
                        " actual");
                break;
            case DAYS_360:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.DAYS_360.getValue().longValue(), BrokenStrategyDayInYear.DAYS_360.getCode(),
                        "360");
                break;
            case DAYS_364:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.DAYS_364.getValue().longValue(), BrokenStrategyDayInYear.DAYS_364.getCode(),
                        "364");
                break;
            case DAYS_365:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.DAYS_365.getValue().longValue(), BrokenStrategyDayInYear.DAYS_365.getCode(),
                        "365");
                break;
            case NOBROKENDAYS:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.NOBROKENDAYS.getValue().longValue(), BrokenStrategyDayInYear.NOBROKENDAYS.getCode(),
                        "NoBrokenInterest");
                break;
            default:
                optionData = new EnumOptionData(BrokenStrategyDaysInMonth.DAYS_31.getValue().longValue(), BrokenStrategyDaysInMonth.DAYS_31.getCode(),
                        "31");
                break;
        }
        return optionData;


    }

    private static EnumOptionData brokenDaysInMonth(BrokenStrategyDaysInMonth fromInt) {

        EnumOptionData optionData = null;
        switch (fromInt) {
            case INVALID:
                optionData = new EnumOptionData(BrokenStrategyDaysInMonth.INVALID.getValue().longValue(),BrokenStrategyDaysInMonth.INVALID.getCode(), "invalid");
                break;
            case ACTUAL:
                optionData = new EnumOptionData(BrokenStrategyDaysInMonth.ACTUAL.getValue().longValue(), BrokenStrategyDaysInMonth.ACTUAL.getCode(),
                        " actual");
                break;
            case DAYS_30:
                optionData = new EnumOptionData(BrokenStrategyDaysInMonth.DAYS_30.getValue().longValue(), BrokenStrategyDaysInMonth.DAYS_30.getCode(),
                        "30");
                break;
            default:
                optionData = new EnumOptionData(BrokenStrategyDaysInMonth.DAYS_31.getValue().longValue(), BrokenStrategyDaysInMonth.DAYS_31.getCode(),
                        "31");
                break;
        }
        return optionData;
    }


    public static EnumOptionData daysInMonthType(final DaysInMonthType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case ACTUAL:
                optionData = new EnumOptionData(DaysInMonthType.ACTUAL.getValue().longValue(), DaysInMonthType.ACTUAL.getCode(), "Actual");
            break;
            case DAYS_30:
                optionData = new EnumOptionData(DaysInMonthType.DAYS_30.getValue().longValue(), DaysInMonthType.DAYS_30.getCode(),
                        "30 Days");
            break;
            case DAYS_31:
                optionData = new EnumOptionData(DaysInMonthType.DAYS_31.getValue().longValue(), DaysInMonthType.DAYS_31.getCode(),
                        "31 Days");
                break;
            default:
                optionData = new EnumOptionData(DaysInMonthType.INVALID.getValue().longValue(), DaysInMonthType.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData brokenStrategy(final BrokenStrategy type) {
        EnumOptionData optionData = null;
        switch (type) {
            case NOBROKEN:
                optionData = new EnumOptionData(BrokenStrategy.NOBROKEN.getValue().longValue(), BrokenStrategy.NOBROKEN.getCode(), "Nobroken");
                break;
            case DISBURSEMENT:
                optionData = new EnumOptionData(BrokenStrategy.DISBURSEMENT.getValue().longValue(), BrokenStrategy.DISBURSEMENT.getCode(),
                        " Disbursement");
                break;
            case FIRSTREPAYMENT:
                optionData = new EnumOptionData(BrokenStrategy.FIRSTREPAYMENT.getValue().longValue(), BrokenStrategy.FIRSTREPAYMENT.getCode(),
                        "FirstRepayment");
                break;
            case LASTREPAYMENT:
                optionData = new EnumOptionData(BrokenStrategy.LASTREPAYMENT.getValue().longValue(), BrokenStrategy.LASTREPAYMENT.getCode(),
                        "LastRepayment");
                break;
            default:
                optionData = new EnumOptionData(BrokenStrategy.INVALID.getValue().longValue(), BrokenStrategy.INVALID.getCode(),
                        "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData gst(final GstEnum type) {
        EnumOptionData optionData = null;
        switch (type) {
            case INCLUSIVE:
                optionData = new EnumOptionData(GstEnum.INCLUSIVE.getValue().longValue(), GstEnum.INCLUSIVE.getCode(), "Inclusive");
                break;
            case EXCLUSIVE:
                optionData = new EnumOptionData(GstEnum.EXCLUSIVE.getValue().longValue(), GstEnum.EXCLUSIVE.getCode(),
                        " Exclusive");
                break;
            default:
                optionData = new EnumOptionData(GstEnum.INVALID.getValue().longValue(), GstEnum.INVALID.getCode(),
                        "Invalid");
                break;
        }
        return optionData;
    }
    public static EnumOptionData gstSlabLimitApplyFor(final GstSlabLimitApplyFor type)
    {
        EnumOptionData optionData = null;
        switch (type)
        {
            case LOAN_AMOUNT:
                optionData = new EnumOptionData(GstSlabLimitApplyFor.LOAN_AMOUNT.getValue().longValue(), GstSlabLimitApplyFor.LOAN_AMOUNT.getCode(),
                        "Loan Amount");
                break;
            case CHARGE_PERCENTAGE:
                optionData = new EnumOptionData(GstSlabLimitApplyFor.CHARGE_PERCENTAGE.getValue().longValue(),
                        GstSlabLimitApplyFor.CHARGE_PERCENTAGE.getCode(), "Charge Percentage");
                break;
            case CHARGE_AMOUNT:
                optionData = new EnumOptionData(GstSlabLimitApplyFor.CHARGE_AMOUNT.getValue().longValue(),
                        GstSlabLimitApplyFor.CHARGE_AMOUNT.getCode(), "Charge Amount");
                break;
            default:
                optionData = new EnumOptionData(GstSlabLimitApplyFor.INVALID.getValue().longValue(),
                        GstSlabLimitApplyFor.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }
    public static EnumOptionData gstSlabLimitOperator(final GstSlabLimitOperator type)
    {
        EnumOptionData optionData = null;
        switch (type)
        {
            case GREATER_THAN:
                optionData = new EnumOptionData(GstSlabLimitOperator.GREATER_THAN.getValue().longValue(), GstSlabLimitOperator.GREATER_THAN.getCode(),
                        "Greater Than");
                break;
            case GREATER_THAN_EQUAL_TO:
                optionData = new EnumOptionData(GstSlabLimitOperator.GREATER_THAN_EQUAL_TO.getValue().longValue(), GstSlabLimitOperator.GREATER_THAN_EQUAL_TO.getCode(),
                        "Greater Than Equal To");
                break;
            case LESS_THAN:
                optionData = new EnumOptionData(GstSlabLimitOperator.LESS_THAN.getValue().longValue(), GstSlabLimitOperator.LESS_THAN.getCode(),
                        "Less Than");
                break;
            case LESS_THAN_EQUAL_TO:
                optionData = new EnumOptionData(GstSlabLimitOperator.LESS_THAN_EQUAL_TO.getValue().longValue(), GstSlabLimitOperator.LESS_THAN_EQUAL_TO.getCode(),
                        "Less Than Equal To");
                break;
            case EQUAL_TO:
                optionData = new EnumOptionData(GstSlabLimitOperator.EQUAL_TO.getValue().longValue(), GstSlabLimitOperator.EQUAL_TO.getCode(),
                        "Equal To");
                break;
            default:
                optionData = new EnumOptionData(GstSlabLimitApplyFor.INVALID.getValue().longValue(),
                        GstSlabLimitApplyFor.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }
    public static EnumOptionData collectionMode(final CollectionMode type) {
        EnumOptionData optionData = null;
        switch (type) {
            case PARTNER:
                optionData = new EnumOptionData(CollectionMode.PARTNER.getValue().longValue(),CollectionMode.PARTNER.getCode(), "partner");
                break;
            case DIRECT:
                optionData = new EnumOptionData(CollectionMode.DIRECT.getValue().longValue(), CollectionMode.DIRECT.getCode(),
                        " direct");
                break;
            case ESCROW:
                optionData = new EnumOptionData(CollectionMode.ESCROW.getValue().longValue(), CollectionMode.ESCROW.getCode(),
                        "escrow");
                break;
            case RAZORPAY:
                optionData = new EnumOptionData(CollectionMode.RAZORPAY.getValue().longValue(),CollectionMode.RAZORPAY.getCode(),
                        "Razorpay");
                break;
            default:
                optionData = new EnumOptionData(CollectionMode.INVALID.getValue().longValue(), CollectionMode.INVALID.getCode(),
                        "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData disbursementMode(final DisbursementMode type) {
        EnumOptionData optionData = null;
        switch (type) {
            case DIRECT:
                optionData = new EnumOptionData(DisbursementMode.DIRECT.getValue().longValue(),DisbursementMode.DIRECT.getCode(), "direct");
                break;
            case ESCROW:
                optionData = new EnumOptionData(DisbursementMode.ESCROW.getValue().longValue(), BrokenStrategy.DISBURSEMENT.getCode(),
                        " escrow");
                break;
            case REIMBURSEMENT:
                optionData = new EnumOptionData(DisbursementMode.REIMBURSEMENT.getValue().longValue(), DisbursementMode.REIMBURSEMENT.getCode(),
                        "Reimbursement");
                break;
            default:
                optionData = new EnumOptionData(DisbursementMode.INVALID.getValue().longValue(), DisbursementMode.INVALID.getCode(),
                        "Invalid");
                break;
        }
        return optionData;
    }


    public static EnumOptionData daysInYearType(final int id) {
        return daysInYearType(DaysInYearType.fromInt(id));
    }

    public static EnumOptionData daysInYearType(final DaysInYearType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case ACTUAL:
                optionData = new EnumOptionData(DaysInYearType.ACTUAL.getValue().longValue(), DaysInYearType.ACTUAL.getCode(), "Actual");
            break;
            case DAYS_360:
                optionData = new EnumOptionData(DaysInYearType.DAYS_360.getValue().longValue(), DaysInYearType.DAYS_360.getCode(),
                        "360 Days");
            break;
            case DAYS_364:
                optionData = new EnumOptionData(DaysInYearType.DAYS_364.getValue().longValue(), DaysInYearType.DAYS_364.getCode(),
                        "364 Days");
            break;
            case DAYS_365:
                optionData = new EnumOptionData(DaysInYearType.DAYS_365.getValue().longValue(), DaysInYearType.DAYS_365.getCode(),
                        "365 Days");
            break;
            default:
                optionData = new EnumOptionData(DaysInYearType.INVALID.getValue().longValue(), DaysInYearType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData transactionTypePreference(final TransactionTypePreference type) {
        EnumOptionData optionData = null;
        switch (type) {
            case IMPS:
                optionData = new EnumOptionData(TransactionTypePreference.IMPS.getValue().longValue(),TransactionTypePreference.IMPS.getCode(), "IMPS");
                break;
            case RTGS:
                optionData = new EnumOptionData(TransactionTypePreference.RTGS.getValue().longValue(), TransactionTypePreference.RTGS.getCode(),
                        " RTGS");
                break;
            case NEFT:
                optionData = new EnumOptionData(TransactionTypePreference.NEFT.getValue().longValue(), TransactionTypePreference.NEFT.getCode(),
                        "NEFT");
                break;
            default:
                optionData = new EnumOptionData(TransactionTypePreference.INVALID.getValue().longValue(), TransactionTypePreference.INVALID.getCode(),
                        "invalid");
                break;
        }
        return optionData;
    }

}
