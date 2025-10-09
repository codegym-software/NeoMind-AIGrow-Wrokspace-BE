package com.springboot.demo.dto.project;

import com.springboot.demo.model.Project;
import com.springboot.demo.enums.ProjectStatus;

public record ProjectSummaryResponse(
        String id,
        String name,
        String description,
        ProjectStatus status,
        String color,
        boolean isArchived,
        boolean isActive
) {
    public static ProjectSummaryResponse from(Project project) {
        return new ProjectSummaryResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getColor(),
                project.isArchived(),
                project.isActive()
        );
    }
}

