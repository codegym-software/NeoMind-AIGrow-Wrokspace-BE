package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.WorkspaceMemberDto;
import com.springboot.demo.dto.workspace.WorkspaceMemberInviteRequest;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.User;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.repository.WorkspaceMemberRepository;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.service.WorkspaceMemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public WorkspaceMember inviteMember(String workspaceId, WorkspaceMemberInviteRequest req, String actorUserId) {
        // Kiểm tra workspace có tồn tại không
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));

        // Kiểm tra user có phải thành viên của công ty không
        companyMemberRepository.findByCompanyIdAndUserId(workspace.getCompanyId(), req.userId())
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of the company"));

        // Kiểm tra user đã có trong workspace chưa
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, req.userId())) {
            throw new IllegalArgumentException("User is already a member of this workspace");
        }

        // Tạo WorkspaceMember
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspaceId);
        member.setUserId(req.userId());
        member.setRoleId(req.roleId());
        member.setOwner(false); // Chỉ owner workspace mới có thể set owner = true
        member.setInvitedBy(actorUserId);

        return workspaceMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberDto> getMembers(String workspaceId) {
        return workspaceMemberRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(member -> {
                    // Fetch user info để lấy email và displayName
                    return userRepository.findById(member.getUserId())
                            .map(user -> WorkspaceMemberDto.fromEntityWithUserInfo(member, user.getEmail(), user.getDisplayName()))
                            .orElse(WorkspaceMemberDto.fromEntity(member));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(String workspaceId, String userId) {
        // Kiểm tra không được xóa owner
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        
        if (member.isOwner()) {
            throw new IllegalArgumentException("Cannot remove workspace owner");
        }

        workspaceMemberRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Override
    @Transactional
    public WorkspaceMember updateMemberRole(String workspaceId, String userId, Integer roleId) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        member.setRoleId(roleId);
        return workspaceMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberDto> getAvailableMembers(String workspaceId) {
        // Lấy workspace để biết companyId
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));

        // Lấy tất cả thành viên của công ty
        List<CompanyMember> companyMembers = companyMemberRepository.findAllByCompanyId(workspace.getCompanyId());

        // Lấy danh sách userId đã có trong workspace
        List<String> existingUserIds = workspaceMemberRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(WorkspaceMember::getUserId)
                .collect(Collectors.toList());

        // Lọc ra những thành viên công ty chưa có trong workspace và fetch user info
        return companyMembers.stream()
                .filter(cm -> !existingUserIds.contains(cm.getUserId()))
                .map(cm -> {
                    // Fetch user info để lấy email và displayName
                    return userRepository.findById(cm.getUserId())
                            .map(user -> WorkspaceMemberDto.builder()
                                    .userId(cm.getUserId())
                                    .roleId(cm.getRoleId())
                                    .userEmail(user.getEmail())
                                    .userDisplayName(user.getDisplayName())
                                    .build())
                            .orElse(WorkspaceMemberDto.builder()
                                    .userId(cm.getUserId())
                                    .roleId(cm.getRoleId())
                                    .build());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(String workspaceId, String userId) {
        return workspaceMemberRepository.isOwner(workspaceId, userId);
    }

    @Override
    @Transactional
    public WorkspaceMember inviteMemberByEmail(String workspaceId, String inviterEmail, String invitedEmail, Integer roleId) {
        // Kiểm tra workspace có tồn tại không
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));

        // Tìm user bằng email (với fallback)
        User invitedUser = userRepository.findByEmail(invitedEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + invitedEmail));

        // Kiểm tra user có phải thành viên của công ty không
        companyMemberRepository.findByCompanyIdAndUserId(workspace.getCompanyId(), invitedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of the company"));

        // Kiểm tra user đã có trong workspace chưa
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, invitedUser.getId())) {
            throw new IllegalArgumentException("User is already a member of this workspace");
        }

        // Tìm inviter bằng email
        User inviter = userRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new EntityNotFoundException("Inviter not found with email: " + inviterEmail));

        // Tạo WorkspaceMember
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspaceId);
        member.setUserId(invitedUser.getId());
        member.setRoleId(roleId);
        member.setOwner(false);
        member.setInvitedBy(inviter.getId());

        return workspaceMemberRepository.save(member);
    }

    @Override
    @Transactional
    public WorkspaceMember updateMemberRole(String workspaceId, String userId, Integer newRoleId, String actorId) {
        // Kiểm tra actor có quyền không (owner hoặc có permission)
        if (!isOwner(workspaceId, actorId)) {
            throw new IllegalArgumentException("Only workspace owner can update member roles");
        }

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        // Không cho phép thay đổi role của owner
        if (member.isOwner()) {
            throw new IllegalArgumentException("Cannot change role of workspace owner");
        }

        member.setRoleId(newRoleId);
        return workspaceMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void transferOwnership(String workspaceId, String toUserId, Integer downgradeRoleId, String actorId) {
        // Kiểm tra actor có phải owner không
        if (!isOwner(workspaceId, actorId)) {
            throw new IllegalArgumentException("Only workspace owner can transfer ownership");
        }

        // Kiểm tra target user có phải thành viên workspace không
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, toUserId)
                .orElseThrow(() -> new EntityNotFoundException("Target user is not a member of this workspace"));

        // Kiểm tra target user có phải thành viên công ty không
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));
        
        companyMemberRepository.findByCompanyIdAndUserId(workspace.getCompanyId(), toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user is not a member of the company"));

        // Lấy current owner
        WorkspaceMember currentOwner = workspaceMemberRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .filter(WorkspaceMember::isOwner)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Current owner not found"));

        // Kiểm tra role có tồn tại không
        Role downgradeRole = roleRepository.findById(downgradeRoleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + downgradeRoleId));

        // Kiểm tra role có phải workspace role không
        if (downgradeRole.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role must be a workspace role");
        }

        // Cập nhật current owner thành member với role mới
        currentOwner.setOwner(false);
        currentOwner.setRoleId(downgradeRoleId);
        workspaceMemberRepository.save(currentOwner);

        // Cập nhật target member thành owner (roleId = null vì owner có toàn quyền)
        targetMember.setOwner(true);
        targetMember.setRoleId(null); // Owner không cần role vì đã có toàn quyền
        workspaceMemberRepository.save(targetMember);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAvailableWorkspaceRoles() {
        return roleRepository.findByScopeAndTargetIdIsNull(Scope.workspace);
    }
}