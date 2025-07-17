package org.vcpl.lms.portfolio.client.api;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RestController;
import org.vcpl.lms.portfolio.client.service.MigrationService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Path("/migrate")
public class MigrationApiResources {
    private final MigrationService migrationService;
    @POST
    @ApiResponse(responseCode = "200", description = "OK")
    public void migrate() throws IllegalBlockSizeException, NoSuchPaddingException,
            BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        migrationService.migrateEncryptStrategy();
    }
}
