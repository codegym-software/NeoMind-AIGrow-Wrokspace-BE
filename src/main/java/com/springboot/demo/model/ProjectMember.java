package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "project_members")
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "project_id", length = 36, nullable = false)
    private String projectId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "is_owner", nullable = false)
    private boolean owner;

    @Column(name = "joined_at", insertable = false, updatable = false)
    private LocalDateTime joinedAt;
}
