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
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.constants.ServicerFeeConstants;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.ServicerFeeChargeData;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "m_servicer_fee_charges_config")
public class ServicerFeeChargesConfig extends AbstractPersistableCustom {

    @OneToOne
    @JoinColumn(name = "servicer_fee_config_id", nullable = false)
    private ServicerFeeConfig servicerFeeConfig;

    @OneToOne
    @JoinColumn(name = "charge_id", nullable = false)
    private Charge charge;

    @Column(name= "sf_self_share_charge", nullable = true)
    private BigDecimal sfSelfShareCharge;

    @Column(name= "sf_partner_share_charge", nullable = true)
    private BigDecimal sfPartnerShareCharge;

    @Column(name = "sf_charge_amt_gst_loss_enabled",nullable = true)
    private boolean sfChargeAmtGstLossEnabled;

    @Column(name = "sf_charge_amt_gst_loss",nullable = true)
    private BigDecimal sfChargeAmtGstLoss;

    @Column(name = "sf_charge_round",nullable = true)
    private String sfChargeRound;

    @Column(name = "sf_charge_decimal",nullable = true)
    private Integer sfChargeDecimal;

    @Column(name = "sf_charge_base_amount_roundingmode",nullable = true)
    private String sfChargeBaseAmountRoundingmode;

    @Column(name = "sf_charge_base_amount_decimal",nullable = true)
    private Integer sfChargeBaseAmountDecimal;

    @Column(name = "sf_charge_gst_roundingmode",nullable = true)
    private String sfChargeGstRoundingmode;

    @Column(name = "sf_charge_gst_decimal",nullable = true)
    private Integer sfChargeGstDecimal;

    @Column(name = "sf_charge_gst",nullable = true)
    private BigDecimal sfChargeGst;

    @Column(name= "is_active", nullable = true)
    private Boolean isActive;

    @Column(name ="servicer_Fee_charges_ratio" ,nullable = true)
    private Integer servicerFeeChargesRatio;

    public ServicerFeeChargesConfig( ServicerFeeConfig servicerFeeConfig, Charge charge,BigDecimal sfSelfShareCharge,
                                     BigDecimal sfPartnerShareCharge,Boolean sfChargeAmtGstLossEnabled,BigDecimal sfChargeAmtGstLoss,
                                     String sfChargeRound,Integer sfChargeDecimal,String sfChargeBaseAmountRoundingmode,
                                     Integer sfChargeBaseAmountDecimal,String sfChargeGstRoundingmode,Integer sfChargeGstDecimal,
                                     BigDecimal sfChargeGst,Boolean isActive,Integer servicerFeeChargesRatio) {
        this.servicerFeeConfig = servicerFeeConfig;
        this.charge = charge;
        this.sfSelfShareCharge = sfSelfShareCharge;
        this.sfPartnerShareCharge = sfPartnerShareCharge;
        this.sfChargeAmtGstLossEnabled = sfChargeAmtGstLossEnabled;
        this.sfChargeAmtGstLoss = sfChargeAmtGstLoss;
        this.sfChargeRound = sfChargeRound;
        this.sfChargeBaseAmountRoundingmode = sfChargeBaseAmountRoundingmode;
        this.sfChargeBaseAmountDecimal = sfChargeBaseAmountDecimal;
        this.sfChargeGstRoundingmode = sfChargeGstRoundingmode;
        this.sfChargeGstDecimal = sfChargeGstDecimal;
        this.sfChargeDecimal = sfChargeDecimal;
        this.sfChargeGst = sfChargeGst;
        this.isActive = isActive;
        this.servicerFeeChargesRatio = servicerFeeChargesRatio;
    }

    public void updateChanges(JsonCommand command) {

        final String sfChargeRound = ServicerFeeConstants.SF_CHARGE_ROUND;
        if (command.isChangeInStringParameterNamed(sfChargeRound,this.sfChargeRound)) {
            final String newValue = command.stringValueOfParameterNamed(sfChargeRound);
            this.sfChargeRound = newValue;
        }

        final String sfChargeDecimal = ServicerFeeConstants.SF_CHARGE_DECIMAL;
        if (command.isChangeInIntegerSansLocaleParameterNamed(sfChargeDecimal, this.sfChargeDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(sfChargeDecimal);
            this.sfChargeDecimal = newValue;
        }

        final String sfChargeBaseAmountRoundingmode = ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_ROUNDINGMODE;
        if (command.isChangeInStringParameterNamed(sfChargeBaseAmountRoundingmode,this.sfChargeBaseAmountRoundingmode)) {
            final String newValue = command.stringValueOfParameterNamed(sfChargeBaseAmountRoundingmode);
            this.sfChargeBaseAmountRoundingmode = newValue;
        }

        final String sfChargeBaseAmountDecimal = ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_DECIMAL;
        if (command.isChangeInIntegerSansLocaleParameterNamed(sfChargeBaseAmountDecimal, this.sfChargeBaseAmountDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(sfChargeBaseAmountDecimal);
            this.sfChargeBaseAmountDecimal = newValue;
        }

        final String sfChargeGstRoundingmode = ServicerFeeConstants.SF_CHARGE_GST_ROUNDINGMODE;
        if (command.isChangeInStringParameterNamed(sfChargeGstRoundingmode,this.sfChargeGstRoundingmode)) {
            final String newValue = command.stringValueOfParameterNamed(sfChargeGstRoundingmode);
            this.sfChargeGstRoundingmode = newValue;
        }

        final String sfChargeGstDecimal = ServicerFeeConstants.SF_CHARGE_GST_DECIMAL;
        if (command.isChangeInIntegerSansLocaleParameterNamed(sfChargeGstDecimal, this.sfChargeGstDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(sfChargeGstDecimal);
            this.sfChargeGstDecimal = newValue;
        }

        final String sfChargeGst = ServicerFeeConstants.SF_CHARGE_GST;
        if (command.isChangeInBigDecimalParameterNamed(sfChargeGst, this.sfChargeGst)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(sfChargeGst);
            this.sfChargeGst = newValue;
        }

    }

    public Set<ServicerFeeChargesConfig> updateServicerFeeCharge(ServicerFeeConfig servicerFeeConfig , List<ServicerFeeChargeData> servicerFeeCharge) {
        Set<ServicerFeeChargesConfig> servicerFeeChargesConfigs = new HashSet<>();
        if(Objects.nonNull(servicerFeeCharge)){
            servicerFeeCharge.stream().forEach(charge ->
                    servicerFeeChargesConfigs.add(new ServicerFeeChargesConfig(servicerFeeConfig,charge.charge(),charge.sfSelfShareCharge(),charge.sfPartnerShareCharge(),charge.sfChargeAmtGstLossEnabled(),charge.sfChargeAmtGstLoss(),
                            sfChargeRound,sfChargeDecimal,sfChargeBaseAmountRoundingmode,sfChargeBaseAmountDecimal,
                            sfChargeGstRoundingmode,sfChargeGstDecimal,sfChargeGst,charge.isActive(),charge.servicerFeeChargesRatio())));}
        return servicerFeeChargesConfigs;
    }
}
