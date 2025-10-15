package com.springboot.demo.dto.workspace;

public record WorkspaceInviteRequest(
        String email,
        String userId,
        Integer roleId
) {
    public boolean hasPrincipal() {
        return (email != null && !email.isBlank()) || (userId != null && !userId.isBlank());
    }
}
