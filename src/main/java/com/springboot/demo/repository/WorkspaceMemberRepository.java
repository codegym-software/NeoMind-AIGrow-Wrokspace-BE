package com.springboot.demo.repository;

import com.springboot.demo.model.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.springboot.demo.model.Workspace;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, String> {

    // Check if user is already a member of workspace
    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);

    // Find workspace member by workspace and user
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);

    // Find all members of a workspace
    List<WorkspaceMember> findByWorkspaceId(String workspaceId);

    // Find all workspaces where user is a member
    @Query("SELECT wm.workspaceId FROM WorkspaceMember wm WHERE wm.userId = :userId")
    List<String> findWorkspaceIdsByUserId(@Param("userId") String userId);


    // Check if user has specific role in workspace
    @Query("SELECT COUNT(wm) > 0 FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId AND wm.userId = :userId AND wm.roleId = :roleId")
    boolean existsByWorkspaceIdAndUserIdAndRoleId(@Param("workspaceId") String workspaceId, 
                                                  @Param("userId") String userId, 
                                                  @Param("roleId") Long roleId);

    // Check if user is the owner of the workspace
    @Query("SELECT COUNT(wm) > 0 FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId AND wm.userId = :userId AND wm.isOwner = true")
    boolean isOwner(@Param("workspaceId") String workspaceId, @Param("userId") String userId);

    // Count owners of a workspace
    @Query("SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId AND wm.isOwner = true")
    long countOwners(@Param("workspaceId") String workspaceId);

    @Query(value = """
        SELECT w.*
        FROM workspaces w
        JOIN workspace_members wm ON wm.workspace_id = w.id
        WHERE w.company_id = :companyId
          AND wm.user_id = :userId
          AND (:includeArchived = TRUE OR w.archived_at IS NULL)
          AND (:q IS NULL OR :q = '' OR LOWER(w.name) LIKE LOWER(CONCAT('%', :q, '%')))
        """,
        countQuery = """
        SELECT count(w.id)
        FROM workspaces w
        JOIN workspace_members wm ON wm.workspace_id = w.id
        WHERE w.company_id = :companyId
          AND wm.user_id = :userId
          AND (:includeArchived = TRUE OR w.archived_at IS NULL)
          AND (:q IS NULL OR :q = '' OR LOWER(w.name) LIKE LOWER(CONCAT('%', :q, '%')))
        """,
        nativeQuery = true)
    Page<Workspace> findMyWorkspacesInCompany(
        @Param("companyId") String companyId,
        @Param("userId") String userId,
        @Param("q") String q,
        @Param("includeArchived") boolean includeArchived,
        Pageable pageable
    );
}