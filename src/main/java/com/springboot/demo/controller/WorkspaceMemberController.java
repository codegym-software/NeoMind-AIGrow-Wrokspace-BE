package com.springboot.demo.controller;

import com.springboot.demo.dto.company.*;
import com.springboot.demo.dto.workspace.AddWorkspaceMemberReq;
import com.springboot.demo.dto.workspace.AssignWorkspaceRoleReq;
import com.springboot.demo.dto.workspace.WorkspaceInviteResponse;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.security.CognitoUserInfoService;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.WorkspaceMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
@Tag(name = "Workspace Members", description = "Quản lý thành viên workspace")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService service;
    private final UserIdResolver userIdResolver;
    private final CognitoUserInfoService cognitoUserInfoService;

    @GetMapping("/members")
    @PreAuthorize("@gate.allows('workspace:member:read', authentication, #workspaceId)")
    public List<WorkspaceMember> list(@PathVariable String workspaceId) {
        return service.list(workspaceId);
    }

    @PostMapping("/members")
    @PreAuthorize("@gate.allows('workspace:member:set_user_permissions', authentication, #workspaceId)")
    public void add(@PathVariable String workspaceId,
                    @RequestBody AddWorkspaceMemberReq req,
                    Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.addMember(workspaceId, req, actorId);
    }

    @PutMapping("/members/{memberUserId}/role")
    @PreAuthorize("@gate.allows('workspace:member:set_user_permissions', authentication, #workspaceId)")
    public void assign(@PathVariable String workspaceId,
                       @PathVariable String memberUserId,
                       @RequestBody AssignWorkspaceRoleReq req,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.assignRole(workspaceId, memberUserId, req, actorId);
    }

    @PostMapping("/members/transfer-ownership")
    @PreAuthorize("@gate.allows('workspace:member:transfer_ownership', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferOwnership(@PathVariable String workspaceId,
                                  @RequestBody TransferOwnershipReq req,
                                  Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.transferOwnership(workspaceId, req.toUserId(), req.downgradeRoleId(), actorId);
    }

    @DeleteMapping("/members/{memberUserId}")
    @PreAuthorize("@gate.allows('workspace:member:set_user_permissions', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String workspaceId,
                       @PathVariable String memberUserId,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.removeMember(workspaceId, memberUserId, actorId);
    }

    @PostMapping("/invite")
    @PreAuthorize("@gate.allows('workspace:member:set_user_permissions', authentication, #workspaceId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> invite(@PathVariable String workspaceId,
                                    @RequestBody InviteRequest req,
                                    @AuthenticationPrincipal Jwt jwt) {
        String inviterEmail = jwt != null ? jwt.getClaimAsString("email") : null;
        if (inviterEmail == null || inviterEmail.isBlank()) {
            try {
                assert jwt != null;
                Map<String, Object> info = cognitoUserInfoService.fetch(jwt.getTokenValue());
                Object e = info.get("email");
                if (e instanceof String s && !s.isBlank()) inviterEmail = s;
            } catch (Exception ignored) {}
        }
        if (inviterEmail == null || inviterEmail.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing email claim in token"));
        }
        try {

            WorkspaceInviteResponse resp = service.inviteMemberByEmail(workspaceId, inviterEmail, req.getEmail(), req.getRoleId());
            return ResponseEntity.ok(resp.getMember());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
