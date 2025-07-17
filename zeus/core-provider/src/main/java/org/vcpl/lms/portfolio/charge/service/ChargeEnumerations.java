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

import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.portfolio.charge.domain.*;
import org.vcpl.lms.portfolio.common.domain.DaysInYearType;

public final class ChargeEnumerations {

    private ChargeEnumerations() {

    }

    public static EnumOptionData chargeTimeType(final int id) {
        return chargeTimeType(ChargeTimeType.fromInt(id));
    }

    public static EnumOptionData chargeTimeType(final ChargeTimeType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case DISBURSEMENT:
                optionData = new EnumOptionData(ChargeTimeType.DISBURSEMENT.getValue().longValue(), ChargeTimeType.DISBURSEMENT.getCode(),
                        "Disbursement");
            break;
            case SPECIFIED_DUE_DATE:
                optionData = new EnumOptionData(ChargeTimeType.SPECIFIED_DUE_DATE.getValue().longValue(),
                        ChargeTimeType.SPECIFIED_DUE_DATE.getCode(), "Specified due date");
            break;
            case SAVINGS_ACTIVATION:
                optionData = new EnumOptionData(ChargeTimeType.SAVINGS_ACTIVATION.getValue().longValue(),
                        ChargeTimeType.SAVINGS_ACTIVATION.getCode(), "Savings Activation");
            break;
            case SAVINGS_CLOSURE:
                optionData = new EnumOptionData(ChargeTimeType.SAVINGS_CLOSURE.getValue().longValue(),
                        ChargeTimeType.SAVINGS_CLOSURE.getCode(), "Savings Closure");
            break;
            case WITHDRAWAL_FEE:
                optionData = new EnumOptionData(ChargeTimeType.WITHDRAWAL_FEE.getValue().longValue(),
                        ChargeTimeType.WITHDRAWAL_FEE.getCode(), "Withdrawal Fee");
            break;
            case ANNUAL_FEE:
                optionData = new EnumOptionData(ChargeTimeType.ANNUAL_FEE.getValue().longValue(), ChargeTimeType.ANNUAL_FEE.getCode(),
                        "Annual Fee");
            break;
            case MONTHLY_FEE:
                optionData = new EnumOptionData(ChargeTimeType.MONTHLY_FEE.getValue().longValue(), ChargeTimeType.MONTHLY_FEE.getCode(),
                        "Monthly Fee");
            break;
            case WEEKLY_FEE:
                optionData = new EnumOptionData(ChargeTimeType.WEEKLY_FEE.getValue().longValue(), ChargeTimeType.WEEKLY_FEE.getCode(),
                        "Weekly Fee");
            break;
            case INSTALMENT_FEE:
                optionData = new EnumOptionData(ChargeTimeType.INSTALMENT_FEE.getValue().longValue(),
                        ChargeTimeType.INSTALMENT_FEE.getCode(), "Installment Fee");
            break;
            case OVERDUE_INSTALLMENT:
                optionData = new EnumOptionData(ChargeTimeType.OVERDUE_INSTALLMENT.getValue().longValue(),
                        ChargeTimeType.OVERDUE_INSTALLMENT.getCode(), "Overdue Charge");
            break;
            case OVERDRAFT_FEE:
                optionData = new EnumOptionData(ChargeTimeType.OVERDRAFT_FEE.getValue().longValue(), ChargeTimeType.OVERDRAFT_FEE.getCode(),
                        "Overdraft Fee");
            break;
            case TRANCHE_DISBURSEMENT:
                optionData = new EnumOptionData(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue().longValue(),
                        ChargeTimeType.TRANCHE_DISBURSEMENT.getCode(), "Tranche Disbursement");
            break;
            case SHAREACCOUNT_ACTIVATION:
                optionData = new EnumOptionData(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue().longValue(),
                        ChargeTimeType.SHAREACCOUNT_ACTIVATION.getCode(), "Share Account Activate");
            break;

            case SHARE_PURCHASE:
                optionData = new EnumOptionData(ChargeTimeType.SHARE_PURCHASE.getValue().longValue(),
                        ChargeTimeType.SHARE_PURCHASE.getCode(), "Share Purchase");
            break;
            case SHARE_REDEEM:
                optionData = new EnumOptionData(ChargeTimeType.SHARE_REDEEM.getValue().longValue(), ChargeTimeType.SHARE_REDEEM.getCode(),
                        "Share Redeem");
            break;
            case SAVINGS_NOACTIVITY_FEE:
                optionData = new EnumOptionData(ChargeTimeType.SAVINGS_NOACTIVITY_FEE.getValue().longValue(),
                        ChargeTimeType.SAVINGS_NOACTIVITY_FEE.getCode(), "Saving No Activity Fee");
            break;
            case FORECLOSURE_CHARGE:
                optionData = new EnumOptionData(ChargeTimeType.FORECLOSURE_CHARGE.getValue().longValue(),
                        ChargeTimeType.FORECLOSURE_CHARGE.getCode(), "Foreclosure Charge");
                break;
            case ADHOC_CHARGE:
                optionData = new EnumOptionData(ChargeTimeType.ADHOC_CHARGE.getValue().longValue(),
                        ChargeTimeType.ADHOC_CHARGE.getCode(), "Adhoc Charge");
                break;
            case BOUNCE_CHARGE:
                optionData = new EnumOptionData(ChargeTimeType.BOUNCE_CHARGE.getValue().longValue(),
                        ChargeTimeType.BOUNCE_CHARGE.getCode(), "Bounce Charge");
                break;
            default:
                optionData = new EnumOptionData(ChargeTimeType.INVALID.getValue().longValue(), ChargeTimeType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData chargeAppliesTo(final int id) {
        return chargeAppliesTo(ChargeAppliesTo.fromInt(id));
    }


    public static EnumOptionData chargeAppliesTo(final ChargeAppliesTo type) {
        EnumOptionData optionData = null;
        switch (type) {
            case LOAN:
                optionData = new EnumOptionData(ChargeAppliesTo.LOAN.getValue().longValue(), ChargeAppliesTo.LOAN.getCode(), "Loan");
            break;
            case SAVINGS:
                optionData = new EnumOptionData(ChargeAppliesTo.SAVINGS.getValue().longValue(), ChargeAppliesTo.SAVINGS.getCode(),
                        "Savings");
            break;
            case CLIENT:
                optionData = new EnumOptionData(ChargeAppliesTo.CLIENT.getValue().longValue(), ChargeAppliesTo.CLIENT.getCode(), "Client");
            break;
            case SHARES:
                optionData = new EnumOptionData(ChargeAppliesTo.SHARES.getValue().longValue(), ChargeAppliesTo.SHARES.getCode(), "Shares");
            break;
            default:
                optionData = new EnumOptionData(ChargeAppliesTo.INVALID.getValue().longValue(), ChargeAppliesTo.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }
    public static EnumOptionData gst(final int id){
        return gstType(GstEnum.fromInt(id));
    }
    public static EnumOptionData gstType(final GstEnum type)
    {
        EnumOptionData optionData = null;
        switch (type)
        {
            case INCLUSIVE:
                optionData = new EnumOptionData(GstEnum.INCLUSIVE.getValue().longValue(), GstEnum.INCLUSIVE.getCode(),
                        "Inclusive");
                break;
            case EXCLUSIVE:
                optionData = new EnumOptionData(GstEnum.EXCLUSIVE.getValue().longValue(),
                        GstEnum.EXCLUSIVE.getCode(), "Exclusive");
                break;
            default:
                optionData = new EnumOptionData(GstEnum.INVALID.getValue().longValue(),
                        GstEnum.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData gstSlabLimitApplyFor(final int id){
        return gstSlabLimitApplyFor(GstSlabLimitApplyFor.fromInt(id));
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

    public static EnumOptionData gstSlabLimitOperator(final int id){
        return gstSlabLimitOperator(GstSlabLimitOperator.fromInt(id));
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
    public static EnumOptionData chargeCalculationType(final int id) {
        return chargeCalculationType(ChargeCalculationType.fromInt(id));
    }

    public static EnumOptionData chargeCalculationType(final ChargeCalculationType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case FLAT:
                optionData = new EnumOptionData(ChargeCalculationType.FLAT.getValue().longValue(), ChargeCalculationType.FLAT.getCode(),
                        "Flat");
            break;
            case PERCENT_OF_AMOUNT:
                optionData = new EnumOptionData(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue().longValue(),
                        ChargeCalculationType.PERCENT_OF_AMOUNT.getCode(), "% Principal Amount");
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                optionData = new EnumOptionData(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue().longValue(),
                        ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getCode(), "% EMI Amount");
            break;
            case PERCENT_OF_INTEREST:
                optionData = new EnumOptionData(ChargeCalculationType.PERCENT_OF_INTEREST.getValue().longValue(),
                        ChargeCalculationType.PERCENT_OF_INTEREST.getCode(), "% Interest");
            break;
            case PERCENT_OF_DISBURSEMENT_AMOUNT:
                optionData = new EnumOptionData(ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue().longValue(),
                        ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getCode(), "% Disbursement Amount");
            break;
            default:
                optionData = new EnumOptionData(ChargeCalculationType.INVALID.getValue().longValue(),
                        ChargeCalculationType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData chargePaymentMode(final int id) {
        return chargePaymentMode(ChargePaymentMode.fromInt(id));
    }


    public static EnumOptionData chargePaymentMode(final ChargePaymentMode type) {
        EnumOptionData optionData = null;
        switch (type) {
//            case ACCOUNT_TRANSFER:
//                optionData = new EnumOptionData(ChargePaymentMode.ACCOUNT_TRANSFER.getValue().longValue(),
//                        ChargePaymentMode.ACCOUNT_TRANSFER.getCode(), "Account transfer");

            default:
                optionData = new EnumOptionData(ChargePaymentMode.REGULAR.getValue().longValue(), ChargePaymentMode.REGULAR.getCode(),
                        "Regular");
            break;




//                optionData=new EnumOptionData(ChargePaymentMode.INVALID.getValue().longValue(),ChargePaymentMode.INVALID.getCode(),"Invalid");
        }
        return optionData;
    }

    public static EnumOptionData penalInterestDaysInYearOption(DaysInYearType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case ACTUAL:
                optionData = new EnumOptionData(DaysInYearType.ACTUAL.getValue().longValue(),
                        DaysInYearType.ACTUAL.getCode(), "Actual");
                break;
            case DAYS_360:
                optionData = new EnumOptionData(DaysInYearType.DAYS_360.getValue().longValue(),
                        DaysInYearType.DAYS_360.getCode(), "360 Days");
                break;
            case DAYS_365:
                optionData = new EnumOptionData(DaysInYearType.DAYS_365.getValue().longValue(),
                        DaysInYearType.DAYS_365.getCode(), "365 Days");
                break;
            default:
                optionData = new EnumOptionData(DaysInYearType.INVALID.getValue().longValue(),
                        DaysInYearType.INVALID.getCode(), "Invalid");
                break;
        }
        return  optionData;
    }
}
