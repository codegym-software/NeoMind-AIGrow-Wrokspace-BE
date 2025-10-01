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

    void replaceRolePermissions(String companyId, Long roleId, @NotEmpty List<Long> permissionIds);
}
