package org.vcpl.lms.portfolio.loanaccount.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.bulkimport.constants.LoanConstants;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.organisation.office.data.OfficeGstData;
import org.vcpl.lms.organisation.office.domain.Office;
import org.vcpl.lms.organisation.office.service.OfficeReadPlatformServiceImpl;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.charge.domain.GstEnum;
import org.vcpl.lms.portfolio.client.domain.Client;
import org.vcpl.lms.portfolio.client.domain.ClientRepositoryWrapper;
import org.vcpl.lms.portfolio.loanaccount.data.GstData;
import org.vcpl.lms.portfolio.loanaccount.data.GstDataBuilder;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCharge;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanPenalForeclosureCharges;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


@AllArgsConstructor
@Service
public class GstServiceImpl implements GstService {

    private static final Logger LOG = LoggerFactory.getLogger(GstServiceImpl.class);

    @Autowired
    private final LoanChargeAssembler loanChargeAssembler;
    private final FromJsonHelper fromApiJsonHelper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final OfficeReadPlatformServiceImpl officeReadPlatformServiceimpl;

    private OfficeGstData retrieveGstData(Client clientDetail) {
        CodeValue stateCode = clientDetail.getState();
        final String clientState = stateCode.getLabel();
        Office office = clientDetail.getOffice();
        final Long officeId = office.getId();
        Collection<OfficeGstData> officeGstData = this.officeReadPlatformServiceimpl.retrieveOfficeGsts(officeId);

        BigDecimal cgstValue = BigDecimal.valueOf(0);
        BigDecimal sgstValue = BigDecimal.valueOf(0);
        BigDecimal igstValue = BigDecimal.valueOf(0);

        CodeValueData officeStateStates = null;
        for (OfficeGstData officeGst : officeGstData) {
            officeStateStates = officeGst.getState();
            final String officeState = officeStateStates.getName();
            igstValue = officeGst.getIgst();
            if (clientState.equalsIgnoreCase(officeState)) {
                cgstValue = officeGst.getCgst();
                sgstValue = officeGst.getSgst();
                igstValue = officeGst.getIgst();
                return new OfficeGstData(null, null, officeStateStates, cgstValue, sgstValue, igstValue);
            }
        }
        return new OfficeGstData(null, null, officeStateStates, cgstValue, sgstValue, igstValue);
    }


    private List<GstData> gstCalculation(List<Charge> loanCharges, OfficeGstData officeGstData, String clientState, BigDecimal principalAmount, LoanProduct loanProduct, JsonElement element, Set<LoanPenalForeclosureCharges> loanPenalForeclosureCharges) {
        final List<GstData> gstData = new ArrayList<>();
        CodeValueData officeGst = officeGstData.getState();
        String officeState = officeGst.getName();

        final BigDecimal cgstValue = officeGstData.getCgst();
        final BigDecimal igstValue = officeGstData.getIgst();
        final BigDecimal sgstValue = officeGstData.getSgst();
        final BigDecimal gstValue = cgstValue.add(sgstValue);

        if (equalState(clientState, officeState)) {
            for (Charge retrieveCharge : loanCharges) {
                final BigDecimal amountValue = Objects.nonNull(element) ? retrieveGstFromJson(retrieveCharge, element) : loanPenalForeclosureCharges
                        .stream()
                        .filter(charges -> charges.getCharge().isForeclosureCharge())
                        .map(LoanPenalForeclosureCharges::getAmountOrPercentage)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal amount = retrieveCharge.isFlatAmount() ? amountValue : GstServiceImpl.percentage(principalAmount, amountValue, retrieveCharge.getChargeDecimal(), retrieveCharge.getChargeRoundingMode());
                final Long id = retrieveCharge.getId();
                final Integer gstEnumId = retrieveCharge.getGst();
                final GstEnum gstEnum = GstEnum.fromInt(gstEnumId);
                if (retrieveCharge.enabelGst()) {
                    if ((gstEnum.toString()).equals(LoanConstants.INCLUSIVE)) {
                        final BigDecimal feeBaseAmount = BigDecimal.valueOf(amount.doubleValue() / ((BigDecimal.valueOf(100).doubleValue() + gstValue.doubleValue()) / BigDecimal.valueOf(100).doubleValue())).setScale(retrieveCharge.getChargeDecimal(), retrieveCharge.getChargeRoundingMode());
                        final BigDecimal cgstAmount = BigDecimal.valueOf(feeBaseAmount.doubleValue() * ((cgstValue.doubleValue()) / (BigDecimal.valueOf(100).doubleValue()))).setScale(retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal totalGst = amount.subtract(feeBaseAmount).setScale(retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal sgstAmount = totalGst.subtract(cgstAmount);
                        final BigDecimal selfGst = GstServiceImpl.percentage(totalGst, loanProduct.getGstLiabilityByVcpl(), retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal partnerGst = totalGst.subtract(selfGst);
                        GstData gstDatas = new GstDataBuilder().setId(id).setCgstAmount(cgstAmount).setSgstAmount(sgstAmount).
                                setUpdatedChargeAmount(amount.subtract(feeBaseAmount)).setTotalGst(totalGst).
                                setSelfGst(selfGst).setPartnerGst(partnerGst).getGstData();
                        gstData.add(gstDatas);
                    } else if ((gstEnum.toString()).equals(LoanConstants.EXCLUSIVE)) {
                        final BigDecimal cgstAmount = BigDecimal.valueOf(amount.doubleValue() * (cgstValue.doubleValue() / (BigDecimal.valueOf(100).doubleValue()))).setScale(retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal totalGst = GstServiceImpl.percentage(amount, gstValue, retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal reduceChargeAmount = BigDecimal.valueOf(0);
                        final BigDecimal sgstAmount = totalGst.subtract(cgstAmount);
                        final BigDecimal selfGst = GstServiceImpl.percentage(totalGst, loanProduct.getGstLiabilityByVcpl(), retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal partnerGst = totalGst.subtract(selfGst);
                        GstData exclusiveGstData = new GstDataBuilder().setId(id).setCgstAmount(cgstAmount).
                                setSgstAmount(sgstAmount).setUpdatedChargeAmount(reduceChargeAmount).
                                setTotalGst(totalGst).setSelfGst(selfGst).setPartnerGst(partnerGst).getGstData();
                        gstData.add(exclusiveGstData);
                    }
                }
            }
            return gstData;
        } else {
            for (Charge retrieveCharge : loanCharges) {
                final BigDecimal amountValue = Objects.nonNull(element) ? retrieveGstFromJson(retrieveCharge, element) : loanPenalForeclosureCharges
                        .stream()
                        .filter(charges -> charges.getCharge().isForeclosureCharge())
                        .map(LoanPenalForeclosureCharges::getAmountOrPercentage)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                final Integer gstEnumId = retrieveCharge.getGst();
                final GstEnum gstEnum = GstEnum.fromInt(gstEnumId);
                final Long id = retrieveCharge.getId();
                if (retrieveCharge.enabelGst()) {
                    BigDecimal amount = retrieveCharge.isFlatAmount() ? amountValue : GstServiceImpl.percentage(principalAmount, amountValue, retrieveCharge.getChargeDecimal(), retrieveCharge.getChargeRoundingMode());
                    if ((gstEnum.toString()).equals(LoanConstants.INCLUSIVE)) {
                        final BigDecimal igstFeeBaseAmount = BigDecimal.valueOf(amount.doubleValue() / ((BigDecimal.valueOf(100).doubleValue() + (igstValue.doubleValue())) / (BigDecimal.valueOf(100)).doubleValue())).setScale(retrieveCharge.getChargeDecimal(), retrieveCharge.getChargeRoundingMode());
                        final BigDecimal igstAmount = BigDecimal.valueOf(amount.doubleValue() - igstFeeBaseAmount.doubleValue()).setScale(retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal selfGst = GstServiceImpl.percentage(igstAmount, loanProduct.getGstLiabilityByVcpl(), retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal partnerGst = igstAmount.subtract(selfGst);
                        GstData inclusiveGstData = new GstDataBuilder().setId(id).setIgstAmount(igstAmount).setUpdatedChargeAmount(amount.subtract(igstFeeBaseAmount)).setTotalGst(igstAmount).setSelfGst(selfGst).setPartnerGst(partnerGst).getGstData();
                        gstData.add(inclusiveGstData);
                    } else if ((gstEnum.toString()).equals(LoanConstants.EXCLUSIVE)) {

                        final BigDecimal igstAmount = BigDecimal.valueOf(amount.doubleValue() * ((igstValue.doubleValue()) / (BigDecimal.valueOf(100).doubleValue()))).setScale(retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal reduceChargeAmount = BigDecimal.valueOf(0);
                        final BigDecimal selfGst = GstServiceImpl.percentage(igstAmount, loanProduct.getGstLiabilityByVcpl(), retrieveCharge.getGstDecimal(), retrieveCharge.getGstRoundingMode());
                        final BigDecimal partnerGst = igstAmount.subtract(selfGst);
                        GstData exclusiveGstData = new GstDataBuilder().setId(id).setIgstAmount(igstAmount).setUpdatedChargeAmount(reduceChargeAmount).setTotalGst(igstAmount).setSelfGst(selfGst).setPartnerGst(partnerGst).getGstData();
                        gstData.add(exclusiveGstData);

                    }
                }
            }
        }
        return gstData;
    }

    private BigDecimal retrieveGstFromJson(Charge retrieveCharge, JsonElement element) {

        BigDecimal amount = BigDecimal.valueOf(0);

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            if (topLevelJsonElement.has("charges") && topLevelJsonElement.get("charges").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();
                    final Long id = this.fromApiJsonHelper.extractLongNamed("id", loanChargeElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    if (id == null && chargeId.equals(retrieveCharge.getId())) {
                        JsonElement chargeAmount = loanChargeElement.get("amount");
                        amount = chargeAmount.getAsBigDecimal();
                    }
                }
            } else if (topLevelJsonElement.has("chargeId")) {
                JsonObject chargeAmount = element.getAsJsonObject();
                final JsonElement adhocCharge = chargeAmount.get("amount");
                amount = adhocCharge.getAsBigDecimal();

            }
        }
        return amount;
    }

    private boolean equalState(String clientState, String officeState) {
        return clientState.equalsIgnoreCase(officeState);
    }


    @Override
    public List<GstData> calculationOfGst(Long clientId, List<Charge> charges, BigDecimal principalAmount, LoanProduct loanProduct, JsonElement element, Set<LoanPenalForeclosureCharges> loanPenalForeclosureCharges) {
        Client clienData = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        OfficeGstData officeGstData = this.retrieveGstData(clienData);
        return this.gstCalculation(charges, officeGstData, clienData.getState().getLabel(), principalAmount, loanProduct, element, loanPenalForeclosureCharges);
    }


    public static BigDecimal percentage(BigDecimal amount, BigDecimal percentage, Integer decimalPlaces, RoundingMode roundingMode) {
        return BigDecimal.valueOf(amount.doubleValue() * (percentage.doubleValue() / 100)).setScale(decimalPlaces, roundingMode);
    }

    public BigDecimal getGstChargeAmount(LoanCharge loanCharge, BigDecimal principalAmount) {
        return loanCharge.getCharge().isFlatAmount() ? loanCharge.getAmount() :
                GstServiceImpl.percentage(principalAmount, loanCharge.getAmount(),
                        loanCharge.getCharge().getChargeDecimal(), loanCharge.getCharge().getChargeRoundingMode());
    }
    public GstData calculateGstPostDisbursementCharges(LoanCharge loanCharge) {
        LOG.debug("Gst Calculation Initiated For Loan {}", loanCharge.getLoan().getId());

        Client clienData = this.clientRepositoryWrapper.findOneWithNotFoundDetection(loanCharge.getLoan().getClientId());

        OfficeGstData officeGstData = retrieveGstData(clienData);

        CodeValueData officeGst = officeGstData.getState();

        String officeState = officeGst.getName();
        final Integer gstEnumId = loanCharge.getCharge().getGst();
        final GstEnum gstEnum = GstEnum.fromInt(gstEnumId);
        return equalState(loanCharge.getLoan().getClient().getState().getLabel(), officeState) ? getSameStateGstInclusiveAndExclusive(loanCharge,gstEnum,officeGstData):

                getOtherStateInclusiveAndExclusive(loanCharge,gstEnum,officeGstData);


    }
    private GstData getOtherStateInclusiveAndExclusive(LoanCharge loanCharge, GstEnum gstEnum, OfficeGstData officeGstData) {
        final BigDecimal igstValue = officeGstData.getIgst();

        Charge charge = loanCharge.getCharge();

        GstData gstData = new GstDataBuilder().getGstData();

        LoanProduct loanProduct = loanCharge.getLoan().getLoanProduct();

        BigDecimal amount = getGstChargeAmount(loanCharge,loanCharge.getLoan().getPrincpal().getAmount());
        switch (gstEnum){
            case INCLUSIVE -> gstData = otherStateInclusive(igstValue, charge, amount,loanProduct);
            case EXCLUSIVE -> gstData = otherStateExclusive(igstValue, charge, amount,loanProduct);

        }
        return gstData;

    }
    private GstData otherStateExclusive(BigDecimal igstValue, Charge charge, BigDecimal amount,LoanProduct loanProduct) {

        GstData gstData;
        final BigDecimal igstAmount = BigDecimal.valueOf(amount.doubleValue() * ((igstValue.doubleValue()) / (BigDecimal.valueOf(100).doubleValue()))).setScale(charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal reduceChargeAmount = BigDecimal.valueOf(0);
        final BigDecimal selfGst = GstServiceImpl.percentage(igstAmount, loanProduct.getGstLiabilityByVcpl(), charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal partnerGst = igstAmount.subtract(selfGst);
        LOG.info("GST Calculate For Other State Exclusive");

        gstData = new GstDataBuilder().setId(charge.getId()).setIgstAmount(igstAmount).

                setUpdatedChargeAmount(reduceChargeAmount).setTotalGst(igstAmount).setSelfGst(selfGst)

                .setPartnerGst(partnerGst).getGstData();
        return gstData;

    }
    private GstData otherStateInclusive(BigDecimal igstValue, Charge charge, BigDecimal amount, LoanProduct loanProduct) {

        GstData gstData;
        final BigDecimal igstFeeBaseAmount = BigDecimal.valueOf(amount.doubleValue() / ((BigDecimal.valueOf(100).doubleValue() + (igstValue.doubleValue())) / (BigDecimal.valueOf(100)).doubleValue())).setScale(charge.getChargeDecimal(), charge.getChargeRoundingMode());
        final BigDecimal igstAmount = BigDecimal.valueOf(amount.doubleValue() - igstFeeBaseAmount.doubleValue()).setScale(charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal selfGst = GstServiceImpl.percentage(igstAmount, loanProduct.getGstLiabilityByVcpl(), charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal partnerGst = igstAmount.subtract(selfGst);
        LOG.info("GST Calculate For Other State Inclusive");

        gstData = new GstDataBuilder().setId(charge.getId()).setIgstAmount(igstAmount).setUpdatedChargeAmount(amount.subtract(igstFeeBaseAmount))

                .setTotalGst(igstAmount).setSelfGst(selfGst).setPartnerGst(partnerGst).getGstData();
        return gstData;

    }
    private GstData getSameStateGstInclusiveAndExclusive(LoanCharge loanCharge,GstEnum gstEnum,OfficeGstData officeGstData) throws ArithmeticException {
        final BigDecimal cgstValue = officeGstData.getCgst();
        final BigDecimal sgstValue = officeGstData.getSgst();
        final BigDecimal gstValue = cgstValue.add(sgstValue);

        Charge charge = loanCharge.getCharge();

        BigDecimal amount = getGstChargeAmount(loanCharge,loanCharge.getLoan().getPrincpal().getAmount());

        GstData gstData = new GstDataBuilder().getGstData();
        switch (gstEnum){
            case INCLUSIVE -> gstData = inclusiveSameState( cgstValue, gstValue, charge, amount,loanCharge.getLoan().getLoanProduct());
            case EXCLUSIVE -> gstData = exclusiveSameState(cgstValue, gstValue, charge, amount,loanCharge.getLoan().getLoanProduct());

        }
        return gstData;

    }
    private GstData exclusiveSameState(BigDecimal cgstValue, BigDecimal gstValue, Charge charge, BigDecimal amount,LoanProduct loanProduct) {
        final BigDecimal cgstAmount = BigDecimal.valueOf(amount.doubleValue() * (cgstValue.doubleValue() / (BigDecimal.valueOf(100).doubleValue()))).setScale(charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal totalGst = GstServiceImpl.percentage(amount, gstValue, charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal reduceChargeAmount = BigDecimal.valueOf(0);
        final BigDecimal sgstAmount = totalGst.subtract(cgstAmount);
        final BigDecimal selfGst = GstServiceImpl.percentage(totalGst, loanProduct.getGstLiabilityByVcpl(), charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal partnerGst = totalGst.subtract(selfGst);
        LOG.info("GST Calculate For Same State Exclusive");
        return new GstDataBuilder().setId(charge.getId()).setCgstAmount(cgstAmount).

                setSgstAmount(sgstAmount).setUpdatedChargeAmount(reduceChargeAmount).

                setTotalGst(totalGst).setSelfGst(selfGst).setPartnerGst(partnerGst).getGstData();

    }
    private GstData inclusiveSameState( BigDecimal cgstValue, BigDecimal gstValue, Charge charge, BigDecimal amount,LoanProduct loanProduct) {
        final BigDecimal feeBaseAmount = BigDecimal.valueOf(amount.doubleValue() / ((BigDecimal.valueOf(100).doubleValue() + gstValue.doubleValue()) / BigDecimal.valueOf(100).doubleValue())).setScale(charge.getChargeDecimal(), charge.getChargeRoundingMode());
        final BigDecimal cgstAmount = BigDecimal.valueOf(feeBaseAmount.doubleValue() * ((cgstValue.doubleValue()) / (BigDecimal.valueOf(100).doubleValue()))).setScale(charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal totalGst = amount.subtract(feeBaseAmount).setScale(charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal sgstAmount = totalGst.subtract(cgstAmount);
        final BigDecimal selfGst = GstServiceImpl.percentage(totalGst, loanProduct.getGstLiabilityByVcpl(), charge.getGstDecimal(), charge.getGstRoundingMode());
        final BigDecimal partnerGst = totalGst.subtract(selfGst);
        LOG.info("GST Calculate For Same State Inclusive");
        return new GstDataBuilder().setId(charge.getId()).setCgstAmount(cgstAmount).setSgstAmount(sgstAmount).

                setUpdatedChargeAmount(amount.subtract(feeBaseAmount)).setTotalGst(totalGst).

                setSelfGst(selfGst).setPartnerGst(partnerGst).getGstData();

    }
}
