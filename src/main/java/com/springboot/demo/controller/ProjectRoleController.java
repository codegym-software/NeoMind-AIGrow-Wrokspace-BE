package com.springboot.demo.controller;

import com.springboot.demo.dto.RolePermission.CreateRoleReq;
import com.springboot.demo.dto.RolePermission.RoleDto;
import com.springboot.demo.dto.RolePermission.UpdateRoleReq;
import com.springboot.demo.dto.RolePermission.UpsertRolePermissionsReq;
import com.springboot.demo.model.Permission;
import com.springboot.demo.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/roles")
@RequiredArgsConstructor
public class ProjectRoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("@gate.allows('project:role:read', authentication, #projectId)")
    public List<RoleDto> list(@PathVariable String projectId) {
        return roleService.listProjectRoles(projectId);
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("@gate.allows('project:role:read', authentication, #projectId)")
    public RoleDto getOne(@PathVariable String projectId, @PathVariable Integer roleId) {
        return roleService.getProjectRoleById(projectId, roleId);
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("@gate.allows('project:role:read', authentication, #projectId)")
    public List<Permission> listPermissions(@PathVariable String projectId, @PathVariable Integer roleId) {
        return roleService.getProjectRolePermissions(projectId, roleId);
    }

    @PostMapping
    @PreAuthorize("@gate.allows('project:role:create', authentication, #projectId)")
    public RoleDto create(@PathVariable String projectId, @RequestBody CreateRoleReq req) {
        return roleService.createProjectRole(projectId, req);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("@gate.allows('project:role:update', authentication, #projectId)")
    public RoleDto update(@PathVariable String projectId, @PathVariable Integer roleId, @RequestBody UpdateRoleReq req) {
        return roleService.updateProjectRole(projectId, roleId, req);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("@gate.allows('project:role:delete', authentication, #projectId)")
    public void delete(@PathVariable String projectId, @PathVariable Integer roleId) {
        roleService.deleteProjectRole(projectId, roleId);
    }

    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("@gate.allows('project:role:grant_permission', authentication, #projectId)")
    public void upsertPermissions(@PathVariable String projectId, @PathVariable Integer roleId,
                                  @RequestBody UpsertRolePermissionsReq req) {
        roleService.replaceProjectRolePermissions(projectId, roleId, req.permissionIds());
    }
}