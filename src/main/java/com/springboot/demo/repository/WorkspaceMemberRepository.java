package com.springboot.demo.repository;

import com.springboot.demo.dto.workspace.WorkspaceMemberDto;
import com.springboot.demo.model.WorkspaceMember;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer> {

    boolean existsByWorkspaceIdAndOwnerIsTrue(String workspaceId);

    List<WorkspaceMember> findAllByWorkspaceId(String workspaceId);

    List<WorkspaceMember> findAllByUserId(String userId);

    @Query("select wm from WorkspaceMember wm where wm.workspaceId = :workspaceId and wm.userId = :userId")
    Optional<WorkspaceMember> findOneByWorkspaceIdAndUserId(@Param("workspaceId") String workspaceId,
                                                            @Param("userId") String userId);

    @Modifying
    @Query("delete from WorkspaceMember wm where wm.workspaceId = :workspaceId and wm.userId = :userId")
    int deleteByWorkspaceIdAndUserId(@Param("workspaceId") String workspaceId, @Param("userId") String userId);

    @Modifying
    @Query("delete from WorkspaceMember wm where wm.workspaceId in :workspaceIds")
    int deleteAllByWorkspaceIds(@Param("workspaceIds") List<String> workspaceIds);

    long countByWorkspaceId(String workspaceId);
    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);

    @Query("SELECT wm.workspaceId, wm.userId, wm.roleId, wm.owner FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId")
    List<WorkspaceMemberDto> listMembers(@Param("workspaceId") String workspaceId);

    @Query("SELECT wm.workspaceId FROM WorkspaceMember wm WHERE wm.userId = :userId")
    List<String> findWorkspaceIdsByUserId(@Param("userId") String userId);

    @Query("SELECT (COUNT(wm) > 0) FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId AND wm.userId = :userId AND wm.owner = true")
    boolean isOwner(@Param("workspaceId") String workspaceId, @Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM WorkspaceMember wm WHERE wm.workspaceId = :workspaceId")
    void deleteByWorkspaceId(@Param("workspaceId") String workspaceId);
}