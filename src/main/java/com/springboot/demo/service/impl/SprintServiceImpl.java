package com.springboot.demo.service.impl;

import com.springboot.demo.dto.sprint.*;
import com.springboot.demo.model.Epic;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.Task;
import com.springboot.demo.model.enums.SprintStatus;
import com.springboot.demo.repository.EpicRepository;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.repository.SprintRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepo;
    private final ProjectRepository projectRepo;
    private final TaskRepository taskRepo;
    private final EpicRepository epicRepo;

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
        
        // ========== AUTO-SET EPIC START DATE ==========
        // Khi Sprint chuyển sang "active", auto-set startDate cho các Epics
        if (req.status() != null && req.status() == SprintStatus.active 
            && s.getStatus() != SprintStatus.active) {
            updateEpicStartDatesOnSprintStart(s);
            updateTaskStartDatesOnSprintStart(s);
        }
        
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
            
            // ========== AUTO-SET EPIC END DATE ==========
            // Khi Sprint complete, auto-set endDate cho các Epics
            updateEpicEndDatesOnSprintComplete(s);
        }
        
        if (req.status() != null) s.setStatus(req.status());

        sprintRepo.save(s);
        return toDto(s);
    }
    
    /**
     * AUTO-SET EPIC DATES - Khi Sprint starts (status = active)
     * Logic: Set epic.startDate = sprint.startDate cho tất cả Epics có tasks trong Sprint này
     */
    private void updateEpicStartDatesOnSprintStart(Sprint sprint) {
        if (sprint.getStartDate() == null) return;
        
        // Lấy tất cả tasks trong sprint này
        List<Task> sprintTasks = taskRepo.findBySprintIdAndArchivedAtIsNull(sprint.getId());
        
        // Lấy danh sách Epic IDs unique
        Set<Integer> epicIds = sprintTasks.stream()
            .filter(task -> task.getEpicId() != null)
            .map(Task::getEpicId)
            .collect(Collectors.toSet());
        
        if (epicIds.isEmpty()) return;
        
        // Update epic.startDate nếu chưa có hoặc sprint.startDate sớm hơn
        List<Epic> epics = epicRepo.findAllById(epicIds);
        epics.forEach(epic -> {
            // Chỉ set nếu epic chưa có startDate HOẶC sprint start sớm hơn
            if (epic.getStartDate() == null || sprint.getStartDate().isBefore(epic.getStartDate())) {
                epic.setStartDate(sprint.getStartDate());
                System.out.println("✅ Auto-set Epic[" + epic.getId() + "] startDate = " + sprint.getStartDate());
            }
        });
        
        if (!epics.isEmpty()) {
            epicRepo.saveAll(epics);
        }
    }
    
    /**
     * AUTO-UPDATE TASK START DATES - Khi Sprint chuyển sang active
     * Logic: Sửa startDate của tất cả tasks trong Sprint từ ngày mặc định thành sprint.startDate
     */
    private void updateTaskStartDatesOnSprintStart(Sprint sprint) {
        if (sprint.getStartDate() == null) return;
        
        // Lấy tất cả tasks trong sprint này
        List<Task> sprintTasks = taskRepo.findBySprintIdAndArchivedAtIsNull(sprint.getId());
        
        if (sprintTasks.isEmpty()) return;
        
        // Update startDate của tất cả tasks thành sprint.startDate
        sprintTasks.forEach(task -> {
            task.setStartDate(sprint.getStartDate());
            System.out.println("✅ Auto-update Task[" + task.getId() + "] startDate = " + sprint.getStartDate());
        });
        
        taskRepo.saveAll(sprintTasks);
    }
    
    /**
     * AUTO-SET EPIC DATES - Khi Sprint completes (status = completed)
     * Logic: Set epic.endDate = sprint.endDate cho tất cả Epics có tasks trong Sprint này
     */
    private void updateEpicEndDatesOnSprintComplete(Sprint sprint) {
        if (sprint.getEndDate() == null) return;
        
        // Lấy tất cả tasks trong sprint này
        List<Task> sprintTasks = taskRepo.findBySprintIdAndArchivedAtIsNull(sprint.getId());
        
        // Lấy danh sách Epic IDs unique
        Set<Integer> epicIds = sprintTasks.stream()
            .filter(task -> task.getEpicId() != null)
            .map(Task::getEpicId)
            .collect(Collectors.toSet());
        
        if (epicIds.isEmpty()) return;
        
        // Update epic.endDate nếu chưa có hoặc sprint.endDate muộn hơn
        List<Epic> epics = epicRepo.findAllById(epicIds);
        epics.forEach(epic -> {
            // Chỉ set nếu epic chưa có endDate HOẶC sprint end muộn hơn
            if (epic.getEndDate() == null || sprint.getEndDate().isAfter(epic.getEndDate())) {
                epic.setEndDate(sprint.getEndDate());
                System.out.println("✅ Auto-set Epic[" + epic.getId() + "] endDate = " + sprint.getEndDate());
            }
        });
        
        if (!epics.isEmpty()) {
            epicRepo.saveAll(epics);
        }
    }

    @Override
    @Transactional
    public void delete(Integer sprintId) {
        Sprint s = sprintRepo.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
        sprintRepo.delete(s);
    }
}
