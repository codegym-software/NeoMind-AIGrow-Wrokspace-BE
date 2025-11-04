package com.springboot.demo.service;

import com.springboot.demo.dto.RolePermission.CreateRoleReq;
import com.springboot.demo.dto.RolePermission.UpdateRoleReq;
import com.springboot.demo.dto.RolePermission.RoleDto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public interface RoleService {

    List<RoleDto> list(String scopeStr, String targetId, boolean includeGlobal);
    RoleDto create(CreateRoleReq req);
    RoleDto update(Integer id, UpdateRoleReq req);

    List<RoleDto> listCompanyRoles(String companyId, boolean includeGlobal);
    RoleDto createCompanyRole(String companyId, CreateRoleReq req);
    RoleDto updateCompanyRole(String companyId, Integer roleId, UpdateRoleReq req);
    void deleteCompanyRole(String companyId, Integer roleId);

    void replaceRolePermissions(String companyId, Integer roleId, @NotEmpty List<Integer> permissionIds);

    // Workspace roles
    List<RoleDto> listWorkspaceRoles(String workspaceId);
    RoleDto createWorkspaceRole(String workspaceId, CreateRoleReq req);
    RoleDto updateWorkspaceRole(String workspaceId, Integer roleId, UpdateRoleReq req);
    void deleteWorkspaceRole(String workspaceId, Integer roleId);
    void replaceWorkspaceRolePermissions(String workspaceId, Integer roleId, @NotEmpty List<Integer> permissionIds);
}
