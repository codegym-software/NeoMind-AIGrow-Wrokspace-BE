package com.springboot.demo.service;

import com.springboot.demo.dto.task.*;

public interface BacklogService {
    TaskListResponse listBacklog(String projectId);
    ReorderResult reorderBacklog(String projectId, ReorderRequest req, String currentUserId);
}
