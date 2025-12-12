package com.springboot.demo.service;

import com.springboot.demo.dto.timeline.TimelineItemDto;
import com.springboot.demo.dto.timeline.TimelineResponse;
import com.springboot.demo.dto.timeline.TimelineUpdateRequest;
import com.springboot.demo.dto.timeline.TaskCompleteRequest;

import java.util.List;

/**
 * Service cho Timeline/Gantt Chart
 */
public interface TimelineService {
    
    /**
     * Lấy dữ liệu Timeline cho một project
     * Trả về TimelineResponse bao gồm items (Epic/Task) và sprints
     * 
     * @param projectId ID của project
     * @param userId ID của user hiện tại (để check quyền)
     * @return TimelineResponse với items và sprints
     */
    TimelineResponse getTimeline(String projectId, String userId);
    
    /**
     * Cập nhật Timeline khi kéo thả task trên Gantt Chart
     * Update start_date và due_date của task
     * 
     * @param taskId ID của task
     * @param request Timeline update request
     * @param userId ID của user hiện tại
     * @return TimelineItemDto đã được update
     */
    TimelineItemDto updateTaskTimeline(Long taskId, TimelineUpdateRequest request, String userId);
    
    /**
     * Cập nhật Timeline khi kéo thả Epic trên Gantt Chart
     * Update start_date và end_date của Epic
     * 
     * @param epicId ID của Epic
     * @param request Timeline update request
     * @param userId ID của user hiện tại
     * @return TimelineItemDto đã được update
     */
    TimelineItemDto updateEpicTimeline(Integer epicId, TimelineUpdateRequest request, String userId);
    
    /**
     * Đánh dấu task hoàn thành hoặc uncomplete
     * Update status và completed_at
     * LƯU Ý: KHÔNG thay đổi due_date để giữ nguyên kế hoạch
     * 
     * @param taskId ID của task
     * @param request Complete request
     * @param userId ID của user hiện tại
     * @return TimelineItemDto đã được update
     */
    TimelineItemDto completeTask(Long taskId, TaskCompleteRequest request, String userId);
}
