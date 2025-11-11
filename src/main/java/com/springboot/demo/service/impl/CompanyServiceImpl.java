package com.springboot.demo.service.impl;

import com.springboot.demo.dto.company.CompanyCreateRequest;
import com.springboot.demo.dto.company.CompanyUpdateRequest;
import com.springboot.demo.model.Company;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.Permission;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.*;
import com.springboot.demo.service.CompanyService;
import com.springboot.demo.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository repo;
    private final CompanyMemberRepository memberRepo;
    private final WorkspaceRepository workspaceRepo;
    private final RoleRepository roleRepo;
    private final PermissionRepository permissionRepo;
    private final RolePermissionService rolePermissionService;

    @Override
    @Transactional
    public Company create(CompanyCreateRequest req, String actorId) {
        Company company = new Company();
        company.setName(req.name() == null ? null : req.name().trim());
        company.setLogoUrl(req.logoUrl());
        company = repo.save(company);

        if (memberRepo.existsByCompanyIdAndIsOwnerTrue(company.getId())) {
            throw new IllegalStateException("Company already has an owner.");
        }

        String companyId = company.getId();
        CompanyMember member = memberRepo.findByCompanyIdAndUserId(companyId, actorId)
                .orElseGet(() -> {
                    CompanyMember m = new CompanyMember();
                    m.setCompanyId(companyId);
                    m.setUserId(actorId);
                    m.setJoinedAt(LocalDateTime.now());
                    return m;
                });
        member.setOwner(true);
        member.setRoleId(null);
        memberRepo.save(member);

        createDefaultCompanyRoles(companyId);

        return company;
    }

    private void createDefaultCompanyRoles(String companyId) {
        if (roleRepo.existsByScopeAndTargetIdAndCode(Scope.company, companyId, "admin")) {
            return;
        }

        Role adminRole = Role.builder()
                .scope(Scope.company)
                .targetId(companyId)
                .code("admin")
                .name("Admin")
                .description("Administrator role with full company access")
                .build();
        roleRepo.save(adminRole);

        Role memberRole = Role.builder()
                .scope(Scope.company)
                .targetId(companyId)
                .code("member")
                .name("Member")
                .description("Standard company member role with limited access")
                .build();
        roleRepo.save(memberRole);

        var allCompanyPermissions = permissionRepo.findByScope(Scope.company);
        List<Integer> allPermissionIds = allCompanyPermissions.stream()
                .map(p -> p.getId())
                .collect(Collectors.toList());

        rolePermissionService.replaceRolePermissions(adminRole.getId(), allPermissionIds);

        List<Integer> memberPermissionIds = allCompanyPermissions.stream()
                .filter(p -> {
                    String code = p.getCode().toLowerCase();
                    return !code.contains("update")
                            && !code.contains("delete")
                            && !code.contains("invite");
                })
                .map(Permission::getId)
                .collect(Collectors.toList());

        rolePermissionService.replaceRolePermissions(memberRole.getId(), memberPermissionIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Company getById(String companyId) {
        return repo.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found: " + companyId));
    }

    @Override
    @Transactional
    public Company update(String companyId, CompanyUpdateRequest req) {
        Company c = getById(companyId);
        c.setName(req.name() == null ? null : req.name().trim());
        c.setLogoUrl(req.logoUrl());
        return repo.save(c);
    }

    @Override
    @Transactional
    public Company save(Company c) {
        return repo.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Company> search(String q, int page, int size) {
        var pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) return repo.findAll(pageable);
        return repo.findByNameContainingIgnoreCase(q.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Company> searchByCurrentUser(String q, int page, int size, String actorId) {
        var companyIds = memberRepo.findCompanyIdsByUserId(actorId);
        var pageable = PageRequest.of(page, size);

        if (companyIds == null || companyIds.isEmpty()) {
            return Page.empty(pageable);
        }

        if (q == null || q.isBlank()) {
            return repo.findByIdIn(companyIds, pageable);
        } else {
            return repo.findByIdInAndNameContainingIgnoreCase(companyIds, q.trim(), pageable);
        }
    }

    @Override
    @Transactional
    public void delete(String companyId) {
        if (!repo.existsById(companyId)) {
            throw new NoSuchElementException("Company not found: " + companyId);
        }
        workspaceRepo.deleteByCompanyId(companyId);
        memberRepo.deleteByCompanyId(companyId);
        repo.deleteById(companyId);
    }
}
