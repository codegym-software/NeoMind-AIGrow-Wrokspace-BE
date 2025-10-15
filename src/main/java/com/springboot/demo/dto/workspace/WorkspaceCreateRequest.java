package com.springboot.demo.dto.workspace;

public record WorkspaceCreateRequest(
        String companyId,
        String name,
        String description
) {
}
