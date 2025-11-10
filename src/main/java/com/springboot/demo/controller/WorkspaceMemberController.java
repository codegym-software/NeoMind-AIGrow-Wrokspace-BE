package com.springboot.demo.controller;

import com.springboot.demo.dto.workspace.AddWorkspaceMemberReq;
import com.springboot.demo.dto.workspace.AssignWorkspaceRoleReq;
import com.springboot.demo.dto.workspace.TransferWorkspaceOwnershipReq;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService service;
    private final UserIdResolver userIdResolver;

    @GetMapping("/members")
    @PreAuthorize("@gate.allows('workspace:member:read', authentication, #workspaceId)")
    public List<WorkspaceMember> listMembers(@PathVariable String workspaceId) {
        return service.listMembers(workspaceId);
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@gate.allows('workspace:member:invite', authentication, #workspaceId)")
    public void addMember(@PathVariable String workspaceId,
                           @RequestBody AddWorkspaceMemberReq req,
                           Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.addMember(workspaceId, req, actorId);
    }

    @PutMapping("/members/{targetUserId}/role")
    @PreAuthorize("@gate.allows('workspace:member:set_user_permissions', authentication, #workspaceId)")
    public void assignRole(@PathVariable String workspaceId,
                           @PathVariable String targetUserId,
                           @RequestBody AssignWorkspaceRoleReq req,
                           Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.assignRole(workspaceId, targetUserId, req, actorId);
    }

    @DeleteMapping("/members/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@gate.allows('workspace:member:delete', authentication, #workspaceId)")
    public void removeMember(@PathVariable String workspaceId,
                             @PathVariable String targetUserId,
                             Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.removeMember(workspaceId, targetUserId, actorId);
    }

    @PostMapping("/members/transfer-ownership")
    @PreAuthorize("@gate.allows('workspace:member:transfer_ownership', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferOwnership(@PathVariable String workspaceId,
                                  @RequestBody TransferWorkspaceOwnershipReq req,
                                  Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.transferOwnership(workspaceId, req, actorId);
    }
}


