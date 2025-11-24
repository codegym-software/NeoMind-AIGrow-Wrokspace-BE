package com.springboot.demo.dto.task;

import java.time.LocalDateTime;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TaskAttachmentResponse {
    private Long id;
    private Long taskId;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String fileUrl;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
