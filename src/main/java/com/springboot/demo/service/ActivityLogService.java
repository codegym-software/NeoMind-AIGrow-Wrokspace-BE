package com.springboot.demo.service;

import com.springboot.demo.model.Task;

public interface ActivityLogService {
    void logTaskCreated(Task task, String userId);
    void logTaskUpdated(Task before, Task after, String userId);
    void logFieldChange(String projectId, Long taskId, String userId, String fieldName);
}
