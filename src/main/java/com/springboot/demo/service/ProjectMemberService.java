package com.springboot.demo.service;

import com.springboot.demo.dto.project.AddProjectMemberReq;
import com.springboot.demo.dto.project.AssignProjectRoleReq;
import com.springboot.demo.dto.project.ProjectMemberDto;
import com.springboot.demo.dto.project.TransferProjectOwnershipReq;
import com.springboot.demo.model.ProjectMember;

import java.util.List;

public interface ProjectMemberService {

    List<ProjectMember> listMembers(String projectId);
    
    List<ProjectMemberDto> listMembersWithDetails(String projectId);

    void addMember(String projectId, AddProjectMemberReq req, String actorId);

    void assignRole(String projectId, String targetUserId, AssignProjectRoleReq req, String actorId);

    void removeMember(String projectId, String targetUserId, String actorId);

    void transferOwnership(String projectId, TransferProjectOwnershipReq req, String actorId);
}