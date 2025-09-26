package com.springboot.demo.companymember;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {
    boolean existsByCompanyIdAndIsOwnerTrue(String companyId);
    boolean existsByCompanyIdAndUserId(String companyId, String userId);
    java.util.Optional<CompanyMember> findByCompanyIdAndUserId(String companyId, String userId);
}
