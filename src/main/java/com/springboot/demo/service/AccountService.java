package com.springboot.demo.service;

import org.springframework.security.oauth2.jwt.Jwt;

public interface AccountService {
    void changePassword(Jwt jwt, String currentPassword, String newPassword, String accessToken);
}

