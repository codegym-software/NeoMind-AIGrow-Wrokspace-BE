package com.springboot.demo.service;

import com.springboot.demo.model.enums.Scope;

public interface AbilityService {
    boolean hasPermission(String userId, Scope scope, String scopeId, String permCode);
}
