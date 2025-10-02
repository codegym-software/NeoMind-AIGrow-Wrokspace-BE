package com.springboot.demo.controller;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.CompanyMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/members")
@Tag(name = "Company Members", description = "Quản lý thành viên công ty")
@RequiredArgsConstructor
public class CompanyMemberController {

    private final CompanyMemberService service;
    private final UserIdResolver userIdResolver;

    @PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
    @GetMapping
    public List<CompanyMember> list(@PathVariable String companyId) {
        return service.list(companyId);
    }

    @PreAuthorize("@gate.allows('company:set_user_permissions', authentication, #companyId)")
    @PostMapping
    public void add(@PathVariable String companyId,
                    @RequestBody AddCompanyMemberReq req,
                    Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.addMember(companyId, req, actorId);
    }

    @PreAuthorize("@gate.allows('company:set_user_permissions', authentication, #companyId)")
    @PutMapping("/{memberUserId}/role")
    public void assign(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       @RequestBody AssignCompanyRoleReq req,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.assignRole(companyId, memberUserId, req, actorId);
    }

    @PreAuthorize("@gate.allows('company:transfer_ownership', authentication, #companyId)")
    @PostMapping("/transfer-ownership")
    public void transferOwnership(@PathVariable String companyId,
                                  @RequestBody Map<String, String> body,
                                  Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.assignRole(companyId, body.get("toUserId"),
                new AssignCompanyRoleReq(null, true), actorId);
    }

    @DeleteMapping("/{memberUserId}")
    @PreAuthorize("@gate.allows('company:set_user_permissions', authentication, #companyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.removeMember(companyId, memberUserId, actorId);
    }
}
