package com.springboot.demo.service.impl;

import com.springboot.demo.dto.company.AddCompanyMemberReq;
import com.springboot.demo.dto.company.AssignCompanyRoleReq;
import com.springboot.demo.dto.company.InviteResponse;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.User;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.MembershipCleanupRepository;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.service.AbilityService;
import com.springboot.demo.service.CompanyMemberService;
import com.springboot.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyMemberServiceImpl implements CompanyMemberService {

    private final CompanyMemberRepository cmRepo;
    private final RoleRepository roleRepo;
    private final AbilityService ability;
    private final MembershipCleanupRepository membershipCleanup;
    private final UserRepository userRepo;
    private final EmailService emailService;

    @Value("${app.defaults.roleId:201}")
    private Integer defaultRoleId;

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

        var target = cmRepo.findByCompanyIdAndUserId(companyId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        if (target.isOwner()) {
            throw new AccessDeniedException("Không thể đổi role cho Owner (dùng transfer-ownership)");
        }
        if (req.roleId() == null) {
            throw new IllegalArgumentException("roleId required");
        }
        Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NoSuchElementException("Role không tồn tại"));
        if (role.getScope() != Scope.company) {
            throw new IllegalArgumentException("Role không thuộc scope company");
        }

        target.setRoleId(role.getId());
        cmRepo.save(target);

    }


    @Override
    @Transactional
    public void removeMember(String companyId, String targetUserId, String actorId) {
        cmRepo.findByCompanyIdAndUserId(companyId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));

        var target = cmRepo.findByCompanyIdAndUserId(companyId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên"));

        if (Boolean.TRUE.equals(target.isOwner())) {
            long owners = cmRepo.countOwners(companyId);
            if (owners <= 1) {
                throw new AccessDeniedException("Không thể xóa Owner duy nhất. Hãy chuyển quyền sở hữu trước.");
            }
        }

        membershipCleanup.deleteProjectMembershipsByCompanyForUser(companyId, targetUserId);
        membershipCleanup.deleteWorkspaceMembershipsByCompanyForUser(companyId, targetUserId);

        cmRepo.deleteByCompanyIdAndUserId(companyId, targetUserId);
    }

    @Override
    @Transactional
    public void transferOwnership(String companyId, String toUserId, Integer downgradeRoleId, String actorId) {
        if (toUserId == null || toUserId.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toUserId required");
        if (downgradeRoleId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "downgradeRoleId required");

        var actor = cmRepo.findByCompanyIdAndUserId(companyId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));
        if (!Boolean.TRUE.equals(actor.isOwner()))
            throw new AccessDeniedException("Chỉ Owner mới được chuyển quyền sở hữu");

        var target = cmRepo.findByCompanyIdAndUserId(companyId, toUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thành viên được chuyển"));

        Role downgradeRole = roleRepo.findById(downgradeRoleId)
                .orElseThrow(() -> new NoSuchElementException("Role downgrade không tồn tại"));
        if (downgradeRole.getScope() != Scope.company)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role downgrade phải thuộc scope=company");
        if (downgradeRole.getTargetId() != null && !companyId.equals(downgradeRole.getTargetId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role downgrade không thuộc company này");

        if (actor.getUserId().equals(target.getUserId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner hiện tại đã là Owner");

        actor.setRoleId(downgradeRole.getId());
        actor.setOwner(false);
        cmRepo.save(actor);

        target.setOwner(true);
        target.setRoleId(null);
        cmRepo.save(target);
    }

    @Override
    @Transactional
    public InviteResponse inviteMemberByEmail(String companyId, String inviterEmail, String invitedEmail, Integer roleId) {
        var inviterUser = userRepo.findByEmail(inviterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Please login first"));
        var inviter = cmRepo.findByCompanyIdAndUserId(companyId, inviterUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc công ty này"));

        String normalized = invitedEmail == null ? null : invitedEmail.trim().toLowerCase();
        if (normalized == null || normalized.isBlank()) throw new IllegalArgumentException("Email không hợp lệ");

        Optional<User> invitedUserOpt = userRepo.findByEmail(normalized);
        if (invitedUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Email chưa đăng ký trong hệ thống");
        }

        String targetUserId = invitedUserOpt.get().getId();

        var existing = cmRepo.findByCompanyIdAndUserId(companyId, targetUserId);
        if (existing.isPresent()) {
            return new InviteResponse("already_member", existing.get());
        }

        var existingInvite = cmRepo.findFirstByCompanyIdAndInvitedEmailIgnoreCase(companyId, normalized);
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

        CompanyMember saved = cmRepo.save(member);

        try {
            emailService.sendInvitationEmail(normalized, "Company", inviterEmail);
        } catch (Exception ignore) {}

        return new InviteResponse("created", saved);
    }
}
