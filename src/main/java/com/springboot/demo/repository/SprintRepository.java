package com.springboot.demo.repository;

import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, String> {
    
    List<Sprint> findByProjectIdAndArchivedAtIsNull(String projectId);
    
    List<Sprint> findByProjectIdAndStatusAndArchivedAtIsNull(String projectId, SprintStatus status);
    
    Optional<Sprint> findFirstByProjectIdAndStatusAndArchivedAtIsNull(String projectId, SprintStatus status);
    
    @Query("SELECT s FROM Sprint s WHERE s.projectId = :projectId AND s.status = 'ACTIVE' AND s.archivedAt IS NULL")
    Optional<Sprint> findActiveSprintByProjectId(@Param("projectId") String projectId);
    
    @Query("SELECT s FROM Sprint s WHERE s.status = 'ACTIVE' AND s.endDate < :currentDate AND s.archivedAt IS NULL")
    List<Sprint> findOverdueSprints(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT s FROM Sprint s WHERE s.status = 'ACTIVE' AND s.endDate BETWEEN :startDate AND :endDate AND s.archivedAt IS NULL")
    List<Sprint> findSprintsEndingSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    boolean existsByProjectIdAndStatusAndArchivedAtIsNull(String projectId, SprintStatus status);
}
