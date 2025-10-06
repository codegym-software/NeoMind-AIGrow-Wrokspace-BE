package com.springboot.demo.dto.task;

import com.springboot.demo.model.TaskPriority;
import com.springboot.demo.model.TaskStatus;
import com.springboot.demo.model.TaskType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskItemDto(
        Integer id,
        String projectId,
        Integer epicId,
        Integer sprintId, // null = backlog
        String title,
        TaskType type,
        TaskStatus status,
        TaskPriority priority,
        String assigneeId,
        String reporterId,
        Integer storyPoints,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
