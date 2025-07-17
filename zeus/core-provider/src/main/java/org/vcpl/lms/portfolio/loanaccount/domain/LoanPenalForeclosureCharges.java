package org.vcpl.lms.portfolio.loanaccount.domain;


import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@NoArgsConstructor
@Setter
@Table(name = "m_loan_penal_foreclosure_charge")
public class LoanPenalForeclosureCharges extends AbstractPersistableCustom {

    @OneToOne(optional = false)
    @JoinColumn(name = "loan_id", referencedColumnName = "id", nullable = false)
    private Loan loan;

    @OneToOne(optional = false)
    @JoinColumn(name ="product_id" , referencedColumnName = "id",nullable = false)
    private LoanProduct loanProduct;

    @OneToOne(optional = false)
    @JoinColumn(name ="charge_id",referencedColumnName = "id",nullable = false)
    private Charge charge;

    @Column (name ="amount_or_percentage",nullable = false)
    private BigDecimal amountOrPercentage;

    @Column (name ="self_share_percentage",nullable = false)
    private BigDecimal selfSharePercentage;

    @Column (name ="partner_share_percentage",nullable = false)
    private BigDecimal partnerSharePercentage;


    public LoanPenalForeclosureCharges(Loan loan, LoanProduct loanProduct, Charge charge, BigDecimal amountOrPercentage,BigDecimal selfShareAmount,BigDecimal partnerShareAmount) {

        this.loan = loan;
        this.loanProduct = loanProduct;
        this.charge = charge;
        this.amountOrPercentage = amountOrPercentage;
        this.selfSharePercentage= selfShareAmount;
        this.partnerSharePercentage = partnerShareAmount;
    }

//    public static Set<LoanPenalForeclosureCharges> retrieveForeclosurePenalCharges(Loan loan,LoanProduct loanProduct,List<Charge> foreclosurePenalCharges) {
//
//        Set<LoanPenalForeclosureCharges> loanPenalForeclosureCharges = new HashSet<>();
//
//        if(foreclosurePenalCharges.isEmpty()){
//            return null;
//        }else {
//            for (final Charge foreclosurePenalCharge : foreclosurePenalCharges) {
//                LoanPenalForeclosureCharges LoanProductFeesCharge = new LoanPenalForeclosureCharges(loan, loanProduct,foreclosurePenalCharge, foreclosurePenalCharge.getAmount(),foreclosurePenalCharge.getChargeTimeType());
//                loanPenalForeclosureCharges.add(LoanProductFeesCharge);
//            }
//            return loanPenalForeclosureCharges;
//        }



    public Loan getLoan() {
        return loan;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

    public Charge getCharge() {
        return charge;
    }

    public BigDecimal getAmountOrPercentage() {
        return amountOrPercentage;
    }

    public BigDecimal getSelfSharePercentage() {
        return selfSharePercentage;
    }

    public BigDecimal getPartnerSharePercentage() {
        return partnerSharePercentage;
    }
}
