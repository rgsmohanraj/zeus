package org.vcpl.lms.infrastructure.security.data;

import lombok.Data;

@Data
public class keycloakLogoutRequest {
    private String refresh_token;
    private Integer expires_in;
    private String scope;
}
