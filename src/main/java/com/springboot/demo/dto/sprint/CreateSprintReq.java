package com.springboot.demo.dto.sprint;

import com.springboot.demo.model.enums.SprintStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateSprintReq(
        @NotEmpty(message = "Name required!") String name,
        LocalDate startDate,
        LocalDate endDate,
        @NotNull(message = "Status required!") SprintStatus status,
        String description,
        Boolean isBacklog
) {}
