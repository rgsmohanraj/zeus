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
//package org.vcpl.lms.organisation.partner.data;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Collection;
//import org.vcpl.lms.organisation.monetary.data.CurrencyData;
//
///**
// * Immutable data object for partner transactions.
// */
//public final class PartnerTransactionData {
//
//    @SuppressWarnings("unused")
//    private final Long id;
//    @SuppressWarnings("unused")
//    private final LocalDate transactionDate;
//    @SuppressWarnings("unused")
//    private final Long fromPartnerId;
//    @SuppressWarnings("unused")
//    private final String fromPartnerName;
//    @SuppressWarnings("unused")
//    private final Long toPartnerId;
//    @SuppressWarnings("unused")
//    private final String toPartnerName;
//    @SuppressWarnings("unused")
//    private final CurrencyData currency;
//    @SuppressWarnings("unused")
//    private final BigDecimal transactionAmount;
//    @SuppressWarnings("unused")
//    private final String description;
//    @SuppressWarnings("unused")
//    private final Collection<CurrencyData> currencyOptions;
//    @SuppressWarnings("unused")
//    private final Collection<PartnerData> allowedPartners;
//
//    public static PartnerTransactionData instance(final Long id, final LocalDate transactionDate, final Long fromPartnerId,
//            final String fromPartnerName, final Long toPartnerId, final String toPartnerName, final CurrencyData currency,
//            final BigDecimal transactionAmount, final String description) {
//        return new PartnerTransactionData(id, transactionDate, fromPartnerId, fromPartnerName, toPartnerId, toPartnerName, currency,
//                transactionAmount, description, null, null);
//    }
//
//    public static PartnerTransactionData template(final LocalDate transactionDate, final Collection<PartnerData> parentLookups,
//            final Collection<CurrencyData> currencyOptions) {
//        return new PartnerTransactionData(null, transactionDate, null, null, null, null, null, null, null, parentLookups, currencyOptions);
//    }
//
//    private PartnerTransactionData(final Long id, final LocalDate transactionDate, final Long fromPartnerId, final String fromPartnerName,
//            final Long toPartnerId, final String toPartnerName, final CurrencyData currency, final BigDecimal transactionAmount,
//            final String description, final Collection<PartnerData> allowedPartners, final Collection<CurrencyData> currencyOptions) {
//        this.id = id;
//        this.fromPartnerId = fromPartnerId;
//        this.fromPartnerName = fromPartnerName;
//        this.toPartnerId = toPartnerId;
//        this.toPartnerName = toPartnerName;
//        this.currency = currency;
//        this.transactionAmount = transactionAmount;
//        this.description = description;
//        this.transactionDate = transactionDate;
//        this.allowedPartners = allowedPartners;
//        this.currencyOptions = currencyOptions;
//    }
//}
