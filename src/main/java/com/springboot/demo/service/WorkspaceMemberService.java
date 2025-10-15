package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.WorkspaceInviteRequest;
import com.springboot.demo.dto.workspace.WorkspaceInviteResponse;
import com.springboot.demo.dto.workspace.WorkspaceMemberDto;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberService {

    // Tạo owner khi create workspace
    void createOwnerMembership(String workspaceId, String ownerUserId, int ownerRoleId);

    // Invite = join ngay
    WorkspaceInviteResponse invite(String workspaceId, WorkspaceInviteRequest req, String inviterUserId);

    // Quản lý role
    void changeMemberRole(String workspaceId, String userId, int newRoleId);

    // Remove
    void removeMember(String workspaceId, String userId);

    // Query
    Optional<WorkspaceMemberDto> getMembership(String workspaceId, String userId);
    List<WorkspaceMemberDto> listMembers(String workspaceId);
}
