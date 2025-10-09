package com.springboot.demo.model;

import com.springboot.demo.model.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "projects")
public class Project {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "workspace_id", length = 36, nullable = false)
    private String workspaceId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "color", length = 50)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('active','archived')")
    private ProjectStatus status = ProjectStatus.active;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
}
