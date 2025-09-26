package com.springboot.demo.workspace;

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
    Page<Workspace> findByCompanyIdAndNotArchived(@Param("companyId") String companyId, Pageable pageable);
    
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
    Page<Workspace> searchByCompanyIdAndName(@Param("companyId") String companyId, 
                                           @Param("query") String query, 
                                           Pageable pageable);
    
    // Count active workspaces by company
    @Query("SELECT COUNT(w) FROM Workspace w WHERE w.companyId = :companyId AND w.archivedAt IS NULL")
    long countByCompanyIdAndNotArchived(@Param("companyId") String companyId);
    
    // Find workspace by name within a company (for duplicate checking)
    @Query("SELECT w FROM Workspace w WHERE w.companyId = :companyId AND w.name = :name AND w.archivedAt IS NULL")
    Optional<Workspace> findByCompanyIdAndNameAndNotArchived(@Param("companyId") String companyId, 
                                                           @Param("name") String name);
}