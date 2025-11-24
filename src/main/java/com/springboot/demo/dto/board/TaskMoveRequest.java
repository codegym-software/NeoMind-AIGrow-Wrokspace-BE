package com.springboot.demo.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để di chuyển Task (Kéo thả)
 * Theo chuẩn Jira: PATCH /api/tasks/{taskId}/move
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskMoveRequest {
    private Integer newColumnId;
    private Integer newOrderIndex;
    private Integer newSprintId;
    private boolean moveToBacklog;
}
