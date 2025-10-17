package com.springboot.demo.dto.workspace;

public record WorkspaceMemberInviteRequest(
        String userId,
        Integer roleId
) {
}