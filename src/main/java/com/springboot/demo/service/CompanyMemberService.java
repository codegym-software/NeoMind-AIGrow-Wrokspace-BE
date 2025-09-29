package com.springboot.demo.service;
import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.model.CompanyMember;
import java.util.List;

public interface CompanyMemberService {
    List<CompanyMember> list(String companyId);
    void addMember(String companyId, AddCompanyMemberReq req, String actorId);
    void assignRole(String companyId, String targetUserId, AssignCompanyRoleReq req, String actorId);
}
