package com.springboot.demo.dto.task;

public record ReorderResult(
        boolean applied,
        long newVersion
) {}
