package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "tasks")
@Getter @Setter
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="project_id", length=36, nullable=false)
    private String projectId;

    @Column(name="epic_id")  private Integer epicId;
    @Column(name="sprint_id")private Integer sprintId;

    @Column(nullable=false, length=255) private String title;
    @Column(columnDefinition="TEXT")     private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, columnDefinition="ENUM('task','story','bug')")
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, columnDefinition="ENUM('to_do','in_progress')")
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, columnDefinition="ENUM('highest','high','medium','low')")
    private TaskPriority priority;

    @Column(name="assignee_id", length=36) private String assigneeId;
    @Column(name="reporter_id", length=36) private String reporterId;

    @Column(name="due_date") private LocalDate dueDate;
    @Column(name="story_points") private Integer storyPoints;

    @Column(name="created_at", insertable=false, updatable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", insertable=false, updatable=false) private LocalDateTime updatedAt;
    @Column(name="archived_at") private LocalDateTime archivedAt;
}
