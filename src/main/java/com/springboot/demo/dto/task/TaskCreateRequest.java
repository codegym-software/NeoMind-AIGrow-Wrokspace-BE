package com.springboot.demo.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern; // Import
import java.time.LocalDate;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TaskCreateRequest {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    private String description;

    @NotBlank(message = "Status cannot be blank")

    @Pattern(regexp = "^(?i)(TODO|IN_PROGRESS|IN_REVIEW|DONE|CANCELLED)$", message = "Invalid status value")
    private String status;

    @NotBlank(message = "Priority cannot be blank")

    @Pattern(regexp = "^(?i)(LOW|MEDIUM|HIGH)$", message = "Invalid priority value")
    private String priority;

    private LocalDate dueDate;
    private Integer estimatedHours;
    
    // CHUẨN HÓA: Đổi từ assignedTo → assigneeId (FK → users.id)
    private String assigneeId;
    
    private String sprintId;
    private String tags;
    private Integer orderIndex;
}