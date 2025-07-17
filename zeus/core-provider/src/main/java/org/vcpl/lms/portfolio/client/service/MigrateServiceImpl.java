package org.vcpl.lms.portfolio.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vcpl.lms.portfolio.client.exception.AadhaarMigrationInterruptException;
import org.vcpl.lms.portfolio.client.utils.AESEncryptionUtils;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MigrateServiceImpl implements MigrationService{
    private final ClientReadPlatformService clientReadPlatformService;
    private final ClientWritePlatformService clientWritePlatformService;
    @Override
    public void migrateEncryptStrategy() {
        Map<Long,String> clientMap = clientReadPlatformService.retrieveEncryptedAadhaar();
        clientMap.forEach((id, encrypted) -> {
            if(Objects.nonNull(encrypted)) {
                String decryptedAadhaar = AESEncryptionUtils.decrypt(encrypted,true);
                String encryptedAadhaar = "";
                try {
                    assert decryptedAadhaar != null;
                    encryptedAadhaar = AESEncryptionUtils.encryptWithKey(decryptedAadhaar);
                }
                catch (Exception e) {
                    throw new AadhaarMigrationInterruptException(String.format("Migration of aadhaar failed for client %d", id));
                }
                clientWritePlatformService.updateAadhaar(encryptedAadhaar,id);
            }
        });

    }
}
