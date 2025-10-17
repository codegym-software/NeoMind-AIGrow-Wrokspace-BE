package com.springboot.demo.dto.workspace;

import com.springboot.demo.model.WorkspaceMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceMemberDto {
    private Integer id;
    private String workspaceId;
    private String userId;
    private Integer roleId;
    private boolean isOwner;
    private String userEmail;
    private String userDisplayName;
    private String roleName;
    private String invitedBy;
    private String joinedAt;

    public static WorkspaceMemberDto fromEntity(WorkspaceMember member) {
        return WorkspaceMemberDto.builder()
                .id(member.getId())
                .workspaceId(member.getWorkspaceId())
                .userId(member.getUserId())
                .roleId(member.getRoleId())
                .isOwner(member.isOwner())
                .invitedBy(member.getInvitedBy())
                .joinedAt(member.getJoinedAt() != null ? member.getJoinedAt().toString() : null)
                .build();
    }

    public static WorkspaceMemberDto fromEntityWithUserInfo(WorkspaceMember member, String userEmail, String userDisplayName) {
        return WorkspaceMemberDto.builder()
                .id(member.getId())
                .workspaceId(member.getWorkspaceId())
                .userId(member.getUserId())
                .roleId(member.getRoleId())
                .isOwner(member.isOwner())
                .invitedBy(member.getInvitedBy())
                .joinedAt(member.getJoinedAt() != null ? member.getJoinedAt().toString() : null)
                .userEmail(userEmail)
                .userDisplayName(userDisplayName)
                .build();
    }
}
