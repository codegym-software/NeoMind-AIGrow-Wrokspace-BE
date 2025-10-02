package com.springboot.demo.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter @Setter
@Component
@RequestScope
public class RequestUserContext {
    private String internalUserId;
}
