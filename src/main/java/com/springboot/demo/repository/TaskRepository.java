package com.springboot.demo.repository;

import com.springboot.demo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    Page<Task> findByProjectId(String projectId, Pageable pageable);
    
    List<Task> findBySprintId(Integer sprintId);
    
    List<Task> findBySprintIdAndArchivedAtIsNull(Integer sprintId);
    

    List<Task> findByProjectIdAndStatusColumn_IdAndArchivedAtIsNull(String projectId, Integer columnId);
    
    
    List<Task> findBySprintIdAndStatusColumn_IdAndArchivedAtIsNull(Integer sprintId, Integer columnId);
    
    List<Task> findByAssignedToAndArchivedAtIsNull(String assignedTo);
    
    List<Task> findByProjectId(String projectId);
    
   
    @Query("SELECT COUNT(t) FROM Task t WHERE t.sprintId = :sprintId AND t.statusColumn.id = :columnId AND t.archivedAt IS NULL")
    long countBySprintIdAndStatus(@Param("sprintId") Integer sprintId, @Param("columnId") Integer columnId);
    
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.statusColumn.id = :columnId AND t.archivedAt IS NULL")
    long countByProjectIdAndStatus(@Param("projectId") String projectId, @Param("columnId") Integer columnId);
}