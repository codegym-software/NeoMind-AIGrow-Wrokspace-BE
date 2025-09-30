package com.springboot.demo.service;

import com.springboot.demo.dto.company.InviteRequest;
import com.springboot.demo.dto.company.InviteResponse;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.User;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyMemberService {

    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public CompanyMemberService(CompanyMemberRepository companyMemberRepository,
                                UserRepository userRepository,
                                EmailService emailService) {
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public String getCompanyIdByUserId(String userId) {
        List<String> companyIds = companyMemberRepository.findCompanyIdsByUserId(userId);
        return companyIds.isEmpty() ? null : companyIds.get(0);
    }

    public InviteResponse inviteMember(String companyId, String inviterId, String invitedEmail, Integer roleId) {
        String normalizedEmail = invitedEmail == null ? null : invitedEmail.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }

        System.out.println("[INVITE] companyId=" + companyId + ", inviterId=" + inviterId + ", email=" + normalizedEmail);

        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent()) {
            String targetUserId = existingUser.get().getId();
            boolean alreadyMember = companyMemberRepository.existsByCompanyIdAndUserId(companyId, targetUserId);
            if (alreadyMember) {
                // Idempotent: trả về đúng bản ghi hiện có (có id), tránh trả object không có id
                CompanyMember existingMember = companyMemberRepository
                        .findByCompanyIdAndUserId(companyId, targetUserId)
                        .orElseGet(() -> {
                            CompanyMember m = new CompanyMember();
                            m.setCompanyId(companyId);
                            m.setUserId(targetUserId);
                            m.setInvitedEmail(normalizedEmail);
                            m.setInvitedBy(inviterId);
                            m.setRoleId(roleId != null ? roleId : 2);
                            m.setJoinedAt(null);
                            return m;
                        });
                System.out.println("[INVITE] Already a member, returning existing member id=" + existingMember.getId());
                return new InviteResponse("already_member", existingMember);
            }
        } else {
            // Idempotent: nếu đã có lời mời theo email, trả về lời mời hiện có
            var existingInvite = companyMemberRepository
                    .findFirstByCompanyIdAndInvitedEmailIgnoreCase(companyId, normalizedEmail);
            if (existingInvite.isPresent()) {
                System.out.println("[INVITE] Already invited, returning existing invitation id=" + existingInvite.get().getId());
                return new InviteResponse("existing_invite", existingInvite.get());
            }
        }

        CompanyMember member = new CompanyMember();
        member.setCompanyId(companyId);
        member.setInvitedEmail(normalizedEmail);
        member.setInvitedBy(inviterId);
        member.setRoleId(roleId != null ? roleId : 2);

        // 2) Không auto-join. Nếu user đã tồn tại, chỉ liên kết userId nhưng joinedAt để null, chờ accept/login
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            member.setUserId(user.getId());
        }

        member.setJoinedAt(null);

        CompanyMember saved = companyMemberRepository.save(member);
        System.out.println("[INVITE] Saved invitation id=" + saved.getId());

        // 3) Gửi email mời (best-effort)
        try {
            emailService.sendInvitationEmail(invitedEmail, "AI-Grow Company", "System");
        } catch (Exception ignored) {}

        return new InviteResponse("created", saved);
    }

    public List<CompanyMember> getMembers(String companyId) {
        return companyMemberRepository.findByCompanyId(companyId);
    }

    public List<CompanyMember> syncUserInvitations(String userEmail, String userId) {
        String normalized = userEmail == null ? null : userEmail.trim().toLowerCase();
        if (normalized == null || normalized.isBlank()) return List.of();
        var pending = companyMemberRepository.findByInvitedEmailIgnoreCaseAndUserIdIsNull(normalized);
        for (CompanyMember inv : pending) {
            inv.setUserId(userId);
            inv.setJoinedAt(LocalDateTime.now());
            companyMemberRepository.save(inv);
            System.out.println("[INVITE] synced invitation id=" + inv.getId() + " for user=" + userEmail);
        }
        return pending;
    }
}
