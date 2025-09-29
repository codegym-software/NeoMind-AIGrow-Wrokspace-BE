package com.springboot.demo.security.filter;

import com.springboot.demo.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class EnsureUserExistsFilter extends OncePerRequestFilter {

    private final UserService userService;

    public EnsureUserExistsFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws java.io.IOException, jakarta.servlet.ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            userService.ensureFromJwt(jwt);
        }
        chain.doFilter(request, response);
    }
}

