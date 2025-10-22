package com.springboot.demo.service.impl;

import com.springboot.demo.model.Task;
import com.springboot.demo.repository.ProjectMemberRepository;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


// --- ĐỊNH NGHĨA CÁC LỚP EXCEPTION ---
// (Không cần @ResponseStatus nữa vì GlobalExceptionHandler sẽ xử lý)
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}


// --- BỘ XỬ LÝ LỖI TOÀN CỤC ---
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage()); // Lấy message chi tiết
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage()); // Lấy message chi tiết
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
}


// --- LỚP SERVICE IMPLEMENTATION CỦA BẠN ---
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
    public Task update(String taskId, Task task, String currentUserId) {
        Task existing = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        checkProjectAccess(existing.getProjectId(), currentUserId);

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
        Task existing = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        checkProjectAccess(existing.getProjectId(), currentUserId);
        taskRepo.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Task get(String taskId, String currentUserId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        checkProjectAccess(task.getProjectId(), currentUserId);
        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> listByProject(String projectId, String currentUserId) {
        checkProjectAccess(projectId, currentUserId);
        return taskRepo.findByProjectId(projectId);
    }
}