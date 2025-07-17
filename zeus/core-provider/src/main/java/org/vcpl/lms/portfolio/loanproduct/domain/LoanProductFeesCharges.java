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
package org.vcpl.lms.portfolio.loanproduct.domain;

import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductFeeData;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "m_product_loan_fees_charges")
public class LoanProductFeesCharges extends AbstractPersistableCustom {


    @OneToOne
    @JoinColumn(name = "product_loan_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne
    @JoinColumn(name = "charge_id", nullable = false)
    private Charge charge;

    @Column(name = "self_share",nullable = true)
    private BigDecimal selfShare;

    @Column(name = "partner_share",nullable = true)
    private BigDecimal partnerShare;

    @Column(name = "charge_type",nullable = true)
    private String chargeType;


    public LoanProductFeesCharges() {
    }

    public LoanProductFeesCharges(LoanProduct loanProduct,Charge charge,BigDecimal selfShare,BigDecimal partnerShare,String type) {
        this.loanProduct=loanProduct;
        this.charge=charge;
        this.selfShare=selfShare;
        this.partnerShare=partnerShare;
        this.chargeType=type; }

    public Charge getCharge() {
        return charge;
    }

    public String getChargeType() {
        return chargeType;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

    public void setLoanProduct(LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public BigDecimal getSelfShare() {
        return selfShare;
    }

    public void setSelfShare(BigDecimal self_share) {
        this.selfShare = self_share;
    }

    public BigDecimal getPartnerShare() {
        return partnerShare;
    }

    public void setPartnerShare(BigDecimal partner_share) {
        this.partnerShare = partner_share;
    }

    public Set<LoanProductFeesCharges> loanCharges(LoanProduct loanProduct,List<LoanProductFeeData> loanProductChargeData) {

        Set<LoanProductFeesCharges> loanProductFeesCharge = new HashSet<>();

        if(loanProductChargeData.isEmpty()){
            return null;
        }else {
            for (final LoanProductFeeData datas : loanProductChargeData) {
                final Charge charge = datas.getCharge();

                final BigDecimal selfShare = datas.getSelfShare();
                final BigDecimal partnerShare = datas.getPartnerShare();
                final String type = datas.getChargeType();
                LoanProductFeesCharges LoanProductFeesCharge = new LoanProductFeesCharges(loanProduct, charge, selfShare, partnerShare, type);
                loanProductFeesCharge.add(LoanProductFeesCharge);
            }
            return loanProductFeesCharge;
        }
    }}

