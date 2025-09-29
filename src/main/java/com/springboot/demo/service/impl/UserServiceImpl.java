package com.springboot.demo.service.impl;

import com.springboot.demo.model.User;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User ensureFromJwt(Jwt jwt) {
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

        String tmp = ((given != null ? given : "") + " " + (family != null ? family : "")).trim();
        if (tmp.isBlank()) {
            tmp = email;
        }
        final String displayName = tmp;

        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing != null) {
            existing.setLastLoginAt(LocalDateTime.now());
            return existing;
        }

        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setEmail(email);
        u.setDisplayName(displayName);
        u.setLastLoginAt(LocalDateTime.now());

        try {
            return userRepository.save(u);
        } catch (DataIntegrityViolationException e) {
            return userRepository.findByEmail(email).orElseThrow(() -> e);
        }
    }
}
