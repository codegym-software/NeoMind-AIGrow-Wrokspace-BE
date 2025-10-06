package com.springboot.demo.dto.task;

import java.util.List;

public record ReorderRequest(
        List<ReorderOp> operations,
        long expectedVersion
) {}
