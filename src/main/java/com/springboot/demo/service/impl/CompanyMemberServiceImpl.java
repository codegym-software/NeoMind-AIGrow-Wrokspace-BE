package com.springboot.demo.service.impl;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.Enum.Scope;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.MembershipCleanupRepository;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.service.AbilityService;
import com.springboot.demo.service.CompanyMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CompanyMemberServiceImpl implements CompanyMemberService {

    private final CompanyMemberRepository cmRepo;
    private final RoleRepository roleRepo;
    private final AbilityService ability;
    private final MembershipCleanupRepository membershipCleanup;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyMember> list(String companyId) {
        return cmRepo.findAllByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void addMember(String companyId, AddCompanyMemberReq req, String actorId) {
        if (req.userId() == null || req.userId().isBlank()) throw new IllegalArgumentException("userId required");
        if (req.roleId() == null) throw new IllegalArgumentException("roleId required");

        var actor = cmRepo.findByCompanyIdAndUserId(companyId, actorId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this company"));
        mustHaveManageUsersPermission(actorId, companyId);

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role not found"));
        if (role.getScope() != Scope.company) throw new IllegalArgumentException("Role does not belong to company scope");

        CompanyMember m = new CompanyMember();
        m.setCompanyId(companyId);
        m.setUserId(req.userId());
        m.setOwner(false);
        m.setRoleId(role.getId());
        cmRepo.save(m);
    }

    @Override
    @Transactional
    public void assignRole(String companyId, String targetUserId, AssignCompanyRoleReq req, String actorId) {
        var actor = cmRepo.findByCompanyIdAndUserId(companyId, actorId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this company"));
        mustHaveManageUsersPermission(actorId, companyId);

        var target = cmRepo.findByCompanyIdAndUserId(companyId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));

        if (Boolean.TRUE.equals(req.makeOwner())) {
            if (!actor.isOwner()) throw new AccessDeniedException("Only the owner can transfer ownership");
            cmRepo.clearOwners(companyId);
            cmRepo.setOwner(companyId, targetUserId);
            return;
        }

        if (req.roleId() == null) throw new IllegalArgumentException("roleId required");

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role not found"));
        if (role.getScope() != Scope.company) throw new IllegalArgumentException("Role does not belong to company scope");

        if (target.isOwner()) throw new AccessDeniedException("Cannot change role of the owner. Transfer ownership first.");

        target.setRoleId(role.getId());
        cmRepo.save(target);
    }

    private void mustHaveManageUsersPermission(String actorId, String companyId) {
        if (!ability.hasPermission(actorId, Scope.company, companyId, "company:set_user_permissions")) {
            throw new AccessDeniedException("You do not have permission to manage users in this company");
        }
    }

    @Override
    @Transactional
    public void removeMember(String companyId, String targetUserId, String actorId) {
        cmRepo.findByCompanyIdAndUserId(companyId, actorId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this company"));

        var target = cmRepo.findByCompanyIdAndUserId(companyId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));

        if (Boolean.TRUE.equals(target.isOwner())) {
            long owners = cmRepo.countOwners(companyId);
            if (owners <= 1) {
                throw new AccessDeniedException("Cannot remove the only owner of the company. Transfer ownership first.");
            }
        }

        membershipCleanup.deleteProjectMembershipsByCompanyForUser(companyId, targetUserId);
        membershipCleanup.deleteWorkspaceMembershipsByCompanyForUser(companyId, targetUserId);

        cmRepo.deleteByCompanyIdAndUserId(companyId, targetUserId);
    }

}
