package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "workspace_members",
        indexes = {
                @Index(name = "idx_wm_workspace_user", columnList = "workspace_id,user_id"),
                @Index(name = "idx_wm_user", columnList = "user_id")
        })
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "workspace_id", length = 36, nullable = false)
    private String workspaceId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId; // 200 admin, 202 editor, 203 viewer

    @Column(name = "is_owner", nullable = false)
    private boolean owner;

    @Column(name = "invited_by", length = 36)
    private String invitedBy;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    void prePersist(){
        if (joinedAt == null) joinedAt = LocalDateTime.now();
    }
}