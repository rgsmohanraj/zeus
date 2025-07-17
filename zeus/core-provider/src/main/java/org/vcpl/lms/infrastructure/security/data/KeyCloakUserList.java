package org.vcpl.lms.infrastructure.security.data;

import lombok.Data;

import java.util.List;
@Data
public class KeyCloakUserList {
    private List<KeycloakUser>keycloakUsers;
}
