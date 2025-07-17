package org.vcpl.lms.infrastructure.core.data;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.vcpl.client.domain.VpayCredential;

import java.util.Objects;

@Component
@Data
public class VPayCredentialManager {
    @Value("${vpay.keystore.password}")
    private String password;

    @Value("${vpay.access.url}")
    private String accessUrl;

    @Value("${vpay.keystore.url}")
    private String keyStoreUrl;

    public VpayCredential getVpayCredentialWithProductName(String productName) {
        return new VpayCredential(password,keyStoreUrl,accessUrl,productName);
    }

    public VpayCredential getVpayCredentialWithoutProductName() {
        return new VpayCredential(password,keyStoreUrl,accessUrl);
    }
}
