package com.springboot.demo.repository;

import com.springboot.demo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.springboot.demo.model.enums.TaskPriority;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    
    
    Page<Task> findByProjectId(String projectId, Pageable pageable);
    
    
    List<Task> findByProjectIdAndArchivedAtIsNull(String projectId);
    List<Task> findBySprintId(Integer sprintId);
    List<Task> findBySprintIdAndArchivedAtIsNull(Integer sprintId);
    List<Task> findByAssigneeIdAndArchivedAtIsNull(String assigneeId);
    List<Task> findByStatusColumn_Id(Integer columnId);
    List<Task> findByStatusColumn_IdAndArchivedAtIsNull(Integer columnId);
    List<Task> findByProjectIdAndStatusColumn_IdAndArchivedAtIsNull(String projectId, Integer columnId);
    
    
    Page<Task> findByAssigneeIdAndArchivedAtIsNull(String assigneeId, Pageable pageable);
    
    Page<Task> findByProjectIdAndArchivedAtIsNull(String projectId, Pageable pageable);
    
    
    Page<Task> findByProjectIdAndArchivedAtIsNotNull(String projectId, Pageable pageable);
    long countByEpicId(Integer epicId);
    Page<Task> findByEpicIdAndArchivedAtIsNull(Integer epicId, Pageable pageable);
    long countByProjectIdAndArchivedAtIsNull(String projectId);

    
    List<Task> findByProjectIdAndArchivedAtIsNullAndCreatedAtBetween(String projectId, LocalDateTime start, LocalDateTime end);
    List<Task> findByProjectIdAndArchivedAtIsNullAndUpdatedAtBetween(String projectId, LocalDateTime start, LocalDateTime end);
    List<Task> findByProjectIdAndArchivedAtIsNullAndDueDateBetween(String projectId, LocalDate start, LocalDate end);

    // Count Queries
    @Query("SELECT COUNT(t) FROM Task t WHERE t.sprintId = :sprintId AND t.statusColumn.id = :columnId AND t.archivedAt IS NULL")
    long countBySprintIdAndStatus(@Param("sprintId") Integer sprintId, @Param("columnId") Integer columnId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.statusColumn.id = :columnId AND t.archivedAt IS NULL")
    long countByProjectIdAndStatus(@Param("projectId") String projectId, @Param("columnId") Integer columnId);

    long countByProjectIdAndPriorityAndArchivedAtIsNull(String projectId, TaskPriority priority);
    long countByProjectIdAndAssigneeIdAndArchivedAtIsNull(String projectId, String assigneeId);
    long countByProjectIdAndArchivedAtIsNullAndAssigneeIdIsNull(String projectId);

    long countByStatusColumn_IdAndArchivedAtIsNull(Integer columnId);
}
