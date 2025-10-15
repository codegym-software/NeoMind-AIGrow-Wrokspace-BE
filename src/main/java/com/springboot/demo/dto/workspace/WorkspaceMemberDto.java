package com.springboot.demo.dto.workspace;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkspaceMemberDto {
    private String id;
    private String workspaceId;
    private String userId;
    private Integer roleId;
    private boolean isOwner;

    private String userEmail;
    private String userDisplayName;
    private String roleName;

    private String invitedEmail;
    private String invitedBy;
    private LocalDateTime joinedAt;
}
