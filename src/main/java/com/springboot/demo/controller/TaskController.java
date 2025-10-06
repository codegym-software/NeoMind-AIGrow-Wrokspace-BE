package com.springboot.demo.controller;

import com.springboot.demo.model.Task;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// DTO phản hồi chung
record ApiResponse<T>(String message, T data) {}

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserIdResolver userIdResolver;

    // Create task
    @PostMapping
    @PreAuthorize("@gate.allows('task:create', authentication, #projectId)")
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
    @PreAuthorize("@gate.allows('task:update', authentication, #projectId)")
    public ApiResponse<Task> update(@PathVariable String projectId,
                                    @PathVariable Long taskId,
                                    @RequestBody Task task,
                                    Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        task.setProjectId(projectId);
        Task updated = taskService.update(taskId, task, userId);
        return new ApiResponse<>("Task updated successfully", updated);
    }

    // Delete task
    @DeleteMapping("/{taskId}")
    @PreAuthorize("@gate.allows('task:delete', authentication, #projectId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable String projectId,
                                    @PathVariable Long taskId,
                                    Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        taskService.delete(taskId, userId);
        return new ApiResponse<>("Task deleted successfully", null);
    }

    // Get task detail
    @GetMapping("/{taskId}")
    @PreAuthorize("@gate.allows('task:read', authentication, #projectId)")
    public ApiResponse<Task> get(@PathVariable String projectId,
                                 @PathVariable Long taskId,
                                 Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Task task = taskService.get(taskId, userId);
        return new ApiResponse<>("Task retrieved successfully", task);
    }

    // List tasks by project
    @GetMapping
    @PreAuthorize("@gate.allows('task:read', authentication, #projectId)")
    public ApiResponse<List<Task>> listByProject(@PathVariable String projectId,
                                                 Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        List<Task> tasks = taskService.listByProject(projectId, userId);
        return new ApiResponse<>("Tasks listed successfully", tasks);
    }
}
