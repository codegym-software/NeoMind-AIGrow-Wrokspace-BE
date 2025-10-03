package com.springboot.demo.service.impl;

import com.springboot.demo.dto.task.*;
import com.springboot.demo.model.Task;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.BacklogService;
import com.springboot.demo.service.OrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BacklogServiceImpl implements BacklogService {

    private final TaskRepository taskRepo;
    private final OrderStore orderStore;

    @Override
    @Transactional(readOnly = true)
    public TaskListResponse listBacklog(String projectId) {
        List<Task> tasks = taskRepo
                .findByProjectIdAndSprintIdIsNullAndArchivedAtIsNullOrderByCreatedAtAscIdAsc(projectId);

        OrderStore.Container c = new OrderStore.Container(projectId, null);
        OrderStore.OrderState state = orderStore.load(c, tasks);

        Map<Integer, Task> byId = tasks.stream().collect(Collectors.toMap(Task::getId, t -> t));
        List<TaskItemDto> items = new ArrayList<>();
        for (Integer id : state.orderedTaskIds()) {
            Task t = byId.get(id);
            if (t != null) items.add(toDto(t));
        }
        // thêm những task chưa có trong order (hiếm)
        for (Task t : tasks) {
            if (state.orderedTaskIds().contains(t.getId())) continue;
            items.add(toDto(t));
        }
        return new TaskListResponse(items, state.version());
    }

    @Override
    @Transactional
    public ReorderResult reorderBacklog(String projectId, ReorderRequest req, String currentUserId) {
        // Load current state
        List<Task> tasks = taskRepo
                .findByProjectIdAndSprintIdIsNullAndArchivedAtIsNullOrderByCreatedAtAscIdAsc(projectId);
        OrderStore.Container c = new OrderStore.Container(projectId, null);
        OrderStore.OrderState state = orderStore.load(c, tasks);

        if (req.expectedVersion() != state.version()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order version changed. Please refresh.");
        }

        // Validate ops
        Set<Integer> containerIds = tasks.stream().map(Task::getId).collect(Collectors.toSet());
        if (req.operations() == null || req.operations().isEmpty()) {
            return new ReorderResult(false, state.version());
        }
        for (ReorderOp op : req.operations()) {
            if (!containerIds.contains(op.taskId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task not in backlog: " + op.taskId());
            if (op.beforeTaskId() != null && !containerIds.contains(op.beforeTaskId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "beforeTaskId not in backlog: " + op.beforeTaskId());
            if (op.afterTaskId() != null && !containerIds.contains(op.afterTaskId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "afterTaskId not in backlog: " + op.afterTaskId());
        }

        // Append event (đính vào comment của task đầu tiên)
        int attachTo = req.operations().get(0).taskId();
        List<OrderStore.EventOp> ops = req.operations().stream()
                .map(o -> new OrderStore.EventOp(o.taskId(), o.beforeTaskId(), o.afterTaskId(), o.atIndex()))
                .toList();

        long newVer = orderStore.appendReorderEvent(c, currentUserId, ops, attachTo);
        return new ReorderResult(true, newVer);
    }

    private static TaskItemDto toDto(Task t) {
        return new TaskItemDto(
                t.getId(), t.getProjectId(), t.getEpicId(), t.getSprintId(),
                t.getTitle(), t.getType(), t.getStatus(), t.getPriority(),
                t.getAssigneeId(), t.getReporterId(), t.getStoryPoints(),
                t.getDueDate(), t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}
