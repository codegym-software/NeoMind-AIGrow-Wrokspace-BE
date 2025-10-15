package com.springboot.demo.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceUpdateRequest(@NotBlank String name, String description) {}
