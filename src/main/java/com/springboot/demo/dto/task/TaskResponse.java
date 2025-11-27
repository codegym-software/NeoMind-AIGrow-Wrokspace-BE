package com.springboot.demo.dto.task;

import java.time.LocalDate;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private Integer estimatedHours;
    
    // CHUẨN HÓA: Đổi từ assignedTo → assigneeId (FK → users.id)
    private String assigneeId;
    
    private String sprintId;
    private Integer epicId;
    private String epicTitle;
    private String tags;
    private Integer orderIndex;
    private String projectId;

    
}
