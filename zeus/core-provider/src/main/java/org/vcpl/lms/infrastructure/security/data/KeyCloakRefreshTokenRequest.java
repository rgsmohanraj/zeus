package org.vcpl.lms.infrastructure.security.data;

import lombok.Data;

@Data
public class KeyCloakRefreshTokenRequest {

    private String refreshToken;
}
