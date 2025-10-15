package com.springboot.demo.service.impl;

import com.springboot.demo.dto.workspace.WorkspaceMemberCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceMemberDto;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.User;
import com.springboot.demo.model.WorkspaceMember;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.repository.WorkspaceMemberRepository;
import com.springboot.demo.repository.WorkspaceRepository;
import com.springboot.demo.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public WorkspaceMemberDto addMember(String workspaceId, WorkspaceMemberCreateRequest request) {
        // Validate workspace exists
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new IllegalArgumentException("Workspace with ID " + workspaceId + " does not exist");
        }

        // Validate user exists
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + request.userId() + " does not exist"));

        // Validate role exists
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Role with ID " + request.roleId() + " does not exist"));

        // Check if user is already a member
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, request.userId())) {
            throw new IllegalArgumentException("User is already a member of this workspace");
        }

        // Create workspace member
        WorkspaceMember member = new WorkspaceMember();
        member.setId(UUID.randomUUID().toString());
        member.setWorkspaceId(workspaceId);
        member.setUserId(request.userId());
        member.setRoleId(request.roleId());
        member.setOwner(false); // New members are not owners

        WorkspaceMember savedMember = workspaceMemberRepository.save(member);

        return WorkspaceMemberDto.builder()
                .id(savedMember.getId())
                .workspaceId(savedMember.getWorkspaceId())
                .userId(savedMember.getUserId())
                .roleId(savedMember.getRoleId())
                .isOwner(savedMember.isOwner())
                .userEmail(user.getEmail())
                .userDisplayName(user.getDisplayName())
                .roleName(role.getName())
                .build();
    }

    @Override
    public WorkspaceMemberDto updateMemberRole(String workspaceId, String userId, Integer roleId) {
        // Validate role exists
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role with ID " + roleId + " does not exist"));

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this workspace"));

        member.setRoleId(roleId);
        WorkspaceMember savedMember = workspaceMemberRepository.save(member);

        // Get user info
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " does not exist"));

        return WorkspaceMemberDto.builder()
                .id(savedMember.getId())
                .workspaceId(savedMember.getWorkspaceId())
                .userId(savedMember.getUserId())
                .roleId(savedMember.getRoleId())
                .isOwner(savedMember.isOwner())
                .userEmail(user.getEmail())
                .userDisplayName(user.getDisplayName())
                .roleName(role.getName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberDto> getWorkspaceMembers(String workspaceId) {
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceId);
        
        return members.stream().map(member -> {
            User user = userRepository.findById(member.getUserId()).orElse(null);
            Role role = roleRepository.findById(member.getRoleId()).orElse(null);
            
            return WorkspaceMemberDto.builder()
                    .id(member.getId())
                    .workspaceId(member.getWorkspaceId())
                    .userId(member.getUserId())
                    .roleId(member.getRoleId())
                    .isOwner(member.isOwner())
                    .userEmail(user != null ? user.getEmail() : null)
                    .userDisplayName(user != null ? user.getDisplayName() : null)
                    .roleName(role != null ? role.getName() : null)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceMemberDto getMember(String workspaceId, String userId) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this workspace"));

        User user = userRepository.findById(userId).orElse(null);
        Role role = roleRepository.findById(member.getRoleId()).orElse(null);

        return WorkspaceMemberDto.builder()
                .id(member.getId())
                .workspaceId(member.getWorkspaceId())
                .userId(member.getUserId())
                .roleId(member.getRoleId())
                .isOwner(member.isOwner())
                .userEmail(user != null ? user.getEmail() : null)
                .userDisplayName(user != null ? user.getDisplayName() : null)
                .roleName(role != null ? role.getName() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(String workspaceId, String userId) {
        return workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserWorkspaces(String userId) {
        return workspaceMemberRepository.findWorkspaceIdsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(String workspaceId, String userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(WorkspaceMember::isOwner)
                .orElse(false);
    }

    @Override
    public void removeMember(String workspaceId, String userId) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this workspace"));

        // Prevent removing the owner
        if (member.isOwner()) {
            throw new IllegalArgumentException("Cannot remove the workspace owner");
        }

        workspaceMemberRepository.delete(member);
    }
}