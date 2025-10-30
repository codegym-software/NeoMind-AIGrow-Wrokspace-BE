package com.springboot.demo.service;

import com.springboot.demo.dto.board.TaskMoveRequest;
import com.springboot.demo.model.Task;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    Task create(Task task, String currentUserId);
    Task update(Long taskId, Task task, String currentUserId);
    void delete(Long taskId, String currentUserId);
    Task get(Long taskId, String currentUserId);
    Page<Task> listByProject(String projectId, String currentUserId, Pageable pageable);
    
    /**
     * API 6 (MỚI): Di chuyển Task (Kéo thả) - Chuẩn Jira
     * Logic xử lý cả di chuyển trong cùng cột và sang cột khác
     */
    Task moveTask(Long taskId, TaskMoveRequest request, String currentUserId);
}