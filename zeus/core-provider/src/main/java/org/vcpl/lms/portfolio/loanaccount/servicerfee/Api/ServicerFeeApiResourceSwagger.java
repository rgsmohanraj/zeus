package org.vcpl.lms.portfolio.loanaccount.servicerfee.Api;
import io.swagger.v3.oas.annotations.media.Schema;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.data.ServicerFeeChargeData;
import java.math.BigDecimal;


final class ServicerFeeApiResourceSwagger {
    private ServicerFeeApiResourceSwagger() {}
    @Schema(description = "PostServicerFeeRequest")
    public static final class PostServicerFeeRequest {

        @Schema(example = "1")
        public String productId;

        @Schema(example = "1")
        public String vclInterestRound;

        @Schema(example = "20")
        public Integer vclInterestDecimal;

        @Schema(example = "19")
        public String servicerFeeRound;

        @Schema(example = "10")
        public Integer servicerFeeDecimal;

        @Schema(example = "true")
        public boolean sfBaseAmtGstLossEnabled;

        @Schema(example = "9")
        public BigDecimal sfBaseAmtGstLoss;

        @Schema(example = "1.3")
        public BigDecimal sfGst;

        @Schema(example = "1")
        public String sfGstRound;

        @Schema(example = "1")
        public Integer sfGstDecimal;

        @Schema(example = "16")
        public BigDecimal vclHurdleRate;

        @Schema(example = "1")
        public BigDecimal sfChargeGst;

        @Schema(example = "1")
        public String sfChargeRound;

        @Schema(example = "1")
        public BigDecimal sfChargeDecimal;

        @Schema(example = "1")
        public String sfChargeBaseAmountRoundingmode;

        @Schema(example = "1")
        public Integer sfChargeBaseAmountDecimal;

        @Schema(example = "1")
        public String sfChargeGstRoundingmode;

        @Schema(example = "1")
        public Integer sfChargeGstDecimal;

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

        @Schema(example = "en_GB")
        public String locale;
        public ServicerFeeChargeData servicerFeeChargeData;
    }
    @Schema(description = "PostServicerFeeResponse")
    public static final class PostServicerFeeResponse {

        private PostServicerFeeResponse() {}

        @Schema(example = "2")
        public Integer resourceId;
    }

    @Schema(description = "GetServicerFeeResponse")
    public class GetServicerFeeResponse {

        @Schema(example = "1")
        public String productId;

    }

    @Schema(description = "PutServicerFeeIdRequest")
    public static final class PutServicerFeeIdRequest {

        private PutServicerFeeIdRequest() {}
        @Schema(example = "1")
        public String productId;

        @Schema(example = "1")
        public String vclInterestRound;

        @Schema(example = "20")
        public Integer vclInterestDecimal;

        @Schema(example = "19")
        public String servicerFeeRound;

        @Schema(example = "10")
        public Integer servicerFeeDecimal;

        @Schema(example = "true")
        public boolean sfBaseAmtGstLossEnabled;

        @Schema(example = "9")
        public BigDecimal sfBaseAmtGstLoss;

        @Schema(example = "1.3")
        public BigDecimal sfGst;

        @Schema(example = "1")
        public String sfGstRound;

        @Schema(example = "1")
        public Integer sfGstDecimal;

        @Schema(example = "16")
        public BigDecimal vclHurdleRate;

        @Schema(example = "1")
        public BigDecimal sfChargeGst;

        @Schema(example = "1")
        public String sfChargeRound;

        @Schema(example = "1")
        public BigDecimal sfChargeDecimal;

        @Schema(example = "1")
        public String sfChargeBaseAmountRoundingmode;

        @Schema(example = "1")
        public Integer sfChargeBaseAmountDecimal;

        @Schema(example = "1")
        public String sfChargeGstRoundingmode;

        @Schema(example = "1")
        public Integer sfChargeGstDecimal;

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

        @Schema(example = "en_GB")
        public String locale;

    }

    @Schema(description = "PutServicerFeeIdResponse")
    public static final class PutServicerFeeIdResponse {

        private PutServicerFeeIdResponse() {}


        @Schema(example = "1")
        public Integer resourceId;

        public PutServicerFeeIdRequest changes;

    }
}
