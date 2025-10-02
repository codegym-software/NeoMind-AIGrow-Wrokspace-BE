package com.springboot.demo.controller;

import com.springboot.demo.dto.sprint.SprintCreateRequest;
import com.springboot.demo.dto.sprint.SprintProgressResponse;
import com.springboot.demo.dto.sprint.SprintResponse;
import com.springboot.demo.dto.sprint.SprintUpdateRequest;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.SprintStatus;
import com.springboot.demo.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprint Management", description = "APIs for managing sprints and tracking progress")
public class SprintController {
    
    private final SprintService sprintService;
    
    @PostMapping("/projects/{projectId}")
    @Operation(summary = "Create a new sprint", description = "Create a new sprint in a project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sprint created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<SprintResponse> createSprint(
            @Parameter(description = "Project ID") @PathVariable String projectId,
            @Valid @RequestBody SprintCreateRequest request) {
        
        Sprint sprint = sprintService.create(projectId, request);
        SprintResponse response = mapToResponse(sprint);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get sprint by ID", description = "Retrieve sprint details with progress information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint found"),
        @ApiResponse(responseCode = "404", description = "Sprint not found")
    })
    public ResponseEntity<SprintResponse> getSprintById(
            @Parameter(description = "Sprint ID") @PathVariable String id) {
        
        Sprint sprint = sprintService.getById(id);
        SprintResponse response = mapToResponse(sprint);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/projects/{projectId}")
    @Operation(summary = "Get sprints by project", description = "Get all sprints for a project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprints retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<List<SprintResponse>> getSprintsByProject(
            @Parameter(description = "Project ID") @PathVariable String projectId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) SprintStatus status) {
        
        List<Sprint> sprints = status != null 
            ? sprintService.getSprintsByProjectAndStatus(projectId, status)
            : sprintService.getSprintsByProject(projectId);
            
        List<SprintResponse> responses = sprints.stream()
            .map(this::mapToResponse)
            .toList();
            
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/projects/{projectId}/active")
    @Operation(summary = "Get active sprint", description = "Get the currently active sprint for a project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active sprint found"),
        @ApiResponse(responseCode = "404", description = "No active sprint found")
    })
    public ResponseEntity<SprintResponse> getActiveSprint(
            @Parameter(description = "Project ID") @PathVariable String projectId) {
        
        Sprint sprint = sprintService.getActiveSprintByProject(projectId);
        SprintResponse response = mapToResponse(sprint);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/progress")
    @Operation(summary = "Get sprint progress", description = "Get detailed progress information for a sprint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Sprint not found")
    })
    public ResponseEntity<SprintProgressResponse> getSprintProgress(
            @Parameter(description = "Sprint ID") @PathVariable String id) {
        
        SprintProgressResponse progress = sprintService.getSprintProgress(id);
        return ResponseEntity.ok(progress);
    }
    
    @GetMapping("/projects/{projectId}/active/progress")
    @Operation(summary = "Get active sprint progress", description = "Get progress for the active sprint in a project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "No active sprint found")
    })
    public ResponseEntity<SprintProgressResponse> getActiveSprintProgress(
            @Parameter(description = "Project ID") @PathVariable String projectId) {
        
        SprintProgressResponse progress = sprintService.getActiveSprintProgress(projectId);
        return ResponseEntity.ok(progress);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update sprint", description = "Update sprint details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint updated successfully"),
        @ApiResponse(responseCode = "404", description = "Sprint not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<SprintResponse> updateSprint(
            @Parameter(description = "Sprint ID") @PathVariable String id,
            @Valid @RequestBody SprintUpdateRequest request) {
        
        Sprint sprint = sprintService.update(id, request);
        SprintResponse response = mapToResponse(sprint);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/projects/{projectId}/{id}")
    @Operation(summary = "Update sprint by project", description = "Update sprint within a specific project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint updated successfully"),
        @ApiResponse(responseCode = "404", description = "Sprint not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<SprintResponse> updateSprintByProject(
            @Parameter(description = "Project ID") @PathVariable String projectId,
            @Parameter(description = "Sprint ID") @PathVariable String id,
            @Valid @RequestBody SprintUpdateRequest request) {
        
        Sprint sprint = sprintService.updateByProjectId(id, projectId, request);
        SprintResponse response = mapToResponse(sprint);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/start")
    @Operation(summary = "Start sprint", description = "Start a sprint (change status from PLANNING to ACTIVE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint started successfully"),
        @ApiResponse(responseCode = "404", description = "Sprint not found"),
        @ApiResponse(responseCode = "400", description = "Sprint cannot be started")
    })
    public ResponseEntity<SprintResponse> startSprint(
            @Parameter(description = "Sprint ID") @PathVariable String id) {
        
        Sprint sprint = sprintService.startSprint(id);
        SprintResponse response = mapToResponse(sprint);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete sprint", description = "Complete a sprint (change status to COMPLETED)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint completed successfully"),
        @ApiResponse(responseCode = "404", description = "Sprint not found"),
        @ApiResponse(responseCode = "400", description = "Sprint cannot be completed")
    })
    public ResponseEntity<SprintResponse> completeSprint(
            @Parameter(description = "Sprint ID") @PathVariable String id) {
        
        Sprint sprint = sprintService.completeSprint(id);
        SprintResponse response = mapToResponse(sprint);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel sprint", description = "Cancel a sprint (change status to CANCELLED)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Sprint not found"),
        @ApiResponse(responseCode = "400", description = "Sprint cannot be cancelled")
    })
    public ResponseEntity<SprintResponse> cancelSprint(
            @Parameter(description = "Sprint ID") @PathVariable String id) {
        
        Sprint sprint = sprintService.cancelSprint(id);
        SprintResponse response = mapToResponse(sprint);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete sprint", description = "Delete a sprint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Sprint deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Sprint not found")
    })
    public ResponseEntity<Void> deleteSprint(
            @Parameter(description = "Sprint ID") @PathVariable String id) {
        
        sprintService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/projects/{projectId}/{id}")
    @Operation(summary = "Delete sprint by project", description = "Delete a sprint within a specific project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Sprint deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Sprint not found")
    })
    public ResponseEntity<Void> deleteSprintByProject(
            @Parameter(description = "Project ID") @PathVariable String projectId,
            @Parameter(description = "Sprint ID") @PathVariable String id) {
        
        sprintService.deleteByProjectId(id, projectId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue sprints", description = "Get all overdue sprints across all projects")
    @ApiResponse(responseCode = "200", description = "Overdue sprints retrieved successfully")
    public ResponseEntity<List<SprintResponse>> getOverdueSprints() {
        List<Sprint> sprints = sprintService.getOverdueSprints();
        List<SprintResponse> responses = sprints.stream()
            .map(this::mapToResponse)
            .toList();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/ending-soon")
    @Operation(summary = "Get sprints ending soon", description = "Get sprints ending within 3 days")
    @ApiResponse(responseCode = "200", description = "Sprints ending soon retrieved successfully")
    public ResponseEntity<List<SprintResponse>> getSprintsEndingSoon() {
        List<Sprint> sprints = sprintService.getSprintsEndingSoon();
        List<SprintResponse> responses = sprints.stream()
            .map(this::mapToResponse)
            .toList();
        return ResponseEntity.ok(responses);
    }
    
    private SprintResponse mapToResponse(Sprint sprint) {
        return new SprintResponse(
            sprint.getId(),
            sprint.getProjectId(),
            sprint.getName(),
            sprint.getDescription(),
            sprint.getStartDate(),
            sprint.getEndDate(),
            sprint.getStatus(),
            sprint.getGoal(),
            sprint.getCreatedAt(),
            sprint.getUpdatedAt(),
            sprint.getTotalTasks(),
            sprint.getCompletedTasks(),
            sprint.getProgressPercentage(),
            sprint.getRemainingDays(),
            true, // canEdit - simplified for demo
            true, // canDelete
            true, // canView
            true  // canManage
        );
    }
}
