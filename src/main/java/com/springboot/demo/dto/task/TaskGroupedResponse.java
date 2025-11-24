package com.springboot.demo.dto.task;

import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TaskGroupedResponse {
    private String groupBy;
    private List<TaskGroup> groups;
}