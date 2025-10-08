package com.springboot.demo.service;

import com.springboot.demo.dto.RolePermission.CreateRoleReq;
import com.springboot.demo.dto.RolePermission.UpdateRoleReq;
import com.springboot.demo.dto.RolePermission.RoleDto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public interface RoleService {

    List<RoleDto> list(String scopeStr, String targetId, boolean includeGlobal);
    RoleDto create(CreateRoleReq req);
    RoleDto update(Long id, UpdateRoleReq req);

    List<RoleDto> listCompanyRoles(String companyId, boolean includeGlobal);
    RoleDto createCompanyRole(String companyId, CreateRoleReq req);
    RoleDto updateCompanyRole(String companyId, Long roleId, UpdateRoleReq req);
    void deleteCompanyRole(String companyId, Long roleId);

    List<RoleDto> listWorkspaceRoles(String workspaceId, boolean includeGlobal);
    RoleDto createWorkspaceRole(String workspaceId, CreateRoleReq req);
    RoleDto updateWorkspaceRole(String workspaceId, Long roleId, UpdateRoleReq req);
    void deleteWorkspaceRole(String workspaceId, Long roleId);

    void replaceCompanyRolePermissions(String companyId, Long roleId, @NotEmpty List<Long> permissionIds);
    void replaceWorkspaceRolePermissions(String workspaceId, Long roleId, @NotEmpty List<Long> permissionIds);
}
