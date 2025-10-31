package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.AddWorkspaceMemberReq;
import com.springboot.demo.dto.workspace.AssignWorkspaceRoleReq;
import com.springboot.demo.dto.workspace.TransferWorkspaceOwnershipReq;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.repository.*;
import com.springboot.demo.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceMemberRepository wmRepo;
    private final WorkspaceRepository workspaceRepo;
    private final CompanyMemberRepository cmRepo;
    private final RoleRepository roleRepo;

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMember> listMembers(String workspaceId) {
        // Validate workspace exists
        workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace không tồn tại"));
        
        return wmRepo.findByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public void addMember(String workspaceId, AddWorkspaceMemberReq req, String actorId) {
        // Validate workspace exists and get companyId
        Workspace workspace = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace không tồn tại"));
        
        // Validate actor is member of workspace
        wmRepo.findByWorkspaceIdAndUserId(workspaceId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc workspace này"));

        // Validate user must be member of company
        cmRepo.findByCompanyIdAndUserId(workspace.getCompanyId(), req.userId())
                .orElseThrow(() -> new IllegalArgumentException("User phải là member của company trước"));

        // Check if user is already a member
        if (wmRepo.existsByWorkspaceIdAndUserId(workspaceId, req.userId())) {
            throw new IllegalArgumentException("User đã là member của workspace này");
        }

        // Validate role
        if (req.roleId() == null) {
            throw new IllegalArgumentException("roleId required");
        }
        
        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        
        if (role.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role phải thuộc scope workspace");
        }

        // Create workspace member
        WorkspaceMember member = new WorkspaceMember();
        member.setId(java.util.UUID.randomUUID().toString());
        member.setWorkspaceId(workspaceId);
        member.setUserId(req.userId());
        member.setRoleId(role.getId());
        member.setOwner(false);

        wmRepo.save(member);
    }

    @Override
    @Transactional
    public void assignRole(String workspaceId, String targetUserId, AssignWorkspaceRoleReq req, String actorId) {
        // Validate actor is member
        wmRepo.findByWorkspaceIdAndUserId(workspaceId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc workspace này"));

        // Get target member
        WorkspaceMember target = wmRepo.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        // Cannot change role of owner
        if (target.isOwner()) {
            throw new AccessDeniedException("Không thể đổi role cho Owner (dùng transfer-ownership)");
        }

        // Validate role
        if (req.roleId() == null) {
            throw new IllegalArgumentException("roleId required");
        }

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        
        if (role.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role phải thuộc scope workspace");
        }

        target.setRoleId(role.getId());
        wmRepo.save(target);
    }

    @Override
    @Transactional
    public void removeMember(String workspaceId, String targetUserId, String actorId) {
        // Validate actor is member
        wmRepo.findByWorkspaceIdAndUserId(workspaceId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc workspace này"));

        // Get target member
        WorkspaceMember target = wmRepo.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        // Cannot remove last owner
        if (target.isOwner()) {
            long owners = wmRepo.countOwners(workspaceId);
            if (owners <= 1) {
                throw new AccessDeniedException("Không thể xóa Owner duy nhất. Hãy chuyển quyền sở hữu trước.");
            }
        }

        wmRepo.delete(target);
    }

    @Override
    @Transactional
    public void transferOwnership(String workspaceId, TransferWorkspaceOwnershipReq req, String actorId) {
        if (req.toUserId() == null || req.toUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toUserId required");
        }
        if (req.downgradeRoleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "downgradeRoleId required");
        }

        // Validate actor is owner
        WorkspaceMember actor = wmRepo.findByWorkspaceIdAndUserId(workspaceId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc workspace này"));
        
        if (!actor.isOwner()) {
            throw new AccessDeniedException("Chỉ Owner mới được chuyển quyền sở hữu");
        }

        // Get target member
        WorkspaceMember target = wmRepo.findByWorkspaceIdAndUserId(workspaceId, req.toUserId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên được chuyển"));

        // Validate downgrade role
        Role downgradeRole = roleRepo.findById(req.downgradeRoleId())
                .orElseThrow(() -> new NoSuchElementException("Role downgrade không tồn tại"));
        
        if (downgradeRole.getScope() != Scope.workspace) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role downgrade phải thuộc scope workspace");
        }

        if (downgradeRole.getTargetId() != null && !workspaceId.equals(downgradeRole.getTargetId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role downgrade không thuộc workspace này");
        }

        // Check self-transfer
        if (actor.getUserId().equals(target.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner hiện tại đã là Owner");
        }

        // Transfer ownership
        actor.setRoleId(downgradeRole.getId());
        actor.setOwner(false);
        wmRepo.save(actor);

        target.setOwner(true);
        target.setRoleId(null);
        wmRepo.save(target);
    }
}

