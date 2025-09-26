package com.springboot.demo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User ensureFromJwt(Jwt jwt) {
        // Try multiple possible claim names for email in Cognito
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("cognito:username");
        }
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("preferred_username");
        }
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("username");
        }
        
        String given = jwt.getClaimAsString("given_name");
        String family = jwt.getClaimAsString("family_name");
        String name = jwt.getClaimAsString("name");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("JWT missing email claim");
        }

        // Tạo displayName (không dùng trong lambda để tránh lỗi 'effectively final')
        String tmp = ((given != null ? given : "") + " " + (family != null ? family : "")).trim();
        if (tmp.isBlank()) {
            tmp = email;
        }
        final String displayName = tmp;

        // Tìm user theo email
        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing != null) {
            existing.setLastLoginAt(LocalDateTime.now());
            return existing;
        }

        // Chưa có: tạo mới
        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setEmail(email);
        u.setDisplayName(displayName);
        u.setLastLoginAt(LocalDateTime.now());

        try {
            return userRepository.save(u);
        } catch (DataIntegrityViolationException e) {
            // Race condition hiếm: request khác đã tạo cùng email
            return userRepository.findByEmail(email).orElseThrow(() -> e);
        }
    }
}
