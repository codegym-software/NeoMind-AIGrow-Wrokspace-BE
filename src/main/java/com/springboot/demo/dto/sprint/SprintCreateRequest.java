package com.springboot.demo.dto.sprint;

import com.springboot.demo.model.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SprintCreateRequest(
    
    @NotBlank(message = "Sprint name is required")
    @Size(max = 255, message = "Sprint name must not exceed 255 characters")
    String name,
    
    String description,
    
    @NotNull(message = "Start date is required")
    LocalDate startDate,
    
    @NotNull(message = "End date is required") 
    LocalDate endDate,
    
    SprintStatus status,
    
    String goal
) {}
