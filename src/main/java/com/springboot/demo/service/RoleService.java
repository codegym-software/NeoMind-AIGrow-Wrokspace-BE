package com.springboot.demo.service;

import com.springboot.demo.dto.RolePermission.CreateRoleReq;
import com.springboot.demo.dto.RolePermission.UpdateRoleReq;
import com.springboot.demo.dto.RolePermission.RoleDto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public interface RoleService {
    // ==== global ==== //
    List<RoleDto> list(String scopeStr, String targetId, boolean includeGlobal);
    RoleDto create(CreateRoleReq req);
    RoleDto update(Integer id, UpdateRoleReq req);

    // ==== company ==== //
    List<RoleDto> listCompanyRoles(String companyId, boolean includeGlobal);
    RoleDto createCompanyRole(String companyId, CreateRoleReq req);
    RoleDto updateCompanyRole(String companyId, Integer roleId, UpdateRoleReq req);
    void deleteCompanyRole(String companyId, Integer roleId);

    void replaceRolePermissionsCompany(String companyId, Integer roleId, @NotEmpty List<Integer> permissionIds);

    // ==== workspace ==== //
    List<RoleDto> listWorkspaceRoles(String workspaceId, boolean includeGlobal);
    RoleDto createWorkspaceRole(String workspaceId, CreateRoleReq req);
    RoleDto updateWorkspaceRole(String workspaceId, Integer roleId, UpdateRoleReq req);
    void deleteWorkspaceRole(String workspaceId, Integer roleId);

    void replaceRolePermissionsWorkspace(String workspaceId, Integer roleId, @NotEmpty List<Integer> permissionIds);
}
