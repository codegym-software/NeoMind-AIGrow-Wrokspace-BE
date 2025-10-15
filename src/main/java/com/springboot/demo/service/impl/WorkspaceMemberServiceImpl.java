package com.springboot.demo.service.impl;

import com.springboot.demo.dto.company.InviteResponse;
import com.springboot.demo.dto.workspace.AddWorkspaceMemberReq;
import com.springboot.demo.dto.workspace.AssignWorkspaceRoleReq;
import com.springboot.demo.dto.workspace.WorkspaceInviteRequest;
import com.springboot.demo.dto.workspace.WorkspaceInviteResponse;
import com.springboot.demo.model.*;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.*;
import com.springboot.demo.service.EmailService;
import com.springboot.demo.service.WorkspaceMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceMemberRepository workspaceMemberRepo;
    private final UserRepository userRepo;
    private final MembershipCleanupRepository membershipCleanup;
    private final RoleRepository roleRepo;
    private final EmailService emailService;

    @Value("${app.defaults.roleId:5}")
    private Integer defaultRoleId;
    
    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMember> list(String workspaceId) {
        return workspaceMemberRepo.findAllByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public void addMember(String workspaceId, AddWorkspaceMemberReq req, String actorId) {
        if (req.userId() == null || req.userId().isBlank()) throw new IllegalArgumentException("userId required");
        if (req.roleId() == null) throw new IllegalArgumentException("roleId required");

        var actor = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc workspace này"));

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        if (role.getScope() != Scope.workspace) throw new IllegalArgumentException("Role không thuộc scope workspace");

        WorkspaceMember m = new WorkspaceMember();
        m.setWorkspaceId(workspaceId);
        m.setUserId(req.userId());
        m.setOwner(false);
        m.setRoleId(role.getId());
        workspaceMemberRepo.save(m);
    }

    @Override
    @Transactional
    public void assignRole(String workspaceId, String targetUserId, AssignWorkspaceRoleReq req, String actorId) {
        var actor = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc workspace này"));

        var target = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        if (target.isOwner()) {
            throw new AccessDeniedException("Không thể đổi role cho Owner (dùng transfer-ownership)");
        }
        if (req.roleId() == null) {
            throw new IllegalArgumentException("roleId required");
        }
        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        if (role.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role không thuộc scope workspace");
        }

        target.setRoleId(role.getId());
        workspaceMemberRepo.save(target);
    }

    @Override
    public void removeMember(String workspaceId, String memberUserId, String name) {
        workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, memberUserId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));

        var target = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, memberUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        if (Boolean.TRUE.equals(target.isOwner())) {
            long owners = workspaceMemberRepo.countOwners(workspaceId);
            if (owners <= 1) {
                throw new AccessDeniedException("Không thể xóa Owner duy nhất. Hãy chuyển quyền sở hữu trước.");
            }
        }

        membershipCleanup.deleteProjectMembershipsByWorkspaceForUser(workspaceId, memberUserId);

        workspaceMemberRepo.deleteByWorkspaceIdAndUserId(workspaceId, memberUserId);
    }

    @Override
    @Transactional
    public void transferOwnership(String workspaceId, String toUserId, Integer downgradeRoleId, String actorId) {
        if (toUserId == null || toUserId.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toUserId required");
        if (downgradeRoleId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "downgradeRoleId required");

        var actor = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc workspace này"));
        if (!Boolean.TRUE.equals(actor.isOwner()))
            throw new AccessDeniedException("Chỉ Owner mới được chuyển quyền sở hữu");

        var target = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, toUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên được chuyển"));

        Role downgradeRole = roleRepo.findById(downgradeRoleId)
                .orElseThrow(() -> new NoSuchElementException("Role downgrade không tồn tại"));
        if (downgradeRole.getScope() != Scope.workspace)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role downgrade phải thuộc scope=workspace");
        if (downgradeRole.getTargetId() != null && !workspaceId.equals(downgradeRole.getTargetId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role downgrade không thuộc workspace này");

        if (actor.getUserId().equals(target.getUserId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner hiện tại đã là Owner");

        actor.setRoleId(downgradeRole.getId());
        actor.setOwner(false);
        workspaceMemberRepo.save(actor);

        target.setOwner(true);
        target.setRoleId(null);
        workspaceMemberRepo.save(target);
    }

    @Override
    @Transactional
    public WorkspaceInviteResponse inviteMemberByEmail(String workspaceId, String inviterEmail, String invitedEmail, Integer roleId) {
        var inviterUser = userRepo.findByEmail(inviterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Please login first"));
        var inviter = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, inviterUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));

        String normalized = invitedEmail == null ? null : invitedEmail.trim().toLowerCase();
        if (normalized == null || normalized.isBlank()) throw new IllegalArgumentException("Email không hợp lệ");

        Optional<User> invitedUserOpt = userRepo.findByEmail(normalized);
        if (invitedUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Email chưa đăng ký trong hệ thống");
        }

        String targetUserId = invitedUserOpt.get().getId();

        var existing = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, targetUserId);
        if (existing.isPresent()) {
            return new WorkspaceInviteResponse("already_member", existing.get());
        }

        var existingInvite = workspaceMemberRepo.findFirstByWorkspaceIdAndInvitedEmailIgnoreCase(workspaceId, normalized);
        if (existingInvite.isPresent()) {
            return new WorkspaceInviteResponse("existing_invite", existingInvite.get());
        }

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspaceId);
        member.setUserId(targetUserId);
        member.setInvitedEmail(normalized);
        member.setInvitedBy(inviter.getUserId());
        member.setRoleId(roleId != null ? roleId : defaultRoleId);
        member.setOwner(false);

        WorkspaceMember saved = workspaceMemberRepo.save(member);

        try {
            emailService.sendInvitationEmail(normalized, "Workspace", inviterEmail);
        } catch (Exception ignore) {}

        return new WorkspaceInviteResponse("created", saved);
    }
}
