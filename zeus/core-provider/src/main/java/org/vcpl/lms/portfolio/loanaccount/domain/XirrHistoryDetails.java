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

import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "m_loan_xirr_history_details")
public class XirrHistoryDetails extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = true)
    private Loan loan;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @Column(name = "loan_event", nullable = true)
    private Integer loanEvent;

    @Column(name = "xirr_value", nullable = true)
    private BigDecimal xirrValue;

    @Column(name = "amount", nullable = true)
    private BigDecimal amount;

    protected XirrHistoryDetails() {
    }

    public XirrHistoryDetails(Loan loan,LocalDate createdDate, Integer loanEvent, BigDecimal xirrValue, BigDecimal amount) {
        this.loan = loan;
        this.createdDate = Date.from(createdDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.loanEvent = loanEvent;
        this.xirrValue = xirrValue;
        this.amount = amount;

    }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges) {

        if (command.isChangeInDateParameterNamed(LoanApiConstants.createdDate, this.createdDate)) {
            final Date newValue = command.dateValueOfParameterNamed(LoanApiConstants.createdDate);
            actualChanges.put(LoanApiConstants.createdDate, newValue);
            this.createdDate = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanApiConstants.loanEvent, this.loanEvent)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanApiConstants.loanEvent);
            actualChanges.put(LoanApiConstants.loanEvent, newValue);
            this.loanEvent = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.xirrValue, this.xirrValue)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanApiConstants.xirrValue);
            actualChanges.put(LoanApiConstants.xirrValue, newValue);
            this.xirrValue = newValue;
        }
        if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.amount, this.amount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanApiConstants.amount);
            actualChanges.put(LoanApiConstants.amount, newValue);
            this.amount = newValue;
        }
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public Integer getLoanEvent() {
        return this.loanEvent;
    }

    public BigDecimal getXirrValue() {
        return this.xirrValue;
    }
    public BigDecimal getAmount() {
        return this.amount;
    }
}
