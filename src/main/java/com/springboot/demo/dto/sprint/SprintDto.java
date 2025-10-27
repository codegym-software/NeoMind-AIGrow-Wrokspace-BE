package com.springboot.demo.dto.sprint;

import com.springboot.demo.model.enums.SprintStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SprintDto(
        Integer id,
        String projectId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SprintStatus status,
        String description,
        Boolean isBacklog,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
