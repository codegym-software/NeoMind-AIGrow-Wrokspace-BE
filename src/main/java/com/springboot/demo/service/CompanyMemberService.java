package com.springboot.demo.service;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.dto.company.InviteResponse;
import com.springboot.demo.model.CompanyMember;

import java.util.List;

public interface CompanyMemberService {
    List<CompanyMember> list(String companyId);
    void addMember(String companyId, AddCompanyMemberReq req, String actorId);
    void assignRole(String companyId, String targetUserId, AssignCompanyRoleReq req, String actorId);
    void removeMember(String companyId, String memberUserId, String name);
    void transferOwnership(String companyId, String toUserId, Integer downgradeRoleId, String actorId);
    InviteResponse inviteMemberByEmail(String companyId, String inviterEmail, String invitedEmail, Integer roleId);
}
