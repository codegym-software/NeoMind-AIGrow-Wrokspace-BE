package com.springboot.demo.repository;

import com.springboot.demo.model.WorkspaceMember;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer> {
    boolean existsByWorkspaceIdAndIsOwnerTrue(String workspaceId);

    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);

    @Query("select wm.workspaceId from WorkspaceMember wm where wm.userId = :userId")
    List<String> findWorkspaceIdsByUserId(@Param("userId") String userId);

    void deleteByWorkspaceId(String workspaceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE WorkspaceMember w SET w.isOwner=false WHERE w.workspaceId=:workspaceId")
    void clearOwners(@Param("workspaceId") String workspaceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE WorkspaceMember w SET w.isOwner=true WHERE w.workspaceId=:workspaceId AND w.userId=:userId")
    int setOwner(@Param("workspaceId") String workspaceId, @Param("userId") String userId);

    @Query("SELECT w FROM WorkspaceMember w WHERE w.workspaceId = :workspaceId")
    List<WorkspaceMember> findAllByWorkspaceId(@Param("workspaceId") String workspaceId);

    boolean existsByWorkspaceIdAndInvitedEmailIgnoreCase(String workspaceId, String invitedEmail);
    Optional<WorkspaceMember> findFirstByWorkspaceIdAndInvitedEmailIgnoreCase(String workspaceId, String invitedEmail);

    @Query(value = """
        SELECT CASE WHEN EXISTS(
           SELECT 1 FROM workspace_members
           WHERE workspace_id = :workspaceId AND user_id = :userId AND is_owner = 1
        ) THEN 1 ELSE 0 END
        """, nativeQuery = true)
    int isOwnerRaw(@Param("workspaceId") String workspaceId, @Param("userId") String userId);

    default boolean isOwner(String workspaceId, String userId) {
        return isOwnerRaw(workspaceId, userId) == 1;
    }


    @Query("SELECT COUNT(w) FROM WorkspaceMember w WHERE w.workspaceId = :workspaceId AND w.isOwner = true")
    long countOwners(@Param("workspaceId") String workspaceId);

    @Modifying
    @Query("DELETE FROM WorkspaceMember w WHERE w.workspaceId = :workspaceId AND w.userId = :userId")
    int deleteByWorkspaceIdAndUserId(@Param("workspaceId") String workspaceId, @Param("userId") String userId);

    @Modifying
    @Query("delete from WorkspaceMember wm where wm.workspaceId in :workspaceIds")
    int deleteAllByWorkspaceIds(@Param("workspaceIds") List<String> workspaceIds);

}
