package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workspace_members",
        indexes = {
                @Index(name = "idx_workspace_user", columnList = "workspace_id,user_id")
        })
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

    public WorkspaceMember(String workspaceId, String userId, Integer roleId) {
        this();
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.roleId = roleId;
    }

    @Column(name = "is_owner", nullable = false)
    private boolean isOwner = false;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        enforceOwnerRoleRule();
    }

    @PreUpdate
    void preUpdate() {
        enforceOwnerRoleRule();
    }

    private void enforceOwnerRoleRule() {
        if (isOwner) {
            // Owner => null role_id (tương tự CompanyMember)
            try {
                var f = this.getClass().getDeclaredField("roleId");
                f.setAccessible(true);
                f.set(this, null);
            } catch (Exception ignore) {
            }
        }
    }
}
