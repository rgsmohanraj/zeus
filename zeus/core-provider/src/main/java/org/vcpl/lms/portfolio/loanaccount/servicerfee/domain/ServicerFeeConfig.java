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
package org.vcpl.lms.portfolio.loanaccount.servicerfee.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.constants.ServicerFeeConstants;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.ServicerFeeChargeData;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "m_servicer_fee_config")
public class ServicerFeeConfig extends AbstractPersistableCustom {

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "vcl_interest_round",nullable = true)
    private String vclInterestRound;

    @Column(name = "vcl_interest_decimal",nullable = true)
    private Integer vclInterestDecimal;

    @Column(name = "servicer_fee_round",nullable = true)
    private String servicerFeeRound;

    @Column(name = "servicer_fee_decimal",nullable = true)
    private Integer servicerFeeDecimal;

    @Column(name = "sf_base_amt_gst_loss_enabled",nullable = true)
    private boolean sfBaseAmtGstLossEnabled;

    @Column(name = "sf_base_amt_gst_loss",nullable = true)
    private BigDecimal sfBaseAmtGstLoss;

    @Column(name = "sf_gst",nullable = true)
    private BigDecimal sfGst;

    @Column(name = "sf_gst_round",nullable = true)
    private String sfGstRound;

    @Column(name = "sf_gst_decimal",nullable = true)
    private Integer sfGstDecimal;

    @Column(name = "vcl_hurdle_rate",nullable = true)
    private BigDecimal vclHurdleRate;

    @Column(name = "createdon_date",nullable = true)
    private Date createdDate;

    @Column(name = "modifiedon_date",nullable = true)
    private Date modifiedDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ServicerFeeConfig", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ServicerFeeChargesConfig> servicerFeeChargesConfig = new HashSet<>();
    public ServicerFeeConfig(LoanProduct loanProduct, String vclInterestRound, Integer vclInterestDecimal,
                             String servicerFeeRound, Integer servicerFeeDecimal, boolean sfBaseAmtGstLossEnabled, BigDecimal sfBaseAmtGstLoss,
                             BigDecimal sfGst, String sfGstRound, Integer sfGstDecimal, BigDecimal vclHurdleRate, List<ServicerFeeChargeData> servicerFeeCharge,String sfChargeRound, Integer sfChargeDecimal,String sfChargeBaseAmountRoundingmode,Integer sfChargeBaseAmountDecimal,String sfChargeGstRoundingmode,Integer sfChargeGstDecimal,BigDecimal sfChargeGst) {
        this.loanProduct = loanProduct;
        this.vclInterestRound = vclInterestRound;
        this.vclInterestDecimal = vclInterestDecimal;
        this.servicerFeeRound = servicerFeeRound;
        this.servicerFeeDecimal = servicerFeeDecimal;
        this.sfBaseAmtGstLossEnabled = sfBaseAmtGstLossEnabled;
        this.sfBaseAmtGstLoss = sfBaseAmtGstLoss;
        this.sfGst = sfGst;
        this.sfGstRound = sfGstRound;
        this.sfGstDecimal = sfGstDecimal;
        this.vclHurdleRate = vclHurdleRate;
        Set<ServicerFeeChargesConfig> servicerFeeChargesConfigs = new HashSet<>();
        servicerFeeCharge.stream().filter(productFeesCharges -> Objects.nonNull(productFeesCharges)).forEach(charge -> {
            servicerFeeChargesConfigs.add(new ServicerFeeChargesConfig(this,charge.charge(),charge.sfSelfShareCharge(),charge.sfPartnerShareCharge(),
                    charge.sfChargeAmtGstLossEnabled(),charge.sfChargeAmtGstLoss(),sfChargeRound,sfChargeDecimal,
                    sfChargeBaseAmountRoundingmode,sfChargeBaseAmountDecimal,sfChargeGstRoundingmode,sfChargeGstDecimal,sfChargeGst,charge.isActive(),charge.servicerFeeChargesRatio()));
        });
        this.servicerFeeChargesConfig =servicerFeeChargesConfigs;
        this.createdDate = DateUtils.getDateOfTenant();

    }

    public static ServicerFeeConfig createServicerFeeConfig(JsonCommand command, LoanProduct loanProduct,List<ServicerFeeChargeData> servicerFeeCharge) {

        BigDecimal vclHurdleRate = command.bigDecimalValueOfParameterNamed("vclHurdleRate");

        if(loanProduct.isServicerFeeInterestConfigEnabled()){
            String  vclInterestRound  = command.stringValueOfParameterNamed("vclInterestRound");
            Integer vclInterestDecimal  = command.integerValueOfParameterNamed("vclInterestDecimal");
            String  servicerFeeRound  = command.stringValueOfParameterNamed("servicerFeeRound");
            Integer servicerFeeDecimal  = command.integerValueOfParameterNamed("servicerFeeDecimal");
            boolean sfBaseAmtGstLossEnabled = command.booleanObjectValueOfParameterNamed("sfBaseAmtGstLossEnabled");
            BigDecimal sfBaseAmtGstLoss = BigDecimal.ZERO;
            if(sfBaseAmtGstLossEnabled == true){
                sfBaseAmtGstLoss = command.bigDecimalValueOfParameterNamed("sfBaseAmtGstLoss");
            }
            BigDecimal sfGst = command.bigDecimalValueOfParameterNamed("sfGst");
            String  sfGstRound  = command.stringValueOfParameterNamed("sfGstRound");
            Integer sfGstDecimal  = command.integerValueOfParameterNamed("sfGstDecimal");

             String sfChargeRound = command.stringValueOfParameterNamed("sfChargeRound");
             Integer sfChargeDecimal = command.integerValueOfParameterNamed("sfChargeDecimal");
             String sfChargeBaseAmountRoundingmode = command.stringValueOfParameterNamed("sfChargeBaseAmountRoundingmode");
             Integer sfChargeBaseAmountDecimal = command.integerValueOfParameterNamed("sfChargeBaseAmountDecimal");
             String sfChargeGstRoundingmode = command.stringValueOfParameterNamed("sfChargeGstRoundingmode");
             Integer sfChargeGstDecimal = command.integerValueOfParameterNamed("sfChargeGstDecimal");
             BigDecimal sfChargeGst = command.bigDecimalValueOfParameterNamed("sfChargeGst");
            return new ServicerFeeConfig(loanProduct,vclInterestRound,
                    vclInterestDecimal,servicerFeeRound,servicerFeeDecimal,sfBaseAmtGstLossEnabled,
                    sfBaseAmtGstLoss,sfGst,sfGstRound,sfGstDecimal,vclHurdleRate,servicerFeeCharge,sfChargeRound,sfChargeDecimal,sfChargeBaseAmountRoundingmode,sfChargeBaseAmountDecimal,sfChargeGstRoundingmode,sfChargeGstDecimal,sfChargeGst);
        }

        return null;
    }

    public static ServicerFeeConfig updateServicerFeeConfig(JsonCommand command,LoanProduct loanProduct,List<ServicerFeeChargeData> servicerFeeCharge) {

        final Boolean servicerFeeInterestConfigEnabled =  command.booleanObjectValueOfParameterNamed("servicerFeeInterestConfigEnabled");

        BigDecimal vclHurdleRate = command.bigDecimalValueOfParameterNamed("vclhurdlerate");

        if(loanProduct.isServicerFeeInterestConfigEnabled()){
            String  vclInterestRound  = command.stringValueOfParameterNamed("vclInterestRound");
            Integer vclInterestDecimal  = command.integerValueOfParameterNamed("vclInterestDecimal");
            String  servicerFeeRound  = command.stringValueOfParameterNamed("servicerFeeRound");
            Integer servicerFeeDecimal  = command.integerValueOfParameterNamed("servicerFeeDecimal");
            boolean sfBaseAmtGstLossEnabled = command.booleanObjectValueOfParameterNamed("sfBaseAmtGstLossEnabled");
            BigDecimal sfBaseAmtGstLoss = BigDecimal.ZERO;
            if(sfBaseAmtGstLossEnabled == true){
                sfBaseAmtGstLoss = command.bigDecimalValueOfParameterNamed("sfBaseAmtGstLoss");
            }
            BigDecimal sfGst = command.bigDecimalValueOfParameterNamed("sfGst");
            String  sfGstRound  = command.stringValueOfParameterNamed("sfGstRound");
            Integer sfGstDecimal  = command.integerValueOfParameterNamed("sfGstDecimal");
            String sfChargeRound = command.stringValueOfParameterNamed("sfChargeRound");
            Integer sfChargeDecimal = command.integerValueOfParameterNamed("sfChargeDecimal");
            String sfChargeBaseAmountRoundingmode = command.stringValueOfParameterNamed("sfChargeBaseAmountRoundingmode");
            Integer sfChargeBaseAmountDecimal = command.integerValueOfParameterNamed("sfChargeBaseAmountDecimal");
            String sfChargeGstRoundingmode = command.stringValueOfParameterNamed("sfChargeGstRoundingmode");
            Integer sfChargeGstDecimal = command.integerValueOfParameterNamed("sfChargeGstDecimal");
            BigDecimal sfChargeGst = command.bigDecimalValueOfParameterNamed("sfChargeGst");
            return new ServicerFeeConfig(loanProduct,vclInterestRound,
                    vclInterestDecimal,servicerFeeRound,servicerFeeDecimal,sfBaseAmtGstLossEnabled,
                    sfBaseAmtGstLoss,sfGst,sfGstRound,sfGstDecimal,vclHurdleRate,servicerFeeCharge,sfChargeRound,sfChargeDecimal,sfChargeBaseAmountRoundingmode,sfChargeBaseAmountDecimal,sfChargeGstRoundingmode,sfChargeGstDecimal,sfChargeGst);
        }

        return null;
    }

    public void changesForUpdate(JsonCommand command,LoanProduct loanProduct,List<ServicerFeeChargeData> servicerFeeCharge) {

        final String sfBaseAmtGstLossEnabled = ServicerFeeConstants.SF_BASE_AMT_GST_LOSS_ENABLED;
        if (command.isChangeInBooleanParameterNamed(sfBaseAmtGstLossEnabled, this.sfBaseAmtGstLossEnabled)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(sfBaseAmtGstLossEnabled);
            this.sfBaseAmtGstLossEnabled = newValue;
        }

        final String vclInterestRound = ServicerFeeConstants.VCL_INTEREST_ROUND;
        if (command.isChangeInStringParameterNamed(vclInterestRound, this.vclInterestRound)) {
            final String newValue = command.stringValueOfParameterNamed(vclInterestRound);
            this.vclInterestRound = newValue;
        }

        final String vclInterestDecimal = ServicerFeeConstants.VCL_INTEREST_DECIMAL;
        if (command.isChangeInIntegerParameterNamed(vclInterestDecimal, this.vclInterestDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(vclInterestDecimal);
            this.vclInterestDecimal = newValue;
        }


        final String servicerFeeRound = ServicerFeeConstants.SERVICER_FEE_ROUND;
        if (command.isChangeInStringParameterNamed(servicerFeeRound, this.servicerFeeRound)) {
            final String newValue = command.stringValueOfParameterNamed(servicerFeeRound);
            this.servicerFeeRound = newValue;
        }


        final String servicerFeeDecimal = ServicerFeeConstants.SERVICER_FEE_DECIMAL;
        if (command.isChangeInIntegerParameterNamed(servicerFeeDecimal, this.servicerFeeDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(servicerFeeDecimal);
            this.servicerFeeDecimal = newValue;
        }


        final String sfBaseAmtGstLoss = ServicerFeeConstants.SF_BASE_AMT_GST_LOSS;
        if (command.isChangeInBigDecimalParameterNamed(sfBaseAmtGstLoss, this.sfBaseAmtGstLoss)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(sfBaseAmtGstLoss);
            this.sfBaseAmtGstLoss = newValue;
        }

        final String sfGst = ServicerFeeConstants.SF_GST;
        if (command.isChangeInBigDecimalParameterNamed(sfGst, this.sfGst)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(sfGst);
            this.sfGst = newValue;
        }


        final String sfGstRound = ServicerFeeConstants.SF_GST_ROUND;
        if (command.isChangeInStringParameterNamed(sfGstRound, this.sfGstRound)) {
            final String newValue = command.stringValueOfParameterNamed(sfGstRound);
            this.sfGstRound = newValue;
        }

        final String sfGstDecimal = ServicerFeeConstants.SF_GST_DECIMAL;
        if (command.isChangeInIntegerParameterNamed(sfGstDecimal, this.sfGstDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(sfGstDecimal);
            this.sfGstDecimal = newValue;
        }

        final String vclHurdleRate = ServicerFeeConstants.VCL_HURDLE_RATE;
        if (command.isChangeInBigDecimalParameterNamed(vclHurdleRate, this.vclHurdleRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(vclHurdleRate);
            this.vclHurdleRate = newValue;
        }

        ServicerFeeChargesConfig servicerFeeChargesConfig = new ServicerFeeChargesConfig();
        servicerFeeChargesConfig.updateChanges(command);

        List<ServicerFeeChargeData> charges = null;
        if (Objects.nonNull(servicerFeeCharge)) {
            charges = new ArrayList<>();
            for (ServicerFeeChargesConfig existingCharge : this.servicerFeeChargesConfig) {
                for (ServicerFeeChargeData updatedCharge : servicerFeeCharge) {
                    if (existingCharge.getCharge().equals(updatedCharge)) {
                        charges.add(updatedCharge);
                        break;
                    }
                }
            }
        }
        Set<ServicerFeeChargesConfig> servicerFeeChargesConfigs = servicerFeeChargesConfig.updateServicerFeeCharge(this,servicerFeeCharge);
        if(!servicerFeeChargesConfigs.isEmpty()){
            this.servicerFeeChargesConfig = servicerFeeChargesConfigs;
        }

    }
}
