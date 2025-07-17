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

import org.vcpl.lms.accounting.common.AccountingEnumerations;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.portfolio.accountdetails.service.AccountEnumerations;
import org.vcpl.lms.portfolio.common.domain.DayOfWeekType;
import org.vcpl.lms.portfolio.common.domain.NthDayType;
import org.vcpl.lms.portfolio.common.domain.PeriodFrequencyType;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.enums.BulkReportDataEnum;
import org.vcpl.lms.portfolio.loanaccount.data.LoanStatusEnumData;
import org.vcpl.lms.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanStatus;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTermVariationType;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionType;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.PmtCalcEnum;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.Enum.ServicerFeeChargesRatio;
import org.vcpl.lms.portfolio.loanproduct.domain.*;

public final class LoanEnumerations {

    private LoanEnumerations() {

    }

    public static final String LOAN_TERM_FREQUENCY_TYPE = "loanTermFrequencyType";
    public static final String TERM_FREQUENCY_TYPE = "termFrequencyType";
    public static final String REPAYMENT_FREQUENCY_TYPE = "repaymentFrequencyType";
    public static final String INTEREST_RATE_FREQUENCY_TYPE = "interestRateFrequencyType";
    public static final String AMORTIZATION_TYPE = "amortizationType";
    public static final String INTEREST_TYPE = "interestType";
    public static final String INTEREST_CALCULATION_PERIOD_TYPE = "interestCalculationPeriodType";
    public static final String PAYMENT_TYPE = "paymentType";
    public static final String ACCOUNTING_RULE_TYPE = "accountingRule";
    public static final String LOAN_TYPE = "loanType";
    public static final String INTEREST_RECALCULATION_COMPOUNDING_TYPE = "interestRecalculationCompoundingType";
    public static final String RESCHEDULE_STRATEGY_TYPE = "rescheduleStrategyType";

    public static EnumOptionData loanEnumueration(final String typeName, final int id) {
        if (typeName.equals(LOAN_TERM_FREQUENCY_TYPE)) {
            return loanTermFrequencyType(id);
        } else if (typeName.equals(TERM_FREQUENCY_TYPE)) {
            return termFrequencyType(id);
        } else if (typeName.equals(REPAYMENT_FREQUENCY_TYPE)) {
            return repaymentFrequencyType(id);
        } else if (typeName.equals(INTEREST_RATE_FREQUENCY_TYPE)) {
            return interestRateFrequencyType(id);
        } else if (typeName.equals(AMORTIZATION_TYPE)) {
            return amortizationType(id);
        } else if (typeName.equals(INTEREST_TYPE)) {
            return interestType(id);
        } else if (typeName.equals(INTEREST_CALCULATION_PERIOD_TYPE)) {
            return interestCalculationPeriodType(id);
        } else if (typeName.equals(ACCOUNTING_RULE_TYPE)) {
            return AccountingEnumerations.accountingRuleType(id);
        } else if (typeName.equals(LOAN_TYPE)) {
            return AccountEnumerations.loanType(id);
        } else if (typeName.equals(INTEREST_RECALCULATION_COMPOUNDING_TYPE)) {
            return interestRecalculationCompoundingType(id);
        } else if (typeName.equals(RESCHEDULE_STRATEGY_TYPE)) {
            return rescheduleStrategyType(id);
        }
        return null;
    }

    public static EnumOptionData loanTermFrequencyType(final int id) {
        return loanTermFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData loanTermFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "loanTermFrequency.";
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

    public static EnumOptionData termFrequencyType(final int id) {
        return termFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData termFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "termFrequency.";
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

    public static EnumOptionData repaymentFrequencyType(final int id) {
        return repaymentFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData repaymentFrequencyNthDayType(final Integer id) {
        if (id == null) {
            return null;
        }
        return repaymentFrequencyNthDayType(NthDayType.fromInt(id));
    }

    public static EnumOptionData repaymentFrequencyNthDayType(final NthDayType type) {
        final String codePrefix = "repaymentFrequency.";
        long nthDayValue = type.getValue().longValue();
        EnumOptionData optionData = null;
        switch (type) {
            case ONE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "1");
                break;
            case TWO:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "2");
                break;
            case THREE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "3");
                break;
            case FOUR:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "4");
                break;
            case FIVE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "5");
                break;
            case SIX:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "6");
                break;
            case SEVEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "7");
                break;
            case EIGHT:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "8");
                break;
            case NINE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "9");
                break;
            case TEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "10");
                break;
            case ELEVEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "11");
                break;
            case TWELVE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "12");
                break;
            case THIRTEEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "13");
                break;
            case FOURTEEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "14");
                break;
            case FIFTEEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "15");
                break;
            case SIXTEEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "16");
                break;
            case SEVENTEEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "17");
                break;
            case EIGHTEEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "18");
                break;
            case NINETEEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "19");
                break;
            case TWENTY:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "20");
                break;
            case TWENTYONE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "21");
                break;
            case TWENTYTWO:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "22");
                break;
            case TWENTYTHREE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "23");
                break;
            case TWENTYFOUR:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "24");
                break;
            case TWENTYFIVE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "25");
                break;
            case TWENTYSIX:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "26");
                break;
            case TWENTYSEVEN:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "27");
                break;
            case TWENTYEIGHT:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "28");
                break;
            case TWENTYNINE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "29");
                break;
            case THIRTY:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "30");
                break;
            case THIRTYONE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "31");
                break;
            default:
                optionData = new EnumOptionData(Integer.valueOf(0).longValue(), codePrefix + type.getCode(), "invalid");
                break;
        }

        return optionData;
    }

    public static EnumOptionData repaymentFrequencyDayOfWeekType(final Integer id) {
        if (id == null) {
            return null;
        }
        return repaymentFrequencyDayOfWeekType(DayOfWeekType.fromInt(id));
    }

    public static EnumOptionData repaymentFrequencyDayOfWeekType(final DayOfWeekType type) {
        final String codePrefix = "repaymentFrequency.";
        EnumOptionData optionData = new EnumOptionData(type.getValue().longValue(), codePrefix + type.getCode(), type.toString());

        return optionData;
    }

    public static EnumOptionData repaymentFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "repaymentFrequency.";
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

    public static EnumOptionData interestRateFrequencyType(final Integer id) {
        return interestRateFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData interestRateFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "interestRateFrequency.";
        EnumOptionData optionData = null;
        switch (type) {
            case MONTHS:
                optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Per month");
                break;
            case YEARS:
                optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.YEARS.getCode(), "Per year");
                break;
            case WHOLE_TERM:
                optionData = new EnumOptionData(PeriodFrequencyType.WHOLE_TERM.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.WHOLE_TERM.getCode(), "Whole term");
                break;
            default:
                optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(),
                        "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData amortizationType(final Integer id) {
        return amortizationType(AmortizationMethod.fromInt(id));
    }

    public static EnumOptionData amortizationType(final AmortizationMethod amortizationMethod) {
        EnumOptionData optionData = null;
        switch (amortizationMethod) {
            case EQUAL_INSTALLMENTS:
                optionData = new EnumOptionData(AmortizationMethod.EQUAL_INSTALLMENTS.getValue().longValue(),
                        AmortizationMethod.EQUAL_INSTALLMENTS.getCode(), "Equal installments");
                break;
            case EQUAL_PRINCIPAL:
                optionData = new EnumOptionData(AmortizationMethod.EQUAL_PRINCIPAL.getValue().longValue(),
                        AmortizationMethod.EQUAL_PRINCIPAL.getCode(), "Equal principal payments");
                break;
            default:
                optionData = new EnumOptionData(AmortizationMethod.INVALID.getValue().longValue(), AmortizationMethod.INVALID.getCode(),
                        "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData interestType(final Integer id) {
        return interestType(InterestMethod.fromInt(id));
    }

    public static EnumOptionData interestType(final InterestMethod type) {
        EnumOptionData optionData = null;
        switch (type) {
            case FLAT:
                optionData = new EnumOptionData(InterestMethod.FLAT.getValue().longValue(), InterestMethod.FLAT.getCode(), "Flat");
                break;
            case DECLINING_BALANCE:
                optionData = new EnumOptionData(InterestMethod.DECLINING_BALANCE.getValue().longValue(),
                        InterestMethod.DECLINING_BALANCE.getCode(), "Declining Balance");
                break;
            default:
                optionData = new EnumOptionData(InterestMethod.INVALID.getValue().longValue(), InterestMethod.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData interestCalculationPeriodType(final Integer id) {
        return interestCalculationPeriodType(InterestCalculationPeriodMethod.fromInt(id));
    }

    public static EnumOptionData interestCalculationPeriodType(final InterestCalculationPeriodMethod type) {
        EnumOptionData optionData = null;
        switch (type) {
            case DAILY:
                optionData = new EnumOptionData(InterestCalculationPeriodMethod.DAILY.getValue().longValue(),
                        InterestCalculationPeriodMethod.DAILY.getCode(), "Daily");
                break;
            case SAME_AS_REPAYMENT_PERIOD:
                optionData = new EnumOptionData(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getValue().longValue(),
                        InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getCode(), "Same as repayment period");
                break;
            default:
                optionData = new EnumOptionData(InterestCalculationPeriodMethod.INVALID.getValue().longValue(),
                        InterestCalculationPeriodMethod.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }

    public static LoanTransactionEnumData transactionType(final Integer id) {
        return transactionType(LoanTransactionType.fromInt(id));
    }

    public static LoanTransactionEnumData transactionType(final LoanTransactionType type) {
        LoanTransactionEnumData optionData = null;
        switch (type) {
            case INVALID:
                optionData = new LoanTransactionEnumData(LoanTransactionType.INVALID.getValue().longValue(),
                        LoanTransactionType.INVALID.getCode(), "Invalid");
                break;
            case DISBURSEMENT:
                optionData = new LoanTransactionEnumData(LoanTransactionType.DISBURSEMENT.getValue().longValue(),
                        LoanTransactionType.DISBURSEMENT.getCode(), "Disbursement");
                break;
            case REPAYMENT:
                optionData = new LoanTransactionEnumData(LoanTransactionType.REPAYMENT.getValue().longValue(),
                        LoanTransactionType.REPAYMENT.getCode(), "Repayment");
                break;
            case REPAYMENT_AT_DISBURSEMENT:
                optionData = new LoanTransactionEnumData(LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue().longValue(),
                        LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getCode(), "Repayment (at time of disbursement)");
                break;
            case CONTRA:
                optionData = new LoanTransactionEnumData(LoanTransactionType.CONTRA.getValue().longValue(),
                        LoanTransactionType.CONTRA.getCode(), "Reversal");
                break;
            case WAIVE_INTEREST:
                optionData = new LoanTransactionEnumData(LoanTransactionType.WAIVE_INTEREST.getValue().longValue(),
                        LoanTransactionType.WAIVE_INTEREST.getCode(), "Waive interest");
                break;
            case MARKED_FOR_RESCHEDULING:
                optionData = new LoanTransactionEnumData(LoanTransactionType.MARKED_FOR_RESCHEDULING.getValue().longValue(),
                        LoanTransactionType.MARKED_FOR_RESCHEDULING.getCode(), "Close (as rescheduled)");
                break;
            case WRITEOFF:
                optionData = new LoanTransactionEnumData(LoanTransactionType.WRITEOFF.getValue().longValue(),
                        LoanTransactionType.WRITEOFF.getCode(), "Close (as written-off)");
                break;
            case RECOVERY_REPAYMENT:
                optionData = new LoanTransactionEnumData(LoanTransactionType.RECOVERY_REPAYMENT.getValue().longValue(),
                        LoanTransactionType.RECOVERY_REPAYMENT.getCode(), "Repayment (after write-off)");
                break;
            case WAIVE_CHARGES:
                optionData = new LoanTransactionEnumData(LoanTransactionType.WAIVE_CHARGES.getValue().longValue(),
                        LoanTransactionType.WAIVE_CHARGES.getCode(), "Waive loan charges");
                break;
            case ACCRUAL:
                optionData = new LoanTransactionEnumData(LoanTransactionType.ACCRUAL.getValue().longValue(),
                        LoanTransactionType.ACCRUAL.getCode(), "Accrual");
                break;
            case APPROVE_TRANSFER:
                optionData = new LoanTransactionEnumData(LoanTransactionType.APPROVE_TRANSFER.getValue().longValue(),
                        LoanTransactionType.APPROVE_TRANSFER.getCode(), "Transfer approved");
                break;
            case INITIATE_TRANSFER:
                optionData = new LoanTransactionEnumData(LoanTransactionType.INITIATE_TRANSFER.getValue().longValue(),
                        LoanTransactionType.INITIATE_TRANSFER.getCode(), "Transfer initiated");
                break;
            case WITHDRAW_TRANSFER:
                optionData = new LoanTransactionEnumData(LoanTransactionType.WITHDRAW_TRANSFER.getValue().longValue(),
                        LoanTransactionType.WITHDRAW_TRANSFER.getCode(), "Transfer Withdrawn");
                break;
            case REJECT_TRANSFER:
                optionData = new LoanTransactionEnumData(LoanTransactionType.REJECT_TRANSFER.getValue().longValue(),
                        LoanTransactionType.REJECT_TRANSFER.getCode(), "Transfer Rejected");
                break;
            case REFUND:
                optionData = new LoanTransactionEnumData(LoanTransactionType.REFUND.getValue().longValue(),
                        LoanTransactionType.REFUND.getCode(), "Transfer Refund");
                break;
            case CHARGE_PAYMENT:
                optionData = new LoanTransactionEnumData(LoanTransactionType.CHARGE_PAYMENT.getValue().longValue(),
                        LoanTransactionType.CHARGE_PAYMENT.getCode(), "Charge Payment");
                break;
            case REFUND_FOR_ACTIVE_LOAN:
                optionData = new LoanTransactionEnumData(LoanTransactionType.REFUND_FOR_ACTIVE_LOAN.getValue().longValue(),
                        LoanTransactionType.REFUND_FOR_ACTIVE_LOAN.getCode(), "Refund");
                break;
            case INCOME_POSTING:
                optionData = new LoanTransactionEnumData(LoanTransactionType.INCOME_POSTING.getValue().longValue(),
                        LoanTransactionType.INCOME_POSTING.getCode(), "Income Posting");
                break;
            case CREDIT_BALANCE_REFUND:
                optionData = new LoanTransactionEnumData(LoanTransactionType.CREDIT_BALANCE_REFUND.getValue().longValue(),
                        LoanTransactionType.CREDIT_BALANCE_REFUND.getCode(), "Credit Balance Refund");
                break;
            case MERCHANT_ISSUED_REFUND:
                optionData = new LoanTransactionEnumData(LoanTransactionType.MERCHANT_ISSUED_REFUND.getValue().longValue(),
                        LoanTransactionType.MERCHANT_ISSUED_REFUND.getCode(), "Merchant Issued Refund");
                break;
            case PAYOUT_REFUND:
                optionData = new LoanTransactionEnumData(LoanTransactionType.PAYOUT_REFUND.getValue().longValue(),
                        LoanTransactionType.PAYOUT_REFUND.getCode(), "Payout Refund");
                break;
            case GOODWILL_CREDIT:
                optionData = new LoanTransactionEnumData(LoanTransactionType.GOODWILL_CREDIT.getValue().longValue(),
                        LoanTransactionType.GOODWILL_CREDIT.getCode(), "Goodwill Credit");
                break;
            case FORECLOSURE:
                optionData = new LoanTransactionEnumData(LoanTransactionType.FORECLOSURE.getValue().longValue(),
                        LoanTransactionType.FORECLOSURE.getCode(), "Foreclosure");
                break;
            case ADHOC_CHARGE:
                optionData = new LoanTransactionEnumData(LoanTransactionType.ADHOC_CHARGE.getValue().longValue(),
                        LoanTransactionType.ADHOC_CHARGE.getCode(), "AdhocCharge");
                break;
            case COOLING_OFF:
                optionData = new LoanTransactionEnumData(LoanTransactionType.COOLING_OFF.getValue().longValue(),
                        LoanTransactionType.COOLING_OFF.getCode(),  "CoolingOff");
                break;
            case CHARGE_REVERSAL:
                optionData = new LoanTransactionEnumData(LoanTransactionType.CHARGE_REVERSAL.getValue().longValue(),
                        LoanTransactionType.CHARGE_REVERSAL.getCode(),  "ChargeReversal");
                break;
        }
        return optionData;
    }

    public static EnumOptionData status(final LoanStatusEnumData status) {

        Long id = status.id();
        String code = status.code();
        String value = status.value();

        return new EnumOptionData(id, code, value);
    }

    public static LoanStatusEnumData status(final Integer statusId) {
        return status(LoanStatus.fromInt(statusId));
    }

    public static LoanStatusEnumData status(final LoanStatus status) {
        LoanStatusEnumData optionData = new LoanStatusEnumData(LoanStatus.INVALID.getValue().longValue(), LoanStatus.INVALID.getCode(),
                "Invalid");
        switch (status) {
            case INVALID:
                optionData = new LoanStatusEnumData(LoanStatus.INVALID.getValue().longValue(), LoanStatus.INVALID.getCode(), "Invalid");
                break;
            case SUBMITTED_AND_PENDING_APPROVAL:
                optionData = new LoanStatusEnumData(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue().longValue(),
                        LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval");
                break;
            case APPROVED:
                optionData = new LoanStatusEnumData(LoanStatus.APPROVED.getValue().longValue(), LoanStatus.APPROVED.getCode(), "Approved");
                break;
            case ACTIVE:
                optionData = new LoanStatusEnumData(LoanStatus.ACTIVE.getValue().longValue(), LoanStatus.ACTIVE.getCode(), "Active");
                break;
            case REJECTED:
                optionData = new LoanStatusEnumData(LoanStatus.REJECTED.getValue().longValue(), LoanStatus.REJECTED.getCode(), "Rejected");
                break;
            case WITHDRAWN_BY_CLIENT:
                optionData = new LoanStatusEnumData(LoanStatus.WITHDRAWN_BY_CLIENT.getValue().longValue(),
                        LoanStatus.WITHDRAWN_BY_CLIENT.getCode(), "Withdrawn by applicant");
                break;
            case CLOSED_OBLIGATIONS_MET:
                optionData = new LoanStatusEnumData(LoanStatus.CLOSED_OBLIGATIONS_MET.getValue().longValue(),
                        LoanStatus.CLOSED_OBLIGATIONS_MET.getCode(), "Closed (obligations met)");
                break;
            case CLOSED_WRITTEN_OFF:
                optionData = new LoanStatusEnumData(LoanStatus.CLOSED_WRITTEN_OFF.getValue().longValue(),
                        LoanStatus.CLOSED_WRITTEN_OFF.getCode(), "Closed (written off)");
                break;
            case CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT:
                optionData = new LoanStatusEnumData(LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT.getValue().longValue(),
                        LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT.getCode(), "Closed (rescheduled)");
                break;
            case OVERPAID:
                optionData = new LoanStatusEnumData(LoanStatus.OVERPAID.getValue().longValue(), LoanStatus.OVERPAID.getCode(), "Overpaid");
                break;
            case TRANSFER_IN_PROGRESS:
                optionData = new LoanStatusEnumData(LoanStatus.TRANSFER_IN_PROGRESS.getValue().longValue(),
                        LoanStatus.TRANSFER_IN_PROGRESS.getCode(), "Transfer in progress");
                break;
            case TRANSFER_ON_HOLD:
                optionData = new LoanStatusEnumData(LoanStatus.TRANSFER_ON_HOLD.getValue().longValue(),
                        LoanStatus.TRANSFER_ON_HOLD.getCode(), "Transfer on hold");
                break;
            case FORECLOSED:
                optionData = new LoanStatusEnumData(LoanStatus.FORECLOSED.getValue().longValue(),
                        LoanStatus.FORECLOSED.getCode(), "foreclosed");
                break;
            case COOLING_OFF:
                optionData = new LoanStatusEnumData(LoanStatus.COOLING_OFF.getValue().longValue(),
                        LoanStatus.COOLING_OFF.getCode(), "coolingOff");
                break;
        }

        return optionData;
    }

    public static EnumOptionData loanCycleValueConditionType(final int id) {
        return loanCycleValueConditionType(LoanProductValueConditionType.fromInt(id));
    }

    public static EnumOptionData loanCycleValueConditionType(final LoanProductValueConditionType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case EQUAL:
                optionData = new EnumOptionData(LoanProductValueConditionType.EQUAL.getValue().longValue(),
                        LoanProductValueConditionType.EQUAL.getCode(), "equals");
                break;
            case GREATERTHAN:
                optionData = new EnumOptionData(LoanProductValueConditionType.GREATERTHAN.getValue().longValue(),
                        LoanProductValueConditionType.GREATERTHAN.getCode(), "greater than");
                break;
            default:
                optionData = new EnumOptionData(LoanProductValueConditionType.INVALID.getValue().longValue(),
                        LoanProductValueConditionType.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData loanCycleParamType(final int id) {
        return loanCycleParamType(LoanProductParamType.fromInt(id));
    }

    public static EnumOptionData loanCycleParamType(final LoanProductParamType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case PRINCIPAL:
                optionData = new EnumOptionData(LoanProductParamType.PRINCIPAL.getValue().longValue(),
                        LoanProductParamType.PRINCIPAL.getCode(), "principal");
                break;
            case INTERESTRATE:
                optionData = new EnumOptionData(LoanProductParamType.INTERESTRATE.getValue().longValue(),
                        LoanProductParamType.INTERESTRATE.getCode(), "Interest rate");
                break;
            case REPAYMENT:
                optionData = new EnumOptionData(LoanProductParamType.REPAYMENT.getValue().longValue(),
                        LoanProductParamType.REPAYMENT.getCode(), "repayment");
                break;
            default:
                optionData = new EnumOptionData(LoanProductParamType.INVALID.getValue().longValue(), LoanProductParamType.INVALID.getCode(),
                        "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData loanvariationType(final int id) {
        return loanvariationType(LoanTermVariationType.fromInt(id));
    }

    public static EnumOptionData loanvariationType(final LoanTermVariationType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case EMI_AMOUNT:
                optionData = new EnumOptionData(LoanTermVariationType.EMI_AMOUNT.getValue().longValue(),
                        LoanTermVariationType.EMI_AMOUNT.getCode(), "emiAmount");
                break;
            case INTEREST_RATE:
                optionData = new EnumOptionData(LoanTermVariationType.INTEREST_RATE.getValue().longValue(),
                        LoanTermVariationType.INTEREST_RATE.getCode(), "interestRate");
                break;
            case DELETE_INSTALLMENT:
                optionData = new EnumOptionData(LoanTermVariationType.DELETE_INSTALLMENT.getValue().longValue(),
                        LoanTermVariationType.DELETE_INSTALLMENT.getCode(), "deleteInstallment");
                break;
            case DUE_DATE:
                optionData = new EnumOptionData(LoanTermVariationType.DUE_DATE.getValue().longValue(),
                        LoanTermVariationType.DUE_DATE.getCode(), "dueDate");
                break;
            case INSERT_INSTALLMENT:
                optionData = new EnumOptionData(LoanTermVariationType.INSERT_INSTALLMENT.getValue().longValue(),
                        LoanTermVariationType.DUE_DATE.getCode(), "insertInstallment");
                break;
            case PRINCIPAL_AMOUNT:
                optionData = new EnumOptionData(LoanTermVariationType.PRINCIPAL_AMOUNT.getValue().longValue(),
                        LoanTermVariationType.PRINCIPAL_AMOUNT.getCode(), "principalAmount");
                break;
            case GRACE_ON_INTEREST:
                optionData = new EnumOptionData(LoanTermVariationType.GRACE_ON_INTEREST.getValue().longValue(),
                        LoanTermVariationType.GRACE_ON_INTEREST.getCode(), "graceOnInterest");
                break;
            case GRACE_ON_PRINCIPAL:
                optionData = new EnumOptionData(LoanTermVariationType.GRACE_ON_PRINCIPAL.getValue().longValue(),
                        LoanTermVariationType.GRACE_ON_PRINCIPAL.getCode(), "graceOnPrincipal");
                break;
            case EXTEND_REPAYMENT_PERIOD:
                optionData = new EnumOptionData(LoanTermVariationType.EXTEND_REPAYMENT_PERIOD.getValue().longValue(),
                        LoanTermVariationType.EXTEND_REPAYMENT_PERIOD.getCode(), "extendRepaymentPeriod");
                break;
            case INTEREST_RATE_FROM_INSTALLMENT:
                optionData = new EnumOptionData(LoanTermVariationType.INTEREST_RATE_FROM_INSTALLMENT.getValue().longValue(),
                        LoanTermVariationType.INTEREST_RATE_FROM_INSTALLMENT.getCode(), "interestRateForInstallment");
                break;
            default:
                optionData = new EnumOptionData(LoanTermVariationType.INVALID.getValue().longValue(),
                        LoanTermVariationType.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData interestRecalculationCompoundingType(final int id) {
        return interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.fromInt(id));
    }

    public static EnumOptionData interestRecalculationCompoundingType(final InterestRecalculationCompoundingMethod type) {
        EnumOptionData optionData = null;
        switch (type) {
            case FEE:
                optionData = new EnumOptionData(InterestRecalculationCompoundingMethod.FEE.getValue().longValue(),
                        InterestRecalculationCompoundingMethod.FEE.getCode(), "Fee");
                break;
            case INTEREST:
                optionData = new EnumOptionData(InterestRecalculationCompoundingMethod.INTEREST.getValue().longValue(),
                        InterestRecalculationCompoundingMethod.INTEREST.getCode(), "Interest");
                break;
            case INTEREST_AND_FEE:
                optionData = new EnumOptionData(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.getValue().longValue(),
                        InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.getCode(), "Fee and Interest");
                break;
            default:
                optionData = new EnumOptionData(InterestRecalculationCompoundingMethod.NONE.getValue().longValue(),
                        InterestRecalculationCompoundingMethod.NONE.getCode(), "None");
                break;
        }
        return optionData;
    }

    public static EnumOptionData interestRecalculationCompoundingNthDayType(final Integer id) {
        if (id == null) {
            return null;
        }
        return interestRecalculationCompoundingNthDayType(NthDayType.fromInt(id));
    }

    public static EnumOptionData interestRecalculationCompoundingNthDayType(final NthDayType type) {
        final String codePrefix = "interestRecalculationCompounding.";
        long nthDayValue = type.getValue().longValue();
        EnumOptionData optionData = null;
        switch (type) {
            case ONE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "first");
                break;
            case TWO:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "second");
                break;
            case THREE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "third");
                break;
            case FOUR:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "fourth");
                break;
            case FIVE:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "fifth");
                break;
            case LAST:
                optionData = new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "last");
                break;
            default:
                optionData = new EnumOptionData(Integer.valueOf(0).longValue(), codePrefix + type.getCode(), "invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData interestRecalculationCompoundingDayOfWeekType(final Integer id) {
        if (id == null) {
            return null;
        }
        return interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.fromInt(id));
    }

    public static EnumOptionData interestRecalculationCompoundingDayOfWeekType(final DayOfWeekType type) {
        final String codePrefix = "interestRecalculationCompounding.";
        EnumOptionData optionData = new EnumOptionData(type.getValue().longValue(), codePrefix + type.getCode(), type.toString());
        return optionData;
    }

    public static EnumOptionData rescheduleStrategyType(final int id) {
        return rescheduleStrategyType(LoanRescheduleStrategyMethod.fromInt(id));
    }

    public static EnumOptionData rescheduleStrategyType(final LoanRescheduleStrategyMethod type) {
        EnumOptionData optionData = null;
        switch (type) {
            case REDUCE_EMI_AMOUNT:
                optionData = new EnumOptionData(LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT.getValue().longValue(),
                        LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT.getCode(), "Reduce EMI amount");
                break;
            case REDUCE_NUMBER_OF_INSTALLMENTS:
                optionData = new EnumOptionData(LoanRescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS.getValue().longValue(),
                        LoanRescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS.getCode(), "Reduce number of installments");
                break;
            case RESCHEDULE_NEXT_REPAYMENTS:
                optionData = new EnumOptionData(LoanRescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS.getValue().longValue(),
                        LoanRescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS.getCode(), "Reschedule next repayments");
                break;
            default:
                optionData = new EnumOptionData(LoanRescheduleStrategyMethod.INVALID.getValue().longValue(),
                        LoanRescheduleStrategyMethod.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData interestRecalculationFrequencyType(final int id) {
        return interestRecalculationFrequencyType(RecalculationFrequencyType.fromInt(id));
    }

    public static EnumOptionData interestRecalculationFrequencyType(final RecalculationFrequencyType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case DAILY:
                optionData = new EnumOptionData(RecalculationFrequencyType.DAILY.getValue().longValue(),
                        RecalculationFrequencyType.DAILY.getCode(), "Daily");
                break;
            case MONTHLY:
                optionData = new EnumOptionData(RecalculationFrequencyType.MONTHLY.getValue().longValue(),
                        RecalculationFrequencyType.MONTHLY.getCode(), "Monthly");
                break;
            case SAME_AS_REPAYMENT_PERIOD:
                optionData = new EnumOptionData(RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD.getValue().longValue(),
                        RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD.getCode(), "Same as repayment period");
                break;
            case WEEKLY:
                optionData = new EnumOptionData(RecalculationFrequencyType.WEEKLY.getValue().longValue(),
                        RecalculationFrequencyType.WEEKLY.getCode(), "Weekly");
                break;
            default:
                optionData = new EnumOptionData(RecalculationFrequencyType.INVALID.getValue().longValue(),
                        RecalculationFrequencyType.INVALID.getCode(), "Invalid");
                break;

        }
        return optionData;
    }

    public static EnumOptionData preCloseInterestCalculationStrategy(final int id) {
        return preCloseInterestCalculationStrategy(LoanPreClosureInterestCalculationStrategy.fromInt(id));
    }

    public static EnumOptionData preCloseInterestCalculationStrategy(final LoanPreClosureInterestCalculationStrategy type) {
        EnumOptionData optionData = null;
        switch (type) {
            case TILL_PRE_CLOSURE_DATE:
                optionData = new EnumOptionData(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getValue().longValue(),
                        LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getCode(), "Till preclose Date");
                break;
            case TILL_REST_FREQUENCY_DATE:
                optionData = new EnumOptionData(LoanPreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE.getValue().longValue(),
                        LoanPreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE.getCode(), "Till rest Frequency Date");
                break;
            case NONE:
                break;
        }
        return optionData;
    }

    public static EnumOptionData brokenStrategy(final BrokenStrategy type) {
        EnumOptionData optionData = null;
        switch (type) {
            case NOBROKEN:
                optionData = new EnumOptionData(BrokenStrategy.NOBROKEN.getValue().longValue(),
                        BrokenStrategy.NOBROKEN.getCode(), "NoBroken");
                break;
            case DISBURSEMENT:
                optionData = new EnumOptionData(BrokenStrategy.DISBURSEMENT.getValue().longValue(),
                        BrokenStrategy.DISBURSEMENT.getCode(), "Disbursement");
                break;
            case FIRSTREPAYMENT:
                optionData = new EnumOptionData(BrokenStrategy.FIRSTREPAYMENT.getValue().longValue(),
                        BrokenStrategy.FIRSTREPAYMENT.getCode(), "FirstRepayment");
                break;
            case LASTREPAYMENT:
                optionData = new EnumOptionData(BrokenStrategy.LASTREPAYMENT.getValue().longValue(),
                        BrokenStrategy.LASTREPAYMENT.getCode(), "LastRepayment");
                break;
            case INVALID:
                break;
        }
        return optionData;
    }

    public static EnumOptionData disbursementMode(final DisbursementMode type) {
        EnumOptionData optionData = null;
        switch (type) {
            case DIRECT:
                optionData = new EnumOptionData(DisbursementMode.DIRECT.getValue().longValue(),
                        DisbursementMode.DIRECT.getCode(), "Direct");
                break;
            case ESCROW:
                optionData = new EnumOptionData(DisbursementMode.ESCROW.getValue().longValue(),
                        DisbursementMode.ESCROW.getCode(), "Escrow");
                break;
            case REIMBURSEMENT:
                optionData = new EnumOptionData(DisbursementMode.REIMBURSEMENT.getValue().longValue(),
                        DisbursementMode.REIMBURSEMENT.getCode(), "Reimbursement");                break;
            case INVALID:
                break;
        }
        return optionData;
    }

    public static EnumOptionData collection(final CollectionMode type){
        EnumOptionData optionData =null;
        switch (type) {
            case DIRECT:
                optionData = new EnumOptionData(CollectionMode.DIRECT.getValue().longValue(),
                        CollectionMode.DIRECT.getCode(), "Direct");
                break;
            case ESCROW:
                optionData = new EnumOptionData(CollectionMode.ESCROW.getValue().longValue(),
                        CollectionMode.ESCROW.getCode(), "Escrow");
                break;
            case PARTNER:
                optionData = new EnumOptionData(CollectionMode.PARTNER.getValue().longValue(),
                        CollectionMode.PARTNER.getCode(), "Partner");
                break;
            case RAZORPAY:
                optionData = new EnumOptionData(CollectionMode.RAZORPAY.getValue().longValue(),
                        CollectionMode.RAZORPAY.getCode(), "Razorpay");
                break;
            case INVALID:
                break;
        }
        return optionData;

    }

    public static EnumOptionData brokenStrategyDaysInYear(final BrokenStrategyDayInYear type){
        EnumOptionData optionData =null;
        switch (type) {
            case ACTUAL:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.ACTUAL.getValue().longValue(),
                        BrokenStrategyDayInYear.DAYS_364.getCode(), "Actual");
                break;

            case NOBROKENDAYS:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.NOBROKENDAYS.getValue().longValue(),
                        BrokenStrategyDayInYear.NOBROKENDAYS.getCode(), "NoBrokendays");
                break;
            case DAYS_360:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.DAYS_360.getValue().longValue(),
                        BrokenStrategyDayInYear.DAYS_360.getCode(), "360 Days");
                break;

            case DAYS_365:
                optionData = new EnumOptionData(BrokenStrategyDayInYear.DAYS_365.getValue().longValue(),
                        BrokenStrategyDayInYear.DAYS_365.getCode(), "365 Days");
                break;

            case INVALID:
                break;
        }
        return optionData;

    }

    public static EnumOptionData brokenStrategyDaysInMonth(final BrokenStrategyDaysInMonth type){
        EnumOptionData optionData =null;
        switch (type) {
            case DAYS_30:
                optionData = new EnumOptionData(BrokenStrategyDaysInMonth.DAYS_30.getValue().longValue(),
                        BrokenStrategyDaysInMonth.DAYS_30.getCode(), "30 Days");
                break;
            case DAYS_31:
                optionData = new EnumOptionData(BrokenStrategyDaysInMonth.DAYS_31.getValue().longValue(),
                        BrokenStrategyDaysInMonth.DAYS_31.getCode(), "31 Days");
                break;
            case ACTUAL:
                optionData = new EnumOptionData(BrokenStrategyDaysInMonth.ACTUAL.getValue().longValue(),
                        BrokenStrategyDaysInMonth.ACTUAL.getCode(), "Actual");
                break;
            case INVALID:
                break;
        }
        return optionData;

    }

    public static EnumOptionData transactionTypePreference(final TransactionTypePreference type){
        EnumOptionData optionData =null;
        switch (type) {
            case IMPS:
                optionData = new EnumOptionData(TransactionTypePreference.IMPS.getValue().longValue(),
                        TransactionTypePreference.IMPS.getCode(), "IMPS");
                break;
            case RTGS:
                optionData = new EnumOptionData(TransactionTypePreference.RTGS.getValue().longValue(),
                        TransactionTypePreference.RTGS.getCode(), "RTGS");
                break;
            case NEFT:
                optionData = new EnumOptionData(TransactionTypePreference.NEFT.getValue().longValue(),
                        TransactionTypePreference.NEFT.getCode(), "NEFT");
                break;
            case INVALID:
                break;
        }
        return optionData;

    }

    public static EnumOptionData bulkReportEnum(final BulkReportDataEnum type){
        EnumOptionData optionData =null;
        switch (type) {
            case BULKCIENTLOANSCREATION:
                optionData = new EnumOptionData(BulkReportDataEnum.BULKCIENTLOANSCREATION.getValue().longValue(),
                        BulkReportDataEnum.BULKCIENTLOANSCREATION.getCode(), "Bulk Loan Creation");
                break;
            case  BULKLOANSREPAYMENT:
                optionData = new EnumOptionData(BulkReportDataEnum.BULKLOANSREPAYMENT.getValue().longValue(),
                        BulkReportDataEnum.BULKLOANSREPAYMENT.getCode(), "Bulk Loan Repayment");
                break;

            case  BULKCHARGEREPAYMENT:
                optionData = new EnumOptionData(BulkReportDataEnum.BULKCHARGEREPAYMENT.getValue().longValue(),
                        BulkReportDataEnum.BULKCHARGEREPAYMENT.getCode(), "Bulk Charge Repayment");
                break;


        }
        return optionData;

    }
    public static EnumOptionData multiplesOf(final MultiplesOf type){
        EnumOptionData optionData =null;
        switch (type) {
            case ONE:
                optionData = new EnumOptionData(MultiplesOf.ONE.getValue().longValue(),
                        MultiplesOf.ONE.getCode(), "1");
                break;
            case  TEN:
                optionData = new EnumOptionData(MultiplesOf.TEN.getValue().longValue(),
                        MultiplesOf.TEN.getCode(), "10");
                break;
            case  HUNDRED:
                optionData = new EnumOptionData(MultiplesOf.HUNDRED.getValue().longValue(),
                        MultiplesOf.HUNDRED.getCode(), "100");
                break;
        }
        return optionData;

    }

    public static EnumOptionData dedupeEnum(final Dedupe dedupe){
        EnumOptionData optionData =null;
        switch (dedupe) {
            case PAN:
                optionData = new EnumOptionData(Dedupe.PAN.getValue().longValue(),
                        Dedupe.PAN.getCode(), "PAN");
                break;
            case AADHAAR:
                optionData = new EnumOptionData(Dedupe.AADHAAR.getValue().longValue(),
                        Dedupe.AADHAAR.getCode(), "AADHAAR");
                break;
        }
        return optionData;

    }
    public  static  EnumOptionData advanceAppropriationEnum (final AdvanceAppropriationOn advanceAppropriationOn){

        EnumOptionData optionData = null;
        switch (advanceAppropriationOn){

            case RECEIPT_DATE ->  optionData= new EnumOptionData(AdvanceAppropriationOn.RECEIPT_DATE.getValue().longValue()
                    , AdvanceAppropriationOn.RECEIPT_DATE.getCode(),"Receipt Date");

            case ON_DUE_DATE -> optionData= new EnumOptionData(AdvanceAppropriationOn.ON_DUE_DATE.getValue().longValue()
                    , AdvanceAppropriationOn.ON_DUE_DATE.getCode(),"Due Date");
            case INVALID -> optionData= new EnumOptionData(AdvanceAppropriationOn.INVALID.getValue().longValue()
                    , AdvanceAppropriationOn.INVALID.getCode(),"Invalid");
        }
        return optionData;
    }
    public  static  EnumOptionData foreclosurePosEnum (final ForeclosurePos foreclosurePos){

        EnumOptionData optionData = null;
        switch (foreclosurePos){

            case RS_POS ->  optionData= new EnumOptionData(ForeclosurePos.RS_POS.getValue().longValue()
                    ,ForeclosurePos.RS_POS.getCode(),"RS POS");

            case REVISED_POS -> optionData= new EnumOptionData(ForeclosurePos.REVISED_POS.getValue().longValue()
                    ,ForeclosurePos.REVISED_POS.getCode(),"Revised POS");
        }
        return optionData;
    }

    public static  EnumOptionData retrieveServiceFeeSplitMethod(final ServicerFeeChargesRatio servicerFeeChargesRatio){

        EnumOptionData optionData = null;

        switch (servicerFeeChargesRatio){
            case FIXED_SPLIT ->  optionData = new EnumOptionData(ServicerFeeChargesRatio.FIXED_SPLIT.getValue().longValue(),
                    ServicerFeeChargesRatio.FIXED_SPLIT.getCode(),"Fixed Split");
            case  DYNAMIC_SPLIT ->  optionData = new EnumOptionData(ServicerFeeChargesRatio.DYNAMIC_SPLIT.getValue().longValue(),
                    ServicerFeeChargesRatio.DYNAMIC_SPLIT.getCode(), "Dynamic Split");
        }

        return optionData;

    }
	
	public static EnumOptionData advanceAppropriationAgainstOn (AdvanceAppropriationAgainstOn advanceAppropriationAgainstOn){

        EnumOptionData optionData = null;
        switch (advanceAppropriationAgainstOn){

            case PRINCIPAL ->  optionData = new EnumOptionData(AdvanceAppropriationAgainstOn.PRINCIPAL.getValue().longValue(),
                    AdvanceAppropriationAgainstOn.PRINCIPAL.getCode(),"Principal");

            case  INTEREST_PRINCIPAL ->  optionData = new EnumOptionData(AdvanceAppropriationAgainstOn.INTEREST_PRINCIPAL.getValue().longValue(),
                    AdvanceAppropriationAgainstOn.INTEREST_PRINCIPAL.getCode() ,"Interest Principal");

            case INVALID ->  optionData =  new EnumOptionData(AdvanceAppropriationAgainstOn.INVALID.getValue().longValue(),
                    AdvanceAppropriationAgainstOn.INVALID.getCode(),"Invalid");
        }
        return optionData;
    }

    public static EnumOptionData pmtCalcEnum(PmtCalcEnum emiCalcEnum){

        EnumOptionData optionData = null;
        switch (emiCalcEnum){

            case PMT_WITH_MONTHLY_INTEREST_RATE->  optionData = new EnumOptionData(PmtCalcEnum.PMT_WITH_MONTHLY_INTEREST_RATE.getValue().longValue(),
                    PmtCalcEnum.PMT_WITH_MONTHLY_INTEREST_RATE.getCode(),"PMT WITH MONTHLY INTEREST RATE");

            case  PMT_WITH_YEARLY_INTEREST_RATE ->  optionData = new EnumOptionData(PmtCalcEnum.PMT_WITH_YEARLY_INTEREST_RATE.getValue().longValue(),
                    PmtCalcEnum.PMT_WITH_YEARLY_INTEREST_RATE.getCode() ,"PMT WITH YEARLY INTEREST RATE");

            case INVALID ->  optionData =  new EnumOptionData(PmtCalcEnum.INVALID.getValue().longValue(),
                    PmtCalcEnum.INVALID.getCode(),"Invalid");
        }
        return optionData;

    }
    public  static  EnumOptionData foreclosureMethodTypeEnums (final ForeclosureMethodTypes foreclosurePos){

        EnumOptionData optionData = null;
        switch (foreclosurePos){

            case PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING ->  optionData= new EnumOptionData(ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING.getValue().longValue()
                    ,ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING.getCode(),"Foreclosure with Principal Outstanding and Interest Outstanding");

            case PRINCIPAL_OUTSTANDING_INTEREST_DUE -> optionData= new EnumOptionData(ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_DUE.getValue().longValue()
                    ,ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_DUE.getCode(),"Foreclosure with Principal Outstanding and Interest Due");
            case PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED -> optionData= new EnumOptionData(ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED.getValue().longValue()
                    ,ForeclosureMethodTypes.PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED.getCode(),"Foreclosure with Principal Outstanding and Interest Accrued");
        }
        return optionData;
    }
	public static EnumOptionData coolingOffInterestAndChargeApplicability(CoolingOffInterestAndChargeApplicability coolingOffInterestAndChargeApplicability) {

        EnumOptionData optionData = null;
        switch (coolingOffInterestAndChargeApplicability) {

            case NO_INTEREST -> optionData = new EnumOptionData(CoolingOffInterestAndChargeApplicability.NO_INTEREST.getValue().longValue()
                    , CoolingOffInterestAndChargeApplicability.NO_INTEREST.getCode(), "NO INTEREST");

            case ONLY_INTEREST -> optionData = new EnumOptionData(CoolingOffInterestAndChargeApplicability.ONLY_INTEREST.getValue().longValue()
                    , CoolingOffInterestAndChargeApplicability.ONLY_INTEREST.getCode(), "ONLY INTEREST");

            case INTEREST_AND_CHARGES -> optionData = new EnumOptionData(CoolingOffInterestAndChargeApplicability.INTEREST_AND_CHARGES.getValue().longValue()
                    , CoolingOffInterestAndChargeApplicability.INTEREST_AND_CHARGES.getCode(), "INTEREST AND CHARGES");

            case ONLY_CHARGES -> optionData = new EnumOptionData(CoolingOffInterestAndChargeApplicability.ONLY_CHARGES.getValue().longValue()
                    , CoolingOffInterestAndChargeApplicability.ONLY_CHARGES.getCode(), "ONLY CHARGES");

            case INVALID ->  optionData =  new EnumOptionData(CoolingOffInterestAndChargeApplicability.INVALID.getValue().longValue(),
                    CoolingOffInterestAndChargeApplicability.INVALID.getCode(),"Invalid");


        }
        return optionData;

    }
	 public static EnumOptionData coolingOffInterestLogicApplicability(CoolingOffInterestLogicApplicability coolingOffInterestLogicApplicability) {

        EnumOptionData optionData = null;
        switch (coolingOffInterestLogicApplicability) {

            case PNR -> optionData = new EnumOptionData(CoolingOffInterestLogicApplicability.PNR.getValue().longValue()
                    , CoolingOffInterestLogicApplicability.PNR.getCode(), "PNR");

            case MAX -> optionData = new EnumOptionData(CoolingOffInterestLogicApplicability.MAX.getValue().longValue()
                    , CoolingOffInterestLogicApplicability.MAX.getCode(), "MAX");

            case INVALID ->  optionData =  new EnumOptionData(CoolingOffInterestLogicApplicability.INVALID.getValue().longValue(),
                    CoolingOffInterestLogicApplicability.INVALID.getCode(),"Invalid");


        }
        return optionData;

    }
}
