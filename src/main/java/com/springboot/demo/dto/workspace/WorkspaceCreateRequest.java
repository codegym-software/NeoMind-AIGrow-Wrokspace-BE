package com.springboot.demo.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceCreateRequest(
        @NotBlank String companyId,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 1000) String description
) {}
