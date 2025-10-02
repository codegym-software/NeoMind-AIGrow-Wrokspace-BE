package com.springboot.demo.repository;

import com.springboot.demo.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, String> {

    // Find all workspaces by company ID (excluding archived)
    @Query("SELECT w FROM Workspace w WHERE w.companyId = :companyId AND w.archivedAt IS NULL")
    List<Workspace> findByCompanyIdAndNotArchived(@Param("companyId") String companyId);

    // Find all workspaces by company ID (including archived)
    List<Workspace> findByCompanyId(String companyId);

    // Hard delete all workspaces by company ID
    void deleteByCompanyId(String companyId);

    // Find workspace by ID and company ID (excluding archived)
    @Query("SELECT w FROM Workspace w WHERE w.id = :id AND w.companyId = :companyId AND w.archivedAt IS NULL")
    Optional<Workspace> findByIdAndCompanyIdAndNotArchived(@Param("id") String id, @Param("companyId") String companyId);

    // Search workspaces by name within a company (excluding archived)
    @Query("SELECT w FROM Workspace w WHERE w.companyId = :companyId AND " +
            "LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')) AND w.archivedAt IS NULL")
    List<Workspace> searchByCompanyIdAndName(@Param("companyId") String companyId,
                                             @Param("query") String query);

    // Count active workspaces by company
    @Query("SELECT COUNT(w) FROM Workspace w WHERE w.companyId = :companyId AND w.archivedAt IS NULL")
    long countByCompanyIdAndNotArchived(@Param("companyId") String companyId);

    // Find workspace by name within a company (for duplicate checking)
    @Query("SELECT w FROM Workspace w WHERE w.companyId = :companyId AND w.name = :name AND w.archivedAt IS NULL")
    Optional<Workspace> findByCompanyIdAndNameAndNotArchived(@Param("companyId") String companyId,
                                                             @Param("name") String name);

    @Query(value = """
        SELECT w.*
        FROM workspaces w
        JOIN workspace_members wm ON wm.workspace_id = w.id
        WHERE w.company_id = :companyId
          AND wm.user_id = :userId
          AND (:includeArchived = TRUE OR w.archived_at IS NULL)
        ORDER BY w.created_at DESC
        """, nativeQuery = true)
    List<Workspace> findMineInCompany(@Param("companyId") String companyId,
                                         @Param("userId") String userId,
                                         @Param("includeArchived") boolean includeArchived);
}

