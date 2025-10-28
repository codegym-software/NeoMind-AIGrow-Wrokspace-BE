package com.springboot.demo.controller;

import com.springboot.demo.dto.project.*;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


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

    @PatchMapping("/projects/{projectId}")
    @PreAuthorize("@gate.allows('project:update', authentication, #projectId)")
    public ProjectDto update(@PathVariable String projectId, @Valid @RequestBody UpdateProjectReq req) {
        return service.update(projectId, req);
    }

    @DeleteMapping("/projects/{projectId}")
    @PreAuthorize("@gate.allows('project:delete', authentication, #projectId)")
    public void delete(@PathVariable String projectId) {
        service.delete(projectId);
    }

    @GetMapping("/projects/mine")
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDto> listMine(@RequestParam(required = false) String workspaceId,
                                     @RequestParam(required = false) String companyId,
                                     Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        if (companyId != null && !companyId.isBlank()) {
            return service.listMineInCompany(companyId, userId);
        }
        return service.listMine(workspaceId, userId);
    }

    @GetMapping("/companies/{companyId}/projects/mine")
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDto> listMineInCompany(@PathVariable String companyId,
                                              Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        return service.listMineInCompany(companyId, userId);
    }

    @GetMapping("/projects/{projectId}")
    @PreAuthorize("@gate.allows('project:read', authentication, #projectId)")
    public ProjectDto getById(@PathVariable String projectId) {
        return service.getProjectById(projectId);
    }

}
