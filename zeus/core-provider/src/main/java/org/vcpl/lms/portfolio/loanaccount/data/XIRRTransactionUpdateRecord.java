package org.vcpl.lms.portfolio.loanaccount.data;

import java.time.LocalDate;

public record XIRRTransactionUpdateRecord(Long clientId, Long loanId, LocalDate dueDate){}
