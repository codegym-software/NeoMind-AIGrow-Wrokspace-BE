package com.springboot.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Mock Authentication Controller để test không cần AWS Cognito
 * Chỉ dùng cho development/testing
 */
@RestController
@RequestMapping("/auth/mock")
@RequiredArgsConstructor
public class MockAuthController {

    private final ObjectMapper objectMapper;

    /**
     * Login endpoint tạo JWT token giả
     * POST /auth/mock/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Tạo mock JWT token
        String token = createMockJwtToken(request.getEmail(), request.getName());
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("access_token", token);
        response.put("token_type", "Bearer");
        response.put("expires_in", 3600);
        
        ObjectNode user = objectMapper.createObjectNode();
        user.put("email", request.getEmail());
        user.put("name", request.getName());
        user.put("sub", request.getEmail());
        response.set("user", user);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No valid token");
        }
        
        String token = authHeader.substring(7);
        
        // Parse token (simplified)
        Map<String, Object> claims = parseMockToken(token);
        
        ObjectNode user = objectMapper.createObjectNode();
        user.put("email", (String) claims.get("email"));
        user.put("name", (String) claims.get("name"));
        user.put("sub", (String) claims.get("sub"));
        
        return ResponseEntity.ok(user);
    }

    /**
     * Tạo mock JWT token (không thật)
     */
    private String createMockJwtToken(String email, String name) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        headers.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("email", email);
        claims.put("name", name);
        claims.put("token_use", "access");
        claims.put("exp", Instant.now().plusSeconds(3600).getEpochSecond());
        claims.put("iat", Instant.now().getEpochSecond());

        String headerJson = toJson(headers);
        String claimsJson = toJson(claims);

        String header = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String claim = Base64.getUrlEncoder().withoutPadding().encodeToString(claimsJson.getBytes(StandardCharsets.UTF_8));

        String signature = signHmacSha256(header + "." + claim, "mock-secret-key");

        return header + "." + claim + "." + signature;
    }

    private String signHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> parseMockToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token");
            }

            String claimsJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            return objectMapper.readValue(claimsJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token", e);
        }
    }

    @lombok.Data
    public static class LoginRequest {
        private String email;
        private String name;
    }
}






