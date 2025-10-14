package com.springboot.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Deprecated
@Component
@RequiredArgsConstructor
public class UserIdResolver {

    private final CurrentUserService currentUserService;
    public String requireCurrentUserId() {
        return currentUserService.requireCurrentUserId();
    }
}
