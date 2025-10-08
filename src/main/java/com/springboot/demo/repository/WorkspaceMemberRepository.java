package com.springboot.demo.repository;

import com.springboot.demo.model.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, String> {

    List<WorkspaceMember> findAllByWorkspaceId(String workspaceId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);

    // Check if user is already a member of workspace
    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId AND wm.isOwner = true")
    Optional<WorkspaceMember> findOwnerByWorkspaceId(@Param("workspaceId") String workspaceId);

    @Query("SELECT COUNT(wm) > 0 FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId AND wm.userId = :userId AND wm.isOwner = true")
    boolean isOwner(@Param("workspaceId") String workspaceId, @Param("userId") String userId);
    // Find workspace member by workspace and user
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);

    @Modifying
    @Query("UPDATE WorkspaceMember wm SET wm.isOwner = false WHERE wm.workspaceId = :workspaceId")
    void clearOwners(@Param("workspaceId") String workspaceId);
    // Find all members of a workspace
    List<WorkspaceMember> findByWorkspaceId(String workspaceId);

    @Modifying
    @Query("UPDATE WorkspaceMember wm SET wm.isOwner = true WHERE wm.workspaceId = :workspaceId AND wm.userId = :userId")
    void setOwner(@Param("workspaceId") String workspaceId, @Param("userId") String userId);
    // Find all workspaces where user is a member
    @Query("SELECT wm.workspaceId FROM WorkspaceMember wm WHERE wm.userId = :userId")
    List<String> findWorkspaceIdsByUserId(@Param("userId") String userId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.userId = :userId")
    List<WorkspaceMember> findAllByUserId(@Param("userId") String userId);
    // Check if user has specific role in workspace
    @Query("SELECT COUNT(wm) > 0 FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId AND wm.userId = :userId AND wm.roleId = :roleId")
    boolean existsByWorkspaceIdAndUserIdAndRoleId(@Param("workspaceId") String workspaceId,
                                                  @Param("userId") String userId,
                                                  @Param("roleId") Long roleId);
}



