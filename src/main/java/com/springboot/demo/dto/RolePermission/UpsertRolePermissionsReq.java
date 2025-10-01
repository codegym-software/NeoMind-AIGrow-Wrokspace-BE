package com.springboot.demo.dto.RolePermission;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpsertRolePermissionsReq(
        @NotEmpty List<Integer> permissionIds
) {}
