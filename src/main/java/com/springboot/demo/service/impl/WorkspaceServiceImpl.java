package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.*;
import com.springboot.demo.model.*;

import com.springboot.demo.repository.*;

import com.springboot.demo.service.WorkspaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.springboot.demo.model.Workspace;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.NoSuchElementException;


@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceMemberRepository workspaceMemberRepo;



    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepo, WorkspaceMemberRepository workspaceMemberRepo) {
        this.workspaceRepo = workspaceRepo;
        this.workspaceMemberRepo = workspaceMemberRepo;
    }
    @Override
    @Transactional
    public Workspace create(WorkspaceCreateRequest req, String companyId, String actorId) {
        if (workspaceRepo.existsByCompanyIdAndNameIgnoreCase(req.companyId(), req.name().trim())) {
            throw new IllegalArgumentException("Tên workspace đã tồn tại trong công ty");
        }

        Workspace w = new Workspace();
        w.setCompanyId(req.companyId());
        w.setName(req.name() == null ? null : req.name().trim());
        w.setDescription(req.description());
        w = workspaceRepo.save(w);

        if (workspaceMemberRepo.existsByWorkspaceIdAndIsOwnerTrue(w.getId())) {
            throw new IllegalStateException("Workspace already has an owner.");
        }

        if (!workspaceMemberRepo.existsByWorkspaceIdAndUserId(w.getId(), actorId)) {
            WorkspaceMember wm = new WorkspaceMember();
            wm.setWorkspaceId(w.getId());
            wm.setUserId(actorId);
            wm.setRoleId(null);
            wm.setOwner(true);
            wm.setJoinedAt(LocalDateTime.now());
            workspaceMemberRepo.save(wm);
        } else {
            WorkspaceMember wm = workspaceMemberRepo.findByWorkspaceIdAndUserId(w.getId(), actorId).orElseThrow();
            wm.setRoleId(null);
            wm.setOwner(true);
            workspaceMemberRepo.save(wm);
        }

        return w;
    }

    @Override
    @Transactional(readOnly = true)
    public Workspace getById(String workspaceId) {
        return workspaceRepo.findById(workspaceId).orElseThrow(() -> new NoSuchElementException("Workspace not found. " +  workspaceId));
    }

    @Override
    @Transactional
    public Workspace update(String workspaceId, String companyId, WorkspaceUpdateRequest req) {
        Workspace w = getById(workspaceId);
        if (req.name() != null && !req.name().isBlank()) {
            String newName = req.name().trim();
            if (!newName.equalsIgnoreCase(w.getName()) &&
                    workspaceRepo.existsByCompanyIdAndNameIgnoreCase(companyId, newName)) {
                throw new IllegalArgumentException("Tên workspace đã tồn tại trong công ty");
            }
            w.setName(newName);
        }
        w.setDescription(req.description());
        return workspaceRepo.save(w);
    }

    private void validateNameUnique(String companyId, String name) {
        if (workspaceRepo.existsByCompanyIdAndNameIgnoreCase(companyId, name.trim())) {
            throw new IllegalArgumentException("Tên workspace đã tồn tại trong công ty");
        }
    }
    @Override
    @Transactional
    public Workspace save(Workspace w) {
        return workspaceRepo.save(w);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Workspace> search(String q, int page, int size) {
        var pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) return workspaceRepo.findAll(pageable);
        return workspaceRepo.findByNameContainingIgnoreCase(q.trim(), pageable);
    }

    @Override
    public Page<Workspace> searchByCurrentUser(String q, int page, int size, String actorId) {
        var workspaceIds = workspaceMemberRepo.findWorkspaceIdsByUserId(actorId);
        var pageable = PageRequest.of(page, size);

        if (workspaceIds == null || workspaceIds.isEmpty()) {
            return Page.empty(pageable);
        }

        if (q == null || q.isBlank()) {
            return workspaceRepo.findByIdIn(workspaceIds, pageable);
        } else {
            return workspaceRepo.findByIdInAndNameContainingIgnoreCase(workspaceIds, q.trim(), pageable);
        }
    }

    @Override
    @Transactional
    public void delete(String workspaceId) {
        if (!workspaceRepo.existsById(workspaceId)) {
            throw new NoSuchElementException("Workspace not found. " +  workspaceId);
        }
        workspaceRepo.deleteById(workspaceId);
    }
}
