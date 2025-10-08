package com.springboot.demo.dto.sprint;

import com.springboot.demo.enums.SprintStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SprintResponse(
    String id,
    String projectId,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    SprintStatus status,
    Boolean isBacklog,
    String goal,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    
    // Progress tracking fields
    Integer totalTasks,
    Integer completedTasks,
    Double progressPercentage,
    Integer remainingDays,
    
    // Permissions
    boolean canEdit,
    boolean canDelete,
    boolean canView,
    boolean canManage
) {}
