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
package org.vcpl.lms.portfolio.loanaccount.service;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.organisation.office.service.OfficeReadPlatformServiceImpl;
import org.vcpl.lms.portfolio.charge.domain.*;
import org.vcpl.lms.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.vcpl.lms.portfolio.client.domain.ClientRepositoryWrapper;
import org.vcpl.lms.portfolio.loanaccount.api.LoanApiConstants;
import org.vcpl.lms.portfolio.loanaccount.data.GstData;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesCharges;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesRepository;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductRepository;
import org.vcpl.lms.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class LoanChargeAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final ChargeRepositoryWrapper chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
    private final LoanProductRepository loanProductRepository;

    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final OfficeReadPlatformServiceImpl officeReadPlatformService;
    private final LoanProductFeesChargesRepository loanProductFeesChargesRepository;

    @Autowired
    public LoanChargeAssembler(final FromJsonHelper fromApiJsonHelper, final ChargeRepositoryWrapper chargeRepository,
                               final LoanChargeRepository loanChargeRepository, final LoanProductRepository loanProductRepository,final ClientRepositoryWrapper clientRepositoryWrapper,
                               final OfficeReadPlatformServiceImpl officeReadPlatformService,final LoanProductFeesChargesRepository loanProductFeesChargesRepository) {

        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chargeRepository = chargeRepository;
        this.loanChargeRepository = loanChargeRepository;
        this.loanProductRepository = loanProductRepository;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.officeReadPlatformService = officeReadPlatformService;
        this.loanProductFeesChargesRepository =loanProductFeesChargesRepository;
    }

    public List<LoanCharge> fromParsedJson(final JsonElement element, List<LoanDisbursementDetails> disbursementDetails, final List<GstData> gstData) {

        JsonArray jsonDisbursement = this.fromApiJsonHelper.extractJsonArrayNamed("disbursementData", element);
        List<Long> disbursementChargeIds = new ArrayList<>();

        if (jsonDisbursement != null && jsonDisbursement.size() > 0) {
            for (int i = 0; i < jsonDisbursement.size(); i++) {
                final JsonObject jsonObject = jsonDisbursement.get(i).getAsJsonObject();
                if (jsonObject != null && jsonObject.getAsJsonPrimitive(LoanApiConstants.loanChargeIdParameterName) != null) {
                    String chargeIds = jsonObject.getAsJsonPrimitive(LoanApiConstants.loanChargeIdParameterName).getAsString();
                    if (chargeIds != null) {
                        if (chargeIds.indexOf(",") != -1) {
                            Iterable<String> chargeId = Splitter.on(',').split(chargeIds);
                            for (String loanChargeId : chargeId) {
                                disbursementChargeIds.add(Long.parseLong(loanChargeId));
                            }
                        } else {
                            disbursementChargeIds.add(Long.parseLong(chargeIds));
                        }
                    }

                }
            }
        }

        final List<LoanCharge> loanCharges = new ArrayList<>();
        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed("productId", element);
        final LoanProduct loanProduct = this.loanProductRepository.findById(productId)
                .orElseThrow(() -> new LoanProductNotFoundException(productId));
        final Set<LoanProductFeesCharges> loanProductFeesCharges = loanProduct.getLoanProductFeesCharges();
        final boolean isMultiDisbursal = loanProduct.isMultiDisburseLoan();
        LocalDate expectedDisbursementDate = null;

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.has("charges") && topLevelJsonElement.get("charges").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();

                    final Long id = this.fromApiJsonHelper.extractLongNamed("id", loanChargeElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerNamed("chargeTimeType", loanChargeElement, locale);
                    final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerNamed("chargeCalculationType",
                            loanChargeElement, locale);
                    final LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed("dueDate", loanChargeElement, dateFormat,
                            locale);
                    final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerNamed("chargePaymentMode", loanChargeElement,
                            locale);
                    final BigDecimal selfShare = this.fromApiJsonHelper.extractBigDecimalNamed("selfShare", loanChargeElement, locale);
                    final BigDecimal partnerShare = this.fromApiJsonHelper.extractBigDecimalNamed("partnerShare", loanChargeElement, locale);
                    if (id == null) {
                        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeId);

                        if (chargeDefinition.isOverdueInstallment()) {

                            final String defaultUserMessage = "Installment charge cannot be added to the loan.";
                            throw new LoanChargeCannotBeAddedException("loanCharge", "overdue.charge", defaultUserMessage, null,
                                    chargeDefinition.getName());
                        }

                        ChargeTimeType chargeTime = null;
                        if (chargeTimeType != null) {
                            chargeTime = ChargeTimeType.fromInt(chargeTimeType);
                        }
                        ChargeCalculationType chargeCalculation = null;
                        if (chargeCalculationType != null) {
                            chargeCalculation = ChargeCalculationType.fromInt(chargeCalculationType);
                        }
                        ChargePaymentMode chargePaymentModeEnum = null;
                        if (chargePaymentMode != null) {
                            chargePaymentModeEnum = ChargePaymentMode.fromInt(chargePaymentMode);
                        }
                        if (!isMultiDisbursal) {
                            final LoanCharge loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition, principal, amount, chargeTime,
                                    chargeCalculation, dueDate, chargePaymentModeEnum, numberOfRepayments, selfShare, partnerShare, gstData);
                            loanCharge.updateGstLiabilityBySelfAndPartner(loanProduct);
                            loanCharges.add(loanCharge);
                        } else {
                            if (topLevelJsonElement.has("disbursementData") && topLevelJsonElement.get("disbursementData").isJsonArray()) {
                                final JsonArray disbursementArray = topLevelJsonElement.get("disbursementData").getAsJsonArray();
                                if (disbursementArray.size() > 0) {
                                    JsonObject disbursementDataElement = disbursementArray.get(0).getAsJsonObject();
                                    expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(
                                            LoanApiConstants.disbursementDateParameterName, disbursementDataElement, dateFormat, locale);
                                }
                            }

                            if (ChargeTimeType.DISBURSEMENT.getValue().equals(chargeDefinition.getChargeTimeType())) {
                                for (LoanDisbursementDetails disbursementDetail : disbursementDetails) {
                                    LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = null;
                                    if (chargeDefinition.isPercentageOfApprovedAmount()
                                            && disbursementDetail.expectedDisbursementDateAsLocalDate().equals(expectedDisbursementDate)) {
                                        final LoanCharge loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition, principal, amount,
                                                chargeTime, chargeCalculation, dueDate, chargePaymentModeEnum, numberOfRepayments, null, null, gstData);
                                        loanCharges.add(loanCharge);
                                        if (loanCharge.isTrancheDisbursementCharge()) {
                                            loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge,
                                                    disbursementDetail);
                                            loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                                        }
                                    } else {
                                        if (disbursementDetail.expectedDisbursementDateAsLocalDate().equals(expectedDisbursementDate)) {
                                            final LoanCharge loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition,
                                                    disbursementDetail.principal(), amount, chargeTime, chargeCalculation,
                                                    disbursementDetail.expectedDisbursementDateAsLocalDate(), chargePaymentModeEnum,
                                                    numberOfRepayments, null, null, gstData);
                                            loanCharges.add(loanCharge);
                                            if (loanCharge.isTrancheDisbursementCharge()) {
                                                loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge,
                                                        disbursementDetail);
                                                loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                                            }
                                        }
                                    }
                                }
                            } else if (ChargeTimeType.TRANCHE_DISBURSEMENT.getValue().equals(chargeDefinition.getChargeTimeType())) {
                                LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = null;
                                for (LoanDisbursementDetails disbursementDetail : disbursementDetails) {
                                    if (ChargeTimeType.TRANCHE_DISBURSEMENT.getValue().equals(chargeDefinition.getChargeTimeType())) {
                                        final LoanCharge loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition,
                                                disbursementDetail.principal(), amount, chargeTime, chargeCalculation,
                                                disbursementDetail.expectedDisbursementDateAsLocalDate(), chargePaymentModeEnum,
                                                numberOfRepayments, null, null, gstData);
                                        loanCharges.add(loanCharge);
                                        loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge, disbursementDetail);
                                        loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                                    }
                                }
                            } else {
                                final LoanCharge loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition, principal, amount,
                                        chargeTime, chargeCalculation, dueDate, chargePaymentModeEnum, numberOfRepayments, null, null, gstData);
                                loanCharges.add(loanCharge);
                            }
                        }
                    } else {
                        final Long loanChargeId = id;
                        final LoanCharge loanCharge = this.loanChargeRepository.findById(loanChargeId).orElse(null);
                        if (loanCharge != null) {
                            if (!loanCharge.isTrancheDisbursementCharge() || disbursementChargeIds.contains(loanChargeId)) {
                                loanCharge.update(amount, dueDate, numberOfRepayments);
                                loanCharges.add(loanCharge);
                            }
                        }
                    }
                }
            }
        }
        return loanCharges;
    }

    public Set<Charge> getNewLoanTrancheCharges(final JsonElement element) {
        final Set<Charge> associatedChargesForLoan = new HashSet<>();
        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            if (topLevelJsonElement.has("charges") && topLevelJsonElement.get("charges").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();
                    final Long id = this.fromApiJsonHelper.extractLongNamed("id", loanChargeElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    if (id == null) {
                        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeId);
                        if (chargeDefinition.getChargeTimeType().equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue())) {
                            associatedChargesForLoan.add(chargeDefinition);
                        }
                    }
                }
            }
        }
        return associatedChargesForLoan;
    }

   

    public List<Charge> retrieveCharge(JsonElement element) {

        final List<Charge> charge = new ArrayList<>();

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            if (topLevelJsonElement.has("charges") && topLevelJsonElement.get("charges").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();

                    final Long id = this.fromApiJsonHelper.extractLongNamed("id", loanChargeElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);

                    if (id == null) {
                        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeId);
                        charge.add(chargeDefinition);
                    }

                }
            }
        }
        return charge;
    }

    public Set<LoanPenalForeclosureCharges> retrievePenaltyAndForeclosureCharge(Loan loanApplication, LoanProduct loanProduct, JsonElement element) {

        Set<LoanPenalForeclosureCharges> loanPenalForeclosureCharges = new HashSet<>();
        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            if ((topLevelJsonElement.has(LoanApiConstants.overduechargeParameterName) && topLevelJsonElement.get(LoanApiConstants.overduechargeParameterName).isJsonArray())
                    || (topLevelJsonElement.has(LoanApiConstants.foreclosureChargesParameterName) && topLevelJsonElement.get(LoanApiConstants.foreclosureChargesParameterName).isJsonArray())
                    || (topLevelJsonElement.has(LoanApiConstants.bounceChargeParameterName) && topLevelJsonElement.get(LoanApiConstants.bounceChargeParameterName).isJsonArray())) {

                JsonArray array = new JsonArray();
                if(topLevelJsonElement.has(LoanApiConstants.overduechargeParameterName) && topLevelJsonElement.get(LoanApiConstants.overduechargeParameterName).isJsonArray()) {
                    array.addAll(topLevelJsonElement.get(LoanApiConstants.overduechargeParameterName).getAsJsonArray());
                }
                if(topLevelJsonElement.has(LoanApiConstants.foreclosureChargesParameterName) && topLevelJsonElement.get(LoanApiConstants.foreclosureChargesParameterName).isJsonArray()){
                    array.addAll(topLevelJsonElement.get(LoanApiConstants.foreclosureChargesParameterName).getAsJsonArray());}

                if(topLevelJsonElement.has(LoanApiConstants.bounceChargeParameterName) && topLevelJsonElement.get(LoanApiConstants.bounceChargeParameterName).isJsonArray()) {
                    array.addAll(topLevelJsonElement.get(LoanApiConstants.bounceChargeParameterName).getAsJsonArray());
                }

                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();

                    final Long id = this.fromApiJsonHelper.extractLongNamed("id", loanChargeElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    if (id == null) {


                        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeId);
                        JsonElement chargeAmount =loanChargeElement.get("amount");
                        BigDecimal amount =chargeAmount.getAsBigDecimal();
                        LoanProductFeesCharges charges = loanProductFeesChargesRepository.getByChargeAndLoanProduct(chargeDefinition,loanProduct);
                        LoanPenalForeclosureCharges LoanProductFeesCharge = new LoanPenalForeclosureCharges(loanApplication, loanProduct,chargeDefinition, amount,charges.getSelfShare(),charges.getPartnerShare());
                        loanPenalForeclosureCharges.add(LoanProductFeesCharge);
                    }

                }
            }
        }
        return loanPenalForeclosureCharges;
    }

   

    }

