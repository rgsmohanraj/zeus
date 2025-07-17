package org.vcpl.lms.portfolio.loanaccount.data;

import java.time.LocalDate;
import java.util.Date;

public record OverdueLoanInstallment(Long loanId, Integer installmentNumber, Integer gracePeriod, LocalDate duedate, LocalDate dpdLastRunOn) {}
