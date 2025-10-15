package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.WorkspaceInviteResponse;
import com.springboot.demo.dto.workspace.AddWorkspaceMemberReq;
import com.springboot.demo.dto.workspace.AssignWorkspaceRoleReq;
import com.springboot.demo.model.WorkspaceMember;

import java.util.List;


public interface WorkspaceMemberService {
    List<WorkspaceMember> list(String workspaceId);
    void addMember(String workspaceId, AddWorkspaceMemberReq req, String actorId);
    void assignRole(String workspaceId, String targetUserId, AssignWorkspaceRoleReq req, String actorId);
    void removeMember(String workspaceId, String memberUserId, String name);
    void transferOwnership(String workspaceId, String toUserId, Integer downgradeRoleId, String actorId);
    WorkspaceInviteResponse inviteMemberByEmail(String workspaceId, String inviterEmail, String invitedEmail, Integer roleId);
}
