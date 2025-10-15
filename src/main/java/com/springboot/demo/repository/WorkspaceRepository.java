package com.springboot.demo.repository;

import com.springboot.demo.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, String> {

    List<Workspace> findAllByCompanyIdAndArchivedAtIsNull(String companyId);

    boolean existsByCompanyIdAndNameIgnoreCaseAndArchivedAtIsNull(String companyId, String name);

    @Query("select w from Workspace w where w.id in :ids and w.archivedAt is null")
    List<Workspace> findAllActiveByIds(@Param("ids") List<String> ids);

    @Query("select w from Workspace w where w.companyId = :companyId and w.archivedAt is null and lower(w.name) like lower(concat('%', :q, '%'))")
    List<Workspace> searchActiveInCompany(@Param("companyId") String companyId, @Param("q") String q);

    @Query("select w from Workspace w where w.id = :id and w.archivedAt is null")
    Optional<Workspace> findActiveById(@Param("id") String id);

    @Query("select w.id from Workspace w where w.companyId = :companyId")
    List<String> findIdsByCompanyId(@Param("companyId") String companyId);

    @Modifying
    @Query("delete from Workspace w where w.companyId = :companyId")
    int deleteByCompanyId(@Param("companyId") String companyId);

    @Query("""
    SELECT w FROM Workspace w
    WHERE w.companyId = :companyId
      AND (:q IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :q, '%')))
      AND w.archivedAt IS NULL
    ORDER BY w.createdAt DESC
  """)
    List<Workspace> searchByCompanyId(@Param("companyId") String companyId, @Param("q") String q);

    @Query(value = """
    SELECT w.* FROM workspaces w
    JOIN workspace_members wm ON wm.workspace_id = w.id
    WHERE wm.user_id = :userId
      AND w.company_id = :companyId
      AND (:includeArchived OR w.archived_at IS NULL)
    ORDER BY w.created_at DESC
  """, nativeQuery = true)
    List<Workspace> findMineInCompany(@Param("companyId") String companyId,
                                      @Param("userId") String userId,
                                      @Param("includeArchived") boolean includeArchived);

    @Query(value = """
    SELECT w.* FROM workspaces w
    JOIN workspace_members wm ON wm.workspace_id = w.id
    WHERE wm.user_id = :userId
      AND w.archived_at IS NULL
    ORDER BY w.created_at DESC
  """, nativeQuery = true)
    Page<Workspace> findByUserIdAndNameContainingIgnoreCase(@Param("userId") String userId,
                                                            @Param("query") String query,
                                                            Pageable pageable);

    long countByCompanyIdAndArchivedAtIsNull(String companyId);
}