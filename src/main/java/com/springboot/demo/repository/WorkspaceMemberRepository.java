package com.springboot.demo.repository;

import com.springboot.demo.model.WorkspaceMember;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, String> {

    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);

    @Query("select wm from WorkspaceMember wm where wm.workspaceId = :workspaceId")
    List<WorkspaceMember> findAllByWorkspaceId(@Param("workspaceId") String workspaceId);

    @Modifying
    @Query("delete from WorkspaceMember wm where wm.workspaceId = :workspaceId and wm.userId = :userId")
    void deleteByWorkspaceIdAndUserId(@Param("workspaceId") String workspaceId,
                                      @Param("userId") String userId);

    @Modifying
    @Query("delete from WorkspaceMember wm where wm.workspaceId = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") String workspaceId);

    @Query("select count(wm) from WorkspaceMember wm where wm.workspaceId = :workspaceId and wm.isOwner = true")
    long countOwners(@Param("workspaceId") String workspaceId);

    @Modifying
    @Query("delete from WorkspaceMember wm where wm.workspaceId in :workspaceIds")
    int deleteAllByWorkspaceIds(@Param("workspaceIds") List<String> workspaceIds);

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
}
