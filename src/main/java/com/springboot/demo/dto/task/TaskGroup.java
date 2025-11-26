package com.springboot.demo.dto.task;

import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TaskGroup {
    private String key;
    private Integer count;
    private List<TaskResponse> tasks;
}