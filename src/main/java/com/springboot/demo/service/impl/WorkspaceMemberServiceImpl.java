package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.WorkspaceInviteRequest;
import com.springboot.demo.dto.workspace.WorkspaceInviteResponse;
import com.springboot.demo.dto.workspace.WorkspaceMemberDto;
import com.springboot.demo.model.User;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.repository.WorkspaceMemberRepository;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.service.WorkspaceMemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceMemberRepository workspaceMemberRepo;
    private final CompanyMemberRepository companyMemberRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public void createOwnerMembership(String workspaceId, String ownerUserId, int ownerRoleId) {
        if (workspaceMemberRepo.existsByWorkspaceIdAndUserId(workspaceId, ownerUserId)) return;

        WorkspaceMember owner = WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(ownerUserId)
                .roleId(ownerRoleId)
                .isOwner(true)
                .invitedBy(null)               // không phải invite
                .invitedEmail(null)
                .joinedAt(LocalDateTime.now())
                .build();
        workspaceMemberRepo.save(owner);
    }

    @Override
    @Transactional
    public WorkspaceInviteResponse invite(String workspaceId, WorkspaceInviteRequest req, String inviterUserId) {
        Workspace w = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));

        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy user theo email: " + req.getEmail()));

        if (!companyMemberRepo.existsByCompanyIdAndUserId(w.getCompanyId(), user.getId())) {
            throw new IllegalArgumentException("User không thuộc công ty của workspace");
        }

        if (workspaceMemberRepo.existsByWorkspaceIdAndUserId(workspaceId, user.getId())) {
            WorkspaceMember existed = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, user.getId()).get();
            return new WorkspaceInviteResponse("already_member", toDto(existed, user));
        }

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

        return new WorkspaceInviteResponse("invited", toDto(wm, user));
    }

    @Override
    @Transactional
    public void changeMemberRole(String workspaceId, String userId, int newRoleId) {
        WorkspaceMember wm = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new NoSuchElementException("Member không tồn tại trong workspace"));
        wm.setRoleId(newRoleId);
        // updated_at nếu cần, DB hoặc entity khác xử lý
    }

    @Override
    @Transactional
    public void removeMember(String workspaceId, String userId) {
        WorkspaceMember wm = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new NoSuchElementException("Member không tồn tại trong workspace"));

        if (wm.isOwner() && workspaceMemberRepo.countOwners(workspaceId) <= 1) {
            throw new IllegalStateException("Không thể xóa owner cuối cùng của workspace");
        }
        workspaceMemberRepo.deleteByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Override
    public Optional<WorkspaceMemberDto> getMembership(String workspaceId, String userId) {
        return workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(wm -> {
                    User u = userRepo.findById(wm.getUserId()).orElse(null);
                    return toDto(wm, u);
                });
    }

    @Override
    public List<WorkspaceMemberDto> listMembers(String workspaceId) {
        List<WorkspaceMember> members = workspaceMemberRepo.findAllByWorkspaceId(workspaceId);
        List<WorkspaceMemberDto> dtos = new ArrayList<>(members.size());
        for (WorkspaceMember wm : members) {
            User u = userRepo.findById(wm.getUserId()).orElse(null);
            dtos.add(toDto(wm, u));
        }
        return dtos;
    }

    private WorkspaceMemberDto toDto(WorkspaceMember wm, User u) {
        return WorkspaceMemberDto.builder()
                .id(wm.getId())
                .workspaceId(wm.getWorkspaceId())
                .userId(wm.getUserId())
                .roleId(wm.getRoleId())
                .isOwner(wm.isOwner())
                .userEmail(u != null ? u.getEmail() : null)
                .userDisplayName(u != null ? u.getDisplayName() : null)
                .invitedBy(wm.getInvitedBy())
                .invitedEmail(wm.getInvitedEmail())
                .joinedAt(wm.getJoinedAt())
                .build();
    }
}
