package com.springboot.demo.controller;

import com.springboot.demo.dto.workspace.WorkspaceMemberDto;
import com.springboot.demo.dto.workspace.WorkspaceMemberInviteRequest;
import com.springboot.demo.dto.workspace.WorkspaceTransferOwnershipRequest;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.WorkspaceMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;
    private final UserIdResolver userIdResolver;

    // Mời thành viên vào workspace (chỉ những người đã có trong công ty)
    @PostMapping("/workspaces/{workspaceId}/members")
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    public WorkspaceMemberDto inviteMember(@PathVariable String workspaceId,
                                          @Valid @RequestBody WorkspaceMemberInviteRequest req,
                                          Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        WorkspaceMember member = workspaceMemberService.inviteMember(workspaceId, req, actorId);
        return WorkspaceMemberDto.fromEntity(member);
    }

    // Lấy danh sách thành viên của workspace
    @GetMapping("/workspaces/{workspaceId}/members")
    @PreAuthorize("@gate.allows('workspace:member:read', authentication, #workspaceId)")
    public List<WorkspaceMemberDto> getMembers(@PathVariable String workspaceId) {
        return workspaceMemberService.getMembers(workspaceId);
    }

    // Xóa thành viên khỏi workspace
    @DeleteMapping("/workspaces/{workspaceId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    public void removeMember(@PathVariable String workspaceId, @PathVariable String userId) {
        workspaceMemberService.removeMember(workspaceId, userId);
    }

    // Cập nhật role của thành viên trong workspace
    @PutMapping("/workspaces/{workspaceId}/members/{userId}/role")
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    public WorkspaceMemberDto updateMemberRole(@PathVariable String workspaceId,
                                               @PathVariable String userId,
                                               @RequestBody Integer roleId) {
        WorkspaceMember member = workspaceMemberService.updateMemberRole(workspaceId, userId, roleId);
        return WorkspaceMemberDto.fromEntity(member);
    }

    // Lấy danh sách thành viên công ty có thể được mời vào workspace
    @GetMapping("/workspaces/{workspaceId}/members/available")
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    public List<WorkspaceMemberDto> getAvailableMembers(@PathVariable String workspaceId) {
        return workspaceMemberService.getAvailableMembers(workspaceId);
    }

    // Mời thành viên vào workspace bằng email
    @PostMapping("/workspaces/{workspaceId}/members/invite-by-email")
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    public WorkspaceMemberDto inviteMemberByEmail(@PathVariable String workspaceId,
                                                  @RequestParam String inviterEmail,
                                                  @RequestParam String invitedEmail,
                                                  @RequestParam Integer roleId) {
        WorkspaceMember member = workspaceMemberService.inviteMemberByEmail(workspaceId, inviterEmail, invitedEmail, roleId);
        return WorkspaceMemberDto.fromEntity(member);
    }

    // Cập nhật role của thành viên với authorization check
    @PutMapping("/workspaces/{workspaceId}/members/{userId}/role")
    @PreAuthorize("@gate.allows('workspace:set_user_permissions', authentication, #workspaceId)")
    public WorkspaceMemberDto updateMemberRoleWithAuth(@PathVariable String workspaceId,
                                                       @PathVariable String userId,
                                                       @RequestBody Integer roleId,
                                                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        WorkspaceMember member = workspaceMemberService.updateMemberRole(workspaceId, userId, roleId, actorId);
        return WorkspaceMemberDto.fromEntity(member);
    }

    // Transfer ownership của workspace
    @PostMapping("/workspaces/{workspaceId}/transfer-ownership")
    @PreAuthorize("@gate.allows('workspace:transfer_ownership', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferOwnership(@PathVariable String workspaceId,
                                 @Valid @RequestBody WorkspaceTransferOwnershipRequest req,
                                 Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        workspaceMemberService.transferOwnership(workspaceId, req.toUserId(), req.downgradeRoleId(), actorId);
    }

    // Lấy danh sách workspace roles có sẵn
    @GetMapping("/workspaces/roles")
    public List<Role> getAvailableWorkspaceRoles() {
        return workspaceMemberService.getAvailableWorkspaceRoles();
    }
}