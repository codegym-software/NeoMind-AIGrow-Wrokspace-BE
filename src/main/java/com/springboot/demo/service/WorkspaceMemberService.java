package com.springboot.demo.service;

import com.springboot.demo.dto.workspace.AddWorkspaceMemberReq;
import com.springboot.demo.dto.workspace.AssignWorkspaceRoleReq;
import com.springboot.demo.dto.workspace.TransferWorkspaceOwnershipReq;
import com.springboot.demo.model.WorkspaceMember;

import java.util.List;

public interface WorkspaceMemberService {
    /**
     * Liệt kê tất cả member trong workspace
     */
    List<WorkspaceMember> listMembers(String workspaceId);

    /**
     * Thêm member vào workspace (chỉ thêm user đã là member trong company)
     */
    void addMember(String workspaceId, AddWorkspaceMemberReq req, String actorId);

    /**
     * Cập nhật role cho member
     */
    void assignRole(String workspaceId, String targetUserId, AssignWorkspaceRoleReq req, String actorId);

    /**
     * Xóa member khỏi workspace
     */
    void removeMember(String workspaceId, String targetUserId, String actorId);

    /**
     * Chuyển quyền sở hữu workspace (chỉ Owner mới được)
     */
    void transferOwnership(String workspaceId, TransferWorkspaceOwnershipReq req, String actorId);
}


