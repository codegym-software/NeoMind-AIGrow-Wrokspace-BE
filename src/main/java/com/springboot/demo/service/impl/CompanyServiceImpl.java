package com.springboot.demo.service.impl;

import com.springboot.demo.dto.company.CompanyCreateRequest;
import com.springboot.demo.dto.company.CompanyUpdateRequest;
import com.springboot.demo.model.Company;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.User;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.CompanyRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.service.CompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository repo;
    private final CompanyMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;

    public CompanyServiceImpl(CompanyRepository repo,
                              CompanyMemberRepository memberRepo,
                              UserRepository userRepo,
                              WorkspaceRepository workspaceRepo) {
        this.repo = repo;
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
        this.workspaceRepo = workspaceRepo;
    }

    @Override
    @Transactional
    public Company create(CompanyCreateRequest req){
        Company c = new Company();
        c.setName(req.name() == null ? null : req.name().trim());
        c.setLogoUrl(req.logoUrl());
        c = repo.save(c);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No authenticated JWT found. Cannot assign company owner.");
        }

        Jwt jwt = jwtAuth.getToken();
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("cognito:username");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Cannot resolve current user (no email/username in JWT).");
        }

        final String finalEmail = email;

        User current = userRepo.findByEmail(finalEmail)
                .orElseThrow(() -> new IllegalStateException("Current user not found for email: " + finalEmail));

        if (memberRepo.existsByCompanyIdAndIsOwnerTrue(c.getId())) {
            throw new IllegalStateException("Company already has an owner.");
        }

        if (!memberRepo.existsByCompanyIdAndUserId(c.getId(), current.getId())) {
            CompanyMember m = new CompanyMember();
            m.setCompanyId(c.getId());
            m.setUserId(current.getId());
            m.setOwner(true);
            m.setJoinedAt(LocalDateTime.now());
            memberRepo.save(m);
        } else {
            CompanyMember m = memberRepo.findByCompanyIdAndUserId(c.getId(), current.getId())
                    .orElseThrow();
            m.setOwner(true);
            memberRepo.save(m);
        }

        return c;
    }

    @Override
    @Transactional(readOnly = true)
    public Company getById(String id){
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found: " + id));
    }

    @Override
    @Transactional
    public Company update(String id, CompanyUpdateRequest req){
        Company c = getById(id);
        c.setName(req.name() == null ? null : req.name().trim());
        c.setLogoUrl(req.logoUrl());
        return repo.save(c);
    }

    @Override
    @Transactional
    public Company save(Company c){ return repo.save(c); }

    @Override
    @Transactional(readOnly = true)
    public Page<Company> search(String q, int page, int size){
        var pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) return repo.findAll(pageable);
        return repo.findByNameContainingIgnoreCase(q.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Company> searchByCurrentUser(String q, int page, int size){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No authenticated JWT found.");
        }

        Jwt jwt = jwtAuth.getToken();
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("cognito:username");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Cannot resolve current user (no email/username in JWT).");
        }

        final String finalEmail = email;

        User currentUser = userRepo.findByEmail(finalEmail)
                .orElseThrow(() -> new IllegalStateException("Current user not found for email: " + finalEmail));

        java.util.List<String> companyIds = memberRepo.findCompanyIdsByUserId(currentUser.getId());
        if (companyIds == null || companyIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }

        var pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) {
            return repo.findByIdIn(companyIds, pageable);
        } else {
            return repo.findByIdInAndNameContainingIgnoreCase(companyIds, q.trim(), pageable);
        }
    }

    @Override
    @Transactional
    public void delete(String id){
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("Company not found: " + id);
        }
        workspaceRepo.deleteByCompanyId(id);
        memberRepo.deleteByCompanyId(id);
        repo.deleteById(id);
    }
}
