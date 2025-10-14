package com.springboot.demo.controller;

import com.springboot.demo.dto.project.ProjectCreateRequest;
import com.springboot.demo.dto.project.ProjectResponse;
import com.springboot.demo.dto.project.ProjectSummaryResponse;
import com.springboot.demo.dto.project.ProjectUpdateRequest;
import com.springboot.demo.model.Project;
import com.springboot.demo.enums.ProjectStatus;
import com.springboot.demo.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
@Tag(name = "Project Management", description = "APIs for managing projects within workspaces")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new project", description = "Create a new project within a workspace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ProjectResponse create(@PathVariable String workspaceId, @Valid @RequestBody ProjectCreateRequest request) {
        // Override workspaceId from path variable
        ProjectCreateRequest requestWithWorkspaceId = new ProjectCreateRequest(
                workspaceId, request.name(), request.description(), request.status(), request.color()
        );
        Project project = service.create(requestWithWorkspaceId);
        return ProjectResponse.from(project);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieve a project by its unique identifier within workspace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ProjectResponse getById(@PathVariable String workspaceId, @Parameter(description = "Project ID") @PathVariable String id) {
        Project project = service.getByIdAndWorkspaceId(id, workspaceId);
        return ProjectResponse.from(project);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Update an existing project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ProjectResponse update(@PathVariable String workspaceId, @PathVariable String id,
                                @Valid @RequestBody ProjectUpdateRequest request) {
        Project project = service.updateByWorkspaceId(id, workspaceId, request);
        return ProjectResponse.from(project);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete project", description = "Permanently delete a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public void delete(@PathVariable String workspaceId, @PathVariable String id) {
        service.deleteByWorkspaceId(id, workspaceId);
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive project", description = "Archive a project (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project archived successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ProjectResponse archive(@PathVariable String workspaceId, @PathVariable String id) {
        Project project = service.archiveByWorkspaceId(id, workspaceId);
        return ProjectResponse.from(project);
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore project", description = "Restore an archived project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project restored successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ProjectResponse restore(@PathVariable String workspaceId, @PathVariable String id) {
        Project project = service.restoreByWorkspaceId(id, workspaceId);
        return ProjectResponse.from(project);
    }

    @GetMapping
    @Operation(summary = "Get projects by workspace", description = "Retrieve all projects belonging to a specific workspace with optional search and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Page<ProjectSummaryResponse> getAll(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId,
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "Filter by status") @RequestParam(required = false) ProjectStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Page<Project> projects;
        if (q != null && !q.trim().isEmpty()) {
            projects = service.searchByWorkspaceId(workspaceId, q, page, size);
        } else if (status != null) {
            projects = service.getByWorkspaceIdAndStatus(workspaceId, status, page, size);
        } else {
            projects = service.getByWorkspaceId(workspaceId, page, size);
        }

        return projects.map(ProjectSummaryResponse::from);
    }
}

