package com.springboot.demo.repository;

import com.springboot.demo.model.Task;
import com.springboot.demo.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    
    List<Task> findByProjectIdAndArchivedAtIsNull(String projectId);
    
    List<Task> findBySprintIdAndArchivedAtIsNull(String sprintId);
    
    List<Task> findByProjectIdAndStatusAndArchivedAtIsNull(String projectId, TaskStatus status);
    
    List<Task> findBySprintIdAndStatusAndArchivedAtIsNull(String sprintId, TaskStatus status);
    
    List<Task> findByAssignedToAndArchivedAtIsNull(String assignedTo);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.sprintId = :sprintId AND t.status = :status AND t.archivedAt IS NULL")
    long countBySprintIdAndStatus(@Param("sprintId") String sprintId, @Param("status") TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.status = :status AND t.archivedAt IS NULL")
    long countByProjectIdAndStatus(@Param("projectId") String projectId, @Param("status") TaskStatus status);
}
