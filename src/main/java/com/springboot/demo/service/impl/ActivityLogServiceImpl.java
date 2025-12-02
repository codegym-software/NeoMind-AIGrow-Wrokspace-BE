package com.springboot.demo.service.impl;

import com.springboot.demo.model.ActivityLog;
import com.springboot.demo.model.Task;
import com.springboot.demo.model.enums.ActivityAction;
import com.springboot.demo.repository.ActivityLogRepository;
import com.springboot.demo.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository repo;

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
        if (before.getTitle() != null || after.getTitle() != null) {
            if (!java.util.Objects.equals(before.getTitle(), after.getTitle())) {
                logFieldChange(after.getProjectId(), after.getId(), userId, "title", before.getTitle(), after.getTitle());
            }
        }
        if (before.getDescription() != null || after.getDescription() != null) {
            if (!java.util.Objects.equals(before.getDescription(), after.getDescription())) {
                logFieldChange(after.getProjectId(), after.getId(), userId, "description", before.getDescription(), after.getDescription());
            }
        }
        String beforeCol = before.getStatusColumn() != null ? before.getStatusColumn().getName() : null;
        String afterCol = after.getStatusColumn() != null ? after.getStatusColumn().getName() : null;
        if (beforeCol != null || afterCol != null) {
            if (!java.util.Objects.equals(beforeCol, afterCol)) {
                logFieldChange(after.getProjectId(), after.getId(), userId, "statusColumn", beforeCol, afterCol);
            }
        }
        String beforeAssignee = before.getAssigneeId();
        String afterAssignee = after.getAssigneeId();
        if (beforeAssignee != null || afterAssignee != null) {
            if (!java.util.Objects.equals(beforeAssignee, afterAssignee)) {
                logFieldChange(after.getProjectId(), after.getId(), userId, "assigneeId", beforeAssignee, afterAssignee);
            }
        }
        String beforeTags = before.getTags();
        String afterTags = after.getTags();
        if (beforeTags != null || afterTags != null) {
            if (!java.util.Objects.equals(beforeTags, afterTags)) {
                logFieldChange(after.getProjectId(), after.getId(), userId, "tags", beforeTags, afterTags);
            }
        }
        Integer beforeOrder = before.getOrderIndex();
        Integer afterOrder = after.getOrderIndex();
        if (beforeOrder != null || afterOrder != null) {
            if (!java.util.Objects.equals(beforeOrder, afterOrder)) {
                logFieldChange(after.getProjectId(), after.getId(), userId, "orderIndex", beforeOrder != null ? String.valueOf(beforeOrder) : null, afterOrder != null ? String.valueOf(afterOrder) : null);
            }
        }
        java.time.LocalDate beforeDue = before.getDueDate();
        java.time.LocalDate afterDue = after.getDueDate();
        if (beforeDue != null || afterDue != null) {
            if (!java.util.Objects.equals(beforeDue, afterDue)) {
                logFieldChange(after.getProjectId(), after.getId(), userId, "dueDate", beforeDue != null ? beforeDue.toString() : null, afterDue != null ? afterDue.toString() : null);
            }
        }
        Integer beforeSprint = before.getSprintId();
        Integer afterSprint = after.getSprintId();
        if (beforeSprint != null || afterSprint != null) {
            if (!java.util.Objects.equals(beforeSprint, afterSprint)) {
                logFieldChange(after.getProjectId(), after.getId(), userId, "sprintId", beforeSprint != null ? String.valueOf(beforeSprint) : null, afterSprint != null ? String.valueOf(afterSprint) : null);
            }
        }
        com.springboot.demo.model.enums.TaskPriority beforePr = before.getPriority();
        com.springboot.demo.model.enums.TaskPriority afterPr = after.getPriority();
        if (beforePr != null || afterPr != null) {
            if (!java.util.Objects.equals(beforePr, afterPr)) {
                logFieldChange(after.getProjectId(), after.getId(), userId, "priority", beforePr != null ? beforePr.name() : null, afterPr != null ? afterPr.name() : null);
            }
        }
    }

    @Override
    public void logFieldChange(String projectId, Long taskId, String userId, String fieldName, String fromValue, String toValue) {
        ActivityLog log = new ActivityLog();
        log.setLogId(UUID.randomUUID().toString());
        log.setProjectId(projectId);
        log.setTaskId(taskId);
        log.setUserId(userId);
        log.setAction(ActivityAction.updated);
        log.setFieldName(fieldName);
        String from = fromValue == null ? "" : fromValue;
        String to = toValue == null ? "" : toValue;
        log.setDescription("" + from + " -> " + to);
        repo.save(log);
    }
}
