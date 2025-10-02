package com.springboot.demo.controller;

import com.springboot.demo.dto.RolePermission.UpsertRolePermissionsReq;
import com.springboot.demo.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles/{roleId}/permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping
    public List<Integer> get(@PathVariable Integer roleId) {
        return rolePermissionService.getPermissionIdsByRole(roleId);
    }

    @PutMapping
    public void upsert(@PathVariable Integer roleId, @RequestBody UpsertRolePermissionsReq req) {
        rolePermissionService.replaceRolePermissions(roleId, req.permissionIds());
    }
}
