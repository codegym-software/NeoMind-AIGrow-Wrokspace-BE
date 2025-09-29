package com.springboot.demo.rbac;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    private final PermissionRepository repo;
    public PermissionController(PermissionRepository repo){ this.repo = repo; }

    @GetMapping
    public List<Permission> list(@RequestParam String scope) {
        return repo.findByScope(Scope.parse(scope));
    }
}
