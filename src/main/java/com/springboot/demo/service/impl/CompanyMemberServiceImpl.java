package com.springboot.demo.service.impl;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.Scope;
import com.springboot.demo.repository.CompanyMemberRepository;
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
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));
        mustHaveManageUsersPermission(actorId, companyId);

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        if (role.getScope() != Scope.company) throw new IllegalArgumentException("Role không thuộc scope company");

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
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));
        mustHaveManageUsersPermission(actorId, companyId);

        var target = cmRepo.findByCompanyIdAndUserId(companyId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        if (Boolean.TRUE.equals(req.makeOwner())) {
            if (!actor.isOwner()) throw new AccessDeniedException("Chỉ Owner được phép chuyển quyền sở hữu");
            cmRepo.clearOwners(companyId);
            cmRepo.setOwner(companyId, targetUserId);
            return;
        }

        if (req.roleId() == null) throw new IllegalArgumentException("roleId required");

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        if (role.getScope() != Scope.company) throw new IllegalArgumentException("Role không thuộc scope company");

        if (target.isOwner()) throw new AccessDeniedException("Không thể đổi role cho Owner (dùng transfer-ownership)");

        target.setRoleId(role.getId());
        cmRepo.save(target);
    }

    private void mustHaveManageUsersPermission(String actorId, String companyId) {
        if (!ability.hasPermission(actorId, Scope.company, companyId, "company:set_user_permissions")) {
            throw new AccessDeniedException("Thiếu quyền company:set_user_permissions");
        }
    }
}
