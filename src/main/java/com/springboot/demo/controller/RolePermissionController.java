package com.springboot.demo.controller;

import com.springboot.demo.dto.RolePermission.UpsertRolePermissionsReq;
import com.springboot.demo.repository.RolePermissionRepository;
import com.springboot.demo.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles/{roleId}/permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionRepository rpRepo;
    private final RolePermissionService rpService;

    @GetMapping
    public List<Long> get(@PathVariable Long roleId) {
        return rpRepo.findByIdRoleId(roleId).stream().map(rp -> rp.getId().getPermissionId()).toList();
    }

    @PutMapping
    @Transactional
    public void upsert(@PathVariable Long roleId, @RequestBody UpsertRolePermissionsReq req) {
        rpService.replaceRolePermissions(roleId, req.permissionIds());
    }
}
