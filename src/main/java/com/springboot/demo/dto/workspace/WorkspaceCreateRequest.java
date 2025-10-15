package com.springboot.demo.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceCreateRequest( @NotBlank String companyId, String name, String description) {}
