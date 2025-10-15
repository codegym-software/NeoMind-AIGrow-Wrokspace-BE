package com.springboot.demo.dto.workspace;

public record WorkspaceCreateInCompanyRequest(
        String name,
        String description
) {
}
