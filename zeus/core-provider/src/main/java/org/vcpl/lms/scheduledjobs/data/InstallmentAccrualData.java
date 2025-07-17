package org.vcpl.lms.scheduledjobs.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class InstallmentAccrualData {
    private LocalDate accruedTill;
    private Integer installment;
    private LocalDate dueDate;
    private LocalDate fromDate;

    private BigDecimal clientInterest;
    private BigDecimal selfInterest;
    private BigDecimal partnerInterest;

    private BigDecimal clientInterestDueReceived;
    private BigDecimal selfInterestDueReceived;
    private BigDecimal partnerInterestDueReceived;

    private BigDecimal clientInterestAccrued;
    private BigDecimal selfInterestAccrued;
    private BigDecimal partnerInterestAccrued;

    private BigDecimal clientInterestAccruedButNotReceived;
    private BigDecimal selfInterestAccruedButNotReceived;
    private BigDecimal partnerInterestAccruedButNotReceived;
}
