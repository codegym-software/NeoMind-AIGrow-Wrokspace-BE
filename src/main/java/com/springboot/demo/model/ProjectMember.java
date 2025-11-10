package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "project_members")
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "project_id", length = 36, nullable = false)
    private String projectId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "is_owner")
    private boolean isOwner;

    @Column(name = "joined_at", insertable = false, updatable = false)
    private LocalDateTime joinedAt;

    public ProjectMember(String projectId, String userId, Integer roleId) {
        this.projectId = projectId;
        this.userId = userId;
        this.roleId = roleId;
        this.isOwner = false;
    }

    public static ProjectMember asOwner(String projectId, String userId) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.isOwner = true;
        member.setRoleId(null);
        return member;
    }
}
