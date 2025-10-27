package com.springboot.demo.dto.sprint;

import com.springboot.demo.model.enums.SprintStatus;
import java.time.LocalDate;

public record UpdateSprintReq(
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SprintStatus status,
        String description,
        Boolean isBacklog
) {}
