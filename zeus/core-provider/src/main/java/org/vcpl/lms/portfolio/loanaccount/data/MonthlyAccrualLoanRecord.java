package org.vcpl.lms.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyAccrualLoanRecord(Long id, Integer installment, LocalDate disbursementDate, LocalDate fromDate, LocalDate dueDate,
                                       BigDecimal principalOutstanding, BigDecimal annualNominalInterestRate) {
}
