package com.springboot.demo.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceCreateRequest(
        @NotBlank(message = "Company ID is required")
        String companyId,

        @NotBlank(message = "Workspace name is required")
        @Size(max = 255, message = "Workspace name must not exceed 255 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {
}

