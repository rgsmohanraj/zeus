///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements. See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership. The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License. You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.vcpl.lms.organisation.partner.domain;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.Date;
//import jakarta.persistence.Column;
//import jakarta.persistence.Embedded;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.Table;
//import jakarta.persistence.Temporal;
//import jakarta.persistence.TemporalType;
//import org.vcpl.lms.infrastructure.core.api.JsonCommand;
//import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
//import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
//import org.vcpl.lms.organisation.monetary.domain.Money;
//
//@Entity
//@Table(name = "m_partner_transaction")
//public class PartnerTransaction extends AbstractPersistableCustom {
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "from_partner_id")
//    private Partner from;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "to_partner_id")
//    private Partner to;
//
//    @Column(name = "transaction_date", nullable = false)
//    @Temporal(TemporalType.DATE)
//    private Date transactionDate;
//
//    @Embedded
//    private MonetaryCurrency currency;
//
//    @Column(name = "transaction_amount", scale = 6, precision = 19, nullable = false)
//    private BigDecimal transactionAmount;
//
//    @Column(name = "description", nullable = true, length = 100)
//    private String description;
//
//    public static PartnerTransaction fromJson(final Partner fromPartner, final Partner toPartner, final Money amount,
//            final JsonCommand command) {
//
//        final LocalDate transactionLocalDate = command.localDateValueOfParameterNamed("transactionDate");
//        final String description = command.stringValueOfParameterNamed("description");
//
//        return new PartnerTransaction(fromPartner, toPartner, transactionLocalDate, amount, description);
//    }
//
//    protected PartnerTransaction() {
//        this.transactionDate = null;
//    }
//
//    private PartnerTransaction(final Partner fromPartner, final Partner toPartner, final LocalDate transactionLocalDate, final Money amount,
//            final String description) {
//        this.from = fromPartner;
//        this.to = toPartner;
//        if (transactionLocalDate != null) {
//            this.transactionDate = Date.from(transactionLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        }
//        this.currency = amount.getCurrency();
//        this.transactionAmount = amount.getAmount();
//        this.description = description;
//    }
//}
