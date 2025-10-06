package com.springboot.demo.service;

import com.springboot.demo.model.Task;

import java.util.List;

public interface OrderStore {
    record Container(String projectId, Integer sprintId) {}
    record OrderState(List<Integer> orderedTaskIds, long version) {}

    OrderState load(Container c, List<Task> currentTasks);
    long appendReorderEvent(Container c, String userId, List<EventOp> ops, int attachToTaskId);

    // op biểu diễn “đặt taskId” vào trước/ sau / vị trí
    record EventOp(Integer taskId, Integer beforeTaskId, Integer afterTaskId, Integer atIndex) {}
}
