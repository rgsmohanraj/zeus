package org.vcpl.lms.portfolio.loanaccount.servicerfee.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vcpl.lms.commands.annotation.CommandType;
import org.vcpl.lms.commands.handler.NewCommandSourceHandler;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeWritePlatformService;

@Service
@CommandType(entity = "SERVICERFEECONFIG", action = "UPDATE")
public class UpdateServicerFeeCommandHandler implements NewCommandSourceHandler {


    private final ServicerFeeWritePlatformService writePlatformService;


    @Autowired
    public UpdateServicerFeeCommandHandler(ServicerFeeWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return this.writePlatformService.updateServicerFeeConfiguration(command,command.entityId());
    }
}
