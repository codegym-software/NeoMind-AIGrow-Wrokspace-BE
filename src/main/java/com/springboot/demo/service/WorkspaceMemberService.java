package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.WorkspaceMemberCreateRequest;
import com.springboot.demo.dto.workspace.WorkspaceMemberDto;

import java.util.List;

public interface WorkspaceMemberService {
    
    // Add member to workspace
    WorkspaceMemberDto addMember(String workspaceId, WorkspaceMemberCreateRequest request);
    
    // Remove member from workspace
    void removeMember(String workspaceId, String userId);
    
    // Update member role
    WorkspaceMemberDto updateMemberRole(String workspaceId, String userId, Integer roleId);
    
    // Get all members of a workspace
    List<WorkspaceMemberDto> getWorkspaceMembers(String workspaceId);
    
    // Get member by workspace and user
    WorkspaceMemberDto getMember(String workspaceId, String userId);
    
    // Check if user is member of workspace
    boolean isMember(String workspaceId, String userId);
    
    // Get user's workspaces
    List<String> getUserWorkspaces(String userId);
    
    // Check if user is owner of workspace
    boolean isOwner(String workspaceId, String userId);
}