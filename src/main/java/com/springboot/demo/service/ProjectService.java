package com.springboot.demo.service;

import com.springboot.demo.dto.project.ProjectCreateRequest;
import com.springboot.demo.dto.project.ProjectUpdateRequest;
import com.springboot.demo.model.Project;
import com.springboot.demo.model.ProjectStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProjectService {
    Project create(ProjectCreateRequest request);

    Project getById(String id);

    Project getByIdAndWorkspaceId(String id, String workspaceId);

    Project update(String id, ProjectUpdateRequest request);

    Project updateByWorkspaceId(String id, String workspaceId, ProjectUpdateRequest request);

    void delete(String id);

    void deleteByWorkspaceId(String id, String workspaceId);

    Project archive(String id);

    Project archiveByWorkspaceId(String id, String workspaceId);

    Project restore(String id);

    Project restoreByWorkspaceId(String id, String workspaceId);

    Page<Project> getByWorkspaceId(String workspaceId, int page, int size);

    Page<Project> searchByWorkspaceId(String workspaceId, String query, int page, int size);

    Page<Project> getByWorkspaceIdAndStatus(String workspaceId, ProjectStatus status, int page, int size);

    List<Project> getAllByWorkspaceId(String workspaceId);

    List<Project> getActiveProjectsByWorkspaceId(String workspaceId);

    List<Project> getOverdueProjectsByWorkspaceId(String workspaceId);

    List<Project> getProjectsEndingSoon(String workspaceId, int days);

    long countByWorkspaceId(String workspaceId);

    long countByWorkspaceIdAndStatus(String workspaceId, ProjectStatus status);
}

