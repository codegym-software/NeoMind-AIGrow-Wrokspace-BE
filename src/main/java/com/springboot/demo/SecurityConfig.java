package com.springboot.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final EnsureUserExistsFilter ensureUserExistsFilter;

    public SecurityConfig(EnsureUserExistsFilter ensureUserExistsFilter) {
        this.ensureUserExistsFilter = ensureUserExistsFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/files/**").permitAll()
                        .requestMatchers("/api/uploads/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        http.addFilterAfter(ensureUserExistsFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*", "${FRONTEND_ORIGIN}"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With","Accept","Origin"));
        cfg.setExposedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
