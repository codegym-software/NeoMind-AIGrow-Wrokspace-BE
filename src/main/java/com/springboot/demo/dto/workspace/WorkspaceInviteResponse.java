package com.springboot.demo.dto.workspace;

import com.springboot.demo.model.WorkspaceMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class WorkspaceInviteResponse {
    private String status;
    private WorkspaceMember member;
}
