package com.springboot.demo.dto.timeline;

import com.springboot.demo.validation.ValidDateRange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO cho việc cập nhật Timeline (kéo thả trên Gantt Chart)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange(startField = "startDate", endField = "dueDate", message = "Due date must be after or equal to start date")
public class TimelineUpdateRequest {
    
    /**
     * Ngày bắt đầu mới (sau khi kéo thả)
     * Update trực tiếp vào task.start_date
     * Optional: Có thể null nếu chỉ muốn update dueDate
     */
    private LocalDate startDate;
    
    /**
     * Ngày kết thúc mới (sau khi kéo thả)
     * Update trực tiếp vào task.due_date
     * Optional: Có thể null nếu chỉ muốn update startDate
     * LƯU Ý: Đây là kế hoạch, KHÔNG thay đổi khi task completed
     */
    private LocalDate dueDate;
}
