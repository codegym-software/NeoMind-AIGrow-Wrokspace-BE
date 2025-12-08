package com.springboot.demo.dto.timeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper cho Timeline API
 * Bao gồm cả items (Epic/Task) và sprints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimelineResponse {
    
    /**
     * Danh sách items (Epic và Task)
     * Giữ nguyên logic cũ với soft inheritance
     */
    private List<TimelineItemDto> items;
    
    /**
     * Danh sách Sprints để hiển thị Sprint headers trên Gantt Chart
     * Chỉ bao gồm các Sprint có đầy đủ start_date và end_date
     */
    private List<SprintDto> sprints;
}
