package com.springboot.demo.dto.timeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho việc đánh dấu Task hoàn thành
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompleteRequest {
    
    /**
     * Flag để đánh dấu completed hoặc uncomplete
     * true: Mark as Done (set completed_at = NOW, status = Done)
     * false: Unmark (set completed_at = null)
     */
    private Boolean completed;
}
