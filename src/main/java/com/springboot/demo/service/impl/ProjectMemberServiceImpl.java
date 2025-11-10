package com.springboot.demo.service.impl;

import com.springboot.demo.dto.project.AddProjectMemberReq;
import com.springboot.demo.dto.project.AssignProjectRoleReq;
import com.springboot.demo.dto.project.ProjectMemberDto;
import com.springboot.demo.dto.project.TransferProjectOwnershipReq;
import com.springboot.demo.model.Project;
import com.springboot.demo.model.ProjectMember;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.User;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.*;
import com.springboot.demo.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository pmRepo;
    private final ProjectRepository projectRepo;
    private final WorkspaceMemberRepository wmRepo;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMember> listMembers(String projectId) {
        projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project không tồn tại"));
        return pmRepo.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberDto> listMembersWithDetails(String projectId) {
        projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project không tồn tại"));
        
        List<ProjectMember> members = pmRepo.findByProjectId(projectId);
        
        // Get all user IDs
        List<String> userIds = members.stream()
                .map(ProjectMember::getUserId)
                .collect(Collectors.toList());
        
        // Fetch users in batch
        List<User> users = userRepo.findAllById(userIds);
        
        // Create userId -> User map
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        // Map to DTO with user details
        return members.stream()
                .map(member -> {
                    User user = userMap.get(member.getUserId());
                    return ProjectMemberDto.builder()
                            .id(member.getId())
                            .projectId(member.getProjectId())
                            .userId(member.getUserId())
                            .roleId(member.getRoleId())
                            .owner(member.isOwner())
                            .email(user != null ? user.getEmail() : null)
                            .displayName(user != null ? user.getDisplayName() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMember(String projectId, AddProjectMemberReq req, String actorId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project không tồn tại"));

        pmRepo.findByProjectIdAndUserId(projectId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc project này"));

        wmRepo.findByWorkspaceIdAndUserId(project.getWorkspaceId(), req.userId())
                .orElseThrow(() -> new IllegalArgumentException("User phải là member của workspace trước"));

        if (pmRepo.existsByProjectIdAndUserId(projectId, req.userId())) {
            throw new IllegalArgumentException("User đã là member của project này");
        }

        if (req.roleId() == null) {
            throw new IllegalArgumentException("roleId required");
        }

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));

        if (role.getScope() != Scope.project) {
            throw new IllegalArgumentException("Role phải thuộc scope project");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(req.userId());
        member.setRoleId(role.getId());
        member.setOwner(false);

        pmRepo.save(member);
    }

    @Override
    @Transactional
    public void assignRole(String projectId, String targetUserId, AssignProjectRoleReq req, String actorId) {
        pmRepo.findByProjectIdAndUserId(projectId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc project này"));

        ProjectMember target = pmRepo.findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        if (target.isOwner()) {
            throw new AccessDeniedException("Không thể đổi role cho Owner (dùng transfer-ownership)");
        }

        if (req.roleId() == null) {
            throw new IllegalArgumentException("roleId required");
        }

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));

        if (role.getScope() != Scope.project) {
            throw new IllegalArgumentException("Role phải thuộc scope project");
        }

        target.setRoleId(role.getId());
        pmRepo.save(target);
    }

    @Override
    @Transactional
    public void removeMember(String projectId, String targetUserId, String actorId) {
        pmRepo.findByProjectIdAndUserId(projectId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc project này"));

        ProjectMember target = pmRepo.findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        if (target.isOwner()) {
            long owners = pmRepo.countOwners(projectId);
            if (owners <= 1) {
                throw new AccessDeniedException("Không thể xóa Owner duy nhất. Hãy chuyển quyền sở hữu trước.");
            }
        }

        pmRepo.delete(target);
    }

    @Override
    @Transactional
    public void transferOwnership(String projectId, TransferProjectOwnershipReq req, String actorId) {
        if (req.toUserId() == null || req.toUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toUserId required");
        }
        if (req.downgradeRoleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "downgradeRoleId required");
        }

        ProjectMember actor = pmRepo.findByProjectIdAndUserId(projectId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc project này"));

        if (!actor.isOwner()) {
            throw new AccessDeniedException("Chỉ Owner mới được chuyển quyền sở hữu");
        }

        ProjectMember target = pmRepo.findByProjectIdAndUserId(projectId, req.toUserId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên được chuyển"));

        Role downgradeRole = roleRepo.findById(req.downgradeRoleId())
                .orElseThrow(() -> new NoSuchElementException("Role downgrade không tồn tại"));

        if (downgradeRole.getScope() != Scope.project) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role downgrade phải thuộc scope project");
        }

        if (downgradeRole.getTargetId() != null && !projectId.equals(downgradeRole.getTargetId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role downgrade không thuộc project này");
        }

        if (actor.getUserId().equals(target.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner hiện tại đã là Owner");
        }

        actor.setRoleId(downgradeRole.getId());
        actor.setOwner(false);
        pmRepo.save(actor);

        target.setOwner(true);
        target.setRoleId(null);
        pmRepo.save(target);
    }
}