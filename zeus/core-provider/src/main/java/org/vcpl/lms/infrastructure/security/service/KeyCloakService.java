package org.vcpl.lms.infrastructure.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.vcpl.lms.infrastructure.security.api.AuthenticationApiResource;
import org.vcpl.lms.infrastructure.security.api.KeyCloakApiResource;
import org.vcpl.lms.infrastructure.security.data.KeyCloakRefreshTokenRequest;
import org.vcpl.lms.infrastructure.security.data.KeycloakResponse;
import org.vcpl.lms.portfolio.client.utils.AESEncryptionUtils;

@Service
public class KeyCloakService {
    @Value("${keycloak.client_id}")
    private String client_id;
    @Value("${keycloak.client_secret}")
    private String client_secret;

    @Value("${keycloak.token_url}")
    private String token_url;

    @Value("${keycloak.logout_url}")
    private String logout_url;
    @Autowired
    private PlatformSecurityContext platformSecurityContext;
    private static final Logger LOG = LoggerFactory.getLogger(KeyCloakApiResource.class);

    public ResponseEntity<?> getToken(AuthenticationApiResource.AuthenticateRequest credential) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", client_id);
        map.add("client_secret", client_secret);
        map.add("grant_type", "password");
        map.add("username", AESEncryptionUtils.decrypt(credential.username,false));
        map.add("password", AESEncryptionUtils.decrypt(credential.password,false));

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KeycloakResponse> response=null;

        try {
           response = restTemplate.postForEntity(token_url, httpEntity, KeycloakResponse.class);
        } catch (Exception e) {
           return ResponseEntity.status(401).body("Unauthorized");
    }
        LOG.info("User Name "+AESEncryptionUtils.decrypt(credential.username,false)+"| Method "+"POST"+"| URL lms/api/v1/jwt/token");
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);

    }
    public ResponseEntity<KeycloakResponse> getRefreshToken(KeyCloakRefreshTokenRequest request)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", client_id);
        map.add("client_secret", client_secret);
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", request.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KeycloakResponse> response = restTemplate.postForEntity(token_url, httpEntity, KeycloakResponse.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);


    }
    public ResponseEntity<?> logoutHandler(KeyCloakRefreshTokenRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", client_id);
        map.add("client_secret", client_secret);
        map.add("refresh_token", request.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KeycloakResponse> response=null;

        try {
            response = restTemplate.postForEntity(logout_url, httpEntity, KeycloakResponse.class);
        } catch (Exception e) {
            //throw new InvalidLoanStateTransitionException("Login Failes", "Exception occured password failed", e.getMessage());
            return ResponseEntity.status(401).body("Unauthorized");
        }

        LOG.info("User Id "+platformSecurityContext.authenticatedUser().getDisplayName()+"| Method "+"POST"+"| URL lms/api/v1/jwt/token/logout");
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);


    }
}
