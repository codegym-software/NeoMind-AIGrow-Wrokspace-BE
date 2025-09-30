package com.springboot.demo.repository;

import com.springboot.demo.model.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {
    boolean existsByCompanyIdAndIsOwnerTrue(String companyId);
    boolean existsByCompanyIdAndUserId(String companyId, String userId);
    java.util.Optional<CompanyMember> findByCompanyIdAndUserId(String companyId, String userId);

    @Query("select cm.companyId from CompanyMember cm where cm.userId = :userId")
    List<String> findCompanyIdsByUserId(@Param("userId") String userId);

    void deleteByCompanyId(String companyId);

    List<CompanyMember> findByCompanyId(String companyId);

    boolean existsByCompanyIdAndInvitedEmail(String companyId, String invitedEmail);

    boolean existsByCompanyIdAndInvitedEmailIgnoreCase(String companyId, String invitedEmail);

    java.util.Optional<CompanyMember> findFirstByCompanyIdAndInvitedEmailIgnoreCase(String companyId, String invitedEmail);

    java.util.List<CompanyMember> findByInvitedEmailIgnoreCaseAndUserIdIsNull(String invitedEmail);
}