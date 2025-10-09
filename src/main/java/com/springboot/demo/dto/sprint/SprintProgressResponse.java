package com.springboot.demo.dto.sprint;

import com.springboot.demo.enums.SprintStatus;

import java.time.LocalDate;

public record SprintProgressResponse(
    String id,
    String name,
    SprintStatus status,
    LocalDate startDate,
    LocalDate endDate,
    
    // Progress metrics
    Integer totalTasks,
    Integer completedTasks,
    Integer inProgressTasks,
    Integer todoTasks,
    Double progressPercentage,
    
    // Timeline metrics
    Integer totalDays,
    Integer elapsedDays,
    Integer remainingDays,
    Double timeProgressPercentage,
    
    // Status indicators
    boolean isOverdue,
    boolean isEndingSoon, // within 3 days
    String statusMessage
) {}
