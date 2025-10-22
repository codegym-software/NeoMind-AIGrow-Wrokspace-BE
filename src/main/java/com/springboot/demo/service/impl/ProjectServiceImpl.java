package com.springboot.demo.service.impl;

import com.springboot.demo.dto.project.*;
import com.springboot.demo.model.Project;
import com.springboot.demo.model.ProjectMember;
import com.springboot.demo.model.enums.ProjectStatus;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.model.Role;
import com.springboot.demo.repository.*;
import com.springboot.demo.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository pmRepo;
    private final RoleRepository roleRepo;
    private final WorkspaceRepository workspaceRepo;

    private ProjectDto toDto(Project p) {
        return new ProjectDto(
                p.getId(), p.getWorkspaceId(), p.getName(), p.getDescription(),
                p.getColor(), p.getStatus(),
                p.getCreatedAt() == null ? null : p.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                p.getUpdatedAt() == null ? null : p.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                p.getArchivedAt() == null ? null : p.getArchivedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        );
    }

    @Override
    @Transactional
    public ProjectDto create(String workspaceId, CreateProjectReq req, String currentUserId) {
        workspaceRepo.findById(workspaceId).orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        Project p = Project.builder()
                .id(UUID.randomUUID().toString())
                .workspaceId(workspaceId)
                .name(req.name() == null ? "Untitled Project" : req.name().trim())
                .description(req.description())
                .color(req.color())
                .status(ProjectStatus.active)
                .build();

        projectRepo.save(p);

        // Gắn owner (is_owner = true). role_id là role(scope='project', code='admin')
        Integer adminRoleId = roleRepo.findByScopeAndCode(Scope.project, "admin")
                .map(Role::getId)
                .orElseThrow(() -> new IllegalStateException("Missing seed role: scope=project, code=admin"));

        if (!pmRepo.existsByProjectIdAndUserId(p.getId(), currentUserId)) {
            pmRepo.save(ProjectMember.builder()
                    .projectId(p.getId())
                    .userId(currentUserId)
                    .roleId(adminRoleId)
                    .owner(true)
                    .build());
        }

        return toDto(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDto> listInWorkspace(String workspaceId, boolean includeArchived, String keyword) {
        List<Project> list = includeArchived
                ? projectRepo.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId)
                : projectRepo.findByWorkspaceIdAndStatusOrderByCreatedAtDesc(workspaceId, ProjectStatus.active);

        if (keyword != null && !keyword.isBlank()) {
            final String k = keyword.toLowerCase();
            list = list.stream()
                    .filter(p -> p.getName().toLowerCase().contains(k)
                            || (p.getDescription() != null && p.getDescription().toLowerCase().contains(k)))
                    .toList();
        }
        return list.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public ProjectDto update(String projectId, UpdateProjectReq req) {
        Project p = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        if (req.name() != null && !req.name().isBlank()) {
            p.setName(req.name().trim());
        }
        if (req.description() != null) {
            p.setDescription(req.description().trim());
        }
        if (req.color() != null) {
            p.setColor(req.color());
        }

        p.setUpdatedAt(LocalDateTime.now());
        projectRepo.save(p);
        return toDto(p);
    }

    @Override
    @Transactional
    public void delete(String projectId) {
        Project p = projectRepo.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        p.setStatus(ProjectStatus.archived);
        p.setArchivedAt(LocalDateTime.now());
        projectRepo.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDto> listMine(String workspaceId, String currentUserId) {
        List<Project> list = (workspaceId == null || workspaceId.isBlank())
                ? projectRepo.findAllByMember(currentUserId)
                : projectRepo.findAllByMemberInWorkspace(currentUserId, workspaceId);

        return list.stream()
                .filter(p -> p.getStatus() == ProjectStatus.active)
                .map(this::toDto)
                .toList();
    }
}
