package com.springboot.demo.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class WorkspaceInviteRequest {
    private String email;
    private int roleId;
}
