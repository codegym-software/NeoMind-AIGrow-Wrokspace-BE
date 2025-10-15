package com.springboot.demo.dto.workspace;

import jakarta.validation.constraints.Size;

public record WorkspaceUpdateRequest(
        @Size(max = 255) String name,
        @Size(max = 1000) String description
) {}
