package com.springboot.demo.rbac.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleReq(
        @NotBlank String name,
        String description
) {}
