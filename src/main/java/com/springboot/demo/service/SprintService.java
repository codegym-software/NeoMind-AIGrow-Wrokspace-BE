package com.springboot.demo.service;

import com.springboot.demo.dto.sprint.SprintCreateRequest;
import com.springboot.demo.dto.sprint.SprintProgressResponse;
import com.springboot.demo.dto.sprint.SprintUpdateRequest;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.SprintStatus;

import java.util.List;

public interface SprintService {
    
    Sprint create(String projectId, SprintCreateRequest request);
    
    Sprint getById(Integer id);
    
    Sprint getByIdAndProjectId(Integer id, String projectId);
    
    Sprint update(Integer id, SprintUpdateRequest request);
    
    void delete(Integer id);
    
    List<Sprint> getSprintsByProject(String projectId);
    
    List<Sprint> getSprintsByProjectAndStatus(String projectId, SprintStatus status);
    
    Sprint getActiveSprintByProject(String projectId);
    
    SprintProgressResponse getSprintProgress(Integer sprintId);
    
    SprintProgressResponse getActiveSprintProgress(String projectId);

    Sprint startSprint(Integer sprintId);
    
    Sprint completeSprint(Integer sprintId);
    
    Sprint cancelSprint(Integer sprintId);
    
}
