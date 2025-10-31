package com.springboot.demo.service.impl;


import com.springboot.demo.dto.board.TaskMoveRequest;
import com.springboot.demo.exception.AccessDeniedException;
import com.springboot.demo.exception.ResourceNotFoundException;

import com.springboot.demo.model.BoardColumn;
import com.springboot.demo.model.Task;
import com.springboot.demo.repository.BoardColumnRepository;
import com.springboot.demo.repository.ProjectMemberRepository;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepo;
    private final ProjectMemberRepository pmRepo;
    private final ProjectRepository projectRepo;
    private final BoardColumnRepository boardColumnRepo;

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
        
        // CHUẨN HÓA: Đổi assignedTo → assigneeId (allow explicit null to unassign)
        existing.setAssigneeId(task.getAssigneeId());
        
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

    /**
     * API 6 (MỚI): Di chuyển Task (Kéo thả) - Chuẩn Jira
     * PATCH /api/projects/{projectId}/tasks/{taskId}/move
     * 
     * Logic:
     * 1. Load task từ database
     * 2. Kiểm tra xem có thay đổi column không
     * 3. Trường hợp 1: Di chuyển trong cùng cột
     *    - Chỉ cập nhật orderIndex của task này và các task xung quanh
     * 4. Trường hợp 2: Di chuyển sang cột khác
     *    - Cập nhật columnId và orderIndex
     *    - Cập nhật orderIndex của các task trong cột cũ (lấp chỗ trống)
     *    - Cập nhật orderIndex của các task trong cột mới (chèn vào)
     */
    @Override
    @Transactional
    public Task moveTask(Long taskId, TaskMoveRequest request, String currentUserId) {
        // 1. Load task và validate
        Task task = taskRepo.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        checkProjectAccess(task.getProjectId(), currentUserId);

        // 2. Validate column tồn tại
        BoardColumn newColumn = boardColumnRepo.findById(request.getNewColumnId())
            .orElseThrow(() -> new ResourceNotFoundException("Column not found with id: " + request.getNewColumnId()));

        Integer oldColumnId = task.getStatusColumn() != null ? task.getStatusColumn().getId() : null;
        Integer oldOrderIndex = task.getOrderIndex();
        Integer newColumnId = request.getNewColumnId();
        Integer newOrderIndex = request.getNewOrderIndex();

        // 3. Trường hợp 1: Di chuyển trong cùng cột
        if (oldColumnId != null && oldColumnId.equals(newColumnId)) {
            reorderWithinSameColumn(task, oldOrderIndex, newOrderIndex);
        } 
        // 4. Trường hợp 2: Di chuyển sang cột khác
        else {
            moveToDifferentColumn(task, oldColumnId, newColumn, newOrderIndex);
        }

        return taskRepo.save(task);
    }

    /**
     * Helper: Sắp xếp lại tasks trong cùng một cột
     * Ví dụ: Di chuyển task từ position 2 lên position 0
     * - Tasks ở position 0, 1 sẽ bị đẩy xuống (+ 1)
     * - Task này nhận position 0
     */
    private void reorderWithinSameColumn(Task task, Integer oldOrderIndex, Integer newOrderIndex) {
        if (oldOrderIndex.equals(newOrderIndex)) {
            return; // Không thay đổi gì
        }

        Integer columnId = task.getStatusColumn().getId();
        List<Task> tasksInColumn = taskRepo.findByProjectIdAndStatusColumn_IdAndArchivedAtIsNull(
            task.getProjectId(), columnId
        );

        // Sắp xếp lại orderIndex
        for (Task t : tasksInColumn) {
            if (t.getId().equals(task.getId())) {
                continue; // Skip task đang di chuyển
            }

            int currentIndex = t.getOrderIndex();
            
            // Task kéo xuống (từ trên xuống dưới)
            if (oldOrderIndex < newOrderIndex) {
                if (currentIndex > oldOrderIndex && currentIndex <= newOrderIndex) {
                    t.setOrderIndex(currentIndex - 1);
                    taskRepo.save(t);
                }
            }
            // Task kéo lên (từ dưới lên trên)
            else {
                if (currentIndex >= newOrderIndex && currentIndex < oldOrderIndex) {
                    t.setOrderIndex(currentIndex + 1);
                    taskRepo.save(t);
                }
            }
        }

        task.setOrderIndex(newOrderIndex);
    }

    /**
     * Helper: Di chuyển task sang cột khác
     * 1. Lấp chỗ trống trong cột cũ (các task phía sau đẩy lên)
     * 2. Chèn vào cột mới (các task phía sau đẩy xuống)
     */
    private void moveToDifferentColumn(Task task, Integer oldColumnId, BoardColumn newColumn, Integer newOrderIndex) {
        // 1. Cập nhật cột cũ - Lấp chỗ trống
        if (oldColumnId != null) {
            List<Task> tasksInOldColumn = taskRepo.findByProjectIdAndStatusColumn_IdAndArchivedAtIsNull(
                task.getProjectId(), oldColumnId
            );

            for (Task t : tasksInOldColumn) {
                if (t.getId().equals(task.getId())) {
                    continue;
                }
                if (t.getOrderIndex() > task.getOrderIndex()) {
                    t.setOrderIndex(t.getOrderIndex() - 1);
                    taskRepo.save(t);
                }
            }
        }

        // 2. Cập nhật cột mới - Chèn vào
        List<Task> tasksInNewColumn = taskRepo.findByProjectIdAndStatusColumn_IdAndArchivedAtIsNull(
            task.getProjectId(), newColumn.getId()
        );

        for (Task t : tasksInNewColumn) {
            if (t.getOrderIndex() >= newOrderIndex) {
                t.setOrderIndex(t.getOrderIndex() + 1);
                taskRepo.save(t);
            }
        }

        // 3. Cập nhật task hiện tại
        task.setStatusColumn(newColumn);
        task.setOrderIndex(newOrderIndex);
    }
}