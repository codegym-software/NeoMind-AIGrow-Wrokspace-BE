package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceDto;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WorkspaceService {
    Workspace create(WorkspaceCreateRequest request);
    Workspace create(WorkspaceCreateRequest request, String creatorUserId);

    Workspace getById(String id);

    Workspace getByIdAndCompanyId(String id, String companyId);

    Workspace update(String id, WorkspaceUpdateRequest request);

    Workspace updateByCompanyId(String id, String companyId, WorkspaceUpdateRequest request);

    void delete(String id);

    void deleteByCompanyId(String id, String companyId);

    Workspace restore(String id);

    List<Workspace> getByCompanyId(String companyId);

    List<Workspace> searchByCompanyId(String companyId, String query);

    List<Workspace> getAllByCompanyId(String companyId);

    long countByCompanyId(String companyId);

    List<WorkspaceDto> listMyWorkspacesInCompany(String companyId, String userId, boolean includeArchived);

    // New method for current user workspaces
    Page<Workspace> searchByCurrentUser(String query, int page, int size, String actorId);
}

