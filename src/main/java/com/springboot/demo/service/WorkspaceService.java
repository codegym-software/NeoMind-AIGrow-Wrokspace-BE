package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.*;
import java.util.List;

public interface WorkspaceService {
    WorkspaceDto createWorkspace(WorkspaceCreateRequest req, String currentUserId);
    WorkspaceDto getWorkspace(String id, String companyId);
    WorkspaceDto updateWorkspace(String id, String companyId, WorkspaceUpdateRequest req, String currentUserId);
    void deleteWorkspaceHard(String id, String companyId, String currentUserId);

    List<WorkspaceDto> listMyWorkspaces(String companyId, String userId);
    List<WorkspaceDto> listWorkspacesInvitedMe(String companyId, String userId);

    WorkspaceInviteResponse inviteMember(String workspaceId, WorkspaceInviteRequest req, String inviterUserId);
    List<WorkspaceMemberDto> listMembers(String workspaceId);

    WorkspaceAccessDto getMyAccessInWorkspace(String workspaceId, String userId);
}
