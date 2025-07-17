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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.portfolio.loanproduct.data.TransactionProcessingStrategyData;

@Entity
@Getter
@Table(name = "ref_loan_transaction_processing_strategy")
public class LoanTransactionProcessingStrategy extends AbstractPersistableCustom {

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder; // Don't change this name as this property name
                               // is used as sort order while retrieving this
                               // objects

    protected LoanTransactionProcessingStrategy() {
        //
    }

    public TransactionProcessingStrategyData toData() {
        return new TransactionProcessingStrategyData(getId(), this.code, this.name);
    }

    public boolean isStandardStrategy() {
        return "zeus-standard-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isHeavensfamilyStrategy() {
        return "heavensfamily-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isEarlyPaymentStrategy() {
        return "early-repayment-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isCreocoreStrategy() {
        return "creocore-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isIndianRBIStrategy() {
        return "rbi-india-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isPrincipalInterestPenaltiesFeesOrderStrategy() {
        return "principal-interest-penalties-fees-order-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isInterestPrincipalPenaltiesFeesOrderStrategy() {
        return "interest-principal-penalties-fees-order-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isVerticalStrategy() {
        return "vertical-interest-principal-order".equalsIgnoreCase(this.code);
    }
    public boolean isHorizontalInterestPrincipal() {
        return "horizontal-interest-principal-order".equalsIgnoreCase(this.code);
    }
}
