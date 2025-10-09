package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.AddWorkspaceMemberReq;
import com.springboot.demo.dto.workspace.AssignWorkspaceRoleReq;
import com.springboot.demo.dto.workspace.WorkspaceMemberResponse;

import java.util.List;

public interface WorkspaceMemberService {
    List<WorkspaceMemberResponse> list(String workspaceId);
    String addMember(String workspaceId, AddWorkspaceMemberReq req, String actorId);
    String assignRole(String workspaceId, String targetUserId, AssignWorkspaceRoleReq req, String actorId);
    String transferOwnership(String workspaceId, String toUserId, String actorId);
}



