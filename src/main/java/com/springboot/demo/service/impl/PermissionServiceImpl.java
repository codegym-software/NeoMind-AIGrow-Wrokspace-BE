// src/main/java/com/springboot/demo/service/impl/PermissionServiceImpl.java
package com.springboot.demo.service.impl;

import com.springboot.demo.model.Permission;
import com.springboot.demo.model.Scope;
import com.springboot.demo.repository.PermissionRepository;
import com.springboot.demo.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository repo;

    @Override
    public List<Permission> listByScope(String scopeStr) {
        if (scopeStr == null || scopeStr.isBlank()) {
            throw new IllegalArgumentException("scope is required");
        }
        Scope scope;
        try {
            scope = Scope.parse(scopeStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid scope: " + scopeStr);
        }
        return repo.findByScope(scope);
    }
}
