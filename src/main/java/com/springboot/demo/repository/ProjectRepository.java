package com.springboot.demo.repository;

import com.springboot.demo.model.Project;
import com.springboot.demo.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    // Find all projects by workspace ID (excluding archived)
    @Query("SELECT p FROM Project p WHERE p.workspaceId = :workspaceId AND p.archivedAt IS NULL")
    Page<Project> findByWorkspaceIdAndNotArchived(@Param("workspaceId") String workspaceId, Pageable pageable);

    // Find all projects by workspace ID (including archived)
    List<Project> findByWorkspaceId(String workspaceId);

    // Find project by ID and workspace ID (excluding archived)
    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.workspaceId = :workspaceId AND p.archivedAt IS NULL")
    Optional<Project> findByIdAndWorkspaceIdAndNotArchived(@Param("id") String id, @Param("workspaceId") String workspaceId);

    // Find project by name and workspace ID (excluding archived) - for duplicate check
    @Query("SELECT p FROM Project p WHERE p.workspaceId = :workspaceId AND LOWER(p.name) = LOWER(:name) AND p.archivedAt IS NULL")
    Optional<Project> findByWorkspaceIdAndNameAndNotArchived(@Param("workspaceId") String workspaceId, @Param("name") String name);

    // Search projects by name within a workspace (excluding archived)
    @Query("SELECT p FROM Project p WHERE p.workspaceId = :workspaceId AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) AND p.archivedAt IS NULL")
    Page<Project> searchByWorkspaceIdAndName(@Param("workspaceId") String workspaceId,
                                           @Param("query") String query,
                                           Pageable pageable);

    // Find projects by status within a workspace (excluding archived)
    @Query("SELECT p FROM Project p WHERE p.workspaceId = :workspaceId AND p.status = :status AND p.archivedAt IS NULL")
    Page<Project> findByWorkspaceIdAndStatus(@Param("workspaceId") String workspaceId,
                                           @Param("status") ProjectStatus status,
                                           Pageable pageable);

    // Find overdue projects within a workspace
    @Query("SELECT p FROM Project p WHERE p.workspaceId = :workspaceId AND " +
            "p.endDate < :currentDate AND p.status != 'COMPLETED' AND p.archivedAt IS NULL")
    List<Project> findOverdueProjectsByWorkspaceId(@Param("workspaceId") String workspaceId,
                                                  @Param("currentDate") LocalDate currentDate);

    // Find projects ending soon within a workspace
    @Query("SELECT p FROM Project p WHERE p.workspaceId = :workspaceId AND " +
            "p.endDate BETWEEN :startDate AND :endDate AND p.status != 'COMPLETED' AND p.archivedAt IS NULL")
    List<Project> findProjectsEndingSoon(@Param("workspaceId") String workspaceId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    // Count active projects by workspace
    @Query("SELECT COUNT(p) FROM Project p WHERE p.workspaceId = :workspaceId AND p.archivedAt IS NULL")
    long countByWorkspaceIdAndNotArchived(@Param("workspaceId") String workspaceId);

    // Count projects by status within a workspace
    @Query("SELECT COUNT(p) FROM Project p WHERE p.workspaceId = :workspaceId AND p.status = :status AND p.archivedAt IS NULL")
    long countByWorkspaceIdAndStatus(@Param("workspaceId") String workspaceId, @Param("status") ProjectStatus status);

    // Hard delete all projects by workspace ID
    void deleteByWorkspaceId(String workspaceId);

    // Find all active projects (not archived, status is ACTIVE)
    @Query("SELECT p FROM Project p WHERE p.workspaceId = :workspaceId AND p.status = 'ACTIVE' AND p.archivedAt IS NULL")
    List<Project> findActiveProjectsByWorkspaceId(@Param("workspaceId") String workspaceId);
}

