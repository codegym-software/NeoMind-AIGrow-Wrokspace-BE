package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.WorkspaceCreateInCompanyRequest;
import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceDto;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WorkspaceService {
    Workspace create(WorkspaceCreateRequest req);
    Workspace createInCompany(String companyId, WorkspaceCreateInCompanyRequest req, String actorUserId);

    Workspace getById(String id);

    Workspace update(String id, WorkspaceUpdateRequest req);

    void deleteHard(String id);

    List<Workspace> searchByCompanyId(String companyId, String q);

    List<WorkspaceDto> listMyWorkspacesInCompany(String companyId, String userId, boolean includeArchived);

    Page<Workspace> searchByCurrentUser(String q, int page, int size, String actorUserId);
}
