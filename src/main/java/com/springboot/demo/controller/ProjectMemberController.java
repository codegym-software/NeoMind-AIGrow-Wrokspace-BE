package com.springboot.demo.controller;

import com.springboot.demo.dto.project.AddProjectMemberReq;
import com.springboot.demo.dto.project.AssignProjectRoleReq;
import com.springboot.demo.dto.project.TransferProjectOwnershipReq;
import com.springboot.demo.dto.project.ProjectMemberDto;
import com.springboot.demo.model.ProjectMember;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService service;
    private final UserIdResolver userIdResolver;

    @GetMapping("/members")
    @PreAuthorize("@gate.allows('project:member:read', authentication, #projectId)")
    public List<ProjectMemberDto> listMembers(@PathVariable String projectId) {
        return service.listMembersWithDetails(projectId);
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@gate.allows('project:member:invite', authentication, #projectId)")
    public void addMember(@PathVariable String projectId,
                           @RequestBody AddProjectMemberReq req,
                           Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.addMember(projectId, req, actorId);
    }

    @PutMapping("/members/{targetUserId}/role")
    @PreAuthorize("@gate.allows('project:member:set_user_permissions', authentication, #projectId)")
    public void assignRole(@PathVariable String projectId,
                           @PathVariable String targetUserId,
                           @RequestBody AssignProjectRoleReq req,
                           Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.assignRole(projectId, targetUserId, req, actorId);
    }

    @DeleteMapping("/members/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@gate.allows('project:member:delete', authentication, #projectId)")
    public void removeMember(@PathVariable String projectId,
                             @PathVariable String targetUserId,
                             Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.removeMember(projectId, targetUserId, actorId);
    }

    @PostMapping("/members/transfer-ownership")
    @PreAuthorize("@gate.allows('project:member:transfer_ownership', authentication, #projectId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferOwnership(@PathVariable String projectId,
                                  @RequestBody TransferProjectOwnershipReq req,
                                  Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.transferOwnership(projectId, req, actorId);
    }
}