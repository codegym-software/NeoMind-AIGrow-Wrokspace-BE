package com.springboot.demo.controller;

import com.springboot.demo.dto.board.TaskMoveRequest;
import com.springboot.demo.dto.task.TaskSearchRequest;
import com.springboot.demo.model.Epic;
import com.springboot.demo.dto.task.TaskGroupedResponse;
import com.springboot.demo.dto.task.TaskUpdateRequest;
import com.springboot.demo.model.Task;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// DTO phản hồi chung
record ApiResponse<T>(String message, T data) {}

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserIdResolver userIdResolver;

    @PostMapping
    @PreAuthorize("@gate.allows('project:task:create', authentication, #projectId)")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Task> create(@PathVariable String projectId,
                                    @RequestBody Task task,
                                    Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        task.setProjectId(projectId);
        Task created = taskService.create(task, userId);
        return new ApiResponse<>("Task created successfully", created);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("@gate.allows('project:task:update', authentication, #projectId)")
    public ApiResponse<Task> update(@PathVariable String projectId,
                                    @PathVariable Long taskId, // <-- SỬA Ở ĐÂY
                                    @RequestBody Task task,
                                    Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        task.setProjectId(projectId);
        Task updated = taskService.update(taskId, task, userId); // <-- Truyền Long vào
        return new ApiResponse<>("Task updated successfully", updated);
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("@gate.allows('project:task:delete', authentication, #projectId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable String projectId,
                                    @PathVariable Long taskId, // <-- SỬA Ở ĐÂY
                                    Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        taskService.delete(taskId, userId); // <-- Truyền Long vào
        return new ApiResponse<>("Task deleted successfully", null);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Task> get(@PathVariable String projectId,
                                 @PathVariable Long taskId, // <-- SỬA Ở ĐÂY
                                 Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Task task = taskService.get(taskId, userId); // <-- Truyền Long vào
        return new ApiResponse<>("Task retrieved successfully", task);
    }

    // List tasks by project (Paginated)
    @GetMapping
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Page<Task>> listByProject(@PathVariable String projectId,
                                                 Authentication authentication,
                                                 Pageable pageable) {
        String userId = userIdResolver.resolveInternalId(authentication);

        Page<Task> tasksPage = taskService.listByProject(projectId, userId, pageable);
        return new ApiResponse<>("Tasks listed successfully", tasksPage);
    }

    // --- BẮT ĐẦU PHẦN HỢP NHẤT ---

    // 1. Search tasks (Từ nhánh 'list')
    @PostMapping("/search")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Page<Task>> search(@PathVariable String projectId,
                                          @RequestBody TaskSearchRequest request,
                                          Authentication authentication,
                                          Pageable pageable) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Page<Task> tasksPage = taskService.search(projectId, userId, request, pageable);
        return new ApiResponse<>("Tasks searched successfully", tasksPage);
    }

    // 2. Metrics Last 7 Days (Từ nhánh 'main')
    @GetMapping("/metrics/7d")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Map<String, Long>> metricsLast7Days(@PathVariable String projectId,
                                                           Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Map<String, Long> metrics = taskService.metricsLast7Days(projectId, userId);
        return new ApiResponse<>("Metrics retrieved successfully", metrics);
    }

    // 3. Status Distribution (Từ nhánh 'main')
    @GetMapping("/metrics/status")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Map<String, Long>> statusDistribution(@PathVariable String projectId,
                                                             Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Map<String, Long> dist = taskService.statusDistribution(projectId, userId);
        return new ApiResponse<>("Status distribution retrieved successfully", dist);
    }

    @PatchMapping("/columns/{sourceColumnId}/transfer/{targetColumnId}")
    @PreAuthorize("@gate.allows('project:update', authentication, #projectId)")
    public ApiResponse<Void> transferTasksFromColumn(@PathVariable String projectId,
                                                     @PathVariable Integer sourceColumnId,
                                                     @PathVariable Integer targetColumnId,
                                                     Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        taskService.transferTasksFromColumn(projectId, sourceColumnId, targetColumnId, userId);
        return new ApiResponse<>("Tasks transferred successfully", null);
    }

    @GetMapping("/metrics/priority")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Map<String, Long>> priorityDistribution(@PathVariable String projectId,
                                                               Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Map<String, Long> dist = taskService.priorityDistribution(projectId, userId);
        return new ApiResponse<>("Priority distribution retrieved successfully", dist);
    }

    @GetMapping("/metrics/workload")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Map<String, Long>> workloadByAssignee(@PathVariable String projectId,
                                                             Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Map<String, Long> dist = taskService.workloadByAssignee(projectId, userId);
        return new ApiResponse<>("Workload distribution retrieved successfully", dist);
    }

    @GetMapping("/metrics/recent-log")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<java.util.List<com.springboot.demo.model.ActivityLog>> recentLogs(@PathVariable String projectId,
                                                                                        Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        java.util.List<com.springboot.demo.model.ActivityLog> logs = taskService.recentLogs(projectId, userId);
        return new ApiResponse<>("Recent logs retrieved successfully", logs);
    }

    @PatchMapping("/{taskId}/move")
    @PreAuthorize("@gate.allows('project:task:update', authentication, #projectId)")
    public ApiResponse<Task> moveTask(@PathVariable String projectId,
                                      @PathVariable Long taskId,
                                      @RequestBody TaskMoveRequest request,
                                      Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Task moved = taskService.moveTask(taskId, request, userId);
        return new ApiResponse<>("Task moved successfully", moved);
    }

    @PatchMapping("/{taskId}/archive")
    @PreAuthorize("@gate.allows('project:task:delete', authentication, #projectId)")
    public ApiResponse<Task> archive(@PathVariable String projectId,
                                     @PathVariable Long taskId,
                                     Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Task archived = taskService.archive(taskId, userId);
        return new ApiResponse<>("Task archived successfully", archived);
    }

    @DeleteMapping("/{taskId}/permanent")
    @PreAuthorize("@gate.allows('project:task:delete', authentication, #projectId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deletePermanent(@PathVariable String projectId,
                                             @PathVariable Long taskId,
                                             Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        taskService.delete(taskId, userId);
        return new ApiResponse<>("Task permanently deleted", null);
    }

    @PatchMapping("/{taskId}/restore")
    @PreAuthorize("@gate.allows('project:task:update', authentication, #projectId)")
    public ApiResponse<Task> restore(@PathVariable String projectId,
                                     @PathVariable Long taskId,
                                     Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Task restored = taskService.restore(taskId, userId);
        return new ApiResponse<>("Task restored successfully", restored);
    }

    @GetMapping("/archived")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Page<Task>> listArchivedTasks(@PathVariable String projectId,
                                                     Authentication authentication,
                                                     Pageable pageable) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Page<Task> archivedTasks = taskService.listArchivedByProject(projectId, userId, pageable);
        return new ApiResponse<>("Archived tasks listed successfully", archivedTasks);
    }

    @PatchMapping("/{taskId}/epic")
    @PreAuthorize("@gate.allows('project:task:update', authentication, #projectId)")
    public ApiResponse<Task> setEpic(@PathVariable String projectId,
                                     @PathVariable Long taskId,
                                     @RequestParam(required = false) Integer epicId,
                                     Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Task updated = taskService.setEpic(taskId, epicId, userId);
        return new ApiResponse<>("Task epic updated successfully", updated);
    }

    // List My Tasks (Global/Assigned to current user)
    @GetMapping("/my-tasks")
    @PreAuthorize("isAuthenticated()") // Không cần #projectId vì đây là global
    public ApiResponse<Page<Task>> listMyTasks(Authentication authentication,
                                               Pageable pageable) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Page<Task> myTasks = taskService.listMyTasks(userId, pageable);
        return new ApiResponse<>("My tasks retrieved successfully", myTasks);
    }

    // Backward-compatible alias for legacy frontend route
    @GetMapping("/api/my-tasks")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<Task>> listMyTasksAlias(Authentication authentication,
                                                    Pageable pageable) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Page<Task> myTasks = taskService.listMyTasks(userId, pageable);
        return new ApiResponse<>("My tasks retrieved successfully", myTasks);
    }
}
