package com.springboot.demo.service;

import com.springboot.demo.dto.epic.*;
import com.springboot.demo.model.Epic;
import com.springboot.demo.dto.task.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EpicService {
    EpicDto create(String projectId, EpicCreateRequest req, String currentUserId);
    EpicListResponse listByProject(String projectId, String currentUserId, Pageable pageable);
    Page<EpicDto> search(String projectId, String currentUserId, EpicSearchRequest req, Pageable pageable);
    EpicDto get(String projectId, Integer epicId, String currentUserId);
    EpicDto update(String projectId, Integer epicId, EpicUpdateRequest req, String currentUserId);
    void delete(String projectId, Integer epicId, String currentUserId);
    Page<TaskResponse> listTasks(String projectId, Integer epicId, String currentUserId, Pageable pageable);
}
