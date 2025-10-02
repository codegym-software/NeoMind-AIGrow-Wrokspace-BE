package com.springboot.demo.service;

import com.springboot.demo.dto.sprint.SprintCreateRequest;
import com.springboot.demo.dto.sprint.SprintProgressResponse;
import com.springboot.demo.dto.sprint.SprintResponse;
import com.springboot.demo.dto.sprint.SprintUpdateRequest;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.SprintStatus;

import java.util.List;

public interface SprintService {
    
    Sprint create(String projectId, SprintCreateRequest request);
    
    Sprint getById(String id);
    
    Sprint getByIdAndProjectId(String id, String projectId);
    
    Sprint update(String id, SprintUpdateRequest request);
    
    Sprint updateByProjectId(String id, String projectId, SprintUpdateRequest request);
    
    void delete(String id);
    
    void deleteByProjectId(String id, String projectId);
    
    List<Sprint> getSprintsByProject(String projectId);
    
    List<Sprint> getSprintsByProjectAndStatus(String projectId, SprintStatus status);
    
    Sprint getActiveSprintByProject(String projectId);
    
    SprintProgressResponse getSprintProgress(String sprintId);
    
    SprintProgressResponse getActiveSprintProgress(String projectId);
    
    List<Sprint> getOverdueSprints();
    
    List<Sprint> getSprintsEndingSoon();
    
    Sprint startSprint(String sprintId);
    
    Sprint completeSprint(String sprintId);
    
    Sprint cancelSprint(String sprintId);
}
