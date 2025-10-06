package com.springboot.demo.dto.sprint;

import com.springboot.demo.model.SprintStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SprintUpdateRequest(
    
    @Size(max = 255, message = "Sprint name must not exceed 255 characters")
    String name,
    
    // removed description (not in DB)
    
    LocalDate startDate,
    
    LocalDate endDate,
    
    SprintStatus status,
    
    Boolean isBacklog,
    
    String goal
) {}
