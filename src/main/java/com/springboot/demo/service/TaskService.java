package com.springboot.demo.service;

import com.springboot.demo.model.Task;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    Task create(Task task, String currentUserId);
    Task update(Long taskId, Task task, String currentUserId); // <-- SỬA Ở ĐÂY
    void delete(Long taskId, String currentUserId); // <-- SỬA Ở ĐÂY
    Task get(Long taskId, String currentUserId); // <-- SỬA Ở ĐÂY
    Page<Task> listByProject(String projectId, String currentUserId, Pageable pageable);
}