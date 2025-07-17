package org.vcpl.lms.scheduledjobs.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class LoanAccrualData {
    private Long loanId;
    private BigDecimal cumulativeClientInterestAccrued;
    private BigDecimal cumulativeSelfInterestAccrued;
    private BigDecimal cumulativePartnerInterestAccrued;
    private List<InstallmentAccrualData> installments;
}