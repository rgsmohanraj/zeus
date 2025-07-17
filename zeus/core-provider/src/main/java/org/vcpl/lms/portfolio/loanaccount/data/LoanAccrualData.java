package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccrualData {
    private Long loanId;
    private Integer installment;
    private Integer accrualType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal accruedAmount;
    private BigDecimal selfAccruedAmount;
    private BigDecimal partnerAccruedAmount;
    private boolean reversed;
    private LocalDate postedDate;
}
