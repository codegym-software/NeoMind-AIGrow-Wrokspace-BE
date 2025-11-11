package com.springboot.demo.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferProjectOwnershipReq(
    @NotBlank
    String toUserId,
    @NotNull
    Integer downgradeRoleId
) {}