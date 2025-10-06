package com.springboot.demo.service.impl;

import com.springboot.demo.dto.project.ProjectCreateRequest;
import com.springboot.demo.dto.project.ProjectUpdateRequest;
import com.springboot.demo.model.Project;
import com.springboot.demo.model.ProjectStatus;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repository;

    public ProjectServiceImpl(ProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public Project create(ProjectCreateRequest request) {
        // Check for duplicate project name in the same workspace
        repository.findByWorkspaceIdAndNameAndNotArchived(request.workspaceId(), request.name())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Project with name '" + request.name() + "' already exists in this workspace");
                });

        Project project = new Project();
        project.setWorkspaceId(request.workspaceId());
        project.setName(request.name().trim());
        project.setDescription(request.description());

        if (request.status() != null) {
            project.setStatus(request.status());
        }
        project.setColor(request.color());

        return repository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Project getByIdAndWorkspaceId(String id, String workspaceId) {
        return repository.findByIdAndWorkspaceIdAndNotArchived(id, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id + " in workspace: " + workspaceId));
    }

    @Override
    public Project update(String id, ProjectUpdateRequest request) {
        Project project = getById(id);
        return updateProject(project, request);
    }

    @Override
    public Project updateByWorkspaceId(String id, String workspaceId, ProjectUpdateRequest request) {
        Project project = getByIdAndWorkspaceId(id, workspaceId);
        return updateProject(project, request);
    }

    private Project updateProject(Project project, ProjectUpdateRequest request) {
        // Check for duplicate name if name is being changed
        if (request.name() != null && !request.name().equals(project.getName())) {
            repository.findByWorkspaceIdAndNameAndNotArchived(project.getWorkspaceId(), request.name())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(project.getId())) {
                            throw new IllegalArgumentException("Project with name '" + request.name() + "' already exists in this workspace");
                        }
                    });
            project.setName(request.name().trim());
        }

        if (request.description() != null) {
            project.setDescription(request.description().trim());
        }
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        if (request.color() != null) {
            project.setColor(request.color());
        }

        return repository.save(project);
    }

    @Override
    public void delete(String id) {
        Project project = getById(id);
        repository.delete(project);
    }

    @Override
    public void deleteByWorkspaceId(String id, String workspaceId) {
        Project project = getByIdAndWorkspaceId(id, workspaceId);
        repository.delete(project);
    }

    @Override
    public Project archive(String id) {
        Project project = getById(id);
        project.archive();
        return repository.save(project);
    }

    @Override
    public Project archiveByWorkspaceId(String id, String workspaceId) {
        Project project = getByIdAndWorkspaceId(id, workspaceId);
        project.archive();
        return repository.save(project);
    }

    @Override
    public Project restore(String id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
        project.unarchive();
        return repository.save(project);
    }

    @Override
    public Project restoreByWorkspaceId(String id, String workspaceId) {
        Project project = repository.findById(id)
                .filter(p -> p.getWorkspaceId().equals(workspaceId))
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id + " in workspace: " + workspaceId));
        project.unarchive();
        return repository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> getByWorkspaceId(String workspaceId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.findByWorkspaceIdAndNotArchived(workspaceId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> searchByWorkspaceId(String workspaceId, String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.searchByWorkspaceIdAndName(workspaceId, query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> getByWorkspaceIdAndStatus(String workspaceId, ProjectStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.findByWorkspaceIdAndStatus(workspaceId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllByWorkspaceId(String workspaceId) {
        return repository.findByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getActiveProjectsByWorkspaceId(String workspaceId) {
        return repository.findActiveProjectsByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getOverdueProjectsByWorkspaceId(String workspaceId) {
        return repository.findOverdueProjectsByWorkspaceId(workspaceId, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsEndingSoon(String workspaceId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return repository.findProjectsEndingSoon(workspaceId, today, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByWorkspaceId(String workspaceId) {
        return repository.countByWorkspaceIdAndNotArchived(workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByWorkspaceIdAndStatus(String workspaceId, ProjectStatus status) {
        return repository.countByWorkspaceIdAndStatus(workspaceId, status);
    }
}

