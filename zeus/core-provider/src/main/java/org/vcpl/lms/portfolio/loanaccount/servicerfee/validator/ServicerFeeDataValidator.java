package org.vcpl.lms.portfolio.loanaccount.servicerfee.validator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.InvalidJsonException;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.Enum.ServicerFeeChargesRatio;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.constants.ServicerFeeConstants;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.ServicerFeeChargeData;
import org.vcpl.lms.portfolio.loanproduct.LoanProductConstants;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

@Component
public class ServicerFeeDataValidator {
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList(ServicerFeeConstants.PRODUCTID,ServicerFeeConstants.LOCALE,
            ServicerFeeConstants.VCL_INTEREST_ROUND,ServicerFeeConstants.VCL_INTEREST_DECIMAL,ServicerFeeConstants.SERVICER_FEE_ROUND,
            ServicerFeeConstants.SERVICER_FEE_DECIMAL,ServicerFeeConstants.SF_BASE_AMT_GST_LOSS_ENABLED,
            ServicerFeeConstants.SF_BASE_AMT_GST_LOSS,ServicerFeeConstants.SF_GST,ServicerFeeConstants.SF_GST_ROUND,
            ServicerFeeConstants.SF_GST_DECIMAL,ServicerFeeConstants.CURRENCY_CODE,ServicerFeeConstants.VCL_HURDLE_RATE,ServicerFeeConstants.SERVICER_FEE_CHARGE,ServicerFeeConstants.SF_CHARGE_ROUND,ServicerFeeConstants.SF_CHARGE_DECIMAL,ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_ROUNDINGMODE,ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_DECIMAL,ServicerFeeConstants.SF_CHARGE_GST_ROUNDINGMODE,ServicerFeeConstants.SF_CHARGE_GST_DECIMAL,ServicerFeeConstants.SF_CHARGE_GST,
            "servicerFeeChargesEnabled","dateFormat"));


    private final FromJsonHelper fromApiJsonHelper;

    public ServicerFeeDataValidator(FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }


    public void validateForCreate(String json, LoanProduct loanProduct) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(ServicerFeeConstants.SERVICER_FEE_RESOURCE);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if(loanProduct.isServicerFeeChargesConfigEnabled()){
            //charge base amount
            final String sfChargeBaseAmountRoundingmode = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_ROUNDINGMODE,element);
            baseDataValidator.reset().parameter("sfChargeBaseAmountRoundingmode").value(sfChargeBaseAmountRoundingmode).notBlank().ignoreIfNull().isRoundingModesExist(sfChargeBaseAmountRoundingmode);

            final Integer sfChargeBaseAmountDecimal = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_DECIMAL,element);
            baseDataValidator.reset().parameter("sfChargeBaseAmountDecimal").value(sfChargeBaseAmountDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);

            //charge gst
            final String sfChargeGstRoundingmode = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SF_CHARGE_GST_ROUNDINGMODE,element);
            baseDataValidator.reset().parameter("sfChargeGstRoundingmode").value(sfChargeGstRoundingmode).notBlank().ignoreIfNull().isRoundingModesExist(sfChargeGstRoundingmode);

            final Integer sfChargeGstDecimal = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SF_CHARGE_GST_DECIMAL,element);
            baseDataValidator.reset().parameter("sfChargeGstDecimal").value(sfChargeGstDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);

            final BigDecimal sfChargeGst = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(ServicerFeeConstants.SF_CHARGE_GST,element);
            baseDataValidator.reset().parameter("sfChargeGst").value(sfChargeGst).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(18));

            //invoice amount
            final String sfChargeRound = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SF_CHARGE_ROUND,element);
            baseDataValidator.reset().parameter("sfChargeRound").value(sfChargeRound).notBlank().ignoreIfNull().isRoundingModesExist(sfChargeRound);

            final Integer sfChargeDecimal = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SF_CHARGE_DECIMAL,element);
            baseDataValidator.reset().parameter("sfChargeDecimal").value(sfChargeDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);

            //charges
            final JsonArray chargesArray = this.fromApiJsonHelper.extractJsonArrayNamed(ServicerFeeConstants.SERVICER_FEE_CHARGE, element);
            baseDataValidator.reset().parameter(ServicerFeeConstants.SERVICER_FEE_CHARGE).value(chargesArray).jsonArrayNotEmpty();
        }

        if(loanProduct.isServicerFeeInterestConfigEnabled()){
            //interest base amount
            BigDecimal vclHurdleRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(ServicerFeeConstants.VCL_HURDLE_RATE,element);
            baseDataValidator.reset().parameter("vclHurdleRate").value(vclHurdleRate).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(50));

            String  vclInterestRound  = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.VCL_INTEREST_ROUND, element);
            baseDataValidator.reset().parameter("vclInterestRound").value(vclInterestRound).notBlank().ignoreIfNull().isRoundingModesExist(vclInterestRound);

            Integer vclInterestDecimal  = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.VCL_INTEREST_DECIMAL,element);
            baseDataValidator.reset().parameter("vclInterestDecimal").value(vclInterestDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);

            //base amount gst loss
            Boolean sfBaseAmtGstLossEnabled = this.fromApiJsonHelper.extractBooleanNamed(ServicerFeeConstants.SF_BASE_AMT_GST_LOSS_ENABLED,element);
            baseDataValidator.reset().parameter("sfBaseAmtGstLossEnabled").value(sfBaseAmtGstLossEnabled).notBlank().ignoreIfNull().validateForBooleanValue();

            if(!baseDataValidator.hasError() && sfBaseAmtGstLossEnabled.booleanValue()){
                BigDecimal sfBaseAmtGstLoss = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(ServicerFeeConstants.SF_BASE_AMT_GST_LOSS,element);
                baseDataValidator.reset().parameter("sfBaseAmtGstLoss").value(sfBaseAmtGstLoss).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(18));
            }

            //interest amount
            BigDecimal sfGst = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(ServicerFeeConstants.SF_GST,element);
            baseDataValidator.reset().parameter("sfGst").value(sfGst).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(18));

            String  sfGstRound  = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SF_GST_ROUND, element);
            baseDataValidator.reset().parameter("sfGstRound").value(sfGstRound).notBlank().ignoreIfNull().isRoundingModesExist(sfGstRound);

            Integer sfGstDecimal  = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SF_GST_DECIMAL,element);
            baseDataValidator.reset().parameter("sfGstDecimal").value(sfGstDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);

            //interest invoice calculation
            String  servicerFeeRound  = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SERVICER_FEE_ROUND, element);
            baseDataValidator.reset().parameter("servicerFeeRound").value(servicerFeeRound).notBlank().ignoreIfNull().isRoundingModesExist(servicerFeeRound);

            Integer servicerFeeDecimal  = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SERVICER_FEE_DECIMAL,element);
            baseDataValidator.reset().parameter("servicerFeeDecimal").value(servicerFeeDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);

        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);


    }

    public void validateForUpdate(String json,LoanProduct loanProduct) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(ServicerFeeConstants.SERVICER_FEE_RESOURCE);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if(loanProduct.isServicerFeeInterestConfigEnabled()){
            //interest base amount
            if (this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.VCL_HURDLE_RATE, element)) {
                final  BigDecimal vclHurdleRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(ServicerFeeConstants.VCL_HURDLE_RATE,element);
                baseDataValidator.reset().parameter("vclHurdleRate").value(vclHurdleRate).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(50));}

            if (this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.VCL_INTEREST_ROUND, element)){
                String  vclInterestRound  = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.VCL_INTEREST_ROUND, element);
                baseDataValidator.reset().parameter("vclInterestRound").value(vclInterestRound).notBlank().ignoreIfNull().isRoundingModesExist(vclInterestRound);}

            if (this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.VCL_INTEREST_DECIMAL, element)){
                Integer vclInterestDecimal  = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.VCL_INTEREST_DECIMAL,element);
                baseDataValidator.reset().parameter("vclInterestDecimal").value(vclInterestDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);}

            //base amount gst loss
            Boolean sfBaseAmtGstLossEnabled = this.fromApiJsonHelper.extractBooleanNamed(ServicerFeeConstants.SF_BASE_AMT_GST_LOSS_ENABLED,element);
            baseDataValidator.reset().parameter("sfBaseAmtGstLossEnabled").value(sfBaseAmtGstLossEnabled).notBlank().ignoreIfNull().validateForBooleanValue();

            if(!baseDataValidator.hasError() && sfBaseAmtGstLossEnabled.booleanValue()){
                if (this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_BASE_AMT_GST_LOSS, element)){
                    BigDecimal sfBaseAmtGstLoss = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(ServicerFeeConstants.SF_BASE_AMT_GST_LOSS,element);
                    baseDataValidator.reset().parameter("sfBaseAmtGstLoss").value(sfBaseAmtGstLoss).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(18));}
            }

            //interest amount
            if (this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_GST, element)){
                BigDecimal sfGst = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(ServicerFeeConstants.SF_GST,element);
                baseDataValidator.reset().parameter("sfGst").value(sfGst).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(18));}

            if (this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_GST_ROUND, element)){
                String  sfGstRound  = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SF_GST_ROUND, element);
                baseDataValidator.reset().parameter("sfGstRound").value(sfGstRound).notBlank().ignoreIfNull().isRoundingModesExist(sfGstRound);}

            if (this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_GST_DECIMAL, element)){
                Integer sfGstDecimal  = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SF_GST_DECIMAL,element);
                baseDataValidator.reset().parameter("sfGstDecimal").value(sfGstDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);}

            //interest invoice calculation
            if (this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SERVICER_FEE_ROUND, element)){
                String  servicerFeeRound  = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SERVICER_FEE_ROUND, element);
                baseDataValidator.reset().parameter("servicerFeeRound").value(servicerFeeRound).notBlank().ignoreIfNull().isRoundingModesExist(servicerFeeRound);}


            if (this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SERVICER_FEE_DECIMAL, element)){
                Integer servicerFeeDecimal  = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SERVICER_FEE_DECIMAL,element);
                baseDataValidator.reset().parameter("servicerFeeDecimal").value(servicerFeeDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);}
        }

        if(loanProduct.isServicerFeeChargesConfigEnabled()){

            //charge base amount
            if(this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_ROUNDINGMODE, element)){
                final String sfChargeBaseAmountRoundingmode = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_ROUNDINGMODE,element);
                baseDataValidator.reset().parameter("sfChargeBaseAmountRoundingmode").value(sfChargeBaseAmountRoundingmode).notBlank().ignoreIfNull().isRoundingModesExist(sfChargeBaseAmountRoundingmode);
            }

            if(this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_DECIMAL, element)){
                final Integer sfChargeBaseAmountDecimal = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SF_CHARGE_BASE_AMOUNT_DECIMAL,element);
                baseDataValidator.reset().parameter("sfChargeBaseAmountDecimal").value(sfChargeBaseAmountDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);
            }

            //charge gst
            if(this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_CHARGE_GST_ROUNDINGMODE, element)){
                final String sfChargeGstRoundingmode = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SF_CHARGE_GST_ROUNDINGMODE,element);
                baseDataValidator.reset().parameter("sfChargeGstRoundingmode").value(sfChargeGstRoundingmode).notBlank().ignoreIfNull().isRoundingModesExist(sfChargeGstRoundingmode);
            }

            if(this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_CHARGE_GST_DECIMAL, element)){
                final Integer sfChargeGstDecimal = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SF_CHARGE_GST_DECIMAL,element);
                baseDataValidator.reset().parameter("sfChargeGstDecimal").value(sfChargeGstDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);
            }

            if(this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_CHARGE_GST, element)){
                final BigDecimal sfChargeGst = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(ServicerFeeConstants.SF_CHARGE_GST,element);
                baseDataValidator.reset().parameter("sfChargeGst").value(sfChargeGst).notBlank().ignoreIfNull().positiveAmount().notGreaterThanMax(BigDecimal.valueOf(18));
            }

            //invoice amount
            if(this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_CHARGE_ROUND, element)){
                final String sfChargeRound = this.fromApiJsonHelper.extractStringNamed(ServicerFeeConstants.SF_CHARGE_ROUND,element);
                baseDataValidator.reset().parameter("sfChargeRound").value(sfChargeRound).notBlank().ignoreIfNull().isRoundingModesExist(sfChargeRound);
            }

            if(this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SF_CHARGE_DECIMAL, element)){
                final Integer sfChargeDecimal = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ServicerFeeConstants.SF_CHARGE_DECIMAL,element);
                baseDataValidator.reset().parameter("sfChargeDecimal").value(sfChargeDecimal).notBlank().ignoreIfNull().isOneOfTheseValues(0,1,2);
            }
            //charges
            if(this.fromApiJsonHelper.parameterExists(ServicerFeeConstants.SERVICER_FEE_CHARGE, element)){
                final JsonArray chargesArray = this.fromApiJsonHelper.extractJsonArrayNamed(ServicerFeeConstants.SERVICER_FEE_CHARGE, element);
                baseDataValidator.reset().parameter(ServicerFeeConstants.SERVICER_FEE_CHARGE).value(chargesArray).jsonArrayNotEmpty();
            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);


    }

    private void throwExceptionIfValidationWarningsExist(List<ApiParameterError> dataValidationErrors) {

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
