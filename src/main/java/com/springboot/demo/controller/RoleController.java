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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/companies/{companyId}")
    @PreAuthorize("@gate.allows('company:role:read', authentication, #companyId)")
    public List<RoleDto> list(@PathVariable String companyId,
                              @RequestParam(defaultValue = "true") boolean includeGlobal) {
        return roleService.listCompanyRoles(companyId, includeGlobal);
    }

    @PostMapping("/companies/{companyId}")
    @PreAuthorize("@gate.allows('company:role:create', authentication, #companyId)")
    public RoleDto create(@PathVariable String companyId, @RequestBody CreateRoleReq req) {
        return roleService.createCompanyRole(companyId, req);
    }

    @PutMapping("/companies/{companyId}/{roleId}")
    @PreAuthorize("@gate.allows('company:role:update', authentication, #companyId)")
    public RoleDto update(@PathVariable String companyId, @PathVariable Integer roleId, @RequestBody UpdateRoleReq req) {
        return roleService.updateCompanyRole(companyId, roleId, req);
    }

    @DeleteMapping("/companies/{companyId}/{roleId}")
    @PreAuthorize("@gate.allows('company:role:delete', authentication, #companyId)")
    public void delete(@PathVariable String companyId, @PathVariable Integer roleId) {
        roleService.deleteCompanyRole(companyId, roleId);
    }

    @PutMapping("/companies/{companyId}/{roleId}/permissions")
    @PreAuthorize("@gate.allows('company:role:grant_permission', authentication, #companyId)")
    public void upsertPermissions(@PathVariable String companyId, @PathVariable Integer roleId,
                                  @RequestBody UpsertRolePermissionsReq req) {
        roleService.replaceRolePermissions(companyId, roleId, req.permissionIds());
    }
}

