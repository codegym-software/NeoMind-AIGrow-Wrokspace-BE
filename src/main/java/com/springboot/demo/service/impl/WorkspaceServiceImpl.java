package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.WorkspaceCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceDto;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.repository.WorkspaceMemberRepository;
import com.springboot.demo.repository.CompanyRepository;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.repository.UserRepository;
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
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public WorkspaceServiceImpl(WorkspaceRepository repository, 
                               WorkspaceMemberRepository workspaceMemberRepository,
                               CompanyRepository companyRepository,
                               RoleRepository roleRepository,
                               UserRepository userRepository) {
        this.repository = repository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Workspace create(WorkspaceCreateRequest request) {
        return create(request, null);
    }

    @Override
    public Workspace create(WorkspaceCreateRequest request, String creatorUserId) {
        try {
            System.out.println("=== SERVICE DEBUG ===");
            System.out.println("Request companyId: " + request.companyId());
            System.out.println("Request name: " + request.name());
            System.out.println("Creator userId: " + creatorUserId);
            
            // Validate company exists
            System.out.println("Checking company exists...");
            if (!companyRepository.existsById(request.companyId())) {
                throw new IllegalArgumentException("Company with ID " + request.companyId() + " does not exist");
            }
            System.out.println("Company exists ✓");

            // Validate workspace name is unique in company
            System.out.println("Checking workspace name uniqueness...");
            repository.findByCompanyIdAndNameAndNotArchived(request.companyId(), request.name())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Workspace with name '" + request.name() + "' already exists in this company");
                    });
            System.out.println("Workspace name unique ✓");

            // Validate user exists if creatorUserId is provided
            String actualUserId = null;
            if (creatorUserId != null && !creatorUserId.trim().isEmpty()) {
                System.out.println("Checking user exists by ID: " + creatorUserId);
                
                // Try to find user by ID first
                if (userRepository.existsById(creatorUserId)) {
                    actualUserId = creatorUserId;
                    System.out.println("User found by ID ✓");
                } else {
                    // If not found by ID, try to find by email (assuming creatorUserId is email)
                    System.out.println("User not found by ID, trying to find by email...");
                    var userByEmail = userRepository.findByEmail(creatorUserId);
                    if (userByEmail.isPresent()) {
                        actualUserId = userByEmail.get().getId();
                        System.out.println("User found by email: " + actualUserId + " ✓");
                    } else {
                        System.out.println("WARNING: User not found by ID or email: " + creatorUserId);
                        // Continue without user (created_by will be null)
                    }
                }
                
                if (actualUserId != null) {
                    System.out.println("Checking role exists...");
                    if (!roleRepository.existsById(200L)) {
                        throw new IllegalArgumentException("Workspace Admin role (ID: 200) does not exist");
                    }
                    System.out.println("Role exists ✓");
                }
            }

            System.out.println("Creating workspace entity...");
            Workspace workspace = new Workspace(
                    request.companyId(),
                    actualUserId, // Use the resolved user ID
                    request.name().trim(),
                    request.description() != null ? request.description().trim() : null
            );
            System.out.println("Workspace entity created ✓");

            System.out.println("Saving workspace to database...");
            Workspace savedWorkspace = repository.save(workspace);
            System.out.println("Workspace saved with ID: " + savedWorkspace.getId());

            // Auto-add creator as Workspace Admin (role_id = 200) if actualUserId is resolved
            if (actualUserId != null && !actualUserId.trim().isEmpty()) {
                System.out.println("Adding creator to workspace members...");
                // Check if user is already a member (idempotent)
                if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(savedWorkspace.getId(), actualUserId)) {
                    WorkspaceMember member = new WorkspaceMember(
                            savedWorkspace.getId(),
                            actualUserId,
                            200L // Workspace Admin role ID
                    );
                    workspaceMemberRepository.save(member);
                    System.out.println("Creator added as workspace member ✓");
                } else {
                    System.out.println("Creator already a member, skipping ✓");
                }
            } else {
                System.out.println("No valid user ID found, skipping workspace member creation");
            }

            System.out.println("=== SERVICE COMPLETED SUCCESSFULLY ===");
            return savedWorkspace;
        } catch (Exception e) {
            System.err.println("=== SERVICE ERROR ===");
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("===================");
            throw new RuntimeException("Failed to create workspace: " + e.getMessage(), e);
        }
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
    public Page<WorkspaceDto> listMyWorkspacesInCompany(String companyId, String userId,
                                                        int page, int size, boolean includeArchived) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));
        Page<Workspace> p = repository.findMineInCompany(companyId, userId, includeArchived, pageable);
        return p.map(WorkspaceDto::fromEntity);
    }
}
