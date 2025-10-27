package com.springboot.demo.controller;

import com.springboot.demo.dto.sprint.*;
import com.springboot.demo.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    @PreAuthorize("@gate.allows('project:sprint:create', authentication, #projectId)")
    public SprintDto create(@PathVariable String projectId, @Valid @RequestBody CreateSprintReq req) {
        return sprintService.create(projectId, req);
    }

    @GetMapping
    @PreAuthorize("@gate.allows('project:sprint:read', authentication, #projectId)")
    public List<SprintDto> listByProject(@PathVariable String projectId) {
        return sprintService.listByProject(projectId);
    }

    @PatchMapping("/{sprintId}")
    @PreAuthorize("@gate.allows('project:sprint:update', authentication, #projectId)")
    public SprintDto update(@PathVariable String projectId, @PathVariable Integer sprintId, @Valid @RequestBody UpdateSprintReq req) {
        return sprintService.update(sprintId, req);
    }

    @DeleteMapping("/{sprintId}")
    @PreAuthorize("@gate.allows('project:sprint:delete', authentication, #projectId)")
    public void delete(@PathVariable String projectId, @PathVariable Integer sprintId) {
        sprintService.delete(sprintId);
    }
}
