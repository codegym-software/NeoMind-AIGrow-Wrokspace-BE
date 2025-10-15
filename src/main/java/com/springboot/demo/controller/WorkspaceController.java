package com.springboot.demo.controller;

import com.springboot.demo.dto.company.CompanyCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceDto;
import com.springboot.demo.dto.workspace.WorkspaceMemberCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceMemberDto;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Company;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.WorkspaceMemberService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@Tag(name = "Workspace Management", description = "APIs for managing workspaces within companies")
@SecurityRequirement(name = "Bearer Authentication")
public class WorkspaceController {
    private final WorkspaceService service;
    private final WorkspaceMemberService memberService;
    private final UserIdResolver userIdResolver;

    public WorkspaceController(WorkspaceService service, WorkspaceMemberService memberService, UserIdResolver userIdResolver) {
        this.service = service;
        this.memberService = memberService;
        this.userIdResolver = userIdResolver;
    }



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@gate.allows('workspace:create', authentication, #request.companyId)")
    @Operation(summary = "Create a new workspace", description = "Create a new workspace within a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Workspace created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })

    public Workspace create(@Valid @RequestBody WorkspaceCreateRequest request, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return service.create(request, actorId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #id)")
    @Operation(summary = "Get workspace by ID", description = "Retrieve a workspace by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workspace found"),
            @ApiResponse(responseCode = "404", description = "Workspace not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public Workspace getById(@Parameter(description = "Workspace ID") @PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@gate.allows('workspace:update', authentication, #id)")
    public Workspace update(@PathVariable String id,
                            @Valid @RequestBody WorkspaceUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@gate.allows('workspace:delete', authentication, #id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping
    public List<Workspace> list(@RequestParam(required = false) String companyId,
                                @RequestParam(required = false) String q,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        if (companyId != null) {
            return service.searchByCompanyId(companyId, q);
        }
        throw new IllegalArgumentException("companyId parameter is required");
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
    @Operation(summary = "Get workspaces by company", description = "Retrieve all workspaces belonging to a specific company with optional search and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workspaces retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<Workspace> getByCompanyId(
            @Parameter(description = "Company ID") @PathVariable String companyId,
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return service.searchByCompanyId(companyId, q);
    }

    @GetMapping("/company/{companyId}/all")
    @PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
    public List<Workspace> getAllByCompanyId(@PathVariable String companyId, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return service.getAllByCompanyId(companyId);
    }

    @GetMapping("/company/{companyId}/count")
    @PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
    public ResponseEntity<Long> countByCompanyId(@PathVariable String companyId, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        long count = service.countByCompanyId(companyId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("@gate.allows('workspace:update', authentication, #id)")
    public Workspace restore(@PathVariable String id, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return service.restore(id);
    }

    // Alternative endpoint structure: /api/companies/{companyId}/workspaces
    @PostMapping("/companies/{companyId}")
    @PreAuthorize("@gate.allows('workspace:create', authentication, #companyId)")
    @ResponseStatus(HttpStatus.CREATED)
    public Workspace createInCompany(@PathVariable String companyId,
                                     @Valid @RequestBody WorkspaceCreateRequest request,
                                     Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        // Override companyId from path variable
        WorkspaceCreateRequest requestWithCompanyId = new WorkspaceCreateRequest(
                companyId, request.name(), request.description()
        );
        return service.create(requestWithCompanyId, actorId);
    }

    @GetMapping("/companies/{companyId}")
    @PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
    public List<Workspace> getByCompanyIdAlt(@PathVariable String companyId,
                                             @RequestParam(required = false) String q,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return service.searchByCompanyId(companyId, q);
    }

    @PutMapping("/companies/{companyId}/{id}")
    @PreAuthorize("@gate.allows('workspace:update', authentication, #id)")
    public Workspace updateInCompany(@PathVariable String companyId,
                                     @PathVariable String id,
                                     @Valid @RequestBody WorkspaceUpdateRequest request,
                                     Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return service.updateByCompanyId(id, companyId, request);
    }

    @DeleteMapping("/companies/{companyId}/{id}")
    @PreAuthorize("@gate.allows('workspace:delete', authentication, #id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInCompany(@PathVariable String companyId, @PathVariable String id, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.deleteByCompanyId(id, companyId);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my workspaces", description = "Get all workspaces where current user is a member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workspaces retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Page<Workspace> listMyWorkspaces(@Parameter(description = "Search query") @RequestParam(required = false) String q,
                                           @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                                           @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
                                           Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return service.searchByCurrentUser(q, page, size, actorId);
    }

    // ==================== WORKSPACE MEMBER ENDPOINTS ====================

    @PostMapping("/{workspaceId}/members")
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add member to workspace", description = "Add a new member to the workspace")
    public WorkspaceMemberDto addMember(@PathVariable String workspaceId,
                                        @Valid @RequestBody WorkspaceMemberCreateRequest request,
                                        Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return memberService.addMember(workspaceId, request);
    }

    @GetMapping("/{workspaceId}/members")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #workspaceId)")
    @Operation(summary = "Get workspace members", description = "Get all members of the workspace")
    public List<WorkspaceMemberDto> getMembers(@PathVariable String workspaceId, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return memberService.getWorkspaceMembers(workspaceId);
    }

    @GetMapping("/{workspaceId}/members/{userId}")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #workspaceId)")
    @Operation(summary = "Get workspace member", description = "Get specific member of the workspace")
    public WorkspaceMemberDto getMember(@PathVariable String workspaceId, @PathVariable String userId, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return memberService.getMember(workspaceId, userId);
    }

    @PutMapping("/{workspaceId}/members/{userId}/role")
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    @Operation(summary = "Update member role", description = "Update the role of a workspace member")
    public WorkspaceMemberDto updateMemberRole(@PathVariable String workspaceId,
                                               @PathVariable String userId,
                                               @RequestParam Integer roleId,
                                               Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return memberService.updateMemberRole(workspaceId, userId, roleId);
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove member from workspace", description = "Remove a member from the workspace")
    public void removeMember(@PathVariable String workspaceId, @PathVariable String userId, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        memberService.removeMember(workspaceId, userId);
    }

    @GetMapping("/{workspaceId}/members/{userId}/check")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #workspaceId)")
    @Operation(summary = "Check membership", description = "Check if user is a member of the workspace")
    public ResponseEntity<Boolean> isMember(@PathVariable String workspaceId, @PathVariable String userId, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        boolean isMember = memberService.isMember(workspaceId, userId);
        return ResponseEntity.ok(isMember);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user workspaces", description = "Get all workspaces where user is a member")
    public List<String> getUserWorkspaces(@PathVariable String userId, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        // Only allow users to see their own workspaces or if they have admin access
        if (!actorId.equals(userId)) {
            throw new IllegalArgumentException("You can only view your own workspaces");
        }
        return memberService.getUserWorkspaces(userId);
    }

    @GetMapping("/{workspaceId}/members/{userId}/owner")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #workspaceId)")
    @Operation(summary = "Check if user is owner", description = "Check if user is the owner of the workspace")
    public ResponseEntity<Boolean> isOwner(@PathVariable String workspaceId, @PathVariable String userId, Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        boolean isOwner = memberService.isOwner(workspaceId, userId);
        return ResponseEntity.ok(isOwner);
    }
}

