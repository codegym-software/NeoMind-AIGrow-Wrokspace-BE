package com.springboot.demo.controller;

import com.springboot.demo.dto.RolePermission.CreateRoleReq;
import com.springboot.demo.dto.RolePermission.RoleDto;
import com.springboot.demo.dto.RolePermission.UpdateRoleReq;
import com.springboot.demo.dto.RolePermission.UpsertRolePermissionsReq;
import com.springboot.demo.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/roles")
@RequiredArgsConstructor
public class WorkspaceRoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("@gate.allows('workspace:role:read', authentication, #workspaceId)")
    public List<RoleDto> list(@PathVariable String workspaceId) {
        return roleService.listWorkspaceRoles(workspaceId);
    }

    @PostMapping
    @PreAuthorize("@gate.allows('workspace:role:create', authentication, #workspaceId)")
    public RoleDto create(@PathVariable String workspaceId, @RequestBody CreateRoleReq req) {
        return roleService.createWorkspaceRole(workspaceId, req);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("@gate.allows('workspace:role:update', authentication, #workspaceId)")
    public RoleDto update(@PathVariable String workspaceId, @PathVariable Integer roleId, @RequestBody UpdateRoleReq req) {
        return roleService.updateWorkspaceRole(workspaceId, roleId, req);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("@gate.allows('workspace:role:delete', authentication, #workspaceId)")
    public void delete(@PathVariable String workspaceId, @PathVariable Integer roleId) {
        roleService.deleteWorkspaceRole(workspaceId, roleId);
    }

    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("@gate.allows('workspace:role:grant_permission', authentication, #workspaceId)")
    public void upsertPermissions(@PathVariable String workspaceId, @PathVariable Integer roleId,
                                  @RequestBody UpsertRolePermissionsReq req) {
        roleService.replaceWorkspaceRolePermissions(workspaceId, roleId, req.permissionIds());
    }
}


