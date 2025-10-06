package com.springboot.demo.dto.task;

import java.util.List;

public record TaskListResponse(
        List<TaskItemDto> items,
        long orderVersion // = last event comment id
) {}
