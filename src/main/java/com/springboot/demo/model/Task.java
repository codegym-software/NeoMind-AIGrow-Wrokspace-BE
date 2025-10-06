package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Liên kết bắt buộc ---
    @Column(name = "project_id", length = 36, nullable = false)
    private String projectId;

    // Có thể null nếu chưa thuộc epic
    @Column(name = "epic_id")
    private Integer epicId;

    // Có thể null nếu task đang ở backlog
    @Column(name = "sprint_id")
    private Integer sprintId;

    // --- Thông tin chính ---
    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type = TaskType.task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.to_do;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.medium;

    // --- Liên quan user ---
    @Column(name = "assignee_id", length = 36)
    private String assigneeId;

    @Column(name = "reporter_id", length = 36)
    private String reporterId;

    // --- Kế hoạch ---
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "story_points")
    private Integer storyPoints;

    // --- Thời gian ---
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
}
