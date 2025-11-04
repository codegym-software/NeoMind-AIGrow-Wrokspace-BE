package com.springboot.demo.controller;

import com.springboot.demo.dto.workspace.WorkspaceCreateInCompanyRequest;
import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WorkspaceController {

    private final WorkspaceService service;
    private final UserIdResolver userIdResolver;

    public WorkspaceController(WorkspaceService service, UserIdResolver userIdResolver) {
        this.service = service;
        this.userIdResolver = userIdResolver;
    }

    @PostMapping("/companies/{companyId}/workspaces")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@gate.allows('workspace:create', authentication, #companyId)")
    public Workspace createInCompany(@PathVariable String companyId,
                                     @Valid @RequestBody WorkspaceCreateInCompanyRequest request,
                                     Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        WorkspaceCreateRequest fullRequest = new WorkspaceCreateRequest(
                companyId, request.name(), request.description()
        );
        return service.create(fullRequest, actorId);
    }

    @GetMapping("/companies/{companyId}/workspaces")
    @PreAuthorize("isAuthenticated()")
    public Page<Workspace> getMyWorkspacesInCompany(
            @PathVariable String companyId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String userId = userIdResolver.resolveInternalId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        return service.searchMyWorkspacesInCompany(companyId, userId, q, includeArchived, pageable);
    }
    @GetMapping("/workspaces/{workspaceId}")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #workspaceId)")
    public Workspace getById(@PathVariable String workspaceId) {
        return service.getById(workspaceId);
    }

    @PutMapping("/companies/{companyId}/workspaces/{workspaceId}")
    @PreAuthorize("@gate.allows('workspace:update', authentication, #workspaceId)")
    public Workspace updateInCompany(@PathVariable String companyId,
                                     @PathVariable String workspaceId,
                                     @Valid @RequestBody WorkspaceUpdateRequest request) {
        return service.updateByCompanyId(workspaceId, companyId, request);
    }

    @DeleteMapping("/companies/{companyId}/workspaces/{workspaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@gate.allows('workspace:delete', authentication, #workspaceId)")
    public void deleteInCompany(@PathVariable String companyId, @PathVariable String workspaceId) {
        service.deleteByCompanyId(workspaceId, companyId);
    }
}

