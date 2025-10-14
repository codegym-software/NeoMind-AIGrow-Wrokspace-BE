package com.springboot.demo.controller;

import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    @Operation(summary = "List workspaces in company")
    @PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
    public List<Workspace> list(@PathVariable String companyId,
                                @RequestParam(required = false, name = "q") String keyword) {
        return workspaceService.searchByCompanyId(companyId, keyword);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create workspace in company")
    @PreAuthorize("@gate.allows('company:update', authentication, #companyId)")
    public Workspace create(@PathVariable String companyId,
                            @Valid @RequestBody WorkspaceCreateRequest request) {
        WorkspaceCreateRequest requestWithCompanyId = new WorkspaceCreateRequest(
                companyId,
                request.name(),
                request.description()
        );
        return workspaceService.create(requestWithCompanyId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace by id in company")
    @PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
    public Workspace get(@PathVariable String companyId, @PathVariable String id) {
        // No special company filtering on read path in service; caller authorization guards the access.
        return workspaceService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workspace in company")
    @PreAuthorize("@gate.allows('company:update', authentication, #companyId)")
    public Workspace update(@PathVariable String companyId,
                            @PathVariable String id,
                            @Valid @RequestBody WorkspaceUpdateRequest request) {
        return workspaceService.updateByCompanyId(id, companyId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete workspace in company")
    @PreAuthorize("@gate.allows('company:update', authentication, #companyId)")
    public void delete(@PathVariable String companyId, @PathVariable String id) {
        workspaceService.deleteByCompanyId(id, companyId);
    }
}

