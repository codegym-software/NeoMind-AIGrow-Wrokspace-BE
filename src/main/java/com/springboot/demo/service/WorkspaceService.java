package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceDto;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Page<Workspace> searchMyWorkspacesInCompany(String companyId, String userId, String query, boolean includeArchived, Pageable pageable);
}

