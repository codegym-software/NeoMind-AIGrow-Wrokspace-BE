package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.WorkspaceMemberDto;
import com.springboot.demo.dto.workspace.WorkspaceMemberInviteRequest;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.WorkspaceMember;

import java.util.List;

public interface WorkspaceMemberService {
    
    // Mời thành viên vào workspace
    WorkspaceMember inviteMember(String workspaceId, WorkspaceMemberInviteRequest req, String actorUserId);

    WorkspaceMember inviteMemberByEmail(String workspaceId, String inviterEmail, String invitedEmail, Integer roleId);

    WorkspaceMember updateMemberRole(String workspaceId, String userId, Integer newRoleId, String actorId);

    // Lấy danh sách thành viên của workspace
    List<WorkspaceMemberDto> getMembers(String workspaceId);

    void transferOwnership(String workspaceId, String toUserId, Integer downgradeRoleId, String actorId);

    // Xóa thành viên khỏi workspace
    void removeMember(String workspaceId, String userId);
    
    // Cập nhật role của thành viên
    WorkspaceMember updateMemberRole(String workspaceId, String userId, Integer roleId);
    
    // Lấy danh sách thành viên công ty có thể được mời vào workspace
    List<WorkspaceMemberDto> getAvailableMembers(String workspaceId);
    
    // Kiểm tra user có phải owner của workspace không
    boolean isOwner(String workspaceId, String userId);
    
    // Lấy danh sách workspace roles có sẵn
    List<Role> getAvailableWorkspaceRoles();
}