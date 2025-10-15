package com.springboot.demo.controller;

import com.springboot.demo.dto.workspace.WorkspaceInviteRequest;
import com.springboot.demo.dto.workspace.WorkspaceMemberCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceMemberDto;
import com.springboot.demo.service.WorkspaceMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService memberService;

    @GetMapping
    @PreAuthorize("@gate.allows('workspace:read', authentication, #workspaceId)")
    public List<WorkspaceMemberDto> list(@PathVariable String workspaceId) {
        return memberService.getWorkspaceMembers(workspaceId);
    }

    @PostMapping("/invite")
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    public WorkspaceMemberDto invite(@PathVariable String workspaceId,
                                     @Valid @RequestBody WorkspaceInviteRequest req,
                                     Authentication auth) {
        return memberService.invite(workspaceId, req, currentUserId(auth));
    }

    // Add member trực tiếp theo userId (nếu FE đã có userId)
    @PostMapping
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    public WorkspaceMemberDto add(@PathVariable String workspaceId,
                                  @Valid @RequestBody WorkspaceMemberCreateRequest req) {
        return memberService.addMember(workspaceId, req);
    }

    // Cập nhật role của một member
    @PutMapping("/{userId}/role")
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    public WorkspaceMemberDto updateRole(@PathVariable String workspaceId,
                                         @PathVariable String userId,
                                         @RequestParam Integer roleId) {
        return memberService.updateMemberRole(workspaceId, userId, roleId);
    }

    // Xoá một member (không cho xoá Owner)
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@gate.allows('workspace:manage_members', authentication, #workspaceId)")
    public void remove(@PathVariable String workspaceId,
                       @PathVariable String userId) {
        memberService.removeMember(workspaceId, userId);
    }
}
