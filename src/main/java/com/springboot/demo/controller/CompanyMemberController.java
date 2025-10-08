package com.springboot.demo.controller;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.dto.company.InviteRequest;
import com.springboot.demo.dto.company.InviteResponse;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.CompanyMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company Members", description = "Quản lý thành viên công ty")
@RequiredArgsConstructor
public class CompanyMemberController {

    private final CompanyMemberService service;
    private final UserIdResolver userIdResolver;


    @GetMapping("/{companyId}/members")
    // @PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
    public List<CompanyMember> list(@PathVariable String companyId) {
        return service.list(companyId);
    }


    @PostMapping(path = "/{companyId}/invite")
    public ResponseEntity<?> invite(@PathVariable String companyId,
                                    @RequestBody InviteRequest req,
                                    @AuthenticationPrincipal Jwt jwt) {
        String inviterEmail = jwt != null ? jwt.getClaimAsString("email") : null;
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

    @PostMapping("/{companyId}/members")
    @PreAuthorize("@gate.allows('company:set_user_permissions', authentication, #companyId)")

    public void add(@PathVariable String companyId,
                    @RequestBody AddCompanyMemberReq req,
                    Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.addMember(companyId, req, actorId);
    }


    @PutMapping("/{companyId}/members/{memberUserId}/role")
    @PreAuthorize("@gate.allows('company:set_user_permissions', authentication, #companyId)")
    
    public void assign(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       @RequestBody AssignCompanyRoleReq req,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.assignRole(companyId, memberUserId, req, actorId);
    }


    @PostMapping("/{companyId}/transfer-ownership/{memberId}")
    @PreAuthorize("@gate.allows('company:transfer_ownership', authentication, #companyId)")
    public void transferOwnership(@PathVariable String companyId,
                              @PathVariable String memberId,
                              Authentication auth) {
    String actorId = userIdResolver.resolveInternalId(auth);
    // memberId chính là người bạn muốn chuyển quyền cho
    service.assignRole(companyId, memberId,
            new AssignCompanyRoleReq(null, true), actorId);
    }


    @DeleteMapping("/{companyId}/members/{memberUserId}")
    @PreAuthorize("@gate.allows('company:set_user_permissions', authentication, #companyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.removeMember(companyId, memberUserId, actorId);
    }
}

