package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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

    @Column(name = "is_owner")
    private boolean isOwner;

    public WorkspaceMember(String workspaceId, String userId, Integer roleId) {
        this.id = UUID.randomUUID().toString();
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.roleId = roleId;
        this.isOwner = false;
    }

    public static WorkspaceMember asOwner(String workspaceId, String userId) {
        WorkspaceMember member = new WorkspaceMember();
        member.setId(UUID.randomUUID().toString());
        member.setWorkspaceId(workspaceId);
        member.setUserId(userId);
        member.setOwner(true);
        member.setRoleId(null); // Explicitly set roleId to null for owners
        return member;
    }
}