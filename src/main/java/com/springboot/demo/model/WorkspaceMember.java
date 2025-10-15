package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workspace_members")
public class WorkspaceMember {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "workspace_id", columnDefinition = "CHAR(36)", nullable = false)
    private String workspaceId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String userId;

    @Column(name = "role_id", nullable = true)
    private Integer roleId;

    @Column(name = "is_owner", nullable = false)
    @Builder.Default
    private boolean isOwner = false;

    public WorkspaceMember(String workspaceId, String userId, Integer roleId) {
        this();
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.roleId = roleId;
        this.isOwner = false;
    }

    public WorkspaceMember(String workspaceId, String userId, Integer roleId, boolean isOwner) {
        this();
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.roleId = roleId;
        this.isOwner = isOwner;
    }
}