package com.springboot.demo.service.impl;

import com.springboot.demo.model.Task;
import com.springboot.demo.model.ProjectMember;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.repository.ProjectMemberRepository;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepo;
    private final ProjectMemberRepository pmRepo;
    private final ProjectRepository projectRepo;

    private void checkProjectMember(String projectId, String userId) {
        if (!pmRepo.existsByProjectIdAndUserId(projectId, userId)) {
            throw new SecurityException("User does not have permission in this project");
        }
    }

    @Override
    @Transactional
    public Task create(Task task, String currentUserId) {
        checkProjectMember(task.getProjectId(), currentUserId);
        return taskRepo.save(task);
    }

    @Override
    @Transactional
    public Task update(String taskId, Task task, String currentUserId) {
        Task existing = taskRepo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        checkProjectMember(existing.getProjectId(), currentUserId);
        // Update allowed fields
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setStatus(task.getStatus());
        existing.setPriority(task.getPriority());
        existing.setDueDate(task.getDueDate());
        existing.setSprintId(task.getSprintId());
        existing.setAssignedTo(task.getAssignedTo());
        existing.setEstimatedHours(task.getEstimatedHours());
        existing.setActualHours(task.getActualHours());
        existing.setTags(task.getTags());
        existing.setOrderIndex(task.getOrderIndex());
        return taskRepo.save(existing);
    }

    @Override
    @Transactional
    public void delete(String taskId, String currentUserId) {
        Task existing = taskRepo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        checkProjectMember(existing.getProjectId(), currentUserId);
        taskRepo.deleteById(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public Task get(String taskId, String currentUserId) {
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        checkProjectMember(task.getProjectId(), currentUserId);
        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> listByProject(String projectId, String currentUserId) {
        checkProjectMember(projectId, currentUserId);
        return taskRepo.findByProjectId(projectId);
    }
}