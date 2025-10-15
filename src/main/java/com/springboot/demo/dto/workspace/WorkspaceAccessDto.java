package com.springboot.demo.dto.workspace;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class WorkspaceAccessDto {
    private String workspaceId;
    private Set<String> workspacePermissions;
    private List<ProjectAccess> projects;

    @Data @Builder
    public static class ProjectAccess {
        private String projectId;
        private Set<String> projectPermissions;
    }
}
