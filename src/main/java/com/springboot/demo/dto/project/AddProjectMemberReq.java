package com.springboot.demo.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddProjectMemberReq(
    @NotBlank
    String userId,
    @NotNull
    Integer roleId
) {}