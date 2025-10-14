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
import com.springboot.demo.security.CognitoUserInfoService;
import com.springboot.demo.service.CompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository repo;
    private final CompanyMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;
    private final CognitoUserInfoService cognitoUserInfoService;

    public CompanyServiceImpl(CompanyRepository repo,
                              CompanyMemberRepository memberRepo,
                              UserRepository userRepo,
                              WorkspaceRepository workspaceRepo, CognitoUserInfoService cognitoUserInfoService) {
        this.repo = repo;
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
        this.workspaceRepo = workspaceRepo;
        this.cognitoUserInfoService = cognitoUserInfoService;
    }

    @Override
    @Transactional
    public Company create(CompanyCreateRequest req) {
        Company c = new Company();
        c.setName(req.name() == null ? null : req.name().trim());
        c.setLogoUrl(req.logoUrl());
        c = repo.save(c);

        User current = resolveCurrentUser();

        // Đảm bảo công ty chưa có owner (phòng race condition)
        if (memberRepo.existsByCompanyIdAndIsOwnerTrue(c.getId())) {
            throw new IllegalStateException("Company already has an owner.");
        }

        // Tạo/điều chỉnh membership cho người tạo -> owner=true, role_id=null
        if (!memberRepo.existsByCompanyIdAndUserId(c.getId(), current.getId())) {
            CompanyMember m = new CompanyMember();
            m.setCompanyId(c.getId());
            m.setUserId(current.getId());
            m.setOwner(true);      // is_owner = true
            m.setRoleId(null);     // owner => role_id NULL
            m.setJoinedAt(LocalDateTime.now());
            memberRepo.save(m);
        } else {
            CompanyMember m = memberRepo.findByCompanyIdAndUserId(c.getId(), current.getId())
                    .orElseThrow();
            m.setOwner(true);
            m.setRoleId(null);
            memberRepo.save(m);
        }

        return c;
    }

    @Override
    @Transactional(readOnly = true)
    public Company getById(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found: " + id));
    }

    @Override
    @Transactional
    public Company update(String id, CompanyUpdateRequest req) {
        Company c = getById(id);
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
    public Page<Company> searchByCurrentUser(String q, int page, int size) {
        User currentUser = resolveCurrentUser();

        var companyIds = memberRepo.findCompanyIdsByUserId(currentUser.getId());
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
    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("Company not found: " + id);
        }
        // Xoá dữ liệu liên quan trước rồi xoá công ty
        workspaceRepo.deleteByCompanyId(id);
        memberRepo.deleteByCompanyId(id);
        repo.deleteById(id);
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AccessDeniedException("Unauthenticated");
        }

        Jwt jwt = (Jwt) jwtAuth.getToken();
        String emailResolved = jwt.getClaimAsString("email");
        if (emailResolved == null || emailResolved.isBlank()) {
            try {
                Map<String, Object> info = cognitoUserInfoService.fetch(jwt.getTokenValue());
                Object e = info.get("email");
                if (e instanceof String s && !s.isBlank()) emailResolved = s;
            } catch (Exception ignored) {}
        }

        if (emailResolved == null || emailResolved.isBlank()) {
            throw new IllegalStateException("Cannot resolve current user (no email/username in JWT).");
        }

        Optional<User> existing = userRepo.findByEmail(emailResolved);
        if (existing.isPresent()) return existing.get();

        User u = new User();
        u.setEmail(emailResolved);
        String name = jwt.getClaimAsString("name");
        if (name == null || name.isBlank()) name = emailResolved;
        u.setDisplayName(name);
        return userRepo.save(u);
    }
}
