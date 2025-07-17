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


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vcpl.lms.infrastructure.bulkimport.constants.TransactionConstants;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.Money;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_collection_report")
public class CollectionReport extends AbstractPersistableCustom {


    @OneToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "loan_transaction_id", nullable = false)
    private LoanTransaction loanTransaction;

    @Column (name ="installment_number",nullable = false)
    private  Integer installmentNumber;

    @Column(name = "external_id", length = 100, nullable = true, unique = true)
    private String externalId;

    @Column(name = "transaction_type_enum", nullable = false)
    private Integer typeOf;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private Date dateOf;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdon_date", nullable = true)
    private Date createdDate;

    @Column(name = "amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal amount;

    @Column(name = "self_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfDue;

    @Column(name = "partner_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerDue;

    @Column(name = "principal_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalPortion;

    @Column(name = "self_principal_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPrincipalPortion;

    @Column(name = "partner_principal_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPrincipalPortion;

    @Column(name = "interest_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestPortion;

    @Column(name = "self_interest_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfInterestPortion;

    @Column(name = "partner_interest_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerInterestPortion;

    @Column(name = "fee_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesPortion;

    @Column(name = "self_fee_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfFeeChargesPortion;

    @Column(name = "partner_fee_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerFeeChargesPortion;

    @Column(name = "penalty_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesPortion;

    @Column(name = "self_penalty_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfPenaltyChargesPortion;

    @Column(name = "partner_penalty_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerPenaltyChargesPortion;

    @Column(name = "outstanding_loan_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal outstandingLoanBalance;

    @Column(name = "self_outstanding_loan_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfOutstandingLoanBalance;

    @Column(name = "partner_outstanding_loan_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partneroutstandingLoanBalance;

    @Column(name = "receipt_reference_number",  nullable = false)
    private String receiptReferenceNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "partner_transfer_date", nullable = true)
    private Date partnerTransferDate;

    @Column(name = "partner_transfer_utr",  nullable = true)
    private String partnerTransferUtr;

    @Column(name = "advance_amount", nullable = true)
    private BigDecimal advanceAmount;

    @Column (name = "status",nullable = false)
    private String status;


    public void mappingLoanTransactionToReport(CollectionReport collectionReport, LoanTransaction loanTransaction,
                                                LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment, Money interestPortion, Money principalPortion, Money feeChargesPortion, Money penaltyChargesPortion, Money selfPrincipalPortion,
                                                Money selfInterestPortion, BigDecimal selfFeeChargesPortion, BigDecimal selfSharePenaltyAmount, Money partnerPrincipalPortion, Money partnerInterestPortion, BigDecimal  partnerFeeChargesPortion, BigDecimal partnerSharePenaltyAmount){
        collectionReport.setLoan(loanTransaction.getLoan());
        collectionReport.setLoanTransaction(loanTransaction);
        collectionReport.setInstallmentNumber(loanRepaymentScheduleInstallment.getInstallmentNumber());
        collectionReport.setAmount(interestPortion.plus(principalPortion).getAmount());
        collectionReport.setSelfDue(selfPrincipalPortion.add(selfInterestPortion).getAmount());
        collectionReport.setPartnerDue(partnerPrincipalPortion.add(partnerInterestPortion).getAmount());
        collectionReport.setPrincipalPortion(principalPortion.getAmount());
        collectionReport.setInterestPortion(interestPortion.getAmount());
        collectionReport.setFeeChargesPortion(feeChargesPortion.getAmount());
        collectionReport.setPenaltyChargesPortion(penaltyChargesPortion.getAmount());
        collectionReport.setSelfPrincipalPortion(selfPrincipalPortion.getAmount());
        collectionReport.setPartnerPrincipalPortion(partnerPrincipalPortion.getAmount());
        collectionReport.setSelfInterestPortion(selfInterestPortion.getAmount());
        collectionReport.setPartnerInterestPortion(partnerInterestPortion.getAmount());
        collectionReport.setSelfFeeChargesPortion(selfFeeChargesPortion);
        collectionReport.setPartnerFeeChargesPortion(partnerFeeChargesPortion);
        collectionReport.setSelfPenaltyChargesPortion(selfSharePenaltyAmount);
        collectionReport.setPartnerPenaltyChargesPortion(partnerSharePenaltyAmount);
        collectionReport.setAdvanceAmount(interestPortion.add(principalPortion).negated().getAmount());
        collectionReport.setDateOf(loanTransaction.getDateOf());
        collectionReport.setTypeOf(loanTransaction.getTypeOf().getValue());
        collectionReport.setCreatedDate(Date.from(loanTransaction.getCreatedDateTime().atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant()));
        collectionReport.setStatus(loanRepaymentScheduleInstallment.isObligationsMet()? TransactionConstants.FULLYPAID:TransactionConstants.PARTIALLYPAID);
        collectionReport.setPartnerTransferDate(loanTransaction.getPartnerTransferDate());
        collectionReport.setPartnerTransferUtr(loanTransaction.getPartnerTransferUtr());
        collectionReport.setReceiptReferenceNumber(loanTransaction.getReceiptReferenceNumber());
    }

}
