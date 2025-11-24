package com.springboot.demo.controller;

import com.springboot.demo.dto.board.TaskMoveRequest;
import com.springboot.demo.dto.task.TaskSearchRequest;
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

record ApiResponse<T>(String message, T data) {
}

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

    @GetMapping
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Page<Task>> listByProject(@PathVariable String projectId,
                                                  Authentication authentication,
                                                  Pageable pageable) {

        String userId = userIdResolver.resolveInternalId(authentication);

        Page<Task> tasksPage = taskService.listByProject(projectId, userId, pageable);
        return new ApiResponse<>("Tasks listed successfully", tasksPage);
    }

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
}