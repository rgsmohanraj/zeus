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
package org.vcpl.lms.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

/**
 * Domain representation of a Loan Schedule (not used for persistence)
 */
public final class LoanScheduleModel {

    private final Collection<LoanScheduleModelPeriod> periods;
    private final ApplicationCurrency applicationCurrency;
    private final int loanTermInDays;
    private final Money totalPrincipalDisbursed;
    private final BigDecimal totalPrincipalExpected;
    private final BigDecimal totalSelfPrincipalExpected;
    private final BigDecimal totalPartnerPrincipalExpected;

    private final BigDecimal totalPrincipalPaid;
    private final BigDecimal totalInterestCharged;
    private final BigDecimal totalSelfInterestCharged;
    private final BigDecimal totalPartnerInterestCharged;
    private final BigDecimal totalSelfDue;
    private final BigDecimal totalPartnerDue;


    private final BigDecimal totalFeeChargesCharged;
    private final BigDecimal totalSelfFeeChargesCharged;
    private final BigDecimal totalPartnerFeeChargesCharged;

    private final BigDecimal totalPenaltyChargesCharged;
    private final BigDecimal totalRepaymentExpected;
    private final BigDecimal totalSelfRepaymentExpected;
    private final BigDecimal totalPartnerRepaymentExpected;

    private final BigDecimal totalOutstanding;
    private final BigDecimal totalSelfOutstanding;
    private final BigDecimal totalPartnerOutstanding;

    private final LoanProduct loanProduct;


    public static LoanScheduleModel from(final Collection<LoanScheduleModelPeriod> periods, final ApplicationCurrency applicationCurrency,
                                         final int loanTermInDays, final Money principalDisbursed, final BigDecimal totalPrincipalExpected,
                                         final BigDecimal totalPrincipalPaid, final BigDecimal totalInterestCharged, final BigDecimal totalFeeChargesCharged,
                                         final BigDecimal totalPenaltyChargesCharged, final BigDecimal totalRepaymentExpected, final BigDecimal totalOutstanding,final LoanProduct loanProduct,final BigDecimal totalSelfRepaymentExpected,final BigDecimal totalPartnerRepaymentExpected,
                                         final BigDecimal totalSelfPrincipalExpected,final BigDecimal totalPartnerPrincipalExpected, final BigDecimal totalSelfInterestCharged, final BigDecimal totalPartnerInterestCharged,final BigDecimal totalSelfDue,final BigDecimal totalPartnerDue
										 ,final BigDecimal totalSelfOutstanding,final BigDecimal totalPartnerOutstanding,final BigDecimal totalSelfFeeChargesCharged,final BigDecimal totalPartnerFeeChargesCharged) {

        return new LoanScheduleModel(periods, applicationCurrency, loanTermInDays, principalDisbursed, totalPrincipalExpected,
                totalPrincipalPaid, totalInterestCharged, totalFeeChargesCharged, totalPenaltyChargesCharged, totalRepaymentExpected,
                totalOutstanding,loanProduct,totalSelfRepaymentExpected,totalPartnerRepaymentExpected,totalSelfPrincipalExpected,totalPartnerPrincipalExpected,totalSelfInterestCharged,totalPartnerInterestCharged,totalSelfDue,totalPartnerDue
				,totalSelfOutstanding,totalPartnerOutstanding,totalSelfFeeChargesCharged,totalPartnerFeeChargesCharged);
    }

    public static LoanScheduleModel withOverdueChargeUpdation(final Collection<LoanScheduleModelPeriod> periods,
                                                              final LoanScheduleModel loanScheduleModel, final BigDecimal totalPenaltyChargesCharged) {

        return new LoanScheduleModel(periods, loanScheduleModel.applicationCurrency, loanScheduleModel.loanTermInDays,
                loanScheduleModel.totalPrincipalDisbursed, loanScheduleModel.totalPrincipalExpected, loanScheduleModel.totalPrincipalPaid,
                loanScheduleModel.totalInterestCharged, loanScheduleModel.totalFeeChargesCharged, totalPenaltyChargesCharged,
                loanScheduleModel.totalRepaymentExpected, loanScheduleModel.totalOutstanding,loanScheduleModel.loanProduct,
                loanScheduleModel.totalSelfRepaymentExpected,loanScheduleModel.totalPartnerRepaymentExpected,
                loanScheduleModel.totalSelfPrincipalExpected,loanScheduleModel.totalPartnerPrincipalExpected,
                loanScheduleModel.totalSelfInterestCharged,loanScheduleModel.totalPartnerInterestCharged,
                loanScheduleModel.totalSelfDue,loanScheduleModel.totalPartnerDue,loanScheduleModel.totalSelfOutstanding,
                loanScheduleModel.totalPartnerOutstanding,loanScheduleModel.totalSelfFeeChargesCharged,loanScheduleModel.totalPartnerFeeChargesCharged);
    }

    public static LoanScheduleModel withLoanScheduleModelPeriods(final Collection<LoanScheduleModelPeriod> periods,
                                                                 final LoanScheduleModel loanScheduleModel) {

        return new LoanScheduleModel(periods, loanScheduleModel.applicationCurrency, loanScheduleModel.loanTermInDays,
                loanScheduleModel.totalPrincipalDisbursed, loanScheduleModel.totalPrincipalExpected, loanScheduleModel.totalPrincipalPaid,
                loanScheduleModel.totalInterestCharged, loanScheduleModel.totalFeeChargesCharged,
                loanScheduleModel.totalPenaltyChargesCharged, loanScheduleModel.totalRepaymentExpected, loanScheduleModel.totalOutstanding,loanScheduleModel.loanProduct,loanScheduleModel.totalSelfRepaymentExpected,loanScheduleModel.totalPartnerRepaymentExpected,
                loanScheduleModel.totalSelfPrincipalExpected,loanScheduleModel.totalPartnerPrincipalExpected,loanScheduleModel.totalSelfInterestCharged,loanScheduleModel.totalPartnerInterestCharged,loanScheduleModel.totalSelfDue,loanScheduleModel.totalPartnerDue
				,loanScheduleModel.totalSelfOutstanding,loanScheduleModel.totalPartnerOutstanding,loanScheduleModel.totalSelfFeeChargesCharged,loanScheduleModel.totalPartnerFeeChargesCharged);
    }

    private LoanScheduleModel(final Collection<LoanScheduleModelPeriod> periods, final ApplicationCurrency applicationCurrency,
                              final int loanTermInDays, final Money principalDisbursed, final BigDecimal totalPrincipalExpected,
                              final BigDecimal totalPrincipalPaid, final BigDecimal totalInterestCharged, final BigDecimal totalFeeChargesCharged,
                              final BigDecimal totalPenaltyChargesCharged, final BigDecimal totalRepaymentExpected, final BigDecimal totalOutstanding,final LoanProduct loanProduct,final BigDecimal totalSelfRepaymentExpected,final BigDecimal totalPartnerRepaymentExpected,
                              final BigDecimal totalSelfPrincipalExpected,final BigDecimal totalPartnerPrincipalExpected,
                              final BigDecimal totalSelfInterestCharged,final BigDecimal totalPartnerInterestCharged,
                              final BigDecimal totalSelfDue,final BigDecimal totalPartnerDue,final BigDecimal totalSelfOutstanding,
                              final BigDecimal totalPartnerOutstanding,final BigDecimal totalSelfFeeChargesCharged , final BigDecimal totalPartnerFeeChargesCharged) {
        this.periods = periods;
        this.applicationCurrency = applicationCurrency;
        this.loanTermInDays = loanTermInDays;
        this.totalPrincipalDisbursed = principalDisbursed;
        this.totalPrincipalExpected = totalPrincipalExpected;
        this.totalSelfPrincipalExpected = totalSelfPrincipalExpected;
        this.totalPartnerPrincipalExpected = totalPartnerPrincipalExpected;

        this.totalPrincipalPaid = totalPrincipalPaid;
        this.totalInterestCharged = totalInterestCharged;
        this.totalSelfInterestCharged = totalSelfInterestCharged;
        this.totalPartnerInterestCharged = totalPartnerInterestCharged;
        this.totalSelfDue = totalSelfDue;
        this.totalPartnerDue = totalPartnerDue;

        this.totalFeeChargesCharged = totalFeeChargesCharged;
        this.totalSelfFeeChargesCharged = totalSelfFeeChargesCharged;
        this.totalPartnerFeeChargesCharged = totalPartnerFeeChargesCharged;
        this.totalPenaltyChargesCharged = totalPenaltyChargesCharged;
        this.totalRepaymentExpected = totalRepaymentExpected;
        this.totalSelfRepaymentExpected = totalSelfRepaymentExpected;
        this.totalPartnerRepaymentExpected = totalPartnerRepaymentExpected;

        this.totalOutstanding = totalOutstanding;
        this.totalSelfOutstanding=totalSelfOutstanding;
        this.totalPartnerOutstanding=totalPartnerOutstanding;


        this.loanProduct=loanProduct;

    }

    public LoanScheduleData toData() {

        final int decimalPlaces = this.totalPrincipalDisbursed.getCurrencyDigitsAfterDecimal();
        final Integer inMultiplesOf = this.totalPrincipalDisbursed.getCurrencyInMultiplesOf();
        final CurrencyData currency = this.applicationCurrency.toData(decimalPlaces, inMultiplesOf);

        final Collection<LoanSchedulePeriodData> periodsData = new ArrayList<>();
        for (final LoanScheduleModelPeriod modelPeriod : this.periods) {
            periodsData.add(modelPeriod.toData());
        }

        final BigDecimal totalWaived = null;
        final BigDecimal totalWrittenOff = null;
        final BigDecimal totalRepayment = null;
        final BigDecimal totalPaidInAdvance = null;
       // final BigDecimal selfTotalPaidInAdvance = null;
       // final BigDecimal partnerTotalPaidInAdvance = null;

        final BigDecimal totalPaidLate = null;
        final BigDecimal selfTotalPaidLate = null;
        final BigDecimal partnerTotalPaidLate = null;

        final BigDecimal totalSelfRepayment = null;
        final BigDecimal totalPartnerRepayment = null;

        return new LoanScheduleData(currency, periodsData, this.loanTermInDays, this.totalPrincipalDisbursed.getAmount(),
                this.totalPrincipalExpected, this.totalPrincipalPaid, this.totalInterestCharged, this.totalFeeChargesCharged,
                this.totalPenaltyChargesCharged, totalWaived, totalWrittenOff, this.totalRepaymentExpected, totalRepayment,
                totalPaidInAdvance, totalPaidLate, this.totalOutstanding,this.totalSelfRepaymentExpected,this.totalPartnerRepaymentExpected,this.totalSelfPrincipalExpected,this.totalPartnerPrincipalExpected,
                this.totalSelfInterestCharged,this.totalPartnerInterestCharged,this.totalSelfDue,this.totalPartnerDue,totalSelfRepayment,
                totalPartnerRepayment,selfTotalPaidLate,partnerTotalPaidLate,
                this.totalSelfOutstanding,this.totalPartnerOutstanding,this.totalSelfFeeChargesCharged,this.totalPartnerFeeChargesCharged);
    }

    public Collection<LoanScheduleModelPeriod> getPeriods() {
        return this.periods;
    }

    public BigDecimal getTotalPenaltyChargesCharged() {
        return this.totalPenaltyChargesCharged;
    }

    public BigDecimal getTotalInterestCharged() {
        return this.totalInterestCharged;
    }
    public BigDecimal getTotalSelfInterestCharged() {
        return this.totalSelfInterestCharged;
    }
    public BigDecimal getTotalPartnerInterestCharged() {
        return this.totalPartnerInterestCharged;
    }
    public BigDecimal getTotalSelfDue() {
        return this.totalSelfDue;
    }
    public BigDecimal getTotalPartnerDue() {
        return this.totalPartnerDue;
    }










}
