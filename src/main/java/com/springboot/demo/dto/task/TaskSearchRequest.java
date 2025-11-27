package com.springboot.demo.dto.task;

import com.springboot.demo.model.enums.TaskPriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchRequest {
    private String q; // keyword search in title/description/tags
    private List<TaskPriority> priorities; // multi-select
    private List<String> assigneeIds; // multi-select
    private List<Integer> columnIds; // status (BoardColumn ids), multi-select
    private Integer sprintId; // optional explicit sprint
    private List<Integer> epicIds; // epic filter, multi-select
    private Boolean onlyActiveSprint; // default true: filter by active sprint if present
    private Boolean includeArchived; // default false
}