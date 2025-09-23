package com.springboot.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class UserController {

    private final JwtDecoder idTokenDecoder;
    private final String clientId;

    public UserController(
            @Value("${cognito.issuer-uri}") String issuer,
            @Value("${cognito.client-id}") String clientId
    ) {
        this.idTokenDecoder = JwtDecoders.fromIssuerLocation(issuer);
        this.clientId = Objects.requireNonNull(clientId).trim();
    }

    @GetMapping("/GetUser")
    public Map<String, Object> getUser(
            @RequestHeader(value = "X-ID-Token", required = false) String idTokenHeader,
            @RequestParam(value = "id_token", required = false) String idTokenQuery
    ) {
        String idToken = (idTokenHeader != null && !idTokenHeader.isBlank())
                ? idTokenHeader
                : idTokenQuery;

        if (idToken == null || idToken.isBlank()) {
            throw new BadRequest("Missing ID token (X-ID-Token header or id_token query)");
        }

        Jwt jwt = idTokenDecoder.decode(idToken);

        String tokenUse = jwt.getClaimAsString("token_use");
        if (!"id".equals(tokenUse)) {
            throw new BadRequest("Provided token is not an ID token");
        }

        List<String> aud = jwt.getAudience();
        String clientIdClaim = jwt.getClaimAsString("client_id");
        boolean audOk = (aud != null && aud.contains(clientId));
        boolean clientOk = clientId.equals(clientIdClaim);
        if (!(audOk || clientOk)) {
            throw new BadRequest("ID token audience/client_id mismatch");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null) {
            throw new BadRequest("ID token has no 'email' claim");
        }

        return Map.of(
                "email", email,
                "sub", jwt.getSubject(),
                "email_verified", jwt.getClaimAsBoolean("email_verified")
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequest.class)
    public Map<String, String> handleBadRequest(BadRequest ex) {
        return Map.of("error", ex.getMessage());
    }
    static class BadRequest extends RuntimeException { public BadRequest(String m){ super(m);} }
}
