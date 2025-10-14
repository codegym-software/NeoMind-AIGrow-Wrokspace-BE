package com.springboot.demo.controller;

import com.springboot.demo.model.Permission;
import com.springboot.demo.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Permission> list(@RequestParam String scope) {
        return permissionService.listByScope(scope);
    }
}
