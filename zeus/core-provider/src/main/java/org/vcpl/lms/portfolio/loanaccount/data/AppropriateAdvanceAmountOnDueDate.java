package org.vcpl.lms.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AppropriateAdvanceAmountOnDueDate(Long loanId, Integer installmentNumber, BigDecimal advanceAmount, LocalDate duedate) { }
