package com.springboot.demo.service;

import com.springboot.demo.model.Task;
import java.util.List;

public interface TaskService {
    Task create(Task task, String currentUserId);
    Task update(Long taskId, Task task, String currentUserId);
    void delete(Long taskId, String currentUserId);
    Task get(Long taskId, String currentUserId);
    List<Task> listByProject(String projectId, String currentUserId);
}
