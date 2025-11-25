package com.springboot.demo.service;

import com.springboot.demo.dto.board.TaskMoveRequest;
import com.springboot.demo.dto.task.TaskSearchRequest;
import com.springboot.demo.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;

public interface TaskService {
    Task create(Task task, String currentUserId);
    Task update(Long taskId, Task task, String currentUserId);
    void delete(Long taskId, String currentUserId);
    Task get(Long taskId, String currentUserId);
    Page<Task> listByProject(String projectId, String currentUserId, Pageable pageable);
    Task moveTask(Long taskId, TaskMoveRequest request, String currentUserId);
    Task archive(Long taskId, String userId);
    Task restore(Long taskId, String userId);
    Page<Task> listArchivedByProject(String projectId, String userId, Pageable pageable);
    
    // Hàm từ nhánh 'list'
    Page<Task> search(String projectId, String currentUserId, TaskSearchRequest req, Pageable pageable);
    
    // Hàm từ nhánh 'main'
    Page<Task> listMyTasks(String userId, Pageable pageable);
    Map<String, Long> metricsLast7Days(String projectId, String userId);
    Map<String, Long> statusDistribution(String projectId, String userId);
    void transferTasksFromColumn(String projectId, Integer sourceColumnId, Integer targetColumnId, String userId);
}
