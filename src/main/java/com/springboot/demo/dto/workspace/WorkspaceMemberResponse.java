package com.springboot.demo.dto.workspace;

import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.model.User;

public record WorkspaceMemberResponse(
        String id,
        String workspaceId,
        String userId,
        String userEmail,
        String userDisplayName,
        Integer roleId,
        String roleName,
        boolean isOwner
) {
    public static WorkspaceMemberResponse from(WorkspaceMember member, User user, String roleName) {
        return new WorkspaceMemberResponse(
                member.getId(),
                member.getWorkspaceId(),
                member.getUserId(),
                user.getEmail(),
                user.getDisplayName(),
                member.getRoleId(),
                roleName,
                member.isOwner()
        );
    }
}



