package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceDto;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.exception.DuplicateWorkspaceException;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.repository.WorkspaceMemberRepository;
import com.springboot.demo.repository.CompanyRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.service.RoleService;
import com.springboot.demo.service.WorkspaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository repository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final NamedParameterJdbcTemplate jdbc;

    public WorkspaceServiceImpl(WorkspaceRepository repository, 
                               WorkspaceMemberRepository workspaceMemberRepository,
                               CompanyRepository companyRepository,
                               UserRepository userRepository,
                               RoleService roleService,
                               NamedParameterJdbcTemplate jdbc) {
        this.repository = repository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.jdbc = jdbc;
    }

    @Override
    public Workspace create(WorkspaceCreateRequest request) {
        return create(request, null);
    }

    @Override
    public Workspace create(WorkspaceCreateRequest request, String creatorUserId) {
        // Validate company exists
        if (!companyRepository.existsById(request.companyId())) {
            throw new IllegalArgumentException("Company with ID " + request.companyId() + " does not exist");
        }

        // Validate workspace name is unique in company
        repository.findByCompanyIdAndNameAndNotArchived(request.companyId(), request.name())
                .ifPresent(existing -> {
                    throw new DuplicateWorkspaceException("Workspace with name '" + request.name() + "' already exists in this company");
                });

        // Validate user exists if creatorUserId is provided
        String actualUserId = null;
        if (creatorUserId != null && !creatorUserId.trim().isEmpty()) {
            // Try to find user by ID first
            if (userRepository.existsById(creatorUserId)) {
                actualUserId = creatorUserId;
            } else {
                // If not found by ID, try to find by email (assuming creatorUserId is email)
                var userByEmail = userRepository.findByEmail(creatorUserId);
                if (userByEmail.isPresent()) {
                    actualUserId = userByEmail.get().getId();
                }
            }
        }

        // Create workspace entity
        Workspace workspace = new Workspace(
                request.companyId(),
                request.name().trim(),
                request.description() != null ? request.description().trim() : null
        );

        Workspace savedWorkspace = repository.save(workspace);

        // Tạo mặc định 2 role admin và member cho workspace mới
        roleService.createDefaultWorkspaceRoles(savedWorkspace.getId());

        // Auto-add creator as Workspace Owner if actualUserId is resolved
        if (actualUserId != null && !actualUserId.trim().isEmpty()) {
            // Check if user is already a member (idempotent)
            if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(savedWorkspace.getId(), actualUserId)) {
                WorkspaceMember member = WorkspaceMember.asOwner(
                        savedWorkspace.getId(),
                        actualUserId
                );
                workspaceMemberRepository.save(member);
                // Owner đã có đầy đủ quyền nên không cần gán thêm role admin
            }
        }

        return savedWorkspace;
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
                            throw new DuplicateWorkspaceException("Workspace with name '" + request.name() + "' already exists in this company");
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
        getByIdAndCompanyId(id, companyId); // Validate user has access to workspace in company
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
        getByIdAndCompanyId(id, companyId); // Validate user has access to workspace in company
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
    public List<Workspace> getByCompanyId(String companyId) {
        return repository.findByCompanyIdAndNotArchived(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workspace> searchByCompanyId(String companyId, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getByCompanyId(companyId);
        }
        return repository.searchByCompanyIdAndName(companyId, query.trim());
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

    @Override
    @Transactional(readOnly = true)
    public Page<Workspace> searchMyWorkspacesInCompany(String companyId, String userId, String query, boolean includeArchived, Pageable pageable) {
        return workspaceMemberRepository.findMyWorkspacesInCompany(companyId, userId, query, includeArchived, pageable);
    }
}
