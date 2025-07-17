package org.vcpl.lms.portfolio.loanaccount.coolingoff.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.commands.annotation.CommandType;
import org.vcpl.lms.commands.handler.NewCommandSourceHandler;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.portfolio.loanaccount.coolingoff.service.CoolingOffWritePlatformServiceImpl;

@Service
@CommandType(entity = "LOAN", action = "COOLING_OFF")
public class CoolingOffCommandHandler implements NewCommandSourceHandler {
    private final CoolingOffWritePlatformServiceImpl coolingOffWritePlatformServiceImpl;
    @Autowired
    public CoolingOffCommandHandler(CoolingOffWritePlatformServiceImpl coolingOffWritePlatformServiceImpl) {
        this.coolingOffWritePlatformServiceImpl = coolingOffWritePlatformServiceImpl;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return coolingOffWritePlatformServiceImpl.coolingOff(command);

    }
}
