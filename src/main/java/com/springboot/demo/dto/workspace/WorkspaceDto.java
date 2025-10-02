package com.springboot.demo.dto.workspace;

import com.springboot.demo.model.Workspace;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceDto {
    private String id;
    private String name;
    private String description;
    private String companyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime archivedAt;

    public static WorkspaceDto fromEntity(Workspace w) {
        return WorkspaceDto.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .companyId(w.getCompanyId())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .archivedAt(w.getArchivedAt())
                .build();
    }
}
