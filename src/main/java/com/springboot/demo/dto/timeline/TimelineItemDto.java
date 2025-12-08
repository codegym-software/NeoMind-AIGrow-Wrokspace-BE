package com.springboot.demo.dto.timeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho Timeline Item (Gantt Chart)
 * Đại diện cho cả Epic và Task trong cùng 1 cấu trúc phẳng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimelineItemDto {
    
    /**
     * ID với prefix theo type: "epic-1", "task-10"
     * Tránh trùng lặp giữa Epic và Task
     */
    private String id;
    
    /**
     * Tên hiển thị (title/name)
     */
    private String text;
    
    /**
     * Ngày bắt đầu (sau khi áp dụng soft inheritance)
     * Task: task.start_date ?? sprint.start_date
     * Epic: epic.start_date
     */
    private LocalDate startDate;
    
    /**
     * Ngày kết thúc (sau khi áp dụng soft inheritance)
     * Task: task.due_date ?? sprint.end_date
     * Epic: epic.end_date
     */
    private LocalDate endDate;
    
    /**
     * ID của parent (Epic ID cho Task, null cho Epic)
     * Format: "epic-{id}" nếu có epic_id
     */
    private String parent;
    
    /**
     * Tiến độ hoàn thành (0.0 - 1.0)
     * - Task Done: 1.0 (100%)
     * - Task khác: 0.0 (0%)
     * - Epic: Tính từ % tasks hoàn thành
     */
    private Double progress;
    
    /**
     * Loại item: "epic" hoặc "task"
     */
    private String type;
    
    /**
     * Ngày hoàn thành thực tế (chỉ cho Task)
     * Dùng để so sánh Kế hoạch vs Thực tế
     */
    private LocalDate completedDate;
    
    /**
     * Project ID (để filter)
     */
    private String projectId;
    
    /**
     * Sprint ID (chỉ cho Task)
     */
    private Integer sprintId;
    
    /**
     * Trạng thái (Done, In Progress, etc.)
     */
    private String status;
}
