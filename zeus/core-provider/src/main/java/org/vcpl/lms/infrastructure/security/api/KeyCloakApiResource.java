package org.vcpl.lms.infrastructure.security.api;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.vcpl.lms.infrastructure.security.data.KeyCloakRefreshTokenRequest;
import org.vcpl.lms.infrastructure.security.data.KeycloakResponse;
import org.vcpl.lms.infrastructure.security.filter.UserActivityFilter;
import org.vcpl.lms.infrastructure.security.service.KeyCloakService;

@Path("/jwt/token")
@Component
@Scope("singleton")
@Tag(name = "KeyCloaks", description = "Non-core reports can be added, updated and deleted.")
public class KeyCloakApiResource {

    private final KeyCloakService keyCloakService;


    public KeyCloakApiResource(KeyCloakService keyCloakService) {
        this.keyCloakService = keyCloakService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Verify authentication", description = "Authenticates the credentials provided and returns the set roles and permissions allowed.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AuthenticationApiResourceSwagger.PostAuthenticationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AuthenticationApiResourceSwagger.PostAuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login") })
    public ResponseEntity<?> authenticate(@Parameter(hidden = true) final String apiRequestBodyAsJson,
                                                         @QueryParam("returnClientList") @DefaultValue("false") boolean returnClientList) {
        AuthenticationApiResource.AuthenticateRequest request = new Gson().fromJson(apiRequestBodyAsJson, AuthenticationApiResource.AuthenticateRequest.class);
        if (request == null) {
            throw new IllegalArgumentException(
                    "Invalid JSON in BODY (no longer URL param;) of POST to /authentication: " + apiRequestBodyAsJson);
        }
        if (request.username == null || request.password == null) {
            throw new IllegalArgumentException("Username or Password is null in JSON of POST to /authentication: "
                    + apiRequestBodyAsJson + "; username=" + request.username + ", password=" + request.password);
        }

           return keyCloakService.getToken(request);
    }
    @POST
    @Path("/refresh")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Verify authentication", description = "Authenticates the credentials provided and returns the set roles and permissions allowed.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AuthenticationApiResourceSwagger.PostAuthenticationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AuthenticationApiResourceSwagger.PostAuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login") })
    public ResponseEntity<KeycloakResponse> getRefreshToken(@Parameter(hidden = true) final String apiRequestBodyAsJson,
                                                         @QueryParam("returnClientList") @DefaultValue("false") boolean returnClientList) {
        KeyCloakRefreshTokenRequest request = new Gson().fromJson(apiRequestBodyAsJson, KeyCloakRefreshTokenRequest.class);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Invalid JSON in BODY (no longer URL param;) of POST to /authentication: " + apiRequestBodyAsJson);
        }
            return keyCloakService.getRefreshToken(request);
    }

    @POST
    @Path("/logout")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Logout Handler", description = "Logout the User.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AuthenticationApiResourceSwagger.PostAuthenticationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AuthenticationApiResourceSwagger.PostAuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login") })
    public ResponseEntity<?> logoutHandler(@Parameter(hidden = true) final String apiRequestBodyAsJson,
                                                            @QueryParam("returnClientList") @DefaultValue("false") boolean returnClientList) {
        KeyCloakRefreshTokenRequest request = new Gson().fromJson(apiRequestBodyAsJson, KeyCloakRefreshTokenRequest.class);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Invalid JSON in BODY (no longer URL param;) of POST to /authentication: " + apiRequestBodyAsJson);
        }
        return keyCloakService.logoutHandler(request);
    }

    }

