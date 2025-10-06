package com.springboot.demo.repository;

import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    
    List<Sprint> findByProjectId(String projectId);
    
    List<Sprint> findByProjectIdAndStatus(String projectId, SprintStatus status);
    
    Optional<Sprint> findFirstByProjectIdAndStatus(String projectId, SprintStatus status);
    
    @Query("SELECT s FROM Sprint s WHERE s.projectId = :projectId AND s.status = 'ACTIVE'")
    Optional<Sprint> findActiveSprintByProjectId(@Param("projectId") String projectId);
    
    boolean existsByProjectIdAndStatus(String projectId, SprintStatus status);
}
