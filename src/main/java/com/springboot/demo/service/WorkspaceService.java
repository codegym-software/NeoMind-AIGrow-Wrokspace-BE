package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.*;
import com.springboot.demo.model.Workspace;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WorkspaceService {
    Workspace create (WorkspaceCreateRequest req, String actorId);

    Workspace getById(String workspaceId);

    Workspace update(String workspaceId, WorkspaceUpdateRequest req);

    Workspace save(Workspace c);

    Page<Workspace> search(String q, int page, int size);

    Page<Workspace> searchByCurrentUser(String q, int page, int size, String actorId);

    void delete(String workspaceId);
}
