package com.springboot.demo.rbac.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpsertRolePermissionsReq(
        @NotEmpty List<Long> permissionIds
) {}
