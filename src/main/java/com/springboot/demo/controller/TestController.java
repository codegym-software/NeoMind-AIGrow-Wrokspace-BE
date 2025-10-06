package com.springboot.demo.controller;

import com.springboot.demo.dto.project.ProjectCreateRequest;
import com.springboot.demo.dto.project.ProjectResponse;
import com.springboot.demo.model.Project;
import com.springboot.demo.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("NEOM-69 Test Controller is working!");
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectCreateRequest request) {
        // Tạo project đơn giản
        Project project = projectService.create(request);
        ProjectResponse response = new ProjectResponse(
            project.getId(),
            project.getWorkspaceId(),
            project.getName(),
            project.getDescription(),
            project.getStatus(),
            project.getColor(),
            project.getCreatedAt(),
            project.getUpdatedAt(),
            project.isArchived(),
            project.isActive()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects")
    public ResponseEntity<String> getProjects() {
        // Trả về message đơn giản
        return ResponseEntity.ok("Use POST /api/test/projects to create a project first!");
    }
}
