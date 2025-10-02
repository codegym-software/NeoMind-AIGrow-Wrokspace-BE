package com.springboot.demo.controller;

import com.springboot.demo.dto.project.ProjectCreateRequest;
import com.springboot.demo.dto.project.ProjectResponse;
import com.springboot.demo.dto.project.ProjectSummaryResponse;
import com.springboot.demo.dto.project.ProjectUpdateRequest;
import com.springboot.demo.model.Project;
import com.springboot.demo.model.ProjectStatus;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
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
    public ProjectResponse create(@Valid @RequestBody ProjectCreateRequest request) {
        Project project = service.create(request);
        return ProjectResponse.from(project);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieve a project by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ProjectResponse getById(@Parameter(description = "Project ID") @PathVariable String id) {
        Project project = service.getById(id);
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
    public ProjectResponse update(@PathVariable String id,
                                @Valid @RequestBody ProjectUpdateRequest request) {
        Project project = service.update(id, request);
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
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive project", description = "Archive a project (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project archived successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ProjectResponse archive(@PathVariable String id) {
        Project project = service.archive(id);
        return ProjectResponse.from(project);
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore project", description = "Restore an archived project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project restored successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ProjectResponse restore(@PathVariable String id) {
        Project project = service.restore(id);
        return ProjectResponse.from(project);
    }

    // Workspace-scoped endpoints
    @GetMapping("/workspace/{workspaceId}")
    @Operation(summary = "Get projects by workspace", description = "Retrieve all projects belonging to a specific workspace with optional search and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Page<ProjectSummaryResponse> getByWorkspaceId(
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

    @GetMapping("/workspace/{workspaceId}/all")
    @Operation(summary = "Get all projects in workspace", description = "Retrieve all projects in a workspace (including archived)")
    public List<ProjectSummaryResponse> getAllByWorkspaceId(@PathVariable String workspaceId) {
        return service.getAllByWorkspaceId(workspaceId)
                .stream()
                .map(ProjectSummaryResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/workspace/{workspaceId}/active")
    @Operation(summary = "Get active projects", description = "Retrieve all active projects in a workspace")
    public List<ProjectSummaryResponse> getActiveProjects(@PathVariable String workspaceId) {
        return service.getActiveProjectsByWorkspaceId(workspaceId)
                .stream()
                .map(ProjectSummaryResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/workspace/{workspaceId}/overdue")
    @Operation(summary = "Get overdue projects", description = "Retrieve all overdue projects in a workspace")
    public List<ProjectSummaryResponse> getOverdueProjects(@PathVariable String workspaceId) {
        return service.getOverdueProjectsByWorkspaceId(workspaceId)
                .stream()
                .map(ProjectSummaryResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/workspace/{workspaceId}/ending-soon")
    @Operation(summary = "Get projects ending soon", description = "Retrieve projects ending within specified days")
    public List<ProjectSummaryResponse> getProjectsEndingSoon(
            @PathVariable String workspaceId,
            @Parameter(description = "Number of days to look ahead") @RequestParam(defaultValue = "7") int days) {
        return service.getProjectsEndingSoon(workspaceId, days)
                .stream()
                .map(ProjectSummaryResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/workspace/{workspaceId}/count")
    @Operation(summary = "Count projects in workspace", description = "Get the total count of projects in a workspace")
    public ResponseEntity<Long> countByWorkspaceId(@PathVariable String workspaceId) {
        long count = service.countByWorkspaceId(workspaceId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/workspace/{workspaceId}/count/status")
    @Operation(summary = "Count projects by status", description = "Get the count of projects by status in a workspace")
    public ResponseEntity<Long> countByWorkspaceIdAndStatus(
            @PathVariable String workspaceId,
            @RequestParam ProjectStatus status) {
        long count = service.countByWorkspaceIdAndStatus(workspaceId, status);
        return ResponseEntity.ok(count);
    }

    // Alternative endpoint structure: /api/workspaces/{workspaceId}/projects
    @PostMapping("/workspaces/{workspaceId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create project in workspace", description = "Create a new project within a specific workspace")
    public ProjectResponse createInWorkspace(@PathVariable String workspaceId,
                                           @Valid @RequestBody ProjectCreateRequest request) {
        // Override workspaceId from path variable
        ProjectCreateRequest requestWithWorkspaceId = new ProjectCreateRequest(
                workspaceId, request.name(), request.description(), request.status(),
                request.priority(), request.startDate(), request.endDate(), request.color()
        );
        Project project = service.create(requestWithWorkspaceId);
        return ProjectResponse.from(project);
    }

    @GetMapping("/workspaces/{workspaceId}")
    @Operation(summary = "Get projects in workspace (alternative endpoint)", description = "Alternative endpoint to get projects in a workspace")
    public Page<ProjectSummaryResponse> getByWorkspaceIdAlt(@PathVariable String workspaceId,
                                                          @RequestParam(required = false) String q,
                                                          @RequestParam(required = false) ProjectStatus status,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return getByWorkspaceId(workspaceId, q, status, page, size);
    }

    @GetMapping("/workspaces/{workspaceId}/{id}")
    @Operation(summary = "Get project in workspace", description = "Get a specific project within a workspace")
    public ProjectResponse getByIdInWorkspace(@PathVariable String workspaceId, @PathVariable String id) {
        Project project = service.getByIdAndWorkspaceId(id, workspaceId);
        return ProjectResponse.from(project);
    }

    @PutMapping("/workspaces/{workspaceId}/{id}")
    @Operation(summary = "Update project in workspace", description = "Update a project within a specific workspace")
    public ProjectResponse updateInWorkspace(@PathVariable String workspaceId,
                                           @PathVariable String id,
                                           @Valid @RequestBody ProjectUpdateRequest request) {
        Project project = service.updateByWorkspaceId(id, workspaceId, request);
        return ProjectResponse.from(project);
    }

    @DeleteMapping("/workspaces/{workspaceId}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete project in workspace", description = "Delete a project within a specific workspace")
    public void deleteInWorkspace(@PathVariable String workspaceId, @PathVariable String id) {
        service.deleteByWorkspaceId(id, workspaceId);
    }

    @PatchMapping("/workspaces/{workspaceId}/{id}/archive")
    @Operation(summary = "Archive project in workspace", description = "Archive a project within a specific workspace")
    public ProjectResponse archiveInWorkspace(@PathVariable String workspaceId, @PathVariable String id) {
        Project project = service.archiveByWorkspaceId(id, workspaceId);
        return ProjectResponse.from(project);
    }

    @PatchMapping("/workspaces/{workspaceId}/{id}/restore")
    @Operation(summary = "Restore project in workspace", description = "Restore a project within a specific workspace")
    public ProjectResponse restoreInWorkspace(@PathVariable String workspaceId, @PathVariable String id) {
        Project project = service.restoreByWorkspaceId(id, workspaceId);
        return ProjectResponse.from(project);
    }
}

