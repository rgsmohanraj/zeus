/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.loanproduct.domain;

import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.portfolio.loanproduct.LoanProductConstants;


import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.util.Map;

@Entity
@Table(name = "m_colending_accepted_dates")
public class LoanProductAcceptedDates extends AbstractPersistableCustom {
    @ManyToOne
    @JoinColumn(name = "product_loan_id", nullable = true)
    private LoanProduct loanProduct;

    @Column(name = "accepted_date", nullable = true)
    private Integer acceptedDate;


    protected LoanProductAcceptedDates() {

    }

    public LoanProductAcceptedDates(final Integer acceptedDate) {
        this.acceptedDate = acceptedDate;

    }

    public void updateLoanProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME, this.acceptedDate)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.ACCEPTED_DATE_PARAMETER_NAME, newValue);
            this.acceptedDate = newValue;
        } else {
            this.acceptedDate = null;

        }
    }

    public Integer acceptedDate() {
        return this.acceptedDate;
    }


}
