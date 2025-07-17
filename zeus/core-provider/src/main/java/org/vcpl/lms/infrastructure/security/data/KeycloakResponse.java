package org.vcpl.lms.infrastructure.security.data;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class KeycloakResponse {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private Integer expires_in;
    private String scope;
    private String error;
    private String error_description;

}
