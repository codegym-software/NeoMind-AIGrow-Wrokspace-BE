package com.springboot.demo.dto.task;

public record ReorderOp(
        Integer taskId,
        Integer beforeTaskId, // XOR
        Integer afterTaskId,  // XOR
        Integer atIndex       // fallback
) {}
