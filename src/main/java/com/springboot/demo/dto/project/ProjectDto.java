package com.springboot.demo.dto.project;

import java.time.LocalDateTime;

import com.springboot.demo.model.Enum.ProjectStatus;

public record ProjectDto(
        String id,
        String workspaceId,
        String name,
        String description,
        String color,
        ProjectStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime archivedAt
) {}
