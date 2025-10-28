package com.springboot.demo.repository;

import com.springboot.demo.model.Project;
import com.springboot.demo.model.enums.ProjectStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findByWorkspaceIdAndStatusOrderByCreatedAtDesc(String workspaceId, ProjectStatus status);

    List<Project> findByWorkspaceIdOrderByCreatedAtDesc(String workspaceId);

    @Query("select p from Project p where p.id in (select pm.projectId from ProjectMember pm where pm.userId = :userId)")
    List<Project> findAllByMember(@Param("userId") String userId);

    @Query("select p from Project p where p.workspaceId = :workspaceId " +
            "and p.id in (select pm.projectId from ProjectMember pm where pm.userId = :userId)")
    List<Project> findAllByMemberInWorkspace(@Param("userId") String userId, @Param("workspaceId") String workspaceId);

    @Query("select p.workspaceId from Project p where p.id = :projectId")
    Optional<String> findWorkspaceIdByProjectId(@Param("projectId") String projectId);

    @Query("select p from Project p where p.workspaceId in (select w.id from Workspace w where w.companyId = :companyId) " +
            "and p.id in (select pm.projectId from ProjectMember pm where pm.userId = :userId)")
    List<Project> findAllByMemberInCompany(@Param("userId") String userId, @Param("companyId") String companyId);
}
