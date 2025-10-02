package com.springboot.demo.security;

import com.springboot.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserIdResolver {

    private final UserRepository userRepo;
    private final RequestUserContext ctx;

    public String resolveInternalId(Authentication auth) {
        if (ctx.getInternalUserId() != null) return ctx.getInternalUserId();

        String email = extractEmail(auth);
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Missing email in token");
        }

        String id = userRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("User not ensured in DB for email: " + email))
                .getId();

        ctx.setInternalUserId(id);
        return id;
    }

    private String extractEmail(Authentication auth) {
        Object p = auth != null ? auth.getPrincipal() : null;
        if (p instanceof Jwt jwt) {
            String email = firstNonBlank(
                    jwt.getClaimAsString("email")
            );
            return email;
        }
        return null;
    }

    private static String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
