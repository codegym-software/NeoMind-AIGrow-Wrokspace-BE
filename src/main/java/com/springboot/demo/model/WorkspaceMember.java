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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "workspace_id", length = 36, nullable = false)
    private String workspaceId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "is_owner", nullable = false)
    private boolean isOwner = false;

    @Column(name = "invited_email")
    private String invitedEmail;

    @Column(name = "invited_by", length = 36)
    private String invitedBy;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    void prePersist() {
        if (joinedAt == null) joinedAt = LocalDateTime.now();
    }
}
