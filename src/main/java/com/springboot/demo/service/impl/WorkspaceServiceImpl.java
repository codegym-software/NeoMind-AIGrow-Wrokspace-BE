package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceDto;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.service.WorkspaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository repository;

    public WorkspaceServiceImpl(WorkspaceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Workspace create(WorkspaceCreateRequest request) {
        repository.findByCompanyIdAndNameAndNotArchived(request.companyId(), request.name())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Workspace with name '" + request.name() + "' already exists in this company");
                });

        Workspace workspace = new Workspace(
                request.companyId(),
                request.name().trim(),
                request.description() != null ? request.description().trim() : null
        );

        return repository.save(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public Workspace getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Workspace getByIdAndCompanyId(String id, String companyId) {
        return repository.findByIdAndCompanyIdAndNotArchived(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found with id: " + id + " in company: " + companyId));
    }

    @Override
    public Workspace update(String id, WorkspaceUpdateRequest request) {
        Workspace workspace = getById(id);

        if (request.name() != null && !request.name().trim().equals(workspace.getName())) {
            repository.findByCompanyIdAndNameAndNotArchived(workspace.getCompanyId(), request.name().trim())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new IllegalArgumentException("Workspace with name '" + request.name() + "' already exists in this company");
                        }
                    });
            workspace.setName(request.name().trim());
        }

        if (request.description() != null) {
            workspace.setDescription(request.description().trim());
        }

        return repository.save(workspace);
    }

    @Override
    public Workspace updateByCompanyId(String id, String companyId, WorkspaceUpdateRequest request) {
        Workspace workspace = getByIdAndCompanyId(id, companyId);
        return update(id, request);
    }

    @Override
    public void delete(String id) {
        Workspace workspace = getById(id);
        workspace.archive();
        repository.save(workspace);
    }

    @Override
    public void deleteByCompanyId(String id, String companyId) {
        Workspace workspace = getByIdAndCompanyId(id, companyId);
        delete(id);
    }

    @Override
    public Workspace restore(String id) {
        Workspace workspace = getById(id);
        if (!workspace.isArchived()) {
            throw new IllegalArgumentException("Workspace is not archived");
        }
        workspace.unarchive();
        return repository.save(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Workspace> getByCompanyId(String companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByCompanyIdAndNotArchived(companyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Workspace> searchByCompanyId(String companyId, String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (query == null || query.trim().isEmpty()) {
            return getByCompanyId(companyId, page, size);
        }
        return repository.searchByCompanyIdAndName(companyId, query.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workspace> getAllByCompanyId(String companyId) {
        return repository.findByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCompanyId(String companyId) {
        return repository.countByCompanyIdAndNotArchived(companyId);
    }

    @Override
    public List<WorkspaceDto> listMyWorkspacesInCompany(String companyId, String userId, boolean includeArchived) {
        List<Workspace> list = repository.findMineInCompany(companyId, userId, includeArchived);
        return list.stream().map(WorkspaceDto::fromEntity).toList();

    }
}
