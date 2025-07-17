package org.vcpl.lms.infrastructure.security.data;

import lombok.Data;

@Data
public class KeycloakUser {

    private String username;
    private boolean enabled;
}
