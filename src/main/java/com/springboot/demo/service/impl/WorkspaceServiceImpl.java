package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.*;
import com.springboot.demo.model.*;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.*;
import com.springboot.demo.service.RolePermissionService;
import com.springboot.demo.service.WorkspaceMemberService;
import com.springboot.demo.service.WorkspaceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.springboot.demo.model.Workspace;


@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceMemberRepository workspaceMemberRepo;



    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepo, WorkspaceMemberRepository workspaceMemberRepo) {
        this.workspaceRepo = workspaceRepo;
        this.workspaceMemberRepo = workspaceMemberRepo;
    }
    @Override
    public Workspace create(WorkspaceCreateRequest req, String actorId) {

        return null;
    }

    @Override
    public Workspace getById(String workspaceId) {
        return null;
    }

    @Override
    public Workspace update(String workspaceId, WorkspaceUpdateRequest req) {
        return null;
    }

    @Override
    public Workspace save(Workspace c) {
        return null;
    }

    @Override
    public Page<Workspace> search(String q, int page, int size) {
        return null;
    }

    @Override
    public Page<Workspace> searchByCurrentUser(String q, int page, int size, String actorId) {
        return null;
    }

    @Override
    public void delete(String workspaceId) {

    }
}
