package com.springboot.demo.controller;

import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Company;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.CompanyService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@Tag(name = "Workspace Management", description = "APIs for managing workspaces within companies")
@SecurityRequirement(name = "Bearer Authentication")
public class WorkspaceController {
    private final WorkspaceService service;
    private final UserIdResolver userIdResolver;

    public WorkspaceController(WorkspaceService service, UserIdResolver userIdResolver) {
        this.service = service;
        this.userIdResolver = userIdResolver;
    }
    
    @PostMapping("/companies/{companyId}/workspaces")
    @PreAuthorize("@gate.allows('company:create-workspace', authentication, #companyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Workspace create(@PathVariable String companyId, @Valid @RequestBody WorkspaceCreateRequest req, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return service.create(req, actorId);
    }

    @GetMapping("/workspaces/{workspaceId}")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Workspace get(@PathVariable String workspaceId) {
        return service.getById(workspaceId);
    }

    @PutMapping("/workspaces/{workspaceId}")
    @PreAuthorize("@gate.allows('workspace:update', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Workspace update(@PathVariable String workspaceId, @Valid @RequestBody WorkspaceUpdateRequest req) {
        return service.update(workspaceId, req);
    }

    @PatchMapping("/workspaces/{workspaceId}")
    @PreAuthorize("@gate.allows('workspace:update', authentication, #workspaceId)")
    public Workspace patch(@PathVariable String workspaceId, @RequestBody Map<String, Object> p) {
        Workspace c = service.getById(workspaceId);
        if (p.containsKey("name")) {
            c.setName(Objects.toString(p.get("name"), "").trim());
        }
        if (p.containsKey("description")) {
            c.setDescription((String) p.get("description"));
        }
        return service.save(c);
    }

    @GetMapping("/workspaces")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Page<Workspace> list(@Parameter(description = "Search query") @RequestParam(required = false) String q,
                              @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                              @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
                              Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return service.searchByCurrentUser(q, page, size, actorId);
    }

    @DeleteMapping("/workspaces/{workspaceId}")
    @PreAuthorize("@gate.allows('company:delete', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workspaceId) {
        service.delete(workspaceId);
    }

    @GetMapping("/workspaces/all")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #workspaceId)")
    public Page<Workspace> listAll(@Parameter(description = "Search query") @RequestParam(required = false) String q,
                                   @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                                   @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size, @PathVariable String workspaceId) {
        return service.search(q, page, size);
    }
    
}

