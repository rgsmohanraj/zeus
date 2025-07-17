package org.vcpl.lms.portfolio.client.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vcpl.lms.commands.annotation.CommandType;
import org.vcpl.lms.commands.handler.NewCommandSourceHandler;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.portfolio.client.service.ClientWritePlatformService;

@RequiredArgsConstructor
@Service
@CommandType(entity = "AADHAAR", action = "UPDATE")
public class UpdateClientAadhaarCommandHandler implements NewCommandSourceHandler {

    private final ClientWritePlatformService clientWritePlatformService;

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.clientWritePlatformService.updateClientAadhaar(command.getClientId(), command);
    }
}
