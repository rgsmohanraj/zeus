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
package org.vcpl.lms.portfolio.loanaccount.servicerfee.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.Enum.ServicerFeeChargesRatio;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.constants.ServicerFeeConstants;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.ServicerFeeChargeData;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.domain.ServicerFeeConfig;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.domain.ServicerFeeRepository;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.exception.ServicerFeeNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.validator.ServicerFeeDataValidator;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRepository;
import org.vcpl.lms.portfolio.loanproduct.exception.LoanProductNotFoundException;

import java.math.BigDecimal;
import java.util.*;

@Service
non-sealed public class ServicerFeeWritePlatformServiceImp implements ServicerFeeWritePlatformService {
    private static final Logger LOG = LoggerFactory.getLogger(ServicerFeeWritePlatformServiceImp.class);
    private final PlatformSecurityContext context;
    private final ServicerFeeDataValidator fromApiJsonDeserializer;
    private final ServicerFeeRepository ServicerFeeRepository;
    private final LoanProductRepository loanProductRepository;
    private final ChargeRepositoryWrapper chargeRepository;



    @Autowired
    public ServicerFeeWritePlatformServiceImp(PlatformSecurityContext context, ServicerFeeDataValidator fromApiJsonDeserializer, ServicerFeeRepository ServicerFeeRepository, LoanProductRepository loanProductRepository, ChargeRepositoryWrapper chargeRepository) {
        this.context = context;
        this.fromApiJsonDeserializer =fromApiJsonDeserializer;
        this.ServicerFeeRepository =ServicerFeeRepository;
        this.loanProductRepository = loanProductRepository;
        this.chargeRepository = chargeRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createServicerFeeConfiguration(JsonCommand command) {
        this.context.authenticatedUser();
        final Long productId =  command.longValueOfParameterNamed(ServicerFeeConstants.PRODUCTID);
        if(Objects.isNull(productId)){
            throw new PlatformApiDataValidationException(List.of(
                    ApiParameterError.parameterErrorWithValue("validation.msg." + ServicerFeeConstants.SERVICER_FEE_RESOURCE + "." + ServicerFeeConstants.PRODUCTID + ".cannot.be.null.or.blank",
                            "The parameter " + ServicerFeeConstants.PRODUCTID + " is mandatory." , ServicerFeeConstants.PRODUCTID , null, productId )));
        }
        final LoanProduct loanProduct = this.loanProductRepository.getReferenceById(productId);
        this.fromApiJsonDeserializer.validateForCreate(command.json(),loanProduct);
        final List<ServicerFeeChargeData> servicerFeeCharge = assembleListOfServicerFeeCharge(command);

        final ServicerFeeConfig servicerFeeConfig = ServicerFeeConfig.createServicerFeeConfig(command,loanProduct,servicerFeeCharge);
        loanProduct.setServicerFeeConfig(servicerFeeConfig);
        try {
            this.loanProductRepository.saveAndFlush(loanProduct);}
        catch (Exception e){
            LOG.error(e.getMessage());}
        return new CommandProcessingResultBuilder().
                withCommandId(command.commandId())
                .withEntityId(loanProduct.getId())
                .build();

    }

    @Transactional
    @Override
    public CommandProcessingResult updateServicerFeeConfiguration(JsonCommand command,final Long servicerFeeConfigId) {
        this.context.authenticatedUser();
        final Long productId =  command.longValueOfParameterNamed(ServicerFeeConstants.PRODUCTID);
        if(Objects.isNull(productId)){
            throw new PlatformApiDataValidationException(List.of(
                    ApiParameterError.parameterErrorWithValue("validation.msg." + ServicerFeeConstants.SERVICER_FEE_RESOURCE + "." + ServicerFeeConstants.PRODUCTID + ".cannot.be.null.or.blank",
                            "The parameter " + ServicerFeeConstants.PRODUCTID + " is mandatory." , ServicerFeeConstants.PRODUCTID , null, productId )));
        }
        final LoanProduct loanProduct = this.loanProductRepository.getReferenceById(productId);

        //update
        this.fromApiJsonDeserializer.validateForUpdate(command.json(),loanProduct);
        final ServicerFeeConfig ServicerFeeConfig = this.ServicerFeeRepository.findById(servicerFeeConfigId)
                .orElseThrow(() -> new ServicerFeeNotFoundException(servicerFeeConfigId));
        final List<ServicerFeeChargeData> servicerFeeCharge = assembleListOfServicerFeeCharge(command);
        ServicerFeeConfig.setModifiedDate(DateUtils.getDateOfTenant());
        ServicerFeeConfig.changesForUpdate(command,loanProduct,servicerFeeCharge);
        if(!loanProduct.isServicerFeeInterestConfigEnabled()){
            ServicerFeeConfig.getServicerFeeChargesConfig()
                    .stream()
                    .filter(Objects::nonNull).forEach(servicerFeeChargesConfig -> {
                        servicerFeeChargesConfig.setSfSelfShareCharge(null);
                        servicerFeeChargesConfig.setSfPartnerShareCharge(null);
                        servicerFeeChargesConfig.setSfChargeAmtGstLossEnabled(false);
                        servicerFeeChargesConfig.setSfChargeAmtGstLoss(null);
                        servicerFeeChargesConfig.setSfChargeRound(null);
                        servicerFeeChargesConfig.setSfChargeDecimal(null);
                        servicerFeeChargesConfig.setSfChargeBaseAmountRoundingmode(null);
                        servicerFeeChargesConfig.setSfChargeBaseAmountDecimal(null);
                        servicerFeeChargesConfig.setSfChargeGstRoundingmode(null);
                        servicerFeeChargesConfig.setSfChargeGstDecimal(null);
                        servicerFeeChargesConfig.setSfChargeGst(null);
                        servicerFeeChargesConfig.setIsActive(false);
                    });
        }
        try {
            this.ServicerFeeRepository.saveAndFlush(ServicerFeeConfig);}
        catch (Exception e){
            LOG.error(e.getMessage());}
        return new CommandProcessingResultBuilder().
                withCommandId(command.commandId())
                .withEntityId(loanProduct.getId())
                .build();

    }

    public  List<ServicerFeeChargeData> assembleListOfServicerFeeCharge(JsonCommand command) {

        Integer servicerFeeChargesRatio = null;
        final List<ServicerFeeChargeData> charges = new ArrayList<>();
        if (command.parameterExists(ServicerFeeConstants.SERVICER_FEE_CHARGE)) {
            final JsonArray chargesArray = command.arrayOfParameterNamed(ServicerFeeConstants.SERVICER_FEE_CHARGE);
            for(int n = 0; n < chargesArray.size(); n++) {
                JsonObject jsonObject = chargesArray.get(n).getAsJsonObject();
                if(jsonObject.has(ServicerFeeConstants.SERVICER_FEE_CHARGES_RATIO)){
                    JsonObject element = jsonObject.get(ServicerFeeConstants.SERVICER_FEE_CHARGES_RATIO).getAsJsonObject();
                    servicerFeeChargesRatio = element.get("id").getAsInt();
                }
                if(jsonObject.has("charge")){
                    JsonObject jsonElements = jsonObject.get("charge").getAsJsonObject();
                    Long id = jsonElements.get("id").getAsLong();
                    final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                    final BigDecimal sfSelfShareCharge = jsonObject.get(ServicerFeeConstants.SF_SELF_SHARE_CHARGE).getAsBigDecimal();
                    BigDecimal sfPartnerShareCharge = !jsonObject.get(ServicerFeeConstants.SF_PARTNER_SHARE_CHARGE).isJsonNull() ?
                            jsonObject.get(ServicerFeeConstants.SF_PARTNER_SHARE_CHARGE).getAsBigDecimal() : BigDecimal.ZERO;
                    if(Objects.nonNull(servicerFeeChargesRatio)){
                        sfPartnerShareCharge = ServicerFeeChargesRatio.getServicerFeeChargesRatio(servicerFeeChargesRatio).equals(ServicerFeeChargesRatio.DYNAMIC_SPLIT) ? BigDecimal.ZERO : sfPartnerShareCharge;
                    }
                    final Boolean sfChargeAmtGstLossEnabled = jsonObject.get(ServicerFeeConstants.SF_CHARGE_AMT_GST_LOSS_ENABLED).getAsBoolean();
                    final BigDecimal sfChargeAmtGstLoss = jsonObject.get(ServicerFeeConstants.SF_CHARGE_AMT_GST_LOSS).getAsBigDecimal();
                    final Boolean isActive = jsonObject.get(ServicerFeeConstants.IS_ACTIVE).getAsBoolean();
                    charges.add(new ServicerFeeChargeData(charge,sfSelfShareCharge,sfPartnerShareCharge,sfChargeAmtGstLossEnabled,sfChargeAmtGstLoss,isActive,servicerFeeChargesRatio));
                    }
                }
            }
        return charges;

    }
}
