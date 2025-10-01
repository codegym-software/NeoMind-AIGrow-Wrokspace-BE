package com.springboot.demo.service;

import java.util.List;

public interface RolePermissionService {
    List<Integer> getPermissionIdsByRole(Integer roleId);
    void replaceRolePermissions(Integer roleId, List<Integer> permissionIds);
}
