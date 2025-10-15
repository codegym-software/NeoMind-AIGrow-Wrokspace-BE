package com.springboot.demo.service;

import java.util.List;
import java.util.Set;

public interface RolePermissionService {
    List<Integer> getPermissionIdsByRole(Integer roleId);
    void replaceRolePermissions(Integer roleId, List<Integer> permissionIds);
    Set<String> getPermissionCodesByRoleId(int roleId);
}
