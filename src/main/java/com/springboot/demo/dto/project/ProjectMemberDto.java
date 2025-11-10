package com.springboot.demo.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDto {
    private Integer id;
    private String projectId;
    private String userId;
    private Integer roleId;
    private boolean owner;
    private String email;
    private String displayName;
}
