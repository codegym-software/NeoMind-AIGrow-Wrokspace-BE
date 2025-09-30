package com.springboot.demo.dto.RolePermission;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleReq(
        @NotBlank String name,
        String description
) {}
