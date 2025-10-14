package com.springboot.demo.security;

import com.springboot.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserIdResolver {

    private final UserRepository userRepo;
    private final RequestUserContext ctx;
    private final CognitoUserInfoService cognitoUserInfoService;

    public String resolveInternalId(Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AccessDeniedException("Unauthenticated");
        }

        if (ctx.getInternalUserId() != null) return ctx.getInternalUserId();

        Jwt jwt = (Jwt) jwtAuth.getToken();
        String emailResolved = jwt.getClaimAsString("email");
        if (emailResolved == null || emailResolved.isBlank()) {
            try {
                Map<String, Object> info = cognitoUserInfoService.fetch(jwt.getTokenValue());
                Object e = info.get("email");
                if (e instanceof String s && !s.isBlank()) emailResolved = s;
            } catch (Exception ignored) {}
        }

        if (emailResolved == null || emailResolved.isBlank()) {
            throw new IllegalStateException("Cannot resolve current user (no email/username in JWT).");
        }

        String finalEmailResolved = emailResolved;
        String id = userRepo.findByEmail(emailResolved)
                .orElseThrow(() -> new AccessDeniedException("User not ensured in DB for email" + finalEmailResolved))
                .getId();

        ctx.setInternalUserId(id);
        return id;
    }
}
