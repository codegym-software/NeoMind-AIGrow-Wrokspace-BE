package com.springboot.demo.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO cho Board - Bao gồm cả columns và tasks
 * Đây là response chính cho API GET /api/projects/{projectId}/board
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {
    private Integer id;
    private String projectId;
    private String name;
    private String description;
    private Boolean isDefault;
    private List<BoardColumnResponse> columns;
    
    // Sprint info - Board luôn hiện tasks của sprint đang active
    private Integer activeSprintId;
    private String activeSprintName;
}
