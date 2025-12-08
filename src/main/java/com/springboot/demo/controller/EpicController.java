package com.springboot.demo.controller;

import com.springboot.demo.dto.epic.*;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.EpicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.springboot.demo.dto.task.TaskResponse;
import com.springboot.demo.model.Task;
import com.springboot.demo.repository.EpicRepository;
import com.springboot.demo.service.TaskService;

@RestController
@RequestMapping("/api/projects/{projectId}/epics")
@RequiredArgsConstructor
public class EpicController {

    private final EpicService epicService;
    private final UserIdResolver userIdResolver;
    private final TaskService taskService;
    private final EpicRepository epicRepository;

    record ApiResponse<T>(String message, T data) {}

    @PostMapping
    @PreAuthorize("@gate.allows('project:epic:create', authentication, #projectId)")
    public ApiResponse<EpicDto> create(@PathVariable String projectId,
                                       @Valid @RequestBody EpicCreateRequest req,
                                       Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        EpicDto created = epicService.create(projectId, req, userId);
        return new ApiResponse<>("Epic created successfully", created);
    }

    @GetMapping
    @PreAuthorize("@gate.allows('project:epic:read', authentication, #projectId)")
    public ApiResponse<EpicListResponse> list(@PathVariable String projectId,
                                           Authentication authentication,
                                           Pageable pageable) {
        String userId = userIdResolver.resolveInternalId(authentication);
        EpicListResponse res = epicService.listByProject(projectId, userId, pageable);
        return new ApiResponse<>("Epics listed successfully", res);
    }

    @PostMapping("/search")
    @PreAuthorize("@gate.allows('project:epic:read', authentication, #projectId)")
    public ApiResponse<Page<EpicDto>> search(@PathVariable String projectId,
                                             @RequestBody EpicSearchRequest req,
                                             Authentication authentication,
                                             Pageable pageable) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Page<EpicDto> page = epicService.search(projectId, userId, req, pageable);
        return new ApiResponse<>("Epics searched successfully", page);
    }

    @GetMapping("/{epicId}")
    @PreAuthorize("@gate.allows('project:epic:read', authentication, #projectId)")
    public ApiResponse<EpicDto> get(@PathVariable String projectId,
                                    @PathVariable Integer epicId,
                                    Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        EpicDto dto = epicService.get(projectId, epicId, userId);
        return new ApiResponse<>("Epic retrieved successfully", dto);
    }

    @PutMapping("/{epicId}")
    @PreAuthorize("@gate.allows('project:epic:update', authentication, #projectId)")
    public ApiResponse<EpicDto> update(@PathVariable String projectId,
                                       @PathVariable Integer epicId,
                                       @Valid @RequestBody EpicUpdateRequest req,
                                       Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        EpicDto updated = epicService.update(projectId, epicId, req, userId);
        return new ApiResponse<>("Epic updated successfully", updated);
    }

    @DeleteMapping("/{epicId}")
    @PreAuthorize("@gate.allows('project:epic:delete', authentication, #projectId)")
    public ApiResponse<Void> delete(@PathVariable String projectId,
                                    @PathVariable Integer epicId,
                                    Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        epicService.delete(projectId, epicId, userId);
        return new ApiResponse<>("Epic deleted successfully", null);
    }

    @GetMapping("/{epicId}/tasks")
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public ApiResponse<Page<TaskResponse>> listTasks(@PathVariable String projectId,
                                                     @PathVariable Integer epicId,
                                                     Authentication authentication,
                                                     Pageable pageable) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Page<TaskResponse> page = epicService.listTasks(projectId, epicId, userId, pageable);
        return new ApiResponse<>("Epic tasks listed successfully", page);
    }

    @PostMapping("/{epicId}/tasks")
    @PreAuthorize("@gate.allows('project:task:create', authentication, #projectId)")
    public ApiResponse<Task> createTaskInEpic(@PathVariable String projectId,
                                              @PathVariable Integer epicId,
                                              @RequestBody Task task,
                                              Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        epicRepository.findById(epicId)
                .filter(e -> projectId.equals(e.getProjectId()))
                .orElseThrow(() -> new RuntimeException("Epic not found in project"));
        task.setProjectId(projectId);
        task.setEpicId(epicId);
        Task created = taskService.create(task, userId);
        return new ApiResponse<>("Task created in epic successfully", created);
    }
}
