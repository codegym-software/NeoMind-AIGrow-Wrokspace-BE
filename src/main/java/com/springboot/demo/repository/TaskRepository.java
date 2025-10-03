package com.springboot.demo.repository;

import com.springboot.demo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByProjectIdAndArchivedAtIsNull(String projectId);
    List<Task> findByProjectIdAndSprintIdIsNullAndArchivedAtIsNullOrderByCreatedAtAscIdAsc(String projectId);
    List<Task> findByIdIn(Collection<Integer> ids);
}
