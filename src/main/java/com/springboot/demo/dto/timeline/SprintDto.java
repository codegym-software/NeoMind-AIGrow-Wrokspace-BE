package com.springboot.demo.dto.timeline;

import com.springboot.demo.model.enums.SprintStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho Sprint trong Timeline (Gantt Chart)
 * Hiển thị Sprint headers trên đầu biểu đồ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SprintDto {
    
    /**
     * Sprint ID (dùng để link với Task.sprintId)
     */
    private Integer id;
    
    /**
     * Tên Sprint
     */
    private String name;
    
    /**
     * Ngày bắt đầu Sprint
     */
    private LocalDate startDate;
    
    /**
     * Ngày kết thúc Sprint
     */
    private LocalDate endDate;
    
    /**
     * Trạng thái Sprint: planned, active, completed
     */
    private SprintStatus status;
}
