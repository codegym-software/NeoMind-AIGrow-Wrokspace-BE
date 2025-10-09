package com.springboot.demo.service.impl;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.dto.company.InviteResponse;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.MembershipCleanupRepository;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.service.AbilityService;
import com.springboot.demo.service.CompanyMemberService;
import com.springboot.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyMemberServiceImpl implements CompanyMemberService {

    private final CompanyMemberRepository companyMemberRepo;
    private final RoleRepository roleRepo;
    private final AbilityService ability;

    private final UserRepository userRepository;
    private final EmailService emailService;
  
    @Value("${app.defaults.roleId:201}")
    private Long defaultRoleId;
    private final MembershipCleanupRepository membershipCleanup;


    @Override
    @Transactional(readOnly = true)
    public List<CompanyMember> list(String companyId) {
        return companyMemberRepo.findAllByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void addMember(String companyId, AddCompanyMemberReq req, String actorId) {
        if (req.userId() == null || req.userId().isBlank()) throw new IllegalArgumentException("userId required");
        if (req.roleId() == null) throw new IllegalArgumentException("roleId required");

        var actor = companyMemberRepo.findByCompanyIdAndUserId(companyId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));
        mustHaveManageUsersPermission(actorId, companyId);

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        if (role.getScope() != Scope.company) throw new IllegalArgumentException("Role không thuộc scope company");

        CompanyMember m = new CompanyMember();
        m.setCompanyId(companyId);
        m.setUserId(req.userId());
        m.setOwner(false);
        m.setRoleId(role.getId().longValue());

        companyMemberRepo.save(m);
    }

    @Override
    @Transactional
    public void assignRole(String companyId, String targetUserId, AssignCompanyRoleReq req, String actorId) {
        var actor = companyMemberRepo.findByCompanyIdAndUserId(companyId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));
        mustHaveManageUsersPermission(actorId, companyId);

        var target = companyMemberRepo.findByCompanyIdAndUserId(companyId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        if (Boolean.TRUE.equals(req.makeOwner())) {
            if (!actor.isOwner()) throw new AccessDeniedException("Chỉ Owner được phép chuyển quyền sở hữu");
            companyMemberRepo.clearOwners(companyId);
            companyMemberRepo.setOwner(companyId, targetUserId);
            return;
        }

        if (req.roleId() == null) throw new IllegalArgumentException("roleId required");

        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        if (role.getScope() != Scope.company) throw new IllegalArgumentException("Role không thuộc scope company");

        if (target.isOwner()) throw new AccessDeniedException("Không thể đổi role cho Owner (dùng transfer-ownership)");

        target.setRoleId(role.getId().longValue());
        companyMemberRepo.save(target);
    }

    @Override
    @Transactional
    public InviteResponse inviteMemberByEmail(String companyId, String inviterEmail, String invitedEmail, Long roleId) {
        var inviterUser = userRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Please login first"));
        var inviter = companyMemberRepo.findByCompanyIdAndUserId(companyId, inviterUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));

        String normalized = invitedEmail == null ? null : invitedEmail.trim().toLowerCase();
        if (normalized == null || normalized.isBlank()) throw new IllegalArgumentException("Email không hợp lệ");

        Optional<com.springboot.demo.model.User> invitedUserOpt = userRepository.findByEmail(normalized);
        if (invitedUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Email chưa đăng ký trong hệ thống");
        }

        String targetUserId = invitedUserOpt.get().getId();

        var existing = companyMemberRepo.findByCompanyIdAndUserId(companyId, targetUserId);
        if (existing.isPresent()) {
            return new InviteResponse("already_member", existing.get());
        }

        // Nếu đã có lời mời trước theo email (case-insensitive), trả existing_invite
        var existingInvite = companyMemberRepo.findFirstByCompanyIdAndInvitedEmailIgnoreCase(companyId, normalized);
        if (existingInvite.isPresent()) {
            return new InviteResponse("existing_invite", existingInvite.get());
        }

        CompanyMember member = new CompanyMember();
        member.setCompanyId(companyId);
        member.setUserId(targetUserId);
        member.setInvitedEmail(normalized);
        member.setInvitedBy(inviter.getUserId());
        member.setRoleId(roleId != null ? roleId : defaultRoleId);
        member.setOwner(false);

        CompanyMember saved = companyMemberRepo.save(member);

        try {
            emailService.sendInvitationEmail(normalized, "Company", inviterEmail);
        } catch (Exception ignore) {}

        return new InviteResponse("created", saved);
    }

    private void mustHaveManageUsersPermission(String actorId, String companyId) {
        if (!ability.hasPermission(actorId, Scope.company, companyId, "company:set_user_permissions")) {
            throw new AccessDeniedException("Thiếu quyền company:set_user_permissions");
        }
    }

    @Override
@Transactional
public void removeMember(String companyId, String targetUserId, String actorId) {
    companyMemberRepo.findByCompanyIdAndUserId(companyId, actorId)
            .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));

    var target = companyMemberRepo.findByCompanyIdAndUserId(companyId, targetUserId)
            .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

    if (Boolean.TRUE.equals(target.isOwner())) {
        long owners = companyMemberRepo.countOwners(companyId);
        if (owners <= 1) {
            throw new AccessDeniedException("Không thể xóa Owner duy nhất. Hãy chuyển quyền sở hữu trước.");
        }
    }

    membershipCleanup.deleteProjectMembershipsByCompanyForUser(companyId, targetUserId);
    membershipCleanup.deleteWorkspaceMembershipsByCompanyForUser(companyId, targetUserId);

    companyMemberRepo.deleteByCompanyIdAndUserId(companyId, targetUserId);
}


}
