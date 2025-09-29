package com.springboot.demo.rbac;

import com.springboot.demo.rbac.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository repo;

    @GetMapping
    public List<Role> list(@RequestParam String scope) {
        return repo.findByScope(Scope.parse(scope));
    }

    @PostMapping
    public Role create(@Valid @RequestBody CreateRoleReq req) {
        var scope = Scope.parse(req.scope());
        if (repo.existsByScopeAndCode(scope, req.code()))
            throw new IllegalArgumentException("Role code duplicated in scope");
        var r = Role.builder().scope(scope).code(req.code()).name(req.name()).description(req.description()).build();
        return repo.save(r);
    }

    @PutMapping("/{id}")
    public Role update(@PathVariable Long id, @Valid @RequestBody UpdateRoleReq req) {
        var r = repo.findById(id).orElseThrow();
        r.setName(req.name());
        r.setDescription(req.description());
        return repo.save(r);
    }
}
