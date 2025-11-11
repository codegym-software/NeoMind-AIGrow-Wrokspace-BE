package com.springboot.demo.dto.task;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TaskUpdateRequest {
    
    
    @Size(min = 1, message = "Title cannot be empty")
    private String title;
    
    private String description;

    @Pattern(regexp = "^(?i)(TODO|IN_PROGRESS|IN_REVIEW|DONE|CANCELLED)$", message = "Invalid status value")
    private String status;

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