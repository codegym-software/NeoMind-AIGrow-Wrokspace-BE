package com.springboot.demo.controller;

import com.springboot.demo.model.Task;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

// DTO phản hồi chung
record ApiResponse<T>(String message, T data) {
}

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserIdResolver userIdResolver;

    // Create task
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

    // Update task
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

    // Delete task
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

    // Get task detail
    @GetMapping("/{taskId}")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Task> get(@PathVariable String projectId,
                                 @PathVariable Long taskId, // <-- SỬA Ở ĐÂY
                                 Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Task task = taskService.get(taskId, userId); // <-- Truyền Long vào
        return new ApiResponse<>("Task retrieved successfully", task);
    }

    // List tasks by project
    @GetMapping
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Page<Task>> listByProject(@PathVariable String projectId,
                                                  Authentication authentication,
                                                  Pageable pageable) {

        String userId = userIdResolver.resolveInternalId(authentication);

        Page<Task> tasksPage = taskService.listByProject(projectId, userId, pageable);
        return new ApiResponse<>("Tasks listed successfully", tasksPage);
    }
}