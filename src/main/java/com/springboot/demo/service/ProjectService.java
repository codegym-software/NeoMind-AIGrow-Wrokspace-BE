package com.springboot.demo.service;
import com.springboot.demo.dto.project.*;

import java.util.List;

public interface ProjectService {
    ProjectDto create(String workspaceId, CreateProjectReq req, String currentUserId);
    List<ProjectDto> listInWorkspace(String workspaceId, boolean includeArchived, String keyword);
    ProjectDto update(String projectId, UpdateProjectReq req);
    void delete(String projectId); // soft-delete
    List<ProjectDto> listMine(String workspaceId, String currentUserId);
    List<ProjectDto> listMineInCompany(String companyId, String currentUserId);
    ProjectDto getProjectById(String projectId);
}
