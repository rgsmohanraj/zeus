package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.Data;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;

import java.util.List;
@Data
@Setter
public class PartnerandProductData
{

        private Long partnerId;
        private String partnerName;
        private List<ProductInfoData> productInfoDataList;
        private List<EnumOptionData> enumOptionData;






}
