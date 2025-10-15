package com.springboot.demo.service.impl;

import com.springboot.demo.dto.company.CompanyCreateRequest;
import com.springboot.demo.dto.company.CompanyUpdateRequest;
import com.springboot.demo.model.Company;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.CompanyRepository;
import com.springboot.demo.repository.WorkspaceMemberRepository;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.service.CompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository repo;
    private final CompanyMemberRepository memberRepo;
    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceMemberRepository wmRepo;

    public CompanyServiceImpl(CompanyRepository repo,
                              CompanyMemberRepository memberRepo,
                              WorkspaceRepository workspaceRepo, WorkspaceMemberRepository wmRepo) {
        this.repo = repo;
        this.memberRepo = memberRepo;
        this.workspaceRepo = workspaceRepo;

        this.wmRepo = wmRepo;
    }

    @Override
    @Transactional
    public Company create(CompanyCreateRequest req, String actorId) {
        Company c = new Company();
        c.setName(req.name() == null ? null : req.name().trim());
        c.setLogoUrl(req.logoUrl());
        c = repo.save(c);

        // Đảm bảo công ty chưa có owner (phòng race condition)
        if (memberRepo.existsByCompanyIdAndIsOwnerTrue(c.getId())) {
            throw new IllegalStateException("Company already has an owner.");
        }

        // Tạo/điều chỉnh membership cho người tạo -> owner=true, role_id=null
        if (!memberRepo.existsByCompanyIdAndUserId(c.getId(), actorId)) {
            CompanyMember m = new CompanyMember();
            m.setCompanyId(c.getId());
            m.setUserId(actorId);
            m.setOwner(true);      // is_owner = true
            m.setRoleId(null);     // owner => role_id NULL
            m.setJoinedAt(LocalDateTime.now());
            memberRepo.save(m);
        } else {
            CompanyMember m = memberRepo.findByCompanyIdAndUserId(c.getId(), actorId)
                    .orElseThrow();
            m.setOwner(true);
            m.setRoleId(null);
            memberRepo.save(m);
        }

        return c;
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
        List<String> wsIds = workspaceRepo.findIdsByCompanyId(companyId);

        if (!wsIds.isEmpty()) {
            wmRepo.deleteAllByWorkspaceIds(wsIds);
            workspaceRepo.deleteByCompanyId(companyId);
        }

        repo.deleteById(companyId);
    }
}
