package com.springboot.demo.service;

import com.springboot.demo.dto.RolePermission.CreateRoleReq;
import com.springboot.demo.dto.RolePermission.UpdateRoleReq;
import com.springboot.demo.dto.RolePermission.RoleDto;
import com.springboot.demo.model.Permission;

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
    
    // Project roles
    List<RoleDto> listProjectRoles(String projectId);
    
    RoleDto getProjectRoleById(String projectId, Integer roleId);

    RoleDto createProjectRole(String projectId, CreateRoleReq req);

    RoleDto updateProjectRole(String projectId, Integer roleId, UpdateRoleReq req);

    void deleteProjectRole(String projectId, Integer roleId);
    void replaceProjectRolePermissions(String projectId, Integer roleId, @NotEmpty List<Integer> permissionIds);

    List<Permission> getProjectRolePermissions(String projectId, Integer roleId);
    
    // Tạo mặc định 2 role admin và member cho project mới
    void createDefaultProjectRoles(String projectId);

    
    // Tạo mặc định 2 role admin và member cho workspace mới
    void createDefaultWorkspaceRoles(String workspaceId);
}
