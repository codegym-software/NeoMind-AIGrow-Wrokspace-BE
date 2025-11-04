package com.springboot.demo.dto.project;

import jakarta.validation.constraints.NotNull;

public record AssignProjectRoleReq(
    @NotNull
    Integer roleId
) {}