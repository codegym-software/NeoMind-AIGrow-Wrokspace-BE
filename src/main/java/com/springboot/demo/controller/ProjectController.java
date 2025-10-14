package com.springboot.demo.controller;

import com.springboot.demo.dto.project.*;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService service;
    private final UserIdResolver userIdResolver;

    @PostMapping("/workspaces/{workspaceId}/projects")
    @PreAuthorize("@gate.allows('project:create', authentication, #workspaceId)")
    public ProjectDto create(@PathVariable String workspaceId,
                             @RequestBody CreateProjectReq req,
                             Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        return service.create(workspaceId, req, userId);
    }

    @GetMapping("/workspaces/{workspaceId}/projects")
    @PreAuthorize("@gate.allows('project:read', authentication, #workspaceId)")
    public List<ProjectDto> listInWorkspace(@PathVariable String workspaceId,
                                            @RequestParam(defaultValue = "false") boolean includeArchived,
                                            @RequestParam(required = false, name = "q") String keyword) {
        return service.listInWorkspace(workspaceId, includeArchived, keyword);
    }

    @PatchMapping("/projects/{projectId}/rename")
    @PreAuthorize("@gate.allows('project:update', authentication, #projectId)")
    public ProjectDto rename(@PathVariable String projectId,
                             @RequestBody RenameProjectReq req) {
        return service.rename(projectId, req.name());
    }

    @DeleteMapping("/projects/{projectId}")
    @PreAuthorize("@gate.allows('project:delete', authentication, #projectId)")
    public void delete(@PathVariable String projectId) {
        service.delete(projectId);
    }

    @GetMapping("/projects/mine")
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDto> listMine(@RequestParam(required = false) String workspaceId,
                                     Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        return service.listMine(workspaceId, userId);
    }
}
