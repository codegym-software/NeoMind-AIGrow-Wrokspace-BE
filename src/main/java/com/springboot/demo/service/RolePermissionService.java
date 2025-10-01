package com.springboot.demo.service;

import java.util.List;

public interface RolePermissionService {
    List<Long> getPermissionIdsByRole(Long roleId);
    void replaceRolePermissions(Long roleId, List<Long> permissionIds);
}
