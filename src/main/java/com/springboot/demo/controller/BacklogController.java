package com.springboot.demo.controller;

import com.springboot.demo.dto.task.*;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.BacklogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/backlog")
@RequiredArgsConstructor
public class BacklogController {

    private final BacklogService backlogService;
    private final UserIdResolver userIdResolver;

    // Xem toàn bộ task trong backlog (sắp xếp theo order đã replay)
    @GetMapping
    @PreAuthorize("@gate.allows('project:task:read', authentication, #projectId)")
    public TaskListResponse list(@PathVariable String projectId) {
        return backlogService.listBacklog(projectId);
    }

    // Sắp xếp (drag & drop) + lưu lịch sử vào comments
    @PatchMapping("/order")
    @PreAuthorize("@gate.allows('project:task:update', authentication, #projectId)")
    public ReorderResult reorder(@PathVariable String projectId,
                                 @RequestBody ReorderRequest req,
                                 Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        return backlogService.reorderBacklog(projectId, req, userId);
    }
}
