package com.springboot.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.demo.dto.task.TaskAttachmentResponse;
import com.springboot.demo.model.Task;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.upload.UploadBucket;
import com.springboot.demo.upload.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/attachments")
@RequiredArgsConstructor
public class TaskAttachmentController {

    private final TaskRepository taskRepo;
    private final UploadService uploadService;
    private final ObjectMapper objectMapper;

    // GET /api/projects/{projectId}/tasks/{taskId}/attachments
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TaskAttachmentResponse> list(@PathVariable String projectId, @PathVariable Long taskId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        return parseAttachments(task.getAttachments());
    }

    // POST /api/projects/{projectId}/tasks/{taskId}/attachments
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public TaskAttachmentResponse upload(
            @PathVariable String projectId,
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Upload file
        var stored = uploadService.store(file, UploadBucket.TASK_ATTACHMENT);

        // Create attachment object
        TaskAttachmentResponse newAttachment = new TaskAttachmentResponse();
        newAttachment.setId(System.currentTimeMillis()); // Use timestamp as ID
        newAttachment.setTaskId(taskId);
        newAttachment.setFilename(stored.filename());
        newAttachment.setOriginalFilename(file.getOriginalFilename());
        newAttachment.setContentType(stored.contentType());
        newAttachment.setFileSize(stored.size());
        newAttachment.setFileUrl(stored.url());
        newAttachment.setUploadedBy(jwt.getSubject());
        newAttachment.setUploadedAt(LocalDateTime.now());

        // Parse existing attachments
        List<TaskAttachmentResponse> attachments = parseAttachments(task.getAttachments());
        attachments.add(newAttachment);

        // Save back to JSON
        task.setAttachments(objectMapper.writeValueAsString(attachments));
        taskRepo.save(task);

        return newAttachment;
    }

    // DELETE /api/projects/{projectId}/tasks/{taskId}/attachments/{attachmentId}
    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void delete(
            @PathVariable String projectId,
            @PathVariable Long taskId,
            @PathVariable Long attachmentId
    ) throws IOException {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Parse and filter attachments
        List<TaskAttachmentResponse> attachments = parseAttachments(task.getAttachments());
        boolean removed = attachments.removeIf(a -> a.getId().equals(attachmentId));

        if (!removed) {
            throw new IllegalArgumentException("Attachment not found");
        }

        // Save back to JSON
        task.setAttachments(attachments.isEmpty() ? null : objectMapper.writeValueAsString(attachments));
        taskRepo.save(task);
    }

    private List<TaskAttachmentResponse> parseAttachments(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<TaskAttachmentResponse>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

