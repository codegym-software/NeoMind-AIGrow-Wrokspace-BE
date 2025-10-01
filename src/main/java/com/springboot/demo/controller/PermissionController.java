// src/main/java/com/springboot/demo/controller/PermissionController.java
package com.springboot.demo.controller;

import com.springboot.demo.model.Permission;
import com.springboot.demo.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public List<Permission> list(@RequestParam String scope) {
        return permissionService.listByScope(scope);
    }
}
