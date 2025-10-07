package com.springboot.demo.repository;

import com.springboot.demo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(String projectId);
    List<Task> findBySprintId(Integer sprintId);
    List<Task> findByEpicId(Integer epicId);

}
