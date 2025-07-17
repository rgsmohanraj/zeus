package org.vcpl.lms.portfolio.loanaccount.coolingoff.service;

import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CoolingOffWritePlatformService {
    CommandProcessingResult coolingOff(JsonCommand command);

    BigDecimal getCoolingOffAmount(Long loanId, LocalDate coolingOffDate);
}
