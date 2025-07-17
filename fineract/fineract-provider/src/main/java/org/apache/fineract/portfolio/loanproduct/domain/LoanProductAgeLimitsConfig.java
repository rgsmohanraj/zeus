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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Column;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "m_product_loan_age_limits_config")
public class LoanProductAgeLimitsConfig extends AbstractPersistableCustom {

    @OneToOne
    @JoinColumn(name = "product_loan_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "min_age")
    private Integer minimumAge;

    @Column(name = "max_age")
    private Integer maximumAge;

    protected LoanProductAgeLimitsConfig() {

    }

    public LoanProductAgeLimitsConfig(final LoanProduct loanProduct, final Integer minimumAge, final Integer maximumAge) {
        this.loanProduct = loanProduct;
        this.minimumAge = minimumAge;
        this.maximumAge = maximumAge;
    }

    public void setLoanProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public Map<? extends String, ? extends Object> update(JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(3);

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.minimumAge, this.minimumAge)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.minimumAge);
            actualChanges.put(LoanProductConstants.minimumAge, newValue);
            this.minimumAge = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.maximumAge, this.maximumAge)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.maximumAge);
            actualChanges.put(LoanProductConstants.maximumAge, newValue);
            this.maximumAge = newValue;
        }

        return actualChanges;
    }

    public Integer getMinimumAge() {
        return this.minimumAge;
    }

    public Integer getMaximumAge() {
        return this.maximumAge;
    }

}
