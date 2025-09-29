package com.springboot.demo.repository;

import com.springboot.demo.model.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {

    boolean existsByCompanyIdAndIsOwnerTrue(String companyId);

    boolean existsByCompanyIdAndUserId(String companyId, String userId);

    Optional<CompanyMember> findByCompanyIdAndUserId(String companyId, String userId);

    @Query("select cm.companyId from CompanyMember cm where cm.userId = :userId")
    List<String> findCompanyIdsByUserId(@Param("userId") String userId);

    void deleteByCompanyId(String companyId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CompanyMember m SET m.isOwner=false WHERE m.companyId=:companyId")
    void clearOwners(@Param("companyId") String companyId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CompanyMember m SET m.isOwner=true WHERE m.companyId=:companyId AND m.userId=:userId")
    int setOwner(@Param("companyId") String companyId, @Param("userId") String userId);

    @Query("SELECT m FROM CompanyMember m WHERE m.companyId = :companyId")
    List<CompanyMember> findAllByCompanyId(@Param("companyId") String companyId);
}
