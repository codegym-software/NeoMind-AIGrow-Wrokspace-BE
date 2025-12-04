package com.springboot.demo.service;

import com.springboot.demo.dto.board.TaskMoveRequest;
import com.springboot.demo.dto.task.TaskGroupedResponse;
import com.springboot.demo.dto.task.TaskSearchRequest;
import com.springboot.demo.model.Task;
import com.springboot.demo.model.ActivityLog;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    Page<Task> search(String projectId, String currentUserId, TaskSearchRequest req, Pageable pageable);
    Task assign(Long taskId, String assigneeId, String currentUserId);
    TaskGroupedResponse groupBy(String projectId, String groupBy, String currentUserId);
    Task setEpic(Long taskId, Integer epicId, String currentUserId);
    
    // Hàm từ nhánh 'main'
    Page<Task> listMyTasks(String userId, Pageable pageable);
    Map<String, Long> metricsLast7Days(String projectId, String userId);
    Map<String, Long> statusDistribution(String projectId, String userId);
    void transferTasksFromColumn(String projectId, Integer sourceColumnId, Integer targetColumnId, String userId);
    Map<String, Long> priorityDistribution(String projectId, String userId);
    Map<String, Long> workloadByAssignee(String projectId, String userId);
    List<ActivityLog> recentLogs(String projectId, String userId);
}
