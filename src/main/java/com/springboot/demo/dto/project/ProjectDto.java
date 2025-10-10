package com.springboot.demo.dto.project;

import com.springboot.demo.model.enums.ProjectStatus;

import java.time.LocalDateTime;

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
