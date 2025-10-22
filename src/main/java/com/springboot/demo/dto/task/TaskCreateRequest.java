package com.springboot.demo.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TaskCreateRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private String status;
    @NotNull
    private String priority;
    private LocalDate dueDate;
    private Integer estimatedHours;
    private Integer actualHours;
    private String assignedTo;
    private String sprintId;
    private String tags;
    private Integer orderIndex;

    
}