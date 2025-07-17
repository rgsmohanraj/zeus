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
package org.apache.fineract.portfolio.loanproduct.domain;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;
import java.util.Map;

@Entity
@Table(name = "m_colending_charge")
public class LoanProductCharges extends AbstractPersistableCustom {
    @ManyToOne
    @JoinColumn(name = "product_loan_id", nullable = true)
    private LoanProduct loanProduct;

    @Column(name = "colending_charge", nullable = true)
    private Integer colendingCharge;

    @Column(name = "self_charge", nullable = true)
    private Integer selfCharge;

    @Column(name = "partner_charge", nullable = true)
    private Integer partnerCharge;
//
//    public static LoanProductCharges FromJson(final LoanProduct loanProduct,final JsonCommand command) {
//        final Integer colending = Integer.valueOf("colendingCharge");
//        final Integer colendingCharge = command.integerValueOfParameterNamed(colending);
//
//        final Integer self = Integer.valueOf("selfCharge");
//        final Integer selfCharge = command.integerValueOfParameterNamed(self);
//
//        final Integer partner = Integer.valueOf("partnerCharge");
//        final Integer partnerCharge = command.integerValueOfParameterNamed(partner);
//
//        return new LoanProductCharges(colendingCharge, selfCharge,partnerCharge);
//    }


    protected LoanProductCharges() {

    }

    public LoanProductCharges(final Integer colendingCharge, final Integer selfCharge, final Integer partnerCharge) {
        this.colendingCharge = colendingCharge;
        this.selfCharge = selfCharge;
        this.partnerCharge = partnerCharge;
    }

    public void updateLoanProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.COLENDING_CHARGE_PARAMETER_NAME, this.colendingCharge)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.COLENDING_CHARGE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.COLENDING_CHARGE_PARAMETER_NAME, newValue);
            this.colendingCharge = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.SELF_CHARGE_PARAMETER_NAME, this.selfCharge)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.SELF_CHARGE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.SELF_CHARGE_PARAMETER_NAME, newValue);
            this.selfCharge = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.PARTNER_CHARGE_PARAMETER_NAME, this.partnerCharge)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.PARTNER_CHARGE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.PARTNER_CHARGE_PARAMETER_NAME, newValue);
            this.partnerCharge = newValue;
        } else {
            this.colendingCharge = null;
            this.selfCharge = null;
            this.partnerCharge = null;
        }
            }
            public Integer colendingCharge() {
            return this.colendingCharge;
        }

        public Integer selfCharge() {
            return this.selfCharge;
        }

        public Integer partnerCharge() {
            return this.partnerCharge;
        }
}



//    public Map<Integer, Object> update(final JsonCommand command) {
//
//        final Map<Integer, Object> actualChanges = new LinkedHashMap<>(7);
//
//                final String colendingChargeParamName = "colendingCharge";
//        if (command.isChangeInIntegerParameterNamed(colendingChargeParamName, this.colendingCharge)) {
//            final Integer colendingChargeNewValue = command.integerValueOfParameterNamed(colendingChargeParamName);
//            actualChanges.put(colendingChargeParamName, colendingChargeNewValue);
//            this.colendingCharge = colendingChargeNewValue;
//        }
//
//        final String selfChargeParamName = "selfCharge";
//        if (command.isChangeInIntegerParameterNamed(colendingChargeParamName, this.selfCharge)) {
//            final Integer selfChargeNewValue = command.integerValueOfParameterNamed(colendingChargeParamName);
//            actualChanges.put(selfChargeParamName, selfChargeNewValue);
//            this.selfCharge = selfChargeNewValue;
//        }
//
//        final String partnerChargeParamName = "partnerCharge";
//        if (command.isChangeInIntegerParameterNamed(partnerChargeParamName, this.partnerCharge)) {
//            final Integer partnerChargeNewValue = command.integerValueOfParameterNamed(partnerChargeParamName);
//            actualChanges.put(partnerChargeParamName, partnerChargeNewValue);
//            this.partnerCharge = partnerChargeNewValue;
//        }
//        return actualChanges;
//    }

