package com.springboot.demo.service.impl;

import com.springboot.demo.dto.epic.*;
import com.springboot.demo.exception.AccessDeniedException;
import com.springboot.demo.exception.ResourceNotFoundException;
import com.springboot.demo.model.Epic;
import com.springboot.demo.model.Task;
import com.springboot.demo.repository.EpicRepository;
import com.springboot.demo.repository.ProjectMemberRepository;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.EpicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.springboot.demo.dto.task.TaskResponse;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EpicServiceImpl implements EpicService {
    private final EpicRepository epicRepo;
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
    public EpicDto create(String projectId, EpicCreateRequest req, String currentUserId) {
        checkProjectAccess(projectId, currentUserId);
        Epic e = new Epic();
        e.setProjectId(projectId);
        e.setTitle(req.getTitle());
        e.setDescription(req.getDescription());
        e.setStartDate(req.getStartDate());
        e.setEndDate(req.getEndDate());
        Epic saved = epicRepo.save(e);
        return toDtoWithStats(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EpicListResponse listByProject(String projectId, String currentUserId, Pageable pageable) {
        checkProjectAccess(projectId, currentUserId);
        Page<EpicDto> page = epicRepo.findByProjectId(projectId, pageable).map(this::toDtoWithStats);
        long totalEpics = page.getTotalElements();
        long totalTasks = taskRepo.countByProjectIdAndArchivedAtIsNull(projectId);
        return new EpicListResponse(totalEpics, totalTasks, page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EpicDto> search(String projectId, String currentUserId, EpicSearchRequest req, Pageable pageable) {
        checkProjectAccess(projectId, currentUserId);
        Specification<Epic> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("projectId"), projectId));
            if (req.getQ() != null && !req.getQ().isBlank()) {
                String pattern = "%" + req.getQ().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            }
            LocalDate sFrom = req.getStartDateFrom();
            LocalDate sTo = req.getStartDateTo();
            if (sFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), sFrom));
            if (sTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), sTo));
            LocalDate eFrom = req.getEndDateFrom();
            LocalDate eTo = req.getEndDateTo();
            if (eFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), eFrom));
            if (eTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), eTo));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Epic> page = epicRepo.findAll(spec, pageable);
        return page.map(this::toDtoWithStats);
    }

    @Override
    @Transactional(readOnly = true)
    public EpicDto get(String projectId, Integer epicId, String currentUserId) {
        checkProjectAccess(projectId, currentUserId);
        Epic e = epicRepo.findById(epicId)
                .filter(x -> projectId.equals(x.getProjectId()))
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found with id: " + epicId));
        return toDtoWithStats(e);
    }

    @Override
    @Transactional
    public EpicDto update(String projectId, Integer epicId, EpicUpdateRequest req, String currentUserId) {
        checkProjectAccess(projectId, currentUserId);
        Epic e = epicRepo.findById(epicId)
                .filter(x -> projectId.equals(x.getProjectId()))
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found with id: " + epicId));
        if (req.getTitle() != null) e.setTitle(req.getTitle());
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        if (req.getStartDate() != null) e.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) e.setEndDate(req.getEndDate());
        Epic saved = epicRepo.save(e);
        return toDtoWithStats(saved);
    }

    @Override
    @Transactional
    public void delete(String projectId, Integer epicId, String currentUserId) {
        checkProjectAccess(projectId, currentUserId);
        Epic e = epicRepo.findById(epicId)
                .filter(x -> projectId.equals(x.getProjectId()))
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found with id: " + epicId));
        long count = taskRepo.countByEpicId(epicId);
        if (count > 0) {
            throw new AccessDeniedException("Epic cannot be deleted because it has tasks");
        }
        epicRepo.delete(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasks(String projectId, Integer epicId, String currentUserId, Pageable pageable) {
        checkProjectAccess(projectId, currentUserId);
        Epic e = epicRepo.findById(epicId)
                .filter(x -> projectId.equals(x.getProjectId()))
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found with id: " + epicId));
        Page<Task> page = taskRepo.findByEpicIdAndArchivedAtIsNull(epicId, pageable);
        return page.map(this::toTaskResponse);
    }

    private EpicDto toDtoWithStats(Epic e) {
        List<Task> tasks = taskRepo.findByProjectIdAndArchivedAtIsNull(e.getProjectId()).stream()
                .filter(t -> t.getEpicId() != null && t.getEpicId().equals(e.getId()))
                .toList();
        int total = tasks.size();
        int done = (int) tasks.stream()
                .filter(t -> t.getStatusColumn() != null && "Done".equalsIgnoreCase(t.getStatusColumn().getName()))
                .count();
        double percent = total == 0 ? 0.0 : (done * 100.0) / total;
        boolean overdue = e.getEndDate() != null && e.getEndDate().isBefore(LocalDate.now()) && done < total;
        return new EpicDto(
                e.getId(),
                e.getProjectId(),
                e.getTitle(),
                e.getDescription(),
                e.getStartDate(),
                e.getEndDate(),
                total,
                done,
                percent,
                overdue
        );
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
        r.setEpicId(task.getEpicId());
        r.setSprintId(task.getSprintId() != null ? task.getSprintId().toString() : null);
        r.setTags(task.getTags());
        r.setOrderIndex(task.getOrderIndex());
        r.setProjectId(task.getProjectId());
        return r;
    }
}
