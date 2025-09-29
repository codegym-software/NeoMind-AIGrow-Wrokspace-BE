package com.springboot.demo.service;

import com.springboot.demo.model.User;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {
    User ensureFromJwt(Jwt jwt);
}

