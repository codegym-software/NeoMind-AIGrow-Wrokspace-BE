package com.springboot.demo.dto.sprint;

import com.springboot.demo.model.enums.SprintStatus;
import com.springboot.demo.validation.ValidDateRange;
import java.time.LocalDate;

@ValidDateRange(startField = "startDate", endField = "endDate", message = "End date must be after or equal to start date")
public record UpdateSprintReq(
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SprintStatus status,
        String description,
        Boolean isBacklog
) {}
