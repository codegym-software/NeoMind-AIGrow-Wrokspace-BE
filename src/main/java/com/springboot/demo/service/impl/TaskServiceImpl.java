package com.springboot.demo.service.impl;


import com.springboot.demo.exception.AccessDeniedException;
import com.springboot.demo.exception.ResourceNotFoundException;

import com.springboot.demo.model.Task;
import com.springboot.demo.repository.ProjectMemberRepository;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepo;
    private final ProjectMemberRepository pmRepo;
    private final ProjectRepository projectRepo;

    private void checkProjectAccess(String projectId, String userId) {
        if (!projectRepo.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        if (!pmRepo.existsByProjectIdAndUserId(projectId, userId)) {
            throw new AccessDeniedException("Access Denied. You are not a member of this project.");
        }
    }

    @Override
    @Transactional
    public Task create(Task task, String currentUserId) {
        checkProjectAccess(task.getProjectId(), currentUserId);
        return taskRepo.save(task);
    }

    @Override
    @Transactional
    public Task update(Long taskId, Task task, String currentUserId) {
        Task existing = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        checkProjectAccess(existing.getProjectId(), currentUserId);

        // --- Logic PATCH (vá lỗi) ---
        if (task.getTitle() != null) {
            existing.setTitle(task.getTitle());
        }
        if (task.getDescription() != null) {
            existing.setDescription(task.getDescription());
        }
        // CRITICAL: Update statusColumn - Check both null and valid ID
        if (task.getStatusColumn() != null && task.getStatusColumn().getId() != null) {
            existing.setStatusColumn(task.getStatusColumn());
        }
        if (task.getPriority() != null) {
            existing.setPriority(task.getPriority());
        }
        if (task.getDueDate() != null) {
            existing.setDueDate(task.getDueDate());
        }
        if (task.getEstimatedHours() != null) {
            existing.setEstimatedHours(task.getEstimatedHours());
        }
        // Handle assignedTo - allow explicit null to unassign
        existing.setAssignedTo(task.getAssignedTo());
        
        // Handle sprintId - allow explicit null to move to backlog
        existing.setSprintId(task.getSprintId());
        
        if (task.getTags() != null) {
            existing.setTags(task.getTags());
        }
        if (task.getOrderIndex() != null) {
            existing.setOrderIndex(task.getOrderIndex());
        }

        return taskRepo.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long taskId, String currentUserId) {
        Task existing = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        checkProjectAccess(existing.getProjectId(), currentUserId);
        taskRepo.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Task get(Long taskId, String currentUserId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        checkProjectAccess(task.getProjectId(), currentUserId);
        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listByProject(String projectId, String currentUserId, Pageable pageable) {
        checkProjectAccess(projectId, currentUserId);
        return taskRepo.findByProjectId(projectId, pageable);
    }
}