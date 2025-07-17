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
package org.vcpl.lms.portfolio.collectionsheet.data;

import java.math.BigDecimal;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for representing loan with dues (example: loan is due for disbursement, repayments).
 */
public class LoanDueData {

    private final Long loanId;
    private final String accountId;
    private final Integer accountStatusId;
    private final String productShortName;
    private final Long productId;
    private final CurrencyData currency;
    private BigDecimal disbursementAmount = BigDecimal.ZERO;
    private BigDecimal principalDue = BigDecimal.ZERO;

    private BigDecimal selfPrincipal = BigDecimal.ZERO;
    private BigDecimal partnerPrincipal = BigDecimal.ZERO;
    private BigDecimal selfInterestCharged = BigDecimal.ZERO;
    private BigDecimal partnerInterestCharged = BigDecimal.ZERO;
    private BigDecimal selfDue = BigDecimal.ZERO;
    private BigDecimal partnerDue = BigDecimal.ZERO;


    private BigDecimal interestDue = BigDecimal.ZERO;

    private BigDecimal principalPaid = BigDecimal.ZERO;
    private BigDecimal interestPaid = BigDecimal.ZERO;
    private BigDecimal chargesDue = BigDecimal.ZERO;
    private BigDecimal totalDue = BigDecimal.ZERO;
    private BigDecimal feeDue = BigDecimal.ZERO;
    private BigDecimal feePaid = BigDecimal.ZERO;

    public LoanDueData(final Long loanId, final String accountId, final Integer accountStatusId, final String productShortName,
                       final Long productId, final CurrencyData currency, final BigDecimal disbursementAmount, final BigDecimal principalDue,
                       final BigDecimal principalPaid, final BigDecimal interestDue, final BigDecimal interestPaid, final BigDecimal chargesDue,
                       final BigDecimal feeDue, final BigDecimal feePaid,final BigDecimal selfPrincipal,final BigDecimal partnerPrincipal,final BigDecimal selfInterestCharged,final BigDecimal partnerInterestCharged,final BigDecimal selfDue,final BigDecimal partnerDue) {
        this.loanId = loanId;
        this.accountId = accountId;
        this.accountStatusId = accountStatusId;
        this.productShortName = productShortName;
        this.productId = productId;
        this.currency = currency;
        this.disbursementAmount = disbursementAmount;
        this.principalDue = principalDue;
        this.principalPaid = principalPaid;
        this.interestDue = interestDue;
        this.interestPaid = interestPaid;
        this.chargesDue = chargesDue;
        this.feeDue = feeDue;
        this.feePaid = feePaid;
        this.totalDue = this.totalDue.add(principalDue).add(interestDue).add(feeDue);
        this.selfPrincipal = selfPrincipal;
        this.partnerPrincipal = partnerPrincipal;
        this.selfInterestCharged = selfInterestCharged;
        this.partnerInterestCharged = partnerInterestCharged;
        this.selfDue = partnerDue;
        this.partnerDue = partnerDue;



    }

    public Long getLoanId() {
        return this.loanId;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public Integer getAccountStatusId() {
        return this.accountStatusId;
    }

    public String getProductShortName() {
        return this.productShortName;
    }

    public Long getProductId() {
        return this.productId;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public BigDecimal getDisbursementAmount() {
        return this.disbursementAmount;
    }

    public BigDecimal getPrincipalDue() {
        return this.principalDue;
    }

    public BigDecimal getPrincipalPaid() {
        return this.principalPaid;
    }

    public BigDecimal getInterestDue() {
        return this.interestDue;
    }

    public BigDecimal getInterestPaid() {
        return this.interestPaid;
    }

    public BigDecimal getChargesDue() {
        return this.chargesDue;
    }

    public BigDecimal getFeeDue() {
        return this.feeDue;
    }

    public BigDecimal getFeePaid() {
        return this.feePaid;
    }

    public BigDecimal getSelfPrincipal() {
        return this.selfPrincipal;
    }

    public BigDecimal getPartnerPrincipal() {
        return this.partnerPrincipal;
    }

    public BigDecimal getSelfInterestCharged() {
        return this.selfInterestCharged;
    }

    public BigDecimal getPartnerInterestCharged() {
        return this.partnerInterestCharged;
    }

    public BigDecimal getSelfDue() {
        return this.selfDue;
    }

    public BigDecimal getPartnerDue() {
        return this.partnerDue;
    }



}
