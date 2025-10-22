package com.springboot.demo.dto.workspace;

import jakarta.validation.constraints.NotBlank;

public record WorkspaceCreateInCompanyRequest(
        @NotBlank(message = "Workspace name is required")
        String name,
        String description
) {}

