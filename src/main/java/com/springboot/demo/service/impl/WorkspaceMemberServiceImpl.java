package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.AddWorkspaceMemberReq;
import com.springboot.demo.dto.workspace.AssignWorkspaceRoleReq;
import com.springboot.demo.dto.workspace.WorkspaceMemberResponse;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.Scope;
import com.springboot.demo.model.User;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.repository.WorkspaceMemberRepository;
import com.springboot.demo.service.AbilityService;
import com.springboot.demo.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final AbilityService ability;

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> list(String workspaceId) {
        List<WorkspaceMember> members = workspaceMemberRepo.findAllByWorkspaceId(workspaceId);
        
        return members.stream()
                .map(member -> {
                    User user = userRepo.findById(member.getUserId())
                            .orElseThrow(() -> new NoSuchElementException("User không tồn tại"));
                    String roleName = member.getRoleId() != null ? 
                            roleRepo.findById(member.getRoleId().longValue())
                                    .map(Role::getName)
                                    .orElse("Unknown Role") : "Owner";
                    
                    return WorkspaceMemberResponse.from(member, user, roleName);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String addMember(String workspaceId, AddWorkspaceMemberReq req, String actorId) {
        if (req.email() == null || req.email().isBlank()) {
            throw new IllegalArgumentException("Email là bắt buộc");
        }
        if (req.roleId() == null) {
            throw new IllegalArgumentException("Role ID là bắt buộc");
        }

        // Kiểm tra quyền actor
        workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc workspace này"));
        mustHaveManageUsersPermission(actorId, workspaceId);

        // Tìm user theo email
        User user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy user với email: " + req.email()));

        // Kiểm tra user đã là member chưa
        if (workspaceMemberRepo.existsByWorkspaceIdAndUserId(workspaceId, user.getId())) {
            throw new IllegalArgumentException("User đã là thành viên của workspace này");
        }

        // Kiểm tra role
        Role role = roleRepo.findById(req.roleId().longValue())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        if (role.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role không thuộc scope workspace");
        }

        // Tạo member mới
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspaceId);
        member.setUserId(user.getId());
        member.setRoleId(role.getId());
        member.setOwner(false);
        
        workspaceMemberRepo.save(member);

        return "Đã thêm thành viên " + user.getDisplayName() + " vào workspace thành công";
    }

    @Override
    @Transactional
    public String assignRole(String workspaceId, String targetUserId, AssignWorkspaceRoleReq req, String actorId) {
        // Kiểm tra quyền actor
        var actor = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc workspace này"));
        mustHaveManageUsersPermission(actorId, workspaceId);

        // Tìm target member
        var target = workspaceMemberRepo.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        User targetUser = userRepo.findById(targetUserId)
                .orElseThrow(() -> new NoSuchElementException("User không tồn tại"));

        // Xử lý transfer ownership
        if (Boolean.TRUE.equals(req.makeOwner())) {
            if (!actor.isOwner()) {
                throw new AccessDeniedException("Chỉ Owner được phép chuyển quyền sở hữu");
            }
            workspaceMemberRepo.clearOwners(workspaceId);
            workspaceMemberRepo.setOwner(workspaceId, targetUserId);
            return "Đã chuyển quyền sở hữu workspace cho " + targetUser.getDisplayName() + " thành công";
        }

        // Xử lý assign role
        if (req.roleId() == null) {
            throw new IllegalArgumentException("Role ID là bắt buộc");
        }

        if (target.isOwner()) {
            throw new AccessDeniedException("Không thể đổi role cho Owner (dùng transfer-ownership)");
        }

        Role role = roleRepo.findById(req.roleId().longValue())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        if (role.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role không thuộc scope workspace");
        }

        target.setRoleId(role.getId());
        workspaceMemberRepo.save(target);

        return "Đã gán role " + role.getName() + " cho " + targetUser.getDisplayName() + " thành công";
    }

    @Override
    @Transactional
    public String transferOwnership(String workspaceId, String toUserId, String actorId) {
        return assignRole(workspaceId, toUserId, new AssignWorkspaceRoleReq(null, true), actorId);
    }

    private void mustHaveManageUsersPermission(String actorId, String workspaceId) {
        if (!ability.hasPermission(actorId, Scope.workspace, workspaceId, "workspace:set_user_permissions")) {
            throw new AccessDeniedException("Thiếu quyền workspace:set_user_permissions");
        }
    }
}
