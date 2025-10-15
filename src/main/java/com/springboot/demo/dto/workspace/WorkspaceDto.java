package com.springboot.demo.dto.workspace;

import com.springboot.demo.model.Workspace;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WorkspaceDto {
    private String id;
    private String companyId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime archivedAt;

    public static WorkspaceDto fromEntity(Workspace w) {
        return WorkspaceDto.builder()
                .id(w.getId())
                .companyId(w.getCompanyId())
                .name(w.getName())
                .description(w.getDescription())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .archivedAt(w.getArchivedAt())
                .build();
    }
}
