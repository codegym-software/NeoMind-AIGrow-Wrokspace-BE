package com.springboot.demo.dto.workspace;


public record WorkspaceUpdateRequest(
        String name,
        String description
) {
}
