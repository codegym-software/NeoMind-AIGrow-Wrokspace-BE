package com.springboot.demo.security.filter;

import com.springboot.demo.model.User;
import com.springboot.demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class EnsureUserExistsFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/auth/") || p.startsWith("/public/") || p.equals("/error");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth && auth.isAuthenticated()) {
            Jwt jwt = jwtAuth.getToken();

            String email      = jwt.getClaimAsString("email");
            String sub        = jwt.getClaimAsString("sub");
            String givenName  = jwt.getClaimAsString("given_name");
            String familyName = jwt.getClaimAsString("family_name");

            if (email != null && !email.isBlank()) {
                String displayName = ((givenName == null ? "" : givenName) + " " + (familyName == null ? "" : familyName)).trim();
                if (displayName.isEmpty()) displayName = email;

                Optional<User> found = userRepository.findByEmail(email);

                if (found.isPresent()) {
                    User u = found.get();
                    u.setLastLoginAt(LocalDateTime.now());
                    userRepository.save(u);
                } else {
                    User u = new User();

                    String id = isSuitableAsId(sub) ? sub : UUID.randomUUID().toString();

                    u.setId(id);
                    u.setEmail(email);
                    u.setDisplayName(displayName);
                    u.setLastLoginAt(LocalDateTime.now());

                    try {
                        userRepository.save(u);
                    } catch (DataIntegrityViolationException e) {
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSuitableAsId(String sub) {
        return sub != null && !sub.isBlank() && sub.length() <= 36;
    }
}
