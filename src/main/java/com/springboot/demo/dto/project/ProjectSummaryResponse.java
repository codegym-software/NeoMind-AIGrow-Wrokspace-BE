package com.springboot.demo.dto.project;

import com.springboot.demo.model.Project;
import com.springboot.demo.model.ProjectPriority;
import com.springboot.demo.model.ProjectStatus;

import java.time.LocalDate;

public record ProjectSummaryResponse(
        String id,
        String name,
        String description,
        ProjectStatus status,
        ProjectPriority priority,
        LocalDate startDate,
        LocalDate endDate,
        String color,
        boolean isOverdue
) {
    public static ProjectSummaryResponse from(Project project) {
        return new ProjectSummaryResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getPriority(),
                project.getStartDate(),
                project.getEndDate(),
                project.getColor(),
                project.isOverdue()
        );
    }
}

