package com.springboot.demo.service.impl;


import com.springboot.demo.dto.board.TaskMoveRequest;
import com.springboot.demo.dto.task.TaskSearchRequest;
import com.springboot.demo.dto.task.TaskGroupedResponse;
import com.springboot.demo.dto.task.TaskGroup;
import com.springboot.demo.dto.task.TaskResponse;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;

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
        
        // Set default priority if not provided
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.medium);
        }
        
        // Set default orderIndex if not provided
        if (task.getOrderIndex() == null) {
            task.setOrderIndex(0);
        }
        
        // Remember if task was created from Board (has statusColumn)
        boolean isCreatedFromBoard = task.getStatusColumn() != null && task.getStatusColumn().getId() != null;

        // Auto-assign default column if not set (To Do / first column)
        if (!isCreatedFromBoard) {
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
        
        // Auto-assign to active sprint ONLY if created from Board (not from Backlog/Sprint page)
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

        // --- Logic PATCH (vá lỗi) ---
        if (task.getTitle() != null) {
            existing.setTitle(task.getTitle());
        }
        if (task.getDescription() != null) {
            existing.setDescription(task.getDescription());
        }
        // CRITICAL: Update statusColumn - Check both null and valid ID
        if (task.getStatusColumn() != null && task.getStatusColumn().getId() != null) {
            // Fetch the actual BoardColumn entity from database
            Integer columnId = task.getStatusColumn().getId();
            BoardColumn statusColumn = boardColumnRepo.findById(columnId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Status column not found with id: " + columnId + 
                        ". Please refresh the page to reload the latest columns."
                    ));
            existing.setStatusColumn(statusColumn);
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
        
        
        // Handle assigneeId: null check but also handle empty string to unassign
        if (task.getAssigneeId() != null) {
            // Empty string means unassign (set to null)
            existing.setAssigneeId(task.getAssigneeId().isEmpty() ? null : task.getAssigneeId());
        }
        
        
        if (task.getStatusColumn() == null || task.getStatusColumn().getId() == null) {
            // Không có statusColumn → Đây là SPRINT drag & drop
            if (task.getOrderIndex() != null) {
                // Sprint drag: cho phép update sprintId (kể cả null để về backlog)
                existing.setSprintId(task.getSprintId());
            } else if (task.getSprintId() != null) {
                // Regular update với sprintId không null
                existing.setSprintId(task.getSprintId());
            }
        }
        // Nếu có statusColumn → Đây là BOARD drag, KHÔNG update sprintId (giữ nguyên)
        
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

    @Override
    @Transactional
    public Task assign(Long taskId, String assigneeId, String currentUserId) {
        Task existing = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        checkProjectAccess(existing.getProjectId(), currentUserId);

        existing.setAssigneeId(assigneeId == null || assigneeId.isEmpty() ? null : assigneeId);
        return taskRepo.save(existing);
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
    public Page<Task> search(String projectId, String currentUserId, TaskSearchRequest req, Pageable pageable) {
        checkProjectAccess(projectId, currentUserId);

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("projectId"), projectId));

            Boolean includeArchived = req.getIncludeArchived();
            if (includeArchived == null || !includeArchived) {
                predicates.add(cb.isNull(root.get("archivedAt")));
            }

            String q = req.getQ();
            if (q != null) {
                String trimmed = q.trim();
                if (!trimmed.isEmpty()) {
                    String pattern = "%" + trimmed.toLowerCase() + "%";
                    Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                    Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                    Predicate tagsLike = cb.like(cb.lower(root.get("tags")), pattern);
                    predicates.add(cb.or(titleLike, descLike, tagsLike));
                }
            }

            if (req.getPriorities() != null && !req.getPriorities().isEmpty()) {
                predicates.add(root.get("priority").in(req.getPriorities()));
            }
            if (req.getAssigneeIds() != null && !req.getAssigneeIds().isEmpty()) {
                predicates.add(root.get("assigneeId").in(req.getAssigneeIds()));
            }
            if (req.getColumnIds() != null && !req.getColumnIds().isEmpty()) {
                predicates.add(root.get("statusColumn").get("id").in(req.getColumnIds()));
            }

            Integer sprintId = req.getSprintId();
            if (sprintId != null) {
                predicates.add(cb.equal(root.get("sprintId"), sprintId));
            } else {
                Boolean onlyActive = req.getOnlyActiveSprint();
                if (onlyActive != null && onlyActive) {
                    Optional<Sprint> activeSprint = sprintRepo.findByProjectIdAndStatus(projectId, SprintStatus.active);
                    activeSprint.ifPresent(s -> predicates.add(cb.equal(root.get("sprintId"), s.getId())));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return taskRepo.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskGroupedResponse groupBy(String projectId, String groupBy, String currentUserId) {
        checkProjectAccess(projectId, currentUserId);
        List<Task> tasks = taskRepo.findByProjectIdAndArchivedAtIsNull(projectId);

        java.util.Map<String, java.util.List<Task>> grouped = tasks.stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> resolveGroupKey(t, groupBy)));

        java.util.List<TaskGroup> groups = grouped.entrySet().stream()
                .map(e -> new TaskGroup(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .sorted(java.util.Comparator
                                        .comparing(Task::getOrderIndex, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                                        .thenComparing(Task::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                                )
                                .map(this::toTaskResponse)
                                .toList()
                ))
                .sorted(java.util.Comparator.comparing(TaskGroup::getCount).reversed()
                        .thenComparing(TaskGroup::getKey, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .toList();

        return new TaskGroupedResponse(groupBy, groups);
    }

    private String resolveGroupKey(Task t, String groupBy) {
        String gb = groupBy == null ? "status" : groupBy.trim().toLowerCase();
        switch (gb) {
            case "assignee":
                return t.getAssigneeId() != null ? t.getAssigneeId() : "UNASSIGNED";
            case "priority":
                return t.getPriority() != null ? t.getPriority().name() : "UNSPECIFIED";
            case "status":
                return t.getStatusColumn() != null ? t.getStatusColumn().getName() : "UNSPECIFIED";
            case "sprint":
                return t.getSprintId() != null ? t.getSprintId().toString() : "BACKLOG";
            default:
                return "UNSPECIFIED";
        }
    }

    private TaskResponse toTaskResponse(Task task) {
        TaskResponse r = new TaskResponse();
        r.setId(task.getId());
        r.setTitle(task.getTitle());
        r.setDescription(task.getDescription());
        r.setStatus(task.getStatusColumn() != null ? task.getStatusColumn().getName() : null);
        r.setPriority(task.getPriority() != null ? task.getPriority().name() : null);
        r.setDueDate(task.getDueDate());
        r.setEstimatedHours(task.getEstimatedHours());
        r.setAssigneeId(task.getAssigneeId());
        r.setSprintId(task.getSprintId() != null ? task.getSprintId().toString() : null);
        r.setTags(task.getTags());
        r.setOrderIndex(task.getOrderIndex());
        r.setProjectId(task.getProjectId());
        return r;
    }
}