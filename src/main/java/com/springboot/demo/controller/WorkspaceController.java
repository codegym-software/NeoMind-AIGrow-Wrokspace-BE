package com.springboot.demo.controller;

import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.service.WorkspaceService;
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

@RestController
@RequestMapping("/api/workspaces")
@Tag(name = "Workspace Management", description = "APIs for managing workspaces within companies")
@SecurityRequirement(name = "Bearer Authentication")
public class WorkspaceController {

    private final WorkspaceService service;

    public WorkspaceController(WorkspaceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new workspace", description = "Create a new workspace within a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Workspace created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Workspace create(@Valid @RequestBody WorkspaceCreateRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace by ID", description = "Retrieve a workspace by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workspace found"),
            @ApiResponse(responseCode = "404", description = "Workspace not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Workspace getById(@Parameter(description = "Workspace ID") @PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Workspace update(@PathVariable String id,
                            @Valid @RequestBody WorkspaceUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping
    public Page<Workspace> list(@RequestParam(required = false) String companyId,
                                @RequestParam(required = false) String q,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        if (companyId != null) {
            return service.searchByCompanyId(companyId, q, page, size);
        }
        throw new IllegalArgumentException("companyId parameter is required");
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get workspaces by company", description = "Retrieve all workspaces belonging to a specific company with optional search and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workspaces retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Page<Workspace> getByCompanyId(
            @Parameter(description = "Company ID") @PathVariable String companyId,
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return service.searchByCompanyId(companyId, q, page, size);
    }

    @GetMapping("/company/{companyId}/all")
    public List<Workspace> getAllByCompanyId(@PathVariable String companyId) {
        return service.getAllByCompanyId(companyId);
    }

    @GetMapping("/company/{companyId}/count")
    public ResponseEntity<Long> countByCompanyId(@PathVariable String companyId) {
        Long count = service.countByCompanyId(companyId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/restore")
    public Workspace restore(@PathVariable String id) {
        return service.restore(id);
    }

    // Alternative endpoint structure: /api/companies/{companyId}/workspaces
    @PostMapping("/companies/{companyId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Workspace createInCompany(@PathVariable String companyId,
                                     @Valid @RequestBody WorkspaceCreateRequest request) {
        // Override companyId from path variable
        WorkspaceCreateRequest requestWithCompanyId = new WorkspaceCreateRequest(
                companyId, request.name(), request.description()
        );
        return service.create(requestWithCompanyId);
    }

    @GetMapping("/companies/{companyId}")
    public Page<Workspace> getByCompanyIdAlt(@PathVariable String companyId,
                                             @RequestParam(required = false) String q,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return service.searchByCompanyId(companyId, q, page, size);
    }

    @PutMapping("/companies/{companyId}/{id}")
    public Workspace updateInCompany(@PathVariable String companyId,
                                     @PathVariable String id,
                                     @Valid @RequestBody WorkspaceUpdateRequest request) {
        return service.updateByCompanyId(id, companyId, request);
    }

    @DeleteMapping("/companies/{companyId}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInCompany(@PathVariable String companyId, @PathVariable String id) {
        service.deleteByCompanyId(id, companyId);
    }
}

