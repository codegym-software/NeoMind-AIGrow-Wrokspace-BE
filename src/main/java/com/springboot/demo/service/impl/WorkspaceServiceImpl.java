package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.*;
import com.springboot.demo.model.*;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.*;
import com.springboot.demo.service.RolePermissionService;
import com.springboot.demo.service.WorkspaceMemberService;
import com.springboot.demo.service.WorkspaceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceMemberRepository workspaceMemberRepo;
    private final CompanyMemberRepository companyMemberRepo;
    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository projectMemberRepo;
    private final RoleRepository roleRepo;

    // ⚠️ Sử dụng RBAC repo đã có sẵn trong dự án.
    // Đảm bảo bean này có method tương đương "getPermissionCodesByRoleId".
    private final RbacRepository rbacRepository;
    private final WorkspaceMemberService workspaceMemberService;
    private final RolePermissionService rolePermissionService;
    // ========================= CRUD =========================

    @Override
    @Transactional
    public WorkspaceDto createWorkspace(WorkspaceCreateRequest req, String currentUserId) {
        // 1) Validate quyền tạo & tính hợp lệ (gate ở Controller), ở service kiểm tra thêm điều kiện cơ bản
        if (workspaceRepo.existsByCompanyIdAndNameIgnoreCase(req.companyId(), req.name().trim())) {
            throw new IllegalArgumentException("Tên workspace đã tồn tại trong công ty");
        }

        // (Khuyến nghị) đảm bảo currentUser thuộc company
        if (!companyMemberRepo.existsByCompanyIdAndUserId(req.companyId(), currentUserId)) {
            throw new IllegalStateException("User hiện tại không thuộc công ty");
        }

        // 2) Tạo workspace
        Workspace w = Workspace.builder()
                .companyId(req.companyId())
                .name(req.name().trim())
                .description(req.description())
                .build();
        workspaceRepo.save(w);

        // 3) Giống createCompany: TỰ GEN OWNER ROLE (scope=workspace, code='owner')
        int ownerRoleId = ensureWorkspaceOwnerRole(w.getId()); // hoặc RoleService.ensure...

        workspaceMemberService.createOwnerMembership(w.getId(), currentUserId, ownerRoleId);

        // 4) Add creator làm OWNER member (mời = vào luôn, nhưng owner auto => invitedBy = null)
        WorkspaceMember owner = WorkspaceMember.builder()
                .workspaceId(w.getId())
                .userId(currentUserId)
                .roleId(ownerRoleId)
                .isOwner(true)
                .invitedBy(null)               // phân biệt với invite
                .invitedEmail(null)
                .joinedAt(LocalDateTime.now())
                .build();
        workspaceMemberRepo.save(owner);

        return WorkspaceDto.fromEntity(w);
    }

    @Override
    public WorkspaceDto getWorkspace(String id, String companyId) {
        Workspace w = workspaceRepo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));
        return WorkspaceDto.fromEntity(w);
    }

    @Override
    @Transactional
    public WorkspaceDto updateWorkspace(String id, String companyId, WorkspaceUpdateRequest req, String currentUserId) {
        Workspace w = workspaceRepo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));

        if (req.name() != null && !req.name().isBlank()) {
            String newName = req.name().trim();
            if (!newName.equalsIgnoreCase(w.getName()) &&
                    workspaceRepo.existsByCompanyIdAndNameIgnoreCase(companyId, newName)) {
                throw new IllegalArgumentException("Tên workspace đã tồn tại trong công ty");
            }
            w.setName(newName);
        }
        if (req.description() != null) {
            w.setDescription(req.description());
        }
        return WorkspaceDto.fromEntity(w);
    }

    @Override
    @Transactional
    public void deleteWorkspaceHard(String id, String companyId, String currentUserId) {
        Workspace w = workspaceRepo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));

        long projectCount = projectRepo.countByWorkspaceId(w.getId());
        if (projectCount > 0) {
            throw new IllegalStateException("Không thể xóa: Workspace còn " + projectCount + " project. Hãy xóa project trước.");
        }

        workspaceMemberRepo.deleteAllByWorkspaceId(w.getId());
        workspaceRepo.delete(w);
    }

    private int ensureWorkspaceOwnerRole(String workspaceId) {
        return roleRepo.findByScopeAndTargetIdAndCode("workspace", workspaceId, "owner")
                .map(Role::getId)
                .orElseGet(() -> {
                    Role owner = new Role();
                    owner.setScope(Scope.workspace);   // Enum mapped as String in entity
                    owner.setTargetId(workspaceId);
                    owner.setCode("owner");
                    owner.setName("Owner");
                    owner.setDescription("Workspace Owner");
                    Role saved = roleRepo.save(owner);
                    return saved.getId();
                });
    }

    // ===================== LISTING (#1, #8) =====================

    @Override
    public List<WorkspaceDto> listMyWorkspaces(String companyId, String userId) {
        return workspaceRepo.findAllByUserInCompany(userId, companyId)
                .stream().map(WorkspaceDto::fromEntity).toList();
    }

    @Override
    public List<WorkspaceDto> listWorkspacesInvitedMe(String companyId, String userId) {
        return workspaceRepo.findAllInvitedForUserInCompany(userId, companyId)
                .stream().map(WorkspaceDto::fromEntity).toList();
    }

    // ================= Members (Invite like Company) (#5) =================

    @Override
    @Transactional
    public WorkspaceInviteResponse inviteMember(String workspaceId, WorkspaceInviteRequest req, String inviterUserId) {
        Workspace w = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));

        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy user theo email: " + req.getEmail()));

        if (!companyMemberRepo.existsByCompanyIdAndUserId(w.getCompanyId(), user.getId())) {
            throw new IllegalArgumentException("User không thuộc công ty của workspace");
        }

        if (workspaceMemberRepo.existsByWorkspaceIdAndUserId(workspaceId, user.getId())) {
            WorkspaceMember existed = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, user.getId()).get();
            return new WorkspaceInviteResponse("already_member", toMemberDto(existed, user, null));
        }

        // mời = vào luôn (giống company)
        WorkspaceMember wm = WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(user.getId())
                .roleId(req.getRoleId())
                .isOwner(false)
                .invitedBy(inviterUserId)
                .invitedEmail(req.getEmail())
                .joinedAt(LocalDateTime.now())
                .build();
        workspaceMemberRepo.save(wm);

        WorkspaceMemberDto dto = toMemberDto(wm, user, null);
        return workspaceMemberService.invite(workspaceId, req, inviterUserId);
    }

    @Override
    public List<WorkspaceMemberDto> listMembers(String workspaceId) {
        List<WorkspaceMember> members = workspaceMemberRepo.findAllByWorkspaceId(workspaceId);
        return members.stream()
                .map(wm -> userRepo.findById(wm.getUserId())
                        .map(u -> toMemberDto(wm, u, null))
                        .orElseGet(() -> toMemberDto(wm, null, null)))
                .collect(Collectors.toList());
    }

    private WorkspaceMemberDto toMemberDto(WorkspaceMember wm, User user, String roleName) {
        return WorkspaceMemberDto.builder()
                .id(wm.getId())
                .workspaceId(wm.getWorkspaceId())
                .userId(wm.getUserId())
                .roleId(wm.getRoleId())
                .isOwner(wm.isOwner())
                .userEmail(user != null ? user.getEmail() : null)
                .userDisplayName(user != null ? user.getDisplayName() : null)
                .roleName(roleName)
                .invitedEmail(wm.getInvitedEmail())
                .invitedBy(wm.getInvitedBy())
                .joinedAt(wm.getJoinedAt())
                .build();
    }

    // ================= My Access (#3) =================

    @Override
    public WorkspaceAccessDto getMyAccessInWorkspace(String workspaceId, String userId) {
        // Workspace permissions (theo role_id của membership)
        Set<String> wsPerms = new HashSet<>();
        workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(wm -> rolePermissionService.getPermissionCodesByRoleId(wm.getRoleId()))
                .orElseGet(Set::of);

        // Project permissions trong workspace (theo role_id từng project membership)
        List<WorkspaceAccessDto.ProjectAccess> projectAccesses = new ArrayList<>();
        projectMemberRepo.findByUserIdInWorkspace(userId, workspaceId)
                .forEach(pm -> {
                    Set<String> pp = new HashSet<>(rbacRepository.getPermissionCodesByRoleId(pm.getRoleId()));
                    projectAccesses.add(
                            WorkspaceAccessDto.ProjectAccess.builder()
                                    .projectId(pm.getProjectId())
                                    .projectPermissions(pp)
                                    .build()
                    );
                });

        return WorkspaceAccessDto.builder()
                .workspaceId(workspaceId)
                .workspacePermissions(wsPerms)
                .projects(projectAccesses)
                .build();
    }
}
