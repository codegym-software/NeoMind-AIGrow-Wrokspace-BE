package com.springboot.demo.controller;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.dto.company.InviteRequest;
import com.springboot.demo.dto.company.InviteResponse;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.service.CompanyMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @GetMapping("/{companyId}/members")
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
    public void add(@PathVariable String companyId,
                    @RequestBody AddCompanyMemberReq req,
                    Authentication auth) {
        service.addMember(companyId, req, auth.getName());
    }

    @PutMapping("/{companyId}/members/{memberUserId}/role")
    public void assign(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       @RequestBody AssignCompanyRoleReq req,
                       Authentication auth) {
        service.assignRole(companyId, memberUserId, req, auth.getName());
    }

    @PostMapping("/{companyId}/members/transfer-ownership")
    public void transferOwnership(@PathVariable String companyId,
                                  @RequestBody Map<String, String> body,
                                  Authentication auth) {
        service.assignRole(companyId, body.get("toUserId"),
                new AssignCompanyRoleReq(null, true), auth.getName());
    }
}
