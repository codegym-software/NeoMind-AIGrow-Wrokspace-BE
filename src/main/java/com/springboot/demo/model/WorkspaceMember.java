package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workspace_members",
        indexes = {
                @Index(name = "idx_ws_member_ws_user", columnList = "workspace_id,user_id"),
                @Index(name = "idx_ws_member_invited_email", columnList = "invited_email"),
                @Index(name = "idx_ws_member_invited_by", columnList = "invited_by")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkspaceMember {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "workspace_id", columnDefinition = "CHAR(36)", nullable = false)
    private String workspaceId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String userId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "is_owner", nullable = false)
    private boolean isOwner;

    @Column(name = "invited_email", length = 255)
    private String invitedEmail;

    @Column(name = "invited_by", columnDefinition = "CHAR(36)")
    private String invitedBy;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (joinedAt == null) joinedAt = LocalDateTime.now();
    }


    public static WorkspaceMember inviteNow(String workspaceId,
                                            String userId,
                                            Integer roleId,
                                            boolean isOwner,
                                            String inviterId,
                                            String invitedEmail) {
        return WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(userId)
                .roleId(roleId)
                .isOwner(isOwner)
                .invitedBy(inviterId)
                .invitedEmail(invitedEmail)
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
