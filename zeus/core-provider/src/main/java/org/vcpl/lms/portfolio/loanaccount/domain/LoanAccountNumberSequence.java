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
package org.vcpl.lms.portfolio.loanaccount.domain;

import lombok.Setter;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Setter
@Table(name = "m_loan_account_sequence")
public class LoanAccountNumberSequence extends AbstractPersistableCustom {
    @Column(name = "partner_id", nullable = true)
    private Long partnerId;
    @Column(name = "product_id", nullable = true)
    private Long productId;
    @Column(name = "loan_acc_no_preference", nullable = true)
    private String loanAccNoPreference;
    @Column(name = "short_name", nullable = true)
    private String shortName;
    @Column(name = "sequence_number", nullable = true)
    private Integer sequenceNumber;

    protected LoanAccountNumberSequence() {
    }

    public LoanAccountNumberSequence(Long partnerId, Long productId, String loanAccNoPreference, String shortName, Integer sequenceNumber) {
        this.partnerId = partnerId;
        this.productId = productId;
        this.loanAccNoPreference = loanAccNoPreference;
        this.shortName = shortName;
        this.sequenceNumber = sequenceNumber;

    }

    public Long getPartnerId() {
        return partnerId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getLoanAccNoPreference() {
        return loanAccNoPreference;
    }

    public String getShortName() {
        return shortName;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
}
