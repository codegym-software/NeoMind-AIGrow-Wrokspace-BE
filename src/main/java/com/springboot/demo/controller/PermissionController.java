package com.springboot.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.springboot.demo.repository.PermissionRepository;
import com.springboot.demo.model.Permission;
import com.springboot.demo.model.Scope;

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
