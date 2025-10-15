package com.springboot.demo.dto.workspace;

import jakarta.validation.constraints.NotNull;

public record WorkspaceMemberCreateRequest(
        @NotNull(message = "User ID is required")
        String userId,
        
        @NotNull(message = "Role ID is required")
        Integer roleId
) {
}