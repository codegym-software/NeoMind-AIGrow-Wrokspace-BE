package com.springboot.demo.service.impl;

import com.springboot.demo.dto.sprint.*;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.Task;
import com.springboot.demo.model.enums.SprintStatus;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.repository.SprintRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepo;
    private final ProjectRepository projectRepo;
    private final TaskRepository taskRepo;

    private SprintDto toDto(Sprint s) {
        return new SprintDto(
                s.getId(),
                s.getProjectId(),
                s.getName(),
                s.getStartDate(),
                s.getEndDate(),
                s.getStatus(),
                s.getDescription(),
                s.getIsBacklog(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public SprintDto create(String projectId, CreateSprintReq req) {
        projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Sprint s = Sprint.builder()
                .projectId(projectId)
                .name(req.name().trim())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .status(req.status())
                .description(req.description())
                .isBacklog(req.isBacklog() != null ? req.isBacklog() : false)
                .build();

        sprintRepo.save(s);
        return toDto(s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintDto> listByProject(String projectId) {
        return sprintRepo.findByProjectIdOrderByStartDateAsc(projectId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SprintDto update(Integer sprintId, UpdateSprintReq req) {
        Sprint s = sprintRepo.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));

        if (req.name() != null && !req.name().isBlank()) s.setName(req.name().trim());
        if (req.startDate() != null) s.setStartDate(req.startDate());
        if (req.endDate() != null) s.setEndDate(req.endDate());
        if (req.description() != null) s.setDescription(req.description().trim());
        if (req.isBacklog() != null) s.setIsBacklog(req.isBacklog());
        
        // ========== LOGIC COMPLETE SPRINT ==========
        // Khi status chuyển sang "completed", tự động chuyển tasks chưa Done về Backlog
        if (req.status() != null && req.status() == SprintStatus.completed 
            && s.getStatus() != SprintStatus.completed) {
            
            // Lấy tất cả tasks của sprint này
            List<Task> sprintTasks = taskRepo.findBySprintIdAndArchivedAtIsNull(sprintId);
            
            // Lọc ra các task chưa Done (statusColumn.name != "Done")
            List<Task> incompleteTasks = sprintTasks.stream()
                .filter(task -> {
                    if (task.getStatusColumn() == null) return true; // Task không có status column
                    String statusName = task.getStatusColumn().getName();
                    // Chỉ giữ lại task nếu KHÔNG phải Done/Completed
                    return !"Done".equalsIgnoreCase(statusName) 
                        && !"Completed".equalsIgnoreCase(statusName);
                })
                .toList();
            
            // Chuyển các task chưa Done về Backlog (sprintId = null)
            if (!incompleteTasks.isEmpty()) {
                incompleteTasks.forEach(task -> task.setSprintId(null));
                taskRepo.saveAll(incompleteTasks);
                System.out.println("✅ Moved " + incompleteTasks.size() + " incomplete tasks to Backlog");
            }
        }
        
        if (req.status() != null) s.setStatus(req.status());

        sprintRepo.save(s);
        return toDto(s);
    }

    @Override
    @Transactional
    public void delete(Integer sprintId) {
        Sprint s = sprintRepo.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
        sprintRepo.delete(s);
    }
}
