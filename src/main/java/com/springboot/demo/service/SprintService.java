package com.springboot.demo.service;

import com.springboot.demo.dto.sprint.*;
import java.util.List;

public interface SprintService {
    SprintDto create(String projectId, CreateSprintReq req);
    List<SprintDto> listByProject(String projectId);
    SprintDto update(Integer sprintId, UpdateSprintReq req);
    void delete(Integer sprintId);
}
