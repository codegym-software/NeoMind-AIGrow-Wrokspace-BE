package com.springboot.demo.service.impl;


import com.springboot.demo.dto.board.TaskMoveRequest;
import com.springboot.demo.exception.AccessDeniedException;
import com.springboot.demo.exception.ResourceNotFoundException;

import com.springboot.demo.model.Board;
import com.springboot.demo.model.BoardColumn;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.Task;
import com.springboot.demo.model.enums.SprintStatus;
import com.springboot.demo.repository.BoardRepository;
import com.springboot.demo.repository.BoardColumnRepository;
import com.springboot.demo.repository.ProjectMemberRepository;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.repository.SprintRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepo;
    private final ProjectMemberRepository pmRepo;
    private final ProjectRepository projectRepo;
    private final BoardRepository boardRepo;
    private final BoardColumnRepository boardColumnRepo;
    private final SprintRepository sprintRepo;

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
        
        // Auto-assign default column if not set (To Do / first column)
        if (task.getStatusColumn() == null || task.getStatusColumn().getId() == null) {
            // Find board of this project
            Optional<Board> boardOpt = boardRepo.findByProjectIdAndIsDefaultTrue(task.getProjectId());
            
            if (boardOpt.isPresent()) {
                // Find columns of this board
                List<BoardColumn> columns = boardColumnRepo.findByBoardIdOrderByPositionAsc(boardOpt.get().getId());
                
                if (!columns.isEmpty()) {
                    // Try to find "To Do" column first
                    BoardColumn toDoColumn = columns.stream()
                        .filter(col -> "To Do".equalsIgnoreCase(col.getName()) || "TODO".equalsIgnoreCase(col.getName()))
                        .findFirst()
                        .orElse(columns.get(0)); // Fallback to first column
                    
                    task.setStatusColumn(toDoColumn);
                }
            }
        }
        
        // Auto-assign to active sprint if not set (khi tạo từ Board)
        if (task.getSprintId() == null) {
            Optional<Sprint> activeSprint = sprintRepo.findByProjectIdAndStatus(task.getProjectId(), SprintStatus.active);
            if (activeSprint.isPresent()) {
                task.setSprintId(activeSprint.get().getId());
            }
        }
        
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
        
        
        if (task.getAssigneeId() != null) {
            existing.setAssigneeId(task.getAssigneeId());
        }
        
       
        if (task.getOrderIndex() != null) {
            // This is a drag & drop operation, update sprintId (can be null for backlog)
            existing.setSprintId(task.getSprintId());
        } else if (task.getSprintId() != null) {
            // Regular update with non-null sprintId
            existing.setSprintId(task.getSprintId());
        }
        
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