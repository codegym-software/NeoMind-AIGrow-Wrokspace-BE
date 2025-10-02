package com.springboot.demo.dto.project;

import com.springboot.demo.model.ProjectPriority;
import com.springboot.demo.model.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectCreateRequest(
        @NotBlank(message = "Workspace ID is required")
        String workspaceId,

        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name must not exceed 255 characters")
        String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        ProjectStatus status,

        ProjectPriority priority,

        LocalDate startDate,

        LocalDate endDate,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex color code")
        String color
) {
    public ProjectCreateRequest {
        // Set default values if not provided
        if (status == null) {
            status = ProjectStatus.PLANNING;
        }
        if (priority == null) {
            priority = ProjectPriority.MEDIUM;
        }
    }
}

