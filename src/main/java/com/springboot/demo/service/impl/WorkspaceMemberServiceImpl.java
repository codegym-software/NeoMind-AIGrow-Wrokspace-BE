package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.WorkspaceInviteRequest;
import com.springboot.demo.dto.workspace.WorkspaceMemberCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceMemberDto;
import com.springboot.demo.model.User;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.repository.WorkspaceMemberRepository;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.service.WorkspaceMemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WorkspaceMemberDto invite(String workspaceId, WorkspaceInviteRequest req, String inviterUserId) {
        if (req == null || req.roleId() == null) {
            throw new IllegalArgumentException("roleId is required");
        }
        if (req.email() == null && (req.userId() == null || req.userId().isBlank())) {
            throw new IllegalArgumentException("Either email or userId is required");
        }

        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
        String companyId = ws.getCompanyId();

        String targetUserId = resolveUserId(req);
        if (!companyMemberRepository.existsByCompanyIdAndUserId(companyId, targetUserId)) {
            throw new IllegalStateException("USER_NOT_IN_COMPANY");
        }
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, targetUserId)) {
            throw new IllegalStateException("ALREADY_IN_WORKSPACE");
        }

        WorkspaceMember wm = new WorkspaceMember();
        wm.setWorkspaceId(workspaceId);
        wm.setUserId(targetUserId);
        wm.setRoleId(req.roleId());
        wm.setOwner(false);
        WorkspaceMember saved = workspaceMemberRepository.save(wm);

        return toDto(saved);
    }

    @Override
    @Transactional
    public WorkspaceMemberDto addMember(String workspaceId, WorkspaceMemberCreateRequest req) {
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
        String companyId = ws.getCompanyId();

        if (!companyMemberRepository.existsByCompanyIdAndUserId(companyId, req.userId())) {
            throw new IllegalStateException("USER_NOT_IN_COMPANY");
        }
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, req.userId())) {
            throw new IllegalStateException("ALREADY_IN_WORKSPACE");
        }

        WorkspaceMember wm = new WorkspaceMember();
        wm.setWorkspaceId(workspaceId);
        wm.setUserId(req.userId());
        wm.setRoleId(req.roleId());
        wm.setOwner(false);
        WorkspaceMember saved = workspaceMemberRepository.save(wm);

        return toDto(saved);
    }

    @Override
    @Transactional
    public WorkspaceMemberDto updateMemberRole(String workspaceId, String userId, Integer roleId) {
        WorkspaceMember wm = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace member not found"));
        if (wm.isOwner()) {
            throw new IllegalStateException("CANNOT_UPDATE_OWNER_ROLE");
        }
        wm.setRoleId(roleId);
        WorkspaceMember saved = workspaceMemberRepository.save(wm);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void removeMember(String workspaceId, String userId) {
        WorkspaceMember wm = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace member not found"));
        if (wm.isOwner()) {
            throw new IllegalStateException("CANNOT_REMOVE_OWNER");
        }
        workspaceMemberRepository.delete(wm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberDto> getWorkspaceMembers(String workspaceId) {
        return workspaceMemberRepository.listMembers(workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(String workspaceId, String userId) {
        return workspaceMemberRepository.isOwner(workspaceId, userId);
    }

    // ===== helpers =====

    private String resolveUserId(WorkspaceInviteRequest req) {
        if (req.userId() != null && !req.userId().isBlank()) {
            return req.userId();
        }
        User u = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found by email: " + req.email()));
        return u.getId();
    }

    private WorkspaceMemberDto toDto(WorkspaceMember wm) {
        // Tuỳ dự án, bạn có thể join User/Role để map thêm userEmail, roleName...
        return WorkspaceMemberDto.builder()
                .id(wm.getId())
                .workspaceId(wm.getWorkspaceId())
                .userId(wm.getUserId())
                .roleId(wm.getRoleId())
                .isOwner(wm.isOwner())
                .build();
    }
}
