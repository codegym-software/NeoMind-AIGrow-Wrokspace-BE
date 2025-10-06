package com.springboot.demo.service.impl;

import com.springboot.demo.dto.sprint.SprintCreateRequest;
import com.springboot.demo.dto.sprint.SprintProgressResponse;
import com.springboot.demo.dto.sprint.SprintUpdateRequest;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.SprintStatus;
import com.springboot.demo.model.Task;
import com.springboot.demo.model.TaskStatus;
import com.springboot.demo.repository.SprintRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SprintServiceImpl implements SprintService {
    
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    
    @Override
    public Sprint create(String projectId, SprintCreateRequest request) {
        // Check if there's already an active sprint
        if (sprintRepository.existsByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)) {
            throw new RuntimeException("Project already has an active sprint");
        }
        
        Sprint sprint = new Sprint();
        sprint.setProjectId(projectId);
        sprint.setName(request.name());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setStatus(request.status() != null ? request.status() : SprintStatus.PLANNED);
        sprint.setIsBacklog(request.isBacklog());
        sprint.setGoal(request.goal());
        
        return sprintRepository.save(sprint);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Sprint getById(Integer id) {
        Sprint sprint = sprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));
        
        return enrichSprintWithProgress(sprint);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Sprint getByIdAndProjectId(Integer id, String projectId) {
        Sprint sprint = sprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));
            
        if (!sprint.getProjectId().equals(projectId)) {
            throw new RuntimeException("Sprint does not belong to the specified project");
        }
        
        return enrichSprintWithProgress(sprint);
    }
    
    @Override
    public Sprint update(Integer id, SprintUpdateRequest request) {
        Sprint sprint = sprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));
            
        updateSprintFields(sprint, request);
        return sprintRepository.save(sprint);
    }
    
    
    @Override
    public void delete(Integer id) {
        Sprint sprint = sprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));
        sprintRepository.delete(sprint);
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public List<Sprint> getSprintsByProject(String projectId) {
        List<Sprint> sprints = sprintRepository.findByProjectId(projectId);
        return sprints.stream()
            .map(this::enrichSprintWithProgress)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Sprint> getSprintsByProjectAndStatus(String projectId, SprintStatus status) {
        List<Sprint> sprints = sprintRepository.findByProjectIdAndStatus(projectId, status);
        return sprints.stream()
            .map(this::enrichSprintWithProgress)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Sprint getActiveSprintByProject(String projectId) {
        Sprint sprint = sprintRepository.findFirstByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)
            .orElseThrow(() -> new RuntimeException("No active sprint found for project: " + projectId));
        
        return enrichSprintWithProgress(sprint);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SprintProgressResponse getSprintProgress(Integer sprintId) {
        Sprint sprint = getById(sprintId);
        return buildProgressResponse(sprint);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SprintProgressResponse getActiveSprintProgress(String projectId) {
        Sprint sprint = getActiveSprintByProject(projectId);
        return buildProgressResponse(sprint);
    }
    
    
    @Override
    public Sprint startSprint(Integer sprintId) {
        Sprint sprint = getById(sprintId);
        
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new RuntimeException("Only planned sprints can be started");
        }
        
        // Check if project already has active sprint
        if (sprintRepository.existsByProjectIdAndStatus(sprint.getProjectId(), SprintStatus.ACTIVE)) {
            throw new RuntimeException("Project already has an active sprint");
        }
        
        sprint.setStatus(SprintStatus.ACTIVE);
        return sprintRepository.save(sprint);
    }
    
    @Override
    public Sprint completeSprint(Integer sprintId) {
        Sprint sprint = getById(sprintId);
        
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new RuntimeException("Only active sprints can be completed");
        }
        
        sprint.setStatus(SprintStatus.COMPLETED);
        return sprintRepository.save(sprint);
    }
    
    @Override
    public Sprint cancelSprint(Integer sprintId) {
        Sprint sprint = getById(sprintId);
        
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new RuntimeException("Completed sprints cannot be cancelled");
        }
        
        sprint.setStatus(SprintStatus.CANCELLED);
        return sprintRepository.save(sprint);
    }
    
    private void updateSprintFields(Sprint sprint, SprintUpdateRequest request) {
        if (request.name() != null) {
            sprint.setName(request.name());
        }
        if (request.startDate() != null) {
            sprint.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            sprint.setEndDate(request.endDate());
        }
        if (request.status() != null) {
            sprint.setStatus(request.status());
        }
        if (request.isBacklog() != null) {
            sprint.setIsBacklog(request.isBacklog());
        }
        if (request.goal() != null) {
            sprint.setGoal(request.goal());
        }
    }
    
    private Sprint enrichSprintWithProgress(Sprint sprint) {
        List<Task> sprintTasks = taskRepository.findBySprintId(sprint.getId().toString());
        
        int totalTasks = sprintTasks.size();
        int completedTasks = (int) sprintTasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.DONE)
            .count();
            
        double progressPercentage = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        
        LocalDate now = LocalDate.now();
        int remainingDays = (int) ChronoUnit.DAYS.between(now, sprint.getEndDate());
        
        sprint.setTotalTasks(totalTasks);
        sprint.setCompletedTasks(completedTasks);
        sprint.setProgressPercentage(progressPercentage);
        sprint.setRemainingDays(Math.max(0, remainingDays));
        
        return sprint;
    }
    
    private SprintProgressResponse buildProgressResponse(Sprint sprint) {
        List<Task> sprintTasks = taskRepository.findBySprintId(sprint.getId().toString());
        
        int totalTasks = sprintTasks.size();
        int completedTasks = (int) sprintTasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.DONE)
            .count();
        int inProgressTasks = (int) sprintTasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
            .count();
        int todoTasks = (int) sprintTasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.TODO)
            .count();
            
        double progressPercentage = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        
        LocalDate now = LocalDate.now();
        int totalDays = (int) ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate());
        int elapsedDays = (int) ChronoUnit.DAYS.between(sprint.getStartDate(), now);
        int remainingDays = (int) ChronoUnit.DAYS.between(now, sprint.getEndDate());
        
        double timeProgressPercentage = totalDays > 0 ? (double) elapsedDays / totalDays * 100 : 0;
        
        boolean isOverdue = now.isAfter(sprint.getEndDate()) && sprint.getStatus() == SprintStatus.ACTIVE;
        boolean isEndingSoon = remainingDays <= 3 && remainingDays > 0 && sprint.getStatus() == SprintStatus.ACTIVE;
        
        String statusMessage = buildStatusMessage(sprint, isOverdue, isEndingSoon, remainingDays, progressPercentage);
        
        return new SprintProgressResponse(
            sprint.getId().toString(),
            sprint.getName(),
            sprint.getStatus(),
            sprint.getStartDate(),
            sprint.getEndDate(),
            totalTasks,
            completedTasks,
            inProgressTasks,
            todoTasks,
            progressPercentage,
            totalDays,
            Math.max(0, elapsedDays),
            Math.max(0, remainingDays),
            Math.min(100, Math.max(0, timeProgressPercentage)),
            isOverdue,
            isEndingSoon,
            statusMessage
        );
    }
    
    private String buildStatusMessage(Sprint sprint, boolean isOverdue, boolean isEndingSoon, 
                                    int remainingDays, double progressPercentage) {
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            return "Sprint completed successfully";
        }
        if (sprint.getStatus() == SprintStatus.CANCELLED) {
            return "Sprint was cancelled";
        }
        if (sprint.getStatus() == SprintStatus.PLANNED) {
            return "Sprint is in planning phase";
        }
        if (isOverdue) {
            return "Sprint is overdue! Please complete or extend.";
        }
        if (isEndingSoon) {
            return String.format("Sprint ending in %d days - %d%% complete", remainingDays, (int) progressPercentage);
        }
        if (remainingDays == 0) {
            return "Sprint ends today!";
        }
        return String.format("Sprint in progress - %d days remaining, %d%% complete", remainingDays, (int) progressPercentage);
    }
}
