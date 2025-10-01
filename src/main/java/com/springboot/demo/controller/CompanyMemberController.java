package com.springboot.demo.controller;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.service.CompanyMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public List<CompanyMember> list(@PathVariable String companyId) {
        return service.list(companyId);
    }

    @PostMapping
    public void add(@PathVariable String companyId,
                    @RequestBody AddCompanyMemberReq req,
                    Authentication auth) {
        service.addMember(companyId, req, auth.getName());
    }

    @PutMapping("/{memberUserId}/role")
    public void assign(@PathVariable String companyId,
                       @PathVariable String memberUserId,
                       @RequestBody AssignCompanyRoleReq req,
                       Authentication auth) {
        service.assignRole(companyId, memberUserId, req, auth.getName());
    }

    @PostMapping("/transfer-ownership")
    public void transferOwnership(@PathVariable String companyId,
                                  @RequestBody Map<String, String> body,
                                  Authentication auth) {
        service.assignRole(companyId, body.get("toUserId"),
                new AssignCompanyRoleReq(null, true), auth.getName());
    }
}
