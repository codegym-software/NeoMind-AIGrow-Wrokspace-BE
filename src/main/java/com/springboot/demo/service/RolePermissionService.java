package com.springboot.demo.service;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public interface RolePermissionService {
    List<Integer> getPermissionIdsByRole(Integer roleId);

    void replaceRolePermissions(Integer roleId, @NotEmpty List<Integer> permissionIds);
}
