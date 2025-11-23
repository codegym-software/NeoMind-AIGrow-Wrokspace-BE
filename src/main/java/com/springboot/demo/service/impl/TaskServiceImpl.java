package com.springboot.demo.service.impl;


import com.springboot.demo.dto.board.TaskMoveRequest;
import com.springboot.demo.exception.AccessDeniedException;
import com.springboot.demo.exception.ResourceNotFoundException;

import com.springboot.demo.model.Board;
import com.springboot.demo.model.BoardColumn;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.Task;
import com.springboot.demo.model.enums.SprintStatus;
import com.springboot.demo.model.enums.TaskPriority;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

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

        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.medium);
        }

        if (task.getOrderIndex() == null) {
            task.setOrderIndex(0);
        }

        boolean isCreatedFromBoard = task.getStatusColumn() != null && task.getStatusColumn().getId() != null;

        if (!isCreatedFromBoard) {
            Optional<Board> boardOpt = boardRepo.findByProjectIdAndIsDefaultTrue(task.getProjectId());
            
            if (boardOpt.isPresent()) {
                List<BoardColumn> columns = boardColumnRepo.findByBoardIdOrderByPositionAsc(boardOpt.get().getId());
                
                if (!columns.isEmpty()) {
                    BoardColumn toDoColumn = columns.stream()
                        .filter(col -> "To Do".equalsIgnoreCase(col.getName()) || "TODO".equalsIgnoreCase(col.getName()))
                        .findFirst()
                        .orElse(columns.get(0));
                    
                    task.setStatusColumn(toDoColumn);
                }
            }
        }

        if (task.getSprintId() == null && isCreatedFromBoard) {
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

        if (task.getTitle() != null) {
            existing.setTitle(task.getTitle());
        }
        if (task.getDescription() != null) {
            existing.setDescription(task.getDescription());
        }
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

        if (task.getStatusColumn() == null || task.getStatusColumn().getId() == null) {
            if (task.getOrderIndex() != null) {
                existing.setSprintId(task.getSprintId());
            } else if (task.getSprintId() != null) {
                existing.setSprintId(task.getSprintId());
            }
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
    @Transactional
    public Task moveTask(Long taskId, TaskMoveRequest request, String currentUserId) {
        Task task = taskRepo.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        checkProjectAccess(task.getProjectId(), currentUserId);

        BoardColumn newColumn = boardColumnRepo.findById(request.getNewColumnId())
            .orElseThrow(() -> new ResourceNotFoundException("Column not found with id: " + request.getNewColumnId()));

        Integer oldColumnId = task.getStatusColumn() != null ? task.getStatusColumn().getId() : null;
        Integer oldOrderIndex = task.getOrderIndex();
        Integer newColumnId = request.getNewColumnId();
        Integer newOrderIndex = request.getNewOrderIndex();

        if (oldColumnId != null && oldColumnId.equals(newColumnId)) {
            reorderWithinSameColumn(task, oldOrderIndex, newOrderIndex);
        }
        else {
            moveToDifferentColumn(task, oldColumnId, newColumn, newOrderIndex);
        }

        return taskRepo.save(task);
    }

    private void reorderWithinSameColumn(Task task, Integer oldOrderIndex, Integer newOrderIndex) {
        if (oldOrderIndex.equals(newOrderIndex)) {
            return;
        }

        Integer columnId = task.getStatusColumn().getId();
        List<Task> tasksInColumn = taskRepo.findByProjectIdAndStatusColumn_IdAndArchivedAtIsNull(
            task.getProjectId(), columnId
        );

        for (Task t : tasksInColumn) {
            if (t.getId().equals(task.getId())) {
                continue;
            }

            int currentIndex = t.getOrderIndex();

            if (oldOrderIndex < newOrderIndex) {
                if (currentIndex > oldOrderIndex && currentIndex <= newOrderIndex) {
                    t.setOrderIndex(currentIndex - 1);
                    taskRepo.save(t);
                }
            }
            else {
                if (currentIndex >= newOrderIndex && currentIndex < oldOrderIndex) {
                    t.setOrderIndex(currentIndex + 1);
                    taskRepo.save(t);
                }
            }
        }

        task.setOrderIndex(newOrderIndex);
    }

    private void moveToDifferentColumn(Task task, Integer oldColumnId, BoardColumn newColumn, Integer newOrderIndex) {
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

        List<Task> tasksInNewColumn = taskRepo.findByProjectIdAndStatusColumn_IdAndArchivedAtIsNull(
            task.getProjectId(), newColumn.getId()
        );

        for (Task t : tasksInNewColumn) {
            if (t.getOrderIndex() >= newOrderIndex) {
                t.setOrderIndex(t.getOrderIndex() + 1);
                taskRepo.save(t);
            }
        }

        task.setStatusColumn(newColumn);
        task.setOrderIndex(newOrderIndex);
    }
    @Override
    @Transactional
    public Task archive(Long taskId, String userId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        checkProjectAccess(task.getProjectId(), userId);

        task.setArchivedAt(java.time.LocalDateTime.now());
        return taskRepo.save(task);
    }

    @Override
    @Transactional
    public Task restore(Long taskId, String userId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        checkProjectAccess(task.getProjectId(), userId);

        task.setArchivedAt(null);
        return taskRepo.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listByProject(String projectId, String currentUserId, Pageable pageable) {
        checkProjectAccess(projectId, currentUserId);
        return taskRepo.findByProjectIdAndArchivedAtIsNull(projectId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listArchivedByProject(String projectId, String userId, Pageable pageable) {
        checkProjectAccess(projectId, userId);
        return taskRepo.findByProjectIdAndArchivedAtIsNotNull(projectId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listMyTasks(String userId, Pageable pageable) {
        return taskRepo.findByAssigneeIdAndArchivedAtIsNull(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> metricsLast7Days(String projectId, String userId) {
        checkProjectAccess(projectId, userId);

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = LocalDate.now().minusDays(6).atStartOfDay();

        List<Task> created = taskRepo.findByProjectIdAndArchivedAtIsNullAndCreatedAtBetween(projectId, start, end);
        List<Task> updated = taskRepo.findByProjectIdAndArchivedAtIsNullAndUpdatedAtBetween(projectId, start, end);

        long createdCount = created.size();
        long updatedCount = updated.stream()
                .filter(t -> t.getUpdatedAt() != null && t.getCreatedAt() != null && t.getUpdatedAt().isAfter(t.getCreatedAt()))
                .count();

        long completedCount = updated.stream()
                .filter(Task::isCompleted)
                .count();

        LocalDate dueStart = LocalDate.now();
        LocalDate dueEnd = dueStart.plusDays(6);
        List<Task> dueSoon = taskRepo.findByProjectIdAndArchivedAtIsNullAndDueDateBetween(projectId, dueStart, dueEnd);
        long dueSoonCount = dueSoon.stream()
                .filter(t -> !t.isCompleted())
                .count();

        Map<String, Long> result = new HashMap<>();
        result.put("created", createdCount);
        result.put("updated", updatedCount);
        result.put("completed", completedCount);
        result.put("dueSoon", dueSoonCount);
        return result;
    }
}
