package com.springboot.demo.dto.task;

import java.time.LocalDate;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TaskUpdateRequest {
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private Integer estimatedHours;
    private Integer actualHours;
    private String assignedTo;
    private String sprintId;
    private String tags;
    private Integer orderIndex;

   
}
