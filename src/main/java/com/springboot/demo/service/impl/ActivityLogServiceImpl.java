package com.springboot.demo.service.impl;

import com.springboot.demo.model.ActivityLog;
import com.springboot.demo.model.Task;
import com.springboot.demo.model.enums.ActivityAction;
import com.springboot.demo.repository.ActivityLogRepository;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository repo;
    private final UserRepository userRepo;
    private final TaskRepository taskRepo;

    @Override
    public void logTaskCreated(Task task, String userId) {
        ActivityLog log = new ActivityLog();
        log.setLogId(UUID.randomUUID().toString());
        log.setProjectId(task.getProjectId());
        log.setTaskId(task.getId());
        log.setUserId(userId);
        log.setAction(ActivityAction.created);
        log.setDescription("Task created");
        repo.save(log);
    }

    @Override
    public void logTaskUpdated(Task before, Task after, String userId) {
        if (before == null || after == null) return;
        String projectId = after.getProjectId();
        Long taskId = after.getId();

        if (!(before.getTitle() == null && after.getTitle() == null) && !java.util.Objects.equals(before.getTitle(), after.getTitle())) {
            logFieldChange(projectId, taskId, userId, "title");
        }
        if (!(before.getDescription() == null && after.getDescription() == null) && !java.util.Objects.equals(before.getDescription(), after.getDescription())) {
            logFieldChange(projectId, taskId, userId, "description");
        }
        String beforeCol = before.getStatusColumn() != null ? before.getStatusColumn().getName() : null;
        String afterCol = after.getStatusColumn() != null ? after.getStatusColumn().getName() : null;
        if (!(beforeCol == null && afterCol == null) && !java.util.Objects.equals(beforeCol, afterCol)) {
            logFieldChange(projectId, taskId, userId, "statusColumn");
        }
        String beforeAssignee = before.getAssigneeId();
        String afterAssignee = after.getAssigneeId();
        if (!(beforeAssignee == null && afterAssignee == null) && !java.util.Objects.equals(beforeAssignee, afterAssignee)) {
            logFieldChange(projectId, taskId, userId, "assigneeId");
        }
        String beforeTags = before.getTags();
        String afterTags = after.getTags();
        if (!(beforeTags == null && afterTags == null) && !java.util.Objects.equals(beforeTags, afterTags)) {
            logFieldChange(projectId, taskId, userId, "tags");
        }
        Integer beforeOrder = before.getOrderIndex();
        Integer afterOrder = after.getOrderIndex();
        if (!(beforeOrder == null && afterOrder == null) && !java.util.Objects.equals(beforeOrder, afterOrder)) {
            logFieldChange(projectId, taskId, userId, "orderIndex");
        }
        java.time.LocalDate beforeDue = before.getDueDate();
        java.time.LocalDate afterDue = after.getDueDate();
        if (!(beforeDue == null && afterDue == null) && !java.util.Objects.equals(beforeDue, afterDue)) {
            logFieldChange(projectId, taskId, userId, "dueDate");
        }
        Integer beforeSprint = before.getSprintId();
        Integer afterSprint = after.getSprintId();
        if (!(beforeSprint == null && afterSprint == null) && !java.util.Objects.equals(beforeSprint, afterSprint)) {
            logFieldChange(projectId, taskId, userId, "sprintId");
        }
        com.springboot.demo.model.enums.TaskPriority beforePr = before.getPriority();
        com.springboot.demo.model.enums.TaskPriority afterPr = after.getPriority();
        if (!(beforePr == null && afterPr == null) && !java.util.Objects.equals(beforePr, afterPr)) {
            logFieldChange(projectId, taskId, userId, "priority");
        }
    }

    @Override
    public void logFieldChange(String projectId, Long taskId, String userId, String fieldName) {
        ActivityLog log = new ActivityLog();
        log.setLogId(UUID.randomUUID().toString());
        log.setProjectId(projectId);
        log.setTaskId(taskId);
        log.setUserId(userId);
        log.setAction(ActivityAction.updated);
        log.setFieldName(fieldName);
        String userName = userRepo.findById(userId)
                .map(u -> u.getDisplayName())
                .orElse(userId);
        String taskTitle = taskRepo.findById(taskId)
                .map(t -> t.getTitle())
                .orElse(String.valueOf(taskId));
        log.setDescription(userName + " updated field \"" + fieldName + "\" on task \"" + taskTitle + "\"");
        repo.save(log);
    }
}
