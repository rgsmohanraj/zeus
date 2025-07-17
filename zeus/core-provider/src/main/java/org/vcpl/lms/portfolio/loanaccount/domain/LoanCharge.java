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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.vcpl.lms.infrastructure.bulkimport.constants.LoanConstants;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableSequenceCustom;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.organisation.monetary.domain.MoneyHelper;
import org.vcpl.lms.portfolio.charge.domain.*;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeWithoutMandatoryFieldException;
import org.vcpl.lms.portfolio.loanaccount.command.LoanChargeCommand;
import org.vcpl.lms.portfolio.loanaccount.data.GstData;
import org.vcpl.lms.portfolio.loanaccount.data.LoanChargePaidDetail;
import org.vcpl.lms.portfolio.loanaccount.service.GstServiceImpl;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.useradministration.domain.AppUser;

@Entity
@Setter
@Getter
@Table(name = "m_loan_charge", uniqueConstraints = { @UniqueConstraint(columnNames = { "external_id" }, name = "external_id") })
@ToString
public class LoanCharge extends AbstractPersistableSequenceCustom {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,generator = "m_loan_charge")
    @TableGenerator(table = "sequence_generator",allocationSize = 1,name = "m_loan_charge")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", referencedColumnName = "id", nullable = false)
    private Loan loan;

    @ManyToOne(optional = false)
    @JoinColumn(name = "charge_id", referencedColumnName = "id", nullable = false)
    private Charge charge;

    @Column(name = "installment", nullable = false)
    private Integer installmentNumber;

    @Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTime;

    @Temporal(TemporalType.DATE)
    @Column(name = "due_for_collection_as_of_date")
    private Date dueDate;

    @Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;

    @Column(name = "charge_payment_mode_enum")
    private Integer chargePaymentMode;

    @Column(name = "calculation_percentage", scale = 6, precision = 19, nullable = true)
    private BigDecimal percentage;

    @Column(name = "calculation_on_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountPercentageAppliedTo;

    @Column(name = "charge_amount_or_percentage", scale = 6, precision = 19, nullable = false)
    private BigDecimal amountOrPercentage;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "amount_paid_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountPaid;

    @Column(name = "amount_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountWaived;

    @Column(name = "amount_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountWrittenOff;

    @Column(name = "amount_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal amountOutstanding;

    @Column(name = "is_penalty", nullable = false)
    private boolean penaltyCharge = false;

    @Column(name = "is_paid_derived", nullable = false)
    private boolean paid = false;

    @Column(name = "waived", nullable = false)
    private boolean waived = false;

    @Column(name = "min_cap", scale = 6, precision = 19, nullable = true)
    private BigDecimal minCap;

    @Column(name = "max_cap", scale = 6, precision = 19, nullable = true)
    private BigDecimal maxCap;

    @Column(name = "igst_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal igstAmount;

    @Column(name = "cgst_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal cgstAmount;

    @Column(name = "sgst_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal sgstAmount;

    @Column(name = "total_gst", scale = 6, precision = 19, nullable = true)
    private BigDecimal totalGst;

    @Column(name = "gst_enum", nullable = true)
    private Integer gst;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fees_charge_type_cv_id", nullable = true)
    private CodeValue feesChargeTypes;

    @Column(name = "gst_enabled", nullable = true)
    private Boolean gstEnabled;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loancharge", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanInstallmentCharge> loanInstallmentCharge = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "external_id")
    private String externalId;

    @OneToOne(mappedBy = "loancharge", cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanOverdueInstallmentCharge overdueInstallmentCharge;

    @OneToOne(mappedBy = "loancharge", cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanTrancheDisbursementCharge loanTrancheDisbursementCharge;

    @OneToMany(mappedBy = "loanCharge", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanChargePaidBy> loanChargePaidBySet;

    @Column(name = "self_gst_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfGst;

    @Column(name = "partner_gst_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerGst;

    @Column(name = "self_gst_paid", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfGstPaid;

    @Column(name = "partner_gst_paid", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerGstPaid;

    @Column(name = "self_gst_outstanding", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfGstOutstanding;

    @Column(name = "partner_gst_outstanding", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerGstOutstanding;

    @Column(name = "self_share_amount_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal selfShareAmount;

    @Column(name = "partner_share_amount_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal partnerShareAmount;

    @Column(name = "self_share_percentage", scale = 6, precision = 19, nullable = false)
    private BigDecimal selfSharePercentage;

    @Column(name = "partner_share_percentage", scale = 6, precision = 19, nullable = false)
    private BigDecimal partnerSharePercentage;

    @Column(name = "self_share_amount_repaid", scale = 6, precision = 19, nullable = false)
    private BigDecimal selfShareAmountRepaid;

    @Column(name = "partner_share_amount_repaid", scale = 6, precision = 19, nullable = false)
    private BigDecimal partnerShareAmountRepaid;

    @Column(name = "self_amount_outstanding", scale = 6, precision = 19, nullable = false)
    private BigDecimal selfAmountOutstanding;

    @Column(name = "partner_amount_outstanding", scale = 6, precision = 19, nullable = false)
    private BigDecimal partnerAmountOutstanding;

    @Column(name = "self_amount_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfAmountWaived;

    @Column(name = "partner_amount_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerAmountWaived;

    @Column(name = "self_amount_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfAmountWrittenOff;

    @Column(name = "parter_amount_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerAmountWrittenOff;

    @Column(name = "self_gst_percentage", scale = 6, precision = 19, nullable = true)
    private BigDecimal selfGstPercentage;

    @Column(name = "partner_gst_percentage", scale = 6, precision = 19, nullable = true)
    private BigDecimal partnerGstPercentage;

    @Column(name = "createdon_date",nullable = true)
    private Date createdDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdon_userid", nullable = true)
    private AppUser createdUser;

    @Column(name = "modifiedon_date",nullable = true)
    private Date modifiedDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedon_userid", nullable = true)
    private AppUser modifiedUser;

    @Column(name = "servicer_fee_enabled",nullable = true)
    private boolean servicerFeeEnabled;

    @Column(name = "sf_self_share",nullable = true)
    private BigDecimal sfSelfShare;

    @Column(name = "sf_partner_share",nullable = true)
    private BigDecimal sfPartnerShare;

    @Column(name = "sf_self_base_amount",nullable = true)
    private BigDecimal sfSelfBaseAmount;

    @Column(name = "sf_partner_base_amount",nullable = true)
    private BigDecimal sfPartnerBaseAmount;

    @Column(name = "sf_charge_gst_loss_amount",nullable = true)
    private BigDecimal sfChargeGstLossAmount;

    @Column(name = "sf_charge_gst_amount",nullable = true)
    private BigDecimal sfChargeGstAmount;

    @Column(name = "sf_charge_invoice_amount",nullable = true)
    private BigDecimal sfChargeInvoiceAmount;

    @Column(name = "gst_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal gstOutstandingDerived;

    @Column(name = "gst_paid_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal gstPaidDerived;

    @Column(name = "gst_waivedoff_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal gstWaivedOffDerived;

    @Column(name = "self_gst_waivedOff", scale = 6, precision = 19, nullable = false)
    private BigDecimal selfGstWaivedOff;

    @Column(name = "partner_gst_waivedOff", scale = 6, precision = 19, nullable = false)
    private BigDecimal partnerGstWaivedOff;

    @Column(name = "cooling_off_reversed", nullable = true)
    private boolean coolingOffReversed;

    @Column(name = "cooling_off_retained_amount", scale = 2, precision = 19, nullable = true)
    private BigDecimal coolingOffRetainedAmount;


    public Integer getGst() {
        return gst;
    }

    public BigDecimal getSelfShareAmount() {
        return selfShareAmount;
    }

    public BigDecimal getPartnerShareAmount() {
        return partnerShareAmount;
    }

    public Integer getChargeTime() {
        return chargeTime;
    }

    public void setIgstAmount(BigDecimal igstAmount) {
        this.igstAmount = igstAmount;
    }

    public void setCgstAmount(BigDecimal cgstAmount) {
        this.cgstAmount = cgstAmount;
    }

    public void setSgstAmount(BigDecimal sgstAmount) {
        this.sgstAmount = sgstAmount;
    }


    public void setGst(Integer gst) {
        this.gst = gst;
    }

    public void setServicerFeeEnabled(boolean servicerFeeEnabled) {
        this.servicerFeeEnabled = servicerFeeEnabled;
    }

    public void setSfSelfShare(BigDecimal sfSelfShare) {
        this.sfSelfShare = sfSelfShare;
    }

    public void setSfPartnerShare(BigDecimal sfPartnerShare) {
        this.sfPartnerShare = sfPartnerShare;
    }

    public void setSfSelfBaseAmount(BigDecimal sfSelfBaseAmount) {
        this.sfSelfBaseAmount = sfSelfBaseAmount;
    }

    public void setSfPartnerBaseAmount(BigDecimal sfPartnerBaseAmount) {
        this.sfPartnerBaseAmount = sfPartnerBaseAmount;
    }

    public void setSfChargeGstLossAmount(BigDecimal sfChargeGstLossAmount) {
        this.sfChargeGstLossAmount = sfChargeGstLossAmount;
    }

    public void setSfChargeGstAmount(BigDecimal sfChargeGstAmount) {
        this.sfChargeGstAmount = sfChargeGstAmount;
    }

    public void setSfChargeInvoiceAmount(BigDecimal sfChargeInvoiceAmount) {
        this.sfChargeInvoiceAmount = sfChargeInvoiceAmount;
    }

    public void setGstEnabled(Boolean gstEnabled) {
        this.gstEnabled = gstEnabled;
    }

    public void setTotalGst(BigDecimal totalGst) {
        this.totalGst = totalGst;
    }


    public BigDecimal getSelfGst() {
        return selfGst != null ? this.selfGst:BigDecimal.ZERO;
    }

    public BigDecimal getPartnerGst() {
        return partnerGst!=null?this.partnerGst:BigDecimal.ZERO;
    }

    public BigDecimal getIgstAmount() {

        BigDecimal igstAmount= BigDecimal.valueOf(0);
//        igstAmount=this.igstAmount;
        if(this.igstAmount != null){

            return igstAmount=this.igstAmount;
        }else{
            return igstAmount;    }
    }

    public BigDecimal getCgstAmount() {
        BigDecimal cgstAmount= BigDecimal.valueOf(0);
//        igstAmount=this.igstAmount;
        if(this.cgstAmount != null){

            return cgstAmount=this.cgstAmount;
        }else{
            return cgstAmount;    }
    }


    public BigDecimal getSgstAmount() {
        BigDecimal sgstAmount= BigDecimal.valueOf(0);
//        igstAmount=this.igstAmount;
        if(this.sgstAmount != null){

            return sgstAmount=this.sgstAmount;
        }else{
            return sgstAmount;    }
    }


    public BigDecimal getTotalGst() {
        return totalGst = this.totalGst == null ? BigDecimal.ZERO : this.totalGst;
    }

    public BigDecimal getTotalSelfGst() {
        return selfGst = this.selfGst == null ? BigDecimal.ZERO : this.selfGst;
    }

    public BigDecimal getTotalPartnerGst() {
        return partnerGst = this.partnerGst == null ? BigDecimal.ZERO : this.partnerGst;
    }

    public BigDecimal getGstWaivedOffDerived() {
        return gstWaivedOffDerived;
    }

    public void setGstWaivedOffDerived(BigDecimal gstWaivedOffDerived) {
        this.gstWaivedOffDerived = gstWaivedOffDerived;
    }

    public Boolean getGstEnabled() {
        return gstEnabled;
    }

    public static LoanCharge createNewFromJson(final Loan loan, final Charge chargeDefinition,
                                               final JsonCommand command,BigDecimal chargeAmount,BigDecimal selfShare,BigDecimal partnerShare,
                                               final List<GstData> gstData,final LoanRepaymentScheduleInstallment installment) {
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        if (chargeDefinition.getChargeTimeType().equals(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()) && dueDate == null) {
            final String defaultUserMessage = "Loan charge is missing due date.";
            throw new LoanChargeWithoutMandatoryFieldException("loanCharge", "dueDate", defaultUserMessage, chargeDefinition.getId(),
                    chargeDefinition.getName());
        }

        return createNewFromJson(loan, chargeDefinition, command, dueDate,chargeAmount,selfShare,partnerShare, installment.getInstallmentNumber(),gstData);

    }

    public static LoanCharge createNewFromJson(final Loan loan, final Charge chargeDefinition, final JsonCommand command,
            final LocalDate dueDate,BigDecimal chargeAmount, BigDecimal selfShare,BigDecimal partnerShare, final Integer periodNumber,final List<GstData> gstData) {
        final BigDecimal amount = command.bigDecimalValueOfParameterNamed("amount");

        final ChargeTimeType chargeTime = null;
        final ChargeCalculationType chargeCalculation = null;
        final ChargePaymentMode chargePaymentMode = null;
        BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        switch (ChargeCalculationType.fromInt(chargeDefinition.getChargeCalculation())) {
            case PERCENT_OF_AMOUNT:
                if (command.hasParameter("principal")) {
                    amountPercentageAppliedTo = command.bigDecimalValueOfParameterNamed("principal");
                } else {
                    amountPercentageAppliedTo = loan.getPrincpal().getAmount();
                }
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                if (command.hasParameter("principal") && command.hasParameter("interest")) {
                    amountPercentageAppliedTo = command.bigDecimalValueOfParameterNamed("principal")
                            .add(command.bigDecimalValueOfParameterNamed("interest"));
                } else {
                    amountPercentageAppliedTo = loan.getPrincpal().getAmount().add(loan.getTotalInterest());
                }
            break;
            case PERCENT_OF_INTEREST:
                if (command.hasParameter("interest")) {
                    amountPercentageAppliedTo = command.bigDecimalValueOfParameterNamed("interest");
                } else {
                    amountPercentageAppliedTo = loan.getTotalInterest();
                }
            break;
            default:
            break;
        }

        BigDecimal loanCharge = BigDecimal.ZERO;
        if (ChargeTimeType.fromInt(chargeDefinition.getChargeTimeType()).equals(ChargeTimeType.INSTALMENT_FEE)) {
            BigDecimal percentage = amount;
            if (percentage == null) {
                percentage = chargeDefinition.getAmount();
            }
            loanCharge = loan.calculatePerInstallmentChargeAmount(ChargeCalculationType.fromInt(chargeDefinition.getChargeCalculation()),
                    percentage);
        }

        // If charge type is specified due date and loan is multi disburment
        // loan.
        // Then we need to get as of this loan charge due date how much amount
        // disbursed.
        if (chargeDefinition.getChargeTimeType().equals(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()) && loan.isMultiDisburmentLoan()) {
            amountPercentageAppliedTo = BigDecimal.ZERO;
            for (final LoanDisbursementDetails loanDisbursementDetails : loan.getDisbursementDetails()) {
                if (!loanDisbursementDetails.expectedDisbursementDate()
                        .after(Date.from(dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
                    amountPercentageAppliedTo = amountPercentageAppliedTo.add(loanDisbursementDetails.principal());
                }
            }
        }

        LoanCharge newLoanCharge = new LoanCharge(loan, chargeDefinition, amountPercentageAppliedTo, chargeAmount, chargeTime, chargeCalculation,
                dueDate, chargePaymentMode, null, loanCharge,selfShare,partnerShare,gstData, periodNumber);
        final String externalId = command.stringValueOfParameterNamedAllowingNull("externalId");
        newLoanCharge.setExternalId(externalId);
        return newLoanCharge;
    }

    /*
     * loanPrincipal is required for charges that are percentage based
     */
    public static LoanCharge createNewWithoutLoan(final Charge chargeDefinition, final BigDecimal loanPrincipal, final BigDecimal amount,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final LocalDate dueDate,
            final ChargePaymentMode chargePaymentMode, final Integer numberOfRepayments,final BigDecimal selfShare,final BigDecimal partnerShare,final List<GstData> gstData) {
        return new LoanCharge(null, chargeDefinition, loanPrincipal, amount, chargeTime, chargeCalculation, dueDate, chargePaymentMode,
                numberOfRepayments, BigDecimal.ZERO,selfShare,partnerShare,gstData, Integer.valueOf(0));
    }

    public LoanCharge() {
        //
    }

    public LoanCharge(final Loan loan, final Charge chargeDefinition, final BigDecimal loanPrincipal,  BigDecimal amount,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final LocalDate dueDate,
            final ChargePaymentMode chargePaymentMode, final Integer numberOfRepayments, final BigDecimal loanCharge,
                      final BigDecimal selfShare ,final BigDecimal partnerShare,final List<GstData> gstData, final Integer installmentNumber) {

        final Long id =chargeDefinition.getId();

       if(!chargeDefinition.isPercentageOfAmount(chargeDefinition.getChargeCalculation())) {
           amount = amount;
           for (GstData gstDatas : gstData) {
               if (id == gstDatas.getId()) {
                   final BigDecimal reductionProcessingFee = gstDatas.getUpdatedChargeAmount();

                  // amount = amount.subtract(reductionProcessingFee);
                   amount = BigDecimal.valueOf(amount.doubleValue() - reductionProcessingFee.doubleValue());
               }
           }
       }

       this.loan = loan;
        this.charge = chargeDefinition;
        this.penaltyCharge = chargeDefinition.isPenalty();
        this.minCap = chargeDefinition.getMinCap();
        this.maxCap = chargeDefinition.getMaxCap();
        this.installmentNumber = installmentNumber;
        this.chargeTime = chargeDefinition.getChargeTimeType();
        if (chargeTime != null) {
            this.chargeTime = chargeTime.getValue();
        }

        if (ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.SPECIFIED_DUE_DATE)
                || ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.OVERDUE_INSTALLMENT)) {

            if (dueDate == null) {
                final String defaultUserMessage = "Loan charge is missing due date.";
                throw new LoanChargeWithoutMandatoryFieldException("loanCharge", "dueDate", defaultUserMessage, chargeDefinition.getId(),
                        chargeDefinition.getName());
            }

            this.dueDate = Date.from(dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            this.dueDate = null;
        }

        this.chargeCalculation = chargeDefinition.getChargeCalculation();
        if (chargeCalculation != null) {
            this.chargeCalculation = chargeCalculation.getValue();
        }

        BigDecimal chargeAmount = chargeDefinition.getAmount();
        if (amount != null) {
            chargeAmount = amount;
        }

        this.chargePaymentMode = chargeDefinition.getChargePaymentMode();
        if (chargePaymentMode != null) {
            this.chargePaymentMode = chargePaymentMode.getValue();
        }

        populateDerivedFields(loanPrincipal, chargeAmount, numberOfRepayments, loanCharge,selfShare,partnerShare,gstData,id,chargeDefinition);
        this.paid = determineIfFullyPaid();
        this.selfSharePercentage =  Objects.isNull(selfShare)? BigDecimal.ZERO: selfShare;
        this.partnerSharePercentage = Objects.isNull(partnerShare ) ? BigDecimal.ZERO: partnerShare;

        this.gstEnabled=chargeDefinition.getEnablegstCharges();
        this.gst=chargeDefinition.getGst();
        this.feesChargeTypes=chargeDefinition.getFeesChargeType();

        if(!gstData.isEmpty()) {
            for (GstData gstDatas : gstData) {

                final Long chargeid = gstDatas.getId();
                 final Long chargeId = chargeDefinition.getId();
                 if (Objects.equals(chargeId,chargeid)) {
                        this.totalGst   = Objects.isNull(gstDatas.getTotalGst()) ? BigDecimal.ZERO : gstDatas.getTotalGst();
                        this.cgstAmount = Objects.isNull(gstDatas.getCgstAmount()) ? BigDecimal.ZERO:gstDatas.getCgstAmount();
                        this.sgstAmount = Objects.isNull(gstDatas.getSgstAmount())? BigDecimal.ZERO :gstDatas.getSgstAmount();
                        this.igstAmount = Objects.isNull(gstDatas.getIgstAmount())? BigDecimal.ZERO:gstDatas.getIgstAmount();
                        this.selfGst    = Objects.isNull(gstDatas.getSelfGst())? BigDecimal.ZERO:gstDatas.getSelfGst();
                        this.partnerGst = Objects.isNull(gstDatas.getPartnerGst()) ? BigDecimal.ZERO:gstDatas.getPartnerGst();
                        this.selfGstPaid = null;
                        this.partnerGstPaid =null;
                        this.selfGstOutstanding = retrieveSelfGstOutstanding();
                        this.partnerGstOutstanding = retrievePartnerGstOutstanding();


                    }
                }
            }
        }

    private BigDecimal retrievePartnerGstOutstanding() {

        BigDecimal partnerGst = BigDecimal.ZERO;
        if(this.partnerGst!=null){
            partnerGst = this.partnerGst;

        }

        BigDecimal partnerGstPaid = BigDecimal.ZERO;
        if(this.partnerGstPaid!=null){
            partnerGstPaid = this.partnerGstPaid;
        }

        return partnerGst.subtract(partnerGstPaid);

    }


    private BigDecimal retrieveSelfGstOutstanding() {

        BigDecimal selfGst = BigDecimal.ZERO;
        if(this.selfGst!=null){
            selfGst = this.selfGst;
        }

        BigDecimal selfGstPaid = BigDecimal.ZERO;
        if(this.selfGstPaid!=null){
            selfGstPaid = this.selfGstPaid;
        }

        return selfGst.subtract(selfGstPaid);
    }
    private void populateDerivedFields(final BigDecimal amountPercentageAppliedTo, final BigDecimal chargeAmount,
            Integer numberOfRepayments, BigDecimal loanCharge,BigDecimal selfShare,BigDecimal partnerShare,final List<GstData> gstData,
                                       final Long id,final Charge chargeDefinition) {

        switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
            case INVALID:
                this.percentage = null;
                this.amount = null;
                this.amountPercentageAppliedTo = null;
                this.amountPaid = null;
                this.amountOutstanding = BigDecimal.ZERO;
                this.amountWaived = null;
                this.amountWrittenOff = null;
            break;
            case FLAT:
                this.percentage = null;
                this.amountPercentageAppliedTo = null;
                //this.amountPaid = null;
                if (isInstalmentFee()) {
                    if (numberOfRepayments == null) {
                        numberOfRepayments = this.loan.fetchNumberOfInstallmensAfterExceptions();
                    }
                    this.amount = chargeAmount.multiply(BigDecimal.valueOf(numberOfRepayments));
                    this.amountPaid = null;

                } else {
                    this.amount = chargeAmount;
                    this.amountPaid = null;
                }
                this.amountOutstanding = calculateOutstanding();
                this.amountWaived = null;
                this.amountWrittenOff = null;

                this.selfShareAmount =Objects.isNull(selfShare) ? BigDecimal.ZERO: GstServiceImpl.percentage(chargeAmount,selfShare,chargeDefinition.getChargeDecimal(),chargeDefinition.getChargeRoundingMode());
                this.partnerShareAmount = Objects.isNull(partnerShare) ? BigDecimal.ZERO: BigDecimal.valueOf(chargeAmount.doubleValue() - this.selfShareAmount.doubleValue())  ;
                this.selfAmountOutstanding=calculateSelfOutstanding();
                this.partnerAmountOutstanding=calculatePartnerOutstanding();
                this.selfShareAmountRepaid = null;
                this.partnerShareAmountRepaid =null;

            break;
            case PERCENT_OF_AMOUNT:
            case PERCENT_OF_AMOUNT_AND_INTEREST:
            case PERCENT_OF_INTEREST:
            case PERCENT_OF_DISBURSEMENT_AMOUNT:
                this.percentage = chargeAmount;
                this.amountPercentageAppliedTo = amountPercentageAppliedTo;
                if (loanCharge.compareTo(BigDecimal.ZERO) == 0) {
                    loanCharge = percentageOf(this.amountPercentageAppliedTo);
                }

                this.amount = minimumAndMaximumCap(loanCharge);

                for (GstData gstDatas : gstData) {
                    if (id == gstDatas.getId()){
                        loanCharge=loanCharge;
                        BigDecimal feesAmount=gstDatas.getUpdatedChargeAmount();
                        if(feesAmount ==null){
                            feesAmount=BigDecimal.valueOf(0);

                        }
                       // final BigDecimal charge=loanCharge.subtract(feesAmount);
                        this.amount = BigDecimal.valueOf(loanCharge.doubleValue() - feesAmount.doubleValue()).setScale(chargeDefinition.getChargeDecimal(),chargeDefinition.getChargeRoundingMode());
                    }
                }

                this.amountPaid = null;
                this.amountOutstanding = calculateOutstanding();
                this.amountWaived = null;

                this.selfShareAmount = Objects.isNull(selfShare)? BigDecimal.ZERO: GstServiceImpl.percentage(this.amount,selfShare,chargeDefinition.getChargeDecimal(),chargeDefinition.getChargeRoundingMode());
                this.selfSharePercentage =Objects.isNull(selfShare) ? BigDecimal.ZERO: selfShare;
                this.partnerShareAmount = Objects.isNull(partnerShare) ? BigDecimal.ZERO : GstServiceImpl.percentage(this.amount,partnerShare,chargeDefinition.getChargeDecimal(),chargeDefinition.getChargeRoundingMode());
                this.partnerSharePercentage = Objects.isNull(partnerShare)? BigDecimal.ZERO: partnerShare;
                this.selfAmountOutstanding=calculateSelfOutstanding();
                this.partnerAmountOutstanding=calculatePartnerOutstanding();
                this.selfShareAmountRepaid = null;
                this.partnerShareAmountRepaid =null;
            break;
        }
        this.amountOrPercentage = chargeAmount;
        if (this.loan != null && isInstalmentFee()) {
            updateInstallmentCharges();
        }
    }

    private BigDecimal calculatePartnerOutstanding() {

        if (this.partnerShareAmount == null) {
            return null;
        }
        BigDecimal amountPaidLocal = BigDecimal.ZERO;
        if (this.partnerShareAmountRepaid != null) {
            amountPaidLocal = this.partnerShareAmountRepaid;
        }

        BigDecimal amountWaivedLocal = BigDecimal.ZERO;
        if (this.partnerAmountWaived != null) {
            amountWaivedLocal = this.partnerAmountWaived;
        }

        BigDecimal amountWrittenOffLocal = BigDecimal.ZERO;
        if (this.partnerAmountWrittenOff != null) {
            amountWrittenOffLocal = this.partnerAmountWrittenOff;
        }

        final BigDecimal totalAccountedFor = amountPaidLocal.add(amountWaivedLocal).add(amountWrittenOffLocal);
        return this.partnerShareAmount.subtract(totalAccountedFor);

    }

    private BigDecimal calculateSelfOutstanding() {
        if (this.selfShareAmount == null) {
            return null;
        }
        BigDecimal amountPaidLocal = BigDecimal.ZERO;
        if (this.selfShareAmountRepaid != null) {
            amountPaidLocal = this.selfShareAmountRepaid;
        }

        BigDecimal amountWaivedLocal = BigDecimal.ZERO;
        if (this.selfAmountWaived != null) {
            amountWaivedLocal = this.selfAmountWaived;
        }

        BigDecimal amountWrittenOffLocal = BigDecimal.ZERO;
        if (this.selfAmountWrittenOff != null) {
            amountWrittenOffLocal = this.selfAmountWrittenOff;
        }

        final BigDecimal totalAccountedFor = amountPaidLocal.add(amountWaivedLocal).add(amountWrittenOffLocal);

        return this.selfShareAmount.subtract(totalAccountedFor);
    }

    private BigDecimal percentageOfColending( BigDecimal selfShare, BigDecimal amount) {
        return percentageOf(selfShare,amount);
    }

    public void markAsFullyPaid() {
        this.amountPaid = this.amount;
        this.amountOutstanding = BigDecimal.ZERO;
        this.paid = true;
        this.selfShareAmountRepaid = this.selfShareAmount;
        this.partnerShareAmountRepaid = this.partnerShareAmount;
        this.selfAmountOutstanding  = BigDecimal.ZERO;
        this.partnerAmountOutstanding = BigDecimal.ZERO;
        this.selfGstPaid = this.selfGst;
        this.partnerGstPaid = this.partnerGst;
        this.partnerGstOutstanding = BigDecimal.ZERO;
        this.selfGstOutstanding =BigDecimal.ZERO;
    }

    public boolean isFullyPaid() {
        return this.paid;
    }

    public void resetToOriginal(final MonetaryCurrency currency) {
        this.amountPaid = BigDecimal.ZERO;
        this.amountWaived = BigDecimal.ZERO;
        this.amountWrittenOff = BigDecimal.ZERO;
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.paid = false;
        this.waived = false;
        for (final LoanInstallmentCharge installmentCharge : this.loanInstallmentCharge) {
            installmentCharge.resetToOriginal(currency);
        }
    }

    public void resetPaidAmount(final MonetaryCurrency currency) {
        this.amountPaid = BigDecimal.ZERO;
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.paid = false;
        for (final LoanInstallmentCharge installmentCharge : this.loanInstallmentCharge) {
            installmentCharge.resetPaidAmount(currency);
        }
    }

    public void resetOutstandingAmount(final BigDecimal amountOutstanding) {
        this.amountOutstanding = amountOutstanding;
    }

    public Money waive(final MonetaryCurrency currency, final Integer loanInstallmentNumber) {
        if (isInstalmentFee()) {
            final LoanInstallmentCharge chargePerInstallment = getInstallmentLoanCharge(loanInstallmentNumber);
            final Money amountWaived = chargePerInstallment.waive(currency);
            if (this.amountWaived == null) {
                this.amountWaived = BigDecimal.ZERO;
            }
            this.amountWaived = this.amountWaived.add(amountWaived.getAmount());
            this.amountOutstanding = this.amountOutstanding.subtract(amountWaived.getAmount());
            if (determineIfFullyPaid()) {
                this.paid = false;
                this.waived = true;
            }
            return amountWaived;
        }
        this.amountWaived = this.amountOutstanding;
        this.amountOutstanding = BigDecimal.ZERO;
        this.paid = false;
        this.waived = true;
        return getAmountWaived(currency);

    }

    public BigDecimal getAmountPercentageAppliedTo() {
        return this.amountPercentageAppliedTo;
    }

    private BigDecimal calculateAmountOutstanding(final MonetaryCurrency currency) {
        return getAmount(currency).minus(getAmountWaived(currency)).minus(getAmountPaid(currency)).getAmount();
    }

    public void update(final Loan loan) {
        this.loan = loan;
    }

    public void update(final BigDecimal amount, final LocalDate dueDate, final BigDecimal loanPrincipal, Integer numberOfRepayments,
            BigDecimal loanCharge,BigDecimal totalGst) {
        GstEnum gstEnum = GstEnum.fromInt(this.getCharge().getGst());
        BigDecimal inclusiveAmount = BigDecimal.ZERO;
        if(gstEnum.toString().equals(LoanConstants.INCLUSIVE)){
            inclusiveAmount =  this.getTotalGst();
        }
        if (dueDate != null) {
            this.dueDate = Date.from(dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (amount != null) {
            switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
                case INVALID:
                break;
                case FLAT:
                    if (isInstalmentFee()) {
                        if (numberOfRepayments == null) {
                            numberOfRepayments = this.loan.fetchNumberOfInstallmensAfterExceptions();
                        }
                        this.amount = amount.multiply(BigDecimal.valueOf(numberOfRepayments));
                    } else {
                        this.amount = amount;
                        this.amountPaid = amount;
                    }
                break;
                case PERCENT_OF_AMOUNT:
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                case PERCENT_OF_INTEREST:
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    this.percentage = amount;
                    this.amountPercentageAppliedTo = loanPrincipal;
                    if (loanCharge.compareTo(BigDecimal.ZERO) == 0) {
                        loanCharge =  GstServiceImpl.percentage(this.amountPercentageAppliedTo,amount,charge.getChargeDecimal(),charge.getChargeRoundingMode());
                    }
                    this.amount = loanCharge.subtract(inclusiveAmount);
                    this.amountPaid = this.amount;
                    break;
            }
            this.amountOrPercentage = amount;
            this.amountOutstanding = calculateOutstanding();
            if (this.loan != null && isInstalmentFee()) {
                updateInstallmentCharges();
            }
        }
    }

    public void update(final BigDecimal amount, final LocalDate dueDate, final Integer numberOfRepayments) {
        BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        if (this.loan != null) {
            switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
                case PERCENT_OF_AMOUNT:
                    // If charge type is specified due date and loan is multi
                    // disburment loan.
                    // Then we need to get as of this loan charge due date how
                    // much amount disbursed.
                    if (this.loan.isMultiDisburmentLoan() && this.isSpecifiedDueDate()) {
                        for (final LoanDisbursementDetails loanDisbursementDetails : this.loan.getDisbursementDetails()) {
                            if (!loanDisbursementDetails.expectedDisbursementDate().after(this.getDueDate())) {
                                amountPercentageAppliedTo = amountPercentageAppliedTo.add(loanDisbursementDetails.principal());
                            }
                        }
                    } else {
                        amountPercentageAppliedTo = this.loan.getPrincpal().getAmount();
                    }
                break;
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                    amountPercentageAppliedTo = this.loan.getPrincpal().getAmount().add(this.loan.getTotalInterest());
                break;
                case PERCENT_OF_INTEREST:
                    amountPercentageAppliedTo = this.loan.getTotalInterest();
                break;
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = this.loanTrancheDisbursementCharge;
                    amountPercentageAppliedTo = loanTrancheDisbursementCharge.getloanDisbursementDetails().principal();
                break;
                default:
                break;
            }
        }
        update(amount, dueDate, amountPercentageAppliedTo, numberOfRepayments, BigDecimal.ZERO,BigDecimal.ZERO);
    }

    public Map<String, Object> update(final JsonCommand command, final BigDecimal amount) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String dueDateParamName = "dueDate";
        if (command.isChangeInLocalDateParameterNamed(dueDateParamName, getDueLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(dueDateParamName);
            actualChanges.put(dueDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(dueDateParamName);
            this.dueDate = Date.from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        final String amountParamName = "amount";
        if (command.isChangeInBigDecimalParameterNamed(amountParamName, this.amount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName);
            BigDecimal loanCharge = null;
            actualChanges.put(amountParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
                case INVALID:
                break;
                case FLAT:
                    if (isInstalmentFee()) {
                        this.amount = newValue.multiply(BigDecimal.valueOf(this.loan.fetchNumberOfInstallmensAfterExceptions()));
                    } else {
                        this.amount = newValue;
                    }
                    this.amountOutstanding = calculateOutstanding();
                break;
                case PERCENT_OF_AMOUNT:
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                case PERCENT_OF_INTEREST:
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    this.percentage = newValue;
                    this.amountPercentageAppliedTo = amount;
                    loanCharge = BigDecimal.ZERO;
                    if (isInstalmentFee()) {
                        loanCharge = this.loan.calculatePerInstallmentChargeAmount(ChargeCalculationType.fromInt(this.chargeCalculation),
                                this.percentage);
                    }
                    if (loanCharge.compareTo(BigDecimal.ZERO) == 0) {
                        loanCharge = percentageOf(this.amountPercentageAppliedTo);
                    }
                    this.amount = minimumAndMaximumCap(loanCharge);
                    this.amountOutstanding = calculateOutstanding();
                break;
            }
            this.amountOrPercentage = newValue;
            if (isInstalmentFee()) {
                updateInstallmentCharges();
            }
        }
        return actualChanges;
    }

    private void updateInstallmentCharges() {
        final Collection<LoanInstallmentCharge> remove = new HashSet<>();
        final List<LoanInstallmentCharge> newChargeInstallments = this.loan.generateInstallmentLoanCharges(this);
        if (this.loanInstallmentCharge.isEmpty()) {
            this.loanInstallmentCharge.addAll(newChargeInstallments);
        } else {
            int index = 0;
            final List<LoanInstallmentCharge> oldChargeInstallments = new ArrayList<>();
            if (this.loanInstallmentCharge != null && !this.loanInstallmentCharge.isEmpty()) {
                oldChargeInstallments.addAll(this.loanInstallmentCharge);
            }
            Collections.sort(oldChargeInstallments);
            final LoanInstallmentCharge[] loanChargePerInstallmentArray = newChargeInstallments
                    .toArray(new LoanInstallmentCharge[newChargeInstallments.size()]);
            for (final LoanInstallmentCharge chargePerInstallment : oldChargeInstallments) {
                if (index == loanChargePerInstallmentArray.length) {
                    remove.add(chargePerInstallment);
                    chargePerInstallment.getInstallment().getInstallmentCharges().remove(chargePerInstallment);
                } else {
                    LoanInstallmentCharge newLoanInstallmentCharge = loanChargePerInstallmentArray[index++];
                    newLoanInstallmentCharge.getInstallment().getInstallmentCharges().remove(newLoanInstallmentCharge);
                    chargePerInstallment.copyFrom(newLoanInstallmentCharge);
                }
            }
            this.loanInstallmentCharge.removeAll(remove);
            while (index < loanChargePerInstallmentArray.length) {
                this.loanInstallmentCharge.add(loanChargePerInstallmentArray[index++]);
            }
        }
        Money amount = Money.zero(this.loan.getCurrency());
        for (LoanInstallmentCharge charge : this.loanInstallmentCharge) {
            amount = amount.plus(charge.getAmount());
        }
        this.amount = amount.getAmount();
    }

    public boolean isDueAtDisbursement() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.DISBURSEMENT)
                || ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.TRANCHE_DISBURSEMENT);
    }

    public boolean isSpecifiedDueDate() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.SPECIFIED_DUE_DATE);
    }

    public boolean isInstalmentFee() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.INSTALMENT_FEE);
    }

    public boolean isOverdueInstallmentCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.OVERDUE_INSTALLMENT);
    }

    private static boolean isGreaterThanZero(final BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    public LoanChargeCommand toCommand() {
        return new LoanChargeCommand(getId(), this.charge.getId(), this.amount, this.chargeTime, this.chargeCalculation, getDueLocalDate());
    }

    public LocalDate getDueLocalDate() {
        LocalDate dueDate = null;
        if (this.dueDate != null) {
            dueDate = LocalDate.ofInstant(this.dueDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }
        return dueDate;
    }

    public Date getDueDate() {
        return this.dueDate;
    }

    public void updateSplitShareAmount(final BigDecimal overdueChargeAmount) {
//        final MathContext mc = new MathContext(8, MoneyHelper.getRoundingMode());
//        final BigDecimal numerator = this.selfSharePercentage.divide(BigDecimal.valueOf(100));
//        final BigDecimal denominator = this.percentage.divide(BigDecimal.valueOf(100));
//        final BigDecimal multiplier = numerator.divide(denominator);
     //   final BigDecimal penalSelfShare = overdueChargeAmount.multiply(multiplier, mc);
        this.selfShareAmount = percentageOf(overdueChargeAmount,this.selfSharePercentage);
        this.partnerShareAmount = overdueChargeAmount.subtract(  this.selfShareAmount);
        this.selfAmountOutstanding = this.selfShareAmount;
        this.partnerAmountOutstanding = this.partnerShareAmount;
    }
    private boolean determineIfFullyPaid() {
        if (this.amount == null) {
            return true;
        }
        return BigDecimal.ZERO.compareTo(calculateOutstanding()) == 0;
    }

    private BigDecimal calculateOutstanding() {
        if (this.amount == null) {
            return null;
        }
        BigDecimal amountPaidLocal = BigDecimal.ZERO;
        if (this.amountPaid != null) {
            amountPaidLocal = this.amountPaid;
        }

        BigDecimal amountWaivedLocal = BigDecimal.ZERO;
        if (this.amountWaived != null) {
            amountWaivedLocal = this.amountWaived;
        }

        BigDecimal amountWrittenOffLocal = BigDecimal.ZERO;
        if (this.amountWrittenOff != null) {
            amountWrittenOffLocal = this.amountWrittenOff;
        }

        final BigDecimal totalAccountedFor = amountPaidLocal.add(amountWaivedLocal).add(amountWrittenOffLocal);

        return this.amount.subtract(totalAccountedFor);
    }

    public BigDecimal percentageOf(final BigDecimal value) {
        return percentageOf(this.percentage,value );
    }

    public static BigDecimal percentageOf(final BigDecimal percentage, final BigDecimal  value  ) {

        BigDecimal percentageOf = BigDecimal.ZERO;

        if (isGreaterThanZero(value)) {
            final MathContext mc = new MathContext(8, MoneyHelper.getRoundingMode());
          //  final BigDecimal multiplicand = percentage.divide(BigDecimal.valueOf(100L), mc);
            final BigDecimal multiplicand = BigDecimal.valueOf(percentage.doubleValue() /(BigDecimal.valueOf(100L).doubleValue()));
            percentageOf = BigDecimal.valueOf(value.doubleValue() * multiplicand.doubleValue()).setScale(2,RoundingMode.HALF_UP) ;
        }
        return percentageOf;
    }

    /**
     * @param percentageOf
     * @returns a minimum cap or maximum cap set on charges if the criteria fits else it returns the percentageOf if the
     *          amount is within min and max cap
     */
    private BigDecimal minimumAndMaximumCap(final BigDecimal percentageOf) {
        BigDecimal minMaxCap = BigDecimal.ZERO;
        if (this.minCap != null) {
            final int minimumCap = percentageOf.compareTo(this.minCap);
            if (minimumCap == -1) {
                minMaxCap = this.minCap;
                return minMaxCap;
            }
        }
        if (this.maxCap != null) {
            final int maximumCap = percentageOf.compareTo(this.maxCap);
            if (maximumCap == 1) {
                minMaxCap = this.maxCap;
                return minMaxCap;
            }
        }
        minMaxCap = percentageOf;
        // this will round the amount value
        if (this.loan != null && minMaxCap != null) {
            minMaxCap = Money.of(this.loan.getCurrency(), minMaxCap).getAmount();
        }
        return minMaxCap;
    }

    public BigDecimal amount() {
        return this.amount;
    }

    public BigDecimal amountOutstanding() {
        return this.amountOutstanding;
    }

    public Money getAmountOutstanding(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountOutstanding);
    }

    public boolean hasNotLoanIdentifiedBy(final Long loanId) {
        return !hasLoanIdentifiedBy(loanId);
    }

    public boolean hasLoanIdentifiedBy(final Long loanId) {
        return this.loan.hasIdentifyOf(loanId);
    }


    public boolean isDueForCollectionFromAndUpToAndIncluding(final LocalDate fromNotInclusive, final LocalDate upToAndInclusive) {
        final LocalDate dueDate = getDueLocalDate();
        return occursOnDayFromAndUpToAndIncluding(fromNotInclusive, upToAndInclusive, dueDate);
    }

    private boolean occursOnDayFromAndUpToAndIncluding(final LocalDate fromNotInclusive, final LocalDate upToAndInclusive,
            final LocalDate target) {
        return target != null && target.isAfter(fromNotInclusive) && (target.isAfter(upToAndInclusive) || target.equals(upToAndInclusive));
    }

    public boolean isFeeCharge() {
        return !this.penaltyCharge;
    }

    public boolean isPenaltyCharge() {
        return this.penaltyCharge;
    }

    public boolean isNotFullyPaid() {
        return !isPaid();
    }

    public boolean isChargePending() {
        return isNotFullyPaid() && !isWaived();
    }

    public boolean isPaid() {
        return this.paid;
    }

    public boolean isWaived() {
        return this.waived;
    }

    public BigDecimal getMinCap() {
        return this.minCap;
    }

    public BigDecimal getMaxCap() {
        return this.maxCap;
    }

    public boolean isPaidOrPartiallyPaid(final MonetaryCurrency currency) {

        final Money amountWaivedOrWrittenOff = getAmountWaived(currency).plus(getAmountWrittenOff(currency));
        return Money.of(currency, this.amountPaid).plus(amountWaivedOrWrittenOff).isGreaterThanZero();
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public Money getAmountPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountPaid);
    }
    public BigDecimal getAmountPaid() {
        return this.amountPaid != null ? amountPaid : BigDecimal.ZERO;
    }

    public Money getGst(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalGst);
    }

    public Money getAmountWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountWaived);
    }

    public BigDecimal getAmountWaived() {
        return this.amountWaived != null?amountWaived:BigDecimal.ZERO;
    }

    public Money getAmountWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountWrittenOff);
    }

    public BigDecimal getSelfGstWaivedOff() {
        return selfGstWaivedOff;
    }

    public void setSelfGstWaivedOff(BigDecimal selfGstWaivedOff) {
        this.selfGstWaivedOff = selfGstWaivedOff;
    }

    public BigDecimal getPartnerGstWaivedOff() {
        return partnerGstWaivedOff;
    }

    public void setPartnerGstWaivedOff(BigDecimal partnerGstWaivedOff) {
        this.partnerGstWaivedOff = partnerGstWaivedOff;
    }

    /**
     * @param incrementBy
     *
     * @param installmentNumber
     *
     * @param feeAmount
     *            TODO
     *
     *
     * @return Actual amount paid on this charge
     */
    public Money updatePaidAmountBy(final Money incrementBy, final Integer installmentNumber, final Money feeAmount) {
        Money processAmount = Money.zero(incrementBy.getCurrency());
        if (isInstalmentFee()) {
            if (installmentNumber == null) {
                processAmount = getUnpaidInstallmentLoanCharge().updatePaidAmountBy(incrementBy, feeAmount);
            } else {
                processAmount = getInstallmentLoanCharge(installmentNumber).updatePaidAmountBy(incrementBy, feeAmount);
            }
        } else {
            processAmount = incrementBy;
        }
        Money amountPaidToDate = Money.of(processAmount.getCurrency(), this.amountPaid);
        final Money amountOutstanding = Money.of(processAmount.getCurrency(), this.amountOutstanding);

        Money amountPaidOnThisCharge = Money.zero(processAmount.getCurrency());
        if (processAmount.isGreaterThanOrEqualTo(amountOutstanding)) {
            amountPaidOnThisCharge = amountOutstanding;
            amountPaidToDate = amountPaidToDate.plus(amountOutstanding);
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = BigDecimal.ZERO;
            Money waivedAmount = getAmountWaived(processAmount.getCurrency());
            if (waivedAmount.isGreaterThanZero()) {
                this.waived = true;
            } else {
                this.paid = true;
            }

        } else {
            amountPaidOnThisCharge = processAmount;
            amountPaidToDate = amountPaidToDate.plus(processAmount);
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = calculateAmountOutstanding(incrementBy.getCurrency());
        }
        return amountPaidOnThisCharge;
    }

    public String name() {
        return this.charge.getName();
    }

    public String currencyCode() {
        return this.charge.getCurrencyCode();
    }

    public Charge getCharge() {
        return this.charge;
    }

    /*
     * @Override public boolean equals(final Object obj) { if (obj == null) { return false; } if (obj == this) { return
     * true; } if (obj.getClass() != getClass()) { return false; } final LoanCharge rhs = (LoanCharge) obj; return new
     * EqualsBuilder().appendSuper(super.equals(obj)) // .append(getId(), rhs.getId()) // .append(this.charge.getId(),
     * rhs.charge.getId()) // .append(this.amount, rhs.amount) // .append(getDueLocalDate(), rhs.getDueLocalDate()) //
     * .isEquals(); }
     *
     * @Override public int hashCode() { return 1;
     *
     * return new HashCodeBuilder(3, 5) // .append(getId()) // .append(this.charge.getId()) //
     * .append(this.amount).append(getDueLocalDate()) // .toHashCode();
     *
     * }
     */

    public ChargePaymentMode getChargePaymentMode() {
        return ChargePaymentMode.fromInt(this.chargePaymentMode);
    }

    public ChargeCalculationType getChargeCalculation() {
        return ChargeCalculationType.fromInt(this.chargeCalculation);
    }

    public BigDecimal getPercentage() {
        return this.percentage;
    }

    public void updateAmount(final BigDecimal amount) {
        this.amount = amount;
        calculateOutstanding();
    }

    public LoanInstallmentCharge getUnpaidInstallmentLoanCharge() {
        LoanInstallmentCharge unpaidChargePerInstallment = null;
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            if (loanChargePerInstallment.isPending() && (unpaidChargePerInstallment == null || unpaidChargePerInstallment
                    .getRepaymentInstallment().getDueDate().isAfter(loanChargePerInstallment.getRepaymentInstallment().getDueDate()))) {
                unpaidChargePerInstallment = loanChargePerInstallment;
            }
        }
        return unpaidChargePerInstallment;
    }

    public LoanInstallmentCharge getInstallmentLoanCharge(final LocalDate periodDueDate) {
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            if (periodDueDate.isEqual(loanChargePerInstallment.getRepaymentInstallment().getDueDate())) {
                return loanChargePerInstallment;
            }
        }
        return null;
    }

    public LoanInstallmentCharge getInstallmentLoanCharge(final Integer installmentNumber) {
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            if (installmentNumber.equals(loanChargePerInstallment.getRepaymentInstallment().getInstallmentNumber().intValue())) {
                return loanChargePerInstallment;
            }
        }
        return null;
    }

    public void setInstallmentLoanCharge(final LoanInstallmentCharge loanInstallmentCharge, final Integer installmentNumber) {
        LoanInstallmentCharge loanInstallmentChargeToBeRemoved = null;
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            if (installmentNumber.equals(loanChargePerInstallment.getRepaymentInstallment().getInstallmentNumber().intValue())) {
                loanInstallmentChargeToBeRemoved = loanChargePerInstallment;
                break;
            }
        }
        this.loanInstallmentCharge.remove(loanInstallmentChargeToBeRemoved);
        this.loanInstallmentCharge.add(loanInstallmentCharge);
    }

    public void clearLoanInstallmentCharges() {
        this.loanInstallmentCharge.clear();
    }

    public void addLoanInstallmentCharges(final Collection<LoanInstallmentCharge> installmentCharges) {
        this.loanInstallmentCharge.addAll(installmentCharges);
    }

    public boolean hasNoLoanInstallmentCharges() {
        return this.loanInstallmentCharge.isEmpty();
    }

    public Set<LoanInstallmentCharge> installmentCharges() {
        return this.loanInstallmentCharge;
    }

    public List<LoanChargePaidDetail> fetchRepaymentInstallment(final MonetaryCurrency currency) {
        List<LoanChargePaidDetail> chargePaidDetails = new ArrayList<>();
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            if (!loanChargePerInstallment.isChargeAmountpaid(currency)
                    && loanChargePerInstallment.getAmountThroughChargePayment(currency).isGreaterThanZero()) {
                LoanChargePaidDetail chargePaidDetail = new LoanChargePaidDetail(
                        loanChargePerInstallment.getAmountThroughChargePayment(currency),
                        loanChargePerInstallment.getRepaymentInstallment(), isFeeCharge());
                chargePaidDetails.add(chargePaidDetail);
            }
        }
        return chargePaidDetails;
    }

    public boolean isActive() {
        return this.active;
    }

    public void  updateActive() {
            this.active = false ;

    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            this.overdueInstallmentCharge = null;
            this.loanTrancheDisbursementCharge = null;
            this.clearLoanInstallmentCharges();
        }
    }

    public BigDecimal amountOrPercentage() {
        return this.amountOrPercentage;
    }

    public BigDecimal chargeAmount() {
        BigDecimal totalChargeAmount = this.amountOutstanding;
        if (this.amountPaid != null) {
            totalChargeAmount = totalChargeAmount.add(this.amountPaid);
        }
        if (this.amountWaived != null) {
            totalChargeAmount = totalChargeAmount.add(this.amountWaived);
        }
        if (this.amountWrittenOff != null) {
            totalChargeAmount = totalChargeAmount.add(this.amountWrittenOff);
        }
        return totalChargeAmount;
    }

    public void updateOverdueInstallmentCharge(LoanOverdueInstallmentCharge overdueInstallmentCharge) {
        this.overdueInstallmentCharge = overdueInstallmentCharge;
    }

    public void updateLoanTrancheDisbursementCharge(final LoanTrancheDisbursementCharge loanTrancheDisbursementCharge) {
        this.loanTrancheDisbursementCharge = loanTrancheDisbursementCharge;
    }

    public void updateWaivedAmount(MonetaryCurrency currency) {
        if (isInstalmentFee()) {
            this.amountWaived = BigDecimal.ZERO;
            for (final LoanInstallmentCharge chargePerInstallment : this.loanInstallmentCharge) {
                final Money amountWaived = chargePerInstallment.updateWaivedAndAmountPaidThroughChargePaymentAmount(currency);
                this.amountWaived = this.amountWaived.add(amountWaived.getAmount());
                this.amountOutstanding = this.amountOutstanding.subtract(amountWaived.getAmount());
                if (determineIfFullyPaid() && Money.of(currency, this.amountWaived).isGreaterThanZero()) {
                    this.paid = false;
                    this.waived = true;
                }
            }
            return;
        }

        Money waivedAmount = Money.of(currency, this.amountWaived);
        if (waivedAmount.isGreaterThanZero()) {
            if (waivedAmount.isGreaterThan(this.getAmount(currency))) {
                this.amountWaived = this.getAmount(currency).getAmount();
                this.amountOutstanding = BigDecimal.ZERO;
                this.paid = false;
                this.waived = true;
            } else if (waivedAmount.isLessThan(this.getAmount(currency))) {
                this.paid = false;
                this.waived = false;
            }
        }

    }

    public LoanOverdueInstallmentCharge getOverdueInstallmentCharge() {
        return this.overdueInstallmentCharge;
    }

    public LoanTrancheDisbursementCharge getTrancheDisbursementCharge() {
        return this.loanTrancheDisbursementCharge;
    }

    public Money undoPaidOrPartiallyAmountBy(final Money incrementBy, final Integer installmentNumber, final Money feeAmount) {
        Money processAmount = Money.zero(incrementBy.getCurrency());
        if (isInstalmentFee()) {
            if (installmentNumber == null) {
                processAmount = getLastPaidOrPartiallyPaidInstallmentLoanCharge(incrementBy.getCurrency()).undoPaidAmountBy(incrementBy,
                        feeAmount);
            } else {
                processAmount = getInstallmentLoanCharge(installmentNumber).undoPaidAmountBy(incrementBy, feeAmount);
            }
        } else {
            processAmount = incrementBy;
        }
        Money amountPaidToDate = Money.of(processAmount.getCurrency(), this.amountPaid);

        Money amountDeductedOnThisCharge = Money.zero(processAmount.getCurrency());
        if (processAmount.isGreaterThanOrEqualTo(amountPaidToDate)) {
            amountDeductedOnThisCharge = amountPaidToDate;
            amountPaidToDate = Money.zero(processAmount.getCurrency());
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = this.amount;
            this.paid = false;

        } else {
            amountDeductedOnThisCharge = processAmount;
            amountPaidToDate = amountPaidToDate.minus(processAmount);
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = calculateAmountOutstanding(incrementBy.getCurrency());
        }
        return amountDeductedOnThisCharge;
    }

    public LoanInstallmentCharge getLastPaidOrPartiallyPaidInstallmentLoanCharge(MonetaryCurrency currency) {
        LoanInstallmentCharge paidChargePerInstallment = null;
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            Money outstanding = Money.of(currency, loanChargePerInstallment.getAmountOutstanding());
            final boolean partiallyPaid = outstanding.isGreaterThanZero()
                    && outstanding.isLessThan(loanChargePerInstallment.getAmount(currency));
            if ((partiallyPaid || loanChargePerInstallment.isPaid()) && (paidChargePerInstallment == null || paidChargePerInstallment
                    .getRepaymentInstallment().getDueDate().isBefore(loanChargePerInstallment.getRepaymentInstallment().getDueDate()))) {
                paidChargePerInstallment = loanChargePerInstallment;
            }
        }
        return paidChargePerInstallment;
    }

    public Set<LoanChargePaidBy> getLoanChargePaidBySet() {
        return this.loanChargePaidBySet;
    }

    public Loan getLoan() {
        return this.loan;
    }

    public boolean isDisbursementCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.DISBURSEMENT);
    }
    public boolean isForeclosureCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.FORECLOSURE_CHARGE);
    }
    public boolean isAdhocChargeCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.ADHOC_CHARGE);
    }
    public boolean isTrancheDisbursementCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.TRANCHE_DISBURSEMENT);
    }

    public boolean isDueDateCharge() {
        return this.dueDate != null;
    }
    public boolean isBounceCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).isBounceCharge();
    }

    public void setAmountWaived(final BigDecimal amountWaived) {
        this.amountWaived = amountWaived;
    }

    public void undoWaived() {
        this.waived = false;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public BigDecimal getAmount() {
        return amount;
    }


    public BigDecimal selfAmount() {
        return this.selfShareAmount;
    }

    public BigDecimal partnerAmount() {
        return this.partnerShareAmount;
    }

    public boolean enabelGst() {
        return this.gstEnabled;
    }

    public void updateProcessingFee(BigDecimal updateChargeAmount) {
        if(updateChargeAmount ==null)
        {
            this.selfShareAmount=this.selfShareAmount;

        }else
        {
            this.selfShareAmount=selfShareAmount.subtract(updateChargeAmount);
        }



    }

    public BigDecimal cgstAmount() {
        BigDecimal cgstAmount=this.cgstAmount;
        if(cgstAmount!= null){
            return cgstAmount;}
        else{
            cgstAmount=BigDecimal.valueOf(0);
            return cgstAmount;

        }

    }

    public BigDecimal igstAmount() {

        BigDecimal igstAmount=this.igstAmount;
        if(igstAmount!= null){
            return igstAmount;}
        else{
            igstAmount=BigDecimal.valueOf(0);
            return igstAmount;
        }
    }

    public BigDecimal sgstAmount() {

        BigDecimal sgstAmount=this.sgstAmount;
        if(sgstAmount!= null){
            return sgstAmount;}
        else{
            sgstAmount=BigDecimal.valueOf(0);
        return sgstAmount;
        }
    }

    public CodeValue getFeesChargeTypes(){
        return feesChargeTypes;
    }

    public boolean isProcessingFee(Charge loanCharge) {
        return loanCharge.getFeesChargeType().getId() == 55;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public boolean isOverDueCharge() {

        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.OVERDUE_INSTALLMENT);
    }

    public boolean isGstEnabled() {

        return this.gstEnabled.equals("true");
    }

    public BigDecimal getSelfGstOutstanding() {
        return selfGstOutstanding;
    }

    public BigDecimal getPartnerGstOutstanding() {
        return partnerGstOutstanding;
    }

    public BigDecimal getSelfSharePercentage() {
        return selfSharePercentage;
    }

    public BigDecimal getPartnerSharePercentage() {
        return partnerSharePercentage;
    }

    public BigDecimal getSelfAmountOutstanding() {
        return selfAmountOutstanding;
    }

    public BigDecimal getPartnerAmountOutstanding() {
        return partnerAmountOutstanding;
    }

    public BigDecimal getSelfShareAmountRepaid() {
        return selfShareAmountRepaid;
    }

    public BigDecimal getPartnerShareAmountRepaid() {
        return partnerShareAmountRepaid;
    }

    public BigDecimal getSelfAmountWaived() {
        return selfAmountWaived;
    }

    public BigDecimal getPartnerAmountWaived() {
        return partnerAmountWaived;
    }

    public BigDecimal getSelfAmountWrittenOff() {
        return selfAmountWrittenOff;
    }

    public BigDecimal getPartnerAmountWrittenOff() {
        return partnerAmountWrittenOff;
    }

    public BigDecimal getGstOutstandingDerived() {
        return gstOutstandingDerived;
    }

    public void setGstOutstandingDerived(BigDecimal gstOutstandingDerived) {
        this.gstOutstandingDerived = gstOutstandingDerived;
    }

    public BigDecimal getGstPaidDerived() {
        return gstPaidDerived;
    }

    public void setGstPaidDerived(BigDecimal gstPaidDerived) {
        this.gstPaidDerived = gstPaidDerived;
    }

    public void updateGstLiabilityBySelfAndPartner(LoanProduct loanProduct) {
        this.selfGstPercentage = loanProduct.getGstLiabilityByVcpl();
        this.partnerGstPercentage = loanProduct.getGstLiabilityByPartner();
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public ChargeTimeType getChargeTimeType() {
        return ChargeTimeType.fromInt(this.chargeTime);
    }
    @Override
    public Long getId() {
        return id;
    }

    public BigDecimal getSelfGstPaid(){
        return Objects.nonNull(selfGstPaid) ? selfGstPaid:BigDecimal.ZERO;
    }

    public BigDecimal getPartnerGstPaid(){
        return Objects.nonNull(partnerGstPaid) ? partnerGstPaid:BigDecimal.ZERO;
    }
}


