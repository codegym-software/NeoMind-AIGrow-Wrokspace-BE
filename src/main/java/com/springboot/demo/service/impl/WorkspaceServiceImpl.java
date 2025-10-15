package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.WorkspaceCreateInCompanyRequest;
import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceDto;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.repository.WorkspaceMemberRepository;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.service.WorkspaceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    private final com.springboot.demo.repository.ProjectRepository projectRepository;
    private final com.springboot.demo.repository.ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public Workspace create(WorkspaceCreateRequest req) {
        if (req == null || req.companyId() == null || req.companyId().isBlank()) {
            throw new IllegalArgumentException("CompanyId is required");
        }
        Workspace w = new Workspace();
        w.setCompanyId(req.companyId());
        w.setName(req.name());
        w.setDescription(req.description());
        w.setCreatedAt(LocalDateTime.now());
        w.setUpdatedAt(LocalDateTime.now());
        return workspaceRepository.save(w);
    }

    @Override
    @Transactional
    public Workspace createInCompany(String companyId, WorkspaceCreateInCompanyRequest req, String actorUserId) {
        if (companyId == null || companyId.isBlank()) {
            throw new IllegalArgumentException("companyId is required");
        }
        Workspace w = new Workspace();
        w.setCompanyId(companyId);
        w.setName(req.name());
        w.setDescription(req.description());
        w.setCreatedAt(LocalDateTime.now());
        w.setUpdatedAt(LocalDateTime.now());
        Workspace saved = workspaceRepository.save(w);

        WorkspaceMember owner = new WorkspaceMember();
        owner.setWorkspaceId(saved.getId());
        owner.setUserId(actorUserId);
        owner.setRoleId(resolveWorkspaceOwnerRoleId());
        owner.setOwner(true);
        workspaceMemberRepository.save(owner);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Workspace getById(String id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + id));
    }

    @Override
    @Transactional
    public Workspace update(String id, WorkspaceUpdateRequest req) {
        Workspace w = getById(id);
        if (req.name() != null && !req.name().isBlank()) {
            w.setName(req.name());
        }
        if (req.description() != null) {
            w.setDescription(req.description());
        }
        w.setUpdatedAt(LocalDateTime.now());
        return workspaceRepository.save(w);
    }

    @Override
    @Transactional
    public void deleteHard(String id) {
        // Delete related data first
        projectMemberRepository.deleteByProjectWorkspaceId(id);
        projectRepository.deleteByWorkspaceId(id);
        workspaceMemberRepository.deleteByWorkspaceId(id);
        workspaceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workspace> searchByCompanyId(String companyId, String q) {
        return workspaceRepository.searchByCompanyId(companyId, q);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceDto> listMyWorkspacesInCompany(String companyId, String userId, boolean includeArchived) {
        List<Workspace> list = workspaceRepository.findMineInCompany(companyId, userId, includeArchived);
        return list.stream()
                .sorted(Comparator.comparing(Workspace::getCreatedAt).reversed())
                .map(WorkspaceDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Workspace> searchByCurrentUser(String q, int page, int size, String actorUserId) {
        var pageable = PageRequest.of(page, size);
        var p = workspaceRepository.findByUserIdAndNameContainingIgnoreCase(actorUserId, q == null ? "" : q, pageable);
        return new PageImpl<>(p.getContent(), pageable, p.getTotalElements());
    }

    private Integer resolveWorkspaceOwnerRoleId() {
        return 1;
    }
}
