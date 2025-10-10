package com.springboot.demo.controller;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.dto.company.TransferOwnershipReq;
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

    @GetMapping
    @PreAuthorize("@gate.allows('company:member:read', authentication, #companyId)")
    public List<CompanyMember> list(@PathVariable String companyId) {
        return service.list(companyId);
    }

    @PreAuthorize("@gate.allows('company:member:set_user_permissions', authentication, #companyId)")
    @PostMapping
    public void add(@PathVariable String companyId,
                    @RequestBody AddCompanyMemberReq req,
                    Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.addMember(companyId, req, actorId);
    }

    @PutMapping("/{memberUserId}/role")
    @PreAuthorize("@gate.allows('company:member:set_user_permissions', authentication, #companyId)")
    public void assign(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       @RequestBody AssignCompanyRoleReq req,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.assignRole(companyId, memberUserId, req, actorId);
    }

    @PostMapping("/transfer-ownership")
    @PreAuthorize("@gate.allows('company:member:transfer_ownership', authentication, #companyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferOwnership(@PathVariable String companyId,
                                  @RequestBody TransferOwnershipReq req,
                                  Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.transferOwnership(companyId, req.toUserId(), req.downgradeRoleId(), actorId);
    }

    @DeleteMapping("/{memberUserId}")
    @PreAuthorize("@gate.allows('company:member:set_user_permissions', authentication, #companyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        service.removeMember(companyId, memberUserId, actorId);
    }
}
