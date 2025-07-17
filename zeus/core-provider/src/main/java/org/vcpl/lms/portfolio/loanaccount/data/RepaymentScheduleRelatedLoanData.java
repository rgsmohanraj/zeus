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
package org.vcpl.lms.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object representing repayment schedule related information.
 */
public class RepaymentScheduleRelatedLoanData {

    private final LocalDate expectedDisbursementDate;
    private final LocalDate actualDisbursementDate;
    private final CurrencyData currency;
    private final BigDecimal principal;
    private final BigDecimal netDisbursalAmount;
    private final BigDecimal inArrearsTolerance;
    private final BigDecimal totalFeeChargesAtDisbursement;

    private final BigDecimal selfPrincipalAmount;
    private final BigDecimal partnerPrincipalAmount;
    private final BigDecimal totalSelfFeeChargesAtDisbursement;
    private final BigDecimal totalPartnerFeeChargesAtDisbursement;




    public RepaymentScheduleRelatedLoanData(final LocalDate expectedDisbursementDate, final LocalDate actualDisbursementDate,
            final CurrencyData currency, final BigDecimal principal, final BigDecimal inArrearsTolerance,
            final BigDecimal totalFeeChargesAtDisbursement,final BigDecimal selfPrincipalAmount,final BigDecimal partnerPrincipalAmount,final BigDecimal totalSelfFeeChargesAtDisbursement,final BigDecimal totalPartnerFeeChargesAtDisbursement) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.actualDisbursementDate = actualDisbursementDate;
        this.currency = currency;
        this.principal = principal;
        this.inArrearsTolerance = inArrearsTolerance;
        this.totalFeeChargesAtDisbursement = totalFeeChargesAtDisbursement;
        this.netDisbursalAmount = this.principal.subtract(this.totalFeeChargesAtDisbursement);
        this.totalSelfFeeChargesAtDisbursement = totalSelfFeeChargesAtDisbursement;
        this.totalPartnerFeeChargesAtDisbursement =totalPartnerFeeChargesAtDisbursement;
        this.selfPrincipalAmount=selfPrincipalAmount;
        this.partnerPrincipalAmount=partnerPrincipalAmount;

    }

    public LocalDate disbursementDate() {
        LocalDate disbursementDate = this.expectedDisbursementDate;
        if (this.actualDisbursementDate != null) {
            disbursementDate = this.actualDisbursementDate;
        }
        return disbursementDate;
    }

    public BigDecimal amount() {
        return this.principal;
    }

    public boolean isDisbursed() {
        return this.actualDisbursementDate != null;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public BigDecimal getInArrearsTolerance() {
        return this.inArrearsTolerance;
    }

    public BigDecimal getTotalFeeChargesAtDisbursement() {
        return this.totalFeeChargesAtDisbursement;
    }
    public BigDecimal getTotalSelfFeeChargesAtDisbursement() {
        return this.totalSelfFeeChargesAtDisbursement;
    }

    public BigDecimal getTotalPartnerFeeChargesAtDisbursement() {
        return this.totalPartnerFeeChargesAtDisbursement;
    }




    public DisbursementData disbursementData() {
        BigDecimal waivedChargeAmount = null;
        return new DisbursementData(null, this.expectedDisbursementDate, this.actualDisbursementDate, this.principal,
                this.netDisbursalAmount, null, null, waivedChargeAmount,selfPrincipalAmount,partnerPrincipalAmount,null,null);
    }
}
