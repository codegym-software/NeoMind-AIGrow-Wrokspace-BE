package com.springboot.demo.controller;

import com.springboot.demo.dto.company.*;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.security.CognitoUserInfoService;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.CompanyMemberService;
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
@RequestMapping("/api/companies/{companyId}")
@Tag(name = "Company Members", description = "Quản lý thành viên công ty")
@RequiredArgsConstructor
public class CompanyMemberController {

    private final CompanyMemberService service;
    private final UserIdResolver userIdResolver;
    private final CognitoUserInfoService cognitoUserInfoService;

    @GetMapping("/members")
    @PreAuthorize("@gate.allows('company:member:read', authentication, #companyId)")
    public List<CompanyMember> list(@PathVariable String companyId) {
        return service.list(companyId);
    }

    @PreAuthorize("@gate.allows('company:member:set_user_permissions', authentication, #companyId)")
    @PostMapping("/members")
    public void add(@PathVariable String companyId,
                    @RequestBody AddCompanyMemberReq req,
                    Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.addMember(companyId, req, actorId);
    }

    @PutMapping("/members/{memberUserId}/role")
    @PreAuthorize("@gate.allows('company:member:set_user_permissions', authentication, #companyId)")
    public void assign(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       @RequestBody AssignCompanyRoleReq req,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.assignRole(companyId, memberUserId, req, actorId);
    }

    @PostMapping("/members/transfer-ownership")
    @PreAuthorize("@gate.allows('company:member:transfer_ownership', authentication, #companyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferOwnership(@PathVariable String companyId,
                                  @RequestBody TransferOwnershipReq req,
                                  Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.transferOwnership(companyId, req.toUserId(), req.downgradeRoleId(), actorId);
    }

    @DeleteMapping("/members/{memberUserId}")
    @PreAuthorize("@gate.allows('company:member:delete', authentication, #companyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.removeMember(companyId, memberUserId, actorId);
    }

    @PostMapping("/invite")
    @PreAuthorize("@gate.allows('company:member:invite', authentication, #companyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> invite(@PathVariable String companyId,
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
            InviteResponse resp = service.inviteMemberByEmail(companyId, inviterEmail, req.getEmail(), req.getRoleId());
            return ResponseEntity.ok(resp.getMember());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
