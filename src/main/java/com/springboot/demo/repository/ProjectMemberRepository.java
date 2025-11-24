package com.springboot.demo.repository;

import com.springboot.demo.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectIdAndUserId(String projectId, String userId);

    Optional<ProjectMember> findByProjectIdAndUserId(String projectId, String userId);

    List<ProjectMember> findByProjectId(String projectId);

    @Query("SELECT pm.projectId FROM ProjectMember pm WHERE pm.userId = :userId")
    List<String> findProjectIdsByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.userId = :userId AND pm.roleId = :roleId")
    boolean existsByProjectIdAndUserIdAndRoleId(@Param("projectId") String projectId,
                                                  @Param("userId") String userId,
                                                  @Param("roleId") Long roleId);

    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.userId = :userId AND pm.isOwner = true")
    boolean isOwner(@Param("projectId") String projectId, @Param("userId") String userId);

    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.isOwner = true")
    long countOwners(@Param("projectId") String projectId);
}
