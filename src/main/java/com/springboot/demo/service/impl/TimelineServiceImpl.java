package com.springboot.demo.service.impl;

import com.springboot.demo.dto.timeline.SprintDto;
import com.springboot.demo.dto.timeline.TaskCompleteRequest;
import com.springboot.demo.dto.timeline.TimelineItemDto;
import com.springboot.demo.dto.timeline.TimelineResponse;
import com.springboot.demo.dto.timeline.TimelineUpdateRequest;
import com.springboot.demo.exception.AccessDeniedException;
import com.springboot.demo.exception.ResourceNotFoundException;
import com.springboot.demo.model.BoardColumn;
import com.springboot.demo.model.Epic;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.Task;
import com.springboot.demo.repository.BoardColumnRepository;
import com.springboot.demo.repository.EpicRepository;
import com.springboot.demo.repository.ProjectMemberRepository;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.repository.SprintRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineServiceImpl implements TimelineService {
    
    private final TaskRepository taskRepo;
    private final EpicRepository epicRepo;
    private final SprintRepository sprintRepo;
    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository projectMemberRepo;
    private final BoardColumnRepository boardColumnRepo;
    
    private void checkProjectAccess(String projectId, String userId) {
        if (!projectRepo.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        if (!projectMemberRepo.existsByProjectIdAndUserId(projectId, userId)) {
            throw new AccessDeniedException("Access Denied. You are not a member of this project.");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public TimelineResponse getTimeline(String projectId, String userId) {
        checkProjectAccess(projectId, userId);
        
        // 1. Lấy danh sách items (Epic + Task) - Logic cũ
        List<TimelineItemDto> items = getTimelineItems(projectId);
        
        // 2. Lấy danh sách Sprints - Logic mới
        List<SprintDto> sprints = getSprintsForTimeline(projectId);
        
        // 3. Trả về TimelineResponse
        return new TimelineResponse(items, sprints);
    }
    
    /**
     * Helper: Lấy danh sách items (Epic + Task) - Logic cũ được tách ra
     */
    private List<TimelineItemDto> getTimelineItems(String projectId) {
        List<TimelineItemDto> result = new ArrayList<>();
        
        // 1. Lấy tất cả Epics của project
        List<Epic> epics = epicRepo.findByProjectId(projectId);
        
        // 2. Lấy tất cả Tasks (chưa archive) của project
        List<Task> tasks = taskRepo.findByProjectIdAndArchivedAtIsNull(projectId);
        
        // 3. Lấy tất cả Sprints của project để dùng cho soft inheritance
        Map<Integer, Sprint> sprintMap = sprintRepo.findByProjectIdOrderByStartDateAsc(projectId)
            .stream()
            .collect(Collectors.toMap(Sprint::getId, s -> s));
        
        // 4. Map Epics sang TimelineItemDto
        for (Epic epic : epics) {
            TimelineItemDto item = new TimelineItemDto();
            item.setId("epic-" + epic.getId());
            item.setText(epic.getTitle());
            item.setStartDate(epic.getStartDate());
            item.setEndDate(epic.getEndDate());
            item.setParent(null); // Epic không có parent
            item.setType("epic");
            item.setProjectId(projectId);
            
            // Tính progress cho Epic = % tasks Done
            long totalTasks = tasks.stream()
                .filter(t -> epic.getId().equals(t.getEpicId()))
                .count();
            long doneTasks = tasks.stream()
                .filter(t -> epic.getId().equals(t.getEpicId()))
                .filter(Task::isCompleted)
                .count();
            
            double progress = totalTasks == 0 ? 0.0 : (double) doneTasks / totalTasks;
            item.setProgress(progress);
            
            result.add(item);
        }
        
        // 5. Map Tasks sang TimelineItemDto với SOFT INHERITANCE
        for (Task task : tasks) {
            TimelineItemDto item = new TimelineItemDto();
            item.setId("task-" + task.getId());
            item.setText(task.getTitle());
            item.setType("task");
            item.setProjectId(projectId);
            item.setSprintId(task.getSprintId());
            
            // SOFT INHERITANCE LOGIC cho start_date
            LocalDate startDate = task.getStartDate(); // Ưu tiên task.start_date
            if (startDate == null && task.getSprintId() != null) {
                Sprint sprint = sprintMap.get(task.getSprintId());
                if (sprint != null) {
                    startDate = sprint.getStartDate(); // Fallback sang sprint.start_date
                }
            }
            item.setStartDate(startDate);
            
            // SOFT INHERITANCE LOGIC cho due_date (end_date)
            LocalDate dueDate = task.getDueDate(); // Ưu tiên task.due_date
            if (dueDate == null && task.getSprintId() != null) {
                Sprint sprint = sprintMap.get(task.getSprintId());
                if (sprint != null) {
                    dueDate = sprint.getEndDate(); // Fallback sang sprint.end_date
                }
            }
            item.setEndDate(dueDate);
            
            // Parent = Epic ID (nếu có)
            if (task.getEpicId() != null) {
                item.setParent("epic-" + task.getEpicId());
            } else {
                item.setParent(null);
            }
            
            // Progress: Done = 1.0 (100%), còn lại = 0.0
            item.setProgress(task.isCompleted() ? 1.0 : 0.0);
            
            // Status
            item.setStatus(task.getStatusColumn() != null ? task.getStatusColumn().getName() : null);
            
            // Completed date (để so sánh kế hoạch vs thực tế)
            if (task.getEndDate() != null) {
                item.setCompletedDate(task.getEndDate().toLocalDate());
            }
            
            result.add(item);
        }
        
        // 6. Sort: Epic trước, Task sau, theo start_date
        result.sort((a, b) -> {
            // Epic lên trước
            if (!a.getType().equals(b.getType())) {
                return "epic".equals(a.getType()) ? -1 : 1;
            }
            
            // Cùng loại thì sort theo start_date
            LocalDate dateA = a.getStartDate();
            LocalDate dateB = b.getStartDate();
            
            if (dateA == null && dateB == null) return 0;
            if (dateA == null) return 1; // null xuống cuối
            if (dateB == null) return -1;
            
            return dateA.compareTo(dateB);
        });
        
        return result;
    }
    
    /**
     * Helper: Lấy danh sách Sprints cho Timeline
     * Chỉ lấy các Sprint có đầy đủ start_date và end_date
     */
    private List<SprintDto> getSprintsForTimeline(String projectId) {
        // Query Sprints có đầy đủ start_date và end_date
        List<Sprint> sprints = sprintRepo.findByProjectIdOrderByStartDateAsc(projectId)
            .stream()
            .filter(s -> s.getStartDate() != null && s.getEndDate() != null)
            .collect(Collectors.toList());
        
        // Map sang SprintDto
        return sprints.stream()
            .map(s -> new SprintDto(
                s.getId(),
                s.getName(),
                s.getStartDate(),
                s.getEndDate(),
                s.getStatus()
            ))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public TimelineItemDto updateTaskTimeline(Long taskId, TimelineUpdateRequest request, String userId) {
        Task task = taskRepo.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        checkProjectAccess(task.getProjectId(), userId);
        
        // Update start_date và due_date (chỉ update nếu không null)
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        
        taskRepo.save(task);
        
        // Trả về TimelineItemDto
        return convertTaskToTimelineItem(task);
    }
    
    @Override
    @Transactional
    public TimelineItemDto updateEpicTimeline(Integer epicId, TimelineUpdateRequest request, String userId) {
        Epic epic = epicRepo.findById(epicId)
            .orElseThrow(() -> new ResourceNotFoundException("Epic not found with id: " + epicId));
        
        checkProjectAccess(epic.getProjectId(), userId);
        
        // Update start_date và end_date (chỉ update nếu không null)
        if (request.getStartDate() != null) {
            epic.setStartDate(request.getStartDate());
        }
        
        if (request.getDueDate() != null) {
            epic.setEndDate(request.getDueDate());
        }
        
        epicRepo.save(epic);
        
        // Trả về TimelineItemDto
        return convertEpicToTimelineItem(epic);
    }
    
    @Override
    @Transactional
    public TimelineItemDto completeTask(Long taskId, TaskCompleteRequest request, String userId) {
        Task task = taskRepo.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        checkProjectAccess(task.getProjectId(), userId);
        
        Boolean completed = request.getCompleted() != null ? request.getCompleted() : true;
        
        if (completed) {
            // Mark as Done
            task.setEndDate(LocalDateTime.now());
            
            // Tìm và set status column = "Done"
            List<BoardColumn> columns = boardColumnRepo.findByProjectIdAndName(task.getProjectId(), "Done");
            if (!columns.isEmpty()) {
                task.setStatusColumn(columns.get(0));
            }
        } else {
            // Unmark
            task.setEndDate(null);
            
            // Optional: Đưa về status "In Progress" hoặc "To Do"
            List<BoardColumn> columns = boardColumnRepo.findByProjectIdAndName(task.getProjectId(), "In Progress");
            if (!columns.isEmpty()) {
                task.setStatusColumn(columns.get(0));
            }
        }
        
        // LƯU Ý: KHÔNG thay đổi due_date - giữ nguyên kế hoạch để so sánh
        
        taskRepo.save(task);
        
        return convertTaskToTimelineItem(task);
    }
    
    /**
     * Helper: Convert Task entity sang TimelineItemDto
     */
    private TimelineItemDto convertTaskToTimelineItem(Task task) {
        TimelineItemDto item = new TimelineItemDto();
        item.setId("task-" + task.getId());
        item.setText(task.getTitle());
        item.setType("task");
        item.setProjectId(task.getProjectId());
        item.setSprintId(task.getSprintId());
        
        // Apply soft inheritance nếu cần
        LocalDate startDate = task.getStartDate();
        LocalDate dueDate = task.getDueDate();
        
        if (task.getSprintId() != null) {
            Sprint sprint = sprintRepo.findById(task.getSprintId()).orElse(null);
            if (sprint != null) {
                if (startDate == null) startDate = sprint.getStartDate();
                if (dueDate == null) dueDate = sprint.getEndDate();
            }
        }
        
        item.setStartDate(startDate);
        item.setEndDate(dueDate);
        
        if (task.getEpicId() != null) {
            item.setParent("epic-" + task.getEpicId());
        }
        
        item.setProgress(task.isCompleted() ? 1.0 : 0.0);
        item.setStatus(task.getStatusColumn() != null ? task.getStatusColumn().getName() : null);
        
        if (task.getEndDate() != null) {
            item.setCompletedDate(task.getEndDate().toLocalDate());
        }
        
        return item;
    }
    
    /**
     * Helper: Convert Epic entity sang TimelineItemDto
     */
    private TimelineItemDto convertEpicToTimelineItem(Epic epic) {
        TimelineItemDto item = new TimelineItemDto();
        item.setId("epic-" + epic.getId());
        item.setText(epic.getTitle());
        item.setType("epic");
        item.setProjectId(epic.getProjectId());
        item.setStartDate(epic.getStartDate());
        item.setEndDate(epic.getEndDate());
        item.setParent(null); // Epic không có parent
        
        // Calculate progress from tasks (đơn giản hóa: chỉ đếm số tasks)
        long totalTasks = taskRepo.countByEpicId(epic.getId());
        if (totalTasks > 0) {
            // Tính toán chính xác progress cần query thêm, tạm thời set 0.0
            item.setProgress(0.0);
        } else {
            item.setProgress(0.0);
        }
        
        return item;
    }
}
