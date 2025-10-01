package com.springboot.demo.dto.RolePermission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoleReq(
        @NotNull String scope,
        String targetId,               // null => template role của scope
        @NotBlank String code,
        @NotBlank String name,
        String description
) {}
