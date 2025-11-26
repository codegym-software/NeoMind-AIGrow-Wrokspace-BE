package com.springboot.demo.dto.epic;

import java.time.LocalDate;

public record EpicDto(
        Integer id,
        String projectId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Integer totalTasks,
        Integer doneTasks,
        Double progressPercent,
        Boolean overdue
) {}
