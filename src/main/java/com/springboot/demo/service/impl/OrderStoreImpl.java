package com.springboot.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.demo.model.Comment;
import com.springboot.demo.model.Task;
import com.springboot.demo.repository.CommentRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.OrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderStoreImpl implements OrderStore {
    private static final String PREFIX = "[[SYS_ORDER]] ";

    private final CommentRepository commentRepo;
    private final TaskRepository taskRepo;
    private final ObjectMapper om;

    @Override
    public OrderState load(Container c, List<Task> currentTasks) {
        // Base order = theo createdAt, id (stable)
        List<Integer> base = currentTasks.stream()
                .sorted(Comparator
                        .comparing(Task::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getId))
                .map(Task::getId).toList();

        if (base.isEmpty()) return new OrderState(base, 0L);

        // Lấy tất cả taskId của project để gom event (move qua lại cần biết)
        List<Integer> allTaskIds = taskRepo.findByProjectIdAndArchivedAtIsNull(c.projectId())
                .stream().map(Task::getId).toList();

        List<Comment> events = allTaskIds.isEmpty()
                ? List.of()
                : commentRepo.findByTaskIdInAndContentStartingWithOrderByIdAsc(allTaskIds, PREFIX);

        List<Integer> order = new ArrayList<>(base);
        long last = 0L;

        for (Comment ev : events) {
            last = Math.max(last, ev.getId());
            String json = ev.getContent().substring(PREFIX.length()).trim();
            Map<?,?> map;
            try {
                map = om.readValue(json, Map.class);
            } catch (Exception ignore) { continue; }

            String type = (String) map.get("type");
            Map<?,?> container = (Map<?,?>) map.get("container");
            if (container == null) continue;
            String pid = (String) container.get("projectId");
            Integer sid = container.get("sprintId") == null ? null : (Integer) container.get("sprintId");
            if (!Objects.equals(pid, c.projectId()) || !Objects.equals(sid, c.sprintId())) {
                // Event không áp vào container này
                continue;
            }

            if ("order.reorder".equals(type)) {
                List<Map<String,Object>> ops = (List<Map<String,Object>>) map.get("ops");
                applyReorder(order, ops);
            } else if ("order.snapshot".equals(type)) {
                List<Integer> snapshot = (List<Integer>) map.get("snapshot");
                if (snapshot != null && !snapshot.isEmpty()) {
                    // snapshot chỉ giữ các task còn tồn tại trong container hiện tại
                    Set<Integer> set = new HashSet<>(base);
                    order = snapshot.stream().filter(set::contains).collect(Collectors.toCollection(ArrayList::new));
                    // append các task “mới” chưa có trong snapshot
                    for (Integer id : base) if (!order.contains(id)) order.add(id);
                }
            }
        }
        // đảm bảo mọi task hiện có đều nằm trong order
        for (Integer id : base) if (!order.contains(id)) order.add(id);

        return new OrderState(order, last);
    }

    @Transactional
    @Override
    public long appendReorderEvent(Container c, String userId, List<EventOp> ops, int attachToTaskId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "order.reorder");
        payload.put("versionBaseOnCommentId", Instant.now().toEpochMilli()); // informative
        payload.put("container", Map.of("projectId", c.projectId(), "sprintId", c.sprintId()));
        payload.put("ops", ops.stream().map(op -> Map.of(
                "taskId", op.taskId(),
                "before", op.beforeTaskId(),
                "after", op.afterTaskId(),
                "atIndex", op.atIndex()
        )).toList());
        payload.put("at", Instant.now().toString());
        payload.put("userId", userId);

        String content;
        try {
            content = PREFIX + om.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Serialize order event failed", e);
        }

        Comment cmt = new Comment();
        cmt.setTaskId(attachToTaskId);
        cmt.setAuthorId(userId);
        cmt.setContent(content);
        Comment saved = commentRepo.save(cmt);
        return saved.getId();
    }

    @SuppressWarnings("unchecked")
    private static void applyReorder(List<Integer> order, List<Map<String,Object>> ops) {
        if (ops == null) return;
        for (Map<String,Object> op : ops) {
            Integer taskId = (Integer) op.get("taskId");
            Integer before = (Integer) op.get("before");
            Integer after  = (Integer) op.get("after");
            Integer atIdx  = (Integer) op.get("atIndex");

            // remove current
            order.remove(taskId);

            int insertAt = -1;
            if (before != null) insertAt = Math.max(0, order.indexOf(before));
            else if (after != null)  insertAt = order.indexOf(after) + 1;
            else if (atIdx != null)  insertAt = Math.max(0, Math.min(atIdx, order.size()));
            else insertAt = order.size(); // append

            if (insertAt < 0 || insertAt > order.size()) insertAt = order.size();
            order.add(insertAt, taskId);
        }
    }
}
