package com.springboot.demo.repository;

import com.springboot.demo.model.ProjectMember;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {

    boolean existsByProjectIdAndUserId(String projectId, String userId);
    boolean existsByProjectIdAndUserIdAndOwnerIsTrue(String projectId, String userId);

    @Query("select pm from ProjectMember pm where pm.projectId = :projectId and pm.userId = :userId")
    Optional<ProjectMember> findOneByProjectIdAndUserId(@Param("projectId") String projectId,
                                                        @Param("userId") String userId);

    List<ProjectMember> findAllByUserId(String userId);

    @Query("""
        select pm from ProjectMember pm
        join Project p on p.id = pm.projectId
        where pm.userId = :userId and p.workspaceId = :workspaceId
    """)
    List<ProjectMember> findByUserIdInWorkspace(@Param("userId") String userId,
                                                @Param("workspaceId") String workspaceId);
}
