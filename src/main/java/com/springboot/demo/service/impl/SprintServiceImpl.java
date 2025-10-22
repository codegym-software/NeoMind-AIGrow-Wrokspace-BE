package com.springboot.demo.service.impl;

import com.springboot.demo.dto.sprint.*;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.repository.ProjectRepository;
import com.springboot.demo.repository.SprintRepository;
import com.springboot.demo.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepo;
    private final ProjectRepository projectRepo;

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
        if (req.status() != null) s.setStatus(req.status());
        if (req.description() != null) s.setDescription(req.description().trim());
        if (req.isBacklog() != null) s.setIsBacklog(req.isBacklog());

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
