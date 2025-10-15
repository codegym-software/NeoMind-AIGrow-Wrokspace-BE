package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.WorkspaceInviteRequest;
import com.springboot.demo.dto.workspace.WorkspaceMemberCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceMemberDto;

import java.util.List;

public interface WorkspaceMemberService {
    WorkspaceMemberDto invite(String workspaceId, WorkspaceInviteRequest req, String inviterUserId);

    WorkspaceMemberDto addMember(String workspaceId, WorkspaceMemberCreateRequest req);

    WorkspaceMemberDto updateMemberRole(String workspaceId, String userId, Integer roleId);

    void removeMember(String workspaceId, String userId);

    List<WorkspaceMemberDto> getWorkspaceMembers(String workspaceId);

    boolean isOwner(String workspaceId, String userId);
}
