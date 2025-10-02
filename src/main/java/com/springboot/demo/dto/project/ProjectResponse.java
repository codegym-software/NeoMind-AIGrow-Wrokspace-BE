package com.springboot.demo.dto.project;

import com.springboot.demo.model.Project;
import com.springboot.demo.model.ProjectPriority;
import com.springboot.demo.model.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponse(
        String id,
        String workspaceId,
        String name,
        String description,
        ProjectStatus status,
        ProjectPriority priority,
        LocalDate startDate,
        LocalDate endDate,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isArchived,
        boolean isActive,
        boolean isCompleted,
        boolean isOverdue
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getWorkspaceId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getPriority(),
                project.getStartDate(),
                project.getEndDate(),
                project.getColor(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.isArchived(),
                project.isActive(),
                project.isCompleted(),
                project.isOverdue()
        );
    }
}

