package com.springboot.demo.controller;

import com.springboot.demo.dto.timeline.TaskCompleteRequest;
import com.springboot.demo.dto.timeline.TimelineItemDto;
import com.springboot.demo.dto.timeline.TimelineResponse;
import com.springboot.demo.dto.timeline.TimelineUpdateRequest;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho Timeline/Gantt Chart
 * Endpoints: GET timeline, PATCH update timeline, PATCH complete task
 */
@RestController
@RequestMapping("/api/projects/{projectId}/timeline")
@RequiredArgsConstructor
public class TimelineController {
    
    private final TimelineService timelineService;
    private final UserIdResolver userIdResolver;
    
    record ApiResponse<T>(String message, T data) {}
    
    /**
     * GET /api/projects/{projectId}/timeline
     * Lấy dữ liệu Timeline cho Gantt Chart
     * 
     * Trả về object bao gồm:
     * - items: Danh sách Epic và Task (với soft inheritance)
     * - sprints: Danh sách Sprint headers để hiển thị trên biểu đồ
     */
    @GetMapping
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<TimelineResponse> getTimeline(
            @PathVariable String projectId,
            Authentication authentication) {
        
        String userId = userIdResolver.resolveInternalId(authentication);
        TimelineResponse timeline = timelineService.getTimeline(projectId, userId);
        
        return new ApiResponse<>("Timeline retrieved successfully", timeline);
    }
    
    /**
     * PATCH /api/projects/{projectId}/timeline/tasks/{taskId}
     * Cập nhật Timeline khi kéo thả task trên Gantt Chart
     * 
     * Update trực tiếp start_date và due_date của task
     * Ghi đè kế hoạch mặc định của Sprint
     */
    @PatchMapping("/tasks/{taskId}")
    @PreAuthorize("@gate.allows('project:task:update', authentication, #projectId)")
    public ApiResponse<TimelineItemDto> updateTaskTimeline(
            @PathVariable String projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TimelineUpdateRequest request,
            Authentication authentication) {
        
        String userId = userIdResolver.resolveInternalId(authentication);
        TimelineItemDto updated = timelineService.updateTaskTimeline(taskId, request, userId);
        
        return new ApiResponse<>("Task timeline updated successfully", updated);
    }
    
    /**
     * PATCH /api/projects/{projectId}/timeline/epics/{epicId}
     * Cập nhật Timeline khi kéo thả Epic trên Gantt Chart
     * 
     * Update trực tiếp start_date và end_date của Epic
     */
    @PatchMapping("/epics/{epicId}")
    @PreAuthorize("@gate.allows('project:epic:update', authentication, #projectId)")
    public ApiResponse<TimelineItemDto> updateEpicTimeline(
            @PathVariable String projectId,
            @PathVariable Integer epicId,
            @Valid @RequestBody TimelineUpdateRequest request,
            Authentication authentication) {
        
        String userId = userIdResolver.resolveInternalId(authentication);
        TimelineItemDto updated = timelineService.updateEpicTimeline(epicId, request, userId);
        
        return new ApiResponse<>("Epic timeline updated successfully", updated);
    }
    
    /**
     * PATCH /api/projects/{projectId}/timeline/tasks/{taskId}/complete
     * Đánh dấu task hoàn thành hoặc uncomplete
     * 
     * Update status = Done và completed_at = NOW
     * LƯU Ý: KHÔNG thay đổi due_date để giữ nguyên kế hoạch
     */
    @PatchMapping("/tasks/{taskId}/complete")
    @PreAuthorize("@gate.allows('project:task:update', authentication, #projectId)")
    public ApiResponse<TimelineItemDto> completeTask(
            @PathVariable String projectId,
            @PathVariable Long taskId,
            @RequestBody TaskCompleteRequest request,
            Authentication authentication) {
        
        String userId = userIdResolver.resolveInternalId(authentication);
        TimelineItemDto updated = timelineService.completeTask(taskId, request, userId);
        
        return new ApiResponse<>("Task completion status updated successfully", updated);
    }
}
