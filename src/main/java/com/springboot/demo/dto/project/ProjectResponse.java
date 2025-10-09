package com.springboot.demo.dto.project;

import com.springboot.demo.model.Project;
import com.springboot.demo.enums.ProjectStatus;

import java.time.LocalDateTime;

public record ProjectResponse(
        String id,
        String workspaceId,
        String name,
        String description,
        ProjectStatus status,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isArchived,
        boolean isActive
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getWorkspaceId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getColor(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.isArchived(),
                project.isActive()
        );
    }
}

